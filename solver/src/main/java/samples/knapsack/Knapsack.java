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
package samples.knapsack;

import choco.kernel.ResolutionPolicy;
import org.kohsuke.args4j.Option;
import samples.AbstractProblem;
import solver.Solver;
import solver.constraints.nary.Sum;
import solver.search.strategy.StrategyFactory;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

import java.io.IOException;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 31/03/11
 */
public class Knapsack extends AbstractProblem {

    @Option(name = "-f", usage = "File name.", required = true)
    String filename;

    @Option(name = "-n", usage = "Restricted to n objects.", required = false)
    int n;

    int[] capacites;
    int[] energies;
    int[] volumes;
    int[] nbOmax;

    public IntVar power;
    public IntVar[] objects;


    public void setUp() {
        parse(filename, n);
    }

    @Override
    public void buildModel() {
        int nos = energies.length;
        solver = new Solver();

        objects = new IntVar[nos];
        for (int i = 0; i < nos; i++) {
            objects[i] = VariableFactory.bounded("o_" + i, 0, nbOmax[i], solver);
        }

        power = VariableFactory.bounded("power", 0, 9999, solver);

        IntVar scalar = VariableFactory.bounded("weight", capacites[0] - 1, capacites[1] + 1, solver);


        solver.post(Sum.eq(objects, volumes, scalar, 1, solver));
        solver.post(Sum.eq(objects, energies, power, 1, solver));
    }

    @Override
    public void configureSolver() {
        solver.set(StrategyFactory.inputOrderInDomainMin(objects, solver.getEnvironment()));
    }

    @Override
    public void solve() {
        solver.findOptimalSolution(ResolutionPolicy.MAXIMIZE, power);
    }

    @Override
    public void prettyOut() {
    }

    public static void main(String[] args) {
        new Knapsack().execute();
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    protected void parse(String fileName, int n) {
        try {
            ParseurKS.parseFile(fileName, n);
        } catch (IOException e) {
            e.printStackTrace();
        }
        capacites = ParseurKS.bounds;
        energies = ParseurKS.instances[0];
        volumes = ParseurKS.instances[1];
        this.n = volumes.length;
        nbOmax = new int[this.n];
        for (int i = 0; i < this.n; i++) {
            nbOmax[i] = capacites[1] / volumes[i];
        }

    }

}
