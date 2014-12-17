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
import org.chocosolver.solver.explanations.Deduction;
import org.chocosolver.solver.explanations.Explanation;
import org.chocosolver.solver.explanations.ExplanationEngine;
import org.chocosolver.solver.explanations.VariableState;
import org.chocosolver.solver.explanations.arlil.RuleStore;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;

import java.util.ArrayList;

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
        sat_.initPropagator();
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
            for (SatSolver.Clause c : sat_.clauses) {
                int cnt = 0;
                for (int i = 0; i < c.size(); i++) {
                    int lit = c._g(i);
                    boolean sign = sign(lit);
                    int var = var(lit);
                    int val = vars[var].getValue();
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
    public void explain(ExplanationEngine xengine, Deduction d, Explanation e) {
        e.add(xengine.getPropagatorActivation(this));
        int idx = indices_.get(d.getVar());
        boolean new_value = ((BoolVar) (d.getVar())).getValue() != 0;
        int lit = makeLiteral(idx, new_value);
        TIntList implies = sat_.implies_.get(lit);
        if (implies != null) {
            for (int i = 0; i < implies.size(); ++i) {
                int l = implies.get(i);
                if (sat_.valueLit(l) != SatSolver.Boolean.kUndefined) {
                    vars[var(l)].explain(xengine, VariableState.DOM, e);
                }
            }
        }
        ArrayList<SatSolver.Watcher> watchers = sat_.watches_.get(lit);
        if (watchers != null) {
            for (SatSolver.Watcher w : watchers) {
                int c = 0;
                // look for all complete causes, incomplete are not interesting
                while (c < w.clause.size() && sat_.valueLit(c) != SatSolver.Boolean.kUndefined) {
                    c++;
                }
                if (c == w.clause.size()) {
                    for (int l = 0; l < w.clause.size(); l++) {
                        vars[var(l)].explain(xengine, VariableState.DOM, e);
                    }
                }
            }
        }
    }

    @Override
    public boolean why(RuleStore ruleStore, IntVar var, IEventType evt, int value) {
        boolean newrules = ruleStore.addPropagatorActivationRule(this);
        int idx = indices_.get(var);
        boolean new_value = var.getValue() != 0;
        int lit = makeLiteral(idx, new_value);
        TIntList implies = sat_.implies_.get(lit);
        if (implies != null) {
            for (int i = 0; i < implies.size(); ++i) {
                int l = implies.get(i);
                if (sat_.valueLit(l) != SatSolver.Boolean.kUndefined) {
                    newrules |= ruleStore.addFullDomainRule(vars[var(l)]);
                }
            }
        }
        ArrayList<SatSolver.Watcher> watchers = sat_.watches_.get(lit);
        if (watchers != null) {
            for (SatSolver.Watcher w : watchers) {
                int c = 0;
                // look for all complete causes, incomplete are not interesting
                while (c < w.clause.size() && sat_.valueLit(c) != SatSolver.Boolean.kUndefined) {
                    c++;
                }
                if (c == w.clause.size()) {
                    for (int l = 0; l < w.clause.size(); l++) {
                        newrules |= ruleStore.addFullDomainRule(vars[var(l)]);
                    }
                }
            }
        }
        return newrules;
    }
}
