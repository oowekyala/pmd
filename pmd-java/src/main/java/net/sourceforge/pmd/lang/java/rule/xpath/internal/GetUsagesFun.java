/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.xpath.internal;

import java.util.List;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.ASTVariableDeclaratorId;
import net.sourceforge.pmd.lang.rule.xpath.internal.AstElementNode;
import net.sourceforge.pmd.lang.rule.xpath.internal.AstTreeInfo;
import net.sourceforge.pmd.util.CollectionUtil;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.SequenceExtent;
import net.sf.saxon.value.SequenceType;

/**
 * The function "localUsages".
 */
public final class GetUsagesFun extends BaseJavaXPathFunction {

    private static final SequenceType[] ARGTYPES = { };

    public static final GetUsagesFun INSTANCE = new GetUsagesFun("localUsages");

    private GetUsagesFun(String localName) {
        super(localName);
    }

    @Override
    public SequenceType[] getArgumentTypes() {
        return ARGTYPES;
    }

    @Override
    public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
        return SequenceType.NODE_SEQUENCE;
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

                if (contextNode instanceof ASTVariableDeclaratorId) {
                    AstTreeInfo treeInfo = ((AstElementNode) context.getContextItem()).getTreeInfo();
                    List<AstElementNode> usages = CollectionUtil.map(
                        ((ASTVariableDeclaratorId) contextNode).getLocalUsages(),
                        treeInfo::findWrapperFor
                    );
                    return new SequenceExtent(usages);
                }
                return EmptySequence.getInstance();
            }
        };
    }
}
