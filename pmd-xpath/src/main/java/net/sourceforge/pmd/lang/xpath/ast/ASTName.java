/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;


import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.checkerframework.checker.nullness.qual.Nullable;

import net.sourceforge.pmd.lang.ast.ParseException;

// @formatter:off
/**
 * The name of an entity. This node represents all names,
 * regardless of the production they matched. It matches the
 * "EQName" production of the XPath language spec.
 *
 * <p>Examples:
 * <ul>
 *     <li> {@code pi} is a lexical QName without a namespace prefix.
 *     <li> {@code math:pi} is a lexical QName with a namespace prefix.
 *     <li> {@code Q{http://www.w3.org/2005/xpath-functions/math}pi} specifies
 *          the namespace URI using a BracedURILiteral; it is not a lexical QName.
 * </ul>
 *
 */
// @formatter:on
public final class ASTName extends AbstractXPathNode {

    private String namespace = null;
    private String localName = null;
    private boolean isUriLiteral = false;


    /**
     * Constructor for synthetic node.
     *
     * @param image Image of the name, may match any of the syntax above
     */
    public ASTName(String image) {
        super(XPathParserImplTreeConstants.JJTNAME);
        setImage(image);
    }

    ASTName() {
        super(XPathParserImplTreeConstants.JJTNAME);
    }


    @Override
    public void setImage(String image) {
        super.setImage(image);
        initUri();
    }


    private void initUri() {
        String image = getImage();
        if (image == null) {
            throw new IllegalStateException("Name has no image?");
        }

        Matcher bracedUriLiteral = Pattern.compile("Q\\{([^}]*)}(.*)").matcher(image);

        if (bracedUriLiteral.matches()) {
            isUriLiteral = true;
            namespace = bracedUriLiteral.group(1);
            localName = bracedUriLiteral.group(2);

            try {
                new URI(namespace);
            } catch (URISyntaxException e) {
                throw new ParseException("Malformed URI in braced URI literal (" + image + ")");
            }

        } else {
            // either unqualified or qualified

            String[] parts = image.split(":");
            if (parts.length == 2) {
                namespace = parts[0];
                localName = parts[1];
            } else {
                // 1
                namespace = null;
                localName = parts[0];
            }
        }

        if (localName == null || localName.isEmpty()) {
            // this catches the case in which "parts" above has length 0 or above 2
            throw new IllegalStateException("Malformed name or procedure doesn't work");
        }


    }


    /**
     * Returns the namespace prefix if it is explicitly specified,
     * e.g. with the syntax "prefix:local". If this is {@linkplain #isUriLiteral() a URI literal},
     * then returns the full URI.
     */
    @Nullable
    public String getExplicitNamespacePrefix() {
        return namespace;
    }


    /**
     * Returns the local part of the name.
     * Never null, never empty.
     */
    public String getLocalName() {
        return localName;
    }


    /**
     * Returns true if this name used the URI
     * literal syntax to specify a namespace prefix,
     * {@literal e.g. Q{http://www.w3.org/2005/xpath-functions/math}pi}.
     */
    public boolean isUriLiteral() {
        return isUriLiteral;
    }


    /**
     * Returns this name as it appears in the source.
     */
    @Override
    public String getImage() { // NOPMD
        return super.getImage();
    }


    @Override
    public <T> void jjtAccept(XPathSideEffectingVisitor<T> visitor, T data) {
        visitor.visit(this, data);
    }


    @Override
    public <R, T> R jjtAccept(XPathVisitor<R, T> visitor, T data) {
        return visitor.visit(this, data);
    }
}
