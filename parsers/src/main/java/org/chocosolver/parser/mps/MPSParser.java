/*
 * This file is part of choco-parsers, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.parser.mps;

import org.chocosolver.parser.ParserException;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.Operator;
import org.chocosolver.solver.constraints.real.PropScalarMixed;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.util.tools.ArrayUtils;
import org.chocosolver.util.tools.VariableUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

/**
 * <p> Project: choco-parsers.
 *
 * @author Charles Prud'homme
 * @since 24/01/2018.
 */
public class MPSParser {

    private static final Pattern twopart = Pattern.compile("");

    private static final String TAG_NAME = "NAME";
    private static final String TAG_ROWS = "ROWS";
    private static final String TAG_COLUMNS = "COLUMNS";
    private static final String TAG_RHS = "RHS";
    private static final String TAG_BOUNDS = "BOUNDS"; // optional
    private static final String TAG_RANGES = "RANGES"; // optional
    private static final String TAG_ENDATA = "ENDATA";
    private static final String TAG_MARKER = "'MARKER'";
    private static final String TAG_INTORG = "'INTORG'";

    private static final int CACHING = 12 * 5;

    private HashMap<String, String> ope4eq;
    private HashMap<String, List<Number>> coeffs4eq;
    private HashMap<String, ArrayList<String>> vars4eq;
    private HashMap<String, Number> rhs4eq;
    private HashMap<String, Number> range4eq;
    private List<String> allvars;
    private HashMap<String, Variable> decVars;
    private HashMap<String, Boolean> varsIsInt;
    private HashMap<String, Number[]> varsDom;

    private double POS_INF;

    private double NEG_INF;

    public void model(Model model, String instance, boolean maximize,
                      double ninf, double pinf,
                      boolean ibex,
                      boolean noeq) throws IOException {
        ope4eq = new HashMap<>();
        coeffs4eq = new HashMap<>();
        vars4eq = new HashMap<>();
        rhs4eq = new HashMap<>();
        range4eq = new HashMap<>();
        decVars = new HashMap<>();
        allvars = new ArrayList<>();
        varsIsInt = new HashMap<>();
        varsDom = new HashMap<>();

        this.POS_INF = pinf;
        this.NEG_INF = ninf;

        Reader reader = null;
        GZIPInputStream gzis = null;
        if (instance.endsWith("mps.gz")) {
            FileInputStream fin = new FileInputStream(instance);
            gzis = new GZIPInputStream(fin);
            reader = new InputStreamReader(gzis);
        } else {
            reader = new FileReader(instance);
        }
        try (BufferedReader br = new BufferedReader(reader)) {
            readName(br);
            readRows(br);
            readColumns(br);
            readRHS(br);
            // optional tags
            br.reset();
            String line = br.readLine();
            while (line != null && !line.startsWith(TAG_ENDATA)) {
                if (line.startsWith(TAG_RANGES)) {
                    readRanges(br);
                }
                if (line.startsWith(TAG_BOUNDS)) {
                    readBounds(br);
                }
                br.mark(CACHING);
                line = br.readLine();
            }
        }
        build(model, maximize, ibex, noeq);
        reader.close();
        if(gzis != null) {
            gzis.close();
        }
    }

    private void readName(BufferedReader br) throws IOException {
        String line = br.readLine();
        // read NAME
        while (line != null && !line.startsWith(TAG_NAME)) {
            line = br.readLine();
        }
        if (line == null) {
            throw new ParserException("No tag \"NAME\" found");
        }
        br.mark(CACHING);
        line = br.readLine();
        assert line.startsWith(TAG_ROWS);
    }

    private void readRows(BufferedReader br) throws IOException {
        br.reset();
        String line = br.readLine();
        if (line == null || !line.startsWith(TAG_ROWS)) {
            throw new ParserException("No tag \"ROWS\" found");
        }
        line = br.readLine();
        String[] values;
        // read the next one, which contains the name of the first constraint
        while (line != null && !line.startsWith(TAG_COLUMNS)) {
            // Fields start in column 2, 5, 15, 25, 40 and 50
            values = Arrays.stream(line.split(" ")).filter(v -> v.length() > 0).toArray(String[]::new);
            switch (values[0]) {
                case "N":
                    break;
                case "E":
                    ope4eq.put(values[1], "=");
                    break;
                case "L":
                    ope4eq.put(values[1], "<=");
                    break;
                case "G":
                    ope4eq.put(values[1], ">=");
                    break;
                default:
                    throw new ParserException("Unknown identifier \"" + values[0] + "\"");
            }
            br.mark(CACHING);
            line = br.readLine();
        }
    }

