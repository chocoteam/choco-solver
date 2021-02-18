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

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.learn.ExplanationForSignedClause;
import org.chocosolver.solver.learn.XParameters;
import org.chocosolver.solver.search.strategy.selectors.variables.ClausesBased;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.ShrinkableList;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableRangeSet;
import org.chocosolver.util.objects.tree.Interval;
import org.chocosolver.util.objects.tree.IntervalTree;
import org.chocosolver.util.tools.ArrayUtils;

import java.util.*;
import java.util.stream.Stream;

import static org.chocosolver.util.ESat.*;

/**
 * A class to manage life of sclauses during resolution. TODO
 *
 * <p> Project: choco-solver.
 *
 * @author Charles Prud'homme
 * @since 27/10/2016.
 */
public class ClauseStore extends Propagator<IntVar> {

    /**
     * Signed clause unique ID -- for toString() mainly
     */
    private static int SID = 1;
    /**
     * Solver that handles the clauses
     */
    private Solver mSolver;
    /**
     * List of current sclauses
     */
    private List<SignedClause> clauses;
    /**
     * List of current sclauses
     */
    private List<SignedClause> learnts;
    /**
     * Number of learnts signed clauses to not exceed
     */
    private final int nbMaxLearnts;
    /**
     * Ratio of clauses to keep on removal
     */
    private final double ratio;

    private final int domPerimeter;
    /**
     * Reference to the last learnt signed clause, for checking routine
     */
    private SignedClause last;

    private HashMap<IntVar, IntervalTree<Container>> watches;
    /**
     * Amount to bump clause with.
     */
    private double clauseInc = 1d;

    private ClausesBased strat;

    /**
     * Create a Nogood store connected to a model.
     *
     * @param mModel model to be connected with
     */
    ClauseStore(Model mModel) {
        super(new IntVar[]{mModel.intVar(0)}, PropagatorPriority.LINEAR, true, false);
        this.vars = new IntVar[0];
        this.mSolver = mModel.getSolver();
        this.nbMaxLearnts = model.getSettings().getNbMaxLearntClauses();
        this.ratio = model.getSettings().getRatioForClauseStoreReduction();
        this.domPerimeter = model.getSettings().getLearntClausesDominancePerimeter();
        this.clauses = new ArrayList<>();
        this.learnts = new ArrayList<>();
        last = null;
        this.watches = new HashMap<>();
        setActive0();
    }

    public int getNbClauses() {
        return clauses.size();
    }

    public int getNbLearntClauses() {
        return learnts.size();
    }

    public void declareClausesBasedStrategy(ClausesBased strat) {
        this.strat = strat;
    }

    /**
     * Declare a new signed clause in this store
     */
    public void add(IntVar[] vars, IntIterableRangeSet[] ranges) {
        if (XParameters.INTERVAL_TREE) {
            SignedClause cl = new SignedClause(vars, ranges);
            attach(new Watcher(cl.pos[0], cl));
            attach(new Watcher(cl.pos[1], cl));
            if (model.getSolver().getEngine().isInitialized()) {
                this.learnts.add(cl);
                last = cl;
                last.activity = clauseInc;
                last.rawActivity = 1;
                if (XParameters.PRINT_CLAUSE) System.out.printf("learn: %s\n", cl);
            } else {
                if (XParameters.PRINT_CLAUSE) System.out.printf("add: %s\n", cl);
                this.clauses.add(cl);
            }
            mSolver.getEngine().dynamicAddition(true, cl);
        } else {
            PropSignedClause cl = PropSignedClause.makeFromIn(vars, ranges);
            if (XParameters.PRINT_CLAUSE) System.out.printf("learn: %s\n", cl);
            new Constraint("SC", cl).post();
        }
    }

    private void attach(Watcher w) {
        IntVar var = w.c.v(w.p);
        IntervalTree<Container> wm = watches.get(var);
        if (wm == null) {
            wm = new IntervalTree<>();
            watches.put(var, wm);
            this.addVariable(var);
        }
        Container ct = wm.get(w.c.l(w.p), w.c.u(w.p));
        if (ct == null) {
            ct = new Container(w.c.l(w.p), w.c.u(w.p));
            wm.insert(ct);
        }
        ct.add(w);
    }

    /**
     * Remove the nogood at position <i>idx</i>
     *
     * @param idx position of the nogood in {@link #learnts}.
     */
    private void remove(int idx) {
        SignedClause ng = learnts.remove(idx);
        mSolver.getEngine().dynamicDeletion(ng);
        ng.pos[0] = ng.pos[1] = -1; // to remove it from watchers
    }

