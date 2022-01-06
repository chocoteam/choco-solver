/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.trace;

/**
 *
 * <p>
 * Project: choco-solver.
 * @author Charles Prud'homme
 * @since 01/10/2018.
 */
public class GephiConstants {

    public static final String OXMLTAG = "<?xml version=\"1.0\" encoding=\"UTF−8\"?>\n" +
            "<gexf \txmlns=\"http://www.gexf.net/1.2draft\"\n" +
            "\txmlns:xsi=\"http://www.w3.org/2001/XMLSchema−instance\"\n" +
            "\txsi:schemaLocation=\"http://www.gexf.net/1.2draft\n" +
            "\t\thttp://www.gexf.net/1.2draft/gexf.xsd\"\n" +
            "\txmlns:viz=\"http://www.gexf.net/1.2draft/viz\"\n"+
            "\tversion=\"1.2\">\n";
    public static final String OGRAPGTAG = "\t<graph mode=\"static\" defaultedgetype=\"directed\">\n";
    public static final String ONODESTAG = "\t\t<nodes count=\"%d\">\n";
    public static final String ROOTTAG =
            "\t\t\t<node id=\"%d\" label=\"%s\">\n" +
                    "\t\t\t\t%s\n" + // color
                    "\t\t\t\t%s\n" + // shape
                    "\t\t\t</node>\n";
    public static final String NODETAG =
            "\t\t\t<node id=\"%s\" label=\"%s\" %s>\n" +
                    "\t\t\t\t%s\n" + // color
//                    "\t\t\t\t<viz:position x=\"15.783598\" y=\"40.109245\" z=\"0.0\"/>\n" +
//                    "\t\t\t\t<viz:size value=\"2.0375757\"/>\n" +
                    "\t\t\t\t%s\n" + // shape
                    "\t\t\t</node>\n";
    public static final String ENODESTAG = "\t\t</nodes>\n";
    public static final String OEDGESTAG = "\t\t<edges>\n";
    public static final String EDGETAG = "\t\t\t<edge id=\"%s\" source=\"%s\" target=\"%s\" type=\"directed\"/>\n";
    public static final String EEDGESTAG = "\t\t</edges>\n";
    public static final String EGRAPGTAG = "\t</graph>\n";
    public static final String EXMLTAG = "</gexf>\n";


    public static final String GREEN = "<viz:color r=\"63\" g=\"191\" b=\"63\" a=\"1\"/>";
    public static final String RED = "<viz:color r=\"191\" g=\"63\" b=\"63\" a=\"1\"/>";
    public static final String BLUE = "<viz:color r=\"63\" g=\"127\" b=\"191\" a=\"0.1\"/>";
    public static final String ORANGE = "<viz:color r=\"191\" g=\"127\" b=\"63\" a=\"1\"/>";

    public static final String DISC = "<viz:shape value=\"disc\"/>";
    public static final String SQUARE = "<viz:shape value=\"square\"/>";
    public static final String DIAM = "<viz:shape value=\"diamond\"/>";

    public static final String PID = "pid=\"%s\"";

    private GephiConstants() {
    }
    
}