    private void readColumns(BufferedReader br) throws IOException {
        br.reset();
        String line = br.readLine();
        if (line == null || !line.startsWith(TAG_COLUMNS)) {
            throw new ParserException("No tag \"COLUMNS\" found");
        }
        line = br.readLine();
        boolean isInt = false;
        String[] values;
        // read the next one, which contains the first instruction
        while (line != null && !line.startsWith(TAG_RHS)) {
            values = Arrays.stream(line.split(" ")).filter(v -> v.length() > 0).toArray(String[]::new);
            if (values[1].equals(TAG_MARKER)) {
                isInt = values[2].equals(TAG_INTORG);
            } else {
                if (!decVars.containsKey(values[0])) {
                    decVars.put(values[0], null);
                    allvars.add(values[0]);
                }
                addElement(values[1], values[0], values[2], isInt);
                if (values.length == 5) {
                    addElement(values[3], values[0], values[4], isInt);
                }
            }
            br.mark(CACHING);
            line = br.readLine();
        }
    }

    private void addElement(String cnam, String vnam, String coeff, boolean isInt) {
        List<Number> coeffs = coeffs4eq.get(cnam);
        ArrayList<String> vars = vars4eq.get(cnam);
        if (coeffs == null) {
            coeffs = new ArrayList<>();
            vars = new ArrayList<>();
            coeffs4eq.put(cnam, coeffs);
            vars4eq.put(cnam, vars);
        }
        if (coeff.matches("-?\\d+")) {
            coeffs.add(Integer.parseInt(coeff));
        } else {
            coeffs.add(Double.parseDouble(coeff));
        }
        vars.add(vnam);
        Boolean vint = varsIsInt.get(vnam);
        if (vint == null) {
            vint = isInt;
            varsIsInt.put(vnam, vint);
        } else if (vint != isInt) {
            throw new ParserException("Incorrect type found");
        }
    }

    private void readRHS(BufferedReader br) throws IOException {
        br.reset();
        String line = br.readLine();
        if (line == null || !line.startsWith(TAG_RHS)) {
            throw new ParserException("No tag \"RHS\" found");
        }
        line = br.readLine();
        String[] values;
        while (line != null
                && !line.startsWith(TAG_RANGES)
                && !line.startsWith(TAG_BOUNDS)
                && !line.startsWith(TAG_ENDATA)) {
            values = Arrays.stream(line.split(" ")).filter(v -> v.length() > 0).toArray(String[]::new);
            addRhs(values[1], values[2]);
            if (values.length == 5) {
                addRhs(values[3], values[4]);
            }
            br.mark(CACHING);
            line = br.readLine();
        }
    }

    private void addRhs(String cnam, String rhsv) {
        Number rhs = rhs4eq.get(cnam);
        if (rhs == null) {
            if (rhsv.matches("-?\\d+")) {
                rhs = Integer.parseInt(rhsv);
            } else {
                rhs = Double.parseDouble(rhsv);
            }
            rhs4eq.put(cnam, rhs);
        }
    }

    private void readRanges(BufferedReader br) throws IOException {
        br.reset();
        String line = br.readLine();
        if (line == null || !line.startsWith(TAG_RANGES)) {
            throw new ParserException("No tag \"RANGES\" found");
        }
        line = br.readLine();
        String[] values;
        while (line != null
                && !line.startsWith(TAG_BOUNDS)
                && !line.startsWith(TAG_ENDATA)) {
            values = Arrays.stream(line.split(" ")).filter(v -> v.length() > 0).toArray(String[]::new);
            addRange(values[1], values[2]);
            if (values.length > 3) {
                addRange(values[3], values[4]);
            }
            br.mark(CACHING);
            line = br.readLine();
        }
    }

    private void addRange(String cnam, String range) {
        Number rng = range4eq.get(cnam);
        if (rng == null) {
            if (range.matches("-?\\d+")) {
                rng = Integer.parseInt(range);
            } else {
                rng = Double.parseDouble(range);
            }
            range4eq.put(cnam, rng);
        }
    }

