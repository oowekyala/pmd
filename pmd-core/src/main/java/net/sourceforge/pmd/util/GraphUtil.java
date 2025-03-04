/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */


package net.sourceforge.pmd.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

import org.checkerframework.checker.nullness.qual.NonNull;

public final class GraphUtil {

    private GraphUtil() {

    }

    public static final class DotGraphDescription<V> {
        private final Collection<? extends V> vertices;
        private final Function<? super V, ? extends Collection<? extends V>> successorFun;
        private final Function<? super V, DotColor> colorFun;
        private final Function<? super V, String> labelFun;

        /**
         * Create a new graph.
         *
         * @param vertices     Set of vertices
         * @param successorFun Function fetching successors
         * @param colorFun     Color of vertex box
         * @param labelFun     Vertex label
         */
        public DotGraphDescription(Collection<? extends V> vertices,
                                   Function<? super V, ? extends Collection<? extends V>> successorFun,
                                   Function<? super V, DotColor> colorFun,
                                   Function<? super V, String> labelFun) {
            this.vertices = vertices;
            this.successorFun = successorFun;
            this.colorFun = colorFun;
            this.labelFun = labelFun;
        }
    }


    /**
     * Generate a DOT representation for a graph.
     *
     * @param vertices     Set of vertices
     * @param successorFun Function fetching successors
     * @param colorFun     Color of vertex box
     * @param labelFun     Vertex label
     * @param <V>          Type of vertex, must be usable as map key (equals/hash)
     * @deprecated Use {@link #toDot(DotGraphDescription)}
     */
    @Deprecated
    public static <V> String toDot(
        Collection<? extends V> vertices,
        Function<? super V, ? extends Collection<? extends V>> successorFun,
        Function<? super V, DotColor> colorFun,
        Function<? super V, String> labelFun
    ) {
        return toDot(new DotGraphDescription<>(vertices, successorFun, colorFun, labelFun));
    }

    public static <V> String toDot(GraphUtil.DotGraphDescription<V> graph) {
        StringBuilder sb = new StringBuilder();
        try {
            toDot(sb, graph);
        } catch (IOException e) {
            throw AssertionUtil.shouldNotReachHere("StringBuilder doesn't throw IOException", e);
        }
        return sb.toString();
    }

    /**
     * Generate a DOT representation for a graph.
     *
     * @param <V>          Type of vertex, must be usable as map key (equals/hash)
     */
    public static <A extends Appendable, V> void toDot(A sb, DotGraphDescription<V> graph) throws IOException {
        // generates a DOT representation of the lattice
        // Visualize eg at http://webgraphviz.com/

        sb.append("strict digraph {\n");
        Map<V, String> ids = new HashMap<>();
        int i = 0;
        List<V> vertexList = new ArrayList<>(graph.vertices);
        vertexList.sort(Comparator.comparing(Object::toString)); // for reproducibility in tests
        for (V node : vertexList) {
            String id = "n" + i++;
            ids.put(node, id);
            sb.append(id)
              .append(" [ shape=box, color=")
              .append(graph.colorFun.apply(node).toDot())
              .append(", label=\"")
              .append(escapeDotString(graph.labelFun.apply(node)))
              .append("\" ];\n");
        }

        List<String> edges = new ArrayList<>();

        for (V node : vertexList) {
            // edges
            String id = ids.get(node);
            for (V succ : graph.successorFun.apply(node)) {
                String succId = ids.get(succ);
                edges.add(id + " -> " + succId + ";\n");
            }
        }

        edges.sort(Comparator.naturalOrder()); // for reproducibility in tests
        for (String edge : edges) {
            sb.append(edge);
        }

        sb.append('}');
    }


    @NonNull
    private static String escapeDotString(String string) {
        return string.replaceAll("\\R", "\\\n")
                     .replaceAll("\"", "\\\"");
    }

    public enum DotColor {
        GREEN, BLACK;

        String toDot() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

}
