/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.opti;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import net.sourceforge.pmd.lang.xpath.ast.ASTAxisStep;
import net.sourceforge.pmd.lang.xpath.ast.ASTExactNameTest;
import net.sourceforge.pmd.lang.xpath.ast.ASTInfixExpr;
import net.sourceforge.pmd.lang.xpath.ast.ASTPathExpr;
import net.sourceforge.pmd.lang.xpath.ast.ASTPathExpr.PathAnchor;
import net.sourceforge.pmd.lang.xpath.ast.ASTVarRef;
import net.sourceforge.pmd.lang.xpath.ast.ASTWildcardNameTest;
import net.sourceforge.pmd.lang.xpath.ast.ASTXPathRoot;
import net.sourceforge.pmd.lang.xpath.ast.Expr;
import net.sourceforge.pmd.lang.xpath.ast.ExprSingle;
import net.sourceforge.pmd.lang.xpath.ast.StepExpr;
import net.sourceforge.pmd.lang.xpath.ast.SyntheticNodeFactory;
import net.sourceforge.pmd.properties.PropertyDescriptor;


/**
 * @author Clément Fournier
 * @since 6.7.0
 */
class XPathQueryImpl implements XPathQuery {

    private final ASTXPathRoot root;
    private final Map<PropertyDescriptor<?>, Object> propertyValues;


    XPathQueryImpl(ASTXPathRoot root, Map<PropertyDescriptor<?>, Object> propertyValues) {
        this.root = Objects.requireNonNull(root);
        this.propertyValues = Objects.requireNonNull(propertyValues);

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
            ExprSingle node = SyntheticNodeFactory.getNodeForValue(value);
            ref.replaceWith(node);
        }
    }


    @Override
    public String toParsableString() {
        return root.toExpressionString();
    }


    /**
     * Expand a union in the first step of a PathExpr starting with //.
     *
     * <p>e.g. {@code //(A|B)/C} can be expanded to {@code //A/C | //B/C},
     * exposing different rulechain queries.
     */
    // TODO
    private void floatUnions() {
        //        Expr main = root.getMainExpr();
        //        if (main instanceof ASTUnionExpr) {
        //            ASTUnionExpr toplevelUnion = (ASTUnionExpr) main;
        //            List<ASTPathExpr> pathExprs = toplevelUnion.getAlternatives().stream()
        //                                                       .filter(e -> e instanceof ASTPathExpr)
        //                                                       .map(e -> (ASTPathExpr) e)
        //                                                       .filter(e -> e.getPathAnchor() == PathAnchor.DESCENDANT_OR_ROOT)
        //                                                       .filter(e -> e.getFirstStep() instanceof ASTParenthesizedExpr
        //                                                               && ((ASTParenthesizedExpr) e.getFirstStep()).getWrappedNode() instanceof ASTUnionExpr)
        //                                                       .collect(Collectors.toList());
        //
        //            for (ASTPathExpr pathExpr : pathExprs) {
        //                pathExpr.
        //
        //            }
        //
        //        } else if (main instanceof ASTPathExpr) {
        //
        //        }
    }

    @Override
    public Map<String/*TODO NodeIdentifier*/, String> getRulechainQueries() {
        floatUnions();
        // From now on, we assume:
        // * all rulechain queries have been floated to the top level UnionExpr
        // * every alternative of the top level union is a PathExpr starting with a nametest

        Map<String, String> rulechains = new HashMap<>();

        Expr main = root.getMainExpr();

        if (main instanceof ASTInfixExpr && ((ASTInfixExpr) main).getOperator().isUnion()) {
            for (ExprSingle expr : ((ASTInfixExpr) main).children()) {
                if (expr instanceof ASTPathExpr) {

                    StepExpr firstStep = ((ASTPathExpr) expr).getFirstStep();
                    if (firstStep.isAxisStep()) {
                        ASTAxisStep axisStep = (ASTAxisStep) firstStep;
                        if (axisStep.getNodeTest() instanceof ASTExactNameTest) {
                            String nodeName = ((ASTExactNameTest) axisStep.getNodeTest()).getNameImage();
                            ((ASTPathExpr) expr).setPathAnchor(PathAnchor.RELATIVE);

                        } else if (axisStep.getNodeTest() instanceof ASTWildcardNameTest) {

                        } else {
                            // Not a NameTest
                        }
                    } else {

                    }
                }
            }
        }

        return null;
    }


    public Map<PropertyDescriptor<?>, Object> getPropertyValues() {
        return propertyValues;
    }


}
