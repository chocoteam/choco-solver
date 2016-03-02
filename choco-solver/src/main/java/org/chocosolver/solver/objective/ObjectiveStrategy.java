/**
 * Copyright (c) 2015, Ecole des Mines de Nantes
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
/**
 * Created by IntelliJ IDEA.
 * User: Jean-Guillaume Fages
 * Date: 25/05/12
 * Time: 15:39
 */

package org.chocosolver.solver.objective;

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.strategy.assignments.DecisionOperator;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.search.strategy.decision.IntDecision;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.PoolManager;

import static org.chocosolver.solver.objective.OptimizationPolicy.DICHOTOMIC;

/**
 * Class that defines a branching strategy over the objective variable
 *
 * @author Jean-Guillaume Fages
 * @since Oct. 2012
 */
public class ObjectiveStrategy extends AbstractStrategy<IntVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private int globalLB, globalUB;
    private int coefLB, coefUB;
    private IntVar obj;
    private long nbSols;
    private Model model;
    private PoolManager<IntDecision> pool;
    private boolean firstCall;
    private DecisionOperator<IntVar> decOperator;
    private OptimizationPolicy optPolicy;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * Defines a branching strategy over the objective variable
	 * BEWARE: only activated after a first solution
     *
     * @param objective variable
     * @param policy    BOTTOM_UP, TOP_TOWN or DICHOTOMIC
     */
    public ObjectiveStrategy(IntVar objective, OptimizationPolicy policy) {
        this(objective, getCoefs(policy), policy);
    }

    /**
     * Defines a parametrized dichotomic branching over the objective variable
	 * BEWARE: only activated after a first solution
     *
     * @param objective variable
     * @param coefs     [a,b] defines how to split the domain of the objective variable
     *                  [1,1] will halve its domain
     *                  [1,2] will take a value closer to the upper bound than the lower bound
     * @param policy    should be DICHOTOMIC
     */
    public ObjectiveStrategy(IntVar objective, int[] coefs, OptimizationPolicy policy) {
        super(new IntVar[]{objective});
        this.pool = new PoolManager<>();
        this.obj = objective;
        this.model = obj.getModel();
        this.firstCall = true;
        this.coefLB = coefs[0];
        this.coefUB = coefs[1];
        this.optPolicy = policy;
        model.getSolver().setRestartOnSolutions();
        if (coefLB < 0 || coefUB < 0 || coefLB + coefUB == 0) {
            throw new UnsupportedOperationException("coefLB<0, coefUB<0 and coefLB+coefUB==0 are forbidden");
        }
        if (coefLB + coefUB != 1 && policy != DICHOTOMIC) {
            throw new UnsupportedOperationException("Invalid coefficients for BOTTOM_UP or TOP_DOWN optimization" +
                    "\nuse signature public ObjectiveStrategy(IntVar obj, OptimizationPolicy policy, Model model) instead");
        }
    }

    private static int[] getCoefs(OptimizationPolicy policy) {
        switch (policy) {
            case BOTTOM_UP:
                return new int[]{1, 0};
            case TOP_DOWN:
                return new int[]{0, 1};
            case DICHOTOMIC:
                return new int[]{1, 1};
            default:
                throw new UnsupportedOperationException("unknown OptimizationPolicy " + policy);
        }
    }

    private DecisionOperator<IntVar> getOperator(OptimizationPolicy optPolicy, ResolutionPolicy resoPolicy) {
        switch (optPolicy) {
            case BOTTOM_UP:
                return decUB;
            case TOP_DOWN:
                return incLB;
            case DICHOTOMIC:
                switch (resoPolicy) {
                    case MINIMIZE:
                        return decUB;
                    case MAXIMIZE:
                        return incLB;
                }
            default:
                throw new UnsupportedOperationException("unknown OptimizationPolicy " + optPolicy + " or ResolutionPolicy " + resoPolicy);
        }
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    public boolean init() {
        decOperator = getOperator(optPolicy, model.getSolver().getObjectiveManager().getPolicy());
        return true;
    }
    @Override
    public Decision getDecision() {
        if (model.getSolver().getSolutionCount() == 0
                || (nbSols == model.getSolver().getSolutionCount() && optPolicy == OptimizationPolicy.DICHOTOMIC)) {
            return null;
        }
        if (obj.isInstantiated()) {
            return null;
        }
        if (firstCall) {
            firstCall = false;
            globalLB = obj.getLB();
            globalUB = obj.getUB();
        }
        nbSols = model.getSolver().getSolutionCount();
        globalLB = Math.max(globalLB, obj.getLB());//check
        globalUB = Math.min(globalUB, obj.getUB());//check
//        ObjectiveManager man = model.getResolver().getObjectiveManager();
//        man.updateLB(globalLB);
//        man.updateUB(globalUB);
        if (globalLB > globalUB) {
            return null;
        }

        if(model.getSettings().warnUser()){
            model.getSolver().getErr().printf("- objective in [" + globalLB + ", " + globalUB + "]\n");
        }
        int target;
        target = (globalLB * coefLB + globalUB * coefUB) / (coefLB + coefUB);
        IntDecision dec = pool.getE();
        if (dec == null) dec = new IntDecision(pool);
        dec.set(obj, target, decOperator);
        if(model.getSettings().warnUser()){
            model.getSolver().getErr().printf("- trying " + obj + " " + (decOperator == decUB ? "<=" : ">=") + " " + target+"\n");
        }
        return dec;
    }

    private DecisionOperator<IntVar> decUB = new DecisionOperator<IntVar>() {
        @Override
        public void apply(IntVar var, int value, ICause cause) throws ContradictionException {
            var.updateUpperBound(value, cause);
        }

        @Override
        public void unapply(IntVar var, int value, ICause cause) throws ContradictionException {
            globalLB = value + 1;
//            model.getResolver().getObjectiveManager().updateLB(globalLB);
            var.updateLowerBound(globalLB, cause);
        }

        @Override
        public String toString() {
            return " objective split(" + coefLB + "," + coefUB + "), decreases the upper bound first";
        }

        @Override
        public boolean isValid(IntVar var, int value) {
            return var.getUB() > value;
        }

        @Override
        public DecisionOperator opposite() {
            return incLB;
        }
    };

    private DecisionOperator<IntVar> incLB = new DecisionOperator<IntVar>() {
        @Override
        public void apply(IntVar var, int value, ICause cause) throws ContradictionException {
            var.updateLowerBound(value, cause);
        }

        @Override
        public void unapply(IntVar var, int value, ICause cause) throws ContradictionException {
            globalUB = value - 1;
//            model.getResolver().getObjectiveManager().updateUB(globalUB);
            var.updateUpperBound(globalUB, cause);
        }

        @Override
        public String toString() {
            return " objective split(" + coefLB + "," + coefUB + "), increases the lower bound first";
        }

        @Override
        public boolean isValid(IntVar var, int value) {
            return var.getLB() < value;
        }

        @Override
        public DecisionOperator opposite() {
            return decUB;
        }
    };
}
