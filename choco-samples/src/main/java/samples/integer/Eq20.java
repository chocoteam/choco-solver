/**
 *  Copyright (c) 1999-2014, Ecole des Mines de Nantes
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
package samples.integer;

import org.slf4j.LoggerFactory;
import samples.AbstractProblem;
import solver.Solver;
import solver.constraints.IntConstraintFactory;
import solver.search.strategy.IntStrategyFactory;
import solver.variables.IntVar;
import solver.variables.VariableFactory;
import util.ESat;

import java.util.Arrays;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 16/03/12
 */
public class Eq20 extends AbstractProblem {

    int n = 7;

    int[][] coeffs = new int[][]{
            {876370, -16105, 62397, -6704, 43340, 95100, -68610, 58301},
            {533909, 51637, 67761, 95951, 3834, -96722, 59190, 15280},
            {915683, 1671, -34121, 10763, 80609, 42532, 93520, -33488},
            {129768, 71202, -11119, 73017, -38875, -14413, -29234, 72370},
            {752447, 8874, -58412, 73947, 17147, 62335, 16005, 8632},
            {90614, 85268, 54180, -18810, -48219, 6013, 78169, -79785},
            {1198280, -45086, 51830, -4578, 96120, 21231, 97919, 65651},
            {18465, -64919, 80460, 90840, -59624, -75542, 25145, -47935},
            {1503588, -43277, 43525, 92298, 58630, 92590, -9372, -60227},
            {1244857, -16835, 47385, 97715, -12640, 69028, 76212, -81102},
            {1410723, -60301, 31227, 93951, 73889, 81526, -72702, 68026},
            {25334, 94016, -82071, 35961, 66597, -30705, -44404, -38304},
            {277271, -67456, 84750, -51553, 21239, 81675, -99395, -4254},
            {249912, -85698, 29958, 57308, 48789, -78219, 4657, 34539},
            {373854, 85176, -95332, -1268, 57898, 15883, 50547, 83287},
            {740061, -10343, 87758, -11782, 19346, 70072, -36991, 44529},
            {146074, 49149, 52871, -7132, 56728, -33576, -49530, -62089},
            {251591, -60113, 29475, 34421, -76870, 62646, 29278, -15212},
            {22167, 87059, -29101, -5513, -21219, 22128, 7276, 57308},
            {821228, -76706, 98205, 23445, 67921, 24111, -48614, -41906}
    };

    IntVar[] vars;

    @Override
    public void createSolver() {
        solver = new Solver("Eq20");
    }

    @Override
    public void buildModel() {
        vars = VariableFactory.boundedArray("v", n, 0, 10, solver);
        for (int i = 0; i < coeffs.length; i++) {
            solver.post(IntConstraintFactory.scalar(vars, Arrays.copyOfRange(coeffs[i], 1, n + 1), VariableFactory.fixed(coeffs[i][0], solver)));
        }
    }

    @Override
    public void configureSearch() {
        solver.set(IntStrategyFactory.minDom_LB(vars));
    }

    @Override
    public void solve() {
        solver.findSolution();
    }

    @Override
    public void prettyOut() {
        LoggerFactory.getLogger("bench").info("20 equations");
        StringBuilder st = new StringBuilder();
        if (solver.isFeasible() == ESat.TRUE) {
            for (int i = 0; i < n; i++) {
                st.append(vars[i].getValue()).append(", ");
            }
        } else {
            st.append("\tINFEASIBLE");
        }
        LoggerFactory.getLogger("bench").info(st.toString());
    }

    public static void main(String[] args) {
        new Eq20().execute(args);
    }
}
