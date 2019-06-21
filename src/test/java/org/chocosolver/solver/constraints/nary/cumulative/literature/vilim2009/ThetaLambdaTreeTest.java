/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2019, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.cumulative.literature.vilim2009;

import org.chocosolver.solver.constraints.nary.cumulative.literature.AbstractCumulativeTest;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Task;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 @author Arthur Godet <arth.godet@gmail.com>
 @since 23/05/2019
 */
public class ThetaLambdaTreeTest {

    @Test
    public void buildThetaLambdaTreeTest() {
        int[][] values = new int[][]{
                new int[]{30, Integer.MAX_VALUE/10, 3, 33, Integer.MAX_VALUE/10},
                new int[]{10, Integer.MAX_VALUE/10, 7, 17, Integer.MAX_VALUE/10},
                new int[]{35, Integer.MAX_VALUE/10, 6, 41, Integer.MAX_VALUE/10},
                new int[]{0, Integer.MAX_VALUE/10, 5, 5, Integer.MAX_VALUE/10},
                new int[]{25, Integer.MAX_VALUE/10, 6, 31, Integer.MAX_VALUE/10}
        };

        Model model = new Model();
        Task[] tasks = AbstractCumulativeTest.buildTasks(values, model);
        IntVar[] heights = model.intVarArray(5, 1, 1);
        IntVar capacity = model.intVar(10);

        ThetaLambdaTree tree = new ThetaLambdaTree(tasks, heights,capacity);
        tree.initializeTree(true);

        Assert.assertEquals(tree.root.left.left.left.taskIdx, 3);
        Assert.assertEquals(tree.root.left.left.right.taskIdx, 1);
        Assert.assertEquals(tree.root.left.right.left.taskIdx, 4);
        Assert.assertEquals(tree.root.left.right.right.taskIdx, 0);
        Assert.assertEquals(tree.root.right.right.right.taskIdx, 2);
    }

    @Test
    public void computeValuesThetaLambdaTreeTest() {
        int[][] values = new int[][]{
                new int[]{0, 4, 1, 1, 5}, // A
                new int[]{2, 2, 3, 5, 5}, // B
                new int[]{2, 3, 2, 4, 5}, // C
                new int[]{0, Integer.MAX_VALUE/10, 3, 3, Integer.MAX_VALUE/10} // D
        };
        int[] heights = new int[]{3, 1, 2, 2};

        Model model = new Model();
        Task[] tasks = AbstractCumulativeTest.buildTasks(values, model);
        IntVar[] heightsVar = new IntVar[heights.length];
        for(int i = 0; i<heights.length; i++) {
            heightsVar[i] = model.intVar(heights[i]);
        }
        IntVar capacity = model.intVar(3);

        ThetaLambdaTree tree = new ThetaLambdaTree(tasks, heightsVar, capacity);
        tree.initializeTree(true);

        // Tree is built correctly
        Assert.assertEquals(tree.root.left.left.taskIdx, 0); // A
        Assert.assertEquals(tree.root.left.right.taskIdx, 3); // D
        Assert.assertEquals(tree.root.right.left.taskIdx, 1); // B
        Assert.assertEquals(tree.root.right.right.taskIdx, 2); // C

        ThetaLambdaTree.Node A = tree.root.left.left;
        ThetaLambdaTree.Node B = tree.root.right.left;
        ThetaLambdaTree.Node C = tree.root.right.right;
        ThetaLambdaTree.Node D = tree.root.left.right;

        // Nodes' values are correctly computed
        tree.addToLambdaAndRemoveFromTheta(3); // add D to lambda
        int[][] nodesValues = new int[][]{
                new int[]{3, 3, ThetaLambdaTree.MINF, ThetaLambdaTree.MINF}, // A
                new int[]{3, 9, ThetaLambdaTree.MINF, ThetaLambdaTree.MINF}, // B
                new int[]{4, 10, ThetaLambdaTree.MINF, ThetaLambdaTree.MINF}, // C
                new int[]{0, ThetaLambdaTree.MINF, 6, 6}, // D
                new int[]{3, 3, 9, 9}, // A & D parent
                new int[]{7, 13, ThetaLambdaTree.MINF, ThetaLambdaTree.MINF}, // B and C parent
                new int[]{10, 13, 16, 16} // root
        };

        ThetaLambdaTree.Node[] nodes = new ThetaLambdaTree.Node[]{A, B, C, D, A.parent, B.parent, tree.root};
        for(int i = 0; i<nodesValues.length; i++) {
            Assert.assertEquals(nodesValues[i][0], nodes[i].e);
            Assert.assertEquals(nodesValues[i][1], nodes[i].env);
            Assert.assertEquals(nodesValues[i][2], nodes[i].eLambda);
            Assert.assertEquals(nodesValues[i][3], nodes[i].envLambda);
        }
    }

    @Test
    public void maxestTest() {
        Model model = new Model();
        int[][] values = new int[][]{
                new int[]{0, 1, 6, 6, 7}, // X
                new int[]{6, 6, 1, 7, 7}, // Y
                new int[]{0, Integer.MAX_VALUE/10-6, 6, 6, Integer.MAX_VALUE/10}, // Z
                new int[]{0, 5, 2, 2, 7} // W
        };
        Task[] tasks = AbstractCumulativeTest.buildTasks(values, model);
        int[] heights = new int[]{1, 1, 1, 1};
        IntVar[] heightsVar = model.intVarArray(4, 1, 1);
        IntVar capacity = model.intVar(2);

        ThetaLambdaTree thetaLambdaTree = new ThetaLambdaTree(tasks, heightsVar, capacity);
        thetaLambdaTree.setC(1);
        thetaLambdaTree.initializeTree(false);
        thetaLambdaTree.addToTheta(0);
        thetaLambdaTree.addToTheta(3);
        thetaLambdaTree.addToTheta(1);

        Assert.assertEquals(thetaLambdaTree.root.e, 9);
        Assert.assertEquals(thetaLambdaTree.root.env, 13);
        Assert.assertEquals(thetaLambdaTree.root.envC, 9);

        Assert.assertEquals(thetaLambdaTree.root.left.e, 8);
        Assert.assertEquals(thetaLambdaTree.root.left.env, 8);
        Assert.assertEquals(thetaLambdaTree.root.left.envC, 8);

        Assert.assertEquals(thetaLambdaTree.root.right.e, 1);
        Assert.assertEquals(thetaLambdaTree.root.right.env, 13);
        Assert.assertEquals(thetaLambdaTree.root.right.envC, 7);

        Assert.assertEquals(thetaLambdaTree.maxest(1, 1).taskIdx, 0);
    }
}
