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

package solver.requests;

import solver.constraints.propagators.Propagator;
import solver.exception.ContradictionException;
import solver.propagation.IQueable;
import solver.propagation.engines.IPropagationEngine;
import solver.variables.EventType;
import solver.variables.Variable;

import java.io.Serializable;

/**
 * Required services a request must provides.
 * <br/>
 * A request is the interface between a propagator (and its related constraint) and a variable.
 * It stores events occuring on a variable and feeds the propagation engine,
 * calls filtering algorithm of the propagator when necessary, etc.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 5 oct. 2010
 */
public interface IRequest<V extends Variable> extends Serializable, IQueable {

    /**
     * Return the propagator declared in <code>this</code>
     * @return the propagator
     */
    Propagator<V> getPropagator();

    /**
     * Returns the variable declared in <code>this</code>
     * @return the variable
     */
    V getVariable();


    int getIndex();

    void setIndex(int idx);

    int getGroup();

    void setGroup(int gidx);

    /**
     * Return the index of <code>this</code> in the requests list of the variable
     * @return index of <code>this</code> in the list of requests of the variable
     */
    int getIdxInVar();

    /**
     * Update the index of <code>this</code> in the list of requests of the variable to <code>idx</code>
     * @param idx new index
     */
    void setIdxInVar(int idx);


    int getIdxVarInProp();
    /**
     * Return the propagation conditions mask of the declared propagator
     * @return propagation conditions maks of the propagator
     */
    int getMask();

    /**
     * Call the filtering algorithm of the declared propagator.
     * Must prepare the propagator to be called.
     * @throws ContradictionException if a contradiction occurs during call to filtering algorithm
     */
    void filter() throws ContradictionException;

    /**
     * Inform <code>this</code> on its variable modification
     * @param e event occured on the variable
     *
     */
    void update(EventType e);

    /**
     * Desactivate <code>this</code>
     */
    void desactivate();

    /**
     * Return the index, in the delta of the variable, of the first element removed but not propagated
     * @return index, in the delta of the variable, of the first element removed but not propagated
     */
    int fromDelta();

    /**
     * Return the index, in the delta of the variable, of the last element removed but not propagated
     * @return index, in the delta of the variable, of the last element removed but not propagated
     */
    int toDelta();

    IPropagationEngine getPropagationEngine();

    void setPropagationEngine(IPropagationEngine engine);
}