    private void check(SignedClause ng) {
        if (mSolver.getDecisionPath().size() > 1) { // if at root node)
            // collect variables related to UNDEF lits.
            // If only one variable is concerned, then we can force the clause to filter.
            IntVar uni = null;
            int usl = 0;
            int fsl = 0;
            for (int i = 0; i < ng.pos.length; i++) {
                switch (ng.check(i)) {
                    case TRUE:
                        throw new SolverException("Learn a satisfied signed clause: " + ng);
                    case FALSE:
                        fsl++;
                        break;
                    case UNDEFINED:
                        if (usl == 0 && uni == null) {
                            uni = ng.mvars[i];
                            usl++;
                        } else if (usl > 0 && uni != ng.mvars[i]) {
                            uni = null;
                        }
                        break;
                }
            }
            if (fsl < ng.cardinality() - 1) {
                if (uni == null) {
                    throw new SolverException("Learn a weak clause (" + fsl + "/" + ng.cardinality() + ")");
                }
            }
            if (XParameters.ASSERT_ASSERTING_LEVEL
                    && fsl == ng.cardinality()) {
                throw new SolverException("wrong clause asserting level");
            }
        }
    }

    /**
     * Try to delete signed clauses from this nogood store.
     */
    public void forget() {
        if(strat != null){
            strat.decayActivity();
        }
        decayActivity();
        if (mSolver.getDecisionPath().size() == 1) { // at root node
            simplifyDB();
        } else if (last != null) {
            if (XParameters.ASSERT_UNIT_PROP) {
                check(last);
            }
            detectDominance();
            if(strat != null){
                Stream.of(last.getVars()).forEach(v -> strat.bump(v));
            }
        }
        // 2. reduce database
        reduceDB();
        last = null;
    }

    private void decayActivity() {
        // Increase the increment by 0.1%.  This introduces "activity
        // inflation", making all previous activity counts have less value.
        clauseInc *= 1.001;
        // If inflation has become too much, normalise all the activity
        // counts by scaling everything down by a factor of 1e20.
        if (clauseInc > 1e20) {
            clauseInc *= 1e-20;
            for (int i = 0; i < learnts.size(); i++) {
                learnts.get(i).activity *= 1e-20;
            }
        }
    }


    /**
     * Top level clean up. At root node, remove clauses entailed to true.
     */
    private void simplifyDB() {
        int size = learnts.size();
        for (int i = size - 1; i >= 0; i--) {
            SignedClause ng = learnts.get(i);
            if (ng.isNotLocked() && ng.isEntailed() == ESat.TRUE) {
                remove(i);
            }
        }
        if (size > learnts.size() && model.getSettings().warnUser()) {
            System.out.printf("Simplify DB: %d -> %d\n", size, learnts.size());
        }
    }

    /**
     * Remove sclauses with a lifespan greater than <i>lifespan</i> and which did not filtered in
     * the current branch.
     */
    private void reduceDB() {
        int size = learnts.size();
        if (size >= nbMaxLearnts) {
            learnts.sort(Comparator.comparingDouble(c -> -c.activity));
            long to = Math.round(ratio * size);
            for (int i = size - 1; i >= to; i--) {
                SignedClause ng = learnts.get(i);
                if (ng.isNotLocked() && ng != last) {
                    remove(i);
                }
            }
            if (size > learnts.size() && model.getSettings().warnUser()) {
                System.out.printf("Reduce DB: %d -> %d\n", size, learnts.size());
            }
            for (IntervalTree<Container> t : watches.values()) {
                Stack<Container> del = new Stack<>();
                for (Container c : t) {
                    c.watchers.removeIf(w -> !w.c.isConnected());
                    if(c.watchers.isEmpty()){
                        del.push(c);
                    }
                }
                while(!del.isEmpty()){
                    t.delete(del.pop());
                }
            }
        }
    }

    private void detectDominance() {
        int size = learnts.size();
        SignedClause ng0 = learnts.get(size - 1);
        for (int i = size - 2; i >= Math.max(0, size - domPerimeter - 1); i--) {
            SignedClause ng = learnts.get(i);
            if (ng.isNotLocked() && ng0.dominate(ng) > 0) {
                remove(i);
            }
        }
        if (size > learnts.size() && model.getSettings().warnUser()) {
            System.out.printf("Dominance DB: %d -> %d\n", size, learnts.size());
        }
    }


