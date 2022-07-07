package org.chocosolver.lp;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 04/07/2022
 */
public class LinearProgramTest {

    @Test(groups = "1s")
    public void testInfeasible() {
        double[] c = {3, -2};
        double[][] A = {{1, 1}, {-2, -2}};
        double[] b = {2, -10};
        LinearProgram lp = new LinearProgram(A, b, c);
        Assert.assertEquals(lp.simplex(), LinearProgram.Status.INFEASIBLE);
        Assert.assertEquals(lp.value(0), -1.0, 1e-8);
        Assert.assertEquals(lp.value(1), -1.0, 1e-8);
        Assert.assertEquals(lp.objective(), Double.NEGATIVE_INFINITY, 1e-8);
    }

    @Test(groups = "1s")
    public void testUnbounded0() {
        double[] c = {1, -1};
        double[][] A = {{-2, 1}, {-1, -2}};
        double[] b = {-1, -2};
        LinearProgram lp = new LinearProgram(A, b, c);
        Assert.assertEquals(lp.simplex(), LinearProgram.Status.UNBOUNDED);
        Assert.assertEquals(lp.value(0), -1.0, 1e-8);
        Assert.assertEquals(lp.value(1), -1.0, 1e-8);
        Assert.assertEquals(lp.objective(), Double.NEGATIVE_INFINITY, 1e-8);
    }

    @Test(groups = "1s")
    public void testFeasible01() {
        // 29.3-7
        double[] c = {-1, -1, -1};
        double[][] A = {
                {-2, -7.5, -3},
                {-20, -5, -10}
        };
        double[] b = {-10000, -30000};
        LinearProgram lp = new LinearProgram(A, b, c);
        Assert.assertEquals(lp.simplex(), LinearProgram.Status.FEASIBLE);
        Assert.assertEquals(lp.objective(), -2250, 1e-8);
        Assert.assertEquals(lp.value(0), 1250, 1e-8);
        Assert.assertEquals(lp.value(1), 1000, 1e-8);
        Assert.assertEquals(lp.value(2), 0, 1e-8);
    }

    @Test(groups = "1s")
    public void testInitSol() {
        double[] c = {2, -1};
        double[][] A = {
                {2, -1},
                {1, -5}
        };
        double[] b = {2, -4};
        LinearProgram lp = new LinearProgram(A, b, c, true);
        Assert.assertEquals(lp.simplex(), LinearProgram.Status.FEASIBLE);
        Assert.assertEquals(lp.value(0), 1.5555555555555554, 1e-8);
        Assert.assertEquals(lp.value(1), 1.1111111111111112, 1e-8);
        Assert.assertEquals(lp.objective(), 2, 1e-8);
    }

    @Test(groups = "1s")
    public void testCycle() {
        double[] c = {1, 1, 1};
        double[][] A = {{1, 1, 0}, {0, -1, 1}};
        double[] b = {8, 0};
        LinearProgram lp = new LinearProgram(A, b, c);
        Assert.assertEquals(lp.simplex(), LinearProgram.Status.FEASIBLE);
        Assert.assertEquals(lp.value(0), 0., 1e-8);
        Assert.assertEquals(lp.value(1), 8., 1e-8);
        Assert.assertEquals(lp.value(2), 8., 1e-8);
        Assert.assertEquals(lp.objective(), 16., 1e-8);
    }

    @Test(groups = "1s")
    public void tesFeasible0() {
        double[] c = new double[]{2, -3, 3};
        double[][] A = new double[][]{
                {1, 1, -1},
                {-1, -1, 1},
                {1, -2, 2}
        };
        double[] b = new double[]{7, -7, 4};
        LinearProgram lp = new LinearProgram(A, b, c);
        Assert.assertEquals(lp.simplex(), LinearProgram.Status.FEASIBLE);
        Assert.assertEquals(lp.objective(), 9., 1e-8);
        Assert.assertEquals(lp.value(0), 6., 1e-8);
        Assert.assertEquals(lp.value(1), 1., 1e-8);
        Assert.assertEquals(lp.value(2), 0., 1e-8);
    }

    @Test(groups = "1s")
    public void testFeasible1() {
        double[] c = new double[]{3, 1, 2};
        double[][] A = new double[][]{
                {1, 1, 3},
                {2, 2, 5},
                {4, 1, 2}
        };
        double[] b = new double[]{30, 24, 36};
        LinearProgram lp = new LinearProgram(A, b, c);
        Assert.assertEquals(lp.simplex(), LinearProgram.Status.FEASIBLE);
        Assert.assertEquals(lp.value(0), 8., 1e-8);
        Assert.assertEquals(lp.value(1), 4., 1e-8);
        Assert.assertEquals(lp.value(2), 0., 1e-8);
        Assert.assertEquals(lp.objective(), 28., 1e-8);
    }

    @Test(groups = "1s")
    public void testFeasible1bis() {
        double[] c = {3, 1, 2};
        double[][] A = {{4, 1, 2}, {1, 1, 3}};
        double[] b = {36, 30};
        LinearProgram lp = new LinearProgram(A, b, c);
        Assert.assertEquals(lp.simplex(), LinearProgram.Status.FEASIBLE);
        Assert.assertEquals(lp.value(0), 2., 1e-8);
        Assert.assertEquals(lp.value(1), 28., 1e-8);
        Assert.assertEquals(lp.value(2), 0., 1e-8);
        Assert.assertEquals(lp.objective(), 34., 1e-8);
    }

