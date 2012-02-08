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
package solver.constraints.propagators.nary.automaton;

import choco.kernel.ESat;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.common.util.procedure.UnaryIntProcedure;
import choco.kernel.memory.structure.StoredIndexedBipartiteSet;
import gnu.trove.stack.TIntStack;
import gnu.trove.stack.array.TIntArrayStack;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.nary.automata.FA.IAutomaton;
import solver.constraints.nary.automata.structure.regular.StoredDirectedMultiGraph;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.recorders.fine.AbstractFineEventRecorder;
import solver.variables.EventType;
import solver.variables.IntVar;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 06/06/11
 */
public class PropRegular extends Propagator<IntVar> {

    final StoredDirectedMultiGraph graph;
    final IAutomaton automaton;
    final TIntStack temp = new TIntArrayStack();

    protected final RemProc rem_proc;

    public PropRegular(IntVar[] vars, IAutomaton automaton, StoredDirectedMultiGraph graph, Solver solver, Constraint<IntVar, Propagator<IntVar>> intVarPropagatorConstraint) {
        super(vars, solver, intVarPropagatorConstraint, PropagatorPriority.LINEAR, false);
        rem_proc = new RemProc(this);
        this.automaton = automaton;
        this.graph = graph;
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return EventType.INT_ALL_MASK();
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        int left, right;
        for (int i = 0; i < vars.length; i++) {
            left = right = Integer.MIN_VALUE;
            for (int j = vars[i].getLB(); j <= vars[i].getUB(); j = vars[i].nextValue(j)) {
                StoredIndexedBipartiteSet sup = graph.getSupport(i, j);
                if (sup == null || sup.isEmpty()) {
                    if (j == right + 1) {
                        right = j;
                    } else {
                        vars[i].removeInterval(left, right, this);
                        left = right = j;
                    }
                }
            }
            vars[i].removeInterval(left, right, this);
        }
    }

    @Override
    public void propagate(AbstractFineEventRecorder eventRecorder, int idxVarInProp,
                          int mask) throws ContradictionException {
        eventRecorder.getDeltaMonitor(this, vars[idxVarInProp]).forEach(rem_proc.set(idxVarInProp), EventType.REMOVE);
    }

    @Override
    public ESat isEntailed() {
        if (this.isCompletelyInstantiated()) {
            int[] str = new int[vars.length];
            for (int i = 0; i < vars.length; i++) {
                str[i] = vars[i].getValue();
            }
            return ESat.eval(automaton.run(str));
        }
        return ESat.UNDEFINED;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static class RemProc implements UnaryIntProcedure<Integer> {

        private final PropRegular p;
        private int idxVar;

        public RemProc(PropRegular p) {
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
                    p.temp.push(arcId);


                }
                it.dispose();

                while (p.temp.size() > 0) {
                    int arcId = p.temp.pop();
                    try {
                        p.graph.removeArc(arcId, this.p);
                    } catch (ContradictionException e) {
                        p.temp.clear();
                        throw e;
                    }
                }
            }
        }
    }
}
