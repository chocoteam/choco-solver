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
import solver.exception.ContradictionException;
import solver.search.loop.monitors.VoidSearchMonitor;

public final class MeasuresRecorder extends VoidSearchMonitor implements IMeasures {

    private static final float IN_MS = 1000 * 1000f;

    private static final float IN_SEC = 1000 * IN_MS;

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

    public long propagationCount, eventCount;

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
        solutionCount = 0;
        objectiveIntValue = Integer.MAX_VALUE;
        hasObjective = false;
        readingTimeCount = 0;
        initialisationTimeCount = 0;
        initialPropagationTimeCount = 0;
        propagationCount = 0;
        eventCount = 0;
        cstrs = null;
    }


    public long timestamp() {
        return nodeCount + backtrackCount;
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
    public float getTimeCount() {
        return timeCount / IN_MS;
    }

    @Override
    public float getReadingTimeCount() {
        return readingTimeCount / IN_MS;
    }

    @Override
    public void setReadingTimeCount(long time) {
        this.readingTimeCount = time;
    }

    @Override
    public float getInitialisationTimeCount() {
        return initialisationTimeCount / IN_MS;
    }

    @Override
    public float getInitialPropagationTimeCount() {
        return initialPropagationTimeCount / IN_MS;
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
    public long getEventsCount() {
        return eventCount;
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
        propagationCount = 0;
        eventCount = 0;
        for (int i = 0; i < cstrs.length; i++) {
            Propagator[] propagators = cstrs[i].propagators;
            for (int j = 0; j < propagators.length; j++) {
                propagationCount += propagators[j].coarseERcalls;
                eventCount += propagators[j].fineERcalls;
            }
        }


    }

    /**
     * Updates the time recorder
     */
    public void updateTimeCount() {
//        timeCount = TimeCacheThread.currentTimeMillis - startingTime;
        timeCount = System.nanoTime() - startingTime;
    }

    //****************************************************************************************************************//
    //**************************************** INCREMENTERS **********************************************************//
    //****************************************************************************************************************//

    @Override
    public void beforeInitialize() {
        reset();
        startingMemory = memoryUsedInMB();
        startingTime = System.nanoTime();
    }

    @Override
    public void afterInitialize() {
        initialisationTimeCount = System.nanoTime() - startingTime;
    }

    @Override
    public void afterInitialPropagation() {
        initialPropagationTimeCount = System.nanoTime() - startingTime;
    }

    @Override
    public void beforeOpenNode() {
//        updateTimeCount();
        nodeCount++;
    }

    @Override
    public void onSolution() {
        solutionCount++;
        updateTimeCount();
        updatePropagationCount();
    }

    @Override
    public void beforeUpBranch() {
        backtrackCount++;
    }

    @Override
    public void onContradiction(ContradictionException cex) {
        failCount++;
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

    //****************************************************************************************************************//
    //**************************************** PRINTERS **************************************************************//
    //****************************************************************************************************************//


    @Override
    public String toOneLineString() {
        StringBuilder st = new StringBuilder(256);
        st.append(String.format("%d Solutions, ", solutionCount));
        st.append(String.format("Building time : %.3fms, Initialisation : %.3fms, Initial propagation : %.3fms, " +
                "Resolution %.3fs (%.6fms), Total %.3fs, Objective: %d , %d Nodes, %d Backtracks, %d Fails, %d Restarts, %d + %d Propagations",
                readingTimeCount / IN_MS,
                initialisationTimeCount / IN_MS,
                (initialPropagationTimeCount - initialisationTimeCount) / IN_MS,
                (timeCount - initialPropagationTimeCount) / IN_SEC,
                (timeCount - initialPropagationTimeCount) / IN_MS,
                timeCount / IN_SEC,
                hasObjective() ? objectiveIntValue : 0,
                nodeCount,
                backtrackCount,
                failCount,
                restartCount,
                eventCount,
                propagationCount));
        return st.toString();
    }

    @Override
    public String toOneShortLineString() {
        StringBuilder st = new StringBuilder(256);
        st.append(String.format("%d Solutions, Resolution %.3fs (%.6fms), Objective: %d , %d Nodes, %d Backtracks, %d Fails, %d Restarts",
                solutionCount,
                (timeCount - initialPropagationTimeCount) / IN_SEC,
                (timeCount - initialPropagationTimeCount) / IN_MS,
                hasObjective() ? objectiveIntValue : 0,
                nodeCount,
                backtrackCount,
                failCount,
                restartCount));
        return st.toString();
    }

    @Override
    public String toString() {
        StringBuilder st = new StringBuilder(256);
        st.append("- Search statistics\n");
        st.append(String.format("\tSolutions: %,d\n", solutionCount));
        if (hasObjective()) {
            st.append(String.format("\tObjective: %,d\n", objectiveIntValue));
        }
        st.append(String.format("\tBuilding time : %,.3fms\n\tInitialisation : %,.3fms\n\tInitial propagation : %,.3fms" +
                "\n\tResolution : %,.3fs (%,.6fms)\n\tNodes: %,d\n\tBacktracks: %,d\n\tFails: %,d\n\t" +
                "Restarts: %,d\n\tPropagations: %,d + %,d\n\tMemory: %,dmb\n\tVariables: %,d\n\tConstraints: %,d\n\tRecords: %,d",
                readingTimeCount / IN_MS,
                initialisationTimeCount / IN_MS,
                initialPropagationTimeCount / IN_MS,
                timeCount / IN_SEC,
                timeCount / IN_MS,
                nodeCount,
                backtrackCount,
                failCount,
                restartCount,
                eventCount,
                propagationCount,
                usedMemory,
                solver.getVars().length,
                solver.getCstrs().length,
//                solver.getEngine().getNbRequests()
                0
        ));

        return st.toString();
    }


}
