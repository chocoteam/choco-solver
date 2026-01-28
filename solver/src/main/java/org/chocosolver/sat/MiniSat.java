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
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.stack.TIntStack;
import gnu.trove.stack.array.TIntArrayStack;
import org.chocosolver.solver.Settings;
import org.chocosolver.solver.variables.impl.LitVar;

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
@SuppressWarnings("FieldCanBeLocal")
public class MiniSat implements SatFactory {

    static final int DEBUG = 0;
    // Value of an undefined variable
    static final int varUndef = -1;
    // value of an undefined literal
    static final int litUndef = -2;

    public static final int lTrue = 0b01;
    public static final int lFalse = 0b10;
    public static final int lUndef = 0b11;
    public static final int TMP_VAR_TYPE = 2;
    // undefined clause
    protected static ThreadLocal<Integer> clauseCounter = ThreadLocal.withInitial(() -> 0);
    private final Comparator<Clause> comp = Comparator.<Clause>comparingInt(c -> -c.lbd)
                    .thenComparingDouble(c -> c.activity);
    public static final Clause C_Undef = Clause.undef();
    static final Clause R_Undef = Reason.undef();
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
    final ArrayList<Clause> learnts = new ArrayList<>();
    // 'watches_[lit]' is a list of constraints watching 'lit'(will go
    // there if literal becomes true).
    private final TIntObjectHashMap<ArrayList<Watcher>> watches_ = new TIntObjectHashMap<>();
    // The current assignments.
    final TIntArrayList assignment_ = new TIntArrayList();
    // Assignment stack; stores all assignments made in the order they
    // were made.
    final TIntArrayList trail_ = new TIntArrayList();
    // Separator indices for different decision levels in 'trail_'.
    final TIntArrayList trail_markers_ = new TIntArrayList();
    // Head of queue(as index into the trail_).
    int qhead_;
    // Number of variables
    int num_vars_;
    int rootlvl = 0;
    private final int ccmin_mode = Settings.PARAM_CLAUSE_MINIMISATION; // Controls conflict clause minimization (0=none, 1=local, 2=recursive)
    private double cla_inc = 1;
    private final double clause_decay = 0.999;
    int learnt_first_removable = 0; // index of first removable learnt clause (related to #reduceDB only).

    double learntsize_adjust_confl = 100;
    int learntsize_adjust_cnt = 100;
    final double learntsize_adjust_inc = 1.5;
    final double learntsize_inc = 1.1;
    final double learntsize_factor = 1 / 3d;
    int propagations;
    final ArrayList<VarData> vardata = new ArrayList<>();
    final ArrayList<ChannelInfo> cinfo = new ArrayList<>();
    private int max_literals;
    private int tot_literals;
    private int clauses_literals;
    private int learnts_literals;
    double max_learnts;
    private final TIntStack analyze_stack = new TIntArrayStack();
    private final TIntArrayList temporary_add_vector_ = new TIntArrayList();
    public final TIntStack temporary_variables = new TIntArrayStack();
    private long[] levels = new long[ccmin_mode == 2 ? 1 << 8 : 0];
    private int[] minRank = new int[ccmin_mode == 2 ? 1 << 8 : 0];
    private long analysisRound;
    private long notKeep;
    private long keep;

    /**
     * Create a new instance of MiniSat solver.
     */
    public MiniSat(boolean addTautology) {
        this.ok_ = true;
        this.qhead_ = 0;
        num_vars_ = 0;
        assignment_.add(lUndef); // required because variable are numbered from 1
        if (addTautology) {
            // true literal
            int v = newVariable();
            int l = makeLiteral(v, true);
            assignment_.set(v, makeBoolean(sgn(l)));
            vardata.set(v, new VarData(R_Undef, trailMarker(), trail_.size()));
            trail_.add(l);
            // false literal
            v = newVariable();
            l = makeLiteral(v, false);
            assignment_.set(v, makeBoolean(sgn(l)));
            vardata.set(v, new VarData(R_Undef, trailMarker(), trail_.size()));
            trail_.add(l);
        }
        clauseCounter.set(2);
        analysisRound = 1; // Starts at 1 so that 0 means unvisited
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
        return v;
    }

