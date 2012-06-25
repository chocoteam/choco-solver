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
import solver.variables.IntVar;

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
        if (solver.cachedConstants.containsKey(value)) {
            return solver.cachedConstants.get(value);
        }
        ConstantView cste;
        if (value == 0 || value == 1) {
            cste = new BoolConstantView(name, value, solver);
        } else {
            cste = new ConstantView("cste -- " + value, value, solver);
        }
        solver.cachedConstants.put(value, cste);
        return cste;
    }


    public static IntVar offset(IntVar ivar, int cste) {
        if (cste == 0) {
            return ivar;
        }
        IView[] views = ivar.getViews();
        for (int i = 0; i < views.length; i++) {
            if (views[i] instanceof OffsetView) {
                OffsetView ov = (OffsetView) views[i];
                if (ivar == ov.getVariable() && ov.cste == cste) {
                    return ov;
                }
            }
        }
        return new OffsetView(ivar, cste, ivar.getSolver());
    }

    public static IntVar eq(IntVar ivar) {
        IView[] views = ivar.getViews();
        for (int i = 0; i < views.length; i++) {
            if (views[i] instanceof OffsetView) {
                OffsetView ov = (OffsetView) views[i];
                if (ivar == ov.getVariable() && ov.cste == 0) {
                    return ov;
                }
            }
        }
        return new OffsetView(ivar, 0, ivar.getSolver());
    }

    public static IntVar minus(IntVar ivar) {
        IView[] views = ivar.getViews();
        for (int i = 0; i < views.length; i++) {
            if (views[i] instanceof MinusView) {
                MinusView mv = (MinusView) views[i];
                if (ivar == mv.getVariable()) {
                    return mv;
                }
            }
        }
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
                IView[] views = ivar.getViews();
                for (int i = 0; i < views.length; i++) {
                    if (views[i] instanceof ScaleView) {
                        ScaleView sv = (ScaleView) views[i];
                        if (ivar == sv.getVariable() && sv.cste == cste) {
                            return sv;
                        }
                    }
                }
                var = new ScaleView(ivar, cste, ivar.getSolver());
            }
        }
        return var;
    }

    public static IntVar abs(IntVar ivar) {
        if (ivar.instantiated()) {
            return fixed(Math.abs(ivar.getValue()), ivar.getSolver());
        } else if (ivar.getLB() >= 0) {
            return ivar;
        } else if (ivar.getUB() <= 0) {
            return minus(ivar);
        } else {
            IView[] views = ivar.getViews();
            for (int i = 0; i < views.length; i++) {
                if (views[i] instanceof AbsView) {
                    AbsView av = (AbsView) views[i];
                    if (ivar == av.getVariable()) {
                        return av;
                    }
                }
            }
            return new AbsView(ivar, ivar.getSolver());
        }
    }

    public static IntVar sqr(IntVar ivar) {
        if (ivar.instantiated()) {
            int value = ivar.getValue();
            return fixed(value * value, ivar.getSolver());
        }
        IView[] views = ivar.getViews();
        for (int i = 0; i < views.length; i++) {
            if (views[i] instanceof SqrView) {
                SqrView sv = (SqrView) views[i];
                if (ivar == sv.getVariable()) {
                    return sv;
                }
            }
        }
        return new SqrView(ivar, ivar.getSolver());
    }
}
