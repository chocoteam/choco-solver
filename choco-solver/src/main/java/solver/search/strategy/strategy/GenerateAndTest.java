/**
 *  Copyright (c) 1999-2014, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package solver.search.strategy.strategy;

import memory.structure.Operation;
import solver.ICause;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.Propagator;
import solver.exception.ContradictionException;
import solver.exception.SolverException;
import solver.propagation.IPropagationEngine;
import solver.search.strategy.decision.Decision;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.Variable;
import util.ESat;
import util.tools.ArrayUtils;

/**
 * A strategy, dedicated to IntVars, that simulate a Generate and Test behavior through a specific internal decision.
 * <br/>
 * The main idea is, from all the variables of a problem,  to generate and test the satisfiability of a complete instantiation.
 * The process does not rely on propagation anymore, but on satisfaction.
 * Thus, the propagation engine, when this strategy is called switch from a standard propagation engine to a dedicated one, which only checks
 * the satisfaction of the instantiation.
 *
 * @author Charles Prud'homme
 * @version choco
 * @since 21/08/2014
 */
public class GenerateAndTest extends AbstractStrategy<IntVar> {

    Solver solver;
    GenerateAndTestDecision gAtDec;
    GenerateAndTestPropagationEngine gAtPE;
    Operation restorePropagationEngine;
    int searchSpaceLimit = -1;
    AbstractStrategy<IntVar> mainStrategy = null;

    private static IntVar[] extractIntVars(Solver solver) {
        IntVar[] ivars = ArrayUtils.append(solver.retrieveBoolVars(), solver.retrieveIntVars());
        if (ivars.length != solver.getNbVars()) {
            throw new SolverException("GenerateAndTest search cannot be applied on non integer (and boolean) variables");
        }
        return ivars;
    }

    public GenerateAndTest(Solver solver) {
        super(extractIntVars(solver));
        this.solver = solver;
    }

    public GenerateAndTest(Solver solver, AbstractStrategy<IntVar> mainStrategy, int searchSpaceLimit) {
        super(extractIntVars(solver));
        this.solver = solver;
        this.searchSpaceLimit = searchSpaceLimit;
        this.mainStrategy = mainStrategy;
    }

    /**
     * Prepare <code>this</code> to be used in a search loop
     */
    @Override
    public void init() throws ContradictionException {
        gAtDec = new GenerateAndTestDecision(vars);
        gAtPE = new GenerateAndTestPropagationEngine(solver);
        final IPropagationEngine stdEngine = solver.getEngine();
        restorePropagationEngine = new Operation() {
            @Override
            public void undo() {
                solver.set(stdEngine);
            }
        };
    }

    /**
     * Provides access to the current decision in the strategy.
     * If there are no more decision to provide, it returns <code>null</code>.
     *
     * @return the current decision
     */
    @Override
    public Decision<IntVar> getDecision() {
        if (searchSpaceLimit > -1) {
            if (!remainingSpace(searchSpaceLimit)){
                return mainStrategy.getDecision();
            }
        }
        if (solver.getEngine() != gAtPE) {
            if (gAtDec.init()) {
                solver.getEnvironment().save(restorePropagationEngine);
                solver.set(gAtPE);
                return gAtDec;
            }
        }
        return null;
    }

    private boolean remainingSpace(int limit) {
        int size = 1;
        for (int i = 0; i < vars.length && size < limit; i++) {
            size *= vars[i].getDomainSize();
        }
        return size < limit;
    }

    //******************************************************************************************************************
    //******************************************************************************************************************
    //******************************************************************************************************************

    /**
     * A specific decision which is based on all the variables of a problem.
     * On a call to hasNext(), it generates the following instantiation to be tested.
     * An EnumDecision is not designed to be reused upon backtrack, unlike other decisions.
     */
    private static class GenerateAndTestDecision extends Decision<IntVar> {

        final IntVar[] variables;
        final int nVars;
        final int[] ivalues;
        boolean free;

        protected GenerateAndTestDecision(IntVar[] variables) {
            this.free = true;
            this.variables = variables;
            this.nVars = variables.length;
            this.ivalues = new int[nVars];
        }


