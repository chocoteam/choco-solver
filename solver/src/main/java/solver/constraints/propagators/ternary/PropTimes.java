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
import choco.kernel.memory.IEnvironment;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.exception.SolverException;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.views.IView;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 26/01/11
 */
public class PropTimes extends Propagator<IntVar> {

    IntVar v0, v1, v2;

    public PropTimes(IntVar X, IntVar Y, IntVar Z, IEnvironment environment, Constraint<IntVar, Propagator<IntVar>> intVarPropagatorConstraint, PropagatorPriority priority, boolean reactOnPromotion) {
        super(new IntVar[]{X, Y, Z}, environment, intVarPropagatorConstraint, priority, reactOnPromotion);
        this.v0 = X;
        this.v1 = Y;
        this.v2 = Z;
    }

    @Override
    public int getPropagationConditions() {
        return EventType.INSTANTIATE.mask + EventType.BOUND.mask;
    }

    @Override
    public void propagate() throws ContradictionException {
        filter(0);
        filter(1);
        filter(2);
    }

    @Override
    public void propagateOnView(IView<IntVar> view, int varIdx, int mask) throws ContradictionException {
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

    protected void awakeOnInst(int vIdx) throws ContradictionException {
        filter(vIdx);
    }

    protected void awakeOnUpp(int idx) throws ContradictionException {
        if (idx == 0) {
            awakeOnX();
        } else if (idx == 1) {
            awakeOnY();
        } else if (idx == 2) {
            awakeOnZ();
            if (!(v2.contains(0))) {
                v2.updateUpperBound(getZmax(), this);
            }
        }
    }

    protected void awakeOnLow(int idx) throws ContradictionException {
        if (idx == 0) {
            awakeOnX();
        } else if (idx == 1) {
            awakeOnY();
        } else if (idx == 2) {
            awakeOnZ();
            if (!(v2.contains(0))) {
                v2.updateLowerBound(getZmin(), this);
            }
        }
    }

    protected void filter(int idx) throws ContradictionException {
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
            v2.updateLowerBound(getZmin(), this);
            v2.updateUpperBound(getZmax(), this);
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
            v2.updateLowerBound(getZmin(), this);
            v2.updateUpperBound(getZmax(), this);
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

    protected int getXminIfNonZero() {
        if ((v2.getLB() >= 0) && (v1.getLB() >= 0)) {
            return ruleA1(v2, v1);
        } else if ((v2.getUB() <= 0) && (v1.getUB() <= 0)) {
            return ruleB1(v2, v1);
        } else if ((v2.getLB() >= 0) && (v1.getUB() <= 0)) {
            return ruleC1(v2, v1);
        } else if ((v2.getUB() <= 0) && (v1.getLB() >= 0)) {
            return ruleD1(v2, v1);
        } else if ((v2.getLB() <= 0) && (v2.getUB() >= 0) && (v1.getUB() <= 0)) {
            return ruleE1(v2, v1);
        } else if ((v2.getUB() <= 0) && (v1.getLB() <= 0) && (v1.getUB() >= 0)) {
            return ruleF1(v2);
        } else if ((v2.getLB() <= 0) && (v2.getUB() >= 0) && (v1.getLB() >= 0)) {
            return ruleG1(v2, v1);
        } else if ((v2.getLB() >= 0) && (v1.getLB() <= 0) && (v1.getUB() >= 0)) {
            return ruleH1(v2);
        } else if ((v2.getLB() <= 0) && (v2.getUB() >= 0) && (v1.getLB() <= 0) && (v1.getUB() >= 0)) {
            return ruleI1(v2);
        } else {
            throw new SolverException("None of the cases is active!");
        }
    }

    protected int getXmaxIfNonZero() {
        if ((v2.getLB() >= 0) && (v1.getLB() >= 0)) {
            return ruleA2(v2, v1);
        } else if ((v2.getUB() <= 0) && (v1.getUB() <= 0)) {
            return ruleB2(v2, v1);
        } else if ((v2.getLB() >= 0) && (v1.getUB() <= 0)) {
            return ruleC2(v2, v1);
        } else if ((v2.getUB() <= 0) && (v1.getLB() >= 0)) {
            return ruleD2(v2, v1);
        } else if ((v2.getLB() <= 0) && (v2.getUB() >= 0) && (v1.getUB() <= 0)) {
            return ruleE2(v2, v1);
        } else if ((v2.getUB() <= 0) && (v1.getLB() <= 0) && (v1.getUB() >= 0)) {
            return ruleF2(v2);
        } else if ((v2.getLB() <= 0) && (v2.getUB() >= 0) && (v1.getLB() >= 0)) {
            return ruleG2(v2, v1);
        } else if ((v2.getLB() >= 0) && (v1.getLB() <= 0) && (v1.getUB() >= 0)) {
            return ruleH2(v2);
        } else if ((v2.getLB() <= 0) && (v2.getUB() >= 0) && (v1.getLB() <= 0) && (v1.getUB() >= 0)) {
            return ruleI2(v2);
        } else {
            throw new SolverException("None of the cases is active!");
        }
    }

    protected int getYminIfNonZero() {
        if ((v2.getLB() >= 0) && (v0.getLB() >= 0)) {
            return ruleA1(v2, v0);
        } else if ((v2.getUB() <= 0) && (v0.getUB() <= 0)) {
            return ruleB1(v2, v0);
        } else if ((v2.getLB() >= 0) && (v0.getUB() <= 0)) {
            return ruleC1(v2, v0);
        } else if ((v2.getUB() <= 0) && (v0.getLB() >= 0)) {
            return ruleD1(v2, v0);
        } else if ((v2.getLB() <= 0) && (v2.getUB() >= 0) && (v0.getUB() <= 0)) {
            return ruleE1(v2, v0);
        } else if ((v2.getUB() <= 0) && (v0.getLB() <= 0) && (v0.getUB() >= 0)) {
            return ruleF1(v2);
        } else if ((v2.getLB() <= 0) && (v2.getUB() >= 0) && (v0.getLB() >= 0)) {
            return ruleG1(v2, v0);
        } else if ((v2.getLB() >= 0) && (v0.getLB() <= 0) && (v0.getUB() >= 0)) {
            return ruleH1(v2);
        } else if ((v2.getLB() <= 0) && (v2.getUB() >= 0) && (v0.getLB() <= 0) && (v0.getUB() >= 0)) {
            return ruleI1(v2);
        } else {
            throw new SolverException("None of the cases is active!");
        }
    }

    protected int getYmaxIfNonZero() {
        if ((v2.getLB() >= 0) && (v0.getLB() >= 0)) {
            return ruleA2(v2, v0);
        } else if ((v2.getUB() <= 0) && (v0.getUB() <= 0)) {
            return ruleB2(v2, v0);
        } else if ((v2.getLB() >= 0) && (v0.getUB() <= 0)) {
            return ruleC2(v2, v0);
        } else if ((v2.getUB() <= 0) && (v0.getLB() >= 0)) {
            return ruleD2(v2, v0);
        } else if ((v2.getLB() <= 0) && (v2.getUB() >= 0) && (v0.getUB() <= 0)) {
            return ruleE2(v2, v0);
        } else if ((v2.getUB() <= 0) && (v0.getLB() <= 0) && (v0.getUB() >= 0)) {
            return ruleF2(v2);
        } else if ((v2.getLB() <= 0) && (v2.getUB() >= 0) && (v0.getLB() >= 0)) {
            return ruleG2(v2, v0);
        } else if ((v2.getLB() >= 0) && (v0.getLB() <= 0) && (v0.getUB() >= 0)) {
            return ruleH2(v2);
        } else if ((v2.getLB() <= 0) && (v2.getUB() >= 0) && (v0.getLB() <= 0) && (v0.getUB() >= 0)) {
            return ruleI2(v2);
        } else {
            throw new SolverException("None of the cases is active!");
        }
    }

    protected int getZmin() {
        if ((v0.getLB() >= 0) && (v1.getLB() >= 0)) {
            return ruleA3(v0, v1);
        } else if ((v0.getUB() <= 0) && (v1.getUB() <= 0)) {
            return ruleB3(v0, v1);
        } else if ((v0.getLB() >= 0) && (v1.getUB() <= 0)) {
            return ruleC3(v0, v1);
        } else if ((v0.getUB() <= 0) && (v1.getLB() >= 0)) {
            return ruleD3(v0, v1);
        } else if ((v0.getLB() <= 0) && (v0.getUB() >= 0) && (v1.getUB() <= 0)) {
            return ruleE3(v0, v1);
        } else if ((v0.getUB() <= 0) && (v1.getLB() <= 0) && (v1.getUB() >= 0)) {
            return ruleF3(v0, v1);
        } else if ((v0.getLB() <= 0) && (v0.getUB() >= 0) && (v1.getLB() >= 0)) {
            return ruleG3(v0, v1);
        } else if ((v0.getLB() >= 0) && (v1.getLB() <= 0) && (v1.getUB() >= 0)) {
            return ruleH3(v0, v1);
        } else if ((v0.getLB() <= 0) && (v0.getUB() >= 0) && (v1.getLB() <= 0) && (v1.getUB() >= 0)) {
            return ruleI3(v0, v1);
        } else {
            throw new SolverException("None of the cases is active!");
        }
    }

    protected int getZmax() {
        if ((v0.getLB() >= 0) && (v1.getLB() >= 0)) {
            return ruleA4(v0, v1);
        } else if ((v0.getUB() <= 0) && (v1.getUB() <= 0)) {
            return ruleB4(v0, v1);
        } else if ((v0.getLB() >= 0) && (v1.getUB() <= 0)) {
            return ruleC4(v0, v1);
        } else if ((v0.getUB() <= 0) && (v1.getLB() >= 0)) {
            return ruleD4(v0, v1);
        } else if ((v0.getLB() <= 0) && (v0.getUB() >= 0) && (v1.getUB() <= 0)) {
            return ruleE4(v0, v1);
        } else if ((v0.getUB() <= 0) && (v1.getLB() <= 0) && (v1.getUB() >= 0)) {
            return ruleF4(v0, v1);
        } else if ((v0.getLB() <= 0) && (v0.getUB() >= 0) && (v1.getLB() >= 0)) {
            return ruleG4(v0, v1);
        } else if ((v0.getLB() >= 0) && (v1.getLB() <= 0) && (v1.getUB() >= 0)) {
            return ruleH4(v0, v1);
        } else if ((v0.getLB() <= 0) && (v0.getUB() >= 0) && (v1.getLB() <= 0) && (v1.getUB() >= 0)) {
            return ruleI4(v0, v1);
        } else {
            throw new SolverException("None of the cases is active!");
        }
    }

    private int ruleA1(IntVar b, IntVar c) {
        return infCeilmM(b, c);
    }

    private int ruleB1(IntVar b, IntVar c) {
        return infCeilMm(b, c);
    }

    private int ruleC1(IntVar b, IntVar c) {
        return infCeilMM(b, c);
    }

    private int ruleD1(IntVar b, IntVar c) {
        return infCeilmm(b, c);
    }

    private int ruleE1(IntVar b, IntVar c) {
        return infCeilMM(b, c);
    }

    private int ruleF1(IntVar b) {
        return infCeilmP(b);
    }

    private int ruleG1(IntVar b, IntVar c) {
        return infCeilmm(b, c);
    }

    private int ruleH1(IntVar b) {
        return infCeilMN(b);
    }

    private int ruleI1(IntVar b) {
        return infCeilxx(b);
    }

    private int ruleA2(IntVar b, IntVar c) {
        return supCeilMm(b, c);
    }

    private int ruleB2(IntVar b, IntVar c) {
        return supCeilmM(b, c);
    }

    private int ruleC2(IntVar b, IntVar c) {
        return supCeilmm(b, c);
    }

    private int ruleD2(IntVar b, IntVar c) {
        return supCeilMM(b, c);
    }

    private int ruleE2(IntVar b, IntVar c) {
        return supCeilmM(b, c);
    }

    private int ruleF2(IntVar b) {
        return supCeilmN(b);
    }

    private int ruleG2(IntVar b, IntVar c) {
        return supCeilMm(b, c);
    }

    private int ruleH2(IntVar b) {
        return supCeilMP(b);
    }

    private int ruleI2(IntVar b) {
        return supCeilEq(b);
    }

    private int ruleA3(IntVar b, IntVar c) {
        return infFloormm(b, c);
    }

    private int ruleB3(IntVar b, IntVar c) {
        return infFloorMM(b, c);
    }

    private int ruleC3(IntVar b, IntVar c) {
        return infFloorMm(b, c);
    }

    private int ruleD3(IntVar b, IntVar c) {
        return infFloormM(b, c);
    }

    private int ruleE3(IntVar b, IntVar c) {
        return infFloorMm(b, c);
    }

    private int ruleF3(IntVar b, IntVar c) {
        return infFloormM(b, c);
    }

    private int ruleG3(IntVar b, IntVar c) {
        return infFloormM(b, c);
    }

    private int ruleH3(IntVar b, IntVar c) {
        return infFloorMm(b, c);
    }

    private int ruleI3(IntVar b, IntVar c) {
        return infFloorxx(b, c);
    }

    private int ruleA4(IntVar b, IntVar c) {
        return supFloorMM(b, c);
    }

    private int ruleB4(IntVar b, IntVar c) {
        return supFloormm(b, c);
    }

    private int ruleC4(IntVar b, IntVar c) {
        return supFloormM(b, c);
    }

    private int ruleD4(IntVar b, IntVar c) {
        return supFloorMm(b, c);
    }

    private int ruleE4(IntVar b, IntVar c) {
        return supFloormm(b, c);
    }

    private int ruleF4(IntVar b, IntVar c) {
        return supFloormm(b, c);
    }

    private int ruleG4(IntVar b, IntVar c) {
        return supFloorMM(b, c);
    }

    private int ruleH4(IntVar b, IntVar c) {
        return supFloorMM(b, c);
    }

    private int ruleI4(IntVar b, IntVar c) {
        return supFloorEq(b, c);
    }

    private int infFloormm(IntVar b, IntVar c) {
        return b.getLB() * c.getLB();
    }

    private int infFloormM(IntVar b, IntVar c) {
        return b.getLB() * c.getUB();
    }

    private int infFloorMm(IntVar b, IntVar c) {
        return b.getUB() * c.getLB();
    }

    private int infFloorMM(IntVar b, IntVar c) {
        return b.getUB() * c.getUB();
    }

    private int supFloormm(IntVar b, IntVar c) {
        return b.getLB() * c.getLB();
    }

    private int supFloormM(IntVar b, IntVar c) {
        return b.getLB() * c.getUB();
    }

    private int supFloorMm(IntVar b, IntVar c) {
        return b.getUB() * c.getLB();
    }

    private int supFloorMM(IntVar b, IntVar c) {
        return b.getUB() * c.getUB();
    }

    private int getMinPositive() {
        return 1;
    }

    private int getMaxNegative() {
        return -1;
    }

    private int getNonZeroSup(IntVar v) {
        return Math.min(v.getUB(), -1);
    }

    private int getNonZeroInf(IntVar v) {
        return Math.max(v.getLB(), 1);
    }

    private int infCeilmm(IntVar b, IntVar c) {
        return MathUtils.divCeil(b.getLB(), getNonZeroInf(c));
    }

    private int infCeilmM(IntVar b, IntVar c) {
        return MathUtils.divCeil(getNonZeroInf(b), c.getUB());
    }

    private int infCeilMm(IntVar b, IntVar c) {
        return MathUtils.divCeil(getNonZeroSup(b), c.getLB());
    }

    private int infCeilMM(IntVar b, IntVar c) {
        return MathUtils.divCeil(b.getUB(), getNonZeroSup(c));
    }

    private int infCeilmP(IntVar b) {
        return MathUtils.divCeil(b.getLB(), getMinPositive());
    }

    private int infCeilMN(IntVar b) {
        return MathUtils.divCeil(b.getUB(), getMaxNegative());
    }

    private int supCeilmm(IntVar b, IntVar c) {
        return MathUtils.divFloor(getNonZeroInf(b), c.getLB());
    }

    private int supCeilmM(IntVar b, IntVar c) {
        return MathUtils.divFloor(b.getLB(), getNonZeroSup(c));
    }

    private int supCeilMm(IntVar b, IntVar c) {
        return MathUtils.divFloor(b.getUB(), getNonZeroInf(c));
    }

    private int supCeilMM(IntVar b, IntVar c) {
        return MathUtils.divFloor(getNonZeroSup(b), c.getUB());
    }

    private int supCeilmN(IntVar b) {
        return MathUtils.divFloor(b.getLB(), getMaxNegative());
    }

    private int supCeilMP(IntVar b) {
        return MathUtils.divFloor(b.getUB(), getMinPositive());
    }

    private int infFloorxx(IntVar b, IntVar c) {
        if (b.getLB() * c.getUB() < b.getUB() * c.getLB()) {
            return b.getLB() * c.getUB();
        } else {
            return b.getUB() * c.getLB();
        }
    }

    private int supFloorEq(IntVar b, IntVar c) {
        if (b.getLB() * c.getLB() > b.getUB() * c.getUB()) {
            return b.getLB() * c.getLB();
        } else {
            return b.getUB() * c.getUB();
        }
    }

    private int infCeilxx(IntVar b) {
        return Math.min(MathUtils.divCeil(b.getLB(), getMinPositive()), MathUtils.divCeil(b.getUB(), getMaxNegative()));
    }  //v0.18

    private int supCeilEq(IntVar b) {
        return Math.max(MathUtils.divFloor(b.getLB(), getMaxNegative()), MathUtils.divFloor(b.getUB(), getMinPositive()));
    }   //v0.18

    /**
     * propagate the fact that v2 (Z) is instantiated to 0
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
     *
     * @return boolean
     * @throws solver.exception.ContradictionException
     *
     */
    protected boolean updateX() throws ContradictionException {
        boolean infChange = v0.updateLowerBound(getXminIfNonZero(), this);
        boolean supChange = v0.updateUpperBound(getXmaxIfNonZero(), this);
        return (infChange || supChange);
    }

    protected boolean updateY() throws ContradictionException {
        boolean infChange = v1.updateLowerBound(getYminIfNonZero(), this);
        boolean supChange = v1.updateUpperBound(getYmaxIfNonZero(), this);
        return (infChange || supChange);
    }

    /**
     * loop until a fix point is reach (see testProd14)
     *
     * @throws solver.exception.ContradictionException
     *
     * @throws solver.exception.ContradictionException
     *
     */
    protected void updateXandY() throws ContradictionException {
        while (updateX() && updateY()) ;
    }

    protected void updateYandX() throws ContradictionException {
        while (updateY() && updateX()) ;
    }

    /**
     * Updating X and Y when Z can  be 0
     *
     * @return
     * @throws solver.exception.ContradictionException
     *
     */
    protected boolean shaveOnX() throws ContradictionException {
        int xmin = getXminIfNonZero();
        int xmax = getXmaxIfNonZero();
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
        int ymin = getYminIfNonZero();
        int ymax = getYmaxIfNonZero();
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
