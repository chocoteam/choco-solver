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

import org.kohsuke.args4j.Option;
import org.slf4j.LoggerFactory;
import solver.Solver;
import solver.constraints.nary.GlobalCardinality;
import solver.constraints.nary.Sum;
import solver.search.strategy.StrategyFactory;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

import java.util.Arrays;
import java.util.Scanner;

/**
 * CSPLib prob001:<br/>
 * "A number of cars are to be produced;
 * they are not identical, because different options are available as variants on the basic model.
 * <br/>
 * The assembly line has different stations which install the various options (air-conditioning, sun-roof, etc.).
 * These stations have been designed to handle at most a certain percentage of the cars passing along the assembly line.
 * Furthermore, the cars requiring a certain option must not be bunched together,
 * otherwise the station will not be able to cope.
 * Consequently, the cars must be arranged in a sequence so that the capacity of each station is never exceeded.
 * <br/>
 * For instance, if a particular station can only cope with at most half of the cars passing along the line,
 * the sequence must be built so that at most 1 car in any 2 requires that option.
 * <br/>
 * The problem has been shown to be NP-complete (Gent 1999)"
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 03/08/11
 */
public class CarSequencing extends AbstractProblem {

    @Option(name = "-d", aliases = "--data", usage = "Car sequencing data.", required = false)
    Data data = Data.P4_72;

    IntVar[] cars;

    int nCars, nClasses, nOptions;

    int[] demands;
    int[][] optfreq, matrix, options, idleConfs;


