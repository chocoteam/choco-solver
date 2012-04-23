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
import solver.propagation.generator.*;
import solver.variables.Variable;

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
            Primitive arcs = Primitive.arcs(constraints);
            Primitive coarses = Primitive.coarses(constraints);
            return Queue.build(Flatten.build(arcs, coarses)).clearOut();
        }
    },
    TWO_QUEUES_WITH_ARCS() {
        @SuppressWarnings({"unchecked"})
        public PropagationStrategy make(Solver solver) {
            Constraint[] constraints = solver.getCstrs();
            Queue arcs = Queue.build(Primitive.arcs(constraints));
            Queue coarses = Queue.build(Primitive.coarses(constraints));
            //return Sort.build(arcs.clearOut(), coarses.pickOne()).clearOut();
            return Sort.build(arcs.clearOut(), coarses.pickOne()).clearOut();
        }
    },
    ONE_QUEUE_WITH_VARS() {
        @SuppressWarnings({"unchecked"})
        public PropagationStrategy make(Solver solver) {
            Variable[] variables = solver.getVars();
            Primitive arcs = Primitive.vars(variables);
            Constraint[] constraints = solver.getCstrs();
            Primitive coarses = Primitive.coarses(constraints);
            return Queue.build(Flatten.build(arcs, coarses)).clearOut();
        }
    },
    TWO_QUEUES_WITH_VARS() {
        @SuppressWarnings({"unchecked"})
        public PropagationStrategy make(Solver solver) {
            Variable[] variables = solver.getVars();
            Constraint[] constraints = solver.getCstrs();
            Queue arcs = Queue.build(Primitive.vars(variables));
            Queue coarses = Queue.build(Primitive.coarses(constraints));
            //return Sort.build(arcs.clearOut(), coarses.pickOne()).clearOut();
            return Queue.build(arcs.clearOut(), coarses.pickOne()).clearOut();
        }
    },
    ONE_QUEUE_WITH_PROPS() {
        @SuppressWarnings({"unchecked"})
        public PropagationStrategy make(Solver solver) {
            Constraint[] constraints = solver.getCstrs();
            Primitive arcs = Primitive.props(constraints);
            Primitive coarses = Primitive.coarses(constraints);
            return Queue.build(Flatten.build(arcs, coarses)).clearOut();
        }
    },
    TWO_QUEUES_WITH_PROPS() {
        @SuppressWarnings({"unchecked"})
        public PropagationStrategy make(Solver solver) {
            Constraint[] constraints = solver.getCstrs();
            Queue arcs = Queue.build(Primitive.props(constraints));
            Queue coarses = Queue.build(Primitive.coarses(constraints));
            //return Sort.build(arcs.clearOut(), coarses.pickOne()).clearOut();
            return Queue.build(arcs.clearOut(), coarses.pickOne()).clearOut();
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
