/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.clauses;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.learn.ExplanationForSignedClause;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableRangeSet;
import org.chocosolver.util.tools.ArrayUtils;

import static org.chocosolver.util.ESat.*;

/**
 * This propagator manages a signed clause: a disjunction of unary membership constraints.
 *
 * <p> Project:  choco-solver.
 *
 * @author Charles Prud'homme
 * @since 11/05/2018.
 */
@SuppressWarnings("Duplicates")
public class PropSignedClause extends Propagator<IntVar> {

    /**
     * Free mask
     */
    private static final byte F0 = 0b00;
    /**
     * Mask that indicates pos[0] as false
     */
    private static final byte F1 = 0b01;
    /**
     * Mask that indicates pos[1] as false
     */
    protected static final byte F2 = 0b10;
    /**
     * Store which pos, among 0 and 1, are false
     */
    private byte FL;
    /**
     * List of variables this propagators deal with.
     */
    private final IntVar[] mvars;

    private final int[] bounds;
    /**
     * Literals of the clauses. Use to always get at position 0 a free literal.
     */
    private final int[] pos;

    private final Solver mSolver;
    /**
     * Store label of last activity
     */
    public long label;

    private static PropagatorPriority computePriority(int nbvars) {
        if (nbvars == 2) {
            return PropagatorPriority.BINARY;
        } else if (nbvars == 3) {
            return PropagatorPriority.TERNARY;
        } else {
            return PropagatorPriority.LINEAR;
        }
    }

    /**
     * Create a {@link PropSignedClause} instance considering that 'ranges' are allowed:
     * <p/>
     * ( ... &or; vars[i] &isin; ranges[i] &or; ... )
     *
     * @param vars set of variables
     * @param ranges set of allowed ranges
     * @return a instance of {@link PropSignedClause}
     */
    public static PropSignedClause makeFromIn(IntVar[] vars, IntIterableRangeSet[] ranges){
        return new PropSignedClause(vars, ranges, true);
    }

    /**
     * Create a {@link PropSignedClause} instance considering that 'ranges' are forbidden:
     * <p/>
     * ( ... &or; vars[i] &notin; ranges[i] &or; ... )
     * @param vars set of variables
     * @param ranges set of allowed ranges
     * @return a instance of {@link PropSignedClause}
     */
    public static PropSignedClause makeFromOut(IntVar[] vars, IntIterableRangeSet[] ranges){
        return new PropSignedClause(vars, ranges, false);
    }

    private PropSignedClause(IntVar[] vars, IntIterableRangeSet[] ranges, boolean in) {
        super(new IntVar[]{vars[0], vars[1]}, computePriority(vars.length), false, true);
        assert in;
        // TODO: accurately select literals
        this.mSolver = vars[0].getModel().getSolver();
        int size = 0;
        for (int i = 0; i < ranges.length; i++) {
            size += ranges[i].getNbRanges();
        }
        this.pos = ArrayUtils.array(0, size - 1);
        this.mvars = new IntVar[size];
        this.bounds = new int[size << 1];
        for (int i = 0, k = -1; i < ranges.length; i++) {
            for (int r = 0; r < ranges[i].getNbRanges(); r++) {
                this.mvars[++k] = vars[i];
                this.bounds[k << 1] = ranges[i].minOfRange(r);
                this.bounds[(k << 1) + 1] = ranges[i].maxOfRange(r);
            }
        }
        if (ranges[0].getNbRanges() > 1) {
            // synchronize positions of var[0] and var[1]
            int nbr = ranges[0].getNbRanges();
            int p = this.pos[1];
            this.pos[1] = this.pos[nbr];
            this.pos[nbr] = p;
        }
//        System.out.println(this);
    }

    @Override
    public final int getPropagationConditions(int vIdx) {
        assert vIdx <= 1;
        assert vars[vIdx] == mvars[pos[vIdx]];
        return IntEventType.boundAndInst();//all();
    }

