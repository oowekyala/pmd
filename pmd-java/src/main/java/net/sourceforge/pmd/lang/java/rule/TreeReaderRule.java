/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.openjdk.jmh.infra.Blackhole;

import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.ast.RootNode;
import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertyFactory;

/**
 * @author Clément Fournier
 */
public class TreeReaderRule extends AbstractJavaRule {

    private static final Map<String, Boolean> READ_PACKAGES = new ConcurrentHashMap<String, Boolean>();
    private static int cacheHit = 0;
    private static int cacheMiss = 0;
    private final PropertyDescriptor<String> dumpRoot =
        PropertyFactory.stringProperty("dumpRoot")
                       .desc("make something")
                       .defaultValue("none")
                       .build();
    private final Blackhole blackhole = new Blackhole("Today's password is swordfish. I understand instantiating Blackholes directly is dangerous.");

    public TreeReaderRule() {
        definePropertyDescriptor(dumpRoot);
    }

    @Override
    public Object visit(ASTCompilationUnit node, Object data) {
        final String packName = node.getPackageDeclaration() == null ? ""
                                                                     : node.getPackageDeclaration().getPackageNameImage();
        if (READ_PACKAGES.containsKey(packName)) {
            cacheHit++;
            return data;
        }
        cacheMiss++;

        final Path packageDump = TreeDumperRule.getFlatDumpPath(node, Paths.get(getProperty(dumpRoot)));
        try (DataInputStream stream = new DataInputStream(new BufferedInputStream(new FileInputStream(packageDump.toFile())))) {
            READ_PACKAGES.put(packName, true);
            final List<RootNode> rootNodes = TreeDumperRule.readPackageFile(stream);
            blackhole.consume(rootNodes);
        } catch (Exception e) {
            System.err.println("Exception while reading " + packName);
            e.printStackTrace();
        }

        return data;
    }

    @Override
    public void afterAnalysis(RuleContext ctx) {
        System.out.println(cacheMiss + "/" + cacheHit + (100d * cacheHit / (cacheMiss + cacheHit)));
    }
}
