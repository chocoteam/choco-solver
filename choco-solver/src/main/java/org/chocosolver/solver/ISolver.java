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
package org.chocosolver.solver;

import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.nary.cnf.SatConstraint;
import org.chocosolver.solver.constraints.nary.nogood.NogoodConstraint;
import org.chocosolver.solver.search.measure.IMeasures;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.util.ESat;

/**
 * An interface to deal with various kind of solvers available.
 * It declares the common methods to declare a problem and to solve it.
 * <p>
 * Created by cprudhom on 02/06/15.
 * Project: choco.
 */
public interface ISolver {

    /**
     * Link a variable to a solver.
     * This is executed AUTOMATICALLY in variable constructor, so no checked are done on multiple occurrences of
     * the very same variable.
     *
     * @param variable a newly created variable, not already added
     */
    void associates(Variable variable);

    /**
     * Post permanently a constraint <code>c</code> in the constraints network of a solver:
     * - add it to the data structure,
     * - set the fixed idx,
     * - checks for restrictions
     *
     * @param c a Constraint
     */
    void post(Constraint c);

    /**
     * Post constraints <code>cs</code> permanently in the constraints network of a solver:
     * - add them to the data structure,
     * - set the fixed idx,
     * - checks for restrictions
     *
     * @param cs Constraints
     */
    void post(Constraint... cs);

    /**
     * Return a reference to the measures recorder.
     * This enables to get, for instance, the number of solutions found, time count, etc.
     */
    public IMeasures getMeasures();

    /**
     * Return a constraint embedding a minisat solver.
     * It is highly recommended that there is only once instance of this constraint in a solver.
     * So a call to this method will create and post the constraint if it does not exist.
     *
     * @return the minisat constraint
     */
    public SatConstraint getMinisat();

    /**
     * Return a constraint embedding a nogood store (based on a sat solver).
     * It is highly recommended that there is only once instance of this constraint in a solver.
     * So a call to this method will create and post the constraint if it does not exist.
     *
     * @return the minisat constraint
     */
    public NogoodConstraint getNogoodStore();

    /**
     * Override the default search strategies to use in the solver.
     * In case many strategies are given, they will be called in sequence:
     * The first strategy in parameter is first called to compute a decision, if possible.
     * If it cannot provide a new decision, the second strategy is called ...
     * and so on, until the last strategy.
     * <p>
     *
     * @param strategies the search strategies to use.
     */
    public void set(AbstractStrategy... strategies);

    /**
     * Returns information on the feasibility of the current problem defined by the solver.
     * <p>
     * Possible back values are:
     * <br/>- {@link org.chocosolver.util.ESat#TRUE}: a solution has been found,
     * <br/>- {@link org.chocosolver.util.ESat#FALSE}: the CSP has been proven to have no solution,
     * <br/>- {@link org.chocosolver.util.ESat#UNDEFINED}: no solution has been found so far (within given limits)
     * without proving the unfeasibility, though.
     *
     * @return an {@link org.chocosolver.util.ESat}.
     */
    public ESat isFeasible();

    /**
     * Returns information on the completeness of the search process.
     * <p>
     * A call to {@link #isFeasible()} may provide complementary information.
     * <p>
     * Possible back values are:
     * <p>
     * <br/>- <code>false</code> : the resolution is complete and
     * <br/>&nbsp;&nbsp;&nbsp;* {@link #findSolution()}: a solution has been found or the CSP has been proven to be unsatisfiable.
     * <br/>&nbsp;&nbsp;&nbsp;* {@link #nextSolution()}: a new solution has been found, or no more solutions exist.
     * <br/>&nbsp;&nbsp;&nbsp;* {@link #findAllSolutions()}: all solutions have been found, or the CSP has been proven to be unsatisfiable.
     * <br/>&nbsp;&nbsp;&nbsp;* {@link #findOptimalSolution(ResolutionPolicy, org.chocosolver.solver.variables.IntVar)}: the optimal solution has been found and
     * proven to be optimal, or the CSP has been proven to be unsatisfiable.
     * <br/>- <code>true</code>: the resolution stopped after reaching a limit.
     */
    boolean hasReachedLimit();

    /**
     * Attempts to find the first solution of the declared problem.
     * Then, following solutions can be found using {@link #nextSolution()}.
     * <p>
     * An alternative is to call {@link #isFeasible()} which tells, whether or not, a solution has been found.
     *
     * @return <code>true</code> if and only if a solution has been found.
     */
    boolean findSolution();

    /**
     * Once {@link ISolver#findSolution()} has been called once, other solutions can be found using this method.
     * <p>
     * The search is then resumed to the last found solution point.
     *
     * @return a boolean stating whereas a new solution has been found (<code>true</code>), or not (<code>false</code>).
     */
    boolean nextSolution();

    /**
     * Attempts to find all solutions of the declared problem.
     *
     * @return the number of found solutions.
     */
    long findAllSolutions();

    /**
     * Attempts optimize the value of the <code>objective</code> variable w.r.t. to the optimization <code>policy</code>.
     * Restores the best solution found so far (if any)
     *
     * @param policy    optimization policy, among ResolutionPolicy.MINIMIZE and ResolutionPolicy.MAXIMIZE
     * @param objective the variable to optimize
     */
    void findOptimalSolution(ResolutionPolicy policy, IntVar objective);

    /**
     * Return the front-end solver, required to model problem.
     */
    Solver _fes_();
}
