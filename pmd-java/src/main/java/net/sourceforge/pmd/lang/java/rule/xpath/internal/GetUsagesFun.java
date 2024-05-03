/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.xpath.internal;

import static net.sourceforge.pmd.util.CollectionUtil.emptyList;

import net.sourceforge.pmd.lang.java.ast.ASTVariableId;

/**
 * The function "localUsages".
 */
public final class GetUsagesFun extends BaseJavaXPathFunction {

    private static final Type[] ARGTYPES = { };

    public static final GetUsagesFun INSTANCE = new GetUsagesFun("localUsages");

    private GetUsagesFun(String localName) {
        super(localName);
    }

    @Override
    public Type[] getArgumentTypes() {
        return ARGTYPES;
    }

    @Override
    public Type getResultType() {
        return Type.ELEMENT_SEQUENCE;
    }

    @Override
    public boolean dependsOnContext() {
        return true;
    }

    @Override
    public FunctionCall makeCallExpression() {
        return (contextNode, arguments)
            -> contextNode instanceof ASTVariableId ? ((ASTVariableId) contextNode).getLocalUsages() : emptyList();
    }
}
