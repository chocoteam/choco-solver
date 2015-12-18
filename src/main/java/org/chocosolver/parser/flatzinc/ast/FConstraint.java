/*
 * Copyright (c) 1999-2012, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.chocosolver.parser.flatzinc.ast;

import gnu.trove.map.hash.THashMap;
import org.chocosolver.parser.flatzinc.FZNException;
import org.chocosolver.parser.flatzinc.FznSettings;
import org.chocosolver.parser.flatzinc.ast.constraints.IBuilder;
import org.chocosolver.parser.flatzinc.ast.expression.EAnnotation;
import org.chocosolver.parser.flatzinc.ast.expression.Expression;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

/*
* User : CPRUDHOM
* Mail : cprudhom(a)emn.fr
* Date : 12 janv. 2010
* Since : Choco 2.1.1
*
* Constraint builder from flatzinc-like object.
*/
public final class FConstraint {

    private static final String ERROR_MSG = "Cant load manager by reflection: ";

    static Properties properties = new Properties();

    private static THashMap<String, IBuilder> builders = new THashMap<>();

    private static IBuilder builder;

    private static String last = "";

    static {
        InputStream is = FConstraint.class.getResourceAsStream("/fzn_manager.properties");
        try {
            properties.load(is);
        } catch (IOException e) {
            throw new FZNException("Could not open fzn_manager.properties");
        }
    }

    private enum Annotation {
        name
    }

    public static void make_constraint(Solver aSolver, Datas datas,
                                       String id, List<Expression> exps, List<EAnnotation> annotations) {
        if (!last.equals(id)) {
            if (builders.containsKey(id)) {
                builder = builders.get(id);
            } else {
                String name = properties.getProperty(id);
                if (name == null) {
                    throw new FZNException("Unknown constraint: " + id);
                }
                builder = (IBuilder) loadManager(name);
                if (((FznSettings) aSolver.getSettings()).printConstraint())
                    System.out.printf("%% %s\n", name);
                builders.put(id, builder);
            }
            last = id;
        }
        builder.build(aSolver, id, exps, annotations, datas);
        //readAnnotations(datas, annotations, c);
    }


    private static Object loadManager(String name) {
        try {
            return Class.forName(name).newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new FZNException(ERROR_MSG + name);
        }
    }

    public static void readAnnotations(Datas datas, List<EAnnotation> annotations, Constraint[] cstr) {
        for (int i = 0; i < annotations.size(); i++) {
            EAnnotation eanno = annotations.get(i);
            try {
                Annotation varanno = Annotation.valueOf(eanno.id.value);
                switch (varanno) {
                    case name:
                        String name = eanno.exps.get(0).toString();
                        if (name.startsWith("\"") && name.endsWith("\"")) {
                            name = name.substring(1, name.length() - 1);
                        }
                        datas.register(name, cstr);
                        break;
                    default:
                        //                            LOGGER.warn("% Unknown annotation :" + varanno.toString());
                }
            } catch (IllegalArgumentException ignored) {
                //                        LOGGER.warn("% Unknown annotation :" + eanno.toString());
            }
        }
    }
}
