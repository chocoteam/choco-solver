/**
 * Copyright (c) 2015, Ecole des Mines de Nantes
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    This product includes software developed by the <organization>.
 * 4. Neither the name of the <organization> nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
/*
 * Created by IntelliJ IDEA.
 * User: sofdem - sophie.demassey{at}emn.fr
 * Date: Jul 27, 2010 - 1:53:16 PM
 */

package org.chocosolver.samples.todo.problems.nsp;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;

import java.util.HashMap;
import java.util.Map;

/**
 * Basis of a Choco CPModel for the Nurse Scheduling Problem.
 * This class defined the model variables and the common utility methods (print, parse, etc.)
 * Constraints should be defined in a derived class
 *
 * @author Sophie Demassey
 */
public class NurseSchedulingProblem {

    /**
     * the instance data
     */
    protected NSData data;

    /**
     * the table [nbEmployees][nbDays] of assignment variables
     */
    protected IntVar[][] shifts;

    /**
     * the table [nbEmployees][nbActivities] of activity counter variables
     */
    protected IntVar[][] occurrences;

    /**
     * the table [nbActivities][nbDays] of activity cover variables
     */
    protected IntVar[][] covers;

    /**
     * a short description (the list of constraints) of the built model configuration
     */
    protected String description;

    /**
     * the configuration of the model to build defined as a map ConstraintType -> ChocoImplementation
     */
    protected Map<String, String> options;

    /**
     * build a Choco CP Model without constraints
     *
     * @param data the instance data
     */
    public NurseSchedulingProblem(NSData data, Model model) {
        this(data, "", model);
    }

    /**
     * build a Choco CP Model under some constraint configuration
     *
     * @param data    the instance data
     * @param options the model configuration
     * @param model the solver
     */
    public NurseSchedulingProblem(NSData data, String options, Model model) {
        super();
        this.data = data;
        this.makeVariables(model);
        this.description = "NSCPModel: ";
        this.parseOptions(options);
    }

    private void makeVariables(Model model) {
        this.shifts = model.intVarMatrix("S", data.nbEmployees(), data.nbDays(), 0, data.nbActivities() - 1, false);

        this.occurrences = new IntVar[data.nbEmployees()][data.nbActivities()];
        for (int e = 0; e < data.nbEmployees(); e++) {
            for (int a = 0; a < occurrences[e].length; a++) {
                occurrences[e][a] = model.intVar("n" + data.getLiteral(a) + e, data.getCounterLB(e, a), data.getCounterUB(e, a), true);
            }
        }

        this.covers = new IntVar[data.nbActivities()][];
        for (int a = 0; a < covers.length; a++) {
            this.covers[a] = model.intVarArray("B", data.nbDays(), data.getCoverLB(a), data.getCoverUB(a), true);
        }
    }

    /**
     * print the current solver solution if exists
     *
     * @param s the CPModel
     */
    public String solutionToString(Model s) {
        if (ESat.TRUE != s.getSolver().isFeasible()) {
            return null;
        }
        StringBuilder buf = new StringBuilder(100);

        for (int e = 0; e < shifts.length; e++) {
            for (IntVar v : shifts[e]) {
                buf.append(data.getLiteral(v.getValue())).append(" ");
            }
            buf.append("% ");
            for (IntVar v : occurrences[e]) {
                buf.append((v == null) ? ". " : v.getValue() + " ");

            }
            buf.append("\n");
        }
        return buf.toString();
    }

    /**
     * print the current solver solution if exists
     *
     * @param s the CPModel
     */
    public void printSolution(Model s) {
        if (ESat.TRUE != s.getSolver().isFeasible()) {
            System.out.println("No solution found.");
            return;
        }
        for (int e = 0; e < shifts.length; e++) {
            for (IntVar v : shifts[e]) {
                System.out.print(data.getLiteral(v.getValue()) + " ");
            }
            System.out.print("% ");
            for (IntVar v : occurrences[e]) {
                System.out.print((v == null) ? ". " : v.getValue() + " ");

            }
            System.out.println("");
        }
    }

    /**
     * get the current solver solution as the assignment table if exists
     *
     * @param s the CPModel
     * @return the current solver solution as the assignment table [nbEmployees][nbDays] if exists, null otherwise
     */
    public int[][] getSolution(Model s) {
        if (ESat.TRUE != s.getSolver().isFeasible()) {
            return null;
        }
        int[][] solution = new int[shifts.length][shifts[0].length];
        for (int e = 0; e < shifts.length; e++) {
            for (int t = 0; t < shifts[e].length; t++) {
                solution[e][t] = shifts[e][t].getValue();
            }
        }
        return solution;
    }

    /**
     * get a description of the model configuration
     *
     * @return the description (list of constraints)
     */
    public String getDescription() {
        return description + ".";
    }

    /**
     * check whether a constraint of a given type should be built according to the required model configuration
     *
     * @param ctrDesc the constraint type
     * @return true iff the constraint has to be built
     */
    protected boolean isSetConstraint(String ctrDesc) {
        return options.containsKey(ctrDesc);
    }

    /**
     * get the wanted Choco implementation of a constraint type according to the required model configuration
     *
     * @param ctrDesc the constraint type
     * @return the wanted Choco implementation
     */
    protected String getConstraintOption(String ctrDesc) {
        return options.get(ctrDesc);
    }

    private void parseOptions(String options) {
        this.options = new HashMap<>();
        if (options == null) return;
        String[] opts = options.trim().split(" ");
        for (String opt : opts) {
            String[] x = opt.split("[\\[\\]]");
            String o = (x.length == 1) ? "" : x[1];
            this.options.put(x[0], o);
        }
    }

    public NSData getData() {
        return data;
    }

    public IntVar[][] getShifts() {
        return shifts;
    }

    public IntVar[][] getOccurrences() {
        return occurrences;
    }

    public IntVar[][] getCovers() {
        return covers;
    }
}
