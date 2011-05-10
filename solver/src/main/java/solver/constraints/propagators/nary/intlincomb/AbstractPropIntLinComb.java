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
import choco.kernel.common.util.tools.MathUtils;
import choco.kernel.memory.IEnvironment;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.constraints.propagators.nary.intlincomb.policy.AbstractCoeffPolicy;
import solver.exception.ContradictionException;
import solver.requests.IRequest;
import solver.variables.EventType;
import solver.variables.IntVar;

/**
 * User : cprudhom<br/>
 * Mail : cprudhom(a)emn.fr<br/>
 * Date : 23 avr. 2010br/>
 * Since : Galak 0.1<br/>
 */
public abstract class AbstractPropIntLinComb extends Propagator<IntVar> {

    /**
     * The coefficients of the linear equations.
     * The positive coefficents should be the first ones.
     */
    final int[] coeffs;

    /**
     * Field representing the number of variables
     * with positive coeffficients in the linear combination.
     */
    private final int nbPosVars;

    /**
     * The constant of the constraint.
     */
    final int cste;

    final IntVar[] vars;

    final AbstractCoeffPolicy coeffPolicy;

    private static PropagatorPriority computePriority(int nbvars) {
        if (nbvars == 1) {
            return PropagatorPriority.UNARY;
        } else if (nbvars == 2) {
            return PropagatorPriority.BINARY;
        } else if (nbvars == 3) {
            return PropagatorPriority.TERNARY;
        } else {
            return PropagatorPriority.LINEAR;
        }
    }


    @SuppressWarnings({"unchecked"})
    public AbstractPropIntLinComb(final int[] coeffs, final int nbPosVars, final int cste, final IntVar[] vars,
                                  final Constraint constraint, final IEnvironment env) {
        super(vars.clone(), env, constraint, computePriority(vars.length), false);
        this.coeffs = coeffs;
        this.nbPosVars = nbPosVars;
        this.cste = cste;
        this.vars = vars;
        coeffPolicy = AbstractCoeffPolicy.build(vars, coeffs, nbPosVars, cste);
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return EventType.INSTANTIATE.mask + EventType.BOUND.mask;
    }


    /**
     * Checks a new lower bound.
     *
     * @return true if filtering has been infered
     * @throws ContradictionException if a domain empties or a contradiction is
     *                                infered
     */
    protected abstract boolean filterOnImprovedLowerBound() throws ContradictionException;

    /**
     * Checks a new upper bound.
     *
     * @return true if filtering has been infered
     * @throws ContradictionException if a domain empties or a contradiction is
     *                                infered
     */
    protected abstract boolean filterOnImprovedUpperBound() throws ContradictionException;

    /**
     * Tests if the constraint is consistent
     * with respect to the current state of domains.
     *
     * @return true iff the constraint is bound consistent
     *         (weaker than arc consistent)
     */
    public abstract boolean isConsistent();

    // Note: additional propagation pass are sometimes useful:
    // For instance : 3*X[0.3] + Y[1.10] = 10
    //                Y >= 2 causes X < 3 -> updateUpperBound(X,2)
    //                and this very var (the new sup of X) causes (Y >= 4).
    //                this induced var (Y>=4) could not be infered
    //                at first (with only Y>=2)
    //

