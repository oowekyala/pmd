/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

/**
 * Root interface for all expressions. Groups {@link Expr}
 * and {@link ASTSequenceExpr SequenceExpr} together.
 */
public interface Expr extends XPathNode {

    int getParenDepth();
}
