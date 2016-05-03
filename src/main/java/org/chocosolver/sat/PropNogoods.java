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
package org.chocosolver.sat;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TLongIntHashMap;
import org.chocosolver.memory.IStateInt;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.explanations.RuleStore;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.util.ESat;

import java.util.*;

import static org.chocosolver.sat.SatSolver.*;


/**
 * A propagator to store and propagate no-goods.
 *
 * Created by cprudhom on 20/01/15.
 * Project: choco.
 * @author Charles Prud'homme
 */
public class PropNogoods extends Propagator<IntVar> {

    /**
     * No entry value for {@link #lit2val}, {@link #lit2pos} and {@link #var2pos}.
     */
    private static final int NO_ENTRY = Integer.MAX_VALUE;

    /**
     * Mask to signed value.
     * The 32^th bit is set to 0 for "= value" and to 1 for "<= value".
     */
    private static final long BITOP = 1L << 32L;
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
    private Deque<IntVar> fp;

    /**
     * Local-like parameter, for #why() method only, lazily initialized.
     */
    private TIntObjectHashMap<ArrayList<SatSolver.Clause>> inClauses;

    /**
     * Store new added variables when {@link #initialized} is <i>false</i>
     */
    private ArrayList<IntVar> add_var;

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
        super(new BoolVar[]{model.ONE()}, PropagatorPriority.VERY_SLOW, true);
        this.vars = new IntVar[0];// erase model.ONE from the variable scope

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
                IntVar ivar = vars[lit2pos[var]];
                value = lit2val[var];
                eq = iseq(value);
                val = ivalue(value);
                if ((eq && sign != ivar.contains(val))
                        || (!eq && sign != ivar.getUB() <= val)) {
                    OK &= impliesEntailed(sat_.implies_.get(k));
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
        IntVar ivar;
        for (int l : lits.toArray()) {
            sign = sign(l);
            var = var(l);
            ivar = vars[lit2pos[var]];
            value = lit2val[var];
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
        }
        return true;
    }

