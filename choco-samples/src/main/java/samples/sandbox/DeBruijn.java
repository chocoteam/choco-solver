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

/**
 * Created by IntelliJ IDEA.
 * User: Jean-Guillaume Fages
 * Date: 29/08/12
 * Time: 14:28
 */

package samples.sandbox;

import solver.Solver;
import solver.constraints.IntConstraintFactory;
import solver.variables.BoolVar;
import solver.variables.IntVar;
import solver.variables.VariableFactory;
import util.ESat;
import util.tools.ArrayUtils;

public class DeBruijn {


//
    // Note: The names is the same as the MiniZinc model for
    // easy comparison.
    //

    // These parameters may be set by the user:
    //  - base
    //  - n
    //  - m
    int base;          // the base to use. Also known as k.
    int n;             // number of bits representing the numbers
    int m;             // length of the sequence, defaults to m = base^n
    int num_solutions; // number of solutions to show, default all

    // The constraint variables
    Solver s;
    IntVar[] x;        // the decimal numbers
    BoolVar[][] binary; // the representation of numbers in x, in the choosen base
    IntVar[] bin_code; // the de Bruijn sequence (first number in binary)


    // integer power method
    static int pow(int x, int y) {
        int z = x;
        for (int i = 1; i < y; i++) z *= x;
        return z;
    } // end pow


    //
    // the model
    //
    public void model(int in_base, int in_n, int in_m, int in_num_solutions) {
        s = new Solver();
        base = in_base;
        n = in_n;
        int pow_base_n = pow(base, n); // base^n, the range of integers
        m = pow_base_n;
        if (in_m > 0) {
            if (in_m > m) {
                System.out.println("m must be <= base^n (" + m + ")");
                System.exit(1);
            }
            m = in_m;
        }
        num_solutions = in_num_solutions;
        System.out.println("Using base: " + base + " n: " + n + " m: " + m);

        // decimal representation, ranges from 0..base^n-1
        System.out.println(s);
        x = VariableFactory.boundedArray("x", m, 0, pow_base_n - 1, s);


        //
        // convert between decimal number in x[i] and "base-ary" numbers
        // in binary[i][0..n-1].
        //
        // (This corresponds to the predicate toNum in the MiniZinc model)
        //

        // calculate the weights array
        int[] weights = new int[n];
        int[] coefs = new int[n + 1];
        int w = 1;
        for (int i = 0; i < n; i++) {
            weights[n - i - 1] = w;
            coefs[n - i - 1] = w;
            w *= base;
        }
        coefs[n] = -1;

        // connect binary <-> x
        binary = new BoolVar[m][n];
        for (int i = 0; i < m; i++) {
            binary[i] = VariableFactory.boolArray("binary" + i, n, s);
            IntVar[] sum = ArrayUtils.append(binary[i], new IntVar[]{x[i]});
            s.post(IntConstraintFactory.scalar(sum, coefs, VariableFactory.fixed(0, s)));
//            s.post(ConstraintFactory.eq(x[i], Sum.build(binary[i], weights)));
        }

        //
        // assert the the deBruijn property:  element i in binary starts
        // with the end of element i-1
        //
        for (int i = 1; i < m; i++) {
            for (int j = 1; j < n; j++) {
                s.post(IntConstraintFactory.arithm(binary[i - 1][j], "=", binary[i][j - 1]));
            }
        }

        // ... "around the corner": last element is connected to the first
        for (int j = 1; j < n; j++) {
            s.post(IntConstraintFactory.arithm(binary[m - 1][j], "=", binary[0][j - 1]));
        }


        //
        // This is the de Bruijn sequence, i.e.
        // the first element of of each row in binary[i]
        //
        bin_code = new IntVar[m];
        for (int i = 0; i < m; i++) {
            bin_code[i] = VariableFactory.bounded("bin_code_" + i, 0, base - 1, s);
            s.post(IntConstraintFactory.arithm(bin_code[i], "=", binary[i][0]));
        }


        // All values in x should be different
        s.post(IntConstraintFactory.alldifferent(x, "BC"));

        // Symmetry breaking: the minimum value in x should be the
        // first element.
        //TODO model.addConstraint(min(x, x[0]));

    } // end model


    //
    // Search
    //
    public void search() {
        s.findSolution();

        // System.out.println(s.pretty());
        if (s.isFeasible() == ESat.TRUE) {

            int num_sols = 0;
            // ChocoUtils.printAllSolutions(model, s);

            do {

                System.out.print("\ndecimal values: ");
                for (int i = 0; i < m; i++) {
                    System.out.print(x[i].getValue() + " ");
//                    System.out.print(s.getVar(x[i]).getVal() + " ");
                }

                System.out.print("\nde Bruijn sequence: ");
                for (int i = 0; i < m; i++) {
                    System.out.print(bin_code[i].getValue() + " ");
//                    System.out.print(s.getVar(bin_code[i]).getVal() + " ");
                }


                System.out.println("\nbinary:");

                for (int i = 0; i < m; i++) {
                    for (int j = 0; j < n; j++) {
                        System.out.print(binary[i][j].getValue() + " ");
//                        System.out.print(s.getVar(binary[i][j]).getVal() + " ");
                    }
                    System.out.println(" : " + x[i].getValue());
//                    System.out.println(" : " + s.getVar(x[i]).getVal());
                }


                System.out.println("");
                num_sols++;
                if (num_solutions > 0 && num_sols >= num_solutions) {
                    break;
                }

            } while (s.nextSolution() == Boolean.TRUE);


            /*
            System.out.print("decimal values: ");
            for(int i = 0; i < m; i++) {
                System.out.print(s.getVar(x[i]).getVal() + " ");
            }
            System.out.println();

            System.out.println("\nbinary:");

            for(int i = 0; i < m; i++) {
                for(int j = 0; j < n; j++) {
                    System.out.print(s.getVar(binary[i][j]).getVal() + " ");
                }
                System.out.println(" : " + s.getVar(x[i]).getVal());
            }
            */

            System.out.println("nbSol: " + s.getMeasures().getSolutionCount());

        } else {
            System.out.println("No solutions.");
        }// end if result


    }

    //
    // Running the program
    //  * java DeBruijn base n
    //  * java DeBruijn base n m
    //  * java DeBruijn base n m num_solutions
    //
    public static void main(String args[]) {

        int base = 2;
        int n = 3;
        int m = 0;
        int num_solutions = 0;

        if (args.length >= 4) {
            num_solutions = Integer.parseInt(args[3]);
        }

        if (args.length >= 3) {
            m = Integer.parseInt(args[2]);
        }
        if (args.length >= 2) {
            base = Integer.parseInt(args[0]);
            n = Integer.parseInt(args[1]);
        }

        DeBruijn debruijn = new DeBruijn();
        debruijn.model(base, n, m, num_solutions);
        debruijn.search();

    } // end main

}