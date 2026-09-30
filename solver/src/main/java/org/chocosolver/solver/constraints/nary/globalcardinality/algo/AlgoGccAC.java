/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.globalcardinality.algo;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.graphOperations.connectivity.StrongConnectivityFinder;
import org.chocosolver.util.objects.graphs.DirectedGraph;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.ISetIterator;
import org.chocosolver.util.objects.setDataStructures.SetFactory;
import org.chocosolver.util.objects.setDataStructures.SetType;

import java.util.Arrays;

/**
 * Arc-consistency algorithm for the global cardinality constraint (GCC), based on:
 * J.-C. Regin. "Generalized Arc Consistency for Global Cardinality Constraint." AAAI-96.
 * <p>
 * Finds a feasible and maximal flow over the dense range {@code [firstValue, firstValue +
 * minOcc.length - 1]} (every value in the range gets its own node, {@code [0, n]}-bounded when it
 * is not one of the restricted values), then removes every (variable, value) edge that crosses
 * two different strongly connected components of the residual graph.
 * <p>
 * The range must be dense (one node per value, not just the restricted ones) for the same reason
 * {@link org.chocosolver.solver.constraints.nary.globalcardinality.algo.AlgoGccBC} needs a dense
 * {@code minOcc}/{@code maxOcc}: a variable may have unrestricted values in its domain, and
 * whether it may safely use one of them is not always a local decision — it can depend on the
 * rest of the network.
 *
 * @author Charles Prud'homme
 */
public class AlgoGccAC implements GccFilter {

    private static final int UNMATCHED = -1;
    private static final int FROM_SOURCE = -2;

    private final Propagator<?> aCause;

    private IntVar[] vars;
    private int n;
    private int range;
    private int firstValue;

    // node numbering in the residual/SCC digraph: 0..n-1 vars, n..n+range-1 values (dense,
    // one per value of the range), n+range the pseudo source node shared by every capacity edge.
    private DirectedGraph digraph;
    private StrongConnectivityFinder sccFinder;
    private int[] nodeSCC;

    private int[] minOcc;
    private int[] maxOcc;
    private int[] matching; // var -> value index (0..range-1), transiently UNMATCHED mid-search
    private int[] flow;     // value index -> number of matched vars

    private ISet[] domVars; // value index -> vars whose domain currently contains it

    // BFS working memory for the augmenting-path search
    private int[] fifo;
    private boolean[] varVisited;
    private boolean[] valueVisited;
    private boolean srcVisited;
    private int[] predOfVar;    // var index -> value index that reached it
    private int[] predOfValue;  // value index -> var index that reached it, or FROM_SOURCE
    private int predOfSrc;      // value index that reached the source pseudo node
    private boolean compatibleFlow;

    public AlgoGccAC(Propagator<?> cause) {
        this.aCause = cause;
    }

    @Override
    public void reset(IntVar[] variables) {
        this.vars = variables;
        this.n = vars.length;
        this.matching = new int[n];
        Arrays.fill(matching, UNMATCHED);
        this.varVisited = new boolean[n];
        this.predOfVar = new int[n];
    }

    //***********************************************************************************
    // PROPAGATION
    //***********************************************************************************

    /**
     * Enforces arc-consistency on {@code vars} given dense minimum/maximum occurrence bounds over
     * the contiguous range {@code [firstValue, firstValue + minOcc.length - 1]}.
     *
     * @param minOcc     minimum number of occurrences, per value, dense on the range
     * @param maxOcc     maximum number of occurrences, per value, dense on the range
     * @param firstValue first value of the range covered by {@code minOcc}/{@code maxOcc}
     * @return {@code true} iff at least one domain update has been done
     */
    @Override
    public boolean filter(int[] minOcc, int[] maxOcc, int firstValue) throws ContradictionException {
        this.minOcc = minOcc;
        this.maxOcc = maxOcc;
        this.firstValue = firstValue;
        int newRange = minOcc.length;
        if (domVars == null || range != newRange) {
            range = newRange;
            flow = new int[range];
            domVars = new ISet[range];
            for (int j = 0; j < range; j++) {
                domVars[j] = SetFactory.makeBitSet(0);
            }
            fifo = new int[n + range + 1];
            valueVisited = new boolean[range];
            predOfValue = new int[range];
            digraph = new DirectedGraph(n + range + 1, SetType.BITSET, false);
            sccFinder = new StrongConnectivityFinder(digraph);
        }
        prepare();
        computeFeasibleMaximalFlow();
        buildResidualDigraph();
        sccFinder.findAllSCC();
        nodeSCC = sccFinder.getNodesSCC();
        return pruneDomains();
    }

