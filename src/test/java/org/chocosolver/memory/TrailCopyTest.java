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
package org.chocosolver.memory;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.search.strategy.SearchStrategyFactory;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.objects.setDataStructures.SetFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.chocosolver.util.tools.StringUtils.randomName;

/**
 * @author Jean-Guillaume Fages
 */
public class TrailCopyTest {

    @Test(groups="10s", timeOut=60000)
    public void testCostas() {
        System.out.println("Costas test");
        int n = 10;
        Model copy = makeCostasArrays(n,Environments.COPY.make(),"copy");
        Model trail = makeCostasArrays(n,Environments.TRAIL.make(),"trail");
        testModels(copy,trail);
    }

    @Test(groups="10s", timeOut=60000)
    public void testCount() {
        System.out.println("Count test");
        int n = 16;
        Model copy = makeCountPb(n,Environments.COPY.make(),"copy");
        Model trail = makeCountPb(n,Environments.TRAIL.make(),"trail");
        SetFactory.HARD_CODED = false;
        Model copy2 = makeCountPb(n,Environments.COPY.make(),"copy generic");
        Model trail2 = makeCountPb(n,Environments.TRAIL.make(),"trail generic");
        testModels(copy,trail,copy2,trail2);
    }

    @Test(groups="10s", timeOut=60000)
    public void testDiffN() {
        System.out.println("DiffN test");
        int n = 4;
        Model copy = makeDiffN(n,Environments.COPY.make(),"copy");
        Model trail = makeDiffN(n,Environments.TRAIL.make(),"trail");
        SetFactory.HARD_CODED = false;
        Model copy2 = makeDiffN(n,Environments.COPY.make(),"copy generic");
        Model trail2 = makeDiffN(n,Environments.TRAIL.make(),"trail generic");
        testModels(copy,trail,copy2,trail2);
    }

    @Test(groups="10s", timeOut=60000)
    public void testTrailBest() {
        System.out.println("Trail best test");
        int n = 9;
        Model copy = makeTrailBest(n,Environments.COPY.make(),"copy");
        Model trail = makeTrailBest(n,Environments.TRAIL.make(),"trail");
        testModels(copy,trail);
    }

    @Test(groups="10s", timeOut=60000)
    public void testCopyBest() {
        System.out.println("Copy best test");
        int n = 5000;
        Model copy = makeCopyBest(n,Environments.COPY.make(),"copy");
        Model trail = makeCopyBest(n,Environments.TRAIL.make(),"trail");
        testModels(copy,trail);
    }

    @Test(groups = "1s", timeOut=60000)
    public void testBasic() {
        test(Environments.TRAIL.make());
        test(Environments.COPY.make());
    }

    private static void test(IEnvironment env) {
        env.makeBitSet(4);
        env.makeBool(false);
        env.makeFloat();
        env.makeFloat(4);
        env.makeLong();
        env.makeLong(4);
        env.makeIntVector(10,10);
        env.makeDoubleVector(2,4);
        env.worldPush();
        env.worldPop();
    }

    private static void testModels(Model... models) {
        IntVar[][] vars = new IntVar[models.length][];
        for(int i=0;i<models.length;i++){
            Assert.assertEquals(models[0].getResolutionPolicy(),models[i].getResolutionPolicy());
            vars[i] = models[i].retrieveIntVars(true);
            Assert.assertEquals(vars[i].length,vars[0].length);
        }
        long t;
        long[] time = new long[models.length];
        boolean bc;
        int nbSols=0;
        do {
            t = System.currentTimeMillis();
            bc = models[0].solve();
            time[0] += System.currentTimeMillis() - t;
            if(bc) nbSols++;
            for(int k=1;k<models.length;k++) {
                t = System.currentTimeMillis();
                Assert.assertEquals(bc, models[k].solve());
                time[k] += System.currentTimeMillis() - t;
                Assert.assertEquals(
                        models[k].getSolver().getBackTrackCount(),
                        models[0].getSolver().getBackTrackCount());
                Assert.assertEquals(
                        models[k].getSolver().getCurrentDepth(),
                        models[0].getSolver().getCurrentDepth());
                Assert.assertEquals(
                        models[k].getSolver().getMaxDepth(),
                        models[0].getSolver().getMaxDepth());
                Assert.assertEquals(
                        models[k].getSolver().getFailCount(),
                        models[0].getSolver().getFailCount());
                if(models[0].getResolutionPolicy()!=ResolutionPolicy.SATISFACTION)
                Assert.assertEquals(
                        models[k].getSolver().getBestSolutionValue(),
                        models[0].getSolver().getBestSolutionValue());
                if (bc) {
                    for (int i = 0; i < vars[k].length; i++) {
                        Assert.assertEquals(vars[0][i].getValue(), vars[0][i].getValue());
                    }
                }
            }
        }while (bc);
        System.out.println(nbSols+" solutions");
        for(int i=0;i<models.length;i++){
            System.out.println(models[i].getName()+" solved in "+time[i]+" ms");
        }
    }

    private static Model makeCostasArrays(int n, IEnvironment env, String suffix) {
        Model model = new Model(env,"Costas "+suffix);
        IntVar[] vars = model.intVarArray("v", n, 0, n - 1);
        IntVar[] vectors = new IntVar[(n * (n - 1)) / 2];
        IntVar[][] diff = new IntVar[n][n];
        int idx = 0;
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                IntVar k = model.intVar(randomName(), -n, n);
                model.arithm(k, "!=", 0).post();
                model.sum(new IntVar[]{vars[i], k}, "=", vars[j]).post();
                vectors[idx] = model.intOffsetView(k, 2 * n * (j - i));
                diff[i][j] = k;
                idx++;
            }
        }
        model.allDifferent(vars, "AC").post();
        model.allDifferent(vectors, "BC").post();
        model.arithm(vars[0], "<", vars[n - 1]).post();
        return model;
    }

    private static Model makeCountPb(int n, IEnvironment env, String suffix) {
        Model model = new Model(env,"Count "+suffix);
        IntVar[] vars = model.intVarArray("v", n, 0, n - 1);
        IntVar nbZeros = model.intVar(0,n);
        model.allDifferentExcept0(vars).post();
        model.sum(vars,"=",n).post();
        model.count(0,vars,nbZeros).post();
        model.setObjective(ResolutionPolicy.MINIMIZE,nbZeros);
        return model;
    }

    private static Model makeDiffN(int n, IEnvironment env, String suffix) {
        Model model = new Model(env,"DiffN "+suffix);
        IntVar[] x = model.intVarArray("x", n, 0, n - 1);
        IntVar[] y = model.intVarArray("y", n, 0, n - 1);
        IntVar[] w = model.intVarArray("w", n, 1,1);
        IntVar[] h = model.intVarArray("h", n, 1,1);
        model.diffN(x,y,w,h,true).post();
        return model;
    }

    private static Model makeTrailBest(int n, IEnvironment env, String suffix) {
        Model model = new Model(env,"Trail best "+suffix);
        IntVar[] x = model.intVarArray("x", n, 0, 5);
        for(int i=0;i<x.length-1;i++){
            model.arithm(x[i],"!=",x[i+1]).post();
        }
        model.getSolver().set(SearchStrategyFactory.randomSearch(x,0));
        return model;
    }

    private static Model makeCopyBest(int n, IEnvironment env, String suffix) {
        Model model = new Model(env,"Copy best "+suffix);
        IntVar[] x = model.intVarArray("x", n, 0, 100000);
        for(int i=0;i<n-1;i++)model.arithm(x[i],"=",x[i+1]).post();
        model.getSolver().limitSolution(500);
        return model;
    }
}
