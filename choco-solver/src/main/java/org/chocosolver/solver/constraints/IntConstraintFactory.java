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
package org.chocosolver.solver.constraints;

import gnu.trove.list.array.TIntArrayList;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.extension.Tuples;
import org.chocosolver.solver.constraints.extension.TuplesFactory;
import org.chocosolver.solver.constraints.nary.alldifferent.conditions.Condition;
import org.chocosolver.solver.constraints.nary.automata.FA.IAutomaton;
import org.chocosolver.solver.constraints.nary.automata.FA.ICostAutomaton;
import org.chocosolver.solver.constraints.nary.circuit.CircuitConf;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Task;
import org.chocosolver.util.objects.graphs.MultivaluedDecisionDiagram;

/**
 * @deprecated : int constraint creation should be done through the {@link Solver} object
 * which extends {@link org.chocosolver.solver.constraints.IIntConstraintFactory}
 *
 * This class will be removed in versions > 3.4.0
 */
@Deprecated
@SuppressWarnings("UnusedDeclaration")
public class IntConstraintFactory {
    IntConstraintFactory() {}

    // BEWARE: PLEASE, keep signatures sorted by increasing arity and alphabetical order!!

    //##################################################################################################################
    // ZEROARIES #######################################################################################################
    //##################################################################################################################

