/*
 * Copyright (c) 1999-2015, Ecole des Mines de Nantes
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
package org.chocosolver.solver;

import org.chocosolver.memory.Environments;
import org.chocosolver.memory.IEnvironment;

/**
 * A solver factory to ease modelling and solving a unique problem with various configurations.
 * Created by cprudhom on 02/06/15.
 * Project: choco.
 */
public class SolverFactory {

    SolverFactory() {
    }

    /**
     * Create a Solver.
     * It is mainly dedicated to mono-thread resolution.
     *
     * @param environment an environment
     * @param name        a name
     * @return a Solver.
     */
    public static Solver makeSolver(IEnvironment environment, String name) {
        return new Solver(environment, name);
    }

    /**
     * Create a Solver.
     * It is mainly dedicated to mono-thread resolution.
     * The environment is set, by default, to {@link org.chocosolver.memory.Environments#TRAIL}.
     *
     * @param name a name
     * @return a Solver.
     */
    public static Solver makeSolver(String name) {
        return new Solver(Environments.DEFAULT.make(), name);
    }

    /**
     * Create a Solver.
     * It is mainly dedicated to mono-thread resolution.
     * The environment is set, by default, to {@link org.chocosolver.memory.Environments#TRAIL}.
     * The default's name is "".
     *
     * @return a Solver.
     */
    public static Solver makeSolver() {
        return new Solver(Environments.DEFAULT.make(), "");
    }


    /**
     * Create a Solver portfolio.
     *
     * @param n number of solvers to create.
     * @return a Solver portfolio.
     */
    public static Portfolio makePortelio(String name, int n) {
        return new Portfolio(name, n);
    }

}
