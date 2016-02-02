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
package org.chocosolver.samples;

import org.chocosolver.util.tools.ArrayUtils;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.lang.management.ManagementFactory;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 29/08/11
 */
public abstract class AbstractBenchmarking {

    @Option(name = "-loop", usage = "Number of time a sample should be run", required = false)
    int loop = 1;

    @Option(name = "-warmUp", usage = "JVM warm up loop", required = false)
    int warmUp = 0;

    @Option(name = "-noJVMclean", usage = "Disable JVM cleaning", required = false)
    boolean noJVMcleaning = false;


    public final void execute(String... args) {
        readArgs(args);
        run();
    }


    public final void readArgs(String... args) {
        CmdLineParser parser = new CmdLineParser(this);
        parser.setUsageWidth(160);
        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            System.err.println("java " + this.getClass() + " [options...]");
            parser.printUsage(System.err);
            System.err.println();
            throw new UnsupportedOperationException("error while reading arguments in "+getClass().getSimpleName());
        }
    }

    protected void cleanJVM() {
        long memUsedPrev = memoryUsed();
        for (int i = 0; i < 100; i++) {
            System.runFinalization();    // see also: http://java.sun.com/developer/technicalArticles/javase/finalization/
            System.gc();
            long memUsedNow = memoryUsed();
            if (    // break early if have no more finalization and get constant mem used
                    (ManagementFactory.getMemoryMXBean().getObjectPendingFinalizationCount() == 0) &&
                            (memUsedNow >= memUsedPrev)
                    ) {
                break;
            } else {
                memUsedPrev = memUsedNow;
            }
        }
    }

    /**
     * Returns how much memory on the heap is currently being used.
     */
    public static long memoryUsed() {
        Runtime rt = Runtime.getRuntime();
        return rt.totalMemory() - rt.freeMemory();
    }

    protected abstract void run();

    protected void run(AbstractProblem pb, String[] args) {
        if (warmUp > 0) {
            System.out.printf("Warm up JVM");
            AbstractProblem.Level level = pb.level;
            String[] _args = args.clone();
            boolean found = false;
            for (int i = 0; i < args.length; i++) {
                if (_args[i].equals("-l")) {
                    _args[i + 1] = "SILENT";
                    found = true;
                    break;
                }
            }
            if (!found) {
                _args = ArrayUtils.append(_args, new String[]{"-l", "SILENT"});
            }
            for (int i = warmUp; i >= 0; i--) {
                pb.execute(_args);
                if (!noJVMcleaning) cleanJVM();
                System.out.printf(".");
            }
            pb.level = level; // restore initial level
            System.out.printf("OK\n");
        }
        for (int i = loop - 1; i >= 0; i--) {
            pb.execute(args);
            if (!noJVMcleaning) cleanJVM();
        }
    }

}
