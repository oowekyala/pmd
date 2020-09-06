/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import net.sourceforge.pmd.lang.Language;
import net.sourceforge.pmd.lang.ast.AstVisitor;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.rule.RuleTargetSelector;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertySource;


public interface RuleBehavior {

    String languageId();

    RuleTargetSelector getTargetSelector();

    default List<? extends PropertyDescriptor<?>> declaredProperties() {
        return Collections.emptyList();
    }

    default void initialize(PropertySource properties, Language language) {

    }

    FileAnalyser startFile();

    interface FileAnalyser {

        void apply(Node node, RuleContext ctx);

        void close();
    }

    abstract class VisitorRuleBehavior implements RuleBehavior {

        private AstVisitor<RuleContext, Void> myVisitor;

        @Override
        public RuleTargetSelector getTargetSelector() {
            return RuleTargetSelector.forRootOnly();
        }

        protected abstract AstVisitor<RuleContext, Void> buildVisitor(PropertySource properties, Language language);

        @Override
        public final void initialize(PropertySource properties, Language language) {
            myVisitor = buildVisitor(properties, language);
        }

        @Override
        public VisitorAnalyser startFile() {
            return new VisitorAnalyser(Objects.requireNonNull(myVisitor, "Missing initialization"));
        }

        protected static class VisitorAnalyser implements FileAnalyser {

            private final AstVisitor<RuleContext, Void> visitor;

            protected VisitorAnalyser(AstVisitor<RuleContext, Void> visitor) {
                this.visitor = visitor;
            }

            @Override
            public final void apply(Node node, RuleContext ctx) {
                node.acceptVisitor(visitor, ctx);
            }

            @Override
            public void close() {
                // do nothing
            }
        }
    }

}
