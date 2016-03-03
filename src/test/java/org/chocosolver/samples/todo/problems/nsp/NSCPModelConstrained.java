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
package org.chocosolver.samples.todo.problems.nsp;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.SatFactory;
import org.chocosolver.solver.constraints.nary.automata.CostRegular;
import org.chocosolver.solver.constraints.nary.automata.FA.FiniteAutomaton;
import org.chocosolver.solver.constraints.nary.cnf.LogOp;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.tools.ArrayUtils;

import static java.lang.System.arraycopy;
import static org.chocosolver.solver.constraints.nary.automata.FA.CostAutomaton.makeMultiResources;
import static org.chocosolver.util.tools.ArrayUtils.getColumn;

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

    public NSCPModelConstrained(Model model) {
        this(NSData.makeDefaultInstance(), model);
    }

    public NSCPModelConstrained(NSData data, Model model) {
        this(data, ConstraintOptions.REDUNDANT, ConstraintOptions.WITH_REIF, model);
    }

    public NSCPModelConstrained(NSData data, ConstraintOptions basisOptions, ConstraintOptions patternOptions, Model model) {
        this(data, basisOptions.getOptions(), patternOptions.getOptions(), model);
    }

    public NSCPModelConstrained(NSData data, String options, String patternOptions, Model model) {
        super(data, options + " " + patternOptions, model);
        this.makeConstraints(model);
    }


    private void makeConstraints(Model model) {
        if (this.isSetConstraint("cover")) {
            this.makeCover(model);
        }
        if (this.isSetConstraint("equity")) {
            this.makeEquity();
        }
        if (this.isSetConstraint("preAssign")) {
            this.makePreAssignments(model);
        }
//        if (this.isSetConstraint("symBreak")) {
//            this.makeSymmetryBreaking(solver);
//        }
        if (this.isSetConstraint("coupling")) {
            this.makeCoverCounterCoupling(model);
        }
        if (this.isSetConstraint("countM")) {
            this.makeMonthlyCounters(model);
        }
        if (this.isSetConstraint("countW")) {
            this.makeWeeklyCounters(model);
        }
//        if (this.isSetConstraint("span")) {
//            this.makeMaxWorkSpan(solver);
//        }
        if (this.isSetConstraint("pat")) {
            this.makeForbiddenPatterns(model);
        }
    }

//**************************************************
//      MANDATORY/FORBIDDEN Assignments
//**************************************************

    private void makePreAssignments(Model model) {
        description += "preAssign ";
        for (int[] trip : data.preAssignments()) {
            boolean mandatory = trip[0] > 0;
            int e = trip[1];
            int t = trip[2];
            int a = trip[3];
            if (mandatory) {
                model.arithm(shifts[e][t], "=", a).post();
            } else {
                model.arithm(shifts[e][t], "!=", a).post();
            }
        }
    }

