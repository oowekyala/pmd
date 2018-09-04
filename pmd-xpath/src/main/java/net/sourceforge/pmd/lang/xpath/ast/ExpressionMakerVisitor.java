/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

import java.util.Iterator;

import net.sourceforge.pmd.lang.ast.Node;


/**
 * Dumps a subtree to a parsable expression. The AST of the dumped string is
 * equivalent to the original one.
 *
 * @author Clément Fournier
 * @since 6.7.0
 */
public final class ExpressionMakerVisitor implements SideEffectingVisitor<StringBuilder> {

    /**
     * Joins some nodes on the builder with a delimiter.
     */
    private void joinExprsOnBuilder(StringBuilder builder, Iterable<? extends XPathNode> nodes, String delimiter) {
        Iterator<? extends XPathNode> iterator = nodes.iterator();
        if (iterator.hasNext()) {
            visit(iterator.next(), builder);
        } else {
            return;
        }

        while (iterator.hasNext()) {
            appendToken(builder, delimiter);
            visit(iterator.next(), builder);
        }
    }


    @Override
    public void visit(XPathNode node, StringBuilder builder) {
        node.jjtAccept(this, builder);
    }


    @Override
    public void visit(ASTXPathRoot node, StringBuilder builder) {
        visit(node.getMainExpr(), builder);
    }


    @Override
    public void visit(ASTSequenceExpr node, StringBuilder builder) {
        joinExprsOnBuilder(builder, node, ", ");
    }


    @Override
    public void visit(ASTForExpr node, StringBuilder builder) {
        appendToken(builder, "for");
        visit(node.getBindings(), builder);
        appendToken(builder, "return");
        visit(node.getReturnExpr(), builder);
    }


    @Override
    public void visit(ASTLetExpr node, StringBuilder builder) {
        appendToken(builder, "let");
        visit(node.getBindings(), builder);
        appendToken(builder, "return");
        visit(node.getReturnExpr(), builder);
    }


    @Override
    public void visit(ASTQuantifiedExpr node, StringBuilder builder) {
        appendToken(builder, node.isExistentiallyQuantified() ? "some" : "every");
        visit(node.getBindings(), builder);
        appendToken(builder, "satisfies");
        visit(node.getTestedExpr(), builder);
    }


    @Override
    public void visit(ASTVarBindingList node, StringBuilder builder) {
        joinExprsOnBuilder(builder, node, ", ");
    }


    @Override
    public void visit(ASTVarBinding node, StringBuilder builder) {
        appendToken(builder, "$");
        visit(node.getNameNode(), builder);
        appendToken(builder, node.isLetStyle() ? ":=" : "in");
        visit(node.getInitializerExpr(), builder);
    }


    @Override
    public void visit(ASTIfExpr node, StringBuilder builder) {
        appendToken(builder, "if (");
        visit(node.getGuardExpressionNode(), builder);
        appendToken(builder, ") then");
        visit(node.getTrueAlternative(), builder);
        appendToken(builder, "else");
        visit(node.getFalseAlternative(), builder);
    }


    @Override
    public void visit(ASTOrExpr node, StringBuilder builder) {
        joinExprsOnBuilder(builder, childrenOf(node), "or");
    }


    @Override
    public void visit(ASTAndExpr node, StringBuilder builder) {
        joinExprsOnBuilder(builder, childrenOf(node), "and");
    }


    @Override
    public void visit(ASTComparisonExpr node, StringBuilder builder) {
        visit(node.getLhs(), builder);
        appendToken(builder, node.getOperator());
        visit(node.getRhs(), builder);
    }


    @Override
    public void visit(ASTStringConcatExpr node, StringBuilder builder) {
        joinExprsOnBuilder(builder, childrenOf(node), "||");
    }


    @Override
    public void visit(ASTRangeExpr node, StringBuilder builder) {
        visit(node.getLowerBound(), builder);
        appendToken(builder, "to");
        visit(node.getUpperBound(), builder);
    }


    @Override
    public void visit(ASTAdditiveExpr node, StringBuilder builder) {
        joinExprsOnBuilder(builder, childrenOf(node), "");
    }


    @Override
    public void visit(ASTAdditiveOperator node, StringBuilder builder) {
        appendToken(builder, node.getImage());
    }


    @Override
    public void visit(ASTMultiplicativeExpr node, StringBuilder builder) {
        justAppendChildren(builder, node);
    }


    @Override
    public void visit(ASTMultiplicativeOperator node, StringBuilder builder) {
        appendToken(builder, node.getImage());
    }


    @Override
    public void visit(ASTUnionExpr node, StringBuilder builder) {
        justAppendChildren(builder, node);
    }


    @Override
    public void visit(ASTUnionOperator node, StringBuilder builder) {
        appendToken(builder, node.getImage());
    }


    @Override
    public void visit(ASTIntersectExceptExpr node, StringBuilder builder) {
        justAppendChildren(builder, node);
    }


    @Override
    public void visit(ASTIntersectExceptOperator node, StringBuilder builder) {
        appendToken(builder, node.getImage());
    }


