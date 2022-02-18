/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.loop;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.propagation.PropagationEngine;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;

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
            //System.out.printf("Solution: %s\n", Arrays.toString(model.getVars()));
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
            //System.out.printf("Solution: %s\n", Arrays.toString(model.getVars()));
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
            //System.out.printf("Solution: %s\n", Arrays.toString(model.getVars()));
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
            //System.out.printf("Solution: %s\n", Arrays.toString(model.getVars()));
        }
        return c;
    }

    public static void main(String[] args) {
        Model model = new Model();
        IntVar[] X = model.intVarArray("X", 3, 0, 2, false);
//        model.post(solver.allDifferent(X));
        Solver r = model.getSolver();
        r.setEngine(new PropagationEngine(model));
        r.getEngine().initialize();
//        System.out.printf("%d solutions\n", setDFS(solver, ISF.lexico_LB(X)));
        //System.out.printf("%d solutions\n", lds(model, inputOrderLBSearch(X), 3));
//        for (int d = 2; d < 3; d++) {
//            System.out.printf("%d solutions\n", ilds(solver, ISF.lexico_LB(X), d, X.length));
//        }
    }

}
