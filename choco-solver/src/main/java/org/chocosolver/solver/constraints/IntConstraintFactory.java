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
import org.chocosolver.solver.Model;
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
 * @deprecated : int constraint creation should be done through the {@link Model} object
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
     * @deprecated : use {@link Model#TRUE()} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint TRUE(Model model) {
        return model.TRUE();
    }

    /**
     * @deprecated : use {@link Model#FALSE()} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint FALSE(Model model) {
        return model.FALSE();
    }

    //##################################################################################################################
    // UNARIES #########################################################################################################
    //##################################################################################################################

    /**
     * @deprecated : use {@link Model#arithm(IntVar, String, int)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint arithm(IntVar VAR, String OP, int CSTE) {
        return VAR.getModel().arithm(VAR,OP,CSTE);
    }

    /**
     * @deprecated : use {@link Model#member(IntVar, int[])} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint member(IntVar VAR, int[] TABLE) {
        return VAR.getModel().member(VAR,TABLE);
    }

    /**
     * @deprecated : use {@link Model#member(IntVar, int, int)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint member(IntVar VAR, int LB, int UB) {
        return VAR.getModel().member(VAR,LB,UB);
    }

    /**
     * @deprecated : use {@link Model#notMember(IntVar, int[])} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint not_member(IntVar VAR, int[] TABLE) {
        return VAR.getModel().notMember(VAR,TABLE);
    }

    /**
     * @deprecated : use {@link Model#notMember(IntVar, int, int)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint not_member(IntVar VAR, int LB, int UB) {
        return VAR.getModel().notMember(VAR,LB,UB);
    }

    //##################################################################################################################
    //BINARIES #########################################################################################################
    //##################################################################################################################

    /**
     * @deprecated : use {@link Model#absolute(IntVar, IntVar)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint absolute(IntVar VAR1, IntVar VAR2) {
        return VAR1.getModel().absolute(VAR1,VAR2);
    }

    /**
     * @deprecated : use {@link Model#arithm(IntVar, String, IntVar)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint arithm(IntVar VAR1, String OP, IntVar VAR2) {
        return VAR1.getModel().arithm(VAR1,OP,VAR2);
    }

    /**
     * @deprecated : use {@link Model#arithm(IntVar, String, IntVar, String, int)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint arithm(IntVar VAR1, String OP1, IntVar VAR2, String OP2, int CSTE) {
        return VAR1.getModel().arithm(VAR1,OP1,VAR2,OP2,CSTE);
    }

    /**
     * @deprecated : use {@link Model#distance(IntVar, IntVar, String, int)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint distance(IntVar VAR1, IntVar VAR2, String OP, int CSTE) {
        return VAR1.getModel().distance(VAR1,VAR2,OP,CSTE);
    }

    /**
     * @deprecated : use {@link Model#element(IntVar, int[], IntVar, int)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint element(IntVar VALUE, int[] TABLE, IntVar INDEX, int OFFSET) {
        return VALUE.getModel().element(VALUE, TABLE, INDEX, OFFSET);
    }

    /**
     * @deprecated : use {@link Model#element(IntVar, int[], IntVar)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint element(IntVar VALUE, int[] TABLE, IntVar INDEX) {
        return VALUE.getModel().element(VALUE,TABLE,INDEX);
    }

    /**
     * @deprecated : use {@link Model#square(IntVar, IntVar)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint square(IntVar VAR1, IntVar VAR2) {
        return VAR1.getModel().square(VAR1,VAR2);
    }

    /**
     * @deprecated : use {@link Model#table(IntVar, IntVar, Tuples)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint table(IntVar VAR1, IntVar VAR2, Tuples TUPLES) {
        return table(VAR1,VAR2,TUPLES,"AC3rm");
    }

    /**
     * @deprecated : use {@link Model#table(IntVar, IntVar, Tuples, String)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint table(IntVar VAR1, IntVar VAR2, Tuples TUPLES, String ALGORITHM) {
        return VAR1.getModel().table(VAR1,VAR2,TUPLES,ALGORITHM);
    }

    //##################################################################################################################
    //TERNARIES ########################################################################################################
    //##################################################################################################################

    /**
     * @deprecated : use {@link Model#arithm(IntVar, String, IntVar, String, IntVar)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint arithm(IntVar VAR1, String OP1, IntVar VAR2, String OP2, IntVar VAR3) {
        return VAR1.getModel().arithm(VAR1,OP1,VAR2,OP2,VAR3);
    }

    /**
     * @deprecated : use {@link Model#distance(IntVar, IntVar, String, IntVar)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint distance(IntVar VAR1, IntVar VAR2, String OP, IntVar VAR3) {
        return VAR1.getModel().distance(VAR1,VAR2,OP,VAR3);
    }

    /**
     * @deprecated : use {@link Model#div(IntVar, IntVar, IntVar)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint eucl_div(IntVar DIVIDEND, IntVar DIVISOR, IntVar RESULT) {
        return RESULT.getModel().div(DIVIDEND,DIVISOR,RESULT);
    }

    /**
     * @deprecated : use {@link Model#max(IntVar, IntVar, IntVar)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint maximum(IntVar MAX, IntVar VAR1, IntVar VAR2) {
        return MAX.getModel().max(MAX,VAR1,VAR2);
    }

    /**
     * @deprecated : use {@link Model#min(IntVar, IntVar, IntVar)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint minimum(IntVar MIN, IntVar VAR1, IntVar VAR2) {
        return MIN.getModel().min(MIN,VAR1,VAR2);
    }

    /**
     * @deprecated : use {@link Model#mod(IntVar, IntVar, IntVar)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint mod(IntVar X, IntVar Y, IntVar Z) {
        return X.getModel().mod(X,Y,Z);
    }

    /**
     * @deprecated : use {@link Model#times(IntVar, IntVar, IntVar)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    @SuppressWarnings("SuspiciousNameCombination")
    public static Constraint times(IntVar X, IntVar Y, IntVar Z) {
        return X.getModel().times(X,Y,Z);
    }

    /**
     * @deprecated : use {@link Model#times(IntVar, int, IntVar)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint times(IntVar X, int Y, IntVar Z) {
        return X.getModel().times(X,Y,Z);
    }

    //##################################################################################################################
    //GLOBALS ##########################################################################################################
    //##################################################################################################################

    /**
     * @deprecated : use {@link Model#allDifferent(IntVar[])} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint alldifferent(IntVar[] VARS) {
        return alldifferent(VARS, "DEFAULT");
    }

    /**
     * @deprecated : use {@link Model#allDifferent(IntVar[], String)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint alldifferent(IntVar[] VARS, String CONSISTENCY) {
        return VARS[0].getModel().allDifferent(VARS,CONSISTENCY);
    }

    /**
     * @deprecated : use {@link Model#allDifferentUnderCondition(IntVar[], Condition, boolean)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint alldifferent_conditionnal(IntVar[] VARS, Condition CONDITION, boolean AC) {
        return VARS[0].getModel().allDifferentUnderCondition(VARS,CONDITION,AC);
    }

    /**
     * @deprecated : use {@link Model#allDifferentUnderCondition(IntVar[], Condition)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint alldifferent_conditionnal(IntVar[] VARS, Condition CONDITION) {
        return alldifferent_conditionnal(VARS, CONDITION, false);
    }

    /**
     * @deprecated : use {@link Model#allDifferentExcept0(IntVar[])} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint alldifferent_except_0(IntVar[] VARS) {
        return alldifferent_conditionnal(VARS, Condition.EXCEPT_0);
    }

    /**
     * @deprecated : use {@link Model#among(IntVar, IntVar[], int[])} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint among(IntVar NVAR, IntVar[] VARS, int[] VALUES) {
        return VARS[0].getModel().among(NVAR,VARS,VALUES);
    }

    /**
     * @deprecated : use {@link Model#atLeastNValues(IntVar[], IntVar, boolean)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint atleast_nvalues(IntVar[] VARS, IntVar NVALUES, boolean AC) {
        return VARS[0].getModel().atLeastNValues(VARS,NVALUES,AC);
    }

    /**
     * @deprecated : use {@link Model#atMostNVvalues(IntVar[], IntVar, boolean)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint atmost_nvalues(IntVar[] VARS, IntVar NVALUES, boolean STRONG) {
        return VARS[0].getModel().atMostNVvalues(VARS,NVALUES,STRONG);
    }

    /**
     * @deprecated : use {@link Model#binPacking(IntVar[], int[], IntVar[], int)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint bin_packing(IntVar[] ITEM_BIN, int[] ITEM_SIZE, IntVar[] BIN_LOAD, int OFFSET) {
        return ITEM_BIN[0].getModel().binPacking(ITEM_BIN,ITEM_SIZE,BIN_LOAD,OFFSET);
    }

    /**
     * @deprecated : use {@link Model#boolsIntChanneling(BoolVar[], IntVar, int)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint boolean_channeling(BoolVar[] BVARS, IntVar VAR, int OFFSET) {
        return VAR.getModel().boolsIntChanneling(BVARS,VAR,OFFSET);
    }

    /**
     * @deprecated : use {@link Model#bitsIntChanneling(BoolVar[], IntVar)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint bit_channeling(BoolVar[] BITS, IntVar VAR) {
        return VAR.getModel().bitsIntChanneling(BITS,VAR);
    }

    /**
     * @deprecated : use {@link Model#clausesIntChanneling(IntVar, BoolVar[], BoolVar[])} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint clause_channeling(IntVar VAR, BoolVar[] EVARS, BoolVar[] LVARS) {
        return VAR.getModel().clausesIntChanneling(VAR,EVARS,LVARS);
    }

    /**
     * @deprecated : use {@link Model#circuit(IntVar[], int, CircuitConf)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint circuit(IntVar[] VARS, int OFFSET, CircuitConf CONF) {
        return VARS[0].getModel().circuit(VARS,OFFSET,CONF);
    }

    /**
     * @deprecated : use {@link Model#circuit(IntVar[], int)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint circuit(IntVar[] VARS, int OFFSET) {
        return circuit(VARS, OFFSET, CircuitConf.RD);
    }

    /**
     * @deprecated : use {@link Model#costRegular(IntVar[], IntVar, ICostAutomaton)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint cost_regular(IntVar[] VARS, IntVar COST, ICostAutomaton CAUTOMATON) {
        return VARS[0].getModel().costRegular(VARS,COST,CAUTOMATON);
    }

    /**
     * @deprecated : use {@link Model#count(int, IntVar[], IntVar)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint count(int VALUE, IntVar[] VARS, IntVar LIMIT) {
        return VARS[0].getModel().count(VALUE,VARS,LIMIT);
    }

    /**
     * @deprecated : use {@link Model#count(IntVar, IntVar[], IntVar)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint count(IntVar VALUE, IntVar[] VARS, IntVar LIMIT) {
        return VARS[0].getModel().count(VALUE,VARS,LIMIT);
    }

    /**
     * @deprecated : use {@link Model#cumulative(Task[], IntVar[], IntVar)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint cumulative(Task[] TASKS, IntVar[] HEIGHTS, IntVar CAPACITY) {
        return cumulative(TASKS, HEIGHTS, CAPACITY, TASKS.length > 500);
    }

    /**
     * @deprecated : use {@link Model#cumulative(Task[], IntVar[], IntVar, boolean)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint cumulative(Task[] TASKS, IntVar[] HEIGHTS, IntVar CAPACITY, boolean INCREMENTAL) {
        return CAPACITY.getModel().cumulative(TASKS,HEIGHTS,CAPACITY,INCREMENTAL);
    }

    /**
     * @deprecated : use {@link Model#diffN(IntVar[], IntVar[], IntVar[], IntVar[], boolean)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint diffn(IntVar[] X, IntVar[] Y, IntVar[] WIDTH, IntVar[] HEIGHT, boolean USE_CUMUL) {
        return X[0].getModel().diffN(X,Y,WIDTH,HEIGHT,USE_CUMUL);
    }

    /**
     * @deprecated : use {@link Model#element(IntVar, IntVar[], IntVar, int)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint element(IntVar VALUE, IntVar[] TABLE, IntVar INDEX, int OFFSET) {
        return VALUE.getModel().element(VALUE,TABLE,INDEX,OFFSET);
    }

    /**
     * @deprecated : use {@link Model#globalCardinality(IntVar[], int[], IntVar[], boolean)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint global_cardinality(IntVar[] VARS, int[] VALUES, IntVar[] OCCURRENCES, boolean CLOSED) {
        return VARS[0].getModel().globalCardinality(VARS,VALUES,OCCURRENCES,CLOSED);
    }

    /**
     * @deprecated : use {@link Model#inverseChanneling(IntVar[], IntVar[])} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint inverse_channeling(IntVar[] VARS1, IntVar[] VARS2) {
        return inverse_channeling(VARS1,VARS2,0,0);
    }

    /**
     * @deprecated : use {@link Model#inverseChanneling(IntVar[], IntVar[], int, int)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint inverse_channeling(IntVar[] VARS1, IntVar[] VARS2, int OFFSET1, int OFFSET2) {
        return VARS1[0].getModel().inverseChanneling(VARS1,VARS2,OFFSET1,OFFSET2);
    }

    /**
     * @deprecated : use {@link Model#intValuePrecedeChain(IntVar[], int, int)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint int_value_precede_chain(IntVar[] X, int S, int T) {
        return X[0].getModel().intValuePrecedeChain(X,S,T);
    }

    /**
     * @deprecated : use {@link Model#intValuePrecedeChain(IntVar[], int[])} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint int_value_precede_chain(IntVar[] X, int[] V) {
        return X[0].getModel().intValuePrecedeChain(X,V);
    }

    /**
     * @deprecated : use {@link Model#knapsack(IntVar[], IntVar, IntVar, int[], int[])} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint knapsack(IntVar[] OCCURRENCES, IntVar SUM_WEIGHT, IntVar SUM_ENERGY,
                                      int[] WEIGHT, int[] ENERGY) {
        return SUM_ENERGY.getModel().knapsack(OCCURRENCES,SUM_WEIGHT,SUM_ENERGY,WEIGHT,ENERGY);
    }

    /**
     * @deprecated : use {@link Model#keySort(IntVar[][], IntVar[], IntVar[][], int)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint keysorting(IntVar[][] VARS, IntVar[] PERMVARS, IntVar[][] SORTEDVARS, int K) {
        return PERMVARS[0].getModel().keySort(VARS,PERMVARS,SORTEDVARS,K);
    }

    /**
     * @deprecated : use {@link Model#lexChainLess(IntVar[]...)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint lex_chain_less(IntVar[]... VARS) {
        return VARS[0][0].getModel().lexChainLess(VARS);
    }

    /**
     * @deprecated : use {@link Model#lexChainLessEq(IntVar[]...)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint lex_chain_less_eq(IntVar[]... VARS) {
        return VARS[0][0].getModel().lexChainLessEq(VARS);
    }

    /**
     * @deprecated : use {@link Model#lexLess(IntVar[], IntVar[])} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint lex_less(IntVar[] VARS1, IntVar[] VARS2) {
        return VARS1[0].getModel().lexLess(VARS1,VARS2);
    }

    /**
     * @deprecated : use {@link Model#lexLessEq(IntVar[], IntVar[])} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint lex_less_eq(IntVar[] VARS1, IntVar[] VARS2) {
        return VARS1[0].getModel().lexLessEq(VARS1,VARS2);
    }

    /**
     * @deprecated : use {@link Model#max(IntVar, IntVar[])} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint maximum(IntVar MAX, IntVar[] VARS) {
        return VARS[0].getModel().max(MAX,VARS);
    }

    /**
     * @deprecated : use {@link Model#max(BoolVar, BoolVar[])} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint maximum(BoolVar MAX, BoolVar[] VARS) {
        return VARS[0].getModel().max(MAX,VARS);
    }

    /**
     * @deprecated : use {@link Model#mddc(IntVar[], MultivaluedDecisionDiagram)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint mddc(IntVar[] VARS, MultivaluedDecisionDiagram MDD) {
        return VARS[0].getModel().mddc(VARS,MDD);
    }

    /**
     * @deprecated : use {@link Model#min(IntVar, IntVar[])} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint minimum(IntVar MIN, IntVar[] VARS) {
        return VARS[0].getModel().min(MIN,VARS);
    }

    /**
     * @deprecated : use {@link Model#min(BoolVar, BoolVar[])} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint minimum(BoolVar MIN, BoolVar[] VARS) {
        return VARS[0].getModel().min(MIN,VARS);
    }

    /**
     * @deprecated : use {@link Model#multiCostRegular(IntVar[], IntVar[], ICostAutomaton)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint multicost_regular(IntVar[] VARS, IntVar[] CVARS, ICostAutomaton CAUTOMATON) {
        return VARS[0].getModel().multiCostRegular(VARS,CVARS,CAUTOMATON);
    }

    /**
     * @deprecated : use {@link Model#nValues(IntVar[], IntVar)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint nvalues(IntVar[] VARS, IntVar NVALUES) {
        return VARS[0].getModel().nValues(VARS,NVALUES);
    }

    /**
     * @deprecated : use {@link Model#path(IntVar[], IntVar, IntVar)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint path(IntVar[] VARS, IntVar START, IntVar END) {
        return path(VARS,START,END,0);
    }

    /**
     * @deprecated : use {@link Model#path(IntVar[], IntVar, IntVar, int)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint path(IntVar[] VARS, IntVar START, IntVar END, int OFFSET) {
        return VARS[0].getModel().path(VARS,START,END,OFFSET);
    }

    /**
     * @deprecated : use {@link Model#regular(IntVar[], IAutomaton)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint regular(IntVar[] VARS, IAutomaton AUTOMATON) {
        return VARS[0].getModel().regular(VARS,AUTOMATON);
    }

    /**
     * @deprecated : use {@link Model#scalar(IntVar[], int[], String, IntVar)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint scalar(IntVar[] VARS, int[] COEFFS, IntVar SCALAR) {
        return scalar(VARS, COEFFS, "=", SCALAR);
    }

    /**
     * @deprecated : use {@link Model#scalar(IntVar[], int[], String, int)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint scalar(IntVar[] VARS, int[] COEFFS, String OPERATOR, int SCALAR) {
        return VARS[0].getModel().scalar(VARS,COEFFS,OPERATOR,SCALAR);
    }

    /**
     * @deprecated : use {@link Model#scalar(IntVar[], int[], String, IntVar)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint scalar(IntVar[] VARS, int[] COEFFS, String OPERATOR, IntVar SCALAR) {
        return VARS[0].getModel().scalar(VARS,COEFFS,OPERATOR,SCALAR);
    }

    /**
     * @deprecated : use {@link Model#sort(IntVar[], IntVar[])} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint sort(IntVar[] VARS, IntVar[] SORTEDVARS) {
        return VARS[0].getModel().sort(VARS,SORTEDVARS);
    }

    /**
     * @deprecated : use {@link Model#subCircuit(IntVar[], int, IntVar)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint subcircuit(IntVar[] VARS, int OFFSET, IntVar SUBCIRCUIT_SIZE) {
        return VARS[0].getModel().subCircuit(VARS,OFFSET,SUBCIRCUIT_SIZE);
    }

    /**
     * @deprecated : use {@link Model#subPath(IntVar[], IntVar, IntVar, int, IntVar)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint subpath(IntVar[] VARS, IntVar START, IntVar END, int OFFSET, IntVar SIZE) {
        return END.getModel().subPath(VARS,START,END,OFFSET,SIZE);
    }

    /**
     * @deprecated : use {@link Model#sum(IntVar[], String, int)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint sum(IntVar[] VARS, IntVar SUM) {
        return sum(VARS, "=", SUM);
    }

    /**
     * @deprecated : use {@link Model#sum(IntVar[], String, int)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint sum(IntVar[] VARS, String OPERATOR, int SUM) {
        return VARS[0].getModel().sum(VARS,OPERATOR,SUM);
    }

    /**
     * @deprecated : use {@link Model#sum(IntVar[], String, IntVar)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint sum(IntVar[] VARS, String OPERATOR, IntVar SUM) {
        return SUM.getModel().sum(VARS,OPERATOR,SUM);
    }

    /**
     * @deprecated : use {@link Model#sum(BoolVar[], String, IntVar)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint sum(BoolVar[] VARS, IntVar SUM) {
        return sum(VARS,"=",SUM);
    }

    /**
     * @deprecated : use {@link Model#sum(BoolVar[], String, int)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint sum(BoolVar[] VARS, String OPERATOR, int SUM) {
        return VARS[0].getModel().sum(VARS,OPERATOR,SUM);
    }

    /**
     * @deprecated : use {@link Model#sum(BoolVar[], String, IntVar)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint sum(BoolVar[] VARS, String OPERATOR, IntVar SUM) {
        return SUM.getModel().sum(VARS,OPERATOR,SUM);
    }

    /**
     * @deprecated : use {@link Model#table(IntVar, IntVar, Tuples)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint table(IntVar[] VARS, Tuples TUPLES) {
        return table(VARS,TUPLES,TUPLES.isFeasible()?"GACSTR+":"GAC3rm");
    }

    /**
     * @deprecated : use {@link Model#table(IntVar, IntVar, Tuples, String)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint table(IntVar[] VARS, Tuples TUPLES, String ALGORITHM) {
        return VARS[0].getModel().table(VARS,TUPLES,ALGORITHM);
    }

    /**
     * @deprecated : use {@link Model#tree(IntVar[], IntVar)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint tree(IntVar[] SUCCS, IntVar NBTREES) {
        return tree(SUCCS, NBTREES, 0);
    }

    /**
     * @deprecated : use {@link Model#tree(IntVar[], IntVar, int)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Constraint tree(IntVar[] SUCCS, IntVar NBTREES, int OFFSET) {
        return NBTREES.getModel().tree(SUCCS,NBTREES,OFFSET);
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
        Model s = COST.getModel();
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
     * @deprecated : use {@link Model#getDomainUnion(IntVar...)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static TIntArrayList getDomainUnion(IntVar[] vars) {
        return vars[0].getModel().getDomainUnion(vars);
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
