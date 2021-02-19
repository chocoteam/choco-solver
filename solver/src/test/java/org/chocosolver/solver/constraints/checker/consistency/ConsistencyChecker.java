/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.checker.consistency;

import gnu.trove.map.hash.THashMap;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.checker.Modeler;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;

import java.util.Random;

import static java.lang.System.arraycopy;
import static org.chocosolver.solver.constraints.checker.DomainBuilder.buildDomainsFromVar;
import static org.chocosolver.solver.constraints.checker.DomainBuilder.buildFullDomains;
import static org.chocosolver.solver.constraints.checker.consistency.ConsistencyChecker.Consistency.valueOf;
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
        for (int ds = lowerB; ds <= upperB; ds++) {
            for (int ide = 0; ide < densities.length; ide++) {
                for (int h = 0; h < homogeneous.length; h++) {
                    map.clear();
                    int[][] domains = buildFullDomains(nbVar, lowerB, ds, r, densities[ide], homogeneous[h]);
                    Model ref = referencePropagation(modeler, nbVar, domains, map, parameters);
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

                            Model test = modeler.model(nbVar, _domains, map, parameters);
                            try {
                                if (!test.getSolver().solve()) {
                                    System.out.println(
                                            String.format("ds :%d, ide:%d, h:%d, var:%s, val:%d, loop:%d, seed: %d",
                                            ds, ide, h, rvars[d], val, loop, seed));
                                    System.out.println(String.format("REF:\n%s\nTEST:\n%s", ref, test));
                                    fail("no solution found");
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                System.out.println(e.getMessage());
                                fail();
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
//        LOGGER.error(ref.toString());
        try {
            ref.getSolver().propagate();
        } catch (ContradictionException e) {
//            System.out.printf("Pas de solution pour ce probleme => rien a tester !");
            return null;
        } catch (Exception e) {
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
}
