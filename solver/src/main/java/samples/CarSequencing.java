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
package samples;

import choco.kernel.common.util.tools.ArrayUtils;
import org.slf4j.LoggerFactory;
import solver.Solver;
import solver.constraints.ConstraintFactory;
import solver.constraints.nary.GlobalCardinality;
import solver.constraints.nary.Sum;
import solver.search.strategy.StrategyFactory;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

import java.util.Arrays;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 03/08/11
 */
public class CarSequencing extends AbstractProblem {
    IntVar[] cars;

    int nCars = 10, nClasses = 6, nOptions = 5;

    int[] demands = {1, 1, 2, 2, 2, 2};
    int[][] optfreq = {
            {1, 2},
            {2, 3},
            {1, 3},
            {2, 5},
            {1, 5}
    };
    int[][] matrix = {{1, 0, 1, 1, 0},
            {0, 0, 0, 1, 0},
            {0, 1, 0, 0, 1},
            {0, 1, 0, 1, 0},
            {1, 0, 1, 0, 0},
            {1, 1, 0, 0, 0}};

    int[][] options;
    int[][] idleConfs;

    private void prepare() {
        options = new int[nOptions][];
        idleConfs = new int[nOptions][];
        for (int i = 0; i < matrix[0].length; i++) {
            int nbNulls = 0;
            int nbOnes = 0;
            for (int j = 0; j < matrix.length; j++) {
                if (matrix[j][i] == 1)
                    nbOnes++;
                else
                    nbNulls++;
            }
            options[i] = new int[nbOnes];
            idleConfs[i] = new int[nbNulls];
            int countOnes = 0;
            int countNulls = 0;
            for (int j = 0; j < matrix.length; j++) {
                if (matrix[j][i] == 1) {
                    options[i][countOnes] = j;
                    countOnes++;
                } else {
                    idleConfs[i][countNulls] = j;
                    countNulls++;
                }
            }
        }
    }


    @Override
    public void buildModel() {
        prepare();
        solver = new Solver();
        int max = nClasses - 1;
        cars = VariableFactory.boundedArray("cars", nCars, 0, max, solver);

        IntVar[] expArray = new IntVar[nClasses];

        for (int optNum = 0; optNum < options.length; optNum++) {
            int nbConf = options[optNum].length;
            for (int seqStart = 0; seqStart < (cars.length - optfreq[optNum][1]); seqStart++) {
                IntVar[] carSequence = extractor(cars, seqStart, optfreq[optNum][1]);
                IntVar[] atMost = VariableFactory.boundedArray("atmost", options[optNum].length, 0, max, solver);
                solver.post(GlobalCardinality.make(carSequence, options[optNum], atMost, solver));
                // configurations that include given option may be chosen
                // optfreq[optNum][0] times AT MOST
                for (int i = 0; i < nbConf; i++) {
                    solver.post(ConstraintFactory.leq(atMost[i], optfreq[optNum][0], solver));
                }

                IntVar[] atLeast = VariableFactory.boundedArray("atleast", idleConfs[optNum].length, 0, max, solver);
                solver.post(GlobalCardinality.make(carSequence, idleConfs[optNum], atLeast, solver));
                // all others configurations may be chosen
                solver.post(Sum.geq(atLeast, optfreq[optNum][1] - optfreq[optNum][0], solver));
            }
        }

        int[] values = new int[expArray.length];
        for (int i = 0; i < expArray.length; i++) {
            expArray[i] = VariableFactory.bounded("var", 0, demands[i], solver);
            values[i] = i;
        }
        solver.post(GlobalCardinality.make(cars, values, expArray, solver));
    }

    private static IntVar[] extractor(IntVar[] cars, int initialNumber, int amount) {
        if ((initialNumber + amount) > cars.length) {
            amount = cars.length - initialNumber;
        }
        IntVar[] tmp = new IntVar[amount];
        System.arraycopy(cars, initialNumber, tmp, initialNumber - initialNumber, initialNumber + amount - initialNumber);
        return tmp;
    }

    @Override
    public void configureSolver() {
        solver.set(StrategyFactory.inputOrderMinVal(cars, solver.getEnvironment()));
    }

    @Override
    public void solve() {
        solver.findSolution();
    }

    @Override
    public void prettyOut() {
        LoggerFactory.getLogger("bench").info("Car sequencing");
        LoggerFactory.getLogger("bench").info(String.format("%d %d %d", nCars, nOptions, nClasses));
        int[][] ropt = ArrayUtils.transpose(optfreq);
        LoggerFactory.getLogger("bench").info(String.format("%s", Arrays.toString(ropt[0])));
        LoggerFactory.getLogger("bench").info(String.format("%s", Arrays.toString(ropt[1])));
        for (int i = 0; i < matrix.length; i++) {
            LoggerFactory.getLogger("bench").info(String.format("%d %d %s", i, demands[i], Arrays.toString(matrix[i])));
        }
        LoggerFactory.getLogger("bench").info("\nA valid sequence for this set of cars is:");
        for (int i = 0; i < cars.length; i++) {
            int k = cars[i].getValue();
            LoggerFactory.getLogger("bench").info(String.format("%d\t %s", k, Arrays.toString(matrix[k])));
        }
    }

    public static void main(String[] args) {
        new CarSequencing().execute(args);
    }
}
