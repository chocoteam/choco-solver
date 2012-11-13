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
package parser.flatzinc.ast.ext;

import solver.propagation.ISchedulable;
import solver.propagation.generator.PropagationStrategy;
import solver.recorders.fine.arc.FineArcEventRecorder;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 22/10/12
 */
public enum AttributeOperator {

    ANY {
        @Override
        public int evaluate(PropagationStrategy input, CombinedAttribute ca, int current) {
            if (current == ca.operators.size()) {
                return ca.attribute.eval((FineArcEventRecorder) input.array()[0]);
            } else {
                return ca.operators.get(current + 1).evaluate((PropagationStrategy) input.array()[0], ca, current + 1);
            }
        }
    }, MIN {
        @Override
        public int evaluate(PropagationStrategy input, CombinedAttribute ca, int current) {
            int min = Integer.MAX_VALUE;
            ISchedulable[] scheds = input.array();
            for (int i = 0; i < scheds.length; i++) {
                ISchedulable sched = scheds[i];
                int val;
                if (current == ca.operators.size()) {
                    val = ca.attribute.eval((FineArcEventRecorder) sched);
                } else {
                    val = ca.operators.get(current + 1).evaluate((PropagationStrategy) sched, ca, current + 1);
                }
                if (val < min) {
                    min = val;
                }
            }
            return min;
        }
    }, MAX {
        @Override
        public int evaluate(PropagationStrategy input, CombinedAttribute ca, int current) {
            int max = Integer.MIN_VALUE;
            ISchedulable[] scheds = input.array();
            for (int i = 0; i < scheds.length; i++) {
                ISchedulable sched = scheds[i];
                int val;
                if (current == ca.operators.size()) {
                    val = ca.attribute.eval((FineArcEventRecorder) sched);
                } else {
                    val = ca.operators.get(current + 1).evaluate((PropagationStrategy) sched, ca, current + 1);
                }
                if (val > max) {
                    max = val;
                }
            }
            return max;
        }
    }, SUM {
        @Override
        public int evaluate(PropagationStrategy input, CombinedAttribute ca, int current) {
            int sum = 0;
            ISchedulable[] scheds = input.array();
            for (int i = 0; i < scheds.length; i++) {
                ISchedulable sched = scheds[i];
                int val;
                if (current == ca.operators.size()) {
                    val = ca.attribute.eval((FineArcEventRecorder) sched);
                } else {
                    val = ca.operators.get(current + 1).evaluate((PropagationStrategy) sched, ca, current + 1);
                }
                sum += val;
            }
            return sum;
        }
    }, SIZE {
        @Override
        public int evaluate(PropagationStrategy input, CombinedAttribute ca, int current) {
            return input.size();
        }
    };

    public abstract int evaluate(PropagationStrategy input, CombinedAttribute ca, int current);

}
