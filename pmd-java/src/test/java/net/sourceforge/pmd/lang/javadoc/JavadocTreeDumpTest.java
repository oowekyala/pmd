/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.javadoc;

import java.util.List;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.Test;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.ast.test.BaseParsingHelper;
import net.sourceforge.pmd.lang.ast.test.BaseTreeDumpTest;
import net.sourceforge.pmd.lang.ast.test.RelevantAttributePrinter;
import net.sourceforge.pmd.lang.javadoc.ast.JavadocNode.JdocCommentData;
import net.sourceforge.pmd.lang.javadoc.ast.JavadocNode.JdocHtmlComment;

/**
 *
 */
public class JavadocTreeDumpTest extends BaseTreeDumpTest {

    public JavadocTreeDumpTest() {
        super(new JavadocAttributesPrinter(), ".javadoc");
    }

    static class JavadocAttributesPrinter extends RelevantAttributePrinter {

        @Override
        protected void fillAttributes(@NonNull Node node, @NonNull List<AttributeInfo> result) {
            super.fillAttributes(node, result);
            if (node instanceof JdocCommentData) {
                result.add(new AttributeInfo("TextData", ((JdocCommentData) node).getData().toString()));
            } else if (node instanceof JdocHtmlComment) {
                result.add(new AttributeInfo("TextData", ((JdocHtmlComment) node).getData().toString()));
            }
        }
    }


    @Override
    public @NonNull BaseParsingHelper<?, ?> getParser() {
        return JavadocParsingHelper.DEFAULT.withResourceContext(getClass());
    }

    @Test
    public void testBigComment() {
        doTest("bigComment");
    }

    @Test
    public void testMethodComment() {
        doTest("methodComment");
    }
}
