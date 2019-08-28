/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.codestyle;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import net.sourceforge.pmd.lang.java.ast.ASTAnnotation;
import net.sourceforge.pmd.lang.java.ast.ASTMemberValuePair;
import net.sourceforge.pmd.lang.java.rule.AbstractJRulechainRule;
import net.sourceforge.pmd.rule7.ScopedRuleContext;

/**
 * @author Kirk Clemens
 * @since 6.2.0
 */
public class UnnecessaryAnnotationValueElementRule extends AbstractJRulechainRule {

    public UnnecessaryAnnotationValueElementRule() {
    }

    @Override
    public Set<Class<ASTAnnotation>> getRulechainVisits() {
        return Collections.singleton(ASTAnnotation.class);
    }

    @Override
    public void visit(ASTAnnotation node, ScopedRuleContext data) {

        final List<ASTMemberValuePair> annotationProperties = node.findDescendantsOfType(ASTMemberValuePair.class);
        // all that needs to be done is check to if there's a single property in the annotation and if if that property is value
        // then it's a violation and it should be resolved.
        if (annotationProperties.size() == 1 && "value".equals(annotationProperties.get(0).getImage())) {
            data.addViolation(node);
        }
    }
}
