/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

/**
 * @author Clément Fournier
 * @since 6.7.0
 */
public class XPathParserVisitorAdapter<T> implements XPathParserVisitor<T> {
    @Override
    public T visit(XPathNode node, T data) {
        return node.childrenAccept(this, data);
    }


    @Override
    public T visit(ASTXPathRoot node, T data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public T visit(ASTExpr node, T data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public T visit(ASTForExpr node, T data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public T visit(ASTLetExpr node, T data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public T visit(ASTQuantifiedExpr node, T data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public T visit(ASTVarBindingList node, T data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public T visit(ASTVarBinding node, T data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public T visit(ASTIfExpr node, T data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public T visit(ASTOrExpr node, T data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public T visit(ASTAndExpr node, T data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public T visit(ASTComparisonExpr node, T data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public T visit(ASTStringConcatExpr node, T data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public T visit(ASTRangeExpr node, T data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public T visit(ASTAdditiveExpr node, T data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public T visit(ASTPlusOperator node, T data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public T visit(ASTMinusOperator node, T data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public T visit(ASTMultiplicativeExpr node, T data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public T visit(ASTTimesOperator node, T data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public T visit(ASTDivOperator node, T data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public T visit(ASTIDivOperator node, T data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public T visit(ASTModOperator node, T data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public T visit(ASTUnionExpr node, T data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public T visit(ASTUnionOperator node, T data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public T visit(ASTUnionShortHandOperator node, T data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public T visit(ASTIntersectExceptExpr node, T data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public T visit(ASTIntersectOperator node, T data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public T visit(ASTExceptOperator node, T data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public T visit(ASTInstanceofExpr node, T data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public T visit(ASTTreatExpr node, T data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public T visit(ASTCastableExpr node, T data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public T visit(ASTCastExpr node, T data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public T visit(ASTUnaryExpr node, T data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public T visit(ASTMapExpr node, T data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public T visit(ASTPathExpr node, T data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public T visit(ASTChildStep node, T data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public T visit(ASTDescendantStep node, T data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public T visit(ASTStepExpr node, T data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public T visit(ASTAxisStep node, T data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public T visit(ASTNodeTest node, T data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public T visit(ASTNameTest node, T data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public T visit(ASTWildcard node, T data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public T visit(ASTPostfixExpr node, T data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public T visit(ASTArgumentList node, T data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public T visit(ASTArgument node, T data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public T visit(ASTPredicateList node, T data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public T visit(ASTPredicate node, T data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public T visit(ASTPrimaryExpr node, T data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public T visit(ASTNumericLiteral node, T data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public T visit(ASTStringLiteral node, T data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public T visit(ASTVarRef node, T data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public T visit(ASTParenthesizedExpr node, T data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public T visit(ASTContextItemExpr node, T data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public T visit(ASTFunctionCall node, T data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public T visit(ASTFunctionItemExpr node, T data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public T visit(ASTNamedFunctionRef node, T data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public T visit(ASTInlineFunctionExpr node, T data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public T visit(ASTFunctionBody node, T data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public T visit(ASTParamList node, T data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public T visit(ASTParam node, T data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public T visit(ASTKindTest node, T data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public T visit(ASTCommentTest node, T data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public T visit(ASTTextTest node, T data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public T visit(ASTNamespaceNodeTest node, T data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public T visit(ASTAnyKindTest node, T data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public T visit(ASTDocumentTest node, T data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public T visit(ASTElementTest node, T data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public T visit(ASTElementNameOrWildcard node, T data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public T visit(ASTAttributeTest node, T data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public T visit(ASTAttributeNameOrWildCard node, T data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public T visit(ASTSchemaAttributeTest node, T data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public T visit(ASTAttributeDeclaration node, T data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public T visit(ASTProcessingInstructionTest node, T data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public T visit(ASTSchemaElementTest node, T data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public T visit(ASTElementDeclaration node, T data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public T visit(ASTSequenceType node, T data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public T visit(ASTEmptySequenceType node, T data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public T visit(ASTItemType node, T data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public T visit(ASTAnyItemType node, T data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public T visit(ASTAtomicOrUnionType node, T data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public T visit(ASTParenthesizedItemType node, T data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public T visit(ASTFunctionTest node, T data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public T visit(ASTAnyFunctionTest node, T data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public T visit(ASTTypedFunctionTest node, T data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public T visit(ASTArgumentTypeList node, T data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public T visit(ASTSingleType node, T data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public T visit(ASTTypeDeclaration node, T data) {
        return visit((XPathNode) node, data);
    }


    @Override
    public T visit(ASTName node, T data) {
        return visit((XPathNode) node, data);
    }
}
