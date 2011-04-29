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
package solver.views.list;

import solver.ICause;
import solver.variables.EventType;
import solver.variables.domain.delta.IDelta;
import solver.views.IView;

import java.io.Serializable;

/**
 *
 * A IViewList is a container of IView objects.
 * A view can change of state (from active to passive) during a propagation step, this container must consider
 * this information. Restoring the previous state of views must be done upon backtracking.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 23/02/11
 */
public interface IViewList<V extends IView> extends Serializable{

    /**
     * Informs the structure of the status modification of <code>view</code>
     * @param view the modified element
     */
    void setPassive(V view);

    /**
     * Add a new <code>view</code>
     * @param view to add
     */
    void addView(V view);

    /**
     * Permanently delete <code>view</code>
     * @param view to delete
     */
    void deleteView(IView view);

    /**
     * Returns the total number of element contained.
     * @return the total number of element contained.
     */
    int size();

    /**
     * Returns the number of active elements.
     * @return the number of active elements.
     */
    int cardinality();

    /**
     * Notifies the active views of an event occuring on the variable declaring <code>this</code>.
     * This method loops over active views matching the event to update it, avoiding the causing view, if exists.
     * @param cause cause of the notification
     * @param event event implication of the cause
     * @param delta removed values implied by the event
     */
    void notifyButCause(ICause cause, EventType event, IDelta delta);
}
