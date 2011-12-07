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

package solver.constraints.propagators.nary;

import choco.kernel.ESat;
import choco.kernel.common.util.procedure.UnaryIntProcedure;
import choco.kernel.common.util.tools.ArrayUtils;
import solver.Solver;
import solver.constraints.IntConstraint;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.requests.IRequest;
import solver.variables.EventType;
import solver.variables.IntVar;

/**
 * (X[i] = j' + Ox && j = j' + Ox) <=> (Y[j] = i' + Oy[j]  && i = i' + Oy[j])
 * <p/>
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 30 sept. 2010
 */
@SuppressWarnings({"UnnecessaryLocalVariable"})
public class PropInverseChanneling extends Propagator<IntVar> {

    private static final int STOP = 0, ON_X = 1, ON_Y = 2;

    protected final int Ox, Oy;

    protected final int nbX, nbY;

    protected IntVar[] X, Y;

    protected final RemProc rem_proc;

    @SuppressWarnings({"unchecked"})
    public PropInverseChanneling(IntVar[] X, IntVar[] Y, int Ox, int Oy, Solver solver, IntConstraint constraint) {
        super(ArrayUtils.append(X, Y), solver, constraint, PropagatorPriority.CUBIC, false);
        this.X = X;
        this.Y = Y;
        nbX = X.length;
        nbY = Y.length;
        this.Ox = Ox;
        this.Oy = Oy;
        rem_proc = new RemProc(this);
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        if (vars[vIdx].hasEnumeratedDomain()) {
            return EventType.INT_ALL_MASK();
        } else {
            return EventType.INSTANTIATE.mask + EventType.BOUND.mask;
        }
    }


    void awakeOnInst(int index) throws ContradictionException {
        if (filter(index)) {
            int step = (index < nbX ? ON_Y : ON_X);
            fixPoint(step);
        }
    }

    void awakeOnRem(int index, int val) throws ContradictionException {
        // X[i] =\= j' && j' = j - Ox[i]  => Y[j] =\= i - Oy[j]
        if (index < nbX) {
            int i = index;
            int j = val + Ox;
            if (0 <= j && j < nbY) {
                Y[j].removeValue(i - Oy, this, false);
                if (Y[j].instantiated()) {
                    awakeOnInst(j + nbX);
                }
            }
        }
        // Y[j] =\= i' && i' = i - Oy[j] => X[i] =\= j - Ox[i]
        else {
            int j = index - nbX;
            int i = val + Oy;
            if (0 <= i && i < nbX) {
                X[i].removeValue(j - Ox, this, false);
                if (X[i].instantiated()) {
                    awakeOnInst(i);
                }
            }
        }
    }

    @Override
    public void propagate() throws ContradictionException {
        adjust();
        checkAllY();
        int step = (checkAllX() ? ON_Y : STOP);
        fixPoint(step);
    }

    @Override
    public void propagateOnRequest(IRequest<IntVar> request, int varIdx, int mask) throws ContradictionException {

        if (EventType.isInstantiate(mask)) {
            this.awakeOnInst(varIdx);
        } else {
            request.forEach(rem_proc.set(varIdx));
        }
    }

    private void adjust() throws ContradictionException {
        int left, right;
        // X[i] = j' && j' = j - Ox[i] => 0 <= j < nbY
        for (int i = 0; i < nbX; i++) {
            left = right = Integer.MIN_VALUE;
            int ub = X[i].getUB();
            for (int val = X[i].getLB(); val <= ub; val = X[i].nextValue(val)) {
                int j = val + Ox;
                if (j < 0 || j >= nbY) {
                    if (val == right + 1) {
                        right = val;
                    } else {
                        X[i].removeInterval(left, right, this, false);
                        left = right = val;
                    }
                }
            }
            X[i].removeInterval(left, right, this, false);
        }
        // Y[j] = i' && i' = i - Oy[j] => 0 <= i < nbX
        for (int j = 0; j < nbY; j++) {
            left = right = Integer.MIN_VALUE;
            int ub = Y[j].getUB();
            for (int val = Y[j].getLB(); val <= ub; val = Y[j].nextValue(val)) {
                int i = val + Oy;
                if (i < 0 || i >= nbX) {
                    if (val == right + 1) {
                        right = val;
                    } else {
                        Y[j].removeInterval(left, right, this, false);
                        left = right = val;
                    }
                }
            }
            Y[j].removeInterval(left, right, this, false);
        }
    }

