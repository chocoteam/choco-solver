/**
 * Copyright (c) 1999-2010, Ecole des Mines de Nantes
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
package solver.search.loop.monitors.cpviz.visualizers;

import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.common.util.iterators.DisposableValueIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import solver.variables.IntVar;
import solver.variables.SetVar;
import solver.variables.Variable;
import solver.search.loop.monitors.cpviz.CPVizConstant;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 14/12/10
 */
public class Writer {

    private static final String[] PREFIX = {"", "\t", "\t\t", "\t\t\t", "\t\t\t\t", "\t\t\t\t\t", "\t\t\t\t\t\t"};

    public static final String _1 = "1", _2 = "2", _3 = "3", _S = " ";

    Logger logger = LoggerFactory.getLogger("visualization");

    private StringBuilder _st = new StringBuilder();


    protected Writer var(Variable var, String idx, int pf) {
        if (var instanceof IntVar) {
            IntVar _ivar = (IntVar) var;
            return ivar(_ivar, idx, pf);
        } else if (var instanceof SetVar) {
            SetVar svar = (SetVar) var;
            return svar(svar, idx, pf);
        }
        throw new UnsupportedOperationException("unknown type of var " + var.getClass());
    }

    protected Writer ivar(IntVar ivar, String idx, int pf) {
        if (ivar.instantiated()) {
            logger.info(CPVizConstant.V_INTEGER_TAG, new Object[]{prefix(pf), idx, ivar.getValue()});
        } else {
            _st.setLength(0);
            if (ivar.hasEnumeratedDomain()) {
                DisposableValueIterator it = ivar.getValueIterator(true);
                while (it.hasNext()) {
                    _st.append(it.next()).append(' ');
                }
                it.dispose();
            } else {
                _st.append(ivar.getLB()).append(" .. ").append(ivar.getUB());
            }

            logger.info(CPVizConstant.V_DVAR_TAG, new Object[]{prefix(pf), idx, _st.toString()});
        }
        return this;
    }

    protected Writer integer(int value, String idx, int pf) {
        logger.info(CPVizConstant.V_INTEGER_TAG, new Object[]{prefix(pf), idx, value});
        return this;
    }

    protected Writer svar(SetVar svar, String idx, int pf) {
        if (svar.instantiated()) {
            //logger.info(CPVizConstant.V_SINTEGER_TAG, new Object[]{prefix(pf), idx, domain(svar.getDomain().getKernelIterator())});
        } else {
            //logger.info(CPVizConstant.V_SVAR_TAG, new Object[]{prefix(pf), idx, domain(svar.getDomain().getKernelIterator()),
            //        domain(svar.getDomain().getEnveloppeIterator())});
        }
        throw new UnsupportedOperationException();
        //return this;
    }

    protected Writer arrayDvar(Variable[] vars, int pf) {
        for (int i = 0; i < vars.length; i++) {
            var(vars[i], Integer.toString(i + 1), pf);
        }
        return this;
    }

    protected Writer arrayDvar(IntVar[] vars, int pf) {
        for (int i = 0; i < vars.length; i++) {
            ivar(vars[i], Integer.toString(i + 1), pf);
        }
        return this;
    }

    protected Writer arrayDvar(SetVar[] vars, int pf) {
        for (int i = 0; i < vars.length; i++) {
            svar(vars[i], Integer.toString(i + 1), pf);
        }
        return this;
    }

    private String domain(DisposableIntIterator it) {
        _st.setLength(0);
        while (it.hasNext()) {
            _st.append(it.next()).append(' ');
        }
        it.dispose();

        return _st.toString();
    }

    protected Writer array(int[] values, int pf) {
        for (int i = 0; i < values.length; i++) {
            logger.info(CPVizConstant.V_INTEGER_TAG, new Object[]{prefix(pf), (i + 1), values[i]});
        }
        return this;
    }

    protected Writer argumentIn(String idx, int pf) {
        logger.info(CPVizConstant.V_ARGUMENT_TAG_IN, prefix(pf), idx);
        return this;
    }

    protected Writer argumentOut(int pf) {
        logger.info(CPVizConstant.V_ARGUMENT_TAG_OUT, prefix(pf));
        return this;
    }

    protected Writer tupleIn(String idx, int pf) {
        logger.info(CPVizConstant.V_TUPLE_TAG_IN, prefix(pf), idx);
        return this;
    }

    protected Writer tupleOut(int pf) {
        logger.info(CPVizConstant.V_TUPLE_TAG_OUT, prefix(pf));
        return this;
    }

    protected Writer collectionIn(String idx, int pf) {
        logger.info(CPVizConstant.V_COLLECTION_TAG_IN, prefix(pf), idx);
        return this;
    }

    protected Writer collectionOut(int pf) {
        logger.info(CPVizConstant.V_COLLECTION_TAG_OUT, prefix(pf));
        return this;
    }

    protected void focus(String idx, String group, String type) {
        logger.info(CPVizConstant.V_FOCUS_TAG, new Object[]{prefix(3), idx, group, type});
    }

    protected void focus(String idx, String group) {
        logger.info(CPVizConstant.V_FOCUS_NO_TYPE_TAG, new Object[]{prefix(3), idx, group});
    }

    protected void fail(String idx, String group, int value) {
        logger.info(CPVizConstant.V_FAILED_TAG, new Object[]{prefix(3), idx, group, value});
    }

    protected String prefix(int nb) {
        if (nb >= 0 && nb <= PREFIX.length) {
            return PREFIX[nb];
        } else if (nb > PREFIX.length) {
            final StringBuilder st = new StringBuilder();
            for (int i = 0; i < nb; i++) {
                st.append(PREFIX[1]);
            }
            return st.toString();
        }
        return PREFIX[0];

    }

}
