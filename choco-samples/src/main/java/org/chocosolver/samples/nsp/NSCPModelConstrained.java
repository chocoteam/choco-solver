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
package org.chocosolver.samples.nsp;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.constraints.LogicalConstraintFactory;
import org.chocosolver.solver.constraints.SatFactory;
import org.chocosolver.solver.constraints.nary.automata.CostRegular;
import org.chocosolver.solver.constraints.nary.automata.FA.CostAutomaton;
import org.chocosolver.solver.constraints.nary.automata.FA.FiniteAutomaton;
import org.chocosolver.solver.constraints.nary.cnf.LogOp;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;
import org.chocosolver.util.tools.ArrayUtils;

/*
* Created by IntelliJ IDEA.
* User: sofdem - sophie.demassey{at}emn.fr
* Date: Jul 29, 2010 - 3:32:02 PM
*/
public class NSCPModelConstrained extends NurseSchedulingProblem {

    public CostRegular[][] cregs;

    public enum ConstraintOptions {
        BASIC("cover preAssign") {},
        REDUNDANT("cover preAssign symBreak coupling") {},
        RED_EQUITY("cover equity preAssign symBreak coupling") {},
        WITH_MCRW("pat[MCRegularWeek]") {},
        WITH_MCR("countW[occ] pat[MCRegular]") {},
        WITH_REG("countM countW[occ] pat[regular]") {},
        WITH_REIF("span countW[occ] countM pat[reif]") {};
        String option;

        ConstraintOptions(String opt) {
            option = opt;
        }

        public String getOptions() {
            return option;
        }

        public boolean isPatternOption() {
            return this.name().startsWith("WITH");
        }
    }

    public NSCPModelConstrained(Solver solver) {
        this(NSData.makeDefaultInstance(), solver);
    }

    public NSCPModelConstrained(NSData data, Solver solver) {
        this(data, ConstraintOptions.REDUNDANT, ConstraintOptions.WITH_REIF, solver);
    }

    public NSCPModelConstrained(NSData data, ConstraintOptions basisOptions, ConstraintOptions patternOptions, Solver solver) {
        this(data, basisOptions.getOptions(), patternOptions.getOptions(), solver);
    }

    public NSCPModelConstrained(NSData data, String options, String patternOptions, Solver solver) {
        super(data, options + " " + patternOptions, solver);
        this.makeConstraints(solver);
    }


    private void makeConstraints(Solver solver) {
        if (this.isSetConstraint("cover")) {
            this.makeCover(solver);
        }
        if (this.isSetConstraint("equity")) {
            this.makeEquity();
        }
        if (this.isSetConstraint("preAssign")) {
            this.makePreAssignments(solver);
        }
//        if (this.isSetConstraint("symBreak")) {
//            this.makeSymmetryBreaking(solver);
//        }
        if (this.isSetConstraint("coupling")) {
            this.makeCoverCounterCoupling(solver);
        }
        if (this.isSetConstraint("countM")) {
            this.makeMonthlyCounters(solver);
        }
        if (this.isSetConstraint("countW")) {
            this.makeWeeklyCounters(solver);
        }
//        if (this.isSetConstraint("span")) {
//            this.makeMaxWorkSpan(solver);
//        }
        if (this.isSetConstraint("pat")) {
            this.makeForbiddenPatterns(solver);
        }
    }

//**************************************************
//      MANDATORY/FORBIDDEN Assignments
//**************************************************

    private void makePreAssignments(Solver solver) {
        description += "preAssign ";
        for (int[] trip : data.preAssignments()) {
            boolean mandatory = trip[0] > 0;
            int e = trip[1];
            int t = trip[2];
            int a = trip[3];
            if (mandatory) {
                solver.post(IntConstraintFactory.arithm(shifts[e][t], "=", a));
            } else {
                solver.post(IntConstraintFactory.arithm(shifts[e][t], "!=", a));
            }
        }
    }

//**************************************************
//      SYMMETRY BREAKING Constraints
//**************************************************

