/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.checker.correctness;

import gnu.trove.map.hash.THashMap;
import java.util.Arrays;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.checker.DomainBuilder;
import org.chocosolver.solver.constraints.checker.Modeler;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;

import java.util.Random;
import org.testng.Assert;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 15/02/11
 */
public class CorrectnessChecker {


    public static void checkCorrectness(Modeler modeler, int nbVar, int lowerB, int upperB, long seed, Object parameters) {
        Random r = new Random(seed);
        //        System.out.printf("Running %s\n", modeler.name());
        THashMap<int[], IntVar> map = new THashMap<>();
        double[] densities = {0.1, 0.25, 0.5, 0.75, 1.0};
        boolean[] homogeneous = {true, false};
        int loop = 0;
        for (int ds = lowerB; ds <= upperB; ds++) {
            for (int ide = 0; ide < densities.length; ide++) {
                for (int h = 0; h < homogeneous.length; h++) {
                    map.clear();
                    int[][] domains = DomainBuilder.buildFullDomains(nbVar, lowerB, ds, r, densities[ide], homogeneous[h]);
                    Model ref = referencePropagation(modeler, nbVar, domains, map, parameters);
                    if (ref == null) break; // no solution found for this generated problem
                    // otherwise, link original domains with reference one.
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

                            Model test = modeler.model(nbVar, _domains, null, parameters);
                            final String format = String
                                .format("ds :%d, ide:%d, h:%d, var:%s, val:%d, loop:%d, seed: %d", ds, ide, h, rvars[d], val, loop, seed);
                            try {
                                if (test.getSolver().solve()) {
                                    System.out.println(format);
                                    System.out.println(String.format("REF:\n%s\n", ref));
                                    ref.getEnvironment().worldPop();
                                    System.out.println(String.format("REF:\n%s\nTEST:\n%s", ref, test));
                                    Assert.fail("one solution found");
                                }
                            } catch (Exception e) {
                                System.out.println(e.getMessage());
                                System.out.println(format);
                                System.out.println("REF:\n" + ref + "\nTEST:\n" + test);
                                Assert.fail();
                            }
                        }
                    }
                }
            }
        }
        //        System.out.printf("loop: %d\n", loop);
    }

    private static Model referencePropagation(Modeler modeler, int nbVar, int[][] domains, THashMap<int[], IntVar> map, Object parameters) {
        Model ref = modeler.model(nbVar, domains, map, parameters);
        ref.getEnvironment().worldPush();
        try {
            ref.getSolver().propagate();
        } catch (ContradictionException e) {
            //            System.out.println("Pas de solution pour ce probleme => rien a tester !");
            return null;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("REF:\n" + ref + "\n");
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