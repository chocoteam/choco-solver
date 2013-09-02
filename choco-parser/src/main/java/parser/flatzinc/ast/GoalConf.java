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
package parser.flatzinc.ast;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 26/11/12
 */
public class GoalConf {
    boolean free; // force free search
    int bbss; // set free search : 1: activity based, 2: impact based, 3: dom/wdeg
    boolean dec_vars; // use same decision pool as the one defines in the fzn file
    public boolean all; // search for all solutions
    long seed; // seed for random search
    boolean lastConflict;  // Search pattern
    long timeLimit;

    String description;

    boolean fastRestart;

    public enum LNS {
        NONE,
        RLNS,
        RLNS_BB,
        PGLNS,
        PGLNS_BB,
        ELNS,
        ELNS_BB,
        PGELNS_BB,
        APGELNS_BB

    }

    LNS lns;

    public GoalConf() {
        this(false, 0, false, false, 29091981L, false, -1, LNS.NONE, false);
    }

    public GoalConf(boolean free, int bbss, boolean dec_vars, boolean all, long seed, boolean lf, long timelimit, LNS lns, boolean fr) {
        this.free = free;
        this.bbss = bbss;
        this.dec_vars = dec_vars;
        this.seed = seed;
        this.all = all;
        this.lastConflict = lf;
        this.timeLimit = timelimit;
        this.lns = lns;
        this.fastRestart = fr;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }


}
