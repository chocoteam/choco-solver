/*
 * This file is part of choco-parsers, http://choco-solver.org/
 *
 * Copyright (c) 2019, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.writer.constraints.nary;

import org.chocosolver.solver.Identity;
import org.chocosolver.solver.constraints.Operator;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.binary.PropNotEqualX_Y;
import org.chocosolver.solver.constraints.extension.Tuples;
import org.chocosolver.solver.constraints.extension.binary.PropBinCSP;
import org.chocosolver.solver.constraints.extension.nary.PropLargeCSP;
import org.chocosolver.solver.constraints.nary.alldifferent.*;
import org.chocosolver.solver.constraints.nary.circuit.CircuitConf;
import org.chocosolver.solver.constraints.nary.count.PropCountVar;
import org.chocosolver.solver.constraints.nary.cumulative.CumulFilter;
import org.chocosolver.solver.constraints.nary.cumulative.PropGraphCumulative;
import org.chocosolver.solver.constraints.nary.sum.PropScalar;
import org.chocosolver.solver.constraints.ternary.PropXplusYeqZ;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.writer.util.Reflection;
import org.chocosolver.writer.constraints.ConstraintWriter;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * <p> Project: choco-json.
 *
 * @author Charles Prud'homme
 * @since 22/09/2017.
 */
public class NaryWriterHelper {

    private NaryWriterHelper() {
    }

    public static void writeAlldifferent(ConstraintWriter writer, Propagator... props) throws IOException {
        String consistency = AllDifferent.NEQS;
        switch (props.length) {
            case 1:
                if (props[0] instanceof PropAllDiffInst) {
                    consistency = AllDifferent.FC;
                } else if (props[0] instanceof PropNotEqualX_Y) {
                    consistency = AllDifferent.NEQS;
                }
                break;
            case 2:
                if (props[1] instanceof PropAllDiffBC) {
                    consistency = AllDifferent.BC;
                } else if (props[1] instanceof PropAllDiffAC) {
                    consistency = AllDifferent.AC;
                } else if (props[1] instanceof PropNotEqualX_Y) {
                    consistency = AllDifferent.NEQS;
                }
                break;
            case 3:
                if (props[2] instanceof PropAllDiffAdaptative) {
                    consistency = AllDifferent.DEFAULT;
                } else if (props[2] instanceof PropNotEqualX_Y) {
                    consistency = AllDifferent.NEQS;
                }
                break;
        }
        writer.writeAlldifferent(consistency,
                Arrays.stream(props[0].getVars())
                        .mapToInt(Identity::getId)
                        .toArray());
    }

    public static void writeAmong(ConstraintWriter writer, Propagator prop) throws IOException {
        int nv = prop.getNbVars();
        writer.writeAmong(
                prop.getVar(nv - 1).getId(),
                Reflection.getObj(prop, "values"),
                IntStream.range(0, nv - 1).map(i -> prop.getVar(i).getId()).toArray()
        );
    }

    public static void writeAtleastnvalues(ConstraintWriter writer, Propagator[] propagators) throws IOException {
        int nv = propagators[0].getNbVars();
        boolean ac = (propagators.length == 2);
        writer.writeAtleastnvalues(
                IntStream.range(0, nv - 1).map(i -> propagators[0].getVar(i).getId()).toArray(),
                propagators[0].getVar(nv - 1).getId(),
                ac
        );
    }

    public static void writeAtmostnvalues(ConstraintWriter writer, Propagator[] propagators) throws IOException {
        int nv = propagators[0].getNbVars();
        boolean ac = (propagators.length == 2);
        writer.writeAtmostnvalues(
                IntStream.range(0, nv - 1).map(i -> propagators[0].getVar(i).getId()).toArray(),
                propagators[0].getVar(nv - 1).getId(),
                ac
        );
    }

    @SuppressWarnings("ConstantConditions")
    public static void writeBinpacking(ConstraintWriter writer, Propagator[] propagators) throws IOException {
        Propagator prop0 = propagators[0];
        writer.writeBinpacking(
                Arrays.stream(
                        Reflection.<IntVar[]>getObj(prop0, "binOfItem"))
                        .mapToInt(Identity::getId).toArray(),
                Arrays.stream(
                        Reflection.<int[]>getObj(prop0, "itemSize"))
                        .toArray(),
                Arrays.stream(
                        Reflection.<IntVar[]>getObj(prop0, "binLoad"))
                        .mapToInt(Identity::getId).toArray(),
                Reflection.getInt(prop0, "offset")
        );

    }

