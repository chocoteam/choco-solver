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
package org.chocosolver.parser.flatzinc;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.bind.DefaultSearchBinder;
import org.chocosolver.solver.search.strategy.ISF;
import org.chocosolver.solver.search.strategy.SSF;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.Variable;

import java.util.*;

/**
 * Created by cprudhom on 08/12/14.
 * Project: choco-parsers.
 */
public class FznSearchBinder extends DefaultSearchBinder {
    @Override
    public void configureSearch(Solver solver) {
        AbstractStrategy current = solver.getStrategy();
        if (current == null) {
            super.configureSearch(solver);
        } else {
            // 1. extract variables
            Variable[] allVars = current.getVariables();
            HashSet<Variable> declaredVariables = new HashSet<>(Arrays.asList(allVars));
            // 2. sort them by type
            List<IntVar> decintvars = new ArrayList<>();
            List<IntVar> intvars = new ArrayList<>();
            List<BoolVar> decboolvars = new ArrayList<>();
            List<BoolVar> boolvars = new ArrayList<>();
            List<SetVar> decsetvars = new ArrayList<>();
            List<SetVar> setvars = new ArrayList<>();
            for (Variable v : allVars) {
                switch (v.getTypeAndKind() & Variable.KIND) {
                    case Variable.BOOL:
                        if ((v.getTypeAndKind() & Variable.TYPE) == Variable.VAR) {
                            if (declaredVariables.contains(v)) {
                                decboolvars.add((BoolVar) v);
                            } else {
                                boolvars.add((BoolVar) v);
                            }
                        }
                        break;
                    case Variable.INT:
                        if ((v.getTypeAndKind() & Variable.TYPE) == Variable.VAR) {
                            if (declaredVariables.contains(v)) {
                                decintvars.add((IntVar) v);
                            } else {
                                intvars.add((IntVar) v);
                            }
                        }
                        break;
                    case Variable.SET:
                        if ((v.getTypeAndKind() & Variable.TYPE) == Variable.VAR) {
                            if (declaredVariables.contains(v)) {
                                decsetvars.add((SetVar) v);
                            } else {
                                setvars.add((SetVar) v);
                            }
                        }
                        break;
                    default:
                        throw new UnsupportedOperationException("Unknown type of variable: " + v);
                }
            }

            if (decboolvars.size() == 0) {
                decboolvars.addAll(boolvars);
                boolvars.clear();
            }
            if (decintvars.size() == 0) {
                decintvars.addAll(intvars);
                intvars.clear();
            }
            if (decsetvars.size() == 0) {
                decsetvars.addAll(decsetvars);
                decsetvars.clear();
            }

            // Make main search strategy
            List<AbstractStrategy> strats = new ArrayList<>();
            if (decintvars.size() > 0) {
                strats.add(ISF.domOverWDeg(decintvars.toArray(new IntVar[decintvars.size()]), 29091981L));
            }
            if (decboolvars.size() > 0) {
                strats.add(ISF.lexico_UB(decboolvars.toArray(new BoolVar[decboolvars.size()])));
            }
            if (decsetvars.size() > 0) {
                strats.add(SSF.force_first(decsetvars.toArray(new SetVar[decsetvars.size()])));
            }

            // Make complementary search
            intvars.addAll(boolvars);

            if (intvars.size() > 0) {
                strats.add(makeComplementarySearch(intvars.toArray(new IntVar[intvars.size()])));
            }
            if (setvars.size() > 0) {
                System.err.println("% No complementary search for SetVar");
            }

            // declare the search strategy
            if (strats.size() == 1) {
                solver.set(strats.get(0));
            } else {
                solver.set(ISF.sequencer(strats.toArray(new AbstractStrategy[strats.size()])));
            }
        }
    }

    /**
     * Create a complementary search for IntVar.
     * <p/>
     * TODO: select only output variables ?
     *
     * @param ivars remaining variables
     */
    private static AbstractStrategy makeComplementarySearch(IntVar[] ivars) {
        Arrays.sort(ivars, new Comparator<IntVar>() {
            @Override
            public int compare(IntVar o1, IntVar o2) {
                return o1.getDomainSize() - o2.getDomainSize();
            }
        });
        return new AbstractStrategy<IntVar>(ivars) {
            boolean created = false;
            Decision d = new Decision<IntVar>() {

                @Override
                public void apply() throws ContradictionException {
                    for (int i = 0; i < ivars.length; i++) {
                        if (!ivars[i].isInstantiated()) {
                            ivars[i].instantiateTo(ivars[i].getLB(), this);
                            ivars[i].getSolver().propagate();
                        }
                    }
                }

                @Override
                public Object getDecisionValue() {
                    return null;
                }

                @Override
                public void free() {
                    created = false;
                }

                @Override
                public String toString() {
                    StringBuilder st = new StringBuilder("(once)");
                    for (int i = 0; i < ivars.length; i++) {
                        if (!ivars[i].isInstantiated()) {
                            st.append(ivars[i]).append("=").append(ivars[i].getLB()).append(", ");
                        }
                    }
                    return st.toString();
                }
            };

            @Override
            public void init() throws ContradictionException {
            }

            @Override
            public Decision<IntVar> getDecision() {
                if (!created) {
                    created = true;
                    d.once(true);
                    return d;
                }
                return null;
            }
        };
    }
}