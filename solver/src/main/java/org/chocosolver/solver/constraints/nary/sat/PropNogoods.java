/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.sat;

import static org.chocosolver.sat.SatSolver.makeLiteral;
import static org.chocosolver.sat.SatSolver.negated;
import static org.chocosolver.sat.SatSolver.sign;
import static org.chocosolver.sat.SatSolver.var;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TLongIntHashMap;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Deque;
import org.chocosolver.memory.IStateInt;
import org.chocosolver.sat.SatSolver;
import org.chocosolver.sat.SatSolver.Clause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.util.ESat;
import org.chocosolver.util.tools.VariableUtils;


/**
 * A propagator to store and propagate no-goods.
 * <p>
 * Created by cprudhom on 20/01/15.
 * Project: choco.
 *
 * @author Charles Prud'homme
 */
public class PropNogoods extends Propagator<Variable> {

    /**
     * No entry value for {@link #lit2val}, {@link #lit2pos} and {@link #var2pos}.
     */
    private static final int NO_ENTRY = Integer.MAX_VALUE;

    /**
     * Mask to signed value.
     * The 33^th bit is set to 0 for "= value" and to 1 for "<= value".
     */
    private static final long BITOP = 1L << 33L;
    /**
     * Frontier between "= value" (below) and "<= value" (above)
     */
    private static final long FRONTIER = BITOP - Integer.MAX_VALUE;
    /**
     * The underlying SAT solver
     */
    private SatSolver sat_;

    /**
     * Binds couple (variable-value) to a unique literal
     */
    private TLongIntHashMap[] vv2lit;

    /**
     * Binds variable ({@link Variable#getId()} to a unique position
     */
    private int[] var2pos;

    /**
     * Binds literal to variable
     */
    private int[] lit2pos;
    /**
     * Binds literal to value.
     * A value is typically a signed int.
     * The 32^th bit is set to 0 for "= value" and to 1 for "<= value".
     */
    private long[] lit2val;

    /**
     * For comparison with SAT solver trail, to deal properly with backtrack
     */
    private IStateInt sat_trail_;

    /**
     * List of early deduction literals
     */
    private TIntList early_deductions_;

    /**
     * Local-like parameter.
     * To reduce learnt no-goods.
     */
    private BitSet test_eq;

    /**
     * Since there is no domain-clause, a fix point may not be reached by SatSolver itself.
     * Stores all modified variable to make sure a fix point is reached.
     */
    private Deque<Variable> fp;

    /**
     * Local-like parameter, for #why() method only, lazily initialized.
     */
    private TIntObjectHashMap<ArrayList<Clause>> inClauses;

    /**
     * Store new added variables when {@link #initialized} is <i>false</i>
     */
    private ArrayList<Variable> add_var;

    /**
     * Indicates if this is initialized or not
     */
    private boolean initialized = false;

    /**
     * Create a (unique) propagator for no-goods recording and propagation.
     *
     * @param model the model that declares the propagator
     */
    public PropNogoods(Model model) {
        super(new Variable[]{model.getVar(0)}, PropagatorPriority.VERY_SLOW, true);
        this.vars = new Variable[0];// erase model.ONE from the variable scope

        int k = 16;
        this.vv2lit = new TLongIntHashMap[k];//new TIntObjectHashMap<>(16, .5f, NO_ENTRY);
        this.lit2val = new long[k];//new TIntIntHashMap(16, .5f, NO_ENTRY, NO_ENTRY);
        Arrays.fill(lit2val, NO_ENTRY);
        this.lit2pos = new int[k];//new TIntIntHashMap(16, .5f, NO_ENTRY, NO_ENTRY);
        Arrays.fill(lit2pos, NO_ENTRY);
        this.var2pos = new int[k];//new TIntIntHashMap(16, .5f, NO_ENTRY, NO_ENTRY);
        Arrays.fill(var2pos, NO_ENTRY);
        //TODO: one satsolver per model...
        sat_ = new SatSolver();
        early_deductions_ = new TIntArrayList();
        sat_trail_ = model.getEnvironment().makeInt();
        test_eq = new BitSet();
        fp = new ArrayDeque<>();
        add_var = new ArrayList<>(16);
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        initialize();
        if (!sat_.ok_) fails();
        fp.clear();
        sat_.cancelUntil(0); // to deal with learnt clauses, only called on coarse grain propagation
        storeEarlyDeductions();
        applyEarlyDeductions();
        for (int i = 0; i < vars.length; ++i) {
            doVariableBound(vars[i]);
        }
        while (fp.size() > 0) {
            doVariableBound(fp.pollFirst());
        }
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        fp.clear();
        doVariableBound(vars[idxVarInProp]);
        while (fp.size() > 0) {
            doVariableBound(fp.pollFirst());
        }
    }

