/**
 * Copyright (c) 2015, Ecole des Mines de Nantes
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    This product includes software developed by the <organization>.
 * 4. Neither the name of the <organization> nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.solver.constraints.nary;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.chocosolver.solver.search.strategy.Search.inputOrderLBSearch;
import static org.testng.Assert.*;

/**
 * @author Jean-Guillaume Fages
 */
public class MinMaxTest {

    @DataProvider(name = "params")
    public Object[][] data1D(){
        // first boolean indicates whether it is minimization or maximization
        // second boolean indicates whether to use explanations or not
        List<Object[]> elt = new ArrayList<>();
        elt.add(new Object[]{true,true});
        elt.add(new Object[]{false, true});
        elt.add(new Object[]{true,false});
        elt.add(new Object[]{false, false});
        return elt.toArray(new Object[elt.size()][1]);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test(groups="1s", timeOut=60000, dataProvider = "params")
    public void testNominal(boolean min, boolean exp) throws ContradictionException{
        Model model = new Model();
        IntVar[] vars = model.intVarArray(3, -2, 3);
        IntVar mMvar = model.intVar(-3, 2);
        if(min){
            model.min(mMvar,vars).post();
        }else{
            model.max(mMvar,vars).post();
        }
        if(exp){
            model.getSolver().setCBJLearning(false,false);
        }
        int nbSol = checkSolutions(vars, mMvar, min);

        // compare to scalar+arithm
        model = new Model();
        vars = model.intVarArray(3, -2, 3);
        mMvar = model.intVar(-3,2);
        model.element(mMvar,vars,model.intVar(0,vars.length-1),0).post();
        for(IntVar v:vars){
            model.arithm(v,min?">=":"<=",mMvar).post();
        }
        int nbSol2 = 0;
        model.getSolver().setSearch(inputOrderLBSearch(vars),inputOrderLBSearch(mMvar));
        while (model.getSolver().solve()) {
            nbSol2++;
        }
        assertEquals(nbSol, nbSol2);
    }

    @Test(groups="1s", timeOut=60000, dataProvider = "params")
    public void testBools(boolean min, boolean exp) throws ContradictionException{
        int n1 = 0;
        {
            Model model = new Model();
            BoolVar[] vars = model.boolVarArray(3);
            BoolVar mMvar = model.boolVar();
            if (min) {
                model.min(mMvar, vars).post();
            } else {
                model.max(mMvar, vars).post();
            }
            if(exp){
                model.getSolver().setCBJLearning(false,false);
            }
            n1 = checkSolutions(vars, mMvar, min);
        }
        int n2 = 0;
        {
            Model model = new Model();
            BoolVar[] vars = model.boolVarArray(3);
            BoolVar mMvar = model.boolVar();
            if (min) {
                model.min(mMvar, vars).post();
            } else {
                model.max(mMvar, vars).post();
            }
            n2 = checkSolutions(vars, mMvar, min);
        }
        System.out.println(n1);
        System.out.println(n2);
        assertEquals(n1, n2);
    }

    @Test(groups="1s", timeOut=60000, dataProvider = "params")
    public void testBoolsGG(boolean min, boolean exp) throws ContradictionException{
        {
            Model m = new Model();
            IntVar[] vars = new BoolVar[2];
            for (int t = 0; t < vars.length; t++) {
                vars[t] = m.boolVar("x"+t);
            }
            IntVar mMvar = m.boolVar("max");
            if (min) {
                m.arithm(vars[0],"=",0).post();
                m.arithm(mMvar,"=",0).post();
                m.min(mMvar, vars).post();
            } else {
                m.arithm(vars[0],">",0).post();
                m.arithm(mMvar,">",0).post();
                m.max(mMvar, vars).post();
            }
            if(exp){
                m.getSolver().setCBJLearning(false,false);
            }
            try {
                m.getSolver().propagate();
            } catch (ContradictionException e) {
                e.printStackTrace();
            }
            assertEquals(vars[0].getValue(),min?0:1);
            assertTrue(!vars[1].isInstantiated());
        }
        {
            Model m = new Model();
            BoolVar[] vars = new BoolVar[2];
            for (int t = 0; t < vars.length; t++) {
                vars[t] = m.boolVar("x"+t);
            }
            BoolVar mMvar = m.boolVar("max");
            if (min) {
                m.arithm(vars[0],"=",0).post();
                m.arithm(mMvar,"=",0).post();
                m.min(mMvar, vars).post();
            } else {
                m.arithm(vars[0],">",0).post();
                m.arithm(mMvar,">",0).post();
                m.max(mMvar, vars).post();
            }
            try {
                m.getSolver().propagate();
            } catch (ContradictionException e) {
                e.printStackTrace();
            }
            assertEquals(vars[0].getValue(),min?0:1);
            assertTrue(!vars[1].isInstantiated());
        }
    }

    @Test(groups = "1s", timeOut=60000, dataProvider = "params")
    public void testNoSolution(boolean min, boolean exp) {
        Model model = new Model();
        IntVar[] vars = model.intVarArray(5, 0, 5);
        IntVar mM = model.intVar(26, 30);
        if(min) {
            model.min(mM,vars).post();
        }else{
            model.max(mM,vars).post();
        }
        if(exp){
            model.getSolver().setCBJLearning(false,false);
        }
        assertEquals(model.getSolver().isSatisfied(), ESat.FALSE);
        assertFalse(model.getSolver().solve());
    }

    @Test(groups = "1s", timeOut=60000, dataProvider = "params")
    public void testNoSolution2(boolean min, boolean exp) {
        Model model = new Model();
        IntVar[] vars = model.intVarArray(5, 0, 5);
        IntVar mM = model.intVar(-26, -3);
        if(min) {
            model.min(mM,vars).post();
        }else{
            model.max(mM,vars).post();
        }
        if(exp){
            model.getSolver().setCBJLearning(false,false);
        }
        assertEquals(model.getSolver().isSatisfied(), ESat.FALSE);
        assertFalse(model.getSolver().solve());
    }

    @Test(groups = "1s", timeOut=60000, dataProvider = "params")
    public void testZero(boolean min, boolean exp) {
        Model model = new Model();
        IntVar[] vars = new IntVar[]{
                model.intVar(-5, -1),
                model.intVar(1, 5),
                model.intVar(-5, -1),
                model.intVar(1, 5)
        };
        IntVar mM = model.intVar(0);
        if(min) {
            model.min(mM,vars).post();
        }else{
            model.max(mM,vars).post();
        }
        if(exp){
            model.getSolver().setCBJLearning(false,false);
        }
        checkSolutions(vars, mM, min);
    }


    @Test(groups = "1s", timeOut=60000, dataProvider = "params")
    public void testSameVariableSolution(boolean min, boolean exp) {
        Model model = new Model();
        IntVar mM = model.intVar(1, 5);
        IntVar ref = model.intVar(1, 5);
        IntVar[] vars = new IntVar[]{ref, ref};
        if(min) {
            model.min(mM,vars).post();
        }else{
            model.max(mM,vars).post();
        }
        if(exp){
            model.getSolver().setCBJLearning(false,false);
        }
        checkSolutions(vars, mM, min);
    }

    @Test(groups = "1s", timeOut=60000, dataProvider = "params")
    public void testSameVariableNoSolution(boolean min, boolean exp) {
        Model model = new Model();
        IntVar mM = model.intVar(0);
        IntVar ref = model.intVar(1, 5);
        IntVar[] vars = new IntVar[]{ref, ref};
        if(min) {
            model.min(mM,vars).post();
        }else{
            model.max(mM,vars).post();
        }
        if(exp){
            model.getSolver().setCBJLearning(false,false);
        }
        assertFalse(model.getSolver().solve());
    }


    @Test(groups = "1s", timeOut=60000, dataProvider = "params", expectedExceptions = AssertionError.class)
    public void testZeroElements(boolean min, boolean exp) {
        Model model = new Model();
        IntVar mM = model.intVar(-1,1);
        IntVar[] vars = new IntVar[0];
        if(min) {
            model.min(mM,vars).post();
        }else{
            model.max(mM,vars).post();
        }
        if(exp){
            model.getSolver().setCBJLearning(false,false);
        }
        assertEquals(checkSolutions(vars, mM, min), 1);
    }

    private int checkSolutions(IntVar[] intVars, IntVar sum, boolean minOrMax) {
        Model model = sum.getModel();
        int nbSol = 0;
        while (model.getSolver().solve()) {
            nbSol++;
            int min = Integer.MAX_VALUE;
            int max = Integer.MIN_VALUE;
            for(IntVar v:intVars){
                min = Math.min(min, v.getValue());
                max = Math.max(max, v.getValue());
            }
            int target = minOrMax?min:max;
            assertTrue(target == sum.getValue());
        }
        return nbSol;
    }
}
