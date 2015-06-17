/**
 * Copyright (c) 2014,
 *       Charles Prud'homme (TASC, INRIA Rennes, LINA CNRS UMR 6241),
 *       Jean-Guillaume Fages (COSLING S.A.S.).
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
    IntVar octet;
    BoolVar[] bits;
    private final int SIZE, MAX;
    private IStateInt KNOW_BIT;


    public PropBitChanneling(IntVar OCTET, BoolVar[] BITS) {
        super(ArrayUtils.append(new IntVar[]{OCTET}, BITS), PropagatorPriority.LINEAR, true);
        this.octet = OCTET;
        this.bits = BITS;
        this.SIZE = BITS.length;
        this.MAX = (int) Math.pow(2, SIZE) - 1;
        this.KNOW_BIT = OCTET.getSolver().getEnvironment().makeInt();
    }


    @Override
    public int getPropagationConditions(int vIdx) {
        if (vIdx == 0) { // OCTET
            return IntEventType.INSTANTIATE.getMask();
        } else { // BITS
            return IntEventType.INSTANTIATE.getMask();
        }
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (octet.isInstantiated()) {
            setPassive();
            int value = octet.getValue();
            for (int i = 0; i < SIZE; i++) {
                bits[i].instantiateTo(((value & (1 << i)) != 0) ? 1 : 0, this);
            }
            return;
        }
        octet.updateLowerBound(0, this);
        octet.updateUpperBound(MAX, this);
        int kb = 0;
        for (int i = 0; i < SIZE; i++) {
            if (bits[i].isInstantiated()) {
                kb++;
            }
        }
        if (kb == SIZE) {
            setPassive();
            octet.instantiateTo(getValueFromBits(), this);
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
            setPassive();
            int value = octet.getValue();
            for (int i = 0; i < SIZE; i++) {
                bits[i].instantiateTo(((value & (1 << i)) != 0) ? 1 : 0, this);
            }
        } else {
            if (KNOW_BIT.add(1) == SIZE) {
                setPassive();
                octet.instantiateTo(getValueFromBits(), this);
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
        if (isCompletelyInstantiated()) {
            return ESat.eval(octet.getValue() == getValueFromBits());
        }
        return ESat.UNDEFINED;
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
