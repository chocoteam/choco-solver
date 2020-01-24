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
package org.chocosolver.util.objects;

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.exception.ContradictionException;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 23/01/2020
 */
public interface RealInterval {

    /**
     * Retrieves the lower bound of the variable
     *
     * @return the lower bound
     */
    double getLB();

    /**
     * Retrieves the upper bound of the variable
     *
     * @return the upper bound
     */
    double getUB();


    /**
     * Modifies the bounds for intersecting with the specified interval.
     *
     * @param interval an interval
     * @param cause who launches the intersection
     * @throws ContradictionException if a failure occurs
     */
    void intersect(RealInterval interval, ICause cause) throws ContradictionException;

    /**
     * Modifies the bounds for intersecting with the specified interval.
     *
     * @param l lower bound
     * @param u upper bound
     * @param cause who launches the intersection
     * @throws ContradictionException if a failure occurs
     */
    void intersect(double l, double u, ICause cause) throws ContradictionException;
}
