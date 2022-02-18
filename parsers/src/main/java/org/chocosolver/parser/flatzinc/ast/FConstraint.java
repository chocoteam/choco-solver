/*
 * This file is part of choco-parsers, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.parser.flatzinc.ast;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.hash.TIntHashSet;
import org.chocosolver.parser.flatzinc.ast.expression.EAnnotation;
import org.chocosolver.parser.flatzinc.ast.expression.ESetBounds;
import org.chocosolver.parser.flatzinc.ast.expression.Expression;
import org.chocosolver.parser.flatzinc.ast.propagators.PropBoolSumEq0Reif;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.extension.Tuples;
import org.chocosolver.solver.constraints.nary.automata.FA.FiniteAutomaton;
import org.chocosolver.solver.constraints.nary.cnf.LogOp;
import org.chocosolver.solver.expression.discrete.logical.LoExpression;
import org.chocosolver.solver.expression.discrete.logical.NaLoExpression;
import org.chocosolver.solver.variables.*;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableRangeSet;
import org.chocosolver.util.tools.VariableUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*
 * User : CPRUDHOM
 * Mail : cprudhom(a)emn.fr
 * Date : 12 janv. 2010
 * Since : Choco 2.1.1
 *
 * Constraint builder from flatzinc-like object.
 */
@SuppressWarnings("Duplicates")
public enum FConstraint {

