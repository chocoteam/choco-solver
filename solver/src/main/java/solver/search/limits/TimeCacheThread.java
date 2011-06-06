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

import solver.exception.SolverException;

/**
 * fast time limit computation inspired from: http://dow.ngra.de/2008/10/27/when-systemcurrenttimemillis-is-too-slow/.
 * cant use the heartbeat counter because it lascks of precision.
 *
 * @author Arnaud Malapert</br>
 * @version 2.1.1</br>
 * @since 23 juil. 2009 version 2.1.1</br>
 */
public final class TimeCacheThread extends Thread {

    public final static int MS_TIME_PRECISION = 100;

    public static volatile long currentTimeMillis = System.currentTimeMillis();


    private TimeCacheThread() {
        super("TimeCachedThread");
        setDaemon(true);
    }


    static {
        new TimeCacheThread().start();
    }

    @Override
    public void run() {
        while (true) {
            currentTimeMillis = System.currentTimeMillis();
            try {
                Thread.sleep(MS_TIME_PRECISION);
            } catch (InterruptedException e) {
                throw new SolverException("Time Limit Thread was interrupted");
            }
        }
    }

    @Override
    public String toString() {
        return "TimeCacheThread";
    }
}
