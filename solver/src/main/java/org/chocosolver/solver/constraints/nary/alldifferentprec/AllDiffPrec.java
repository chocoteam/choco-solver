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

import gnu.trove.set.hash.TIntHashSet;
import org.chocosolver.solver.ICause;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.objects.graphs.DirectedGraph;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Filtering algorithm for the AllDiffPrec constraint introduced in the following paper :
 * Bessiere et al. (2011). The AllDifferent Constraint with Precedences. In Integration of AI and OR Techniques in Constraint Programming for Combinatorial Optimization Problems - 8th International Conference, CPAIOR 2011, Berlin, Germany, May 23-27, 2011. Proceedings. Ed. by Tobias Achterberg and J. Christopher Beck. Vol. 5697. Lecture Notes in Computer Science. Springer, 2011, pp. 36-52.
 *
 * @author Arthur Godet <arth.godet@gmail.com>
 */
public class AllDiffPrec extends FilterAllDiffPrec {
    private IntUnionFind unionFind;
    private final IntUnionFind unionFindLB;
    private final IntUnionFind unionFindUB;
    private final ArrayList<Integer> list;
    private final int[] lb;
    private final int[] ub;
    private final int[] ubFilt;

    public AllDiffPrec(IntVar[] variables, boolean[][] precedence) {
        super(variables, precedence);
        TIntHashSet set = new TIntHashSet();
        TIntHashSet set2 = new TIntHashSet();
        for(int i = 0; i < variables.length; i++) {
            for(int v = variables[i].getLB(); v <= variables[i].getUB(); v++) {
                set.add(v);
                set2.add(-v);
            }
        }
        unionFindUB = new IntUnionFind(set.toArray());
        unionFindLB = new IntUnionFind(set2.toArray());
        list = new ArrayList<>();
        for(int i = 0; i < variables.length; i++) {
            list.add(i);
        }
        lb = new int[variables.length];
        ub = new int[variables.length];
        ubFilt = new int[variables.length];
    }

    @Override
    public PropagatorPriority getPriority() {
        return PropagatorPriority.QUADRATIC;
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return IntEventType.boundAndInst();
    }

    private boolean isBefore(boolean filterUb, int i, int j) {
        if(filterUb) {
            return precedence[i][j];
        } else {
            return precedence[j][i];
        }
    }

    private void writeLbUb(boolean filterUb) {
        for(int i = 0; i < variables.length; i++) {
            if(filterUb) {
                lb[i] = variables[i].getLB();
                ub[i] = variables[i].getUB();
            } else {
                lb[i] = -variables[i].getUB();
                ub[i] = -variables[i].getLB();
            }
        }
    }

    private void filterLbUb(boolean filterUb, ICause aCause) throws ContradictionException {
        for(int i = 0; i < variables.length; i++) {
            if(filterUb) {
                variables[i].updateUpperBound(ubFilt[i], aCause);
            } else {
                variables[i].updateLowerBound(-ubFilt[i], aCause);
            }
        }
    }

    private final Comparator<Integer> comparator = new Comparator<Integer>() {
        @Override
        public int compare(Integer o1, Integer o2) {
            if (ub[o1] == ub[o2]) {
                if (lb[o1] == lb[o2]) {
                    return o1 - o2;
                }
                return lb[o1] - lb[o2];
            }
            return ub[o1] - ub[o2];
        }
    };

    private boolean filter(boolean filterUb) {
        boolean hasFiltered = false;
        list.sort(comparator);
        for (int i = 0; i < variables.length; i++) {
            unionFind.init();
            ubFilt[i] = ub[i];
            int b = ub[list.get(0)] + 1;
            boolean encountered = false;
            for (int k = 0; k < list.size(); k++) {
                int j = list.get(k);
                if(j == i) {
                    encountered = true;
                }
                if(k > 0) {
                    for (int l = 0; l < ub[list.get(k)] - ub[list.get(k - 1)]; l++) {
                        int idxSet = unionFind.find(b);
                        if (idxSet == -1) {
                            b++;
                        } else {
                            b = unionFind.getMax(idxSet) + 1;
                        }
                    }
                }
                if(j != i) {
                    int v = -1;
                    if (!isBefore(filterUb, i, j)) {
                        int idxSet = unionFind.find(lb[j]);
                        v = unionFind.getMin(idxSet);
                        if(unionFind.find(unionFind.getMax(idxSet) + 1) >= 0) {
                            unionFind.union(v, unionFind.getMax(idxSet) + 1);
                        }
                    }
                    if (k > 0 && (isBefore(filterUb, i, j) || v > b || unionFind.find(v) == unionFind.find(b))) {
                        int idxSet = unionFind.find(b - 1);
                        if (idxSet == -1) {
                            b--;
                        } else {
                            b = unionFind.getMin(idxSet);
                        }
                    }
                    if(encountered && ubFilt[i] > b - 1) {
                        hasFiltered = true;
                        ubFilt[i] = b - 1;
                    }
                }
            }
        }
        return hasFiltered;
    }

    private boolean propagateBounds(boolean filterUb, ICause aCause) throws ContradictionException {
        boolean hasFiltered;
        writeLbUb(filterUb);
        unionFind = filterUb ? unionFindUB : unionFindLB;
        hasFiltered = filter(filterUb);
        filterLbUb(filterUb, aCause);
        return hasFiltered;
    }

    @Override
    public boolean propagate(DirectedGraph precedenceGraph, int[] topologicalTraversal, ICause aCause) throws ContradictionException {
        return propagateBounds(true, aCause) || propagateBounds(false, aCause);
    }
}