    @Override
    public void visit(ASTInstanceofExpr node, StringBuilder builder) {
        visit(node.getTestedExpr(), builder);
        builder.append(" instance of ");
        visit(node.getTestedType(), builder);
    }


    @Override
    public void visit(ASTTreatExpr node, StringBuilder builder) {
        visit(node.getCastedExpr(), builder);
        builder.append(" treat as ");
        visit(node.getCastedType(), builder);
    }


    @Override
    public void visit(ASTCastableExpr node, StringBuilder builder) {
        visit(node.getTestedExpr(), builder);
        builder.append(" castable as ");
        visit(node.getTestedType(), builder);
    }


    @Override
    public void visit(ASTCastExpr node, StringBuilder builder) {
        visit(node.getCastedExpr(), builder);
        builder.append(" cast as ");
        visit(node.getCastedType(), builder);
    }


    @Override
    public void visit(ASTUnaryExpr node, StringBuilder builder) {
        builder.append(node.getOperator());
        visit(node.getOperand(), builder);
    }


    @Override
    public void visit(ASTMapExpr node, StringBuilder builder) {
        joinExprsOnBuilder(builder, node.getOperands(), " ! ");
    }


    @Override
    public void visit(ASTPathExpr node, StringBuilder builder) {
        builder.append(node.getPathAnchor().getPrefix());

        Iterator<StepExpr> steps = node.iterator();
        visit(steps.next(), builder);

        while (steps.hasNext()) {
            StepExpr step = steps.next();
            if (!step.isAbbrevDescendantOrSelf()) {
                builder.append("/");
            } // else step adds "//"
            visit(step, builder);
        }
    }


    @Override
    public void visit(ASTAxisStep node, StringBuilder builder) {
        if (node.isAbbrevDescendantOrSelf()) {
            builder.append("//");
        } else if (node.isAbbrevAttributeAxis()) {
            builder.append("@");
            visit(node.getNodeTest(), builder);
        } else if (node.isAbbrevParentNodeTest()) {
            builder.append("..");
        } else if (!node.isAbbrevNoAxis()) {
            builder.append(node.getAxis().getAxisName()).append("::");
            visit(node.getNodeTest(), builder);
        }

        for (ASTPredicate predicate : node.getPredicates()) {
            visit(predicate, builder);
        }
    }


    @Override
    public void visit(ASTExactNameTest node, StringBuilder builder) {
        visit(node.getNameNode(), builder);
    }


    @Override
    public void visit(ASTWildcardNameTest node, StringBuilder builder) {
        if (node.isFullWildcard()) {
            builder.append("*");
        } else if (node.getExpectedLocalName() != null) {
            builder.append("*:").append(node.getExpectedLocalName());
        } else if (node.getExpectedNamespaceUri() != null) {
            builder.append("Q{").append(node.getExpectedNamespaceUri()).append("}*");
        } else if (node.getExpectedNamespacePrefix() != null) {
            builder.append(node.getExpectedNamespacePrefix()).append(":*");
        }
    }


    @Override
    public void visit(ASTPostfixExpr node, StringBuilder builder) {
        for (Node child : node.getChildren()) {
            visit((XPathNode) child, builder);
        }
    }


    @Override
    public void visit(ASTArgumentList node, StringBuilder builder) {
        builder.append("(");
        joinExprsOnBuilder(builder, node, ", ");
        builder.append(")");
    }


    @Override
    public void visit(ASTArgument node, StringBuilder builder) {
        if (node.isPlaceholder()) {
            builder.append("?");
        } else {
            visit(node.getExpression(), builder);
        }
    }


    @Override
    public void visit(ASTPredicate node, StringBuilder builder) {
        builder.append("[");
        visit(node.getWrappedExpression(), builder);
        builder.append("]");
    }


    @Override
    public void visit(ASTStringLiteral node, StringBuilder builder) {
        builder.append(node.getImage());
    }


    @Override
    public void visit(ASTVarRef node, StringBuilder builder) {
        builder.append("$");
        visit(node.getVariableName(), builder);
    }


    @Override
    public void visit(ASTParenthesizedExpr node, StringBuilder builder) {
        builder.append("(");
        visit(node.getWrappedNode(), builder);
        builder.append(")");
    }


    @Override
    public void visit(ASTContextItemExpr node, StringBuilder builder) {
        builder.append(".");
    }


    @Override
    public void visit(ASTNumericLiteral node, StringBuilder builder) {
        builder.append(node.getImage());
    }


    @Override
    public void visit(ASTFunctionCall node, StringBuilder builder) {
        visit(node.getFunctionName(), builder);
        visit(node.getArguments(), builder);
    }


    @Override
    public void visit(ASTNamedFunctionRef node, StringBuilder builder) {
        visit(node.getFunctionName(), builder);
        builder.append("#").append(node.getArity());
    }


    @Override
    public void visit(ASTInlineFunctionExpr node, StringBuilder builder) {
        throw new UnsupportedOperationException("FIXME!!!!!!!!!!!!!!!");
    }


    @Override
    public void visit(ASTParamList node, StringBuilder builder) {
        throw new UnsupportedOperationException("FIXME!!!!!!!!!!!!!!!");
    }


