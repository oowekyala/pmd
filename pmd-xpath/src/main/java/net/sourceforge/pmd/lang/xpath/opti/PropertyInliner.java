/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.opti;

import java.util.Map;

import net.sourceforge.pmd.lang.xpath.ast.ASTVarRef;
import net.sourceforge.pmd.lang.xpath.ast.ASTXPathRoot;
import net.sourceforge.pmd.properties.PropertyDescriptor;


/**
 * @author Clément Fournier
 * @since 6.7.0
 */
class PropertyInliner {

    private final Map<PropertyDescriptor<?>, Object> valuesByDescriptor;
    private final ASTXPathRoot root;


    PropertyInliner(XPathQueryImpl impl) {
        this.valuesByDescriptor = impl.getPropertyValues();
        this.root = impl.getRoot();
    }


    void inlineProperties(XPathQueryImpl impl) {
        Map<PropertyDescriptor<?>, Object> propertyValues = impl.getPropertyValues();
        ASTXPathRoot root = impl.getRoot();

        for (ASTVarRef ref : root.getFreeVarRefs()) {



        }
    }


    private void inlineProperties() {

    }


}
