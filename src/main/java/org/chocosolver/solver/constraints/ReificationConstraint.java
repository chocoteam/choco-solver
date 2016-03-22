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
package org.chocosolver.solver.constraints;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.reification.PropReif;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.events.PropagatorEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.tools.ArrayUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * Implication constraint: boolean b => constraint c
 * Also known as half reification
 * <br/>
 *
 * @author Jean-Guillaume Fages
 * @since 02/2013
 */
public class ReificationConstraint extends Constraint {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    // boolean variable of the reification
    private final BoolVar bool;
    // constraint to apply if bool = true
    private final Constraint trueCons;
    // constraint to apply if bool = false
    private final Constraint falseCons;
    // indices of propagators
    private int[] indices;
    // reification propagator;
    private final PropReif propReif;

    //***********************************************************************************
    // CONSTRUCTION
    //***********************************************************************************

    protected ReificationConstraint(BoolVar bVar, Constraint consIfBoolTrue, Constraint consIfBoolFalse) {
        super("ReificationConstraint", createProps(bVar, consIfBoolTrue, consIfBoolFalse));
        this.propReif = (PropReif) propagators[0];
        propReif.setReifCons(this);
        trueCons = consIfBoolTrue;
        falseCons = consIfBoolFalse;
        bool = bVar;
        indices = new int[3];
        indices[0] = 1;
        indices[1] = indices[0] + trueCons.getPropagators().length;
        indices[2] = indices[1] + falseCons.getPropagators().length;
        for (int i = 1; i < propagators.length; i++) {
            propagators[i].setReifiedSilent();
        }
    }

    private static Propagator[] createProps(BoolVar bVar, Constraint trueCons, Constraint falseCons) {
        Set<Variable> setOfVars = new HashSet<>();
        prepareConstraint(bVar, trueCons, setOfVars);
        prepareConstraint(bVar, falseCons, setOfVars);
        Variable[] allVars = ArrayUtils.append(new Variable[]{bVar}, setOfVars.toArray(new Variable[setOfVars.size()]));
        PropReif reifProp = new PropReif(allVars, trueCons, falseCons);
        return ArrayUtils.append(new Propagator[]{reifProp},
                trueCons.getPropagators().clone(),
                falseCons.getPropagators().clone()
        );
    }

    private static void prepareConstraint(BoolVar bVar, Constraint c, Set<Variable> setOfVars){
        for (Propagator p : c.getPropagators()) {
            for (Variable v : p.getVars()) {
                if (v != bVar) {
                    setOfVars.add(v);
                }
            }
        }
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    public void activate(int idx) throws ContradictionException {
        Model model = propagators[0].getModel();
        assert bool.isInstantiatedTo(1 - idx);
        for (int p = indices[idx]; p < indices[idx + 1]; p++) {
            assert (propagators[p].isReifiedAndSilent());
            propagators[p].setReifiedTrue();
            model.getSolver().getExplainer().activePropagator(bool, propagators[p]);
            propagators[p].propagate(PropagatorEventType.FULL_PROPAGATION.getMask());
            model.getSolver().getEngine().onPropagatorExecution(propagators[p]);
        }
    }

    @Override
    public ESat isSatisfied() {
        return propReif.isEntailed();
    }

    @Override
    public String toString() {
        return bool.toString() + "=>" + trueCons.toString() + ", !" + bool.toString() + "=>" + falseCons.toString();
    }

}
