/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.rule7;

import java.util.function.Supplier;
import java.util.logging.Level;

import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.ast.Node;

/**
 * Replaces the {@link RuleContext} of PMD 6.0. A scoped rule context
 * is bound to a rule descriptor.
 */
public interface ScopedRuleContext {

    RuleDescriptor getDescriptor();


    void addViolation(Node node);


    void addViolation(Node node, String message);


    void addViolationWith(Node node, Object... args);


    void reportError(Throwable ex);


    void logMeta(Level level, Supplier<String> message);

}
