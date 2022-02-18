/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.sat;

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.util.ESat;

import java.util.Objects;

import static org.chocosolver.sat.MiniSat.sgn;
import static org.chocosolver.sat.MiniSat.var;

/**
 * <p>
 * This abstract class makes possible to maintain relationships between a CP variable
 * and a SAT variable.
 * </p>
 * <p>
 * That is, it turns an event on a variable from constraint programming side
 * to an event on a variable in SAT side ({@link MiniSat}).</p>
 * <p>
 * On the other side, it turns an event on a variable from SAT side
 * to an event on a variable in CP side.
 * </p>
 *
 * @author Charles Prud'homme
 * @since 29/03/2021
 */
public interface Literalizer {

    /**
     * Set the SAT variable
     */
    void svar(int svar);

    /**
     * @return this SAT variable
     */
    int svar();

    /**
     * @return this CP variable
     */
    Variable cvar();

    /**
     * @return {@code true} if this will fix its literal
     */
    boolean canReact();

    /**
     * Turns an event into a literal and returns it.
     *
     * @return the literal to enqueue in SAT
     */
    int toLit();

    /**
     * Turns an event from SAT side (in the form of a literal) to an event in CP side.
     * Actually, it is expected that the event is directly applied, that's why a contradiction may be thrown.
     *
     * @param lit   the literal
     * @param cause cause (for CP propagation purpose)
     * @return {@code true} if the CP variable has been modified
     * @throws ContradictionException if the conversion leads to a failure
     */
    boolean toEvent(int lit, ICause cause) throws ContradictionException;

    boolean check(boolean sign);

    class BoolLit implements Literalizer {

        public final BoolVar cpVar;
        public int satVar;

        public BoolLit(BoolVar cpVar) {
            this.cpVar = cpVar;
            this.satVar = -1;
        }

        @Override
        public void svar(int svar) {
            if (satVar == -1) {
                this.satVar = svar;
            } else {
                throw new UnsupportedOperationException("Overriding Literalizer's satVar is forbidden");
            }
        }

        @Override
        public int svar() {
            return satVar;
        }

        @Override
        public Variable cvar() {
            return cpVar;
        }

        @Override
        public boolean canReact() {
            return cpVar.isInstantiated();
        }

        @Override
        public int toLit() {
            return MiniSat.makeLiteral(satVar, cpVar.getValue() != 0);
        }

        @Override
        public boolean toEvent(int lit, ICause cause) throws ContradictionException {
            assert satVar == var(lit);
            return cpVar.instantiateTo(sgn(lit) ? 1 : 0, cause);
        }

        /**
         * @return {@code true} if this relationship holds
         */
        @Override
        public boolean check(boolean sign) {
            return cpVar.getBooleanValue().equals(ESat.eval(sign));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof BoolLit)) return false;
            BoolLit boolLit = (BoolLit) o;
            return cpVar.equals(boolLit.cpVar);
        }

        @Override
        public int hashCode() {
            return Objects.hash(cpVar);
        }
    }

    class IntEqLit implements Literalizer {
        public final IntVar cpVar;
        public final int val;
        public int satVar;

        public IntEqLit(IntVar cpVar, int val) {
            this.cpVar = cpVar;
            this.val = val;
            this.satVar = -1;
        }

        @Override
        public void svar(int svar) {
            if (satVar == -1) {
                this.satVar = svar;
            } else {
                throw new UnsupportedOperationException("Overriding Literalizer's satVar is forbidden");
            }
        }

        @Override
        public int svar() {
            return satVar;
        }

        @Override
        public Variable cvar() {
            return cpVar;
        }

        @Override
        public boolean canReact() {
            return cpVar.isInstantiated() || !cpVar.contains(val);
        }

        @Override
        public int toLit() {
            return MiniSat.makeLiteral(satVar, cpVar.isInstantiatedTo(val));
        }

        @Override
        public boolean toEvent(int lit, ICause cause) throws ContradictionException {
            assert satVar == var(lit);
            if (sgn(lit)) {
                return cpVar.instantiateTo(val, cause);
            } else {
                return cpVar.removeValue(val, cause);
            }
        }

        @Override
        public boolean check(boolean sign) {
            return sign ? cpVar.isInstantiatedTo(val) : !cpVar.contains(val);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof IntEqLit)) return false;
            IntEqLit intEqLit = (IntEqLit) o;
            return val == intEqLit.val && cpVar.equals(intEqLit.cpVar);
        }

        @Override
        public int hashCode() {
            return Objects.hash(cpVar, val);
        }
    }

    class IntLeLit implements Literalizer {
        public final IntVar cpVar;
        public final int val;
        public int satVar;

        public IntLeLit(IntVar cpVar, int val) {
            this.cpVar = cpVar;
            this.val = val;
            this.satVar = -1;
        }

        @Override
        public void svar(int svar) {
            if (satVar == -1) {
                this.satVar = svar;
            } else {
                throw new UnsupportedOperationException("Overriding Literalizer's satVar is forbidden");
            }
        }

        @Override
        public int svar() {
            return satVar;
        }

        @Override
        public Variable cvar() {
            return cpVar;
        }

        @Override
        public boolean canReact() {
            return cpVar.getUB() <= val || cpVar.getLB() > val;
        }

        @Override
        public int toLit() {
            return MiniSat.makeLiteral(satVar, cpVar.getUB() <= val);
        }

        @Override
        public boolean toEvent(int lit, ICause cause) throws ContradictionException {
            assert satVar == var(lit);
            if (sgn(lit)) {
                return cpVar.updateUpperBound(val, cause);
            } else {
                return cpVar.updateLowerBound(val + 1, cause);
            }
        }

        @Override
        public boolean check(boolean sign) {
            return sign ? cpVar.getUB() <= val : cpVar.getLB() > val;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof IntLeLit)) return false;
            IntLeLit intLeLit = (IntLeLit) o;
            return val == intLeLit.val && satVar == intLeLit.satVar && cpVar.equals(intLeLit.cpVar);
        }

        @Override
        public int hashCode() {
            return Objects.hash(cpVar, val);
        }
    }

    class SetInLit implements Literalizer {
        public final SetVar cpVar;
        public final int val;
        public int satVar;

        public SetInLit(SetVar cpVar, int val) {
            this.cpVar = cpVar;
            this.val = val;
            this.satVar = -1;
        }

        @Override
        public void svar(int svar) {
            if (satVar == -1) {
                this.satVar = svar;
            } else {
                throw new UnsupportedOperationException("Overriding Literalizer's satVar is forbidden");
            }
        }

        @Override
        public int svar() {
            return satVar;
        }

        @Override
        public Variable cvar() {
            return cpVar;
        }

        @Override
        public boolean canReact() {
            return cpVar.getLB().contains(val) || !cpVar.getUB().contains(val);
        }

        @Override
        public int toLit() {
            return MiniSat.makeLiteral(satVar, cpVar.getLB().contains(val));
        }

        @Override
        public boolean toEvent(int lit, ICause cause) throws ContradictionException {
            assert satVar == var(lit);
            if (sgn(lit)) {
                return cpVar.force(val, cause);
            } else {
                return cpVar.remove(val, cause);
            }
        }

        @Override
        public boolean check(boolean sign) {
            return sign ? cpVar.getLB().contains(val) : !cpVar.getUB().contains(val);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof SetInLit)) return false;
            SetInLit setInLit = (SetInLit) o;
            return val == setInLit.val && cpVar.equals(setInLit.cpVar);
        }

        @Override
        public int hashCode() {
            return Objects.hash(cpVar, val);
        }
    }

}
