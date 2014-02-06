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

import gnu.trove.list.array.TIntArrayList;
import memory.IEnvironment;
import org.slf4j.LoggerFactory;
import solver.Configuration;
import solver.Solver;
import solver.constraints.Propagator;
import solver.exception.ContradictionException;
import solver.variables.EventType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * A specific propagation engine for initial propagation and dynamic addition of propagators during the resolution.
 * <br/>
 * It handles three different types of constraints:
 * - constraints posted before solving the problem,
 * - permanent constraints posted once the resolution has begun,
 * - temporary constraints (removed upon backtrack) posted once the resolution has begun.
 *
 * @author Charles Prud'homme
 * @since 15/12/12
 */
public class PropagationTrigger implements Serializable {

    final IPropagationEngine engine; // wrapped engine
    final IEnvironment environment;
    final Solver solver;

    // stores the static propagators
    ArrayList<Propagator> sta_propagators = new ArrayList<Propagator>();
    // stores the dynamic propagators, ie cut
    ArrayList<Propagator> perm_propagators = new ArrayList<Propagator>();
    // stores the world of the last propagation of the cuts
    TIntArrayList perm_world = new TIntArrayList();

    int size;

    public PropagationTrigger(IPropagationEngine engine, Solver solver) {
        this.engine = engine;
        this.environment = solver.getEnvironment();
        this.solver = solver;
        size = 0;
    }

    public void addAll(Propagator... propagators) {
        assert perm_propagators.size() == perm_world.size();
        sta_propagators.addAll(Arrays.asList(propagators));
        size += propagators.length;
    }

    public void dynAdd(Propagator propagator, boolean permanent) {
        if (permanent) {
            assert perm_propagators.size() == perm_world.size();
            perm_propagators.add(propagator);
            perm_world.add(Integer.MAX_VALUE);
            size++;
        }
    }

    public void remove(Propagator propagator) {
        // Remove a pending propagator, ie, not yet propagated

        // 1. look for a static propagator
        int idx = sta_propagators.indexOf(propagator);
        if (idx > -1) {
            sta_propagators.remove(idx);
        }
        // 2. then, if necessary look for permanent one
        idx = perm_propagators.indexOf(propagator);
        if (idx > -1) {
            perm_propagators.remove(idx);
            perm_world.removeAt(idx);
        }
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
        if (sta_propagators.size() > 0) {
            for (int p = 0; p < sta_propagators.size(); p++) {
                execute(sta_propagators.get(p), engine);
            }
            size -= sta_propagators.size();
            sta_propagators.clear();
        }
        if (perm_propagators.size() > 0) {
            int cw = environment.getWorldIndex(); // get current index
            for (int p = 0; p < perm_propagators.size(); p++) {
                if (perm_world.getQuick(p) >= cw) {
                    execute(perm_propagators.get(p), engine);
                    perm_world.replace(p, cw);
                    // TODO: add a test on the root world to clear the list
                }
            }
        }
        /*if (tmp_propagators.size() > 0) {
            for (int p = 0; p < tmp_propagators.size(); p++) {
                Propagator propagator = tmp_propagators.get(p);
                execute(propagator, engine);
                tmp_propagators.remove(p);
                p--;
                size--;
                tmp_cstr.add(propagator.getConstraint());
            }
            for (final Constraint c : tmp_cstr) {
                // the constraint has been added during the resolution.
                // it should be removed on backtrack -> create a new undo operation
                environment.save(new Operation() {
                    @Override
                    public void undo() {
                        if (LoggerFactory.getLogger("solver").isDebugEnabled()) {
                            LoggerFactory.getLogger("solver").debug("unpost " + c);
                        }
                        solver.unpost(c);
                    }
                });
            }
            tmp_cstr.clear();
        }*/
    }

    public static void execute(Propagator toPropagate, IPropagationEngine engine) throws ContradictionException {
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
