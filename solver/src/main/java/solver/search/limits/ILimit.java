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
 * <code>LimitChecker</code> is an abstract class extending <code>Thread</code>.
 * When it has been started, it sleeps during <code>sleeptime</code> and awake, check the particular limit
 * defined by the extension. If the limit is not reached, it falls asleep. Otherwise,
 * it informs the <code>AbstractSearchLoop</code> that it should stop.
 * A limit can only be used once. Using a limit twice, ie starting it more than one time, leads to a
 * {@link IllegalThreadStateException}.
 * <p/>
 * TODO: deal with multiple uses of a limit...
 * <br/>
 *
 * @author Charles Prud'homme
 * @see ThreadTimeLimit
 * @see solver.search.limits.NodeLimit
 * @see solver.search.limits.BacktrackLimit
 * @see solver.search.limits.FailLimit
 * @see solver.search.limits.SolutionLimit
 * @since 15 juil. 2010
 */
public interface ILimit {

    void init();

    boolean isReached();

    void update();


}
