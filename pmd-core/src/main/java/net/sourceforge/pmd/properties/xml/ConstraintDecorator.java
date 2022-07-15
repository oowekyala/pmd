/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.properties.xml;

import java.util.List;
import java.util.Set;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.w3c.dom.Element;

import net.sourceforge.pmd.properties.constraints.PropertyConstraint;
import net.sourceforge.pmd.util.CollectionUtil;
import net.sourceforge.pmd.util.internal.xml.PmdXmlReporter;
import net.sourceforge.pmd.util.internal.xml.XmlErrorMessages;

/**
 * Decorates an XmlMapper with some {@link PropertyConstraint}s.
 * Those are checked when the value is parsed. This is used to
 * report errors on the most specific failing element.
 *
 * <p>Note that this is the only XmlMapper that *applies* constraints
 * in {@link #fromXml(Element, PmdXmlReporter)}. A {@link SeqSyntax}
 * or {@link OptionalSyntax} may return some constraints in {@link #getConstraints()}
 * that are derived from the constraints of the item, yet not check them
 * on elements (they will be applied on each element by the {@link XmlMapper}
 * they wrap).
 */
class ConstraintDecorator<T> extends XmlMapper<T> {


    private final XmlMapper<T> xmlMapper;
    private final List<PropertyConstraint<? super T>> constraints;

    ConstraintDecorator(XmlMapper<T> mapper, List<PropertyConstraint<? super T>> constraints) {
        this.xmlMapper = mapper;
        this.constraints = constraints;
    }

    @Override
    public T fromXml(Element element, PmdXmlReporter err) {
        T t = xmlMapper.fromXml(element, err);

        XmlSyntaxUtils.checkConstraintsThrow(
            t,
            constraints,
            s -> err.at(element).error(XmlErrorMessages.ERR__CONSTRAINT_NOT_SATISFIED, s)
        );

        return t;
    }

    @Override
    public List<PropertyConstraint<? super T>> getConstraints() {
        return constraints;
    }

    @Override
    public XmlMapper<T> withConstraint(PropertyConstraint<? super T> t) {
        return new ConstraintDecorator<>(this.xmlMapper, CollectionUtil.plus(this.constraints, t));
    }

    @Override
    public void toXml(Element container, T value) {
        xmlMapper.toXml(container, value);
    }


    @Override
    public String getWriteElementName(T value) {
        return xmlMapper.getWriteElementName(value);
    }


    @Override
    public Set<String> getReadElementNames() {
        return xmlMapper.getReadElementNames();
    }


    @Override
    protected List<String> examplesImpl(String curIndent, String baseIndent) {
        return xmlMapper.examplesImpl(curIndent, baseIndent);
    }

    @Override
    public boolean supportsStringMapping() {
        return xmlMapper.supportsStringMapping();
    }

    @Override
    public T fromString(@NonNull String attributeData) {
        return xmlMapper.fromString(attributeData);
    }

    @Override
    public @NonNull String toString(T value) {
        return xmlMapper.toString(value);
    }

    @Override
    public String toString() {
        return xmlMapper.toString();
    }

}
