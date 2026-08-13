/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary;

import gnu.trove.list.array.TIntArrayList;
import org.chocosolver.solver.ICause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.loop.monitors.IMonitorRestart;
import org.chocosolver.solver.search.strategy.assignments.DecisionOperator;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.search.strategy.decision.DecisionPath;
import org.chocosolver.solver.search.strategy.decision.IntDecision;
import org.chocosolver.solver.search.strategy.decision.SetDecision;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.events.PropagatorEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.queues.CircularQueue;

import java.util.*;

import static org.chocosolver.solver.search.strategy.assignments.DecisionOperatorFactory.*;


/**
 * A propagator that records and minimizes nogoods from restarts to avoid repeating the same inferences.
 *
 * <p>This implementation is based on the paper "Recording and Minimizing Nogoods from Restarts"
 * (doi: <a href="https://doi.org/10.3233/SAT190009">10.3233/SAT190009</a>).
 * Nogoods are conjunctions of decisions that lead to a contradiction. By storing and enforcing
 * these nogoods, the solver can prune equivalent subtrees in the search tree during subsequent restarts,
 * significantly improving search efficiency.
 *
 * <p>The propagator supports optional nogood minimization, which reduces the size of nogoods by identifying
 * the minimal set of decisions (transition decisions) that cause the contradiction. This is achieved
 * through either a constructive approach ({@link #minimizeNogood(Dec[])} or a dichotomic (binary search)
 * approach ({@link #minimizeNogoodDichotomy(Dec[])}).
 *
 * <p>Nogoods are stored as arrays of {@link Dec}, where each {@code Dec} represents the negation of a
 * decision (e.g., if the decision was "x = v", the corresponding {@code Dec} is "x != v").
 * The propagator uses a watched literals scheme (with two watched positions per nogood) for efficient
 * propagation and contradiction detection.
 *
 * <p>This propagator monitors the search process through the {@link IMonitorRestart} interface:
 * <ul>
 *   <li>Before each restart, {@link #beforeRestart()} extracts nogoods from the current decision path.</li>
 *   <li>During each restart, {@link #duringRestart()} minimizes and adds the pending nogoods.</li>
 * </ul>
 *
 * @author Charles Prud'homme
 * @see IMonitorRestart
 * @see Propagator
 */
public class NogoodBase extends Propagator<Variable> implements IMonitorRestart {

    private static final boolean DICHOTOMIC_APPROACH = true;

    private static final int WL1 = 0;
    private static final int WL2 = 1;
    /**
     * Stores the decision path before
     */
    @SuppressWarnings("rawtypes")
    private final ArrayDeque<Decision> decisions;
    /**
     * List of stored nogoods
     */
    private final List<Nogood> nogoods = new ArrayList<>();
    /**
     * Store unary nogoods
     */
    private final List<Dec<Variable>> unaries = new ArrayList<>();
    /**
     * For each variable (index in {@link #vars}), list of decs involving it.
     */
    private final TreeMap<Variable, TreeSet<Dec<Variable>>> var2decs = new TreeMap<>();
    /**
     * Stores all modified decs to make sure a fix point is reached.
     */
    private final CircularQueue<Dec<?>> decQueue = new CircularQueue<>(16);
    /**
     * Whether nogood minimization is enabled.
     */
    private final boolean minimize;
    /**
     * Nogoods pending minimization (the last nogood of each restart).
     */
    private final List<Dec<Variable>[]> pendingMinimization = new ArrayList<>();


    enum Op {
        eq, ne, le, ge, in, out
    }

    static final class Dec<V extends Variable> implements Comparable<Dec<V>> {
        final V var;
        final Op op;
        final int val;
        final TIntArrayList watches = new TIntArrayList();
        private final int hash;

        Dec(V var, Op op, int val) {
            this.var = var;
            this.op = op;
            this.val = val;
            this.hash = 31 * (31 * System.identityHashCode(var) + op.ordinal()) + val;
        }

        @SuppressWarnings("unchecked")
        public static <V extends Variable> Dec<V> from(Decision<V> decision) {
            if (decision instanceof IntDecision id) {
                IntVar var = id.getDecisionVariable();
                int val = id.getDecisionValue();
                DecisionOperator<IntVar> op = id.getDecOp();
                op = op.opposite();
                if (op == makeIntSplit()) { // was "geq" before opposite
                    val--;
                }
                if (op == makeIntReverseSplit()) { // was "leq" before opposite
                    val++;
                }
                //}
                return new Dec<>((V) var, NogoodBase.toOp(op), val);
            } else if (decision instanceof SetDecision sd) {
                SetVar var = sd.getDecisionVariable();
                int val = sd.getDecisionValue();
                DecisionOperator<SetVar> op = sd.getDecOp();
                op = op.opposite();
                return new Dec<>((V) var, NogoodBase.toOp(op), val);
            } else {
                throw new UnsupportedOperationException("Unsupported decision type: " + decision);
            }
        }

