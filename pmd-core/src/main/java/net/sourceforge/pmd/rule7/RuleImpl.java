/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.rule7;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Set;

import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.annotation.Experimental;
import net.sourceforge.pmd.lang.ast.AstProcessingStage;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.ast.RootNode;
import net.sourceforge.pmd.properties.PropertyDescriptor;

/**
 * The implementation of a rule. Rule impls may be created but not run.
 */
public interface RuleImpl {

    /**
     * Initializes this instance using the given descriptor. The
     * descriptor contains the full property mapping.
     */
    default void init(RuleDescriptor descriptor) {
        // do nothing
    }


    /** Perform some initialization or cleanup before processing a file. */
    default void beforeFile() {
        // do nothing
    }


    /** Perform some initialization or cleanup after processing a file. */
    default void afterFile() {
        // do nothing
    }


    /** Process a node which has been opted-in by {@link #appliesOn(Node)}). */
    void visit(Node node, RuleContext ctx);


    /**
     * Returns true if this rule expects {@link #visit(Node, RuleContext)}
     * to be called on the given node. Standard, full-tree visit rules
     * will return true on just {@link RootNode}s. Rulechain rules may
     * return true on any node in the tree.
     *
     * @param node Node to test
     */
    default boolean appliesOn(Node node) {
        return node instanceof RootNode;
    }


    @Experimental
    default boolean dependsOn(AstProcessingStage<?> stage) {
        return false;
    }


    /**
     * Return a new impl instance. Copies are made to process files in
     * several threads.
     */
    default RuleImpl deepCopy(RuleDescriptor descriptor) {
        try {
            RuleImpl rule = this.getClass().getDeclaredConstructor().newInstance();
            rule.init(descriptor);
            return rule;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("Cannot copy " + this, e);
        }
    }


    interface JvmRuleImpl extends RuleImpl {

        /** This type of impl may declare properties directly in code (ie not in XML). */
        default Set<PropertyDescriptor<?>> getDeclaredProperties() {
            return Collections.emptySet();
        }
    }
}
