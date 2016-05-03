/*
 * Copyright (c) 1999-2012, Ecole des Mines de Nantes
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

package org.chocosolver.parser.flatzinc.ast;

import org.chocosolver.parser.flatzinc.ast.expression.EAnnotation;
import org.chocosolver.parser.flatzinc.ast.expression.Expression;
import org.chocosolver.solver.Model;

import java.util.List;

/*
* User : CPRUDHOM
* Mail : cprudhom(a)emn.fr
* Date : 12 janv. 2010
* Since : Choco 2.1.1
*
* Constraint builder from flatzinc-like object.
*/
public enum FConstraint {

    array_bool_and {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new org.chocosolver.parser.flatzinc.ast.constraints.ArrayBoolAndBuilder().build(model, id, exps, annotations, datas);
        }
    },
    array_bool_element {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new org.chocosolver.parser.flatzinc.ast.constraints.ArrayElementBuilder().build(model, id, exps, annotations, datas);
        }
    },
    array_bool_or {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new org.chocosolver.parser.flatzinc.ast.constraints.ArrayBoolOrBuilder().build(model, id, exps, annotations, datas);
        }
    },
    array_bool_xor {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new org.chocosolver.parser.flatzinc.ast.constraints.ArrayBoolXorBuilder().build(model, id, exps, annotations, datas);
        }
    },
    array_int_element {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new org.chocosolver.parser.flatzinc.ast.constraints.ArrayElementBuilder().build(model, id, exps, annotations, datas);
        }
    },
    array_var_bool_element {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new org.chocosolver.parser.flatzinc.ast.constraints.ArrayVarElementBuilder().build(model, id, exps, annotations, datas);
        }
    },
    array_var_int_element {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new org.chocosolver.parser.flatzinc.ast.constraints.ArrayVarElementBuilder().build(model, id, exps, annotations, datas);
        }
    },
    bool2int {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new org.chocosolver.parser.flatzinc.ast.constraints.Bool2IntBuilder().build(model, id, exps, annotations, datas);
        }
    },
    bool_and {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new org.chocosolver.parser.flatzinc.ast.constraints.BoolAndBuilder().build(model, id, exps, annotations, datas);
        }
    },
    bool_clause {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new org.chocosolver.parser.flatzinc.ast.constraints.BoolClauseBuilder().build(model, id, exps, annotations, datas);
        }
    },
    bool_eq {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new org.chocosolver.parser.flatzinc.ast.constraints.BoolEqBuilder().build(model, id, exps, annotations, datas);
        }
    },
    bool_eq_reif {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new org.chocosolver.parser.flatzinc.ast.constraints.BoolEqReifBuilder().build(model, id, exps, annotations, datas);
        }
    },
    bool_le {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new org.chocosolver.parser.flatzinc.ast.constraints.BoolLeBuilder().build(model, id, exps, annotations, datas);
        }
    },
    bool_le_reif {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new org.chocosolver.parser.flatzinc.ast.constraints.BoolLeReifBuilder().build(model, id, exps, annotations, datas);
        }
    },
    bool_lin_eq {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new org.chocosolver.parser.flatzinc.ast.constraints.IntLinEqBuilder().build(model, id, exps, annotations, datas);
        }
    },
    bool_lin_le {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new org.chocosolver.parser.flatzinc.ast.constraints.IntLinLeBuilder().build(model, id, exps, annotations, datas);
        }
    },
    bool_lt {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new org.chocosolver.parser.flatzinc.ast.constraints.BoolLtBuilder().build(model, id, exps, annotations, datas);
        }
    },
    bool_lt_reif {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new org.chocosolver.parser.flatzinc.ast.constraints.BoolLtReifBuilder().build(model, id, exps, annotations, datas);
        }
    },
    bool_not {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new org.chocosolver.parser.flatzinc.ast.constraints.BoolNotBuilder().build(model, id, exps, annotations, datas);
        }
    },
    bool_or {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new org.chocosolver.parser.flatzinc.ast.constraints.BoolOrBuilder().build(model, id, exps, annotations, datas);
        }
    },
    bool_xor {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new org.chocosolver.parser.flatzinc.ast.constraints.BoolXorBuilder().build(model, id, exps, annotations, datas);
        }
    },
    int_abs {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new org.chocosolver.parser.flatzinc.ast.constraints.IntAbsBuilder().build(model, id, exps, annotations, datas);
        }
    },
    int_div {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new org.chocosolver.parser.flatzinc.ast.constraints.IntDivBuilder().build(model, id, exps, annotations, datas);
        }
    },
    int_eq {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new org.chocosolver.parser.flatzinc.ast.constraints.IntEqBuilder().build(model, id, exps, annotations, datas);
        }
    },
    int_eq_reif {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new org.chocosolver.parser.flatzinc.ast.constraints.IntEqReifBuilder().build(model, id, exps, annotations, datas);
        }
    },
    int_le {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new org.chocosolver.parser.flatzinc.ast.constraints.IntLeBuilder().build(model, id, exps, annotations, datas);
        }
    },
    int_le_reif {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new org.chocosolver.parser.flatzinc.ast.constraints.IntLeReifBuilder().build(model, id, exps, annotations, datas);
        }
    },
    int_lin_eq {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new org.chocosolver.parser.flatzinc.ast.constraints.IntLinEqBuilder().build(model, id, exps, annotations, datas);
        }
    },
    int_lin_eq_reif {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new org.chocosolver.parser.flatzinc.ast.constraints.IntLinEqReifBuilder().build(model, id, exps, annotations, datas);
        }
    },
    int_lin_le {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new org.chocosolver.parser.flatzinc.ast.constraints.IntLinLeBuilder().build(model, id, exps, annotations, datas);
        }
    },
    int_lin_le_reif {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new org.chocosolver.parser.flatzinc.ast.constraints.IntLinLeReifBuilder().build(model, id, exps, annotations, datas);
        }
    },
    int_lin_ne {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new org.chocosolver.parser.flatzinc.ast.constraints.IntLinNeBuilder().build(model, id, exps, annotations, datas);
        }
    },
    int_lin_ne_reif {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new org.chocosolver.parser.flatzinc.ast.constraints.IntLinNeReifBuilder().build(model, id, exps, annotations, datas);
        }
    },
    int_lt {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new org.chocosolver.parser.flatzinc.ast.constraints.IntLtBuilder().build(model, id, exps, annotations, datas);
        }
    },
    int_lt_reif {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new org.chocosolver.parser.flatzinc.ast.constraints.IntLtReifBuilder().build(model, id, exps, annotations, datas);
        }
    },
    int_max {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new org.chocosolver.parser.flatzinc.ast.constraints.IntMaxBuilder().build(model, id, exps, annotations, datas);
        }
    },
    int_min {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new org.chocosolver.parser.flatzinc.ast.constraints.IntMinBuilder().build(model, id, exps, annotations, datas);
        }
    },
    int_mod {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new org.chocosolver.parser.flatzinc.ast.constraints.IntModBuilder().build(model, id, exps, annotations, datas);
        }
    },
    int_ne {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new org.chocosolver.parser.flatzinc.ast.constraints.IntNeBuilder().build(model, id, exps, annotations, datas);
        }
    },
    int_ne_reif {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new org.chocosolver.parser.flatzinc.ast.constraints.IntNeReifBuilder().build(model, id, exps, annotations, datas);
        }
    },
    int_plus {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new org.chocosolver.parser.flatzinc.ast.constraints.IntPlusBuilder().build(model, id, exps, annotations, datas);
        }
    },
    int_times {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new org.chocosolver.parser.flatzinc.ast.constraints.IntTimesBuilder().build(model, id, exps, annotations, datas);
        }
    },
    alldifferentChoco {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new org.chocosolver.parser.flatzinc.ast.constraints.global.AllDifferentBuilder().build(model, id, exps, annotations, datas);
        }
    },
    alldifferentBut0Choco {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new org.chocosolver.parser.flatzinc.ast.constraints.global.AllDifferentBut0Builder().build(model, id, exps, annotations, datas);
        }
    },
    amongChoco {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new org.chocosolver.parser.flatzinc.ast.constraints.global.AmongBuilder().build(model, id, exps, annotations, datas);
        }
    },
    atleastChoco {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new org.chocosolver.parser.flatzinc.ast.constraints.global.AtLeastBuilder().build(model, id, exps, annotations, datas);
        }
    },
    atmostChoco {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new org.chocosolver.parser.flatzinc.ast.constraints.global.AtMostBuilder().build(model, id, exps, annotations, datas);
        }
    },
    bin_packingChoco {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new org.chocosolver.parser.flatzinc.ast.constraints.global.BinPackingBuilder().build(model, id, exps, annotations, datas);
        }
    },
    bin_packing_capaChoco {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new org.chocosolver.parser.flatzinc.ast.constraints.global.BinPackingCapaBuilder().build(model, id, exps, annotations, datas);
        }
    },
    bin_packing_loadChoco {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new org.chocosolver.parser.flatzinc.ast.constraints.global.BinPackingLoadBuilder().build(model, id, exps, annotations, datas);
        }
    },
    circuitChoco {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new org.chocosolver.parser.flatzinc.ast.constraints.global.CircuitBuilder().build(model, id, exps, annotations, datas);
        }
    },
    count_eqchoco {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new org.chocosolver.parser.flatzinc.ast.constraints.global.CountEqBuilder().build(model, id, exps, annotations, datas);
        }
    },
    cumulativeChoco {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new org.chocosolver.parser.flatzinc.ast.constraints.global.CumulativeBuilder().build(model, id, exps, annotations, datas);
        }
    },
    diffnChoco {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new org.chocosolver.parser.flatzinc.ast.constraints.global.DiffNBuilder().build(model, id, exps, annotations, datas);
        }
    },
    distributeChoco {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new org.chocosolver.parser.flatzinc.ast.constraints.global.DistributeBuilder().build(model, id, exps, annotations, datas);
        }
    },
    exactlyChoco {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new org.chocosolver.parser.flatzinc.ast.constraints.global.ExactlyBuilder().build(model, id, exps, annotations, datas);
        }
    },
    geostChoco {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new org.chocosolver.parser.flatzinc.ast.constraints.global.GeostBuilder().build(model, id, exps, annotations, datas);
        }
    },
    globalCardinalityChoco {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new org.chocosolver.parser.flatzinc.ast.constraints.global.GlobalCardinalityBuilder().build(model, id, exps, annotations, datas);
        }
    },
    globalCardinalityLowUpChoco {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new org.chocosolver.parser.flatzinc.ast.constraints.global.GlobalCardinalityLowUpBuilder().build(model, id, exps, annotations, datas);
        }
    },
    inverseChoco {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new org.chocosolver.parser.flatzinc.ast.constraints.global.InverseBuilder().build(model, id, exps, annotations, datas);
        }
    },
    knapsackChoco {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new org.chocosolver.parser.flatzinc.ast.constraints.global.KnapsackBuilder().build(model, id, exps, annotations, datas);
        }
    },
    lex2Choco {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new org.chocosolver.parser.flatzinc.ast.constraints.global.Lex2Builder().build(model, id, exps, annotations, datas);
        }
    },
    lex_lessChoco {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new org.chocosolver.parser.flatzinc.ast.constraints.global.LexLessBuilder().build(model, id, exps, annotations, datas);
        }
    },
    maximumChoco {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new org.chocosolver.parser.flatzinc.ast.constraints.global.MaximumBuilder().build(model, id, exps, annotations, datas);
        }
    },
    memberChoco {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new org.chocosolver.parser.flatzinc.ast.constraints.global.MemberBuilder().build(model, id, exps, annotations, datas);
        }
    },
    memberVarChoco {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new org.chocosolver.parser.flatzinc.ast.constraints.global.MemberVarBuilder().build(model, id, exps, annotations, datas);
        }
    },
    memberReifChoco {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new org.chocosolver.parser.flatzinc.ast.constraints.global.MemberReifBuilder().build(model, id, exps, annotations, datas);
        }
    },
    memberVarReifChoco {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new org.chocosolver.parser.flatzinc.ast.constraints.global.MemberVarReifBuilder().build(model, id, exps, annotations, datas);
        }
    },
    minimumChoco {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new org.chocosolver.parser.flatzinc.ast.constraints.global.MinimumBuilder().build(model, id, exps, annotations, datas);
        }
    },
    nvalueChoco {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new org.chocosolver.parser.flatzinc.ast.constraints.global.NValueBuilder().build(model, id, exps, annotations, datas);
        }
    },
    regularChoco {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new org.chocosolver.parser.flatzinc.ast.constraints.global.RegularBuilder().build(model, id, exps, annotations, datas);
        }
    },
    sortChoco {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new org.chocosolver.parser.flatzinc.ast.constraints.global.SortBuilder().build(model, id, exps, annotations, datas);
        }
    },
    subcircuitChoco {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new org.chocosolver.parser.flatzinc.ast.constraints.global.SubcircuitBuilder().build(model, id, exps, annotations, datas);
        }
    },
    tableChoco {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new org.chocosolver.parser.flatzinc.ast.constraints.global.TableBuilder().build(model, id, exps, annotations, datas);
        }
    },
    value_precede_chain_intChoco {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new org.chocosolver.parser.flatzinc.ast.constraints.global.ValuePrecedeChainInt().build(model, id, exps, annotations, datas);
        }
    },
    count_eq_reif_choco {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new org.chocosolver.parser.flatzinc.ast.constraints.global.CountEqReifBuilder().build(model, id, exps, annotations, datas);
        }
    },
    set_card {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new org.chocosolver.parser.flatzinc.ast.constraints.SetCardBuilder().build(model, id, exps, annotations, datas);
        }
    },
    set_diff {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new org.chocosolver.parser.flatzinc.ast.constraints.SetDiffBuilder().build(model, id, exps, annotations, datas);
        }
    },
    set_eq {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new org.chocosolver.parser.flatzinc.ast.constraints.SetEqBuilder().build(model, id, exps, annotations, datas);
        }
    },
    set_eq_reif {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new org.chocosolver.parser.flatzinc.ast.constraints.SetEqReifBuilder().build(model, id, exps, annotations, datas);
        }
    },
    set_in {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new org.chocosolver.parser.flatzinc.ast.constraints.SetInBuilder().build(model, id, exps, annotations, datas);
        }
    },
    set_in_reif {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new org.chocosolver.parser.flatzinc.ast.constraints.SetInReifBuilder().build(model, id, exps, annotations, datas);
        }
    },
    set_intersect {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new org.chocosolver.parser.flatzinc.ast.constraints.SetIntersectBuilder().build(model, id, exps, annotations, datas);
        }
    },
    set_le {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new org.chocosolver.parser.flatzinc.ast.constraints.SetLeqBuilder().build(model, id, exps, annotations, datas);
        }
    },
    set_lt {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new org.chocosolver.parser.flatzinc.ast.constraints.SetLtBuilder().build(model, id, exps, annotations, datas);
        }
    },
    set_ne {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new org.chocosolver.parser.flatzinc.ast.constraints.SetNeBuilder().build(model, id, exps, annotations, datas);
        }
    },
    set_ne_reif {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new org.chocosolver.parser.flatzinc.ast.constraints.SetNeReifBuilder().build(model, id, exps, annotations, datas);
        }
    },
    set_subset {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new org.chocosolver.parser.flatzinc.ast.constraints.SetSubsetBuilder().build(model, id, exps, annotations, datas);
        }
    },
    set_subset_reif {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new org.chocosolver.parser.flatzinc.ast.constraints.SetSubsetReifBuilder().build(model, id, exps, annotations, datas);
        }
    },
    set_symdiff {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new org.chocosolver.parser.flatzinc.ast.constraints.SetSymdiffBuilder().build(model, id, exps, annotations, datas);
        }
    },
    set_union {
        @Override
        public void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations) {
            new org.chocosolver.parser.flatzinc.ast.constraints.SetUnionBuilder().build(model, id, exps, annotations, datas);
        }
    };

    public abstract void build(Model model, Datas datas, String id, List<Expression> exps, List<EAnnotation> annotations);
}
