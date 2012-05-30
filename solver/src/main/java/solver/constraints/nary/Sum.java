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
import choco.kernel.common.util.iterators.DisposableRangeIterator;
import choco.kernel.common.util.tools.StringUtils;
import choco.kernel.memory.IStateBitSet;
import gnu.trove.map.hash.TObjectIntHashMap;
import solver.Solver;
import solver.constraints.IntConstraint;
import solver.constraints.propagators.nary.sum.*;
import solver.exception.SolverException;
import solver.search.strategy.enumerations.sorters.AbstractSorter;
import solver.search.strategy.enumerations.sorters.Decr;
import solver.search.strategy.enumerations.sorters.Incr;
import solver.search.strategy.enumerations.sorters.Seq;
import solver.search.strategy.enumerations.sorters.metrics.DomSize;
import solver.search.strategy.enumerations.sorters.metrics.IMetric;
import solver.search.strategy.enumerations.sorters.metrics.operators.Div;
import solver.search.strategy.enumerations.values.HeuristicValFactory;
import solver.search.strategy.enumerations.values.heuristics.HeuristicVal;
import solver.variables.IntVar;
import solver.variables.fast.BitsetIntVarImpl;
import solver.variables.fast.IntervalIntVarImpl;
import solver.variables.view.Views;

import java.util.Arrays;

/**
 * <br/>
 * Bounds Consistency Techniques for Long Linear Constraint" </br>
 * W. Harvey and J. Schimpf
 *
 * @author Charles Prud'homme
 * @since 18/03/11
 */
public class Sum extends IntConstraint<IntVar> {

    public static final String
            VAR_DECRCOEFFS = "var_decrcoeffs",
            VAR_DOMOVERCOEFFS = "var_domovercoeffs",
            VAL_TOTO = "domovercoeffs",
            METRIC_COEFFS = "met_coeffs";


    final int[] coeffs;
    final int b;
    final Type op;
    public static boolean incr = false;

    TObjectIntHashMap<IntVar> shared_map; // a shared map for interanl comparator

    public enum Type {
        LEQ, GEQ, EQ, NQ
    }

    protected Sum(IntVar[] vars, int[] coeffs, int pos, Type type, int b, Solver solver) {
        super(vars, solver);
        this.coeffs = coeffs.clone();
        this.b = b;
        this.op = type;

        int l = vars.length;
        IntVar[] x = new IntVar[l];
        int s = 0;
        int i = 0;
        for (; i < pos; i++) {
            if (coeffs[i] != 1) {
                x[s++] = Views.scale(vars[i], coeffs[i]);
            } else {
                x[s++] = vars[i];
            }
        }
        for (int e = l; i < l; i++) {
            if (coeffs[i] != -1) {
                x[--e] = Views.minus(Views.scale(vars[i], -coeffs[i]));
            } else {
                x[--e] = Views.minus(vars[i]);
            }
        }

        switch (type) {
            case LEQ:
                setPropagators(incr ? new PropSumLeqIncr(x, b, solver, this) : new PropSumLeq(x, b, solver, this));
                break;
            case GEQ:
                setPropagators(incr ? new PropSumGeqIncr(x, b, solver, this) : new PropSumGeq(x, b, solver, this));
                break;
            case EQ:
                setPropagators(incr ? new PropSumEqIncr(x, b, solver, this) : new PropSumEq(x, b, solver, this));
                break;
            case NQ:
                setPropagators(new PropSumNeq(x, b, solver, this));
                break;
        }

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////// GENERIC /////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static Sum build(IntVar[] vars, int[] coeffs, Type type, int r, Solver solver) {
        TObjectIntHashMap<IntVar> map = new TObjectIntHashMap<IntVar>();
        for (int i = 0; i < vars.length; i++) {
            map.adjustOrPutValue(vars[i], coeffs[i], coeffs[i]);
            if (map.get(vars[i]) == 0) {
                map.remove(vars[i]);
            }
        }
        int b = 0, e = map.size();
        IntVar[] tmpV = new IntVar[e];
        int[] tmpC = new int[e];
        // to fix determinism in the construction, we iterate over the original array of variables
        for (int i = 0; i < vars.length; i++) {
            IntVar key = vars[i];
            int coeff = map.get(key);
            if (coeff > 0) {
                tmpV[b] = key;
                tmpC[b++] = coeff;
            } else if (coeff < 0) {
                tmpV[--e] = key;
                tmpC[e] = coeff;
            }
            map.adjustValue(key, -coeff); // to avoid multiple occurrence of the variable
        }
        return new Sum(tmpV, tmpC, b, type, r, solver);
    }

