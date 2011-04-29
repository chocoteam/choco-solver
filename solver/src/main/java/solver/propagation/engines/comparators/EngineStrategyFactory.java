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
import solver.constraints.Constraint;
import solver.propagation.engines.IPropagationEngine;
import solver.propagation.engines.Policy;
import solver.propagation.engines.comparators.predicate.EqualC;
import solver.propagation.engines.comparators.predicate.EqualV;
import solver.propagation.engines.group.Group;
import solver.variables.Variable;
import solver.views.IView;

import java.util.Comparator;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 30/03/11
 */
public class EngineStrategyFactory {

    public static final int
            QUEUE = -1,
            INPUT_ORDER_VAR = 0,
            ARITY_PROP = 1,
            PRIORITY_PROP = 2,
            SHUFFLE = 3,
            DOM_SIZE = 4,
            DOM_DEG = 5,
            DECR_DOM_DEG = 6,
            INPUT_ORDER_CSTR = 7,
            ARITY_VAR = 8,
            ARITY_CSTR = 9;

    private EngineStrategyFactory() {
    }

    public static Comparator<IView> comparator(Solver solver, int type) {
        switch (type) {
            case QUEUE:
                return Queue.get();
            case INPUT_ORDER_VAR:
                return new IncrOrderV(solver.getVars());
            case ARITY_PROP:
                return IncrArityP.get();
            case PRIORITY_PROP:
                return IncrPriorityP.get();
            case SHUFFLE:
                return new Shuffle();
            case DOM_SIZE:
                return IncrDomSize.get();
            case DOM_DEG:
                return IncrDomDeg.get();
            case DECR_DOM_DEG:
                return new Decr(IncrDomDeg.get());
            case INPUT_ORDER_CSTR:
                return new IncrOrderC(solver.getCstrs());
            case ARITY_VAR:
                return IncrArityV.get();
            case ARITY_CSTR:
                return IncrArityC.get();
            default:
                throw new UnsupportedOperationException("Unknown engine strategy");
        }
    }

    public static void variableOriented(Solver solver) {
        IPropagationEngine engine = solver.getEngine();
        engine.setDeal(IPropagationEngine.Deal.QUEUE);
        Variable[] vars = solver.getVars();
        for (Variable var : vars) {
            engine.addGroup(
                    new Group(
                            new EqualV(var),
                            IncrArityC.get(),
                            Policy.ITERATE
                    )
            );
        }
    }

    public static void constraintOriented(Solver solver) {
        IPropagationEngine engine = solver.getEngine();
        engine.setDeal(IPropagationEngine.Deal.QUEUE);
        Constraint[] cstrs = solver.getCstrs();
        for (Constraint cstr : cstrs) {
            engine.addGroup(
                    new Group(
                            new EqualC(cstr),
                            IncrArityV.get(),
                            Policy.ITERATE
                    )
            );
        }
    }

}
