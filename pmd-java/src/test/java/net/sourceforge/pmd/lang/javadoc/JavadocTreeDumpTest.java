/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.javadoc;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.Test;

import net.sourceforge.pmd.lang.ast.test.BaseParsingHelper;
import net.sourceforge.pmd.lang.ast.test.BaseTreeDumpTest;
import net.sourceforge.pmd.lang.ast.test.RelevantAttributePrinter;

/**
 *
 */
public class JavadocTreeDumpTest extends BaseTreeDumpTest {

    public JavadocTreeDumpTest() {
        super(new RelevantAttributePrinter(), ".javadoc");
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
