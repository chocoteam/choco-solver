/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.span;

import org.chocosolver.memory.IStateInt;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.OptionalTask;
import org.chocosolver.solver.variables.Task;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.solver.variables.events.PropagatorEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.ISetIterator;
import org.chocosolver.util.objects.setDataStructures.bitset.Set_Std_BitSet;

/**
 * Span constraint such that span(span, tasks) is true iff <ul>
 *     <li>either span is not performed and none of tasks are performed</li>
 *     <li>either span is performed and all performed tasks are temporally spanned by span {@code span.getEst() <= tasks[i].getEst() <= tasks[i].getLct() <= span.getLct()},
 *         and span starts when the earliest performed task starts, and span ends when the latest performed task ends.</li>
 * </ul>
 *
 * @author Arthur Godet <arth.godet@gmail.com>
 * @since 02/01/2024
 */
public class PropSpan extends Propagator<IntVar> {
    private final Task span;
    private final Task[] tasks;
    private final IntVar varN;
    private final ISet mandatories;
    private final ISet candidates;
    private final IStateInt watcherInf;
    private final IStateInt watcherSup;

    public PropSpan(Task span, Task[] tasks, IntVar varN) {
        super(extractVars(span, tasks, varN), PropagatorPriority.LINEAR, true);
        this.span = span;
        this.tasks = tasks;
        this.varN = varN;
        this.candidates = new Set_Std_BitSet(model.getEnvironment(), 0);
        this.mandatories = new Set_Std_BitSet(model.getEnvironment(), 0);
        this.watcherInf = model.getEnvironment().makeInt(0);
        this.watcherSup = model.getEnvironment().makeInt(0);
        for (int i = 0; i < tasks.length; i++) {
            if (tasks[i].mayBePerformed()) {
                this.candidates.add(i);
            }
        }
    }

    private static IntVar[] extractVars(Task span, Task[] tasks, IntVar varN) {
        IntVar[] array = new IntVar[(tasks.length + 1) * 3 + 1];
        int idx = 0;
        final BoolVar present = span.getStart().getModel().boolVar(true);
        for (int i = 0; i < tasks.length; i++) {
            array[idx++] = tasks[i].getStart();
            array[idx++] = tasks[i].getEnd();
            if (tasks[i] instanceof OptionalTask optionalTask) {
                array[idx++] = optionalTask.getPerformed();
            } else {
                array[idx++] = present;
            }
        }
        array[idx++] = span.getStart();
        array[idx++] = span.getEnd();
        if (span instanceof OptionalTask optionalSpan) {
            array[idx++] = optionalSpan.getPerformed();
        } else {
            array[idx++] = present;
        }
        array[idx] = varN;
        return array;
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        if (vIdx % 3 == 0) { // start variables
            return IntEventType.lowerBoundAndInst();
        } else if (vIdx % 3 == 1) { // end variables
            return IntEventType.upperBoundAndInst();
        } else { // performed variables
            return IntEventType.instantiation();
        }
    }

    private void updateSpan() throws ContradictionException {
        if (span.mayBePerformed()) {
            ISetIterator iter = candidates.iterator();
            while (iter.hasNext()) {
                int idx = iter.nextInt();
                if (tasks[idx].mayBePerformed()) {
                    if (tasks[idx].mustBePerformed()) {
                        mandatories.add(idx);
                        span.forceToBePerformed(this);
                    }
                    updateWatcherInf(idx);
                    updateWatcherSup(idx);
                } else {
                    candidates.remove(idx);
                }
            }
            updateSpanBounds();
        }
        if (!span.mayBePerformed()) {
            updateTasks();
        }
    }

    private boolean updateWatcherInf(int idx) {
        if (
            !tasks[watcherInf.get()].mayBePerformed()
            || tasks[idx].getEst() < tasks[watcherInf.get()].getEst()
            || tasks[idx].getEst() == tasks[watcherInf.get()].getEst()
               && !tasks[watcherInf.get()].isStartInstantiated()
               && tasks[idx].isStartInstantiated()
        ) {
            watcherInf.set(idx);
            return true;
        }
        return false;
    }

    private boolean updateWatcherSup(int idx) {
        if (
            !tasks[watcherSup.get()].mayBePerformed()
            || tasks[idx].getLct() > tasks[watcherSup.get()].getLct()
            || tasks[idx].getLct() == tasks[watcherSup.get()].getLct()
               && !tasks[watcherSup.get()].isEndInstantiated()
               && tasks[idx].isEndInstantiated()
        ) {
            watcherSup.set(idx);
            return true;
        }
        return false;
    }

