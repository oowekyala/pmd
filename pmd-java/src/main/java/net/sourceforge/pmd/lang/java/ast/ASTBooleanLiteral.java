/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.ast;

/**
 * The boolean literal, either "true" or "false".
 */
public final class ASTBooleanLiteral extends AbstractLiteral implements ASTLiteral {

    private boolean isTrue;


    ASTBooleanLiteral(int id) {
        super(id);
    }


    ASTBooleanLiteral(JavaParser p, int id) {
        super(p, id);
    }

    @Override
    public int jjtGetId() {
        return isTrue ? NodeFactory.TRUE_LITERAL : super.jjtGetId();
    }

    void setTrue() {
        isTrue = true;
    }

    public boolean isTrue() {
        return this.isTrue;
    }

    @Override
    public Object jjtAccept(JavaParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }


    @Override
    public <T> void jjtAccept(SideEffectingVisitor<T> visitor, T data) {
        visitor.visit(this, data);
    }

    @Override
    public NodeMetaModel<? extends JavaNode> metaModel() {
        return new NodeMetaModel<>(getClass(), true);
    }
}
