/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;


import net.sourceforge.pmd.lang.xpath.ast.NodeTest.NameTest;


/**
 * A wildcard in an {@link ASTAxisStep}.
 *
 * <pre>
 *
 * (: Note that this node has no children. :)
 * WildcardNameTest ::= "*"
 *                    | "*" : &lt;NCName&gt;
 *                    | &lt;NCName&gt; : "*"
 *                    | &lt;BRACED_URI_LITERAL&gt; "*"
 *
 * </pre>
 */
public final class ASTWildcardNameTest extends AbstractXPathNode implements NameTest {

    /** Constructor for synthetic node. */
    ASTWildcardNameTest() {
        super(null, XPathParserTreeConstants.JJTWILDCARDNAMETEST);
    }

    // they don't have the same name as the getters because kotlin gets
    // confused with properties since they're package private
    private String expectedLocalName = null;
    private String expectedNamespacePrefix = null;
    private String expectedUri = null;


    ASTWildcardNameTest(XPathParser p, int id) {
        super(p, id);
    }


    /**
     * Returns true if this is the "*" wildcard, which doesn't constraint
     * either the namespace or local name of the node.
     */
    public boolean isFullWildcard() {
        return expectedLocalName == null && expectedNamespacePrefix == null && expectedUri == null;
    }


    /**
     * Returns the expected local name, when the namespace is
     * not constrained. This returns e.g. "local" in "*:local".
     * This returns null when the local name is not constrained,
     * e.g. in "fn:*" or "*".
     */
    public String getExpectedLocalName() {
        return expectedLocalName;
    }


    /**
     * Returns the expected namespace URI, when a namespace constraint
     * was mentioned with a URI literal., when the local name is
     * not constrained. This returns e.g. "fn" in "fn:*".
     * This returns null when the namespace is not constrained,
     * e.g. in "*:pi" or "*".
     */
    public String getExpectedNamespaceUri() {
        return expectedUri;
    }


    /**
     * Returns the expected namespace prefix, when the local name is
     * not constrained. This returns e.g. "fn" in "fn:*".
     * This returns null when the namespace is not constrained,
     * e.g. in "*:pi" or "*".
     */
    public String getExpectedNamespacePrefix() {
        return expectedNamespacePrefix;
    }


    @Override
    public <T> void jjtAccept(SideEffectingVisitor<T> visitor, T data) {
        visitor.visit(this, data);
    }


    @Override
    public void jjtAccept(ParameterlessSideEffectingVisitor visitor) {
        visitor.visit(this);
    }


    @Override
    public <T> T jjtAccept(XPathGenericVisitor<T> visitor, T data) {
        return visitor.visit(this, data);
    }


    void setRightWildcard(String substring) {
        expectedNamespacePrefix = substring;
    }


    void setRightUriWildcard(String substring) {
        expectedUri = substring;
    }


    void setLeftWildcard(String substring) {
        expectedLocalName = substring;
    }
}
/* JavaCC - OriginalChecksum=9cc10133cc5ea5705f090e1cc01ecf11 (do not edit this line) */
