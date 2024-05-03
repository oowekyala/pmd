/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.xpath.internal;

import net.sourceforge.pmd.lang.java.ast.ASTAssignableExpr.ASTNamedReferenceExpr;
import net.sourceforge.pmd.lang.java.symbols.JVariableSymbol;

/**
 * The function "declaratorId".
 */
public final class GetDeclarationFun extends BaseJavaXPathFunction {

    private static final Type[] ARGTYPES = { };

    public static final GetDeclarationFun INSTANCE = new GetDeclarationFun("declaratorId");

    private GetDeclarationFun(String localName) {
        super(localName);
    }

    @Override
    public Type[] getArgumentTypes() {
        return ARGTYPES;
    }

    @Override
    public Type getResultType() {
        return Type.NULLABLE_ELEMENT;
    }

    @Override
    public boolean dependsOnContext() {
        return true;
    }

    @Override
    public FunctionCall makeCallExpression() {
        return (contextNode, arguments) -> {
            if (contextNode instanceof ASTNamedReferenceExpr) {
                JVariableSymbol sym = ((ASTNamedReferenceExpr) contextNode).getReferencedSym();
                if (sym != null) {
                    return sym.tryGetNode();
                }
            }
            return null;
        };
    }
}
