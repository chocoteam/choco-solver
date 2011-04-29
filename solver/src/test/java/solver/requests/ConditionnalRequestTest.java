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

package solver.requests;

import choco.kernel.common.util.tools.MathUtils;
import choco.kernel.memory.IEnvironment;
import org.testng.Assert;
import org.testng.annotations.Test;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.ConstraintFactory;
import solver.constraints.nary.AllDifferent;
import solver.constraints.propagators.Propagator;
import solver.search.strategy.StrategyFactory;
import solver.variables.IntVar;
import solver.variables.VariableFactory;
import solver.requests.conditions.AbstractCondition;
import solver.requests.conditions.CompletlyInstantiated;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 22/03/11
 */
public class ConditionnalRequestTest {

    private static void castRequests(Constraint[] constraints, IEnvironment environment, int threshold) {
        try {
            Method m_unlink = Propagator.class.getDeclaredMethod("unlinkVariables");
            Field f_requests = Propagator.class.getDeclaredField("requests");
            Field f_vars = Propagator.class.getDeclaredField("vars");

            m_unlink.setAccessible(true);
            f_requests.setAccessible(true);
            f_vars.setAccessible(true);

            for (Constraint cstr : constraints) {
                Propagator[] propagators = cstr.propagators;
                for (Propagator prop : propagators) {
                    m_unlink.invoke(prop);
                    IntVar[] ivars = (IntVar[]) f_vars.get(prop);
                    AbstractCondition cond = new CompletlyInstantiated(environment, threshold);
                    ConditionnalRequest[] requests = new ConditionnalRequest[ivars.length];
                    for (int i = 0; i < ivars.length; i++) {
                        ivars[i].addObserver(prop);
                        requests[i] = new ConditionnalRequest(prop, ivars[i], i, cond, environment);
                        ivars[i].addRequest(requests[i]);
                        cond.linkRequest(requests[i]);
                    }
                    f_requests.set(prop, requests);
                }
            }

            m_unlink.setAccessible(false);
            f_requests.setAccessible(false);
            f_vars.setAccessible(false);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    @Test(groups = "1s")
    public void test1() {
        Solver solver = new Solver();

        IntVar x = VariableFactory.enumerated("x", 1, 5, solver);
        IntVar y = VariableFactory.enumerated("y", 1, 5, solver);
        IntVar z = VariableFactory.enumerated("z", 1, 5, solver);


        List<Constraint> lcstrs = new ArrayList<Constraint>(2);
        lcstrs.add(ConstraintFactory.lt(x, y, solver));
        lcstrs.add(ConstraintFactory.lt(y, z, solver));

        Constraint[] cstrs = lcstrs.toArray(new Constraint[lcstrs.size()]);
        IntVar[] vars = new IntVar[]{x, y, z};

        castRequests(cstrs, solver.getEnvironment(), 2);

        solver.post(cstrs);
        solver.set(StrategyFactory.inputOrderInDomainMin(vars, solver.getEnvironment()));

        solver.findAllSolutions();
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 10);
    }

    @Test(groups = "1m")
    public void allDifferentTest() {
        Random random = new Random();
        for (int seed = 0; seed < 50; seed++) {
            random.setSeed(seed);
            Solver solver = new Solver();
            int n = 2 + random.nextInt(7);

            IntVar[] x = VariableFactory.enumeratedArray("x", n, 1, n, solver);

            Constraint[] cstrs = {new AllDifferent(x, solver, AllDifferent.Type.AC)};

            castRequests(cstrs, solver.getEnvironment(), n / 2);

            solver.post(cstrs);
            solver.set(StrategyFactory.random(x, solver.getEnvironment()));

            solver.findAllSolutions();
            Assert.assertEquals(solver.getMeasures().getSolutionCount(), MathUtils.factoriel(n));
        }
    }


}
