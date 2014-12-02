/**
 * Copyright (c) 2014,
 *       Charles Prud'homme (TASC, INRIA Rennes, LINA CNRS UMR 6241),
 *       Jean-Guillaume Fages (COSLING S.A.S.).
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.solver.exception;

import org.chocosolver.solver.Cause;
import org.chocosolver.solver.ICause;
import org.chocosolver.solver.explanations.Explanation;
import org.chocosolver.solver.explanations.ExplanationEngine;
import org.chocosolver.solver.explanations.VariableState;
import org.chocosolver.solver.variables.Variable;

/**
 * A specific <code>Exception</code> to deal with contradiction.
 * <p/>
 * A contradiction appears when at least one <code>Variable</code> object is not coherent
 * regarding all or part of <code>Constraint</code> network.
 * Empty domain, instantiation to an out-of-domain value, etc. throws contradiction.
 * <p/>
 * For performance consideration, a <code>ContradictionException</code> is created every time a contradiction
 * occurs. A unique object is build and set with specific case information.
 *
 * @author Xavier Lorca
 * @author Charles Prud'homme
 * @version 0.01, june 2010
 * @since 0.01
 */
public final class ContradictionException extends Exception {

    private static final long serialVersionUID = 2L;

    public ICause c = Cause.Null;
    public Variable v = null;
    public String s;

    public ContradictionException() {
//        super();
    }

    /**
     * Throws the unique <code>ContradictionException</code> filled with the specified parameters.
     *
     * @param c the constraint at the origin of the contradiction
     * @param v the variable concerned by the contradiction
     * @param s the message to print
     * @return ContradictionException the filled exception
     */
    public ContradictionException set(ICause c, Variable v, String s) {
        assert c != null;
        this.c = c;
        this.v = v;
        this.s = s;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return "CONTRADICTION (" + (c == null ? "" : c.toString() + ", ") + v + ") : " + s;
    }

    /**
     * {@inheritDoc}
     */
    public synchronized Throwable fillInStackTrace() {
        return this;
    }

    public Explanation explain(ExplanationEngine xengine) {
        Explanation expl = new Explanation();
        if (v != null) {
            v.explain(xengine, VariableState.DOM, expl);
        } else {
            c.explain(xengine, null, expl);
        }
        return expl;
    }
}
