/*
 * Copyright (c) 1999-2012, Ecole des Mines de Nantes
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
package solver.constraints.propagators.nary.automaton;

import common.ESat;
import common.util.iterators.DisposableIntIterator;
import common.util.procedure.UnaryIntProcedure;
import gnu.trove.stack.TIntStack;
import gnu.trove.stack.array.TIntArrayStack;
import memory.IStateBool;
import memory.structure.StoredIndexedBipartiteSet;
import solver.constraints.nary.automata.FA.ICostAutomaton;
import solver.constraints.nary.automata.FA.utils.Bounds;
import solver.constraints.nary.automata.structure.costregular.StoredValuedDirectedMultiGraph;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.delta.IIntDeltaMonitor;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 06/06/11
 */
public class PropCostRegular extends Propagator<IntVar> {

    final int zIdx;

    final StoredValuedDirectedMultiGraph graph;
    final ICostAutomaton cautomaton;
    TIntStack toRemove;

    final IStateBool boundChange;
    int lastWorld = -1;

    long lastNbOfBacktracks = -1, lastNbOfRestarts = -1;

    protected final RemProc rem_proc;
    protected final IIntDeltaMonitor[] idms;


    public PropCostRegular(IntVar[] variables, ICostAutomaton cautomaton, StoredValuedDirectedMultiGraph graph) {
        super(variables, PropagatorPriority.CUBIC, false);
        this.idms = new IIntDeltaMonitor[this.vars.length];
        for (int i = 0; i < this.vars.length; i++) {
            idms[i] = this.vars[i].monitorDelta(this);
        }
        this.zIdx = vars.length - 1;
        this.rem_proc = new RemProc(this);
        this.environment = solver.getEnvironment();
        this.toRemove = new TIntArrayStack();
        this.boundChange = environment.makeBool(false);
        this.graph = graph;
        this.cautomaton = cautomaton;
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return (vIdx != zIdx ? EventType.INT_ALL_MASK() : EventType.BOUND.mask + EventType.INSTANTIATE.mask);
    }

    /**
     * Build internal structure of the propagator, if necessary
     *
     * @throws solver.exception.ContradictionException
     *          if initialisation encounters a contradiction
     */
    protected void initialize() throws ContradictionException {
        Bounds bounds = this.cautomaton.getCounters().get(0).bounds();
        vars[zIdx].updateLowerBound(bounds.min.value, aCause);
        vars[zIdx].updateUpperBound(bounds.max.value, aCause);
        this.prefilter();
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if ((evtmask & EventType.FULL_PROPAGATION.mask) != 0) {
            initialize();
        }
        filter();
        for (int i = 0; i < idms.length; i++) {
            idms[i].unfreeze();
        }
    }

    @Override
    public void propagate(int varIdx, int mask) throws ContradictionException {
        checkWorld();
        if (varIdx == zIdx) { // z only deals with bound events
            boundChange.set(true);
        } else { // other variables only deals with removal events
            idms[varIdx].freeze();
            idms[varIdx].forEach(rem_proc.set(varIdx), EventType.REMOVE);
            idms[varIdx].unfreeze();
        }
        forcePropagate(EventType.CUSTOM_PROPAGATION);
    }

