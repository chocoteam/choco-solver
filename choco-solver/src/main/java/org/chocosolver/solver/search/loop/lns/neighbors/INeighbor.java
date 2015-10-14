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
package org.chocosolver.solver.search.loop.lns.neighbors;

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.limits.ACounter;

/**
 * An interface defining services required for the LNS to select variables to freeze-unfreeze.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 18/06/13
 */
public interface INeighbor {


    /**
     * Record values of decision variables to freeze some ones during the next LNS run
     */
    void recordSolution();

    /**
     * Freezes some variables in order to have a fast computation
     *
     * @param cause the LNS
     * @throws org.chocosolver.solver.exception.ContradictionException
     *          if variables have been fixed to inconsistent values
     *          this can happen if fixed variables cannot yield to a better solution than the last one
     *          a contradiction is raised because a cut has been posted on the objective function
     *          Notice that it could be used to generate a no-good
     */
    void fixSomeVariables(ICause cause) throws ContradictionException;

    /**
     * Use less restriction at the beginning of a LNS run
     * in order to get better solutions
     * Called when no solution was found during a LNS run (trapped into a local optimum)
     */
    void restrictLess();


    /**
     * @return true iff the search is in a complete mode (no fixed variable)
     */
    boolean isSearchComplete();

    /**
     * Plug a fast restart strategy to the neighborhood
     *
     * @param counter a counter
     */
    void fastRestart(ACounter counter);

    /**
     * This method is called by {@link org.chocosolver.solver.search.loop.lns.LargeNeighborhoodSearch} on the first solution,
     * it activates the fast restart strategy
     */
    void activeFastRestart();
}
