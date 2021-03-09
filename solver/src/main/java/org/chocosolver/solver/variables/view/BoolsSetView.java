/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.view;

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.events.SetEventType;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.SetDynamicFilter;
import org.chocosolver.util.objects.setDataStructures.SetFactory;

import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * Set view over an array of boolean variables defined such that:
 * boolVars[x - offset] = True <=> x in setView
 * This view is equivalent to the {@link org.chocosolver.solver.constraints.set.PropBoolChannel} constraint.
 */
public class BoolsSetView<B extends BoolVar> extends SetView<B> {

    /**
     * Offset between boolVars array indices and set elements
     */
    private int offset;

    /**
     * Dynamic sets observing the array of boolean variables
     * Such sets do not store data but behave like a regular (read-only) set.
     * They avoid constructing objects at each bound retrieval on the view,
     * and allow to take advantage of the view semantic to optimize bounds read operations.
     */
    private BoolsSetViewLB lb;
    private BoolsSetViewUB ub;

    /**
     * Instantiate an set view over an array of boolean variables such that:
     * boolVars[x - offset] = True <=> x in setView
     *
     * @param name  name of the variable
     * @param offset Offset between boolVars array indices and set elements
     * @param variables observed variables
     */
    protected BoolsSetView(String name, int offset, B... variables) {
        super(name, variables);
        this.offset = offset;
        this.lb = new BoolsSetViewLB(this);
        this.ub = new BoolsSetViewUB(this);
    }

    /**
     * Instantiate an set view over an array of boolean variables such that:
     * boolVars[x - offset] = True <=> x in setView
     *
     * @param offset Offset between boolVars array indices and set elements
     * @param variables observed variables
     */
    public BoolsSetView(int offset, B... variables) {
        this("BOOLS_SET_VIEW["
                    + String.join(",", Arrays.stream(variables)
                        .map(i -> i.getName())
                        .toArray(String[]::new))
                    + "]",
                offset, variables);
    }

    @Override
    protected boolean doRemoveSetElement(int element) throws ContradictionException {
        return getVariables()[element - this.offset].instantiateTo(BoolVar.kFALSE, this);
    }

    @Override
    protected boolean doForceSetElement(int element) throws ContradictionException {
        return getVariables()[element - this.offset].instantiateTo(BoolVar.kTRUE, this);
    }

    @Override
    public void notify(IEventType event, int variableIdx) throws ContradictionException {
        if (this.getVariables()[variableIdx].isInstantiatedTo(BoolVar.kTRUE)) {
            notifyPropagators(SetEventType.ADD_TO_KER, this);
        } else {
            notifyPropagators(SetEventType.REMOVE_FROM_ENVELOPE, this);
        }
    }

    @Override
    public ISet getLB() {
        return lb;
    }

    @Override
    public ISet getUB() {
        return ub;
    }

    @Override
    public boolean instantiateTo(int[] value, ICause cause) throws ContradictionException {
        boolean changed = !isInstantiated();
        ISet s = SetFactory.makeConstantSet(Arrays.stream(value).map(i -> i - offset).toArray());
        for (int i = 0; i < getNbObservedVariables(); i++) {
            B var = getVariables()[i];
            if (s.contains(i)) {
                var.instantiateTo(BoolVar.kTRUE, this);
            } else {
                var.instantiateTo(BoolVar.kFALSE, this);
            }
        }
        return changed;
    }

    @Override
    public boolean isInstantiated() {
        for (B var : getVariables()) {
            if (!var.isInstantiated()) {
                return false;
            }
        }
        return true;
    }

    private class BoolsSetViewLB extends BoolsSetViewBound {

        public BoolsSetViewLB(BoolsSetView ref) {
            super(ref);
        }

        @Override
        public boolean contains(int element) {
            if (element < ref.offset || element >= vars.length + ref.offset) {
                return false;
            }
            return vars[element - ref.offset].isInstantiatedTo(BoolVar.kTRUE);        }
    }

    private class BoolsSetViewUB extends BoolsSetViewBound {

        public BoolsSetViewUB(BoolsSetView ref) {
            super(ref);
        }

        @Override
        public boolean contains(int element) {
            if (element < ref.offset || element >= vars.length + ref.offset) {
                return false;
            }
            return vars[element - ref.offset].contains(BoolVar.kTRUE);          }
    }

    private abstract class BoolsSetViewBound extends SetDynamicFilter {

        protected BoolsSetView ref;
        protected B[] vars;

        public BoolsSetViewBound(BoolsSetView ref) {
            this.ref = ref;
            this.vars = (B[]) ref.getVariables();
        }

        @Override
        protected SetDynamicFilterIterator createIterator() {
            return new SetDynamicFilterIterator() {

                private int idx = 0;

                @Override
                protected void resetPointers() {
                    idx = 0;
                }

                @Override
                protected void findNext() {
                    next = null;
                    while (!hasNext()) {
                        if (idx == vars.length) {
                            return;
                        }
                        if (contains(idx + ref.offset)) {
                            next = idx + ref.offset;
                        }
                        idx++;
                    }
                }
            };
        }

        @Override
        public int min() {
            int i = 0;
            while (!contains(i + ref.offset)) {
                i++;
                if (i >= vars.length) {
                    throw new IllegalStateException("cannot find maximum of an empty set");
                }
            }
            return i + ref.offset;
        }

        @Override
        public int max() {
            int i = vars.length - 1;
            while (!contains(i + ref.offset)) {
                i--;
                if (i < 0) {
                    throw new IllegalStateException("cannot find maximum of an empty set");
                }
            }
            return i + ref.offset;
        }
    }
}