    public void printStatistics() {
        learnts.sort(Comparator.comparingInt(c -> -c.rawActivity));
        System.out.print("Top ten clauses:\n");
        for (int i = 0; i < 10 && i < learnts.size(); i++) {
            System.out.printf("%d : %d %s\n", i, learnts.get(i).rawActivity, learnts.get(i));
        }
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        // nothing is done here
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        // iterate over clauses that needs to be propagator
        IntVar var = vars[idxVarInProp];
        IntervalTree<Container> wm = watches.get(var);
        int lb = var.getLB();
        int ub = var.getUB();
        if (IntEventType.isInstantiate(mask) || IntEventType.isRemove(mask)) {
            sweep(wm.iterator(), var, lb, ub);
        } else {
            if (IntEventType.isInclow(mask)) {
                wm.forAllBelow(lb, c -> checkCont(c, var, lb, ub));
            }
            if (IntEventType.isDecupp(mask)) {
                wm.forAllAbove(ub, c -> checkCont(c, var, lb, ub));
            }
        }
    }

    @Override
    public ESat isEntailed() {
        ESat sat = ESat.TRUE;
        for (int i = 0; i < clauses.size() && sat == TRUE; i++) {
            sat = clauses.get(i).isEntailed();
        }
        for (int i = 0; i < learnts.size() && sat == TRUE; i++) {
            sat = learnts.get(i).isEntailed();
        }
        return sat;
    }


    private void sweep(Iterator<Container> it, IntVar v, int lb, int ub) {
        while (it.hasNext()) {
            checkCont(it.next(), v, lb, ub);
        }
    }

    private void checkCont(Container ct, IntVar v, int lb, int ub) {
        if (!ct.isActive()) return;
        ESat check = check(lb, ub, ct.s, ct.e, v);
        if (check != UNDEFINED) {
            if (check == FALSE) {
                ct.sweepOnFalse();
            } else {
                ct.sweepOnTrue();
            }
            model.getEnvironment().save(ct::setActive);
            ct.setPassive();
        }
    }

    private final class Container implements Interval {

        int s, e;
        ShrinkableList<Watcher> watchers;
        boolean active = true;

        Container(int s, int e) {
            this.s = s;
            this.e = e;
            this.watchers = new ShrinkableList<>();
        }

        @Override
        public int start() {
            return s;
        }

        @Override
        public int end() {
            return e;
        }

        public void add(Watcher w) {
            this.watchers.add(w);
        }

        boolean isActive() {
            return active;
        }

        void setPassive() {
            active = false;
        }

        void setActive() {
            active = true;
        }

        void sweepOnFalse() {
            int i = 0;
            int j = i;
            int p;
            int s = watchers.size();
            while (i < s) {
                Watcher w = watchers.get(i++);
                SignedClause c = w.c;
                if (w.p != c.pos[p = 0] && w.p != c.pos[++p]) {
                    // watched literal loss, forget it
                    continue;
                }
                if (w.c.isScheduled()) { // clause already scheduled, skip it
                    watchers.set(j++, w);
                    continue;
                }
                if (!w.c.isActive()) {
                    // clause passive, forget it
                    model.getEnvironment().save(() -> {
                        if (w.c.isConnected()) {
                            attach(w);
                        }
                    });
                    continue;
                }/*else*/
                {
                    watchers.set(j++, w); // keep it, even it is false, since propagation may change the other WL
                    mSolver.getEngine().schedule(w.c, p, 1);
                }
            }
            // shrink
            watchers.removeRange(j, i);
        }

        void sweepOnTrue() {
            int i = 0;
            int j = 0;
            int s = watchers.size();
            while (i < s) {
                Watcher w = watchers.get(i++);
                SignedClause c = w.c;
                if (w.p == c.pos[0] || w.p == c.pos[1]) {
                    watchers.set(j++, w);
                }// watched literal loss due to initial propagation
            }
            // shrink
            watchers.removeRange(j, i);
        }

        @Override
        public String toString() {
            return String.format("[%d,%d]", start(), end());
        }
    }


    private static final class Watcher {
        int p;
        SignedClause c; // clause watch by c.mvars[p]

        Watcher(int p, SignedClause c) {
            this.p = p;
            this.c = c;
        }

        @Override
        public String toString() {
            return c.toString();
        }
    }

