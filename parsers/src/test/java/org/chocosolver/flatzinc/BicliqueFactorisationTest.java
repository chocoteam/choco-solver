package org.chocosolver.flatzinc;

import org.chocosolver.parser.SetUpException;
import org.chocosolver.parser.flatzinc.Flatzinc;
import org.chocosolver.sat.MiniSat;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Settings;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.loop.learn.LazyClauseGeneration;
import org.chocosolver.solver.search.loop.monitors.IMonitorContradiction;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Task;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.nio.file.Paths;
import java.util.Objects;


public class BicliqueFactorisationTest {

    @DataProvider
    public Object[][] sudoku() {

        return new Object[][]{
                //
                {"/flatzinc/factorisation/sudoku_fixed_sudoku_p89.fzn", false, 0, 1, 1214},
                {"/flatzinc/factorisation/sudoku_fixed_sudoku_p89.fzn", true, 0, 1, 1214},
                {"/flatzinc/factorisation/sudoku_fixed_sudoku_p89.fzn", false, 1, 1, 1188},
                {"/flatzinc/factorisation/sudoku_fixed_sudoku_p89.fzn", true, 1,  1, 1188},
                {"/flatzinc/factorisation/sudoku_fixed_sudoku_p89.fzn", false, 2, 1, 1140},
                {"/flatzinc/factorisation/sudoku_fixed_sudoku_p89.fzn", true, 2, 1, 1140},
                //
                {"/flatzinc/factorisation/sudoku_fixed_sudoku_p20.fzn", false, 0, 1, 10062},
                {"/flatzinc/factorisation/sudoku_fixed_sudoku_p20.fzn", true, 0, 1, 10062},
                {"/flatzinc/factorisation/sudoku_fixed_sudoku_p20.fzn", false, 1, 1, 9903},
                {"/flatzinc/factorisation/sudoku_fixed_sudoku_p20.fzn", true, 1, 1, 9903},
                {"/flatzinc/factorisation/sudoku_fixed_sudoku_p20.fzn", false, 2, 1, 9827},
                {"/flatzinc/factorisation/sudoku_fixed_sudoku_p20.fzn", true, 2, 1, 9827},
                //
                {"/flatzinc/factorisation/sudoku_fixed_sudoku_p28.fzn", false, 0, 1, 59632},
                {"/flatzinc/factorisation/sudoku_fixed_sudoku_p28.fzn", true, 0, 1, 59632},
                {"/flatzinc/factorisation/sudoku_fixed_sudoku_p28.fzn", false, 1, 1, 60093},
                {"/flatzinc/factorisation/sudoku_fixed_sudoku_p28.fzn", true, 1, 1, 60093},
                {"/flatzinc/factorisation/sudoku_fixed_sudoku_p28.fzn", false, 2, 1, 60195},
                {"/flatzinc/factorisation/sudoku_fixed_sudoku_p28.fzn", true, 2, 1, 60195},
                //
                //{"/flatzinc/sudoku_fixed/sudoku_fixed_sudoku_p26.fzn", false, 2, 1, 89145},
                //{"/flatzinc/sudoku_fixed/sudoku_fixed_sudoku_p26.fzn", true, 2, 1, 89145},
        };
    }

    @DataProvider
    public Object[][] trains() {

        return new Object[][]{
                //
                {"/flatzinc/factorisation/trains_trains15.fzn", false, 0, 314, 34112},
                {"/flatzinc/factorisation/trains_trains15.fzn", true, 0, 314, 34112},
                {"/flatzinc/factorisation/trains_trains15.fzn", false, 1, 314, 34375},
                {"/flatzinc/factorisation/trains_trains15.fzn", true, 1, 314, 34375},
                {"/flatzinc/factorisation/trains_trains15.fzn", false, 2, 314, 34375},
                {"/flatzinc/factorisation/trains_trains15.fzn", true, 2, 314, 34375},

        };
    }

