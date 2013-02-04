/*
 * Copyright (c) 1999-2012, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package solver.constraints.real;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 19/07/12
 */
public class RealTest {

    @Test(groups = "1s")
    public void test1() {

        Ibex ibex = new Ibex();

        ibex.add_ctr(2, "{0}+{1}=3");

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

    @Test(groups = "1s")
    public void test2() {
        Ibex ibex = new Ibex();
        ibex.add_ctr(2, "{0}^2+{1}^<=1");

        double[] domains;
        double vv = Math.sqrt(2.) / 2.;

        // CASE 1: the boolean is set to TRUE
        Assert.assertEquals(ibex.contract(0, new double[]{2., 3., 2., 3.}, Ibex.TRUE), Ibex.FAIL);
        Assert.assertEquals(ibex.contract(0, new double[]{-.5, .5, -.5, .5}, Ibex.TRUE), Ibex.ENTAILED);
        domains = new double[]{-2., 1., -2., 1.};
        Assert.assertEquals(ibex.contract(0, domains, Ibex.TRUE), Ibex.CONTRACT);
        Assert.assertEquals(domains, new double[]{-1., 1., -1., 1.});
        Assert.assertEquals(ibex.contract(0, domains, Ibex.TRUE), Ibex.NOT_SIGNIFICANT);


        // CASE 2: the boolean is set to FALSE
        Assert.assertEquals(ibex.contract(0, new double[]{2., 3., 2., 3.}, Ibex.FALSE), Ibex.FAIL);
        Assert.assertEquals(ibex.contract(0, new double[]{-.5, .5, -.5, .5}, Ibex.FALSE), Ibex.ENTAILED);
        Assert.assertEquals(ibex.contract(0, new double[]{-2., 1., -2., -1.}, Ibex.FALSE), Ibex.NOT_SIGNIFICANT);
        domains = new double[]{0., 2., -vv, vv};
        Assert.assertEquals(ibex.contract(0, domains, Ibex.FALSE), Ibex.NOT_SIGNIFICANT);
        Assert.assertEquals(domains, new double[]{vv, 2., -vv, vv});

        // CASE 2: the boolean is set to UNKNOWN
        Assert.assertEquals(ibex.contract(0, new double[]{2., 3., 2., 3.}, Ibex.FALSE_OR_TRUE), Ibex.FAIL);
        Assert.assertEquals(ibex.contract(0, new double[]{-.5, .5, -.5, .5}, Ibex.FALSE_OR_TRUE), Ibex.ENTAILED);
        Assert.assertEquals(ibex.contract(0, new double[]{-2., 1., -2., -1.}, Ibex.FALSE_OR_TRUE), Ibex.NOT_SIGNIFICANT);
        domains = new double[]{0., 2., -vv, vv};
        Assert.assertEquals(ibex.contract(0, domains, Ibex.FALSE), Ibex.NOT_SIGNIFICANT);
        Assert.assertEquals(domains, new double[]{0., 2., -vv, vv});


        ibex.release();
    }

    @Test(groups = "1s")
    public void test3() {
        Ibex ibex = new Ibex();
        ibex.add_ctr(2, "{0}^2+{1}^<=1");

        double[] domains;

        domains = new double[]{0., 1., 0., 1.};
        Assert.assertEquals(ibex.inflate(0, new double[]{0., 0.}, domains, true), Ibex.INFLATE);
        Assert.assertEquals(ibex.inflate(0, new double[]{0., 0.}, domains, true), Ibex.ALL_IN);
        domains = new double[]{1., 2., 1., 2.};
        Assert.assertEquals(ibex.inflate(0, new double[]{1., 1.}, domains, true), Ibex.POINT_OUT);
        domains = new double[]{0., 1., -1., 0.};
        Assert.assertEquals(ibex.inflate(0, new double[]{1., 0.}, domains, true), Ibex.NOT_SIGNIFICANT);

        domains = new double[]{-1., 0., -1., 0.};
        Assert.assertEquals(ibex.inflate(0, new double[]{-1., -1.}, domains, false), Ibex.INFLATE);
        Assert.assertEquals(ibex.inflate(0, new double[]{-1., -1.}, domains, false), Ibex.ALL_OUT);
        domains = new double[]{0., .5, 0., .5};
        Assert.assertEquals(ibex.inflate(0, new double[]{0., 0.}, domains, false), Ibex.POINT_IN);
        domains = new double[]{0., 1.01, -1., 0.};
        Assert.assertEquals(ibex.inflate(0, new double[]{1.01, 0.}, domains, false), Ibex.NOT_SIGNIFICANT);


        ibex.release();
    }
}