    private void updateN() throws ContradictionException {
        varN.updateBounds(mandatories.size(), candidates.size(), this);
        if (mandatories.size() == varN.getUB()) {
            ISetIterator iter = candidates.iterator();
            while (iter.hasNext()) {
                int idx = iter.nextInt();
                if (!mandatories.contains(idx)) {
                    tasks[idx].forceToBeUnperformed(this);
                    candidates.remove(idx);
                }
            }
            varN.instantiateTo(mandatories.size(), this);
            span.forceToBe(!mandatories.isEmpty(), this);
        }
        if (candidates.size() == varN.getLB()) {
            ISetIterator iter = candidates.iterator();
            while (iter.hasNext()) {
                tasks[iter.nextInt()].forceToBePerformed(this);
            }
            varN.instantiateTo(candidates.size(), this);
            span.forceToBe(!candidates.isEmpty(), this);
        }
    }

    private void updateSpanBounds() throws ContradictionException {
        int inf = watcherInf.get();
        int est = tasks[inf].getEst();
        if (tasks[inf].mustBePerformed() && tasks[inf].isStartInstantiated()) {
            span.instantiateStartAt(est, this);
        } else {
            span.updateEst(est, this);
        }
        int sup = watcherSup.get();
        int lct = tasks[sup].getLct();
        if (tasks[sup].mustBePerformed() && tasks[sup].isEndInstantiated()) {
            span.instantiateEndAt(lct, this);
        } else {
            span.updateLct(lct, this);
        }
    }

    private void updateTasks() throws ContradictionException {
        ISetIterator iter = candidates.iterator();
        if (span.mayBePerformed()) {
            boolean shouldUpdateSpan = false;
            while (iter.hasNext()) {
                shouldUpdateSpan |= updateTask(iter.nextInt());
            }
            if (candidates.isEmpty()) {
                span.forceToBeUnperformed(this);
                varN.instantiateTo(0, this);
            } else if (shouldUpdateSpan) {
                updateN();
                updateSpan();
            }
        } else {
            while (iter.hasNext()) {
                int idx = iter.nextInt();
                tasks[idx].forceToBeUnperformed(this);
            }
            candidates.clear();
            if (!isPassive()) { // updateSpan() may call this method again once span is not performed
                setPassive();
            }
        }
    }

    private boolean updateTask(int idx) throws ContradictionException{
        tasks[idx].updateEst(span.getEst(), this);
        tasks[idx].updateLct(span.getLct(), this);
        if (!tasks[idx].mayBePerformed()) {
            candidates.remove(idx);
            return idx == watcherInf.get() || idx == watcherSup.get();
        } else if (tasks[idx].mustBePerformed()) {
            mandatories.add(idx);
        }
        return false;
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        int n = idxVarInProp / 3;
        if (n == tasks.length) { // span
            if (idxVarInProp % 3 == 2) { // performed variable
                if (!span.mayBePerformed()) {
                    updateTasks();
                    varN.instantiateTo(0, this);
                }
            } else {
                forcePropagate(PropagatorEventType.CUSTOM_PROPAGATION);
            }
            return;
        }
        if (idxVarInProp == 3 * (tasks.length + 1)) { // varN
            forcePropagate(PropagatorEventType.CUSTOM_PROPAGATION);
            return;
        }
        boolean prop = false;
        if (idxVarInProp % 3 == 0) { // start variables
            prop = n == watcherInf.get() || updateWatcherInf(n);
        } else if (idxVarInProp % 3 == 1) { // end variables
            prop = n == watcherSup.get() || updateWatcherSup(n);
        } else { // performed variables
            if (!tasks[n].mayBePerformed()) {
                candidates.remove(n);
                prop = n == watcherInf.get() || n == watcherSup.get() || shouldPropagateVarN();
            } else {
                mandatories.add(n);
                prop = shouldPropagateVarN();
            }
        }
        if (prop) {
            forcePropagate(PropagatorEventType.CUSTOM_PROPAGATION);
        }
    }

    private boolean shouldPropagateVarN() {
        return mandatories.size() >= varN.getUB() || candidates.size() <= varN.getLB();
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        updateTasks();
        updateN();
        updateSpan();
    }

    @Override
    public ESat isEntailed() {
        if (!span.isInstantiated() || !varN.isInstantiated()) {
            return ESat.UNDEFINED;
        }
        boolean performed = span.mayBePerformed();
        boolean perfStart = false;
        boolean perfEnd = false;
        int nbPerformed = 0;
        for (int i = 0; i < tasks.length; ++i) {
            if (tasks[i].mayBePerformed()) {
                if (!performed) {
                    return ESat.FALSE;
                }
                boolean isSpanningOver = span.getEst() <= tasks[i].getEst() && tasks[i].getLct() <= span.getLct();
                if (!isSpanningOver) {
                    return ESat.FALSE;
                }
                if (tasks[i].mustBePerformed()) {
                    nbPerformed++;
                    perfStart |= tasks[i].isStartInstantiatedTo(span.getEst());
                    perfEnd |= tasks[i].isEndInstantiatedTo(span.getLct());
                }
            }
        }
        return ESat.eval(!performed && varN.isInstantiatedTo(0) || perfStart && perfEnd && nbPerformed == varN.getValue());
    }
}
