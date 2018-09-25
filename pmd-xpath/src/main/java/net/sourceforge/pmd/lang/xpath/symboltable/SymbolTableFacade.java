/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.xpath.symboltable;

import net.sourceforge.pmd.lang.xpath.ast.ASTXPathRoot;
import net.sourceforge.pmd.lang.xpath.ast.VarBindingResolver;


/**
 * Symbol table façade for XPath.
 *
 * @author Clément Fournier
 * @since 6.7.0
 */
public final class SymbolTableFacade {

    private SymbolTableFacade() {
    }


    /**
     * Runs symbol table analyses on the expression.
     *
     * @param root Expression
     */
    public static void runOn(ASTXPathRoot root) {
        root.jjtAccept(new VarBindingResolver());
    }
}
