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
package org.chocosolver.solver.constraints.real;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.strategy.selectors.values.RealDomainMiddle;
import org.chocosolver.solver.search.strategy.selectors.variables.Cyclic;
import org.chocosolver.solver.search.strategy.strategy.RealStrategy;
import org.chocosolver.solver.trace.Chatterbox;
import org.chocosolver.solver.variables.*;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * -Djava.library.path=/Users/cprudhom/Sources/Ibex/ibex-2.1.12/__build__/src/
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 19/07/12
 */
public class RealTest {

    public void cmpDomains(double[] a1, double[] a2) {
        double DELTA = 1e-10;
        for (int i = 0; i < a1.length; i++)
            Assert.assertEquals(a1[i], a2[i], DELTA);
    }


    @Test(groups = "ignored")
    public void test1() {

        Ibex ibex = new Ibex();

        ibex.add_contractor(2, "{0}+{1}=3", Ibex.COMPO);

        double domains[] = {1.0, 10.0, 1.0, 10.0};
        System.out.println("Before contract:");
        System.out.println("([" + domains[0] + "," + domains[1] + "] ; [" + domains[2] + "," + domains[3] + "])");

        int result = ibex.contract(0, domains);

        if (result == Ibex.FAIL) {
            System.out.println("Failed!");
        } else if (result == Ibex.CONTRACT) {
            System.out.println("After contract:");
            System.out.println("([" + domains[0] + "," + domains[1] + "] ; [" + domains[2] + "," + domains[3] + "])");
        } else {
            System.out.println("Nothing.");
        }
        ibex.release();
    }

    @Test(groups = "ignored")
    public void test2() {
        Ibex ibex = new Ibex();
        ibex.add_contractor(2, "{0}^2+{1}^2<=1", Ibex.COMPO);

        double[] domains;
        double vv = Math.sqrt(2.) / 2.;

        // CASE 1: the boolean is set to TRUE
        Assert.assertEquals(ibex.contract(0, new double[]{2., 3., 2., 3.}, Ibex.TRUE), Ibex.FAIL);
        Assert.assertEquals(ibex.contract(0, new double[]{-.5, .5, -.5, .5}, Ibex.TRUE), Ibex.ENTAILED);
        domains = new double[]{-2., 1., -2., 1.};
        Assert.assertEquals(ibex.contract(0, domains, Ibex.TRUE), Ibex.CONTRACT);
        cmpDomains(domains, new double[]{-1., 1., -1., 1.});
        Assert.assertEquals(ibex.contract(0, domains, Ibex.TRUE), Ibex.NOTHING);


        // CASE 2: the boolean is set to FALSE
        Assert.assertEquals(ibex.contract(0, new double[]{2., 3., 2., 3.}, Ibex.FALSE), Ibex.FAIL);
        Assert.assertEquals(ibex.contract(0, new double[]{-.5, .5, -.5, .5}, Ibex.FALSE), Ibex.ENTAILED);
        Assert.assertEquals(ibex.contract(0, new double[]{-2., 1., -2., -1.}, Ibex.FALSE), Ibex.NOTHING);
        domains = new double[]{0., 2., -vv, vv};
        Assert.assertEquals(ibex.contract(0, domains, Ibex.FALSE), Ibex.CONTRACT);
        cmpDomains(domains, new double[]{vv, 2., -vv, vv});

        // CASE 3: the boolean is set to UNKNOWN
        Assert.assertEquals(ibex.contract(0, new double[]{2., 3., 2., 3.}, Ibex.FALSE_OR_TRUE), Ibex.FAIL);
        Assert.assertEquals(ibex.contract(0, new double[]{-.5, .5, -.5, .5}, Ibex.FALSE_OR_TRUE), Ibex.ENTAILED);
        Assert.assertEquals(ibex.contract(0, new double[]{-2., 1., -2., -1.}, Ibex.FALSE_OR_TRUE), Ibex.NOTHING);
        domains = new double[]{0., 2., -vv, vv};
        Assert.assertEquals(ibex.contract(0, domains, Ibex.FALSE_OR_TRUE), Ibex.NOTHING);
        cmpDomains(domains, new double[]{0., 2., -vv, vv});

        ibex.release();
    }

