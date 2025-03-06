/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.internal;

import static java.lang.Math.min;
import static net.sourceforge.pmd.util.CollectionUtil.union;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import net.sourceforge.pmd.util.GraphUtil;
import net.sourceforge.pmd.util.GraphUtil.DotColor;
import net.sourceforge.pmd.util.GraphUtil.DotGraphDescription;
import net.sourceforge.pmd.util.IteratorUtil;

/**
 * A graph to walk over ivar dependencies in an efficient way.
 * This is not a general purpose implementation, there's no cleanup
 * of the vertices whatsoever, meaning each algo ({@link #mergeCycles()}
 * and {@link #topologicalSort()}) can only be done once reliably.
 */
public class TarjanGraph<T> {

    /** Undefined index for Tarjan's algo. */
    private static final int UNDEFINED = -1;

    private final Set<Vertex<T>> vertices = new LinkedHashSet<>();
    // direct successors
    private Map<Vertex<T>, Set<Vertex<T>>> successors = new HashMap<>();

    public Vertex<T> addLeaf(T data) {
        Vertex<T> v = new Vertex<>(this, Collections.singleton(data));
        vertices.add(v);
        return v;
    }

    /**
     * Implicitly add both nodes to the graph and record a directed
     * edge between the first and the second.
     */
    public void addEdge(Vertex<T> start, Vertex<T> end) {
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

    // test only
    Set<Vertex<T>> successorsOf(Vertex<T> node) {
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
        for (Vertex<T> vertex : new ArrayList<>(vertices)) {
            if (vertex.index == UNDEFINED) {
                strongConnect(state, vertex);
            }
        }
    }

    private void strongConnect(TarjanState<T> state, Vertex<T> v) {
        v.index = state.index;
        v.lowLink = state.index;
        state.index++;
        state.stack.push(v);
        v.onStack = true;

        for (Vertex<T> w : new ArrayList<>(successorsOf(v))) {
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
            Vertex<T> w;
            do {
                w = state.stack.pop();
                w.onStack = false;
                // merge w into v
                v.absorb(w);
            } while (w != v); // NOPMD CompareObjectsWithEquals
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

    protected void onAbsorb(Vertex<T> vertex, Vertex<T> toMerge) {
        Set<Vertex<T>> succ = union(successorsOf(vertex), successorsOf(toMerge));
        succ.remove(toMerge);
        succ.remove(vertex);
        successors.put(vertex, succ);
        successors.remove(toMerge);
        vertices.remove(toMerge);
        successors.values().forEach(it -> it.remove(toMerge));
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



    private static final class TarjanState<T> {

        int index;
        Deque<Vertex<T>> stack = new ArrayDeque<>();

    }

    public static final class Vertex<T> {

        private final TarjanGraph<T> owner;
        private final Set<T> data;
        // Tarjan state
        private int index = UNDEFINED;
        private int lowLink = UNDEFINED;
        private boolean onStack = false;
        // Toposort state
        private boolean mark;

        private Vertex(TarjanGraph<T> owner, Set<T> data) {
            this.owner = owner;
            this.data = new LinkedHashSet<>(data);
        }

        public Set<T> getData() {
            return data;
        }

        /** Absorbs the given node into this node. */
        private void absorb(Vertex<T> toMerge) {
            if (this == toMerge) { // NOPMD CompareObjectsWithEquals
                return;
            }
            this.data.addAll(toMerge.data);
            owner.onAbsorb(this, toMerge);
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
        protected void onAbsorb(Vertex<T> vertex, Vertex<T> toMerge) {
            super.onAbsorb(vertex, toMerge);
            for (T ivar : toMerge.getData()) {
                vertexMap.put(ivar, vertex);
            }
        }
    }
}
