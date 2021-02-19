/*
 * This file is part of examples, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.examples.integer;

import gnu.trove.map.hash.TObjectIntHashMap;
import org.chocosolver.examples.AbstractProblem;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
import org.kohsuke.args4j.Option;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

import static java.lang.Math.max;
import static java.lang.System.out;
import static java.util.Arrays.copyOfRange;

/**
 * OR-LIBRARY:<br/>
 * "Given a set of planes and runways, the objective is to minimize the total (weighted) deviation from
 * the target landing time for each plane.
 * There are costs associated with landing either earlier or later than a target landing time for each plane.
 * Each plane has to land on one of the runways within its predetermined time windows such that
 * separation criteria between all pairs of planes are satisfied.
 * This type of problem is a large-scale optimization problem, which occurs at busy airports where
 * making optimal use of the bottleneck resource (the runways) is crucial to keep the airport operating smoothly."
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 18/04/11
 */
public class AirPlaneLanding extends AbstractProblem {

    private static final String groupSeparator = "\\,";
    private static final String decimalSeparator = "\\.";
    private static final String non0Digit = "[\\p{javaDigit}&&[^0]]";
    private static Pattern decimalPattern;

    static {
        // \\p{javaDigit} may not be perfect, see above
        String digit = "([0-9])";
        String groupedNumeral = "(" + non0Digit + digit + "?" + digit + "?(" +
                groupSeparator + digit + digit + digit + ")+)";
        // Once again digit++ is used for performance, as above
        String numeral = "((" + digit + "++)|" + groupedNumeral + ")";
        String decimalNumeral = "(" + numeral + "|" + numeral +
                decimalSeparator + digit + "*+|" + decimalSeparator +
                digit + "++)";
        String decimal = "([-+]?" + decimalNumeral + ")";
        decimalPattern = Pattern.compile(decimal);
    }


    @Option(name = "-d", usage = "Airplane landing Data.", required = false)
    Data mData = Data.airland1;

    //DATA
    private int[][] data;
    int n;

    //    private static final int AT = 0;
    private static final int ELT = 1;
    private static final int TT = 2;
    private static final int LLT = 3;
    private static final int PCBT = 4;
    private static final int PCAT = 5;
    private static final int ST = 6;


    IntVar[] planes, tardiness, earliness;
    BoolVar[] bVars;
    int[] costLAT;
    TObjectIntHashMap<IntVar> maxCost;

    IntVar objective;

    @Override
    public void buildModel() {
        model = new Model("Air plane landing");
        data = parse(mData.source());
        n = data.length;
        planes = new IntVar[n];
        tardiness = new IntVar[n];
        earliness = new IntVar[n];
//        int obj_ub = 0;
        for (int i = 0; i < n; i++) {
            planes[i] = model.intVar("p_" + i, data[i][ELT], data[i][LLT], true);
            earliness[i] = model.intVar("e_" + i, 0, data[i][TT] - data[i][ELT], true);
            tardiness[i] = model.intVar("t_" + i, 0, data[i][LLT] - data[i][TT], true);
            earliness[i].eq((planes[i].neg().add(data[i][TT])).max(0)).post();
            tardiness[i].eq((planes[i].sub(data[i][TT])).max(0)).post();
        }
        List<BoolVar> booleans = new ArrayList<>();
        //disjunctive
        for (int i = 0; i < n - 1; i++) {
            for (int j = i + 1; j < n; j++) {
                BoolVar boolVar = model.boolVar("b_" + i + "_" + j);
                booleans.add(boolVar);

                Constraint c1 = precedence(planes[i], data[i][ST + j], planes[j]);
                Constraint c2 = precedence(planes[j], data[j][ST + i], planes[i]);
//                model.ifThenElse(boolVar, c1, c2);
                model.addClausesBoolNot(c1.reify(),c2.reify());
            }
        }

        bVars = booleans.toArray(new BoolVar[booleans.size()]);

        objective = model.intVar("obj", 0, 999999, true);

        // build C array
        costLAT = new int[2 * n];
        maxCost = new TObjectIntHashMap<>();
        for (int i = 0; i < n; i++) {
            costLAT[i] = data[i][PCBT];
            costLAT[n + i] = data[i][PCAT];
            maxCost.put(planes[i], max(data[i][PCBT], data[i][PCAT]));

        }

        IntVar obj_e = model.intVar("obj_e", 0, 999999, true);
        model.scalar(earliness, copyOfRange(costLAT, 0, n), "=", obj_e).post();

        IntVar obj_t = model.intVar("obj_t", 0, 999999, true);
        model.scalar(tardiness, copyOfRange(costLAT, n, 2 * n), "=", obj_t).post();
        model.sum(new IntVar[]{obj_e, obj_t}, "=", objective).post();

        model.allDifferent(planes, "BC").post();
        model.setObjective(false, objective);
    }