    public static void writeBoolchanneling(ConstraintWriter writer, Propagator[] propagators) throws IOException {
        Propagator<IntVar> prop0 = propagators[0];
        int n = Reflection.getInt(prop0, "n");
        writer.writeBoolchanneling(
                IntStream.range(0, n)
                        .mapToObj(i -> prop0.getVar(i).getId())
                        .mapToInt(i -> i)
                        .toArray(),
                prop0.getVar(n).getId(),
                Reflection.getInt(prop0, "offSet")
        );
    }

    @SuppressWarnings("ConstantConditions")
    public static void writeBitschanneling(ConstraintWriter writer, Propagator[] propagators) throws IOException {
        Propagator prop0 = propagators[0];
        writer.writeBitschanneling(
                Reflection.<IntVar>getObj(prop0, "octet").getId(),
                Arrays.stream(
                        Reflection.<IntVar[]>getObj(prop0, "bits"))
                        .mapToInt(Identity::getId).toArray()
        );
    }

    @SuppressWarnings("ConstantConditions")
    public static void writeCircuit(ConstraintWriter writer, Propagator[] propagators) throws IOException {
        //props[1] instanceof PropAllDiffAC)
        String conf = "LIGHT";
        if (propagators.length > 3) { // not light
            Propagator prop5 = propagators[5];
            conf = Reflection.<CircuitConf>getObj(prop5, "conf").name();
        }
        Propagator prop2 = propagators[2];
        writer.writeCircuit(
                Arrays.stream(prop2.getVars()).mapToInt(Identity::getId).toArray(), // vars
                Reflection.getInt(prop2, "offset"),
                conf
        );
    }

    public static void writeCount(ConstraintWriter writer, Propagator[] propagators) throws IOException {
        Propagator prop0 = propagators[0];
        int n = prop0.getNbVars();
        if (prop0 instanceof PropCountVar) {
            writer.writeCountVar(
                    IntStream.range(0, n - 2).map(i -> prop0.getVar(i).getId()).toArray(),
                    propagators[0].getVar(n - 2).getId(),
                    propagators[0].getVar(n - 1).getId()
            );
        } else {
            writer.writeCount(
                    IntStream.range(0, n - 1).map(i -> prop0.getVar(i).getId()).toArray(),
                    Reflection.getInt(prop0, "value"),
                    propagators[0].getVar(n - 1).getId()
            );
        }
    }

    @SuppressWarnings("ConstantConditions")
    public static void writeCumulative(ConstraintWriter writer, Propagator[] propagators) throws IOException {
        Propagator prop0 = propagators[0];
        boolean inc = (prop0 instanceof PropGraphCumulative);
        CumulFilter[] filters = Reflection.getObj(prop0, "filters");
        String[] sfilters = new String[filters.length];
        for (int i = 0; i < sfilters.length; i++) {
            switch (filters[i].getClass().getSimpleName()) {
                case "HeightCumulFilter":
                    sfilters[i] = "HEIGHTS";
                    break;
                case "TimeCumulFilter":
                    sfilters[i] = "TIME";
                    break;
                case "SweepCumulFilter":
                    sfilters[i] = "SWEEP";
                    break;
                case "SweepHeiSortCumulFilter":
                    sfilters[i] = "SWEEP_HEI_SORT";
                    break;
                case "NRJCumulFilter":
                    sfilters[i] = "NRJ";
                    break;
                case "DisjunctiveTaskIntervalFilter":
                    sfilters[i] = "DISJUNCTIVE_TASK_INTERVAL";
                    break;
                case "DefaultCumulFilter":
                    sfilters[i] = "DEFAULT";
                    break;
            }
        }
        writer.writeCumulative(
                Arrays.stream(Reflection.<IntVar[]>getObj(prop0, "s"))
                        .mapToInt(Identity::getId).toArray(),
                Arrays.stream(Reflection.<IntVar[]>getObj(prop0, "d"))
                        .mapToInt(Identity::getId).toArray(),
                Arrays.stream(Reflection.<IntVar[]>getObj(prop0, "e"))
                        .mapToInt(Identity::getId).toArray(),
                Arrays.stream(Reflection.<IntVar[]>getObj(prop0, "h"))
                        .mapToInt(Identity::getId).toArray(),
                Reflection.<IntVar>getObj(prop0, "capa").getId(),
                inc,
                sfilters
        );
    }

