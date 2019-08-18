/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.cfa.internal.printers;

import net.sourceforge.pmd.lang.cfa.CfgRenderer;
import net.sourceforge.pmd.lang.cfa.CfgRenderer.CfgPrinterCollection;

public class BasePrinterCollection<N> implements CfgPrinterCollection<N> {

    private final CfgRenderer<N> gexf;
    private final CfgRenderer<N> ascii;
    private final CfgRenderer<N> latex;

    public BasePrinterCollection(RenderStrategies<N> strategies) {
        gexf = new GexfRenderer<>(strategies);
        ascii = new AsciiRenderer<>(strategies);
        latex = new LatexRenderer<>(strategies);
    }

    @Override
    public CfgRenderer<N> ascii() {
        return ascii;
    }

    @Override
    public CfgRenderer<N> gexf() {
        return gexf;
    }

    @Override
    public CfgRenderer<N> latex() {
        return latex;
    }
}
