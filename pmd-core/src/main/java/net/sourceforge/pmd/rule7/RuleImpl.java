/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.rule7;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Set;

import net.sourceforge.pmd.annotation.Experimental;
import net.sourceforge.pmd.lang.ast.AstProcessingStage;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.ast.RootNode;
import net.sourceforge.pmd.lang.rule.internal.TargetSelectionStrategy;
import net.sourceforge.pmd.properties.PropertyDescriptor;

/**
 * The implementation of a rule. Rule impls may be created without being run.
 */
public interface RuleImpl {

    /**
     * Initializes this instance using the given descriptor. The
     * descriptor contains the full property mapping.
     */
    default void init(RuleDescriptor descriptor) {
        // do nothing
    }


    /** Process a node which has been opted-in by the {@link #getTargetingStrategy()}. */
    void apply(Node node, ScopedRuleContext ctx);


    /**
     * Returns an object which decides which nodes are passed to the
     * {@link #apply(Node, ScopedRuleContext) apply} method. For rules
     * which want to perform a full tree traversal, the strategy would
     * select {@link RootNode}s. For rules which want to be called only
     * on some nodes, for performance, the strategy may select other
     * nodes based on the index.
     */
    TargetSelectionStrategy getTargetingStrategy();


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
