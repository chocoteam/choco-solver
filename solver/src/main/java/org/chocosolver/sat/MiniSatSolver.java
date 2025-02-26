/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2025, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.sat;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.IntHeap;

import java.util.BitSet;
import java.util.Random;

/**
 * <p>A MiniSat solver.</p>
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 21/02/2025
 */
@SuppressWarnings("FieldCanBeLocal")
public final class MiniSatSolver extends MiniSat implements Dimacs {

    private final int random_seed = 7;
    private final Random rand;

    private final int phase_saving = 0; // Controls the level of phase saving (0=none, 1=limited, 2=full)
    private double var_inc = 1;
    private final double var_decay = 0.95;
    private final double random_var_freq = 0;
    private final int restart_inc = 2;
    private final boolean rnd_init_act = true;
    private final boolean luby_restart = true;
    private final int propagation_budget = -1;
    private final int restart_first = 100;
    private final boolean rnd_pol = true;
    private final int conflict_budget = -1;
    private int rnd_decisions;
    private final boolean asynch_interrupt = false;
    private int dec_vars;
    private int conflicts;
    private int decisions;

    private final TIntArrayList model = new TIntArrayList();
    private final TIntArrayList conflict = new TIntArrayList();
    private final BitSet decision = new BitSet();
    private final BitSet polarity = new BitSet();
    private final TDoubleArrayList activity = new TDoubleArrayList();
    private final IntHeap order_heap = new IntHeap((a, b) -> activity.get(a) > activity.get(b));

    /**
     * Create a new instance of MiniSat solver.
     */
    public MiniSatSolver() {
        super(false);
        rand = new Random(random_seed);
    }

    @Override
    public MiniSatSolver _me() {
        return this;
    }

    /**
     * Create and return a new variable
     *
     * @param ci channel info
     * @return a variable
     */
    @Override
    public int newVariable(ChannelInfo ci) {
        int v = super.newVariable(ci);
        activity.add(rnd_init_act ? rand.nextDouble() * 0.00001 : 0);
        polarity.set(v);
        if (!decision.get(v)) dec_vars++;
        decision.set(v);
        insertVarOrder(v);
        return v;
    }

    private void insertVarOrder(int v) {
        if (!order_heap.contains(v) && decision.get(v)) {
            order_heap.insert(v);
        }
    }

    public void addLearnt(TIntList learnt_clause) {
        for (int v = 0; v < nVars(); v++) {
            assert valueVar(v) != MiniSat.lUndef || order_heap.contains(v) : v + " not heaped";
        }
        super.addLearnt(learnt_clause);
        varDecayActivity();
    }

    // Backtrack until a certain level.
    public void cancelUntil(int level) {
        if (trailMarker() > level) {
            for (int c = trail_.size() - 1; c >= trail_markers_.get(level); c--) {
                int x = var(trail_.get(c));
                assignment_.set(x, lUndef);
                if (DEBUG > 0) {
                    if (DEBUG > 1)
                        System.out.printf("Unfix %s\n", printLit(trail_.get(c)));
                    else
                        System.out.printf("Unfix %d\n", trail_.get(c));
                }
                if (phase_saving > 1 || (phase_saving == 1) && c > trail_markers_.get(trail_markers_.size() - 1))
                    polarity.set(x, sgn(trail_.get(c)));
                insertVarOrder(x);
            }
            qhead_ = trail_markers_.get(level);
            trail_.remove(trail_markers_.get(level), trail_.size() - trail_markers_.get(level));
            trail_markers_.remove(level, trail_markers_.size() - level);
        }
    }

    /**
     * A call to this method will attempt to find
     * an interpretation that satisfies the Boolean formula declared in this.
     *
     * @return {@code ESat.TRUE} if such an interpretation is found,
     * {@code ESat.FALSE} if no interpretation exists,
     * {@code ESat.UNDEFINED} if a limit was reached.
     */
    public ESat solve() {
        model.resetQuick();
        conflict.resetQuick();
        if (!ok_) return ESat.FALSE;
        max_learnts = nClauses() * learntsize_factor;
        learntsize_adjust_confl = 100;
        learntsize_adjust_cnt = (int) learntsize_adjust_confl;
        ESat status = ESat.UNDEFINED;

        // Search:
        int curr_restarts = 0;
        while (status == ESat.UNDEFINED) {
            double rest_base = luby_restart ? luby(restart_inc, curr_restarts) : Math.pow(restart_inc, curr_restarts);
            status = search((int) (rest_base * restart_first));
            if (!withinBudget()) break;
            curr_restarts++;
        }

        if (status == ESat.TRUE) {
            // Extend & copy model:
            model.ensureCapacity(nVars());
            for (int i = 0; i < nVars(); i++) {
                model.add(valueLit(i));
            }

        } else if (status == ESat.FALSE && conflict.isEmpty())
            ok_ = false;

        cancelUntil(0);
        if (status == ESat.TRUE) {
            System.out.print("SAT\n");
            for (int i = 0; i < nVars(); i++)
                if (model.get(i) != lUndef)
                    System.out.printf("%s%s%d", (i == 0) ? "" : " ",
                            (model.get(i) == lTrue) ? "" : "-", i + 1);
            System.out.print(" 0\n");
        } else if (status == ESat.FALSE)
            System.out.print("UNSAT\n");
        else
            System.out.print("INDET\n");
        return status;
    }

