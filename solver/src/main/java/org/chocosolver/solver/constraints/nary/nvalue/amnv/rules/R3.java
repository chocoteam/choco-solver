/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.nvalue.amnv.rules;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.nary.nvalue.amnv.mis.F;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.ISetIterator;
import org.chocosolver.util.objects.setDataStructures.SetFactory;
import org.chocosolver.util.objects.setDataStructures.SetType;

import java.util.BitSet;

/**
 * R3 filtering rule (back-propagation)
 *
 * @author Jean-Guillaume Fages
 * @since 01/01/2014
 */
public class R3 implements R {


    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private final int n;
    private int[] valToRem;
    private final ISet[] learntEqualities;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public R3(int nbDecVars, Model model) {
        n = nbDecVars;
        valToRem = new int[31];
        learntEqualities = new ISet[n];
        for (int i = 0; i < n; i++) {
            learntEqualities[i] = SetFactory.makeStoredSet(SetType.BITSET, 0, model);
        }
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    public void filter(IntVar[] vars, UndirectedGraph graph, F heur, Propagator aCause) throws ContradictionException {
        assert n == vars.length - 1;
        BitSet mis = heur.getMIS();
        if (mis.cardinality() == vars[n].getUB()) {
            for (int i = mis.nextClearBit(0); i >= 0 && i < n; i = mis.nextClearBit(i + 1)) {
                int mate = -1;
                int last = 0;
                if (valToRem.length < vars[i].getDomainSize()) {
                    valToRem = new int[vars[i].getDomainSize() * 2];
                }
                int ub = vars[i].getUB();
                int lb = vars[i].getLB();
                for (int k = lb; k <= ub; k = vars[i].nextValue(k)) {
                    valToRem[last++] = k;
                }
                ISetIterator nei = graph.getNeighborsOf(i).iterator();
                while (nei.hasNext()) {
                    int j = nei.nextInt();
                    if (mis.get(j)) {
                        if (mate == -1) {
                            mate = j;
                        } else if (mate >= 0) {
                            mate = -2;
                        }
                        for (int ik = 0; ik < last; ik++) {
                            if (vars[j].contains(valToRem[ik])) {
                                last--;
                                if (ik < last) {
                                    valToRem[ik] = valToRem[last];
                                    ik--;
                                }
                            }
                        }
                        if (mate == -2 && last == 0) break;
                    }
                }
                if (mate >= 0) {
                    enforceEq(i, mate, vars, aCause);
                } else {
                    for (int ik = 0; ik < last; ik++) {
                        vars[i].removeValue(valToRem[ik], aCause);
                    }
                }
            }
        }
        for (int i = 0; i < n; i++) {
            ISetIterator eqs = learntEqualities[i].iterator();
            while (eqs.hasNext()) {
                enforceEq(i, eqs.nextInt(), vars, aCause);
            }
        }
    }

    protected void enforceEq(int i, int j, IntVar[] vars, Propagator aCause) throws ContradictionException {
        if (i > j) {
            enforceEq(j, i, vars, aCause);
        } else {
            learntEqualities[i].add(j);
            learntEqualities[j].add(i);
            IntVar x = vars[i];
            IntVar y = vars[j];
            while (x.getLB() != y.getLB() || x.getUB() != y.getUB()) {
                x.updateLowerBound(y.getLB(), aCause);
                x.updateUpperBound(y.getUB(), aCause);
                y.updateLowerBound(x.getLB(), aCause);
                y.updateUpperBound(x.getUB(), aCause);
            }
            if (x.hasEnumeratedDomain() && y.hasEnumeratedDomain()) {
                int ub = x.getUB();
                for (int val = x.getLB(); val <= ub; val = x.nextValue(val)) {
                    if (!y.contains(val)) {
                        x.removeValue(val, aCause);
                    }
                }
                ub = y.getUB();
                for (int val = y.getLB(); val <= ub; val = y.nextValue(val)) {
                    if (!x.contains(val)) {
                        y.removeValue(val, aCause);
                    }
                }
            }
        }
    }
}
