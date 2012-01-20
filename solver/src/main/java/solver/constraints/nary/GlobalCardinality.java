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
import choco.kernel.common.util.tools.ArrayUtils;
import choco.kernel.common.util.tools.StringUtils;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.IntConstraint;
import solver.constraints.binary.EqualX_YC;
import solver.constraints.binary.NotEqualX_YC;
import solver.constraints.propagators.nary.globalcardinality.PropBoundGlobalCardinality;
import solver.constraints.propagators.nary.globalcardinality.PropBoundGlobalCardinaltyLowUp;
import solver.constraints.reified.ReifiedConstraint;
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
public class GlobalCardinality extends IntConstraint<IntVar> {

    private final int range, nbvars, offset;
    private final int[] minOccurrences, maxOccurrences;// can be null -- when card is defined

    public static enum Consistency {AC, BC}


    public static GlobalCardinality make(IntVar[] vars, IntVar[] card, int offset, Solver solver) {
        return new GlobalCardinality(vars, card, offset, solver);
    }

    public static GlobalCardinality make(IntVar[] vars, int[] minOccurrences, int[] maxOccurrences, int offset,
                                         Consistency cons, Solver solver) {
        return new GlobalCardinality(vars, minOccurrences, maxOccurrences, offset, cons, solver);
    }

    private GlobalCardinality(IntVar[] vars, IntVar[] card, int offset, Solver solver) {
        super(ArrayUtils.append(vars, card), solver);
        this.nbvars = vars.length;
        this.offset = offset;
        this.range = card.length + offset;
        maxOccurrences = minOccurrences = null;
        //CPRU  double to simulate idempotency
        setPropagators(new PropBoundGlobalCardinality(vars, card, offset, card.length - 1 + offset, solver, this),
                new PropBoundGlobalCardinality(vars, card, offset, card.length - 1 + offset, solver, this));
    }

    public static GlobalCardinality make(IntVar[] vars, int[] values, IntVar[] card, Solver solver) {
        int n = vars.length;
        int min = values[0];
        int max = values[values.length - 1];

        for (int v = 0; v < vars.length; v++) {
            IntVar var = vars[v];
            if (min > var.getLB()) {
                min = var.getLB();
            }
            if (max < var.getUB()) {
                max = var.getUB();
            }
        }
        IntVar[] ncards = new IntVar[max - min + 1];
        int k = 0;
        for (int i = min; i <= max; i++) {
            if (k < values.length && i == values[k]) {
                ncards[i - min] = card[k];
                k++;
            } else {
                ncards[i - min] = VariableFactory.bounded(StringUtils.randomName(), 0, n, solver);
            }
        }
        return make(vars, ncards, 0, solver);
    }

    private GlobalCardinality(IntVar[] vars, int[] minOccurrences, int[] maxOccurrences, int offset, Consistency cons, Solver solver) {
        super(vars, solver);
        checker(vars, minOccurrences, maxOccurrences);
        this.nbvars = vars.length;
        this.offset = offset;
        this.range = minOccurrences.length;
        this.minOccurrences = minOccurrences;
        this.maxOccurrences = maxOccurrences;

        switch (cons) {
            case AC:
//                setPropagators(new PropGlobalCardinalityAC(vars, offset, offset + range - 1, minOccurrences, maxOccurrences, this, solver));
//                break;
                throw new SolverException("!! GlobalCardinality + AC: bugs in filtering algorithm...");
            default:
            case BC:
                //CPRU  double to simulate idempotency
                setPropagators(new PropBoundGlobalCardinaltyLowUp(vars, minOccurrences, maxOccurrences, offset, offset + range - 1, solver, this),
                        new PropBoundGlobalCardinaltyLowUp(vars, minOccurrences, maxOccurrences, offset, offset + range - 1, solver, this));
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

    public static Constraint[] reformulate(IntVar[] vars, IntVar[] card, int offset, Solver solver) {
        List<Constraint> cstrs = new ArrayList<Constraint>();
        for (int i = 0; i < card.length; i++) {
            IntVar cste = Views.fixed(i + offset, solver);
            BoolVar[] bs = VariableFactory.boolArray("b_" + i, vars.length, solver);
            for (int j = 0; j < vars.length; j++) {
                cstrs.add(new ReifiedConstraint(
                        bs[j], new EqualX_YC(vars[j], cste, 0, solver),
                        new NotEqualX_YC(vars[j], cste, 0, solver), solver));
            }
            cstrs.add(Sum.eq(bs, card[i], solver));
        }
        return cstrs.toArray(new Constraint[cstrs.size()]);
    }

    public static Constraint[] reformulate(IntVar[] vars, int[] minOccurrences, int[] maxOccurrences, int offset, Solver solver) {
        List<Constraint> cstrs = new ArrayList<Constraint>();
        for (int i = 0; i < minOccurrences.length; i++) {
            IntVar cste = Views.fixed(i + offset, solver);
            BoolVar[] bs = VariableFactory.boolArray("b_" + i, vars.length, solver);
            for (int j = 0; j < vars.length; j++) {
                cstrs.add(new ReifiedConstraint(
                        bs[j], new EqualX_YC(vars[j], cste, 0, solver),
                        new NotEqualX_YC(vars[j], cste, 0, solver), solver));
            }
            cstrs.add(Sum.geq(bs, minOccurrences[i], solver));
            cstrs.add(Sum.leq(bs, maxOccurrences[i], solver));
        }
        return cstrs.toArray(new Constraint[cstrs.size()]);
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer("GlobalCardinality(<");
        buf.append(vars[0]);
        for (int i = 1; i < nbvars; i++) {
            buf.append(',').append(vars[i]);
        }
        buf.append(">,<");
        if (nbvars < vars.length) {
            buf.append(offset).append(":").append(vars[nbvars]);
            for (int i = 1; i < vars.length - nbvars; i++) {
                buf.append(',').append(offset + i).append(":").append(vars[nbvars + i]);
            }
        } else {
            buf.append(offset).append(":[").append(minOccurrences[0]).append(",").append(maxOccurrences[0]).append("]");
            for (int i = 1; i < minOccurrences.length; i++) {
                buf.append(',').append(offset + i).append(":[").append(minOccurrences[i]).append(",").append(maxOccurrences[i]).append("]");
            }
        }
        buf.append(">)");
        return new String(buf);
    }

    @Override
    public ESat isSatisfied(int[] tuple) {
        int[] occurrences = new int[this.range];
        for (int i = 0; i < nbvars; i++) {
            occurrences[tuple[i] - this.offset]++;
        }
        if (tuple.length > nbvars) {
            for (int i = 0; i < occurrences.length; i++) {
                int occurrence = occurrences[i];
                if (tuple[nbvars + i] != occurrence) {
                    return ESat.FALSE;
                }
            }
        } else {
            for (int i = 0; i < occurrences.length; i++) {
                int occurrence = occurrences[i];
                if ((this.minOccurrences[i] > occurrence) || (occurrence > this.maxOccurrences[i])) {
                    return ESat.FALSE;
                }
            }
        }
        return ESat.TRUE;
    }
}
