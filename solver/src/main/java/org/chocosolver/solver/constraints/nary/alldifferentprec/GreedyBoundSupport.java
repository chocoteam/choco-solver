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
@since 14/04/2021
*/

package org.chocosolver.solver.constraints.nary.alldifferentprec;

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.objects.graphs.DirectedGraph;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.ISetIterator;
import org.chocosolver.util.objects.setDataStructures.SetFactory;

/**
 * Filtering algorithm for the AllDiffPrec constraint introduced in the following paper :
 * Bessiere et al. (2011). The AllDifferent Constraint with Precedences. In Integration of AI and OR Techniques in Constraint Programming for Combinatorial Optimization Problems - 8th International Conference, CPAIOR 2011, Berlin, Germany, May 23-27, 2011. Proceedings. Ed. by Tobias Achterberg and J. Christopher Beck. Vol. 5697. Lecture Notes in Computer Science. Springer, 2011, pp. 36-52.
 *
 * @author Arthur Godet <arth.godet@gmail.com>
 */
public class GreedyBoundSupport extends FilterAllDiffPrec {
    private final boolean rcFiltering;
    private final ISet instVars;
    private final ISet notInstVars;
    private final ISet instValues;
    private final ISet availableVars;
    private final ISet tmp;
    private final int n;
    private int min;
    private int max;
    private final int[] mins, maxs;
    private final int[] assign;
    private int candidate;
    private int nextAvailableValue;

    public GreedyBoundSupport(IntVar[] variables, boolean[][] precedence) {
        this(variables, precedence, false);
    }

    public GreedyBoundSupport(IntVar[] variables, boolean[][] precedence, boolean rcFiltering) {
        super(variables, precedence);
        this.rcFiltering = rcFiltering;
        instVars = SetFactory.makeBitSet(0);
        notInstVars = SetFactory.makeBitSet(0);
        instValues = SetFactory.makeBitSet(0);
        availableVars = SetFactory.makeBitSet(0);
        tmp = SetFactory.makeBitSet(0);

        this.n = variables.length;

        mins = new int[n];
        maxs = new int[n];
        assign = new int[n];
    }

    @Override
    public PropagatorPriority getPriority() {
        return PropagatorPriority.QUADRATIC;
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return IntEventType.boundAndInst();
    }