    public void forceActivation(){
        setActive0();
    }

    /**
     * @return the number of literals in this
     */
    public final int cardinality() {
        return mvars.length;
    }

    private ESat check(int p) {
        IntVar v = mvars[p];
        int lv = v.getLB();
        int uv = v.getUB();
        int l = bounds[p << 1];
        int u = bounds[(p << 1) + 1];
        if (l <= lv && uv <= u) { // v in [l,u]
            return ESat.TRUE;
        } else if (l > uv || lv > u || (v.hasEnumeratedDomain() && v.nextValue(l - 1) > u)) {  // v does not intersect [l,u]
            return ESat.FALSE;
        }
        return ESat.UNDEFINED;
    }

    private boolean restrict(int p) throws ContradictionException {
        return mvars[p].updateBounds(bounds[p << 1], bounds[(p << 1) + 1], this);
    }

    @SuppressWarnings("Duplicates")
    public final void propagate(int evtmask) throws ContradictionException {
        switch (check(pos[0])) {
            case TRUE:
                FL = F0;
                label = -this.mSolver.getDecisionPath().size();
                setPassive();
                return;
            case FALSE:
                FL |= F1;
                break;
            case UNDEFINED:
                break;
        }
        switch (check(pos[1])) {
            case TRUE:
                FL = F0;
                label = -this.mSolver.getDecisionPath().size();
                setPassive();
                return;
            case FALSE:
                FL |= F2;
                break;
            case UNDEFINED:
                break;
        }
        if (FL != F0) {
            propagateClause();
        }
    }

    /**
     * Condition: at least one lit is false and none is true among l0 and l1.
     */
    private void propagateClause() throws ContradictionException {
        int k = 2;
        int to = pos.length;
        do {
            int p;
            if ((FL & F2) != 0) {
                p = 1;
                FL ^= F2;
            } else {
                p = 0;
                FL ^= F1;
            }
            // assertion: p is false
            int l0 = pos[0];
            int l1 = pos[1];
            if (p == 0) {
                // Make sure the false literal is pos[1]:
                int t = l0;
                pos[0] = l0 = l1;
                pos[1] = l1 = t;
                swap();
            }
            // Look for new watch:
            boolean cont = false;
            for (; k < to; k++) {
                int l = pos[k];
                ESat b = check(l);
                if (b != FALSE) {
                    // update watcher -- preserve the operations order
                    if (vars[1] != mvars[l]) {
                        vars[1].unlink(this, 1);
                        setVIndices(1, mvars[l].link(this, 1));
                        vars[1] = mvars[l];
                    }
                    pos[1] = l;
                    pos[k] = pos[--to];
                    pos[to] = l1;
                    if (b == TRUE) {
                        label = -this.mSolver.getDecisionPath().size();
                        setPassive();
                        FL = F0;
                        assert this.isEntailed() == TRUE;
                        return;
                    }
                    cont = true;
                    break;
                }
            }
            // Did not find watch -- clause is unit under assignment:
            if (!cont) {
                FL = F0;
                if (restrict(l0)) {
                    assert this.isEntailed() == TRUE;
                    label = this.mSolver.getDecisionPath().size();
                    setPassive();
                    return;
                } else {
                    assert this.isEntailed() != FALSE;
                }
            }
        } while (FL != F0);
    }

    private void swap() {
        // update propagator internal structure
        // 0. get temp var
        IntVar v = this.vars[1];
        // 1. swap variables
        vars[1] = vars[0];
        vars[0] = v;
        int vi0 = getVIndice(0);
        assert vars[1].getIndexInPropagator(vi0) == 0;
        int vi1 = getVIndice(1);
        assert vars[0].getIndexInPropagator(vi1) == 1;
        // 2. swap pindices
        this.vars[0].setPIndice(vi1, 0);
        this.vars[1].setPIndice(vi0, 1);
        // 3. swap vindices
        setVIndices(0, vi1);
        setVIndices(1, vi0);
    }

