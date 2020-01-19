/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.rule;

import java.util.List;

import net.sourceforge.pmd.Rule;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.rule.AbstractRuleChainVisitor;
import net.sourceforge.pmd.lang.xpath.ast.ASTXPathRoot;
import net.sourceforge.pmd.lang.xpath.ast.XPathNode;
import net.sourceforge.pmd.lang.xpath.ast.XPathSideEffectingVisitor;
import net.sourceforge.pmd.lang.xpath.ast.XPathVisitor;


/**
 *
 */
public class XPathRuleChainVisitor extends AbstractRuleChainVisitor {


    @Override
    protected void indexNodes(List<Node> nodes, RuleContext ctx) {
        XPathSideEffectingVisitor<Void> javaParserVistor = new XPathSideEffectingVisitor<Void>() {
            // Perform a visitation of the AST to index nodes which need
            // visiting by type


            @Override
            public void visit(XPathNode node, Void v) {
                indexNode(node);
                XPathSideEffectingVisitor.super.visit(node, v);
            }
        };

        for (final Node node : nodes) {
            javaParserVistor.visit((ASTXPathRoot) node, null);
        }
    }


    @Override
    protected void visit(Rule rule, Node node, RuleContext ctx) {

        // Rule better either be a vfParserVisitor, or a XPathRule
        if (rule instanceof XPathVisitor) {
            ((XPathNode) node).jjtAccept((PmdXPathRule) rule, ctx);
        } else {
            ((net.sourceforge.pmd.lang.rule.XPathRule) rule).evaluate(node, ctx);
        }

    }

}
