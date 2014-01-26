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

package solver.search.strategy.strategy;

import gnu.trove.map.hash.TLongObjectHashMap;
import memory.IEnvironment;
import memory.IStateBool;
import solver.search.strategy.assignments.DecisionOperator;
import solver.search.strategy.decision.Decision;
import solver.search.strategy.decision.fast.FastDecision;
import solver.search.strategy.enumerations.SortConductor;
import solver.search.strategy.enumerations.sorters.AbstractSorter;
import solver.search.strategy.enumerations.validators.IValid;
import solver.variables.IntVar;
import util.PoolManager;

/**
 * A specific class to build a decision based on :
 * - variable selection (dynamic)<br/>
 * - value selection<br/>
 * (- assignement selection)<br/>
 * <br/>
 * {@link StrategyVarValAssign#getDecision()} returns a decision object or null.
 * <p/>
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 15/12/10
 */
public class StrategyVarValAssign extends AbstractStrategy<IntVar> {

    final SortConductor<IntVar> varColl;

    TLongObjectHashMap<IStateBool> firstSelection;

    final PoolManager<FastDecision> decisionPool;

    DecisionOperator assignment;

    public static StrategyVarValAssign dyn(IntVar[] vars, AbstractSorter<IntVar> varComp,
                                           IValid<IntVar> varV, IEnvironment env) {
        return new StrategyVarValAssign(vars, varComp, varV, env, SortConductor.Type.DYN, DecisionOperator.int_eq);
    }

    public static StrategyVarValAssign dyn(IntVar[] vars, AbstractSorter<IntVar> varComp,
                                           IValid<IntVar> varV, DecisionOperator assignment,
                                           IEnvironment env) {
        return new StrategyVarValAssign(vars, varComp, varV, env, SortConductor.Type.DYN, assignment);
    }

    public static StrategyVarValAssign sta(IntVar[] vars, AbstractSorter<IntVar> varComp,
                                           IValid<IntVar> varV, IEnvironment env) {
        return new StrategyVarValAssign(vars, varComp, varV, env, SortConductor.Type.STA, DecisionOperator.int_eq);
    }

    public static StrategyVarValAssign sta(IntVar[] vars, AbstractSorter<IntVar> varComp,
                                           IValid<IntVar> varV, DecisionOperator assignment,
                                           IEnvironment env) {
        return new StrategyVarValAssign(vars, varComp, varV, env, SortConductor.Type.STA, assignment);
    }

    private StrategyVarValAssign(IntVar[] vars, AbstractSorter<IntVar> varComp,
                                 IValid<IntVar> varV, IEnvironment env, SortConductor.Type type,
                                 DecisionOperator assignment) {
        super(vars);
        switch (type) {
            case STA:
                this.varColl = SortConductor.sta(vars, varComp, varV, env);
                break;

            case DYN:
            default:
                this.varColl = SortConductor.dyn(vars, varComp, varV, env);
                break;

        }
        this.firstSelection = new TLongObjectHashMap<IStateBool>(vars.length);
        for (int i = 0; i < vars.length; i++) {
            firstSelection.put(vars[i].getId(), env.makeBool(false));
        }
        this.assignment = assignment;
        decisionPool = new PoolManager<FastDecision>();
    }

    @Override
    public void init() {
        for (int i = 0; i < vars.length; i++) {
//            vars[i].getHeuristicVal().update(Action.initial_propagation);
        }
    }

    @Override
    public Decision<IntVar> computeDecision(IntVar variable) {
        if (variable == null || variable.isInstantiated()) {
            return null;
        }
        // test on first selection of the variable
        if (!firstSelection.get(variable.getId()).get()) {
//			variable.getHeuristicVal().update(Action.first_selection);
            firstSelection.get(variable.getId()).set(true);
        }
//		variable.getHeuristicVal().update(Action.open_node);
//		if (variable.getHeuristicVal().hasNext()) {
//			int value = variable.getHeuristicVal().next();
//			FastDecision d = decisionPool.getE();
//			if (d == null) {
//				d = new FastDecision(decisionPool);
//			}
//			d.set(variable, value, assignment);
//			return d;
//		} else {
//			LoggerFactory.getLogger("solver").warn("StrategyVarValAssign : no value for var {}", variable.toString());
//			return null;
//		}
        return null;
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public Decision getDecision() {
        if (varColl.hasNext()) {
            IntVar var = varColl.next();
            return computeDecision(var);
        }
        return null;
    }
}
