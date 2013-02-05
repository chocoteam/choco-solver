/**
 * Copyright (c) 1999-2010, Ecole des Mines de Nantes
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
package solver.search.loop;

import choco.kernel.common.util.tools.ArrayUtils;
import org.testng.annotations.Test;
import solver.Solver;
import solver.constraints.IntConstraintFactory;
import solver.search.loop.monitors.cpviz.Visualization;
import solver.search.loop.monitors.cpviz.visualizers.*;
import solver.variables.BoolVar;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 6 déc. 2010
 */
public class CPVizTest {

    private String dir = System.getProperty("user.dir");

    @Test
    public void testNoLog() {
        int n = 4;

        Solver s = new Solver();
        IntVar[] Q = VariableFactory.enumeratedArray("Q", n, 1, n, s);

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                int k = j - i;
                s.post(IntConstraintFactory.arithm(Q[i], "!=", Q[j]));
                s.post(IntConstraintFactory.arithm(Q[i], "!=", Q[j], "+", k));
                s.post(IntConstraintFactory.arithm(Q[i], "!=", Q[j], "-", k));
            }
        }
        s.findAllSolutions();
    }

    @Test
    public void testVector() {
        int n = 4;

        Solver s = new Solver();
        IntVar[] Q = VariableFactory.enumeratedArray("Q", n, 1, n, s);

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                int k = j - i;
                s.post(IntConstraintFactory.arithm(Q[i], "!=", Q[j]));
                s.post(IntConstraintFactory.arithm(Q[i], "!=", Q[j], "+", k));
                s.post(IntConstraintFactory.arithm(Q[i], "!=", Q[j], "-", k));
            }
        }


        Visualization visu = new Visualization("Vector", s, dir + "/out");

        visu.createTree();
        visu.createViz();

        Vector vector = new Vector(Q, "expanded", n, n);
        vector.setMinMax(1, n);

        visu.addVisualizer(vector);

        s.findAllSolutions();


    }

    @Test
    public void testVectorSize() {
        int n = 13;

        Solver s = new Solver();
        IntVar[] Q = VariableFactory.enumeratedArray("Q", n, 1, n, s);

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                int k = j - i;
                s.post(IntConstraintFactory.arithm(Q[i], "!=", Q[j]));
                s.post(IntConstraintFactory.arithm(Q[i], "!=", Q[j], "+", k));
                s.post(IntConstraintFactory.arithm(Q[i], "!=", Q[j], "-", k));
            }
        }


        Visualization visu = new Visualization("VectorSize", s, dir + "/out");

        visu.createTree();
        visu.createViz();

        VectorSize vector = new VectorSize(Q, "expanded", n, n);
        vector.setMinMax(1, n);

        visu.addVisualizer(vector);

        s.solve();


    }

    @Test
    public void testVectorWaterfall() {
        int n = 4;

        Solver s = new Solver();
        IntVar[] Q = VariableFactory.enumeratedArray("Q", n, 1, n, s);

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                int k = j - i;
                s.post(IntConstraintFactory.arithm(Q[i], "!=", Q[j]));
                s.post(IntConstraintFactory.arithm(Q[i], "!=", Q[j], "+", k));
                s.post(IntConstraintFactory.arithm(Q[i], "!=", Q[j], "-", k));
            }
        }


        Visualization visu = new Visualization("VectorWaterfall", s, dir + "/out");

        visu.createTree();
        visu.createViz();

        VectorWaterfall visualizer = new VectorWaterfall(Q, "expanded", n, n);
        visualizer.setMinMax(1, n);

        visu.addVisualizer(visualizer);

        s.findAllSolutions();


    }

    @Test
    public void testAllDifferent() {
        IntVar S, E, N, D, M, O, R, Y;
        IntVar[] SEND, MORE, MONEY;

        Solver solver = new Solver();

        S = VariableFactory.enumerated("S", 0, 9, solver);
        E = VariableFactory.enumerated("E", 0, 9, solver);
        N = VariableFactory.enumerated("N", 0, 9, solver);
        D = VariableFactory.enumerated("D", 0, 9, solver);
        M = VariableFactory.enumerated("M", 0, 9, solver);
        O = VariableFactory.enumerated("0", 0, 9, solver);
        R = VariableFactory.enumerated("R", 0, 9, solver);
        Y = VariableFactory.enumerated("Y", 0, 9, solver);

        solver.post(IntConstraintFactory.arithm(S, "!=", 0));
        solver.post(IntConstraintFactory.arithm(M, "!=", 0));
        solver.post(IntConstraintFactory.arithm(S, "!=", 0));
        solver.post(IntConstraintFactory.arithm(M, "!=", 0));
        solver.post(IntConstraintFactory.alldifferent_bc(new IntVar[]{S, E, N, D, M, O, R, Y}));


        IntVar[] ALL = new IntVar[]{
                S, E, N, D,
                M, O, R, E,
                M, O, N, E, Y};
        int[] COEFFS = new int[]{
                1000, 100, 10, 1,
                1000, 100, 10, 1,
                -10000, -1000, -100, -10, -1
        };
        solver.post(IntConstraintFactory.scalar(ALL, COEFFS, "=", 0));

        Visualization visu = new Visualization("AllDifferent", solver, dir + "/out");

        visu.createTree();
        visu.createViz();

        Vector visualizer = new Vector(ALL, "expanded", 0, 0, 8, 10, "SENDMORY", 0, 9);

        visu.addVisualizer(visualizer);

        solver.solve();


    }

    @Test
    public void testElement() {
        Solver s = new Solver();
        int[] values = new int[]{1, 2, 0, 4, -10};
        IntVar index = VariableFactory.enumerated("index", -3, 10, s);
        IntVar value = VariableFactory.enumerated("value", -20, 20, s);
        s.post(IntConstraintFactory.element(index, values, value));

        Visualization visu = new Visualization("Element", s, dir + "/out");

        visu.createTree();
        visu.createViz();

        solver.search.loop.monitors.cpviz.visualizers.Element visualizer = new solver.search.loop.monitors.cpviz.visualizers.Element(index, values, value, "expanded", 13, 40);
        visualizer.setMinMax(-20, 20);

        visu.addVisualizer(visualizer);
        s.solve();
    }

    @Test
    public void testBinaryVector() {
        Solver s = new Solver();
        IntVar var = VariableFactory.enumerated("var", 1, 8, s);
        BoolVar[] bool = VariableFactory.boolArray("b", 8, s);
        s.post(IntConstraintFactory.channeling(bool, var));

        Visualization visu = new Visualization("BinaryVector", s, dir + "/out");

        visu.createTree();
        visu.createViz();


        BinaryVector visualizer = new BinaryVector(bool, "expanded", 8, 8);
        visualizer.setMinMax(0, 8);

        visu.addVisualizer(visualizer);

        s.solve();


    }

    @Test
    public void testDomainMatrix() {
        Solver s = new Solver();
        int n = 3;
        final int ub = n * n;
        final int ms = n * (n * n + 1) / 2;
        IntVar[][] vars = VariableFactory.enumeratedMatrix("v", n, n, 1, ub, s);
        // All cells of the matrix must be different
        s.post(IntConstraintFactory.alldifferent_bc(ArrayUtils.flatten(vars)));
        final IntVar[] varDiag1 = new IntVar[n];
        final IntVar[] varDiag2 = new IntVar[n];
        for (int i = 0; i < n; i++) {
            // All rows must be equal to the magic sum
            s.post(IntConstraintFactory.sum(vars[i], "=", ms));
            // All columns must be equal to the magic sum
            s.post(IntConstraintFactory.sum(ArrayUtils.getColumn(vars, i), "=", ms));
            //record diagonals variable
            varDiag1[i] = vars[i][i];
            varDiag2[i] = vars[(n - 1) - i][i];
        }
        // Every diagonal have to be equal to the magic sum
        s.post(IntConstraintFactory.sum(varDiag1, "=", ms));
        s.post(IntConstraintFactory.sum(varDiag2, "=", ms));
        //symmetry breaking constraint: enforce that the upper left corner contains the minimum corner value.
        s.post(IntConstraintFactory.arithm(vars[0][0], "<", vars[0][n - 1]));
        s.post(IntConstraintFactory.arithm(vars[0][0], "<", vars[n - 1][n - 1]));
        s.post(IntConstraintFactory.arithm(vars[0][0], "<", vars[n - 1][0]));

        Visualization visu = new Visualization("DomainMatrix", s, dir + "/out");

        visu.createTree();
        visu.createViz();

        DomainMatrix visualizer = new DomainMatrix(vars, "expanded", 3, 3);
        visualizer.setMinMax(1, ub);

        visu.addVisualizer(visualizer);

        s.solve();


    }

    @Test
    public void testAllDifferentMatrix() {
        Solver s = new Solver();
        int n = 3;
        final int ub = n * n;
        final int ms = n * (n * n + 1) / 2;
        IntVar[][] vars = VariableFactory.enumeratedMatrix("v", n, n, 1, ub, s);
        // All cells of the matrix must be different
        s.post(IntConstraintFactory.alldifferent_bc(ArrayUtils.flatten(vars)));
        final IntVar[] varDiag1 = new IntVar[n];
        final IntVar[] varDiag2 = new IntVar[n];
        for (int i = 0; i < n; i++) {
            // All rows must be equal to the magic sum
            s.post(IntConstraintFactory.sum(vars[i], "=", ms));
            // All columns must be equal to the magic sum
            s.post(IntConstraintFactory.sum(ArrayUtils.getColumn(vars, i), "=", ms));
            //record diagonals variable
            varDiag1[i] = vars[i][i];
            varDiag2[i] = vars[(n - 1) - i][i];
        }
        // Every diagonal have to be equal to the magic sum
        s.post(IntConstraintFactory.sum(varDiag1, "=", ms));
        s.post(IntConstraintFactory.sum(varDiag2, "=", ms));
        //symmetry breaking constraint: enforce that the upper left corner contains the minimum corner value.
        s.post(IntConstraintFactory.arithm(vars[0][0], "<", vars[0][n - 1]));
        s.post(IntConstraintFactory.arithm(vars[0][0], "<", vars[n - 1][n - 1]));
        s.post(IntConstraintFactory.arithm(vars[0][0], "<", vars[n - 1][0]));


        Visualization visu = new Visualization("AllDifferentMatrix", s, dir + "/out");

        visu.createTree();
        visu.createViz();

        AllDifferentMatrix visualizer = new AllDifferentMatrix(vars, "expanded", 3, 3);
        visualizer.setMinMax(1, ub);

        visu.addVisualizer(visualizer);

        s.solve();


    }

    @Test
    public void testBinaryMatrix() {
        Solver solver = new Solver();
        int n = 4;
        IntVar[] var = VariableFactory.enumeratedArray("var", n, 0, n - 1, solver);
        BoolVar[][] bool = new BoolVar[n][n];
        for (int i = 0; i < n; i++) {
            bool[i] = VariableFactory.boolArray("bool_" + i, n, solver);
            solver.post(IntConstraintFactory.channeling(bool[i], var[i]));
        }
        solver.post(IntConstraintFactory.alldifferent_bc(var));

        Visualization visu = new Visualization("BinaryMatrix", solver, dir + "/out");

        visu.createTree();
        visu.createViz();


        BinaryMatrix visualizer = new BinaryMatrix(bool, "expanded", n, n);

        visu.addVisualizer(visualizer);

        solver.solve();

    }

    @Test
    public void testBoolChanneling() {
        Solver s = new Solver();
        IntVar var = VariableFactory.enumerated("var", 1, 8, s);
        BoolVar[] bool = VariableFactory.boolArray("b", 8, s);
        s.post(IntConstraintFactory.channeling(bool, var));

        Visualization visu = new Visualization("BoolChanneling", s, dir + "/out");

        visu.createTree();
        visu.createViz();


        BoolChanneling visualizer = new BoolChanneling(var, bool, "expanded", 8, 8);
        visualizer.setMinMax(0, 8);

        visu.addVisualizer(visualizer);

        s.solve();

    }

