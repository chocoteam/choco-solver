/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2025, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.loop.learn;

import gnu.trove.list.array.TIntArrayList;
import org.chocosolver.sat.Clause;
import org.chocosolver.sat.MiniSat;
import org.chocosolver.solver.Cause;
import org.chocosolver.solver.Settings;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.search.strategy.assignments.DecisionOperatorFactory;
import org.chocosolver.solver.search.strategy.decision.DecisionPath;
import org.chocosolver.solver.search.strategy.decision.IntDecision;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.tools.VariableUtils;

import java.util.Arrays;
import java.util.Comparator;

/**
 * This class aims at defining a lazy clause generation algorithm as a {@link Learn} object.
 * <br/>
 * It is based on :
 * Feydy, T., Stuckey, P.J. (2009). "Lazy Clause Generation Reengineered".
 * In: Gent, I.P. (eds) Principles and Practice of Constraint Programming - CP 2009.
 * CP 2009. Lecture Notes in Computer Science, vol 5732. Springer, Berlin, Heidelberg.
 * <a href="https://doi.org/10.1007/978-3-642-04244-7_29">DOI</a>.
 *
 * @author Charles Prud'homme
 * @since 08/11/2023
 */
public class LazyClauseGeneration implements Learn {
    public static boolean VERBOSE = false;
    public static boolean SORT_LITS_ON_SOLUTION = false;
    private final boolean SORT_LITS_ON_FAILURE = Settings.PARAM_SORT_LITS_ON_FAILURE;

    private static final String ON_FAILURE = "On SAT failure,";
    private static final String ON_SOLUTION = "On solution,";
    /**
     * The solver that is watched
     */
    private final Solver mSolver;
    /**
     * The SAT solver that is used to learn clauses
     */
    private final MiniSat mSat;
    /**
     * The maximum number of learnt clauses
     */
    private final int max_learnts;
    /**
     * Maintains the number of solutions found, required for {@link Learn#record()}.
     */
    private long nbSolutions = 0;
    /**
     * Maintains the number of restarts, required for {@link Learn#forget()}.
     */
    private long nbRestarts = 0;
    /**
     * Indicates whether the current clause comes from on a solution (or on a failure).
     */
    private boolean onSolution = false;
    private long nextReductionCall = Settings.PARAM_REDUCE_SAT_LEARNTS_CLAUSE_BASE;
    private int reductions = 0;
    /**
     * A temporary storage for learnt clauses.
     */
    private final SortableIntArrayList learnt_clause = new SortableIntArrayList(SORT_LITS_ON_SOLUTION || SORT_LITS_ON_FAILURE);

    public LazyClauseGeneration(Solver solver, MiniSat sat) {
        this.mSolver = solver;
        this.mSat = sat;
        this.max_learnts = mSolver.getModel().getSettings().getNbMaxLearntClauses();
    }

    @Override
    public void init() {
        mSat.setRootLevel();
    }

    @Override
    public boolean record() {
        if (nbSolutions == mSolver.getSolutionCount()) {
            onFailure();
        } else {
            nbSolutions++;
            onSolution();
        }
        return true;
    }

    @Override
    public void forget() {
        // required because MoveBinaryDFS add a useless decision level on refutation
        if (nbRestarts == mSolver.getRestartCount()) {
            mSolver.cancelTrail();
            mSolver.getDecisionPath().synchronize(true, learnt_clause.size() > 1);
            if (!learnt_clause.isEmpty()) {
                if (onSolution ? SORT_LITS_ON_SOLUTION : SORT_LITS_ON_FAILURE) {
                    learnt_clause.quicksort(1, learnt_clause.size() - 1);
                }
                mSat.addLearnt(learnt_clause, onSolution);
            }
        } else {
            nbRestarts = mSolver.getRestartCount();
        }
        if(mSat.nLearnts() >= max_learnts || mSolver.getFailCount() > nextReductionCall){
            mSat.doReduceDB();
            nextReductionCall += Settings.PARAM_REDUCE_SAT_LEARNTS_CLAUSE_BASE +
                    (long) Settings.PARAM_REDUCE_SAT_LEARNTS_CLAUSE_FACTOR * (++reductions);
        }
    }

    private void onFailure() {
        ContradictionException cex = mSolver.getContradictionException();
        int backtrack_level = analyze(cex, ON_FAILURE);
        onSolution = false; // to indicate that we are learning on a failure, for #forget()
        int upto = mSolver.getEnvironment().getWorldIndex() - backtrack_level;
        if (upto > 1) {
            mSolver.getMeasures().incBackjumpCount();
        }
        mSolver.setJumpTo(upto);
    }

    private void onSolution() {
        assert mSat.confl == MiniSat.C_Undef;
        if (!mSolver.getObjectiveManager().isOptimization()) {
            learnt_clause.resetQuick();
            extractFromVariables();
            //extractFromDecisions();

            mSat.confl = new Clause(learnt_clause, false /*?*/);
            int backtrack_level = analyze(mSolver.getContradictionException().set(Cause.Sat, null, null), ON_SOLUTION);
            onSolution = true; // to indicate that we are learning on a solution, for #forget()
            int upto = mSolver.getEnvironment().getWorldIndex() - backtrack_level;
            if (upto > 1) {
                mSolver.getMeasures().incBackjumpCount();
            }
            mSolver.setJumpTo(upto);
        } // else always restart on a solution, managed in Solver
    }

