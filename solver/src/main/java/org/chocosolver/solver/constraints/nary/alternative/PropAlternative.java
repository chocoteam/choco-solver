/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.alternative;

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
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableBitSet;

/**
 * Alternative constraint such that alternative(alter, tasks, varN) is true iff <ul>
 *     <li>either alter is not performed and none of tasks are performed and varN = 0</li>
 *     <li>either alter is performed and exactly varN elements of tasks are performed and all performed tasks have same start and end as alter</li>
 * </ul>
 *
 * @author Arthur Godet <arth.godet@gmail.com>
 * @since 31/12/2023
 */
public class PropAlternative extends Propagator<IntVar> {
    private final Task alter;
    private final Task[] tasks;
    private final IntVar varN;
    private final ISet mandatories;
    private final ISet candidates;
    private final int[] ests;
    private final int[] lcts;
    private final ISet watchersInf;
    private final ISet watchersSup;
    private final int[] idxEsts;
    private final int[] idxLcts;
    private final IntIterableBitSet durationDomain;
    private int sizeEsts;
    private int sizeLcts;
    private int minDuration;
    private int maxDuration;

    public PropAlternative(Task alter, Task[] tasks, IntVar varN) {
        super(extractVars(alter, tasks, varN), PropagatorPriority.LINEAR, true);
        this.alter = alter;
        this.tasks = tasks;
        this.varN = varN;
        this.candidates = new Set_Std_BitSet(model.getEnvironment(), 0);
        this.mandatories = new Set_Std_BitSet(model.getEnvironment(), 0);
        this.watchersInf = new Set_Std_BitSet(model.getEnvironment(), 0);
        this.watchersSup = new Set_Std_BitSet(model.getEnvironment(), 0);
        for (int i = 0; i < tasks.length; i++) {
            if (tasks[i].mayBePerformed()) {
                this.candidates.add(i);
            }
            if (tasks[i].mustBePerformed()) {
                this.mandatories.add(i);
            }
        }
        ests = new int[varN.getUB()];
        lcts = new int[varN.getUB()];
        idxEsts = new int[varN.getUB()];
        idxLcts = new int[varN.getUB()];
        durationDomain = new IntIterableBitSet();
    }

