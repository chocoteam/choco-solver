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

package parser.flatzinc;

/*
* User : charles
* Mail : cprudhom(a)emn.fr
* Date : 22 oct. 2009
* Since : Choco 2.1.1
* Update : Choco 2.1.1
*
* Class to build a FlatZinc file from a MiniZinc file and data (if required).
*
* Using this class requires to MiniZinc installed.
*
*/


import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * For internal uses
 */

public class Mzn2fzn {

    @Option(name = "--mzn-dir", usage = "Specify the MiniZinc directory.", required = true, metaVar = "<minizinc dir>")
    private static String mznDir;

    @Option(name = "-lib", usage = "Specify directory containing the CHOCO standard library directory.",
            required = false, metaVar = "<choco_std dir>")
    private static String chocoLib;

    @Option(name = "-m", usage = "File named <model file> contains the model.", required = true, metaVar = "<model file>")
    private static String mznFile;

    @Option(name = "-d", usage = "File named <data file> contains data used by the model.", required = false, metaVar = "<data file>")
    private static String dznFile;

    @Option(name = "-o", usage = "Output the FlatZinc to the specified file rather than temp directory.", required = true,
            metaVar = "<fzn file>")
    private static String fznFile;

    private static final String CHOCO_STD = "choco_std";

    private final static String WHITSPACE = " ";

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    public static void main(String[] args) throws IOException, InterruptedException, URISyntaxException {
        // set default value to chocoLib
        chocoLib = Mzn2fzn.class.getResource("/std_lib").toURI().getPath();
        new Mzn2fzn().doMain(args);
    }

    public void doMain(String[] args) throws IOException {
        CmdLineParser parser = new CmdLineParser(this);
        parser.setUsageWidth(160);
        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            System.err.println("java SampleMain [options...]");
            parser.printUsage(System.err);
            System.err.println("\nCheck MiniZinc is correctly installed.");
            System.err.println();
            return;
        }
        mzn2fzn();
    }

    protected static void mzn2fzn() throws IOException {
        StringBuilder cmd = new StringBuilder();
        cmd.append(mznDir).append(File.separator).append("bin").append(File.separator).append("mzn2fzn");
        cmd.append(WHITSPACE)
                .append("-v ")
                .append("--stdlib-dir").append(WHITSPACE)
                .append(chocoLib).append(WHITSPACE)
                .append("-G").append(WHITSPACE)
                .append(CHOCO_STD).append(WHITSPACE)
                .append(mznFile).append(WHITSPACE)
                .append("-o").append(WHITSPACE)
                .append(fznFile).append(WHITSPACE);
        if (dznFile != null) {
            cmd.append("-d ").append(WHITSPACE)
                    .append(dznFile).append(WHITSPACE);
        }
        //System.out.println(cmd.toString());
        run(cmd.toString());
    }

    /**
     * Run a specific command
     *
     * @param cmd the command
     */
    private static void run(String cmd) {
        Process process;
        try {
            process = Runtime.getRuntime().exec(cmd);
            process.waitFor();
            process.destroy();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
