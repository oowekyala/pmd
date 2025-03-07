package net.sourceforge.pmd.lang.java.symbols.internal.asm;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.pcollections.HashTreePSet;
import org.pcollections.PSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sourceforge.pmd.lang.document.FileId;
import net.sourceforge.pmd.lang.java.internal.TarjanGraph;
import net.sourceforge.pmd.lang.java.internal.TarjanGraph.UniqueGraph;
import net.sourceforge.pmd.lang.java.internal.TarjanGraph.Vertex;
import net.sourceforge.pmd.util.CollectionUtil;
import net.sourceforge.pmd.util.GraphUtil.DotGraphDescription;
import net.sourceforge.pmd.util.GraphUtil.GexfGraphDescription;

/**
 * This is the data structure that is written to disk. It has info about the
 * classes and source files of the analysis.
 */
public final class ClassDependencyGraph {
    // todo some things are missing:
    //  - initializing the ClasspathDependencyTracker
    //  - taking care of self classpath (maybe AnalysisCache can keep doing this)
    //  - taking care of newly added files (maybe AnalysisCache can keep doing this too)
    // todo test
    //  - consider the unknown file

    private static final Logger LOG = LoggerFactory.getLogger(ClassDependencyGraph.class);

    // This graph is inverted. There is an edge U -> V if V depends on U.
    private final CompressibleGraph graph;

    ClassDependencyGraph() {
        graph = new CompressibleGraph();
    }

    ClassDependencyGraph(ClassDependencyGraph toCopy) {
        graph = new CompressibleGraph();
        toCopy.graph.cloneInto(graph);
    }

    Vertex<DependencyItem> addSourceLeaf(FileId fileId) {
        return graph.addLeaf(new SourceItem(fileId));
    }

    Vertex<DependencyItem> addClassLeaf(String binaryName, long hash) {
        return graph.addLeaf(new ClassItem(binaryName, hash));
    }

    void recordDependency(Vertex<DependencyItem> from, Vertex<DependencyItem> to) {
        // notice the inversion
        graph.addEdge(to, from);
    }

    /**
     * This is a variant of the {@link UniqueGraph} that implements a custom
     * graph compression (pruning) routine.
     */
    private static class CompressibleGraph extends UniqueGraph<DependencyItem> {

        protected void cloneInto(CompressibleGraph graph) {
            super.cloneInto(graph);
        }