    /**
     * A strategy for chaotic iteration with two rules (LB and UB propagation).
     * The fix point is reached individually for each rule in one function call
     * but this call may break the stability condition for the other rule
     * (in which case the second rule infers new information from the fresh
     * inferences made by the first rule) .
     * The algorithm oscilates between both rules until
     * a global fix point is reached.
     *
     * @param startWithLB whether LB must be the first rule applied
     * @param minNbRules  minimum number of rules required to reach fix point.
     * @throws ContradictionException if a domain empties or a contradiction is
     *                                infered
     */
    public final void filter(final boolean startWithLB,
                             final int minNbRules) throws ContradictionException {
        boolean lastRuleEffective = true;
        // whether the last rule indeed perform some reductions
        int nbr = 0;
        // number of rules applied
        boolean nextRuleIsLB = startWithLB;
        // whether the next rule that should be filtered is LB (or UB)
        while (lastRuleEffective || nbr < minNbRules) {
            if (nextRuleIsLB) {
                lastRuleEffective = filterOnImprovedLowerBound();
            } else {
                lastRuleEffective = filterOnImprovedUpperBound();
            }
//			nextRuleIsLB = !nextRuleIsLB;
            nextRuleIsLB ^= true;
            nbr++;
        }
        checkEntailment();
        //////////////////////////////////////////////////////////////////////////////////
    }

    protected abstract void checkEntailment();

    /**
     * Launchs the filtering algorithm.
     *
     * @throws ContradictionException if a domain empties or a contradiction is
     *                                infered
     */
    public void propagate() throws ContradictionException {
        filter(true, 2);
    }

    @Override
    public void propagateOnRequest(IRequest<IntVar> request, int varIdx, int mask) throws ContradictionException {
        if (EventType.isInstantiate(mask)) {
            this.awakeOnInst(varIdx, this.constraint);
        } else {
            if(EventType.isBound(mask)){
                filter(true, 2);
            }else
            if (EventType.isInclow(mask)) {
                this.awakeOnLow(varIdx);
            }else
            if (EventType.isDecupp(mask)) {
                this.awakeOnUpp(varIdx);
            }
        }
    }

    /**
     * Propagation whenever the lower bound of a variable is modified.
     *
     * @param idx the index of the modified variable
     * @throws ContradictionException if a domain empties or a contradiction is
     *                                infered
     */
    void awakeOnLow(final int idx) throws ContradictionException {
        if (idx < nbPosVars) {
            filter(true, 1);
        } else {
            filter(false, 1);
        }
    }

    /**
     * Propagation whenever the upper bound of a variable is modified.
     *
     * @param idx the index of the modified variable
     * @throws ContradictionException if a domain empties or a contradiction is
     *                                infered
     */
    void awakeOnUpp(final int idx) throws ContradictionException {
        if (idx < nbPosVars) {
            filter(false, 1);
        } else {
            filter(true, 1);
        }
    }

    /**
     * Propagation whenever a variable is instantiated.
     *
     * @param idx        the index of the modified variable
     * @param constraint
     * @throws solver.exception.ContradictionException
     *          if a domain empties or a contradiction is
     *          infered
     */
    void awakeOnInst(final int idx, Constraint constraint) throws ContradictionException {
        propagate();
    }

    /**
     * Propagates the constraint sigma(ai Xi) + c <= 0
     * where mylb = sigma(ai inf(Xi)) + c.
     * Note: this does not reach saturation (fix point),
     * but returns a boolean indicating whether
     * it infered new information or not.
     *
     * @param mylb the computed lower bound
     * @return true if filtering has been infered
     * @throws ContradictionException if a domain empties or a contradiction
     *                                is infered
     */
    final boolean propagateNewLowerBound(final int mylb)
            throws ContradictionException {
        boolean anyChange = false;
        int nbVars = vars.length;
        if (mylb > 0) {
            ContradictionException.throwIt(this, null, "computed lower bound > 0");
        }
        int i;
        for (i = 0; i < nbPosVars; i++) {
            int newSupi = coeffPolicy.getSupPV(i, mylb);//MathUtils.divFloor(-(mylb), coeffs[i]) + vars[i].getLB();
            if (vars[i].updateUpperBound(newSupi, this)) {
                anyChange = true;
            }
        }
        for (i = nbPosVars; i < nbVars; i++) {
            int newInfi = coeffPolicy.getInfNV(i, mylb);//MathUtils.divCeil(mylb, -(coeffs[i])) + vars[i].getUB();
            if (vars[i].updateLowerBound(newInfi, this)) {
                anyChange = true;
            }
        }
        return anyChange;
    }

