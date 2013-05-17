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

package solver.search.loop;

import solver.Solver;
import solver.exception.ContradictionException;
import solver.exception.SolverException;
import solver.search.strategy.IntStrategyFactory;
import solver.search.strategy.decision.Decision;
import solver.search.strategy.decision.RootDecision;
import solver.variables.VariableFactory;
import util.ESat;

/**
 * This is the default implementation of {@link AbstractSearchLoop} abstract class.
 *
 * @author Xavier Lorca
 * @author Charles Prud'homme
 * @version 0.01, june 2010
 * @since 0.01
 */
public class BinarySearchLoop extends AbstractSearchLoop {

    @SuppressWarnings({"unchecked"})
    BinarySearchLoop(Solver solver) {
        super(solver);
    }

    /**
     * {@inheritDoc}
     */
    protected void initialPropagation() {
        this.env.worldPush();
        try {
            solver.getEngine().propagate();
        } catch (ContradictionException e) {
            this.env.worldPop();
            solver.setFeasible(ESat.FALSE);
            solver.getEngine().flush();
            interrupt();
            return;
        }
        this.env.worldPush(); // push another wolrd to recover the state after initial propagation
        this.searchWorldIndex = env.getWorldIndex();
        // call to HeuristicVal.update(Action.initial_propagation)
        if (strategy == null) {
            //LoggerFactory.getLogger("solver").info("Set default search strategy: Dow/WDeg");
            solver.set(IntStrategyFactory.firstFail_InDomainMin(VariableFactory.castToIntVar(solver.getVars())));
//            set(StrategyFactory.firstFail_InDomainMin(VariableFactory.castToIntVar(solver.getVars()), solver.getEnvironment()));
        }
        try {
            strategy.init(); // the initialisation of the strategy can detect inconsistency
        } catch (ContradictionException cex) {
            this.env.worldPop();
            solver.setFeasible(ESat.FALSE);
            solver.getEngine().flush();
            interrupt();
        }
        moveTo(OPEN_NODE);
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Computes the next decision to apply, move to the next step in the run loop.
     * If there are no next decision, the current state is considered as a solution.
     * Once the solution has been stored -- and checked--, the next step in the tree search is computed: continue or stop.
     */
    @Override
    protected void openNode() {
        Decision tmp = decision;
        decision = strategy.getDecision();
        if (decision != null) { // null means there is no more decision
            decision.setPrevious(tmp);
            moveTo(DOWN_LEFT_BRANCH);
        } else {
            decision = tmp;
            recordSolution();
        }
    }

    protected void recordSolution() {
        //todo: checker d'etat
        solver.setFeasible(ESat.TRUE);
        assert (ESat.TRUE.equals(solver.isEntailed())) : Reporting.fullReport(solver);
        solutionpool.recordSolution(solver);
        objectivemanager.update();
        if (stopAtFirstSolution) {
            interrupt();
        } else {
            moveTo(stateAfterSolution);
        }
        smList.onSolution();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void resume() {
        if (nextState == INIT) {
            throw new SolverException("the search loop has not been initialized.\n " +
                    "This appears when 'nextSolution' is called before 'findSolution'.");
        } else if (nextState != RESUME) {
            throw new SolverException("The search cannot be resumed.");
        }
        moveTo(stateAfterSolution);
        loop();
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Backs up the current state.
     * Then gets the current computed decision,applies it to the problem and calls the main propagation.
     */
    @Override
    protected void downLeftBranch() {
        downBranch();
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Backs up the current state.
     * Then gets the current computed decision,applies it to the problem and calls the main propagation.
     */
    @Override
    protected void downRightBranch() {
        downBranch();
    }

    protected void downBranch() {
        env.worldPush();
        try {
            decision.buildNext();
            objectivemanager.apply(decision);
            objectivemanager.postDynamicCut();

            solver.getEngine().propagate();
            moveTo(OPEN_NODE);
        } catch (ContradictionException e) {
            solver.getEngine().flush();
            moveTo(stateAfterFail);
            jumpTo = 1;
            smList.onContradiction(e);
        }
    }


    /**
     * {@inheritDoc}
     * Rolls back the previous state.
     * Then, if it goes back to the base world, stop the search.
     * Otherwise, gets the opposite decision, applies it and calls the propagation.
     */
    @Override
    protected void upBranch() {
        env.worldPop();
        //if (env.getWorldIndex() <= searchWorldIndex ){// Issue#55
        if (decision == RootDecision.ROOT) {// Issue#55
            // The entire tree search has been explored, the search cannot be followed
            interrupt();
        } else {
            jumpTo--;
            if (jumpTo <= 0 && decision.hasNext()) {
                moveTo(DOWN_RIGHT_BRANCH);
            } else {
                Decision tmp = decision;
                decision = decision.getPrevious();
                tmp.free();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void restartSearch() {
        restoreRootNode();
        solver.getEnvironment().worldPush(); //issue#55
        try {
            objectivemanager.postDynamicCut();
            solver.getEngine().propagate();
            nextState = OPEN_NODE;
        } catch (ContradictionException e) {
            interrupt();
        }
    }

    /**
     * Required method to be sure a restart is taken into account.
     * Because, restart limit checker are threads, si they can interrupt the search loop at any moment.
     * And the interruption must not be forget and replaced by the wrong next state.
     * <br/>
     * <b>Beware, if this method is called from RESTART case, it leads to an infinite loop!</b>
     *
     * @param to STEP to reach
     */
    public void moveTo(int to) {
        if ((nextState & RESTART) == 0) {
            nextState = to;
        }
    }

    @Override
    public String decisionToString() {
        return decision.toString();
    }
}
