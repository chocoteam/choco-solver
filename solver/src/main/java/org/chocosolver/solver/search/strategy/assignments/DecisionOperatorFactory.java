/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.strategy.assignments;

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.GraphVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;

/**
 * Create serializable decisions. 
 * Decisions are static nested classes because serialization of lambda functions or anonymous classes is compiler-dependent.
 * Furthermore, the serialization of the singleton pattern requires a special treatment.
 * 
 * @see <a href="@linkp http://stackoverflow.com/questions/19440511/explanation-needed-why-adding-implements-serializable-to-singleton-class-is-ins">stack overflow</a>
 * @author Arnaud Malapert
 *
 */
public final class DecisionOperatorFactory {

    private DecisionOperatorFactory() {}

    public static DecisionOperator<IntVar> makeIntEq() {
        return IntEqDecision.getInstance();
    }

    public static DecisionOperator<IntVar> makeIntNeq() {
        return IntNeqDecision.getInstance();
    }

    public static DecisionOperator<IntVar> makeIntSplit() {
        return IntSplitDecision.getInstance();
    }

    public static DecisionOperator<IntVar> makeIntReverseSplit() {
        return IntReverseSplitDecision.getInstance();
    }

    public static DecisionOperator<SetVar> makeSetForce() {
        return SetForceDecision.getInstance();
    }

    public static DecisionOperator<SetVar> makeSetRemove() {
        return SetRemoveDecision.getInstance();
    }

    public static GraphDecisionOperator makeGraphEnforce() { return GraphEnforceDecision.getInstance(); }

    public static GraphDecisionOperator makeGraphRemove() { return GraphRemoveDecision.getInstance(); }

    // INTEGERS
    private static final class IntEqDecision implements DecisionOperator<IntVar> {

        private static final long serialVersionUID = 7293773317776136982L;

        private final static IntEqDecision INSTANCE = new IntEqDecision();

        public static IntEqDecision getInstance() {
            return INSTANCE;
        }

        /**
         * readResolve method to preserve singleton property
         */
        private Object readResolve() {
            // Return the one true INSTANCE and let the garbage collector
            // take care of the INSTANCE impersonator.
            return INSTANCE;
        }

        @Override
        public boolean apply(IntVar var, int value, ICause cause) throws ContradictionException {
        	// not true because of parallel portfolio (bound update)
            // assert var.contains(value) : "branching on value not in domain ; var :"+var+" val : "+value;
            // not true because of ABS
            // assert var.hasEnumeratedDomain() || var.getLB() == value || var.getUB() == value: "branching in the middle of bounded domain ; var :"+var+" val : "+value;
            return var.instantiateTo(value, cause);
        }

        @Override
        public boolean unapply(IntVar var, int value, ICause cause) throws ContradictionException {
            return var.removeValue(value, cause);
        }

        @Override
        public String toString() {
            return " == ";
        }

        @Override
        public DecisionOperator<IntVar> opposite() {
            return makeIntNeq();
        }
    }

    private static final class IntNeqDecision implements DecisionOperator<IntVar> {

        private static final long serialVersionUID = 3056222234436601667L;
        private final static IntNeqDecision INSTANCE = new IntNeqDecision();

        public static IntNeqDecision getInstance() {
            return INSTANCE;
        }

        /**
         * readResolve method to preserve singleton property
         */
        private Object readResolve() {
            // Return the one true INSTANCE and let the garbage collector
            // take care of the INSTANCE impersonator.
            return INSTANCE;
        }

        @Override
        public boolean apply(IntVar var, int value, ICause cause) throws ContradictionException {
			// not true because of parallel portfolio (bound update)
			// assert var.contains(value) : "branching on value not in domain ; var :"+var+" val : "+value;
            assert var.hasEnumeratedDomain() || var.getLB() == value || var.getUB() == value: "branching in the middle of bounded domain ; var :"+var+" val : "+value;
            return var.removeValue(value, cause);
        }

