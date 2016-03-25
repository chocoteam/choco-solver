/**
 *  Copyright (c) 1999-2014, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.parser.flatzinc;

import org.chocosolver.parser.ParserListener;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.Propagator;

import java.util.ArrayList;

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
        System.out.println("% parse instance...");
    }

    @Override
    public void afterParsingFile() {

        if (((FznSettings) fznparser.getModel().getSettings()).printConstraint()) {
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
        System.out.println("% solve instance...");
        fznparser.getModel().getSolver().getMeasures().setReadingTimeCount(creationTime + System.nanoTime());
    }

    @Override
    public void afterSolving() {

    }
}
