/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

import java.util.List;


/**
 * Map expression.
 *
 * <pre>
 *
 * MapExpr ::= {@linkplain ASTPathExpr PathExpr} ( "!" {@linkplain ASTPathExpr PathExpr} )+
 *
 * </pre>
 */
public final class ASTMapExpr extends AbstractXPathNode implements ExprSingle {


    ASTMapExpr(XPathParser p, int id) {
        super(p, id);
    }


    @Override
    public <T> void jjtAccept(SideEffectingVisitor<T> visitor, T data) {
        visitor.visit(this, data);
    }


    @Override
    public <T> T jjtAccept(XPathGenericVisitor<T> visitor, T data) {
        return visitor.visit(this, data);
    }


    public List<ExprSingle> getOperands() {
        return findChildrenOfType(ExprSingle.class);
    }


}
/* JavaCC - OriginalChecksum=4cbaf35312e52a85f9f37924cf27e023 (do not edit this line) */
