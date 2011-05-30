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

package solver.constraints.propagators.ternary;

import choco.kernel.ESat;
import choco.kernel.common.util.tools.MathUtils;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.exception.SolverException;
import solver.requests.IRequest;
import solver.variables.EventType;
import solver.variables.IntVar;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 26/01/11
 */
public class PropTimes extends Propagator<IntVar> {

    private static final int MAX = Integer.MAX_VALUE - 1, MIN = Integer.MIN_VALUE + 1;

    IntVar v0, v1, v2;

    public PropTimes(IntVar X, IntVar Y, IntVar Z, Solver solver, Constraint<IntVar,
            Propagator<IntVar>> intVarPropagatorConstraint, PropagatorPriority priority, boolean reactOnPromotion) {
        super(new IntVar[]{X, Y, Z}, solver, intVarPropagatorConstraint, priority, reactOnPromotion);
        this.v0 = X;
        this.v1 = Y;
        this.v2 = Z;
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return EventType.INSTANTIATE.mask + EventType.BOUND.mask;
    }

    @Override
    public void propagate() throws ContradictionException {
        filter(0);
        filter(1);
        filter(2);
    }

    @Override
    public void propagateOnRequest(IRequest<IntVar> request, int varIdx, int mask) throws ContradictionException {
        if (EventType.isInstantiate(mask)) {
            this.awakeOnInst(varIdx);
        } else {
            if (EventType.isInclow(mask)) {
                this.awakeOnLow(varIdx);
            }
            if (EventType.isDecupp(mask)) {
                this.awakeOnUpp(varIdx);
            }
        }
    }

    @Override
    public ESat isEntailed() {
        if (isCompletelyInstantiated()) {
            return ESat.eval(v0.getValue() * v1.getValue() == v2.getValue());
        } else if (v2.instantiatedTo(0)) {
            if (v0.instantiatedTo(0) || v1.instantiatedTo(0)) {
                return ESat.TRUE;
            } else if (!(v0.contains(0)) && !(v1.contains(0))) {
                return ESat.FALSE;
            } else {
                return ESat.UNDEFINED;
            }
        } else if (!(v2.contains(0))) {
            if (v0.getUB() < getXminIfNonZero()) {
                return ESat.FALSE;
            } else if (v0.getLB() > getXmaxIfNonZero()) {
                return ESat.FALSE;
            } else if (v1.getUB() < getYminIfNonZero()) {
                return ESat.FALSE;
            } else if (v1.getLB() > getYmaxIfNonZero()) {
                return ESat.FALSE;
            } else {
                return ESat.UNDEFINED;
            }
        } else {
            return ESat.UNDEFINED;
        }
    }

    //****************************************************************************************************************//
    //****************************************************************************************************************//
    //****************************************************************************************************************//

    void awakeOnUpp(int idx) throws ContradictionException {
        if (idx == 0) {
            awakeOnX();
        } else if (idx == 1) {
            awakeOnY();
        } else if (idx == 2) {
            awakeOnZ();
            if (!(v2.contains(0))) {
                int r = (int) Math.min(getZmax(), MAX);
                v2.updateUpperBound(r, this);
            }
        }
    }

    void awakeOnLow(int idx) throws ContradictionException {
        if (idx == 0) {
            awakeOnX();
        } else if (idx == 1) {
            awakeOnY();
        } else if (idx == 2) {
            awakeOnZ();
            if (!(v2.contains(0))) {
                int r = (int) Math.max(getZmin(), MIN);
                v2.updateLowerBound(r, this);
            }
        }
    }

    void awakeOnInst(int vIdx) throws ContradictionException {
        filter(vIdx);
    }

    public void filter(int idx) throws ContradictionException {
        if (idx == 0) {
            awakeOnX();
        } else if (idx == 1) {
            awakeOnY();
        } else if (idx == 2) {
            awakeOnZ();
        }
    }

