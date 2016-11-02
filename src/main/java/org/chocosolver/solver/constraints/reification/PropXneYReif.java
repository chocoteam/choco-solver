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
package org.chocosolver.solver.constraints.reification;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.explanations.RuleStore;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.util.ESat;

/**
 * A propagator dedicated to express in a compact way: (x &ne; y) &hArr; b
 *
 * @author Charles Prud'homme
 * @since 03/05/2016.
 */
public class PropXneYReif extends Propagator<IntVar> {

    public PropXneYReif(IntVar x, IntVar y, BoolVar r) {
        super(new IntVar[]{x, y, r}, PropagatorPriority.TERNARY, false);
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (vars[2].getLB() == 1) {
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
            if (vars[2].getUB() == 0) {
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
                            vars[2].instantiateTo(1,this);
                        } else {
                            vars[2].instantiateTo(0,this);
                        }
                        setPassive();
                    } else {
                        if (!vars[1].contains(vars[0].getValue())) {
                            vars[2].instantiateTo(1,this);
                            setPassive();
                        }
                    }
                } else {
                    if (vars[1].isInstantiated()) {
                        if (!vars[0].contains(vars[1].getValue())) {
                            vars[2].instantiateTo(0,this);
                            setPassive();
                        }
                    }
                }
            }
        }
    }

    @Override
    public ESat isEntailed() {
        if(isCompletelyInstantiated()){
            if(vars[2].isInstantiatedTo(1)){
                return ESat.eval(vars[0].getValue() != vars[1].getValue());
            }else{
                return ESat.eval(vars[0].getValue() == vars[1].getValue());
            }
        }
        return ESat.UNDEFINED;
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

    @Override
    public String toString() {
        return "(" + vars[0].getName() +" = " + vars[0].getName() + ") <=> "+vars[2].getName();
    }
}
