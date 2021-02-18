/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
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
        IMonitorInitialize, IMonitorOpenNode, IMonitorRestart,
        IMonitorSolution, IMonitorUpBranch {

    /**
     * Close monitors
     */
    private List<IMonitorClose> mclos = new ArrayList<>();

    /**
     * Contradiction monitors
     */
    private List<IMonitorContradiction> mcont = new ArrayList<>();

    /**
     * Down branch monitors
     */
    private List<IMonitorDownBranch> mdbra = new ArrayList<>();

    /**
     * Initialize monitors
     */
    private List<IMonitorInitialize> minit = new ArrayList<>();

    /**
     * Open node monitors
     */
    private List<IMonitorOpenNode> mopno = new ArrayList<>();

    /**
     * Restart monitors
     */
    private List<IMonitorRestart> mrest = new ArrayList<>();

    /**
     * Solution monitors
     */
    private List<IMonitorSolution> msolu = new ArrayList<>();

    /**
     * Up branch monitors
     */
    private List<IMonitorUpBranch> mubra = new ArrayList<>();


    @Override
    public void beforeInitialize() {
        for (int i = 0; i < minit.size(); i++) {
            minit.get(i).beforeInitialize();
        }
    }

    @Override
    public void afterInitialize(boolean correct) {
        for (int i = 0; i < minit.size(); i++) {
            minit.get(i).afterInitialize(correct);
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

    /**
     * Adds a search monitor to this list
     * @param sm a search monitor
     */
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

    /**
     * Checks if this list contains a search monitor.
     * @param sm a search monitor
     * @return <tt>true</tt> if this list contains <code>sm</code>, <tt>false</tt> otherwise.
     */
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

    /**
     * Removes a search monitor for this list.
     * @param sm a search monitor.
     */
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

    /**
     * Clears all internal lists of search monitors.
     */
    public void reset() {
        mclos.clear();
        mcont.clear();
        mdbra.clear();
        minit.clear();
        mopno.clear();
        mrest.clear();
        msolu.clear();
        mubra.clear();
    }

}
