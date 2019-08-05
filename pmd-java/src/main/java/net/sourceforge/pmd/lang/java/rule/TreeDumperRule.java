/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

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
    private static Map<String, DataOutputStream> outstreams = new ConcurrentHashMap<>();
    private final PropertyDescriptor<String> dumpRoot =
        PropertyFactory.stringProperty("dumpRoot")
                       .desc("make something")
                       .defaultValue("none")
                       .build();

    /*
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
            DataOutputStream stream = getWriter(node, (RuleContext) data, Paths.get(getProperty(dumpRoot)));
            synchronized (stream) {
                dump(node, stream);
                stream.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return data;
    }

    private DataOutputStream openOrFetchStream(Path outPath, String packageName, String fileName) throws IOException {
        Files.createDirectories(outPath.getParent());

        // this needs to be atomic so we use computeIfAbsent
        return outstreams.computeIfAbsent(packageName, pname -> {
            OutputStream os;
            try {
                os = Files.newOutputStream(outPath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return new DataOutputStream(new BufferedOutputStream(os));
        });
    }

    private DataOutputStream getWriter(ASTCompilationUnit node, RuleContext data, Path dumpRoot) throws IOException {
        ASTPackageDeclaration pack = node.getPackageDeclaration();
        String fname = data.getSourceCodeFile().getName();
        final String packageName = pack == null ? "" : pack.getPackageNameImage();
        return openOrFetchStream(getFlatDumpPath(node, dumpRoot), packageName, fname);
    }

    @Override
    public void afterAnalysis(RuleContext ctx) {
        super.end(ctx);
        System.out.println("done!");

        for (Entry<String, DataOutputStream> stream : outstreams.entrySet()) {
            try {
                stream.getValue().close();
                outstreams.remove(stream.getKey());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    static Path getFlatDumpPath(ASTCompilationUnit node, Path dumpRoot) {
        ASTPackageDeclaration pack = node.getPackageDeclaration();
        final String packageName = pack == null ? "<empty_package>" : pack.getPackageNameImage();
        // TODO collisions! For now if they hash to the same value (+-) and have same simple name they collide
        int hash = Math.abs(packageName.hashCode());

        Path bucket = dumpRoot.resolve(String.format("%08x", Math.abs(hash)).substring(0, 2));
        return bucket.resolve(packageName);
    }

    // TODO better to dump in post order, then we can use jjtOpen/jjtClose during construction
    // TODO save offsets of different trees in header?

    private static void dump(JavaNode root, DataOutputStream out) throws IOException {
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


        out.writeByte(root.jjtGetId());
        writeNullableStr(out, root.getImage());
        out.writeInt(root.getBeginLine());
        out.writeInt(root.getEndLine());

        for (int i = 0; i < root.jjtGetNumChildren(); i++) {
            dump(root.jjtGetChild(i), out);
        }

        out.writeByte(END_MARKER);
    }

    static void writeNullableStr(DataOutputStream dos, String s) throws IOException {
        dos.writeBoolean(s == null);
        if (s != null) {
            dos.writeUTF(s);
        }
    }

    static String readNullableStr(DataInputStream dos) throws IOException {
        boolean isNull = dos.readBoolean();
        return isNull ? null : dos.readUTF();
    }


    static List<RootNode> readPackageFile(DataInputStream in) throws IOException {
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
        // eg store ambiguous name (better, just whether node was ambiguous),
        // and rebuild as ambiguous name


        int childIdx = 0;
        while (nextType != -1) {

            if (nextType == END_MARKER) {
                //stop children
                stack.pop();
                childIdx = 0;
            } else {
                final JavaNode node = NodeFactory.jjtCreate(null, nextType);
                node.setImage(readNullableStr(in));
                in.readInt();
                in.readInt();

                stack.push(node);

                if (stack.isEmpty()) {
                    // new tree
                    stack.push(node);
                    result.add((RootNode) node);
                }

                JavaNode top = stack.peek();
                top.jjtAddChild(node, childIdx++);
            }

            try {
                nextType = in.readByte();
            } catch (EOFException eof) {
                break;
            }
        }
        return result;
    }

}
