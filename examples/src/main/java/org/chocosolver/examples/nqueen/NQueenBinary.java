/*
 * This file is part of examples, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.examples.nqueen;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;

import java.util.HashSet;
import java.util.stream.IntStream;

import static org.chocosolver.solver.search.strategy.Search.inputOrderLBSearch;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 31/03/11
 */
public class NQueenBinary extends AbstractNQueen {

    HashSet<Constraint> set;

    @Override
    public void buildModel() {
        model = new Model("NQueen");
        vars = model.intVarArray("Q", n, 1, n);
        IntStream.range(0, n-1).forEach(i ->
                IntStream.range(i+1, n).forEach(j ->{
                    model.arithm(vars[i], "!=", vars[j]).post();
                    model.arithm(vars[i], "!=", vars[j], "-", j - i).post();
                    model.arithm(vars[i], "!=", vars[j], "+", j - i).post();
                })
        );
    }



    @Override
    public void configureSearch() {
    	model.getSolver().setSearch(inputOrderLBSearch(vars));
        // model.getSolver().showSolutions();
    }

    @Override
    public void solve() {
        model.getSolver().findAllSolutions();
        model.getSolver().printShortStatistics();
    }

    public static void main(String[] args) {
        new NQueenBinary().execute(args);
    }
}