    private boolean clauseEntailed(ArrayList<SatSolver.Clause> clauses) {
        int lit, var;
        long value;
        boolean sign;
        IntVar ivar;
        for (SatSolver.Clause c : clauses) {
            int cnt = 0;
            for (int i = 0; i < c.size(); i++) {
                lit = c._g(i);
                sign = sign(lit);
                var = var(lit);
                ivar = vars[lit2pos[var]];
                value = lit2val[var];
                if (iseq(value)) {
                    if (sign != ivar.contains(ivalue(value))) {
                        cnt++;
                    } else break;
                } else {
                    if (sign && ivar.getUB() <= ivalue(value)) {
                        cnt++;
                    } else if (!sign && ivar.getLB() > ivalue(value)) {
                        cnt++;
                    } else break;
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
    private static boolean iseq(long v) {
        return (v & BITOP) == 0;
    }

    /**
     * @param v a value
     * @return <code>v</code> with `&le;' information encoded into it
     */
    protected static long leq(int v) {
        return (v ^ BITOP);
    }

    /**
     * @param v a value
     * @return the value without the '=' or '&le;' information.
     */
    protected static int ivalue(long v) {
        return (int) (v & ~ BITOP);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Initializes this propagator
     */
    public void initialize(){
        if (initialized) {
            throw new SolverException("Nogoods store already initialized");
        }
        if(add_var.size()>0) {
            addVariable(add_var.toArray(new IntVar[add_var.size()]));
        }
        add_var.clear();
        this.initialized = true;
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
            if(initialized) {
                addVariable(ivar);
            }else {
                add_var.add(ivar);
            }
            pos = vars.length - 1;
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
     * var points a clause variable whom value is now to be val.
     *
     * @param index a clause var
     * @param sign  the sign of the lit
     * @throws ContradictionException if inconsistency is detected
     */
    protected void VariableBound(int index, boolean sign) throws ContradictionException {
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
    protected void doReduce(int lit) throws ContradictionException {
        int var = var(lit);
        long value = lit2val[var];
        IntVar ivar = vars[lit2pos[var]];
        if (iseq(value)) {
            if (sign(lit)) {
                ivar.instantiateTo(ivalue(value), this);
            } else {
                if (ivar.removeValue(ivalue(value), this)) {
                    fp.push(ivar);
                }
            }
        } else {
            if (sign(lit)) {
                if (ivar.updateUpperBound(ivalue(value), this)) {
                    fp.push(ivar);
                }
            } else if (ivar.updateLowerBound(ivalue(value) + 1, this)) {
                fp.push(ivar);
            }
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
     * @param var an integer variable
     * @return if clauses have been successfully added to the store.
     */
    public boolean declareDomainNogood(IntVar var) {
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
        this.getModel().getSolver().getEngine().propagateOnBacktrack(this); // issue#327
        // compare the current clauses with the previous stored one,
        // just in case the current one dominates the previous none
        if (sat_.nLearnt() > 1) {
            SatSolver.Clause last = sat_.learnts.get(sat_.learnts.size() - 1);
            test_eq.clear();
            for (int i = last.size() - 1; i >= 0; i--) {
                test_eq.set(last._g(i));
            }
            for (int c = sat_.learnts.size() - 2; c >= 0; c--) {
                int s = test_eq.cardinality();
                SatSolver.Clause prev = sat_.learnts.get(c);
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
    protected void applyEarlyDeductions() throws ContradictionException {
        for (int i = 0; i < early_deductions_.size(); ++i) {
            int lit = early_deductions_.get(i);
            doReduce(lit);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public boolean why(RuleStore ruleStore, IntVar ivar, IEventType evt, int ivalue) {
        if (inClauses == null) {
            fillInClauses();
        }
        boolean newrules = ruleStore.addPropagatorActivationRule(this);
        // When we got here, there are multiple cases:
        // 1. the propagator fails, at least one clause or implication cannot be satisfied
        // 2. the propagator is the cause of an instantiation
        // but the clauses and implications may be lost (cf. propagate)

        // get the index of the variable in the sat solver
        int var = vv2lit[ivar.getId()].get(ivalue);
        boolean new_value = true;
        if (!ivar.contains(ivalue)) {
            new_value = false;
        }
        int lit = makeLiteral(var, new_value);
        int neg = negated(lit);
        // A. implications:
        // simply iterate over implies_ and add the instantiated variables
        TIntList implies = sat_.implies_.get(lit);
        if (implies != null) {
            for (int i = implies.size() - 1; i >= 0; i--) {
                int l = implies.get(i);
                newrules |= _why(l, ruleStore);
            }
        }
        implies = sat_.implies_.get(neg);
        if (implies != null) {
            for (int i = implies.size() - 1; i >= 0; i--) {
                int l = implies.get(i);
                newrules |= _why(l, ruleStore);
            }
        }
        // B. clauses:
        // We need to find the fully instantiated clauses where bvar appears
        ArrayList<SatSolver.Clause> mClauses = inClauses.get(lit);
        if (mClauses != null) {
            for (int i = mClauses.size() - 1; i >= 0; i--) {
                newrules |= _why(mClauses.get(i), ruleStore);
            }
        }
        mClauses = inClauses.get(neg);
        if (mClauses != null) {
            for (int i = mClauses.size() - 1; i >= 0; i--) {
                newrules |= _why(mClauses.get(i), ruleStore);
            }
        }
        // C. learnt clauses:
        // We need to find the fully instantiated clauses where bvar appears
        // we cannot rely on watches_ because is not backtrackable
        // So, we iterate over clauses where the two first literal are valued AND which contains bvar
        for (int k = sat_.nLearnt() - 1; k >= 0; k--) {
            newrules |= _why(neg, lit, sat_.learnts.get(k), ruleStore);
        }
        return newrules;
    }

    private void fillInClauses() {
        inClauses = new TIntObjectHashMap<>();
        for (int k = sat_.nClauses() - 1; k >= 0; k--) {
            SatSolver.Clause cl = sat_.clauses.get(k);
            for (int d = cl.size() - 1; d >= 0; d--) {
                int l = cl._g(d);
                ArrayList<SatSolver.Clause> mcls = inClauses.get(l);
                if (mcls == null) {
                    mcls = new ArrayList<>();
                    inClauses.put(l, mcls);
                }
                mcls.add(cl);
            }
        }
    }

    private boolean _why(SatSolver.Clause cl, RuleStore ruleStore) {
        boolean newrules = false;
        // if the watched literals are instantiated
        if (litIsKnown(cl._g(0)) && litIsKnown(cl._g(1))) {
            for (int d = cl.size() - 1; d >= 0; d--) {
                newrules |= _why(cl._g(d), ruleStore);
            }
        }
        return newrules;
    }

    private boolean _why(int neg, int lit, SatSolver.Clause cl, RuleStore ruleStore) {
        boolean newrules = false;
        // if the variable watches
        if (cl._g(0) == neg || cl._g(0) == lit || cl._g(1) == neg || cl._g(1) == lit) {
            for (int d = cl.size() - 1; d >= 0; d--) {
                newrules |= _why(cl._g(d), ruleStore);
            }
        } else
            // if the watched literals are instantiated
            if (litIsKnown(cl._g(0)) && litIsKnown(cl._g(1))) {
                // then, look for the lit
                int p = cl.pos(neg);
                int q = cl.pos(lit);
                if (p > -1 || q > -1) { // we found a clause where neg is in
                    for (int d = cl.size() - 1; d >= 0; d--) {
                        newrules |= _why(cl._g(d), ruleStore);
                    }
                }
            }
        return newrules;
    }

    private boolean _why(int l, RuleStore ruleStore) {
        int _var = var(l);
        IntVar avar = vars[lit2pos[_var]];
        long aval = lit2val[_var];
        if (iseq(aval)) {
            if (avar.contains(ivalue(aval))) {
                if (avar.isInstantiated()) {
                    return ruleStore.addFullDomainRule(avar);
                }
            } else {
                return ruleStore.addRemovalRule(avar, ivalue(aval));
            }
        } else {
            if (avar.getLB() > ivalue(aval)) {
                return ruleStore.addLowerBoundRule(avar);
            } else if (avar.getUB() <= ivalue(aval)) {
                return ruleStore.addUpperBoundRule(avar);
            }
        }
        return false;
    }

    private boolean litIsKnown(int l) {
        int _var = var(l);
        IntVar avar = vars[lit2pos[_var]];
        long aval = lit2val[_var];
        if (iseq(aval)) {
            return !avar.contains(ivalue(aval)) || avar.isInstantiated();
        } else {
            return avar.getLB() > ivalue(aval) || avar.getUB() <= ivalue(aval);
        }
    }
}
