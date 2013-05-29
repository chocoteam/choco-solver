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

package solver.search.limits;

import solver.search.loop.AbstractSearchLoop;
import solver.search.loop.monitors.IMonitorInitialize;
import solver.search.loop.monitors.IMonitorOpenNode;
import solver.search.loop.monitors.IMonitorUpBranch;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * This classes provides services to <code>add</code> and <code>delete</code> limits over the search loop.
 * At the very beginning of the search, the limit checkers are started, using <code>start</code>, and are
 * stopped, using <code>interrupt</code> at the end of the search.
 * During the search, when a limit is reached, the search loop is interrupted, and the resolution stops.
 * <p/>
 * <br/>
 *
 * @author Charles Prud'homme
 * @see ILimit
 * @since 15 juil. 2010
 */
public class LimitChecker implements Serializable, IMonitorInitialize, IMonitorOpenNode, IMonitorUpBranch {

    private ArrayList<ILimit> limits;
    final AbstractSearchLoop searchloop;


    public LimitChecker(AbstractSearchLoop searchloop) {
        this.limits = new ArrayList<ILimit>();
        this.searchloop = searchloop;
    }

    /**
     * Adds a <code>limit</code> to the pool of limit checkers.
     *
     * @param limit object to add
     */
    public void add(ILimit limit) {
        if (limits.isEmpty()) { // if not plugged uet
            searchloop.plugSearchMonitor(this);
        }
        limits.add(limit);
    }

    public void remove(ILimit limit) {
        if (limits.remove(limit) && limits.isEmpty()) {
//            searchloop.unplugSearchMonitor(this);
        }
    }

    public void clear() {
        limits.clear();
    }

    public void hasEncounteredLimit() {
        for (int i = 0; i < limits.size(); i++) {
            if (limits.get(i).isReached()) {
                searchloop.interrupt();
                return;
            }
        }
    }

    /**
     * Inits the limit checkers
     */
    public void init() {
        for (int i = 0; i < limits.size(); i++) {
            limits.get(i).init();
        }
    }

    public boolean isReached() {
        for (int i = 0; i < limits.size(); i++) {
            if (limits.get(i).isReached()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the array of limits
     *
     * @return
     */
    public ArrayList<ILimit> getLimits() {
        return limits;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    @Override
    public void beforeInitialize() {
    }

    @Override
    public void afterInitialize() {
        this.init();
    }

    @Override
    public void beforeOpenNode() {
    }

    @Override
    public void afterOpenNode() {
        this.hasEncounteredLimit();
    }

    @Override
    public void beforeUpBranch() {
    }

    @Override
    public void afterUpBranch() {
        this.hasEncounteredLimit();
    }
}