        @Override
        public boolean unapply(IntVar var, int value, ICause cause) throws ContradictionException {
            return var.instantiateTo(value, cause);
        }

        @Override
        public String toString() {
            return " != ";
        }

        @Override
        public DecisionOperator<IntVar> opposite() {
            return makeIntEq();
        }
    }

    private static final class IntSplitDecision implements DecisionOperator<IntVar> {

        private static final long serialVersionUID = 2796498653106384502L;
        private final static IntSplitDecision INSTANCE = new IntSplitDecision();

        public static IntSplitDecision getInstance() {
            return INSTANCE;
        }

        private IntSplitDecision() {
            super();
        }

        @Override
        public boolean apply(IntVar var, int value, ICause cause) throws ContradictionException {
			// not true because of parallel portfolio (bound update)
			// assert var.contains(value) : "branching on value not in domain ; var :"+var+" val : "+value;
            assert var.getUB()>value : "Branching value should be < UB; var :"+var+" val : "+value;
            return var.updateUpperBound(value, cause);
        }

        @Override
        public boolean unapply(IntVar var, int value, ICause cause) throws ContradictionException {
            return var.updateLowerBound(value + 1, cause);
        }

        @Override
        public String toString() {
            return " <= ";
        }

        @Override
        public DecisionOperator<IntVar> opposite() {
            return makeIntReverseSplit();
        }

    }

    private static final class IntReverseSplitDecision implements DecisionOperator<IntVar> {

        private static final long serialVersionUID = -4155926684198463505L;

        private final static IntReverseSplitDecision INSTANCE = new IntReverseSplitDecision();

        public static IntReverseSplitDecision getInstance() {
            return INSTANCE;
        }

        private IntReverseSplitDecision() {}

        /**
         * readResolve method to preserve singleton property
         */
        private Object readResolve() {
            // Return the one true INSTANCE and let the garbage collector
            // take care of the INSTANCE impersonator.
            return INSTANCE;
        }

        @Override
        public boolean apply(IntVar var, int value, ICause cause) throws ContradictionException {
			// not true because of parallel portfolio (bound update)
			// assert var.contains(value) : "branching on value not in domain ; var :"+var+" val : "+value;
            assert var.getLB()<value : "Branching value should be > LB; var :"+var+" val : "+value;
            return var.updateLowerBound(value, cause);
        }

        @Override
        public boolean unapply(IntVar var, int value, ICause cause) throws ContradictionException {
            return var.updateUpperBound(value - 1, cause);
        }

        @Override
        public String toString() {
            return " >= ";
        }

        @Override
        public DecisionOperator<IntVar> opposite() {
            return makeIntSplit();
        }
    }


    // SETS
    private static final class SetForceDecision implements DecisionOperator<SetVar> {

        private static final long serialVersionUID = -4868225105307378160L;

        private final static SetForceDecision INSTANCE = new DecisionOperatorFactory.SetForceDecision();

        public static SetForceDecision getInstance() {
            return INSTANCE;
        }

        private SetForceDecision() {}

        /**
         * readResolve method to preserve singleton property
         */
        private Object readResolve() {
            // Return the one true INSTANCE and let the garbage collector
            // take care of the INSTANCE impersonator.
            return INSTANCE;
        }

        @Override
        public boolean apply(SetVar var, int element, ICause cause) throws ContradictionException {
            assert var.getUB().contains(element) && !var.getLB().contains(element): "Invalid branching; var :"+var+" val : "+element;
            return var.force(element, cause);
        }

        @Override
        public boolean unapply(SetVar var, int element, ICause cause) throws ContradictionException {
            return var.remove(element, cause);
        }

        @Override
        public String toString() {
            return " contains ";
        }

        @Override
        public DecisionOperator<SetVar> opposite() {
            return DecisionOperatorFactory.makeSetRemove();
        }
    }

    private static final class SetRemoveDecision implements DecisionOperator<SetVar> {

        private static final long serialVersionUID = -580239209082758455L;

