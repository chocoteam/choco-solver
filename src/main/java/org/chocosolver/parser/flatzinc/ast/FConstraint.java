/*
 * Copyright (c) 1999-2012, Ecole des Mines de Nantes
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

package org.chocosolver.parser.flatzinc.ast;

import gnu.trove.set.hash.TIntHashSet;
import org.chocosolver.parser.flatzinc.FznSettings;
import org.chocosolver.parser.flatzinc.ast.constraints.IBuilder;
import org.chocosolver.parser.flatzinc.ast.expression.EAnnotation;
import org.chocosolver.parser.flatzinc.ast.expression.ESetBounds;
import org.chocosolver.parser.flatzinc.ast.expression.Expression;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.constraints.extension.Tuples;
import org.chocosolver.solver.constraints.nary.automata.FA.FiniteAutomaton;
import org.chocosolver.solver.constraints.nary.cnf.LogOp;
import org.chocosolver.solver.constraints.nary.geost.Constants;
import org.chocosolver.solver.constraints.nary.geost.GeostOptions;
import org.chocosolver.solver.constraints.nary.geost.PropGeost;
import org.chocosolver.solver.constraints.nary.geost.externalConstraints.ExternalConstraint;
import org.chocosolver.solver.constraints.nary.geost.externalConstraints.NonOverlapping;
import org.chocosolver.solver.constraints.nary.geost.geometricPrim.GeostObject;
import org.chocosolver.solver.constraints.nary.geost.geometricPrim.ShiftedBox;
import org.chocosolver.solver.constraints.nary.sum.IntLinCombFactory;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.explanations.RuleStore;
import org.chocosolver.solver.variables.*;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.tools.ArrayUtils;
import org.chocosolver.util.tools.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

/*
* User : CPRUDHOM
* Mail : cprudhom(a)emn.fr
* Date : 12 janv. 2010
* Since : Choco 2.1.1
*
* Constraint builder from flatzinc-like object.
*/
public enum FConstraint {