    private void doVariableBound(Variable var) throws ContradictionException {
        if (VariableUtils.isInt(var)) {
            doVariableBound((IntVar) var);
        } else if (VariableUtils.isSet(var)) {
            doVariableBound((SetVar) var);
        } else {
            throw new UnsupportedOperationException("Unknown case");
        }
    }

    private void doVariableBound(IntVar var) throws ContradictionException {
        TLongIntHashMap map;
        for (long k : (map = vv2lit[var.getId()]).keys()) {
            int value = ivalue(k);
            if (iseq(k)) {
                if (var.contains(value)) {
                    if (var.isInstantiated()) {
                        VariableBound(map.get(k), true);
                    }
                } else {
                    VariableBound(map.get(k), false);
                }
            } else {
                if (var.getUB() <= value) {
                    VariableBound(map.get(k), true);
                } else if (var.getLB() > value) {
                    VariableBound(map.get(k), false);
                }
            }
        }
    }

    private void doVariableBound(SetVar var) throws ContradictionException {
        TLongIntHashMap map;
        for (long k : (map = vv2lit[var.getId()]).keys()) {
            int value = ivalue(k);
            if (iseq(k)) {
                if (var.getLB().contains(value)) {
                    VariableBound(map.get(k), true);
                } else if (!var.getUB().contains(value)) {
                    VariableBound(map.get(k), false);
                }
            } else {
                throw new UnsupportedOperationException("SetVar does not support that case");
            }
        }
    }

    @Override
    public ESat isEntailed() {
        if (vars.length == 0) return ESat.TRUE;
        if (isCompletelyInstantiated()) {
            boolean OK = true;
            int var, val;
            long value;
            boolean sign, eq;
            for (int k : sat_.implies_.keys()) {
                sign = sign(negated(k));
                var = var(k);
                Variable avar = vars[lit2pos[var]];
                value = lit2val[var];
                eq = iseq(value);
                val = ivalue(value);
                if (VariableUtils.isInt(avar)) {
                    IntVar ivar = (IntVar) avar;
                    if ((eq && sign != ivar.contains(val))
                            || (!eq && sign != ivar.getUB() <= val)) {
                        OK &= impliesEntailed(sat_.implies_.get(k));
                    }
                } else if (VariableUtils.isSet(avar)) {
                    SetVar svar = (SetVar) avar;
                    if (eq && sign != svar.getLB().contains(val)) {
                        OK &= impliesEntailed(sat_.implies_.get(k));
                    }
                } else {
                    throw new UnsupportedOperationException("Unknown case");
                }
            }
            OK &= clauseEntailed(sat_.clauses);
            OK &= clauseEntailed(sat_.learnts);
            return ESat.eval(OK);
        }
        return ESat.UNDEFINED;
    }

