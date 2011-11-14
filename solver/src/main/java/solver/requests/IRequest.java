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

import choco.kernel.common.MultiDimensionIndex;
import choco.kernel.common.util.procedure.IntProcedure;
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
public interface IRequest<V extends Variable> extends Serializable, IQueable, MultiDimensionIndex {

    public static final int VAR_IN_PROP = 0, IN_VAR = 1, GROUP_ID = 2, IN_GROUP = 3;


    /**
     * Return the propagator declared in <code>this</code>
     *
     * @return the propagator
     */
    Propagator<V> getPropagator();

    /**
     * Returns the variable declared in <code>this</code>
     *
     * @return the variable
     */
    V getVariable();

    /**
     * Return the propagation conditions mask of the declared propagator
     *
     * @return propagation conditions maks of the propagator
     */
    int getMask();

    /**
     * Call the filtering algorithm of the declared propagator.
     * Must prepare the propagator to be called.
     *
     * @throws ContradictionException if a contradiction occurs during call to filtering algorithm
     */
    void filter() throws ContradictionException;

    /**
     * Inform <code>this</code> on its variable modification
     *
     * @param e event occured on the variable
     */
    void update(EventType e);

    /**
     * Activate <code>this</code>
     */
    void activate();

    /**
     * Desactivate <code>this</code>
     */
    void desactivate();

    /**
     * Execute a given procedure <code>proc</code> for every value removed from the variable and not yet propagated
     * @param proc procedure to execute
     * @throws ContradictionException if a contradiction occurs.
     */
    void forEach(IntProcedure proc) throws ContradictionException;

    /**
     * Execute a given procedure <code>proc</code> for every value removed from the variable between the index <code>from</code>
     * to the index <code>to</code>.
     * <br/> <b>For advanced users only!</b>
     * @param proc procedure to execute
     * @param from from index (inclusive) regarding values already propagated by the propagator
     * @param to   from index (exclusive) regarding values already propagated by the propagator
     * @throws ContradictionException if a contradiction occurs.
     */
    void forEach(IntProcedure proc, int from, int to) throws ContradictionException;

    IPropagationEngine getPropagationEngine();

    void setPropagationEngine(IPropagationEngine engine);
}
