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

package solver.search.strategy;

import solver.Solver;
import solver.search.strategy.strategy.LastConflict;
import solver.search.strategy.selectors.values.InDomainMax;
import solver.search.strategy.selectors.values.InDomainMiddle;
import solver.search.strategy.selectors.values.InDomainMin;
import solver.search.strategy.selectors.values.InDomainRandom;
import solver.search.strategy.selectors.variables.*;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.search.strategy.strategy.Assignment;
import solver.search.strategy.strategy.StaticStrategiesSequencer;
import solver.variables.IntVar;
import solver.variables.Variable;

/**
 * Strategies, Variable selectors and Value selectors factory.
 * Just there to simplify strategies creation.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 5 juil. 2010
 */
public class IntStrategyFactory {

    IntStrategyFactory() {
    }

    /**
     * Assignment strategy combining <code>InputOrder</code> and <code>InDomainMin</code>.
     *
     * @param VARS list of variables
     * @return assignment strategy
     */
    public static AbstractStrategy<IntVar> presetI(IntVar[] VARS) {
        return new Assignment(new InputOrder(VARS), new InDomainMin());
    }


    /**
     * Assignment strategy combining <code>InputOrder</code> and <code>InDomainMin</code>.
     *
     * @param VARS list of variables
     * @return assignment strategy
     */
    public static AbstractStrategy<IntVar> inputOrder_InDomainMin(IntVar[] VARS) {
        return new Assignment(new InputOrder(VARS), new InDomainMin());
    }

    /**
     * Assignment strategy combining <code>InputOrder</code> and <code>InDomainMin</code>
     *
     * @param VARS list of variables
     * @return assignment strategy
     */
    public static AbstractStrategy<IntVar> force_InputOrder_InDomainMin(Variable[] VARS) {
        IntVar[] ivars = new IntVar[VARS.length];
        for (int i = 0; i < VARS.length; i++) {
            ivars[i] = (IntVar) VARS[i];
        }
        return inputOrder_InDomainMin(ivars);
    }

    /**
     * Assignment strategy combining <code>InputOrder</code> and <code>InDomainMax</code>
     *
     * @param VARS list of variables
     * @return assignment strategy
     */
    public static AbstractStrategy<IntVar> inputOrder_InDomainMax(IntVar[] VARS) {
        return new Assignment(new InputOrder(VARS), new InDomainMax());
    }

    /**
     * Assignment strategy combining <code>FirstFail</code> and <code>InDomainMin</code>
     *
     * @param VARS list of variables
     * @return assignment strategy
     */
    public static AbstractStrategy<IntVar> firstFail_InDomainMin(IntVar[] VARS) {
        return new Assignment(new FirstFail(VARS), new InDomainMin());
    }


    /**
     * Assignment strategy combining <code>FirstFail</code> and <code>InDomainMiddle</code>
     *
     * @param VARS list of variables
     * @return assignment strategy
     */
    public static AbstractStrategy<IntVar> firstFail_InDomainMiddle(IntVar[] VARS) {
        return new Assignment(new FirstFail(VARS), new InDomainMiddle());
    }

    /**
     * Assignment strategy combining <code>FirstFail</code> and <code>InDomainMax</code>
     *
     * @param VARS list of variables
     * @return assignment strategy
     */
    public static AbstractStrategy<IntVar> firstFail_InDomainMax(IntVar[] VARS) {
        return new Assignment(new FirstFail(VARS), new InDomainMax());
    }

    /**
     * Assignment strategy combining <code>MaxRegret</code> and <code>InDomainMin</code>
     *
     * @param VARS list of variables
     * @return assignment strategy
     */
    public static AbstractStrategy<IntVar> maxReg_InDomainMin(IntVar[] VARS) {
        return new Assignment(new MaxRegret(VARS), new InDomainMin());
    }

    /**
     * Assignment strategy combining <code>Random</code> and <code>Random</code>
     *
     * @param VARS list of variables
     * @param SEED a seed for random
     * @return assignment strategy
     */
    public static AbstractStrategy<IntVar> random(IntVar[] VARS, long SEED) {
        return new Assignment(new Random(VARS, SEED), new InDomainRandom(SEED));
    }


    /**
     * Assignment strategy combining <code>DomOverWDeg</code> and <code>InDomainMin</code>
     *
     * @param VARS list of variables
     * @return assignment strategy
     */
    public static AbstractStrategy<IntVar> domOverWDeg_InDomainMin(IntVar[] VARS, long SEED) {
        return new Assignment(new DomOverWDeg(VARS, SEED), new InDomainMin());
    }

    /**
     * Create an Activity based search strategy.
     * <p/>
     * <b>"Activity-Based Search for Black-Box Constraint Propagramming Solver"<b/>,
     * Laurent Michel and Pascal Van Hentenryck, CPAIOR12.
     * <br/>
     *
     * @param VARS           collection of variables
     * @param GAMMA          aging parameters
     * @param DELTA          for interval domain size estimation
     * @param ALPHA          forget parameter
     * @param RESTART        restart parameter
     * @param FORCE_SAMPLING minimal number of iteration for sampling phase
     * @param SEED           the seed for random
     */
    public static AbstractStrategy<IntVar> ActivityBased(IntVar[] VARS, Solver solver, double GAMMA, double DELTA, int ALPHA,
                                                         double RESTART, int FORCE_SAMPLING, long SEED) {
        return new ActivityBased(solver, VARS, GAMMA, DELTA, ALPHA, RESTART, FORCE_SAMPLING, SEED);
    }

    /**
     * Create an Impact-based search strategy.
     * <p/>
     * <b>"Impact-Based Search Strategies for Constraint Programming",
     * Philippe Refalo, CP2004.</b>
     *
     * @param VARS       variables of the problem (should be integers)
     * @param ALPHA      aging parameter
     * @param SPLIT      split parameter for subdomains computation
     * @param NODEIMPACT force update of impacts every <code>nodeImpact</code> nodes. Set value to 0 to avoid using it.
     * @param SEED       a seed for random
     * @param INITONLY   only apply the initialisation phase, do not update impact thereafter
     */
    public static AbstractStrategy<IntVar> ImpactBased(IntVar[] VARS, int ALPHA, int SPLIT, int NODEIMPACT, long SEED, boolean INITONLY) {
        return new ImpactBased(VARS, ALPHA, SPLIT, NODEIMPACT, SEED, INITONLY);
    }

	/**
	 * Use the last conflict heuristic as a pluggin over a former search heuristic STRAT
	 *
	 * @param SOLVER
	 * @param STRAT
	 * @return last conflict strategy
	 */
	public static AbstractStrategy lastConflict(Solver SOLVER, AbstractStrategy STRAT) {
		return new StaticStrategiesSequencer(new LastConflict(SOLVER, STRAT, 1, false), STRAT);
	}
}