    array_bool_and {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            BoolVar[] as = exps.get(0).toBoolVarArray(model);
            BoolVar r = exps.get(1).boolVarValue(model);
            if (as.length > 0) {

                switch (as.length) {
                    case 0:
                        break;
//                    case 1:
//                        solver.post(model.arithm(as[0], "=", r));
//                        break;
//                    case 2:
//                        model.arithm(as[0], "+", as[1], ">", 1).reifyWith(r);
//                        break;
                    default:
                        if (r.isInstantiatedTo(0)) {
                            model.addClausesBoolAndArrayEqualFalse(as);
                        } else {
                            model.addClausesBoolAndArrayEqVar(as, r);
                        }
                        break;
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

            switch (as.length) {
                case 0:
                    break;
                                    /*case 1:
                                        solver.post(model.arithm(as[0], "=", r));
                                        break;
                                    case 2:
                                        model.arithm(as[0], "+", as[1], ">", 0).reifyWith(r);
                                        break;*/
                default:
                    if (r.isInstantiatedTo(1)) {
                        model.addClausesBoolOrArrayEqualTrue(as);
                    } else {
                        model.addClausesBoolOrArrayEqVar(as, r);
                    }
                    break;
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
            IntVar res = model.intVar(StringUtils.randomName(), values);
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
            if (iVar.isBool() && ((FznSettings) model.getSettings()).enableClause()) {
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
            if (((FznSettings) model.getSettings()).enableClause()) {
                model.addClausesBoolEq(a, b);
            } else {
                model.arithm(a, "=", b).post();
            }

        }
    },
    bool_eq_reif {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            BoolVar a = exps.get(0).boolVarValue(model);
            BoolVar b = exps.get(1).boolVarValue(model);
            BoolVar r = exps.get(2).boolVarValue(model);
            // Adding clause seems more efficient than other alternatives
            if (((FznSettings) model.getSettings()).enableClause()) {
                model.addClausesBoolIsEqVar(a, b, r);
            } else if (model.getSettings().enableTableSubstitution()) {
                Tuples t = new Tuples(true);
                t.add(1, 1, 1);
                t.add(0, 0, 1);
                t.add(1, 0, 0);
                t.add(0, 1, 0);
                model.table(new BoolVar[]{a, b, r}, t, "default").post();
            } else {
                model.arithm(a, "=", b).reifyWith(r);
            }

        }
    },
    bool_le {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            BoolVar a = exps.get(0).boolVarValue(model);
            BoolVar b = exps.get(1).boolVarValue(model);
            if (((FznSettings) model.getSettings()).enableClause()) {
                model.addClausesBoolLe(a, b);
            } else {
                model.arithm(a, "<=", b).post();
            }

        }
    },
    bool_le_reif {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new IBuilder() {
                @Override
                public void build(Model model, String name, List<Expression> exps, List<EAnnotation> annotations, Datas datas) {
                    BoolVar a = exps.get(0).boolVarValue(model);
                    BoolVar b = exps.get(1).boolVarValue(model);
                    BoolVar r = exps.get(2).boolVarValue(model);
                    if (((FznSettings) model.getSettings()).enableClause()) {
                        model.addClausesBoolIsLeVar(a, b, r);
                    } else {
                        if (((FznSettings) model.getSettings()).adhocReification()) {
                            new Constraint("reifBool(a<b,r)", new Propagator<BoolVar>(new BoolVar[]{a, b, r}, PropagatorPriority.TERNARY, false) {
                                @Override
                                public void propagate(int evtmask) throws ContradictionException {
                                    if (vars[0].contains(0) || vars[1].contains(1)) {
                                        vars[2].setToTrue(this);
                                    }
                                    if (vars[2].getUB() == 0) {
                                        vars[0].setToTrue(this);
                                        vars[1].setToFalse(this);
                                    }
                                }

                                @Override
                                public ESat isEntailed() {
                                    throw new UnsupportedOperationException("isEntailed not implemented ");
                                }
                            }).post();
                        } else {
                            model.arithm(a, "<=", b).reifyWith(r);
                        }
                    }
                }
            }.build(model, id, exps, annotations, datas);
        }
    },
    bool_lin_eq {
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
    bool_lin_le {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            int[] as = exps.get(0).toIntArray();
            IntVar[] bs = exps.get(1).toIntVarArray(model);
            IntVar c = exps.get(2).intVarValue(model);
            if (bs.length > 0) {
                if (c.isInstantiated()) {
                    if (bs.length == 1) {
                        if (as[0] == -1) {
                            model.arithm(bs[0], ">=", -c.getValue()).post();
                            return;
                        }
                        if (as[0] == 1) {
                            model.arithm(bs[0], "<=", c.getValue()).post();
                            return;
                        }
                    }
                    if (bs.length == 2) {
                        if (as[0] == -1 && as[1] == 1) {
                            model.arithm(bs[1], "<=", bs[0], "+", c.getValue()).post();
                            return;
                        }
                        if (as[0] == 1 && as[1] == -1) {
                            model.arithm(bs[0], "<=", bs[1], "+", c.getValue()).post();
                            return;
                        }
                    }
                }
                if (((FznSettings) model.getSettings()).enableDecompositionOfLinearCombination()) {
                    int[] tmp = IntLinCombFactory.getScalarBounds(bs, as);
                    IntVar scal = model.intVar(StringUtils.randomName(), tmp[0], tmp[1], true);
                    model.scalar(bs, as, "=", scal).post();
                    model.arithm(scal, "<=", c).post();
                } else {
                    model.scalar(bs, as, "<=", c).post();
                }
            }


        }
    },
    bool_lt {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            BoolVar a = exps.get(0).boolVarValue(model);
            BoolVar b = exps.get(1).boolVarValue(model);
            if (((FznSettings) model.getSettings()).enableClause()) {
                model.addClausesBoolLt(a, b);
            } else {
                model.arithm(a, "<", b).post();
            }

        }
    },
    bool_lt_reif {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new IBuilder() {
                @Override
                public void build(Model model, String name, List<Expression> exps, List<EAnnotation> annotations, Datas datas) {
                    BoolVar a = exps.get(0).boolVarValue(model);
                    BoolVar b = exps.get(1).boolVarValue(model);
                    BoolVar r = exps.get(2).boolVarValue(model);
                    if (((FznSettings) model.getSettings()).enableClause()) {
                        model.addClausesBoolIsLtVar(a, b, r);
                    } else {
                        if (((FznSettings) model.getSettings()).adhocReification()) {
                            new Constraint("reifBool(a<b,r)", new Propagator<BoolVar>(new BoolVar[]{a, b, r}, PropagatorPriority.TERNARY, false) {
                                @Override
                                public void propagate(int evtmask) throws ContradictionException {
                                    if (!(vars[0].contains(0)) && (vars[1].contains(1))) {
                                        vars[2].setToFalse(this);
                                    }
                                    if (vars[2].getLB() == 1) {
                                        vars[0].setToFalse(this);
                                        vars[1].setToTrue(this);
                                    }
                                }

                                @Override
                                public ESat isEntailed() {
                                    throw new UnsupportedOperationException("isEntailed not implemented ");
                                }
                            }).post();
                        } else {
                            model.arithm(a, "<", b).reifyWith(r);
                        }
                    }
                }
            }.build(model, id, exps, annotations, datas);
        }
    },
    bool_not {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            BoolVar a = exps.get(0).boolVarValue(model);
            BoolVar b = exps.get(1).boolVarValue(model);
            if (((FznSettings) model.getSettings()).enableClause()) {
                model.addClausesBoolNot(a, b);
            } else {
                model.arithm(a, "!=", b).post();
            }

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
            if (((FznSettings) model.getSettings()).enableClause()
                    && ((a.getTypeAndKind() & Variable.KIND) == Variable.BOOL) && ((b.getTypeAndKind() & Variable.KIND) == Variable.BOOL)) {
                model.addClausesBoolEq((BoolVar) a, (BoolVar) b);
            } else {
                model.arithm(a, "=", b).post();
            }

        }
    },
    int_eq_reif {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new IBuilder() {
                @Override
                public void build(Model model, String name, List<Expression> exps, List<EAnnotation> annotations, Datas datas) {
                    IntVar a = exps.get(0).intVarValue(model);
                    IntVar b = exps.get(1).intVarValue(model);
                    final BoolVar r = exps.get(2).boolVarValue(model);
                    // this constraint is not poster, hence not returned, because it is reified
                    if (((FznSettings) model.getSettings()).enableClause()
                            && ((a.getTypeAndKind() & Variable.KIND) == Variable.BOOL) && ((b.getTypeAndKind() & Variable.KIND) == Variable.BOOL)) {
                        model.addClausesBoolIsEqVar((BoolVar) a, (BoolVar) b, r);
                    } else {
                        if (((FznSettings) model.getSettings()).adhocReification()) {
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
                                new Constraint("reif(a=cste,r)", new Propagator<IntVar>(new IntVar[]{x, r}, PropagatorPriority.BINARY, false) {
                                    @Override
                                    public void propagate(int evtmask) throws ContradictionException {
                                        if (r.getLB() == 1) {
                                            var.instantiateTo(cste, this);
                                            setPassive();
                                        } else {
                                            if (r.getUB() == 0) {
                                                if (var.removeValue(cste, this) || !var.contains(cste)) {
                                                    setPassive();
                                                }
                                            } else {
                                                if (var.isInstantiatedTo(cste)) {
                                                    r.setToTrue(this);
                                                    setPassive();
                                                } else if (!var.contains(cste)) {
                                                    r.setToFalse(this);
                                                    setPassive();
                                                }
                                            }
                                        }
                                    }

                                    @Override
                                    public ESat isEntailed() {
                                        //                            throw new UnsupportedOperationException("isEntailed not implemented ");
                                        return ESat.TRUE;
                                    }

                                    @Override
                                    public boolean why(RuleStore ruleStore, IntVar var, IEventType evt, int value) {
                                        boolean nrules = ruleStore.addPropagatorActivationRule(this);
                                        if (var == vars[1]) {
                                            if (vars[1].isInstantiatedTo(1)) {
                                                nrules |= ruleStore.addFullDomainRule(vars[0]);
                                            } else {
                                                nrules |= ruleStore.addRemovalRule(vars[0], cste);
                                            }
                                        } else {
                                            nrules |= ruleStore.addFullDomainRule(vars[1]);
                                        }
                                        return nrules;
                                    }
                                }).post();
                            } else {
                                new Constraint("reif(a=b,r)", new Propagator<IntVar>(new IntVar[]{a, b, r}, PropagatorPriority.TERNARY, false) {
                                    @Override
                                    public void propagate(int evtmask) throws ContradictionException {
                                        if (r.getLB() == 1) {
                                            if (vars[0].isInstantiated()) {
                                                setPassive();
                                                vars[1].instantiateTo(vars[0].getValue(), this);
                                            } else if (vars[1].isInstantiated()) {
                                                setPassive();
                                                vars[0].instantiateTo(vars[1].getValue(), this);
                                            }
                                        } else if (r.getUB() == 0) {
                                            if (vars[0].isInstantiated()) {
                                                if (vars[1].removeValue(vars[0].getValue(), this)) {
                                                    setPassive();
                                                }
                                            } else if (vars[1].isInstantiated()) {
                                                if (vars[0].removeValue(vars[1].getValue(), this)) {
                                                    setPassive();
                                                }
                                            }
                                        } else {
                                            if (vars[0].isInstantiated()) {
                                                if (vars[1].isInstantiated()) {
                                                    if (vars[0].getValue() == vars[1].getValue()) {
                                                        r.setToTrue(this);
                                                    } else {
                                                        r.setToFalse(this);
                                                    }
                                                    setPassive();
                                                } else {
                                                    if (!vars[1].contains(vars[0].getValue())) {
                                                        setPassive();
                                                        r.setToFalse(this);
                                                    }
                                                }
                                            } else {
                                                if (vars[1].isInstantiated()) {
                                                    if (!vars[0].contains(vars[1].getValue())) {
                                                        setPassive();
                                                        r.setToFalse(this);
                                                    }
                                                } else {
                                                    if (vars[0].getLB() > vars[1].getUB()
                                                            || vars[1].getLB() > vars[0].getUB()) {
                                                        setPassive();
                                                        r.setToFalse(this);
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    @Override
                                    public ESat isEntailed() {
                                        return ESat.TRUE;//throw new UnsupportedOperationException("isEntailed not implemented ");
                                    }

                                    @Override
                                    public boolean why(RuleStore ruleStore, IntVar var, IEventType evt, int value) {
                                        boolean nrules = ruleStore.addPropagatorActivationRule(this);
                                        if (var == vars[2]) {
                                            if (vars[2].isInstantiatedTo(1)) {
                                                nrules |= ruleStore.addFullDomainRule(vars[0]);
                                                nrules |= ruleStore.addFullDomainRule(vars[1]);
                                            } else {
                                                if (vars[0].isInstantiated()) {
                                                    nrules |= ruleStore.addRemovalRule(vars[1], vars[0].getValue());
                                                } else {
                                                    nrules |= ruleStore.addFullDomainRule(vars[1]);
                                                }
                                                if (vars[1].isInstantiated()) {
                                                    nrules |= ruleStore.addRemovalRule(vars[0], vars[1].getValue());
                                                } else {
                                                    nrules |= ruleStore.addFullDomainRule(vars[0]);
                                                }
                                            }
                                        } else {
                                            if (var == vars[0]) {
                                                nrules |= ruleStore.addFullDomainRule(vars[1]);
                                            } else if (var == vars[1]) {
                                                nrules |= ruleStore.addFullDomainRule(vars[0]);
                                            }
                                            nrules |= ruleStore.addFullDomainRule(vars[2]);
                                        }
                                        return nrules;
                                    }
                                }).post();
                            }
                        } else {
                            model.arithm(a, "=", b).reifyWith(r);
                        }
                    }
                }
            }.build(model, id, exps, annotations, datas);
        }
    },
    int_le {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            IntVar a = exps.get(0).intVarValue(model);
            IntVar b = exps.get(1).intVarValue(model);
            if (((FznSettings) model.getSettings()).enableClause()
                    && ((a.getTypeAndKind() & Variable.KIND) == Variable.BOOL) && ((b.getTypeAndKind() & Variable.KIND) == Variable.BOOL)) {
                model.addClausesBoolLe((BoolVar) a, (BoolVar) b);
            } else {
                model.arithm(a, "<=", b).post();
            }

        }
    },
    int_le_reif {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new IBuilder() {
                @Override
                public void build(Model model, String name, List<Expression> exps, List<EAnnotation> annotations, Datas datas) {
                    IntVar a = exps.get(0).intVarValue(model);
                    IntVar b = exps.get(1).intVarValue(model);
                    final BoolVar r = exps.get(2).boolVarValue(model);
                    // this constraint is not poster, hence not returned, because it is reified
                    if (((FznSettings) model.getSettings()).enableClause()
                            && ((a.getTypeAndKind() & Variable.KIND) == Variable.BOOL) && ((b.getTypeAndKind() & Variable.KIND) == Variable.BOOL)) {
                        model.addClausesBoolIsLeVar((BoolVar) a, (BoolVar) b, r);
                    } else {
                        if (((FznSettings) model.getSettings()).adhocReification()) {
                            if (a.isInstantiated() || b.isInstantiated()) {
                                final IntVar var;
                                final int cste;
                                if (a.isInstantiated()) {
                                    var = b;
                                    cste = a.getValue();
                                    new Constraint("reif(a>=cste,r)", new Propagator<IntVar>(new IntVar[]{var, r}, PropagatorPriority.BINARY, false) {
                                        @Override
                                        public void propagate(int evtmask) throws ContradictionException {
                                            if (r.getLB() == 1) {
                                                setPassive();
                                                var.updateLowerBound(cste, this);
                                            } else {
                                                if (r.getUB() == 0) {
                                                    if (var.updateUpperBound(cste - 1, this)) {
                                                        setPassive();
                                                    }
                                                } else {
                                                    if (var.getLB() >= cste) {
                                                        setPassive();
                                                        r.setToTrue(this);
                                                    } else if (var.getUB() < cste) {
                                                        setPassive();
                                                        r.setToFalse(this);
                                                    }
                                                }
                                            }
                                        }

                                        @Override
                                        public ESat isEntailed() {
                                            //                                throw new UnsupportedOperationException("isEntailed not implemented ");
                                            return ESat.TRUE;
                                        }

                                        @Override
                                        public boolean why(RuleStore ruleStore, IntVar var, IEventType evt, int value) {
                                            boolean nrules = ruleStore.addPropagatorActivationRule(this);
                                            if (var == vars[1]) { // r
                                                if (vars[1].isInstantiatedTo(1)) {
                                                    nrules |= ruleStore.addLowerBoundRule(vars[0]);
                                                } else {
                                                    nrules |= ruleStore.addUpperBoundRule(vars[0]);
                                                }
                                            } else { //
                                                nrules |= ruleStore.addFullDomainRule(vars[1]);
                                            }
                                            return nrules;
                                        }

                                    }).post();
                                } else {
                                    var = a;
                                    cste = b.getValue();
                                    new Constraint("reif(a<=cste,r)", new Propagator<IntVar>(new IntVar[]{var, r}, PropagatorPriority.BINARY, false) {
                                        @Override
                                        public void propagate(int evtmask) throws ContradictionException {
                                            if (r.getLB() == 1) {
                                                setPassive();
                                                var.updateUpperBound(cste, this);
                                            } else {
                                                if (r.getUB() == 0) {
                                                    if (var.updateLowerBound(cste + 1, this)) {
                                                        setPassive();
                                                    }
                                                } else {
                                                    if (var.getUB() <= cste) {
                                                        setPassive();
                                                        r.setToTrue(this);
                                                    } else if (var.getLB() > cste) {
                                                        setPassive();
                                                        r.setToFalse(this);
                                                    }
                                                }
                                            }
                                        }

                                        @Override
                                        public ESat isEntailed() {
                                            //                                throw new UnsupportedOperationException("isEntailed not implemented ");
                                            return ESat.TRUE;
                                        }

                                        @Override
                                        public boolean why(RuleStore ruleStore, IntVar var, IEventType evt, int value) {
                                            boolean nrules = ruleStore.addPropagatorActivationRule(this);
                                            if (var == vars[1]) {
                                                if (vars[1].isInstantiatedTo(1)) {
                                                    nrules |= ruleStore.addUpperBoundRule(vars[0]);
                                                } else {
                                                    nrules |= ruleStore.addLowerBoundRule(vars[0]);
                                                }
                                            } else {
                                                nrules |= ruleStore.addFullDomainRule(vars[1]);
                                            }
                                            return nrules;
                                        }
                                    }).post();
                                }
                            } else {
                                new Constraint("reif(a<=b,r)", new Propagator<IntVar>(new IntVar[]{a, b, r}, PropagatorPriority.TERNARY, false) {
                                    @Override
                                    public void propagate(int evtmask) throws ContradictionException {
                                        if (r.getLB() == 1) {
                                            vars[0].updateUpperBound(vars[1].getUB(), this);
                                            vars[1].updateLowerBound(vars[0].getLB(), this);
                                            if (vars[0].getUB() <= vars[1].getLB()) {
                                                this.setPassive();
                                            }
                                        } else {
                                            if (r.getUB() == 0) {
                                                vars[0].updateLowerBound(vars[1].getLB() + 1, this);
                                                vars[1].updateUpperBound(vars[0].getUB() - 1, this);
                                                if (vars[0].getLB() > vars[1].getUB()) {
                                                    setPassive();
                                                }
                                            } else {
                                                if (vars[0].getUB() <= vars[1].getLB()) {
                                                    setPassive();
                                                    r.setToTrue(this);
                                                } else if (vars[0].getLB() > vars[1].getUB()) {
                                                    setPassive();
                                                    r.setToFalse(this);
                                                }
                                            }
                                        }
                                    }

                                    @Override
                                    public ESat isEntailed() {
                                        throw new UnsupportedOperationException("isEntailed not implemented ");
                                    }

                                    @Override
                                    public boolean why(RuleStore ruleStore, IntVar var, IEventType evt, int value) {
                                        boolean nrules = ruleStore.addPropagatorActivationRule(this);
                                        if (var == vars[2]) {
                                            if (vars[2].isInstantiatedTo(1)) {
                                                nrules |= ruleStore.addUpperBoundRule(vars[0]);
                                                nrules |= ruleStore.addLowerBoundRule(vars[1]);
                                            } else {
                                                nrules |= ruleStore.addLowerBoundRule(vars[0]);
                                                nrules |= ruleStore.addUpperBoundRule(vars[1]);
                                            }
                                        } else {
                                            if (var == vars[0]) {
                                                if (evt == IntEventType.DECUPP) {
                                                    nrules |= ruleStore.addUpperBoundRule(vars[1]);
                                                } else {
                                                    nrules |= ruleStore.addLowerBoundRule(vars[1]);
                                                }
                                            } else if (var == vars[1]) {
                                                if (evt == IntEventType.DECUPP) {
                                                    nrules |= ruleStore.addUpperBoundRule(vars[0]);
                                                } else {
                                                    nrules |= ruleStore.addLowerBoundRule(vars[0]);
                                                }
                                            }
                                            nrules |= ruleStore.addFullDomainRule(vars[2]);
                                        }
                                        return nrules;
                                    }

                                }).post();
                            }
                        } else {
                            model.arithm(a, "<=", b).reifyWith(r);
                        }
                    }
                }
            }.build(model, id, exps, annotations, datas);
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
            new org.chocosolver.parser.flatzinc.ast.constraints.IntLinEqReifBuilder().build(model, id, exps, annotations, datas);
        }
    },
    int_lin_le {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            int[] as = exps.get(0).toIntArray();
            IntVar[] bs = exps.get(1).toIntVarArray(model);
            IntVar c = exps.get(2).intVarValue(model);
            if (bs.length > 0) {
                if (c.isInstantiated()) {
                    if (bs.length == 1) {
                        if (as[0] == -1) {
                            model.arithm(bs[0], ">=", -c.getValue()).post();
                            return;
                        }
                        if (as[0] == 1) {
                            model.arithm(bs[0], "<=", c.getValue()).post();
                            return;
                        }
                    }
                    if (bs.length == 2) {
                        if (as[0] == -1 && as[1] == 1) {
                            model.arithm(bs[1], "<=", bs[0], "+", c.getValue()).post();
                            return;
                        }
                        if (as[0] == 1 && as[1] == -1) {
                            model.arithm(bs[0], "<=", bs[1], "+", c.getValue()).post();
                            return;
                        }
                    }
                }
                if (((FznSettings) model.getSettings()).enableDecompositionOfLinearCombination()) {
                    int[] tmp = IntLinCombFactory.getScalarBounds(bs, as);
                    IntVar scal = model.intVar(StringUtils.randomName(), tmp[0], tmp[1], true);
                    model.scalar(bs, as, "=", scal).post();
                    model.arithm(scal, "<=", c).post();
                } else {
                    model.scalar(bs, as, "<=", c).post();
                }
            }


        }
    },
    int_lin_le_reif {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new org.chocosolver.parser.flatzinc.ast.constraints.IntLinLeReifBuilder().build(model, id, exps, annotations, datas);
        }
    },
    int_lin_ne {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            int[] as = exps.get(0).toIntArray();
            IntVar[] bs = exps.get(1).toIntVarArray(model);
            IntVar c = exps.get(2).intVarValue(model);

            if (bs.length > 0) {
                if (((FznSettings) model.getSettings()).enableDecompositionOfLinearCombination()) {
                    int[] tmp = IntLinCombFactory.getScalarBounds(bs, as);
                    IntVar scal = model.intVar(StringUtils.randomName(), tmp[0], tmp[1], true);
                    model.scalar(bs, as, "=", scal).post();
                    model.arithm(scal, "!=", c).post();
                } else {
                    model.scalar(bs, as, "!=", c).post();
                }
            }

        }
    },
    int_lin_ne_reif {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            int[] as = exps.get(0).toIntArray();
            IntVar[] bs = exps.get(1).toIntVarArray(model);
            IntVar c = exps.get(2).intVarValue(model);
            BoolVar r = exps.get(3).boolVarValue(model);

            if (bs.length > 0) {
                if (((FznSettings) model.getSettings()).enableDecompositionOfLinearCombination()) {
                    int[] tmp = IntLinCombFactory.getScalarBounds(bs, as);
                    IntVar scal = model.intVar(StringUtils.randomName(), tmp[0], tmp[1], true);
                    Constraint cstr = model.scalar(bs, as, "=", scal);
                    model.arithm(scal, "!=", c).reifyWith(r);
                    cstr.post();
                } else {
                    model.scalar(bs, as, "!=", c).reifyWith(r);
                }
            }

        }
    },
    int_lt {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            IntVar a = exps.get(0).intVarValue(model);
            IntVar b = exps.get(1).intVarValue(model);
            if (((FznSettings) model.getSettings()).enableClause()
                    && ((a.getTypeAndKind() & Variable.KIND) == Variable.BOOL) && ((b.getTypeAndKind() & Variable.KIND) == Variable.BOOL)) {
                model.addClausesBoolLt((BoolVar) a, (BoolVar) b);
            } else {
                model.arithm(a, "<", b).post();
            }

        }
    },
    int_lt_reif {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new IBuilder() {
                @Override
                public void build(Model model, String name, List<Expression> exps, List<EAnnotation> annotations, Datas datas) {
                    IntVar a = exps.get(0).intVarValue(model);
                    IntVar b = exps.get(1).intVarValue(model);
                    final BoolVar r = exps.get(2).boolVarValue(model);
                    // this constraint is not poster, hence not returned, because it is reified
                    if (((FznSettings) model.getSettings()).enableClause()
                            && ((a.getTypeAndKind() & Variable.KIND) == Variable.BOOL) && ((b.getTypeAndKind() & Variable.KIND) == Variable.BOOL)) {
                        model.addClausesBoolIsLtVar((BoolVar) a, (BoolVar) b, r);

                    } else if (((FznSettings) model.getSettings()).adhocReification()) {
                        if (a.isInstantiated() || b.isInstantiated()) {
                            final IntVar var;
                            final int cste;
                            if (a.isInstantiated()) {
                                var = b;
                                cste = a.getValue();
                                new Constraint("reif(b>cste,r)", new Propagator<IntVar>(new IntVar[]{var, r}, PropagatorPriority.BINARY, false) {
                                    @Override
                                    public void propagate(int evtmask) throws ContradictionException {
                                        if (r.getLB() == 1) {
                                            setPassive();
                                            var.updateLowerBound(cste + 1, this);
                                        } else if (r.getUB() == 0) {
                                            if (var.updateUpperBound(cste, this)) {
                                                setPassive();
                                            }
                                        } else {
                                            if (var.getLB() > cste) {
                                                setPassive();
                                                r.setToTrue(this);
                                            } else if (var.getUB() <= cste) {
                                                setPassive();
                                                r.setToFalse(this);
                                            }
                                        }
                                    }

                                    @Override
                                    public ESat isEntailed() {
                                        //                            throw new UnsupportedOperationException("isEntailed not implemented ");
                                        return ESat.TRUE;
                                    }

                                    @Override
                                    public boolean why(RuleStore ruleStore, IntVar var, IEventType evt, int value) {
                                        boolean nrules = ruleStore.addPropagatorActivationRule(this);
                                        if (var == vars[1]) { // r
                                            if (vars[1].isInstantiatedTo(1)) {
                                                nrules |= ruleStore.addLowerBoundRule(vars[0]);
                                            } else {
                                                nrules |= ruleStore.addUpperBoundRule(vars[0]);
                                            }
                                        } else { //
                                            nrules |= ruleStore.addFullDomainRule(vars[1]);
                                        }
                                        return nrules;
                                    }
                                }).post();
                            } else {
                                var = a;
                                cste = b.getValue();
                                new Constraint("reif(a<cste,r)", new Propagator<IntVar>(new IntVar[]{var, r}, PropagatorPriority.BINARY, false) {
                                    @Override
                                    public void propagate(int evtmask) throws ContradictionException {
                                        if (r.getLB() == 1) {
                                            setPassive();
                                            var.updateUpperBound(cste - 1, this);
                                        } else if (r.getUB() == 0) {
                                            if (var.updateLowerBound(cste, this)) {
                                                setPassive();
                                            }
                                        } else {
                                            if (var.getUB() < cste) {
                                                setPassive();
                                                r.setToTrue(this);
                                            } else if (var.getLB() >= cste) {
                                                setPassive();
                                                r.setToFalse(this);
                                            }
                                        }
                                    }

                                    @Override
                                    public ESat isEntailed() {
                                        throw new UnsupportedOperationException("isEntailed not implemented ");
                                    }

                                    @Override
                                    public boolean why(RuleStore ruleStore, IntVar var, IEventType evt, int value) {
                                        boolean nrules = ruleStore.addPropagatorActivationRule(this);
                                        if (var == vars[1]) { // r
                                            if (vars[1].isInstantiatedTo(1)) {
                                                nrules |= ruleStore.addUpperBoundRule(vars[0]);
                                            } else {
                                                nrules |= ruleStore.addLowerBoundRule(vars[0]);
                                            }
                                        } else { //
                                            nrules |= ruleStore.addFullDomainRule(vars[1]);
                                        }
                                        return nrules;
                                    }
                                }).post();
                            }
                        } else {
                            new Constraint("reif(a<b,r)", new Propagator<IntVar>(new IntVar[]{a, b, r}, PropagatorPriority.TERNARY, false) {
                                @Override
                                public void propagate(int evtmask) throws ContradictionException {
                                    if (r.getLB() == 1) {
                                        vars[0].updateUpperBound(vars[1].getUB() - 1, this);
                                        vars[1].updateLowerBound(vars[0].getLB() + 1, this);
                                        if (vars[0].getUB() < vars[1].getLB()) {
                                            this.setPassive();
                                        }
                                    } else if (r.getUB() == 0) {
                                        vars[0].updateLowerBound(vars[1].getLB(), this);
                                        vars[1].updateUpperBound(vars[0].getUB(), this);
                                        if (vars[0].getLB() >= vars[1].getUB()) {
                                            setPassive();
                                        }
                                    } else {
                                        if (vars[0].getUB() < vars[1].getLB()) {
                                            setPassive();
                                            r.setToTrue(this);
                                        } else if (vars[0].getLB() >= vars[1].getUB()) {
                                            setPassive();
                                            r.setToFalse(this);
                                        }
                                    }
                                }

                                @Override
                                public ESat isEntailed() {
                                    //                        throw new UnsupportedOperationException("isEntailed not implemented ");
                                    return ESat.TRUE;
                                }

                                @Override
                                public boolean why(RuleStore ruleStore, IntVar var, IEventType evt, int value) {
                                    boolean nrules = ruleStore.addPropagatorActivationRule(this);
                                    if (var == vars[2]) {
                                        if (vars[2].isInstantiatedTo(1)) {
                                            nrules |= ruleStore.addUpperBoundRule(vars[0]);
                                            nrules |= ruleStore.addLowerBoundRule(vars[1]);
                                        } else {
                                            nrules |= ruleStore.addLowerBoundRule(vars[0]);
                                            nrules |= ruleStore.addUpperBoundRule(vars[1]);
                                        }
                                    } else {
                                        if (var == vars[0]) {
                                            if (evt == IntEventType.DECUPP) {
                                                nrules |= ruleStore.addUpperBoundRule(vars[1]);
                                            } else {
                                                nrules |= ruleStore.addLowerBoundRule(vars[1]);
                                            }
                                        } else if (var == vars[1]) {
                                            if (evt == IntEventType.DECUPP) {
                                                nrules |= ruleStore.addUpperBoundRule(vars[0]);
                                            } else {
                                                nrules |= ruleStore.addLowerBoundRule(vars[0]);
                                            }
                                        }
                                        nrules |= ruleStore.addFullDomainRule(vars[2]);
                                    }
                                    return nrules;
                                }

                            }).post();
                        }
                    } else {
                        model.arithm(a, "<", b).reifyWith(r);
                    }
                }
            }.build(model, id, exps, annotations, datas);
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
            if (((FznSettings) model.getSettings()).enableClause()
                    && ((a.getTypeAndKind() & Variable.KIND) == Variable.BOOL) && ((b.getTypeAndKind() & Variable.KIND) == Variable.BOOL)) {
                model.addClausesBoolNot((BoolVar) a, (BoolVar) b);
            } else {
                model.arithm(a, "!=", b).post();
            }

        }
    },
    int_ne_reif {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new IBuilder() {
                @Override
                public void build(Model model, String name, List<Expression> exps, List<EAnnotation> annotations, Datas datas) {
                    IntVar a = exps.get(0).intVarValue(model);
                    IntVar b = exps.get(1).intVarValue(model);
                    final BoolVar r = exps.get(2).boolVarValue(model);
                    // this constraint is not poster, hence not returned, because it is reified
                    if (((FznSettings) model.getSettings()).enableClause()
                            && ((a.getTypeAndKind() & Variable.KIND) == Variable.BOOL) && ((b.getTypeAndKind() & Variable.KIND) == Variable.BOOL)) {
                        model.addClausesBoolIsNeqVar((BoolVar) a, (BoolVar) b, r);
                    } else {
                        if (((FznSettings) model.getSettings()).adhocReification()) {
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
                                new Constraint("reif(a!=cste,r)", new Propagator<IntVar>(new IntVar[]{x, r}, PropagatorPriority.BINARY, false) {
                                    @Override
                                    public void propagate(int evtmask) throws ContradictionException {
                                        if (r.getLB() == 1) {
                                            if (var.removeValue(cste, this)) {
                                                setPassive();
                                            }
                                        } else {
                                            if (r.getUB() == 0) {
                                                if (var.instantiateTo(cste, this)) {
                                                    setPassive();
                                                }
                                            } else {
                                                if (!var.contains(cste)) {
                                                    setPassive();
                                                    r.setToTrue(this);
                                                } else if (var.isInstantiatedTo(cste)) {
                                                    setPassive();
                                                    r.setToFalse(this);
                                                }
                                            }
                                        }
                                    }

                                    @Override
                                    public ESat isEntailed() {
                                        return ESat.TRUE;
                                        //throw new UnsupportedOperationException("isEntailed not implemented ");
                                    }

                                    @Override
                                    public boolean why(RuleStore ruleStore, IntVar var, IEventType evt, int value) {
                                        boolean nrules = ruleStore.addPropagatorActivationRule(this);
                                        if (var == vars[1]) {
                                            if (vars[1].isInstantiatedTo(1)) {
                                                nrules |= ruleStore.addRemovalRule(vars[0], cste);
                                            } else {
                                                nrules |= ruleStore.addFullDomainRule(vars[0]);
                                            }
                                        } else {
                                            nrules |= ruleStore.addFullDomainRule(vars[1]);
                                        }
                                        return nrules;
                                    }

                                }).post();
                            } else {
                                new Constraint("reif(a!=b,r)", new Propagator<IntVar>(new IntVar[]{a, b, r}, PropagatorPriority.TERNARY, false) {
                                    @Override
                                    public void propagate(int evtmask) throws ContradictionException {
                                        if (r.getLB() == 1) {
                                            if (vars[0].isInstantiated()) {
                                                if (vars[1].removeValue(vars[0].getValue(), this)) {
                                                    setPassive();
                                                }
                                            } else if (vars[1].isInstantiated()) {
                                                if (vars[0].removeValue(vars[1].getValue(), this)) {
                                                    setPassive();
                                                }
                                            }
                                        } else {
                                            if (r.getUB() == 0) {
                                                if (vars[0].isInstantiated()) {
                                                    setPassive();
                                                    vars[1].instantiateTo(vars[0].getValue(), this);
                                                } else if (vars[1].isInstantiated()) {
                                                    setPassive();
                                                    vars[0].instantiateTo(vars[1].getValue(), this);
                                                }
                                            } else {
                                                if (vars[0].isInstantiated()) {
                                                    if (vars[1].isInstantiated()) {
                                                        if (vars[0].getValue() != vars[1].getValue()) {
                                                            r.setToTrue(this);
                                                        } else {
                                                            r.setToFalse(this);
                                                        }
                                                        setPassive();
                                                    } else {
                                                        if (!vars[1].contains(vars[0].getValue())) {
                                                            r.setToTrue(this);
                                                            setPassive();
                                                        }
                                                    }
                                                } else {
                                                    if (vars[1].isInstantiated()) {
                                                        if (!vars[0].contains(vars[1].getValue())) {
                                                            r.setToTrue(this);
                                                            setPassive();
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    @Override
                                    public ESat isEntailed() {
                                        throw new UnsupportedOperationException("isEntailed not implemented ");
                                    }

                                    @Override
                                    public boolean why(RuleStore ruleStore, IntVar var, IEventType evt, int value) {
                                        boolean nrules = ruleStore.addPropagatorActivationRule(this);
                                        if (var == vars[2]) {
                                            if (vars[2].isInstantiatedTo(0)) {
                                                nrules |= ruleStore.addFullDomainRule(vars[0]);
                                                nrules |= ruleStore.addFullDomainRule(vars[1]);
                                            } else {
                                                if (vars[0].isInstantiated()) {
                                                    nrules |= ruleStore.addRemovalRule(vars[1], vars[0].getValue());
                                                } else {
                                                    nrules |= ruleStore.addFullDomainRule(vars[1]);
                                                }
                                                if (vars[1].isInstantiated()) {
                                                    nrules |= ruleStore.addRemovalRule(vars[0], vars[1].getValue());
                                                } else {
                                                    nrules |= ruleStore.addFullDomainRule(vars[0]);
                                                }
                                            }
                                        } else {
                                            if (var == vars[0]) {
                                                nrules |= ruleStore.addFullDomainRule(vars[1]);
                                            } else if (var == vars[1]) {
                                                nrules |= ruleStore.addFullDomainRule(vars[0]);
                                            }
                                            nrules |= ruleStore.addFullDomainRule(vars[2]);
                                        }
                                        return nrules;
                                    }

                                }).post();
                            }
                        } else {
                            model.arithm(a, "!=", b).reifyWith(r);
                        }
                    }
                }
            }.build(model, id, exps, annotations, datas);
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

            IntVar[] vars = exps.get(0).toIntVarArray(model);
            if (vars.length > 1) {
                model.allDifferent(vars).post();
            }

        }
    },
    alldifferentBut0Choco {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            IntVar[] vars = exps.get(0).toIntVarArray(model);
            if (vars.length > 1) {
                model.allDifferentExcept0(vars).post();
            }

        }
    },
    amongChoco {
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
    count_eqchoco {
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

            final IntVar[] starts = exps.get(0).toIntVarArray(model);
            final IntVar[] durations = exps.get(1).toIntVarArray(model);
            final IntVar[] resources = exps.get(2).toIntVarArray(model);
            final IntVar[] ends = new IntVar[starts.length];
            Task[] tasks = new Task[starts.length];
            final IntVar limit = exps.get(3).intVarValue(model);
            for (int i = 0; i < starts.length; i++) {
                ends[i] = model.intVar(starts[i].getName() + "_" + durations[i].getName(),
                        starts[i].getLB() + durations[i].getLB(),
                        starts[i].getUB() + durations[i].getUB(),
                        true);
                tasks[i] = new Task(starts[i], durations[i], ends[i]);
            }
            model.cumulative(tasks, resources, limit, true).post();

        }
    },
    diffnChoco {
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

            //int: n, array[int] of var int: x, int: v
            int n = exps.get(0).intValue();
            IntVar[] x = exps.get(1).toIntVarArray(model);
            int v = exps.get(2).intValue();
            model.among(model.intVar(n), x, new int[]{v}).post();

        }
    },
    geostChoco {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            int dim = exps.get(0).intValue();
            int[] rect_size = exps.get(1).toIntArray();
            int[] rect_offset = exps.get(2).toIntArray();
            int[][] shape = exps.get(3).toIntMatrix();
            IntVar[] x = exps.get(4).toIntVarArray(model);
            IntVar[] kind = exps.get(5).toIntVarArray(model);


            //Create Objects
            int nbOfObj = x.length / dim;
            int[] objIds = new int[nbOfObj];
            List<GeostObject> objects = new ArrayList<>();
            for (int i = 0; i < nbOfObj; i++) {
                IntVar shapeId = kind[i];
                IntVar[] coords = Arrays.copyOfRange(x, i * dim, (i + 1) * dim);
                objects.add(new GeostObject(dim, i, shapeId, coords, model.ONE(), model.ONE(), model.ONE()));
                objIds[i] = i;
            }

            //create shiftedboxes and add them to corresponding shapes
            List<ShiftedBox> shapes = new ArrayList<>();
            for (int i = 0; i < shape.length; i++) {
                for (int j = 0; j < shape[i].length; j++) {
                    int h = shape[i][j];
                    int[] l = Arrays.copyOfRange(rect_size, (h - 1) * dim, h * dim);
                    int[] t = Arrays.copyOfRange(rect_offset, (h - 1) * dim, h * dim);
                    shapes.add(new ShiftedBox(i + 1, t, l));
                }
            }

            //Create the external constraints vecotr
            List<ExternalConstraint> extcstr = new ArrayList<>(1);
            //add the external constraint of type non overlapping
            extcstr.add(new NonOverlapping(Constants.NON_OVERLAPPING, ArrayUtils.oneToN(dim), objIds));


            int originOfObjects = objects.size() * dim; //Number of domain variables to represent the origin of all objects
            int otherVariables = objects.size() * 4; //each object has 4 other variables: shapeId, start, duration; end

            //vars will be stored as follows: object 1 coords(so k coordinates), sid, start, duration, end,
            //                                object 2 coords(so k coordinates), sid, start, duration, end and so on ........
            IntVar[] vars = new IntVar[originOfObjects + otherVariables];
            for (int i = 0; i < objects.size(); i++) {
                for (int j = 0; j < dim; j++) {
                    vars[(i * (dim + 4)) + j] = objects.get(i).getCoordinates()[j];
                }
                vars[(i * (dim + 4)) + dim] = objects.get(i).getShapeId();
                vars[(i * (dim + 4)) + dim + 1] = objects.get(i).getStart();
                vars[(i * (dim + 4)) + dim + 2] = objects.get(i).getDuration();
                vars[(i * (dim + 4)) + dim + 3] = objects.get(i).getEnd();
            }
            GeostOptions opt = new GeostOptions();
            PropGeost propgeost = new PropGeost(vars, dim, objects, shapes, extcstr, false, opt.included, model.getSolver());

            new Constraint("Geost", propgeost).post();
            throw new UnsupportedOperationException("Geost is not robust");

        }
    },
    globalCardinalityChoco {
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

            IntVar[] x = exps.get(0).toIntVarArray(model);
            IntVar[] y = exps.get(1).toIntVarArray(model);
            model.inverseChanneling(x, y, 1, 1).post();

        }
    },
    knapsackChoco {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new IBuilder() {
                @Override
                public void build(Model model, String name, List<Expression> exps, List<EAnnotation> annotations, Datas datas) {
                    int[] w = exps.get(0).toIntArray();
                    int[] p = exps.get(1).toIntArray();
                    IntVar[] x = exps.get(2).toIntVarArray(model);
                    IntVar W = exps.get(3).intVarValue(model);
                    IntVar P = exps.get(4).intVarValue(model);

                    model.scalar(x, w, "=", W).post();
                    model.scalar(x, p, "=", P).post();
                    new Constraint("knapsack", new Propagator<IntVar>(ArrayUtils.append(x, new IntVar[]{W, P})) {

                        private int[] order;
                        private double[] ratio;

                        @Override
                        public void propagate(int evtmask) throws ContradictionException {
                            // initial sort
                            if (order == null) {
                                order = new int[w.length];
                                ratio = new double[w.length];
                                for (int i = 0; i < w.length; i++) {
                                    ratio[i] = (double) (p[i]) / (double) (w[i]);
                                }
                                BitSet in = new BitSet(w.length);
                                double best = -1;
                                int index = 0;
                                for (int i = 0; i < w.length; i++) {
                                    int item = -1;
                                    for (int o = in.nextClearBit(0); o < w.length; o = in.nextClearBit(o + 1)) {
                                        if (item == -1 || w[i] == 0 || ratio[o] > best) {
                                            best = ratio[o];
                                            item = o;
                                        }
                                    }
                                    in.set(item);
                                    if (item == -1) {
                                        throw new UnsupportedOperationException();
                                    } else {
                                        order[index++] = item;
                                    }
                                }
                            }
                            // filtering algorithm
                            int pomin = 0;
                            int pomax = 0;
                            int cmin = 0;
                            int cmax = 0;
                            for (int i = 0; i < w.length; i++) {
                                pomin += p[i] * vars[i].getLB();
                                pomax += p[i] * vars[i].getUB();
                                cmin += w[i] * vars[i].getLB();
                                cmax += w[i] * vars[i].getUB();
                            }
                            P.updateLowerBound(pomin, this);
                            P.updateUpperBound(pomax, this);
                            W.updateLowerBound(cmin, this);
                            W.updateUpperBound(cmax, this);

                            {
                                cmax = Math.min(cmax, W.getUB());
                                for (int idx : order) {
                                    if (vars[idx].getUB() > vars[idx].getLB()) {
                                        int deltaW = w[idx] * (vars[idx].getUB() - vars[idx].getLB());
                                        if (cmin + deltaW <= cmax) {
                                            pomin += p[idx] * (vars[idx].getUB() - vars[idx].getLB());
                                            cmin += deltaW;
                                        } else {
                                            pomin += Math.ceil((cmax - cmin) * ratio[idx]);
                                            break;
                                        }
                                    }
                                }
                                P.updateUpperBound(pomin, this);
                            }
                        }

                        @Override
                        public ESat isEntailed() {
                            return ESat.TRUE;
                        }
                    }).post();
                }
            }.build(model, id, exps, annotations, datas);
        }
    },
    lex2Choco {
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

            // var int: m, array[int] of var int: x
            IntVar m = exps.get(0).intVarValue(model);
            IntVar[] x = exps.get(1).toIntVarArray(model);
            model.max(m, x).post();

        }
    },
    memberChoco {
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

            IntVar[] x = exps.get(0).toIntVarArray(model);
            IntVar y = exps.get(1).intVarValue(model);
            model.element(y, x, model.intVar(StringUtils.randomName(), 0, x.length - 1, false), 0).post();

        }
    },
    memberReifChoco {
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
            new org.chocosolver.parser.flatzinc.ast.constraints.global.MemberVarReifBuilder().build(model, id, exps, annotations, datas);
        }
    },
    minimumChoco {
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

            IntVar nValues = exps.get(0).intVarValue(model);
            IntVar[] vars = exps.get(1).toIntVarArray(model);
            model.nValues(vars, nValues).post();

        }
    },
    regularChoco {
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

            model.regular(vars, auto).post();

        }
    },
    sortChoco {
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
    tableChoco {
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
                model.table(x[0], x[1], tuples, "AC3bit+rm").post();
            } else {
                model.table(x, tuples, "GACSTR+").post();
            }

        }
    },
    value_precede_chain_intChoco {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            int[] c = exps.get(0).toIntArray();
            IntVar[] x = exps.get(1).toIntVarArray(model);
            model.intValuePrecedeChain(x, c).post();

        }
    },
    count_eq_reif_choco {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            IntVar[] decVars = exps.get(0).toIntVarArray(model);
            IntVar valVar = exps.get(1).intVarValue(model);
            IntVar countVar = exps.get(2).intVarValue(model);
            BoolVar b = exps.get(3).boolVarValue(model);
            Constraint cstr;
            if (valVar.isInstantiated()) {
                IntVar nbOcc = model.intVar(StringUtils.randomName(), 0, decVars.length, true);
                cstr = model.count(valVar.getValue(), decVars, nbOcc);
                model.arithm(nbOcc, "=", countVar).reifyWith(b);
            } else {
                IntVar value = model.intVar(StringUtils.randomName(), valVar.getLB(), valVar.getUB());
                cstr = model.count(value, decVars, countVar);
                model.arithm(value, "=", valVar).reifyWith(b);
            }
            cstr.post();

        }
    },
    set_card {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {

            SetVar a = exps.get(0).setVarValue(model);
            IntVar b = exps.get(1).intVarValue(model);
            model.cardinality(a, b).post();

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
            model.allEqual(new SetVar[]{a, b}).reifyWith(r);

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
                int[] values = exps.get(1).toIntArray();
                model.member(a, values).reifyWith(r);
            } else if (exps.get(1).getTypeOf().equals(Expression.EType.SET_B)) {
                int low = ((ESetBounds) exps.get(1)).getLow();
                int upp = ((ESetBounds) exps.get(1)).getUpp();
                model.member(a, low, upp).reifyWith(r);
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

            SetVar ab = model.setVar(StringUtils.randomName(), a.getLB().toArray(), a.getUB().toArray());
            SetVar ba = model.setVar(StringUtils.randomName(), b.getLB().toArray(), b.getUB().toArray());

            TIntHashSet values = new TIntHashSet();
            for (int i : a.getUB()) {
                values.add(i);
            }
            for (int i : b.getUB()) {
                values.add(i);
            }
            int[] env = values.toArray();
            Arrays.sort(env);
            SetVar c = model.setVar(StringUtils.randomName(), new int[]{}, env);
            IntVar min = model.intVar(StringUtils.randomName(), env[0], env[env.length - 1]);

            BoolVar _b1 = model.subsetEq(new SetVar[]{a, b}).reify();

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

            SetVar ab = model.setVar(StringUtils.randomName(), a.getLB().toArray(), a.getUB().toArray());
            SetVar ba = model.setVar(StringUtils.randomName(), b.getLB().toArray(), b.getUB().toArray());

            TIntHashSet values = new TIntHashSet();
            for (int i : a.getUB()) {
                values.add(i);
            }
            for (int i : b.getUB()) {
                values.add(i);
            }
            int[] env = values.toArray();
            Arrays.sort(env);
            SetVar c = model.setVar(StringUtils.randomName(), new int[]{}, env);
            IntVar min = model.intVar(StringUtils.randomName(), env[0], env[env.length - 1]);

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
            SetVar ab = model.setVar(StringUtils.randomName(), a.getLB().toArray(), a.getUB().toArray());
            SetVar ba = model.setVar(StringUtils.randomName(), b.getLB().toArray(), b.getUB().toArray());
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
    };

    public abstract void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations);
}
