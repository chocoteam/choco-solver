/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2018, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.loop.monitors;

import org.chocosolver.solver.Solver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * <p> Project: choco-solver.
 *
 * @author Charles Prud'homme
 * @since 03/05/2018.
 */
public class GephiGenerator extends SearchViz {

    private static final String OXMLTAG = "<?xml version=\"1.0\" encoding=\"UTF−8\"?>\n" +
            "<gexf \txmlns=\"http://www.gexf.net/1.2draft\"\n" +
            "\txmlns:xsi=\"http://www.w3.org/2001/XMLSchema−instance\"\n" +
            "\txsi:schemaLocation=\"http://www.gexf.net/1.2draft\n" +
            "\t\thttp://www.gexf.net/1.2draft/gexf.xsd\"\n" +
            "\txmlns:viz=\"http://www.gexf.net/1.2draft/viz\"\n"+
            "\tversion=\"1.2\">\n";
    private static final String OGRAPGTAG = "\t<graph mode=\"static\" defaultedgetype=\"directed\">\n";
    private static final String ONODESTAG = "\t\t<nodes count=\"%d\">\n";
    private static final String ROOTTAG =
            "\t\t\t<node id=\"%d\" label=\"%s\">\n" +
                    "\t\t\t\t%s\n" + // color
                    "\t\t\t\t<viz:shape value=\"disc\"/>\n" +
                    "\t\t\t</node>\n";
    private static final String NODETAG =
            "\t\t\t<node id=\"%d\" label=\"%s\" pid=\"%d\">\n" +
                    "\t\t\t\t%s\n" + // color
//                    "\t\t\t\t<viz:position x=\"15.783598\" y=\"40.109245\" z=\"0.0\"/>\n" +
//                    "\t\t\t\t<viz:size value=\"2.0375757\"/>\n" +
                    "\t\t\t\t<viz:shape value=\"disc\"/>\n" +
                    "\t\t\t</node>\n";
    private static final String ENODESTAG = "\t\t</nodes>\n";
    private static final String OEDGESTAG = "\t\t<edges>\n";
    private static final String EDGETAG = "\t\t\t<edge id=\"%d\" source=\"%d\" target=\"%d\" type=\"directed\"/>\n";
    private static final String EEDGESTAG = "\t\t</edges>\n";
    private static final String EGRAPGTAG = "\t</graph>\n";
    private static final String EXMLTAG = "</gexf>\n";


    private static final String GREEN = "<viz:color r=\"63\" g=\"191\" b=\"63\" a=\"1\"/>";
    private static final String RED = "<viz:color r=\"191\" g=\"63\" b=\"63\" a=\"1\"/>";
    private static final String BLUE = "<viz:color r=\"63\" g=\"127\" b=\"191\" a=\"0.1\"/>";
    private static final String ORANGE = "<viz:color r=\"191\" g=\"127\" b=\"63\" a=\"1\"/>";

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
            nodes.append(String.format(ROOTTAG, nc, label, ORANGE));
        }else {
            nodes.append(String.format(NODETAG, nc, label, pid, BLUE));
            edges.append(String.format(EDGETAG, edgeCount++, pid, nc));
        }
    }

    @Override
    protected void sendSolution(int nc, int pid, int alt, int kid, int rid, String label, String info) {
        nodeCount++;
        nodes.append(String.format(NODETAG, nc, label, pid, GREEN));
        edges.append(String.format(EDGETAG, edgeCount++, pid, nc));
    }

    @Override
    protected void sendFailure(int nc, int pid, int alt, int kid, int rid, String label, String info) {
        nodeCount++;
        nodes.append(String.format(NODETAG, nc, label, pid, RED));
        edges.append(String.format(EDGETAG, edgeCount++, pid, nc));
    }

    @Override
    protected void sendRestart(int rid) {

    }
}
