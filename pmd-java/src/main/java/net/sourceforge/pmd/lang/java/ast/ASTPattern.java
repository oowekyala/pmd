/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.ast;

import net.sourceforge.pmd.annotation.Experimental;

/**
 * A pattern (for pattern matching constructs like {@link ASTInstanceOfExpression InstanceOfExpression}
 * or within a {@link ASTSwitchLabel}). This is a JDK 16 feature.
 *
 * <p>This interface will be implemented by all forms of patterns. For
 * now, only type test patterns are supported. Record deconstruction
 * patterns is planned for a future JDK version.
 *
 * <pre class="grammar">
 *
 * Pattern ::=   {@link ASTTypePattern TypePattern}
 *             | {@link ASTGuardedPattern GuardedPattern}
 *
 * </pre>
 * 
 * @see <a href="https://openjdk.java.net/jeps/394">JEP 394: Pattern Matching for instanceof</a>
 */
public interface ASTPattern extends JavaNode {

    /**
     * Returns the number of parenthesis levels around this pattern.
     * If this method returns 0, then no parentheses are present.
     */
    @Experimental
    int getParenthesisDepth();
}
