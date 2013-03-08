/**
 *  Copyright (c) 1999-2010, Ecole des Mines de Nantes
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
package solver.search.loop.monitors.cpviz;

/**
 * List of XML tags required for CP Viz log files
 * <p/>
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 6 dec. 2010
 */
@SuppressWarnings({"UnusedDeclaration"})
public class CPVizConstant {

    public static final String HEADER =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<!-- choco-solver -->";

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //******************************************* CONFIGURATION ******************************************************//
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static final String C_CONF_TAG_IN = "<configuration version=\"1.0\" directory=\"{}\" idx=\"{}\">";
    public static final String C_CONF_TAG_OUT = "</configuration>";

    public static final String C_TOOL_TAG = "\t<tool show=\"{}\" type=\"{}\" display=\"{}\" repeat=\"{}\" " +
            "width=\"{}\" height=\"{}\" fileroot=\"{}\" />";

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //******************************************* TREE ***************************************************************//
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static final String T_TREE_TAG_IN = "<tree version=\"1.0\" >";
    public static final String T_TREE_TAG_OUT = "</tree>";

    public static final String T_ROOT_TAG = "\t<root id=\"0\" />";
    public static final String T_TRY_TAG = "\t<try id=\"{}\" parent=\"{}\" name=\"{}\" size=\"{}\" value=\"{}\" />";
    public static final String T_FAIL_TAG = "\t<fail id=\"{}\" parent=\"{}\" name=\"{}\" size=\"{}\" value=\"{}\" />";
    public static final String T_SUCC_TAG = "\t<succ id=\"{}\" />";

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //******************************************* VISUALIZATION*******************************************************//
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static final String V_VISUALIZATION_TAG_IN = "<visualization version=\"1.0\" >";
    public static final String V_VISUALIZATION_TAG_OUT = "</visualization>";

    public static final String V_VISUALIZER_TAG = "\t<visualizer id=\"{}\" type=\"{}\" display=\"{}\" width=\"{}\" height=\"{}\" {}/>";


    public static final String V_ARGUMENT_TAG_IN = "{}<argument index=\"{}\">";
    public static final String V_ARGUMENT_TAG_OUT = "{}</argument>";

    public static final String V_COLLECTION_TAG_IN = "{}<collection index=\"{}\">";
    public static final String V_COLLECTION_TAG_OUT = "{}</collection>";

    public static final String V_DVAR_TAG = "{}<dvar index=\"{}\" domain=\"{}\" />";

    public static final String V_FAILED_TAG = "{}<failed index=\"{}\" group=\"{}\" value=\"{}\"/>";

    public static final String V_FOCUS_TAG = "{}<focus index=\"{}\" group=\"{}\" type=\"{}\"/>";

    public static final String V_FOCUS_NO_TYPE_TAG = "{}<focus index=\"{}\" group=\"{}\" />";

    public static final String V_INTEGER_TAG = "{}<integer index=\"{}\" value=\"{}\" />";

    public static final String V_OTHER_TAG = "{}<other index=\"{}\" value=\"{}\" />";

    public static final String V_SINTEGER_TAG = "{}<sinteger index=\"{}\" value=\"{}\" />";

    public static final String V_STATE_TAG_IN = "\t<state id=\"{}\" tree_node=\"{}\" >";
    public static final String V_STATE_TAG_OUT = "\t</state>";

    public static final String V_SVAR_TAG = "{}<svar index=\"{}\" low=\"{}\" high=\"{}\" />";

    public static final String V_TUPLE_TAG_IN = "{}<tuple index=\"{}\">";
    public static final String V_TUPLE_TAG_OUT = "{}</tuple>";

    public static final String V_VISUALIZER_STATE_TAG_IN = "\t\t<visualizer_state id=\"{}\" >";
    public static final String V_VISUALIZER_STATE_TAG_OUT = "\t\t</visualizer_state>";


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static final String ALL = "all";

    public static final String COMPACT = "compact";

    public static final String EXPANDED = "expanded";

    public static final String FINAL = "final";

    public static final String GRAPH = "graph";

    public static final String I = "i";

    public static final String LAYOUT = "layout";

    public static final String minusI = "-i";

    public static final String VALUES = "values";

}
