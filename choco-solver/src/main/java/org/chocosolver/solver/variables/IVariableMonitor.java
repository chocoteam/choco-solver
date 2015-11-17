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
package org.chocosolver.solver.variables;

import gnu.trove.map.hash.THashMap;
import org.chocosolver.solver.ICause;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.events.IEventType;

import java.io.Serializable;

/**
 * A monitor for Variable, to observe variable modification (for integer variable : value removals, bounds modification
 * or instantiation) and do something right after the modification.
 * <p/>
 * This differs from {@link org.chocosolver.solver.constraints.Propagator} because it is not scheduled in the propagation engine.
 * However, it assumes that <code>this</code> executes fast and low complexity operations.
 * Otherwise, it should be a propagator.
 * <p/>
 * This also differs from {@link org.chocosolver.solver.variables.view.IView} because it is not a specific variable, and can connect
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
     *  @param var variable concerned
     * @param evt modification event
	 */
    void onUpdate(V var, IEventType evt) throws ContradictionException;

    /**
     * Duplicate <code>this</code> (which naturally adds it into <code>solver</code>).
     * IMonitor should be duplicated only if it is about modeling and not solving (ie: search).
     * @param solver target solver
     * @param identitymap a map to guarantee uniqueness of objects
     */
    default void duplicate(Solver solver, THashMap<Object, Object> identitymap){
        // nothing to do
    }
}
