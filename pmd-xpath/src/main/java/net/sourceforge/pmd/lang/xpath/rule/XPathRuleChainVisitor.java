/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.rule;

import java.util.List;

import net.sourceforge.pmd.Rule;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.rule.AbstractRuleChainVisitor;
import net.sourceforge.pmd.lang.xpath.ast.ASTXPathRoot;
import net.sourceforge.pmd.lang.xpath.ast.AbstractXPathGenericVisitor;
import net.sourceforge.pmd.lang.xpath.ast.XPathNode;
import net.sourceforge.pmd.lang.xpath.ast.XPathGenericVisitor;


/**
 * @author Clément Fournier
 * @since 6.7.0
 */
public class XPathRuleChainVisitor extends AbstractRuleChainVisitor {


    @Override
    protected void indexNodes(List<Node> nodes, RuleContext ctx) {
        XPathGenericVisitor<?> javaParserVistor = new AbstractXPathGenericVisitor<Object>() {
            // Perform a visitation of the AST to index nodes which need
            // visiting by type


            @Override
            public Object visit(XPathNode node, Object data) {
                indexNode(node);
                return super.visit(node, data);
            }
        };

        for (final Node node : nodes) {
            javaParserVistor.visit((ASTXPathRoot) node, null);
        }
    }


    @Override
    protected void visit(Rule rule, Node node, RuleContext ctx) {

        // Rule better either be a vfParserVisitor, or a XPathRule
        if (rule instanceof XPathGenericVisitor) {
            ((XPathNode) node).jjtAccept((PmdXPathRule) rule, ctx);
        } else {
            ((net.sourceforge.pmd.lang.rule.XPathRule) rule).evaluate(node, ctx);
        }

    }

}
