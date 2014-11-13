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
package solver.explanations.strategies;

import solver.ICause;
import solver.Solver;
import solver.exception.ContradictionException;
import solver.explanations.*;
import solver.search.loop.lns.neighbors.ANeighbor;
import solver.search.loop.monitors.IMonitorUpBranch;
import solver.search.strategy.decision.Decision;
import solver.search.strategy.decision.RootDecision;
import util.tools.StatisticUtils;

import java.util.*;

/**
 * a specific neighborhood for LNS based on the explanation of the cut imposed by a new solution.
 * <p/>
 * This neighborhood is specific in the sense that it needs to compute explanation after a new solution has been found.
 * Furthermore, the fixSomeVariables method creates and applies decisions, so that the explanation recorder can infer.
 * <br/>
 * It works as follow:
 * - on a solution: force the application of the cut together with the decision path which leads to the solution, explain the failure
 * - then, on a call to fixSomeVariables, it selects randomly K decisions explaining the cut, and relax them from the decision path.
 * <p/>
 * Unrelated decisions are never relaxed, the idea here is to work only on the decisions which lead to a failure.
 *
 * @author Charles Prud'homme
 * @since 03/07/13
 */
public class ExplainingCut extends ANeighbor implements IMonitorUpBranch {

    protected final ExplanationEngine mExplanationEngine; // the explanation engine -- it works faster when it's a lazy one
    protected final Random random;

    private ArrayList<Decision> path; // decision path that leads to a solution

    private BitSet related2cut; // a bitset indicating which decisions of the path are related to the cut
    private BitSet notFrozen;
    private BitSet refuted;
    private BitSet unrelated;
    private boolean forceCft; // does the cut has already been explained?
    private boolean isTerminated; // if explanations do not contain decisions, then the optimality has been proven

    private double nbFixedVariables = 0d; // number of decision to fix in the set of decisions explaining the cut
    private int nbCall, limit;
    private final int level; // relaxing factor

    private Decision last; // needed to catch up the case when a subtree is closed, and this imposes the fgmt

    // TEMPORARY DATA STRUCTURES
    private final ArrayList<Deduction> tmpDeductions;
    private final Set<Deduction> tmpValueDeductions;


    public ExplainingCut(Solver aSolver, int level, long seed) {
        super(aSolver);
        if (!(aSolver.getExplainer() instanceof LazyExplanationEngineFromRestart)) {
            aSolver.set(new LazyExplanationEngineFromRestart(aSolver));
        }
        this.mExplanationEngine = aSolver.getExplainer();
        this.level = level;
        this.random = new Random(seed);

        path = new ArrayList<>(16);
        related2cut = new BitSet(16);
        notFrozen = new BitSet(16);
        unrelated = new BitSet(16);
        refuted = new BitSet(16);
        // TEMPORARY DATA STRUCTURES
        tmpDeductions = new ArrayList<>(16);
        tmpValueDeductions = new HashSet<>(16);
        mSolver.getSearchLoop().plugSearchMonitor(this);
    }

    @Override
    public void recordSolution() {
        clonePath();
        forceCft = true;
    }

    @Override
    public void fixSomeVariables(ICause cause) throws ContradictionException {
        // this is called after restart
        // if required, force the cut and explain the cut
        if (forceCft) {
            explainCut();
            nbFixedVariables = related2cut.cardinality();
            nbCall = 0;
            increaseLimit();
        }
        // then fix variables
        // this part is specific: a fake decision path has to be created
        nbCall++;
        restrictLess();
        notFrozen.clear();
        notFrozen.or(related2cut);
        for (; !notFrozen.isEmpty() && notFrozen.cardinality() > nbFixedVariables; ) {
            int idx = selectVariable();
            notFrozen.clear(idx);
        }
        assert mSolver.getSearchLoop().getLastDecision() == RootDecision.ROOT;
        // add the first refuted decisions
        int first = notFrozen.nextSetBit(0);
        for (int i = (first>-1?refuted.nextSetBit(first):first); i > -1; i = refuted.nextSetBit(i + 1)) {
            notFrozen.clear(i);
        }

        // add unrelated
        notFrozen.or(unrelated);

        // then build the fake decision path
        last = null;
//        LOGGER.debug("relax cut {}", notFrozen.cardinality());
        for (int id = notFrozen.nextSetBit(0); id >= 0 && id < path.size(); id = notFrozen.nextSetBit(id + 1)) {
//            last = ExplanationToolbox.mimic(path.get(id)); // required because some unrelated decisions can be refuted
            if (path.get(id).hasNext()) {
                last = path.get(id).duplicate();
                if (refuted.get(id)) last.buildNext();
                ExplanationToolbox.imposeDecisionPath(mSolver, last);
            }
        }
    }

