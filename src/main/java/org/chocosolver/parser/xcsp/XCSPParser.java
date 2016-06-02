/**
 * Copyright (c) 2016, Ecole des Mines de Nantes
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 * must display the following acknowledgement:
 * This product includes software developed by the <organization>.
 * 4. Neither the name of the <organization> nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * <p>
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
package org.chocosolver.parser.xcsp;

import gnu.trove.map.hash.TObjectIntHashMap;
import org.chocosolver.parser.ParserException;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.extension.Tuples;
import org.chocosolver.solver.constraints.nary.automata.FA.FiniteAutomaton;
import org.chocosolver.solver.expression.discrete.arithmetic.ArExpression;
import org.chocosolver.solver.expression.discrete.relational.ReExpression;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.tools.ArrayUtils;
import org.chocosolver.util.tools.VariableUtils;
import org.xcsp.parser.XCallbacks2;
import org.xcsp.parser.XEnums;
import org.xcsp.parser.XParser;
import org.xcsp.parser.XVariables;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

/**
 * <p>
 * Project: choco-parsers.
 *
 * @author Charles Prud'homme
 * @since 01/06/2016.
 */
public class XCSPParser implements XCallbacks2 {

    /**
     * Mapping between XCSP vars and Choco vars
     */
    protected HashMap<XVariables.XVarInteger, IntVar> mvars;

    /**
     * The model to feed
     */
    Model model;

    public void model(Model model, String instance) throws Exception {
        this.model = model;
        this.mvars = new HashMap<>();
        loadInstance(instance);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////// VARIABLES //////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void buildVarInteger(XVariables.XVarInteger x, int minValue, int maxValue) {
        mvars.put(x, model.intVar(x.id, minValue, maxValue));
    }

    @Override
    public void buildVarInteger(XVariables.XVarInteger x, int[] values) {
        mvars.put(x, model.intVar(x.id, values));
    }

    private IntVar var(XVariables.XVarInteger var) {
        return mvars.get(var);
    }

    private IntVar[] vars(XVariables.XVarInteger[] vars) {
        return Arrays.stream(vars).map(v -> var(v)).toArray(IntVar[]::new);
    }

    private IntVar[][] vars(XVariables.XVarInteger[][] vars) {
        return Arrays.stream(vars).map(v -> vars(v)).toArray(IntVar[][]::new);
    }

    private BoolVar bool(XVariables.XVarInteger var) {
        return (BoolVar) mvars.get(var);
    }

    private BoolVar[] bools(XVariables.XVarInteger[] vars) {
        return Arrays.stream(vars).map(v -> bool(v)).toArray(BoolVar[]::new);
    }

    private BoolVar[][] bools(XVariables.XVarInteger[][] vars) {
        return Arrays.stream(vars).map(v -> bools(v)).toArray(BoolVar[][]::new);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////// EXTENSION CONSTRAINTS ////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void buildCtrExtension(String id, XVariables.XVarInteger[] list, int[][] tuples, boolean positive, Set<XEnums.TypeFlag> flags) {
        if (flags.contains(XEnums.TypeFlag.STARRED_TUPLES)) {
            // can you manage tables with symbol * ?
            throw new ParserException("Tables with symbol * are not supported");
        }
        if (flags.contains(XEnums.TypeFlag.UNCLEAN_TUPLES)) {
            // do you have to clean the tuples, so as to remove those that cannot be built from variable domains ?
        }
        model.table(vars(list), new Tuples(tuples, positive)).post();
    }

    @Override
    public void buildCtrExtension(String id, XVariables.XVarInteger x, int[] values, boolean positive, Set<XEnums.TypeFlag> flags) {
        if (flags.contains(XEnums.TypeFlag.STARRED_TUPLES)) {
            // can you manage tables with symbol * ?
            throw new ParserException("Tables with symbol * are not supported");
        }
        if (flags.contains(XEnums.TypeFlag.UNCLEAN_TUPLES)) {
            // do you have to clean the tuples, so as to remove those that cannot be built from variable domains ?
        }
        model.member(var(x), values).post();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////// PRIMITIVE CONSTRAINTS ////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static ReExpression rel(ArExpression a, XEnums.TypeConditionOperatorRel op, int k) {
        ReExpression e = null;
        switch (op) {
            case LT:
                e = a.lt(k);
                break;
            case LE:
                e = a.le(k);
                break;
            case GE:
                e = a.ge(k);
                break;
            case GT:
                e = a.gt(k);
                break;
            case NE:
                e = a.ne(k);
                break;
            case EQ:
                e = a.eq(k);
                break;
        }
        return e;
    }

