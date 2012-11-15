// $ANTLR 3.4 parser/flatzinc/FlatzincFullExtLexer.g 2012-11-15 16:27:48

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

package parser.flatzinc;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked"})
public class FlatzincFullExtLexer extends Lexer {
    public static final int EOF=-1;
    public static final int AND=4;
    public static final int ANNOTATIONS=5;
    public static final int ANY=6;
    public static final int APAR=7;
    public static final int ARRAY=8;
    public static final int ARRPAR=9;
    public static final int ARRVAR=10;
    public static final int AS=11;
    public static final int AVAR=12;
    public static final int BOOL=13;
    public static final int CARITY=14;
    public static final int CHAR=15;
    public static final int CL=16;
    public static final int CM=17;
    public static final int CNAME=18;
    public static final int COMMENT=19;
    public static final int CONSTRAINT=20;
    public static final int CSTR=21;
    public static final int DC=22;
    public static final int DD=23;
    public static final int DO=24;
    public static final int EACH=25;
    public static final int EQ=26;
    public static final int ESC_SEQ=27;
    public static final int EXPONENT=28;
    public static final int EXPR=29;
    public static final int FALSE=30;
    public static final int FLOAT=31;
    public static final int FOR=32;
    public static final int HEAP=33;
    public static final int HEX_DIGIT=34;
    public static final int IDENTIFIER=35;
    public static final int IN=36;
    public static final int INDEX=37;
    public static final int INT=38;
    public static final int INT_CONST=39;
    public static final int KEY=40;
    public static final int LB=41;
    public static final int LIST=42;
    public static final int LP=43;
    public static final int LS=44;
    public static final int MAX=45;
    public static final int MAXIMIZE=46;
    public static final int MIN=47;
    public static final int MINIMIZE=48;
    public static final int MN=49;
    public static final int NOT=50;
    public static final int OCTAL_ESC=51;
    public static final int OEQ=52;
    public static final int OF=53;
    public static final int OGQ=54;
    public static final int OGT=55;
    public static final int OLQ=56;
    public static final int OLT=57;
    public static final int ONE=58;
    public static final int ONQ=59;
    public static final int OR=60;
    public static final int ORDERBY=61;
    public static final int PAR=62;
    public static final int PARITY=63;
    public static final int PL=64;
    public static final int PPRIO=65;
    public static final int PPRIOD=66;
    public static final int PREDICATE=67;
    public static final int PROP=68;
    public static final int QUEUE=69;
    public static final int RB=70;
    public static final int REV=71;
    public static final int RP=72;
    public static final int RS=73;
    public static final int SATISFY=74;
    public static final int SC=75;
    public static final int SET=76;
    public static final int SIZE=77;
    public static final int SOLVE=78;
    public static final int STREG=79;
    public static final int STRING=80;
    public static final int STRUC=81;
    public static final int SUM=82;
    public static final int TRUE=83;
    public static final int UNICODE_ESC=84;
    public static final int VAR=85;
    public static final int VCARD=86;
    public static final int VNAME=87;
    public static final int WFOR=88;
    public static final int WONE=89;
    public static final int WS=90;

    // delegates
    // delegators
    public Lexer[] getDelegates() {
        return new Lexer[] {};
    }

    public FlatzincFullExtLexer() {} 
    public FlatzincFullExtLexer(CharStream input) {
        this(input, new RecognizerSharedState());
    }
    public FlatzincFullExtLexer(CharStream input, RecognizerSharedState state) {
        super(input,state);
    }
    public String getGrammarFileName() { return "parser/flatzinc/FlatzincFullExtLexer.g"; }

