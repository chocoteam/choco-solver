/*
 * Copyright (c) 1999-2012, Ecole des Mines de Nantes
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

package solver.explanations;

import solver.Identity;
import solver.variables.Variable;

/**
 * An abstract class to explain event occuring on variables during the search.
 * There are for of them: value removal, variable assignment, variable refutation and explanation.
 * An explanations is a combination of deductions and propagators.
 * <p/>
 * Created by IntelliJ IDEA.
 * User: njussien
 * Date: 26 oct. 2010
 * Time: 12:57:36
 */
public abstract class Deduction implements Identity, Comparable<Deduction> {

    public static int _ID = 0;

    final int id;

    int wi;

    public enum Type {
        Exp, ValRem, DecLeft, DecRight, PropAct
    }

    final Type mType;

    public Deduction(Type yType) {
        this.mType = yType;
        id = _ID++;
    }

    /**
     * Returns the variable to explain
     *
     * @return variable to explain
     */
    public Variable getVar() {
        return null;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public int compareTo(Deduction o) {
        return this.id - o.id;
    }

    @Override
    public int getId() {
        return id;
    }

    public Type getmType() {
        return mType;
    }

    public final void setWI(int wi){
        this.wi = wi;
    }

    public final int getWI(){
        return this.wi;

    }
}
