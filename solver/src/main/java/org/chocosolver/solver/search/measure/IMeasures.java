/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.measure;


import org.chocosolver.solver.objective.IBoundsManager;
import org.chocosolver.solver.search.SearchState;

import java.io.Serializable;
import java.text.DecimalFormat;

/**
 * Interface for providing resolution statistics
 *
 * @author Charles Prud'Homme, Jean-Guillaume Fages, Arnaud Malapert
 */
public interface IMeasures extends Serializable {

    /**
     * To transform time from nanoseconds to seconds
     */
    float IN_SEC = 1000 * 1000 * 1000f;

    /**
     * @return name of the model/solver observed
     */
    String getModelName();

    /**
     * @return the current world unique id
     */
    long getTimestamp();

    /**
     * @return the time count (in seconds), including initial propagation time count
     */
    default float getTimeCount() {
        return getTimeCountInNanoSeconds() / IN_SEC;
    }

    /**
     * @return the time count (in nano seconds), including initial propagation time count
     */
    long getTimeCountInNanoSeconds();

    /**
     * @return the time count (in seconds), including initial propagation time count
     */
    default float getTimeToBestSolution() {
        return getTimeToBestSolutionInNanoSeconds() / IN_SEC;
    }

    /**
     * @return the time count (in nano seconds), including initial propagation time count
     */
    long getTimeToBestSolutionInNanoSeconds();

    /**
     * @return the reading time count (in sec)
     */
    default float getReadingTimeCount() {
        return getReadingTimeCountInNanoSeconds() / IN_SEC;
    }

    /**
     * @return the reading time count (in nano seconds).
     */
    long getReadingTimeCountInNanoSeconds();

    /**
     * @return the node count
     */
    long getNodeCount();

    /**
     * @return the backtrack count
     */
    long getBackTrackCount();

    /**
     * @return the fail count
     */
    long getFailCount();

    /**
     * @return the fixpoint count
     */
    long getFixpointCount();

    /**
     * @return the non chronological backtracks count
     */
    long getBackjumpCount();

    /**
     * @return the restart count
     */
    long getRestartCount();

    /**
     * @return the solution count of the measure
     */
    long getSolutionCount();

    /**
     * @return the decision count
     */
    long getDecisionCount();

    /**
     * @return the maximum depth of the search tree
     */
    long getMaxDepth();

    /**
     * @return the current depth in the search tree
     */
    long getCurrentDepth();

    /**
     * @return true iff the problem has an objective variable (i.e. optimization problem)
     */
    boolean hasObjective();

    /**
     * @return true iff the optimum has been found and proved
     */
    boolean isObjectiveOptimal();

    /**
     * @return the objective value of the best solution found (can be Integer or Double)
     */
    Number getBestSolutionValue();

    /**
     * @return the search state
     */
    SearchState getSearchState();

    /**
     * @return current bound manager
     */
    IBoundsManager getBoundsManager();

    /**
     * @return a summary of recorded statistics
     */
    default String toOneLineString() {
        StringBuilder st = new StringBuilder(256);
        st.append("Model[").append(getModelName()).append("], ");
        st.append(String.format("%d Solutions, ", getSolutionCount()));
        if (hasObjective()) {
            st.append(getBoundsManager()).append(", ");
        }
        st.append(
            String.format(
                "Resolution time %.3fs, "
                    + (hasObjective() ? String.format("Time to best solution %.3fs, ", getTimeToBestSolution()) : "")
                    + "%d Nodes (%,.1f n/s), %d Backtracks, %d Backjumps, %d Fails, %d Restarts",
                getTimeCount(),
                getNodeCount(),
                getNodeCount() / getTimeCount(),
                getBackTrackCount(),
                getBackjumpCount(),
                getFailCount(),
                getRestartCount()
            )
        );
        return st.toString();
    }

    default String toDimacsString() {
        final StringBuilder st = new StringBuilder(256);
        st.append("i ").append(getModelName()).append("\n");
        st.append("s ").append(getSearchState()).append("\n");
        if (hasObjective()) {
            final DecimalFormat df = new DecimalFormat("#.###");
            st.append("o ").append(df.format(getBoundsManager().getBestSolutionValue())).append("\n");
        }
        st.append(String.format("d NBSOLS %d\nd TIME %.3f\nd NODES %d\nd BACKTRACKS %d\nd FAILURES %d\nd RESTARTS %d",
                getSolutionCount(), getTimeCount(), getNodeCount(), getBackTrackCount(), getFailCount(), getRestartCount()));
        return st.toString();
    }


    default String toMultiLineString() {
        StringBuilder st = new StringBuilder(256);
        //        st.append("- Search statistics\n");
        final long solutionCount = getSolutionCount();
        switch (getSearchState()) {
            case NEW:
                st.append("- Search not started- ");
                break;
            case RUNNING:
                st.append("- Running search - ");
                break;
            case TERMINATED:
                st.append("- Complete search - ");
                if (solutionCount == 0) {
                    st.append("No solution.");
                } else if (solutionCount == 1) {
                    st.append("1 solution found.");
                } else {
                    st.append(String.format("%,d solution(s) found.", solutionCount));
                }
                st.append('\n');
                break;
            case STOPPED:
                st.append("- Incomplete search - Limit reached.\n");
                break;
            case KILLED:
                st.append("- Incomplete search - Unexpected interruption.\n");
                break;
            default:
                throw new IllegalArgumentException("Illegal search state " + getSearchState());
        }
        st.append("\tModel[").append(getModelName()).append("]\n");
        st.append(String.format("\tSolutions: %,d\n", solutionCount));
        if (hasObjective()) {
            st.append("\t").append(getBoundsManager()).append(",\n");
        }
        st.append(
            String.format(
                "\tBuilding time : %,.3fs\n" +
                    "\tResolution time : %,.3fs\n" +
                    (hasObjective() ? String.format("\tTime to best solution : %,.3fs\n",getTimeToBestSolution()) : "") +
                    "\tNodes: %,d (%,.1f n/s) \n" +
                    "\tBacktracks: %,d\n" +
                    "\tBackjumps: %,d\n" +
                    "\tFails: %,d\n" +
                    "\tRestarts: %,d",
                getReadingTimeCount(),
                getTimeCount(),
                getNodeCount(),
                getNodeCount() / getTimeCount(),
                getBackTrackCount(),
                getBackjumpCount(),
                getFailCount(),
                getRestartCount()
            )
        );
        return st.toString();
    }

    /**
     * @return statistic values only
     */
    default Number[] toArray() {
        return new Number[]{
                getSearchState().ordinal(),
                getSolutionCount(),
                getReadingTimeCount(),
                getTimeCount(),
                getBoundsManager().getPolicy().ordinal(),
                hasObjective() ? getBestSolutionValue() : 0,
                getNodeCount(),
                getBackTrackCount(),
                getFailCount(),
                getRestartCount()
        };
    }

    /**
     * @return statistics in a CSV format
     */
    default String toCSV() {
        // solutionCount;buildingTime(sec);totalTime(sec);timeToBest(sec);objective;nodes;backtracks;fails;restarts;
        return String.format("%c;%d;%.3f;%.3f;%.3f;%d;%d;%d;%d;%d;%d;",
                getSearchState().toString().charAt(0),
                getSolutionCount(),
                getReadingTimeCount(),
                getTimeCount(),
                getTimeToBestSolution(),
                hasObjective() ? getBestSolutionValue().intValue() : 0,
                getNodeCount(),
                getBackTrackCount(),
                getBackjumpCount(),
                getFailCount(),
                getRestartCount()
        );
    }

}
