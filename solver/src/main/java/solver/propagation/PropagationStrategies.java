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
package solver.propagation;

import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.propagation.strategy.Group;
import solver.propagation.strategy.QueueGroup;
import solver.recorders.coarse.AbstractCoarseEventRecorder;
import solver.recorders.coarse.CoarseEventRecorder;
import solver.recorders.fine.AbstractFineEventRecorder;
import solver.recorders.fine.ArcEventRecorder;
import solver.variables.Variable;

import java.util.ArrayList;
import java.util.List;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 08/12/11
 */
public enum PropagationStrategies {

    ONE_QUEUE_WITH_ARCS() {
        @SuppressWarnings({"unchecked"})
        public Group make(Solver solver) {
            List<ISchedulable> all = new ArrayList<ISchedulable>();
            all.addAll(makeArcs(solver));
            int fines = all.size();
            all.addAll(makeCoarses(solver));

            Group queue = new QueueGroup(Group.Iteration.CLEAR_OUT,
                    all.toArray(new ISchedulable[all.size()]));
            for (int i = fines; i < all.size(); i++) {
                queue.schedule(all.get(i));
            }
            return queue;
        }
    },
    TWO_QUEUES_WITH_ARCS() {
        @SuppressWarnings({"unchecked"})
        public Group make(Solver solver) {
            List<AbstractFineEventRecorder> ars = makeArcs(solver);
            List<AbstractCoarseEventRecorder> coarses = makeCoarses(solver);

            final ISchedulable q1 = new QueueGroup(Group.Iteration.CLEAR_OUT,
                    ars.toArray(new ISchedulable[ars.size()]));
            final ISchedulable q2 = new QueueGroup(Group.Iteration.PICK_ONE,
                    coarses.toArray(new ISchedulable[coarses.size()]));

            Group queues = new QueueGroup(
                    Group.Iteration.CLEAR_OUT,
                    new ArrayList<ISchedulable>() {{
                        add(q1);
                        add(q2);
                    }}.toArray(new ISchedulable[2]));
            for (int i = 0; i < coarses.size(); i++) {
                queues.schedule(coarses.get(i));
            }

            return queues;
        }
    };

    public abstract Group make(Solver solver);

    public static List<AbstractFineEventRecorder> makeArcs(Solver solver) {
        Constraint[] constraints = solver.getCstrs();
        List<AbstractFineEventRecorder> fers = new ArrayList<AbstractFineEventRecorder>();

        for (int c = 0; c < constraints.length; c++) {
            Propagator[] propagators = constraints[c].propagators;
            for (int p = 0; p < propagators.length; p++) {
                int nbV = propagators[p].getNbVars();
                for (int v = 0; v < nbV; v++) {
                    Variable variable = propagators[p].getVar(v);
                    AbstractFineEventRecorder fer = new ArcEventRecorder(variable, propagators[p], v, solver);
                    propagators[p].addRecorder(fer);
                    variable.addMonitor(fer);
                    fers.add(fer);
                }
            }
        }
        return fers;
    }

    public static List<AbstractCoarseEventRecorder> makeCoarses(Solver solver) {
        Constraint[] constraints = solver.getCstrs();
        List<AbstractCoarseEventRecorder> cers = new ArrayList<AbstractCoarseEventRecorder>();

        for (int c = 0; c < constraints.length; c++) {
            Propagator[] propagators = constraints[c].propagators;
            for (int p = 0; p < propagators.length; p++) {
                CoarseEventRecorder cer = (CoarseEventRecorder) propagators[p].getRecorder(-1);
                cers.add(cer);
            }
        }
        return cers;
    }
}
