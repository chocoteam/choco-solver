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

/**
 * A monitor for Variable, to observe variable modification (for integer variable : value removals, bounds modification
 * or instantiation.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 14/11/11
 */
public interface IVariableMonitor<V extends Variable> {

    /**
     * Operations to execute before updating the domain variable
     *
     * @param var   variable concerned
     * @param evt   modification event
     * @param cause origin of the modification
     */
    void beforeUpdate(V var, EventType evt, ICause cause);

    /**
     * Operations to execute after updating the domain variable
     *
     * @param var   variable concerned
     * @param evt   modification event
     * @param cause origin of the modification
     */
    void afterUpdate(V var, EventType evt, ICause cause);

    /**
     * Operations to execute if a contradiction occurs during variable modification
     *
     * @param var   variable concerned
     * @param evt   modification event
     * @param cause origin of the modification
     */
    void contradict(V var, EventType evt, ICause cause);

    /**
     * Return the index of <code>this</code> in <code>variable</code>
     *
     * @param variable a variable, must be a known <code>this</code>
     * @return index index of <code>this</code> in <code>variable</code> list of event recorder
     */
    public abstract int getIdxInV(V variable);

    /**
     * Return the index of <code>this</code> in <code>variable</code>
     *
     * @param variable a variable, must be a known <code>this</code>
     * @param idx      index of <code>this</code> in <code>variable</code> list of event recorder
     */
    public abstract void setIdxInV(V variable, int idx);

}