    @Override
    public ESat isEntailed() {
        if (isCompletelyInstantiated()) {
            int first = this.graph.sourceIndex;
            boolean found;
            double cost = 0.0;
            int[] str = new int[vars.length];
            for (int i = 0; i < vars.length - 1; i++) {
                found = false;
                str[i] = vars[i].getValue();
                StoredIndexedBipartiteSet bs = this.graph.GNodes.outArcs[first];
                DisposableIntIterator it = bs.getIterator();
                while (!found && it.hasNext()) {
                    int idx = it.next();
                    if (this.graph.GArcs.values[idx] == vars[i].getValue()) {
                        found = true;
                        first = this.graph.GArcs.dests[idx];
                        cost += this.graph.GArcs.costs[idx];
                    }
                }
                if (!found)
                    return ESat.FALSE;

            }
            int intCost = vars[zIdx].getValue();
            return ESat.eval(cost == intCost && cautomaton.run(str));
        }
        return ESat.UNDEFINED;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void prefilter() throws ContradictionException {
        double zinf = this.graph.GNodes.spft.get(this.graph.sourceIndex);
        double zsup = this.graph.GNodes.lpfs.get(this.graph.tinkIndex);

        vars[zIdx].updateLowerBound((int) Math.ceil(zinf), aCause);
        vars[zIdx].updateUpperBound((int) Math.floor(zsup), aCause);

        DisposableIntIterator it = this.graph.inGraph.getIterator();
        //for (int id = this.graph.inGraph.nextSetBit(0) ; id >=0 ; id = this.graph.inGraph.nextSetBit(id+1))  {
        while (it.hasNext()) {
            int id = it.next();
            int orig = this.graph.GArcs.origs[id];
            int dest = this.graph.GArcs.dests[id];

            double acost = this.graph.GArcs.costs[id];

            double spfs = this.graph.GNodes.spfs.get(orig);
            double lpfs = this.graph.GNodes.lpfs.get(orig);

            double spft = this.graph.GNodes.spft.get(dest);
            double lpft = this.graph.GNodes.lpft.get(dest);


            if ((spfs + spft + acost > vars[zIdx].getUB() || lpfs + lpft + acost < vars[zIdx].getLB()) && !this.graph.isInStack(id)) {
                this.graph.setInStack(id);
                this.toRemove.push(id);
            }
        }

        it.dispose();

        try {
            do {
                while (toRemove.size() > 0) {
                    int id = toRemove.pop();
                    // toRemove.removeLast();
                    this.graph.removeArc(id, toRemove, this, aCause);
                }
                while (this.graph.toUpdateLeft.size() > 0) {
                    this.graph.updateLeft(this.graph.toUpdateLeft.pop(), toRemove, this);
                }
                while (this.graph.toUpdateRight.size() > 0) {
                    this.graph.updateRight(this.graph.toUpdateRight.pop(), toRemove, this);
                }
            } while (toRemove.size() > 0);
        } catch (ContradictionException e) {
            toRemove.clear();
            this.graph.inStack.clear();
            this.graph.toUpdateLeft.clear();
            this.graph.toUpdateRight.clear();

            throw e;
        }
    }

    private void checkWorld() {
        int currentworld = environment.getWorldIndex();
        long currentbt = solver.getMeasures().getBackTrackCount();
        long currentrestart = solver.getMeasures().getRestartCount();
        if (currentworld < lastWorld || currentbt != lastNbOfBacktracks || currentrestart > lastNbOfRestarts) {
            this.toRemove.clear();
            this.graph.inStack.clear();
            this.graph.toUpdateLeft.clear();
            this.graph.toUpdateRight.clear();
        }
        lastWorld = currentworld;
        lastNbOfBacktracks = currentbt;
        lastNbOfRestarts = currentrestart;
    }

    private void filter() throws ContradictionException {

        if (boundChange.get()) {
            boundChange.set(false);
            DisposableIntIterator it = this.graph.inGraph.getIterator();
            //for (int id = this.graph.inGraph.nextSetBit(0) ; id >=0 ; id = this.graph.inGraph.nextSetBit(id+1))  {
            while (it.hasNext()) {
                int id = it.next();
                int orig = this.graph.GArcs.origs[id];
                int dest = this.graph.GArcs.dests[id];

                double acost = this.graph.GArcs.costs[id];
                double lpfs = this.graph.GNodes.lpfs.get(orig);
                double lpft = this.graph.GNodes.lpft.get(dest);

                double spfs = this.graph.GNodes.spfs.get(orig);
                double spft = this.graph.GNodes.spft.get(dest);


                if ((lpfs + lpft + acost < vars[zIdx].getLB() || spfs + spft + acost > vars[zIdx].getUB()) && !this.graph.isInStack(id)) {
                    this.graph.setInStack(id);
                    this.toRemove.push(id);
                }
            }
            it.dispose();

        }

        do {
            while (toRemove.size() > 0) {
                int id = toRemove.pop();
                // toRemove.removeLast();
                this.graph.removeArc(id, toRemove, this, aCause);
            }
            while (this.graph.toUpdateLeft.size() > 0) {
                this.graph.updateLeft(this.graph.toUpdateLeft.pop(), toRemove, this);
            }
            while (this.graph.toUpdateRight.size() > 0) {
                this.graph.updateRight(this.graph.toUpdateRight.pop(), toRemove, this
                );
            }
        } while (toRemove.size() > 0);


        double zinf = this.graph.GNodes.spft.get(this.graph.sourceIndex);
        double zsup = this.graph.GNodes.lpfs.get(this.graph.tinkIndex);

        vars[zIdx].updateLowerBound((int) Math.ceil(zinf), aCause);
        vars[zIdx].updateUpperBound((int) Math.floor(zsup), aCause);
    }


    private static class RemProc implements UnaryIntProcedure<Integer> {

        private final PropCostRegular p;
        private int idxVar;

        public RemProc(PropCostRegular p) {
            this.p = p;
        }

        @Override
        public UnaryIntProcedure set(Integer idxVar) {
            this.idxVar = idxVar;
            return this;
        }

        @Override
        public void execute(int i) throws ContradictionException {
            StoredIndexedBipartiteSet sup = p.graph.getSupport(idxVar, i);
            if (sup != null) {
                DisposableIntIterator it = sup.getIterator();
                while (it.hasNext()) {
                    int arcId = it.next();
                    if (!p.graph.isInStack(arcId)) {
                        p.graph.setInStack(arcId);
                        p.toRemove.push(arcId);
//                        mod = true;
                    }
                }
                it.dispose();
            }
        }
    }
}
