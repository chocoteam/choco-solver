/**
 * This file is part of choco-parsers, https://github.com/chocoteam/choco-parsers
 *
 * Copyright (c) 2019, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.parser.json.variables;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import org.chocosolver.parser.json.JSONHelper;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableRangeSet;

import java.lang.reflect.Type;
import java.util.regex.Pattern;

import static org.chocosolver.parser.json.ModelDeserializer.addVar;
import static org.chocosolver.parser.json.ModelDeserializer.get;

/**
 * * Utility class to deserialize IntVar (including BoolVar and fixed variations)
 *
 * <p> Project: choco-json.
 *
 * @author Charles Prud'homme
 * @since 18/09/2017.
 */
public class IntVarDeserializer implements JsonDeserializer<IntVar> {

    private static final Pattern p0 = Pattern.compile("\\{.*}");
    private static final Pattern p1 = Pattern.compile("\\[.*]");
    private static final Pattern p3 = Pattern.compile(",");

    @Override
    public IntVar deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        Model model = get();
        JsonObject src = json.getAsJsonObject();
        String name = src.get("name").getAsString();
        IntVar var;
        if (src.get("type").getAsString().equals("bvar")) {
            var = model.boolVar(name);
        } else {
            String dom = src.get("dom").getAsString();
            var = read(model, name, dom);
        }
        addVar(src.get("id").getAsString(), var);
        return var;
    }

    private static IntVar read(Model model, String name, String dom1) {
        if (p0.matcher(dom1).matches()) {
            IntIterableRangeSet set = JSONHelper.convert(dom1);
            if (set.size() == 1) { // fixed, may be cached
                return model.intVar(name, set.min());
            } else if (set.size() == set.max() - set.min() + 1) {
                return model.intVar(name, set.min(), set.max());
            } else {
                return model.intVar(name, set.toArray());
            }
        } else if (p1.matcher(dom1).matches()) {
            String dom = dom1.substring(1, dom1.length() - 1);
            String[] range = dom.split(p3.pattern());
            if (range.length == 2) {
                return model.intVar(name, Integer.parseInt(range[0]), Integer.parseInt(range[1]), true);
            }
        }
        throw new JsonParseException("Cannot parse domain for integer variable");
    }
}
