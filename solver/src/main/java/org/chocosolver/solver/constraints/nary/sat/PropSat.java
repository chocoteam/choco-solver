/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
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
 * A propagator that bridges Constraint Programming (CP) and SAT solving by interfacing with a {@link MiniSat} solver.
 *
 * <p>This propagator enables the integration of SAT solving techniques within Choco-Solver's CP framework.
 * It maintains a {@link SatDecorator} instance that wraps a MiniSat solver, allowing CP constraints to be
 * translated into SAT clauses and vice versa. This hybrid approach leverages the efficiency of SAT solvers
 * for clause propagation and conflict analysis while maintaining the expressive power of CP modeling.
 *
 * <p><b>Primary Use Case:</b> This propagator is <em>not</em> used for lazy clause generation. Its main purpose is to
 * manage <em>nogoods from restarts</em> and <em>nogoods from solutions</em>, providing an efficient mechanism to
 * prevent re-exploring equivalent search subspaces that have already been proven inconsistent or suboptimal.
 *
 * <p>The propagator supports:
 * <ul>
 *   <li>Creation of SAT literals from CP variables (bool, int equality, int less-or-equal, set membership)</li>
 *   <li>Addition of both user-defined and learnt clauses to the SAT solver</li>
 *   <li>Bidirectional propagation between CP and SAT layers</li>
 * </ul>
 *
 * <p>This implementation is based on the MiniSat solver architecture, originally presented in
 * "An Extensible SAT-solver" (SAT 2003). The integration approach follows CP-SAT hybridization techniques
 * described in the constraint programming literature.
 *
 * @author Charles Prud'homme
 * @since 12/07/13
 * @see MiniSat
 * @see SatDecorator
 * @see Propagator
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
                PropagatorPriority.VERY_SLOW, true, false);
        // erase model.ONE from the variable scope
        this.vars = new Variable[0];
        sat_ = new SatDecorator(model, model.getSettings().getSatCCMinMode());
        add_var = new ArrayList<>(16);
    }


    /**
     * Propagates constraints by synchronizing with the underlying SAT solver.
     * <p>
     * First initializes the propagator (adding any pending variables), then checks if the SAT solver
     * is in a consistent state. If a contradiction is detected in the SAT layer, it triggers a failure.
     * Otherwise, it clears the SAT solver's trail, stores early deductions from the SAT solver,
     * applies those deductions to the CP layer, and propagates bounds for all variables.
     *
     * @param evtmask the propagation event mask
     * @throws ContradictionException if the SAT solver detects a contradiction
     */
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

    /**
     * Propagates bound changes for a specific variable to the SAT solver.
     * <p>
     * Called when a variable in the propagator's scope has changed. Delegates to {@link #doBound(int)}
     * to update the corresponding SAT literal bounds.
     *
     * @param idxVarInProp the index of the variable in this propagator's variable array
     * @param mask the propagation event mask
     * @throws ContradictionException if a contradiction is detected during bound propagation
     */
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
            return ESat.eval(sat_.clauseEntailed(sat_.clauses) && sat_.clauseEntailed(sat_.getLearnts()));
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
            if (!add_var.isEmpty()) {
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

    /**
     * Lazily adds a variable to this propagator's scope.
     * <p>
     * If the propagator is already initialized, the variable is added immediately via {@link #addVariable(Variable[])}.
     * Otherwise, the variable is stored in a temporary list and will be added when {@link #initialize()}
     * is called. This allows variables to be added before the propagator is fully initialized.
     *
     * @param var the variable to add to this propagator's scope
     */
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

    /**
     * Reset the underlying SAT decorator.
     * <p>
     * This method removes all learnt clauses and literals from the SAT solver.
     */
    public void reset() {
        sat_.reset();
    }


}
