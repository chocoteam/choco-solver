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

package solver.requests;

import choco.kernel.common.util.procedure.IntProcedure;
import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.IStateInt;
import solver.constraints.propagators.Propagator;
import solver.exception.ContradictionException;
import solver.requests.conditions.AbstractCondition;
import solver.search.loop.AbstractSearchLoop;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.delta.IntDelta;

/**
 * A conditionnal request that call run filter under conditions.
 * It stores every events occuring on the declared variable, all branch long, until a valid call to filter
 * is done.
 * <p/>
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 22/03/11
 */
public class ConditionnalRequest<P extends Propagator<IntVar>> extends AbstractRequest<IntVar, P> {

    int timestamp; // timestamp of the last clear call -- for lazy clear

    final AbstractCondition condition; // condition to run the filtering algorithm of the propagator

    final IStateInt evtmask; // bactrackable probably merged event mask -- on a branch

    final int[] removedValues; // removedvalues from variable domain, its size is at most |D(X)|-1.

    final IStateInt first, last; // regarding "removedValues", point out the interval not propagated

    int frozenFirst, frozenLast; // same as previous while the request is frozen, to allow "concurrent modifications"

    int dLast; // regarding variable delta domain, point out last value already recorded

    public ConditionnalRequest(P propagator, IntVar variable, int idxInProp, AbstractCondition condition, IEnvironment environment) {
        super(propagator, variable, idxInProp);
        this.condition = condition;
        evtmask = environment.makeInt();
        first = environment.makeInt();
        last = environment.makeInt();
        removedValues = new int[variable.getDomainSize() - 1];
        dLast = 0;
    }


    public int getLast() {
        return last.get();
    }

    @Override
    public void forEach(IntProcedure proc) throws ContradictionException {
        int last_ = last.get();
        for(int i = first.get(); i < last_; i++){
            proc.execute(removedValues[i]);
        }
    }

    @Override
    public void forEach(IntProcedure proc, int from, int to) throws ContradictionException {
        for(int i = from; i < to; i++){
            proc.execute(removedValues[i]);
        }
    }

    @Override
    public void filter() throws ContradictionException {
        int evtmask_ = evtmask.get();
        if (evtmask_ > 0) {
            // for concurrent modification..
            first.set(last.get()); // point out current last
            evtmask.set(0); // and clean up current mask
            propagator.eventCalls++;
            propagator.propagateOnRequest(this, idxVarInProp, evtmask_);
        }
    }

    @Override
    public void update(EventType e) {
        // Only notify constraints that filter on the specific event received
        if ((e.mask & propagator.getPropagationConditions(idxVarInProp)) != 0) {
            this.lazyClear();
            if (EventType.anInstantiationEvent(e.mask)) {
                propagator.decArity();
            }
            addAll(e);
            condition.updateAndValid(this, e.mask);
        }
    }


    private void addAll(EventType e) {
        if ((e.mask & evtmask.get()) == 0) {
            evtmask.add(e.mask);
        }
        int last_ = last.get();
        IntDelta delta = variable.getDelta();
        for (; dLast < delta.size(); dLast++) {
            removedValues[last_++] = delta.get(dLast);
        }
        last.set(last_);
    }

    protected void lazyClear() {
        if (timestamp - AbstractSearchLoop.timeStamp != 0) {
            this.dLast = 0;
            timestamp = AbstractSearchLoop.timeStamp;
        }
    }

    public boolean hasChanged() {
        return evtmask.get() > 0;
    }

    @Override
    public void desactivate() {
        super.desactivate();
        evtmask.set(0);
    }

}