    @Override
    public void visit(ASTParam node, StringBuilder builder) {
        throw new UnsupportedOperationException("FIXME!!!!!!!!!!!!!!!");
    }


    @Override
    public void visit(ASTCommentTest node, StringBuilder builder) {
        throw new UnsupportedOperationException("FIXME!!!!!!!!!!!!!!!");
    }


    @Override
    public void visit(ASTTextTest node, StringBuilder builder) {
        throw new UnsupportedOperationException("FIXME!!!!!!!!!!!!!!!");
    }


    @Override
    public void visit(ASTNamespaceNodeTest node, StringBuilder builder) {
        throw new UnsupportedOperationException("FIXME!!!!!!!!!!!!!!!");
    }


    @Override
    public void visit(ASTAnyKindTest node, StringBuilder builder) {
        throw new UnsupportedOperationException("FIXME!!!!!!!!!!!!!!!");
    }


    @Override
    public void visit(ASTDocumentTest node, StringBuilder builder) {
        throw new UnsupportedOperationException("FIXME!!!!!!!!!!!!!!!");
    }


    @Override
    public void visit(ASTElementTest node, StringBuilder builder) {
        throw new UnsupportedOperationException("FIXME!!!!!!!!!!!!!!!");
    }


    @Override
    public void visit(ASTElementNameOrWildcard node, StringBuilder builder) {
        throw new UnsupportedOperationException("FIXME!!!!!!!!!!!!!!!");
    }


    @Override
    public void visit(ASTAttributeTest node, StringBuilder builder) {
        throw new UnsupportedOperationException("FIXME!!!!!!!!!!!!!!!");
    }


    @Override
    public void visit(ASTAttributeNameOrWildCard node, StringBuilder builder) {
        throw new UnsupportedOperationException("FIXME!!!!!!!!!!!!!!!");
    }


    @Override
    public void visit(ASTSchemaAttributeTest node, StringBuilder builder) {
        throw new UnsupportedOperationException("FIXME!!!!!!!!!!!!!!!");
    }


    @Override
    public void visit(ASTAttributeDeclaration node, StringBuilder builder) {
        throw new UnsupportedOperationException("FIXME!!!!!!!!!!!!!!!");
    }


    @Override
    public void visit(ASTProcessingInstructionTest node, StringBuilder builder) {
        throw new UnsupportedOperationException("FIXME!!!!!!!!!!!!!!!");
    }


    @Override
    public void visit(ASTSchemaElementTest node, StringBuilder builder) {
        throw new UnsupportedOperationException("FIXME!!!!!!!!!!!!!!!");
    }


    @Override
    public void visit(ASTElementDeclaration node, StringBuilder builder) {
        throw new UnsupportedOperationException("FIXME!!!!!!!!!!!!!!!");
    }


    @Override
    public void visit(ASTSequenceType node, StringBuilder builder) {
        throw new UnsupportedOperationException("FIXME!!!!!!!!!!!!!!!");
    }


    @Override
    public void visit(ASTAnyItemType node, StringBuilder builder) {
        throw new UnsupportedOperationException("FIXME!!!!!!!!!!!!!!!");
    }


    @Override
    public void visit(ASTAtomicOrUnionType node, StringBuilder builder) {
        throw new UnsupportedOperationException("FIXME!!!!!!!!!!!!!!!");
    }


    @Override
    public void visit(ASTParenthesizedItemType node, StringBuilder builder) {
        throw new UnsupportedOperationException("FIXME!!!!!!!!!!!!!!!");
    }


    @Override
    public void visit(ASTAnyFunctionTest node, StringBuilder builder) {
        throw new UnsupportedOperationException("FIXME!!!!!!!!!!!!!!!");
    }


    @Override
    public void visit(ASTTypedFunctionTest node, StringBuilder builder) {
        throw new UnsupportedOperationException("FIXME!!!!!!!!!!!!!!!");
    }


    @Override
    public void visit(ASTArgumentTypeList node, StringBuilder builder) {
        throw new UnsupportedOperationException("FIXME!!!!!!!!!!!!!!!");
    }


    @Override
    public void visit(ASTSingleType node, StringBuilder builder) {
        throw new UnsupportedOperationException("FIXME!!!!!!!!!!!!!!!");
    }


    @Override
    public void visit(ASTName node, StringBuilder builder) {
        builder.append(node.getImage());
    }


    private void justAppendChildren(StringBuilder builder, XPathNode node) {
        joinExprsOnBuilder(builder, childrenOf(node), "");
    }


    private static void appendToken(StringBuilder builder, String token) {
        builder.append(" ").append(token).append(" "); // TODO
    }


    private static Iterable<XPathNode> childrenOf(XPathNode node) {
        return () -> new ChildrenIterator(node);
    }


    private static class ChildrenIterator implements Iterator<XPathNode> {
        private final XPathNode parent;
        private int current;


        ChildrenIterator(XPathNode parent) {
            this.parent = parent;
        }


        @Override
        public boolean hasNext() {
            return parent.jjtGetNumChildren() > current;
        }


        @Override
        public XPathNode next() {
            return (XPathNode) parent.jjtGetChild(current++);
        }
    }
}
