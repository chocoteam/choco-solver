/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2025, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.lp;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.NoInjection;
import org.testng.annotations.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 04/07/2022
 */
public class LinearProgramTest {

    @Test(groups = "1s")
    public void testSlack1() {
        double[] c = {3, 2, 3};
        double[][] A = {{2, 1, 1}, {1, 2, 3}, {2, 2, 1}};
        double[] b = {2, 5, 6};
        LinearProgram.Slack slack = new LinearProgram.Slack(A, b, c);
        int e = LinearProgram.enteringVariable(slack);
        Assert.assertEquals(e, 0);
        int l = LinearProgram.leavingVariable(slack, e);
        Assert.assertEquals(l, 0);
        slack.pivot(l, e, false);
        Assert.assertEquals(slack.b, new double[]{1., 4., 4.});
        Assert.assertEquals(slack.c, new double[]{-3 / 2., 1. / 2., 3. / 2.});
        Assert.assertEquals(slack.A, new double[][]{{1. / 2., 1. / 2., 1. / 2.}, {-1. / 2., 3. / 2., 5. / 2.}, {-1., 1., 0.}});
        Assert.assertEquals(slack.v, 3.);
        Assert.assertEquals(slack.B, new int[]{0, 4, 5});
        Assert.assertEquals(slack.N, new int[]{3, 1, 2});
        e = LinearProgram.enteringVariable(slack);
        Assert.assertEquals(e, 1);
        l = LinearProgram.leavingVariable(slack, e);
        Assert.assertEquals(l, 0);
        slack.pivot(l, e, false);
        Assert.assertEquals(slack.b, new double[]{2., 1., 2.}, 1e-8);
        Assert.assertEquals(slack.c, new double[]{-2., -1., 1.0}, 1e-8);
        Assert.assertEquals(slack.A, new double[][]{{1., 2., 1.}, {-2., -3., 1.}, {-2., -2., -1.}});
        Assert.assertEquals(slack.v, 4.);
        Assert.assertEquals(slack.B, new int[]{1, 4, 5});
        Assert.assertEquals(slack.N, new int[]{3, 0, 2});
        e = LinearProgram.enteringVariable(slack);
        Assert.assertEquals(e, 2);
        l = LinearProgram.leavingVariable(slack, e);
        Assert.assertEquals(l, 1);
        slack.pivot(l, e, false);
        Assert.assertEquals(slack.b, new double[]{1., 1., 3.}, 1e-8);
        Assert.assertEquals(slack.c, new double[]{0., 2., -1.}, 1e-8);
        Assert.assertEquals(slack.A, new double[][]{{3., 5., -1.}, {-2., -3., 1.}, {-4., -5., 1.}});
        Assert.assertEquals(slack.v, 5.);
        Assert.assertEquals(slack.B, new int[]{1, 2, 5});
        Assert.assertEquals(slack.N, new int[]{3, 0, 4});
        e = LinearProgram.enteringVariable(slack);
        Assert.assertEquals(e, 1);
        l = LinearProgram.leavingVariable(slack, e);
        Assert.assertEquals(l, 0);
        slack.pivot(l, e, false);
        Assert.assertEquals(slack.b, new double[]{1. / 5., 8. / 5., 4.0}, 1e-8);
        Assert.assertEquals(slack.c, new double[]{-6. / 5., -2. / 5., -3. / 5.}, 1e-8);
        Assert.assertEquals(slack.A[0], new double[]{3. / 5., 1. / 5., -1. / 5.}, 1e-8);
        Assert.assertEquals(slack.A[1], new double[]{-1. / 5., 3. / 5., 2. / 5.}, 1e-8);
        Assert.assertEquals(slack.A[2], new double[]{-1., 1., 0.}, 1e-8);
        Assert.assertEquals(slack.v, 5.4);
        Assert.assertEquals(slack.B, new int[]{0, 2, 5});
        Assert.assertEquals(slack.N, new int[]{3, 1, 4});
        e = LinearProgram.enteringVariable(slack);
        Assert.assertEquals(e, Integer.MAX_VALUE);
    }


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
        LinearProgram lp = new LinearProgram(A, b, c, false);
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
                "Maximize" +
                        //"\\ v = 0.0\n" +
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


    @Test(groups = "1s")
    public void testWikipedia() {
        // found on wikipedia
        double[] c = {2, 3, 4};
        double[][] A = {
                {3, 2, 1},
                {2, 5, 3}
        };
        double[] b = {10, 15};
        LinearProgram lp = new LinearProgram(A, b, c, false);
        Assert.assertEquals(lp.simplex(), LinearProgram.Status.FEASIBLE);
        Assert.assertEquals(lp.value(0), 0., 1e-8);
        Assert.assertEquals(lp.value(1), 0., 1e-8);
        Assert.assertEquals(lp.value(2), 5., 1e-8);
        Assert.assertEquals(lp.objective(), 20, 1e-8);
    }

    @Test(groups = "1s")
    public void testGatech() {
        // from: https://www2.isye.gatech.edu/~spyros/LP/node23.html#SECTION00050010000000000000
        double[] c = {200, 400};
        double[][] A = {
                {1. / 40, 1. / 60},
                {1. / 50, 1. / 50}
        };
        double[] b = {1, 1};
        LinearProgram lp = new LinearProgram(A, b, c, false);
        Assert.assertEquals(lp.simplex(), LinearProgram.Status.FEASIBLE);
        Assert.assertEquals(lp.value(0), 0., 1e-8);
        Assert.assertEquals(lp.value(1), 50., 1e-8);
        Assert.assertEquals(lp.objective(), 20000, 1e-8);
    }

    @Test(groups = "1s")
    public void testModeler1() {
        LinearProgram lp = new LinearProgram(false);
        lp.makeVariables(2);
        lp.addLeq(new double[]{4, 3}, 36);
        lp.addLeq(new double[]{2, 3}, 48);
        lp.setObjective(true, new double[]{5, 7});
        Assert.assertEquals(lp.simplex(), LinearProgram.Status.FEASIBLE);
        Assert.assertEquals(lp.value(0), 0., 1e-8);
        Assert.assertEquals(lp.value(1), 12.0, 1e-8);
        Assert.assertEquals(lp.objective(), 84, 1e-8);
    }

    @SuppressWarnings("Convert2Diamond")
    @Test(groups = "1s")
    public void testModeler2() {
        LinearProgram lp = new LinearProgram(false);
        int x1 = lp.makeVariable();
        int x2 = lp.makeVariable();
        lp.addLeq(new HashMap<Integer, Double>() {{
            put(x1, 4.);
            put(x2, 3.);
        }}, 36);
        lp.addLeq(new HashMap<Integer, Double>() {{
            put(x1, 2.);
            put(x2, 3.);
        }}, 48);
        lp.setObjective(true, new double[]{5, 7});
        Assert.assertEquals(lp.simplex(), LinearProgram.Status.FEASIBLE);
        Assert.assertEquals(lp.value(0), 0., 1e-8);
        Assert.assertEquals(lp.value(1), 12.0, 1e-8);
        Assert.assertEquals(lp.objective(), 84, 1e-8);
    }

    @SuppressWarnings("Convert2Diamond")
    @Test(groups = "1s")
    public void testModeler3() {
        LinearProgram lp = new LinearProgram();
        lp.makeVariables(3);
        lp.addEq(new HashMap<Integer, Double>() {{
            put(0, 1.);
            put(1, 1.);
            put(2, -1.);
        }}, 7);
        lp.addLeq(new double[]{1, -2, 2}, 4);
        lp.setObjective(false, new double[]{-2, 3, -3});
        Assert.assertEquals(lp.simplex(), LinearProgram.Status.FEASIBLE);
        Assert.assertEquals(lp.objective(), 9., 1e-8);
        Assert.assertEquals(lp.value(0), 6., 1e-8);
        Assert.assertEquals(lp.value(1), 1., 1e-8);
        Assert.assertEquals(lp.value(2), 0., 1e-8);
    }