    @DataProvider
    public Object[][] mrcpsp() {

        return new Object[][]{
                //
//                {"/flatzinc/factorisation/mrcpsp_j30_25_5.fzn", false, 0, 10, 2479},
//                {"/flatzinc/factorisation/mrcpsp_j30_25_5.fzn", true, 0, 10, 2479},
//                {"/flatzinc/factorisation/mrcpsp_j30_25_5.fzn", false, 1, 10, 2475},
//                {"/flatzinc/factorisation/mrcpsp_j30_25_5.fzn", true, 1, 10, 2475},
//                {"/flatzinc/factorisation/mrcpsp_j30_25_5.fzn", false, 2, 10, 2474},
//                {"/flatzinc/factorisation/mrcpsp_j30_25_5.fzn", true, 2, 10, 2474},

//                {"/flatzinc/factorisation/mrcpsp_j30_33_5.fzn", false, 0, 17, 2479},
//                {"/flatzinc/factorisation/mrcpsp_j30_33_5.fzn", true, 0, 17, 2479},
//                {"/flatzinc/factorisation/mrcpsp_j30_33_5.fzn", false, 1, 17, 2475},
//                {"/flatzinc/factorisation/mrcpsp_j30_33_5.fzn", true, 1, 17, 2475},
//                {"/flatzinc/factorisation/mrcpsp_j30_33_5.fzn", false, 2, 17, 2474},
//                {"/flatzinc/factorisation/mrcpsp_j30_33_5.fzn", true, 2, 17, 2474},
                //
                {"/flatzinc/factorisation/mrcpsp_j30_54_2.fzn", false, 0, 14, 2479},
                {"/flatzinc/factorisation/mrcpsp_j30_54_2.fzn", true, 0, 14, 2479},
//                {"/flatzinc/factorisation/mrcpsp_j30_54_2.fzn", false, 1, 14, 2475},
//                {"/flatzinc/factorisation/mrcpsp_j30_54_2.fzn", true, 1, 14, 2475},
//                {"/flatzinc/factorisation/mrcpsp_j30_54_2.fzn", false, 2, 14, 2474},
//                {"/flatzinc/factorisation/mrcpsp_j30_54_2.fzn", true, 2, 14, 2474},

        };
    }

    @Test(groups = "mzn", timeOut = 600_000, dataProvider = "mrcpsp")
    public void test1(String path,
                      boolean PARAM_BICLIQUE_FACTORISATION,
                      int PARAM_CLAUSE_MINIMISATION,
                      int nbSolutions,
                      int nbNodes) throws SetUpException {
        // |                       instance | fact. | ccmin |  sort |   time (s) |      nodes |
        //String path = "/flatzinc/sudoku_fixed/sudoku_fixed_sudoku_p20.fzn";
        Settings.PARAM_BICLIQUE_FACTORISATION = PARAM_BICLIQUE_FACTORISATION;
        Settings.PARAM_CLAUSE_MINIMISATION = PARAM_CLAUSE_MINIMISATION;
        LazyClauseGeneration.VERBOSE = true;
        MiniSat.DEBUG = 0;


        String file = Objects.requireNonNull(this.getClass().getResource(path)).getFile();
        String[] args = new String[]{
                file,
                "--disable-shutdown-hook",
                "-limit", "[500s]", // but, problems are expected to end within 15s max
                "-lvl", "INFO",
                "-p", "1",
                "-lcg"
        };
        Flatzinc fzn = new Flatzinc();
        fzn.setUp(args);
        fzn.createSolver();
        fzn.buildModel();
        fzn.configureSearch();
        Solver solver = fzn.getModel().getSolver();
        //Settings.PARAM_SORT_LITS_ON_FAILURE = true;
        //int limit = 60;//3200; // [3121, 3246]
        solver.limitFail(3811);//) 3136 - 3832 / 2 + 3832 / 4 + 3832 / 8);
        solver.plugMonitor(new IMonitorContradiction() {
            @Override
            public void onContradiction(ContradictionException cex) {
                if(solver.getFailCount() >= 3811){
                    MiniSat.DEBUG = 2;
                }
            }
        });
        solver.showDecisions(()-> ""+ solver.getFailCount());
        fzn.solve();
        Assert.assertEquals(solver.getSolutionCount(), nbSolutions, "Unexpected number of solutions");
        //Assert.assertEquals(fzn.getModel().getSolver().getNodeCount(), nbNodes, "Unexpected number of nodes");
        System.out.printf("| %30s | %5s | %5s | %10.2f | %10d | %10d |\n",
                Paths.get(file).getFileName(),
                PARAM_BICLIQUE_FACTORISATION,
                PARAM_CLAUSE_MINIMISATION,
                solver.getTimeCount(),
                solver.getObjectiveManager().isOptimization() ? solver.getObjectiveManager().getBestSolutionValue().intValue() : -1,
                solver.getNodeCount());
    }


    @Test
    public void testBug(){
        Model model = new Model(Settings.init().setLCG(true));
        Task[] tasks = new Task[2];
        tasks[0] = model.taskVar(model.intVar("S1", 0, 15), 15);
        tasks[1] = model.taskVar(model.intVar("S2",0, 19), 11);
        model.cumulative(tasks, new IntVar[]{model.intVar(1), model.intVar(1)}, model.intVar(1)).post();

        Solver solver = model.getSolver();
        solver.setSearch(Search.inputOrderLBSearch(tasks[0].getStart(), tasks[1].getStart()));
        while(solver.solve()){
            System.out.printf("[%d, %d] -- [%d, %d]%n",
                    tasks[0].getStart().getValue(), tasks[0].getEnd().getValue(),
                    tasks[1].getStart().getValue(), tasks[1].getEnd().getValue());
        }
        Assert.assertEquals(solver.getSolutionCount(), 30);
    }

}
