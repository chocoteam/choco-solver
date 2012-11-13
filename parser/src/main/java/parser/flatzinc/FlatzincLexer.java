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

// $ANTLR 3.4 parser/flatzinc/FlatzincLexer.g 2012-11-13 10:00:41

package parser.flatzinc;


import org.antlr.runtime.*;

@SuppressWarnings({"all", "warnings", "unchecked"})
public class FlatzincLexer extends Lexer {
    public static final int EOF = -1;
    public static final int ANNOTATIONS = 4;
    public static final int APAR = 5;
    public static final int ARRAY = 6;
    public static final int ARRPAR = 7;
    public static final int ARRVAR = 8;
    public static final int AVAR = 9;
    public static final int BOOL = 10;
    public static final int CHAR = 11;
    public static final int CL = 12;
    public static final int CM = 13;
    public static final int COMMENT = 14;
    public static final int CONSTRAINT = 15;
    public static final int DC = 16;
    public static final int DD = 17;
    public static final int DO = 18;
    public static final int EQ = 19;
    public static final int ESC_SEQ = 20;
    public static final int EXPONENT = 21;
    public static final int EXPR = 22;
    public static final int FALSE = 23;
    public static final int FLOAT = 24;
    public static final int HEX_DIGIT = 25;
    public static final int IDENTIFIER = 26;
    public static final int INDEX = 27;
    public static final int INT = 28;
    public static final int INT_CONST = 29;
    public static final int LB = 30;
    public static final int LP = 31;
    public static final int LS = 32;
    public static final int MAXIMIZE = 33;
    public static final int MINIMIZE = 34;
    public static final int MN = 35;
    public static final int OCTAL_ESC = 36;
    public static final int OF = 37;
    public static final int PAR = 38;
    public static final int PL = 39;
    public static final int PREDICATE = 40;
    public static final int RB = 41;
    public static final int RP = 42;
    public static final int RS = 43;
    public static final int SATISFY = 44;
    public static final int SC = 45;
    public static final int SET = 46;
    public static final int SOLVE = 47;
    public static final int STRING = 48;
    public static final int TRUE = 49;
    public static final int UNICODE_ESC = 50;
    public static final int VAR = 51;
    public static final int WS = 52;

    // delegates
    // delegators
    public Lexer[] getDelegates() {
        return new Lexer[]{};
    }

    public FlatzincLexer() {
    }

    public FlatzincLexer(CharStream input) {
        this(input, new RecognizerSharedState());
    }

    public FlatzincLexer(CharStream input, RecognizerSharedState state) {
        super(input, state);
    }

    public String getGrammarFileName() {
        return "parser/flatzinc/FlatzincLexer.g";
    }

    // $ANTLR start "BOOL"
    public final void mBOOL() throws RecognitionException {
        try {
            int _type = BOOL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincLexer.g:38:5: ( 'bool' )
            // parser/flatzinc/FlatzincLexer.g:38:6: 'bool'
            {
                match("bool");


            }

            state.type = _type;
            state.channel = _channel;
        } finally {
            // do for sure before leaving
        }
    }
    // $ANTLR end "BOOL"

    // $ANTLR start "TRUE"
    public final void mTRUE() throws RecognitionException {
        try {
            int _type = TRUE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincLexer.g:39:5: ( 'true' )
            // parser/flatzinc/FlatzincLexer.g:39:6: 'true'
            {
                match("true");


            }

            state.type = _type;
            state.channel = _channel;
        } finally {
            // do for sure before leaving
        }
    }
    // $ANTLR end "TRUE"

    // $ANTLR start "FALSE"
    public final void mFALSE() throws RecognitionException {
        try {
            int _type = FALSE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincLexer.g:40:6: ( 'false' )
            // parser/flatzinc/FlatzincLexer.g:40:7: 'false'
            {
                match("false");


            }

            state.type = _type;
            state.channel = _channel;
        } finally {
            // do for sure before leaving
        }
    }
    // $ANTLR end "FALSE"

    // $ANTLR start "INT"
    public final void mINT() throws RecognitionException {
        try {
            int _type = INT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincLexer.g:41:4: ( 'int' )
            // parser/flatzinc/FlatzincLexer.g:41:5: 'int'
            {
                match("int");


            }

            state.type = _type;
            state.channel = _channel;
        } finally {
            // do for sure before leaving
        }
    }
    // $ANTLR end "INT"

    // $ANTLR start "FLOAT"
    public final void mFLOAT() throws RecognitionException {
        try {
            int _type = FLOAT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincLexer.g:42:6: ( 'float' )
            // parser/flatzinc/FlatzincLexer.g:42:7: 'float'
            {
                match("float");


            }

            state.type = _type;
            state.channel = _channel;
        } finally {
            // do for sure before leaving
        }
    }
    // $ANTLR end "FLOAT"

    // $ANTLR start "SET"
    public final void mSET() throws RecognitionException {
        try {
            int _type = SET;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincLexer.g:43:5: ( 'set' )
            // parser/flatzinc/FlatzincLexer.g:43:6: 'set'
            {
                match("set");


            }

            state.type = _type;
            state.channel = _channel;
        } finally {
            // do for sure before leaving
        }
    }
    // $ANTLR end "SET"

    // $ANTLR start "OF"
    public final void mOF() throws RecognitionException {
        try {
            int _type = OF;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincLexer.g:44:4: ( 'of' )
            // parser/flatzinc/FlatzincLexer.g:44:5: 'of'
            {
                match("of");


            }

            state.type = _type;
            state.channel = _channel;
        } finally {
            // do for sure before leaving
        }
    }
    // $ANTLR end "OF"

    // $ANTLR start "ARRAY"
    public final void mARRAY() throws RecognitionException {
        try {
            int _type = ARRAY;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincLexer.g:45:7: ( 'array' )
            // parser/flatzinc/FlatzincLexer.g:45:8: 'array'
            {
                match("array");


            }

            state.type = _type;
            state.channel = _channel;
        } finally {
            // do for sure before leaving
        }
    }
    // $ANTLR end "ARRAY"

    // $ANTLR start "VAR"
    public final void mVAR() throws RecognitionException {
        try {
            int _type = VAR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincLexer.g:46:5: ( 'var' )
            // parser/flatzinc/FlatzincLexer.g:46:6: 'var'
            {
                match("var");


            }

            state.type = _type;
            state.channel = _channel;
        } finally {
            // do for sure before leaving
        }
    }
    // $ANTLR end "VAR"

    // $ANTLR start "PAR"
    public final void mPAR() throws RecognitionException {
        try {
            int _type = PAR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincLexer.g:47:5: ( 'par' )
            // parser/flatzinc/FlatzincLexer.g:47:6: 'par'
            {
                match("par");


            }

            state.type = _type;
            state.channel = _channel;
        } finally {
            // do for sure before leaving
        }
    }
    // $ANTLR end "PAR"

    // $ANTLR start "PREDICATE"
    public final void mPREDICATE() throws RecognitionException {
        try {
            int _type = PREDICATE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincLexer.g:48:11: ( 'predicate' )
            // parser/flatzinc/FlatzincLexer.g:48:12: 'predicate'
            {
                match("predicate");


            }

            state.type = _type;
            state.channel = _channel;
        } finally {
            // do for sure before leaving
        }
    }
    // $ANTLR end "PREDICATE"

