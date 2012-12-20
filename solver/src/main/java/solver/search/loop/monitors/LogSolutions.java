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
import solver.search.loop.AbstractSearchLoop;
import solver.variables.Variable;

/**
 * A search monitor logger which prints solution during the search.
 * <p/>
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 09/05/11
 */
public final class LogSolutions implements IMonitorSolution {

    private static Logger LOGGER = LoggerFactory.getLogger("solver");

    final AbstractSearchLoop searchLoop;

    final ISolutionFormat format;

    public LogSolutions(AbstractSearchLoop searchLoop, ISolutionFormat format) {
        this.searchLoop = searchLoop;
        this.format = format;
    }

    public LogSolutions(final AbstractSearchLoop searchLoop) {
        this(searchLoop, new ISolutionFormat() {
            @Override
            public String print() {
                return String.format("- Solution #{} found. {} \n\t{}.",
                        new Object[]{searchLoop.getMeasures().getSolutionCount(),
                                searchLoop.getMeasures().toOneShortLineString(),
                                print(searchLoop.getStrategy().vars)
                        });
            }

            private String print(Variable[] vars) {
                StringBuilder s = new StringBuilder(32);
                for (Variable v : vars) {
                    s.append(v).append(' ');
                }
                return s.toString();

            }
        });
    }

    @Override
    public void onSolution() {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(format.print());
        }
    }


}