    /**
     * reaction when X (v0) is updated
     *
     * @throws ContradictionException
     */
    protected void awakeOnX() throws ContradictionException {
        if (v0.instantiatedTo(0)) {
            v2.instantiateTo(0, this);
        }
        if ((v2.instantiatedTo(0)) && (!v0.contains(0))) {
            v1.instantiateTo(0, this);
        } else if (!v2.contains(0)) {
            updateYandX();
        } else if (!(v2.instantiatedTo(0))) {
            shaveOnYandX();
        }
        if (!(v2.instantiatedTo(0))) {
            int r = (int) Math.max(getZmin(), MIN);
            v2.updateLowerBound(r, this);
            r = (int) Math.min(getZmax(), MAX);
            v2.updateUpperBound(r, this);
        }
    }

    protected void awakeOnY() throws ContradictionException {
        if (v1.instantiatedTo(0)) {
            v2.instantiateTo(0, this);
        }
        if ((v2.instantiatedTo(0)) && (!v1.contains(0))) {
            v0.instantiateTo(0, this);
        } else if (!v2.contains(0)) {
            updateXandY();
        } else if (!(v2.instantiatedTo(0))) {
            shaveOnXandY();
        }
        if (!(v2.instantiatedTo(0))) {
            int r = (int) Math.max(getZmin(), MIN);
            v2.updateLowerBound(r, this);
            r = (int) Math.min(getZmax(), MAX);
            v2.updateUpperBound(r, this);
        }
    }

    protected void awakeOnZ() throws ContradictionException {
        if (!(v2.contains(0))) {
            updateX();
            if (updateY()) {
                updateXandY();
            }
        } else if (!(v2.instantiatedTo(0))) {
            shaveOnX();
            if (shaveOnY()) {
                shaveOnXandY();
            }
        }
        if (v2.instantiatedTo(0)) {
            propagateZero();
        }
    }


    protected long getXminIfNonZero() {
        if ((v2.getLB() >= 0) && (v1.getLB() >= 0)) {
            return infCeilmM(v2, v1);
        } else if ((v2.getUB() <= 0) && (v1.getUB() <= 0)) {
            return infCeilMm(v2, v1);
        } else if ((v2.getLB() >= 0) && (v1.getUB() <= 0)) {
            return infCeilMM(v2, v1);
        } else if ((v2.getUB() <= 0) && (v1.getLB() >= 0)) {
            return infCeilmm(v2, v1);
        } else if ((v2.getLB() <= 0) && (v2.getUB() >= 0) && (v1.getUB() <= 0)) {
            return infCeilMM(v2, v1);
        } else if ((v2.getUB() <= 0) && (v1.getLB() <= 0) && (v1.getUB() >= 0)) {
            return infCeilmP(v2);
        } else if ((v2.getLB() <= 0) && (v2.getUB() >= 0) && (v1.getLB() >= 0)) {
            return infCeilmm(v2, v1);
        } else if ((v2.getLB() >= 0) && (v1.getLB() <= 0) && (v1.getUB() >= 0)) {
            return infCeilMN(v2);
        } else if ((v2.getLB() <= 0) && (v2.getUB() >= 0) && (v1.getLB() <= 0) && (v1.getUB() >= 0)) {
            return infCeilxx(v2);
        } else {
            throw new SolverException("None of the cases is active!");
        }
    }

    protected long getXmaxIfNonZero() {
        if ((v2.getLB() >= 0) && (v1.getLB() >= 0)) {
            return supCeilMm(v2, v1);
        } else if ((v2.getUB() <= 0) && (v1.getUB() <= 0)) {
            return supCeilmM(v2, v1);
        } else if ((v2.getLB() >= 0) && (v1.getUB() <= 0)) {
            return supCeilmm(v2, v1);
        } else if ((v2.getUB() <= 0) && (v1.getLB() >= 0)) {
            return supCeilMM(v2, v1);
        } else if ((v2.getLB() <= 0) && (v2.getUB() >= 0) && (v1.getUB() <= 0)) {
            return supCeilmM(v2, v1);
        } else if ((v2.getUB() <= 0) && (v1.getLB() <= 0) && (v1.getUB() >= 0)) {
            return supCeilmN(v2);
        } else if ((v2.getLB() <= 0) && (v2.getUB() >= 0) && (v1.getLB() >= 0)) {
            return supCeilMm(v2, v1);
        } else if ((v2.getLB() >= 0) && (v1.getLB() <= 0) && (v1.getUB() >= 0)) {
            return supCeilMP(v2);
        } else if ((v2.getLB() <= 0) && (v2.getUB() >= 0) && (v1.getLB() <= 0) && (v1.getUB() >= 0)) {
            return supCeilEq(v2);
        } else {
            throw new SolverException("None of the cases is active!");
        }
    }

