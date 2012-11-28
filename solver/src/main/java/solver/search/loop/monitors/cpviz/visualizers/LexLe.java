/**
 * Copyright (c) 1999-2010, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package solver.search.loop.monitors.cpviz.visualizers;

import org.slf4j.Logger;
import solver.search.loop.monitors.cpviz.Visualizer;
import solver.search.strategy.decision.Decision;
import solver.variables.IntVar;

import static solver.search.loop.monitors.cpviz.visualizers.Writer._1;
import static solver.search.loop.monitors.cpviz.visualizers.Writer._2;

/**
 * A specialized visualizer for the lex less constraint.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 14/12/10
 */
public class LexLe extends Visualizer {

    private static final String type = "lex_le";

    final IntVar[] X, Y;

    /**
     * Build a visualizer for the lex less constraint
     *
     * @param X       array of domain variables
     * @param Y       array of domain variables
     * @param display "expanded" or "compact"
     * @param width   width of the visualizer
     * @param height  height of the visualizer
     */
    public LexLe(IntVar[] X, IntVar[] Y, String display, int width, int height) {
        super(type, display, width, height);
        this.X = X;
        this.Y = Y;
    }

    /**
     * Build a visualizer for the lex less constraint
     *
     * @param X       array of domain variables
     * @param Y       array of domain variables
     * @param display "expanded" or "compact"
     * @param x       coordinate of the visualizer in the x-axis (horizontal)
     * @param y       coordinate of the visualizer in the y-axis (vertical)
     * @param width   width of the visualizer
     * @param height  height of the visualizer
     * @param group   group name (to group multiple constraints)
     * @param min     expected minimal value of any of the domains
     * @param max     expected maximal value of any of the domains
     */
    public LexLe(IntVar[] X, IntVar[] Y, String display, int x, int y, int width, int height, String group, int min, int max) {
        super(type, display, x, y, width, height, group, min, max);
        this.X = X;
        this.Y = Y;
    }

    @Override
    protected void print(Logger logger, boolean focus, Decision decision) {
        writer.argumentIn(_1, 3).arrayDvar(X, 4).argumentOut(3);
        writer.argumentIn(_2, 3).arrayDvar(Y, 4).argumentOut(3);

        /*if (decision != null) {
            for (int x = 0; x < X.length; x++) {
                if (decision.getBranchingObject() == X[x]) {
                    if (focus) {
                        writer.focus(1 + "," + Integer.toString(x + 1), group, type);
                    } else {
                        writer.fail(1 + "," + Integer.toString(x + 1), group, decision.getBranchingValue());
                    }
                }
            }
            for (int y = 0; y < Y.length; y++) {
                if (decision.getBranchingObject() == Y[y]) {
                    if (focus) {
                        writer.focus(2 + "," + Integer.toString(y + 1), group, type);
                    } else {
                        writer.fail(2 + "," + Integer.toString(y + 1), group, decision.getBranchingValue());
                    }
                }
            }
        }*/
    }
}
