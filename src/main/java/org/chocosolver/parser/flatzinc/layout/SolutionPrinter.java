/*
 * Copyright (c) 1999-2015, Ecole des Mines de Nantes
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
package org.chocosolver.parser.flatzinc.layout;

import org.chocosolver.solver.Solver;

/**
 * Created by cprudhom on 18/06/15.
 * Project: choco-parsers.
 */
public class SolutionPrinter extends ASolutionPrinter {

    Solver solver;

    public SolutionPrinter(Solver solver, boolean printAll, boolean printStat) {
        super(solver.getSolutionRecorder(), printAll, printStat);
        this.solver = solver;
        solver.plugMonitor(this);
    }

    @Override
    public void onSolution() {
        super.onSolution();
        if(printStat) {
            System.out.printf("%% %s \n", solver.getMeasures().toOneShortLineString());
        }
    }

    public void doFinalOutPut() {
        userinterruption = false;
        boolean complete = !solver.getSearchLoop().hasReachedLimit() && !solver.getSearchLoop().hasEndedUnexpectedly();
        if (nbSolution == 0) {
            if (complete) {
                System.out.printf("=====UNSATISFIABLE=====\n");
            } else {
                System.out.printf("=====UNKNOWN=====\n");
            }
        } else { // at least one solution
            if (!printAll) { // print the first/best solution when -a is not enabled
                printSolution(solrecorder.getLastSolution());
            }
            if (complete) {
                System.out.printf("==========\n");
            } else {
                if ((solver.getObjectiveManager().isOptimization())) {
                    System.out.printf("=====UNBOUNDED=====\n");
                }
            }
        }
        if(printStat){
            System.out.printf("%% %s \n", solver.getMeasures().toOneShortLineString());
        }
    }
}
