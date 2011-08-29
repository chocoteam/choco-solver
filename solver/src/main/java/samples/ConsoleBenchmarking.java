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
package samples;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 29/08/11
 */
public class ConsoleBenchmarking extends AbstractBenchmarking {

    BufferedReader _in;
    String _strCmd;

    public ConsoleBenchmarking() throws IOException {
        _in = new BufferedReader(new InputStreamReader(System.in));
    }

    public void run() {
        boolean quit = false;
        do {
            System.out.printf("Please indicate the command to execute (or \"quit\"):\n");
            try {
                _strCmd = _in.readLine();
            } catch (IOException e) {
                System.out.printf("IOException...!");
            }
            if (_strCmd.equalsIgnoreCase("quit")) {
                quit = true;
            } else {
                String[] cmd = _strCmd.split(" ");
                String[] args = new String[cmd.length - 1];
                System.arraycopy(cmd, 1, args, 0, args.length);
                try {
                    Class exec = Class.forName(cmd[0]);
                    Object inst = exec.newInstance();
                    if (inst instanceof AbstractProblem) {
                        AbstractProblem pb = (AbstractProblem) inst;
                        run(pb, args);
                    } else {
                        throw new ClassCastException(cmd[0] + " does not extend AbstractProblem");
                    }
                } catch (ClassNotFoundException e) {
                    System.out.printf("unknown class %s\n", cmd[0]);
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            System.out.println();
        } while (!quit);
        System.out.printf("Bye!\n");
    }


    public static void main(String[] args) throws IOException {
        new ConsoleBenchmarking().execute(args);
    }

}