    /*private void makeSymmetryBreaking(Solver solver) {
        description += "symBreak ";
        for (int[] group : data.symmetricEmployeeGroups()) {
            this.makeSymmetryBreaking(group);
        }
    }*/

    /*private void makeSymmetryBreaking(int[] group) {
        IntVar[][] vars = new IntVar[group.length][];
        for (int i = 0; i < vars.length; i++) {
            vars[i] = shifts[group[i]];
        }
        solver.post(ConstraintFactory.lexChainEq(vars));
    }*/

//**************************************************
//      EQUALITY Constraints
//**************************************************

    private void makeEquity() {
        description += "equity ";
        for (int[] group : data.equityEmployeeGroups()) {
            this.makeEquityDirect(group);
        }
    }

    private void makeEquityWithEq(Solver solver, int[] group) {
        IntVar[][] occ = new IntVar[group.length][];
        for (int i = 0; i < occ.length; i++) {
            occ[i] = occurrences[group[i]];
        }
        for (int i = 1; i < occ.length; i++) {
            for (int j = 0; j < i; j++) {
                for (int a = 0; a < occ[0].length; a++) {
                    solver.post(IntConstraintFactory.arithm(occ[i][a], "=", occ[j][a]));
                }
            }
        }
    }

    private void makeEquityDirect(int[] group) {
        for (int e = 1; e < group.length; e++) {
            occurrences[group[e]] = occurrences[group[0]];
        }
    }


//**************************************************
//      COVER Constraints
//**************************************************

    private void makeCover(Solver solver) {
        String option = this.getConstraintOption("pat");
        if (option.equals("gccFix")) {
            this.makeCoverWithGCCFix(solver);
        } else {
            this.makeCoverWithGCCVar(solver);
        }
    }

    private void makeCoverWithGCCVar(Solver solver) {
        description += "cover[gcc] ";
        IntVar[][] cards = ArrayUtils.transpose(covers);
        IntVar[][] vars = ArrayUtils.transpose(shifts);
        for (int t = 0; t < vars.length; t++) {
            int[] values = new int[cards[t].length];
            for (int i = 0; i < values.length; i++) {
                values[i] = i;
            }
            solver.post(IntConstraintFactory.global_cardinality(vars[t], values, cards[t], false));
        }
    }

    private void makeCoverWithGCCFix(Solver solver) {
        description += "cover[gccFix] ";
        IntVar[] cover = new IntVar[data.nbActivities()];
        for (int a = 0; a < data.nbActivities(); a++) {
            cover[a] = VariableFactory.bounded("cover_" + a, data.getCoverLB(a), data.getCoverUB(a), solver);
        }
        IntVar[][] vars = ArrayUtils.transpose(shifts);
        for (IntVar[] var : vars) {
            int[] values = new int[cover.length];
            for (int i = 0; i < values.length; i++) {
                values[i] = i;
            }
            solver.post(IntConstraintFactory.global_cardinality(var, values, cover, false));
        }
    }

//**************************************************
//      COVER-COUNTER COUPLING Constraints
//**************************************************

    private void makeCoverCounterCoupling(Solver solver) {
        if (this.isSetConstraint("equity")) {
            this.makeCoverCounterCouplingAndEquity(solver);
        } else {
            for (int a = 0; a < data.nbActivities(); a++) {
                this.makeCoverCounterCoupling(solver, a);
            }
            //		this.makeCoverCounterCoupling(data.getValue("REST"));
        }
    }


    private void makeCoverCounterCoupling(Solver solver, int a) {
        description += "coupling[" + a + "] ";
        solver.post(IntConstraintFactory.sum(ArrayUtils.getColumn(occurrences, a, IntVar.class), VariableFactory.fixed(data.getTotalCover(a), solver)));
    }

