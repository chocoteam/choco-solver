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
package org.chocosolver.util;

import org.testng.ITestResult;
import org.testng.TestListenerAdapter;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @version choco
 * @since 20/08/2014
 */
public class CustomListener extends TestListenerAdapter {
    private int m_count = 0;

    @Override
    public void onTestStart(ITestResult tr) {
        System.out.print(String.format("\t%s.%s ..", tr.getTestClass().getName(), tr.getName()));
    }

    @Override
    public void onTestFailure(ITestResult tr) {
        log(tr, "FAILURE");
    }

    @Override
    public void onTestSkipped(ITestResult tr) {
        log(tr, "SKIP");
    }

    @Override
    public void onTestSuccess(ITestResult tr) {
        log(tr, "SUCCESS");
    }

    private void log(ITestResult tr, String RESULT) {
        System.out.print(String.format(".. %s (%dms)\n", RESULT, tr.getEndMillis() - tr.getStartMillis()));
        if (++m_count % 40 == 0) {
            System.out.println("");
        }
    }

}
