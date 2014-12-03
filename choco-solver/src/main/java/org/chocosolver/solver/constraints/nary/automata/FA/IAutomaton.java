/**
 *  Copyright (c) 1999-2014, Ecole des Mines de Nantes
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

package org.chocosolver.solver.constraints.nary.automata.FA;


import gnu.trove.set.hash.TIntHashSet;

/**
 * Created by IntelliJ IDEA.
 * User: julien
 * Date: Nov 19, 2010
 * Time: 2:06:37 PM
 */
public interface IAutomaton {


    IAutomaton clone() throws CloneNotSupportedException;

    int getInitialState();

    int delta(int k, int j) throws NonDeterministicOperationException;

    void delta(int k, int j, TIntHashSet nexts);

    boolean isFinal(int k);

    int getNbStates();

    boolean run(int[] str);


    public static class StateNotInAutomatonException extends Exception {
        public StateNotInAutomatonException(int state) {
            super("State " + state + " is not in the automaton, please add it using addState");
        }
    }

    public static class NonDeterministicOperationException extends Exception {
        public NonDeterministicOperationException() {
            super("This operation can oly be called on a determinitic automaton, please use determinize()");
        }
    }

    public static class Triple {
        int a;
        int b;
        int c;

        public Triple(int a, int b, int c) {
            this.a = a;
            this.b = b;
            this.c = c;
        }
    }
}
