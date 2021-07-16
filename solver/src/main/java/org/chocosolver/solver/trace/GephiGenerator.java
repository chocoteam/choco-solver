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
import java.nio.file.StandardOpenOption;

import static org.chocosolver.solver.trace.GephiConstants.*;

/**
 * <p> Project: choco-solver.
 *
 * @author Charles Prud'homme
 * @since 03/05/2018.
 */
public class GephiGenerator extends SearchViz {

    private final String instance;

    private final StringBuilder nodes;
    private final StringBuilder edges;
    private int nodeCount;
    private int edgeCount;

    public GephiGenerator(String gexfFile, Solver aSolver) {
        super(aSolver, false);
        this.instance = gexfFile;
        this.nodes = new StringBuilder();
        this.edges = new StringBuilder();
    }

    @Override
    protected boolean connect(String label) {
        return true;
    }

    @Override
    protected void disconnect() {
        Path file = Paths.get(instance);
        if (Files.exists(file)) {
            try {
                Files.delete(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            Files.createFile(file);
            Files.write(file, OXMLTAG.getBytes(), StandardOpenOption.WRITE);
            Files.write(file, OGRAPGTAG.getBytes(), StandardOpenOption.APPEND);
            Files.write(file, String.format(ONODESTAG, nodeCount).getBytes(), StandardOpenOption.APPEND);
            Files.write(file, nodes.toString().getBytes(), StandardOpenOption.APPEND);
            Files.write(file, ENODESTAG.getBytes(), StandardOpenOption.APPEND);
            Files.write(file, OEDGESTAG.getBytes(), StandardOpenOption.APPEND);
            Files.write(file, edges.toString().getBytes(), StandardOpenOption.APPEND);
            Files.write(file, EEDGESTAG.getBytes(), StandardOpenOption.APPEND);
            Files.write(file, EGRAPGTAG.getBytes(), StandardOpenOption.APPEND);
            Files.write(file, EXMLTAG.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("Unable to write to GEXF file. No information will be sent.");
        }
    }

    @Override
    protected void sendNode(int nc, int pid, int alt, int kid, int rid, String label, String info) {
        nodeCount++;
        if(pid == -1){
            nodes.append(String.format(ROOTTAG, nc, label, ORANGE, DISC));
        }else {
            nodes.append(String.format(NODETAG, nc, label, String.format(PID, pid), BLUE, DISC));
            edges.append(String.format(EDGETAG, edgeCount++, pid, nc));
        }
    }

    @Override
    protected void sendSolution(int nc, int pid, int alt, int kid, int rid, String label, String info) {
        nodeCount++;
        nodes.append(String.format(NODETAG, nc, label, String.format(PID, pid), GREEN, DISC));
        edges.append(String.format(EDGETAG, edgeCount++, pid, nc));
    }

    @Override
    protected void sendFailure(int nc, int pid, int alt, int kid, int rid, String label, String info) {
        nodeCount++;
        nodes.append(String.format(NODETAG, nc, label, String.format(PID, pid), RED, DISC));
        edges.append(String.format(EDGETAG, edgeCount++, pid, nc));
    }

    @Override
    protected void sendRestart(int rid) {
        // nothing is done in that case
    }
}
