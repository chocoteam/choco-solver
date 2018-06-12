/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2017, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license. See LICENSE file in the project root for full license
 * information.
 */
package org.chocosolver.solver.constraints.binary;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.set.hash.TIntHashSet;

import org.chocosolver.solver.ISelf;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.extension.Tuples;
import org.chocosolver.solver.constraints.nary.automata.FA.IAutomaton;
import org.chocosolver.solver.variables.IntVar;

/**
 * An interface dedicated to list decomposition of some constraints.
 * <p>
 * Project: choco-solver.
 * @author Charles Prud'homme
 * @since 12/06/2018.
 */
public interface IDecompositionFactory extends ISelf<Model> {

    /**
     * Creates and <b>posts</b> a decomposition of a regular constraint.
     * Enforces the sequence of vars to be a word
     * recognized by the deterministic finite automaton.
     * For example regexp = "(1|2)(3*)(4|5)";
     * The same dfa can be used for different propagators.
     *
     * @param vars      sequence of variables
     * @param automaton a deterministic finite automaton defining the regular language
     * @return array of variables that encodes the states, which can optionally be constrained too.
     */
    default IntVar[] regularDec(IntVar[] vars, IAutomaton automaton) {
        int n = vars.length;
        IntVar[] states = new IntVar[n + 1];
        TIntHashSet[] layer = new TIntHashSet[n + 1];
        for (int i = 0; i <= n; i++) {
            layer[i] = new TIntHashSet();
        }
        layer[0].add(automaton.getInitialState());
        states[0] = ref().intVar("Q_0", layer[0].toArray());
        TIntHashSet nexts = new TIntHashSet();
        for (int i = 0; i < n; i++) {
            int ub = vars[i].getUB();
            Tuples tuples = new Tuples(true);
            for (int j = vars[i].getLB(); j <= ub; j = vars[i].nextValue(j)) {
                TIntIterator layerIter = layer[i].iterator();
                while (layerIter.hasNext()) {
                    int k = layerIter.next();
                    nexts.clear();
                    automaton.delta(k, j, nexts);
                    for (TIntIterator it = nexts.iterator(); it.hasNext(); ) {
                        int succ = it.next();
                        if(i + 1 < n || automaton.isFinal(succ)) {
                            layer[i + 1].add(succ);
                            tuples.add(k, succ, j);
                        }
                    }
                }
            }
            states[i + 1] = ref().intVar("Q_" + (i+1), layer[i + 1].toArray());
            ref().table(new IntVar[]{states[i], states[i + 1], vars[i]}, tuples).post();
        }
        return states;
    }
}