    // $ANTLR start "CONSTRAINT"
    public final void mCONSTRAINT() throws RecognitionException {
        try {
            int _type = CONSTRAINT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincLexer.g:49:12: ( 'constraint' )
            // parser/flatzinc/FlatzincLexer.g:49:15: 'constraint'
            {
                match("constraint");


            }

            state.type = _type;
            state.channel = _channel;
        } finally {
            // do for sure before leaving
        }
    }
    // $ANTLR end "CONSTRAINT"

    // $ANTLR start "SOLVE"
    public final void mSOLVE() throws RecognitionException {
        try {
            int _type = SOLVE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincLexer.g:50:7: ( 'solve' )
            // parser/flatzinc/FlatzincLexer.g:50:8: 'solve'
            {
                match("solve");


            }

            state.type = _type;
            state.channel = _channel;
        } finally {
            // do for sure before leaving
        }
    }
    // $ANTLR end "SOLVE"

    // $ANTLR start "SATISFY"
    public final void mSATISFY() throws RecognitionException {
        try {
            int _type = SATISFY;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincLexer.g:51:9: ( 'satisfy' )
            // parser/flatzinc/FlatzincLexer.g:51:10: 'satisfy'
            {
                match("satisfy");


            }

            state.type = _type;
            state.channel = _channel;
        } finally {
            // do for sure before leaving
        }
    }
    // $ANTLR end "SATISFY"

    // $ANTLR start "MINIMIZE"
    public final void mMINIMIZE() throws RecognitionException {
        try {
            int _type = MINIMIZE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincLexer.g:52:10: ( 'minimize' )
            // parser/flatzinc/FlatzincLexer.g:52:11: 'minimize'
            {
                match("minimize");


            }

            state.type = _type;
            state.channel = _channel;
        } finally {
            // do for sure before leaving
        }
    }
    // $ANTLR end "MINIMIZE"

    // $ANTLR start "MAXIMIZE"
    public final void mMAXIMIZE() throws RecognitionException {
        try {
            int _type = MAXIMIZE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincLexer.g:53:10: ( 'maximize' )
            // parser/flatzinc/FlatzincLexer.g:53:11: 'maximize'
            {
                match("maximize");


            }

            state.type = _type;
            state.channel = _channel;
        } finally {
            // do for sure before leaving
        }
    }
    // $ANTLR end "MAXIMIZE"

    // $ANTLR start "DD"
    public final void mDD() throws RecognitionException {
        try {
            int _type = DD;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincLexer.g:55:3: ( '..' )
            // parser/flatzinc/FlatzincLexer.g:55:4: '..'
            {
                match("..");


            }

            state.type = _type;
            state.channel = _channel;
        } finally {
            // do for sure before leaving
        }
    }
    // $ANTLR end "DD"

    // $ANTLR start "DO"
    public final void mDO() throws RecognitionException {
        try {
            int _type = DO;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincLexer.g:56:3: ( '.' )
            // parser/flatzinc/FlatzincLexer.g:56:4: '.'
            {
                match('.');

            }

            state.type = _type;
            state.channel = _channel;
        } finally {
            // do for sure before leaving
        }
    }
    // $ANTLR end "DO"

    // $ANTLR start "LB"
    public final void mLB() throws RecognitionException {
        try {
            int _type = LB;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincLexer.g:57:3: ( '{' )
            // parser/flatzinc/FlatzincLexer.g:57:4: '{'
            {
                match('{');

            }

            state.type = _type;
            state.channel = _channel;
        } finally {
            // do for sure before leaving
        }
    }
    // $ANTLR end "LB"

    // $ANTLR start "RB"
    public final void mRB() throws RecognitionException {
        try {
            int _type = RB;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincLexer.g:58:3: ( '}' )
            // parser/flatzinc/FlatzincLexer.g:58:4: '}'
            {
                match('}');

            }

            state.type = _type;
            state.channel = _channel;
        } finally {
            // do for sure before leaving
        }
    }
    // $ANTLR end "RB"

    // $ANTLR start "CM"
    public final void mCM() throws RecognitionException {
        try {
            int _type = CM;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincLexer.g:59:3: ( ',' )
            // parser/flatzinc/FlatzincLexer.g:59:4: ','
            {
                match(',');

            }

            state.type = _type;
            state.channel = _channel;
        } finally {
            // do for sure before leaving
        }
    }
    // $ANTLR end "CM"

    // $ANTLR start "LS"
    public final void mLS() throws RecognitionException {
        try {
            int _type = LS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincLexer.g:60:3: ( '[' )
            // parser/flatzinc/FlatzincLexer.g:60:4: '['
            {
                match('[');

            }

            state.type = _type;
            state.channel = _channel;
        } finally {
            // do for sure before leaving
        }
    }
    // $ANTLR end "LS"

    // $ANTLR start "RS"
    public final void mRS() throws RecognitionException {
        try {
            int _type = RS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincLexer.g:61:4: ( ']' )
            // parser/flatzinc/FlatzincLexer.g:61:5: ']'
            {
                match(']');

            }

            state.type = _type;
            state.channel = _channel;
        } finally {
            // do for sure before leaving
        }
    }
    // $ANTLR end "RS"

    // $ANTLR start "EQ"
    public final void mEQ() throws RecognitionException {
        try {
            int _type = EQ;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincLexer.g:62:3: ( '=' )
            // parser/flatzinc/FlatzincLexer.g:62:4: '='
            {
                match('=');

            }

            state.type = _type;
            state.channel = _channel;
        } finally {
            // do for sure before leaving
        }
    }
    // $ANTLR end "EQ"

    // $ANTLR start "PL"
    public final void mPL() throws RecognitionException {
        try {
            int _type = PL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincLexer.g:63:3: ( '+' )
            // parser/flatzinc/FlatzincLexer.g:63:4: '+'
            {
                match('+');

            }

            state.type = _type;
            state.channel = _channel;
        } finally {
            // do for sure before leaving
        }
    }
    // $ANTLR end "PL"

    // $ANTLR start "MN"
    public final void mMN() throws RecognitionException {
        try {
            int _type = MN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincLexer.g:64:3: ( '-' )
            // parser/flatzinc/FlatzincLexer.g:64:4: '-'
            {
                match('-');

            }

            state.type = _type;
            state.channel = _channel;
        } finally {
            // do for sure before leaving
        }
    }
    // $ANTLR end "MN"

    // $ANTLR start "SC"
    public final void mSC() throws RecognitionException {
        try {
            int _type = SC;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincLexer.g:65:3: ( ';' )
            // parser/flatzinc/FlatzincLexer.g:65:4: ';'
            {
                match(';');

            }

            state.type = _type;
            state.channel = _channel;
        } finally {
            // do for sure before leaving
        }
    }
    // $ANTLR end "SC"

    // $ANTLR start "CL"
    public final void mCL() throws RecognitionException {
        try {
            int _type = CL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincLexer.g:66:3: ( ':' )
            // parser/flatzinc/FlatzincLexer.g:66:4: ':'
            {
                match(':');

            }

            state.type = _type;
            state.channel = _channel;
        } finally {
            // do for sure before leaving
        }
    }
    // $ANTLR end "CL"

