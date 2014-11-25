/**
 *  Copyright (c) 1999-2014, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.solver;

import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.thread.AbstractParallelMaster;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;

/**
 * A MasterSolver which enables multi-thread resolution.
 * The main idea of that class is to solve the same problem
 * with various search strategies, and to share few possible information.
 * A problem declared in a solver is duplicated into solvers.
 * Each of them is then configured and run into a single thread.
 * On satisfaction problem, the first solver who finds a solution
 * advises the others.
 * On optimisation problem, the best value found so far is shared among
 * all the solvers.
 * <p>
 * <p>
 * The expected ways to solve a problem using MasterSolver is:
 * <pre>
 *     Solver solver = new Solver();
 *     // declare the variables and constraints
 *     // and an optional search strategy
 *     //...
 *     // Then create the master-solver
 *     MasterSolver ms = new MasterSolver();
 *     // duplicate the solver into 4 solvers (1+3)
 *     ms.populate(solver, 3);
 *     // configure the search strategies (optional, but recommended)
 *     ms.configureSearches();
 *     // Finally, solve the problem
 *     ms.findSolution();
 * </pre>
 * <br/>
 *
 * @author Charles Prud'homme
 * @version choco
 * @since 24/10/14
 */
public class MasterSolver extends AbstractParallelMaster<SlaveSolver> {

    /**
     * Pool of solvers to drive
     */
    protected Solver[] solvers = new Solver[0];

    /**
     * State if at least one solver has found a solution
     */
    ESat feasible;

    /**
     * State if all solvers have reached a limit
     */
    boolean limit;

    /**
     * resolution policy defined.
     */
    ResolutionPolicy policy;

    /**
     * Number of solutions found
     */
    int nbSolution;

    /**
     * Value of the objective variable, if any
     */
    int bestValue;

    public MasterSolver() {
        feasible = ESat.FALSE;
        limit = true;
        policy = ResolutionPolicy.SATISFACTION;
        nbSolution = 0;
        bestValue = 0;
    }

    /**
     * Make <code>n-1</code> copies of the current <code>model</code>,
     * <b>the first solver of the array is the one given in parameter</b>.
     * The solvers created are available thanks to the {@code }
     * <b>
     * Thus, there is n solvers in the returned array</b>
     * Note that only the variables and the constraints are duplicated.
     *
     * @param model the model to duplicate
     * @param n     number of of copies to make.
     */
    public void populate(Solver model, int n) {
        solvers = new Solver[n + 1];
        solvers[0] = model;
        for (int i = 1; i < n + 1; i++) {
            solvers[i] = model.duplicateModel();
        }
    }

    /**
     * An alternative to {@link MasterSolver#populate(Solver, int)} where the initial
     * model has already been duplicated.
     * <b>The first solver in the array needs to be the original one (required for optimization problem)</b>
     *
     * @param solvers the set of solvers to drive.
     */
    public void declare(Solver... solvers) {
        this.solvers = solvers;
    }


    /**
     * Return the solvers to drive
     *
     * @return the array of solvers
     */
    public Solver[] getSolvers() {
        return solvers;
    }

    /**
     * Declare a specific {@link org.chocosolver.solver.Settings} to each solver.
     * Calling this method is highly recommended to, at least, configure the search strategies for each solver.
     *
     * @param settings array of settings
     */
    public void declareSettings(Settings... settings) {
        assert settings.length == solvers.length;
        for (int i = 0; i < settings.length; i++) {
            solvers[i].set(settings[i]);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// RELATED TO RESOLUTION //////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

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
    public ESat isFeasible() {
        if (nbSolution > 0) return ESat.TRUE;
        else if (limit) return ESat.UNDEFINED;
        else return ESat.FALSE;
    }

    /**
     * Returns information on the completeness of the search process.
     * <p>
     * A call to {@link #isFeasible()} may provide complementary information.
     * <p>
     * Possible back values are:
     * <p>
     * <br/>- <code>false</code> : the resolution is complete and
     * <br/>&nbsp;&nbsp;&nbsp;* {@link #findSolution()}: a solution has been found or the CSP has been proven to be unsatisfiable.
     * <br/>&nbsp;&nbsp;&nbsp;* {@link #findOptimalSolution(ResolutionPolicy, IntVar)} : the optimal solution has been found and
     * proven to be optimal, or the CSP has been proven to be unsatisfiable.
     * <br/>- <code>true</code>: the resolution stopped after reaching a limit.
     */
    public boolean hasReachedLimit() {
        return limit;
    }

    /**
     * Attempts to find the first solution of the declared problem, running all solvers declared.
     * The first one which finds a solution stops the process.
     *
     * @return <code>true</code> if and only if a solution has been found.
     */
    public boolean findSolution() {
        this.policy = ResolutionPolicy.SATISFACTION;
        this.slaves = new SlaveSolver[solvers.length];
        for (int i = 0; i < solvers.length; i++) {
            this.slaves[i] = new SlaveSolver(this, i, solvers[i]);
        }
        this.distributedSlavery();
        return nbSolution > 0;
    }


    /**
     * Attempts optimize the value of the <code>objective</code> variable w.r.t. to the optimization <code>policy</code>.
     * Restores the best solution found so far (if any)
     *
     * @param policy    optimization policy, among ResolutionPolicy.MINIMIZE and ResolutionPolicy.MAXIMIZE
     * @param objective the variable to optimize, the variable must be declared in solvers[0].
     */
    public void findOptimalSolution(ResolutionPolicy policy, IntVar objective) {
        this.policy = policy;
        int oidx = findIndexOfObjective(solvers[0], objective);
        if (oidx == solvers[0].getNbVars()) {
            throw new SolverException(objective.getName() + " cannot be found in the first solver, as expected");
        }
        this.slaves = new SlaveSolver[solvers.length];
        for (int i = 0; i < solvers.length; i++) {
            this.slaves[i] = new SlaveSolver(this, i, solvers[i], policy, (IntVar) solvers[i].getVar(oidx));
        }
        this.distributedSlavery();
    }

    /**
     * Return the index of the objective variable within the variables of <code>solver</code>
     *
     * @param solver    solver to inspect
     * @param objective the variable to optimize, the variable must be declared in solvers[0].
     * @return index of the objective variable
     */
    private static int findIndexOfObjective(Solver solver, IntVar objective) {
        int idx = 0;
        int n = solver.getNbVars();
        int oid = objective.getId();
        while (idx < n && oid != solver.getVar(idx).getId()) {
            idx++;
        }
        return idx;
    }

    /**
     * A solution of cost val has been found
     * informs slaves that they must find better
     *
     * @param val value of the objective variable
     */
    synchronized boolean onSolution(int val) {
        if (nbSolution == 0) {
            bestValue = val;
        }
        nbSolution++;
        boolean isBetter = false;
        switch (policy) {
            case MINIMIZE:
                if (bestValue > val || nbSolution == 1) {
                    bestValue = val;
                    isBetter = true;
                }
                break;
            case MAXIMIZE:
                if (bestValue < val || nbSolution == 1) {
                    bestValue = val;
                    isBetter = true;
                }
                break;
            case SATISFACTION:
                bestValue = 1;
                isBetter = nbSolution == 1;
                break;
        }
        if (isBetter) {
            for (int i = 0; i < slaves.length; i++) {
                if (slaves[i] != null) {
                    slaves[i].findBetterThan(val, policy);
                }
            }
        }
        return isBetter;
    }

    synchronized void closeWithSuccess() {
        limit = false;
    }

    @Override
    public synchronized void wishGranted() {
        for (SlaveSolver s : slaves) {
            s.stop();
        }
        super.wishGranted();
    }
}
