package net.sourceforge.pmd.lang.java.symbols.internal.asm;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.pcollections.HashTreePSet;
import org.pcollections.PSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sourceforge.pmd.lang.document.FileId;
import net.sourceforge.pmd.lang.java.internal.TarjanGraph;
import net.sourceforge.pmd.lang.java.internal.TarjanGraph.UniqueGraph;
import net.sourceforge.pmd.lang.java.internal.TarjanGraph.Vertex;
import net.sourceforge.pmd.lang.java.symbols.internal.asm.ClassDependencyGraph.ClassQueryGraph.BinaryInfo;
import net.sourceforge.pmd.lang.java.symbols.internal.asm.ClassDependencyGraph.ClassQueryGraph.NodeIdSet;
import net.sourceforge.pmd.lang.java.symbols.internal.asm.ClasspathDependencyTracker.ClasspathRequest;
import net.sourceforge.pmd.util.CollectionUtil;
import net.sourceforge.pmd.util.GraphUtil.DotGraphDescription;
import net.sourceforge.pmd.util.GraphUtil.GexfGraphDescription;

/**
 * This is the data structure that is written to disk. It has info about the
 * classes and source files of the analysis.
 */
public final class ClassDependencyGraph {

    private static final Logger LOG = LoggerFactory.getLogger(ClassDependencyGraph.class);

    // This graph is inverted. There is an edge U -> V if V depends on U.
    private final CompressibleGraph graph;

    ClassDependencyGraph() {
        graph = new CompressibleGraph();
    }

    Vertex<DependencyNode> addSourceLeaf(FileId fileId) {
        return graph.addLeaf(new SourceDependencyNode(fileId));
    }

    Vertex<DependencyNode> addClassLeaf(String binaryName, long hash) {
        return graph.addLeaf(new ClassDependencyNode(binaryName, hash));
    }

    void recordDependency(Vertex<DependencyNode> from, Vertex<DependencyNode> to) {
        // notice the inversion
        graph.addEdge(to, from);
    }

    /**
     * This is a variant of the {@link UniqueGraph} that implements a custom
     * graph compression (pruning) routine.
     */
    private static class CompressibleGraph extends UniqueGraph<DependencyNode> {

        @Override
        protected Vertex<DependencyNode> makeVertex(Set<DependencyNode> data) {
            return new DependencyVertex(this, data);
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        private Set<DependencyVertex> castVertices(Set<Vertex<DependencyNode>> vertices) {
            return (Set) vertices;
        }

        void compressGraphHeuristically() {
            Set<DependencyVertex> vertices = castVertices(getVertices());
            CompressionState state = new CompressionState();

            for (DependencyVertex vertex : vertices) {
                if (vertex.downstream == null) {
                    compressGraphRec(vertex, state);
                }
            }

            if (LOG.isTraceEnabled()) {
                int numPruned = state.toBeMerged.values().stream()
                                                .filter(it -> it.size() >= 2)
                                                .mapToInt(it -> it.size() - 1)
                                                .sum();
                int numVertices = vertices.size();
                int percentPruned = 100 * numPruned / numVertices;
                LOG.trace("Pruned {} vertices from dependency graph ({}%)", numPruned, percentPruned);
            }

            batchMerge(state.toBeMerged.values());

        }

        private static class CompressionState {
            private final Map<PSet<SourceDependencyNode>, Set<DependencyVertex>> toBeMerged = new HashMap<>();
        }

        private void compressGraphRec(DependencyVertex v, CompressionState state) {
            v.downstream = HashTreePSet.empty();

            for (DependencyNode node : v.getData()) {
                if (node instanceof SourceDependencyNode) {
                    v.downstream = v.downstream.plus((SourceDependencyNode) node);
                }
            }

            for (DependencyVertex w : castVertices(successorsOf(v))) {
                if (w.downstream == null) {
                    compressGraphRec(w, state);
                }
                v.downstream = CollectionUtil.union(v.downstream, w.downstream);
            }


            for (DependencyVertex w : castVertices(successorsOf(v))) {
                if (w.downstream.equals(v.downstream)) {
                    Set<DependencyVertex> equivClass = state.toBeMerged.computeIfAbsent(v.downstream, k -> new LinkedHashSet<>());
                    equivClass.add(v);
                    equivClass.add(w);
                    // They're equal, but maybe not the same instance.
                    // We deduplicate them
                    w.downstream = v.downstream;
                }
            }
        }

        static final class DependencyVertex extends Vertex<DependencyNode> {
            private PSet<SourceDependencyNode> downstream;

            DependencyVertex(TarjanGraph<DependencyNode> owner, Set<DependencyNode> data) {
                super(owner, data);
            }
        }
    }

