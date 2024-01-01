/*
 * This file is part of choco-parsers, http://choco-solver.org/
 *
 * Copyright (c) 2024, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
// Generated from Flatzinc4Lexer.g4 by ANTLR 4.9.3
package org.chocosolver.parser.flatzinc;


import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class Flatzinc4Lexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.9.3", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		BOOL=1, TRUE=2, FALSE=3, INT=4, FLOAT=5, SET=6, OF=7, ARRAY=8, VAR=9, 
		PAR=10, PREDICATE=11, CONSTRAINT=12, SOLVE=13, SATISFY=14, MINIMIZE=15, 
		MAXIMIZE=16, DD=17, DO=18, LB=19, RB=20, CM=21, LS=22, RS=23, EQ=24, PL=25, 
		MN=26, SC=27, CL=28, DC=29, LP=30, RP=31, IDENTIFIER=32, COMMENT=33, WS=34, 
		INT_CONST=35, FLOAT_CONST=36, STRING=37, CHAR=38;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"BOOL", "TRUE", "FALSE", "INT", "FLOAT", "SET", "OF", "ARRAY", "VAR", 
			"PAR", "PREDICATE", "CONSTRAINT", "SOLVE", "SATISFY", "MINIMIZE", "MAXIMIZE", 
			"DD", "DO", "LB", "RB", "CM", "LS", "RS", "EQ", "PL", "MN", "SC", "CL", 
			"DC", "LP", "RP", "IDENTIFIER", "COMMENT", "WS", "INT_CONST", "FLOAT_CONST", 
			"STRING", "CHAR", "EXPONENT", "ESC_SEQ", "OCTAL_ESC", "HEX_DIGIT", "UNICODE_ESC"
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
			"FLOAT_CONST", "STRING", "CHAR"
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


	public Flatzinc4Lexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "Flatzinc4Lexer.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getChannelNames() { return channelNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2(\u014e\b\1\4\2\t"+
		"\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"+
		"\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\4)\t)\4*\t*\4+\t+\4"+
		",\t,\3\2\3\2\3\2\3\2\3\2\3\3\3\3\3\3\3\3\3\3\3\4\3\4\3\4\3\4\3\4\3\4\3"+
		"\5\3\5\3\5\3\5\3\6\3\6\3\6\3\6\3\6\3\6\3\7\3\7\3\7\3\7\3\b\3\b\3\b\3\t"+
		"\3\t\3\t\3\t\3\t\3\t\3\n\3\n\3\n\3\n\3\13\3\13\3\13\3\13\3\f\3\f\3\f\3"+
		"\f\3\f\3\f\3\f\3\f\3\f\3\f\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\r"+
		"\3\16\3\16\3\16\3\16\3\16\3\16\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17"+
		"\3\20\3\20\3\20\3\20\3\20\3\20\3\20\3\20\3\20\3\21\3\21\3\21\3\21\3\21"+
		"\3\21\3\21\3\21\3\21\3\22\3\22\3\22\3\23\3\23\3\24\3\24\3\25\3\25\3\26"+
		"\3\26\3\27\3\27\3\30\3\30\3\31\3\31\3\32\3\32\3\33\3\33\3\34\3\34\3\35"+
		"\3\35\3\36\3\36\3\36\3\37\3\37\3 \3 \3!\3!\7!\u00e0\n!\f!\16!\u00e3\13"+
		"!\3\"\3\"\7\"\u00e7\n\"\f\"\16\"\u00ea\13\"\3\"\5\"\u00ed\n\"\3\"\3\""+
		"\3\"\3\"\3#\3#\3#\3#\3$\5$\u00f8\n$\3$\6$\u00fb\n$\r$\16$\u00fc\3%\5%"+
		"\u0100\n%\3%\6%\u0103\n%\r%\16%\u0104\3%\3%\6%\u0109\n%\r%\16%\u010a\3"+
		"%\5%\u010e\n%\3%\5%\u0111\n%\3%\6%\u0114\n%\r%\16%\u0115\3%\5%\u0119\n"+
		"%\3&\3&\3&\7&\u011e\n&\f&\16&\u0121\13&\3&\3&\3\'\3\'\3\'\5\'\u0128\n"+
		"\'\3\'\3\'\3(\3(\5(\u012e\n(\3(\6(\u0131\n(\r(\16(\u0132\3)\3)\3)\3)\5"+
		")\u0139\n)\3*\3*\3*\3*\3*\3*\3*\3*\3*\5*\u0144\n*\3+\3+\3,\3,\3,\3,\3"+
		",\3,\3,\2\2-\3\3\5\4\7\5\t\6\13\7\r\b\17\t\21\n\23\13\25\f\27\r\31\16"+
		"\33\17\35\20\37\21!\22#\23%\24\'\25)\26+\27-\30/\31\61\32\63\33\65\34"+
		"\67\359\36;\37= ?!A\"C#E$G%I&K\'M(O\2Q\2S\2U\2W\2\3\2\f\5\2C\\aac|\6\2"+
		"\62;C\\aac|\4\2\f\f\17\17\5\2\13\f\17\17\"\"\4\2$$^^\4\2))^^\4\2GGgg\4"+
		"\2--//\n\2$$))^^ddhhppttvv\5\2\62;CHch\2\u015d\2\3\3\2\2\2\2\5\3\2\2\2"+
		"\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3"+
		"\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27\3\2\2\2\2\31\3\2\2\2\2\33\3\2\2"+
		"\2\2\35\3\2\2\2\2\37\3\2\2\2\2!\3\2\2\2\2#\3\2\2\2\2%\3\2\2\2\2\'\3\2"+
		"\2\2\2)\3\2\2\2\2+\3\2\2\2\2-\3\2\2\2\2/\3\2\2\2\2\61\3\2\2\2\2\63\3\2"+
		"\2\2\2\65\3\2\2\2\2\67\3\2\2\2\29\3\2\2\2\2;\3\2\2\2\2=\3\2\2\2\2?\3\2"+
		"\2\2\2A\3\2\2\2\2C\3\2\2\2\2E\3\2\2\2\2G\3\2\2\2\2I\3\2\2\2\2K\3\2\2\2"+
		"\2M\3\2\2\2\3Y\3\2\2\2\5^\3\2\2\2\7c\3\2\2\2\ti\3\2\2\2\13m\3\2\2\2\r"+
		"s\3\2\2\2\17w\3\2\2\2\21z\3\2\2\2\23\u0080\3\2\2\2\25\u0084\3\2\2\2\27"+
		"\u0088\3\2\2\2\31\u0092\3\2\2\2\33\u009d\3\2\2\2\35\u00a3\3\2\2\2\37\u00ab"+
		"\3\2\2\2!\u00b4\3\2\2\2#\u00bd\3\2\2\2%\u00c0\3\2\2\2\'\u00c2\3\2\2\2"+
		")\u00c4\3\2\2\2+\u00c6\3\2\2\2-\u00c8\3\2\2\2/\u00ca\3\2\2\2\61\u00cc"+
		"\3\2\2\2\63\u00ce\3\2\2\2\65\u00d0\3\2\2\2\67\u00d2\3\2\2\29\u00d4\3\2"+
		"\2\2;\u00d6\3\2\2\2=\u00d9\3\2\2\2?\u00db\3\2\2\2A\u00dd\3\2\2\2C\u00e4"+
		"\3\2\2\2E\u00f2\3\2\2\2G\u00f7\3\2\2\2I\u0118\3\2\2\2K\u011a\3\2\2\2M"+
		"\u0124\3\2\2\2O\u012b\3\2\2\2Q\u0138\3\2\2\2S\u0143\3\2\2\2U\u0145\3\2"+
		"\2\2W\u0147\3\2\2\2YZ\7d\2\2Z[\7q\2\2[\\\7q\2\2\\]\7n\2\2]\4\3\2\2\2^"+
		"_\7v\2\2_`\7t\2\2`a\7w\2\2ab\7g\2\2b\6\3\2\2\2cd\7h\2\2de\7c\2\2ef\7n"+
		"\2\2fg\7u\2\2gh\7g\2\2h\b\3\2\2\2ij\7k\2\2jk\7p\2\2kl\7v\2\2l\n\3\2\2"+
		"\2mn\7h\2\2no\7n\2\2op\7q\2\2pq\7c\2\2qr\7v\2\2r\f\3\2\2\2st\7u\2\2tu"+
		"\7g\2\2uv\7v\2\2v\16\3\2\2\2wx\7q\2\2xy\7h\2\2y\20\3\2\2\2z{\7c\2\2{|"+
		"\7t\2\2|}\7t\2\2}~\7c\2\2~\177\7{\2\2\177\22\3\2\2\2\u0080\u0081\7x\2"+
		"\2\u0081\u0082\7c\2\2\u0082\u0083\7t\2\2\u0083\24\3\2\2\2\u0084\u0085"+
		"\7r\2\2\u0085\u0086\7c\2\2\u0086\u0087\7t\2\2\u0087\26\3\2\2\2\u0088\u0089"+
		"\7r\2\2\u0089\u008a\7t\2\2\u008a\u008b\7g\2\2\u008b\u008c\7f\2\2\u008c"+
		"\u008d\7k\2\2\u008d\u008e\7e\2\2\u008e\u008f\7c\2\2\u008f\u0090\7v\2\2"+
		"\u0090\u0091\7g\2\2\u0091\30\3\2\2\2\u0092\u0093\7e\2\2\u0093\u0094\7"+
		"q\2\2\u0094\u0095\7p\2\2\u0095\u0096\7u\2\2\u0096\u0097\7v\2\2\u0097\u0098"+
		"\7t\2\2\u0098\u0099\7c\2\2\u0099\u009a\7k\2\2\u009a\u009b\7p\2\2\u009b"+
		"\u009c\7v\2\2\u009c\32\3\2\2\2\u009d\u009e\7u\2\2\u009e\u009f\7q\2\2\u009f"+
		"\u00a0\7n\2\2\u00a0\u00a1\7x\2\2\u00a1\u00a2\7g\2\2\u00a2\34\3\2\2\2\u00a3"+
		"\u00a4\7u\2\2\u00a4\u00a5\7c\2\2\u00a5\u00a6\7v\2\2\u00a6\u00a7\7k\2\2"+
		"\u00a7\u00a8\7u\2\2\u00a8\u00a9\7h\2\2\u00a9\u00aa\7{\2\2\u00aa\36\3\2"+
		"\2\2\u00ab\u00ac\7o\2\2\u00ac\u00ad\7k\2\2\u00ad\u00ae\7p\2\2\u00ae\u00af"+
		"\7k\2\2\u00af\u00b0\7o\2\2\u00b0\u00b1\7k\2\2\u00b1\u00b2\7|\2\2\u00b2"+
		"\u00b3\7g\2\2\u00b3 \3\2\2\2\u00b4\u00b5\7o\2\2\u00b5\u00b6\7c\2\2\u00b6"+
		"\u00b7\7z\2\2\u00b7\u00b8\7k\2\2\u00b8\u00b9\7o\2\2\u00b9\u00ba\7k\2\2"+
		"\u00ba\u00bb\7|\2\2\u00bb\u00bc\7g\2\2\u00bc\"\3\2\2\2\u00bd\u00be\7\60"+
		"\2\2\u00be\u00bf\7\60\2\2\u00bf$\3\2\2\2\u00c0\u00c1\7\60\2\2\u00c1&\3"+
		"\2\2\2\u00c2\u00c3\7}\2\2\u00c3(\3\2\2\2\u00c4\u00c5\7\177\2\2\u00c5*"+
		"\3\2\2\2\u00c6\u00c7\7.\2\2\u00c7,\3\2\2\2\u00c8\u00c9\7]\2\2\u00c9.\3"+
		"\2\2\2\u00ca\u00cb\7_\2\2\u00cb\60\3\2\2\2\u00cc\u00cd\7?\2\2\u00cd\62"+
		"\3\2\2\2\u00ce\u00cf\7-\2\2\u00cf\64\3\2\2\2\u00d0\u00d1\7/\2\2\u00d1"+
		"\66\3\2\2\2\u00d2\u00d3\7=\2\2\u00d38\3\2\2\2\u00d4\u00d5\7<\2\2\u00d5"+
		":\3\2\2\2\u00d6\u00d7\7<\2\2\u00d7\u00d8\7<\2\2\u00d8<\3\2\2\2\u00d9\u00da"+
		"\7*\2\2\u00da>\3\2\2\2\u00db\u00dc\7+\2\2\u00dc@\3\2\2\2\u00dd\u00e1\t"+
		"\2\2\2\u00de\u00e0\t\3\2\2\u00df\u00de\3\2\2\2\u00e0\u00e3\3\2\2\2\u00e1"+
		"\u00df\3\2\2\2\u00e1\u00e2\3\2\2\2\u00e2B\3\2\2\2\u00e3\u00e1\3\2\2\2"+
		"\u00e4\u00e8\7\'\2\2\u00e5\u00e7\n\4\2\2\u00e6\u00e5\3\2\2\2\u00e7\u00ea"+
		"\3\2\2\2\u00e8\u00e6\3\2\2\2\u00e8\u00e9\3\2\2\2\u00e9\u00ec\3\2\2\2\u00ea"+
		"\u00e8\3\2\2\2\u00eb\u00ed\7\17\2\2\u00ec\u00eb\3\2\2\2\u00ec\u00ed\3"+
		"\2\2\2\u00ed\u00ee\3\2\2\2\u00ee\u00ef\7\f\2\2\u00ef\u00f0\3\2\2\2\u00f0"+
		"\u00f1\b\"\2\2\u00f1D\3\2\2\2\u00f2\u00f3\t\5\2\2\u00f3\u00f4\3\2\2\2"+
		"\u00f4\u00f5\b#\2\2\u00f5F\3\2\2\2\u00f6\u00f8\7/\2\2\u00f7\u00f6\3\2"+
		"\2\2\u00f7\u00f8\3\2\2\2\u00f8\u00fa\3\2\2\2\u00f9\u00fb\4\62;\2\u00fa"+
		"\u00f9\3\2\2\2\u00fb\u00fc\3\2\2\2\u00fc\u00fa\3\2\2\2\u00fc\u00fd\3\2"+
		"\2\2\u00fdH\3\2\2\2\u00fe\u0100\7/\2\2\u00ff\u00fe\3\2\2\2\u00ff\u0100"+
		"\3\2\2\2\u0100\u0102\3\2\2\2\u0101\u0103\4\62;\2\u0102\u0101\3\2\2\2\u0103"+
		"\u0104\3\2\2\2\u0104\u0102\3\2\2\2\u0104\u0105\3\2\2\2\u0105\u0106\3\2"+
		"\2\2\u0106\u0108\7\60\2\2\u0107\u0109\4\62;\2\u0108\u0107\3\2\2\2\u0109"+
		"\u010a\3\2\2\2\u010a\u0108\3\2\2\2\u010a\u010b\3\2\2\2\u010b\u010d\3\2"+
		"\2\2\u010c\u010e\5O(\2\u010d\u010c\3\2\2\2\u010d\u010e\3\2\2\2\u010e\u0119"+
		"\3\2\2\2\u010f\u0111\7/\2\2\u0110\u010f\3\2\2\2\u0110\u0111\3\2\2\2\u0111"+
		"\u0113\3\2\2\2\u0112\u0114\4\62;\2\u0113\u0112\3\2\2\2\u0114\u0115\3\2"+
		"\2\2\u0115\u0113\3\2\2\2\u0115\u0116\3\2\2\2\u0116\u0117\3\2\2\2\u0117"+
		"\u0119\5O(\2\u0118\u00ff\3\2\2\2\u0118\u0110\3\2\2\2\u0119J\3\2\2\2\u011a"+
		"\u011f\7$\2\2\u011b\u011e\5Q)\2\u011c\u011e\n\6\2\2\u011d\u011b\3\2\2"+
		"\2\u011d\u011c\3\2\2\2\u011e\u0121\3\2\2\2\u011f\u011d\3\2\2\2\u011f\u0120"+
		"\3\2\2\2\u0120\u0122\3\2\2\2\u0121\u011f\3\2\2\2\u0122\u0123\7$\2\2\u0123"+
		"L\3\2\2\2\u0124\u0127\7)\2\2\u0125\u0128\5Q)\2\u0126\u0128\n\7\2\2\u0127"+
		"\u0125\3\2\2\2\u0127\u0126\3\2\2\2\u0128\u0129\3\2\2\2\u0129\u012a\7)"+
		"\2\2\u012aN\3\2\2\2\u012b\u012d\t\b\2\2\u012c\u012e\t\t\2\2\u012d\u012c"+
		"\3\2\2\2\u012d\u012e\3\2\2\2\u012e\u0130\3\2\2\2\u012f\u0131\4\62;\2\u0130"+
		"\u012f\3\2\2\2\u0131\u0132\3\2\2\2\u0132\u0130\3\2\2\2\u0132\u0133\3\2"+
		"\2\2\u0133P\3\2\2\2\u0134\u0135\7^\2\2\u0135\u0139\t\n\2\2\u0136\u0139"+
		"\5W,\2\u0137\u0139\5S*\2\u0138\u0134\3\2\2\2\u0138\u0136\3\2\2\2\u0138"+
		"\u0137\3\2\2\2\u0139R\3\2\2\2\u013a\u013b\7^\2\2\u013b\u013c\4\62\65\2"+
		"\u013c\u013d\4\629\2\u013d\u0144\4\629\2\u013e\u013f\7^\2\2\u013f\u0140"+
		"\4\629\2\u0140\u0144\4\629\2\u0141\u0142\7^\2\2\u0142\u0144\4\629\2\u0143"+
		"\u013a\3\2\2\2\u0143\u013e\3\2\2\2\u0143\u0141\3\2\2\2\u0144T\3\2\2\2"+
		"\u0145\u0146\t\13\2\2\u0146V\3\2\2\2\u0147\u0148\7^\2\2\u0148\u0149\7"+
		"w\2\2\u0149\u014a\5U+\2\u014a\u014b\5U+\2\u014b\u014c\5U+\2\u014c\u014d"+
		"\5U+\2\u014dX\3\2\2\2\26\2\u00e1\u00e8\u00ec\u00f7\u00fc\u00ff\u0104\u010a"+
		"\u010d\u0110\u0115\u0118\u011d\u011f\u0127\u012d\u0132\u0138\u0143\3\b"+
		"\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}