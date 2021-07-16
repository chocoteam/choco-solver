/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.learn;

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.nary.clauses.ClauseBuilder;
import org.chocosolver.solver.constraints.nary.clauses.ClauseStore;
import org.chocosolver.solver.constraints.nary.clauses.PropSignedClause;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.search.strategy.assignments.DecisionOperatorFactory;
import org.chocosolver.solver.search.strategy.decision.DecisionPath;
import org.chocosolver.solver.search.strategy.decision.IntDecision;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.PoolManager;
import org.chocosolver.util.objects.ValueSortedMap;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableRangeSet;

import java.util.HashSet;

/**
 * An implementation of {@link IExplanation} dedicated to learn signed clauses
 * <p>
 *
 * <p>
 * Project: choco-solver.
 *
 * @author Charles Prud'homme
 * @since 27/01/2017.
 */
public class ExplanationForSignedClause extends IExplanation {

    /**
     * Conflicting nodes
     */
    private final ValueSortedMap<IntVar> front;
    /**
     * Literals that explains the conflict
     */
    private final HashSet<IntVar> literals;
    /**
     * The decision to refute (ie, point to jump to wrt the current decision path).
     *
     * @implSpec 0 represents the ROOT node,
     * any value greater than the decision path is ignored,
     * otherwise it represents the decision to refute in the decision path.
     */
    private int assertLevel = 0;
    /**
     * The implication graph
     */
    private final Implications mIG;

    private final PoolManager<IntIterableRangeSet> manager;

    public ExplanationForSignedClause(Implications ig) {
        front = new ValueSortedMap<>();
        literals = new HashSet<>();
        manager = new PoolManager<>();
        mIG = ig;
    }

    /**
     * @implSpec The
     */
    @Override
    public void extractConstraint(Model mModel, ClauseStore ngstore) {
        ClauseBuilder ngb = mModel.getClauseBuilder();
        literals.forEach(v -> ngb.put(v, v.getLit().export())); // TODO : improve
        ngb.buildNogood(mModel);
    }

    @Override
    public void recycle() {
        front.clear();
        literals.forEach(IntVar::flushLit);
        literals.clear();
        assertLevel = Integer.MAX_VALUE;
    }

    public void learnSolution(DecisionPath path) {
        recycle();
        if (path.size() > 1) { // skip solution at ROOT node
            int i = path.size() - 1;
            IntDecision dec = (IntDecision) path.getDecision(i);
            // skip refuted bottom decisions
            while (i > 1 /*0 is ROOT */ && !dec.hasNext() && dec.getArity() > 1) {
                dec = (IntDecision) path.getDecision(--i);
            }
            // build a 'fake' explanation that is able to refute the right decision
            for (; i > 0 /*0 is ROOT */ ; i--) {
                dec = (IntDecision) path.getDecision(i);
                IntIterableRangeSet dom = null;
                IntVar var = dec.getDecisionVariable();
                if (dec.getDecOp().equals(DecisionOperatorFactory.makeIntEq())) {
                    if (dec.hasNext() || dec.getArity() == 1) {
                        dom = universe();
                        dom.remove(dec.getDecisionValue());
                    } else {
                        dom = empty();
                        dom.add(dec.getDecisionValue());
                    }
                } else if (dec.getDecOp().equals(DecisionOperatorFactory.makeIntNeq())) {
                    if (dec.hasNext() || dec.getArity() == 1) {
                        dom = empty();
                        dom.add(dec.getDecisionValue());
                    } else {
                        dom = universe();
                        dom.remove(dec.getDecisionValue());
                    }
                } else if (dec.getDecOp().equals(DecisionOperatorFactory.makeIntSplit())) { // <=
                    dom = universe();
                    if (dec.hasNext() || dec.getArity() == 1) {
                        dom.retainBetween(dec.getDecisionValue() + 1, IntIterableRangeSet.MAX);
                    } else {
                        dom.retainBetween(IntIterableRangeSet.MIN, dec.getDecisionValue());
                    }
                } else if (dec.getDecOp().equals(DecisionOperatorFactory.makeIntReverseSplit())) { // >=
                    dom = universe();
                    if (dec.hasNext() || dec.getArity() == 1) {
                        dom.retainBetween(IntIterableRangeSet.MIN, dec.getDecisionValue() - 1);
                    } else {
                        dom.retainBetween(dec.getDecisionValue(), IntIterableRangeSet.MAX);
                    }
                }
                var.unionLit(dom, this);
            }
        }
    }

