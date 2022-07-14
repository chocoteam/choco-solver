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
			setState(176);
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
				match(INT);

				    ((Var_type_uContext)_localctx).decl =  new DSet(DInt.me);
				    
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(154);
				match(SET);
				setState(155);
				match(OF);
				setState(156);
				((Var_type_uContext)_localctx).i1 = match(INT_CONST);
				setState(157);
				match(DD);
				setState(158);
				((Var_type_uContext)_localctx).i2 = match(INT_CONST);

				    ((Var_type_uContext)_localctx).decl =  new DSet(new DInt2(EInt.make((((Var_type_uContext)_localctx).i1!=null?((Var_type_uContext)_localctx).i1.getText():null)), EInt.make((((Var_type_uContext)_localctx).i2!=null?((Var_type_uContext)_localctx).i2.getText():null))));
				    
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{

				    ArrayList<EInt> values = new ArrayList();
				    
				setState(161);
				match(SET);
				setState(162);
				match(OF);
				setState(163);
				match(LB);
				setState(164);
				((Var_type_uContext)_localctx).i = match(INT_CONST);
				values.add(EInt.make((((Var_type_uContext)_localctx).i!=null?((Var_type_uContext)_localctx).i.getText():null)));
				setState(171);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==CM) {
					{
					{
					setState(166);
					match(CM);
					setState(167);
					((Var_type_uContext)_localctx).i = match(INT_CONST);
					values.add(EInt.make((((Var_type_uContext)_localctx).i!=null?((Var_type_uContext)_localctx).i.getText():null)));
					}
					}
					setState(173);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(174);
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
			setState(184);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case INT_CONST:
				enterOuterAlt(_localctx, 1);
				{
				setState(178);
				((Index_setContext)_localctx).i1 = match(INT_CONST);
				setState(179);
				match(DD);
				setState(180);
				((Index_setContext)_localctx).i2 = match(INT_CONST);

				    ((Index_setContext)_localctx).decl =  new DInt2(EInt.make((((Index_setContext)_localctx).i1!=null?((Index_setContext)_localctx).i1.getText():null)), EInt.make((((Index_setContext)_localctx).i2!=null?((Index_setContext)_localctx).i2.getText():null)));
				    
				}
				break;
			case INT:
				enterOuterAlt(_localctx, 2);
				{
				setState(182);
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
			setState(234);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,17,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(186);
				match(LB);
				setState(187);
				match(RB);

				    ((ExprContext)_localctx).exp =  new ESetList(new ArrayList());
				    
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{

				    ArrayList<EInt> values = new ArrayList();
				    
				setState(190);
				match(LB);
				setState(191);
				((ExprContext)_localctx).i = match(INT_CONST);
				values.add(EInt.make((((ExprContext)_localctx).i!=null?((ExprContext)_localctx).i.getText():null)));
				setState(198);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==CM) {
					{
					{
					setState(193);
					match(CM);
					setState(194);
					((ExprContext)_localctx).i = match(INT_CONST);
					values.add(EInt.make((((ExprContext)_localctx).i!=null?((ExprContext)_localctx).i.getText():null)));
					}
					}
					setState(200);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(201);
				match(RB);

				    ((ExprContext)_localctx).exp =  new ESetList(values);
				    
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(203);
				((ExprContext)_localctx).b = bool_const();

				    ((ExprContext)_localctx).exp = EBool.make(((ExprContext)_localctx).b.value);
				    
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(206);
				((ExprContext)_localctx).i1 = match(INT_CONST);
				setState(209);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==DD) {
					{
					setState(207);
					match(DD);
					setState(208);
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
				    
				setState(213);
				match(LS);
				setState(225);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << TRUE) | (1L << FALSE) | (1L << LB) | (1L << LS) | (1L << IDENTIFIER) | (1L << INT_CONST) | (1L << STRING))) != 0)) {
					{
					setState(214);
					((ExprContext)_localctx).e = expr();
					exps.add(((ExprContext)_localctx).e.exp);
					setState(222);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==CM) {
						{
						{
						setState(216);
						match(CM);
						setState(217);
						((ExprContext)_localctx).e = expr();
						exps.add(((ExprContext)_localctx).e.exp);
						}
						}
						setState(224);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					}
				}

				setState(227);
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
				setState(229);
				((ExprContext)_localctx).ie = id_expr();

				    ((ExprContext)_localctx).exp =  ((ExprContext)_localctx).ie.exp;
				    
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(232);
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
			setState(260);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,19,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{

				    ArrayList<Expression> exps = new ArrayList();
				    
				setState(237);
				((Id_exprContext)_localctx).IDENTIFIER = match(IDENTIFIER);
				setState(238);
				match(LP);
				setState(239);
				((Id_exprContext)_localctx).e = expr();
				exps.add(((Id_exprContext)_localctx).e.exp);
				setState(247);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==CM) {
					{
					{
					setState(241);
					match(CM);
					setState(242);
					((Id_exprContext)_localctx).e = expr();
					exps.add(((Id_exprContext)_localctx).e.exp);
					}
					}
					setState(249);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(250);
				match(RP);

				    ((Id_exprContext)_localctx).exp =  new EAnnotation(new EIdentifier(datas, (((Id_exprContext)_localctx).IDENTIFIER!=null?((Id_exprContext)_localctx).IDENTIFIER.getText():null)), exps);
				    
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(253);
				((Id_exprContext)_localctx).IDENTIFIER = match(IDENTIFIER);
				setState(254);
				match(LS);
				setState(255);
				((Id_exprContext)_localctx).i = match(INT_CONST);
				setState(256);
				match(RS);

				    ((Id_exprContext)_localctx).exp =  new EIdArray(datas, (((Id_exprContext)_localctx).IDENTIFIER!=null?((Id_exprContext)_localctx).IDENTIFIER.getText():null), Integer.parseInt((((Id_exprContext)_localctx).i!=null?((Id_exprContext)_localctx).i.getText():null)));
				    
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(258);
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
			setState(262);
			((Param_declContext)_localctx).pt = par_type();
			setState(263);
			match(CL);
			setState(264);
			((Param_declContext)_localctx).IDENTIFIER = match(IDENTIFIER);
			setState(265);
			match(EQ);
			setState(266);
			((Param_declContext)_localctx).e = expr();
			setState(267);
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
			setState(270);
			((Var_declContext)_localctx).vt = var_type();
			setState(271);
			match(CL);
			setState(272);
			((Var_declContext)_localctx).IDENTIFIER = match(IDENTIFIER);
			setState(273);
			((Var_declContext)_localctx).anns = annotations();
			setState(276);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==EQ) {
				{
				setState(274);
				((Var_declContext)_localctx).eq = match(EQ);
				setState(275);
				((Var_declContext)_localctx).e = expr();
				}
			}

			setState(278);
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
			    
			setState(282);
			match(CONSTRAINT);
			setState(283);
			((ConstraintContext)_localctx).IDENTIFIER = match(IDENTIFIER);
			setState(284);
			match(LP);
			setState(285);
			((ConstraintContext)_localctx).e = expr();
			exps.add(((ConstraintContext)_localctx).e.exp);
			setState(293);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==CM) {
				{
				{
				setState(287);
				match(CM);
				setState(288);
				((ConstraintContext)_localctx).e = expr();
				exps.add(((ConstraintContext)_localctx).e.exp);
				}
				}
				setState(295);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(296);
			match(RP);
			setState(297);
			((ConstraintContext)_localctx).anns = annotations();
			setState(298);
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
			setState(301);
			match(SOLVE);
			setState(302);
			((Solve_goalContext)_localctx).anns = annotations();
			setState(303);
			((Solve_goalContext)_localctx).res = resolution();
			setState(304);
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
			setState(317);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case MINIMIZE:
				enterOuterAlt(_localctx, 1);
				{
				setState(307);
				match(MINIMIZE);
				setState(308);
				((ResolutionContext)_localctx).e = expr();

				    ((ResolutionContext)_localctx).rtype = ResolutionPolicy.MINIMIZE;
				    ((ResolutionContext)_localctx).exp = ((ResolutionContext)_localctx).e.exp;
				    
				}
				break;
			case MAXIMIZE:
				enterOuterAlt(_localctx, 2);
				{
				setState(311);
				match(MAXIMIZE);
				setState(312);
				((ResolutionContext)_localctx).e = expr();

				    ((ResolutionContext)_localctx).rtype = ResolutionPolicy.MAXIMIZE;
				    ((ResolutionContext)_localctx).exp = ((ResolutionContext)_localctx).e.exp;
				    
				}
				break;
			case SATISFY:
				enterOuterAlt(_localctx, 3);
				{
				setState(315);
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
			    
			setState(326);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==DC) {
				{
				{
				setState(320);
				match(DC);
				setState(321);
				((AnnotationsContext)_localctx).e = annotation();
				_localctx.anns.add(((AnnotationsContext)_localctx).e.ann);
				}
				}
				setState(328);
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
			    
			setState(330);
			((AnnotationContext)_localctx).IDENTIFIER = match(IDENTIFIER);
			setState(345);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LP) {
				{
				setState(331);
				match(LP);
				setState(332);
				((AnnotationContext)_localctx).e = expr();
				exps.add(((AnnotationContext)_localctx).e.exp);
				setState(340);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==CM) {
					{
					{
					setState(334);
					match(CM);
					setState(335);
					((AnnotationContext)_localctx).e = expr();
					exps.add(((AnnotationContext)_localctx).e.exp);
					}
					}
					setState(342);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(343);
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
			setState(353);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case TRUE:
				enterOuterAlt(_localctx, 1);
				{
				setState(349);
				match(TRUE);
				((Bool_constContext)_localctx).value =  true;
				}
				break;
			case FALSE:
				enterOuterAlt(_localctx, 2);
				{
				setState(351);
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
			setState(355);
			match(PREDICATE);
			setState(356);
			((Pred_declContext)_localctx).IDENTIFIER = match(IDENTIFIER);
			setState(357);
			match(LP);
			setState(358);
			pred_param();
			setState(363);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==CM) {
				{
				{
				setState(359);
				match(CM);
				setState(360);
				pred_param();
				}
				}
				setState(365);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(366);
			match(RP);
			setState(367);
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
			setState(370);
			pred_param_type();
			setState(371);
			match(CL);
			setState(372);
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
			setState(376);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,28,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(374);
				par_pred_param_type();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(375);
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
			setState(491);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,37,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(378);
				par_type();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(379);
				match(INT_CONST);
				setState(380);
				match(DD);
				setState(381);
				match(INT_CONST);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(382);
				match(LB);
				setState(383);
				match(INT_CONST);
				setState(388);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==CM) {
					{
					{
					setState(384);
					match(CM);
					setState(385);
					match(INT_CONST);
					}
					}
					setState(390);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(391);
				match(RB);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(392);
				match(SET);
				setState(393);
				match(OF);
				setState(394);
				match(INT_CONST);
				setState(395);
				match(DD);
				setState(396);
				match(INT_CONST);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(397);
				match(SET);
				setState(398);
				match(OF);
				setState(399);
				match(LB);
				setState(400);
				match(INT_CONST);
				setState(405);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==CM) {
					{
					{
					setState(401);
					match(CM);
					setState(402);
					match(INT_CONST);
					}
					}
					setState(407);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(408);
				match(RB);
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(409);
				match(ARRAY);
				setState(410);
				match(LS);
				setState(411);
				index_set();
				setState(416);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==CM) {
					{
					{
					setState(412);
					match(CM);
					setState(413);
					index_set();
					}
					}
					setState(418);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(419);
				match(RS);
				setState(420);
				match(OF);
				setState(421);
				match(INT_CONST);
				setState(422);
				match(DD);
				setState(423);
				match(INT_CONST);
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(425);
				match(ARRAY);
				setState(426);
				match(LS);
				setState(427);
				index_set();
				setState(432);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==CM) {
					{
					{
					setState(428);
					match(CM);
					setState(429);
					index_set();
					}
					}
					setState(434);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(435);
				match(RS);
				setState(436);
				match(OF);
				setState(437);
				match(LB);
				setState(438);
				match(INT_CONST);
				setState(443);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==CM) {
					{
					{
					setState(439);
					match(CM);
					setState(440);
					match(INT_CONST);
					}
					}
					setState(445);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(446);
				match(RB);
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(448);
				match(ARRAY);
				setState(449);
				match(LS);
				setState(450);
				index_set();
				setState(455);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==CM) {
					{
					{
					setState(451);
					match(CM);
					setState(452);
					index_set();
					}
					}
					setState(457);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(458);
				match(RS);
				setState(459);
				match(OF);
				setState(460);
				match(SET);
				setState(461);
				match(OF);
				setState(462);
				match(INT_CONST);
				setState(463);
				match(DD);
				setState(464);
				match(INT_CONST);
				}
				break;
			case 9:
				enterOuterAlt(_localctx, 9);
				{
				setState(466);
				match(ARRAY);
				setState(467);
				match(LS);
				setState(468);
				index_set();
				setState(473);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==CM) {
					{
					{
					setState(469);
					match(CM);
					setState(470);
					index_set();
					}
					}
					setState(475);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(476);
				match(RS);
				setState(477);
				match(OF);
				setState(478);
				match(SET);
				setState(479);
				match(OF);
				setState(480);
				match(LB);
				setState(481);
				match(INT_CONST);
				setState(486);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==CM) {
					{
					{
					setState(482);
					match(CM);
					setState(483);
					match(INT_CONST);
					}
					}
					setState(488);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(489);
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
			setState(515);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,39,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(493);
				var_type();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(494);
				match(VAR);
				setState(495);
				match(SET);
				setState(496);
				match(OF);
				setState(497);
				match(INT);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(498);
				match(ARRAY);
				setState(499);
				match(LS);
				setState(500);
				index_set();
				setState(505);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==CM) {
					{
					{
					setState(501);
					match(CM);
					setState(502);
					index_set();
					}
					}
					setState(507);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(508);
				match(RS);
				setState(509);
				match(OF);
				setState(510);
				match(VAR);
				setState(511);
				match(SET);
				setState(512);
				match(OF);
				setState(513);
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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\'\u0208\4\2\t\2\4"+
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
		"\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\7\6\u00ac\n\6\f\6\16\6\u00af\13\6"+
		"\3\6\3\6\5\6\u00b3\n\6\3\7\3\7\3\7\3\7\3\7\3\7\5\7\u00bb\n\7\3\b\3\b\3"+
		"\b\3\b\3\b\3\b\3\b\3\b\3\b\3\b\7\b\u00c7\n\b\f\b\16\b\u00ca\13\b\3\b\3"+
		"\b\3\b\3\b\3\b\3\b\3\b\3\b\5\b\u00d4\n\b\3\b\3\b\3\b\3\b\3\b\3\b\3\b\3"+
		"\b\3\b\7\b\u00df\n\b\f\b\16\b\u00e2\13\b\5\b\u00e4\n\b\3\b\3\b\3\b\3\b"+
		"\3\b\3\b\3\b\5\b\u00ed\n\b\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\7\t\u00f8"+
		"\n\t\f\t\16\t\u00fb\13\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\5\t\u0107"+
		"\n\t\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\13\3\13\3\13\3\13\3\13\3\13\5\13"+
		"\u0117\n\13\3\13\3\13\3\13\3\f\3\f\3\f\3\f\3\f\3\f\3\f\3\f\3\f\3\f\7\f"+
		"\u0126\n\f\f\f\16\f\u0129\13\f\3\f\3\f\3\f\3\f\3\f\3\r\3\r\3\r\3\r\3\r"+
		"\3\r\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\5\16\u0140\n\16"+
		"\3\17\3\17\3\17\3\17\3\17\7\17\u0147\n\17\f\17\16\17\u014a\13\17\3\20"+
		"\3\20\3\20\3\20\3\20\3\20\3\20\3\20\3\20\7\20\u0155\n\20\f\20\16\20\u0158"+
		"\13\20\3\20\3\20\5\20\u015c\n\20\3\20\3\20\3\21\3\21\3\21\3\21\5\21\u0164"+
		"\n\21\3\22\3\22\3\22\3\22\3\22\3\22\7\22\u016c\n\22\f\22\16\22\u016f\13"+
		"\22\3\22\3\22\3\22\3\22\3\23\3\23\3\23\3\23\3\24\3\24\5\24\u017b\n\24"+
		"\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\7\25\u0185\n\25\f\25\16\25\u0188"+
		"\13\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\7\25"+
		"\u0196\n\25\f\25\16\25\u0199\13\25\3\25\3\25\3\25\3\25\3\25\3\25\7\25"+
		"\u01a1\n\25\f\25\16\25\u01a4\13\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25"+
		"\3\25\3\25\3\25\3\25\7\25\u01b1\n\25\f\25\16\25\u01b4\13\25\3\25\3\25"+
		"\3\25\3\25\3\25\3\25\7\25\u01bc\n\25\f\25\16\25\u01bf\13\25\3\25\3\25"+
		"\3\25\3\25\3\25\3\25\3\25\7\25\u01c8\n\25\f\25\16\25\u01cb\13\25\3\25"+
		"\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\7\25\u01da"+
		"\n\25\f\25\16\25\u01dd\13\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\7"+
		"\25\u01e7\n\25\f\25\16\25\u01ea\13\25\3\25\3\25\5\25\u01ee\n\25\3\26\3"+
		"\26\3\26\3\26\3\26\3\26\3\26\3\26\3\26\3\26\7\26\u01fa\n\26\f\26\16\26"+
		"\u01fd\13\26\3\26\3\26\3\26\3\26\3\26\3\26\3\26\5\26\u0206\n\26\3\26\2"+
		"\2\27\2\4\6\b\n\f\16\20\22\24\26\30\32\34\36 \"$&(*\2\2\2\u0231\2,\3\2"+
		"\2\2\4X\3\2\2\2\6d\3\2\2\2\b~\3\2\2\2\n\u00b2\3\2\2\2\f\u00ba\3\2\2\2"+
		"\16\u00ec\3\2\2\2\20\u0106\3\2\2\2\22\u0108\3\2\2\2\24\u0110\3\2\2\2\26"+
		"\u011b\3\2\2\2\30\u012f\3\2\2\2\32\u013f\3\2\2\2\34\u0141\3\2\2\2\36\u014b"+
		"\3\2\2\2 \u0163\3\2\2\2\"\u0165\3\2\2\2$\u0174\3\2\2\2&\u017a\3\2\2\2"+
		"(\u01ed\3\2\2\2*\u0205\3\2\2\2,\60\b\2\1\2-/\5\"\22\2.-\3\2\2\2/\62\3"+
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
		"\3\2\2\2\u0080\u0081\7\3\2\2\u0081\u00b3\b\6\1\2\u0082\u0083\7\7\2\2\u0083"+
		"\u00b3\b\6\1\2\u0084\u0085\7\6\2\2\u0085\u00b3\b\6\1\2\u0086\u0087\7%"+
		"\2\2\u0087\u0088\7\23\2\2\u0088\u0089\7%\2\2\u0089\u00b3\b\6\1\2\u008a"+
		"\u008b\b\6\1\2\u008b\u008c\7\25\2\2\u008c\u008d\7%\2\2\u008d\u0093\b\6"+
		"\1\2\u008e\u008f\7\27\2\2\u008f\u0090\7%\2\2\u0090\u0092\b\6\1\2\u0091"+
		"\u008e\3\2\2\2\u0092\u0095\3\2\2\2\u0093\u0091\3\2\2\2\u0093\u0094\3\2"+
		"\2\2\u0094\u0096\3\2\2\2\u0095\u0093\3\2\2\2\u0096\u0097\7\26\2\2\u0097"+
		"\u00b3\b\6\1\2\u0098\u0099\7\b\2\2\u0099\u009a\7\t\2\2\u009a\u009b\7\6"+
		"\2\2\u009b\u00b3\b\6\1\2\u009c\u009d\7\b\2\2\u009d\u009e\7\t\2\2\u009e"+
		"\u009f\7%\2\2\u009f\u00a0\7\23\2\2\u00a0\u00a1\7%\2\2\u00a1\u00b3\b\6"+
		"\1\2\u00a2\u00a3\b\6\1\2\u00a3\u00a4\7\b\2\2\u00a4\u00a5\7\t\2\2\u00a5"+
		"\u00a6\7\25\2\2\u00a6\u00a7\7%\2\2\u00a7\u00ad\b\6\1\2\u00a8\u00a9\7\27"+
		"\2\2\u00a9\u00aa\7%\2\2\u00aa\u00ac\b\6\1\2\u00ab\u00a8\3\2\2\2\u00ac"+
		"\u00af\3\2\2\2\u00ad\u00ab\3\2\2\2\u00ad\u00ae\3\2\2\2\u00ae\u00b0\3\2"+
		"\2\2\u00af\u00ad\3\2\2\2\u00b0\u00b1\7\26\2\2\u00b1\u00b3\b\6\1\2\u00b2"+
		"\u0080\3\2\2\2\u00b2\u0082\3\2\2\2\u00b2\u0084\3\2\2\2\u00b2\u0086\3\2"+
		"\2\2\u00b2\u008a\3\2\2\2\u00b2\u0098\3\2\2\2\u00b2\u009c\3\2\2\2\u00b2"+
		"\u00a2\3\2\2\2\u00b3\13\3\2\2\2\u00b4\u00b5\7%\2\2\u00b5\u00b6\7\23\2"+
		"\2\u00b6\u00b7\7%\2\2\u00b7\u00bb\b\7\1\2\u00b8\u00b9\7\6\2\2\u00b9\u00bb"+
		"\b\7\1\2\u00ba\u00b4\3\2\2\2\u00ba\u00b8\3\2\2\2\u00bb\r\3\2\2\2\u00bc"+
		"\u00bd\7\25\2\2\u00bd\u00be\7\26\2\2\u00be\u00ed\b\b\1\2\u00bf\u00c0\b"+
		"\b\1\2\u00c0\u00c1\7\25\2\2\u00c1\u00c2\7%\2\2\u00c2\u00c8\b\b\1\2\u00c3"+
		"\u00c4\7\27\2\2\u00c4\u00c5\7%\2\2\u00c5\u00c7\b\b\1\2\u00c6\u00c3\3\2"+
		"\2\2\u00c7\u00ca\3\2\2\2\u00c8\u00c6\3\2\2\2\u00c8\u00c9\3\2\2\2\u00c9"+
		"\u00cb\3\2\2\2\u00ca\u00c8\3\2\2\2\u00cb\u00cc\7\26\2\2\u00cc\u00ed\b"+
		"\b\1\2\u00cd\u00ce\5 \21\2\u00ce\u00cf\b\b\1\2\u00cf\u00ed\3\2\2\2\u00d0"+
		"\u00d3\7%\2\2\u00d1\u00d2\7\23\2\2\u00d2\u00d4\7%\2\2\u00d3\u00d1\3\2"+
		"\2\2\u00d3\u00d4\3\2\2\2\u00d4\u00d5\3\2\2\2\u00d5\u00ed\b\b\1\2\u00d6"+
		"\u00d7\b\b\1\2\u00d7\u00e3\7\30\2\2\u00d8\u00d9\5\16\b\2\u00d9\u00e0\b"+
		"\b\1\2\u00da\u00db\7\27\2\2\u00db\u00dc\5\16\b\2\u00dc\u00dd\b\b\1\2\u00dd"+
		"\u00df\3\2\2\2\u00de\u00da\3\2\2\2\u00df\u00e2\3\2\2\2\u00e0\u00de\3\2"+
		"\2\2\u00e0\u00e1\3\2\2\2\u00e1\u00e4\3\2\2\2\u00e2\u00e0\3\2\2\2\u00e3"+
		"\u00d8\3\2\2\2\u00e3\u00e4\3\2\2\2\u00e4\u00e5\3\2\2\2\u00e5\u00e6\7\31"+
		"\2\2\u00e6\u00ed\b\b\1\2\u00e7\u00e8\5\20\t\2\u00e8\u00e9\b\b\1\2\u00e9"+
		"\u00ed\3\2\2\2\u00ea\u00eb\7&\2\2\u00eb\u00ed\b\b\1\2\u00ec\u00bc\3\2"+
		"\2\2\u00ec\u00bf\3\2\2\2\u00ec\u00cd\3\2\2\2\u00ec\u00d0\3\2\2\2\u00ec"+
		"\u00d6\3\2\2\2\u00ec\u00e7\3\2\2\2\u00ec\u00ea\3\2\2\2\u00ed\17\3\2\2"+
		"\2\u00ee\u00ef\b\t\1\2\u00ef\u00f0\7\"\2\2\u00f0\u00f1\7 \2\2\u00f1\u00f2"+
		"\5\16\b\2\u00f2\u00f9\b\t\1\2\u00f3\u00f4\7\27\2\2\u00f4\u00f5\5\16\b"+
		"\2\u00f5\u00f6\b\t\1\2\u00f6\u00f8\3\2\2\2\u00f7\u00f3\3\2\2\2\u00f8\u00fb"+
		"\3\2\2\2\u00f9\u00f7\3\2\2\2\u00f9\u00fa\3\2\2\2\u00fa\u00fc\3\2\2\2\u00fb"+
		"\u00f9\3\2\2\2\u00fc\u00fd\7!\2\2\u00fd\u00fe\b\t\1\2\u00fe\u0107\3\2"+
		"\2\2\u00ff\u0100\7\"\2\2\u0100\u0101\7\30\2\2\u0101\u0102\7%\2\2\u0102"+
		"\u0103\7\31\2\2\u0103\u0107\b\t\1\2\u0104\u0105\7\"\2\2\u0105\u0107\b"+
		"\t\1\2\u0106\u00ee\3\2\2\2\u0106\u00ff\3\2\2\2\u0106\u0104\3\2\2\2\u0107"+
		"\21\3\2\2\2\u0108\u0109\5\4\3\2\u0109\u010a\7\36\2\2\u010a\u010b\7\"\2"+
		"\2\u010b\u010c\7\32\2\2\u010c\u010d\5\16\b\2\u010d\u010e\7\35\2\2\u010e"+
		"\u010f\b\n\1\2\u010f\23\3\2\2\2\u0110\u0111\5\b\5\2\u0111\u0112\7\36\2"+
		"\2\u0112\u0113\7\"\2\2\u0113\u0116\5\34\17\2\u0114\u0115\7\32\2\2\u0115"+
		"\u0117\5\16\b\2\u0116\u0114\3\2\2\2\u0116\u0117\3\2\2\2\u0117\u0118\3"+
		"\2\2\2\u0118\u0119\7\35\2\2\u0119\u011a\b\13\1\2\u011a\25\3\2\2\2\u011b"+
		"\u011c\b\f\1\2\u011c\u011d\7\16\2\2\u011d\u011e\7\"\2\2\u011e\u011f\7"+
		" \2\2\u011f\u0120\5\16\b\2\u0120\u0127\b\f\1\2\u0121\u0122\7\27\2\2\u0122"+
		"\u0123\5\16\b\2\u0123\u0124\b\f\1\2\u0124\u0126\3\2\2\2\u0125\u0121\3"+
		"\2\2\2\u0126\u0129\3\2\2\2\u0127\u0125\3\2\2\2\u0127\u0128\3\2\2\2\u0128"+
		"\u012a\3\2\2\2\u0129\u0127\3\2\2\2\u012a\u012b\7!\2\2\u012b\u012c\5\34"+
		"\17\2\u012c\u012d\7\35\2\2\u012d\u012e\b\f\1\2\u012e\27\3\2\2\2\u012f"+
		"\u0130\7\17\2\2\u0130\u0131\5\34\17\2\u0131\u0132\5\32\16\2\u0132\u0133"+
		"\7\35\2\2\u0133\u0134\b\r\1\2\u0134\31\3\2\2\2\u0135\u0136\7\21\2\2\u0136"+
		"\u0137\5\16\b\2\u0137\u0138\b\16\1\2\u0138\u0140\3\2\2\2\u0139\u013a\7"+
		"\22\2\2\u013a\u013b\5\16\b\2\u013b\u013c\b\16\1\2\u013c\u0140\3\2\2\2"+
		"\u013d\u013e\7\20\2\2\u013e\u0140\b\16\1\2\u013f\u0135\3\2\2\2\u013f\u0139"+
		"\3\2\2\2\u013f\u013d\3\2\2\2\u0140\33\3\2\2\2\u0141\u0148\b\17\1\2\u0142"+
		"\u0143\7\37\2\2\u0143\u0144\5\36\20\2\u0144\u0145\b\17\1\2\u0145\u0147"+
		"\3\2\2\2\u0146\u0142\3\2\2\2\u0147\u014a\3\2\2\2\u0148\u0146\3\2\2\2\u0148"+
		"\u0149\3\2\2\2\u0149\35\3\2\2\2\u014a\u0148\3\2\2\2\u014b\u014c\b\20\1"+
		"\2\u014c\u015b\7\"\2\2\u014d\u014e\7 \2\2\u014e\u014f\5\16\b\2\u014f\u0156"+
		"\b\20\1\2\u0150\u0151\7\27\2\2\u0151\u0152\5\16\b\2\u0152\u0153\b\20\1"+
		"\2\u0153\u0155\3\2\2\2\u0154\u0150\3\2\2\2\u0155\u0158\3\2\2\2\u0156\u0154"+
		"\3\2\2\2\u0156\u0157\3\2\2\2\u0157\u0159\3\2\2\2\u0158\u0156\3\2\2\2\u0159"+
		"\u015a\7!\2\2\u015a\u015c\3\2\2\2\u015b\u014d\3\2\2\2\u015b\u015c\3\2"+
		"\2\2\u015c\u015d\3\2\2\2\u015d\u015e\b\20\1\2\u015e\37\3\2\2\2\u015f\u0160"+
		"\7\4\2\2\u0160\u0164\b\21\1\2\u0161\u0162\7\5\2\2\u0162\u0164\b\21\1\2"+
		"\u0163\u015f\3\2\2\2\u0163\u0161\3\2\2\2\u0164!\3\2\2\2\u0165\u0166\7"+
		"\r\2\2\u0166\u0167\7\"\2\2\u0167\u0168\7 \2\2\u0168\u016d\5$\23\2\u0169"+
		"\u016a\7\27\2\2\u016a\u016c\5$\23\2\u016b\u0169\3\2\2\2\u016c\u016f\3"+
		"\2\2\2\u016d\u016b\3\2\2\2\u016d\u016e\3\2\2\2\u016e\u0170\3\2\2\2\u016f"+
		"\u016d\3\2\2\2\u0170\u0171\7!\2\2\u0171\u0172\7\35\2\2\u0172\u0173\b\22"+
		"\1\2\u0173#\3\2\2\2\u0174\u0175\5&\24\2\u0175\u0176\7\36\2\2\u0176\u0177"+
		"\7\"\2\2\u0177%\3\2\2\2\u0178\u017b\5(\25\2\u0179\u017b\5*\26\2\u017a"+
		"\u0178\3\2\2\2\u017a\u0179\3\2\2\2\u017b\'\3\2\2\2\u017c\u01ee\5\4\3\2"+
		"\u017d\u017e\7%\2\2\u017e\u017f\7\23\2\2\u017f\u01ee\7%\2\2\u0180\u0181"+
		"\7\25\2\2\u0181\u0186\7%\2\2\u0182\u0183\7\27\2\2\u0183\u0185\7%\2\2\u0184"+
		"\u0182\3\2\2\2\u0185\u0188\3\2\2\2\u0186\u0184\3\2\2\2\u0186\u0187\3\2"+
		"\2\2\u0187\u0189\3\2\2\2\u0188\u0186\3\2\2\2\u0189\u01ee\7\26\2\2\u018a"+
		"\u018b\7\b\2\2\u018b\u018c\7\t\2\2\u018c\u018d\7%\2\2\u018d\u018e\7\23"+
		"\2\2\u018e\u01ee\7%\2\2\u018f\u0190\7\b\2\2\u0190\u0191\7\t\2\2\u0191"+
		"\u0192\7\25\2\2\u0192\u0197\7%\2\2\u0193\u0194\7\27\2\2\u0194\u0196\7"+
		"%\2\2\u0195\u0193\3\2\2\2\u0196\u0199\3\2\2\2\u0197\u0195\3\2\2\2\u0197"+
		"\u0198\3\2\2\2\u0198\u019a\3\2\2\2\u0199\u0197\3\2\2\2\u019a\u01ee\7\26"+
		"\2\2\u019b\u019c\7\n\2\2\u019c\u019d\7\30\2\2\u019d\u01a2\5\f\7\2\u019e"+
		"\u019f\7\27\2\2\u019f\u01a1\5\f\7\2\u01a0\u019e\3\2\2\2\u01a1\u01a4\3"+
		"\2\2\2\u01a2\u01a0\3\2\2\2\u01a2\u01a3\3\2\2\2\u01a3\u01a5\3\2\2\2\u01a4"+
		"\u01a2\3\2\2\2\u01a5\u01a6\7\31\2\2\u01a6\u01a7\7\t\2\2\u01a7\u01a8\7"+
		"%\2\2\u01a8\u01a9\7\23\2\2\u01a9\u01aa\7%\2\2\u01aa\u01ee\3\2\2\2\u01ab"+
		"\u01ac\7\n\2\2\u01ac\u01ad\7\30\2\2\u01ad\u01b2\5\f\7\2\u01ae\u01af\7"+
		"\27\2\2\u01af\u01b1\5\f\7\2\u01b0\u01ae\3\2\2\2\u01b1\u01b4\3\2\2\2\u01b2"+
		"\u01b0\3\2\2\2\u01b2\u01b3\3\2\2\2\u01b3\u01b5\3\2\2\2\u01b4\u01b2\3\2"+
		"\2\2\u01b5\u01b6\7\31\2\2\u01b6\u01b7\7\t\2\2\u01b7\u01b8\7\25\2\2\u01b8"+
		"\u01bd\7%\2\2\u01b9\u01ba\7\27\2\2\u01ba\u01bc\7%\2\2\u01bb\u01b9\3\2"+
		"\2\2\u01bc\u01bf\3\2\2\2\u01bd\u01bb\3\2\2\2\u01bd\u01be\3\2\2\2\u01be"+
		"\u01c0\3\2\2\2\u01bf\u01bd\3\2\2\2\u01c0\u01c1\7\26\2\2\u01c1\u01ee\3"+
		"\2\2\2\u01c2\u01c3\7\n\2\2\u01c3\u01c4\7\30\2\2\u01c4\u01c9\5\f\7\2\u01c5"+
		"\u01c6\7\27\2\2\u01c6\u01c8\5\f\7\2\u01c7\u01c5\3\2\2\2\u01c8\u01cb\3"+
		"\2\2\2\u01c9\u01c7\3\2\2\2\u01c9\u01ca\3\2\2\2\u01ca\u01cc\3\2\2\2\u01cb"+
		"\u01c9\3\2\2\2\u01cc\u01cd\7\31\2\2\u01cd\u01ce\7\t\2\2\u01ce\u01cf\7"+
		"\b\2\2\u01cf\u01d0\7\t\2\2\u01d0\u01d1\7%\2\2\u01d1\u01d2\7\23\2\2\u01d2"+
		"\u01d3\7%\2\2\u01d3\u01ee\3\2\2\2\u01d4\u01d5\7\n\2\2\u01d5\u01d6\7\30"+
		"\2\2\u01d6\u01db\5\f\7\2\u01d7\u01d8\7\27\2\2\u01d8\u01da\5\f\7\2\u01d9"+
		"\u01d7\3\2\2\2\u01da\u01dd\3\2\2\2\u01db\u01d9\3\2\2\2\u01db\u01dc\3\2"+
		"\2\2\u01dc\u01de\3\2\2\2\u01dd\u01db\3\2\2\2\u01de\u01df\7\31\2\2\u01df"+
		"\u01e0\7\t\2\2\u01e0\u01e1\7\b\2\2\u01e1\u01e2\7\t\2\2\u01e2\u01e3\7\25"+
		"\2\2\u01e3\u01e8\7%\2\2\u01e4\u01e5\7\27\2\2\u01e5\u01e7\7%\2\2\u01e6"+
		"\u01e4\3\2\2\2\u01e7\u01ea\3\2\2\2\u01e8\u01e6\3\2\2\2\u01e8\u01e9\3\2"+
		"\2\2\u01e9\u01eb\3\2\2\2\u01ea\u01e8\3\2\2\2\u01eb\u01ec\7\26\2\2\u01ec"+
		"\u01ee\3\2\2\2\u01ed\u017c\3\2\2\2\u01ed\u017d\3\2\2\2\u01ed\u0180\3\2"+
		"\2\2\u01ed\u018a\3\2\2\2\u01ed\u018f\3\2\2\2\u01ed\u019b\3\2\2\2\u01ed"+
		"\u01ab\3\2\2\2\u01ed\u01c2\3\2\2\2\u01ed\u01d4\3\2\2\2\u01ee)\3\2\2\2"+
		"\u01ef\u0206\5\b\5\2\u01f0\u01f1\7\13\2\2\u01f1\u01f2\7\b\2\2\u01f2\u01f3"+
		"\7\t\2\2\u01f3\u0206\7\6\2\2\u01f4\u01f5\7\n\2\2\u01f5\u01f6\7\30\2\2"+
		"\u01f6\u01fb\5\f\7\2\u01f7\u01f8\7\27\2\2\u01f8\u01fa\5\f\7\2\u01f9\u01f7"+
		"\3\2\2\2\u01fa\u01fd\3\2\2\2\u01fb\u01f9\3\2\2\2\u01fb\u01fc\3\2\2\2\u01fc"+
		"\u01fe\3\2\2\2\u01fd\u01fb\3\2\2\2\u01fe\u01ff\7\31\2\2\u01ff\u0200\7"+
		"\t\2\2\u0200\u0201\7\13\2\2\u0201\u0202\7\b\2\2\u0202\u0203\7\t\2\2\u0203"+
		"\u0204\7\6\2\2\u0204\u0206\3\2\2\2\u0205\u01ef\3\2\2\2\u0205\u01f0\3\2"+
		"\2\2\u0205\u01f4\3\2\2\2\u0206+\3\2\2\2*\60\65\67=MXdq~\u0093\u00ad\u00b2"+
		"\u00ba\u00c8\u00d3\u00e0\u00e3\u00ec\u00f9\u0106\u0116\u0127\u013f\u0148"+
		"\u0156\u015b\u0163\u016d\u017a\u0186\u0197\u01a2\u01b2\u01bd\u01c9\u01db"+
		"\u01e8\u01ed\u01fb\u0205";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}