    @Test(groups = "ignored")
    public void test3() {
        Ibex ibex = new Ibex();
        ibex.add_contractor(2, "{0}^2+{1}^2<=1", Ibex.COMPO);

        double[] domains;

        domains = new double[]{0., 1., 0., 1.};
        Assert.assertEquals(ibex.inflate(0, new double[]{0., 0.}, domains, true), Ibex.INFLATE);
        Assert.assertEquals(ibex.inflate(0, new double[]{0., 0.}, domains, true), Ibex.FULL_INFLATE);
        domains = new double[]{1., 2., 1., 2.};
        Assert.assertEquals(ibex.inflate(0, new double[]{1., 1.}, domains, true), Ibex.BAD_POINT);
        domains = new double[]{0., 1., -1., 0.};
        Assert.assertEquals(ibex.inflate(0, new double[]{1., 0.}, domains, true), Ibex.NOT_SIGNIFICANT);

        domains = new double[]{-1., 0., -1., 0.};
        Assert.assertEquals(ibex.inflate(0, new double[]{-1., -1.}, domains, false), Ibex.INFLATE);
        Assert.assertEquals(ibex.inflate(0, new double[]{-1., -1.}, domains, false), Ibex.FULL_INFLATE);
        domains = new double[]{0., .5, 0., .5};
        Assert.assertEquals(ibex.inflate(0, new double[]{0., 0.}, domains, false), Ibex.BAD_POINT);
        domains = new double[]{0., 1.01, -1., 0.};
        Assert.assertEquals(ibex.inflate(0, new double[]{1.01, 0.}, domains, false), Ibex.NOT_SIGNIFICANT);

        ibex.release();
    }

    @Test(groups = "ignored")
    public void test4() {
        Solver solver = new Solver();

        double precision = 0.00000001;
        IntVar x = VariableFactory.bounded("x", 0, 9, solver);
        IntVar y = VariableFactory.bounded("y", 0, 9, solver);
        RealVar[] vars = new RealVar[]{VariableFactory.real(x, precision), VariableFactory.real(y, precision)};
        // Actually ,we need the calculated result like these :
        // x : [2.000000, 2.000000], y : [4.000000, 4.000000]
        // or x : [1.000000, 1.000000], y : [8.000000, 8.000000]
        // but it always like this : x : [2.418267, 2.418267], y : [3.308154, 3.308154]
//        rcons.discretize(x,y);
        solver.post(new RealConstraint("RC", "{0} * {1} = 8", vars));
        solver.set(new RealStrategy(vars, new Cyclic(), new RealDomainMiddle()));
        solver.findSolution();
        Assert.assertEquals(x.getValue(), 2);
        Assert.assertEquals(y.getValue(), 4);
    }

    @Test(groups = "ignored")
    public void testFreemajb1() {
        Solver solver = new Solver();

        // Declare variables
        RealVar attr = VF.real("attr", 0.0, 20.0, 0.1, solver);

        // Create and reify constraints to assign values to the real
        RealConstraint attrEquals1 = new RealConstraint("attrEquals1", "{0}=4.0", Ibex.HC4, attr);
        BoolVar attrEquals1Reification = attrEquals1.reif();
        RealConstraint attrEquals2 = new RealConstraint("attrEquals2", "{0}=8.0", Ibex.HC4, attr);
        BoolVar attrEquals2Reification = attrEquals2.reif();

        // Walk and print the solutions
        int numSolutions = 0;
        boolean foundSolution = solver.findSolution();
        while (foundSolution) {
            numSolutions++;
            System.out.println(String.format("Solution #%d:", numSolutions));
            System.out.println("b1: " + attrEquals1Reification.getValue());
            System.out.println("b2: " + attrEquals2Reification.getValue());
            System.out.println("attr: [" + attr.getLB() + ", " + attr.getUB() + "]");
            System.out.println();

            foundSolution = solver.nextSolution();
        }
    }

    @Test(groups = "ignored")
    public void testFreemajb2() {
        Solver solver = new Solver();

        RealVar x = VariableFactory.real("x", 0.0, 5.0, 0.001, solver);
        System.out.println("Before solving:");

        RealConstraint newRange = new RealConstraint("newRange", "1.4142<{0};{0}<3.1416", Ibex.HC4, x);
        solver.post(newRange);

        try {
            solver.propagate();
        } catch (ContradictionException e) {
            e.printStackTrace();
        }
        System.out.printf("%s\n", solver.toString());
        Chatterbox.printStatistics(solver);

    }
}
