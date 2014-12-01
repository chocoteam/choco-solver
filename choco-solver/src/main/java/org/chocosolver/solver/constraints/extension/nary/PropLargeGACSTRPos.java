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

import gnu.trove.list.TIntList;
import gnu.trove.list.linked.TIntLinkedList;
import gnu.trove.map.hash.THashMap;
import org.chocosolver.memory.IStateInt;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.extension.Tuples;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.iterators.DisposableValueIterator;

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
    protected ValidityChecker valcheck;

    /**
     * size of the scope
     */
    protected int arity;

    /**
     * original lower bounds
     */
    protected int[] offsets;

    /**
     * Variables that are not proved to be GAC yet
     */
    protected TIntList futureVars;

    /**
     * Values that have found a support for each variable
     */
    protected BitSet[] gacValues;

    protected int[] nbGacValues;

    /**
     * The backtrackable list of tuples representing the current
     * allowed tuples of the constraint
     */
    protected IStateInt last;
    int[] listuples;


    private PropLargeGACSTRPos(IntVar[] vs, TuplesList relation) {
        super(vs, relation);
        this.arity = vs.length;
        this.futureVars = new TIntLinkedList();
        this.gacValues = new BitSet[arity];
        this.nbGacValues = new int[arity];

        this.offsets = new int[arity];
        for (int i = 0; i < arity; i++) {
            this.offsets[i] = vs[i].getLB();
            this.gacValues[i] = new BitSet(vs[i].getDomainSize());
        }
        listuples = new int[this.relation.getTupleTable().length];
        for (int i = 0; i < listuples.length; i++) {
            listuples[i] = i;
        }
        last = solver.getEnvironment().makeInt(listuples.length - 1);

        int[][] tt = this.relation.getTupleTable();
        boolean fastValidCheckAllowed = true;
        boolean fastBooleanValidCheckAllowed = true;
        // check if all tuples are within the range
        // of the domain and if so set up a faster validity checker
        // that avoids checking original bounds first
        for (int i = 0; i < tt.length; i++) {
            for (int j = 0; j < tt[i].length; j++) {
                int lb = vs[j].getLB();
                int ub = vs[j].getUB();
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

    public PropLargeGACSTRPos(IntVar[] vs, Tuples tuples) {
        this(vs, makeRelation(tuples, vs));
    }

    private static TuplesList makeRelation(Tuples tuples, IntVar[] vars) {
        return new TuplesList(tuples, vars);
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        valcheck.sortvars();
        gacstr();
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        filter();
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void initializeData() {
        //INITIALIZATION
        futureVars.clear();
        for (int i = 0; i < arity; i++) {
            gacValues[i].clear();
            nbGacValues[i] = 0;
            futureVars.add(i);
        }
    }

    public void pruningPhase() throws ContradictionException {
        for (int i = 0; i < futureVars.size(); i++) {
            int vIdx = futureVars.get(i);
            IntVar v = vars[vIdx];
            DisposableValueIterator it3 = v.getValueIterator(true);
            int left = Integer.MIN_VALUE;
            int right = left;
            try {
                while (it3.hasNext()) {
                    int val = it3.next();
                    if (!gacValues[vIdx].get(val - offsets[vIdx])) {
                        if (val == right + 1) {
                            right = val;
                        } else {
                            v.removeInterval(left, right, this);
                            left = right = val;
                        }
                        //                        v.removeVal(val, this, false);
                    }
                }
                v.removeInterval(left, right, this);
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
    public void maintainList(/*int idx*/) {
        int cidx = 0;
        int nLast = last.get();
        while (cidx <= nLast) {
            int idxt = listuples[cidx++];
            int[] tuple = relation.getTuple(idxt);

            if (valcheck.isValid(tuple/*,idx*/)) {
                //extract the supports
                for (int i = 0; i < futureVars.size(); i++) {
                    int vIdx = futureVars.get(i);
                    if (!gacValues[vIdx].get(tuple[vIdx] - offsets[vIdx])) {
                        gacValues[vIdx].set(tuple[vIdx] - offsets[vIdx]);
                        nbGacValues[vIdx]++;
                        if (nbGacValues[vIdx] == vars[vIdx].getDomainSize()) {
                            futureVars.removeAt(i);
                            i--;
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
    public void gacstr() throws ContradictionException {
        initializeData();
        maintainList();
        pruningPhase();
        if (getCartesianProduct() <= last.get() + 1) {
            setPassive();
        }
    }

    public double getCartesianProduct() {
        double cp = 1d;
        for (int i = 0; i < arity; i++) {
            cp *= vars[i].getDomainSize();
        }
        return cp;
    }


    public void filter() throws ContradictionException {
        //sort variables regarding domain sizes to speedup the check !
        valcheck.sortvars();
        gacstr();
        //constAwake(false);
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
            identitymap.put(this, new PropLargeGACSTRPos(aVars, (TuplesList) relation.duplicate()));
        }
    }
}
