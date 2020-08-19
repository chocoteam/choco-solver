/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2020, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search;

import org.chocosolver.solver.ISelf;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.nary.lex.PropLexInt;
import org.chocosolver.solver.objective.ParetoOptimizer;
import org.chocosolver.solver.search.limits.ACounter;
import org.chocosolver.solver.search.measure.IMeasures;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
import org.chocosolver.util.criteria.Criterion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Interface to define most commonly used resolution procedures.
 * <p>
 * Project: choco-solver.
 *
 * @author Jean-Guillaum Fages
 * @author Charles Prud'homme
 * @author Guillaume Lelouet
 * @author Dimitri Justeau-Allaire (dimitri.justeau@gmail.com)
 * @since 25/04/2016.
 */
public interface IResolutionHelper extends ISelf<Solver> {

    /**
     * Attempts to find a solution of the declared satisfaction problem.
     * <ul>
     * <li>If the method returns <tt>null</tt>:</li>
     * <ul>
     * <li>either a stop criterion (e.g., a time limit) stops the search before a solution has been found,</li>
     * <li>or no solution exists for the problem (i.e., over-constrained).</li>
     * </ul>
     * <li>if the method returns a {@link Solution}:</li>
     * <ul>
     * <li>a solution has been found. This method can be called anew to look for the next solution, if any.</li>
     * </ul>
     * </ul>
     * <p>
     * If a solution has been found, since the search process stops on that solution, variables' value can be read, e.g.,
     * {@code intvar.getValue()} or the solution can be recorded:
     * <p>
     * <pre>
     *    {@code
     * 	Solution s = new Solution(model);
     * 	s.record();
     * }
     * </pre>
     * <p>
     * Basically, this method runs the following instructions:
     * <p>
     * <pre>
     *     {@code
     *     if(ref().solve()) {
     *          return new Solution(ref()).record();
     *     }else{
     *          return null;
     *       }
     *     }
     * </pre>
     *
     * Note that all variables will be recorded
	 *
	 * Note that it clears the current objective function, if any
     *
     * @param stop optional criterion to stop the search before finding a solution
     * @return a {@link Solution} if and only if a solution has been found, <tt>null</tt> otherwise.
     */
    default Solution findSolution(Criterion... stop) {
    	ref().getModel().clearObjective();
        ref().addStopCriterion(stop);
        boolean found = ref().solve();
        ref().removeStopCriterion(stop);
        if (found) {
            return new Solution(ref().getModel()).record();
        } else {
            return null;
        }
    }

