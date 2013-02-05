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
package solver.constraints.nary.globalcardinality;

import choco.kernel.ESat;
import choco.kernel.common.util.tools.ArrayUtils;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.IntConstraint;
import solver.constraints.IntConstraintFactory;
import solver.constraints.propagators.nary.globalcardinality.PropBoundGlobalCardinality;
import solver.constraints.propagators.nary.globalcardinality.PropGCC_AC_Cards_AC;
import solver.constraints.propagators.nary.globalcardinality.PropGCC_AC_Cards_Fast;
import solver.variables.BoolVar;
import solver.variables.IntVar;
import solver.variables.VariableFactory;
import solver.variables.view.Views;

import java.util.ArrayList;
import java.util.List;

/**
 * Global Cardinality constraint
 *
 * @author Hadrien Cambazard, Charles Prud'homme
 * @since 16/06/11
 */
public class GlobalCardinality extends IntConstraint<IntVar> {

    private final int range, nbvars;

    public static enum Consistency {
        AC, AC_ON_CARDS, BC
    }

    public GlobalCardinality(IntVar[] vars, int[] values, IntVar[] cards, Consistency consistency, Solver solver) {
        super(ArrayUtils.append(vars, cards), solver);
        this.nbvars = vars.length;
        this.range = cards.length;
        switch (consistency) {
            case AC:
                setPropagators(new PropGCC_AC_Cards_Fast(vars, values, cards, this, solver));
                return;
            case AC_ON_CARDS:
                setPropagators(new PropGCC_AC_Cards_AC(vars, values, cards, this, solver));
                return;
            case BC:
            default:
                //CPRU  double to simulate idempotency
                setPropagators(new PropBoundGlobalCardinality(vars, cards, values[0], values[values.length - 1], solver, this),
                        new PropBoundGlobalCardinality(vars, cards, values[0], values[values.length - 1], solver, this));

        }

    }

    public static Constraint[] reformulate(IntVar[] vars, IntVar[] card, Solver solver) {
        List<Constraint> cstrs = new ArrayList<Constraint>();
        for (int i = 0; i < card.length; i++) {
            IntVar cste = Views.fixed(i, solver);
            BoolVar[] bs = VariableFactory.boolArray("b_" + i, vars.length, solver);
            for (int j = 0; j < vars.length; j++) {
                cstrs.add(IntConstraintFactory.reified(bs[j], IntConstraintFactory.arithm(vars[j], "=", cste), IntConstraintFactory.arithm(vars[j], "!=", cste)));
            }
            cstrs.add(IntConstraintFactory.sum(bs, "=", card[i]));
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
        buf.append(":").append(vars[nbvars]);
        for (int i = 1; i < vars.length - nbvars; i++) {
            buf.append(',').append(i).append(":").append(vars[nbvars + i]);
        }
        buf.append(">)");
        return new String(buf);
    }

    @Override
    public ESat isSatisfied(int[] tuple) {
        int[] occurrences = new int[this.range];
        for (int i = 0; i < nbvars; i++) {
            occurrences[tuple[i]]++;
        }
        for (int i = 0; i < occurrences.length; i++) {
            int occurrence = occurrences[i];
            if (tuple[nbvars + i] != occurrence) {
                return ESat.FALSE;
            }
        }
        return ESat.TRUE;
    }
}
