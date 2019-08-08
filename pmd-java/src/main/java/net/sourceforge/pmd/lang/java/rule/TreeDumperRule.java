/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.mutable.MutableInt;

import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.ast.RootNode;
import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit;
import net.sourceforge.pmd.lang.java.ast.ASTPackageDeclaration;
import net.sourceforge.pmd.lang.java.ast.JavaNode;
import net.sourceforge.pmd.lang.java.ast.NodeFactory;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertyFactory;

/**
 * @author Clément Fournier
 */
public class TreeDumperRule extends AbstractJavaRule {

    private static final byte END_MARKER = -2;
    private static Map<String, StubFile> outstreams = new ConcurrentHashMap<>();
    private final PropertyDescriptor<String> dumpRoot =
        PropertyFactory.stringProperty("dumpRoot")
                       .desc("make something")
                       .defaultValue("none")
                       .build();

    private int numSaved;

    /*
        TODO OK we don't need to save all the files of a project. Just
        save in-memory the files that are being accessed through the project.
        An index would serve as custom swap but is not necessary RN.
        Incremental analysis completes this mechanism.
        Overarching goals:
        * parse all files lazily
        * reuse ASTs as much as possible

        Implementation goals:
        * Reuse incremental analysis
        * Be language-independent
        * Don't save huge cache files to disk


        TODO by saving a whole directory into a package, the cache would
         be inconsistent if a file was added or removed in the dir.

        TODO things to care about:
         * aborted caching should leave the cache in a decent state,
         or at least the next run should not use it
     */

    public TreeDumperRule() {
        definePropertyDescriptor(dumpRoot);
    }

