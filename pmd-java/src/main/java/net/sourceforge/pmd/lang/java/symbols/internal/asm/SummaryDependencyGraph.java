package net.sourceforge.pmd.lang.java.symbols.internal.asm;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.checkerframework.checker.nullness.qual.Nullable;

import net.sourceforge.pmd.lang.document.FileId;
import net.sourceforge.pmd.lang.java.internal.TarjanGraph;
import net.sourceforge.pmd.lang.java.internal.TarjanGraph.UniqueGraph;
import net.sourceforge.pmd.lang.java.internal.TarjanGraph.Vertex;
import net.sourceforge.pmd.lang.java.symbols.internal.asm.SummaryDependencyGraph.ClassQueryGraph.BinaryInfo;
import net.sourceforge.pmd.lang.java.symbols.internal.asm.SummaryDependencyGraph.ClassQueryGraph.NodeIdSet;
import net.sourceforge.pmd.util.GraphUtil.DotGraphDescription;

/**
 * This is the data structure that is written to disk. It has info about the
 * classes and source files of the analysis.
 */
public final class SummaryDependencyGraph {

    // This graph is inverted. There is an edge U -> V if V depends on U.
    private final TarjanGraph<DependencyNode> graph;

    SummaryDependencyGraph() {
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

    public void serialize(OutputStream os) throws IOException {
        try (GZIPOutputStream gzip = new GZIPOutputStream(os);
             ObjectOutputStream out = new ObjectOutputStream(gzip)) {
            serialize(out);
        }
    }

    public static ClassQueryGraph deserialize(InputStream is) throws IOException {
        try (GZIPInputStream gzip = new GZIPInputStream(is);
             ObjectInputStream in = new ObjectInputStream(gzip)) {
            return deserialize(in);
        }
    }

    private void serialize(ObjectOutputStream out) throws IOException {
        Map<Vertex<DependencyNode>, Integer> vertexToId = new HashMap<>();
        List<Vertex<DependencyNode>> vertices = graph.toposortVertices();
        // write out all nodes
        out.writeInt(vertices.size());
        for (int i = 0; i < vertices.size(); i++) {
            Vertex<DependencyNode> vertex = vertices.get(i);
            vertexToId.put(vertex, i);
            out.writeInt(vertex.getData().size());
            for (DependencyNode node : vertex.getData()) {
                boolean isClassNode = node instanceof ClassDependencyNode;
                out.writeBoolean(isClassNode);
                node.serialize(out);
            }
        }
        List<Integer> successors = new ArrayList<>();
        for (Vertex<DependencyNode> vertex : vertices) {
            for (Vertex<DependencyNode> succ : graph.successorsOf(vertex)) {
                Integer id = vertexToId.get(succ);
                Objects.requireNonNull(id, "id should not be null");
                successors.add(id);
            }
            successors.sort(Integer::compareTo);
            out.write(successors.size());
            for (Integer succ : successors) {
                out.writeInt(succ);
            }
            successors.clear();
        }
    }

    private static ClassQueryGraph deserialize(ObjectInputStream in) throws IOException {

        final int numVertices = in.readInt();
        final List<@Nullable NodeIdSet> successors = new ArrayList<>(numVertices);
        final Map<String, BinaryInfo> classNodeIdByBinaryName = new HashMap<>();
        final Map<FileId, Integer> sourceNodeIdByFileId = new HashMap<>();

        for (int i = 0; i < numVertices; i++) {
            int nodeSize = in.readInt();
            for (int j = 0; j < nodeSize; j++) {
                boolean isClassNode = in.readBoolean();
                if (isClassNode) {
                    ClassDependencyNode node = ClassDependencyNode.deserialize(in);
                    classNodeIdByBinaryName.put(node.binaryName, new BinaryInfo(i, node.hash));
                } else {
                    SourceDependencyNode node = SourceDependencyNode.deserialize(in);
                    sourceNodeIdByFileId.put(node.fileId, i);
                }
            }
        }

        for (int i = 0; i < numVertices; i++) {
            int numSuccessors = in.readInt();
            if (numSuccessors == 0) {
                successors.add(null);
                continue;
            }
            NodeIdSet nodeSuccs = new NodeIdSet(numSuccessors);
            successors.add(nodeSuccs);
            for (int j = 0; j < numSuccessors; j++) {
                int succId = in.readInt();
                nodeSuccs.set(j, succId);
            }
        }

        return new ClassQueryGraph(classNodeIdByBinaryName, sourceNodeIdByFileId, numVertices, successors);
    }

    /**
     * This is the deserialized structure. Its structure is meant to make queries fast.
     */
    public static class ClassQueryGraph {
        final Map<String, BinaryInfo> classNodeIdByBinaryName;
        final Map<FileId, Integer> sourceNodeIdByFileId;
        final int numVertices;
        final List<@Nullable NodeIdSet> successors;


        ClassQueryGraph(Map<String, BinaryInfo> classNodeIdByBinaryName,
                        Map<FileId, Integer> sourceNodeIdByFileId,
                        int numVertices,
                        List<@Nullable NodeIdSet> successors) {
            this.classNodeIdByBinaryName = classNodeIdByBinaryName;
            this.sourceNodeIdByFileId = sourceNodeIdByFileId;
            this.numVertices = numVertices;
            this.successors = successors;
            assert successors.size() == numVertices;
        }


        static class NodeIdSet {
            // always sorted ascending, always full.
            private final int[] data;

            NodeIdSet(int capacity) {
                data = new int[capacity];
            }

            void set(int idx, int id) {
                assert idx == 0 || id > data[idx - 1];
                data[idx] = id;
            }

            boolean contains(int id) {
                if (data.length <= 4) {
                    // linear check for small size. 4*32 bit is 128bit which is a cache line.
                    for (int i = 0; i < data.length; i++) {
                        if (data[i] == id) {
                            return true;
                        }
                    }
                    return false;
                }
                int index = Arrays.binarySearch(data, 0, data.length, id);
                return index >= 0;
            }
        }

        static class BinaryInfo {
            final int vertexId;
            final long hash;

            private BinaryInfo(int vertexId, long hash) {
                this.vertexId = vertexId;
                this.hash = hash;
            }
        }
    }

    public DotGraphDescription<?> asDotGraph() {
        return graph.asDotGraph();
    }

    public void reduce() {
        // note: these algorithms are not optimized enough for the size of graphs we may encounter.
        // It is likely that the transitive reduction especially is unnecessary.
        // We should use a proper graph library for this.
        graph.mergeCycles();
//        graph.transitiveReductionOnDag();
    }

    abstract static class DependencyNode {
        private DependencyNode() {
        }

        abstract void serialize(ObjectOutputStream out) throws IOException;
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

        void serialize(ObjectOutputStream out) throws IOException {
            out.writeUTF(binaryName);
            out.writeLong(hash);
        }

        static ClassDependencyNode deserialize(ObjectInputStream in) throws IOException {
            String binName = in.readUTF();
            long hash = in.readLong();
            return new ClassDependencyNode(binName, hash);
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

        void serialize(ObjectOutputStream out) throws IOException {
            out.writeUTF(fileId.getAbsolutePath());
            // todo parent path
        }

        static SourceDependencyNode deserialize(ObjectInputStream in) throws IOException {
            String absPath = in.readUTF();
            return new SourceDependencyNode(FileId.fromAbsolutePath(absPath, null));
        }
    }


}
