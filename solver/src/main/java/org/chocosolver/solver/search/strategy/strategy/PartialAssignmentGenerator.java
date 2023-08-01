/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2023, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.strategy.strategy;

import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.loop.monitors.IMonitorRestart;
import org.chocosolver.solver.search.loop.monitors.IMonitorSolution;
import org.chocosolver.solver.search.restart.AbstractRestart;
import org.chocosolver.solver.search.strategy.assignments.DecisionOperatorFactory;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;

import java.util.*;

/**
 * This is the source code of the GPA part of the paper "Finding Good
 * Partial Assignments During Restart-based Branch and Bound Search, AAAI'23".
 *
 * @author Hongbo Li
 * @since 08/29/2022
 */

public class PartialAssignmentGenerator<V extends Variable> extends AbstractStrategy<V> implements IMonitorSolution, IMonitorRestart {

    /**
     * The main strategy declared in the solver
     */
    private final AbstractStrategy<V> mainStrategy;

    public final int[] lastSolution;
    public int lastObjective;
    public IntVar objectiveVariable;
    public final int varNum;
    public IntVar[] vars;
    public Solver solver;

    ////////////The two linked list implement the solQueue
    public LinkedList<Integer> objectiveValues;
    public LinkedList<int[]> solutionValues;
    private final boolean hackCutoff;
    public final int maxSolNum;
    public int recordSolNum;
    public final boolean isMaximize;
    public HashMap<String, Assignment> assignmentMap;
    public int lubyCutoffTimes;
    public boolean newSolutionFound;
    public int restartNum;
    public LinkedList<Assignment> paList;
    public LinkedList<Assignment> subtree;

    public PartialAssignmentGenerator(IntVar[] vs,
                                      int maxSN,
                                      boolean hackCutoff,
                                      AbstractStrategy<V> mainStrategy) {
        //noinspection unchecked
        super((V[]) vs);
        this.varNum = vs.length;
        this.vars = vs;
        this.solver = vs[0].getModel().getSolver();
        this.objectiveVariable = vs[0].getModel().getObjective().asIntVar();
        this.maxSolNum = maxSN;
        this.hackCutoff = hackCutoff;
        this.mainStrategy = mainStrategy;

        this.lastSolution = new int[varNum];
        this.solutionValues = new LinkedList<>();
        this.objectiveValues = new LinkedList<>();
        this.newSolutionFound = false;
        this.isMaximize = solver.getModel().getResolutionPolicy() == ResolutionPolicy.MAXIMIZE;
        this.assignmentMap = new HashMap<>();
        this.subtree = new LinkedList<>();
        this.lubyCutoffTimes = 1;
        this.restartNum = 1;
        this.recordSolNum = 0;
        this.paList = new LinkedList<>();
    }

    @Override
    public boolean init() {
        if (solver.getRestarter() == AbstractRestart.NO_RESTART) {
            throw new UnsupportedOperationException("Partial Assignment Generator requires a restart strategy. " +
                    "Please set a restart strategy. ");
        } else {
            solver.getRestarter().setGrower(() -> this.lubyCutoffTimes);
        }
        if (!solver.getSearchMonitors().contains(this)) {
            solver.plugMonitor(this);
        }
        return mainStrategy.init();
    }

    @Override
    public void remove() {
        this.mainStrategy.remove();
        if (solver.getSearchMonitors().contains(this)) {
            solver.unplugMonitor(this);
        }
    }

    @Override
    public Decision<V> getDecision() {
        while (!subtree.isEmpty()) {
            Assignment as = subtree.removeFirst();
            int currentVar = as.varID;
            int currentVal = as.value;
            IntVar best = vars[currentVar];
            if (best.isInstantiated()) {
                continue;
            }
            if (best.contains(currentVal)) {
                //noinspection unchecked
                return (Decision<V>) solver.getDecisionPath().makeIntDecision(
                        best, DecisionOperatorFactory.makeIntEq(), currentVal);
            }
        }
        return mainStrategy.getDecision();
    }

    @Override
    public void onSolution() {
        newSolutionFound = true;
        lastObjective = objectiveVariable.getValue();
        for (int i = 0; i < varNum; i++) {
            lastSolution[i] = vars[i].getValue();
        }
    }

    public void generatePartialAssignment() {
        // Legacy code. The refined generation performs better.
        // generation();
        refinedGeneration();
    }