    private void makeCoverCounterCouplingAndEquity(Solver solver) {
        int[] belongsToNbGroups = new int[data.nbEmployees()];
        for (int[] group : data.equityEmployeeGroups()) {
            for (int e : group) {
                belongsToNbGroups[e]++;
            }
        }
        boolean eachEmployeeBelongsToExactlyOneGroup = true;
        for (int n : belongsToNbGroups) {
            if (n != 1) {
                eachEmployeeBelongsToExactlyOneGroup = false;
                break;
            }
        }
        if (eachEmployeeBelongsToExactlyOneGroup) {
            for (int a = 0; a < data.nbActivities(); a++) {
                this.makeCoverCounterCouplingAndEquity(solver, a);
            }
        }
    }

    private void makeCoverCounterCouplingAndEquity(Solver solver, int a) {
        IntVar[] occ = new IntVar[data.equityEmployeeGroups().size()];
        int sizes[] = new int[occ.length];
        int g = 0;
        for (int[] group : data.equityEmployeeGroups()) {
            occ[g] = occurrences[group[0]][a];
            sizes[g] = group.length;
            g++;
        }
        description += "couplingEquality[" + a + "] ";
        solver.post(IntConstraintFactory.scalar(occ, sizes, VariableFactory.fixed(data.getTotalCover(a), solver)));
    }


//**************************************************
//      MONTHLY ACTIVITY COUNTER Constraints
//**************************************************

    private void makeMonthlyCounters(Solver solver) {
        String option = this.getConstraintOption("countM");
        if (option.equals("occ")) {
            this.makeMonthlyCountersWithOccurrence(solver);
        } else {
            this.makeMonthlyCountersWithGCC(solver);
        }
    }

    private void makeMonthlyCountersWithGCC(Solver solver) {
        description += "countM[gcc] ";
        for (int e = 0; e < data.nbEmployees(); e++) {
            int[] values = new int[occurrences[e].length];
            for (int i = 0; i < values.length; i++) {
                values[i] = i;
            }
            solver.post(IntConstraintFactory.global_cardinality(shifts[e], values, occurrences[e], false));
        }

    }

    private void makeMonthlyCountersWithOccurrence(Solver solver) {
        description += "countM[occ] ";
        for (int a = 0; a < data.nbActivities(); a++) {
            this.makeMonthlyCounterWithOccurrence(solver, a);
        }
    }

    private void makeMonthlyCounterWithOccurrence(Solver solver, int a) {
        for (int e = 0; e < data.nbEmployees(); e++) {
            if (occurrences[e][a].getLB() > 0 || occurrences[e][a].getUB() < data.nbDays()) {
                solver.post(IntConstraintFactory.count(a, shifts[e], occurrences[e][a]));
            }
        }
    }

//**************************************************
//      WEEKLY ACTIVITY COUNTER Constraints
//**************************************************

    private void makeWeeklyCounters(Solver solver) {
        String option = this.getConstraintOption("countW");
        if (option.equals("occ")) {
            this.makeWeeklyCountersWithOccurrence(solver);
        } else {
            this.makeWeeklyCountersWithGCC(solver);
        }
    }

    private void makeWeeklyCountersWithOccurrence(Solver solver) {
        description += "countW[occ] ";
        for (int a = 0; a < data.nbActivities(); a++) {
            this.makeWeeklyCountersWithOccurrence(solver, a);
        }
    }

    private void makeWeeklyCountersWithOccurrence(Solver solver, int a) {
        IntVar[] vars = new IntVar[7];
        for (int e = 0; e < data.nbEmployees(); e++) {
            int lb = data.getWeekCounterLB(e, a);
            int ub = data.getWeekCounterUB(e, a);
            if (lb > 0 || ub < 7) {
                for (int t = 0; t < data.nbWeeks(); t++) {
//                    IntVar occ = ConstraintFactory.makeIntVar("nW" + t + data.getLiteral(a) + e, lb, ub, "cp:bound", Options.V_NO_DECISION);
                    IntVar occ = VariableFactory.bounded("nW" + t + data.getLiteral(a) + e, lb, ub, solver);
                    System.arraycopy(shifts[e], t * 7, vars, 0, 7);
                    solver.post(IntConstraintFactory.count(a, vars, occ));
                }
            }
        }
    }

