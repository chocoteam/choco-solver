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
package samples.integer;

import samples.AbstractProblem;
import solver.ResolutionPolicy;
import solver.Solver;
import solver.constraints.IntConstraintFactory;
import solver.constraints.LogicalConstraintFactory;
import solver.variables.BoolVar;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

/**
 * A curriculum is a set of courses with prerequisites.
 * <p/>
 * Each course must be assigned within a set number of periods.
 * <p/>
 * A course cannot be scheduled before its prerequisites.
 * <p/>
 * Each course confers a number of academic credits (it's "load").
 * <p/>
 * Students have lower and upper bounds on the number of credits
 * they can study for in a given period.
 * <p/>
 * Students have lower and upper bounds on the number of courses
 * they can study for in a given period.
 * <p/>
 * The goal is to assign a period to every course satisfying these
 * criteria, minimising the load for all periods.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 20/07/12
 */
public class BACP extends AbstractProblem {

    int n_courses = 50;
    int n_periods = 10;
    int load_per_period_lb = 2;
    int load_per_period_ub = 100;
    int courses_per_period_lb = 2;
    int courses_per_period_ub = 10;
    int[] course_load =
            {6, 3, 5, 3, 7, 8, 1, 9,
                    4, 9, 8, 8, 4, 5,
                    6, 3, 2, 1, 3, 1,
                    1, 2, 6, 7, 6, 10,
                    10, 1, 7, 3, 4, 2, 7,
                    9, 7, 4, 6, 7, 2, 2,
                    5, 9, 9, 10, 4, 6, 4,
                    5, 6, 6};

    IntVar[] course_period;
    BoolVar[][] x;
    IntVar[] load;
    IntVar objective;


    @Override
    public void createSolver() {
        solver = new Solver("BACP");
    }

    @Override
    public void buildModel() {
        // period is assigned to
        course_period = VariableFactory.enumeratedArray("c_p", n_courses, 0, n_periods, solver);
        // whether period i has a course j assigned
        x = VariableFactory.boolMatrix("X", n_periods, n_courses, solver);
        // total load for each period
        load = VariableFactory.enumeratedArray("load", n_periods, 0, load_per_period_ub - load_per_period_lb + 1, solver);
        // opt. target
        objective = VariableFactory.bounded("objective", load_per_period_lb, load_per_period_ub, solver);
        // sum variable
        IntVar sum = VariableFactory.bounded("courses_per_period", courses_per_period_lb, courses_per_period_ub, solver);
        // constraints
        for (int i = 0; i < n_periods; i++) {
            //forall(c in courses) (x[p,c] = bool2int(course_period[c] = p)) /\
            for (int j = 0; j < n_courses; j++) {
                solver.post(
                        LogicalConstraintFactory.ifThenElse(x[i][j],
								IntConstraintFactory.arithm(course_period[j], "=", i),
								IntConstraintFactory.arithm(course_period[j], "!=", i))
                );
            }
            // sum(i in courses) (x[p, i])>=courses_per_period_lb /\
            // sum(i in courses) (x[p, i])<=courses_per_period_ub /\
            solver.post(IntConstraintFactory.sum(x[i], sum));
            //  load[p] = sum(c in courses) (x[p, c]*course_load[c])/\
            solver.post(IntConstraintFactory.scalar(x[i], course_load, load[i]));
            //  load[p] >= load_per_period_lb /\
            solver.post(IntConstraintFactory.arithm(load[i], ">=", load_per_period_lb));
            //  load[p] <= objective
            solver.post(IntConstraintFactory.arithm(load[i], "<=", objective));
        }

        prerequisite(3, 1);
        prerequisite(4, 1);
        prerequisite(5, 1);
        prerequisite(6, 1);
        prerequisite(7, 1);
        prerequisite(6, 2);
        prerequisite(8, 2);
        prerequisite(11, 3);
        prerequisite(11, 4);
        prerequisite(16, 4);
        prerequisite(16, 5);
        prerequisite(11, 6);
        prerequisite(14, 6);
        prerequisite(16, 8);
        prerequisite(13, 9);
        prerequisite(14, 9);
        prerequisite(17, 11);
        prerequisite(19, 11);
        prerequisite(17, 12);
        prerequisite(19, 12);
        prerequisite(18, 13);
        prerequisite(17, 14);
        prerequisite(18, 14);
        prerequisite(23, 17);
        prerequisite(21, 19);
        prerequisite(26, 21);
        prerequisite(27, 21);
        prerequisite(30, 22);
        prerequisite(24, 23);
        prerequisite(25, 23);
        prerequisite(27, 23);
        prerequisite(33, 25);
        prerequisite(34, 27);
        prerequisite(35, 27);
        prerequisite(35, 28);
        prerequisite(33, 29);
        prerequisite(34, 29);
        prerequisite(35, 30);
        prerequisite(36, 31);
        prerequisite(38, 31);
        prerequisite(39, 31);
        prerequisite(40, 31);
        prerequisite(43, 31);
        prerequisite(40, 32);
        prerequisite(37, 33);
        prerequisite(38, 33);
        prerequisite(40, 33);
        prerequisite(38, 34);
        prerequisite(41, 34);
        prerequisite(41, 35);
        prerequisite(42, 35);
        prerequisite(44, 36);
        prerequisite(45, 36);
        prerequisite(45, 37);
        prerequisite(44, 40);
        prerequisite(45, 40);
        prerequisite(47, 40);
        prerequisite(44, 41);
        prerequisite(45, 41);
        prerequisite(46, 41);
        prerequisite(46, 42);
        prerequisite(47, 42);
        prerequisite(48, 47);
        prerequisite(44, 43);
        prerequisite(45, 43);
        prerequisite(49, 46);
        prerequisite(50, 47);
    }

    private void prerequisite(int a, int b) {
        solver.post(IntConstraintFactory.arithm(course_period[b - 1], "<", course_period[a - 1]));
    }


    @Override
    public void configureSearch() {
    }

    @Override
    public void solve() {
        solver.findOptimalSolution(ResolutionPolicy.MINIMIZE, objective);
    }

    @Override
    public void prettyOut() {
    }

    public static void main(String[] args) {
        new BACP().execute(args);
    }
}