//**************************************************
//      SYMMETRY BREAKING Constraints
//**************************************************

    /*private void makeSymmetryBreaking(Model solver) {
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
        data.equityEmployeeGroups().forEach(this::makeEquityDirect);
    }

    private void makeEquityDirect(int[] group) {
        for (int e = 1; e < group.length; e++) {
            occurrences[group[e]] = occurrences[group[0]];
        }
    }


//**************************************************
//      COVER Constraints
//**************************************************

    private void makeCover(Model model) {
        String option = this.getConstraintOption("pat");
        if (option.equals("gccFix")) {
            this.makeCoverWithGCCFix(model);
        } else {
            this.makeCoverWithGCCVar(model);
        }
    }

    private void makeCoverWithGCCVar(Model model) {
        description += "cover[gcc] ";
        IntVar[][] cards = ArrayUtils.transpose(covers);
        IntVar[][] vars = ArrayUtils.transpose(shifts);
        for (int t = 0; t < vars.length; t++) {
            int[] values = new int[cards[t].length];
            for (int i = 0; i < values.length; i++) {
                values[i] = i;
            }
            model.globalCardinality(vars[t], values, cards[t], false).post();
        }
    }

    private void makeCoverWithGCCFix(Model model) {
        description += "cover[gccFix] ";
        IntVar[] cover = new IntVar[data.nbActivities()];
        for (int a = 0; a < data.nbActivities(); a++) {
            cover[a] = model.intVar("cover_" + a, data.getCoverLB(a), data.getCoverUB(a), true);
        }
        IntVar[][] vars = ArrayUtils.transpose(shifts);
        for (IntVar[] var : vars) {
            int[] values = new int[cover.length];
            for (int i = 0; i < values.length; i++) {
                values[i] = i;
            }
            model.globalCardinality(var, values, cover, false).post();
        }
    }

//**************************************************
//      COVER-COUNTER COUPLING Constraints
//**************************************************

    private void makeCoverCounterCoupling(Model model) {
        if (this.isSetConstraint("equity")) {
            this.makeCoverCounterCouplingAndEquity(model);
        } else {
            for (int a = 0; a < data.nbActivities(); a++) {
                this.makeCoverCounterCoupling(model, a);
            }
            //		this.makeCoverCounterCoupling(data.getValue("REST"));
        }
    }


    private void makeCoverCounterCoupling(Model model, int a) {
        description += "coupling[" + a + "] ";
        model.sum(getColumn(occurrences, a), "=", data.getTotalCover(a)).post();
    }

    private void makeCoverCounterCouplingAndEquity(Model model) {
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
                this.makeCoverCounterCouplingAndEquity(model, a);
            }
        }
    }

    private void makeCoverCounterCouplingAndEquity(Model model, int a) {
        IntVar[] occ = new IntVar[data.equityEmployeeGroups().size()];
        int sizes[] = new int[occ.length];
        int g = 0;
        for (int[] group : data.equityEmployeeGroups()) {
            occ[g] = occurrences[group[0]][a];
            sizes[g] = group.length;
            g++;
        }
        description += "couplingEquality[" + a + "] ";
        model.scalar(occ, sizes, "=", data.getTotalCover(a)).post();
    }


//**************************************************
//      MONTHLY ACTIVITY COUNTER Constraints
//**************************************************

    private void makeMonthlyCounters(Model model) {
        String option = this.getConstraintOption("countM");
        if (option.equals("occ")) {
            this.makeMonthlyCountersWithOccurrence(model);
        } else {
            this.makeMonthlyCountersWithGCC(model);
        }
    }

    private void makeMonthlyCountersWithGCC(Model model) {
        description += "countM[gcc] ";
        for (int e = 0; e < data.nbEmployees(); e++) {
            int[] values = new int[occurrences[e].length];
            for (int i = 0; i < values.length; i++) {
                values[i] = i;
            }
            model.globalCardinality(shifts[e], values, occurrences[e], false).post();
        }

    }

    private void makeMonthlyCountersWithOccurrence(Model model) {
        description += "countM[occ] ";
        for (int a = 0; a < data.nbActivities(); a++) {
            this.makeMonthlyCounterWithOccurrence(model, a);
        }
    }

    private void makeMonthlyCounterWithOccurrence(Model model, int a) {
        for (int e = 0; e < data.nbEmployees(); e++) {
            if (occurrences[e][a].getLB() > 0 || occurrences[e][a].getUB() < data.nbDays()) {
                model.count(a, shifts[e], occurrences[e][a]).post();
            }
        }
    }

//**************************************************
//      WEEKLY ACTIVITY COUNTER Constraints
//**************************************************

    private void makeWeeklyCounters(Model model) {
        String option = this.getConstraintOption("countW");
        if (option.equals("occ")) {
            this.makeWeeklyCountersWithOccurrence(model);
        } else {
            this.makeWeeklyCountersWithGCC(model);
        }
    }

    private void makeWeeklyCountersWithOccurrence(Model model) {
        description += "countW[occ] ";
        for (int a = 0; a < data.nbActivities(); a++) {
            this.makeWeeklyCountersWithOccurrence(model, a);
        }
    }

    private void makeWeeklyCountersWithOccurrence(Model model, int a) {
        IntVar[] vars = new IntVar[7];
        for (int e = 0; e < data.nbEmployees(); e++) {
            int lb = data.getWeekCounterLB(e, a);
            int ub = data.getWeekCounterUB(e, a);
            if (lb > 0 || ub < 7) {
                for (int t = 0; t < data.nbWeeks(); t++) {
//                    IntVar occ = ConstraintFactory.makeIntVar("nW" + t + data.getLiteral(a) + e, lb, ub, "cp:bound", Options.V_NO_DECISION);
                    IntVar occ = model.intVar("nW" + t + data.getLiteral(a) + e, lb, ub, true);
                    arraycopy(shifts[e], t * 7, vars, 0, 7);
                    model.count(a, vars, occ).post();
                }
            }
        }
    }

    private void makeWeeklyCountersWithGCC(Model model) {
        description += "countW[gcc] ";
        IntVar[] vars = new IntVar[7];
        for (int e = 0; e < data.nbEmployees(); e++) {
            IntVar[] cards = new IntVar[data.nbActivities()];
            for (int a = 0; a < data.nbActivities(); a++) {
                cards[a] = model.intVar("card_" + a, data.getWeekCounterLB(e, a), data.getWeekCounterLB(e, a), true);
            }
            for (int t = 0; t < data.nbWeeks(); t++) {
                arraycopy(shifts[e], t * 7, vars, 0, 7);
                int[] values = new int[cards.length];
                for (int i = 0; i < values.length; i++) {
                    values[i] = i;
                }
                model.globalCardinality(vars, values, cards, false).post();
            }
        }

    }

//**************************************************
//      MAXIMAL WORK SPAN Constraints
//**************************************************

    /*private void makeMaxWorkSpan(Model solver) {
        this.makeMaxWorkSpanWithMax(solver);
    }*/

    /*// use the fact that value "REST" is greater than the value of any worked activity
    private void makeMaxWorkSpanWithMax(Model solver) {
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

    private void makeForbiddenPatterns(Model model) {
        String option = this.getConstraintOption("pat");
        switch (option) {
            case "reif":
                this.makeForbiddenPatternsWithExtensionOrReified(model);
                break;
            case "MCRegular":
                this.makeForbiddenPatternsAndMonthlyCountersWithMultiCostRegular(model);
                break;
            default:
                this.makeForbiddenPatternsWithRegular(model);
                break;
        }
    }

    private void makeForbiddenPatternsWithExtensionOrReified(Model model) {
        this.makeCompleteWEWithLogic(model);
        //this.makeCompleteWEWithFeasTuple();
        //this.makeCompleteWEWithInfeasTuple();
        //this.makeForbidNightBeforeFreeWE(solver);
        this.makeForceRestRestAfterNights(model);
        this.makeForbidTreeConsecutiveWEs(model);
    }

    /*private void makeForbidNightBeforeFreeWE(Model solver) {
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

    private void makeForceRestRestAfterNights(Model model) {
        description += "patNRR[reif] ";
        this.makeForceRestRestAfterNightsLogic(model);
    }

    private void makeForceRestRestAfterNightsLogic(Model model) {
        int n = data.getValue("NIGHT");
        int r = data.getValue("REST");
        for (IntVar[] s : this.shifts) {
            for (int t = 0; t + 2 < data.nbDays(); t++) {
                //solver.post(Options.E_DECOMP, ConstraintFactory.ifThenElse(ConstraintFactory.eq(s[t], n), ConstraintFactory.or(ConstraintFactory.eq(s[t+1], n), ConstraintFactory.and(ConstraintFactory.eq(s[t+1], r), ConstraintFactory.eq(s[t+2], r)))));
                BoolVar[] bvars = model.boolVarArray("b", 4);
                LogOp tree = LogOp.implies(
                        bvars[0],
                        LogOp.or(bvars[1], bvars[2], bvars[3])
                );
                SatFactory.addClauses(tree, model);
                model.ifThenElse(bvars[0],
                        model.arithm(s[t], "=", n), model.arithm(s[t], "!=", n));
                model.ifThenElse(bvars[1],
                        model.arithm(s[t + 1], "=", n), model.arithm(s[t + 1], "!=", n));
                model.ifThenElse(bvars[2],
                        model.arithm(s[t + 1], "=", r), model.arithm(s[t + 1], "!=", r));
                model.ifThenElse(bvars[3],
                        model.arithm(s[t + 2], "=", r), model.arithm(s[t + 2], "!=", r));
            }
            int t = data.nbDays() - 2;
            BoolVar[] bvars = model.boolVarArray("b", 2);
//            solver.post(ConstraintFactory.ifThenElse(ConstraintFactory.eq(s[t], n), ConstraintFactory.eq(s[t + 1], n)));
            LogOp tree = LogOp.implies(bvars[0], bvars[1]);
            SatFactory.addClauses(tree, model);
            model.ifThenElse(bvars[0],
                    model.arithm(s[t], "=", n), model.arithm(s[t], "!=", n));
            model.ifThenElse(bvars[1],
                    model.arithm(s[t + 1], "=", n), model.arithm(s[t + 1], "!=", n));
        }
    }

    /*private void makeForceRestRestAfterNightsReifTuple(Model solver) {
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

    private void makeForbidTreeConsecutiveWEs(Model model) {
        description += "patConsWE[reif] ";
        int r = data.getValue("REST");
        for (IntVar[] s : this.shifts) {
            for (int t = 5; t + 15 < data.nbDays(); t += 7) {
                BoolVar[] bvars = model.boolVarArray("b", 6);
                LogOp tree = LogOp.implies(
                        LogOp.and(
                                LogOp.or(bvars[0], bvars[1]),
                                LogOp.or(bvars[2], bvars[3])),
                        LogOp.and(bvars[4], bvars[5])
                );
                SatFactory.addClauses(tree, model);
                model.ifThenElse(bvars[0],
                        model.arithm(s[t], "=", r),
                        model.arithm(s[t], "!=", r));
                model.ifThenElse(bvars[1],
                        model.arithm(s[t + 1], "=", r),
                        model.arithm(s[t + 1], "!=", r));
                model.ifThenElse(bvars[2],
                        model.arithm(s[t + 7], "=", r), model.arithm(s[t + 7], "!=", r));
                model.ifThenElse(bvars[3],
                        model.arithm(s[t + 8], "=", r), model.arithm(s[t + 8], "!=", r));
                model.ifThenElse(bvars[4],
                        model.arithm(s[t + 14], "=", r), model.arithm(s[t + 14], "!=", r));
                model.ifThenElse(bvars[5],
                        model.arithm(s[t + 15], "=", r), model.arithm(s[t + 15], "!=", r));

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

    /*private void makeCompleteWEWithFeasTuple(Model solver) {
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

    /*private void makeCompleteWEWithInfeasTuple(Model solver) {
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


    private void makeCompleteWEWithLogic(Model model) {
        description += "patCompWE[reif] ";
        int r = data.getValue("REST");
        for (IntVar[] s : this.shifts) {
            for (int t = 5; t + 1 < data.nbDays(); t += 7) {
                BoolVar[] bvars = model.boolVarArray("b", 2);
                LogOp tree = LogOp.ifOnlyIf(bvars[0], bvars[1]);
                SatFactory.addClauses(tree, model);
                model.ifThenElse(bvars[0],
                        model.arithm(s[t], "=", r),
                        model.arithm(s[t], "!=", r));
                model.ifThenElse(bvars[1],
                        model.arithm(s[t + 1], "=", r),
                        model.arithm(s[t + 1], "!=", r));
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

    private void makeForbiddenPatternsWithRegular(Model model) {
        FiniteAutomaton automaton = this.makeForbiddenPatternsAsAutomaton();
        description += "pat[regular/" + automaton.getNbStates() + "] ";
        for (int e = 0; e < data.nbEmployees(); e++) {
            model.regular(shifts[e], automaton).post();
        }
    }

    private void makeForbiddenPatternsAndMonthlyCountersWithMultiCostRegular(Model model) {
        FiniteAutomaton automaton = this.makeForbiddenPatternsAsAutomaton();
        int[][][] costs = new int[data.nbDays()][data.nbActivities()][data.nbActivities()];
        for (int a = 0; a < data.nbActivities(); a++) {
            for (int t = 0; t < data.nbDays(); t++) {
                costs[t][a][a] = 1;
            }
        }
        description += "pat[MCRegular/" + automaton.getNbStates() + "/" + costs[0][0].length + "] ";
        for (int e = 0; e < data.nbEmployees(); e++) {
            model.multiCostRegular(shifts[e], occurrences[e], makeMultiResources(automaton, costs, occurrences[e])).post();
        }
    }


}