    private void readBounds(BufferedReader br) throws IOException {
        br.reset();
        String line = br.readLine();
        if (line == null || !line.startsWith(TAG_BOUNDS)) {
            throw new ParserException("No tag \"BOUNDS\" found");
        }
        line = br.readLine();
        String[] values;
        while (line != null
                && !line.startsWith(TAG_ENDATA)) {
            values = Arrays.stream(line.split(" ")).filter(v -> v.length() > 0).toArray(String[]::new);
            String var = values[2];
            Number[] bounds = varsDom.get(var);
            if (bounds == null) {
                bounds = new Number[]{0, POS_INF};
                varsDom.put(var, bounds);
            }
            String val = values.length > 3 ? values[3] : "--";
            switch (values[0]) {
                case "LO":
                    if (val.matches("-?\\d+")) {
                        bounds[0] = Integer.parseInt(val);
                    } else {
                        bounds[0] = Double.parseDouble(val);
                    }
                    break;
                case "UP":
                    if (val.matches("-?\\d+")) {
                        bounds[1] = Integer.parseInt(val);
                    } else {
                        bounds[1] = Double.parseDouble(val);
                    }
                    break;
                case "FX":
                    if (val.matches("-?\\d+")) {
                        bounds[0] = bounds[1] = Integer.parseInt(val);
                    } else {
                        bounds[0] = bounds[1] = Double.parseDouble(val);
                    }
                    break;
                case "FR":
                    bounds[0] = NEG_INF;
                    bounds[1] = POS_INF;
                    break;
                case "MI":
                    bounds[0] = NEG_INF;
                    bounds[1] = 0;
                    break;
                case "PL":
                    bounds[0] = 0;
                    bounds[1] = POS_INF;
                    break;
                case "BV":
                    varsIsInt.put(var, true);
                    bounds[0] = 0;
                    bounds[1] = 1;
                    break;
                case "UI":
                    varsIsInt.put(var, true);
                    if (val.matches("-?\\d+")) {
                        bounds[1] = Integer.parseInt(val);
                    } else {
                        throw new ParserException("UI refers to double instead of int");
                    }
                    break;
                case "LI":
                    varsIsInt.put(var, true);
                    if (val.matches("-?\\d+")) {
                        bounds[0] = Integer.parseInt(val);
                    } else {
                        throw new ParserException("LI refers to double instead of int");
                    }
                    break;
                case "SC":
                    throw new ParserException("semi-continuous not supported");
                default:
                    throw new ParserException("Unknown identifier \"" + values[0] + "\"");
            }
            br.mark(CACHING);
            line = br.readLine();
        }
    }


    private void build(Model model, boolean maximize, boolean ibex, boolean noeq) {
        // First, create variables
        for (int i = 0; i < allvars.size(); i++) {
            String vnam = allvars.get(i);
            Number[] bounds = varsDom.get(vnam);
            if (varsIsInt.get(vnam)) {
                if (bounds == null) {
                    bounds = new Number[]{0, POS_INF};
                }
                IntVar ivar = model.intVar(vnam, bounds[0].intValue(), bounds[1].intValue());
                decVars.put(vnam, ivar);
            } else {
                if (bounds == null) {
                    bounds = new Number[]{0, POS_INF};
                }
                RealVar rvar = model.realVar(vnam, bounds[0].doubleValue(), bounds[1].doubleValue(), model.getPrecision());
                decVars.put(vnam, rvar);
            }
        }
        // then create constraints
        String[] cnames = vars4eq.keySet().toArray(new String[vars4eq.size()]);
        Arrays.sort(cnames); // preserve order for determinism
        boolean foundObj = false;
        boolean unimod = true;
        for (int i = 0; i < cnames.length; i++) {
            String cnam = cnames[i];
            List<Number> coefs = coeffs4eq.get(cnam);
            unimod &= coefs.stream().allMatch(n -> (n.getClass() == Integer.class) && Math.abs(n.intValue()) == 1);
            String op = ope4eq.get(cnam);
            Number rhs = rhs4eq.get(cnam);
            Number rng = range4eq.get(cnam);
            if (rhs == null) rhs = 0;
            List<String> vars = vars4eq.get(cnam);
            if (vars.stream().allMatch(v -> varsIsInt.get(v))
                    && coefs.stream().allMatch(n -> n.getClass() == Integer.class)
                    && rhs.getClass() == Integer.class
                    && (rng == null || rng.getClass() == Integer.class)) {
                if (op == null) {
                    foundObj = postIntObectiveFunction(model, vars, coefs, rhs, rng, maximize, foundObj, noeq);
                } else {
                    postIntEquation(model, vars, coefs, op, rhs, rng, noeq);
                }
            } else {
                if (ibex) {
                    // ibex function
                    if (op == null) {
                        // objective function
                        foundObj = postIbexObjectiveFunction(model, vars, coefs, rhs, rng, maximize, foundObj);
                    } else {
                        postIbexEquation(model, vars, coefs, op, rhs, rng);
                    }
                } else {
                    if (op == null) {
                        foundObj = postObjectiveFunction(model, vars, coefs, rhs, rng, maximize, foundObj);
                    } else {
                        postEquation(model, vars, coefs, op, rhs, rng);
                    }
                }
            }
        }
        model.getSolver().log().white().printf("c Unimodular: %s\n", unimod);
    }