    @SuppressWarnings("Convert2Diamond")
    @Test(groups = "1s")
    public void testModeler4() {
        // 29.3-7
        LinearProgram lp = new LinearProgram();
        lp.makeVariables(3);
        lp.setObjective(false, new double[]{1, 1, 1});
        lp.addGeq(new double[]{2, 7.5, 3}, 10000);
        lp.addGeq(new HashMap<Integer, Double>() {{
            put(0, 20.);
            put(1, 5.);
            put(2, 3.);
        }}, 30000);
        Assert.assertEquals(lp.simplex(), LinearProgram.Status.FEASIBLE);
        Assert.assertEquals(lp.objective(), -2250, 1e-8);
        Assert.assertEquals(lp.value(0), 1250, 1e-8);
        Assert.assertEquals(lp.value(1), 1000, 1e-8);
        Assert.assertEquals(lp.value(2), 0, 1e-8);
    }

    @Test(groups = "1s", expectedExceptions = UnsupportedOperationException.class)
    public void testError0() {
        LinearProgram lp = new LinearProgram();
        lp.makeVariable();
        lp.addEq(new double[]{1}, 1);
        lp.makeVariable();
    }

    @Test(groups = "1s", expectedExceptions = UnsupportedOperationException.class)
    public void testError1() {
        LinearProgram lp = new LinearProgram();
        lp.makeVariable();
        lp.addEq(new double[]{1}, 1);
        lp.makeVariables(2);
    }

    @Test(groups = "1s", expectedExceptions = UnsupportedOperationException.class)
    public void testError2() {
        LinearProgram lp = new LinearProgram();
        lp.makeVariables(2);
        lp.addLeq(new double[]{1}, 1);
    }

    @Test(groups = "1s", expectedExceptions = UnsupportedOperationException.class)
    public void testError3() {
        LinearProgram lp = new LinearProgram();
        lp.makeVariables(2);
        lp.addGeq(new double[]{1}, 1);
    }

    @Test(groups = "1s", expectedExceptions = UnsupportedOperationException.class)
    public void testError4() {
        LinearProgram lp = new LinearProgram();
        lp.makeVariables(2);
        lp.addEq(new double[]{1}, 1);
    }

    @Test(groups = "1s", expectedExceptions = UnsupportedOperationException.class)
    public void testError5() {
        LinearProgram lp = new LinearProgram();
        lp.makeVariables(2);
        lp.setObjective(true, new double[]{1});
    }

    public static void testSimplex(double[][] A, double[] b, double[] c, double optimal) {
        LinearProgram lp = new LinearProgram(A, b, c);
        LinearProgram.Status status = lp.simplex();
//        System.out.println("value = " + lp.objective());
        if (!Double.isNaN(optimal)) {
            Assert.assertEquals(status, LinearProgram.Status.FEASIBLE);
            Assert.assertEquals(lp.objective(), optimal, 1e-8);
//            double[] x = lp.values();
//            if (x != null) {
//                for (int i = 0; i < x.length; i++)
//                    System.out.println("x[" + i + "] = " + x[i]);
//            }
        } else {
            Assert.assertNotEquals(status, TwoPhaseSimplex.Status.FEASIBLE);
        }
    }

    public static void testTwoPhaseSimplex(double[][] A, double[] b, double[] c, double optimal) {
        TwoPhaseSimplex lp = new TwoPhaseSimplex(A, b, c);
        TwoPhaseSimplex.Status status = lp.solve();
//        System.out.println("value = " + lp.value());
        if (!Double.isNaN(optimal)) {
            Assert.assertEquals(status, TwoPhaseSimplex.Status.FEASIBLE);
            Assert.assertEquals(lp.value(), optimal, 1e-8);
//            double[] x = lp.primal();
//            for (int i = 0; i < x.length; i++)
//                System.out.println("x[" + i + "] = " + x[i]);
        } else {
            Assert.assertNotEquals(status, TwoPhaseSimplex.Status.FEASIBLE);
        }
    }

    @DataProvider
    public Object[][] algo() throws NoSuchMethodException {
        return new Object[][]{
                {LinearProgramTest.class.getDeclaredMethod("testTwoPhaseSimplex", double[][].class, double[].class, double[].class, double.class)},
                {LinearProgramTest.class.getDeclaredMethod("testSimplex", double[][].class, double[].class, double[].class, double.class)}
        };
    }

