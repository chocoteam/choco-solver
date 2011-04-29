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
package solver.views.conditions;

import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.IStateBool;
import solver.views.ConditionnalView;

/**
 * An abstract class to declare a specific conditions to be satisfied for a conditionnal view.
 * <br/>
 * It provides 2 services: one to check validity, another to keep informed of event occuring on related view(s).
 * </br>
 * In basic behaviour (no condition views), events are propagated automatically after popping.
 * In conditionnal views, previously popped (but not propagated) events should be propagated.
 * That's why on a newly validation, modified variable view must be "added" to the propagation engine,
 * view should act like "no condition one".
 *
 * @author Charles Prud'homme
 * @since 22/03/11
 */
public abstract class AbstractCondition {

    ConditionnalView[] relatedViews; // array of conditionnal views declaring this -- size >= number of elements!
    int idxLastView; // index of the last not null view in relatedViews
    final IStateBool wasValid;

    protected AbstractCondition(IEnvironment environment) {
        wasValid = environment.makeBool(false);
        relatedViews = new ConditionnalView[8];
    }

    /**
     * Keep informed the condition of the modification of one of its related views.
     * If the condition is newly validate, push all related views in the propagation engine.
     *
     * @param view    recently modified view
     * @param evtmask variable modification event
     */
    public final void updateAndValid(ConditionnalView view, int evtmask) {
        update(view, evtmask);
        if (wasValid.get()) {
            view.getPropagationEngine().update(view);
        } else if (isValid()) {
            for (int i = 0; i < idxLastView; i++) {
                ConditionnalView cview = relatedViews[i];
                if (cview.hasChanged()) {
                    cview.getPropagationEngine().update(cview);
                }
            }
            wasValid.set(alwaysValid());
        }
    }

    /**
     * Check if the condition is valid
     *
     * @return true if the condition is satisfied, false otherwise
     */
    abstract boolean isValid();

    /**
     * Return true if the condition, once validate, won't change anymore in the current branch,
     * avoiding validation computation each time.
     * This simulates "no condition" view behaviour.
     *
     * @return true if the condition, once validate, won't change anymore in the current branch
     */
    abstract boolean alwaysValid();

    /**
     * Updates the current condition on the modification of one its related views.
     *
     * @param view    recently modified view
     * @param evtMask
     */
    abstract void update(ConditionnalView view, int evtMask);

    /**
     * Link the <code>view</code> to the condition
     *
     * @param view condition view
     */
    public void linkView(ConditionnalView view) {
        if (idxLastView >= relatedViews.length) {
            ConditionnalView[] tmp = relatedViews;
            relatedViews = new ConditionnalView[tmp.length * 2];
            System.arraycopy(tmp, 0, relatedViews, 0, tmp.length);
        }
        relatedViews[idxLastView++] = view;
    }


}