    // $ANTLR start "DC"
    public final void mDC() throws RecognitionException {
        try {
            int _type = DC;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincLexer.g:67:3: ( '::' )
            // parser/flatzinc/FlatzincLexer.g:67:4: '::'
            {
                match("::");


            }

            state.type = _type;
            state.channel = _channel;
        } finally {
            // do for sure before leaving
        }
    }
    // $ANTLR end "DC"

    // $ANTLR start "LP"
    public final void mLP() throws RecognitionException {
        try {
            int _type = LP;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincLexer.g:68:3: ( '(' )
            // parser/flatzinc/FlatzincLexer.g:68:4: '('
            {
                match('(');

            }

            state.type = _type;
            state.channel = _channel;
        } finally {
            // do for sure before leaving
        }
    }
    // $ANTLR end "LP"

    // $ANTLR start "RP"
    public final void mRP() throws RecognitionException {
        try {
            int _type = RP;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincLexer.g:69:3: ( ')' )
            // parser/flatzinc/FlatzincLexer.g:69:4: ')'
            {
                match(')');

            }

            state.type = _type;
            state.channel = _channel;
        } finally {
            // do for sure before leaving
        }
    }
    // $ANTLR end "RP"

    // $ANTLR start "APAR"
    public final void mAPAR() throws RecognitionException {
        try {
            int _type = APAR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincLexer.g:76:9: ( '###_P###' )
            // parser/flatzinc/FlatzincLexer.g:76:13: '###_P###'
            {
                match("###_P###");


            }

            state.type = _type;
            state.channel = _channel;
        } finally {
            // do for sure before leaving
        }
    }
    // $ANTLR end "APAR"

    // $ANTLR start "ARRPAR"
    public final void mARRPAR() throws RecognitionException {
        try {
            int _type = ARRPAR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincLexer.g:77:9: ( '###AP###' )
            // parser/flatzinc/FlatzincLexer.g:77:13: '###AP###'
            {
                match("###AP###");


            }

            state.type = _type;
            state.channel = _channel;
        } finally {
            // do for sure before leaving
        }
    }
    // $ANTLR end "ARRPAR"

    // $ANTLR start "AVAR"
    public final void mAVAR() throws RecognitionException {
        try {
            int _type = AVAR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincLexer.g:78:9: ( '###_V###' )
            // parser/flatzinc/FlatzincLexer.g:78:13: '###_V###'
            {
                match("###_V###");


            }

            state.type = _type;
            state.channel = _channel;
        } finally {
            // do for sure before leaving
        }
    }
    // $ANTLR end "AVAR"

    // $ANTLR start "ARRVAR"
    public final void mARRVAR() throws RecognitionException {
        try {
            int _type = ARRVAR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincLexer.g:79:9: ( '###AV###' )
            // parser/flatzinc/FlatzincLexer.g:79:13: '###AV###'
            {
                match("###AV###");


            }

            state.type = _type;
            state.channel = _channel;
        } finally {
            // do for sure before leaving
        }
    }
    // $ANTLR end "ARRVAR"

    // $ANTLR start "INDEX"
    public final void mINDEX() throws RecognitionException {
        try {
            int _type = INDEX;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincLexer.g:80:9: ( '###ID###' )
            // parser/flatzinc/FlatzincLexer.g:80:13: '###ID###'
            {
                match("###ID###");


            }

            state.type = _type;
            state.channel = _channel;
        } finally {
            // do for sure before leaving
        }
    }
    // $ANTLR end "INDEX"

    // $ANTLR start "EXPR"
    public final void mEXPR() throws RecognitionException {
        try {
            int _type = EXPR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincLexer.g:81:9: ( '###EX###' )
            // parser/flatzinc/FlatzincLexer.g:81:13: '###EX###'
            {
                match("###EX###");


            }

            state.type = _type;
            state.channel = _channel;
        } finally {
            // do for sure before leaving
        }
    }
    // $ANTLR end "EXPR"

    // $ANTLR start "ANNOTATIONS"
    public final void mANNOTATIONS() throws RecognitionException {
        try {
            int _type = ANNOTATIONS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincLexer.g:82:12: ( '###AS###' )
            // parser/flatzinc/FlatzincLexer.g:82:13: '###AS###'
            {
                match("###AS###");


            }

            state.type = _type;
            state.channel = _channel;
        } finally {
            // do for sure before leaving
        }
    }
    // $ANTLR end "ANNOTATIONS"

    // $ANTLR start "IDENTIFIER"
    public final void mIDENTIFIER() throws RecognitionException {
        try {
            int _type = IDENTIFIER;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincLexer.g:91:5: ( ( 'a' .. 'z' | 'A' .. 'Z' | '_' ) ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '_' )* )
            // parser/flatzinc/FlatzincLexer.g:91:9: ( 'a' .. 'z' | 'A' .. 'Z' | '_' ) ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '_' )*
            {
                if ((input.LA(1) >= 'A' && input.LA(1) <= 'Z') || input.LA(1) == '_' || (input.LA(1) >= 'a' && input.LA(1) <= 'z')) {
                    input.consume();
                } else {
                    MismatchedSetException mse = new MismatchedSetException(null, input);
                    recover(mse);
                    throw mse;
                }


                // parser/flatzinc/FlatzincLexer.g:91:33: ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '_' )*
                loop1:
                do {
                    int alt1 = 2;
                    switch (input.LA(1)) {
                        case '0':
                        case '1':
                        case '2':
                        case '3':
                        case '4':
                        case '5':
                        case '6':
                        case '7':
                        case '8':
                        case '9':
                        case 'A':
                        case 'B':
                        case 'C':
                        case 'D':
                        case 'E':
                        case 'F':
                        case 'G':
                        case 'H':
                        case 'I':
                        case 'J':
                        case 'K':
                        case 'L':
                        case 'M':
                        case 'N':
                        case 'O':
                        case 'P':
                        case 'Q':
                        case 'R':
                        case 'S':
                        case 'T':
                        case 'U':
                        case 'V':
                        case 'W':
                        case 'X':
                        case 'Y':
                        case 'Z':
                        case '_':
                        case 'a':
                        case 'b':
                        case 'c':
                        case 'd':
                        case 'e':
                        case 'f':
                        case 'g':
                        case 'h':
                        case 'i':
                        case 'j':
                        case 'k':
                        case 'l':
                        case 'm':
                        case 'n':
                        case 'o':
                        case 'p':
                        case 'q':
                        case 'r':
                        case 's':
                        case 't':
                        case 'u':
                        case 'v':
                        case 'w':
                        case 'x':
                        case 'y':
                        case 'z': {
                            alt1 = 1;
                        }
                        break;

                    }

                    switch (alt1) {
                        case 1:
                            // parser/flatzinc/FlatzincLexer.g:
                        {
                            if ((input.LA(1) >= '0' && input.LA(1) <= '9') || (input.LA(1) >= 'A' && input.LA(1) <= 'Z') || input.LA(1) == '_' || (input.LA(1) >= 'a' && input.LA(1) <= 'z')) {
                                input.consume();
                            } else {
                                MismatchedSetException mse = new MismatchedSetException(null, input);
                                recover(mse);
                                throw mse;
                            }


                        }
                        break;

                        default:
                            break loop1;
                    }
                } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        } finally {
            // do for sure before leaving
        }
    }
    // $ANTLR end "IDENTIFIER"

