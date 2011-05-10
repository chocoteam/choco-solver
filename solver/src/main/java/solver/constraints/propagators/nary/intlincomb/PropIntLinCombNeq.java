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

package solver.constraints.propagators.nary.intlincomb;

import choco.kernel.ESat;
import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.IStateInt;
import solver.constraints.Constraint;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.Variable;

/**
 * User : cprudhom<br/>
 * Mail : cprudhom(a)emn.fr<br/>
 * Date : 23 avr. 2010br/>
 * Since : Galak 0.1<br/>
 */
public final class PropIntLinCombNeq extends AbstractPropIntLinComb {

    private final IStateInt nb_instantiated;

    public PropIntLinCombNeq(final int[] coeffs, final int nbPosVars, final int cste, final IntVar[] vars,
                            final Constraint constraint, final IEnvironment env) {
        super(coeffs, nbPosVars, cste, vars, constraint, env);
        nb_instantiated = env.makeInt(0);
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return EventType.INSTANTIATE.mask;
    }

    @Override
    /**
     * Update nb_instantiated
     */
    public void propagate() throws ContradictionException {
        int n = 0;
        for(Variable v : vars){
            if(v.instantiated()){
                n++;
            }
        }
        nb_instantiated.set(n);
        filter();
    }

    @Override
    public void awakeOnInst(final int idx, Constraint constraint) throws ContradictionException {
        nb_instantiated.add(1);
        filter();
    }

    /**
     * if there is only one uninstantiated variable,
     * then filtering can be applied on this uninstantiated variable.
     * 
     * @throws ContradictionException if the domain of variables are inconsistent regarding to the constraint
     */
    private void filter() throws ContradictionException {
        if(nb_instantiated.get() >= vars.length-1){
            int index = -1;
            int sum = this.cste;
            for(int i=0; i < vars.length; i++ ){
                if(vars[i].instantiated()){
                    sum += coeffs[i] * vars[i].getValue();
                }else{
                    index = i;
                }
            }
            // If every variables are instantiated (by side effects),
            if(index == -1){
                // then check the sum is not equal to 0
                if(sum == 0){
                    // Otherwise, FAIL, the constraint is not satisfied
                    ContradictionException.throwIt(this, null, "sum is equal to 0");
                }
            }else{
                // Compute the value to remove (including position in the linear combination)
                int value = -1 * sum / coeffs[index];
                if(vars[index].removeValue(value, this)){
                    this.setPassive();
                }
            }
        }
    }

    /**
	 * Checks a new lower bound.
	 * @return true if filtering has been infered
	 * @throws ContradictionException if a domain empties or a contradiction is
	 * infered
     */
    public boolean filterOnImprovedLowerBound() throws ContradictionException {
        return false;
    }

	/**
	 * Checks a new upper bound.
	 * @return true if filtering has been infered
	 * @throws ContradictionException if a domain empties or a contradiction is
	 * infered  */
    public boolean filterOnImprovedUpperBound() throws ContradictionException {
        return false;
	}

    /**
	 * Tests if the constraint is consistent
	 * with respect to the current state of domains.
	 * @return true iff the constraint is bound consistent
	 * (weaker than arc consistent)
	 */
	public boolean isConsistent() {
		return true;
	}

    @Override
    protected String getOperator() {
        return " =/= ";
    }

    @Override
    protected ESat check(int value) {
        return ESat.eval(value != 0);
    }

    @Override
    protected void checkEntailment() {
        // USELESS FOR NEQ, entailment is checked within #awakeOnInst()
    }
}