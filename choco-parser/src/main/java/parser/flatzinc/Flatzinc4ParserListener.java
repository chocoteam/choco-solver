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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.trove.list.array.TIntArrayList;

import parser.flatzinc.ast.declaration.*;
import parser.flatzinc.ast.expression.*;
import parser.flatzinc.FZNException;
import parser.flatzinc.FZNLayout;
import parser.flatzinc.ast.FConstraint;
import parser.flatzinc.ast.FGoal;
import parser.flatzinc.ast.FParameter;
import parser.flatzinc.ast.FVariable;
import parser.flatzinc.ast.Datas;
import parser.flatzinc.ast.FGoal;

import solver.Solver;
import solver.constraints.Constraint;
import solver.ResolutionPolicy;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.antlr.v4.runtime.tree.*;
import org.antlr.v4.runtime.Token;

public interface Flatzinc4ParserListener extends ParseTreeListener {
	void enterId_expr(Flatzinc4Parser.Id_exprContext ctx);
	void exitId_expr(Flatzinc4Parser.Id_exprContext ctx);

	void enterPred_param(Flatzinc4Parser.Pred_paramContext ctx);
	void exitPred_param(Flatzinc4Parser.Pred_paramContext ctx);

	void enterSolve_goal(Flatzinc4Parser.Solve_goalContext ctx);
	void exitSolve_goal(Flatzinc4Parser.Solve_goalContext ctx);

	void enterPred_param_type(Flatzinc4Parser.Pred_param_typeContext ctx);
	void exitPred_param_type(Flatzinc4Parser.Pred_param_typeContext ctx);

	void enterConstraint(Flatzinc4Parser.ConstraintContext ctx);
	void exitConstraint(Flatzinc4Parser.ConstraintContext ctx);

	void enterBool_const(Flatzinc4Parser.Bool_constContext ctx);
	void exitBool_const(Flatzinc4Parser.Bool_constContext ctx);

	void enterExpr(Flatzinc4Parser.ExprContext ctx);
	void exitExpr(Flatzinc4Parser.ExprContext ctx);

	void enterResolution(Flatzinc4Parser.ResolutionContext ctx);
	void exitResolution(Flatzinc4Parser.ResolutionContext ctx);

	void enterVar_type_u(Flatzinc4Parser.Var_type_uContext ctx);
	void exitVar_type_u(Flatzinc4Parser.Var_type_uContext ctx);

	void enterVar_decl(Flatzinc4Parser.Var_declContext ctx);
	void exitVar_decl(Flatzinc4Parser.Var_declContext ctx);

	void enterPar_type_u(Flatzinc4Parser.Par_type_uContext ctx);
	void exitPar_type_u(Flatzinc4Parser.Par_type_uContext ctx);

	void enterPar_pred_param_type(Flatzinc4Parser.Par_pred_param_typeContext ctx);
	void exitPar_pred_param_type(Flatzinc4Parser.Par_pred_param_typeContext ctx);

	void enterAnnotation(Flatzinc4Parser.AnnotationContext ctx);
	void exitAnnotation(Flatzinc4Parser.AnnotationContext ctx);

	void enterVar_pred_param_type(Flatzinc4Parser.Var_pred_param_typeContext ctx);
	void exitVar_pred_param_type(Flatzinc4Parser.Var_pred_param_typeContext ctx);

	void enterPar_type(Flatzinc4Parser.Par_typeContext ctx);
	void exitPar_type(Flatzinc4Parser.Par_typeContext ctx);

	void enterIndex_set(Flatzinc4Parser.Index_setContext ctx);
	void exitIndex_set(Flatzinc4Parser.Index_setContext ctx);

	void enterVar_type(Flatzinc4Parser.Var_typeContext ctx);
	void exitVar_type(Flatzinc4Parser.Var_typeContext ctx);

	void enterAnnotations(Flatzinc4Parser.AnnotationsContext ctx);
	void exitAnnotations(Flatzinc4Parser.AnnotationsContext ctx);

	void enterParam_decl(Flatzinc4Parser.Param_declContext ctx);
	void exitParam_decl(Flatzinc4Parser.Param_declContext ctx);

	void enterFlatzinc_model(Flatzinc4Parser.Flatzinc_modelContext ctx);
	void exitFlatzinc_model(Flatzinc4Parser.Flatzinc_modelContext ctx);

	void enterPred_decl(Flatzinc4Parser.Pred_declContext ctx);
	void exitPred_decl(Flatzinc4Parser.Pred_declContext ctx);
}