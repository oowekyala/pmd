/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.ast;

/**
 * @deprecated Replaced with {@link ASTArrayAllocation} and {@link ASTConstructorCall}
 */
@Deprecated
public class ASTAllocationExpression extends AbstractJavaTypeNode {

    ASTAllocationExpression(int id) {
        super(id);
    }


    @Override
    protected <P, R> R acceptVisitor(JavaVisitor<? super P, ? extends R> visitor, P data) {
        throw new UnsupportedOperationException("Node was removed from grammar");
    }


    /**
     * Returns true if this expression defines a body,
     * which is compiled to an anonymous class. If this
     * method returns false, then {@link #getQualifiedName()}
     * returns {@code null}.
     */
    public boolean isAnonymousClass() {
        // check the last child
        return getNumChildren() > 1 && getChild(getNumChildren() - 1) instanceof ASTClassOrInterfaceBody;
    }


}
