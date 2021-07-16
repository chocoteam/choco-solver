/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.extension.nary;

import org.chocosolver.memory.IEnvironment;
import org.chocosolver.memory.IStateInt;
import org.chocosolver.solver.constraints.extension.Tuples;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.iterators.DisposableValueIterator;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableBitSet;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 26/07/12
 */
public class PropLargeGAC2001Positive extends PropLargeCSP<IterTuplesTable> {

    /**
     * supports[i][j stores the index of the tuple that currently support
     * the variable-value pair (i,j)
     */
    private IStateInt[][] supports;

    private int arity;

    private int[] offsets;

    private static final int NO_SUPPORT = -2;

    private int[][][] tab;

    // check if none of the tuple is trivially outside
    //the domains and if yes use a fast valid check
    //by avoiding checking the bounds
    private ValidityChecker valcheck;

    private final IntIterableBitSet vrms;

    private PropLargeGAC2001Positive(IntVar[] vs, IterTuplesTable relation) {
        super(vs, relation);
        this.arity = vs.length;
        this.offsets = new int[arity];
        this.supports = new IStateInt[arity][];

        IEnvironment environment = model.getEnvironment();
        for (int i = 0; i < arity; i++) {
            offsets[i] = vs[i].getLB();
            this.supports[i] = new IStateInt[vars[i].getRange()];
            for (int j = 0; j < supports[i].length; j++) {
                this.supports[i][j] = environment.makeInt(0);
            }

        }
        this.tab = relation.getTableLists();

        int[][] tt = relation.getTupleTable();
        boolean fastBooleanValidCheckAllowed = true;

        // check if all tuples are within the range
        // of the domain and if so set up a faster validity checker
        // that avoids checking original bounds first
        loop:
        for (int i = 0; i < tt.length; i++) {
            for (int j = 0; j < tt[i].length; j++) {
                int lb = vs[j].getLB();
                int ub = vs[j].getUB();
                if (lb > tt[i][j] ||
                        ub < tt[i][j]) {
                }
                if (lb < 0 || ub > 1) {
                    fastBooleanValidCheckAllowed = false;
                    break loop;
                }
            }
        }
        if (fastBooleanValidCheckAllowed) {
            valcheck = new FastBooleanValidityChecker(arity, vars);
        } else valcheck = new ValidityChecker(arity, vars);
        vrms = new IntIterableBitSet();
    }

    public PropLargeGAC2001Positive(IntVar[] vs, Tuples tuples) {
        this(vs, RelationFactory.makeIterableRelation(tuples, vs));
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        for (int indexVar = 0; indexVar < arity; indexVar++) {
            reviseVar(indexVar);
        }
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        filter(idxVarInProp);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * updates the support for all values in the domain of variable
     * and remove unsupported values for variable
     *
     * @throws ContradictionException
     */
    private void reviseVar(int indexVar) throws ContradictionException {
        DisposableValueIterator itv = vars[indexVar].getValueIterator(true);
        vrms.clear();
        vrms.setOffset(vars[indexVar].getLB());
        try {
            while (itv.hasNext()) {
                int val = itv.next();
                int nva = val - relation.getRelationOffset(indexVar);
                int currentIdxSupport = getUBport(indexVar, val);
                currentIdxSupport = seekNextSupport(indexVar, nva, currentIdxSupport);
                if (currentIdxSupport == NO_SUPPORT) {
                    vrms.add(val);
                    //                    vars[indexVar].removeVal(val, this, false);
                } else {
                    setSupport(indexVar, val, currentIdxSupport);
                }
            }
            vars[indexVar].removeValues(vrms, this);
        } finally {
            itv.dispose();
        }
    }


    /**
     * seek a new support for the pair variable-value : (indexVar, nva)
     * start the iteration from the stored support (the last one)
     */
    private int seekNextSupport(int indexVar, int nva, int start) {
        int currentIdxSupport;
        int[] currentSupport;
        for (int i = start; i < tab[indexVar][nva].length; i++) {
            currentIdxSupport = tab[indexVar][nva][i];
            currentSupport = relation.getTuple(currentIdxSupport);
            if (valcheck.isValid(currentSupport)) return i;
        }
        return NO_SUPPORT;
    }

    /**
     * store the new support
     *
     * @param idxSupport : the index of the support in the list of allowed tuples for
     *                   the pair variable-value (indexVar,value)
     */
    private void setSupport(int indexVar, int value, int idxSupport) {
        supports[indexVar][value - offsets[indexVar]].set(idxSupport); //- offset already included in blocks
    }

    /**
     * @return the stored support for the pair (indexVar,value)
     */
    private int getUBport(int indexVar, int value) {
        return supports[indexVar][value - offsets[indexVar]].get();
    }


    private void filter(int idx) throws ContradictionException {
        //sort variables regarding domain sizes to speedup the check !
        valcheck.sortvars();
        for (int i = 0; i < arity; i++)
            if (idx != valcheck.getPosition(i)) reviseVar(valcheck.getPosition(i));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("GAC2001AllowedLarge({");
        for (int i = 0; i < vars.length; i++) {
            if (i > 0) sb.append(", ");
            IntVar var = vars[i];
            sb.append(var);
        }
        sb.append("})");
        return sb.toString();
    }
}
