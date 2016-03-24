// Generated from org/chocosolver/parser/flatzinc/Flatzinc4Parser.g4 by ANTLR 4.2
package org.chocosolver.parser.flatzinc;

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

import org.chocosolver.parser.flatzinc.ast.*;
import org.chocosolver.parser.flatzinc.ast.declaration.*;
import org.chocosolver.parser.flatzinc.ast.expression.*;
import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.Model;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link Flatzinc4Parser}.
 */
public interface Flatzinc4ParserListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link Flatzinc4Parser#annotation}.
	 * @param ctx the parse tree
	 */
	void enterAnnotation(@NotNull Flatzinc4Parser.AnnotationContext ctx);
	/**
	 * Exit a parse tree produced by {@link Flatzinc4Parser#annotation}.
	 * @param ctx the parse tree
	 */
	void exitAnnotation(@NotNull Flatzinc4Parser.AnnotationContext ctx);

	/**
	 * Enter a parse tree produced by {@link Flatzinc4Parser#par_type}.
	 * @param ctx the parse tree
	 */
	void enterPar_type(@NotNull Flatzinc4Parser.Par_typeContext ctx);
	/**
	 * Exit a parse tree produced by {@link Flatzinc4Parser#par_type}.
	 * @param ctx the parse tree
	 */
	void exitPar_type(@NotNull Flatzinc4Parser.Par_typeContext ctx);

	/**
	 * Enter a parse tree produced by {@link Flatzinc4Parser#var_pred_param_type}.
	 * @param ctx the parse tree
	 */
	void enterVar_pred_param_type(@NotNull Flatzinc4Parser.Var_pred_param_typeContext ctx);
	/**
	 * Exit a parse tree produced by {@link Flatzinc4Parser#var_pred_param_type}.
	 * @param ctx the parse tree
	 */
	void exitVar_pred_param_type(@NotNull Flatzinc4Parser.Var_pred_param_typeContext ctx);

	/**
	 * Enter a parse tree produced by {@link Flatzinc4Parser#id_expr}.
	 * @param ctx the parse tree
	 */
	void enterId_expr(@NotNull Flatzinc4Parser.Id_exprContext ctx);
	/**
	 * Exit a parse tree produced by {@link Flatzinc4Parser#id_expr}.
	 * @param ctx the parse tree
	 */
	void exitId_expr(@NotNull Flatzinc4Parser.Id_exprContext ctx);

	/**
	 * Enter a parse tree produced by {@link Flatzinc4Parser#var_type_u}.
	 * @param ctx the parse tree
	 */
	void enterVar_type_u(@NotNull Flatzinc4Parser.Var_type_uContext ctx);
	/**
	 * Exit a parse tree produced by {@link Flatzinc4Parser#var_type_u}.
	 * @param ctx the parse tree
	 */
	void exitVar_type_u(@NotNull Flatzinc4Parser.Var_type_uContext ctx);

	/**
	 * Enter a parse tree produced by {@link Flatzinc4Parser#flatzinc_model}.
	 * @param ctx the parse tree
	 */
	void enterFlatzinc_model(@NotNull Flatzinc4Parser.Flatzinc_modelContext ctx);
	/**
	 * Exit a parse tree produced by {@link Flatzinc4Parser#flatzinc_model}.
	 * @param ctx the parse tree
	 */
	void exitFlatzinc_model(@NotNull Flatzinc4Parser.Flatzinc_modelContext ctx);

	/**
	 * Enter a parse tree produced by {@link Flatzinc4Parser#param_decl}.
	 * @param ctx the parse tree
	 */
	void enterParam_decl(@NotNull Flatzinc4Parser.Param_declContext ctx);
	/**
	 * Exit a parse tree produced by {@link Flatzinc4Parser#param_decl}.
	 * @param ctx the parse tree
	 */
	void exitParam_decl(@NotNull Flatzinc4Parser.Param_declContext ctx);

	/**
	 * Enter a parse tree produced by {@link Flatzinc4Parser#annotations}.
	 * @param ctx the parse tree
	 */
	void enterAnnotations(@NotNull Flatzinc4Parser.AnnotationsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Flatzinc4Parser#annotations}.
	 * @param ctx the parse tree
	 */
	void exitAnnotations(@NotNull Flatzinc4Parser.AnnotationsContext ctx);

	/**
	 * Enter a parse tree produced by {@link Flatzinc4Parser#resolution}.
	 * @param ctx the parse tree
	 */
	void enterResolution(@NotNull Flatzinc4Parser.ResolutionContext ctx);
	/**
	 * Exit a parse tree produced by {@link Flatzinc4Parser#resolution}.
	 * @param ctx the parse tree
	 */
	void exitResolution(@NotNull Flatzinc4Parser.ResolutionContext ctx);

	/**
	 * Enter a parse tree produced by {@link Flatzinc4Parser#pred_param_type}.
	 * @param ctx the parse tree
	 */
	void enterPred_param_type(@NotNull Flatzinc4Parser.Pred_param_typeContext ctx);
	/**
	 * Exit a parse tree produced by {@link Flatzinc4Parser#pred_param_type}.
	 * @param ctx the parse tree
	 */
	void exitPred_param_type(@NotNull Flatzinc4Parser.Pred_param_typeContext ctx);

	/**
	 * Enter a parse tree produced by {@link Flatzinc4Parser#var_type}.
	 * @param ctx the parse tree
	 */
	void enterVar_type(@NotNull Flatzinc4Parser.Var_typeContext ctx);
	/**
	 * Exit a parse tree produced by {@link Flatzinc4Parser#var_type}.
	 * @param ctx the parse tree
	 */
	void exitVar_type(@NotNull Flatzinc4Parser.Var_typeContext ctx);

	/**
	 * Enter a parse tree produced by {@link Flatzinc4Parser#pred_decl}.
	 * @param ctx the parse tree
	 */
	void enterPred_decl(@NotNull Flatzinc4Parser.Pred_declContext ctx);
	/**
	 * Exit a parse tree produced by {@link Flatzinc4Parser#pred_decl}.
	 * @param ctx the parse tree
	 */
	void exitPred_decl(@NotNull Flatzinc4Parser.Pred_declContext ctx);

	/**
	 * Enter a parse tree produced by {@link Flatzinc4Parser#index_set}.
	 * @param ctx the parse tree
	 */
	void enterIndex_set(@NotNull Flatzinc4Parser.Index_setContext ctx);
	/**
	 * Exit a parse tree produced by {@link Flatzinc4Parser#index_set}.
	 * @param ctx the parse tree
	 */
	void exitIndex_set(@NotNull Flatzinc4Parser.Index_setContext ctx);

	/**
	 * Enter a parse tree produced by {@link Flatzinc4Parser#expr}.
	 * @param ctx the parse tree
	 */
	void enterExpr(@NotNull Flatzinc4Parser.ExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link Flatzinc4Parser#expr}.
	 * @param ctx the parse tree
	 */
	void exitExpr(@NotNull Flatzinc4Parser.ExprContext ctx);

	/**
	 * Enter a parse tree produced by {@link Flatzinc4Parser#constraint}.
	 * @param ctx the parse tree
	 */
	void enterConstraint(@NotNull Flatzinc4Parser.ConstraintContext ctx);
	/**
	 * Exit a parse tree produced by {@link Flatzinc4Parser#constraint}.
	 * @param ctx the parse tree
	 */
	void exitConstraint(@NotNull Flatzinc4Parser.ConstraintContext ctx);

	/**
	 * Enter a parse tree produced by {@link Flatzinc4Parser#pred_param}.
	 * @param ctx the parse tree
	 */
	void enterPred_param(@NotNull Flatzinc4Parser.Pred_paramContext ctx);
	/**
	 * Exit a parse tree produced by {@link Flatzinc4Parser#pred_param}.
	 * @param ctx the parse tree
	 */
	void exitPred_param(@NotNull Flatzinc4Parser.Pred_paramContext ctx);

	/**
	 * Enter a parse tree produced by {@link Flatzinc4Parser#par_type_u}.
	 * @param ctx the parse tree
	 */
	void enterPar_type_u(@NotNull Flatzinc4Parser.Par_type_uContext ctx);
	/**
	 * Exit a parse tree produced by {@link Flatzinc4Parser#par_type_u}.
	 * @param ctx the parse tree
	 */
	void exitPar_type_u(@NotNull Flatzinc4Parser.Par_type_uContext ctx);

	/**
	 * Enter a parse tree produced by {@link Flatzinc4Parser#var_decl}.
	 * @param ctx the parse tree
	 */
	void enterVar_decl(@NotNull Flatzinc4Parser.Var_declContext ctx);
	/**
	 * Exit a parse tree produced by {@link Flatzinc4Parser#var_decl}.
	 * @param ctx the parse tree
	 */
	void exitVar_decl(@NotNull Flatzinc4Parser.Var_declContext ctx);

	/**
	 * Enter a parse tree produced by {@link Flatzinc4Parser#par_pred_param_type}.
	 * @param ctx the parse tree
	 */
	void enterPar_pred_param_type(@NotNull Flatzinc4Parser.Par_pred_param_typeContext ctx);
	/**
	 * Exit a parse tree produced by {@link Flatzinc4Parser#par_pred_param_type}.
	 * @param ctx the parse tree
	 */
	void exitPar_pred_param_type(@NotNull Flatzinc4Parser.Par_pred_param_typeContext ctx);

	/**
	 * Enter a parse tree produced by {@link Flatzinc4Parser#bool_const}.
	 * @param ctx the parse tree
	 */
	void enterBool_const(@NotNull Flatzinc4Parser.Bool_constContext ctx);
	/**
	 * Exit a parse tree produced by {@link Flatzinc4Parser#bool_const}.
	 * @param ctx the parse tree
	 */
	void exitBool_const(@NotNull Flatzinc4Parser.Bool_constContext ctx);

	/**
	 * Enter a parse tree produced by {@link Flatzinc4Parser#solve_goal}.
	 * @param ctx the parse tree
	 */
	void enterSolve_goal(@NotNull Flatzinc4Parser.Solve_goalContext ctx);
	/**
	 * Exit a parse tree produced by {@link Flatzinc4Parser#solve_goal}.
	 * @param ctx the parse tree
	 */
	void exitSolve_goal(@NotNull Flatzinc4Parser.Solve_goalContext ctx);
}