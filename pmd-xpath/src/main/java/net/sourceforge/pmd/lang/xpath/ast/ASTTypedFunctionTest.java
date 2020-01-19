/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;

import net.sourceforge.pmd.lang.xpath.ast.ItemType.FunctionTest;


/**
 * Function test testing the types of parameters and return type.
 *
 * <pre>
 *
 * TypedFunctionTest ::= "function" {@linkplain ASTArgumentTypeList ArgumentTypeList} "as" {@linkplain ASTSequenceType SequenceType}
 *
 * </pre>
 */
public final class ASTTypedFunctionTest extends AbstractXPathNode implements FunctionTest {

    /** Constructor for synthetic node. */
    public ASTTypedFunctionTest() {
        super(XPathParserImplTreeConstants.JJTTYPEDFUNCTIONTEST);
    }


    /**
     * Returns the parameter list.
     */
    public ASTArgumentTypeList getParamTypeList() {
        return (ASTArgumentTypeList) jjtGetChild(0);
    }


    /**
     * Returns the declared return type of the function.
     */
    public ASTSequenceType getDeclaredReturnType() {
        return (ASTSequenceType) jjtGetChild(1);
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
/* JavaCC - OriginalChecksum=ed05b0527ec9096e982d2e82504dd7d2 (do not edit this line) */
