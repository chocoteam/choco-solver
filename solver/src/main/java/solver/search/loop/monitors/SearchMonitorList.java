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

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 09/05/11
 */
public final class SearchMonitorList implements ISearchMonitor {
    ISearchMonitor[] searchMonitors = new ISearchMonitor[4];
    int size = 0;

    @Override
    public void beforeInitialize() {
        for (int i = 0; i < size; i++) {
            searchMonitors[i].beforeInitialize();
        }
    }

    @Override
    public void afterInitialize() {
        for (int i = 0; i < size; i++) {
            searchMonitors[i].afterInitialize();
        }
    }

    @Override
    public void beforeInitialPropagation() {
        for (int i = 0; i < size; i++) {
            searchMonitors[i].beforeInitialPropagation();
        }
    }

    @Override
    public void afterInitialPropagation() {
        for (int i = 0; i < size; i++) {
            searchMonitors[i].afterInitialPropagation();
        }
    }

    @Override
    public void beforeOpenNode() {
        for (int i = 0; i < size; i++) {
            searchMonitors[i].beforeOpenNode();
        }
    }

    @Override
    public void afterOpenNode() {
        for (int i = 0; i < size; i++) {
            searchMonitors[i].afterOpenNode();
        }
    }

    @Override
    public void onSolution() {
        for (int i = 0; i < size; i++) {
            searchMonitors[i].onSolution();
        }
    }

    @Override
    public void beforeDownLeftBranch() {
        for (int i = 0; i < size; i++) {
            searchMonitors[i].beforeDownLeftBranch();
        }
    }

    @Override
    public void afterDownLeftBranch() {
        for (int i = 0; i < size; i++) {
            searchMonitors[i].afterDownLeftBranch();
        }
    }

    @Override
    public void beforeDownRightBranch() {
        for (int i = 0; i < size; i++) {
            searchMonitors[i].beforeDownRightBranch();
        }
    }

    @Override
    public void afterDownRightBranch() {
        for (int i = 0; i < size; i++) {
            searchMonitors[i].afterDownRightBranch();
        }
    }

    @Override
    public void beforeUpBranch() {
        for (int i = 0; i < size; i++) {
            searchMonitors[i].beforeUpBranch();
        }
    }

    @Override
    public void afterUpBranch() {
        for (int i = 0; i < size; i++) {
            searchMonitors[i].afterUpBranch();
        }
    }

    @Override
    public void onContradiction(ContradictionException cex) {
        for (int i = 0; i < size; i++) {
            searchMonitors[i].onContradiction(cex);
        }
    }

    @Override
    public void beforeRestart() {
        for (int i = 0; i < size; i++) {
            searchMonitors[i].beforeRestart();
        }
    }

    @Override
    public void afterRestart() {
        for (int i = 0; i < size; i++) {
            searchMonitors[i].afterRestart();
        }
    }

    @Override
    public void beforeClose() {
        for (int i = 0; i < size; i++) {
            searchMonitors[i].beforeClose();
        }
    }

    @Override
    public void afterClose() {
        for (int i = 0; i < size; i++) {
            searchMonitors[i].afterClose();
        }
    }

    public void add(ISearchMonitor sm) {
        if (sm != null) {
            if (size >= searchMonitors.length) {
                ISearchMonitor[] tmp = searchMonitors;
                searchMonitors = new ISearchMonitor[tmp.length * 2];
                System.arraycopy(tmp, 0, searchMonitors, 0, tmp.length);
            }
            searchMonitors[size++] = sm;
        }

    }

}
