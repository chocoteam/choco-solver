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
package org.chocosolver.solver.search.strategy.assignments;

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.Variable;



/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 03/02/11
 */
public interface DecisionOperator<V extends Variable>  {

    void apply(V var, int value, ICause cause) throws ContradictionException;

    void unapply(V var, int value, ICause cause) throws ContradictionException;

    DecisionOperator opposite();

    String toString();

    /**
     * Evaluate the possible effect of the decision and return a boolean indicating whether or not
     * the decision can reduce the domain of var.
     *
     * @param var   a variable
     * @param value a value
     * @return true if this has an effect on var
     */
    boolean isValid(V var, int value);

    // INTEGERS
    DecisionOperator<IntVar> int_eq = new DecisionOperator<IntVar>() {

        @Override
        public void apply(IntVar var, int value, ICause cause) throws ContradictionException {
            var.instantiateTo(value, cause);
        }

        @Override
        public void unapply(IntVar var, int value, ICause cause) throws ContradictionException {
            var.removeValue(value, cause);
        }

        @Override
        public String toString() {
            return " == ";
        }

        @Override
        public boolean isValid(IntVar var, int value) {
            return var.contains(value);
        }

        @Override
        public DecisionOperator opposite() {
            return int_neq;
        }
    };

    DecisionOperator<IntVar> int_neq = new DecisionOperator<IntVar>() {

        @Override
        public void apply(IntVar var, int value, ICause cause) throws ContradictionException {
            var.removeValue(value, cause);
        }

        @Override
        public void unapply(IntVar var, int value, ICause cause) throws ContradictionException {
            var.instantiateTo(value, cause);
        }

        @Override
        public String toString() {
            return " != ";
        }

        @Override
        public boolean isValid(IntVar var, int value) {
            return var.contains(value);
        }

        @Override
        public DecisionOperator opposite() {
            return int_eq;
        }
    };

    DecisionOperator<IntVar> int_split = new DecisionOperator<IntVar>() {

        @Override
        public void apply(IntVar var, int value, ICause cause) throws ContradictionException {
            var.updateUpperBound(value, cause);
        }

        @Override
        public void unapply(IntVar var, int value, ICause cause) throws ContradictionException {
            var.updateLowerBound(value + 1, cause);
        }

        @Override
        public String toString() {
            return " <= ";
        }

        @Override
        public boolean isValid(IntVar var, int value) {
            return var.getUB() >= value;
        }

        @Override
        public DecisionOperator opposite() {
            return int_reverse_split;
        }

    };

    DecisionOperator<IntVar> int_reverse_split = new DecisionOperator<IntVar>() {

        @Override
        public void apply(IntVar var, int value, ICause cause) throws ContradictionException {
            var.updateLowerBound(value, cause);
        }

        @Override
        public void unapply(IntVar var, int value, ICause cause) throws ContradictionException {
            var.updateUpperBound(value - 1, cause);
        }

        @Override
        public String toString() {
            return " >= ";
        }

        @Override
        public boolean isValid(IntVar var, int value) {
            return var.getLB() <= value;
        }

        @Override
        public DecisionOperator opposite() {
            return int_split;
        }
    };


    // SETS
    DecisionOperator<SetVar> set_force = new DecisionOperator<SetVar>() {

        @Override
        public void apply(SetVar var, int element, ICause cause) throws ContradictionException {
            var.force(element, cause);
        }

        @Override
        public void unapply(SetVar var, int element, ICause cause) throws ContradictionException {
            var.remove(element, cause);
        }

        @Override
        public String toString() {
            return " contains ";
        }

        @Override
        public boolean isValid(SetVar var, int element) {
            return var.getUB().contain(element) && !var.getLB().contain(element);
        }

        @Override
        public DecisionOperator opposite() {
            return set_remove;
        }
    };

    DecisionOperator<SetVar> set_remove = new DecisionOperator<SetVar>() {

        @Override
        public void apply(SetVar var, int element, ICause cause) throws ContradictionException {
            var.remove(element, cause);
        }

        @Override
        public void unapply(SetVar var, int element, ICause cause) throws ContradictionException {
            var.force(element, cause);
        }

        @Override
        public String toString() {
            return " !contains ";
        }

        @Override
        public boolean isValid(SetVar var, int element) {
			return var.getUB().contain(element) && !var.getLB().contain(element);
        }

        @Override
        public DecisionOperator opposite() {
            return set_force;
        }
    };
}