    /**
     * Propagates the constraint sigma(ai Xi) + c <= 0
     * where myub = sigma(ai sup(Xi)) + c.
     * Note: this does not reach saturation (fix point),
     * but returns a boolean indicating whether
     * it infered new information or not.
     *
     * @param myub the computed upper bound
     * @return true if filtering has been infered
     * @throws ContradictionException if a domain empties or a contradiction
     *                                is infered
     */
    final boolean propagateNewUpperBound(final int myub)
            throws ContradictionException {
        boolean anyChange = false;
        int nbVars = vars.length;
        if (myub < 0) {
            ContradictionException.throwIt(this, null, "computed upper bound > 0");
        }
        int i;
        for (i = 0; i < nbPosVars; i++) {
            int newInfi = coeffPolicy.getInfPV(i, myub);//MathUtils.divCeil(-(myub), coeffs[i]) + vars[i].getUB();
            if (vars[i].updateLowerBound(newInfi, this)) {
                anyChange = true;
            }
        }
        for (i = nbPosVars; i < nbVars; i++) {
            int newSupi = coeffPolicy.getSupNV(i, myub);//MathUtils.divFloor(myub, -(coeffs[i])) + vars[i].getLB();
            if (vars[i].updateUpperBound(newSupi, this)) {
                anyChange = true;
            }
        }
        return anyChange;
    }

    /**
     * Tests if the constraint is consistent
     * with respect to the current state of domains.
     *
     * @return true iff the constraint is bound consistent
     *         (weaker than arc consistent)
     */
    final boolean hasConsistentLowerBound() {
        int lb = coeffPolicy.computeLowerBound();
        int nbVars = vars.length;

        if (lb > 0) {
            return false;
        } else {
            for (int i = 0; i < nbPosVars; i++) {
                int newSupi = MathUtils.divFloor(-(lb), coeffs[i]) + vars[i].getLB();
                if (vars[i].getUB() < newSupi) {
                    return false;
                }
            }
            for (int i = nbPosVars; i < nbVars; i++) {
                int newInfi = MathUtils.divCeil(lb, -(coeffs[i])) + vars[i].getUB();
                if (vars[i].getLB() > newInfi) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * Tests if the constraint is consistent
     * with respect to the current state of domains.
     *
     * @return true iff the constraint is bound consistent
     *         (weaker than arc consistent)
     */
    protected final boolean hasConsistentUpperBound() {
        int ub = coeffPolicy.computeUpperBound();
        int nbVars = vars.length;

        if (ub < 0) {
            return false;
        } else {
            for (int i = 0; i < nbPosVars; i++) {
                int newInfi = MathUtils.divCeil(-(ub), coeffs[i]) + vars[i].getUB();
                if (vars[i].getLB() > newInfi) {
                    return false;
                }
            }
            for (int i = nbPosVars; i < nbVars; i++) {
                int newSupi = MathUtils.divFloor(ub, -(coeffs[i])) + vars[i].getLB();
                if (vars[i].getUB() < newSupi) {
                    return false;
                }
            }
            return true;
        }
    }

    protected abstract String getOperator();

    @Override
    public ESat isEntailed() {
        if (isCompletelyInstantiated()) {
            int result = coeffPolicy.computeLowerBound();
            return check(result);
        }
        return ESat.UNDEFINED;
    }

    protected abstract ESat check(int value);

    /**
     * {@inheritDoc}
     */
    public String toString() {
        StringBuilder linComb = new StringBuilder(50);
        for (int i = 0; i < coeffs.length - 1; i++) {
            linComb.append(coeffs[i]).append('*').append(vars[i]).append(" + ");
        }
        linComb.append(coeffs[coeffs.length - 1]).append('*').append(vars[coeffs.length - 1]);
        linComb.append(getOperator());
        linComb.append(-cste);
        return linComb.toString();
    }
}
