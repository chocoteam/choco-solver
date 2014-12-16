/*
 * Copyright (c) 1999-2014, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.solver.explanations.arlil;

import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.TIntHashSet;
import org.chocosolver.solver.explanations.store.IEventStore;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IEventType;

import static org.chocosolver.solver.variables.events.PropagatorEventType.FULL_PROPAGATION;

/**
 * Created by cprudhom on 09/12/14.
 * Project: choco.
 */
public class RuleStore {

    final TIntHashSet paRules; // rules for propagator activation, ordered by propagator.id
    final TIntObjectHashMap vmRules;    // rules for variable modification, ordered by variable.id

    public RuleStore() {
        paRules = new TIntHashSet(16, 0.5f, -1);
        vmRules = new TIntObjectHashMap(16, .5f, -1);
    }

    /**
     * Return true if the event represented by matches one of the active rules.
     *
     * @param idx        index in <code>eventStore</code> of the event to evaluate
     * @param eventStore set of events
     */
    public boolean match(final int idx, final IEventStore eventStore) {
//            IntVar var, ICause cause, IEventType etype, int i1, int i2, int i3) {
        IEventType theEvent = eventStore.getEventType(idx);
        if (theEvent.equals(FULL_PROPAGATION)) {
            // the event is a propagator activation
            int pid = eventStore.getFirstValue(idx);
            return paRules.contains(pid);
        } else {
            // the event is a variable modification
            IntVar theVariable = eventStore.getVariable(idx);
            int vid = theVariable.getId();
            if (vmRules.contains(vid)) {
                // TODO: do smth
                throw new UnsupportedOperationException();
            }
        }
        return false;
    }
}
