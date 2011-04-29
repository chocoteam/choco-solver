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

package choco;

import org.testng.Assert;
import org.testng.annotations.Test;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.nary.AllDifferent;
import solver.search.strategy.StrategyFactory;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

/**
 * Created by IntelliJ IDEA.
 * User: xlorca
 *
 * TODO: ne marche pas depuis au moins r161
 */
public class TestProbaAllDiff {

    public static long basicAllDiff(int size) {
        Solver s = new Solver();
        IntVar[] vars = new IntVar[size];
        for (int i = 0; i < size ; i++) {
            vars[i] = VariableFactory.enumerated("v_"+i, 1,size, s);
        }
        //vars[0] = ls.createEnumIntVar("x"+0, 2,2);
        //vars[1] = ls.createEnumIntVar("x"+1, 3,3);
        //vars[0] = ls.createEnumIntVar("x"+0, 3,3);
        Constraint[] cstrs = new Constraint[]{new AllDifferent(vars, s)};

        s.post(cstrs);
        s.set(StrategyFactory.preset(vars, s.getEnvironment()));
        long t = System.currentTimeMillis();
        s.findAllSolutions();
        long nbSol = s.getMeasures().getSolutionCount();
        System.out.println("nb solutions : " + nbSol);
        t = System.currentTimeMillis() - t;
        System.out.println("time : " + t);

        return  nbSol;
    }

    public static long probaAllDiff(int size, double threshold) {
        Solver s = new Solver();
        IntVar[] vars = new IntVar[size];
        for (int i = 0; i < size ; i++) {
            vars[i] = VariableFactory.enumerated("v_"+i, 1,size, s);
        }
        //vars[0] = ls.createEnumIntVar("x"+0, 2,2);
        //vars[1] = ls.createEnumIntVar("x"+1, 3,3);
        //vars[0] = ls.createEnumIntVar("x"+0, 3,3);

        AllDifferent cstr = new AllDifferent(vars, s, AllDifferent.Type.PROBABILISTIC);
        Constraint[] cstrs = new Constraint[]{cstr};
        //cstr.setThreshold(threshold);

        s.post(cstrs);
        s.set(StrategyFactory.preset(vars, s.getEnvironment()));
        long t = System.currentTimeMillis();
        s.findAllSolutions();
        long nbSol = s.getMeasures().getSolutionCount();
        System.out.println("nb solutions : " + nbSol);
        t = System.currentTimeMillis() - t;
        System.out.println("time : " + t);

        return  nbSol;
    }



    @Test(groups = "1s")
    public void test(){
        int size = 6;
        double threshold = 1.1;
        long x = basicAllDiff(size);
        System.out.println("\n");
        long y = probaAllDiff(size,threshold);
        Assert.assertEquals(x,y);
    }


}