        protected boolean init() {
            int nbInst = 0;
            for (int j = 0; j < nVars; j++) {
                ivalues[j] = variables[j].getLB();
                if (variables[j].isInstantiated()) nbInst++;
            }
            free = false;
            return nbInst < nVars;
        }

        /**
         * Compute if exists, the next instantiation to test.
         */
        @Override
        public boolean hasNext() {
            int vIdx;
            for (vIdx = 0; vIdx < nVars; vIdx++) {
                int v = ivalues[vIdx] = variables[vIdx].nextValue(ivalues[vIdx]);
                if (v < Integer.MAX_VALUE) {
                    return true;
                }
                ivalues[vIdx] = variables[vIdx].getLB();
            }
            return (vIdx < nVars);
        }

        /**
         * Do nothing, everything is achieved in hasNext()
         */
        @Override
        public void buildNext() {
        }

        @Override
        public void apply() throws ContradictionException {
            for (int i = 0; i < variables.length; i++) {
                if (!variables[i].isInstantiated()) {
                    variables[i].instantiateTo(ivalues[i], this);
                }
            }
        }

        @Override
        public Object getDecisionValue() {
            return ivalues;
        }

        @Override
        public void free() {
            free = true;
        }

        @Override
        public String toString() {
            StringBuilder st = new StringBuilder("[GenerateAndTest]<");
            for (int i = 0; i < variables.length; i++) {
                st.append(ivalues[i]).append(", ");
            }
            st.deleteCharAt(st.length() - 1);
            st.deleteCharAt(st.length() - 1);
            st.append(">");
            return st.toString();
        }
    }

    //******************************************************************************************************************
    //******************************************************************************************************************
    //******************************************************************************************************************

    /**
     * A specific propagation engine that does not propagate.
     * It can only be used in a GenerateAndTest framework and thus only needs to check
     * the satisfaction of a complete instantiation.
     */
    private static class GenerateAndTestPropagationEngine implements IPropagationEngine {

        private final ContradictionException e = new ContradictionException();

        Propagator[] propagators = new Propagator[0];

        private GenerateAndTestPropagationEngine(Solver solver) {
            for (Constraint c : solver.getCstrs()) {
                propagators = ArrayUtils.append(propagators, c.getPropagators());
            }
        }

        @Override
        public boolean isInitialized() {
            return true;
        }

        /**
         * The only method that justifies the creation of a concrete propagation engine.
         *
         * @throws ContradictionException
         */
        @Override
        public void propagate() throws ContradictionException {
            int sat = 0;
            Propagator ptmp;
            for (int i = 0; i < propagators.length; i++) {
                ptmp = propagators[i];
                if (ptmp.isActive()) { // only active propagators need to be checked, the other must alredy be satisfied
                    ESat entail = ptmp.isEntailed();
                    if (entail.equals(ESat.FALSE)) {
                        fails(ptmp, null, "GenerateAndTest");
                    } else if (entail.equals(ESat.TRUE)) {
                        sat++;
                    }
                } else {
                    sat++;
                }
            }
            if (sat != propagators.length) {
                throw new SolverException("GenerateAndTest has generated an incomplete instantiation");
            }
        }

        @Override
        public void flush() {
        }

        @Override
        public void fails(ICause cause, Variable variable, String message) throws ContradictionException {
            throw e.set(cause, variable, message);
        }

        @Override
        public ContradictionException getContradictionException() {
            return e;
        }

        @Override
        public void clear() {
        }

        @Override
        public void onVariableUpdate(Variable variable, EventType type, ICause cause) throws ContradictionException {
        }

        @Override
        public void delayedPropagation(Propagator propagator, EventType type) throws ContradictionException {

        }

        @Override
        public void onPropagatorExecution(Propagator propagator) {
        }

        @Override
        public void desactivatePropagator(Propagator propagator) {
        }

        @Override
        public void dynamicAddition(Constraint c, boolean permanent) {
            throw new SolverException("GenerateAndTest does not support propagator dynamic addition");
        }

        @Override
        public void dynamicDeletion(Constraint c) {
            throw new SolverException("GenerateAndTest does not support propagator dynamic deletion");
        }
    }
}
