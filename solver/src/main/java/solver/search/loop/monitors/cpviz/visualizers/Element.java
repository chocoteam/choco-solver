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

import static solver.search.loop.monitors.cpviz.visualizers.Writer.*;

/**
 * A specialized visualizer for a vector the element constraint.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 13/12/10
 */
public final class Element extends Visualizer {

    private static final String type = "element";

    final IntVar index;
    final int[] values;
    final IntVar value;


    /**
     * Build a visualizer for the element constraint
     *
     * @param index   domain variable
     * @param values  collection of values
     * @param value   domain variable
     * @param display "expanded" or "compact"
     * @param width   width of the visualizer
     * @param height  height of the visualizer
     */
    public Element(IntVar index, int[] values, IntVar value, String display, int width, int height) {
        super(type, display, width, height);
        this.index = index;
        this.values = values;
        this.value = value;
    }

    /**
     * Build a visualizer for the element constraint
     *
     * @param index   domain variable
     * @param values  collection of values
     * @param value   domain variable
     * @param display "expanded" or "compact"
     * @param x       coordinate of the visualizer in the x-axis (horizontal)
     * @param y       coordinate of the visualizer in the y-axis (vertical)
     * @param width   width of the visualizer
     * @param height  height of the visualizer
     * @param group   group name (to group multiple constraints)
     * @param min     expected minimal value of any of the domains
     * @param max     expected maximal value of any of the domains
     */
    public Element(IntVar index, int[] values, IntVar value, String display, int x, int y, int width, int height, String group, int min, int max) {
        super(type, display, x, y, width, height, group, min, max);
        this.index = index;
        this.values = values;
        this.value = value;
    }

    @Override
    protected void print(Logger logger, boolean focus, Decision decision) {
        writer.argumentIn(_1, 3).ivar(index, _1, 4).argumentOut(3);

        if (decision != null && decision.getDecisionVariable() == index) {
            if (focus) {
                writer.focus(_1 + _S + Integer.toString(1), group, type);
            } else {
                writer.fail(_1 + _S + Integer.toString(1), group, (Integer)decision.getDecisionValue());
            }
        }
        writer.argumentIn(_2, 3).array(values, 4).argumentOut(3);

        writer.argumentIn(_3, 3).ivar(value, _3, 4).argumentOut(3);
        if (decision != null && decision.getDecisionVariable() == value) {
            if (focus) {
                writer.focus(_3 + _S + Integer.toString(3), group, type);
            } else {
                writer.fail(_3 + _S + Integer.toString(3), group, (Integer)decision.getDecisionValue());
            }
        }
    }
}
