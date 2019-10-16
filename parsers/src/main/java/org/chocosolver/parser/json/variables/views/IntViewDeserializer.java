/*
 * This file is part of choco-parsers, http://choco-solver.org/
 *
 * Copyright (c) 2019, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.parser.json.variables.views;

import com.google.gson.*;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.view.*;

import java.lang.reflect.Type;

import static org.chocosolver.parser.json.ModelDeserializer.addVar;
import static org.chocosolver.parser.json.ModelDeserializer.getIntVar;

/**
 * * Utility class to deserialize IntView
 *
 * <p> Project: choco-json.
 *
 * @author Charles Prud'homme
 * @since 18/09/2017.
 */
public class IntViewDeserializer implements JsonDeserializer<Variable> {

    @Override
    public Variable deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jview = json.getAsJsonObject();
        String impl = jview.get("type").getAsString();
        IntVar src = getIntVar(jview.get("of").getAsString());
        Variable view = null;
        switch (impl) {
            case "not":
                view = makeNot((BoolVar) src);
                break;
            case "minus":
                view = new MinusView(src);
                break;
            case "offset": {
                int c = jview.get("factor").getAsInt();
                view = new OffsetView(src, c);
            }
            break;
            case "scale": {
                int c = jview.get("factor").getAsInt();
                view = new ScaleView(src, c);
            }
            break;
            case "real": {
                double p = jview.get("pr").getAsDouble();
                view = new RealView(src, p);
            }
            break;
        }
        addVar(jview.get("id").getAsString(), view);
        return view;
    }

    private BoolVar makeNot(BoolVar src) {
        BoolVar view = new BoolNotView(src);
        view._setNot(src);
        view.setNot(true);
        src._setNot(view);
        return view;
    }
}
