/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.ast;


import net.sourceforge.pmd.lang.xpath.ast.ItemType.FunctionTest;


/**
 * Function test that matches anything.
 *
 * <pre>
 *
 * AnyFunctionTest ::= "function" "(" "*" ")"
 *
 * </pre>
 */
public final class ASTAnyFunctionTest extends AbstractXPathNode implements FunctionTest {

    /** Constructor for synthetic node. */
    public ASTAnyFunctionTest() {
        super(XPathParserImplTreeConstants.JJTANYFUNCTIONTEST);
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
