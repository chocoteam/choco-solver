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
package org.chocosolver.solver.trace;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.loop.monitors.IMonitorClose;
import org.chocosolver.solver.search.loop.monitors.IMonitorInitialize;

/**
 * A search monitor logger which prints statistics every XX ms.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 18 aug. 2010
 */
public class LogStatEveryXXms implements IMonitorInitialize, IMonitorClose {

    Thread printer;
    boolean alive;

    public LogStatEveryXXms(final Solver solver, final long duration) {

        printer = new Thread() {

            @Override
            public void run() {
                alive = true;
                try {
                    sleep(duration);
                    //noinspection InfiniteLoopStatement
                    do {
                        solver.getMeasures().updateTime();
                        Chatterbox.out.println(String.format(">> %s", solver.getMeasures().toOneShortLineString()));
                        sleep(duration);
                    } while (alive);
                } catch (InterruptedException ignored) {
                }
            }
        };
        printer.setDaemon(true);
    }

    @Override
    public void afterInitialize() {
        printer.start();
    }

    @Override
    public void afterClose() {
        alive = false;
    }
}
