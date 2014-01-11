// Generated from parser/flatzinc/Flatzinc4Parser.g4 by ANTLR 4.0
package parser.flatzinc;

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

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;

public class Flatzinc4ParserBaseListener implements Flatzinc4ParserListener {
	@Override public void enterId_expr(Flatzinc4Parser.Id_exprContext ctx) { }
	@Override public void exitId_expr(Flatzinc4Parser.Id_exprContext ctx) { }

	@Override public void enterPred_param(Flatzinc4Parser.Pred_paramContext ctx) { }
	@Override public void exitPred_param(Flatzinc4Parser.Pred_paramContext ctx) { }

	@Override public void enterSolve_goal(Flatzinc4Parser.Solve_goalContext ctx) { }
	@Override public void exitSolve_goal(Flatzinc4Parser.Solve_goalContext ctx) { }

	@Override public void enterPred_param_type(Flatzinc4Parser.Pred_param_typeContext ctx) { }
	@Override public void exitPred_param_type(Flatzinc4Parser.Pred_param_typeContext ctx) { }

	@Override public void enterConstraint(Flatzinc4Parser.ConstraintContext ctx) { }
	@Override public void exitConstraint(Flatzinc4Parser.ConstraintContext ctx) { }

	@Override public void enterBool_const(Flatzinc4Parser.Bool_constContext ctx) { }
	@Override public void exitBool_const(Flatzinc4Parser.Bool_constContext ctx) { }

	@Override public void enterExpr(Flatzinc4Parser.ExprContext ctx) { }
	@Override public void exitExpr(Flatzinc4Parser.ExprContext ctx) { }

	@Override public void enterResolution(Flatzinc4Parser.ResolutionContext ctx) { }
	@Override public void exitResolution(Flatzinc4Parser.ResolutionContext ctx) { }

	@Override public void enterVar_type_u(Flatzinc4Parser.Var_type_uContext ctx) { }
	@Override public void exitVar_type_u(Flatzinc4Parser.Var_type_uContext ctx) { }

	@Override public void enterVar_decl(Flatzinc4Parser.Var_declContext ctx) { }
	@Override public void exitVar_decl(Flatzinc4Parser.Var_declContext ctx) { }

	@Override public void enterPar_type_u(Flatzinc4Parser.Par_type_uContext ctx) { }
	@Override public void exitPar_type_u(Flatzinc4Parser.Par_type_uContext ctx) { }

	@Override public void enterPar_pred_param_type(Flatzinc4Parser.Par_pred_param_typeContext ctx) { }
	@Override public void exitPar_pred_param_type(Flatzinc4Parser.Par_pred_param_typeContext ctx) { }

	@Override public void enterAnnotation(Flatzinc4Parser.AnnotationContext ctx) { }
	@Override public void exitAnnotation(Flatzinc4Parser.AnnotationContext ctx) { }

	@Override public void enterVar_pred_param_type(Flatzinc4Parser.Var_pred_param_typeContext ctx) { }
	@Override public void exitVar_pred_param_type(Flatzinc4Parser.Var_pred_param_typeContext ctx) { }

	@Override public void enterPar_type(Flatzinc4Parser.Par_typeContext ctx) { }
	@Override public void exitPar_type(Flatzinc4Parser.Par_typeContext ctx) { }

	@Override public void enterIndex_set(Flatzinc4Parser.Index_setContext ctx) { }
	@Override public void exitIndex_set(Flatzinc4Parser.Index_setContext ctx) { }

	@Override public void enterVar_type(Flatzinc4Parser.Var_typeContext ctx) { }
	@Override public void exitVar_type(Flatzinc4Parser.Var_typeContext ctx) { }

	@Override public void enterAnnotations(Flatzinc4Parser.AnnotationsContext ctx) { }
	@Override public void exitAnnotations(Flatzinc4Parser.AnnotationsContext ctx) { }

	@Override public void enterParam_decl(Flatzinc4Parser.Param_declContext ctx) { }
	@Override public void exitParam_decl(Flatzinc4Parser.Param_declContext ctx) { }

	@Override public void enterFlatzinc_model(Flatzinc4Parser.Flatzinc_modelContext ctx) { }
	@Override public void exitFlatzinc_model(Flatzinc4Parser.Flatzinc_modelContext ctx) { }

	@Override public void enterPred_decl(Flatzinc4Parser.Pred_declContext ctx) { }
	@Override public void exitPred_decl(Flatzinc4Parser.Pred_declContext ctx) { }

	@Override public void enterEveryRule(ParserRuleContext ctx) { }
	@Override public void exitEveryRule(ParserRuleContext ctx) { }
	@Override public void visitTerminal(TerminalNode node) { }
	@Override public void visitErrorNode(ErrorNode node) { }
}