    private void makeWeeklyCountersWithGCC(Solver solver) {
        description += "countW[gcc] ";
        IntVar[] vars = new IntVar[7];
        for (int e = 0; e < data.nbEmployees(); e++) {
            IntVar[] cards = new IntVar[data.nbActivities()];
            for (int a = 0; a < data.nbActivities(); a++) {
                cards[a] = VariableFactory.bounded("card_" + a, data.getWeekCounterLB(e, a), data.getWeekCounterLB(e, a), solver);
            }
            for (int t = 0; t < data.nbWeeks(); t++) {
                System.arraycopy(shifts[e], t * 7, vars, 0, 7);
                int[] values = new int[cards.length];
                for (int i = 0; i < values.length; i++) {
                    values[i] = i;
                }
                solver.post(IntConstraintFactory.global_cardinality(vars, values, cards, false));
            }
        }

    }

//**************************************************
//      MAXIMAL WORK SPAN Constraints
//**************************************************

    /*private void makeMaxWorkSpan(Solver solver) {
        this.makeMaxWorkSpanWithMax(solver);
    }*/

    /*// use the fact that value "REST" is greater than the value of any worked activity
    private void makeMaxWorkSpanWithMax(Solver solver) {
        description += "span[max] ";
        int r = data.getValue("REST");
        assert r >= data.nbActivities() - 1;

        for (int e = 0; e < data.nbEmployees(); e++) {
            int patternLength = data.getMaxWorkSpan(e) + 1;
            IntVar restVar = ConstraintFactory.makeIntVar("Rcst", r, r, Options.V_NO_DECISION);
            IntVar[] vars = new IntVar[patternLength];
            for (int t = 0; t + patternLength < data.nbDays(); t++) {
                System.arraycopy(shifts[e], t, vars, 0, patternLength);
                solver.post(new MaxXYZ(vars, restVar));
            }
        }
    }*/


//**************************************************
//      FORBIDDEN PATTERNS Constraints
//**************************************************

    private void makeForbiddenPatterns(Solver solver) {
        String option = this.getConstraintOption("pat");
        switch (option) {
            case "reif":
                this.makeForbiddenPatternsWithExtensionOrReified(solver);
                break;
            case "MCRegular":
                this.makeForbiddenPatternsAndMonthlyCountersWithMultiCostRegular(solver);
                break;
            default:
                this.makeForbiddenPatternsWithRegular(solver);
                break;
        }
    }

    private void makeForbiddenPatternsWithExtensionOrReified(Solver solver) {
        this.makeCompleteWEWithLogic(solver);
        //this.makeCompleteWEWithFeasTuple();
        //this.makeCompleteWEWithInfeasTuple();
        //this.makeForbidNightBeforeFreeWE(solver);
        this.makeForceRestRestAfterNights(solver);
        this.makeForbidTreeConsecutiveWEs(solver);
    }

    /*private void makeForbidNightBeforeFreeWE(Solver solver) {
        description += "patNWE[extAC] ";
        int n = data.getValue("NIGHT");
        int r = data.getValue("REST");
        List<int[]> tuples = new ArrayList<int[]>(1);
        tuples.add(new int[]{n, r, r});

        for (IntVar[] s : this.shifts) {
            for (int t = 4; t + 2 < data.nbDays(); t += 7) {
                solver.post(ConstraintFactory.infeasTupleAC(tuples, s[t], s[t + 1], s[t + 2]));
            }
        }
    }*/

    private void makeForceRestRestAfterNights(Solver solver) {
        description += "patNRR[reif] ";
        this.makeForceRestRestAfterNightsLogic(solver);
    }