    /**
     * Repairs {@code matching}/{@code flow} against the current domains and capacities, and
     * rebuilds the {@code domVars} adjacency used by the augmenting-path search.
     */
    private void prepare() {
        for (int j = 0; j < range; j++) {
            domVars[j].clear();
        }
        for (int i = 0; i < n; i++) {
            IntVar v = vars[i];
            int ub = v.getUB();
            for (int k = v.getLB(); k <= ub; k = v.nextValue(k)) {
                domVars[k - firstValue].add(i);
            }
            int j = matching[i];
            if (j != UNMATCHED && !v.contains(firstValue + j)) {
                matching[i] = UNMATCHED;
            }
        }
        Arrays.fill(flow, 0);
        for (int i = 0; i < n; i++) {
            if (matching[i] != UNMATCHED) {
                flow[matching[i]]++;
            }
        }
        // a cardinality upper bound may have shrunk below the (previously valid) flow: drop
        // enough arbitrary matches to fit back under the new capacity.
        for (int j = 0; j < range; j++) {
            while (flow[j] > maxOcc[j]) {
                for (int i = 0; i < n; i++) {
                    if (matching[i] == j) {
                        matching[i] = UNMATCHED;
                        flow[j]--;
                        break;
                    }
                }
            }
        }
        // a cardinality lower bound may have grown since the warm-started matching was built: any
        // deficit left over here is handled directly by the augmenting-path search below, which
        // knows how to pull a variable away from a value that has slack (see findAugmentingPath).
    }

    //***********************************************************************************
    // FEASIBLE + MAXIMAL FLOW (Ford-Fulkerson with lower bounds on value nodes)
    //***********************************************************************************

    private void computeFeasibleMaximalFlow() throws ContradictionException {
        while (true) {
            int freeVar = findAugmentingPath();
            if (freeVar == UNMATCHED) {
                if (!compatibleFlow) {
                    aCause.fails(); // some value cannot reach its minimum: infeasible
                }
                // the range is dense (covers every variable's whole domain), so every variable
                // must end up matched to some value; one left over means a real Hall violation.
                for (int i = 0; i < n; i++) {
                    if (matching[i] == UNMATCHED) {
                        aCause.fails();
                    }
                }
                return; // compatibleFlow phase found nothing more: maximal flow reached
            }
            augment(freeVar);
        }
    }

