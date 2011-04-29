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

package sandbox;

import choco.kernel.common.util.tools.StringUtils;
import solver.exception.ContradictionException;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 22 juil. 2010
 */
public class Sandbox2 {

    static int de = 10;

    volatile boolean alive = Boolean.TRUE;

    public static void main(String[] args) {
        new Sandbox2().main();
    }


    public void main() {
        long count;
        Container c;
        Timer tim;

        c = new Empty();
        for (int k = 0; k < de; k++) {
            alive = Boolean.TRUE;
            count = 0;
            tim = new Timer(this);
            tim.start();
            while (alive) {
                try {
                    c._throw();
                } catch (ContradictionException e) {
                    count++;
                }
            }
            System.out.println(StringUtils.pad("throw : ", 10, " ") + StringUtils.pad(Long.toString(count), -15, " "));
            c = new Full(c);
        }

        c = new Empty();
        for (int k = 0; k < de; k++) {
            alive = Boolean.TRUE;
            count = 0;
            tim = new Timer(this);
            tim.start();
            while (alive && c._enum() == Cas.ERROR) {
                count++;
            }
            System.out.println(StringUtils.pad("enum : ", 10, " ") + StringUtils.pad(Long.toString(count), -15, " "));
            c = new Full(c);
        }

        c = new Empty();
        for (int k = 0; k < de; k++) {
            alive = Boolean.TRUE;
            count = 0;
            tim = new Timer(this);
            tim.start();
            while (alive) {
                if (c._return() == -1) {
                    count++;
                }
            }
            System.out.println(StringUtils.pad("return : ", 10, " ") + StringUtils.pad(Long.toString(count), -15, " "));
            c = new Full(c);
        }

    }

    public void interrupt() {
        alive = Boolean.FALSE;
    }

    private enum Cas {
        @SuppressWarnings({"UnusedDeclaration"})NULL, ERROR
    }

    public interface Container {
        int _return();

        void _throw() throws ContradictionException;

        Cas _enum();
    }

    private static final class Empty implements Container {

        public int _return() {
            return -1;
        }

        public void _throw() throws ContradictionException {
            ContradictionException.throwIt(null, null, "container");
        }

        @Override
        public Cas _enum() {
            return Cas.ERROR;
        }
    }

    private static final class Full implements Container {
        final Container c;

        public Full(Container c) {
            this.c = c;
        }

        public int _return() {
            return c._return();
        }

        public void _throw() throws ContradictionException {
            c._throw();
        }

        @Override
        public Cas _enum() {
            return c._enum();
        }
    }

    private static final class Timer extends Thread {
        final Sandbox2 sb;

        private Timer(Sandbox2 sb) {
            this.sb = sb;
        }


        @Override
        public void run() {
            try {
                Thread.sleep(1000);
                sb.interrupt();
            } catch (InterruptedException ignored) {
            }
        }
    }


}