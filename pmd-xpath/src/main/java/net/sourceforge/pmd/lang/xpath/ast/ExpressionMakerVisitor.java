/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

import java.util.Iterator;


/**
 * Dumps a subtree to a parsable expression. The AST of the dumped string is
 * equivalent to the original one.
 *
 * <p>The corresponding public API is provided by {@link AbstractXPathNode#toExpressionString()},
 * this class is private though.
 *
 * @author Clément Fournier
 * @since 6.7.0
 */
final class ExpressionMakerVisitor implements SideEffectingVisitor<StringBuilder> {

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
    public void visit(ASTEmptySequenceExpr node, StringBuilder builder) {
        appendToken(builder, "()");
    }


    @Override
    public void visit(ASTForExpr node, StringBuilder builder) {
        appendToken(builder, "for ");
        joinExprsOnBuilder(builder, node.getBindings(), ", ");
        appendToken(builder, "return ");
        visit(node.getBodyExpr(), builder);
    }


    @Override
    public void visit(ASTLetExpr node, StringBuilder builder) {
        appendToken(builder, "let ");
        joinExprsOnBuilder(builder, node.getBindings(), ", ");
        appendToken(builder, " return ");
        visit(node.getBodyExpr(), builder);
    }


    @Override
    public void visit(ASTQuantifiedExpr node, StringBuilder builder) {
        appendToken(builder, node.isExistentiallyQuantified() ? "some " : "every ");
        joinExprsOnBuilder(builder, node.getBindings(), ", ");
        appendToken(builder, " satisfies ");
        visit(node.getBodyExpr(), builder);
    }


    @Override
    public void visit(ASTVarBinding node, StringBuilder builder) {
        appendToken(builder, "$");
        visit(node.getVarNameNode(), builder);
        appendToken(builder, node.isLetStyle() ? " := " : " in ");
        visit(node.getInitializerExpr(), builder);
    }


    @Override
    public void visit(ASTIfExpr node, StringBuilder builder) {
        appendToken(builder, "if (");
        visit(node.getGuardExpressionNode(), builder);
        appendToken(builder, ") then ");
        visit(node.getTrueAlternative(), builder);
        appendToken(builder, " else ");
        visit(node.getFalseAlternative(), builder);
    }


    @Override
    public void visit(ASTOrExpr node, StringBuilder builder) {
        joinExprsOnBuilder(builder, childrenOf(node), " or ");
    }


    @Override
    public void visit(ASTAndExpr node, StringBuilder builder) {
        joinExprsOnBuilder(builder, childrenOf(node), " and ");
    }


    @Override
    public void visit(ASTComparisonExpr node, StringBuilder builder) {
        visit(node.getLhs(), builder);
        appendToken(builder, node.getOperatorImage());
        visit(node.getRhs(), builder);
    }


    @Override
    public void visit(ASTStringConcatExpr node, StringBuilder builder) {
        joinExprsOnBuilder(builder, childrenOf(node), " || ");
    }


    @Override
    public void visit(ASTRangeExpr node, StringBuilder builder) {
        visit(node.getLowerBound(), builder);
        appendToken(builder, " to ");
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
        appendToken(builder, " instance of ");
        visit(node.getTestedType(), builder);
    }


    @Override
    public void visit(ASTTreatExpr node, StringBuilder builder) {
        visit(node.getCastedExpr(), builder);
        appendToken(builder, " treat as ");
        visit(node.getCastedType(), builder);
    }


    @Override
    public void visit(ASTCastableExpr node, StringBuilder builder) {
        visit(node.getTestedExpr(), builder);
        appendToken(builder, " castable as ");
        visit(node.getTestedType(), builder);
    }


    @Override
    public void visit(ASTCastExpr node, StringBuilder builder) {
        visit(node.getCastedExpr(), builder);
        appendToken(builder, " cast as ");
        visit(node.getCastedType(), builder);
    }


    @Override
    public void visit(ASTUnaryExpr node, StringBuilder builder) {
        appendToken(builder, node.getOperator());
        visit(node.getOperand(), builder);
    }


    @Override
    public void visit(ASTMapExpr node, StringBuilder builder) {
        joinExprsOnBuilder(builder, node.getOperands(), " ! ");
    }


    @Override
    public void visit(ASTPathExpr node, StringBuilder builder) {
        appendToken(builder, node.getPathAnchor().getPrefix());

        Iterator<StepExpr> steps = node.iterator();
        StepExpr prev = steps.next();

        visit(prev, builder);

        while (steps.hasNext()) {
            StepExpr step = steps.next();
            if (!prev.isAbbrevDescendantOrSelf() && !step.isAbbrevDescendantOrSelf()) {
                appendToken(builder, "/");
            } // else step adds "//"
            prev = step;
            visit(step, builder);
        }
    }