    /**
     * Legacy code. The refined generation performs better.
     * This code is kept for comparison purpose.
     */
    @SuppressWarnings("unused")
    private void generation() {
        paList.clear();
        int[][] sols = new int[0][];
        sols = solutionValues.toArray(sols);
        double size = sols.length;
        for (int i = 0; i < size - 1; i++) {
            int[] sol1 = sols[i];
            int[] sol2 = sols[i + 1];
            ArrayList<Integer> list = compare(sol2, sol1);
            for (int k = 0; k < list.size(); k += 2) {
                int vid = list.get(k);
                int v = list.get(k + 1);
                Assignment as = new Assignment(vars[vid], vid, v);
                paList.add(as);
            }
        }
    }

    private void refinedGeneration() {
        paList.clear();
        assignmentMap.clear();
        int[] objs = new int[objectiveValues.size()];
        int index = 0;
        for (java.util.Iterator<Integer> ite = objectiveValues.iterator(); ite.hasNext(); ) {
            objs[index++] = ite.next();
        }
        double decayWeight;
        int[][] sols = new int[0][];
        sols = solutionValues.toArray(sols);
        int range = sols.length / 2;

        for (int i = 0; i < range; i++) {
            int[] sol1 = sols[i];
            decayWeight = ((range - i) * 1. / range);
            for (int j = 0; j < range; j++) {
                int[] sol2 = sols[j + i + 1];
                ArrayList<Integer> list = compare(sol2, sol1);
                double score = objs[i] - objs[j];
                if (!isMaximize) {
                    score = 0 - score;
                }
                score *= decayWeight;
                double upNum = list.size() / 2.;
                score /= upNum;
                for (int k = 0; k < list.size(); k += 2) {
                    int vid = list.get(k);
                    int v = list.get(k + 1);
                    String name = Assignment.getName(vid, v);
                    if (assignmentMap.containsKey(name)) {
                        Assignment as = assignmentMap.get(name);
                        as.increScore(score);
                    } else {
                        Assignment as = new Assignment(vars[vid], vid, v);
                        as.increScore(score);
                        assignmentMap.put(name, as);
                    }
                }
            }
        }
        paList.addAll(assignmentMap.values());
        Assignment[] asArray = new Assignment[paList.size()];
        asArray = paList.toArray(asArray);
        Arrays.sort(asArray, Comparator.comparingDouble(Assignment::getAveScore));
        paList.clear();
        for (int i = asArray.length - 1; i >= 0; i--) {
            paList.addLast(asArray[i]);
        }
    }

    public void beforeRestart() {
        lubyCutoffTimes = 1;
        restartNum++;
        if (!paList.isEmpty()) {
            subtree.clear();
            subtree.addAll(paList);
            int n = paList.size() / 2;
            if (n == 0) {
                n = 1;
            }
            while (n > 0 && !paList.isEmpty()) {
                paList.removeLast();
                n--;
            }
        }
        if (newSolutionFound) {
            newSolutionFound = false;
            if (solutionValues.size() == maxSolNum) {
                solutionValues.removeLast();
                objectiveValues.removeLast();
            }
            solutionValues.addFirst(lastSolution.clone());
            recordSolNum++;
            objectiveValues.addFirst(lastObjective);
            if (objectiveValues.size() > 1) {
                generatePartialAssignment();
                subtree.clear();
                subtree.addAll(paList);
                lubyCutoffTimes = hackCutoff ? restartNum : 1;
            }
            restartNum = 1;
        }

    }

    public ArrayList<Integer> compare(int[] worseSol, int[] betterSol) {
        ArrayList<Integer> results = new ArrayList<Integer>();
        for (int i = 0; i < varNum; i++) {
            int v1 = worseSol[i];
            int v2 = betterSol[i];
            if (v1 != v2) {
                results.add(i);
                results.add(v2);
            }
        }
        return results;
    }

    public static class Assignment {
        public String name;
        public IntVar var;
        public int value;
        public double score;
        public int varID;
        public double incTimes;

        public Assignment(IntVar vr, int vid, int v) {
            varID = vid;
            var = vr;
            value = v;
            name = getName(vid, v);
            score = 0;
            incTimes = 0;
        }

        public static String getName(int vid, int v) {
            return "X[" + vid + "]=" + v;
        }

        public void increScore(double d) {
            score += d;
            incTimes++;
        }

        public double getAveScore() {
            return score / incTimes;
        }

        public void print() {
            System.out.print("(" + name + ", " + score + ") || ");
        }

    }

}


