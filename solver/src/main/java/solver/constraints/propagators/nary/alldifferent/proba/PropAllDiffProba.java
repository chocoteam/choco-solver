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
package solver.constraints.propagators.nary.alldifferent.proba;

import choco.kernel.ESat;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.nary.alldifferent.CounterProba;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.constraints.propagators.nary.alldifferent.PropCliqueNeq;
import solver.exception.ContradictionException;
import solver.recorders.fine.AbstractFineEventRecorder;
import solver.variables.EventType;
import solver.variables.IntVar;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 02/03/12
 */
public class PropAllDiffProba<V extends IntVar> extends Propagator<V> {

    PropCliqueNeq propCliqueNeq;
    Propagator propAllDiff;
    CondAllDiffBCProba condition;
    CounterProba count;

    public PropAllDiffProba(V[] vars, Solver solver, Constraint<V, Propagator<V>> vPropagatorConstraint,
                            CondAllDiffBCProba condition,
                            CounterProba count,
                            PropCliqueNeq propCliqueNeq,
                            Propagator propAllDiff) {
        super(vars, solver, vPropagatorConstraint, PropagatorPriority.VERY_SLOW, false);
        this.propCliqueNeq = propCliqueNeq;
        this.propAllDiff = propAllDiff;
        this.condition = condition;
        this.count = count;
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return EventType.INT_ALL_MASK();
    }

    @Override
    public int getPropagationConditions() {
        return EventType.FULL_PROPAGATION.mask + EventType.CUSTOM_PROPAGATION.mask;
    }

    /*public void propagate(int evtmask) throws ContradictionException {
        count.incrAllProp();
        if ((evtmask & EventType.FULL_PROPAGATION.mask) != 0) {
            if (condition.isValid()) {
                count.incrAllDiff();
                System.out.println("IF [alldiff]:" + count.getNbProp() + "--" + count.getNbAllDiff() + "--" + count.getNbNeq());
                propAllDiff.propagate(evtmask);
            } else {
                count.incrNeq();
                System.out.println("IF [neq]:" + count.getNbProp() + "--" + count.getNbAllDiff() + "--" + count.getNbNeq());
                propCliqueNeq.propagate(evtmask);
            }
        } else {
            // if CUSTOM => promote for AllDiff
            count.incrAllDiff();
            System.out.println("ELSE [initPropag]:" + count.getNbProp() + "--" + count.getNbAllDiff() + "--" + count.getNbNeq());
            propAllDiff.propagate(EventType.FULL_PROPAGATION.mask);
        }
    }*/

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        count.incrAllProp();
        if ((evtmask & EventType.FULL_PROPAGATION.mask) != 0) {
            count.incrAllDiff();
            //System.out.println("initPropag:" + count.getNbProp() + "--" + count.getNbAllDiff() + "--" + count.getNbNeq());
            propAllDiff.propagate(EventType.FULL_PROPAGATION.mask);
        } else {
            count.incrAllDiff();
            //System.out.println("full alldiff:" + count.getNbProp() + "--" + count.getNbAllDiff() + "--" + count.getNbNeq());
            propAllDiff.propagate(evtmask);
        }
    }


    /*public void propagate(AbstractFineEventRecorder eventRecorder, int idxVarInProp, int mask) throws ContradictionException {
        count.incrAllProp();
        // 1. update the condition wrt the event received
        condition.update(eventRecorder, this, mask);
        // 2. switch on the condition to execute the correct propagator
        if (condition.isValid()) {
            count.incrAllDiff();
            forcePropagate(EventType.CUSTOM_PROPAGATION);
        } else {
            count.incrNeq();
            propCliqueNeq.propagate(eventRecorder, idxVarInProp, mask);
        }
    } */

    @Override
    public void propagate(AbstractFineEventRecorder eventRecorder, int idxVarInProp, int mask) throws ContradictionException {
        // 1. update the condition wrt the event received
        condition.update(eventRecorder, this, mask);
        // 2. switch on the condition to execute the correct propagator
        if (condition.isValid()) {
            //count.incrAllDiff();
            forcePropagate(EventType.CUSTOM_PROPAGATION);
        } else {
            count.incrNeq();
            //System.out.println("neq:" + count.getNbProp() + "--" + count.getNbAllDiff() + "--" + count.getNbNeq());
            propCliqueNeq.propagate(eventRecorder, idxVarInProp, mask);
        }
    }

    @Override
    public ESat isEntailed() {
        return propCliqueNeq.isEntailed();
    }
}
