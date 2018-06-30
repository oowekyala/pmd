/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.codestyle;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.java.ast.ASTAnnotationTypeDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTAnyTypeDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTEnumDeclaration;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaMetricsRule;
import net.sourceforge.pmd.lang.java.typeresolution.TypeHelper;
import net.sourceforge.pmd.properties.BooleanProperty;
import net.sourceforge.pmd.properties.BooleanProperty.BooleanPBuilder;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.RegexProperty;
import net.sourceforge.pmd.properties.StringMultiProperty;
import net.sourceforge.pmd.util.StringUtil;


/**
 * Template rule allowing to define a naming convention for a class hiararchy.
 */
public class ClassHierarchyNamingConventionsRule extends AbstractJavaMetricsRule {

    private static final PropertyDescriptor<Pattern> CONVENTION_REGEX
            = RegexProperty.named("regexConvention")
                           .desc("The regex to test on matched classes")
                           .isRequired()
                           .build();

    private static final PropertyDescriptor<List<String>> SUPERTYPES_DESCRIPTOR
            = StringMultiProperty.named("superTypes")
                                 .desc("List of supertypes to intersect to get a match")
                                 .isRequired()
                                 .build();

    private static final PropertyDescriptor<Boolean> MATCH_CONCRETE_CLASS = matchProp("concrete classes").defaultValue(true).build();
    private static final PropertyDescriptor<Boolean> MATCH_INTERFACES = matchProp("interfaces").build();
    private static final PropertyDescriptor<Boolean> MATCH_ABSTRACT_CLASS = matchProp("abstract classes").build();
    private static final PropertyDescriptor<Boolean> MATCH_ENUM = matchProp("enums").build();
    private static final PropertyDescriptor<Boolean> MATCH_ANNOTATIONS = matchProp("annotations").build();


    public ClassHierarchyNamingConventionsRule() {
        definePropertyDescriptor(CONVENTION_REGEX);
        definePropertyDescriptor(SUPERTYPES_DESCRIPTOR);
        definePropertyDescriptor(MATCH_ABSTRACT_CLASS);
        definePropertyDescriptor(MATCH_CONCRETE_CLASS);
        definePropertyDescriptor(MATCH_INTERFACES);
        definePropertyDescriptor(MATCH_ENUM);
        definePropertyDescriptor(MATCH_ANNOTATIONS);
    }


    @Override
    public void start(RuleContext ctx) {
        if (getProperty(MATCH_ENUM)) {
            addRuleChainVisit(ASTEnumDeclaration.class);
        }

        if (getProperty(MATCH_CONCRETE_CLASS) || getProperty(MATCH_ABSTRACT_CLASS) || getProperty(MATCH_INTERFACES)) {
            addRuleChainVisit(ASTClassOrInterfaceDeclaration.class);
        }

        if (getProperty(MATCH_ANNOTATIONS)) {
            addRuleChainVisit(ASTAnnotationTypeDeclaration.class);
        }

    }


    private boolean matchesSuperTypes(ASTAnyTypeDeclaration declaration) {

        for (String superType : getProperty(SUPERTYPES_DESCRIPTOR)) {
            if (!TypeHelper.isA(declaration, superType)) {
                return false;
            }
        }

        return true;
    }


    @Override
    public Object visit(ASTAnyTypeDeclaration node, Object data) {
        if (matchesSuperTypes(node)) {
            if (!getProperty(CONVENTION_REGEX).matcher(node.getImage()).matches()) {
                addViolation(data, node);
            }
        }

        return data;
    }


    private static BooleanPBuilder matchProp(String nodeKind) {
        return BooleanProperty.named("match" + StringUtil.toPascalCase(nodeKind))
                              .desc("Whether the convention applies to " + nodeKind.toLowerCase(Locale.ROOT))
                              .defaultValue(false);
    }
}
