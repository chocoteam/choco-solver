/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.exception;

import org.chocosolver.solver.Cause;
import org.chocosolver.solver.ICause;
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

    public ICause c;
    public Variable v;
    public String s;

    public ContradictionException() {
//        does not call super() on purpose
        c = Cause.Null;
        v = null;
        s = null;
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
    @Override
    public String toString() {
        return "CONTRADICTION (" + (c == null ? "" : c + ", ") + v + ") : " + s;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
