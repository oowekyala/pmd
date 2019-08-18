/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.cfa.internal.printers;

import java.io.PrintStream;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.checkerframework.checker.nullness.qual.NonNull;

import net.sourceforge.pmd.lang.cfa.BasicBlock;
import net.sourceforge.pmd.lang.cfa.BasicBlock.EdgeTarget;
import net.sourceforge.pmd.lang.cfa.CfgRenderer;
import net.sourceforge.pmd.lang.cfa.FlowGraph;
import net.sourceforge.pmd.lang.cfa.exprs.EdgeCondition;

public class LatexRenderer<N> implements CfgRenderer<N> {

    // private static final String SPRING_LAYOUT = "spring layout, node distance=300pt";
    private static final String LAYERED_LAYOUT = "layered layout, level distance=100pt, sibling distance=50pt";
    private static final String BEFORE_NODES = "\\documentclass[border=20,tikz]{standalone}\n"
        + "% Make sure you compile with lualatex\n"
        + "\\usepgflibrary{graphdrawing}\n"
        + "\\usepackage[T1]{fontenc}\n"
        + "\\usegdlibrary{layered,force}\n"
        + "\\usetikzlibrary{graphs,graphdrawing,arrows.meta}\n"
        + "\n"
        + "\\tikzset{every picture/.style={/utils/exec={\\ttfamily}}}"
        + "\n"
        + "\\begin{document}\n"
        + "\n"
        + "\\begin{tikzpicture}[" + LAYERED_LAYOUT + "]\n";
    private static final String AFTER_EDGES =
        "\t\\end{tikzpicture}\n"
            + "\n"
            + "\\end{document}";


    private final RenderStrategies<N> strat;

    public LatexRenderer(RenderStrategies<N> strat) {
        this.strat = strat;
    }


    @Override
    public void render(FlowGraph<N> cfg, PrintStream out) {
        out.append(BEFORE_NODES);

        Set<String> edges = new LinkedHashSet<>();

        for (BasicBlock<N> block : cfg.getBlocks()) {
            String label = strat.renderBlockLabel(block)
                + block.getStatements().stream().map(strat::renderAstNode).collect(Collectors.joining("\n\t\t", "\n", "\n"));

            appendNode(block, label, out);
            for (EdgeTarget<N> outEdge : block.getOutEdges()) {
                edges.add(formatEdge(block, outEdge));
            }
        }

        for (String edge : edges) {
            out.println(edge);
        }

        out.println(AFTER_EDGES);
    }

    private String escapeLatex(String desc) {
        return desc.replace("\n", "\\\\")
                   .replace("&", "\\&")
                   .replace("{", "\\{")
                   .replace("}", "\\}")
                   .replace("^", "\\^")
                   .replace("_", "\\_");
    }

    private void appendNode(BasicBlock<N> block, String label, PrintStream out) {
        out.format("    \\pgfnode{rectangle}{north}{\\begin{tabular}{l}%s\\end{tabular}}{%d}{\\pgfusepath{stroke}}%n", escapeLatex(label.trim()), block.hashCode());
    }

    private String formatEdge(BasicBlock<N> block, EdgeTarget<N> target) {
        return String.format("    \\pgfgdedge{%d}{%d}{->}{}{%s}",
                             block.hashCode(),
                             target.getBlock().hashCode(),
                             edgeLabel(target.getCondition()));
    }

    @NonNull
    private String edgeLabel(EdgeCondition condition) {
        if (condition == EdgeCondition.TRUE) {
            return "";
        }
        return "node[sloped, fill=white]{" + escapeLatex(strat.renderEdgeLabel(condition)) + "}";
    }

}