    private void makeForceRestRestAfterNightsLogic(Solver solver) {
        int n = data.getValue("NIGHT");
        int r = data.getValue("REST");
        for (IntVar[] s : this.shifts) {
            for (int t = 0; t + 2 < data.nbDays(); t++) {
                //solver.post(Options.E_DECOMP, ConstraintFactory.ifThenElse(ConstraintFactory.eq(s[t], n), ConstraintFactory.or(ConstraintFactory.eq(s[t+1], n), ConstraintFactory.and(ConstraintFactory.eq(s[t+1], r), ConstraintFactory.eq(s[t+2], r)))));
                BoolVar[] bvars = VariableFactory.boolArray("b", 4, solver);
                LogOp tree = LogOp.implies(
                        bvars[0],
                        LogOp.or(bvars[1], bvars[2], bvars[3])
                );
                SatFactory.addClauses(tree, solver);
                LogicalConstraintFactory.ifThenElse(bvars[0],
                        IntConstraintFactory.arithm(s[t], "=", n), IntConstraintFactory.arithm(s[t], "!=", n));
                LogicalConstraintFactory.ifThenElse(bvars[1],
                        IntConstraintFactory.arithm(s[t + 1], "=", n), IntConstraintFactory.arithm(s[t + 1], "!=", n));
                LogicalConstraintFactory.ifThenElse(bvars[2],
                        IntConstraintFactory.arithm(s[t + 1], "=", r), IntConstraintFactory.arithm(s[t + 1], "!=", r));
                LogicalConstraintFactory.ifThenElse(bvars[3],
                        IntConstraintFactory.arithm(s[t + 2], "=", r), IntConstraintFactory.arithm(s[t + 2], "!=", r));
            }
            int t = data.nbDays() - 2;
            BoolVar[] bvars = VariableFactory.boolArray("b", 2, solver);
//            solver.post(ConstraintFactory.ifThenElse(ConstraintFactory.eq(s[t], n), ConstraintFactory.eq(s[t + 1], n)));
            LogOp tree = LogOp.implies(bvars[0], bvars[1]);
            SatFactory.addClauses(tree, solver);
            LogicalConstraintFactory.ifThenElse(bvars[0],
                    IntConstraintFactory.arithm(s[t], "=", n), IntConstraintFactory.arithm(s[t], "!=", n));
            LogicalConstraintFactory.ifThenElse(bvars[1],
                    IntConstraintFactory.arithm(s[t + 1], "=", n), IntConstraintFactory.arithm(s[t + 1], "!=", n));
        }
    }

    /*private void makeForceRestRestAfterNightsReifTuple(Solver solver) {
        int n = data.getValue("NIGHT");
        int r = data.getValue("REST");
        List<int[]> tuples = new ArrayList<int[]>(3);
        tuples.add(new int[]{n, r});
        tuples.add(new int[]{n, n});
        tuples.add(new int[]{r, r});
        for (IntVar[] s : this.shifts) {
            for (int t = 0; t + 2 < data.nbDays(); t++) {
                solver.post(ConstraintFactory.ifThenElse(ConstraintFactory.eq(s[t], n), ConstraintFactory.feasPairAC(s[t + 1], s[t + 2], tuples)));
            }
            int t = data.nbDays() - 2;
            solver.post(ConstraintFactory.ifThenElse(ConstraintFactory.eq(s[t], n), ConstraintFactory.eq(s[t + 1], n)));
        }
    }*/

