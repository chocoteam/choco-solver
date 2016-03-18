/**
 * Copyright (c) 2015, Ecole des Mines de Nantes
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    This product includes software developed by the <organization>.
 * 4. Neither the name of the <organization> nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.solver.constraints.extension.binary;

import org.chocosolver.solver.constraints.extension.Tuples;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.explanations.RuleStore;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.solver.variables.events.PropagatorEventType;
import org.chocosolver.util.iterators.DisposableValueIterator;

/**
 * AC3 bit rm algorithm for binary table constraint
 * <br/>
 *
 * @author Charles Prud'homme, Hadrien Cambazard
 * @since 22/04/2014
 */
public class PropBinAC3bitrm extends PropBinCSP {

    protected int offset0;
    protected int offset1;

    protected int minS0;    //value with minimum number of supports for v0
    protected int minS1;    //value with minimum number of supports for v1

    protected int initDomSize0;
    protected int initDomSize1;

    public PropBinAC3bitrm(IntVar x, IntVar y, Tuples tuples) {
        this(x, y, new CouplesBitSetTable(tuples, x, y));
        offset0 = v0.getLB();
        offset1 = v1.getLB();

        initDomSize0 = v0.getUB() - offset0 + 1;
        initDomSize1 = v1.getUB() - offset1 + 1;
        assert v0.hasEnumeratedDomain() && v1.hasEnumeratedDomain() : "PropBinAC3bitrm may produce incorrect filtering with bounded variables";
    }

    private PropBinAC3bitrm(IntVar x, IntVar y, CouplesBitSetTable table) {
        super(x, y, table);
        offset0 = v0.getLB();
        offset1 = v1.getLB();

        initDomSize0 = v0.getUB() - offset0 + 1;
        initDomSize1 = v1.getUB() - offset1 + 1;
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (PropagatorEventType.isFullPropagation(evtmask)) {
            fastInitNbSupports();
            DisposableValueIterator itv0 = v0.getValueIterator(true);
            try {
                while (itv0.hasNext()) {
                    int val0 = itv0.next();
                    if (((CouplesBitSetTable) relation).checkUnsupportedValue(0, val0, v1)) {
                        v0.removeValue(val0, this);
                    }
                }
            } finally {
                itv0.dispose();
            }
            itv0 = v1.getValueIterator(true);
            try {
                while (itv0.hasNext()) {
                    int val1 = itv0.next();
                    if (((CouplesBitSetTable) relation).checkUnsupportedValue(1, val1, v0)) {
                        v1.removeValue(val1, this);
                    }
                }
            } finally {
                itv0.dispose();
            }
        }
        reviseV0();
        reviseV1();
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        if (IntEventType.isInstantiate(mask)) {
            onInstantiationOf(idxVarInProp);
        } else if (idxVarInProp == 0) {
            reviseV1();
        } else {
            reviseV0();
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    private void fastInitNbSupports() {
        int[] initS1 = new int[initDomSize1];
        minS0 = Integer.MAX_VALUE;
        minS1 = Integer.MAX_VALUE;
        DisposableValueIterator itv0 = v0.getValueIterator(true);
        while (itv0.hasNext()) {
            int val0 = itv0.next();
            int initS0 = 0;
            DisposableValueIterator itv1 = v1.getValueIterator(true);
            while (itv1.hasNext()) {
                int val1 = itv1.next();
                if (relation.isConsistent(val0, val1)) {
                    initS0++;
                    initS1[val1 - offset1]++;
                }
            }
            if (initS0 < minS0) minS0 = initS0;
            itv1.dispose();
        }
        itv0.dispose();
        for (int i = 0; i < initS1.length; i++) {
            if (initS1[i] < minS1) minS1 = initS1[i];
        }
    }


    /**
     * updates the support for all values in the domain of v1, and remove unsupported values for v1
     */
    private void reviseV1() throws ContradictionException {
        int v0Size = v0.getDomainSize();
        if (minS1 <= (initDomSize0 - v0Size)) {
            DisposableValueIterator itv1 = v1.getValueIterator(true);
            try {
                while (itv1.hasNext()) {
                    int y = itv1.next();
                    if (((CouplesBitSetTable) relation).checkUnsupportedValue(1, y, v0)) {
                        v1.removeValue(y, this);
                    }
                }
            } finally {
                itv1.dispose();
            }
        }
    }

    /**
     * updates the support for all values in the domain of v0, and remove unsupported values for v0
     */
    private void reviseV0() throws ContradictionException {
        int v1Size = v1.getDomainSize();
        if (minS0 <= (initDomSize1 - v1Size)) {
            DisposableValueIterator itv0 = v0.getValueIterator(true);
            try {
                while (itv0.hasNext()) {
                    int x = itv0.next();
                    if (((CouplesBitSetTable) relation).checkUnsupportedValue(0, x, v1)) {
                        v0.removeValue(x, this);
                    }
                }
            } finally {
                itv0.dispose();
            }
        }
    }


    private void onInstantiationOf(int idx) throws ContradictionException {
        if (idx == 0) {
            int value = v0.getValue();
            DisposableValueIterator itv1 = v1.getValueIterator(true);
            try {
                while (itv1.hasNext()) {
                    int val = itv1.next();
                    if (!relation.isConsistent(value, val)) {
                        v1.removeValue(val, this);
                    }
                }
            } finally {
                itv1.dispose();
            }
        } else {
            int value = v1.getValue();
            DisposableValueIterator itv0 = v0.getValueIterator(true);
            try {
                while (itv0.hasNext()) {
                    int val = itv0.next();
                    if (!relation.isConsistent(val, value)) {
                        v0.removeValue(val, this);
                    }
                }
            } finally {
                itv0.dispose();
            }
        }
    }

    @Override
    public boolean why(RuleStore ruleStore, IntVar var, IEventType evt, int value) {
        boolean nrules = ruleStore.addPropagatorActivationRule(this);
        if (var == v0) {
            for (int i = 0; i < initDomSize1; i++) {
                if (relation.checkCouple(value, i + offset1)) {
                    nrules |= ruleStore.addRemovalRule(v1, i + offset1);
                }
            }
        } else {
            for (int i = 0; i < initDomSize0; i++) {
                if (relation.checkCouple(i + offset0, value)) {
                    nrules |= ruleStore.addRemovalRule(v0, i + offset0);
                }
            }
        }
        return nrules;
    }
}
