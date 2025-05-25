/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.security;

import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.java.ast.JavaNode;
import net.sourceforge.pmd.lang.java.ast.JavadocCommentOwner;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;

public class JdocTestRule extends AbstractJavaRule {


    private static class State {

        public int fileId = 0;
        public long numResolved = 0;
        public long numerrors = 0;
        public long numUnresolved = 0;

        void print() {
            System.out.println();
            System.out.println(fileId);
            System.out.println("Resolved: " + numResolved + ", unresolved " + numUnresolved);
            double rate = numResolved / (double) (numUnresolved + numResolved);
            System.out.println("Resolved " + Math.floor(10000 * rate) / 100 + "%");
            System.out.println("Errors\t" + numerrors);
            //            System.out.println("CL cache size\t" + ClasspathSymbolResolver.cacheSize);
            //            System.out.println("CL stream found\t" + ClasspathSymbolResolver.streamFound);
            //            System.out.println("CL class found\t" + ClasspathSymbolResolver.classFound);
        }

        int absorb(State other) {
            fileId += other.fileId;
            numResolved += other.numResolved;
            numUnresolved += other.numUnresolved;
            numerrors += other.numerrors;

            return fileId;
        }

    }

    private static final State STATIC = new State();

    private State state = new State();


    //    @Override
    //    public Object visit(ASTCompilationUnit node, Object data) {
    //        // do nothing
    //        return data;
    //    }

    @Override
    public Object visitJavaNode(JavaNode node, Object data) {

        if (node instanceof JavadocCommentOwner) {
            try {
                ((JavadocCommentOwner) node).getJavadocComment();
                state.numResolved++;
            } catch (Throwable e) {
                System.err.println(node.getReportLocation().startPosToStringWithFile());
                e.printStackTrace();
                state.numerrors++;
                if (e instanceof Error) {
                    // throw e;
                }
            }
        }

        return super.visitJavaNode(node, data);
    }

    @Override
    public void end(RuleContext ctx) {
        super.end(ctx);
        state.fileId++;
        if (state.fileId % 200 == 0) {
            int fid;
            synchronized (STATIC) {
                fid = STATIC.absorb(state);
            }
            state = new State();

            if (fid % 400 == 0) {
                synchronized (STATIC) {
                    if (STATIC.fileId % 400 == 0) {
                        STATIC.print();
                    }
                }
            }
        }
    }
}
