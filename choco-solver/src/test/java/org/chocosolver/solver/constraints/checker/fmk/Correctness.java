/**
 * Copyright (c) 2015, Ecole des Mines de Nantes
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    This product includes software developed by the <organization>.
 * 4. Neither the name of the <organization> nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.solver.constraints.checker.fmk;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.Variable;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import static java.lang.System.arraycopy;
import static java.util.Arrays.copyOfRange;
import static org.chocosolver.solver.Model.writeInFile;
import static org.chocosolver.solver.constraints.checker.fmk.Domain.*;
import static org.testng.Assert.fail;

/**
 * @author Jean-Guillaume Fages, Charles Prud'homme
 * @since 01/13
 */
public class Correctness {

    public static final int INT = 0;
    public static final int BOOL = 1;
    public static final int SET = 2;

    public static void checkCorrectness(SetTestModel modeler, int nbVar, int lowerB, int upperB, long seed, Object parameters) {
        Random r = new Random(seed);
        double[] densities = {0.1, 0.25, 0.5, 0.75, 1.0};
        boolean[] homogeneous = {true, false};
        int loop = 0;
        int[] types = new int[nbVar];
        modeler.fillTypes(types);
        for (int ds = nbVar; ds < upperB; ds++) {
            for (int ide = 0; ide < densities.length; ide++) {
                for (int h = 0; h < homogeneous.length; h++) {
                    Domain[] domains = new Domain[nbVar];
                    for (int i = 0; i < nbVar; i++) {
                        switch (types[i]) {
                            case BOOL:
                                domains[i] = buildBoolDomain(r);
                                break;
                            case INT:
                                domains[i] = buildIntDomain(lowerB, ds, r, densities[ide], homogeneous[h]);
                                break;
                            case SET:
                                domains[i] = buildSetDomain(ds, r, densities[ide], homogeneous[h]);
                                break;
                            default:
                                throw new UnsupportedOperationException();
                        }
                    }
                    Variable[] rvars = new Variable[nbVar];
                    Model ref = referencePropagation(modeler, nbVar, rvars, domains, parameters);
                    if (ref == null) break; // no solution found for this generated problem
                    for (int d = 0; d < nbVar; d++) {
                        if (types[d] == INT || types[d] == BOOL) {
                            int[] values = getRemovedValues((IntVar) rvars[d], domains[d]);
                            for (int v : values) {
                                loop++;
                                Domain[] _domains = new Domain[nbVar];
                                arraycopy(domains, 0, _domains, 0, nbVar);
                                _domains[d] = new Domain(new int[]{v});
                                checkNoSol(modeler, rvars, _domains, parameters, ref, new Object[]{ds, ide, h, rvars[d], v, loop, seed});
                            }
                        } else if (types[d] == SET) {
                            int[] oldEnv = domains[d].getSetEnv();
                            int[] oldKer = domains[d].getSetKer();
                            // removed
                            int[] rems = getRemovedElements((SetVar) rvars[d], oldEnv);
                            for (int v : rems) {
                                loop++;
                                int[] newKer = new int[oldKer.length + 1];
                                arraycopy(oldKer, 0, newKer, 0, oldKer.length);
                                newKer[oldKer.length] = v;
                                Domain[] _domains = new Domain[nbVar];
                                arraycopy(domains, 0, _domains, 0, nbVar);
                                _domains[d] = new Domain(oldEnv, newKer);
                                checkNoSol(modeler, rvars, _domains, parameters, ref, new Object[]{ds, ide, h, rvars[d], v, loop, seed});
                            }
                            // forced
                            int[] enfs = getForcedElements((SetVar) rvars[d], oldKer);
                            for (int v : enfs) {
                                loop++;
                                int[] newEnv = new int[oldEnv.length - 1];
                                int idx = 0;
                                for (int e : oldEnv) {
                                    if (e != v) {
                                        newEnv[idx++] = e;
                                    }
                                }
                                Domain[] _domains = new Domain[nbVar];
                                arraycopy(domains, 0, _domains, 0, nbVar);
                                _domains[d] = new Domain(newEnv, oldKer);
                                checkNoSol(modeler, rvars, _domains, parameters, ref, new Object[]{ds, ide, h, rvars[d], v, loop, seed});
                            }
                        } else {
                            throw new UnsupportedOperationException();
                        }
                    }
                }
            }
        }
//        System.out.printf("loop: %d\n", loop);
    }

