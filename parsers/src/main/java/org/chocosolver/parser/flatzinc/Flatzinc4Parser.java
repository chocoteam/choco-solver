/*
 * This file is part of choco-parsers, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
// Generated from Flatzinc4Parser.g4 by ANTLR 4.9.3
package org.chocosolver.parser.flatzinc;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ATNDeserializer;
import org.antlr.v4.runtime.atn.ParserATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.chocosolver.parser.flatzinc.ast.*;
import org.chocosolver.parser.flatzinc.ast.declaration.*;
import org.chocosolver.parser.flatzinc.ast.expression.*;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.ResolutionPolicy;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class Flatzinc4Parser extends Parser {
	static { RuntimeMetaData.checkVersion("4.9.3", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		BOOL=1, TRUE=2, FALSE=3, INT=4, FLOAT=5, SET=6, OF=7, ARRAY=8, VAR=9, 
		PAR=10, PREDICATE=11, CONSTRAINT=12, SOLVE=13, SATISFY=14, MINIMIZE=15, 
		MAXIMIZE=16, DD=17, DO=18, LB=19, RB=20, CM=21, LS=22, RS=23, EQ=24, PL=25, 
		MN=26, SC=27, CL=28, DC=29, LP=30, RP=31, IDENTIFIER=32, COMMENT=33, WS=34, 
		INT_CONST=35, STRING=36, CHAR=37;
	public static final int
		RULE_flatzinc_model = 0, RULE_par_type = 1, RULE_par_type_u = 2, RULE_var_type = 3, 
		RULE_var_type_u = 4, RULE_index_set = 5, RULE_expr = 6, RULE_id_expr = 7, 
		RULE_param_decl = 8, RULE_var_decl = 9, RULE_constraint = 10, RULE_solve_goal = 11, 
		RULE_resolution = 12, RULE_annotations = 13, RULE_annotation = 14, RULE_bool_const = 15, 
		RULE_pred_decl = 16, RULE_pred_param = 17, RULE_pred_param_type = 18, 
		RULE_par_pred_param_type = 19, RULE_var_pred_param_type = 20;
	private static String[] makeRuleNames() {
		return new String[] {
			"flatzinc_model", "par_type", "par_type_u", "var_type", "var_type_u", 
			"index_set", "expr", "id_expr", "param_decl", "var_decl", "constraint", 
			"solve_goal", "resolution", "annotations", "annotation", "bool_const", 
			"pred_decl", "pred_param", "pred_param_type", "par_pred_param_type", 
			"var_pred_param_type"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'bool'", "'true'", "'false'", "'int'", "'float'", "'set'", "'of'", 
			"'array'", "'var'", "'par'", "'predicate'", "'constraint'", "'solve'", 
			"'satisfy'", "'minimize'", "'maximize'", "'..'", "'.'", "'{'", "'}'", 
			"','", "'['", "']'", "'='", "'+'", "'-'", "';'", "':'", "'::'", "'('", 
			"')'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "BOOL", "TRUE", "FALSE", "INT", "FLOAT", "SET", "OF", "ARRAY", 
			"VAR", "PAR", "PREDICATE", "CONSTRAINT", "SOLVE", "SATISFY", "MINIMIZE", 
			"MAXIMIZE", "DD", "DO", "LB", "RB", "CM", "LS", "RS", "EQ", "PL", "MN", 
			"SC", "CL", "DC", "LP", "RP", "IDENTIFIER", "COMMENT", "WS", "INT_CONST", 
			"STRING", "CHAR"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "Flatzinc4Parser.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }



	public Datas datas;

	// the model
	public Model mModel;

	public Flatzinc4Parser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	public static class Flatzinc_modelContext extends ParserRuleContext {
		public Model aModel;
		public Datas datas;
		public Solve_goalContext solve_goal() {
			return getRuleContext(Solve_goalContext.class,0);
		}
		public List<Pred_declContext> pred_decl() {
			return getRuleContexts(Pred_declContext.class);
		}
		public Pred_declContext pred_decl(int i) {
			return getRuleContext(Pred_declContext.class,i);
		}
		public List<Param_declContext> param_decl() {
			return getRuleContexts(Param_declContext.class);
		}
		public Param_declContext param_decl(int i) {
			return getRuleContext(Param_declContext.class,i);
		}
		public List<Var_declContext> var_decl() {
			return getRuleContexts(Var_declContext.class);
		}
		public Var_declContext var_decl(int i) {
			return getRuleContext(Var_declContext.class,i);
		}
		public List<ConstraintContext> constraint() {
			return getRuleContexts(ConstraintContext.class);
		}
		public ConstraintContext constraint(int i) {
			return getRuleContext(ConstraintContext.class,i);
		}
		public Flatzinc_modelContext(ParserRuleContext parent, int invokingState) { super(parent, invokingState); }
		public Flatzinc_modelContext(ParserRuleContext parent, int invokingState, Model aModel, Datas datas) {
			super(parent, invokingState);
			this.aModel = aModel;
			this.datas = datas;
		}
		@Override public int getRuleIndex() { return RULE_flatzinc_model; }
	}

	public final Flatzinc_modelContext flatzinc_model(Model aModel,Datas datas) throws RecognitionException {
		Flatzinc_modelContext _localctx = new Flatzinc_modelContext(_ctx, getState(), aModel, datas);
		enterRule(_localctx, 0, RULE_flatzinc_model);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{

			    this.mModel = aModel;
			    this.datas = datas;
			    
			setState(46);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==PREDICATE) {
				{
				{
				setState(43);
				pred_decl();
				}
				}
				setState(48);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(53);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << BOOL) | (1L << INT) | (1L << FLOAT) | (1L << SET) | (1L << ARRAY) | (1L << VAR))) != 0)) {
				{
				setState(51);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,1,_ctx) ) {
				case 1:
					{
					setState(49);
					param_decl();
					}
					break;
				case 2:
					{
					setState(50);
					var_decl();
					}
					break;
				}
				}
				setState(55);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(59);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==CONSTRAINT) {
				{
				{
				setState(56);
				constraint();
				}
				}
				setState(61);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(62);
			solve_goal();
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
		public TerminalNode LS() { return getToken(Flatzinc4Parser.LS, 0); }
		public TerminalNode RS() { return getToken(Flatzinc4Parser.RS, 0); }
		public TerminalNode OF() { return getToken(Flatzinc4Parser.OF, 0); }
		public List<Index_setContext> index_set() {
			return getRuleContexts(Index_setContext.class);
		}
		public Index_setContext index_set(int i) {
			return getRuleContext(Index_setContext.class,i);
		}
		public Par_type_uContext par_type_u() {
			return getRuleContext(Par_type_uContext.class,0);
		}
		public List<TerminalNode> CM() { return getTokens(Flatzinc4Parser.CM); }
		public TerminalNode CM(int i) {
			return getToken(Flatzinc4Parser.CM, i);
		}
		public Par_typeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_par_type; }
	}

	public final Par_typeContext par_type() throws RecognitionException {
		Par_typeContext _localctx = new Par_typeContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_par_type);
		int _la;
		try {
			setState(86);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ARRAY:
				enterOuterAlt(_localctx, 1);
				{

				        List<Declaration> decls = new ArrayList();
				    
				setState(65);
				match(ARRAY);
				setState(66);
				match(LS);
				setState(67);
				((Par_typeContext)_localctx).d = index_set();
				decls.add(((Par_typeContext)_localctx).d.decl);
				setState(75);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==CM) {
					{
					{
					setState(69);
					match(CM);
					setState(70);
					((Par_typeContext)_localctx).d = index_set();
					decls.add(((Par_typeContext)_localctx).d.decl);
					}
					}
					setState(77);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(78);
				match(RS);
				setState(79);
				match(OF);
				setState(80);
				((Par_typeContext)_localctx).p = par_type_u();

				    ((Par_typeContext)_localctx).decl =  new DArray(decls,((Par_typeContext)_localctx).p.decl);
				    
				}
				break;
			case BOOL:
			case INT:
			case FLOAT:
			case SET:
				enterOuterAlt(_localctx, 2);
				{
				setState(83);
				((Par_typeContext)_localctx).p = par_type_u();

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
		public TerminalNode FLOAT() { return getToken(Flatzinc4Parser.FLOAT, 0); }
		public TerminalNode SET() { return getToken(Flatzinc4Parser.SET, 0); }
		public TerminalNode OF() { return getToken(Flatzinc4Parser.OF, 0); }
		public TerminalNode INT() { return getToken(Flatzinc4Parser.INT, 0); }
		public Par_type_uContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_par_type_u; }
	}

	public final Par_type_uContext par_type_u() throws RecognitionException {
		Par_type_uContext _localctx = new Par_type_uContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_par_type_u);
		try {
			setState(98);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case BOOL:
				enterOuterAlt(_localctx, 1);
				{
				setState(88);
				match(BOOL);

				    ((Par_type_uContext)_localctx).decl = DBool.me;
				    
				}
				break;
			case FLOAT:
				enterOuterAlt(_localctx, 2);
				{
				setState(90);
				match(FLOAT);

				    ((Par_type_uContext)_localctx).decl = DFloat.me;
				    
				}
				break;
			case SET:
				enterOuterAlt(_localctx, 3);
				{
				setState(92);
				match(SET);
				setState(93);
				match(OF);
				setState(94);
				match(INT);

				    ((Par_type_uContext)_localctx).decl = DSetOfInt.me;
				    
				}
				break;
			case INT:
				enterOuterAlt(_localctx, 4);
				{
				setState(96);
				match(INT);

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
		public TerminalNode LS() { return getToken(Flatzinc4Parser.LS, 0); }
		public TerminalNode RS() { return getToken(Flatzinc4Parser.RS, 0); }
		public TerminalNode OF() { return getToken(Flatzinc4Parser.OF, 0); }
		public TerminalNode VAR() { return getToken(Flatzinc4Parser.VAR, 0); }
		public List<Index_setContext> index_set() {
			return getRuleContexts(Index_setContext.class);
		}
		public Index_setContext index_set(int i) {
			return getRuleContext(Index_setContext.class,i);
		}
		public Var_type_uContext var_type_u() {
			return getRuleContext(Var_type_uContext.class,0);
		}
		public List<TerminalNode> CM() { return getTokens(Flatzinc4Parser.CM); }
		public TerminalNode CM(int i) {
			return getToken(Flatzinc4Parser.CM, i);
		}
		public Var_typeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_var_type; }
	}

	public final Var_typeContext var_type() throws RecognitionException {
		Var_typeContext _localctx = new Var_typeContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_var_type);
		int _la;
		try {
			setState(124);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ARRAY:
				enterOuterAlt(_localctx, 1);
				{

				    List<Declaration> decls = new ArrayList();
				    
				setState(101);
				match(ARRAY);
				setState(102);
				match(LS);
				setState(103);
				((Var_typeContext)_localctx).d = index_set();
				decls.add(((Var_typeContext)_localctx).d.decl);
				setState(111);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==CM) {
					{
					{
					setState(105);
					match(CM);
					setState(106);
					((Var_typeContext)_localctx).d = index_set();
					decls.add(((Var_typeContext)_localctx).d.decl);
					}
					}
					setState(113);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(114);
				match(RS);
				setState(115);
				match(OF);
				setState(116);
				match(VAR);
				setState(117);
				((Var_typeContext)_localctx).vt = var_type_u();

				    ((Var_typeContext)_localctx).decl =  new DArray(decls, ((Var_typeContext)_localctx).vt.decl);
				    
				}
				break;
			case VAR:
				enterOuterAlt(_localctx, 2);
				{
				setState(120);
				match(VAR);
				setState(121);
				((Var_typeContext)_localctx).vt = var_type_u();

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
		public TerminalNode BOOL() { return getToken(Flatzinc4Parser.BOOL, 0); }
		public TerminalNode FLOAT() { return getToken(Flatzinc4Parser.FLOAT, 0); }
		public TerminalNode INT() { return getToken(Flatzinc4Parser.INT, 0); }
		public TerminalNode DD() { return getToken(Flatzinc4Parser.DD, 0); }
		public List<TerminalNode> INT_CONST() { return getTokens(Flatzinc4Parser.INT_CONST); }
		public TerminalNode INT_CONST(int i) {
			return getToken(Flatzinc4Parser.INT_CONST, i);
		}
		public TerminalNode LB() { return getToken(Flatzinc4Parser.LB, 0); }
		public TerminalNode RB() { return getToken(Flatzinc4Parser.RB, 0); }
		public List<TerminalNode> CM() { return getTokens(Flatzinc4Parser.CM); }
		public TerminalNode CM(int i) {
			return getToken(Flatzinc4Parser.CM, i);
		}
		public TerminalNode SET() { return getToken(Flatzinc4Parser.SET, 0); }
		public TerminalNode OF() { return getToken(Flatzinc4Parser.OF, 0); }
		public Var_type_uContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_var_type_u; }
	}

	public final Var_type_uContext var_type_u() throws RecognitionException {
		Var_type_uContext _localctx = new Var_type_uContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_var_type_u);
		int _la;
		try {
			setState(172);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,11,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(126);
				match(BOOL);

				    ((Var_type_uContext)_localctx).decl =  DBool.me;
				    
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(128);
				match(FLOAT);

				    ((Var_type_uContext)_localctx).decl =  DFloat.me;
				    
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(130);
				match(INT);

				    ((Var_type_uContext)_localctx).decl =  DInt.me;
				    
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(132);
				((Var_type_uContext)_localctx).i1 = match(INT_CONST);
				setState(133);
				match(DD);
				setState(134);
				((Var_type_uContext)_localctx).i2 = match(INT_CONST);

				     ((Var_type_uContext)_localctx).decl =  new DInt2(EInt.make((((Var_type_uContext)_localctx).i1!=null?((Var_type_uContext)_localctx).i1.getText():null)), EInt.make((((Var_type_uContext)_localctx).i2!=null?((Var_type_uContext)_localctx).i2.getText():null)));
				     
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{

				    ArrayList<EInt> values = new ArrayList();
				    
				setState(137);
				match(LB);
				setState(138);
				((Var_type_uContext)_localctx).i = match(INT_CONST);
				values.add(EInt.make((((Var_type_uContext)_localctx).i!=null?((Var_type_uContext)_localctx).i.getText():null)));
				setState(145);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==CM) {
					{
					{
					setState(140);
					match(CM);
					setState(141);
					((Var_type_uContext)_localctx).i = match(INT_CONST);
					values.add(EInt.make((((Var_type_uContext)_localctx).i!=null?((Var_type_uContext)_localctx).i.getText():null)));
					}
					}
					setState(147);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(148);
				match(RB);

				    ((Var_type_uContext)_localctx).decl =  new DManyInt(values);
				    
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(150);
				match(SET);
				setState(151);
				match(OF);
				setState(152);
				((Var_type_uContext)_localctx).i1 = match(INT_CONST);
				setState(153);
				match(DD);
				setState(154);
				((Var_type_uContext)_localctx).i2 = match(INT_CONST);

				    ((Var_type_uContext)_localctx).decl =  new DSet(new DInt2(EInt.make((((Var_type_uContext)_localctx).i1!=null?((Var_type_uContext)_localctx).i1.getText():null)), EInt.make((((Var_type_uContext)_localctx).i2!=null?((Var_type_uContext)_localctx).i2.getText():null))));
				    
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{

				    ArrayList<EInt> values = new ArrayList();
				    
				setState(157);
				match(SET);
				setState(158);
				match(OF);
				setState(159);
				match(LB);
				setState(160);
				((Var_type_uContext)_localctx).i = match(INT_CONST);
				values.add(EInt.make((((Var_type_uContext)_localctx).i!=null?((Var_type_uContext)_localctx).i.getText():null)));
				setState(167);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==CM) {
					{
					{
					setState(162);
					match(CM);
					setState(163);
					((Var_type_uContext)_localctx).i = match(INT_CONST);
					values.add(EInt.make((((Var_type_uContext)_localctx).i!=null?((Var_type_uContext)_localctx).i.getText():null)));
					}
					}
					setState(169);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(170);
				match(RB);

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
		public List<TerminalNode> INT_CONST() { return getTokens(Flatzinc4Parser.INT_CONST); }
		public TerminalNode INT_CONST(int i) {
			return getToken(Flatzinc4Parser.INT_CONST, i);
		}
		public TerminalNode INT() { return getToken(Flatzinc4Parser.INT, 0); }
		public Index_setContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_index_set; }
	}

	public final Index_setContext index_set() throws RecognitionException {
		Index_setContext _localctx = new Index_setContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_index_set);
		try {
			setState(180);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case INT_CONST:
				enterOuterAlt(_localctx, 1);
				{
				setState(174);
				((Index_setContext)_localctx).i1 = match(INT_CONST);
				setState(175);
				match(DD);
				setState(176);
				((Index_setContext)_localctx).i2 = match(INT_CONST);

				    ((Index_setContext)_localctx).decl =  new DInt2(EInt.make((((Index_setContext)_localctx).i1!=null?((Index_setContext)_localctx).i1.getText():null)), EInt.make((((Index_setContext)_localctx).i2!=null?((Index_setContext)_localctx).i2.getText():null)));
				    
				}
				break;
			case INT:
				enterOuterAlt(_localctx, 2);
				{
				setState(178);
				match(INT);

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
		public TerminalNode LB() { return getToken(Flatzinc4Parser.LB, 0); }
		public TerminalNode RB() { return getToken(Flatzinc4Parser.RB, 0); }
		public List<TerminalNode> INT_CONST() { return getTokens(Flatzinc4Parser.INT_CONST); }
		public TerminalNode INT_CONST(int i) {
			return getToken(Flatzinc4Parser.INT_CONST, i);
		}
		public List<TerminalNode> CM() { return getTokens(Flatzinc4Parser.CM); }
		public TerminalNode CM(int i) {
			return getToken(Flatzinc4Parser.CM, i);
		}
		public Bool_constContext bool_const() {
			return getRuleContext(Bool_constContext.class,0);
		}
		public TerminalNode DD() { return getToken(Flatzinc4Parser.DD, 0); }
		public TerminalNode LS() { return getToken(Flatzinc4Parser.LS, 0); }
		public TerminalNode RS() { return getToken(Flatzinc4Parser.RS, 0); }
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public Id_exprContext id_expr() {
			return getRuleContext(Id_exprContext.class,0);
		}
		public TerminalNode STRING() { return getToken(Flatzinc4Parser.STRING, 0); }
		public ExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expr; }
	}

	public final ExprContext expr() throws RecognitionException {
		ExprContext _localctx = new ExprContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_expr);
		int _la;
		try {
			setState(230);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,17,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(182);
				match(LB);
				setState(183);
				match(RB);

				    ((ExprContext)_localctx).exp =  new ESetList(new ArrayList());
				    
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{

				    ArrayList<EInt> values = new ArrayList();
				    
				setState(186);
				match(LB);
				setState(187);
				((ExprContext)_localctx).i = match(INT_CONST);
				values.add(EInt.make((((ExprContext)_localctx).i!=null?((ExprContext)_localctx).i.getText():null)));
				setState(194);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==CM) {
					{
					{
					setState(189);
					match(CM);
					setState(190);
					((ExprContext)_localctx).i = match(INT_CONST);
					values.add(EInt.make((((ExprContext)_localctx).i!=null?((ExprContext)_localctx).i.getText():null)));
					}
					}
					setState(196);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(197);
				match(RB);

				    ((ExprContext)_localctx).exp =  new ESetList(values);
				    
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(199);
				((ExprContext)_localctx).b = bool_const();

				    ((ExprContext)_localctx).exp = EBool.make(((ExprContext)_localctx).b.value);
				    
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(202);
				((ExprContext)_localctx).i1 = match(INT_CONST);
				setState(205);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==DD) {
					{
					setState(203);
					match(DD);
					setState(204);
					((ExprContext)_localctx).i2 = match(INT_CONST);
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
				    
				setState(209);
				match(LS);
				setState(221);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << TRUE) | (1L << FALSE) | (1L << LB) | (1L << LS) | (1L << IDENTIFIER) | (1L << INT_CONST) | (1L << STRING))) != 0)) {
					{
					setState(210);
					((ExprContext)_localctx).e = expr();
					exps.add(((ExprContext)_localctx).e.exp);
					setState(218);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==CM) {
						{
						{
						setState(212);
						match(CM);
						setState(213);
						((ExprContext)_localctx).e = expr();
						exps.add(((ExprContext)_localctx).e.exp);
						}
						}
						setState(220);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					}
				}

				setState(223);
				match(RS);

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
				setState(225);
				((ExprContext)_localctx).ie = id_expr();

				    ((ExprContext)_localctx).exp =  ((ExprContext)_localctx).ie.exp;
				    
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(228);
				((ExprContext)_localctx).STRING = match(STRING);

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
		public TerminalNode IDENTIFIER() { return getToken(Flatzinc4Parser.IDENTIFIER, 0); }
		public TerminalNode LP() { return getToken(Flatzinc4Parser.LP, 0); }
		public TerminalNode RP() { return getToken(Flatzinc4Parser.RP, 0); }
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public List<TerminalNode> CM() { return getTokens(Flatzinc4Parser.CM); }
		public TerminalNode CM(int i) {
			return getToken(Flatzinc4Parser.CM, i);
		}
		public TerminalNode LS() { return getToken(Flatzinc4Parser.LS, 0); }
		public TerminalNode RS() { return getToken(Flatzinc4Parser.RS, 0); }
		public TerminalNode INT_CONST() { return getToken(Flatzinc4Parser.INT_CONST, 0); }
		public Id_exprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_id_expr; }
	}

	public final Id_exprContext id_expr() throws RecognitionException {
		Id_exprContext _localctx = new Id_exprContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_id_expr);
		int _la;
		try {
			setState(256);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,19,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{

				    ArrayList<Expression> exps = new ArrayList();
				    
				setState(233);
				((Id_exprContext)_localctx).IDENTIFIER = match(IDENTIFIER);
				setState(234);
				match(LP);
				setState(235);
				((Id_exprContext)_localctx).e = expr();
				exps.add(((Id_exprContext)_localctx).e.exp);
				setState(243);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==CM) {
					{
					{
					setState(237);
					match(CM);
					setState(238);
					((Id_exprContext)_localctx).e = expr();
					exps.add(((Id_exprContext)_localctx).e.exp);
					}
					}
					setState(245);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(246);
				match(RP);

				    ((Id_exprContext)_localctx).exp =  new EAnnotation(new EIdentifier(datas, (((Id_exprContext)_localctx).IDENTIFIER!=null?((Id_exprContext)_localctx).IDENTIFIER.getText():null)), exps);
				    
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(249);
				((Id_exprContext)_localctx).IDENTIFIER = match(IDENTIFIER);
				setState(250);
				match(LS);
				setState(251);
				((Id_exprContext)_localctx).i = match(INT_CONST);
				setState(252);
				match(RS);

				    ((Id_exprContext)_localctx).exp =  new EIdArray(datas, (((Id_exprContext)_localctx).IDENTIFIER!=null?((Id_exprContext)_localctx).IDENTIFIER.getText():null), Integer.parseInt((((Id_exprContext)_localctx).i!=null?((Id_exprContext)_localctx).i.getText():null)));
				    
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(254);
				((Id_exprContext)_localctx).IDENTIFIER = match(IDENTIFIER);

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
		public TerminalNode IDENTIFIER() { return getToken(Flatzinc4Parser.IDENTIFIER, 0); }
		public TerminalNode EQ() { return getToken(Flatzinc4Parser.EQ, 0); }
		public TerminalNode SC() { return getToken(Flatzinc4Parser.SC, 0); }
		public Par_typeContext par_type() {
			return getRuleContext(Par_typeContext.class,0);
		}
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public Param_declContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_param_decl; }
	}

	public final Param_declContext param_decl() throws RecognitionException {
		Param_declContext _localctx = new Param_declContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_param_decl);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(258);
			((Param_declContext)_localctx).pt = par_type();
			setState(259);
			match(CL);
			setState(260);
			((Param_declContext)_localctx).IDENTIFIER = match(IDENTIFIER);
			setState(261);
			match(EQ);
			setState(262);
			((Param_declContext)_localctx).e = expr();
			setState(263);
			match(SC);

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
		public TerminalNode IDENTIFIER() { return getToken(Flatzinc4Parser.IDENTIFIER, 0); }
		public TerminalNode SC() { return getToken(Flatzinc4Parser.SC, 0); }
		public Var_typeContext var_type() {
			return getRuleContext(Var_typeContext.class,0);
		}
		public AnnotationsContext annotations() {
			return getRuleContext(AnnotationsContext.class,0);
		}
		public TerminalNode EQ() { return getToken(Flatzinc4Parser.EQ, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public Var_declContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_var_decl; }
	}

	public final Var_declContext var_decl() throws RecognitionException {
		Var_declContext _localctx = new Var_declContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_var_decl);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(266);
			((Var_declContext)_localctx).vt = var_type();
			setState(267);
			match(CL);
			setState(268);
			((Var_declContext)_localctx).IDENTIFIER = match(IDENTIFIER);
			setState(269);
			((Var_declContext)_localctx).anns = annotations();
			setState(272);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==EQ) {
				{
				setState(270);
				((Var_declContext)_localctx).eq = match(EQ);
				setState(271);
				((Var_declContext)_localctx).e = expr();
				}
			}

			setState(274);
			match(SC);

				FVariable.make_variable(datas, ((Var_declContext)_localctx).vt.decl, (((Var_declContext)_localctx).IDENTIFIER!=null?((Var_declContext)_localctx).IDENTIFIER.getText():null), ((Var_declContext)_localctx).anns.anns, ((Var_declContext)_localctx).eq!=null?((Var_declContext)_localctx).e.exp:null, mModel);
			    
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
		public TerminalNode CONSTRAINT() { return getToken(Flatzinc4Parser.CONSTRAINT, 0); }
		public TerminalNode IDENTIFIER() { return getToken(Flatzinc4Parser.IDENTIFIER, 0); }
		public TerminalNode LP() { return getToken(Flatzinc4Parser.LP, 0); }
		public TerminalNode RP() { return getToken(Flatzinc4Parser.RP, 0); }
		public TerminalNode SC() { return getToken(Flatzinc4Parser.SC, 0); }
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public AnnotationsContext annotations() {
			return getRuleContext(AnnotationsContext.class,0);
		}
		public List<TerminalNode> CM() { return getTokens(Flatzinc4Parser.CM); }
		public TerminalNode CM(int i) {
			return getToken(Flatzinc4Parser.CM, i);
		}
		public ConstraintContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_constraint; }
	}

	public final ConstraintContext constraint() throws RecognitionException {
		ConstraintContext _localctx = new ConstraintContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_constraint);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{

			    //  Model aModel, String id, List<Expression> exps, List<EAnnotation> annotations
			    ArrayList<Expression> exps = new ArrayList();
			    
			setState(278);
			match(CONSTRAINT);
			setState(279);
			((ConstraintContext)_localctx).IDENTIFIER = match(IDENTIFIER);
			setState(280);
			match(LP);
			setState(281);
			((ConstraintContext)_localctx).e = expr();
			exps.add(((ConstraintContext)_localctx).e.exp);
			setState(289);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==CM) {
				{
				{
				setState(283);
				match(CM);
				setState(284);
				((ConstraintContext)_localctx).e = expr();
				exps.add(((ConstraintContext)_localctx).e.exp);
				}
				}
				setState(291);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(292);
			match(RP);
			setState(293);
			((ConstraintContext)_localctx).anns = annotations();
			setState(294);
			match(SC);

			    String name = (((ConstraintContext)_localctx).IDENTIFIER!=null?((ConstraintContext)_localctx).IDENTIFIER.getText():null);
			    datas.incCstrCounter(name);
			    FConstraint.valueOf(name).build(mModel, datas, name, exps, ((ConstraintContext)_localctx).anns.anns);
			    
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
		public TerminalNode SC() { return getToken(Flatzinc4Parser.SC, 0); }
		public AnnotationsContext annotations() {
			return getRuleContext(AnnotationsContext.class,0);
		}
		public ResolutionContext resolution() {
			return getRuleContext(ResolutionContext.class,0);
		}
		public Solve_goalContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_solve_goal; }
	}

	public final Solve_goalContext solve_goal() throws RecognitionException {
		Solve_goalContext _localctx = new Solve_goalContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_solve_goal);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(297);
			match(SOLVE);
			setState(298);
			((Solve_goalContext)_localctx).anns = annotations();
			setState(299);
			((Solve_goalContext)_localctx).res = resolution();
			setState(300);
			match(SC);

			    FGoal.define_goal(mModel, ((Solve_goalContext)_localctx).anns.anns,((Solve_goalContext)_localctx).res.rtype,((Solve_goalContext)_localctx).res.exp);
			    
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
		public TerminalNode MINIMIZE() { return getToken(Flatzinc4Parser.MINIMIZE, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TerminalNode MAXIMIZE() { return getToken(Flatzinc4Parser.MAXIMIZE, 0); }
		public TerminalNode SATISFY() { return getToken(Flatzinc4Parser.SATISFY, 0); }
		public ResolutionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_resolution; }
	}

	public final ResolutionContext resolution() throws RecognitionException {
		ResolutionContext _localctx = new ResolutionContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_resolution);
		try {
			setState(313);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case MINIMIZE:
				enterOuterAlt(_localctx, 1);
				{
				setState(303);
				match(MINIMIZE);
				setState(304);
				((ResolutionContext)_localctx).e = expr();

				    ((ResolutionContext)_localctx).rtype = ResolutionPolicy.MINIMIZE;
				    ((ResolutionContext)_localctx).exp = ((ResolutionContext)_localctx).e.exp;
				    
				}
				break;
			case MAXIMIZE:
				enterOuterAlt(_localctx, 2);
				{
				setState(307);
				match(MAXIMIZE);
				setState(308);
				((ResolutionContext)_localctx).e = expr();

				    ((ResolutionContext)_localctx).rtype = ResolutionPolicy.MAXIMIZE;
				    ((ResolutionContext)_localctx).exp = ((ResolutionContext)_localctx).e.exp;
				    
				}
				break;
			case SATISFY:
				enterOuterAlt(_localctx, 3);
				{
				setState(311);
				match(SATISFY);

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
		public List<TerminalNode> DC() { return getTokens(Flatzinc4Parser.DC); }
		public TerminalNode DC(int i) {
			return getToken(Flatzinc4Parser.DC, i);
		}
		public List<AnnotationContext> annotation() {
			return getRuleContexts(AnnotationContext.class);
		}
		public AnnotationContext annotation(int i) {
			return getRuleContext(AnnotationContext.class,i);
		}
		public AnnotationsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_annotations; }
	}

	public final AnnotationsContext annotations() throws RecognitionException {
		AnnotationsContext _localctx = new AnnotationsContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_annotations);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{

			    ((AnnotationsContext)_localctx).anns =  new ArrayList();
			    
			setState(322);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==DC) {
				{
				{
				setState(316);
				match(DC);
				setState(317);
				((AnnotationsContext)_localctx).e = annotation();
				_localctx.anns.add(((AnnotationsContext)_localctx).e.ann);
				}
				}
				setState(324);
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
		public TerminalNode IDENTIFIER() { return getToken(Flatzinc4Parser.IDENTIFIER, 0); }
		public TerminalNode LP() { return getToken(Flatzinc4Parser.LP, 0); }
		public TerminalNode RP() { return getToken(Flatzinc4Parser.RP, 0); }
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public List<TerminalNode> CM() { return getTokens(Flatzinc4Parser.CM); }
		public TerminalNode CM(int i) {
			return getToken(Flatzinc4Parser.CM, i);
		}
		public AnnotationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_annotation; }
	}

	public final AnnotationContext annotation() throws RecognitionException {
		AnnotationContext _localctx = new AnnotationContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_annotation);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{

			    ArrayList<Expression> exps = new ArrayList();
			    
			setState(326);
			((AnnotationContext)_localctx).IDENTIFIER = match(IDENTIFIER);
			setState(341);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LP) {
				{
				setState(327);
				match(LP);
				setState(328);
				((AnnotationContext)_localctx).e = expr();
				exps.add(((AnnotationContext)_localctx).e.exp);
				setState(336);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==CM) {
					{
					{
					setState(330);
					match(CM);
					setState(331);
					((AnnotationContext)_localctx).e = expr();
					exps.add(((AnnotationContext)_localctx).e.exp);
					}
					}
					setState(338);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(339);
				match(RP);
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
	}

	public final Bool_constContext bool_const() throws RecognitionException {
		Bool_constContext _localctx = new Bool_constContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_bool_const);
		try {
			setState(349);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case TRUE:
				enterOuterAlt(_localctx, 1);
				{
				setState(345);
				match(TRUE);
				((Bool_constContext)_localctx).value =  true;
				}
				break;
			case FALSE:
				enterOuterAlt(_localctx, 2);
				{
				setState(347);
				match(FALSE);
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
		public TerminalNode PREDICATE() { return getToken(Flatzinc4Parser.PREDICATE, 0); }
		public TerminalNode IDENTIFIER() { return getToken(Flatzinc4Parser.IDENTIFIER, 0); }
		public TerminalNode LP() { return getToken(Flatzinc4Parser.LP, 0); }
		public List<Pred_paramContext> pred_param() {
			return getRuleContexts(Pred_paramContext.class);
		}
		public Pred_paramContext pred_param(int i) {
			return getRuleContext(Pred_paramContext.class,i);
		}
		public TerminalNode RP() { return getToken(Flatzinc4Parser.RP, 0); }
		public TerminalNode SC() { return getToken(Flatzinc4Parser.SC, 0); }
		public List<TerminalNode> CM() { return getTokens(Flatzinc4Parser.CM); }
		public TerminalNode CM(int i) {
			return getToken(Flatzinc4Parser.CM, i);
		}
		public Pred_declContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_pred_decl; }
	}

	public final Pred_declContext pred_decl() throws RecognitionException {
		Pred_declContext _localctx = new Pred_declContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_pred_decl);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(351);
			match(PREDICATE);
			setState(352);
			((Pred_declContext)_localctx).IDENTIFIER = match(IDENTIFIER);
			setState(353);
			match(LP);
			setState(354);
			pred_param();
			setState(359);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==CM) {
				{
				{
				setState(355);
				match(CM);
				setState(356);
				pred_param();
				}
				}
				setState(361);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(362);
			match(RP);
			setState(363);
			match(SC);

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
		public TerminalNode CL() { return getToken(Flatzinc4Parser.CL, 0); }
		public TerminalNode IDENTIFIER() { return getToken(Flatzinc4Parser.IDENTIFIER, 0); }
		public Pred_paramContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_pred_param; }
	}

	public final Pred_paramContext pred_param() throws RecognitionException {
		Pred_paramContext _localctx = new Pred_paramContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_pred_param);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(366);
			pred_param_type();
			setState(367);
			match(CL);
			setState(368);
			match(IDENTIFIER);
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
		public Par_pred_param_typeContext par_pred_param_type() {
			return getRuleContext(Par_pred_param_typeContext.class,0);
		}
		public Var_pred_param_typeContext var_pred_param_type() {
			return getRuleContext(Var_pred_param_typeContext.class,0);
		}
		public Pred_param_typeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_pred_param_type; }
	}

	public final Pred_param_typeContext pred_param_type() throws RecognitionException {
		Pred_param_typeContext _localctx = new Pred_param_typeContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_pred_param_type);
		try {
			setState(372);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,28,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(370);
				par_pred_param_type();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(371);
				var_pred_param_type();
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
		public Par_typeContext par_type() {
			return getRuleContext(Par_typeContext.class,0);
		}
		public List<TerminalNode> INT_CONST() { return getTokens(Flatzinc4Parser.INT_CONST); }
		public TerminalNode INT_CONST(int i) {
			return getToken(Flatzinc4Parser.INT_CONST, i);
		}
		public TerminalNode DD() { return getToken(Flatzinc4Parser.DD, 0); }
		public TerminalNode LB() { return getToken(Flatzinc4Parser.LB, 0); }
		public TerminalNode RB() { return getToken(Flatzinc4Parser.RB, 0); }
		public List<TerminalNode> CM() { return getTokens(Flatzinc4Parser.CM); }
		public TerminalNode CM(int i) {
			return getToken(Flatzinc4Parser.CM, i);
		}
		public TerminalNode SET() { return getToken(Flatzinc4Parser.SET, 0); }
		public List<TerminalNode> OF() { return getTokens(Flatzinc4Parser.OF); }
		public TerminalNode OF(int i) {
			return getToken(Flatzinc4Parser.OF, i);
		}
		public TerminalNode ARRAY() { return getToken(Flatzinc4Parser.ARRAY, 0); }
		public TerminalNode LS() { return getToken(Flatzinc4Parser.LS, 0); }
		public List<Index_setContext> index_set() {
			return getRuleContexts(Index_setContext.class);
		}
		public Index_setContext index_set(int i) {
			return getRuleContext(Index_setContext.class,i);
		}
		public TerminalNode RS() { return getToken(Flatzinc4Parser.RS, 0); }
		public Par_pred_param_typeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_par_pred_param_type; }
	}

	public final Par_pred_param_typeContext par_pred_param_type() throws RecognitionException {
		Par_pred_param_typeContext _localctx = new Par_pred_param_typeContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_par_pred_param_type);
		int _la;
		try {
			setState(487);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,37,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(374);
				par_type();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(375);
				match(INT_CONST);
				setState(376);
				match(DD);
				setState(377);
				match(INT_CONST);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(378);
				match(LB);
				setState(379);
				match(INT_CONST);
				setState(384);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==CM) {
					{
					{
					setState(380);
					match(CM);
					setState(381);
					match(INT_CONST);
					}
					}
					setState(386);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(387);
				match(RB);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(388);
				match(SET);
				setState(389);
				match(OF);
				setState(390);
				match(INT_CONST);
				setState(391);
				match(DD);
				setState(392);
				match(INT_CONST);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(393);
				match(SET);
				setState(394);
				match(OF);
				setState(395);
				match(LB);
				setState(396);
				match(INT_CONST);
				setState(401);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==CM) {
					{
					{
					setState(397);
					match(CM);
					setState(398);
					match(INT_CONST);
					}
					}
					setState(403);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(404);
				match(RB);
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(405);
				match(ARRAY);
				setState(406);
				match(LS);
				setState(407);
				index_set();
				setState(412);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==CM) {
					{
					{
					setState(408);
					match(CM);
					setState(409);
					index_set();
					}
					}
					setState(414);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(415);
				match(RS);
				setState(416);
				match(OF);
				setState(417);
				match(INT_CONST);
				setState(418);
				match(DD);
				setState(419);
				match(INT_CONST);
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(421);
				match(ARRAY);
				setState(422);
				match(LS);
				setState(423);
				index_set();
				setState(428);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==CM) {
					{
					{
					setState(424);
					match(CM);
					setState(425);
					index_set();
					}
					}
					setState(430);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(431);
				match(RS);
				setState(432);
				match(OF);
				setState(433);
				match(LB);
				setState(434);
				match(INT_CONST);
				setState(439);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==CM) {
					{
					{
					setState(435);
					match(CM);
					setState(436);
					match(INT_CONST);
					}
					}
					setState(441);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(442);
				match(RB);
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(444);
				match(ARRAY);
				setState(445);
				match(LS);
				setState(446);
				index_set();
				setState(451);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==CM) {
					{
					{
					setState(447);
					match(CM);
					setState(448);
					index_set();
					}
					}
					setState(453);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(454);
				match(RS);
				setState(455);
				match(OF);
				setState(456);
				match(SET);
				setState(457);
				match(OF);
				setState(458);
				match(INT_CONST);
				setState(459);
				match(DD);
				setState(460);
				match(INT_CONST);
				}
				break;
			case 9:
				enterOuterAlt(_localctx, 9);
				{
				setState(462);
				match(ARRAY);
				setState(463);
				match(LS);
				setState(464);
				index_set();
				setState(469);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==CM) {
					{
					{
					setState(465);
					match(CM);
					setState(466);
					index_set();
					}
					}
					setState(471);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(472);
				match(RS);
				setState(473);
				match(OF);
				setState(474);
				match(SET);
				setState(475);
				match(OF);
				setState(476);
				match(LB);
				setState(477);
				match(INT_CONST);
				setState(482);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==CM) {
					{
					{
					setState(478);
					match(CM);
					setState(479);
					match(INT_CONST);
					}
					}
					setState(484);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(485);
				match(RB);
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
		public Var_typeContext var_type() {
			return getRuleContext(Var_typeContext.class,0);
		}
		public TerminalNode VAR() { return getToken(Flatzinc4Parser.VAR, 0); }
		public TerminalNode SET() { return getToken(Flatzinc4Parser.SET, 0); }
		public List<TerminalNode> OF() { return getTokens(Flatzinc4Parser.OF); }
		public TerminalNode OF(int i) {
			return getToken(Flatzinc4Parser.OF, i);
		}
		public TerminalNode INT() { return getToken(Flatzinc4Parser.INT, 0); }
		public TerminalNode ARRAY() { return getToken(Flatzinc4Parser.ARRAY, 0); }
		public TerminalNode LS() { return getToken(Flatzinc4Parser.LS, 0); }
		public List<Index_setContext> index_set() {
			return getRuleContexts(Index_setContext.class);
		}
		public Index_setContext index_set(int i) {
			return getRuleContext(Index_setContext.class,i);
		}
		public TerminalNode RS() { return getToken(Flatzinc4Parser.RS, 0); }
		public List<TerminalNode> CM() { return getTokens(Flatzinc4Parser.CM); }
		public TerminalNode CM(int i) {
			return getToken(Flatzinc4Parser.CM, i);
		}
		public Var_pred_param_typeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_var_pred_param_type; }
	}

	public final Var_pred_param_typeContext var_pred_param_type() throws RecognitionException {
		Var_pred_param_typeContext _localctx = new Var_pred_param_typeContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_var_pred_param_type);
		int _la;
		try {
			setState(511);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,39,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(489);
				var_type();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(490);
				match(VAR);
				setState(491);
				match(SET);
				setState(492);
				match(OF);
				setState(493);
				match(INT);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(494);
				match(ARRAY);
				setState(495);
				match(LS);
				setState(496);
				index_set();
				setState(501);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==CM) {
					{
					{
					setState(497);
					match(CM);
					setState(498);
					index_set();
					}
					}
					setState(503);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(504);
				match(RS);
				setState(505);
				match(OF);
				setState(506);
				match(VAR);
				setState(507);
				match(SET);
				setState(508);
				match(OF);
				setState(509);
				match(INT);
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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\'\u0204\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t"+
		"\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\3\2\3\2\7\2/\n\2\f\2\16\2\62"+
		"\13\2\3\2\3\2\7\2\66\n\2\f\2\16\29\13\2\3\2\7\2<\n\2\f\2\16\2?\13\2\3"+
		"\2\3\2\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\7\3L\n\3\f\3\16\3O\13\3\3\3"+
		"\3\3\3\3\3\3\3\3\3\3\3\3\3\3\5\3Y\n\3\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4"+
		"\3\4\3\4\5\4e\n\4\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\7\5p\n\5\f\5\16"+
		"\5s\13\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\5\5\177\n\5\3\6\3\6\3"+
		"\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\7\6\u0092\n"+
		"\6\f\6\16\6\u0095\13\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6"+
		"\3\6\3\6\3\6\3\6\3\6\7\6\u00a8\n\6\f\6\16\6\u00ab\13\6\3\6\3\6\5\6\u00af"+
		"\n\6\3\7\3\7\3\7\3\7\3\7\3\7\5\7\u00b7\n\7\3\b\3\b\3\b\3\b\3\b\3\b\3\b"+
		"\3\b\3\b\3\b\7\b\u00c3\n\b\f\b\16\b\u00c6\13\b\3\b\3\b\3\b\3\b\3\b\3\b"+
		"\3\b\3\b\5\b\u00d0\n\b\3\b\3\b\3\b\3\b\3\b\3\b\3\b\3\b\3\b\7\b\u00db\n"+
		"\b\f\b\16\b\u00de\13\b\5\b\u00e0\n\b\3\b\3\b\3\b\3\b\3\b\3\b\3\b\5\b\u00e9"+
		"\n\b\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\7\t\u00f4\n\t\f\t\16\t\u00f7"+
		"\13\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\5\t\u0103\n\t\3\n\3\n\3"+
		"\n\3\n\3\n\3\n\3\n\3\n\3\13\3\13\3\13\3\13\3\13\3\13\5\13\u0113\n\13\3"+
		"\13\3\13\3\13\3\f\3\f\3\f\3\f\3\f\3\f\3\f\3\f\3\f\3\f\7\f\u0122\n\f\f"+
		"\f\16\f\u0125\13\f\3\f\3\f\3\f\3\f\3\f\3\r\3\r\3\r\3\r\3\r\3\r\3\16\3"+
		"\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\5\16\u013c\n\16\3\17\3\17"+
		"\3\17\3\17\3\17\7\17\u0143\n\17\f\17\16\17\u0146\13\17\3\20\3\20\3\20"+
		"\3\20\3\20\3\20\3\20\3\20\3\20\7\20\u0151\n\20\f\20\16\20\u0154\13\20"+
		"\3\20\3\20\5\20\u0158\n\20\3\20\3\20\3\21\3\21\3\21\3\21\5\21\u0160\n"+
		"\21\3\22\3\22\3\22\3\22\3\22\3\22\7\22\u0168\n\22\f\22\16\22\u016b\13"+
		"\22\3\22\3\22\3\22\3\22\3\23\3\23\3\23\3\23\3\24\3\24\5\24\u0177\n\24"+
		"\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\7\25\u0181\n\25\f\25\16\25\u0184"+
		"\13\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\7\25"+
		"\u0192\n\25\f\25\16\25\u0195\13\25\3\25\3\25\3\25\3\25\3\25\3\25\7\25"+
		"\u019d\n\25\f\25\16\25\u01a0\13\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25"+
		"\3\25\3\25\3\25\3\25\7\25\u01ad\n\25\f\25\16\25\u01b0\13\25\3\25\3\25"+
		"\3\25\3\25\3\25\3\25\7\25\u01b8\n\25\f\25\16\25\u01bb\13\25\3\25\3\25"+
		"\3\25\3\25\3\25\3\25\3\25\7\25\u01c4\n\25\f\25\16\25\u01c7\13\25\3\25"+
		"\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\7\25\u01d6"+
		"\n\25\f\25\16\25\u01d9\13\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\7"+
		"\25\u01e3\n\25\f\25\16\25\u01e6\13\25\3\25\3\25\5\25\u01ea\n\25\3\26\3"+
		"\26\3\26\3\26\3\26\3\26\3\26\3\26\3\26\3\26\7\26\u01f6\n\26\f\26\16\26"+
		"\u01f9\13\26\3\26\3\26\3\26\3\26\3\26\3\26\3\26\5\26\u0202\n\26\3\26\2"+
		"\2\27\2\4\6\b\n\f\16\20\22\24\26\30\32\34\36 \"$&(*\2\2\2\u022c\2,\3\2"+
		"\2\2\4X\3\2\2\2\6d\3\2\2\2\b~\3\2\2\2\n\u00ae\3\2\2\2\f\u00b6\3\2\2\2"+
		"\16\u00e8\3\2\2\2\20\u0102\3\2\2\2\22\u0104\3\2\2\2\24\u010c\3\2\2\2\26"+
		"\u0117\3\2\2\2\30\u012b\3\2\2\2\32\u013b\3\2\2\2\34\u013d\3\2\2\2\36\u0147"+
		"\3\2\2\2 \u015f\3\2\2\2\"\u0161\3\2\2\2$\u0170\3\2\2\2&\u0176\3\2\2\2"+
		"(\u01e9\3\2\2\2*\u0201\3\2\2\2,\60\b\2\1\2-/\5\"\22\2.-\3\2\2\2/\62\3"+
		"\2\2\2\60.\3\2\2\2\60\61\3\2\2\2\61\67\3\2\2\2\62\60\3\2\2\2\63\66\5\22"+
		"\n\2\64\66\5\24\13\2\65\63\3\2\2\2\65\64\3\2\2\2\669\3\2\2\2\67\65\3\2"+
		"\2\2\678\3\2\2\28=\3\2\2\29\67\3\2\2\2:<\5\26\f\2;:\3\2\2\2<?\3\2\2\2"+
		"=;\3\2\2\2=>\3\2\2\2>@\3\2\2\2?=\3\2\2\2@A\5\30\r\2A\3\3\2\2\2BC\b\3\1"+
		"\2CD\7\n\2\2DE\7\30\2\2EF\5\f\7\2FM\b\3\1\2GH\7\27\2\2HI\5\f\7\2IJ\b\3"+
		"\1\2JL\3\2\2\2KG\3\2\2\2LO\3\2\2\2MK\3\2\2\2MN\3\2\2\2NP\3\2\2\2OM\3\2"+
		"\2\2PQ\7\31\2\2QR\7\t\2\2RS\5\6\4\2ST\b\3\1\2TY\3\2\2\2UV\5\6\4\2VW\b"+
		"\3\1\2WY\3\2\2\2XB\3\2\2\2XU\3\2\2\2Y\5\3\2\2\2Z[\7\3\2\2[e\b\4\1\2\\"+
		"]\7\7\2\2]e\b\4\1\2^_\7\b\2\2_`\7\t\2\2`a\7\6\2\2ae\b\4\1\2bc\7\6\2\2"+
		"ce\b\4\1\2dZ\3\2\2\2d\\\3\2\2\2d^\3\2\2\2db\3\2\2\2e\7\3\2\2\2fg\b\5\1"+
		"\2gh\7\n\2\2hi\7\30\2\2ij\5\f\7\2jq\b\5\1\2kl\7\27\2\2lm\5\f\7\2mn\b\5"+
		"\1\2np\3\2\2\2ok\3\2\2\2ps\3\2\2\2qo\3\2\2\2qr\3\2\2\2rt\3\2\2\2sq\3\2"+
		"\2\2tu\7\31\2\2uv\7\t\2\2vw\7\13\2\2wx\5\n\6\2xy\b\5\1\2y\177\3\2\2\2"+
		"z{\7\13\2\2{|\5\n\6\2|}\b\5\1\2}\177\3\2\2\2~f\3\2\2\2~z\3\2\2\2\177\t"+
		"\3\2\2\2\u0080\u0081\7\3\2\2\u0081\u00af\b\6\1\2\u0082\u0083\7\7\2\2\u0083"+
		"\u00af\b\6\1\2\u0084\u0085\7\6\2\2\u0085\u00af\b\6\1\2\u0086\u0087\7%"+
		"\2\2\u0087\u0088\7\23\2\2\u0088\u0089\7%\2\2\u0089\u00af\b\6\1\2\u008a"+
		"\u008b\b\6\1\2\u008b\u008c\7\25\2\2\u008c\u008d\7%\2\2\u008d\u0093\b\6"+
		"\1\2\u008e\u008f\7\27\2\2\u008f\u0090\7%\2\2\u0090\u0092\b\6\1\2\u0091"+
		"\u008e\3\2\2\2\u0092\u0095\3\2\2\2\u0093\u0091\3\2\2\2\u0093\u0094\3\2"+
		"\2\2\u0094\u0096\3\2\2\2\u0095\u0093\3\2\2\2\u0096\u0097\7\26\2\2\u0097"+
		"\u00af\b\6\1\2\u0098\u0099\7\b\2\2\u0099\u009a\7\t\2\2\u009a\u009b\7%"+
		"\2\2\u009b\u009c\7\23\2\2\u009c\u009d\7%\2\2\u009d\u00af\b\6\1\2\u009e"+
		"\u009f\b\6\1\2\u009f\u00a0\7\b\2\2\u00a0\u00a1\7\t\2\2\u00a1\u00a2\7\25"+
		"\2\2\u00a2\u00a3\7%\2\2\u00a3\u00a9\b\6\1\2\u00a4\u00a5\7\27\2\2\u00a5"+
		"\u00a6\7%\2\2\u00a6\u00a8\b\6\1\2\u00a7\u00a4\3\2\2\2\u00a8\u00ab\3\2"+
		"\2\2\u00a9\u00a7\3\2\2\2\u00a9\u00aa\3\2\2\2\u00aa\u00ac\3\2\2\2\u00ab"+
		"\u00a9\3\2\2\2\u00ac\u00ad\7\26\2\2\u00ad\u00af\b\6\1\2\u00ae\u0080\3"+
		"\2\2\2\u00ae\u0082\3\2\2\2\u00ae\u0084\3\2\2\2\u00ae\u0086\3\2\2\2\u00ae"+
		"\u008a\3\2\2\2\u00ae\u0098\3\2\2\2\u00ae\u009e\3\2\2\2\u00af\13\3\2\2"+
		"\2\u00b0\u00b1\7%\2\2\u00b1\u00b2\7\23\2\2\u00b2\u00b3\7%\2\2\u00b3\u00b7"+
		"\b\7\1\2\u00b4\u00b5\7\6\2\2\u00b5\u00b7\b\7\1\2\u00b6\u00b0\3\2\2\2\u00b6"+
		"\u00b4\3\2\2\2\u00b7\r\3\2\2\2\u00b8\u00b9\7\25\2\2\u00b9\u00ba\7\26\2"+
		"\2\u00ba\u00e9\b\b\1\2\u00bb\u00bc\b\b\1\2\u00bc\u00bd\7\25\2\2\u00bd"+
		"\u00be\7%\2\2\u00be\u00c4\b\b\1\2\u00bf\u00c0\7\27\2\2\u00c0\u00c1\7%"+
		"\2\2\u00c1\u00c3\b\b\1\2\u00c2\u00bf\3\2\2\2\u00c3\u00c6\3\2\2\2\u00c4"+
		"\u00c2\3\2\2\2\u00c4\u00c5\3\2\2\2\u00c5\u00c7\3\2\2\2\u00c6\u00c4\3\2"+
		"\2\2\u00c7\u00c8\7\26\2\2\u00c8\u00e9\b\b\1\2\u00c9\u00ca\5 \21\2\u00ca"+
		"\u00cb\b\b\1\2\u00cb\u00e9\3\2\2\2\u00cc\u00cf\7%\2\2\u00cd\u00ce\7\23"+
		"\2\2\u00ce\u00d0\7%\2\2\u00cf\u00cd\3\2\2\2\u00cf\u00d0\3\2\2\2\u00d0"+
		"\u00d1\3\2\2\2\u00d1\u00e9\b\b\1\2\u00d2\u00d3\b\b\1\2\u00d3\u00df\7\30"+
		"\2\2\u00d4\u00d5\5\16\b\2\u00d5\u00dc\b\b\1\2\u00d6\u00d7\7\27\2\2\u00d7"+
		"\u00d8\5\16\b\2\u00d8\u00d9\b\b\1\2\u00d9\u00db\3\2\2\2\u00da\u00d6\3"+
		"\2\2\2\u00db\u00de\3\2\2\2\u00dc\u00da\3\2\2\2\u00dc\u00dd\3\2\2\2\u00dd"+
		"\u00e0\3\2\2\2\u00de\u00dc\3\2\2\2\u00df\u00d4\3\2\2\2\u00df\u00e0\3\2"+
		"\2\2\u00e0\u00e1\3\2\2\2\u00e1\u00e2\7\31\2\2\u00e2\u00e9\b\b\1\2\u00e3"+
		"\u00e4\5\20\t\2\u00e4\u00e5\b\b\1\2\u00e5\u00e9\3\2\2\2\u00e6\u00e7\7"+
		"&\2\2\u00e7\u00e9\b\b\1\2\u00e8\u00b8\3\2\2\2\u00e8\u00bb\3\2\2\2\u00e8"+
		"\u00c9\3\2\2\2\u00e8\u00cc\3\2\2\2\u00e8\u00d2\3\2\2\2\u00e8\u00e3\3\2"+
		"\2\2\u00e8\u00e6\3\2\2\2\u00e9\17\3\2\2\2\u00ea\u00eb\b\t\1\2\u00eb\u00ec"+
		"\7\"\2\2\u00ec\u00ed\7 \2\2\u00ed\u00ee\5\16\b\2\u00ee\u00f5\b\t\1\2\u00ef"+
		"\u00f0\7\27\2\2\u00f0\u00f1\5\16\b\2\u00f1\u00f2\b\t\1\2\u00f2\u00f4\3"+
		"\2\2\2\u00f3\u00ef\3\2\2\2\u00f4\u00f7\3\2\2\2\u00f5\u00f3\3\2\2\2\u00f5"+
		"\u00f6\3\2\2\2\u00f6\u00f8\3\2\2\2\u00f7\u00f5\3\2\2\2\u00f8\u00f9\7!"+
		"\2\2\u00f9\u00fa\b\t\1\2\u00fa\u0103\3\2\2\2\u00fb\u00fc\7\"\2\2\u00fc"+
		"\u00fd\7\30\2\2\u00fd\u00fe\7%\2\2\u00fe\u00ff\7\31\2\2\u00ff\u0103\b"+
		"\t\1\2\u0100\u0101\7\"\2\2\u0101\u0103\b\t\1\2\u0102\u00ea\3\2\2\2\u0102"+
		"\u00fb\3\2\2\2\u0102\u0100\3\2\2\2\u0103\21\3\2\2\2\u0104\u0105\5\4\3"+
		"\2\u0105\u0106\7\36\2\2\u0106\u0107\7\"\2\2\u0107\u0108\7\32\2\2\u0108"+
		"\u0109\5\16\b\2\u0109\u010a\7\35\2\2\u010a\u010b\b\n\1\2\u010b\23\3\2"+
		"\2\2\u010c\u010d\5\b\5\2\u010d\u010e\7\36\2\2\u010e\u010f\7\"\2\2\u010f"+
		"\u0112\5\34\17\2\u0110\u0111\7\32\2\2\u0111\u0113\5\16\b\2\u0112\u0110"+
		"\3\2\2\2\u0112\u0113\3\2\2\2\u0113\u0114\3\2\2\2\u0114\u0115\7\35\2\2"+
		"\u0115\u0116\b\13\1\2\u0116\25\3\2\2\2\u0117\u0118\b\f\1\2\u0118\u0119"+
		"\7\16\2\2\u0119\u011a\7\"\2\2\u011a\u011b\7 \2\2\u011b\u011c\5\16\b\2"+
		"\u011c\u0123\b\f\1\2\u011d\u011e\7\27\2\2\u011e\u011f\5\16\b\2\u011f\u0120"+
		"\b\f\1\2\u0120\u0122\3\2\2\2\u0121\u011d\3\2\2\2\u0122\u0125\3\2\2\2\u0123"+
		"\u0121\3\2\2\2\u0123\u0124\3\2\2\2\u0124\u0126\3\2\2\2\u0125\u0123\3\2"+
		"\2\2\u0126\u0127\7!\2\2\u0127\u0128\5\34\17\2\u0128\u0129\7\35\2\2\u0129"+
		"\u012a\b\f\1\2\u012a\27\3\2\2\2\u012b\u012c\7\17\2\2\u012c\u012d\5\34"+
		"\17\2\u012d\u012e\5\32\16\2\u012e\u012f\7\35\2\2\u012f\u0130\b\r\1\2\u0130"+
		"\31\3\2\2\2\u0131\u0132\7\21\2\2\u0132\u0133\5\16\b\2\u0133\u0134\b\16"+
		"\1\2\u0134\u013c\3\2\2\2\u0135\u0136\7\22\2\2\u0136\u0137\5\16\b\2\u0137"+
		"\u0138\b\16\1\2\u0138\u013c\3\2\2\2\u0139\u013a\7\20\2\2\u013a\u013c\b"+
		"\16\1\2\u013b\u0131\3\2\2\2\u013b\u0135\3\2\2\2\u013b\u0139\3\2\2\2\u013c"+
		"\33\3\2\2\2\u013d\u0144\b\17\1\2\u013e\u013f\7\37\2\2\u013f\u0140\5\36"+
		"\20\2\u0140\u0141\b\17\1\2\u0141\u0143\3\2\2\2\u0142\u013e\3\2\2\2\u0143"+
		"\u0146\3\2\2\2\u0144\u0142\3\2\2\2\u0144\u0145\3\2\2\2\u0145\35\3\2\2"+
		"\2\u0146\u0144\3\2\2\2\u0147\u0148\b\20\1\2\u0148\u0157\7\"\2\2\u0149"+
		"\u014a\7 \2\2\u014a\u014b\5\16\b\2\u014b\u0152\b\20\1\2\u014c\u014d\7"+
		"\27\2\2\u014d\u014e\5\16\b\2\u014e\u014f\b\20\1\2\u014f\u0151\3\2\2\2"+
		"\u0150\u014c\3\2\2\2\u0151\u0154\3\2\2\2\u0152\u0150\3\2\2\2\u0152\u0153"+
		"\3\2\2\2\u0153\u0155\3\2\2\2\u0154\u0152\3\2\2\2\u0155\u0156\7!\2\2\u0156"+
		"\u0158\3\2\2\2\u0157\u0149\3\2\2\2\u0157\u0158\3\2\2\2\u0158\u0159\3\2"+
		"\2\2\u0159\u015a\b\20\1\2\u015a\37\3\2\2\2\u015b\u015c\7\4\2\2\u015c\u0160"+
		"\b\21\1\2\u015d\u015e\7\5\2\2\u015e\u0160\b\21\1\2\u015f\u015b\3\2\2\2"+
		"\u015f\u015d\3\2\2\2\u0160!\3\2\2\2\u0161\u0162\7\r\2\2\u0162\u0163\7"+
		"\"\2\2\u0163\u0164\7 \2\2\u0164\u0169\5$\23\2\u0165\u0166\7\27\2\2\u0166"+
		"\u0168\5$\23\2\u0167\u0165\3\2\2\2\u0168\u016b\3\2\2\2\u0169\u0167\3\2"+
		"\2\2\u0169\u016a\3\2\2\2\u016a\u016c\3\2\2\2\u016b\u0169\3\2\2\2\u016c"+
		"\u016d\7!\2\2\u016d\u016e\7\35\2\2\u016e\u016f\b\22\1\2\u016f#\3\2\2\2"+
		"\u0170\u0171\5&\24\2\u0171\u0172\7\36\2\2\u0172\u0173\7\"\2\2\u0173%\3"+
		"\2\2\2\u0174\u0177\5(\25\2\u0175\u0177\5*\26\2\u0176\u0174\3\2\2\2\u0176"+
		"\u0175\3\2\2\2\u0177\'\3\2\2\2\u0178\u01ea\5\4\3\2\u0179\u017a\7%\2\2"+
		"\u017a\u017b\7\23\2\2\u017b\u01ea\7%\2\2\u017c\u017d\7\25\2\2\u017d\u0182"+
		"\7%\2\2\u017e\u017f\7\27\2\2\u017f\u0181\7%\2\2\u0180\u017e\3\2\2\2\u0181"+
		"\u0184\3\2\2\2\u0182\u0180\3\2\2\2\u0182\u0183\3\2\2\2\u0183\u0185\3\2"+
		"\2\2\u0184\u0182\3\2\2\2\u0185\u01ea\7\26\2\2\u0186\u0187\7\b\2\2\u0187"+
		"\u0188\7\t\2\2\u0188\u0189\7%\2\2\u0189\u018a\7\23\2\2\u018a\u01ea\7%"+
		"\2\2\u018b\u018c\7\b\2\2\u018c\u018d\7\t\2\2\u018d\u018e\7\25\2\2\u018e"+
		"\u0193\7%\2\2\u018f\u0190\7\27\2\2\u0190\u0192\7%\2\2\u0191\u018f\3\2"+
		"\2\2\u0192\u0195\3\2\2\2\u0193\u0191\3\2\2\2\u0193\u0194\3\2\2\2\u0194"+
		"\u0196\3\2\2\2\u0195\u0193\3\2\2\2\u0196\u01ea\7\26\2\2\u0197\u0198\7"+
		"\n\2\2\u0198\u0199\7\30\2\2\u0199\u019e\5\f\7\2\u019a\u019b\7\27\2\2\u019b"+
		"\u019d\5\f\7\2\u019c\u019a\3\2\2\2\u019d\u01a0\3\2\2\2\u019e\u019c\3\2"+
		"\2\2\u019e\u019f\3\2\2\2\u019f\u01a1\3\2\2\2\u01a0\u019e\3\2\2\2\u01a1"+
		"\u01a2\7\31\2\2\u01a2\u01a3\7\t\2\2\u01a3\u01a4\7%\2\2\u01a4\u01a5\7\23"+
		"\2\2\u01a5\u01a6\7%\2\2\u01a6\u01ea\3\2\2\2\u01a7\u01a8\7\n\2\2\u01a8"+
		"\u01a9\7\30\2\2\u01a9\u01ae\5\f\7\2\u01aa\u01ab\7\27\2\2\u01ab\u01ad\5"+
		"\f\7\2\u01ac\u01aa\3\2\2\2\u01ad\u01b0\3\2\2\2\u01ae\u01ac\3\2\2\2\u01ae"+
		"\u01af\3\2\2\2\u01af\u01b1\3\2\2\2\u01b0\u01ae\3\2\2\2\u01b1\u01b2\7\31"+
		"\2\2\u01b2\u01b3\7\t\2\2\u01b3\u01b4\7\25\2\2\u01b4\u01b9\7%\2\2\u01b5"+
		"\u01b6\7\27\2\2\u01b6\u01b8\7%\2\2\u01b7\u01b5\3\2\2\2\u01b8\u01bb\3\2"+
		"\2\2\u01b9\u01b7\3\2\2\2\u01b9\u01ba\3\2\2\2\u01ba\u01bc\3\2\2\2\u01bb"+
		"\u01b9\3\2\2\2\u01bc\u01bd\7\26\2\2\u01bd\u01ea\3\2\2\2\u01be\u01bf\7"+
		"\n\2\2\u01bf\u01c0\7\30\2\2\u01c0\u01c5\5\f\7\2\u01c1\u01c2\7\27\2\2\u01c2"+
		"\u01c4\5\f\7\2\u01c3\u01c1\3\2\2\2\u01c4\u01c7\3\2\2\2\u01c5\u01c3\3\2"+
		"\2\2\u01c5\u01c6\3\2\2\2\u01c6\u01c8\3\2\2\2\u01c7\u01c5\3\2\2\2\u01c8"+
		"\u01c9\7\31\2\2\u01c9\u01ca\7\t\2\2\u01ca\u01cb\7\b\2\2\u01cb\u01cc\7"+
		"\t\2\2\u01cc\u01cd\7%\2\2\u01cd\u01ce\7\23\2\2\u01ce\u01cf\7%\2\2\u01cf"+
		"\u01ea\3\2\2\2\u01d0\u01d1\7\n\2\2\u01d1\u01d2\7\30\2\2\u01d2\u01d7\5"+
		"\f\7\2\u01d3\u01d4\7\27\2\2\u01d4\u01d6\5\f\7\2\u01d5\u01d3\3\2\2\2\u01d6"+
		"\u01d9\3\2\2\2\u01d7\u01d5\3\2\2\2\u01d7\u01d8\3\2\2\2\u01d8\u01da\3\2"+
		"\2\2\u01d9\u01d7\3\2\2\2\u01da\u01db\7\31\2\2\u01db\u01dc\7\t\2\2\u01dc"+
		"\u01dd\7\b\2\2\u01dd\u01de\7\t\2\2\u01de\u01df\7\25\2\2\u01df\u01e4\7"+
		"%\2\2\u01e0\u01e1\7\27\2\2\u01e1\u01e3\7%\2\2\u01e2\u01e0\3\2\2\2\u01e3"+
		"\u01e6\3\2\2\2\u01e4\u01e2\3\2\2\2\u01e4\u01e5\3\2\2\2\u01e5\u01e7\3\2"+
		"\2\2\u01e6\u01e4\3\2\2\2\u01e7\u01e8\7\26\2\2\u01e8\u01ea\3\2\2\2\u01e9"+
		"\u0178\3\2\2\2\u01e9\u0179\3\2\2\2\u01e9\u017c\3\2\2\2\u01e9\u0186\3\2"+
		"\2\2\u01e9\u018b\3\2\2\2\u01e9\u0197\3\2\2\2\u01e9\u01a7\3\2\2\2\u01e9"+
		"\u01be\3\2\2\2\u01e9\u01d0\3\2\2\2\u01ea)\3\2\2\2\u01eb\u0202\5\b\5\2"+
		"\u01ec\u01ed\7\13\2\2\u01ed\u01ee\7\b\2\2\u01ee\u01ef\7\t\2\2\u01ef\u0202"+
		"\7\6\2\2\u01f0\u01f1\7\n\2\2\u01f1\u01f2\7\30\2\2\u01f2\u01f7\5\f\7\2"+
		"\u01f3\u01f4\7\27\2\2\u01f4\u01f6\5\f\7\2\u01f5\u01f3\3\2\2\2\u01f6\u01f9"+
		"\3\2\2\2\u01f7\u01f5\3\2\2\2\u01f7\u01f8\3\2\2\2\u01f8\u01fa\3\2\2\2\u01f9"+
		"\u01f7\3\2\2\2\u01fa\u01fb\7\31\2\2\u01fb\u01fc\7\t\2\2\u01fc\u01fd\7"+
		"\13\2\2\u01fd\u01fe\7\b\2\2\u01fe\u01ff\7\t\2\2\u01ff\u0200\7\6\2\2\u0200"+
		"\u0202\3\2\2\2\u0201\u01eb\3\2\2\2\u0201\u01ec\3\2\2\2\u0201\u01f0\3\2"+
		"\2\2\u0202+\3\2\2\2*\60\65\67=MXdq~\u0093\u00a9\u00ae\u00b6\u00c4\u00cf"+
		"\u00dc\u00df\u00e8\u00f5\u0102\u0112\u0123\u013b\u0144\u0152\u0157\u015f"+
		"\u0169\u0176\u0182\u0193\u019e\u01ae\u01b9\u01c5\u01d7\u01e4\u01e9\u01f7"+
		"\u0201";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}