    static Constraint precedence(IntVar x, int duration, IntVar y) {
        return x.getModel().arithm(x, "<=", y, "-", duration);
    }

    @Override
    public void configureSearch() {
//        Arrays.sort(planes, (o1, o2) -> maxCost.get(o2) - maxCost.get(o1));
        Solver r = model.getSolver();
        r.setSearch(Search.minDomLBSearch(planes));
//        r.set(/*randomSearch(bVars, seed), */inputOrderLBSearch(planes));
    }

    @Override
    public void solve() {
        while (model.getSolver().solve()) {
            out.println("New solution found : " + objective);
            prettyOut();
        }
    }

    private void prettyOut() {
        System.out.println(String.format("Air plane landing(%s)", mData));
        StringBuilder st = new StringBuilder();
        if (model.getSolver().isFeasible() != ESat.TRUE) {
            st.append("\tINFEASIBLE");
        } else {
            for (int i = 0; i < n; i++) {
                System.out.printf("%s lands at %d, (diff: %d)\n",
                        planes[i].getName(),
                        planes[i].getValue(),
                        planes[i].getValue() - data[i][TT]);
            }
        }
//        System.out.println(st.toString());
    }

    public static void main(String[] args) {
        new AirPlaneLanding().execute(args);
    }

    private int[][] parse(String source) {
        Scanner sc = new Scanner(source);
        int nb = sc.nextInt();
        data = new int[nb][6 + nb];
        sc.nextLine();
        for (int i = 0; i < nb; i++) {
            data[i][0] = sc.nextInt(); // appearance time
            data[i][1] = sc.nextInt(); // earliest landing time
            data[i][2] = sc.nextInt(); // target landing time
            data[i][3] = sc.nextInt(); // latest landing time
            double tt = Double.parseDouble(sc.next(decimalPattern));
            data[i][4] = (int) Math.ceil(tt); // penalty C per unit of time for landing before target
            tt = Double.parseDouble(sc.next(decimalPattern));
            data[i][5] = (int) Math.ceil(tt); // penalty C per unit of time for landing after target
            for (int j = 0; j < nb; j++) {
                data[i][6 + j] = sc.nextInt();
            }
        }
        sc.close();
        return data;
    }

    /////////////////////////////////////////

