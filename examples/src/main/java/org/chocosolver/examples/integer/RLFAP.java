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

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.hash.TIntHashSet;
import org.chocosolver.examples.AbstractProblem;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.search.limits.FailCounter;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
import org.kohsuke.args4j.Option;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import static java.util.Arrays.sort;
import static org.chocosolver.solver.search.strategy.Search.domOverWDegSearch;

/**
 * <a href="http://www.inra.fr/mia/T/schiex/Doc/CELAR.shtml">CELAR Radio Link Frequency Assignment Problem</a>:
 * <br/>
 * "The Radio Link frequency Assignment Problem consists in assigning frequencies to a set of radio links
 * defined between pairs of sites in order to avoid interferences.
 * Each radio link is represented by a variable whose domain is the set of all frequences
 * that are available for this link."
 * <br/>
 * <p/>
 * Instances can be downloaded <a href="http://www.inra.fr/mia/T/schiex/Export/FullRLFAP.tgz">here</a>.
 * Once unzipped, the program argument "-d" should point out to one directory containing instance description.
 * For instance "../FullRLFAP/CELAR/scen02".
 *
 * @author Charles Prud'homme
 * @since 19/03/12
 */
public class RLFAP extends AbstractProblem {

    private static String DOM = "dom.txt";
    private static String VAR = "var.txt";
    private static String CTR = "ctr.txt";

    @Option(name = "-d", aliases = "--directory", usage = "RLFAP instance directory (see http://www.inra.fr/mia/T/schiex/Export/FullRLFAP.tgz).", required = true)
    String dir;

    @Option(name = "-o", aliases = "--optimize", usage = "Minimize the number of allocated frequencies", required = false)
    boolean opt = false;

    int[][] _dom, _ctr;
    int[][] _var;

    IntVar[] vars;
    IntVar[] cards;
    IntVar nb0;
    int[] freqs;
    int[] rank;


    @Override
    public void buildModel() {
        model = new Model();
        _dom = readDOM(dir + File.separator + DOM);
        _var = readVAR(dir + File.separator + VAR);
        _ctr = readCTR(dir + File.separator + CTR);

        TIntHashSet values = new TIntHashSet();

        vars = new IntVar[_var.length];

        vars = new IntVar[_var[_var.length - 1][0]];
        int n = vars.length;
        int prev = 0;
        for (int i = 0; i < _var.length; i++) {
            int vidx = _var[i][0] - 1;
            if (vidx > prev) {
                for (; prev < vidx; ) {
                    vars[prev++] = model.intVar(0);
                }
            }
            int didx = _var[i][1];
            if (_var[i].length > 2) {
                vars[vidx] = model.intVar(_var[i][2]);
            } else {
                vars[vidx] = model.intVar("v_" + vidx, _dom[didx]);
                values.addAll(_dom[didx]);
            }
            prev = vidx + 1;
        }
        int[][] graph = new int[n][n];

        for (int i = 0; i < _ctr.length; i++) {
            int[] ci = _ctr[i];
            model.distance(vars[ci[0] - 1], vars[ci[1] - 1], (ci[2] == 0 ? "=" : ">"), ci[3]).post();

            // MARK BOTH SPOTS IN "PRECEDENCE" GRAPH
            graph[ci[0] - 1][ci[1] - 1] = 1;
            graph[ci[1] - 1][ci[0] - 1] = 1;

        }
        if (opt) {
            cards = model.intVarArray("c", values.size(), 0, vars.length, true);
            freqs = values.toArray();
            sort(freqs);
            for (int i = 0; i < freqs.length; i++) {
                model.count(freqs[i], vars, cards[i]).post();
            }
            nb0 = model.intVar("nb0", 0, freqs.length, true);
            model.count(0, cards, nb0).post();
        }
        // RANKING VARIABLES PER LAYER OF DISTINCT SPOT
        rank = new int[n];
        boolean[] treated = new boolean[n];
        int i = 0;
        Deque<Integer> toTreat = new ArrayDeque<>();
        toTreat.push(i);
        rank[i] = 0;
        while (!toTreat.isEmpty()) {
            i = toTreat.pop();
            treated[i] = true;
            for (int j = 0; j < n; j++) {
                if (graph[i][j] == 1) {
                    rank[j] = Math.max(rank[i] + 1, rank[j]);
                    if (!treated[j] && !toTreat.contains(j)) {
                        toTreat.push(j);
                    }
                }
            }
        }
    }