    // $ANTLR start "COMMENT"
    public final void mCOMMENT() throws RecognitionException {
        try {
            int _type = COMMENT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincLexer.g:96:5: ( '%' (~ ( '\\n' | '\\r' ) )* ( '\\r' )? '\\n' )
            // parser/flatzinc/FlatzincLexer.g:96:9: '%' (~ ( '\\n' | '\\r' ) )* ( '\\r' )? '\\n'
            {
                match('%');

                // parser/flatzinc/FlatzincLexer.g:96:13: (~ ( '\\n' | '\\r' ) )*
                loop2:
                do {
                    int alt2 = 2;
                    int LA2_0 = input.LA(1);

                    if (((LA2_0 >= '\u0000' && LA2_0 <= '\t') || (LA2_0 >= '\u000B' && LA2_0 <= '\f') || (LA2_0 >= '\u000E' && LA2_0 <= '\uFFFF'))) {
                        alt2 = 1;
                    }


                    switch (alt2) {
                        case 1:
                            // parser/flatzinc/FlatzincLexer.g:
                        {
                            if ((input.LA(1) >= '\u0000' && input.LA(1) <= '\t') || (input.LA(1) >= '\u000B' && input.LA(1) <= '\f') || (input.LA(1) >= '\u000E' && input.LA(1) <= '\uFFFF')) {
                                input.consume();
                            } else {
                                MismatchedSetException mse = new MismatchedSetException(null, input);
                                recover(mse);
                                throw mse;
                            }


                        }
                        break;

                        default:
                            break loop2;
                    }
                } while (true);


                // parser/flatzinc/FlatzincLexer.g:96:27: ( '\\r' )?
                int alt3 = 2;
                switch (input.LA(1)) {
                    case '\r': {
                        alt3 = 1;
                    }
                    break;
                }

                switch (alt3) {
                    case 1:
                        // parser/flatzinc/FlatzincLexer.g:96:27: '\\r'
                    {
                        match('\r');

                    }
                    break;

                }


                match('\n');

                _channel = HIDDEN;

            }

            state.type = _type;
            state.channel = _channel;
        } finally {
            // do for sure before leaving
        }
    }
    // $ANTLR end "COMMENT"

    // $ANTLR start "WS"
    public final void mWS() throws RecognitionException {
        try {
            int _type = WS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincLexer.g:99:5: ( ( ' ' | '\\t' | '\\r' | '\\n' ) )
            // parser/flatzinc/FlatzincLexer.g:99:9: ( ' ' | '\\t' | '\\r' | '\\n' )
            {
                if ((input.LA(1) >= '\t' && input.LA(1) <= '\n') || input.LA(1) == '\r' || input.LA(1) == ' ') {
                    input.consume();
                } else {
                    MismatchedSetException mse = new MismatchedSetException(null, input);
                    recover(mse);
                    throw mse;
                }


                _channel = HIDDEN;

            }

            state.type = _type;
            state.channel = _channel;
        } finally {
            // do for sure before leaving
        }
    }
    // $ANTLR end "WS"

    // $ANTLR start "INT_CONST"
    public final void mINT_CONST() throws RecognitionException {
        try {
            int _type = INT_CONST;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincLexer.g:111:5: ( ( '+' | '-' )? ( '0' .. '9' )+ )
            // parser/flatzinc/FlatzincLexer.g:111:9: ( '+' | '-' )? ( '0' .. '9' )+
            {
                // parser/flatzinc/FlatzincLexer.g:111:9: ( '+' | '-' )?
                int alt4 = 2;
                switch (input.LA(1)) {
                    case '+':
                    case '-': {
                        alt4 = 1;
                    }
                    break;
                }

                switch (alt4) {
                    case 1:
                        // parser/flatzinc/FlatzincLexer.g:
                    {
                        if (input.LA(1) == '+' || input.LA(1) == '-') {
                            input.consume();
                        } else {
                            MismatchedSetException mse = new MismatchedSetException(null, input);
                            recover(mse);
                            throw mse;
                        }


                    }
                    break;

                }


                // parser/flatzinc/FlatzincLexer.g:111:20: ( '0' .. '9' )+
                int cnt5 = 0;
                loop5:
                do {
                    int alt5 = 2;
                    switch (input.LA(1)) {
                        case '0':
                        case '1':
                        case '2':
                        case '3':
                        case '4':
                        case '5':
                        case '6':
                        case '7':
                        case '8':
                        case '9': {
                            alt5 = 1;
                        }
                        break;

                    }

                    switch (alt5) {
                        case 1:
                            // parser/flatzinc/FlatzincLexer.g:
                        {
                            if ((input.LA(1) >= '0' && input.LA(1) <= '9')) {
                                input.consume();
                            } else {
                                MismatchedSetException mse = new MismatchedSetException(null, input);
                                recover(mse);
                                throw mse;
                            }


                        }
                        break;

                        default:
                            if (cnt5 >= 1) break loop5;
                            EarlyExitException eee =
                                    new EarlyExitException(5, input);
                            throw eee;
                    }
                    cnt5++;
                } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        } finally {
            // do for sure before leaving
        }
    }
    // $ANTLR end "INT_CONST"

    // $ANTLR start "STRING"
    public final void mSTRING() throws RecognitionException {
        try {
            int _type = STRING;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincLexer.g:121:5: ( '\"' ( ESC_SEQ |~ ( '\\\\' | '\"' ) )* '\"' )
            // parser/flatzinc/FlatzincLexer.g:121:8: '\"' ( ESC_SEQ |~ ( '\\\\' | '\"' ) )* '\"'
            {
                match('\"');

                // parser/flatzinc/FlatzincLexer.g:121:12: ( ESC_SEQ |~ ( '\\\\' | '\"' ) )*
                loop6:
                do {
                    int alt6 = 3;
                    int LA6_0 = input.LA(1);

                    if ((LA6_0 == '\\')) {
                        alt6 = 1;
                    } else if (((LA6_0 >= '\u0000' && LA6_0 <= '!') || (LA6_0 >= '#' && LA6_0 <= '[') || (LA6_0 >= ']' && LA6_0 <= '\uFFFF'))) {
                        alt6 = 2;
                    }


                    switch (alt6) {
                        case 1:
                            // parser/flatzinc/FlatzincLexer.g:121:14: ESC_SEQ
                        {
                            mESC_SEQ();


                        }
                        break;
                        case 2:
                            // parser/flatzinc/FlatzincLexer.g:121:24: ~ ( '\\\\' | '\"' )
                        {
                            if ((input.LA(1) >= '\u0000' && input.LA(1) <= '!') || (input.LA(1) >= '#' && input.LA(1) <= '[') || (input.LA(1) >= ']' && input.LA(1) <= '\uFFFF')) {
                                input.consume();
                            } else {
                                MismatchedSetException mse = new MismatchedSetException(null, input);
                                recover(mse);
                                throw mse;
                            }


                        }
                        break;

                        default:
                            break loop6;
                    }
                } while (true);


                match('\"');

            }

            state.type = _type;
            state.channel = _channel;
        } finally {
            // do for sure before leaving
        }
    }
    // $ANTLR end "STRING"

    // $ANTLR start "CHAR"
    public final void mCHAR() throws RecognitionException {
        try {
            int _type = CHAR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincLexer.g:124:5: ( '\\'' ( ESC_SEQ |~ ( '\\'' | '\\\\' ) ) '\\'' )
            // parser/flatzinc/FlatzincLexer.g:124:8: '\\'' ( ESC_SEQ |~ ( '\\'' | '\\\\' ) ) '\\''
            {
                match('\'');

                // parser/flatzinc/FlatzincLexer.g:124:13: ( ESC_SEQ |~ ( '\\'' | '\\\\' ) )
                int alt7 = 2;
                int LA7_0 = input.LA(1);

                if ((LA7_0 == '\\')) {
                    alt7 = 1;
                } else if (((LA7_0 >= '\u0000' && LA7_0 <= '&') || (LA7_0 >= '(' && LA7_0 <= '[') || (LA7_0 >= ']' && LA7_0 <= '\uFFFF'))) {
                    alt7 = 2;
                } else {
                    NoViableAltException nvae =
                            new NoViableAltException("", 7, 0, input);

                    throw nvae;

                }
                switch (alt7) {
                    case 1:
                        // parser/flatzinc/FlatzincLexer.g:124:15: ESC_SEQ
                    {
                        mESC_SEQ();


                    }
                    break;
                    case 2:
                        // parser/flatzinc/FlatzincLexer.g:124:25: ~ ( '\\'' | '\\\\' )
                    {
                        if ((input.LA(1) >= '\u0000' && input.LA(1) <= '&') || (input.LA(1) >= '(' && input.LA(1) <= '[') || (input.LA(1) >= ']' && input.LA(1) <= '\uFFFF')) {
                            input.consume();
                        } else {
                            MismatchedSetException mse = new MismatchedSetException(null, input);
                            recover(mse);
                            throw mse;
                        }


                    }
                    break;

                }


                match('\'');

            }

            state.type = _type;
            state.channel = _channel;
        } finally {
            // do for sure before leaving
        }
    }
    // $ANTLR end "CHAR"