    enum Data {
        airland1(" 10 10 \n" +

                " 54 129 155 559 10.00 10.00\n" +
                " 99999 3 15 15 15 15 15 15 15 15 \n" +

                " 120 195 258 744 10.00 10.00 \n" +
                " 3 99999 15 15 15 15 15 15 15 15 \n" +

                " 14 89 98 510 30.00 30.00 \n" +
                " 15 15 99999 8 8 8 8 8 8 8 \n" +

                " 21 96 106 521 30.00 30.00 \n" +
                " 15 15 8 99999 8 8 8 8 8 8 \n" +

                " 35 110 123 555 30.00 30.00 \n" +
                " 15 15 8 8 99999 8 8 8 8 8 \n" +

                " 45 120 135 576 30.00 30.00 \n" +
                " 15 15 8 8 8 99999 8 8 8 8 \n" +

                " 49 124 138 577 30.00 30.00 \n" +
                " 15 15 8 8 8 8 99999 8 8 8 \n" +

                " 51 126 140 573 30.00 30.00 \n" +
                " 15 15 8 8 8 8 8 99999 8 8 \n" +

                " 60 135 150 591 30.00 30.00 \n" +
                " 15 15 8 8 8 8 8 8  99999 8 \n" +

                " 85 160 180 657 30.00 30.00 \n" +
                " 15 15 8 8 8 8 8 8  8 99999"
        ),
        airland2(" 15 10 \n" +
                " 54 129 155 559 10.00 10.00 \n" +
                " 99999 3 15 15 15 15 15 15 \n" +
                " 15 15 3 3 15 15 3 \n" +
                " 115 190 250 732 10.00 10.00 \n" +
                " 3 99999 15 15 15 15 15 15 \n" +
                " 15 15 3 3 15 15 3 \n" +
                " 9 84 93 501 30.00 30.00 \n" +
                " 15 15 99999 8 8 8 8 8 \n" +
                " 8 8 15 15 8 8 15 \n" +
                " 14 89 98 509 30.00 30.00 \n" +
                " 15 15 8 99999 8 8 8 8 \n" +
                " 8 8 15 15 8 8 15 \n" +
                " 25 100 111 536 30.00 30.00 \n" +
                " 15 15 8 8 99999 8 8 8 \n" +
                " 8 8 15 15 8 8 15 \n" +
                " 32 107 120 552 30.00 30.00 \n" +
                " 15 15 8 8 8 99999 8 8 \n" +
                " 8 8 15 15 8 8 15 \n" +
                " 34 109 121 550 30.00 30.00 \n" +
                " 15 15 8 8 8 8 99999 8 \n" +
                " 8 8 15 15 8 8 15 \n" +
                " 34 109 120 544 30.00 30.00 \n" +
                " 15 15 8 8 8 8 8 99999 \n" +
                " 8 8 15 15 8 8 15 \n" +
                " 40 115 128 557 30.00 30.00 \n" +
                " 15 15 8 8 8 8 8 8 \n" +
                " 99999 8 15 15 8 8 15 \n" +
                " 59 134 151 610 30.00 30.00 \n" +
                " 15 15 8 8 8 8 8 8 \n" +
                " 8 99999 15 15 8 8 15 \n" +
                " 191 266 341 837 10.00 10.00 \n" +
                " 3 3 15 15 15 15 15 15 \n" +
                " 15 15 99999 3 15 15 3 \n" +
                " 176 251 313 778 10.00 10.00 \n" +
                " 3 3 15 15 15 15 15 15 \n" +
                " 15 15 3 99999 15 15 3 \n" +
                " 85 160 181 674 30.00 30.00 \n" +
                " 15 15 8 8 8 8 8 8 \n" +
                " 8 8 15 15 99999 8 15 \n" +
                " 77 152 171 637 30.00 30.00 \n" +
                " 15 15 8 8 8 8 8 8 \n" +
                " 8 8 15 15 8 99999 15 \n" +
                " 201 276 342 815 10.00 10.00 \n" +
                " 3 3 15 15 15 15 15 15 \n" +
                " 15 15 3 3 15 15 99999"),
        airland3(" 20 10\n" +
                " 0 75 82 486 30.00 30.00 \n" +
                " 99999 15 15 8 15 8 15 8 \n" +
                " 8 8 8 8 15 15 15 15 \n" +
                " 15 15 8 8 \n" +
                " 82 157 197 628 10.00 10.00 \n" +
                " 15 99999 3 15 3 15 3 15 \n" +
                " 15 15 15 15 3 3 3 3 \n" +
                " 3 3 15 15 \n" +
                " 59 134 160 561 10.00 10.00 \n" +
                " 15 3 99999 15 3 15 3 15 \n" +
                " 15 15 15 15 3 3 3 3 \n" +
                " 3 3 15 15 \n" +
                " 28 103 117 565 30.00 30.00 \n" +
                " 8 15 15 99999 15 8 15 8 \n" +
                " 8 8 8 8 15 15 15 15 \n" +
                " 15 15 8 8 \n" +
                " 126 201 261 735 10.00 10.00 \n" +
                " 15 3 3 15 99999 15 3 15 \n" +
                " 15 15 15 15 3 3 3 3 \n" +
                " 3 3 15 15 \n" +
                " 20 95 106 524 30.00 30.00 \n" +
                " 8 15 15 8 15 99999 15 8 \n" +
                " 8 8 8 8 15 15 15 15 \n" +
                " 15 15 8 8 \n" +
                " 110 185 229 664 10.00 10.00 \n" +
                " 15 3 3 15 3 15 99999 15 \n" +
                " 15 15 15 15 3 3 3 3 \n" +
                " 3 3 15 15 \n" +
                " 23 98 108 523 30.00 30.00 \n" +
                " 8 15 15 8 15 8 15 99999 \n" +
                " 8 8 8 8 15 15 15 15 \n" +
                " 15 15 8 8 \n" +
                " 42 117 132 578 30.00 30.00 \n" +
                " 8 15 15 8 15 8 15 8 \n" +
                " 99999 8 8 8 15 15 15 15 \n" +
                " 15 15 8 8 \n" +
                " 42 117 130 569 30.00 30.00 \n" +
                " 8 15 15 8 15 8 15 8 \n" +
                " 8 99999 8 8 15 15 15 15 \n" +
                " 15 15 8 8 \n" +
                " 57 132 149 615 30.00 30.00 \n" +
                " 8 15 15 8 15 8 15 8 \n" +
                " 8 8 99999 8 15 15 15 15 \n" +
                " 15 15 8 8 \n" +
                " 39 114 126 551 30.00 30.00 \n" +
                " 8 15 15 8 15 8 15 8 \n" +
                " 8 8 8 99999 15 15 15 15 \n" +
                " 15 15 8 8 \n" +
                " 186 261 336 834 10.00 10.00 \n" +
                " 15 3 3 15 3 15 3 15 \n" +
                " 15 15 15 15 99999 3 3 3 \n" +
                " 3 3 15 15 \n" +
                " 175 250 316 790 10.00 10.00 \n" +
                " 15 3 3 15 3 15 3 15 \n" +
                " 15 15 15 15 3 99999 3 3 \n" +
                " 3 3 15 15 \n" +
                " 139 214 258 688 10.00 10.00 \n" +
                " 15 3 3 15 3 15 3 15 \n" +
                " 15 15 15 15 3 3 99999 3 \n" +
                " 3 3 15 15 \n" +
                " 235 310 409 967 10.00 10.00 \n" +
                " 15 3 3 15 3 15 3 15 \n" +
                " 15 15 15 15 3 3 3 99999 \n" +
                " 3 3 15 15 \n" +
                " 194 269 338 818 10.00 10.00 \n" +
                " 15 3 3 15 3 15 3 15 \n" +
                " 15 15 15 15 3 3 3 3 \n" +
                " 99999 3 15 15 \n" +
                " 162 237 287 726 10.00 10.00 \n" +
                " 15 3 3 15 3 15 3 15 \n" +
                " 15 15 15 15 3 3 3 3 \n" +
                " 3 99999 15 15 \n" +
                " 69 144 160 607 30.00 30.00 \n" +
                " 8 15 15 8 15 8 15 8 \n" +
                " 8 8 8 8 15 15 15 15 \n" +
                " 15 15 99999 8 \n" +
                " 76 151 169 624 30.00 30.00 \n" +
                " 8 15 15 8 15 8 15 8 \n" +
                " 8 8 8 8 15 15 15 15 \n" +
                " 15 15 8 99999");
        final String source;

        Data(String source) {
            this.source = source;
        }

        String source() {
            return source;
        }
    }
}
