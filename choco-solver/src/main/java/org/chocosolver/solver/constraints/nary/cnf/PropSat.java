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
package org.chocosolver.solver.constraints.nary.cnf;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TObjectIntHashMap;
import org.chocosolver.memory.IStateInt;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.explanations.RuleStore;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;

import static org.chocosolver.solver.constraints.nary.cnf.SatSolver.*;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 12/07/13
 */
public class PropSat extends Propagator<BoolVar> {

    SatSolver sat_;
    TObjectIntHashMap<BoolVar> indices_;

    IStateInt sat_trail_;

    TIntList early_deductions_;

    public PropSat(Solver solver) {
        // this propagator initially has no variable
        super(new BoolVar[]{solver.ONE}, PropagatorPriority.VERY_SLOW, true);// adds solver.ONE to fit to the super constructor
        this.vars = new BoolVar[0];    // erase solver.ONE from the variable scope

        this.indices_ = new TObjectIntHashMap<>();
        sat_ = new SatSolver();
        early_deductions_ = new TIntArrayList();
        sat_trail_ = solver.getEnvironment().makeInt();
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return IntEventType.instantiation();
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if(!sat_.ok_)contradiction(null, "inconsistent");
        sat_.cancelUntil(0);
        storeEarlyDeductions();
        applyEarlyDeductions();
        for (int i = 0; i < vars.length; ++i) {
            BoolVar var = vars[i];
            if (var.isInstantiated()) {
                VariableBound(i);
            }
        }
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        VariableBound(idxVarInProp);
    }