        public boolean isSatisfied() {
            switch (op) {
                case ne -> {
                    return !var.asIntVar().contains(val);
                }
                case eq -> {
                    return var.asIntVar().isInstantiatedTo(val);
                }
                case ge -> {
                    return var.asIntVar().getLB() >= val;
                }
                case le -> {
                    return var.asIntVar().getUB() <= val;
                }
                case in -> {
                    return var.asSetVar().getLB().contains(val);
                }
                case out -> {
                    return !var.asSetVar().getUB().contains(val);
                }
            }
            throw new UnsupportedOperationException();
        }

        public boolean isFalsified() {
            switch (op) {
                case ne -> {
                    return var.asIntVar().isInstantiatedTo(val);
                }
                case eq -> {
                    return !var.asIntVar().contains(val);
                }
                case ge -> {
                    return var.asIntVar().getUB() < val;
                }
                case le -> {
                    return var.asIntVar().getLB() > val;
                }
                case in -> {
                    return !var.asSetVar().getUB().contains(val);
                }
                case out -> {
                    return var.asSetVar().getLB().contains(val);
                }
            }
            throw new UnsupportedOperationException();
        }

        public boolean canBeWatched() {
            switch (op) {
                case ne -> {
                    return !var.asIntVar().contains(val) || var.asIntVar().getDomainSize() > 1;
                }
                case eq -> {
                    return var.asIntVar().contains(val);
                }
                case ge -> {
                    return var.asIntVar().getLB() >= val || var.asIntVar().getUB() >= val;
                }
                case le -> {
                    return var.asIntVar().getUB() <= val || var.asIntVar().getLB() <= val;
                }
                case in -> {
                    return var.asSetVar().getLB().contains(val) || var.asSetVar().getUB().contains(val);
                }
                case out -> {
                    return !var.asSetVar().getUB().contains(val) || !var.asSetVar().getLB().contains(val);
                }
            }
            throw new UnsupportedOperationException();
        }

        public void satisfy(ICause cause) throws ContradictionException {
            switch (op) {
                case ne -> var.asIntVar().removeValue(val, cause);
                case eq -> var.asIntVar().instantiateTo(val, cause);
                case ge -> var.asIntVar().updateLowerBound(val, cause);
                case le -> var.asIntVar().updateUpperBound(val, cause);
                case in -> var.asSetVar().force(val, cause);
                case out -> var.asSetVar().remove(val, cause);
            }
        }

        /**
         * Falsify this Dec, i.e., apply the original decision it negates.
         */
        public void falsify(ICause cause) throws ContradictionException {
            switch (op) {
                case ne -> var.asIntVar().instantiateTo(val, cause);
                case eq -> var.asIntVar().removeValue(val, cause);
                case ge -> var.asIntVar().updateUpperBound(val - 1, cause);
                case le -> var.asIntVar().updateLowerBound(val + 1, cause);
                case in -> var.asSetVar().remove(val, cause);
                case out -> var.asSetVar().force(val, cause);
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Dec<?> d) {
                return d.var == this.var && d.op.equals(this.op) && d.val == this.val;
            }
            return false;
        }

        @Override
        public String toString() {
            return "Dec{" + var.getName() +
                    " " + op +
                    " " + val +
                    '}';
        }

        @Override
        public int compareTo(Dec<V> o) {
            int diff = this.val - o.val;
            if (diff == 0) return op.ordinal() - o.op.ordinal();
            return diff;
        }

        @Override
        public int hashCode() {
            return hash;
        }
    }

    private static <V extends Variable> Op toOp(DecisionOperator<V> d) {
        if (d == makeIntEq()) {
            return Op.eq;
        } else if (d == makeIntNeq()) {
            return Op.ne;
        }
        if (d == makeIntSplit()) {
            return Op.le;
        }
        if (d == makeIntReverseSplit()) {
            return Op.ge;
        }
        if (d == makeSetForce()) {
            return Op.in;
        }
        if (d == makeSetRemove()) {
            return Op.out;
        }
        throw new UnsupportedOperationException();
    }

    static class Nogood {
        final Dec<Variable>[] decs;

