/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */


package net.sourceforge.pmd.lang.cfa;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

import net.sourceforge.pmd.lang.cfa.internal.printers.BasePrinterCollection;
import net.sourceforge.pmd.lang.cfa.internal.printers.RenderStrategies;

/** Generic text renderer for a control flow graph. */
public interface CfgRenderer<N> {

    /** Renders the given CFG onto the print stream. */
    void render(FlowGraph<N> cfg, PrintStream out);

    default String renderString(FlowGraph<N> cfg) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        render(cfg, new PrintStream(bos));
        return bos.toString();
    }


    default void renderToFile(FlowGraph<N> cfg, Path file) throws IOException {
        Files.createDirectories(file.getParent());
        try (OutputStream os = Files.newOutputStream(file);
             PrintStream out = new PrintStream(os)) {
            render(cfg, out);
        }
    }


    /** A language-specific collection of CFG renderers. */
    interface CfgPrinterCollection<N> {

        /** It ain't much but it's honest work. */
        CfgRenderer<N> ascii();


        /**
         * Exports the graph to GEXF format. Files like that can be opened
         * in graph editors like Gephi (https://gephi.org/)
         */
        CfgRenderer<N> gexf();


        /**
         * Exports the graph to LaTeX format. Running lualatex on the
         * produced file produces a PDF image of the graph.
         */
        CfgRenderer<N> latex();


        static <N> CfgPrinterCollection<N> defaultPrinters() {
            return new BasePrinterCollection<>(new RenderStrategies<N>() { });
        }
    }

}
