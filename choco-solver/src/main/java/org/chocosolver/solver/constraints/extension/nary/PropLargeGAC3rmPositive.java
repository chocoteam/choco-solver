/**
 * Copyright (c) 2014,
 *       Charles Prud'homme (TASC, INRIA Rennes, LINA CNRS UMR 6241),
 *       Jean-Guillaume Fages (COSLING S.A.S.).
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
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

import gnu.trove.map.hash.THashMap;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.extension.Tuples;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;

/**
 * <br/>
 *
 * @author Charles Prud'homme, Hadrien Cambazard
 * @since 08/06/11
 */
public class PropLargeGAC3rmPositive extends PropLargeCSP<IterTuplesTable> {

    /**
     * supports[i][j stores the index of the tuple that currently support
     * the variable-value pair (i,j)
     */
    protected final int[][] supports;

    /**
     * size of the scope of the constraint
     */
    protected final int arity;

    /**
     * original lower bounds
     */
    protected final int[] offsets;

    protected static final int NO_SUPPORT = -2;

    //a reference on the lists of supports per variable value pair
    protected int[][][] tab;

    // check if none of the tuple is trivially outside
    //the domains and if yes use a fast valid check
    //by avoiding checking the bounds
    protected ValidityChecker valcheck;

    private PropLargeGAC3rmPositive(IntVar[] vars, IterTuplesTable relation) {
        super(vars, relation);
        this.arity = vars.length;
        this.offsets = new int[arity];
        this.supports = new int[arity][];
        for (int i = 0; i < arity; i++) {
            this.offsets[i] = vars[i].getLB();
            this.supports[i] = new int[vars[i].getDomainSize()];
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
                int lb = vars[j].getLB();
                int ub = vars[j].getUB();
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

    public PropLargeGAC3rmPositive(IntVar[] vars, Tuples tuples) {
        this(vars, makeRelation(tuples, vars));
    }

    private static IterTuplesTable makeRelation(Tuples tuples, IntVar[] vars) {
        return new IterTuplesTable(tuples, vars);
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        initSupports();
        for (int indexVar = 0; indexVar < arity; indexVar++)
            reviseVar(indexVar);
    }

    @Override
    public void propagate(int vIdx, int mask) throws ContradictionException {
        filter(vIdx);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * initialize the residual supports of each pair to their
     * first allowed tuple
     */
    protected void initSupports() throws ContradictionException {
        for (int i = 0; i < vars.length; i++) {
            int left = Integer.MIN_VALUE;
            int right = left;
            int ubi = vars[i].getUB();
            for (int val = vars[i].getLB(); val <= ubi; val = vars[i].nextValue(val)) {
                int nva = val - relation.getRelationOffset(i);
                if (tab[i][nva].length == 0) {
                    if (val == right + 1) {
                        right = val;
                    } else {
                        vars[i].removeInterval(left, right, aCause);
                        left = right = val;
                    }
                } else {
                    setSupport(tab[i][nva][0]);
                }
            }
            vars[i].removeInterval(left, right, aCause);
        }
    }

    /**
     * set the support using multidirectionality
     */
    protected void setSupport(final int idxSupport) {
        int[] tuple = relation.getTuple(idxSupport);
        for (int i = 0; i < tuple.length; i++) {
            supports[i][tuple[i] - offsets[i]] = idxSupport;
        }
    }

    /**
     * @param value with offset removed
     * @return the residual support
     */
    protected int getSupport(final int indexVar, final int value) {
        return supports[indexVar][value - offsets[indexVar]];
    }

    /**
     * updates the support for all values in the domain of variable
     * and remove unsupported values for variable
     *
     * @throws ContradictionException
     */
    protected void reviseVar(final int indexVar) throws ContradictionException {
        int left = Integer.MIN_VALUE;
        int right = left;
        int ub = vars[indexVar].getUB();
        for (int val = vars[indexVar].getLB(); val <= ub; val = vars[indexVar].nextValue(val)) {
            int nva = val - relation.getRelationOffset(indexVar);
            int currentIdxSupport = getSupport(indexVar, val);
            //check the residual support !
            if (!valcheck.isValid(relation.getTuple(currentIdxSupport))) {
                //the residual support is not valid anymore, seek a new one
                currentIdxSupport = seekNextSupport(indexVar, nva);
                if (currentIdxSupport == NO_SUPPORT) {
                    if (val == right + 1) {
                        right = val;
                    } else {
                        vars[indexVar].removeInterval(left, right, aCause);
                        left = right = val;
                    }
                } else {
                    setSupport(currentIdxSupport);
                }
            }
        }
        vars[indexVar].removeInterval(left, right, aCause);
    }


    /**
     * seek a new support for the pair variable-value : (indexVar, nva)
     * start the iteration from scratch in the list
     */
    protected int seekNextSupport(final int indexVar, final int nva) {
        int currentIdxSupport;
        int[] currentSupport;
        for (int i = 0; i < tab[indexVar][nva].length; i++) {
            currentIdxSupport = tab[indexVar][nva][i];
            currentSupport = relation.getTuple(currentIdxSupport);
            if (valcheck.isValid(currentSupport)) {
                return currentIdxSupport;
            }
        }
        return NO_SUPPORT;
    }

    protected void filter(int idx) throws ContradictionException {
        //sort variables regarding domain sizes to speedup the check !
        valcheck.sortvars();
        for (int i = 0; i < arity; i++) {
            if (idx != valcheck.getPosition(i)) {
                reviseVar(valcheck.getPosition(i));
            }
        }
    }

    @Override
    public void duplicate(Solver solver, THashMap<Object, Object> identitymap) {
        if (!identitymap.containsKey(this)) {
            int size = this.vars.length;
            IntVar[] aVars = new IntVar[size];
            for (int i = 0; i < size; i++) {
                this.vars[i].duplicate(solver, identitymap);
                aVars[i] = (IntVar) identitymap.get(this.vars[i]);
            }
            identitymap.put(this, new PropLargeGAC3rmPositive(aVars, (IterTuplesTable) relation.duplicate()));
        }
    }
}