        Nogood(Dec<Variable>[] lits) {
            this.decs = lits;
        }

        Dec<Variable> dec(int idx) {
            return decs[idx];
        }

        void swap(int i0, int i1) {
            Dec<Variable> t = decs[i0];
            decs[i0] = decs[i1];
            decs[i1] = t;
        }

        public int size() {
            return decs.length;
        }

        @Override
        public String toString() {
            return "Nogood{" +
                    "decs=" + Arrays.toString(decs) +
                    '}';
        }
    }

    /**
     * Creates a NogoodBase propagator.
     *
     * @param model    the model to attach this propagator to
     * @param minimize if {@code true}, nogoods will be minimized before being stored;
     *                 if {@code false}, nogoods will be stored as-is
     */
    public NogoodBase(Model model, boolean minimize) {
        super(new Variable[]{model.boolVar(false)},
                PropagatorPriority.VERY_SLOW, true, false);
        // erase model.ONE from the variable scope
        this.vars = new Variable[0];
        this.decisions = new ArrayDeque<>();
        this.minimize = minimize;
        new Constraint("NogoodBase", this).post();
    }

    /**
     * Called before a restart occurs. Extracts nogoods from the current decision path
     * and prepares them for minimization (if enabled).
     */
    @Override
    public void beforeRestart() {
        storeNogood(model.getSolver().getDecisionPath());
    }

    /**
     * Called during a restart. Minimizes and adds all pending nogoods collected
     * from the previous search phase.
     */
    @Override
    public void duringRestart() {
        if (!pendingMinimization.isEmpty()) {
            for (Dec<Variable>[] candidate : pendingMinimization) {
                Dec<Variable>[] minimized =
                        DICHOTOMIC_APPROACH ? minimizeNogoodDichotomy(candidate) : minimizeNogood(candidate);
                // Use minimized version if successful, otherwise keep the original
                addNogood(minimized.length > 0 ? minimized : candidate);
            }
            pendingMinimization.clear();
            forcePropagationOnBacktrack();
        }
    }

    /**
     * Extracts an increasing nogood from the current decision path and stores it.
     *
     * @param decisionPath the current decision path
     */
    @SuppressWarnings("unchecked")
    public void storeNogood(DecisionPath decisionPath) {
        decisionPath.transferInto(decisions, false);
        int d = decisions.size();
        Decision<Variable> decision;
        Dec<Variable>[] decs = new Dec[d];
        int i = 0;
        while (!decisions.isEmpty()) {
            decision = decisions.pollFirst();
            Dec<Variable> dec = Dec.from(decision);
            if (decision.hasNext() || decision.getArity() == 1) {
                decs[i++] = dec;
            } else {
                decs[i] = dec;
                Dec<Variable>[] ng = Arrays.copyOf(decs, i + 1);
//                System.out.println("Extract "+Arrays.toString(ng));
                if (minimize && ng.length > 1) {
                    // All nogoods are candidates for minimization.
                    // Those from direct dead-ends (φ-nogoods) will be reduced,
                    // others will be kept as-is (fallback in afterRestart).
                    pendingMinimization.add(ng);
                } else {
                    addNogood(ng);
                }
            }
        }
        forcePropagationOnBacktrack();
    }

    /**
     * Minimizes a reduced nld-nogood using the constructive approach.
     * Iteratively finds transition decisions: the decisions whose addition
     * triggers inconsistency when propagated.
     * <p>
     * Returns the minimized nogood if a contradiction was found,
     * or an empty array if the nogood is not a φ-nogood
     * (inconsistency is not detectable by propagation alone).
     *
     * @param decs the candidate nogood to minimize
     * @return the minimized nogood, or empty if minimization failed
     */
    @SuppressWarnings("unchecked")
    private Dec<Variable>[] minimizeNogood(Dec<Variable>[] decs) {
        List<Dec<Variable>> kept = new ArrayList<>();
        List<Dec<Variable>> remaining = new ArrayList<>(Arrays.asList(decs));
        var solver = model.getSolver();

        while (!remaining.isEmpty()) {
            solver.pushTrail();
            try {
                // Apply all previously found transition decisions
                for (Dec<Variable> d : kept) {
                    d.falsify(this);
                }
                solver.propagate();

                // Try remaining decisions one by one
                Dec<Variable> transition = null;
                int transitionIdx = -1;
                for (int i = 0; i < remaining.size(); i++) {
                    Dec<Variable> d = remaining.get(i);
                    try {
                        d.falsify(this);
                        solver.propagate();
                    } catch (ContradictionException e) {
                        solver.getEngine().flush();
                        transition = d;
                        transitionIdx = i;
                        break;
                    }
                }

                if (transition != null) {
                    kept.add(transition);
                    // Remove the transition and everything after it from remaining
                    remaining.subList(transitionIdx, remaining.size()).clear();
                } else {
                    // No contradiction found: not a φ-nogood
                    return new Dec[0];
                }
            } catch (ContradictionException e) {
                // The kept decisions alone already cause inconsistency
                solver.getEngine().flush();
                break;
            } finally {
                solver.cancelTrail();
            }
        }

        return kept.toArray(new Dec[0]);
    }

