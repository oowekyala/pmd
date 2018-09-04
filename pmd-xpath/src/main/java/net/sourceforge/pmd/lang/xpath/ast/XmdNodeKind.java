/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

/**
 * Node kinds defined by the XQuery and XPath data model.
 *
 * @author Clément Fournier
 * @since 6.7.0
 */
public enum XmdNodeKind {
    DOCUMENT,
    ELEMENT,
    ATTRIBUTE,
    NAMESPACE,
    PROCESSING_INSTRUCTION,
    COMMENT,
    TEXT
}
