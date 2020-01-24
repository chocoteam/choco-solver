/**
 * Copyright (c) 1999-2020, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p>
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the Ecole des Mines de Nantes nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * <p>
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
package org.chocosolver.solver.expression.continuous.arithmetic;

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.util.objects.RealInterval;

import java.util.List;
import java.util.TreeSet;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 23/01/2020
 */
public class RealIntervalConstant implements CArExpression {

    protected final double lb;
    protected final double ub;

    public RealIntervalConstant(RealInterval interval) {
        this.lb = interval.getLB();
        this.ub = interval.getUB();
    }

    public RealIntervalConstant(double l, double u) {
        this.lb = l;
        this.ub = u;
    }

    public String toString() {
        return "[" + lb + "," + ub + "]";
    }

    @Override
    public double getLB() {
        return lb;
    }

    @Override
    public double getUB() {
        return ub;
    }

    @Override
    public void intersect(RealInterval interval, ICause cause) {
        
    }

    @Override
    public void intersect(double l, double u, ICause cause) {

    }

    @Override
    public Model getModel() {
        return null;
    }

    @Override
    public void tighten() {
    }

    @Override
    public void project(ICause cause) throws ContradictionException {
    }

    @Override
    public void collectVariables(TreeSet<RealVar> set) {
        // void
    }

    @Override
    public void subExps(List<CArExpression> list) {
        list.add(this);
    }

    @Override
    public boolean isolate(RealVar var, List<CArExpression> wx, List<CArExpression> wox) {
        return false;
    }

    @Override
    public void init() {
        // void
    }

    @Override
    public RealVar realVar(double precision) {
        return null;
    }
}