    /**
     * Minimizes a reduced nld-nogood using a dichotomic (binary search) approach.
     * Instead of testing remaining decisions one by one, splits them in half
     * and uses binary search to locate the transition decision that triggers
     * inconsistency.
     * <p>
     * Returns the minimized nogood if a contradiction was found,
     * or an empty array if the nogood is not a φ-nogood.
     *
     * @param decs the candidate nogood to minimize
     * @return the minimized nogood, or empty if minimization failed
     */
    @SuppressWarnings("unchecked")
    private Dec<Variable>[] minimizeNogoodDichotomy(Dec<Variable>[] decs) {
        List<Dec<Variable>> kept = new ArrayList<>();
        List<Dec<Variable>> remaining = new ArrayList<>(Arrays.asList(decs));
        var solver = model.getSolver();

        while (!remaining.isEmpty()) {
            solver.pushTrail();
            try {
                // Apply all previously found transition decisions
                for (Dec<Variable> d : kept) {
                    d.falsify(this);
                }
                solver.propagate();

                // Binary search for the transition decision in remaining
                Dec<Variable> transition = findTransitionDichotomy(remaining, 0, remaining.size(), solver);

                if (transition != null) {
                    int transitionIdx = remaining.indexOf(transition);
                    kept.add(transition);
                    // Remove the transition and everything after it from remaining
                    remaining.subList(transitionIdx, remaining.size()).clear();
                } else {
                    // No contradiction found: not a φ-nogood
                    return new Dec[0];
                }
            } catch (ContradictionException e) {
                // The kept decisions alone already cause inconsistency
                solver.getEngine().flush();
                break;
            } finally {
                solver.cancelTrail();
            }
        }
        System.out.println("minimize");
        return kept.toArray(new Dec[0]);
    }

    /**
     * Finds the transition decision in {@code remaining[from..to)} using binary search.
     * A transition decision is one whose application (on top of already applied decisions)
     * triggers a contradiction via propagation.
     * <p>
     * The method applies decisions in bulk (first half), then recurses into the
     * half that contains the contradiction. This is O(log n) propagation calls
     * per transition instead of O(n).
     * <p>
     * IMPORTANT: this method is called within a pushTrail/cancelTrail block.
     * It does its own push/cancel for each recursive test, so the caller's
     * trail state is preserved.
     *
     * @param remaining the list of candidate decisions
     * @param from      inclusive start index
     * @param to        exclusive end index
     * @param solver    the solver instance
     * @return the transition decision, or null if none found
     */
    private Dec<Variable> findTransitionDichotomy(List<Dec<Variable>> remaining,
                                                  int from, int to,
                                                  org.chocosolver.solver.Solver solver) {
        if (from >= to) {
            return null;
        }
        if (to - from == 1) {
            // Base case: single decision — test it directly
            Dec<Variable> d = remaining.get(from);
            solver.pushTrail();
            try {
                d.falsify(this);
                solver.propagate();
                // No contradiction: this decision is not the transition
                return null;
            } catch (ContradictionException e) {
                solver.getEngine().flush();
                return d;
            } finally {
                solver.cancelTrail();
            }
        }

        int mid = from + (to - from) / 2;

        // Test if applying all decisions in [from, mid) causes contradiction
        solver.pushTrail();
        try {
            for (int i = from; i < mid; i++) {
                remaining.get(i).falsify(this);
            }
            solver.propagate();

            // No contradiction with first half — transition must be in [mid, to)
            // Keep the first half applied as context and recurse on second half
            return findTransitionDichotomy(remaining, mid, to, solver);
        } catch (ContradictionException e) {
            solver.getEngine().flush();
        } finally {
            solver.cancelTrail();
        }

        // Contradiction was in first half — recurse into [from, mid)
        return findTransitionDichotomy(remaining, from, mid, solver);
    }

