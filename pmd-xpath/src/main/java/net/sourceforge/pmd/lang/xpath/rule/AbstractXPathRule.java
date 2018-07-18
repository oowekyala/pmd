/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.rule;

import java.util.List;

import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.LanguageRegistry;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.rule.AbstractRule;
import net.sourceforge.pmd.lang.xpath.XPathLanguageModule;
import net.sourceforge.pmd.lang.xpath.ast.ASTAbbrevForwardStep;
import net.sourceforge.pmd.lang.xpath.ast.ASTAbbrevReverseStep;
import net.sourceforge.pmd.lang.xpath.ast.ASTAdditiveExpr;
import net.sourceforge.pmd.lang.xpath.ast.ASTAndExpr;
import net.sourceforge.pmd.lang.xpath.ast.ASTAnyFunctionTest;
import net.sourceforge.pmd.lang.xpath.ast.ASTAnyItemType;
import net.sourceforge.pmd.lang.xpath.ast.ASTAnyKindTest;
import net.sourceforge.pmd.lang.xpath.ast.ASTArgument;
import net.sourceforge.pmd.lang.xpath.ast.ASTArgumentList;
import net.sourceforge.pmd.lang.xpath.ast.ASTArgumentTypeList;
import net.sourceforge.pmd.lang.xpath.ast.ASTAtomicOrUnionType;
import net.sourceforge.pmd.lang.xpath.ast.ASTAttributeDeclaration;
import net.sourceforge.pmd.lang.xpath.ast.ASTAttributeNameOrWildCard;
import net.sourceforge.pmd.lang.xpath.ast.ASTAttributeTest;
import net.sourceforge.pmd.lang.xpath.ast.ASTAxisStep;
import net.sourceforge.pmd.lang.xpath.ast.ASTCastExpr;
import net.sourceforge.pmd.lang.xpath.ast.ASTCastableExpr;
import net.sourceforge.pmd.lang.xpath.ast.ASTChildStep;
import net.sourceforge.pmd.lang.xpath.ast.ASTCommentTest;
import net.sourceforge.pmd.lang.xpath.ast.ASTComparisonExpr;
import net.sourceforge.pmd.lang.xpath.ast.ASTContextItemExpr;
import net.sourceforge.pmd.lang.xpath.ast.ASTDescendantStep;
import net.sourceforge.pmd.lang.xpath.ast.ASTDivOperator;
import net.sourceforge.pmd.lang.xpath.ast.ASTDocumentTest;
import net.sourceforge.pmd.lang.xpath.ast.ASTElementDeclaration;
import net.sourceforge.pmd.lang.xpath.ast.ASTElementNameOrWildcard;
import net.sourceforge.pmd.lang.xpath.ast.ASTElementTest;
import net.sourceforge.pmd.lang.xpath.ast.ASTEmptySequenceType;
import net.sourceforge.pmd.lang.xpath.ast.ASTExceptOperator;
import net.sourceforge.pmd.lang.xpath.ast.ASTExpr;
import net.sourceforge.pmd.lang.xpath.ast.ASTForExpr;
import net.sourceforge.pmd.lang.xpath.ast.ASTForwardAxis;
import net.sourceforge.pmd.lang.xpath.ast.ASTForwardStep;
import net.sourceforge.pmd.lang.xpath.ast.ASTFunctionBody;
import net.sourceforge.pmd.lang.xpath.ast.ASTFunctionCall;
import net.sourceforge.pmd.lang.xpath.ast.ASTFunctionItemExpr;
import net.sourceforge.pmd.lang.xpath.ast.ASTFunctionTest;
import net.sourceforge.pmd.lang.xpath.ast.ASTIDivOperator;
import net.sourceforge.pmd.lang.xpath.ast.ASTIfExpr;
import net.sourceforge.pmd.lang.xpath.ast.ASTInlineFunctionExpr;
import net.sourceforge.pmd.lang.xpath.ast.ASTInstanceofExpr;
import net.sourceforge.pmd.lang.xpath.ast.ASTIntersectExceptExpr;
import net.sourceforge.pmd.lang.xpath.ast.ASTIntersectOperator;
import net.sourceforge.pmd.lang.xpath.ast.ASTItemType;
import net.sourceforge.pmd.lang.xpath.ast.ASTKindTest;
import net.sourceforge.pmd.lang.xpath.ast.ASTLetExpr;
import net.sourceforge.pmd.lang.xpath.ast.ASTMapExpr;
import net.sourceforge.pmd.lang.xpath.ast.ASTMinusOperator;
import net.sourceforge.pmd.lang.xpath.ast.ASTModOperator;
import net.sourceforge.pmd.lang.xpath.ast.ASTMultiplicativeExpr;
import net.sourceforge.pmd.lang.xpath.ast.ASTName;
import net.sourceforge.pmd.lang.xpath.ast.ASTNameTest;
import net.sourceforge.pmd.lang.xpath.ast.ASTNamedFunctionRef;
import net.sourceforge.pmd.lang.xpath.ast.ASTNamespaceNodeTest;
import net.sourceforge.pmd.lang.xpath.ast.ASTNodeTest;
import net.sourceforge.pmd.lang.xpath.ast.ASTNumericLiteral;
import net.sourceforge.pmd.lang.xpath.ast.ASTOrExpr;
import net.sourceforge.pmd.lang.xpath.ast.ASTParam;
import net.sourceforge.pmd.lang.xpath.ast.ASTParamList;
import net.sourceforge.pmd.lang.xpath.ast.ASTParenthesizedExpr;
import net.sourceforge.pmd.lang.xpath.ast.ASTParenthesizedItemType;
import net.sourceforge.pmd.lang.xpath.ast.ASTPathExpr;
import net.sourceforge.pmd.lang.xpath.ast.ASTPlusOperator;
import net.sourceforge.pmd.lang.xpath.ast.ASTPostfixExpr;
import net.sourceforge.pmd.lang.xpath.ast.ASTPredicate;
import net.sourceforge.pmd.lang.xpath.ast.ASTPredicateList;
import net.sourceforge.pmd.lang.xpath.ast.ASTPrimaryExpr;
import net.sourceforge.pmd.lang.xpath.ast.ASTProcessingInstructionTest;
import net.sourceforge.pmd.lang.xpath.ast.ASTQuantifiedExpr;
import net.sourceforge.pmd.lang.xpath.ast.ASTRangeExpr;
import net.sourceforge.pmd.lang.xpath.ast.ASTReverseAxis;
import net.sourceforge.pmd.lang.xpath.ast.ASTReverseStep;
import net.sourceforge.pmd.lang.xpath.ast.ASTSchemaAttributeTest;
import net.sourceforge.pmd.lang.xpath.ast.ASTSchemaElementTest;
import net.sourceforge.pmd.lang.xpath.ast.ASTSequenceType;
import net.sourceforge.pmd.lang.xpath.ast.ASTSingleType;
import net.sourceforge.pmd.lang.xpath.ast.ASTStepExpr;
import net.sourceforge.pmd.lang.xpath.ast.ASTStringConcatExpr;
import net.sourceforge.pmd.lang.xpath.ast.ASTStringLiteral;
import net.sourceforge.pmd.lang.xpath.ast.ASTTextTest;
import net.sourceforge.pmd.lang.xpath.ast.ASTTimesOperator;
import net.sourceforge.pmd.lang.xpath.ast.ASTTreatExpr;
import net.sourceforge.pmd.lang.xpath.ast.ASTTypeDeclaration;
import net.sourceforge.pmd.lang.xpath.ast.ASTTypedFunctionTest;
import net.sourceforge.pmd.lang.xpath.ast.ASTUnaryExpr;
import net.sourceforge.pmd.lang.xpath.ast.ASTUnionExpr;
import net.sourceforge.pmd.lang.xpath.ast.ASTUnionOperator;
import net.sourceforge.pmd.lang.xpath.ast.ASTUnionShortHandOperator;
import net.sourceforge.pmd.lang.xpath.ast.ASTVarBinding;
import net.sourceforge.pmd.lang.xpath.ast.ASTVarBindingList;
import net.sourceforge.pmd.lang.xpath.ast.ASTVarRef;
import net.sourceforge.pmd.lang.xpath.ast.ASTWildcard;
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


    @Override
    public RuleContext visit(XPathNode node, RuleContext data) {
        return node.childrenAccept(this, data);
    }


    @Override
    public RuleContext visit(ASTXPathRoot node, RuleContext data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public RuleContext visit(ASTExpr node, RuleContext data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public RuleContext visit(ASTForExpr node, RuleContext data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public RuleContext visit(ASTLetExpr node, RuleContext data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public RuleContext visit(ASTQuantifiedExpr node, RuleContext data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public RuleContext visit(ASTVarBindingList node, RuleContext data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public RuleContext visit(ASTVarBinding node, RuleContext data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public RuleContext visit(ASTIfExpr node, RuleContext data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public RuleContext visit(ASTOrExpr node, RuleContext data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public RuleContext visit(ASTAndExpr node, RuleContext data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public RuleContext visit(ASTComparisonExpr node, RuleContext data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public RuleContext visit(ASTStringConcatExpr node, RuleContext data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public RuleContext visit(ASTRangeExpr node, RuleContext data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public RuleContext visit(ASTAdditiveExpr node, RuleContext data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public RuleContext visit(ASTPlusOperator node, RuleContext data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public RuleContext visit(ASTMinusOperator node, RuleContext data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public RuleContext visit(ASTMultiplicativeExpr node, RuleContext data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public RuleContext visit(ASTTimesOperator node, RuleContext data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public RuleContext visit(ASTDivOperator node, RuleContext data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public RuleContext visit(ASTIDivOperator node, RuleContext data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public RuleContext visit(ASTModOperator node, RuleContext data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public RuleContext visit(ASTUnionExpr node, RuleContext data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public RuleContext visit(ASTUnionOperator node, RuleContext data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public RuleContext visit(ASTUnionShortHandOperator node, RuleContext data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public RuleContext visit(ASTIntersectExceptExpr node, RuleContext data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public RuleContext visit(ASTIntersectOperator node, RuleContext data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public RuleContext visit(ASTExceptOperator node, RuleContext data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public RuleContext visit(ASTInstanceofExpr node, RuleContext data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public RuleContext visit(ASTTreatExpr node, RuleContext data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public RuleContext visit(ASTCastableExpr node, RuleContext data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public RuleContext visit(ASTCastExpr node, RuleContext data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public RuleContext visit(ASTUnaryExpr node, RuleContext data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public RuleContext visit(ASTMapExpr node, RuleContext data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public RuleContext visit(ASTPathExpr node, RuleContext data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public RuleContext visit(ASTChildStep node, RuleContext data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public RuleContext visit(ASTDescendantStep node, RuleContext data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public RuleContext visit(ASTStepExpr node, RuleContext data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public RuleContext visit(ASTAxisStep node, RuleContext data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public RuleContext visit(ASTForwardStep node, RuleContext data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public RuleContext visit(ASTForwardAxis node, RuleContext data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public RuleContext visit(ASTAbbrevForwardStep node, RuleContext data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public RuleContext visit(ASTReverseStep node, RuleContext data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public RuleContext visit(ASTReverseAxis node, RuleContext data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public RuleContext visit(ASTAbbrevReverseStep node, RuleContext data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public RuleContext visit(ASTNodeTest node, RuleContext data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public RuleContext visit(ASTNameTest node, RuleContext data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public RuleContext visit(ASTWildcard node, RuleContext data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public RuleContext visit(ASTPostfixExpr node, RuleContext data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public RuleContext visit(ASTArgumentList node, RuleContext data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public RuleContext visit(ASTArgument node, RuleContext data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public RuleContext visit(ASTPredicateList node, RuleContext data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public RuleContext visit(ASTPredicate node, RuleContext data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public RuleContext visit(ASTPrimaryExpr node, RuleContext data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public RuleContext visit(ASTNumericLiteral node, RuleContext data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public RuleContext visit(ASTStringLiteral node, RuleContext data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public RuleContext visit(ASTVarRef node, RuleContext data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public RuleContext visit(ASTParenthesizedExpr node, RuleContext data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public RuleContext visit(ASTContextItemExpr node, RuleContext data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public RuleContext visit(ASTFunctionCall node, RuleContext data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public RuleContext visit(ASTFunctionItemExpr node, RuleContext data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public RuleContext visit(ASTNamedFunctionRef node, RuleContext data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public RuleContext visit(ASTInlineFunctionExpr node, RuleContext data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public RuleContext visit(ASTFunctionBody node, RuleContext data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public RuleContext visit(ASTParamList node, RuleContext data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public RuleContext visit(ASTParam node, RuleContext data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public RuleContext visit(ASTKindTest node, RuleContext data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public RuleContext visit(ASTCommentTest node, RuleContext data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public RuleContext visit(ASTTextTest node, RuleContext data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public RuleContext visit(ASTNamespaceNodeTest node, RuleContext data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public RuleContext visit(ASTAnyKindTest node, RuleContext data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public RuleContext visit(ASTDocumentTest node, RuleContext data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public RuleContext visit(ASTElementTest node, RuleContext data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public RuleContext visit(ASTElementNameOrWildcard node, RuleContext data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public RuleContext visit(ASTAttributeTest node, RuleContext data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public RuleContext visit(ASTAttributeNameOrWildCard node, RuleContext data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public RuleContext visit(ASTSchemaAttributeTest node, RuleContext data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public RuleContext visit(ASTAttributeDeclaration node, RuleContext data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public RuleContext visit(ASTProcessingInstructionTest node, RuleContext data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public RuleContext visit(ASTSchemaElementTest node, RuleContext data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public RuleContext visit(ASTElementDeclaration node, RuleContext data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public RuleContext visit(ASTSequenceType node, RuleContext data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public RuleContext visit(ASTEmptySequenceType node, RuleContext data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public RuleContext visit(ASTItemType node, RuleContext data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public RuleContext visit(ASTAnyItemType node, RuleContext data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public RuleContext visit(ASTAtomicOrUnionType node, RuleContext data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public RuleContext visit(ASTParenthesizedItemType node, RuleContext data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public RuleContext visit(ASTFunctionTest node, RuleContext data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public RuleContext visit(ASTAnyFunctionTest node, RuleContext data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public RuleContext visit(ASTTypedFunctionTest node, RuleContext data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public RuleContext visit(ASTArgumentTypeList node, RuleContext data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public RuleContext visit(ASTSingleType node, RuleContext data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public RuleContext visit(ASTTypeDeclaration node, RuleContext data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public RuleContext visit(ASTName node, RuleContext data) {
        return visit((XPathNode) node, data);
    }


}