    protected long getYminIfNonZero() {
        if ((v2.getLB() >= 0) && (v0.getLB() >= 0)) {
            return infCeilmM(v2, v0);
        } else if ((v2.getUB() <= 0) && (v0.getUB() <= 0)) {
            return infCeilMm(v2, v0);
        } else if ((v2.getLB() >= 0) && (v0.getUB() <= 0)) {
            return infCeilMM(v2, v0);
        } else if ((v2.getUB() <= 0) && (v0.getLB() >= 0)) {
            return infCeilmm(v2, v0);
        } else if ((v2.getLB() <= 0) && (v2.getUB() >= 0) && (v0.getUB() <= 0)) {
            return infCeilMM(v2, v0);
        } else if ((v2.getUB() <= 0) && (v0.getLB() <= 0) && (v0.getUB() >= 0)) {
            return infCeilmP(v2);
        } else if ((v2.getLB() <= 0) && (v2.getUB() >= 0) && (v0.getLB() >= 0)) {
            return infCeilmm(v2, v0);
        } else if ((v2.getLB() >= 0) && (v0.getLB() <= 0) && (v0.getUB() >= 0)) {
            return infCeilMN(v2);
        } else if ((v2.getLB() <= 0) && (v2.getUB() >= 0) && (v0.getLB() <= 0) && (v0.getUB() >= 0)) {
            return infCeilxx(v2);
        } else {
            throw new SolverException("None of the cases is active!");
        }
    }

    protected long getYmaxIfNonZero() {
        if ((v2.getLB() >= 0) && (v0.getLB() >= 0)) {
            return supCeilMm(v2, v0);
        } else if ((v2.getUB() <= 0) && (v0.getUB() <= 0)) {
            return supCeilmM(v2, v0);
        } else if ((v2.getLB() >= 0) && (v0.getUB() <= 0)) {
            return supCeilmm(v2, v0);
        } else if ((v2.getUB() <= 0) && (v0.getLB() >= 0)) {
            return supCeilMM(v2, v0);
        } else if ((v2.getLB() <= 0) && (v2.getUB() >= 0) && (v0.getUB() <= 0)) {
            return supCeilmM(v2, v0);
        } else if ((v2.getUB() <= 0) && (v0.getLB() <= 0) && (v0.getUB() >= 0)) {
            return supCeilmN(v2);
        } else if ((v2.getLB() <= 0) && (v2.getUB() >= 0) && (v0.getLB() >= 0)) {
            return supCeilMm(v2, v0);
        } else if ((v2.getLB() >= 0) && (v0.getLB() <= 0) && (v0.getUB() >= 0)) {
            return supCeilMP(v2);
        } else if ((v2.getLB() <= 0) && (v2.getUB() >= 0) && (v0.getLB() <= 0) && (v0.getUB() >= 0)) {
            return supCeilEq(v2);
        } else {
            throw new SolverException("None of the cases is active!");
        }
    }

    protected long getZmin() {
        if ((v0.getLB() >= 0) && (v1.getLB() >= 0)) {
            return infFloormm(v0, v1);
        } else if ((v0.getUB() <= 0) && (v1.getUB() <= 0)) {
            return infFloorMM(v0, v1);
        } else if ((v0.getLB() >= 0) && (v1.getUB() <= 0)) {
            return infFloorMm(v0, v1);
        } else if ((v0.getUB() <= 0) && (v1.getLB() >= 0)) {
            return infFloormM(v0, v1);
        } else if ((v0.getLB() <= 0) && (v0.getUB() >= 0) && (v1.getUB() <= 0)) {
            return infFloorMm(v0, v1);
        } else if ((v0.getUB() <= 0) && (v1.getLB() <= 0) && (v1.getUB() >= 0)) {
            return infFloormM(v0, v1);
        } else if ((v0.getLB() <= 0) && (v0.getUB() >= 0) && (v1.getLB() >= 0)) {
            return infFloormM(v0, v1);
        } else if ((v0.getLB() >= 0) && (v1.getLB() <= 0) && (v1.getUB() >= 0)) {
            return infFloorMm(v0, v1);
        } else if ((v0.getLB() <= 0) && (v0.getUB() >= 0) && (v1.getLB() <= 0) && (v1.getUB() >= 0)) {
            return infFloorxx(v0, v1);
        } else {
            throw new SolverException("None of the cases is active!");
        }
    }

