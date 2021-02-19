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

import static org.chocosolver.solver.trace.GephiConstants.BLUE;
import static org.chocosolver.solver.trace.GephiConstants.DIAM;
import static org.chocosolver.solver.trace.GephiConstants.DISC;
import static org.chocosolver.solver.trace.GephiConstants.EDGETAG;
import static org.chocosolver.solver.trace.GephiConstants.EEDGESTAG;
import static org.chocosolver.solver.trace.GephiConstants.EGRAPGTAG;
import static org.chocosolver.solver.trace.GephiConstants.ENODESTAG;
import static org.chocosolver.solver.trace.GephiConstants.EXMLTAG;
import static org.chocosolver.solver.trace.GephiConstants.NODETAG;
import static org.chocosolver.solver.trace.GephiConstants.OEDGESTAG;
import static org.chocosolver.solver.trace.GephiConstants.OGRAPGTAG;
import static org.chocosolver.solver.trace.GephiConstants.ONODESTAG;
import static org.chocosolver.solver.trace.GephiConstants.ORANGE;
import static org.chocosolver.solver.trace.GephiConstants.OXMLTAG;
import static org.chocosolver.solver.trace.GephiConstants.RED;
import static org.chocosolver.solver.trace.GephiConstants.SQUARE;

import gnu.trove.set.hash.TIntHashSet;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.view.IView;

/**
 * <p> Project: choco-solver.
 *
 * @author Charles Prud'homme
 * @since 03/05/2018.
 */
public class GephiNetwork{


    private GephiNetwork() {
    }


    public static void write(String gexfFile, Model model) {
        int nodeCount = 1;
        int edgeCount = 1;

        StringBuilder nodes = new StringBuilder();
        StringBuilder edges = new StringBuilder();
        Path file = Paths.get(gexfFile);
        if (Files.exists(file)) {
            try {
                Files.delete(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        List<IView> views = new ArrayList<>();
        for(Variable var: model.getVars()){
            nodeCount++;
            nodes.append(String.format(NODETAG, var.getId(), var.getName(), "", RED, DISC));
            for(int i = 0 ; i < var.getNbViews(); i++){
                views.add(var.getView(i));
            }
        }
        // 1.b, write views
        for(IView view: views){
            nodeCount++;
            nodes.append(String.format(NODETAG, view.getId(), view.getName(), "", ORANGE, DIAM));
            edges.append(String.format(EDGETAG, edgeCount++, view.getId(), view.getVariable().getId()));
        }
        // 1.c, write constraints
        nodeCount++;
        int c = 0;
        TIntHashSet set = new TIntHashSet();
        String id;
        for(Constraint cstr: model.getCstrs()){
            set.clear();
            id = "c_"+(++c);
            nodes.append(String.format(NODETAG, id, cstr.getName(), "", BLUE, SQUARE));
            for(Propagator prop: cstr.getPropagators()) {
                for(Variable var : prop.getVars()){
                    if(!set.contains(var.getId())){
                        set.add(var.getId());
                        edges.append(String.format(EDGETAG, edgeCount++, id, var.getId()));
                    }
                }
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
}
