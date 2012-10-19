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

// $ANTLR 3.4 parser/flatzinc/Flatzinc.g 2012-10-19 09:21:19

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
        return "parser/flatzinc/Flatzinc.g";
    }

    // $ANTLR start "ARRAY"
    public final void mARRAY() throws RecognitionException {
        try {
            int _type = ARRAY;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/Flatzinc.g:11:7: ( 'array' )
            // parser/flatzinc/Flatzinc.g:11:9: 'array'
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

    // $ANTLR start "BOOL"
    public final void mBOOL() throws RecognitionException {
        try {
            int _type = BOOL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/Flatzinc.g:12:6: ( 'bool' )
            // parser/flatzinc/Flatzinc.g:12:8: 'bool'
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

    // $ANTLR start "CL"
    public final void mCL() throws RecognitionException {
        try {
            int _type = CL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/Flatzinc.g:13:4: ( ':' )
            // parser/flatzinc/Flatzinc.g:13:6: ':'
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

    // $ANTLR start "CM"
    public final void mCM() throws RecognitionException {
        try {
            int _type = CM;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/Flatzinc.g:14:4: ( ',' )
            // parser/flatzinc/Flatzinc.g:14:6: ','
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

    // $ANTLR start "CONSTRAINT"
    public final void mCONSTRAINT() throws RecognitionException {
        try {
            int _type = CONSTRAINT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/Flatzinc.g:15:12: ( 'constraint' )
            // parser/flatzinc/Flatzinc.g:15:14: 'constraint'
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

    // $ANTLR start "DC"
    public final void mDC() throws RecognitionException {
        try {
            int _type = DC;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/Flatzinc.g:16:4: ( '::' )
            // parser/flatzinc/Flatzinc.g:16:6: '::'
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

    // $ANTLR start "DD"
    public final void mDD() throws RecognitionException {
        try {
            int _type = DD;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/Flatzinc.g:17:4: ( '..' )
            // parser/flatzinc/Flatzinc.g:17:6: '..'
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
            // parser/flatzinc/Flatzinc.g:18:4: ( '.' )
            // parser/flatzinc/Flatzinc.g:18:6: '.'
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

    // $ANTLR start "EQ"
    public final void mEQ() throws RecognitionException {
        try {
            int _type = EQ;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/Flatzinc.g:19:4: ( '=' )
            // parser/flatzinc/Flatzinc.g:19:6: '='
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

    // $ANTLR start "FALSE"
    public final void mFALSE() throws RecognitionException {
        try {
            int _type = FALSE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/Flatzinc.g:20:7: ( 'false' )
            // parser/flatzinc/Flatzinc.g:20:9: 'false'
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

    // $ANTLR start "FLOAT"
    public final void mFLOAT() throws RecognitionException {
        try {
            int _type = FLOAT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/Flatzinc.g:21:7: ( 'float' )
            // parser/flatzinc/Flatzinc.g:21:9: 'float'
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

    // $ANTLR start "INT"
    public final void mINT() throws RecognitionException {
        try {
            int _type = INT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/Flatzinc.g:22:5: ( 'int' )
            // parser/flatzinc/Flatzinc.g:22:7: 'int'
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

    // $ANTLR start "LB"
    public final void mLB() throws RecognitionException {
        try {
            int _type = LB;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/Flatzinc.g:23:4: ( '{' )
            // parser/flatzinc/Flatzinc.g:23:6: '{'
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

    // $ANTLR start "LP"
    public final void mLP() throws RecognitionException {
        try {
            int _type = LP;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/Flatzinc.g:24:4: ( '(' )
            // parser/flatzinc/Flatzinc.g:24:6: '('
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

    // $ANTLR start "LS"
    public final void mLS() throws RecognitionException {
        try {
            int _type = LS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/Flatzinc.g:25:4: ( '[' )
            // parser/flatzinc/Flatzinc.g:25:6: '['
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

    // $ANTLR start "MAXIMIZE"
    public final void mMAXIMIZE() throws RecognitionException {
        try {
            int _type = MAXIMIZE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/Flatzinc.g:26:10: ( 'maximize' )
            // parser/flatzinc/Flatzinc.g:26:12: 'maximize'
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

    // $ANTLR start "MINIMIZE"
    public final void mMINIMIZE() throws RecognitionException {
        try {
            int _type = MINIMIZE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/Flatzinc.g:27:10: ( 'minimize' )
            // parser/flatzinc/Flatzinc.g:27:12: 'minimize'
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

    // $ANTLR start "MN"
    public final void mMN() throws RecognitionException {
        try {
            int _type = MN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/Flatzinc.g:28:4: ( '-' )
            // parser/flatzinc/Flatzinc.g:28:6: '-'
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

    // $ANTLR start "OF"
    public final void mOF() throws RecognitionException {
        try {
            int _type = OF;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/Flatzinc.g:29:4: ( 'of' )
            // parser/flatzinc/Flatzinc.g:29:6: 'of'
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

    // $ANTLR start "PAR"
    public final void mPAR() throws RecognitionException {
        try {
            int _type = PAR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/Flatzinc.g:30:5: ( 'par' )
            // parser/flatzinc/Flatzinc.g:30:7: 'par'
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

    // $ANTLR start "PL"
    public final void mPL() throws RecognitionException {
        try {
            int _type = PL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/Flatzinc.g:31:4: ( '+' )
            // parser/flatzinc/Flatzinc.g:31:6: '+'
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

    // $ANTLR start "PREDICATE"
    public final void mPREDICATE() throws RecognitionException {
        try {
            int _type = PREDICATE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/Flatzinc.g:32:11: ( 'predicate' )
            // parser/flatzinc/Flatzinc.g:32:13: 'predicate'
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

    // $ANTLR start "RB"
    public final void mRB() throws RecognitionException {
        try {
            int _type = RB;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/Flatzinc.g:33:4: ( '}' )
            // parser/flatzinc/Flatzinc.g:33:6: '}'
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

    // $ANTLR start "RP"
    public final void mRP() throws RecognitionException {
        try {
            int _type = RP;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/Flatzinc.g:34:4: ( ')' )
            // parser/flatzinc/Flatzinc.g:34:6: ')'
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

    // $ANTLR start "RS"
    public final void mRS() throws RecognitionException {
        try {
            int _type = RS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/Flatzinc.g:35:4: ( ']' )
            // parser/flatzinc/Flatzinc.g:35:6: ']'
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

    // $ANTLR start "SATISFY"
    public final void mSATISFY() throws RecognitionException {
        try {
            int _type = SATISFY;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/Flatzinc.g:36:9: ( 'satisfy' )
            // parser/flatzinc/Flatzinc.g:36:11: 'satisfy'
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

    // $ANTLR start "SC"
    public final void mSC() throws RecognitionException {
        try {
            int _type = SC;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/Flatzinc.g:37:4: ( ';' )
            // parser/flatzinc/Flatzinc.g:37:6: ';'
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

    // $ANTLR start "SET"
    public final void mSET() throws RecognitionException {
        try {
            int _type = SET;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/Flatzinc.g:38:5: ( 'set' )
            // parser/flatzinc/Flatzinc.g:38:7: 'set'
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

    // $ANTLR start "SOLVE"
    public final void mSOLVE() throws RecognitionException {
        try {
            int _type = SOLVE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/Flatzinc.g:39:7: ( 'solve' )
            // parser/flatzinc/Flatzinc.g:39:9: 'solve'
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

    // $ANTLR start "TRUE"
    public final void mTRUE() throws RecognitionException {
        try {
            int _type = TRUE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/Flatzinc.g:40:6: ( 'true' )
            // parser/flatzinc/Flatzinc.g:40:8: 'true'
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

    // $ANTLR start "VAR"
    public final void mVAR() throws RecognitionException {
        try {
            int _type = VAR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/Flatzinc.g:41:5: ( 'var' )
            // parser/flatzinc/Flatzinc.g:41:7: 'var'
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

    // $ANTLR start "APAR"
    public final void mAPAR() throws RecognitionException {
        try {
            int _type = APAR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/Flatzinc.g:78:5: ( '###_P###' )
            // parser/flatzinc/Flatzinc.g:78:10: '###_P###'
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
            // parser/flatzinc/Flatzinc.g:82:5: ( '###AP###' )
            // parser/flatzinc/Flatzinc.g:82:10: '###AP###'
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
            // parser/flatzinc/Flatzinc.g:100:5: ( '###_V###' )
            // parser/flatzinc/Flatzinc.g:100:10: '###_V###'
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
            // parser/flatzinc/Flatzinc.g:104:5: ( '###AV###' )
            // parser/flatzinc/Flatzinc.g:104:10: '###AV###'
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
            // parser/flatzinc/Flatzinc.g:165:5: ( '###ID###' )
            // parser/flatzinc/Flatzinc.g:165:9: '###ID###'
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
            // parser/flatzinc/Flatzinc.g:176:5: ( '###EX###' )
            // parser/flatzinc/Flatzinc.g:176:9: '###EX###'
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
            // parser/flatzinc/Flatzinc.g:224:5: ( '###AS###' )
            // parser/flatzinc/Flatzinc.g:224:9: '###AS###'
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

    // $ANTLR start "INT_CONST"
    public final void mINT_CONST() throws RecognitionException {
        try {
            int _type = INT_CONST;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/Flatzinc.g:239:5: ( ( '+' | '-' )? ( '0' .. '9' )+ )
            // parser/flatzinc/Flatzinc.g:239:9: ( '+' | '-' )? ( '0' .. '9' )+
            {
                // parser/flatzinc/Flatzinc.g:239:9: ( '+' | '-' )?
                int alt1 = 2;
                switch (input.LA(1)) {
                    case '+':
                    case '-': {
                        alt1 = 1;
                    }
                    break;
                }

                switch (alt1) {
                    case 1:
                        // parser/flatzinc/Flatzinc.g:
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


                // parser/flatzinc/Flatzinc.g:239:20: ( '0' .. '9' )+
                int cnt2 = 0;
                loop2:
                do {
                    int alt2 = 2;
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
                            alt2 = 1;
                        }
                        break;

                    }

                    switch (alt2) {
                        case 1:
                            // parser/flatzinc/Flatzinc.g:
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
                            if (cnt2 >= 1) break loop2;
                            EarlyExitException eee =
                                    new EarlyExitException(2, input);
                            throw eee;
                    }
                    cnt2++;
                } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        } finally {
            // do for sure before leaving
        }
    }
    // $ANTLR end "INT_CONST"

    // $ANTLR start "IDENTIFIER"
    public final void mIDENTIFIER() throws RecognitionException {
        try {
            int _type = IDENTIFIER;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/Flatzinc.g:243:5: ( ( 'a' .. 'z' | 'A' .. 'Z' | '_' ) ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '_' )* )
            // parser/flatzinc/Flatzinc.g:243:9: ( 'a' .. 'z' | 'A' .. 'Z' | '_' ) ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '_' )*
            {
                if ((input.LA(1) >= 'A' && input.LA(1) <= 'Z') || input.LA(1) == '_' || (input.LA(1) >= 'a' && input.LA(1) <= 'z')) {
                    input.consume();
                } else {
                    MismatchedSetException mse = new MismatchedSetException(null, input);
                    recover(mse);
                    throw mse;
                }


                // parser/flatzinc/Flatzinc.g:243:33: ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '_' )*
                loop3:
                do {
                    int alt3 = 2;
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
                            alt3 = 1;
                        }
                        break;

                    }

                    switch (alt3) {
                        case 1:
                            // parser/flatzinc/Flatzinc.g:
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
                            break loop3;
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
            // parser/flatzinc/Flatzinc.g:261:5: ( '%' (~ ( '\\n' | '\\r' ) )* ( '\\r' )? '\\n' )
            // parser/flatzinc/Flatzinc.g:261:9: '%' (~ ( '\\n' | '\\r' ) )* ( '\\r' )? '\\n'
            {
                match('%');

                // parser/flatzinc/Flatzinc.g:261:13: (~ ( '\\n' | '\\r' ) )*
                loop4:
                do {
                    int alt4 = 2;
                    int LA4_0 = input.LA(1);

                    if (((LA4_0 >= '\u0000' && LA4_0 <= '\t') || (LA4_0 >= '\u000B' && LA4_0 <= '\f') || (LA4_0 >= '\u000E' && LA4_0 <= '\uFFFF'))) {
                        alt4 = 1;
                    }


                    switch (alt4) {
                        case 1:
                            // parser/flatzinc/Flatzinc.g:
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
                            break loop4;
                    }
                } while (true);


                // parser/flatzinc/Flatzinc.g:261:27: ( '\\r' )?
                int alt5 = 2;
                switch (input.LA(1)) {
                    case '\r': {
                        alt5 = 1;
                    }
                    break;
                }

                switch (alt5) {
                    case 1:
                        // parser/flatzinc/Flatzinc.g:261:27: '\\r'
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
            // parser/flatzinc/Flatzinc.g:264:5: ( ( ' ' | '\\t' | '\\r' | '\\n' ) )
            // parser/flatzinc/Flatzinc.g:264:9: ( ' ' | '\\t' | '\\r' | '\\n' )
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

    // $ANTLR start "STRING"
    public final void mSTRING() throws RecognitionException {
        try {
            int _type = STRING;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/Flatzinc.g:272:5: ( '\"' ( ESC_SEQ |~ ( '\\\\' | '\"' ) )* '\"' )
            // parser/flatzinc/Flatzinc.g:272:8: '\"' ( ESC_SEQ |~ ( '\\\\' | '\"' ) )* '\"'
            {
                match('\"');

                // parser/flatzinc/Flatzinc.g:272:12: ( ESC_SEQ |~ ( '\\\\' | '\"' ) )*
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
                            // parser/flatzinc/Flatzinc.g:272:14: ESC_SEQ
                        {
                            mESC_SEQ();


                        }
                        break;
                        case 2:
                            // parser/flatzinc/Flatzinc.g:272:24: ~ ( '\\\\' | '\"' )
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
            // parser/flatzinc/Flatzinc.g:275:5: ( '\\'' ( ESC_SEQ |~ ( '\\'' | '\\\\' ) ) '\\'' )
            // parser/flatzinc/Flatzinc.g:275:8: '\\'' ( ESC_SEQ |~ ( '\\'' | '\\\\' ) ) '\\''
            {
                match('\'');

                // parser/flatzinc/Flatzinc.g:275:13: ( ESC_SEQ |~ ( '\\'' | '\\\\' ) )
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
                        // parser/flatzinc/Flatzinc.g:275:15: ESC_SEQ
                    {
                        mESC_SEQ();


                    }
                    break;
                    case 2:
                        // parser/flatzinc/Flatzinc.g:275:25: ~ ( '\\'' | '\\\\' )
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
            // parser/flatzinc/Flatzinc.g:280:10: ( ( 'e' | 'E' ) ( '+' | '-' )? ( '0' .. '9' )+ )
            // parser/flatzinc/Flatzinc.g:280:12: ( 'e' | 'E' ) ( '+' | '-' )? ( '0' .. '9' )+
            {
                if (input.LA(1) == 'E' || input.LA(1) == 'e') {
                    input.consume();
                } else {
                    MismatchedSetException mse = new MismatchedSetException(null, input);
                    recover(mse);
                    throw mse;
                }


                // parser/flatzinc/Flatzinc.g:280:22: ( '+' | '-' )?
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
                        // parser/flatzinc/Flatzinc.g:
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


                // parser/flatzinc/Flatzinc.g:280:33: ( '0' .. '9' )+
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
                            // parser/flatzinc/Flatzinc.g:
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
            // parser/flatzinc/Flatzinc.g:284:5: ( '\\\\' ( 'b' | 't' | 'n' | 'f' | 'r' | '\\\"' | '\\'' | '\\\\' ) | UNICODE_ESC | OCTAL_ESC )
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
                    // parser/flatzinc/Flatzinc.g:284:9: '\\\\' ( 'b' | 't' | 'n' | 'f' | 'r' | '\\\"' | '\\'' | '\\\\' )
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
                    // parser/flatzinc/Flatzinc.g:285:9: UNICODE_ESC
                {
                    mUNICODE_ESC();


                }
                break;
                case 3:
                    // parser/flatzinc/Flatzinc.g:286:9: OCTAL_ESC
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
            // parser/flatzinc/Flatzinc.g:291:5: ( '\\\\' ( '0' .. '3' ) ( '0' .. '7' ) ( '0' .. '7' ) | '\\\\' ( '0' .. '7' ) ( '0' .. '7' ) | '\\\\' ( '0' .. '7' ) )
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
                    // parser/flatzinc/Flatzinc.g:291:9: '\\\\' ( '0' .. '3' ) ( '0' .. '7' ) ( '0' .. '7' )
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
                    // parser/flatzinc/Flatzinc.g:292:9: '\\\\' ( '0' .. '7' ) ( '0' .. '7' )
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
                    // parser/flatzinc/Flatzinc.g:293:9: '\\\\' ( '0' .. '7' )
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
            // parser/flatzinc/Flatzinc.g:297:11: ( ( '0' .. '9' | 'a' .. 'f' | 'A' .. 'F' ) )
            // parser/flatzinc/Flatzinc.g:
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
            // parser/flatzinc/Flatzinc.g:301:5: ( '\\\\' 'u' HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT )
            // parser/flatzinc/Flatzinc.g:301:9: '\\\\' 'u' HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT
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
        // parser/flatzinc/Flatzinc.g:1:8: ( ARRAY | BOOL | CL | CM | CONSTRAINT | DC | DD | DO | EQ | FALSE | FLOAT | INT | LB | LP | LS | MAXIMIZE | MINIMIZE | MN | OF | PAR | PL | PREDICATE | RB | RP | RS | SATISFY | SC | SET | SOLVE | TRUE | VAR | APAR | ARRPAR | AVAR | ARRVAR | INDEX | EXPR | ANNOTATIONS | INT_CONST | IDENTIFIER | COMMENT | WS | STRING | CHAR )
        int alt12 = 44;
        alt12 = dfa12.predict(input);
        switch (alt12) {
            case 1:
                // parser/flatzinc/Flatzinc.g:1:10: ARRAY
            {
                mARRAY();


            }
            break;
            case 2:
                // parser/flatzinc/Flatzinc.g:1:16: BOOL
            {
                mBOOL();


            }
            break;
            case 3:
                // parser/flatzinc/Flatzinc.g:1:21: CL
            {
                mCL();


            }
            break;
            case 4:
                // parser/flatzinc/Flatzinc.g:1:24: CM
            {
                mCM();


            }
            break;
            case 5:
                // parser/flatzinc/Flatzinc.g:1:27: CONSTRAINT
            {
                mCONSTRAINT();


            }
            break;
            case 6:
                // parser/flatzinc/Flatzinc.g:1:38: DC
            {
                mDC();


            }
            break;
            case 7:
                // parser/flatzinc/Flatzinc.g:1:41: DD
            {
                mDD();


            }
            break;
            case 8:
                // parser/flatzinc/Flatzinc.g:1:44: DO
            {
                mDO();


            }
            break;
            case 9:
                // parser/flatzinc/Flatzinc.g:1:47: EQ
            {
                mEQ();


            }
            break;
            case 10:
                // parser/flatzinc/Flatzinc.g:1:50: FALSE
            {
                mFALSE();


            }
            break;
            case 11:
                // parser/flatzinc/Flatzinc.g:1:56: FLOAT
            {
                mFLOAT();


            }
            break;
            case 12:
                // parser/flatzinc/Flatzinc.g:1:62: INT
            {
                mINT();


            }
            break;
            case 13:
                // parser/flatzinc/Flatzinc.g:1:66: LB
            {
                mLB();


            }
            break;
            case 14:
                // parser/flatzinc/Flatzinc.g:1:69: LP
            {
                mLP();


            }
            break;
            case 15:
                // parser/flatzinc/Flatzinc.g:1:72: LS
            {
                mLS();


            }
            break;
            case 16:
                // parser/flatzinc/Flatzinc.g:1:75: MAXIMIZE
            {
                mMAXIMIZE();


            }
            break;
            case 17:
                // parser/flatzinc/Flatzinc.g:1:84: MINIMIZE
            {
                mMINIMIZE();


            }
            break;
            case 18:
                // parser/flatzinc/Flatzinc.g:1:93: MN
            {
                mMN();


            }
            break;
            case 19:
                // parser/flatzinc/Flatzinc.g:1:96: OF
            {
                mOF();


            }
            break;
            case 20:
                // parser/flatzinc/Flatzinc.g:1:99: PAR
            {
                mPAR();


            }
            break;
            case 21:
                // parser/flatzinc/Flatzinc.g:1:103: PL
            {
                mPL();


            }
            break;
            case 22:
                // parser/flatzinc/Flatzinc.g:1:106: PREDICATE
            {
                mPREDICATE();


            }
            break;
            case 23:
                // parser/flatzinc/Flatzinc.g:1:116: RB
            {
                mRB();


            }
            break;
            case 24:
                // parser/flatzinc/Flatzinc.g:1:119: RP
            {
                mRP();


            }
            break;
            case 25:
                // parser/flatzinc/Flatzinc.g:1:122: RS
            {
                mRS();


            }
            break;
            case 26:
                // parser/flatzinc/Flatzinc.g:1:125: SATISFY
            {
                mSATISFY();


            }
            break;
            case 27:
                // parser/flatzinc/Flatzinc.g:1:133: SC
            {
                mSC();


            }
            break;
            case 28:
                // parser/flatzinc/Flatzinc.g:1:136: SET
            {
                mSET();


            }
            break;
            case 29:
                // parser/flatzinc/Flatzinc.g:1:140: SOLVE
            {
                mSOLVE();


            }
            break;
            case 30:
                // parser/flatzinc/Flatzinc.g:1:146: TRUE
            {
                mTRUE();


            }
            break;
            case 31:
                // parser/flatzinc/Flatzinc.g:1:151: VAR
            {
                mVAR();


            }
            break;
            case 32:
                // parser/flatzinc/Flatzinc.g:1:155: APAR
            {
                mAPAR();


            }
            break;
            case 33:
                // parser/flatzinc/Flatzinc.g:1:160: ARRPAR
            {
                mARRPAR();


            }
            break;
            case 34:
                // parser/flatzinc/Flatzinc.g:1:167: AVAR
            {
                mAVAR();


            }
            break;
            case 35:
                // parser/flatzinc/Flatzinc.g:1:172: ARRVAR
            {
                mARRVAR();


            }
            break;
            case 36:
                // parser/flatzinc/Flatzinc.g:1:179: INDEX
            {
                mINDEX();


            }
            break;
            case 37:
                // parser/flatzinc/Flatzinc.g:1:185: EXPR
            {
                mEXPR();


            }
            break;
            case 38:
                // parser/flatzinc/Flatzinc.g:1:190: ANNOTATIONS
            {
                mANNOTATIONS();


            }
            break;
            case 39:
                // parser/flatzinc/Flatzinc.g:1:202: INT_CONST
            {
                mINT_CONST();


            }
            break;
            case 40:
                // parser/flatzinc/Flatzinc.g:1:212: IDENTIFIER
            {
                mIDENTIFIER();


            }
            break;
            case 41:
                // parser/flatzinc/Flatzinc.g:1:223: COMMENT
            {
                mCOMMENT();


            }
            break;
            case 42:
                // parser/flatzinc/Flatzinc.g:1:231: WS
            {
                mWS();


            }
            break;
            case 43:
                // parser/flatzinc/Flatzinc.g:1:234: STRING
            {
                mSTRING();


            }
            break;
            case 44:
                // parser/flatzinc/Flatzinc.g:1:241: CHAR
            {
                mCHAR();


            }
            break;

        }

    }


    protected DFA12 dfa12 = new DFA12(this);
    static final String DFA12_eotS =
            "\1\uffff\2\33\1\43\1\uffff\1\33\1\46\1\uffff\2\33\3\uffff\1\33\1" +
                    "\54\2\33\1\60\3\uffff\1\33\1\uffff\2\33\7\uffff\2\33\2\uffff\1\33" +
                    "\2\uffff\5\33\1\uffff\1\77\2\33\1\uffff\5\33\1\uffff\5\33\1\115" +
                    "\2\33\1\uffff\1\120\2\33\1\123\2\33\1\126\1\uffff\1\33\1\134\3\33" +
                    "\1\uffff\2\33\1\uffff\2\33\1\uffff\1\33\1\145\5\uffff\1\153\1\uffff" +
                    "\1\33\1\155\1\156\4\33\1\163\7\uffff\1\33\2\uffff\4\33\1\uffff\4" +
                    "\33\1\175\1\33\1\177\1\u0080\1\33\1\uffff\1\33\2\uffff\1\u0083\1" +
                    "\u0084\2\uffff";
    static final String DFA12_eofS =
            "\u0085\uffff";
    static final String DFA12_minS =
            "\1\11\1\162\1\157\1\72\1\uffff\1\157\1\56\1\uffff\1\141\1\156\3" +
                    "\uffff\1\141\1\60\1\146\1\141\1\60\3\uffff\1\141\1\uffff\1\162\1" +
                    "\141\1\43\6\uffff\1\162\1\157\2\uffff\1\156\2\uffff\1\154\1\157" +
                    "\1\164\1\170\1\156\1\uffff\1\60\1\162\1\145\1\uffff\2\164\1\154" +
                    "\1\165\1\162\1\43\1\141\1\154\2\163\1\141\1\60\2\151\1\uffff\1\60" +
                    "\1\144\1\151\1\60\1\166\1\145\1\60\1\101\1\171\1\60\1\164\1\145" +
                    "\1\164\1\uffff\2\155\1\uffff\1\151\1\163\1\uffff\1\145\1\60\1\uffff" +
                    "\2\120\2\uffff\1\60\1\uffff\1\162\2\60\2\151\1\143\1\146\1\60\7" +
                    "\uffff\1\141\2\uffff\2\172\1\141\1\171\1\uffff\1\151\2\145\1\164" +
                    "\1\60\1\156\2\60\1\145\1\uffff\1\164\2\uffff\2\60\2\uffff";
    static final String DFA12_maxS =
            "\1\175\1\162\1\157\1\72\1\uffff\1\157\1\56\1\uffff\1\154\1\156\3" +
                    "\uffff\1\151\1\71\1\146\1\162\1\71\3\uffff\1\157\1\uffff\1\162\1" +
                    "\141\1\43\6\uffff\1\162\1\157\2\uffff\1\156\2\uffff\1\154\1\157" +
                    "\1\164\1\170\1\156\1\uffff\1\172\1\162\1\145\1\uffff\2\164\1\154" +
                    "\1\165\1\162\1\43\1\141\1\154\2\163\1\141\1\172\2\151\1\uffff\1" +
                    "\172\1\144\1\151\1\172\1\166\1\145\1\172\1\137\1\171\1\172\1\164" +
                    "\1\145\1\164\1\uffff\2\155\1\uffff\1\151\1\163\1\uffff\1\145\1\172" +
                    "\1\uffff\2\126\2\uffff\1\172\1\uffff\1\162\2\172\2\151\1\143\1\146" +
                    "\1\172\7\uffff\1\141\2\uffff\2\172\1\141\1\171\1\uffff\1\151\2\145" +
                    "\1\164\1\172\1\156\2\172\1\145\1\uffff\1\164\2\uffff\2\172\2\uffff";
    static final String DFA12_acceptS =
            "\4\uffff\1\4\2\uffff\1\11\2\uffff\1\15\1\16\1\17\5\uffff\1\27\1" +
                    "\30\1\31\1\uffff\1\33\3\uffff\1\47\1\50\1\51\1\52\1\53\1\54\2\uffff" +
                    "\1\6\1\3\1\uffff\1\7\1\10\5\uffff\1\22\3\uffff\1\25\16\uffff\1\23" +
                    "\15\uffff\1\14\2\uffff\1\24\2\uffff\1\34\2\uffff\1\37\2\uffff\1" +
                    "\44\1\45\1\uffff\1\2\10\uffff\1\36\1\40\1\42\1\41\1\43\1\46\1\1" +
                    "\1\uffff\1\12\1\13\4\uffff\1\35\11\uffff\1\32\1\uffff\1\20\1\21" +
                    "\2\uffff\1\26\1\5";
    static final String DFA12_specialS =
            "\u0085\uffff}>";
    static final String[] DFA12_transitionS = {
            "\2\35\2\uffff\1\35\22\uffff\1\35\1\uffff\1\36\1\31\1\uffff\1" +
                    "\34\1\uffff\1\37\1\13\1\23\1\uffff\1\21\1\4\1\16\1\6\1\uffff" +
                    "\12\32\1\3\1\26\1\uffff\1\7\3\uffff\32\33\1\14\1\uffff\1\24" +
                    "\1\uffff\1\33\1\uffff\1\1\1\2\1\5\2\33\1\10\2\33\1\11\3\33\1" +
                    "\15\1\33\1\17\1\20\2\33\1\25\1\27\1\33\1\30\4\33\1\12\1\uffff" +
                    "\1\22",
            "\1\40",
            "\1\41",
            "\1\42",
            "",
            "\1\44",
            "\1\45",
            "",
            "\1\47\12\uffff\1\50",
            "\1\51",
            "",
            "",
            "",
            "\1\52\7\uffff\1\53",
            "\12\32",
            "\1\55",
            "\1\56\20\uffff\1\57",
            "\12\32",
            "",
            "",
            "",
            "\1\61\3\uffff\1\62\11\uffff\1\63",
            "",
            "\1\64",
            "\1\65",
            "\1\66",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\67",
            "\1\70",
            "",
            "",
            "\1\71",
            "",
            "",
            "\1\72",
            "\1\73",
            "\1\74",
            "\1\75",
            "\1\76",
            "",
            "\12\33\7\uffff\32\33\4\uffff\1\33\1\uffff\32\33",
            "\1\100",
            "\1\101",
            "",
            "\1\102",
            "\1\103",
            "\1\104",
            "\1\105",
            "\1\106",
            "\1\107",
            "\1\110",
            "\1\111",
            "\1\112",
            "\1\113",
            "\1\114",
            "\12\33\7\uffff\32\33\4\uffff\1\33\1\uffff\32\33",
            "\1\116",
            "\1\117",
            "",
            "\12\33\7\uffff\32\33\4\uffff\1\33\1\uffff\32\33",
            "\1\121",
            "\1\122",
            "\12\33\7\uffff\32\33\4\uffff\1\33\1\uffff\32\33",
            "\1\124",
            "\1\125",
            "\12\33\7\uffff\32\33\4\uffff\1\33\1\uffff\32\33",
            "\1\130\3\uffff\1\132\3\uffff\1\131\25\uffff\1\127",
            "\1\133",
            "\12\33\7\uffff\32\33\4\uffff\1\33\1\uffff\32\33",
            "\1\135",
            "\1\136",
            "\1\137",
            "",
            "\1\140",
            "\1\141",
            "",
            "\1\142",
            "\1\143",
            "",
            "\1\144",
            "\12\33\7\uffff\32\33\4\uffff\1\33\1\uffff\32\33",
            "",
            "\1\146\5\uffff\1\147",
            "\1\150\2\uffff\1\152\2\uffff\1\151",
            "",
            "",
            "\12\33\7\uffff\32\33\4\uffff\1\33\1\uffff\32\33",
            "",
            "\1\154",
            "\12\33\7\uffff\32\33\4\uffff\1\33\1\uffff\32\33",
            "\12\33\7\uffff\32\33\4\uffff\1\33\1\uffff\32\33",
            "\1\157",
            "\1\160",
            "\1\161",
            "\1\162",
            "\12\33\7\uffff\32\33\4\uffff\1\33\1\uffff\32\33",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\164",
            "",
            "",
            "\1\165",
            "\1\166",
            "\1\167",
            "\1\170",
            "",
            "\1\171",
            "\1\172",
            "\1\173",
            "\1\174",
            "\12\33\7\uffff\32\33\4\uffff\1\33\1\uffff\32\33",
            "\1\176",
            "\12\33\7\uffff\32\33\4\uffff\1\33\1\uffff\32\33",
            "\12\33\7\uffff\32\33\4\uffff\1\33\1\uffff\32\33",
            "\1\u0081",
            "",
            "\1\u0082",
            "",
            "",
            "\12\33\7\uffff\32\33\4\uffff\1\33\1\uffff\32\33",
            "\12\33\7\uffff\32\33\4\uffff\1\33\1\uffff\32\33",
            "",
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
            return "1:1: Tokens : ( ARRAY | BOOL | CL | CM | CONSTRAINT | DC | DD | DO | EQ | FALSE | FLOAT | INT | LB | LP | LS | MAXIMIZE | MINIMIZE | MN | OF | PAR | PL | PREDICATE | RB | RP | RS | SATISFY | SC | SET | SOLVE | TRUE | VAR | APAR | ARRPAR | AVAR | ARRVAR | INDEX | EXPR | ANNOTATIONS | INT_CONST | IDENTIFIER | COMMENT | WS | STRING | CHAR );";
        }
    }


}