    private static ReExpression rel(ArExpression a, XEnums.TypeConditionOperatorRel op, IntVar k) {
        ReExpression e = null;
        switch (op) {
            case LT:
                e = a.lt(k);
                break;
            case LE:
                e = a.le(k);
                break;
            case GE:
                e = a.ge(k);
                break;
            case GT:
                e = a.gt(k);
                break;
            case NE:
                e = a.ne(k);
                break;
            case EQ:
                e = a.eq(k);
                break;
        }
        return e;
    }

    private static ArExpression ari(ArExpression a, XEnums.TypeArithmeticOperator opa, ArExpression b) {
        ArExpression e = null;
        switch (opa) {
            case ADD:
                e = a.add(b);
                break;
            case SUB:
                e = a.sub(b);
                break;
            case MUL:
                e = a.mul(b);
                break;
            case DIV:
                e = a.div(b);
                break;
            case MOD:
                e = a.mod(b);
                break;
            case DIST:
                e = a.dist(b);
                break;
        }
        return e;
    }

    @Override
    public void buildCtrPrimitive(String id, XVariables.XVarInteger x, XEnums.TypeConditionOperatorRel op, int k) {
        rel(var(x), op, k).post();
    }

    @Override
    public void buildCtrPrimitive(String id, XVariables.XVarInteger x, XEnums.TypeArithmeticOperator opa, XVariables.XVarInteger y, XEnums.TypeConditionOperatorRel op, int k) {
        rel(ari(var(x), opa, var(y)), op, k).post();
    }

    @Override
    public void buildCtrPrimitive(String id, XVariables.XVarInteger x, XEnums.TypeArithmeticOperator opa, XVariables.XVarInteger y, XEnums.TypeConditionOperatorRel op, XVariables.XVarInteger z) {
        rel(ari(var(x), opa, var(y)), op, var(z)).post();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////// GLOBAL CONSTRAINTS //////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void buildCtrAllDifferent(String id, XVariables.XVarInteger[] list) {
        model.allDifferent(vars(list)).post();
    }

    @Override
    public void buildCtrAllDifferentMatrix(String id, XVariables.XVarInteger[][] matrix) {
        for (XVariables.XVarInteger[] list : matrix) {
            model.allDifferent(vars(list)).post();
        }
        XVariables.XVarInteger[][] tmatrix = ArrayUtils.transpose(matrix);
        for(XVariables.XVarInteger[] list : tmatrix){
            model.allDifferent(vars(list)).post();
        }
    }

    @Override
    public void buildCtrAllEqual(String id, XVariables.XVarInteger[] list) {
        IntVar first = var(list[0]);
        for(int i = 1; i < list.length; i++){
            first.eq(var(list[i])).post();
        }
    }

    @Override
    public void buildCtrClause(String id, XVariables.XVarInteger[] pos, XVariables.XVarInteger[] neg) {
        model.addClauses(bools(pos), bools(neg));
    }

    private IntVar sumR(XParser.Condition condition) {
        IntVar sum;
        if (condition instanceof XParser.ConditionVar)
            sum = var((XVariables.XVarInteger) ((XParser.ConditionVar) condition).x);
        else if (condition instanceof XParser.ConditionVal)
            sum = model.intVar(((XParser.ConditionVal) condition).k);
        else
            throw new ParserException("unknow result for scalar constraint");
        return sum;
    }

