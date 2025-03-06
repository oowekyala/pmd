package net.sourceforge.pmd.lang.java.symbols.internal.asm;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import net.sourceforge.pmd.lang.document.FileId;
import net.sourceforge.pmd.lang.java.internal.TarjanGraph;
import net.sourceforge.pmd.lang.java.internal.TarjanGraph.UniqueGraph;
import net.sourceforge.pmd.lang.java.internal.TarjanGraph.Vertex;
import net.sourceforge.pmd.util.GraphUtil.DotGraphDescription;

/**
 * This is the data structure that is written to disk. It has info about the
 * classes and source files of the analysis.
 */
final class SummaryDependencyGraph {

    // This graph is inverted. There is an edge U -> V if V depends on U.
    private final TarjanGraph<DependencyNode> graph;

    public SummaryDependencyGraph() {
        graph = new UniqueGraph<>();
    }

    Vertex<DependencyNode> addSourceLeaf(FileId fileId) {
        return graph.addLeaf(new SourceDependencyNode(fileId));
    }

    Vertex<DependencyNode> addClassLeaf(String binaryName, long hash) {
        return graph.addLeaf(new ClassDependencyNode(binaryName, hash));
    }

    void recordDependency(Vertex<DependencyNode> from, Vertex<DependencyNode> to) {
        // note the inversion
        graph.addEdge(to, from);
    }

    void serialize(ObjectOutputStream out) throws IOException {
        // todo this is not finished
        Set<Vertex<DependencyNode>> vertices = graph.getVertices();
        Map<DependencyNode, Integer> nodeToId = new HashMap<>();
        out.writeInt(vertices.size());
        int nextId = 0;
        for (Vertex<DependencyNode> vertex : vertices) {
            Set<DependencyNode> data = vertex.getData();
            // todo
            nextId++;
        }
    }

    public DotGraphDescription<?> asDotGraph() {
        return graph.asDotGraph();
    }

    public void reduce() {
        graph.mergeCycles();
        graph.transitiveReductionOnDag();
    }

    static class DependencyNode {
        private DependencyNode() {
        }
    }

    private static final class ClassDependencyNode extends DependencyNode {
        private final String binaryName;
        private final long hash;

        ClassDependencyNode(String binaryName, long hash) {
            this.binaryName = binaryName;
            this.hash = hash;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            ClassDependencyNode that = (ClassDependencyNode) o;
            return Objects.equals(binaryName, that.binaryName);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(binaryName);
        }

        @Override
        public String toString() {
            return binaryName;
        }
    }

    private static final class SourceDependencyNode extends DependencyNode {
        private final FileId fileId;

        SourceDependencyNode(FileId fileId) {
            this.fileId = fileId;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            SourceDependencyNode that = (SourceDependencyNode) o;
            return Objects.equals(fileId, that.fileId);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(fileId);
        }

        @Override
        public String toString() {
            return '/' + fileId.getFileName();
        }
    }


}