    /**
     * Returns the canonical Dec instance for the given Dec, creating it if needed.
     */
    private Dec<Variable> canonicalize(Dec<Variable> dx) {
        TreeSet<Dec<Variable>> set = var2decs.get(dx.var);
        if (set == null) {
            this.addVariable(dx.var);
            set = new TreeSet<>();
            var2decs.put(dx.var, set);
        }
        Dec<Variable> existing = set.floor(dx);
        if (existing != null && existing.equals(dx)) {
            return existing;
        }
        set.add(dx);
        return dx;
    }

    private void addNogood(Dec<Variable>[] decs) {
//        System.out.println("Add " + Arrays.toString(decs));
        // canonicalize all decs first
        for (int i = 0; i < decs.length; i++) {
            decs[i] = canonicalize(decs[i]);
        }
        if (decs.length == 1) {
            unaries.add(decs[0]);
        } else {
            Nogood ng = new Nogood(decs);
            this.nogoods.add(ng);
            ng.decs[0].watches.add(nogoods.size() - 1);
            ng.decs[1].watches.add(nogoods.size() - 1);
        }
    }

    private void addFalsifiedDecs(Variable var) {
        TreeSet<Dec<Variable>> ngs = var2decs.get(var);
        if (ngs != null) {
            for (Dec<?> dx : var2decs.get(var)) {
                if (dx.isFalsified()) {
                    decQueue.addLast(dx);
                }
            }
        }
    }

    /**
     * Propagates events to enforce stored nogoods. Uses a watched literals scheme:
     * for each nogood, two decisions are watched. When a watched decision is falsified,
     * the propagator checks if the other watched decision can be satisfied.
     * If not, it searches for a new watch among the remaining decisions in the nogood.
     *
     * @param evtmask the propagation event mask
     * @throws ContradictionException if a contradiction is detected
     */
    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (PropagatorEventType.isFullPropagation(evtmask)) {
            for (int i = 0; i < unaries.size(); i++) {
                unaries.get(i).satisfy(this);
            }
            for (Variable var : this.vars) {
                addFalsifiedDecs(var);
            }
        }
        while (!decQueue.isEmpty()) {
            Dec<?> dx = decQueue.pollFirst();
            if (dx.isFalsified()) {
                TIntArrayList ngs = dx.watches;
                // inferences
                for (int i = 0; i < ngs.size(); i++) {
                    int nidx = ngs.getQuick(i);
                    Nogood ng = nogoods.get(nidx);
                    Dec<?> dy = ng.dec(WL2);
                    if (dx == dy) {
                        dy = ng.dec(WL1);
                    }
                    if (!dy.isSatisfied()) {
                        if (!canFindAnotherWatch(ng, dx)) {
                            dy.satisfy(this);
                            addFalsifiedDecs(dy.var);
                        } else {
                            // unsubscribe dx
                            ngs.setQuick(i, ngs.getQuick(ngs.size() - 1));
                            ngs.remove(ngs.size() - 1, 1);
                            i--;
                            // subscribe new watched dec
                            ng.dec(WL1).watches.add(nidx);
                        }
                    }
                }
            }
        }
    }

    /**
     * Propagates events for a specific variable. Adds falsified decisions to the queue
     * for further processing.
     *
     * @param idxVarInProp the index of the variable in the propagator's variable array
     * @param mask         the propagation event mask
     * @throws ContradictionException if a contradiction is detected
     */
    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        int d = decQueue.size();
        addFalsifiedDecs(vars[idxVarInProp]);
        if (d != decQueue.size()) {
            forcePropagate(PropagatorEventType.CUSTOM_PROPAGATION);
        }
    }

    private boolean canFindAnotherWatch(Nogood ng, Dec<?> dx) {
        // dx should be at WL1
        if (dx.equals(ng.dec(WL2))) {
            ng.swap(WL1, WL2);
        }// else {
        // throw new UnsupportedOperationException();
        //}
        assert dx.equals(ng.dec(WL1)) || dx.equals(ng.dec(WL1)) : dx + " not found in " + Arrays.toString(ng.decs) + " at 0:" + ng.dec(WL1);
        for (int i = 2; i < ng.size(); i++) {
            Dec<?> dy = ng.dec(i);
            if (dy.canBeWatched()) {
                ng.swap(WL1, i);
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if this propagator is entailed. NogoodBase is always entailed as it only
     * enforces constraints derived from the search process.
     *
     * @return {@link ESat#TRUE} always
     */
    @Override
    public ESat isEntailed() {
        return ESat.TRUE;
    }

}