    @Override
    public void restrictLess() {
        if (nbCall > limit) {
            nbFixedVariables = random.nextDouble() * related2cut.cardinality();
            increaseLimit();
        }
        last = null;
    }

    @Override
    public boolean isSearchComplete() {
        return isTerminated;
    }

    @Override
    public void beforeUpBranch() {
    }

    @Override
    public void afterUpBranch() {
        // we need to catch up that case when the sub tree is closed and this imposes a fragment
        if (last != null && mSolver.getSearchLoop().getLastDecision().getId() == last.getId()) {
            mSolver.getSearchLoop().restart();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    private void increaseLimit() {
        long ank = (long) (1.2 * StatisticUtils.binomialCoefficients(related2cut.cardinality(), (int) nbFixedVariables - 1));
        int step = (int) Math.min(ank, level);
        limit = nbCall + step;
    }

    private int selectVariable() {
        int id;
        int cc = random.nextInt(notFrozen.cardinality());
        for (id = notFrozen.nextSetBit(0); id >= 0 && cc > 0; id = notFrozen.nextSetBit(id + 1)) {
            cc--;
        }
        return id;
    }


    /**
     * Compute the initial fragment, ie set of decisions to keep.
     */
    private void clonePath() {
        Decision dec = mSolver.getSearchLoop().getLastDecision();
        while ((dec != RootDecision.ROOT)) {
            addToPath(dec);
            dec = dec.getPrevious();
        }
        Collections.reverse(path);
        int size = path.size();
        for (int i = 0, mid = size >> 1, j = size - 1; i < mid; i++, j--) {
            boolean bi = refuted.get(i);
            refuted.set(i, refuted.get(j));
            refuted.set(j, bi);
        }
    }


    /**
     * Add a copy of the current decision to path
     *
     * @param dec a decision of the current decision path
     */
    private void addToPath(Decision dec) {
        Decision clone = dec.duplicate();
        path.add(clone);
        int pos = path.size() - 1;
        if (!dec.hasNext()) {
            refuted.set(pos);
        }
        /*boolean forceNext = !dec.hasNext();
        if (forceNext) {
            clone.buildNext(); // force to set up the decision in the very state it was
            clone.buildNext(); // that's why we call it twice
        }*/
    }

    /**
     * Force the failure, apply decisions to the last solution + cut => failure!
     */
    private void explainCut() {
        // Goal: force the failure to get the set of decisions related to the cut
        forceCft = false;
        // 1. make a backup
        mSolver.getEnvironment().worldPush();
        Decision d;
        try {

            Decision previous = mSolver.getSearchLoop().getLastDecision();
            assert previous == RootDecision.ROOT;
            // 2. apply the decisions
            mExplanationEngine.getSolver().getObjectiveManager().postDynamicCut();
            for (int i = 0; i < path.size(); i++) {
                d = path.get(i);
                d.setPrevious(previous);
                d.buildNext();
                if (refuted.get(i)) d.buildNext();
                d.apply();
                mSolver.propagate();
                previous = d;
            }
            //mSolver.propagate();
            assert false : "SHOULD FAIL!";
        } catch (ContradictionException cex) {
            if ((cex.v != null) || (cex.c != null)) { // contradiction on domain wipe out
                tmpDeductions.clear();
                tmpValueDeductions.clear();
                related2cut.clear();
                unrelated.clear();

                // 3. explain the failure
                Explanation expl = new Explanation();
                if (cex.v != null) {
                    cex.v.explain(VariableState.DOM, expl);
                } else {
                    cex.c.explain(null, expl);
                }
                Explanation complete = mExplanationEngine.flatten(expl);
                ExplanationToolbox.extractDecision(complete, tmpValueDeductions);
                tmpDeductions.addAll(tmpValueDeductions);

                if (tmpDeductions.isEmpty()) {
//                    if (LOGGER.isErrorEnabled()) {
//                        LOGGER.error("2 cases: (a) optimality proven or (b) bug in explanation");
//                    }
//                    throw new SolverException("2 cases: (a) optimality proven or (b) bug in explanation");
                    isTerminated = true;
                }

                for (int i = 0; i < tmpDeductions.size(); i++) {
                    int idx = path.indexOf(((BranchingDecision) tmpDeductions.get(i)).getDecision());
                    related2cut.set(idx);
                }

                // 4. need to replace the duplicated decision with the correct one
                for (int i = 0; i < path.size(); i++) {
                    Decision dec = path.get(i);
                    boolean forceNext = !dec.hasNext();
                    dec.rewind();
                    if (forceNext) dec.buildNext();
                    dec.setPrevious(null); // useless .. but ... you know
                }

            } else {
                throw new UnsupportedOperationException(this.getClass().getName() + ".onContradiction incoherent state");
            }
        }
        mSolver.getEnvironment().worldPop();
        mSolver.getEngine().flush();
        unrelated.andNot(related2cut);
        unrelated.andNot(refuted);
    }

}
