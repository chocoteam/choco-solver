/**
 *  Copyright (c) 2010, Ecole des Mines de Nantes
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

package solver.constraints.nary;

import choco.kernel.ESat;
import solver.Solver;
import solver.constraints.IntConstraint;
import solver.constraints.propagators.PropagatorPriority;
import solver.constraints.propagators.nary.intlincomb.PropIntLinCombEq;
import solver.constraints.propagators.nary.intlincomb.PropIntLinCombGeq;
import solver.constraints.propagators.nary.intlincomb.PropIntLinCombLeq;
import solver.constraints.propagators.nary.intlincomb.PropIntLinCombNeq;
import solver.variables.IntVar;

/**
 * User : cprudhom<br/>
 * Mail : cprudhom(a)emn.fr<br/>
 * Date : 23 avr. 2010<br/>
 * Since : Galak 0.1<br/>
 */
public class IntLinComb extends IntConstraint<IntVar> {

    public enum Operator {
        EQ, NEQ, LEQ, GEQ
    }

    protected int constant;

    protected int[] coefficients;

    protected Operator operator;

    public IntLinComb(IntVar[] vars, final int[] coeffs, final int nbPos, final Operator operator, final int c,
                      Solver solver) {
        this(vars, coeffs, nbPos, operator, c, solver, _DEFAULT_THRESHOLD);
    }

    public IntLinComb(IntVar[] vars, final int[] coeffs, final int nbPos, final Operator operator, final int c,
                      Solver solver,
                      PropagatorPriority threshold) {
        super(vars, solver, threshold);
        this.operator = operator;
        this.coefficients = coeffs;
        this.constant = c;
        switch (operator) {
            case EQ:
                setPropagators(new PropIntLinCombEq(coeffs, nbPos, c, vars, this, solver.getEnvironment()));
                break;
            case GEQ:
                setPropagators(new PropIntLinCombGeq(coeffs, nbPos, c, vars, this, solver.getEnvironment()));
                break;
            case LEQ:
                setPropagators(new PropIntLinCombLeq(coeffs, nbPos, c, vars, this, solver.getEnvironment()));
                break;
            case NEQ:
                setPropagators(new PropIntLinCombNeq(coeffs, nbPos, c, vars, this, solver.getEnvironment()));
                break;
        }
    }

    private int compute(int[] tuple) {
        int s = constant;
        int nbVars = vars.length;
        int i;
        for (i = 0; i < nbVars; i++) {
            s += (tuple[i] * coefficients[i]);
        }
        return s;
    }

    public ESat isSatisfied(final int[] tuple) {
        int result = compute(tuple);
        switch (operator) {
            case EQ:
                return ESat.eval(result == 0);
            case GEQ:
                return ESat.eval(result >= 0);
            case LEQ:
                return ESat.eval(result <= 0);
            case NEQ:
                return ESat.eval(result != 0);
        }
        return ESat.FALSE;
    }

    public String toString() {
        StringBuilder linComb = new StringBuilder(20);
        linComb.append(coefficients[0]).append('*').append(vars[0].getName());
        for (int i = 1; i < coefficients.length; i++) {
            linComb.append(coefficients[i]>=0?" +":" ").append(coefficients[i]).append('*').append(vars[i].getName());
        }
        switch (operator) {
            case EQ:
                linComb.append(" = ");
                break;
            case GEQ:
                linComb.append(" >= ");
                break;
            case LEQ:
                linComb.append(" <= ");
                break;
            case NEQ:
                linComb.append(" =/= ");
                break;
        }
        linComb.append(-constant);
        return linComb.toString();
    }

}
