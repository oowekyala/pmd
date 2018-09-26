/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.opti;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;

import net.sourceforge.pmd.lang.xpath.ast.ASTVarRef;
import net.sourceforge.pmd.lang.xpath.ast.ASTXPathRoot;
import net.sourceforge.pmd.lang.xpath.ast.SyntheticNodeFactory;
import net.sourceforge.pmd.properties.PropertyDescriptor;


/**
 * @author Clément Fournier
 * @since 6.7.0
 */
class XPathQueryImpl implements XPathQuery {

    private final ASTXPathRoot root;
    private final Map<PropertyDescriptor<?>, Object> propertyValues;


    XPathQueryImpl(ASTXPathRoot root, Map<PropertyDescriptor<?>, Object> propertyValues) {
        this.root = Objects.requireNonNull(root);
        this.propertyValues = Objects.requireNonNull(propertyValues);
    }


    public ASTXPathRoot getRoot() {
        return root;
    }


    @Override
    public void inlineProperties() {
        Map<String, Object> propNamesToValues = new HashMap<>();
        propertyValues.forEach((p, o) -> propNamesToValues.put(p.name(), o));

        // make new hashset to avoid concurrent modification exception
        for (ASTVarRef ref : new HashSet<>(root.getFreeVarRefs())) {
            Object value = propNamesToValues.get(ref.getVarName());
            ref.replaceWith(SyntheticNodeFactory.getNodeForValue(value));
        }
    }


    @Override
    public String toParsableString() {
        return root.toExpressionString();
    }


    @Override
    public Map<NodeIdentifier, XPathQuery> getRulechainQueries() {
        return null;
    }


    public Map<PropertyDescriptor<?>, Object> getPropertyValues() {
        return propertyValues;
    }
}
