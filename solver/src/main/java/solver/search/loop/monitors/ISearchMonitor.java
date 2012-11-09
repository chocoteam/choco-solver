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
import solver.exception.ContradictionException;

import java.io.Serializable;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 06/05/11
 */
public interface ISearchMonitor extends Serializable{

    static Logger LOGGER = LoggerFactory.getLogger(ISearchMonitor.class);

//    static int
//            beforeInitialize = 1 << 1,
//            afterInitialize = 1 << 2,
//            beforeInitialPropagation = 1 << 3,
//            afterInitialPropagation = 1 << 4,
//            beforeOpenNode = 1 << 5,
//            afterOpenNode = 1 << 6,
//            onSolution = 1 << 7,
//            beforeDownLeftBranch = 1 << 8,
//            afterDownLeftBranch = 1 << 9,
//            beforeDownRightBranch = 1 << 10,
//            afterDownRightBranch = 1 << 11,
//            beforeUpBranch = 1 << 12,
//            afterUpBranch = 1 << 13,
//            onContradiction = 1 << 14,
//            beforeRestart = 1 << 15,
//            afterRestart = 1 << 16,
//            beforeClose = 1 << 17,
//            afterClose = 1 << 18;

//    int getMonitorMask();

    void beforeInitialize();

    void afterInitialize();

    void beforeInitialPropagation();

    void afterInitialPropagation();

    void beforeOpenNode();

    void afterOpenNode();

    void onSolution();

    void beforeDownLeftBranch();

    void afterDownLeftBranch();

    void beforeDownRightBranch();

    void afterDownRightBranch();

    void beforeUpBranch();

    void afterUpBranch();

    void onContradiction(ContradictionException cex);

    void beforeRestart();

    void afterRestart();

    void afterInterrupt();

    void beforeClose();

    void afterClose();
}
