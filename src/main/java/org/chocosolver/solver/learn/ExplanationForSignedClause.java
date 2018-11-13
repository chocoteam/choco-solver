/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2018, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.learn;

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.nary.clauses.ClauseBuilder;
import org.chocosolver.solver.constraints.nary.clauses.ClauseStore;
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
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableSetUtils;

import java.util.HashMap;

/**
 * An implementation of {@link IExplanation} dedicated to learn signed clauses
 * <p>
 *
 * <p>
 * Project: choco-solver.
 * @author Charles Prud'homme
 * @since 27/01/2017.
 */
public class ExplanationForSignedClause extends IExplanation {

    /**
     * Set to false when skip assertion that
     * no left branch are backtracked to
     */
    public static boolean ASSERT_NO_LEFT_BRANCH = true;
    /**
     * Set to true to force all cause to use default explanations
     */
    public static boolean DEFAULT_X = false;
    /**
     * FOR DEBUGGING PURPOSE ONLY.
     * Set to true to output proofs
     */
    public static boolean PROOF = false;
    /**
     * FOR DEBUGGING PURPOSE ONLY.
     * Set to true to output proofs with details
     */
    public static boolean FINE_PROOF = PROOF;

    /**
     * Conflicting nodes
     */
    private ValueSortedMap<IntVar> front;
    /**
     * Literals that explains the conflict
     */
    private HashMap<IntVar, IntIterableRangeSet> literals;
    /**
     * The decision to refute (ie, point to jump to wrt the current decision path).
     * @implSpec 0 represents the ROOT node,
     *           any value greater than the decision path is ignored,
     *           otherwise it represents the decision to refute in the decision path.
     */
    private int assertLevel = 0;
    /**
     * The implication graph
     */
    private final Implications mIG;

    private PoolManager<IntIterableRangeSet> manager;

    public ExplanationForSignedClause(Implications ig) {
        front = new ValueSortedMap<>();
        literals = new HashMap<>();
        manager = new PoolManager<>();
        mIG = ig;
    }

    /**
     * @implSpec The
     */
    @Override
    public void extractConstraint(Model mModel, ClauseStore ngstore) {
        ClauseBuilder ngb = mModel.getClauseBuilder();
        literals.forEach(ngb::put);
        ngb.buildNogood(mModel);
    }

