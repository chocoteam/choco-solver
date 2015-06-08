/*
 * Copyright (c) 1999-2015, Ecole des Mines de Nantes
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
package org.chocosolver.solver.constraints.nary.cnf;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.stack.TIntStack;
import gnu.trove.stack.array.TIntArrayStack;
import org.chocosolver.memory.IStateInt;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.explanations.RuleStore;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.util.ESat;

import java.util.Arrays;
import java.util.BitSet;

import static org.chocosolver.solver.constraints.nary.cnf.SatSolver.*;


/**
 * Created by cprudhom on 20/01/15.
 * Project: choco.
 */
public class PropNogoods extends Propagator<IntVar> {

    private static final int NO_ENTRY = Integer.MAX_VALUE;

    SatSolver sat_;

    TIntIntHashMap[] vv2lit; // maintain uniqueness of pair VAR-VAL

    int[] var2pos, lit2pos, lit2val;

    IStateInt sat_trail_;

    TIntList early_deductions_;

    BitSet test_eq;

    TIntStack fp;  // lit instantiated -> fix point

    public PropNogoods(Solver solver) {
        super(new BoolVar[]{solver.ONE()}, PropagatorPriority.VERY_SLOW, true);
        this.vars = new IntVar[0];// erase solver.ONE from the variable scope

        int k = 16;
        this.vv2lit = new TIntIntHashMap[k];//new TIntObjectHashMap<>(16, .5f, NO_ENTRY);
        this.lit2val = new int[k];//new TIntIntHashMap(16, .5f, NO_ENTRY, NO_ENTRY);
        Arrays.fill(lit2val, NO_ENTRY);
        this.lit2pos = new int[k];//new TIntIntHashMap(16, .5f, NO_ENTRY, NO_ENTRY);
        Arrays.fill(lit2pos, NO_ENTRY);
        this.var2pos = new int[k];//new TIntIntHashMap(16, .5f, NO_ENTRY, NO_ENTRY);
        Arrays.fill(var2pos, NO_ENTRY);
        //TODO: one satsolver per solver...
        sat_ = new SatSolver();
        early_deductions_ = new TIntArrayList();
        sat_trail_ = solver.getEnvironment().makeInt();
        test_eq = new BitSet();
        fp = new TIntArrayStack();
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (!sat_.ok_) contradiction(null, "inconsistent");
        fp.clear();
        sat_.cancelUntil(0); // to deal with learnt clauses, only called on coarse grain propagation
        storeEarlyDeductions();
        applyEarlyDeductions();
        TIntIntHashMap map;
        for (int i = 0; i < vars.length; ++i) {
            IntVar var = vars[i];
            for (int k : (map = vv2lit[var.getId()]).keys()) {
                if (var.contains(k)) {
                    if (var.isInstantiated()) {
                        VariableBound(map.get(k), true);
                    }
                } else {
                    VariableBound(map.get(k), false);
                }
            }
        }
        while (fp.size() > 0) {
            VariableBound(fp.pop(), true);
        }
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        fp.clear();
        IntVar var = vars[idxVarInProp];
        TIntIntHashMap map;
        for (int k : (map = vv2lit[var.getId()]).keys()) {
            if (var.contains(k)) {
                if (var.isInstantiated()) {
                    VariableBound(map.get(k), true);
                }
            } else {
                VariableBound(map.get(k), false);
            }
        }
        while (fp.size() > 0) {
            VariableBound(fp.pop(), true);
        }
    }

