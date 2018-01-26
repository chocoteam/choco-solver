/**
 * This file is part of choco-parsers, https://github.com/chocoteam/choco-parsers
 *
 * Copyright (c) 2018, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.parser.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.Task;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.view.IView;

import java.util.HashMap;

/**
 * Deserializer for Model. <p> Project: choco-json.
 *
 * @author Charles Prud'homme
 * @since 12/09/2017.
 */
public class ModelDeserializer implements JsonDeserializer<Model> {

    // Thread local variable containing each thread's ID
    private static final ThreadLocal<Model> cache = ThreadLocal.withInitial(Model::new);

    @Override
    public Model deserialize(JsonElement json, java.lang.reflect.Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jobject = json.getAsJsonObject();
        Model model = get();
        // read name
        model.setName(jobject.get("name").getAsString());
        // read precision
        if (jobject.get("precision") != null) {
            model.setPrecision(jobject.get("precision").getAsDouble());
        }
        // read all variables
        JsonArray store = jobject.get("variables").getAsJsonArray();
        for (int i = 0; i < store.size(); i++) {
            JsonObject elm = (JsonObject) store.get(i);
            String vtype = context.deserialize(elm.get("type"), String.class);
            switch (vtype) {
                case "bvar":
                case "ivar":
                    context.deserialize(elm, IntVar.class);
                    break;
                case "not":
                case "minus":
                case "scale":
                case "offset":
                case "real":
                    context.deserialize(elm, IView.class);
                    break;
                case "svar":
                    context.deserialize(elm, SetVar.class);
                    break;
                case "rvar":
                    context.deserialize(elm, RealVar.class);
                    break;
            }
        }
        JsonElement elm = jobject.get("tasks");
        if (elm != null) {
            store = elm.getAsJsonArray();
            for (int i = 0; i < store.size(); i++) {
                JsonObject obj = (JsonObject) store.get(i);
                context.deserialize(obj, Task.class);
            }
        }
        elm = jobject.get("objective");
        if (elm != null) {
            JsonObject objective = elm.getAsJsonObject();
            model.setObjective(objective.get("maximize").getAsBoolean(),
                    getVar(objective.get("id").getAsString()));
        }
        store = jobject.get("constraints").getAsJsonArray();
        for (int i = 0; i < store.size(); i++) {
            Constraint c = context.deserialize(store.get(i), Constraint.class);
            if(c != null){ // may happen, f-ex. for reification
                c.post();
            }
        }
        cache.remove();
        return model;
    }

    public static Model get() {
        return cache.get();
    }

    @SuppressWarnings("unchecked")
    private static HashMap<String, Variable> getVarMap() {
        Model model = cache.get();
        HashMap<String, Variable> map = (HashMap<String, Variable>) model.getHook("mapOfVars");
        if (map == null) {
            map = new HashMap<>();
            model.addHook("mapOfVars", map);
        }
        return map;
    }

    /**
     * Add a new entry in the map of variable
     *
     * @param id  variable id in JSON file
     * @param var variable in the current model
     */
    public static void addVar(String id, Variable var) {
        getVarMap().put(id, var);
    }

    /**
     * @param id variable id in JSON file
     * @return variable that is mapped to 'id'
     */
    public static Variable getVar(String id) {
        return getVarMap().get(id);
    }

    /**
     * @param id variable id in JSON file
     * @return integer variable that is mapped to 'id'
     */
    public static IntVar getIntVar(String id) {
        return (IntVar)getVarMap().get(id);
    }

    /**
     * @param id variable id in JSON file
     * @return boolean variable that is mapped to 'id'
     */
    public static BoolVar getBoolVar(String id) {
        return (BoolVar)getVarMap().get(id);
    }

    /**
     * @param id variable id in JSON file
     * @return Set variable that is mapped to 'id'
     */
    public static SetVar getSetVar(String id) {
        return (SetVar)getVarMap().get(id);
    }

    /**
     * @param id variable id in JSON file
     * @return Real variable that is mapped to 'id'
     */
    public static RealVar getRealVar(String id) {
        return (RealVar)getVarMap().get(id);
    }
}
