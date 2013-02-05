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
package solver.constraints.nary;

import choco.kernel.ESat;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.IntConstraint;
import solver.constraints.IntConstraintFactory;
import solver.constraints.propagators.nary.globalcardinality.PropBoundGlobalCardinaltyLowUp;
import solver.constraints.propagators.nary.globalcardinality.PropGCC_AC_LowUp;
import solver.exception.SolverException;
import solver.variables.BoolVar;
import solver.variables.IntVar;
import solver.variables.VariableFactory;
import solver.variables.view.Views;

import java.util.ArrayList;
import java.util.List;

/**
 * Global Cardinality constraints.
 * Two versions available:
 * <br/>- GCC(VARS, CARDS, OFFSET) ensures CARDS[i] counts the number of occurrences of the value (i+offset) in VARS
 * <br/>- GCC(VARS, MINS, MAXS, OFFSET) ensures the number of occurrences of the values (i+OFFSET) is btween MINS[i] and MAX[i]
 * <br/>This last version is available ensuring BC or AC.
 * <p/>
 * <p/>
 * The BC propagators are based on:
 * <br/>
 * Bound Global cardinality : Given an array of variables vars, min the minimal value over all variables,
 * and max the maximal value over all variables (or a table IntDomainVar to represent the cardinalities), the constraint ensures that the number of occurences
 * of the value i among the variables is between low[i - min] and up[i - min]. Note that the length
 * of low and up should be max - min + 1.
 * Use the propagator of :
 * C.-G. Quimper, P. van Beek, A. Lopez-Ortiz, A. Golynski, and S.B. Sadjad.
 * An efficient bounds consistency algorithm for the global cardinality constraint.
 * CP-2003.
 * <br/>
 *
 * @author Hadrien Cambazard, Charles Prud'homme
 * @since 16/06/11
 */
public class GlobalCardinalityLowUp extends IntConstraint<IntVar> {

    private final int range;
    private final int[] lows, ups;

    public static enum Consistency {AC, BC}


    public GlobalCardinalityLowUp(IntVar[] vars, int[] values, int[] lows, int[] ups, Consistency cons, Solver solver) {
        super(vars, solver);
        checker(vars, lows, ups);
        this.range = lows.length;
        this.lows = lows;
        this.ups = ups;

        switch (cons) {
            case AC:
                addPropagators(new PropGCC_AC_LowUp(vars, values, lows, ups, this, solver));
            default:
            case BC:
                //CPRU  double to simulate idempotency
                setPropagators(new PropBoundGlobalCardinaltyLowUp(vars, lows, ups, values[0], values[range - 1], solver, this),
                        new PropBoundGlobalCardinaltyLowUp(vars, lows, ups, values[0], values[range - 1], solver, this));
                break;
        }
    }

    private static void checker(IntVar[] vars, int[] low, int[] up) {
        if (low.length != up.length) {
            throw new SolverException("globalCardinality : low and up do not have same size");
        }
        int sumL = 0;
        for (int i = 0; i < low.length; i++) {
            sumL += low[i];
            if (low[i] > up[i]) throw new SolverException("globalCardinality : incorrect low and up (" + i + ")");
        }

        if (vars.length < sumL) {
            throw new SolverException("globalCardinality : not enough minimum values");
        }
    }

    public static Constraint[] reformulate(IntVar[] vars, int[] minOccurrences, int[] maxOccurrences, int offset, Solver solver) {
        List<Constraint> cstrs = new ArrayList<Constraint>();
        for (int i = 0; i < minOccurrences.length; i++) {
            IntVar cste = Views.fixed(i + offset, solver);
            BoolVar[] bs = VariableFactory.boolArray("b_" + i, vars.length, solver);
            for (int j = 0; j < vars.length; j++) {
                cstrs.add(IntConstraintFactory.reified(bs[j], IntConstraintFactory.arithm(vars[j], "=", cste), IntConstraintFactory.arithm(vars[j], "!=", cste)));
            }
            cstrs.add(IntConstraintFactory.sum(bs, ">=", minOccurrences[i]));
            cstrs.add(IntConstraintFactory.sum(bs, "<=", maxOccurrences[i]));
        }
        return cstrs.toArray(new Constraint[cstrs.size()]);
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer("GlobalCardinality(<");
        buf.append(vars[0]);
        for (int i = 1; i < vars.length; i++) {
            buf.append(',').append(vars[i]);
        }
        buf.append(">,<");

        buf.append(":[").append(lows[0]).append(",").append(ups[0]).append("]");
        for (int i = 1; i < lows.length; i++) {
            buf.append(',').append(i).append(":[").append(lows[i]).append(",").append(ups[i]).append("]");
        }
        buf.append(">)");
        return new String(buf);
    }

    @Override
    public ESat isSatisfied(int[] tuple) {
        int[] occurrences = new int[this.range];
        for (int i = 0; i < vars.length; i++) {
            occurrences[tuple[i]]++;
        }
        for (int i = 0; i < occurrences.length; i++) {
            int occurrence = occurrences[i];
            if ((this.lows[i] > occurrence) || (occurrence > this.ups[i])) {
                return ESat.FALSE;
            }
        }
        return ESat.TRUE;
    }
}