    @Override
    public ESat isEntailed() {
        if (vars.length == 0) return ESat.TRUE;
        if (isCompletelyInstantiated()) {
            int lit, var, val;
            boolean sign;
            for (int k : sat_.implies_.keys()) {
                sign = sign(negated(k));
                var = var(k);
                IntVar ivar = vars[lit2pos[var]];
                val = lit2val[var];
                if (sign != ivar.contains(val)) {
                    TIntList lits = sat_.implies_.get(k);
                    for (int l : lits.toArray()) {
                        sign = sign(l);
                        var = var(l);
                        ivar = vars[lit2pos[var]];
                        val = lit2val[var];
                        if (sign != ivar.contains(val)) return ESat.FALSE;
                    }
                }
            }
            for (SatSolver.Clause c : sat_.clauses) {
                int cnt = 0;
                for (int i = 0; i < c.size(); i++) {
                    lit = c._g(i);
                    sign = sign(lit);
                    var = var(lit);
                    IntVar ivar = vars[lit2pos[var]];
                    val = lit2val[var];
                    if (sign != ivar.contains(val)) {
                        cnt++;
                    } else break;
                }
                if (cnt == c.size()) return ESat.FALSE;
            }
            for (SatSolver.Clause c : sat_.learnts) {
                int cnt = 0;
                for (int i = 0; i < c.size(); i++) {
                    lit = c._g(i);
                    sign = sign(lit);
                    var = var(lit);
                    IntVar ivar = vars[lit2pos[var]];
                    val = lit2val[var];
                    if (sign != ivar.contains(val)) {
                        cnt++;
                    } else break;
                }
                if (cnt == c.size()) return ESat.FALSE;
            }
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public int Literal(IntVar ivar, int value) {
        // TODO: deal with BoolVar
        int vid = ivar.getId();
        int var;
        TIntIntHashMap map;
        if (vid >= vv2lit.length) {
            TIntIntHashMap[] tmp = vv2lit;
            vv2lit = new TIntIntHashMap[vid + 1];
            System.arraycopy(tmp, 0, vv2lit, 0, tmp.length);

            int[] tmpi = var2pos;
            var2pos = new int[vid + 1];
            System.arraycopy(tmpi, 0, var2pos, 0, tmpi.length);
            Arrays.fill(var2pos, tmpi.length, vid + 1, NO_ENTRY);
        }
        if ((map = vv2lit[vid]) == null) {
            map = new TIntIntHashMap(16, .5f, NO_ENTRY, NO_ENTRY);
            vv2lit[vid] = map;
        }

        int pos;
        if ((pos = var2pos[vid]) == NO_ENTRY) {
            addVariable(ivar);
            pos = vars.length - 1;
            var2pos[vid] = pos;
        }

        if ((var = map.get(value)) == NO_ENTRY) {
            var = sat_.newVariable();
            map.put(value, var);
            if (var >= lit2pos.length) {
                int[] tmp = lit2pos;
                lit2pos = new int[var + 1];
                System.arraycopy(tmp, 0, lit2pos, 0, tmp.length);
                Arrays.fill(lit2pos, tmp.length, var + 1, NO_ENTRY);

                tmp = lit2val;
                lit2val = new int[var + 1];
                System.arraycopy(tmp, 0, lit2val, 0, tmp.length);
                Arrays.fill(lit2val, tmp.length, var + 1, NO_ENTRY);
            }


            lit2pos[var] = pos;
            lit2val[var] = value;
        }
        return SatSolver.makeLiteral(var, true);
    }

    /**
     * var points a clause variable whom value is now to be val.
     *
     * @param var       a clause var
     * @param new_value its value
     * @throws ContradictionException
     */
    void VariableBound(int var, boolean new_value) throws ContradictionException {
        if (sat_trail_.get() < sat_.trailMarker()) {
            sat_.cancelUntil(sat_trail_.get());
            assert (sat_trail_.get() == sat_.trailMarker());
        }
        int lit = makeLiteral(var, new_value);
        boolean fail = !sat_.propagateOneLiteral(lit);
        // Remark: explanations require to instantiated variables even if fail is set to true
        sat_trail_.set(sat_.trailMarker());
        for (int i = 0; i < sat_.touched_variables_.size(); ++i) {
            lit = sat_.touched_variables_.get(i);
            var = var(lit);
            if (sign(lit)) {
                vars[lit2pos[var]].instantiateTo(lit2val[var], this);
            } else {
                vars[lit2pos[var]].removeValue(lit2val[var], this);
                if (vars[lit2pos[var]].isInstantiated()) {
                    IntVar tvar = vars[lit2pos[var]];
                    int value = tvar.getValue();
                    int alit = vv2lit[tvar.getId()].get(value);
                    if (alit != NO_ENTRY) {
                        fp.push(alit);
                    }
                }
            }
        }
        if (fail) {
            // force failure by removing the last value
            IntVar iv = vars[lit2pos[var]];
            if (sign(lit)) {
                iv.removeValue(lit2val[var], this);
            } else {
                iv.instantiateTo(lit2val[var], this);
            }

        }
    }

    // Add a unit clause to the solver.
    public boolean addNogood(int p) {
        boolean result = sat_.addClause(p);
        storeEarlyDeductions();
        return result;
    }

    // Add a nogood to the solver, clears the vector.
    public boolean addNogood(TIntList lits) {
        boolean result = sat_.addClause(lits);
        storeEarlyDeductions();
        return result;
    }

    // Add a learnt clause
    public void addLearnt(int... lits) {
        sat_.learnClause(lits);
        // early deductions of learnt clause may lead to incorrect behavior on backtrack
        // since early deduction is not backtrackable.

        // compare the current clauses with the previous stored one,
        // just in case the current one dominates the previous none
        if (sat_.nLearnt() > 1) {
            SatSolver.Clause last = sat_.learnts.get(sat_.learnts.size() - 1);
            test_eq.clear();
            for (int i = last.size() - 1; i >= 0; i--) {
                test_eq.set(last._g(i));
            }
            for (int c = sat_.learnts.size() - 2; c >= 0; c--) {
                int s = test_eq.cardinality();
                SatSolver.Clause prev = sat_.learnts.get(c);
                if (last.size() > 1 && last.size() < prev.size()) {
                    for (int i = prev.size() - 1; i >= 0; i--) {
                        s -= test_eq.get(prev._g(i)) ? 1 : 0;
                    }
                    if (s == 0) { // then last dominates prev
                        sat_.detachLearnt(c);
                    }
                }
            }
        }
    }


    private void storeEarlyDeductions() {
        for (int i = 0; i < sat_.touched_variables_.size(); ++i) {
            int lit = sat_.touched_variables_.get(i);
            early_deductions_.add(lit);
        }
        sat_.touched_variables_.clear();
    }

    void applyEarlyDeductions() throws ContradictionException {
        for (int i = 0; i < early_deductions_.size(); ++i) {
            int lit = early_deductions_.get(i);
            int var = var(lit);
            if (sign(lit)) {
                vars[lit2pos[var]].instantiateTo(lit2val[var], this);
            } else {
                vars[lit2pos[var]].removeValue(lit2val[var], this);
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public boolean why(RuleStore ruleStore, IntVar ivar, IEventType evt, int ivalue) {
        boolean newrules = ruleStore.addPropagatorActivationRule(this);
        // When we got here, there are multiple cases:
        // 1. the propagator fails, at least one clause or implication cannot be satisfied
        // 2. the propagator is the cause of an instantiation
        // but the clauses and implications may be lost (cf. propagate)

        // get the index of the variable in the sat solver
        int var = vv2lit[ivar.getId()].get(ivalue);
        boolean new_value = true;
        if (!ivar.contains(ivalue)) {
            new_value = false;
        }
        int lit = makeLiteral(var, new_value);
        int neg = negated(lit);
        // A. implications:
        // simply iterate over implies_ and add the instantiated variables
        TIntList implies = sat_.implies_.get(lit);
        if (implies != null) {
            for (int i = implies.size() - 1; i >= 0; i--) {
                int l = implies.get(i);
                newrules |= _why(l, ruleStore);
            }
        }

        // B. clauses:
        // We need to find the fully instantiated clauses where bvar appears
        // we cannot rely on watches_ because is not backtrackable
        // So, we iterate over clauses where the two first literal are valued AND which contains bvar
        for (int k = sat_.nClauses() - 1; k >= 0; k--) {
            newrules |= _why(neg, lit, sat_.clauses.get(k), ruleStore);
        }
        // C. learnt clauses:
        // We need to find the fully instantiated clauses where bvar appears
        // we cannot rely on watches_ because is not backtrackable
        // So, we iterate over clauses where the two first literal are valued AND which contains bvar
        for (int k = sat_.nLearnt() - 1; k >= 0; k--) {
            newrules |= _why(neg, lit, sat_.learnts.get(k), ruleStore);
        }
        return newrules;
    }

    private boolean _why(int neg, int lit, SatSolver.Clause cl, RuleStore ruleStore) {
        boolean newrules = false;
        // if the variable watches
        if (cl._g(0) == neg || cl._g(0) == lit || cl._g(1) == neg || cl._g(1) == lit) {
            for (int d = cl.size() - 1; d >= 0; d--) {
                newrules |= _why(cl._g(d), ruleStore);
            }
        } else
            // if the watched literals are instantiated
            if (litIsKnown(cl._g(0)) && litIsKnown(cl._g(1))) {
                // then, look for the lit
                int p = cl.pos(neg);
                int q = cl.pos(lit);
                if (p > -1 || q > -1) { // we found a clause where neg is in
                    for (int d = cl.size() - 1; d >= 0; d--) {
                        newrules |= _why(cl._g(d), ruleStore);
                    }
                }
            }
        return newrules;
    }

    private boolean _why(int l, RuleStore ruleStore) {
        int _var = var(l);
        IntVar avar = vars[lit2pos[_var]];
        int aval = lit2val[_var];
        if (avar.contains(aval)) {
            if (avar.isInstantiated()) {
                return ruleStore.addFullDomainRule(avar);
            }
        } else {
            return ruleStore.addRemovalRule(avar, aval);
        }
        return false;
    }


    private boolean litIsKnown(int l) {
        int _var = var(l);
        IntVar avar = vars[lit2pos[_var]];
        int aval = lit2val[_var];
        return !avar.contains(aval) || avar.isInstantiated();
    }
}
