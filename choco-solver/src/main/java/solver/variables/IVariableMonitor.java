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
package solver.variables;

import solver.ICause;
import solver.exception.ContradictionException;

import java.io.Serializable;

/**
 * A monitor for Variable, to observe variable modification (for integer variable : value removals, bounds modification
 * or instantiation) and do something right after the modification.
 * <p/>
 * This differs from {@link solver.constraints.Propagator} because it is not scheduled in the propagation engine.
 * However, it assumes that <code>this</code> executes fast and low complexity operations.
 * Otherwise, it should be a propagator.
 * <p/>
 * This also differs from {@link solver.variables.view.IView} because it is not a specific variable, and can connect
 * two or more variables together. For instance, this can be used for logging issue.
 * <p/>
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 14/11/11
 */
public interface IVariableMonitor<V extends Variable> extends Serializable, ICause {

    /**
     * Operations to execute after updating the domain variable
     *
     * @param var variable concerned
     * @param evt modification event
     */
    void onUpdate(V var, EventType evt) throws ContradictionException;

}
