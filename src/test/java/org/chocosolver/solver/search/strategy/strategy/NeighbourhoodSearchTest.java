package org.chocosolver.solver.search.strategy.strategy;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.IntVar;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * <br/>
 *
 * @author Pierre Tassel
 * @since 10/10/2019
 */
public class NeighbourhoodSearchTest {

    Model model;
    IntVar[] x;
    IntVar[] y;
    Solver solver;

    @BeforeMethod
    public void setUp() {
        model = new Model();
        x = model.intVarArray(10, new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9});
        y = model.intVarArray(10, new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9});
        model.allDifferent(x).post();
        model.allDifferent(y).post();
        model.arithm(x[0], "=", y[0]).post();
        model.arithm(x[1], "!=", y[1]).post();
        solver = model.getSolver();
    }

    @Test
    public void selected() {
        boolean[] selected = new boolean[]{true, false, false, false, false, false, false, false, false, false};
        int[] initial = new int[]{4, 1, 0, 3, 2, 9, 5, 7, 6, 7};
        AbstractStrategy<IntVar>  search = Search.intNeighbourhoodSearch(x, selected, initial, Search.minDomLBSearch(x));
        solver.setSearch(search);
        int selectedByMinDom = Search.minDomLBSearch(x).getValSelector().selectValue(x[0]);
        assertTrue(solver.solve(), "We have found a solution to the model");
        assertEquals(x[0].getValue(), 4, "We use the initial selection");
        assertEquals(x[0].getValue(), initial[0], "We use the initial selection");
        assertNotEquals(x[0].getValue(), selectedByMinDom, "We didn't used the initial selection");
    }

    @Test
    public void notSelected() {
        boolean[] selected = new boolean[]{false, false, false, false, false, false, false, false, false, false};
        int[] initial = new int[]{2, 1, 0, 3, 2, 9, 5, 7, 6, 7};
        AbstractStrategy<IntVar>  search = Search.intNeighbourhoodSearch(x, selected, initial, Search.minDomLBSearch(x));
        solver.setSearch(search);
        int selectedByMinDom = Search.minDomLBSearch(x).getValSelector().selectValue(x[0]);
        assertTrue(solver.solve(), "We have found a solution to the model");
        assertNotEquals(x[0].getValue(), initial[0], "We don't use the initial selection");
        assertEquals(x[0].getValue(), selectedByMinDom, "We use the initial selection");
    }
}
