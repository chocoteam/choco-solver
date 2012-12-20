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

import choco.kernel.common.util.tools.StringUtils;
import gnu.trove.map.hash.TObjectLongHashMap;
import org.slf4j.LoggerFactory;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 30/01/12
 */
public class LogPropagationCount implements IMonitorClose {

    final Solver solver;

    public LogPropagationCount(Solver solver) {
        this.solver = solver;
    }


    @Override
    public void beforeClose() {
    }

    @Override
    public void afterClose() {
        Constraint[] cstrs = solver.getCstrs();
        TObjectLongHashMap<String> fcounter = new TObjectLongHashMap<String>();
        TObjectLongHashMap<String> ccounter = new TObjectLongHashMap<String>();
        for (int i = 0; i < cstrs.length; i++) {
            Propagator[] props = cstrs[i].propagators;
            for (int j = 0; j < props.length; j++) {
                String clazz = props[j].getClass().getSimpleName();
                fcounter.adjustOrPutValue(clazz, props[j].fineERcalls, props[j].fineERcalls);
                ccounter.adjustOrPutValue(clazz, props[j].coarseERcalls, props[j].coarseERcalls);
            }
        }
        String[] classes = fcounter.keys(new String[fcounter.size()]);
        StringBuilder st = new StringBuilder();
        st.append(StringUtils.pad(" ", 50, "-")).append("\n");
        st.append("| ").append(StringUtils.pad("Name", 30, " "));
        st.append("| ").append(StringUtils.pad("fine", 7, " "));
        st.append("| coarse |\n");
        st.append(StringUtils.pad(" ", 50, "-")).append("\n");
        long fsum = 0, csum = 0;
        for (int i = 0; i < classes.length; i++) {
            st.append("| ").append(StringUtils.pad("" + classes[i], 30, " "));
            st.append("| ").append(StringUtils.pad("" + fcounter.get((classes[i])), -7, " "));
            fsum += fcounter.get((classes[i]));
            st.append("| ").append(StringUtils.pad("" + ccounter.get((classes[i])), -7, " "));
            csum += ccounter.get((classes[i]));
            st.append("|\n");
        }
        st.append(StringUtils.pad(" ", 50, "-")).append("\n");
        st.append("| ").append(StringUtils.pad("", 30, " "));
        st.append("| ").append(StringUtils.pad("" + fsum, -7, " "));
        st.append("| ").append(StringUtils.pad("" + csum, -7, " "));
        st.append("|\n");
        st.append(StringUtils.pad(" ", 50, "-")).append("\n");
        LoggerFactory.getLogger("solver").info(st.toString());

    }
}
