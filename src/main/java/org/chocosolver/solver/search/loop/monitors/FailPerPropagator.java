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
package org.chocosolver.solver.search.loop.monitors;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.util.objects.IntMap;

/**
 * A counter which maintains the number of times a propagator fails during the resolution.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 11/06/12
 */
public class FailPerPropagator implements IMonitorContradiction {

    /**
     * Map (propagator - weight), where weight is the number of times the propagator fails.
     */
    protected IntMap p2w;


    /**
     * Create an observer on propagators failures, based on the constraints in input
     * @param constraints set of constraints to observe
     * @param model the target model
     */
    public FailPerPropagator(Constraint[] constraints, Model model) {
        p2w = new IntMap(10, 0);
        init(constraints);
        model.getSolver().plugMonitor(this);
    }

    private void init(Constraint[] constraints) {
        for (Constraint cstr : constraints) {
            for (Propagator propagator : cstr.getPropagators()) {
                p2w.put(propagator.getId(), 0);
            }
        }
    }

    @Override
    public void onContradiction(ContradictionException cex) {
        if (cex.c != null && cex.c instanceof Propagator) {
            p2w.putOrAdjust(((Propagator) cex.c).getId(), 1, 1);
        }
    }

    /**
     * Gets, for a given propagator, the number of times it has failed during the resolution
     * @param p the propagator to evaluate
     * @return the number of times <code>p</code> has failed from the beginning of the resolution
     */
    public int getFails(Propagator p) {
        int f = p2w.get(p.getId());
        return f < 0 ? 0 : f;
    }
}