    /**
     * Breadth-first search for an augmenting path. First tries to reach a variable that can fix
     * a value under its minimum ({@code compatibleFlow = false}); once no value is under its
     * minimum any more, tries to grow the flow further, up to the maxima
     * ({@code compatibleFlow = true}).
     *
     * @return the index of a variable that can be newly matched, or {@link #UNMATCHED} if none
     * is reachable
     */
    private int findAugmentingPath() {
        Arrays.fill(varVisited, false);
        Arrays.fill(valueVisited, false);
        srcVisited = false;
        int head = 0;
        int tail = 0;

        boolean anyDeficit = false;
        for (int j = 0; j < range; j++) {
            if (flow[j] < minOcc[j]) {
                fifo[tail++] = n + j;
                valueVisited[j] = true;
                anyDeficit = true;
            }
        }
        compatibleFlow = !anyDeficit;
        if (compatibleFlow) {
            for (int j = 0; j < range; j++) {
                if (flow[j] < maxOcc[j]) {
                    fifo[tail++] = n + j;
                    valueVisited[j] = true;
                }
            }
        }

        int src = n + range;
        while (head < tail) {
            int x = fifo[head++];
            if (x < n) { // var node, always already matched when reached this way
                int j = matching[x];
                // warm start: every variable may already be matched, leaving no genuinely free
                // one for the search to end on. Pulling x away from a value that has slack is
                // always safe -- j keeps at least its minimum -- so it is just as good an
                // endpoint: augment() will walk the same predecessor chain back to the deficient
                // value either way (see BEST_PRACTICES.md).
                if (!compatibleFlow && flow[j] > minOcc[j]) {
                    flow[j]--;
                    return x;
                }
                if (!valueVisited[j]) {
                    valueVisited[j] = true;
                    predOfValue[j] = x;
                    fifo[tail++] = n + j;
                }
            } else if (x < src) { // value node
                int j = x - n;
                ISetIterator it = domVars[j].iterator();
                while (it.hasNext()) {
                    int i = it.nextInt();
                    if (matching[i] != j && !varVisited[i]) {
                        varVisited[i] = true;
                        predOfVar[i] = j;
                        if (matching[i] == UNMATCHED) {
                            return i;
                        }
                        fifo[tail++] = i;
                    }
                }
                if (!compatibleFlow && flow[j] > minOcc[j] && !srcVisited) {
                    srcVisited = true;
                    predOfSrc = j;
                    fifo[tail++] = src;
                }
            } else if (!compatibleFlow) { // source pseudo node
                for (int j = 0; j < range; j++) {
                    if (flow[j] < maxOcc[j] && !valueVisited[j]) {
                        valueVisited[j] = true;
                        predOfValue[j] = FROM_SOURCE;
                        fifo[tail++] = n + j;
                    }
                }
            }
        }
        return UNMATCHED;
    }

    /**
     * Flips the matching along the augmenting path ending at {@code freeVar}, growing the flow
     * of the value at the root of the path by one unit.
     */
    private void augment(int freeVar) {
        int varNode = freeVar;
        int valIdx = predOfVar[varNode];
        if (compatibleFlow) {
            while (flow[valIdx] == maxOcc[valIdx]) {
                matching[varNode] = valIdx;
                varNode = predOfValue[valIdx];
                valIdx = predOfVar[varNode];
            }
        } else {
            while (flow[valIdx] >= minOcc[valIdx]) {
                matching[varNode] = valIdx;
                int pred = predOfValue[valIdx];
                if (pred == FROM_SOURCE) {
                    flow[valIdx]++;
                    int donor = predOfSrc;
                    flow[donor]--;
                    varNode = predOfValue[donor];
                } else {
                    varNode = pred;
                }
                valIdx = predOfVar[varNode];
            }
        }
        matching[varNode] = valIdx;
        flow[valIdx]++;
    }

    //***********************************************************************************
    // PRUNING (strongly connected components of the residual graph)
    //***********************************************************************************

    private void buildResidualDigraph() {
        int nbNodes = n + range + 1;
        for (int idx = 0; idx < nbNodes; idx++) {
            digraph.getSuccessorsOf(idx).clear();
            digraph.getPredecessorsOf(idx).clear();
        }
        for (int i = 0; i < n; i++) {
            IntVar v = vars[i];
            int ub = v.getUB();
            for (int k = v.getLB(); k <= ub; k = v.nextValue(k)) {
                int j = k - firstValue;
                if (matching[i] == j) {
                    digraph.addEdge(n + j, i);
                } else {
                    digraph.addEdge(i, n + j);
                }
            }
        }
        int src = n + range;
        for (int j = 0; j < range; j++) {
            if (flow[j] < maxOcc[j]) {
                digraph.addEdge(n + j, src);
            }
            if (flow[j] > minOcc[j]) {
                digraph.addEdge(src, n + j);
            }
        }
    }

    private boolean pruneDomains() throws ContradictionException {
        boolean filter = false;
        for (int i = 0; i < n; i++) {
            IntVar v = vars[i];
            int ub = v.getUB();
            for (int k = v.getLB(); k <= ub; k = v.nextValue(k)) {
                int j = k - firstValue;
                // the matched pair is part of the feasible flow: never touch it here.
                if (matching[i] != j && nodeSCC[i] != nodeSCC[n + j]) {
                    filter |= v.removeValue(k, aCause);
                }
            }
        }
        return filter;
    }
}
