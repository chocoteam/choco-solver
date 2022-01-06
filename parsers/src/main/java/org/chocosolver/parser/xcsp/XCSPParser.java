/*
 * This file is part of choco-parsers, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.parser.xcsp;

import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import org.chocosolver.parser.ParserException;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.extension.Tuples;
import org.chocosolver.solver.constraints.nary.automata.FA.FiniteAutomaton;
import org.chocosolver.solver.expression.discrete.arithmetic.ArExpression;
import org.chocosolver.solver.expression.discrete.arithmetic.NaArExpression;
import org.chocosolver.solver.expression.discrete.logical.BiLoExpression;
import org.chocosolver.solver.expression.discrete.logical.LoExpression;
import org.chocosolver.solver.expression.discrete.logical.NaLoExpression;
import org.chocosolver.solver.expression.discrete.relational.NaReExpression;
import org.chocosolver.solver.expression.discrete.relational.ReExpression;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Task;
import org.chocosolver.util.objects.graphs.MultivaluedDecisionDiagram;
import org.chocosolver.util.objects.queues.CircularQueue;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableRangeSet;
import org.chocosolver.util.tools.ArrayUtils;
import org.chocosolver.util.tools.VariableUtils;
import org.xcsp.common.Condition;
import org.xcsp.common.Types;
import org.xcsp.common.predicates.XNode;
import org.xcsp.common.predicates.XNodeParent;
import org.xcsp.common.structures.Transition;
import org.xcsp.parser.callbacks.XCallbacks2;
import org.xcsp.parser.entries.XConstraints;
import org.xcsp.parser.entries.XVariables;

import java.io.File;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.xcsp.common.Constants.STAR_INT;

/**
 * <p>
 * Project: choco-parsers.
 *
 * @author Charles Prud'homme
 * @since 01/06/2016.
 */
public class XCSPParser implements XCallbacks2 {

    private static final String S_INST_IN = "v <instantiation id='sol%s' type='solution' ";
    private static final String S_INST_OUT = "</instantiation>\n";
    private static final String S_LIST_IN = "<list>";
    private static final String S_LIST_OUT = "</list>";
    private static final String S_VALU_IN = "<values>";
    private static final String S_VALU_OUT = "</values>";

    /**
     * Mapping between XCSP vars and Choco vars
     */
    protected HashMap<XVariables.XVar, IntVar> mvars;
    protected HashSet<IntVar> symbolics;
    protected TObjectIntHashMap<String> symbolToInt;
    protected TIntObjectHashMap<String> intToSymbol;
    protected int unusedSymbol = 0;
    private ArrayList<IntVar> ovars;
    /**
     * The model to feed
     */
    Model model;

    Implem implem;

