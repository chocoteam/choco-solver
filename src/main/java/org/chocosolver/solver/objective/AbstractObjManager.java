/**
 * Copyright (c) 2016, Ecole des Mines de Nantes
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    This product includes software developed by the <organization>.
 * 4. Neither the name of the <organization> nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.solver.objective;

import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.variables.Variable;

import java.util.Objects;
import java.util.function.Function;

/**
 * This class defines common methods to COP based on maximization or minimization of integer or real variable
 * @author Jean-Guillaume Fages, Charles Prud'homme, Arnaud Malapert
 *
 */
abstract class AbstractObjManager<V extends Variable> implements IObjectiveManager<V> {

    /** The variable to optimize **/
    protected final V objective;

    /** Define how should the objective be optimize */
    protected final ResolutionPolicy policy;

    /** define the precision to consider a variable as instantiated **/
    protected final Number precision;

    /** best lower bound found so far **/
    protected Number bestProvedLB;

    /** best upper bound found so far **/
    protected Number bestProvedUB;

    /** Define how the cut should be update when posting the cut **/
    protected Function<Number, Number> cutComputer = n -> n; // walking cut by default

    public AbstractObjManager(AbstractObjManager<V> objman) {
        objective = objman.objective;
        policy = objman.policy;
        precision = objman.precision;
        bestProvedLB = objman.bestProvedLB;
        bestProvedUB = objman.bestProvedUB;
        cutComputer = objman.cutComputer;
    }


    public AbstractObjManager(V objective, ResolutionPolicy policy, Number precision) {
        super();
        Objects.nonNull(objective);
        this.objective = objective;
        Objects.nonNull(policy);
        this.policy = policy;
        Objects.nonNull(precision);
        this.precision = precision;
    }

    @Override
    public final V getObjective() {
        return objective;
    }

    @Override
    public final ResolutionPolicy getPolicy() {
        return policy;
    }

    @Override
    public final Number getBestLB() {
        return bestProvedLB;
    }

    @Override
    public final Number getBestUB() {
        return bestProvedUB;
    }

    @Override
    public final void setCutComputer(Function<Number, Number> cutComputer) {
        this.cutComputer = cutComputer;
    }

    @Override
    public final void setWalkingDynamicCut() {
        cutComputer = (obj) -> obj;
    }


}