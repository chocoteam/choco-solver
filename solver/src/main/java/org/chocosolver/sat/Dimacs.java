/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.sat;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;

import java.io.*;
import java.util.Arrays;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 19/03/2021
 */
public interface Dimacs {

    String TAG_COMM = "c";
    String TAG_PROB = "p";
    String TAG_CNF = "cnf";

    MiniSat _me();

    /**
     * A call to this method parses {@code pathToFile}, a CNF file, and populates this {@code MiniSat} pathToFile
     * with variables and clauses defined in the file.
     * Then, a call to {@link MiniSat#solve()} is required.
     * @param pathToFile path to the CNF file to parse
     * @throws FileNotFoundException if no file is found at
     */
    default void parse(String pathToFile) throws FileNotFoundException {

        Reader reader = new FileReader(pathToFile);
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
                        throw new IllegalStateException("Not a CNF file");
                    }
                    nvars = Integer.parseInt(values[2]);
                    nclauses = Integer.parseInt(values[3]);
                    break;
                }
            }
            TIntList lits = new TIntArrayList();
            while ((line = br.readLine()) != null) {
                if (line.startsWith(TAG_COMM)) continue;
                int[] ls = Arrays.stream(line.split("\\s+"))
                        .filter(v -> v.length() > 0)
                        .mapToInt(Integer::parseInt)
                        .toArray();
                int i = 0, j = 0, var;
                while (j < ls.length) {
                    i = ls[j];
                    if (i == 0) break;
                    var = Math.abs(i) - 1;
                    while (var >= _me().nVars()) {
                        _me().newVariable();
                    }
                    lits.add(i > 0 ?
                            MiniSat.makeLiteral(var) :
                            MiniSat.neg(MiniSat.makeLiteral(var)));
                    j++;
                }
                if (i == 0 && lits.size() > 0) {
                    nclauses--;
                    _me().addClause(lits);
                    lits.clear();
                }
            }
            if (nclauses != 0) {
                throw new IllegalStateException("Missing clauses");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