    /**
     * Attempts to find all solutions of the declared satisfaction problem.
     * <ul>
     * <li>If the method returns an empty list: </li>
     * <ul>
     * <li>
     * either a stop criterion (e.g., a time limit) stops the search before any solution has been found,
     * </li>
     * <li>
     * or no solution exists for the problem (i.e., over-constrained).
     * </li>
     * </ul>
     * <li>if the method returns a list with at least one element in it:</li>
     * <ul>
     * <li>either the resolution stops eagerly du to a stop criterion before finding all solutions,</li>
     * <li>or all solutions have been found.</li>
     * </ul>
     * </ul>
     * <p>
     * This method run the following instructions:
     * <pre>
     *     {@code
     *     List<Solution> solutions = new ArrayList<>();
     *     while (model.getSolver().solve()){
     *          solutions.add(new Solution(model).record());
     *     }
     *     return solutions;
     *     }
     * </pre>
     *
     * Note that all variables will be recorded
	 *
	 * Note that it clears the current objective function, if any
     *
     * @param stop optional criterion to stop the search before finding all solutions
     * @return a list that contained the found solutions.
     */
    default List<Solution> findAllSolutions(Criterion... stop) {
		ref().getModel().clearObjective();
        ref().addStopCriterion(stop);
        List<Solution> solutions = new ArrayList<>();
        while (ref().solve()) {
            solutions.add(new Solution(ref().getModel()).record());
        }
        ref().removeStopCriterion(stop);
        return solutions;
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
     * Basically, this method runs the following instructions:
     * <p>
     * <pre>
     * {@code
     * 	List<Solution> solutions = new ArrayList<>();
     * 	while (model.getSolver().solve()) {
     * 		solutions.add(new Solution(model).record());
     *    }
     * 	return solutions;
     * }
     * </pre>
     *
     * Note that all variables will be recorded
     *
     * @param stop optional criterion to stop the search before finding all/best solution
     * @return a list that contained the found solutions.
     */
    default Stream<Solution> streamSolutions(Criterion... stop) {
        ref().addStopCriterion(stop);
        Spliterator<Solution> it = new Spliterator<Solution>() {

            @Override
            public boolean tryAdvance(Consumer<? super Solution> action) {
                if (ref().solve()) {
                    action.accept(new Solution(ref().getModel()).record());
                    return true;
                }
                ref().removeStopCriterion(stop);
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

    /**
     * Attempt to find the solution that optimizes the mono-objective problem defined by a unique objective variable and
     * an optimization criteria.
     * <ul>
     * <li>If this method returns <i>null</i>:</li>
     * <ul>
     * <li>either the resolution stops eagerly du to a stop criterion (e.g., a time limit) and no solution has been found
     * so far,</li>
     * <li>or the problem cannot be satisfied (i.e., over constrained).</li>
     * </ul>
     * <li>If this method returns a {@link Solution}:</li>
     * <ul>
     * <li>either the resolution stops eagerly du to a stop criterion and the solution is the <b>best</b> found so far but not
     * necessarily the optimal one,</li>
     * <li>or it is the optimal one.</li>
     * </ul>
     * </ul>
     * <p>
     * Basically, this method runs the following instructions:
     * <p>
     * <pre>
     *     {@code
     *     model.setObjective(maximize, objective);
     *     Solution s = new Solution(model);
     *     while (model.getSolver().solve()) {
     *          s.record();
     *     }
     *     return model.getSolver().isFeasible() == ESat.TRUE ? s : null;
     *     }
     * </pre>
     *
     * Note that all variables will be recorded
     *
     * @param objective integer variable to optimize
     * @param maximize  set to <tt>true</tt> to solve a maximization problem, set to <tt>false</tt> to solve a minimization
     *                  problem.
     * @param stop      optional criterion to stop the search before finding all/best solution
     * @return <ul>
     * <li><tt>null</tt> if the problem has no solution or a stop criterion stops the search before finding a
     * first solution</li>
     * <li>a {@link Solution} if at least one solution has been found. The solution is proven to be optimal if no
     * stop criterion stops the search.</li>
     * </ul>
     */
    default Solution findOptimalSolution(IntVar objective, boolean maximize, Criterion... stop) {
        ref().getModel().setObjective(maximize, objective);
        ref().addStopCriterion(stop);
        Solution s = new Solution(ref().getModel());
        while (ref().solve()) {
            s.record();
        }
        ref().removeStopCriterion(stop);
        return ref().isFeasible() == ESat.TRUE ? s : null;
    }

    /**
     * Attempt to find the solution that optimizes the mono-objective problem defined by
     * a unique objective variable and an optimization criteria, then finds and stores all optimal solution.
     * Searching for all optimal solutions is only triggered if the first search is complete.
     * This method works as follow:
     * <ol>
     * <li>It finds and prove the optimum</li>
     * <li>It resets the search and enumerates all solutions of optimal cost</li>
     * </ol>
     * Note that the returned list can be empty.
     * <ul>
     * <li>If the method returns an empty list: </li>
     * <ul>
     * <li>
     * either a stop criterion (e.g., a time limit) stops the search before any solution has been found,
     * </li>
     * <li>
     * or no solution exists for the problem (i.e., over-constrained).
     * </li>
     * </ul>
     * <li>if the method returns a list with at least one element in it:</li>
     * <ul>
     * <li>either the resolution stops eagerly du to a stop criterion before finding all solutions,</li>
     * <li>or all optimal solutions have been found.</li>
     * </ul>
     * </ul>
     *
     * This method runs the following instructions:
     * <pre>
     *     {@code
     *     ref().findOptimalSolution(objective, maximize, stop);
     *     if (!ref().isStopCriterionMet()  &&
     *          model.getSolver().getMeasures().getSolutionCount() > 0) {
     *         int opt = _model.getSolver().getObjectiveManager().getBestSolutionValue().intValue();
     *         model.getSolver().reset();
     *         model.clearObjective();
     *         model.arithm(objective, "=", opt).post();
     *         return findAllSolutions();
     *     } else {
     *          return Collections.emptyList();
     *     }
     *     }
     * </pre>
     *
     * Note that all variables will be recorded
     *
     * @param objective the variable to optimize
     * @param maximize  set to <tt>true</tt> to solve a maximization problem,
     *                  set to <tt>false</tt> to solve a minimization problem.
     * @param stop      optional criterion to stop the search before finding all/best solution
     * @return a list that contained the solutions found.
     */
    default List<Solution> findAllOptimalSolutions(IntVar objective, boolean maximize, Criterion... stop) {
        ref().addStopCriterion(stop);
		boolean defaultS = ref().getSearch()==null;// best bound (in default) is only for optim
        ref().findOptimalSolution(objective, maximize);
        if (!ref().isStopCriterionMet()
                && ref().getSolutionCount() > 0) {
            ref().removeStopCriterion(stop);
            int opt = ref().getObjectiveManager().getBestSolutionValue().intValue();
            ref().reset();
            ref().getModel().clearObjective();
            Constraint forceOptimal = ref().getModel().arithm(objective, "=", opt);
            forceOptimal.post();
            if(defaultS) ref().setSearch(Search.defaultSearch(ref().getModel()));// best bound (in default) is only for optim
            List<Solution> solutions = findAllSolutions(stop);
            ref().getModel().unpost(forceOptimal);
            return solutions;
        } else {
            ref().removeStopCriterion(stop);
            return Collections.emptyList();
        }
    }

    /**
     * Attempt to find the solution that optimizes the mono-objective problem defined by a unique objective variable and
     * an optimization criteria, then finds and stores all optimal solution. This method works as follow:
     * <ol>
     * <li>It finds and prove the optimum</li>
     * <li>It resets the search and enumerates all solutions of optimal cost</li>
     * </ol>
     * Note that the returned list can be empty.
     * <ul>
     * <li>If the method returns an empty list:</li>
     * <ul>
     * <li>either a stop criterion (e.g., a time limit) stops the search before any solution has been found,</li>
     * <li>or no solution exists for the problem (i.e., over-constrained).</li>
     * </ul>
     * <li>if the method returns a list with at least one element in it:</li>
     * <ul>
     * <li>either the resolution stops eagerly du to a stop criterion before finding all solutions,</li>
     * <li>or all optimal solutions have been found.</li>
     * </ul>
     * </ul>
     * <p>
     * Basically, this method runs the following instructions:
     * <p>
     * <pre>
     *     {@code
     *     ref().findOptimalSolution(objective, maximize);
     *     if (model.getSolver().getMeasures().getSolutionCount() > 0) {
     *         int opt = _model.getSolver().getObjectiveManager().getBestSolutionValue().intValue();
     *         model.getSolver().reset();
     *         model.clearObjective();
     *         model.arithm(objective, "=", opt).post();
     *         return findAllSolutions();
     *     } else {
     *          return Collections.emptyList();
     *     }
     *     }
     * </pre>
	 *
     * Note that all variables will be recorded
     *
     * @param objective the variable to optimize
     * @param maximize  set to <tt>true</tt> to solve a maximization problem, set to <tt>false</tt> to solve a minimization
     *                  problem.
     * @param stop      optional criterion to stop the search before finding all/best solution
     * @return a list that contained the solutions found.
     */
    default Stream<Solution> streamOptimalSolutions(IntVar objective, boolean maximize, Criterion... stop) {
        ref().addStopCriterion(stop);
        boolean defaultS = ref().getSearch()==null;// best bound (in default) is only for optim
        ref().findOptimalSolution(objective, maximize);
        if (!ref().isStopCriterionMet()
                && ref().getSolutionCount() > 0) {
            ref().removeStopCriterion(stop);
            int opt = ref().getObjectiveManager().getBestSolutionValue().intValue();
            ref().reset();
            ref().getModel().clearObjective();
            Constraint forceOptimal = ref().getModel().arithm(objective, "=", opt);
            forceOptimal.post();
            if(defaultS) ref().setSearch(Search.defaultSearch(ref().getModel()));// best bound (in default) is only for optim
            Stream<Solution> stream = streamSolutions(stop);
            ref().getModel().unpost(forceOptimal);
        } else {
            ref().removeStopCriterion(stop);
            return Stream.empty();
        }
    }

    /**
     * Attempts optimize the value of the <i>objectives</i> variable w.r.t. to an optimization criteria. Finds and stores
     * all optimal solution. Note that the returned list can be empty.
     * <ul>
     * <li>If the method returns an empty list:</li>
     * <ul>
     * <li>either a stop criterion (e.g., a time limit) stops the search before any solution has been found,</li>
     * <li>or no solution exists for the problem (i.e., over-constrained).</li>
     * </ul>
     * <li>if the method returns a list with at least one element in it:</li>
     * <ul>
     * <li>either the resolution stops eagerly du to a stop criterion before finding all solutions,</li>
     * <li>or all optimal solutions have been found.</li>
     * </ul>
     * </ul>
     * Basically, this method runs the following instructions:
     * <p>
     * <pre>
     * {@code
     * ParetoOptimizer pareto = new ParetoOptimizer(maximize, objectives);
     * 	while (ref().solve()) {
     * 		pareto.onSolution();
     * 	}
     * 	return pareto.getParetoFront();
     * }
     * </pre>
	 *
     * Note that all variables will be recorded
     *
     * @param objectives the array of variables to optimize
     * @param maximize   set to <tt>true</tt> to solve a maximization problem, set to <tt>false</tt> to solve a minimization
     *                   problem.
     * @param stop       optional criterions to stop the search before finding all/best solution
     * @return a list that contained the solutions found.
     */
    default List<Solution> findParetoFront(IntVar[] objectives, boolean maximize, Criterion... stop) {
        ref().addStopCriterion(stop);
        ParetoOptimizer pareto = new ParetoOptimizer(maximize, objectives);
        while (ref().solve()) {
            pareto.onSolution();
        }
        ref().removeStopCriterion(stop);
        return pareto.getParetoFront();
    }

    /**
     * Attempts optimize the value of the <i>objectives</i> variable w.r.t. to an optimization criteria.
     * Finds and stores the optimal solution, if any.
     * Moreover, the objective variables are ordered wrt their significance.
     * The first objective variable is more significant or equally significant to the second one,
     * which in turn is more significant or equally significant to the third one, etc.
     * On an optimal solution of a maximization problem, the first variable is maximized, then the second one is maximized, etc.
     *
     * Note that if a stop criteria stops the search eagerly, no optimal solution may have been found.
     * In that case, the best solution, if at least one has been found, is returned.
	 *
     * Note that all variables will be recorded
     *
     * @param objectives
     *          the list of objectives to find the optimal. A solution o1..on is optimal if lexicographically better than
     *          any other correct solution s1..sn
     * @param maximize
     *          to maximize the objective, false to minimize.
     *  @param stop
     *          stop criterion are added before search and removed after search.
     * @return A solution with the optimal objectives value, null if no solution exists or search was stopped before a
     *         solution could be found. If null, check if a criterion was met to find out was caused the null.
     */
    default Solution findLexOptimalSolution(IntVar[] objectives, boolean maximize, Criterion... stop) {
        if (objectives == null || objectives.length == 0) {
            return findSolution(stop);
        }
        ref().addStopCriterion(stop);
        Solution sol = null;
        Constraint clint = null;
        PropLexInt plint = null;
        // 1. copy objective variables and transform it if necessary
        IntVar[] mobj = new IntVar[objectives.length];
        for (int i = 0; i < objectives.length; i++) {
            mobj[i] = maximize ? ref().getModel().intMinusView(objectives[i]) : objectives[i];
        }
        // 2. try to find a first solution
        while (ref().solve()) {
            if (sol == null) {
                sol = new Solution(ref().getModel());
            }
            sol.record();
            // 3. extract values of each objective
            int[] bestFound = new int[objectives.length];
            for (int vIdx = 0; vIdx < objectives.length; vIdx++) {
                bestFound[vIdx] = sol.getIntVal(objectives[vIdx]) * (maximize ? -1 : 1);
            }
            // 4. either update the constraint, or declare it if first solution
            if (plint != null) {
                plint.updateIntVector(bestFound);
            } else {
                plint = new PropLexInt(mobj, bestFound, true);
                clint = new Constraint("lex objectives", plint);
                clint.post();
            }
        }
        if (clint != null) {
            ref().getModel().unpost(clint);
        }
        ref().removeStopCriterion(stop);
        return sol;
    }

    /**
     * Explore the model, calling a {@link BiConsumer} for each {@link Solution} with its corresponding {@link IMeasures}.
     * <p>
     * The {@link Solution} and the {@link IMeasures} provided by the Biconsumer are always the same reference, consider
     * either extracting values from them or copy them. See {@link IMeasures} and {@link Solution#copySolution()}
     * </p>
     * <p>
     * The consumer and the criterion should not be linked ; instead use {@link ACounter} sub-classes.
     * </p>
	 *
     * Note that all variables will be recorded
     *
     * @param cons the consumer of solution and measure couples
     * @param stop optional criterions to stop the search before finding all/best solution
     */
    default void eachSolutionWithMeasure(BiConsumer<Solution, IMeasures> cons, Criterion... stop) {
        ref().addStopCriterion(stop);
        Solution s = new Solution(ref().getModel());
        while (ref().solve()) {
            cons.accept(s.record(), ref().getMeasures());
        }
        ref().removeStopCriterion(stop);
    }
}
