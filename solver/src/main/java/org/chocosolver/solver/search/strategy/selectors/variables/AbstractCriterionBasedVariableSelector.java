/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.strategy.selectors.variables;

import gnu.trove.list.array.TIntArrayList;
import org.chocosolver.memory.IEnvironment;
import org.chocosolver.memory.IStateInt;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.loop.monitors.IMonitorContradiction;
import org.chocosolver.solver.search.loop.monitors.IMonitorRestart;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.util.objects.IdentityToDouble;

import java.util.HashSet;
import java.util.List;

/**
 * <p>
 * Project: choco.
 *
 * @author Charles Prud'homme
 * @since 26/02/2020.
 */
public abstract class AbstractCriterionBasedVariableSelector<V extends Variable> implements VariableSelector<V>,
        IMonitorContradiction, IMonitorRestart {

    // TO MANAGE FLUSHING
    protected long flushThs;
    /**
     * Randomness to break ties
     */
    private final java.util.Random random;
    /***
     * Pointer to the last free variable
     */
    private final IStateInt last;
    /**
     * Temporary. Stores index of variables with the same (best) score
     */
    private final TIntArrayList bests = new TIntArrayList();
    /**
     * A reference to the Solver
     */
    protected final Solver solver;
    /**
     * Needed to save operations
     */
    final IEnvironment environment;
    /**
     * Scoring for each variable, is updated dynamically.
     */
    final IdentityToDouble<Variable> weights;

    /**
     * Create a variable selector based on a criterion
     *
     * @param vars  decision variables
     * @param seed  seed for breaking ties randomly, if set to -1, ties are broken by the variable position
     * @param flush flush-threshold. Counts the number of restarts, and flushes the weights if it exceeds this value.
     */
    public AbstractCriterionBasedVariableSelector(V[] vars, long seed, int flush) {
        this.random = seed > -1 ? new java.util.Random(seed) : null;
        this.solver = vars[0].getModel().getSolver();
        this.environment = vars[0].getModel().getEnvironment();
        this.last = environment.makeInt(vars.length - 1);
        this.weights = new IdentityToDouble<>();
        flushThs = flush;
    }

    /**
     * @implNote
     * Seems to be a bad idea to aggregate the weights of the views
     */
    @Override
    public final V getVariable(V[] vars) {
        V best = null;
        bests.resetQuick();
        double w = Double.NEGATIVE_INFINITY;
        int to = last.get();
        for (int idx = 0; idx <= to; idx++) {
            int domSize = vars[idx].getDomainSize();
            if (domSize > 1) {
                double weight = weight(vars[idx]) / domSize;
                if (w < weight) {
                    bests.resetQuick();
                    bests.add(idx);
                    w = weight;
                } else if (w == weight) {
                    bests.add(idx);
                }
            } else {
                // swap
                V tmp = vars[to];
                vars[to] = vars[idx];
                vars[idx] = tmp;
                idx--;
                to--;
            }
        }
        last.set(to);
        if (!bests.isEmpty()) {
            //System.out.printf("%s%n", bests);
            int currentVar = bests.get(0);

            if (random != null) {
                currentVar = bests.get(random.nextInt(bests.size()));
            }
            best = vars[currentVar];
        }
        return best;
    }

    protected abstract double weight(V v);

    int remapInc() {
        return 0;
    }

    /**
     * This method sorts elements wrt to their weight.
     * If 90% of the top {@code FLUSH_TOPS} elements remain unchanged, then weights are flushed
     *
     * @return <i>true</i> if the weights should be flushed
     */
    protected boolean flushWeights() {
      if (solver.getRestartCount() >= flushThs) {
            flushThs = solver.getRestartCount();
            flushThs += 20;
            return true;
        }
        return false;
    }

}
