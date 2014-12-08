/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
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

/**
 * Created by IntelliJ IDEA.
 * User: Jean-Guillaume Fages
 * Date: 15/07/13
 * Time: 16:31
 */

package org.chocosolver.parser.flatzinc.para;

import org.chocosolver.parser.flatzinc.BaseFlatzincListener;
import org.chocosolver.parser.flatzinc.Flatzinc;
import org.chocosolver.parser.flatzinc.layout.FZNLayoutPara;
import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.Settings;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.objective.ObjectiveManager;
import org.chocosolver.solver.thread.AbstractParallelSlave;

import java.io.IOException;

public class ParserSlave extends AbstractParallelSlave<ParserMaster> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    String[] args;
    Solver solver;
    Flatzinc fznp;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public ParserSlave(final ParserMaster master, int id, String[] args, Settings settings) {
        super(master, id);
        this.args = args;
        fznp = new Flatzinc();
        try {// sequential parsing (safer)
            fznp.parseParameters(args);
            fznp.addListener(new BaseFlatzincListener(fznp) {
                @Override
                public void buildLayout(String instance, boolean printAll) {
                    FZNLayoutPara fl = new FZNLayoutPara(master, fznp.instance, printAll);
                    fznp.datas.setLayout(fl);
                    fl.set(fznp.getSolver());
                }
            });
            fznp.defineSettings(settings);
            fznp.createSolver();
            fznp.parseInputFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public void work() {
        // plug monitor to communicate bounds (should not be added in the sequential phasis)
        solver = fznp.getSolver();
        fznp.configureSearch();
        fznp.solve();
        if (!solver.hasReachedLimit()) {
            master.closeWithSuccess();
        }
    }

    public void findBetterThan(int val, ResolutionPolicy policy) {
        if (solver == null) return;// can happen if a solution is found before this thread is fully ready
        ObjectiveManager iom = solver.getObjectiveManager();
        if (iom == null) return;// can happen if a solution is found before this thread is fully ready
        switch (policy) {
            case MAXIMIZE:
                iom.updateBestLB(val);
                break;
            case MINIMIZE:
                iom.updateBestUB(val);
                break;
            case SATISFACTION:
                // nothing to do
                break;
        }
    }
}