    @Override
    public void visit(ASTAxisStep node, StringBuilder builder) {
        if (node.isAbbrevDescendantOrSelf()) {
            appendToken(builder, "//");
        } else if (node.isAbbrevAttributeAxis()) {
            appendToken(builder, "@");
            visit(node.getNodeTest(), builder);
        } else if (node.isAbbrevParentNodeTest()) {
            appendToken(builder, "..");
        } else if (!node.isAbbrevNoAxis()) {
            appendToken(builder, node.getAxis().getAxisName());
            appendToken(builder, "::");
            visit(node.getNodeTest(), builder);
        } else if (node.isAbbrevNoAxis()) {
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
            appendToken(builder, "*");
        } else if (node.getExpectedLocalName() != null) {
            appendToken(builder, "*:");
            appendToken(builder, node.getExpectedLocalName());
        } else if (node.getExpectedNamespaceUri() != null) {
            appendToken(builder, "Q{");
            appendToken(builder, node.getExpectedNamespaceUri());
            appendToken(builder, "}*");
        } else if (node.getExpectedNamespacePrefix() != null) {
            appendToken(builder, node.getExpectedNamespacePrefix());
            appendToken(builder, ":*");
        }
    }


    @Override
    public void visit(ASTPostfixExpr node, StringBuilder builder) {
        justAppendChildren(builder, node);
    }


    @Override
    public void visit(ASTArgumentList node, StringBuilder builder) {
        appendToken(builder, "(");
        joinExprsOnBuilder(builder, node, ", ");
        appendToken(builder, ")");
    }


    @Override
    public void visit(ASTArgument node, StringBuilder builder) {
        if (node.isPlaceholder()) {
            appendToken(builder, "?");
        } else {
            visit(node.getExpression(), builder);
        }
    }


    @Override
    public void visit(ASTPredicate node, StringBuilder builder) {
        appendToken(builder, "[");
        visit(node.getWrappedExpression(), builder);
        appendToken(builder, "]");
    }


    @Override
    public void visit(ASTStringLiteral node, StringBuilder builder) {
        appendToken(builder, node.getImage());
    }


    @Override
    public void visit(ASTVarRef node, StringBuilder builder) {
        appendToken(builder, "$");
        visit(node.getVarNameNode(), builder);
    }


    @Override
    public void visit(ASTParenthesizedExpr node, StringBuilder builder) {
        appendToken(builder, "(");
        visit(node.getWrappedNode(), builder);
        appendToken(builder, ")");
    }


    @Override
    public void visit(ASTContextItemExpr node, StringBuilder builder) {
        appendToken(builder, ".");
    }


    @Override
    public void visit(ASTNumericLiteral node, StringBuilder builder) {
        appendToken(builder, node.getImage());
    }


    @Override
    public void visit(ASTFunctionCall node, StringBuilder builder) {
        visit(node.getFunctionName(), builder);
        visit(node.getArguments(), builder);
    }


    @Override
    public void visit(ASTNamedFunctionRef node, StringBuilder builder) {
        visit(node.getFunctionName(), builder);
        appendToken(builder, "#" + node.getArity());
    }


    @Override
    public void visit(ASTInlineFunctionExpr node, StringBuilder builder) {
        appendToken(builder, "function");
        visit(node.getParamList(), builder);
        if (!node.isDefaultReturnType()) {
            appendToken(builder, " as ");
            visit(node.getDeclaredReturnType(), builder);
        }
        appendToken(builder, "{");
        visit(node.getBodyExpr(), builder);
        appendToken(builder, "}");
    }


    @Override
    public void visit(ASTParamList node, StringBuilder builder) {
        appendToken(builder, "(");
        joinExprsOnBuilder(builder, node, ", ");
        appendToken(builder, ")");
    }


    @Override
    public void visit(ASTParam node, StringBuilder builder) {
        appendToken(builder, "$");
        visit(node.getNameNode(), builder);
        if (!node.isDefaultType()) {
            appendToken(builder, " as ");
            visit(node.getDeclaredType(), builder);
        }
    }


    @Override
    public void visit(ASTCommentTest node, StringBuilder builder) {
        appendToken(builder, "comment()");
    }


    @Override
    public void visit(ASTTextTest node, StringBuilder builder) {
        appendToken(builder, "text()");
    }


    @Override
    public void visit(ASTNamespaceNodeTest node, StringBuilder builder) {
        appendToken(builder, "namespace-node()");
    }


    @Override
    public void visit(ASTAnyKindTest node, StringBuilder builder) {
        appendToken(builder, "node()");
    }


