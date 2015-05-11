/**
 * Copyright (c) 2014,
 *       Charles Prud'homme (TASC, INRIA Rennes, LINA CNRS UMR 6241),
 *       Jean-Guillaume Fages (COSLING S.A.S.).
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.solver.search.bind;

import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.search.strategy.ISF;
import org.chocosolver.solver.search.strategy.RSF;
import org.chocosolver.solver.search.strategy.RealStrategyFactory;
import org.chocosolver.solver.search.strategy.SSF;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.*;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A search binder, which configures but not overrides, a search strategy if none is defined.
 * The method is called after the initial propagation step, for single solver.
 * <br/>
 *
 * @author Charles Prud'homme
 * @version choco
 * @since 23/10/14
 */
public class DefaultSearchBinder implements ISearchBinder {

    @Override
    public void configureSearch(Solver solver) {
        LoggerFactory.getLogger(ISearchBinder.class).warn("No search strategies defined");
        LoggerFactory.getLogger(ISearchBinder.class).warn("Set to default ones");

        solver.set(getDefault(solver));
        // + last conflict
        solver.set(ISF.lastConflict(solver));
    }

    public AbstractStrategy[] getDefault(Solver solver) {
        AbstractStrategy[] strats = new AbstractStrategy[4];
        int nb = 0;

        // 1. retrieve variables, keeping the declaration order, and put them in four groups:
        // a. integer and boolean variables
        List<IntVar> livars = new ArrayList<>();
        // b. set variables
        List<SetVar> lsvars = new ArrayList<>();
        // c. real variables.
        List<RealVar> lrvars = new ArrayList<>();
        Variable[] variables = solver.getVars();
        Variable objective = null;
        int n = variables.length;
        for (int i = 0; i < n; i++) {
            Variable var = variables[i];
            int type = var.getTypeAndKind();
            if ((type & Variable.CSTE) == 0) {
                int kind = type & Variable.KIND;
                switch (kind) {
                    case Variable.INT:
                        livars.add((IntVar) var);
                        break;
                    case Variable.BOOL:
                        livars.add((BoolVar) var);
                        break;
                    case Variable.SET:
                        lsvars.add((SetVar) var);
                        break;
                    case Variable.REAL:
                        lrvars.add((RealVar) var);
                        break;
                    default:
                        throw new SolverException("Unknown variable type '" + kind + "' while defining the default search strategy.");
                }
            }
        }
        // d. extract the objective variable if any
        if (solver.getSearchLoop().getObjectiveManager().isOptimization()) {
            objective = solver.getSearchLoop().getObjectiveManager().getObjective();
            int kind = objective.getTypeAndKind() & Variable.KIND;
            switch (kind) {
                case Variable.INT:
                case Variable.BOOL:
                    livars.remove(objective);
                    break;
                case Variable.SET:
                    lsvars.remove(objective);
                    break;
                case Variable.REAL:
                    lrvars.remove(objective);
                    break;
                default:
                    throw new SolverException("Unknown variable type '" + kind + "' while defining the default search strategy.");
            }
        }

        // 2. Apply
        // INTEGER VARIABLES DEFAULT SEARCH STRATEGY
        // a. Dom/Wdeg on integer/boolean variables
        IntVar[] ivars = livars.toArray(new IntVar[livars.size()]);
        if (ivars.length > 0) {
            strats[nb++] = ISF.domOverWDeg(ivars, 0);
        }

        // SET VARIABLES DEFAULT SEARCH STRATEGY
        // b. MinDelta + min domain
        SetVar[] svars = lsvars.toArray(new SetVar[lsvars.size()]);
        if (svars.length > 0) {
            strats[nb++] = SSF.force_minDelta_first(svars);
        }

        // REAL VARIABLES DEFAULT SEARCH STRATEGY
        // c. cyclic + middle
        RealVar[] rvars = lrvars.toArray(new RealVar[lrvars.size()]);
        if (rvars.length > 0) {
            strats[nb++] = RealStrategyFactory.cyclic_middle(rvars);
        }

        // d. lexico LB/UB for the objective variable
        if (objective != null) {
            boolean max = solver.getSearchLoop().getObjectiveManager().getPolicy() == ResolutionPolicy.MAXIMIZE;
            int kind = objective.getTypeAndKind() & Variable.KIND;
            switch (kind) {
                case Variable.INT:
                case Variable.BOOL:
                    if (max) {
                        strats[nb++] = ISF.minDom_UB((IntVar) objective);
                    } else {
                        strats[nb++] = ISF.minDom_LB((IntVar) objective);
                    }
                    break;
                case Variable.REAL:
                    if (max) {
                        strats[nb++] = RSF.custom(RealStrategyFactory.cyclic(), RSF.max_value_selector(), (RealVar) objective);
                    } else {
                        strats[nb++] = RSF.custom(RealStrategyFactory.cyclic(), RSF.min_value_selector(), (RealVar) objective);
                    }
                    break;
                default:
                    throw new SolverException("Unknown variable type '" + kind + "' while defining the default search strategy.");
            }
        }

        if (nb == 0) {
            // simply to avoid null pointers in case all variables are instantiated
            strats[nb++] = ISF.minDom_LB(solver.ONE);
        }
        return Arrays.copyOf(strats, nb);
    }
}
