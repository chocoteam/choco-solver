/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.cnf;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 23 nov. 2010
 */
public interface ILogical extends Cloneable {

    /**
     * Current tree is a literal
     *
     * @return <code>true</code> if <code>this</code> is a literal
     */
    boolean isLit();

    /**
     * Current tree is rooted with NOT logical operator
	 * This is a one way relationship: in case a = not(b)
	 * a.isNot() returns true whereas b.isNot() returns false (unless b = not(c)...)
	 *
     * @return <code>true</code> if <code>this</code> is NOT
     */
    boolean isNot();

	/**
	 * States whether or not this variable is the negation of another.
	 * This is a one way relationship: in case a = not(b)
	 * a.isNot() returns true whereas b.isNot() returns false (unless b = not(c)...)
	 *
	 * @param isNot	true iff this variable is the negation of another
	 */
	void setNot(boolean isNot);
}
