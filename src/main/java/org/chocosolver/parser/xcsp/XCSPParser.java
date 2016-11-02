/**
 * Copyright (c) 2014, chocoteam
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of the {organization} nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
import org.xcsp.parser.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Stream;

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
    public void buildCtrAllDifferentList(String id, XVariables.XVarInteger[][] lists) {
        Stream.of(lists).forEach(l -> model.allDifferent(vars(l)).post());
    }

    @Override
    public void buildCtrAllDifferentMatrix(String id, XVariables.XVarInteger[][] matrix) {
        for (XVariables.XVarInteger[] list : matrix) {
            model.allDifferent(vars(list)).post();
        }
        XVariables.XVarInteger[][] tmatrix = ArrayUtils.transpose(matrix);
        for (XVariables.XVarInteger[] list : tmatrix) {
            model.allDifferent(vars(list)).post();
        }
    }

    @Override
    public void buildCtrAllEqual(String id, XVariables.XVarInteger[] list) {
        model.allEqual(vars(list)).post();
    }

    @Override
    public void buildCtrNotAllEqual(String id, XVariables.XVarInteger[] list) {
        model.notAllEqual(vars(list)).post();
    }

    @Override
    public void buildCtrClause(String id, XVariables.XVarInteger[] pos, XVariables.XVarInteger[] neg) {
        model.addClauses(bools(pos), bools(neg));
    }

    private IntVar condV(XParser.Condition condition) {
        IntVar sum;
        if (condition instanceof XParser.ConditionVar)
            sum = var((XVariables.XVarInteger) ((XParser.ConditionVar) condition).x);
        else if (condition instanceof XParser.ConditionVal)
            sum = model.intVar(((XParser.ConditionVal) condition).k);
        else
            throw new ParserException("unknow result for scalar constraint");
        return sum;
    }

    private void notin(IntVar var, XParser.Condition condition) {
        if (condition instanceof XParser.ConditionIntvl) {
            model.notMember(var, ((XParser.ConditionIntvl) condition).min, ((XParser.ConditionIntvl) condition).max);
        } else if (condition instanceof XParser.ConditionVal) {
            var.ne(((XParser.ConditionVal) condition).k).post();
        } else {
            throw new ParserException("unknow result for scalar constraint");
        }
    }

    private void scalar(XVariables.XVarInteger[] list, int[] coeffs, XParser.Condition condition) {
        switch (condition.operator) {
            case LT:
                model.scalar(vars(list), coeffs, "<", condV(condition)).post();
                break;
            case LE:
                model.scalar(vars(list), coeffs, "<=", condV(condition)).post();
                break;
            case GE:
                model.scalar(vars(list), coeffs, ">", condV(condition)).post();
                break;
            case GT:
                model.scalar(vars(list), coeffs, ">=", condV(condition)).post();
                break;
            case NE:
                model.scalar(vars(list), coeffs, "!=", condV(condition)).post();
                break;
            case EQ:
                model.scalar(vars(list), coeffs, "=", condV(condition)).post();
                break;
            case IN: {
                IntVar sum;
                if (condition instanceof XParser.ConditionIntvl) {
                    sum = model.intVar(((XParser.ConditionIntvl) condition).min, ((XParser.ConditionIntvl) condition).max);
                } else {
                    sum = condV(condition);
                }
                model.scalar(vars(list), coeffs, "=", sum).post();
            }
            break;
            case NOTIN: {
                int[] bounds = VariableUtils.boundsForScalar(vars(list), coeffs);
                IntVar sum = model.intVar(bounds[0], bounds[1]);
                notin(sum, condition);
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

    @Override
    public void buildCtrMinimum(String id, XVariables.XVarInteger[] list, XParser.Condition condition) {
        IntVar[] vars = vars(list);
        int min = Arrays.stream(vars).min((v1, v2) -> v1.getLB() - v2.getLB()).get().getLB();
        int max = Arrays.stream(vars).max((v1, v2) -> v1.getUB() - v2.getUB()).get().getUB();
        switch (condition.operator) {
            case LT: {
                IntVar res = model.intVar(min, max);
                model.min(res, vars).post();
                res.lt(condV(condition)).post();
            }
            break;
            case LE: {
                IntVar res = model.intVar(min, max);
                model.min(res, vars).post();
                res.le(condV(condition)).post();
            }
            break;
            case GE: {
                IntVar res = model.intVar(min, max);
                model.min(res, vars).post();
                res.ge(condV(condition)).post();
            }
            break;
            case GT: {
                IntVar res = model.intVar(min, max);
                model.min(res, vars).post();
                res.gt(condV(condition)).post();
            }
            break;
            case NE: {
                IntVar res = model.intVar(min, max);
                model.min(res, vars).post();
                res.ne(condV(condition)).post();
            }
            break;
            case EQ:
                model.min(condV(condition), vars).post();
                break;
            case IN: {
                IntVar res;
                if (condition instanceof XParser.ConditionIntvl) {
                    res = model.intVar(((XParser.ConditionIntvl) condition).min, ((XParser.ConditionIntvl) condition).max);
                } else {
                    res = condV(condition);
                }
                model.min(res, vars(list)).post();
            }
            break;
            case NOTIN: {
                IntVar res = model.intVar(min, max);
                model.min(res, vars).post();
                res.ne(condV(condition)).post();
                notin(res, condition);
                model.min(res, vars(list)).post();
            }
            break;
        }
    }

    @Override
    public void buildCtrMaximum(String id, XVariables.XVarInteger[] list, XParser.Condition condition) {
        IntVar[] vars = vars(list);
        int min = Arrays.stream(vars).min((v1, v2) -> v1.getLB() - v2.getLB()).get().getLB();
        int max = Arrays.stream(vars).max((v1, v2) -> v1.getUB() - v2.getUB()).get().getUB();
        switch (condition.operator) {
            case LT: {
                IntVar res = model.intVar(min, max);
                model.max(res, vars).post();
                res.lt(condV(condition)).post();
            }
            break;
            case LE: {
                IntVar res = model.intVar(min, max);
                model.max(res, vars).post();
                res.le(condV(condition)).post();
            }
            break;
            case GE: {
                IntVar res = model.intVar(min, max);
                model.max(res, vars).post();
                res.ge(condV(condition)).post();
            }
            break;
            case GT: {
                IntVar res = model.intVar(min, max);
                model.max(res, vars).post();
                res.gt(condV(condition)).post();
            }
            break;
            case NE: {
                IntVar res = model.intVar(min, max);
                model.max(res, vars).post();
                res.ne(condV(condition)).post();
            }
            break;
            case EQ:
                model.max(condV(condition), vars).post();
                break;
            case IN: {
                IntVar res;
                if (condition instanceof XParser.ConditionIntvl) {
                    res = model.intVar(((XParser.ConditionIntvl) condition).min, ((XParser.ConditionIntvl) condition).max);
                } else {
                    res = condV(condition);
                }
                model.max(res, vars(list)).post();
            }
            break;
            case NOTIN: {
                IntVar res = model.intVar(min, max);
                model.max(res, vars).post();
                res.ne(condV(condition)).post();
                notin(res, condition);
                model.min(res, vars(list)).post();
            }
            break;
        }
    }

    @Override
    public void buildCtrLexMatrix(String id, XVariables.XVarInteger[][] matrix, XEnums.TypeOperator operator) {
        switch (operator) {
            case LT: {
                for (XVariables.XVarInteger[] list : matrix) {
                    model.lexChainLess(vars(list)).post();
                }
                XVariables.XVarInteger[][] tmatrix = ArrayUtils.transpose(matrix);
                for (XVariables.XVarInteger[] list : tmatrix) {
                    model.lexChainLessEq(vars(list)).post();
                }
            }
            break;
            case LE: {
                for (XVariables.XVarInteger[] list : matrix) {
                    model.lexChainLessEq(vars(list)).post();
                }
                XVariables.XVarInteger[][] tmatrix = ArrayUtils.transpose(matrix);
                for (XVariables.XVarInteger[] list : tmatrix) {
                    model.lexChainLessEq(vars(list)).post();
                }
            }
            break;
            case GE: {
                XVariables.XVarInteger[][] rmatrix = matrix.clone();
                ArrayUtils.reverse(rmatrix);
                for (XVariables.XVarInteger[] list : matrix) {
                    model.lexChainLessEq(vars(list)).post();
                }
                XVariables.XVarInteger[][] tmatrix = ArrayUtils.transpose(rmatrix);
                for (XVariables.XVarInteger[] list : tmatrix) {
                    model.lexChainLessEq(vars(list)).post();
                }
            }
            break;
            case GT: {
                XVariables.XVarInteger[][] rmatrix = matrix.clone();
                ArrayUtils.reverse(rmatrix);
                for (XVariables.XVarInteger[] list : matrix) {
                    model.lexChainLess(vars(list)).post();
                }
                XVariables.XVarInteger[][] tmatrix = ArrayUtils.transpose(rmatrix);
                for (XVariables.XVarInteger[] list : tmatrix) {
                    model.lexChainLess(vars(list)).post();
                }
            }
            break;
            case SUBSET:
            case SUBSEQ:
            case SUPSEQ:
            case SUPSET:
                XCallbacks2.super.buildCtrLexMatrix(id, matrix, operator);
                break;
        }
    }

    @Override
    public void buildCtrOrdered(String id, XVariables.XVarInteger[] list, XEnums.TypeOperator operator) {
        IntVar[] vars = vars(list);
        IntVar[][] vectors = new IntVar[vars.length][1];
        for (int i = 0; i < vars.length; i++) {
            vectors[i] = new IntVar[]{vars[i]};
        }
        switch (operator) {
            case LT:
                model.lexChainLess(vectors).post();
                break;
            case LE:
                model.lexChainLessEq(vectors).post();
                break;
            case GE: {
                ArrayUtils.reverse(vectors);
                model.lexChainLessEq(vectors).post();
            }
            break;
            case GT: {
                ArrayUtils.reverse(vectors);
                model.lexChainLess(vectors).post();
            }
            break;
            case SUBSET:
            case SUBSEQ:
            case SUPSEQ:
            case SUPSET:
                XCallbacks2.super.buildCtrOrdered(id, list, operator);
                break;
        }
    }

    @Override
    public void buildCtrChannel(String id, XVariables.XVarInteger[] list, int startIndex) {
        model.inverseChanneling(vars(list), vars(list), startIndex, startIndex).post();
    }

    @Override
    public void buildCtrChannel(String id, XVariables.XVarInteger[] list1, int startIndex1, XVariables.XVarInteger[] list2, int startIndex2) {
        model.inverseChanneling(vars(list1), vars(list2), startIndex1, startIndex2).post();
    }

    @Override
    public void buildCtrChannel(String id, XVariables.XVarInteger[] list, int startIndex, XVariables.XVarInteger value) {
        model.boolsIntChanneling(bools(list), var(value), startIndex).post();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////// GROUP ///////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void loadGroup(XConstraints.XGroup g) {
        beginGroup(g);
        if (g.template instanceof XConstraints.XCtr)
            loadCtrs((XConstraints.XCtr) g.template, g.argss, g);
        else if (g.template instanceof XConstraints.XLogic && ((XConstraints.XLogic) g.template).getType() == XEnums.TypeCtr.not) {
            XConstraints.CEntry child = ((XConstraints.XLogic) g.template).components[0];
            if (child instanceof XConstraints.XCtr && ((XConstraints.XCtr) child).type == XEnums.TypeCtr.allEqual) {
                // http://sofdem.github.io/gccat/aux/pdf/not_all_equal.pdf
                Stream.of(g.argss).forEach(o -> model.notAllEqual(vars((XVariables.XVarInteger[]) o)).post());
            }
        } else
            unimplementedCase(g);
        endGroup(g);
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