    private boolean impliesEntailed(TIntList lits) {
        int var;
        long value;
        boolean sign;
        Variable avar;
        for (int l : lits.toArray()) {
            sign = sign(l);
            var = var(l);
            avar = vars[lit2pos[var]];
            value = lit2val[var];
            if (VariableUtils.isInt(avar)) {
                IntVar ivar = (IntVar) avar;
                if (iseq(value)) {
                    if (sign != ivar.contains(ivalue(value))) {
                        return false;
                    }
                } else {
                    if (sign && ivar.getLB() > ivalue(value)) {
                        return false;
                    } else if (!sign && ivar.getUB() <= ivalue(value)) {
                        return false;
                    }
                }
            } else if (VariableUtils.isSet(avar)) {
                SetVar svar = (SetVar) avar;
                if (iseq(value)) {
                    if (sign != svar.getLB().contains(ivalue(value))) {
                        return false;
                    }
                } else {
                    throw new UnsupportedOperationException("SetVar does not support that case");
                }
            } else {
                throw new UnsupportedOperationException("Unknown case");
            }
        }
        return true;
    }

    private boolean clauseEntailed(ArrayList<Clause> clauses) {
        int lit, var;
        long value;
        boolean sign;
        Variable avar;
        for (Clause c : clauses) {
            int cnt = 0;
            for (int i = 0; i < c.size(); i++) {
                lit = c._g(i);
                sign = sign(lit);
                var = var(lit);
                avar = vars[lit2pos[var]];
                value = lit2val[var];
                if (VariableUtils.isInt(avar)) {
                    IntVar ivar = (IntVar) avar;
                    if (iseq(value)) {
                        if (sign != ivar.contains(ivalue(value))) {
                            cnt++;
                        } else break;
                    } else {
                        if (sign && ivar.getLB() > ivalue(value)) {
                            cnt++;
                        } else if (!sign && ivar.getUB() <= ivalue(value)) {
                            cnt++;
                        } else break;
                    }
                } else if (VariableUtils.isSet(avar)) {
                    SetVar svar = (SetVar) avar;
                    if (iseq(value)) {
                        if (sign != svar.getLB().contains(ivalue(value))) {
                            cnt++;
                        } else break;
                    } else {
                        throw new UnsupportedOperationException("SetVar does not support that case");
                    }
                } else {
                    throw new UnsupportedOperationException("Unknown case");
                }
            }
            if (cnt == c.size()) return false;
        }
        return true;
    }


    /**
     * @param v a value
     * @return <tt>true</tt> if the value encodes '=', <tt>false</tt> if it encodes '&le;'.
     */
    static boolean iseq(long v) {
        return v < FRONTIER;
    }

    /**
     * @param v a value
     * @return <code>v</code> with `&le;' information encoded into it
     */
    static long leq(int v) {
        return v + BITOP;
    }