//    @Test
//    public void testBinPacking() {
//        Solver solver = new Solver();
//        IntVar[] loads = new IntVar[3];
//        loads[0] = VariableFactory.enumerated("load1", 0, 5, solver);
//        loads[1] = VariableFactory.enumerated("load2", 0, 5, solver);
//        loads[2] = VariableFactory.enumerated("load3", 0, 5, solver);
//        IntVar[] bins = VariableFactory.enumerated("bin", 4, 0, 1);
//        int[] _sizes = new int[]{4, 3, 1, 1};
//        IntegerConstantVariable[] sizes = constantArray(_sizes);
//
//        Solver s = new Solver();
//        m.addConstraint(Choco.pack(new PackModel(bins, sizes, loads)));
//
//        Solver solver = new CPSolver();
//        solver.read(m);
//
//        Visualization visu = new Visualization("BinPacking", solver, dir + "/out");
//        visu.createTree();
//        visu.createViz();
//
//        BinPacking visualizer = new BinPacking(solver.getVar(bins), _sizes, solver.getVar(loads), "expanded", 15, 15);
//
//        visu.addVisualizer(visualizer);
//        solver.solve();
//
//
//    }

    @Test
    public void testLexLe() {
        Solver s = new Solver();

        IntVar[] X = VariableFactory.enumeratedArray("X", 3, 0, 1, s);
        IntVar[] Y = VariableFactory.enumeratedArray("Y", 3, 0, 1, s);
        s.post(IntConstraintFactory.lex_less_eq(X, Y));

        Visualization visu = new Visualization("LexLe", s, dir + "/out");
        visu.createTree();
        visu.createViz();

        LexLe visualizer = new LexLe(X, Y, "expanded", 3, 2);

        visu.addVisualizer(visualizer);

        s.solve();
    }

    @Test
    public void testInverse() {
        Solver s = new Solver();
        IntVar[] X = VariableFactory.enumeratedArray("X", 3, 0, 2, s);
        IntVar[] Y = VariableFactory.enumeratedArray("Y", 3, 0, 2, s);

        s.post(IntConstraintFactory.channeling(X, Y));

        Visualization visu = new Visualization("Inverse", s, dir + "/out");
        visu.createTree();
        visu.createViz();

        Inverse visualizer = new Inverse(X, Y, "expanded", 2, 3);

        visu.addVisualizer(visualizer);

        s.solve();


    }

    @Test
    public void testGcc() {
        Solver s = new Solver();
        IntVar[] X = VariableFactory.enumeratedArray("X", 3, 0, 4, s);
        int[] values = new int[]{1, 2, 3};
        int[] low = new int[]{0, 1, 0};
        int[] up = new int[]{1, 2, 1};

        s.post(IntConstraintFactory.global_cardinality_low_up_bc(X, values, low, up, false));

        Visualization visu = new Visualization("Gcc", s, dir + "/out");
        visu.createTree();
        visu.createViz();

        Gcc visualizer = new Gcc(X, values, low, up, "expanded", 30, 30);

        visu.addVisualizer(visualizer);

        s.solve();
    }

    @Test
    public void testCumulative() {
//        Solver s = new Solver();
//        // data
//        int n = 11 + 3; //number of tasks (include the three fake tasks)
//        int[] heights_data = new int[]{2, 1, 4, 2, 3, 1, 5, 6, 2, 1, 3, 1, 1, 2};
//        int[] durations_data = new int[]{1, 1, 1, 2, 1, 3, 1, 1, 3, 4, 2, 3, 1, 1};
//        // variables
//        IntVar[] starts = VariableFactory.boundedArray("start", n, 0, 5, s);
//        IntVar[] ends = VariableFactory.boundedArray("end", n, 0, 6, s);
//        IntVar[] duration = new IntVar[n];
//        IntVar[] height = new IntVar[n];
//        for (int i = 0; i < height.length; i++) {
//            duration[i] = Views.fixed(durations_data[i], s);
//            height[i] = VariableFactory.enumerated("height " + i, new int[]{0, heights_data[i]}, s);
//        }
//        IntVar[] bool = VariableFactory.boolArray("taskIn?", n, s);
//        IntVar obj = VariableFactory.bounded("obj", 0, n, s);
//        //post the cumulative
//        s.post(new solver.constraints.nary.scheduling.Cumulative(starts, duration, ends, height, 7, s));
//        //post the channeling to know if the task is scheduled or not
//        for (int i = 0; i < n; i++) {
//            s.post(new Channel(height[i], bool[i]));
//            m.addConstraint(boolChanneling(bool[i], height[i], heights_data[i]));
//        }
//        //state the objective function
//        m.addConstraint(eq(sum(bool), obj));
//        CPSolver solver = new CPSolver();
//        solver.read(m);
//        //set the fake tasks to establish the profile capacity of the ressource
//        try {
//            solver.getVar(starts[0]).setVal(1);
//            solver.getVar(ends[0]).setVal(2);
//            solver.getVar(height[0]).setVal(2);
//            solver.getVar(starts[1]).setVal(2);
//            solver.getVar(ends[1]).setVal(3);
//            solver.getVar(height[1]).setVal(1);
//            solver.getVar(starts[2]).setVal(3);
//            solver.getVar(ends[2]).setVal(4);
//            solver.getVar(height[2]).setVal(4);
//        } catch (ContradictionException e) {
//            System.out.println("error, no contradiction expected at this stage");
//        }
//        Visualization visu = new Visualization("Cumulative", solver, dir + "/out");
//        visu.createTree();
//        visu.createViz();
//
//        Cumulative visualizer = new Cumulative(solver.getVar(tasks), solver.getVar(capa), solver.getMakespan(), "expanded", 30, 30);
//
//        visu.addVisualizer(visualizer);
//
//        // maximize the number of tasks placed in this profile
////        solver.maximize(solver.getVar(obj), false);
//        solver.solve();
//        System.out.println("Objective : " + (solver.getVar(obj).getVal() - 3));
//        for (int i = 3; i < starts.length; i++) {
//            if (solver.getVar(height[i]).getVal() != 0)
//                System.out.println("[" + solver.getVar(starts[i]).getVal() + " - "
//                        + (solver.getVar(ends[i]).getVal() - 1) + "]:"
//                        + solver.getVar(height[i]).getVal());
//        }

    }
}
