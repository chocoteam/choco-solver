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
package solver.propagation.generator.sorter.evaluator;

import solver.constraints.propagators.Propagator;
import solver.recorders.IEventRecorder;
import solver.variables.IntVar;
import solver.variables.Variable;

/**
 * A factory of most common evaluators.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 09/03/12
 */
public class EvtRecEvaluators {

    private EvtRecEvaluators() {
    }

    /**
     * Evaluate the maximum priority among propagators of an event recorder
     */
    public static IEvaluator<IEventRecorder> MaxPriorityC = new IEvaluator<IEventRecorder>() {
        @Override
        public int eval(IEventRecorder eventRecorder) {
            Propagator[] propagators = eventRecorder.getPropagators();
            int p = propagators[0].getPriority().priority;
            for (int i = 1; i < propagators.length; i++) {
                int pp = propagators[i].getPriority().priority;
                if (pp > p) {
                    p = pp;
                }
            }
            return p;
        }
    };

    /**
     * Evaluate the maximum priority among propagators of an event recorder
     */
    public static IEvaluator<IEventRecorder> MaxDynPriorityC = new IEvaluator<IEventRecorder>() {
        @Override
        public int eval(IEventRecorder eventRecorder) {
            Propagator[] propagators = eventRecorder.getPropagators();
            int a = propagators[0].dynPriority();
            for (int i = 1; i < propagators.length; i++) {
                int pp = propagators[i].arity();
                if (pp > a) {
                    a = pp;
                }
            }
            return a;
        }
    };

    /**
     * Evaluate the minimum priority among propagators of an event recorder
     */
    public static IEvaluator<IEventRecorder> MinPriorityC = new IEvaluator<IEventRecorder>() {
        @Override
        public int eval(IEventRecorder eventRecorder) {
            Propagator[] propagators = eventRecorder.getPropagators();
            int p = propagators[0].getPriority().priority;
            for (int i = 1; i < propagators.length; i++) {
                int pp = propagators[i].getPriority().priority;
                if (pp < p) {
                    p = pp;
                }
            }
            return p;
        }
    };

    /**
     * Evaluate the maximum arity among propagators of an event recorder
     */
    public static IEvaluator<IEventRecorder> MaxArityC = new IEvaluator<IEventRecorder>() {
        @Override
        public int eval(IEventRecorder eventRecorder) {
            Propagator[] propagators = eventRecorder.getPropagators();
            int p = propagators[0].arity();
            for (int i = 1; i < propagators.length; i++) {
                int pp = propagators[i].arity();
                if (pp > p) {
                    p = pp;
                }
            }
            return p;
        }
    };

    /**
     * Evaluate the minimum arity among propagators of an event recorder
     */
    public static IEvaluator<IEventRecorder> MinArityC = new IEvaluator<IEventRecorder>() {
        @Override
        public int eval(IEventRecorder eventRecorder) {
            Propagator[] propagators = eventRecorder.getPropagators();
            int p = propagators[0].getPriority().priority;
            for (int i = 1; i < propagators.length; i++) {
                int pp = propagators[i].arity();
                if (pp < p) {
                    p = pp;
                }
            }
            return p;
        }
    };


    /**
     * Evaluate the maximum arity among variables of an event recorder
     */
    public static IEvaluator<IEventRecorder> MaxArityV = new IEvaluator<IEventRecorder>() {
        @Override
        public int eval(IEventRecorder eventRecorder) {
            Variable[] variables = eventRecorder.getVariables();
            int a = variables[0].nbConstraints();
            for (int i = 1; i < variables.length; i++) {
                int aa = variables[i].nbConstraints();
                if (aa > a) {
                    a = aa;
                }
            }
            return a;
        }
    };

    /**
     * Evaluate the minimum arity among variables of an event recorder
     */
    public static IEvaluator<IEventRecorder> MinArityV = new IEvaluator<IEventRecorder>() {
        @Override
        public int eval(IEventRecorder eventRecorder) {
            Variable[] variables = eventRecorder.getVariables();
            int a = variables[0].nbConstraints();
            for (int i = 1; i < variables.length; i++) {
                int aa = variables[i].nbConstraints();
                if (aa < a) {
                    a = aa;
                }
            }
            return a;
        }
    };

    /**
     * Evaluate the maximum domain ssize among integer variables of an event recorder
     */
    public static IEvaluator<IEventRecorder<IntVar>> MaxDomSize = new IEvaluator<IEventRecorder<IntVar>>() {
        @Override
        public int eval(IEventRecorder<IntVar> eventRecorder) {
            Variable[] variables = eventRecorder.getVariables();
            int a = ((IntVar)variables[0]).getDomainSize();
            for (int i = 1; i < variables.length; i++) {
                int aa = ((IntVar) variables[i]).getDomainSize();
                if (aa > a) {
                    a = aa;
                }
            }
            return a;
        }
    };

    /**
     * Evaluate the mimimum domain ssize among integer variables of an event recorder
     */
    public static IEvaluator<IEventRecorder<IntVar>> MinDomSize = new IEvaluator<IEventRecorder<IntVar>>() {
        @Override
        public int eval(IEventRecorder<IntVar> eventRecorder) {
            Variable[] variables = eventRecorder.getVariables();
            int a = ((IntVar)variables[0]).getDomainSize();
            for (int i = 1; i < variables.length; i++) {
                int aa = ((IntVar) variables[i]).getDomainSize();
                if (aa < a) {
                    a = aa;
                }
            }
            return a;
        }
    };

}