    private void scalar(XVariables.XVarInteger[] list, int[] coeffs, XParser.Condition condition) {
        switch (condition.operator) {
            case LT:
                model.scalar(vars(list), coeffs, "<", sumR(condition)).post();
                break;
            case LE:
                model.scalar(vars(list), coeffs, "<=", sumR(condition)).post();
                break;
            case GE:
                model.scalar(vars(list), coeffs, ">", sumR(condition)).post();
                break;
            case GT:
                model.scalar(vars(list), coeffs, ">=", sumR(condition)).post();
                break;
            case NE:
                model.scalar(vars(list), coeffs, "!=", sumR(condition)).post();
                break;
            case EQ:
                model.scalar(vars(list), coeffs, "=", sumR(condition)).post();
                break;
            case IN: {
                IntVar sum;
                if (condition instanceof XParser.ConditionIntvl) {
                    sum = model.intVar(((XParser.ConditionIntvl) condition).min, ((XParser.ConditionIntvl) condition).max);
                } else {
                    sum = sumR(condition);
                }
                model.scalar(vars(list), coeffs, "=", sum).post();
            }
            break;
            case NOTIN: {
                int[] bounds = VariableUtils.boundsForScalar(vars(list), coeffs);
                IntVar sum = model.intVar(bounds[0], bounds[1]);
                if (condition instanceof XParser.ConditionIntvl) {
                    model.notMember(sum, ((XParser.ConditionIntvl) condition).min, ((XParser.ConditionIntvl) condition).max);
                } else if (condition instanceof XParser.ConditionVal) {
                    sum.ne(((XParser.ConditionVal) condition).k).post();
                } else {
                    throw new ParserException("unknow result for scalar constraint");
                }
                model.scalar(vars(list), coeffs, "=", sum).post();
            }
            break;
        }
    }

    @Override
    public void buildCtrSum(String id, XVariables.XVarInteger[] list, XParser.Condition condition) {
        int[] coeffs = new int[list.length];
        Arrays.fill(coeffs, 1);
        scalar(list, coeffs, condition);
    }

    @Override
    public void buildCtrSum(String id, XVariables.XVarInteger[] list, int[] coeffs, XParser.Condition condition) {
        scalar(list, coeffs, condition);
    }

    @Override
    public void buildCtrRegular(String id, XVariables.XVarInteger[] list, Object[][] transitions, String startState, String[] finalStates) {
        FiniteAutomaton auto = new FiniteAutomaton();
        TObjectIntHashMap<String> s2s = new TObjectIntHashMap<>(16, 1.5f, -1);
        for (Object[] tr : transitions) {
            int f = s2s.get(tr[0]);
            int v = (int) tr[1];
            int t = s2s.get(tr[2]);

            if (f == -1) {
                f = auto.addState();
                s2s.put((String) tr[0], f);
            }
            if (t == -1) {
                t = auto.addState();
                s2s.put((String) tr[0], t);
            }
            auto.addTransition(f, t, v);
        }
        auto.setInitialState(s2s.get(startState));
        auto.setFinal(Arrays.stream(finalStates).mapToInt(s -> s2s.get(s)).toArray());
        model.regular(vars(list), auto).post();
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////// OBJECTIVE ///////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    @Override
    public void buildObjToMinimize(String id, XVariables.XVarInteger x) {
        model.setObjective(false, var(x));
    }

    @Override
    public void buildObjToMaximize(String id, XVariables.XVarInteger x) {
        model.setObjective(true, var(x));
    }

    private IntVar optSum(String id, XVariables.XVarInteger[] list) {
        IntVar[] vars = vars(list);
        int[] bounds = VariableUtils.boundsForAddition(vars);
        IntVar res = model.intVar(id, bounds[0], bounds[1], true);
        model.sum(vars, "=", res).post();
        return res;
    }

    @Override
    public void buildObjToMinimize(String id, XEnums.TypeObjective type, XVariables.XVarInteger[] list) {
        model.setObjective(false, optSum(id, list));
    }

    @Override
    public void buildObjToMaximize(String id, XEnums.TypeObjective type, XVariables.XVarInteger[] list) {
        model.setObjective(true, optSum(id, list));
    }

    private IntVar optScalar(String id, XVariables.XVarInteger[] list, int[] coeffs) {
        IntVar[] vars = vars(list);
        int[] bounds = VariableUtils.boundsForScalar(vars, coeffs);
        IntVar res = model.intVar(id, bounds[0], bounds[1], true);
        model.scalar(vars, coeffs, "=", res).post();
        return res;
    }

    @Override
    public void buildObjToMinimize(String id, XEnums.TypeObjective type, XVariables.XVarInteger[] list, int[] coeffs) {
        model.setObjective(false, optScalar(id, list, coeffs));
    }


    @Override
    public void buildObjToMaximize(String id, XEnums.TypeObjective type, XVariables.XVarInteger[] list, int[] coeffs) {
        model.setObjective(true, optScalar(id, list, coeffs));
    }
}
