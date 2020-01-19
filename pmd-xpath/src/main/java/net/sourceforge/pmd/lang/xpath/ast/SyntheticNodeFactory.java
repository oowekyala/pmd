/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;


import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


/**
 * Synthesises nodes.
 *
 * @author Clément Fournier
 * @since 6.7.0
 */
public final class SyntheticNodeFactory {


    private SyntheticNodeFactory() {

    }


    /**
     * Converts the given java value to an AST node if possible and returns it.
     *
     * @param value Value to convert
     *
     * @return An AST node
     *
     * @throws IllegalArgumentException If the value type is not supported
     */
    public static Expr getNodeForValue(Object value) {
        if (value == null) {
            return new ASTEmptySequenceExpr();
        } else if (value instanceof String || value instanceof Character) {
            return new ASTStringLiteral(String.valueOf(value));
        } else if (value instanceof Boolean) {
            return SyntheticNodeFactory.synthesizeBooleanLiteral((Boolean) value);
        } else if (value instanceof Number) {
            return new ASTNumericLiteral(value.toString());
        } else if (value instanceof Pattern) {
            return new ASTStringLiteral(((Pattern) value).pattern());
        } else if (value instanceof List) {
            return convertListToSequence((List<?>) value);
        } else {
            // We could maybe use UntypedAtomicValue
            throw new IllegalArgumentException("Unable to create ValueRepresentation for value of type: " + value.getClass());
        }


    }


    /**
     * Returns a synthesized node representing a boolean value.
     * These are function calls.
     *
     * @param value Boolean value to represent
     */
    private static PrimaryExpr synthesizeBooleanLiteral(boolean value) {
        return new ASTFunctionCall(new ASTName(String.valueOf(value)), new ASTArgumentList());
    }


    public static Expr convertListToSequence(List<?> value) {
        if (value.isEmpty()) {
            return new ASTEmptySequenceExpr();
        } else if (value.size() == 1) {
            return getNodeForValue(value.get(0));
        } else {
            List<Expr> elts = value.stream().map(SyntheticNodeFactory::getNodeForValue).collect(Collectors.toList());
            ASTSequenceExpr seq = new ASTSequenceExpr(elts);
            seq.bumpParenDepth();
            return seq;
        }
    }
}