    /**
     * From a given conflict, defined by <i>cex</i> and the current implication graph <i>mIG</i>,
     * this method will compute the signed clause inferred from the conflict.
     * A call to {@link #extractConstraint(Model, ClauseStore)} will return the computed result.
     *
     * @param cex the conflict
     */
    public void learnSignedClause(ContradictionException cex) {
        recycle();
        if (XParameters.PROOF) System.out.print("<-----");
        initFront(cex);
        loop();
        if (XParameters.PROOF) System.out.print(">\n");
    }

    private void initFront(ContradictionException cex) {
        mIG.collectNodesFromConflict(cex, front);
        // deal with global conflict
        if (cex.v == null) {
            if (Propagator.class.isAssignableFrom(cex.c.getClass())) {
                if (XParameters.PROOF) {
                    System.out.printf("\nCstr: %s\n", cex.c);
                    System.out.print("Pivot: none\n");
                }
                explain(cex.c, -1);
            } else {
                throw new UnsupportedOperationException();
            }
        }
    }

    private void loop() {
        int current;
        do {
            current = front.pollLastValue();
            mIG.predecessorsOf(current, front);
            if (XParameters.PROOF) {
                System.out.printf("\nCstr: %s\n", mIG.getCauseAt(current));
                System.out.printf("Pivot: %s = %s\n", mIG.getIntVarAt(current).getName(),
                        mIG.getDomainAt(current));
            }
            explain(mIG.getCauseAt(current), current);
            if (XParameters.PROOF) {
                System.out.print("Expl: {");
                literals.stream()
                        //.sorted(Comparator.comparingInt(Identity::getId))
                        .forEach(v -> System.out.printf("%s ∈ %s,", v, v.getLit()));
                System.out.print("}\n-----");
            }
            // filter irrelevant nodes
            relax();
        } while (!stop());
    }

    private void explain(ICause cause, int p) {
        if (p == -1 ||
                XParameters.DEFAULT_X
                        && Propagator.class.isAssignableFrom(cause.getClass())
                        && !PropSignedClause.class.isAssignableFrom(cause.getClass())
                        && !ClauseStore.SignedClause.class.isAssignableFrom(cause.getClass())
        ) {
            Propagator<IntVar> propagator = (Propagator<IntVar>) cause;
            Propagator.defaultExplain(propagator, p, this);
        } else {
            cause.explain(p, this);
        }
        // check reification
        checkReification(cause, p);
    }

    private void checkReification(ICause cause, int p) {
        if (Propagator.class.isAssignableFrom(cause.getClass())) {
            Propagator<IntVar> propagator = (Propagator<IntVar>) cause;
            if (propagator.isReified()) {
                BoolVar b = propagator.reifiedWith();
                assert !propagator.isReifiedAndSilent();
                mIG.findPredecessor(front, b, p == -1 ? mIG.size() : p);
                if (b.isInstantiated()) {
                    if (XParameters.FINE_PROOF) System.out.print("Reif: ");
                    b.unionLit(1 - b.getValue(), this);
                } else {
                    throw new UnsupportedOperationException("Oh nooo!");
                }
            }
        }
    }

    private void relax() {
        int l, k = -1;
        while (!front.isEmpty() && (l = front.getLastValue()) != k) {
            // remove variable in 'front' but not in literals
            // achieved lazily by only evaluating the right-most one
            if (!literals.contains(mIG.getIntVarAt(l))) {
                front.pollLastValue();
            } else {
                IntVar var = mIG.getIntVarAt(l);
                // cpru deal with : if(VariableUtils.isView(var))?
                int p = mIG.getPredecessorOf(l);
                // todo improve
                // go left as long as the right-most variable in 'front' contradicts 'literals'
                if (p < l /* to avoid going "before" root */
                        && var.getLit().disjoint(mIG.getDomainAt(p))) {
                    front.replace(var, p);
                }
            }
            k = l;
        }
    }