    private void postIntEquation(Model model, List<String> vars, List<Number> coefs, String op,
                                 Number rhs, Number rng, boolean noeq) {
        switch (op) {
            case "=":
                if (rng == null) {
                    // only made of int var, and all coeffs are int
                    if(noeq) {
                        model.scalar(vars.stream().map(s -> (IntVar) decVars.get(s)).toArray(IntVar[]::new),
                                coefs.stream().mapToInt(Number::intValue).toArray(), "<=", rhs.intValue()
                        ).post();
                        model.scalar(vars.stream().map(s -> (IntVar) decVars.get(s)).toArray(IntVar[]::new),
                                coefs.stream().mapToInt(Number::intValue).toArray(), ">=", rhs.intValue()
                        ).post();
                    }else{
                        model.scalar(vars.stream().map(s -> (IntVar) decVars.get(s)).toArray(IntVar[]::new),
                                coefs.stream().mapToInt(Number::intValue).toArray(), op, rhs.intValue()
                        ).post();
                    }
                } else {
                    if (rng.intValue() > 0) {
                        model.scalar(vars.stream().map(s -> (IntVar) decVars.get(s)).toArray(IntVar[]::new),
                                coefs.stream().mapToInt(Number::intValue).toArray(), ">=", rhs.intValue()
                        ).post();
                        model.scalar(vars.stream().map(s -> (IntVar) decVars.get(s)).toArray(IntVar[]::new),
                                coefs.stream().mapToInt(Number::intValue).toArray(), "<=",
                                rhs.intValue() + rng.intValue()
                        ).post();
                    } else {
                        model.scalar(vars.stream().map(s -> (IntVar) decVars.get(s)).toArray(IntVar[]::new),
                                coefs.stream().mapToInt(Number::intValue).toArray(), "<=", rhs.intValue()
                        ).post();
                        model.scalar(vars.stream().map(s -> (IntVar) decVars.get(s)).toArray(IntVar[]::new),
                                coefs.stream().mapToInt(Number::intValue).toArray(), ">=",
                                rhs.intValue() + rng.intValue()
                        ).post();
                    }
                }
                break;
            default:
                // only made of int var, and all coeffs are int
                model.scalar(
                        vars.stream()
                                .map(s -> (IntVar) decVars.get(s))
                                .toArray(IntVar[]::new),
                        coefs.stream()
                                .mapToInt(Number::intValue)
                                .toArray(),
                        op,
                        rhs.intValue()
                ).post();
                if (rng != null) {
                    String nop = ">=";
                    int b = rhs.intValue();
                    if (op.equals(">=")) {
                        nop = "<=";
                        b += Math.abs(rng.intValue());
                    } else {
                        b -= Math.abs(rng.intValue());
                    }
                    model.scalar(
                            vars.stream()
                                    .map(s -> (IntVar) decVars.get(s))
                                    .toArray(IntVar[]::new),
                            coefs.stream()
                                    .mapToInt(Number::intValue)
                                    .toArray(),
                            nop,
                            b
                    ).post();
                }

                break;
        }
    }

