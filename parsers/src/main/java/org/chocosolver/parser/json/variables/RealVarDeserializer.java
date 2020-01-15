/*
 * This file is part of choco-parsers, http://choco-solver.org/
 *
 * Copyright (c) 2019, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.parser.json.variables;

import com.google.gson.*;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.impl.FixedRealVarImpl;
import org.chocosolver.solver.variables.impl.RealVarImpl;

import java.lang.reflect.Type;
import java.util.regex.Pattern;

import static org.chocosolver.parser.json.ModelDeserializer.addVar;
import static org.chocosolver.parser.json.ModelDeserializer.get;

/**
 * * Utility class to deserialize RealVar
 *
 * <p> Project: choco-json.
 *
 * @author Charles Prud'homme
 * @since 18/09/2017.
 */
public class RealVarDeserializer implements JsonDeserializer<RealVar> {

    private static final Pattern p0 = Pattern.compile("\\{.*}");
    private static final Pattern p1 = Pattern.compile("\\[.*]");
    private static final Pattern p2 = Pattern.compile("\\.\\.");
    private static final Pattern p3 = Pattern.compile(",");

    @Override
    public RealVar deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        Model model = get();
        JsonObject src = json.getAsJsonObject();
        String name = src.get("name").getAsString();
        RealVar var;
        String domain = src.get("dom").getAsString();
        double[] dom = read(domain);
        if (dom.length == 1) {
            var = new FixedRealVarImpl(name, dom[0], model);
        } else {
            double p = src.get("pr").getAsDouble();
            var = new RealVarImpl(name, dom[0], dom[1], p, model);
        }
        addVar(src.get("id").getAsString(), var);
        return var;
    }

    private static double[] read(String dom1) {
        if (p0.matcher(dom1).matches()) {
            String dom = dom1.substring(1, dom1.length() - 1);
            String[] ranges = dom.split(p3.pattern());
            assert ranges.length == 1;
            String[] range = ranges[0].split(p2.pattern());
            if (range.length == 1) {
                return new double[]{Double.parseDouble(range[0])};
            }
        } else if (p1.matcher(dom1).matches()) {
            String dom = dom1.substring(1, dom1.length() - 1);
            String[] range = dom.split(p3.pattern());
            if (range.length == 2) {
                return new double[]{Double.parseDouble(range[0]), Double.parseDouble(range[1])};
            }
        }
        throw new JsonParseException("Cannot parse domain for real variable");
    }

}
