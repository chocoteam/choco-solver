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
package solver.constraints.propagators.nary.channeling;

import choco.kernel.ESat;
import choco.kernel.common.util.procedure.IntProcedure;
import choco.kernel.common.util.tools.ArrayUtils;
import choco.kernel.memory.IStateInt;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.recorders.fine.AbstractFineEventRecorder;
import solver.variables.BoolVar;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.delta.IDeltaMonitor;
import solver.variables.delta.IntDelta;

/**
 * Constraints that map the boolean assignments variables (bvars) with the standard assignment variables (var).
 * var = i -> bvars[i] = 1
 * <br/>
 *
 * @author Xavier Lorca
 * @author Hadrien Cambazard
 * @author Fabien Hermenier
 * @author Charles Prud'homme
 * @since 04/08/11
 */
public class PropDomainChanneling extends Propagator<IntVar> {

    /**
     * Number of possible assignments.
     * ie, the number of boolean vars
     */
    private final int dsize;

    /**
     * The last lower bounds of the assignment var.
     */
    private final IStateInt oldinf;

    /**
     * The last upper bounds of the assignment var.
     */
    private final IStateInt oldsup;

    protected final RemProc rem_proc;

    protected final IDeltaMonitor<IntDelta>[] idms;

    public PropDomainChanneling(BoolVar[] bs, IntVar x, Solver solver,
                                Constraint<IntVar, Propagator<IntVar>> intVarPropagatorConstraint) {
        super(ArrayUtils.append(bs, new IntVar[]{x}), solver, intVarPropagatorConstraint, PropagatorPriority.LINEAR, false);
        this.idms = new IDeltaMonitor[this.vars.length];
        for (int i = 0; i < this.vars.length; i++){
            idms[i] = this.vars[i].getDelta().createDeltaMonitor(this);
        }
        this.dsize = bs.length;
        oldinf = environment.makeInt();
        oldsup = environment.makeInt();
        this.rem_proc = new RemProc(this);
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        if (vIdx < dsize) {
            return EventType.INSTANTIATE.mask;
        } else {
            return EventType.INT_ALL_MASK();
        }
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        vars[dsize].updateLowerBound(0, this);
        vars[dsize].updateUpperBound(dsize - 1, this);

        int left = Integer.MIN_VALUE;
        int right = left;
        for (int i = 0; i < dsize; i++) {
            if (vars[i].instantiatedTo(0)) {
                if (i == right + 1) {
                    right = i;
                } else {
                    vars[dsize].removeInterval(left, right, this);
                    left = i;
                    right = i;
                }
//                vars[dsize].removeVal(i, this, false);
            } else if (vars[i].instantiatedTo(1)) {
                vars[dsize].instantiateTo(i, this);
                clearBooleanExcept(i);
            } else if (!vars[dsize].contains(i)) {
                clearBoolean(i);
            }
        }
        vars[dsize].removeInterval(left, right, this);
        if (vars[dsize].instantiated()) {
            final int value = vars[dsize].getValue();
            clearBooleanExcept(value);
            vars[value].instantiateTo(1, this);
        }

        //Set oldinf & oldsup equals to the nt bounds of the assignment var
        oldinf.set(vars[dsize].getLB());
        oldsup.set(vars[dsize].getUB());


    }

    @Override
    public void propagate(AbstractFineEventRecorder eventRecorder, int varIdx, int mask) throws ContradictionException {
        if (EventType.isInstantiate(mask)) {
            //val = the current value
            final int val = vars[varIdx].getValue();

            if (varIdx == dsize) {
                //We instantiate the assignment var
                //val = index to keep
                vars[val].instantiateTo(1, this);
                clearBooleanExcept(val);
            } else {
                //We instantiate a boolean var
                if (val == 1) {
                    //We report the instantiation to the associated assignment var
                    vars[dsize].instantiateTo(varIdx, this);
                    //Next line should be useless ?
                    clearBooleanExcept(varIdx);
                } else {
                    vars[dsize].removeValue(varIdx, this);
                    if (vars[dsize].instantiated()) {
                        vars[vars[dsize].getValue()].instantiateTo(1, this);
                    }
                }
            }
        } else {
            if (EventType.isInclow(mask)) {
                clearBoolean(oldinf.get(), vars[varIdx].getLB());
                oldinf.set(vars[varIdx].getLB());
            }
            if (EventType.isDecupp(mask)) {
                clearBoolean(vars[varIdx].getUB() + 1, oldsup.get() + 1);
                oldsup.set(vars[varIdx].getUB());
            }
            if (EventType.isRemove(mask)) {
                idms[varIdx].freeze();
                idms[varIdx].forEach(rem_proc, EventType.REMOVE);
                idms[varIdx].unfreeze();
            }
        }

    }

    @Override
    public ESat isEntailed() {
        if (isCompletelyInstantiated()) {
            //TODO: ugly
            return constraint.isSatisfied();
        }
        return ESat.UNDEFINED;
    }


    private void clearBoolean(int val) throws ContradictionException {
        vars[val].instantiateTo(0, this);
    }


    private void clearBoolean(int begin, int end) throws ContradictionException {
        for (int i = begin; i < end; i++) {
            clearBoolean(i);
        }
    }

    /**
     * Instantiate all the boolean variable to 1 except one.
     *
     * @param val The index of the variable to keep
     * @throws ContradictionException if an error occured
     */
    private void clearBooleanExcept(int val) throws ContradictionException {
        clearBoolean(oldinf.get(), val);
        clearBoolean(val + 1, oldsup.get());
    }


    private static class RemProc implements IntProcedure {

        private final PropDomainChanneling p;

        public RemProc(PropDomainChanneling p) {
            this.p = p;
        }

        @Override
        public void execute(int i) throws ContradictionException {
            p.clearBoolean(i);
        }
    }
}