    /**
     * Estimate if conflict analysis can stop:
     * <ul>
     * <li>the rightmost node in conflict is a decision</li>
     * <li>or it is above the first decision</li>
     * </ul>
     *
     * @return <i>true</i> if the conflict analysis can stop
     */
    private boolean stop() {
        int max;
        if (front.isEmpty()
                || IntEventType.VOID.getMask() == mIG.getEventMaskAt(max = front.getLastValue())
                || mIG.getDecisionLevelAt(max) == 1) {
            if (XParameters.PROOF) System.out.print("\nbacktrack to ROOT\n-----");
            assertLevel = mIG.getIntVarAt(0)
                    .getModel()
                    .getSolver()
                    .getDecisionPath()
                    .getDecision(0)
                    .getPosition();
        } else
            /*// check UIP
              // WARNING: the following code does not work. It cannot be applied stricto-senso from SAT
              // Since a variable may have been modified more than once in a decision level, unlike SAT
            {
            int prev = front.getLowerValue(max);
            int dl = mIG.getDecisionLevelAt(max);
            if (prev == -1 || mIG.getDecisionLevelAt(prev) != dl) {
                // find backtrack point
                while (max > 0 && !IntDecision.class.isAssignableFrom(mIG.getCauseAt(max).getClass())) {
                    max--;
                }
                //assert mIG.getDecisionLevelAt(max) != dl;
                assert IntDecision.class.isAssignableFrom(mIG.getCauseAt(max).getClass());
                if (PROOF)
                    System.out.printf("\nbacktrack to %s\n-----", mIG.getCauseAt(max));
                if (ASSERT_NO_LEFT_BRANCH && !((IntDecision) mIG.getCauseAt(max)).hasNext()) {
                    throw new SolverException("Weak explanation found. Try to backjump to :" + mIG.getCauseAt(max) + "\n" + literals);
                }
                assertLevel = ((IntDecision) mIG.getCauseAt(max)).getPosition();
            }
            /*/if (IntDecision.class.isAssignableFrom(mIG.getCauseAt(max).getClass())) {
            if (XParameters.PROOF)
                System.out.printf("\nbacktrack to %s\n-----", mIG.getCauseAt(max));
            if (XParameters.ASSERT_NO_LEFT_BRANCH && !((IntDecision) mIG.getCauseAt(max)).hasNext()) {
                throw new SolverException("Weak explanation found. Try to backjump to :" + mIG.getCauseAt(max) + "\n" + literals);
            }
            assertLevel = ((IntDecision) mIG.getCauseAt(max)).getPosition();
            //*/
        }
        return assertLevel != Integer.MAX_VALUE;
    }

    /**
     * @see IntVar#unionLit(int, ExplanationForSignedClause)
     * @see IntVar#unionLit(int, int, ExplanationForSignedClause)
     * @see IntVar#unionLit(IntIterableRangeSet, ExplanationForSignedClause)
     * @see IntVar#intersectLit(int, ExplanationForSignedClause)
     * @see IntVar#intersectLit(int, int, ExplanationForSignedClause)
     * @see IntVar#intersectLit(IntIterableRangeSet, ExplanationForSignedClause)
     * @deprecated
     */
    @Deprecated
    public void addLiteral(IntVar var, IntIterableRangeSet dom, boolean pivot) {
        if (pivot) {
            var.intersectLit(dom, this);
        } else {
            var.unionLit(dom, this);
        }
    }

    /**
     * Remove {@code var} from {@link #literals} and {@link #front}
     *
     * @param var a variable
     */
    public void removeLit(IntVar var) {
        literals.remove(var);
        front.remove(var);
    }

    /**
     * Add {@code var} to {@link #literals}
     *
     * @param var a variable
     */
    public void addLit(IntVar var) {
        literals.add(var);
    }

    /**
     * Check if {@code var} is in {@link #literals}
     *
     * @param var a variable
     */
    public boolean contains(IntVar var) {
        return literals.contains(var);
    }

    /**
     * @return the number of literals in this explanation
     */
    public int getCardinality() {
        return literals.size();
    }

    /**
     * @return the decision to refute (ie, point to jump to wrt the current decision path).
     */
    public int getAssertingLevel() {
        return assertLevel;
    }

