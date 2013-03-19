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

package choco.checker.fmk;

import org.slf4j.LoggerFactory;
import org.testng.Assert;
import solver.Solver;
import solver.exception.ContradictionException;
import solver.variables.IntVar;
import solver.variables.SetVar;
import solver.variables.Variable;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

/**
 * @author Jean-Guillaume Fages, Charles Prud'homme
 * @since 01/13
 */
public class Correctness {

    public static final int INT = 0;
    public static final int BOOL = 1;
    public static final int SET = 2;

    public static void checkCorrectness(Model modeler, int nbVar, int lowerB, int upperB, long seed, Object parameters) {
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
                                domains[i] = Domain.buildBoolDomain(r);
                                break;
                            case INT:
                                domains[i] = Domain.buildIntDomain(lowerB, ds, r, densities[ide], homogeneous[h]);
                                break;
                            case SET:
                                domains[i] = Domain.buildSetDomain(ds, r, densities[ide], homogeneous[h]);
                                break;
                            default:
                                throw new UnsupportedOperationException();
                        }
                    }
                    Variable[] rvars = new Variable[nbVar];
                    Solver ref = referencePropagation(modeler, nbVar, rvars, domains, parameters);
                    if (ref == null) break; // no solution found for this generated problem
                    for (int d = 0; d < nbVar; d++) {
                        if (types[d] == INT || types[d] == BOOL) {
                            int[] values = getRemovedValues((IntVar) rvars[d], domains[d]);
                            for (int v : values) {
                                loop++;
                                Domain[] _domains = new Domain[nbVar];
                                System.arraycopy(domains, 0, _domains, 0, nbVar);
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
                                System.arraycopy(oldKer, 0, newKer, 0, oldKer.length);
                                newKer[oldKer.length] = v;
                                Domain[] _domains = new Domain[nbVar];
                                System.arraycopy(domains, 0, _domains, 0, nbVar);
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
                                System.arraycopy(domains, 0, _domains, 0, nbVar);
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
        System.out.printf("loop: %d\n", loop);
    }

    private static Solver referencePropagation(Model modeler, int nbVar, Variable[] rvars, Domain[] domains, Object parameters) {
        Solver ref = modeler.model(nbVar, rvars, domains, parameters);
        ref.getEnvironment().worldPush();
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

    private static void checkNoSol(Model m, Variable[] rvars, Domain[] _domains, Object parameters, Solver ref, Object[] logObjects) {
        int nbVar = rvars.length;
        Solver test = m.model(nbVar, rvars, _domains, parameters);
        try {
            if (test.findSolution()) {
                LoggerFactory.getLogger("test").error("ds :{}, ide:{}, h:{}, var:{}, val:{}, loop:{}, seed: {}",
                        logObjects);
                LoggerFactory.getLogger("test").error("REF:\n{}\n", ref);
                ref.getEnvironment().worldPop();
                LoggerFactory.getLogger("test").error("REF:\n{}\nTEST:\n{}", ref, test);
                Assert.fail("one solution found");
            }
        } catch (Exception e) {
            LoggerFactory.getLogger("test").error(e.getMessage());
            LoggerFactory.getLogger("test").error("ds :{}, ide:{}, h:{}, var:{}, val:{}, loop:{}, seed: {}",
                    logObjects);
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
        return Arrays.copyOfRange(_values, 0, k);
    }

    ////////////////////////////////////////////////////////////////////////
    private static int[] getRemovedElements(SetVar variable, int[] d) {
        int[] _values = new int[d.length];
        int k = 0;
        for (int i : d) {
            if (!variable.envelopeContains(i)) {
                _values[k++] = i;
            }
        }
        return Arrays.copyOfRange(_values, 0, k);
    }

    ////////////////////////////////////////////////////////////////////////
    private static int[] getForcedElements(SetVar v, int[] d) {
        int[] _values = new int[v.getKernelSize()];
        int k = 0;
        for (int j = v.getKernelFirstElement(); j!=SetVar.END; j = v.getKernelNextElement()) {
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
        return Arrays.copyOfRange(_values, 0, k);
    }
    ////////////////////////////////////////////////////////////////////////
}
