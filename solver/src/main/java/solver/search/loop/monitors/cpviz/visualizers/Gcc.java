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

/**
 * A specialized visualizer for the global cardinality constraint.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 14/12/10
 */
public class Gcc extends Visualizer {

    private static final String type = "gcc";

    final IntVar[] vars;
    final int[] values, low, high;

    /**
     * Build a visualizer for the global cardinality constraint
     *
     * @param vars    domain variables
     * @param values  collection of values
     * @param low     minimum number of occurrences of values
     * @param high    maximum number of occurrences
     * @param display "expanded" or "compact"
     * @param width   width of the visualizer
     * @param height  height of the visualizer
     */
    public Gcc(IntVar[] vars, int[] values, int[] low, int[] high, String display, int width, int height) {
        super(type, display, width, height);
        this.vars = vars;
        this.values = values;
        this.low = low;
        this.high = high;
    }

    /**
     * Build a visualizer for the global cardinality constraint
     *
     * @param vars    domain variables
     * @param values  collection of values
     * @param low     minimum number of occurrences of values
     * @param high    maximum number of occurrences
     * @param display "expanded" or "compact"
     * @param x       coordinate of the visualizer in the x-axis (horizontal)
     * @param y       coordinate of the visualizer in the y-axis (vertical)
     * @param width   width of the visualizer
     * @param height  height of the visualizer
     * @param group   group name (to group multiple constraints)
     * @param min     expected minimal value of any of the domains
     * @param max     expected maximal value of any of the domains
     */
    public Gcc(IntVar[] vars, int[] values, int[] low, int[] high, String display, int x, int y, int width, int height, String group, int min, int max) {
        super(type, display, x, y, width, height, group, min, max);
        this.vars = vars;
        this.values = values;
        this.low = low;
        this.high = high;
    }

    @Override
    protected void print(Logger logger, boolean focus, Decision decision) {
        writer.argumentIn("1", 3);
        for (int i = 0; i < values.length; i++) {
            writer.tupleIn(Integer.toString(i + 1), 4)
                    .integer(values[i], "value", 5)
                    .integer(low[i], "low", 5)
                    .integer(high[i], "high", 5)
                    .tupleOut(4);
        }
        writer.argumentOut(3);

        writer.argumentIn("2", 3).arrayDvar(vars, 4).argumentOut(3);
    }
}
