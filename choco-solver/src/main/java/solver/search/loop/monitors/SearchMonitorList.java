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
package solver.search.loop.monitors;

import solver.exception.ContradictionException;

import java.util.ArrayList;
import java.util.List;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 09/05/11
 */
public final class SearchMonitorList implements IMonitorClose, IMonitorContradiction, IMonitorDownBranch,
        IMonitorInitialize, IMonitorInitPropagation, IMonitorInterruption, IMonitorOpenNode, IMonitorRestart,
        IMonitorSolution, IMonitorUpBranch {

	List<IMonitorClose> mclos = new ArrayList<IMonitorClose>();
    List<IMonitorContradiction> mcont = new ArrayList<IMonitorContradiction>();
    List<IMonitorDownBranch> mdbra = new ArrayList<IMonitorDownBranch>();
    List<IMonitorInitialize> minit = new ArrayList<IMonitorInitialize>();
    List<IMonitorInitPropagation> mipro = new ArrayList<IMonitorInitPropagation>();
    List<IMonitorInterruption> minte = new ArrayList<IMonitorInterruption>();
    List<IMonitorOpenNode> mopno = new ArrayList<IMonitorOpenNode>();
    List<IMonitorRestart> mrest = new ArrayList<IMonitorRestart>();
    List<IMonitorSolution> msolu = new ArrayList<IMonitorSolution>();
    List<IMonitorUpBranch> mubra = new ArrayList<IMonitorUpBranch>();


    @Override
    public void beforeInitialize() {
        for (int i = 0; i < minit.size(); i++) {
            minit.get(i).beforeInitialize();
        }
    }

    @Override
    public void afterInitialize() {
        for (int i = 0; i < minit.size(); i++) {
            minit.get(i).afterInitialize();
        }
    }

    @Override
    public void beforeInitialPropagation() {
        for (int i = 0; i < mipro.size(); i++) {
            mipro.get(i).beforeInitialPropagation();
        }
    }

    @Override
    public void afterInitialPropagation() {
        for (int i = 0; i < mipro.size(); i++) {
            mipro.get(i).afterInitialPropagation();
        }
    }

    @Override
    public void beforeOpenNode() {
        for (int i = 0; i < mopno.size(); i++) {
            mopno.get(i).beforeOpenNode();
        }
    }

    @Override
    public void afterOpenNode() {
        for (int i = 0; i < mopno.size(); i++) {
            mopno.get(i).afterOpenNode();
        }
    }

    @Override
    public void onSolution() {
        for (int i = 0; i < msolu.size(); i++) {
            msolu.get(i).onSolution();
        }
    }

    @Override
    public void beforeDownLeftBranch() {
        for (int i = 0; i < mdbra.size(); i++) {
            mdbra.get(i).beforeDownLeftBranch();
        }
    }

    @Override
    public void afterDownLeftBranch() {
        for (int i = 0; i < mdbra.size(); i++) {
            mdbra.get(i).afterDownLeftBranch();
        }
    }

    @Override
    public void beforeDownRightBranch() {
        for (int i = 0; i < mdbra.size(); i++) {
            mdbra.get(i).beforeDownRightBranch();
        }
    }

    @Override
    public void afterDownRightBranch() {
        for (int i = 0; i < mdbra.size(); i++) {
            mdbra.get(i).afterDownRightBranch();
        }
    }

    @Override
    public void beforeUpBranch() {
        for (int i = 0; i < mubra.size(); i++) {
            mubra.get(i).beforeUpBranch();
        }
    }

    @Override
    public void afterUpBranch() {
        for (int i = 0; i < mubra.size(); i++) {
            mubra.get(i).afterUpBranch();
        }
    }

    @Override
    public void onContradiction(ContradictionException cex) {
        for (int i = 0; i < mcont.size(); i++) {
            mcont.get(i).onContradiction(cex);
        }
    }

    @Override
    public void beforeRestart() {
        for (int i = 0; i < mrest.size(); i++) {
            mrest.get(i).beforeRestart();
        }
    }

    @Override
    public void afterRestart() {
        for (int i = 0; i < mrest.size(); i++) {
            mrest.get(i).afterRestart();
        }
    }

    @Override
    public void afterInterrupt() {
        for (int i = 0; i < minte.size(); i++) {
            minte.get(i).afterInterrupt();
        }
    }

    @Override
    public void beforeClose() {
        for (int i = 0; i < mclos.size(); i++) {
            mclos.get(i).beforeClose();
        }
    }

    @Override
    public void afterClose() {
        for (int i = 0; i < mclos.size(); i++) {
            mclos.get(i).afterClose();
        }
    }

    public void add(ISearchMonitor sm) {
        if (sm != null) {
            if (sm instanceof IMonitorClose) {
                mclos.add((IMonitorClose) sm);
            }
            if (sm instanceof IMonitorContradiction) {
                mcont.add((IMonitorContradiction) sm);
            }
            if (sm instanceof IMonitorDownBranch) {
                mdbra.add((IMonitorDownBranch) sm);
            }
            if (sm instanceof IMonitorInitialize) {
                minit.add((IMonitorInitialize) sm);
            }
            if (sm instanceof IMonitorInitPropagation) {
                mipro.add((IMonitorInitPropagation) sm);
            }
            if (sm instanceof IMonitorInterruption) {
                minte.add((IMonitorInterruption) sm);
            }
            if (sm instanceof IMonitorOpenNode) {
                mopno.add((IMonitorOpenNode) sm);
            }
            if (sm instanceof IMonitorRestart) {
                mrest.add((IMonitorRestart) sm);
            }
            if (sm instanceof IMonitorSolution) {
                msolu.add((IMonitorSolution) sm);
            }
            if (sm instanceof IMonitorUpBranch) {
                mubra.add((IMonitorUpBranch) sm);
            }
        }
    }

}
