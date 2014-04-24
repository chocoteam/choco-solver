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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import solver.Solver;
import solver.search.loop.ISearchLoop;
import util.tools.StringUtils;

/**
 * A search monitor logger which prints choices during the search.
 * <p/>
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 09/05/11
 */
public class LogChoices implements IMonitorDownBranch {

    private static Logger LOGGER = LoggerFactory.getLogger("solver");

    final Solver solver;
    final ISearchLoop searchLoop;
    final IMessage message;

    public LogChoices(Solver solver, IMessage message) {
        this.solver = solver;
        this.searchLoop = solver.getSearchLoop();
        this.message = message;
    }


    @Override
    public void beforeDownLeftBranch() {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("{}[L]{} //{}", new Object[]{
                    StringUtils.pad("", solver.getEnvironment().getWorldIndex(), "."),
                    searchLoop.getLastDecision().toString(), message.print()});
        }
    }

    @Override
    public void afterDownLeftBranch() {
    }

    @Override
    public void beforeDownRightBranch() {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("{}[R]{} //{}", new Object[]{
                    StringUtils.pad("", solver.getEnvironment().getWorldIndex(), "."),
                    searchLoop.getLastDecision().toString(), message.print()});
        }
    }

    @Override
    public void afterDownRightBranch() {
    }
}
