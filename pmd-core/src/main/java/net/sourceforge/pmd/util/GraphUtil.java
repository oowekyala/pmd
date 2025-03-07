/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */


package net.sourceforge.pmd.util;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public final class GraphUtil {

    public static final String GEXF_SCHEMA = "http://gexf.net/1.3";

    private GraphUtil() {

    }

    public static class DotGraphDescription<V> {
        private final List<V> vertices;
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
            this.vertices = new ArrayList<>(vertices);
            this.vertices.sort(Comparator.comparing(Object::toString)); // for reproducibility in tests
            this.successorFun = successorFun;
            this.colorFun = colorFun;
            this.labelFun = labelFun;
        }

        Map<V, String> makeGraphIds() {
            Map<V, String> ids = new HashMap<>();
            int i = 0;
            for (V node : vertices) {
                String id = "n" + i++;
                ids.put(node, id);

            }
            return ids;
        }
    }


    public static final class GexfGraphDescription<V> extends DotGraphDescription<V> {

        private int nextAttrId;
        private final List<GexfAttribute> attributes = new ArrayList<>();

        /**
         * Create a new graph.
         *
         * @param vertices     Set of vertices
         * @param successorFun Function fetching successors
         * @param colorFun     Color of vertex box
         * @param labelFun     Vertex label
         */
        public GexfGraphDescription(Collection<? extends V> vertices,
                                    Function<? super V, ? extends Collection<? extends V>> successorFun,
                                    Function<? super V, DotColor> colorFun,
                                    Function<? super V, String> labelFun) {
            super(vertices, successorFun, colorFun, labelFun);
        }


        public void recordAttribute(String title, String type, Function<? super V, String> valueFun) {
            String id = Integer.toString(attributes.size());
            attributes.add(new GexfAttribute(id, title, type, valueFun));
        }

        public class GexfAttribute {
            private final String id;
            private final String title;
            private final String type;
            private final Function<? super V, String> valueFun;

            public GexfAttribute(String id, String title, String type, Function<? super V, String> valueFun) {
                this.id = id;
                this.title = title;
                this.type = type;
                this.valueFun = valueFun;
            }
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
        Map<V, String> ids = graph.makeGraphIds();
        for (V node : graph.vertices) {
            sb.append(ids.get(node))
              .append(" [ shape=box, color=")
              .append(graph.colorFun.apply(node).toDot())
              .append(", label=\"")
              .append(escapeDotString(graph.labelFun.apply(node)))
              .append("\" ];\n");
        }

        List<String> edges = new ArrayList<>();

        for (V node : graph.vertices) {
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


    private static @NonNull String escapeDotString(String string) {
        return string.replaceAll("\\R", "\\\n")
                     .replaceAll("\"", "\\\"");
    }

    /**
     * Generate a GEXF representation for a graph. This is the format used by Gephi,
     * which is more practical for large networks.
     *
     * @param <V>          Type of vertex, must be usable as map key (equals/hash)
     */
    public static <V> String toGexf(DotGraphDescription<V> graph) {
        StringWriter sw = new StringWriter();
        toGexf(sw, graph);
        return sw.toString();
    }

    /**
     * Generate a GEXF representation for a graph. This is the format used by Gephi,
     * which is more practical for large networks.
     *
     * @param <V>          Type of vertex, must be usable as map key (equals/hash)
     */
    public static <V> void toGexf(Writer out, DotGraphDescription<V> graph) {
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.newDocument();

            makeGexf(document, graph);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.transform(new DOMSource(document), new StreamResult(out));
        } catch (DOMException | FactoryConfigurationError | ParserConfigurationException | TransformerException e) {
            throw new RuntimeException(e);
        }
    }

    private static <V> void makeGexf(Document document, DotGraphDescription<V> graph) {
        Element root = document.createElementNS(GEXF_SCHEMA, "gexf");
        root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        root.setAttributeNS("http://www.w3.org/2001/XMLSchema-instance",
                            "xsi:schemaLocation",
                            GEXF_SCHEMA + " " + GEXF_SCHEMA + "/gexf.xsd");
        root.setAttribute("version", "1.3");
        document.appendChild(root);

        Element graphElt = document.createElement("graph");
        graphElt.setAttribute("defaultedgetype", "directed");
        root.appendChild(graphElt);

        if (graph instanceof GexfGraphDescription) {
            Element attributesElt = document.createElement("attributes");
            attributesElt.setAttribute("class", "node");
            graphElt.appendChild(attributesElt);

            for (GexfGraphDescription<V>.GexfAttribute attr : ((GexfGraphDescription<V>) graph).attributes) {
                Element attrElt = document.createElement("attribute");
                attrElt.setAttribute("id", attr.id);
                attrElt.setAttribute("title", attr.title);
                attrElt.setAttribute("type", attr.type);
                attributesElt.appendChild(attrElt);
            }
        }

        Element nodesElt = document.createElement("nodes");
        graphElt.appendChild(nodesElt);

        Map<V, String> ids = graph.makeGraphIds();
        for (V node : graph.vertices) {
            Element nodeElt = document.createElement("node");
            nodeElt.setAttribute("id", ids.get(node));
            nodeElt.setAttribute("label", graph.labelFun.apply(node));
            nodesElt.appendChild(nodeElt);

            if (graph instanceof GexfGraphDescription) {
                Element attValuesElt = document.createElement("attvalues");
                nodeElt.appendChild(attValuesElt);

                for (GexfGraphDescription<V>.GexfAttribute attr : ((GexfGraphDescription<V>) graph).attributes) {
                    Element attrElt = document.createElement("attvalue");
                    attrElt.setAttribute("for", attr.id);
                    attrElt.setAttribute("value", attr.valueFun.apply(node));
                    attValuesElt.appendChild(attrElt);
                }
            }
        }

        Element edgesElt = document.createElement("edges");
        graphElt.appendChild(edgesElt);

        for (V node : graph.vertices) {
            String sourceId = ids.get(node);
            for (V succ : graph.successorFun.apply(node)) {
                String targetId = ids.get(succ);
                Element edge = document.createElement("edge");
                edge.setAttribute("source", sourceId);
                edge.setAttribute("target", targetId);
                edgesElt.appendChild(edge);
            }
        }
    }

    public enum DotColor {
        GREEN, BLACK;

        String toDot() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

}
