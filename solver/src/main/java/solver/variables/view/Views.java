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

import solver.Solver;
import solver.variables.IntCste;
import solver.variables.IntVar;

/**
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
            return new BoolCste(name, value, solver);
        } else {
            //            solver.associates(var);
            return new IntCste("cste -- " + value, value, solver);
        }
    }

    public static IntVar offset(IntVar ivar, int cste) {
        return new IntVarAddCste(ivar, cste, ivar.getSolver());
    }

    public static IntVar minus(IntVar ivar) {
        return new ViewMinus(ivar, ivar.getSolver());
    }

    public static IntVar scale(IntVar ivar, int cste) {
        IntVar var;
        if (cste < 0) {
            throw new UnsupportedOperationException("scale required positive coefficient!");
        } else {
            var = new IntVarTimesPosCste(ivar, cste, ivar.getSolver());
        }
        return var;
    }

    public static IntVar abs(IntVar ivar) {
        return new IntVarAbs(ivar, ivar.getSolver());
    }

    public static IntVar sum(IntVar a, IntVar b) {
        if (a.hasEnumeratedDomain() || b.hasEnumeratedDomain()) {
            return new BitsetXYSumView(a, b, a.getSolver());
        } else {
            return new IntervalXYSumView(a, b, a.getSolver());
        }
    }
}