    @Override
    public void buildModel() {
        parse(data.source());
        prepare();
        solver = new Solver();
        int max = nClasses - 1;
        cars = VariableFactory.enumeratedArray("cars", nCars, 0, max, solver);

        IntVar[] expArray = new IntVar[nClasses];

        for (int optNum = 0; optNum < options.length; optNum++) {
            int nbConf = options[optNum].length;
            for (int seqStart = 0; seqStart < (cars.length - optfreq[optNum][1]); seqStart++) {
                IntVar[] carSequence = extractor(cars, seqStart, optfreq[optNum][1]);

                // configurations that include given option may be chosen
                IntVar[] atMost = new IntVar[nbConf];
                for (int i = 0; i < nbConf; i++) {
                // optfreq[optNum][0] times AT MOST
                    atMost[i] = VariableFactory.bounded("atmost_" + optNum + "_" + seqStart + "_" + nbConf, 0, optfreq[optNum][0], solver);
                }

                solver.post(GlobalCardinality.make(carSequence, options[optNum], atMost, solver));

                IntVar[] atLeast = VariableFactory.boundedArray("atleast_"+optNum+"_"+seqStart, idleConfs[optNum].length, 0, max, solver);
                solver.post(GlobalCardinality.make(carSequence, idleConfs[optNum], atLeast, solver));
                // all others configurations may be chosen
                solver.post(Sum.geq(atLeast, optfreq[optNum][1] - optfreq[optNum][0], solver));
            }
        }

        int[] values = new int[expArray.length];
        for (int i = 0; i < expArray.length; i++) {
            expArray[i] = VariableFactory.enumerated("var_"+i, 0, demands[i], solver);
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
        solver.set(StrategyFactory.minDomMinVal(cars, solver.getEnvironment()));
        /*solver.getEngine().addGroup(
                Group.buildGroup(
                        Predicates.light(),
                        new Seq(IncrArityP.get(),
                                IncrDomDeg.get()),
                        Policy.FIXPOINT));
        solver.getEngine().addGroup(
                Group.buildGroup(
                        Predicates.all(),
                        IncrArityP.get(),
                        Policy.ONE));*/

    }

    @Override
    public void solve() {
        solver.findSolution();
    }

    @Override
    public void prettyOut() {
        LoggerFactory.getLogger("bench").info("Car sequencing : {}", data.name());
        LoggerFactory.getLogger("bench").info(data.source());
        LoggerFactory.getLogger("bench").info("\nA valid sequence for this set of cars is:");
        for (int i = 0; i < cars.length; i++) {
            int k = cars[i].getValue();
            LoggerFactory.getLogger("bench").info(String.format("%d\t %s", k, Arrays.toString(matrix[k])));
        }
    }

    public static void main(String[] args) {
        new CarSequencing().execute(args);
    }

    private int[][] parse(String source) {
        int[][] data = null;
        Scanner sc = new Scanner(source);
        nCars = sc.nextInt();
        nOptions = sc.nextInt();
        nClasses = sc.nextInt();

        optfreq = new int[nOptions][2];
        // get frequencies
        for (int i = 0; i < nOptions; i++) {
            optfreq[i][0] = sc.nextInt();
        }
        for (int i = 0; i < nOptions; i++) {
            optfreq[i][1] = sc.nextInt();
        }

        // get the demand and options
        demands = new int[nClasses];
        matrix = new int[nClasses][nOptions];
        for (int i = 0; i < nClasses; i++) {
            sc.nextInt();
            demands[i] = sc.nextInt();
            for (int j = 0; j < nOptions; j++) {
                matrix[i][j] = sc.nextInt();
            }
        }
        sc.close();
        return data;
    }

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

    /////////////////////////////////// DATA //////////////////////////////////////////////////

    static enum Data {
        myPb("10 5 6\n" +
                "1 2 1 2 1\n" +
                "2 3 3 5 5\n" +
                "0 1 1 0 1 1 0\n" +
                "1 1 0 0 0 1 0\n" +
                "2 2 0 1 0 0 1\n" +
                "3 2 0 1 0 1 0\n" +
                "4 2 1 0 1 0 0\n" +
                "5 2 1 1 0 0 0"),
        P4_72("80 5 22\n" +
                "1 2 1 2 1\n" +
                "2 3 3 5 5\n" +
                "0 6 1 0 0 1 0\n" +
                "1 10 1 1 1 0 0\n" +
                "2 2 1 1 0 0 1\n" +
                "3 2 0 1 1 0 0\n" +
                "4 8 0 0 0 1 0\n" +
                "5 15 0 1 0 0 0\n" +
                "6 1 0 1 1 1 0\n" +
                "7 5 0 0 1 1 0\n" +
                "8 2 1 0 1 1 0\n" +
                "9 3 0 0 1 0 0\n" +
                "10 2 1 0 1 0 0\n" +
                "11 1 1 1 1 0 1\n" +
                "12 8 0 1 0 1 0\n" +
                "13 3 1 0 0 1 1\n" +
                "14 10 1 0 0 0 0\n" +
                "15 4 0 1 0 0 1\n" +
                "16 4 0 0 0 0 1\n" +
                "17 2 1 0 0 0 1\n" +
                "18 4 1 1 0 0 0\n" +
                "19 6 1 1 0 1 0\n" +
                "20 1 1 0 1 0 1\n" +
                "21 1 1 1 1 1 1"),
        P6_76("100 5 22\n" +
                "1 2 1 2 1\n" +
                "2 3 3 5 5\n" +
                "0 13 1 0 0 0 0\n" +
                "1 8 0 0 0 1 0\n" +
                "2 7 0 1 0 0 0\n" +
                "3 1 1 0 0 1 0\n" +
                "4 12 0 0 1 0 0\n" +
                "5 5 0 1 0 1 0\n" +
                "6 5 0 0 1 1 0\n" +
                "7 6 0 1 1 0 0\n" +
                "8 3 1 0 0 0 1\n" +
                "9 12 1 1 0 0 0\n" +
                "10 8 1 1 0 1 0\n" +
                "11 2 1 0 0 1 1\n" +
                "12 2 1 1 1 0 0\n" +
                "13 1 0 1 0 1 1\n" +
                "14 4 1 0 1 0 0\n" +
                "15 4 0 1 0 0 1\n" +
                "16 1 1 1 0 1 1\n" +
                "17 2 1 0 1 1 0\n" +
                "18 1 0 0 0 0 1\n" +
                "19 1 1 1 1 1 0\n" +
                "20 1 1 1 0 0 1\n" +
                "21 1 0 1 1 1 0"),
        P10_93("100 5 25\n" +
                "1 2 1 2 1\n" +
                "2 3 3 5 5\n" +
                "0 7 1 0 0 1 0\n" +
                "1 11 1 1 0 0 0\n" +
                "2 1 0 1 1 1 1\n" +
                "3 3 1 0 1 0 0\n" +
                "4 15 0 1 0 0 0\n" +
                "5 2 1 0 1 1 0\n" +
                "6 8 0 1 0 1 0\n" +
                "7 5 0 0 1 0 0\n" +
                "8 3 0 0 0 1 0\n" +
                "9 4 0 1 1 1 0\n" +
                "10 5 1 0 0 0 0\n" +
                "11 2 1 1 1 0 1\n" +
                "12 6 0 1 1 0 0\n" +
                "13 2 0 0 1 0 1\n" +
                "14 2 0 1 0 0 1\n" +
                "15 4 1 1 1 1 0\n" +
                "16 3 1 0 0 0 1\n" +
                "17 5 1 1 0 1 0\n" +
                "18 2 1 1 1 0 0\n" +
                "19 4 1 1 0 0 1\n" +
                "20 1 1 0 0 1 1\n" +
                "21 1 1 1 0 1 1\n" +
                "22 1 0 1 0 1 1\n" +
                "23 1 0 1 1 0 1\n" +
                "24 2 0 0 0 0 1"),
        P16_81("100 5 26\n" +
                "1 2 1 2 1\n" +
                "2 3 3 5 5\n" +
                "0 10 1 0 0 0 0\n" +
                "1 2 0 0 0 0 1\n" +
                "2 8 0 1 0 1 0\n" +
                "3 8 0 0 0 1 0\n" +
                "4 6 0 1 1 0 0\n" +
                "5 11 0 1 0 0 0\n" +
                "6 3 0 0 1 0 0\n" +
                "7 2 0 0 1 1 0\n" +
                "8 7 1 1 0 0 0\n" +
                "9 2 1 0 0 1 1\n" +
                "10 4 1 0 1 0 0\n" +
                "11 7 1 0 0 1 0\n" +
                "12 1 1 1 1 0 1\n" +
                "13 3 0 1 1 1 0\n" +
                "14 4 0 1 0 0 1\n" +
                "15 5 1 1 1 0 0\n" +
                "16 2 1 1 0 0 1\n" +
                "17 1 1 0 1 1 1\n" +
                "18 2 1 0 1 1 0\n" +
                "19 3 1 0 0 0 1\n" +
                "20 2 0 1 1 0 1\n" +
                "21 1 0 1 0 1 1\n" +
                "22 3 1 1 0 1 0\n" +
                "23 1 0 0 1 1 1\n" +
                "24 1 1 1 1 1 1\n" +
                "25 1 1 1 1 1 0"),
        P10_71("100 5 23\n" +
                "1 2 1 2 1\n" +
                "2 3 3 5 5\n" +
                "0 2 0 0 0 1 1\n" +
                "1 2 0 0 1 0 1\n" +
                "2 5 0 1 1 1 0\n" +
                "3 4 0 0 0 1 0\n" +
                "4 4 0 1 0 1 0\n" +
                "5 1 1 1 0 0 1\n" +
                "6 3 1 1 1 0 1\n" +
                "7 4 0 0 1 0 0\n" +
                "8 19 0 1 0 0 0\n" +
                "9 7 1 1 0 1 0\n" +
                "10 10 1 0 0 0 0\n" +
                "11 1 0 0 1 1 0\n" +
                "12 5 1 1 1 1 0\n" +
                "13 2 1 0 1 1 0\n" +
                "14 6 1 1 0 0 0\n" +
                "15 4 1 1 1 0 0\n" +
                "16 8 1 0 0 1 0\n" +
                "17 1 1 0 0 0 1\n" +
                "18 4 0 1 1 0 0\n" +
                "19 2 0 0 0 0 1\n" +
                "20 4 0 1 0 0 1\n" +
                "21 1 1 1 0 1 1\n" +
                "22 1 0 1 1 0 1"),
        P21_90("100 5 23\n" +
                "1 2 1 2 1\n" +
                "2 3 3 5 5\n" +
                "0 14 0 1 0 0 0\n" +
                "1 11 1 0 0 0 0\n" +
                "2 2 0 1 1 1 0\n" +
                "3 1 0 1 1 0 1\n" +
                "4 1 1 0 0 1 1\n" +
                "5 3 1 0 1 0 0\n" +
                "6 5 0 0 0 1 0\n" +
                "7 4 1 0 0 1 0\n" +
                "8 1 1 1 1 1 1\n" +
                "9 5 0 0 1 0 0\n" +
                "10 3 1 1 0 1 0\n" +
                "11 2 1 1 0 1 1\n" +
                "12 2 1 1 1 0 1\n" +
                "13 7 0 1 1 0 0\n" +
                "14 9 0 1 0 1 0\n" +
                "15 14 1 1 0 0 0\n" +
                "16 3 0 1 0 1 1\n" +
                "17 2 0 0 1 0 1\n" +
                "18 6 1 1 1 0 0\n" +
                "19 2 1 1 1 1 0\n" +
                "20 1 0 1 0 0 1\n" +
                "21 1 0 0 0 0 1\n" +
                "22 1 0 0 0 1 1"),
        P36_92("100 5 22\n" +
                "1 2 1 2 1\n" +
                "2 3 3 5 5\n" +
                "0 20 0 1 0 0 0\n" +
                "1 7 1 1 1 0 0\n" +
                "2 3 0 0 1 1 0\n" +
                "3 9 0 0 0 1 0\n" +
                "4 3 0 0 0 0 1\n" +
                "5 1 0 1 1 1 1\n" +
                "6 7 1 0 0 0 0\n" +
                "7 3 0 1 0 0 1\n" +
                "8 3 1 1 1 1 0\n" +
                "9 1 1 0 0 1 1\n" +
                "10 2 1 1 0 0 1\n" +
                "11 5 0 1 1 1 0\n" +
                "12 9 1 1 0 0 0\n" +
                "13 3 0 1 0 1 0\n" +
                "14 1 1 0 1 1 1\n" +
                "15 6 1 1 0 1 0\n" +
                "16 4 1 0 0 1 0\n" +
                "17 7 0 1 1 0 0\n" +
                "18 1 1 1 0 1 1\n" +
                "19 2 1 0 0 0 1\n" +
                "20 2 1 0 1 1 0\n" +
                "21 1 0 0 0 1 1"),
        P41_66("100 5 19\n" +
                "1 2 1 2 1\n" +
                "2 3 3 5 5\n" +
                "0 7 1 0 0 0 0\n" +
                "1 9 0 1 1 0 0\n" +
                "2 4 0 0 0 1 0\n" +
                "3 2 0 1 0 1 1\n" +
                "4 6 0 0 1 0 0\n" +
                "5 18 0 1 0 0 0\n" +
                "6 6 0 1 0 0 1\n" +
                "7 6 0 0 0 0 1\n" +
                "8 1 1 1 0 1 1\n" +
                "9 10 1 1 0 0 0\n" +
                "10 2 1 0 0 0 1\n" +
                "11 11 0 1 0 1 0\n" +
                "12 5 0 0 1 1 0\n" +
                "13 1 0 1 1 1 0\n" +
                "14 1 0 1 1 0 1\n" +
                "15 3 1 0 1 0 0\n" +
                "16 3 1 1 1 0 0\n" +
                "17 3 1 1 0 1 0\n" +
                "18 2 1 1 1 1 0"),
        P26_82("100 5 24\n" +
                "1 2 1 2 1\n" +
                "2 3 3 5 5\n" +
                "0 2 1 1 0 1 0\n" +
                "1 13 0 1 0 0 0\n" +
                "2 10 0 1 0 1 0\n" +
                "3 14 1 1 0 0 0\n" +
                "4 5 0 0 0 1 0\n" +
                "5 2 0 1 0 1 1\n" +
                "6 2 0 1 1 0 0\n" +
                "7 8 1 0 0 1 0\n" +
                "8 5 0 0 1 1 0\n" +
                "9 3 1 1 1 0 0\n" +
                "10 9 1 0 0 0 0\n" +
                "11 6 1 1 0 0 1\n" +
                "12 2 1 1 1 1 0\n" +
                "13 2 0 0 0 0 1\n" +
                "14 1 1 1 1 0 1\n" +
                "15 2 0 1 1 1 0\n" +
                "16 2 1 0 1 0 0\n" +
                "17 1 1 0 0 0 1\n" +
                "18 1 1 0 1 1 0\n" +
                "19 6 0 0 1 0 0\n" +
                "20 1 1 1 1 1 1\n" +
                "21 1 0 0 1 1 1\n" +
                "22 1 0 1 1 0 1\n" +
                "23 1 0 0 1 0 1"),;

        final String source;

        Data(String source) {
            this.source = source;
        }

        String source() {
            return source;
        }
    }

}
