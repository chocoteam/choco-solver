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
package org.chocosolver.parser.flatzinc.ast.propagators;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.explanations.RuleStore;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.util.ESat;

/**
 *
 * <p>
 * Project: choco-parsers.
 * @author Charles Prud'homme
 * @since 03/05/2016.
 */
public class PropXeqCReif extends Propagator<IntVar>{

    IntVar var;
    int cste;
    BoolVar r;

    public PropXeqCReif(IntVar x, int c, BoolVar r) {
        super(new IntVar[]{x, r}, PropagatorPriority.BINARY, false);
        this.cste = c;
        this.var = x;
        this.r = r;
    }

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
}
