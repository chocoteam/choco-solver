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
package sandbox;

import solver.Solver;
import solver.constraints.binary.Element;
import solver.exception.ContradictionException;
import solver.search.strategy.StrategyFactory;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

import java.util.Random;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 19/05/11
 */
public class ArnaudTest {

    public static void main(String[] args) throws ContradictionException {
        long t1 = System.currentTimeMillis();
        Solver s = new Solver();
        int nv = 15000;
        IntVar[] vars = new IntVar[2 * nv];
        //create variables
        for (int i = 0; i < nv; i++) {
            vars[2 * i] = VariableFactory.enumerated("i_" + i, 0, 100, s); //index vars
            vars[2 * i + 1] = VariableFactory.enumerated("v_" + i, 0, 3500, s); //value vars
//           s.createBoundIntVar("v_"+i, 0, 3500); //value vars
//            s.createListIntVar("v_" + i, 0, 3500); //value vars

        }
        //create values arrays
        int[][] values = new int[5][101];
        Random rnd = new Random();
        for (int i = 0; i < values.length; i++) {
            for (int j = 0; j < values[i].length; j++) {
                values[i][j] = rnd.nextInt(3501);
            }
        }
        //create constraints
        for (int i = 0; i < nv; i++) {
            s.post(new Element(vars[2 * i + 1], values[i % 5], vars[2 * i], s));
        }
        long t2 = System.currentTimeMillis();
        s.set(StrategyFactory.presetI(vars, s.getEnvironment()));
        System.out.println("build solver: " + (t2 - t1) + " ms");
        t1 = System.currentTimeMillis();
        s.propagate();
        t2 = System.currentTimeMillis();
        System.out.println("pi: " + (t2 - t1) + " ms");
        t1 = System.currentTimeMillis();
        s.findSolution();
        t2 = System.currentTimeMillis();
        System.out.println("res: " + (t2 - t1) + " ms");

    }
}
