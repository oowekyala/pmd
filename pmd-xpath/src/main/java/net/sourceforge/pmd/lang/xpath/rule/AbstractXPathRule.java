/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.rule;

import java.util.List;

import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.LanguageRegistry;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.rule.AbstractRule;
import net.sourceforge.pmd.lang.xpath.XPathLanguageModule;
import net.sourceforge.pmd.lang.xpath.ast.ASTIfExpr;
import net.sourceforge.pmd.lang.xpath.ast.ASTXPathRoot;
import net.sourceforge.pmd.lang.xpath.ast.XPathNode;


/**
 * Base class for XPath rules.
 */
public abstract class AbstractXPathRule extends AbstractRule implements PmdXPathRule {

    protected AbstractXPathRule() {
        super.setLanguage(LanguageRegistry.getLanguage(XPathLanguageModule.NAME));
    }


    @Override
    public void apply(List<? extends Node> nodes, RuleContext ctx) {
        visitAll(nodes, ctx);
    }


    private void visitAll(List<? extends Node> nodes, RuleContext ctx) {
        for (Node element : nodes) {
            if (element instanceof ASTXPathRoot) {
                visit((ASTXPathRoot) element, ctx);
            } else {
                visit((XPathNode) element, ctx);
            }
        }
    }

}