    @Test(groups = "1s", dataProvider = "algo")
    public void testConvergence(@NoInjection Method method) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        double[] c = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0};
        double[][] A = {{1.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                {0.0, 0.0, 1.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                {1.0, 1.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                {0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                {0.0, 1.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                {1.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                {-0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -1.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0},
                {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                {-0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -1.0, -0.0, -0.0, -0.0, -0.0, -0.0},
                {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0},
                {-0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -1.0, -0.0, -0.0, -0.0, -0.0},
                {0.0, 1.0, 0.0, 0.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                {0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                {0.0, 0.0, 0.0, 0.0, 1.0, 1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                {1.0, 0.0, 0.0, 1.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                {1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                {0.0, 0.0, 0.0, 1.0, 0.0, 1.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0},
                {-0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -1.0, -0.0, -0.0, -0.0},
                {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0},
                {-0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -1.0, -0.0, -0.0},
                {0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                {0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                {0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0},
                {-0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -1.0, -0.0},
                {1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -1.0},
                {-1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, 1.0},
                {-0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -1.0},
                {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0},
                {-0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -1.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0},
                {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                {-0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -1.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0},
                {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                {-0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -1.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0},
                {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                {-0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -1.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0},
                {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                {-0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -1.0, -0.0},
                {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0},
                {-0.0, -0.0, -0.0, -1.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0},
                {0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                {-0.0, -0.0, -0.0, -0.0, -0.0, -1.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0},
                {0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                {-0.0, -0.0, -0.0, -0.0, -1.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0},
                {0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                {-0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -1.0, -0.0, -0.0},
                {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0},
                {-0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -1.0, -0.0, -0.0, -0.0},
                {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0},
                {-1.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0},
                {1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                {-0.0, -1.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0},
                {0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                {-0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -1.0, -0.0, -0.0, -0.0, -0.0},
                {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0},
                {-0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -1.0, -0.0, -0.0, -0.0, -0.0, -0.0},
                {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                {-0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -1.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0},
                {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                {-0.0, -0.0, -1.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0},
                {0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},};
        double[] b = {2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 0.0, -0.0, 0.0, -0.0, 0.0, -0.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 0.0, -0.0, 0.0, -0.0, 2.0, 2.0, 2.0, 0.0, -0.0, 0.0, -0.0, -6.0, 10.0, -0.0, 1.0, -0.0, 1.0, -0.0, 1.0, -0.0, 1.0, -0.0, 0.0, -0.0, 1.0, -0.0, 1.0, -0.0, 1.0, -0.0, 0.0, -0.0, 0.0, -0.0, 1.0, -0.0, 1.0, -0.0, 0.0, -0.0, 0.0, -0.0, 0.0, -1.0, 1.0};
        double v = 6.;
        method.invoke(null, A, b, c, v);
    }

    @Test(groups = "1s", dataProvider = "algo")
    // x0 = 12, x1 = 28, opt = 800
    public void test1(@NoInjection Method method) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        double[] c = {13.0, 23.0};
        double[] b = {480.0, 160.0, 1190.0};
        double[][] A = {
                {5.0, 15.0},
                {4.0, 4.0},
                {35.0, 20.0},
        };
        double v = 800.;
        method.invoke(null, A, b, c, v);
    }

    @Test(groups = "1s", dataProvider = "algo")
    // dual of test1():  x0 = 12, x1 = 28, opt = 800
    public void test2(@NoInjection Method method) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        double[] b = {-13.0, -23.0};
        double[] c = {-480.0, -160.0, -1190.0};
        double[][] A = {
                {-5.0, -4.0, -35.0},
                {-15.0, -4.0, -20.0}
        };
        double v = -800.;
        method.invoke(null, A, b, c, v);
    }

    @Test(groups = "1s", dataProvider = "algo")
    public void test3(@NoInjection Method method) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        double[][] A = {
                {-1, 1, 0},
                {1, 4, 0},
                {2, 1, 0},
                {3, -4, 0},
                {0, 0, 1},
        };
        double[] c = {1, 1, 1};
        double[] b = {5, 45, 27, 24, 4};
        double v = 22;
        method.invoke(null, A, b, c, v);
    }

    @Test(groups = "1s", dataProvider = "algo")
    // unbounded
    public void test4(@NoInjection Method method) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        double[] c = {2.0, 3.0, -1.0, -12.0};
        double[] b = {3.0, 2.0};
        double[][] A = {
                {-2.0, -9.0, 1.0, 9.0},
                {1.0, 1.0, -1.0, -2.0},
        };
        double v = Double.NaN;
        method.invoke(null, A, b, c, v);
    }

    @Test(groups = "1s", dataProvider = "algo")
    // degenerate - cycles if you choose most positive objective function coefficient
    public void test5(@NoInjection Method method) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        double[] c = {10.0, -57.0, -9.0, -24.0};
        double[] b = {0.0, 0.0, 1.0};
        double[][] A = {
                {0.5, -5.5, -2.5, 9.0},
                {0.5, -1.5, -0.5, 1.0},
                {1.0, 0.0, 0.0, 0.0},
        };
        double v = 1.;
        method.invoke(null, A, b, c, v);
    }

    @Test(groups = "1s", dataProvider = "algo")
    // floating-point EPSILON needed in min-ratio test
    public void test6(@NoInjection Method method) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        double[] c = {-1, -1, -1, -1, -1, -1, -1, -1, -1};
        double[] b = {-0.9, 0.2, -0.2, -1.1, -0.7, -0.5, -0.1, -0.1, -1};
        double[][] A = {
                {-2, 1, 0, 0, 0, 0, 0, 0, 0},
                {1, -2, -1, 0, 0, 0, 0, 0, 0},
                {0, -1, -2, -1, 0, 0, 0, 0, 0},
                {0, 0, -1, -2, -1, -1, 0, 0, 0},
                {0, 0, 0, -1, -2, -1, 0, 0, 0},
                {0, 0, 0, -1, -1, -2, -1, 0, 0},
                {0, 0, 0, 0, 0, -1, -2, -1, 0},
                {0, 0, 0, 0, 0, 0, -1, -2, -1},
                {0, 0, 0, 0, 0, 0, 0, -1, -2}
        };
        double v = -1.75;
        method.invoke(null, A, b, c, v);
    }

    @Test(groups = "1s", dataProvider = "algo")
    // testing divergence of simplex
    public void test7(@NoInjection Method method) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        double[] c = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0};
        double[] b = {2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 0.0, -0.0, 0.0, -0.0, 0.0, -0.0, 0.0, -0.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 0.0, -0.0, 0.0, -0.0, 0.0, -0.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 0.0, -0.0, 0.0, -0.0, 2.0, 2.0, 2.0, 2.0, 0.0, -0.0, 0.0, -0.0, -6.0, 9.0, -0.0, 1.0, -0.0, 1.0, -0.0, 1.0, -0.0, 1.0, -0.0, 1.0, -0.0, 0.0, -0.0, 0.0, -0.0, 0.0, -0.0, 0.0, -1.0, 1.0, -0.0, 0.0, -0.0, 0.0, -0.0, 0.0, -0.0, 0.0, -1.0, 1.0, -0.0, 0.0, -0.0, 0.0, -0.0, 0.0, -0.0, 0.0, -1.0, 1.0, -0.0, 0.0, -0.0, 0.0, -0.0, 0.0, -0.0, 0.0, -1.0, 1.0};
        double[][] A = {{1.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                {0.0, 0.0, 1.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                {1.0, 1.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                {0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                {0.0, 1.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                {1.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                {0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                {0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                {0.0, 0.0, 0.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                {1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                {-0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -1.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0},
                {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                {-0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -1.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0},
                {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                {-0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -1.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0},
                {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                {-0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -1.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0},
                {0.0, 1.0, 0.0, 0.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                {0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                {0.0, 0.0, 0.0, 0.0, 1.0, 1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                {0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                {0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                {0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                {1.0, 0.0, 0.0, 1.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                {1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                {0.0, 0.0, 0.0, 1.0, 0.0, 1.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                {1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                {0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                {0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                {-0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -1.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0},
                {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                {-0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -1.0, -0.0, -0.0, -0.0, -0.0, -0.0},
                {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0},
                {-0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -1.0, -0.0, -0.0, -0.0, -0.0},
                {0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                {0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 1.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                {0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                {0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 1.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                {0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                {0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0},
                {-0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -1.0, -0.0, -0.0, -0.0},
                {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0},
                {-0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -1.0, -0.0, -0.0},
                {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0},
                {-0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -1.0, -0.0},
                {1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -1.0},
                {-1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, 1.0},
                {-0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -1.0},
                {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0},
                {-0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -1.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0},
                {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                {-0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -1.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0},
                {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                {-0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -1.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0},
                {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                {-0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -1.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0},
                {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                {-0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -1.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0},
                {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                {-0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -1.0, -0.0},
                {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0},
                {-0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -1.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0},
                {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                {-0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -1.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0},
                {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                {-0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -1.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0},
                {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                {-0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -1.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0},
                {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                {-0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -1.0, -0.0, -0.0},
                {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0},
                {-0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -1.0, -0.0, -0.0, -0.0},
                {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0},
                {-0.0, -0.0, -0.0, -1.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0},
                {0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                {-0.0, -0.0, -0.0, -0.0, -0.0, -1.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0},
                {0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                {-0.0, -0.0, -0.0, -0.0, -1.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0},
                {0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                {-0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -1.0, -0.0, -0.0, -0.0, -0.0},
                {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0},
                {-0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -1.0, -0.0, -0.0, -0.0, -0.0, -0.0},
                {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                {-0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -1.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0},
                {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                {-1.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0},
                {1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                {-0.0, -1.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0},
                {0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                {-0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -1.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0},
                {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                {-0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -1.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0},
                {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                {-0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -1.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0},
                {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                {-0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -1.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0},
                {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                {-0.0, -0.0, -1.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0, -0.0},
                {0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
        };
        double v = 8.;
        method.invoke(null, A, b, c, v);
    }


    @Test(groups = "1s", dataProvider = "algo")
    public void test8(@NoInjection Method method) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, IOException {
        String strLp = "Maximize obj: +0.0 x1 +0.0 x2 +0.0 x3 +0.0 x4 +0.0 x5 +0.0 x6 +0.0 x7 +0.0 x8 +0.0 x9 +0.0 x10 +0.0 x11 +0.0 x12 +0.0 x13 +0.0 x14 +0.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 +1.0 x26\n" +
                "Subject to\n" +
                " c1: 1.0 x1 +1.0 x2 +1.0 x3 +0.0 x4 +0.0 x5 +0.0 x6 +0.0 x7 +0.0 x8 +0.0 x9 +0.0 x10 +0.0 x11 +0.0 x12 +0.0 x13 +0.0 x14 +0.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 +0.0 x26 <= 2.0\n" +
                " c2: 0.0 x1 +0.0 x2 +1.0 x3 +1.0 x4 +1.0 x5 +0.0 x6 +0.0 x7 +0.0 x8 +0.0 x9 +0.0 x10 +0.0 x11 +0.0 x12 +0.0 x13 +0.0 x14 +0.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 +0.0 x26 <= 2.0\n" +
                " c3: 1.0 x1 +1.0 x2 +0.0 x3 +0.0 x4 +0.0 x5 +1.0 x6 +0.0 x7 +0.0 x8 +0.0 x9 +0.0 x10 +0.0 x11 +0.0 x12 +0.0 x13 +0.0 x14 +0.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 +0.0 x26 <= 2.0\n" +
                " c4: 0.0 x1 +0.0 x2 +1.0 x3 +0.0 x4 +0.0 x5 +0.0 x6 +1.0 x7 +1.0 x8 +0.0 x9 +0.0 x10 +0.0 x11 +0.0 x12 +0.0 x13 +0.0 x14 +0.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 +0.0 x26 <= 2.0\n" +
                " c5: 0.0 x1 +1.0 x2 +0.0 x3 +1.0 x4 +0.0 x5 +0.0 x6 +0.0 x7 +0.0 x8 +1.0 x9 +0.0 x10 +0.0 x11 +0.0 x12 +0.0 x13 +0.0 x14 +0.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 +0.0 x26 <= 2.0\n" +
                " c6: 1.0 x1 +0.0 x2 +0.0 x3 +0.0 x4 +1.0 x5 +0.0 x6 +0.0 x7 +0.0 x8 +0.0 x9 +1.0 x10 +0.0 x11 +0.0 x12 +0.0 x13 +0.0 x14 +0.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 +0.0 x26 <= 2.0\n" +
                " c7: 0.0 x1 +0.0 x2 +1.0 x3 +0.0 x4 +0.0 x5 +0.0 x6 +0.0 x7 +0.0 x8 +0.0 x9 +0.0 x10 +1.0 x11 +1.0 x12 +0.0 x13 +0.0 x14 +0.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 +0.0 x26 <= 2.0\n" +
                " c8: 0.0 x1 +1.0 x2 +0.0 x3 +0.0 x4 +0.0 x5 +0.0 x6 +1.0 x7 +0.0 x8 +0.0 x9 +0.0 x10 +0.0 x11 +0.0 x12 +1.0 x13 +0.0 x14 +0.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 +0.0 x26 <= 2.0\n" +
                " c9: 0.0 x1 +0.0 x2 +0.0 x3 +1.0 x4 +1.0 x5 +0.0 x6 +0.0 x7 +0.0 x8 +0.0 x9 +0.0 x10 +0.0 x11 +0.0 x12 +0.0 x13 +1.0 x14 +0.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 +0.0 x26 <= 2.0\n" +
                " c10: 1.0 x1 +0.0 x2 +0.0 x3 +0.0 x4 +0.0 x5 +0.0 x6 +0.0 x7 +1.0 x8 +0.0 x9 +0.0 x10 +0.0 x11 +0.0 x12 +0.0 x13 +0.0 x14 +1.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 +0.0 x26 <= 2.0\n" +
                " c11: 0.0 x1 +0.0 x2 +0.0 x3 +0.0 x4 +0.0 x5 +0.0 x6 +0.0 x7 +0.0 x8 +0.0 x9 +0.0 x10 +0.0 x11 +0.0 x12 +0.0 x13 +0.0 x14 +0.0 x15 +1.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 +0.0 x26 <= 0.0\n" +
                " c12: 0.0 x1 +0.0 x2 +0.0 x3 +0.0 x4 +0.0 x5 +0.0 x6 +0.0 x7 +0.0 x8 +0.0 x9 +0.0 x10 +0.0 x11 +0.0 x12 +0.0 x13 +0.0 x14 +0.0 x15 -1.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 +0.0 x26 <= 0.0\n" +
                " c13: 0.0 x1 +0.0 x2 +0.0 x3 +0.0 x4 +0.0 x5 +0.0 x6 +0.0 x7 +0.0 x8 +0.0 x9 +0.0 x10 +0.0 x11 +0.0 x12 +0.0 x13 +0.0 x14 +0.0 x15 +0.0 x16 +1.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 +0.0 x26 <= 0.0\n" +
                " c14: 0.0 x1 +0.0 x2 +0.0 x3 +0.0 x4 +0.0 x5 +0.0 x6 +0.0 x7 +0.0 x8 +0.0 x9 +0.0 x10 +0.0 x11 +0.0 x12 +0.0 x13 +0.0 x14 +0.0 x15 +0.0 x16 -1.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 +0.0 x26 <= 0.0\n" +
                " c15: 0.0 x1 +0.0 x2 +0.0 x3 +0.0 x4 +0.0 x5 +0.0 x6 +0.0 x7 +0.0 x8 +0.0 x9 +0.0 x10 +0.0 x11 +0.0 x12 +0.0 x13 +0.0 x14 +0.0 x15 +0.0 x16 +0.0 x17 +1.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 +0.0 x26 <= 0.0\n" +
                " c16: 0.0 x1 +0.0 x2 +0.0 x3 +0.0 x4 +0.0 x5 +0.0 x6 +0.0 x7 +0.0 x8 +0.0 x9 +0.0 x10 +0.0 x11 +0.0 x12 +0.0 x13 +0.0 x14 +0.0 x15 +0.0 x16 +0.0 x17 -1.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 +0.0 x26 <= 0.0\n" +
                " c17: 0.0 x1 +0.0 x2 +0.0 x3 +0.0 x4 +0.0 x5 +0.0 x6 +0.0 x7 +0.0 x8 +0.0 x9 +0.0 x10 +0.0 x11 +0.0 x12 +0.0 x13 +0.0 x14 +0.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 +1.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 +0.0 x26 <= 0.0\n" +
                " c18: 0.0 x1 +0.0 x2 +0.0 x3 +0.0 x4 +0.0 x5 +0.0 x6 +0.0 x7 +0.0 x8 +0.0 x9 +0.0 x10 +0.0 x11 +0.0 x12 +0.0 x13 +0.0 x14 +0.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 -1.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 +0.0 x26 <= 0.0\n" +
                " c19: 0.0 x1 +1.0 x2 +0.0 x3 +0.0 x4 +1.0 x5 +1.0 x6 +0.0 x7 +0.0 x8 +0.0 x9 +0.0 x10 +0.0 x11 +0.0 x12 +0.0 x13 +0.0 x14 +0.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 +0.0 x26 <= 2.0\n" +
                " c20: 0.0 x1 +1.0 x2 +0.0 x3 +0.0 x4 +0.0 x5 +0.0 x6 +0.0 x7 +1.0 x8 +0.0 x9 +1.0 x10 +0.0 x11 +0.0 x12 +0.0 x13 +0.0 x14 +0.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 +0.0 x26 <= 2.0\n" +
                " c21: 0.0 x1 +0.0 x2 +0.0 x3 +0.0 x4 +1.0 x5 +1.0 x6 +0.0 x7 +0.0 x8 +1.0 x9 +0.0 x10 +0.0 x11 +0.0 x12 +0.0 x13 +0.0 x14 +0.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 +0.0 x26 <= 2.0\n" +
                " c22: 0.0 x1 +1.0 x2 +0.0 x3 +0.0 x4 +0.0 x5 +0.0 x6 +0.0 x7 +0.0 x8 +0.0 x9 +0.0 x10 +0.0 x11 +1.0 x12 +0.0 x13 +0.0 x14 +1.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 +0.0 x26 <= 2.0\n" +
                " c23: 0.0 x1 +0.0 x2 +0.0 x3 +0.0 x4 +1.0 x5 +0.0 x6 +0.0 x7 +0.0 x8 +0.0 x9 +1.0 x10 +0.0 x11 +0.0 x12 +1.0 x13 +0.0 x14 +0.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 +0.0 x26 <= 2.0\n" +
                " c24: 0.0 x1 +0.0 x2 +0.0 x3 +0.0 x4 +0.0 x5 +1.0 x6 +0.0 x7 +1.0 x8 +0.0 x9 +0.0 x10 +0.0 x11 +0.0 x12 +0.0 x13 +1.0 x14 +0.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 +0.0 x26 <= 2.0\n" +
                " c25: 1.0 x1 +0.0 x2 +0.0 x3 +1.0 x4 +0.0 x5 +1.0 x6 +0.0 x7 +0.0 x8 +0.0 x9 +0.0 x10 +0.0 x11 +0.0 x12 +0.0 x13 +0.0 x14 +0.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 +0.0 x26 <= 2.0\n" +
                " c26: 1.0 x1 +0.0 x2 +0.0 x3 +0.0 x4 +0.0 x5 +0.0 x6 +1.0 x7 +0.0 x8 +1.0 x9 +0.0 x10 +0.0 x11 +0.0 x12 +0.0 x13 +0.0 x14 +0.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 +0.0 x26 <= 2.0\n" +
                " c27: 0.0 x1 +0.0 x2 +0.0 x3 +1.0 x4 +0.0 x5 +1.0 x6 +0.0 x7 +0.0 x8 +0.0 x9 +1.0 x10 +0.0 x11 +0.0 x12 +0.0 x13 +0.0 x14 +0.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 +0.0 x26 <= 2.0\n" +
                " c28: 1.0 x1 +0.0 x2 +0.0 x3 +0.0 x4 +0.0 x5 +0.0 x6 +0.0 x7 +0.0 x8 +0.0 x9 +0.0 x10 +1.0 x11 +0.0 x12 +1.0 x13 +0.0 x14 +0.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 +0.0 x26 <= 2.0\n" +
                " c29: 0.0 x1 +0.0 x2 +0.0 x3 +0.0 x4 +0.0 x5 +1.0 x6 +1.0 x7 +0.0 x8 +0.0 x9 +0.0 x10 +0.0 x11 +0.0 x12 +0.0 x13 +1.0 x14 +0.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 +0.0 x26 <= 2.0\n" +
                " c30: 0.0 x1 +0.0 x2 +0.0 x3 +1.0 x4 +0.0 x5 +0.0 x6 +0.0 x7 +0.0 x8 +1.0 x9 +0.0 x10 +0.0 x11 +0.0 x12 +0.0 x13 +0.0 x14 +1.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 +0.0 x26 <= 2.0\n" +
                " c31: 0.0 x1 +0.0 x2 +0.0 x3 +0.0 x4 +0.0 x5 +0.0 x6 +0.0 x7 +0.0 x8 +0.0 x9 +0.0 x10 +0.0 x11 +0.0 x12 +0.0 x13 +0.0 x14 +0.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 +1.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 +0.0 x26 <= 0.0\n" +
                " c32: 0.0 x1 +0.0 x2 +0.0 x3 +0.0 x4 +0.0 x5 +0.0 x6 +0.0 x7 +0.0 x8 +0.0 x9 +0.0 x10 +0.0 x11 +0.0 x12 +0.0 x13 +0.0 x14 +0.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 -1.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 +0.0 x26 <= 0.0\n" +
                " c33: 0.0 x1 +0.0 x2 +0.0 x3 +0.0 x4 +0.0 x5 +0.0 x6 +0.0 x7 +0.0 x8 +0.0 x9 +0.0 x10 +0.0 x11 +0.0 x12 +0.0 x13 +0.0 x14 +0.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 +1.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 +0.0 x26 <= 0.0\n" +
                " c34: 0.0 x1 +0.0 x2 +0.0 x3 +0.0 x4 +0.0 x5 +0.0 x6 +0.0 x7 +0.0 x8 +0.0 x9 +0.0 x10 +0.0 x11 +0.0 x12 +0.0 x13 +0.0 x14 +0.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 -1.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 +0.0 x26 <= 0.0\n" +
                " c35: 0.0 x1 +0.0 x2 +0.0 x3 +0.0 x4 +0.0 x5 +0.0 x6 +0.0 x7 +0.0 x8 +0.0 x9 +0.0 x10 +0.0 x11 +0.0 x12 +0.0 x13 +0.0 x14 +0.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 +1.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 +0.0 x26 <= 0.0\n" +
                " c36: 0.0 x1 +0.0 x2 +0.0 x3 +0.0 x4 +0.0 x5 +0.0 x6 +0.0 x7 +0.0 x8 +0.0 x9 +0.0 x10 +0.0 x11 +0.0 x12 +0.0 x13 +0.0 x14 +0.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 -1.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 +0.0 x26 <= 0.0\n" +
                " c37: 0.0 x1 +0.0 x2 +0.0 x3 +0.0 x4 +1.0 x5 +0.0 x6 +0.0 x7 +1.0 x8 +1.0 x9 +0.0 x10 +0.0 x11 +0.0 x12 +0.0 x13 +0.0 x14 +0.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 +0.0 x26 <= 2.0\n" +
                " c38: 0.0 x1 +0.0 x2 +0.0 x3 +0.0 x4 +1.0 x5 +0.0 x6 +0.0 x7 +0.0 x8 +0.0 x9 +0.0 x10 +0.0 x11 +1.0 x12 +0.0 x13 +1.0 x14 +0.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 +0.0 x26 <= 2.0\n" +
                " c39: 0.0 x1 +0.0 x2 +0.0 x3 +0.0 x4 +0.0 x5 +0.0 x6 +0.0 x7 +1.0 x8 +1.0 x9 +0.0 x10 +0.0 x11 +0.0 x12 +1.0 x13 +0.0 x14 +0.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 +0.0 x26 <= 2.0\n" +
                " c40: 0.0 x1 +0.0 x2 +0.0 x3 +0.0 x4 +0.0 x5 +1.0 x6 +0.0 x7 +0.0 x8 +1.0 x9 +1.0 x10 +0.0 x11 +0.0 x12 +0.0 x13 +0.0 x14 +0.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 +0.0 x26 <= 2.0\n" +
                " c41: 0.0 x1 +0.0 x2 +0.0 x3 +0.0 x4 +0.0 x5 +1.0 x6 +0.0 x7 +0.0 x8 +0.0 x9 +0.0 x10 +0.0 x11 +0.0 x12 +1.0 x13 +0.0 x14 +1.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 +0.0 x26 <= 2.0\n" +
                " c42: 0.0 x1 +0.0 x2 +0.0 x3 +0.0 x4 +0.0 x5 +0.0 x6 +0.0 x7 +0.0 x8 +1.0 x9 +1.0 x10 +0.0 x11 +0.0 x12 +0.0 x13 +1.0 x14 +0.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 +0.0 x26 <= 2.0\n" +
                " c43: 0.0 x1 +0.0 x2 +0.0 x3 +1.0 x4 +0.0 x5 +0.0 x6 +1.0 x7 +0.0 x8 +0.0 x9 +1.0 x10 +0.0 x11 +0.0 x12 +0.0 x13 +0.0 x14 +0.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 +0.0 x26 <= 2.0\n" +
                " c44: 0.0 x1 +0.0 x2 +0.0 x3 +1.0 x4 +0.0 x5 +0.0 x6 +0.0 x7 +0.0 x8 +0.0 x9 +0.0 x10 +1.0 x11 +0.0 x12 +0.0 x13 +1.0 x14 +0.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 +0.0 x26 <= 2.0\n" +
                " c45: 0.0 x1 +0.0 x2 +0.0 x3 +0.0 x4 +0.0 x5 +0.0 x6 +1.0 x7 +0.0 x8 +0.0 x9 +1.0 x10 +0.0 x11 +0.0 x12 +0.0 x13 +0.0 x14 +1.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 +0.0 x26 <= 2.0\n" +
                " c46: 0.0 x1 +0.0 x2 +0.0 x3 +0.0 x4 +0.0 x5 +0.0 x6 +0.0 x7 +0.0 x8 +0.0 x9 +0.0 x10 +0.0 x11 +0.0 x12 +0.0 x13 +0.0 x14 +0.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 +1.0 x23 +0.0 x24 +0.0 x25 +0.0 x26 <= 0.0\n" +
                " c47: 0.0 x1 +0.0 x2 +0.0 x3 +0.0 x4 +0.0 x5 +0.0 x6 +0.0 x7 +0.0 x8 +0.0 x9 +0.0 x10 +0.0 x11 +0.0 x12 +0.0 x13 +0.0 x14 +0.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 -1.0 x23 +0.0 x24 +0.0 x25 +0.0 x26 <= 0.0\n" +
                " c48: 0.0 x1 +0.0 x2 +0.0 x3 +0.0 x4 +0.0 x5 +0.0 x6 +0.0 x7 +0.0 x8 +0.0 x9 +0.0 x10 +0.0 x11 +0.0 x12 +0.0 x13 +0.0 x14 +0.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 +1.0 x24 +0.0 x25 +0.0 x26 <= 0.0\n" +
                " c49: 0.0 x1 +0.0 x2 +0.0 x3 +0.0 x4 +0.0 x5 +0.0 x6 +0.0 x7 +0.0 x8 +0.0 x9 +0.0 x10 +0.0 x11 +0.0 x12 +0.0 x13 +0.0 x14 +0.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 -1.0 x24 +0.0 x25 +0.0 x26 <= 0.0\n" +
                " c50: 0.0 x1 +0.0 x2 +0.0 x3 +0.0 x4 +0.0 x5 +0.0 x6 +0.0 x7 +1.0 x8 +0.0 x9 +0.0 x10 +0.0 x11 +1.0 x12 +1.0 x13 +0.0 x14 +0.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 +0.0 x26 <= 2.0\n" +
                " c51: 0.0 x1 +0.0 x2 +0.0 x3 +0.0 x4 +0.0 x5 +0.0 x6 +0.0 x7 +0.0 x8 +1.0 x9 +0.0 x10 +0.0 x11 +0.0 x12 +1.0 x13 +1.0 x14 +0.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 +0.0 x26 <= 2.0\n" +
                " c52: 0.0 x1 +0.0 x2 +0.0 x3 +0.0 x4 +0.0 x5 +0.0 x6 +0.0 x7 +0.0 x8 +0.0 x9 +1.0 x10 +0.0 x11 +0.0 x12 +0.0 x13 +1.0 x14 +1.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 +0.0 x26 <= 2.0\n" +
                " c53: 0.0 x1 +0.0 x2 +0.0 x3 +0.0 x4 +0.0 x5 +0.0 x6 +1.0 x7 +0.0 x8 +0.0 x9 +0.0 x10 +1.0 x11 +0.0 x12 +0.0 x13 +0.0 x14 +1.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 +0.0 x26 <= 2.0\n" +
                " c54: 0.0 x1 +0.0 x2 +0.0 x3 +0.0 x4 +0.0 x5 +0.0 x6 +0.0 x7 +0.0 x8 +0.0 x9 +0.0 x10 +0.0 x11 +0.0 x12 +0.0 x13 +0.0 x14 +0.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 +1.0 x25 +0.0 x26 <= 0.0\n" +
                " c55: 0.0 x1 +0.0 x2 +0.0 x3 +0.0 x4 +0.0 x5 +0.0 x6 +0.0 x7 +0.0 x8 +0.0 x9 +0.0 x10 +0.0 x11 +0.0 x12 +0.0 x13 +0.0 x14 +0.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 -1.0 x25 +0.0 x26 <= 0.0\n" +
                " c56: 1.0 x1 +1.0 x2 +1.0 x3 +1.0 x4 +1.0 x5 +1.0 x6 +1.0 x7 +1.0 x8 +1.0 x9 +1.0 x10 +1.0 x11 +1.0 x12 +1.0 x13 +1.0 x14 +1.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 -1.0 x26 <= 0.0\n" +
                " c57: -1.0 x1 -1.0 x2 -1.0 x3 -1.0 x4 -1.0 x5 -1.0 x6 -1.0 x7 -1.0 x8 -1.0 x9 -1.0 x10 -1.0 x11 -1.0 x12 -1.0 x13 -1.0 x14 -1.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 +1.0 x26 <= 0.0\n" +
                " c58: 0.0 x1 +0.0 x2 +0.0 x3 +0.0 x4 +0.0 x5 +0.0 x6 +0.0 x7 +0.0 x8 +0.0 x9 +0.0 x10 +0.0 x11 +0.0 x12 +0.0 x13 +0.0 x14 +0.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 -1.0 x26 <= -3.0\n" +
                " c59: 0.0 x1 +0.0 x2 +0.0 x3 +0.0 x4 +0.0 x5 +0.0 x6 +0.0 x7 +0.0 x8 +0.0 x9 +0.0 x10 +0.0 x11 +0.0 x12 +0.0 x13 +0.0 x14 +0.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 +1.0 x26 <= 12.0\n" +
                " c60: 0.0 x1 +0.0 x2 +0.0 x3 +0.0 x4 +0.0 x5 +0.0 x6 +0.0 x7 +0.0 x8 +0.0 x9 +0.0 x10 -1.0 x11 +0.0 x12 +0.0 x13 +0.0 x14 +0.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 +0.0 x26 <= 0.0\n" +
                " c61: 0.0 x1 +0.0 x2 +0.0 x3 +0.0 x4 +0.0 x5 +0.0 x6 +0.0 x7 +0.0 x8 +0.0 x9 +0.0 x10 +1.0 x11 +0.0 x12 +0.0 x13 +0.0 x14 +0.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 +0.0 x26 <= 1.0\n" +
                " c62: 0.0 x1 +0.0 x2 +0.0 x3 +0.0 x4 +0.0 x5 +0.0 x6 +0.0 x7 +0.0 x8 +0.0 x9 +0.0 x10 +0.0 x11 +0.0 x12 +0.0 x13 +0.0 x14 -1.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 +0.0 x26 <= 0.0\n" +
                " c63: 0.0 x1 +0.0 x2 +0.0 x3 +0.0 x4 +0.0 x5 +0.0 x6 +0.0 x7 +0.0 x8 +0.0 x9 +0.0 x10 +0.0 x11 +0.0 x12 +0.0 x13 +0.0 x14 +1.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 +0.0 x26 <= 1.0\n" +
                " c64: 0.0 x1 +0.0 x2 +0.0 x3 +0.0 x4 +0.0 x5 +0.0 x6 +0.0 x7 +0.0 x8 +0.0 x9 +0.0 x10 +0.0 x11 +0.0 x12 +0.0 x13 -1.0 x14 +0.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 +0.0 x26 <= 0.0\n" +
                " c65: 0.0 x1 +0.0 x2 +0.0 x3 +0.0 x4 +0.0 x5 +0.0 x6 +0.0 x7 +0.0 x8 +0.0 x9 +0.0 x10 +0.0 x11 +0.0 x12 +0.0 x13 +1.0 x14 +0.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 +0.0 x26 <= 1.0\n" +
                " c66: 0.0 x1 +0.0 x2 +0.0 x3 +0.0 x4 +0.0 x5 +0.0 x6 +0.0 x7 +0.0 x8 +0.0 x9 +0.0 x10 +0.0 x11 +0.0 x12 -1.0 x13 +0.0 x14 +0.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 +0.0 x26 <= 0.0\n" +
                " c67: 0.0 x1 +0.0 x2 +0.0 x3 +0.0 x4 +0.0 x5 +0.0 x6 +0.0 x7 +0.0 x8 +0.0 x9 +0.0 x10 +0.0 x11 +0.0 x12 +1.0 x13 +0.0 x14 +0.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 +0.0 x26 <= 1.0\n" +
                " c68: 0.0 x1 +0.0 x2 +0.0 x3 +0.0 x4 +0.0 x5 +0.0 x6 +0.0 x7 +0.0 x8 +0.0 x9 +0.0 x10 +0.0 x11 -1.0 x12 +0.0 x13 +0.0 x14 +0.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 +0.0 x26 <= 0.0\n" +
                " c69: 0.0 x1 +0.0 x2 +0.0 x3 +0.0 x4 +0.0 x5 +0.0 x6 +0.0 x7 +0.0 x8 +0.0 x9 +0.0 x10 +0.0 x11 +1.0 x12 +0.0 x13 +0.0 x14 +0.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 +0.0 x26 <= 1.0\n" +
                " c70: 0.0 x1 +0.0 x2 +0.0 x3 +0.0 x4 +0.0 x5 +0.0 x6 +0.0 x7 +0.0 x8 +0.0 x9 +0.0 x10 +0.0 x11 +0.0 x12 +0.0 x13 +0.0 x14 +0.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 -1.0 x25 +0.0 x26 <= 0.0\n" +
                " c71: 0.0 x1 +0.0 x2 +0.0 x3 +0.0 x4 +0.0 x5 +0.0 x6 +0.0 x7 +0.0 x8 +0.0 x9 +0.0 x10 +0.0 x11 +0.0 x12 +0.0 x13 +0.0 x14 +0.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 +1.0 x25 +0.0 x26 <= 0.0\n" +
                " c72: 0.0 x1 +0.0 x2 +0.0 x3 +0.0 x4 +0.0 x5 +0.0 x6 -1.0 x7 +0.0 x8 +0.0 x9 +0.0 x10 +0.0 x11 +0.0 x12 +0.0 x13 +0.0 x14 +0.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 +0.0 x26 <= 0.0\n" +
                " c73: 0.0 x1 +0.0 x2 +0.0 x3 +0.0 x4 +0.0 x5 +0.0 x6 +1.0 x7 +0.0 x8 +0.0 x9 +0.0 x10 +0.0 x11 +0.0 x12 +0.0 x13 +0.0 x14 +0.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 +0.0 x26 <= 1.0\n" +
                " c74: 0.0 x1 +0.0 x2 +0.0 x3 +0.0 x4 +0.0 x5 +0.0 x6 +0.0 x7 +0.0 x8 +0.0 x9 -1.0 x10 +0.0 x11 +0.0 x12 +0.0 x13 +0.0 x14 +0.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 +0.0 x26 <= 0.0\n" +
                " c75: 0.0 x1 +0.0 x2 +0.0 x3 +0.0 x4 +0.0 x5 +0.0 x6 +0.0 x7 +0.0 x8 +0.0 x9 +1.0 x10 +0.0 x11 +0.0 x12 +0.0 x13 +0.0 x14 +0.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 +0.0 x26 <= 1.0\n" +
                " c76: 0.0 x1 +0.0 x2 +0.0 x3 +0.0 x4 +0.0 x5 +0.0 x6 +0.0 x7 +0.0 x8 -1.0 x9 +0.0 x10 +0.0 x11 +0.0 x12 +0.0 x13 +0.0 x14 +0.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 +0.0 x26 <= 0.0\n" +
                " c77: 0.0 x1 +0.0 x2 +0.0 x3 +0.0 x4 +0.0 x5 +0.0 x6 +0.0 x7 +0.0 x8 +1.0 x9 +0.0 x10 +0.0 x11 +0.0 x12 +0.0 x13 +0.0 x14 +0.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 +0.0 x26 <= 1.0\n" +
                " c78: 0.0 x1 +0.0 x2 +0.0 x3 +0.0 x4 +0.0 x5 +0.0 x6 +0.0 x7 -1.0 x8 +0.0 x9 +0.0 x10 +0.0 x11 +0.0 x12 +0.0 x13 +0.0 x14 +0.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 +0.0 x26 <= 0.0\n" +
                " c79: 0.0 x1 +0.0 x2 +0.0 x3 +0.0 x4 +0.0 x5 +0.0 x6 +0.0 x7 +1.0 x8 +0.0 x9 +0.0 x10 +0.0 x11 +0.0 x12 +0.0 x13 +0.0 x14 +0.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 +0.0 x26 <= 1.0\n" +
                " c80: 0.0 x1 +0.0 x2 +0.0 x3 +0.0 x4 +0.0 x5 +0.0 x6 +0.0 x7 +0.0 x8 +0.0 x9 +0.0 x10 +0.0 x11 +0.0 x12 +0.0 x13 +0.0 x14 +0.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 -1.0 x24 +0.0 x25 +0.0 x26 <= 0.0\n" +
                " c81: 0.0 x1 +0.0 x2 +0.0 x3 +0.0 x4 +0.0 x5 +0.0 x6 +0.0 x7 +0.0 x8 +0.0 x9 +0.0 x10 +0.0 x11 +0.0 x12 +0.0 x13 +0.0 x14 +0.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 +1.0 x24 +0.0 x25 +0.0 x26 <= 0.0\n" +
                " c82: 0.0 x1 +0.0 x2 +0.0 x3 +0.0 x4 +0.0 x5 +0.0 x6 +0.0 x7 +0.0 x8 +0.0 x9 +0.0 x10 +0.0 x11 +0.0 x12 +0.0 x13 +0.0 x14 +0.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 -1.0 x23 +0.0 x24 +0.0 x25 +0.0 x26 <= 0.0\n" +
                " c83: 0.0 x1 +0.0 x2 +0.0 x3 +0.0 x4 +0.0 x5 +0.0 x6 +0.0 x7 +0.0 x8 +0.0 x9 +0.0 x10 +0.0 x11 +0.0 x12 +0.0 x13 +0.0 x14 +0.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 +1.0 x23 +0.0 x24 +0.0 x25 +0.0 x26 <= 0.0\n" +
                " c84: 0.0 x1 +0.0 x2 +0.0 x3 -1.0 x4 +0.0 x5 +0.0 x6 +0.0 x7 +0.0 x8 +0.0 x9 +0.0 x10 +0.0 x11 +0.0 x12 +0.0 x13 +0.0 x14 +0.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 +0.0 x26 <= 0.0\n" +
                " c85: 0.0 x1 +0.0 x2 +0.0 x3 +1.0 x4 +0.0 x5 +0.0 x6 +0.0 x7 +0.0 x8 +0.0 x9 +0.0 x10 +0.0 x11 +0.0 x12 +0.0 x13 +0.0 x14 +0.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 +0.0 x26 <= 0.0\n" +
                " c86: 0.0 x1 +0.0 x2 +0.0 x3 +0.0 x4 +0.0 x5 -1.0 x6 +0.0 x7 +0.0 x8 +0.0 x9 +0.0 x10 +0.0 x11 +0.0 x12 +0.0 x13 +0.0 x14 +0.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 +0.0 x26 <= 0.0\n" +
                " c87: 0.0 x1 +0.0 x2 +0.0 x3 +0.0 x4 +0.0 x5 +1.0 x6 +0.0 x7 +0.0 x8 +0.0 x9 +0.0 x10 +0.0 x11 +0.0 x12 +0.0 x13 +0.0 x14 +0.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 +0.0 x26 <= 0.0\n" +
                " c88: 0.0 x1 +0.0 x2 +0.0 x3 +0.0 x4 -1.0 x5 +0.0 x6 +0.0 x7 +0.0 x8 +0.0 x9 +0.0 x10 +0.0 x11 +0.0 x12 +0.0 x13 +0.0 x14 +0.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 +0.0 x26 <= -1.0\n" +
                " c89: 0.0 x1 +0.0 x2 +0.0 x3 +0.0 x4 +1.0 x5 +0.0 x6 +0.0 x7 +0.0 x8 +0.0 x9 +0.0 x10 +0.0 x11 +0.0 x12 +0.0 x13 +0.0 x14 +0.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 +0.0 x26 <= 1.0\n" +
                " c90: 0.0 x1 +0.0 x2 +0.0 x3 +0.0 x4 +0.0 x5 +0.0 x6 +0.0 x7 +0.0 x8 +0.0 x9 +0.0 x10 +0.0 x11 +0.0 x12 +0.0 x13 +0.0 x14 +0.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 -1.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 +0.0 x26 <= 0.0\n" +
                " c91: 0.0 x1 +0.0 x2 +0.0 x3 +0.0 x4 +0.0 x5 +0.0 x6 +0.0 x7 +0.0 x8 +0.0 x9 +0.0 x10 +0.0 x11 +0.0 x12 +0.0 x13 +0.0 x14 +0.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 +1.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 +0.0 x26 <= 0.0\n" +
                " c92: 0.0 x1 +0.0 x2 +0.0 x3 +0.0 x4 +0.0 x5 +0.0 x6 +0.0 x7 +0.0 x8 +0.0 x9 +0.0 x10 +0.0 x11 +0.0 x12 +0.0 x13 +0.0 x14 +0.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 -1.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 +0.0 x26 <= 0.0\n" +
                " c93: 0.0 x1 +0.0 x2 +0.0 x3 +0.0 x4 +0.0 x5 +0.0 x6 +0.0 x7 +0.0 x8 +0.0 x9 +0.0 x10 +0.0 x11 +0.0 x12 +0.0 x13 +0.0 x14 +0.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 +1.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 +0.0 x26 <= 0.0\n" +
                " c94: 0.0 x1 +0.0 x2 +0.0 x3 +0.0 x4 +0.0 x5 +0.0 x6 +0.0 x7 +0.0 x8 +0.0 x9 +0.0 x10 +0.0 x11 +0.0 x12 +0.0 x13 +0.0 x14 +0.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 -1.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 +0.0 x26 <= 0.0\n" +
                " c95: 0.0 x1 +0.0 x2 +0.0 x3 +0.0 x4 +0.0 x5 +0.0 x6 +0.0 x7 +0.0 x8 +0.0 x9 +0.0 x10 +0.0 x11 +0.0 x12 +0.0 x13 +0.0 x14 +0.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 +1.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 +0.0 x26 <= 0.0\n" +
                " c96: -1.0 x1 +0.0 x2 +0.0 x3 +0.0 x4 +0.0 x5 +0.0 x6 +0.0 x7 +0.0 x8 +0.0 x9 +0.0 x10 +0.0 x11 +0.0 x12 +0.0 x13 +0.0 x14 +0.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 +0.0 x26 <= 0.0\n" +
                " c97: 1.0 x1 +0.0 x2 +0.0 x3 +0.0 x4 +0.0 x5 +0.0 x6 +0.0 x7 +0.0 x8 +0.0 x9 +0.0 x10 +0.0 x11 +0.0 x12 +0.0 x13 +0.0 x14 +0.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 +0.0 x26 <= 0.0\n" +
                " c98: 0.0 x1 -1.0 x2 +0.0 x3 +0.0 x4 +0.0 x5 +0.0 x6 +0.0 x7 +0.0 x8 +0.0 x9 +0.0 x10 +0.0 x11 +0.0 x12 +0.0 x13 +0.0 x14 +0.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 +0.0 x26 <= -1.0\n" +
                " c99: 0.0 x1 +1.0 x2 +0.0 x3 +0.0 x4 +0.0 x5 +0.0 x6 +0.0 x7 +0.0 x8 +0.0 x9 +0.0 x10 +0.0 x11 +0.0 x12 +0.0 x13 +0.0 x14 +0.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 +0.0 x26 <= 1.0\n" +
                " c100: 0.0 x1 +0.0 x2 +0.0 x3 +0.0 x4 +0.0 x5 +0.0 x6 +0.0 x7 +0.0 x8 +0.0 x9 +0.0 x10 +0.0 x11 +0.0 x12 +0.0 x13 +0.0 x14 +0.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 -1.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 +0.0 x26 <= 0.0\n" +
                " c101: 0.0 x1 +0.0 x2 +0.0 x3 +0.0 x4 +0.0 x5 +0.0 x6 +0.0 x7 +0.0 x8 +0.0 x9 +0.0 x10 +0.0 x11 +0.0 x12 +0.0 x13 +0.0 x14 +0.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 +1.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 +0.0 x26 <= 0.0\n" +
                " c102: 0.0 x1 +0.0 x2 +0.0 x3 +0.0 x4 +0.0 x5 +0.0 x6 +0.0 x7 +0.0 x8 +0.0 x9 +0.0 x10 +0.0 x11 +0.0 x12 +0.0 x13 +0.0 x14 +0.0 x15 +0.0 x16 +0.0 x17 -1.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 +0.0 x26 <= 0.0\n" +
                " c103: 0.0 x1 +0.0 x2 +0.0 x3 +0.0 x4 +0.0 x5 +0.0 x6 +0.0 x7 +0.0 x8 +0.0 x9 +0.0 x10 +0.0 x11 +0.0 x12 +0.0 x13 +0.0 x14 +0.0 x15 +0.0 x16 +0.0 x17 +1.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 +0.0 x26 <= 0.0\n" +
                " c104: 0.0 x1 +0.0 x2 +0.0 x3 +0.0 x4 +0.0 x5 +0.0 x6 +0.0 x7 +0.0 x8 +0.0 x9 +0.0 x10 +0.0 x11 +0.0 x12 +0.0 x13 +0.0 x14 +0.0 x15 +0.0 x16 -1.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 +0.0 x26 <= 0.0\n" +
                " c105: 0.0 x1 +0.0 x2 +0.0 x3 +0.0 x4 +0.0 x5 +0.0 x6 +0.0 x7 +0.0 x8 +0.0 x9 +0.0 x10 +0.0 x11 +0.0 x12 +0.0 x13 +0.0 x14 +0.0 x15 +0.0 x16 +1.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 +0.0 x26 <= 0.0\n" +
                " c106: 0.0 x1 +0.0 x2 +0.0 x3 +0.0 x4 +0.0 x5 +0.0 x6 +0.0 x7 +0.0 x8 +0.0 x9 +0.0 x10 +0.0 x11 +0.0 x12 +0.0 x13 +0.0 x14 +0.0 x15 -1.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 +0.0 x26 <= 0.0\n" +
                " c107: 0.0 x1 +0.0 x2 +0.0 x3 +0.0 x4 +0.0 x5 +0.0 x6 +0.0 x7 +0.0 x8 +0.0 x9 +0.0 x10 +0.0 x11 +0.0 x12 +0.0 x13 +0.0 x14 +0.0 x15 +1.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 +0.0 x26 <= 0.0\n" +
                " c108: 0.0 x1 +0.0 x2 -1.0 x3 +0.0 x4 +0.0 x5 +0.0 x6 +0.0 x7 +0.0 x8 +0.0 x9 +0.0 x10 +0.0 x11 +0.0 x12 +0.0 x13 +0.0 x14 +0.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 +0.0 x26 <= -1.0\n" +
                " c109: 0.0 x1 +0.0 x2 +1.0 x3 +0.0 x4 +0.0 x5 +0.0 x6 +0.0 x7 +0.0 x8 +0.0 x9 +0.0 x10 +0.0 x11 +0.0 x12 +0.0 x13 +0.0 x14 +0.0 x15 +0.0 x16 +0.0 x17 +0.0 x18 +0.0 x19 +0.0 x20 +0.0 x21 +0.0 x22 +0.0 x23 +0.0 x24 +0.0 x25 +0.0 x26 <= 1.0\n" +
                "End\n";

        double v = 8.;
        LinearProgram lp = LinearProgram.parseLPFromString(strLp);
        method.invoke(null, lp.getA(), lp.getB(), lp.getC(), v);
    }

}