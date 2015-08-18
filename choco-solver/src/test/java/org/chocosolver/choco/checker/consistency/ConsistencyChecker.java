/**
 * Copyright (c) 2014,
 *       Charles Prud'homme (TASC, INRIA Rennes, LINA CNRS UMR 6241),
 *       Jean-Guillaume Fages (COSLING S.A.S.).
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.choco.checker.consistency;

import gnu.trove.map.hash.THashMap;
import org.chocosolver.choco.checker.Modeler;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import static java.lang.System.arraycopy;
import static org.chocosolver.choco.checker.DomainBuilder.buildDomainsFromVar;
import static org.chocosolver.choco.checker.DomainBuilder.buildFullDomains;
import static org.chocosolver.choco.checker.consistency.ConsistencyChecker.Consistency.valueOf;
import static org.chocosolver.solver.Solver.writeInFile;
import static org.testng.Assert.fail;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 15/02/11
 */
public class ConsistencyChecker {

    enum Consistency {
        ac, bc
    }

    public static void checkConsistency(Modeler modeler, int nbVar, int lowerB, int upperB, Object parameters, long seed, String consistency) {
        Random r = new Random(seed);
//        System.out.printf("Running %s\n", modeler.name());
        Consistency _consistency = valueOf(consistency);

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

                            arraycopy(domains, 0, _domains, 0, d);
                            _domains[d] = new int[]{val};
                            arraycopy(domains, d + 1, _domains, d + 1, nbVar - (d + 1));

                            Solver test = modeler.model(nbVar, _domains, map, parameters);
                            try {
                                if (!test.findSolution()) {
                                    System.out.println(
                                            String.format("ds :%d, ide:%d, h:%d, var:%s, val:%d, loop:%d, seed: %d",
                                            ds, ide, h, rvars[d], val, loop, seed));
                                    System.out.println(String.format("REF:\n%s\nTEST:\n%s", ref, test));
                                    writeDown(ref);
                                    fail("no solution found");
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                System.out.println(e.getMessage());
                                writeDown(ref);
                                fail();
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
            System.out.printf("Pas de solution pour ce probleme => rien a tester !");
            return null;
        } catch (Exception e) {
            writeDown(ref);
            System.out.println(e.getMessage());
            System.out.println(String.format("REF:\n%s\n", ref));
            fail();
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
            writeInFile(ref, f);
        } catch (IOException ee) {
            ee.printStackTrace();
        }
        System.out.println(""+ f.getAbsolutePath());
    }
}
