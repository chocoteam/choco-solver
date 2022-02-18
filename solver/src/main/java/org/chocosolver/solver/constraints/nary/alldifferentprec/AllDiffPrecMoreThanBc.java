/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
/*
@author Arthur Godet <arth.godet@gmail.com>
@since 05/02/2021
*/

package org.chocosolver.solver.constraints.nary.alldifferentprec;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntIntHashMap;
import org.chocosolver.solver.ICause;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.objects.graphs.DirectedGraph;
import org.chocosolver.util.objects.setDataStructures.ISetIterator;
import org.chocosolver.util.objects.setDataStructures.SetType;
import org.chocosolver.util.objects.setDataStructures.bitset.Set_BitSet;

import java.util.Arrays;
import java.util.BitSet;

/**
 * Filtering algorithm for the AllDiffPrec constraint introduced in the following thesis :
 * TODO: add the thesis citation when it is fixed (and/or the CP paper if accepted)
 *
 * @author Arthur Godet <arth.godet@gmail.com>
 */
public class AllDiffPrecMoreThanBc extends FilterAllDiffPrec {
    private final boolean rcFiltering;

    // for augmenting matching (BFS)
    private final int[] next;
    private final BitSet in;
    private final int[] fifo;
    private final int n, m;
    private final DirectedGraph digraph;
    private final TIntArrayList removedArcs;
    private final int nbNodes;
    private final boolean[] matched;
    private final BitSet free;
    private final TIntIntHashMap mapValIdx;
    private final int[] mins, maxs;

    public AllDiffPrecMoreThanBc(IntVar[] variables, boolean[][] precedence) {
        this(variables, precedence, false);
    }

    public AllDiffPrecMoreThanBc(IntVar[] variables, boolean[][] precedence, boolean rcFiltering) {
        super(variables, precedence);
        this.rcFiltering = rcFiltering;
        this.n = variables.length;

        TIntArrayList list = new TIntArrayList();
        for(int i = 0; i < n; i++) {
            for(int v = variables[i].getLB(); v <= variables[i].getUB(); v = variables[i].nextValue(v)) {
                if(!list.contains(v)) {
                    list.add(v);
                }
            }
        }
        list.sort();
        int[] values = list.toArray();
        m = values.length;
        mapValIdx = new TIntIntHashMap();
        for(int j = 0; j < m; j++) {
            mapValIdx.put(values[j], j);
        }

        matched = new boolean[n + m];
        fifo = new int[n + m];
        free = new BitSet(n + m);
        next = new int[n + m];
        in = new BitSet(n + m);

        digraph = new DirectedGraph(n + m, SetType.BITSET, SetType.BITSET, true);

        mins = new int[n];
        maxs = new int[n];
        removedArcs = new TIntArrayList();
        nbNodes = n + m;
    }

    @Override
    public PropagatorPriority getPriority() {
        return PropagatorPriority.CUBIC;
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return IntEventType.all();
    }

    private int augmentPath_BFS(int root) {
        in.clear();
        int indexFirst = 0, indexLast = 0;
        fifo[indexLast++] = root;
        int x;
        ISetIterator succs;
        while (indexFirst != indexLast) {
            x = fifo[indexFirst++];
            succs = digraph.getPredecessorsOf(x).iterator();
            while (succs.hasNext()) {
                int y = succs.nextInt();
                if (!in.get(y)) {
                    next[y] = x;
                    fifo[indexLast++] = y;
                    in.set(y);
                    if (free.get(y)) {
                        return y;
                    }
                }
            }
        }
        return -1;
    }

    private boolean tryToMatch(int i) {
        int mate = augmentPath_BFS(i);
        if (mate != -1) {
            free.clear(mate);
            free.clear(i);
            int tmp = mate;
            while (tmp != i) {
                digraph.removeEdge(tmp, next[tmp]);
                digraph.addEdge(next[tmp], tmp);
                tmp = next[tmp];
            }
            return true;
        }
        return false;
    }

