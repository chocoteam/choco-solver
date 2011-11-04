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

package solver.constraints.probabilistic.propagators.nary;

import solver.Solver;
import solver.constraints.IntConstraint;
import solver.constraints.probabilistic.IProbaPropagator;
import solver.constraints.propagators.nary.PropAllDiffBC;
import solver.requests.ConditionnalRequest;
import solver.requests.IRequest;
import solver.requests.conditions.AbstractCondition;
import solver.requests.conditions.CompletlyInstantiated;
import solver.variables.IntVar;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 25 nov. 2010
 */
public class PropProbaAllDiffBC extends PropAllDiffBC {

    public PropProbaAllDiffBC(IntVar[] vars, Solver solver, IntConstraint constraint) {
        super(vars, solver, constraint);
        //unionset = new Union(vars, environment);
    }

    // todo voir charles pour mettre cela en oeuvre
    @Override
    public IRequest<IntVar> makeRequest(IntVar var, int idx) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void linkToVariables() {
        //noinspection unchecked
        AbstractCondition condition = new CompletlyInstantiated(this.environment, vars.length / 2);
        for (int i = 0; i < vars.length; i++) {
            vars[i].updatePropagationConditions(this, i);
            ConditionnalRequest crequest =
                    new ConditionnalRequest<PropProbaAllDiffBC>(this, vars[i], i, condition, this.environment);
            this.addRequest(crequest);
            vars[i].addRequest(requests[i]);
            condition.linkRequest(crequest);
        }
    }
    
}