    private static Model referencePropagation(SetTestModel modeler, int nbVar, Variable[] rvars, Domain[] domains, Object parameters) {
        Model ref = modeler.model(nbVar, rvars, domains, parameters);
        ref.getEnvironment().worldPush();
        try {
            ref.getResolver().propagate();
        } catch (ContradictionException e) {
//            System.out.println("Pas de solution pour ce probleme => rien a tester !");
            return null;
        } catch (Exception e) {
            File f = new File("SOLVER_ERROR.ser");
            try {
                writeInFile(ref, f);
            } catch (IOException ee) {
                ee.printStackTrace();
            }
            System.out.println(e.getMessage());
            System.out.println("REF:\n" + ref + "\n");
            System.out.println("" + f.getAbsolutePath());
            fail();
        }
        return ref;
    }

    private static void checkNoSol(SetTestModel m, Variable[] rvars, Domain[] _domains, Object parameters, Model ref, Object[] logObjects) {
        int nbVar = rvars.length;
        Model test = m.model(nbVar, rvars, _domains, parameters);
        try {
            if (test.solve()) {
                System.out.println(String.format("ds :%d, ide:%d, h:%d, var:%s, val:%d, loop:%d, seed: %d",
                        logObjects));
                System.out.println("REF:\n" + ref + "\n");
                ref.getEnvironment().worldPop();
                System.out.println(String.format("REF:\n%s\nTEST:\n%s", ref, test));
                fail("one solution found");
            }
        } catch (Exception e) {
            System.out.println(String.format("ds :%d, ide:%d, h:%d, var:%s, val:%d, loop:%d, seed: %d",
                    logObjects));
            System.out.println("REF:\n" + ref + "\n");
            ref.getEnvironment().worldPop();
            System.out.println(String.format("REF:\n%s\nTEST:\n%s", ref, test));
            File f = new File("SOLVER_ERROR.ser");
            try {
                writeInFile(ref, f);
            } catch (IOException ee) {
                ee.printStackTrace();
            }
            System.out.printf("" + f.getAbsolutePath());
            fail();
        }
    }

    ////////////////////////////////////////////////////////////////////////
    private static int[] getRemovedValues(IntVar variable, Domain domain) {
        int[] d = domain.getIntDom();
        int[] _values = new int[d.length];
        int k = 0;
        for (int i : d) {
            if (!variable.contains(i)) {
                _values[k++] = i;
            }
        }
        return copyOfRange(_values, 0, k);
    }

    ////////////////////////////////////////////////////////////////////////
    private static int[] getRemovedElements(SetVar variable, int[] d) {
        int[] _values = new int[d.length];
        int k = 0;
        for (int i : d) {
            if (!variable.getUB().contain(i)) {
                _values[k++] = i;
            }
        }
        return copyOfRange(_values, 0, k);
    }

    ////////////////////////////////////////////////////////////////////////
    private static int[] getForcedElements(SetVar v, int[] d) {
        int[] _values = new int[v.getLB().getSize()];
        int k = 0;
        for (int j : v.getLB()) {
            boolean newEl = true;
            for (int i : d) {
                if (i == j) {
                    newEl = false;
                    break;
                }
            }
            if (newEl) {
                _values[k++] = j;
            }
        }
        return copyOfRange(_values, 0, k);
    }
    ////////////////////////////////////////////////////////////////////////
}
