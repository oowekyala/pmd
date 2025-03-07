/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.internal;

import static java.lang.Math.min;
import static net.sourceforge.pmd.util.CollectionUtil.union;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import net.sourceforge.pmd.util.GraphUtil;
import net.sourceforge.pmd.util.GraphUtil.DotColor;
import net.sourceforge.pmd.util.GraphUtil.DotGraphDescription;
import net.sourceforge.pmd.util.GraphUtil.GexfGraphDescription;
import net.sourceforge.pmd.util.IteratorUtil;

/**
 * A graph to walk over ivar dependencies in an efficient way.
 * This is not a general purpose implementation, there's no cleanup
 * of the vertices whatsoever, meaning each algo ({@link #mergeCycles()}
 * and {@link #topologicalSort()}) can only be done once reliably.
 */
public class TarjanGraph<T> {

    /** Undefined index for Tarjan's algo. */
    protected static final int UNDEFINED = -1;

    private final Set<Vertex<T>> vertices = new LinkedHashSet<>();
    // direct successors
    private Map<Vertex<T>, Set<Vertex<T>>> successors = new HashMap<>();

    public Vertex<T> addLeaf(T data) {
        Vertex<T> v = makeVertex(Collections.singleton(data));
        vertices.add(v);
        return v;
    }

    protected Vertex<T> addLeaf(Set<T> data) {
        Vertex<T> v = makeVertex(data);
        vertices.add(v);
        return v;
    }

    protected Vertex<T> makeVertex(Set<T> data) {
        return new Vertex<>(this, data);
    }

    /**
     * Implicitly add both nodes to the graph and record a directed
     * edge between the first and the second.
     */
    public final void addEdge(Vertex<T> start, Vertex<T> end) {
        Objects.requireNonNull(end);
        Objects.requireNonNull(start);

        vertices.add(start);
        vertices.add(end);
        if (start == end) { // NOPMD CompareObjectsWithEquals
            // no self loop allowed (for tarjan), and besides an
            // inference variable depending on itself is trivial
            return;
        }
        successors.computeIfAbsent(start, k -> new LinkedHashSet<>()).add(end);
    }


    protected void cloneInto(TarjanGraph<T> graph) {
        Map<Vertex<T>, Vertex<T>> newVertices = new HashMap<>();
        for (Vertex<T> v : vertices) {
            Vertex<T> vertex = graph.addLeaf(v.getData());
            newVertices.put(v, vertex);
        }

        for (Entry<Vertex<T>, Set<Vertex<T>>> entry : successors.entrySet()) {
            Vertex<T> vertex = newVertices.get(entry.getKey());
            Set<Vertex<T>> succs = new HashSet<>(entry.getValue().size());
            for (Vertex<T> succ : entry.getValue()) {
                succs.add(newVertices.get(succ));
            }
            graph.successors.put(vertex, succs);
        }
    }

    // test only
    public Set<Vertex<T>> successorsOf(Vertex<T> node) {
        return successors.getOrDefault(node, Collections.emptySet());
    }

    public Set<Vertex<T>> getVertices() {
        return vertices;
    }

    public Iterator<Set<T>> topologicalSort() {
        return IteratorUtil.map(toposortVertices().iterator(), Vertex::getData);
    }

    /**
     * Returns a list in which the vertices of this graph are sorted
     * in the following way:
     *
     * if there exists an edge u -> v, then u comes AFTER v in the list.
     *
     * <p>Note that this assumes that the graph is acyclic and will
     * not terminate if it is.
     */
    public List<Vertex<T>> toposortVertices() {
        List<Vertex<T>> sorted = new ArrayList<>(vertices.size());
        for (Vertex<T> n : vertices) {
            toposort(n, sorted);
        }
        return sorted;
    }

    private void toposort(Vertex<T> v, List<Vertex<T>> sorted) {
        if (v.mark) {
            return;
        }

        for (Vertex<T> w : successorsOf(v)) {
            toposort(w, sorted);
        }

        v.mark = true;
        sorted.add(v);
    }

