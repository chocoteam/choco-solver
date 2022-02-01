/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
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
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.IntHeap;

import java.util.*;

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

    // Value of an undefined variable
    private static final int varUndef = -1;
    // value of an undefined literal
    private static final int litUndef = -2;
    // undefined clause
    static final Clause CR_Undef = new Clause(new int[0]);
    static final VarData VD_Undef = new VarData(CR_Undef, -1);

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
    List<Boolean> assignment_ = new ArrayList<>();
    // Assignment stack; stores all assignments made in the order they
    // were made.
    TIntArrayList trail_ = new TIntArrayList();
    // Separator indices for different decision levels in 'trail_'.
    TIntArrayList trail_markers_ = new TIntArrayList();
    // Head of queue(as index into the trail_).
    int qhead_;
    // Number of variables
    int num_vars_;

    int ccmin_mode = 1; // Controls conflict clause minimization (0=none, 1=basic, 2=deep)
    int phase_saving = 2; // Controls the level of phase saving (0=none, 1=limited, 2=full)
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
    ArrayList<Boolean> model = new ArrayList<>();
    TIntArrayList conflict = new TIntArrayList();
    ArrayList<VarData> vardata = new ArrayList<>();
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
    public TIntArrayList touched_variables_ = new TIntArrayList();

    /**
     * Create a new instance of MiniSat solver.
     */
    protected MiniSat() {
        this.ok_ = true;
        this.qhead_ = 0;
        num_vars_ = 0;
        rand = new Random(random_seed);
        assignment_.add(Boolean.lUndef); // required because variable are numbered from 1
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
        int v = incrementVariableCounter();
        assignment_.add(v, Boolean.lUndef);
        vardata.add(new VarData(CR_Undef, 0));
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


    /**
     * Add a clause to the solver.
     *
     * @param ps a list of literals
     * @return {@code false} if the Boolean formula is unsatisfiable.
     */
    public boolean addClause(TIntList ps) {
        assert 0 == trailMarker();
        if (!ok_) return false;

        // Check if clause is satisfied and remove false/duplicated literals:
        ps.sort();
        int lit = litUndef;
        int j = 0;
        for (int i = 0; i < ps.size(); i++) {
            if (valueLit(ps.get(i)) == Boolean.lTrue || ps.get(i) == neg(lit)) {
                return true;
            } else if (valueLit(ps.get(i)) != Boolean.lFalse && ps.get(i) != lit) {
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
                return (ok_ = propagate() == CR_Undef);
            default:
                Clause cr = new Clause(ps.toArray());
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

    // Incremental propagation.
    boolean initPropagator() {
        touched_variables_.resetQuick();
        return !ok_;
    }

    // Backtrack until a certain level.
    void cancelUntil(int level) {
        if (trailMarker() > level) {
            for (int c = trail_.size() - 1; c >= trail_markers_.get(level); c--) {
                int x = var(trail_.get(c));
                assignment_.set(x, Boolean.lUndef);
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
    int trailMarker() {
        return trail_markers_.size();
    }

    // The current value of a variable.
    Boolean valueVar(int x) {
        return assignment_.get(x);
    }

    // The current value of a literal.
    Boolean valueLit(int l) {
        Boolean b = assignment_.get(var(l));
        return b == Boolean.lUndef ? Boolean.lUndef : xor(b, sgn(l));
    }

    // The current number of original clauses.
    int nClauses() {
        return clauses.size();
    }


    private int incrementVariableCounter() {
        return num_vars_++;
    }

    int nVars() {
        return num_vars_;
    }

    // Begins a new decision level.
    void pushTrailMarker() {
        trail_markers_.add(trail_.size());
    }

    // Enqueue a literal. Assumes value of literal is undefined.
    void uncheckedEnqueue(int l, Clause from) {
        assert valueLit(l) == Boolean.lUndef;
        if (assignment_.get(var(l)) == Boolean.lUndef) {
            touched_variables_.add(l);
        }
        assignment_.set(var(l), makeBoolean(sgn(l)));
        //vardata.ensureCapacity(var(l));
        while (vardata.size() < var(l)) {
            vardata.add(VD_Undef);
        }
        vardata.set(var(l), new VarData(from, trailMarker()));
        trail_.add(l);
    }

    void uncheckedEnqueue(int l) {
        uncheckedEnqueue(l, CR_Undef);
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
    Clause propagate() {
        Clause confl = CR_Undef;
        int num_props = 0;
        while (qhead_ < trail_.size()) {
            int p = trail_.get(qhead_++);

            // 'p' is enqueued fact to propagate.
            ArrayList<Watcher> ws = watches_.get(p);
            num_props++;
            int i = 0;
            int j = 0;
            while (ws != null && i < ws.size()) {
                // Try to avoid inspecting the clause:
                int blocker = ws.get(i).blocker;
                if (valueLit(blocker) == Boolean.lTrue) {
                    ws.set(j++, ws.get(i++));
                    continue;
                }

                // Make sure the false literal is data[1]:
                Clause cr = ws.get(i).clause;
                final int false_lit = neg(p);
                if (cr._g(0) == false_lit) {
                    cr._s(0, cr._g(1));
                    cr._s(1, false_lit);
                }
                assert (cr._g(1) == false_lit);
                i++;

                // If 0th watch is true, then clause is already satisfied.
                final int first = cr._g(0);
                Watcher w = new Watcher(cr, first);
                if (first != blocker && valueLit(first) == Boolean.lTrue) {
                    ws.set(j++, w);
                    continue;
                }

                // Look for new watch:
                boolean cont = false;
                for (int k = 2; k < cr.size(); k++) {
                    if (valueLit(cr._g(k)) != Boolean.lFalse) {
                        cr._s(1, cr._g(k));
                        cr._s(k, false_lit);
                        ArrayList<Watcher> lw = watches_.get(neg(cr._g(1)));
                        if (lw == null) {
                            lw = new ArrayList<>();
                            watches_.put(neg(cr._g(1)), lw);
                        }
                        lw.add(w);
                        cont = true;
                        break;
                    }
                }

                // Did not find watch -- clause is unit under assignment:
                if (!cont) {
                    ws.set(j++, w);
                    if (valueLit(first) == Boolean.lFalse) {
                        confl = cr;
                        qhead_ = trail_.size();
                        // Copy the remaining watches_:
                        while (i < ws.size()) {
                            ws.set(j++, ws.get(i++));
                        }
                        touched_variables_.add(first);
                    } else {
                        uncheckedEnqueue(first, cr);
                    }
                }
            }
            if (ws != null) {
                if (ws.size() > j) {
                    ws.subList(j, ws.size()).clear();
                }
            }
        }
        propagations += num_props;
        return confl;
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
        model.clear();
        conflict.clear();
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

        } else if (status == ESat.FALSE && conflict.size() == 0)
            ok_ = false;

        cancelUntil(0);
        if (status == ESat.TRUE) {
            System.out.print("SAT\n");
            for (int i = 0; i < nVars(); i++)
                if (model.get(i) != Boolean.lUndef)
                    System.out.printf("%s%s%d", (i == 0) ? "" : " ",
                            (model.get(i) == Boolean.lTrue) ? "" : "-", i + 1);
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
        TIntList learnt_clause = new TIntArrayList();

        for (; ; ) {
            Clause confl = propagate();
            if (confl != CR_Undef) {
                // CONFLICT
                conflicts++;
                conflictC++;
                if (trailMarker() == 0) return ESat.FALSE;

                learnt_clause.clear();
                backtrack_level = analyze(confl, learnt_clause);
                cancelUntil(backtrack_level);

                for (int v = 0; v < nVars(); v++) {
                    assert valueVar(v) != Boolean.lUndef || order_heap.contains(v) : v + " not heaped";
                }

                if (learnt_clause.size() == 1) {
                    uncheckedEnqueue(learnt_clause.get(0));
                } else {
                    Clause cr = new Clause(learnt_clause.toArray(), true);
                    learnts.add(cr);
                    attachClause(cr);
                    claBumpActivity(cr);
                    uncheckedEnqueue(learnt_clause.get(0), cr);
                }

                varDecayActivity();
                claDecayActivity();


                if (--learntsize_adjust_cnt == 0) {
                    learntsize_adjust_confl *= learntsize_adjust_inc;
                    learntsize_adjust_cnt = (int) learntsize_adjust_confl;
                    max_learnts *= learntsize_inc;
                }

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
                    // Reduce the set of learnt clauses:
                    reduceDB();

                // New variable decision:
                decisions++;
                int next = pickBranchLit();

                if (next == litUndef)
                    // Model found:
                    return ESat.TRUE;

                // Increase decision level and enqueue 'next'
                pushTrailMarker();
                uncheckedEnqueue(next, CR_Undef);
            }
        }

    }

    int pickBranchLit() {
        int next = varUndef;

        // Random decision:
        if (rand.nextDouble() < random_var_freq && !order_heap.isEmpty()) {
            next = order_heap.get(rand.nextInt(order_heap.size()));
            if (valueVar(next) == Boolean.lUndef && decision.get(next))
                rnd_decisions++;
        }

        // Activity based decision:
        while (next == varUndef || valueVar(next) != Boolean.lUndef || !decision.get(next))
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

    int analyze(Clause confl, TIntList out_learnt) {
        int pathC = 0;
        int p = litUndef;

        // Generate conflict clause:
        //
        out_learnt.add(litUndef);      // (leave room for the asserting literal)
        int index = trail_.size() - 1;

        do {
            assert (confl != CR_Undef); // (otherwise should be UIP)
            Clause c = confl;

            if (c.learnt())
                claBumpActivity(c);

            for (int j = (p == litUndef) ? 0 : 1; j < c.size(); j++) {
                int q = c._g(j);

                if (!seen.get(var(q)) && level(var(q)) > 0) {
                    varBumpActivity(var(q));
                    seen.set(var(q));
                    if (level(var(q)) >= trailMarker())
                        pathC++;
                    else
                        out_learnt.add(q);
                }
            }

            // Select next clause to look at:
            //noinspection StatementWithEmptyBody
            while (!seen.get(var(trail_.get(index--)))) ;
            p = trail_.get(index + 1);
            confl = reason(var(p));
            seen.clear(var(p));
            pathC--;

        } while (pathC > 0);
        out_learnt.set(0, neg(p));

        // Simplify conflict clause:
        //
        int i, j;
        analyze_toclear.clear();
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
            for (i = j = 1; i < out_learnt.size(); i++) {
                int x = var(out_learnt.get(i));

                if (reason(x) == CR_Undef)
                    out_learnt.set(j++, out_learnt.get(i));
                else {
                    Clause c = reason(var(out_learnt.get(i)));
                    for (int k = 1; k < c.size(); k++)
                        if (!seen.get(var(c._g(k))) && level(var(c._g(k))) > 0) {
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
        int out_btlevel;
        if (out_learnt.size() == 1)
            out_btlevel = 0;
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

        for (j = 0; j < analyze_toclear.size(); j++)
            seen.clear(var(analyze_toclear.get(j)));    // ('seen[]' is now cleared)
        return out_btlevel;
    }

    boolean simplify() {
        assert (trailMarker() == 0);

        if (!ok_ || propagate() != CR_Undef)
            return ok_ = false;
        //System.err.println("TODO : simplify() ");
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
            if (decision.get(v) && valueVar(v) == Boolean.lUndef)
                vs.add(v);
        order_heap.build(vs);
    }

    void reduceDB() {
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
        learnts.subList(j, learnts.size()).clear();
    }


    boolean withinBudget() {
        return !asynch_interrupt &&
                (conflict_budget < 0 || conflicts < conflict_budget) &&
                (propagation_budget < 0 || propagations < propagation_budget);
    }

    Clause reason(int x) {
        return vardata.get(x).cr;
    }

    int level(int x) {
        return vardata.get(x).level;
    }

    boolean locked(Clause c) {
        return valueLit(c._g(0)) == Boolean.lTrue
                && reason(var(c._g(0))) != CR_Undef
                && reason(var(c._g(0))) == c;
    }

    void removeClause(Clause cr) {
        detachClause(cr);
        // Don't leave pointers to free'd memory!
        if (locked(cr)) {
            vardata.get(var(cr._g(0))).cr = CR_Undef;
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
            // Rescale:
            for (int i = 0; i < nVars(); i++)
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

    static Boolean makeBoolean(boolean b) {
        return (b ? Boolean.lTrue : Boolean.lFalse);
    }

    private static Boolean xor(Boolean a, boolean b) {
        return Boolean.make((byte) (a.value() ^ (b ? 1 : 0)));
    }


    /**
     * Clause -- a simple class for representing a clause
     * <br/>
     *
     * @author Charles Prud'homme, Laurent Perron
     * @since 12/07/13
     */
    public static class Clause {

        private final int[] literals_;
        private final boolean learnt;
        private double activity;

        Clause(int[] ps, boolean learnt) {
            literals_ = ps.clone();
            this.learnt = learnt;
        }

        Clause(int[] ps) {
            this(ps, false);
        }

        public int size() {
            return literals_.length;
        }

        public boolean learnt() {
            return learnt;
        }


        public int _g(int i) {
            return literals_[i];
        }

        void _s(int pos, int l) {
            literals_[pos] = l;
        }

        public String toString() {
            return Arrays.toString(literals_);
        }
    }

    /**
     * // A watcher represent a clause attached to a literal.
     * <br/>
     * (or-tools, booleans.cc, ty L. Perron).
     *
     * @author Charles Prud'homme
     * @since 12/07/13
     */
    static class Watcher {

        Clause clause;
        int blocker;

        Watcher(final Clause cr, int l) {
            this.clause = cr;
            this.blocker = l;
        }
    }

    /**
     * <br/>
     * (or-tools, booleans.cc, ty L. Perron).
     *
     * @author Charles Prud'homme, Laurent Perron
     * @since 12/07/13
     */
    public enum Boolean {

        lTrue((byte) 0),
        lFalse((byte) 1),
        lUndef((byte) 2);


        byte value;

        Boolean(byte value) {
            this.value = value;
        }

        public byte value() {
            return value;
        }

        public static Boolean make(byte b) {
            if (b == 1) return lTrue;
            else if (b == 0) return lFalse;
            else return lUndef;
        }

    }

    static class VarData {
        Clause cr;
        int level;

        public VarData(Clause cr, int level) {
            this.cr = cr;
            this.level = level;
        }
    }
}