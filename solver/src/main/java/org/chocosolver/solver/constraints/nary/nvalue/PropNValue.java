/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.nvalue;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.hash.TIntHashSet;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.solver.variables.events.PropagatorEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.ISetIterator;
import org.chocosolver.util.objects.setDataStructures.SetFactory;
import org.chocosolver.util.objects.setDataStructures.SetType;
import org.chocosolver.util.tools.ArrayUtils;

import java.util.Random;

/**
 * @author Arthur Godet <arth.godet@gmail.com>
 * @since 13/03/2020
 */
public class PropNValue extends Propagator<IntVar> {
    private static final int NO_WITNESS = -1;

    private final IntVar nValue;
    private final int n;
    private final int[] concernedValues;
    private final int[] witness;
    private final ISet mandatoryValues;
    private final ISet possibleValues;
    private final TIntArrayList listForRandomPick;
    private final Random rnd = new Random(vars[0].getModel().getSeed());

    public PropNValue(IntVar[] vars, IntVar nvalue) {
        super(ArrayUtils.concat(vars, nvalue), PropagatorPriority.LINEAR, true);
        this.nValue = nvalue;
        n = vars.length;
        TIntHashSet set = new TIntHashSet();
        int min = Integer.MAX_VALUE;
        for(int i = 0; i<vars.length; i++) {
            for(int value = vars[i].getLB(); value <= vars[i].getUB(); value = vars[i].nextValue(value)) {
                set.add(value);
                min = Math.min(min, value);
            }
        }
        concernedValues = set.toArray();
        possibleValues = SetFactory.makeStoredSet(SetType.BITSET, min, model);
        mandatoryValues = SetFactory.makeStoredSet(SetType.BITSET, min, model);
        listForRandomPick = new TIntArrayList();
        witness = new int[concernedValues.length];
        for(int j = 0; j<witness.length; j++) {
            possibleValues.add(concernedValues[j]);
            witness[j] = NO_WITNESS;
            selectRandomWitness(j);
        }
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        if(vIdx < n) {
            return IntEventType.all();
        } else {
            return IntEventType.instantiation();
        }
    }

    private int contains(int v) {
        for(int i = 0; i < concernedValues.length; i++) {
            if(concernedValues[i] == v) {
                return i;
            }
        }
        return -1;
    }

    private void instantiateTo(int idxVar, int value) {
        int idxConcernedValue = contains(value);
        mandatoryValues.add(vars[idxVar].getValue());
        witness[idxConcernedValue] = idxVar;
        for(int i = 0; i < concernedValues.length; i++) {
            if(witness[i] == idxVar && concernedValues[i] != value) {
                selectRandomWitness(i);
            }
        }
    }

    private void selectRandomWitness(int idxConcernedValue) {
        int value = concernedValues[idxConcernedValue];
        listForRandomPick.clear();
        for(int i = 0; i < n; i++) {
            if(vars[i].isInstantiatedTo(value)) {
                mandatoryValues.add(value);
                witness[idxConcernedValue] = i;
                return;
            } else if(vars[i].contains(value)) {
                listForRandomPick.add(i);
            }
        }
        if(listForRandomPick.size() == 0) {
            possibleValues.remove(value);
            witness[idxConcernedValue] = NO_WITNESS;
        } else {
            witness[idxConcernedValue] = listForRandomPick.getQuick(rnd.nextInt(listForRandomPick.size()));
        }
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        if(idxVarInProp < n) {
            for(int j = 0; j < concernedValues.length; j++) {
                if(witness[j] == idxVarInProp && !vars[idxVarInProp].contains(concernedValues[j])) {
                    selectRandomWitness(j);
                }
            }
            if(vars[idxVarInProp].isInstantiated()) {
                instantiateTo(idxVarInProp, vars[idxVarInProp].getValue());
            }
        }
        forcePropagate(PropagatorEventType.CUSTOM_PROPAGATION);
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if(PropagatorEventType.isFullPropagation(evtmask)) {
            nValue.updateUpperBound(Math.min(vars.length-1, concernedValues.length), this);
            for(int j = 0; j < witness.length; j++) {
                selectRandomWitness(j);
            }
        } else if(PropagatorEventType.isCustomPropagation(evtmask)) {
            ISetIterator iterator = possibleValues.iterator();
            while(iterator.hasNext()) {
                int value = iterator.nextInt();
                int idxConcernedValue = contains(value);
                if(
                    witness[idxConcernedValue] == NO_WITNESS
                        || !vars[witness[idxConcernedValue]].contains(concernedValues[idxConcernedValue])
                ) { // because witness not backtrackable
                    selectRandomWitness(idxConcernedValue);
                }
            }
        }
        nValue.updateBounds(mandatoryValues.size(), possibleValues.size(), this);
        if(nValue.isInstantiatedTo(mandatoryValues.size())) {
            for(int i = 0; i < n; i++) {
                for(int value = vars[i].getLB(); value <= vars[i].getUB(); value = vars[i].nextValue(value)) {
                    if(!mandatoryValues.contains(value)) {
                        vars[i].removeValue(value, this);
                    }
                }
            }
            setPassive();
        } else if(nValue.isInstantiatedTo(n)) {
            for(int i = 0; i < witness.length; i++) {
                if(witness[i] != NO_WITNESS && vars[witness[i]].isInstantiatedTo(concernedValues[i])) {
                    for(int j = 0; j < n; j++) {
                        if(j != witness[i]) {
                            vars[j].removeValue(concernedValues[i], this);
                            if(vars[j].isInstantiated()) {
                                instantiateTo(j, vars[j].getValue());
                            }
                        }
                    }
                }
            }
            nValue.updateBounds(mandatoryValues.size(), possibleValues.size(), this);
        }
    }

    @Override
    public ESat isEntailed() {
        int countMin = 0;
        int countMax = 0;
        for (int i = 0; i < concernedValues.length; i++) {
            boolean possible = false;
            boolean mandatory = false;
            for (int v = 0; v < n; v++) {
                if (vars[v].contains(concernedValues[i])) {
                    possible = true;
                    if (vars[v].isInstantiated()) {
                        mandatory = true;
                        break;
                    }
                }
            }
            if (possible) {
                countMax++;
            }
            if (mandatory) {
                countMin++;
            }
        }
        if (countMin > vars[n].getUB() || countMax < vars[n].getLB()) {
            return ESat.FALSE;
        } else if (isCompletelyInstantiated() && countMin == nValue.getValue()) {
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }
}
