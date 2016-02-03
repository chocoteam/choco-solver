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

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.reification.PropOpposite;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.VariableFactory;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.util.ESat;
import org.chocosolver.util.tools.StringUtils;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * A Constraint is basically a set of <code>Propagator</code>.
 * It can either be posted or reified
 *
 * @author Jean-Guillaume Fages
 * @author Xavier Lorca
 * @author Charles Prud'homme
 * @version major revision 13/01/2014
 * @see org.chocosolver.solver.variables.Variable
 * @see Propagator
 * @see org.chocosolver.solver.propagation.IPropagationEngine
 * @since 0.01
 */
public class Constraint implements Serializable {

    // propagators of the constraint (they will filter domains and eventually check solutions)
    final protected Propagator[] propagators;

    // for reification
    BoolVar boolReif;
    Constraint opposite;

    // name
    protected String name;

    // serialization stuff
    private static final long serialVersionUID = 1L;

    /**
     * Make a new constraint defined as a set of given propagators
     *
     * @param name        name of the constraint
     * @param propagators set of propagators defining the constraint
     */
    public Constraint(String name, Propagator... propagators) {
        if (propagators == null || propagators.length == 0) {
            throw new UnsupportedOperationException("cannot create a constraint without propagators ");
        }
        this.name = name;
        this.propagators = propagators;
        for (int i = 0; i < propagators.length; i++) {
            propagators[i].defineIn(this);
        }
    }

    /**
     * Return an array which contains the propagators declared in <code>this</code>.
     *
     * @return an array of {@link Propagator}.
     */
    public Propagator[] getPropagators() {
        return propagators;
    }

    public Propagator getPropagator(int i) {
        return propagators[i];
    }

    /**
     * Test if this <code>Constraint</code> object is satisfied,
     * regarding its <code>Propagators</code> and its <code>Variable</code> current domains.
     * <p/>
     * This method is called on each solution as a checker when assertions are enabled (-ea in VM parameters)
     * It is also called for constraint reification (to state whether or not a constraint is satisfied)
     * <p/>
     * The method calls entailment checks of <code>this</code> propagators
     *
     * @return <code>ESat.FALSE</code> if the constraint cannot be satisfied (from domain consideration),
     * <code>ESat.TRUE</code> if whatever future decisions are, the constraint will be satisfied for sure (without propagating domain modifications)
     * <code>ESat.UNDIFINED</code> otherwise (more decisions/filtering must be made before concluding about constraint satisfaction)
     */
    public ESat isSatisfied() {
        int sat = 0;
        for (int i = 0; i < propagators.length; i++) {
            ESat entail = propagators[i].isEntailed();
            if (entail.equals(ESat.FALSE)) {
                return entail;
            } else if (entail.equals(ESat.TRUE)) {
                sat++;
            }
        }
        if (sat == propagators.length) {
            return ESat.TRUE;
        } else {// No need to check if FALSE, must have been returned before
            return ESat.UNDEFINED;
        }
    }

    public String toString() {
        return name + " (" + Arrays.toString(propagators) + ")";
    }

    /**
     * @return true iff this constraint has been reified
     */
    public final boolean isReified() {
        return boolReif != null;
    }

    /**
     * Reifies the constraint with a boolean variable
     * If the reified boolean variable already exists, an additional (equality) constraint is automatically posted.
     *
     * @param bool the variable to reify with
     */
    public final void reifyWith(BoolVar bool) {
        Solver s = propagators[0].getSolver();
        if (boolReif == null) {
            boolReif = bool;
            s.post(new ReificationConstraint(boolReif, this, getOpposite()));
        } else if(bool!=boolReif){
            s.post(ICF.arithm(bool, "=", boolReif));
        }
    }

    /**
     * Get/make the boolean variable indicating whether the constraint is satisfied or not
     *
     * @return the boolean reifying the constraint
     */
    public final BoolVar reif() {
        if (boolReif == null) {
            Solver s = propagators[0].getSolver();
            boolReif = VariableFactory.bool(StringUtils.randomName(), s);
            s.post(new ReificationConstraint(boolReif, this, getOpposite()));
        }
        return boolReif;
    }

    /**
     * Get/make the opposite constraint of this
     * The default opposite constraint does not filter domains but fails if this constraint is satisfied
     *
     * @return the opposite constraint of this
     */
    public final Constraint getOpposite() {
        reif();
        if (opposite == null) {
            opposite = makeOpposite();
            opposite.opposite = this;
            opposite.boolReif = boolReif.not();
        }
        return opposite;
    }

    /**
     * Make the opposite constraint of this
     * BEWARE: this method should never be called by the user
     * but it can be overridden to provide better constraint negations
     */
    public Constraint makeOpposite() {
        Variable[] vars;
        if (propagators.length == 1) {
            vars = propagators[0].vars;
        } else {
            Set<Variable> allvars = new HashSet<>();
            for (Propagator p : propagators) {
                Collections.addAll(allvars, p.vars);
            }
            vars = allvars.toArray(new Variable[allvars.size()]);
        }
        return new Constraint("DefaultOppositeOf" + name, new PropOpposite(this, vars));
    }

    /**
     * Changes the name of <code>this</code> constraint
     *
     * @param newName the name of the constraint
     */
    public void setName(String newName) {
        name = newName;
    }

    /**
     * @return the name of <code>this</code> constraint
     */
    public String getName() {
        return name;
    }

    /**
     * @return the maximum priority of a propagator of this constraint
     */
    public PropagatorPriority computeMaxPriority() {
        int priority = 1;
        for (Propagator p : propagators) {
            priority = Math.max(priority, p.getPriority().priority);
        }
        return PropagatorPriority.get(priority);
    }
}
