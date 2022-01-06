/*
 * This file is part of examples, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.examples.integer;

import org.chocosolver.examples.AbstractProblem;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;

import static org.chocosolver.solver.search.strategy.Search.inputOrderUBSearch;

/**
 * <a href="http://www.mozart-oz.org/documentation/fdt/node21.html">mozart-oz</a>:<br/>
 * "A kid goes into a grocery store and buys four items. The cashier
 * charges $7.11, the kid pays and is about to leave when the cashier
 * calls the kid back, and says ''Hold on, I multiplied the four items
 * instead of adding them; I'll try again; Hah, with adding them the
 * price still comes to $7.11''. What were the prices of the four items?
 * <p/>
 * The model is taken from: Christian Schulte, Gert Smolka, Finite Domain
 * Constraint Programming in Oz. A Tutorial. 2001."
 * <br/>
 * <p/>
 * This problem deals with large domains which result in integer overflows with classical constraints.
 * Thus, this example introduces a dedicated propagator which handles large value products.
 *
 * @author Charles Prud'homme, Jean-Guillaume Fages
 * @since 08/08/11
 */
public class Grocery extends AbstractProblem {


    IntVar[] itemCost;


    @Override
    public void buildModel() {
        model = new Model("Grocery");
        itemCost = model.intVarArray("item", 4, 1, 711, false);
        model.sum(itemCost, "=", 711).post();

        // intermediary products
        IntVar[] tmp = model.intVarArray("tmp", 2, 1, 71100, true);
        model.times(itemCost[0], itemCost[1], tmp[0]).post();
        model.times(itemCost[2], itemCost[3], tmp[1]).post();

        // the global product itemCost[0]*itemCost[1]*itemCost[2]*itemCost[3] (equal to tmp[0]*tmp[1])
        // is too large to be used within integer ranges. Thus, we will set up a dedicated constraint
        // which uses a long to handle such a product

        new Constraint("LargeProduct", new PropLargeProduct(tmp, 711000000)).post();

        // symmetry breaking
        model.arithm(itemCost[0], "<=", itemCost[1]).post();
        model.arithm(itemCost[1], "<=", itemCost[2]).post();
        model.arithm(itemCost[2], "<=", itemCost[3]).post();
    }

    @Override
    public void configureSearch() {
        model.getSolver().setSearch(inputOrderUBSearch(itemCost));
    }

    @Override
    public void solve() {
        model.getSolver().solve();

        System.out.println("Grocery");
        StringBuilder st = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            st.append(String.format("\titem %d : %d$\n", (i + 1), itemCost[i].getValue()));
        }
        System.out.println(st);
    }

    public static void main(String[] args) {
        new Grocery().execute(args);
    }

    /**
     * Simple propagator ensuring that vars[0]*vars[1] = target
     * It has been designed to handle large values (by using longs)
     */
    private class PropLargeProduct extends Propagator<IntVar> {
        private final long target;

        /**
         * Large product propagator
         *
         * @param vrs    two integer variables
         * @param target long representing the expected value of vrs[0]*vrs[1]
         */
        public PropLargeProduct(IntVar[] vrs, long target) {
            // involved variables, priority (=arity), false (the last parameter should always be false!)
            super(vrs, PropagatorPriority.BINARY, true);
            assert vrs.length == 2;
            this.target = target;
        }

        @Override
        /**
         * Propagation condition : if a variable is instantiated or a domain bound is modified
         */
        public int getPropagationConditions(int vIdx) {
            return IntEventType.boundAndInst();
        }

        @Override
        /**
         * Initial propagation algorithm. Runs in O(1)
         */
        public void propagate(int evtmask) throws ContradictionException {
            long min = (long) (vars[0].getLB()) * (long) (vars[1].getLB());
            if (min > target) {
                fails(); // TODO: could be more precise, for explanation purpose
            }
            long max = (long) (vars[0].getUB()) * (long) (vars[1].getUB());
            if (max > 0 && max < target) {
                fails(); // TODO: could be more precise, for explanation purpose
            }
        }

        @Override
        /**
         * Incremental propagation (called after the initial propagation, each time a variable bound is modified.
         * In this case, we call the initial propagation directly (it runs in constant time).
         */
        public void propagate(int idxVarInProp, int mask) throws ContradictionException {
            propagate(0);
        }

        @Override
        /**
         * Entailment condition and feasibility checker
         */
        public ESat isEntailed() {
            long min = (long) (vars[0].getLB()) * (long) (vars[1].getLB());
            long max = (long) (vars[0].getUB()) * (long) (vars[1].getUB());
            if (min > target || (max > 0 && max < target)) {
                return ESat.FALSE;
            }
            if (isCompletelyInstantiated()) {
                return ESat.TRUE;
            } else {
                return ESat.UNDEFINED;
            }
        }

    }
}
