/**
 * This file is part of choco-parsers, https://github.com/chocoteam/choco-parsers
 *
 * Copyright (c) 2017, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.parser.json.constraints;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import org.chocosolver.parser.json.JSONHelper;
import org.chocosolver.parser.json.ModelDeserializer;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.Operator;
import org.chocosolver.solver.constraints.extension.Tuples;
import org.chocosolver.solver.constraints.nary.circuit.CircuitConf;
import org.chocosolver.solver.constraints.nary.cumulative.Cumulative;
import org.chocosolver.solver.constraints.real.IntEqRealConstraint;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.Task;

import java.lang.reflect.Type;
import java.util.Arrays;

/**
 * Utility class for Constraint deserialization
 *
 * <p> Project: choco-json.
 *
 * @author Charles Prud'homme
 * @since 19/09/2017.
 */
public class ConstraintDeserializer implements JsonDeserializer<Constraint> {

    @Override
    public Constraint deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        Model model = ModelDeserializer.get();
        JsonObject jcstr = json.getAsJsonObject();
        Constraint c;
        String type = jcstr.get("type").getAsString();
        JsonArray jparams = jcstr.getAsJsonArray("params");
        switch (type) {
            case "reif": {
                BoolVar by = ModelDeserializer.getBoolVar(jcstr.get("by").getAsString());
                Constraint of = context.deserialize(jcstr.get("of"), Constraint.class);
                of.reifyWith(by);
                return null; // skip default post instruction
            }
            case "opp": {
                Constraint of = context.deserialize(jcstr.get("of"), Constraint.class);
                c = of.getOpposite();
            }
            break;
            case "arithm":
                c = makeArithm(jparams, model);
                break;
            case "member":
                c = makeMember(jparams, model);
                break;
            case "notmember":
                c = makeNotMember(jparams, model);
                break;
            case "absolute":
                c = makeAbsolute(jparams, model);
                break;
            case "alldifferent":
                c = makeAlldifferent(jparams, model);
                break;
            case "among":
                c = makeAmong(jparams, model);
                break;
            case "atleastnvalues":
                c = makeAtLeastNValues(jparams, model);
                break;
            case "atmostnvalues":
                c = makeAtMostNValues(jparams, model);
                break;
            case "binpacking":
                c = makeBinPacking(jparams, model);
                break;
            case "boolchanneling":
                c = makeBoolChanneling(jparams, model);
                break;
            case "bitschanneling":
                c = makeBitsChanneling(jparams, model);
                break;
            case "circuit":
                c = makeCircuit(jparams, model);
                break;
            case "count":
                c = makeCount(jparams, model);
                break;
            case "cumulative":
                c = makeCumulative(jparams, model);
                break;
            case "diffn":
                c = makeDiffn(jparams, model);
                break;
            case "distance":
                c = makeDistance(jparams, model);
                break;
            case "division":
                c = makeDivision(jparams, model);
                break;
            case "element":
                c = makeElement(jparams, model);
                break;
            case "gcc":
                c = makeGcc(jparams, model);
                break;
            case "inverse":
                c = makeInverseChanneling(jparams, model);
                break;
            case "knapsack":
                c = makeKnapsack(jparams, model);
                break;
            case "lexchain":
                c = makeLexchain(jparams, model);
                break;
            case "lex":
                c = makeLex(jparams, model);
                break;
            case "max":
                c = makeMax(jparams, model);
                break;
            case "min":
                c = makeMin(jparams, model);
                break;
            case "nvalues":
                c = makeNvalues(jparams, model);
                break;
            case "rarithm":
                c = makeRArithm(jparams, model);
                break;
            case "scalar":
                c = makeScalar(jparams, model);
                break;
            case "scale":
                c = makeScale(jparams, model);
                break;
            case "sum":
                c = makeSum(jparams, model);
                break;
            case "square":
                c = makeSquare(jparams, model);
                break;
            case "subcircuit":
                c = makeSubcircuit(jparams, model);
                break;
            case "table":
                c = makeTable(jparams, model);
                break;
            case "times":
                c = makeTimes(jparams, model);
                break;
            case "tree":
                c = makeTree(jparams, model);
                break;
            // Set constraints
            case "setalldifferent":
                c = makeSetAlldifferent(jparams, model);
                break;
            case "setalldisjoint":
                c = makeSetAlldisjoint(jparams, model);
                break;
            case "setallequal":
                c = makeSetAllequal(jparams, model);
                break;
            case "setboolchanneling":
                c = makeSetBoolChanneling(jparams, model);
                break;
            case "setcard":
                c = makeSetCard(jparams, model);
                break;
            case "setelement":
                c = makeSetElement(jparams, model);
                break;
            case "setintchanneling":
                c = makeSetIntChanneling(jparams, model);
                break;
            case "setintersection":
                c = makeSetIntersection(jparams, model);
                break;
            case "setintunion":
                c = makeSetIntUnion(jparams, model);
                break;
            case "setinverse":
                c = makeSetInverse(jparams, model);
                break;
            case "setmax":
                c = makeSetMax(jparams, model);
                break;
            case "setmember":
                c = makeSetMember(jparams, model);
                break;
            case "setmin":
                c = makeSetMin(jparams, model);
                break;
            case "setnbempty":
                c = makeSetNbEmpty(jparams, model);
                break;
            case "setnotempty":
                c = makeSetNotEmpty(jparams, model);
                break;
            case "setnotmember":
                c = makeSetNotMember(jparams, model);
                break;
            case "setoffset":
                c = makeSetOffset(jparams, model);
                break;
            case "setpartition":
                c = makeSetPartition(jparams, model);
                break;
            case "setsubseteq":
                c = makeSetSubSetEq(jparams, model);
                break;
            case "setsum":
                c = makeSetSum(jparams, model);
                break;
            case "setsymmetric":
                c = makeSetSymmetric(jparams, model);
                break;
            case "setunion":
                c = makeSetUnion(jparams, model);
                break;
            // Real constraints
            case "inteqreal":
                c = new IntEqRealConstraint(
                        extractIntVarArray(jparams.get(0).getAsJsonArray()),
                        extractRealVarArray(jparams.get(1).getAsJsonArray()),
                        jparams.get(2).getAsDouble());
                break;
            case "realcstr":
                c = model.realIbexGenericConstraint(
                        jparams.get(1).getAsString(),
                        extractRealVarArray(jparams.get(0).getAsJsonArray())
                );
                break;
            default:
                throw new JsonParseException("Unknown type : " + type);
        }
        return c;
    }

    private int[] extractIntArray(JsonArray jarray) {
        int[] array = new int[jarray.size()];
        for (int i = 0; i < jarray.size(); i++) {
            array[i] = jarray.get(i).getAsInt();
        }
        return array;
    }

    private BoolVar[] extractBoolVarArray(JsonArray jarray) {
        BoolVar[] array = new BoolVar[jarray.size()];
        for (int i = 0; i < jarray.size(); i++) {
            array[i] = ModelDeserializer.getBoolVar(jarray.get(i).getAsString());
        }
        return array;
    }

    private IntVar[] extractIntVarArray(JsonArray jarray) {
        IntVar[] array = new IntVar[jarray.size()];
        for (int i = 0; i < jarray.size(); i++) {
            array[i] = ModelDeserializer.getIntVar(jarray.get(i).getAsString());
        }
        return array;
    }

    private SetVar[] extractSetVarArray(JsonArray jarray) {
        SetVar[] array = new SetVar[jarray.size()];
        for (int i = 0; i < jarray.size(); i++) {
            array[i] = ModelDeserializer.getSetVar(jarray.get(i).getAsString());
        }
        return array;
    }

    private RealVar[] extractRealVarArray(JsonArray jarray) {
        RealVar[] array = new RealVar[jarray.size()];
        for (int i = 0; i < jarray.size(); i++) {
            array[i] = ModelDeserializer.getRealVar(jarray.get(i).getAsString());
        }
        return array;
    }

    private Constraint makeMember(JsonArray jparams, Model model) {
        IntVar v1 = ModelDeserializer.getIntVar(jparams.get(0).getAsString());
        JsonElement elm = jparams.get(1);
        if (elm.isJsonArray()) {
            JsonArray array = elm.getAsJsonArray();
            int[] values = new int[array.size()];
            for (int i = 0; i < array.size(); i++) {
                values[i] = array.get(i).getAsInt();
            }
            return model.member(v1, values);
        } else {
            return model.member(v1, elm.getAsInt(), jparams.get(2).getAsInt());
        }
    }

    private Constraint makeNotMember(JsonArray jparams, Model model) {
        IntVar v1 = ModelDeserializer.getIntVar(jparams.get(0).getAsString());
        JsonElement elm = jparams.get(1);
        if (elm.isJsonArray()) {
            JsonArray array = elm.getAsJsonArray();
            int[] values = new int[array.size()];
            for (int i = 0; i < array.size(); i++) {
                values[i] = array.get(i).getAsInt();
            }
            return model.notMember(v1, values);
        } else {
            return model.notMember(v1, elm.getAsInt(), jparams.get(2).getAsInt());
        }
    }

    private Constraint makeArithm(JsonArray jparams, Model model) {
        IntVar v1 = ModelDeserializer.getIntVar(jparams.get(0).getAsString());
        switch (jparams.size()) {
            case 3: {
                Operator op1 = Operator.get(jparams.get(1).getAsString());
                JsonElement elm = jparams.get(2);
                if (elm.getAsJsonPrimitive().isNumber()) {
                    return model.arithm(
                            v1,
                            op1.toString(),
                            elm.getAsInt()
                    );
                } else {
                    IntVar v2 = ModelDeserializer.getIntVar(elm.getAsString());
                    return model.arithm(
                            v1,
                            op1.toString(),
                            v2
                    );
                }
            }
            case 4: {
                Operator op1 = Operator.get(jparams.get(1).getAsString());
                if (op1 != null) {
                    return model.arithm(
                            v1,
                            op1.toString(),
                            ModelDeserializer.getIntVar(jparams.get(2).getAsString()),
                            Operator.PL.toString(),
                            jparams.get(3).getAsInt()
                    );
                } else {
                    return model.arithm(
                            v1,
                            Operator.PL.toString(),
                            ModelDeserializer.getIntVar(jparams.get(1).getAsString()),
                            jparams.get(2).getAsString(),
                            jparams.get(3).getAsInt()
                    );
                }
            }
        }
        return null;
    }

    private Constraint makeAbsolute(JsonArray jparams, Model model) {
        IntVar v1 = ModelDeserializer.getIntVar(jparams.get(0).getAsString());
        IntVar v2 = ModelDeserializer.getIntVar(jparams.get(1).getAsString());
        return model.absolute(v1, v2);
    }

    private Constraint makeDistance(JsonArray jparams, Model model) {
        IntVar v1 = ModelDeserializer.getIntVar(jparams.get(0).getAsString());
        IntVar v2 = ModelDeserializer.getIntVar(jparams.get(1).getAsString());
        String op = jparams.get(2).getAsString();
        JsonElement elm = jparams.get(3);
        if (elm.getAsJsonPrimitive().isNumber()) {
            return model.distance(v1, v2, op, elm.getAsInt());
        }
        IntVar v3 = ModelDeserializer.getIntVar(jparams.get(3).getAsString());
        return model.distance(v1, v2, op, v3);
    }

    private Constraint makeDivision(JsonArray jparams, Model model) {
        IntVar v1 = ModelDeserializer.getIntVar(jparams.get(0).getAsString());
        IntVar v2 = ModelDeserializer.getIntVar(jparams.get(1).getAsString());
        IntVar v3 = ModelDeserializer.getIntVar(jparams.get(2).getAsString());


        return model.div(v1, v2, v3);
    }

    private Constraint makeElement(JsonArray jparams, Model model) {
        IntVar v1 = ModelDeserializer.getIntVar(jparams.get(0).getAsString());
        JsonArray elm = jparams.get(1).getAsJsonArray();
        IntVar v2 = ModelDeserializer.getIntVar(jparams.get(2).getAsString());
        int o = jparams.get(3).getAsInt();
        if (elm.get(0).getAsJsonPrimitive().isNumber()) {
            int[] values = extractIntArray(jparams.get(1).getAsJsonArray());
            return model.element(v1, values, v2, o);
        } else {
            IntVar[] values = extractIntVarArray(jparams.get(1).getAsJsonArray());
            return model.element(v1, values, v2, o);
        }
    }

    private Constraint makeMax(JsonArray jparams, Model model) {
        IntVar v1 = ModelDeserializer.getIntVar(jparams.get(0).getAsString());
        IntVar[] v2 = extractIntVarArray(jparams.get(1).getAsJsonArray());
        return model.max(v1, v2);
    }

    private Constraint makeMin(JsonArray jparams, Model model) {
        IntVar v1 = ModelDeserializer.getIntVar(jparams.get(0).getAsString());
        IntVar[] v2 = extractIntVarArray(jparams.get(1).getAsJsonArray());
        return model.min(v1, v2);
    }

    private Constraint makeSquare(JsonArray jparams, Model model) {
        IntVar v1 = ModelDeserializer.getIntVar(jparams.get(0).getAsString());
        IntVar v2 = ModelDeserializer.getIntVar(jparams.get(1).getAsString());
        return model.square(v1, v2);
    }

    private Constraint makeScale(JsonArray jparams, Model model) {
        IntVar v1 = ModelDeserializer.getIntVar(jparams.get(0).getAsString());
        int f = jparams.get(1).getAsInt();
        IntVar v2 = ModelDeserializer.getIntVar(jparams.get(2).getAsString());
        return model.times(v1, f, v2);
    }

    private Constraint makeTimes(JsonArray jparams, Model model) {
        IntVar v1 = ModelDeserializer.getIntVar(jparams.get(0).getAsString());
        IntVar v2 = ModelDeserializer.getIntVar(jparams.get(1).getAsString());
        IntVar v3 = ModelDeserializer.getIntVar(jparams.get(2).getAsString());
        return model.times(v1, v2, v3);
    }


    private Constraint makeAlldifferent(JsonArray jparams, Model model) {
        return model.allDifferent(
                extractIntVarArray(jparams.get(1).getAsJsonArray()),
                jparams.get(0).getAsString()
        );
    }

    private Constraint makeAmong(JsonArray jparams, Model model) {
        return model.among(
                ModelDeserializer.getIntVar(jparams.get(0).getAsString()),
                extractIntVarArray(jparams.get(2).getAsJsonArray()),
                extractIntArray(jparams.get(1).getAsJsonArray()));
    }

    private Constraint makeAtLeastNValues(JsonArray jparams, Model model) {
        return model.atLeastNValues(
                extractIntVarArray(jparams.get(0).getAsJsonArray()),
                ModelDeserializer.getIntVar(jparams.get(1).getAsString()),
                jparams.get(2).getAsBoolean());
    }

    private Constraint makeAtMostNValues(JsonArray jparams, Model model) {
        return model.atMostNValues(
                extractIntVarArray(jparams.get(0).getAsJsonArray()),
                ModelDeserializer.getIntVar(jparams.get(1).getAsString()),
                jparams.get(2).getAsBoolean());
    }

    private Constraint makeBinPacking(JsonArray jparams, Model model) {
        return model.binPacking(
                extractIntVarArray(jparams.get(0).getAsJsonArray()),
                extractIntArray(jparams.get(1).getAsJsonArray()),
                extractIntVarArray(jparams.get(2).getAsJsonArray()),
                jparams.get(3).getAsInt()
        );
    }

    private Constraint makeBoolChanneling(JsonArray jparams, Model model) {
        return model.boolsIntChanneling(
                extractBoolVarArray(jparams.get(0).getAsJsonArray()),
                ModelDeserializer.getIntVar(jparams.get(1).getAsString()),
                jparams.get(2).getAsInt()
        );
    }

    private Constraint makeBitsChanneling(JsonArray jparams, Model model) {
        return model.bitsIntChanneling(
                extractBoolVarArray(jparams.get(1).getAsJsonArray()),
                ModelDeserializer.getIntVar(jparams.get(0).getAsString())
        );
    }

    private Constraint makeCircuit(JsonArray jparams, Model model) {
        return model.circuit(
                extractIntVarArray(jparams.get(0).getAsJsonArray()),
                jparams.get(1).getAsInt(),
                CircuitConf.valueOf(jparams.get(2).getAsString())
        );
    }

    private Constraint makeCount(JsonArray jparams, Model model) {
        if (jparams.get(0).getAsJsonPrimitive().isNumber()) {
            return model.count(
                    jparams.get(0).getAsInt(),
                    extractIntVarArray(jparams.get(1).getAsJsonArray()),
                    ModelDeserializer.getIntVar(jparams.get(2).getAsString())
            );
        } else {
            return model.count(
                    ModelDeserializer.getIntVar(jparams.get(2).getAsString()),
                    extractIntVarArray(jparams.get(1).getAsJsonArray()),
                    ModelDeserializer.getIntVar(jparams.get(0).getAsString())
            );
        }
    }

    private Constraint makeCumulative(JsonArray jparams, Model model) {
        IntVar[] s = extractIntVarArray(jparams.get(0).getAsJsonArray());
        IntVar[] d = extractIntVarArray(jparams.get(1).getAsJsonArray());
        IntVar[] e = extractIntVarArray(jparams.get(2).getAsJsonArray());
        Task[] tasks = new Task[s.length];
        for (int i = 0; i < s.length; i++) {
            tasks[i] = new Task(s[i], d[i], e[i]);
        }
        IntVar[] h = extractIntVarArray(jparams.get(3).getAsJsonArray());
        IntVar C = ModelDeserializer.getIntVar(jparams.get(4).getAsString());
        boolean inc = jparams.get(5).getAsBoolean();
        JsonArray jarray = jparams.get(6).getAsJsonArray();
        Cumulative.Filter[] filters = new Cumulative.Filter[jarray.size()];
        for (int i = 0; i < jarray.size(); i++) {
            filters[i] = Cumulative.Filter.valueOf(jarray.get(i).getAsString());
        }
        return model.cumulative(tasks, h, C, inc, filters);
    }

    private Constraint makeDiffn(JsonArray jparams, Model model) {
        IntVar[] x = extractIntVarArray(jparams.get(0).getAsJsonArray());
        IntVar[] y = extractIntVarArray(jparams.get(1).getAsJsonArray());
        IntVar[] w = extractIntVarArray(jparams.get(2).getAsJsonArray());
        IntVar[] h = extractIntVarArray(jparams.get(3).getAsJsonArray());
        return model.diffN(x, y, w, h, false);
    }

    private Constraint makeGcc(JsonArray jparams, Model model) {
        return model.globalCardinality(
                extractIntVarArray(jparams.get(0).getAsJsonArray()),
                extractIntArray(jparams.get(1).getAsJsonArray()),
                extractIntVarArray(jparams.get(2).getAsJsonArray()),
                false
        );
    }

    private Constraint makeInverseChanneling(JsonArray jparams, Model model) {
        return model.inverseChanneling(
                extractIntVarArray(jparams.get(0).getAsJsonArray()),
                extractIntVarArray(jparams.get(1).getAsJsonArray()),
                jparams.get(2).getAsInt(),
                jparams.get(3).getAsInt()
        );
    }

    private Constraint makeKnapsack(JsonArray jparams, Model model) {
        return model.knapsack(
                extractIntVarArray(jparams.get(0).getAsJsonArray()),
                ModelDeserializer.getIntVar(jparams.get(1).getAsString()),
                ModelDeserializer.getIntVar(jparams.get(2).getAsString()),
                extractIntArray(jparams.get(3).getAsJsonArray()),
                extractIntArray(jparams.get(4).getAsJsonArray())
        );
    }

    private Constraint makeLexchain(JsonArray jparams, Model model) {
        IntVar[] XS = extractIntVarArray(jparams.get(0).getAsJsonArray());
        int n = jparams.get(1).getAsInt();
        IntVar[][] vars = new IntVar[XS.length / n][];
        for (int i = 0; i < vars.length; i++) {
            vars[i] = Arrays.copyOfRange(XS, i * n, (i + 1) * n);
        }
        if (jparams.get(2).getAsBoolean()) {
            return model.lexChainLess(vars);
        } else {
            return model.lexChainLessEq(vars);
        }
    }


    private Constraint makeLex(JsonArray jparams, Model model) {
        if (jparams.get(2).getAsBoolean()) {
            return model.lexLess(
                    extractIntVarArray(jparams.get(0).getAsJsonArray()),
                    extractIntVarArray(jparams.get(1).getAsJsonArray()));
        } else {
            return model.lexLessEq(
                    extractIntVarArray(jparams.get(0).getAsJsonArray()),
                    extractIntVarArray(jparams.get(1).getAsJsonArray()));
        }
    }

    private Constraint makeNvalues(JsonArray jparams, Model model) {
        return model.nValues(
                extractIntVarArray(jparams.get(0).getAsJsonArray()),
                ModelDeserializer.getIntVar(jparams.get(1).getAsString()));
    }

    private Constraint makeRArithm(JsonArray jparams, Model model) {
        IntVar v1 = ModelDeserializer.getIntVar(jparams.get(0).getAsString());
        switch (jparams.size()) {
            case 4:
                if (jparams.get(2).getAsJsonPrimitive().isNumber()) {
                    switch (jparams.get(1).getAsString()) {
                        case "=":
                            model.reifyXeqC(
                                    v1,
                                    jparams.get(2).getAsInt(),
                                    ModelDeserializer.getBoolVar(jparams.get(3).getAsString())
                            );
                            break;
                        case ">":
                            model.reifyXgtC(
                                    v1,
                                    jparams.get(2).getAsInt(),
                                    ModelDeserializer.getBoolVar(jparams.get(3).getAsString())
                            );
                            break;
                        case "<":
                            model.reifyXltC(
                                    v1,
                                    jparams.get(2).getAsInt(),
                                    ModelDeserializer.getBoolVar(jparams.get(3).getAsString())
                            );
                            break;
                        case "!=":
                            model.reifyXneC(
                                    v1,
                                    jparams.get(2).getAsInt(),
                                    ModelDeserializer.getBoolVar(jparams.get(3).getAsString())
                            );
                            break;
                    }
                } else if (jparams.get(2).getAsString().startsWith("#")) {
                    switch (jparams.get(1).getAsString()) {
                        case "=":
                            model.reifyXeqY(
                                    v1,
                                    ModelDeserializer.getIntVar(jparams.get(2).getAsString()),
                                    ModelDeserializer.getBoolVar(jparams.get(3).getAsString())
                            );
                            break;
                        case "<":
                            model.reifyXltY(
                                    v1,
                                    ModelDeserializer.getIntVar(jparams.get(2).getAsString()),
                                    ModelDeserializer.getBoolVar(jparams.get(3).getAsString())
                            );
                            break;
                        case "!=":
                            model.reifyXneY(
                                    v1,
                                    ModelDeserializer.getIntVar(jparams.get(2).getAsString()),
                                    ModelDeserializer.getBoolVar(jparams.get(3).getAsString())
                            );
                            break;
                    }
                } else {
                    model.reifyXinS(
                            v1,
                            JSONHelper.convert(jparams.get(2).getAsString()),
                            ModelDeserializer.getBoolVar(jparams.get(3).getAsString())
                    );
                }
                break;
            case 5:
                model.reifyXltYC(
                        v1,
                        ModelDeserializer.getIntVar(jparams.get(2).getAsString()),
                        jparams.get(3).getAsInt(),
                        ModelDeserializer.getBoolVar(jparams.get(4).getAsString())
                );
                break;
        }
        return null;
    }

    private Constraint makeSum(JsonArray jparams, Model model) {
        IntVar[] vars = extractIntVarArray(jparams.get(0).getAsJsonArray());
        int p = jparams.get(1).getAsInt();
        int[] cs = new int[vars.length];
        Arrays.fill(cs, 0, p, 1);
        Arrays.fill(cs, p, vars.length, -1);
        String op = jparams.get(2).getAsString();
        int b = jparams.get(3).getAsInt();
        return model.scalar(vars, cs, op, b);
    }

    private Constraint makeScalar(JsonArray jparams, Model model) {
        IntVar[] vars = extractIntVarArray(jparams.get(0).getAsJsonArray());
        int[] cs = extractIntArray(jparams.get(1).getAsJsonArray());
        String op = jparams.get(3).getAsString();
        int b = jparams.get(4).getAsInt();
        return model.scalar(vars, cs, op, b);
    }

    private Constraint makeSubcircuit(JsonArray jparams, Model model) {
        IntVar[] vars = extractIntVarArray(jparams.get(0).getAsJsonArray());
        IntVar var = ModelDeserializer.getIntVar(jparams.get(1).getAsString());
        int offset = jparams.get(2).getAsInt();
        return model.subCircuit(vars, offset, var);
    }

    private Constraint makeTable(JsonArray jparams, Model model) {
        IntVar[] vars = extractIntVarArray(jparams.get(0).getAsJsonArray());
        Tuples tuples = new Tuples(jparams.get(2).getAsBoolean());
        JsonArray array = jparams.get(1).getAsJsonArray();
        for (int i = 0; i < array.size(); i++) {
            int[] tt = extractIntArray(array.get(i).getAsJsonArray());
            tuples.add(tt);
        }
        if (vars.length == 2) {
            return model.table(vars[0], vars[1], tuples, jparams.get(3).getAsString());
        } else {
            return model.table(vars, tuples, jparams.get(3).getAsString());
        }
    }

    private Constraint makeTree(JsonArray jparams, Model model) {
        IntVar[] vars = extractIntVarArray(jparams.get(0).getAsJsonArray());
        IntVar var = ModelDeserializer.getIntVar(jparams.get(1).getAsString());
        int offset = jparams.get(2).getAsInt();
        return model.tree(vars, var, offset);
    }

    private Constraint makeSetAlldifferent(JsonArray jparams, Model model) {
        return model.allDifferent(extractSetVarArray(jparams.get(0).getAsJsonArray()));
    }

    private Constraint makeSetAlldisjoint(JsonArray jparams, Model model) {
        return model.allDisjoint(extractSetVarArray(jparams.get(0).getAsJsonArray()));
    }

    private Constraint makeSetAllequal(JsonArray jparams, Model model) {
        return model.allEqual(extractSetVarArray(jparams.get(0).getAsJsonArray()));
    }

    private Constraint makeSetBoolChanneling(JsonArray jparams, Model model) {
        return model.setBoolsChanneling(
                extractBoolVarArray(jparams.get(0).getAsJsonArray()),
                ModelDeserializer.getSetVar(jparams.get(1).getAsString()),
                jparams.get(2).getAsInt());
    }

    private Constraint makeSetCard(JsonArray jparams, Model model) {
        ModelDeserializer.getSetVar(jparams.get(0).getAsString()).setCard(
                ModelDeserializer.getIntVar(jparams.get(1).getAsString())
        );
        return null;
    }

    private Constraint makeSetElement(JsonArray jparams, Model model) {
        return model.element(
                ModelDeserializer.getIntVar(jparams.get(0).getAsString()),
                extractSetVarArray(jparams.get(1).getAsJsonArray()),
                jparams.get(3).getAsInt(),
                ModelDeserializer.getSetVar(jparams.get(2).getAsString())
        );
    }

    private Constraint makeSetIntChanneling(JsonArray jparams, Model model) {
        return model.setsIntsChanneling(
                extractSetVarArray(jparams.get(0).getAsJsonArray()),
                extractIntVarArray(jparams.get(1).getAsJsonArray()),
                jparams.get(2).getAsInt(),
                jparams.get(3).getAsInt()
        );
    }

    private Constraint makeSetIntersection(JsonArray jparams, Model model) {
        return model.intersection(
                extractSetVarArray(jparams.get(0).getAsJsonArray()),
                ModelDeserializer.getSetVar(jparams.get(1).getAsString()),
                jparams.get(2).getAsBoolean()
        );
    }

    private Constraint makeSetIntUnion(JsonArray jparams, Model model) {
        return model.union(
                extractIntVarArray(jparams.get(0).getAsJsonArray()),
                ModelDeserializer.getSetVar(jparams.get(1).getAsString())
        );
    }

    private Constraint makeSetInverse(JsonArray jparams, Model model) {
        return model.inverseSet(
                extractSetVarArray(jparams.get(0).getAsJsonArray()),
                extractSetVarArray(jparams.get(1).getAsJsonArray()),
                jparams.get(2).getAsInt(),
                jparams.get(3).getAsInt()
        );
    }

    private Constraint makeSetMax(JsonArray jparams, Model model) {
        return model.max(
                ModelDeserializer.getSetVar(jparams.get(0).getAsString()),
                jparams.get(2).isJsonNull() ? null : extractIntArray(jparams.get(2).getAsJsonArray()),
                jparams.get(3).getAsInt(),
                ModelDeserializer.getIntVar(jparams.get(1).getAsString()),
                jparams.get(4).getAsBoolean()
        );
    }

    private Constraint makeSetMember(JsonArray jparams, Model model) {
        if (jparams.get(1).getAsJsonPrimitive().isNumber()) {
            return model.member(jparams.get(1).getAsInt(),
                    ModelDeserializer.getSetVar(jparams.get(0).getAsString())
            );
        } else {
            return model.member(ModelDeserializer.getIntVar(jparams.get(1).getAsString()),
                    ModelDeserializer.getSetVar(jparams.get(0).getAsString())
            );
        }
    }

    private Constraint makeSetMin(JsonArray jparams, Model model) {
        return model.min(
                ModelDeserializer.getSetVar(jparams.get(0).getAsString()),
                jparams.get(2).isJsonNull() ? null : extractIntArray(jparams.get(2).getAsJsonArray()),
                jparams.get(3).getAsInt(),
                ModelDeserializer.getIntVar(jparams.get(1).getAsString()),
                jparams.get(4).getAsBoolean()
        );
    }

    private Constraint makeSetNbEmpty(JsonArray jparams, Model model) {
        return model.nbEmpty(
                extractSetVarArray(jparams.get(0).getAsJsonArray()),
                ModelDeserializer.getIntVar(jparams.get(1).getAsString())
        );
    }

    private Constraint makeSetNotEmpty(JsonArray jparams, Model model) {
        return model.notEmpty(
                ModelDeserializer.getSetVar(jparams.get(0).getAsString())
        );
    }

    private Constraint makeSetNotMember(JsonArray jparams, Model model) {
        if (jparams.get(1).getAsJsonPrimitive().isNumber()) {
            return model.notMember(jparams.get(1).getAsInt(),
                    ModelDeserializer.getSetVar(jparams.get(0).getAsString())
            );
        } else {
            return model.notMember(ModelDeserializer.getIntVar(jparams.get(1).getAsString()),
                    ModelDeserializer.getSetVar(jparams.get(0).getAsString())
            );
        }
    }

    private Constraint makeSetOffset(JsonArray jparams, Model model) {
        return model.offSet(
                ModelDeserializer.getSetVar(jparams.get(0).getAsString()),
                ModelDeserializer.getSetVar(jparams.get(1).getAsString()),
                jparams.get(2).getAsInt()
        );
    }

    private Constraint makeSetPartition(JsonArray jparams, Model model) {
        return model.partition(
                extractSetVarArray(jparams.get(0).getAsJsonArray()),
                ModelDeserializer.getSetVar(jparams.get(1).getAsString())
        );
    }

    private Constraint makeSetSubSetEq(JsonArray jparams, Model model) {
        return model.subsetEq(
                extractSetVarArray(jparams.get(0).getAsJsonArray())
        );
    }

    private Constraint makeSetSum(JsonArray jparams, Model model) {
        return model.sumElements(
                ModelDeserializer.getSetVar(jparams.get(0).getAsString()),
                jparams.get(2).isJsonNull() ? null : extractIntArray(jparams.get(2).getAsJsonArray()),
                jparams.get(3).getAsInt(),
                ModelDeserializer.getIntVar(jparams.get(1).getAsString())
        );
    }

    private Constraint makeSetSymmetric(JsonArray jparams, Model model) {
        return model.symmetric(
                extractSetVarArray(jparams.get(0).getAsJsonArray()),
                jparams.get(1).getAsInt()
        );
    }

    private Constraint makeSetUnion(JsonArray jparams, Model model) {
        return model.union(
                extractSetVarArray(jparams.get(0).getAsJsonArray()),
                ModelDeserializer.getSetVar(jparams.get(1).getAsString())
        );
    }


}
