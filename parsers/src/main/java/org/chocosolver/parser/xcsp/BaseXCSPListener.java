/*
 * This file is part of choco-parsers, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.parser.xcsp;

import org.chocosolver.parser.ParserListener;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.Propagator;

import java.util.ArrayList;

import static org.chocosolver.parser.RegParser.PRINT_LOG;

/**
 * A base listener for Flatzinc parser, dedicated to single thread resolution.
 * <br/>
 *
 * @author Charles Prud'homme
 * @version choco-parsers
 * @since 21/10/2014
 */
public class BaseXCSPListener implements ParserListener {

    long creationTime;

    final XCSP xcspparser;

    public BaseXCSPListener(XCSP xcspparser) {
        this.xcspparser = xcspparser;
    }


    @Override
    public void beforeParsingParameters() {

    }

    @Override
    public void afterParsingParameters() {
    }

    @Override
    public void beforeSolverCreation() {
        creationTime = -System.nanoTime();
    }

    @Override
    public void afterSolverCreation() {
    }

    @Override
    public void beforeParsingFile() {
        System.out.println("c parse instance...");
    }

    @Override
    public void afterParsingFile() {

        if (((XCSPSettings) xcspparser.getModel().getSettings()).printConstraints()) {
            ArrayList<String> l = new ArrayList<>();
            System.out.println("c INVOLVED CONSTRAINTS (CHOCO) ");
            for (Constraint c : xcspparser.getModel().getCstrs()) {
                if (!l.contains(c.getName())) {
                    l.add(c.getName());
                    System.out.printf("c %s\n", c.getName());
                    for(Propagator p: c.getPropagators()) {
                        System.out.printf("c \t%s\n", p.getClass().getName());
                    }
                }
            }
        }
    }

    @Override
    public void beforeConfiguringSearch() {

    }

    @Override
    public void afterConfiguringSearch() {
    }

    @Override
    public void beforeSolving() {
        if(PRINT_LOG)System.out.println("c solve instance...");
        xcspparser.getModel().getSolver().getMeasures().setReadingTimeCount(creationTime + System.nanoTime());
    }

    @Override
    public void afterSolving() {

    }
}
