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
package org.chocosolver.solver.constraints.nary.cnf;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * A MiniSat solver.
 * <p>
 * (or-tools, booleans.cc, ty L. Perron).
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 12/07/13
 */
public class SatSolver {


    /**
     * static const Literal kUndefinedLiteral = Literal(-2);
     */
    static final int kUndefinedLiteral = -2;

    // If false, the constraints are already unsatisfiable. No part of
    // the solver state may be used!
    boolean ok_;
    // List of problem addClauses.
    ArrayList<Clause> clauses;
    // List of learnt addClauses.
    ArrayList<Clause> learnts;
    // 'watches_[lit]' is a list of constraints watching 'lit'(will go
    // there if literal becomes true).
    TIntObjectHashMap<ArrayList<Watcher>> watches_;
    // implies_[lit] is a list of literals to set to true if 'lit' becomes true.
    TIntObjectHashMap<TIntArrayList> implies_;
    // The current assignments.
    TIntObjectHashMap<Boolean> assignment_;
    // Assignment stack; stores all assigments made in the order they
    // were made.
    TIntArrayList trail_;
    // Separator indices for different decision levels in 'trail_'.
    TIntArrayList trail_markers_;
    // Head of queue(as index into the trail_.
    int qhead_;
    // Number of variables
    int num_vars_;

    TIntArrayList temporary_add_vector_;
    TIntArrayList touched_variables_;


    public SatSolver() {
        this.ok_ = true;
        this.qhead_ = 0;
        num_vars_ = 0;
        this.clauses = new ArrayList<>();
        this.learnts = new ArrayList<>();
        this.watches_ = new TIntObjectHashMap<>();
        this.implies_ = new TIntObjectHashMap<>();
        this.assignment_ = new TIntObjectHashMap<>();
        this.trail_ = new TIntArrayList();
        this.trail_markers_ = new TIntArrayList();
        this.temporary_add_vector_ = new TIntArrayList();
        this.touched_variables_ = new TIntArrayList();
    }

    // Add a new variable.
    public int newVariable() {
        int v = incrementVariableCounter();
//            watches_.resize(2 * v + 2);
//        implies_.resize(2 * v.value() + 2);
        assignment_.put(v, Boolean.kUndefined);
        return v;
    }