    public static Sum build(IntVar[] vars, int c, Type type, Solver solver) {
        int[] coeffs = new int[vars.length];
        Arrays.fill(coeffs, 1);
        return build(vars, coeffs, type, c, solver);
    }

    public static Sum build(IntVar[] vars, IntVar b, Type type, Solver solver) {
        int[] cs = new int[vars.length + 1];
        Arrays.fill(cs, 1);
        cs[vars.length] = -1;
        IntVar[] x = new IntVar[vars.length + 1];
        System.arraycopy(vars, 0, x, 0, vars.length);
        x[vars.length] = b;
        return build(x, cs, type, 0, solver);
    }

    public static Sum build(IntVar[] vars, int[] coeffs, int c, Type type, Solver solver) {
        return build(vars, coeffs, type, c, solver);
    }

    public static Sum build(IntVar[] vars, int[] coeffs, IntVar b, int c, Type type, Solver solver) {
        IntVar[] x = new IntVar[vars.length + 1];
        System.arraycopy(vars, 0, x, 0, vars.length);
        x[x.length - 1] = b;
        int[] cs = new int[coeffs.length + 1];
        System.arraycopy(coeffs, 0, cs, 0, coeffs.length);
        cs[cs.length - 1] = -c;
        return build(x, cs, type, 0, solver);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////// EQ ///////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static Sum eq(IntVar[] vars, int c, Solver solver) {
        return build(vars, c, Type.EQ, solver);
    }

    public static Sum eq(IntVar[] vars, IntVar b, Solver solver) {
        return build(vars, b, Type.EQ, solver);
    }

    public static Sum eq(IntVar[] vars, int[] coeffs, int c, Solver solver) {
        return build(vars, coeffs, Type.EQ, c, solver);
    }

    public static Sum eq(IntVar[] vars, int[] coeffs, IntVar b, int c, Solver solver) {
        return build(vars, coeffs, b, c, Type.EQ, solver);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////// LEQ //////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static Sum leq(IntVar[] vars, int c, Solver solver) {
        return build(vars, c, Type.LEQ, solver);
    }

    public static Sum leq(IntVar[] vars, IntVar b, Solver solver) {
        return build(vars, b, Type.LEQ, solver);
    }

    public static Sum leq(IntVar[] vars, int[] coeffs, int c, Solver solver) {
        return build(vars, coeffs, Type.LEQ, c, solver);
    }

    public static Sum leq(IntVar[] vars, int[] coeffs, IntVar b, int c, Solver solver) {
        return build(vars, coeffs, b, c, Type.LEQ, solver);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////// GEQ //////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static Sum geq(IntVar[] vars, int c, Solver solver) {
        return build(vars, c, Type.GEQ, solver);
    }

    public static Sum geq(IntVar[] vars, IntVar b, Solver solver) {
        return build(vars, b, Type.GEQ, solver);
    }

    public static Sum geq(IntVar[] vars, int[] coeffs, int c, Solver solver) {
        return build(vars, coeffs, Type.GEQ, c, solver);
    }

    public static Sum geq(IntVar[] vars, int[] coeffs, IntVar b, int c, Solver solver) {
        return build(vars, coeffs, b, c, Type.GEQ, solver);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////// NEQ //////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static Sum neq(IntVar[] vars, int c, Solver solver) {
        return build(vars, c, Type.NQ, solver);
    }

    public static Sum neq(IntVar[] vars, IntVar b, Solver solver) {
        return build(vars, b, Type.NQ, solver);
    }

    public static Sum neq(IntVar[] vars, int[] coeffs, int c, Solver solver) {
        return build(vars, coeffs, Type.NQ, c, solver);
    }