        private final static SetRemoveDecision INSTANCE = new SetRemoveDecision();

        public static SetRemoveDecision getInstance() {
            return INSTANCE;
        }

        private SetRemoveDecision() {}

        /**
         * readResolve method to preserve singleton property
         */
        private Object readResolve() {
            // Return the one true INSTANCE and let the garbage collector
            // take care of the INSTANCE impersonator.
            return INSTANCE;
        }

        @Override
        public boolean apply(SetVar var, int element, ICause cause) throws ContradictionException {
            assert var.getUB().contains(element) && !var.getLB().contains(element): "Invalid branching; var :"+var+" val : "+element;
            return var.remove(element, cause);
        }

        @Override
        public boolean unapply(SetVar var, int element, ICause cause) throws ContradictionException {
            return var.force(element, cause);
        }

        @Override
        public String toString() {
            return " !contains ";
        }

        @Override
        public DecisionOperator<SetVar> opposite() {
            return DecisionOperatorFactory.makeSetForce();
        }
    }

    // GRAPHS
    public static final class GraphEnforceDecision implements GraphDecisionOperator {

        private final static GraphEnforceDecision INSTANCE = new DecisionOperatorFactory.GraphEnforceDecision();

        public static GraphEnforceDecision getInstance() {
            return INSTANCE;
        }

        @Override
        public boolean apply(GraphVar var, int node, ICause cause) throws ContradictionException {
            if (node < var.getNbMaxNodes()) {
                return var.enforceNode(node, cause);
            } else {
                throw new UnsupportedOperationException();
            }
        }

        @Override
        public boolean unapply(GraphVar var, int node, ICause cause) throws ContradictionException {
            if (node < var.getNbMaxNodes()) {
                return var.removeNode(node, cause);
            } else {
                throw new UnsupportedOperationException();
            }
        }

        @Override
        public boolean apply(GraphVar var, int from, int to, ICause cause) throws ContradictionException {
            if (from == -1 || to == -1) {
                throw new UnsupportedOperationException();
            }
            return var.enforceEdge(from, to, cause);
        }

        @Override
        public boolean unapply(GraphVar var, int from, int to, ICause cause) throws ContradictionException {
            if (from == -1 || to == -1) {
                throw new UnsupportedOperationException();
            }
            return var.removeEdge(from, to, cause);
        }

        @Override
        public GraphDecisionOperator opposite() {
            return makeGraphRemove();
        }

        @Override
        public String toString() {
            return " enforcing ";
        }

    }

    public static final class GraphRemoveDecision implements GraphDecisionOperator {

        private final static GraphRemoveDecision INSTANCE = new DecisionOperatorFactory.GraphRemoveDecision();

        public static GraphRemoveDecision getInstance() {
            return INSTANCE;
        }

        @Override
        public boolean apply(GraphVar var, int node, ICause cause) throws ContradictionException {
            if (node < var.getNbMaxNodes()) {
                return var.removeNode(node, cause);
            } else {
                throw new UnsupportedOperationException();
            }
        }

        @Override
        public boolean unapply(GraphVar var, int node, ICause cause) throws ContradictionException {
            if (node < var.getNbMaxNodes()) {
                return var.enforceNode(node, cause);
            } else {
                throw new UnsupportedOperationException();
            }
        }

        @Override
        public boolean apply(GraphVar var, int from, int to, ICause cause) throws ContradictionException {
            if (from == -1 || to == -1) {
                throw new UnsupportedOperationException();
            }
            return var.removeEdge(from, to, cause);
        }

        @Override
        public boolean unapply(GraphVar var, int from, int to, ICause cause) throws ContradictionException {
            if (from == -1 || to == -1) {
                throw new UnsupportedOperationException();
            }
            return var.enforceEdge(from, to, cause);
        }

        @Override
        public GraphDecisionOperator opposite() {
            return makeGraphEnforce();
        }

        @Override
        public String toString() {
            return " removal ";
        }
    }
}