    // Add a clause to the solver.
    boolean addClause(TIntList ps) {
        assert 0 == trailMarker();
        if (!ok_) return false;

        /**
         * std::sort(clause->begin(), clause->end());
         Literal p = kUndefinedLiteral;
         int j = 0;
         for (int i = 0; i < clause->size(); ++i) {
         if (Value((*clause)[i]) == kTrue || (*clause)[i] == Negated(p)) {
         return true;
         } else if (Value((*clause)[i]) != kFalse && (*clause)[i] != p) {
         (*clause)[j++] = p = (*clause)[i];
         }
         }
         clause->resize(j);
         */

        // Check if clause is satisfied and remove false/duplicated literals:
        ps.sort();
        int lit = kUndefinedLiteral;
        int j = 0;
        for (int i = 0; i < ps.size(); i++) {
            if (valueLit(ps.get(i)) == Boolean.kTrue || ps.get(i) == negated(lit)) {
                return true;
            } else if (valueLit(ps.get(i)) != Boolean.kFalse && ps.get(i) != lit) {
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
                return (ok_ = propagate());
            case 2:
                int l0 = ps.get(0);
                int l1 = ps.get(1);
                TIntArrayList i0 = implies_.get(negated(l0));
                if (i0 == null) {
                    i0 = new TIntArrayList();
                    implies_.put(negated(l0), i0);
                }
                i0.add(l1);

                TIntArrayList i1 = implies_.get(negated(l1));
                if (i1 == null) {
                    i1 = new TIntArrayList();
                    implies_.put(negated(l1), i1);
                }
                i1.add(l0);
                break;
            default:
                Clause cr = new Clause(ps.toArray());
                clauses.add(cr);
                attachClause(cr);
                break;

        }
        return true;
    }

    public boolean learnClause(int... ps) {
        Arrays.sort(ps);
        switch (ps.length) {
            case 0:
                return (ok_ = false);
            case 1:
                dynUncheckedEnqueue(ps[0]);
                return (ok_ = propagate());
            default:
                Clause cr = new Clause(ps);
                learnts.add(cr);
                attachClause(cr);
                break;
        }
        return true;
    }

    // Add the empty clause, making the solver contradictory.
    boolean addEmptyClause() {
        temporary_add_vector_.resetQuick();
        return addClause(temporary_add_vector_);
    }

    // Add a unit clause to the solver.
    boolean addClause(int l) {
        temporary_add_vector_.resetQuick();
        temporary_add_vector_.add(l);
        return addClause(temporary_add_vector_);
    }

    // Add a binary clause to the solver.
    boolean addClause(int p, int q) {
        temporary_add_vector_.resetQuick();
        temporary_add_vector_.add(p);
        temporary_add_vector_.add(q);
        return addClause(temporary_add_vector_);
    }

    // Add a ternary clause to the solver.
    boolean addClause(int p, int q, int r) {
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
                assignment_.put(x, Boolean.kUndefined);
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

    // The current value of a variable.
    Boolean valueVar(int x) {
        return assignment_.get(x);
    }

    // The current value of a literal.
    Boolean valueLit(int l) {
        Boolean b = assignment_.get(var(l));
        return b == Boolean.kUndefined ? Boolean.kUndefined : xor(b, sign(l));
    }

    // The current number of original clauses.
    int nClauses() {
        return clauses.size();
    }

    // The current number of original clauses.
    int nLearnt() {
        return learnts.size();
    }

    // Propagates one literal, returns true if successful, false in case
    // of failure.
    boolean propagateOneLiteral(int lit) {
        assert ok_;
        touched_variables_.resetQuick();
        if (!propagate()) {
            return false;
        }
        if (valueLit(lit) == Boolean.kTrue) {
            // Dummy decision level:
            pushTrailMarker();
            return true;
        } else if (valueLit(lit) == Boolean.kFalse) {
            return false;
        }
        pushTrailMarker();
        // Unchecked enqueue
        assert valueLit(lit) == Boolean.kUndefined;
        assignment_.put(var(lit), makeBoolean(!sign(lit)));
        trail_.add(lit);
        return propagate();
    }


    private int incrementVariableCounter() {
        return num_vars_++;
    }

    // Begins a new decision level.
    void pushTrailMarker() {
        trail_markers_.add(trail_.size());
    }

    // Enqueue a literal. Assumes value of literal is undefined.
    void uncheckedEnqueue(int l) {
        assert valueLit(l) == Boolean.kUndefined;
        if (assignment_.get(var(l)) == Boolean.kUndefined) {
            touched_variables_.add(l);
        }
        assignment_.put(var(l), sign(l) ? Boolean.kFalse : Boolean.kTrue);
        trail_.add(l);
    }

    void dynUncheckedEnqueue(int l) {
        touched_variables_.add(l);
    }


    // Test if fact 'p' contradicts current state, Enqueue otherwise.
    boolean enqueue(int l) {
        if (valueLit(l) != Boolean.kUndefined) {
            return valueLit(l) != Boolean.kFalse;
        } else {
            uncheckedEnqueue(l);
            return true;
        }
    }

    // Attach a clause to watcher lists.
    void attachClause(Clause cr) {
        assert cr.size() > 1;
        ArrayList<Watcher> l0 = watches_.get(negated(cr._g(0)));
        if (l0 == null) {
            l0 = new ArrayList<>();
            watches_.put(negated(cr._g(0)), l0);
        }
        ArrayList<Watcher> l1 = watches_.get(negated(cr._g(1)));
        if (l1 == null) {
            l1 = new ArrayList<>();
            watches_.put(negated(cr._g(1)), l1);
        }
        l0.add(new Watcher(cr, cr._g(1)));
        l1.add(new Watcher(cr, cr._g(0)));
    }

    public void detachLearnt(int ci) {
        Clause cr = learnts.get(ci);
        learnts.remove(ci);

        ArrayList<Watcher> ws = watches_.get(negated(cr._g(0)));
        int i = ws.size() - 1;
        while (i >= 0 && ws.get(i).clause != cr) {
            i--;
        }
        assert i > -1;
        ws.remove(i);
        ws = watches_.get(negated(cr._g(1)));
        i = ws.size() - 1;
        while (i >= 0 && ws.get(i).clause != cr) {
            i--;
        }
        assert i > -1;
        ws.remove(i);
    }

    // Perform unit propagation. returns true upon success.
    boolean propagate() {
        boolean result = true;
        while (qhead_ < trail_.size()) {
            int p = trail_.get(qhead_++);
            // Propagate the implies first.
            if(!propagateImplies(p)){
                return false;
            }
            result &= propagateClauses(p);
        }
        return result;
    }

    private boolean propagateClauses(int p) {
        boolean result = true;
        // 'p' is enqueued fact to propagate.
        ArrayList<Watcher> ws = watches_.get(p);

        int i = 0;
        int j = 0;
        while (ws != null && i < ws.size()) {
            // Try to avoid inspecting the clause:
            int blocker = ws.get(i).blocker;
            if (valueLit(blocker) == Boolean.kTrue) {
                ws.set(j++, ws.get(i++));
                continue;
            }

            // Make sure the false literal is data[1]:
            Clause cr = ws.get(i).clause;
            final int false_lit = negated(p);
            if (cr._g(0) == false_lit) {
                cr._s(0, cr._g(1));
                cr._s(1, false_lit);
            }
            assert (cr._g(1) == false_lit);
            i++;

            // If 0th watch is true, then clause is already satisfied.
            final int first = cr._g(0);
            Watcher w = new Watcher(cr, first);
            if (first != blocker && valueLit(first) == Boolean.kTrue) {
                ws.set(j++, w);
                continue;
            }

            // Look for new watch:
            boolean cont = false;
            for (int k = 2; k < cr.size(); k++) {
                if (valueLit(cr._g(k)) != Boolean.kFalse) {
                    cr._s(1, cr._g(k));
                    cr._s(k, false_lit);
                    ArrayList<Watcher> lw = watches_.get(negated(cr._g(1)));
                    if (lw == null) {
                        lw = new ArrayList<>();
                        watches_.put(negated(cr._g(1)), lw);
                    }
                    lw.add(w);
                    cont = true;
                    break;
                }
            }

            // Did not find watch -- clause is unit under assignment:
            if (!cont) {
                ws.set(j++, w);
                if (valueLit(first) == Boolean.kFalse) {
                    result = false;
                    qhead_ = trail_.size();
                    // Copy the remaining watches_:
                    while (i < ws.size()) {
                        ws.set(j++, ws.get(i++));
                    }
                    touched_variables_.add(first);
                } else {
                    uncheckedEnqueue(first);
                }
            }
        }
        if (ws != null) {
            for (int k = ws.size() - 1; k >= j; k--) {
                ws.remove(k);
            }
        }
        return result;
    }

    private boolean propagateImplies(int p) {
        TIntList to_add = implies_.get(p);
        if (to_add != null) {
            for (int i = 0; i < to_add.size(); ++i) {
                if (!enqueue(to_add.get(i))) {
                    touched_variables_.add(to_add.get(i));
                    return false;
                }
            }
        }
        return true;
    }


    /**
     * inline Literal MakeLiteral(Variable var, bool sign) {
     * return Literal(2 * var.value() + static_cast<int>(sign));
     * int(true) is always 1. And int(false) is always 0
     * }
     */
    protected static int makeLiteral(int var, boolean sign) {
        return (2 * var + (sign ? 1 : 0));
    }

    /**
     * inline Literal Negated(Literal p) { return Literal(p.value() ^ 1); }
     */
    public static int negated(int l) {
        return (l ^ 1);
    }


    /**
     * inline bool Sign(Literal p) { return p.value() & 1; }
     * int(true) is always 1. And int(false) is always 0
     */
    protected static boolean sign(int l) {
        return (l & 1) != 0;
    }

    /**
     * inline Variable Var(Literal p) { return Variable(p.value() >> 1); }
     */
    protected static int var(int l) {
        return (l >> 1);
    }

    /**
     * inline Boolean MakeBoolean(bool x) { return Boolean(!x); }
     */
    protected static Boolean makeBoolean(boolean b) {
        return (b ? Boolean.kTrue : Boolean.kFalse);
    }

    /**
     * inline Boolean Xor(Boolean a, bool b) {
     * return Boolean((uint8)(a.value() ^ (uint8) b));
     * }
     */
    protected static Boolean xor(Boolean a, boolean b) {
        return Boolean.make((byte) (a.value() ^ (b ? 1 : 0)));
    }


    /**
     * Clause -- a simple class for representing a clause
     * <br/>
     *
     * @author Charles Prud'homme, Laurent Perron
     * @since 12/07/13
     */
    class Clause {
        private int[] literals_;

        public Clause(int[] ps) {
            literals_ = ps.clone();
        }

        int size() {
            return literals_.length;
        }

        int _g(int i) {
            return literals_[i];
        }

        int _s(int pos, int l) {
            return literals_[pos] = l;
        }

        int pos(int l) {
            int i = literals_.length - 1;
            while (i >= 0 && literals_[i] != l) {
                i--;
            }
            return i;
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
    class Watcher {

        Clause clause;
        int blocker;

        public Watcher(final Clause cr, int l) {
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
    enum Boolean {

        kTrue((byte) 0),
        kFalse((byte) 1),
        kUndefined((byte) 2);


        byte value;

        Boolean(byte value) {
            this.value = value;
        }

        public byte value() {
            return value;
        }

        public static Boolean make(byte b) {
            if (b == 0) return kTrue;
            else if (b == 1) return kFalse;
            else return kUndefined;
        }

    }

    public void copyFrom(SatSolver o) {
        // Then, copy all data structures:
        this.ok_ = o.ok_;
        this.qhead_ = o.qhead_;
        this.num_vars_ = o.num_vars_;
        this.trail_.resetQuick();
        this.trail_.addAll(o.trail_);
        this.trail_markers_.resetQuick();
        this.trail_markers_.addAll(o.trail_markers_);
        this.touched_variables_.resetQuick();
        this.touched_variables_.addAll(o.touched_variables_);
        this.temporary_add_vector_.resetQuick();
        this.temporary_add_vector_.addAll(o.temporary_add_vector_);
        for (int k : o.assignment_.keys()) {
            this.assignment_.putIfAbsent(k, o.assignment_.get(k));
        }
        for (int k : o.implies_.keys()) {
            TIntArrayList tl = this.implies_.get(k);
            if(tl == null){
                tl = new TIntArrayList();
                this.implies_.put(k, tl);
            }else{
                tl.resetQuick();
            }
            tl.addAll(o.implies_.get(k));
        }
        final HashMap<Clause, Clause> map = new HashMap<>();
        this.clauses.clear();
        for (Clause cl : o.clauses) {
            Clause _cl = new Clause(cl.literals_);
            map.put(cl, _cl);
            this.clauses.add(_cl);
        }
        this.learnts.clear();
        for (Clause cl : o.learnts) {
            Clause _cl = new Clause(cl.literals_);
            map.put(cl, _cl);
            this.learnts.add(_cl);
        }
        this.watches_.clear();
        for (int k : o.watches_.keys()) {
            ArrayList<Watcher> ws = o.watches_.get(k);
            ArrayList<Watcher> _ws = new ArrayList<>(ws.size());
            for (Watcher w : ws) {
                _ws.add(new Watcher(map.get(w.clause), w.blocker));
            }
            this.watches_.put(k, _ws);
        }
    }

    public long nbclauses() {
        return clauses.size() + learnts.size() + implies_.size() / 2;
    }

    public long numvars() {
        return num_vars_;
    }

}
