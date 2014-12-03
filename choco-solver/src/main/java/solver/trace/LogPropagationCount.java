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
package org.chocosolver.solver.trace;

import gnu.trove.map.hash.TObjectLongHashMap;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.util.tools.StringUtils;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 30/01/12
 */
public class LogPropagationCount {

    public String print(Solver solver) {
        Constraint[] cstrs = solver.getCstrs();
        TObjectLongHashMap<String> fcounter = new TObjectLongHashMap<>();
        TObjectLongHashMap<String> ccounter = new TObjectLongHashMap<>();
        for (int i = 0; i < cstrs.length; i++) {
            Propagator[] props = cstrs[i].getPropagators();
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
        return st.toString();
    }
}
