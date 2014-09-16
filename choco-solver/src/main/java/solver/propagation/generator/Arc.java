/*
 * Copyright (c) 1999-2014, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package solver.propagation.generator;

import solver.Configuration;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.Propagator;
import solver.exception.ContradictionException;
import solver.propagation.IPropagationEngine;
import solver.propagation.ISchedulable;
import solver.variables.Variable;
import solver.variables.events.IEventType;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 14/11/12
 */
public class Arc<V extends Variable> implements Serializable, ISchedulable<PropagationStrategy<Arc>> {
    public final V var;
    public final Propagator<V> prop;
    public final int idxVinP; // idx var in prop

    int evtmask; // reference to events occuring -- inclusive OR over event mask
    protected PropagationStrategy<Arc> scheduler;
    protected int schedulerIdx = -1; // index in the scheduler if required, -1 by default;

    protected IEvaluator<Arc> evaluator;

    public Arc(V var, Propagator<V> prop, int idxVinP) {
        this.var = var;
        this.prop = prop;
        this.idxVinP = idxVinP;
    }

    public void update(IEventType evt) {
        if (evtmask == 0) {
            if (Configuration.PRINT_SCHEDULE) {
                IPropagationEngine.Trace.printSchedule(prop);
            }
            prop.incNbPendingEvt();
            scheduler.schedule(this);
        } else {
            if (Configuration.PRINT_SCHEDULE) {
                IPropagationEngine.Trace.printAlreadySchedule(prop);
            }
            // to treat case where this belongs to the current group executed
            // so the master group must be scheduled
            if (!scheduler.enqueued()) {
                scheduler.getScheduler().schedule(scheduler);
            }
            if (scheduler.needUpdate()) {
                scheduler.update(this);
            }
        }
        evtmask |= evt.getStrengthenedMask();
    }

    @Override
    public boolean execute() throws ContradictionException {
        if (evtmask > 0) {
            int evtmask_ = evtmask;
            this.evtmask = 0; // and clean up mask
            if (Configuration.PRINT_PROPAGATION) {
                IPropagationEngine.Trace.printPropagation(var, prop);
            }
            prop.fineERcalls++;
            prop.decNbPendingEvt();
            prop.propagate(idxVinP, evtmask_);
        }
        return true;
    }

    @Override
    public int evaluate() {
        return evaluator.eval(this);
    }

    @Override
    public void attachEvaluator(IEvaluator evaluator) {
        this.evaluator = (IEvaluator<Arc>) evaluator;
    }

    @Override
    public void enqueue() {
    }

    @Override
    public void deque() {
    }

    @Override
    public boolean enqueued() {
        return evtmask != 0;
    }

    @Override
    public void setScheduler(PropagationStrategy<Arc> scheduler, int idxInS) {
        this.scheduler = scheduler;
        this.schedulerIdx = idxInS;
    }

    @Override
    public PropagationStrategy<Arc> getScheduler() {
        return scheduler;
    }

    @Override
    public int getIndexInScheduler() {
        return schedulerIdx;
    }

    @Override
    public void setIndexInScheduler(int sIdx) {
        this.schedulerIdx = sIdx;
    }

    @Override
    public void flush() {
        if (this.evtmask > 0) {
            this.evtmask = 0;
            prop.decNbPendingEvt();
        }
    }

    public static ArrayList<Arc> populate(Solver solver) {
        ArrayList<Arc> pairs = new ArrayList<Arc>();
        Constraint[] cstrs = solver.getCstrs();
        for (int i = 0; i < cstrs.length; i++) {
            Constraint c = cstrs[i];
            Propagator[] props = c.getPropagators();
            for (int j = 0; j < props.length; j++) {
                Propagator prop = props[j];
                Variable[] vars = prop.getVars();
                for (int k = 0; k < vars.length; k++) {
                    if ((vars[k].getTypeAndKind() & Variable.CSTE) == 0) { // this is not a constant
                        pairs.add(new Arc(vars[k], props[j], k));
                    }
                }
            }
        }
        return pairs;
    }

    public static void remove(ArrayList<Arc> orig, ArrayList toRemove) {
        for (int i = 0; i < toRemove.size(); i++) {
            Object o = toRemove.get(i);
            if (o instanceof Arc) {
                orig.remove(o);
            } else {
                remove(orig, (ArrayList) o);
            }
        }
    }

    @Override
    public String toString() {
        return "ARC(" + var + "," + prop + ")";
    }
}
