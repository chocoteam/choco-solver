package org.chocosolver.lp;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.BitSet;
import java.util.HashMap;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 01/03/2023
 */
public class MILPTest {

    @Test(groups = "1s")
    public void testFeasible01() {
        // 29.3-7
        double[] c = {-1, -1, -1};
        double[][] A = {
                {-2, -7.5, -3},
                {-20, -5, -10}
        };
        double[] b = {-10000, -30000};
        BitSet integers = new BitSet();
        integers.set(0, 3);
        MILP lp = new MILP(A, b, c, integers, new BitSet());
        Assert.assertEquals(lp.branchAndBound(), LinearProgram.Status.FEASIBLE);
        Assert.assertEquals(lp.objective(), -2250, 1e-8);
        Assert.assertEquals(lp.value(0), 1250, 1e-8);
        Assert.assertEquals(lp.value(1), 1000, 1e-8);
        Assert.assertEquals(lp.value(2), 0, 1e-8);
    }


    @Test(groups = "1s")
    public void testFeasible2() {
        // 29.5-5
        double[] c = {1, 3};
        double[][] A = {
                {1, -1},
                {-1, -1},
                {-1, 4}
        };
        double[] b = {8, -3, 2};
        BitSet integers = new BitSet();
        integers.set(0, 2);
        MILP lp = new MILP(A, b, c, integers, new BitSet());
        Assert.assertEquals(lp.branchAndBound(), LinearProgram.Status.FEASIBLE);
        Assert.assertEquals(lp.value(0), 11., 1e-8);
        Assert.assertEquals(lp.value(1), 3., 1e-8);
        Assert.assertEquals(lp.objective(), 20., 1e-8);
    }

    @Test(groups = "1s")
    public void testFeasible3() {
        // https://en.wikipedia.org/wiki/Branch_and_bound
        MILP milp = new MILP();
        milp.makeIntegers(2);
        milp.addLeq(new double[]{1, 1}, 50);
        milp.addLeq(new double[]{4, 7}, 280);
        //milp.addLeq(new double[]{1, 0}, 23);
        milp.setObjective(true, new double[]{5., 6.});
        Assert.assertEquals(milp.branchAndBound(), LinearProgram.Status.FEASIBLE);
        Assert.assertEquals(milp.value(0), 24., 1e-8);
        Assert.assertEquals(milp.value(1), 26., 1e-8);
        Assert.assertEquals(milp.objective(), 276., 1e-8);
    }

    @Test(groups = "1s")
    public void testDiffn1() {
        MILP milp = new MILP(false);
        // 7 taches
        milp.makeVariables(7);  // starting time
        milp.makeBooleans(21); // ti avant tj ou non
        int[] d = new int[]{60, 274, 6, 30, 45, 30, 30};
        // fixed tasks
        milp.addEq(0, 1., 33);
        milp.addEq(1, 1., 116);
        milp.addEq(2, 1., 494);

        // free tasks
        milp.addLeq(3, 1., 464);
        milp.addLeq(4, 1., 484);
        milp.addLeq(5, 1., 499);
        milp.addLeq(6, 1., 499);
        // diffn
        double M = 9999.;
        for (int i = 0, k = 7; i < 7; i++) {
            int _i = i;
            for (int j = i + 1; j < 7; j++, k++) {
                int _j = j;
                int _k = k;
                // ti + di <= tj + M.(1-y)
                milp.addLeq(new HashMap<>() {{
                    put(_i, 1.);
                    put(_j, -1.);
                    put(_k, M);
                }}, -d[i] + M);
                // tj + dj <= ti + M.y
                milp.addLeq(new HashMap<>() {{
                    put(_j, 1.);
                    put(_i, -1.);
                    put(_k, -M);
                }}, -d[j]);
            }
        }
        double[] c = new double[28];
        for (int i = 7; i < 28; i++) {
            c[i] = 1.;
        }
        milp.setObjective(true, c);
        LinearProgram.Status status = milp.branchAndBound();
        if (status.equals(LinearProgram.Status.FEASIBLE)) {
            for (int i = 0; i < 7; i++) {
                System.out.printf("task %d starts at %.2f, for %d\n", i, milp.value(i), d[i]);
            }
            for (int k = 7; k < 28; k++) {
                System.out.printf("b_%d %.4f\n", k, milp.value(k));
            }
            System.out.printf("Cost=: %.4f", milp.objective());
        } else {
            System.out.printf("%s\n", status);
        }
    }

}