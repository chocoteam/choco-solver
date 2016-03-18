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
package org.chocosolver.solver.search.bind;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.chocosolver.solver.search.strategy.SearchStrategyFactory.*;
import static org.chocosolver.solver.search.strategy.selectors.ValSelectorFactory.maxRealVal;
import static org.chocosolver.solver.search.strategy.selectors.ValSelectorFactory.minRealVal;
import static org.chocosolver.solver.search.strategy.selectors.VarSelectorFactory.roundRobinVar;

/**
 * A search binder, which configures but not overrides, a search strategy if none is defined.
 * The method is called after the initial propagation step, for single model.
 * <br/>
 *
 * @author Charles Prud'homme
 * @version choco
 * @since 23/10/14
 */
public class DefaultSearchBinder implements ISearchBinder {

    @Override
    public void configureSearch(Model model) {
        if(model.getSettings().warnUser()) {
            model.getSolver().getErr().printf("No search strategies defined.\nSet to default ones.");
        }

        Solver r = model.getSolver();
        r.set(getDefault(model));
        r.set(lastConflict(r.getStrategy()));
    }

    public AbstractStrategy[] getDefault(Model model) {
        Solver r = model.getSolver();
        AbstractStrategy[] strats = new AbstractStrategy[4];
        int nb = 0;

        // 1. retrieve variables, keeping the declaration order, and put them in four groups:
        // a. integer and boolean variables
        List<IntVar> livars = new ArrayList<>();
        // b. set variables
        List<SetVar> lsvars = new ArrayList<>();
        // c. real variables.
        List<RealVar> lrvars = new ArrayList<>();
        Variable[] variables = model.getVars();
        Variable objective = null;
        for (Variable var : variables) {
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
        if (r.getObjectiveManager().isOptimization()) {
            objective = r.getObjectiveManager().getObjective();
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
            strats[nb++] = intVarSearch(ivars);
        }

        // SET VARIABLES DEFAULT SEARCH STRATEGY
        // b. MinDelta + min domain
        SetVar[] svars = lsvars.toArray(new SetVar[lsvars.size()]);
        if (svars.length > 0) {
            strats[nb++] = setVarSearch(svars);
        }

        // REAL VARIABLES DEFAULT SEARCH STRATEGY
        // c. cyclic + middle
        RealVar[] rvars = lrvars.toArray(new RealVar[lrvars.size()]);
        if (rvars.length > 0) {
            strats[nb++] = realVarSearch(rvars);
        }

        // d. lexico LB/UB for the objective variable
        if (objective != null) {
            boolean max = r.getObjectiveManager().getPolicy() == ResolutionPolicy.MAXIMIZE;
            int kind = objective.getTypeAndKind() & Variable.KIND;
            switch (kind) {
                case Variable.INT:
                case Variable.BOOL:
                    if (max) {
                        strats[nb++] = minDomUBSearch((IntVar) objective);
                    } else {
                        strats[nb++] = minDomLBSearch((IntVar) objective);
                    }
                    break;
                case Variable.REAL:
                    if (max) {
                        strats[nb++] = realVarSearch(roundRobinVar(), maxRealVal(), (RealVar) objective);
                    } else {
                        strats[nb++] = realVarSearch(roundRobinVar(), minRealVal(), (RealVar) objective);
                    }
                    break;
                default:
                    throw new SolverException("Unknown variable type '" + kind + "' while defining the default search strategy.");
            }
        }

        if (nb == 0) {
            // simply to avoid null pointers in case all variables are instantiated
            strats[nb++] = minDomLBSearch(model.ONE());
        }
        return Arrays.copyOf(strats, nb);
    }
}
