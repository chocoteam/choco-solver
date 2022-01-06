/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver;

import org.chocosolver.cutoffseq.LubyCutoffStrategy;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.nary.sat.NogoodStealer;
import org.chocosolver.solver.constraints.real.RealConstraint;
import org.chocosolver.solver.exception.InvalidSolutionException;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.search.limits.FailCounter;
import org.chocosolver.solver.search.loop.lns.INeighborFactory;
import org.chocosolver.solver.search.loop.monitors.IMonitorSolution;
import org.chocosolver.solver.search.loop.monitors.NogoodFromRestarts;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.Variable;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.chocosolver.solver.search.strategy.Search.*;

/**
 * <p>
 * A Portfolio helper.
 * </p>
 * <p>
 * The ParallelPortfolio resolution of a problem is made of four steps:
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
 *     It is essential to eagerly declare the objective variable(s) with {@link Model#setObjective(boolean, Variable)}.
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
 *
 * @author Charles Prud'homme, Jean-Guillaume Fages
 * @since 23/12/2015.
 */
public class ParallelPortfolio {

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////       VARIABLES       //////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * List of {@link Model}s to be executed in parallel.
     */
    private final List<Model> models;

    /**
     * whether or not to use default search configurations for the different threads
     **/
    private final boolean searchAutoConf;

    /**
     * This manager is used to synchronize nogood sharing.
     */
    private NogoodStealer manager = NogoodStealer.NONE;

    /**
     * Stores whether or not prepare() method has been called
     */
    private boolean isPrepared = false;

    /**
     * List of {@link Model}s to be executed in parallel.
     */
    private final HashMap<Model, Boolean> reliableness;

    private final AtomicBoolean solverTerminated = new AtomicBoolean(false);
    private final AtomicBoolean solutionFound = new AtomicBoolean(false);
    private final AtomicInteger solverRunning = new AtomicInteger(0);

