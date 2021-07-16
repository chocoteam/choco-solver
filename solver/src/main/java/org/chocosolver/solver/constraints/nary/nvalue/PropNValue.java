/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
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
import org.chocosolver.util.objects.setDataStructures.SetFactory;
import org.chocosolver.util.objects.setDataStructures.SetType;
import org.chocosolver.util.tools.ArrayUtils;

import java.util.Arrays;
import java.util.Random;

/**
 * @author Arthur Godet <arth.godet@gmail.com>
 * @since 13/03/2020
*/
public class PropNValue extends Propagator<IntVar> {
    private IntVar nValue;
    private final int n;
    private int[] concernedValues;
    private int[] witness;
    private ISet mandatoryValues;
    private ISet possibleValues;
    private TIntArrayList listForRandomPick;
    private Random rnd = new Random(vars[0].getModel().getSeed());

    public PropNValue(IntVar[] vars, IntVar nvalue) {
        super(ArrayUtils.concat(vars, nvalue), PropagatorPriority.LINEAR, false);
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
        witness = new int[concernedValues.length];
        Arrays.fill(witness, -1);
        listForRandomPick = new TIntArrayList();
        for(int j = 0; j<witness.length; j++) {
            possibleValues.add(concernedValues[j]);
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

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        if(idxVarInProp < n) {
            for(int j = 0; j<concernedValues.length; j++) {
                if(witness[j] == idxVarInProp && !vars[idxVarInProp].contains(concernedValues[j])) {
                    selectRandomWitness(j);
                }
            }
            if(vars[idxVarInProp].isInstantiated()) {
                mandatoryValues.add(vars[idxVarInProp].getValue());
            }
        }
        forcePropagate(PropagatorEventType.CUSTOM_PROPAGATION);
    }

    private void selectRandomWitness(int idxConcernedValue) {
        int value = concernedValues[idxConcernedValue];
        listForRandomPick.clear();
        for(int i = 0; i<n; i++) {
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
        } else {
            witness[idxConcernedValue] = listForRandomPick.getQuick(rnd.nextInt(listForRandomPick.size()));
        }
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if(PropagatorEventType.isFullPropagation(evtmask)) {
            nValue.updateUpperBound(Math.max(vars.length-1, concernedValues.length), this);
            for(int j = 0; j<witness.length; j++) {
                selectRandomWitness(j);
            }
        }
        nValue.updateBounds(mandatoryValues.size(), possibleValues.size(), this);
        if(nValue.isInstantiated() && mandatoryValues.size() == nValue.getValue()) {
            for(int i = 0; i<n; i++) {
                for(int value = vars[i].getLB(); value <= vars[i].getUB(); value = vars[i].nextValue(value)) {
                    if(!mandatoryValues.contains(value)) {
                        vars[i].removeValue(value, this);
                    }
                }
            }
            setPassive();
        } else if(nValue.isInstantiated() && mandatoryValues.size() > nValue.getValue()) {
            fails();
        } else if(nValue.isInstantiated() && nValue.getValue() == concernedValues.length) {
            boolean hasFiltered;
            do {
                hasFiltered = false;
                for(int i = 0; i < n; i++) {
                    if(vars[i].isInstantiated()) {
                        for(int j = 0; j < n; j++) {
                            if(i != j) {
                                hasFiltered |= vars[j].removeValue(vars[i].getValue(), this);
                            }
                        }
                    }
                }
            } while(hasFiltered);
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
