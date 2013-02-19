/*
 * Copyright (c) 1999-2012, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package solver.search.loop.monitors;

import common.util.tools.StringUtils;
import gnu.trove.map.hash.TObjectLongHashMap;
import org.slf4j.LoggerFactory;
import solver.ICause;
import solver.Solver;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.IVariableMonitor;
import solver.variables.Variable;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 30/01/12
 */
public class LogEventCount implements IVariableMonitor, IMonitorClose {

    final Solver solver;

    TObjectLongHashMap<EventType> countPerEvent;

    public LogEventCount(Solver solver) {
        this.solver = solver;
        for (int i = 0; i < solver.getNbVars(); i++) {
            solver.getVar(i).addMonitor(this);
        }
        countPerEvent = new TObjectLongHashMap<EventType>(EventType.values().length);
    }


    @Override
    public void beforeClose() {
    }

    @Override
    public void afterClose() {
        StringBuilder st = new StringBuilder();
        st.append(StringUtils.pad(" ", 41, "-")).append("\n");
        st.append("| ").append(StringUtils.pad("Event", 25, " "));
        st.append("| ").append(StringUtils.pad("Count", 12, " "));
        st.append("|\n");
        st.append(StringUtils.pad(" ", 41, "-")).append("\n");
        for (EventType e : EventType.values()) {
            st.append("| ").append(StringUtils.pad("" + e, 25, " "));
            st.append("| ").append(StringUtils.pad("" + countPerEvent.get(e), -12, " "));
            st.append("|\n");
        }
        st.append(StringUtils.pad(" ", 41, "-")).append("\n");
        LoggerFactory.getLogger("solver").info(st.toString());

    }

    @Override
    public void onUpdate(Variable var, EventType evt, ICause cause) throws ContradictionException {
        countPerEvent.adjustOrPutValue(evt, 1, 1);
    }
}