    /**
     * @deprecated : use {@link Solver#TRUE()} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint TRUE(Solver solver) {
        return solver.TRUE();
    }

    /**
     * @deprecated : use {@link Solver#FALSE()} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint FALSE(Solver solver) {
        return solver.FALSE();
    }

    //##################################################################################################################
    // UNARIES #########################################################################################################
    //##################################################################################################################

    /**
     * @deprecated : use {@link Solver#arithm(IntVar, String, int)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint arithm(IntVar VAR, String OP, int CSTE) {
        return VAR.getSolver().arithm(VAR,OP,CSTE);
    }

    /**
     * @deprecated : use {@link Solver#member(IntVar, int[])} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint member(IntVar VAR, int[] TABLE) {
        return VAR.getSolver().member(VAR,TABLE);
    }

    /**
     * @deprecated : use {@link Solver#member(IntVar, int, int)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint member(IntVar VAR, int LB, int UB) {
        return VAR.getSolver().member(VAR,LB,UB);
    }

    /**
     * @deprecated : use {@link Solver#notMember(IntVar, int[])} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint not_member(IntVar VAR, int[] TABLE) {
        return VAR.getSolver().notMember(VAR,TABLE);
    }

    /**
     * @deprecated : use {@link Solver#notMember(IntVar, int, int)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint not_member(IntVar VAR, int LB, int UB) {
        return VAR.getSolver().notMember(VAR,LB,UB);
    }

    //##################################################################################################################
    //BINARIES #########################################################################################################
    //##################################################################################################################

    /**
     * @deprecated : use {@link Solver#absolute(IntVar, IntVar)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint absolute(IntVar VAR1, IntVar VAR2) {
        return VAR1.getSolver().absolute(VAR1,VAR2);
    }

    /**
     * @deprecated : use {@link Solver#arithm(IntVar, String, IntVar)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint arithm(IntVar VAR1, String OP, IntVar VAR2) {
        return VAR1.getSolver().arithm(VAR1,OP,VAR2);
    }

    /**
     * @deprecated : use {@link Solver#arithm(IntVar, String, IntVar, String, int)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint arithm(IntVar VAR1, String OP1, IntVar VAR2, String OP2, int CSTE) {
        return VAR1.getSolver().arithm(VAR1,OP1,VAR2,OP2,CSTE);
    }

    /**
     * @deprecated : use {@link Solver#distance(IntVar, IntVar, String, int)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint distance(IntVar VAR1, IntVar VAR2, String OP, int CSTE) {
        return VAR1.getSolver().distance(VAR1,VAR2,OP,CSTE);
    }

    /**
     * @deprecated : use {@link Solver#element(IntVar, int[], IntVar, int)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint element(IntVar VALUE, int[] TABLE, IntVar INDEX, int OFFSET) {
        return VALUE.getSolver().element(VALUE, TABLE, INDEX, OFFSET);
    }

    /**
     * @deprecated : use {@link Solver#element(IntVar, int[], IntVar)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint element(IntVar VALUE, int[] TABLE, IntVar INDEX) {
        return VALUE.getSolver().element(VALUE,TABLE,INDEX);
    }

    /**
     * @deprecated : use {@link Solver#square(IntVar, IntVar)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint square(IntVar VAR1, IntVar VAR2) {
        return VAR1.getSolver().square(VAR1,VAR2);
    }

    /**
     * @deprecated : use {@link Solver#table(IntVar, IntVar, Tuples)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint table(IntVar VAR1, IntVar VAR2, Tuples TUPLES) {
        return table(VAR1,VAR2,TUPLES,"AC3rm");
    }

    /**
     * @deprecated : use {@link Solver#table(IntVar, IntVar, Tuples, String)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint table(IntVar VAR1, IntVar VAR2, Tuples TUPLES, String ALGORITHM) {
        return VAR1.getSolver().table(VAR1,VAR2,TUPLES,ALGORITHM);
    }

    //##################################################################################################################
    //TERNARIES ########################################################################################################
    //##################################################################################################################

    /**
     * @deprecated : use {@link Solver#arithm(IntVar, String, IntVar, String, IntVar)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint arithm(IntVar VAR1, String OP1, IntVar VAR2, String OP2, IntVar VAR3) {
        return VAR1.getSolver().arithm(VAR1,OP1,VAR2,OP2,VAR3);
    }

    /**
     * @deprecated : use {@link Solver#distance(IntVar, IntVar, String, IntVar)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint distance(IntVar VAR1, IntVar VAR2, String OP, IntVar VAR3) {
        return VAR1.getSolver().distance(VAR1,VAR2,OP,VAR3);
    }

    /**
     * @deprecated : use {@link Solver#div(IntVar, IntVar, IntVar)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint eucl_div(IntVar DIVIDEND, IntVar DIVISOR, IntVar RESULT) {
        return RESULT.getSolver().div(DIVIDEND,DIVISOR,RESULT);
    }

    /**
     * @deprecated : use {@link Solver#max(IntVar, IntVar, IntVar)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint maximum(IntVar MAX, IntVar VAR1, IntVar VAR2) {
        return MAX.getSolver().max(MAX,VAR1,VAR2);
    }

    /**
     * @deprecated : use {@link Solver#min(IntVar, IntVar, IntVar)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint minimum(IntVar MIN, IntVar VAR1, IntVar VAR2) {
        return MIN.getSolver().min(MIN,VAR1,VAR2);
    }

    /**
     * @deprecated : use {@link Solver#mod(IntVar, IntVar, IntVar)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint mod(IntVar X, IntVar Y, IntVar Z) {
        return X.getSolver().mod(X,Y,Z);
    }

    /**
     * @deprecated : use {@link Solver#times(IntVar, IntVar, IntVar)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    @SuppressWarnings("SuspiciousNameCombination")
    public static Constraint times(IntVar X, IntVar Y, IntVar Z) {
        return X.getSolver().times(X,Y,Z);
    }

    /**
     * @deprecated : use {@link Solver#times(IntVar, int, IntVar)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint times(IntVar X, int Y, IntVar Z) {
        return X.getSolver().times(X,Y,Z);
    }

    //##################################################################################################################
    //GLOBALS ##########################################################################################################
    //##################################################################################################################

    /**
     * @deprecated : use {@link Solver#allDifferent(IntVar[])} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint alldifferent(IntVar[] VARS) {
        return alldifferent(VARS, "DEFAULT");
    }

    /**
     * @deprecated : use {@link Solver#allDifferent(IntVar[], String)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint alldifferent(IntVar[] VARS, String CONSISTENCY) {
        return VARS[0].getSolver().allDifferent(VARS,CONSISTENCY);
    }

    /**
     * @deprecated : use {@link Solver#allDifferentUnderCondition(IntVar[], Condition, boolean)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint alldifferent_conditionnal(IntVar[] VARS, Condition CONDITION, boolean AC) {
        return VARS[0].getSolver().allDifferentUnderCondition(VARS,CONDITION,AC);
    }

    /**
     * @deprecated : use {@link Solver#allDifferentUnderCondition(IntVar[], Condition)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint alldifferent_conditionnal(IntVar[] VARS, Condition CONDITION) {
        return alldifferent_conditionnal(VARS, CONDITION, false);
    }

    /**
     * @deprecated : use {@link Solver#allDifferentExcept0(IntVar[])} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint alldifferent_except_0(IntVar[] VARS) {
        return alldifferent_conditionnal(VARS, Condition.EXCEPT_0);
    }

    /**
     * @deprecated : use {@link Solver#among(IntVar, IntVar[], int[])} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint among(IntVar NVAR, IntVar[] VARS, int[] VALUES) {
        return VARS[0].getSolver().among(NVAR,VARS,VALUES);
    }

    /**
     * @deprecated : use {@link Solver#atLeastNValues(IntVar[], IntVar, boolean)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint atleast_nvalues(IntVar[] VARS, IntVar NVALUES, boolean AC) {
        return VARS[0].getSolver().atLeastNValues(VARS,NVALUES,AC);
    }

    /**
     * @deprecated : use {@link Solver#atMostNVvalues(IntVar[], IntVar, boolean)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint atmost_nvalues(IntVar[] VARS, IntVar NVALUES, boolean STRONG) {
        return VARS[0].getSolver().atMostNVvalues(VARS,NVALUES,STRONG);
    }

    /**
     * @deprecated : use {@link Solver#binPacking(IntVar[], int[], IntVar[], int)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint bin_packing(IntVar[] ITEM_BIN, int[] ITEM_SIZE, IntVar[] BIN_LOAD, int OFFSET) {
        return ITEM_BIN[0].getSolver().binPacking(ITEM_BIN,ITEM_SIZE,BIN_LOAD,OFFSET);
    }

    /**
     * @deprecated : use {@link Solver#boolsIntChanneling(BoolVar[], IntVar, int)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint boolean_channeling(BoolVar[] BVARS, IntVar VAR, int OFFSET) {
        return VAR.getSolver().boolsIntChanneling(BVARS,VAR,OFFSET);
    }

    /**
     * @deprecated : use {@link Solver#bitsIntChanneling(BoolVar[], IntVar)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint bit_channeling(BoolVar[] BITS, IntVar VAR) {
        return VAR.getSolver().bitsIntChanneling(BITS,VAR);
    }

    /**
     * @deprecated : use {@link Solver#clausesIntChanneling(IntVar, BoolVar[], BoolVar[])} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint clause_channeling(IntVar VAR, BoolVar[] EVARS, BoolVar[] LVARS) {
        return VAR.getSolver().clausesIntChanneling(VAR,EVARS,LVARS);
    }

    /**
     * @deprecated : use {@link Solver#circuit(IntVar[], int, CircuitConf)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint circuit(IntVar[] VARS, int OFFSET, CircuitConf CONF) {
        return VARS[0].getSolver().circuit(VARS,OFFSET,CONF);
    }

    /**
     * @deprecated : use {@link Solver#circuit(IntVar[], int)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint circuit(IntVar[] VARS, int OFFSET) {
        return circuit(VARS, OFFSET, CircuitConf.RD);
    }

    /**
     * @deprecated : use {@link Solver#costRegular(IntVar[], IntVar, ICostAutomaton)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint cost_regular(IntVar[] VARS, IntVar COST, ICostAutomaton CAUTOMATON) {
        return VARS[0].getSolver().costRegular(VARS,COST,CAUTOMATON);
    }

    /**
     * @deprecated : use {@link Solver#count(int, IntVar[], IntVar)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint count(int VALUE, IntVar[] VARS, IntVar LIMIT) {
        return VARS[0].getSolver().count(VALUE,VARS,LIMIT);
    }

    /**
     * @deprecated : use {@link Solver#count(IntVar, IntVar[], IntVar)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint count(IntVar VALUE, IntVar[] VARS, IntVar LIMIT) {
        return VARS[0].getSolver().count(VALUE,VARS,LIMIT);
    }

    /**
     * @deprecated : use {@link Solver#cumulative(Task[], IntVar[], IntVar)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint cumulative(Task[] TASKS, IntVar[] HEIGHTS, IntVar CAPACITY) {
        return cumulative(TASKS, HEIGHTS, CAPACITY, TASKS.length > 500);
    }

    /**
     * @deprecated : use {@link Solver#cumulative(Task[], IntVar[], IntVar, boolean)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint cumulative(Task[] TASKS, IntVar[] HEIGHTS, IntVar CAPACITY, boolean INCREMENTAL) {
        return CAPACITY.getSolver().cumulative(TASKS,HEIGHTS,CAPACITY,INCREMENTAL);
    }

    /**
     * @deprecated : use {@link Solver#diffN(IntVar[], IntVar[], IntVar[], IntVar[], boolean)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint diffn(IntVar[] X, IntVar[] Y, IntVar[] WIDTH, IntVar[] HEIGHT, boolean USE_CUMUL) {
        return X[0].getSolver().diffN(X,Y,WIDTH,HEIGHT,USE_CUMUL);
    }

    /**
     * @deprecated : use {@link Solver#element(IntVar, IntVar[], IntVar, int)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint element(IntVar VALUE, IntVar[] TABLE, IntVar INDEX, int OFFSET) {
        return VALUE.getSolver().element(VALUE,TABLE,INDEX,OFFSET);
    }

    /**
     * @deprecated : use {@link Solver#globalCardinality(IntVar[], int[], IntVar[], boolean)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint global_cardinality(IntVar[] VARS, int[] VALUES, IntVar[] OCCURRENCES, boolean CLOSED) {
        return VARS[0].getSolver().globalCardinality(VARS,VALUES,OCCURRENCES,CLOSED);
    }

    /**
     * @deprecated : use {@link Solver#inverseChanneling(IntVar[], IntVar[])} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint inverse_channeling(IntVar[] VARS1, IntVar[] VARS2) {
        return inverse_channeling(VARS1,VARS2,0,0);
    }

    /**
     * @deprecated : use {@link Solver#inverseChanneling(IntVar[], IntVar[], int, int)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint inverse_channeling(IntVar[] VARS1, IntVar[] VARS2, int OFFSET1, int OFFSET2) {
        return VARS1[0].getSolver().inverseChanneling(VARS1,VARS2,OFFSET1,OFFSET2);
    }

    /**
     * @deprecated : use {@link Solver#intValuePrecedeChain(IntVar[], int, int)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint int_value_precede_chain(IntVar[] X, int S, int T) {
        return X[0].getSolver().intValuePrecedeChain(X,S,T);
    }

    /**
     * @deprecated : use {@link Solver#intValuePrecedeChain(IntVar[], int[])} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint int_value_precede_chain(IntVar[] X, int[] V) {
        return X[0].getSolver().intValuePrecedeChain(X,V);
    }

    /**
     * @deprecated : use {@link Solver#knapsack(IntVar[], IntVar, IntVar, int[], int[])} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint knapsack(IntVar[] OCCURRENCES, IntVar SUM_WEIGHT, IntVar SUM_ENERGY,
                                      int[] WEIGHT, int[] ENERGY) {
        return SUM_ENERGY.getSolver().knapsack(OCCURRENCES,SUM_WEIGHT,SUM_ENERGY,WEIGHT,ENERGY);
    }

    /**
     * @deprecated : use {@link Solver#keySort(IntVar[][], IntVar[], IntVar[][], int)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint keysorting(IntVar[][] VARS, IntVar[] PERMVARS, IntVar[][] SORTEDVARS, int K) {
        return PERMVARS[0].getSolver().keySort(VARS,PERMVARS,SORTEDVARS,K);
    }

    /**
     * @deprecated : use {@link Solver#lexChainLess(IntVar[]...)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint lex_chain_less(IntVar[]... VARS) {
        return VARS[0][0].getSolver().lexChainLess(VARS);
    }

    /**
     * @deprecated : use {@link Solver#lexChainLessEq(IntVar[]...)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint lex_chain_less_eq(IntVar[]... VARS) {
        return VARS[0][0].getSolver().lexChainLessEq(VARS);
    }

    /**
     * @deprecated : use {@link Solver#lexLess(IntVar[], IntVar[])} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint lex_less(IntVar[] VARS1, IntVar[] VARS2) {
        return VARS1[0].getSolver().lexLess(VARS1,VARS2);
    }

    /**
     * @deprecated : use {@link Solver#lexLessEq(IntVar[], IntVar[])} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint lex_less_eq(IntVar[] VARS1, IntVar[] VARS2) {
        return VARS1[0].getSolver().lexLessEq(VARS1,VARS2);
    }

    /**
     * @deprecated : use {@link Solver#max(IntVar, IntVar[])} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint maximum(IntVar MAX, IntVar[] VARS) {
        return VARS[0].getSolver().max(MAX,VARS);
    }

    /**
     * @deprecated : use {@link Solver#max(BoolVar, BoolVar[])} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint maximum(BoolVar MAX, BoolVar[] VARS) {
        return VARS[0].getSolver().max(MAX,VARS);
    }

    /**
     * @deprecated : use {@link Solver#mddc(IntVar[], MultivaluedDecisionDiagram)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint mddc(IntVar[] VARS, MultivaluedDecisionDiagram MDD) {
        return VARS[0].getSolver().mddc(VARS,MDD);
    }

    /**
     * @deprecated : use {@link Solver#min(IntVar, IntVar[])} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint minimum(IntVar MIN, IntVar[] VARS) {
        return VARS[0].getSolver().min(MIN,VARS);
    }

    /**
     * @deprecated : use {@link Solver#min(BoolVar, BoolVar[])} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint minimum(BoolVar MIN, BoolVar[] VARS) {
        return VARS[0].getSolver().min(MIN,VARS);
    }

    /**
     * @deprecated : use {@link Solver#multiCostRegular(IntVar[], IntVar[], ICostAutomaton)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint multicost_regular(IntVar[] VARS, IntVar[] CVARS, ICostAutomaton CAUTOMATON) {
        return VARS[0].getSolver().multiCostRegular(VARS,CVARS,CAUTOMATON);
    }

    /**
     * @deprecated : use {@link Solver#nValues(IntVar[], IntVar)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint nvalues(IntVar[] VARS, IntVar NVALUES) {
        return VARS[0].getSolver().nValues(VARS,NVALUES);
    }

    /**
     * @deprecated : use {@link Solver#path(IntVar[], IntVar, IntVar)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint path(IntVar[] VARS, IntVar START, IntVar END) {
        return path(VARS,START,END,0);
    }

    /**
     * @deprecated : use {@link Solver#path(IntVar[], IntVar, IntVar, int)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint path(IntVar[] VARS, IntVar START, IntVar END, int OFFSET) {
        return VARS[0].getSolver().path(VARS,START,END,OFFSET);
    }

    /**
     * @deprecated : use {@link Solver#regular(IntVar[], IAutomaton)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint regular(IntVar[] VARS, IAutomaton AUTOMATON) {
        return VARS[0].getSolver().regular(VARS,AUTOMATON);
    }

    /**
     * @deprecated : use {@link Solver#scalar(IntVar[], int[], String, IntVar)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint scalar(IntVar[] VARS, int[] COEFFS, IntVar SCALAR) {
        return scalar(VARS, COEFFS, "=", SCALAR);
    }

    /**
     * @deprecated : use {@link Solver#scalar(IntVar[], int[], String, int)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint scalar(IntVar[] VARS, int[] COEFFS, String OPERATOR, int SCALAR) {
        return VARS[0].getSolver().scalar(VARS,COEFFS,OPERATOR,SCALAR);
    }

    /**
     * @deprecated : use {@link Solver#scalar(IntVar[], int[], String, IntVar)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint scalar(IntVar[] VARS, int[] COEFFS, String OPERATOR, IntVar SCALAR) {
        return VARS[0].getSolver().scalar(VARS,COEFFS,OPERATOR,SCALAR);
    }

    /**
     * @deprecated : use {@link Solver#sort(IntVar[], IntVar[])} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint sort(IntVar[] VARS, IntVar[] SORTEDVARS) {
        return VARS[0].getSolver().sort(VARS,SORTEDVARS);
    }

    /**
     * @deprecated : use {@link Solver#subCircuit(IntVar[], int, IntVar)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint subcircuit(IntVar[] VARS, int OFFSET, IntVar SUBCIRCUIT_SIZE) {
        return VARS[0].getSolver().subCircuit(VARS,OFFSET,SUBCIRCUIT_SIZE);
    }

    /**
     * @deprecated : use {@link Solver#subPath(IntVar[], IntVar, IntVar, int, IntVar)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint subpath(IntVar[] VARS, IntVar START, IntVar END, int OFFSET, IntVar SIZE) {
        return END.getSolver().subPath(VARS,START,END,OFFSET,SIZE);
    }

    /**
     * @deprecated : use {@link Solver#sum(IntVar[], String, int)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint sum(IntVar[] VARS, IntVar SUM) {
        return sum(VARS, "=", SUM);
    }

    /**
     * @deprecated : use {@link Solver#sum(IntVar[], String, int)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint sum(IntVar[] VARS, String OPERATOR, int SUM) {
        return VARS[0].getSolver().sum(VARS,OPERATOR,SUM);
    }

    /**
     * @deprecated : use {@link Solver#sum(IntVar[], String, IntVar)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint sum(IntVar[] VARS, String OPERATOR, IntVar SUM) {
        return SUM.getSolver().sum(VARS,OPERATOR,SUM);
    }

    /**
     * @deprecated : use {@link Solver#sum(BoolVar[], String, IntVar)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint sum(BoolVar[] VARS, IntVar SUM) {
        return sum(VARS,"=",SUM);
    }

    /**
     * @deprecated : use {@link Solver#sum(BoolVar[], String, int)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint sum(BoolVar[] VARS, String OPERATOR, int SUM) {
        return VARS[0].getSolver().sum(VARS,OPERATOR,SUM);
    }

    /**
     * @deprecated : use {@link Solver#sum(BoolVar[], String, IntVar)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint sum(BoolVar[] VARS, String OPERATOR, IntVar SUM) {
        return SUM.getSolver().sum(VARS,OPERATOR,SUM);
    }

    /**
     * @deprecated : use {@link Solver#table(IntVar, IntVar, Tuples)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint table(IntVar[] VARS, Tuples TUPLES) {
        return table(VARS,TUPLES,TUPLES.isFeasible()?"GACSTR+":"GAC3rm");
    }

    /**
     * @deprecated : use {@link Solver#table(IntVar, IntVar, Tuples, String)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint table(IntVar[] VARS, Tuples TUPLES, String ALGORITHM) {
        return VARS[0].getSolver().table(VARS,TUPLES,ALGORITHM);
    }

    /**
     * @deprecated : use {@link Solver#tree(IntVar[], IntVar)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint tree(IntVar[] SUCCS, IntVar NBTREES) {
        return tree(SUCCS, NBTREES, 0);
    }

    /**
     * @deprecated : use {@link Solver#tree(IntVar[], IntVar, int)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint tree(IntVar[] SUCCS, IntVar NBTREES, int OFFSET) {
        return NBTREES.getSolver().tree(SUCCS,NBTREES,OFFSET);
    }



    /**
     * @deprecated : will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint[] tsp(IntVar[] SUCCS, IntVar COST, int[][] COST_MATRIX) {
        int n = SUCCS.length;
        assert n > 1;
        assert n == COST_MATRIX.length && n == COST_MATRIX[0].length;
        IntVar[] costOf = new IntVar[n];
        Solver s = COST.getSolver();
        for (int i = 0; i < n; i++) {
            costOf[i] = s.intVar("costOf(" + i + ")", COST_MATRIX[i]);
        }
        Constraint[] model = new Constraint[n + 2];
        for (int i = 0; i < n; i++) {
            model[i] = s.element(costOf[i], COST_MATRIX[i], SUCCS[i]);
        }
        model[n] = s.sum(costOf, "=", COST);
        model[n + 1] = s.circuit(SUCCS);
        return model;
    }

    /**
     * @deprecated : use {@link Solver#getDomainUnion(IntVar...)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static TIntArrayList getDomainUnion(IntVar[] vars) {
        return vars[0].getSolver().getDomainUnion(vars);
    }

    // ###################

    /**
     * @deprecated : use {@link TuplesFactory#canBeTupled(IntVar...)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static boolean tupleIt(IntVar... VARS) {
        return TuplesFactory.canBeTupled(VARS);
    }
}