    /**
     * Builds the bipartite graph induced by the instantiation var <-- val.
     *
     * @param var the variable
     * @param val the value
     * @return true iff the bipartite graph has been built without any domain's wipe-out
     */
    private boolean buildDigraph(int var, int val) {
        ISetIterator iterator = digraph.getPredecessorsOf(var).iterator();
        int idxVal = mapValIdx.get(val) + n;
        while(iterator.hasNext()) {
            int idxVal2 = iterator.nextInt();
            if(idxVal2 != idxVal) {
                digraph.removeEdge(idxVal2, var);
                removedArcs.add(var + nbNodes * idxVal2);
            }
        }
        mins[var] = idxVal;
        maxs[var] = idxVal;
        for(int i = 0; i < n; i++) {
            if(i != var) {
                if(precedence[i][var]) { // i is a predecessor of v
                    if(variables[i].getUB() >= val) {
                        Set_BitSet set = (Set_BitSet) digraph.getPredecessorsOf(i);
                        int max = set.max();
                        for(int d = set.nextValue(idxVal); d <= max && d >= 0; d = set.nextValue(d+1)) {
                            digraph.removeEdge(d, i);
                            removedArcs.add(i + nbNodes * d);
                        }
                    }
                    mins[i] = mapValIdx.get(variables[i].getLB()) + n;
                    maxs[i] = mapValIdx.get(variables[i].previousValue(val)) + n;
                } else if(precedence[var][i]) { // i is a successor of v
                    if(variables[i].getLB() <= val) {
                        Set_BitSet set = (Set_BitSet) digraph.getPredecessorsOf(i);
                        int min = set.min();
                        for(int d = min; d <= idxVal && d >= 0; d = set.nextValue(d+1)) {
                            digraph.removeEdge(d, i);
                            removedArcs.add(i + nbNodes * d);
                        }
                    }
                    mins[i] = mapValIdx.get(variables[i].nextValue(val)) + n;
                    maxs[i] = mapValIdx.get(variables[i].getUB()) + n;
                } else {
                    if(digraph.getPredecessorsOf(i).contains(idxVal)) {
                        digraph.removeEdge(idxVal, i);
                        removedArcs.add(i + nbNodes * idxVal);
                    }
                    mins[i] = mapValIdx.get(variables[i].getLB()) + n;
                    maxs[i] = mapValIdx.get(variables[i].getUB()) + n;
                    if(variables[i].getLB() == val) {
                        mins[i] = mapValIdx.get(variables[i].nextValue(val)) + n;
                    }
                    if(variables[i].getUB() == val) {
                        maxs[i] = mapValIdx.get(variables[i].previousValue(val)) + n;
                    }
                }
                if(digraph.getPredecessorsOf(i).isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Applies the precedence constraints on the bipartite graph.
     *
     * @param precedenceGraph the precedence graph
     * @param topologicalTraversal the topological traversal of the precedence graph
     * @param lb true iff lower bounds are filtered (upper bounds whenever lb is false)
     * @return true iff the precedence constraints have been enforced without any domain's wipe-out
     */
    private boolean updateBoundWithinDigraph(DirectedGraph precedenceGraph, int[] topologicalTraversal, boolean lb) {
        ISetIterator it;
        for(int i = 0; i < topologicalTraversal.length; i++) {
            int var = lb ? topologicalTraversal[i] : topologicalTraversal[topologicalTraversal.length-1-i];
            if(digraph.getPredecessorsOf(var).isEmpty()) {
                return false;
            }
            it = lb ? precedenceGraph.getSuccessorsOf(var).iterator() : precedenceGraph.getPredecessorsOf(var).iterator();
            while(it.hasNext()) {
                int v = it.nextInt();
                if(digraph.getPredecessorsOf(v).isEmpty()) {
                    return false;
                }
                if(lb && mins[v] <= mins[var] || !lb && maxs[v] >= maxs[var]) {
                    int from = lb ? mins[v] : maxs[var];
                    int to = lb ? mins[var] : maxs[v];
                    for(int k = from; k <= to; k++) {
                        if(digraph.removeEdge(k, v)) {
                            removedArcs.add(v + nbNodes * k);
                        }
                    }
                    if(digraph.getPredecessorsOf(v).isEmpty()) {
                        return false;
                    }
                    if(lb) {
                        mins[v] = digraph.getPredecessorsOf(v).min();
                    } else {
                        maxs[v] = digraph.getPredecessorsOf(v).max();
                    }
                }
            }
        }
        return true;
    }

    private void greedyMatch() {
        Arrays.fill(matched, false);
        ISetIterator iterator;
        for(int varIdx = 0; varIdx < n; varIdx++) {
            iterator = digraph.getPredecessorsOf(varIdx).iterator();
            while(!matched[varIdx] && iterator.hasNext()) {
                int tmp = iterator.nextInt();
                if(!matched[tmp]) {
                    digraph.removeEdge(tmp, varIdx);
                    digraph.addEdge(varIdx, tmp);
                    matched[varIdx] = true;
                    matched[tmp] = true;
                }
            }
        }
    }

    private void restoreDigraph() {
        for(int varIdx = 0; varIdx < n; varIdx++) {
            if(!digraph.getSuccessorsOf(varIdx).isEmpty()) {
                int idxVal = digraph.getSuccessorsOf(varIdx).min();
                digraph.removeEdge(varIdx, idxVal);
                digraph.addEdge(idxVal, varIdx);
            }
        }
        for(int k = 0; k < removedArcs.size(); k++) {
            int tmp = removedArcs.getQuick(k);
            int varIdx = tmp % nbNodes;
            int idxVal = tmp / nbNodes;
            digraph.addEdge(idxVal, varIdx);
        }
        removedArcs.clear();
    }

    /**
     * Returns true iff a maximum matching of size n has been found within the bipartite graph induced by the instantiation var <-- value.
     *
     * @param var the variable
     * @param value the value
     * @param precedenceGraph the precedence graph
     * @param topologicalTraversal the topological traversal of the precedence graph
     * @return true iff a maximum matching has been found
     */
    private boolean findMaximumMatching(int var, int value, DirectedGraph precedenceGraph, int[] topologicalTraversal) {
        if(!buildDigraph(var, value)) {
            restoreDigraph();
            return false;
        }
        boolean update = updateBoundWithinDigraph(precedenceGraph, topologicalTraversal, true)
            && updateBoundWithinDigraph(precedenceGraph, topologicalTraversal, false);
        if(!update) {
            restoreDigraph();
            return false;
        }
        greedyMatch();
        free.clear();
        free.set(0, n + m);
        int nbMatched = 0;
        for(int k = 0; k < n; k++) {
            if(!digraph.getSuccessorsOf(k).isEmpty()) {
                nbMatched++;
                free.clear(k);
                free.clear(digraph.getSuccessorsOf(k).min());
            }
        }
        if(nbMatched < n) {
            boolean augmentedPathFound;
            do {
                augmentedPathFound = false;
                for (int node = free.nextSetBit(0); node >= 0 && node < n; node = free.nextSetBit(node + 1)) {
                    augmentedPathFound |= tryToMatch(node);
                }
            } while(augmentedPathFound);
            nbMatched = 0;
            for(int varIdx = 0; varIdx < n; varIdx++) {
                if(!digraph.getSuccessorsOf(varIdx).isEmpty()) {
                    nbMatched++;
                }
            }
        }
        restoreDigraph();
        return nbMatched == n;
    }

    @Override
    public boolean propagate(DirectedGraph precedenceGraph, int[] topologicalTraversal, ICause aCause) throws ContradictionException {
        for(int k = 0; k < n + m; k++) {
            digraph.getPredecessorsOf(k).clear();
            digraph.getSuccessorsOf(k).clear();
        }
        for(int i = 0; i < n; i++) {
            for(int d = variables[i].getLB(); d <= variables[i].getUB(); d = variables[i].nextValue(d)) {
                digraph.addEdge(mapValIdx.get(d) + n, i);
            }
        }
        boolean hasFiltered = false;
        for (int var = 0; var < n; var++) {
            // loop on all variables (not just the ones with predecessors or successors)
            // because we also assure arc-consistency filtering for allDifferent
            if(rcFiltering) {
                for(int val = variables[var].getLB(); val <= variables[var].getUB(); val = variables[var].nextValue(val)) {
                    if(!findMaximumMatching(var, val, precedenceGraph, topologicalTraversal)) {
                        hasFiltered |= variables[var].removeValue(val, aCause);
                        digraph.removeEdge(mapValIdx.get(val) + n, var);
                    }
                }
            } else {
                while(!findMaximumMatching(var, variables[var].getLB(), precedenceGraph, topologicalTraversal)) {
                    digraph.removeEdge(mapValIdx.get(variables[var].getLB()) + n, var);
                    hasFiltered |= variables[var].removeValue(variables[var].getLB(), aCause);
                }
                while(!findMaximumMatching(var, variables[var].getUB(), precedenceGraph, topologicalTraversal)) {
                    digraph.removeEdge(mapValIdx.get(variables[var].getUB()) + n, var);
                    hasFiltered |= variables[var].removeValue(variables[var].getUB(), aCause);
                }
            }
        }
        return hasFiltered;
    }
}