    /**
     * Search for a model the specified number of conflicts.
     *
     * @param nof_conflicts limit over the number of conflicts
     * @implNote Use negative value for 'nof_conflicts' indicate infinity.
     */
    private ESat search(int nof_conflicts) {
        assert ok_;
        int backtrack_level;
        int conflictC = 0;
        TIntArrayList learnt_clause = new TIntArrayList();

        for (; ; ) {
            propagate();
            if (confl != C_Undef) {
                // CONFLICT
                conflicts++;
                conflictC++;
                if (trailMarker() == 0) return ESat.FALSE;

                learnt_clause.resetQuick();
                backtrack_level = analyze(confl, learnt_clause);
                cancelUntil(backtrack_level);
                addLearnt(learnt_clause);

            } else {
                // NO CONFLICT
                if (nof_conflicts >= 0 && conflictC >= nof_conflicts || !withinBudget()) {
                    // Reached bound on number of conflicts:
                    cancelUntil(0);
                    return ESat.UNDEFINED;
                }

                // Simplify the set of problem clauses:
                if (trailMarker() == 0 && !simplify())
                    return ESat.FALSE;

                if (learnts.size() - trail_.size() >= max_learnts)
                    doReduceDB();

                // New variable decision:
                decisions++;
                int next = pickBranchLit();

                if (next == litUndef)
                    // Model found:
                    return ESat.TRUE;

                // Increase decision level and enqueue 'next'
                pushTrailMarker();
                uncheckedEnqueue(next, R_Undef);
            }
        }

    }

    int pickBranchLit() {
        int next = varUndef;

        // Random decision:
        if (rand.nextDouble() < random_var_freq && !order_heap.isEmpty()) {
            next = order_heap.get(rand.nextInt(order_heap.size()));
            if (valueVar(next) == lUndef && decision.get(next))
                rnd_decisions++;
        }

        // Activity based decision:
        while (next == varUndef || valueVar(next) != lUndef || !decision.get(next))
            if (order_heap.isEmpty()) {
                next = varUndef;
                break;
            } else {
                next = order_heap.removeMin();
            }

        return next == varUndef ?
                litUndef :
                makeLiteral(next, rnd_pol ? rand.nextDouble() < 0.5 : polarity.get(next));
    }

    boolean simplify() {
        assert (trailMarker() == rootlvl);
        if (ok_) propagate();
        if (!ok_ || (confl != C_Undef))
            return ok_ = false;
        // TODO
//            if (nAssigns() == simpDB_assigns || (simpDB_props > 0))
//                return true;

        // Remove satisfied clauses:
        removeSatisfied(learnts);
//            if (remove_satisfied)        // Can be turned off.
//                removeSatisfied(clauses);
        rebuildOrderHeap();

            /*simpDB_assigns = nAssigns();
            simpDB_props = clauses_literals + learnts_literals;   // (shouldn't depend on stats really, but it will do for now)
            */
        return true;
    }

    private void rebuildOrderHeap() {
        TIntList vs = new TIntArrayList();
        for (int v = 0; v < nVars(); v++)
            if (decision.get(v) && valueVar(v) == lUndef)
                vs.add(v);
        order_heap.build(vs);
    }

    private boolean withinBudget() {
        return !asynch_interrupt &&
                (conflict_budget < 0 || conflicts < conflict_budget) &&
                (propagation_budget < 0 || propagations < propagation_budget);
    }

    @Override
    void varBumpActivity(int v) {
        varBumpActivity(v, var_inc);
    }

    private void varDecayActivity() {
        var_inc *= (1 / var_decay);
    }

    private void varBumpActivity(int v, double inc) {
        activity.ensureCapacity(nVars());
        double a = activity.get(v);
        activity.setQuick(v, a + inc);
        if (a + inc > 1e100) {
            activity.transformValues(value -> value * 1e-100);
            var_inc *= 1e-100;
        }
//             Update order_heap with respect to new activity:
        if (order_heap.contains(v))
            order_heap.decrease(v);
    }

    private static double luby(double y, int x) {

        // Find the finite subsequence that contains index 'x', and the
        // size of that subsequence:
        int size = 1;
        int seq = 0;
        while (size < x + 1) {
            seq++;
            size = 2 * size + 1;
        }

        while (size - 1 != x) {
            size = (size - 1) >> 1;
            seq--;
            x = x % size;
        }

        return Math.pow(y, seq);
    }

}
