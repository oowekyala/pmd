/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.cfa.FlowGraph;
import net.sourceforge.pmd.lang.java.ast.ASTBlock;
import net.sourceforge.pmd.lang.java.ast.ASTConstructorDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclaration;
import net.sourceforge.pmd.lang.java.ast.JavaNode;
import net.sourceforge.pmd.lang.java.cfa.internal.JavaCfa;

/**
 * @author Clément Fournier
 */
public class CfgTestRule extends AbstractJavaRule {


    private static final ByteArrayOutputStream bos = new ByteArrayOutputStream(4096 << 3);
    private static final PrintStream out = new PrintStream(bos);
    private static volatile int count;

    @Override
    public Object visit(ASTMethodDeclaration node, Object data) {
        if (node.getBody() != null) {
            buildCfg(node.getBody(), node.getName(), (RuleContext) data);
        }
        return data;
    }

    private void buildCfg(ASTBlock node, String name, RuleContext data) {
        long st = System.nanoTime();
        FlowGraph<JavaNode> cfg = JavaCfa.INSTANCE.getBuilder().buildCfg(node);
        long time = System.nanoTime() - st;
        String message = data.getSourceCodeFilename() + "#" + name + "," + cfg.getBlocks().size() + "," + time;
        synchronized (out) {
            out.println(message);
        }
    }

    @Override
    public Object visit(ASTConstructorDeclaration node, Object data) {
        buildCfg(node.getBody(), "new", (RuleContext) data);
        return data;
    }

    @Override
    public void end(RuleContext ctx) {
        synchronized (out) {
            if (count++ == 400) {
                Path file = Paths.get("/home/clifrr/.pmd/cfg/report.csv");
                try {
                    Files.createDirectories(file.getParent());

                    try (OutputStream os = Files.newOutputStream(file);
                         PrintStream out = new PrintStream(os)) {
                        out.println(bos.toString());
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
