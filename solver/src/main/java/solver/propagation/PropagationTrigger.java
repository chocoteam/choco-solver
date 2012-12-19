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
package solver.propagation;

import choco.kernel.memory.IEnvironment;
import gnu.trove.list.array.TIntArrayList;
import org.slf4j.LoggerFactory;
import solver.Configuration;
import solver.Solver;
import solver.constraints.propagators.Propagator;
import solver.exception.ContradictionException;
import solver.variables.EventType;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * A specific propagation engine for initial propagation and dynamic addition of propagators during the resolution.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 15/12/12
 */
public class PropagationTrigger {

    IPropagationEngine engine; // wrapped engine
    IEnvironment environment;

    // stores the static propagators
    ArrayList<Propagator> sta_propagators = new ArrayList<Propagator>();
    // stores the dynamic propagators, ie cut
    ArrayList<Propagator> dyn_propagators = new ArrayList<Propagator>();
    // stores the world of the last propagation of the cuts
    TIntArrayList dyn_world = new TIntArrayList();
    int size;

    public PropagationTrigger(IPropagationEngine engine, Solver solver) {
        this.engine = engine;
        this.environment = solver.getEnvironment();
        size = 0;
    }

    public void addAll(Propagator... propagators) {
        assert dyn_propagators.size() == dyn_world.size();
        sta_propagators.addAll(Arrays.asList(propagators));
        size += propagators.length;
    }

    public void add(Propagator propagator, boolean dynamic) {
        if (dynamic) {
            assert dyn_propagators.size() == dyn_world.size();
            dyn_propagators.add(propagator);
            dyn_world.add(Integer.MAX_VALUE);
        } else {
            sta_propagators.add(propagator);
        }
        size++;
    }

    public boolean needToRun() {
        return size > 0;
    }


    /**
     * Define a way to initialize the propagation engine.
     * Loops over the propagator of the solver, then activate them one by one, if stateless,
     * and call propagate after each propagator.
     *
     * @throws ContradictionException can throw a contradiction
     */
    public void propagate() throws ContradictionException {
        if (dyn_propagators.size() > 0) {
            int cw = environment.getWorldIndex(); // get current index
            for (int p = 0; p < dyn_propagators.size(); p++) {
                if (dyn_world.getQuick(p) >= cw) {
                    execute(dyn_propagators.get(p));
                    dyn_world.replace(p, cw);
                }
            }
        }
        if (sta_propagators.size() > 0) {
            for (int p = 0; p < sta_propagators.size(); p++) {
                execute(sta_propagators.get(p));
            }
            size -= sta_propagators.size();
            sta_propagators.clear();
        }
    }

    private void execute(Propagator toPropagate) throws ContradictionException {
        if (Configuration.PRINT_PROPAGATION) {
            LoggerFactory.getLogger("solver").info("[A] {}", toPropagate);
        }
        if (toPropagate.isStateLess()) {
            toPropagate.setActive();
            toPropagate.propagate(EventType.FULL_PROPAGATION.strengthened_mask);
            engine.onPropagatorExecution(toPropagate);
        }
    }
}
