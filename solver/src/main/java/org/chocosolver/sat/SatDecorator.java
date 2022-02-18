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
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.chocosolver.memory.IStateInt;
import org.chocosolver.solver.ICause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.queues.CircularQueue;

import java.util.*;
import java.util.function.Consumer;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 19/03/2021
 */
public class SatDecorator extends MiniSat {

    // store clauses dynamically added from outside
    public ArrayList<Clause> dynClauses = new ArrayList<>();
    private final TIntObjectHashMap<Literalizer> lits = new TIntObjectHashMap<>();
    private final HashMap<Variable, List<Literalizer>> vars = new HashMap<>();
    /**
     * For comparison with SAT solver trail, to deal properly with backtrack
     */
    private final IStateInt sat_trail_;
    /**
     * Since there is no domain-clause, a fix point may not be reached by SatSolver itself.
     * Stores all modified variable to make sure a fix point is reached.
     */
    private final CircularQueue<Variable> toCheck = new CircularQueue<>(16);

    /**
     * List of early deduction literals
     */
    private final TIntList early_deductions_;

    public SatDecorator(Model model) {
        super();
        early_deductions_ = new TIntArrayList();
        sat_trail_ = model.getEnvironment().makeInt();
    }

    /**
     * Add a clause during resolution
     *
     * @param ps clause to add
     */
    public void learnClause(int... ps) {
        Arrays.sort(ps);
        switch (ps.length) {
            case 0:
                ok_ = false;
                return;
            case 1:
                dynUncheckedEnqueue(ps[0]);
                ok_ = (propagate() == CR_Undef);
                return;
            default:
                Clause cr = new Clause(ps);
                removeDominated(cr);
                dynClauses.add(cr);
                attachClause(cr);
                break;
        }
    }

    /**
     * Check wether {@code cr} dominates one or more learnt clauses.
     *
     * @param last the clause to compare the other with
     */
    private void removeDominated(Clause last) {
        for (int c = dynClauses.size() - 1; c >= 0; c--) {
            Clause prev = dynClauses.get(c);
            if (last.size() < prev.size()) {
                int i = 0, j = 0;
                while (i < last.size() && j < prev.size()) {
                    int l = last._g(i);
                    int p = prev._g(j);
                    if (l < p) break;
                    j++;
                    if (l == p) {
                        i++;
                    }
                }
                if (i == last.size() && j == prev.size()) {
                    // then 'last' dominates 'prev'
                    detachLearnt(c);
                }
            }
        }
    }

    public void detachLearnt(int ci) {
        Clause cr = dynClauses.get(ci);
        detachClause(cr);
        dynClauses.remove(ci);
    }

    private void dynUncheckedEnqueue(int l) {
        touched_variables_.add(l);
    }

    public int nLearnt() {
        return dynClauses.size();
    }

    public ESat value(int svar) {
        switch (valueVar(svar)) {
            case lFalse:
                return ESat.FALSE;
            case lTrue:
                return ESat.TRUE;
            default:
            case lUndef:
                return ESat.UNDEFINED;
        }
    }

    /**
     * Propagates one literal, returns true if successful, false in case of failure.
     *
     * @param lit literal to propagate
     * @return {@code false} if a failure occurs.
     * @implNote A call to this fill {@link #touched_variables_} with modified literals.
     */
    public boolean propagateOneLiteral(int lit) {
        assert ok_;
        touched_variables_.resetQuick();
        if (propagate() != CR_Undef) {
            return false;
        }
        if (valueLit(lit) == Boolean.lTrue) {
            // Dummy decision level:
            pushTrailMarker();
            return true;
        } else if (valueLit(lit) == Boolean.lFalse) {
            return false;
        }
        pushTrailMarker();
        // Unchecked enqueue
        assert valueLit(lit) == Boolean.lUndef;
        assignment_.set(var(lit), makeBoolean(sgn(lit)));
        trail_.add(lit);
        return propagate() == CR_Undef;
    }

    public void bound(Variable cpvar, ICause cause) throws ContradictionException {
        try {
            if (sat_trail_.get() < trailMarker()) {
                cancelUntil(sat_trail_.get());
                assert (sat_trail_.get() == trailMarker());
            }
            toCheck.addFirst(cpvar);
            while (toCheck.size() > 0) {
                Variable cvar = toCheck.pollFirst();
                List<Literalizer> myLits = vars.get(cvar);
                for (int i  = 0; i < myLits.size(); i++) {
                    Literalizer ltz = myLits.get(i);
                    if (ltz.canReact()) {
                        int lit = ltz.toLit();
                        if (propagateOneLiteral(lit)) {
                            sat_trail_.set(trailMarker());
                            for (int j = 0; j < touched_variables_.size(); ++j) {
                                lit = touched_variables_.get(j);
                                Literalizer lzr = lits.get(var(lit));
                                if (lzr != null && lzr.toEvent(lit, cause)) {
                                    toCheck.addFirst(lzr.cvar());
                                }// else case only for addSumBoolArrayLessEqKVar extra variable
                            }
                        } else {
                            ltz.toEvent(neg(lit), cause);
                        }
                    }
                }
            }
        } finally {
            touched_variables_.resetQuick(); // issue#327
            toCheck.clear();
        }
    }

    public void storeEarlyDeductions() {
        for (int i = 0; i < touched_variables_.size(); ++i) {
            int lit = touched_variables_.get(i);
            early_deductions_.add(lit);
        }
        touched_variables_.resetQuick();
    }

    public void applyEarlyDeductions(ICause cause) throws ContradictionException {
        for (int i = 0; i < early_deductions_.size(); ++i) {
            int lit = early_deductions_.get(i);
            lits.get(var(lit)).toEvent(lit, cause);
        }
    }

    public void cancelUntil(int level) {
        super.cancelUntil(level);
    }

    /**
     * Bind a boolean variable {@code bvar}, from CP side, to a variable from SAT side.
     * It creates the SAT variable and {@link Literalizer.BoolLit} that connect both world.
     *
     * @param bvar a boolean variable
     * @return the SAT variable (an int)
     */
    public <V extends Variable> int bind(V bvar, Literalizer ltz, Consumer<V> actionOnNew) {
        List<Literalizer> tmp = vars.computeIfAbsent(bvar, k -> new ArrayList<>());
        if (tmp.isEmpty()) {
            actionOnNew.accept(bvar);
        }
        Optional<Literalizer> opt = tmp.stream().filter(l -> l.equals(ltz)).findFirst();
        if (!opt.isPresent()) {
            int var = newVariable();
            ltz.svar(var);
            lits.put(var, ltz);
            tmp.add(ltz);
            opt = Optional.of(ltz);
        }
        return opt.get().svar();
    }


    public void synchro() {
        if (sat_trail_.get() < trailMarker()) {
            cancelUntil(sat_trail_.get());
            assert (sat_trail_.get() == trailMarker());
        }
    }

    /**
     * Checks if all clauses from <code>clauses</code> are satisfied
     *
     * @param clauses list of clause
     * @return <tt>true</tt> if all clauses are satisfied, <tt>false</tt> otherwise
     */
    public boolean clauseEntailed(ArrayList<Clause> clauses) {
        int lit;
        cl:
        for (Clause c : clauses) {
            for (int i = 0; i < c.size(); i++) {
                lit = c._g(i);
                Literalizer ltz = lits.get(var(lit));
                // ltz is null only for 'addClausesSumBoolArrayLessEqKVar' that needs an extra var.
                if (ltz == null || lits.get(var(lit)).check(sgn(lit))) {
                    continue cl;
                }
            }
            return false;
        }
        return true;
    }
}
