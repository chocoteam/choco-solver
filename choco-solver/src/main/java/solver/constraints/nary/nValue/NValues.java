/*
 * Copyright (c) 1999-2012, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package solver.constraints.nary.nValue;

import gnu.trove.list.array.TIntArrayList;
import solver.Solver;
import solver.constraints.IntConstraint;
import solver.constraints.nary.nValue.PropAtLeastNValues_AC;
import solver.constraints.nary.nValue.PropAtMostNValues_BC;
import solver.constraints.nary.nValue.PropAtMostNValues_Greedy;
import solver.constraints.nary.nValue.PropNValues_Light;
import solver.variables.IntVar;
import solver.variables.Variable;
import util.ESat;
import util.tools.ArrayUtils;

import java.util.BitSet;

/**
 * NValues constraint
 * The number of distinct values in the set of variables and within a set of given values is equal to nValues
 *
 * @author Jean-Guillaume Fages
 */
public class NValues extends IntConstraint<IntVar> {

    public enum Type {
        at_most_BC {
            @Override
            public void addProp(IntVar[] vars, IntVar nValues, IntConstraint<IntVar> cons, Solver solver) {
                cons.addPropagators(new PropAtMostNValues_BC(vars, nValues));
                boolean enumDom = false;
                for (IntVar v : vars) {
                    if (v.hasEnumeratedDomain()) {
                        enumDom = true;
                        break;
                    }
                }
                if (enumDom)// added twice to perform fixpoint
                    cons.addPropagators(new PropAtMostNValues_BC(vars, nValues));
            }
        },
        at_most_greedy {
            @Override
            public void addProp(IntVar[] vars, IntVar nValues, IntConstraint<IntVar> cons, Solver solver) {
                cons.addPropagators(new PropAtMostNValues_Greedy(vars, nValues));
            }
        },
        at_least_AC {
            @Override
            public void addProp(IntVar[] vars, IntVar nValues, IntConstraint<IntVar> cons, Solver solver) {
                cons.addPropagators(new PropAtLeastNValues_AC(vars, nValues));
            }
        };

        public abstract void addProp(IntVar[] vars, IntVar nValues, IntConstraint<IntVar> cons, Solver solver);
    }

    /**
     * NValues constraint
     * The number of distinct values in vars is exactly nValues
     * private because the case were all values are not restricted is not tested (i.e. unsafe)
     *
     * @param vars
     * @param nValues
     * @param concernedValues
     * @param solver
     */
    private NValues(IntVar[] vars, IntVar nValues, TIntArrayList concernedValues, Solver solver) {
        super(ArrayUtils.append(vars, new IntVar[]{nValues}), solver);
        addPropagators(new PropNValues_Light(vars, concernedValues, nValues));
    }

    /**
     * NValues constraint
     * The number of distinct values in vars is exactly nValues
     *
     * @param vars
     * @param nValues
     * @param solver
     * @param types   additional filtering algorithms to consider
     */
    public NValues(IntVar[] vars, IntVar nValues, Solver solver, Type... types) {
        this(vars, nValues, getDomainUnion(vars), solver);
        for (Type t : types) {
            t.addProp(vars, nValues, this, solver);
        }
    }

    private static TIntArrayList getDomainUnion(IntVar[] vars) {
        TIntArrayList values = new TIntArrayList();
        for (IntVar v : vars) {
            int ub = v.getUB();
            for (int i = v.getLB(); i <= ub; i = v.nextValue(i)) {
                if (!values.contains(i)) {
                    values.add(i);
                }
            }
        }
        return values;
    }

    /**
     * Checks if the constraint is satisfied when all variables are instantiated.
     *
     * @param tuple an complete instantiation
     * @return true iff a solution
     */
    @Override
    public ESat isSatisfied(int[] tuple) {
        int minval = tuple[0];
        for (int i = 0; i < tuple.length - 1; i++) {
            if (minval > tuple[i])
                minval = tuple[i];
        }
        BitSet values = new BitSet(tuple.length - 1);
        for (int i = 0; i < tuple.length - 1; i++) {
            values.set(tuple[i] - minval);
        }
        if (values.cardinality() == tuple[tuple.length - 1]) {
            return ESat.TRUE;
        }
        return ESat.FALSE;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(32);
        sb.append("NValue({");
        for (int i = 0; i < vars.length - 1; i++) {
            if (i > 0) sb.append(", ");
            Variable var = vars[i];
            sb.append(var);
        }
        sb.append("}, " + vars[vars.length - 1]);
        sb.append(")");
        return sb.toString();
    }
}