    @Override
    public ESat isEntailed() {
        if (isCompletelyInstantiated()) {
            int lit, var, val;
            boolean sign;

            for (int k : sat_.implies_.keys()) {
                sign = sign(negated(k));
                var = var(k);
                val = vars[var].getValue();
                if (val == (sign ? 0 : 1)) {
                    TIntList lits = sat_.implies_.get(k);
                    for (int l : lits.toArray()) {
                        sign = sign(l);
                        var = var(l);
                        val = vars[var].getValue();
                        if (val == (sign ? 0 : 1)) return ESat.FALSE;
                    }
                }
            }
            for (SatSolver.Clause c : sat_.clauses) {
                int cnt = 0;
                for (int i = 0; i < c.size(); i++) {
                    lit = c._g(i);
                    sign = sign(lit);
                    var = var(lit);
                    val = vars[var].getValue();
                    if (val == (sign ? 0 : 1)) cnt++; // if the lit is ok
                    else break;
                }
                if (cnt == c.size()) return ESat.FALSE;
            }
            for (SatSolver.Clause c : sat_.learnts) {
                int cnt = 0;
                for (int i = 0; i < c.size(); i++) {
                    lit = c._g(i);
                    sign = sign(lit);
                    var = var(lit);
                    val = vars[var].getValue();
                    if (val == (sign ? 0 : 1)) cnt++; // if the lit is ok
                    else break;
                }
                if (cnt == c.size()) return ESat.FALSE;
            }
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }

    public SatSolver getSatSolver() {
        return sat_;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public int Literal(BoolVar expr) {
        if (indices_.containsKey(expr)) {
            return makeLiteral(indices_.get(expr), true);
        } else {
            int var = sat_.newVariable();
            assert (vars.length == var);
            addVariable(expr);
            indices_.put(expr, var);
            return makeLiteral(var, true);
        }
    }

    void VariableBound(int index) throws ContradictionException {
        if (sat_trail_.get() < sat_.trailMarker()) {
            sat_.cancelUntil(sat_trail_.get());
            assert (sat_trail_.get() == sat_.trailMarker());
        }
        int var = index;
        boolean new_value = vars[index].getValue() != 0;
        int lit = makeLiteral(var, new_value);
        if (!sat_.propagateOneLiteral(lit)) {
            // force failure by removing the last value, required for explanations
            // TODO: inutile dans le cas des implications, peut être remplacé par contradiction(vars[index], "");
            vars[index].instantiateTo(1 - vars[index].getValue(), this);
        } else {
            sat_trail_.set(sat_.trailMarker());
            for (int i = 0; i < sat_.touched_variables_.size(); ++i) {
                lit = sat_.touched_variables_.get(i);
                var = var(lit);
                boolean assigned_bool = sign(lit);
//                demons_[var.value()].inhibit(solver());
                vars[var].instantiateTo(assigned_bool ? 1 : 0, this);
            }
        }
    }


    // Add a clause to the solver, clears the vector.
    public boolean addClause(TIntList lits) {
        boolean result = sat_.addClause(lits);
        storeEarlyDeductions();
        return result;
    }

    // Add the empty clause, making the solver contradictory.
    public boolean addEmptyClause() {
        return sat_.addEmptyClause();
    }

    // Add a unit clause to the solver.
    public boolean addClause(int p) {
        boolean result = sat_.addClause(p);
        storeEarlyDeductions();
        return result;
    }

    // Add a binary clause to the solver.
    public boolean addClause(int p, int q) {
        boolean result = sat_.addClause(p, q);
        storeEarlyDeductions();
        return result;
    }

    // Add a ternary clause to the solver.
    public boolean addClause(int p, int q, int r) {
        boolean result = sat_.addClause(p, q, r);
        storeEarlyDeductions();
        return result;
    }

    // Add a learnt clause
    public void addLearnt(int... lits) {
        sat_.learnClause(lits);
        // early deductions of learnt clause may lead to incorrect behavior on backtrack
        // since early deduction is not backtrackable.
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
            boolean assigned_bool = sign(lit);
//            demons_[var.value()].inhibit(solver());
            vars[var].instantiateTo(assigned_bool ? 1 : 0, this);
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static void declareVariable(PropSat sat, BoolVar var) {
        //CHECK(sat.Check(var));
        sat.Literal(var);
    }

    @Override
    public boolean why(RuleStore ruleStore, IntVar bvar, IEventType evt, int bvalue) {
        boolean newrules = ruleStore.addPropagatorActivationRule(this);
        // When we got here, there are multiple cases:
        // 1. the propagator fails, at least one clause or implication cannot be satisfied
        // 2. the propagator is the cause of an instantiation
        // but the clauses and implications may be lost (cf. propagate)

        // get the index of the variable in the sat solver
        int var = indices_.get(bvar);
        boolean new_value = bvar.getValue() != 0;
        int lit = makeLiteral(var, new_value);
        int neg = negated(lit);
        // A. implications:
        // simply iterate over implies_ and add the instantiated variables
        TIntList implies = sat_.implies_.get(neg);
        if (implies != null) {
            for (int i = implies.size() - 1; i >= 0; i--) {
                int l = implies.get(i);
                // if the other variable is instantiated ...
                if (vars[var(l)].isInstantiated()) {
                    // could be : if(sat_.valueLit(l) != SatSolver.Boolean.kUndefined)
                    // but as far as the variable is instantiated, that's enough, and we skip conflict cases
                    newrules |= ruleStore.addFullDomainRule(vars[var(l)]);
                }
            }
        }

        // B. clauses:
        // We need to find the fully instantiated clauses where bvar appears
        // we cannot rely on watches_ because is not backtrackable
        // So, we iterate over clauses where the two first literal are valued AND which contains bvar
        for (int k = sat_.nClauses() - 1; k >= 0; k--) {
            SatSolver.Clause cl = sat_.clauses.get(k);
            // if the watched literals are instantiated
            if (cl._g(0) == neg || cl._g(1) == neg
                    || (vars[var(cl._g(0))].isInstantiated() && vars[var(cl._g(1))].isInstantiated())) {
                // then, look for the lit
                int p = cl.pos(neg);
                if (p > -1) { // we found a clause where neg is in
                    for (int d = cl.size() - 1; d >= 0; d--) {
                        int l = cl._g(d);
                        if (vars[var(l)].isInstantiated()) {
                            newrules |= ruleStore.addFullDomainRule(vars[var(l)]);
                        }
                    }
                }
            }
        }
        // C. learnt clauses:
        // We need to find the fully instantiated clauses where bvar appears
        // we cannot rely on watches_ because is not backtrackable
        // So, we iterate over clauses where the two first literal are valued AND which contains bvar
        for (int k = sat_.nLearnt() - 1; k >= 0; k--) {
            SatSolver.Clause cl = sat_.learnts.get(k);
            // if the watched literals are instantiated
            if (cl._g(0) == neg || cl._g(1) == neg
                    || (vars[var(cl._g(0))].isInstantiated() && vars[var(cl._g(1))].isInstantiated())) {
                // then, look for the lit
                int p = cl.pos(neg);
                if (p > -1) { // we found a clause where neg is in
                    for (int d = cl.size() - 1; d >= 0; d--) {
                        int l = cl._g(d);
                        if (vars[var(l)].isInstantiated()) {
                            newrules |= ruleStore.addFullDomainRule(vars[var(l)]);
                        }
                    }
                }
            }
        }
        return newrules;
    }

}
