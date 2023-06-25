/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2023, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.extension.hybrid;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.SetFactory;
import org.chocosolver.util.objects.setDataStructures.SetType;

import java.util.BitSet;

/**
 * This class implements an adaptation of the filtering algorithm from:
 * "Extending Compact-Table to Basic Smart Tables",
 * H. Verhaeghe and C. Lecoutre and Y. Deville and P. Schauss, CP-17.
 * to STR2. It deals with smart/hybrid tuples.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 22/03/2023
 */
public class PropHybridTable extends Propagator<IntVar> {

    private final ISupportable[][] table;
    private final ASupport.StrHVar[] str2vars;
    private final ISet activeTuples;
    private final BitSet ssup;
    private final BitSet sval;
    private final UndirectedGraph relationships;
    private boolean firstProp = true;

    public PropHybridTable(IntVar[] vars, HybridTuples tuples) {
        super(vars, PropagatorPriority.QUADRATIC, false);
        this.table = tuples.toArray();
        int nbVars = vars.length;
        relationships = new UndirectedGraph(nbVars, SetType.BITSET, SetType.BITSET, true);
        str2vars = new ASupport.StrHVar[nbVars];
        for (int i = 0; i < nbVars; i++) {
            str2vars[i] = new ASupport.StrHVar(model.getEnvironment(), vars[i], i);
            for (int t = 0; t < table.length; t++) {
                connect(table[t][i]);
            }
        }
        activeTuples = SetFactory.makeStoredSet(SetType.BIPARTITESET, 0, model);
        ssup = new BitSet(vars.length);
        sval = new BitSet(vars.length);
    }

    /**
     * Connect variable to each other for any non-unary expression.
     * @param supportable an expression
     */
    private void connect(ISupportable supportable) {
        if (supportable instanceof ISupportable.Many) {
            ISupportable.Many many = (ISupportable.Many) supportable;
            for (ISupportable supp : many.exps) {
                connect(supp);
            }
        } else if (supportable instanceof ISupportable.Nary) {
            ISupportable.Nary nary = (ISupportable.Nary) supportable;
            for (int i = 0; i < nary.is.length - 1; i++) {
                for (int j = i + 1; j < nary.is.length; j++) {
                    relationships.addEdge(nary.is[i], nary.is[j]);
                }
            }
        }
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (firstProp) {
            firstProp = false;
            model.getEnvironment().save(() -> firstProp = true);
            initialPropagate();
        }
        filter();
    }

    @Override
    public ESat isEntailed() {
        boolean hasSupport = false;
        l1:
        for (int i = 0; i < table.length && !hasSupport; i++) {
            for (int j = 0; j < str2vars.length; j++) {
                ASupport.StrHVar v = str2vars[j];
                if (!table[i][v.index].satisfiable(str2vars, v.index)) {
                    continue l1;
                }
            }
            hasSupport = true;
        }
        if (hasSupport) {
            if (isCompletelyInstantiated()) {
                return ESat.TRUE;
            } else {
                return ESat.UNDEFINED;
            }
        } else {
            return ESat.FALSE;
        }
    }

    @Override
    public String toString() {
        return "STR2 hybrid table constraint with " + vars.length + " vars and " + table.length + " tuples";
    }

    //***********************************************************************************
    // DEDICATED METHODS
    //***********************************************************************************

    private boolean isTupleSupported(int tuple_index) {
        for (int i = sval.nextSetBit(0); i > -1; i = sval.nextSetBit(i + 1)) {
            ASupport.StrHVar v = str2vars[i];
            if (!table[tuple_index][v.index].satisfiable(str2vars, v.index)) {
                return false;
            }
        }
        return true;
    }

    private void initialPropagate() throws ContradictionException {
        for (int t = 0; t < table.length; t++) {
            activeTuples.add(t);
        }
        if (activeTuples.isEmpty()) {
            this.fails();
        }
    }

    private void filter() throws ContradictionException {
        ssup.clear();
        sval.clear();
        for (int i = 0; i < str2vars.length; i++) {
            ASupport.StrHVar tmp = str2vars[i];
            ssup.set(i);
            tmp.reset();
            if (tmp.last_size.get() != tmp.cnt) {
                // to get variables modified since last call
                sval.set(i);
                tmp.last_size.set(tmp.cnt);
                // if the modified variable is connected to other ones with expressions...
                for (int n : relationships.getNeighborsOf(i)/*.toArray()*/) {
                    sval.set(n);
                }
            }
        }
        boolean loop;
        do {
            loop = false;
            for (int tidx : activeTuples/*.toArray()*/) {
                if (isTupleSupported(tidx)) {
                    for (int i = ssup.nextSetBit(0); i > -1; i = ssup.nextSetBit(i + 1)) {
                        ISupportable exp = table[tidx][i];
                        // For Many only, because it computes supports on the call to satisfiable(_,_)
                        // if the variable was not tagged as modified, then isTupleSupported(_) is outdated
                        if(!sval.get(i) && exp instanceof ISupportable.Many){
                            exp.satisfiable(str2vars, i);
                        }
                        exp.support(str2vars, i);
                        if (str2vars[i].cnt == 0) {
                            ssup.clear(i);
                        }
                    }
                } else {
                    activeTuples.remove(tidx);
                }
            }
            sval.clear();
            for (int i = 0; i < str2vars.length; i++) {
                if (str2vars[i].cnt > 0) {
                    if (str2vars[i].removeUnsupportedValue(this)) {
                        loop = true;
                        sval.set(i);
                        // if the modified variable is connected to other ones with expressions...
                        for (int n : relationships.getNeighborsOf(i)/*.toArray()*/) {
                            sval.set(n);
                        }
                        str2vars[i].last_size.set(str2vars[i].cnt);
                    }
                }
                ssup.set(i);
            }
        } while (loop);
    }
}
