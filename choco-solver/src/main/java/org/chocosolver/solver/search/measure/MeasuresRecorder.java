/**
 * Copyright (c) 2014,
 *       Charles Prud'homme (TASC, INRIA Rennes, LINA CNRS UMR 6241),
 *       Jean-Guillaume Fages (COSLING S.A.S.).
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.solver.search.measure;


import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.loop.monitors.*;

/**
 * Object which stores resolution information to get statistics
 *
 * @author Charles Prud'Homme
 */
public final class MeasuresRecorder implements IMeasures, IMonitorClose, IMonitorContradiction, IMonitorDownBranch,
        IMonitorInitialize, IMonitorInitPropagation, IMonitorOpenNode, IMonitorRestart,
        IMonitorSolution, IMonitorUpBranch {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private static final float IN_SEC = 1000 * 1000 * 1000f;

    public boolean hasObjective;
    public boolean objectiveOptimal;
    public long solutionCount;
    public long timeCount;
    public long readingTimeCount;
    public long initialisationTimeCount;
    public long initialPropagationTimeCount;
    public long nodeCount;
    public long backtrackCount;
    public long failCount;
    public long restartCount;
    public long maxDepth, depth;
    public long propagationCount, eventCount;
    public long usedMemory;

    protected long startingTime, startingMemory;

    protected Solver solver;
    protected Constraint[] cstrs;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public MeasuresRecorder(Solver solver) {
        super();
        this.solver = solver;
    }

    @Override
    public MeasuresRecorder duplicate() {
        MeasuresRecorder mr = new MeasuresRecorder(solver);
        mr.hasObjective = hasObjective;
        mr.objectiveOptimal = objectiveOptimal;
        mr.solutionCount = solutionCount;
        mr.timeCount = timeCount;
        mr.readingTimeCount = readingTimeCount;
        mr.initialisationTimeCount = initialisationTimeCount;
        mr.initialPropagationTimeCount = initialPropagationTimeCount;
        mr.nodeCount = nodeCount;
        mr.backtrackCount = backtrackCount;
        mr.failCount = failCount;
        mr.restartCount = restartCount;
        mr.maxDepth = maxDepth;
        mr.depth = depth;
        mr.propagationCount = propagationCount;
        mr.eventCount = eventCount;
        mr.usedMemory = usedMemory;
        mr.startingTime = startingTime;
        mr.startingMemory = startingMemory;
        mr.cstrs = cstrs.clone();
        return mr;
    }

    //****************************************************************************************************************//
    //**************************************** SETTERS ***************************************************************//
    //****************************************************************************************************************//

    @Override
    public long getSolutionCount() {
        return solutionCount;
    }

    @Override
    public void declareObjective() {
        hasObjective = true;
    }

    @Override
    public void setObjectiveOptimal(boolean objectiveOptimal) {
        this.objectiveOptimal = objectiveOptimal;
    }

    @Override
    public final void reset() {
        timeCount = 0;
        nodeCount = 0;
        backtrackCount = 0;
        restartCount = 0;
        failCount = 0;
        solutionCount = 0;
        hasObjective = false;
        readingTimeCount = 0;
        initialisationTimeCount = 0;
        initialPropagationTimeCount = 0;
        propagationCount = 0;
        eventCount = 0;
        maxDepth = 0;
        cstrs = null;
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
        return timeCount / IN_SEC;
    }

    @Override
    public float getReadingTimeCount() {
        return readingTimeCount / IN_SEC;
    }

    @Override
    public void setReadingTimeCount(long time) {
        this.readingTimeCount = time;
    }

    @Override
    public float getInitialisationTimeCount() {
        return initialisationTimeCount / IN_SEC;
    }

    @Override
    public float getInitialPropagationTimeCount() {
        return initialPropagationTimeCount / IN_SEC;
    }

    @Override
    public long getRestartCount() {
        return restartCount;
    }

    @Override
    public long getMaxDepth() {
        return maxDepth;
    }

    @Override
    public long getCurrentDepth() {
        return depth;
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
    public Number getBestSolutionValue() {
        return solver.getObjectiveManager().getBestSolutionValue();
    }

    @Override
    public long getUsedMemory() {
        return usedMemory;
    }

    static long memoryUsedInMB() {
        return Runtime.getRuntime().freeMemory() / 1024 / 1024;
    }

    @Override
    public void updateMemoryUsed() {
        usedMemory = memoryUsedInMB() - startingMemory;
    }

    @Override
    public void updatePropagationCount() {
        if (cstrs == null || cstrs.length != solver.getNbCstrs()) {
            cstrs = solver.getCstrs();
        }
        propagationCount = 0;
        eventCount = 0;
        for (int i = 0; i < cstrs.length; i++) {
            Propagator[] propagators = cstrs[i].getPropagators();
            for (int j = 0; j < propagators.length; j++) {
                propagationCount += propagators[j].coarseERcalls;
                eventCount += propagators[j].fineERcalls;
            }
        }


    }

    @Override
    public void updateTimeCount() {
//        timeCount = TimeCacheThread.currentTimeMillis - startingTime;
        timeCount = System.nanoTime() - startingTime;
    }

    @Override
    public long timestamp() {
        return nodeCount + backtrackCount;
    }

    //****************************************************************************************************************//
    //**************************************** INCREMENTERS **********************************************************//
    //****************************************************************************************************************//

    @Override
    public void beforeInitialize() {
        startingMemory = memoryUsedInMB();
        startingTime = System.nanoTime();
    }

    @Override
    public void afterInitialize() {
        initialisationTimeCount = System.nanoTime() - startingTime;
    }

    @Override
    public void beforeInitialPropagation() {
    }

    @Override
    public void afterInitialPropagation() {
        initialPropagationTimeCount = System.nanoTime() - startingTime;
    }

    @Override
    public void beforeOpenNode() {
//        updateTimeCount();
        nodeCount++;
        if (depth > maxDepth) {
            maxDepth = depth;
        }
    }

    @Override
    public void afterOpenNode() {
    }

    @Override
    public void onSolution() {
        solutionCount++;
        updateTimeCount();
//        updatePropagationCount();
    }

    @Override
    public void beforeDownLeftBranch() {
        depth++;
    }

    @Override
    public void afterDownLeftBranch() {
    }

    @Override
    public void beforeDownRightBranch() {
        depth++;
    }

    @Override
    public void afterDownRightBranch() {
    }

    @Override
    public void beforeUpBranch() {
        backtrackCount++;
        depth--;
    }

    @Override
    public void afterUpBranch() {
    }

    @Override
    public void onContradiction(ContradictionException cex) {
        failCount++;
    }

    @Override
    public void beforeRestart() {
    }

    @Override
    public void afterRestart() {
        restartCount++;
        depth = 0;
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
    public Number[] toArray() {
        return new Number[]{
                getSolutionCount(),
                getReadingTimeCount(),
                getInitialisationTimeCount(),
                getInitialPropagationTimeCount(),
                getTimeCount(),
                hasObjective() ? getBestSolutionValue() : 0,
                getNodeCount(),
                getBackTrackCount(),
                getFailCount(),
                getRestartCount(),
                getEventsCount(),
                getPropagationsCount(),

        };
    }

    @Override
    public String toOneLineString() {
        beforeClose();
        StringBuilder st = new StringBuilder(256);
        st.append(String.format("%d Solutions, ", solutionCount));
        if (hasObjective()) {
            st.append(solver.getObjectiveManager()).append(", ");
        }
        st.append(String.format("Building time : %,.3fs, Initialisation : %,.3fs, Initial propagation : %,.3fs, " +
                "Total %,.3fs, %d Nodes (%,.1f n/s), %,d Backtracks, %,d Fails, %,d Restarts, %,d + %,d Propagations",
                getReadingTimeCount(),
                getInitialisationTimeCount(),
                getInitialPropagationTimeCount(),
                getTimeCount(),
                getNodeCount(),
                getNodeCount() / getTimeCount(),
                getBackTrackCount(),
                getFailCount(),
                getRestartCount(),
                getEventsCount(),
                getPropagationsCount()));
        return st.toString();
    }

    @Override
    public String toOneShortLineString() {
        beforeClose();
        StringBuilder st = new StringBuilder(256);
        st.append(String.format("%d Solutions, ", solutionCount));
        if (hasObjective()) {
            st.append(solver.getObjectiveManager()).append(", ");
        }
        st.append(String.format("Resolution %.3fs, %d Nodes (%,.1f n/s), %d Backtracks, %d Fails, %d Restarts",
                getTimeCount(),
                getNodeCount(),
                getNodeCount() / getTimeCount(),
                getBackTrackCount(),
                getFailCount(),
                getRestartCount()));
        return st.toString();
    }

    @Override
    public String toString() {
        beforeClose();
        StringBuilder st = new StringBuilder(256);
//        st.append("- Search statistics\n");

        if(solver.hasReachedLimit()){
            st.append("- Incomplete search - Limit reached.\n");
        }else if(solver.getSearchLoop().hasEndedUnexpectedly()) {
            st.append("- Incomplete search - Unexpected interruption.\n");
        }else{
            st.append("- Complete search - ");
            if(solutionCount == 0) {
                st.append("No solution.");
            } else if(solutionCount == 1){
                st.append("1 solution found.");
            }else{
                st.append(String.format("%,d solution(s) found.", solutionCount));
            }
            st.append('\n');
        }
        st.append(String.format("\tSolutions: %,d\n", solutionCount));
        if (hasObjective()) {
            st.append("\t").append(solver.getObjectiveManager()).append(",\n");
        }
        st.append(String.format("\tBuilding time : %,.3fs\n\tInitialisation : %,.3fs\n\tInitial propagation : %,.3fs" +
                "\n\tResolution : %,.3fs\n\tNodes: %,d (%,.1f n/s) \n\tBacktracks: %,d\n\tFails: %,d\n\t" +
                "Restarts: %,d\n\tMax depth: %,d\n\tPropagations: %,d + %,d\n\tMemory: %,dmb\n\tVariables: %,d\n\tConstraints: %,d",
                getReadingTimeCount(),
                getInitialisationTimeCount(),
                getInitialPropagationTimeCount(),
                getTimeCount(),
                getNodeCount(),
                getNodeCount() / getTimeCount(),
                getBackTrackCount(),
                getFailCount(),
                getRestartCount(),
                getMaxDepth(),
                getEventsCount(),
                getPropagationsCount(),
                getUsedMemory(),
                solver.getNbVars(),
                solver.getNbCstrs()
        ));
        return st.toString();
    }

    @Override
    public String toCSV() {
        beforeClose();
        // solutionCount;buildingTime(sec);initTime(sec);initPropag(sec);totalTime(sec);objective;nodes;backtracks;fails;restarts;fineProp;coarseProp;
        return String.format("%d;%.3f;%.3f;%.3f;%.3f;%e;%d;%d;%d;%d;%d;%d;",
                getSolutionCount(),
                getReadingTimeCount(),
                getInitialisationTimeCount(),
                getInitialPropagationTimeCount(),
                getTimeCount(),
                hasObjective() ? getBestSolutionValue().doubleValue() : 0,
                getNodeCount(),
                getBackTrackCount(),
                getFailCount(),
                getRestartCount(),
                getEventsCount(),
                getPropagationsCount());
    }
}
