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

package solver.search.strategy.decision;

import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.nary.IntLinComb;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.nary.intlincomb.*;
import solver.exception.ContradictionException;
import solver.explanations.Deduction;
import solver.explanations.Explanation;
import solver.variables.IntVar;

/**
 * todo: comment
 */
public class IntLinCombDecision extends IntLinComb implements Decision {

    Decision previous;

    int branch;

    AbstractPropIntLinComb prop, negprop;

    long fails;

    public IntLinCombDecision(Solver solver) {
        super(new IntVar[0], new int[0], 0, Operator.EQ, 0, solver, null);
    }

    public IntLinCombDecision set(IntVar[] vars, final int[] coeffs, final Operator operator, final int c) {
        branch = 0;
        this.vars = vars;
        this.coefficients = coeffs;
        int nbPos = 0;
        while (nbPos < coefficients.length && coefficients[nbPos] > 0) {
            nbPos++;
        }
        int nbPositiveCoefficients = nbPos;
        this.constant = c;
        this.operator = operator;
        switch (operator) {
            case EQ:
                prop = new PropIntLinCombEq(coefficients, nbPositiveCoefficients, constant, vars, this, solver.getEnvironment());
                negprop = new PropIntLinCombNeq(coefficients, nbPositiveCoefficients, constant, vars, this, solver.getEnvironment());
                break;
            case GEQ:
                prop = new PropIntLinCombGeq(coefficients, nbPositiveCoefficients, constant, vars, this, solver.getEnvironment());
                negprop = new PropIntLinCombLeq(coefficients, nbPositiveCoefficients, constant - 1, vars, this, solver.getEnvironment());
                break;
            case LEQ:
                prop = new PropIntLinCombLeq(coefficients, nbPositiveCoefficients, constant, vars, this, solver.getEnvironment());
                negprop = new PropIntLinCombGeq(coefficients, nbPositiveCoefficients, constant + 1, vars, this, solver.getEnvironment());
                break;
            case NEQ:
                prop = new PropIntLinCombNeq(coefficients, nbPositiveCoefficients, constant, vars, this, solver.getEnvironment());
                negprop = new PropIntLinCombEq(coefficients, nbPositiveCoefficients, constant, vars, this, solver.getEnvironment());
                break;
        }
        return this;
    }

    @Override
    public boolean hasNext() {
        return branch < 2;
    }

    @Override
    public void buildNext() {
        branch++;
    }

    public void apply() throws ContradictionException {
        if (branch == 1) {
            this.setPropagators(prop);
        } else if (branch == 2) {
            this.setPropagators(negprop);
        }
        int last = lastPropagatorActive.get();
        for (int p = 0; p < last; p++) {
            propagators[p].propagate();
        }
    }

    @Override
    public void setPrevious(Decision decision) {
        this.previous = decision;
    }

    @Override
    public Decision getPrevious() {
        return previous;
    }

    @Override
    public void free() {
    }

    @Override
    public boolean reactOnPromotion() {
        return false;
    }

    @Override
    public void updateActivity(Propagator<IntVar> prop) {
    }

    @Override
    public Constraint getConstraint() {
        return this;
    }

    @Override
    public Explanation explain(IntVar v, Deduction d) {
        return null;  // TODO implémenter la notion d'explication pour ce type de décision
    }

    @Override
    public String toString() {
        return String.format("%s (%d)", prop.toString(), branch);
    }

    public void incFail() {
        fails++;
    }

    public long getFails() {
        return fails;
    }
}
