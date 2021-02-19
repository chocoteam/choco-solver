/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.automata.FA;


import gnu.trove.set.hash.TIntHashSet;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: julien
 * Date: Nov 19, 2010
 * Time: 2:06:37 PM
 */
public interface IAutomaton extends Cloneable {


    IAutomaton clone() throws CloneNotSupportedException;

    List<int[]> getTransitions();

    List<int[]> getTransitions(int i);

    int getInitialState();

    int delta(int k, int j) throws NonDeterministicOperationException;

    void delta(int k, int j, TIntHashSet nexts);

    boolean isFinal(int k);

    boolean isNotFinal(int k);

    int getNbStates();

    boolean run(int[] str);


    class StateNotInAutomatonException extends Exception {
        public StateNotInAutomatonException(int state) {
            super("State " + state + " is not in the automaton, please add it using addState");
        }
    }

    class NonDeterministicOperationException extends Exception {
        public NonDeterministicOperationException() {
            super("This operation can oly be called on a determinitic automaton, please use determinize()");
        }
    }

    class Triple {
        protected int a;
        protected int b;
        protected int c;

        public Triple(int a, int b, int c) {
            this.a = a;
            this.b = b;
            this.c = c;
        }
    }
}
