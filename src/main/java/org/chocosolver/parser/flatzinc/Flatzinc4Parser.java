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
import org.chocosolver.solver.Solver;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class Flatzinc4Parser extends Parser {
	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		PAR=10, DD=17, RS=23, FLOAT=5, SET=6, ARRAY=8, LP=30, VAR=9, LS=22, CHAR=37, 
		DO=18, CONSTRAINT=12, INT=4, MAXIMIZE=16, COMMENT=33, INT_CONST=35, SC=27, 
		SATISFY=14, OF=7, IDENTIFIER=32, WS=34, MN=26, TRUE=2, PREDICATE=11, SOLVE=13, 
		CL=28, CM=21, EQ=24, RB=20, BOOL=1, LB=19, STRING=36, FALSE=3, MINIMIZE=15, 
		PL=25, RP=31, DC=29;
	public static final String[] tokenNames = {
		"<INVALID>", "BOOL", "'true'", "'false'", "'int'", "'float'", "'set'", 
		"'of'", "'array'", "'var'", "'par'", "'predicate'", "'constraint'", "'solve'", 
		"'satisfy'", "'minimize'", "'maximize'", "'..'", "'.'", "'{'", "'}'", 
		"','", "'['", "']'", "'='", "'+'", "'-'", "';'", "':'", "'::'", "'('", 
		"')'", "IDENTIFIER", "COMMENT", "WS", "INT_CONST", "STRING", "CHAR"
	};
	public static final int
		RULE_flatzinc_model = 0, RULE_par_type = 1, RULE_par_type_u = 2, RULE_var_type = 3, 
		RULE_var_type_u = 4, RULE_index_set = 5, RULE_expr = 6, RULE_id_expr = 7, 
		RULE_param_decl = 8, RULE_var_decl = 9, RULE_constraint = 10, RULE_solve_goal = 11, 
		RULE_resolution = 12, RULE_annotations = 13, RULE_annotation = 14, RULE_bool_const = 15, 
		RULE_pred_decl = 16, RULE_pred_param = 17, RULE_pred_param_type = 18, 
		RULE_par_pred_param_type = 19, RULE_var_pred_param_type = 20;
	public static final String[] ruleNames = {
		"flatzinc_model", "par_type", "par_type_u", "var_type", "var_type_u", 
		"index_set", "expr", "id_expr", "param_decl", "var_decl", "constraint", 
		"solve_goal", "resolution", "annotations", "annotation", "bool_const", 
		"pred_decl", "pred_param", "pred_param_type", "par_pred_param_type", "var_pred_param_type"
	};

	@Override
	public String getGrammarFileName() { return "Flatzinc4Parser.g4"; }

	@Override
	public String[] getTokenNames() { return tokenNames; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }



	public Datas datas;

	// the solver
	public Solver mSolver;


	public boolean allSolutions, freeSearch;

	public Flatzinc4Parser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}
	public static class Flatzinc_modelContext extends ParserRuleContext {
		public Solver aSolver;
		public Datas datas;
		public boolean allSolutions;
		public boolean freeSearch;
		public Pred_declContext pred_decl(int i) {
			return getRuleContext(Pred_declContext.class,i);
		}
		public Param_declContext param_decl(int i) {
			return getRuleContext(Param_declContext.class,i);
		}
		public List<ConstraintContext> constraint() {
			return getRuleContexts(ConstraintContext.class);
		}
		public List<Pred_declContext> pred_decl() {
			return getRuleContexts(Pred_declContext.class);
		}
		public List<Param_declContext> param_decl() {
			return getRuleContexts(Param_declContext.class);
		}
		public Var_declContext var_decl(int i) {
			return getRuleContext(Var_declContext.class,i);
		}
		public Solve_goalContext solve_goal() {
			return getRuleContext(Solve_goalContext.class,0);
		}
		public List<Var_declContext> var_decl() {
			return getRuleContexts(Var_declContext.class);
		}
		public ConstraintContext constraint(int i) {
			return getRuleContext(ConstraintContext.class,i);
		}
		public Flatzinc_modelContext(ParserRuleContext parent, int invokingState) { super(parent, invokingState); }
		public Flatzinc_modelContext(ParserRuleContext parent, int invokingState, Solver aSolver, Datas datas, boolean allSolutions, boolean freeSearch) {
			super(parent, invokingState);
			this.aSolver = aSolver;
			this.datas = datas;
			this.allSolutions = allSolutions;
			this.freeSearch = freeSearch;
		}
		@Override public int getRuleIndex() { return RULE_flatzinc_model; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof Flatzinc4ParserListener ) ((Flatzinc4ParserListener)listener).enterFlatzinc_model(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof Flatzinc4ParserListener ) ((Flatzinc4ParserListener)listener).exitFlatzinc_model(this);
		}
	}

	public final Flatzinc_modelContext flatzinc_model(Solver aSolver,Datas datas,boolean allSolutions,boolean freeSearch) throws RecognitionException {
		Flatzinc_modelContext _localctx = new Flatzinc_modelContext(_ctx, getState(), aSolver, datas, allSolutions, freeSearch);
		enterRule(_localctx, 0, RULE_flatzinc_model);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{

			    this.mSolver = aSolver;
			    this.datas = datas;
			    this.allSolutions = allSolutions;
			    this.freeSearch = freeSearch;
			    
			setState(46);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==PREDICATE) {
				{
				{
				setState(43); pred_decl();
				}
				}
				setState(48);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(52);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,1,_ctx);
			while ( _alt!=2 && _alt!=-1 ) {
				if ( _alt==1 ) {
					{
					{
					setState(49); param_decl();
					}
					} 
				}
				setState(54);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,1,_ctx);
			}
			setState(58);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==ARRAY || _la==VAR) {
				{
				{
				setState(55); var_decl();
				}
				}
				setState(60);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(64);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==CONSTRAINT) {
				{
				{
				setState(61); constraint();
				}
				}
				setState(66);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(67); solve_goal();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Par_typeContext extends ParserRuleContext {
		public Declaration decl;
		public Index_setContext d;
		public Par_type_uContext p;
		public TerminalNode ARRAY() { return getToken(Flatzinc4Parser.ARRAY, 0); }
		public TerminalNode CM(int i) {
			return getToken(Flatzinc4Parser.CM, i);
		}
		public TerminalNode LS() { return getToken(Flatzinc4Parser.LS, 0); }
		public TerminalNode OF() { return getToken(Flatzinc4Parser.OF, 0); }
		public Index_setContext index_set(int i) {
			return getRuleContext(Index_setContext.class,i);
		}
		public Par_type_uContext par_type_u() {
			return getRuleContext(Par_type_uContext.class,0);
		}
		public TerminalNode RS() { return getToken(Flatzinc4Parser.RS, 0); }
		public List<Index_setContext> index_set() {
			return getRuleContexts(Index_setContext.class);
		}
		public List<TerminalNode> CM() { return getTokens(Flatzinc4Parser.CM); }
		public Par_typeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_par_type; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof Flatzinc4ParserListener ) ((Flatzinc4ParserListener)listener).enterPar_type(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof Flatzinc4ParserListener ) ((Flatzinc4ParserListener)listener).exitPar_type(this);
		}
	}

	public final Par_typeContext par_type() throws RecognitionException {
		Par_typeContext _localctx = new Par_typeContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_par_type);
		int _la;
		try {
			setState(91);
			switch (_input.LA(1)) {
			case ARRAY:
				enterOuterAlt(_localctx, 1);
				{

				        List<Declaration> decls = new ArrayList();
				    
				setState(70); match(ARRAY);
				setState(71); match(LS);
				setState(72); ((Par_typeContext)_localctx).d = index_set();
				decls.add(((Par_typeContext)_localctx).d.decl);
				setState(80);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==CM) {
					{
					{
					setState(74); match(CM);
					setState(75); ((Par_typeContext)_localctx).d = index_set();
					decls.add(((Par_typeContext)_localctx).d.decl);
					}
					}
					setState(82);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(83); match(RS);
				setState(84); match(OF);
				setState(85); ((Par_typeContext)_localctx).p = par_type_u();

				    ((Par_typeContext)_localctx).decl =  new DArray(decls,((Par_typeContext)_localctx).p.decl);
				    
				}
				break;
			case BOOL:
			case INT:
			case FLOAT:
			case SET:
				enterOuterAlt(_localctx, 2);
				{
				setState(88); ((Par_typeContext)_localctx).p = par_type_u();

				    ((Par_typeContext)_localctx).decl =  ((Par_typeContext)_localctx).p.decl;
				    
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Par_type_uContext extends ParserRuleContext {
		public Declaration decl;
		public TerminalNode BOOL() { return getToken(Flatzinc4Parser.BOOL, 0); }
		public TerminalNode OF() { return getToken(Flatzinc4Parser.OF, 0); }
		public TerminalNode INT() { return getToken(Flatzinc4Parser.INT, 0); }
		public TerminalNode SET() { return getToken(Flatzinc4Parser.SET, 0); }
		public TerminalNode FLOAT() { return getToken(Flatzinc4Parser.FLOAT, 0); }
		public Par_type_uContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_par_type_u; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof Flatzinc4ParserListener ) ((Flatzinc4ParserListener)listener).enterPar_type_u(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof Flatzinc4ParserListener ) ((Flatzinc4ParserListener)listener).exitPar_type_u(this);
		}
	}

	public final Par_type_uContext par_type_u() throws RecognitionException {
		Par_type_uContext _localctx = new Par_type_uContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_par_type_u);
		try {
			setState(103);
			switch (_input.LA(1)) {
			case BOOL:
				enterOuterAlt(_localctx, 1);
				{
				setState(93); match(BOOL);

				    ((Par_type_uContext)_localctx).decl = DBool.me;
				    
				}
				break;
			case FLOAT:
				enterOuterAlt(_localctx, 2);
				{
				setState(95); match(FLOAT);

				    ((Par_type_uContext)_localctx).decl = DFloat.me;
				    
				}
				break;
			case SET:
				enterOuterAlt(_localctx, 3);
				{
				setState(97); match(SET);
				setState(98); match(OF);
				setState(99); match(INT);

				    ((Par_type_uContext)_localctx).decl = DSetOfInt.me;
				    
				}
				break;
			case INT:
				enterOuterAlt(_localctx, 4);
				{
				setState(101); match(INT);

				    ((Par_type_uContext)_localctx).decl = DInt.me;
				    
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Var_typeContext extends ParserRuleContext {
		public Declaration decl;
		public Index_setContext d;
		public Var_type_uContext vt;
		public TerminalNode ARRAY() { return getToken(Flatzinc4Parser.ARRAY, 0); }
		public TerminalNode CM(int i) {
			return getToken(Flatzinc4Parser.CM, i);
		}
		public TerminalNode VAR() { return getToken(Flatzinc4Parser.VAR, 0); }
		public TerminalNode LS() { return getToken(Flatzinc4Parser.LS, 0); }
		public TerminalNode OF() { return getToken(Flatzinc4Parser.OF, 0); }
		public Var_type_uContext var_type_u() {
			return getRuleContext(Var_type_uContext.class,0);
		}
		public Index_setContext index_set(int i) {
			return getRuleContext(Index_setContext.class,i);
		}
		public TerminalNode RS() { return getToken(Flatzinc4Parser.RS, 0); }
		public List<Index_setContext> index_set() {
			return getRuleContexts(Index_setContext.class);
		}
		public List<TerminalNode> CM() { return getTokens(Flatzinc4Parser.CM); }
		public Var_typeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_var_type; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof Flatzinc4ParserListener ) ((Flatzinc4ParserListener)listener).enterVar_type(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof Flatzinc4ParserListener ) ((Flatzinc4ParserListener)listener).exitVar_type(this);
		}
	}

	public final Var_typeContext var_type() throws RecognitionException {
		Var_typeContext _localctx = new Var_typeContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_var_type);
		int _la;
		try {
			setState(129);
			switch (_input.LA(1)) {
			case ARRAY:
				enterOuterAlt(_localctx, 1);
				{

				    List<Declaration> decls = new ArrayList();
				    
				setState(106); match(ARRAY);
				setState(107); match(LS);
				setState(108); ((Var_typeContext)_localctx).d = index_set();
				decls.add(((Var_typeContext)_localctx).d.decl);
				setState(116);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==CM) {
					{
					{
					setState(110); match(CM);
					setState(111); ((Var_typeContext)_localctx).d = index_set();
					decls.add(((Var_typeContext)_localctx).d.decl);
					}
					}
					setState(118);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(119); match(RS);
				setState(120); match(OF);
				setState(121); match(VAR);
				setState(122); ((Var_typeContext)_localctx).vt = var_type_u();

				    ((Var_typeContext)_localctx).decl =  new DArray(decls, ((Var_typeContext)_localctx).vt.decl);
				    
				}
				break;
			case VAR:
				enterOuterAlt(_localctx, 2);
				{
				setState(125); match(VAR);
				setState(126); ((Var_typeContext)_localctx).vt = var_type_u();

				    ((Var_typeContext)_localctx).decl = ((Var_typeContext)_localctx).vt.decl;
				    
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Var_type_uContext extends ParserRuleContext {
		public Declaration decl;
		public Token i1;
		public Token i2;
		public Token i;
		public TerminalNode DD() { return getToken(Flatzinc4Parser.DD, 0); }
		public TerminalNode CM(int i) {
			return getToken(Flatzinc4Parser.CM, i);
		}
		public TerminalNode BOOL() { return getToken(Flatzinc4Parser.BOOL, 0); }
		public TerminalNode INT_CONST(int i) {
			return getToken(Flatzinc4Parser.INT_CONST, i);
		}
		public TerminalNode OF() { return getToken(Flatzinc4Parser.OF, 0); }
		public TerminalNode INT() { return getToken(Flatzinc4Parser.INT, 0); }
		public List<TerminalNode> INT_CONST() { return getTokens(Flatzinc4Parser.INT_CONST); }
		public TerminalNode RB() { return getToken(Flatzinc4Parser.RB, 0); }
		public List<TerminalNode> CM() { return getTokens(Flatzinc4Parser.CM); }
		public TerminalNode LB() { return getToken(Flatzinc4Parser.LB, 0); }
		public TerminalNode SET() { return getToken(Flatzinc4Parser.SET, 0); }
		public TerminalNode FLOAT() { return getToken(Flatzinc4Parser.FLOAT, 0); }
		public Var_type_uContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_var_type_u; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof Flatzinc4ParserListener ) ((Flatzinc4ParserListener)listener).enterVar_type_u(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof Flatzinc4ParserListener ) ((Flatzinc4ParserListener)listener).exitVar_type_u(this);
		}
	}

	public final Var_type_uContext var_type_u() throws RecognitionException {
		Var_type_uContext _localctx = new Var_type_uContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_var_type_u);
		int _la;
		try {
			setState(177);
			switch ( getInterpreter().adaptivePredict(_input,11,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(131); match(BOOL);

				    ((Var_type_uContext)_localctx).decl =  DBool.me;
				    
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(133); match(FLOAT);

				    ((Var_type_uContext)_localctx).decl =  DFloat.me;
				    
				}
				break;

			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(135); match(INT);

				    ((Var_type_uContext)_localctx).decl =  DInt.me;
				    
				}
				break;

			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(137); ((Var_type_uContext)_localctx).i1 = match(INT_CONST);
				setState(138); match(DD);
				setState(139); ((Var_type_uContext)_localctx).i2 = match(INT_CONST);

				     ((Var_type_uContext)_localctx).decl =  new DInt2(EInt.make((((Var_type_uContext)_localctx).i1!=null?((Var_type_uContext)_localctx).i1.getText():null)), EInt.make((((Var_type_uContext)_localctx).i2!=null?((Var_type_uContext)_localctx).i2.getText():null)));
				     
				}
				break;

			case 5:
				enterOuterAlt(_localctx, 5);
				{

				    ArrayList<EInt> values = new ArrayList();
				    
				setState(142); match(LB);
				setState(143); ((Var_type_uContext)_localctx).i = match(INT_CONST);
				values.add(EInt.make((((Var_type_uContext)_localctx).i!=null?((Var_type_uContext)_localctx).i.getText():null)));
				setState(150);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==CM) {
					{
					{
					setState(145); match(CM);
					setState(146); ((Var_type_uContext)_localctx).i = match(INT_CONST);
					values.add(EInt.make((((Var_type_uContext)_localctx).i!=null?((Var_type_uContext)_localctx).i.getText():null)));
					}
					}
					setState(152);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(153); match(RB);

				    ((Var_type_uContext)_localctx).decl =  new DManyInt(values);
				    
				}
				break;

			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(155); match(SET);
				setState(156); match(OF);
				setState(157); ((Var_type_uContext)_localctx).i1 = match(INT_CONST);
				setState(158); match(DD);
				setState(159); ((Var_type_uContext)_localctx).i2 = match(INT_CONST);

				    ((Var_type_uContext)_localctx).decl =  new DSet(new DInt2(EInt.make((((Var_type_uContext)_localctx).i1!=null?((Var_type_uContext)_localctx).i1.getText():null)), EInt.make((((Var_type_uContext)_localctx).i2!=null?((Var_type_uContext)_localctx).i2.getText():null))));
				    
				}
				break;

			case 7:
				enterOuterAlt(_localctx, 7);
				{

				    ArrayList<EInt> values = new ArrayList();
				    
				setState(162); match(SET);
				setState(163); match(OF);
				setState(164); match(LB);
				setState(165); ((Var_type_uContext)_localctx).i = match(INT_CONST);
				values.add(EInt.make((((Var_type_uContext)_localctx).i!=null?((Var_type_uContext)_localctx).i.getText():null)));
				setState(172);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==CM) {
					{
					{
					setState(167); match(CM);
					setState(168); ((Var_type_uContext)_localctx).i = match(INT_CONST);
					values.add(EInt.make((((Var_type_uContext)_localctx).i!=null?((Var_type_uContext)_localctx).i.getText():null)));
					}
					}
					setState(174);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(175); match(RB);

				    ((Var_type_uContext)_localctx).decl =  new DSet(new DManyInt(values));
				    
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Index_setContext extends ParserRuleContext {
		public Declaration decl;
		public Token i1;
		public Token i2;
		public TerminalNode DD() { return getToken(Flatzinc4Parser.DD, 0); }
		public TerminalNode INT_CONST(int i) {
			return getToken(Flatzinc4Parser.INT_CONST, i);
		}
		public List<TerminalNode> INT_CONST() { return getTokens(Flatzinc4Parser.INT_CONST); }
		public TerminalNode INT() { return getToken(Flatzinc4Parser.INT, 0); }
		public Index_setContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_index_set; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof Flatzinc4ParserListener ) ((Flatzinc4ParserListener)listener).enterIndex_set(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof Flatzinc4ParserListener ) ((Flatzinc4ParserListener)listener).exitIndex_set(this);
		}
	}

	public final Index_setContext index_set() throws RecognitionException {
		Index_setContext _localctx = new Index_setContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_index_set);
		try {
			setState(185);
			switch (_input.LA(1)) {
			case INT_CONST:
				enterOuterAlt(_localctx, 1);
				{
				setState(179); ((Index_setContext)_localctx).i1 = match(INT_CONST);
				setState(180); match(DD);
				setState(181); ((Index_setContext)_localctx).i2 = match(INT_CONST);

				    ((Index_setContext)_localctx).decl =  new DInt2(EInt.make((((Index_setContext)_localctx).i1!=null?((Index_setContext)_localctx).i1.getText():null)), EInt.make((((Index_setContext)_localctx).i2!=null?((Index_setContext)_localctx).i2.getText():null)));
				    
				}
				break;
			case INT:
				enterOuterAlt(_localctx, 2);
				{
				setState(183); match(INT);

				    ((Index_setContext)_localctx).decl =  DInt.me;
				    
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ExprContext extends ParserRuleContext {
		public Expression exp;
		public Token i;
		public Bool_constContext b;
		public Token i1;
		public Token i2;
		public ExprContext e;
		public Id_exprContext ie;
		public Token STRING;
		public TerminalNode DD() { return getToken(Flatzinc4Parser.DD, 0); }
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public TerminalNode INT_CONST(int i) {
			return getToken(Flatzinc4Parser.INT_CONST, i);
		}
		public TerminalNode LS() { return getToken(Flatzinc4Parser.LS, 0); }
		public Id_exprContext id_expr() {
			return getRuleContext(Id_exprContext.class,0);
		}
		public TerminalNode RS() { return getToken(Flatzinc4Parser.RS, 0); }
		public TerminalNode STRING() { return getToken(Flatzinc4Parser.STRING, 0); }
		public TerminalNode LB() { return getToken(Flatzinc4Parser.LB, 0); }
		public TerminalNode CM(int i) {
			return getToken(Flatzinc4Parser.CM, i);
		}
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public Bool_constContext bool_const() {
			return getRuleContext(Bool_constContext.class,0);
		}
		public TerminalNode RB() { return getToken(Flatzinc4Parser.RB, 0); }
		public List<TerminalNode> INT_CONST() { return getTokens(Flatzinc4Parser.INT_CONST); }
		public List<TerminalNode> CM() { return getTokens(Flatzinc4Parser.CM); }
		public ExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof Flatzinc4ParserListener ) ((Flatzinc4ParserListener)listener).enterExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof Flatzinc4ParserListener ) ((Flatzinc4ParserListener)listener).exitExpr(this);
		}
	}

	public final ExprContext expr() throws RecognitionException {
		ExprContext _localctx = new ExprContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_expr);
		int _la;
		try {
			setState(235);
			switch ( getInterpreter().adaptivePredict(_input,17,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(187); match(LB);
				setState(188); match(RB);

				    ((ExprContext)_localctx).exp =  new ESetList(new ArrayList());
				    
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{

				    ArrayList<EInt> values = new ArrayList();
				    
				setState(191); match(LB);
				setState(192); ((ExprContext)_localctx).i = match(INT_CONST);
				values.add(EInt.make((((ExprContext)_localctx).i!=null?((ExprContext)_localctx).i.getText():null)));
				setState(199);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==CM) {
					{
					{
					setState(194); match(CM);
					setState(195); ((ExprContext)_localctx).i = match(INT_CONST);
					values.add(EInt.make((((ExprContext)_localctx).i!=null?((ExprContext)_localctx).i.getText():null)));
					}
					}
					setState(201);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(202); match(RB);

				    ((ExprContext)_localctx).exp =  new ESetList(values);
				    
				}
				break;

			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(204); ((ExprContext)_localctx).b = bool_const();

				    ((ExprContext)_localctx).exp = EBool.make(((ExprContext)_localctx).b.value);
				    
				}
				break;

			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(207); ((ExprContext)_localctx).i1 = match(INT_CONST);
				setState(210);
				_la = _input.LA(1);
				if (_la==DD) {
					{
					setState(208); match(DD);
					setState(209); ((ExprContext)_localctx).i2 = match(INT_CONST);
					}
				}


				    if(((ExprContext)_localctx).i2==null){
				        ((ExprContext)_localctx).exp = EInt.make((((ExprContext)_localctx).i1!=null?((ExprContext)_localctx).i1.getText():null));
				    }else{
				        ((ExprContext)_localctx).exp =  new ESetBounds(EInt.make((((ExprContext)_localctx).i1!=null?((ExprContext)_localctx).i1.getText():null)), EInt.make((((ExprContext)_localctx).i2!=null?((ExprContext)_localctx).i2.getText():null)));
				    }
				    
				}
				break;

			case 5:
				enterOuterAlt(_localctx, 5);
				{

				    ArrayList<Expression> exps = new ArrayList();
				    
				setState(214); match(LS);
				setState(226);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << TRUE) | (1L << FALSE) | (1L << LB) | (1L << LS) | (1L << IDENTIFIER) | (1L << INT_CONST) | (1L << STRING))) != 0)) {
					{
					setState(215); ((ExprContext)_localctx).e = expr();
					exps.add(((ExprContext)_localctx).e.exp);
					setState(223);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==CM) {
						{
						{
						setState(217); match(CM);
						setState(218); ((ExprContext)_localctx).e = expr();
						exps.add(((ExprContext)_localctx).e.exp);
						}
						}
						setState(225);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					}
				}

				setState(228); match(RS);

				    if(exps.size()>0){
				        ((ExprContext)_localctx).exp =  new EArray(exps);
				    }else{
				        ((ExprContext)_localctx).exp =  new EArray();
				    }
				    
				}
				break;

			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(230); ((ExprContext)_localctx).ie = id_expr();

				    ((ExprContext)_localctx).exp =  ((ExprContext)_localctx).ie.exp;
				    
				}
				break;

			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(233); ((ExprContext)_localctx).STRING = match(STRING);

				    ((ExprContext)_localctx).exp =  new EString((((ExprContext)_localctx).STRING!=null?((ExprContext)_localctx).STRING.getText():null));
				    
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Id_exprContext extends ParserRuleContext {
		public Expression exp;
		public Token IDENTIFIER;
		public ExprContext e;
		public Token i;
		public TerminalNode RP() { return getToken(Flatzinc4Parser.RP, 0); }
		public TerminalNode CM(int i) {
			return getToken(Flatzinc4Parser.CM, i);
		}
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public TerminalNode LP() { return getToken(Flatzinc4Parser.LP, 0); }
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public TerminalNode LS() { return getToken(Flatzinc4Parser.LS, 0); }
		public TerminalNode RS() { return getToken(Flatzinc4Parser.RS, 0); }
		public TerminalNode IDENTIFIER() { return getToken(Flatzinc4Parser.IDENTIFIER, 0); }
		public List<TerminalNode> CM() { return getTokens(Flatzinc4Parser.CM); }
		public TerminalNode INT_CONST() { return getToken(Flatzinc4Parser.INT_CONST, 0); }
		public Id_exprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_id_expr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof Flatzinc4ParserListener ) ((Flatzinc4ParserListener)listener).enterId_expr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof Flatzinc4ParserListener ) ((Flatzinc4ParserListener)listener).exitId_expr(this);
		}
	}

	public final Id_exprContext id_expr() throws RecognitionException {
		Id_exprContext _localctx = new Id_exprContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_id_expr);
		int _la;
		try {
			setState(261);
			switch ( getInterpreter().adaptivePredict(_input,19,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{

				    ArrayList<Expression> exps = new ArrayList();
				    
				setState(238); ((Id_exprContext)_localctx).IDENTIFIER = match(IDENTIFIER);
				setState(239); match(LP);
				setState(240); ((Id_exprContext)_localctx).e = expr();
				exps.add(((Id_exprContext)_localctx).e.exp);
				setState(248);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==CM) {
					{
					{
					setState(242); match(CM);
					setState(243); ((Id_exprContext)_localctx).e = expr();
					exps.add(((Id_exprContext)_localctx).e.exp);
					}
					}
					setState(250);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(251); match(RP);

				    ((Id_exprContext)_localctx).exp =  new EAnnotation(new EIdentifier(datas, (((Id_exprContext)_localctx).IDENTIFIER!=null?((Id_exprContext)_localctx).IDENTIFIER.getText():null)), exps);
				    
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(254); ((Id_exprContext)_localctx).IDENTIFIER = match(IDENTIFIER);
				setState(255); match(LS);
				setState(256); ((Id_exprContext)_localctx).i = match(INT_CONST);
				setState(257); match(RS);

				    ((Id_exprContext)_localctx).exp =  new EIdArray(datas, (((Id_exprContext)_localctx).IDENTIFIER!=null?((Id_exprContext)_localctx).IDENTIFIER.getText():null), Integer.parseInt((((Id_exprContext)_localctx).i!=null?((Id_exprContext)_localctx).i.getText():null)));
				    
				}
				break;

			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(259); ((Id_exprContext)_localctx).IDENTIFIER = match(IDENTIFIER);

				    ((Id_exprContext)_localctx).exp =  new EIdentifier(datas, (((Id_exprContext)_localctx).IDENTIFIER!=null?((Id_exprContext)_localctx).IDENTIFIER.getText():null));
				    
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Param_declContext extends ParserRuleContext {
		public Par_typeContext pt;
		public Token IDENTIFIER;
		public ExprContext e;
		public TerminalNode CL() { return getToken(Flatzinc4Parser.CL, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TerminalNode EQ() { return getToken(Flatzinc4Parser.EQ, 0); }
		public TerminalNode IDENTIFIER() { return getToken(Flatzinc4Parser.IDENTIFIER, 0); }
		public TerminalNode SC() { return getToken(Flatzinc4Parser.SC, 0); }
		public Par_typeContext par_type() {
			return getRuleContext(Par_typeContext.class,0);
		}
		public Param_declContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_param_decl; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof Flatzinc4ParserListener ) ((Flatzinc4ParserListener)listener).enterParam_decl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof Flatzinc4ParserListener ) ((Flatzinc4ParserListener)listener).exitParam_decl(this);
		}
	}

	public final Param_declContext param_decl() throws RecognitionException {
		Param_declContext _localctx = new Param_declContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_param_decl);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(263); ((Param_declContext)_localctx).pt = par_type();
			setState(264); match(CL);
			setState(265); ((Param_declContext)_localctx).IDENTIFIER = match(IDENTIFIER);
			setState(266); match(EQ);
			setState(267); ((Param_declContext)_localctx).e = expr();
			setState(268); match(SC);

			    // Parameter(Datas datas, Declaration type, String identifier, Expression expression)
			    FParameter.make_parameter(datas, ((Param_declContext)_localctx).pt.decl, (((Param_declContext)_localctx).IDENTIFIER!=null?((Param_declContext)_localctx).IDENTIFIER.getText():null), ((Param_declContext)_localctx).e.exp);
			    
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Var_declContext extends ParserRuleContext {
		public Var_typeContext vt;
		public Token IDENTIFIER;
		public AnnotationsContext anns;
		public Token eq;
		public ExprContext e;
		public TerminalNode CL() { return getToken(Flatzinc4Parser.CL, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public Var_typeContext var_type() {
			return getRuleContext(Var_typeContext.class,0);
		}
		public AnnotationsContext annotations() {
			return getRuleContext(AnnotationsContext.class,0);
		}
		public TerminalNode EQ() { return getToken(Flatzinc4Parser.EQ, 0); }
		public TerminalNode IDENTIFIER() { return getToken(Flatzinc4Parser.IDENTIFIER, 0); }
		public TerminalNode SC() { return getToken(Flatzinc4Parser.SC, 0); }
		public Var_declContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_var_decl; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof Flatzinc4ParserListener ) ((Flatzinc4ParserListener)listener).enterVar_decl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof Flatzinc4ParserListener ) ((Flatzinc4ParserListener)listener).exitVar_decl(this);
		}
	}

	public final Var_declContext var_decl() throws RecognitionException {
		Var_declContext _localctx = new Var_declContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_var_decl);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(271); ((Var_declContext)_localctx).vt = var_type();
			setState(272); match(CL);
			setState(273); ((Var_declContext)_localctx).IDENTIFIER = match(IDENTIFIER);
			setState(274); ((Var_declContext)_localctx).anns = annotations();
			setState(277);
			_la = _input.LA(1);
			if (_la==EQ) {
				{
				setState(275); ((Var_declContext)_localctx).eq = match(EQ);
				setState(276); ((Var_declContext)_localctx).e = expr();
				}
			}

			setState(279); match(SC);

				FVariable.make_variable(datas, ((Var_declContext)_localctx).vt.decl, (((Var_declContext)_localctx).IDENTIFIER!=null?((Var_declContext)_localctx).IDENTIFIER.getText():null), ((Var_declContext)_localctx).anns.anns, ((Var_declContext)_localctx).eq!=null?((Var_declContext)_localctx).e.exp:null, mSolver);
			    
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ConstraintContext extends ParserRuleContext {
		public Token IDENTIFIER;
		public ExprContext e;
		public AnnotationsContext anns;
		public TerminalNode RP() { return getToken(Flatzinc4Parser.RP, 0); }
		public TerminalNode CM(int i) {
			return getToken(Flatzinc4Parser.CM, i);
		}
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public TerminalNode LP() { return getToken(Flatzinc4Parser.LP, 0); }
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public TerminalNode CONSTRAINT() { return getToken(Flatzinc4Parser.CONSTRAINT, 0); }
		public AnnotationsContext annotations() {
			return getRuleContext(AnnotationsContext.class,0);
		}
		public TerminalNode IDENTIFIER() { return getToken(Flatzinc4Parser.IDENTIFIER, 0); }
		public List<TerminalNode> CM() { return getTokens(Flatzinc4Parser.CM); }
		public TerminalNode SC() { return getToken(Flatzinc4Parser.SC, 0); }
		public ConstraintContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_constraint; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof Flatzinc4ParserListener ) ((Flatzinc4ParserListener)listener).enterConstraint(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof Flatzinc4ParserListener ) ((Flatzinc4ParserListener)listener).exitConstraint(this);
		}
	}

	public final ConstraintContext constraint() throws RecognitionException {
		ConstraintContext _localctx = new ConstraintContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_constraint);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{

			    //  Solver aSolver, String id, List<Expression> exps, List<EAnnotation> annotations
			    ArrayList<Expression> exps = new ArrayList();
			    
			setState(283); match(CONSTRAINT);
			setState(284); ((ConstraintContext)_localctx).IDENTIFIER = match(IDENTIFIER);
			setState(285); match(LP);
			setState(286); ((ConstraintContext)_localctx).e = expr();
			exps.add(((ConstraintContext)_localctx).e.exp);
			setState(294);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==CM) {
				{
				{
				setState(288); match(CM);
				setState(289); ((ConstraintContext)_localctx).e = expr();
				exps.add(((ConstraintContext)_localctx).e.exp);
				}
				}
				setState(296);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(297); match(RP);
			setState(298); ((ConstraintContext)_localctx).anns = annotations();
			setState(299); match(SC);

			    FConstraint.make_constraint(mSolver, datas, (((ConstraintContext)_localctx).IDENTIFIER!=null?((ConstraintContext)_localctx).IDENTIFIER.getText():null), exps, ((ConstraintContext)_localctx).anns.anns);
			    
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Solve_goalContext extends ParserRuleContext {
		public AnnotationsContext anns;
		public ResolutionContext res;
		public AnnotationsContext annotations() {
			return getRuleContext(AnnotationsContext.class,0);
		}
		public ResolutionContext resolution() {
			return getRuleContext(ResolutionContext.class,0);
		}
		public TerminalNode SOLVE() { return getToken(Flatzinc4Parser.SOLVE, 0); }
		public TerminalNode SC() { return getToken(Flatzinc4Parser.SC, 0); }
		public Solve_goalContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_solve_goal; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof Flatzinc4ParserListener ) ((Flatzinc4ParserListener)listener).enterSolve_goal(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof Flatzinc4ParserListener ) ((Flatzinc4ParserListener)listener).exitSolve_goal(this);
		}
	}

	public final Solve_goalContext solve_goal() throws RecognitionException {
		Solve_goalContext _localctx = new Solve_goalContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_solve_goal);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(302); match(SOLVE);
			setState(303); ((Solve_goalContext)_localctx).anns = annotations();
			setState(304); ((Solve_goalContext)_localctx).res = resolution();
			setState(305); match(SC);

			    FGoal.define_goal(mSolver, freeSearch, ((Solve_goalContext)_localctx).anns.anns,((Solve_goalContext)_localctx).res.rtype,((Solve_goalContext)_localctx).res.exp);
			    
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ResolutionContext extends ParserRuleContext {
		public ResolutionPolicy rtype;
		public Expression exp;
		public ExprContext e;
		public TerminalNode MAXIMIZE() { return getToken(Flatzinc4Parser.MAXIMIZE, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TerminalNode SATISFY() { return getToken(Flatzinc4Parser.SATISFY, 0); }
		public TerminalNode MINIMIZE() { return getToken(Flatzinc4Parser.MINIMIZE, 0); }
		public ResolutionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_resolution; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof Flatzinc4ParserListener ) ((Flatzinc4ParserListener)listener).enterResolution(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof Flatzinc4ParserListener ) ((Flatzinc4ParserListener)listener).exitResolution(this);
		}
	}

	public final ResolutionContext resolution() throws RecognitionException {
		ResolutionContext _localctx = new ResolutionContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_resolution);
		try {
			setState(318);
			switch (_input.LA(1)) {
			case MINIMIZE:
				enterOuterAlt(_localctx, 1);
				{
				setState(308); match(MINIMIZE);
				setState(309); ((ResolutionContext)_localctx).e = expr();

				    ((ResolutionContext)_localctx).rtype = ResolutionPolicy.MINIMIZE;
				    ((ResolutionContext)_localctx).exp = ((ResolutionContext)_localctx).e.exp;
				    
				}
				break;
			case MAXIMIZE:
				enterOuterAlt(_localctx, 2);
				{
				setState(312); match(MAXIMIZE);
				setState(313); ((ResolutionContext)_localctx).e = expr();

				    ((ResolutionContext)_localctx).rtype = ResolutionPolicy.MAXIMIZE;
				    ((ResolutionContext)_localctx).exp = ((ResolutionContext)_localctx).e.exp;
				    
				}
				break;
			case SATISFY:
				enterOuterAlt(_localctx, 3);
				{
				setState(316); match(SATISFY);

				    ((ResolutionContext)_localctx).rtype = ResolutionPolicy.SATISFACTION;
				    ((ResolutionContext)_localctx).exp = null;
				    
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class AnnotationsContext extends ParserRuleContext {
		public List<EAnnotation> anns;
		public AnnotationContext e;
		public List<AnnotationContext> annotation() {
			return getRuleContexts(AnnotationContext.class);
		}
		public TerminalNode DC(int i) {
			return getToken(Flatzinc4Parser.DC, i);
		}
		public AnnotationContext annotation(int i) {
			return getRuleContext(AnnotationContext.class,i);
		}
		public List<TerminalNode> DC() { return getTokens(Flatzinc4Parser.DC); }
		public AnnotationsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_annotations; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof Flatzinc4ParserListener ) ((Flatzinc4ParserListener)listener).enterAnnotations(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof Flatzinc4ParserListener ) ((Flatzinc4ParserListener)listener).exitAnnotations(this);
		}
	}

	public final AnnotationsContext annotations() throws RecognitionException {
		AnnotationsContext _localctx = new AnnotationsContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_annotations);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{

			    ((AnnotationsContext)_localctx).anns =  new ArrayList();
			    
			setState(327);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==DC) {
				{
				{
				setState(321); match(DC);
				setState(322); ((AnnotationsContext)_localctx).e = annotation();
				_localctx.anns.add(((AnnotationsContext)_localctx).e.ann);
				}
				}
				setState(329);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class AnnotationContext extends ParserRuleContext {
		public EAnnotation ann;
		public Token IDENTIFIER;
		public ExprContext e;
		public TerminalNode RP() { return getToken(Flatzinc4Parser.RP, 0); }
		public TerminalNode CM(int i) {
			return getToken(Flatzinc4Parser.CM, i);
		}
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public TerminalNode LP() { return getToken(Flatzinc4Parser.LP, 0); }
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public TerminalNode IDENTIFIER() { return getToken(Flatzinc4Parser.IDENTIFIER, 0); }
		public List<TerminalNode> CM() { return getTokens(Flatzinc4Parser.CM); }
		public AnnotationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_annotation; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof Flatzinc4ParserListener ) ((Flatzinc4ParserListener)listener).enterAnnotation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof Flatzinc4ParserListener ) ((Flatzinc4ParserListener)listener).exitAnnotation(this);
		}
	}

	public final AnnotationContext annotation() throws RecognitionException {
		AnnotationContext _localctx = new AnnotationContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_annotation);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{

			    ArrayList<Expression> exps = new ArrayList();
			    
			setState(331); ((AnnotationContext)_localctx).IDENTIFIER = match(IDENTIFIER);
			setState(346);
			_la = _input.LA(1);
			if (_la==LP) {
				{
				setState(332); match(LP);
				setState(333); ((AnnotationContext)_localctx).e = expr();
				exps.add(((AnnotationContext)_localctx).e.exp);
				setState(341);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==CM) {
					{
					{
					setState(335); match(CM);
					setState(336); ((AnnotationContext)_localctx).e = expr();
					exps.add(((AnnotationContext)_localctx).e.exp);
					}
					}
					setState(343);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(344); match(RP);
				}
			}


			    ((AnnotationContext)_localctx).ann =  new EAnnotation(new EIdentifier(datas,(((AnnotationContext)_localctx).IDENTIFIER!=null?((AnnotationContext)_localctx).IDENTIFIER.getText():null)), exps);
			    
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Bool_constContext extends ParserRuleContext {
		public boolean value;
		public TerminalNode FALSE() { return getToken(Flatzinc4Parser.FALSE, 0); }
		public TerminalNode TRUE() { return getToken(Flatzinc4Parser.TRUE, 0); }
		public Bool_constContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_bool_const; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof Flatzinc4ParserListener ) ((Flatzinc4ParserListener)listener).enterBool_const(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof Flatzinc4ParserListener ) ((Flatzinc4ParserListener)listener).exitBool_const(this);
		}
	}

	public final Bool_constContext bool_const() throws RecognitionException {
		Bool_constContext _localctx = new Bool_constContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_bool_const);
		try {
			setState(354);
			switch (_input.LA(1)) {
			case TRUE:
				enterOuterAlt(_localctx, 1);
				{
				setState(350); match(TRUE);
				((Bool_constContext)_localctx).value =  true;
				}
				break;
			case FALSE:
				enterOuterAlt(_localctx, 2);
				{
				setState(352); match(FALSE);
				((Bool_constContext)_localctx).value =  false;
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Pred_declContext extends ParserRuleContext {
		public Token IDENTIFIER;
		public List<Pred_paramContext> pred_param() {
			return getRuleContexts(Pred_paramContext.class);
		}
		public TerminalNode RP() { return getToken(Flatzinc4Parser.RP, 0); }
		public TerminalNode CM(int i) {
			return getToken(Flatzinc4Parser.CM, i);
		}
		public TerminalNode LP() { return getToken(Flatzinc4Parser.LP, 0); }
		public TerminalNode PREDICATE() { return getToken(Flatzinc4Parser.PREDICATE, 0); }
		public Pred_paramContext pred_param(int i) {
			return getRuleContext(Pred_paramContext.class,i);
		}
		public TerminalNode IDENTIFIER() { return getToken(Flatzinc4Parser.IDENTIFIER, 0); }
		public List<TerminalNode> CM() { return getTokens(Flatzinc4Parser.CM); }
		public TerminalNode SC() { return getToken(Flatzinc4Parser.SC, 0); }
		public Pred_declContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_pred_decl; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof Flatzinc4ParserListener ) ((Flatzinc4ParserListener)listener).enterPred_decl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof Flatzinc4ParserListener ) ((Flatzinc4ParserListener)listener).exitPred_decl(this);
		}
	}

	public final Pred_declContext pred_decl() throws RecognitionException {
		Pred_declContext _localctx = new Pred_declContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_pred_decl);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(356); match(PREDICATE);
			setState(357); ((Pred_declContext)_localctx).IDENTIFIER = match(IDENTIFIER);
			setState(358); match(LP);
			setState(359); pred_param();
			setState(364);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==CM) {
				{
				{
				setState(360); match(CM);
				setState(361); pred_param();
				}
				}
				setState(366);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(367); match(RP);
			setState(368); match(SC);

			//        LOGGER.info("\% skip predicate : "+ (((Pred_declContext)_localctx).IDENTIFIER!=null?((Pred_declContext)_localctx).IDENTIFIER.getText():null));
				
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Pred_paramContext extends ParserRuleContext {
		public TerminalNode CL() { return getToken(Flatzinc4Parser.CL, 0); }
		public Pred_param_typeContext pred_param_type() {
			return getRuleContext(Pred_param_typeContext.class,0);
		}
		public TerminalNode IDENTIFIER() { return getToken(Flatzinc4Parser.IDENTIFIER, 0); }
		public Pred_paramContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_pred_param; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof Flatzinc4ParserListener ) ((Flatzinc4ParserListener)listener).enterPred_param(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof Flatzinc4ParserListener ) ((Flatzinc4ParserListener)listener).exitPred_param(this);
		}
	}

	public final Pred_paramContext pred_param() throws RecognitionException {
		Pred_paramContext _localctx = new Pred_paramContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_pred_param);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(371); pred_param_type();
			setState(372); match(CL);
			setState(373); match(IDENTIFIER);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Pred_param_typeContext extends ParserRuleContext {
		public Var_pred_param_typeContext var_pred_param_type() {
			return getRuleContext(Var_pred_param_typeContext.class,0);
		}
		public Par_pred_param_typeContext par_pred_param_type() {
			return getRuleContext(Par_pred_param_typeContext.class,0);
		}
		public Pred_param_typeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_pred_param_type; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof Flatzinc4ParserListener ) ((Flatzinc4ParserListener)listener).enterPred_param_type(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof Flatzinc4ParserListener ) ((Flatzinc4ParserListener)listener).exitPred_param_type(this);
		}
	}

	public final Pred_param_typeContext pred_param_type() throws RecognitionException {
		Pred_param_typeContext _localctx = new Pred_param_typeContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_pred_param_type);
		try {
			setState(377);
			switch ( getInterpreter().adaptivePredict(_input,28,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(375); par_pred_param_type();
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(376); var_pred_param_type();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Par_pred_param_typeContext extends ParserRuleContext {
		public TerminalNode DD() { return getToken(Flatzinc4Parser.DD, 0); }
		public TerminalNode ARRAY() { return getToken(Flatzinc4Parser.ARRAY, 0); }
		public TerminalNode INT_CONST(int i) {
			return getToken(Flatzinc4Parser.INT_CONST, i);
		}
		public TerminalNode LS() { return getToken(Flatzinc4Parser.LS, 0); }
		public Index_setContext index_set(int i) {
			return getRuleContext(Index_setContext.class,i);
		}
		public TerminalNode OF(int i) {
			return getToken(Flatzinc4Parser.OF, i);
		}
		public List<Index_setContext> index_set() {
			return getRuleContexts(Index_setContext.class);
		}
		public TerminalNode RS() { return getToken(Flatzinc4Parser.RS, 0); }
		public TerminalNode LB() { return getToken(Flatzinc4Parser.LB, 0); }
		public TerminalNode CM(int i) {
			return getToken(Flatzinc4Parser.CM, i);
		}
		public List<TerminalNode> OF() { return getTokens(Flatzinc4Parser.OF); }
		public List<TerminalNode> INT_CONST() { return getTokens(Flatzinc4Parser.INT_CONST); }
		public TerminalNode RB() { return getToken(Flatzinc4Parser.RB, 0); }
		public List<TerminalNode> CM() { return getTokens(Flatzinc4Parser.CM); }
		public TerminalNode SET() { return getToken(Flatzinc4Parser.SET, 0); }
		public Par_typeContext par_type() {
			return getRuleContext(Par_typeContext.class,0);
		}
		public Par_pred_param_typeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_par_pred_param_type; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof Flatzinc4ParserListener ) ((Flatzinc4ParserListener)listener).enterPar_pred_param_type(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof Flatzinc4ParserListener ) ((Flatzinc4ParserListener)listener).exitPar_pred_param_type(this);
		}
	}

	public final Par_pred_param_typeContext par_pred_param_type() throws RecognitionException {
		Par_pred_param_typeContext _localctx = new Par_pred_param_typeContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_par_pred_param_type);
		int _la;
		try {
			setState(492);
			switch ( getInterpreter().adaptivePredict(_input,37,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(379); par_type();
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(380); match(INT_CONST);
				setState(381); match(DD);
				setState(382); match(INT_CONST);
				}
				break;

			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(383); match(LB);
				setState(384); match(INT_CONST);
				setState(389);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==CM) {
					{
					{
					setState(385); match(CM);
					setState(386); match(INT_CONST);
					}
					}
					setState(391);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(392); match(RB);
				}
				break;

			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(393); match(SET);
				setState(394); match(OF);
				setState(395); match(INT_CONST);
				setState(396); match(DD);
				setState(397); match(INT_CONST);
				}
				break;

			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(398); match(SET);
				setState(399); match(OF);
				setState(400); match(LB);
				setState(401); match(INT_CONST);
				setState(406);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==CM) {
					{
					{
					setState(402); match(CM);
					setState(403); match(INT_CONST);
					}
					}
					setState(408);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(409); match(RB);
				}
				break;

			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(410); match(ARRAY);
				setState(411); match(LS);
				setState(412); index_set();
				setState(417);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==CM) {
					{
					{
					setState(413); match(CM);
					setState(414); index_set();
					}
					}
					setState(419);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(420); match(RS);
				setState(421); match(OF);
				setState(422); match(INT_CONST);
				setState(423); match(DD);
				setState(424); match(INT_CONST);
				}
				break;

			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(426); match(ARRAY);
				setState(427); match(LS);
				setState(428); index_set();
				setState(433);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==CM) {
					{
					{
					setState(429); match(CM);
					setState(430); index_set();
					}
					}
					setState(435);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(436); match(RS);
				setState(437); match(OF);
				setState(438); match(LB);
				setState(439); match(INT_CONST);
				setState(444);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==CM) {
					{
					{
					setState(440); match(CM);
					setState(441); match(INT_CONST);
					}
					}
					setState(446);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(447); match(RB);
				}
				break;

			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(449); match(ARRAY);
				setState(450); match(LS);
				setState(451); index_set();
				setState(456);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==CM) {
					{
					{
					setState(452); match(CM);
					setState(453); index_set();
					}
					}
					setState(458);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(459); match(RS);
				setState(460); match(OF);
				setState(461); match(SET);
				setState(462); match(OF);
				setState(463); match(INT_CONST);
				setState(464); match(DD);
				setState(465); match(INT_CONST);
				}
				break;

			case 9:
				enterOuterAlt(_localctx, 9);
				{
				setState(467); match(ARRAY);
				setState(468); match(LS);
				setState(469); index_set();
				setState(474);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==CM) {
					{
					{
					setState(470); match(CM);
					setState(471); index_set();
					}
					}
					setState(476);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(477); match(RS);
				setState(478); match(OF);
				setState(479); match(SET);
				setState(480); match(OF);
				setState(481); match(LB);
				setState(482); match(INT_CONST);
				setState(487);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==CM) {
					{
					{
					setState(483); match(CM);
					setState(484); match(INT_CONST);
					}
					}
					setState(489);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(490); match(RB);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Var_pred_param_typeContext extends ParserRuleContext {
		public TerminalNode ARRAY() { return getToken(Flatzinc4Parser.ARRAY, 0); }
		public TerminalNode LS() { return getToken(Flatzinc4Parser.LS, 0); }
		public Index_setContext index_set(int i) {
			return getRuleContext(Index_setContext.class,i);
		}
		public Var_typeContext var_type() {
			return getRuleContext(Var_typeContext.class,0);
		}
		public TerminalNode OF(int i) {
			return getToken(Flatzinc4Parser.OF, i);
		}
		public List<Index_setContext> index_set() {
			return getRuleContexts(Index_setContext.class);
		}
		public TerminalNode RS() { return getToken(Flatzinc4Parser.RS, 0); }
		public TerminalNode INT() { return getToken(Flatzinc4Parser.INT, 0); }
		public TerminalNode CM(int i) {
			return getToken(Flatzinc4Parser.CM, i);
		}
		public TerminalNode VAR() { return getToken(Flatzinc4Parser.VAR, 0); }
		public List<TerminalNode> OF() { return getTokens(Flatzinc4Parser.OF); }
		public List<TerminalNode> CM() { return getTokens(Flatzinc4Parser.CM); }
		public TerminalNode SET() { return getToken(Flatzinc4Parser.SET, 0); }
		public Var_pred_param_typeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_var_pred_param_type; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof Flatzinc4ParserListener ) ((Flatzinc4ParserListener)listener).enterVar_pred_param_type(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof Flatzinc4ParserListener ) ((Flatzinc4ParserListener)listener).exitVar_pred_param_type(this);
		}
	}

	public final Var_pred_param_typeContext var_pred_param_type() throws RecognitionException {
		Var_pred_param_typeContext _localctx = new Var_pred_param_typeContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_var_pred_param_type);
		int _la;
		try {
			setState(516);
			switch ( getInterpreter().adaptivePredict(_input,39,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(494); var_type();
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(495); match(VAR);
				setState(496); match(SET);
				setState(497); match(OF);
				setState(498); match(INT);
				}
				break;

			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(499); match(ARRAY);
				setState(500); match(LS);
				setState(501); index_set();
				setState(506);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==CM) {
					{
					{
					setState(502); match(CM);
					setState(503); index_set();
					}
					}
					setState(508);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(509); match(RS);
				setState(510); match(OF);
				setState(511); match(VAR);
				setState(512); match(SET);
				setState(513); match(OF);
				setState(514); match(INT);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\3\'\u0209\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t"+
		"\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\3\2\3\2\7\2/\n\2\f\2\16\2\62"+
		"\13\2\3\2\7\2\65\n\2\f\2\16\28\13\2\3\2\7\2;\n\2\f\2\16\2>\13\2\3\2\7"+
		"\2A\n\2\f\2\16\2D\13\2\3\2\3\2\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\7\3"+
		"Q\n\3\f\3\16\3T\13\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\5\3^\n\3\3\4\3\4"+
		"\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\5\4j\n\4\3\5\3\5\3\5\3\5\3\5\3\5\3\5"+
		"\3\5\3\5\7\5u\n\5\f\5\16\5x\13\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3"+
		"\5\5\5\u0084\n\5\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3"+
		"\6\3\6\3\6\3\6\7\6\u0097\n\6\f\6\16\6\u009a\13\6\3\6\3\6\3\6\3\6\3\6\3"+
		"\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\7\6\u00ad\n\6\f\6\16\6"+
		"\u00b0\13\6\3\6\3\6\5\6\u00b4\n\6\3\7\3\7\3\7\3\7\3\7\3\7\5\7\u00bc\n"+
		"\7\3\b\3\b\3\b\3\b\3\b\3\b\3\b\3\b\3\b\3\b\7\b\u00c8\n\b\f\b\16\b\u00cb"+
		"\13\b\3\b\3\b\3\b\3\b\3\b\3\b\3\b\3\b\5\b\u00d5\n\b\3\b\3\b\3\b\3\b\3"+
		"\b\3\b\3\b\3\b\3\b\7\b\u00e0\n\b\f\b\16\b\u00e3\13\b\5\b\u00e5\n\b\3\b"+
		"\3\b\3\b\3\b\3\b\3\b\3\b\5\b\u00ee\n\b\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t"+
		"\3\t\7\t\u00f9\n\t\f\t\16\t\u00fc\13\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t"+
		"\3\t\3\t\5\t\u0108\n\t\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\13\3\13\3\13"+
		"\3\13\3\13\3\13\5\13\u0118\n\13\3\13\3\13\3\13\3\f\3\f\3\f\3\f\3\f\3\f"+
		"\3\f\3\f\3\f\3\f\7\f\u0127\n\f\f\f\16\f\u012a\13\f\3\f\3\f\3\f\3\f\3\f"+
		"\3\r\3\r\3\r\3\r\3\r\3\r\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16"+
		"\3\16\5\16\u0141\n\16\3\17\3\17\3\17\3\17\3\17\7\17\u0148\n\17\f\17\16"+
		"\17\u014b\13\17\3\20\3\20\3\20\3\20\3\20\3\20\3\20\3\20\3\20\7\20\u0156"+
		"\n\20\f\20\16\20\u0159\13\20\3\20\3\20\5\20\u015d\n\20\3\20\3\20\3\21"+
		"\3\21\3\21\3\21\5\21\u0165\n\21\3\22\3\22\3\22\3\22\3\22\3\22\7\22\u016d"+
		"\n\22\f\22\16\22\u0170\13\22\3\22\3\22\3\22\3\22\3\23\3\23\3\23\3\23\3"+
		"\24\3\24\5\24\u017c\n\24\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\7\25"+
		"\u0186\n\25\f\25\16\25\u0189\13\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25"+
		"\3\25\3\25\3\25\3\25\3\25\7\25\u0197\n\25\f\25\16\25\u019a\13\25\3\25"+
		"\3\25\3\25\3\25\3\25\3\25\7\25\u01a2\n\25\f\25\16\25\u01a5\13\25\3\25"+
		"\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\7\25\u01b2\n\25\f\25"+
		"\16\25\u01b5\13\25\3\25\3\25\3\25\3\25\3\25\3\25\7\25\u01bd\n\25\f\25"+
		"\16\25\u01c0\13\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\7\25\u01c9\n\25"+
		"\f\25\16\25\u01cc\13\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3"+
		"\25\3\25\3\25\3\25\7\25\u01db\n\25\f\25\16\25\u01de\13\25\3\25\3\25\3"+
		"\25\3\25\3\25\3\25\3\25\3\25\7\25\u01e8\n\25\f\25\16\25\u01eb\13\25\3"+
		"\25\3\25\5\25\u01ef\n\25\3\26\3\26\3\26\3\26\3\26\3\26\3\26\3\26\3\26"+
		"\3\26\7\26\u01fb\n\26\f\26\16\26\u01fe\13\26\3\26\3\26\3\26\3\26\3\26"+
		"\3\26\3\26\5\26\u0207\n\26\3\26\2\2\27\2\4\6\b\n\f\16\20\22\24\26\30\32"+
		"\34\36 \"$&(*\2\2\u0231\2,\3\2\2\2\4]\3\2\2\2\6i\3\2\2\2\b\u0083\3\2\2"+
		"\2\n\u00b3\3\2\2\2\f\u00bb\3\2\2\2\16\u00ed\3\2\2\2\20\u0107\3\2\2\2\22"+
		"\u0109\3\2\2\2\24\u0111\3\2\2\2\26\u011c\3\2\2\2\30\u0130\3\2\2\2\32\u0140"+
		"\3\2\2\2\34\u0142\3\2\2\2\36\u014c\3\2\2\2 \u0164\3\2\2\2\"\u0166\3\2"+
		"\2\2$\u0175\3\2\2\2&\u017b\3\2\2\2(\u01ee\3\2\2\2*\u0206\3\2\2\2,\60\b"+
		"\2\1\2-/\5\"\22\2.-\3\2\2\2/\62\3\2\2\2\60.\3\2\2\2\60\61\3\2\2\2\61\66"+
		"\3\2\2\2\62\60\3\2\2\2\63\65\5\22\n\2\64\63\3\2\2\2\658\3\2\2\2\66\64"+
		"\3\2\2\2\66\67\3\2\2\2\67<\3\2\2\28\66\3\2\2\29;\5\24\13\2:9\3\2\2\2;"+
		">\3\2\2\2<:\3\2\2\2<=\3\2\2\2=B\3\2\2\2><\3\2\2\2?A\5\26\f\2@?\3\2\2\2"+
		"AD\3\2\2\2B@\3\2\2\2BC\3\2\2\2CE\3\2\2\2DB\3\2\2\2EF\5\30\r\2F\3\3\2\2"+
		"\2GH\b\3\1\2HI\7\n\2\2IJ\7\30\2\2JK\5\f\7\2KR\b\3\1\2LM\7\27\2\2MN\5\f"+
		"\7\2NO\b\3\1\2OQ\3\2\2\2PL\3\2\2\2QT\3\2\2\2RP\3\2\2\2RS\3\2\2\2SU\3\2"+
		"\2\2TR\3\2\2\2UV\7\31\2\2VW\7\t\2\2WX\5\6\4\2XY\b\3\1\2Y^\3\2\2\2Z[\5"+
		"\6\4\2[\\\b\3\1\2\\^\3\2\2\2]G\3\2\2\2]Z\3\2\2\2^\5\3\2\2\2_`\7\3\2\2"+
		"`j\b\4\1\2ab\7\7\2\2bj\b\4\1\2cd\7\b\2\2de\7\t\2\2ef\7\6\2\2fj\b\4\1\2"+
		"gh\7\6\2\2hj\b\4\1\2i_\3\2\2\2ia\3\2\2\2ic\3\2\2\2ig\3\2\2\2j\7\3\2\2"+
		"\2kl\b\5\1\2lm\7\n\2\2mn\7\30\2\2no\5\f\7\2ov\b\5\1\2pq\7\27\2\2qr\5\f"+
		"\7\2rs\b\5\1\2su\3\2\2\2tp\3\2\2\2ux\3\2\2\2vt\3\2\2\2vw\3\2\2\2wy\3\2"+
		"\2\2xv\3\2\2\2yz\7\31\2\2z{\7\t\2\2{|\7\13\2\2|}\5\n\6\2}~\b\5\1\2~\u0084"+
		"\3\2\2\2\177\u0080\7\13\2\2\u0080\u0081\5\n\6\2\u0081\u0082\b\5\1\2\u0082"+
		"\u0084\3\2\2\2\u0083k\3\2\2\2\u0083\177\3\2\2\2\u0084\t\3\2\2\2\u0085"+
		"\u0086\7\3\2\2\u0086\u00b4\b\6\1\2\u0087\u0088\7\7\2\2\u0088\u00b4\b\6"+
		"\1\2\u0089\u008a\7\6\2\2\u008a\u00b4\b\6\1\2\u008b\u008c\7%\2\2\u008c"+
		"\u008d\7\23\2\2\u008d\u008e\7%\2\2\u008e\u00b4\b\6\1\2\u008f\u0090\b\6"+
		"\1\2\u0090\u0091\7\25\2\2\u0091\u0092\7%\2\2\u0092\u0098\b\6\1\2\u0093"+
		"\u0094\7\27\2\2\u0094\u0095\7%\2\2\u0095\u0097\b\6\1\2\u0096\u0093\3\2"+
		"\2\2\u0097\u009a\3\2\2\2\u0098\u0096\3\2\2\2\u0098\u0099\3\2\2\2\u0099"+
		"\u009b\3\2\2\2\u009a\u0098\3\2\2\2\u009b\u009c\7\26\2\2\u009c\u00b4\b"+
		"\6\1\2\u009d\u009e\7\b\2\2\u009e\u009f\7\t\2\2\u009f\u00a0\7%\2\2\u00a0"+
		"\u00a1\7\23\2\2\u00a1\u00a2\7%\2\2\u00a2\u00b4\b\6\1\2\u00a3\u00a4\b\6"+
		"\1\2\u00a4\u00a5\7\b\2\2\u00a5\u00a6\7\t\2\2\u00a6\u00a7\7\25\2\2\u00a7"+
		"\u00a8\7%\2\2\u00a8\u00ae\b\6\1\2\u00a9\u00aa\7\27\2\2\u00aa\u00ab\7%"+
		"\2\2\u00ab\u00ad\b\6\1\2\u00ac\u00a9\3\2\2\2\u00ad\u00b0\3\2\2\2\u00ae"+
		"\u00ac\3\2\2\2\u00ae\u00af\3\2\2\2\u00af\u00b1\3\2\2\2\u00b0\u00ae\3\2"+
		"\2\2\u00b1\u00b2\7\26\2\2\u00b2\u00b4\b\6\1\2\u00b3\u0085\3\2\2\2\u00b3"+
		"\u0087\3\2\2\2\u00b3\u0089\3\2\2\2\u00b3\u008b\3\2\2\2\u00b3\u008f\3\2"+
		"\2\2\u00b3\u009d\3\2\2\2\u00b3\u00a3\3\2\2\2\u00b4\13\3\2\2\2\u00b5\u00b6"+
		"\7%\2\2\u00b6\u00b7\7\23\2\2\u00b7\u00b8\7%\2\2\u00b8\u00bc\b\7\1\2\u00b9"+
		"\u00ba\7\6\2\2\u00ba\u00bc\b\7\1\2\u00bb\u00b5\3\2\2\2\u00bb\u00b9\3\2"+
		"\2\2\u00bc\r\3\2\2\2\u00bd\u00be\7\25\2\2\u00be\u00bf\7\26\2\2\u00bf\u00ee"+
		"\b\b\1\2\u00c0\u00c1\b\b\1\2\u00c1\u00c2\7\25\2\2\u00c2\u00c3\7%\2\2\u00c3"+
		"\u00c9\b\b\1\2\u00c4\u00c5\7\27\2\2\u00c5\u00c6\7%\2\2\u00c6\u00c8\b\b"+
		"\1\2\u00c7\u00c4\3\2\2\2\u00c8\u00cb\3\2\2\2\u00c9\u00c7\3\2\2\2\u00c9"+
		"\u00ca\3\2\2\2\u00ca\u00cc\3\2\2\2\u00cb\u00c9\3\2\2\2\u00cc\u00cd\7\26"+
		"\2\2\u00cd\u00ee\b\b\1\2\u00ce\u00cf\5 \21\2\u00cf\u00d0\b\b\1\2\u00d0"+
		"\u00ee\3\2\2\2\u00d1\u00d4\7%\2\2\u00d2\u00d3\7\23\2\2\u00d3\u00d5\7%"+
		"\2\2\u00d4\u00d2\3\2\2\2\u00d4\u00d5\3\2\2\2\u00d5\u00d6\3\2\2\2\u00d6"+
		"\u00ee\b\b\1\2\u00d7\u00d8\b\b\1\2\u00d8\u00e4\7\30\2\2\u00d9\u00da\5"+
		"\16\b\2\u00da\u00e1\b\b\1\2\u00db\u00dc\7\27\2\2\u00dc\u00dd\5\16\b\2"+
		"\u00dd\u00de\b\b\1\2\u00de\u00e0\3\2\2\2\u00df\u00db\3\2\2\2\u00e0\u00e3"+
		"\3\2\2\2\u00e1\u00df\3\2\2\2\u00e1\u00e2\3\2\2\2\u00e2\u00e5\3\2\2\2\u00e3"+
		"\u00e1\3\2\2\2\u00e4\u00d9\3\2\2\2\u00e4\u00e5\3\2\2\2\u00e5\u00e6\3\2"+
		"\2\2\u00e6\u00e7\7\31\2\2\u00e7\u00ee\b\b\1\2\u00e8\u00e9\5\20\t\2\u00e9"+
		"\u00ea\b\b\1\2\u00ea\u00ee\3\2\2\2\u00eb\u00ec\7&\2\2\u00ec\u00ee\b\b"+
		"\1\2\u00ed\u00bd\3\2\2\2\u00ed\u00c0\3\2\2\2\u00ed\u00ce\3\2\2\2\u00ed"+
		"\u00d1\3\2\2\2\u00ed\u00d7\3\2\2\2\u00ed\u00e8\3\2\2\2\u00ed\u00eb\3\2"+
		"\2\2\u00ee\17\3\2\2\2\u00ef\u00f0\b\t\1\2\u00f0\u00f1\7\"\2\2\u00f1\u00f2"+
		"\7 \2\2\u00f2\u00f3\5\16\b\2\u00f3\u00fa\b\t\1\2\u00f4\u00f5\7\27\2\2"+
		"\u00f5\u00f6\5\16\b\2\u00f6\u00f7\b\t\1\2\u00f7\u00f9\3\2\2\2\u00f8\u00f4"+
		"\3\2\2\2\u00f9\u00fc\3\2\2\2\u00fa\u00f8\3\2\2\2\u00fa\u00fb\3\2\2\2\u00fb"+
		"\u00fd\3\2\2\2\u00fc\u00fa\3\2\2\2\u00fd\u00fe\7!\2\2\u00fe\u00ff\b\t"+
		"\1\2\u00ff\u0108\3\2\2\2\u0100\u0101\7\"\2\2\u0101\u0102\7\30\2\2\u0102"+
		"\u0103\7%\2\2\u0103\u0104\7\31\2\2\u0104\u0108\b\t\1\2\u0105\u0106\7\""+
		"\2\2\u0106\u0108\b\t\1\2\u0107\u00ef\3\2\2\2\u0107\u0100\3\2\2\2\u0107"+
		"\u0105\3\2\2\2\u0108\21\3\2\2\2\u0109\u010a\5\4\3\2\u010a\u010b\7\36\2"+
		"\2\u010b\u010c\7\"\2\2\u010c\u010d\7\32\2\2\u010d\u010e\5\16\b\2\u010e"+
		"\u010f\7\35\2\2\u010f\u0110\b\n\1\2\u0110\23\3\2\2\2\u0111\u0112\5\b\5"+
		"\2\u0112\u0113\7\36\2\2\u0113\u0114\7\"\2\2\u0114\u0117\5\34\17\2\u0115"+
		"\u0116\7\32\2\2\u0116\u0118\5\16\b\2\u0117\u0115\3\2\2\2\u0117\u0118\3"+
		"\2\2\2\u0118\u0119\3\2\2\2\u0119\u011a\7\35\2\2\u011a\u011b\b\13\1\2\u011b"+
		"\25\3\2\2\2\u011c\u011d\b\f\1\2\u011d\u011e\7\16\2\2\u011e\u011f\7\"\2"+
		"\2\u011f\u0120\7 \2\2\u0120\u0121\5\16\b\2\u0121\u0128\b\f\1\2\u0122\u0123"+
		"\7\27\2\2\u0123\u0124\5\16\b\2\u0124\u0125\b\f\1\2\u0125\u0127\3\2\2\2"+
		"\u0126\u0122\3\2\2\2\u0127\u012a\3\2\2\2\u0128\u0126\3\2\2\2\u0128\u0129"+
		"\3\2\2\2\u0129\u012b\3\2\2\2\u012a\u0128\3\2\2\2\u012b\u012c\7!\2\2\u012c"+
		"\u012d\5\34\17\2\u012d\u012e\7\35\2\2\u012e\u012f\b\f\1\2\u012f\27\3\2"+
		"\2\2\u0130\u0131\7\17\2\2\u0131\u0132\5\34\17\2\u0132\u0133\5\32\16\2"+
		"\u0133\u0134\7\35\2\2\u0134\u0135\b\r\1\2\u0135\31\3\2\2\2\u0136\u0137"+
		"\7\21\2\2\u0137\u0138\5\16\b\2\u0138\u0139\b\16\1\2\u0139\u0141\3\2\2"+
		"\2\u013a\u013b\7\22\2\2\u013b\u013c\5\16\b\2\u013c\u013d\b\16\1\2\u013d"+
		"\u0141\3\2\2\2\u013e\u013f\7\20\2\2\u013f\u0141\b\16\1\2\u0140\u0136\3"+
		"\2\2\2\u0140\u013a\3\2\2\2\u0140\u013e\3\2\2\2\u0141\33\3\2\2\2\u0142"+
		"\u0149\b\17\1\2\u0143\u0144\7\37\2\2\u0144\u0145\5\36\20\2\u0145\u0146"+
		"\b\17\1\2\u0146\u0148\3\2\2\2\u0147\u0143\3\2\2\2\u0148\u014b\3\2\2\2"+
		"\u0149\u0147\3\2\2\2\u0149\u014a\3\2\2\2\u014a\35\3\2\2\2\u014b\u0149"+
		"\3\2\2\2\u014c\u014d\b\20\1\2\u014d\u015c\7\"\2\2\u014e\u014f\7 \2\2\u014f"+
		"\u0150\5\16\b\2\u0150\u0157\b\20\1\2\u0151\u0152\7\27\2\2\u0152\u0153"+
		"\5\16\b\2\u0153\u0154\b\20\1\2\u0154\u0156\3\2\2\2\u0155\u0151\3\2\2\2"+
		"\u0156\u0159\3\2\2\2\u0157\u0155\3\2\2\2\u0157\u0158\3\2\2\2\u0158\u015a"+
		"\3\2\2\2\u0159\u0157\3\2\2\2\u015a\u015b\7!\2\2\u015b\u015d\3\2\2\2\u015c"+
		"\u014e\3\2\2\2\u015c\u015d\3\2\2\2\u015d\u015e\3\2\2\2\u015e\u015f\b\20"+
		"\1\2\u015f\37\3\2\2\2\u0160\u0161\7\4\2\2\u0161\u0165\b\21\1\2\u0162\u0163"+
		"\7\5\2\2\u0163\u0165\b\21\1\2\u0164\u0160\3\2\2\2\u0164\u0162\3\2\2\2"+
		"\u0165!\3\2\2\2\u0166\u0167\7\r\2\2\u0167\u0168\7\"\2\2\u0168\u0169\7"+
		" \2\2\u0169\u016e\5$\23\2\u016a\u016b\7\27\2\2\u016b\u016d\5$\23\2\u016c"+
		"\u016a\3\2\2\2\u016d\u0170\3\2\2\2\u016e\u016c\3\2\2\2\u016e\u016f\3\2"+
		"\2\2\u016f\u0171\3\2\2\2\u0170\u016e\3\2\2\2\u0171\u0172\7!\2\2\u0172"+
		"\u0173\7\35\2\2\u0173\u0174\b\22\1\2\u0174#\3\2\2\2\u0175\u0176\5&\24"+
		"\2\u0176\u0177\7\36\2\2\u0177\u0178\7\"\2\2\u0178%\3\2\2\2\u0179\u017c"+
		"\5(\25\2\u017a\u017c\5*\26\2\u017b\u0179\3\2\2\2\u017b\u017a\3\2\2\2\u017c"+
		"\'\3\2\2\2\u017d\u01ef\5\4\3\2\u017e\u017f\7%\2\2\u017f\u0180\7\23\2\2"+
		"\u0180\u01ef\7%\2\2\u0181\u0182\7\25\2\2\u0182\u0187\7%\2\2\u0183\u0184"+
		"\7\27\2\2\u0184\u0186\7%\2\2\u0185\u0183\3\2\2\2\u0186\u0189\3\2\2\2\u0187"+
		"\u0185\3\2\2\2\u0187\u0188\3\2\2\2\u0188\u018a\3\2\2\2\u0189\u0187\3\2"+
		"\2\2\u018a\u01ef\7\26\2\2\u018b\u018c\7\b\2\2\u018c\u018d\7\t\2\2\u018d"+
		"\u018e\7%\2\2\u018e\u018f\7\23\2\2\u018f\u01ef\7%\2\2\u0190\u0191\7\b"+
		"\2\2\u0191\u0192\7\t\2\2\u0192\u0193\7\25\2\2\u0193\u0198\7%\2\2\u0194"+
		"\u0195\7\27\2\2\u0195\u0197\7%\2\2\u0196\u0194\3\2\2\2\u0197\u019a\3\2"+
		"\2\2\u0198\u0196\3\2\2\2\u0198\u0199\3\2\2\2\u0199\u019b\3\2\2\2\u019a"+
		"\u0198\3\2\2\2\u019b\u01ef\7\26\2\2\u019c\u019d\7\n\2\2\u019d\u019e\7"+
		"\30\2\2\u019e\u01a3\5\f\7\2\u019f\u01a0\7\27\2\2\u01a0\u01a2\5\f\7\2\u01a1"+
		"\u019f\3\2\2\2\u01a2\u01a5\3\2\2\2\u01a3\u01a1\3\2\2\2\u01a3\u01a4\3\2"+
		"\2\2\u01a4\u01a6\3\2\2\2\u01a5\u01a3\3\2\2\2\u01a6\u01a7\7\31\2\2\u01a7"+
		"\u01a8\7\t\2\2\u01a8\u01a9\7%\2\2\u01a9\u01aa\7\23\2\2\u01aa\u01ab\7%"+
		"\2\2\u01ab\u01ef\3\2\2\2\u01ac\u01ad\7\n\2\2\u01ad\u01ae\7\30\2\2\u01ae"+
		"\u01b3\5\f\7\2\u01af\u01b0\7\27\2\2\u01b0\u01b2\5\f\7\2\u01b1\u01af\3"+
		"\2\2\2\u01b2\u01b5\3\2\2\2\u01b3\u01b1\3\2\2\2\u01b3\u01b4\3\2\2\2\u01b4"+
		"\u01b6\3\2\2\2\u01b5\u01b3\3\2\2\2\u01b6\u01b7\7\31\2\2\u01b7\u01b8\7"+
		"\t\2\2\u01b8\u01b9\7\25\2\2\u01b9\u01be\7%\2\2\u01ba\u01bb\7\27\2\2\u01bb"+
		"\u01bd\7%\2\2\u01bc\u01ba\3\2\2\2\u01bd\u01c0\3\2\2\2\u01be\u01bc\3\2"+
		"\2\2\u01be\u01bf\3\2\2\2\u01bf\u01c1\3\2\2\2\u01c0\u01be\3\2\2\2\u01c1"+
		"\u01c2\7\26\2\2\u01c2\u01ef\3\2\2\2\u01c3\u01c4\7\n\2\2\u01c4\u01c5\7"+
		"\30\2\2\u01c5\u01ca\5\f\7\2\u01c6\u01c7\7\27\2\2\u01c7\u01c9\5\f\7\2\u01c8"+
		"\u01c6\3\2\2\2\u01c9\u01cc\3\2\2\2\u01ca\u01c8\3\2\2\2\u01ca\u01cb\3\2"+
		"\2\2\u01cb\u01cd\3\2\2\2\u01cc\u01ca\3\2\2\2\u01cd\u01ce\7\31\2\2\u01ce"+
		"\u01cf\7\t\2\2\u01cf\u01d0\7\b\2\2\u01d0\u01d1\7\t\2\2\u01d1\u01d2\7%"+
		"\2\2\u01d2\u01d3\7\23\2\2\u01d3\u01d4\7%\2\2\u01d4\u01ef\3\2\2\2\u01d5"+
		"\u01d6\7\n\2\2\u01d6\u01d7\7\30\2\2\u01d7\u01dc\5\f\7\2\u01d8\u01d9\7"+
		"\27\2\2\u01d9\u01db\5\f\7\2\u01da\u01d8\3\2\2\2\u01db\u01de\3\2\2\2\u01dc"+
		"\u01da\3\2\2\2\u01dc\u01dd\3\2\2\2\u01dd\u01df\3\2\2\2\u01de\u01dc\3\2"+
		"\2\2\u01df\u01e0\7\31\2\2\u01e0\u01e1\7\t\2\2\u01e1\u01e2\7\b\2\2\u01e2"+
		"\u01e3\7\t\2\2\u01e3\u01e4\7\25\2\2\u01e4\u01e9\7%\2\2\u01e5\u01e6\7\27"+
		"\2\2\u01e6\u01e8\7%\2\2\u01e7\u01e5\3\2\2\2\u01e8\u01eb\3\2\2\2\u01e9"+
		"\u01e7\3\2\2\2\u01e9\u01ea\3\2\2\2\u01ea\u01ec\3\2\2\2\u01eb\u01e9\3\2"+
		"\2\2\u01ec\u01ed\7\26\2\2\u01ed\u01ef\3\2\2\2\u01ee\u017d\3\2\2\2\u01ee"+
		"\u017e\3\2\2\2\u01ee\u0181\3\2\2\2\u01ee\u018b\3\2\2\2\u01ee\u0190\3\2"+
		"\2\2\u01ee\u019c\3\2\2\2\u01ee\u01ac\3\2\2\2\u01ee\u01c3\3\2\2\2\u01ee"+
		"\u01d5\3\2\2\2\u01ef)\3\2\2\2\u01f0\u0207\5\b\5\2\u01f1\u01f2\7\13\2\2"+
		"\u01f2\u01f3\7\b\2\2\u01f3\u01f4\7\t\2\2\u01f4\u0207\7\6\2\2\u01f5\u01f6"+
		"\7\n\2\2\u01f6\u01f7\7\30\2\2\u01f7\u01fc\5\f\7\2\u01f8\u01f9\7\27\2\2"+
		"\u01f9\u01fb\5\f\7\2\u01fa\u01f8\3\2\2\2\u01fb\u01fe\3\2\2\2\u01fc\u01fa"+
		"\3\2\2\2\u01fc\u01fd\3\2\2\2\u01fd\u01ff\3\2\2\2\u01fe\u01fc\3\2\2\2\u01ff"+
		"\u0200\7\31\2\2\u0200\u0201\7\t\2\2\u0201\u0202\7\13\2\2\u0202\u0203\7"+
		"\b\2\2\u0203\u0204\7\t\2\2\u0204\u0205\7\6\2\2\u0205\u0207\3\2\2\2\u0206"+
		"\u01f0\3\2\2\2\u0206\u01f1\3\2\2\2\u0206\u01f5\3\2\2\2\u0207+\3\2\2\2"+
		"*\60\66<BR]iv\u0083\u0098\u00ae\u00b3\u00bb\u00c9\u00d4\u00e1\u00e4\u00ed"+
		"\u00fa\u0107\u0117\u0128\u0140\u0149\u0157\u015c\u0164\u016e\u017b\u0187"+
		"\u0198\u01a3\u01b3\u01be\u01ca\u01dc\u01e9\u01ee\u01fc\u0206";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}