    /**
     * Reuse or create a temporary variable.
     * A temporary variable is released upon backtrack.
     *
     * @return a variable
     */
    public int newTemporaryVariable() {
        int var;
        if (temporary_variables.size() > 0) {
            var = temporary_variables.pop();
        } else {
            var = newVariable(new MiniSat.ChannelInfo(null, TMP_VAR_TYPE, -1, -1, false));
        }
        return MiniSat.makeLiteral(var, true);
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

    /**
     * Add a learnt clause to the solver.
     * If the param unforgettable is true, the clause will not be removed during {@link #doReduceDB()}.
     * This is useful for clauses that prohibit certain parts of the search space (e.g. solution clauses).
     *
     * @param learnt_clause the clause to add
     * @param unforgettable if true, the clause will not be removed during {@link #doReduceDB()}
     */
    public void addLearnt(TIntList learnt_clause, boolean unforgettable) {
        if (learnt_clause.size() == 1) {
            uncheckedEnqueue(learnt_clause.get(0));
        } else {
            Clause cr = new Clause(learnt_clause, true);
            learnts.add(cr);
            if (unforgettable) { // in the case of a solution, for instance.
                if (learnts.size() > 1) {
                    // swap the clause with the first removable clause
                    learnts.set(learnts.size() - 1, learnts.get(learnt_first_removable));
                    // replace it by the new clause
                    learnts.set(learnt_first_removable, cr);
                }
                // increment the index
                learnt_first_removable++;
            }
            attachClause(cr);
            claBumpActivity(cr);
            uncheckedEnqueue(learnt_clause.get(0), cr);
        }
        claDecayActivity();

        if (--learntsize_adjust_cnt == 0) {
            learntsize_adjust_confl *= learntsize_adjust_inc;
            learntsize_adjust_cnt = (int) learntsize_adjust_confl;
            max_learnts *= learntsize_inc;
        }
    }

    /**
     * Compute the literals blocks distance of the current clause
     *
     * @param cr a clause
     */
    private void computeLBD(Clause cr) {
        if (cr.size() == 1) {
            cr.lbd = 1;
        } else if (cr.size() == 2) {
            cr.lbd = level(var(cr._g(0))) == level(var(cr._g(1))) ? 1 : 2;
        } else {
            int maxLvl = level(var(cr._g(0))) + 1;
            if (levels.length <= maxLvl) {
                levels = new long[(int) (maxLvl * 1.2)];
            }
            analysisRound++;
            assert analysisRound > 0;
            int lbd = 0;
            for (int i = 0; i < cr.size(); i++) {
                if (levels[level(var(cr._g(i)))] != analysisRound) {
                    lbd++;
                    levels[level(var(cr._g(i)))] = analysisRound;
                }
            }
            cr.lbd = Math.min(lbd, cr.lbd);
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
                if (cinfo.get(x).cons_type == TMP_VAR_TYPE) {
                    // recycle
                    temporary_variables.push(x);
                }
                if (DEBUG > 0) {
                    if (DEBUG > 1)
                        System.out.printf("Unfix %s\n", printLit(trail_.get(c)));
                    else
                        System.out.printf("Unfix %d\n", trail_.get(c));
                }
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
        max_learnts = nClauses() * learntsize_factor;
        learntsize_adjust_confl = 100;
        learntsize_adjust_cnt = (int) learntsize_adjust_confl;
        simplify();
        // todo: reset analysisRound and related structures
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


    int incrementVariableCounter() {
        return num_vars_++;
    }

    public int nVars() {
        return num_vars_;
    }

    // Begins a new decision level.
    public void pushTrailMarker() {
        trail_markers_.add(trail_.size());
    }

    /**
     * Indicates if all literals but one are false in the clause.
     * By implication, the remaining literal is the asserting literal.
     * By construction, the literal at index 0 is the asserting literal and should not be considered.
     *
     * @param sat the solver
     * @param c   the clause to consider
     * @return true if all literals but one (at index 0) are false, false otherwise
     * @implNote This method can only be called on clauses learnt during propagation.
     * Indeed, the method assumes that the literal at index 0 is the asserting literal and all other literals are false.
     * In such situation, the clause is a reason (i.e. it is the reason of a propagation).
     * If the clause has been propagated once (ie, is not a reason anymore), the clause may be in any order
     * or may not be unit anymore.
     */
    private static boolean isAssertingClause(MiniSat sat, Clause c) {
        for (int i = 1; i < c.size(); i++) {
            if (sat.valueLit(c._g(i)) != MiniSat.lFalse) {
                return false;
            }
        }
        return true;
    }

    // Enqueue a literal. Assumes value of literal is undefined.
    public void uncheckedEnqueue(int l, Clause from) {
        assert valueLit(l) == lUndef : "l: " + printLit(l) + " from: " + from;
        assert isAssertingClause(this, from.getConflict()) : "the reason " + showReason(from) + " is not valid because it is not unit";
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
        if (from.learnt()) {
            computeLBD(from);
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
        assert r != null : "reason is null for " + printLit(l);
        assert isAssertingClause(this, r.getConflict()) : "the reason " + showReason(r) + " is not valid because it is not unit";
        int v = var(l);
        if (valueLit(l) == lFalse) {
            if (r == R_Undef) {
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
        int level = findConflictLevel();
        cancelUntil(level);
        if (level <= rootlvl) return level;
        int p = litUndef;
        // Generate conflict clause:
        analysisRound += 2; // Reset the marks
        notKeep = analysisRound;
        keep = analysisRound + 1;
        assert analysisRound > 0; // no underflow allowed
        analyseConflict(confl, out_learnt, p);
        replaceUnreliableLits(out_learnt);
        int j;
        if (DEBUG > 1) System.out.printf("Before minimisation (%d) : %s\n", ccmin_mode, out_learnt);
        max_literals += out_learnt.size();

        // Simplify conflict clause:
        if (ccmin_mode == 1) {
            j = localMinimisation(out_learnt);
            out_learnt.remove(j, out_learnt.size() - j);
        } else if (ccmin_mode == 2) {
            j = recursiveMinimisation(out_learnt);
            out_learnt.remove(j, out_learnt.size() - j);
        }
        tot_literals += out_learnt.size();

        // Find correct backtrack level:
        return getBacktrackLevel(out_learnt);
    }

    private void analyseConflict(Clause confl, TIntList out_learnt, int p) {
        int pathC = 0;
        out_learnt.add(litUndef);      // (leave room for the asserting literal)
        int index = trail_.size() - 1;

        do {
            assert (confl != C_Undef); // (otherwise should be UIP)
            pathC = updateNogood(confl, out_learnt, p, pathC);
            // Select next clause to look at:
            //noinspection StatementWithEmptyBody
            while (vardata.get(var(trail_.get(index--))).mark != analysisRound);
            p = trail_.get(index + 1);
            confl = getConfl(p);
            vardata.get(var(p)).mark--;
            if (DEBUG > 1) System.out.printf("clear %d l:%d\n", var(p), p);
            // Ignore the type of the literal when the biclique factorisation is inactive
            if (!Settings.PARAM_BICLIQUE_FACTORISATION
                    || cinfo.get(var(p)).cons_type != TMP_VAR_TYPE) {
                pathC--;
            }
            assert pathC >= 0 : "Something goes wrong with the UIP";
            if (DEBUG > 1) System.out.printf("path-- (%d)\n", pathC);
        } while (pathC > 0 || !cinfo.get(var(p)).reliable);
        out_learnt.set(0, neg(p));
    }

    private int updateNogood(Clause c, TIntList out_learnt, int lit_p, int pathC) {
        if (DEBUG > 0) {
            if (lit_p != litUndef) {
                c._s(0, lit_p);
            }
            if (DEBUG > 1)
                System.out.printf("%s\n", c.toString(this));
            else System.out.printf("%s\n", c.toString());
        }

        if (c.learnt())
            claBumpActivity(c);
        // Check the type of the literal only if the biclique factorisation is active
        if (Settings.PARAM_BICLIQUE_FACTORISATION
                && lit_p != litUndef && cinfo.get(var(lit_p)).cons_type == TMP_VAR_TYPE) {
            // if this is a factor, then it should have been already expanded
            return pathC;
        }
        for (int j = (lit_p == litUndef) ? 0 : 1; j < c.size(); j++) {
            int lit_q = c._g(j);
            int var_q = var(lit_q);
            if (level(var_q) > rootlvl) {
                assert lit_p == litUndef || pos(var(lit_p)) > pos(var_q) : "chronological inconsistency :(" + printLit(lit_q) + " @ " + pos(lit_q) +
                        ") is explained by an older event (" + printLit(lit_q) + " @ " + pos(var_q) + ") " + c;
                pathC = updateNogoodRec(out_learnt, lit_q, pathC);
            }
        }
        return pathC;
    }

    private int updateNogoodRec(TIntList out_learnt, int lit_p, int pathC) {
        int var_p = var(lit_p);
        if (vardata.get(var_p).mark != analysisRound) {
            if (DEBUG > 1) System.out.printf("mark %d\n", var_p);
            vardata.get(var_p).mark = analysisRound;
            // Check the type of the literal only if the biclique factorisation is active
            if (Settings.PARAM_BICLIQUE_FACTORISATION &&
                    cinfo.get(var_p).cons_type == TMP_VAR_TYPE) {
                // we recursively expand the factor to get the real reason
                Clause c = getConfl(lit_p);
                for (int j = (lit_p == litUndef) ? 0 : 1; j < c.size(); j++) {
                    int lit_q = c._g(j);
                    int var_q = var(lit_q);
                    if (level(var_q) > rootlvl) {
                        assert lit_p == litUndef || pos(var(lit_p)) > pos(var_q) : "chronological inconsistency :(" + printLit(lit_q) + " @ " + pos(lit_q) +
                                ") is explained by an older event (" + printLit(lit_q) + " @ " + pos(var_q) + ") " + c;
                        pathC = updateNogoodRec(out_learnt, lit_q, pathC);
                    }
                }
            } else {
                varBumpActivity(var_p);
                if (level(var_p) >= trailMarker()) {
                    if (DEBUG > 1) System.out.printf("path++ (%d -- %d >= %d)\n", pathC, level(var_p), trailMarker());
                    pathC++;
                } else {
                    out_learnt.add(lit_p);
                    if (DEBUG > 1) System.out.printf("out %d\n", lit_p);
                }
            }
        }
        return pathC;
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
            assert cinfo.get(var(p)).cons_type != TMP_VAR_TYPE : "no factor is allowed in a nogood";
            if (DEBUG > 0) {
                System.out.printf("replacing %s in %s\n", p, out_learnt);
            }
            i = replaceUnreliableLit(out_learnt, p, i);
        }
        while (!temporary_add_vector_.isEmpty()) {
            vardata.get(var(temporary_add_vector_.removeAt(temporary_add_vector_.size() - 1))).mark--;
        }
    }

    private int replaceUnreliableLit(TIntList out_learnt, int p, int i) {
        Clause c = getConfl(neg(p));
        temporary_add_vector_.add(p);
        int at = out_learnt.size() - 1;
        out_learnt.set(i, out_learnt.get(at));
        out_learnt.removeAt(at);
        i--;
        for (int j = 1; j < c.size(); j++) {
            int q = c._g(j);
            if (vardata.get(var(q)).mark != analysisRound) {
                vardata.get(var(q)).mark = analysisRound;
                out_learnt.add(q);
            }
        }
        return i;
    }

    private int localMinimisation(TIntArrayList out_learnt) {
        int j;
        int i;

        for (i = j = 1; i < out_learnt.size(); i++) {
            // If the reason of the literal is not included in the initial no-good, then keep it in the no-good
            if (reason(var(out_learnt.get(i))) == C_Undef
                    || !isIncludedIterativeVersion(out_learnt.get(i))) {
                assert level(var(out_learnt.get(i))) > rootlvl : "Not supposed to keep literals from the initial propagation";
                out_learnt.set(j++, out_learnt.get(i));
            }
            // If the reason of the literal is included in the initial no-good, then remove it from the no-good
        }
        return j;
    }

    private boolean isIncludedRecursiveVersion(int p) {
        Clause c = getConfl(p);
        for (int k = 1; k < c.size(); k++) {
            int q = c._g(k); // The literal (either negative or positive)
            int v = var(q); // The boolean variable associated with the literal
            //TODO: replace this test by a "reliable" test ?
            if (level(v) > rootlvl && cinfo.get(v).cons_type == TMP_VAR_TYPE) { // The literal is a factor so we check its reason instead
                assert cinfo.get(var(p)).cons_type != TMP_VAR_TYPE : "Two factors are not supposed to be linked by an arc";
                if (vardata.get(v).mark < analysisRound) { // not marked
                    if (isIncludedIterativeVersion(q)) {
                        vardata.get(v).mark = notKeep;
                    } else {
                        vardata.get(v).mark = keep;
                        return false;
                    }
                } else if (vardata.get(v).mark == keep) {
                    return false;
                }
            } else if (level(v) > rootlvl && vardata.get(v).mark < analysisRound) { // The literal is neither a factor nor from the initial propagation but is not present in the initial no-good
                return false;
            }
        }
        return true;
    }

    private boolean isIncludedIterativeVersion(int p_start) {
        analyze_stack.clear();
        analyze_stack.push(p_start);
        while (analyze_stack.size() > 0) {
            int p = analyze_stack.pop();
            int v_p = var(p);
            if ((p != p_start && vardata.get(v_p).mark >= analysisRound) || (level(v_p) <= rootlvl)) continue;
            Clause c = getConfl(p);
            for (int k = 1; k < c.size(); k++) {
                int q = c._g(k); // The literal (either negative or positive)
                int v_q = var(q); // The boolean variable associated with the literal
                if (level(v_q) <= rootlvl) continue;
                if (cinfo.get(v_q).cons_type == TMP_VAR_TYPE) { // The literal is a factor so we check its reason instead
                    assert cinfo.get(var(p)).cons_type != TMP_VAR_TYPE : "Currently, two factors are not supposed to be linked by an arc";
                    if (vardata.get(v_q).mark < analysisRound) { // not marked
                        analyze_stack.push(q);
                        vardata.get(v_q).parent = v_p;
                    } else if (vardata.get(v_q).mark == keep) {
                        int cur = v_p;
                        while (cur != var(p_start)) {
                            vardata.get(cur).mark = keep;
                            cur = vardata.get(cur).parent;
                        }
                        return false;
                    }
                } else if (vardata.get(v_q).mark < analysisRound) { // The literal is neither a factor nor from the initial propagation but is not present in the initial no-good
                    int cur = v_p;
                    while (cur != var(p_start)) {
                        vardata.get(cur).mark = keep;
                        cur = vardata.get(cur).parent;
                    }
                    return false;
                }
            }
            if (p != p_start) {
                vardata.get(v_p).mark = notKeep;
            }
        }
        return true;
    }

    private int recursiveMinimisation(TIntArrayList out_learnt) {
        int i;
        int j;
        // adapt size if needed //TODO: change this at some point ?
        if (minRank.length < trailMarker()) {
            levels = new long[(int) (trailMarker() * 1.2)];
            minRank = new int[(int) (trailMarker() * 1.2)];
        }
//        analysisRound += 2;
//        assert analysisRound > 0; // no underflow allowed
//        notKeep = analysisRound;
//        keep = analysisRound + 1;

        // Reset minRank (only concerned levels)
        for (i = 1; i < out_learnt.size(); i++) {
            int v = var(out_learnt.get(i));
            minRank[level(v)] = Integer.MAX_VALUE;
        }
        // Update the required structures
        for (i = 1; i < out_learnt.size(); i++) {
            int v = var(out_learnt.get(i));
            levels[level(v)] = analysisRound;
            minRank[level(v)] = Math.min(minRank[level(v)], pos(v));
//            vardata.get(v).mark = notKeep;
        }

        for (i = j = 1; i < out_learnt.size(); i++) {
            // If the literal is not dominated by the initial no-good, then keep it in the no-good
            if (reason(var(out_learnt.get(i))) == C_Undef
                    || !isDominatedIterativeVersion(out_learnt.get(i))) {
                out_learnt.set(j++, out_learnt.get(i));
            }
            // If the literal is dominated by the initial no-good, then remove it from the no-good
        }
        return j;
    }

    /**
     * Check if 'p' is dominated by the no-good in the implication graph. 'levels' and 'minRank' are used to abort early if the algorithm is
     * visiting literals that can not be dominated by the no-good.
     */
    boolean isDominatedRecursiveVersion(int p, int root) {
        int v = var(p);
        // IF the literal is marked as dominated and is not the root OR the literal is from the initial propagation THEN we may remove the root from the no-good
        if ((p != root && vardata.get(v).mark == notKeep) || (level(v) <= rootlvl)) {
            return true;
        }
        // ELSE-IF the literal is marked as notDominated OR it is a decision OR it can not be dominated by literals from the no-good THEN we must keep the root in the no-good
        else if (vardata.get(v).mark == keep
                || reason(v) == C_Undef
                || levels[level(v)] != analysisRound
                || pos(v) < minRank[level(v)]) {
            vardata.get(v).mark = keep; // It is not necessary to mark the literal here, this is just an optimisation to avoid testing the other assertions in the future
            return false;
        }
        // ELSE check the predecessors, but ignore the literals from the initial propagation
        else {
            Clause c = getConfl(p);
            // quick fix for recursive version -- no need to adapt to binary clause
            // TODO: remove
            if (c.size() == 3) {
                c = new Clause(new int[]{0, c._g(1), c._g(2)});
            }
            for (int i = 1; i < c.size(); i++) {
                int r = c._g(i);
                if (level(var(r)) > rootlvl
                        && !isDominatedRecursiveVersion(r, root)) {
                    if (p != root) { // The root is from the initial no-good, and literals from the initial no-good must remain marked as dominated
                        assert vardata.get(v).mark < analysisRound : "Not supposed to re-mark literals";
                        vardata.get(v).mark = keep;
                    }
                    return false;
                }
            }
            if (p != root) { // The root is from the initial no-good, and literals from the initial no-good are already marked as dominated
                assert vardata.get(v).mark < analysisRound : "Not supposed to re-mark literals";
                vardata.get(v).mark = notKeep;
            }
            return true;
        }
    }

    private boolean isDominatedIterativeVersion(int p_start) {
        analyze_stack.clear();
        analyze_stack.push(p_start);
        while (analyze_stack.size() > 0) {
            int p = analyze_stack.pop();
            int v_p = var(p);
            // IF the literal is marked as dominated and is not the root OR the literal is from the initial propagation THEN we may remove the root from the no-good
            if ((p != p_start && vardata.get(v_p).mark >= analysisRound) || (level(v_p) <= rootlvl)) continue;
            Clause c = getConfl(p);
            for (int k = 1; k < c.size(); k++) {
                int q = c._g(k); // The literal (either negative or positive)
                int v_q = var(q); // The boolean variable associated with the literal
                // IF the literal is marked as dominated and is not the root OR the literal is from the initial propagation THEN we may remove the root from the no-good
                if ((vardata.get(v_q).mark == notKeep) || (level(v_q) <= rootlvl)) continue;
                // ELSE-IF the literal is marked as notDominated OR it is a decision OR it can not be dominated by literals from the no-good THEN we must keep the root in the no-good
                if (vardata.get(v_q).mark == keep
                        || reason(v_q) == C_Undef
                        || levels[level(v_q)] != analysisRound
                        || pos(v_q) < minRank[level(v_q)]) {
                    vardata.get(v_q).mark = keep;
                    int cur = v_p;
                    while (cur != var(p_start)) {
                        vardata.get(cur).mark = keep;
                        cur = vardata.get(cur).parent;
                    }
                    return false;
                } else if (vardata.get(v_q).mark < analysisRound) { // not marked
                    analyze_stack.push(q);
                    vardata.get(v_q).parent = v_p;
                }
            }
            if (p != p_start) {
                vardata.get(v_p).mark = notKeep;
            }
        }
        return true;
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
//        if (nAssigns() == simpDB_assigns || (simpDB_props > 0))
//            return true;

        // Remove satisfied clauses:
        removeSatisfied(learnts);
//        if (remove_satisfied)        // Can be turned off.
//            removeSatisfied(clauses);

        return true;
    }

    void removeSatisfied(ArrayList<Clause> cs) {
        int i, j;
        for (i = j = 0; i < cs.size(); i++) {
            Clause c = cs.get(i);
            if (satisfied(c)) {
                removeClause(cs.get(i));
                if (i < learnt_first_removable) { // maintain the index
                    learnt_first_removable--;
                    assert learnt_first_removable >= 0;
                }
            } else {
                cs.set(j++, cs.get(i));

            }
        }
//        cs.shrink(i - j);
        if (cs.size() > j) {
            cs.subList(j, cs.size()).clear();
        }
    }

    boolean satisfied(Clause c) {
        for (int i = 0; i < c.size(); i++)
            if (valueLit(c._g(i)) == lTrue) {
                return true;
            }
        return false;
    }

    public void deleteAllLearnedClauses() {
        for (int i = 0; i < learnts.size(); i++) {
            Clause c = learnts.get(i);
            removeClause(c);
        }
        learnts.clear();
    }

    public void doReduceDB() {
        int i, j;
        double extra_lim = cla_inc / learnts.size();    // Remove any clause below this activity
        learnts.subList(learnt_first_removable, learnts.size())  // only removable clauses
                .sort(comp);
        // Don't delete binary or locked clauses or unforgettable clauses.
        // From the rest, delete clauses from the first half
        // and clauses with activity smaller than 'extra_lim':
        int deleted = (learnts.size() - learnt_first_removable) / 2;
        for (i = j = learnt_first_removable; i < learnts.size(); i++) {
            Clause c = learnts.get(i);
            if (c.size() > 2 && !locked(c) && deleted >= 0){ //&& (c.activity < extra_lim)) {
                removeClause(learnts.get(i));
                deleted--;
            } else {
                learnts.set(j++, learnts.get(i));
            }
        }
        int n = learnts.size();
        learnts.subList(j, n).clear();
        // System.out.printf("reduceDB removed %d clauses\n", n - j);
    }

    Clause getConfl(int p) {
        Reason r = reason(var(p));
        return r.getConflict();
    }

    Reason reason(int x) {
        return vardata.get(x).cr;
    }

    public int level(int x) {
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
            // Rescale, only clauses that can be removed with reduceDB
            for (int i = learnt_first_removable; i < learnts.size(); i++) {
                learnts.get(i).activity *= 1e-20d;
            }
            cla_inc *= 1e-20d;
        }
    }

    void varBumpActivity(int v) {
        // empty
    }


    void claDecayActivity() {
        cla_inc *= (1 / clause_decay);
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
                    if (i > 1) st.append(" ∨ ");
                    st.append(printLit(cl._g(i)));
                }
                st.append(")");
                //st.append(" -> ").append(printLit(r.cl._g(0)));
                break;
            //case 1:
            //    ss << "absorbed binary clause?";
            //    break;
            case 2:
                st.append("single literal ").append(printLit(((Reason.Reason1) r).d1));
                break;
            case 3:
                st.append("two literals ").append(printLit(((Reason.Reason2) r).d1))
                        .append(" ∨ ").append(printLit(((Reason.Reason2) r).d2));
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
        private long mark; // used in conflict analysis, denotes status of a lit (analysisRound, keep, notKeep)
        private int parent; // used in conflict minimisation, denotes the parent lit

        public VarData(Reason cr, int level, int pos) {
            this.cr = cr;
            this.level = level;
            this.pos = pos;
            this.mark = 0;
        }

        private void set(Reason cr, int level, int pos) {
            this.cr = cr;
            this.level = level;
            this.pos = pos;
        }

        private void clearReason() {
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