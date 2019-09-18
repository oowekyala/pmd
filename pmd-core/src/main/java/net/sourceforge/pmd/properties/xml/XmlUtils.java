/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.properties.xml;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class XmlUtils {

    static final String UNEXPECTED_ELEMENT = "Unexpected element '%s', expecting %s";
    static final String MISSING_REQUIRED_ATTRIBUTE = "Required attribute '%s' is missing";
    static final String MISSING_REQUIRED_ATTRIBUTE = "Required attribute '%s' is missing";

    private XmlUtils() {

    }

    public static List<Node> toList(NodeList lst) {
        ArrayList<Node> nodes = new ArrayList<>();
        for (int i = 0; i < lst.getLength(); i++) {
            nodes.add(lst.item(i));
        }
        return nodes;
    }

    static Stream<Element> getElementChildren(Element parent) {
        return toList(parent.getChildNodes()).stream()
                                             .filter(it -> it.getNodeType() == Node.ELEMENT_NODE)
                                             .map(Element.class::cast);
    }

    public static <T> T expectElement(XmlErrorReporter err, Element elt, XmlSyntax<T> syntax) {

        if (!syntax.getReadElementNames().contains(elt.getTagName())) {
            err.warn(elt, "Wrong name, expect " + XmlSyntaxUtils.formatPossibilities(syntax.getReadElementNames()));
        } else {
            return syntax.fromXml(elt, err);
        }

        return null;
    }
}