    private void makeForbidTreeConsecutiveWEs(Solver solver) {
        description += "patConsWE[reif] ";
        int r = data.getValue("REST");
        for (IntVar[] s : this.shifts) {
            for (int t = 5; t + 15 < data.nbDays(); t += 7) {
                BoolVar[] bvars = VariableFactory.boolArray("b", 6, solver);
                LogOp tree = LogOp.implies(
                        LogOp.and(
                                LogOp.or(bvars[0], bvars[1]),
                                LogOp.or(bvars[2], bvars[3])),
                        LogOp.and(bvars[4], bvars[5])
                );
                SatFactory.addClauses(tree, solver);
                LogicalConstraintFactory.ifThenElse(bvars[0],
                        IntConstraintFactory.arithm(s[t], "=", r),
                        IntConstraintFactory.arithm(s[t], "!=", r));
                LogicalConstraintFactory.ifThenElse(bvars[1],
                        IntConstraintFactory.arithm(s[t + 1], "=", r),
                        IntConstraintFactory.arithm(s[t + 1], "!=", r));
                LogicalConstraintFactory.ifThenElse(bvars[2],
                        IntConstraintFactory.arithm(s[t + 7], "=", r), IntConstraintFactory.arithm(s[t + 7], "!=", r));
                LogicalConstraintFactory.ifThenElse(bvars[3],
                        IntConstraintFactory.arithm(s[t + 8], "=", r), IntConstraintFactory.arithm(s[t + 8], "!=", r));
                LogicalConstraintFactory.ifThenElse(bvars[4],
                        IntConstraintFactory.arithm(s[t + 14], "=", r), IntConstraintFactory.arithm(s[t + 14], "!=", r));
                LogicalConstraintFactory.ifThenElse(bvars[5],
                        IntConstraintFactory.arithm(s[t + 15], "=", r), IntConstraintFactory.arithm(s[t + 15], "!=", r));

                /*solver.post(ConstraintFactory.ifThenElse(
         ConstraintFactory.and(
                 ConstraintFactory.or(ConstraintFactory.neq(s[t], r),
                         ConstraintFactory.neq(s[t + 1], r)),
                 ConstraintFactory.or(ConstraintFactory.neq(s[t + 7], r),
                         ConstraintFactory.neq(s[t + 8], r))),
         ConstraintFactory.and(ConstraintFactory.eq(s[t + 14], r), ConstraintFactory.eq(s[t + 15], r))));*/
            }
        }
    }

//**************************************************
//      COMPLETE WEEK-END Constraints
//**************************************************

    /*private void makeCompleteWEWithFeasTuple(Solver solver) {
        description += "patCompWE[feasAC] ";
        int r = data.getValue("REST");
        List<int[]> tuples = new ArrayList<int[]>();
        tuples.add(new int[]{r, r});
        for (int w1 = 0; w1 < r; w1++) {
            tuples.add(new int[]{w1, w1});
            for (int w2 = w1 + 1; w2 < r; w2++) {
                tuples.add(new int[]{w1, w2});
                tuples.add(new int[]{w2, w1});
            }
        }
        for (IntVar[] s : this.shifts) {
            for (int t = 5; t + 1 < data.nbDays(); t += 7) {
                solver.post(ConstraintFactory.feasPairAC(s[t], s[t + 1], tuples));
            }
        }
    }*/

    /*private void makeCompleteWEWithInfeasTuple(Solver solver) {
        description += "patCompWE[infeasAC] ";
        int r = data.getValue("REST");
        List<int[]> tuples = new ArrayList<int[]>();
        for (int w = 0; w < r; w++) {
            tuples.add(new int[]{w, r});
            tuples.add(new int[]{r, w});
        }
        for (IntVar[] s : this.shifts) {
            for (int t = 5; t + 1 < data.nbDays(); t += 7) {
                solver.post(ConstraintFactory.infeasPairAC(s[t], s[t + 1], tuples));
            }
        }
    }*/


