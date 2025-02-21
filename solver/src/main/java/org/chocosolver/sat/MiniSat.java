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
import gnu.trove.map.hash.TIntObjectHashMap;
import org.chocosolver.solver.variables.impl.LitVar;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.IntHeap;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Comparator;
import java.util.Random;

/**
 * <p>A MiniSat solver.</p>
 * <p>This is a transposition in Java of <a href="http://minisat.se/">MiniSat</a>.</p>
 * <p><blockquote cite="http://minisat.se">MiniSat is a minimalistic, open-source SAT solver,
 * developed to help researchers and developers alike to get started on SAT.
 * It is released under the MIT licence.</blockquote></p>
 * <p></p>
 * <p><pre>
 * <code>MiniSat sat = new MiniSat();
 * int a = sat.newVariable();
 * int b = sat.newVariable();
 * sat.addClause(a, b);
 * sat.solve();
 * </code>
 * </pre></p>
 *
 * @author Charles Prud'homme
 * @since 12/07/13
 */
public class MiniSat implements SatFactory, Dimacs {

    private static final int DEBUG = 0;
    // Value of an undefined variable
    private static final int varUndef = -1;
    // value of an undefined literal
    private static final int litUndef = -2;

    public static final int lTrue = 0b01;
    public static final int lFalse = 0b10;
    public static final int lUndef = 0b11;
    // undefined clause
    protected static ThreadLocal<Integer> clauseCounter = ThreadLocal.withInitial(() -> 0);
    public static final Clause C_Undef = Clause.undef();
    private static final Reason R_Undef = Reason.undef();
    static final VarData VD_Undef = new VarData(R_Undef, -1, -1);
    public Clause confl = C_Undef;

    public static final Clause C_Fail = new Clause(new int[]{0, 0});
    static final ChannelInfo CI_Null = new ChannelInfo(null, 0, 0, 0);

    // If false, the constraints are already unsatisfiable. No part of
    // the solver state may be used!
    public boolean ok_;
    // List of problem addClauses.
    public final ArrayList<Clause> clauses = new ArrayList<>();
    // List of learnt addClauses.
    private final ArrayList<Clause> learnts = new ArrayList<>();
    // 'watches_[lit]' is a list of constraints watching 'lit'(will go
    // there if literal becomes true).
    private final TIntObjectHashMap<ArrayList<Watcher>> watches_ = new TIntObjectHashMap<>();
    // The current assignments.
    //TIntObjectHashMap<Boolean> assignment_ = new TIntObjectHashMap<>();
    TIntArrayList assignment_ = new TIntArrayList();
    // Assignment stack; stores all assignments made in the order they
    // were made.
    TIntArrayList trail_ = new TIntArrayList();
    // Separator indices for different decision levels in 'trail_'.
    TIntArrayList trail_markers_ = new TIntArrayList();
    // Head of queue(as index into the trail_).
    int qhead_;
    // Number of variables
    int num_vars_;
    int rootlvl = 0;
    int ccmin_mode = 0; // Controls conflict clause minimization (0=none, 1=basic, 2=deep)
    int phase_saving = 0; // Controls the level of phase saving (0=none, 1=limited, 2=full)
    double cla_inc = 1;
    double var_inc = 1;
    double var_decay = 0.95;
    double clause_decay = 0.999;
    double random_var_freq = 0;
    int restart_inc = 2;
    boolean rnd_init_act = true;
    boolean luby_restart = true;
    int restart_first = 100;
    int random_seed = 7;
    double learntsize_adjust_confl = 100;
    int learntsize_adjust_cnt = 100;
    double learntsize_adjust_inc = 1.5;
    double learntsize_inc = 1.1;
    double learntsize_factor = 1 / 3d;
    boolean rnd_pol;
    int conflict_budget = -1;
    int propagation_budget = -1;
    int propagations;
    int rnd_decisions;
    boolean asynch_interrupt = false;
    TIntArrayList model = new TIntArrayList();
    TIntArrayList conflict = new TIntArrayList();
    ArrayList<VarData> vardata = new ArrayList<>();
    ArrayList<ChannelInfo> cinfo = new ArrayList<>();
    int conflicts;
    int decisions;
    int max_literals;
    int tot_literals;
    int dec_vars;
    int clauses_literals;
    int learnts_literals;
    double max_learnts;
    BitSet seen = new BitSet();
    BitSet decision = new BitSet();
    BitSet polarity = new BitSet();
    TIntArrayList analyze_toclear = new TIntArrayList();
    TDoubleArrayList activity = new TDoubleArrayList();
    IntHeap order_heap = new IntHeap((a, b) -> activity.get(a) > activity.get(b));
    Random rand;
    private final TIntArrayList temporary_add_vector_ = new TIntArrayList();