    protected long getZmax() {
        if ((v0.getLB() >= 0) && (v1.getLB() >= 0)) {
            return supFloorMM(v0, v1);
        } else if ((v0.getUB() <= 0) && (v1.getUB() <= 0)) {
            return supFloormm(v0, v1);
        } else if ((v0.getLB() >= 0) && (v1.getUB() <= 0)) {
            return supFloormM(v0, v1);
        } else if ((v0.getUB() <= 0) && (v1.getLB() >= 0)) {
            return supFloorMm(v0, v1);
        } else if ((v0.getLB() <= 0) && (v0.getUB() >= 0) && (v1.getUB() <= 0)) {
            return supFloormm(v0, v1);
        } else if ((v0.getUB() <= 0) && (v1.getLB() <= 0) && (v1.getUB() >= 0)) {
            return supFloormm(v0, v1);
        } else if ((v0.getLB() <= 0) && (v0.getUB() >= 0) && (v1.getLB() >= 0)) {
            return supFloorMM(v0, v1);
        } else if ((v0.getLB() >= 0) && (v1.getLB() <= 0) && (v1.getUB() >= 0)) {
            return supFloorMM(v0, v1);
        } else if ((v0.getLB() <= 0) && (v0.getUB() >= 0) && (v1.getLB() <= 0) && (v1.getUB() >= 0)) {
            return supFloorEq(v0, v1);
        } else {
            throw new SolverException("None of the cases is active!");
        }
    }

    private long infFloormm(IntVar b, IntVar c) {
        return b.getLB() * c.getLB();
    }

    private long infFloormM(IntVar b, IntVar c) {
        return b.getLB() * c.getUB();
    }

    private long infFloorMm(IntVar b, IntVar c) {
        return b.getUB() * c.getLB();
    }

    private long infFloorMM(IntVar b, IntVar c) {
        return b.getUB() * c.getUB();
    }

    private long supFloormm(IntVar b, IntVar c) {
        return b.getLB() * c.getLB();
    }

    private long supFloormM(IntVar b, IntVar c) {
        return b.getLB() * c.getUB();
    }

    private long supFloorMm(IntVar b, IntVar c) {
        return b.getUB() * c.getLB();
    }

    private long supFloorMM(IntVar b, IntVar c) {
        return b.getUB() * c.getUB();
    }

    private int getNonZeroSup(IntVar v) {
        return Math.min(v.getUB(), -1);
    }

    private int getNonZeroInf(IntVar v) {
        return Math.max(v.getLB(), 1);
    }

    private long infCeilmm(IntVar b, IntVar c) {
        return MathUtils.divCeil(b.getLB(), getNonZeroInf(c));
    }

    private long infCeilmM(IntVar b, IntVar c) {
        return MathUtils.divCeil(getNonZeroInf(b), c.getUB());
    }

    private long infCeilMm(IntVar b, IntVar c) {
        return MathUtils.divCeil(getNonZeroSup(b), c.getLB());
    }

    private long infCeilMM(IntVar b, IntVar c) {
        return MathUtils.divCeil(b.getUB(), getNonZeroSup(c));
    }

    private long infCeilmP(IntVar b) {
        return MathUtils.divCeil(b.getLB(), 1);
    }

    private long infCeilMN(IntVar b) {
        return MathUtils.divCeil(b.getUB(), -1);
    }

    private long supCeilmm(IntVar b, IntVar c) {
        return MathUtils.divFloor(getNonZeroInf(b), c.getLB());
    }

    private long supCeilmM(IntVar b, IntVar c) {
        return MathUtils.divFloor(b.getLB(), getNonZeroSup(c));
    }

    private long supCeilMm(IntVar b, IntVar c) {
        return MathUtils.divFloor(b.getUB(), getNonZeroInf(c));
    }

    private long supCeilMM(IntVar b, IntVar c) {
        return MathUtils.divFloor(getNonZeroSup(b), c.getUB());
    }

    private long supCeilmN(IntVar b) {
        return MathUtils.divFloor(b.getLB(), -1);
    }

