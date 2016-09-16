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
package org.chocosolver.solver.objective;

import org.chocosolver.solver.ResolutionPolicy;

/**
 * interface to monitor bounds.
 *
 * @author Jean-Guillaume Fages, Charles Prud'homme, Arnaud Malapert
 */
public interface IBoundsManager {

    /**
     * @return the ResolutionPolicy of the problem
     */
    ResolutionPolicy getPolicy();

    /**
     * @return true iff the problem is an optimization problem
     */
    default boolean isOptimization() {
        return true;
    }

    /**
     * @return the best lower bound computed so far
     */
    Number getBestLB();

    /**
     * @return the best upper bound computed so far
     */
    Number getBestUB();

    /**
     * States that lb is a global lower bound on the problem
     *
     * @param lb lower bound
     */
    void updateBestLB(Number lb);

    /**
     * States that ub is a global upper bound on the problem
     *
     * @param ub upper bound
     */
    void updateBestUB(Number ub);

    /**
     * @return the best solution value found so far (returns the initial bound if no solution has been found yet)
     */
    Number getBestSolutionValue();
}