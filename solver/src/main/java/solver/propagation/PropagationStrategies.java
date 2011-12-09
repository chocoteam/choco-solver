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

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.propagation.strategy.Group;
import solver.propagation.strategy.QueueGroup;
import solver.recorders.coarse.AbstractCoarseEventRecorder;
import solver.recorders.coarse.CoarseEventRecorder;
import solver.recorders.fine.AbstractFineEventRecorder;
import solver.recorders.fine.ArcEventRecorder;
import solver.recorders.fine.ViewEventRecorderWrapper;
import solver.variables.Variable;
import solver.variables.view.View;

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
            Constraint[] constraints = solver.getCstrs();
            List<ISchedulable> erecorders = new ArrayList<ISchedulable>();
            TIntList coarseIdx = new TIntArrayList();
            for (int c = 0; c < constraints.length; c++) {
                Propagator[] propagators = constraints[c].propagators;
                for (int p = 0; p < propagators.length; p++) {
                    // 1. the fine event recorders
                    int nbV = propagators[p].getNbVars();
                    for (int v = 0; v < nbV; v++) {
                        Variable variable = propagators[p].getVar(v);
                        AbstractFineEventRecorder fer;
                        if (variable.getType() == Variable.VIEW) {
                            View view = (View) variable;
                            fer = new ViewEventRecorderWrapper(
                                    new ArcEventRecorder(view.getVariable(), propagators[p], v, solver),
                                    view.getModifier(),
                                    solver);
                        } else {
                            fer = new ArcEventRecorder(variable, propagators[p], v, solver);
                        }
                        propagators[p].addRecorder(fer);
                        variable.addMonitor(fer);
                        erecorders.add(fer);
                    }
                    // 2. the coarse event recorder
                    CoarseEventRecorder cer = (CoarseEventRecorder) propagators[p].getRecorder(-1);
                    erecorders.add(cer);
                    coarseIdx.add(erecorders.size() - 1);
                }
            }
            return new QueueGroup(Group.Iteration.CLEAR_OUT, erecorders, coarseIdx);
        }
    },
    TWO_QUEUES_WITH_ARCS() {
        @SuppressWarnings({"unchecked"})
        public Group make(Solver solver) {
            Constraint[] constraints = solver.getCstrs();
            List<ISchedulable> frecorders = new ArrayList<ISchedulable>();
            List<ISchedulable> crecorders = new ArrayList<ISchedulable>();
            TIntList coarseIdx = new TIntArrayList();
            for (int c = 0; c < constraints.length; c++) {
                Propagator[] propagators = constraints[c].propagators;
                for (int p = 0; p < propagators.length; p++) {
                    // 1. the fine event recorders
                    int nbV = propagators[p].getNbVars();
                    for (int v = 0; v < nbV; v++) {
                        Variable variable = propagators[p].getVar(v);
                        AbstractFineEventRecorder fer;
                        if (variable.getType() == Variable.VIEW) {
                            View view = (View) variable;
                            fer = new ViewEventRecorderWrapper(
                                    new ArcEventRecorder(view.getVariable(), propagators[p], v, solver),
                                    view.getModifier(),
                                    solver);
                        } else {
                            fer = new ArcEventRecorder(variable, propagators[p], v, solver);
                        }
                        propagators[p].addRecorder(fer);
                        variable.addMonitor(fer);
                        frecorders.add(fer);
                    }
                    // 2. the coarse event recorder
                    CoarseEventRecorder cer = (CoarseEventRecorder) propagators[p].getRecorder(-1);
                    crecorders.add(cer);
                    coarseIdx.add(crecorders.size() - 1);
                }
            }
            final TIntArrayList empty = new TIntArrayList();
            final ISchedulable q1 = new QueueGroup(Group.Iteration.CLEAR_OUT, frecorders, empty);
            final ISchedulable q2 = new QueueGroup(Group.Iteration.PICK_ONE, crecorders, coarseIdx);
            return new QueueGroup(
                    Group.Iteration.CLEAR_OUT,
                    new ArrayList<ISchedulable>() {{
                        add(q1);
                        add(q2);
                    }}, new TIntArrayList() {{
                add(1);
            }}
            );
        }
    };

    public abstract Group make(Solver solver);

    public static List<AbstractFineEventRecorder> makeArcs(Solver solver) {
        Constraint[] constraints = solver.getCstrs();
        List<AbstractFineEventRecorder> fers = new ArrayList<AbstractFineEventRecorder>();

        for (int c = 0; c < constraints.length; c++) {
            Propagator[] propagators = constraints[c].propagators;
            for (int p = 0; p < propagators.length; p++) {
                // 1. the fine event recorders
                int nbV = propagators[p].getNbVars();
                for (int v = 0; v < nbV; v++) {
                    Variable variable = propagators[p].getVar(v);
                    AbstractFineEventRecorder fer;
                    if (variable.getType() == Variable.VIEW) {
                        View view = (View) variable;
                        fer = new ViewEventRecorderWrapper(
                                new ArcEventRecorder(view.getVariable(), propagators[p], v, solver),
                                view.getModifier(),
                                solver);
                    } else {
                        fer = new ArcEventRecorder(variable, propagators[p], v, solver);
                    }
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