    @Override
    public void configureSearch() {
        model.getSolver().setSearch(domOverWDegSearch(vars));
        model.getSolver().setLubyRestart(2, new FailCounter(model, 2), 25000);
    }

    @Override
    public void solve() {
        model.getSolver().limitNode(10000);
        if (opt) {
            model.setObjective(true, nb0);
        }
        while(model.getSolver().solve()){
            System.out.println(nb0);
            prettyOut();
        }
    }

    private void prettyOut() {
        System.out.println(String.format("RLFAP %s", dir));
        StringBuilder st = new StringBuilder();
        if (model.getSolver().isFeasible() == ESat.TRUE) {
            st.append("\t");
            for (int i = 0; i < vars.length; i++) {
                st.append(vars[i].getValue()).append(" ");
                if (i % 10 == 9) {
                    st.append("\n\t");
                }
            }
            if (opt) {
                st.append("\n\tnb assigned freq.:").append(freqs.length - nb0.getValue());
                for (int i = 0; i < freqs.length; i++) {
                    st.append("\n\tF ").append(freqs[i]).append(" : ").append(cards[i].getValue());
                }
            }
        } else {
            st.append("\tINFEASIBLE");
        }
        System.out.println(st.toString());
    }

    public static void main(String[] args) {
        new RLFAP().execute(args);
    }

    /////////////////////

    protected int[][] readDOM(String filename) {
        FileReader f;
        String line;
        TIntHashSet values = new TIntHashSet();
        try {
            f = new FileReader(filename);
            BufferedReader r = new BufferedReader(f);
            List<int[]> domains = new ArrayList<>();
            while ((line = r.readLine()) != null) {
                Scanner sc = new Scanner(line);
                sc.nextInt();
                while (sc.hasNextInt()) {
                    values.add(sc.nextInt());
                }
                domains.add(values.toArray());
                values.clear();
            }
            int[][] data = new int[domains.size()][];
            for (int i = 0; i < domains.size(); i++) {
                data[i] = domains.get(i);
                Arrays.sort(data[i]);
            }
            f.close();
            return data;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected int[][] readVAR(String filename) {
        FileReader f;
        String line;
        TIntList values = new TIntArrayList();
        try {
            f = new FileReader(filename);
            BufferedReader r = new BufferedReader(f);
            List<int[]> ints = new ArrayList<>();
            while ((line = r.readLine()) != null) {
                Scanner sc = new Scanner(line);
                while (sc.hasNextInt()) {
                    values.add(sc.nextInt());
                }
                ints.add(values.toArray());
                values.clear();
            }
            int[][] data = new int[ints.size()][];
            for (int i = 0; i < ints.size(); i++) {
                data[i] = ints.get(i);
            }
            f.close();
            return data;
        } catch (IOException
                e) {
            e.printStackTrace();
        }
        return null;
    }


    protected int[][] readCTR(String filename) {
        FileReader f;
        String line;
        TIntList values = new TIntArrayList();
        try {
            f = new FileReader(filename);
            BufferedReader r = new BufferedReader(f);
            List<int[]> ints = new ArrayList<>();
            while ((line = r.readLine()) != null) {
                Scanner sc = new Scanner(line);
                values.add(sc.nextInt());
                values.add(sc.nextInt());
                sc.next();
                values.add(sc.next().equals("=") ? 0 : 1);
                values.add(sc.nextInt());
                ints.add(values.toArray());
                values.clear();
            }
            int[][] data = new int[ints.size()][];
            for (int i = 0; i < ints.size(); i++) {
                data[i] = ints.get(i);
            }
            f.close();
            return data;
        } catch (IOException
                e) {
            e.printStackTrace();
        }
        return null;
    }


}