    // $ANTLR start "EXPONENT"
    public final void mEXPONENT() throws RecognitionException {
        try {
            // parser/flatzinc/FlatzincLexer.g:133:10: ( ( 'e' | 'E' ) ( '+' | '-' )? ( '0' .. '9' )+ )
            // parser/flatzinc/FlatzincLexer.g:133:12: ( 'e' | 'E' ) ( '+' | '-' )? ( '0' .. '9' )+
            {
                if (input.LA(1) == 'E' || input.LA(1) == 'e') {
                    input.consume();
                } else {
                    MismatchedSetException mse = new MismatchedSetException(null, input);
                    recover(mse);
                    throw mse;
                }


                // parser/flatzinc/FlatzincLexer.g:133:22: ( '+' | '-' )?
                int alt8 = 2;
                switch (input.LA(1)) {
                    case '+':
                    case '-': {
                        alt8 = 1;
                    }
                    break;
                }

                switch (alt8) {
                    case 1:
                        // parser/flatzinc/FlatzincLexer.g:
                    {
                        if (input.LA(1) == '+' || input.LA(1) == '-') {
                            input.consume();
                        } else {
                            MismatchedSetException mse = new MismatchedSetException(null, input);
                            recover(mse);
                            throw mse;
                        }


                    }
                    break;

                }


                // parser/flatzinc/FlatzincLexer.g:133:33: ( '0' .. '9' )+
                int cnt9 = 0;
                loop9:
                do {
                    int alt9 = 2;
                    switch (input.LA(1)) {
                        case '0':
                        case '1':
                        case '2':
                        case '3':
                        case '4':
                        case '5':
                        case '6':
                        case '7':
                        case '8':
                        case '9': {
                            alt9 = 1;
                        }
                        break;

                    }

                    switch (alt9) {
                        case 1:
                            // parser/flatzinc/FlatzincLexer.g:
                        {
                            if ((input.LA(1) >= '0' && input.LA(1) <= '9')) {
                                input.consume();
                            } else {
                                MismatchedSetException mse = new MismatchedSetException(null, input);
                                recover(mse);
                                throw mse;
                            }


                        }
                        break;

                        default:
                            if (cnt9 >= 1) break loop9;
                            EarlyExitException eee =
                                    new EarlyExitException(9, input);
                            throw eee;
                    }
                    cnt9++;
                } while (true);


            }


        } finally {
            // do for sure before leaving
        }
    }
    // $ANTLR end "EXPONENT"

    // $ANTLR start "ESC_SEQ"
    public final void mESC_SEQ() throws RecognitionException {
        try {
            // parser/flatzinc/FlatzincLexer.g:137:5: ( '\\\\' ( 'b' | 't' | 'n' | 'f' | 'r' | '\\\"' | '\\'' | '\\\\' ) | UNICODE_ESC | OCTAL_ESC )
            int alt10 = 3;
            switch (input.LA(1)) {
                case '\\': {
                    switch (input.LA(2)) {
                        case '\"':
                        case '\'':
                        case '\\':
                        case 'b':
                        case 'f':
                        case 'n':
                        case 'r':
                        case 't': {
                            alt10 = 1;
                        }
                        break;
                        case 'u': {
                            alt10 = 2;
                        }
                        break;
                        case '0':
                        case '1':
                        case '2':
                        case '3':
                        case '4':
                        case '5':
                        case '6':
                        case '7': {
                            alt10 = 3;
                        }
                        break;
                        default:
                            NoViableAltException nvae =
                                    new NoViableAltException("", 10, 1, input);

                            throw nvae;

                    }

                }
                break;
                default:
                    NoViableAltException nvae =
                            new NoViableAltException("", 10, 0, input);

                    throw nvae;

            }

            switch (alt10) {
                case 1:
                    // parser/flatzinc/FlatzincLexer.g:137:9: '\\\\' ( 'b' | 't' | 'n' | 'f' | 'r' | '\\\"' | '\\'' | '\\\\' )
                {
                    match('\\');

                    if (input.LA(1) == '\"' || input.LA(1) == '\'' || input.LA(1) == '\\' || input.LA(1) == 'b' || input.LA(1) == 'f' || input.LA(1) == 'n' || input.LA(1) == 'r' || input.LA(1) == 't') {
                        input.consume();
                    } else {
                        MismatchedSetException mse = new MismatchedSetException(null, input);
                        recover(mse);
                        throw mse;
                    }


                }
                break;
                case 2:
                    // parser/flatzinc/FlatzincLexer.g:138:9: UNICODE_ESC
                {
                    mUNICODE_ESC();


                }
                break;
                case 3:
                    // parser/flatzinc/FlatzincLexer.g:139:9: OCTAL_ESC
                {
                    mOCTAL_ESC();


                }
                break;

            }

        } finally {
            // do for sure before leaving
        }
    }
    // $ANTLR end "ESC_SEQ"