    private void extractFromVariables() {
        IntVar[] ivars = mSolver.getModel().retrieveIntVars(true);
        for (int i = 0; i < ivars.length; i++) {
            // todo check root failure ==> isRootLevel ?
            if (!VariableUtils.isView(ivars[i])) {
                learnt_clause.add(ivars[i].getValLit());
            }
        }
    }

    @SuppressWarnings("unused")
    private void extractFromDecisions() {
        //todo deal with LazyLit
        DecisionPath path = mSolver.getDecisionPath();
        if (path.size() > 1) { // skip solution at ROOT node
            int i = path.size() - 1;
            IntDecision dec = (IntDecision) path.getDecision(i);
            // skip refuted bottom decisions
            while (i > 1 /*0 is ROOT */ && !dec.hasNext() && dec.getArity() > 1) {
                dec = (IntDecision) path.getDecision(--i);
            }
            // build a 'fake' explanation that is able to refute the right decision
            for (; i > 0 /*0 is ROOT */ ; i--) {
                dec = (IntDecision) path.getDecision(i);
                IntVar var = dec.getDecisionVariable();
                if (dec.getDecOp().equals(DecisionOperatorFactory.makeIntEq())) {
                    if (dec.hasNext() || dec.getArity() == 1) {
                        learnt_clause.add(var.getLit(dec.getDecisionValue(), IntVar.LR_NE));
                    } else {
                        learnt_clause.add(var.getLit(dec.getDecisionValue(), IntVar.LR_EQ));
                    }
                } else if (dec.getDecOp().equals(DecisionOperatorFactory.makeIntNeq())) {
                    if (dec.hasNext() || dec.getArity() == 1) {
                        learnt_clause.add(var.getLit(dec.getDecisionValue(), IntVar.LR_EQ));
                    } else {
                        learnt_clause.add(var.getLit(dec.getDecisionValue(), IntVar.LR_NE));
                    }
                } else if (dec.getDecOp().equals(DecisionOperatorFactory.makeIntSplit())) { // <=
                    if (dec.hasNext() || dec.getArity() == 1) {
                        learnt_clause.add(var.getLit(dec.getDecisionValue() + 1, IntVar.LR_GE));
                    } else {
                        learnt_clause.add(var.getLit(dec.getDecisionValue(), IntVar.LR_LE));
                    }
                } else if (dec.getDecOp().equals(DecisionOperatorFactory.makeIntReverseSplit())) { // >=
                    if (dec.hasNext() || dec.getArity() == 1) {
                        learnt_clause.add(var.getLit(dec.getDecisionValue() - 1, IntVar.LR_LE));
                    } else {
                        learnt_clause.add(var.getLit(dec.getDecisionValue(), IntVar.LR_GE));
                    }
                }
            }
        } else {
            learnt_clause.add(0);
        }

    }

    private int analyze(ContradictionException cex, String message) {
        int level;
        learnt_clause.resetQuick();
        if (mSat.confl != MiniSat.C_Undef) {
            Clause cl = mSat.confl;
            level = mSat.analyze(cl, learnt_clause);
            if (VERBOSE)
                System.out.printf("%s learn %s\n",
                        message,
                        Arrays.stream(learnt_clause.toArray())
                                .mapToObj(v -> mSat.printLit(v) + ", ")
                                .reduce("", (s1, s2) -> s1 + " " + s2));
            mSat.confl = MiniSat.C_Undef;
        } else {
            System.err.print("On CP failure -- no learn\n");
            // find the first decision that has not been refuted
                /*level = getEnvironment().getWorldIndex();
                while (level > 0) {
                    Decision<?> dec = decisions.get(--level);
                    if (dec.hasNext()) {
                        break;
                    }
                }*/
            throw new SolverException("Unexpected contradiction:" + cex);
        }
        return level;
    }

    private class SortableIntArrayList extends TIntArrayList {
        Comparator<Integer> comparator;

        public SortableIntArrayList(final boolean sort) {
            super();
            if (sort) {
                comparator = //Comparator.comparingInt(i -> i);
                        Comparator.comparingInt(a -> mSat.level(MiniSat.var(a)));
                comparator = comparator.thenComparingInt(i -> i);
            }
        }

        public void quicksort(int low, int high) {
            assert high < this._pos;
            _quicksort(this._data, low, high);
        }

        private void _quicksort(int[] tab, int low, int high) {
            if (low < high) {
                int pivotIndex = _partition(tab, low, high);
                _quicksort(tab, low, pivotIndex - 1);
                _quicksort(tab, pivotIndex + 1, high);
            }
        }

        private int _partition(int[] tab, int low, int high) {
            int pivot = tab[high];
            int i = low - 1;
            for (int j = low; j < high; j++) {
                if (comparator.compare(tab[j], pivot) <= 0) {
                    i++;
                    // swap tab[i] and tab[j]
                    int temp = tab[i];
                    tab[i] = tab[j];
                    tab[j] = temp;
                }
            }
            // swap tab[i + 1] and tab[high] (or pivot)
            int temp = tab[i + 1];
            tab[i + 1] = tab[high];
            tab[high] = temp;
            return i + 1;
        }
    }
}
