/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.sat;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import org.chocosolver.memory.IStateInt;
import org.chocosolver.sat.SatSolver;
import org.chocosolver.sat.SatSolver.*;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;

import java.util.ArrayList;

import static org.chocosolver.sat.SatSolver.*;

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
    private SatSolver sat_;

    /**
     * Map between BoolVar and its literal
     */
    private TObjectIntHashMap<BoolVar> indices_;

    /**
     * For comparison with SAT solver trail, to deal properly with backtrack
     */
    private IStateInt sat_trail_;

    /**
     *  List of early deduction literals
     */
    private TIntList early_deductions_;

    /**
     * Local-like parameter, for #why() method only, lazily initialized.
     */
    private TIntObjectHashMap<ArrayList<Clause>> inClauses;

    /**
     * Store new added variables when {@link #initialized} is <i>false</i>
     */
    private ArrayList<BoolVar> add_var;

    /**
     * Indicates if this is initialized or not
     */
    private boolean initialized = false;

    /**
     * Create a (unique) propagator for clauses recording and propagation.
     *
     * @param model the solver that declares the propagator
     */
    public PropSat(Model model) {
        // this propagator initially has no variable
        super(new BoolVar[]{model.boolVar(true)}, PropagatorPriority.VERY_SLOW, true);// adds solver.ONE to fit to the super constructor
        this.vars = new BoolVar[0];    // erase model.ONE from the variable scope

        this.indices_ = new TObjectIntHashMap<>(16,.5f, -1);
        sat_ = new SatSolver();
        early_deductions_ = new TIntArrayList();
        sat_trail_ = model.getEnvironment().makeInt();
        add_var = new ArrayList<>(16);
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return IntEventType.instantiation();
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        initialize();
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
    private boolean clauseEntailed(ArrayList<Clause> clauses) {
        int lit, var, val;
        boolean sign;
        for (Clause c : clauses) {
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
     * Initializes this propagator
     */
    public void initialize() {
        if (!initialized) {
            if (add_var.size() > 0) {
                addVariable(add_var.toArray(new BoolVar[0]));
            }
            add_var.clear();
            this.initialized = true;
        }
    }

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
    public int makeVar(BoolVar expr) {
        int var = indices_.get(expr);
        if (var == -1) {
            var = sat_.newVariable();
            assert (vars.length + add_var.size() == var);
            if(initialized) {
                addVariable(expr);
            }else {
                add_var.add(expr);
            }
            indices_.put(expr, var);
        }
        return var;
    }


    /**
     * Creates, or returns if already existing, the literal corresponding to :
     * <p>
     * <code>expr</code> is <tt>true</tt>
     * <p>
     * The negation of the literal is managed outside.
     *
     * @param expr a boolean variable
     * @param sign true for even
     * @return its literal
     */
    public int makeLiteral(BoolVar expr, boolean sign) {
        return SatSolver.makeLiteral(makeVar(expr), sign);
    }

    /**
     * The value of the <code>index</code>^th literal is known.
     *
     * @param index position of the literal
     * @throws ContradictionException if inconsistency is detected
     */
    private void VariableBound(int index) throws ContradictionException {
        try {
            if (sat_trail_.get() < sat_.trailMarker()) {
                sat_.cancelUntil(sat_trail_.get());
                assert (sat_trail_.get() == sat_.trailMarker());
            }
            int var = index;
            boolean sign = vars[index].getValue() != 0;
            int lit = SatSolver.makeLiteral(var, sign);
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


    public void beforeAddingClauses(){
        if (sat_trail_.get() < sat_.trailMarker()) {
            sat_.cancelUntil(sat_trail_.get());
            assert (sat_trail_.get() == sat_.trailMarker());
        }
    }

    public void afterAddingClauses(){
        storeEarlyDeductions();
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
     * Add learnt clause to SAT solver
     *
     * @param lits clause
     */
    public void addLearnt(int... lits) {
        sat_.learnClause(lits);
        forcePropagationOnBacktrack(); // issue#327
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

    private void applyEarlyDeductions() throws ContradictionException {
        for (int i = 0; i < early_deductions_.size(); ++i) {
            int lit = early_deductions_.get(i);
            int var = var(lit);
            boolean assigned_bool = sign(lit);
//            demons_[var.value()].inhibit(solver());
            vars[var].instantiateTo(assigned_bool ? 1 : 0, this);
        }
    }
}
