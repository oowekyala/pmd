/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.types;

import static net.sourceforge.pmd.lang.java.types.InvocationMatcher.parse;
import static org.hamcrest.Matchers.equalTo;

import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.junit.Test;

import net.sourceforge.pmd.lang.java.BaseParserTest;
import net.sourceforge.pmd.lang.java.ast.ASTConstructorCall;
import net.sourceforge.pmd.lang.java.ast.ASTMethodCall;
import net.sourceforge.pmd.lang.java.ast.InvocationNode;

public class InvocationMatcherTest extends BaseParserTest {

    @Test
    public void testSimpleMatcher() {

        ASTMethodCall call =
            java.parse("class Foo {{ Integer.valueOf('c'); }}")
                .descendants(ASTMethodCall.class).firstOrThrow();

        assertMatch(call, "_#valueOf(int)");
        assertMatch(call, "java.lang.Integer#valueOf(int)");
        assertMatch(call, "java.lang.Integer#_(int)");
        assertMatch(call, "java.lang.Integer#_(_*)");

        assertNoMatch(call, "java.lang.Integer#valueOf(char)");
        assertNoMatch(call, "java.lang.Integer#valueOf2(_*)");
        assertNoMatch(call, "java.lang.Object#valueOf(_*)");
    }

    @Test
    public void testCtorMatchers() {

        ASTConstructorCall call =
            java.parse("class Foo {{ new java.util.ArrayList('c'); }}")
                .descendants(ASTConstructorCall.class).firstOrThrow();

        assertMatch(call, "_#new(int)");
        assertMatch(call, "java.util.ArrayList#new(int)");
        assertMatch(call, "java.util.ArrayList#_(int)");
        assertMatch(call, "java.util.ArrayList#_(_*)");

        assertNoMatch(call, "java.util.ArrayList#new()");
        assertNoMatch(call, "java.util.ArrayList#_()");

        assertNoMatch(call, "java.util.List#new(_*)");
        assertNoMatch(call, "java.util.List#_(_*)");
        assertNoMatch(call, "java.lang.Object#new(int)");
    }

    @Test
    public void testArray() {

        ASTMethodCall call =
            java.parse("class Foo {{ new int[0].toString(); }}")
                .descendants(ASTMethodCall.class).firstOrThrow();

        assertMatch(call, "int[]#toString()");
        assertMatch(call, "_#toString()");
        assertMatch(call, "int[]#_()");
        assertMatch(call, "int[]#_(_*)");
        assertMatch(call, "_#_(_*)");

        assertNoMatch(call, "_#new(int)");
        assertNoMatch(call, "_[][]#_(_*)");
        // maybe we should support this one later
        assertNoMatch(call, "_[]#toString()");
    }

    @Test
    public void testWhitespaceErrorMessage() {

        parse("_#_(int,int)"); // does not fail
        IllegalArgumentException e = Assert.assertThrows(IllegalArgumentException.class, () -> parse("_#_(int, int)"));
        MatcherAssert.assertThat(e.getMessage(), equalTo("Expected type at index 8:\n"
                                                             + "    \"_#_(int, int)\"\n"
                                                             + "             ^\n"));

    }

    private void assertMatch(InvocationNode call, String sig) {
        Assert.assertTrue(sig + " should match " + call,
                          parse(sig).matchesCall(call));
    }

    private void assertNoMatch(InvocationNode call, String s) {
        Assert.assertFalse(s + " should not match " + call,
                           parse(s).matchesCall(call));
    }

}
