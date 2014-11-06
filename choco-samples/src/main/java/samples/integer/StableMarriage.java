package samples.integer; /**
 *
 * Stable marriage problem in Choco3
 *
 * Translation of the OPL version from
 * Pascal Van Hentenryck "The OPL Optimization Programming Language"
 * E.g.
 * http://www.comp.rgu.ac.uk/staff/ha/ZCSP/additional_problems/stable_marriage/stable_marriage.pdf
 *
 * Choco3 model by Hakan Kjellerstrand (hakank@gmail.com)
 * http://www.hakank.org/choco3/
 *
 */

import org.kohsuke.args4j.Option;
import samples.AbstractProblem;
import solver.Solver;
import solver.constraints.LogicalConstraintFactory;
import solver.constraints.SatFactory;
import solver.constraints.nary.cnf.LogOp;
import solver.search.strategy.IntStrategyFactory;
import solver.variables.BoolVar;
import solver.variables.IntVar;
import util.ESat;
import util.tools.ArrayUtils;

import static solver.constraints.IntConstraintFactory.*;
import static solver.variables.VariableFactory.*;

public class StableMarriage extends AbstractProblem {

    @Option(name = "-problem", usage = "Problem instance (default 1.", required = false)
    int problem = 1;


    //
    // From Pascal Van Hentenryck's OPL book
    //
    int[][][] van_hentenryck = {
            // rankWomen
            {{1, 2, 4, 3, 5},
                    {3, 5, 1, 2, 4},
                    {5, 4, 2, 1, 3},
                    {1, 3, 5, 4, 2},
                    {4, 2, 3, 5, 1}},

            // rankMen
            {{5, 1, 2, 4, 3},
                    {4, 1, 3, 2, 5},
                    {5, 3, 2, 4, 1},
                    {1, 5, 4, 3, 2},
                    {4, 3, 2, 1, 5}}
    };


    //
    // Data from MathWorld
    // http://mathworld.wolfram.com/StableMarriageProblem.html
    //
    int[][][] mathworld = {
            // rankWomen
            {{3, 1, 5, 2, 8, 7, 6, 9, 4},
                    {9, 4, 8, 1, 7, 6, 3, 2, 5},
                    {3, 1, 8, 9, 5, 4, 2, 6, 7},
                    {8, 7, 5, 3, 2, 6, 4, 9, 1},
                    {6, 9, 2, 5, 1, 4, 7, 3, 8},
                    {2, 4, 5, 1, 6, 8, 3, 9, 7},
                    {9, 3, 8, 2, 7, 5, 4, 6, 1},
                    {6, 3, 2, 1, 8, 4, 5, 9, 7},
                    {8, 2, 6, 4, 9, 1, 3, 7, 5}},

            // rankMen
            {{7, 3, 8, 9, 6, 4, 2, 1, 5},
                    {5, 4, 8, 3, 1, 2, 6, 7, 9},
                    {4, 8, 3, 9, 7, 5, 6, 1, 2},
                    {9, 7, 4, 2, 5, 8, 3, 1, 6},
                    {2, 6, 4, 9, 8, 7, 5, 1, 3},
                    {2, 7, 8, 6, 5, 3, 4, 1, 9},
                    {1, 6, 2, 3, 8, 5, 4, 9, 7},
                    {5, 6, 9, 1, 2, 8, 4, 3, 7},
                    {6, 1, 4, 7, 5, 8, 3, 9, 2}}};

    //
    // Data from
    // http://www.csee.wvu.edu/~ksmani/courses/fa01/random/lecnotes/lecture5.pdf
    //
    int[][][] problem3 = {
            // rankWomen
            {{1, 2, 3, 4},
                    {4, 3, 2, 1},
                    {1, 2, 3, 4},
                    {3, 4, 1, 2}},

            // rankMen"
            {{1, 2, 3, 4},
                    {2, 1, 3, 4},
                    {1, 4, 3, 2},
                    {4, 3, 1, 2}}};


