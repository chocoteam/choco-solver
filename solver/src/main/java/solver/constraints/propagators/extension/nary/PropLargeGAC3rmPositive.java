/*
 * Copyright (c) 1999-2012, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package solver.constraints.propagators.extension.nary;

import choco.kernel.ESat;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.constraints.propagators.extension.FastBooleanValidityChecker;
import solver.constraints.propagators.extension.FastValidityChecker;
import solver.constraints.propagators.extension.ValidityChecker;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.IntVar;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 08/06/11
 */
public class PropLargeGAC3rmPositive extends Propagator<IntVar> {

    protected final IterTuplesTable relation;

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

    public PropLargeGAC3rmPositive(IntVar[] vars, IterTuplesTable relation, Solver solver, Constraint<IntVar, Propagator<IntVar>> intVarPropagatorConstraint) {
        super(vars, solver, intVarPropagatorConstraint, PropagatorPriority.QUADRATIC, false);
        this.relation = relation;
        this.arity = vars.length;
        this.offsets = new int[arity];
        this.tab = relation.getTableLists();
        this.supports = new int[arity][];
        for (int i = 0; i < arity; i++) {
            this.offsets[i] = vars[i].getLB();
            this.supports[i] = new int[vars[i].getUB() - vars[i].getLB() + 1];
        }
        int[][] tt = relation.getTupleTable();
        boolean fastValidCheckAllowed = true;
        boolean fastBooleanValidCheckAllowed = true;
        // check if all tuples are within the range
        // of the domain and if so set up a faster validity checker
        // that avoids checking original bounds first
        for (int i = 0; i < tt.length; i++) {
            for (int j = 0; j < tt[i].length; j++) {
                int lb = vars[j].getLB();
                int ub = vars[j].getUB();
                if (lb > tt[i][j] ||
                        ub < tt[i][j]) {
                    fastValidCheckAllowed = false;
                }
                if (lb < 0 || ub > 1) {
                    fastBooleanValidCheckAllowed = false;
                }
            }
            if (!fastBooleanValidCheckAllowed &&
                    !fastValidCheckAllowed) break;
        }
        if (fastBooleanValidCheckAllowed) {
            valcheck = new FastBooleanValidityChecker(arity, vars);
        } else if (fastValidCheckAllowed) {
            valcheck = new FastValidityChecker(arity, vars);
        } else valcheck = new ValidityChecker(arity, vars);
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return EventType.INSTANTIATE.mask + EventType.REMOVE.mask;
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

    /*@Override
    public ESat isEntailed() {
        if (isCompletelyInstantiated()) {
            int[] tuple = new int[vars.length];
            for (int i = 0; i < vars.length; i++) {
                tuple[i] = vars[i].getValue();
            }
            return ESat.eval(relation.isConsistent(tuple));
        }
        return ESat.UNDEFINED;
    }*/
    @Override
    public ESat isEntailed() {
        if (isCompletelyInstantiated()) {
            int[] tuple = new int[vars.length];
            for (int i = 0; i < vars.length; i++) {
                tuple[i] = vars[i].getValue();
            }

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
                if (isValid) return ESat.TRUE;
            }
            return ESat.FALSE;

        }
        return ESat.UNDEFINED;
    }

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
     *
     * @param idxSupport
     * @return the residual support
     */
    protected void setSupport(final int idxSupport) {
        int[] tuple = relation.getTuple(idxSupport);
        for (int i = 0; i < tuple.length; i++) {
            supports[i][tuple[i] - offsets[i]] = idxSupport;
        }
    }

    /**
     * @param indexVar
     * @param value    with offset removed
     * @return the residual support
     */
    protected int getSupport(final int indexVar, final int value) {
        return supports[indexVar][value - offsets[indexVar]];
    }

    /**
     * updates the support for all values in the domain of variable
     * and remove unsupported values for variable
     *
     * @param indexVar
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
}
