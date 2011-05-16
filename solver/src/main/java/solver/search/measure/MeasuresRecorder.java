/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
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

package solver.search.measure;


import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.search.limits.TimeCacheThread;

public final class MeasuresRecorder implements IMeasures {

    public long solutionCount;

    public int objectiveIntValue = Integer.MAX_VALUE;

    public boolean objectiveOptimal;

    public boolean hasObjective;

    public long timeCount;

    public long readingTimeCount;

    public long initialisationTimeCount;

    public long initialPropagationTimeCount;

    public long nodeCount;

    public long backtrackCount;

    public long restartCount;

    public long failCount;

    public long propagationCount;

    public long usedMemory;

    protected Solver solver;

    protected long startingTime, startingMemory;

    protected Constraint[] cstrs;

    public MeasuresRecorder(Solver solver) {
        super();
        this.solver = solver;
    }

    public final void reset() {
        timeCount = 0;
        nodeCount = 0;
        backtrackCount = 0;
        restartCount = 0;
        failCount = 0;
    }

//	public void setSearchMeasures(ISearchMeasures toCopy) {
//		timeCount = toCopy.getTimeCount();
//		nodeCount = toCopy.getNodeCount();
//		backtrackCount = toCopy.getBackTrackCount();
//		restartCount = toCopy.getRestartCount();
//		failCount = toCopy.getFailCount();
//	}

    //****************************************************************************************************************//
    //**************************************** SETTERS ***************************************************************//
    //****************************************************************************************************************//

    @Override
    public long getSolutionCount() {
        return solutionCount;
    }

    @Override
    public int getObjectiveValue() {
        return objectiveIntValue;
    }

    @Override
    public void setObjectiveValue(int value) {
        objectiveIntValue = value;
    }

    @Override
    public void declareObjective() {
        hasObjective = true;
    }

    @Override
    public void setObjectiveOptimal(boolean objectiveOptimal) {
        this.objectiveOptimal = objectiveOptimal;
    }


    //****************************************************************************************************************//
    //**************************************** GETTERS ***************************************************************//
    //****************************************************************************************************************//

    @Override
    public long getBackTrackCount() {
        return backtrackCount;
    }

    @Override
    public long getFailCount() {
        return failCount;
    }

    @Override
    public long getNodeCount() {
        return nodeCount;
    }

    @Override
    public long getTimeCount() {
        return timeCount;
    }

    @Override
    public long getReadingTimeCount() {
        return readingTimeCount;
    }

    @Override
    public void setReadingTimeCount(long time) {
        this.readingTimeCount = time;
    }

    @Override
    public long getInitialisationTimeCount() {
        return initialisationTimeCount;
    }

    @Override
    public long getInitialPropagationTimeCount() {
        return initialPropagationTimeCount;
    }

    @Override
    public long getRestartCount() {
        return restartCount;
    }

    @Override
    public long getPropagationsCount() {
        return propagationCount;
    }

    @Override
    public boolean isObjectiveOptimal() {
        return objectiveOptimal;
    }

    @Override
    public boolean hasObjective() {
        return hasObjective;
    }

    @Override
    public long getUsedMemory() {
        return usedMemory;
    }


    static long memoryUsedInMB() {
        return Runtime.getRuntime().freeMemory() / 1024 / 1024;
    }

    /**
     * Updates the used memory
     */
    void updateMemoryUsed() {
        usedMemory = memoryUsedInMB() - startingMemory;
    }

    public void updatePropagationCount() {
        if (cstrs == null || cstrs.length != solver.getNbCstrs()) {
            cstrs = solver.getCstrs();
        }
        for (int i = 0; i < cstrs.length; i++) {
            Propagator[] propagators = cstrs[i].propagators;
            for (int j = 0; j < propagators.length; j++) {
                propagationCount += propagators[j].filterCall;
            }
        }

    }

    /**
     * Updates the time recorder
     */
    public void updateTimeCount() {
        timeCount = TimeCacheThread.currentTimeMillis - startingTime;
    }

    //****************************************************************************************************************//
    //**************************************** INCREMENTERS **********************************************************//
    //****************************************************************************************************************//

    @Override
    public void beforeInitialize() {
        reset();
        startingMemory = memoryUsedInMB();
        startingTime = System.currentTimeMillis();
        TimeCacheThread.currentTimeMillis = startingTime;
    }

    @Override
    public void afterInitialize() {
        updateTimeCount();
        initialisationTimeCount = TimeCacheThread.currentTimeMillis - startingTime;
    }

    @Override
    public void beforeInitialPropagation() {
    }

    @Override
    public void afterInitialPropagation() {
        initialPropagationTimeCount = TimeCacheThread.currentTimeMillis - startingTime;
        updateTimeCount();
    }

    @Override
    public void beforeOpenNode() {
        updateTimeCount();
    }

    @Override
    public void afterOpenNode() {
        nodeCount++;
    }

    @Override
    public void onSolution() {
        solutionCount++;
        updateTimeCount();
        updatePropagationCount();
    }

    @Override
    public void beforeDownLeftBranch() {
    }

    @Override
    public void afterDownLeftBranch() {
    }

    @Override
    public void beforeDownRightBranch() {

    }

    @Override
    public void afterDownRightBranch() {
    }

    @Override
    public void beforeUpBranch() {
    }

    @Override
    public void afterUpBranch() {
        backtrackCount++;

    }

    @Override
    public void onContradiction() {
        failCount++;
    }

    @Override
    public void beforeRestart() {
    }

    @Override
    public void afterRestart() {
        restartCount++;
    }

    @Override
    public void beforeClose() {
        updateTimeCount();
        updateMemoryUsed();
        updatePropagationCount();
    }

    @Override
    public void afterClose() {
    }


    //****************************************************************************************************************//
    //**************************************** PRINTERS **************************************************************//
    //****************************************************************************************************************//


    @Override
    public String toOneLineString() {
        StringBuilder st = new StringBuilder(256);
        st.append(String.format("%d Solutions, %sResolution %dms, %d Nodes, %d Backtracks, %d Fails, %d Restarts, %d Propagations",
                solutionCount, hasObjective() ? "Objective: " + objectiveIntValue + ", " : "", timeCount, nodeCount, backtrackCount, failCount, restartCount,
                propagationCount));
        return st.toString();
    }

    @Override
    public String toString() {
        StringBuilder st = new StringBuilder(256);
        st.append("- Search statistics\n");
        st.append(String.format("\tSolutions: %d\n\t%sBuilding time : %dms\n\tInitial propagation : %dms" +
                "\n\tResolution : %dms\n\tNodes: %d\n\tBacktracks: %d\n\tFails: %d\n\t" +
                "Restarts: %d\n\tPropagations: %d\n\tMemory: %dmb\n\tVariables: %d\n\tConstraints: %d\n\tRequests: %d",
                solutionCount, hasObjective() ? "Objective: " + objectiveIntValue + ", \n\t" : "", readingTimeCount,
                initialPropagationTimeCount, timeCount, nodeCount, backtrackCount, failCount, restartCount, propagationCount,
                usedMemory,
                solver.getVars().length, solver.getCstrs().length, solver.getEngine().getNbRequests()));
        return st.toString();
    }


}
