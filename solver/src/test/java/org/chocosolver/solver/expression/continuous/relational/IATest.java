/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.expression.continuous.relational;

import org.chocosolver.solver.expression.continuous.arithmetic.RealIntervalConstant;
import org.chocosolver.util.objects.RealInterval;
import org.chocosolver.util.tools.RealUtils;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Random;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 19/08/2020
 */
public class IATest {
    RealInterval[] intervals = new RealInterval[3];
    public static int nbBox = 20;

    @BeforeTest(alwaysRun = true)
    public void setUp() {
        Random rand = new Random();
        for (int i = 0; i < intervals.length; i++) {
            double a = rand.nextDouble() * 10 * (rand.nextBoolean() ? -1 : 1);
            double b = rand.nextDouble() * 10 * (rand.nextBoolean() ? -1 : 1);
            intervals[i] = new RealIntervalConstant(Math.min(a, b), Math.max(a, b));
        }
    }

    @AfterTest(alwaysRun = true)
    public void tearDown() {
        Arrays.fill(intervals, null);
    }

    @Test
    public void testPlus() {
        RealInterval a = intervals[0];
        RealInterval b = intervals[1];
        System.out.println("Testing " + a + " + " + b);

        RealInterval res = RealUtils.add(a, b);
        double aw = (a.getUB() - a.getLB()) / nbBox;
        double bw = (b.getUB() - b.getLB()) / nbBox;
        for (int i = 1; i < nbBox; i++) {
            double aa = a.getLB() + aw * i;
            for (int j = 1; j < nbBox; j++) {
                double bb = b.getLB() + bw * j;
                Assert.assertTrue(aa + bb > res.getLB());
                Assert.assertTrue(aa + bb < res.getUB());
            }
        }
    }

    @Test
    public void testMinus() {
        RealInterval a = intervals[0];
        RealInterval b = intervals[1];
        System.out.println("Testing " + a + " - " + b);

        RealInterval res = RealUtils.sub(a, b);
        double aw = (a.getUB() - a.getLB()) / nbBox;
        double bw = (b.getUB() - b.getLB()) / nbBox;
        for (int i = 1; i < nbBox; i++) {
            double aa = a.getLB() + aw * i;
            for (int j = 1; j < nbBox; j++) {
                double bb = b.getLB() + bw * j;
                Assert.assertTrue(aa - bb > res.getLB());
                Assert.assertTrue(aa - bb < res.getUB());
            }
        }
    }

    @Test
    public void testMult() {
        RealInterval a = intervals[0];
        RealInterval b = intervals[1];
        System.out.println("Testing " + a + " * " + b);

        RealInterval res = RealUtils.mul(a, b);
        double aw = (a.getUB() - a.getLB()) / nbBox;
        double bw = (b.getUB() - b.getLB()) / nbBox;
        for (int i = 1; i < nbBox; i++) {
            double aa = a.getLB() + aw * i;
            for (int j = 1; j < nbBox; j++) {
                double bb = b.getLB() + bw * j;
                Assert.assertTrue(aa * bb > res.getLB());
                Assert.assertTrue(aa * bb < res.getUB());
            }
        }
    }

    @Test
    public void testDiv() {
        RealInterval a = intervals[0];
        RealInterval b = intervals[1];
        RealInterval c = intervals[2];
        System.out.println("Testing " + a + " / " + b + " in " + c);

        RealInterval res = RealUtils.odiv_wrt(a, b, c);
        double aw = (a.getUB() - a.getLB()) / nbBox;
        double bw = (b.getUB() - b.getLB()) / nbBox;
        for (int i = 1; i < nbBox; i++) {
            double aa = a.getLB() + aw * i;
            for (int j = 1; j < nbBox; j++) {
                double bb = b.getLB() + bw * j;
                if (bb != 0) {
                    Assert.assertTrue(aa / bb > res.getLB() || aa / bb > c.getUB() || aa / bb < c.getLB());
                    Assert.assertTrue(aa / bb < res.getUB() || aa / bb > c.getUB() || aa / bb < c.getLB());
                }
            }
        }
    }

    @Test
    public void testIPower() {
        int[] power = new int[]{2, 3, 4};
        RealInterval a = intervals[0];

        for (int powerIdx = 0; powerIdx < power.length; powerIdx++) {
            int p = power[powerIdx];
            System.out.println("Testing " + a + " ** " + p);
            RealInterval res = RealUtils.iPower(a, p);

            double aw = (a.getUB() - a.getLB()) / nbBox;
            for (int i = 1; i < nbBox; i++) {
                double aa = a.getLB() + aw * i;
                Assert.assertTrue(Math.pow(aa, p) > res.getLB());
                Assert.assertTrue(Math.pow(aa, p) < res.getUB());
            }
        }
    }
    
      /*public void testIRoot() {
        int[] power = new int[]{2,3,4};
        RealInterval a = intervals[0];
        RealInterval b = intervals[1];
    
        if (a.getLB() > 0) for (int powerIdx = 0; powerIdx < power.length; powerIdx++) {
          int p = power[powerIdx];
          System.out.println("Testing " + a + " ** 1/" + p);
          RealInterval res = RealMath.iRoot(a, p, b);
    
          double aw = (a.getUB() - a.getLB()) / nbBox;
          for(int i = 1; i < nbBox; i ++) {
            double aa = a.getLB() + aw * i;
            double calc = Math.pow(aa, 1/p);
            assertTrue(calc > res.getLB() || calc > b.getUB() || calc < b.getLB());
            assertTrue(calc < res.getUB() || calc > b.getUB() || calc < b.getLB());
          }
        }
      } */


    @Test
    public void testSin() {
        RealInterval a = intervals[0];

        System.out.println("Testing sin(" + a + ")");
        RealInterval res = RealUtils.sin(a);

        double aw = (a.getUB() - a.getLB()) / nbBox;
        for (int i = 1; i < nbBox; i++) {
            double aa = a.getLB() + aw * i;
            Assert.assertTrue(Math.sin(aa) > res.getLB());
            Assert.assertTrue(Math.sin(aa) < res.getUB());
        }
    }


    @Test
    public void testCos() {
        RealInterval a = intervals[0];

        System.out.println("Testing cos(" + a + ")");
        RealInterval res = RealUtils.cos(a);

        double aw = (a.getUB() - a.getLB()) / nbBox;
        for (int i = 1; i < nbBox; i++) {
            double aa = a.getLB() + aw * i;
            Assert.assertTrue(Math.cos(aa) > res.getLB());
            Assert.assertTrue(Math.cos(aa) < res.getUB());
        }
    }
}
