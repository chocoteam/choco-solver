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
import solver.constraints.propagators.PropagatorPriority;
import solver.propagation.generator.*;
import solver.propagation.generator.sorter.Increasing;
import solver.propagation.generator.sorter.evaluator.EvtRecEvaluators;
import solver.variables.IntVar;
import solver.variables.Variable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 08/12/11
 */
public enum PropagationStrategies {

    ONE_QUEUE_WITH_ARCS() {
        @SuppressWarnings({"unchecked"})
        public PropagationStrategy make(Solver solver) {
            Constraint[] constraints = solver.getCstrs();
            PArc arcs = new PArc(constraints);
            PCoarse coarses = new PCoarse(constraints);
            return new Queue(arcs, coarses).clearOut();
        }
    },
    TWO_QUEUES_WITH_ARCS() {
        @SuppressWarnings({"unchecked"})
        public PropagationStrategy make(Solver solver) {
            Constraint[] constraints = solver.getCstrs();
            Queue arcs = new Queue(new PArc(constraints));
            Queue coarses = new Queue(new PCoarse(constraints));
            return new Sort(arcs.clearOut(), coarses.pickOne()).clearOut();
        }
    },
    PRIORITY_QUEUES_WITH_ARCS() {
        @SuppressWarnings({"unchecked"})
        public PropagationStrategy make(Solver solver) {
            Constraint[] constraints = solver.getCstrs();
            ArrayList<Propagator>[] queues = new ArrayList[PropagatorPriority.VERY_SLOW.priority + 1];
            for (int i = 0; i < constraints.length; i++) {
                Propagator[] propagators = constraints[i].propagators;
                for (int j = 0; j < propagators.length; j++) {
                    if (queues[propagators[j].getPriority().priority] == null) {
                        queues[propagators[j].getPriority().priority] = new ArrayList<Propagator>();
                    }
                    queues[propagators[j].getPriority().priority].add(propagators[j]);
                }
            }
            ArrayList<PropagationStrategy> real_q = new ArrayList<PropagationStrategy>();
            for (int i = 0; i < queues.length; i++) {
                if (queues[i] != null) {
                    real_q.add(
                            new Queue(new PArc(
                                    queues[i].toArray(new Propagator[queues[i].size()])
                            )
                            ).pickOne()
                    );
                }
            }
            real_q.add(new Queue(new PCoarse(constraints)).pickOne());
            return new Sort(real_q.toArray(new PropagationStrategy[real_q.size()])).clearOut();
        }
    },
    ONE_QUEUE_WITH_VARS() {
        @SuppressWarnings({"unchecked"})
        public PropagationStrategy make(Solver solver) {
            Variable[] variables = solver.getVars();
            PVar arcs = new PVar(variables);
            Constraint[] constraints = solver.getCstrs();
            PCoarse coarses = new PCoarse(constraints);
            return new Queue(arcs, coarses).clearOut();
        }
    },
    TWO_QUEUES_WITH_VARS() {
        @SuppressWarnings({"unchecked"})
        public PropagationStrategy make(Solver solver) {
            Variable[] variables = solver.getVars();
            Constraint[] constraints = solver.getCstrs();
            Queue arcs = new Queue(new PVar(variables));
            Sort coarses = new Sort(new Increasing(EvtRecEvaluators.MaxPriorityC), new PCoarse(constraints));
            return new Sort(coarses.pickOne(), arcs.clearOut()).clearOut();
        }
    },
    INCREASING_DEGREE_VARS() {
        @SuppressWarnings({"unchecked"})
        public PropagationStrategy make(Solver solver) {
            Variable[] variables = solver.getVars();
            Arrays.sort(variables, new Comparator<Variable>() {
                @Override
                public int compare(Variable o1, Variable o2) {
                    return ((IntVar) o1).getDomainSize() - ((IntVar) o2).getDomainSize();
                }
            });
            PropagationStrategy svar = new Sort(new PVar(variables)).clearOut();
            Constraint[] constraints = solver.getCstrs();
            return new Sort(svar, new Sort(
                    /*new Comparator<CoarseEventRecorder> (){
                        @Override
                        public int compare(CoarseEventRecorder o1, CoarseEventRecorder o2) {
                            return o1.getPropagators()[0].getPriority().priority -
                                    o2.getPropagators()[0].getPriority().priority;
                        }
                    },*/new PCoarse(
                    constraints)).pickOne()).clearOut();
        }
    },
    ONE_QUEUE_WITH_PROPS() {
        @SuppressWarnings({"unchecked"})
        public PropagationStrategy make(Solver solver) {
            Constraint[] constraints = solver.getCstrs();
            PCons arcs = new PCons(constraints);
            PCoarse coarses = new PCoarse(constraints);
            return new Queue(arcs, coarses).clearOut();
        }
    },
    TWO_QUEUES_WITH_PROPS() {
        @SuppressWarnings({"unchecked"})
        public PropagationStrategy make(Solver solver) {
            Constraint[] constraints = solver.getCstrs();
            Queue arcs = new Queue(new PCons(constraints));
            Queue coarses = new Queue(new PCoarse(constraints));
            return new Sort(coarses.pickOne(), arcs.clearOut()).clearOut();
        }
    },
    PRIORITY_QUEUES_WITH_PROPS() {
        @SuppressWarnings({"unchecked"})
        public PropagationStrategy make(Solver solver) {
            Constraint[] constraints = solver.getCstrs();
            ArrayList<Propagator>[] queues = new ArrayList[PropagatorPriority.VERY_SLOW.priority + 1];
            for (int i = 0; i < constraints.length; i++) {
                Propagator[] propagators = constraints[i].propagators;
                for (int j = 0; j < propagators.length; j++) {
                    if (queues[propagators[j].getPriority().priority] == null) {
                        queues[propagators[j].getPriority().priority] = new ArrayList<Propagator>();
                    }
                    queues[propagators[j].getPriority().priority].add(propagators[j]);
                }
            }
            ArrayList<PropagationStrategy> real_q = new ArrayList<PropagationStrategy>();
            for (int i = 0; i < queues.length; i++) {
                if (queues[i] != null) {
                    real_q.add(
                            new Queue(new PCons(
                                    queues[i].toArray(new Propagator[queues[i].size()])
                            )
                            ).pickOne()
                    );
                }
            }
            real_q.add(new Queue(new PCoarse(constraints)).pickOne());
            return new Sort(real_q.toArray(new PropagationStrategy[real_q.size()])).clearOut();
        }
    },
    DEFAULT() {
        @Override
        public PropagationStrategy make(Solver solver) {
            return TWO_QUEUES_WITH_ARCS.make(solver);
        }
    };

    public abstract PropagationStrategy make(Solver solver);
}
