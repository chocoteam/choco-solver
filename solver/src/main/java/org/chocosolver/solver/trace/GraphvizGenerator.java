/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.trace;

import org.chocosolver.solver.Solver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.StandardOpenOption.APPEND;

/**
 * <p> Project: choco-solver.
 *
 * @author Charles Prud'homme
 * @since 03/05/2018.
 */
public class GraphvizGenerator extends SearchViz {

    private static final String OPEN = "digraph G{\n"+
            "\trankdir=TB;\n\n";

    private static final String ROOT = "\t0 [label=\"ROOT\", shape = doublecircle, color = gray];\n";

    private static final String NODE = "\t%d [label = \"%s\" shape = circle];\n";

    private static final String SOLU = "\ts%d [label = \"Sol.#%d\" shape = box, color = green2];\n";

    private static final String FAIL = "\t%d [shape = point, color = red];\n";

    private static final String EDGE = "\t%d -> %d;\n";

    private static final String SEDGE = "\t%d -> s%d;\n";

    private static final String END = "}";

    private final Path instance;

    private int scount = 0;

    public GraphvizGenerator(String gvFile, Solver aSolver) {
        super(aSolver, false);
        instance = Paths.get(gvFile);
        if (Files.exists(instance)) {
            try {
                Files.delete(instance);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            Files.createFile(instance);
            Files.write(instance, OPEN.getBytes(), APPEND);
        } catch (IOException e) {
            System.err.println("Unable to create to GEXF file. No information will be sent.");
            connected = false;
        }
        connected = true;
    }


    @Override
    protected boolean connect(String label) {
        return true;
    }

    @Override
    protected void disconnect() {
        try {
            Files.write(instance, END.getBytes(), APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void sendNode(int nc, int pid, int alt, int kid, int rid, String label, String info) {
        if(pid == -1){
            try {
                Files.write(instance, ROOT.getBytes(), APPEND);
            } catch (IOException e) {
                System.err.println("Unable to write to GEXF file. No information will be sent.");
                connected = false;
            }
        }else{
            try {
                Files.write(instance, String.format(NODE, nc, label).getBytes(), APPEND);
                Files.write(instance, String.format(EDGE, pid, nc).getBytes(), APPEND);
            } catch (IOException e) {
                System.err.println("Unable to write to GEXF file. No information will be sent.");
                connected = false;
            }
        }
    }

    @Override
    protected void sendSolution(int nc, int pid, int alt, int kid, int rid, String label, String info) {
        sendNode(nc, pid, alt, kid, rid, label, info);
        try {

            Files.write(instance, String.format(SOLU, ++scount, scount).getBytes(), APPEND);
            Files.write(instance, String.format(SEDGE, nc, scount).getBytes(), APPEND);
        } catch (IOException e) {
            System.err.println("Unable to write to GEXF file. No information will be sent.");
            connected = false;
        }
    }

    @Override
    protected void sendFailure(int nc, int pid, int alt, int kid, int rid, String label, String info) {
        try {
            Files.write(instance, String.format(FAIL, nc).getBytes(), APPEND);
            Files.write(instance, String.format(EDGE, pid, nc).getBytes(), APPEND);
        } catch (IOException e) {
            System.err.println("Unable to write to GEXF file. No information will be sent.");
            connected = false;
        }
    }

    @Override
    protected void sendRestart(int rid) {
        // nothing is done in that case
    }
}
