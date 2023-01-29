/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.xpath.internal;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.ASTAssignableExpr.ASTNamedReferenceExpr;
import net.sourceforge.pmd.lang.java.ast.ASTVariableDeclaratorId;
import net.sourceforge.pmd.lang.java.symbols.JVariableSymbol;
import net.sourceforge.pmd.lang.rule.xpath.internal.AstElementNode;
import net.sourceforge.pmd.lang.rule.xpath.internal.AstTreeInfo;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.SequenceType;

/**
 * The function "declaratorId".
 */
public final class GetDeclarationFun extends BaseJavaXPathFunction {

    private static final SequenceType[] ARGTYPES = { };

    public static final GetDeclarationFun INSTANCE = new GetDeclarationFun("declaratorId");

    private GetDeclarationFun(String localName) {
        super(localName);
    }

    @Override
    public SequenceType[] getArgumentTypes() {
        return ARGTYPES;
    }

    @Override
    public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
        return SequenceType.OPTIONAL_NODE;
    }

    @Override
    public boolean dependsOnFocus() {
        return true;
    }

    @Override
    public ExtensionFunctionCall makeCallExpression() {
        return new ExtensionFunctionCall() {
            @Override
            public Sequence call(XPathContext context, Sequence[] arguments) {
                Node contextNode = ((AstElementNode) context.getContextItem()).getUnderlyingNode();

                if (contextNode instanceof ASTNamedReferenceExpr) {
                    JVariableSymbol sym = ((ASTNamedReferenceExpr) contextNode).getReferencedSym();
                    if (sym != null && sym.tryGetNode() != null) {
                        ASTVariableDeclaratorId decl = sym.tryGetNode();
                        AstTreeInfo treeInfo = ((AstElementNode) context.getContextItem()).getTreeInfo();
                        return treeInfo.findWrapperFor(decl);
                    }
                }
                return EmptySequence.getInstance();
            }
        };
    }
}