    /**
     * Point to (one of) the solver(s) which found a solution
     */
    private Model finder;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////      CONSTRUCTOR      //////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Creates a new ParallelPortfolio
     * This class stores the models to be executed in parallel in a {@link ArrayList} initially empty.
     *
     * @param searchAutoConf changes the search heuristics of the different solvers, except the first one (true by default).
     *                       Must be set to false if search heuristics of the different threads are specified manually, so that they are not erased
     */
    public ParallelPortfolio(boolean searchAutoConf) {
        this.models = new ArrayList<>();
        this.reliableness = new HashMap<>();
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
     * Calling this method will ensure that workers equipped with a restart policy not only
     * record nogoods from themselves (based on {@link NogoodFromRestarts}) but also based on
     * other workers of the portfolio.
     *
     * @implSpec It is assumed that all models in this portfolio are equivalent (ie, each variable has
     * the same ID in each worker).
     */
    public void stealNogoodsOnRestarts() {
        this.manager = new NogoodStealer();
    }

    /**
     * <p>
     * Adds a model to the list of models to run in parallel.
     * The model can either be a fresh one, ready for populating, or a populated one.
     * </p>
     * <p>
     * <b>Important:</b>
     *  <ul>
     *      <li>the populating process is not managed by this ParallelPortfolio
     *  and should be done externally, with a dedicated method for example.
     *  </li>
     *  <li>
     *      when dealing with optimization problems, the objective variables <b>HAVE</b> to be declared eagerly with
     *      {@link Model#setObjective(boolean, Variable)}.
     *  </li>
     *  </ul>
     *
     * </p>
     *
     * @param model a model to add
     */
    public void addModel(Model model) {
        addModel(model, true);
    }

    /**
     * <p>
     * Adds a model to the list of models to run in parallel.
     * The model can either be a fresh one, ready for populating, or a populated one.
     * </p>
     * <p>
     * <b>Important:</b>
     *  <ul>
     *      <li>the populating process is not managed by this {@code ParallelPortfolio}
     *  and should be done externally, with a dedicated method for example.
     *  </li>
     *  <li>
     *      when dealing with optimization problems, the objective variables <b>HAVE</b> to be declared eagerly with
     *      {@link Model#setObjective(boolean, Variable)}.
     *  </li>
     *  </ul>
     *
     * </p>
     * </p>
     * <p>
     *     A reliable model is expected to prove the absence of a solution,
     *     improving one in the case of optimisation problem.
     *     A model with non-redundant constraints posted
     *     to improve resolution at the expense of completeness is considered unreliable.
     *     An unreliable model cannot share its no-goods and when it stops, cannot stop other models.
     * </p>
     * <p>
     *    There should be at least one reliable model in a Portfolio.
     *    Otherwise, solving may be made incomplete.
     * </p>
     *
     * @param model    a model to add
     * @param reliable set to {@code true} if the model is reliable.
     */
    public void addModel(Model model, boolean reliable) {
        this.models.add(model);
        this.reliableness.put(model, reliable);
    }

    /**
     * Run the solve() instruction of every model of the portfolio in parallel.
     *
     * <p>
     * Note that a call to {@link #getBestModel()} returns a model which has found the best solution.
     * </p>
     *
     * @return <code>true</code> if and only if at least one new solution has been found.
     * @throws SolverException if no model or only model has been added.
     */
    public boolean solve() {
        getSolverTerminated().set(false);
        getSolutionFound().set(false);
        getSolverRunning().set(models.size());
        if (!isPrepared) {
            prepare();
        }
        ForkJoinPool forkJoinPool = new ForkJoinPool(models.size());
        try {
            forkJoinPool.submit(() -> models.parallelStream().forEach(m -> {
                if (!getSolverTerminated().get()) {
                    boolean so = m.getSolver().solve();
                    if (!so || finder == m) {
                        getSolverTerminated().set(so || reliableness.get(m) || getSolverRunning().decrementAndGet() <= 0);
                    }
                }
            })).get();
        } catch (InterruptedException | ExecutionException | SolverException e) {
            getSolverRunning().decrementAndGet();
            //If a InvalidSolutionException occurs and at least one model is not reliable
            // the exception may come from this model and should be ignored
            if (e.getCause() instanceof InvalidSolutionException) {
                InvalidSolutionException ex = (InvalidSolutionException) e.getCause();
                if (reliableness.get(ex.getModel())) {
                    throw (SolverException) e.getCause();
                }// else ignore the error
            } else {
                e.printStackTrace();
            }
        }
        forkJoinPool.shutdownNow();
        getSolverTerminated().set(false);// otherwise, solver.isStopCriterionMet() always returns true
        if (getSolutionFound().get() && models.get(0).getResolutionPolicy() != ResolutionPolicy.SATISFACTION) {
            int bestAll = getBestModel().getSolver().getBestSolutionValue().intValue();
            for (Model m : models) {
                int mVal = m.getSolver().getBestSolutionValue().intValue();
                if (m.getResolutionPolicy() == ResolutionPolicy.MAXIMIZE) {
                    assert mVal <= bestAll : mVal + " > " + bestAll;
                } else
                    assert m.getResolutionPolicy() != ResolutionPolicy.MINIMIZE || mVal >= bestAll : mVal + " < " + bestAll;
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
    public Model getBestModel() {
        return finder;
    }

    /**
     * @return the (mutable!) list of models used in this ParallelPortfolio
     */
    public List<Model> getModels() {
        return models;
    }

    /**
     * Attempts to find all solutions of the declared problem.
     * <ul>
     * <li>If the method returns an empty list:</li>
     * <ul>
     * <li>either a stop criterion (e.g., a time limit) stops the search before any solution has been found,</li>
     * <li>or no solution exists for the problem (i.e., over-constrained).</li>
     * </ul>
     * <li>if the method returns a list with at least one element in it:</li>
     * <ul>
     * <li>either the resolution stops eagerly du to a stop criterion before finding all solutions,</li>
     * <li>or all solutions have been found.</li>
     * </ul>
     * </ul>
     * <p>
     * <p>
     * Note that all variables will be recorded
     *
     * @return a list that contained the found solutions.
     */
    public Stream<Solution> streamSolutions() {
        Spliterator<Solution> it = new Spliterator<Solution>() {

            @Override
            public boolean tryAdvance(Consumer<? super Solution> action) {
                if (solve()) {
                    action.accept(new Solution(getBestModel()).record());
                    return true;
                }
                return false;
            }

            @Override
            public Spliterator<Solution> trySplit() {
                return null;
            }

            @Override
            public long estimateSize() {
                return Long.MAX_VALUE;
            }

            @Override
            public int characteristics() {
                return Spliterator.ORDERED | Spliterator.DISTINCT | Spliterator.NONNULL | Spliterator.CONCURRENT;
            }

        };
        return StreamSupport.stream(it, false);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////   INTERNAL METHODS    //////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @SuppressWarnings("unchecked")
    public void prepare() {
        isPrepared = true;
        check();
        for (int i = 0; i < models.size(); i++) {
            Solver s = models.get(i).getSolver();
            s.addStopCriterion(() -> getSolverTerminated().get());
            s.plugMonitor((IMonitorSolution) () -> updateFromSolution(s.getModel()));
            if (searchAutoConf) {
                configureModel(i);
            }
        }
    }

    private synchronized void updateFromSolution(Model m) {
        if (m.getResolutionPolicy() == ResolutionPolicy.SATISFACTION) {
            finder = m;
            getSolutionFound().set(true);
        } else {
            int solverVal = ((IntVar) m.getObjective()).getValue();
            int bestVal = m.getSolver().getObjectiveManager().getBestSolutionValue().intValue();
            if (m.getResolutionPolicy() == ResolutionPolicy.MAXIMIZE) {
                assert solverVal <= bestVal : solverVal + ">" + bestVal;
            } else
                assert
                        m.getResolutionPolicy() != ResolutionPolicy.MINIMIZE || solverVal >= bestVal : solverVal + "<" + bestVal;
            if (solverVal == bestVal) {
                getSolutionFound().set(true);
                finder = m;
                if (m.getResolutionPolicy() == ResolutionPolicy.MAXIMIZE) {
                    models.forEach(s1 -> s1.getSolver().getObjectiveManager().updateBestLB(bestVal));
                } else {
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
        if (solver.getSearch() != null && solver.getSearch().getVariables().length > 0) {
            varsX = solver.getSearch().getVariables();
        } else {
            varsX = worker.getVars();
        }
        IntVar[] ivars = new IntVar[varsX.length];
        SetVar[] svars = new SetVar[varsX.length];
        RealVar[] rvars = new RealVar[varsX.length];
        int ki = 0, ks = 0, kr = 0;
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
        boolean opt = policy != ResolutionPolicy.SATISFACTION;
        switch (workerID) {
            case 0:
                // DWD  + fast restart + LC (+ B2V)
                solver.setSearch(
                        lastConflict(
                                VarH.DOMWDEG.make(solver, ivars, ValH.BEST, Integer.MAX_VALUE, opt)
                        )
                );
                Restarts.LUBY.declare(solver, 500, 0.d, 5000);
                if (reliableness.containsKey(worker)) {
                    manager.add(worker);
                }
                break;
            case 1:
                solver.setSearch(
                        lastConflict(
                                VarH.CHS.make(solver, ivars, ValH.MIN, Integer.MAX_VALUE, opt)
                        )
                );
                Restarts.LUBY.declare(solver, 500, 0.d, 5000);
                if (reliableness.containsKey(worker)) {
                    manager.add(worker);
                }
                break;
            case 2:
                // input order + LC
                solver.setSearch(
                        lastConflict(
                                VarH.INPUT.make(solver, ivars, ValH.MIN, Integer.MAX_VALUE, opt)
                        )
                );
                if (reliableness.containsKey(worker)) {
                    manager.add(worker);
                }
                break;
            case 3:
                if (!opt) {
                    solver.setSearch(
                            lastConflict(
                                    VarH.DOMWDEGR.make(solver, ivars, ValH.MIN, Integer.MAX_VALUE, opt)
                            )
                    );
                    Restarts.LUBY.declare(solver, 500, 0.d, 5000);
                    break;
                } else {
                    // input order + LC + LNS
                    solver.setSearch(
                            lastConflict(
                                    VarH.INPUT.make(solver, ivars, ValH.MIN, Integer.MAX_VALUE, opt)
                            )
                    );
                    solver.setLNS(INeighborFactory.blackBox(ivars), new FailCounter(solver.getModel(), 1000));
                }
                if (reliableness.containsKey(worker)) {
                    manager.add(worker);
                }
                break;
            case 4:
                // ABS  + fast restart + LC
                solver.setSearch(
                        lastConflict(
                                VarH.ABS.make(solver, ivars, ValH.DEFAULT, Integer.MAX_VALUE, opt)
                        )
                );
                Restarts.LUBY.declare(solver, 500, 0.d, 5000);
                break;
            case 5:
                // DWD  + fast restart + COS
                solver.setSearch(
                        Search.conflictOrderingSearch(
                                VarH.DOMWDEG.make(solver, ivars, ValH.MIN, Integer.MAX_VALUE, opt)
                        )
                );
                Restarts.LUBY.declare(solver, 500, 0.d, 5000);
                solver.setSearch(lastConflict(solver.getSearch()));
                break;
            case 6:
                // DWD  + fast restart + LC (+ B2V)
                if (!opt) {
                    solver.setSearch(VarH.DOMWDEG.make(solver, ivars, ValH.MIN, Integer.MAX_VALUE, false));
                } else {
                    solver.setSearch(
                            lastConflict(
                                    VarH.DOMWDEGR.make(solver, ivars, ValH.MIN, Integer.MAX_VALUE, opt)
                            )
                    );
                    Restarts.LUBY.declare(solver, 500, 0.d, 5000);
                }
                break;
            case 7:
                solver.setSearch(
                        lastConflict(
                                VarH.CHS.make(solver, ivars, ValH.MIN, Integer.MAX_VALUE, opt)
                        )
                );
                Restarts.LUBY.declare(solver, 40, 0.d, 5000);
                if (reliableness.containsKey(worker)) {
                    manager.add(worker);
                }
                break;
            default:
                // random search (various seeds) + LNS if optim
                solver.setSearch(lastConflict(randomSearch(ivars, workerID)));
                if (policy != ResolutionPolicy.SATISFACTION) {
                    solver.setLNS(INeighborFactory.blackBox(ivars), new FailCounter(solver.getModel(), 1000));
                }
                if (reliableness.containsKey(worker)) {
                    solver.plugMonitor(new NogoodFromRestarts(worker, manager));
                }
                solver.setRestarts(count -> solver.getFailCount() >= count, new LubyCutoffStrategy(500), 5000);
                break;
        }
        // complete with set default search
        if (ks > 0) {
            solver.setSearch(solver.getSearch(), setVarSearch(svars));
        }
        // complete with real default search
        if (kr > 0) {
            solver.setSearch(solver.getSearch(), realVarSearch(rvars));
        }
    }

    private void check() {
        if (models.size() == 0) {
            throw new SolverException("No model found in the ParallelPortfolio.");
        }
        if (models.get(0).getResolutionPolicy() != ResolutionPolicy.SATISFACTION) {
            Variable objective = models.get(0).getObjective();
            if (objective == null) {
                throw new UnsupportedOperationException("No objective has been defined");
            }
            if ((objective.getTypeAndKind() & Variable.REAL) != 0) {
                for (Constraint c : models.get(0).getCstrs()) {
                    if (c instanceof RealConstraint) {
                        throw new UnsupportedOperationException("" +
                                "Ibex is not multithread safe, ParallelPortfolio cannot be used");
                    }
                }
            }
        }
    }

    private synchronized AtomicBoolean getSolverTerminated() {
        return solverTerminated;
    }

    private synchronized AtomicBoolean getSolutionFound() {
        return solutionFound;
    }

    private synchronized AtomicInteger getSolverRunning() {
        return solverRunning;
    }
}
