/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2018, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.channeling;

import org.chocosolver.memory.IStateInt;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.tools.ArrayUtils;

/**
 * A propagator which ensures that OCTET = 2<sup>0</sup>*BIT_1 + 2<sup>1</sup>*BIT_2 + ... 2<sup>n-1</sup>*BIT_n.
 * <br/>
 * BIT_1 is related to the first bit of OCTET (2^0),
 * BIT_2 is related to the first bit of OCTET (2^1), etc.
 * <br/>
 *
 * @author Charles Prud'homme
 * @version choco
 * @since 23/09/2014
 */
public class PropBitChanneling extends Propagator<IntVar> {
    private IntVar octet;
    private BoolVar[] bits;
    private final int SIZE, MAX;
    private IStateInt KNOW_BIT;


    public PropBitChanneling(IntVar OCTET, BoolVar[] BITS) {
        super(ArrayUtils.append(new IntVar[]{OCTET}, BITS), PropagatorPriority.LINEAR, true);
        this.octet = OCTET;
        this.bits = BITS;
        this.SIZE = BITS.length;
        this.MAX = (int) Math.pow(2, SIZE) - 1;
        this.KNOW_BIT = OCTET.getEnvironment().makeInt();
    }


    @Override
    public int getPropagationConditions(int vIdx) {
        return IntEventType.instantiation();
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (octet.isInstantiated()) {
            int value = octet.getValue();
            for (int i = 0; i < SIZE; i++) {
                bits[i].instantiateTo(((value & (1 << i)) != 0) ? 1 : 0, this);
            }
            setPassive();
            return;
        }
        octet.updateBounds(0, MAX, this);
        int kb = 0;
        for (int i = 0; i < SIZE; i++) {
            if (bits[i].isInstantiated()) {
                kb++;
            }
        }
        if (kb == SIZE) {
            octet.instantiateTo(getValueFromBits(), this);
            setPassive();
        } else {
            KNOW_BIT.set(kb);
            if (kb > 0) {
                for (int i = 0; i < SIZE; i++) {
                    if (bits[i].isInstantiated()) {
                        removeFromOctet(i);
                    }
                }
            }
        }
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        if (idxVarInProp == 0) {
            int value = octet.getValue();
            for (int i = 0; i < SIZE; i++) {
                bits[i].instantiateTo(((value & (1 << i)) != 0) ? 1 : 0, this);
            }
            setPassive();
        } else {
            if (KNOW_BIT.add(1) == SIZE) {
                octet.instantiateTo(getValueFromBits(), this);
                setPassive();
            } else {
                int i = idxVarInProp - 1;
                if (bits[i].isInstantiated()) {
                    removeFromOctet(i);
                }
            }
        }
    }

    /**
     * Remove all values from OCTET which match BITS_i
     *
     * @param bitidx index of the bit
     * @throws ContradictionException
     */
    private void removeFromOctet(int bitidx) throws ContradictionException {
        int bit = (1 << bitidx);
        int from = octet.getLB();
        int to = octet.getUB();
        if (bits[bitidx].isInstantiatedTo(1)) { //  the bit is present
            for (; from <= to; from = octet.nextValue(from)) {
                if ((from & bit) == 0) {
                    octet.removeValue(from, this);
                }
            }
        } else { // the bit is not present
            for (; from <= to; from = octet.nextValue(from)) {
                if ((from & bit) != 0) {
                    octet.removeValue(from, this);
                }
            }
        }
    }

    @Override
    public ESat isEntailed() {
        if(octet.getUB() < 0 || getValueFromBits() > octet.getUB() || getMaxValueFromBits() < octet.getLB()) {
            return ESat.FALSE;
        }
        if (isCompletelyInstantiated()) {
            return ESat.eval(octet.getValue() == getValueFromBits());
        }
        return ESat.UNDEFINED;
    }

    private int getMaxValueFromBits() {
        int word = 0;
        for (int i = 0; i < SIZE; i++) {
            if(bits[i].contains(1)) {
                word |= (1 << i);
            }
        }
        return word;
    }

    /**
     * Compute the value from BITS
     *
     * @return the value declared by BITS
     */
    private int getValueFromBits() {
        int word = 0;
        for (int i = 0; i < SIZE; i++) {
            if (bits[i].isInstantiatedTo(1)) {
                word |= (1 << i);
            }
        }
        return word;
    }
}