    private static PropagatorPriority computePriority(int nbvars) {
        if (nbvars == 2) {
            return PropagatorPriority.BINARY;
        } else if (nbvars == 3) {
            return PropagatorPriority.TERNARY;
        } else {
            return PropagatorPriority.LINEAR;
        }
    }

    private static ESat check(int lv, int uv, int l, int u, IntVar v) {
        if (l <= lv && uv <= u) { // v in [l,u]
            return ESat.TRUE;
        } else if (l > uv || lv > u || (v.hasEnumeratedDomain() && v.nextValue(l - 1) > u)) {  // v does not intersect [l,u]
            return ESat.FALSE;
        }
        return ESat.UNDEFINED;
    }

    public class SignedClause extends Propagator<IntVar> {

        static final short LOCK = 4;
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

        private double activity = 0d;

        private int rawActivity = 0;

        private int id;

        IntIterableRangeSet uua;

        SignedClause(IntVar[] vars, IntIterableRangeSet[] ranges) {
            super(new IntVar[]{vars[0], vars[0]}, computePriority(vars.length), false, false);
            this.vars = new IntVar[0];
            this.uua = new IntIterableRangeSet();
            setActive0();
            this.id = SID++;
            // TODO: accurately select literals
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
//                 synchronize positions of var[0] and var[1]
                int nbr = ranges[0].getNbRanges();
                int p = this.pos[1];
                this.pos[1] = this.pos[nbr];
                this.pos[nbr] = p;
            }
        }


        /**
         * @return the number of literals in this
         */
        public final int cardinality() {
            return mvars.length;
        }

        private ESat check(int p) {
            return ClauseStore.check(mvars[p].getLB(), mvars[p].getUB(), bounds[p << 1], bounds[(p << 1) + 1], mvars[p]);
        }

        private boolean restrict(int p) throws ContradictionException {
            return mvars[p].updateBounds(bounds[p << 1], bounds[(p << 1) + 1], this);
        }

        public final boolean isConnected() {
            return pos[0] > -1 && pos[1] > -1;
        }

        @SuppressWarnings("Duplicates")
        public final void propagate(int evtmask) throws ContradictionException {
            if (evtmask == 2) {
                if (!isConnected()) {
                    // this was removed from ClauseStore, to ignore
                    return;
                }
                switch (check(pos[0])) {
                    case TRUE:
                        FL = F0;
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
                        setPassive();
                        return;
                    case FALSE:
                        FL |= F2;
                        break;
                    case UNDEFINED:
                        break;
                }
            }
            if (FL != F0) {
                propagateClause();
            }
            if (evtmask == 2 && this.isActive()) {
                detectHiddenUUA();
            }
        }

        /**
         * Detect hidden "unit under assignment" case.
         * This is supposed to be called only at coarse propagation (so mainly on backtrack).
         * It collects variables related to UNDEF lits.
         * If only one variable is concerned, then we can force the clause to filter.
         * @throws ContradictionException not supposed to happen
         */
        private void detectHiddenUUA() throws ContradictionException {
            IntVar one = null;
            this.uua.clear();
            fl:
            for (int i = 0; i < pos.length; i++) {
                switch (check(i)) {
                    case UNDEFINED:
                        if (one == null || one == mvars[i]) {
                            one = mvars[i];
                            uua.addBetween(bounds[i << 1], bounds[(i << 1) + 1]);
                        } else {
                            one = null;
                            break fl;
                        }
                        break;
                    case TRUE:
                        one = null;
                        break fl;
                }
            }
            if (one != null) {
                if (one.removeAllValuesBut(uua, this)) {
                    setPassiveAndLock();
                } else setPassive();
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
                }
                // Look for new watch:
                boolean cont = false;
                for (; k < to; k++) {
                    int l = pos[k];
                    ESat b = check(l);
                    if (b != FALSE) {
                        // update watcher -- preserve the operations order
                        // remove current watcher
//                        detach(this, pos[1]); // useless, will be removed later on
                        pos[1] = l;
                        pos[k] = pos[--to];
                        pos[to] = l1;
                        attach(new Watcher(l, this));
                        if (b == TRUE) {
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
                        setPassiveAndLock();
                        return;
                    } else {
                        assert this.isEntailed() != FALSE;
                    }
                }
            } while (FL != F0);
        }