    /**
     * Merge strongly connected components into a single node each.
     * This turns the graph into a DAG. This modifies the graph in
     * place, no cleanup of the vertices is performed.
     */
    public void mergeCycles() {
        // https://en.wikipedia.org/wiki/Tarjan's_strongly_connected_components_algorithm

        TarjanState<T> state = new TarjanState<>();
        for (Vertex<T> vertex : vertices) {
            if (vertex.index == UNDEFINED) {
                strongConnect(state, vertex);
            }
        }

        batchMerge(state.toBeMerged);
    }

    private void strongConnect(TarjanState<T> state, Vertex<T> v) {
        v.index = state.index;
        v.lowLink = state.index;
        state.index++;
        state.stack.push(v);
        v.onStack = true;

        for (Vertex<T> w : successorsOf(v)) {
            if (w.index == UNDEFINED) {
                // Successor has not yet been visited; recurse on it
                strongConnect(state, w);
                v.lowLink = min(w.lowLink, v.lowLink);
            } else if (w.onStack) {
                // Successor w is in stack S and hence in the current SCC
                // If w is not on stack, then (v, w) is a cross-edge in the DFS tree and must be ignored
                // Note: The next line may look odd - but is correct.
                // It says w.index not w.lowlink; that is deliberate and from the original paper
                v.lowLink = min(v.lowLink, w.index);
            }
        }

        // If v is a root node, pop the stack and generate an SCC
        if (v.lowLink == v.index) {
            Set<Vertex<T>> toMerge = new HashSet<>();
            toMerge.add(v);
            Vertex<T> w;
            do {
                w = state.stack.pop();
                w.onStack = false;
                // merge w into v
                toMerge.add(w);
            } while (w != v); // NOPMD CompareObjectsWithEquals

            state.toBeMerged.add(toMerge);
        }
    }

    /**
     * Compute transitive reduction of this graph. This MUST be run
     * after {@link #mergeCycles()} has been run, as it requires an
     * acyclic graph.
     */
    public void transitiveReductionOnDag() {
        List<Vertex<T>> toposort = toposortVertices();
        Collections.reverse(toposort);
        Map<Vertex<T>, Set<Vertex<T>>> newSuccessors = new LinkedHashMap<>();

        // See algorithm from https://github.com/jafingerhut/cljol/blob/master/doc/transitive-reduction-notes.md
        for (int i = 0; i < toposort.size(); i++) {
            Vertex<T> vi = toposort.get(i);

            for (int j = 0; j < i; j++) {
                toposort.get(j).mark = false;
            }

            for (int j = i - 1; j >= 0; j--) {
                Vertex<T> vj = toposort.get(j);

                if (successorsOf(vj).contains(vi)) {
                    if (!vj.mark) {
                        vj.mark = true;
                        newSuccessors.compute(vj, (vj2, vjSuccs) -> {
                            if (vjSuccs == null) {
                                vjSuccs = new HashSet<>();
                            }
                            vjSuccs.add(vi);
                            return vjSuccs;
                        });
                    }
                }
                // If T[j] can reach T[i], then any node with an edge into
                // T[j] can also reach T[i], so mark them, too.
                if (vj.mark) {

                    for (int k = 0; k < j; k++) {
                        Vertex<T> vk = toposort.get(k);
                        if (newSuccessors.computeIfAbsent(vk, ignored -> new LinkedHashSet<>()).contains(vj)) {
                            vk.mark = true;
                        }
                    }
                }
            }
        }

        this.successors = newSuccessors;
    }

    void onAbsorb(Vertex<T> vertex, Vertex<T> toMerge, boolean isBatchMerge) {
        if (!isBatchMerge) {
            Set<Vertex<T>> succ = union(successorsOf(vertex), successorsOf(toMerge));
            succ.remove(toMerge);
            succ.remove(vertex);
            successors.put(vertex, succ);
            successors.remove(toMerge);
            vertices.remove(toMerge);
            successors.values().forEach(it -> it.remove(toMerge));
        }
    }

