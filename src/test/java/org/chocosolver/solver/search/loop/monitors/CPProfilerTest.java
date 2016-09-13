package org.chocosolver.solver.search.loop.monitors;

import org.chocosolver.solver.Model;
import org.chocosolver.util.ProblemMaker;
import org.testng.annotations.Test;

import java.io.IOException;

import static java.lang.System.out;

/**
 * <p>
 * Project: choco-solver.
 *
 * @author Charles Prud'homme
 * @since 13/09/2016.
 */
public class CPProfilerTest {

    @Test(groups = "1s", timeOut = 60000)
    public void test1() throws IOException {
        Model s1 = ProblemMaker.makeCostasArrays(7);
        try (CPProfiler profiler = new CPProfiler(s1)) {
            while (s1.getSolver().solve()) ;
            out.println(s1.getSolver().getSolutionCount());
        }
    }

    @Test(groups = "1s", timeOut = 60000)
    public void test2() throws IOException {
        Model s1 = ProblemMaker.makeCostasArrays(7);
        CPProfiler profiler = new CPProfiler(s1);
        while (s1.getSolver().solve()) ;
        out.println(s1.getSolver().getSolutionCount());
        profiler.close();
    }

}