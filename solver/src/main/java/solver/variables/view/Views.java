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
package solver.variables.view;

import choco.kernel.common.util.iterators.DisposableRangeIterator;
import choco.kernel.common.util.tools.StringUtils;
import choco.kernel.memory.IStateBitSet;
import solver.Solver;
import solver.constraints.nary.Sum;
import solver.constraints.ternary.MaxXYZ;
import solver.variables.IntVar;
import solver.variables.fast.BitsetIntVarImpl;
import solver.variables.fast.IntervalIntVarImpl;

/**
 * Factory to build views.
 * <p/>
 * Based on "Views and Iterators for Generic Constraint Implementations",
 * C. Schulte and G. Tack
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 23/08/11
 */
public enum Views {
    ;

    public static IntVar fixed(int value, Solver solver) {
        return fixed("cste -- " + value, value, solver);
    }

    public static IntVar fixed(String name, int value, Solver solver) {
        if (value == 0 || value == 1) {
            //            solver.associates(var);
            return new BoolConstantView(name, value, solver);
        } else {
            //            solver.associates(var);
            return new ConstantView("cste -- " + value, value, solver);
        }
    }

    public static IntVar offset(IntVar ivar, int cste) {
        return new OffsetView(ivar, cste, ivar.getSolver());
    }

    public static IntVar minus(IntVar ivar) {
        return new MinusView(ivar, ivar.getSolver());
    }

    public static IntVar scale(IntVar ivar, int cste) {
        IntVar var;
        if (cste < 0) {
            throw new UnsupportedOperationException("scale required positive coefficient!");
        } else {
            if (cste == 1) {
                var = ivar;
            } else if (cste == -1) {
                var = Views.minus(ivar);
            } else {
                var = new ScaleView(ivar, cste, ivar.getSolver());
            }
        }
        return var;
    }

    public static IntVar abs(IntVar ivar) {
        return new AbsView(ivar, ivar.getSolver());
    }

    public static IntVar sqr(IntVar ivar) {
        return new SqrView(ivar, ivar.getSolver());
    }

    public static IntVar sum(IntVar a, IntVar b) {
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

    public static IntVar max(IntVar a, IntVar b) {
        Solver solver = a.getSolver();
        IntVar z = new IntervalIntVarImpl(StringUtils.randomName(),
                Math.max(a.getLB(),  b.getLB()), Math.max(a.getUB(),b.getUB()), solver);
        solver.post(new MaxXYZ(a,b, z, solver));
        return z;
    }
}