    private boolean filter(int index) throws ContradictionException {
        boolean modified;
        // X[i] = j' && j' = j - Ox[i] => Y[j] = i - Oy[j]
        if (index < nbX) {
            int i = index;
            int j = X[i].getValue() + Ox;
            modified = Y[j].instantiateTo(i - Oy, this, false);
            // j" =\= j, Y[j"] =\= i - Oy[j"]
            for (int jj = 0; jj < nbY; jj++) {
                if (jj != j) {
                    modified |= Y[jj].removeValue(i - Oy, this, false);
                }
            }
        }
        // Y[j] = i' && i' = i - Oy[j] => X[i] = j - Ox[i]
        else {
            int j = index - nbX;
            int i = Y[j].getValue() + Oy;
            modified = X[i].instantiateTo(j - Ox, this, false);
            // i" =\= i, X[i"] =\= j - Ox[i"]
            for (int ii = 0; ii < nbX; ii++) {
                if (ii != i) {
                    modified |= X[ii].removeValue(j - Ox, this, false);
                }
            }
        }
        return modified;
    }

    private void fixPoint(int from) throws ContradictionException {
        int step = from;
        while (step > STOP) {
            switch (step) {
                case ON_X:
                    step = (checkAllX() ? ON_Y : STOP);
                    break;
                case ON_Y:
                    step = (checkAllY() ? ON_X : STOP);
                    break;
            }
        }
    }

    private boolean checkAllX() throws ContradictionException {
        boolean modified = false;
        for (int i = 0; i < nbX; i++) {
            modified |= checkX(i);
        }
        return modified;
    }

    private boolean checkX(int i) throws ContradictionException {
        boolean modified = false;
        if (X[i].instantiated()) {
            modified = filter(i);
        } else {
            // 0 < j < Y, j = j' + Ox[i], X[i] =\= j' => Y[j] =\= i + Oy[j]
            for (int j = 0; j < nbY; j++) {
                if (!X[i].contains(j - Ox)) {
                    modified |= Y[j].removeValue(i - Oy, this, false);
                }
            }
        }
        return modified;
    }

    private boolean checkAllY() throws ContradictionException {
        boolean modified = false;
        for (int j = 0; j < nbY; j++) {
            modified |= checkY(j);
        }
        return modified;
    }

    private boolean checkY(int j) throws ContradictionException {
        boolean modified = false;
        if (Y[j].instantiated()) {
            modified = filter(j + nbX);
        } else {
            // 0 < i < X, i = i' + Oy[j], Y[j] =\= i' => X[i] =\= j + Ox[i]
            for (int i = 0; i < nbX; i++) {
                if (!Y[j].contains(i - Oy)) {
                    modified |= X[i].removeValue(j - Ox, this, false);
                }
            }
        }
        return modified;
    }

    @Override
    public ESat isEntailed() {
        if (isCompletelyInstantiated()) {
            for (int i = 0; i < nbX; i++) {
                if (X[i].instantiated()) {
                    int j = X[i].getValue() + Ox;
                    if (j < 0 || j > nbY) {
                        return ESat.FALSE;
                    }
                    if (Y[j].instantiated()) {
                        if (Y[j].getValue() != (i - Oy)) {
                            return ESat.FALSE;
                        }
                    } else {
                        return ESat.UNDEFINED;
                    }
                } else {
                    return ESat.UNDEFINED;
                }
            }
            return ESat.TRUE;
        } else {
            return ESat.UNDEFINED;
        }
    }


    private static class RemProc implements UnaryIntProcedure<Integer> {

        private final PropInverseChanneling p;
        private int idxVar;

        public RemProc(PropInverseChanneling p) {
            this.p = p;
        }

        @Override
        public UnaryIntProcedure set(Integer idxVar) {
            this.idxVar = idxVar;
            return this;
        }

        @Override
        public void execute(int i) throws ContradictionException {
            p.awakeOnRem(idxVar, i);
        }
    }
}
