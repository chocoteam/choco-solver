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
import solver.search.strategy.decision.Decision;
import solver.variables.IntVar;
import solver.search.loop.monitors.cpviz.Visualizer;

/**
 * A specialized visualizer for the bin packing constraint.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 14/12/10
 */
public class BinPacking extends Visualizer {

    private static final String type = "bin_packing";

    final IntVar[] items;

    final int[] sizes;

    final IntVar[] bins;

    /**
     * Build a visualizer for the bin packing constraint
     *
     * @param items   todo to complete
     * @param sizes   todo to complete
     * @param bins    todo to complete
     * @param display "compact" or "expanded"
     * @param width   width of the visualizer
     * @param height  height of the visualizer
     */
    public BinPacking(IntVar[] items, int[] sizes, IntVar[] bins, String display, int width, int height) {
        super(type, display, width, height);
        // BEWARE: duplicate first item, as fake object, at position 0, due to iteration starting at 1 in CPViz...
        this.items = new IntVar[items.length + 1];
        this.items[0] = items[0];
        System.arraycopy(items, 0, this.items, 1, items.length);
        // BEWARE: duplicate first size, as fake object, at position 0, due to iteration starting at 1 in CPViz...
        this.sizes = new int[sizes.length + 1];
        this.sizes[0] = sizes[0];
        System.arraycopy(sizes, 0, this.sizes, 1, sizes.length);
        // BEWARE: duplicate first bin, as fake object, at position 0, due to iteration starting at 1 in CPViz...
        this.bins = new IntVar[bins.length + 1];
        this.bins[0] = bins[0];
        System.arraycopy(bins, 0, this.bins, 1, bins.length);
    }

    /**
     * Build a visualizer for the bin packing constraint
     *
     * @param items   todo to complete
     * @param sizes   todo to complete
     * @param bins    todo to complete
     * @param display "expanded" or "compact"
     * @param x       coordinate of the visualizer in the x-axis (horizontal)
     * @param y       coordinate of the visualizer in the y-axis (vertical)
     * @param width   width of the visualizer
     * @param height  height of the visualizer
     * @param group   group name (to group multiple constraints)
     * @param min     expected minimal value of any of the domains
     * @param max     expected maximal value of any of the domains
     */
    public BinPacking(IntVar[] items, int[] sizes, IntVar[] bins, String display, int x, int y, int width, int height, String group, int min, int max) {
        super(type, display, x, y, width, height, group, min, max);
        this.items = items;
        this.sizes = sizes;
        this.bins = bins;
    }

    @Override
    protected void print(Logger logger, boolean focus, Decision decision) {
        writer.argumentIn("items", 3).arrayDvar(items, 4).argumentOut(3);
        writer.argumentIn("sizes", 3).array(sizes, 4).argumentOut(3);
        writer.argumentIn("bins", 3).arrayDvar(bins, 4).argumentOut(3);
    }
}