    @Test(groups = "1s")
    public void testFeasible2() {
        double[] c = {5, -3};
        double[][] A = {{1, -1}, {2, 1}};
        double[] b = {1, 2};
        LinearProgram lp = new LinearProgram(A, b, c);
        Assert.assertEquals(lp.simplex(), LinearProgram.Status.FEASIBLE);
        Assert.assertEquals(lp.value(0), 1., 1e-8);
        Assert.assertEquals(lp.value(1), 0., 1e-8);
        Assert.assertEquals(lp.objective(), 5., 1e-8);
    }

    @Test(groups = "1s")
    public void testFeasible3() {
        // 29.3-5
        double[] c = {18, 12.5};
        double[][] A = {{1, 1}, {1, 0}, {0, 1}};
        double[] b = {20, 12, 16};
        LinearProgram lp = new LinearProgram(A, b, c);
        Assert.assertEquals(lp.simplex(), LinearProgram.Status.FEASIBLE);
        Assert.assertEquals(lp.value(0), 12., 1e-8);
        Assert.assertEquals(lp.value(1), 8., 1e-8);
        Assert.assertEquals(lp.objective(), 316., 1e-8);
    }

    @Test(groups = "1s")
    public void testFeasible4() {
        // 29.3-6
        double[] c = {5, -3};
        double[][] A = {
                {1, -1},
                {2, 1}
        };
        double[] b = {1, 2};
        LinearProgram lp = new LinearProgram(A, b, c);
        Assert.assertEquals(lp.simplex(), LinearProgram.Status.FEASIBLE);
        Assert.assertEquals(lp.value(0), 1., 1e-8);
        Assert.assertEquals(lp.value(1), 0., 1e-8);
        Assert.assertEquals(lp.objective(), 5, 1e-8);
    }

    @Test(groups = "1s")
    public void testFeasible6() {
        // 29.5-5
        double[] c = {1, 3};
        double[][] A = {
                {1, -1},
                {-1, -1},
                {-1, 4}
        };
        double[] b = {8, -3, 2};
        LinearProgram lp = new LinearProgram(A, b, c);
        Assert.assertEquals(lp.simplex(), LinearProgram.Status.FEASIBLE);
        Assert.assertEquals(lp.value(0), 11.333333333333334, 1e-8);
        Assert.assertEquals(lp.value(1), 3.3333333333333335, 1e-8);
        Assert.assertEquals(lp.objective(), 21.333333333333336, 1e-8);
    }

    @Test(groups = "1s")
    public void testInfeasible7() {
        // 29.5-6
        double[] c = {1, -2};
        double[][] A = {
                {1, 2},
                {-2, -6},
                {0, 1}
        };
        double[] b = {4, -12, 1};
        LinearProgram lp = new LinearProgram(A, b, c);
        Assert.assertEquals(lp.simplex(), LinearProgram.Status.INFEASIBLE);
    }

    @Test(groups = "1s")
    public void testUnbounded8() {
        // 29.5-7
        double[] c = {1, 3};
        double[][] A = {
                {-1, 1},
                {-1, -1},
                {-1, 4}
        };
        double[] b = {-1, -3, 2};
        LinearProgram lp = new LinearProgram(A, b, c);
        Assert.assertEquals(lp.simplex(), LinearProgram.Status.UNBOUNDED);
    }

    @Test(groups = "1s")
    public void testFeasible9() {
        // (29.6)-(29.10)
        double[] c = {-1, -1, -1, -1};
        double[][] A = {
                {2, -8, 0, -10},
                {-5, -2, 0, 0},
                {-3, 5, -10, 2}
        };
        double[] b = {-50, -100, -25};
        LinearProgram lp = new LinearProgram(A, b, c);
        Assert.assertEquals(lp.simplex(), LinearProgram.Status.FEASIBLE);
        Assert.assertEquals(lp.objective(), -2.7927927927927925e+01, 1e-8);
        Assert.assertEquals(lp.value(0), 1.8468468468468465e+01, 1e-8);
        Assert.assertEquals(lp.value(1), 3.8288288288288292e+00, 1e-8);
        Assert.assertEquals(lp.value(2), 0., 1e-8);
        Assert.assertEquals(lp.value(3), 5.6306306306306295e+00, 1e-8);
    }

    @Test(groups = "1s")
    public void testFeasible10() {
        // FIL
        double[] c = {50, 70};
        double[][] A = {
                {40, 30},
                {20, 30}
        };
        double[] b = {360, 480};
        LinearProgram lp = new LinearProgram(A, b, c);
        Assert.assertEquals(lp.toString(),
                "Maximize\n" +
                        "\\ v = 0.0\n" +
                        " obj: +50.0 x1 +70.0 x2\n" +
                        "Subject to\n" +
                        " c1: 40.0 x1 +30.0 x2 <= 360.0\n" +
                        " c2: 20.0 x1 +30.0 x2 <= 480.0\n" +
                        "End");
        Assert.assertEquals(lp.simplex(), LinearProgram.Status.FEASIBLE);
        Assert.assertEquals(lp.value(0), 0., 1e-8);
        Assert.assertEquals(lp.value(1), 12.0, 1e-8);
        Assert.assertEquals(lp.objective(), 840, 1e-8);
    }

}