    private boolean initDomains(int var, int val) {
        mins[var] = val;
        maxs[var] = val;
        for(int i = 0; i < n; i++) {
            if(i != var) {
                if(precedence[i][var]) { // i is a predecessor of v
                    mins[i] = variables[i].getLB();
                    maxs[i] = Math.min(val - 1, variables[i].getUB());
                } else if(precedence[var][i]) { // i is a successor of v
                    mins[i] = Math.max(val + 1, variables[i].getLB());
                    maxs[i] = variables[i].getUB();
                } else {
                    mins[i] = variables[i].getLB();
                    if(mins[i] == val) {
                        mins[i] = Math.max(val + 1, variables[i].getLB());
                    }
                    maxs[i] = variables[i].getUB();
                    if(maxs[i] == val) {
                        maxs[i] = Math.min(val - 1, variables[i].getUB());
                    }
                }
                if(mins[i] > maxs[i]) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean updateBounds(DirectedGraph precedenceGraph, int[] topologicalTraversal, boolean lb) {
        ISetIterator it;
        for(int i = 0; i < topologicalTraversal.length; i++) {
            int var = lb ? topologicalTraversal[i] : topologicalTraversal[topologicalTraversal.length-1-i];
            if(mins[var] > maxs[var]) {
                return false;
            }
            it = lb ? precedenceGraph.getSuccessorsOf(var).iterator() : precedenceGraph.getPredecessorsOf(var).iterator();
            while(it.hasNext()) {
                int v = it.nextInt();
                if(mins[v] > maxs[v]) {
                    return false;
                }
                if(lb && mins[v] <= mins[var] || !lb && maxs[v] >= maxs[var]) {
                    if(lb) {
                        mins[v] = mins[var] + 1;
                    } else {
                        maxs[v] = maxs[var] - 1;
                    }
                    if(mins[v] > maxs[v]) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean containsPrecOf(ISet set, int value, DirectedGraph precedenceGraph) {
        ISetIterator it = set.iterator();
        while(it.hasNext()) {
            int i = it.nextInt();
            if(precedenceGraph.getPredecessorsOf(value).contains(i)) {
                return true;
            }
        }
        return false;
    }

    private void addAvailableValues(int var, int v) {
        if(v == nextAvailableValue) {
            nextAvailableValue = Integer.MAX_VALUE;
            ISetIterator it = notInstVars.iterator();
            while(it.hasNext()) {
                int i = it.nextInt();
                if(mins[i] == v && i != var) {
                    availableVars.add(i);
                    tmp.add(i);
                } else if(mins[i] > v) {
                    nextAvailableValue = Math.min(nextAvailableValue, mins[i]);
                }
            }
        }
    }

    private boolean computeCandidate(int v, DirectedGraph precedenceGraph) {
        ISetIterator iterator;
        int u = Integer.MAX_VALUE;
        candidate = -1;
        iterator = availableVars.iterator();
        while(iterator.hasNext()) {
            int i = iterator.nextInt();
            if(u > maxs[i] && !containsPrecOf(tmp, i, precedenceGraph)) {
                u = maxs[i];
                candidate = i;
            }
        }
        return u >= v && candidate != -1; // found valid candidate
    }

    private boolean foundBoundSupport(int var, int val, DirectedGraph precedenceGraph, int[] topologicalTraversal) {
        if(!initDomains(var, val)) {
            return false;
        }
        if(!updateBounds(precedenceGraph, topologicalTraversal, true) || !updateBounds(precedenceGraph, topologicalTraversal, false)) {
            return false;
        }
        availableVars.clear();
        tmp.clear();
        ISetIterator it = notInstVars.iterator();
        while(it.hasNext()) {
            assign[it.nextInt()] = -1;
        }
        assign[var] = val;
        nextAvailableValue = min;
        for(int v = min; v <= max; v++) {
            addAvailableValues(var, v);
            if(!instValues.contains(v) && !availableVars.isEmpty() && v != val) {
                if(!computeCandidate(v, precedenceGraph)) {
                    return false;
                }
                availableVars.remove(candidate);
                tmp.remove(candidate);
                assign[candidate] = v;
            }
        }
        return !PropAllDiffPrec.contains(assign,-1);
    }

    @Override
    public boolean propagate(DirectedGraph precedenceGraph, int[] topologicalTraversal, ICause aCause) throws ContradictionException {
        instValues.clear();
        min = Integer.MAX_VALUE;
        max = Integer.MIN_VALUE;
        instVars.clear();
        notInstVars.clear();
        for(int i = 0; i < variables.length; i++) {
            if(variables[i].isInstantiated()) {
                instVars.add(i);
                instValues.add(variables[i].getValue());
                assign[i] = variables[i].getValue();
            } else {
                notInstVars.add(i);
                min = Math.min(min, variables[i].getLB());
                max = Math.max(max, variables[i].getUB());
            }
        }
        boolean hasFiltered = false;
        for (int var = 0; var < variables.length; var++) {
            if(rcFiltering) {
                for(int val = variables[var].getLB(); val <= variables[var].getUB(); val = variables[var].nextValue(val)) {
                    if(!foundBoundSupport(var, val, precedenceGraph, topologicalTraversal)) {
                        hasFiltered |= variables[var].removeValue(val, aCause);
                    }
                }
            } else {
                while(!foundBoundSupport(var, variables[var].getLB(), precedenceGraph, topologicalTraversal)) {
                    hasFiltered |= variables[var].removeValue(variables[var].getLB(), aCause);
                }
                while(!foundBoundSupport(var, variables[var].getUB(), precedenceGraph, topologicalTraversal)) {
                    hasFiltered |= variables[var].removeValue(variables[var].getUB(), aCause);
                }
            }
            if(variables[var].isInstantiated() && !instVars.contains(var)) {
                instVars.add(var);
                notInstVars.remove(var);
                instValues.add(variables[var].getValue());
            }
        }
        return hasFiltered;
    }
}
