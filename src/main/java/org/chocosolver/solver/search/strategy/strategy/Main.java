package org.chocosolver.solver.search.strategy.strategy;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.ValueSelect;
import org.chocosolver.solver.search.strategy.VariableSelect;
import org.chocosolver.solver.variables.IntVar;

/**
 * Created by alexa on 28/07/2016.
 */
public class Main {

    public static void main(String[] args) {
        Model model = new Model();
        IntVar a = model.intVar("a", new int[]{1, 5, 8, 9});
        IntVar b = model.intVar("b", new int[]{5, 8, 9});
        IntVar c = model.intVar("c", new int[]{8, 9, 10, 15});

        model.getSolver().showDecisions();

        model.getSolver().setSearch(Search.intVarSearch(VariableSelect.cyclic(),
                ValueSelect.intDomainMin(),
                a, b, c));

        model.getSolver().solve();

    }
}
