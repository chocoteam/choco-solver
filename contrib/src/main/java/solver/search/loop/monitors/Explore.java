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
package solver.search.loop.monitors;

import choco.kernel.ESat;
import solver.Cause;
import solver.ICause;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.exception.ContradictionException;
import solver.propagation.IPropagationEngine;
import solver.propagation.IPropagationStrategy;
import solver.recorders.coarse.AbstractCoarseEventRecorder;
import solver.recorders.fine.AbstractFineEventRecorder;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.Variable;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 29/06/12
 */
public class Explore {

    protected final Solver solver;
    protected final int[] solution;
    protected final int[] ubs;
    protected final IntVar[] vars;

    public Explore(Solver solver, IntVar[] vars) {
        this.solver = solver;
        this.vars = vars;
        int n = vars.length;
        this.solution = new int[n];
        this.ubs = new int[n];
    }


    public boolean explore(boolean all) {
        // 1. temporary set a default engine
        IPropagationEngine current = solver.getEngine();
        solver.set(new NoPropagation());
        // 1. push a world
        solver.getEnvironment().worldPush();
        boolean isSat = false;

        int n = vars.length;
        Set<Constraint> cons_set = new LinkedHashSet<Constraint>();
        for (int i = 0; i < n; i++) {
            if (vars[i].instantiated()) {
                solution[i] = ubs[i] = vars[i].getValue();
            } else {
                solution[i] = vars[i].getLB();
                ubs[i] = vars[i].getUB();
                cons_set.addAll(Arrays.asList(vars[i].getConstraints()));
            }
        }
        Constraint[] constraints = cons_set.toArray(new Constraint[1]);
        if (checkSolution(constraints)) {
            isSat = true;
        } else {
            boolean run = true;
            while (run) {
                solver.getEnvironment().worldPop();
                solver.getEnvironment().worldPush();
                int i = n - 1;
                while (i >= 0 && next(i)) {
                    i--;
                }
                if (i == -1) {
                    run = false;
                } else {
                    forceSolution();
                    if (checkSolution(constraints)) {
                        run = all;
                        isSat = true;
                    }
                }
            }
        }
        solver.getEnvironment().worldPop();
        solver.set(current);
        return isSat;
    }

    private boolean next(int i) {
        if (solution[i] == ubs[i]) {
            solution[i] = vars[i].getLB();
            return true;
        } else {
            solution[i] = vars[i].nextValue(solution[i]);
            return false;
        }
    }

    private void forceSolution() {
        try {
            for (int j = 0; j < vars.length; j++) {
                vars[j].instantiateTo(solution[j], Cause.Null);
            }
        } catch (ContradictionException e) {
            e.printStackTrace();
        }
    }


    private boolean checkSolution(Constraint[] constraints) {
        for (int c = 0; c < constraints.length; c++) {
            ESat satC = constraints[c].isEntailed();
            if (!ESat.TRUE.equals(satC)) {
                return false;
            }
        }
//        System.out.printf("%s\n", Arrays.toString(solver.getVars()));
        return true;
    }

    public static class NoPropagation implements IPropagationEngine {

        @Override
        public boolean initialized() {
            return false;
        }

        @Override
        public void init(Solver solver) {
        }

        @Override
        public boolean forceActivation() {
            return false;
        }

        @Override
        public IPropagationEngine set(IPropagationStrategy propagationStrategy) {
            return null;
        }

        @Override
        public void propagate() throws ContradictionException {
        }

        @Override
        public void flush() {
        }

        @Override
        public void fails(ICause cause, Variable variable, String message) throws ContradictionException {
        }

        @Override
        public ContradictionException getContradictionException() {
            return null;
        }

        @Override
        public void clear() {
        }

        @Override
        public void prepareWM(Solver solver) {
        }

        @Override
        public void clearWatermark(int id1, int id2) {
        }

        @Override
        public boolean isMarked(int id1, int id2) {
            return false;
        }

        @Override
        public void onVariableUpdate(Variable variable, EventType type, ICause cause) throws ContradictionException {
        }

        @Override
        public void onPropagatorExecution(Propagator propagator) {
        }

        @Override
        public void activatePropagator(Propagator propagator) {
        }

        @Override
        public void desactivatePropagator(Propagator propagator) {
        }

        @Override
        public void addEventRecorder(AbstractFineEventRecorder fer) {
        }

        @Override
        public void addEventRecorder(AbstractCoarseEventRecorder er) {
        }

        @Override
        public void activateFineEventRecorder(AbstractFineEventRecorder fer) {
        }

        @Override
        public void desactivateFineEventRecorder(AbstractFineEventRecorder fer) {
        }
    }
}
