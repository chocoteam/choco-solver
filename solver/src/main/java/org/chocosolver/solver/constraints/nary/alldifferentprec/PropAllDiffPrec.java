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
@since 01/02/2021
*/

package org.chocosolver.solver.constraints.nary.alldifferentprec;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.nary.alldifferent.algo.AlgoAllDiffBC;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.graphs.DirectedGraph;
import org.chocosolver.util.objects.setDataStructures.ISetIterator;
import org.chocosolver.util.objects.setDataStructures.SetType;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.stream.IntStream;

/**
 * Propagator for the AllDiffPrec constraint.
 *
 * @author Arthur Godet <arth.godet@gmail.com>
 */
public class PropAllDiffPrec extends Propagator<IntVar> {
    private final IntVar[] variables;
    private final boolean[][] precedence;
    private final FilterAllDiffPrec filter;
    private final AlgoAllDiffBC allDiffBC;
    private final DirectedGraph precGraph;
    private final int[] topologicalTraversal;

    /**
     * Builds a propagator for the AllDiffPrec constraint. The predecessors and successors matrix are built as following:
     * with n = |variables|, for all i in [0,n-1], if there is k such that predecessors[i][k] = j then variables[j] is a predecessor of variables[i].
     * Similarly, with n = |variables|, for all i in [0,n-1], if there is k such that successors[i][k] = j then variables[j] is a successor of variables[i].
     * The matrix should be built such that, if variables[i] is a predecessor of variables[j], then i is in successors[j] and vice versa.
     *
     * filter should be one of the following (depending on the wanted filtering algorithm): BESSIERE, GREEDY, GREEDY_RC, GODET_RC, GODET_BC or DEFAULT (which is GODET_BC).
     *
     * @param variables the variables
     * @param predecessors the predecessors matrix
     * @param successors the successors matrix
     * @param filter the name of the filtering scheme
     */
    public PropAllDiffPrec(IntVar[] variables, int[][] predecessors, int[][] successors, String filter) {
        this(variables, buildPrecedence(predecessors, successors), filter);
    }

    /**
     * Builds a propagator for the AllDiffPrec constraint. The precedence matrix is built as following:
     * with n = |variables|, for all i,j in [0,n-1], precedence[i][j] = true iff precedence[j][i] = false iff variables[i] precedes variables[j].
     *
     * filter should be one of the following (depending on the wanted filtering algorithm): BESSIERE, GREEDY, GREEDY_RC, GODET_RC, GODET_BC or DEFAULT (which is GODET_BC).
     *
     * @param variables the variables
     * @param precedence the precedence matrix
     * @param filter the name of the filtering scheme
     */
    public PropAllDiffPrec(IntVar[] variables, boolean[][] precedence, String filter) {
        this(variables, precedence, buildFilter(variables, precedence, filter));
    }

    /**
     * Builds a propagator for the AllDiffPrec constraint. The precedence matrix is built as following:
     * with n = |variables|, for all i,j in [0,n-1], precedence[i][j] = true iff precedence[j][i] = false iff variables[i] precedes variables[j].
     *
     * @param variables the variables
     * @param precedence the precedence matrix
     * @param filter the filtering scheme
     */
    public PropAllDiffPrec(IntVar[] variables, boolean[][] precedence, FilterAllDiffPrec filter) {
        super(variables, filter.getPriority(), false);
        this.variables = variables;
        this.precedence = precedence;
        this.filter = filter;
        if(filter instanceof AllDiffPrec) {
            allDiffBC = new AlgoAllDiffBC(this);
            allDiffBC.reset(vars);
        } else {
            allDiffBC = null;
        }

        precGraph = buildPrecGraph(precedence);
        topologicalTraversal = buildTopologicalTraversal(precGraph);
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return filter.getPropagationConditions(vIdx);
    }

    private boolean updateBound(boolean lb) throws ContradictionException {
        boolean hasFiltered = false;
        ISetIterator iterator;
        for(int k = 0; k < topologicalTraversal.length; k++) {
            int var = lb ? topologicalTraversal[k] : topologicalTraversal[topologicalTraversal.length-1-k];
            iterator = lb ? precGraph.getSuccessorsOf(var).iterator() : precGraph.getPredecessorsOf(var).iterator();
            while(iterator.hasNext()) {
                int rel = iterator.nextInt();
                if(lb) { // rel is a successor of var
                    if(variables[rel].updateLowerBound(variables[var].getLB() + 1, this)) {
                        hasFiltered = true;
                    }
                } else { // rel is a predecessor of var
                    if(variables[rel].updateUpperBound(variables[var].getUB() - 1, this)) {
                        hasFiltered = true;
                    }
                }
            }
        }
        return hasFiltered;
    }