    private static IntVar[] extractVars(Task alter, Task[] tasks, IntVar varN) {
        IntVar[] array = new IntVar[(tasks.length + 1) * 3 + 1];
        final BoolVar present = varN.getModel().boolVar(true);
        int idx = 0;
        for (int i = 0; i < tasks.length; i++) {
            array[idx++] = tasks[i].getStart();
            array[idx++] = tasks[i].getEnd();
            if (tasks[i] instanceof OptionalTask optionalTask) {
                array[idx++] = optionalTask.getPerformed();
            } else {
                array[idx++] = present;
            }
        }
        array[idx++] = alter.getStart();
        array[idx++] = alter.getEnd();
        if (alter instanceof OptionalTask optionalTask) {
            array[idx++] = optionalTask.getPerformed();
        } else {
            array[idx++] = present;
        }
        array[idx] = varN;
        return array;
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        if (vIdx == 3 * (tasks.length + 1)) { // varN
            return IntEventType.boundAndInst();
        } else if (vIdx % 3 == 0) { // start variables
            return IntEventType.lowerBoundAndInst();
        } else if (vIdx % 3 == 1) { // end variables
            return IntEventType.upperBoundAndInst();
        } else { // performed variables
            return IntEventType.instantiation();
        }
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
            alter.forceToBe(!mandatories.isEmpty(), this);
        }
        if (candidates.size() == varN.getLB()) {
            ISetIterator iter = candidates.iterator();
            while (iter.hasNext()) {
                tasks[iter.nextInt()].forceToBePerformed(this);
            }
            varN.instantiateTo(candidates.size(), this);
            alter.forceToBe(!candidates.isEmpty(), this);
        }
    }

    private int updateEstsLcts(int taskIdx, int newValue, int[] array, int[] idxArray, int idx, boolean est) {
        if (idx < Math.max(varN.getLB(), 1)) {
            idxArray[idx] = taskIdx;
            array[idx++] = newValue;
        } else if (est && array[idx - 1] > newValue || !est && array[idx - 1] < newValue) {
            idxArray[idx - 1] = taskIdx;
            array[idx - 1] = newValue;
        }
        // Maintains the order of the array and idxArray, so that the last element is always the min (for est) or max (for lct)
        int i = idx - 1;
        if (est) {
            // the last element is always the min
            while (i > 0 && array[i] < array[i - 1]) {
                swap(array, i);
                swap(idxArray, i);
                i--;
            }
        } else {
            // the last element is always the max
            while (i > 0 && array[i] > array[i - 1]) {
                swap(array, i);
                swap(idxArray, i);
                i--;
            }
        }
        return idx;
    }

    private void swap(int[] array, int idx) {
        int tmp = array[idx - 1];
        array[idx - 1] = array[idx];
        array[idx] = tmp;
    }

    private void updateAlter() throws ContradictionException {
        if (alter.mayBePerformed()) {
            IntIterableBitSet domain = updateEstsLctsAndComputeDurationDomain();
            alter.updateEst(ests[sizeEsts - 1], this);
            alter.updateLct(lcts[sizeLcts - 1], this);
            updateDurationDomain(alter, domain, minDuration, maxDuration);
            if (!alter.mayBePerformed()) {
                updateTasks();
            } else {
                updateWatchers();
            }
        }
    }

    private IntIterableBitSet updateEstsLctsAndComputeDurationDomain() throws ContradictionException {
        sizeEsts = 0;
        sizeLcts = 0;
        ISetIterator iter = candidates.iterator();
        minDuration = Integer.MAX_VALUE;
        maxDuration = Integer.MIN_VALUE;
        durationDomain.clear();
        IntIterableBitSet domain = durationDomain;
        while (iter.hasNext()) {
            int idx = iter.nextInt();
            if (tasks[idx].mayBePerformed()) {
                if (tasks[idx].mustBePerformed()) {
                    alter.forceToBePerformed(this);
                    alter.updateEst(tasks[idx].getEst(), this);
                    alter.updateLct(tasks[idx].getLct(), this);
                    alter.updateDuration(tasks[idx].getMinDuration(), tasks[idx].getMaxDuration(), this);
                }
                sizeEsts = updateEstsLcts(idx, tasks[idx].getEst(), ests, idxEsts, sizeEsts, true);
                sizeLcts = updateEstsLcts(idx, tasks[idx].getLct(), lcts, idxLcts, sizeLcts, false);
                minDuration = Math.min(minDuration, tasks[idx].getMinDuration());
                maxDuration = Math.max(maxDuration, tasks[idx].getMaxDuration());
                if (tasks[idx].getDuration().hasEnumeratedDomain() && domain != null) {
                    for (int v = tasks[idx].getMinDuration(); v <= tasks[idx].getMaxDuration(); v = tasks[idx].getDuration().nextValue(v)) {
                        durationDomain.add(v);
                    }
                } else {
                    domain = null;
                }
            }
        }
        return domain;
    }

    private void updateWatchers() throws ContradictionException {
        watchersInf.clear();
        for (int i = 0; i < sizeEsts; i++) {
            tasks[idxEsts[i]].updateEst(ests[sizeEsts - 1], this);
            if (!tasks[idxEsts[i]].mayBePerformed()) {
                candidates.remove(idxEsts[i]);
            } else {
                watchersInf.add(idxEsts[i]);
            }
        }
        watchersSup.clear();
        for (int i = 0; i < sizeLcts; i++) {
            tasks[idxLcts[i]].updateLct(lcts[sizeLcts - 1], this);
            if (!tasks[idxLcts[i]].mayBePerformed()) {
                candidates.remove(idxLcts[i]);
            } else {
                watchersSup.add(idxLcts[i]);
            }
        }
    }

    private void updateTasks() throws ContradictionException {
        ISetIterator iter = candidates.iterator();
        iter.reset();
        if (alter.mayBePerformed()) {
            IntIterableBitSet alterDurationDomain = getAlterDurationDomain();
            while (iter.hasNext()) {
                updateTask(iter.nextInt(), alterDurationDomain);
            }
            if (candidates.isEmpty()) {
                alter.forceToBeUnperformed(this);
                varN.instantiateTo(0, this);
            } else if (shouldPropagateVarN()) {
                updateN();
                updateAlter();
            }
        } else {
            while (iter.hasNext()) {
                int idx = iter.nextInt();
                tasks[idx].forceToBeUnperformed(this);
            }
            candidates.clear();
            setPassive();
        }
    }

    private IntIterableBitSet getAlterDurationDomain() {
        IntIterableBitSet alterDurationDomain = null;
        if (alter.getDuration().hasEnumeratedDomain()) {
            durationDomain.clear();
            alterDurationDomain = durationDomain;
            for (int v = alter.getMinDuration(); v <= alter.getMaxDuration(); v = alter.getDuration().nextValue(v)) {
                alterDurationDomain.add(v);
            }
        }
        return alterDurationDomain;
    }

    private void updateTask(int idx, IntIterableBitSet alterDurationDomain) throws ContradictionException{
        if (!tasks[idx].mayBePerformed()) {
            candidates.remove(idx);
        } else {
            tasks[idx].updateEst(alter.getEst(), this);
            tasks[idx].updateLct(alter.getLct(), this);
            updateDurationDomain(tasks[idx], alterDurationDomain, alter.getMinDuration(), alter.getMaxDuration());
            updateStatusOfTask(idx);
        }
    }

    private void updateStatusOfTask(int idx) {
        if (tasks[idx].mustBePerformed()) {
            mandatories.add(idx);
        } else if (!tasks[idx].mayBePerformed()) {
            candidates.remove(idx);
        }
    }

    private void updateDurationDomain(Task task, IntIterableBitSet durationDomain, int minDuration, int maxDuration) throws ContradictionException {
        if (task.mayBePerformed() && task.getDuration().hasEnumeratedDomain() && durationDomain != null) {
            task.removeAllDurationsBut(durationDomain, this);
        } else {
            task.updateDuration(minDuration, maxDuration, this);
        }
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        int n = idxVarInProp / 3;
        if (n == tasks.length) { // alter
            if (idxVarInProp % 3 == 2) { // performed variable
                if (!alter.mayBePerformed()) {
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
        boolean prop;
        if (idxVarInProp % 3 == 0) { // start variables
            prop = watchersInf.contains(n);
        } else if (idxVarInProp % 3 == 1) { // end variables
            prop = watchersSup.contains(n);
        } else { // performed variables
            updateStatusOfTask(n);
            prop = watchersInf.contains(n) || watchersSup.contains(n) || shouldPropagateVarN();
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
        updateAlter();
    }

    @Override
    public ESat isEntailed() {
        if (varN.isInstantiated() && alter.isInstantiated()) {
            int nbPerformed = 0;
            for (int i = 0; i < tasks.length; i++) {
                if (!tasks[i].isInstantiated()) {
                    return ESat.UNDEFINED;
                }
                if (tasks[i].mustBePerformed()) {
                    nbPerformed++;
                    boolean isAlternate = alter.mustBePerformed() && alter.getEst() == tasks[i].getEst() && alter.getLct() == tasks[i].getLct();
                    if (!isAlternate) {
                        return ESat.FALSE;
                    }
                }
            }
            return ESat.eval(varN.getValue() == nbPerformed);
        }
        return ESat.UNDEFINED;
    }
}