    /**
     * Create a new instance of MiniSat solver.
     */
    public MiniSat(boolean addTautology) {
        this.ok_ = true;
        this.qhead_ = 0;
        num_vars_ = 0;
        rand = new Random(random_seed);
        assignment_.add(lUndef); // required because variable are numbered from 1
        if (addTautology) {
            // true literal
            int v = newVariable();
            int l = makeLiteral(v, true);
            assignment_.set(v, makeBoolean(sgn(l)));
            vardata.set(v, new VarData(R_Undef, trailMarker(), trail_.size()));
            trail_.add(l);
            seen.set(v);
            // false literal
            v = newVariable();
            l = makeLiteral(v, true);
            assignment_.set(v, makeBoolean(sgn(l)));
            vardata.set(v, new VarData(R_Undef, trailMarker(), trail_.size()));
            trail_.add(l);
            seen.set(v);
        }
        clauseCounter.set(2);
    }

    @Override
    public MiniSat _me() {
        return this;
    }

    /**
     * Create and return a new variable
     *
     * @return a variable
     */
    public int newVariable() {
        return newVariable(CI_Null);
    }

    /**
     * Create and return a new variable
     *
     * @param ci channel info
     * @return a variable
     */
    public int newVariable(ChannelInfo ci) {
        int v = incrementVariableCounter();
        assert assignment_.size() == v + 1;
        assignment_.add(lUndef);
        vardata.add(VD_Undef);
        cinfo.add(ci);
        //activity .push(0);
        activity.add(rnd_init_act ? rand.nextDouble() * 0.00001 : 0);
        seen.clear(v);
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

    public void beforeAddingClauses() {
        // nothing to do by default.
    }

    public void afterAddingClauses() {
        // nothing to do by default.
    }


    /**
     * Add a clause to the solver.
     *
     * @param ps a list of literals
     * @return {@code false} if the Boolean formula is unsatisfiable.
     */
    public boolean addClause(TIntList ps) {
        assert 0 == trailMarker();
        if (!ok_) return false;

        // Check if the clause is satisfied and remove false/duplicated literals:
        ps.sort();
        int lit = litUndef;
        int j = 0;
        for (int i = 0; i < ps.size(); i++) {
            if (valueLit(ps.get(i)) == lTrue || ps.get(i) == neg(lit)) {
                return true;
            } else if (valueLit(ps.get(i)) != lFalse && ps.get(i) != lit) {
                lit = ps.get(i);
                ps.set(j++, lit);
            }
        }
        if (j < ps.size()) {
            ps.remove(j, ps.size() - j);
        }


        switch (ps.size()) {
            case 0:
                return (ok_ = false);
            case 1:
                uncheckedEnqueue(ps.get(0));
                propagate();
                return (ok_ = (confl == C_Undef));
            default:
                Clause cr = new Clause(ps);
                clauses.add(cr);
                attachClause(cr);
                break;

        }
        return true;
    }


    /**
     * Add a unit clause to the solver.
     *
     * @param l a literal
     * @return {@code false} if the Boolean formula is unsatisfiable.
     */
    public boolean addClause(int l) {
        temporary_add_vector_.resetQuick();
        temporary_add_vector_.add(l);
        return addClause(temporary_add_vector_);
    }

    /**
     * Add a binary clause to the solver.
     *
     * @param p a literal
     * @param q a literal
     * @return {@code false} if the Boolean formula is unsatisfiable.
     */
    public boolean addClause(int p, int q) {
        temporary_add_vector_.resetQuick();
        temporary_add_vector_.add(p);
        temporary_add_vector_.add(q);
        return addClause(temporary_add_vector_);
    }

    /**
     * Add a ternary clause to the solver.
     *
     * @param p a literal
     * @param q a literal
     * @return {@code false} if the Boolean formula is unsatisfiable.
     */
    public boolean addClause(int p, int q, int r) {
        temporary_add_vector_.resetQuick();
        temporary_add_vector_.add(p);
        temporary_add_vector_.add(q);
        temporary_add_vector_.add(r);
        return addClause(temporary_add_vector_);
    }

    public void addLearnt(TIntList learnt_clause) {
        for (int v = 0; v < nVars(); v++) {
            assert valueVar(v) != MiniSat.lUndef || order_heap.contains(v) : v + " not heaped";
        }
        if (learnt_clause.size() == 1) {
            uncheckedEnqueue(learnt_clause.get(0));
        } else {
            Clause cr = new Clause(learnt_clause, true);
            learnts.add(cr);
            attachClause(cr);
            claBumpActivity(cr);
            uncheckedEnqueue(learnt_clause.get(0), Reason.r(cr));
        }
        varDecayActivity();
        claDecayActivity();

        if (--learntsize_adjust_cnt == 0) {
            learntsize_adjust_confl *= learntsize_adjust_inc;
            learntsize_adjust_cnt = (int) learntsize_adjust_confl;
            max_learnts *= learntsize_inc;
        }
    }


    // Backtrack to the previous level.
    public void cancel() {
        cancelUntil(trailMarker() - 1);
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

    // Gives the current decisionlevel.
    public int trailMarker() {
        return trail_markers_.size();
    }

    // Overwrite the default root level (namely 0) -- for lcg
    public void setRootLevel() {
        rootlvl = trailMarker();
        if (DEBUG > 1) System.out.println("root level: " + rootlvl);
        topLevelCleanUp();
    }

    public void topLevelCleanUp() {
        //todo simplifydb?
        for (int i = 0; i < trail_.size(); i++) {
            int x = var(trail_.get(i));
            seen.set(x);
        }
        max_learnts = nClauses() * learntsize_factor;
        learntsize_adjust_confl = 100;
        learntsize_adjust_cnt = (int) learntsize_adjust_confl;
        simplify();
    }

    // The current value of a variable.
    public int valueVar(int x) {
        return assignment_.getQuick(x);
    }

    // The current value of a literal.
    public int valueLit(int l) {
        return value(l, assignment_.getQuick(var(l)));
    }

    private int value(int l, int b) {
        if (b != lUndef) {
            if ((l & 1) != 0) {
                return b;
            } else {
                // not b
                //return (b == lTrue) ? lFalse : lTrue;
                return b ^ lUndef;
            }
        }
        return b;
    }

    // The current number of original clauses.
    public int nClauses() {
        return clauses.size();
    }

    /**
     * The current number of learnt clauses.
     */
    public int nLearnts() {
        return learnts.size();
    }


    private int incrementVariableCounter() {
        return num_vars_++;
    }

    public int nVars() {
        return num_vars_;
    }

    // Begins a new decision level.
    public void pushTrailMarker() {
        trail_markers_.add(trail_.size());
    }

    // Enqueue a literal. Assumes value of literal is undefined.
    public void uncheckedEnqueue(int l, Reason from) {
        assert valueLit(l) == lUndef : "l: " + printLit(l) + " from: " + from;
        int v = var(l);
        if (assignment_.getQuick(v) == lUndef) {
            onLiteralPushed(l);
        }
        assignment_.set(v, makeBoolean(sgn(l)));
        //System.out.printf("Fix %d to %d @ %d due to %s\n", v, sgn(l) ? 1 : 0, trailMarker(), from);
        if (DEBUG > 0) {
            if (DEBUG > 1)
                System.out.printf("uncheckedEnqueue:: Fix %s at %d due to %s\n", printLit(l), trailMarker(), showReason(from));
            else
                System.out.printf("uncheckedEnqueue:: Fix %d at %d due to %s\n", l, sgn(l) ? 0 : 1, showReason(from));
        }
        // ensure capacity of vardata
        assert vardata.size() >= v;
        VarData vd = vardata.get(v);
        if (vd != VD_Undef) {
            vd.set(from, trailMarker(), trail_.size());
        } else {
            vardata.set(v, new VarData(from, trailMarker(), trail_.size()));
        }
        trail_.add(l);
        cinfo.get(v).channel(sgn(l));
    }

    public void onLiteralPushed(int l) {
        // nothing to do by default.
    }

    public void uncheckedEnqueue(int l) {
        uncheckedEnqueue(l, R_Undef);
    }

    public void cEnqueue(int l, Reason r) {
        assert valueLit(l) != lTrue;
        int v = var(l);
        if (valueLit(l) == lFalse) {
            if (r == null || r == R_Undef) {
                // assert(decisionLevel() == 0);
                confl = C_Fail; // todo: check
            } else {
                confl = r.getConflict();
                confl._s(0, l);
            }
            return;
        }
        assignment_.set(v, makeBoolean(sgn(l)));
        //System.out.printf("Fix %d to %d @ %d due to %s\n", v, sgn(l) ? 1 : 0, trailMarker(), r);
        if (DEBUG > 0) {
            if (DEBUG > 1)
                System.out.printf("cEnqueue:: Fix %s at %d due to %s\n", printLit(l), trailMarker(), showReason(r));
            else
                System.out.printf("cEnqueue:: Fix %d at %d due to %s\n", l, sgn(l) ? 0 : 1, showReason(r));
        }
        assert vardata.size() >= v;
        VarData vd = vardata.get(v);
        if (vd != VD_Undef) {
            vd.set(r, trailMarker(), trail_.size());
        } else {
            vardata.set(v, new VarData(r, trailMarker(), trail_.size()));
        }
        trail_.add(l);
    }

    // Attach a clause to watcher lists.
    void attachClause(Clause cr) {
        assert cr.size() > 1;
        ArrayList<Watcher> l0 = watches_.get(neg(cr._g(0)));
        if (l0 == null) {
            l0 = new ArrayList<>();
            watches_.put(neg(cr._g(0)), l0);
        }
        ArrayList<Watcher> l1 = watches_.get(neg(cr._g(1)));
        if (l1 == null) {
            l1 = new ArrayList<>();
            watches_.put(neg(cr._g(1)), l1);
        }
        l0.add(new Watcher(cr, cr._g(1)));
        l1.add(new Watcher(cr, cr._g(0)));
        if (cr.learnt()) learnts_literals += cr.size();
        else clauses_literals += cr.size();
    }

    void detachClause(Clause cr) {
        ArrayList<Watcher> ws = watches_.get(neg(cr._g(0)));
        int i = ws.size() - 1;
        while (i >= 0 && ws.get(i).clause != cr) {
            i--;
        }
        assert i > -1;
        ws.remove(i);
        ws = watches_.get(neg(cr._g(1)));
        i = ws.size() - 1;
        while (i >= 0 && ws.get(i).clause != cr) {
            i--;
        }
        assert i > -1;
        ws.remove(i);
    }

    // Perform unit propagation. returns true upon success.
    public boolean propagate() {
        confl = C_Undef;
        int num_props = 0;
        while (qhead_ < trail_.size()) {
            int p = trail_.get(qhead_++);
            num_props++;
            propagateLit(p);
        }
        propagations += num_props;
        return (confl == C_Undef);
    }

    private void propagateLit(int p) {
        // 'p' is enqueued fact to propagate.
        ArrayList<Watcher> ws = watches_.get(p);

        int i = 0;
        int j = 0;
        while (ws != null && i < ws.size()) {
            Watcher w = ws.get(i);
            // Try to avoid inspecting the clause:
            int blocker = w.blocker;
            if (valueLit(blocker) == lTrue) {
                ws.set(j++, w);
                i++;
                continue;
            }

            // Make sure the false literal is data[1]:
            Clause cr = w.clause;
            final int false_lit = neg(p);
            if (cr._g(0) == false_lit) {
                cr._s(0, cr._g(1));
                cr._s(1, false_lit);
            }
            assert (cr._g(1) == false_lit);
            i++;

            // If 0th watch is true, then clause is already satisfied.
            final int first = cr._g(0);
//            Watcher w = new Watcher(cr, first);
            /* ws.set(i - 1, null); // now w can be used
            w.clause = cr; */
            w.blocker = first;
            if (first != blocker && valueLit(first) == lTrue) {
                ws.set(j++, w);
                continue;
            }

            boolean cont = newWatch(cr, false_lit, w);

            // Did not find watch -- clause is unit under assignment:
            if (!cont) {
                ws.set(j++, w);
                if (valueLit(first) == lFalse) {
                    confl = cr;
                    qhead_ = trail_.size();
                    // Copy the remaining watches_:
                    while (i < ws.size()) {
                        ws.set(j++, ws.get(i++));
                    }
                    onLiteralPushed(first);
                } else {
                    uncheckedEnqueue(first, cr);
                }
            }
        }
        if (ws != null && ws.size() > j) {
//            for (int k = ws.size() - 1; k >= j; k--) {
//                ws.remove(j);
//            }
            ws.subList(j, ws.size()).clear();
        }
    }

    private boolean newWatch(Clause cr, int false_lit, Watcher w) {
        // Look for new watch:
        for (int k = 2; k < cr.size(); k++) {
            if (valueLit(cr._g(k)) != lFalse) {
                cr._s(1, cr._g(k));
                cr._s(k, false_lit);
                ArrayList<Watcher> lw = watches_.get(neg(cr._g(1)));
                if (lw == null) {
                    lw = new ArrayList<>();
                    watches_.put(neg(cr._g(1)), lw);
                }
                lw.add(w);
                return true;
            }
        }
        return false;
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
    ESat search(int nof_conflicts) {
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

    public int findConflictLevel() {
        int lvl = -1;
        for (int i = 0; i < confl.size(); i++) {
            int l = vardata.get(var(confl._g(i))).level;
            if (l > lvl) {
                lvl = l;
            }
        }
        return lvl;
    }

    public int analyze(Clause confl, TIntArrayList out_learnt) {
        int pathC = 0;
        int p = litUndef;

        // Generate conflict clause:
        //
        analyseConflict(confl, out_learnt, p, pathC);
        replaceUnreliableLits(out_learnt);
        // Simplify conflict clause:
        //
        int i, j = 1;
        analyze_toclear.resetQuick();
        analyze_toclear.addAll(out_learnt);
        /*if (ccmin_mode == 2) {
            uint32_t abstract_level = 0;
            for (i = 1; i < out_learnt.size(); i++)
                abstract_level |= abstractLevel(var(out_learnt.get(i))); // (maintain an abstraction of levels involved in conflict)

            for (i = j = 1; i < out_learnt.size(); i++)
                if (reason(var(out_learnt.get(i))) == CR_Undef || !litRedundant(out_learnt.get(i), abstract_level))
                    out_learnt.set(j++, out_learnt.get(i));

        } else */
        if (ccmin_mode == 1) {
            //todo fix ccmin_mode
            for (i = j = 1; i < out_learnt.size(); i++) {
                int x = var(out_learnt.get(i));

                if (getConfl(x) == C_Undef)
                    out_learnt.set(j++, out_learnt.get(i));
                else {
                    Clause c = getConfl(var(out_learnt.get(i)));
                    for (int k = 1; k < c.size(); k++)
                        if (!seen.get(var(c._g(k))) && level(var(c._g(k))) > rootlvl) {
                            out_learnt.set(j++, out_learnt.get(i));
                            break;
                        }
                }
            }
        } else
            j = out_learnt.size();

        max_literals += out_learnt.size();
        out_learnt.subList(j, out_learnt.size()).clear();
        tot_literals += out_learnt.size();

        // Find correct backtrack level:
        //
        int out_btlevel = getBacktrackLevel(out_learnt);

        for (j = 0; j < analyze_toclear.size(); j++)
            seen.clear(var(analyze_toclear.get(j)));    // ('seen[]' is now cleared)
        return out_btlevel;
    }

    private void analyseConflict(Clause confl, TIntList out_learnt, int p, int pathC) {
        out_learnt.add(litUndef);      // (leave room for the asserting literal)
        int index = trail_.size() - 1;

        do {
            assert (confl != C_Undef); // (otherwise should be UIP)
            Clause c = confl;

            if (DEBUG > 0) {
                if (p != litUndef) {
                    c._s(0, p);
                }
                if (DEBUG > 1)
                    System.out.printf("%s\n", c.toString(this));
                else System.out.printf("%s\n", c.toString());
            }

            if (c.learnt())
                claBumpActivity(c);

            for (int j = (p == litUndef) ? 0 : 1; j < c.size(); j++) {
                int q = c._g(j);
                int x = var(q);
                if (!seen.get(x) && level(x) > rootlvl) {
                    assert p == litUndef || pos(var(p)) > pos(x) : "chronological inconsistency :(" + printLit(p) + " @ " + pos(var(p)) +
                            ") is explained by a previous event (" + printLit(x) + " @ " + pos(x) + ") "+c;
                    varBumpActivity(x);
                    seen.set(x);
                    if (DEBUG > 1) System.out.printf("mark %d\n", x);
                    if (level(x) >= trailMarker()) {
                        pathC++;
                        if (DEBUG > 1) System.out.printf("path++ (%d)\n", pathC);
                    } else {
                        out_learnt.add(q);
                        if (DEBUG > 1) System.out.printf("out %d\n", q);
                    }
                }
            }
            // Select next clause to look at:
            //noinspection StatementWithEmptyBody
            while (!seen.get(var(trail_.get(index--)))) ;
            p = trail_.get(index + 1);
            confl = getConfl(p);
            seen.clear(var(p));
            if (DEBUG > 1) System.out.printf("clear %d l:%d\n", var(p), p);
            pathC--;
            if (DEBUG > 1) System.out.printf("path-- (%d)\n", pathC);
        } while (pathC > 0 || !cinfo.get(var(p)).reliable);
        out_learnt.set(0, neg(p));
    }

    /**
     * Some lits cannot be used in clause (the ones related to instantiation in lazy lits vars).
     * They need to be replaced by their explanation.
     *
     * @param out_learnt the current clause
     */
    private void replaceUnreliableLits(TIntList out_learnt) {
        temporary_add_vector_.resetQuick();
        for (int i = 1; i < out_learnt.size(); i++) {
            int p = out_learnt.get(i);
            if (cinfo.get(var(p)).reliable) {
                continue;
            }
            if (DEBUG > 0) {
                System.out.printf("replacing %s in %s\n", p, out_learnt);
            }
            Clause c = getConfl(neg(p));
            temporary_add_vector_.add(p);
            int at = out_learnt.size() - 1;
            out_learnt.set(i, out_learnt.get(at));
            out_learnt.removeAt(at);
            i--;
            for (int j = 1; j < c.size(); j++) {
                int q = c._g(j);
                if (!seen.get(var(q))) {
                    seen.set(var(q));
                    out_learnt.add(q);
                }
            }
        }
        while (!temporary_add_vector_.isEmpty()) {
            seen.clear(var(temporary_add_vector_.removeAt(temporary_add_vector_.size() - 1)));
        }
    }

    private int getBacktrackLevel(TIntList out_learnt) {
        int i;
        int p;
        int out_btlevel;
        if (out_learnt.size() == 1)
            out_btlevel = rootlvl;
        else {
            int max_i = 1;
            // Find the first literal assigned at the next-highest level:
            for (i = 2; i < out_learnt.size(); i++)
                if (level(var(out_learnt.get(i))) > level(var(out_learnt.get(max_i))))
                    max_i = i;
            // Swap-in this literal at index 1:
            p = out_learnt.get(max_i);
            out_learnt.set(max_i, out_learnt.get(1));
            out_learnt.set(1, p);
            out_btlevel = level(var(p));
        }
        return out_btlevel;
    }


    boolean simplify() {
        assert (trailMarker() == rootlvl);

        if (ok_) propagate();
        if (!ok_ || (confl != C_Undef))
            return ok_ = false;
        // TODO
        /*if (nAssigns() == simpDB_assigns || (simpDB_props > 0))
            return true;

        // Remove satisfied clauses:
        removeSatisfied(learnts);
        if (remove_satisfied)        // Can be turned off.
            removeSatisfied(clauses);
        */
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


    public void doReduceDB() {
        int i, j;
        double extra_lim = cla_inc / learnts.size();    // Remove any clause below this activity

        learnts.sort(Comparator.comparingDouble(c -> c.activity));
        // Don't delete binary or locked clauses. From the rest, delete clauses from the first half
        // and clauses with activity smaller than 'extra_lim':
        for (i = j = 0; i < learnts.size(); i++) {
            Clause c = learnts.get(i);
            if (c.size() > 2 && !locked(c) && (i < learnts.size() / 2 || c.activity < extra_lim))
                removeClause(learnts.get(i));
            else
                learnts.set(j++, learnts.get(i));
        }
        int n = learnts.size();
        learnts.subList(j, n).clear();
        // System.out.printf("reduceDB removed %d clauses\n", n - j);
    }


    boolean withinBudget() {
        return !asynch_interrupt &&
                (conflict_budget < 0 || conflicts < conflict_budget) &&
                (propagation_budget < 0 || propagations < propagation_budget);
    }

    Clause getConfl(int p) {
        Reason r = reason(var(p));
        return r.getConflict();
    }

    Reason reason(int x) {
        return vardata.get(x).cr;
    }

    int level(int x) {
        return vardata.get(x).level;
    }

    int pos(int x) {
        return vardata.get(x).pos;
    }

    boolean locked(Clause c) {
        Clause cr = getConfl(c._g(0));
        return valueLit(c._g(0)) == lTrue
                && cr != C_Undef
                && cr == c;
    }

    void removeClause(Clause cr) {
        detachClause(cr);
        // Don't leave pointers to free'd memory!
        if (locked(cr)) {
            vardata.get(var(cr._g(0))).clearReason();
        }
    }


    void claBumpActivity(Clause c) {
        if ((c.activity += cla_inc) > 1e20d) {
            // Rescale:
            for (int i = 0; i < learnts.size(); i++) {
                learnts.get(i).activity *= 1e-20d;
            }
            cla_inc *= 1e-20d;
        }
    }

    void varBumpActivity(int v) {
        varBumpActivity(v, var_inc);
    }

    void varBumpActivity(int v, double inc) {
        activity.ensureCapacity(nVars());
        double a = activity.get(v);
        activity.setQuick(v, a + inc);
        if (a + inc > 1e100) {
            activity.transformValues(value -> value * 1e-100);
            var_inc *= 1e-100;
        }
        // Update order_heap with respect to new activity:
        if (order_heap.contains(v))
            order_heap.decrease(v);
    }

    void varDecayActivity() {
        var_inc *= (1 / var_decay);
    }

    void claDecayActivity() {
        cla_inc *= (1 / clause_decay);
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

    ///////

    /**
     * Make a literal from a variable and a sign
     *
     * @param var  a variable
     * @param sign the required sign of the literal
     * @return a literal
     */
    public static int makeLiteral(int var, boolean sign) {
        if (var < 0) {
            return (2 * (-var - 1) + (sign ? 0 : 1));
        }
        return (2 * var + (sign ? 1 : 0));
    }

    /**
     * Make a positive literal from a variable
     *
     * @param var a variable
     * @return a positive literal
     * @implNote Equivalent to call {@code makeLiteral(var, true)}.
     */
    public static int makeLiteral(int var) {
        return makeLiteral(var, true);
    }


    /**
     * Negate the literal given as a parameter
     *
     * @param l a literal
     * @return its negation
     */
    public static int neg(int l) {
        return (l ^ 1);
    }


    /**
     * Returns the sign of a given literal
     *
     * @param l a literal
     * @return <tt>true</tt> if <i>l</i> is odd (<tt>false</tt> literal),
     * <tt>false</tt> if <i>l</i> is even (<tt>true<tt/> literal)
     */
    public static boolean sgn(int l) {
        // 1 is true, 0 is false
        return (l & 1) != 0;
    }

    /**
     * Returns the variable of a given literal
     *
     * @param l a literal
     * @return its variable
     */
    public static int var(int l) {
        return (l >> 1);
    }

    static int makeBoolean(boolean b) {
        return (b ? lTrue : lFalse);
    }

    public String printLit(int p) {
        ChannelInfo ci = cinfo.get(var(p));
        if (ci != null && ci != CI_Null) {
            if (ci.cons_type == 1) {
                int op = ci.val_type * 3 ^ (sgn(p) ? 1 : 0);
                switch (op) {
                    case 0:
                        return (ci.reliable ? "" : "*") + p + "|" + valueLit(p) + "|:" + ci.var + " != " + ci.val + " ";
                    case 1:
                        return (ci.reliable ? "" : "*") + p + "|" + valueLit(p) + "|:" + ci.var + " == " + ci.val;
                    case 2:
                        return (ci.reliable ? "" : "*") + p + "|" + valueLit(p) + "|:" + ci.var + " >= " + (ci.val + 1);
                    case 3:
                        return (ci.reliable ? "" : "*") + p + "|" + valueLit(p) + "|:" + ci.var + " <= " + ci.val;
                    case 6:
                        return "*" + p + ":~" + ci.var + " fixed";
                    case 7:
                        return "*" + p + ":" + ci.var + " fixed";
                }
            }
            throw new UnsupportedOperationException();
        }
        return String.valueOf(p);
    }

    private String showReason(Reason r) {
        if (r == null || r == MiniSat.R_Undef) {
            return "no reason";
        }
        StringBuilder st = new StringBuilder();
        switch (r.type) {
            case 0:
                st.append("clause (");
                Clause cl = (Clause) r;
                for (int i = 1; i < cl.size(); i++) {
                    if (i > 1) st.append(" ∧ ");
                    st.append(printLit(neg(cl._g(i))));
                }
                st.append(")");
                //st.append(" -> ").append(printLit(r.cl._g(0)));
                break;
            //case 1:
            //    ss << "absorbed binary clause?";
            //    break;
            case 2:
                st.append("single literal ").append(printLit(neg(((Reason.Reason1) r).d1)));
                break;
            case 3:
                st.append("two literals ").append(printLit(neg(((Reason.Reason2) r).d1)))
                        .append(" ∧ ").append(printLit(neg(((Reason.Reason2) r).d2)));
                break;
        }
        return st.toString();
    }

    /**
     * A watcher represent a clause attached to a literal.
     * <br/>
     * (or-tools, booleans.cc, ty L. Perron).
     * <p>
     * todo: recycle
     *
     * @author Charles Prud'homme
     * @since 12/07/13
     */
    private static final class Watcher {

        final Clause clause;
        int blocker;

        Watcher(final Clause cr, int l) {
            this.clause = cr;
            this.blocker = l;
        }
    }


    private static final class VarData {
        private Reason cr;
        private int level;
        private int pos;

        public VarData(Reason cr, int level, int pos) {
            this.cr = cr;
            this.level = level;
            this.pos = pos;
        }

        private void set(Reason cr, int level, int pos){
            this.cr = cr;
            this.level = level;
            this.pos = pos;
        }

        private void clearReason(){
            this.cr = R_Undef;
        }
    }

    public interface Channeler {
        void channel(boolean sign);
    }

    public static final class ChannelInfo implements Channeler {
        private final LitVar var;
        private final int cons_type;
        private final int val_type;
        private final int val;
        private final boolean reliable;

        public ChannelInfo(LitVar var, int ct, int vt, int v) {
            this(var, ct, vt, v, true);
        }

        public ChannelInfo(LitVar var, int ct, int vt, int v, boolean reliable) {
            this.var = var;
            this.cons_type = ct;
            this.val_type = vt;
            this.val = v;
            this.reliable = reliable;
        }

        @Override
        public void channel(boolean sign) {
            if (cons_type == 1) {
                var.channel(val, val_type, sign ? 1 : 0);
            }
        }
    }
}