    private boolean postIntObectiveFunction(Model model, List<String> vars, List<Number> coefs,
                                            Number rhs, Number rng,
                                            boolean maximize, boolean foundObj, boolean noeq) {
        if (foundObj) {
            throw new ParserException("More than one objective function found");
        } else if (rng != null) {
            throw new ParserException("Range found for objective function");
        } else {
            IntVar objective = model.intVar("OBJ", (int) Math.ceil(NEG_INF), (int) Math.floor(POS_INF));
            model.setObjective(maximize, objective);
            IntVar[] ivars = vars.stream().map(s -> (IntVar) decVars.get(s)).toArray(IntVar[]::new);
            if(noeq) {
                model.scalar(
                        ArrayUtils.append(ivars, new IntVar[]{objective}),
                        ArrayUtils.concat(coefs.stream()
                                .mapToInt(Number::intValue)
                                .toArray(), -1),
                        "<=",
                        rhs.intValue()
                ).post();
                model.scalar(
                        ArrayUtils.append(ivars, new IntVar[]{objective}),
                        ArrayUtils.concat(coefs.stream()
                                .mapToInt(Number::intValue)
                                .toArray(), -1),
                        ">=",
                        rhs.intValue()
                ).post();
            }else{
                model.scalar(
                        ArrayUtils.append(ivars, new IntVar[]{objective}),
                        ArrayUtils.concat(coefs.stream()
                                .mapToInt(Number::intValue)
                                .toArray(), -1),
                        "=",
                        rhs.intValue()
                ).post();
            }
        }
        return true;
    }

    private static Constraint mixedScalar(Variable[] vars, double[] coefs, String op, double b) {
        return new Constraint("MIXEDSCALAR",
                new PropScalarMixed(vars, coefs, Operator.get(op), b));
    }


    private void postEquation(Model model, List<String> vars, List<Number> coefs, String op,
                              Number rhs, Number rng) {
        switch (op) {
            case "=":
                if (rng == null) {
                    // only made of int var, and all coeffs are int
                    mixedScalar(vars.stream().map(s -> decVars.get(s)).toArray(Variable[]::new),
                            coefs.stream().mapToDouble(Number::doubleValue).toArray(), op, rhs.doubleValue()
                    ).post();
                } else {
                    if (rng.intValue() > 0) {
                        mixedScalar(vars.stream().map(s -> decVars.get(s)).toArray(Variable[]::new),
                                coefs.stream().mapToDouble(Number::doubleValue).toArray(), ">=", rhs.doubleValue()
                        ).post();
                        mixedScalar(vars.stream().map(s -> decVars.get(s)).toArray(Variable[]::new),
                                coefs.stream().mapToDouble(Number::doubleValue).toArray(), "<=",
                                rhs.doubleValue() + rng.doubleValue()
                        ).post();
                    } else {
                        mixedScalar(vars.stream().map(s -> decVars.get(s)).toArray(Variable[]::new),
                                coefs.stream().mapToDouble(Number::doubleValue).toArray(), "<=", rhs.doubleValue()
                        ).post();
                        mixedScalar(vars.stream().map(s -> decVars.get(s)).toArray(Variable[]::new),
                                coefs.stream().mapToDouble(Number::doubleValue).toArray(), ">=",
                                rhs.doubleValue() + rng.doubleValue()
                        ).post();
                    }
                }
                break;
            default:
                // only made of int var, and all coeffs are int
                mixedScalar(
                        vars.stream()
                                .map(s -> decVars.get(s))
                                .toArray(Variable[]::new),
                        coefs.stream()
                                .mapToDouble(Number::doubleValue)
                                .toArray(),
                        op,
                        rhs.doubleValue()
                ).post();
                if (rng != null) {
                    String nop = ">=";
                    double b = rhs.doubleValue();
                    if (op.equals(">=")) {
                        nop = "<=";
                        b += Math.abs(rng.doubleValue());
                    } else {
                        b -= Math.abs(rng.doubleValue());
                    }
                    mixedScalar(
                            vars.stream()
                                    .map(s -> decVars.get(s))
                                    .toArray(Variable[]::new),
                            coefs.stream()
                                    .mapToDouble(Number::doubleValue)
                                    .toArray(),
                            nop,
                            b
                    ).post();
                }
                break;
        }
    }

