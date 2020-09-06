/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd;

import java.util.Collections;
import java.util.List;

import net.sourceforge.pmd.lang.Language;
import net.sourceforge.pmd.lang.ast.AstVisitor;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.rule.RuleTargetSelector;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertySource;


public interface RuleBehavior {

    String languageId();

    default RuleTargetSelector getTargetSelector() {
        return RuleTargetSelector.forRootOnly();
    }

    default List<? extends PropertyDescriptor<?>> declaredProperties() {
        return Collections.emptyList();
    }

    FileAnalyser initialize(PropertySource properties, Language language);

    interface FileAnalyser {

        void apply(Node node, RuleContext ctx);

        default FileAnalyser nextFile() {
            return this;
        }
    }


    class VisitorAnalyser implements FileAnalyser {

        private final AstVisitor<RuleContext, Void> visitor;

        protected VisitorAnalyser(AstVisitor<RuleContext, Void> visitor) {
            this.visitor = visitor;
        }

        @Override
        public final void apply(Node node, RuleContext ctx) {
            node.acceptVisitor(visitor, ctx);
        }
    }
}
