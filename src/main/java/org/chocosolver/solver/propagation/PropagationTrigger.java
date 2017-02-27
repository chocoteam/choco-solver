/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2017, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.propagation;

import org.chocosolver.memory.IEnvironment;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.events.PropagatorEventType;
import org.chocosolver.util.objects.IntList;

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
public class PropagationTrigger  {

    private final IPropagationEngine engine; // wrapped engine
    private final IEnvironment environment;
    private final Model model;
    private final boolean DEBUG;

    // stores the static propagators
    private ArrayList<Propagator> sta_propagators = new ArrayList<>();
    // stores the dynamic propagators, ie cut
    private ArrayList<Propagator> perm_propagators = new ArrayList<>();
    // stores the world of the last propagation of the cuts
    private IntList perm_world = new IntList();

    private int size;

    public PropagationTrigger(IPropagationEngine engine, Model model) {
        this.engine = engine;
        this.environment = model.getEnvironment();
        this.model = model;
        size = 0;
        this.DEBUG = model.getSettings().debugPropagation();
    }

    public void addAll(Propagator... propagators) {
        assert perm_propagators.size() == perm_world.size();
        sta_propagators.addAll(Arrays.asList(propagators));
        size += propagators.length;
        if(model.getSettings().sortPropagatorActivationWRTPriority()) {
            sta_propagators.sort((p1, p2) -> p1.getPriority().priority - p2.getPriority().priority);
        }
    }

    public void propagateOnBacktrack(Propagator propagator) {
        assert perm_propagators.size() == perm_world.size();
        int pos = find(propagator);
        if(pos == -1){
            dynAdd(propagator, true);
        }else {
            perm_world.replaceQuick(pos, Integer.MAX_VALUE);
        }
    }

    private int find(Propagator p) {
        int i = 0;
        while (i < perm_propagators.size() && perm_propagators.get(i) != p) {
            i++;
        }
        if (i == perm_propagators.size()) return -1;
        else return i;
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

    public void clear(){
        sta_propagators.clear();
        perm_propagators.clear();
        perm_world.clear();
        size = 0;
    }

    /**
     * Define a way to initialize the propagation engine.
     * Loops over the propagator of the model, then activate them one by one, if stateless,
     * and call propagate after each propagator.
     *
     * @throws ContradictionException can throw a contradiction
     */
    public void propagate() throws ContradictionException {
        if (sta_propagators.size() > 0) {
            for (int p = 0; p < sta_propagators.size(); p++) {
                if (DEBUG) {
                    IPropagationEngine.Trace.printFirstPropagation(sta_propagators.get(p));
                }
                execute(sta_propagators.get(p), engine);
            }
            size -= sta_propagators.size();
            sta_propagators.clear();
        }
        if (perm_propagators.size() > 0) {
            int cw = environment.getWorldIndex(); // get current index
            int p = perm_propagators.size() - 1;
            while (p >= 0 && perm_world.getQuick(p) >= cw) {
                execute(perm_propagators.get(p), engine);
                perm_world.replaceQuick(p, cw);
                p--;
            }
        }
    }

    public static void execute(Propagator toPropagate, IPropagationEngine engine) throws ContradictionException {
        if (toPropagate.isStateLess()) {
            toPropagate.setActive();
            toPropagate.propagate(PropagatorEventType.FULL_PROPAGATION.getMask());
            engine.onPropagatorExecution(toPropagate);
        } else if (toPropagate.isActive()) { // deal with updated propagator
            toPropagate.propagate(PropagatorEventType.FULL_PROPAGATION.getMask());
            engine.onPropagatorExecution(toPropagate);
        }
    }
}
