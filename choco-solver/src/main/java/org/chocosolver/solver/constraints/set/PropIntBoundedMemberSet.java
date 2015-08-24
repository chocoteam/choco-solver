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
/**
 * Created by IntelliJ IDEA.
 * User: Jean-Guillaume Fages
 * Date: 14/01/13
 * Time: 16:36
 */

package org.chocosolver.solver.constraints.set;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.solver.variables.events.SetEventType;
import org.chocosolver.util.ESat;

/**
 * Propagator for Member constraint: iv is in set
 *
 * @author Jean-Guillaume Fages
 */
public class PropIntBoundedMemberSet extends Propagator<Variable> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private IntVar iv;
    private SetVar set;
    private int watchLit1, watchLit2;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * Propagator for Member constraint
     * val(intVar) is in setVar
     *
     * @param setVar a set variable
     * @param intVar an integer variable
     */
    public PropIntBoundedMemberSet(SetVar setVar, IntVar intVar) {
        super(new Variable[]{setVar, intVar}, PropagatorPriority.BINARY, true);
        assert !intVar.hasEnumeratedDomain();
        this.set = (SetVar) vars[0];
        this.iv = (IntVar) vars[1];
        watchLit1 = iv.getLB();
        watchLit2 = iv.nextValue(watchLit1);
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public int getPropagationConditions(int vIdx) {
        if (vIdx == 0) {
            return SetEventType.REMOVE_FROM_ENVELOPE.getMask();
        } else {
            return IntEventType.boundAndInst();
        }
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (iv.isInstantiated()) {
            set.addToKernel(iv.getValue(), aCause);
            setPassive();
            return;
        }
        int maxVal = set.getEnvelopeFirst();
        int minVal = maxVal;
        for (int j = maxVal; j != SetVar.END; j = set.getEnvelopeNext()) {
            maxVal = j;
        }
        iv.updateUpperBound(maxVal, aCause);
        iv.updateLowerBound(minVal, aCause);
        minVal = iv.getLB();
        maxVal = iv.getUB();
        while (minVal <= maxVal && !set.envelopeContains(minVal)) {
            iv.updateLowerBound(++minVal, this);
        }
        while (minVal <= maxVal && !set.envelopeContains(maxVal)) {
            iv.updateUpperBound(--maxVal, this);
        }
        if (iv.isInstantiated()) {
            set.addToKernel(iv.getValue(), aCause);
            setPassive();
            return;
        }
        // search for watch literals
        int i = set.getEnvelopeFirst(), wl = 0, cnt = 0;
        while (i != SetVar.END && wl < 2) {
            if (!iv.contains(i)) {
                cnt++;
            } else {
                watchLit2 = watchLit1;
                watchLit1 = i;
                wl++;
            }
            i = set.getEnvelopeNext();
        }
        if (cnt == set.getEnvelopeSize()) {
            this.contradiction(iv, "Inconsistent");
        } else if (cnt == set.getEnvelopeSize() - 1) {
            setWatchLiteral(watchLit1);
        }
    }

    @Override
    public void propagate(int i, int mask) throws ContradictionException {
        if (i == 1) {
            if (iv.isInstantiated()) {
                set.addToKernel(iv.getValue(), aCause);
                setPassive();
            } else if (!iv.contains(watchLit1)) {
                setWatchLiteral(watchLit2);
            } else if (!iv.contains(watchLit2)) {
                setWatchLiteral(watchLit1);
            }
        } else {
            if (!set.envelopeContains(watchLit1)) {
                setWatchLiteral(watchLit2);
            } else if (!set.envelopeContains(watchLit2)) {
                setWatchLiteral(watchLit1);
            }
        }
    }

    /**
     * Search a watchLiteral. A watchLiteral (or wL) is pointing out one variable not yet instantiated.
     * If every variables are instantiated, get out.
     * Otherwise, set the new not yet instantiated wL.
     *
     * @param otherWL previous known wL
     * @throws ContradictionException if a contradiction occurs
     */
    private void setWatchLiteral(int otherWL) throws ContradictionException {
        int i = set.getEnvelopeFirst();
        int cnt = 0;
        while (i != SetVar.END) {
            if (!iv.contains(i)) {
                cnt++;
            } else if (i != otherWL) {
                watchLit1 = i;
                watchLit2 = otherWL;
                return;
            }
            i = set.getEnvelopeNext();
        }
        if (cnt == set.getEnvelopeSize()) {
            this.contradiction(iv, "Inconsistent");
        }
        set.addToKernel(otherWL, aCause);
        iv.instantiateTo(otherWL, aCause);
        setPassive();
    }

    @Override
    public ESat isEntailed() {
        if (iv.isInstantiated()) {
            if (!set.envelopeContains(iv.getValue())) {
                return ESat.FALSE;
            } else {
                if (set.kernelContains(iv.getValue())) {
                    return ESat.TRUE;
                } else {
                    return ESat.UNDEFINED;
                }
            }
        } else {
            int lb = iv.getLB();
            int ub = iv.getUB();
            boolean all = true;
            for (int i = lb; i <= ub; i++) {
                if (!set.kernelContains(i)) {
                    all = false;
                    break;
                }
            }
            if (all) {
                return ESat.TRUE;
            }
            for (int i = lb; i <= ub; i++) {
                if (set.envelopeContains(i)) {
                    return ESat.UNDEFINED;
                }
            }
            return ESat.FALSE;
        }
    }

}
