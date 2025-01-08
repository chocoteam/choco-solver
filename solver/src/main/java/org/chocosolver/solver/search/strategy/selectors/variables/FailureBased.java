/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2025, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.strategy.selectors.variables;

import gnu.trove.list.array.TIntArrayList;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.loop.monitors.IMonitorContradiction;
import org.chocosolver.solver.variables.Variable;

import java.util.Random;


/**
 * This is the source code of the failure based variable ordering heuristics proposed
 * in paper "Failure Based Variable Ordering Heuristics for Solving CSPs" of  CP2021.
 *
 * @author Hongbo Li
 * @author Charles Prud'homme
 */


public class FailureBased<V extends Variable> implements IMonitorContradiction, VariableSelector<V> {

    private double currenFixNum;
    private final int varNum;
    private final Random ran;
    private final Solver solver;
    private final TIntArrayList bests = new TIntArrayList();
    private int currentVarIndex = -1;

    /**
     * 1. FRB:  (failures[x]/assignTimes[x])/dom[x]
     * 2. FRBA: {failures[x]/assignTimes[x]+[1/(totalFailCount-lastFailure[x]+1)}/dom[x]
     * 3. FLB:  AFL[x]/dom[x]
     * 4. FLBA: {AFL[x]*[1/(totalFailCount-lastFailure[x]+1)]}/dom[x]
     */
    private final int scoreType;
    private final double[] failures;// record the number of failures caused by each variable.
    private final double[] AFL;// accumulated failure length of each variable.
    private final double[] assignTimes;// record the number of assigned times of each variable.
    private final double[] lastFailure;// record the timestamp of failure count of
    // the last failure caused by each variable

    /**
     * @param vars:  the decision variables.
     * @param seed:  a random seed.
     * @param sType: the score type. 1->FRB; 2->FRBA; 3->FLB; 4->FLBA.
     */
    public FailureBased(V[] vars, long seed, int sType) {
        ran = new Random(seed);
        solver = vars[0].getModel().getSolver();
        solver.plugMonitor(this);
        varNum = vars.length;
        if (sType > 4 || sType < 1) {
            if (solver.getModel().getSettings().warnUser()) {
                solver.log().white().println("Unknown score type of failure based search!!");
                solver.log().white().println("Please specify the score type between 1 - 4 !!");
                solver.log().white().println("1 -> FRB");
                solver.log().white().println("2 -> FRBA");
                solver.log().white().println("3 -> FLB");
                solver.log().white().println("4 -> FLBA");
                solver.log().white().println("Using the default score type 2 !!");
            }
            scoreType = 2;
        } else {
            scoreType = sType;
        }
        failures = new double[varNum];
        lastFailure = new double[varNum];
        AFL = new double[varNum];
        assignTimes = new double[varNum];
        if (scoreType == 1 || scoreType == 2) {
            for (int i = 0; i < varNum; i++) {
                failures[i] = 0.5;
                assignTimes[i] = 1;
            }
        }
    }

    @Override
    public V getVariable(V[] vars) {
        currenFixNum = 0;
        V best = null;
        bests.resetQuick();
        double w = Double.NEGATIVE_INFINITY;
        for (int idx = 0; idx < varNum; idx++) {
            int dsize = vars[idx].getDomainSize();
            if (dsize > 1) {
                double weight = weight(idx, dsize);
                if (w < weight) {
                    bests.resetQuick();
                    bests.add(idx);
                    w = weight;
                } else if (w == weight) {
                    bests.add(idx);
                }
            } else {
                currenFixNum++;
            }
        }
        if (bests.size() > 0) {
            currentVarIndex = bests.get(ran.nextInt(bests.size()));
            best = vars[currentVarIndex];
            assignTimes[currentVarIndex]++;
        }
        return best;
    }

    public void onContradiction(ContradictionException cex) {
        if (currentVarIndex != -1) {
            lastFailure[currentVarIndex] = solver.getFailCount();
            double fail_length = currenFixNum + 1;
            double flInc = 1 / (fail_length);
            AFL[currentVarIndex] += flInc;
            failures[currentVarIndex] += 1;
            currentVarIndex = -1;
        }
    }

    protected double weight(int id, int ds) {
        double finalScore = 0;
        switch (scoreType) {
            case 1:
                double fr = failures[id] / assignTimes[id];
                finalScore = fr / ds;
                break;
            case 2:
                double FR = failures[id] / assignTimes[id];
                double A = 1 / (solver.getFailCount() - lastFailure[id] + 1);
                finalScore = (FR + A) / ds;
                break;
            case 3:
                finalScore = AFL[id] / ds;
                break;
            case 4:
                double hs = 1 / (solver.getFailCount() - lastFailure[id] + 1);
                finalScore = (hs * AFL[id]) / ds;
                break;
        }

        return finalScore;
    }

    @Override
    public boolean init() {
        if (!solver.getSearchMonitors().contains(this)) {
            solver.plugMonitor(this);
        }
        return true;
    }

    @Override
    public void remove() {
        if (solver.getSearchMonitors().contains(this)) {
            solver.unplugMonitor(this);
        }
    }
}