    @Override
    public Object visit(ASTCompilationUnit node, Object data) {

        try {
            StubFile stubFile = getWriter(node, (RuleContext) data, Paths.get(getProperty(dumpRoot)));
            synchronized (stubFile) {
                numSaved = 0;
                dump(node, stubFile.outputStream);
                stubFile.recordTreeEntry(numSaved);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return data;
    }

    private StubFile openOrFetchStream(Path outPath, String packageName, String fileName) throws IOException {
        Files.createDirectories(outPath.getParent());

        // this needs to be atomic so we use computeIfAbsent
        return outstreams.computeIfAbsent(packageName, pname -> {
            OutputStream os;
            try {
                os = Files.newOutputStream(outPath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return new StubFile(new DataOutputStream(new BufferedOutputStream(os)));
        });
    }

    private StubFile getWriter(ASTCompilationUnit node, RuleContext data, Path dumpRoot) throws IOException {
        ASTPackageDeclaration pack = node.getPackageDeclaration();
        String fname = data.getSourceCodeFile().getName();
        final String packageName = pack == null ? "" : pack.getPackageNameImage();
        return openOrFetchStream(getFlatDumpPath(node, dumpRoot), packageName, fname);
    }

    @Override
    public void afterAnalysis(RuleContext ctx) {
        super.end(ctx);
        System.out.println("done!");

        IntSummaryStatistics totalStats = new IntSummaryStatistics();
        IntSummaryStatistics nodesByBlob = new IntSummaryStatistics();
        IntSummaryStatistics acusByBlob = new IntSummaryStatistics();
        Map<Long, MutableInt> numAcusByBlob = new HashMap<>();
        for (Iterator<Entry<String, StubFile>> iterator = outstreams.entrySet().iterator(); iterator.hasNext(); ) {
            Entry<String, StubFile> entry = iterator.next();
            try {
                final StubFile stubFile = entry.getValue();
                stubFile.close();
                totalStats.combine(stubFile.getStats());
                nodesByBlob.accept((int) stubFile.getStats().getSum());
                final long numAcus = stubFile.getStats().getCount();
                acusByBlob.accept((int) numAcus);
                numAcusByBlob.computeIfAbsent(numAcus, l -> new MutableInt()).increment();
                iterator.remove();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        System.out.println(
            "Saved " + totalStats.getSum() + " nodes from " + totalStats.getCount() + " compilation units into "
                + nodesByBlob.getCount() + " stub files");
        System.out.println(
            "Averaging " + totalStats.getAverage() + "+-" + totalStats.getStdDev()
                + " nodes per ACU, or " + nodesByBlob.getAverage() + " by blob");

        numAcusByBlob.entrySet().stream()
                     .sorted(Comparator.comparing(Entry::getKey))
                     .forEach(it -> System.out.println(it.getKey() + "," + it.getValue()));


        System.out.println("Averaging " + acusByBlob.getAverage() + "+-" + acusByBlob.getStdDev()
                               + " ACUs per blob [" + acusByBlob.getMin() + ", "
                               + acusByBlob.getMax() + "]");

    }

    private void dump(JavaNode root, DataOutputStream out) throws IOException {
        // So here:
        /*
            A node's structural footprint is 1B type + 1B end marker = 2B
            That is nothing, saving the whole of JDK12 (18,000 files, 234MB of text)
            is about 30MB. Other attributes take a lot of space though,
            eg saving just image for all nodes makes that about 120MB
            (not all nodes need an image though). Also, we probably need
            to save text coordinates

            Also a very big contributor to total cache size is filesystem metadata.
            Using as few files as possible is important.
         */

        numSaved++;

        out.writeByte(root.jjtGetId());
        root.metaModel().write(root, out);

        for (int i = 0; i < root.jjtGetNumChildren(); i++) {
            final JavaNode child = root.jjtGetChild(i);
            if (!isNotDumped(child)) {
                dump(child, out);
            }
        }

        out.writeByte(END_MARKER);
    }

    // TODO better to dump in post order, then we can use jjtOpen/jjtClose during construction
    // TODO save offsets of different trees in header?

    static Path getFlatDumpPath(ASTCompilationUnit node, Path dumpRoot) {
        /*
            TODO this layout scheme is better than completely mirroring
                package structure with directories, yet:
                  10% of packages have           1 ACU
                  30% of packages have less than 3 ACUs
                  50%                            5
                  80%                            23

                So this hashing method is still too unfair.
         */


        ASTPackageDeclaration pack = node.getPackageDeclaration();
        final String packageName = pack == null ? "<empty_package>" : pack.getPackageNameImage();
        int hash = Math.abs(packageName.hashCode());

        Path bucket = dumpRoot.resolve(String.format("%08x", Math.abs(hash)).substring(0, 2));
        return bucket.resolve(pack == null ? ".javast" : packageName + ".javast");
    }

    private static boolean isNotDumped(JavaNode node) {
        // this writes the structure of the compilation unit that is
        // visible to other declarations, without caring about the
        // contents of methods (expressions take the most part of the trees).
        return false;
        //        return node instanceof ASTBlock || node instanceof ASTInitializer
        //            || node instanceof ASTBlockStatement && node.jjtGetParent() instanceof ASTConstructorDeclaration;
    }


    public static List<RootNode> readPackageFile(DataInputStream in) throws IOException {
        Stack<JavaNode> stack = new Stack<>();
        List<RootNode> result = new ArrayList<>();
        byte nextType;

        try {
            nextType = in.readByte();
        } catch (EOFException eof) {
            return result;
        }

        // TODO lazy parsing, maybe we only need one of the trees
        // TODO let children rewrite themselves
        // eg store ambiguous name, parenthesized expression

        while (nextType != -1) {

            if (nextType == END_MARKER) {
                //stop children
                stack.pop();
            } else {
                final JavaNode node = NodeFactory.jjtCreate(null, nextType);
                node.metaModel().readInto(node, in);

                if (stack.isEmpty()) {
                    // new tree
                    stack.push(node);
                    result.add((RootNode) node);
                } else {
                    JavaNode top = stack.peek();
                    stack.push(node);
                    top.jjtAddChild(node, top.jjtGetNumChildren());
                }
            }

            try {
                nextType = in.readByte();
            } catch (EOFException eof) {
                break;
            }
        }
        return result;
    }

    private static class IntSummaryStatistics extends java.util.IntSummaryStatistics {

        private final List<Integer> stats = new ArrayList<>();

        @Override
        public void accept(int value) {
            super.accept(value);
            stats.add(value);
        }

        private double getStdDev() {
            double mean = getAverage();
            double dev = stats.stream().parallel().reduce(0d, (acc, i) -> {
                double diff = i - mean;
                return acc + diff * diff;
            }, Double::sum);

            return Math.sqrt(dev / getCount() - 1); // todo div/0
        }

    }

    private static class StubFile implements Closeable {

        private final IntSummaryStatistics stats = new IntSummaryStatistics();
        private final DataOutputStream outputStream;

        StubFile(OutputStream outputStream) {
            this.outputStream = new DataOutputStream(new BufferedOutputStream(outputStream));
        }

        void recordTreeEntry(int numNodes) {
            stats.accept(numNodes);
        }

        public IntSummaryStatistics getStats() {
            return stats;
        }

        @Override
        public void close() throws IOException {
            outputStream.close();
        }
    }

}