    private void filterPrecedenceAndBounds() throws ContradictionException {
        boolean hasFiltered;
        do {
            hasFiltered = updateBound(true);
            hasFiltered |= updateBound(false);
            if(allDiffBC != null) {
                hasFiltered |= allDiffBC.filter();
            }
        } while(hasFiltered);
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        boolean hasFiltered;
        do {
            filterPrecedenceAndBounds(); // idempotent, no need to register hasFiltered
            hasFiltered = filter.propagate(precGraph, topologicalTraversal, this);
        } while(hasFiltered);
    }

    @Override
    public ESat isEntailed() {
        if(isCompletelyInstantiated()) {
            for(int i = 0; i < variables.length; i++) {
                for(int j = i + 1; j < variables.length; j++) {
                    if(
                        variables[i].getValue() == variables[j].getValue()
                            || precedence[i][j] && variables[i].getValue() > variables[j].getValue()
                            || precedence[j][i] && variables[i].getValue() < variables[j].getValue()
                    ) {
                        return ESat.FALSE;
                    }
                }
            }
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }

    //***********************************************************************************
    // Build data
    //***********************************************************************************

    /**
     * Builds the ancestors matrix.
     *
     * @param predecessors the predecessors matrix
     * @param successors the successors matrix
     * @return the ancestors matrix
     */
    public static int[][] buildAncestors(int[][] predecessors, int[][] successors) {
        int n = predecessors.length;
        int[][] ancestors = new int[n][];
        HashSet<Integer>[] sets = new HashSet[n];
        LinkedList<Integer> list = new LinkedList<>();
        boolean[] done = new boolean[n];
        for(int i = 0; i < n; i++) {
            sets[i] = new HashSet<>(n);
            if(predecessors[i].length == 0) {
                list.addLast(i);
            }
        }
        while(!list.isEmpty()) {
            int i = list.removeFirst();
            if(!done[i]) {
                boolean allDone = true;
                for(int j = 0; j < predecessors[i].length && allDone; j++) {
                    allDone = done[predecessors[i][j]];
                }
                if(allDone) {
                    for(int j = 0; j < predecessors[i].length; j++) {
                        sets[i].add(predecessors[i][j]);
                        sets[i].addAll(sets[predecessors[i][j]]);
                    }
                    for(int j = 0; j < successors[i].length; j++) {
                        list.addLast(successors[i][j]);
                    }
                    done[i] = true;
                }
            }
        }
        for(int i = 0; i < n; i++) {
            ancestors[i] = new int[sets[i].size()];
            Iterator<Integer> iter = sets[i].iterator();
            int j = 0;
            while(iter.hasNext()) {
                ancestors[i][j++] = iter.next();
            }
        }
        return ancestors;
    }

    /**
     * Builds the descendants matrx.
     *
     * @param predecessors the predecessors matrix
     * @param successors the successors matrix
     * @return the descendants matrix
     */
    public static int[][] buildDescendants(int[][] predecessors, int[][] successors) {
        int n = successors.length;
        int[][] descendants = new int[n][];
        HashSet<Integer>[] sets = new HashSet[n];
        LinkedList<Integer> list = new LinkedList<>();
        boolean[] done = new boolean[n];
        for(int i = 0; i < n; i++) {
            sets[i] = new HashSet<>();
            if(successors[i].length == 0) {
                list.addLast(i);
            }
        }
        while(!list.isEmpty()) {
            int i = list.removeFirst();
            if(!done[i]) {
                boolean allDone = true;
                for(int j = 0; j < successors[i].length && allDone; j++) {
                    allDone = done[successors[i][j]];
                }
                if(allDone) {
                    for(int j = 0; j < successors[i].length; j++) {
                        sets[i].add(successors[i][j]);
                        sets[i].addAll(sets[successors[i][j]]);
                    }
                    for(int j = 0; j < predecessors[i].length; j++) {
                        list.addLast(predecessors[i][j]);
                    }
                    done[i] = true;
                }
            }
        }
        for(int i = 0; i < n; i++) {
            descendants[i] = new int[sets[i].size()];
            Iterator<Integer> iter = sets[i].iterator();
            int j = 0;
            while(iter.hasNext()) {
                descendants[i][j++] = iter.next();
            }
        }
        return descendants;
    }

    /**
     * Returns true iff the array a contains the value v.
     *
     * @param a the array
     * @param v the value
     * @return true iff the array a contains the value v
     */
    public static boolean contains(int[] a, int v) {
        return contains(a, v, a.length);
    }

    /**
     * Returns true iff the array a contains the value v before the index maxIdx (strictly).
     *
     * @param a the array
     * @param v the value
     * @param maxIdx the maximum index
     * @return true iff a contains v before index maxIdx
     */
    public static boolean contains(int[] a, int v, int maxIdx) {
        for(int i = 0; i < maxIdx; i++) {
            if(a[i] == v) {
                return true;
            }
        }
        return false;
    }

    public static boolean[][] buildPrecedence(int[][] predecessors, int[][] successors) {
        return buildPrecedence(predecessors, successors, false);
    }

    /**
     * Returns the precedence matrix, such that precedence[v][w] = true iff v is a predecessor of w.
     *
     * @param predecessors the predecessors matrix
     * @param successors the successors matrix
     * @param alreadyComputed true iff predecessors and successors matrix are ancestors and descendants matrix
     * @return the precedence matrix
     */
    public static boolean[][] buildPrecedence(int[][] predecessors, int[][] successors, boolean alreadyComputed) {
        int[][] ancestors = alreadyComputed ? predecessors : buildAncestors(predecessors, successors);
        int[][] descendants = alreadyComputed ? successors : buildDescendants(predecessors, successors);
        int n = predecessors.length;
        boolean[][] precedence = new boolean[n][n];
        for(int i = 0; i < n; i++) {
            precedence[i][i] = false;
            for(int j = i + 1; j < n; j++) {
                if(contains(ancestors[i], j)) {
                    precedence[j][i] = true;
                } else if(contains(descendants[i], j)) {
                    precedence[i][j] = true;
                }
                // nothing to do in the else case
            }
        }
        return precedence;
    }

    public static DirectedGraph buildPrecGraph(boolean[][] precedence) {
        int n = precedence.length;
        DirectedGraph precGraph = new DirectedGraph(n, SetType.BITSET, true);
        for(int v = 0; v < n; v++) {
            for(int w = v + 1; w < n; w++) {
                if(precedence[v][w]) {
                    precGraph.addEdge(v, w);
                } else if(precedence[w][v]) {
                    precGraph.addEdge(w, v);
                }
            }
        }
        return precGraph;
    }

    /**
     * Builds the topological traversal of the given precedence graph.
     *
     * @param precGraph the precedence graph
     * @return the topological traversal
     */
    public static int[] buildTopologicalTraversal(DirectedGraph precGraph) {
        int n = precGraph.getNbMaxNodes();
        int[] depth = new int[n];
        LinkedList<Integer> queue = new LinkedList<>();
        for(int v = 0; v < n; v++) {
            if(precGraph.getPredecessorsOf(v).isEmpty()) {
                queue.addLast(v);
            }
        }
        while(!queue.isEmpty()) {
            int v = queue.removeFirst();
            ISetIterator iterator = precGraph.getPredecessorsOf(v).iterator();
            while(iterator.hasNext()) {
                int pre = iterator.nextInt();
                depth[v] = Math.max(depth[v], depth[pre] + 1);
            }
            iterator = precGraph.getSuccessorsOf(v).iterator();
            while(iterator.hasNext()) {
                int succ = iterator.nextInt();
                if(!queue.contains(succ)) {
                    queue.addLast(succ);
                }
            }
        }
        return IntStream.range(0, n)
                        .boxed()
                        .sorted(Comparator.comparingInt(i -> depth[i]))
                        .mapToInt(i -> i)
                        .toArray();
    }

    /**
     * Returns the filtering algorithm structure for the given variables, the precedence and whose behaviour is one of the following:
     * BESSIERE, GREEDY, GREEDY_RC, GODET_RC, GODET_BC or DEFAULT (which is GODET_BC).
     *
     * @param variables the variables
     * @param precedence the precedence matrix
     * @param filt the filtering algorithm to select
     * @return the filtering algorithm
     */
    public static FilterAllDiffPrec buildFilter(IntVar[] variables, boolean[][] precedence, String filt) {
        switch(filt) {
            case "BESSIERE": return new AllDiffPrec(variables, precedence);
            case "GREEDY": return new GreedyBoundSupport(variables, precedence);
            case "GREEDY_RC": return new GreedyBoundSupport(variables, precedence,true);
            case "GODET_RC": return new AllDiffPrecMoreThanBc(variables, precedence, true);
            case "GODET":
            case "DEFAULT":
            default: return new AllDiffPrecMoreThanBc(variables, precedence);
        }
    }
}
