/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.properties.xml;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.w3c.dom.Element;

import net.sourceforge.pmd.properties.constraints.PropertyConstraint;
import net.sourceforge.pmd.properties.xml.XmlMapper.StableXmlMapper;
import net.sourceforge.pmd.util.internal.xml.PmdXmlReporter;
import net.sourceforge.pmd.util.internal.xml.XmlErrorMessages;
import net.sourceforge.pmd.util.internal.xml.XmlUtil;


/**
 * Serialize to and from a simple string. Examples:
 *
 * <pre>{@code
 *  <seq>1</seq>
 * }</pre>
 */
final class SeqSyntax<T, C extends Iterable<T>> extends StableXmlMapper<C> {

    private final XmlMapper<T> itemSyntax;
    private final Collector<? super T, ?, ? extends C> collector;

    SeqSyntax(XmlMapper<T> itemSyntax, Collector<? super T, ?, ? extends C> collector) {
        super("seq");
        this.itemSyntax = itemSyntax;
        this.collector = collector;
    }

    @Override
    public void toXml(Element container, C value) {
        for (T v : value) {
            Element item = container.getOwnerDocument().createElement(itemSyntax.getWriteElementName(v));
            itemSyntax.toXml(item, v);
            container.appendChild(item);
        }
    }

    @Override
    public C fromXml(Element element, PmdXmlReporter err) {
        RuntimeException aggregateEx = err.at(element).error(XmlErrorMessages.ERR__LIST_CONSTRAINT_NOT_SATISFIED);

        C result = XmlUtil.getElementChildren(element)
                          .map(child -> {
                              try {
                                  return XmlUtil.expectElement(err, child, itemSyntax);
                              } catch (Exception e) {
                                  aggregateEx.addSuppressed(e);
                                  return null;
                              }
                          })
                          .filter(Objects::nonNull)
                          .collect(collector);

        if (aggregateEx.getSuppressed().length > 0) {
            throw aggregateEx;
        } else {
            return result;
        }
    }

    @Override
    public List<PropertyConstraint<? super C>> getConstraints() {
        return itemSyntax.getConstraints().stream()
                         .map(PropertyConstraint::toCollectionConstraint)
                         .collect(Collectors.toList());
    }

    @Override
    protected List<String> examplesImpl(String curIndent, String baseIndent) {
        String newIndent = curIndent + baseIndent;
        return Collections.singletonList(
            curIndent + "<seq>\n"
                + newIndent + String.join("\n", itemSyntax.examplesImpl(newIndent, baseIndent)) + "\n"
                + newIndent + "..."
                + curIndent + "</seq>"
        );
    }
}