    //
    // Data from
    // http://www.comp.rgu.ac.uk/staff/ha/ZCSP/additional_problems/stable_marriage/stable_marriage.pdf
    // page 4
    //
    int[][][] problem4 = {
            // rankWomen
            {{1, 5, 4, 6, 2, 3},
                    {4, 1, 5, 2, 6, 3},
                    {6, 4, 2, 1, 5, 3},
                    {1, 5, 2, 4, 3, 6},
                    {4, 2, 1, 5, 6, 3},
                    {2, 6, 3, 5, 1, 4}},

            // rankMen
            {{1, 4, 2, 5, 6, 3},
                    {3, 4, 6, 1, 5, 2},
                    {1, 6, 4, 2, 3, 5},
                    {6, 5, 3, 4, 2, 1},
                    {3, 1, 2, 4, 5, 6},
                    {2, 3, 1, 6, 5, 4}}};


    int[][][] ranks;
    int n;

    IntVar[] wife;
    IntVar[] husband;


    @Override
    public void buildModel() {

        //
        // data
        //
        System.out.println("\n#####################");
        System.out.println("Problem: " + problem);

        switch (problem) {
            case 1:
                ranks = van_hentenryck;
                break;
            case 2:
                ranks = mathworld;
                break;
            case 3:
                ranks = problem3;
                break;
            case 4:
                ranks = problem4;
                break;
        }

        int[][] rankWomen = ranks[0];
        int[][] rankMen = ranks[1];

        n = rankWomen.length;

        //
        // variables
        //
        wife = enumeratedArray("wife", n, 0, n - 1, solver);
        husband = enumeratedArray("husband", n, 0, n - 1, solver);

        solver.post(alldifferent(wife, "BC"));
        solver.post(alldifferent(husband, "BC"));


        //
        // (the comments are the Comet code)
        //   forall(m in Men)
        //      cp.post(husband[wife[m]] == m);
        for (int m = 0; m < n; m++) {
      /*
      solver.addConstraint(
          solver.makeEquality(solver.makeElement(husband, wife[m]), m));
      */
            solver.post(element(fixed(m, solver), husband, wife[m], 0));
        }

        //   forall(w in Women)
        //     cp.post(wife[husband[w]] == w);
        for (int w = 0; w < n; w++) {
      /*
      solver.addConstraint(
          solver.makeEquality(solver.makeElement(wife, husband[w]), w));
      */
            solver.post(element(fixed(w, solver), wife, husband[w], 0));
        }


        // VALUE = TABLE[INDEX-OFFSET]
        // element(IntVar VALUE, IntVar[] TABLE, IntVar INDEX, int OFFSET)
        // element(IntVar VALUE, int[] TABLE, IntVar INDEX, int OFFSET, String SORT)

        //   forall(m in Men, o in Women)
        //       cp.post(rankMen[m,o] < rankMen[m, wife[m]] =>
        //               rankWomen[o,husband[o]] < rankWomen[o,m]);
        for (int m = 0; m < n; m++) {
            for (int o = 0; o < n; o++) {
        /*
        IntVar b1 = solver.makeIsGreaterCstVar(
                        solver.makeElement(rankMen[m], wife[m]).var(),
                        rankMen[m][o]);
        */
                IntVar v1 = enumerated("v1", 0, n - 1, solver);
                solver.post(element(v1, rankMen[m], wife[m], 0, "detect"));

                BoolVar b1 = bool("b1", solver);
                LogicalConstraintFactory.ifThenElse(b1,
                        arithm(v1, ">", rankMen[m][o]),
                        arithm(v1, "<=", rankMen[m][o])
                );


        /*
        IntVar b2 = solver.makeIsLessCstVar(
                        solver.makeElement(rankWomen[o], husband[o]).var(),
                        rankWomen[o][m]);
        */
                IntVar v2 = enumerated("v2", 0, n - 1, solver);
                solver.post(element(v2, rankWomen[o], husband[o], 0, "detect"));

                BoolVar b2 = bool("b2", solver);
                LogicalConstraintFactory.ifThenElse(b2,
                        arithm(v2, "<", rankWomen[o][m]),
                        arithm(v2, ">=", rankWomen[o][m])
                );


                    
        /*
        solver.addConstraint(
            solver.makeLessOrEqual(
                solver.makeDifference(b1, b2), 0));
        */

                // b1 -> b2
                LogOp t = LogOp.implies(b1, b2);
                SatFactory.addClauses(t, solver);

                // solver.post(IntConstraintFactory.arithm(b1, "<=", b2));


            }
        }

        //   forall(w in Women, o in Men)
        //      cp.post(rankWomen[w,o] < rankWomen[w,husband[w]] =>
        //              rankMen[o,wife[o]] < rankMen[o,w]);
        for (int w = 0; w < n; w++) {
            for (int o = 0; o < n; o++) {
        /*
        IntVar b1 = solver.makeIsGreaterCstVar(
                        solver.makeElement(rankWomen[w], husband[w]).var(),
                        rankWomen[w][o]);
        */
                IntVar v1 = enumerated("v1", 0, n - 1, solver);
                solver.post(element(v1, rankWomen[w], husband[w], 0, "detect"));

                BoolVar b1 = bool("b1", solver);
                LogicalConstraintFactory.ifThenElse(b1,
                        arithm(v1, ">", rankWomen[w][o]),
                        arithm(v1, "<=", rankWomen[w][o])
                );


        /*
        IntVar b2 = solver.makeIsLessCstVar(
                        solver.makeElement(rankMen[o], wife[o]).var(),
                        rankMen[o][w]);
        */
                IntVar v2 = enumerated("v2", 0, n - 1, solver);
                solver.post(element(v2, rankMen[o], wife[o], 0, "detect"));

                BoolVar b2 = bool("b2", solver);
                LogicalConstraintFactory.ifThenElse(b2,
                        arithm(v2, "<", rankMen[o][w]),
                        arithm(v2, ">=", rankMen[o][w])
                );


        /*
        solver.addConstraint(
            solver.makeLessOrEqual(
                solver.makeDifference(b1, b2), 0));
        }
        */

                // b1 -> b2
                LogOp t = LogOp.implies(b1, b2);
                SatFactory.addClauses(t, solver);

                // solver.post(IntConstraintFactory.arithm(b1, "<=", b2));

            }

        }

    }

