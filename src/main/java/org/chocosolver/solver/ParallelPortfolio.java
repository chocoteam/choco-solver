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
package org.chocosolver.solver;

import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.search.limits.FailCounter;
import org.chocosolver.solver.search.loop.lns.INeighborFactory;
import org.chocosolver.solver.search.loop.monitors.IMonitorSolution;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.util.criteria.Criterion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.chocosolver.solver.search.strategy.SearchStrategyFactory.*;

/**
 *
 * <p>
 *     A Portfolio helper.
 * </p>
 * <p>
 *     The ParallelPortfolio resolution of a problem is made of four steps:
 *      <ol>
 *          <li>adding models to be run in parallel,</li>
 *          <li>running resolution in parallel,</li>
 *          <li>getting the model which finds a solution (or the best one), if any.</li>
 *      </ol>
 *      Each of the four steps is needed and the order is imposed too.
 *      In particular, in step 1. each model should be populated individually with a model of the problem
 *      (presumably the same model, but not required).
 *      Populating model is not managed by this class and should be done before applying step 2.,
 *      with a dedicated method for instance.
 *      </br>
 *      Note also that there should not be pending resolution process in any models.
 *      Otherwise, unexpected behaviors may occur.
 * </p>
 * <p>
 *     The resolution process is synchronized. As soon as one model ends (naturally or by hitting a limit)
 *     the other ones are eagerly stopped.
 *     Moreover, when dealing with an optimization problem, cut on the objective variable's value is propagated
 *     to all models on solution.
 *     It is essential to eagerly declare the objective variable(s) with {@link Model#setObjective(ResolutionPolicy, Variable)}.
 *
 * </p>
 * <p>
 *     Note that the similarity of the models declared is not required.
 *     However, when dealing with an optimization problem, keep in mind that the cut on the objective variable's value
 *     is propagated among all models, so different objectives may lead to wrong results.
 * </p>
 * <p>
 *     Since there is no condition on the similarity of the models,
 *     once the resolution ends, the model which finds the (best) solution is internally stored.
 * </p>
 * <p>
 *     Example of use.
 *
 * <pre>
 * <code>ParallelPortfolio pares = new ParallelPortfolio();
 * int n = 4; // number of models to use
 * for (int i = 0; i < n; i++) {
 *      pares.addModel(modeller());
 * }
 * pares.solve();
 * IOutputFactory.printSolutions(pares.getBestModel());
 * </code>
 * </pre>
 *
 * </p>
 * <p>
 *     This class uses Java 8 streaming feature, and may be not compliant with older versions.
 * </p>
 *
 *
 * <p>
 * Project: choco.
 * @author Charles Prud'homme, Jean-Guillaume Fages
 * @since 23/12/2015.
 */
public class ParallelPortfolio {

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////       VARIABLES       //////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /** List of {@link Model}s to be executed in parallel. */
    private final List<Model> models;

    /** whether or not to use default search configurations for the different threads **/
    private boolean searchAutoConf;

    /** Stores whether or not prepare() method has been called */
    private boolean isPrepared = false;

    private AtomicBoolean solverTerminated = new AtomicBoolean(false);
    private AtomicBoolean solutionFound = new AtomicBoolean(false);

    /** Point to (one of) the solver(s) which found a solution */
    private Model finder;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////      CONSTRUCTOR      //////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Creates a new ParallelPortfolio
     * This class stores the models to be executed in parallel in a {@link ArrayList} initially empty.
     *
     * @param searchAutoConf changes the search heuristics of the different solvers, except the first one (true by default).
     *                           Must be set to false if search heuristics of the different threads are specified manually, so that they are not erased
     */
    public ParallelPortfolio(boolean searchAutoConf) {
        this.models = new ArrayList<>();
        this.searchAutoConf = searchAutoConf;
    }

