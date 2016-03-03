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
package org.chocosolver.solver.constraints.nary.cnf;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import org.chocosolver.memory.IStateInt;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.explanations.RuleStore;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;

import java.util.ArrayList;

import static org.chocosolver.solver.constraints.nary.cnf.SatSolver.*;

/**
 * A propagator to deal with clauses and interface a {@link SatSolver}.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 12/07/13
 */
public class PropSat extends Propagator<BoolVar> {

    /**
     * The SAT solver
     */
    SatSolver sat_;

    /**
     * Map between BoolVar and its literal
     */
    TObjectIntHashMap<BoolVar> indices_;

    /**
     * For comparison with SAT solver trail, to deal properly with backtrack
     */
    IStateInt sat_trail_;

    /**
     *  List of early deduction literals
     */
    TIntList early_deductions_;

    /**
     * Local-like parameter, for #why() method only, lazily initialized.
     */
    TIntObjectHashMap<ArrayList<SatSolver.Clause>> inClauses;

    /**
     * Create a (unique) propagator for clauses recording and propagation.
     *
     * @param model the solver that declares the propagator
     */
    public PropSat(Model model) {
        // this propagator initially has no variable
        super(new BoolVar[]{model.ONE()}, PropagatorPriority.VERY_SLOW, true);// adds solver.ONE to fit to the super constructor
        this.vars = new BoolVar[0];    // erase model.ONE from the variable scope

        this.indices_ = new TObjectIntHashMap<>();
        sat_ = new SatSolver();
        early_deductions_ = new TIntArrayList();
        sat_trail_ = model.getEnvironment().makeInt();
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return IntEventType.instantiation();
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (!sat_.ok_) fails();
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
            int var, val;
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
                        if (val == (sign ? 0 : 1)) {
                            return ESat.FALSE;
                        }
                    }
                }
            }
            boolean OK = clauseEntailed(sat_.clauses);
            OK &= clauseEntailed(sat_.learnts);
            return ESat.eval(OK);
        }
        return ESat.UNDEFINED;
    }

    /**
     * Checks if all clauses from <code>clauses</code> are satisfied
     * @param clauses list of clause
     * @return <tt>true</tt> if all clauses are satisfied, <tt>false</tt> otherwise
     */
    private boolean clauseEntailed(ArrayList<SatSolver.Clause> clauses) {
        int lit, var, val;
        boolean sign;
        for (SatSolver.Clause c : clauses) {
            int cnt = 0;
            for (int i = 0; i < c.size(); i++) {
                lit = c._g(i);
                sign = sign(lit);
                var = var(lit);
                val = vars[var].getValue();
                if (val == (sign ? 0 : 1)) cnt++; // if the lit is ok
                else break;
            }
            if (cnt == c.size()) return false;
        }
        return true;
    }

    /**
     * @return the underlying SAT solver
     */
    public SatSolver getSatSolver() {
        return sat_;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Creates, or returns if already existing, the literal corresponding to :
     * <p>
     * <code>expr</code> is <tt>true</tt>
     * <p>
     * The negation of the literal is managed outside.
     *
     * @param expr a boolean variable
     * @return its literal
     */
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

    /**
     * The value of the <code>index</code>^th literal is known.
     *
     * @param index position of the literal
     * @throws ContradictionException if inconsistency is detected
     */
    void VariableBound(int index) throws ContradictionException {
        try {
            if (sat_trail_.get() < sat_.trailMarker()) {
                sat_.cancelUntil(sat_trail_.get());
                assert (sat_trail_.get() == sat_.trailMarker());
            }
            int var = index;
            boolean sign = vars[index].getValue() != 0;
            int lit = makeLiteral(var, sign);
            boolean fail = !sat_.propagateOneLiteral(lit);
            // Remark: explanations require to instantiated variables even if fail is set to true
            sat_trail_.set(sat_.trailMarker());
            for (int i = 0; i < sat_.touched_variables_.size(); ++i) {
                lit = sat_.touched_variables_.get(i);
                var = var(lit);
                boolean assigned_bool = sign(lit);
                vars[var].instantiateTo(assigned_bool ? 1 : 0, this);
            }
            if (fail) {
//            force failure by removing the last value
                vars[index].instantiateTo(1 - vars[index].getValue(), this);
            }
        }finally {
            sat_.touched_variables_.resetQuick(); // issue#327
        }
    }


    /**
     * Add a clause to SAT solver
     *
     * @param lits clause
     * @return <tt>false</tt> if failure is detected
     */
    public boolean addClause(TIntList lits) {
        boolean result = sat_.addClause(lits);
        storeEarlyDeductions();
        return result;
    }

    /**
     * Add empty clause, make SAT solver fails
     *
     * @return <tt>false</tt>
     */
    public boolean addEmptyClause() {
        return sat_.addEmptyClause();
    }

    /**
     * Add unit clause to SAT solver
     *
     * @param p unit clause
     * @return <tt>false</tt> if failure is detected
     */
    public boolean addClause(int p) {
        boolean result = sat_.addClause(p);
        storeEarlyDeductions();
        return result;
    }

    /**
     * Add binary clause to SAT solver
     *
     * @param p literal
     * @param q literal
     * @return <tt>false</tt> if failure is detected
     */
    public boolean addClause(int p, int q) {
        boolean result = sat_.addClause(p, q);
        storeEarlyDeductions();
        return result;
    }

    /**
     * Add ternary clause to SAT solver
     *
     * @param p literal
     * @param q literal
    * @param r literal
     * @return <tt>false</tt> if failure is detected
     */
    public boolean addClause(int p, int q, int r) {
        boolean result = sat_.addClause(p, q, r);
        storeEarlyDeductions();
        return result;
    }

    /**
     * Add learnt clause to SAT solver
     *
     * @param lits clause
     */
    public void addLearnt(int... lits) {
        sat_.learnClause(lits);
        this.getModel().getSolver().getEngine().propagateOnBacktrack(this); // issue#327
        // early deductions of learnt clause may lead to incorrect behavior on backtrack
        // since early deduction is not backtrackable.
    }

    private void storeEarlyDeductions() {
        for (int i = 0; i < sat_.touched_variables_.size(); ++i) {
            int lit = sat_.touched_variables_.get(i);
            early_deductions_.add(lit);
        }
        sat_.touched_variables_.resetQuick();
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
        if (inClauses == null) {
            fillInClauses();
        }
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
        TIntList implies = sat_.implies_.get(lit);
        if (implies != null) {
            for (int i = implies.size() - 1; i >= 0; i--) {
                newrules |= _why(implies.get(i), ruleStore);
            }
        }
        implies = sat_.implies_.get(neg);
        if (implies != null) {
            for (int i = implies.size() - 1; i >= 0; i--) {
                newrules |= _why(implies.get(i), ruleStore);
            }
        }
        // B. clauses:
        // We need to find the fully instantiated clauses where bvar appears
        ArrayList<SatSolver.Clause> mClauses = inClauses.get(lit);
        if (mClauses != null) {
            for (int i = mClauses.size() - 1; i >= 0; i--) {
                newrules |= _why(mClauses.get(i), ruleStore);
            }
        }
        mClauses = inClauses.get(neg);
        if (mClauses != null) {
            for (int i = mClauses.size() - 1; i >= 0; i--) {
                newrules |= _why(mClauses.get(i), ruleStore);
            }
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

    private void fillInClauses() {
        inClauses = new TIntObjectHashMap<>();
        for (int k = sat_.nClauses() - 1; k >= 0; k--) {
            SatSolver.Clause cl = sat_.clauses.get(k);
            for (int d = cl.size() - 1; d >= 0; d--) {
                int l = cl._g(d);
                ArrayList<SatSolver.Clause> mcls = inClauses.get(l);
                if (mcls == null) {
                    mcls = new ArrayList<>();
                    inClauses.put(l, mcls);
                }
                mcls.add(cl);
            }
        }
    }

    private boolean _why(SatSolver.Clause cl, RuleStore ruleStore) {
        boolean newrules = false;
        // if the variable watches
        if (vars[var(cl._g(0))].isInstantiated() && vars[var(cl._g(1))].isInstantiated()) {
            for (int d = cl.size() - 1; d >= 0; d--) {
                newrules |= _why(cl._g(d), ruleStore);
            }
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
            if (vars[var(cl._g(0))].isInstantiated() && vars[var(cl._g(1))].isInstantiated()) {
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
        return vars[var(l)].isInstantiated() && ruleStore.addFullDomainRule(vars[var(l)]);
    }

}
