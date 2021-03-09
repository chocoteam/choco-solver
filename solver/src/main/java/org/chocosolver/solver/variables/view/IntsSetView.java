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
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.delta.IIntDeltaMonitor;
import org.chocosolver.solver.variables.delta.SetDelta;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.events.SetEventType;
import org.chocosolver.util.objects.setDataStructures.*;
import org.chocosolver.util.procedure.IntProcedure;

import java.util.Arrays;

/**
 * Set view over an array of integer variables defined such that:
 * with v and offset two integers (constant) intVariables[x - offset] = c <=> x in set.
 */
public class IntsSetView<I extends IntVar> extends SetView<I> {

    /**
     * Integer value such that intVariables[x - offset] = v <=> x in set
     */
    private int v;

    /**
     * Integer value such that intVariables[x - offset] = v <=> x in set
     */
    private int offset;

    private IIntDeltaMonitor[] idm;

    private IntProcedure valRemoved;

    /**
     * Dynamic sets observing the array of integer variables
     * Such sets do not store data but behave like a regular (read-only) set.
     * They avoid constructing objects at each bound retrieval on the view,
     * and allow to take advantage of the view semantic to optimize bounds read operations.
     */
    private IntsSetViewLB lb;
    private IntsSetViewUB ub;

    /**
     * Instantiate an set view over an array of integer variables such that:
     * intVariables[x - offset] = c <=> x in set
     *
     * @param name  name of the variable
     * @param v integer that "toggle" integer variables index inclusion in the set view
     * @param offset offset such that if intVariables[x - offset] = v <=> x in set view.
     * @param variables observed variables
     */
    protected IntsSetView(String name, int v, int offset, I... variables) {
        super(name, variables);
        this.v = v;
        this.offset = offset;
        this.idm = new IIntDeltaMonitor[getNbObservedVariables()];
        for (int i = 0; i < getNbObservedVariables(); i++) {
            this.idm[i] = getVariables()[i].monitorDelta(this);
        }
        this.valRemoved = i -> {
            if (i == this.v) {
                notifyPropagators(SetEventType.REMOVE_FROM_ENVELOPE, this);
            }
        };
        lb = new IntsSetViewLB(this);
        ub = new IntsSetViewUB(this);
    }

    /**
     * Instantiate an set view over an array of integer variables such that:
     * intVariables[x - offset] = c <=> x in set
     *
     * @param v integer that "toggle" integer variables index inclusion in the set view
     * @param offset offset between integer variables indices and set elements.
     * @param variables observed variables
     */
    public IntsSetView(int v, int offset, I... variables) {
        this("INTS_SET_VIEW["
                    + String.join(",", Arrays.stream(variables)
                        .map(i -> i.getName())
                        .toArray(String[]::new))
                    + "]",
                v, offset, variables);
    }

    @Override
    protected boolean doRemoveSetElement(int element) throws ContradictionException {
        if (!getVariables()[element - this.offset].contains(this.v)) {
            return false;
        }
        return getVariables()[element - this.offset].removeValue(this.v, this);
    }

    @Override
    protected boolean doForceSetElement(int element) throws ContradictionException {
        if (getVariables()[element - this.offset].isInstantiatedTo(this.v)) {
            return false;
        }
        return getVariables()[element - this.offset].instantiateTo(this.v, this);
    }

    @Override
    public void notify(IEventType event, int variableIdx) throws ContradictionException {
        if (this.getVariables()[variableIdx].isInstantiatedTo(this.v)) {
            notifyPropagators(SetEventType.ADD_TO_KER, this);
        } else {
            this.idm[variableIdx].forEachRemVal(this.valRemoved);
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
            I var = getVariables()[i];
            if (s.contains(i)) {
                var.instantiateTo(this.v, this);
            } else {
                var.removeValue(this.v, this);
            }
        }
        return changed;
    }

    @Override
    public boolean isInstantiated() {
        for (I var : getVariables()) {
            if (!var.isInstantiated() && var.contains(this.v)) {
                return false;
            }
        }
        return true;
    }

    private class IntsSetViewLB extends IntsSetViewBound {

        public IntsSetViewLB(IntsSetView ref) {
            super(ref);
        }

        @Override
        public boolean contains(int element) {
            if (element < ref.offset || element >= vars.length + ref.offset) {
                return false;
            }
            return vars[element - ref.offset].isInstantiatedTo(ref.v);
        }
    }

    private class IntsSetViewUB extends IntsSetViewBound {

        public IntsSetViewUB(IntsSetView ref) {
            super(ref);
        }

        @Override
        public boolean contains(int element) {
            if (element < ref.offset || element >= vars.length + ref.offset) {
                return false;
            }
            return vars[element - ref.offset].contains(ref.v);
        }
    }

    private abstract class IntsSetViewBound extends SetDynamicFilter {

        protected IntsSetView ref;
        protected I[] vars;

        public IntsSetViewBound(IntsSetView ref) {
            this.ref = ref;
            this.vars = (I[]) ref.getVariables();
        }

        @Override
        public SetDynamicFilterIterator createIterator() {
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
