/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

/**
 * Context item expression.
 *
 * <pre>
 *
 * ContextItemExpr ::= "."
 *
 * </pre>
 */
public final class ASTContextItemExpr extends AbstractXPathNode implements PrimaryExpr {


    ASTContextItemExpr(XPathParser p, int id) {
        super(p, id);
    }


    @Override
    public <T> T jjtAccept(XPathParserVisitor<T> visitor, T data) {
        return visitor.visit(this, data);
    }
}
/* JavaCC - OriginalChecksum=f84ce10a4a9a29e0d18b17f6a11e34f8 (do not edit this line) */
