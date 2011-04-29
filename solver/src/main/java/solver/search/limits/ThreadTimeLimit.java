/**
*  Copyright (c) 2010, Ecole des Mines de Nantes
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

/**
 * Set a limit over the search time.
 * When this limit is reached, the search loop is informed and the resolution is stopped.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 15 juil. 2010
 */
public class ThreadTimeLimit extends Thread implements ILimit {

    private long timelimit;

    private final long threshold;

    private final long duration;

    private volatile boolean isreached;

    protected ThreadTimeLimit(long duration) {
        this.duration = duration;
        this.threshold = duration / 10;
        this.isreached = false;
    }

    @Override
    public void init() {
        this.timelimit = System.currentTimeMillis() + duration;
        setDaemon(true);
        this.start();
    }

    @Override
    public void run() {
        try {
            //TODO: to be checked!
            long sleep = duration - threshold;
            Thread.sleep(sleep);
            do {
                final long diff = timelimit - System.currentTimeMillis();
                isreached = (diff <= 0);
            } while (!isreached);
        } catch (InterruptedException ignored) {
        }
    }

    @Override
    public boolean isReached() {
        return isreached;
    }

    @Override
    public String toString() {
        return String.format("Time (ms): %d ", duration);
    }

    @Override
    public void update() {
    }
}
