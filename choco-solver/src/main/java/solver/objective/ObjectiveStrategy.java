/*
 * Copyright (c) 1999-2014, Ecole des Mines de Nantes
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

/**
 * Created by IntelliJ IDEA.
 * User: Jean-Guillaume Fages
 * Date: 25/05/12
 * Time: 15:39
 */

package solver.objective;

import solver.ICause;
import solver.ResolutionPolicy;
import solver.Solver;
import solver.exception.ContradictionException;
import solver.search.loop.monitors.SMF;
import solver.search.strategy.assignments.DecisionOperator;
import solver.search.strategy.decision.Decision;
import solver.search.strategy.decision.fast.FastDecision;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.variables.IntVar;
import util.PoolManager;

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
    private Solver solver;
    private PoolManager<FastDecision> pool;
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
        this.solver = obj.getSolver();
        this.firstCall = true;
        this.coefLB = coefs[0];
        this.coefUB = coefs[1];
        this.optPolicy = policy;
		SMF.restartAfterEachSolution(solver);
        if (coefLB < 0 || coefUB < 0 || coefLB + coefUB == 0) {
            throw new UnsupportedOperationException("coefLB<0, coefUB<0 and coefLB+coefUB==0 are forbidden");
        }
        if (coefLB + coefUB != 1 && policy != OptimizationPolicy.DICHOTOMIC) {
            throw new UnsupportedOperationException("Invalid coefficients for BOTTOM_UP or TOP_DOWN optimization" +
                    "\nuse signature public ObjectiveStrategy(IntVar obj, OptimizationPolicy policy, Solver solver) instead");
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

    @Override
    public void init() {
        decOperator = getOperator(optPolicy, solver.getObjectiveManager().getPolicy());
    }

    @Override
    public Decision getDecision() {
        if (solver.getMeasures().getSolutionCount() == 0
                || (nbSols == solver.getMeasures().getSolutionCount() && optPolicy == OptimizationPolicy.DICHOTOMIC)) {
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
        nbSols = solver.getMeasures().getSolutionCount();
        globalLB = Math.max(globalLB, obj.getLB());//check
        globalUB = Math.min(globalUB, obj.getUB());//check
//        ObjectiveManager man = solver.getSearchLoop().getObjectiveManager();
//        man.updateLB(globalLB);
//        man.updateUB(globalUB);
        if (globalLB > globalUB) {
            return null;
        }
		if(LOGGER.isDebugEnabled())
			LOGGER.debug("% objective in [" + globalLB + ", " + globalUB + "]");
        int target;
        target = (globalLB * coefLB + globalUB * coefUB) / (coefLB + coefUB);
        FastDecision dec = pool.getE();
        if (dec == null) dec = new FastDecision(pool);
        dec.set(obj, target, decOperator);
		if(LOGGER.isDebugEnabled())
			LOGGER.debug("% trying " + obj+" "+(decOperator==decUB?"<=":">=")+" "+target);
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
//            solver.getSearchLoop().getObjectiveManager().updateLB(globalLB);
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
//            solver.getSearchLoop().getObjectiveManager().updateUB(globalUB);
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