    public void model(Model model, String instance) throws Exception {
        this.model = model;
        this.mvars = new HashMap<>();
        this.symbolics = new HashSet<>();
        this.symbolToInt = new TObjectIntHashMap<>();
        this.intToSymbol = new TIntObjectHashMap<>();
        this.implem = new Implem(this);
        File file = new File(instance);
        if (file.exists()) {
            loadInstance(instance);
        } else {
            throw new RuntimeException("FILE DOES NOT EXIST");
        }
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

    @Override
    public void buildVarSymbolic(XVariables.XVarSymbolic x, String[] values) {
        int[] domain = new int[values.length];
        for (int i = 0; i < values.length; i++) {
            int value;
            if (symbolToInt.contains(values[i])) {
                value = symbolToInt.get(values[i]);
            } else {
                value = unusedSymbol++;
                symbolToInt.put(values[i], value);
                intToSymbol.put(value, values[i]);
            }
            domain[i] = value;
        }
        mvars.put(x, model.intVar(x.id, domain));
        symbolics.add(var(x));
    }

    @Override
    public void buildCtrIntension(String id, XVariables.XVarInteger[] scope, XNodeParent<XVariables.XVarInteger> tree) {
        ReExpression exp = buildRe(tree);
        if (VariableUtils.domainCardinality(vars(scope)) < Integer.MAX_VALUE / 1000) {
            exp.extension().post();
        } else {
            exp.decompose().post();
        }
    }

    private <V extends XVariables.XVar> ArExpression[] extractAr(XNode<V>[] sons) {
        return Arrays.stream(sons).map(this::buildAr).toArray(ArExpression[]::new);
    }

    private <V extends XVariables.XVar> ReExpression[] extractRe(XNode<V>[] sons) {
        return Arrays.stream(sons).map(this::buildRe).toArray(ReExpression[]::new);
    }

    private <V extends XVariables.XVar> ReExpression buildRe(XNode<V> tree) {
        Types.TypeExpr type = tree.type;
        if (type == Types.TypeExpr.VAR) {
            return (BoolVar) var(tree.var(0));
        } else if (type == Types.TypeExpr.LONG) {
            // TODO: deal with boolean primitive?
            return (BoolVar) model.intVar(tree.val(0));
        }
        XNode<V>[] sons = tree.sons;
        switch (type) {
            // relationnal
            case LT:
                return buildAr(sons[0]).lt(buildAr(sons[1]));
            case LE:
                return buildAr(sons[0]).le(buildAr(sons[1]));
            case GE:
                return buildAr(sons[0]).ge(buildAr(sons[1]));
            case GT:
                return buildAr(sons[0]).gt(buildAr(sons[1]));
            case NE:
                return buildAr(sons[0]).ne(buildAr(sons[1]));
            case IN:
                List<ArExpression> set = new ArrayList<>();
                for (XNode<V> sonsons : sons[1].sons) {
                    set.add(buildAr(sonsons));
                }
                //noinspection ConstantForZeroLengthArrayAllocation
                return buildAr(sons[0]).in(set.toArray(new ArExpression[0]));
            case EQ:
                if (sons.length == 2) {
                    return buildAr(sons[0]).eq(buildAr(sons[1]));
                } else {
                    return new NaReExpression(ReExpression.Operator.EQ, extractAr(sons));
                }
                // logical
            case AND:
                return new NaLoExpression(LoExpression.Operator.AND, extractRe(sons));
            case OR:
                return new NaLoExpression(LoExpression.Operator.OR, extractRe(sons));
            case XOR:
                return new NaLoExpression(LoExpression.Operator.XOR, extractRe(sons));
            case IFF:
                return new NaLoExpression(LoExpression.Operator.IFF, extractRe(sons));
            case IMP:
                return buildRe(sons[0]).imp(buildRe(sons[1]));
            case NOT:
                return buildRe(sons[0]).not();
            default:
                throw new UnsupportedOperationException("Unknown type : " + type);
        }
    }

    private <V extends XVariables.XVar> ArExpression buildAr(XNode<V> node) {
        Types.TypeExpr type = node.type;
        if (type == Types.TypeExpr.VAR) {
            return var(node.var(0));
        } else if (type == Types.TypeExpr.SYMBOL) {
            return model.intVar(symbolToInt.get(node.toString()));
        } else if (type == Types.TypeExpr.LONG) {
            return new ArExpression.IntPrimitive(node.val(0), model);
        }
        XNode<V>[] sons = node.sons;
        if (type.isLogicalOperator() && type.arityMax > 1 || type.equals(Types.TypeExpr.NOT)) {
            ReExpression[] res = extractRe(sons);
            switch (type) {
                // logical
                case AND:
                    return new NaLoExpression(LoExpression.Operator.AND, res);
                case OR:
                    return new NaLoExpression(LoExpression.Operator.OR, res);
                case XOR:
                    return new BiLoExpression(LoExpression.Operator.XOR, res[0], res[1]);
                case IFF:
                    return new NaLoExpression(LoExpression.Operator.IFF, res);
                case IMP:
                    return new NaLoExpression(LoExpression.Operator.OR, res);
                case NOT:
                    return res[0].not();
                default:
                    throw new UnsupportedOperationException("Unknown type : " + type);
            }
        } else {
            ArExpression[] aes = extractAr(sons);
            switch (type) {
                // arithmetic
//            return this == ADD || this == SUB || this == MUL || this == DIV || this == MOD || this == POW || this == DIST;
                case ADD:
                    return new NaArExpression(ArExpression.Operator.ADD, aes);
                case SUB:
                    return aes[0].sub(aes[1]);
                case MUL:
                    return new NaArExpression(ArExpression.Operator.MUL, aes);
                case MIN:
                    return new NaArExpression(ArExpression.Operator.MIN, aes);
                case MAX:
                    return new NaArExpression(ArExpression.Operator.MAX, aes);
                case DIV:
                    return aes[0].div(aes[1]);
                case MOD:
                    return aes[0].mod(aes[1]);
                case POW:
                    return aes[0].pow(aes[1]);
                case DIST:
                    return aes[0].dist(aes[1]);
                case NEG:
                    return aes[0].neg();
                case ABS:
                    return aes[0].abs();
                case SQR:
                    return aes[0].sqr();
                // relationnal
                case LT:
                    return aes[0].lt(aes[1]);
                case LE:
                    return aes[0].le(aes[1]);
                case GE:
                    return aes[0].ge(aes[1]);
                case GT:
                    return aes[0].gt(aes[1]);
                case NE:
                    return aes[0].ne(aes[1]);
                case IN:
                    return new NaReExpression(ReExpression.Operator.IN, aes);
                case EQ:
                    if (aes.length == 2) {
                        return aes[0].eq(aes[1]);
                    } else {
                        return new NaReExpression(ReExpression.Operator.EQ, aes);
                    }
                    // logical
                case IF:
                    return ((ReExpression) aes[0]).ift(aes[1], aes[2]);
                default:
                    throw new UnsupportedOperationException("Unknown type : " + type);
            }
        }
    }


    private <V extends XVariables.XVar> IntVar var(V var) {
        return mvars.get(var);
    }

    private <V extends XVariables.XVar> IntVar[] vars(V[] vars) {
        return Arrays.stream(vars).map(this::var).toArray(IntVar[]::new);
    }

    private <V extends XVariables.XVar> IntVar[][] vars(V[][] vars) {
        return Arrays.stream(vars).map(this::vars).toArray(IntVar[][]::new);
    }

    private <V extends XVariables.XVar> BoolVar bool(V var) {
        return (BoolVar) mvars.get(var);
    }

    private <V extends XVariables.XVar> BoolVar[] bools(V[] vars) {
        return Arrays.stream(vars).map(this::bool).toArray(BoolVar[]::new);
    }

    private <V extends XVariables.XVar> BoolVar[][] bools(V[][] vars) {
        return Arrays.stream(vars).map(this::bools).toArray(BoolVar[][]::new);
    }

    private <V extends XVariables.XVar> IntVar var(XNode<V> tree) {
        //todo: use caching to avoid useless clones
        return buildAr(tree).intVar();
    }

    private <V extends XVariables.XVar> IntVar[] vars(XNode<V>[] trees) {
        return Arrays.stream(trees).map(this::var).toArray(IntVar[]::new);
    }

    public String printSolution(boolean format) {
        StringBuilder buffer = new StringBuilder();
        if (ovars == null) {
            ovars = new ArrayList<>(mvars.values());
            ovars.sort(IntVar::compareTo);
        }
        buffer.append(String.format(S_INST_IN, model.getSolver().getSolutionCount()));
        if (model.getSolver().hasObjective()) {
            buffer.append("cost='").append(model.getObjective().asIntVar().getValue()).append("' ");
        }
        buffer.append(">");
        buffer.append(format ? "\nv \t" : "").append(S_LIST_IN);
        // list variables
        ovars.forEach(ovar -> buffer.append(ovar.getName()).append(' '));
        buffer.append(S_LIST_OUT).append(format ? "\nv \t" : "").append(S_VALU_IN);
        ovars.forEach(ovar -> {
            if (symbolics.contains(ovar)) {
                buffer.append(intToSymbol.get(ovar.getValue())).append(' ');
            } else {
                buffer.append(ovar.getValue()).append(' ');
            }
        });
        buffer.append(S_VALU_OUT).append(format ? "\nv " : "").append(S_INST_OUT);
        return buffer.toString();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////// EXTENSION CONSTRAINTS ////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void buildCtrIntension(String id, XVariables.XVarSymbolic[] scope, XNodeParent<XVariables.XVarSymbolic> syntaxTreeRoot) {
        ReExpression exp = buildRe(syntaxTreeRoot);
        if (VariableUtils.domainCardinality(vars(scope)) < Integer.MAX_VALUE / 1000) {
            exp.extension().post();
        } else {
            exp.decompose().post();
        }
    }

    @Override
    public void buildCtrExtension(String id, XVariables.XVarSymbolic x, String[] values, boolean positive, Set<Types.TypeFlag> flags) {
        if (flags.contains(Types.TypeFlag.STARRED_TUPLES)) {
            // can you manage tables with symbol * ?
            throw new ParserException("Tables with symbol * are not supported");
        }
        //noinspection StatementWithEmptyBody
        if (flags.contains(Types.TypeFlag.UNCLEAN_TUPLES)) {
            // do you have to clean the tuples, so as to remove those that cannot be built from variable domains ?
        }
        if (positive) {
            model.member(var(x), Arrays.stream(values).mapToInt(t -> symbolToInt.get(t)).toArray()).post();
        } else {
            model.notMember(var(x), Arrays.stream(values).mapToInt(t -> symbolToInt.get(t)).toArray()).post();
        }
    }

    @Override
    public void buildCtrExtension(String id, XVariables.XVarSymbolic[] list, String[][] tuples, boolean positive, Set<Types.TypeFlag> flags) {
        //noinspection StatementWithEmptyBody
        if (flags.contains(Types.TypeFlag.UNCLEAN_TUPLES)) {
            // do you have to clean the tuples, so as to remove those that cannot be built from variable domains ?
        }
        Tuples mTuples = new Tuples(Arrays.stream(tuples)
                .map(t -> Arrays.stream(t).mapToInt(e -> symbolToInt.get(e)).toArray())
                .toArray(int[][]::new), positive);
        if (flags.contains(Types.TypeFlag.STARRED_TUPLES)) {
            if (!positive) {
                // can you manage tables with symbol * ?
                throw new ParserException("Negative tables with symbol * are not supported");
            }
            mTuples.setUniversalValue(STAR_INT);
        }
        model.table(vars(list), mTuples).post();
    }

    @Override
    public void buildCtrExtension(String id, XVariables.XVarInteger[] list, int[][] tuples, boolean positive, Set<Types.TypeFlag> flags) {
        //noinspection StatementWithEmptyBody
        if (flags.contains(Types.TypeFlag.UNCLEAN_TUPLES)) {
            // do you have to clean the tuples, so as to remove those that cannot be built from variable domains ?
        }
        Tuples mTuples = new Tuples(tuples, positive);
        if (flags.contains(Types.TypeFlag.STARRED_TUPLES)) {
            if (!positive) {
                // can you manage tables with symbol * ?
                throw new ParserException("Negative tables with symbol * are not supported");
            }
            mTuples.setUniversalValue(STAR_INT);
        }
        model.table(vars(list), mTuples).post();
    }

    @Override
    public void buildCtrExtension(String id, XVariables.XVarInteger x, int[] values, boolean positive, Set<Types.TypeFlag> flags) {
        if (flags.contains(Types.TypeFlag.STARRED_TUPLES)) {
            // can you manage tables with symbol * ?
            throw new ParserException("Tables with symbol * are not supported");
        }
        //noinspection StatementWithEmptyBody
        if (flags.contains(Types.TypeFlag.UNCLEAN_TUPLES)) {
            // do you have to clean the tuples, so as to remove those that cannot be built from variable domains ?
        }
        if (positive) {
            model.member(var(x), values).post();
        } else {
            model.notMember(var(x), values).post();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////// PRIMITIVE CONSTRAINTS ////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static ReExpression rel(ArExpression a, Types.TypeConditionOperatorRel op, int k) {
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

    private static ReExpression rel(ArExpression a, Types.TypeConditionOperatorRel op, IntVar k) {
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

    private static ArExpression ari(ArExpression a, Types.TypeArithmeticOperator opa, ArExpression b) {
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
    public void buildCtrPrimitive(String id, XVariables.XVarInteger x, Types.TypeConditionOperatorRel op, int k) {
        switch (op) {
            case LT:
                model.arithm(var(x), "<", k).post();
                break;
            case LE:
                model.arithm(var(x), "<=", k).post();
                break;
            case GE:
                model.arithm(var(x), ">=", k).post();
                break;
            case GT:
                model.arithm(var(x), ">", k).post();
                break;
            case NE:
                model.arithm(var(x), "!=", k).post();
                break;
            case EQ:
                model.arithm(var(x), "=", k).post();
                break;
            default:
                rel(var(x), op, k).post();
                break;
        }
    }

    @Override
    public void buildCtrPrimitive(String id, XVariables.XVarInteger x, Types.TypeArithmeticOperator opa, XVariables.XVarInteger y, Types.TypeConditionOperatorRel op, int k) {
        // TODO
        rel(ari(var(x), opa, var(y)), op, k).post();
    }

    @Override
    public void buildCtrPrimitive(String id, XVariables.XVarInteger x, Types.TypeArithmeticOperator opa, XVariables.XVarInteger y, Types.TypeConditionOperatorRel op, XVariables.XVarInteger z) {
        // TODO
        rel(ari(var(x), opa, var(y)), op, var(z)).post();
    }

    @Override
    public void buildCtrLogic(String id, Types.TypeLogicalOperator op, XVariables.XVarInteger[] vars) {
        repost(id);
    }

    @Override
    public void buildCtrPrimitive(String id, XVariables.XVarInteger x, Types.TypeArithmeticOperator aop, int p, Types.TypeConditionOperatorRel op, int k) {
        rel(ari(var(x), aop, model.intVar(p)), op, model.intVar(k)).post();
    }

    @Override
    public void buildCtrPrimitive(String id, XVariables.XVarInteger x, Types.TypeUnaryArithmeticOperator aop, XVariables.XVarInteger y) {
        switch (aop) {
            case ABS:
                model.absolute(var(x), var(y)).post();
                break;
            case NEG:
                model.arithm(var(x), "+", var(y), "=", 0).post();
                break;
            case SQR:
                model.square(var(x), var(y)).post();
                break;
            case NOT:
                XCallbacks2.super.buildCtrPrimitive(id, x, aop, y);
                break;
        }
    }

    @Override
    public void buildCtrPrimitive(String id, XVariables.XVarInteger x, Types.TypeArithmeticOperator aop, int p, Types.TypeConditionOperatorRel op, XVariables.XVarInteger y) {
        rel(ari(var(x), aop, model.intVar(p)), op, var(y)).post();
    }

    @Override
    public void buildCtrPrimitive(String id, XVariables.XVarInteger x, Types.TypeConditionOperatorSet op, int[] t) {
        switch (op) {
            case IN:
                model.member(var(x), t).post();
                break;
            case NOTIN:
                model.notMember(var(x), t).post();
                break;
        }
    }

    @Override
    public void buildCtrPrimitive(String id, XVariables.XVarInteger x, Types.TypeConditionOperatorSet op, int min, int max) {
        switch (op) {
            case IN:
                model.member(var(x), min, max).post();
                break;
            case NOTIN:
                model.notMember(var(x), min, max).post();
                break;
        }
    }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////// GLOBAL CONSTRAINTS //////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void buildCtrAtLeast(String id, XVariables.XVarInteger[] list, int value, int k) {
        model.count(value, vars(list), model.intVar(k, list.length)).post();
    }

    @Override
    public void buildCtrAtMost(String id, XVariables.XVarInteger[] list, int value, int k) {
        model.count(value, vars(list), model.intVar(0, k)).post();
    }

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
        for (XVariables.XVarInteger[] list : tmatrix) {
            model.allDifferent(vars(list)).post();
        }
    }

    @Override
    public void buildCtrAllDifferentExcept(String id, XVariables.XVarInteger[] list, int[] except) {
        if (except.length == 0) {
            model.allDifferent(vars(list)).post();
        } else if (except.length == 1) {
            model.allDifferentUnderCondition(vars(list), x -> !x.contains(except[0]), true).post();
        } else {
            IntIterableRangeSet set = new IntIterableRangeSet(except);
            model.allDifferentUnderCondition(vars(list), x -> !set.intersect(x), true).post();
        }
    }

    @Override
    public void buildCtrAllDifferentMatrix(String id, XVariables.XVarInteger[][] matrix, int[] except) {
        this.buildCtrAllDifferentExcept(id, ArrayUtils.flatten(matrix), except);
    }

    @Override
    public void buildCtrAllDifferentList(String id, XVariables.XVarInteger[][] lists) {
        int d1 = lists.length;
        for (int i = 0; i < d1; i++) {
            for (int j = i + 1; j < d1; j++) {
                buildDistinctVectors(vars(lists[i]), vars(lists[j]));
            }
        }
    }

    @Override
    public void buildCtrAllDifferent(String id, XNode<XVariables.XVarInteger>[] trees) {
        model.allDifferent(vars(trees)).post();
    }

    @Override
    public void buildCtrAllDifferent(String id, XVariables.XVarSymbolic[] list) {
        model.allDifferent(vars(list)).post();
    }

    private void buildDistinctVectors(IntVar[] t1, IntVar[] t2) {
        int k = t1.length;
        BoolVar[] diffs = model.boolVarArray(k);
        for (int i = 0; i < k; i++) {
            model.reifyXneY(t1[i], t2[i], diffs[i]);
        }
        model.addClausesBoolOrArrayEqualTrue(diffs);
    }

    @Override
    public void buildCtrAllEqual(String id, XVariables.XVarInteger[] list) {
        model.allEqual(vars(list)).post();
    }

    @Override
    public void buildCtrAllEqual(String id, XNode<XVariables.XVarInteger>[] trees) {
        model.allEqual(vars(trees)).post();
    }

    @Override
    public void buildCtrNotAllEqual(String id, XVariables.XVarInteger[] list) {
        model.notAllEqual(vars(list)).post();
    }

    @Override
    public void buildCtrCardinality(String id, XVariables.XVarInteger[] list, boolean closed, int[] values, XVariables.XVarInteger[] occurs) {
        model.globalCardinality(vars(list), values, vars(occurs), closed).post();
    }

    @Override
    public void buildCtrCardinality(String id, XVariables.XVarInteger[] list, boolean closed, int[] values, int[] occurs) {
        model.globalCardinality(
                vars(list),
                values,
                Arrays.stream(occurs)
                        .mapToObj(v -> model.intVar(v))
                        .toArray(IntVar[]::new),
                closed
        ).post();
    }

    @Override
    public void buildCtrCardinality(String id, XVariables.XVarInteger[] list, boolean closed, int[] values, int[] occursMin, int[] occursMax) {
        model.globalCardinality(
                vars(list),
                values,
                IntStream.range(0, values.length)
                        .mapToObj(i -> model.intVar(occursMin[i], occursMax[i]))
                        .toArray(IntVar[]::new),
                closed
        ).post();
    }

    @Override
    public void buildCtrClause(String id, XVariables.XVarInteger[] pos, XVariables.XVarInteger[] neg) {
        model.addClauses(bools(pos), bools(neg));
    }

    @Override
    public void buildCtrCircuit(String id, XVariables.XVarInteger[] list, int startIndex) {
        model.subCircuit(vars(list), startIndex, model.intVar("circ_size", 0, list.length)).post();
    }

    @Override
    public void buildCtrCircuit(String id, XVariables.XVarInteger[] list, int startIndex, int size) {
        model.subCircuit(vars(list), startIndex, model.intVar(size)).post();
    }

    @Override
    public void buildCtrCircuit(String id, XVariables.XVarInteger[] list, int startIndex, XVariables.XVarInteger size) {
        model.subCircuit(vars(list), startIndex, var(size)).post();
    }

    private IntVar condV(Condition condition) {
        IntVar sum;
        if (condition instanceof Condition.ConditionVar)
            sum = var((XVariables.XVarInteger) ((Condition.ConditionVar) condition).x);
        else if (condition instanceof Condition.ConditionVal)
            sum = model.intVar((int) ((Condition.ConditionVal) condition).k);
        else
            throw new ParserException("unknow result for scalar constraint");
        return sum;
    }

    private void notin(IntVar var, Condition condition) {
        if (condition instanceof Condition.ConditionIntvl) {
            model.notMember(var, (int) ((Condition.ConditionIntvl) condition).min, (int) ((Condition.ConditionIntvl) condition).max);
        } else if (condition instanceof Condition.ConditionVal) {
            var.ne((int) ((Condition.ConditionVal) condition).k).post();
        } else {
            throw new ParserException("unknow result for scalar constraint");
        }
    }

    private void buildSum(IntVar[] res, int[] coeffs, Condition condition) {
        if (condition instanceof Condition.ConditionRel) {
            Condition.ConditionRel conditionRel = (Condition.ConditionRel) condition;
            switch (conditionRel.operator) {
                case LT:
                    model.scalar(res, coeffs, "<", condV(condition)).post();
                    break;
                case LE:
                    model.scalar(res, coeffs, "<=", condV(condition)).post();
                    break;
                case GE:
                    model.scalar(res, coeffs, ">=", condV(condition)).post();
                    break;
                case GT:
                    model.scalar(res, coeffs, ">", condV(condition)).post();
                    break;
                case NE:
                    model.scalar(res, coeffs, "!=", condV(condition)).post();
                    break;
                case EQ:
                    model.scalar(res, coeffs, "=", condV(condition)).post();
                    break;
            }
        } else if (condition instanceof Condition.ConditionSet) {
            Condition.ConditionSet conditionSet = (Condition.ConditionSet) condition;
            switch (conditionSet.operator) {
                case IN: {
                    IntVar sum;
                    if (condition instanceof Condition.ConditionIntvl) {
                        sum = model.intVar((int) ((Condition.ConditionIntvl) condition).min, (int) ((Condition.ConditionIntvl) condition).max);
                    } else {
                        sum = condV(condition);
                    }
                    model.scalar(res, coeffs, "=", sum).post();
                }
                break;
                case NOTIN: {
                    int[] bounds = VariableUtils.boundsForScalar(res, coeffs);
                    IntVar sum = model.intVar(bounds[0], bounds[1]);
                    notin(sum, condition);
                    model.scalar(res, coeffs, "=", sum).post();
                }
                break;
            }
        }
    }


    @Override
    public void buildCtrSum(String id, XNode<XVariables.XVarInteger>[] trees, Condition condition) {
        int[] coeffs = new int[trees.length];
        Arrays.fill(coeffs, 1);
        buildSum(vars(trees), coeffs, condition);
    }

    @Override
    public void buildCtrSum(String id, XNode<XVariables.XVarInteger>[] trees, int[] coeffs, Condition condition) {
        buildSum(vars(trees), coeffs, condition);
    }

    @Override
    public void buildCtrSum(String id, XNode<XVariables.XVarInteger>[] trees, XVariables.XVarInteger[] coeffs, Condition condition) {
        IntVar[] res = new IntVar[trees.length];
        for (int i = 0; i < trees.length; i++) {
            IntVar var = var(trees[i]);
            int[] bounds = VariableUtils.boundsForMultiplication(var, var(coeffs[i]));
            res[i] = model.intVar(bounds[0], bounds[1]);
            model.times(var, var(coeffs[i]), res[i]).post();
        }
        int[] _coeffs = new int[trees.length];
        Arrays.fill(_coeffs, 1);
        buildSum(res, _coeffs, condition);
    }

    @Override
    public void buildCtrSum(String id, XVariables.XVarInteger[] list, Condition condition) {
        int[] coeffs = new int[list.length];
        Arrays.fill(coeffs, 1);
        buildSum(vars(list), coeffs, condition);
    }

    @Override
    public void buildCtrSum(String id, XVariables.XVarInteger[] list, int[] coeffs, Condition condition) {
        buildSum(vars(list), coeffs, condition);
    }

    @Override
    public void buildCtrSum(String id, XVariables.XVarInteger[] list, XVariables.XVarInteger[] _coeffs, Condition condition) {
        IntVar[] res = new IntVar[list.length];
        for (int i = 0; i < list.length; i++) {
            int[] bounds = VariableUtils.boundsForMultiplication(var(list[i]), var(_coeffs[i]));
            res[i] = model.intVar(bounds[0], bounds[1]);
            model.times(var(list[i]), var(_coeffs[i]), res[i]).post();
        }
        int[] coeffs = new int[list.length];
        Arrays.fill(coeffs, 1);
        buildSum(res, coeffs, condition);

    }

    @Override
    public void buildCtrCount(String id, XVariables.XVarInteger[] list, int[] values, Condition condition) {
        dealWithCondition(condition, 0, list.length,
                x -> model.among(x, vars(list), values).post());
    }


    @Override
    public void buildCtrCount(String id, XNode<XVariables.XVarInteger>[] trees, int[] values, Condition condition) {
        dealWithCondition(condition, 0, trees.length,
                x -> model.among(x, vars(trees), values).post());
    }


    private void buildCtrNValues(String id, IntVar[] vars, Condition condition) {
        if (condition instanceof Condition.ConditionRel) {
            Condition.ConditionRel conditionRel = (Condition.ConditionRel) condition;
            switch (conditionRel.operator) {
                case LT: {
                    //TODO
                    model.atMostNValues(vars, model.intOffsetView(condV(condition), -1), false).post();
                }
                return;
                case LE: {
                    model.atMostNValues(vars, condV(condition), false).post();
                }
                return;
                case GE: {
                    //TODO
                    model.atLeastNValues(vars, condV(condition), false).post();
                }
                return;
                case GT: {
                    //TODO
                    model.atLeastNValues(vars, model.intOffsetView(condV(condition), 1), false).post();
                }
                return;
                case NE: {
                    IntVar count = model.intVar(0, vars.length);
                    model.nValues(vars, count).post();
                    IntVar limit = condV(condition);
                    model.arithm(count, "!=", limit).post();
                }
                return;
                case EQ:
                    model.nValues(vars, condV(condition)).post();
                    return;
            }
        }
        this.unimplementedCase(id);
    }

    @Override
    public void buildCtrNValues(String id, XVariables.XVarInteger[] list, Condition condition) {
        buildCtrNValues(id, vars(list), condition);
    }

    @Override
    public void buildCtrNValues(String id, XNode<XVariables.XVarInteger>[] trees, Condition condition) {
        buildCtrNValues(id, vars(trees), condition);
    }

    @Override
    public void buildCtrNValuesExcept(String id, XVariables.XVarInteger[] list, int[] except, Condition condition) {
        XCallbacks2.super.buildCtrNValuesExcept(id, list, except, condition);
    }


    @Override
    public void buildCtrRegular(String id, XVariables.XVarInteger[] list, Transition[] transitions, String startState, String[] finalStates) {
        FiniteAutomaton auto = new FiniteAutomaton();
        TObjectIntHashMap<String> s2s = new TObjectIntHashMap<>(16, 1.5f, -1);
        for (Transition tr : transitions) {
            int f = s2s.get(tr.start);
            int v = ((Long) tr.value).intValue();
            if (f == -1) {
                f = auto.addState();
                s2s.put(tr.start, f);
            }
            int t = s2s.get(tr.end);
            if (t == -1) {
                t = auto.addState();
                s2s.put(tr.end, t);
            }
            auto.addTransition(f, t, v);
        }
        auto.setInitialState(s2s.get(startState));
        auto.setFinal(Arrays.stream(finalStates).mapToInt(s2s::get).toArray());
        model.regular(vars(list), auto).post();
    }

    @Override
    public void buildCtrMDD(String id, XVariables.XVarInteger[] list, Transition[] transitions) {
        HashMap<String, List<Transition>> layers = new HashMap<>();
        HashSet<String> possibleRoots = new HashSet<>(), notRoots = new HashSet<>();
        Set<String> possibleWells = new HashSet<>(), notWells = new HashSet<>();
        for (int t = 0; t < transitions.length; t++) {
            String src = transitions[t].start, tgt = transitions[t].end;
            notWells.add(src);
            notRoots.add(tgt);
            if (!notRoots.contains(src)) {
                possibleRoots.add(src);
            }
            if (!notWells.contains(tgt)) {
                possibleWells.add(tgt);
            }
            possibleRoots.remove(tgt);
            possibleWells.remove(src);
            List<Transition> succs = layers.computeIfAbsent(src, k -> new ArrayList<>());
            succs.add(transitions[t]);
        }

        String first = possibleRoots.toArray(new String[1])[0];
        String last = possibleWells.toArray(new String[1])[0];
        TObjectIntHashMap<String> map = new TObjectIntHashMap<>();
        map.put(first, 0);
        map.put(last, -1);
        possibleRoots.add(last);
        int n = 1;

        int[][] mtransitions = new int[transitions.length][3];
        int k = 0;
        CircularQueue<String> queue = new CircularQueue<>(layers.size());
        queue.addLast(first);
        while (!queue.isEmpty()) {
            String src = queue.pollFirst();
            List<Transition> succs = layers.get(src);
            if (succs == null) continue;
            for (Transition t : succs) {
                String tgt = t.end;
                if (!possibleRoots.contains(tgt)) {
                    queue.addLast(tgt);
                    possibleRoots.add(tgt);
                    map.put(tgt, n++);
                }
                mtransitions[k++] = new int[]{map.get(src), ((Long) t.value).intValue(), map.get(tgt)};
            }
        }
        IntVar[] mVars = vars(list);
        MultivaluedDecisionDiagram mdd = new MultivaluedDecisionDiagram(mVars, mtransitions);
        model.mddc(mVars, mdd).post();

    }


    @Override
    public void buildCtrExactly(String id, XVariables.XVarInteger[] list, int value, int k) {
        model.count(value, vars(list), model.intVar(k)).post();
    }

    @Override
    public void buildCtrExactly(String id, XVariables.XVarInteger[] list, int value, XVariables.XVarInteger k) {
        model.count(value, vars(list), var(k)).post();
    }

    @Override
    public void buildCtrMinimum(String id, XVariables.XVarInteger[] list, Condition condition) {
        buildMin(vars(list), condition);
    }

    @Override
    public void buildCtrMinimum(String id, XNode<XVariables.XVarInteger>[] trees, Condition condition) {
        buildMin(vars(trees), condition);
    }

    private void buildMin(IntVar[] vars, Condition condition) {
        int min = Arrays.stream(vars).min(Comparator.comparingInt(IntVar::getLB)).get().getLB();
        int max = Arrays.stream(vars).max(Comparator.comparingInt(IntVar::getUB)).get().getUB();
        dealWithCondition(condition, min, max, x -> model.min(x, vars).post());
    }


    @Override
    public void buildCtrElement(String id, XVariables.XVarInteger[] list, Condition condition) {
        IntVar[] vars = vars(list);
        int min = Arrays.stream(vars).min(Comparator.comparingInt(IntVar::getLB)).get().getLB();
        int max = Arrays.stream(vars).max(Comparator.comparingInt(IntVar::getUB)).get().getUB();
        dealWithCondition(condition, min, max,
                x -> model.element(x, vars(list), model.intVar(0, list.length), 0).post());
    }

    @Override
    public void buildCtrElement(String id, XVariables.XVarInteger[] list,
                                int startIndex, XVariables.XVarInteger index,
                                Types.TypeRank rank, Condition condition) {
        if (rank == Types.TypeRank.ANY) {
            IntVar[] vars = vars(list);
            int min = Arrays.stream(vars).min(Comparator.comparingInt(IntVar::getLB)).get().getLB();
            int max = Arrays.stream(vars).max(Comparator.comparingInt(IntVar::getUB)).get().getUB();
            dealWithCondition(condition, min, max,
                    x -> model.element(x, vars(list), var(index), startIndex).post());
        } else XCallbacks2.super.buildCtrElement(id, list, startIndex, index, rank, condition);
    }

    @Override
    public void buildCtrElement(String id, int[] list, int startIndex, XVariables.XVarInteger index,
                                Types.TypeRank rank, Condition condition) {
        if (rank == Types.TypeRank.ANY) {
            int min = Arrays.stream(list).min().getAsInt();
            int max = Arrays.stream(list).max().getAsInt();
            dealWithCondition(condition, min, max,
                    x -> model.element(x, list, var(index), startIndex).post());
        } else XCallbacks2.super.buildCtrElement(id, list, startIndex, index, rank, condition);
    }

    @Override
    public void buildCtrElement(String id, int[][] matrix, int startRowIndex, XVariables.XVarInteger rowIndex,
                                int startColIndex, XVariables.XVarInteger colIndex, Condition condition) {
        int min = Arrays.stream(matrix).mapToInt(r -> Arrays.stream(r).min().getAsInt()).min().getAsInt();
        int max = Arrays.stream(matrix).mapToInt(r -> Arrays.stream(r).max().getAsInt()).max().getAsInt();
        dealWithCondition(condition, min, max,
                x -> model.element(x, matrix, var(rowIndex), startColIndex, var(colIndex), startColIndex));
    }

    @Override
    public void buildCtrElement(String id, XVariables.XVarInteger[][] matrix, int startRowIndex,
                                XVariables.XVarInteger rowIndex, int startColIndex, XVariables.XVarInteger colIndex,
                                Condition condition) {
        IntVar[][] vars = vars(matrix);
        int min = Arrays.stream(vars).mapToInt(r -> Arrays.stream(r).mapToInt(IntVar::getLB)
                .min().getAsInt()).min().getAsInt();
        int max = Arrays.stream(vars).mapToInt(r -> Arrays.stream(r).mapToInt(IntVar::getLB)
                .max().getAsInt()).max().getAsInt();
        dealWithCondition(condition, min, max,
                x -> model.element(x, vars(matrix), var(rowIndex), startColIndex, var(colIndex), startColIndex));
    }

    @Override
    public void buildCtrMaximum(String id, XVariables.XVarInteger[] list, Condition condition) {
        buildMax(vars(list), condition);
    }

    @Override
    public void buildCtrMaximum(String id, XNode<XVariables.XVarInteger>[] trees, Condition condition) {
        buildMax(vars(trees), condition);
    }

    private void buildMax(IntVar[] vars, Condition condition) {
        int min = Arrays.stream(vars).min(Comparator.comparingInt(IntVar::getLB)).get().getLB();
        int max = Arrays.stream(vars).max(Comparator.comparingInt(IntVar::getUB)).get().getUB();
        dealWithCondition(condition, min, max,
                x -> model.max(x, vars).post());
    }

    @Override
    public void buildCtrLexMatrix(String id, XVariables.XVarInteger[][] matrix, Types.TypeOperatorRel operator) {
        switch (operator) {
            case LT: {
                model.lexChainLess(vars(matrix)).post();
                XVariables.XVarInteger[][] tmatrix = ArrayUtils.transpose(matrix);
                model.lexChainLess(vars(tmatrix)).post();
            }
            break;
            case LE: {
                model.lexChainLessEq(vars(matrix)).post();
                XVariables.XVarInteger[][] tmatrix = ArrayUtils.transpose(matrix);
                model.lexChainLessEq(vars(tmatrix)).post();
            }
            break;
            case GT: {
                XVariables.XVarInteger[][] rmatrix = matrix.clone();
                ArrayUtils.reverse(rmatrix);
                model.lexChainLess(vars(rmatrix)).post();
                model.lexChainLess(vars(ArrayUtils.transpose(rmatrix))).post();
            }
            break;
            case GE: {
                XVariables.XVarInteger[][] rmatrix = matrix.clone();
                ArrayUtils.reverse(rmatrix);
                model.lexChainLessEq(vars(rmatrix)).post();
                model.lexChainLessEq(vars(ArrayUtils.transpose(rmatrix))).post();
            }
            break;
        }
    }

    @Override
    public void buildCtrOrdered(String id, XVariables.XVarInteger[] list, Types.TypeOperatorRel operator) {
        IntVar[] vars = vars(list);
        IntVar[][] vectors = new IntVar[vars.length][1];
        for (int i = 0; i < vars.length; i++) {
            vectors[i] = new IntVar[]{vars[i]};
        }
        lexCtr(vectors, operator);
    }

    @Override
    public void buildCtrOrdered(String id, XVariables.XVarInteger[] list, int[] lengths, Types.TypeOperatorRel operator) {
        IntVar[] vars = vars(list);
        IntVar[][] vectors = new IntVar[vars.length * 2 - 1][1];
        int k = 0;
        for (int i = 0; i < vars.length - 1; i++) {
            vectors[k++] = new IntVar[]{vars[i]};
            vectors[k++] = new IntVar[]{vars[i].add(lengths[i]).intVar()};
        }
        vectors[k] = new IntVar[]{vars[vars.length - 1]};
        lexCtr(vectors, operator);
    }

    @Override
    public void buildCtrOrdered(String id, XVariables.XVarInteger[] list, XVariables.XVarInteger[] lengths, Types.TypeOperatorRel operator) {
        IntVar[] vars = vars(list);
        IntVar[][] vectors = new IntVar[vars.length * 2 - 1][1];
        int k = 0;
        for (int i = 0; i < vars.length - 1; i++) {
            vectors[k++] = new IntVar[]{vars[i]};
            vectors[k++] = new IntVar[]{vars[i].add(var(lengths[i])).intVar()};
        }
        vectors[k] = new IntVar[]{vars[vars.length - 1]};
        lexCtr(vectors, operator);
    }

    @Override
    public void buildCtrLex(String id, XVariables.XVarInteger[][] lists, Types.TypeOperatorRel operator) {
        lexCtr(vars(lists), operator);
    }

    private void lexCtr(IntVar[][] vectors, Types.TypeOperatorRel operator) {
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
        }
    }

    @Override
    public void buildCtrChannel(String id, XVariables.XVarInteger[] list, int startIndex) {
        model.inverseChanneling(vars(list), vars(list), startIndex, startIndex).post();
    }

    @Override
    public void buildCtrChannel(String id, XVariables.XVarInteger[] list1, int startIndex1, XVariables.XVarInteger[] list2, int startIndex2) {
        if (list1.length == list2.length) {
            model.inverseChanneling(vars(list1), vars(list2), startIndex1, startIndex2).post();
        } else if (list1.length < list2.length) {
            IntVar[] x = vars(list1);
            IntVar[] y = vars(list2);
            for (int xi = 0; xi < x.length; xi++) {
                model.element(model.intVar(xi + startIndex1), y, x[xi], startIndex2).post();
            }
        } else {
            XCallbacks2.super.buildCtrChannel(id, list1, startIndex1, list2, startIndex2);
        }
    }

    @Override
    public void buildCtrChannel(String id, XVariables.XVarInteger[] list, int startIndex, XVariables.XVarInteger value) {
        model.boolsIntChanneling(bools(list), var(value), startIndex).post();
    }


    @Override
    public void buildCtrNoOverlap(String id, XVariables.XVarInteger[] origins, int[] lengths, boolean zeroIgnored) {
        // disjunctive
        model.cumulative(
                IntStream.range(0, origins.length)
                        .mapToObj(i -> model.taskVar(var(origins[i]), lengths[i]))
                        .toArray(Task[]::new),
                model.intVarArray(origins.length, 1, 1),
                model.intVar(1)
        ).post();
    }

    @Override
    public void buildCtrNoOverlap(String id, XVariables.XVarInteger[] origins, XVariables.XVarInteger[] lengths, boolean zeroIgnored) {
        // disjunctive
        model.cumulative(
                IntStream.range(0, origins.length)
                        .mapToObj(i -> model.taskVar(var(origins[i]), var(lengths[i])))
                        .toArray(Task[]::new),
                model.intVarArray(origins.length, 1, 1),
                model.intVar(1)
        ).post();
    }

    @Override
    public void buildCtrNoOverlap(String id, XVariables.XVarInteger[][] origins, int[][] lengths, boolean zeroIgnored) {
        if (origins[0].length == 2) {
            IntVar[] X = Arrays.stream(origins).map(o -> var(o[0])).toArray(IntVar[]::new);
            IntVar[] Y = Arrays.stream(origins).map(o -> var(o[1])).toArray(IntVar[]::new);
            IntVar[] W = Arrays.stream(lengths).map(l -> model.intVar(l[0])).toArray(IntVar[]::new);
            IntVar[] H = Arrays.stream(lengths).map(l -> model.intVar(l[1])).toArray(IntVar[]::new);
            model.diffN(X, Y, W, H, true).post();
        } else {
            XCallbacks2.super.buildCtrNoOverlap(id, origins, lengths, zeroIgnored);
        }
    }

    @Override
    public void buildCtrNoOverlap(String id, XVariables.XVarInteger[][] origins, XVariables.XVarInteger[][] lengths, boolean zeroIgnored) {
        if (origins[0].length == 2) {
            IntVar[] X = Arrays.stream(origins).map(o -> var(o[0])).toArray(IntVar[]::new);
            IntVar[] Y = Arrays.stream(origins).map(o -> var(o[1])).toArray(IntVar[]::new);
            IntVar[] W = Arrays.stream(lengths).map(l -> var(l[0])).toArray(IntVar[]::new);
            IntVar[] H = Arrays.stream(lengths).map(l -> var(l[1])).toArray(IntVar[]::new);
            model.diffN(X, Y, W, H, true).post();
        } else {
            XCallbacks2.super.buildCtrNoOverlap(id, origins, lengths, zeroIgnored);
        }
    }

    @Override
    public void buildCtrCumulative(String id, XVariables.XVarInteger[] origins, int[] lengths, int[] heights, Condition condition) {
        if (condition instanceof Condition.ConditionRel) {
            Condition.ConditionRel conditionRel = (Condition.ConditionRel) condition;
            switch (conditionRel.operator) {
                case LT: {
                    model.cumulative(
                            IntStream.range(0, origins.length)
                                    .mapToObj(i -> model.taskVar(var(origins[i]), lengths[i]))
                                    .toArray(Task[]::new),
                            IntStream.range(0, origins.length)
                                    .mapToObj(i -> model.intVar(heights[i]))
                                    .toArray(IntVar[]::new),
                            model.intOffsetView(condV(condition), -1)
                    ).post();
                }
                return;
                case LE: {
                    model.cumulative(
                            IntStream.range(0, origins.length)
                                    .mapToObj(i -> model.taskVar(var(origins[i]), lengths[i]))
                                    .toArray(Task[]::new),
                            IntStream.range(0, origins.length)
                                    .mapToObj(i -> model.intVar(heights[i]))
                                    .toArray(IntVar[]::new),
                            condV(condition)
                    ).post();
                }
                return;
            }
        }
        XCallbacks2.super.buildCtrCumulative(id, origins, lengths, heights, condition);
    }

    @Override
    public void buildCtrCumulative(String id, XVariables.XVarInteger[] origins, int[] lengths, XVariables.XVarInteger[] heights, Condition condition) {
        if (condition instanceof Condition.ConditionRel) {
            Condition.ConditionRel conditionRel = (Condition.ConditionRel) condition;
            switch (conditionRel.operator) {
                case LT: {
                    model.cumulative(
                            IntStream.range(0, origins.length)
                                    .mapToObj(i -> model.taskVar(var(origins[i]), lengths[i]))
                                    .toArray(Task[]::new),
                            vars(heights),
                            model.intOffsetView(condV(condition), -1)
                    ).post();
                }
                return;
                case LE: {
                    model.cumulative(
                            IntStream.range(0, origins.length)
                                    .mapToObj(i -> model.taskVar(var(origins[i]), lengths[i]))
                                    .toArray(Task[]::new),
                            vars(heights),
                            condV(condition)
                    ).post();
                }
                return;
            }
        }
        XCallbacks2.super.buildCtrCumulative(id, origins, lengths, heights, condition);
    }

    @Override
    public void buildCtrCumulative(String id, XVariables.XVarInteger[] origins, XVariables.XVarInteger[] lengths, int[] heights, Condition condition) {
        if (condition instanceof Condition.ConditionRel) {
            Condition.ConditionRel conditionRel = (Condition.ConditionRel) condition;
            switch (conditionRel.operator) {
                case LT: {
                    model.cumulative(
                            IntStream.range(0, origins.length)
                                    .mapToObj(i -> model.taskVar(var(origins[i]), var(lengths[i])))
                                    .toArray(Task[]::new),
                            IntStream.range(0, origins.length)
                                    .mapToObj(i -> model.intVar(heights[i]))
                                    .toArray(IntVar[]::new),
                            model.intOffsetView(condV(condition), -1)
                    ).post();
                }
                return;
                case LE: {
                    model.cumulative(
                            IntStream.range(0, origins.length)
                                    .mapToObj(i -> model.taskVar(var(origins[i]), var(lengths[i])))
                                    .toArray(Task[]::new),
                            IntStream.range(0, origins.length)
                                    .mapToObj(i -> model.intVar(heights[i]))
                                    .toArray(IntVar[]::new),
                            condV(condition)
                    ).post();
                }
                return;
            }
        }
        XCallbacks2.super.buildCtrCumulative(id, origins, lengths, heights, condition);
    }

    @Override
    public void buildCtrCumulative(String id, XVariables.XVarInteger[] origins, XVariables.XVarInteger[] lengths, XVariables.XVarInteger[] heights, Condition condition) {
        if (condition instanceof Condition.ConditionRel) {
            Condition.ConditionRel conditionRel = (Condition.ConditionRel) condition;
            switch (conditionRel.operator) {
                case LT: {
                    model.cumulative(
                            IntStream.range(0, origins.length)
                                    .mapToObj(i -> model.taskVar(var(origins[i]), var(lengths[i])))
                                    .toArray(Task[]::new),
                            vars(heights),
                            model.intOffsetView(condV(condition), -1)
                    ).post();
                }
                return;
                case LE: {
                    model.cumulative(
                            IntStream.range(0, origins.length)
                                    .mapToObj(i -> model.taskVar(var(origins[i]), var(lengths[i])))
                                    .toArray(Task[]::new),
                            vars(heights),
                            condV(condition)
                    ).post();
                }
                return;
            }
        }
        XCallbacks2.super.buildCtrCumulative(id, origins, lengths, heights, condition);
    }

    @Override
    public void buildCtrBinPacking(String id, XVariables.XVarInteger[] list, int[] sizes, Condition condition) {
        model.binPacking(vars(list), sizes, new IntVar[]{condV(condition)}, 0);
    }

    @Override
    public void buildCtrBinPacking(String id, XVariables.XVarInteger[] list, int[] sizes, Condition[] conditions, int startIndex) {
        IntVar[] cds = new IntVar[list.length];
        for (int i = 0; i < cds.length; i++) {
            cds[i] = condV(conditions[i]);
        }
        model.binPacking(vars(list), sizes, cds, startIndex);
    }

    @Override
    public void buildCtrInstantiation(String id, XVariables.XVarInteger[] list, int[] values) {
        Tuples tuples = new Tuples(true);
        tuples.add(values);
        model.table(vars(list), tuples).post();
    }

    private void dealWithCondition(Condition condition, int min, int max, Consumer<IntVar> postCtr) {
        if (condition instanceof Condition.ConditionRel) {
            Condition.ConditionRel conditionRel = (Condition.ConditionRel) condition;
            switch (conditionRel.operator) {
                case LT: {
                    IntVar res = model.intVar(min, max);
                    postCtr.accept(res);
                    res.lt(condV(condition)).post();
                }
                break;
                case LE: {
                    IntVar res = model.intVar(min, max);
                    postCtr.accept(res);
                    res.le(condV(condition)).post();
                }
                break;
                case GE: {
                    IntVar res = model.intVar(min, max);
                    postCtr.accept(res);
                    res.ge(condV(condition)).post();
                }
                break;
                case GT: {
                    IntVar res = model.intVar(min, max);
                    postCtr.accept(res);
                    res.gt(condV(condition)).post();
                }
                break;
                case NE: {
                    IntVar res = model.intVar(min, max);
                    postCtr.accept(res);
                    res.ne(condV(condition)).post();
                }
                break;
                case EQ:
                    postCtr.accept(condV(condition));
                    break;
            }
        } else if (condition instanceof Condition.ConditionSet) {
            Condition.ConditionSet conditionSet = (Condition.ConditionSet) condition;
            switch (conditionSet.operator) {
                case IN: {
                    IntVar res;
                    if (condition instanceof Condition.ConditionIntvl) {
                        res = model.intVar((int) ((Condition.ConditionIntvl) condition).min, (int) ((Condition.ConditionIntvl) condition).max);
                    } else {
                        res = condV(condition);
                    }
                    postCtr.accept(res);
                }
                break;
                case NOTIN: {
                    IntVar res = model.intVar(min, max);
                    postCtr.accept(res);
                    res.ne(condV(condition)).post();
                    notin(res, condition);
                }
                break;
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////// GROUP ///////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    @Override
    public Implem implem() {
        return implem;
    }

    @Override
    public void loadGroup(XConstraints.XGroup g) {
        beginGroup(g);
        if (g.template instanceof XConstraints.XCtr)
            loadCtrs((XConstraints.XCtr) g.template, g.argss, g);
        else if (g.template instanceof XConstraints.XLogic && ((XConstraints.XLogic) g.template).getType() == Types.TypeCtr.not) {
            XConstraints.CEntryReifiable child = ((XConstraints.XLogic) g.template).components[0];
            // http://sofdem.github.io/gccat/aux/pdf/not_all_equal.pdf
            Stream.of(g.argss).forEach(o -> model.notAllEqual(vars((XVariables.XVarInteger[]) o)).post());
        } else
            unimplementedCase(g);
        endGroup(g);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////// ANNOTATIONS /////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void buildAnnotationDecision(XVariables.XVarInteger[] list) {
        model.addHook("decisions", vars(list));
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////// OBJECTIVE ///////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private IntVar optSum(IntVar[] vars) {
        int[] bounds = VariableUtils.boundsForAddition(vars);
        IntVar res = model.intVar("SUM", bounds[0], bounds[1], true);
        model.sum(vars, "=", res).post();
        return res;
    }

    private IntVar optScalar(IntVar[] vars, int[] coeffs) {
        int[] bounds = VariableUtils.boundsForScalar(vars, coeffs);
        IntVar res = model.intVar("SCALAR", bounds[0], bounds[1], true);
        model.scalar(vars, coeffs, "=", res).post();
        return res;
    }

    private IntVar optMin(IntVar[] vars) {
        int[] bounds = VariableUtils.boundsForMinimum(vars);
        IntVar res = model.intVar("MIN", bounds[0], bounds[1]);
        model.min(res, vars).post();
        return res;
    }

    private IntVar optMax(IntVar[] vars) {
        int[] bounds = VariableUtils.boundsForMaximum(vars);
        IntVar res = model.intVar("MAX", bounds[0], bounds[1]);
        model.max(res, vars).post();
        return res;
    }

    private IntVar optNValues(IntVar[] vars) {
        IntVar res = model.intVar("NVALUES", 0, vars.length);
        model.nValues(vars, res).post();
        return res;
    }

    private void buildObjective(boolean maximize, Types.TypeObjective type, IntVar[] vars) {
        switch (type) {
            case SUM:
                model.setObjective(maximize, optSum(vars));
                break;
            case MINIMUM:
                model.setObjective(maximize, optMin(vars));
                break;
            case MAXIMUM:
                model.setObjective(maximize, optMax(vars));
                break;
            case NVALUES:
                model.setObjective(maximize, optNValues(vars));
                break;
            case EXPRESSION:
            case PRODUCT:
            case LEX:
                throw new UnsupportedOperationException("Unknown objective");
        }
    }

    private void buildObjective(boolean maximize, Types.TypeObjective type, IntVar[] vars, int[] coeffs) {
        // Only SUM supports coeffs
        switch (type) {
            case SUM:
                model.setObjective(maximize, optScalar(vars, coeffs));
                break;
            case MINIMUM:
            case MAXIMUM:
            case NVALUES:
            case EXPRESSION:
            case PRODUCT:
            case LEX:
                throw new UnsupportedOperationException("Unknown objective");
        }
    }

    @Override
    public void buildObjToMinimize(String id, XVariables.XVarInteger x) {
        model.setObjective(false, var(x));
    }

    @Override
    public void buildObjToMaximize(String id, XVariables.XVarInteger x) {
        model.setObjective(true, var(x));
    }

    @Override
    public void buildObjToMaximize(String id, XNodeParent<XVariables.XVarInteger> tree) {
        model.setObjective(true, buildAr(tree).intVar());
    }

    @Override
    public void buildObjToMinimize(String id, XNodeParent<XVariables.XVarInteger> tree) {
        model.setObjective(false, buildAr(tree).intVar());
    }

    @Override
    public void buildObjToMinimize(String id, Types.TypeObjective type, XVariables.XVarInteger[] list) {
        buildObjective(false, type, vars(list));
    }

    @Override
    public void buildObjToMaximize(String id, Types.TypeObjective type, XVariables.XVarInteger[] list) {
        buildObjective(true, type, vars(list));
    }

    @Override
    public void buildObjToMinimize(String id, Types.TypeObjective type, XNode<XVariables.XVarInteger>[] trees) {
        buildObjective(false, type, vars(trees));
    }

    @Override
    public void buildObjToMaximize(String id, Types.TypeObjective type, XNode<XVariables.XVarInteger>[] trees) {
        buildObjective(true, type, vars(trees));
    }

    @Override
    public void buildObjToMinimize(String id, Types.TypeObjective type, XVariables.XVarInteger[] list, int[] coeffs) {
        buildObjective(false, type, vars(list), coeffs);
    }


    @Override
    public void buildObjToMaximize(String id, Types.TypeObjective type, XVariables.XVarInteger[] list, int[] coeffs) {
        buildObjective(true, type, vars(list), coeffs);
    }

    @Override
    public void buildObjToMinimize(String id, Types.TypeObjective type, XNode<XVariables.XVarInteger>[] trees, int[] coeffs) {
        buildObjective(false, type, vars(trees), coeffs);
    }

    @Override
    public void buildObjToMaximize(String id, Types.TypeObjective type, XNode<XVariables.XVarInteger>[] trees, int[] coeffs) {
        buildObjective(true, type, vars(trees), coeffs);
    }
}
