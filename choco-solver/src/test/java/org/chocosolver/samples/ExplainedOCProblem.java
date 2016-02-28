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
package org.chocosolver.samples;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.search.measure.IMeasures;
import org.chocosolver.solver.trace.Chatterbox;
import org.chocosolver.solver.variables.IntVar;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.chocosolver.solver.search.strategy.SearchStrategyFactory.inputOrderLBSearch;

/**
 * Created by IntelliJ IDEA.
 * User: njussien
 * Date: 01/05/11
 * Time: 13:26
 */
public class ExplainedOCProblem extends AbstractProblem {

    IntVar[] vars;
    int n = 7;
    int vals = n - 1;

    @Override
    public void buildModel() {
        model = new Model();
        vars = model.intVarArray("x", 2 * n, 1, vals, false);
        for (int i = 0; i < n - 1; i++) {
            for (int j = i + 1; j < n; j++) {
                model.arithm(vars[2 * i], "!=", vars[2 * j]).post();
            }
        }
    }

    @Override
    public void configureSearch() {
        model.getSolver().set(inputOrderLBSearch(vars));
        model.getSolver().setCBJLearning(false, false);
    }

    @Override
    public void solve() {
        model.solve();
        Chatterbox.printStatistics(model);
    }

    @Test(groups = "10s", timeOut = 60000)
   	public void test(){
        ExplainedOCProblem p1 = new ExplainedOCProblem();
        p1.execute();
        IMeasures m1 = p1.getModel().getSolver().getMeasures();
        Assert.assertEquals(m1.getSolutionCount(),0);
        Assert.assertEquals(m1.getNodeCount(),1235);
    }

    @Test(groups = "10s", timeOut = 60000)
   	public void comparisonTest(){
        ExplainedOCProblem p1 = new ExplainedOCProblem();
        ExplainedOCProblem p2 = new ExplainedOCProblem();
        p1.buildModel();
        p2.buildModel();
        p1.configureSearch();
        p1.getModel().getSolver().setNoLearning();
        p2.configureSearch();
        p1.solve();
        p2.solve();
        IMeasures m1 = p1.getModel().getSolver().getMeasures();
        IMeasures m2 = p2.getModel().getSolver().getMeasures();
        Assert.assertEquals(m1.getSolutionCount(),0);
        Assert.assertEquals(m2.getSolutionCount(),0);
        Assert.assertTrue(m1.getFailCount()>m2.getFailCount());
    }
}
