/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.cfa.internal;

import net.sourceforge.pmd.lang.cfa.CfgRenderer.CfgPrinterCollection;
import net.sourceforge.pmd.lang.cfa.internal.impl.CfgBuilder;

/**
 * Façade object for a language-specific CFA framework.
 *
 * @param <N> Type of nodes of the built CFG
 */
public interface CfaFramework<N> {

    /** Returns the set of language-specific printers. */
    default CfgPrinterCollection<N> getPrinters() {
        return CfgPrinterCollection.defaultPrinters();
    }


    /** Returns the object responsible for building CFGs for this language. */
    CfgBuilder<N, ?> getBuilder();

}
