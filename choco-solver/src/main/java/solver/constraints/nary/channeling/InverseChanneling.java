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

package solver.constraints.nary.channeling;

import solver.Solver;
import solver.constraints.IntConstraint;
import solver.constraints.nary.alldifferent.AllDifferent;
import solver.constraints.nary.alldifferent.PropAllDiffBC;
import solver.variables.IntVar;
import util.ESat;
import util.tools.ArrayUtils;

/**
 * X[i] = j+Ox <=> Y[j] = i+Oy
 * <p/>
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 30 sept. 2010
 */
public class InverseChanneling extends IntConstraint {

    protected IntVar[] X, Y;
    protected final int n, minX, minY;

    /**
     * Make an inverse channeling between X and Y:
     * X[i] = j+Ox <=> Y[j] = i+Oy
     * Could performs AC if domains are enumerated (but it currently uses the DEFAULT alldifferent
	 * which is between BC and AC for speed up purpose),
     * If not, then it works on bounds without guaranteeing BC
     * Indeed, it would require to know somehow holes in (bounded) domains
     * (enumerated domains are strongly recommended)
     * The constraint requires that |X| = |Y|
     *
     * @param X
     * @param Y
     * @param minX   lowest value in X:
     *               usually 0 but if you start counting at 1 (which is the case in minizinc),
     *               then this should be set to 1)
     * @param minY   lowest value in Y
     * @param solver
     */
    public InverseChanneling(IntVar[] X, IntVar[] Y, int minX, int minY, Solver solver) {
        super(ArrayUtils.append(X, Y), solver);
        this.X = X.clone();
        this.Y = Y.clone();
        if (X.length != Y.length) throw new UnsupportedOperationException(X + " and " + Y + " should have same size");
        n = Y.length;
        this.minX = minX;
        this.minY = minY;
        if (allEnumerated(X, Y)) {
			addPropagators(AllDifferent.createPropagators(this.X, AllDifferent.Type.AC));
			addPropagators(AllDifferent.createPropagators(this.Y, AllDifferent.Type.AC));
            addPropagators(new PropInverseChannelAC(this.X, this.Y, minX, minY));
        } else {// Beware no BC on the conjunction of those propagators but only separately
            addPropagators(new PropAllDiffBC(this.X, false));
            addPropagators(new PropAllDiffBC(this.Y, false));
            addPropagators(new PropInverseChannelBC(this.X, this.Y, minX, minY));
        }
    }

    private static boolean allEnumerated(IntVar[] X, IntVar[] Y) {
        for (int i = 0; i < X.length; i++) {
            if (!(X[i].hasEnumeratedDomain() && Y[i].hasEnumeratedDomain())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if the constraint is satisfied when all variables are instantiated.
     *
     * @param tuple an complete instantiation
     * @return true iff a solution
     */
    @Override
    public ESat isSatisfied(int[] tuple) {
        for (int i = 0; i < n; i++) {
            int j = tuple[i] - minX;
            if (tuple[j] != (i + minY)) {
                return ESat.FALSE;
            }
        }
        return ESat.TRUE;
    }

    @Override
    public ESat isSatisfied() {
        boolean allInst = true;
        for (int i = 0; i < n; i++) {
            if (!(vars[i].instantiated() && vars[i + n].instantiated())) {
                allInst = false;
            }
            if (X[i].instantiated() && !Y[X[i].getValue() - minX].contains(i + minY)) {
                return ESat.FALSE;
            }
            if (Y[i].instantiated() && !X[Y[i].getValue() - minY].contains(i + minX)) {
                return ESat.FALSE;
            }
        }
        if (allInst) return ESat.TRUE;
        return ESat.UNDEFINED;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("InverseChanneling({");
        for (int i = 0; i < n; i++) {
            if (i > 0) sb.append(", ");
            sb.append(X[i]);
        }
        sb.append("}, {");
        for (int i = 0; i < n; i++) {
            if (i > 0) sb.append(", ");
            sb.append(Y[i]);
        }
        sb.append("})");
        return sb.toString();
    }

}
