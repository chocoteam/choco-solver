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
package org.chocosolver.solver.search.strategy.decision;

import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.explanations.RuleStore;
import org.chocosolver.solver.search.strategy.assignments.DecisionOperator;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.util.PoolManager;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 2 juil. 2010
 */
public class IntDecision extends Decision<IntVar> {

    int value;

    DecisionOperator<IntVar> assignment;

    final PoolManager<IntDecision> poolManager;

    public IntDecision(PoolManager<IntDecision> poolManager) {
        super(2);
        this.poolManager = poolManager;
    }

    @Override
    public Integer getDecisionValue() {
        return value;
    }

    @Override
    public void apply() throws ContradictionException {
        if (branch == 1) {
            assignment.apply(var, value, this);
        } else if (branch == 2) {
            assignment.unapply(var, value, this);
        }
    }

    public void set(IntVar v, int value, DecisionOperator<IntVar> assignment) {
        super.set(v, v.getSolver().getEnvironment().getWorldIndex());
        this.value = value;
        this.assignment = assignment;
    }

    @Override
    public void reverse() {
        this.assignment = assignment.opposite();
    }

    @Override
    public void free() {
        previous = null;
        poolManager.returnE(this);
    }

    @Override
    public Decision<IntVar> duplicate() {
        IntDecision d = poolManager.getE();
        if (d == null) {
            d = new IntDecision(poolManager);
        }
        d.set(var, value, assignment);
        return d;
    }

    @Override
    public boolean isEquivalentTo(Decision dec) {
        if (dec instanceof IntDecision) {
            IntDecision id = (IntDecision) dec;
            return (id.var == this.var
                    && id.assignment == this.assignment
                    && id.value == this.value
                    && id.max_branching == this.max_branching
                    && id.branch == this.branch);
        } else {
            return false;
        }
    }

    public DecisionOperator<IntVar> getDecOp() {
        return assignment;
    }

    public IntDecision flip(){
        IntDecision d = poolManager.getE();
        if (d == null) {
            d = new IntDecision(poolManager);
        }
        int val = value;
        if(assignment == DecisionOperator.int_split){
            val++;
        }
        else if(assignment == DecisionOperator.int_reverse_split){
            val--;
        }
        d.set(var, val, assignment.opposite());
        return d;
    }

    @Override
    public String toString() {
        if (assignment.equals(DecisionOperator.int_eq)) {
            return String.format("%s %s {%d}",
                    var.getName(),
                    branch < 1 ? "=" : '\\',
                    value);
        } else if (assignment.equals(DecisionOperator.int_neq)) {
            return String.format("%s %s {%d}",
                    var.getName(),
                    branch < 1 ? '\\' : "=",
                    value);
        } else if (assignment.equals(DecisionOperator.int_split)) {
            return String.format("%s in %s%d,%d]",
                    var.getName(),
                    branch < 1 ? '[' : ']',
                    branch < 1 ? var.getLB() : value,
                    branch < 1 ? value : var.getUB());
        } else if (assignment.equals(DecisionOperator.int_reverse_split)) {
            return String.format("%s in [%d,%d%s",
                    var.getName(),
                    branch < 1 ? value : var.getLB(),
                    branch < 1 ? var.getUB() : value,
                    branch < 1 ? ']' : '[');
        } else {
            return String.format("%s %s {%s}",
                    var.getName(),
                    branch < 1 ? assignment.toString() : assignment.opposite().toString(),
                    value);
        }
    }

    @Override
    public boolean why(RuleStore ruleStore, IntVar var, IEventType evt, int value) {
        return false;
    }
}
