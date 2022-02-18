/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.sat;

import gnu.trove.list.TIntList;
import org.chocosolver.sat.Literalizer;
import org.chocosolver.sat.MiniSat;
import org.chocosolver.sat.SatDecorator;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.util.ESat;

import java.util.ArrayList;

/**
 * A propagator to deal with clauses and interface a {@link MiniSat}.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 12/07/13
 */
public class PropSat extends Propagator<Variable> {

    /**
     * The SAT solver
     */
    private final SatDecorator sat_;

    /**
     * Store new added variables when {@link #initialized} is <i>false</i>
     */
    private final ArrayList<Variable> add_var;

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
        // adds solver.ONE to fit to the super constructor
        super(new Variable[]{model.getNbVars() > 0 ? model.getVar(0) : model.boolVar(false)},
                PropagatorPriority.VERY_SLOW, true);
        // erase model.ONE from the variable scope
        this.vars = new Variable[0];
        sat_ = new SatDecorator(model);
        add_var = new ArrayList<>(16);
    }


    @Override
    public void propagate(int evtmask) throws ContradictionException {
        initialize();
        if (!sat_.ok_) fails();
        sat_.cancelUntil(0);
        sat_.storeEarlyDeductions();
        sat_.applyEarlyDeductions(this);
        for (int i = 0; i < vars.length; ++i) {
            doBound(i);
        }
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        doBound(idxVarInProp);
    }

    protected void doBound(int i) throws ContradictionException {
        sat_.bound(vars[i], this);
    }

    @Override
    public ESat isEntailed() {
        if (isCompletelyInstantiated()) {
            return ESat.eval(sat_.clauseEntailed(sat_.clauses) && sat_.clauseEntailed(sat_.dynClauses));
        }
        return ESat.UNDEFINED;
    }

    /**
     * @return the underlying SAT solver
     */
    public MiniSat getMiniSat() {
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
                addVariable(add_var.toArray(new Variable[0]));
            }
            add_var.clear();
            this.initialized = true;
        }
    }

    /**
     * Creates, or returns if already existing, the SAT variable corresponding to this CP variable.
     *
     * @param expr a boolean variable
     * @return its SAT twin
     */
    public int makeBool(BoolVar expr) {
        return sat_.bind(expr,
                new Literalizer.BoolLit(expr),
                this::lazyAddVar);
    }

    /**
     * Creates, or returns if already existing, the SAT variable corresponding
     * to the relationship {@code var = val}.
     *
     * @param var an integer variable
     * @param val an integer
     * @return its SAT twin
     */
    public int makeIntEq(IntVar var, int val) {
        return sat_.bind(var,
                new Literalizer.IntEqLit(var, val),
                this::lazyAddVar);
    }

    /**
     * Creates, or returns if already existing, the SAT variable corresponding
     * to the relationship {@code var} &le; {@code val}.
     *
     * @param var an integer variable
     * @param val an integer
     * @return its SAT twin
     */
    public int makeIntLe(IntVar var, int val) {
        return sat_.bind(var,
                new Literalizer.IntLeLit(var, val),
                this::lazyAddVar);
    }

    /**
     * Creates, or returns if already existing, the SAT variable corresponding
     * to the relationship {@code val} &isin; {@code var}.
     *
     * @param var an integer variable
     * @param val an integer
     * @return its SAT twin
     */
    public int makeSetIn(SetVar var, int val) {
        return sat_.bind(var,
                new Literalizer.SetInLit(var, val),
                this::lazyAddVar);
    }

    public void lazyAddVar(Variable var) {
        if (initialized) {
            addVariable(var);
        } else {
            add_var.add(var);
        }
    }

    protected ESat value(int svar) {
        return sat_.value(svar);
    }

    public void beforeAddingClauses() {
        sat_.synchro();
    }

    public void afterAddingClauses() {
        sat_.storeEarlyDeductions();
    }

    /**
     * Add a clause to SAT solver
     *
     * @param lits clause
     * @return <tt>false</tt> if failure is detected
     */
    public boolean addClause(TIntList lits) {
        boolean result = sat_.addClause(lits);
        sat_.storeEarlyDeductions();
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

}