    private long supCeilMP(IntVar b) {
        return MathUtils.divFloor(b.getUB(), 1);
    }

    private long infFloorxx(IntVar b, IntVar c) {
        long s1 = b.getLB() * c.getUB();
        long s2 = b.getUB() * c.getLB();
        if (s1 < s2) {
            return b.getLB() * c.getUB();
        } else {
            return b.getUB() * c.getLB();
        }
    }

    private long supFloorEq(IntVar b, IntVar c) {
        if (b.getLB() * c.getLB() > b.getUB() * c.getUB()) {
            return b.getLB() * c.getLB();
        } else {
            return b.getUB() * c.getUB();
        }
    }

    private long infCeilxx(IntVar b) {
        return Math.min(MathUtils.divCeil(b.getLB(), 1), MathUtils.divCeil(b.getUB(), -1));
    }  //v0.18

    private long supCeilEq(IntVar b) {
        return Math.max(MathUtils.divFloor(b.getLB(), -1), MathUtils.divFloor(b.getUB(), 1));
    }   //v0.18


    /**
     * propagate the fact that v2 (Z) is instantiateTod to 0
     *
     * @throws ContradictionException
     */
    public void propagateZero() throws ContradictionException {
        if (!(v1.contains(0))) {
            v0.instantiateTo(0, this);
        }
        if (!(v0.contains(0))) {
            v1.instantiateTo(0, this);
        }
    }

    /**
     * Updating X and Y when Z cannot be 0
     */
    protected boolean updateX() throws ContradictionException {
        int r = (int) Math.max(getXminIfNonZero(), MIN);
        boolean infChange = v0.updateLowerBound(r, this);
        r = (int) Math.min(getXmaxIfNonZero(), MAX);
        boolean supChange = v0.updateUpperBound(r, this);
        return (infChange || supChange);
    }

    protected boolean updateY() throws ContradictionException {
        int r = (int) Math.max(getYminIfNonZero(), MIN);
        boolean infChange = v1.updateLowerBound(r, this);
        r = (int) Math.min(getYmaxIfNonZero(), MAX);
        boolean supChange = v1.updateUpperBound(r, this);
        return (infChange || supChange);
    }

    /**
     * loop until a fix point is reach (see testProd14)
     */
    protected void updateXandY() throws ContradictionException {
        while (updateX() && updateY()) ;
    }

    protected void updateYandX() throws ContradictionException {
        while (updateY() && updateX()) ;
    }

    /**
     * Updating X and Y when Z can  be 0
     */

    protected boolean shaveOnX() throws ContradictionException {
        int xmin = (int) Math.max(getXminIfNonZero(), MIN);
        int xmax = (int) Math.min(getXmaxIfNonZero(), MAX);
        if ((xmin > v0.getUB()) || (xmax < v0.getLB())) {
            v2.instantiateTo(0, this);
            propagateZero();    // make one of X,Y be 0 if the other cannot be
            return false;       //no more shaving need to be performed
        } else {
            boolean infChange = (!(v1.contains(0)) && v0.updateLowerBound(Math.min(0, xmin), this));
            boolean supChange = (!(v1.contains(0)) && v0.updateUpperBound(Math.max(0, xmax), this));
            return (infChange || supChange);
        }
    }

    protected boolean shaveOnY() throws ContradictionException {
        int ymin = (int) Math.max(getYminIfNonZero(), MIN);
        int ymax = (int) Math.min(getYmaxIfNonZero(), MAX);
        if ((ymin > v1.getUB()) || (ymax < v1.getLB())) {
            v2.instantiateTo(0, this);
            propagateZero();    // make one of X,Y be 0 if the other cannot be
            return false;       //no more shaving need to be performed
        } else {
            boolean infChange = (!(v0.contains(0)) && v1.updateLowerBound(Math.min(0, ymin), this));
            boolean supChange = (!(v0.contains(0)) && v1.updateUpperBound(Math.max(0, ymax), this));
            return (infChange || supChange);
        }
    }

    protected void shaveOnXandY() throws ContradictionException {
        while (shaveOnX() && shaveOnY()) {
        }
    }

    protected void shaveOnYandX() throws ContradictionException {
        while (shaveOnY() && shaveOnX()) {
        }
    }

}
