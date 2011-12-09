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

package choco.checker;

import gnu.trove.map.hash.THashMap;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import solver.Solver;
import solver.exception.ContradictionException;
import solver.variables.IntVar;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

import static choco.checker.DomainBuilder.buildFullDomains;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 15/02/11
 */
public class CorrectnessChecker {

    public static void checkCorrectness(Modeler modeler, int nbVar, int lowerB, int upperB, long seed, Object parameters) {
        Random r = new Random(seed);

        THashMap<int[], IntVar> map = new THashMap<int[], IntVar>();
        double[] densities = {0.1, 0.25, 0.5, 0.75, 1.0};
        boolean[] homogeneous = {true, false};
        int loop = 0;
        for (int ds = nbVar; ds < upperB; ds++) {
            for (int ide = 0; ide < densities.length; ide++) {
                for (int h = 0; h < homogeneous.length; h++) {
                    map.clear();
                    int[][] domains = buildFullDomains(nbVar, lowerB, ds, r, densities[ide], homogeneous[h]);
                    Solver ref = referencePropagation(modeler, nbVar, domains, map, parameters);
                    if (ref == null) break; // no solution found for this generated problem
                    // otherwise, link original domains with refernce one.
                    IntVar[] rvars = new IntVar[nbVar];
                    for (int k = 0; k < nbVar; k++) {
                        rvars[k] = map.get(domains[k]);
                    }
                    for (int d = 0; d < domains.length; d++) {
                        int[] values = getRemovedValues(rvars[d], domains[d]);
                        for (int v = 0; v < values.length; v++) {
                            loop++;
                            int val = values[v];
                            int[][] _domains = new int[nbVar][];

                            System.arraycopy(domains, 0, _domains, 0, d);
                            _domains[d] = new int[]{val};
                            System.arraycopy(domains, d + 1, _domains, d + 1, nbVar - (d + 1));

                            Solver test = modeler.model(nbVar, _domains, map, parameters);
                            try {
                                if (test.findSolution()) {
                                    LoggerFactory.getLogger("test").error("ds :{}, ide:{}, h:{}, var:{}, val:{}, loop:{}, seed: {}",
                                            new Object[]{ds, ide, h, rvars[d], val, loop, seed});
                                    LoggerFactory.getLogger("test").error("REF:\n{}\nTEST:\n{}", ref, test);
                                    Assert.fail("one solution found");
                                }
                            } catch (Exception e) {
                                LoggerFactory.getLogger("test").error(e.getMessage());
                                LoggerFactory.getLogger("test").error("ds :{}, ide:{}, h:{}, var:{}, val:{}, loop:{}, seed: {}",
                                        new Object[]{ds, ide, h, rvars[d], val, loop, seed});
                                LoggerFactory.getLogger("test").error("REF:\n{}\nTEST:\n{}", ref, test);
                                File f = new File("SOLVER_ERROR.ser");
                                try {
                                    Solver.writeInFile(ref, f);
                                } catch (IOException ee) {
                                    ee.printStackTrace();
                                }
                                LoggerFactory.getLogger("test").error("{}", f.getAbsolutePath());
                                Assert.fail();
                            }
                        }
                    }
                }
            }
        }
        System.out.printf("loop: %d\n", loop);
    }

    private static Solver referencePropagation(Modeler modeler, int nbVar, int[][] domains, THashMap<int[], IntVar> map, Object parameters) {
        Solver ref = modeler.model(nbVar, domains, map, parameters);
        try {
            ref.propagate();
        } catch (ContradictionException e) {
            LoggerFactory.getLogger("test").info("Pas de solution pour ce probleme => rien a tester !");
            return null;
        } catch (Exception e) {
            File f = new File("SOLVER_ERROR.ser");
            try {
                Solver.writeInFile(ref, f);
            } catch (IOException ee) {
                ee.printStackTrace();
            }
            LoggerFactory.getLogger("test").error(e.getMessage());
            LoggerFactory.getLogger("test").error("REF:\n{}\n", ref);
            LoggerFactory.getLogger("test").error("{}", f.getAbsolutePath());
            Assert.fail();
        }
        return ref;
    }

    private static int[] getRemovedValues(IntVar variable, int[] domain) {
        int[] _values = domain.clone();
        int k = 0;
        for (int i = 0; i < domain.length; i++) {
            if (!variable.contains(domain[i])) {
                _values[k++] = domain[i];
            }
        }
        return Arrays.copyOfRange(_values, 0, k);
    }

}
