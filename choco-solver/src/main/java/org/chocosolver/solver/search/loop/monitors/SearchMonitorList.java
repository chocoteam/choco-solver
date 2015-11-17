/**
 * Copyright (c) 2015, Ecole des Mines de Nantes
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    This product includes software developed by the <organization>.
 * 4. Neither the name of the <organization> nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.solver.search.loop.monitors;

import org.chocosolver.solver.exception.ContradictionException;

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

    List<IMonitorClose> mclos = new ArrayList<>();
    List<IMonitorContradiction> mcont = new ArrayList<>();
    List<IMonitorDownBranch> mdbra = new ArrayList<>();
    List<IMonitorInitialize> minit = new ArrayList<>();
    List<IMonitorInterruption> minte = new ArrayList<>();
    List<IMonitorOpenNode> mopno = new ArrayList<>();
    List<IMonitorRestart> mrest = new ArrayList<>();
    List<IMonitorSolution> msolu = new ArrayList<>();
    List<IMonitorUpBranch> mubra = new ArrayList<>();


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
    public void beforeDownBranch(boolean left) {
        for (int i = 0; i < mdbra.size(); i++) {
            mdbra.get(i).beforeDownBranch(left);
        }
    }

    @Override
    public void afterDownBranch(boolean left) {
        for (int i = 0; i < mdbra.size(); i++) {
            mdbra.get(i).afterDownBranch(left);
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

    public boolean contains(ISearchMonitor sm) {
        boolean isPluggedIn = false;
        if (sm != null) {
            if (sm instanceof IMonitorClose) {
                isPluggedIn = mclos.contains(sm);
            }
            if (sm instanceof IMonitorContradiction) {
                isPluggedIn = mcont.contains(sm);
            }
            if (sm instanceof IMonitorDownBranch) {
                isPluggedIn = mdbra.contains(sm);
            }
            if (sm instanceof IMonitorInitialize) {
                isPluggedIn = minit.contains(sm);
            }
            if (sm instanceof IMonitorInterruption) {
                isPluggedIn = minte.contains(sm);
            }
            if (sm instanceof IMonitorOpenNode) {
                isPluggedIn = mopno.contains(sm);
            }
            if (sm instanceof IMonitorRestart) {
                isPluggedIn = mrest.contains(sm);
            }
            if (sm instanceof IMonitorSolution) {
                isPluggedIn = msolu.contains(sm);
            }
            if (sm instanceof IMonitorUpBranch) {
                isPluggedIn = mubra.contains(sm);
            }
        }
        return isPluggedIn;
    }

    public void remove(ISearchMonitor sm) {
        if (sm != null) {
            if (sm instanceof IMonitorClose) {
                mclos.remove(sm);
            }
            if (sm instanceof IMonitorContradiction) {
                mcont.remove(sm);
            }
            if (sm instanceof IMonitorDownBranch) {
                mdbra.remove(sm);
            }
            if (sm instanceof IMonitorInitialize) {
                minit.remove(sm);
            }
            if (sm instanceof IMonitorInterruption) {
                minte.remove(sm);
            }
            if (sm instanceof IMonitorOpenNode) {
                mopno.remove(sm);
            }
            if (sm instanceof IMonitorRestart) {
                mrest.remove(sm);
            }
            if (sm instanceof IMonitorSolution) {
                msolu.remove(sm);
            }
            if (sm instanceof IMonitorUpBranch) {
                mubra.remove(sm);
            }
        }
    }

    public void reset() {
        mclos.clear();
        mcont.clear();
        mdbra.clear();
        minit.clear();
        minte.clear();
        mopno.clear();
        mrest.clear();
        msolu.clear();
        mubra.clear();
    }

}
