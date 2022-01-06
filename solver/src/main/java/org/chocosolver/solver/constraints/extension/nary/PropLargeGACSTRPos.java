/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.extension.nary;

import org.chocosolver.memory.IStateInt;
import org.chocosolver.solver.constraints.extension.Tuples;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.iterators.DisposableValueIterator;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableBitSet;

import java.util.Arrays;
import java.util.BitSet;

/**
 * GAC maintaind by STR
 * <br/>
 *
 * @author Charles Prud'homme, Hadrien Cambazard
 * @since 24/04/2014
 */
public class PropLargeGACSTRPos extends PropLargeCSP<TuplesList> {

    // check if none of the tuple is trivially outside
    // the domains and if yes use a fast valid check
    // by avoiding checking the bounds
    private final ValidityChecker valcheck;

    /**
     * size of the scope
     */
    private final int arity;

    /**
     * original lower bounds
     */
    private final int[] offsets;

    /**
     * Variables that are not proved to be GAC yet
     */
    private final BitSet futureVars;

    /**
     * Values that have found a support for each variable
     */
    private final BitSet[] gacValues;

    private final int[] nbGacValues;

    /**
     * The backtrackable list of tuples representing the current
     * allowed tuples of the constraint
     */
    private final IStateInt last;
    private final int[] listuples;

    private final IntIterableBitSet vrms;

    private PropLargeGACSTRPos(IntVar[] vs, TuplesList relation) {
        super(vs, relation, false);
        this.arity = vs.length;
        this.futureVars = new BitSet(arity);
        this.gacValues = new BitSet[arity];
        this.nbGacValues = new int[arity];

        this.offsets = new int[arity];
        int min = Integer.MAX_VALUE;
        for (int i = 0; i < arity; i++) {
            this.offsets[i] = vs[i].getLB();
            this.gacValues[i] = new BitSet(vs[i].getDomainSize());
            min = Math.min(min, offsets[i]);
        }
        vrms = new IntIterableBitSet();
        vrms.setOffset(min);
        listuples = new int[this.relation.getTupleTable().length];
        for (int i = 0; i < listuples.length; i++) {
            listuples[i] = i;
        }
        last = model.getEnvironment().makeInt(listuples.length - 1);

        int[][] tt = this.relation.getTupleTable();
        boolean fastBooleanValidCheckAllowed = true;
        // check if all tuples are within the range
        // of the domain and if so set up a faster validity checker
        // that avoids checking original bounds first
        loop:
        for (int i = 0; i < tt.length; i++) {
            for (int j = 0; j < tt[i].length; j++) {
                int lb = vs[j].getLB();
                int ub = vs[j].getUB();
                if (lb < 0 || ub > 1) {
                    fastBooleanValidCheckAllowed = false;
                    break loop;
                }
            }
        }
        if (fastBooleanValidCheckAllowed) {
            valcheck = new FastBooleanValidityChecker(arity, vars);
        } else valcheck = new ValidityChecker(arity, vars);
    }

    public PropLargeGACSTRPos(IntVar[] vs, Tuples tuples) {
        this(vs, RelationFactory.makeListBasedRelation(tuples, vs));
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        valcheck.sortvars();
        gacstr();
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void initializeData() {
        //INITIALIZATION
        Arrays.fill(nbGacValues, 0);
        futureVars.set(0, arity);
        for (int i = 0; i < arity; i++) {
            gacValues[i].clear();
        }
    }

    private void pruningPhase() throws ContradictionException {
        for (int i = futureVars.nextSetBit(0); i > -1; i = futureVars.nextSetBit(i + 1)) {
            IntVar v = vars[i];
            DisposableValueIterator it3 = v.getValueIterator(true);
            vrms.clear();
            try {
                while (it3.hasNext()) {
                    int val = it3.next();
                    if (!gacValues[i].get(val - offsets[i])) {
                        vrms.add(val);
                        //                        v.removeVal(val, this, false);
                    }
                }
                v.removeValues(vrms, this);
            } finally {
                it3.dispose();
            }
        }
    }

    /**
     * maintain the list by checking all variable within isValid
     */
    //maintain the list by checking only the variable that has changed when
    //* checking if a tuple is valid.
    //*
    //* @param idx : the variable changed
    private void maintainList(/*int idx*/) {
        int cidx = 0;
        int nLast = last.get();
        while (cidx <= nLast) {
            int idxt = listuples[cidx++];
            int[] tuple = relation.getTuple(idxt);

            if (valcheck.isValid(tuple/*,idx*/)) {
                //extract the supports
                for (int i = futureVars.nextSetBit(0); i > -1; i = futureVars.nextSetBit(i + 1)) {
                    if (!gacValues[i].get(tuple[i] - offsets[i])) {
                        gacValues[i].set(tuple[i] - offsets[i]);
                        nbGacValues[i]++;
                        if (nbGacValues[i] == vars[i].getDomainSize()) {
                            futureVars.clear(i);
                        }
                    }
                }
            } else {
                //remove the tuple from the current list
                cidx--;
                final int temp = listuples[nLast];
                listuples[nLast] = listuples[cidx];
                listuples[cidx] = temp;
                last.add(-1);
                nLast--;
            }
        }
    }

    /**
     * Main propagation loop. It maintains the list of valid tuples
     * through the search
     *
     * @throws ContradictionException
     */
    private void gacstr() throws ContradictionException {
        initializeData();
        maintainList();
        pruningPhase();
    }
}
