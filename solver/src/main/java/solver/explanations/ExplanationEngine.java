/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package solver.explanations;

import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.IStateBitSet;
import com.sun.istack.internal.NotNull;
import solver.ICause;
import solver.variables.IntVar;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: njussien
 * Date: 26 oct. 2010
 * Time: 12:34:02
 *
 * A class to manage explanations. The default behavior is to do nothing !
 */
public class ExplanationEngine implements Serializable {

    IEnvironment env;

    public void removeValue(IntVar var, int val, @NotNull ICause cause) {}
    public void updateLowerBound(IntVar intVar, int old, int value, @NotNull ICause cause) {}
    public void updateUpperBound(IntVar intVar, int old, int value, @NotNull ICause cause) {}
    public void instantiateTo(IntVar var, int val, @NotNull ICause cause) {}

    public IStateBitSet getRemovedValues(IntVar v) { return null; }

    /**
     * Provides an explanation for the removal of value <code>val</code> from variable
     * <code>var</code> ; the implementation is recording policy dependent
     * for a flattened policy, the database is checked (automatically flattening explanations)
     * for a non flattened policy, only the value removal is returned
     *
     * @param var an integer variable
     * @param val an integer value
     */
    public Deduction explain(IntVar var, int val) {return null; }
     /**
     * Provides a FLATTENED explanation for the removal of value <code>val</code> from variable
     * <code>var</code>
     *
     * @param var an integer variable
     * @param val an integer value
     */
    public Explanation why(IntVar var, int val) {return null; }


     /**
     * Provides the recorded explanation in database for the removal of value <code>val</code>
     * from variable <code>var</code>
     * The result will depend upon the recording policy of the engine
     *
     * @param var an integer variable
     * @param val an integer value
     */
    public Explanation check(IntVar var, int val) { return null; }




    /**
     * Builds an ExplanationEngine
     * @param env associated solver's environment
     */
    public ExplanationEngine(IEnvironment env) {
        this.env = env;
    }



}