    @Override
    public void visit(ASTDocumentTest node, StringBuilder builder) {
        appendToken(builder, "document(");
        if (node.getArgumentTest() != null) {
            visit(node.getArgumentTest(), builder);
        }
        appendToken(builder, ")");
    }


    @Override
    public void visit(ASTElementTest node, StringBuilder builder) {
        appendToken(builder, "element(");
        if (!node.isEmptyParen()) {
            if (node.getElementName() != null) {
                visit(node.getElementName(), builder);
            } else {
                appendToken(builder, "*");
            }

            if (node.getTypeName() != null) {
                appendToken(builder, ",");
                visit(node.getTypeName(), builder);
                if (node.isOptionalType()) {
                    appendToken(builder, "?");
                }
            }
        }
        appendToken(builder, ")");
    }


    @Override
    public void visit(ASTAttributeTest node, StringBuilder builder) {
        appendToken(builder, "element(");
        if (!node.isEmptyParen()) {
            if (node.getAttributeName() != null) {
                visit(node.getAttributeName(), builder);
            } else {
                appendToken(builder, "*");
            }

            if (node.getTypeName() != null) {
                appendToken(builder, ",");
                visit(node.getTypeName(), builder);
            }
        }
        appendToken(builder, ")");
    }


    @Override
    public void visit(ASTSchemaAttributeTest node, StringBuilder builder) {
        appendToken(builder, "schema-attribute(");
        visit(node.getAttributeNameNode(), builder);
        appendToken(builder, ")");
    }


    @Override
    public void visit(ASTProcessingInstructionTest node, StringBuilder builder) {
        appendToken(builder, "processing-instruction(");
        if (node.hasArgument()) {
            visit((XPathNode) node.jjtGetChild(0), builder);
        }
        appendToken(builder, ")");
    }


    @Override
    public void visit(ASTSchemaElementTest node, StringBuilder builder) {
        appendToken(builder, "schema-element(");
        visit(node.getLastChild(), builder);
        appendToken(builder, ")");
    }


    @Override
    public void visit(ASTSequenceType node, StringBuilder builder) {
        if (node.isEmptySequence()) {
            appendToken(builder, "empty-sequence()");
        } else {
            visit(node.getItemType(), builder);
            appendToken(builder, node.getCardinality().getOccurrenceIndicator());
        }
    }


    @Override
    public void visit(ASTAnyItemType node, StringBuilder builder) {
        appendToken(builder, "item()");
    }


    @Override
    public void visit(ASTAtomicOrUnionType node, StringBuilder builder) {
        justAppendChildren(builder, node);
    }


    @Override
    public void visit(ASTParenthesizedItemType node, StringBuilder builder) {
        appendToken(builder, "(");
        visit(node.getWrappedNode(), builder);
        appendToken(builder, ")");
    }


    @Override
    public void visit(ASTAnyFunctionTest node, StringBuilder builder) {
        appendToken(builder, "function(*)");
    }


    @Override
    public void visit(ASTTypedFunctionTest node, StringBuilder builder) {
        appendToken(builder, "function");
        visit(node.getParamTypeList(), builder);
        appendToken(builder, "as");
        visit(node.getDeclaredReturnType(), builder);
    }


    @Override
    public void visit(ASTArgumentTypeList node, StringBuilder builder) {
        appendToken(builder, "(");
        joinExprsOnBuilder(builder, node, ", ");
        appendToken(builder, ")");
    }


    @Override
    public void visit(ASTSingleType node, StringBuilder builder) {
        visit(node.getTypeNameNode(), builder);
        if (node.isOptional()) {
            appendToken(builder, "?");
        }
    }


    @Override
    public void visit(ASTName node, StringBuilder builder) {
        appendToken(builder, node.getImage());
    }

    // Utilities


    private void justAppendChildren(StringBuilder builder, XPathNode node) {
        joinExprsOnBuilder(builder, childrenOf(node), "");
    }


    private static void appendToken(StringBuilder builder, String token) {
        if (token.isEmpty()) {
            return;
        }
        if (builder.length() > 0 && isDelimitingChar(builder.charAt(builder.length() - 1))
                || isDelimitingChar(token.charAt(0))) {
            // No need to insert a delimiting space
            builder.append(token);
        } else {
            builder.append(' ').append(token); // TODO
        }
    }


    private static boolean isDelimitingChar(char c) {
        switch (c) {
        case '!':
        case '\"':
        case '#':
        case '$':
        case '(':
        case ')':
        case '*':
        case '+':
        case ',':
        case '-':
        case '.':
        case '/':
        case ':':
        case '<':
        case '=':
        case '>':
        case '?':
        case '@':
        case '[':
        case ']':
        case '{':
        case '|':
        case '}':
        case ' ':
        case '\n':
        case '\f':
        case '\r':
            return true;
        default:
            return false;
        }
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
