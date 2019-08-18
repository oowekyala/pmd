/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.cfa.internal;

import net.sourceforge.pmd.lang.cfa.CfgRenderer.CfgPrinterCollection;
import net.sourceforge.pmd.lang.cfa.internal.CfaFramework;
import net.sourceforge.pmd.lang.cfa.internal.impl.CfgBuilder;
import net.sourceforge.pmd.lang.cfa.internal.printers.BasePrinterCollection;
import net.sourceforge.pmd.lang.cfa.internal.printers.RenderStrategies;
import net.sourceforge.pmd.lang.java.ast.JavaNode;

public class JavaCfa implements CfaFramework<JavaNode> {

    public static final JavaCfa INSTANCE = new JavaCfa();
    private static final CfgPrinterCollection<JavaNode> PRINTERS =
        new BasePrinterCollection<>(new RenderStrategies<JavaNode>() {
        @Override
        public String renderAstNode(JavaNode astNode) {
            return astNode.getText().toString();
        }
    });

    @Override
    public CfgPrinterCollection<JavaNode> getPrinters() {
        return PRINTERS;
    }


    @Override
    public CfgBuilder<JavaNode, ?> getBuilder() {
        return JavaCfgBuilder.DFA_INSTANCE;
    }
}
