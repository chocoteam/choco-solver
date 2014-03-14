// Generated from parser/flatzinc/Flatzinc4Parser.g4 by ANTLR 4.2
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

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ATNDeserializer;
import org.antlr.v4.runtime.atn.ParserATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import parser.flatzinc.ast.*;
import parser.flatzinc.ast.declaration.*;
import parser.flatzinc.ast.expression.*;
import solver.ResolutionPolicy;
import solver.Solver;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class Flatzinc4Parser extends Parser {
	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		RB=20, RP=31, LS=22, LP=30, BOOL=1, CHAR=37, INT_CONST=35, DO=18, SET=6, 
		FLOAT=5, INT=4, MINIMIZE=15, OF=7, DD=17, TRUE=2, PREDICATE=11, SC=27, 
		WS=34, DC=29, MN=26, IDENTIFIER=32, MAXIMIZE=16, SATISFY=14, PL=25, RS=23, 
		PAR=10, LB=19, VAR=9, EQ=24, COMMENT=33, FALSE=3, CONSTRAINT=12, SOLVE=13, 
		ARRAY=8, CM=21, STRING=36, CL=28;
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


	// The flatzinc logger -- 'System.out/err' is fobidden!
	protected static final Logger LOGGER = LoggerFactory.getLogger("fzn");

	public Datas datas;

	// the solver
	public Solver mSolver;

	public Flatzinc4Parser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}
	public static class Flatzinc_modelContext extends ParserRuleContext {
		public Solver aSolver;
		public Datas datas;
		public ConstraintContext constraint(int i) {
			return getRuleContext(ConstraintContext.class,i);
		}
		public List<ConstraintContext> constraint() {
			return getRuleContexts(ConstraintContext.class);
		}
		public List<Pred_declContext> pred_decl() {
			return getRuleContexts(Pred_declContext.class);
		}
		public Var_declContext var_decl(int i) {
			return getRuleContext(Var_declContext.class,i);
		}
		public Param_declContext param_decl(int i) {
			return getRuleContext(Param_declContext.class,i);
		}
		public Pred_declContext pred_decl(int i) {
			return getRuleContext(Pred_declContext.class,i);
		}
		public List<Var_declContext> var_decl() {
			return getRuleContexts(Var_declContext.class);
		}
		public List<Param_declContext> param_decl() {
			return getRuleContexts(Param_declContext.class);
		}
		public Solve_goalContext solve_goal() {
			return getRuleContext(Solve_goalContext.class,0);
		}
		public Flatzinc_modelContext(ParserRuleContext parent, int invokingState) { super(parent, invokingState); }
		public Flatzinc_modelContext(ParserRuleContext parent, int invokingState, Solver aSolver, Datas datas) {
			super(parent, invokingState);
			this.aSolver = aSolver;
			this.datas = datas;
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

	public final Flatzinc_modelContext flatzinc_model(Solver aSolver,Datas datas) throws RecognitionException {
		Flatzinc_modelContext _localctx = new Flatzinc_modelContext(_ctx, getState(), aSolver, datas);
		enterRule(_localctx, 0, RULE_flatzinc_model);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{

			    this.mSolver = aSolver;
			    this.datas = datas;
			    
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

			    if (LoggerFactory.getLogger("fzn").isInfoEnabled()) {
			        datas.plugLayout(mSolver);
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

	public static class Par_typeContext extends ParserRuleContext {
		public Declaration decl;
		public Index_setContext d;
		public Par_type_uContext p;
		public TerminalNode CM(int i) {
			return getToken(Flatzinc4Parser.CM, i);
		}
		public Par_type_uContext par_type_u() {
			return getRuleContext(Par_type_uContext.class,0);
		}
		public Index_setContext index_set(int i) {
			return getRuleContext(Index_setContext.class,i);
		}
		public TerminalNode OF() { return getToken(Flatzinc4Parser.OF, 0); }
		public TerminalNode RS() { return getToken(Flatzinc4Parser.RS, 0); }
		public TerminalNode LS() { return getToken(Flatzinc4Parser.LS, 0); }
		public List<TerminalNode> CM() { return getTokens(Flatzinc4Parser.CM); }
		public List<Index_setContext> index_set() {
			return getRuleContexts(Index_setContext.class);
		}
		public TerminalNode ARRAY() { return getToken(Flatzinc4Parser.ARRAY, 0); }
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
			setState(92);
			switch (_input.LA(1)) {
			case ARRAY:
				enterOuterAlt(_localctx, 1);
				{

				        List<Declaration> decls = new ArrayList();
				    
				setState(71); match(ARRAY);
				setState(72); match(LS);
				setState(73); ((Par_typeContext)_localctx).d = index_set();
				decls.add(((Par_typeContext)_localctx).d.decl);
				setState(81);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==CM) {
					{
					{
					setState(75); match(CM);
					setState(76); ((Par_typeContext)_localctx).d = index_set();
					decls.add(((Par_typeContext)_localctx).d.decl);
					}
					}
					setState(83);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(84); match(RS);
				setState(85); match(OF);
				setState(86); ((Par_typeContext)_localctx).p = par_type_u();

				    ((Par_typeContext)_localctx).decl =  new DArray(decls,((Par_typeContext)_localctx).p.decl);
				    
				}
				break;
			case BOOL:
			case INT:
			case FLOAT:
			case SET:
				enterOuterAlt(_localctx, 2);
				{
				setState(89); ((Par_typeContext)_localctx).p = par_type_u();

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
		public TerminalNode SET() { return getToken(Flatzinc4Parser.SET, 0); }
		public TerminalNode BOOL() { return getToken(Flatzinc4Parser.BOOL, 0); }
		public TerminalNode INT() { return getToken(Flatzinc4Parser.INT, 0); }
		public TerminalNode FLOAT() { return getToken(Flatzinc4Parser.FLOAT, 0); }
		public TerminalNode OF() { return getToken(Flatzinc4Parser.OF, 0); }
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
			setState(104);
			switch (_input.LA(1)) {
			case BOOL:
				enterOuterAlt(_localctx, 1);
				{
				setState(94); match(BOOL);

				    ((Par_type_uContext)_localctx).decl = DBool.me;
				    
				}
				break;
			case FLOAT:
				enterOuterAlt(_localctx, 2);
				{
				setState(96); match(FLOAT);

				    ((Par_type_uContext)_localctx).decl = DFloat.me;
				    
				}
				break;
			case SET:
				enterOuterAlt(_localctx, 3);
				{
				setState(98); match(SET);
				setState(99); match(OF);
				setState(100); match(INT);

				    ((Par_type_uContext)_localctx).decl = DSetOfInt.me;
				    
				}
				break;
			case INT:
				enterOuterAlt(_localctx, 4);
				{
				setState(102); match(INT);

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
		public TerminalNode CM(int i) {
			return getToken(Flatzinc4Parser.CM, i);
		}
		public TerminalNode VAR() { return getToken(Flatzinc4Parser.VAR, 0); }
		public Index_setContext index_set(int i) {
			return getRuleContext(Index_setContext.class,i);
		}
		public TerminalNode OF() { return getToken(Flatzinc4Parser.OF, 0); }
		public TerminalNode RS() { return getToken(Flatzinc4Parser.RS, 0); }
		public TerminalNode LS() { return getToken(Flatzinc4Parser.LS, 0); }
		public List<TerminalNode> CM() { return getTokens(Flatzinc4Parser.CM); }
		public Var_type_uContext var_type_u() {
			return getRuleContext(Var_type_uContext.class,0);
		}
		public List<Index_setContext> index_set() {
			return getRuleContexts(Index_setContext.class);
		}
		public TerminalNode ARRAY() { return getToken(Flatzinc4Parser.ARRAY, 0); }
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
			setState(130);
			switch (_input.LA(1)) {
			case ARRAY:
				enterOuterAlt(_localctx, 1);
				{

				    List<Declaration> decls = new ArrayList();
				    
				setState(107); match(ARRAY);
				setState(108); match(LS);
				setState(109); ((Var_typeContext)_localctx).d = index_set();
				decls.add(((Var_typeContext)_localctx).d.decl);
				setState(117);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==CM) {
					{
					{
					setState(111); match(CM);
					setState(112); ((Var_typeContext)_localctx).d = index_set();
					decls.add(((Var_typeContext)_localctx).d.decl);
					}
					}
					setState(119);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(120); match(RS);
				setState(121); match(OF);
				setState(122); match(VAR);
				setState(123); ((Var_typeContext)_localctx).vt = var_type_u();

				    ((Var_typeContext)_localctx).decl =  new DArray(decls, ((Var_typeContext)_localctx).vt.decl);
				    
				}
				break;
			case VAR:
				enterOuterAlt(_localctx, 2);
				{
				setState(126); match(VAR);
				setState(127); ((Var_typeContext)_localctx).vt = var_type_u();

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
		public TerminalNode SET() { return getToken(Flatzinc4Parser.SET, 0); }
		public TerminalNode CM(int i) {
			return getToken(Flatzinc4Parser.CM, i);
		}
		public TerminalNode BOOL() { return getToken(Flatzinc4Parser.BOOL, 0); }
		public TerminalNode LB() { return getToken(Flatzinc4Parser.LB, 0); }
		public TerminalNode INT() { return getToken(Flatzinc4Parser.INT, 0); }
		public TerminalNode FLOAT() { return getToken(Flatzinc4Parser.FLOAT, 0); }
		public TerminalNode DD() { return getToken(Flatzinc4Parser.DD, 0); }
		public TerminalNode RB() { return getToken(Flatzinc4Parser.RB, 0); }
		public List<TerminalNode> INT_CONST() { return getTokens(Flatzinc4Parser.INT_CONST); }
		public TerminalNode OF() { return getToken(Flatzinc4Parser.OF, 0); }
		public TerminalNode INT_CONST(int i) {
			return getToken(Flatzinc4Parser.INT_CONST, i);
		}
		public List<TerminalNode> CM() { return getTokens(Flatzinc4Parser.CM); }
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
			setState(178);
			switch ( getInterpreter().adaptivePredict(_input,11,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(132); match(BOOL);

				    ((Var_type_uContext)_localctx).decl =  DBool.me;
				    
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(134); match(FLOAT);

				    ((Var_type_uContext)_localctx).decl =  DFloat.me;
				    
				}
				break;

			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(136); match(INT);

				    ((Var_type_uContext)_localctx).decl =  DInt.me;
				    
				}
				break;

			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(138); ((Var_type_uContext)_localctx).i1 = match(INT_CONST);
				setState(139); match(DD);
				setState(140); ((Var_type_uContext)_localctx).i2 = match(INT_CONST);

				     ((Var_type_uContext)_localctx).decl =  new DInt2(EInt.make((((Var_type_uContext)_localctx).i1!=null?((Var_type_uContext)_localctx).i1.getText():null)), EInt.make((((Var_type_uContext)_localctx).i2!=null?((Var_type_uContext)_localctx).i2.getText():null)));
				     
				}
				break;

			case 5:
				enterOuterAlt(_localctx, 5);
				{

				    ArrayList<EInt> values = new ArrayList();
				    
				setState(143); match(LB);
				setState(144); ((Var_type_uContext)_localctx).i = match(INT_CONST);
				values.add(EInt.make((((Var_type_uContext)_localctx).i!=null?((Var_type_uContext)_localctx).i.getText():null)));
				setState(151);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==CM) {
					{
					{
					setState(146); match(CM);
					setState(147); ((Var_type_uContext)_localctx).i = match(INT_CONST);
					values.add(EInt.make((((Var_type_uContext)_localctx).i!=null?((Var_type_uContext)_localctx).i.getText():null)));
					}
					}
					setState(153);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(154); match(RB);

				    ((Var_type_uContext)_localctx).decl =  new DManyInt(values);
				    
				}
				break;

			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(156); match(SET);
				setState(157); match(OF);
				setState(158); ((Var_type_uContext)_localctx).i1 = match(INT_CONST);
				setState(159); match(DD);
				setState(160); ((Var_type_uContext)_localctx).i2 = match(INT_CONST);

				    ((Var_type_uContext)_localctx).decl =  new DSet(new DInt2(EInt.make((((Var_type_uContext)_localctx).i1!=null?((Var_type_uContext)_localctx).i1.getText():null)), EInt.make((((Var_type_uContext)_localctx).i2!=null?((Var_type_uContext)_localctx).i2.getText():null))));
				    
				}
				break;

			case 7:
				enterOuterAlt(_localctx, 7);
				{

				    ArrayList<EInt> values = new ArrayList();
				    
				setState(163); match(SET);
				setState(164); match(OF);
				setState(165); match(LB);
				setState(166); ((Var_type_uContext)_localctx).i = match(INT_CONST);
				values.add(EInt.make((((Var_type_uContext)_localctx).i!=null?((Var_type_uContext)_localctx).i.getText():null)));
				setState(173);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==CM) {
					{
					{
					setState(168); match(CM);
					setState(169); ((Var_type_uContext)_localctx).i = match(INT_CONST);
					values.add(EInt.make((((Var_type_uContext)_localctx).i!=null?((Var_type_uContext)_localctx).i.getText():null)));
					}
					}
					setState(175);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(176); match(RB);

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
		public TerminalNode INT() { return getToken(Flatzinc4Parser.INT, 0); }
		public TerminalNode DD() { return getToken(Flatzinc4Parser.DD, 0); }
		public List<TerminalNode> INT_CONST() { return getTokens(Flatzinc4Parser.INT_CONST); }
		public TerminalNode INT_CONST(int i) {
			return getToken(Flatzinc4Parser.INT_CONST, i);
		}
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
			setState(186);
			switch (_input.LA(1)) {
			case INT_CONST:
				enterOuterAlt(_localctx, 1);
				{
				setState(180); ((Index_setContext)_localctx).i1 = match(INT_CONST);
				setState(181); match(DD);
				setState(182); ((Index_setContext)_localctx).i2 = match(INT_CONST);

				    ((Index_setContext)_localctx).decl =  new DInt2(EInt.make((((Index_setContext)_localctx).i1!=null?((Index_setContext)_localctx).i1.getText():null)), EInt.make((((Index_setContext)_localctx).i2!=null?((Index_setContext)_localctx).i2.getText():null)));
				    
				}
				break;
			case INT:
				enterOuterAlt(_localctx, 2);
				{
				setState(184); match(INT);

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
		public TerminalNode CM(int i) {
			return getToken(Flatzinc4Parser.CM, i);
		}
		public TerminalNode DD() { return getToken(Flatzinc4Parser.DD, 0); }
		public List<TerminalNode> INT_CONST() { return getTokens(Flatzinc4Parser.INT_CONST); }
		public TerminalNode LS() { return getToken(Flatzinc4Parser.LS, 0); }
		public TerminalNode LB() { return getToken(Flatzinc4Parser.LB, 0); }
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public TerminalNode RB() { return getToken(Flatzinc4Parser.RB, 0); }
		public Bool_constContext bool_const() {
			return getRuleContext(Bool_constContext.class,0);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public TerminalNode RS() { return getToken(Flatzinc4Parser.RS, 0); }
		public Id_exprContext id_expr() {
			return getRuleContext(Id_exprContext.class,0);
		}
		public TerminalNode INT_CONST(int i) {
			return getToken(Flatzinc4Parser.INT_CONST, i);
		}
		public TerminalNode STRING() { return getToken(Flatzinc4Parser.STRING, 0); }
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
			setState(236);
			switch ( getInterpreter().adaptivePredict(_input,17,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(188); match(LB);
				setState(189); match(RB);

				    ((ExprContext)_localctx).exp =  new ESetList(new ArrayList());
				    
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{

				    ArrayList<EInt> values = new ArrayList();
				    
				setState(192); match(LB);
				setState(193); ((ExprContext)_localctx).i = match(INT_CONST);
				values.add(EInt.make((((ExprContext)_localctx).i!=null?((ExprContext)_localctx).i.getText():null)));
				setState(200);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==CM) {
					{
					{
					setState(195); match(CM);
					setState(196); ((ExprContext)_localctx).i = match(INT_CONST);
					values.add(EInt.make((((ExprContext)_localctx).i!=null?((ExprContext)_localctx).i.getText():null)));
					}
					}
					setState(202);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(203); match(RB);

				    ((ExprContext)_localctx).exp =  new ESetList(values);
				    
				}
				break;

			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(205); ((ExprContext)_localctx).b = bool_const();

				    ((ExprContext)_localctx).exp = EBool.make(((ExprContext)_localctx).b.value);
				    
				}
				break;

			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(208); ((ExprContext)_localctx).i1 = match(INT_CONST);
				setState(211);
				_la = _input.LA(1);
				if (_la==DD) {
					{
					setState(209); match(DD);
					setState(210); ((ExprContext)_localctx).i2 = match(INT_CONST);
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
				    
				setState(215); match(LS);
				setState(227);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << TRUE) | (1L << FALSE) | (1L << LB) | (1L << LS) | (1L << IDENTIFIER) | (1L << INT_CONST) | (1L << STRING))) != 0)) {
					{
					setState(216); ((ExprContext)_localctx).e = expr();
					exps.add(((ExprContext)_localctx).e.exp);
					setState(224);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==CM) {
						{
						{
						setState(218); match(CM);
						setState(219); ((ExprContext)_localctx).e = expr();
						exps.add(((ExprContext)_localctx).e.exp);
						}
						}
						setState(226);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					}
				}

				setState(229); match(RS);

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
				setState(231); ((ExprContext)_localctx).ie = id_expr();

				    ((ExprContext)_localctx).exp =  ((ExprContext)_localctx).ie.exp;
				    
				}
				break;

			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(234); ((ExprContext)_localctx).STRING = match(STRING);

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
		public TerminalNode CM(int i) {
			return getToken(Flatzinc4Parser.CM, i);
		}
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public TerminalNode LP() { return getToken(Flatzinc4Parser.LP, 0); }
		public TerminalNode IDENTIFIER() { return getToken(Flatzinc4Parser.IDENTIFIER, 0); }
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public TerminalNode INT_CONST() { return getToken(Flatzinc4Parser.INT_CONST, 0); }
		public TerminalNode RS() { return getToken(Flatzinc4Parser.RS, 0); }
		public TerminalNode RP() { return getToken(Flatzinc4Parser.RP, 0); }
		public TerminalNode LS() { return getToken(Flatzinc4Parser.LS, 0); }
		public List<TerminalNode> CM() { return getTokens(Flatzinc4Parser.CM); }
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
			enterOuterAlt(_localctx, 1);
			{

			    ArrayList<Expression> exps = new ArrayList();
			    
			setState(239); ((Id_exprContext)_localctx).IDENTIFIER = match(IDENTIFIER);
			setState(257);
			switch (_input.LA(1)) {
			case LP:
				{
				{
				setState(240); match(LP);
				setState(241); ((Id_exprContext)_localctx).e = expr();
				exps.add(((Id_exprContext)_localctx).e.exp);
				setState(249);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==CM) {
					{
					{
					setState(243); match(CM);
					setState(244); ((Id_exprContext)_localctx).e = expr();
					exps.add(((Id_exprContext)_localctx).e.exp);
					}
					}
					setState(251);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(252); match(RP);
				}
				}
				break;
			case LS:
				{
				{
				setState(254); match(LS);
				setState(255); ((Id_exprContext)_localctx).i = match(INT_CONST);
				setState(256); match(RS);
				}
				}
				break;
			case CM:
			case RS:
			case SC:
			case RP:
				break;
			default:
				throw new NoViableAltException(this);
			}

			    if(exps.size()>0){
			        ((Id_exprContext)_localctx).exp =  new EAnnotation(new EIdentifier(datas, (((Id_exprContext)_localctx).IDENTIFIER!=null?((Id_exprContext)_localctx).IDENTIFIER.getText():null)), exps);
			    }else if(((Id_exprContext)_localctx).i!=null) {
			        ((Id_exprContext)_localctx).exp =  new EIdArray(datas, (((Id_exprContext)_localctx).IDENTIFIER!=null?((Id_exprContext)_localctx).IDENTIFIER.getText():null), Integer.parseInt((((Id_exprContext)_localctx).i!=null?((Id_exprContext)_localctx).i.getText():null)));
			    }else{
			        ((Id_exprContext)_localctx).exp =  new EIdentifier(datas, (((Id_exprContext)_localctx).IDENTIFIER!=null?((Id_exprContext)_localctx).IDENTIFIER.getText():null));
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

	public static class Param_declContext extends ParserRuleContext {
		public Par_typeContext pt;
		public Token IDENTIFIER;
		public ExprContext e;
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TerminalNode IDENTIFIER() { return getToken(Flatzinc4Parser.IDENTIFIER, 0); }
		public Par_typeContext par_type() {
			return getRuleContext(Par_typeContext.class,0);
		}
		public TerminalNode CL() { return getToken(Flatzinc4Parser.CL, 0); }
		public TerminalNode SC() { return getToken(Flatzinc4Parser.SC, 0); }
		public TerminalNode EQ() { return getToken(Flatzinc4Parser.EQ, 0); }
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
			setState(261); ((Param_declContext)_localctx).pt = par_type();
			setState(262); match(CL);
			setState(263); ((Param_declContext)_localctx).IDENTIFIER = match(IDENTIFIER);
			setState(264); match(EQ);
			setState(265); ((Param_declContext)_localctx).e = expr();
			setState(266); match(SC);

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
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TerminalNode IDENTIFIER() { return getToken(Flatzinc4Parser.IDENTIFIER, 0); }
		public TerminalNode CL() { return getToken(Flatzinc4Parser.CL, 0); }
		public AnnotationsContext annotations() {
			return getRuleContext(AnnotationsContext.class,0);
		}
		public Var_typeContext var_type() {
			return getRuleContext(Var_typeContext.class,0);
		}
		public TerminalNode SC() { return getToken(Flatzinc4Parser.SC, 0); }
		public TerminalNode EQ() { return getToken(Flatzinc4Parser.EQ, 0); }
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
			setState(269); ((Var_declContext)_localctx).vt = var_type();
			setState(270); match(CL);
			setState(271); ((Var_declContext)_localctx).IDENTIFIER = match(IDENTIFIER);
			setState(272); ((Var_declContext)_localctx).anns = annotations();
			setState(275);
			_la = _input.LA(1);
			if (_la==EQ) {
				{
				setState(273); ((Var_declContext)_localctx).eq = match(EQ);
				setState(274); ((Var_declContext)_localctx).e = expr();
				}
			}

			setState(277); match(SC);

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
		public TerminalNode CM(int i) {
			return getToken(Flatzinc4Parser.CM, i);
		}
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public TerminalNode LP() { return getToken(Flatzinc4Parser.LP, 0); }
		public TerminalNode IDENTIFIER() { return getToken(Flatzinc4Parser.IDENTIFIER, 0); }
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public TerminalNode RP() { return getToken(Flatzinc4Parser.RP, 0); }
		public TerminalNode CONSTRAINT() { return getToken(Flatzinc4Parser.CONSTRAINT, 0); }
		public AnnotationsContext annotations() {
			return getRuleContext(AnnotationsContext.class,0);
		}
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
			    
			setState(281); match(CONSTRAINT);
			setState(282); ((ConstraintContext)_localctx).IDENTIFIER = match(IDENTIFIER);
			setState(283); match(LP);
			setState(284); ((ConstraintContext)_localctx).e = expr();
			exps.add(((ConstraintContext)_localctx).e.exp);
			setState(292);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==CM) {
				{
				{
				setState(286); match(CM);
				setState(287); ((ConstraintContext)_localctx).e = expr();
				exps.add(((ConstraintContext)_localctx).e.exp);
				}
				}
				setState(294);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(295); match(RP);
			setState(296); ((ConstraintContext)_localctx).anns = annotations();
			setState(297); match(SC);

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
		public TerminalNode SOLVE() { return getToken(Flatzinc4Parser.SOLVE, 0); }
		public AnnotationsContext annotations() {
			return getRuleContext(AnnotationsContext.class,0);
		}
		public TerminalNode SC() { return getToken(Flatzinc4Parser.SC, 0); }
		public ResolutionContext resolution() {
			return getRuleContext(ResolutionContext.class,0);
		}
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
			setState(300); match(SOLVE);
			setState(301); ((Solve_goalContext)_localctx).anns = annotations();
			setState(302); ((Solve_goalContext)_localctx).res = resolution();
			setState(303); match(SC);

			    FGoal.define_goal(datas, mSolver,((Solve_goalContext)_localctx).anns.anns,((Solve_goalContext)_localctx).res.rtype,((Solve_goalContext)_localctx).res.exp);
			    
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
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TerminalNode SATISFY() { return getToken(Flatzinc4Parser.SATISFY, 0); }
		public TerminalNode MAXIMIZE() { return getToken(Flatzinc4Parser.MAXIMIZE, 0); }
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
			setState(316);
			switch (_input.LA(1)) {
			case MINIMIZE:
				enterOuterAlt(_localctx, 1);
				{
				setState(306); match(MINIMIZE);
				setState(307); ((ResolutionContext)_localctx).e = expr();

				    ((ResolutionContext)_localctx).rtype = ResolutionPolicy.MINIMIZE;
				    ((ResolutionContext)_localctx).exp = ((ResolutionContext)_localctx).e.exp;
				    
				}
				break;
			case MAXIMIZE:
				enterOuterAlt(_localctx, 2);
				{
				setState(310); match(MAXIMIZE);
				setState(311); ((ResolutionContext)_localctx).e = expr();

				    ((ResolutionContext)_localctx).rtype = ResolutionPolicy.MAXIMIZE;
				    ((ResolutionContext)_localctx).exp = ((ResolutionContext)_localctx).e.exp;
				    
				}
				break;
			case SATISFY:
				enterOuterAlt(_localctx, 3);
				{
				setState(314); match(SATISFY);

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
		public AnnotationContext annotation(int i) {
			return getRuleContext(AnnotationContext.class,i);
		}
		public List<TerminalNode> DC() { return getTokens(Flatzinc4Parser.DC); }
		public List<AnnotationContext> annotation() {
			return getRuleContexts(AnnotationContext.class);
		}
		public TerminalNode DC(int i) {
			return getToken(Flatzinc4Parser.DC, i);
		}
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
			    
			setState(325);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==DC) {
				{
				{
				setState(319); match(DC);
				setState(320); ((AnnotationsContext)_localctx).e = annotation();
				_localctx.anns.add(((AnnotationsContext)_localctx).e.ann);
				}
				}
				setState(327);
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
		public TerminalNode CM(int i) {
			return getToken(Flatzinc4Parser.CM, i);
		}
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public TerminalNode LP() { return getToken(Flatzinc4Parser.LP, 0); }
		public TerminalNode IDENTIFIER() { return getToken(Flatzinc4Parser.IDENTIFIER, 0); }
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public TerminalNode RP() { return getToken(Flatzinc4Parser.RP, 0); }
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
			    
			setState(329); ((AnnotationContext)_localctx).IDENTIFIER = match(IDENTIFIER);
			setState(344);
			_la = _input.LA(1);
			if (_la==LP) {
				{
				setState(330); match(LP);
				setState(331); ((AnnotationContext)_localctx).e = expr();
				exps.add(((AnnotationContext)_localctx).e.exp);
				setState(339);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==CM) {
					{
					{
					setState(333); match(CM);
					setState(334); ((AnnotationContext)_localctx).e = expr();
					exps.add(((AnnotationContext)_localctx).e.exp);
					}
					}
					setState(341);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(342); match(RP);
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
		public TerminalNode TRUE() { return getToken(Flatzinc4Parser.TRUE, 0); }
		public TerminalNode FALSE() { return getToken(Flatzinc4Parser.FALSE, 0); }
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
			setState(352);
			switch (_input.LA(1)) {
			case TRUE:
				enterOuterAlt(_localctx, 1);
				{
				setState(348); match(TRUE);
				((Bool_constContext)_localctx).value =  true;
				}
				break;
			case FALSE:
				enterOuterAlt(_localctx, 2);
				{
				setState(350); match(FALSE);
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
		public TerminalNode CM(int i) {
			return getToken(Flatzinc4Parser.CM, i);
		}
		public TerminalNode PREDICATE() { return getToken(Flatzinc4Parser.PREDICATE, 0); }
		public Pred_paramContext pred_param(int i) {
			return getRuleContext(Pred_paramContext.class,i);
		}
		public TerminalNode LP() { return getToken(Flatzinc4Parser.LP, 0); }
		public TerminalNode IDENTIFIER() { return getToken(Flatzinc4Parser.IDENTIFIER, 0); }
		public TerminalNode RP() { return getToken(Flatzinc4Parser.RP, 0); }
		public List<Pred_paramContext> pred_param() {
			return getRuleContexts(Pred_paramContext.class);
		}
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
			setState(354); match(PREDICATE);
			setState(355); ((Pred_declContext)_localctx).IDENTIFIER = match(IDENTIFIER);
			setState(356); match(LP);
			setState(357); pred_param();
			setState(362);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==CM) {
				{
				{
				setState(358); match(CM);
				setState(359); pred_param();
				}
				}
				setState(364);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(365); match(RP);
			setState(366); match(SC);

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
		public Pred_param_typeContext pred_param_type() {
			return getRuleContext(Pred_param_typeContext.class,0);
		}
		public TerminalNode IDENTIFIER() { return getToken(Flatzinc4Parser.IDENTIFIER, 0); }
		public TerminalNode CL() { return getToken(Flatzinc4Parser.CL, 0); }
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
			setState(369); pred_param_type();
			setState(370); match(CL);
			setState(371); match(IDENTIFIER);
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
			setState(375);
			switch ( getInterpreter().adaptivePredict(_input,28,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(373); par_pred_param_type();
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(374); var_pred_param_type();
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
		public TerminalNode SET() { return getToken(Flatzinc4Parser.SET, 0); }
		public TerminalNode CM(int i) {
			return getToken(Flatzinc4Parser.CM, i);
		}
		public TerminalNode DD() { return getToken(Flatzinc4Parser.DD, 0); }
		public List<TerminalNode> INT_CONST() { return getTokens(Flatzinc4Parser.INT_CONST); }
		public TerminalNode OF(int i) {
			return getToken(Flatzinc4Parser.OF, i);
		}
		public TerminalNode LS() { return getToken(Flatzinc4Parser.LS, 0); }
		public TerminalNode LB() { return getToken(Flatzinc4Parser.LB, 0); }
		public TerminalNode RB() { return getToken(Flatzinc4Parser.RB, 0); }
		public Index_setContext index_set(int i) {
			return getRuleContext(Index_setContext.class,i);
		}
		public TerminalNode RS() { return getToken(Flatzinc4Parser.RS, 0); }
		public List<TerminalNode> OF() { return getTokens(Flatzinc4Parser.OF); }
		public Par_typeContext par_type() {
			return getRuleContext(Par_typeContext.class,0);
		}
		public TerminalNode INT_CONST(int i) {
			return getToken(Flatzinc4Parser.INT_CONST, i);
		}
		public List<Index_setContext> index_set() {
			return getRuleContexts(Index_setContext.class);
		}
		public List<TerminalNode> CM() { return getTokens(Flatzinc4Parser.CM); }
		public TerminalNode ARRAY() { return getToken(Flatzinc4Parser.ARRAY, 0); }
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
			setState(490);
			switch ( getInterpreter().adaptivePredict(_input,37,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(377); par_type();
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(378); match(INT_CONST);
				setState(379); match(DD);
				setState(380); match(INT_CONST);
				}
				break;

			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(381); match(LB);
				setState(382); match(INT_CONST);
				setState(387);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==CM) {
					{
					{
					setState(383); match(CM);
					setState(384); match(INT_CONST);
					}
					}
					setState(389);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(390); match(RB);
				}
				break;

			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(391); match(SET);
				setState(392); match(OF);
				setState(393); match(INT_CONST);
				setState(394); match(DD);
				setState(395); match(INT_CONST);
				}
				break;

			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(396); match(SET);
				setState(397); match(OF);
				setState(398); match(LB);
				setState(399); match(INT_CONST);
				setState(404);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==CM) {
					{
					{
					setState(400); match(CM);
					setState(401); match(INT_CONST);
					}
					}
					setState(406);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(407); match(RB);
				}
				break;

			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(408); match(ARRAY);
				setState(409); match(LS);
				setState(410); index_set();
				setState(415);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==CM) {
					{
					{
					setState(411); match(CM);
					setState(412); index_set();
					}
					}
					setState(417);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(418); match(RS);
				setState(419); match(OF);
				setState(420); match(INT_CONST);
				setState(421); match(DD);
				setState(422); match(INT_CONST);
				}
				break;

			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(424); match(ARRAY);
				setState(425); match(LS);
				setState(426); index_set();
				setState(431);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==CM) {
					{
					{
					setState(427); match(CM);
					setState(428); index_set();
					}
					}
					setState(433);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(434); match(RS);
				setState(435); match(OF);
				setState(436); match(LB);
				setState(437); match(INT_CONST);
				setState(442);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==CM) {
					{
					{
					setState(438); match(CM);
					setState(439); match(INT_CONST);
					}
					}
					setState(444);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(445); match(RB);
				}
				break;

			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(447); match(ARRAY);
				setState(448); match(LS);
				setState(449); index_set();
				setState(454);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==CM) {
					{
					{
					setState(450); match(CM);
					setState(451); index_set();
					}
					}
					setState(456);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(457); match(RS);
				setState(458); match(OF);
				setState(459); match(SET);
				setState(460); match(OF);
				setState(461); match(INT_CONST);
				setState(462); match(DD);
				setState(463); match(INT_CONST);
				}
				break;

			case 9:
				enterOuterAlt(_localctx, 9);
				{
				setState(465); match(ARRAY);
				setState(466); match(LS);
				setState(467); index_set();
				setState(472);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==CM) {
					{
					{
					setState(468); match(CM);
					setState(469); index_set();
					}
					}
					setState(474);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(475); match(RS);
				setState(476); match(OF);
				setState(477); match(SET);
				setState(478); match(OF);
				setState(479); match(LB);
				setState(480); match(INT_CONST);
				setState(485);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==CM) {
					{
					{
					setState(481); match(CM);
					setState(482); match(INT_CONST);
					}
					}
					setState(487);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(488); match(RB);
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
		public TerminalNode SET() { return getToken(Flatzinc4Parser.SET, 0); }
		public TerminalNode CM(int i) {
			return getToken(Flatzinc4Parser.CM, i);
		}
		public TerminalNode INT() { return getToken(Flatzinc4Parser.INT, 0); }
		public TerminalNode VAR() { return getToken(Flatzinc4Parser.VAR, 0); }
		public Index_setContext index_set(int i) {
			return getRuleContext(Index_setContext.class,i);
		}
		public TerminalNode RS() { return getToken(Flatzinc4Parser.RS, 0); }
		public TerminalNode OF(int i) {
			return getToken(Flatzinc4Parser.OF, i);
		}
		public List<TerminalNode> OF() { return getTokens(Flatzinc4Parser.OF); }
		public TerminalNode LS() { return getToken(Flatzinc4Parser.LS, 0); }
		public Var_typeContext var_type() {
			return getRuleContext(Var_typeContext.class,0);
		}
		public List<TerminalNode> CM() { return getTokens(Flatzinc4Parser.CM); }
		public List<Index_setContext> index_set() {
			return getRuleContexts(Index_setContext.class);
		}
		public TerminalNode ARRAY() { return getToken(Flatzinc4Parser.ARRAY, 0); }
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
			setState(514);
			switch ( getInterpreter().adaptivePredict(_input,39,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(492); var_type();
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(493); match(VAR);
				setState(494); match(SET);
				setState(495); match(OF);
				setState(496); match(INT);
				}
				break;

			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(497); match(ARRAY);
				setState(498); match(LS);
				setState(499); index_set();
				setState(504);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==CM) {
					{
					{
					setState(500); match(CM);
					setState(501); index_set();
					}
					}
					setState(506);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(507); match(RS);
				setState(508); match(OF);
				setState(509); match(VAR);
				setState(510); match(SET);
				setState(511); match(OF);
				setState(512); match(INT);
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
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\3\'\u0207\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t"+
		"\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\3\2\3\2\7\2/\n\2\f\2\16\2\62"+
		"\13\2\3\2\7\2\65\n\2\f\2\16\28\13\2\3\2\7\2;\n\2\f\2\16\2>\13\2\3\2\7"+
		"\2A\n\2\f\2\16\2D\13\2\3\2\3\2\3\2\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3"+
		"\7\3R\n\3\f\3\16\3U\13\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\5\3_\n\3\3\4"+
		"\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\5\4k\n\4\3\5\3\5\3\5\3\5\3\5\3\5"+
		"\3\5\3\5\3\5\7\5v\n\5\f\5\16\5y\13\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3"+
		"\5\3\5\5\5\u0085\n\5\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3"+
		"\6\3\6\3\6\3\6\3\6\7\6\u0098\n\6\f\6\16\6\u009b\13\6\3\6\3\6\3\6\3\6\3"+
		"\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\7\6\u00ae\n\6\f\6\16"+
		"\6\u00b1\13\6\3\6\3\6\5\6\u00b5\n\6\3\7\3\7\3\7\3\7\3\7\3\7\5\7\u00bd"+
		"\n\7\3\b\3\b\3\b\3\b\3\b\3\b\3\b\3\b\3\b\3\b\7\b\u00c9\n\b\f\b\16\b\u00cc"+
		"\13\b\3\b\3\b\3\b\3\b\3\b\3\b\3\b\3\b\5\b\u00d6\n\b\3\b\3\b\3\b\3\b\3"+
		"\b\3\b\3\b\3\b\3\b\7\b\u00e1\n\b\f\b\16\b\u00e4\13\b\5\b\u00e6\n\b\3\b"+
		"\3\b\3\b\3\b\3\b\3\b\3\b\5\b\u00ef\n\b\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t"+
		"\3\t\7\t\u00fa\n\t\f\t\16\t\u00fd\13\t\3\t\3\t\3\t\3\t\3\t\5\t\u0104\n"+
		"\t\3\t\3\t\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\13\3\13\3\13\3\13\3\13\3"+
		"\13\5\13\u0116\n\13\3\13\3\13\3\13\3\f\3\f\3\f\3\f\3\f\3\f\3\f\3\f\3\f"+
		"\3\f\7\f\u0125\n\f\f\f\16\f\u0128\13\f\3\f\3\f\3\f\3\f\3\f\3\r\3\r\3\r"+
		"\3\r\3\r\3\r\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\5\16\u013f"+
		"\n\16\3\17\3\17\3\17\3\17\3\17\7\17\u0146\n\17\f\17\16\17\u0149\13\17"+
		"\3\20\3\20\3\20\3\20\3\20\3\20\3\20\3\20\3\20\7\20\u0154\n\20\f\20\16"+
		"\20\u0157\13\20\3\20\3\20\5\20\u015b\n\20\3\20\3\20\3\21\3\21\3\21\3\21"+
		"\5\21\u0163\n\21\3\22\3\22\3\22\3\22\3\22\3\22\7\22\u016b\n\22\f\22\16"+
		"\22\u016e\13\22\3\22\3\22\3\22\3\22\3\23\3\23\3\23\3\23\3\24\3\24\5\24"+
		"\u017a\n\24\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\7\25\u0184\n\25\f"+
		"\25\16\25\u0187\13\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25"+
		"\3\25\3\25\7\25\u0195\n\25\f\25\16\25\u0198\13\25\3\25\3\25\3\25\3\25"+
		"\3\25\3\25\7\25\u01a0\n\25\f\25\16\25\u01a3\13\25\3\25\3\25\3\25\3\25"+
		"\3\25\3\25\3\25\3\25\3\25\3\25\3\25\7\25\u01b0\n\25\f\25\16\25\u01b3\13"+
		"\25\3\25\3\25\3\25\3\25\3\25\3\25\7\25\u01bb\n\25\f\25\16\25\u01be\13"+
		"\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\7\25\u01c7\n\25\f\25\16\25\u01ca"+
		"\13\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25"+
		"\7\25\u01d9\n\25\f\25\16\25\u01dc\13\25\3\25\3\25\3\25\3\25\3\25\3\25"+
		"\3\25\3\25\7\25\u01e6\n\25\f\25\16\25\u01e9\13\25\3\25\3\25\5\25\u01ed"+
		"\n\25\3\26\3\26\3\26\3\26\3\26\3\26\3\26\3\26\3\26\3\26\7\26\u01f9\n\26"+
		"\f\26\16\26\u01fc\13\26\3\26\3\26\3\26\3\26\3\26\3\26\3\26\5\26\u0205"+
		"\n\26\3\26\2\2\27\2\4\6\b\n\f\16\20\22\24\26\30\32\34\36 \"$&(*\2\2\u022f"+
		"\2,\3\2\2\2\4^\3\2\2\2\6j\3\2\2\2\b\u0084\3\2\2\2\n\u00b4\3\2\2\2\f\u00bc"+
		"\3\2\2\2\16\u00ee\3\2\2\2\20\u00f0\3\2\2\2\22\u0107\3\2\2\2\24\u010f\3"+
		"\2\2\2\26\u011a\3\2\2\2\30\u012e\3\2\2\2\32\u013e\3\2\2\2\34\u0140\3\2"+
		"\2\2\36\u014a\3\2\2\2 \u0162\3\2\2\2\"\u0164\3\2\2\2$\u0173\3\2\2\2&\u0179"+
		"\3\2\2\2(\u01ec\3\2\2\2*\u0204\3\2\2\2,\60\b\2\1\2-/\5\"\22\2.-\3\2\2"+
		"\2/\62\3\2\2\2\60.\3\2\2\2\60\61\3\2\2\2\61\66\3\2\2\2\62\60\3\2\2\2\63"+
		"\65\5\22\n\2\64\63\3\2\2\2\658\3\2\2\2\66\64\3\2\2\2\66\67\3\2\2\2\67"+
		"<\3\2\2\28\66\3\2\2\29;\5\24\13\2:9\3\2\2\2;>\3\2\2\2<:\3\2\2\2<=\3\2"+
		"\2\2=B\3\2\2\2><\3\2\2\2?A\5\26\f\2@?\3\2\2\2AD\3\2\2\2B@\3\2\2\2BC\3"+
		"\2\2\2CE\3\2\2\2DB\3\2\2\2EF\5\30\r\2FG\b\2\1\2G\3\3\2\2\2HI\b\3\1\2I"+
		"J\7\n\2\2JK\7\30\2\2KL\5\f\7\2LS\b\3\1\2MN\7\27\2\2NO\5\f\7\2OP\b\3\1"+
		"\2PR\3\2\2\2QM\3\2\2\2RU\3\2\2\2SQ\3\2\2\2ST\3\2\2\2TV\3\2\2\2US\3\2\2"+
		"\2VW\7\31\2\2WX\7\t\2\2XY\5\6\4\2YZ\b\3\1\2Z_\3\2\2\2[\\\5\6\4\2\\]\b"+
		"\3\1\2]_\3\2\2\2^H\3\2\2\2^[\3\2\2\2_\5\3\2\2\2`a\7\3\2\2ak\b\4\1\2bc"+
		"\7\7\2\2ck\b\4\1\2de\7\b\2\2ef\7\t\2\2fg\7\6\2\2gk\b\4\1\2hi\7\6\2\2i"+
		"k\b\4\1\2j`\3\2\2\2jb\3\2\2\2jd\3\2\2\2jh\3\2\2\2k\7\3\2\2\2lm\b\5\1\2"+
		"mn\7\n\2\2no\7\30\2\2op\5\f\7\2pw\b\5\1\2qr\7\27\2\2rs\5\f\7\2st\b\5\1"+
		"\2tv\3\2\2\2uq\3\2\2\2vy\3\2\2\2wu\3\2\2\2wx\3\2\2\2xz\3\2\2\2yw\3\2\2"+
		"\2z{\7\31\2\2{|\7\t\2\2|}\7\13\2\2}~\5\n\6\2~\177\b\5\1\2\177\u0085\3"+
		"\2\2\2\u0080\u0081\7\13\2\2\u0081\u0082\5\n\6\2\u0082\u0083\b\5\1\2\u0083"+
		"\u0085\3\2\2\2\u0084l\3\2\2\2\u0084\u0080\3\2\2\2\u0085\t\3\2\2\2\u0086"+
		"\u0087\7\3\2\2\u0087\u00b5\b\6\1\2\u0088\u0089\7\7\2\2\u0089\u00b5\b\6"+
		"\1\2\u008a\u008b\7\6\2\2\u008b\u00b5\b\6\1\2\u008c\u008d\7%\2\2\u008d"+
		"\u008e\7\23\2\2\u008e\u008f\7%\2\2\u008f\u00b5\b\6\1\2\u0090\u0091\b\6"+
		"\1\2\u0091\u0092\7\25\2\2\u0092\u0093\7%\2\2\u0093\u0099\b\6\1\2\u0094"+
		"\u0095\7\27\2\2\u0095\u0096\7%\2\2\u0096\u0098\b\6\1\2\u0097\u0094\3\2"+
		"\2\2\u0098\u009b\3\2\2\2\u0099\u0097\3\2\2\2\u0099\u009a\3\2\2\2\u009a"+
		"\u009c\3\2\2\2\u009b\u0099\3\2\2\2\u009c\u009d\7\26\2\2\u009d\u00b5\b"+
		"\6\1\2\u009e\u009f\7\b\2\2\u009f\u00a0\7\t\2\2\u00a0\u00a1\7%\2\2\u00a1"+
		"\u00a2\7\23\2\2\u00a2\u00a3\7%\2\2\u00a3\u00b5\b\6\1\2\u00a4\u00a5\b\6"+
		"\1\2\u00a5\u00a6\7\b\2\2\u00a6\u00a7\7\t\2\2\u00a7\u00a8\7\25\2\2\u00a8"+
		"\u00a9\7%\2\2\u00a9\u00af\b\6\1\2\u00aa\u00ab\7\27\2\2\u00ab\u00ac\7%"+
		"\2\2\u00ac\u00ae\b\6\1\2\u00ad\u00aa\3\2\2\2\u00ae\u00b1\3\2\2\2\u00af"+
		"\u00ad\3\2\2\2\u00af\u00b0\3\2\2\2\u00b0\u00b2\3\2\2\2\u00b1\u00af\3\2"+
		"\2\2\u00b2\u00b3\7\26\2\2\u00b3\u00b5\b\6\1\2\u00b4\u0086\3\2\2\2\u00b4"+
		"\u0088\3\2\2\2\u00b4\u008a\3\2\2\2\u00b4\u008c\3\2\2\2\u00b4\u0090\3\2"+
		"\2\2\u00b4\u009e\3\2\2\2\u00b4\u00a4\3\2\2\2\u00b5\13\3\2\2\2\u00b6\u00b7"+
		"\7%\2\2\u00b7\u00b8\7\23\2\2\u00b8\u00b9\7%\2\2\u00b9\u00bd\b\7\1\2\u00ba"+
		"\u00bb\7\6\2\2\u00bb\u00bd\b\7\1\2\u00bc\u00b6\3\2\2\2\u00bc\u00ba\3\2"+
		"\2\2\u00bd\r\3\2\2\2\u00be\u00bf\7\25\2\2\u00bf\u00c0\7\26\2\2\u00c0\u00ef"+
		"\b\b\1\2\u00c1\u00c2\b\b\1\2\u00c2\u00c3\7\25\2\2\u00c3\u00c4\7%\2\2\u00c4"+
		"\u00ca\b\b\1\2\u00c5\u00c6\7\27\2\2\u00c6\u00c7\7%\2\2\u00c7\u00c9\b\b"+
		"\1\2\u00c8\u00c5\3\2\2\2\u00c9\u00cc\3\2\2\2\u00ca\u00c8\3\2\2\2\u00ca"+
		"\u00cb\3\2\2\2\u00cb\u00cd\3\2\2\2\u00cc\u00ca\3\2\2\2\u00cd\u00ce\7\26"+
		"\2\2\u00ce\u00ef\b\b\1\2\u00cf\u00d0\5 \21\2\u00d0\u00d1\b\b\1\2\u00d1"+
		"\u00ef\3\2\2\2\u00d2\u00d5\7%\2\2\u00d3\u00d4\7\23\2\2\u00d4\u00d6\7%"+
		"\2\2\u00d5\u00d3\3\2\2\2\u00d5\u00d6\3\2\2\2\u00d6\u00d7\3\2\2\2\u00d7"+
		"\u00ef\b\b\1\2\u00d8\u00d9\b\b\1\2\u00d9\u00e5\7\30\2\2\u00da\u00db\5"+
		"\16\b\2\u00db\u00e2\b\b\1\2\u00dc\u00dd\7\27\2\2\u00dd\u00de\5\16\b\2"+
		"\u00de\u00df\b\b\1\2\u00df\u00e1\3\2\2\2\u00e0\u00dc\3\2\2\2\u00e1\u00e4"+
		"\3\2\2\2\u00e2\u00e0\3\2\2\2\u00e2\u00e3\3\2\2\2\u00e3\u00e6\3\2\2\2\u00e4"+
		"\u00e2\3\2\2\2\u00e5\u00da\3\2\2\2\u00e5\u00e6\3\2\2\2\u00e6\u00e7\3\2"+
		"\2\2\u00e7\u00e8\7\31\2\2\u00e8\u00ef\b\b\1\2\u00e9\u00ea\5\20\t\2\u00ea"+
		"\u00eb\b\b\1\2\u00eb\u00ef\3\2\2\2\u00ec\u00ed\7&\2\2\u00ed\u00ef\b\b"+
		"\1\2\u00ee\u00be\3\2\2\2\u00ee\u00c1\3\2\2\2\u00ee\u00cf\3\2\2\2\u00ee"+
		"\u00d2\3\2\2\2\u00ee\u00d8\3\2\2\2\u00ee\u00e9\3\2\2\2\u00ee\u00ec\3\2"+
		"\2\2\u00ef\17\3\2\2\2\u00f0\u00f1\b\t\1\2\u00f1\u0103\7\"\2\2\u00f2\u00f3"+
		"\7 \2\2\u00f3\u00f4\5\16\b\2\u00f4\u00fb\b\t\1\2\u00f5\u00f6\7\27\2\2"+
		"\u00f6\u00f7\5\16\b\2\u00f7\u00f8\b\t\1\2\u00f8\u00fa\3\2\2\2\u00f9\u00f5"+
		"\3\2\2\2\u00fa\u00fd\3\2\2\2\u00fb\u00f9\3\2\2\2\u00fb\u00fc\3\2\2\2\u00fc"+
		"\u00fe\3\2\2\2\u00fd\u00fb\3\2\2\2\u00fe\u00ff\7!\2\2\u00ff\u0104\3\2"+
		"\2\2\u0100\u0101\7\30\2\2\u0101\u0102\7%\2\2\u0102\u0104\7\31\2\2\u0103"+
		"\u00f2\3\2\2\2\u0103\u0100\3\2\2\2\u0103\u0104\3\2\2\2\u0104\u0105\3\2"+
		"\2\2\u0105\u0106\b\t\1\2\u0106\21\3\2\2\2\u0107\u0108\5\4\3\2\u0108\u0109"+
		"\7\36\2\2\u0109\u010a\7\"\2\2\u010a\u010b\7\32\2\2\u010b\u010c\5\16\b"+
		"\2\u010c\u010d\7\35\2\2\u010d\u010e\b\n\1\2\u010e\23\3\2\2\2\u010f\u0110"+
		"\5\b\5\2\u0110\u0111\7\36\2\2\u0111\u0112\7\"\2\2\u0112\u0115\5\34\17"+
		"\2\u0113\u0114\7\32\2\2\u0114\u0116\5\16\b\2\u0115\u0113\3\2\2\2\u0115"+
		"\u0116\3\2\2\2\u0116\u0117\3\2\2\2\u0117\u0118\7\35\2\2\u0118\u0119\b"+
		"\13\1\2\u0119\25\3\2\2\2\u011a\u011b\b\f\1\2\u011b\u011c\7\16\2\2\u011c"+
		"\u011d\7\"\2\2\u011d\u011e\7 \2\2\u011e\u011f\5\16\b\2\u011f\u0126\b\f"+
		"\1\2\u0120\u0121\7\27\2\2\u0121\u0122\5\16\b\2\u0122\u0123\b\f\1\2\u0123"+
		"\u0125\3\2\2\2\u0124\u0120\3\2\2\2\u0125\u0128\3\2\2\2\u0126\u0124\3\2"+
		"\2\2\u0126\u0127\3\2\2\2\u0127\u0129\3\2\2\2\u0128\u0126\3\2\2\2\u0129"+
		"\u012a\7!\2\2\u012a\u012b\5\34\17\2\u012b\u012c\7\35\2\2\u012c\u012d\b"+
		"\f\1\2\u012d\27\3\2\2\2\u012e\u012f\7\17\2\2\u012f\u0130\5\34\17\2\u0130"+
		"\u0131\5\32\16\2\u0131\u0132\7\35\2\2\u0132\u0133\b\r\1\2\u0133\31\3\2"+
		"\2\2\u0134\u0135\7\21\2\2\u0135\u0136\5\16\b\2\u0136\u0137\b\16\1\2\u0137"+
		"\u013f\3\2\2\2\u0138\u0139\7\22\2\2\u0139\u013a\5\16\b\2\u013a\u013b\b"+
		"\16\1\2\u013b\u013f\3\2\2\2\u013c\u013d\7\20\2\2\u013d\u013f\b\16\1\2"+
		"\u013e\u0134\3\2\2\2\u013e\u0138\3\2\2\2\u013e\u013c\3\2\2\2\u013f\33"+
		"\3\2\2\2\u0140\u0147\b\17\1\2\u0141\u0142\7\37\2\2\u0142\u0143\5\36\20"+
		"\2\u0143\u0144\b\17\1\2\u0144\u0146\3\2\2\2\u0145\u0141\3\2\2\2\u0146"+
		"\u0149\3\2\2\2\u0147\u0145\3\2\2\2\u0147\u0148\3\2\2\2\u0148\35\3\2\2"+
		"\2\u0149\u0147\3\2\2\2\u014a\u014b\b\20\1\2\u014b\u015a\7\"\2\2\u014c"+
		"\u014d\7 \2\2\u014d\u014e\5\16\b\2\u014e\u0155\b\20\1\2\u014f\u0150\7"+
		"\27\2\2\u0150\u0151\5\16\b\2\u0151\u0152\b\20\1\2\u0152\u0154\3\2\2\2"+
		"\u0153\u014f\3\2\2\2\u0154\u0157\3\2\2\2\u0155\u0153\3\2\2\2\u0155\u0156"+
		"\3\2\2\2\u0156\u0158\3\2\2\2\u0157\u0155\3\2\2\2\u0158\u0159\7!\2\2\u0159"+
		"\u015b\3\2\2\2\u015a\u014c\3\2\2\2\u015a\u015b\3\2\2\2\u015b\u015c\3\2"+
		"\2\2\u015c\u015d\b\20\1\2\u015d\37\3\2\2\2\u015e\u015f\7\4\2\2\u015f\u0163"+
		"\b\21\1\2\u0160\u0161\7\5\2\2\u0161\u0163\b\21\1\2\u0162\u015e\3\2\2\2"+
		"\u0162\u0160\3\2\2\2\u0163!\3\2\2\2\u0164\u0165\7\r\2\2\u0165\u0166\7"+
		"\"\2\2\u0166\u0167\7 \2\2\u0167\u016c\5$\23\2\u0168\u0169\7\27\2\2\u0169"+
		"\u016b\5$\23\2\u016a\u0168\3\2\2\2\u016b\u016e\3\2\2\2\u016c\u016a\3\2"+
		"\2\2\u016c\u016d\3\2\2\2\u016d\u016f\3\2\2\2\u016e\u016c\3\2\2\2\u016f"+
		"\u0170\7!\2\2\u0170\u0171\7\35\2\2\u0171\u0172\b\22\1\2\u0172#\3\2\2\2"+
		"\u0173\u0174\5&\24\2\u0174\u0175\7\36\2\2\u0175\u0176\7\"\2\2\u0176%\3"+
		"\2\2\2\u0177\u017a\5(\25\2\u0178\u017a\5*\26\2\u0179\u0177\3\2\2\2\u0179"+
		"\u0178\3\2\2\2\u017a\'\3\2\2\2\u017b\u01ed\5\4\3\2\u017c\u017d\7%\2\2"+
		"\u017d\u017e\7\23\2\2\u017e\u01ed\7%\2\2\u017f\u0180\7\25\2\2\u0180\u0185"+
		"\7%\2\2\u0181\u0182\7\27\2\2\u0182\u0184\7%\2\2\u0183\u0181\3\2\2\2\u0184"+
		"\u0187\3\2\2\2\u0185\u0183\3\2\2\2\u0185\u0186\3\2\2\2\u0186\u0188\3\2"+
		"\2\2\u0187\u0185\3\2\2\2\u0188\u01ed\7\26\2\2\u0189\u018a\7\b\2\2\u018a"+
		"\u018b\7\t\2\2\u018b\u018c\7%\2\2\u018c\u018d\7\23\2\2\u018d\u01ed\7%"+
		"\2\2\u018e\u018f\7\b\2\2\u018f\u0190\7\t\2\2\u0190\u0191\7\25\2\2\u0191"+
		"\u0196\7%\2\2\u0192\u0193\7\27\2\2\u0193\u0195\7%\2\2\u0194\u0192\3\2"+
		"\2\2\u0195\u0198\3\2\2\2\u0196\u0194\3\2\2\2\u0196\u0197\3\2\2\2\u0197"+
		"\u0199\3\2\2\2\u0198\u0196\3\2\2\2\u0199\u01ed\7\26\2\2\u019a\u019b\7"+
		"\n\2\2\u019b\u019c\7\30\2\2\u019c\u01a1\5\f\7\2\u019d\u019e\7\27\2\2\u019e"+
		"\u01a0\5\f\7\2\u019f\u019d\3\2\2\2\u01a0\u01a3\3\2\2\2\u01a1\u019f\3\2"+
		"\2\2\u01a1\u01a2\3\2\2\2\u01a2\u01a4\3\2\2\2\u01a3\u01a1\3\2\2\2\u01a4"+
		"\u01a5\7\31\2\2\u01a5\u01a6\7\t\2\2\u01a6\u01a7\7%\2\2\u01a7\u01a8\7\23"+
		"\2\2\u01a8\u01a9\7%\2\2\u01a9\u01ed\3\2\2\2\u01aa\u01ab\7\n\2\2\u01ab"+
		"\u01ac\7\30\2\2\u01ac\u01b1\5\f\7\2\u01ad\u01ae\7\27\2\2\u01ae\u01b0\5"+
		"\f\7\2\u01af\u01ad\3\2\2\2\u01b0\u01b3\3\2\2\2\u01b1\u01af\3\2\2\2\u01b1"+
		"\u01b2\3\2\2\2\u01b2\u01b4\3\2\2\2\u01b3\u01b1\3\2\2\2\u01b4\u01b5\7\31"+
		"\2\2\u01b5\u01b6\7\t\2\2\u01b6\u01b7\7\25\2\2\u01b7\u01bc\7%\2\2\u01b8"+
		"\u01b9\7\27\2\2\u01b9\u01bb\7%\2\2\u01ba\u01b8\3\2\2\2\u01bb\u01be\3\2"+
		"\2\2\u01bc\u01ba\3\2\2\2\u01bc\u01bd\3\2\2\2\u01bd\u01bf\3\2\2\2\u01be"+
		"\u01bc\3\2\2\2\u01bf\u01c0\7\26\2\2\u01c0\u01ed\3\2\2\2\u01c1\u01c2\7"+
		"\n\2\2\u01c2\u01c3\7\30\2\2\u01c3\u01c8\5\f\7\2\u01c4\u01c5\7\27\2\2\u01c5"+
		"\u01c7\5\f\7\2\u01c6\u01c4\3\2\2\2\u01c7\u01ca\3\2\2\2\u01c8\u01c6\3\2"+
		"\2\2\u01c8\u01c9\3\2\2\2\u01c9\u01cb\3\2\2\2\u01ca\u01c8\3\2\2\2\u01cb"+
		"\u01cc\7\31\2\2\u01cc\u01cd\7\t\2\2\u01cd\u01ce\7\b\2\2\u01ce\u01cf\7"+
		"\t\2\2\u01cf\u01d0\7%\2\2\u01d0\u01d1\7\23\2\2\u01d1\u01d2\7%\2\2\u01d2"+
		"\u01ed\3\2\2\2\u01d3\u01d4\7\n\2\2\u01d4\u01d5\7\30\2\2\u01d5\u01da\5"+
		"\f\7\2\u01d6\u01d7\7\27\2\2\u01d7\u01d9\5\f\7\2\u01d8\u01d6\3\2\2\2\u01d9"+
		"\u01dc\3\2\2\2\u01da\u01d8\3\2\2\2\u01da\u01db\3\2\2\2\u01db\u01dd\3\2"+
		"\2\2\u01dc\u01da\3\2\2\2\u01dd\u01de\7\31\2\2\u01de\u01df\7\t\2\2\u01df"+
		"\u01e0\7\b\2\2\u01e0\u01e1\7\t\2\2\u01e1\u01e2\7\25\2\2\u01e2\u01e7\7"+
		"%\2\2\u01e3\u01e4\7\27\2\2\u01e4\u01e6\7%\2\2\u01e5\u01e3\3\2\2\2\u01e6"+
		"\u01e9\3\2\2\2\u01e7\u01e5\3\2\2\2\u01e7\u01e8\3\2\2\2\u01e8\u01ea\3\2"+
		"\2\2\u01e9\u01e7\3\2\2\2\u01ea\u01eb\7\26\2\2\u01eb\u01ed\3\2\2\2\u01ec"+
		"\u017b\3\2\2\2\u01ec\u017c\3\2\2\2\u01ec\u017f\3\2\2\2\u01ec\u0189\3\2"+
		"\2\2\u01ec\u018e\3\2\2\2\u01ec\u019a\3\2\2\2\u01ec\u01aa\3\2\2\2\u01ec"+
		"\u01c1\3\2\2\2\u01ec\u01d3\3\2\2\2\u01ed)\3\2\2\2\u01ee\u0205\5\b\5\2"+
		"\u01ef\u01f0\7\13\2\2\u01f0\u01f1\7\b\2\2\u01f1\u01f2\7\t\2\2\u01f2\u0205"+
		"\7\6\2\2\u01f3\u01f4\7\n\2\2\u01f4\u01f5\7\30\2\2\u01f5\u01fa\5\f\7\2"+
		"\u01f6\u01f7\7\27\2\2\u01f7\u01f9\5\f\7\2\u01f8\u01f6\3\2\2\2\u01f9\u01fc"+
		"\3\2\2\2\u01fa\u01f8\3\2\2\2\u01fa\u01fb\3\2\2\2\u01fb\u01fd\3\2\2\2\u01fc"+
		"\u01fa\3\2\2\2\u01fd\u01fe\7\31\2\2\u01fe\u01ff\7\t\2\2\u01ff\u0200\7"+
		"\13\2\2\u0200\u0201\7\b\2\2\u0201\u0202\7\t\2\2\u0202\u0203\7\6\2\2\u0203"+
		"\u0205\3\2\2\2\u0204\u01ee\3\2\2\2\u0204\u01ef\3\2\2\2\u0204\u01f3\3\2"+
		"\2\2\u0205+\3\2\2\2*\60\66<BS^jw\u0084\u0099\u00af\u00b4\u00bc\u00ca\u00d5"+
		"\u00e2\u00e5\u00ee\u00fb\u0103\u0115\u0126\u013e\u0147\u0155\u015a\u0162"+
		"\u016c\u0179\u0185\u0196\u01a1\u01b1\u01bc\u01c8\u01da\u01e7\u01ec\u01fa"+
		"\u0204";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}