    @Override
    public void recycle() {
        front.clear();
        literals.forEach((v,r) -> returnSet(r));
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
                literals.get(var);
                if (dec.getDecOp().equals(DecisionOperatorFactory.makeIntEq())) {
                    if (dec.hasNext() || dec.getArity() == 1) {
                        dom = getRootSet(var);
                        dom.remove(dec.getDecisionValue());
                    } else {
                        dom = getFreeSet(dec.getDecisionValue());
                    }
                } else if (dec.getDecOp().equals(DecisionOperatorFactory.makeIntNeq())) {
                    if (dec.hasNext()|| dec.getArity() == 1) {
                        dom = getFreeSet(dec.getDecisionValue());
                    } else {
                        dom = getRootSet(var);
                        dom.remove(dec.getDecisionValue());
                    }
                } else if (dec.getDecOp().equals(DecisionOperatorFactory.makeIntSplit())) { // <=
                    dom = getRootSet(var);
                    if (dec.hasNext()|| dec.getArity() == 1) {
                        dom.retainBetween(dec.getDecisionValue() + 1, IntIterableRangeSet.MAX);
                    } else {
                        dom.retainBetween(IntIterableRangeSet.MIN, dec.getDecisionValue());
                    }
                } else if (dec.getDecOp().equals(DecisionOperatorFactory.makeIntReverseSplit())) { // >=
                    dom = getRootSet(var);
                    if (dec.hasNext()|| dec.getArity() == 1) {
                        dom.retainBetween(IntIterableRangeSet.MIN, dec.getDecisionValue() - 1);
                    } else {
                        dom.retainBetween(dec.getDecisionValue(), IntIterableRangeSet.MAX);
                    }
                }
                addLiteral(var, dom, false);
            }
        }
    }

    /**
     * From a given conflict, defined by <i>cex</i> and the current implication graph <i>mIG</i>,
     * this method will compute the signed clause inferred from the conflict.
     * A call to {@link #extractConstraint(Model, ClauseStore)} will return the computed result.
     * @param cex the conflict
     *
     */
    public void learnSignedClause(ContradictionException cex) {
        recycle();
        if (PROOF) System.out.print("<-----");
        initFront(cex);
        loop();
        if (PROOF) System.out.print(">\n");
    }

    private void initFront(ContradictionException cex) {
        mIG.collectNodesFromConflict(cex, front);
        // deal with global conflict
        if (cex.v == null) {
            if (Propagator.class.isAssignableFrom(cex.c.getClass())) {
                if (PROOF) {
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
        while (!stop()) {
            current = front.pollLastValue();
            mIG.predecessorsOf(current, front);
            if (PROOF) {
                System.out.printf("\nCstr: %s\n", mIG.getCauseAt(current));
                System.out.printf("Pivot: %s = %s\n", mIG.getIntVarAt(current).getName(),
                        mIG.getDomainAt(current));
            }
            explain(mIG.getCauseAt(current), current);
            if (PROOF) {
                System.out.printf("Expl: %s\n-----", literals);
            }
            // filter irrelevant nodes
//            front.removeIf(updateFront);
            while (!front.isEmpty() && !literals.containsKey(mIG.getIntVarAt(front.getLastValue()))) {
                front.pollLastValue();
            }
        }
    }

    private void explain(ICause cause, int p) {
        if (p == -1 || DEFAULT_X
                && Propagator.class.isAssignableFrom(cause.getClass())) {
            Propagator<IntVar> propagator = (Propagator<IntVar>) cause;
            Propagator.defaultExplain(propagator, this, front, mIG, p);
        } else {
            cause.explain(this, front, mIG, p);
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
                    IntIterableRangeSet set = getFreeSet();
                    set.add(1 - b.getValue());
                    if (FINE_PROOF) System.out.print("Reif: ");
                    addLiteral(b, set, false);
                } else {
                    throw new UnsupportedOperationException("Oh nooo!");
                }
            }
        }
    }

    /**
     * Estimate if conflict analysis can stop:
     * <ul>
     *     <li>the rightmost node in conflict is a decision</li>
     *     <li>or it is above the first decision</li>
     * </ul>
     * @return <i>true</i> if the conflict analysis can stop
     */
    private boolean stop() {
        int max;
        if (front.isEmpty() || IntEventType.VOID.getMask() == mIG.getEventMaskAt(max = front.getLastValue())) {
            if (PROOF) System.out.print("\nbacktrack to ROOT\n-----");
            assertLevel = mIG.getIntVarAt(0)
                    .getModel()
                    .getSolver()
                    .getDecisionPath()
                    .getDecision(0)
                    .getPosition();
        } else if (IntDecision.class.isAssignableFrom(mIG.getCauseAt(max).getClass())) {
            if (PROOF)
                System.out.printf("\nbacktrack to %s\n-----", mIG.getCauseAt(max));
            if(ASSERT_NO_LEFT_BRANCH && !((IntDecision) mIG.getCauseAt(max)).hasNext()){
                throw new SolverException("Weak explanation found. Try to backjump to :" + mIG.getCauseAt(max));
            }
            assertLevel = ((IntDecision) mIG.getCauseAt(max)).getPosition();
        }
        return assertLevel != Integer.MAX_VALUE;
    }

    /**
     * Add a signed literal (<i>var</i> &isin; <i>dom</i>) to this explanation.
     * This is achieved in three steps:
     * <ol>
     *     <li>signed binary resolution (where 'v' is the pivot variable):
     *     <pre>
     *         (v &isin; A &or; X), (v &isin; B &or; Y) : (v &isin; (A&cap;B) &or; X &or; Y)
     *     </pre>
     *     <li>
     *         simplification:
     *         <pre>
     *             (v &isin; &empty; &or; Z) : (Z)
     *         </pre>
     *     </li>
     *     <li>
     *         join literals:
     *         <pre>
     *             ((&forall;i v &isin; Ai) &or; Z) : (v &isin; (&cup;i Ai) &or; Z)
     *         </pre>
     *     </li>
     *
     *
     *     </li>
     * </ol>
     * @param var signed literal variable
     * @param dom signed literal domain
     * @param pivot <i>true</i> if <i>var</i> is the pivot variable
     */
    public void addLiteral(IntVar var, IntIterableRangeSet dom, boolean pivot) {
        assert literals.values().stream().noneMatch(d -> d.equals(dom)) : "try to add a dom already declare";
        /*if(VariableUtils.isConstant(var) && !dom.contains(var.getValue())){
            if(FINE_PROOF.getAsBoolean())System.out.printf("%s: %s -- skip\n", var.getName(), dom);
            returnSet(dom);
            return;
        }*/
        if (var.isBool()) {
            dom.retainBetween(0, 1);
            if (!dom.contains(0) && !dom.contains(1)) {
                if (FINE_PROOF)
                    System.out.printf("%s: %s -- skip\n", var.getName(), dom);
                if (pivot) {
                    literals.remove(var);
                    front.remove(var);
                }
                returnSet(dom);
                return;
            }
        }
        addLiteralInternal(var, dom, pivot);
    }

    private void addLiteralInternal(IntVar var, IntIterableRangeSet dom, boolean pivot) {
        IntIterableRangeSet rset = literals.get(var);
        if (rset == null) {
            if (dom.size() > 0) {
                if (FINE_PROOF) System.out.printf("%s: %s\n", var.getName(), dom);
                literals.put(var, dom);
            } else {
                if (FINE_PROOF)
                    System.out.printf("%s: %s -- skip\n", var.getName(), dom);
                returnSet(dom);
            }
        } else {
            if (pivot) {
                if (FINE_PROOF)
                    System.out.printf("%s: %s ∩ %s", var.getName(), rset, dom);
                IntIterableSetUtils.intersectionOf(rset, dom);
                if (FINE_PROOF) System.out.printf(" = %s", rset);
            } else {
                if (FINE_PROOF)
                    System.out.printf("%s: %s ∪ %s", var.getName(), rset, dom);
                IntIterableSetUtils.unionOf(rset, dom);
                if (FINE_PROOF) System.out.printf(" = %s", rset);
            }
            if (rset.size() == 0) {
                assert !var.isBool() || rset.contains(0) || !rset.contains(1);
                if (FINE_PROOF) System.out.print(" -- remove");
                literals.remove(var);
                front.remove(var);
                returnSet(rset);
            }
            if (FINE_PROOF) System.out.print("\n");
            returnSet(dom);
        }
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
     * @return a free set
     */
    public IntIterableRangeSet getFreeSet() {
        IntIterableRangeSet set = manager.getE();
        if (set == null) {
            return new IntIterableRangeSet();
        }
        return set;
    }

    /**
     * Return an available set (created and returned) or create a new one
     * then add 'val' to it.
     * @return a free set
     */
    public IntIterableRangeSet getFreeSet(int val) {
        IntIterableRangeSet set = manager.getE();
        if (set == null) {
            set = new IntIterableRangeSet();
        }
        set.add(val);
        return set;
    }

    /**
     * Return an available set (created and returned) or create a new one
     * then add range ['a','b'] to it.
     * @return a free set
     */
    public IntIterableRangeSet getFreeSet(int a, int b) {
        IntIterableRangeSet set = manager.getE();
        if (set == null) {
            set = new IntIterableRangeSet();
        }
        set.addBetween(a, b);
        return set;
    }

    public void returnSet(IntIterableRangeSet set) {
        set.clear();
        manager.returnE(set);
    }

    /**
     * @param p position
     * @return a set which contains a copy of the domain of the var at position <i>p</i>
     */
    public IntIterableRangeSet getSet(int p) {
        IntIterableRangeSet set = getFreeSet();
        set.copyFrom(mIG.getDomainAt(p));
        return set;
    }

    /**
     * @param var a variable
     * @return a set which contains a copy of the domain of <i>var</i> at its front position
     */
    public IntIterableRangeSet getSet(IntVar var) {
        return getSet(front.getValue(var));
    }

    /**
     * @param var a variable
     * @return a set which contains a copy of the complement domain of <i>var</i> at its front position
     * wrt to its root domain
     */
    public IntIterableRangeSet getComplementSet(IntVar var) {
        IntIterableRangeSet set = getFreeSet();
        set.copyFrom(mIG.getRootDomain(var));
        set.removeAll(mIG.getDomainAt(front.getValue(var)));
        return set;
    }

    /**
     * @param var a variable
     * @return a set which contains a copy of the root domain of <i>var</i>
     */
    public IntIterableRangeSet getRootSet(IntVar var) {
        IntIterableRangeSet set = getFreeSet();
        set.copyFrom(mIG.getRootDomain(var));
        return set;
    }

    public ValueSortedMap<IntVar> getFront() {
        return front;
    }

    public HashMap<IntVar, IntIterableRangeSet> getLiterals() {
        return literals;
    }

    @Override
    public String toString() {
        StringBuilder st = new StringBuilder();
        st.append('{');
        for (IntVar v : literals.keySet()) {
            st.append(v.getName()).append('\u2208').append(literals.get(v)).append(',');
        }
        st.append('}');
        return st.toString();

    }
}