    @Override
    public void createSolver() {
        solver = new Solver("StableMarriage");
    }

    @Override
    public void configureSearch() {
        solver.set(IntStrategyFactory.minDom_LB(ArrayUtils.append(husband, wife)));
    }

    @Override
    public void solve() {
        solver.findSolution();
    }

    @Override
    public void prettyOut() {

        if (solver.isFeasible() == ESat.TRUE) {
            int num_sols = 0;
            do {

                System.out.print("wife   : ");
                for (int i = 0; i < n; i++) {
                    System.out.print(wife[i].getValue() + " ");
                }
                System.out.println();
                System.out.print("husband: ");
                for (int i = 0; i < n; i++) {
                    System.out.print(husband[i].getValue() + " ");
                }
                System.out.println();

                num_sols++;

            } while (solver.nextSolution() == Boolean.TRUE);

            System.out.println("It was " + num_sols + " solutions.");

        } else {

            System.out.println("Problem is not feasible.");

        }


    }

  /*
wife   :  [1, 0, 4, 2, 3]
husband:  [1, 0, 3, 4, 2]

wife   :  [1, 2, 4, 0, 3]
husband:  [3, 0, 1, 4, 2]

wife   :  [3, 0, 1, 2, 4]
husband:  [1, 2, 3, 0, 4]

   */

    //
    // main
    //
    public static void main(String args[]) {

        new StableMarriage().execute(args);

    }
}