    /**
     * Return an empty set available (created and returned) or create a new one
     *
     * @return a free set
     */
    public IntIterableRangeSet empty() {
        IntIterableRangeSet set = manager.getE();
        if (set == null) {
            return new IntIterableRangeSet();
        } else {
            set.unlock();
        }
        return set;
    }

    public void returnSet(IntIterableRangeSet set) {
        set.clear();
        set.lock();
        manager.returnE(set);
    }

    /**
     * @param var a variable
     * @return a set which contains a copy of the domain of <i>var</i> at its front position
     */
    public IntIterableRangeSet domain(IntVar var) {
        IntIterableRangeSet set = empty();
        set.copyFrom(readDom(var));
        return set;
    }

    /**
     * @param var a variable
     * @return a set which contains a copy of the complement domain of <i>var</i> at its front position
     * wrt to its root domain
     */
    public IntIterableRangeSet complement(IntVar var) {
        IntIterableRangeSet set = root(var);
        set.removeAll(readDom(var));
        return set;
    }
    
    /**
     * @param val a value
     * @return a set which contains all values after <i>val</i> and <i>val</i>
     */
    public IntIterableRangeSet setDiffVal(int val) {
        IntIterableRangeSet set = universe();
        set.remove(val);
        return set;
    }

    /**
     * Return (-&infin;,+&infin;) set (created and returned).
     *
     * @return a full set
     */
    public IntIterableRangeSet universe() {
        IntIterableRangeSet set = empty();
        set.addBetween(IntIterableRangeSet.MIN, IntIterableRangeSet.MAX);
        return set;
    }

    /**
     * @param var a variable
     * @return a <b>copy</b> of the root domain of <i>var</i>
     */
    public IntIterableRangeSet root(IntVar var) {
        IntIterableRangeSet set = empty();
        set.copyFrom(mIG.getRootDomain(var));
        return set;
    }

    public ValueSortedMap<IntVar> getFront() {
        return front;
    }

    public Implications getImplicationGraph() {
        return mIG;
    }

    /**
     * Return the variable stored in {@link #mIG} at positon {@code p}.
     *
     * @param p position of the node to read.
     * @return the variable at position {@code p} in {@link #mIG}.
     */
    public IntVar readVar(int p) {
        return mIG.getIntVarAt(p);
    }

    /**
     * Return the event mask stored in {@link #mIG} at positon {@code p}.
     *
     * @param p position of the node to read.
     * @return the event mask at position {@code p} in {@link #mIG}
     */
    public int readMask(int p) {
        return mIG.getEventMaskAt(p);
    }

    /**
     * Return the value stored in {@link #mIG} at positon {@code p}.
     *
     * @param p position of the node to read.
     * @return the value at position {@code p} in {@link #mIG}
     */
    public int readValue(int p) {
        return mIG.getValueAt(p);
    }


    /**
     * Return the domain stored in {@link #mIG} at positon {@code p}.
     *
     * @param p position of the node to read.
     * @return the domain at position {@code p} in {@link #mIG}
     * @implNote <b>read-only</b> method.
     * Object returned by this method is not intended to be modified.
     */
    public IntIterableRangeSet readDom(int p) {
        return mIG.getDomainAt(p);
    }

    /**
     * Return the domain stored in {@link #mIG} at positon {@code p}.
     *
     * @param var variable to read
     * @return the domain at position {@code p} in {@link #mIG}
     * @implNote <b>read-only</b> method.
     * Object returned by this method is not intended to be modified.
     * @implSpec position of {@code var} in {@link #mIG} is retrieved
     * through {@link #front}
     */
    public IntIterableRangeSet readDom(IntVar var) {
        return mIG.getDomainAt(front.getValue(var));
    }

    public HashSet<IntVar> getLiterals() {
        return literals;
    }

    @Override
    public String toString() {
        StringBuilder st = new StringBuilder();
        st.append('{');
        literals.stream()
                //.sorted(Comparator.comparingInt(Identity::getId))
                .forEach(v -> st.append(v.getName()).append('\u2208').append(v.getLit()).append(','));
        st.append('}');
        return st.toString();

    }
}