    public static void writeDiffn(ConstraintWriter writer, Propagator[] propagators) throws IOException {
        Propagator<IntVar> prop0 = propagators[0];
        int n = Reflection.getInt(prop0, "n");
        writer.writeDiffn(
                IntStream.range(0, n).map(i -> prop0.getVar(i).getId()).toArray(),
                IntStream.range(n, 2 * n).map(i -> prop0.getVar(i).getId()).toArray(),
                IntStream.range(2 * n, 3 * n).map(i -> prop0.getVar(i).getId()).toArray(),
                IntStream.range(3 * n, 4 * n).map(i -> prop0.getVar(i).getId()).toArray()
        );
    }

    public static void writeElement(ConstraintWriter writer, Propagator prop) throws IOException {
        int n = prop.getNbVars();
        writer.writeElementVar(
                prop.getVar(0).getId(),
                IntStream.range(2, n).map(i -> prop.getVar(i).getId()).toArray(),
                prop.getVar(1).getId(),
                Reflection.getInt(prop, "offset")
        );
    }

    public static void writeGcc(ConstraintWriter writer, Propagator prop) throws IOException {
        int n = Reflection.getInt(prop, "n");
        writer.writeGcc(
                IntStream.range(0, n).map(i -> prop.getVar(i).getId()).toArray(),
                Reflection.getObj(prop, "values"),
                IntStream.range(n, prop.getNbVars()).map(i -> prop.getVar(i).getId()).toArray()
        );
    }

    public static void writeInversechanneling(ConstraintWriter writer, Propagator[] propagators) throws IOException {
        Propagator propN = propagators[propagators.length - 1];
        int[] xids = Arrays.stream(
                Reflection.<IntVar[]>getObj(propN, "X"))
                .mapToInt(Identity::getId).toArray();
        int[] yids = Arrays.stream(
                Reflection.<IntVar[]>getObj(propN, "Y"))
                .mapToInt(Identity::getId).toArray();
        int ox = Reflection.getInt(propN, "minX");
        int oy = Reflection.getInt(propN, "minY");
        writer.writeInversechanneling(xids, yids, ox, oy);
    }

    public static void writeKnapsack(ConstraintWriter writer, Propagator prop) throws IOException {
        int n = prop.getNbVars();
        int[] occ = IntStream.range(0, n - 2).map(i -> prop.getVar(i).getId()).toArray();
        int cap = prop.getVar(n - 2).getId();
        int pow = prop.getVar(n - 1).getId();
        int[] ws = Reflection.getObj(prop, "weigth");
        int[] es = Reflection.getObj(prop, "energy");
        writer.writeKnapsack(occ, cap, pow, ws, es);
    }

    public static void writeLexchain(ConstraintWriter writer, Propagator prop) throws IOException {
        writer.writeLexchain(
                IntStream.range(0, prop.getNbVars()).map(i -> prop.getVar(i).getId()).toArray(),
                Reflection.getInt(prop, "N"),
                Reflection.<Boolean>getObj(prop, "strict")
        );
    }

    public static void writeLex(ConstraintWriter writer, Propagator prop) throws IOException {
        int n = Reflection.getObj(prop, "n");
        writer.writeLex(
                IntStream.range(0, n).map(i -> prop.getVar(i).getId()).toArray(),
                IntStream.range(n, prop.getNbVars()).map(i -> prop.getVar(i).getId()).toArray(),
                Reflection.<Boolean>getObj(prop, "strict")
        );
    }

    public static void writeMax(ConstraintWriter writer, Propagator prop) throws IOException {
        if (prop.getNbVars() == 3) {
            writer.writeMax(
                    prop.getVar(0).getId(),
                    IntStream.range(1, prop.getNbVars())
                            .map(i -> prop.getVar(i).getId())
                            .toArray());
        } else {
            writer.writeMax(
                    prop.getVar(prop.getNbVars() - 1).getId(),
                    IntStream.range(0, prop.getNbVars() - 1)
                            .map(i -> prop.getVar(i).getId())
                            .toArray());
        }
    }

    public static void writeMin(ConstraintWriter writer, Propagator prop) throws IOException {
        if (prop.getNbVars() == 3) {
            writer.writeMin(
                    prop.getVar(0).getId(),
                    IntStream.range(1, prop.getNbVars())
                            .map(i -> prop.getVar(i).getId())
                            .toArray());
        } else {
            writer.writeMin(
                    prop.getVar(prop.getNbVars() - 1).getId(),
                    IntStream.range(0, prop.getNbVars() - 1)
                            .map(i -> prop.getVar(i).getId())
                            .toArray());
        }
    }

