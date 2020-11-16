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
import net.sourceforge.pmd.lang.javadoc.ast.JdocBlockTag;
import net.sourceforge.pmd.lang.rule.xpath.Attribute;

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

        @Override
        protected boolean ignoreAttribute(@NonNull Node node, @NonNull Attribute attribute) {
            if (super.ignoreAttribute(node, attribute)) {
                return true;
            }
            if (attribute.getName().equals("ParamName") && node instanceof JdocBlockTag) {
                return !"@param".equals(((JdocBlockTag) node).getTagName());
            }
            return false;
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

    @Test
    public void testParamTag() {
        doTest("paramTag");
    }
}
