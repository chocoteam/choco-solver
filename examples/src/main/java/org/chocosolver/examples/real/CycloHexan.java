/*
 * This file is part of examples, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.examples.real;

import org.chocosolver.examples.AbstractProblem;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.loop.monitors.IMonitorSolution;
import org.chocosolver.solver.variables.RealVar;

import static java.lang.Double.NEGATIVE_INFINITY;
import static java.lang.Double.POSITIVE_INFINITY;
import static java.lang.String.format;
import static java.lang.System.out;
import static org.chocosolver.solver.search.strategy.Search.realVarSearch;

/**
 * The cyclo hexan problem.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 18/07/12
 */
public class CycloHexan extends AbstractProblem {

    RealVar[] vars;
    RealVar x, y, z;


    @Override
    public void buildModel() {
        model = new Model();
        out.println("The CycloHexan problem consists in finding the 3D configuration of a cyclohexane molecule.\n" +
                "It is decribed with a system of three non linear equations : \n" + " y^2 * (1 + z^2) + z * (z - 24 * y) = -13 \n" +
                " x^2 * (1 + y^2) + y * (y - 24 * x) = -13 \n" +
                " z^2 * (1 + x^2) + x * (x - 24 * z) = -13 \n" + "This example comes from the Elisa project (LINA) examples. \n");

        double precision = 1.0e-6;
        x = model.realVar("x", NEGATIVE_INFINITY, POSITIVE_INFINITY, precision);
        y = model.realVar("y", -1.0e8, 1.0e8, precision);
        z = model.realVar("z", -1.0e8, 1.0e8, precision);

        vars = new RealVar[]{x, y, z};
        model.realIbexGenericConstraint(
                "{1}^2 * (1 + {2}^2) + {2} * ({2} - 24 * {1}) = -13;" +
                        "{0}^2 * (1 + {1}^2) + {1} * ({1} - 24 * {0}) = -13;" +
                        "{2}^2 * (1 + {0}^2) + {0} * ({0} - 24 * {2}) = -13",
                vars).post();


    }

    @Override
    public void configureSearch() {
        Solver r = model.getSolver();
        r.setSearch(realVarSearch(vars));
    }

    @Override
    public void solve() {
        model.getSolver().plugMonitor((IMonitorSolution) () -> {
            StringBuilder st = new StringBuilder();
            st.append("\t");
            for (int i = 0; i < vars.length; i++) {
                st.append(format("%s : [%f, %f]\n\t", vars[i].getName(), vars[i].getLB(), vars[i].getUB()));
            }
            out.println("CycloHexan");
            out.println(st.toString());
        });
        while (model.getSolver().solve()) ;
    }

    public static void main(String[] args) {
        new CycloHexan().execute(args);
    }
}
