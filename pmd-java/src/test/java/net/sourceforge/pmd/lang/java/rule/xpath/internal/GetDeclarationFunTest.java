/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.xpath.internal;

import org.junit.jupiter.api.Test;

import net.sourceforge.pmd.lang.rule.Rule;


/**
 * @author Clément Fournier
 * @since 7.0.0
 */
class GetDeclarationFunTest extends BaseXPathFunctionTest {


    @Test
    void testFetchVarDeclarations() {
        Rule rule = makeXpathRuleFromXPath("//VariableAccess ! pmd-java:declaratorId()");
        String code = "interface O { class Foo { int i = 0; {i++;i++;} } }";

        assertFinds(rule, 1, code);
    }

    @Test
    void testFetchVarDeclarations2() {
        Rule rule = makeXpathRuleFromXPath("//VariableAccess ! pmd-java:declaratorId()");
        String code = "interface O { class Foo { int i = 0, j = 1; {i++;j++;} } }";

        assertFinds(rule, 2, code);
    }
}