    public static Sum neq(IntVar[] vars, int[] coeffs, IntVar b, int c, Solver solver) {
        return build(vars, coeffs, b, c, Type.NQ, solver);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static IntVar var(IntVar a, IntVar b) {
        if (a.instantiated()) {
            if (b.instantiated()) {
                return Views.fixed(a.getValue() + b.getValue(), a.getSolver());
            } else {
                return Views.offset(b, a.getValue());
            }
        } else if (b.instantiated()) {
            return Views.offset(a, b.getValue());
        } else {
            Solver solver = a.getSolver();
            IntVar z;
            //TODO: add a more complex analysis of the build domain
            if (a.hasEnumeratedDomain() || b.hasEnumeratedDomain()) {
                int lbA = a.getLB();
                int ubA = a.getUB();
                int lbB = b.getLB();
                int ubB = b.getUB();
                int OFFSET = lbA + lbB;
                IStateBitSet VALUES = solver.getEnvironment().makeBitSet((ubA + ubB) - (lbA + lbB) + 1);
                DisposableRangeIterator itA = a.getRangeIterator(true);
                DisposableRangeIterator itB = b.getRangeIterator(true);
                while (itA.hasNext()) {
                    itB.bottomUpInit();
                    while (itB.hasNext()) {
                        VALUES.set(itA.min() + itB.min() - OFFSET, itA.max() + itB.max() - OFFSET + 1);
                        itB.next();
                    }
                    itB.dispose();
                    itA.next();
                }
                itA.dispose();
                z = new BitsetIntVarImpl(StringUtils.randomName(), OFFSET, VALUES, solver);
            } else {
                z = new IntervalIntVarImpl(StringUtils.randomName(), a.getLB() + b.getLB(), a.getUB() + b.getUB(), solver);
            }
            solver.post(Sum.eq(new IntVar[]{a, b}, z, solver));
            return z;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public ESat isSatisfied(int[] tuple) {
        int sum = 0;
        for (int i = 0; i < tuple.length; i++) {
            sum += coeffs[i] * tuple[i];
        }
        switch (op) {
            case EQ:
                return ESat.eval(sum == b);
            case GEQ:
                return ESat.eval(sum >= b);
            case LEQ:
                return ESat.eval(sum <= b);
            case NQ:
                return ESat.eval(sum != b);
        }
        return ESat.UNDEFINED;
    }

    @Override
    public String toString() {
        StringBuilder linComb = new StringBuilder(20);
        for (int i = 0; i < coeffs.length; i++) {
            linComb.append(coeffs[i]).append('*').append(vars[i].getName()).append(coeffs[i] < coeffs.length ? " +" : " ");
        }
        switch (op) {
            case EQ:
                linComb.append(" = ");
                break;
            case NQ:
                linComb.append(" =/= ");
                break;
            case GEQ:
                linComb.append(" >= ");
                break;
            case LEQ:
                linComb.append(" <= ");
                break;
        }
        linComb.append(b);
        return linComb.toString();
    }

    @Override
    public AbstractSorter<IntVar> getComparator(String name) {
        if (name.equals(VAR_DECRCOEFFS)) {
            return new Seq<IntVar>(
                    super.getComparator(VAR_DEFAULT),
                    new Decr<IntVar>(new Coeffs(this)));
        } else if (name.equals(VAR_DOMOVERCOEFFS)) {
            return new Seq<IntVar>(
                    super.getComparator(VAR_DEFAULT),
                    new Incr<IntVar>(
                            Div.<IntVar>build(DomSize.build(), new Coeffs(this)))
            );
        }
        return super.getComparator(name);
    }

    @Override
    public HeuristicVal getIterator(String name, IntVar var) {
        if (name.equals(VAL_TOTO)) {
            return HeuristicValFactory.enumVal(var, var.getUB(), -1, var.getLB());
        }
        return super.getIterator(name, var);
    }


    @Override
    public IMetric<IntVar> getMetric(String name) {
        if (name.equals(METRIC_COEFFS)) {
            //TODO: must be composed with BELONG
            return new Coeffs(this);//Belong.build(this);
        }
        throw new SolverException("Unknown comparator name :" + name);
    }

    static class Coeffs implements IMetric<IntVar> {

        TObjectIntHashMap<IntVar> map;

        public Coeffs(Sum sum) {
            if (sum.shared_map == null) {
                sum.shared_map = new TObjectIntHashMap<IntVar>(sum.coeffs.length);
                for (int i = 0; i < sum.vars.length; i++) {
                    sum.shared_map.put(sum.vars[i], sum.coeffs[i]);
                }
            }
            map = sum.shared_map;
        }

        @Override
        public int eval(IntVar var) {
            return map.get(var);
        }
    }
}