    // $ANTLR start "OCTAL_ESC"
    public final void mOCTAL_ESC() throws RecognitionException {
        try {
            // parser/flatzinc/FlatzincLexer.g:144:5: ( '\\\\' ( '0' .. '3' ) ( '0' .. '7' ) ( '0' .. '7' ) | '\\\\' ( '0' .. '7' ) ( '0' .. '7' ) | '\\\\' ( '0' .. '7' ) )
            int alt11 = 3;
            switch (input.LA(1)) {
                case '\\': {
                    switch (input.LA(2)) {
                        case '0':
                        case '1':
                        case '2':
                        case '3': {
                            switch (input.LA(3)) {
                                case '0':
                                case '1':
                                case '2':
                                case '3':
                                case '4':
                                case '5':
                                case '6':
                                case '7': {
                                    switch (input.LA(4)) {
                                        case '0':
                                        case '1':
                                        case '2':
                                        case '3':
                                        case '4':
                                        case '5':
                                        case '6':
                                        case '7': {
                                            alt11 = 1;
                                        }
                                        break;
                                        default:
                                            alt11 = 2;
                                    }

                                }
                                break;
                                default:
                                    alt11 = 3;
                            }

                        }
                        break;
                        case '4':
                        case '5':
                        case '6':
                        case '7': {
                            switch (input.LA(3)) {
                                case '0':
                                case '1':
                                case '2':
                                case '3':
                                case '4':
                                case '5':
                                case '6':
                                case '7': {
                                    alt11 = 2;
                                }
                                break;
                                default:
                                    alt11 = 3;
                            }

                        }
                        break;
                        default:
                            NoViableAltException nvae =
                                    new NoViableAltException("", 11, 1, input);

                            throw nvae;

                    }

                }
                break;
                default:
                    NoViableAltException nvae =
                            new NoViableAltException("", 11, 0, input);

                    throw nvae;

            }

            switch (alt11) {
                case 1:
                    // parser/flatzinc/FlatzincLexer.g:144:9: '\\\\' ( '0' .. '3' ) ( '0' .. '7' ) ( '0' .. '7' )
                {
                    match('\\');

                    if ((input.LA(1) >= '0' && input.LA(1) <= '3')) {
                        input.consume();
                    } else {
                        MismatchedSetException mse = new MismatchedSetException(null, input);
                        recover(mse);
                        throw mse;
                    }


                    if ((input.LA(1) >= '0' && input.LA(1) <= '7')) {
                        input.consume();
                    } else {
                        MismatchedSetException mse = new MismatchedSetException(null, input);
                        recover(mse);
                        throw mse;
                    }


                    if ((input.LA(1) >= '0' && input.LA(1) <= '7')) {
                        input.consume();
                    } else {
                        MismatchedSetException mse = new MismatchedSetException(null, input);
                        recover(mse);
                        throw mse;
                    }


                }
                break;
                case 2:
                    // parser/flatzinc/FlatzincLexer.g:145:9: '\\\\' ( '0' .. '7' ) ( '0' .. '7' )
                {
                    match('\\');

                    if ((input.LA(1) >= '0' && input.LA(1) <= '7')) {
                        input.consume();
                    } else {
                        MismatchedSetException mse = new MismatchedSetException(null, input);
                        recover(mse);
                        throw mse;
                    }


                    if ((input.LA(1) >= '0' && input.LA(1) <= '7')) {
                        input.consume();
                    } else {
                        MismatchedSetException mse = new MismatchedSetException(null, input);
                        recover(mse);
                        throw mse;
                    }


                }
                break;
                case 3:
                    // parser/flatzinc/FlatzincLexer.g:146:9: '\\\\' ( '0' .. '7' )
                {
                    match('\\');

                    if ((input.LA(1) >= '0' && input.LA(1) <= '7')) {
                        input.consume();
                    } else {
                        MismatchedSetException mse = new MismatchedSetException(null, input);
                        recover(mse);
                        throw mse;
                    }


                }
                break;

            }

        } finally {
            // do for sure before leaving
        }
    }
    // $ANTLR end "OCTAL_ESC"

    // $ANTLR start "HEX_DIGIT"
    public final void mHEX_DIGIT() throws RecognitionException {
        try {
            // parser/flatzinc/FlatzincLexer.g:150:11: ( ( '0' .. '9' | 'a' .. 'f' | 'A' .. 'F' ) )
            // parser/flatzinc/FlatzincLexer.g:
            {
                if ((input.LA(1) >= '0' && input.LA(1) <= '9') || (input.LA(1) >= 'A' && input.LA(1) <= 'F') || (input.LA(1) >= 'a' && input.LA(1) <= 'f')) {
                    input.consume();
                } else {
                    MismatchedSetException mse = new MismatchedSetException(null, input);
                    recover(mse);
                    throw mse;
                }


            }


        } finally {
            // do for sure before leaving
        }
    }
    // $ANTLR end "HEX_DIGIT"

    // $ANTLR start "UNICODE_ESC"
    public final void mUNICODE_ESC() throws RecognitionException {
        try {
            // parser/flatzinc/FlatzincLexer.g:154:5: ( '\\\\' 'u' HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT )
            // parser/flatzinc/FlatzincLexer.g:154:9: '\\\\' 'u' HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT
            {
                match('\\');

                match('u');

                mHEX_DIGIT();


                mHEX_DIGIT();


                mHEX_DIGIT();


                mHEX_DIGIT();


            }


        } finally {
            // do for sure before leaving
        }
    }
    // $ANTLR end "UNICODE_ESC"

