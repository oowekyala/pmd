/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.opti;

import static net.sourceforge.pmd.lang.xpath.ast.Axis.CHILD;
import static net.sourceforge.pmd.lang.xpath.ast.Axis.SELF;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import net.sourceforge.pmd.lang.xpath.ast.ASTAxisStep;
import net.sourceforge.pmd.lang.xpath.ast.ASTExactNameTest;
import net.sourceforge.pmd.lang.xpath.ast.ASTInfixExpr;
import net.sourceforge.pmd.lang.xpath.ast.ASTPathExpr;
import net.sourceforge.pmd.lang.xpath.ast.ASTPathExpr.PathAnchor;
import net.sourceforge.pmd.lang.xpath.ast.ASTVarRef;
import net.sourceforge.pmd.lang.xpath.ast.ASTXPathRoot;
import net.sourceforge.pmd.lang.xpath.ast.Expr;
import net.sourceforge.pmd.lang.xpath.ast.StepExpr;
import net.sourceforge.pmd.lang.xpath.ast.SyntheticNodeFactory;
import net.sourceforge.pmd.properties.PropertyDescriptor;


class XPathQueryImpl implements XPathQuery {

    private static final String ROOT_NAME = "";
    private final ASTXPathRoot root;
    private final Map<PropertyDescriptor<?>, Object> propertyValues;
    private final SyntheticNodeFactory factory;
    private Map<String, String> rulechains;


    XPathQueryImpl(ASTXPathRoot root, Map<PropertyDescriptor<?>, Object> propertyValues) {
        this.root = Objects.requireNonNull(root);
        this.propertyValues = Objects.requireNonNull(propertyValues);
        this.factory = new SyntheticNodeFactory();

        optimise();
    }


    public ASTXPathRoot getRoot() {
        return root;
    }


    public void optimise() {
        inlineProperties();
    }


    /**
     * Inlines the values of the properties.
     *
     * <p>Property inlining helps Saxon's early evaluator,
     * in best cases pruning some alternation branches
     * which cannot be realised (e.g. conditioned by a boolean
     * rule property).
     */
    private void inlineProperties() {
        Map<String, Object> propNamesToValues = new HashMap<>();
        propertyValues.forEach((p, o) -> propNamesToValues.put(p.name(), o));

        for (ASTVarRef ref : root.getFreeVarRefs()) {
            Object value = propNamesToValues.get(ref.getVarName());
            Expr node = factory.getNodeForValue(value);
            ref.replaceWith(node);
        }
    }


    @Override
    public Map<String, String> getRulechainQueries() {
        if (rulechains == null) {
            rulechains = decomposeRulechain(root);
        }
        return rulechains;
    }

    private Map<String, String> decomposeRulechain(ASTXPathRoot root) {
        // root is ""
        Map<String, String> queriesByName = new HashMap<>();
        final Deque<Expr> pending = new ArrayDeque<>();
        pending.push(root.getMainExpr());

        while (!pending.isEmpty()) {
            final Expr node = pending.pop();

            // Must be a PathExpr... that is something like //Type
            exprOk:
            if (node instanceof ASTPathExpr) {
                final ASTPathExpr pathExpr = (ASTPathExpr) node;

                if (pathExpr.getPathAnchor() != PathAnchor.DESCENDANT_OR_ROOT) {
                    break exprOk;
                }
                StepExpr firstStep = pathExpr.getFirstStep();
                if (!(firstStep instanceof ASTAxisStep)) {
                    break exprOk;
                }
                ASTAxisStep firstAxis = (ASTAxisStep) firstStep;
                if (firstAxis.getAxis() != CHILD || !(firstAxis.getNodeTest() instanceof ASTExactNameTest)) {
                    break exprOk;
                }

                // alles gut
                firstAxis.setAxis(SELF);
                pathExpr.setPathAnchor(PathAnchor.RELATIVE);
                ASTExactNameTest nameTest = (ASTExactNameTest) firstAxis.getNodeTest();
                queriesByName.put(nameTest.getNameNode().getLocalName(), pathExpr.toExpressionString());
                continue;
            } else if (node instanceof ASTInfixExpr && ((ASTInfixExpr) node).getOperator().isUnion()) {
                // Or a UnionExpr, that is
                // something like //TypeA | //TypeB
                ASTInfixExpr unionExpr = (ASTInfixExpr) node;
                unionExpr.children().forEach(pending::push);
                continue;
            }

            queriesByName.put(ROOT_NAME, node.toExpressionString());
        }
        return queriesByName;
    }


}
