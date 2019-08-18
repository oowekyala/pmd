/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.cfa.internal.printers;

import java.io.PrintStream;
import java.util.LinkedHashSet;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringEscapeUtils;

import net.sourceforge.pmd.lang.cfa.BasicBlock;
import net.sourceforge.pmd.lang.cfa.BasicBlock.EdgeTarget;
import net.sourceforge.pmd.lang.cfa.CfgRenderer;
import net.sourceforge.pmd.lang.cfa.FlowGraph;

public class GexfRenderer<N> implements CfgRenderer<N> {

    private static final String BEFORE_NODES = "<gexf xmlns=\"http://www.gexf.net/1.2draft\"\n"
        + "          xmlns:xsi=\"http://www.w3.org/2001/XMLSchema−instance\"\n"
        + "          xsi:schemaLocation=\"http://www.gexf.net/1.2draft http://www.gexf.net/1.2draft/gexf.xsd\"\n"
        + "          version=\"1.2\">\n"
        + "      <meta>\n"
        + "        <creator>PMD</creator>\n"
        + "        <description>CFG</description>\n"
        + "      </meta>\n"
        + "      <graph defaultedgetype=\"directed\">\n"
        + "        <nodes>\n";
    private static final String AFTER_NODES = "        </nodes>\n"
        + "        <edges>\n";
    private static final String AFTER_EDGES = "        </edges>\n"
        + "      </graph>\n"
        + "    </gexf>";


    private final RenderStrategies<N> strat;

    public GexfRenderer(RenderStrategies<N> strat) {
        this.strat = strat;
    }


    @Override
    public void render(FlowGraph<N> cfg, PrintStream out) {
        out.append(BEFORE_NODES);

        Set<String> edges = new LinkedHashSet<>();

        Random random = new Random(System.currentTimeMillis());

        for (BasicBlock<N> block : cfg.getBlocks()) {
            String label = strat.renderBlockLabel(block)
                + block.getStatements().stream().map(strat::renderAstNode).collect(Collectors.joining("\n\t\t", "\n", "\n"));

            appendNode(block, label, out);
            for (EdgeTarget<N> outEdge : block.getOutEdges()) {
                edges.add(formatEdge(block, outEdge, random));
            }
        }

        out.println(AFTER_NODES);

        for (String edge : edges) {
            out.println(edge);
        }

        out.println(AFTER_EDGES);
    }

    private void appendNode(BasicBlock<N> block, String label, PrintStream out) {
        out.format("    <node id='%d' label='%s'/>%n", block.hashCode(), StringEscapeUtils.escapeXml10(label));
    }

    private String formatEdge(BasicBlock<N> block, EdgeTarget<N> target, Random random) {
        return String.format("    <edge id='%d' source='%d' target='%d' label='%s'/>",
                             random.nextInt(),
                             block.hashCode(),
                             target.getBlock().hashCode(),
                             StringEscapeUtils.escapeXml10(strat.renderEdgeLabel(target.getCondition())));
    }

}