    public void mTokens() throws RecognitionException {
        // parser/flatzinc/FlatzincLexer.g:1:8: ( BOOL | TRUE | FALSE | INT | FLOAT | SET | OF | ARRAY | VAR | PAR | PREDICATE | CONSTRAINT | SOLVE | SATISFY | MINIMIZE | MAXIMIZE | DD | DO | LB | RB | CM | LS | RS | EQ | PL | MN | SC | CL | DC | LP | RP | APAR | ARRPAR | AVAR | ARRVAR | INDEX | EXPR | ANNOTATIONS | IDENTIFIER | COMMENT | WS | INT_CONST | STRING | CHAR )
        int alt12 = 44;
        alt12 = dfa12.predict(input);
        switch (alt12) {
            case 1:
                // parser/flatzinc/FlatzincLexer.g:1:10: BOOL
            {
                mBOOL();


            }
            break;
            case 2:
                // parser/flatzinc/FlatzincLexer.g:1:15: TRUE
            {
                mTRUE();


            }
            break;
            case 3:
                // parser/flatzinc/FlatzincLexer.g:1:20: FALSE
            {
                mFALSE();


            }
            break;
            case 4:
                // parser/flatzinc/FlatzincLexer.g:1:26: INT
            {
                mINT();


            }
            break;
            case 5:
                // parser/flatzinc/FlatzincLexer.g:1:30: FLOAT
            {
                mFLOAT();


            }
            break;
            case 6:
                // parser/flatzinc/FlatzincLexer.g:1:36: SET
            {
                mSET();


            }
            break;
            case 7:
                // parser/flatzinc/FlatzincLexer.g:1:40: OF
            {
                mOF();


            }
            break;
            case 8:
                // parser/flatzinc/FlatzincLexer.g:1:43: ARRAY
            {
                mARRAY();


            }
            break;
            case 9:
                // parser/flatzinc/FlatzincLexer.g:1:49: VAR
            {
                mVAR();


            }
            break;
            case 10:
                // parser/flatzinc/FlatzincLexer.g:1:53: PAR
            {
                mPAR();


            }
            break;
            case 11:
                // parser/flatzinc/FlatzincLexer.g:1:57: PREDICATE
            {
                mPREDICATE();


            }
            break;
            case 12:
                // parser/flatzinc/FlatzincLexer.g:1:67: CONSTRAINT
            {
                mCONSTRAINT();


            }
            break;
            case 13:
                // parser/flatzinc/FlatzincLexer.g:1:78: SOLVE
            {
                mSOLVE();


            }
            break;
            case 14:
                // parser/flatzinc/FlatzincLexer.g:1:84: SATISFY
            {
                mSATISFY();


            }
            break;
            case 15:
                // parser/flatzinc/FlatzincLexer.g:1:92: MINIMIZE
            {
                mMINIMIZE();


            }
            break;
            case 16:
                // parser/flatzinc/FlatzincLexer.g:1:101: MAXIMIZE
            {
                mMAXIMIZE();


            }
            break;
            case 17:
                // parser/flatzinc/FlatzincLexer.g:1:110: DD
            {
                mDD();


            }
            break;
            case 18:
                // parser/flatzinc/FlatzincLexer.g:1:113: DO
            {
                mDO();


            }
            break;
            case 19:
                // parser/flatzinc/FlatzincLexer.g:1:116: LB
            {
                mLB();


            }
            break;
            case 20:
                // parser/flatzinc/FlatzincLexer.g:1:119: RB
            {
                mRB();


            }
            break;
            case 21:
                // parser/flatzinc/FlatzincLexer.g:1:122: CM
            {
                mCM();


            }
            break;
            case 22:
                // parser/flatzinc/FlatzincLexer.g:1:125: LS
            {
                mLS();


            }
            break;
            case 23:
                // parser/flatzinc/FlatzincLexer.g:1:128: RS
            {
                mRS();


            }
            break;
            case 24:
                // parser/flatzinc/FlatzincLexer.g:1:131: EQ
            {
                mEQ();


            }
            break;
            case 25:
                // parser/flatzinc/FlatzincLexer.g:1:134: PL
            {
                mPL();


            }
            break;
            case 26:
                // parser/flatzinc/FlatzincLexer.g:1:137: MN
            {
                mMN();


            }
            break;
            case 27:
                // parser/flatzinc/FlatzincLexer.g:1:140: SC
            {
                mSC();


            }
            break;
            case 28:
                // parser/flatzinc/FlatzincLexer.g:1:143: CL
            {
                mCL();


            }
            break;
            case 29:
                // parser/flatzinc/FlatzincLexer.g:1:146: DC
            {
                mDC();


            }
            break;
            case 30:
                // parser/flatzinc/FlatzincLexer.g:1:149: LP
            {
                mLP();


            }
            break;
            case 31:
                // parser/flatzinc/FlatzincLexer.g:1:152: RP
            {
                mRP();


            }
            break;
            case 32:
                // parser/flatzinc/FlatzincLexer.g:1:155: APAR
            {
                mAPAR();


            }
            break;
            case 33:
                // parser/flatzinc/FlatzincLexer.g:1:160: ARRPAR
            {
                mARRPAR();


            }
            break;
            case 34:
                // parser/flatzinc/FlatzincLexer.g:1:167: AVAR
            {
                mAVAR();


            }
            break;
            case 35:
                // parser/flatzinc/FlatzincLexer.g:1:172: ARRVAR
            {
                mARRVAR();


            }
            break;
            case 36:
                // parser/flatzinc/FlatzincLexer.g:1:179: INDEX
            {
                mINDEX();


            }
            break;
            case 37:
                // parser/flatzinc/FlatzincLexer.g:1:185: EXPR
            {
                mEXPR();


            }
            break;
            case 38:
                // parser/flatzinc/FlatzincLexer.g:1:190: ANNOTATIONS
            {
                mANNOTATIONS();


            }
            break;
            case 39:
                // parser/flatzinc/FlatzincLexer.g:1:202: IDENTIFIER
            {
                mIDENTIFIER();


            }
            break;
            case 40:
                // parser/flatzinc/FlatzincLexer.g:1:213: COMMENT
            {
                mCOMMENT();


            }
            break;
            case 41:
                // parser/flatzinc/FlatzincLexer.g:1:221: WS
            {
                mWS();


            }
            break;
            case 42:
                // parser/flatzinc/FlatzincLexer.g:1:224: INT_CONST
            {
                mINT_CONST();


            }
            break;
            case 43:
                // parser/flatzinc/FlatzincLexer.g:1:234: STRING
            {
                mSTRING();


            }
            break;
            case 44:
                // parser/flatzinc/FlatzincLexer.g:1:241: CHAR
            {
                mCHAR();


            }
            break;

        }

    }


