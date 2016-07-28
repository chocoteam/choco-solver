/**
 * Copyright (c) 2016, Ecole des Mines de Nantes
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
import org.chocosolver.util.objects.setDataStructures.ISetIterator;

/**
 * Retrieves the maximum element of the set
 * the set must not be empty
 *
 * @author Jean-Guillaume Fages
 */
public class PropMaxElement extends Propagator<Variable> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private IntVar max;
    private SetVar set;
    private int offSet;
    private int[] weights;
    private final boolean notEmpty;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * Retrieves the maximum element of the set
     * MAX{i | i in setVar} = max
     *
     * @param setVar set variable
     * @param max integer variable
     * @param notEmpty true : the set variable cannot be empty
     *                 false : the set may be empty (if so, the MAX constraint is not applied)
     */
    public PropMaxElement(SetVar setVar, IntVar max, boolean notEmpty) {
        this(setVar, null, 0, max, notEmpty);
    }

    /**
     * Retrieves the maximum element induced by set
     * MAX{weight[i-offset] | i in setVar} = max
     *
     * @param setVar set variable
     * @param weights array of int
     * @param offset int
     * @param max integer variable
     * @param notEmpty true : the set variable cannot be empty
     *                 false : the set may be empty (if so, the MAX constraint is not applied)
     */
    public PropMaxElement(SetVar setVar, int[] weights, int offset, IntVar max, boolean notEmpty) {
        super(new Variable[]{setVar, max}, PropagatorPriority.BINARY, false);
        this.max = (IntVar) vars[1];
        this.set = (SetVar) vars[0];
        this.weights = weights;
        this.offSet = offset;
        this.notEmpty = notEmpty;
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
        ISetIterator iter = set.getLB().iterator();
        while (iter.hasNext()){
            max.updateLowerBound(get(iter.nextInt()), this);
        }
        int maxVal = Integer.MIN_VALUE;
        int ub = max.getUB();
        iter = set.getUB().iterator();
        while (iter.hasNext()){
            int j = iter.nextInt();
            int k = get(j);
            if (k > ub) {
                set.remove(j, this);
            } else {
                if (maxVal < k) {
                    maxVal = k;
                }
            }
        }
        if (notEmpty || set.getLB().size() > 0) {
            max.updateUpperBound(maxVal, this);
        }
    }

    @Override
    public ESat isEntailed() {
        if (set.getUB().size() == 0) {
            if (notEmpty) {
                return ESat.FALSE;
            } else {
                return ESat.TRUE;
            }
        }
        int lb = max.getLB();
        int ub = max.getUB();
        ISetIterator iter = set.getLB().iterator();
        while (iter.hasNext()){
            if (get(iter.nextInt()) > ub) {
                return ESat.FALSE;
            }
        }
        int maxVal = Integer.MIN_VALUE;
        iter = set.getUB().iterator();
        while (iter.hasNext()){
            int j = iter.nextInt();
            if (maxVal < get(j)) {
                maxVal = get(j);
            }
        }
        if (maxVal < lb && (notEmpty || set.getLB().size() > 0)) {
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