    private void makeCompleteWEWithLogic(Solver solver) {
        description += "patCompWE[reif] ";
        int r = data.getValue("REST");
        for (IntVar[] s : this.shifts) {
            for (int t = 5; t + 1 < data.nbDays(); t += 7) {
                BoolVar[] bvars = VariableFactory.boolArray("b", 2, solver);
                LogOp tree = LogOp.ifOnlyIf(bvars[0], bvars[1]);
                SatFactory.addClauses(tree, solver);
                LogicalConstraintFactory.ifThenElse(bvars[0],
                        IntConstraintFactory.arithm(s[t], "=", r),
                        IntConstraintFactory.arithm(s[t], "!=", r));
                LogicalConstraintFactory.ifThenElse(bvars[1],
                        IntConstraintFactory.arithm(s[t + 1], "=", r),
                        IntConstraintFactory.arithm(s[t + 1], "!=", r));
                //solver.post(ConstraintFactory.ifOnlyIf(ConstraintFactory.eq(s[t], r), ConstraintFactory.eq(s[t + 1], r)));
            }
        }
    }

//**************************************************
//      AUTOMATON Constraints
//**************************************************


    private FiniteAutomaton makeForbiddenPatternsAsAutomaton() {
        FiniteAutomaton automaton = new FiniteAutomaton();
        for (String reg : data.forbiddenRegExps()) {
            FiniteAutomaton a = new FiniteAutomaton(reg);
            automaton = automaton.union(a);
            automaton.minimize();
        }
        for (int a = 0; a < data.nbActivities(); a++) {
            automaton.addToAlphabet(a);
        }
        automaton = automaton.complement();
        automaton.minimize();
        return automaton;
    }

    private void makeForbiddenPatternsWithRegular(Solver solver) {
        FiniteAutomaton automaton = this.makeForbiddenPatternsAsAutomaton();
        description += "pat[regular/" + automaton.getNbStates() + "] ";
        for (int e = 0; e < data.nbEmployees(); e++) {
            solver.post(IntConstraintFactory.regular(shifts[e], automaton));
        }
    }

    private void makeForbiddenPatternsAndMonthlyCountersWithMultiCostRegular(Solver solver) {
        FiniteAutomaton automaton = this.makeForbiddenPatternsAsAutomaton();
        int[][][] costs = new int[data.nbDays()][data.nbActivities()][data.nbActivities()];
        for (int a = 0; a < data.nbActivities(); a++) {
            for (int t = 0; t < data.nbDays(); t++) {
                costs[t][a][a] = 1;
            }
        }
        description += "pat[MCRegular/" + automaton.getNbStates() + "/" + costs[0][0].length + "] ";
        for (int e = 0; e < data.nbEmployees(); e++) {
            solver.post(IntConstraintFactory.multicost_regular(shifts[e], occurrences[e], CostAutomaton.makeMultiResources(automaton, costs, occurrences[e])));
        }
    }


    private void makeForbiddenPatternsAndMonthlyAndRestWeeklyCountersWithMultiCostRegular(Solver solver) {
        FiniteAutomaton automaton = this.makeForbiddenPatternsAsAutomaton();

        int[][][] costs = new int[data.nbDays()][data.nbActivities()][data.nbActivities() + data.nbWeeks()];
        for (int a = 0; a < data.nbActivities(); a++) {
            for (int t = 0; t < data.nbDays(); t++) {
                costs[t][a][a] = 1;
            }
        }
        description += "pat[MCRegular/" + automaton.getNbStates() + "/" + costs[0][0].length + "] ";

        int r = data.getValue("REST");
        for (int w = 0; w < data.nbWeeks(); w++) {
            for (int t = 0; t < 7; t++) {
                costs[7 * w + t][r][data.nbActivities() + w] = 1;
            }
        }
        for (int e = 0; e < data.nbEmployees(); e++) {
            int lb = data.getWeekCounterLB(e, r);
            int ub = data.getWeekCounterUB(e, r);
            IntVar[] occs = VariableFactory.boundedArray("nWR", data.nbWeeks(), lb, ub, solver);
            IntVar[] cv = ArrayUtils.append(occurrences[e], occs);
            solver.post(IntConstraintFactory.multicost_regular(shifts[e], cv, CostAutomaton.makeMultiResources(automaton, costs, cv)));
        }
    }

}