    /**
     * @param v a value
     * @return the value without the '=' or '&le;' information.
     */
    static int ivalue(long v) {
        return (int) (iseq(v) ? v : v - BITOP);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Initializes this propagator
     */
    public void initialize() {
        if (!initialized) {
            if (add_var.size() > 0) {
                addVariable(add_var.toArray(new IntVar[0]));
            }
            add_var.clear();
            this.initialized = true;
        }
    }

    /**
     * Creates or returns if already existing, the literal corresponding to :
     * <p>
     * <code>ivar</code> (<code>eq</code>?"=":"<=") <code>value</code>
     * <p>
     * where "=" is selected if <code>eq</code> is <tt>true</tt>, "<=" otherwise.
     * <p>
     * The negation of the literal is managed outside.
     *
     * @param ivar  an integer variable
     * @param value a value
     * @param eq    set to <tt>true</tt> to select "=", to <tt>false</tt> to select "<=".
     * @return the literal corresponding to <code>ivar</code> (<code>eq</code>?"=":"<=") <code>value</code>
     */
    public int Literal(IntVar ivar, int value, boolean eq) {
        // TODO: deal with BoolVar
        int vid = ivar.getId();
        int var;
        TLongIntHashMap map;
        if (vid >= vv2lit.length) {
            TLongIntHashMap[] tmp = vv2lit;
            vv2lit = new TLongIntHashMap[vid + 1];
            System.arraycopy(tmp, 0, vv2lit, 0, tmp.length);

            int[] tmpi = var2pos;
            var2pos = new int[vid + 1];
            System.arraycopy(tmpi, 0, var2pos, 0, tmpi.length);
            Arrays.fill(var2pos, tmpi.length, vid + 1, NO_ENTRY);
        }
        if ((map = vv2lit[vid]) == null) {
            map = new TLongIntHashMap(16, .5f, NO_ENTRY, NO_ENTRY);
            vv2lit[vid] = map;
        }

        int pos;
        if ((pos = var2pos[vid]) == NO_ENTRY) {
            if (initialized) {
                addVariable(ivar);
                pos = vars.length - 1;
            } else {
                add_var.add(ivar);
                pos = add_var.size() - 1;
            }
            var2pos[vid] = pos;
        }
        long lvalue = eq ? value : leq(value);
        if ((var = map.get(lvalue)) == NO_ENTRY) {
            var = sat_.newVariable();
            map.put(lvalue, var);
            if (var >= lit2pos.length) {
                int[] itmp = lit2pos;
                lit2pos = new int[var + 1];
                System.arraycopy(itmp, 0, lit2pos, 0, itmp.length);
                Arrays.fill(lit2pos, itmp.length, var + 1, NO_ENTRY);

                long[] ltmp = lit2val;
                lit2val = new long[var + 1];
                System.arraycopy(ltmp, 0, lit2val, 0, ltmp.length);
                Arrays.fill(lit2val, ltmp.length, var + 1, NO_ENTRY);
            }


            lit2pos[var] = pos;
            lit2val[var] = lvalue;
        }
        return makeLiteral(var, true);
    }

    /**
     * Creates or returns if already existing, the literal corresponding to :
     * <p>
     * <code>value</code> (<code>in</code>?"&isin;":"&notin;") <code>svar</code>
     * <p>
     * where "in" is selected if <code>in</code> is <tt>true</tt>, "not in" otherwise.
     * <p>
     * The negation of the literal is managed outside.
     *
     * @param svar  an integer variable
     * @param value a value
     * @param in    set to <tt>true</tt> to select "&isin;", to <tt>false</tt> to select "&notin;".
     * @return the literal corresponding to <code>value</code> (<code>in</code>?"&isin;":"&notin;") <code>svar</code>
     */
    public int Literal(SetVar svar, int value, boolean in) {
        // TODO: deal with BoolVar
        int vid = svar.getId();
        int var;
        TLongIntHashMap map;
        if (vid >= vv2lit.length) {
            TLongIntHashMap[] tmp = vv2lit;
            vv2lit = new TLongIntHashMap[vid + 1];
            System.arraycopy(tmp, 0, vv2lit, 0, tmp.length);

            int[] tmpi = var2pos;
            var2pos = new int[vid + 1];
            System.arraycopy(tmpi, 0, var2pos, 0, tmpi.length);
            Arrays.fill(var2pos, tmpi.length, vid + 1, NO_ENTRY);
        }
        if ((map = vv2lit[vid]) == null) {
            map = new TLongIntHashMap(16, .5f, NO_ENTRY, NO_ENTRY);
            vv2lit[vid] = map;
        }

        int pos;
        if ((pos = var2pos[vid]) == NO_ENTRY) {
            if (initialized) {
                addVariable(svar);
                pos = vars.length - 1;
            } else {
                add_var.add(svar);
                pos = add_var.size() - 1;
            }
            var2pos[vid] = pos;
        }
        long lvalue = in ? value : leq(value);
        if ((var = map.get(lvalue)) == NO_ENTRY) {
            var = sat_.newVariable();
            map.put(lvalue, var);
            if (var >= lit2pos.length) {
                int[] itmp = lit2pos;
                lit2pos = new int[var + 1];
                System.arraycopy(itmp, 0, lit2pos, 0, itmp.length);
                Arrays.fill(lit2pos, itmp.length, var + 1, NO_ENTRY);

                long[] ltmp = lit2val;
                lit2val = new long[var + 1];
                System.arraycopy(ltmp, 0, lit2val, 0, ltmp.length);
                Arrays.fill(lit2val, ltmp.length, var + 1, NO_ENTRY);
            }


            lit2pos[var] = pos;
            lit2val[var] = lvalue;
        }
        return makeLiteral(var, true);
    }

    /**
     * var points a clause variable whom value is now to be val.
     *
     * @param index a clause var
     * @param sign  the sign of the lit
     * @throws ContradictionException if inconsistency is detected
     */
    void VariableBound(int index, boolean sign) throws ContradictionException {
        try {
            if (sat_trail_.get() < sat_.trailMarker()) {
                sat_.cancelUntil(sat_trail_.get());
                assert (sat_trail_.get() == sat_.trailMarker());
            }
            int lit = makeLiteral(index, sign);
            if (!sat_.propagateOneLiteral(lit)) {
                // force failure by removing the last value: flip the sign
                // explanations require doing the failure
                doReduce(negated(lit));
            } else {
                sat_trail_.set(sat_.trailMarker());
                for (int i = 0; i < sat_.touched_variables_.size(); ++i) {
                    lit = sat_.touched_variables_.get(i);
                    doReduce(lit);
                }
            }
        } finally {
            sat_.touched_variables_.resetQuick(); // issue#327
        }
    }

    /**
     * Reduce the variable.
     *
     * @param lit literal to assign
     * @throws ContradictionException if reduction leads to failure
     */
    void doReduce(int lit) throws ContradictionException {
        int var = var(lit);
        long value = lit2val[var];
        Variable avar = vars[lit2pos[var]];
        if (VariableUtils.isInt(avar)) {
            doReduce((IntVar) avar, sign(lit), value);
        } else if (VariableUtils.isSet(avar)) {
            doReduce((SetVar) avar, sign(lit), value);
        }
    }

    /**
     * Reduce the variable.
     *
     * @param sign literal to assign
     * @throws ContradictionException if reduction leads to failure
     */
    private void doReduce(IntVar ivar, boolean sign, long value) throws ContradictionException {
        if (iseq(value)) {
            if (sign) {
                ivar.instantiateTo(ivalue(value), this);
            } else {
                if (ivar.removeValue(ivalue(value), this)) {
                    fp.push(ivar);
                }
            }
        } else {
            if (sign) {
                if (ivar.updateUpperBound(ivalue(value), this)) {
                    fp.push(ivar);
                }
            } else if (ivar.updateLowerBound(ivalue(value) + 1, this)) {
                fp.push(ivar);
            }
        }
    }

    /**
     * Reduce the variable.
     *
     * @param sign literal to assign
     * @throws ContradictionException if reduction leads to failure
     */
    private void doReduce(SetVar svar, boolean sign, long value) throws ContradictionException {
        if (iseq(value)) {
            if (sign) {
                svar.force(ivalue(value), this);
            } else {
                if (svar.remove(ivalue(value), this)) {
                    fp.push(svar);
                }
            }
        } else {
            throw new UnsupportedOperationException("SetVar does not support that case");
        }
    }

    /**
     * Add clauses to ensure domain consistency, that is:
     * <ol>
     *     <li>
     *          [ x &le; d ] &rArr; [ x &le; d +1 ]
     *     </li>
     *     <li>
     *          [ x = d ] &hArr; ([ x &le; d ] &and; &not;[ x &le; d + 1])
     *     </li>
     * </ol>
     *
     * @param var an integer variable
     * @return if clauses have been successfully added to the store.
     */
    boolean declareDomainNogood(IntVar var) {
        int size = var.getDomainSize();
        int[] lits = new int[size * 2];
        // 1. generate lits
        int a = var.getLB();
        int ub = var.getUB();
        int i = 0;
        while (a <= ub) {
            lits[i] = Literal(var, a, true);
            lits[i + size] = Literal(var, a, false);
            i++;
            a = var.nextValue(a);
        }
        TIntList clauses = new TIntArrayList();
        boolean add = false;
        // 2. add clauses
        // 2a.  [ x <= d ] => [ x <= d +1 ]
        for (int j = size; j < 2 * size - 1; j++) {
            clauses.add(negated(lits[j]));
            clauses.add(lits[j + 1]);
            add |= sat_.addClause(clauses);
            clauses.clear();
        }
        // 2b.  [ x = d ] <=> [ x <= d ] and not[ x <= d +1 ]
        for (int k = 0; k < size - 1; k++) {
            // [ x = d ] or not[ x <= d ] or [ x <= d +1 ]
            clauses.add(lits[k]);
            clauses.add(negated(lits[size + k]));
            clauses.add(lits[size + k + 1]);
            add |= sat_.addClause(clauses);
            clauses.clear();
            // not [ x = d ] or [ x <= d ]
            clauses.add(negated(lits[k]));
            clauses.add(lits[size + k]);
            add |= sat_.addClause(clauses);
            clauses.clear();
            // not [ x = d ] or not[ x <= d +1 ]
            clauses.add(negated(lits[k]));
            clauses.add(negated(lits[size + k + 1]));
            add |= sat_.addClause(clauses);
            clauses.clear();
        }
        storeEarlyDeductions();
        return add;
    }

    /**
     * Add unit clause to no-goods store
     *
     * @param p unit clause
     * @return <tt>false</tt> if failure is detected
     */
    @SuppressWarnings("unused")
    public boolean addNogood(int p) {
        boolean result = sat_.addClause(p);
        storeEarlyDeductions();
        return result;
    }

    /**
     * Add unit clause to no-goods store
     *
     * @param lits clause
     * @return <tt>false</tt> if failure is detected
     */
    @SuppressWarnings("unused")
    public boolean addNogood(TIntList lits) {
        boolean result = sat_.addClause(lits);
        storeEarlyDeductions();
        return result;
    }

    /**
     * Add learnt clause to no-goods store
     *
     * @param lits clause
     */
    public void addLearnt(int... lits) {
        sat_.learnClause(lits);
        // early deductions of learnt clause may lead to incorrect behavior on backtrack
        // since early deduction is not backtrackable.
        forcePropagationOnBacktrack(); // issue#327
        // compare the current clauses with the previous stored one,
        // just in case the current one dominates the previous none
        if (sat_.nLearnt() > 1) {
            Clause last = sat_.learnts.get(sat_.learnts.size() - 1);
            test_eq.clear();
            for (int i = last.size() - 1; i >= 0; i--) {
                test_eq.set(last._g(i));
            }
            for (int c = sat_.learnts.size() - 2; c >= 0; c--) {
                int s = test_eq.cardinality();
                Clause prev = sat_.learnts.get(c);
                if (last.size() > 1 && last.size() < prev.size()) {
                    for (int i = prev.size() - 1; i >= 0; i--) {
                        s -= test_eq.get(prev._g(i)) ? 1 : 0;
                    }
                    if (s == 0) { // then last dominates prev
                        sat_.detachLearnt(c);
                    }
                }
            }
        }
    }

    private void storeEarlyDeductions() {
        for (int i = 0; i < sat_.touched_variables_.size(); ++i) {
            int lit = sat_.touched_variables_.get(i);
            early_deductions_.add(lit);
        }
        sat_.touched_variables_.resetQuick();
    }

    /**
     * Apply early deduction
     *
     * @throws ContradictionException if it fails
     */
    private void applyEarlyDeductions() throws ContradictionException {
        for (int i = 0; i < early_deductions_.size(); ++i) {
            int lit = early_deductions_.get(i);
            doReduce(lit);
        }
    }
}
