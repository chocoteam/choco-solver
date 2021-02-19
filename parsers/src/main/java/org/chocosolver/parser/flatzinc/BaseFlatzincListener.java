/*
 * This file is part of choco-parsers, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.parser.flatzinc;

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
public class BaseFlatzincListener implements ParserListener {

    long creationTime;

    final Flatzinc fznparser;

    public BaseFlatzincListener(Flatzinc fznparser) {
        this.fznparser = fznparser;
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
        if(PRINT_LOG)System.out.println("% parse instance...");
    }

    @Override
    public void afterParsingFile() {

        if (((FznSettings) fznparser.getModel().getSettings()).printConstraints()) {
            ArrayList<String> l = new ArrayList<>();
            System.out.println("% INVOLVED CONSTRAINTS (CHOCO) ");
            for (Constraint c : fznparser.getModel().getCstrs()) {
                if (!l.contains(c.getName())) {
                    l.add(c.getName());
                    System.out.printf("%% %s\n", c.getName());
                    for(Propagator p: c.getPropagators()) {
                        System.out.printf("%% \t%s\n", p.getClass().getName());
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
        if(PRINT_LOG)System.out.println("% solve instance...");
        fznparser.getModel().getSolver().getMeasures().setReadingTimeCount(creationTime + System.nanoTime());
        if(PRINT_LOG)System.out.printf("%% ");
        if(PRINT_LOG)fznparser.getModel().getSolver().printShortFeatures();
    }

    @Override
    public void afterSolving() {

    }
}