    private boolean postObjectiveFunction(Model model, List<String> vars, List<Number> coefs,
                                          Number rhs, Number rng,
                                          boolean maximize, boolean foundObj) {
        if (foundObj) {
            throw new ParserException("More than one objective function found");
        } else if (rng != null) {
            throw new ParserException("Range found for objective function");
        } else {
            RealVar objective = model.realVar("OBJ", NEG_INF, POS_INF, model.getPrecision());
            model.setObjective(maximize, objective);
            Variable[] svars = vars.stream().map(s -> decVars.get(s)).toArray(Variable[]::new);
            coefs.add(-1d);
            mixedScalar(
                    ArrayUtils.append(svars, new RealVar[]{objective}),
                    coefs.stream().mapToDouble(Number::doubleValue)
                            .toArray(),
                    "=",
                    rhs.doubleValue()).post();
            coefs.remove(coefs.size() - 1);
        }
        return true;
    }

    private void postIbexEquation(Model model, List<String> vars, List<Number> coefs, String op,
                                  Number rhs, Number rng) {
        StringBuilder fct = new StringBuilder();
        for (int j = 0; j < vars.size(); j++) {
            if (j > 0) fct.append('+');
            fct.append('{').append(j).append('}').append("*").append(coefs.get(j).doubleValue());
        }
        switch (op) {
            case "=":
                if (rng == null) {
                    fct.append(op).append(rhs.doubleValue());
                    model.realIbexGenericConstraint(fct.toString(), vars.stream()
                            .map(s -> decVars.get(s))
                            .toArray(Variable[]::new)).post();
                } else {
                    if (rng.intValue() > 0) {
                        model.realIbexGenericConstraint(fct + ">=" + rhs.doubleValue(),
                                vars.stream().map(s -> decVars.get(s)).toArray(Variable[]::new)).post();
                        model.realIbexGenericConstraint(fct + "<=" +
                                        rhs.doubleValue() + rng.doubleValue(),
                                vars.stream().map(s -> decVars.get(s)).toArray(Variable[]::new)).post();
                    } else {
                        model.realIbexGenericConstraint(fct + "<=" + rhs.doubleValue(),
                                vars.stream().map(s -> decVars.get(s)).toArray(Variable[]::new)).post();
                        model.realIbexGenericConstraint(fct + ">=" +
                                        rhs.doubleValue() + rng.doubleValue(),
                                vars.stream().map(s -> decVars.get(s)).toArray(Variable[]::new)).post();
                    }
                }
                break;
            default:
                fct.append(op).append(rhs.doubleValue());
                model.realIbexGenericConstraint(fct.toString(), vars.stream()
                        .map(s -> decVars.get(s))
                        .toArray(Variable[]::new)).post();
                if (rng != null) {
                    String nop = ">=";
                    double b = rhs.doubleValue();
                    if (op.equals(">=")) {
                        nop = "<=";
                        b += Math.abs(rng.doubleValue());
                    } else {
                        b -= Math.abs(rng.doubleValue());
                    }
                    fct.append(nop).append(b);
                    model.realIbexGenericConstraint(fct.toString(), vars.stream()
                            .map(s -> decVars.get(s))
                            .toArray(Variable[]::new)).post();
                }
                break;
        }

    }

    private boolean postIbexObjectiveFunction(Model model, List<String> vars, List<Number> coefs,
                                              Number rhs, Number rng,
                                              boolean maximize, boolean foundObj) {
        if (foundObj) {
            throw new ParserException("More than one objective function found");
        } else if (rng != null) {
            throw new ParserException("Range found for objective function");
        } else {
            StringBuilder fct = new StringBuilder();
            for (int j = 0; j < vars.size(); j++) {
                if (j > 0) fct.append('+');
                fct.append('{').append(j).append('}').append("*").append(coefs.get(j).doubleValue());
            }
            RealVar objective = model.realVar("OBJ", NEG_INF, POS_INF, model.getPrecision());
            model.setObjective(maximize, objective);
            fct.append('=').append('{').append(vars.size()).append('}').append('+').append(rhs.doubleValue());
            Variable[] svars = vars.stream().map(s -> decVars.get(s)).toArray(Variable[]::new);
            model.realIbexGenericConstraint(fct.toString(),
                    ArrayUtils.append(svars, new Variable[]{objective})).post();
        }
        return true;
    }


    public String printSolution() {
        StringBuilder st = new StringBuilder();
        for (int i = 0; i < allvars.size(); i++) {
            String vnam = allvars.get(i);
            st.append(vnam).append('\t');
            Variable var = decVars.get(vnam);
            if (VariableUtils.isReal(var)) {
                st.append(var.asRealVar().getLB());
            } else {
                st.append(var.asIntVar().getLB());
            }
            st.append('\n');
        }
        return st.toString();
    }
}