    // $ANTLR start "BOOL"
    public final void mBOOL() throws RecognitionException {
        try {
            int _type = BOOL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincFullExtLexer.g:39:5: ( 'bool' )
            // parser/flatzinc/FlatzincFullExtLexer.g:39:6: 'bool'
            {
            match("bool"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "BOOL"

    // $ANTLR start "TRUE"
    public final void mTRUE() throws RecognitionException {
        try {
            int _type = TRUE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincFullExtLexer.g:40:5: ( 'true' )
            // parser/flatzinc/FlatzincFullExtLexer.g:40:6: 'true'
            {
            match("true"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "TRUE"

    // $ANTLR start "FALSE"
    public final void mFALSE() throws RecognitionException {
        try {
            int _type = FALSE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincFullExtLexer.g:41:6: ( 'false' )
            // parser/flatzinc/FlatzincFullExtLexer.g:41:7: 'false'
            {
            match("false"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "FALSE"

    // $ANTLR start "INT"
    public final void mINT() throws RecognitionException {
        try {
            int _type = INT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincFullExtLexer.g:42:4: ( 'int' )
            // parser/flatzinc/FlatzincFullExtLexer.g:42:5: 'int'
            {
            match("int"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "INT"

    // $ANTLR start "FLOAT"
    public final void mFLOAT() throws RecognitionException {
        try {
            int _type = FLOAT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincFullExtLexer.g:43:6: ( 'float' )
            // parser/flatzinc/FlatzincFullExtLexer.g:43:7: 'float'
            {
            match("float"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "FLOAT"

    // $ANTLR start "SET"
    public final void mSET() throws RecognitionException {
        try {
            int _type = SET;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincFullExtLexer.g:44:5: ( 'set' )
            // parser/flatzinc/FlatzincFullExtLexer.g:44:6: 'set'
            {
            match("set"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "SET"

    // $ANTLR start "OF"
    public final void mOF() throws RecognitionException {
        try {
            int _type = OF;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincFullExtLexer.g:45:4: ( 'of' )
            // parser/flatzinc/FlatzincFullExtLexer.g:45:5: 'of'
            {
            match("of"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "OF"

    // $ANTLR start "ARRAY"
    public final void mARRAY() throws RecognitionException {
        try {
            int _type = ARRAY;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincFullExtLexer.g:46:7: ( 'array' )
            // parser/flatzinc/FlatzincFullExtLexer.g:46:8: 'array'
            {
            match("array"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "ARRAY"

    // $ANTLR start "VAR"
    public final void mVAR() throws RecognitionException {
        try {
            int _type = VAR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincFullExtLexer.g:47:5: ( 'var' )
            // parser/flatzinc/FlatzincFullExtLexer.g:47:6: 'var'
            {
            match("var"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "VAR"

    // $ANTLR start "PAR"
    public final void mPAR() throws RecognitionException {
        try {
            int _type = PAR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincFullExtLexer.g:48:5: ( 'par' )
            // parser/flatzinc/FlatzincFullExtLexer.g:48:6: 'par'
            {
            match("par"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "PAR"

    // $ANTLR start "PREDICATE"
    public final void mPREDICATE() throws RecognitionException {
        try {
            int _type = PREDICATE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincFullExtLexer.g:49:11: ( 'predicate' )
            // parser/flatzinc/FlatzincFullExtLexer.g:49:12: 'predicate'
            {
            match("predicate"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "PREDICATE"

    // $ANTLR start "CONSTRAINT"
    public final void mCONSTRAINT() throws RecognitionException {
        try {
            int _type = CONSTRAINT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincFullExtLexer.g:50:12: ( 'constraint' )
            // parser/flatzinc/FlatzincFullExtLexer.g:50:15: 'constraint'
            {
            match("constraint"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "CONSTRAINT"

    // $ANTLR start "SOLVE"
    public final void mSOLVE() throws RecognitionException {
        try {
            int _type = SOLVE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincFullExtLexer.g:51:7: ( 'solve' )
            // parser/flatzinc/FlatzincFullExtLexer.g:51:8: 'solve'
            {
            match("solve"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "SOLVE"

    // $ANTLR start "SATISFY"
    public final void mSATISFY() throws RecognitionException {
        try {
            int _type = SATISFY;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincFullExtLexer.g:52:9: ( 'satisfy' )
            // parser/flatzinc/FlatzincFullExtLexer.g:52:10: 'satisfy'
            {
            match("satisfy"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "SATISFY"

    // $ANTLR start "MINIMIZE"
    public final void mMINIMIZE() throws RecognitionException {
        try {
            int _type = MINIMIZE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincFullExtLexer.g:53:10: ( 'minimize' )
            // parser/flatzinc/FlatzincFullExtLexer.g:53:11: 'minimize'
            {
            match("minimize"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "MINIMIZE"

    // $ANTLR start "MAXIMIZE"
    public final void mMAXIMIZE() throws RecognitionException {
        try {
            int _type = MAXIMIZE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincFullExtLexer.g:54:10: ( 'maximize' )
            // parser/flatzinc/FlatzincFullExtLexer.g:54:11: 'maximize'
            {
            match("maximize"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "MAXIMIZE"

    // $ANTLR start "DD"
    public final void mDD() throws RecognitionException {
        try {
            int _type = DD;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincFullExtLexer.g:56:3: ( '..' )
            // parser/flatzinc/FlatzincFullExtLexer.g:56:4: '..'
            {
            match(".."); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "DD"

    // $ANTLR start "DO"
    public final void mDO() throws RecognitionException {
        try {
            int _type = DO;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincFullExtLexer.g:57:3: ( '.' )
            // parser/flatzinc/FlatzincFullExtLexer.g:57:4: '.'
            {
            match('.'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "DO"

    // $ANTLR start "LB"
    public final void mLB() throws RecognitionException {
        try {
            int _type = LB;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincFullExtLexer.g:58:3: ( '{' )
            // parser/flatzinc/FlatzincFullExtLexer.g:58:4: '{'
            {
            match('{'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "LB"

    // $ANTLR start "RB"
    public final void mRB() throws RecognitionException {
        try {
            int _type = RB;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincFullExtLexer.g:59:3: ( '}' )
            // parser/flatzinc/FlatzincFullExtLexer.g:59:4: '}'
            {
            match('}'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "RB"

    // $ANTLR start "CM"
    public final void mCM() throws RecognitionException {
        try {
            int _type = CM;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincFullExtLexer.g:60:3: ( ',' )
            // parser/flatzinc/FlatzincFullExtLexer.g:60:4: ','
            {
            match(','); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "CM"

    // $ANTLR start "LS"
    public final void mLS() throws RecognitionException {
        try {
            int _type = LS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincFullExtLexer.g:61:3: ( '[' )
            // parser/flatzinc/FlatzincFullExtLexer.g:61:4: '['
            {
            match('['); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "LS"

    // $ANTLR start "RS"
    public final void mRS() throws RecognitionException {
        try {
            int _type = RS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincFullExtLexer.g:62:4: ( ']' )
            // parser/flatzinc/FlatzincFullExtLexer.g:62:5: ']'
            {
            match(']'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "RS"

    // $ANTLR start "EQ"
    public final void mEQ() throws RecognitionException {
        try {
            int _type = EQ;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincFullExtLexer.g:63:3: ( '=' )
            // parser/flatzinc/FlatzincFullExtLexer.g:63:4: '='
            {
            match('='); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "EQ"

    // $ANTLR start "PL"
    public final void mPL() throws RecognitionException {
        try {
            int _type = PL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincFullExtLexer.g:64:3: ( '+' )
            // parser/flatzinc/FlatzincFullExtLexer.g:64:4: '+'
            {
            match('+'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "PL"

    // $ANTLR start "MN"
    public final void mMN() throws RecognitionException {
        try {
            int _type = MN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincFullExtLexer.g:65:3: ( '-' )
            // parser/flatzinc/FlatzincFullExtLexer.g:65:4: '-'
            {
            match('-'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "MN"

    // $ANTLR start "SC"
    public final void mSC() throws RecognitionException {
        try {
            int _type = SC;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincFullExtLexer.g:66:3: ( ';' )
            // parser/flatzinc/FlatzincFullExtLexer.g:66:4: ';'
            {
            match(';'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "SC"

    // $ANTLR start "CL"
    public final void mCL() throws RecognitionException {
        try {
            int _type = CL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincFullExtLexer.g:67:3: ( ':' )
            // parser/flatzinc/FlatzincFullExtLexer.g:67:4: ':'
            {
            match(':'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "CL"

    // $ANTLR start "DC"
    public final void mDC() throws RecognitionException {
        try {
            int _type = DC;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincFullExtLexer.g:68:3: ( '::' )
            // parser/flatzinc/FlatzincFullExtLexer.g:68:4: '::'
            {
            match("::"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "DC"

    // $ANTLR start "LP"
    public final void mLP() throws RecognitionException {
        try {
            int _type = LP;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincFullExtLexer.g:69:3: ( '(' )
            // parser/flatzinc/FlatzincFullExtLexer.g:69:4: '('
            {
            match('('); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "LP"

    // $ANTLR start "RP"
    public final void mRP() throws RecognitionException {
        try {
            int _type = RP;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincFullExtLexer.g:70:3: ( ')' )
            // parser/flatzinc/FlatzincFullExtLexer.g:70:4: ')'
            {
            match(')'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "RP"

    // $ANTLR start "AS"
    public final void mAS() throws RecognitionException {
        try {
            int _type = AS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincFullExtLexer.g:76:3: ( 'as' )
            // parser/flatzinc/FlatzincFullExtLexer.g:76:4: 'as'
            {
            match("as"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "AS"

    // $ANTLR start "EACH"
    public final void mEACH() throws RecognitionException {
        try {
            int _type = EACH;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincFullExtLexer.g:77:5: ( 'each' )
            // parser/flatzinc/FlatzincFullExtLexer.g:77:6: 'each'
            {
            match("each"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "EACH"

    // $ANTLR start "QUEUE"
    public final void mQUEUE() throws RecognitionException {
        try {
            int _type = QUEUE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincFullExtLexer.g:80:6: ( 'queue' )
            // parser/flatzinc/FlatzincFullExtLexer.g:80:7: 'queue'
            {
            match("queue"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "QUEUE"

    // $ANTLR start "LIST"
    public final void mLIST() throws RecognitionException {
        try {
            int _type = LIST;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincFullExtLexer.g:81:5: ( 'list' )
            // parser/flatzinc/FlatzincFullExtLexer.g:81:6: 'list'
            {
            match("list"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "LIST"

    // $ANTLR start "HEAP"
    public final void mHEAP() throws RecognitionException {
        try {
            int _type = HEAP;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincFullExtLexer.g:82:5: ( 'heap' )
            // parser/flatzinc/FlatzincFullExtLexer.g:82:6: 'heap'
            {
            match("heap"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "HEAP"

    // $ANTLR start "ONE"
    public final void mONE() throws RecognitionException {
        try {
            int _type = ONE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincFullExtLexer.g:84:4: ( 'one' )
            // parser/flatzinc/FlatzincFullExtLexer.g:84:5: 'one'
            {
            match("one"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "ONE"

    // $ANTLR start "WONE"
    public final void mWONE() throws RecognitionException {
        try {
            int _type = WONE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincFullExtLexer.g:85:5: ( 'wone' )
            // parser/flatzinc/FlatzincFullExtLexer.g:85:6: 'wone'
            {
            match("wone"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "WONE"

    // $ANTLR start "FOR"
    public final void mFOR() throws RecognitionException {
        try {
            int _type = FOR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincFullExtLexer.g:86:4: ( 'for' )
            // parser/flatzinc/FlatzincFullExtLexer.g:86:5: 'for'
            {
            match("for"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "FOR"

    // $ANTLR start "WFOR"
    public final void mWFOR() throws RecognitionException {
        try {
            int _type = WFOR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincFullExtLexer.g:87:5: ( 'wfor' )
            // parser/flatzinc/FlatzincFullExtLexer.g:87:6: 'wfor'
            {
            match("wfor"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "WFOR"

    // $ANTLR start "ORDERBY"
    public final void mORDERBY() throws RecognitionException {
        try {
            int _type = ORDERBY;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincFullExtLexer.g:88:8: ( 'order by' )
            // parser/flatzinc/FlatzincFullExtLexer.g:88:9: 'order by'
            {
            match("order by"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "ORDERBY"

    // $ANTLR start "AND"
    public final void mAND() throws RecognitionException {
        try {
            int _type = AND;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincFullExtLexer.g:89:4: ( '&&' )
            // parser/flatzinc/FlatzincFullExtLexer.g:89:5: '&&'
            {
            match("&&"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "AND"

    // $ANTLR start "OR"
    public final void mOR() throws RecognitionException {
        try {
            int _type = OR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincFullExtLexer.g:90:3: ( '||' )
            // parser/flatzinc/FlatzincFullExtLexer.g:90:4: '||'
            {
            match("||"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "OR"

    // $ANTLR start "NOT"
    public final void mNOT() throws RecognitionException {
        try {
            int _type = NOT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincFullExtLexer.g:91:4: ( '!' )
            // parser/flatzinc/FlatzincFullExtLexer.g:91:5: '!'
            {
            match('!'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "NOT"

    // $ANTLR start "IN"
    public final void mIN() throws RecognitionException {
        try {
            int _type = IN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincFullExtLexer.g:92:3: ( 'in' )
            // parser/flatzinc/FlatzincFullExtLexer.g:92:4: 'in'
            {
            match("in"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "IN"

    // $ANTLR start "REV"
    public final void mREV() throws RecognitionException {
        try {
            int _type = REV;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincFullExtLexer.g:93:4: ( 'rev' )
            // parser/flatzinc/FlatzincFullExtLexer.g:93:5: 'rev'
            {
            match("rev"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "REV"

    // $ANTLR start "OEQ"
    public final void mOEQ() throws RecognitionException {
        try {
            int _type = OEQ;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincFullExtLexer.g:94:4: ( '==' )
            // parser/flatzinc/FlatzincFullExtLexer.g:94:5: '=='
            {
            match("=="); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "OEQ"

    // $ANTLR start "ONQ"
    public final void mONQ() throws RecognitionException {
        try {
            int _type = ONQ;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincFullExtLexer.g:95:4: ( '!=' )
            // parser/flatzinc/FlatzincFullExtLexer.g:95:5: '!='
            {
            match("!="); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "ONQ"

    // $ANTLR start "OLT"
    public final void mOLT() throws RecognitionException {
        try {
            int _type = OLT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincFullExtLexer.g:96:4: ( '<' )
            // parser/flatzinc/FlatzincFullExtLexer.g:96:5: '<'
            {
            match('<'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "OLT"

    // $ANTLR start "OGT"
    public final void mOGT() throws RecognitionException {
        try {
            int _type = OGT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincFullExtLexer.g:97:4: ( '>' )
            // parser/flatzinc/FlatzincFullExtLexer.g:97:5: '>'
            {
            match('>'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "OGT"

    // $ANTLR start "OLQ"
    public final void mOLQ() throws RecognitionException {
        try {
            int _type = OLQ;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincFullExtLexer.g:98:4: ( '<=' )
            // parser/flatzinc/FlatzincFullExtLexer.g:98:5: '<='
            {
            match("<="); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "OLQ"

    // $ANTLR start "OGQ"
    public final void mOGQ() throws RecognitionException {
        try {
            int _type = OGQ;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincFullExtLexer.g:99:4: ( '>=' )
            // parser/flatzinc/FlatzincFullExtLexer.g:99:5: '>='
            {
            match(">="); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "OGQ"

    // $ANTLR start "KEY"
    public final void mKEY() throws RecognitionException {
        try {
            int _type = KEY;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincFullExtLexer.g:100:4: ( 'key' )
            // parser/flatzinc/FlatzincFullExtLexer.g:100:5: 'key'
            {
            match("key"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "KEY"

    // $ANTLR start "CSTR"
    public final void mCSTR() throws RecognitionException {
        try {
            int _type = CSTR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincFullExtLexer.g:105:5: ( 'cstr' )
            // parser/flatzinc/FlatzincFullExtLexer.g:105:6: 'cstr'
            {
            match("cstr"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "CSTR"

    // $ANTLR start "PROP"
    public final void mPROP() throws RecognitionException {
        try {
            int _type = PROP;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincFullExtLexer.g:106:5: ( 'prop' )
            // parser/flatzinc/FlatzincFullExtLexer.g:106:6: 'prop'
            {
            match("prop"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "PROP"

    // $ANTLR start "VNAME"
    public final void mVNAME() throws RecognitionException {
        try {
            int _type = VNAME;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincFullExtLexer.g:107:6: ( 'var.name' )
            // parser/flatzinc/FlatzincFullExtLexer.g:107:7: 'var.name'
            {
            match("var.name"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "VNAME"

    // $ANTLR start "VCARD"
    public final void mVCARD() throws RecognitionException {
        try {
            int _type = VCARD;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincFullExtLexer.g:108:6: ( 'var.cardinality' )
            // parser/flatzinc/FlatzincFullExtLexer.g:108:7: 'var.cardinality'
            {
            match("var.cardinality"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "VCARD"

    // $ANTLR start "CNAME"
    public final void mCNAME() throws RecognitionException {
        try {
            int _type = CNAME;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincFullExtLexer.g:109:6: ( 'cstr.name' )
            // parser/flatzinc/FlatzincFullExtLexer.g:109:7: 'cstr.name'
            {
            match("cstr.name"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "CNAME"

    // $ANTLR start "CARITY"
    public final void mCARITY() throws RecognitionException {
        try {
            int _type = CARITY;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincFullExtLexer.g:110:7: ( 'cstr.arity' )
            // parser/flatzinc/FlatzincFullExtLexer.g:110:8: 'cstr.arity'
            {
            match("cstr.arity"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "CARITY"

    // $ANTLR start "PPRIO"
    public final void mPPRIO() throws RecognitionException {
        try {
            int _type = PPRIO;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincFullExtLexer.g:111:6: ( 'prop.priority' )
            // parser/flatzinc/FlatzincFullExtLexer.g:111:7: 'prop.priority'
            {
            match("prop.priority"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "PPRIO"

    // $ANTLR start "PARITY"
    public final void mPARITY() throws RecognitionException {
        try {
            int _type = PARITY;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincFullExtLexer.g:112:7: ( 'prop.arity' )
            // parser/flatzinc/FlatzincFullExtLexer.g:112:8: 'prop.arity'
            {
            match("prop.arity"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "PARITY"

    // $ANTLR start "PPRIOD"
    public final void mPPRIOD() throws RecognitionException {
        try {
            int _type = PPRIOD;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincFullExtLexer.g:113:7: ( 'prop.prioDyn' )
            // parser/flatzinc/FlatzincFullExtLexer.g:113:8: 'prop.prioDyn'
            {
            match("prop.prioDyn"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "PPRIOD"

    // $ANTLR start "ANY"
    public final void mANY() throws RecognitionException {
        try {
            int _type = ANY;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincFullExtLexer.g:114:4: ( 'any' )
            // parser/flatzinc/FlatzincFullExtLexer.g:114:5: 'any'
            {
            match("any"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "ANY"

    // $ANTLR start "MIN"
    public final void mMIN() throws RecognitionException {
        try {
            int _type = MIN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincFullExtLexer.g:115:4: ( 'min' )
            // parser/flatzinc/FlatzincFullExtLexer.g:115:5: 'min'
            {
            match("min"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "MIN"

    // $ANTLR start "MAX"
    public final void mMAX() throws RecognitionException {
        try {
            int _type = MAX;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincFullExtLexer.g:116:4: ( 'max' )
            // parser/flatzinc/FlatzincFullExtLexer.g:116:5: 'max'
            {
            match("max"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "MAX"

    // $ANTLR start "SUM"
    public final void mSUM() throws RecognitionException {
        try {
            int _type = SUM;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincFullExtLexer.g:117:4: ( 'sum' )
            // parser/flatzinc/FlatzincFullExtLexer.g:117:5: 'sum'
            {
            match("sum"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "SUM"

    // $ANTLR start "SIZE"
    public final void mSIZE() throws RecognitionException {
        try {
            int _type = SIZE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincFullExtLexer.g:118:5: ( 'size' )
            // parser/flatzinc/FlatzincFullExtLexer.g:118:6: 'size'
            {
            match("size"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "SIZE"

    // $ANTLR start "APAR"
    public final void mAPAR() throws RecognitionException {
        try {
            int _type = APAR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincFullExtLexer.g:125:9: ( '###_P###' )
            // parser/flatzinc/FlatzincFullExtLexer.g:125:13: '###_P###'
            {
            match("###_P###"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "APAR"

    // $ANTLR start "ARRPAR"
    public final void mARRPAR() throws RecognitionException {
        try {
            int _type = ARRPAR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincFullExtLexer.g:126:9: ( '###AP###' )
            // parser/flatzinc/FlatzincFullExtLexer.g:126:13: '###AP###'
            {
            match("###AP###"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "ARRPAR"

    // $ANTLR start "AVAR"
    public final void mAVAR() throws RecognitionException {
        try {
            int _type = AVAR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincFullExtLexer.g:127:9: ( '###_V###' )
            // parser/flatzinc/FlatzincFullExtLexer.g:127:13: '###_V###'
            {
            match("###_V###"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "AVAR"

    // $ANTLR start "ARRVAR"
    public final void mARRVAR() throws RecognitionException {
        try {
            int _type = ARRVAR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincFullExtLexer.g:128:9: ( '###AV###' )
            // parser/flatzinc/FlatzincFullExtLexer.g:128:13: '###AV###'
            {
            match("###AV###"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "ARRVAR"

    // $ANTLR start "INDEX"
    public final void mINDEX() throws RecognitionException {
        try {
            int _type = INDEX;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincFullExtLexer.g:129:9: ( '###ID###' )
            // parser/flatzinc/FlatzincFullExtLexer.g:129:13: '###ID###'
            {
            match("###ID###"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "INDEX"

    // $ANTLR start "EXPR"
    public final void mEXPR() throws RecognitionException {
        try {
            int _type = EXPR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincFullExtLexer.g:130:9: ( '###EX###' )
            // parser/flatzinc/FlatzincFullExtLexer.g:130:13: '###EX###'
            {
            match("###EX###"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "EXPR"

    // $ANTLR start "ANNOTATIONS"
    public final void mANNOTATIONS() throws RecognitionException {
        try {
            int _type = ANNOTATIONS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincFullExtLexer.g:131:12: ( '###AS###' )
            // parser/flatzinc/FlatzincFullExtLexer.g:131:13: '###AS###'
            {
            match("###AS###"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "ANNOTATIONS"

    // $ANTLR start "STRUC"
    public final void mSTRUC() throws RecognitionException {
        try {
            int _type = STRUC;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincFullExtLexer.g:133:6: ( '###ST###' )
            // parser/flatzinc/FlatzincFullExtLexer.g:133:7: '###ST###'
            {
            match("###ST###"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "STRUC"

    // $ANTLR start "STREG"
    public final void mSTREG() throws RecognitionException {
        try {
            int _type = STREG;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincFullExtLexer.g:134:6: ( '###SR###' )
            // parser/flatzinc/FlatzincFullExtLexer.g:134:7: '###SR###'
            {
            match("###SR###"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "STREG"

    // $ANTLR start "IDENTIFIER"
    public final void mIDENTIFIER() throws RecognitionException {
        try {
            int _type = IDENTIFIER;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincFullExtLexer.g:143:5: ( ( 'a' .. 'z' | 'A' .. 'Z' | '_' ) ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '_' )* )
            // parser/flatzinc/FlatzincFullExtLexer.g:143:9: ( 'a' .. 'z' | 'A' .. 'Z' | '_' ) ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '_' )*
            {
            if ( (input.LA(1) >= 'A' && input.LA(1) <= 'Z')||input.LA(1)=='_'||(input.LA(1) >= 'a' && input.LA(1) <= 'z') ) {
                input.consume();
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;
            }


            // parser/flatzinc/FlatzincFullExtLexer.g:143:33: ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '_' )*
            loop1:
            do {
                int alt1=2;
                switch ( input.LA(1) ) {
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
                case 'z':
                    {
                    alt1=1;
                    }
                    break;

                }

                switch (alt1) {
            	case 1 :
            	    // parser/flatzinc/FlatzincFullExtLexer.g:
            	    {
            	    if ( (input.LA(1) >= '0' && input.LA(1) <= '9')||(input.LA(1) >= 'A' && input.LA(1) <= 'Z')||input.LA(1)=='_'||(input.LA(1) >= 'a' && input.LA(1) <= 'z') ) {
            	        input.consume();
            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;
            	    }


            	    }
            	    break;

            	default :
            	    break loop1;
                }
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "IDENTIFIER"

    // $ANTLR start "COMMENT"
    public final void mCOMMENT() throws RecognitionException {
        try {
            int _type = COMMENT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincFullExtLexer.g:148:5: ( '%' (~ ( '\\n' | '\\r' ) )* ( '\\r' )? '\\n' )
            // parser/flatzinc/FlatzincFullExtLexer.g:148:9: '%' (~ ( '\\n' | '\\r' ) )* ( '\\r' )? '\\n'
            {
            match('%'); 

            // parser/flatzinc/FlatzincFullExtLexer.g:148:13: (~ ( '\\n' | '\\r' ) )*
            loop2:
            do {
                int alt2=2;
                int LA2_0 = input.LA(1);

                if ( ((LA2_0 >= '\u0000' && LA2_0 <= '\t')||(LA2_0 >= '\u000B' && LA2_0 <= '\f')||(LA2_0 >= '\u000E' && LA2_0 <= '\uFFFF')) ) {
                    alt2=1;
                }


                switch (alt2) {
            	case 1 :
            	    // parser/flatzinc/FlatzincFullExtLexer.g:
            	    {
            	    if ( (input.LA(1) >= '\u0000' && input.LA(1) <= '\t')||(input.LA(1) >= '\u000B' && input.LA(1) <= '\f')||(input.LA(1) >= '\u000E' && input.LA(1) <= '\uFFFF') ) {
            	        input.consume();
            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;
            	    }


            	    }
            	    break;

            	default :
            	    break loop2;
                }
            } while (true);


            // parser/flatzinc/FlatzincFullExtLexer.g:148:27: ( '\\r' )?
            int alt3=2;
            switch ( input.LA(1) ) {
                case '\r':
                    {
                    alt3=1;
                    }
                    break;
            }

            switch (alt3) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtLexer.g:148:27: '\\r'
                    {
                    match('\r'); 

                    }
                    break;

            }


            match('\n'); 

            _channel=HIDDEN;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "COMMENT"

    // $ANTLR start "WS"
    public final void mWS() throws RecognitionException {
        try {
            int _type = WS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincFullExtLexer.g:151:5: ( ( ' ' | '\\t' | '\\r' | '\\n' ) )
            // parser/flatzinc/FlatzincFullExtLexer.g:151:9: ( ' ' | '\\t' | '\\r' | '\\n' )
            {
            if ( (input.LA(1) >= '\t' && input.LA(1) <= '\n')||input.LA(1)=='\r'||input.LA(1)==' ' ) {
                input.consume();
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;
            }


            _channel=HIDDEN;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "WS"

    // $ANTLR start "INT_CONST"
    public final void mINT_CONST() throws RecognitionException {
        try {
            int _type = INT_CONST;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincFullExtLexer.g:163:5: ( ( '+' | '-' )? ( '0' .. '9' )+ )
            // parser/flatzinc/FlatzincFullExtLexer.g:163:9: ( '+' | '-' )? ( '0' .. '9' )+
            {
            // parser/flatzinc/FlatzincFullExtLexer.g:163:9: ( '+' | '-' )?
            int alt4=2;
            switch ( input.LA(1) ) {
                case '+':
                case '-':
                    {
                    alt4=1;
                    }
                    break;
            }

            switch (alt4) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtLexer.g:
                    {
                    if ( input.LA(1)=='+'||input.LA(1)=='-' ) {
                        input.consume();
                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;
                    }


                    }
                    break;

            }


            // parser/flatzinc/FlatzincFullExtLexer.g:163:20: ( '0' .. '9' )+
            int cnt5=0;
            loop5:
            do {
                int alt5=2;
                switch ( input.LA(1) ) {
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
                    {
                    alt5=1;
                    }
                    break;

                }

                switch (alt5) {
            	case 1 :
            	    // parser/flatzinc/FlatzincFullExtLexer.g:
            	    {
            	    if ( (input.LA(1) >= '0' && input.LA(1) <= '9') ) {
            	        input.consume();
            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;
            	    }


            	    }
            	    break;

            	default :
            	    if ( cnt5 >= 1 ) break loop5;
                        EarlyExitException eee =
                            new EarlyExitException(5, input);
                        throw eee;
                }
                cnt5++;
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "INT_CONST"

    // $ANTLR start "STRING"
    public final void mSTRING() throws RecognitionException {
        try {
            int _type = STRING;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincFullExtLexer.g:173:5: ( '\"' ( ESC_SEQ |~ ( '\\\\' | '\"' ) )* '\"' )
            // parser/flatzinc/FlatzincFullExtLexer.g:173:8: '\"' ( ESC_SEQ |~ ( '\\\\' | '\"' ) )* '\"'
            {
            match('\"'); 

            // parser/flatzinc/FlatzincFullExtLexer.g:173:12: ( ESC_SEQ |~ ( '\\\\' | '\"' ) )*
            loop6:
            do {
                int alt6=3;
                int LA6_0 = input.LA(1);

                if ( (LA6_0=='\\') ) {
                    alt6=1;
                }
                else if ( ((LA6_0 >= '\u0000' && LA6_0 <= '!')||(LA6_0 >= '#' && LA6_0 <= '[')||(LA6_0 >= ']' && LA6_0 <= '\uFFFF')) ) {
                    alt6=2;
                }


                switch (alt6) {
            	case 1 :
            	    // parser/flatzinc/FlatzincFullExtLexer.g:173:14: ESC_SEQ
            	    {
            	    mESC_SEQ(); 


            	    }
            	    break;
            	case 2 :
            	    // parser/flatzinc/FlatzincFullExtLexer.g:173:24: ~ ( '\\\\' | '\"' )
            	    {
            	    if ( (input.LA(1) >= '\u0000' && input.LA(1) <= '!')||(input.LA(1) >= '#' && input.LA(1) <= '[')||(input.LA(1) >= ']' && input.LA(1) <= '\uFFFF') ) {
            	        input.consume();
            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;
            	    }


            	    }
            	    break;

            	default :
            	    break loop6;
                }
            } while (true);


            match('\"'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "STRING"

    // $ANTLR start "CHAR"
    public final void mCHAR() throws RecognitionException {
        try {
            int _type = CHAR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parser/flatzinc/FlatzincFullExtLexer.g:176:5: ( '\\'' ( ESC_SEQ |~ ( '\\'' | '\\\\' ) ) '\\'' )
            // parser/flatzinc/FlatzincFullExtLexer.g:176:8: '\\'' ( ESC_SEQ |~ ( '\\'' | '\\\\' ) ) '\\''
            {
            match('\''); 

            // parser/flatzinc/FlatzincFullExtLexer.g:176:13: ( ESC_SEQ |~ ( '\\'' | '\\\\' ) )
            int alt7=2;
            int LA7_0 = input.LA(1);

            if ( (LA7_0=='\\') ) {
                alt7=1;
            }
            else if ( ((LA7_0 >= '\u0000' && LA7_0 <= '&')||(LA7_0 >= '(' && LA7_0 <= '[')||(LA7_0 >= ']' && LA7_0 <= '\uFFFF')) ) {
                alt7=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 7, 0, input);

                throw nvae;

            }
            switch (alt7) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtLexer.g:176:15: ESC_SEQ
                    {
                    mESC_SEQ(); 


                    }
                    break;
                case 2 :
                    // parser/flatzinc/FlatzincFullExtLexer.g:176:25: ~ ( '\\'' | '\\\\' )
                    {
                    if ( (input.LA(1) >= '\u0000' && input.LA(1) <= '&')||(input.LA(1) >= '(' && input.LA(1) <= '[')||(input.LA(1) >= ']' && input.LA(1) <= '\uFFFF') ) {
                        input.consume();
                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
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
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "CHAR"

    // $ANTLR start "EXPONENT"
    public final void mEXPONENT() throws RecognitionException {
        try {
            // parser/flatzinc/FlatzincFullExtLexer.g:185:10: ( ( 'e' | 'E' ) ( '+' | '-' )? ( '0' .. '9' )+ )
            // parser/flatzinc/FlatzincFullExtLexer.g:185:12: ( 'e' | 'E' ) ( '+' | '-' )? ( '0' .. '9' )+
            {
            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;
            }


            // parser/flatzinc/FlatzincFullExtLexer.g:185:22: ( '+' | '-' )?
            int alt8=2;
            switch ( input.LA(1) ) {
                case '+':
                case '-':
                    {
                    alt8=1;
                    }
                    break;
            }

            switch (alt8) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtLexer.g:
                    {
                    if ( input.LA(1)=='+'||input.LA(1)=='-' ) {
                        input.consume();
                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;
                    }


                    }
                    break;

            }


            // parser/flatzinc/FlatzincFullExtLexer.g:185:33: ( '0' .. '9' )+
            int cnt9=0;
            loop9:
            do {
                int alt9=2;
                switch ( input.LA(1) ) {
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
                    {
                    alt9=1;
                    }
                    break;

                }

                switch (alt9) {
            	case 1 :
            	    // parser/flatzinc/FlatzincFullExtLexer.g:
            	    {
            	    if ( (input.LA(1) >= '0' && input.LA(1) <= '9') ) {
            	        input.consume();
            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;
            	    }


            	    }
            	    break;

            	default :
            	    if ( cnt9 >= 1 ) break loop9;
                        EarlyExitException eee =
                            new EarlyExitException(9, input);
                        throw eee;
                }
                cnt9++;
            } while (true);


            }


        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "EXPONENT"

    // $ANTLR start "ESC_SEQ"
    public final void mESC_SEQ() throws RecognitionException {
        try {
            // parser/flatzinc/FlatzincFullExtLexer.g:189:5: ( '\\\\' ( 'b' | 't' | 'n' | 'f' | 'r' | '\\\"' | '\\'' | '\\\\' ) | UNICODE_ESC | OCTAL_ESC )
            int alt10=3;
            switch ( input.LA(1) ) {
            case '\\':
                {
                switch ( input.LA(2) ) {
                case '\"':
                case '\'':
                case '\\':
                case 'b':
                case 'f':
                case 'n':
                case 'r':
                case 't':
                    {
                    alt10=1;
                    }
                    break;
                case 'u':
                    {
                    alt10=2;
                    }
                    break;
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                    {
                    alt10=3;
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
                case 1 :
                    // parser/flatzinc/FlatzincFullExtLexer.g:189:9: '\\\\' ( 'b' | 't' | 'n' | 'f' | 'r' | '\\\"' | '\\'' | '\\\\' )
                    {
                    match('\\'); 

                    if ( input.LA(1)=='\"'||input.LA(1)=='\''||input.LA(1)=='\\'||input.LA(1)=='b'||input.LA(1)=='f'||input.LA(1)=='n'||input.LA(1)=='r'||input.LA(1)=='t' ) {
                        input.consume();
                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;
                    }


                    }
                    break;
                case 2 :
                    // parser/flatzinc/FlatzincFullExtLexer.g:190:9: UNICODE_ESC
                    {
                    mUNICODE_ESC(); 


                    }
                    break;
                case 3 :
                    // parser/flatzinc/FlatzincFullExtLexer.g:191:9: OCTAL_ESC
                    {
                    mOCTAL_ESC(); 


                    }
                    break;

            }

        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "ESC_SEQ"

    // $ANTLR start "OCTAL_ESC"
    public final void mOCTAL_ESC() throws RecognitionException {
        try {
            // parser/flatzinc/FlatzincFullExtLexer.g:196:5: ( '\\\\' ( '0' .. '3' ) ( '0' .. '7' ) ( '0' .. '7' ) | '\\\\' ( '0' .. '7' ) ( '0' .. '7' ) | '\\\\' ( '0' .. '7' ) )
            int alt11=3;
            switch ( input.LA(1) ) {
            case '\\':
                {
                switch ( input.LA(2) ) {
                case '0':
                case '1':
                case '2':
                case '3':
                    {
                    switch ( input.LA(3) ) {
                    case '0':
                    case '1':
                    case '2':
                    case '3':
                    case '4':
                    case '5':
                    case '6':
                    case '7':
                        {
                        switch ( input.LA(4) ) {
                        case '0':
                        case '1':
                        case '2':
                        case '3':
                        case '4':
                        case '5':
                        case '6':
                        case '7':
                            {
                            alt11=1;
                            }
                            break;
                        default:
                            alt11=2;
                        }

                        }
                        break;
                    default:
                        alt11=3;
                    }

                    }
                    break;
                case '4':
                case '5':
                case '6':
                case '7':
                    {
                    switch ( input.LA(3) ) {
                    case '0':
                    case '1':
                    case '2':
                    case '3':
                    case '4':
                    case '5':
                    case '6':
                    case '7':
                        {
                        alt11=2;
                        }
                        break;
                    default:
                        alt11=3;
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
                case 1 :
                    // parser/flatzinc/FlatzincFullExtLexer.g:196:9: '\\\\' ( '0' .. '3' ) ( '0' .. '7' ) ( '0' .. '7' )
                    {
                    match('\\'); 

                    if ( (input.LA(1) >= '0' && input.LA(1) <= '3') ) {
                        input.consume();
                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;
                    }


                    if ( (input.LA(1) >= '0' && input.LA(1) <= '7') ) {
                        input.consume();
                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;
                    }


                    if ( (input.LA(1) >= '0' && input.LA(1) <= '7') ) {
                        input.consume();
                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;
                    }


                    }
                    break;
                case 2 :
                    // parser/flatzinc/FlatzincFullExtLexer.g:197:9: '\\\\' ( '0' .. '7' ) ( '0' .. '7' )
                    {
                    match('\\'); 

                    if ( (input.LA(1) >= '0' && input.LA(1) <= '7') ) {
                        input.consume();
                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;
                    }


                    if ( (input.LA(1) >= '0' && input.LA(1) <= '7') ) {
                        input.consume();
                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;
                    }


                    }
                    break;
                case 3 :
                    // parser/flatzinc/FlatzincFullExtLexer.g:198:9: '\\\\' ( '0' .. '7' )
                    {
                    match('\\'); 

                    if ( (input.LA(1) >= '0' && input.LA(1) <= '7') ) {
                        input.consume();
                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;
                    }


                    }
                    break;

            }

        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "OCTAL_ESC"

    // $ANTLR start "HEX_DIGIT"
    public final void mHEX_DIGIT() throws RecognitionException {
        try {
            // parser/flatzinc/FlatzincFullExtLexer.g:202:11: ( ( '0' .. '9' | 'a' .. 'f' | 'A' .. 'F' ) )
            // parser/flatzinc/FlatzincFullExtLexer.g:
            {
            if ( (input.LA(1) >= '0' && input.LA(1) <= '9')||(input.LA(1) >= 'A' && input.LA(1) <= 'F')||(input.LA(1) >= 'a' && input.LA(1) <= 'f') ) {
                input.consume();
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;
            }


            }


        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "HEX_DIGIT"

    // $ANTLR start "UNICODE_ESC"
    public final void mUNICODE_ESC() throws RecognitionException {
        try {
            // parser/flatzinc/FlatzincFullExtLexer.g:206:5: ( '\\\\' 'u' HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT )
            // parser/flatzinc/FlatzincFullExtLexer.g:206:9: '\\\\' 'u' HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT
            {
            match('\\'); 

            match('u'); 

            mHEX_DIGIT(); 


            mHEX_DIGIT(); 


            mHEX_DIGIT(); 


            mHEX_DIGIT(); 


            }


        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "UNICODE_ESC"

    public void mTokens() throws RecognitionException {
        // parser/flatzinc/FlatzincFullExtLexer.g:1:8: ( BOOL | TRUE | FALSE | INT | FLOAT | SET | OF | ARRAY | VAR | PAR | PREDICATE | CONSTRAINT | SOLVE | SATISFY | MINIMIZE | MAXIMIZE | DD | DO | LB | RB | CM | LS | RS | EQ | PL | MN | SC | CL | DC | LP | RP | AS | EACH | QUEUE | LIST | HEAP | ONE | WONE | FOR | WFOR | ORDERBY | AND | OR | NOT | IN | REV | OEQ | ONQ | OLT | OGT | OLQ | OGQ | KEY | CSTR | PROP | VNAME | VCARD | CNAME | CARITY | PPRIO | PARITY | PPRIOD | ANY | MIN | MAX | SUM | SIZE | APAR | ARRPAR | AVAR | ARRVAR | INDEX | EXPR | ANNOTATIONS | STRUC | STREG | IDENTIFIER | COMMENT | WS | INT_CONST | STRING | CHAR )
        int alt12=82;
        alt12 = dfa12.predict(input);
        switch (alt12) {
            case 1 :
                // parser/flatzinc/FlatzincFullExtLexer.g:1:10: BOOL
                {
                mBOOL(); 


                }
                break;
            case 2 :
                // parser/flatzinc/FlatzincFullExtLexer.g:1:15: TRUE
                {
                mTRUE(); 


                }
                break;
            case 3 :
                // parser/flatzinc/FlatzincFullExtLexer.g:1:20: FALSE
                {
                mFALSE(); 


                }
                break;
            case 4 :
                // parser/flatzinc/FlatzincFullExtLexer.g:1:26: INT
                {
                mINT(); 


                }
                break;
            case 5 :
                // parser/flatzinc/FlatzincFullExtLexer.g:1:30: FLOAT
                {
                mFLOAT(); 


                }
                break;
            case 6 :
                // parser/flatzinc/FlatzincFullExtLexer.g:1:36: SET
                {
                mSET(); 


                }
                break;
            case 7 :
                // parser/flatzinc/FlatzincFullExtLexer.g:1:40: OF
                {
                mOF(); 


                }
                break;
            case 8 :
                // parser/flatzinc/FlatzincFullExtLexer.g:1:43: ARRAY
                {
                mARRAY(); 


                }
                break;
            case 9 :
                // parser/flatzinc/FlatzincFullExtLexer.g:1:49: VAR
                {
                mVAR(); 


                }
                break;
            case 10 :
                // parser/flatzinc/FlatzincFullExtLexer.g:1:53: PAR
                {
                mPAR(); 


                }
                break;
            case 11 :
                // parser/flatzinc/FlatzincFullExtLexer.g:1:57: PREDICATE
                {
                mPREDICATE(); 


                }
                break;
            case 12 :
                // parser/flatzinc/FlatzincFullExtLexer.g:1:67: CONSTRAINT
                {
                mCONSTRAINT(); 


                }
                break;
            case 13 :
                // parser/flatzinc/FlatzincFullExtLexer.g:1:78: SOLVE
                {
                mSOLVE(); 


                }
                break;
            case 14 :
                // parser/flatzinc/FlatzincFullExtLexer.g:1:84: SATISFY
                {
                mSATISFY(); 


                }
                break;
            case 15 :
                // parser/flatzinc/FlatzincFullExtLexer.g:1:92: MINIMIZE
                {
                mMINIMIZE(); 


                }
                break;
            case 16 :
                // parser/flatzinc/FlatzincFullExtLexer.g:1:101: MAXIMIZE
                {
                mMAXIMIZE(); 


                }
                break;
            case 17 :
                // parser/flatzinc/FlatzincFullExtLexer.g:1:110: DD
                {
                mDD(); 


                }
                break;
            case 18 :
                // parser/flatzinc/FlatzincFullExtLexer.g:1:113: DO
                {
                mDO(); 


                }
                break;
            case 19 :
                // parser/flatzinc/FlatzincFullExtLexer.g:1:116: LB
                {
                mLB(); 


                }
                break;
            case 20 :
                // parser/flatzinc/FlatzincFullExtLexer.g:1:119: RB
                {
                mRB(); 


                }
                break;
            case 21 :
                // parser/flatzinc/FlatzincFullExtLexer.g:1:122: CM
                {
                mCM(); 


                }
                break;
            case 22 :
                // parser/flatzinc/FlatzincFullExtLexer.g:1:125: LS
                {
                mLS(); 


                }
                break;
            case 23 :
                // parser/flatzinc/FlatzincFullExtLexer.g:1:128: RS
                {
                mRS(); 


                }
                break;
            case 24 :
                // parser/flatzinc/FlatzincFullExtLexer.g:1:131: EQ
                {
                mEQ(); 


                }
                break;
            case 25 :
                // parser/flatzinc/FlatzincFullExtLexer.g:1:134: PL
                {
                mPL(); 


                }
                break;
            case 26 :
                // parser/flatzinc/FlatzincFullExtLexer.g:1:137: MN
                {
                mMN(); 


                }
                break;
            case 27 :
                // parser/flatzinc/FlatzincFullExtLexer.g:1:140: SC
                {
                mSC(); 


                }
                break;
            case 28 :
                // parser/flatzinc/FlatzincFullExtLexer.g:1:143: CL
                {
                mCL(); 


                }
                break;
            case 29 :
                // parser/flatzinc/FlatzincFullExtLexer.g:1:146: DC
                {
                mDC(); 


                }
                break;
            case 30 :
                // parser/flatzinc/FlatzincFullExtLexer.g:1:149: LP
                {
                mLP(); 


                }
                break;
            case 31 :
                // parser/flatzinc/FlatzincFullExtLexer.g:1:152: RP
                {
                mRP(); 


                }
                break;
            case 32 :
                // parser/flatzinc/FlatzincFullExtLexer.g:1:155: AS
                {
                mAS(); 


                }
                break;
            case 33 :
                // parser/flatzinc/FlatzincFullExtLexer.g:1:158: EACH
                {
                mEACH(); 


                }
                break;
            case 34 :
                // parser/flatzinc/FlatzincFullExtLexer.g:1:163: QUEUE
                {
                mQUEUE(); 


                }
                break;
            case 35 :
                // parser/flatzinc/FlatzincFullExtLexer.g:1:169: LIST
                {
                mLIST(); 


                }
                break;
            case 36 :
                // parser/flatzinc/FlatzincFullExtLexer.g:1:174: HEAP
                {
                mHEAP(); 


                }
                break;
            case 37 :
                // parser/flatzinc/FlatzincFullExtLexer.g:1:179: ONE
                {
                mONE(); 


                }
                break;
            case 38 :
                // parser/flatzinc/FlatzincFullExtLexer.g:1:183: WONE
                {
                mWONE(); 


                }
                break;
            case 39 :
                // parser/flatzinc/FlatzincFullExtLexer.g:1:188: FOR
                {
                mFOR(); 


                }
                break;
            case 40 :
                // parser/flatzinc/FlatzincFullExtLexer.g:1:192: WFOR
                {
                mWFOR(); 


                }
                break;
            case 41 :
                // parser/flatzinc/FlatzincFullExtLexer.g:1:197: ORDERBY
                {
                mORDERBY(); 


                }
                break;
            case 42 :
                // parser/flatzinc/FlatzincFullExtLexer.g:1:205: AND
                {
                mAND(); 


                }
                break;
            case 43 :
                // parser/flatzinc/FlatzincFullExtLexer.g:1:209: OR
                {
                mOR(); 


                }
                break;
            case 44 :
                // parser/flatzinc/FlatzincFullExtLexer.g:1:212: NOT
                {
                mNOT(); 


                }
                break;
            case 45 :
                // parser/flatzinc/FlatzincFullExtLexer.g:1:216: IN
                {
                mIN(); 


                }
                break;
            case 46 :
                // parser/flatzinc/FlatzincFullExtLexer.g:1:219: REV
                {
                mREV(); 


                }
                break;
            case 47 :
                // parser/flatzinc/FlatzincFullExtLexer.g:1:223: OEQ
                {
                mOEQ(); 


                }
                break;
            case 48 :
                // parser/flatzinc/FlatzincFullExtLexer.g:1:227: ONQ
                {
                mONQ(); 


                }
                break;
            case 49 :
                // parser/flatzinc/FlatzincFullExtLexer.g:1:231: OLT
                {
                mOLT(); 


                }
                break;
            case 50 :
                // parser/flatzinc/FlatzincFullExtLexer.g:1:235: OGT
                {
                mOGT(); 


                }
                break;
            case 51 :
                // parser/flatzinc/FlatzincFullExtLexer.g:1:239: OLQ
                {
                mOLQ(); 


                }
                break;
            case 52 :
                // parser/flatzinc/FlatzincFullExtLexer.g:1:243: OGQ
                {
                mOGQ(); 


                }
                break;
            case 53 :
                // parser/flatzinc/FlatzincFullExtLexer.g:1:247: KEY
                {
                mKEY(); 


                }
                break;
            case 54 :
                // parser/flatzinc/FlatzincFullExtLexer.g:1:251: CSTR
                {
                mCSTR(); 


                }
                break;
            case 55 :
                // parser/flatzinc/FlatzincFullExtLexer.g:1:256: PROP
                {
                mPROP(); 


                }
                break;
            case 56 :
                // parser/flatzinc/FlatzincFullExtLexer.g:1:261: VNAME
                {
                mVNAME(); 


                }
                break;
            case 57 :
                // parser/flatzinc/FlatzincFullExtLexer.g:1:267: VCARD
                {
                mVCARD(); 


                }
                break;
            case 58 :
                // parser/flatzinc/FlatzincFullExtLexer.g:1:273: CNAME
                {
                mCNAME(); 


                }
                break;
            case 59 :
                // parser/flatzinc/FlatzincFullExtLexer.g:1:279: CARITY
                {
                mCARITY(); 


                }
                break;
            case 60 :
                // parser/flatzinc/FlatzincFullExtLexer.g:1:286: PPRIO
                {
                mPPRIO(); 


                }
                break;
            case 61 :
                // parser/flatzinc/FlatzincFullExtLexer.g:1:292: PARITY
                {
                mPARITY(); 


                }
                break;
            case 62 :
                // parser/flatzinc/FlatzincFullExtLexer.g:1:299: PPRIOD
                {
                mPPRIOD(); 


                }
                break;
            case 63 :
                // parser/flatzinc/FlatzincFullExtLexer.g:1:306: ANY
                {
                mANY(); 


                }
                break;
            case 64 :
                // parser/flatzinc/FlatzincFullExtLexer.g:1:310: MIN
                {
                mMIN(); 


                }
                break;
            case 65 :
                // parser/flatzinc/FlatzincFullExtLexer.g:1:314: MAX
                {
                mMAX(); 


                }
                break;
            case 66 :
                // parser/flatzinc/FlatzincFullExtLexer.g:1:318: SUM
                {
                mSUM(); 


                }
                break;
            case 67 :
                // parser/flatzinc/FlatzincFullExtLexer.g:1:322: SIZE
                {
                mSIZE(); 


                }
                break;
            case 68 :
                // parser/flatzinc/FlatzincFullExtLexer.g:1:327: APAR
                {
                mAPAR(); 


                }
                break;
            case 69 :
                // parser/flatzinc/FlatzincFullExtLexer.g:1:332: ARRPAR
                {
                mARRPAR(); 


                }
                break;
            case 70 :
                // parser/flatzinc/FlatzincFullExtLexer.g:1:339: AVAR
                {
                mAVAR(); 


                }
                break;
            case 71 :
                // parser/flatzinc/FlatzincFullExtLexer.g:1:344: ARRVAR
                {
                mARRVAR(); 


                }
                break;
            case 72 :
                // parser/flatzinc/FlatzincFullExtLexer.g:1:351: INDEX
                {
                mINDEX(); 


                }
                break;
            case 73 :
                // parser/flatzinc/FlatzincFullExtLexer.g:1:357: EXPR
                {
                mEXPR(); 


                }
                break;
            case 74 :
                // parser/flatzinc/FlatzincFullExtLexer.g:1:362: ANNOTATIONS
                {
                mANNOTATIONS(); 


                }
                break;
            case 75 :
                // parser/flatzinc/FlatzincFullExtLexer.g:1:374: STRUC
                {
                mSTRUC(); 


                }
                break;
            case 76 :
                // parser/flatzinc/FlatzincFullExtLexer.g:1:380: STREG
                {
                mSTREG(); 


                }
                break;
            case 77 :
                // parser/flatzinc/FlatzincFullExtLexer.g:1:386: IDENTIFIER
                {
                mIDENTIFIER(); 


                }
                break;
            case 78 :
                // parser/flatzinc/FlatzincFullExtLexer.g:1:397: COMMENT
                {
                mCOMMENT(); 


                }
                break;
            case 79 :
                // parser/flatzinc/FlatzincFullExtLexer.g:1:405: WS
                {
                mWS(); 


                }
                break;
            case 80 :
                // parser/flatzinc/FlatzincFullExtLexer.g:1:408: INT_CONST
                {
                mINT_CONST(); 


                }
                break;
            case 81 :
                // parser/flatzinc/FlatzincFullExtLexer.g:1:418: STRING
                {
                mSTRING(); 


                }
                break;
            case 82 :
                // parser/flatzinc/FlatzincFullExtLexer.g:1:425: CHAR
                {
                mCHAR(); 


                }
                break;

        }

    }


    protected DFA12 dfa12 = new DFA12(this);
    static final String DFA12_eotS =
        "\1\uffff\13\46\1\105\5\uffff\1\107\1\110\1\111\1\uffff\1\113\2\uffff"+
        "\5\46\2\uffff\1\123\1\46\1\126\1\130\1\46\7\uffff\5\46\1\141\5\46"+
        "\1\147\3\46\1\153\10\46\10\uffff\6\46\2\uffff\1\46\4\uffff\1\46"+
        "\1\uffff\4\46\1\u0082\1\u0083\1\uffff\1\u0084\2\46\1\u0087\1\46"+
        "\1\uffff\1\u0089\2\46\1\uffff\1\u008c\1\u008e\1\u008f\4\46\1\u0095"+
        "\1\u0097\6\46\1\u009e\1\u009f\1\uffff\1\u00a5\1\u00a6\2\46\3\uffff"+
        "\2\46\1\uffff\1\u00ab\1\uffff\2\46\4\uffff\1\46\1\u00b2\1\46\1\u00b5"+
        "\1\46\1\uffff\1\46\1\uffff\1\u00b8\1\46\1\u00ba\1\u00bb\1\u00bc"+
        "\1\u00bd\11\uffff\1\u00c5\1\u00c6\1\u00c7\1\46\1\uffff\1\46\1\u00ca"+
        "\2\uffff\1\46\2\uffff\1\46\2\uffff\2\46\1\uffff\1\u00d3\16\uffff"+
        "\1\46\2\uffff\1\46\2\uffff\1\46\2\uffff\2\46\1\uffff\1\u00da\1\46"+
        "\1\uffff\3\46\1\uffff\1\46\1\uffff\1\46\1\u00e3\1\u00e4\1\u00e5"+
        "\1\uffff\1\46\5\uffff\1\u00e9\1\uffff";
    static final String DFA12_eofS =
        "\u00ea\uffff";
    static final String DFA12_minS =
        "\1\11\1\157\1\162\1\141\1\156\1\141\1\146\1\156\2\141\1\157\1\141"+
        "\1\56\5\uffff\1\75\2\60\1\uffff\1\72\2\uffff\1\141\1\165\1\151\1"+
        "\145\1\146\2\uffff\1\75\1\145\2\75\1\145\1\43\6\uffff\1\157\1\165"+
        "\1\154\1\157\1\162\1\60\1\164\1\154\1\164\1\155\1\172\1\60\1\145"+
        "\1\144\1\162\1\60\1\171\2\162\1\145\1\156\1\164\1\156\1\170\10\uffff"+
        "\1\143\1\145\1\163\1\141\1\156\1\157\2\uffff\1\166\4\uffff\1\171"+
        "\1\43\1\154\1\145\1\163\1\141\2\60\1\uffff\1\60\1\166\1\151\1\60"+
        "\1\145\1\uffff\1\60\1\145\1\141\1\uffff\1\60\1\56\1\60\1\144\1\160"+
        "\1\163\1\162\2\60\1\150\1\165\1\164\1\160\1\145\1\162\2\60\1\101"+
        "\2\60\1\145\1\164\3\uffff\1\145\1\163\1\uffff\1\60\1\uffff\1\162"+
        "\1\171\1\uffff\1\143\2\uffff\1\151\1\56\1\164\1\56\1\155\1\uffff"+
        "\1\155\1\uffff\1\60\1\145\4\60\2\uffff\2\120\2\uffff\1\122\2\uffff"+
        "\3\60\1\146\1\uffff\1\40\1\60\2\uffff\1\143\1\141\1\uffff\1\162"+
        "\1\141\1\uffff\2\151\1\uffff\1\60\16\uffff\1\171\2\uffff\1\141\1"+
        "\162\1\uffff\1\141\2\uffff\2\172\1\uffff\1\60\1\164\2\151\2\145"+
        "\1\uffff\1\145\1\157\1\156\3\60\1\104\1\164\5\uffff\1\60\1\uffff";
    static final String DFA12_maxS =
        "\1\175\1\157\1\162\1\157\1\156\1\165\1\162\1\163\1\141\1\162\1\163"+
        "\1\151\1\56\5\uffff\1\75\2\71\1\uffff\1\72\2\uffff\1\141\1\165\1"+
        "\151\1\145\1\157\2\uffff\1\75\1\145\2\75\1\145\1\43\6\uffff\1\157"+
        "\1\165\1\154\1\157\1\162\1\172\1\164\1\154\1\164\1\155\2\172\1\145"+
        "\1\144\1\162\1\172\1\171\2\162\1\157\1\156\1\164\1\156\1\170\10"+
        "\uffff\1\143\1\145\1\163\1\141\1\156\1\157\2\uffff\1\166\4\uffff"+
        "\1\171\1\43\1\154\1\145\1\163\1\141\2\172\1\uffff\1\172\1\166\1"+
        "\151\1\172\1\145\1\uffff\1\172\1\145\1\141\1\uffff\3\172\1\144\1"+
        "\160\1\163\1\162\2\172\1\150\1\165\1\164\1\160\1\145\1\162\2\172"+
        "\1\137\2\172\1\145\1\164\3\uffff\1\145\1\163\1\uffff\1\172\1\uffff"+
        "\1\162\1\171\1\uffff\1\156\2\uffff\1\151\1\172\1\164\1\172\1\155"+
        "\1\uffff\1\155\1\uffff\1\172\1\145\4\172\2\uffff\2\126\2\uffff\1"+
        "\124\2\uffff\3\172\1\146\1\uffff\1\40\1\172\2\uffff\1\143\1\160"+
        "\1\uffff\1\162\1\156\1\uffff\2\151\1\uffff\1\172\16\uffff\1\171"+
        "\2\uffff\1\141\1\162\1\uffff\1\141\2\uffff\2\172\1\uffff\1\172\1"+
        "\164\2\151\2\145\1\uffff\1\145\1\157\1\156\3\172\1\162\1\164\5\uffff"+
        "\1\172\1\uffff";
    static final String DFA12_acceptS =
        "\15\uffff\1\23\1\24\1\25\1\26\1\27\3\uffff\1\33\1\uffff\1\36\1\37"+
        "\5\uffff\1\52\1\53\6\uffff\1\115\1\116\1\117\1\120\1\121\1\122\30"+
        "\uffff\1\21\1\22\1\57\1\30\1\31\1\32\1\35\1\34\6\uffff\1\60\1\54"+
        "\1\uffff\1\63\1\61\1\64\1\62\10\uffff\1\55\5\uffff\1\7\3\uffff\1"+
        "\40\26\uffff\1\47\1\4\1\6\2\uffff\1\102\1\uffff\1\45\2\uffff\1\77"+
        "\1\uffff\1\11\1\12\5\uffff\1\100\1\uffff\1\101\6\uffff\1\56\1\65"+
        "\2\uffff\1\110\1\111\1\uffff\1\1\1\2\4\uffff\1\103\2\uffff\1\70"+
        "\1\71\2\uffff\1\67\2\uffff\1\66\2\uffff\1\41\1\uffff\1\43\1\44\1"+
        "\46\1\50\1\104\1\106\1\105\1\107\1\112\1\113\1\114\1\3\1\5\1\15"+
        "\1\uffff\1\51\1\10\2\uffff\1\75\1\uffff\1\72\1\73\2\uffff\1\42\6"+
        "\uffff\1\16\10\uffff\1\17\1\20\1\13\1\74\1\76\1\uffff\1\14";
    static final String DFA12_specialS =
        "\u00ea\uffff}>";
    static final String[] DFA12_transitionS = {
            "\2\50\2\uffff\1\50\22\uffff\1\50\1\40\1\52\1\45\1\uffff\1\47"+
            "\1\36\1\53\1\27\1\30\1\uffff\1\23\1\17\1\24\1\14\1\uffff\12"+
            "\51\1\26\1\25\1\42\1\22\1\43\2\uffff\32\46\1\20\1\uffff\1\21"+
            "\1\uffff\1\46\1\uffff\1\7\1\1\1\12\1\46\1\31\1\3\1\46\1\34\1"+
            "\4\1\46\1\44\1\33\1\13\1\46\1\6\1\11\1\32\1\41\1\5\1\2\1\46"+
            "\1\10\1\35\3\46\1\15\1\37\1\16",
            "\1\54",
            "\1\55",
            "\1\56\12\uffff\1\57\2\uffff\1\60",
            "\1\61",
            "\1\64\3\uffff\1\62\3\uffff\1\66\5\uffff\1\63\5\uffff\1\65",
            "\1\67\7\uffff\1\70\3\uffff\1\71",
            "\1\74\3\uffff\1\72\1\73",
            "\1\75",
            "\1\76\20\uffff\1\77",
            "\1\100\3\uffff\1\101",
            "\1\103\7\uffff\1\102",
            "\1\104",
            "",
            "",
            "",
            "",
            "",
            "\1\106",
            "\12\51",
            "\12\51",
            "",
            "\1\112",
            "",
            "",
            "\1\114",
            "\1\115",
            "\1\116",
            "\1\117",
            "\1\121\10\uffff\1\120",
            "",
            "",
            "\1\122",
            "\1\124",
            "\1\125",
            "\1\127",
            "\1\131",
            "\1\132",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\133",
            "\1\134",
            "\1\135",
            "\1\136",
            "\1\137",
            "\12\46\7\uffff\32\46\4\uffff\1\46\1\uffff\23\46\1\140\6\46",
            "\1\142",
            "\1\143",
            "\1\144",
            "\1\145",
            "\1\146",
            "\12\46\7\uffff\32\46\4\uffff\1\46\1\uffff\32\46",
            "\1\150",
            "\1\151",
            "\1\152",
            "\12\46\7\uffff\32\46\4\uffff\1\46\1\uffff\32\46",
            "\1\154",
            "\1\155",
            "\1\156",
            "\1\157\11\uffff\1\160",
            "\1\161",
            "\1\162",
            "\1\163",
            "\1\164",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\165",
            "\1\166",
            "\1\167",
            "\1\170",
            "\1\171",
            "\1\172",
            "",
            "",
            "\1\173",
            "",
            "",
            "",
            "",
            "\1\174",
            "\1\175",
            "\1\176",
            "\1\177",
            "\1\u0080",
            "\1\u0081",
            "\12\46\7\uffff\32\46\4\uffff\1\46\1\uffff\32\46",
            "\12\46\7\uffff\32\46\4\uffff\1\46\1\uffff\32\46",
            "",
            "\12\46\7\uffff\32\46\4\uffff\1\46\1\uffff\32\46",
            "\1\u0085",
            "\1\u0086",
            "\12\46\7\uffff\32\46\4\uffff\1\46\1\uffff\32\46",
            "\1\u0088",
            "",
            "\12\46\7\uffff\32\46\4\uffff\1\46\1\uffff\32\46",
            "\1\u008a",
            "\1\u008b",
            "",
            "\12\46\7\uffff\32\46\4\uffff\1\46\1\uffff\32\46",
            "\1\u008d\1\uffff\12\46\7\uffff\32\46\4\uffff\1\46\1\uffff\32"+
            "\46",
            "\12\46\7\uffff\32\46\4\uffff\1\46\1\uffff\32\46",
            "\1\u0090",
            "\1\u0091",
            "\1\u0092",
            "\1\u0093",
            "\12\46\7\uffff\32\46\4\uffff\1\46\1\uffff\10\46\1\u0094\21"+
            "\46",
            "\12\46\7\uffff\32\46\4\uffff\1\46\1\uffff\10\46\1\u0096\21"+
            "\46",
            "\1\u0098",
            "\1\u0099",
            "\1\u009a",
            "\1\u009b",
            "\1\u009c",
            "\1\u009d",
            "\12\46\7\uffff\32\46\4\uffff\1\46\1\uffff\32\46",
            "\12\46\7\uffff\32\46\4\uffff\1\46\1\uffff\32\46",
            "\1\u00a1\3\uffff\1\u00a3\3\uffff\1\u00a2\11\uffff\1\u00a4\13"+
            "\uffff\1\u00a0",
            "\12\46\7\uffff\32\46\4\uffff\1\46\1\uffff\32\46",
            "\12\46\7\uffff\32\46\4\uffff\1\46\1\uffff\32\46",
            "\1\u00a7",
            "\1\u00a8",
            "",
            "",
            "",
            "\1\u00a9",
            "\1\u00aa",
            "",
            "\12\46\7\uffff\32\46\4\uffff\1\46\1\uffff\32\46",
            "",
            "\1\u00ac",
            "\1\u00ad",
            "",
            "\1\u00af\12\uffff\1\u00ae",
            "",
            "",
            "\1\u00b0",
            "\1\u00b1\1\uffff\12\46\7\uffff\32\46\4\uffff\1\46\1\uffff\32"+
            "\46",
            "\1\u00b3",
            "\1\u00b4\1\uffff\12\46\7\uffff\32\46\4\uffff\1\46\1\uffff\32"+
            "\46",
            "\1\u00b6",
            "",
            "\1\u00b7",
            "",
            "\12\46\7\uffff\32\46\4\uffff\1\46\1\uffff\32\46",
            "\1\u00b9",
            "\12\46\7\uffff\32\46\4\uffff\1\46\1\uffff\32\46",
            "\12\46\7\uffff\32\46\4\uffff\1\46\1\uffff\32\46",
            "\12\46\7\uffff\32\46\4\uffff\1\46\1\uffff\32\46",
            "\12\46\7\uffff\32\46\4\uffff\1\46\1\uffff\32\46",
            "",
            "",
            "\1\u00be\5\uffff\1\u00bf",
            "\1\u00c0\2\uffff\1\u00c2\2\uffff\1\u00c1",
            "",
            "",
            "\1\u00c4\1\uffff\1\u00c3",
            "",
            "",
            "\12\46\7\uffff\32\46\4\uffff\1\46\1\uffff\32\46",
            "\12\46\7\uffff\32\46\4\uffff\1\46\1\uffff\32\46",
            "\12\46\7\uffff\32\46\4\uffff\1\46\1\uffff\32\46",
            "\1\u00c8",
            "",
            "\1\u00c9",
            "\12\46\7\uffff\32\46\4\uffff\1\46\1\uffff\32\46",
            "",
            "",
            "\1\u00cb",
            "\1\u00cd\16\uffff\1\u00cc",
            "",
            "\1\u00ce",
            "\1\u00d0\14\uffff\1\u00cf",
            "",
            "\1\u00d1",
            "\1\u00d2",
            "",
            "\12\46\7\uffff\32\46\4\uffff\1\46\1\uffff\32\46",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\u00d4",
            "",
            "",
            "\1\u00d5",
            "\1\u00d6",
            "",
            "\1\u00d7",
            "",
            "",
            "\1\u00d8",
            "\1\u00d9",
            "",
            "\12\46\7\uffff\32\46\4\uffff\1\46\1\uffff\32\46",
            "\1\u00db",
            "\1\u00dc",
            "\1\u00dd",
            "\1\u00de",
            "\1\u00df",
            "",
            "\1\u00e0",
            "\1\u00e1",
            "\1\u00e2",
            "\12\46\7\uffff\32\46\4\uffff\1\46\1\uffff\32\46",
            "\12\46\7\uffff\32\46\4\uffff\1\46\1\uffff\32\46",
            "\12\46\7\uffff\32\46\4\uffff\1\46\1\uffff\32\46",
            "\1\u00e7\55\uffff\1\u00e6",
            "\1\u00e8",
            "",
            "",
            "",
            "",
            "",
            "\12\46\7\uffff\32\46\4\uffff\1\46\1\uffff\32\46",
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
        for (int i=0; i<numStates; i++) {
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
            return "1:1: Tokens : ( BOOL | TRUE | FALSE | INT | FLOAT | SET | OF | ARRAY | VAR | PAR | PREDICATE | CONSTRAINT | SOLVE | SATISFY | MINIMIZE | MAXIMIZE | DD | DO | LB | RB | CM | LS | RS | EQ | PL | MN | SC | CL | DC | LP | RP | AS | EACH | QUEUE | LIST | HEAP | ONE | WONE | FOR | WFOR | ORDERBY | AND | OR | NOT | IN | REV | OEQ | ONQ | OLT | OGT | OLQ | OGQ | KEY | CSTR | PROP | VNAME | VCARD | CNAME | CARITY | PPRIO | PARITY | PPRIOD | ANY | MIN | MAX | SUM | SIZE | APAR | ARRPAR | AVAR | ARRVAR | INDEX | EXPR | ANNOTATIONS | STRUC | STREG | IDENTIFIER | COMMENT | WS | INT_CONST | STRING | CHAR );";
        }
    }
 

}