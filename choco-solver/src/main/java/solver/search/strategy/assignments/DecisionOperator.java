/*
 * Copyright (c) 1999-2014, Ecole des Mines de Nantes
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

package solver.search.strategy.assignments;

import solver.ICause;
import solver.exception.ContradictionException;
import solver.variables.IntVar;
import solver.variables.SetVar;
import solver.variables.Variable;

import java.io.Serializable;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 03/02/11
 */
public abstract class DecisionOperator<V extends Variable> implements Serializable {

    public abstract void apply(V var, int value, ICause cause) throws ContradictionException;

    public abstract void unapply(V var, int value, ICause cause) throws ContradictionException;

    public abstract DecisionOperator opposite();

    public abstract String toString();

    /**
     * Evaluate the possible effect of the decision and return a boolean indicating whether or not
     * the decision can reduce the domain of var.
     *
     * @param var   a variable
     * @param value a value
     * @return true if this has an effect on var
     */
    public abstract boolean isValid(V var, int value);

    // INTEGERS
    public static DecisionOperator<IntVar> int_eq = new DecisionOperator<IntVar>() {

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

    public static DecisionOperator<IntVar> int_neq = new DecisionOperator<IntVar>() {

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

    public static DecisionOperator<IntVar> int_split = new DecisionOperator<IntVar>() {

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

    public static DecisionOperator<IntVar> int_reverse_split = new DecisionOperator<IntVar>() {

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
    public static DecisionOperator<SetVar> set_force = new DecisionOperator<SetVar>() {

        @Override
        public void apply(SetVar var, int element, ICause cause) throws ContradictionException {
            var.addToKernel(element, cause);
        }

        @Override
        public void unapply(SetVar var, int element, ICause cause) throws ContradictionException {
            var.removeFromEnvelope(element, cause);
        }

        @Override
        public String toString() {
            return " contains ";
        }

        @Override
        public boolean isValid(SetVar var, int element) {
            return var.envelopeContains(element) && !var.kernelContains(element);
        }

        @Override
        public DecisionOperator opposite() {
            return set_remove;
        }
    };

    public static DecisionOperator<SetVar> set_remove = new DecisionOperator<SetVar>() {

        @Override
        public void apply(SetVar var, int element, ICause cause) throws ContradictionException {
            var.removeFromEnvelope(element, cause);
        }

        @Override
        public void unapply(SetVar var, int element, ICause cause) throws ContradictionException {
            var.addToKernel(element, cause);
        }

        @Override
        public String toString() {
            return " !contains ";
        }

        @Override
        public boolean isValid(SetVar var, int element) {
			return var.envelopeContains(element) && !var.kernelContains(element);
        }

        @Override
        public DecisionOperator opposite() {
            return set_force;
        }
    };
}
