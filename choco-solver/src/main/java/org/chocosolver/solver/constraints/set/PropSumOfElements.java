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
 * Sums elements given by a set variable
 *
 * @author Jean-Guillaume Fages
 */
public class PropSumOfElements extends Propagator<Variable> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private IntVar sum;
    private SetVar set;
    private int offSet;
    private int[] weights;
    private final boolean notEmpty;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * Sums weights given by a set of indexes
     * SUM(weights[i-offset] | i in indexes) = sum
     *
     * @param indexes a set variable
     * @param weights array of int
     * @param offset int
     * @param sum integer variable
     * @param notEmpty true : the set variable cannot be empty
     *                 false : the set may be empty (if so, the SUM constraint is not applied)
     */
    public PropSumOfElements(SetVar indexes, int[] weights, int offset, IntVar sum, boolean notEmpty) {
        super(new Variable[]{indexes, sum}, PropagatorPriority.BINARY, true);
        this.sum = (IntVar) vars[1];
        this.set = (SetVar) vars[0];
        this.weights = weights;
        this.offSet = offset;
        this.notEmpty = notEmpty;
    }

    /**
     * Sums elements of a set
     * SUM(i | i in setVar) = sum
     *
     * @param setVar a set variable
     * @param sum a integer variable
     * @param notEmpty true : the set variable cannot be empty
     *                 false : the set may be empty (if so, the SUM constraint is not applied)
     */
    public PropSumOfElements(SetVar setVar, IntVar sum, boolean notEmpty) {
        this(setVar, null, 0, sum, notEmpty);
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public int getPropagationConditions(int vIdx) {
        if (vIdx == 0) return SetEventType.all();
        else return IntEventType.boundAndInst();
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (weights != null) {
            for (int j = set.getEnvelopeFirst(); j != SetVar.END; j = set.getEnvelopeNext()) {
                if (j < offSet || j >= weights.length + offSet) {
                    set.removeFromEnvelope(j, this);
                }
            }
        }
        propagate(0, 0);
    }

    @Override
    public void propagate(int i, int mask) throws ContradictionException {
        int sK = 0;
        int sE = 0;
        for (int j = set.getKernelFirst(); j != SetVar.END; j = set.getKernelNext()) {
            sK += get(j);
        }
        for (int j = set.getEnvelopeFirst(); j != SetVar.END; j = set.getEnvelopeNext()) {
            sE += get(j);
        }
        if (notEmpty || set.getKernelSize() > 0) {
            sum.updateBounds(sK, sE, this);
        }
        boolean again = false;
        // filter set
        int lb = sum.getLB();
        int ub = sum.getUB();
        for (int j = set.getEnvelopeFirst(); j != SetVar.END; j = set.getEnvelopeNext()) {
            if (!set.kernelContains(j)) {
                if (sE - get(j) < lb) {
                    if (set.addToKernel(j, this)) {
                        again = true;
                    }
                } else if (sK + get(j) > ub) {
                    if (set.removeFromEnvelope(j, this)) {
                        again = true;
                    }
                }
            }
        }
        if (again) {
            propagate(0, 0);
        }
    }

    @Override
    public ESat isEntailed() {
        if (set.getEnvelopeSize() == 0) {
            if (notEmpty) {
                return ESat.FALSE;
            } else {
                return ESat.TRUE;
            }
        }
        int sK = 0;
        int sE = 0;
        for (int j = set.getKernelFirst(); j != SetVar.END; j = set.getKernelNext()) {
            sK += get(j);
        }
        for (int j = set.getEnvelopeFirst(); j != SetVar.END; j = set.getEnvelopeNext()) {
            sE += get(j);
        }
        // filter set
        int lb = sum.getLB();
        int ub = sum.getUB();
        if ((lb > sE || ub < sK) && (notEmpty || set.getKernelSize() > 0)) {
            return ESat.FALSE;
        }
        if (isCompletelyInstantiated()) {
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }

    private int get(int j) {
        return (weights == null) ? j : weights[j - offSet];
    }

}
