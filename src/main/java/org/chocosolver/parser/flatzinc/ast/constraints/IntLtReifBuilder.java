/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
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

package org.chocosolver.parser.flatzinc.ast.constraints;

import org.chocosolver.parser.flatzinc.FznSettings;
import org.chocosolver.parser.flatzinc.ast.Datas;
import org.chocosolver.parser.flatzinc.ast.expression.EAnnotation;
import org.chocosolver.parser.flatzinc.ast.expression.Expression;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.*;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.explanations.RuleStore;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;

import java.util.List;

/**
 * (a < b) &#8660; r
 * <br/>
 *
 * @author Charles Prud'homme, Jean-Guillaume Fages
 * @since 26/01/11
 */
public class IntLtReifBuilder implements IBuilder {

    @Override
    public void build(Solver solver, String name, List<Expression> exps, List<EAnnotation> annotations, Datas datas) {
        IntVar a = exps.get(0).intVarValue(solver);
        IntVar b = exps.get(1).intVarValue(solver);
        final BoolVar r = exps.get(2).boolVarValue(solver);
        // this constraint is not poster, hence not returned, because it is reified
        if (((FznSettings)solver.getSettings()).enableClause()
                && ((a.getTypeAndKind() & Variable.KIND) == Variable.BOOL) && ((b.getTypeAndKind() & Variable.KIND) == Variable.BOOL)) {
            SatFactory.addBoolIsLtVar((BoolVar) a, (BoolVar) b, r);

        } else if (((FznSettings)solver.getSettings()).adhocReification()) {
            if (a.isInstantiated() || b.isInstantiated()) {
                final IntVar var;
                final int cste;
                if (a.isInstantiated()) {
                    var = b;
                    cste = a.getValue();
                    solver.post(new Constraint("reif(b>cste,r)", new Propagator<IntVar>(new IntVar[]{var, r}, PropagatorPriority.BINARY, false) {
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
                    }));
                } else {
                    var = a;
                    cste = b.getValue();
                    solver.post(new Constraint("reif(a<cste,r)", new Propagator<IntVar>(new IntVar[]{var, r}, PropagatorPriority.BINARY, false) {
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
                    }));
                }
            } else {
                solver.post(new Constraint("reif(a<b,r)", new Propagator<IntVar>(new IntVar[]{a, b, r}, PropagatorPriority.TERNARY, false) {
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

                }));
            }
        } else {
            ICF.arithm(a, "<", b).reifyWith(r);
        }
    }
}
