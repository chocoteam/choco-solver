/**
 * This file is part of choco-parsers, https://github.com/chocoteam/choco-parsers
 *
 * Copyright (c) 2019, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.writer.constraints.real;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.util.Reflection;
import org.chocosolver.writer.constraints.ConstraintWriter;

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
        for(Propagator prop : propagators) {
            String functions = Reflection.getObj(prop, "functions");
            BoolVar boolVar = Reflection.getObj(prop, "reified");
            if(boolVar == null) {
                writer.writeRealConstraint(
                        IntStream.range(0, prop.getNbVars()).map(i -> prop.getVar(i).getId()).toArray(),
                        functions
                );
            }else{
                writer.writeRealConstraint(
                        IntStream.range(0, prop.getNbVars()).map(i -> prop.getVar(i).getId()).toArray(),
                        functions, boolVar.getId()
                );
            }
        }
    }
}