        private void setPassiveAndLock() {
            state = LOCK;
            model.getEnvironment().save(operations[ACTIVE]);
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

        boolean isNotLocked() {
            return state != 4;
        }

        IntVar v(int i) {
            return mvars[i];
        }

        int l(int i) {
            return bounds[i << 1];
        }

        int u(int i) {
            return bounds[(i << 1) + 1];
        }

        /**
         * Test if one clause outshines another one or is incomparable with it. A clause ci
         * outshines a clause cj iff: <ul> <li>var(ci) &sube; var(cj) and</li> <li>for each v in
         * var(ci), rang(v, ci) &sube; rang(v, cj)</li> </ul>
         *
         * @param cj another clause
         * @return negative integer, zero, or a positive integer as ci outshines, is not comparable
         * with or is outshone by cj.
         * @implSpec vars in each clause is supposed to be sorted wrt the var ID. Otherwise, this
         * method can return incorrect results.
         */
        final int dominate(SignedClause cj) {
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
         *
         * @param ci a clause
         * @param cj another clause
         * @return 1 if ci outshines cj, 0 otherwise
         * @implSpec variables, in each clause, are supposed to be sorted wrt to increasing ID.
         */
        private int outhsine0(SignedClause ci, SignedClause cj) {
            int[] idx = {0, 0};
            boolean outs = true;
            while (idx[0] <= ci.mvars.length - 1 && idx[1] <= cj.mvars.length - 1 && outs) {
                int idi = ci.mvars[idx[0]].getId();
                int idj = cj.mvars[idx[1]].getId();
                if (idi == idj) {
                    outs = includedIn(ci, cj, idi, idj, idx);
                } else if (idj < idi) {
                    idx[1]++;
                    outs = idx[1] >= idx[0];
                } else {
                    outs = false;
                }
            }
            return outs ? 1 : 0;
        }

        /**
         * Considering two clauses with same cardinality, check which one outshines the other, if
         * any.
         *
         * @param ci a clause
         * @param cj another clause
         * @return 1, 0 or -1 as ci outshines cj, ci and cj are incomparable or cj oushines ci.
         * @implSpec variables, in each clause, are supposed to be sorted wrt to increasing ID.
         */
        private int outhsine1(SignedClause ci, SignedClause cj) {
            int k = ci.mvars.length - 1;
            int outi = 0, outj = 0;
            byte skip = 0b00;
            while (k >= 0 && skip < 0b11) {
                int idi = ci.mvars[k].getId();
                int idj = cj.mvars[k].getId();
                if (idi == idj) {
                    if (outi >= outj && cj.l(k) <= ci.l(k) && ci.u(k) <= cj.u(k)) {
                        outi++;
                    } else {
                        skip |= 0b01;
                    }
                    if (outj >= outi - 1 /* -1: because of previous condition */ &&
                            ci.l(k) <= cj.l(k) && cj.u(k) <= ci.u(k)) {
                        outj++;
                    } else {
                        skip |= 0b10;
                    }
                    k--;
                } else break;
            }
            if (outi == ci.mvars.length) {
                return 1;
            } else if (outj == cj.mvars.length) {
                return -1;
            }
            return 0;
        }

        boolean includedIn(SignedClause ci, SignedClause cj, int idi, int idj, int[] idx) {
            int lbi = ci.l(idx[0]);
            int ubi = ci.u(idx[0]);
            int lbj = cj.l(idx[1]);
            int ubj = cj.u(idx[1]);
            while (idx[0] <= ci.mvars.length - 1 && idi == ci.mvars[idx[0]].getId()
                    && idx[1] <= cj.mvars.length - 1 && idj == cj.mvars[idx[1]].getId()) {
                if (ubj < lbi && ++idx[1] <= cj.mvars.length - 1 && idj == cj.mvars[idx[1]].getId()) {
                    lbj = cj.l(idx[1]);
                    ubj = cj.u(idx[1]);
                } else if (lbj <= lbi && ubi <= ubj) {
                    if (++idx[0] <= ci.mvars.length - 1 && idi == ci.mvars[idx[0]].getId()) {
                        lbi = ci.l(idx[0]);
                        ubi = ci.u(idx[0]);
                    }
                } else {
                    return false;
                }
            }
            return true;
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
            activity += clauseInc;
            rawActivity += 1;
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
                }else{
                    v.unionLit(set, explanation);
                }
            }
        }

        @Override
        public String toString() {
            StringBuilder st = new StringBuilder();
            st.append("#").append(id).append(" : ");
            st.append("?").append(isEntailed()).append(" : ");
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
}