    array_bool_and {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            BoolVar[] as = exps.get(0).toBoolVarArray(model);
            BoolVar r = exps.get(1).boolVarValue(model);
            if (as.length == 0) {
                r.eq(1).post();
            } else {
                if (r.isInstantiatedTo(0)) {
                    model.addClausesBoolAndArrayEqualFalse(as);
                } else {
                    model.addClausesBoolAndArrayEqVar(as, r);
                }
            }
        }
    },
    array_bool_element {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            IntVar b = exps.get(0).intVarValue(model);
            int[] as = exps.get(1).toIntArray();
            IntVar c = exps.get(2).intVarValue(model);
            model.element(c, as, b, 1).post();

        }
    },
    array_bool_or {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            BoolVar[] as = exps.get(0).toBoolVarArray(model);
            BoolVar r = exps.get(1).boolVarValue(model);

            if (as.length == 0) {
                r.eq(1).post();
            } else {
                if (r.isInstantiatedTo(1)) {
                    model.addClausesBoolOrArrayEqualTrue(as);
                } else {
                    model.addClausesBoolOrArrayEqVar(as, r);
                }
            }

        }
    },
    array_bool_xor {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            BoolVar[] as = exps.get(0).toBoolVarArray(model);

            int[] values = new int[as.length % 2 == 0 ? as.length / 2 : (as.length + 1) / 2];
            for (int i = 0, j = 1; i < values.length; i++, j += 2) {
                values[i] = j;
            }
            IntVar res = model.intVar(model.generateName(), values);
            model.sum(as, "=", res).post();

        }
    },
    array_int_element {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            IntVar b = exps.get(0).intVarValue(model);
            int[] as = exps.get(1).toIntArray();
            IntVar c = exps.get(2).intVarValue(model);
            model.element(c, as, b, 1).post();

        }
    },
    array_var_bool_element {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            IntVar b = exps.get(0).intVarValue(model);
            IntVar[] as = exps.get(1).toIntVarArray(model);
            IntVar c = exps.get(2).intVarValue(model);
            model.element(c, as, b, 1).post();

        }
    },
    array_var_int_element {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            IntVar b = exps.get(0).intVarValue(model);
            IntVar[] as = exps.get(1).toIntVarArray(model);
            IntVar c = exps.get(2).intVarValue(model);
            model.element(c, as, b, 1).post();

        }
    },
    bool2int {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            BoolVar bVar = exps.get(0).boolVarValue(model);
            IntVar iVar = exps.get(1).intVarValue(model);
            if (iVar.isBool()) {
                model.addClausesBoolEq(bVar, (BoolVar) iVar);
                return;
            }
            model.arithm(bVar, "=", iVar).post();

        }
    },
    bool_and {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            BoolVar a = exps.get(0).boolVarValue(model);
            BoolVar b = exps.get(1).boolVarValue(model);
            BoolVar r = exps.get(2).boolVarValue(model);
            model.addClausesBoolAndEqVar(a, b, r);

        }
    },
    bool_clause {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            BoolVar[] as = exps.get(0).toBoolVarArray(model);
            BoolVar[] bs = exps.get(1).toBoolVarArray(model);
            model.addClauses(as, bs);

        }
    },
    bool_eq {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            BoolVar a = exps.get(0).boolVarValue(model);
            BoolVar b = exps.get(1).boolVarValue(model);
            model.addClausesBoolEq(a, b);
        }
    },
    bool_eq_reif {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            BoolVar a = exps.get(0).boolVarValue(model);
            BoolVar b = exps.get(1).boolVarValue(model);
            BoolVar r = exps.get(2).boolVarValue(model);
            model.addClausesBoolIsEqVar(a, b, r);

        }
    },
    bool_le {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            BoolVar a = exps.get(0).boolVarValue(model);
            BoolVar b = exps.get(1).boolVarValue(model);
            model.addClausesBoolLe(a, b);
        }
    },
    bool_le_reif {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            BoolVar a = exps.get(0).boolVarValue(model);
            BoolVar b = exps.get(1).boolVarValue(model);
            BoolVar r = exps.get(2).boolVarValue(model);
            model.addClausesBoolIsLeVar(a, b, r);

        }
    },
    bool_lin_eq {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            int[] as = exps.get(0).toIntArray();
            IntVar[] bs = exps.get(1).toIntVarArray(model);
            IntVar c = exps.get(2).intVarValue(model);
            model.scalar(bs, as, "=", c).post();
        }
    },
    bool_lin_le {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            int[] as = exps.get(0).toIntArray();
            IntVar[] bs = exps.get(1).toIntVarArray(model);
            IntVar c = exps.get(2).intVarValue(model);
            model.scalar(bs, as, "<=", c).post();
        }
    },
    bool_lt {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            BoolVar a = exps.get(0).boolVarValue(model);
            BoolVar b = exps.get(1).boolVarValue(model);
            model.addClausesBoolLt(a, b);

        }
    },
    bool_lt_reif {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            BoolVar a = exps.get(0).boolVarValue(model);
            BoolVar b = exps.get(1).boolVarValue(model);
            BoolVar r = exps.get(2).boolVarValue(model);
            model.addClausesBoolIsLtVar(a, b, r);
        }
    },
    bool_not {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            BoolVar a = exps.get(0).boolVarValue(model);
            BoolVar b = exps.get(1).boolVarValue(model);
            model.addClausesBoolNot(a, b);

        }
    },
    bool_or {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            BoolVar a = exps.get(0).boolVarValue(model);
            BoolVar b = exps.get(1).boolVarValue(model);
            BoolVar r = exps.get(2).boolVarValue(model);
            model.addClausesBoolOrEqVar(a, b, r);

        }
    },
    bool_xor {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            BoolVar a = exps.get(0).boolVarValue(model);
            BoolVar b = exps.get(1).boolVarValue(model);
            BoolVar r = exps.get(2).boolVarValue(model);
            model.addClausesBoolIsNeqVar(a, b, r);

        }
    },
    int_abs {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            IntVar a = exps.get(0).intVarValue(model);
            IntVar b = exps.get(1).intVarValue(model);
            model.absolute(b, a).post();

        }
    },
    int_div {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            IntVar a = exps.get(0).intVarValue(model);
            IntVar b = exps.get(1).intVarValue(model);
            IntVar c = exps.get(2).intVarValue(model);
            model.div(a, b, c).post();

        }
    },
    int_eq {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            IntVar a = exps.get(0).intVarValue(model);
            IntVar b = exps.get(1).intVarValue(model);
            if (((a.getTypeAndKind() & Variable.KIND) == Variable.BOOL) && ((b.getTypeAndKind() & Variable.KIND) == Variable.BOOL)) {
                model.addClausesBoolEq((BoolVar) a, (BoolVar) b);
            } else {
                model.arithm(a, "=", b).post();
            }

        }
    },
    int_eq_reif {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            IntVar a = exps.get(0).intVarValue(model);
            IntVar b = exps.get(1).intVarValue(model);
            final BoolVar r = exps.get(2).boolVarValue(model);
            // this constraint is not poster, hence not returned, because it is reified
            if ((a.getTypeAndKind() & Variable.KIND) == Variable.BOOL && ((b.getTypeAndKind() & Variable.KIND) == Variable.BOOL)) {
                model.addClausesBoolIsEqVar((BoolVar) a, (BoolVar) b, r);
            } else {
                if (a.isInstantiated() || b.isInstantiated()) {
                    IntVar x;
                    int c;
                    if (a.isInstantiated()) {
                        x = b;
                        c = a.getValue();
                    } else {
                        x = a;
                        c = b.getValue();
                    }
                    model.reifyXeqC(x, c, r);
                } else {
                    model.reifyXeqY(a, b, r);
                }
            }
        }
    },
    int_le {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            IntVar a = exps.get(0).intVarValue(model);
            IntVar b = exps.get(1).intVarValue(model);
            if ((a.getTypeAndKind() & Variable.KIND) == Variable.BOOL && ((b.getTypeAndKind() & Variable.KIND) == Variable.BOOL)) {
                model.addClausesBoolLe((BoolVar) a, (BoolVar) b);
            } else {
                model.arithm(a, "<=", b).post();
            }

        }
    },
    int_le_reif {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            IntVar a = exps.get(0).intVarValue(model);
            IntVar b = exps.get(1).intVarValue(model);
            final BoolVar r = exps.get(2).boolVarValue(model);
            // this constraint is not poster, hence not returned, because it is reified
            if ((a.getTypeAndKind() & Variable.KIND) == Variable.BOOL && ((b.getTypeAndKind() & Variable.KIND) == Variable.BOOL)) {
                model.addClausesBoolIsLeVar((BoolVar) a, (BoolVar) b, r);
            } else {
                if (a.isInstantiated() || b.isInstantiated()) {
                    final IntVar var;
                    final int cste;
                    if (a.isInstantiated()) {
                        var = b;
                        cste = a.getValue();
                        model.reifyXgtC(var, cste - 1, r);
//                            model.arithm(a, "<=", b).reifyWith(r);
                    } else {
                        var = a;
                        cste = b.getValue();
                        model.reifyXltC(var, cste + 1, r);
//                            model.arithm(a, "<=", b).reifyWith(r);
                    }
                } else {
                    model.reifyXltYC(a, b, +1, r);
                }
            }
        }
    },
    int_lin_eq {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            int[] as = exps.get(0).toIntArray();
            IntVar[] bs = exps.get(1).toIntVarArray(model);
            IntVar c = exps.get(2).intVarValue(model);
            if (bs.length > 0) {
                model.scalar(bs, as, "=", c).post();
            }

        }
    },
    int_lin_eq_reif {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            int[] as = exps.get(0).toIntArray();
            IntVar[] bs = exps.get(1).toIntVarArray(model);
            IntVar c = exps.get(2).intVarValue(model);
            BoolVar r = exps.get(3).boolVarValue(model);

            if ((boolean) model.getSettings().get("adhocReification")) {
                if (bs.length == 1) {
                    if (bs[0].isInstantiated() || c.isInstantiated()) {
                        IntVar x;
                        int t;
                        if (bs[0].isInstantiated()) {
                            x = c;
                            t = bs[0].getValue();
                        } else {
                            x = bs[0];
                            t = c.getValue();
                        }
                        model.reifyXeqC(x, t, r);
                    } else {
                        model.reifyXeqY(bs[0], c, r);
                    }
                    return;
                } else {
                    // detect boolSumEq bool reified
                    int n = bs.length;
                    boolean boolSum = c.isBool();
                    for (int i = 0; i < n; i++) {
                        boolSum &= bs[i].isBool();
                        boolSum &= as[i] == 1;
                    }
                    if (boolSum && c.isInstantiatedTo(0)) {
                        BoolVar[] bbs = new BoolVar[n + 1];
                        for (int i = 0; i < n; i++) {
                            bbs[i] = (BoolVar) bs[i];
                        }
                        bbs[bs.length] = r;
                        new Constraint("BoolSumLeq0Reif", new PropBoolSumEq0Reif(bbs)).post();
                        return;
                    }
                }
            }
            model.scalar(bs, as, "=", c).reifyWith(r);
        }
    },
    int_lin_le {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            int[] as = exps.get(0).toIntArray();
            IntVar[] bs = exps.get(1).toIntVarArray(model);
            IntVar c = exps.get(2).intVarValue(model);
            model.scalar(bs, as, "<=", c).post();
        }
    },
    int_lin_le_reif {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            int[] as = exps.get(0).toIntArray();
            IntVar[] bs = exps.get(1).toIntVarArray(model);
            IntVar c = exps.get(2).intVarValue(model);
            BoolVar r = exps.get(3).boolVarValue(model);
            if (c.isInstantiatedTo(0)) {
                // detect boolSumLeq 0 reified
                int n = bs.length;
                boolean boolSum = c.isBool();
                for (int i = 0; i < n; i++) {
                    boolSum &= bs[i].isBool();
                    boolSum &= as[i] == 1;
                }
                if (boolSum) {
                    BoolVar[] bbs = new BoolVar[n + 1];
                    for (int i = 0; i < n; i++) {
                        bbs[i] = (BoolVar) bs[i];
                    }
                    bbs[bs.length] = r;
                    new Constraint("BoolSumEq0Reif", new PropBoolSumEq0Reif(bbs)).post();
                    return;
                }
            } else if (c.isInstantiated()) {
                if (bs.length == 1) {
                    if (as[0] == -1) {
                        model.reifyXgtC(bs[0], -(c.getValue() + 1), r);
                        return;
                    }
                    if (as[0] == 1) {
                        model.reifyXltC(bs[0], c.getValue() + 1, r);
                        return;
                    }
                }
                if (bs.length == 2) {
                    if (as[0] == -1 && as[1] == 1) {
                        model.arithm(bs[1], "<=", bs[0], "+", c.getValue()).reifyWith(r);
                        return;
                    }
                    if (as[0] == 1 && as[1] == -1) {
                        model.reifyXltYC(bs[0], bs[1], c.getValue() + 1, r);
                        return;
                    }
                }
            }
            model.scalar(bs, as, "<=", c).reifyWith(r);
        }
    },
    int_lin_ne {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            int[] as = exps.get(0).toIntArray();
            IntVar[] bs = exps.get(1).toIntVarArray(model);
            IntVar c = exps.get(2).intVarValue(model);
            model.scalar(bs, as, "!=", c).post();

        }
    },
    int_lin_ne_reif {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            int[] as = exps.get(0).toIntArray();
            IntVar[] bs = exps.get(1).toIntVarArray(model);
            IntVar c = exps.get(2).intVarValue(model);
            BoolVar r = exps.get(3).boolVarValue(model);
            if (bs.length == 1) {
                if (as[0] == 1 || as[0] == -1) {
                    model.reifyXneC(bs[0], as[0] * c.getValue(), r);
                    return;
                }
            }
            if (bs.length == 2 && c.isInstantiatedTo(0)) {
                if (as[0] == 1 && as[1] == -1) {
                    model.reifyXneY(bs[0], bs[1], r);
                    return;
                }
                if (as[0] == -1 && as[1] == 1) {
                    model.reifyXneY(bs[0], bs[1], r);
                    return;
                }
            }
            model.scalar(bs, as, "!=", c).reifyWith(r);

        }
    },
    int_lt {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            IntVar a = exps.get(0).intVarValue(model);
            IntVar b = exps.get(1).intVarValue(model);
            if ((a.getTypeAndKind() & Variable.KIND) == Variable.BOOL && ((b.getTypeAndKind() & Variable.KIND) == Variable.BOOL)) {
                model.addClausesBoolLt((BoolVar) a, (BoolVar) b);
            } else {
                model.arithm(a, "<", b).post();
            }

        }
    },
    int_lt_reif {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            IntVar a = exps.get(0).intVarValue(model);
            IntVar b = exps.get(1).intVarValue(model);
            final BoolVar r = exps.get(2).boolVarValue(model);
            // this constraint is not poster, hence not returned, because it is reified
            if ((a.getTypeAndKind() & Variable.KIND) == Variable.BOOL && ((b.getTypeAndKind() & Variable.KIND) == Variable.BOOL)) {
                model.addClausesBoolIsLtVar((BoolVar) a, (BoolVar) b, r);
            } else {
                if (a.isInstantiated() || b.isInstantiated()) {
                    final IntVar var;
                    final int cste;
                    if (a.isInstantiated()) {
                        var = b;
                        cste = a.getValue();
                        model.reifyXgtC(var, cste, r);
                    } else {
                        var = a;
                        cste = b.getValue();
                        model.reifyXltC(var, cste, r);
                    }
                } else {
                    model.reifyXltY(a, b, r);
                }
            }
        }
    },
    int_max {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            IntVar a = exps.get(0).intVarValue(model);
            IntVar b = exps.get(1).intVarValue(model);
            IntVar c = exps.get(2).intVarValue(model);
            model.max(c, a, b).post();
        }
    },
    int_min {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            IntVar a = exps.get(0).intVarValue(model);
            IntVar b = exps.get(1).intVarValue(model);
            IntVar c = exps.get(2).intVarValue(model);
            model.min(c, a, b).post();

        }
    },
    int_mod {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            IntVar a = exps.get(0).intVarValue(model);
            IntVar b = exps.get(1).intVarValue(model);
            IntVar c = exps.get(2).intVarValue(model);
            model.mod(a, b, c).post();

        }
    },
    int_ne {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            IntVar a = exps.get(0).intVarValue(model);
            IntVar b = exps.get(1).intVarValue(model);
            if ((a.getTypeAndKind() & Variable.KIND) == Variable.BOOL && ((b.getTypeAndKind() & Variable.KIND) == Variable.BOOL)) {
                model.addClausesBoolNot((BoolVar) a, (BoolVar) b);
            } else {
                model.arithm(a, "!=", b).post();
            }
        }
    },
    int_ne_reif {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            IntVar a = exps.get(0).intVarValue(model);
            IntVar b = exps.get(1).intVarValue(model);
            final BoolVar r = exps.get(2).boolVarValue(model);
            // this constraint is not poster, hence not returned, because it is reified
            if ((a.getTypeAndKind() & Variable.KIND) == Variable.BOOL && ((b.getTypeAndKind() & Variable.KIND) == Variable.BOOL)) {
                model.addClausesBoolIsNeqVar((BoolVar) a, (BoolVar) b, r);
            } else {
                if ((boolean) model.getSettings().get("adhocReification")) {
                    if (a.isInstantiated() || b.isInstantiated()) {
                        IntVar x;
                        int c;
                        if (a.isInstantiated()) {
                            x = b;
                            c = a.getValue();
                        } else {
                            x = a;
                            c = b.getValue();
                        }
                        final IntVar var = x;
                        final int cste = c;
                        model.reifyXneC(var, cste, r);
                    } else {
                        model.reifyXneY(a, b, r);
                    }
                } else {
                    model.arithm(a, "!=", b).reifyWith(r);
                }
            }
        }
    },
    int_plus {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            IntVar[] vars = new IntVar[2];
            vars[0] = exps.get(0).intVarValue(model);
            vars[1] = exps.get(1).intVarValue(model);
            IntVar c = exps.get(2).intVarValue(model);
            model.sum(vars, "=", c).post();

        }
    },
    int_pow {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            IntVar a = exps.get(0).intVarValue(model);
            IntVar b = exps.get(1).intVarValue(model);
            IntVar c = exps.get(2).intVarValue(model);
            a.pow(b).eq(c).extension().post();
        }
    },
    int_times {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            IntVar a = exps.get(0).intVarValue(model);
            IntVar b = exps.get(1).intVarValue(model);
            IntVar c = exps.get(2).intVarValue(model);
            model.times(a, b, c).post();

        }
    },
    alldifferentChoco {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            fzn_all_different_int.build(model, datas, id, exps, annotations);
        }
    },
    fzn_all_different_int {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            boolean AC = annotations.stream().anyMatch(a -> a.id.toString().equals("domain"));
            IntVar[] vars = exps.get(0).toIntVarArray(model);
            if (vars.length > 1) {
                model.allDifferent(vars, AC ? "AC" : "").post();
            }

        }
    },
    alldifferentBut0Choco {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            fzn_alldifferent_except_0.build(model, datas, id, exps, annotations);
        }
    },
    fzn_alldifferent_except_0 {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            IntVar[] vars = exps.get(0).toIntVarArray(model);
            if (vars.length > 1) {
                model.allDifferentExcept0(vars).post();
            }

        }
    },
    fzn_all_equal_int {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            IntVar[] vars = exps.get(0).toIntVarArray(model);
            if (vars.length > 1) {
                model.allEqual(vars).post();
            }
        }
    },
    fzn_all_equal_int_reif {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            IntVar[] vars = exps.get(0).toIntVarArray(model);
            BoolVar b = exps.get(1).boolVarValue(model);
            IntVar count = model.intVar(0, vars.length);
            model.atMostNValues(vars, count, false).post();
            model.reifyXeqC(count, 1, b);
        }
    },
    amongChoco {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            fzn_among.build(model, datas, id, exps, annotations);
        }
    },
    fzn_among {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            //var int: n, array[int] of var int: x, set of int: v
            int n = exps.get(0).intValue();
            IntVar[] vars = exps.get(1).toIntVarArray(model);
            int[] values = exps.get(2).toIntArray();
            model.among(model.intVar(n), vars, values).post();

        }
    },
    atleastChoco {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            fzn_at_least_int.build(model, datas, id, exps, annotations);
        }
    },
    fzn_at_least_int {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            //int: n, array[int] of var int: x, int: v
            int n = exps.get(0).intValue();
            IntVar[] x = exps.get(1).toIntVarArray(model);
            int v = exps.get(2).intValue();
            IntVar limit = model.intVar("limit_" + n, n, x.length, true);
            model.among(limit, x, new int[]{v}).post();

        }
    },
    atmostChoco {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            fzn_at_most_int.build(model, datas, id, exps, annotations);
        }
    },
    fzn_at_most_int {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            //int: n, array[int] of var int: x, int: v
            int n = exps.get(0).intValue();
            IntVar[] x = exps.get(1).toIntVarArray(model);
            int v = exps.get(2).intValue();
            IntVar limit = model.intVar("limit_" + n, 0, n, true);
            model.among(limit, x, new int[]{v}).post();

        }
    },
    bin_packingChoco {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            fzn_bin_packing.build(model, datas, id, exps, annotations);
        }
    },
    fzn_bin_packing {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            int c = exps.get(0).intValue();
            IntVar[] item_bin = exps.get(1).toIntVarArray(model);
            int[] item_size = exps.get(2).toIntArray();
            int min = Integer.MAX_VALUE / 2;
            int max = Integer.MIN_VALUE / 2;
            for (int i = 0; i < item_bin.length; i++) {
                min = Math.min(min, item_bin[i].getLB());
                max = Math.max(max, item_bin[i].getUB());
            }
            IntVar[] loads = model.intVarArray("TMPload", max - min + 1, 0, c, true);
            model.binPacking(item_bin, item_size, loads, min).post();

        }
    },
    bin_packing_capaChoco {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            fzn_bin_packing_capa.build(model, datas, id, exps, annotations);
        }
    },
    fzn_bin_packing_capa {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            int[] c = exps.get(0).toIntArray();
            IntVar[] item_bin = exps.get(1).toIntVarArray(model);
            int[] item_size = exps.get(2).toIntArray();
            for (int i = 0; i < item_bin.length; i++) {
                if (item_bin[i].getLB() < 1) {
                    model.arithm(item_bin[i], ">=", 1).post();
                }
                if (item_bin[i].getUB() > c.length) {
                    model.arithm(item_bin[i], "<=", c.length).post();
                }
            }
            IntVar[] loads = new IntVar[c.length];
            for (int i = 0; i < c.length; i++) {
                loads[i] = model.intVar("load_" + i, 0, c[i], true);
            }
            model.binPacking(item_bin, item_size, loads, 1).post();

        }
    },
    bin_packing_loadChoco {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            fzn_bin_packing_load.build(model, datas, id, exps, annotations);
        }
    },
    fzn_bin_packing_load {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            IntVar[] loads = exps.get(0).toIntVarArray(model);
            IntVar[] item_bin = exps.get(1).toIntVarArray(model);
            int[] item_size = exps.get(2).toIntArray();
            for (int i = 0; i < item_bin.length; i++) {
                if (item_bin[i].getLB() < 1) {
                    model.arithm(item_bin[i], ">=", 1).post();
                }
                if (item_bin[i].getUB() > loads.length) {
                    model.arithm(item_bin[i], "<=", loads.length).post();
                }
            }
            model.binPacking(item_bin, item_size, loads, 1).post();

        }
    },
    circuitChoco {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            IntVar[] vars = exps.get(0).toIntVarArray(model);
            if (vars.length > 0) {
                int min = vars[0].getLB();
                for (IntVar v : vars) {
                    min = Math.min(min, v.getLB());
                }
                model.circuit(vars, min).post();
            }
        }
    },
    fzn_circuit {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            int offset = exps.get(0).intValue();
            IntVar[] vars = exps.get(1).toIntVarArray(model);
            model.circuit(vars, offset).post();
        }
    },
    count_eqchoco {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            fzn_count_eq.build(model, datas, id, exps, annotations);
        }
    },
    fzn_count_eq {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            IntVar[] x = exps.get(0).toIntVarArray(model);
            IntVar y = exps.get(1).intVarValue(model);
            IntVar c = exps.get(2).intVarValue(model);
            model.count(y, x, c).post();

        }
    },
    cumulativeChoco {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            fzn_cumulative.build(model, datas, id, exps, annotations);
        }
    },
    fzn_cumulative {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            final IntVar[] starts = exps.get(0).toIntVarArray(model);
            final IntVar[] durations = exps.get(1).toIntVarArray(model);
            final IntVar[] resources = exps.get(2).toIntVarArray(model);
            final IntVar limit = exps.get(3).intVarValue(model);
            String decomp = (String) model.getHook("CUMULATIVE");
            int n = starts.length;
            switch (decomp) {
                case "GLB":
                    final IntVar[] ends = new IntVar[n];
                    Task[] tasks = new Task[n];
                    for (int i = 0; i < n; i++) {
                        ends[i] = model.intVar(starts[i].getName() + "_" + durations[i].getName(),
                                starts[i].getLB() + durations[i].getLB(),
                                starts[i].getUB() + durations[i].getUB(),
                                true);
                        assert durations[i].getUB() >= 0 && resources[i].getUB() >= 0;
                        tasks[i] = new Task(starts[i], durations[i], ends[i]);
                    }
                    model.cumulative(tasks, resources, limit, true/*, Cumulative.Filter.NAIVETIME*/).post();
                    break;
                case "MZN":
                    model.cumulativeTimeDec(starts,
                            Arrays.stream(durations).mapToInt(IntVar::getUB).toArray(),
                            Arrays.stream(resources).mapToInt(IntVar::getUB).toArray(),
                            limit.getLB());
                    break;
                case "MIC":
                    int epsilon = 1;
                    BoolVar[][] b = new BoolVar[n][];
                    BoolVar[][] b1 = new BoolVar[n][];
                    BoolVar[][] b2 = new BoolVar[n][];
                    for (int i = 0; i < n; i++) {
                        TIntArrayList sumC = new TIntArrayList();
                        ArrayList<IntVar> sumV = new ArrayList<>();
                        b[i] = new BoolVar[n - 1];
                        b1[i] = new BoolVar[n - 1];
                        b2[i] = new BoolVar[n - 1];

                        for (int j = 0, k = 0; j < n; j++) {
                            if (i != j) {
                                b[i][k] = model.boolVar();
                                b1[i][k] = model.boolVar();
                                b2[i][k] = model.boolVar();
                                // sum constraint
                                assert resources[j].isInstantiated() : "resources not fixed";
                                sumC.add(resources[j].getValue());
                                sumV.add(b[i][k]);
                                // bij <=> bij1 and bij2
                                model.scalar(
                                        new BoolVar[]{b[i][k], b1[i][k]},
                                        new int[]{1, -1},
                                        "<=", 0
                                ).post();
                                model.scalar(
                                        new BoolVar[]{b[i][k], b2[i][k]},
                                        new int[]{1, -1},
                                        "<=", 0
                                ).post();

                                model.scalar(
                                        new BoolVar[]{b[i][k], b1[i][k], b2[i][k]},
                                        new int[]{1, -1, -1},
                                        ">=", -1
                                ).post();

                                // b1ij <=> start[j] <= start[i]
                                int m = starts[j].getLB() - starts[i].getUB();
                                int M = starts[j].getUB() - starts[i].getLB();
                                model.scalar(
                                        new IntVar[]{starts[j], starts[i], b1[i][k]},
                                        new int[]{1, -1, M},
                                        "<=", M
                                ).post();
                                model.scalar(
                                        new IntVar[]{starts[j], starts[i], b1[i][k]},
                                        new int[]{1, -1, -m + 1},
                                        ">=", epsilon
                                ).post();
                                // b2ij <=> start[i] <= start[j] + dur[j] - epsilon
                                //      <=> start[i] - start[j] - dur[j] <= - epsilon
                                assert durations[j].isInstantiated() : "durations not fixed";
                                m = starts[i].getLB() - (starts[j].getUB() + durations[j].getValue()) + epsilon;
                                M = starts[i].getUB() - (starts[j].getLB() + durations[j].getLB()) + epsilon;
                                //System.out.println("m = "+m+", M = "+M);
                                model.scalar(
                                        new IntVar[]{starts[i], starts[j], b2[i][k]},
                                        new int[]{1, -1, M},
                                        "<=", M - epsilon + durations[j].getValue()
                                ).post();
                                model.scalar(
                                        new IntVar[]{starts[i], starts[j], b2[i][k]},
                                        new int[]{1, -1, -m + 1},
                                        ">=", durations[j].getValue()
                                ).post();
                                k++;
                            }
                        }
                        model.scalar(
                                sumV.toArray(new IntVar[0]),
                                sumC.toArray(),
                                "<=",
                                -resources[i].getValue() + limit.getValue()).post();
                    }
                    break;
            }
        }
    },
    diffnChoco {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            fzn_diffn.build(model, datas, id, exps, annotations);
        }
    },
    fzn_diffn {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            IntVar[] x = exps.get(0).toIntVarArray(model);
            IntVar[] y = exps.get(1).toIntVarArray(model);
            IntVar[] dx = exps.get(2).toIntVarArray(model);
            IntVar[] dy = exps.get(3).toIntVarArray(model);
            if (x.length > 1) {
                model.diffN(x, y, dx, dy, true).post();
            }

        }
    },
    distributeChoco {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            fzn_distribute.build(model, datas, id, exps, annotations);
        }
    },
    fzn_distribute {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            IntVar[] card = exps.get(0).toIntVarArray(model);
            IntVar[] value = exps.get(1).toIntVarArray(model);
            IntVar[] base = exps.get(2).toIntVarArray(model);
            for (int i = 0; i < card.length; i++) {
                model.count(value[i], base, card[i]).post();
            }

        }
    },
    exactlyChoco {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            fzn_exactly_int.build(model, datas, id, exps, annotations);
        }
    },
    fzn_exactly_int {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            //int: n, array[int] of var int: x, int: v
            int n = exps.get(0).intValue();
            IntVar[] x = exps.get(1).toIntVarArray(model);
            int v = exps.get(2).intValue();
            model.among(model.intVar(n), x, new int[]{v}).post();

        }
    },
    globalCardinalityChoco {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            choco_fzn_global_cardinality.build(model, datas, id, exps, annotations);
        }
    },
    choco_fzn_global_cardinality {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            IntVar[] vars = exps.get(0).toIntVarArray(model);
            int[] values = exps.get(1).toIntArray();
            IntVar[] cards = exps.get(2).toIntVarArray(model);
            boolean closed = exps.get(3).boolValue();
            model.globalCardinality(vars, values, cards, closed).post();

        }
    },
    globalCardinalityLowUpChoco {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            choco_fzn_global_cardinality_low_up.build(model, datas, id, exps, annotations);
        }
    },
    choco_fzn_global_cardinality_low_up {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            IntVar[] vars = exps.get(0).toIntVarArray(model);
            int[] values = exps.get(1).toIntArray();
            int[] low = exps.get(2).toIntArray();
            int[] up = exps.get(3).toIntArray();
            boolean closed = exps.get(4).boolValue();
            IntVar[] cards = new IntVar[low.length];
            for (int i = 0; i < low.length; i++) {
                cards[i] = model.intVar("card of val " + values[i], low[i], up[i], true);
            }
            model.globalCardinality(vars, values, cards, closed).post();

        }
    },
    inverseChoco {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            fzn_inverse.build(model, datas, id, exps, annotations);
        }
    },
    fzn_inverse {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            boolean AC = annotations.stream().anyMatch(a -> a.id.toString().equals("domain"));
            IntVar[] x = exps.get(0).toIntVarArray(model);
            IntVar[] y = exps.get(1).toIntVarArray(model);
            model.inverseChanneling(x, y, 1, 1, AC).post();

        }
    },
    knapsackChoco {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            fzn_knapsack.build(model, datas, id, exps, annotations);
        }
    },
    fzn_knapsack {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            int[] w = exps.get(0).toIntArray();
            int[] p = exps.get(1).toIntArray();
            IntVar[] x = exps.get(2).toIntVarArray(model);
            IntVar W = exps.get(3).intVarValue(model);
            IntVar P = exps.get(4).intVarValue(model);

            model.knapsack(x, W, P, w, p).post();
        }
    },
    lex2Choco {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            choco_fzn_lex2.build(model, datas, id, exps, annotations);
        }
    },
    choco_fzn_lex2 {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            IntVar[] xs = exps.get(0).toIntVarArray(model);
            boolean strict = exps.get(1).boolValue();
            int le = (int) Math.sqrt(xs.length);
            assert le * le == xs.length;
            IntVar[][] ys = new IntVar[le][le];
            for (int i = 0; i < le; i++) {
                ys[i] = Arrays.copyOfRange(xs, le * i, le * (i + 1));
            }
            if (strict) {
                model.lexChainLess(ys).post();
            } else {
                model.lexChainLessEq(ys).post();
            }

        }
    },
    lex_lessChoco {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            choco_fzn_lex_less.build(model, datas, id, exps, annotations);
        }
    },
    choco_fzn_lex_less {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            IntVar[] xs = exps.get(0).toIntVarArray(model);
            IntVar[] ys = exps.get(1).toIntVarArray(model);
            boolean strict = exps.get(2).boolValue();
            if (strict) {
                model.lexLess(xs, ys).post();
            } else {
                model.lexLessEq(xs, ys).post();
            }

        }
    },
    maximumChoco {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            fzn_maximum_int.build(model, datas, id, exps, annotations);
        }
    },
    fzn_maximum_int {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            // var int: m, array[int] of var int: x
            IntVar m = exps.get(0).intVarValue(model);
            IntVar[] x = exps.get(1).toIntVarArray(model);
            model.max(m, x).post();

        }
    },
    memberChoco {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            fzn_member_int.build(model, datas, id, exps, annotations);
        }
    },
    fzn_member_int {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            int[] x = exps.get(0).toIntArray();
            IntVar y = exps.get(1).intVarValue(model);
            model.member(y, x).post();

        }
    },
    memberVarChoco {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            fzn_member_int_var.build(model, datas, id, exps, annotations);
        }
    },
    fzn_member_int_var {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            IntVar[] x = exps.get(0).toIntVarArray(model);
            IntVar y = exps.get(1).intVarValue(model);
            model.element(y, x, model.intVar(model.generateName(), 0, x.length - 1, false), 0).post();

        }
    },
    memberReifChoco {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            fzn_member_int_reif.build(model, datas, id, exps, annotations);
        }
    },
    fzn_member_int_reif {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            int[] x = exps.get(0).toIntArray();
            IntVar y = exps.get(1).intVarValue(model);
            BoolVar b = exps.get(2).boolVarValue(model);
            model.member(y, x).reifyWith(b);

        }
    },
    memberVarReifChoco {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            fzn_member_int_var_reif.build(model, datas, id, exps, annotations);
        }
    },
    fzn_member_int_var_reif {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            IntVar[] xs = exps.get(0).toIntVarArray(model);
            IntVar y = exps.get(1).intVarValue(model);
            BoolVar b = exps.get(2).boolVarValue(model);

            ArrayList<BoolVar> eqs = new ArrayList<>();
            for (IntVar x : xs) {
                if (VariableUtils.intersect(x, y)) {
                    eqs.add(model.arithm(x, "=", y).reify());
                }
            }
            if (eqs.size() == 0) {
                model.arithm(b, "=", 0).post();
            } else {
                model.addClausesBoolOrArrayEqVar(eqs.toArray(new BoolVar[0]), b);
            }
        }
    },
    minimumChoco {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            fzn_minimum_int.build(model, datas, id, exps, annotations);
        }
    },
    fzn_minimum_int {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            // var int: m, array[int] of var int: x
            IntVar m = exps.get(0).intVarValue(model);
            IntVar[] x = exps.get(1).toIntVarArray(model);
            model.min(m, x).post();

        }
    },
    nvalueChoco {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            fzn_nvalue.build(model, datas, id, exps, annotations);
        }
    },
    fzn_nvalue {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            IntVar nValues = exps.get(0).intVarValue(model);
            IntVar[] vars = exps.get(1).toIntVarArray(model);
            model.nValues(vars, nValues).post();

        }
    },
    regularChoco {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            fzn_regular.build(model, datas, id, exps, annotations);
        }
    },
    fzn_regular {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            //        array[int] of var int: x, int: Q, int: S,
            //        array[int,int] of int: d, int: q0, set of int: F
            IntVar[] vars = exps.get(0).toIntVarArray(model);
            int Q = exps.get(1).intValue();
            int S = exps.get(2).intValue();
            int[] d = exps.get(3).toIntArray();
            int q0 = exps.get(4).intValue();
            int[] F = exps.get(5).toIntArray();
            FiniteAutomaton auto = new FiniteAutomaton();
            for (int q = 0; q <= Q; q++) auto.addState();
            auto.setInitialState(q0);
            auto.setFinal(F);

            for (int i = 0, k = 0; i < Q; i++) {
                for (int j = 0; j < S; j++, k++) {
                    // 0 is the fail state;
                    if (d[k] > 0) {
                        auto.addTransition(i + 1, d[k], j + 1);
                    }
                }
            }
            //        auto.removeDeadTransitions();
            //        auto.minimize();

            model.regularDec(vars, auto);//.post();
//            model.regular(vars, auto).post();

        }
    },
    sortChoco {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            fzn_sort.build(model, datas, id, exps, annotations);
        }
    },
    fzn_sort {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            IntVar[] xs = exps.get(0).toIntVarArray(model);
            IntVar[] ys = exps.get(1).toIntVarArray(model);
            model.sort(xs, ys).post();

        }
    },
    subcircuitChoco {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            IntVar[] vars = exps.get(0).toIntVarArray(model);
            if (vars.length > 0) {
                int min = vars[0].getLB();
                for (IntVar v : vars) {
                    min = Math.min(min, v.getLB());
                }
                model.subCircuit(vars, min, model.intVar("length", 0, vars.length, true)).post();
            }
        }
    },
    fzn_subcircuit {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            int offset = exps.get(0).intValue();
            IntVar[] vars = exps.get(1).toIntVarArray(model);
            model.subCircuit(vars, offset, model.intVar("length", 0, vars.length, true)).post();
        }
    },
    tableChoco {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            choco_fzn_table.build(model, datas, id, exps, annotations);
        }
    },
    choco_fzn_table {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            // array[int] of var int: x, array[int, int] of int: t
            IntVar[] x = exps.get(0).toIntVarArray(model);
            int[] f_t = exps.get(1).toIntArray();
            int d2 = x.length;
            int d1 = f_t.length / d2;
            List<int[]> t = new ArrayList<>();
            for (int i = 0; i < d1; i++) {
                t.add(Arrays.copyOfRange(f_t, i * d2, (i + 1) * d2));
            }
            Tuples tuples = new Tuples(true);
            for (int[] couple : t) {
                tuples.add(couple);
            }
            if (x.length == 2) {
                model.table(x[0], x[1], tuples).post();
            } else {
                model.table(x, tuples).post();
            }
        }
    },
    value_precede_chain_intChoco {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            fzn_value_precede_chain_int.build(model, datas, id, exps, annotations);
        }
    },
    fzn_value_precede_chain_int {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            int[] c = exps.get(0).toIntArray();
            IntVar[] x = exps.get(1).toIntVarArray(model);
            model.intValuePrecedeChain(x, c).post();

        }
    },
    fzn_value_precede_int {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            int s = exps.get(0).intValue();
            int t = exps.get(1).intValue();
            IntVar[] x = exps.get(2).toIntVarArray(model);
            model.intValuePrecedeChain(x, s, t).post();

        }
    },
    count_eq_reif_choco {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            fzn_count_eq_reif.build(model, datas, id, exps, annotations);
        }
    },
    fzn_count_eq_reif {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            IntVar[] decVars = exps.get(0).toIntVarArray(model);
            IntVar valVar = exps.get(1).intVarValue(model);
            IntVar countVar = exps.get(2).intVarValue(model);
            BoolVar b = exps.get(3).boolVarValue(model);
            Constraint cstr;
            if (valVar.isInstantiated()) {
                IntVar nbOcc = model.intVar(model.generateName(), 0, decVars.length, true);
                cstr = model.count(valVar.getValue(), decVars, nbOcc);
                model.reifyXeqY(nbOcc, countVar, b);
            } else {
                IntVar value = model.intVar(model.generateName(), valVar.getLB(), valVar.getUB());
                cstr = model.count(value, decVars, countVar);
                model.reifyXeqY(value, valVar, b);
            }
            cstr.post();

        }
    },
    fzn_count_geq_reif {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            IntVar[] decVars = exps.get(0).toIntVarArray(model);
            IntVar valVar = exps.get(1).intVarValue(model);
            IntVar countVar = exps.get(2).intVarValue(model);
            BoolVar b = exps.get(3).boolVarValue(model);
            Constraint cstr;
            if (valVar.isInstantiated()) {
                IntVar nbOcc = model.intVar(model.generateName(), 0, decVars.length, true);
                cstr = model.count(valVar.getValue(), decVars, nbOcc);
                model.reifyXgeY(nbOcc, countVar, b);
            } else {
                IntVar value = model.intVar(model.generateName(), valVar.getLB(), valVar.getUB());
                cstr = model.count(value, decVars, countVar);
                model.reifyXgeY(value, valVar, b);
            }
            cstr.post();

        }
    },
    fzn_count_gt_reif {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            IntVar[] decVars = exps.get(0).toIntVarArray(model);
            IntVar valVar = exps.get(1).intVarValue(model);
            IntVar countVar = exps.get(2).intVarValue(model);
            BoolVar b = exps.get(3).boolVarValue(model);
            Constraint cstr;
            if (valVar.isInstantiated()) {
                IntVar nbOcc = model.intVar(model.generateName(), 0, decVars.length, true);
                cstr = model.count(valVar.getValue(), decVars, nbOcc);
                model.reifyXgtY(nbOcc, countVar, b);
            } else {
                IntVar value = model.intVar(model.generateName(), valVar.getLB(), valVar.getUB());
                cstr = model.count(value, decVars, countVar);
                model.reifyXgtY(value, valVar, b);
            }
            cstr.post();

        }
    },
    fzn_count_leq_reif {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            IntVar[] decVars = exps.get(0).toIntVarArray(model);
            IntVar valVar = exps.get(1).intVarValue(model);
            IntVar countVar = exps.get(2).intVarValue(model);
            BoolVar b = exps.get(3).boolVarValue(model);
            Constraint cstr;
            if (valVar.isInstantiated()) {
                IntVar nbOcc = model.intVar(model.generateName(), 0, decVars.length, true);
                cstr = model.count(valVar.getValue(), decVars, nbOcc);
                model.reifyXleY(nbOcc, countVar, b);
            } else {
                IntVar value = model.intVar(model.generateName(), valVar.getLB(), valVar.getUB());
                cstr = model.count(value, decVars, countVar);
                model.reifyXleY(value, valVar, b);
            }
            cstr.post();

        }
    },
    fzn_count_lt_reif {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            IntVar[] decVars = exps.get(0).toIntVarArray(model);
            IntVar valVar = exps.get(1).intVarValue(model);
            IntVar countVar = exps.get(2).intVarValue(model);
            BoolVar b = exps.get(3).boolVarValue(model);
            Constraint cstr;
            if (valVar.isInstantiated()) {
                IntVar nbOcc = model.intVar(model.generateName(), 0, decVars.length, true);
                cstr = model.count(valVar.getValue(), decVars, nbOcc);
                model.reifyXltY(nbOcc, countVar, b);
            } else {
                IntVar value = model.intVar(model.generateName(), valVar.getLB(), valVar.getUB());
                cstr = model.count(value, decVars, countVar);
                model.reifyXltY(value, valVar, b);
            }
            cstr.post();

        }
    },
    fzn_count_neq_reif {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            IntVar[] decVars = exps.get(0).toIntVarArray(model);
            IntVar valVar = exps.get(1).intVarValue(model);
            IntVar countVar = exps.get(2).intVarValue(model);
            BoolVar b = exps.get(3).boolVarValue(model);
            Constraint cstr;
            if (valVar.isInstantiated()) {
                IntVar nbOcc = model.intVar(model.generateName(), 0, decVars.length, true);
                cstr = model.count(valVar.getValue(), decVars, nbOcc);
                model.reifyXneY(nbOcc, countVar, b);
            } else {
                IntVar value = model.intVar(model.generateName(), valVar.getLB(), valVar.getUB());
                cstr = model.count(value, decVars, countVar);
                model.reifyXneY(value, valVar, b);
            }
            cstr.post();

        }
    },
    array_set_element {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            array_var_set_element.build(model, datas, id, exps, annotations);

        }
    },
    array_var_set_element {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            IntVar b = exps.get(0).intVarValue(model);
            SetVar[] as = exps.get(1).toSetVarArray(model);
            SetVar c = exps.get(2).setVarValue(model);
            model.element(b, as, 1, c).post();

        }
    },
    set_card {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            SetVar a = exps.get(0).setVarValue(model);
            IntVar b = exps.get(1).intVarValue(model);
            a.setCard(b);

        }
    },
    set_diff {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            SetVar a = exps.get(0).setVarValue(model);
            SetVar b = exps.get(1).setVarValue(model);
            SetVar c = exps.get(1).setVarValue(model);
            model.partition(new SetVar[]{c, b}, a).post();

        }
    },
    set_eq {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            SetVar a = exps.get(0).setVarValue(model);
            SetVar b = exps.get(1).setVarValue(model);
            model.allEqual(a, b).post();

        }
    },
    set_eq_reif {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            SetVar a = exps.get(0).setVarValue(model);
            SetVar b = exps.get(1).setVarValue(model);
            BoolVar r = exps.get(2).boolVarValue(model);
            model.allEqual(a, b).reifyWith(r);

        }
    },
    set_in {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            IntVar var = exps.get(0).intVarValue(model);
            if (exps.get(1).getTypeOf().equals(Expression.EType.SET_L)) {
                int[] values = exps.get(1).toIntArray();
                model.member(var, values).post();
            } else if (exps.get(1).getTypeOf().equals(Expression.EType.SET_B)) {
                int low = ((ESetBounds) exps.get(1)).getLow();
                int upp = ((ESetBounds) exps.get(1)).getUpp();
                model.member(var, low, upp).post();
            } else {
                SetVar b = exps.get(1).setVarValue(model);
                model.member(var, b).post();
            }

        }
    },
    set_in_reif {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            IntVar a = exps.get(0).intVarValue(model);
            BoolVar r = exps.get(2).boolVarValue(model);
            if (exps.get(1).getTypeOf().equals(Expression.EType.SET_L)) {
                IntIterableRangeSet set = new IntIterableRangeSet(exps.get(1).toIntArray());
                model.reifyXinS(a, set, r);
            } else if (exps.get(1).getTypeOf().equals(Expression.EType.SET_B)) {
                int low = ((ESetBounds) exps.get(1)).getLow();
                int upp = ((ESetBounds) exps.get(1)).getUpp();
                IntIterableRangeSet set = new IntIterableRangeSet(low, upp);
                model.reifyXinS(a, set, r);
            } else {
                SetVar b = exps.get(1).setVarValue(model);
                model.member(a, b).reifyWith(r);
            }
        }
    },
    set_intersect {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            SetVar a = exps.get(0).setVarValue(model);
            SetVar b = exps.get(1).setVarValue(model);
            SetVar c = exps.get(2).setVarValue(model);
            model.intersection(new SetVar[]{a, b}, c).post();

        }
    },
    set_le {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            SetVar a = exps.get(0).setVarValue(model);
            SetVar b = exps.get(1).setVarValue(model);

            SetVar ab = model.setVar(model.generateName(), a.getLB().toArray(), a.getUB().toArray());
            SetVar ba = model.setVar(model.generateName(), b.getLB().toArray(), b.getUB().toArray());

            TIntHashSet values = new TIntHashSet();
            for (int i : a.getUB()) {
                values.add(i);
            }
            for (int i : b.getUB()) {
                values.add(i);
            }
            int[] env = values.toArray();
            Arrays.sort(env);
            SetVar c = model.setVar(model.generateName(), new int[]{}, env);
            IntVar min = model.intVar(model.generateName(), env[0], env[env.length - 1]);

            BoolVar _b1 = model.subsetEq(a, b).reify();

            model.post(model.partition(new SetVar[]{ab, b}, a),
                    model.partition(new SetVar[]{ba, a}, b),
                    model.union(new SetVar[]{ab, ba}, c));

            model.min(c, min, false);
            BoolVar _b2 = model.member(min, a).reify();

            model.addClausesAtMostNMinusOne(new BoolVar[]{_b1, _b2});

        }
    },
    set_lt {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            SetVar a = exps.get(0).setVarValue(model);
            SetVar b = exps.get(1).setVarValue(model);

            SetVar ab = model.setVar(model.generateName(), a.getLB().toArray(), a.getUB().toArray());
            SetVar ba = model.setVar(model.generateName(), b.getLB().toArray(), b.getUB().toArray());

            TIntHashSet values = new TIntHashSet();
            for (int i : a.getUB()) {
                values.add(i);
            }
            for (int i : b.getUB()) {
                values.add(i);
            }
            int[] env = values.toArray();
            Arrays.sort(env);
            SetVar c = model.setVar(model.generateName(), new int[]{}, env);
            IntVar min = model.intVar(model.generateName(), env[0], env[env.length - 1]);

            BoolVar _b1 = model.subsetEq(a, b).reify();
            BoolVar _b2 = model.allDifferent(a, b).reify();

            model.post(model.partition(new SetVar[]{ab, b}, a),
                    model.partition(new SetVar[]{ba, a}, b),
                    model.union(new SetVar[]{ab, ba}, c));
            model.min(c, min, false);
            BoolVar _b3 = model.member(min, a).reify();

            model.addClauses(LogOp.or(_b3, LogOp.and(_b1, _b2)));

        }
    },
    set_ne {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            SetVar a = exps.get(0).setVarValue(model);
            SetVar b = exps.get(1).setVarValue(model);
            model.allDifferent(a, b).post();

        }
    },
    set_ne_reif {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            SetVar a = exps.get(0).setVarValue(model);
            SetVar b = exps.get(1).setVarValue(model);
            BoolVar r = exps.get(2).boolVarValue(model);
            model.allDifferent(a, b).reifyWith(r);

        }
    },
    set_subset {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            SetVar a = exps.get(0).setVarValue(model);
            SetVar b = exps.get(1).setVarValue(model);
            model.subsetEq(a, b).post();

        }
    },
    set_subset_reif {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            SetVar a = exps.get(0).setVarValue(model);
            SetVar b = exps.get(1).setVarValue(model);
            BoolVar r = exps.get(2).boolVarValue(model);
            model.subsetEq(a, b).reifyWith(r);

        }
    },
    set_symdiff {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            SetVar a = exps.get(0).setVarValue(model);
            SetVar b = exps.get(1).setVarValue(model);
            SetVar c = exps.get(2).setVarValue(model);
            SetVar ab = model.setVar(model.generateName(), a.getLB().toArray(), a.getUB().toArray());
            SetVar ba = model.setVar(model.generateName(), b.getLB().toArray(), b.getUB().toArray());
            model.partition(new SetVar[]{ab, b}, a).post();
            model.partition(new SetVar[]{ba, a}, b).post();
            model.union(new SetVar[]{ab, ba}, c).post();

        }
    },
    set_union {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            SetVar a = exps.get(0).setVarValue(model);
            SetVar b = exps.get(1).setVarValue(model);
            SetVar c = exps.get(2).setVarValue(model);
            model.union(new SetVar[]{a, b}, c).post();

        }
    },
    fzn_if_then_else_bool {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            BoolVar[] c = exps.get(0).toBoolVarArray(model);
            int[] x = exps.get(1).toIntArray();
            BoolVar y = exps.get(2).boolVarValue(model);
            model.ifThenElseDec(c, x, y);
        }
    },
    fzn_if_then_else_int {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            BoolVar[] c = exps.get(0).toBoolVarArray(model);
            int[] x = exps.get(1).toIntArray();
            IntVar y = exps.get(2).intVarValue(model);
            model.ifThenElseDec(c, x, y);
        }
    },
    fzn_if_then_else_var_bool {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            BoolVar[] c = exps.get(0).toBoolVarArray(model);
            BoolVar[] x = exps.get(1).toBoolVarArray(model);
            BoolVar y = exps.get(2).boolVarValue(model);
            model.ifThenElseDec(c, x, y);
        }
    },
    fzn_if_then_else_var_int {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            BoolVar[] c = exps.get(0).toBoolVarArray(model);
            IntVar[] x = exps.get(1).toIntVarArray(model);
            IntVar y = exps.get(2).intVarValue(model);
            model.ifThenElseDec(c, x, y);
        }
    },
    fzn_maximum_arg_bool {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            BoolVar[] x = exps.get(0).toBoolVarArray(model);
            IntVar z = exps.get(1).intVarValue(model);
            model.argmaxDec(z, 1, x);
        }
    },
    fzn_maximum_arg_int {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            IntVar[] x = exps.get(0).toIntVarArray(model);
            IntVar z = exps.get(1).intVarValue(model);
            model.argmaxDec(z, 1, x);
        }
    },
    fzn_minimum_arg_bool {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            BoolVar[] x = exps.get(0).toBoolVarArray(model);
            IntVar z = exps.get(1).intVarValue(model);
            model.argminDec(z, 1, x);
        }
    },
    fzn_minimum_arg_int {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            IntVar[] x = exps.get(0).toIntVarArray(model);
            IntVar z = exps.get(1).intVarValue(model);
            model.argminDec(z, 1, x);
        }
    },
    // redefinitions.mzn
    array_bool_and_imp {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            BoolVar[] as = exps.get(0).toBoolVarArray(model);
            BoolVar r = exps.get(1).boolVarValue(model);
            new NaLoExpression(LoExpression.Operator.AND, as).decompose().impliedBy(r);
        }
    },
    array_bool_or_imp {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            BoolVar[] as = exps.get(0).toBoolVarArray(model);
            BoolVar r = exps.get(1).boolVarValue(model);
            new NaLoExpression(LoExpression.Operator.OR, as).decompose().impliedBy(r);
        }
    },
    array_bool_xor_imp {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            BoolVar[] as = exps.get(0).toBoolVarArray(model);
            BoolVar r = exps.get(1).boolVarValue(model);
            new NaLoExpression(LoExpression.Operator.XOR, as).decompose().impliedBy(r);
        }
    },
    bool_and_imp {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            BoolVar a = exps.get(0).boolVarValue(model);
            BoolVar b = exps.get(1).boolVarValue(model);
            BoolVar r = exps.get(2).boolVarValue(model);
            a.and(b).decompose().impliedBy(r);
        }
    },
    bool_clause_imp {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            BoolVar[] as = exps.get(0).toBoolVarArray(model);
            BoolVar[] bs = exps.get(1).toBoolVarArray(model);
            BoolVar r = exps.get(2).boolVarValue(model);
            int PL = as.length;
            int NL = bs.length;
            BoolVar[] LITS = new BoolVar[PL + NL];
            System.arraycopy(as, 0, LITS, 0, PL);
            for (int i = 0; i < NL; i++) {
                LITS[i + PL] = bs[i].not();
            }
            model.sum(LITS, ">", 0).impliedBy(r);
        }
    },
    bool_ge_imp {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            BoolVar a = exps.get(0).boolVarValue(model);
            BoolVar b = exps.get(1).boolVarValue(model);
            BoolVar r = exps.get(2).boolVarValue(model);
            a.ge(b).decompose().impliedBy(r);
        }
    },
    bool_gt_imp {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            BoolVar a = exps.get(0).boolVarValue(model);
            BoolVar b = exps.get(1).boolVarValue(model);
            BoolVar r = exps.get(2).boolVarValue(model);
            a.gt(b).decompose().impliedBy(r);
        }
    },

    bool_le_imp {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            BoolVar a = exps.get(0).boolVarValue(model);
            BoolVar b = exps.get(1).boolVarValue(model);
            BoolVar r = exps.get(2).boolVarValue(model);
            a.le(b).decompose().impliedBy(r);
        }
    },
    bool_lt_imp {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            BoolVar a = exps.get(0).boolVarValue(model);
            BoolVar b = exps.get(1).boolVarValue(model);
            BoolVar r = exps.get(2).boolVarValue(model);
            a.lt(b).decompose().impliedBy(r);
        }
    },
    bool_eq_imp {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            BoolVar a = exps.get(0).boolVarValue(model);
            BoolVar b = exps.get(1).boolVarValue(model);
            BoolVar r = exps.get(2).boolVarValue(model);
            a.eq(b).decompose().impliedBy(r);
        }
    },
    bool_ne_imp {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            BoolVar a = exps.get(0).boolVarValue(model);
            BoolVar b = exps.get(1).boolVarValue(model);
            BoolVar r = exps.get(2).boolVarValue(model);
            a.ne(b).decompose().impliedBy(r);
        }
    },
    bool_or_imp {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            BoolVar a = exps.get(0).boolVarValue(model);
            BoolVar b = exps.get(1).boolVarValue(model);
            BoolVar r = exps.get(2).boolVarValue(model);
            a.or(b).decompose().impliedBy(r);
        }
    },
    bool_xor_imp {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            BoolVar a = exps.get(0).boolVarValue(model);
            BoolVar b = exps.get(1).boolVarValue(model);
            BoolVar r = exps.get(2).boolVarValue(model);
            a.xor(b).decompose().impliedBy(r);
        }
    },
    bool_lin_eq_imp {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            int[] as = exps.get(0).toIntArray();
            BoolVar[] bs = exps.get(1).toBoolVarArray(model);
            int c = exps.get(2).intValue();
            BoolVar r = exps.get(3).boolVarValue(model);
            model.scalar(bs, as, "=", c, as.length + 1).impliedBy(r);
        }
    },
    bool_lin_ge_imp {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            int[] as = exps.get(0).toIntArray();
            BoolVar[] bs = exps.get(1).toBoolVarArray(model);
            int c = exps.get(2).intValue();
            BoolVar r = exps.get(3).boolVarValue(model);
            model.scalar(bs, as, ">=", c, as.length + 1).impliedBy(r);
        }
    },
    bool_lin_gt_imp {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            int[] as = exps.get(0).toIntArray();
            BoolVar[] bs = exps.get(1).toBoolVarArray(model);
            int c = exps.get(2).intValue();
            BoolVar r = exps.get(3).boolVarValue(model);
            model.scalar(bs, as, ">", c, as.length + 1).impliedBy(r);
        }
    },
    bool_lin_le_imp {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            int[] as = exps.get(0).toIntArray();
            BoolVar[] bs = exps.get(1).toBoolVarArray(model);
            int c = exps.get(2).intValue();
            BoolVar r = exps.get(3).boolVarValue(model);
            model.scalar(bs, as, "<=", c, as.length + 1).impliedBy(r);
        }
    },
    bool_lin_lt_imp {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            int[] as = exps.get(0).toIntArray();
            BoolVar[] bs = exps.get(1).toBoolVarArray(model);
            int c = exps.get(2).intValue();
            BoolVar r = exps.get(3).boolVarValue(model);
            model.scalar(bs, as, "<", c, as.length + 1).impliedBy(r);
        }
    },
    bool_lin_ne_imp {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            int[] as = exps.get(0).toIntArray();
            BoolVar[] bs = exps.get(1).toBoolVarArray(model);
            int c = exps.get(2).intValue();
            BoolVar r = exps.get(3).boolVarValue(model);
            model.scalar(bs, as, "!=", c, as.length + 1).impliedBy(r);
        }
    },
    int_eq_imp {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            IntVar a = exps.get(0).intVarValue(model);
            IntVar b = exps.get(1).intVarValue(model);
            BoolVar r = exps.get(2).boolVarValue(model);
            a.eq(b).decompose().impliedBy(r);
        }
    },
    int_ge_imp {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            IntVar a = exps.get(0).intVarValue(model);
            IntVar b = exps.get(1).intVarValue(model);
            BoolVar r = exps.get(2).boolVarValue(model);
            a.ge(b).decompose().impliedBy(r);
        }
    },
    int_gt_imp {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            IntVar a = exps.get(0).intVarValue(model);
            IntVar b = exps.get(1).intVarValue(model);
            BoolVar r = exps.get(2).boolVarValue(model);
            a.gt(b).decompose().impliedBy(r);
        }
    },
    int_le_imp {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            IntVar a = exps.get(0).intVarValue(model);
            IntVar b = exps.get(1).intVarValue(model);
            BoolVar r = exps.get(2).boolVarValue(model);
            a.le(b).decompose().impliedBy(r);
        }
    },
    int_lt_imp {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            IntVar a = exps.get(0).intVarValue(model);
            IntVar b = exps.get(1).intVarValue(model);
            BoolVar r = exps.get(2).boolVarValue(model);
            a.lt(b).decompose().impliedBy(r);
        }
    },
    int_ne_imp {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            IntVar a = exps.get(0).intVarValue(model);
            IntVar b = exps.get(1).intVarValue(model);
            BoolVar r = exps.get(2).boolVarValue(model);
            a.ne(b).decompose().impliedBy(r);
        }
    },
    int_lin_eq_imp {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            int[] as = exps.get(0).toIntArray();
            IntVar[] bs = exps.get(1).toIntVarArray(model);
            int c = exps.get(2).intValue();
            BoolVar r = exps.get(3).boolVarValue(model);
            model.scalar(bs, as, "=", c, as.length + 1).impliedBy(r);
        }
    },
    int_lin_ge_imp {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            int[] as = exps.get(0).toIntArray();
            IntVar[] bs = exps.get(1).toIntVarArray(model);
            int c = exps.get(2).intValue();
            BoolVar r = exps.get(3).boolVarValue(model);
            model.scalar(bs, as, ">=", c, as.length + 1).impliedBy(r);
        }
    },
    int_lin_gt_imp {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            int[] as = exps.get(0).toIntArray();
            IntVar[] bs = exps.get(1).toIntVarArray(model);
            int c = exps.get(2).intValue();
            BoolVar r = exps.get(3).boolVarValue(model);
            model.scalar(bs, as, ">", c, as.length + 1).impliedBy(r);
        }
    },
    int_lin_le_imp {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            int[] as = exps.get(0).toIntArray();
            IntVar[] bs = exps.get(1).toIntVarArray(model);
            int c = exps.get(2).intValue();
            BoolVar r = exps.get(3).boolVarValue(model);
            model.scalar(bs, as, "<=", c, as.length + 1).impliedBy(r);
        }
    },
    int_lin_lt_imp {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            int[] as = exps.get(0).toIntArray();
            IntVar[] bs = exps.get(1).toIntVarArray(model);
            int c = exps.get(2).intValue();
            BoolVar r = exps.get(3).boolVarValue(model);
            model.scalar(bs, as, "<", c, as.length + 1).impliedBy(r);
        }
    },
    int_lin_ne_imp {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            int[] as = exps.get(0).toIntArray();
            IntVar[] bs = exps.get(1).toIntVarArray(model);
            int c = exps.get(2).intValue();
            BoolVar r = exps.get(3).boolVarValue(model);
            model.scalar(bs, as, "!=", c, as.length + 1).impliedBy(r);
        }
    },
    set_in_imp {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            IntVar a = exps.get(0).intVarValue(model);
            BoolVar r = exps.get(2).boolVarValue(model);
            IntIterableRangeSet set = null;
            if (exps.get(1).getTypeOf().equals(Expression.EType.SET_L)) {
                set = new IntIterableRangeSet(exps.get(1).toIntArray());
            } else if (exps.get(1).getTypeOf().equals(Expression.EType.SET_B)) {
                int low = ((ESetBounds) exps.get(1)).getLow();
                int upp = ((ESetBounds) exps.get(1)).getUpp();
                set = new IntIterableRangeSet(low, upp);
            }
            model.member(a, set).impliedBy(r);
        }
    },

    // redefinitions-2.0.mzn
    bool_clause_reif {
        @Override
        public void build(Model model, Datas datas, String
                id, List<Expression> exps, List<EAnnotation> annotations) {
            BoolVar[] as = exps.get(0).toBoolVarArray(model);
            BoolVar[] bs = exps.get(1).toBoolVarArray(model);
            BoolVar r = exps.get(2).boolVarValue(model);
            int PL = as.length;
            int NL = bs.length;
            BoolVar[] LITS = new BoolVar[PL + NL];
            System.arraycopy(as, 0, LITS, 0, PL);
            for (int i = 0; i < NL; i++) {
                LITS[i + PL] = bs[i].not();
            }
            model.sum(LITS, ">", 0).reifyWith(r);
        }
    },

    array_int_maximum {
        @Override
        public void build(Model model, Datas datas, String
                id, List<Expression> exps, List<EAnnotation> annotations) {
            IntVar m = exps.get(0).intVarValue(model);
            IntVar[] x = exps.get(1).toIntVarArray(model);
            model.max(m, x).post();
        }
    },

    array_int_minimum {
        @Override
        public void build(Model model, Datas datas, String
                id, List<Expression> exps, List<EAnnotation> annotations) {
            IntVar m = exps.get(0).intVarValue(model);
            IntVar[] x = exps.get(1).toIntVarArray(model);
            model.min(m, x).post();
        }
    },

    // redefinitions-2.0.2.mzn
    choco_array_bool_element_nonshifted {
        @Override
        public void build(Model model, Datas datas, String
                id, List<Expression> exps, List<EAnnotation> annotations) {
            choco_array_int_element_nonshifted.build(model, datas, id, exps, annotations);
        }
    },
    choco_array_int_element_nonshifted {
        @Override
        public void build(Model model, Datas datas, String
                id, List<Expression> exps, List<EAnnotation> annotations) {
            IntVar idx = exps.get(0).intVarValue(model);
            int os = exps.get(1).intValue();
            int[] x = exps.get(2).toIntArray();
            IntVar c = exps.get(3).intVarValue(model);
            model.element(c, x, idx, os).post();
        }
    },
    choco_array_var_bool_element_nonshifted {
        @Override
        public void build(Model model, Datas datas, String
                id, List<Expression> exps, List<EAnnotation> annotations) {
            choco_array_var_int_element_nonshifted.build(model, datas, id, exps, annotations);
        }
    },
    choco_array_var_int_element_nonshifted {
        @Override
        public void build(Model model, Datas datas, String
                id, List<Expression> exps, List<EAnnotation> annotations) {
            IntVar idx = exps.get(0).intVarValue(model);
            int os = exps.get(1).intValue();
            IntVar[] x = exps.get(2).toIntVarArray(model);
            IntVar c = exps.get(3).intVarValue(model);
            model.element(c, x, idx, os).post();
        }
    },
    choco_array_var_set_element_nonshifted {
        @Override
        public void build(Model model, Datas datas, String
                id, List<Expression> exps, List<EAnnotation> annotations) {
            IntVar idx = exps.get(0).intVarValue(model);
            int os = exps.get(1).intValue();
            SetVar[] x = exps.get(2).toSetVarArray(model);
            SetVar c = exps.get(3).setVarValue(model);
            model.element(idx, x, os, c).post();
        }
    },
    // redefinitions-2.2.1.mzn
    int_pow_fixed {
        @Override
        public void build(Model model, Datas datas, String
                id, List<Expression> exps, List<EAnnotation> annotations) {
            IntVar x = exps.get(0).intVarValue(model);
            int y = exps.get(1).intValue();
            IntVar z = exps.get(2).intVarValue(model);
            Tuples tuples = new Tuples(true);
            for (int val1 : x) {
                int res = (int) Math.pow(val1, y);
                if (z.contains(res)) {
                    tuples.add(val1, res);
                }
            }
            model.table(x, z, tuples).post();
        }
    },

    // redefinitions-2.5.2.mzn
    choco_array_bool_element2d_nonshifted {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            choco_array_int_element2d_nonshifted.build(model, datas, id, exps, annotations);
        }
    },
    choco_array_int_element2d_nonshifted {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            IntVar idx1 = exps.get(0).intVarValue(model);
            int o1 = exps.get(1).intValue();
            IntVar idx2 = exps.get(2).intVarValue(model);
            int o2 = exps.get(3).intValue();
            int[] x = exps.get(4).toIntArray();
            int d1 = exps.get(5).intValue();
            int d2 = exps.get(6).intValue();
            IntVar c = exps.get(7).intVarValue(model);

            int[][] mx = new int[d1][d2];
            for (int i1 = 0; i1 < d1; i1++) {
                System.arraycopy(x, i1 * d2, mx[i1], 0, d2);
            }
            model.element(c, mx, idx1, o1, idx2, o2);
        }
    },
    choco_array_var_bool_element2d_nonshifted {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            choco_array_var_int_element2d_nonshifted.build(model, datas, id, exps, annotations);
        }
    },
    choco_array_var_int_element2d_nonshifted {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            IntVar idx1 = exps.get(0).intVarValue(model);
            int o1 = exps.get(1).intValue();
            IntVar idx2 = exps.get(2).intVarValue(model);
            int o2 = exps.get(3).intValue();
            IntVar[] x = exps.get(4).toIntVarArray(model);
            int d1 = exps.get(5).intValue();
            int d2 = exps.get(6).intValue();
            IntVar c = exps.get(7).intVarValue(model);
            IntVar[][] mx = new IntVar[d1][d2];
            for (int i1 = 0; i1 < d1; i1++) {
                System.arraycopy(x, i1 * d2, mx[i1], 0, d2);
            }
            model.element(c, mx, idx1, o1, idx2, o2);
        }
    };


    public abstract void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations);
}
