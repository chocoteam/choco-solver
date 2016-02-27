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
package org.chocosolver.solver.search.loop;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.propagation.hardcoded.SevenQueuesPropagatorEngine;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;

import java.util.Arrays;

import static org.chocosolver.solver.search.strategy.SearchStrategyFactory.inputOrderLBSearch;

/**
 * A list of service, just for fun
 * Created by cprudhom on 09/10/15.
 * Project: choco.
 */
class RecursiveSearchLoop {
    RecursiveSearchLoop() {
    }

    public static int dfs(Model model, AbstractStrategy strategy) {
        // some preprocess for the first call
        int c = 0;
        try {
            model.getSolver().getEngine().propagate();
        } catch (ContradictionException e) {
            return c;
        }
        Decision dec = strategy.getDecision();
        if (dec != null) {
            // apply the decision
            model.getEnvironment().worldPush();
            try {
                dec.buildNext();
                dec.apply();
                c += dfs(model, strategy);
            } catch (ContradictionException cex) {
                model.getSolver().getEngine().flush();
            }
            model.getEnvironment().worldPop();
            model.getEnvironment().worldPush();
            try {
                dec.buildNext();
                dec.apply();
                c += dfs(model, strategy);
            } catch (ContradictionException cex) {
                model.getSolver().getEngine().flush();
            }
            model.getEnvironment().worldPop();
        } else {
            assert model.getSolver().isSatisfied() == ESat.TRUE;
            c++;
            System.out.printf("Solution: %s\n", Arrays.toString(model.getVars()));
        }
        return c;
    }

    public static int lds(Model model, AbstractStrategy strategy, int dis) {
        int c = 0;
        try {
            model.getSolver().getEngine().propagate();
        } catch (ContradictionException e) {
            return c;
        }
        Decision dec = strategy.getDecision();
        if (dec != null) {
            // apply the decision
            model.getEnvironment().worldPush();
            try {
                dec.buildNext();
                dec.apply();
                c += lds(model, strategy, dis);
            } catch (ContradictionException cex) {
                model.getSolver().getEngine().flush();
            }
            model.getEnvironment().worldPop();
            if (dis > 0) {
                model.getEnvironment().worldPush();
                try {
                    dec.buildNext();
                    dec.apply();
                    c += lds(model, strategy, dis - 1);
                } catch (ContradictionException cex) {
                    model.getSolver().getEngine().flush();
                }
                model.getEnvironment().worldPop();
            }
        } else {
            assert model.getSolver().isSatisfied() == ESat.TRUE;
            c++;
            System.out.printf("Solution: %s\n", Arrays.toString(model.getVars()));
        }
        return c;
    }

    public static int dds(Model model, AbstractStrategy strategy, int dis, int dep) {
        int c = 0;
        try {
            model.getSolver().getEngine().propagate();
        } catch (ContradictionException e) {
            return c;
        }
        Decision dec = strategy.getDecision();
        if (dec != null) {
            // apply the decision
            if (dep >= dis) {
                model.getEnvironment().worldPush();
                try {
                    dec.buildNext();
                    dec.apply();
                    c += ilds(model, strategy, dis, dep - 1);
                } catch (ContradictionException cex) {
                    model.getSolver().getEngine().flush();
                }
                model.getEnvironment().worldPop();
            } else {
                dec.buildNext();
            }
            if (dis > 0) {
                model.getEnvironment().worldPush();
                try {
                    dec.buildNext();
                    dec.apply();
                    c += ilds(model, strategy, dis - 1, dep);
                } catch (ContradictionException cex) {
                    model.getSolver().getEngine().flush();
                }
                model.getEnvironment().worldPop();
            }
        } else if (dis == 0) {
            assert model.getSolver().isSatisfied() == ESat.TRUE;
            c++;
            System.out.printf("Solution: %s\n", Arrays.toString(model.getVars()));
        }
        return c;
    }


    public static int ilds(Model model, AbstractStrategy strategy, int dis, int dep) {
        int c = 0;
        try {
            model.getSolver().getEngine().propagate();
        } catch (ContradictionException e) {
            return c;
        }
        Decision dec = strategy.getDecision();
        if (dec != null) {
            // apply the decision
            if (dep >= dis) {
                model.getEnvironment().worldPush();
                try {
                    dec.buildNext();
                    dec.apply();
                    c += ilds(model, strategy, dis, dep - 1);
                } catch (ContradictionException cex) {
                    model.getSolver().getEngine().flush();
                }
                model.getEnvironment().worldPop();
            } else {
                dec.buildNext();
            }
            if (dis > 0) {
                model.getEnvironment().worldPush();
                try {
                    dec.buildNext();
                    dec.apply();
                    c += ilds(model, strategy, dis - 1, dep);
                } catch (ContradictionException cex) {
                    model.getSolver().getEngine().flush();
                }
                model.getEnvironment().worldPop();
            }
        } else if (dis == 0) {
            assert model.getSolver().isSatisfied() == ESat.TRUE;
            c++;
            System.out.printf("Solution: %s\n", Arrays.toString(model.getVars()));
        }
        return c;
    }

    public static void main(String[] args) {
        Model model = new Model();
        IntVar[] X = model.intVarArray("X", 3, 0, 2, false);
//        model.post(solver.allDifferent(X));
        Solver r = model.getSolver();
        r.set(new SevenQueuesPropagatorEngine(model));
        r.getEngine().initialize();
//        System.out.printf("%d solutions\n", setDFS(solver, ISF.lexico_LB(X)));
        System.out.printf("%d solutions\n", lds(model, inputOrderLBSearch(X), 3));
//        for (int d = 2; d < 3; d++) {
//            System.out.printf("%d solutions\n", ilds(solver, ISF.lexico_LB(X), d, X.length));
//        }
    }

}