    public static void writeNvalues(ConstraintWriter writer, Propagator prop) throws IOException {
        int nv = prop.getNbVars();
        writer.writeNvalues(
                IntStream.range(0, nv - 1).map(i -> prop.getVar(i).getId()).toArray(),
                prop.getVar(nv - 1).getId()
        );
    }

    public static void writeSum(ConstraintWriter writer, Propagator propagator) throws IOException {
        if (propagator instanceof PropXplusYeqZ) {
            writer.writeSum(
                    Arrays.stream(propagator.getVars()).mapToInt(Identity::getId).toArray(),
                    2, Operator.EQ.toString(),
                    0
            );
        } else if (propagator instanceof PropScalar) {
            writer.writeScalar(
                    Arrays.stream(propagator.getVars()).mapToInt(Identity::getId).toArray(),
                    Reflection.getObj(propagator, "c"),
                    Reflection.getInt(propagator, "pos"),
                    Reflection.<Operator>getObj(propagator, "o").toString(),
                    Reflection.getInt(propagator, "b")
            );
        } else {
            writer.writeSum(
                    Arrays.stream(propagator.getVars()).mapToInt(Identity::getId).toArray(),
                    Reflection.getInt(propagator, "pos"),
                    Reflection.<Operator>getObj(propagator, "o").toString(),
                    Reflection.getInt(propagator, "b")
            );
        }
    }

    public static void writeSubcircuit(ConstraintWriter writer, Propagator propagator) throws IOException {
        int nv = propagator.getNbVars();
        writer.writeSubcircuit(
                IntStream.range(0, nv - 1).map(i -> propagator.getVar(i).getId()).toArray(),
                propagator.getVar(nv - 1).getId(),
                Reflection.getInt(propagator, "offset")
        );
    }

    public static void writeTree(ConstraintWriter writer, Propagator propagator) throws IOException {
        int nv = propagator.getNbVars();
        writer.writeTree(
                IntStream.range(0, nv - 1).map(i -> propagator.getVar(i).getId()).toArray(),
                propagator.getVar(nv - 1).getId(),
                Reflection.getInt(propagator, "offSet")
        );
    }

    public static void writeTable(ConstraintWriter writer, Propagator propagator) throws IOException {
        if (propagator instanceof PropBinCSP) {
            PropBinCSP prop0 = (PropBinCSP) propagator;
            Tuples tuples = prop0.extractTuples();
            String algo = "";
            boolean feasible = tuples.isFeasible();
            switch (prop0.getClass().getSimpleName()) {
                case "PropBinAC2001":
                    algo = "AC2001";
                    break;
                case "PropBinFC":
                    algo = "FC";
                    break;
                case "PropBinAC3":
                    algo = "AC3";
                    break;
                case "PropBinAC3rm":
                    algo = "AC3rm";
                    break;
                case "PropBinAC3bitrm":
                    algo = "AC3bit+rm";
                    break;
            }
            writer.writeTable(
                    Arrays.stream(propagator.getVars()).mapToInt(Identity::getId).toArray(),
                    tuples.toMatrix(),
                    algo,
                    feasible);
        } else if (propagator instanceof PropLargeCSP) {
            PropLargeCSP prop0 = (PropLargeCSP) propagator;
            Tuples tuples = prop0.extractTuples();
            String algo = "";
            boolean feasible = tuples.isFeasible();
            switch (prop0.getClass().getSimpleName()) {
                case "PropLargeFC":
                    algo = "FC";
                    break;
                case "PropLargeGAC3rm":
                    algo = "GAC3rm";
                    break;
                case "PropLargeGAC2001":
                    algo = "GAC2001";
                    break;
                case "PropLargeGACSTRPos":
                    algo = "GACSTR+";
                    break;
                case "PropLargeGAC2001Positive":
                    algo = "GAC2001+";
                    break;
                case "PropLargeGAC3rmPositive":
                    algo = "GAC3rm+";
                    break;
            }
            writer.writeTable(
                    Arrays.stream(propagator.getVars()).mapToInt(Identity::getId).toArray(),
                    tuples.toMatrix(),
                    algo,
                    feasible);
        } else {
            throw new UnsupportedOperationException("MMDC, CT+ and STR2+ are not supported yet.");
        }
    }
}
