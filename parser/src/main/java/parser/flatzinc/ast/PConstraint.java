/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package parser.flatzinc.ast;

import gnu.trove.map.hash.THashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import parser.flatzinc.FZNException;
import parser.flatzinc.ast.constraints.IBuilder;
import parser.flatzinc.ast.expression.EAnnotation;
import parser.flatzinc.ast.expression.Expression;
import parser.flatzinc.parser.FZNParser;
import solver.Solver;
import solver.constraints.Constraint;

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
public final class PConstraint {

    static Logger LOGGER = LoggerFactory.getLogger("fzn");

    private static final String ERROR_MSG = "Cant load manager by reflection: ";

    static Properties properties = new Properties();

    private static THashMap<String, IBuilder> builders = new THashMap<String, IBuilder>();

    static {
        InputStream is = PConstraint.class.getResourceAsStream("/fzn_manager.properties");
        try {
            properties.load(is);
        } catch (IOException e) {
            LOGGER.error("Could not open application.properties");
            throw new FZNException("Could not open fzn_manager.properties");
        }
    }

    public PConstraint(FZNParser parser, String id, List<Expression> exps, List<EAnnotation> annotations) {
        //TODO: manage annotations
//        build(id, exps, parser.solver);
        Solver solver = parser.solver;
        IBuilder builder;
        if (builders.containsKey(id)) {
            builder = builders.get(id);
        } else {
            String name = properties.getProperty(id);
            if (name == null) {
                throw new FZNException("Unkown constraint: " + id);
            }
            builder = (IBuilder) loadManager(name);
            builders.put(id, builder);
        }
        Constraint cstr = builder.build(solver, id, exps, annotations);
        solver.post(cstr);
        readAnnotations(annotations);
    }


    private static Object loadManager(String name) {
        try {
            return Class.forName(name).newInstance();
        } catch (ClassNotFoundException e) {
            throw new FZNException(ERROR_MSG + name);
        } catch (InstantiationException e) {
            throw new FZNException(ERROR_MSG + name);
        } catch (IllegalAccessException e) {
            throw new FZNException(ERROR_MSG + name);
        }
    }

    private static void readAnnotations(List<EAnnotation> annotations) {
        if (annotations.size() > 0) {
            LOGGER.trace("% unsupported operation: annotation");
        }
//        for (int i = 0; i < annotations.size(); i++) {
//            EAnnotation annotation = annotations.get(i);
//        }
    }
}
