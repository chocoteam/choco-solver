/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2017, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license. See LICENSE file in the project root for full license
 * information.
 */
package org.chocosolver.writer.constraints.real;

import org.chocosolver.util.Reflection;
import org.chocosolver.writer.constraints.ConstraintWriter;
import org.chocosolver.solver.constraints.Propagator;

import java.io.IOException;
import java.util.stream.IntStream;

/**
 * Utility class to encode real constraints
 *
 * <p> Project: choco-json.
 *
 * @author Charles Prud'homme
 * @since 27/09/2017.
 */
public class RealWriterHelper {

    private RealWriterHelper() {
    }

    public static void writeInteqreal(ConstraintWriter writer, Propagator[] propagators) throws IOException {
        Propagator prop0 = propagators[0];
        int n = Reflection.getInt(prop0, "n");
        double epsilon = Reflection.getObj(prop0, "epsilon");
        writer.writeInteqreal(
                IntStream.range(0, n).map(i -> prop0.getVar(i).getId()).toArray(),
                IntStream.range(n, prop0.getNbVars()).map(i -> prop0.getVar(i).getId()).toArray(),
                epsilon
        );
    }

    public static void writeRealConstraint(ConstraintWriter writer, Propagator[] propagators) throws IOException {
        Propagator prop0 = propagators[0];
        String functions = Reflection.getObj(prop0, "functions");
        writer.writeRealConstraint(
                IntStream.range(0, prop0.getNbVars()).map(i -> prop0.getVar(i).getId()).toArray(),
                functions
        );
    }
}
