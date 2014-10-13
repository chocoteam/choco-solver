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

package choco.checker.consistency;

import choco.checker.Modeler;
import gnu.trove.map.hash.THashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import solver.Solver;
import solver.exception.ContradictionException;
import solver.variables.IntVar;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import static choco.checker.DomainBuilder.buildDomainsFromVar;
import static choco.checker.DomainBuilder.buildFullDomains;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 15/02/11
 */
public class ConsistencyChecker {

    private static Logger LOGGER = LoggerFactory.getLogger("test");

    enum Consistency {
        ac, bc
    }

    public static void checkConsistency(Modeler modeler, int nbVar, int lowerB, int upperB, Object parameters, long seed, String consistency) {
        Random r = new Random(seed);
//        System.out.printf("Running %s\n", modeler.name());
        Consistency _consistency = Consistency.valueOf(consistency);

        THashMap<int[], IntVar> map = new THashMap<>();
        double[] densities = {0.1, 0.25, 0.5, 0.75, 1.0};
        boolean[] homogeneous = {true, false};
        int loop = 0;
        for (int ds = lowerB; ds < upperB; ds++) {
            for (int ide = 0; ide < densities.length; ide++) {
                for (int h = 0; h < homogeneous.length; h++) {
                    map.clear();
                    int[][] domains = buildFullDomains(nbVar, lowerB, ds, r, densities[ide], homogeneous[h]);
                    Solver ref = referencePropagation(modeler, nbVar, domains, map, parameters);
                    if (ref == null) break; // no solution found for this generated problem
                    // otherwise, link original domains with reference one.
                    IntVar[] rvars = new IntVar[nbVar];
                    for (int k = 0; k < nbVar; k++) {
                        rvars[k] = map.get(domains[k]);
                    }
                    // and get the reduced domain
                    domains = buildDomainsFromVar(rvars);
                    for (int d = 0; d < domains.length; d++) {
                        int[] values = getValues(domains[d], _consistency);
                        for (int v = 0; v < values.length; v++) {
                            loop++;
                            int val = values[v];
                            int[][] _domains = new int[nbVar][];

                            System.arraycopy(domains, 0, _domains, 0, d);
                            _domains[d] = new int[]{val};
                            System.arraycopy(domains, d + 1, _domains, d + 1, nbVar - (d + 1));

                            Solver test = modeler.model(nbVar, _domains, map, parameters);
                            try {
                                if (!test.findSolution()) {
                                    LOGGER.error("ds :{}, ide:{}, h:{}, var:{}, val:{}, loop:{}, seed: {}",
                                            ds, ide, h, rvars[d], val, loop, seed);
                                    LOGGER.error("REF:\n{}\nTEST:\n{}", ref, test);
                                    writeDown(ref);
                                    Assert.fail("no solution found");
                                }
                            } catch (Exception e) {
                                LOGGER.error(e.getMessage());
                                LOGGER.error("ds :{}, ide:{}, h:{}, var:{}, val:{}, loop:{}, seed: {}",
                                        ds, ide, h, rvars[d], val, loop, seed);
                                LOGGER.error("REF:\n{}\nTEST:\n{}", ref, test);
                                writeDown(ref);
                                Assert.fail();
                            }
                        }
                    }
                }
            }
        }
//        System.out.printf("loop: %d\n", loop);
    }

    private static Solver referencePropagation(Modeler modeler, int nbVar, int[][] domains, THashMap<int[], IntVar> map, Object parameters) {
        Solver ref = modeler.model(nbVar, domains, map, parameters);
//        LOGGER.error(ref.toString());
        try {
            ref.propagate();
        } catch (ContradictionException e) {
            LOGGER.info("Pas de solution pour ce probleme => rien a tester !");
            return null;
        } catch (Exception e) {
            writeDown(ref);
            LOGGER.error(e.getMessage());
            LOGGER.error("REF:\n{}\n", ref);
            Assert.fail();
        }
        return ref;
    }

    private static int[] getValues(int[] domain, Consistency consistency) {
        switch (consistency) {
            case ac:
                return domain;
            case bc:
                if (domain.length == 1) {
                    return new int[]{domain[0]};
                } else {
                    return new int[]{domain[0], domain[domain.length - 1]};
                }
            default:
                throw new UnsupportedOperationException();
        }
    }

    protected static void writeDown(Solver ref) {
        File f = new File("SOLVER_ERROR.ser");
        try {
            Solver.writeInFile(ref, f);
        } catch (IOException ee) {
            ee.printStackTrace();
        }
        LoggerFactory.getLogger("test").error("{}", f.getAbsolutePath());
    }
}
