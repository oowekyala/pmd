/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;


import static net.sourceforge.pmd.lang.xpath.ast.ASTWildcardNameTest.WildcardType.ANY;
import static net.sourceforge.pmd.lang.xpath.ast.ASTWildcardNameTest.WildcardType.LOCAL_NAME;
import static net.sourceforge.pmd.lang.xpath.ast.ASTWildcardNameTest.WildcardType.PREFIX;

import net.sourceforge.pmd.lang.xpath.ast.NodeTest.NameTest;


/**
 * A wildcard in an {@link ASTAxisStep}.
 */
public final class ASTWildcardNameTest extends AbstractXPathNode implements NameTest {

    private WildcardType wildcardType = ANY;


    ASTWildcardNameTest(XPathParser p, int id) {
        super(p, id);
    }


    void setLocalNameWildcard() {
        wildcardType = LOCAL_NAME;
    }


    void setPrefixWildcard() {
        wildcardType = PREFIX;
    }


    @Override
    public <T> T jjtAccept(XPathParserVisitor<T> visitor, T data) {
        return visitor.visit(this, data);
    }


    enum WildcardType {
        LOCAL_NAME,
        PREFIX,
        ANY
    }
}
/* JavaCC - OriginalChecksum=9cc10133cc5ea5705f090e1cc01ecf11 (do not edit this line) */
