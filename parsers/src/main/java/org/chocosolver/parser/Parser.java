/*
 * This file is part of choco-parsers, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.parser;

import org.chocosolver.parser.flatzinc.ChocoFZN;
import org.chocosolver.parser.mps.ChocoMPS;
import org.chocosolver.parser.xcsp.ChocoXCSP;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

/**
 * A class that binds the parser wrt the input file
 *
 * <p> Project: choco-parsers.
 * @author Charles Prud'homme
 * @since 30/04/2018.
 */
public class Parser {

    @Argument(required = true, metaVar = "file", usage = "File to parse.")
    private String instance;

    @Option(name = "-pa", aliases = {"--parser"}, usage = "Parser to use:\n" +
            "0: automatic -- based on file name extension (compression is allowed), " +
            "1: FlatZinc (.fzn)," +
            "2: XCSP3 (.xml)," +
            "3: MPS (.mps)")
    private int pa = 0;

    public static void main(String[] args) throws Exception {
        new Parser().main0(args);
    }

    /**
     * Detect which parser to use
     * @param args arguments
     * @throws Exception when an argument is not correctly defined
     */
    private void main0(String[] args) throws Exception {
        CmdLineParser cmdparser = new CmdLineParser(this);
        try {
            cmdparser.parseArgument(args);
        } catch (CmdLineException e) {
            // ignored exception here
            if(instance == null) {
                System.err.println(e.getMessage());
                System.err.println("Parser [options...] file");
                cmdparser.printUsage(System.err);
                System.err.println();
                return;
            }
        }
        assert instance != null;
        if (pa == 0) {
            String[] parts = instance.split("\\.");
            for (String part : parts) {
                if (part.equals("fzn")) {
                    pa = 1;
                    break;
                }
                if (part.equals("xml")) {
                    pa = 2;
                    break;
                }
                if (part.equals("mps")) {
                    pa = 3;
                    break;
                }
            }
            switch (pa) {
                case 0:
                    System.err.println("Unknown file type.");
                    System.err.println("Expected file extensions: *.fzn, *.xml, *.mps");
                    System.err.println();
                    return;
                case 1:
                    ChocoFZN.main(args);
                    break;
                case 2:
                    ChocoXCSP.main(args);
                    break;
                case 3:
                    ChocoMPS.main(args);
                    break;
            }
        }
    }
}
