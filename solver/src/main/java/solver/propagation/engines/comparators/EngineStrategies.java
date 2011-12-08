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

package solver.propagation.engines.comparators;

import solver.Solver;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 30/03/11
 */
public enum EngineStrategies {

   /* OLDEST {
        @Override
        public void defineIn(Solver solver) {
            IPropagationEngine engine = solver.getEngine();
            engine.setDeal(IPropagationEngine.Deal.SEQUENCE);
            engine.addGroup(Group.buildQueue(Predicates.all(), Policy.FIXPOINT));
        }
    },
    BI_OLDEST {
        @Override
        public void defineIn(Solver solver) {
            IPropagationEngine engine = solver.getEngine();
            engine.setDeal(IPropagationEngine.Deal.SEQUENCE);
            engine.addGroup(Group.buildQueue(Predicates.light(), Policy.FIXPOINT));
            engine.addGroup(Group.buildQueue(Predicates.all(), Policy.ONE));
        }
    },
    VARIABLE_LEX {
        @Override
        public void defineIn(Solver solver) {
            IPropagationEngine engine = solver.getEngine();
            engine.setDeal(IPropagationEngine.Deal.SEQUENCE);
            engine.addGroup(Group.buildGroup(
                    Predicates.all(),
                    new IncrOrderV(solver.getVars()),
                    Policy.FIXPOINT));
        }
    },
    CONSTRAINT_LEX {
        @Override
        public void defineIn(Solver solver) {
            IPropagationEngine engine = solver.getEngine();
            engine.setDeal(IPropagationEngine.Deal.SEQUENCE);
            engine.addGroup(Group.buildGroup(
                    Predicates.all(),
                    new IncrOrderC(solver.getCstrs()),
                    Policy.FIXPOINT));
        }
    },
    VARIABLE_ORIENTED {
        @Override
        public void defineIn(Solver solver) {
            IPropagationEngine engine = solver.getEngine();
            engine.setDeal(IPropagationEngine.Deal.QUEUE);
            Variable[] vars = solver.getVars();
            for (Variable var : vars) {
                engine.addGroup(
                        Group.buildGroup(
                                Predicates.member(var),
                                IncrPriorityP.get(),
                                Policy.ITERATE
                        )
                );
            }
            engine.addGroup(Group.buildQueue(
                    Predicates.all(),
                    Policy.ONE
            ));
        }
    }, CONSTRAINT_ORIENTED {
        @Override
        public void defineIn(Solver solver) {
            IPropagationEngine engine = solver.getEngine();
            engine.setDeal(IPropagationEngine.Deal.QUEUE);
            Constraint[] cstrs = solver.getCstrs();
            Predicate light = Predicates.light();
            for (final Constraint cstr : cstrs) {
                engine.addGroup(
                        Group.buildGroup(
                                Predicates.member_light(cstr),
                                new Seq(new IncrOrderV(cstr.getVariables()), IncrPriorityP.get()),
                                Policy.ITERATE
                        )
                );
            }
            engine.addGroup(Group.buildQueue(
                    Predicates.all(),
                    Policy.ONE
            ));
        }
    },
    PRIORITY_PROP {
        @Override
        public void defineIn(Solver solver) {
            IPropagationEngine engine = solver.getEngine();
            engine.setDeal(IPropagationEngine.Deal.QUEUE);
            for (PropagatorPriority p : PropagatorPriority.values()) {
                engine.addGroup(
                        Group.buildGroup(
                                Predicates.priority(p),
                                IncrArityC.get(),
                                Policy.ITERATE
                        )
                );
            }
        }
    },*/
    SHUFFLE {
        @Override
        public void defineIn(Solver solver) {
        }
    },
    DEFAULT {
        @Override
        public void defineIn(Solver solver) {
        }
    };

    public abstract void defineIn(Solver solver);


}