    /**
     * Serialize this dependency graph onto the given stream.
     * @param os Output stream
     * @throws IOException If writing fails
     */
    public void serialize(OutputStream os) throws IOException {
        try (GZIPOutputStream gzip = new GZIPOutputStream(os);
             ObjectOutputStream out = new ObjectOutputStream(gzip)) {
            serialize(out);
        }
    }

    /**
     * Deserialize a structure written by {@link #serialize(OutputStream)}
     * into a {@link ClassQueryGraph}.
     *
     * @param is Input stream
     *
     * @return A structure that can be used to test the classpath for freshness
     *
     * @throws IOException if reading fails
     */
    public static ClassQueryGraph deserialize(InputStream is) throws IOException {
        try (GZIPInputStream gzip = new GZIPInputStream(is);
             ObjectInputStream in = new ObjectInputStream(gzip)) {
            return deserialize(in);
        }
    }

    private void serialize(ObjectOutputStream out) throws IOException {
        Map<Vertex<DependencyNode>, Integer> vertexToId = new HashMap<>();
        List<Vertex<DependencyNode>> vertices = new ArrayList<>(graph.getVertices());

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
        // then write out edges
        List<Integer> successors = new ArrayList<>();
        for (Vertex<DependencyNode> vertex : vertices) {
            for (Vertex<DependencyNode> succ : graph.successorsOf(vertex)) {
                Integer id = vertexToId.get(succ);
                Objects.requireNonNull(id, "id should not be null");
                successors.add(id);
            }
            out.writeInt(successors.size());
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
        final Map<Integer, Set<FileId>> filesByVxId = new HashMap<>();

        for (int i = 0; i < numVertices; i++) {
            int nodeSize = in.readInt();
            for (int j = 0; j < nodeSize; j++) {
                boolean isClassNode = in.readBoolean();
                if (isClassNode) {
                    ClassDependencyNode node = ClassDependencyNode.deserialize(in);
                    classNodeIdByBinaryName.put(node.binaryName, new BinaryInfo(i, node.hash));
                } else {
                    SourceDependencyNode node = SourceDependencyNode.deserialize(in);
                    filesByVxId.computeIfAbsent(i, k -> new HashSet<>()).add(node.fileId);
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
            for (int j = 0; j < numSuccessors; j++) {
                int succId = in.readInt();
                nodeSuccs.set(j, succId);
            }
            successors.add(nodeSuccs);
        }

        return new ClassQueryGraph(classNodeIdByBinaryName, filesByVxId, numVertices, successors);
    }

    /**
     * This is the deserialized structure. Its structure is meant to make queries fast.
     * We will use this to replay queries and compare them to the current classpath.
     */
    public static final class ClassQueryGraph {
        // todo some things are missing:
        //  - initializing the ClasspathDependencyTracker
        //  - consider the unknown file
        //  - taking care of self classpath (maybe AnalysisCache can keep doing this)
        //  - taking care of newly added files (maybe AnalysisCache can keep doing this too)

        final Map<String, BinaryInfo> classNodeIdByInternalName;
        final int numVertices;
        final List<@Nullable NodeIdSet> successors;
        final Map<Integer, Set<FileId>> filesByVxId;


        ClassQueryGraph(Map<String, BinaryInfo> classNodeIdByInternalName,
                        Map<Integer, Set<FileId>> filesByVxId,
                        int numVertices,
                        List<@Nullable NodeIdSet> successors) {
            this.classNodeIdByInternalName = classNodeIdByInternalName;
            this.numVertices = numVertices;
            this.successors = successors;
            this.filesByVxId = filesByVxId;
            assert successors.size() == numVertices;
        }

        /**
         * Replay all queries to the classloader from the previous run
         * with the current classpath. The hash of each result is compared
         * with the cached hash. If the hash is different, all vertices
         * reachable from the vertex of the query are marked as out-of-date,
         * and their queries are not replayed.
         *
         * @param resolver Resolver with the current classpath
         */
        public ClasspathCheckResult checkClasspathIsUpToDate(AsmSymbolResolver resolver) {
            BitSet changed = new BitSet(numVertices);
            BitSet visited = new BitSet(numVertices);
            Set<FileId> files = new HashSet<>();

            // Could we do some of this in parallel?
            for (Entry<String, BinaryInfo> entry : classNodeIdByInternalName.entrySet()) {
                BinaryInfo info = entry.getValue();
                if (visited.get(info.vertexId)) {
                    continue;
                }
                ClassStub sym = resolver.resolveClassFromBinaryName(entry.getKey(), ClasspathRequest.noOrigin());
                boolean isChanged = sym == null && info.hash != 0
                    || sym != null && sym.getAbiFingerprint() != info.hash;

                if (isChanged) {
                    // class has changed. Mark all the nodes it can reach as changed.
                    boolean abort = markChanged(info.vertexId, visited, changed, files);
                    if (abort) {
                        // a dependency changed that influences all files.
                        return new ClasspathCheckResult(Collections.emptySet(), true);
                    }
                }
            }
            return new ClasspathCheckResult(files, false);
        }

        public static class ClasspathCheckResult {
            private final Set<FileId> changedFiles;
            private final boolean aborted;

            ClasspathCheckResult(Set<FileId> changedFiles, boolean aborted) {
                this.changedFiles = changedFiles;
                this.aborted = aborted;
            }

            public boolean allFilesNeedToBeProcessedAgain() {
                return aborted;
            }

            public Set<FileId> getChangedFiles() {
                return changedFiles;
            }
        }

        private boolean markChanged(int id, BitSet visited, BitSet changed, Set<FileId> files) {
            visited.set(id);
            changed.set(id);
            Set<FileId> filesInThisVertex = filesByVxId.get(id);
            if (filesInThisVertex != null) {
                boolean added = files.addAll(filesInThisVertex);
                if (added && filesInThisVertex.contains(FileId.UNKNOWN)) {
                    // todo all files should be invalidated
                    return true;
                }
            }
            NodeIdSet successors = this.successors.get(id);
            if (successors != null) {
                for (int succ : successors.data) {
                    if (!changed.get(succ)) {
                        boolean abort = markChanged(succ, visited, changed, files);
                        if (abort) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        /** A set of node IDs */
        static class NodeIdSet {
            // always full.
            private final int[] data;

            NodeIdSet(int capacity) {
                data = new int[capacity];
            }

            void set(int idx, int id) {
                assert idx == 0 || id > data[idx - 1];
                data[idx] = id;
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

    public DotGraphDescription<?> asWriteableGraph() {
        GexfGraphDescription<Vertex<DependencyNode>> gexf = graph.asGexfGraph();
        gexf.setLabelFun(v -> v.getData().stream().map(it -> it.toString().replace('/', '.')).collect(Collectors.joining(", ")));
        gexf.recordAttribute("containsFile", "boolean", v -> Boolean.toString(v.getData().stream().anyMatch(it -> it instanceof SourceDependencyNode)));
        gexf.recordAttribute("nodeSize", "int", v -> Integer.toString(v.getData().size()));
        return gexf;
    }

    void finalizeGraph() {
        // note: these algorithms are not optimized enough for the size of graphs we may encounter.
        // It is likely that the transitive reduction especially is unnecessary.
        // We should use a more optimized implementation, maybe using parallel algorithms.
        // I tried with jgrapht, but it's even worse.
        //
        // Alternatively I think we should find a way to reduce the number of nodes and edges.
        // We can reduce the resolution of the graph. Let RG=(RV, RE) be a coarsening of G=(V,E).
        // RG still respects all dependencies of G if
        //      forall e=(s,t) in E, one of the following holds:
        //          1. exists re=(rs, rt) in RE where s in rs, t in rt.
        //          2. exists rv in RV where s in rv, t in rv.
        //
        // The degenerate case is RV has a single vertex that contains all vertices of V.
        // A coarsening in this sense changes the transition relation, but the new transition
        // relation is a superset of the original one.
        //
        // We want to find a "good coarsening". Coarsenings can be measured with two metrics:
        // - compression: the fewer edges and vertices, the better.
        // - minimality: the fewer extraneous edges exist, the better.
        //
        // A measure of minimality would be `card(->*_RG \ ->*_G)`, ie, the number of paths in the
        // coarsening that are not part of the original graph.
        //
        // A measure of compression would be just number of edges and vertices.
        //
        // The problem now becomes an optimization problem. We want to maximize compression and minimize
        // extra edges.
        //
        // Notice that the condensation of a graph is a perfectly minimal coarsening. But it might not be
        // compressed enough. We need to find heuristics to compress the graph more.
        //
        // Another kind of coarsening that might work is just:
        //  a node that has no successors may be merged with a sibling that has no successor.
        // Here we could introduce a special kind of vertex that condenses several vertices, but does not assume
        // that
        //
        //

        graph.mergeCycles();
        graph.compressGraphHeuristically();
    }

    abstract static class DependencyNode {
        private DependencyNode() {
        }

        abstract void serialize(ObjectOutputStream out) throws IOException;
    }

    static final class ClassDependencyNode extends DependencyNode {
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
            return binaryName.hashCode();
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
            return fileId.hashCode();
        }

        @Override
        public String toString() {
            return '@' + fileId.getFileName();
        }

        void serialize(ObjectOutputStream out) throws IOException {
            out.writeObject(fileId);
        }

        static SourceDependencyNode deserialize(ObjectInputStream in) throws IOException {
            try {
                FileId fileId = (FileId) in.readObject();
                return new SourceDependencyNode(fileId);
            } catch (ClassNotFoundException e) {
                throw new IOException(e);
            }
        }
    }


}
