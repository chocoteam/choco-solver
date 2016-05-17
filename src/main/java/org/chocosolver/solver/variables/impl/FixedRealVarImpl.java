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
package org.chocosolver.solver.variables.impl;

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.delta.IDelta;
import org.chocosolver.solver.variables.delta.NoDelta;
import org.chocosolver.solver.variables.events.IEventType;

/**
 *
 * <p>
 * Project: choco-solver.
 * @author Charles Prud'homme
 * @since 13/05/2016.
 */
public class FixedRealVarImpl extends AbstractVariable implements RealVar {

    /**
     * The constant this variable relies on.
     */
    double value;

    /**
     * Create the shared data of any type of variable.
     *
     * @param name  name of the variable
     * @param value a double value
     * @param model model which declares this variable
     */
    public FixedRealVarImpl(String name, double value, Model model) {
        super(name, model);
        this.value = value;
    }

    @Override
    public double getLB() {
        return value;
    }

    @Override
    public double getUB() {
        return value;
    }

    @Override
    public boolean updateLowerBound(double value, ICause cause) throws ContradictionException {
        if (value > value) {
            assert cause != null;
            this.contradiction(cause, "outside domain update bound");
        }
        return false;
    }

    @Override
    public boolean updateUpperBound(double value, ICause cause) throws ContradictionException {
        if (value < value) {
            assert cause != null;
            this.contradiction(cause, "outside domain update bound");
        }
        return false;
    }

    @Override
    public boolean updateBounds(double lowerbound, double upperbound, ICause cause) throws ContradictionException {
        if (lowerbound > value || upperbound < value) {
            assert cause != null;
            this.contradiction(cause, "outside domain update bound");
        }
        return false;
    }

    @Override
    public double getPrecision() {
        return Double.MIN_VALUE;
    }

    @Override
    public boolean isInstantiated() {
        return true;
    }

    @Override
    public IDelta getDelta() {
        return NoDelta.singleton;
    }

    @Override
    public void createDelta() {

    }

    @Override
    public void notifyMonitors(IEventType event) throws ContradictionException {

    }

    @Override
    public void contradiction(ICause cause, String message) throws ContradictionException {

    }

    @Override
    public int getTypeAndKind() {
        return Variable.REAL | Variable.CSTE;
    }
}