    /**
     * Creates a new ParallelPortfolio
     * This class stores the models to be executed in parallel in a {@link ArrayList} initially empty.
     * Search heuristics will be changed automatically (except for the first thread that will remain in the same configuration).
     */
    public ParallelPortfolio() {
        this(true);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////          API          //////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * <p>
     * Adds a model to the list of models to run in parallel.
     * The model can either be a fresh one, ready for populating, or a populated one.
     * </p>
     * <p>
     *     <b>Important:</b>
     *  <ul>
     *      <li>the populating process is not managed by this ParallelPortfolio
     *  and should be done externally, with a dedicated method for example.
     *  </li>
     *  <li>
     *      when dealing with optimization problems, the objective variables <b>HAVE</b> to be declared eagerly with
     *      {@link Model#setObjective(ResolutionPolicy, Variable)}.
     *  </li>
     *  </ul>
     *
     * </p>
     * @param model a model to add
     */
    public void addModel(Model model){
        this.models.add(model);
    }

    /**
     * Run the solve() instruction of every model of the portfolio in parallel.
     *
     * <p>
     * Note that a call to {@link #getBestModel()} returns a model which has found the best solution.
     * </p>
     * @return <code>true</code> if and only if at least one new solution has been found.
     * @throws SolverException if no model or only model has been added.
     */
    public boolean solve() {
        getSolverTerminated().set(false);
        getSolutionFound().set(false);
        if (!isPrepared) {
            prepare();
        }
        ForkJoinPool forkJoinPool = new ForkJoinPool(models.size());
        try {
            forkJoinPool.submit(() -> {
                models.parallelStream().forEach(m -> {
                    if (!getSolverTerminated().get()) {
                        boolean so = m.getSolver().solve();
                        if (so && finder == m || !so) {
                            getSolverTerminated().set(true);
                        }
                    }
                });
            }).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        forkJoinPool.shutdownNow();
        getSolverTerminated().set(false);// otherwise, solver.isStopCriterionMet() always returns true
        if(getSolutionFound().get() && models.get(0).getResolutionPolicy()!=ResolutionPolicy.SATISFACTION) {
            int bestAll = getBestModel().getSolver().getBestSolutionValue().intValue();
            for (Model m : models) {
                int mVal = m.getSolver().getBestSolutionValue().intValue();
                if (m.getResolutionPolicy() == ResolutionPolicy.MAXIMIZE) {
                    assert mVal <= bestAll : mVal + " > " + bestAll;
                } else if (m.getResolutionPolicy() == ResolutionPolicy.MINIMIZE) {
                    assert mVal >= bestAll : mVal + " < " + bestAll;
                }
            }
        }
        return getSolutionFound().get();
    }

    /**
     * Returns the first model from the list which, either :
     * <ul>
     *     <li>
     *         finds a solution when dealing with a satisfaction problem,
     *     </li>
     *     <li>
     *         or finds (and possibly proves) the best solution when dealing with an optimization problem.
     *     </li>
     * </ul>
     * or <tt>null</tt> if no such model exists.
     * Note that there can be more than one "finder" in the list, yet, this method returns the index of the first one.
     *
     * @return the first model which finds a solution (or the best one) or <tt>null</tt> if no such model exists.
     */
    public Model getBestModel(){
        return finder;
    }

    /**
     * @return the (mutable!) list of models used in this ParallelPortfolio
     */
    public List<Model> getModels(){
        return models;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////   INTERNAL METHODS    //////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @SuppressWarnings("unchecked")
    private void prepare(){
        isPrepared = true;
        check();
        for(int i=0;i<models.size();i++){
            Solver s = models.get(i).getSolver();
            s.addStopCriterion((Criterion) () -> getSolverTerminated().get());
            s.plugMonitor((IMonitorSolution) () -> {updateFromSolution(s.getModel());});
            if(searchAutoConf){
                configureModel(i);
            }
        }
    }

    private synchronized void updateFromSolution(Model m){
        if (m.getResolutionPolicy() == ResolutionPolicy.SATISFACTION) {
            finder = m;
            getSolutionFound().set(true);
        }else{
            int solverVal = ((IntVar)m.getObjective()).getValue();
            int bestVal = m.getSolver().getObjectiveManager().getBestSolutionValue().intValue();
            if(m.getResolutionPolicy()==ResolutionPolicy.MAXIMIZE){
                assert solverVal<=bestVal:solverVal+">"+bestVal;
            }else if(m.getResolutionPolicy()==ResolutionPolicy.MINIMIZE){
                assert solverVal>=bestVal:solverVal+"<"+bestVal;
            }
            if(solverVal == bestVal){
                getSolutionFound().set(true);
                finder = m;
                if (m.getResolutionPolicy() == ResolutionPolicy.MAXIMIZE) {
                    models.forEach(s1 -> s1.getSolver().getObjectiveManager().updateBestLB(bestVal));
                }else {
                    models.forEach(s1 -> s1.getSolver().getObjectiveManager().updateBestUB(bestVal));
                }
            }
        }
    }

    private void configureModel(int workerID) {
        Model worker = getModels().get(workerID);
        Solver solver = worker.getSolver();
        ResolutionPolicy policy = worker.getResolutionPolicy();

        // compute decision variables
        Variable[] varsX;
        boolean customSearch = false;
        if (solver.getStrategy() != null && solver.getStrategy().getVariables().length > 0) {
            varsX = solver.getStrategy().getVariables();
            customSearch = true;
        }else{
            varsX = worker.getVars();
        }
        IntVar[] ivars = new IntVar[varsX.length];
        SetVar[] svars = new SetVar[varsX.length];
        RealVar[] rvars = new RealVar[varsX.length];
        int ki=0,ks=0,kr=0;
        for (Variable aVarsX : varsX) {
            if ((aVarsX.getTypeAndKind() & Variable.INT) > 0) {
                ivars[ki++] = (IntVar) aVarsX;
            } else if ((aVarsX.getTypeAndKind() & Variable.SET) > 0) {
                svars[ks++] = (SetVar) aVarsX;
            } else if ((aVarsX.getTypeAndKind() & Variable.REAL) > 0) {
                rvars[kr++] = (RealVar) aVarsX;
            } else {
                throw new UnsupportedOperationException("unrecognized variable kind " + aVarsX);
            }
        }
        ivars = Arrays.copyOf(ivars, ki);
        svars = Arrays.copyOf(svars, ks);
        rvars = Arrays.copyOf(rvars, kr);

        // set heuristic
        switch (workerID) {
            case 0:
                // original (custom or default)
                break;
            case 1:
                // custom + LC (or default + LC)
                if (customSearch && !solver.getStrategy().getClass().getSimpleName().contains("LastConflict")) {
                    solver.set(lastConflict(solver.getStrategy()));
                }else{
                    solver.set(lastConflict(intVarSearch(ivars)));
                    solver.setGeometricalRestart(ivars.length * 3, 1.1d, new FailCounter(solver.getModel(), 1000), 1000);
                }
                break;
            case 3:
                // default + LC (or input order is already default) + LNS is optim
                if(customSearch) {
                    solver.set(lastConflict(intVarSearch(ivars)));
                    solver.setGeometricalRestart(ivars.length * 3, 1.1d, new FailCounter(solver.getModel(), 1000), 1000);
                }else{
                    solver.set(inputOrderLBSearch(ivars));
                }
                if(policy!=ResolutionPolicy.SATISFACTION){
                    solver.setLNS(INeighborFactory.blackBox(ivars), new FailCounter(solver.getModel(), 1000));
                }
                break;
            case 4:
                // ABS with LC
                solver.set(lastConflict(activityBasedSearch(ivars)));
                solver.setGeometricalRestart(ivars.length * 3, 1.1d, new FailCounter(solver.getModel(), 1000), 1000);
                solver.setNoGoodRecordingFromRestarts();
                if(ks+kr==0) {// plug no goods from solution is ABS is complete
                    solver.setNoGoodRecordingFromSolutions(ivars);
                }
                break;
            default:
                // random search (various seeds) + LNS if optim
                solver.set(lastConflict(randomSearch(ivars,workerID)));
                if(policy!=ResolutionPolicy.SATISFACTION){
                    solver.setLNS(INeighborFactory.blackBox(ivars), new FailCounter(solver.getModel(), 1000));
                }
                break;
        }
        // complete with set default search
        if(ks>0) {
            solver.set(solver.getStrategy(),setVarSearch(svars));
        }
        // complete with real default search
        if(kr>0) {
            solver.set(solver.getStrategy(),realVarSearch(rvars));
        }
    }

    private void check(){
        if (models.size() == 0) {
            throw new SolverException("No model found in the ParallelPortfolio.");
        }
        if(models.get(0).getResolutionPolicy() != ResolutionPolicy.SATISFACTION) {
            Variable objective = models.get(0).getObjective();
            if (objective == null) {
                throw new UnsupportedOperationException("No objective has been defined");
            }
            if ((objective.getTypeAndKind() & Variable.REAL) != 0) {
                throw new UnsupportedOperationException("ParallelPortfolio cannot deal with " +
                        "real variable objective optimization problems");
            }
        }
    }

    private synchronized AtomicBoolean getSolverTerminated(){
        return solverTerminated;
    }

    private synchronized AtomicBoolean getSolutionFound(){
        return solutionFound;
    }
}
