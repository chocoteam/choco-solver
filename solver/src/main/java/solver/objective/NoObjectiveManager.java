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

package solver.objective;

import solver.exception.ContradictionException;
import solver.explanations.Deduction;
import solver.explanations.Explanation;

/**
 * This implementation of <code>IObjectiveManager</code> interface provides empty methods for satisfaction problems,
 * where no objective variable is defined. A static reference to this object is available using <code>get()</code>.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 27 juil. 2010
 */
public class NoObjectiveManager extends IObjectiveManager {

    public static final NoObjectiveManager internal = new NoObjectiveManager();

    public static NoObjectiveManager get(){
        return internal;
    }

    private NoObjectiveManager(){}

    /**
     * {@inheritDoc}
     */
    @Override
    public int getBestValue() {
        return 0;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Empty method for <code>this</code>.
     */
    @Override
    public void update() {}

    @Override
    public void postDynamicCut() throws ContradictionException {}

    @Override
    public boolean isOptimization(){
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "";
    }

   
    @Override
    public Explanation explain(Deduction d) {
        return null;  //TODO change body of implemented methods use File | Settings | File Templates.
    }
}