        @Override
        protected Vertex<DependencyItem> makeVertex(Set<DependencyItem> data) {
            return new DependencyVertex(this, data);
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        private Set<DependencyVertex> castVertices(Set<Vertex<DependencyItem>> vertices) {
            return (Set) vertices;
        }


        public GexfGraphDescription<Vertex<DependencyItem>> asGexfGraph() {
            GexfGraphDescription<Vertex<DependencyItem>> gexf = super.asGexfGraph();
            gexf.setLabelFun(v -> v.getData().stream().map(it -> it.toString().replace('/', '.')).collect(Collectors.joining(", ")));
            gexf.recordAttribute("containsFile", "boolean", v -> Boolean.toString(v.getData().stream().anyMatch(it -> it instanceof SourceItem)));
            gexf.recordAttribute("nodeSize", "int", v -> Integer.toString(v.getData().size()));
            return gexf;
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
                int numVertices = vertices.size() + 1;
                int percentPruned = 100 * numPruned / numVertices;
                LOG.trace("Pruned {} vertices from dependency graph ({}%)", numPruned, percentPruned);
            }

            batchMerge(state.toBeMerged.values());

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
        ClasspathCheckResult checkClasspathIsUpToDate(AsmSymbolResolver resolver) {
            // Could we do some of this in parallel?

            for (DependencyVertex vertex : castVertices(getVertices())) {
                vertex.reset();
            }

            Set<FileId> files = new HashSet<>();

            nextVertex:
            for (DependencyVertex vertex : castVertices(getVertices())) {
                if (vertex.hasBeenVisited()) {
                    continue;
                }
                for (DependencyItem item : vertex.getData()) {
                    if (item instanceof ClassItem) {
                        ClassItem classItem = (ClassItem) item;
                        long checksum = resolver.getStubChecksumWithClassloaderHit(classItem.internalName);
                        boolean isChanged = classItem.checksum != checksum;

                        if (isChanged) {
                            // class has changed. Mark all the nodes it can reach as changed.
                            boolean abort = markChanged(vertex, files);
                            if (abort) {
                                // a dependency changed that influences all files.
                                return new ClasspathCheckResult(Collections.emptySet(), true);
                            }
                            // no need to process other items in this vertex, it has been marked as changed
                            continue nextVertex;
                        }
                    }
                }
                vertex.markUnchanged();
            }

            return new ClasspathCheckResult(files, false);
        }

        private boolean markChanged(DependencyVertex vertex, Set<FileId> files) {
            vertex.markChanged();

            for (DependencyItem item : vertex.getData()) {
                if (item instanceof SourceItem) {
                    SourceItem sourceItem = (SourceItem) item;
                    if (sourceItem.fileId == FileId.UNKNOWN) {
                        return true;
                    }
                    files.add(sourceItem.fileId);
                }
            }

            for (DependencyVertex succ : castVertices(successorsOf(vertex))) {
                if (!vertex.isChanged()) {
                    boolean abort = markChanged(succ, files);
                    if (abort) {
                        return true;
                    }
                }
            }
            return false;
        }

        protected Vertex<DependencyItem> addLeaf(Set<DependencyItem> nodeValue) {
            return super.addLeaf(nodeValue);
        }

        private static class CompressionState {
            private final Map<PSet<SourceItem>, Set<DependencyVertex>> toBeMerged = new HashMap<>();
        }

        private void compressGraphRec(DependencyVertex v, CompressionState state) {
            v.downstream = HashTreePSet.empty();

            for (DependencyItem node : v.getData()) {
                if (node instanceof SourceItem) {
                    v.downstream = v.downstream.plus((SourceItem) node);
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

        private static final class DependencyVertex extends Vertex<DependencyItem> {
            private PSet<SourceItem> downstream;

            DependencyVertex(TarjanGraph<DependencyItem> owner, Set<DependencyItem> data) {
                super(owner, data);
            }

            private static final int NOT_VISITED = 0;
            private static final int CHANGED = 1;
            private static final int UNCHANGED = 2;

            void reset() {
                index = NOT_VISITED;
            }

            boolean hasBeenVisited() {
                return index != NOT_VISITED;
            }

            void markChanged() {
                index = CHANGED;
            }

            void markUnchanged() {
                index = UNCHANGED;
            }

            boolean isChanged() {
                return index == CHANGED;
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
     * Deserialize a structure written by {@link #serialize(OutputStream)}.
     *
     * @param is Input stream
     *
     * @return A structure that can be used to test the classpath for freshness
     *
     * @throws IOException if reading fails
     */
    public static ClassDependencyGraph deserialize(InputStream is) throws IOException {
        try (GZIPInputStream gzip = new GZIPInputStream(is);
             ObjectInputStream in = new ObjectInputStream(gzip)) {
            return deserialize(in);
        }
    }

    private void serialize(ObjectOutputStream out) throws IOException {
        Map<Vertex<DependencyItem>, Integer> vertexToId = new HashMap<>();
        List<Vertex<DependencyItem>> vertices = new ArrayList<>(graph.getVertices());

        // write out all nodes
        out.writeInt(vertices.size());
        for (int i = 0; i < vertices.size(); i++) {
            Vertex<DependencyItem> vertex = vertices.get(i);
            vertexToId.put(vertex, i);
            out.writeInt(vertex.getData().size());
            for (DependencyItem node : vertex.getData()) {
                boolean isClassNode = node instanceof ClassItem;
                out.writeBoolean(isClassNode);
                node.serialize(out);
            }
        }
        // then write out edges
        List<Integer> successors = new ArrayList<>();
        for (Vertex<DependencyItem> vertex : vertices) {
            for (Vertex<DependencyItem> succ : graph.successorsOf(vertex)) {
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

    ClasspathCheckResult checkClasspathIsUpToDate(AsmSymbolResolver resolver) {
        return graph.checkClasspathIsUpToDate(resolver);
    }

    private static ClassDependencyGraph deserialize(ObjectInputStream in) throws IOException {
        ClassDependencyGraph result = new ClassDependencyGraph();

        List<Vertex<DependencyItem>> vertices = new ArrayList<>();

        final int numVertices = in.readInt();
        for (int i = 0; i < numVertices; i++) {
            int nodeSize = in.readInt();
            Set<DependencyItem> nodeValue = new HashSet<>();
            for (int j = 0; j < nodeSize; j++) {
                boolean isClassNode = in.readBoolean();
                DependencyItem item = isClassNode ? ClassItem.deserialize(in) : SourceItem.deserialize(in);
                nodeValue.add(item);
            }
            Vertex<DependencyItem> vertex = result.graph.addLeaf(nodeValue);
            vertices.add(vertex);
        }

        for (Vertex<DependencyItem> vertex : vertices) {
            int numSuccessors = in.readInt();
            for (int j = 0; j < numSuccessors; j++) {
                int succId = in.readInt();
                Vertex<DependencyItem> succVertex = vertices.get(succId);
                result.graph.addEdge(vertex, succVertex);
            }
        }

        return result;
    }

    public DotGraphDescription<?> debugGraph() {
        return graph.asGexfGraph();
    }

    void compressGraphForQueryPhase() {
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

    /**
     * One of the values of the {@link CompressibleGraph}. Vertices
     * of the graph contain a set of those.
     */
    abstract static class DependencyItem {
        private DependencyItem() {
        }

        abstract void serialize(ObjectOutputStream out) throws IOException;
    }

    /** Represents a class file in the dependency graph. */
    private static final class ClassItem extends DependencyItem {
        private final String internalName;
        private final long checksum;

        ClassItem(String internalName, long checksum) {
            this.internalName = internalName;
            this.checksum = checksum;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            ClassItem that = (ClassItem) o;
            return Objects.equals(internalName, that.internalName);
        }

        @Override
        public int hashCode() {
            return internalName.hashCode();
        }

        @Override
        public String toString() {
            return internalName;
        }

        void serialize(ObjectOutputStream out) throws IOException {
            out.writeUTF(internalName);
            out.writeLong(checksum);
        }

        static ClassItem deserialize(ObjectInputStream in) throws IOException {
            String binName = in.readUTF();
            long hash = in.readLong();
            return new ClassItem(binName, hash);
        }
    }

    /** Represents a source file in the dependency graph. */
    private static final class SourceItem extends DependencyItem {
        private final FileId fileId;

        SourceItem(FileId fileId) {
            this.fileId = fileId;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            SourceItem that = (SourceItem) o;
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

        static SourceItem deserialize(ObjectInputStream in) throws IOException {
            try {
                FileId fileId = (FileId) in.readObject();
                return new SourceItem(fileId);
            } catch (ClassNotFoundException e) {
                throw new IOException(e);
            }
        }
    }


    public static class ClasspathCheckResult {
        private final Set<FileId> changedFiles;
        private final boolean aborted;

        ClasspathCheckResult(Set<FileId> changedFiles, boolean aborted) {
            this.changedFiles = changedFiles;
            this.aborted = aborted;
        }

        public static ClasspathCheckResult noCacheFile() {
           return new ClasspathCheckResult(Collections.emptySet(), true);
        }

        public boolean allFilesNeedToBeProcessedAgain() {
            return aborted;
        }

        public Set<FileId> getChangedFiles() {
            return changedFiles;
        }
    }
}
