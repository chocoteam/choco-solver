/*
 * This file is part of choco-parsers, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.parser.dimacs;

import org.chocosolver.parser.ParserException;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.BoolVar;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

/**
 * DIMACS CNF file parser.
 * <br/>
 * Based on <a href="https://people.sc.fsu.edu/~jburkardt/data/cnf/cnf.html">DIMACS CNF file format</a>.
 *
 * @author Charles Prud'homme
 * @since 04/03/2021
 */
public class DIMACSParser {
    private static final String TAG_COMM = "c";
    private static final String TAG_PROB = "p";
    private static final String TAG_CNF = "cnf";
    private BoolVar[] literals;

    public void model(Model model, String instance) throws FileNotFoundException {
        Reader reader = new FileReader(instance);
        try (BufferedReader br = new BufferedReader(reader)) {
            String line;
            int nvars, nclauses = 0;
            while ((line = br.readLine()) != null) {
                if (line.startsWith(TAG_COMM)) continue;
                if (line.startsWith(TAG_PROB)) {
                    String[] values = Arrays.stream(line.split("\\s+"))
                            .filter(v -> v.length() > 0)
                            .toArray(String[]::new);
                    if (!values[1].equals(TAG_CNF)) {
                        throw new ParserException("Not a CNF file");
                    }
                    nvars = Integer.parseInt(values[2]);
                    nclauses = Integer.parseInt(values[3]);
                    literals = IntStream.range(0, nvars)
                            .mapToObj(i -> model.boolVar(Integer.toString(i)))
                            .toArray(BoolVar[]::new);
                    break;
                }
            }
            List<BoolVar> lits = new ArrayList<>();
            while ((line = br.readLine()) != null) {
                if (line.startsWith(TAG_COMM)) continue;
                int[] ls = Arrays.stream(line.split("\\s+"))
                        .filter(v -> v.length() > 0)
                        .mapToInt(Integer::parseInt)
                        .toArray();
                int i = 0, j = 0;
                while (j < ls.length && (i = ls[j++]) != 0) {
                    lits.add(i > 0 ?
                            literals[i - 1] :
                            literals[-i - 1].not());
                }
                if (i == 0 && lits.size() > 0) {
                    nclauses--;
                    model.addClausesBoolOrArrayEqualTrue(lits.toArray(new BoolVar[0]));
                    lits.clear();
                }
            }
            if (nclauses != 0) {
                throw new ParserException("Missing clauses");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public String printSolution() {
        StringBuilder st = new StringBuilder();
        for (BoolVar outputVar : literals) {
            st.append(outputVar.getValue()).append(' ');
        }
        st.append('\n');
        return st.toString();
    }
}
