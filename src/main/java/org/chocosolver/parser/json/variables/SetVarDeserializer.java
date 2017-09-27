/**
 * This file is part of choco-parsers, https://github.com/chocoteam/choco-parsers
 *
 * Copyright (c) 2017, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.parser.json.variables;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.SetVar;

import java.lang.reflect.Type;

import static org.chocosolver.parser.json.ModelDeserializer.addVar;
import static org.chocosolver.parser.json.ModelDeserializer.get;

/**
 * Utility class to deserialize Task
 *
 * <p> Project: choco-json.
 *
 * @author Charles Prud'homme
 * @since 18/09/2017.
 */
public class SetVarDeserializer implements JsonDeserializer<SetVar> {

    @Override
    public SetVar deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        Model model = get();
        JsonObject src = json.getAsJsonObject();
        String name = src.get("name").getAsString();
        JsonArray lb = src.get("lb").getAsJsonArray();
        int[] lbs = new int[lb.size()];
        for (int i = 0; i < lb.size(); i++) {
            lbs[i] = lb.get(i).getAsInt();
        }
        JsonArray ub = src.get("ub").getAsJsonArray();
        int[] ubs = new int[ub.size()];
        for (int i = 0; i < ub.size(); i++) {
            ubs[i] = ub.get(i).getAsInt();
        }
        SetVar var = model.setVar(name, lbs, ubs);
        addVar(src.get("id").getAsString(), var);
        return var;
    }

}
