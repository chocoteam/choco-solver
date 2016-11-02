/**
 * Copyright (c) 2016, Ecole des Mines de Nantes
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

/**
 * Create serializable decisions. 
 * Decisions are static nested classes because serialization of lambda functions or anonymous classes is compiler-dependent.
 * Furthermore, the serialization of the singleton pattern requires a special treatment.
 * 
 * @see <a href="@linkp http://stackoverflow.com/questions/19440511/explanation-needed-why-adding-implements-serializable-to-singleton-class-is-ins">stack overflow</a>
 * @author Arnaud Malapert
 *
 */
public final class DecisionOperatorFactory {

    private DecisionOperatorFactory() {}

    public static DecisionOperator<IntVar> makeIntEq() {
        return IntEqDecision.getInstance();
    }

    public static DecisionOperator<IntVar> makeIntNeq() {
        return IntNeqDecision.getInstance();
    }

    public static DecisionOperator<IntVar> makeIntSplit() {
        return IntSplitDecision.getInstance();
    }

    public static DecisionOperator<IntVar> makeIntReverseSplit() {
        return IntReverseSplitDecision.getInstance();
    }

    public static DecisionOperator<SetVar> makeSetForce() {
        return SetForceDecision.getInstance();
    }

    public static DecisionOperator<SetVar> makeSetRemove() {
        return SetRemoveDecision.getInstance();
    }
    // INTEGERS
    private static final class IntEqDecision implements DecisionOperator<IntVar> {

        private static final long serialVersionUID = 7293773317776136982L;

        private final static IntEqDecision INSTANCE = new IntEqDecision();

        public final static IntEqDecision getInstance() {
            return INSTANCE;
        }

        /**
         * readResolve method to preserve singleton property
         */
        private Object readResolve() {
            // Return the one true INSTANCE and let the garbage collector
            // take care of the INSTANCE impersonator.
            return INSTANCE;
        }

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
        public DecisionOperator<IntVar> opposite() {
            return makeIntNeq();
        }
    };

    private static final class IntNeqDecision implements DecisionOperator<IntVar> {

        private static final long serialVersionUID = 3056222234436601667L;
        private final static IntNeqDecision INSTANCE = new IntNeqDecision();

        public final static IntNeqDecision getInstance() {
            return INSTANCE;
        }

        /**
         * readResolve method to preserve singleton property
         */
        private Object readResolve() {
            // Return the one true INSTANCE and let the garbage collector
            // take care of the INSTANCE impersonator.
            return INSTANCE;
        }

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
        public DecisionOperator<IntVar> opposite() {
            return makeIntEq();
        }
    };

    private static final class IntSplitDecision implements DecisionOperator<IntVar> {

        private static final long serialVersionUID = 2796498653106384502L;
        private final static IntSplitDecision INSTANCE = new IntSplitDecision();

        public final static IntSplitDecision getInstance() {
            return INSTANCE;
        }

        private IntSplitDecision() {
            super();
        }

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
            //FIXME If equal => no pruning !? CPRU
            //FIXME If < LB  => fail CPRU
            return var.getUB() >= value;
        }

        @Override
        public DecisionOperator<IntVar> opposite() {
            return makeIntReverseSplit();
        }

    };

    private static final class IntReverseSplitDecision implements DecisionOperator<IntVar> {

        private static final long serialVersionUID = -4155926684198463505L;

        private final static IntReverseSplitDecision INSTANCE = new IntReverseSplitDecision();

        public final static IntReverseSplitDecision getInstance() {
            return INSTANCE;
        }

        private IntReverseSplitDecision() {}

        /**
         * readResolve method to preserve singleton property
         */
        private Object readResolve() {
            // Return the one true INSTANCE and let the garbage collector
            // take care of the INSTANCE impersonator.
            return INSTANCE;
        }

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
            //FIXME If equal => no pruning !? CPRU
            //FIXME If > UB  => fail CPRU
            return var.getLB() <= value;
        }

        @Override
        public DecisionOperator<IntVar> opposite() {
            return makeIntSplit();
        }
    };


    // SETS
    private static final class SetForceDecision implements DecisionOperator<SetVar> {

        private static final long serialVersionUID = -4868225105307378160L;

        private final static SetForceDecision INSTANCE = new DecisionOperatorFactory.SetForceDecision();

        public final static SetForceDecision getInstance() {
            return INSTANCE;
        }

        private SetForceDecision() {}

        /**
         * readResolve method to preserve singleton property
         */
        private Object readResolve() {
            // Return the one true INSTANCE and let the garbage collector
            // take care of the INSTANCE impersonator.
            return INSTANCE;
        }

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
            return var.getUB().contains(element) && !var.getLB().contains(element);
        }

        @Override
        public DecisionOperator<SetVar> opposite() {
            return set_remove;
        }
    };

    private static final class SetRemoveDecision implements DecisionOperator<SetVar> {

        private static final long serialVersionUID = -580239209082758455L;

        private final static SetRemoveDecision INSTANCE = new SetRemoveDecision();

        public final static SetRemoveDecision getInstance() {
            return INSTANCE;
        }

        private SetRemoveDecision() {}

        /**
         * readResolve method to preserve singleton property
         */
        private Object readResolve() {
            // Return the one true INSTANCE and let the garbage collector
            // take care of the INSTANCE impersonator.
            return INSTANCE;
        }

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
            return var.getUB().contains(element) && !var.getLB().contains(element);
        }

        @Override
        public DecisionOperator<SetVar> opposite() {
            return set_force;
        }
    };


}
