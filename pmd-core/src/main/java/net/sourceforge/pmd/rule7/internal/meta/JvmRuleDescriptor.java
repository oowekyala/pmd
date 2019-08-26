/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.rule7.internal.meta;


import net.sourceforge.pmd.rule7.RuleImpl;
import net.sourceforge.pmd.rule7.RuleImpl.JvmRuleImpl;

/**
 * Descriptor for a rule
 */
public class JvmRuleDescriptor extends BaseRuleDescriptor {

    private final JvmRuleImpl impl;

    protected JvmRuleDescriptor(JavaRuleBuilder builder, JvmRuleImpl impl) {
        super(builder);
        this.impl = impl;
    }

    @Override
    public RuleImpl getVisitor() {
        return impl.deepCopy();
    }
}