    int getNbFalsified() {
        int count = 0;
        for (int i = 0; i < pos.length; i++) {
            ESat b = check(i);
            if (b == FALSE) {
                count++;
            }
        }
        return count;
    }


    int getNbSatisfied() {
        int count = 0;
        for (int i = 0; i < pos.length; i++) {
            ESat b = check(i);
            if (b == TRUE) {
                count++;
            }
        }
        return count;
    }

    /**
     * Test if one clause outshines another one or is incomparable with it.
     * A clause ci outshines a clause cj iff:
     * <ul>
     *     <li>var(ci) &sube; var(cj) and</li>
     *     <li>for each v in var(ci), rang(v, ci) &sube; rang(v, cj)</li>
     * </ul>
     * @implSpec vars in each clause is supposed to be sorted wrt the var ID.
     * Otherwise, this method can return incorrect results.
     * @param cj another clause
     * @return negative integer, zero, or a positive integer as ci outshines,
     * is not comparable with or is outshone by cj.
     */
    final int dominate(PropSignedClause cj) {
        if (this.mvars.length < cj.mvars.length) {
            return outhsine0(this, cj);
        } else if (this.mvars.length > cj.mvars.length) {
            return -outhsine0(cj, this);
        } else {
            return outhsine1(this, cj);
        }
    }

    /**
     * Considering |ci| < |cj|, test if ci outshines cj.
     * @implSpec variables, in each clause, are supposed to be sorted wrt to increasing ID.
     * @param ci a clause
     * @param cj another clause
     * @return 1 if ci outshines cj, 0 otherwise
     */
    private int outhsine0(PropSignedClause ci, PropSignedClause cj){
        return 0;
    }

    /**
     * Considering two clauses with same cardinality, check which one outshines the other, if any.
     * @implSpec variables, in each clause, are supposed to be sorted wrt to increasing ID.
     * @param ci a clause
     * @param cj another clause
     * @return 1, 0 or -1 as ci outshines cj, ci and cj are incomparable or cj oushines ci.
     */
    private int outhsine1(PropSignedClause ci, PropSignedClause cj){
        return 0;
    }


    public final ESat isEntailed() {
        int i = 0;
        boolean u = false;
        while (i < pos.length) {
            ESat b = check(i);
            if (b == TRUE) {
                return TRUE;
            } else if (b == UNDEFINED) {
                u = true;
            }
            i++;
        }
        return u ? UNDEFINED : FALSE;
    }

    public void explain(int p, ExplanationForSignedClause explanation) {
        IntVar pivot = explanation.readVar(p);
        IntIterableRangeSet set;
        int i = 0;
        while (i < mvars.length) {
            IntVar v = mvars[i];
            if (explanation.getFront().getValueOrDefault(v, -1) == -1) { // see javadoc for motivation of these two lines
                explanation.getImplicationGraph().findPredecessor(explanation.getFront(), v, p);
            }
            set = explanation.empty();
            do {
                set.addBetween(bounds[i << 1], bounds[(i << 1) + 1]);
                i++;
            } while (i < mvars.length && mvars[i - 1] == mvars[i]);
            if(v == pivot){
                v.intersectLit(set, explanation);
            } else {
                v.unionLit(set, explanation);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder st = new StringBuilder();
        st.append('(').append(mvars[pos[0]]).append(" \u2208 [")
                .append(bounds[pos[0] << 1]).append(',').append(bounds[(pos[0] << 1) + 1]).append(']');
        st.append(':').append(check(pos[0]));
        for (int i = 1; i < pos.length; i++) {
            st.append(") \u2228 (");
            st.append(mvars[pos[i]]).append(" \u2208 [").append(bounds[pos[i] << 1])
                    .append(',').append(bounds[(pos[i] << 1) + 1]).append(']');
            st.append(':').append(check(pos[i]));
        }
        st.append(')');
        return st.toString();
    }

}
