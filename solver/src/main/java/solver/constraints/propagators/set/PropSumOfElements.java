/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/**
 * Created by IntelliJ IDEA.
 * User: Jean-Guillaume Fages
 * Date: 14/01/13
 * Time: 16:36
 */

package solver.constraints.propagators.set;

import choco.kernel.ESat;
import choco.kernel.memory.setDataStructures.ISet;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.SetVar;
import solver.variables.Variable;

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

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * Sums weights given by a set of indexes
     * SUM(weights[i-offset] | i in indexes) = sum
     *
     * @param indexes
     * @param weights
     * @param offset
     * @param sum
     * @param solver
     * @param c
     */
    public PropSumOfElements(SetVar indexes, int[] weights, int offset, IntVar sum, Solver solver, Constraint c) {
        super(new Variable[]{indexes, sum}, solver, c, PropagatorPriority.BINARY);
        this.sum = sum;
        this.set = indexes;
        this.weights = weights;
        this.offSet = offset;
    }

    /**
     * Sums elements of a set
     * SUM(i | i in setVar) = sum
     *
     * @param setVar
     * @param sum
     * @param solver
     * @param c
     */
    public PropSumOfElements(SetVar setVar, IntVar sum, Solver solver, Constraint c) {
        this(setVar, null, 0, sum, solver, c);
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public int getPropagationConditions(int vIdx) {
        if (vIdx == 0) return EventType.ADD_TO_KER.mask + EventType.REMOVE_FROM_ENVELOPE.mask;
        else return EventType.INSTANTIATE.mask + EventType.DECUPP.mask + EventType.INCLOW.mask;
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (weights != null) {
            ISet tmp = set.getEnvelope();
            for (int j = tmp.getFirstElement(); j >= 0; j = tmp.getNextElement()) {
                if (j < offSet || j >= weights.length + offSet) {
                    set.removeFromEnvelope(j, aCause);
                }
            }
        }
        propagate(0, 0);
    }

    @Override
    public void propagate(int i, int mask) throws ContradictionException {
        ISet tmp = set.getKernel();
        int sK = 0;
        int sE = 0;
        for (int j = tmp.getFirstElement(); j >= 0; j = tmp.getNextElement()) {
            sK += get(j);
        }
        tmp = set.getEnvelope();
        for (int j = tmp.getFirstElement(); j >= 0; j = tmp.getNextElement()) {
            sE += get(j);
        }
        sum.updateLowerBound(sK, aCause);
        sum.updateUpperBound(sE, aCause);
        boolean again = false;
        // filter set
        int lb = sum.getLB();
        int ub = sum.getUB();
        for (int j = tmp.getFirstElement(); j >= 0; j = tmp.getNextElement()) {
            if (sE - get(j) < lb) {
                if (set.addToKernel(j, aCause)) {
                    again = true;
                }
            } else if (sK + get(j) > ub) {
                if (set.removeFromEnvelope(j, aCause)) {
                    again = true;
                }
            }
        }
        if (again) {
            propagate(0, 0);
        }
    }

    @Override
    public ESat isEntailed() {
        ISet tmp = set.getKernel();
        int sK = 0;
        int sE = 0;
        for (int j = tmp.getFirstElement(); j >= 0; j = tmp.getNextElement()) {
            sK += get(j);
        }
        tmp = set.getEnvelope();
        for (int j = tmp.getFirstElement(); j >= 0; j = tmp.getNextElement()) {
            sE += get(j);
        }
        // filter set
        int lb = sum.getLB();
        int ub = sum.getUB();
        if (lb > sE || ub < sK) {
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