    protected DFA12 dfa12 = new DFA12(this);
    static final String DFA12_eotS =
            "\1\uffff\13\32\1\61\6\uffff\1\62\1\63\1\uffff\1\65\11\uffff\10\32" +
                    "\1\77\7\32\7\uffff\4\32\1\114\1\115\2\32\1\uffff\1\32\1\121\1\122" +
                    "\4\32\1\uffff\1\133\1\134\2\32\2\uffff\3\32\2\uffff\4\32\6\uffff" +
                    "\1\153\1\154\1\155\1\32\1\157\4\32\10\uffff\1\32\1\uffff\4\32\1" +
                    "\171\4\32\1\uffff\2\32\1\u0080\1\u0081\1\u0082\1\32\3\uffff\1\u0084" +
                    "\1\uffff";
    static final String DFA12_eofS =
            "\u0085\uffff";
    static final String DFA12_minS =
            "\1\11\1\157\1\162\1\141\1\156\1\141\1\146\1\162\2\141\1\157\1\141" +
                    "\1\56\6\uffff\2\60\1\uffff\1\72\2\uffff\1\43\6\uffff\1\157\1\165" +
                    "\1\154\1\157\2\164\1\154\1\164\1\60\3\162\1\145\2\156\1\170\6\uffff" +
                    "\1\43\1\154\1\145\1\163\1\141\2\60\1\166\1\151\1\uffff\1\141\2\60" +
                    "\1\144\1\163\2\151\1\101\2\60\1\145\1\164\2\uffff\1\145\1\163\1" +
                    "\171\2\uffff\1\151\1\164\2\155\2\120\4\uffff\3\60\1\146\1\60\1\143" +
                    "\1\162\2\151\10\uffff\1\171\1\uffff\2\141\2\172\1\60\1\164\1\151" +
                    "\2\145\1\uffff\1\145\1\156\3\60\1\164\3\uffff\1\60\1\uffff";
    static final String DFA12_maxS =
            "\1\175\1\157\1\162\1\154\1\156\1\157\1\146\1\162\1\141\1\162\1\157" +
                    "\1\151\1\56\6\uffff\2\71\1\uffff\1\72\2\uffff\1\43\6\uffff\1\157" +
                    "\1\165\1\154\1\157\2\164\1\154\1\164\1\172\3\162\1\145\2\156\1\170" +
                    "\6\uffff\1\43\1\154\1\145\1\163\1\141\2\172\1\166\1\151\1\uffff" +
                    "\1\141\2\172\1\144\1\163\2\151\1\137\2\172\1\145\1\164\2\uffff\1" +
                    "\145\1\163\1\171\2\uffff\1\151\1\164\2\155\2\126\4\uffff\3\172\1" +
                    "\146\1\172\1\143\1\162\2\151\10\uffff\1\171\1\uffff\2\141\3\172" +
                    "\1\164\1\151\2\145\1\uffff\1\145\1\156\3\172\1\164\3\uffff\1\172" +
                    "\1\uffff";
    static final String DFA12_acceptS =
            "\15\uffff\1\23\1\24\1\25\1\26\1\27\1\30\2\uffff\1\33\1\uffff\1\36" +
                    "\1\37\1\uffff\1\47\1\50\1\51\1\52\1\53\1\54\20\uffff\1\21\1\22\1" +
                    "\31\1\32\1\35\1\34\11\uffff\1\7\14\uffff\1\4\1\6\3\uffff\1\11\1" +
                    "\12\6\uffff\1\44\1\45\1\1\1\2\11\uffff\1\40\1\42\1\41\1\43\1\46" +
                    "\1\3\1\5\1\15\1\uffff\1\10\11\uffff\1\16\6\uffff\1\17\1\20\1\13" +
                    "\1\uffff\1\14";
    static final String DFA12_specialS =
            "\u0085\uffff}>";
    static final String[] DFA12_transitionS = {
            "\2\34\2\uffff\1\34\22\uffff\1\34\1\uffff\1\36\1\31\1\uffff\1" +
                    "\33\1\uffff\1\37\1\27\1\30\1\uffff\1\23\1\17\1\24\1\14\1\uffff" +
                    "\12\35\1\26\1\25\1\uffff\1\22\3\uffff\32\32\1\20\1\uffff\1\21" +
                    "\1\uffff\1\32\1\uffff\1\7\1\1\1\12\2\32\1\3\2\32\1\4\3\32\1" +
                    "\13\1\32\1\6\1\11\2\32\1\5\1\2\1\32\1\10\4\32\1\15\1\uffff\1" +
                    "\16",
            "\1\40",
            "\1\41",
            "\1\42\12\uffff\1\43",
            "\1\44",
            "\1\47\3\uffff\1\45\11\uffff\1\46",
            "\1\50",
            "\1\51",
            "\1\52",
            "\1\53\20\uffff\1\54",
            "\1\55",
            "\1\57\7\uffff\1\56",
            "\1\60",
            "",
            "",
            "",
            "",
            "",
            "",
            "\12\35",
            "\12\35",
            "",
            "\1\64",
            "",
            "",
            "\1\66",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\67",
            "\1\70",
            "\1\71",
            "\1\72",
            "\1\73",
            "\1\74",
            "\1\75",
            "\1\76",
            "\12\32\7\uffff\32\32\4\uffff\1\32\1\uffff\32\32",
            "\1\100",
            "\1\101",
            "\1\102",
            "\1\103",
            "\1\104",
            "\1\105",
            "\1\106",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\107",
            "\1\110",
            "\1\111",
            "\1\112",
            "\1\113",
            "\12\32\7\uffff\32\32\4\uffff\1\32\1\uffff\32\32",
            "\12\32\7\uffff\32\32\4\uffff\1\32\1\uffff\32\32",
            "\1\116",
            "\1\117",
            "",
            "\1\120",
            "\12\32\7\uffff\32\32\4\uffff\1\32\1\uffff\32\32",
            "\12\32\7\uffff\32\32\4\uffff\1\32\1\uffff\32\32",
            "\1\123",
            "\1\124",
            "\1\125",
            "\1\126",
            "\1\130\3\uffff\1\132\3\uffff\1\131\25\uffff\1\127",
            "\12\32\7\uffff\32\32\4\uffff\1\32\1\uffff\32\32",
            "\12\32\7\uffff\32\32\4\uffff\1\32\1\uffff\32\32",
            "\1\135",
            "\1\136",
            "",
            "",
            "\1\137",
            "\1\140",
            "\1\141",
            "",
            "",
            "\1\142",
            "\1\143",
            "\1\144",
            "\1\145",
            "\1\146\5\uffff\1\147",
            "\1\150\2\uffff\1\152\2\uffff\1\151",
            "",
            "",
            "",
            "",
            "\12\32\7\uffff\32\32\4\uffff\1\32\1\uffff\32\32",
            "\12\32\7\uffff\32\32\4\uffff\1\32\1\uffff\32\32",
            "\12\32\7\uffff\32\32\4\uffff\1\32\1\uffff\32\32",
            "\1\156",
            "\12\32\7\uffff\32\32\4\uffff\1\32\1\uffff\32\32",
            "\1\160",
            "\1\161",
            "\1\162",
            "\1\163",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\164",
            "",
            "\1\165",
            "\1\166",
            "\1\167",
            "\1\170",
            "\12\32\7\uffff\32\32\4\uffff\1\32\1\uffff\32\32",
            "\1\172",
            "\1\173",
            "\1\174",
            "\1\175",
            "",
            "\1\176",
            "\1\177",
            "\12\32\7\uffff\32\32\4\uffff\1\32\1\uffff\32\32",
            "\12\32\7\uffff\32\32\4\uffff\1\32\1\uffff\32\32",
            "\12\32\7\uffff\32\32\4\uffff\1\32\1\uffff\32\32",
            "\1\u0083",
            "",
            "",
            "",
            "\12\32\7\uffff\32\32\4\uffff\1\32\1\uffff\32\32",
            ""
    };

    static final short[] DFA12_eot = DFA.unpackEncodedString(DFA12_eotS);
    static final short[] DFA12_eof = DFA.unpackEncodedString(DFA12_eofS);
    static final char[] DFA12_min = DFA.unpackEncodedStringToUnsignedChars(DFA12_minS);
    static final char[] DFA12_max = DFA.unpackEncodedStringToUnsignedChars(DFA12_maxS);
    static final short[] DFA12_accept = DFA.unpackEncodedString(DFA12_acceptS);
    static final short[] DFA12_special = DFA.unpackEncodedString(DFA12_specialS);
    static final short[][] DFA12_transition;

    static {
        int numStates = DFA12_transitionS.length;
        DFA12_transition = new short[numStates][];
        for (int i = 0; i < numStates; i++) {
            DFA12_transition[i] = DFA.unpackEncodedString(DFA12_transitionS[i]);
        }
    }

    class DFA12 extends DFA {

        public DFA12(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 12;
            this.eot = DFA12_eot;
            this.eof = DFA12_eof;
            this.min = DFA12_min;
            this.max = DFA12_max;
            this.accept = DFA12_accept;
            this.special = DFA12_special;
            this.transition = DFA12_transition;
        }

        public String getDescription() {
            return "1:1: Tokens : ( BOOL | TRUE | FALSE | INT | FLOAT | SET | OF | ARRAY | VAR | PAR | PREDICATE | CONSTRAINT | SOLVE | SATISFY | MINIMIZE | MAXIMIZE | DD | DO | LB | RB | CM | LS | RS | EQ | PL | MN | SC | CL | DC | LP | RP | APAR | ARRPAR | AVAR | ARRVAR | INDEX | EXPR | ANNOTATIONS | IDENTIFIER | COMMENT | WS | INT_CONST | STRING | CHAR );";
        }
    }


}