/**
 * Copyright (c) 2015, Ecole des Mines de Nantes
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    This product includes software developed by the <organization>.
 * 4. Neither the name of the <organization> nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.solver.constraints.extension.nary;

import org.chocosolver.memory.IEnvironment;
import org.chocosolver.memory.IStateInt;
import org.chocosolver.solver.constraints.extension.Tuples;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.ranges.IntIterableBitSet;
import org.chocosolver.solver.variables.ranges.IntIterableSet;
import org.chocosolver.util.iterators.DisposableValueIterator;

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
    protected IStateInt[][] supports;

    protected int[] blocks;

    protected int arity;

    protected int[] offsets;

    protected static final int NO_SUPPORT = -2;

    protected int[][][] tab;

    // check if none of the tuple is trivially outside
    //the domains and if yes use a fast valid check
    //by avoiding checking the bounds
    protected ValidityChecker valcheck;

    protected final IntIterableSet vrms;

    private PropLargeGAC2001Positive(IntVar[] vs, IterTuplesTable relation) {
        super(vs, relation);
        this.arity = vs.length;
        this.blocks = new int[arity];
        this.offsets = new int[arity];
        this.supports = new IStateInt[arity][];

        IEnvironment environment = solver.getEnvironment();
        for (int i = 0; i < arity; i++) {
            offsets[i] = vs[i].getLB();
            this.supports[i] = new IStateInt[vars[i].getDomainSize()];
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
        this(vs, makeRelation(tuples, vs));
    }

    private static IterTuplesTable makeRelation(Tuples tuples, IntVar[] vars) {
        return new IterTuplesTable(tuples, vars);
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
    public void reviseVar(int indexVar) throws ContradictionException {
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
    public int seekNextSupport(int indexVar, int nva, int start) {
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
    public void setSupport(int indexVar, int value, int idxSupport) {
        supports[indexVar][value - offsets[indexVar]].set(idxSupport); //- offset already included in blocks
    }

    /**
     * @return the stored support for the pair (indexVar,value)
     */
    public int getUBport(int indexVar, int value) {
        return supports[indexVar][value - offsets[indexVar]].get();
    }


    public void filter(int idx) throws ContradictionException {
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

    //<hca> implementation not efficient at all because
    //this constraint never "check" tuples but iterate over them and check the domains.
    //this should only be called in the restore solution
    public boolean isSatisfied(int[] tuple) {
        int minListIdx = -1;
        int minSize = Integer.MAX_VALUE;
        for (int i = 0; i < tuple.length; i++) {
            if (tab[i][tuple[i] - offsets[i]].length < minSize) {
                minSize = tab[i][tuple[i] - offsets[i]].length;
                minListIdx = i;
            }
        }
        int currentIdxSupport;
        int[] currentSupport;
        int nva = tuple[minListIdx] - relation.getRelationOffset(minListIdx);
        for (int i = 0; i < tab[minListIdx][nva].length; i++) {
            currentIdxSupport = tab[minListIdx][nva][i];
            currentSupport = relation.getTuple(currentIdxSupport);
            boolean isValid = true;
            for (int j = 0; isValid && j < tuple.length; j++) {
                if (tuple[j] != currentSupport[j]) {
                    isValid = false;
                }
            }
            if (isValid) return true;
        }
        return false;
    }

}