    // Merge many vertices together in one pass
    protected void batchMerge(Collection<? extends Set<? extends Vertex<T>>> equivClasses) {
        Map<Vertex<T>, Vertex<T>> remapping = new HashMap<>();

        for (Set<? extends Vertex<T>> equivClass : equivClasses) {
            if (equivClass.size() < 2) {
                continue;
            }
            Iterator<? extends Vertex<T>> iter = equivClass.iterator();
            Vertex<T> first = iter.next();
            Set<Vertex<T>> newSuccessors = new LinkedHashSet<>(successorsOf(first));
            while (iter.hasNext()) {
                Vertex<T> next = iter.next();
                remapping.put(next, first);
                newSuccessors.addAll(successorsOf(next));
                successors.remove(next);
                vertices.remove(next);
                first.absorb(next, true);
            }
            newSuccessors.remove(first);
            successors.put(first, newSuccessors);
        }
        // finally remap all vertices
        successors.values().forEach(it -> {
            for (Vertex<T> v : new ArrayList<>(it)) {
                Vertex<T> remapped = remapping.get(v);
                if (remapped != null) {
                    it.remove(v);
                    it.add(remapped);
                }
            }
        });
    }

    @Override
    public String toString() {
        return GraphUtil.toDot(asDotGraph());
    }

    public DotGraphDescription<?> asDotGraph() {
        return new DotGraphDescription<>(
            vertices,
            this::successorsOf,
            v -> DotColor.BLACK,
            v -> v.data.toString()
        );
    }

    public GexfGraphDescription<Vertex<T>> asGexfGraph() {
        return new GexfGraphDescription<>(
            vertices,
            this::successorsOf,
            v -> DotColor.BLACK,
            v -> v.data.toString()
        );
    }

    public void setEdges(Vertex<T> fromNode, Set<Vertex<T>> successors) {
        this.successors.put(fromNode, successors);
    }

    private static class TarjanState<T> {

        int index;
        final Deque<Vertex<T>> stack = new ArrayDeque<>();
        final List<Set<Vertex<T>>> toBeMerged = new ArrayList<>();

    }

    public static class Vertex<T> {

        private final TarjanGraph<T> owner;
        private final Set<T> data;
        // Tarjan state
        protected int index = UNDEFINED;
        private int lowLink = UNDEFINED;
        private boolean onStack = false;
        // Toposort state
        private boolean mark;

        protected Vertex(TarjanGraph<T> owner, Set<T> data) {
            this.owner = owner;
            this.data = new LinkedHashSet<>(data);
        }

        public Set<T> getData() {
            return data;
        }

        /** Absorbs the given node into this node. */
        private void absorb(Vertex<T> toMerge, boolean isBatchMerge) {
            if (this == toMerge) { // NOPMD CompareObjectsWithEquals
                return;
            }
            this.data.addAll(toMerge.data);
            owner.onAbsorb(this, toMerge, isBatchMerge);
        }

        @Override
        public String toString() {
            return data.toString();
        }
    }


    /** Maintains uniqueness of nodes wrt data. */
    public static class UniqueGraph<T> extends TarjanGraph<T> {

        private final Map<T, Vertex<T>> vertexMap = new HashMap<>();

        public UniqueGraph() {
        }

        @Override
        public Vertex<T> addLeaf(T data) {
            return vertexMap.computeIfAbsent(data, super::addLeaf);
        }

        @Override
        protected Vertex<T> addLeaf(Set<T> data) {
            // Note that this version will not fetch an existing leaf.
            // This is because the items within data may disagree about
            // what vertex is associated to the data. For this reason this
            // routine requires that none of the data items be associated
            // yet.
            assert data.stream().noneMatch(vertexMap::containsKey)
                : "Duplicate node added " + data;

            Vertex<T> vertex = super.addLeaf(data);
            for (T v : data) {
                vertexMap.put(v, vertex);
            }
            return vertex;
        }

        @Override
        void onAbsorb(Vertex<T> vertex, Vertex<T> toMerge, boolean isBatchMerge) {
            super.onAbsorb(vertex, toMerge, isBatchMerge);
            for (T ivar : toMerge.getData()) {
                vertexMap.put(ivar, vertex);
            }
        }
    }
}
