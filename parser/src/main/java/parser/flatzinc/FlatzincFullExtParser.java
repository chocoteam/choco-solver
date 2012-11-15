// $ANTLR 3.4 parser/flatzinc/FlatzincFullExtParser.g 2012-11-15 14:46:17

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
import java.util.Map;
import java.util.HashMap;

import org.antlr.runtime.tree.*;


@SuppressWarnings({"all", "warnings", "unchecked"})
public class FlatzincFullExtParser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "AND", "ANNOTATIONS", "ANY", "APAR", "ARRAY", "ARRPAR", "ARRVAR", "AS", "AVAR", "BOOL", "CARITY", "CHAR", "CL", "CM", "CNAME", "COMMENT", "CONSTRAINT", "DC", "DD", "DO", "EACH", "EQ", "ESC_SEQ", "EXPONENT", "EXPR", "FALSE", "FLOAT", "FOR", "HEAP", "HEX_DIGIT", "IDENTIFIER", "IN", "INDEX", "INT", "INT_CONST", "KEY", "LB", "LIST", "LP", "LS", "MAX", "MAXIMIZE", "MIN", "MINIMIZE", "MN", "NOT", "OCTAL_ESC", "OEQ", "OF", "OGQ", "OGT", "OLQ", "OLT", "ONE", "ONQ", "OR", "ORDERBY", "PAR", "PARITY", "PIDX", "PL", "PPRIO", "PPRIOD", "PREDICATE", "QUEUE", "RB", "REV", "RP", "RS", "SATISFY", "SC", "SET", "SIZE", "SOLVE", "STREG", "STRING", "STRUC", "SUM", "TRUE", "UNICODE_ESC", "VAR", "VCARD", "VNAME", "WFOR", "WONE", "WS"
    };

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
    public static final int DC=21;
    public static final int DD=22;
    public static final int DO=23;
    public static final int EACH=24;
    public static final int EQ=25;
    public static final int ESC_SEQ=26;
    public static final int EXPONENT=27;
    public static final int EXPR=28;
    public static final int FALSE=29;
    public static final int FLOAT=30;
    public static final int FOR=31;
    public static final int HEAP=32;
    public static final int HEX_DIGIT=33;
    public static final int IDENTIFIER=34;
    public static final int IN=35;
    public static final int INDEX=36;
    public static final int INT=37;
    public static final int INT_CONST=38;
    public static final int KEY=39;
    public static final int LB=40;
    public static final int LIST=41;
    public static final int LP=42;
    public static final int LS=43;
    public static final int MAX=44;
    public static final int MAXIMIZE=45;
    public static final int MIN=46;
    public static final int MINIMIZE=47;
    public static final int MN=48;
    public static final int NOT=49;
    public static final int OCTAL_ESC=50;
    public static final int OEQ=51;
    public static final int OF=52;
    public static final int OGQ=53;
    public static final int OGT=54;
    public static final int OLQ=55;
    public static final int OLT=56;
    public static final int ONE=57;
    public static final int ONQ=58;
    public static final int OR=59;
    public static final int ORDERBY=60;
    public static final int PAR=61;
    public static final int PARITY=62;
    public static final int PIDX=63;
    public static final int PL=64;
    public static final int PPRIO=65;
    public static final int PPRIOD=66;
    public static final int PREDICATE=67;
    public static final int QUEUE=68;
    public static final int RB=69;
    public static final int REV=70;
    public static final int RP=71;
    public static final int RS=72;
    public static final int SATISFY=73;
    public static final int SC=74;
    public static final int SET=75;
    public static final int SIZE=76;
    public static final int SOLVE=77;
    public static final int STREG=78;
    public static final int STRING=79;
    public static final int STRUC=80;
    public static final int SUM=81;
    public static final int TRUE=82;
    public static final int UNICODE_ESC=83;
    public static final int VAR=84;
    public static final int VCARD=85;
    public static final int VNAME=86;
    public static final int WFOR=87;
    public static final int WONE=88;
    public static final int WS=89;

    // delegates
    public Parser[] getDelegates() {
        return new Parser[] {};
    }

    // delegators


    public FlatzincFullExtParser(TokenStream input) {
        this(input, new RecognizerSharedState());
    }
    public FlatzincFullExtParser(TokenStream input, RecognizerSharedState state) {
        super(input, state);
    }

protected TreeAdaptor adaptor = new CommonTreeAdaptor();

public void setTreeAdaptor(TreeAdaptor adaptor) {
    this.adaptor = adaptor;
}
public TreeAdaptor getTreeAdaptor() {
    return adaptor;
}
    public String[] getTokenNames() { return FlatzincFullExtParser.tokenNames; }
    public String getGrammarFileName() { return "parser/flatzinc/FlatzincFullExtParser.g"; }


    public static class flatzinc_ext_model_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "flatzinc_ext_model"
    // parser/flatzinc/FlatzincFullExtParser.g:42:1: flatzinc_ext_model : ( pred_decl )* ( param_decl )* ( var_decl )* ( constraint )* ( group_decl )* ( structure )? solve_goal ;
    public final FlatzincFullExtParser.flatzinc_ext_model_return flatzinc_ext_model() throws RecognitionException {
        FlatzincFullExtParser.flatzinc_ext_model_return retval = new FlatzincFullExtParser.flatzinc_ext_model_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        FlatzincFullExtParser.pred_decl_return pred_decl1 =null;

        FlatzincFullExtParser.param_decl_return param_decl2 =null;

        FlatzincFullExtParser.var_decl_return var_decl3 =null;

        FlatzincFullExtParser.constraint_return constraint4 =null;

        FlatzincFullExtParser.group_decl_return group_decl5 =null;

        FlatzincFullExtParser.structure_return structure6 =null;

        FlatzincFullExtParser.solve_goal_return solve_goal7 =null;



        try {
            // parser/flatzinc/FlatzincFullExtParser.g:43:2: ( ( pred_decl )* ( param_decl )* ( var_decl )* ( constraint )* ( group_decl )* ( structure )? solve_goal )
            // parser/flatzinc/FlatzincFullExtParser.g:43:6: ( pred_decl )* ( param_decl )* ( var_decl )* ( constraint )* ( group_decl )* ( structure )? solve_goal
            {
            root_0 = (Object)adaptor.nil();


            // parser/flatzinc/FlatzincFullExtParser.g:43:6: ( pred_decl )*
            loop1:
            do {
                int alt1=2;
                switch ( input.LA(1) ) {
                case PREDICATE:
                    {
                    alt1=1;
                    }
                    break;

                }

                switch (alt1) {
            	case 1 :
            	    // parser/flatzinc/FlatzincFullExtParser.g:43:7: pred_decl
            	    {
            	    pushFollow(FOLLOW_pred_decl_in_flatzinc_ext_model67);
            	    pred_decl1=pred_decl();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) adaptor.addChild(root_0, pred_decl1.getTree());

            	    }
            	    break;

            	default :
            	    break loop1;
                }
            } while (true);


            // parser/flatzinc/FlatzincFullExtParser.g:43:19: ( param_decl )*
            loop2:
            do {
                int alt2=2;
                alt2 = dfa2.predict(input);
                switch (alt2) {
            	case 1 :
            	    // parser/flatzinc/FlatzincFullExtParser.g:43:20: param_decl
            	    {
            	    pushFollow(FOLLOW_param_decl_in_flatzinc_ext_model72);
            	    param_decl2=param_decl();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) adaptor.addChild(root_0, param_decl2.getTree());

            	    }
            	    break;

            	default :
            	    break loop2;
                }
            } while (true);


            // parser/flatzinc/FlatzincFullExtParser.g:43:33: ( var_decl )*
            loop3:
            do {
                int alt3=2;
                switch ( input.LA(1) ) {
                case ARRAY:
                case VAR:
                    {
                    alt3=1;
                    }
                    break;

                }

                switch (alt3) {
            	case 1 :
            	    // parser/flatzinc/FlatzincFullExtParser.g:43:34: var_decl
            	    {
            	    pushFollow(FOLLOW_var_decl_in_flatzinc_ext_model77);
            	    var_decl3=var_decl();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) adaptor.addChild(root_0, var_decl3.getTree());

            	    }
            	    break;

            	default :
            	    break loop3;
                }
            } while (true);


            // parser/flatzinc/FlatzincFullExtParser.g:43:45: ( constraint )*
            loop4:
            do {
                int alt4=2;
                switch ( input.LA(1) ) {
                case CONSTRAINT:
                    {
                    alt4=1;
                    }
                    break;

                }

                switch (alt4) {
            	case 1 :
            	    // parser/flatzinc/FlatzincFullExtParser.g:43:46: constraint
            	    {
            	    pushFollow(FOLLOW_constraint_in_flatzinc_ext_model82);
            	    constraint4=constraint();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) adaptor.addChild(root_0, constraint4.getTree());

            	    }
            	    break;

            	default :
            	    break loop4;
                }
            } while (true);


            // parser/flatzinc/FlatzincFullExtParser.g:43:59: ( group_decl )*
            loop5:
            do {
                int alt5=2;
                switch ( input.LA(1) ) {
                case IDENTIFIER:
                    {
                    switch ( input.LA(2) ) {
                    case CL:
                        {
                        alt5=1;
                        }
                        break;

                    }

                    }
                    break;

                }

                switch (alt5) {
            	case 1 :
            	    // parser/flatzinc/FlatzincFullExtParser.g:43:60: group_decl
            	    {
            	    pushFollow(FOLLOW_group_decl_in_flatzinc_ext_model87);
            	    group_decl5=group_decl();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) adaptor.addChild(root_0, group_decl5.getTree());

            	    }
            	    break;

            	default :
            	    break loop5;
                }
            } while (true);


            // parser/flatzinc/FlatzincFullExtParser.g:43:73: ( structure )?
            int alt6=2;
            switch ( input.LA(1) ) {
                case IDENTIFIER:
                case LIST:
                case MAX:
                case MIN:
                case QUEUE:
                case REV:
                    {
                    alt6=1;
                    }
                    break;
            }

            switch (alt6) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtParser.g:43:74: structure
                    {
                    pushFollow(FOLLOW_structure_in_flatzinc_ext_model92);
                    structure6=structure();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, structure6.getTree());

                    }
                    break;

            }


            pushFollow(FOLLOW_solve_goal_in_flatzinc_ext_model96);
            solve_goal7=solve_goal();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, solve_goal7.getTree());

            }

            retval.stop = input.LT(-1);


            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "flatzinc_ext_model"


    public static class group_decl_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "group_decl"
    // parser/flatzinc/FlatzincFullExtParser.g:59:1: group_decl : IDENTIFIER CL predicates SC -> ^( IDENTIFIER predicates ) ;
    public final FlatzincFullExtParser.group_decl_return group_decl() throws RecognitionException {
        FlatzincFullExtParser.group_decl_return retval = new FlatzincFullExtParser.group_decl_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token IDENTIFIER8=null;
        Token CL9=null;
        Token SC11=null;
        FlatzincFullExtParser.predicates_return predicates10 =null;


        Object IDENTIFIER8_tree=null;
        Object CL9_tree=null;
        Object SC11_tree=null;
        RewriteRuleTokenStream stream_SC=new RewriteRuleTokenStream(adaptor,"token SC");
        RewriteRuleTokenStream stream_IDENTIFIER=new RewriteRuleTokenStream(adaptor,"token IDENTIFIER");
        RewriteRuleTokenStream stream_CL=new RewriteRuleTokenStream(adaptor,"token CL");
        RewriteRuleSubtreeStream stream_predicates=new RewriteRuleSubtreeStream(adaptor,"rule predicates");
        try {
            // parser/flatzinc/FlatzincFullExtParser.g:60:5: ( IDENTIFIER CL predicates SC -> ^( IDENTIFIER predicates ) )
            // parser/flatzinc/FlatzincFullExtParser.g:60:9: IDENTIFIER CL predicates SC
            {
            IDENTIFIER8=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_group_decl125); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_IDENTIFIER.add(IDENTIFIER8);


            CL9=(Token)match(input,CL,FOLLOW_CL_in_group_decl127); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_CL.add(CL9);


            pushFollow(FOLLOW_predicates_in_group_decl129);
            predicates10=predicates();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_predicates.add(predicates10.getTree());

            SC11=(Token)match(input,SC,FOLLOW_SC_in_group_decl131); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_SC.add(SC11);


            // AST REWRITE
            // elements: IDENTIFIER, predicates
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {

            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 61:5: -> ^( IDENTIFIER predicates )
            {
                // parser/flatzinc/FlatzincFullExtParser.g:61:9: ^( IDENTIFIER predicates )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot(
                stream_IDENTIFIER.nextNode()
                , root_1);

                adaptor.addChild(root_1, stream_predicates.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }


            retval.tree = root_0;
            }

            }

            retval.stop = input.LT(-1);


            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "group_decl"


    public static class predicates_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "predicates"
    // parser/flatzinc/FlatzincFullExtParser.g:66:1: predicates : ( predicate | LP predicates ( AND predicates )+ RP -> ^( AND ( predicates )+ ) | LP predicates ( OR predicates )+ RP -> ^( OR ( predicates )+ ) );
    public final FlatzincFullExtParser.predicates_return predicates() throws RecognitionException {
        FlatzincFullExtParser.predicates_return retval = new FlatzincFullExtParser.predicates_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token LP13=null;
        Token AND15=null;
        Token RP17=null;
        Token LP18=null;
        Token OR20=null;
        Token RP22=null;
        FlatzincFullExtParser.predicate_return predicate12 =null;

        FlatzincFullExtParser.predicates_return predicates14 =null;

        FlatzincFullExtParser.predicates_return predicates16 =null;

        FlatzincFullExtParser.predicates_return predicates19 =null;

        FlatzincFullExtParser.predicates_return predicates21 =null;


        Object LP13_tree=null;
        Object AND15_tree=null;
        Object RP17_tree=null;
        Object LP18_tree=null;
        Object OR20_tree=null;
        Object RP22_tree=null;
        RewriteRuleTokenStream stream_AND=new RewriteRuleTokenStream(adaptor,"token AND");
        RewriteRuleTokenStream stream_RP=new RewriteRuleTokenStream(adaptor,"token RP");
        RewriteRuleTokenStream stream_OR=new RewriteRuleTokenStream(adaptor,"token OR");
        RewriteRuleTokenStream stream_LP=new RewriteRuleTokenStream(adaptor,"token LP");
        RewriteRuleSubtreeStream stream_predicates=new RewriteRuleSubtreeStream(adaptor,"rule predicates");
        try {
            // parser/flatzinc/FlatzincFullExtParser.g:67:2: ( predicate | LP predicates ( AND predicates )+ RP -> ^( AND ( predicates )+ ) | LP predicates ( OR predicates )+ RP -> ^( OR ( predicates )+ ) )
            int alt9=3;
            switch ( input.LA(1) ) {
            case CARITY:
            case CNAME:
            case IN:
            case NOT:
            case PARITY:
            case PIDX:
            case PPRIO:
            case PPRIOD:
            case TRUE:
            case VCARD:
            case VNAME:
                {
                alt9=1;
                }
                break;
            case LP:
                {
                int LA9_5 = input.LA(2);

                if ( (synpred9_FlatzincFullExtParser()) ) {
                    alt9=2;
                }
                else if ( (true) ) {
                    alt9=3;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 9, 5, input);

                    throw nvae;

                }
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 9, 0, input);

                throw nvae;

            }

            switch (alt9) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtParser.g:67:4: predicate
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_predicate_in_predicates160);
                    predicate12=predicate();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, predicate12.getTree());

                    }
                    break;
                case 2 :
                    // parser/flatzinc/FlatzincFullExtParser.g:68:4: LP predicates ( AND predicates )+ RP
                    {
                    LP13=(Token)match(input,LP,FOLLOW_LP_in_predicates165); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LP.add(LP13);


                    pushFollow(FOLLOW_predicates_in_predicates167);
                    predicates14=predicates();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_predicates.add(predicates14.getTree());

                    // parser/flatzinc/FlatzincFullExtParser.g:68:18: ( AND predicates )+
                    int cnt7=0;
                    loop7:
                    do {
                        int alt7=2;
                        switch ( input.LA(1) ) {
                        case AND:
                            {
                            alt7=1;
                            }
                            break;

                        }

                        switch (alt7) {
                    	case 1 :
                    	    // parser/flatzinc/FlatzincFullExtParser.g:68:19: AND predicates
                    	    {
                    	    AND15=(Token)match(input,AND,FOLLOW_AND_in_predicates170); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_AND.add(AND15);


                    	    pushFollow(FOLLOW_predicates_in_predicates172);
                    	    predicates16=predicates();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_predicates.add(predicates16.getTree());

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt7 >= 1 ) break loop7;
                    	    if (state.backtracking>0) {state.failed=true; return retval;}
                                EarlyExitException eee =
                                    new EarlyExitException(7, input);
                                throw eee;
                        }
                        cnt7++;
                    } while (true);


                    RP17=(Token)match(input,RP,FOLLOW_RP_in_predicates176); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RP.add(RP17);


                    // AST REWRITE
                    // elements: predicates, AND
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {

                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 69:2: -> ^( AND ( predicates )+ )
                    {
                        // parser/flatzinc/FlatzincFullExtParser.g:69:6: ^( AND ( predicates )+ )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot(
                        stream_AND.nextNode()
                        , root_1);

                        if ( !(stream_predicates.hasNext()) ) {
                            throw new RewriteEarlyExitException();
                        }
                        while ( stream_predicates.hasNext() ) {
                            adaptor.addChild(root_1, stream_predicates.nextTree());

                        }
                        stream_predicates.reset();

                        adaptor.addChild(root_0, root_1);
                        }

                    }


                    retval.tree = root_0;
                    }

                    }
                    break;
                case 3 :
                    // parser/flatzinc/FlatzincFullExtParser.g:70:4: LP predicates ( OR predicates )+ RP
                    {
                    LP18=(Token)match(input,LP,FOLLOW_LP_in_predicates192); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LP.add(LP18);


                    pushFollow(FOLLOW_predicates_in_predicates194);
                    predicates19=predicates();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_predicates.add(predicates19.getTree());

                    // parser/flatzinc/FlatzincFullExtParser.g:70:18: ( OR predicates )+
                    int cnt8=0;
                    loop8:
                    do {
                        int alt8=2;
                        switch ( input.LA(1) ) {
                        case OR:
                            {
                            alt8=1;
                            }
                            break;

                        }

                        switch (alt8) {
                    	case 1 :
                    	    // parser/flatzinc/FlatzincFullExtParser.g:70:19: OR predicates
                    	    {
                    	    OR20=(Token)match(input,OR,FOLLOW_OR_in_predicates197); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_OR.add(OR20);


                    	    pushFollow(FOLLOW_predicates_in_predicates199);
                    	    predicates21=predicates();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_predicates.add(predicates21.getTree());

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt8 >= 1 ) break loop8;
                    	    if (state.backtracking>0) {state.failed=true; return retval;}
                                EarlyExitException eee =
                                    new EarlyExitException(8, input);
                                throw eee;
                        }
                        cnt8++;
                    } while (true);


                    RP22=(Token)match(input,RP,FOLLOW_RP_in_predicates203); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RP.add(RP22);


                    // AST REWRITE
                    // elements: OR, predicates
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {

                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 71:2: -> ^( OR ( predicates )+ )
                    {
                        // parser/flatzinc/FlatzincFullExtParser.g:71:6: ^( OR ( predicates )+ )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot(
                        stream_OR.nextNode()
                        , root_1);

                        if ( !(stream_predicates.hasNext()) ) {
                            throw new RewriteEarlyExitException();
                        }
                        while ( stream_predicates.hasNext() ) {
                            adaptor.addChild(root_1, stream_predicates.nextTree());

                        }
                        stream_predicates.reset();

                        adaptor.addChild(root_0, root_1);
                        }

                    }


                    retval.tree = root_0;
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);


            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "predicates"


    public static class predicate_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "predicate"
    // parser/flatzinc/FlatzincFullExtParser.g:75:1: predicate : ( TRUE | attribute op INT_CONST | IN LP IDENTIFIER ( CM IDENTIFIER )* RP -> ^( IN ( IDENTIFIER )+ ) | NOT predicate );
    public final FlatzincFullExtParser.predicate_return predicate() throws RecognitionException {
        FlatzincFullExtParser.predicate_return retval = new FlatzincFullExtParser.predicate_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token TRUE23=null;
        Token INT_CONST26=null;
        Token IN27=null;
        Token LP28=null;
        Token IDENTIFIER29=null;
        Token CM30=null;
        Token IDENTIFIER31=null;
        Token RP32=null;
        Token NOT33=null;
        FlatzincFullExtParser.attribute_return attribute24 =null;

        FlatzincFullExtParser.op_return op25 =null;

        FlatzincFullExtParser.predicate_return predicate34 =null;


        Object TRUE23_tree=null;
        Object INT_CONST26_tree=null;
        Object IN27_tree=null;
        Object LP28_tree=null;
        Object IDENTIFIER29_tree=null;
        Object CM30_tree=null;
        Object IDENTIFIER31_tree=null;
        Object RP32_tree=null;
        Object NOT33_tree=null;
        RewriteRuleTokenStream stream_IN=new RewriteRuleTokenStream(adaptor,"token IN");
        RewriteRuleTokenStream stream_RP=new RewriteRuleTokenStream(adaptor,"token RP");
        RewriteRuleTokenStream stream_IDENTIFIER=new RewriteRuleTokenStream(adaptor,"token IDENTIFIER");
        RewriteRuleTokenStream stream_CM=new RewriteRuleTokenStream(adaptor,"token CM");
        RewriteRuleTokenStream stream_LP=new RewriteRuleTokenStream(adaptor,"token LP");

        try {
            // parser/flatzinc/FlatzincFullExtParser.g:76:2: ( TRUE | attribute op INT_CONST | IN LP IDENTIFIER ( CM IDENTIFIER )* RP -> ^( IN ( IDENTIFIER )+ ) | NOT predicate )
            int alt11=4;
            switch ( input.LA(1) ) {
            case TRUE:
                {
                alt11=1;
                }
                break;
            case CARITY:
            case CNAME:
            case PARITY:
            case PIDX:
            case PPRIO:
            case PPRIOD:
            case VCARD:
            case VNAME:
                {
                alt11=2;
                }
                break;
            case IN:
                {
                alt11=3;
                }
                break;
            case NOT:
                {
                alt11=4;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 11, 0, input);

                throw nvae;

            }

            switch (alt11) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtParser.g:76:4: TRUE
                    {
                    root_0 = (Object)adaptor.nil();


                    TRUE23=(Token)match(input,TRUE,FOLLOW_TRUE_in_predicate226); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    TRUE23_tree = 
                    (Object)adaptor.create(TRUE23)
                    ;
                    adaptor.addChild(root_0, TRUE23_tree);
                    }

                    }
                    break;
                case 2 :
                    // parser/flatzinc/FlatzincFullExtParser.g:77:4: attribute op INT_CONST
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_attribute_in_predicate231);
                    attribute24=attribute();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, attribute24.getTree());

                    pushFollow(FOLLOW_op_in_predicate233);
                    op25=op();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, op25.getTree());

                    INT_CONST26=(Token)match(input,INT_CONST,FOLLOW_INT_CONST_in_predicate235); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    INT_CONST26_tree = 
                    (Object)adaptor.create(INT_CONST26)
                    ;
                    adaptor.addChild(root_0, INT_CONST26_tree);
                    }

                    }
                    break;
                case 3 :
                    // parser/flatzinc/FlatzincFullExtParser.g:78:4: IN LP IDENTIFIER ( CM IDENTIFIER )* RP
                    {
                    IN27=(Token)match(input,IN,FOLLOW_IN_in_predicate240); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_IN.add(IN27);


                    LP28=(Token)match(input,LP,FOLLOW_LP_in_predicate242); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LP.add(LP28);


                    IDENTIFIER29=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_predicate244); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_IDENTIFIER.add(IDENTIFIER29);


                    // parser/flatzinc/FlatzincFullExtParser.g:78:21: ( CM IDENTIFIER )*
                    loop10:
                    do {
                        int alt10=2;
                        switch ( input.LA(1) ) {
                        case CM:
                            {
                            alt10=1;
                            }
                            break;

                        }

                        switch (alt10) {
                    	case 1 :
                    	    // parser/flatzinc/FlatzincFullExtParser.g:78:22: CM IDENTIFIER
                    	    {
                    	    CM30=(Token)match(input,CM,FOLLOW_CM_in_predicate247); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_CM.add(CM30);


                    	    IDENTIFIER31=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_predicate249); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_IDENTIFIER.add(IDENTIFIER31);


                    	    }
                    	    break;

                    	default :
                    	    break loop10;
                        }
                    } while (true);


                    RP32=(Token)match(input,RP,FOLLOW_RP_in_predicate253); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RP.add(RP32);


                    // AST REWRITE
                    // elements: IN, IDENTIFIER
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {

                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 79:2: -> ^( IN ( IDENTIFIER )+ )
                    {
                        // parser/flatzinc/FlatzincFullExtParser.g:79:6: ^( IN ( IDENTIFIER )+ )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot(
                        stream_IN.nextNode()
                        , root_1);

                        if ( !(stream_IDENTIFIER.hasNext()) ) {
                            throw new RewriteEarlyExitException();
                        }
                        while ( stream_IDENTIFIER.hasNext() ) {
                            adaptor.addChild(root_1, 
                            stream_IDENTIFIER.nextNode()
                            );

                        }
                        stream_IDENTIFIER.reset();

                        adaptor.addChild(root_0, root_1);
                        }

                    }


                    retval.tree = root_0;
                    }

                    }
                    break;
                case 4 :
                    // parser/flatzinc/FlatzincFullExtParser.g:80:4: NOT predicate
                    {
                    root_0 = (Object)adaptor.nil();


                    NOT33=(Token)match(input,NOT,FOLLOW_NOT_in_predicate269); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    NOT33_tree = 
                    (Object)adaptor.create(NOT33)
                    ;
                    adaptor.addChild(root_0, NOT33_tree);
                    }

                    pushFollow(FOLLOW_predicate_in_predicate271);
                    predicate34=predicate();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, predicate34.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);


            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "predicate"


    public static class attribute_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "attribute"
    // parser/flatzinc/FlatzincFullExtParser.g:84:1: attribute : ( VNAME | VCARD | CNAME | CARITY | PIDX | PPRIO | PARITY | PPRIOD );
    public final FlatzincFullExtParser.attribute_return attribute() throws RecognitionException {
        FlatzincFullExtParser.attribute_return retval = new FlatzincFullExtParser.attribute_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token set35=null;

        Object set35_tree=null;

        try {
            // parser/flatzinc/FlatzincFullExtParser.g:85:2: ( VNAME | VCARD | CNAME | CARITY | PIDX | PPRIO | PARITY | PPRIOD )
            // parser/flatzinc/FlatzincFullExtParser.g:
            {
            root_0 = (Object)adaptor.nil();


            set35=(Token)input.LT(1);

            if ( input.LA(1)==CARITY||input.LA(1)==CNAME||(input.LA(1) >= PARITY && input.LA(1) <= PIDX)||(input.LA(1) >= PPRIO && input.LA(1) <= PPRIOD)||(input.LA(1) >= VCARD && input.LA(1) <= VNAME) ) {
                input.consume();
                if ( state.backtracking==0 ) adaptor.addChild(root_0, 
                (Object)adaptor.create(set35)
                );
                state.errorRecovery=false;
                state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

            retval.stop = input.LT(-1);


            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "attribute"


    public static class op_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "op"
    // parser/flatzinc/FlatzincFullExtParser.g:97:1: op : ( OEQ | ONQ | OLT | OGT | OLQ | OGQ );
    public final FlatzincFullExtParser.op_return op() throws RecognitionException {
        FlatzincFullExtParser.op_return retval = new FlatzincFullExtParser.op_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token set36=null;

        Object set36_tree=null;

        try {
            // parser/flatzinc/FlatzincFullExtParser.g:98:5: ( OEQ | ONQ | OLT | OGT | OLQ | OGQ )
            // parser/flatzinc/FlatzincFullExtParser.g:
            {
            root_0 = (Object)adaptor.nil();


            set36=(Token)input.LT(1);

            if ( input.LA(1)==OEQ||(input.LA(1) >= OGQ && input.LA(1) <= OLT)||input.LA(1)==ONQ ) {
                input.consume();
                if ( state.backtracking==0 ) adaptor.addChild(root_0, 
                (Object)adaptor.create(set36)
                );
                state.errorRecovery=false;
                state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

            retval.stop = input.LT(-1);


            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "op"


    public static class structure_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "structure"
    // parser/flatzinc/FlatzincFullExtParser.g:111:1: structure : ( struct SC !| struct_reg SC !);
    public final FlatzincFullExtParser.structure_return structure() throws RecognitionException {
        FlatzincFullExtParser.structure_return retval = new FlatzincFullExtParser.structure_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token SC38=null;
        Token SC40=null;
        FlatzincFullExtParser.struct_return struct37 =null;

        FlatzincFullExtParser.struct_reg_return struct_reg39 =null;


        Object SC38_tree=null;
        Object SC40_tree=null;

        try {
            // parser/flatzinc/FlatzincFullExtParser.g:112:2: ( struct SC !| struct_reg SC !)
            int alt12=2;
            switch ( input.LA(1) ) {
            case LIST:
            case MAX:
            case MIN:
            case QUEUE:
            case REV:
                {
                alt12=1;
                }
                break;
            case IDENTIFIER:
                {
                alt12=2;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 12, 0, input);

                throw nvae;

            }

            switch (alt12) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtParser.g:112:6: struct SC !
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_struct_in_structure440);
                    struct37=struct();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, struct37.getTree());

                    SC38=(Token)match(input,SC,FOLLOW_SC_in_structure442); if (state.failed) return retval;

                    }
                    break;
                case 2 :
                    // parser/flatzinc/FlatzincFullExtParser.g:113:6: struct_reg SC !
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_struct_reg_in_structure450);
                    struct_reg39=struct_reg();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, struct_reg39.getTree());

                    SC40=(Token)match(input,SC,FOLLOW_SC_in_structure452); if (state.failed) return retval;

                    }
                    break;

            }
            retval.stop = input.LT(-1);


            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "structure"


    public static class struct_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "struct"
    // parser/flatzinc/FlatzincFullExtParser.g:116:1: struct : coll OF LB elt ( CM elt )* RB ( KEY comb_attr )? -> ^( STRUC ( elt )+ ( comb_attr )? coll ) ;
    public final FlatzincFullExtParser.struct_return struct() throws RecognitionException {
        FlatzincFullExtParser.struct_return retval = new FlatzincFullExtParser.struct_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token OF42=null;
        Token LB43=null;
        Token CM45=null;
        Token RB47=null;
        Token KEY48=null;
        FlatzincFullExtParser.coll_return coll41 =null;

        FlatzincFullExtParser.elt_return elt44 =null;

        FlatzincFullExtParser.elt_return elt46 =null;

        FlatzincFullExtParser.comb_attr_return comb_attr49 =null;


        Object OF42_tree=null;
        Object LB43_tree=null;
        Object CM45_tree=null;
        Object RB47_tree=null;
        Object KEY48_tree=null;
        RewriteRuleTokenStream stream_LB=new RewriteRuleTokenStream(adaptor,"token LB");
        RewriteRuleTokenStream stream_RB=new RewriteRuleTokenStream(adaptor,"token RB");
        RewriteRuleTokenStream stream_OF=new RewriteRuleTokenStream(adaptor,"token OF");
        RewriteRuleTokenStream stream_KEY=new RewriteRuleTokenStream(adaptor,"token KEY");
        RewriteRuleTokenStream stream_CM=new RewriteRuleTokenStream(adaptor,"token CM");
        RewriteRuleSubtreeStream stream_comb_attr=new RewriteRuleSubtreeStream(adaptor,"rule comb_attr");
        RewriteRuleSubtreeStream stream_coll=new RewriteRuleSubtreeStream(adaptor,"rule coll");
        RewriteRuleSubtreeStream stream_elt=new RewriteRuleSubtreeStream(adaptor,"rule elt");
        try {
            // parser/flatzinc/FlatzincFullExtParser.g:117:5: ( coll OF LB elt ( CM elt )* RB ( KEY comb_attr )? -> ^( STRUC ( elt )+ ( comb_attr )? coll ) )
            // parser/flatzinc/FlatzincFullExtParser.g:117:7: coll OF LB elt ( CM elt )* RB ( KEY comb_attr )?
            {
            pushFollow(FOLLOW_coll_in_struct467);
            coll41=coll();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_coll.add(coll41.getTree());

            OF42=(Token)match(input,OF,FOLLOW_OF_in_struct469); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_OF.add(OF42);


            LB43=(Token)match(input,LB,FOLLOW_LB_in_struct471); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LB.add(LB43);


            pushFollow(FOLLOW_elt_in_struct473);
            elt44=elt();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_elt.add(elt44.getTree());

            // parser/flatzinc/FlatzincFullExtParser.g:117:22: ( CM elt )*
            loop13:
            do {
                int alt13=2;
                switch ( input.LA(1) ) {
                case CM:
                    {
                    alt13=1;
                    }
                    break;

                }

                switch (alt13) {
            	case 1 :
            	    // parser/flatzinc/FlatzincFullExtParser.g:117:23: CM elt
            	    {
            	    CM45=(Token)match(input,CM,FOLLOW_CM_in_struct476); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_CM.add(CM45);


            	    pushFollow(FOLLOW_elt_in_struct478);
            	    elt46=elt();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_elt.add(elt46.getTree());

            	    }
            	    break;

            	default :
            	    break loop13;
                }
            } while (true);


            RB47=(Token)match(input,RB,FOLLOW_RB_in_struct482); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RB.add(RB47);


            // parser/flatzinc/FlatzincFullExtParser.g:117:35: ( KEY comb_attr )?
            int alt14=2;
            switch ( input.LA(1) ) {
                case KEY:
                    {
                    alt14=1;
                    }
                    break;
            }

            switch (alt14) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtParser.g:117:36: KEY comb_attr
                    {
                    KEY48=(Token)match(input,KEY,FOLLOW_KEY_in_struct485); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_KEY.add(KEY48);


                    pushFollow(FOLLOW_comb_attr_in_struct487);
                    comb_attr49=comb_attr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_comb_attr.add(comb_attr49.getTree());

                    }
                    break;

            }


            // AST REWRITE
            // elements: coll, elt, comb_attr
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {

            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 118:5: -> ^( STRUC ( elt )+ ( comb_attr )? coll )
            {
                // parser/flatzinc/FlatzincFullExtParser.g:118:9: ^( STRUC ( elt )+ ( comb_attr )? coll )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot(
                (Object)adaptor.create(STRUC, "STRUC")
                , root_1);

                if ( !(stream_elt.hasNext()) ) {
                    throw new RewriteEarlyExitException();
                }
                while ( stream_elt.hasNext() ) {
                    adaptor.addChild(root_1, stream_elt.nextTree());

                }
                stream_elt.reset();

                // parser/flatzinc/FlatzincFullExtParser.g:118:22: ( comb_attr )?
                if ( stream_comb_attr.hasNext() ) {
                    adaptor.addChild(root_1, stream_comb_attr.nextTree());

                }
                stream_comb_attr.reset();

                adaptor.addChild(root_1, stream_coll.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }


            retval.tree = root_0;
            }

            }

            retval.stop = input.LT(-1);


            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "struct"


    public static class struct_reg_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "struct_reg"
    // parser/flatzinc/FlatzincFullExtParser.g:121:1: struct_reg : IDENTIFIER AS coll OF LB many RB ( KEY comb_attr )? -> ^( STREG IDENTIFIER many ( comb_attr )? coll ) ;
    public final FlatzincFullExtParser.struct_reg_return struct_reg() throws RecognitionException {
        FlatzincFullExtParser.struct_reg_return retval = new FlatzincFullExtParser.struct_reg_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token IDENTIFIER50=null;
        Token AS51=null;
        Token OF53=null;
        Token LB54=null;
        Token RB56=null;
        Token KEY57=null;
        FlatzincFullExtParser.coll_return coll52 =null;

        FlatzincFullExtParser.many_return many55 =null;

        FlatzincFullExtParser.comb_attr_return comb_attr58 =null;


        Object IDENTIFIER50_tree=null;
        Object AS51_tree=null;
        Object OF53_tree=null;
        Object LB54_tree=null;
        Object RB56_tree=null;
        Object KEY57_tree=null;
        RewriteRuleTokenStream stream_AS=new RewriteRuleTokenStream(adaptor,"token AS");
        RewriteRuleTokenStream stream_LB=new RewriteRuleTokenStream(adaptor,"token LB");
        RewriteRuleTokenStream stream_RB=new RewriteRuleTokenStream(adaptor,"token RB");
        RewriteRuleTokenStream stream_OF=new RewriteRuleTokenStream(adaptor,"token OF");
        RewriteRuleTokenStream stream_IDENTIFIER=new RewriteRuleTokenStream(adaptor,"token IDENTIFIER");
        RewriteRuleTokenStream stream_KEY=new RewriteRuleTokenStream(adaptor,"token KEY");
        RewriteRuleSubtreeStream stream_many=new RewriteRuleSubtreeStream(adaptor,"rule many");
        RewriteRuleSubtreeStream stream_comb_attr=new RewriteRuleSubtreeStream(adaptor,"rule comb_attr");
        RewriteRuleSubtreeStream stream_coll=new RewriteRuleSubtreeStream(adaptor,"rule coll");
        try {
            // parser/flatzinc/FlatzincFullExtParser.g:122:2: ( IDENTIFIER AS coll OF LB many RB ( KEY comb_attr )? -> ^( STREG IDENTIFIER many ( comb_attr )? coll ) )
            // parser/flatzinc/FlatzincFullExtParser.g:122:4: IDENTIFIER AS coll OF LB many RB ( KEY comb_attr )?
            {
            IDENTIFIER50=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_struct_reg519); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_IDENTIFIER.add(IDENTIFIER50);


            AS51=(Token)match(input,AS,FOLLOW_AS_in_struct_reg521); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_AS.add(AS51);


            pushFollow(FOLLOW_coll_in_struct_reg523);
            coll52=coll();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_coll.add(coll52.getTree());

            OF53=(Token)match(input,OF,FOLLOW_OF_in_struct_reg525); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_OF.add(OF53);


            LB54=(Token)match(input,LB,FOLLOW_LB_in_struct_reg527); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LB.add(LB54);


            pushFollow(FOLLOW_many_in_struct_reg529);
            many55=many();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_many.add(many55.getTree());

            RB56=(Token)match(input,RB,FOLLOW_RB_in_struct_reg531); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RB.add(RB56);


            // parser/flatzinc/FlatzincFullExtParser.g:122:37: ( KEY comb_attr )?
            int alt15=2;
            switch ( input.LA(1) ) {
                case KEY:
                    {
                    alt15=1;
                    }
                    break;
            }

            switch (alt15) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtParser.g:122:38: KEY comb_attr
                    {
                    KEY57=(Token)match(input,KEY,FOLLOW_KEY_in_struct_reg534); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_KEY.add(KEY57);


                    pushFollow(FOLLOW_comb_attr_in_struct_reg536);
                    comb_attr58=comb_attr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_comb_attr.add(comb_attr58.getTree());

                    }
                    break;

            }


            // AST REWRITE
            // elements: IDENTIFIER, many, coll, comb_attr
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {

            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 123:2: -> ^( STREG IDENTIFIER many ( comb_attr )? coll )
            {
                // parser/flatzinc/FlatzincFullExtParser.g:123:6: ^( STREG IDENTIFIER many ( comb_attr )? coll )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot(
                (Object)adaptor.create(STREG, "STREG")
                , root_1);

                adaptor.addChild(root_1, 
                stream_IDENTIFIER.nextNode()
                );

                adaptor.addChild(root_1, stream_many.nextTree());

                // parser/flatzinc/FlatzincFullExtParser.g:123:30: ( comb_attr )?
                if ( stream_comb_attr.hasNext() ) {
                    adaptor.addChild(root_1, stream_comb_attr.nextTree());

                }
                stream_comb_attr.reset();

                adaptor.addChild(root_1, stream_coll.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }


            retval.tree = root_0;
            }

            }

            retval.stop = input.LT(-1);


            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "struct_reg"


    public static class elt_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "elt"
    // parser/flatzinc/FlatzincFullExtParser.g:127:1: elt : ( struct_reg | struct | IDENTIFIER ( KEY attribute )? );
    public final FlatzincFullExtParser.elt_return elt() throws RecognitionException {
        FlatzincFullExtParser.elt_return retval = new FlatzincFullExtParser.elt_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token IDENTIFIER61=null;
        Token KEY62=null;
        FlatzincFullExtParser.struct_reg_return struct_reg59 =null;

        FlatzincFullExtParser.struct_return struct60 =null;

        FlatzincFullExtParser.attribute_return attribute63 =null;


        Object IDENTIFIER61_tree=null;
        Object KEY62_tree=null;

        try {
            // parser/flatzinc/FlatzincFullExtParser.g:128:5: ( struct_reg | struct | IDENTIFIER ( KEY attribute )? )
            int alt17=3;
            switch ( input.LA(1) ) {
            case IDENTIFIER:
                {
                switch ( input.LA(2) ) {
                case AS:
                    {
                    alt17=1;
                    }
                    break;
                case EOF:
                case CM:
                case KEY:
                case RB:
                    {
                    alt17=3;
                    }
                    break;
                default:
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 17, 1, input);

                    throw nvae;

                }

                }
                break;
            case LIST:
            case MAX:
            case MIN:
            case QUEUE:
            case REV:
                {
                alt17=2;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 17, 0, input);

                throw nvae;

            }

            switch (alt17) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtParser.g:128:7: struct_reg
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_struct_reg_in_elt570);
                    struct_reg59=struct_reg();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, struct_reg59.getTree());

                    }
                    break;
                case 2 :
                    // parser/flatzinc/FlatzincFullExtParser.g:129:9: struct
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_struct_in_elt580);
                    struct60=struct();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, struct60.getTree());

                    }
                    break;
                case 3 :
                    // parser/flatzinc/FlatzincFullExtParser.g:130:9: IDENTIFIER ( KEY attribute )?
                    {
                    root_0 = (Object)adaptor.nil();


                    IDENTIFIER61=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_elt590); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    IDENTIFIER61_tree = 
                    (Object)adaptor.create(IDENTIFIER61)
                    ;
                    adaptor.addChild(root_0, IDENTIFIER61_tree);
                    }

                    // parser/flatzinc/FlatzincFullExtParser.g:130:20: ( KEY attribute )?
                    int alt16=2;
                    switch ( input.LA(1) ) {
                        case KEY:
                            {
                            alt16=1;
                            }
                            break;
                    }

                    switch (alt16) {
                        case 1 :
                            // parser/flatzinc/FlatzincFullExtParser.g:130:21: KEY attribute
                            {
                            KEY62=(Token)match(input,KEY,FOLLOW_KEY_in_elt593); if (state.failed) return retval;
                            if ( state.backtracking==0 ) {
                            KEY62_tree = 
                            (Object)adaptor.create(KEY62)
                            ;
                            adaptor.addChild(root_0, KEY62_tree);
                            }

                            pushFollow(FOLLOW_attribute_in_elt595);
                            attribute63=attribute();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) adaptor.addChild(root_0, attribute63.getTree());

                            }
                            break;

                    }


                    }
                    break;

            }
            retval.stop = input.LT(-1);


            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "elt"


    public static class many_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "many"
    // parser/flatzinc/FlatzincFullExtParser.g:133:1: many : EACH attribute AS coll ( OF LB m= many RB )? ( KEY comb_attr )? -> {m==null}? ^( attribute ( comb_attr )? coll ) -> ^( EACH attribute ( comb_attr )? many coll ) ;
    public final FlatzincFullExtParser.many_return many() throws RecognitionException {
        FlatzincFullExtParser.many_return retval = new FlatzincFullExtParser.many_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token EACH64=null;
        Token AS66=null;
        Token OF68=null;
        Token LB69=null;
        Token RB70=null;
        Token KEY71=null;
        FlatzincFullExtParser.many_return m =null;

        FlatzincFullExtParser.attribute_return attribute65 =null;

        FlatzincFullExtParser.coll_return coll67 =null;

        FlatzincFullExtParser.comb_attr_return comb_attr72 =null;


        Object EACH64_tree=null;
        Object AS66_tree=null;
        Object OF68_tree=null;
        Object LB69_tree=null;
        Object RB70_tree=null;
        Object KEY71_tree=null;
        RewriteRuleTokenStream stream_AS=new RewriteRuleTokenStream(adaptor,"token AS");
        RewriteRuleTokenStream stream_LB=new RewriteRuleTokenStream(adaptor,"token LB");
        RewriteRuleTokenStream stream_EACH=new RewriteRuleTokenStream(adaptor,"token EACH");
        RewriteRuleTokenStream stream_RB=new RewriteRuleTokenStream(adaptor,"token RB");
        RewriteRuleTokenStream stream_OF=new RewriteRuleTokenStream(adaptor,"token OF");
        RewriteRuleTokenStream stream_KEY=new RewriteRuleTokenStream(adaptor,"token KEY");
        RewriteRuleSubtreeStream stream_many=new RewriteRuleSubtreeStream(adaptor,"rule many");
        RewriteRuleSubtreeStream stream_comb_attr=new RewriteRuleSubtreeStream(adaptor,"rule comb_attr");
        RewriteRuleSubtreeStream stream_attribute=new RewriteRuleSubtreeStream(adaptor,"rule attribute");
        RewriteRuleSubtreeStream stream_coll=new RewriteRuleSubtreeStream(adaptor,"rule coll");
        try {
            // parser/flatzinc/FlatzincFullExtParser.g:134:5: ( EACH attribute AS coll ( OF LB m= many RB )? ( KEY comb_attr )? -> {m==null}? ^( attribute ( comb_attr )? coll ) -> ^( EACH attribute ( comb_attr )? many coll ) )
            // parser/flatzinc/FlatzincFullExtParser.g:134:7: EACH attribute AS coll ( OF LB m= many RB )? ( KEY comb_attr )?
            {
            EACH64=(Token)match(input,EACH,FOLLOW_EACH_in_many611); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_EACH.add(EACH64);


            pushFollow(FOLLOW_attribute_in_many613);
            attribute65=attribute();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_attribute.add(attribute65.getTree());

            AS66=(Token)match(input,AS,FOLLOW_AS_in_many615); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_AS.add(AS66);


            pushFollow(FOLLOW_coll_in_many617);
            coll67=coll();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_coll.add(coll67.getTree());

            // parser/flatzinc/FlatzincFullExtParser.g:134:30: ( OF LB m= many RB )?
            int alt18=2;
            switch ( input.LA(1) ) {
                case OF:
                    {
                    alt18=1;
                    }
                    break;
            }

            switch (alt18) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtParser.g:134:31: OF LB m= many RB
                    {
                    OF68=(Token)match(input,OF,FOLLOW_OF_in_many620); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_OF.add(OF68);


                    LB69=(Token)match(input,LB,FOLLOW_LB_in_many622); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LB.add(LB69);


                    pushFollow(FOLLOW_many_in_many626);
                    m=many();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_many.add(m.getTree());

                    RB70=(Token)match(input,RB,FOLLOW_RB_in_many628); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RB.add(RB70);


                    }
                    break;

            }


            // parser/flatzinc/FlatzincFullExtParser.g:134:49: ( KEY comb_attr )?
            int alt19=2;
            switch ( input.LA(1) ) {
                case KEY:
                    {
                    alt19=1;
                    }
                    break;
            }

            switch (alt19) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtParser.g:134:50: KEY comb_attr
                    {
                    KEY71=(Token)match(input,KEY,FOLLOW_KEY_in_many633); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_KEY.add(KEY71);


                    pushFollow(FOLLOW_comb_attr_in_many635);
                    comb_attr72=comb_attr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_comb_attr.add(comb_attr72.getTree());

                    }
                    break;

            }


            // AST REWRITE
            // elements: coll, comb_attr, attribute, many, EACH, attribute, coll, comb_attr
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {

            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 135:5: -> {m==null}? ^( attribute ( comb_attr )? coll )
            if (m==null) {
                // parser/flatzinc/FlatzincFullExtParser.g:135:21: ^( attribute ( comb_attr )? coll )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot(stream_attribute.nextNode(), root_1);

                // parser/flatzinc/FlatzincFullExtParser.g:135:33: ( comb_attr )?
                if ( stream_comb_attr.hasNext() ) {
                    adaptor.addChild(root_1, stream_comb_attr.nextTree());

                }
                stream_comb_attr.reset();

                adaptor.addChild(root_1, stream_coll.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            else // 136:5: -> ^( EACH attribute ( comb_attr )? many coll )
            {
                // parser/flatzinc/FlatzincFullExtParser.g:136:21: ^( EACH attribute ( comb_attr )? many coll )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot(
                stream_EACH.nextNode()
                , root_1);

                adaptor.addChild(root_1, stream_attribute.nextTree());

                // parser/flatzinc/FlatzincFullExtParser.g:136:38: ( comb_attr )?
                if ( stream_comb_attr.hasNext() ) {
                    adaptor.addChild(root_1, stream_comb_attr.nextTree());

                }
                stream_comb_attr.reset();

                adaptor.addChild(root_1, stream_many.nextTree());

                adaptor.addChild(root_1, stream_coll.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }


            retval.tree = root_0;
            }

            }

            retval.stop = input.LT(-1);


            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "many"


    public static class coll_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "coll"
    // parser/flatzinc/FlatzincFullExtParser.g:142:1: coll : ( QUEUE LP ! qiter RP !| ( REV )? LIST LP ! liter RP !| ( MIN | MAX ) HEAP LP ! qiter RP !);
    public final FlatzincFullExtParser.coll_return coll() throws RecognitionException {
        FlatzincFullExtParser.coll_return retval = new FlatzincFullExtParser.coll_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token QUEUE73=null;
        Token LP74=null;
        Token RP76=null;
        Token REV77=null;
        Token LIST78=null;
        Token LP79=null;
        Token RP81=null;
        Token set82=null;
        Token HEAP83=null;
        Token LP84=null;
        Token RP86=null;
        FlatzincFullExtParser.qiter_return qiter75 =null;

        FlatzincFullExtParser.liter_return liter80 =null;

        FlatzincFullExtParser.qiter_return qiter85 =null;


        Object QUEUE73_tree=null;
        Object LP74_tree=null;
        Object RP76_tree=null;
        Object REV77_tree=null;
        Object LIST78_tree=null;
        Object LP79_tree=null;
        Object RP81_tree=null;
        Object set82_tree=null;
        Object HEAP83_tree=null;
        Object LP84_tree=null;
        Object RP86_tree=null;

        try {
            // parser/flatzinc/FlatzincFullExtParser.g:143:5: ( QUEUE LP ! qiter RP !| ( REV )? LIST LP ! liter RP !| ( MIN | MAX ) HEAP LP ! qiter RP !)
            int alt21=3;
            switch ( input.LA(1) ) {
            case QUEUE:
                {
                alt21=1;
                }
                break;
            case LIST:
            case REV:
                {
                alt21=2;
                }
                break;
            case MAX:
            case MIN:
                {
                alt21=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 21, 0, input);

                throw nvae;

            }

            switch (alt21) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtParser.g:143:7: QUEUE LP ! qiter RP !
                    {
                    root_0 = (Object)adaptor.nil();


                    QUEUE73=(Token)match(input,QUEUE,FOLLOW_QUEUE_in_coll705); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    QUEUE73_tree = 
                    (Object)adaptor.create(QUEUE73)
                    ;
                    adaptor.addChild(root_0, QUEUE73_tree);
                    }

                    LP74=(Token)match(input,LP,FOLLOW_LP_in_coll707); if (state.failed) return retval;

                    pushFollow(FOLLOW_qiter_in_coll710);
                    qiter75=qiter();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, qiter75.getTree());

                    RP76=(Token)match(input,RP,FOLLOW_RP_in_coll712); if (state.failed) return retval;

                    }
                    break;
                case 2 :
                    // parser/flatzinc/FlatzincFullExtParser.g:144:7: ( REV )? LIST LP ! liter RP !
                    {
                    root_0 = (Object)adaptor.nil();


                    // parser/flatzinc/FlatzincFullExtParser.g:144:7: ( REV )?
                    int alt20=2;
                    switch ( input.LA(1) ) {
                        case REV:
                            {
                            alt20=1;
                            }
                            break;
                    }

                    switch (alt20) {
                        case 1 :
                            // parser/flatzinc/FlatzincFullExtParser.g:144:8: REV
                            {
                            REV77=(Token)match(input,REV,FOLLOW_REV_in_coll722); if (state.failed) return retval;
                            if ( state.backtracking==0 ) {
                            REV77_tree = 
                            (Object)adaptor.create(REV77)
                            ;
                            adaptor.addChild(root_0, REV77_tree);
                            }

                            }
                            break;

                    }


                    LIST78=(Token)match(input,LIST,FOLLOW_LIST_in_coll726); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LIST78_tree = 
                    (Object)adaptor.create(LIST78)
                    ;
                    adaptor.addChild(root_0, LIST78_tree);
                    }

                    LP79=(Token)match(input,LP,FOLLOW_LP_in_coll728); if (state.failed) return retval;

                    pushFollow(FOLLOW_liter_in_coll731);
                    liter80=liter();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, liter80.getTree());

                    RP81=(Token)match(input,RP,FOLLOW_RP_in_coll733); if (state.failed) return retval;

                    }
                    break;
                case 3 :
                    // parser/flatzinc/FlatzincFullExtParser.g:145:7: ( MIN | MAX ) HEAP LP ! qiter RP !
                    {
                    root_0 = (Object)adaptor.nil();


                    set82=(Token)input.LT(1);

                    if ( input.LA(1)==MAX||input.LA(1)==MIN ) {
                        input.consume();
                        if ( state.backtracking==0 ) adaptor.addChild(root_0, 
                        (Object)adaptor.create(set82)
                        );
                        state.errorRecovery=false;
                        state.failed=false;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        throw mse;
                    }


                    HEAP83=(Token)match(input,HEAP,FOLLOW_HEAP_in_coll748); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    HEAP83_tree = 
                    (Object)adaptor.create(HEAP83)
                    ;
                    adaptor.addChild(root_0, HEAP83_tree);
                    }

                    LP84=(Token)match(input,LP,FOLLOW_LP_in_coll751); if (state.failed) return retval;

                    pushFollow(FOLLOW_qiter_in_coll754);
                    qiter85=qiter();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, qiter85.getTree());

                    RP86=(Token)match(input,RP,FOLLOW_RP_in_coll756); if (state.failed) return retval;

                    }
                    break;

            }
            retval.stop = input.LT(-1);


            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "coll"


    public static class qiter_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "qiter"
    // parser/flatzinc/FlatzincFullExtParser.g:148:1: qiter : ( ONE | WONE );
    public final FlatzincFullExtParser.qiter_return qiter() throws RecognitionException {
        FlatzincFullExtParser.qiter_return retval = new FlatzincFullExtParser.qiter_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token set87=null;

        Object set87_tree=null;

        try {
            // parser/flatzinc/FlatzincFullExtParser.g:149:5: ( ONE | WONE )
            // parser/flatzinc/FlatzincFullExtParser.g:
            {
            root_0 = (Object)adaptor.nil();


            set87=(Token)input.LT(1);

            if ( input.LA(1)==ONE||input.LA(1)==WONE ) {
                input.consume();
                if ( state.backtracking==0 ) adaptor.addChild(root_0, 
                (Object)adaptor.create(set87)
                );
                state.errorRecovery=false;
                state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

            retval.stop = input.LT(-1);


            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "qiter"


    public static class liter_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "liter"
    // parser/flatzinc/FlatzincFullExtParser.g:153:1: liter : ( qiter | FOR | WFOR );
    public final FlatzincFullExtParser.liter_return liter() throws RecognitionException {
        FlatzincFullExtParser.liter_return retval = new FlatzincFullExtParser.liter_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token FOR89=null;
        Token WFOR90=null;
        FlatzincFullExtParser.qiter_return qiter88 =null;


        Object FOR89_tree=null;
        Object WFOR90_tree=null;

        try {
            // parser/flatzinc/FlatzincFullExtParser.g:154:5: ( qiter | FOR | WFOR )
            int alt22=3;
            switch ( input.LA(1) ) {
            case ONE:
            case WONE:
                {
                alt22=1;
                }
                break;
            case FOR:
                {
                alt22=2;
                }
                break;
            case WFOR:
                {
                alt22=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 22, 0, input);

                throw nvae;

            }

            switch (alt22) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtParser.g:154:7: qiter
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_qiter_in_liter798);
                    qiter88=qiter();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, qiter88.getTree());

                    }
                    break;
                case 2 :
                    // parser/flatzinc/FlatzincFullExtParser.g:155:9: FOR
                    {
                    root_0 = (Object)adaptor.nil();


                    FOR89=(Token)match(input,FOR,FOLLOW_FOR_in_liter808); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    FOR89_tree = 
                    (Object)adaptor.create(FOR89)
                    ;
                    adaptor.addChild(root_0, FOR89_tree);
                    }

                    }
                    break;
                case 3 :
                    // parser/flatzinc/FlatzincFullExtParser.g:156:9: WFOR
                    {
                    root_0 = (Object)adaptor.nil();


                    WFOR90=(Token)match(input,WFOR,FOLLOW_WFOR_in_liter818); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    WFOR90_tree = 
                    (Object)adaptor.create(WFOR90)
                    ;
                    adaptor.addChild(root_0, WFOR90_tree);
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);


            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "liter"


    public static class comb_attr_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "comb_attr"
    // parser/flatzinc/FlatzincFullExtParser.g:160:1: comb_attr : ( attr_op ( DO attr_op )* ( DO attribute )? -> ^( DO ( attr_op )* ( attribute )? ) | ( attr_op DO )* attribute -> ^( DO ( attr_op )* ( attribute )? ) );
    public final FlatzincFullExtParser.comb_attr_return comb_attr() throws RecognitionException {
        FlatzincFullExtParser.comb_attr_return retval = new FlatzincFullExtParser.comb_attr_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token DO92=null;
        Token DO94=null;
        Token DO97=null;
        FlatzincFullExtParser.attr_op_return attr_op91 =null;

        FlatzincFullExtParser.attr_op_return attr_op93 =null;

        FlatzincFullExtParser.attribute_return attribute95 =null;

        FlatzincFullExtParser.attr_op_return attr_op96 =null;

        FlatzincFullExtParser.attribute_return attribute98 =null;


        Object DO92_tree=null;
        Object DO94_tree=null;
        Object DO97_tree=null;
        RewriteRuleTokenStream stream_DO=new RewriteRuleTokenStream(adaptor,"token DO");
        RewriteRuleSubtreeStream stream_attribute=new RewriteRuleSubtreeStream(adaptor,"rule attribute");
        RewriteRuleSubtreeStream stream_attr_op=new RewriteRuleSubtreeStream(adaptor,"rule attr_op");
        try {
            // parser/flatzinc/FlatzincFullExtParser.g:161:2: ( attr_op ( DO attr_op )* ( DO attribute )? -> ^( DO ( attr_op )* ( attribute )? ) | ( attr_op DO )* attribute -> ^( DO ( attr_op )* ( attribute )? ) )
            int alt26=2;
            alt26 = dfa26.predict(input);
            switch (alt26) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtParser.g:161:4: attr_op ( DO attr_op )* ( DO attribute )?
                    {
                    pushFollow(FOLLOW_attr_op_in_comb_attr833);
                    attr_op91=attr_op();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_attr_op.add(attr_op91.getTree());

                    // parser/flatzinc/FlatzincFullExtParser.g:161:12: ( DO attr_op )*
                    loop23:
                    do {
                        int alt23=2;
                        switch ( input.LA(1) ) {
                        case DO:
                            {
                            switch ( input.LA(2) ) {
                            case ANY:
                            case MAX:
                            case MIN:
                            case SIZE:
                            case SUM:
                                {
                                alt23=1;
                                }
                                break;

                            }

                            }
                            break;

                        }

                        switch (alt23) {
                    	case 1 :
                    	    // parser/flatzinc/FlatzincFullExtParser.g:161:13: DO attr_op
                    	    {
                    	    DO92=(Token)match(input,DO,FOLLOW_DO_in_comb_attr836); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_DO.add(DO92);


                    	    pushFollow(FOLLOW_attr_op_in_comb_attr838);
                    	    attr_op93=attr_op();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_attr_op.add(attr_op93.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop23;
                        }
                    } while (true);


                    // parser/flatzinc/FlatzincFullExtParser.g:161:27: ( DO attribute )?
                    int alt24=2;
                    switch ( input.LA(1) ) {
                        case DO:
                            {
                            alt24=1;
                            }
                            break;
                    }

                    switch (alt24) {
                        case 1 :
                            // parser/flatzinc/FlatzincFullExtParser.g:161:28: DO attribute
                            {
                            DO94=(Token)match(input,DO,FOLLOW_DO_in_comb_attr844); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_DO.add(DO94);


                            pushFollow(FOLLOW_attribute_in_comb_attr846);
                            attribute95=attribute();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_attribute.add(attribute95.getTree());

                            }
                            break;

                    }


                    // AST REWRITE
                    // elements: DO, attribute, attr_op
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {

                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 162:2: -> ^( DO ( attr_op )* ( attribute )? )
                    {
                        // parser/flatzinc/FlatzincFullExtParser.g:162:6: ^( DO ( attr_op )* ( attribute )? )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot(
                        stream_DO.nextNode()
                        , root_1);

                        // parser/flatzinc/FlatzincFullExtParser.g:162:11: ( attr_op )*
                        while ( stream_attr_op.hasNext() ) {
                            adaptor.addChild(root_1, stream_attr_op.nextTree());

                        }
                        stream_attr_op.reset();

                        // parser/flatzinc/FlatzincFullExtParser.g:162:20: ( attribute )?
                        if ( stream_attribute.hasNext() ) {
                            adaptor.addChild(root_1, stream_attribute.nextTree());

                        }
                        stream_attribute.reset();

                        adaptor.addChild(root_0, root_1);
                        }

                    }


                    retval.tree = root_0;
                    }

                    }
                    break;
                case 2 :
                    // parser/flatzinc/FlatzincFullExtParser.g:163:6: ( attr_op DO )* attribute
                    {
                    // parser/flatzinc/FlatzincFullExtParser.g:163:6: ( attr_op DO )*
                    loop25:
                    do {
                        int alt25=2;
                        switch ( input.LA(1) ) {
                        case ANY:
                        case MAX:
                        case MIN:
                        case SIZE:
                        case SUM:
                            {
                            alt25=1;
                            }
                            break;

                        }

                        switch (alt25) {
                    	case 1 :
                    	    // parser/flatzinc/FlatzincFullExtParser.g:163:7: attr_op DO
                    	    {
                    	    pushFollow(FOLLOW_attr_op_in_comb_attr870);
                    	    attr_op96=attr_op();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_attr_op.add(attr_op96.getTree());

                    	    DO97=(Token)match(input,DO,FOLLOW_DO_in_comb_attr872); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_DO.add(DO97);


                    	    }
                    	    break;

                    	default :
                    	    break loop25;
                        }
                    } while (true);


                    pushFollow(FOLLOW_attribute_in_comb_attr876);
                    attribute98=attribute();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_attribute.add(attribute98.getTree());

                    // AST REWRITE
                    // elements: DO, attr_op, attribute
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {

                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 164:2: -> ^( DO ( attr_op )* ( attribute )? )
                    {
                        // parser/flatzinc/FlatzincFullExtParser.g:164:6: ^( DO ( attr_op )* ( attribute )? )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot(
                        stream_DO.nextNode()
                        , root_1);

                        // parser/flatzinc/FlatzincFullExtParser.g:164:11: ( attr_op )*
                        while ( stream_attr_op.hasNext() ) {
                            adaptor.addChild(root_1, stream_attr_op.nextTree());

                        }
                        stream_attr_op.reset();

                        // parser/flatzinc/FlatzincFullExtParser.g:164:20: ( attribute )?
                        if ( stream_attribute.hasNext() ) {
                            adaptor.addChild(root_1, stream_attribute.nextTree());

                        }
                        stream_attribute.reset();

                        adaptor.addChild(root_0, root_1);
                        }

                    }


                    retval.tree = root_0;
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);


            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "comb_attr"


    public static class attr_op_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "attr_op"
    // parser/flatzinc/FlatzincFullExtParser.g:167:1: attr_op : ( ANY | MIN | MAX | SUM | SIZE );
    public final FlatzincFullExtParser.attr_op_return attr_op() throws RecognitionException {
        FlatzincFullExtParser.attr_op_return retval = new FlatzincFullExtParser.attr_op_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token set99=null;

        Object set99_tree=null;

        try {
            // parser/flatzinc/FlatzincFullExtParser.g:168:5: ( ANY | MIN | MAX | SUM | SIZE )
            // parser/flatzinc/FlatzincFullExtParser.g:
            {
            root_0 = (Object)adaptor.nil();


            set99=(Token)input.LT(1);

            if ( input.LA(1)==ANY||input.LA(1)==MAX||input.LA(1)==MIN||input.LA(1)==SIZE||input.LA(1)==SUM ) {
                input.consume();
                if ( state.backtracking==0 ) adaptor.addChild(root_0, 
                (Object)adaptor.create(set99)
                );
                state.errorRecovery=false;
                state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

            retval.stop = input.LT(-1);


            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "attr_op"


    public static class pred_decl_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "pred_decl"
    // parser/flatzinc/FlatzincFullExtParser.g:182:1: pred_decl : PREDICATE IDENTIFIER LP pred_param ( CM pred_param )* RP SC -> ^( PREDICATE IDENTIFIER ( pred_param )+ ) ;
    public final FlatzincFullExtParser.pred_decl_return pred_decl() throws RecognitionException {
        FlatzincFullExtParser.pred_decl_return retval = new FlatzincFullExtParser.pred_decl_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token PREDICATE100=null;
        Token IDENTIFIER101=null;
        Token LP102=null;
        Token CM104=null;
        Token RP106=null;
        Token SC107=null;
        FlatzincFullExtParser.pred_param_return pred_param103 =null;

        FlatzincFullExtParser.pred_param_return pred_param105 =null;


        Object PREDICATE100_tree=null;
        Object IDENTIFIER101_tree=null;
        Object LP102_tree=null;
        Object CM104_tree=null;
        Object RP106_tree=null;
        Object SC107_tree=null;
        RewriteRuleTokenStream stream_SC=new RewriteRuleTokenStream(adaptor,"token SC");
        RewriteRuleTokenStream stream_RP=new RewriteRuleTokenStream(adaptor,"token RP");
        RewriteRuleTokenStream stream_PREDICATE=new RewriteRuleTokenStream(adaptor,"token PREDICATE");
        RewriteRuleTokenStream stream_IDENTIFIER=new RewriteRuleTokenStream(adaptor,"token IDENTIFIER");
        RewriteRuleTokenStream stream_CM=new RewriteRuleTokenStream(adaptor,"token CM");
        RewriteRuleTokenStream stream_LP=new RewriteRuleTokenStream(adaptor,"token LP");
        RewriteRuleSubtreeStream stream_pred_param=new RewriteRuleSubtreeStream(adaptor,"rule pred_param");
        try {
            // parser/flatzinc/FlatzincFullExtParser.g:183:2: ( PREDICATE IDENTIFIER LP pred_param ( CM pred_param )* RP SC -> ^( PREDICATE IDENTIFIER ( pred_param )+ ) )
            // parser/flatzinc/FlatzincFullExtParser.g:183:6: PREDICATE IDENTIFIER LP pred_param ( CM pred_param )* RP SC
            {
            PREDICATE100=(Token)match(input,PREDICATE,FOLLOW_PREDICATE_in_pred_decl964); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_PREDICATE.add(PREDICATE100);


            IDENTIFIER101=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_pred_decl966); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_IDENTIFIER.add(IDENTIFIER101);


            LP102=(Token)match(input,LP,FOLLOW_LP_in_pred_decl968); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LP.add(LP102);


            pushFollow(FOLLOW_pred_param_in_pred_decl970);
            pred_param103=pred_param();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_pred_param.add(pred_param103.getTree());

            // parser/flatzinc/FlatzincFullExtParser.g:183:41: ( CM pred_param )*
            loop27:
            do {
                int alt27=2;
                switch ( input.LA(1) ) {
                case CM:
                    {
                    alt27=1;
                    }
                    break;

                }

                switch (alt27) {
            	case 1 :
            	    // parser/flatzinc/FlatzincFullExtParser.g:183:42: CM pred_param
            	    {
            	    CM104=(Token)match(input,CM,FOLLOW_CM_in_pred_decl973); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_CM.add(CM104);


            	    pushFollow(FOLLOW_pred_param_in_pred_decl975);
            	    pred_param105=pred_param();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_pred_param.add(pred_param105.getTree());

            	    }
            	    break;

            	default :
            	    break loop27;
                }
            } while (true);


            RP106=(Token)match(input,RP,FOLLOW_RP_in_pred_decl979); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RP.add(RP106);


            SC107=(Token)match(input,SC,FOLLOW_SC_in_pred_decl981); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_SC.add(SC107);


            // AST REWRITE
            // elements: pred_param, IDENTIFIER, PREDICATE
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {

            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 184:2: -> ^( PREDICATE IDENTIFIER ( pred_param )+ )
            {
                // parser/flatzinc/FlatzincFullExtParser.g:184:5: ^( PREDICATE IDENTIFIER ( pred_param )+ )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot(
                stream_PREDICATE.nextNode()
                , root_1);

                adaptor.addChild(root_1, 
                stream_IDENTIFIER.nextNode()
                );

                if ( !(stream_pred_param.hasNext()) ) {
                    throw new RewriteEarlyExitException();
                }
                while ( stream_pred_param.hasNext() ) {
                    adaptor.addChild(root_1, stream_pred_param.nextTree());

                }
                stream_pred_param.reset();

                adaptor.addChild(root_0, root_1);
                }

            }


            retval.tree = root_0;
            }

            }

            retval.stop = input.LT(-1);


            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "pred_decl"


    public static class pred_param_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "pred_param"
    // parser/flatzinc/FlatzincFullExtParser.g:187:1: pred_param : pred_param_type CL IDENTIFIER -> ^( CL pred_param_type IDENTIFIER ) ;
    public final FlatzincFullExtParser.pred_param_return pred_param() throws RecognitionException {
        FlatzincFullExtParser.pred_param_return retval = new FlatzincFullExtParser.pred_param_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token CL109=null;
        Token IDENTIFIER110=null;
        FlatzincFullExtParser.pred_param_type_return pred_param_type108 =null;


        Object CL109_tree=null;
        Object IDENTIFIER110_tree=null;
        RewriteRuleTokenStream stream_IDENTIFIER=new RewriteRuleTokenStream(adaptor,"token IDENTIFIER");
        RewriteRuleTokenStream stream_CL=new RewriteRuleTokenStream(adaptor,"token CL");
        RewriteRuleSubtreeStream stream_pred_param_type=new RewriteRuleSubtreeStream(adaptor,"rule pred_param_type");
        try {
            // parser/flatzinc/FlatzincFullExtParser.g:188:5: ( pred_param_type CL IDENTIFIER -> ^( CL pred_param_type IDENTIFIER ) )
            // parser/flatzinc/FlatzincFullExtParser.g:188:9: pred_param_type CL IDENTIFIER
            {
            pushFollow(FOLLOW_pred_param_type_in_pred_param1009);
            pred_param_type108=pred_param_type();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_pred_param_type.add(pred_param_type108.getTree());

            CL109=(Token)match(input,CL,FOLLOW_CL_in_pred_param1011); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_CL.add(CL109);


            IDENTIFIER110=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_pred_param1013); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_IDENTIFIER.add(IDENTIFIER110);


            // AST REWRITE
            // elements: IDENTIFIER, CL, pred_param_type
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {

            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 189:5: -> ^( CL pred_param_type IDENTIFIER )
            {
                // parser/flatzinc/FlatzincFullExtParser.g:189:9: ^( CL pred_param_type IDENTIFIER )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot(
                stream_CL.nextNode()
                , root_1);

                adaptor.addChild(root_1, stream_pred_param_type.nextTree());

                adaptor.addChild(root_1, 
                stream_IDENTIFIER.nextNode()
                );

                adaptor.addChild(root_0, root_1);
                }

            }


            retval.tree = root_0;
            }

            }

            retval.stop = input.LT(-1);


            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "pred_param"


    public static class pred_param_type_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "pred_param_type"
    // parser/flatzinc/FlatzincFullExtParser.g:192:1: pred_param_type : ( par_pred_param_type | var_pred_param_type );
    public final FlatzincFullExtParser.pred_param_type_return pred_param_type() throws RecognitionException {
        FlatzincFullExtParser.pred_param_type_return retval = new FlatzincFullExtParser.pred_param_type_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        FlatzincFullExtParser.par_pred_param_type_return par_pred_param_type111 =null;

        FlatzincFullExtParser.var_pred_param_type_return var_pred_param_type112 =null;



        try {
            // parser/flatzinc/FlatzincFullExtParser.g:193:5: ( par_pred_param_type | var_pred_param_type )
            int alt28=2;
            alt28 = dfa28.predict(input);
            switch (alt28) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtParser.g:193:9: par_pred_param_type
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_par_pred_param_type_in_pred_param_type1047);
                    par_pred_param_type111=par_pred_param_type();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, par_pred_param_type111.getTree());

                    }
                    break;
                case 2 :
                    // parser/flatzinc/FlatzincFullExtParser.g:194:9: var_pred_param_type
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_var_pred_param_type_in_pred_param_type1057);
                    var_pred_param_type112=var_pred_param_type();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, var_pred_param_type112.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);


            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "pred_param_type"


    public static class par_type_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "par_type"
    // parser/flatzinc/FlatzincFullExtParser.g:197:1: par_type : ( ARRAY LS index_set ( CM index_set )* RS OF par_type_u -> ^( ARRPAR ( index_set )+ par_type_u ) | par_type_u -> ^( APAR par_type_u ) );
    public final FlatzincFullExtParser.par_type_return par_type() throws RecognitionException {
        FlatzincFullExtParser.par_type_return retval = new FlatzincFullExtParser.par_type_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token ARRAY113=null;
        Token LS114=null;
        Token CM116=null;
        Token RS118=null;
        Token OF119=null;
        FlatzincFullExtParser.index_set_return index_set115 =null;

        FlatzincFullExtParser.index_set_return index_set117 =null;

        FlatzincFullExtParser.par_type_u_return par_type_u120 =null;

        FlatzincFullExtParser.par_type_u_return par_type_u121 =null;


        Object ARRAY113_tree=null;
        Object LS114_tree=null;
        Object CM116_tree=null;
        Object RS118_tree=null;
        Object OF119_tree=null;
        RewriteRuleTokenStream stream_RS=new RewriteRuleTokenStream(adaptor,"token RS");
        RewriteRuleTokenStream stream_OF=new RewriteRuleTokenStream(adaptor,"token OF");
        RewriteRuleTokenStream stream_ARRAY=new RewriteRuleTokenStream(adaptor,"token ARRAY");
        RewriteRuleTokenStream stream_LS=new RewriteRuleTokenStream(adaptor,"token LS");
        RewriteRuleTokenStream stream_CM=new RewriteRuleTokenStream(adaptor,"token CM");
        RewriteRuleSubtreeStream stream_index_set=new RewriteRuleSubtreeStream(adaptor,"rule index_set");
        RewriteRuleSubtreeStream stream_par_type_u=new RewriteRuleSubtreeStream(adaptor,"rule par_type_u");
        try {
            // parser/flatzinc/FlatzincFullExtParser.g:198:5: ( ARRAY LS index_set ( CM index_set )* RS OF par_type_u -> ^( ARRPAR ( index_set )+ par_type_u ) | par_type_u -> ^( APAR par_type_u ) )
            int alt30=2;
            switch ( input.LA(1) ) {
            case ARRAY:
                {
                alt30=1;
                }
                break;
            case BOOL:
            case FLOAT:
            case INT:
            case SET:
                {
                alt30=2;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 30, 0, input);

                throw nvae;

            }

            switch (alt30) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtParser.g:198:9: ARRAY LS index_set ( CM index_set )* RS OF par_type_u
                    {
                    ARRAY113=(Token)match(input,ARRAY,FOLLOW_ARRAY_in_par_type1076); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ARRAY.add(ARRAY113);


                    LS114=(Token)match(input,LS,FOLLOW_LS_in_par_type1078); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LS.add(LS114);


                    pushFollow(FOLLOW_index_set_in_par_type1080);
                    index_set115=index_set();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_index_set.add(index_set115.getTree());

                    // parser/flatzinc/FlatzincFullExtParser.g:198:28: ( CM index_set )*
                    loop29:
                    do {
                        int alt29=2;
                        switch ( input.LA(1) ) {
                        case CM:
                            {
                            alt29=1;
                            }
                            break;

                        }

                        switch (alt29) {
                    	case 1 :
                    	    // parser/flatzinc/FlatzincFullExtParser.g:198:29: CM index_set
                    	    {
                    	    CM116=(Token)match(input,CM,FOLLOW_CM_in_par_type1083); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_CM.add(CM116);


                    	    pushFollow(FOLLOW_index_set_in_par_type1085);
                    	    index_set117=index_set();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_index_set.add(index_set117.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop29;
                        }
                    } while (true);


                    RS118=(Token)match(input,RS,FOLLOW_RS_in_par_type1089); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RS.add(RS118);


                    OF119=(Token)match(input,OF,FOLLOW_OF_in_par_type1091); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_OF.add(OF119);


                    pushFollow(FOLLOW_par_type_u_in_par_type1093);
                    par_type_u120=par_type_u();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_par_type_u.add(par_type_u120.getTree());

                    // AST REWRITE
                    // elements: par_type_u, index_set
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {

                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 199:5: -> ^( ARRPAR ( index_set )+ par_type_u )
                    {
                        // parser/flatzinc/FlatzincFullExtParser.g:199:9: ^( ARRPAR ( index_set )+ par_type_u )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot(
                        (Object)adaptor.create(ARRPAR, "ARRPAR")
                        , root_1);

                        if ( !(stream_index_set.hasNext()) ) {
                            throw new RewriteEarlyExitException();
                        }
                        while ( stream_index_set.hasNext() ) {
                            adaptor.addChild(root_1, stream_index_set.nextTree());

                        }
                        stream_index_set.reset();

                        adaptor.addChild(root_1, stream_par_type_u.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }


                    retval.tree = root_0;
                    }

                    }
                    break;
                case 2 :
                    // parser/flatzinc/FlatzincFullExtParser.g:200:9: par_type_u
                    {
                    pushFollow(FOLLOW_par_type_u_in_par_type1119);
                    par_type_u121=par_type_u();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_par_type_u.add(par_type_u121.getTree());

                    // AST REWRITE
                    // elements: par_type_u
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {

                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 201:5: -> ^( APAR par_type_u )
                    {
                        // parser/flatzinc/FlatzincFullExtParser.g:201:9: ^( APAR par_type_u )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot(
                        (Object)adaptor.create(APAR, "APAR")
                        , root_1);

                        adaptor.addChild(root_1, stream_par_type_u.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }


                    retval.tree = root_0;
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);


            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "par_type"


    public static class par_type_u_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "par_type_u"
    // parser/flatzinc/FlatzincFullExtParser.g:204:1: par_type_u : ( BOOL | FLOAT | SET OF INT | INT );
    public final FlatzincFullExtParser.par_type_u_return par_type_u() throws RecognitionException {
        FlatzincFullExtParser.par_type_u_return retval = new FlatzincFullExtParser.par_type_u_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token BOOL122=null;
        Token FLOAT123=null;
        Token SET124=null;
        Token OF125=null;
        Token INT126=null;
        Token INT127=null;

        Object BOOL122_tree=null;
        Object FLOAT123_tree=null;
        Object SET124_tree=null;
        Object OF125_tree=null;
        Object INT126_tree=null;
        Object INT127_tree=null;

        try {
            // parser/flatzinc/FlatzincFullExtParser.g:205:5: ( BOOL | FLOAT | SET OF INT | INT )
            int alt31=4;
            switch ( input.LA(1) ) {
            case BOOL:
                {
                alt31=1;
                }
                break;
            case FLOAT:
                {
                alt31=2;
                }
                break;
            case SET:
                {
                alt31=3;
                }
                break;
            case INT:
                {
                alt31=4;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 31, 0, input);

                throw nvae;

            }

            switch (alt31) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtParser.g:205:9: BOOL
                    {
                    root_0 = (Object)adaptor.nil();


                    BOOL122=(Token)match(input,BOOL,FOLLOW_BOOL_in_par_type_u1151); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    BOOL122_tree = 
                    (Object)adaptor.create(BOOL122)
                    ;
                    adaptor.addChild(root_0, BOOL122_tree);
                    }

                    }
                    break;
                case 2 :
                    // parser/flatzinc/FlatzincFullExtParser.g:206:9: FLOAT
                    {
                    root_0 = (Object)adaptor.nil();


                    FLOAT123=(Token)match(input,FLOAT,FOLLOW_FLOAT_in_par_type_u1161); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    FLOAT123_tree = 
                    (Object)adaptor.create(FLOAT123)
                    ;
                    adaptor.addChild(root_0, FLOAT123_tree);
                    }

                    }
                    break;
                case 3 :
                    // parser/flatzinc/FlatzincFullExtParser.g:207:9: SET OF INT
                    {
                    root_0 = (Object)adaptor.nil();


                    SET124=(Token)match(input,SET,FOLLOW_SET_in_par_type_u1171); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    SET124_tree = 
                    (Object)adaptor.create(SET124)
                    ;
                    adaptor.addChild(root_0, SET124_tree);
                    }

                    OF125=(Token)match(input,OF,FOLLOW_OF_in_par_type_u1173); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    OF125_tree = 
                    (Object)adaptor.create(OF125)
                    ;
                    adaptor.addChild(root_0, OF125_tree);
                    }

                    INT126=(Token)match(input,INT,FOLLOW_INT_in_par_type_u1175); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    INT126_tree = 
                    (Object)adaptor.create(INT126)
                    ;
                    adaptor.addChild(root_0, INT126_tree);
                    }

                    }
                    break;
                case 4 :
                    // parser/flatzinc/FlatzincFullExtParser.g:208:9: INT
                    {
                    root_0 = (Object)adaptor.nil();


                    INT127=(Token)match(input,INT,FOLLOW_INT_in_par_type_u1185); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    INT127_tree = 
                    (Object)adaptor.create(INT127)
                    ;
                    adaptor.addChild(root_0, INT127_tree);
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);


            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "par_type_u"


    public static class var_type_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "var_type"
    // parser/flatzinc/FlatzincFullExtParser.g:211:1: var_type : ( ARRAY LS index_set ( CM index_set )* RS OF VAR var_type_u -> ^( ARRVAR ( index_set )+ var_type_u ) | VAR var_type_u -> ^( AVAR var_type_u ) );
    public final FlatzincFullExtParser.var_type_return var_type() throws RecognitionException {
        FlatzincFullExtParser.var_type_return retval = new FlatzincFullExtParser.var_type_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token ARRAY128=null;
        Token LS129=null;
        Token CM131=null;
        Token RS133=null;
        Token OF134=null;
        Token VAR135=null;
        Token VAR137=null;
        FlatzincFullExtParser.index_set_return index_set130 =null;

        FlatzincFullExtParser.index_set_return index_set132 =null;

        FlatzincFullExtParser.var_type_u_return var_type_u136 =null;

        FlatzincFullExtParser.var_type_u_return var_type_u138 =null;


        Object ARRAY128_tree=null;
        Object LS129_tree=null;
        Object CM131_tree=null;
        Object RS133_tree=null;
        Object OF134_tree=null;
        Object VAR135_tree=null;
        Object VAR137_tree=null;
        RewriteRuleTokenStream stream_RS=new RewriteRuleTokenStream(adaptor,"token RS");
        RewriteRuleTokenStream stream_VAR=new RewriteRuleTokenStream(adaptor,"token VAR");
        RewriteRuleTokenStream stream_OF=new RewriteRuleTokenStream(adaptor,"token OF");
        RewriteRuleTokenStream stream_ARRAY=new RewriteRuleTokenStream(adaptor,"token ARRAY");
        RewriteRuleTokenStream stream_LS=new RewriteRuleTokenStream(adaptor,"token LS");
        RewriteRuleTokenStream stream_CM=new RewriteRuleTokenStream(adaptor,"token CM");
        RewriteRuleSubtreeStream stream_index_set=new RewriteRuleSubtreeStream(adaptor,"rule index_set");
        RewriteRuleSubtreeStream stream_var_type_u=new RewriteRuleSubtreeStream(adaptor,"rule var_type_u");
        try {
            // parser/flatzinc/FlatzincFullExtParser.g:212:5: ( ARRAY LS index_set ( CM index_set )* RS OF VAR var_type_u -> ^( ARRVAR ( index_set )+ var_type_u ) | VAR var_type_u -> ^( AVAR var_type_u ) )
            int alt33=2;
            switch ( input.LA(1) ) {
            case ARRAY:
                {
                alt33=1;
                }
                break;
            case VAR:
                {
                alt33=2;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 33, 0, input);

                throw nvae;

            }

            switch (alt33) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtParser.g:212:9: ARRAY LS index_set ( CM index_set )* RS OF VAR var_type_u
                    {
                    ARRAY128=(Token)match(input,ARRAY,FOLLOW_ARRAY_in_var_type1204); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ARRAY.add(ARRAY128);


                    LS129=(Token)match(input,LS,FOLLOW_LS_in_var_type1206); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LS.add(LS129);


                    pushFollow(FOLLOW_index_set_in_var_type1208);
                    index_set130=index_set();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_index_set.add(index_set130.getTree());

                    // parser/flatzinc/FlatzincFullExtParser.g:212:28: ( CM index_set )*
                    loop32:
                    do {
                        int alt32=2;
                        switch ( input.LA(1) ) {
                        case CM:
                            {
                            alt32=1;
                            }
                            break;

                        }

                        switch (alt32) {
                    	case 1 :
                    	    // parser/flatzinc/FlatzincFullExtParser.g:212:29: CM index_set
                    	    {
                    	    CM131=(Token)match(input,CM,FOLLOW_CM_in_var_type1211); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_CM.add(CM131);


                    	    pushFollow(FOLLOW_index_set_in_var_type1213);
                    	    index_set132=index_set();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_index_set.add(index_set132.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop32;
                        }
                    } while (true);


                    RS133=(Token)match(input,RS,FOLLOW_RS_in_var_type1217); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RS.add(RS133);


                    OF134=(Token)match(input,OF,FOLLOW_OF_in_var_type1219); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_OF.add(OF134);


                    VAR135=(Token)match(input,VAR,FOLLOW_VAR_in_var_type1221); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_VAR.add(VAR135);


                    pushFollow(FOLLOW_var_type_u_in_var_type1223);
                    var_type_u136=var_type_u();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_var_type_u.add(var_type_u136.getTree());

                    // AST REWRITE
                    // elements: var_type_u, index_set
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {

                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 213:5: -> ^( ARRVAR ( index_set )+ var_type_u )
                    {
                        // parser/flatzinc/FlatzincFullExtParser.g:213:9: ^( ARRVAR ( index_set )+ var_type_u )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot(
                        (Object)adaptor.create(ARRVAR, "ARRVAR")
                        , root_1);

                        if ( !(stream_index_set.hasNext()) ) {
                            throw new RewriteEarlyExitException();
                        }
                        while ( stream_index_set.hasNext() ) {
                            adaptor.addChild(root_1, stream_index_set.nextTree());

                        }
                        stream_index_set.reset();

                        adaptor.addChild(root_1, stream_var_type_u.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }


                    retval.tree = root_0;
                    }

                    }
                    break;
                case 2 :
                    // parser/flatzinc/FlatzincFullExtParser.g:214:9: VAR var_type_u
                    {
                    VAR137=(Token)match(input,VAR,FOLLOW_VAR_in_var_type1249); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_VAR.add(VAR137);


                    pushFollow(FOLLOW_var_type_u_in_var_type1251);
                    var_type_u138=var_type_u();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_var_type_u.add(var_type_u138.getTree());

                    // AST REWRITE
                    // elements: var_type_u
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {

                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 215:5: -> ^( AVAR var_type_u )
                    {
                        // parser/flatzinc/FlatzincFullExtParser.g:215:9: ^( AVAR var_type_u )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot(
                        (Object)adaptor.create(AVAR, "AVAR")
                        , root_1);

                        adaptor.addChild(root_1, stream_var_type_u.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }


                    retval.tree = root_0;
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);


            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "var_type"


    public static class var_type_u_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "var_type_u"
    // parser/flatzinc/FlatzincFullExtParser.g:218:1: var_type_u : ( BOOL | FLOAT | INT | INT_CONST DD INT_CONST -> ^( DD INT_CONST INT_CONST ) | LB INT_CONST ( CM INT_CONST )* RB -> ^( CM ( INT_CONST )+ ) | SET OF INT_CONST DD INT_CONST -> ^( SET ^( DD INT_CONST INT_CONST ) ) | SET OF LB INT_CONST ( CM INT_CONST )* RB -> ^( SET ^( CM INT_CONST INT_CONST ) ) );
    public final FlatzincFullExtParser.var_type_u_return var_type_u() throws RecognitionException {
        FlatzincFullExtParser.var_type_u_return retval = new FlatzincFullExtParser.var_type_u_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token BOOL139=null;
        Token FLOAT140=null;
        Token INT141=null;
        Token INT_CONST142=null;
        Token DD143=null;
        Token INT_CONST144=null;
        Token LB145=null;
        Token INT_CONST146=null;
        Token CM147=null;
        Token INT_CONST148=null;
        Token RB149=null;
        Token SET150=null;
        Token OF151=null;
        Token INT_CONST152=null;
        Token DD153=null;
        Token INT_CONST154=null;
        Token SET155=null;
        Token OF156=null;
        Token LB157=null;
        Token INT_CONST158=null;
        Token CM159=null;
        Token INT_CONST160=null;
        Token RB161=null;

        Object BOOL139_tree=null;
        Object FLOAT140_tree=null;
        Object INT141_tree=null;
        Object INT_CONST142_tree=null;
        Object DD143_tree=null;
        Object INT_CONST144_tree=null;
        Object LB145_tree=null;
        Object INT_CONST146_tree=null;
        Object CM147_tree=null;
        Object INT_CONST148_tree=null;
        Object RB149_tree=null;
        Object SET150_tree=null;
        Object OF151_tree=null;
        Object INT_CONST152_tree=null;
        Object DD153_tree=null;
        Object INT_CONST154_tree=null;
        Object SET155_tree=null;
        Object OF156_tree=null;
        Object LB157_tree=null;
        Object INT_CONST158_tree=null;
        Object CM159_tree=null;
        Object INT_CONST160_tree=null;
        Object RB161_tree=null;
        RewriteRuleTokenStream stream_INT_CONST=new RewriteRuleTokenStream(adaptor,"token INT_CONST");
        RewriteRuleTokenStream stream_SET=new RewriteRuleTokenStream(adaptor,"token SET");
        RewriteRuleTokenStream stream_LB=new RewriteRuleTokenStream(adaptor,"token LB");
        RewriteRuleTokenStream stream_RB=new RewriteRuleTokenStream(adaptor,"token RB");
        RewriteRuleTokenStream stream_OF=new RewriteRuleTokenStream(adaptor,"token OF");
        RewriteRuleTokenStream stream_DD=new RewriteRuleTokenStream(adaptor,"token DD");
        RewriteRuleTokenStream stream_CM=new RewriteRuleTokenStream(adaptor,"token CM");

        try {
            // parser/flatzinc/FlatzincFullExtParser.g:219:5: ( BOOL | FLOAT | INT | INT_CONST DD INT_CONST -> ^( DD INT_CONST INT_CONST ) | LB INT_CONST ( CM INT_CONST )* RB -> ^( CM ( INT_CONST )+ ) | SET OF INT_CONST DD INT_CONST -> ^( SET ^( DD INT_CONST INT_CONST ) ) | SET OF LB INT_CONST ( CM INT_CONST )* RB -> ^( SET ^( CM INT_CONST INT_CONST ) ) )
            int alt36=7;
            switch ( input.LA(1) ) {
            case BOOL:
                {
                alt36=1;
                }
                break;
            case FLOAT:
                {
                alt36=2;
                }
                break;
            case INT:
                {
                alt36=3;
                }
                break;
            case INT_CONST:
                {
                alt36=4;
                }
                break;
            case LB:
                {
                alt36=5;
                }
                break;
            case SET:
                {
                switch ( input.LA(2) ) {
                case OF:
                    {
                    switch ( input.LA(3) ) {
                    case INT_CONST:
                        {
                        alt36=6;
                        }
                        break;
                    case LB:
                        {
                        alt36=7;
                        }
                        break;
                    default:
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 36, 7, input);

                        throw nvae;

                    }

                    }
                    break;
                default:
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 36, 6, input);

                    throw nvae;

                }

                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 36, 0, input);

                throw nvae;

            }

            switch (alt36) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtParser.g:219:9: BOOL
                    {
                    root_0 = (Object)adaptor.nil();


                    BOOL139=(Token)match(input,BOOL,FOLLOW_BOOL_in_var_type_u1283); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    BOOL139_tree = 
                    (Object)adaptor.create(BOOL139)
                    ;
                    adaptor.addChild(root_0, BOOL139_tree);
                    }

                    }
                    break;
                case 2 :
                    // parser/flatzinc/FlatzincFullExtParser.g:220:9: FLOAT
                    {
                    root_0 = (Object)adaptor.nil();


                    FLOAT140=(Token)match(input,FLOAT,FOLLOW_FLOAT_in_var_type_u1293); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    FLOAT140_tree = 
                    (Object)adaptor.create(FLOAT140)
                    ;
                    adaptor.addChild(root_0, FLOAT140_tree);
                    }

                    }
                    break;
                case 3 :
                    // parser/flatzinc/FlatzincFullExtParser.g:221:9: INT
                    {
                    root_0 = (Object)adaptor.nil();


                    INT141=(Token)match(input,INT,FOLLOW_INT_in_var_type_u1303); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    INT141_tree = 
                    (Object)adaptor.create(INT141)
                    ;
                    adaptor.addChild(root_0, INT141_tree);
                    }

                    }
                    break;
                case 4 :
                    // parser/flatzinc/FlatzincFullExtParser.g:222:9: INT_CONST DD INT_CONST
                    {
                    INT_CONST142=(Token)match(input,INT_CONST,FOLLOW_INT_CONST_in_var_type_u1313); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_INT_CONST.add(INT_CONST142);


                    DD143=(Token)match(input,DD,FOLLOW_DD_in_var_type_u1315); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_DD.add(DD143);


                    INT_CONST144=(Token)match(input,INT_CONST,FOLLOW_INT_CONST_in_var_type_u1317); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_INT_CONST.add(INT_CONST144);


                    // AST REWRITE
                    // elements: INT_CONST, INT_CONST, DD
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {

                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 223:5: -> ^( DD INT_CONST INT_CONST )
                    {
                        // parser/flatzinc/FlatzincFullExtParser.g:223:9: ^( DD INT_CONST INT_CONST )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot(
                        stream_DD.nextNode()
                        , root_1);

                        adaptor.addChild(root_1, 
                        stream_INT_CONST.nextNode()
                        );

                        adaptor.addChild(root_1, 
                        stream_INT_CONST.nextNode()
                        );

                        adaptor.addChild(root_0, root_1);
                        }

                    }


                    retval.tree = root_0;
                    }

                    }
                    break;
                case 5 :
                    // parser/flatzinc/FlatzincFullExtParser.g:226:9: LB INT_CONST ( CM INT_CONST )* RB
                    {
                    LB145=(Token)match(input,LB,FOLLOW_LB_in_var_type_u1344); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LB.add(LB145);


                    INT_CONST146=(Token)match(input,INT_CONST,FOLLOW_INT_CONST_in_var_type_u1346); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_INT_CONST.add(INT_CONST146);


                    // parser/flatzinc/FlatzincFullExtParser.g:226:22: ( CM INT_CONST )*
                    loop34:
                    do {
                        int alt34=2;
                        switch ( input.LA(1) ) {
                        case CM:
                            {
                            alt34=1;
                            }
                            break;

                        }

                        switch (alt34) {
                    	case 1 :
                    	    // parser/flatzinc/FlatzincFullExtParser.g:226:23: CM INT_CONST
                    	    {
                    	    CM147=(Token)match(input,CM,FOLLOW_CM_in_var_type_u1349); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_CM.add(CM147);


                    	    INT_CONST148=(Token)match(input,INT_CONST,FOLLOW_INT_CONST_in_var_type_u1351); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_INT_CONST.add(INT_CONST148);


                    	    }
                    	    break;

                    	default :
                    	    break loop34;
                        }
                    } while (true);


                    RB149=(Token)match(input,RB,FOLLOW_RB_in_var_type_u1355); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RB.add(RB149);


                    // AST REWRITE
                    // elements: INT_CONST, CM
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {

                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 227:5: -> ^( CM ( INT_CONST )+ )
                    {
                        // parser/flatzinc/FlatzincFullExtParser.g:227:9: ^( CM ( INT_CONST )+ )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot(
                        stream_CM.nextNode()
                        , root_1);

                        if ( !(stream_INT_CONST.hasNext()) ) {
                            throw new RewriteEarlyExitException();
                        }
                        while ( stream_INT_CONST.hasNext() ) {
                            adaptor.addChild(root_1, 
                            stream_INT_CONST.nextNode()
                            );

                        }
                        stream_INT_CONST.reset();

                        adaptor.addChild(root_0, root_1);
                        }

                    }


                    retval.tree = root_0;
                    }

                    }
                    break;
                case 6 :
                    // parser/flatzinc/FlatzincFullExtParser.g:228:9: SET OF INT_CONST DD INT_CONST
                    {
                    SET150=(Token)match(input,SET,FOLLOW_SET_in_var_type_u1379); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_SET.add(SET150);


                    OF151=(Token)match(input,OF,FOLLOW_OF_in_var_type_u1381); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_OF.add(OF151);


                    INT_CONST152=(Token)match(input,INT_CONST,FOLLOW_INT_CONST_in_var_type_u1383); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_INT_CONST.add(INT_CONST152);


                    DD153=(Token)match(input,DD,FOLLOW_DD_in_var_type_u1385); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_DD.add(DD153);


                    INT_CONST154=(Token)match(input,INT_CONST,FOLLOW_INT_CONST_in_var_type_u1387); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_INT_CONST.add(INT_CONST154);


                    // AST REWRITE
                    // elements: INT_CONST, SET, INT_CONST, DD
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {

                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 229:5: -> ^( SET ^( DD INT_CONST INT_CONST ) )
                    {
                        // parser/flatzinc/FlatzincFullExtParser.g:229:9: ^( SET ^( DD INT_CONST INT_CONST ) )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot(
                        stream_SET.nextNode()
                        , root_1);

                        // parser/flatzinc/FlatzincFullExtParser.g:229:15: ^( DD INT_CONST INT_CONST )
                        {
                        Object root_2 = (Object)adaptor.nil();
                        root_2 = (Object)adaptor.becomeRoot(
                        stream_DD.nextNode()
                        , root_2);

                        adaptor.addChild(root_2, 
                        stream_INT_CONST.nextNode()
                        );

                        adaptor.addChild(root_2, 
                        stream_INT_CONST.nextNode()
                        );

                        adaptor.addChild(root_1, root_2);
                        }

                        adaptor.addChild(root_0, root_1);
                        }

                    }


                    retval.tree = root_0;
                    }

                    }
                    break;
                case 7 :
                    // parser/flatzinc/FlatzincFullExtParser.g:230:9: SET OF LB INT_CONST ( CM INT_CONST )* RB
                    {
                    SET155=(Token)match(input,SET,FOLLOW_SET_in_var_type_u1416); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_SET.add(SET155);


                    OF156=(Token)match(input,OF,FOLLOW_OF_in_var_type_u1418); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_OF.add(OF156);


                    LB157=(Token)match(input,LB,FOLLOW_LB_in_var_type_u1420); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LB.add(LB157);


                    INT_CONST158=(Token)match(input,INT_CONST,FOLLOW_INT_CONST_in_var_type_u1422); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_INT_CONST.add(INT_CONST158);


                    // parser/flatzinc/FlatzincFullExtParser.g:230:29: ( CM INT_CONST )*
                    loop35:
                    do {
                        int alt35=2;
                        switch ( input.LA(1) ) {
                        case CM:
                            {
                            alt35=1;
                            }
                            break;

                        }

                        switch (alt35) {
                    	case 1 :
                    	    // parser/flatzinc/FlatzincFullExtParser.g:230:30: CM INT_CONST
                    	    {
                    	    CM159=(Token)match(input,CM,FOLLOW_CM_in_var_type_u1425); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_CM.add(CM159);


                    	    INT_CONST160=(Token)match(input,INT_CONST,FOLLOW_INT_CONST_in_var_type_u1427); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_INT_CONST.add(INT_CONST160);


                    	    }
                    	    break;

                    	default :
                    	    break loop35;
                        }
                    } while (true);


                    RB161=(Token)match(input,RB,FOLLOW_RB_in_var_type_u1431); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RB.add(RB161);


                    // AST REWRITE
                    // elements: CM, INT_CONST, INT_CONST, SET
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {

                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 231:5: -> ^( SET ^( CM INT_CONST INT_CONST ) )
                    {
                        // parser/flatzinc/FlatzincFullExtParser.g:231:9: ^( SET ^( CM INT_CONST INT_CONST ) )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot(
                        stream_SET.nextNode()
                        , root_1);

                        // parser/flatzinc/FlatzincFullExtParser.g:231:15: ^( CM INT_CONST INT_CONST )
                        {
                        Object root_2 = (Object)adaptor.nil();
                        root_2 = (Object)adaptor.becomeRoot(
                        stream_CM.nextNode()
                        , root_2);

                        adaptor.addChild(root_2, 
                        stream_INT_CONST.nextNode()
                        );

                        adaptor.addChild(root_2, 
                        stream_INT_CONST.nextNode()
                        );

                        adaptor.addChild(root_1, root_2);
                        }

                        adaptor.addChild(root_0, root_1);
                        }

                    }


                    retval.tree = root_0;
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);


            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "var_type_u"


    public static class par_pred_param_type_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "par_pred_param_type"
    // parser/flatzinc/FlatzincFullExtParser.g:234:1: par_pred_param_type : ( par_type | INT_CONST DD INT_CONST -> ^( DD INT_CONST INT_CONST ) | LB INT_CONST ( CM INT_CONST )* RB -> ^( CM ( INT_CONST )+ ) | SET OF INT_CONST DD INT_CONST -> ^( SET ^( DD INT_CONST INT_CONST ) ) | SET OF LB INT_CONST ( CM INT_CONST )* RB -> ^( SET ^( CM ( INT_CONST )+ ) ) | ARRAY LS index_set ( CM index_set )* RS OF INT_CONST DD INT_CONST -> ^( ARRAY ( index_set )+ ^( DD INT_CONST INT_CONST ) ) | ARRAY LS index_set ( CM index_set )* RS OF LB INT_CONST ( CM INT_CONST )* RB -> ^( ARRAY ( index_set )+ ^( CM ( INT_CONST )+ ) ) | ARRAY LS index_set ( CM index_set )* RS OF SET OF INT_CONST DD INT_CONST -> ^( ARRAY ( index_set )+ ^( SET ^( DD INT_CONST INT_CONST ) ) ) | ARRAY LS index_set ( CM index_set )* RS OF SET OF LB INT_CONST ( CM INT_CONST )* RB -> ^( ARRAY ( index_set )+ ^( SET ^( CM ( INT_CONST )+ ) ) ) );
    public final FlatzincFullExtParser.par_pred_param_type_return par_pred_param_type() throws RecognitionException {
        FlatzincFullExtParser.par_pred_param_type_return retval = new FlatzincFullExtParser.par_pred_param_type_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token INT_CONST163=null;
        Token DD164=null;
        Token INT_CONST165=null;
        Token LB166=null;
        Token INT_CONST167=null;
        Token CM168=null;
        Token INT_CONST169=null;
        Token RB170=null;
        Token SET171=null;
        Token OF172=null;
        Token INT_CONST173=null;
        Token DD174=null;
        Token INT_CONST175=null;
        Token SET176=null;
        Token OF177=null;
        Token LB178=null;
        Token INT_CONST179=null;
        Token CM180=null;
        Token INT_CONST181=null;
        Token RB182=null;
        Token ARRAY183=null;
        Token LS184=null;
        Token CM186=null;
        Token RS188=null;
        Token OF189=null;
        Token INT_CONST190=null;
        Token DD191=null;
        Token INT_CONST192=null;
        Token ARRAY193=null;
        Token LS194=null;
        Token CM196=null;
        Token RS198=null;
        Token OF199=null;
        Token LB200=null;
        Token INT_CONST201=null;
        Token CM202=null;
        Token INT_CONST203=null;
        Token RB204=null;
        Token ARRAY205=null;
        Token LS206=null;
        Token CM208=null;
        Token RS210=null;
        Token OF211=null;
        Token SET212=null;
        Token OF213=null;
        Token INT_CONST214=null;
        Token DD215=null;
        Token INT_CONST216=null;
        Token ARRAY217=null;
        Token LS218=null;
        Token CM220=null;
        Token RS222=null;
        Token OF223=null;
        Token SET224=null;
        Token OF225=null;
        Token LB226=null;
        Token INT_CONST227=null;
        Token CM228=null;
        Token INT_CONST229=null;
        Token RB230=null;
        FlatzincFullExtParser.par_type_return par_type162 =null;

        FlatzincFullExtParser.index_set_return index_set185 =null;

        FlatzincFullExtParser.index_set_return index_set187 =null;

        FlatzincFullExtParser.index_set_return index_set195 =null;

        FlatzincFullExtParser.index_set_return index_set197 =null;

        FlatzincFullExtParser.index_set_return index_set207 =null;

        FlatzincFullExtParser.index_set_return index_set209 =null;

        FlatzincFullExtParser.index_set_return index_set219 =null;

        FlatzincFullExtParser.index_set_return index_set221 =null;


        Object INT_CONST163_tree=null;
        Object DD164_tree=null;
        Object INT_CONST165_tree=null;
        Object LB166_tree=null;
        Object INT_CONST167_tree=null;
        Object CM168_tree=null;
        Object INT_CONST169_tree=null;
        Object RB170_tree=null;
        Object SET171_tree=null;
        Object OF172_tree=null;
        Object INT_CONST173_tree=null;
        Object DD174_tree=null;
        Object INT_CONST175_tree=null;
        Object SET176_tree=null;
        Object OF177_tree=null;
        Object LB178_tree=null;
        Object INT_CONST179_tree=null;
        Object CM180_tree=null;
        Object INT_CONST181_tree=null;
        Object RB182_tree=null;
        Object ARRAY183_tree=null;
        Object LS184_tree=null;
        Object CM186_tree=null;
        Object RS188_tree=null;
        Object OF189_tree=null;
        Object INT_CONST190_tree=null;
        Object DD191_tree=null;
        Object INT_CONST192_tree=null;
        Object ARRAY193_tree=null;
        Object LS194_tree=null;
        Object CM196_tree=null;
        Object RS198_tree=null;
        Object OF199_tree=null;
        Object LB200_tree=null;
        Object INT_CONST201_tree=null;
        Object CM202_tree=null;
        Object INT_CONST203_tree=null;
        Object RB204_tree=null;
        Object ARRAY205_tree=null;
        Object LS206_tree=null;
        Object CM208_tree=null;
        Object RS210_tree=null;
        Object OF211_tree=null;
        Object SET212_tree=null;
        Object OF213_tree=null;
        Object INT_CONST214_tree=null;
        Object DD215_tree=null;
        Object INT_CONST216_tree=null;
        Object ARRAY217_tree=null;
        Object LS218_tree=null;
        Object CM220_tree=null;
        Object RS222_tree=null;
        Object OF223_tree=null;
        Object SET224_tree=null;
        Object OF225_tree=null;
        Object LB226_tree=null;
        Object INT_CONST227_tree=null;
        Object CM228_tree=null;
        Object INT_CONST229_tree=null;
        Object RB230_tree=null;
        RewriteRuleTokenStream stream_RS=new RewriteRuleTokenStream(adaptor,"token RS");
        RewriteRuleTokenStream stream_INT_CONST=new RewriteRuleTokenStream(adaptor,"token INT_CONST");
        RewriteRuleTokenStream stream_SET=new RewriteRuleTokenStream(adaptor,"token SET");
        RewriteRuleTokenStream stream_LB=new RewriteRuleTokenStream(adaptor,"token LB");
        RewriteRuleTokenStream stream_RB=new RewriteRuleTokenStream(adaptor,"token RB");
        RewriteRuleTokenStream stream_OF=new RewriteRuleTokenStream(adaptor,"token OF");
        RewriteRuleTokenStream stream_DD=new RewriteRuleTokenStream(adaptor,"token DD");
        RewriteRuleTokenStream stream_ARRAY=new RewriteRuleTokenStream(adaptor,"token ARRAY");
        RewriteRuleTokenStream stream_LS=new RewriteRuleTokenStream(adaptor,"token LS");
        RewriteRuleTokenStream stream_CM=new RewriteRuleTokenStream(adaptor,"token CM");
        RewriteRuleSubtreeStream stream_index_set=new RewriteRuleSubtreeStream(adaptor,"rule index_set");
        try {
            // parser/flatzinc/FlatzincFullExtParser.g:235:5: ( par_type | INT_CONST DD INT_CONST -> ^( DD INT_CONST INT_CONST ) | LB INT_CONST ( CM INT_CONST )* RB -> ^( CM ( INT_CONST )+ ) | SET OF INT_CONST DD INT_CONST -> ^( SET ^( DD INT_CONST INT_CONST ) ) | SET OF LB INT_CONST ( CM INT_CONST )* RB -> ^( SET ^( CM ( INT_CONST )+ ) ) | ARRAY LS index_set ( CM index_set )* RS OF INT_CONST DD INT_CONST -> ^( ARRAY ( index_set )+ ^( DD INT_CONST INT_CONST ) ) | ARRAY LS index_set ( CM index_set )* RS OF LB INT_CONST ( CM INT_CONST )* RB -> ^( ARRAY ( index_set )+ ^( CM ( INT_CONST )+ ) ) | ARRAY LS index_set ( CM index_set )* RS OF SET OF INT_CONST DD INT_CONST -> ^( ARRAY ( index_set )+ ^( SET ^( DD INT_CONST INT_CONST ) ) ) | ARRAY LS index_set ( CM index_set )* RS OF SET OF LB INT_CONST ( CM INT_CONST )* RB -> ^( ARRAY ( index_set )+ ^( SET ^( CM ( INT_CONST )+ ) ) ) )
            int alt45=9;
            alt45 = dfa45.predict(input);
            switch (alt45) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtParser.g:235:9: par_type
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_par_type_in_par_pred_param_type1469);
                    par_type162=par_type();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, par_type162.getTree());

                    }
                    break;
                case 2 :
                    // parser/flatzinc/FlatzincFullExtParser.g:238:9: INT_CONST DD INT_CONST
                    {
                    INT_CONST163=(Token)match(input,INT_CONST,FOLLOW_INT_CONST_in_par_pred_param_type1481); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_INT_CONST.add(INT_CONST163);


                    DD164=(Token)match(input,DD,FOLLOW_DD_in_par_pred_param_type1483); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_DD.add(DD164);


                    INT_CONST165=(Token)match(input,INT_CONST,FOLLOW_INT_CONST_in_par_pred_param_type1485); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_INT_CONST.add(INT_CONST165);


                    // AST REWRITE
                    // elements: DD, INT_CONST, INT_CONST
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {

                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 239:5: -> ^( DD INT_CONST INT_CONST )
                    {
                        // parser/flatzinc/FlatzincFullExtParser.g:239:9: ^( DD INT_CONST INT_CONST )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot(
                        stream_DD.nextNode()
                        , root_1);

                        adaptor.addChild(root_1, 
                        stream_INT_CONST.nextNode()
                        );

                        adaptor.addChild(root_1, 
                        stream_INT_CONST.nextNode()
                        );

                        adaptor.addChild(root_0, root_1);
                        }

                    }


                    retval.tree = root_0;
                    }

                    }
                    break;
                case 3 :
                    // parser/flatzinc/FlatzincFullExtParser.g:240:9: LB INT_CONST ( CM INT_CONST )* RB
                    {
                    LB166=(Token)match(input,LB,FOLLOW_LB_in_par_pred_param_type1510); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LB.add(LB166);


                    INT_CONST167=(Token)match(input,INT_CONST,FOLLOW_INT_CONST_in_par_pred_param_type1512); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_INT_CONST.add(INT_CONST167);


                    // parser/flatzinc/FlatzincFullExtParser.g:240:22: ( CM INT_CONST )*
                    loop37:
                    do {
                        int alt37=2;
                        switch ( input.LA(1) ) {
                        case CM:
                            {
                            alt37=1;
                            }
                            break;

                        }

                        switch (alt37) {
                    	case 1 :
                    	    // parser/flatzinc/FlatzincFullExtParser.g:240:23: CM INT_CONST
                    	    {
                    	    CM168=(Token)match(input,CM,FOLLOW_CM_in_par_pred_param_type1515); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_CM.add(CM168);


                    	    INT_CONST169=(Token)match(input,INT_CONST,FOLLOW_INT_CONST_in_par_pred_param_type1517); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_INT_CONST.add(INT_CONST169);


                    	    }
                    	    break;

                    	default :
                    	    break loop37;
                        }
                    } while (true);


                    RB170=(Token)match(input,RB,FOLLOW_RB_in_par_pred_param_type1521); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RB.add(RB170);


                    // AST REWRITE
                    // elements: CM, INT_CONST
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {

                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 241:5: -> ^( CM ( INT_CONST )+ )
                    {
                        // parser/flatzinc/FlatzincFullExtParser.g:241:9: ^( CM ( INT_CONST )+ )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot(
                        stream_CM.nextNode()
                        , root_1);

                        if ( !(stream_INT_CONST.hasNext()) ) {
                            throw new RewriteEarlyExitException();
                        }
                        while ( stream_INT_CONST.hasNext() ) {
                            adaptor.addChild(root_1, 
                            stream_INT_CONST.nextNode()
                            );

                        }
                        stream_INT_CONST.reset();

                        adaptor.addChild(root_0, root_1);
                        }

                    }


                    retval.tree = root_0;
                    }

                    }
                    break;
                case 4 :
                    // parser/flatzinc/FlatzincFullExtParser.g:242:9: SET OF INT_CONST DD INT_CONST
                    {
                    SET171=(Token)match(input,SET,FOLLOW_SET_in_par_pred_param_type1545); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_SET.add(SET171);


                    OF172=(Token)match(input,OF,FOLLOW_OF_in_par_pred_param_type1547); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_OF.add(OF172);


                    INT_CONST173=(Token)match(input,INT_CONST,FOLLOW_INT_CONST_in_par_pred_param_type1549); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_INT_CONST.add(INT_CONST173);


                    DD174=(Token)match(input,DD,FOLLOW_DD_in_par_pred_param_type1551); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_DD.add(DD174);


                    INT_CONST175=(Token)match(input,INT_CONST,FOLLOW_INT_CONST_in_par_pred_param_type1553); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_INT_CONST.add(INT_CONST175);


                    // AST REWRITE
                    // elements: INT_CONST, INT_CONST, DD, SET
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {

                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 243:5: -> ^( SET ^( DD INT_CONST INT_CONST ) )
                    {
                        // parser/flatzinc/FlatzincFullExtParser.g:243:9: ^( SET ^( DD INT_CONST INT_CONST ) )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot(
                        stream_SET.nextNode()
                        , root_1);

                        // parser/flatzinc/FlatzincFullExtParser.g:243:15: ^( DD INT_CONST INT_CONST )
                        {
                        Object root_2 = (Object)adaptor.nil();
                        root_2 = (Object)adaptor.becomeRoot(
                        stream_DD.nextNode()
                        , root_2);

                        adaptor.addChild(root_2, 
                        stream_INT_CONST.nextNode()
                        );

                        adaptor.addChild(root_2, 
                        stream_INT_CONST.nextNode()
                        );

                        adaptor.addChild(root_1, root_2);
                        }

                        adaptor.addChild(root_0, root_1);
                        }

                    }


                    retval.tree = root_0;
                    }

                    }
                    break;
                case 5 :
                    // parser/flatzinc/FlatzincFullExtParser.g:244:9: SET OF LB INT_CONST ( CM INT_CONST )* RB
                    {
                    SET176=(Token)match(input,SET,FOLLOW_SET_in_par_pred_param_type1582); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_SET.add(SET176);


                    OF177=(Token)match(input,OF,FOLLOW_OF_in_par_pred_param_type1584); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_OF.add(OF177);


                    LB178=(Token)match(input,LB,FOLLOW_LB_in_par_pred_param_type1586); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LB.add(LB178);


                    INT_CONST179=(Token)match(input,INT_CONST,FOLLOW_INT_CONST_in_par_pred_param_type1588); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_INT_CONST.add(INT_CONST179);


                    // parser/flatzinc/FlatzincFullExtParser.g:244:29: ( CM INT_CONST )*
                    loop38:
                    do {
                        int alt38=2;
                        switch ( input.LA(1) ) {
                        case CM:
                            {
                            alt38=1;
                            }
                            break;

                        }

                        switch (alt38) {
                    	case 1 :
                    	    // parser/flatzinc/FlatzincFullExtParser.g:244:30: CM INT_CONST
                    	    {
                    	    CM180=(Token)match(input,CM,FOLLOW_CM_in_par_pred_param_type1591); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_CM.add(CM180);


                    	    INT_CONST181=(Token)match(input,INT_CONST,FOLLOW_INT_CONST_in_par_pred_param_type1593); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_INT_CONST.add(INT_CONST181);


                    	    }
                    	    break;

                    	default :
                    	    break loop38;
                        }
                    } while (true);


                    RB182=(Token)match(input,RB,FOLLOW_RB_in_par_pred_param_type1597); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RB.add(RB182);


                    // AST REWRITE
                    // elements: CM, INT_CONST, SET
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {

                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 245:5: -> ^( SET ^( CM ( INT_CONST )+ ) )
                    {
                        // parser/flatzinc/FlatzincFullExtParser.g:245:9: ^( SET ^( CM ( INT_CONST )+ ) )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot(
                        stream_SET.nextNode()
                        , root_1);

                        // parser/flatzinc/FlatzincFullExtParser.g:245:15: ^( CM ( INT_CONST )+ )
                        {
                        Object root_2 = (Object)adaptor.nil();
                        root_2 = (Object)adaptor.becomeRoot(
                        stream_CM.nextNode()
                        , root_2);

                        if ( !(stream_INT_CONST.hasNext()) ) {
                            throw new RewriteEarlyExitException();
                        }
                        while ( stream_INT_CONST.hasNext() ) {
                            adaptor.addChild(root_2, 
                            stream_INT_CONST.nextNode()
                            );

                        }
                        stream_INT_CONST.reset();

                        adaptor.addChild(root_1, root_2);
                        }

                        adaptor.addChild(root_0, root_1);
                        }

                    }


                    retval.tree = root_0;
                    }

                    }
                    break;
                case 6 :
                    // parser/flatzinc/FlatzincFullExtParser.g:248:9: ARRAY LS index_set ( CM index_set )* RS OF INT_CONST DD INT_CONST
                    {
                    ARRAY183=(Token)match(input,ARRAY,FOLLOW_ARRAY_in_par_pred_param_type1627); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ARRAY.add(ARRAY183);


                    LS184=(Token)match(input,LS,FOLLOW_LS_in_par_pred_param_type1629); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LS.add(LS184);


                    pushFollow(FOLLOW_index_set_in_par_pred_param_type1631);
                    index_set185=index_set();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_index_set.add(index_set185.getTree());

                    // parser/flatzinc/FlatzincFullExtParser.g:248:28: ( CM index_set )*
                    loop39:
                    do {
                        int alt39=2;
                        switch ( input.LA(1) ) {
                        case CM:
                            {
                            alt39=1;
                            }
                            break;

                        }

                        switch (alt39) {
                    	case 1 :
                    	    // parser/flatzinc/FlatzincFullExtParser.g:248:29: CM index_set
                    	    {
                    	    CM186=(Token)match(input,CM,FOLLOW_CM_in_par_pred_param_type1634); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_CM.add(CM186);


                    	    pushFollow(FOLLOW_index_set_in_par_pred_param_type1636);
                    	    index_set187=index_set();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_index_set.add(index_set187.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop39;
                        }
                    } while (true);


                    RS188=(Token)match(input,RS,FOLLOW_RS_in_par_pred_param_type1640); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RS.add(RS188);


                    OF189=(Token)match(input,OF,FOLLOW_OF_in_par_pred_param_type1642); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_OF.add(OF189);


                    INT_CONST190=(Token)match(input,INT_CONST,FOLLOW_INT_CONST_in_par_pred_param_type1644); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_INT_CONST.add(INT_CONST190);


                    DD191=(Token)match(input,DD,FOLLOW_DD_in_par_pred_param_type1646); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_DD.add(DD191);


                    INT_CONST192=(Token)match(input,INT_CONST,FOLLOW_INT_CONST_in_par_pred_param_type1648); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_INT_CONST.add(INT_CONST192);


                    // AST REWRITE
                    // elements: INT_CONST, index_set, INT_CONST, DD, ARRAY
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {

                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 249:5: -> ^( ARRAY ( index_set )+ ^( DD INT_CONST INT_CONST ) )
                    {
                        // parser/flatzinc/FlatzincFullExtParser.g:249:9: ^( ARRAY ( index_set )+ ^( DD INT_CONST INT_CONST ) )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot(
                        stream_ARRAY.nextNode()
                        , root_1);

                        if ( !(stream_index_set.hasNext()) ) {
                            throw new RewriteEarlyExitException();
                        }
                        while ( stream_index_set.hasNext() ) {
                            adaptor.addChild(root_1, stream_index_set.nextTree());

                        }
                        stream_index_set.reset();

                        // parser/flatzinc/FlatzincFullExtParser.g:249:28: ^( DD INT_CONST INT_CONST )
                        {
                        Object root_2 = (Object)adaptor.nil();
                        root_2 = (Object)adaptor.becomeRoot(
                        stream_DD.nextNode()
                        , root_2);

                        adaptor.addChild(root_2, 
                        stream_INT_CONST.nextNode()
                        );

                        adaptor.addChild(root_2, 
                        stream_INT_CONST.nextNode()
                        );

                        adaptor.addChild(root_1, root_2);
                        }

                        adaptor.addChild(root_0, root_1);
                        }

                    }


                    retval.tree = root_0;
                    }

                    }
                    break;
                case 7 :
                    // parser/flatzinc/FlatzincFullExtParser.g:250:9: ARRAY LS index_set ( CM index_set )* RS OF LB INT_CONST ( CM INT_CONST )* RB
                    {
                    ARRAY193=(Token)match(input,ARRAY,FOLLOW_ARRAY_in_par_pred_param_type1680); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ARRAY.add(ARRAY193);


                    LS194=(Token)match(input,LS,FOLLOW_LS_in_par_pred_param_type1682); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LS.add(LS194);


                    pushFollow(FOLLOW_index_set_in_par_pred_param_type1684);
                    index_set195=index_set();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_index_set.add(index_set195.getTree());

                    // parser/flatzinc/FlatzincFullExtParser.g:250:28: ( CM index_set )*
                    loop40:
                    do {
                        int alt40=2;
                        switch ( input.LA(1) ) {
                        case CM:
                            {
                            alt40=1;
                            }
                            break;

                        }

                        switch (alt40) {
                    	case 1 :
                    	    // parser/flatzinc/FlatzincFullExtParser.g:250:29: CM index_set
                    	    {
                    	    CM196=(Token)match(input,CM,FOLLOW_CM_in_par_pred_param_type1687); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_CM.add(CM196);


                    	    pushFollow(FOLLOW_index_set_in_par_pred_param_type1689);
                    	    index_set197=index_set();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_index_set.add(index_set197.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop40;
                        }
                    } while (true);


                    RS198=(Token)match(input,RS,FOLLOW_RS_in_par_pred_param_type1693); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RS.add(RS198);


                    OF199=(Token)match(input,OF,FOLLOW_OF_in_par_pred_param_type1695); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_OF.add(OF199);


                    LB200=(Token)match(input,LB,FOLLOW_LB_in_par_pred_param_type1697); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LB.add(LB200);


                    INT_CONST201=(Token)match(input,INT_CONST,FOLLOW_INT_CONST_in_par_pred_param_type1699); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_INT_CONST.add(INT_CONST201);


                    // parser/flatzinc/FlatzincFullExtParser.g:250:63: ( CM INT_CONST )*
                    loop41:
                    do {
                        int alt41=2;
                        switch ( input.LA(1) ) {
                        case CM:
                            {
                            alt41=1;
                            }
                            break;

                        }

                        switch (alt41) {
                    	case 1 :
                    	    // parser/flatzinc/FlatzincFullExtParser.g:250:64: CM INT_CONST
                    	    {
                    	    CM202=(Token)match(input,CM,FOLLOW_CM_in_par_pred_param_type1702); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_CM.add(CM202);


                    	    INT_CONST203=(Token)match(input,INT_CONST,FOLLOW_INT_CONST_in_par_pred_param_type1704); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_INT_CONST.add(INT_CONST203);


                    	    }
                    	    break;

                    	default :
                    	    break loop41;
                        }
                    } while (true);


                    RB204=(Token)match(input,RB,FOLLOW_RB_in_par_pred_param_type1708); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RB.add(RB204);


                    // AST REWRITE
                    // elements: ARRAY, CM, index_set, INT_CONST
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {

                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 251:5: -> ^( ARRAY ( index_set )+ ^( CM ( INT_CONST )+ ) )
                    {
                        // parser/flatzinc/FlatzincFullExtParser.g:251:9: ^( ARRAY ( index_set )+ ^( CM ( INT_CONST )+ ) )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot(
                        stream_ARRAY.nextNode()
                        , root_1);

                        if ( !(stream_index_set.hasNext()) ) {
                            throw new RewriteEarlyExitException();
                        }
                        while ( stream_index_set.hasNext() ) {
                            adaptor.addChild(root_1, stream_index_set.nextTree());

                        }
                        stream_index_set.reset();

                        // parser/flatzinc/FlatzincFullExtParser.g:251:28: ^( CM ( INT_CONST )+ )
                        {
                        Object root_2 = (Object)adaptor.nil();
                        root_2 = (Object)adaptor.becomeRoot(
                        stream_CM.nextNode()
                        , root_2);

                        if ( !(stream_INT_CONST.hasNext()) ) {
                            throw new RewriteEarlyExitException();
                        }
                        while ( stream_INT_CONST.hasNext() ) {
                            adaptor.addChild(root_2, 
                            stream_INT_CONST.nextNode()
                            );

                        }
                        stream_INT_CONST.reset();

                        adaptor.addChild(root_1, root_2);
                        }

                        adaptor.addChild(root_0, root_1);
                        }

                    }


                    retval.tree = root_0;
                    }

                    }
                    break;
                case 8 :
                    // parser/flatzinc/FlatzincFullExtParser.g:252:9: ARRAY LS index_set ( CM index_set )* RS OF SET OF INT_CONST DD INT_CONST
                    {
                    ARRAY205=(Token)match(input,ARRAY,FOLLOW_ARRAY_in_par_pred_param_type1739); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ARRAY.add(ARRAY205);


                    LS206=(Token)match(input,LS,FOLLOW_LS_in_par_pred_param_type1741); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LS.add(LS206);


                    pushFollow(FOLLOW_index_set_in_par_pred_param_type1743);
                    index_set207=index_set();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_index_set.add(index_set207.getTree());

                    // parser/flatzinc/FlatzincFullExtParser.g:252:28: ( CM index_set )*
                    loop42:
                    do {
                        int alt42=2;
                        switch ( input.LA(1) ) {
                        case CM:
                            {
                            alt42=1;
                            }
                            break;

                        }

                        switch (alt42) {
                    	case 1 :
                    	    // parser/flatzinc/FlatzincFullExtParser.g:252:29: CM index_set
                    	    {
                    	    CM208=(Token)match(input,CM,FOLLOW_CM_in_par_pred_param_type1746); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_CM.add(CM208);


                    	    pushFollow(FOLLOW_index_set_in_par_pred_param_type1748);
                    	    index_set209=index_set();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_index_set.add(index_set209.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop42;
                        }
                    } while (true);


                    RS210=(Token)match(input,RS,FOLLOW_RS_in_par_pred_param_type1752); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RS.add(RS210);


                    OF211=(Token)match(input,OF,FOLLOW_OF_in_par_pred_param_type1754); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_OF.add(OF211);


                    SET212=(Token)match(input,SET,FOLLOW_SET_in_par_pred_param_type1756); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_SET.add(SET212);


                    OF213=(Token)match(input,OF,FOLLOW_OF_in_par_pred_param_type1758); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_OF.add(OF213);


                    INT_CONST214=(Token)match(input,INT_CONST,FOLLOW_INT_CONST_in_par_pred_param_type1760); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_INT_CONST.add(INT_CONST214);


                    DD215=(Token)match(input,DD,FOLLOW_DD_in_par_pred_param_type1762); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_DD.add(DD215);


                    INT_CONST216=(Token)match(input,INT_CONST,FOLLOW_INT_CONST_in_par_pred_param_type1764); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_INT_CONST.add(INT_CONST216);


                    // AST REWRITE
                    // elements: SET, INT_CONST, DD, index_set, INT_CONST, ARRAY
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {

                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 253:5: -> ^( ARRAY ( index_set )+ ^( SET ^( DD INT_CONST INT_CONST ) ) )
                    {
                        // parser/flatzinc/FlatzincFullExtParser.g:253:9: ^( ARRAY ( index_set )+ ^( SET ^( DD INT_CONST INT_CONST ) ) )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot(
                        stream_ARRAY.nextNode()
                        , root_1);

                        if ( !(stream_index_set.hasNext()) ) {
                            throw new RewriteEarlyExitException();
                        }
                        while ( stream_index_set.hasNext() ) {
                            adaptor.addChild(root_1, stream_index_set.nextTree());

                        }
                        stream_index_set.reset();

                        // parser/flatzinc/FlatzincFullExtParser.g:253:28: ^( SET ^( DD INT_CONST INT_CONST ) )
                        {
                        Object root_2 = (Object)adaptor.nil();
                        root_2 = (Object)adaptor.becomeRoot(
                        stream_SET.nextNode()
                        , root_2);

                        // parser/flatzinc/FlatzincFullExtParser.g:253:34: ^( DD INT_CONST INT_CONST )
                        {
                        Object root_3 = (Object)adaptor.nil();
                        root_3 = (Object)adaptor.becomeRoot(
                        stream_DD.nextNode()
                        , root_3);

                        adaptor.addChild(root_3, 
                        stream_INT_CONST.nextNode()
                        );

                        adaptor.addChild(root_3, 
                        stream_INT_CONST.nextNode()
                        );

                        adaptor.addChild(root_2, root_3);
                        }

                        adaptor.addChild(root_1, root_2);
                        }

                        adaptor.addChild(root_0, root_1);
                        }

                    }


                    retval.tree = root_0;
                    }

                    }
                    break;
                case 9 :
                    // parser/flatzinc/FlatzincFullExtParser.g:254:9: ARRAY LS index_set ( CM index_set )* RS OF SET OF LB INT_CONST ( CM INT_CONST )* RB
                    {
                    ARRAY217=(Token)match(input,ARRAY,FOLLOW_ARRAY_in_par_pred_param_type1800); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ARRAY.add(ARRAY217);


                    LS218=(Token)match(input,LS,FOLLOW_LS_in_par_pred_param_type1802); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LS.add(LS218);


                    pushFollow(FOLLOW_index_set_in_par_pred_param_type1804);
                    index_set219=index_set();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_index_set.add(index_set219.getTree());

                    // parser/flatzinc/FlatzincFullExtParser.g:254:28: ( CM index_set )*
                    loop43:
                    do {
                        int alt43=2;
                        switch ( input.LA(1) ) {
                        case CM:
                            {
                            alt43=1;
                            }
                            break;

                        }

                        switch (alt43) {
                    	case 1 :
                    	    // parser/flatzinc/FlatzincFullExtParser.g:254:29: CM index_set
                    	    {
                    	    CM220=(Token)match(input,CM,FOLLOW_CM_in_par_pred_param_type1807); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_CM.add(CM220);


                    	    pushFollow(FOLLOW_index_set_in_par_pred_param_type1809);
                    	    index_set221=index_set();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_index_set.add(index_set221.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop43;
                        }
                    } while (true);


                    RS222=(Token)match(input,RS,FOLLOW_RS_in_par_pred_param_type1813); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RS.add(RS222);


                    OF223=(Token)match(input,OF,FOLLOW_OF_in_par_pred_param_type1815); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_OF.add(OF223);


                    SET224=(Token)match(input,SET,FOLLOW_SET_in_par_pred_param_type1817); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_SET.add(SET224);


                    OF225=(Token)match(input,OF,FOLLOW_OF_in_par_pred_param_type1819); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_OF.add(OF225);


                    LB226=(Token)match(input,LB,FOLLOW_LB_in_par_pred_param_type1821); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LB.add(LB226);


                    INT_CONST227=(Token)match(input,INT_CONST,FOLLOW_INT_CONST_in_par_pred_param_type1823); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_INT_CONST.add(INT_CONST227);


                    // parser/flatzinc/FlatzincFullExtParser.g:254:70: ( CM INT_CONST )*
                    loop44:
                    do {
                        int alt44=2;
                        switch ( input.LA(1) ) {
                        case CM:
                            {
                            alt44=1;
                            }
                            break;

                        }

                        switch (alt44) {
                    	case 1 :
                    	    // parser/flatzinc/FlatzincFullExtParser.g:254:71: CM INT_CONST
                    	    {
                    	    CM228=(Token)match(input,CM,FOLLOW_CM_in_par_pred_param_type1826); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_CM.add(CM228);


                    	    INT_CONST229=(Token)match(input,INT_CONST,FOLLOW_INT_CONST_in_par_pred_param_type1828); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_INT_CONST.add(INT_CONST229);


                    	    }
                    	    break;

                    	default :
                    	    break loop44;
                        }
                    } while (true);


                    RB230=(Token)match(input,RB,FOLLOW_RB_in_par_pred_param_type1832); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RB.add(RB230);


                    // AST REWRITE
                    // elements: INT_CONST, SET, CM, ARRAY, index_set
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {

                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 255:5: -> ^( ARRAY ( index_set )+ ^( SET ^( CM ( INT_CONST )+ ) ) )
                    {
                        // parser/flatzinc/FlatzincFullExtParser.g:255:9: ^( ARRAY ( index_set )+ ^( SET ^( CM ( INT_CONST )+ ) ) )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot(
                        stream_ARRAY.nextNode()
                        , root_1);

                        if ( !(stream_index_set.hasNext()) ) {
                            throw new RewriteEarlyExitException();
                        }
                        while ( stream_index_set.hasNext() ) {
                            adaptor.addChild(root_1, stream_index_set.nextTree());

                        }
                        stream_index_set.reset();

                        // parser/flatzinc/FlatzincFullExtParser.g:255:28: ^( SET ^( CM ( INT_CONST )+ ) )
                        {
                        Object root_2 = (Object)adaptor.nil();
                        root_2 = (Object)adaptor.becomeRoot(
                        stream_SET.nextNode()
                        , root_2);

                        // parser/flatzinc/FlatzincFullExtParser.g:255:34: ^( CM ( INT_CONST )+ )
                        {
                        Object root_3 = (Object)adaptor.nil();
                        root_3 = (Object)adaptor.becomeRoot(
                        stream_CM.nextNode()
                        , root_3);

                        if ( !(stream_INT_CONST.hasNext()) ) {
                            throw new RewriteEarlyExitException();
                        }
                        while ( stream_INT_CONST.hasNext() ) {
                            adaptor.addChild(root_3, 
                            stream_INT_CONST.nextNode()
                            );

                        }
                        stream_INT_CONST.reset();

                        adaptor.addChild(root_2, root_3);
                        }

                        adaptor.addChild(root_1, root_2);
                        }

                        adaptor.addChild(root_0, root_1);
                        }

                    }


                    retval.tree = root_0;
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);


            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "par_pred_param_type"


    public static class var_pred_param_type_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "var_pred_param_type"
    // parser/flatzinc/FlatzincFullExtParser.g:259:1: var_pred_param_type : ( var_type -> ^( VAR var_type ) | VAR SET OF INT -> ^( VAR SET ) | ARRAY LS index_set ( CM index_set )* RS OF VAR SET OF INT -> ^( ARRAY ( index_set )+ ^( VAR SET ) ) );
    public final FlatzincFullExtParser.var_pred_param_type_return var_pred_param_type() throws RecognitionException {
        FlatzincFullExtParser.var_pred_param_type_return retval = new FlatzincFullExtParser.var_pred_param_type_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token VAR232=null;
        Token SET233=null;
        Token OF234=null;
        Token INT235=null;
        Token ARRAY236=null;
        Token LS237=null;
        Token CM239=null;
        Token RS241=null;
        Token OF242=null;
        Token VAR243=null;
        Token SET244=null;
        Token OF245=null;
        Token INT246=null;
        FlatzincFullExtParser.var_type_return var_type231 =null;

        FlatzincFullExtParser.index_set_return index_set238 =null;

        FlatzincFullExtParser.index_set_return index_set240 =null;


        Object VAR232_tree=null;
        Object SET233_tree=null;
        Object OF234_tree=null;
        Object INT235_tree=null;
        Object ARRAY236_tree=null;
        Object LS237_tree=null;
        Object CM239_tree=null;
        Object RS241_tree=null;
        Object OF242_tree=null;
        Object VAR243_tree=null;
        Object SET244_tree=null;
        Object OF245_tree=null;
        Object INT246_tree=null;
        RewriteRuleTokenStream stream_RS=new RewriteRuleTokenStream(adaptor,"token RS");
        RewriteRuleTokenStream stream_SET=new RewriteRuleTokenStream(adaptor,"token SET");
        RewriteRuleTokenStream stream_VAR=new RewriteRuleTokenStream(adaptor,"token VAR");
        RewriteRuleTokenStream stream_INT=new RewriteRuleTokenStream(adaptor,"token INT");
        RewriteRuleTokenStream stream_OF=new RewriteRuleTokenStream(adaptor,"token OF");
        RewriteRuleTokenStream stream_ARRAY=new RewriteRuleTokenStream(adaptor,"token ARRAY");
        RewriteRuleTokenStream stream_LS=new RewriteRuleTokenStream(adaptor,"token LS");
        RewriteRuleTokenStream stream_CM=new RewriteRuleTokenStream(adaptor,"token CM");
        RewriteRuleSubtreeStream stream_index_set=new RewriteRuleSubtreeStream(adaptor,"rule index_set");
        RewriteRuleSubtreeStream stream_var_type=new RewriteRuleSubtreeStream(adaptor,"rule var_type");
        try {
            // parser/flatzinc/FlatzincFullExtParser.g:260:5: ( var_type -> ^( VAR var_type ) | VAR SET OF INT -> ^( VAR SET ) | ARRAY LS index_set ( CM index_set )* RS OF VAR SET OF INT -> ^( ARRAY ( index_set )+ ^( VAR SET ) ) )
            int alt47=3;
            alt47 = dfa47.predict(input);
            switch (alt47) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtParser.g:260:9: var_type
                    {
                    pushFollow(FOLLOW_var_type_in_var_pred_param_type1877);
                    var_type231=var_type();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_var_type.add(var_type231.getTree());

                    // AST REWRITE
                    // elements: var_type
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {

                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 261:5: -> ^( VAR var_type )
                    {
                        // parser/flatzinc/FlatzincFullExtParser.g:261:9: ^( VAR var_type )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot(
                        (Object)adaptor.create(VAR, "VAR")
                        , root_1);

                        adaptor.addChild(root_1, stream_var_type.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }


                    retval.tree = root_0;
                    }

                    }
                    break;
                case 2 :
                    // parser/flatzinc/FlatzincFullExtParser.g:262:9: VAR SET OF INT
                    {
                    VAR232=(Token)match(input,VAR,FOLLOW_VAR_in_var_pred_param_type1900); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_VAR.add(VAR232);


                    SET233=(Token)match(input,SET,FOLLOW_SET_in_var_pred_param_type1902); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_SET.add(SET233);


                    OF234=(Token)match(input,OF,FOLLOW_OF_in_var_pred_param_type1904); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_OF.add(OF234);


                    INT235=(Token)match(input,INT,FOLLOW_INT_in_var_pred_param_type1906); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_INT.add(INT235);


                    // AST REWRITE
                    // elements: VAR, SET
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {

                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 263:5: -> ^( VAR SET )
                    {
                        // parser/flatzinc/FlatzincFullExtParser.g:263:9: ^( VAR SET )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot(
                        stream_VAR.nextNode()
                        , root_1);

                        adaptor.addChild(root_1, 
                        stream_SET.nextNode()
                        );

                        adaptor.addChild(root_0, root_1);
                        }

                    }


                    retval.tree = root_0;
                    }

                    }
                    break;
                case 3 :
                    // parser/flatzinc/FlatzincFullExtParser.g:264:9: ARRAY LS index_set ( CM index_set )* RS OF VAR SET OF INT
                    {
                    ARRAY236=(Token)match(input,ARRAY,FOLLOW_ARRAY_in_var_pred_param_type1929); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ARRAY.add(ARRAY236);


                    LS237=(Token)match(input,LS,FOLLOW_LS_in_var_pred_param_type1931); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LS.add(LS237);


                    pushFollow(FOLLOW_index_set_in_var_pred_param_type1933);
                    index_set238=index_set();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_index_set.add(index_set238.getTree());

                    // parser/flatzinc/FlatzincFullExtParser.g:264:28: ( CM index_set )*
                    loop46:
                    do {
                        int alt46=2;
                        switch ( input.LA(1) ) {
                        case CM:
                            {
                            alt46=1;
                            }
                            break;

                        }

                        switch (alt46) {
                    	case 1 :
                    	    // parser/flatzinc/FlatzincFullExtParser.g:264:29: CM index_set
                    	    {
                    	    CM239=(Token)match(input,CM,FOLLOW_CM_in_var_pred_param_type1936); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_CM.add(CM239);


                    	    pushFollow(FOLLOW_index_set_in_var_pred_param_type1938);
                    	    index_set240=index_set();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_index_set.add(index_set240.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop46;
                        }
                    } while (true);


                    RS241=(Token)match(input,RS,FOLLOW_RS_in_var_pred_param_type1942); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RS.add(RS241);


                    OF242=(Token)match(input,OF,FOLLOW_OF_in_var_pred_param_type1944); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_OF.add(OF242);


                    VAR243=(Token)match(input,VAR,FOLLOW_VAR_in_var_pred_param_type1946); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_VAR.add(VAR243);


                    SET244=(Token)match(input,SET,FOLLOW_SET_in_var_pred_param_type1948); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_SET.add(SET244);


                    OF245=(Token)match(input,OF,FOLLOW_OF_in_var_pred_param_type1950); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_OF.add(OF245);


                    INT246=(Token)match(input,INT,FOLLOW_INT_in_var_pred_param_type1952); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_INT.add(INT246);


                    // AST REWRITE
                    // elements: VAR, ARRAY, index_set, SET
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {

                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 265:5: -> ^( ARRAY ( index_set )+ ^( VAR SET ) )
                    {
                        // parser/flatzinc/FlatzincFullExtParser.g:265:9: ^( ARRAY ( index_set )+ ^( VAR SET ) )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot(
                        stream_ARRAY.nextNode()
                        , root_1);

                        if ( !(stream_index_set.hasNext()) ) {
                            throw new RewriteEarlyExitException();
                        }
                        while ( stream_index_set.hasNext() ) {
                            adaptor.addChild(root_1, stream_index_set.nextTree());

                        }
                        stream_index_set.reset();

                        // parser/flatzinc/FlatzincFullExtParser.g:265:28: ^( VAR SET )
                        {
                        Object root_2 = (Object)adaptor.nil();
                        root_2 = (Object)adaptor.becomeRoot(
                        stream_VAR.nextNode()
                        , root_2);

                        adaptor.addChild(root_2, 
                        stream_SET.nextNode()
                        );

                        adaptor.addChild(root_1, root_2);
                        }

                        adaptor.addChild(root_0, root_1);
                        }

                    }


                    retval.tree = root_0;
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);


            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "var_pred_param_type"


    public static class index_set_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "index_set"
    // parser/flatzinc/FlatzincFullExtParser.g:268:1: index_set : ( INT_CONST DD INT_CONST -> ^( INDEX ^( DD INT_CONST INT_CONST ) ) | INT -> ^( INDEX INT ) );
    public final FlatzincFullExtParser.index_set_return index_set() throws RecognitionException {
        FlatzincFullExtParser.index_set_return retval = new FlatzincFullExtParser.index_set_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token INT_CONST247=null;
        Token DD248=null;
        Token INT_CONST249=null;
        Token INT250=null;

        Object INT_CONST247_tree=null;
        Object DD248_tree=null;
        Object INT_CONST249_tree=null;
        Object INT250_tree=null;
        RewriteRuleTokenStream stream_INT_CONST=new RewriteRuleTokenStream(adaptor,"token INT_CONST");
        RewriteRuleTokenStream stream_INT=new RewriteRuleTokenStream(adaptor,"token INT");
        RewriteRuleTokenStream stream_DD=new RewriteRuleTokenStream(adaptor,"token DD");

        try {
            // parser/flatzinc/FlatzincFullExtParser.g:269:5: ( INT_CONST DD INT_CONST -> ^( INDEX ^( DD INT_CONST INT_CONST ) ) | INT -> ^( INDEX INT ) )
            int alt48=2;
            switch ( input.LA(1) ) {
            case INT_CONST:
                {
                alt48=1;
                }
                break;
            case INT:
                {
                alt48=2;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 48, 0, input);

                throw nvae;

            }

            switch (alt48) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtParser.g:269:9: INT_CONST DD INT_CONST
                    {
                    INT_CONST247=(Token)match(input,INT_CONST,FOLLOW_INT_CONST_in_index_set1991); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_INT_CONST.add(INT_CONST247);


                    DD248=(Token)match(input,DD,FOLLOW_DD_in_index_set1993); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_DD.add(DD248);


                    INT_CONST249=(Token)match(input,INT_CONST,FOLLOW_INT_CONST_in_index_set1995); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_INT_CONST.add(INT_CONST249);


                    // AST REWRITE
                    // elements: INT_CONST, DD, INT_CONST
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {

                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 270:5: -> ^( INDEX ^( DD INT_CONST INT_CONST ) )
                    {
                        // parser/flatzinc/FlatzincFullExtParser.g:270:9: ^( INDEX ^( DD INT_CONST INT_CONST ) )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot(
                        (Object)adaptor.create(INDEX, "INDEX")
                        , root_1);

                        // parser/flatzinc/FlatzincFullExtParser.g:270:17: ^( DD INT_CONST INT_CONST )
                        {
                        Object root_2 = (Object)adaptor.nil();
                        root_2 = (Object)adaptor.becomeRoot(
                        stream_DD.nextNode()
                        , root_2);

                        adaptor.addChild(root_2, 
                        stream_INT_CONST.nextNode()
                        );

                        adaptor.addChild(root_2, 
                        stream_INT_CONST.nextNode()
                        );

                        adaptor.addChild(root_1, root_2);
                        }

                        adaptor.addChild(root_0, root_1);
                        }

                    }


                    retval.tree = root_0;
                    }

                    }
                    break;
                case 2 :
                    // parser/flatzinc/FlatzincFullExtParser.g:271:9: INT
                    {
                    INT250=(Token)match(input,INT,FOLLOW_INT_in_index_set2024); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_INT.add(INT250);


                    // AST REWRITE
                    // elements: INT
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {

                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 272:5: -> ^( INDEX INT )
                    {
                        // parser/flatzinc/FlatzincFullExtParser.g:272:9: ^( INDEX INT )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot(
                        (Object)adaptor.create(INDEX, "INDEX")
                        , root_1);

                        adaptor.addChild(root_1, 
                        stream_INT.nextNode()
                        );

                        adaptor.addChild(root_0, root_1);
                        }

                    }


                    retval.tree = root_0;
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);


            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "index_set"


    public static class expr_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "expr"
    // parser/flatzinc/FlatzincFullExtParser.g:275:1: expr : ( LB INT_CONST ( CM INT_CONST )* RB -> LB ( INT_CONST )+ RB | bool_const | INT_CONST ( DD INT_CONST )? | LS ( expr ( CM expr )* )? RS -> ^( EXPR LS ( expr )* RS ) | id_expr | STRING );
    public final FlatzincFullExtParser.expr_return expr() throws RecognitionException {
        FlatzincFullExtParser.expr_return retval = new FlatzincFullExtParser.expr_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token LB251=null;
        Token INT_CONST252=null;
        Token CM253=null;
        Token INT_CONST254=null;
        Token RB255=null;
        Token INT_CONST257=null;
        Token DD258=null;
        Token INT_CONST259=null;
        Token LS260=null;
        Token CM262=null;
        Token RS264=null;
        Token STRING266=null;
        FlatzincFullExtParser.bool_const_return bool_const256 =null;

        FlatzincFullExtParser.expr_return expr261 =null;

        FlatzincFullExtParser.expr_return expr263 =null;

        FlatzincFullExtParser.id_expr_return id_expr265 =null;


        Object LB251_tree=null;
        Object INT_CONST252_tree=null;
        Object CM253_tree=null;
        Object INT_CONST254_tree=null;
        Object RB255_tree=null;
        Object INT_CONST257_tree=null;
        Object DD258_tree=null;
        Object INT_CONST259_tree=null;
        Object LS260_tree=null;
        Object CM262_tree=null;
        Object RS264_tree=null;
        Object STRING266_tree=null;
        RewriteRuleTokenStream stream_RS=new RewriteRuleTokenStream(adaptor,"token RS");
        RewriteRuleTokenStream stream_INT_CONST=new RewriteRuleTokenStream(adaptor,"token INT_CONST");
        RewriteRuleTokenStream stream_LB=new RewriteRuleTokenStream(adaptor,"token LB");
        RewriteRuleTokenStream stream_RB=new RewriteRuleTokenStream(adaptor,"token RB");
        RewriteRuleTokenStream stream_LS=new RewriteRuleTokenStream(adaptor,"token LS");
        RewriteRuleTokenStream stream_CM=new RewriteRuleTokenStream(adaptor,"token CM");
        RewriteRuleSubtreeStream stream_expr=new RewriteRuleSubtreeStream(adaptor,"rule expr");
        try {
            // parser/flatzinc/FlatzincFullExtParser.g:276:5: ( LB INT_CONST ( CM INT_CONST )* RB -> LB ( INT_CONST )+ RB | bool_const | INT_CONST ( DD INT_CONST )? | LS ( expr ( CM expr )* )? RS -> ^( EXPR LS ( expr )* RS ) | id_expr | STRING )
            int alt53=6;
            switch ( input.LA(1) ) {
            case LB:
                {
                alt53=1;
                }
                break;
            case FALSE:
            case TRUE:
                {
                alt53=2;
                }
                break;
            case INT_CONST:
                {
                alt53=3;
                }
                break;
            case LS:
                {
                alt53=4;
                }
                break;
            case IDENTIFIER:
                {
                alt53=5;
                }
                break;
            case STRING:
                {
                alt53=6;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 53, 0, input);

                throw nvae;

            }

            switch (alt53) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtParser.g:276:9: LB INT_CONST ( CM INT_CONST )* RB
                    {
                    LB251=(Token)match(input,LB,FOLLOW_LB_in_expr2056); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LB.add(LB251);


                    INT_CONST252=(Token)match(input,INT_CONST,FOLLOW_INT_CONST_in_expr2058); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_INT_CONST.add(INT_CONST252);


                    // parser/flatzinc/FlatzincFullExtParser.g:276:22: ( CM INT_CONST )*
                    loop49:
                    do {
                        int alt49=2;
                        switch ( input.LA(1) ) {
                        case CM:
                            {
                            alt49=1;
                            }
                            break;

                        }

                        switch (alt49) {
                    	case 1 :
                    	    // parser/flatzinc/FlatzincFullExtParser.g:276:23: CM INT_CONST
                    	    {
                    	    CM253=(Token)match(input,CM,FOLLOW_CM_in_expr2061); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_CM.add(CM253);


                    	    INT_CONST254=(Token)match(input,INT_CONST,FOLLOW_INT_CONST_in_expr2063); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_INT_CONST.add(INT_CONST254);


                    	    }
                    	    break;

                    	default :
                    	    break loop49;
                        }
                    } while (true);


                    RB255=(Token)match(input,RB,FOLLOW_RB_in_expr2067); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RB.add(RB255);


                    // AST REWRITE
                    // elements: RB, INT_CONST, LB
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {

                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 277:5: -> LB ( INT_CONST )+ RB
                    {
                        adaptor.addChild(root_0, 
                        stream_LB.nextNode()
                        );

                        if ( !(stream_INT_CONST.hasNext()) ) {
                            throw new RewriteEarlyExitException();
                        }
                        while ( stream_INT_CONST.hasNext() ) {
                            adaptor.addChild(root_0, 
                            stream_INT_CONST.nextNode()
                            );

                        }
                        stream_INT_CONST.reset();

                        adaptor.addChild(root_0, 
                        stream_RB.nextNode()
                        );

                    }


                    retval.tree = root_0;
                    }

                    }
                    break;
                case 2 :
                    // parser/flatzinc/FlatzincFullExtParser.g:278:9: bool_const
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_bool_const_in_expr2091);
                    bool_const256=bool_const();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, bool_const256.getTree());

                    }
                    break;
                case 3 :
                    // parser/flatzinc/FlatzincFullExtParser.g:279:9: INT_CONST ( DD INT_CONST )?
                    {
                    root_0 = (Object)adaptor.nil();


                    INT_CONST257=(Token)match(input,INT_CONST,FOLLOW_INT_CONST_in_expr2101); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    INT_CONST257_tree = 
                    (Object)adaptor.create(INT_CONST257)
                    ;
                    adaptor.addChild(root_0, INT_CONST257_tree);
                    }

                    // parser/flatzinc/FlatzincFullExtParser.g:279:19: ( DD INT_CONST )?
                    int alt50=2;
                    switch ( input.LA(1) ) {
                        case DD:
                            {
                            alt50=1;
                            }
                            break;
                    }

                    switch (alt50) {
                        case 1 :
                            // parser/flatzinc/FlatzincFullExtParser.g:279:20: DD INT_CONST
                            {
                            DD258=(Token)match(input,DD,FOLLOW_DD_in_expr2104); if (state.failed) return retval;
                            if ( state.backtracking==0 ) {
                            DD258_tree = 
                            (Object)adaptor.create(DD258)
                            ;
                            adaptor.addChild(root_0, DD258_tree);
                            }

                            INT_CONST259=(Token)match(input,INT_CONST,FOLLOW_INT_CONST_in_expr2106); if (state.failed) return retval;
                            if ( state.backtracking==0 ) {
                            INT_CONST259_tree = 
                            (Object)adaptor.create(INT_CONST259)
                            ;
                            adaptor.addChild(root_0, INT_CONST259_tree);
                            }

                            }
                            break;

                    }


                    }
                    break;
                case 4 :
                    // parser/flatzinc/FlatzincFullExtParser.g:280:9: LS ( expr ( CM expr )* )? RS
                    {
                    LS260=(Token)match(input,LS,FOLLOW_LS_in_expr2118); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LS.add(LS260);


                    // parser/flatzinc/FlatzincFullExtParser.g:280:12: ( expr ( CM expr )* )?
                    int alt52=2;
                    switch ( input.LA(1) ) {
                        case FALSE:
                        case IDENTIFIER:
                        case INT_CONST:
                        case LB:
                        case LS:
                        case STRING:
                        case TRUE:
                            {
                            alt52=1;
                            }
                            break;
                    }

                    switch (alt52) {
                        case 1 :
                            // parser/flatzinc/FlatzincFullExtParser.g:280:13: expr ( CM expr )*
                            {
                            pushFollow(FOLLOW_expr_in_expr2121);
                            expr261=expr();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_expr.add(expr261.getTree());

                            // parser/flatzinc/FlatzincFullExtParser.g:280:18: ( CM expr )*
                            loop51:
                            do {
                                int alt51=2;
                                switch ( input.LA(1) ) {
                                case CM:
                                    {
                                    alt51=1;
                                    }
                                    break;

                                }

                                switch (alt51) {
                            	case 1 :
                            	    // parser/flatzinc/FlatzincFullExtParser.g:280:19: CM expr
                            	    {
                            	    CM262=(Token)match(input,CM,FOLLOW_CM_in_expr2124); if (state.failed) return retval; 
                            	    if ( state.backtracking==0 ) stream_CM.add(CM262);


                            	    pushFollow(FOLLOW_expr_in_expr2126);
                            	    expr263=expr();

                            	    state._fsp--;
                            	    if (state.failed) return retval;
                            	    if ( state.backtracking==0 ) stream_expr.add(expr263.getTree());

                            	    }
                            	    break;

                            	default :
                            	    break loop51;
                                }
                            } while (true);


                            }
                            break;

                    }


                    RS264=(Token)match(input,RS,FOLLOW_RS_in_expr2132); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RS.add(RS264);


                    // AST REWRITE
                    // elements: LS, expr, RS
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {

                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 281:5: -> ^( EXPR LS ( expr )* RS )
                    {
                        // parser/flatzinc/FlatzincFullExtParser.g:281:9: ^( EXPR LS ( expr )* RS )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot(
                        (Object)adaptor.create(EXPR, "EXPR")
                        , root_1);

                        adaptor.addChild(root_1, 
                        stream_LS.nextNode()
                        );

                        // parser/flatzinc/FlatzincFullExtParser.g:281:19: ( expr )*
                        while ( stream_expr.hasNext() ) {
                            adaptor.addChild(root_1, stream_expr.nextTree());

                        }
                        stream_expr.reset();

                        adaptor.addChild(root_1, 
                        stream_RS.nextNode()
                        );

                        adaptor.addChild(root_0, root_1);
                        }

                    }


                    retval.tree = root_0;
                    }

                    }
                    break;
                case 5 :
                    // parser/flatzinc/FlatzincFullExtParser.g:282:9: id_expr
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_id_expr_in_expr2160);
                    id_expr265=id_expr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, id_expr265.getTree());

                    }
                    break;
                case 6 :
                    // parser/flatzinc/FlatzincFullExtParser.g:283:9: STRING
                    {
                    root_0 = (Object)adaptor.nil();


                    STRING266=(Token)match(input,STRING,FOLLOW_STRING_in_expr2170); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    STRING266_tree = 
                    (Object)adaptor.create(STRING266)
                    ;
                    adaptor.addChild(root_0, STRING266_tree);
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);


            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "expr"


    public static class id_expr_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "id_expr"
    // parser/flatzinc/FlatzincFullExtParser.g:287:1: id_expr : IDENTIFIER ( ( LP expr ( CM expr )* RP ) | ( LS INT_CONST RS ) )? ;
    public final FlatzincFullExtParser.id_expr_return id_expr() throws RecognitionException {
        FlatzincFullExtParser.id_expr_return retval = new FlatzincFullExtParser.id_expr_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token IDENTIFIER267=null;
        Token LP268=null;
        Token CM270=null;
        Token RP272=null;
        Token LS273=null;
        Token INT_CONST274=null;
        Token RS275=null;
        FlatzincFullExtParser.expr_return expr269 =null;

        FlatzincFullExtParser.expr_return expr271 =null;


        Object IDENTIFIER267_tree=null;
        Object LP268_tree=null;
        Object CM270_tree=null;
        Object RP272_tree=null;
        Object LS273_tree=null;
        Object INT_CONST274_tree=null;
        Object RS275_tree=null;

        try {
            // parser/flatzinc/FlatzincFullExtParser.g:289:5: ( IDENTIFIER ( ( LP expr ( CM expr )* RP ) | ( LS INT_CONST RS ) )? )
            // parser/flatzinc/FlatzincFullExtParser.g:289:9: IDENTIFIER ( ( LP expr ( CM expr )* RP ) | ( LS INT_CONST RS ) )?
            {
            root_0 = (Object)adaptor.nil();


            IDENTIFIER267=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_id_expr2191); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            IDENTIFIER267_tree = 
            (Object)adaptor.create(IDENTIFIER267)
            ;
            adaptor.addChild(root_0, IDENTIFIER267_tree);
            }

            // parser/flatzinc/FlatzincFullExtParser.g:289:20: ( ( LP expr ( CM expr )* RP ) | ( LS INT_CONST RS ) )?
            int alt55=3;
            switch ( input.LA(1) ) {
                case LP:
                    {
                    alt55=1;
                    }
                    break;
                case LS:
                    {
                    alt55=2;
                    }
                    break;
            }

            switch (alt55) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtParser.g:289:21: ( LP expr ( CM expr )* RP )
                    {
                    // parser/flatzinc/FlatzincFullExtParser.g:289:21: ( LP expr ( CM expr )* RP )
                    // parser/flatzinc/FlatzincFullExtParser.g:289:22: LP expr ( CM expr )* RP
                    {
                    LP268=(Token)match(input,LP,FOLLOW_LP_in_id_expr2195); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LP268_tree = 
                    (Object)adaptor.create(LP268)
                    ;
                    adaptor.addChild(root_0, LP268_tree);
                    }

                    pushFollow(FOLLOW_expr_in_id_expr2197);
                    expr269=expr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, expr269.getTree());

                    // parser/flatzinc/FlatzincFullExtParser.g:289:30: ( CM expr )*
                    loop54:
                    do {
                        int alt54=2;
                        switch ( input.LA(1) ) {
                        case CM:
                            {
                            alt54=1;
                            }
                            break;

                        }

                        switch (alt54) {
                    	case 1 :
                    	    // parser/flatzinc/FlatzincFullExtParser.g:289:31: CM expr
                    	    {
                    	    CM270=(Token)match(input,CM,FOLLOW_CM_in_id_expr2200); if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) {
                    	    CM270_tree = 
                    	    (Object)adaptor.create(CM270)
                    	    ;
                    	    adaptor.addChild(root_0, CM270_tree);
                    	    }

                    	    pushFollow(FOLLOW_expr_in_id_expr2202);
                    	    expr271=expr();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) adaptor.addChild(root_0, expr271.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop54;
                        }
                    } while (true);


                    RP272=(Token)match(input,RP,FOLLOW_RP_in_id_expr2206); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    RP272_tree = 
                    (Object)adaptor.create(RP272)
                    ;
                    adaptor.addChild(root_0, RP272_tree);
                    }

                    }


                    }
                    break;
                case 2 :
                    // parser/flatzinc/FlatzincFullExtParser.g:289:45: ( LS INT_CONST RS )
                    {
                    // parser/flatzinc/FlatzincFullExtParser.g:289:45: ( LS INT_CONST RS )
                    // parser/flatzinc/FlatzincFullExtParser.g:289:46: LS INT_CONST RS
                    {
                    LS273=(Token)match(input,LS,FOLLOW_LS_in_id_expr2210); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LS273_tree = 
                    (Object)adaptor.create(LS273)
                    ;
                    adaptor.addChild(root_0, LS273_tree);
                    }

                    INT_CONST274=(Token)match(input,INT_CONST,FOLLOW_INT_CONST_in_id_expr2212); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    INT_CONST274_tree = 
                    (Object)adaptor.create(INT_CONST274)
                    ;
                    adaptor.addChild(root_0, INT_CONST274_tree);
                    }

                    RS275=(Token)match(input,RS,FOLLOW_RS_in_id_expr2214); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    RS275_tree = 
                    (Object)adaptor.create(RS275)
                    ;
                    adaptor.addChild(root_0, RS275_tree);
                    }

                    }


                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);


            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "id_expr"


    public static class param_decl_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "param_decl"
    // parser/flatzinc/FlatzincFullExtParser.g:293:1: param_decl : par_type CL IDENTIFIER EQ expr SC -> ^( PAR IDENTIFIER par_type expr ) ;
    public final FlatzincFullExtParser.param_decl_return param_decl() throws RecognitionException {
        FlatzincFullExtParser.param_decl_return retval = new FlatzincFullExtParser.param_decl_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token CL277=null;
        Token IDENTIFIER278=null;
        Token EQ279=null;
        Token SC281=null;
        FlatzincFullExtParser.par_type_return par_type276 =null;

        FlatzincFullExtParser.expr_return expr280 =null;


        Object CL277_tree=null;
        Object IDENTIFIER278_tree=null;
        Object EQ279_tree=null;
        Object SC281_tree=null;
        RewriteRuleTokenStream stream_SC=new RewriteRuleTokenStream(adaptor,"token SC");
        RewriteRuleTokenStream stream_EQ=new RewriteRuleTokenStream(adaptor,"token EQ");
        RewriteRuleTokenStream stream_IDENTIFIER=new RewriteRuleTokenStream(adaptor,"token IDENTIFIER");
        RewriteRuleTokenStream stream_CL=new RewriteRuleTokenStream(adaptor,"token CL");
        RewriteRuleSubtreeStream stream_par_type=new RewriteRuleSubtreeStream(adaptor,"rule par_type");
        RewriteRuleSubtreeStream stream_expr=new RewriteRuleSubtreeStream(adaptor,"rule expr");
        try {
            // parser/flatzinc/FlatzincFullExtParser.g:294:2: ( par_type CL IDENTIFIER EQ expr SC -> ^( PAR IDENTIFIER par_type expr ) )
            // parser/flatzinc/FlatzincFullExtParser.g:294:6: par_type CL IDENTIFIER EQ expr SC
            {
            pushFollow(FOLLOW_par_type_in_param_decl2234);
            par_type276=par_type();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_par_type.add(par_type276.getTree());

            CL277=(Token)match(input,CL,FOLLOW_CL_in_param_decl2236); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_CL.add(CL277);


            IDENTIFIER278=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_param_decl2238); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_IDENTIFIER.add(IDENTIFIER278);


            EQ279=(Token)match(input,EQ,FOLLOW_EQ_in_param_decl2240); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_EQ.add(EQ279);


            pushFollow(FOLLOW_expr_in_param_decl2242);
            expr280=expr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_expr.add(expr280.getTree());

            SC281=(Token)match(input,SC,FOLLOW_SC_in_param_decl2244); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_SC.add(SC281);


            // AST REWRITE
            // elements: par_type, IDENTIFIER, expr
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {

            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 295:2: -> ^( PAR IDENTIFIER par_type expr )
            {
                // parser/flatzinc/FlatzincFullExtParser.g:295:6: ^( PAR IDENTIFIER par_type expr )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot(
                (Object)adaptor.create(PAR, "PAR")
                , root_1);

                adaptor.addChild(root_1, 
                stream_IDENTIFIER.nextNode()
                );

                adaptor.addChild(root_1, stream_par_type.nextTree());

                adaptor.addChild(root_1, stream_expr.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }


            retval.tree = root_0;
            }

            }

            retval.stop = input.LT(-1);


            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "param_decl"


    public static class var_decl_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "var_decl"
    // parser/flatzinc/FlatzincFullExtParser.g:299:1: var_decl : var_type CL IDENTIFIER annotations ( EQ expr )? SC -> ^( VAR IDENTIFIER var_type annotations ( expr )? ) ;
    public final FlatzincFullExtParser.var_decl_return var_decl() throws RecognitionException {
        FlatzincFullExtParser.var_decl_return retval = new FlatzincFullExtParser.var_decl_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token CL283=null;
        Token IDENTIFIER284=null;
        Token EQ286=null;
        Token SC288=null;
        FlatzincFullExtParser.var_type_return var_type282 =null;

        FlatzincFullExtParser.annotations_return annotations285 =null;

        FlatzincFullExtParser.expr_return expr287 =null;


        Object CL283_tree=null;
        Object IDENTIFIER284_tree=null;
        Object EQ286_tree=null;
        Object SC288_tree=null;
        RewriteRuleTokenStream stream_SC=new RewriteRuleTokenStream(adaptor,"token SC");
        RewriteRuleTokenStream stream_EQ=new RewriteRuleTokenStream(adaptor,"token EQ");
        RewriteRuleTokenStream stream_IDENTIFIER=new RewriteRuleTokenStream(adaptor,"token IDENTIFIER");
        RewriteRuleTokenStream stream_CL=new RewriteRuleTokenStream(adaptor,"token CL");
        RewriteRuleSubtreeStream stream_var_type=new RewriteRuleSubtreeStream(adaptor,"rule var_type");
        RewriteRuleSubtreeStream stream_expr=new RewriteRuleSubtreeStream(adaptor,"rule expr");
        RewriteRuleSubtreeStream stream_annotations=new RewriteRuleSubtreeStream(adaptor,"rule annotations");
        try {
            // parser/flatzinc/FlatzincFullExtParser.g:300:2: ( var_type CL IDENTIFIER annotations ( EQ expr )? SC -> ^( VAR IDENTIFIER var_type annotations ( expr )? ) )
            // parser/flatzinc/FlatzincFullExtParser.g:300:6: var_type CL IDENTIFIER annotations ( EQ expr )? SC
            {
            pushFollow(FOLLOW_var_type_in_var_decl2272);
            var_type282=var_type();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_var_type.add(var_type282.getTree());

            CL283=(Token)match(input,CL,FOLLOW_CL_in_var_decl2274); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_CL.add(CL283);


            IDENTIFIER284=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_var_decl2276); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_IDENTIFIER.add(IDENTIFIER284);


            pushFollow(FOLLOW_annotations_in_var_decl2278);
            annotations285=annotations();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_annotations.add(annotations285.getTree());

            // parser/flatzinc/FlatzincFullExtParser.g:300:41: ( EQ expr )?
            int alt56=2;
            switch ( input.LA(1) ) {
                case EQ:
                    {
                    alt56=1;
                    }
                    break;
            }

            switch (alt56) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtParser.g:300:42: EQ expr
                    {
                    EQ286=(Token)match(input,EQ,FOLLOW_EQ_in_var_decl2281); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_EQ.add(EQ286);


                    pushFollow(FOLLOW_expr_in_var_decl2283);
                    expr287=expr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_expr.add(expr287.getTree());

                    }
                    break;

            }


            SC288=(Token)match(input,SC,FOLLOW_SC_in_var_decl2287); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_SC.add(SC288);


            // AST REWRITE
            // elements: IDENTIFIER, annotations, var_type, expr
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {

            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 301:2: -> ^( VAR IDENTIFIER var_type annotations ( expr )? )
            {
                // parser/flatzinc/FlatzincFullExtParser.g:301:6: ^( VAR IDENTIFIER var_type annotations ( expr )? )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot(
                (Object)adaptor.create(VAR, "VAR")
                , root_1);

                adaptor.addChild(root_1, 
                stream_IDENTIFIER.nextNode()
                );

                adaptor.addChild(root_1, stream_var_type.nextTree());

                adaptor.addChild(root_1, stream_annotations.nextTree());

                // parser/flatzinc/FlatzincFullExtParser.g:301:44: ( expr )?
                if ( stream_expr.hasNext() ) {
                    adaptor.addChild(root_1, stream_expr.nextTree());

                }
                stream_expr.reset();

                adaptor.addChild(root_0, root_1);
                }

            }


            retval.tree = root_0;
            }

            }

            retval.stop = input.LT(-1);


            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "var_decl"


    public static class constraint_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "constraint"
    // parser/flatzinc/FlatzincFullExtParser.g:304:1: constraint : CONSTRAINT IDENTIFIER LP expr ( CM expr )* RP annotations SC -> ^( CONSTRAINT IDENTIFIER ( expr )+ annotations ) ;
    public final FlatzincFullExtParser.constraint_return constraint() throws RecognitionException {
        FlatzincFullExtParser.constraint_return retval = new FlatzincFullExtParser.constraint_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token CONSTRAINT289=null;
        Token IDENTIFIER290=null;
        Token LP291=null;
        Token CM293=null;
        Token RP295=null;
        Token SC297=null;
        FlatzincFullExtParser.expr_return expr292 =null;

        FlatzincFullExtParser.expr_return expr294 =null;

        FlatzincFullExtParser.annotations_return annotations296 =null;


        Object CONSTRAINT289_tree=null;
        Object IDENTIFIER290_tree=null;
        Object LP291_tree=null;
        Object CM293_tree=null;
        Object RP295_tree=null;
        Object SC297_tree=null;
        RewriteRuleTokenStream stream_SC=new RewriteRuleTokenStream(adaptor,"token SC");
        RewriteRuleTokenStream stream_RP=new RewriteRuleTokenStream(adaptor,"token RP");
        RewriteRuleTokenStream stream_CONSTRAINT=new RewriteRuleTokenStream(adaptor,"token CONSTRAINT");
        RewriteRuleTokenStream stream_IDENTIFIER=new RewriteRuleTokenStream(adaptor,"token IDENTIFIER");
        RewriteRuleTokenStream stream_CM=new RewriteRuleTokenStream(adaptor,"token CM");
        RewriteRuleTokenStream stream_LP=new RewriteRuleTokenStream(adaptor,"token LP");
        RewriteRuleSubtreeStream stream_expr=new RewriteRuleSubtreeStream(adaptor,"rule expr");
        RewriteRuleSubtreeStream stream_annotations=new RewriteRuleSubtreeStream(adaptor,"rule annotations");
        try {
            // parser/flatzinc/FlatzincFullExtParser.g:305:2: ( CONSTRAINT IDENTIFIER LP expr ( CM expr )* RP annotations SC -> ^( CONSTRAINT IDENTIFIER ( expr )+ annotations ) )
            // parser/flatzinc/FlatzincFullExtParser.g:305:6: CONSTRAINT IDENTIFIER LP expr ( CM expr )* RP annotations SC
            {
            CONSTRAINT289=(Token)match(input,CONSTRAINT,FOLLOW_CONSTRAINT_in_constraint2317); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_CONSTRAINT.add(CONSTRAINT289);


            IDENTIFIER290=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_constraint2319); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_IDENTIFIER.add(IDENTIFIER290);


            LP291=(Token)match(input,LP,FOLLOW_LP_in_constraint2321); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LP.add(LP291);


            pushFollow(FOLLOW_expr_in_constraint2323);
            expr292=expr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_expr.add(expr292.getTree());

            // parser/flatzinc/FlatzincFullExtParser.g:305:36: ( CM expr )*
            loop57:
            do {
                int alt57=2;
                switch ( input.LA(1) ) {
                case CM:
                    {
                    alt57=1;
                    }
                    break;

                }

                switch (alt57) {
            	case 1 :
            	    // parser/flatzinc/FlatzincFullExtParser.g:305:37: CM expr
            	    {
            	    CM293=(Token)match(input,CM,FOLLOW_CM_in_constraint2326); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_CM.add(CM293);


            	    pushFollow(FOLLOW_expr_in_constraint2328);
            	    expr294=expr();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_expr.add(expr294.getTree());

            	    }
            	    break;

            	default :
            	    break loop57;
                }
            } while (true);


            RP295=(Token)match(input,RP,FOLLOW_RP_in_constraint2332); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RP.add(RP295);


            pushFollow(FOLLOW_annotations_in_constraint2334);
            annotations296=annotations();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_annotations.add(annotations296.getTree());

            SC297=(Token)match(input,SC,FOLLOW_SC_in_constraint2336); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_SC.add(SC297);


            // AST REWRITE
            // elements: annotations, expr, CONSTRAINT, IDENTIFIER
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {

            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 306:2: -> ^( CONSTRAINT IDENTIFIER ( expr )+ annotations )
            {
                // parser/flatzinc/FlatzincFullExtParser.g:306:6: ^( CONSTRAINT IDENTIFIER ( expr )+ annotations )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot(
                stream_CONSTRAINT.nextNode()
                , root_1);

                adaptor.addChild(root_1, 
                stream_IDENTIFIER.nextNode()
                );

                if ( !(stream_expr.hasNext()) ) {
                    throw new RewriteEarlyExitException();
                }
                while ( stream_expr.hasNext() ) {
                    adaptor.addChild(root_1, stream_expr.nextTree());

                }
                stream_expr.reset();

                adaptor.addChild(root_1, stream_annotations.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }


            retval.tree = root_0;
            }

            }

            retval.stop = input.LT(-1);


            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "constraint"


    public static class solve_goal_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "solve_goal"
    // parser/flatzinc/FlatzincFullExtParser.g:309:1: solve_goal : SOLVE ^ annotations resolution SC !;
    public final FlatzincFullExtParser.solve_goal_return solve_goal() throws RecognitionException {
        FlatzincFullExtParser.solve_goal_return retval = new FlatzincFullExtParser.solve_goal_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token SOLVE298=null;
        Token SC301=null;
        FlatzincFullExtParser.annotations_return annotations299 =null;

        FlatzincFullExtParser.resolution_return resolution300 =null;


        Object SOLVE298_tree=null;
        Object SC301_tree=null;

        try {
            // parser/flatzinc/FlatzincFullExtParser.g:310:2: ( SOLVE ^ annotations resolution SC !)
            // parser/flatzinc/FlatzincFullExtParser.g:310:6: SOLVE ^ annotations resolution SC !
            {
            root_0 = (Object)adaptor.nil();


            SOLVE298=(Token)match(input,SOLVE,FOLLOW_SOLVE_in_solve_goal2364); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            SOLVE298_tree = 
            (Object)adaptor.create(SOLVE298)
            ;
            root_0 = (Object)adaptor.becomeRoot(SOLVE298_tree, root_0);
            }

            pushFollow(FOLLOW_annotations_in_solve_goal2367);
            annotations299=annotations();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, annotations299.getTree());

            pushFollow(FOLLOW_resolution_in_solve_goal2369);
            resolution300=resolution();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, resolution300.getTree());

            SC301=(Token)match(input,SC,FOLLOW_SC_in_solve_goal2371); if (state.failed) return retval;

            }

            retval.stop = input.LT(-1);


            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "solve_goal"


    public static class resolution_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "resolution"
    // parser/flatzinc/FlatzincFullExtParser.g:313:1: resolution : ( MINIMIZE ^ expr | MAXIMIZE ^ expr | SATISFY ^);
    public final FlatzincFullExtParser.resolution_return resolution() throws RecognitionException {
        FlatzincFullExtParser.resolution_return retval = new FlatzincFullExtParser.resolution_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token MINIMIZE302=null;
        Token MAXIMIZE304=null;
        Token SATISFY306=null;
        FlatzincFullExtParser.expr_return expr303 =null;

        FlatzincFullExtParser.expr_return expr305 =null;


        Object MINIMIZE302_tree=null;
        Object MAXIMIZE304_tree=null;
        Object SATISFY306_tree=null;

        try {
            // parser/flatzinc/FlatzincFullExtParser.g:314:5: ( MINIMIZE ^ expr | MAXIMIZE ^ expr | SATISFY ^)
            int alt58=3;
            switch ( input.LA(1) ) {
            case MINIMIZE:
                {
                alt58=1;
                }
                break;
            case MAXIMIZE:
                {
                alt58=2;
                }
                break;
            case SATISFY:
                {
                alt58=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 58, 0, input);

                throw nvae;

            }

            switch (alt58) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtParser.g:314:9: MINIMIZE ^ expr
                    {
                    root_0 = (Object)adaptor.nil();


                    MINIMIZE302=(Token)match(input,MINIMIZE,FOLLOW_MINIMIZE_in_resolution2388); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    MINIMIZE302_tree = 
                    (Object)adaptor.create(MINIMIZE302)
                    ;
                    root_0 = (Object)adaptor.becomeRoot(MINIMIZE302_tree, root_0);
                    }

                    pushFollow(FOLLOW_expr_in_resolution2391);
                    expr303=expr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, expr303.getTree());

                    }
                    break;
                case 2 :
                    // parser/flatzinc/FlatzincFullExtParser.g:315:9: MAXIMIZE ^ expr
                    {
                    root_0 = (Object)adaptor.nil();


                    MAXIMIZE304=(Token)match(input,MAXIMIZE,FOLLOW_MAXIMIZE_in_resolution2401); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    MAXIMIZE304_tree = 
                    (Object)adaptor.create(MAXIMIZE304)
                    ;
                    root_0 = (Object)adaptor.becomeRoot(MAXIMIZE304_tree, root_0);
                    }

                    pushFollow(FOLLOW_expr_in_resolution2404);
                    expr305=expr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, expr305.getTree());

                    }
                    break;
                case 3 :
                    // parser/flatzinc/FlatzincFullExtParser.g:316:9: SATISFY ^
                    {
                    root_0 = (Object)adaptor.nil();


                    SATISFY306=(Token)match(input,SATISFY,FOLLOW_SATISFY_in_resolution2414); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    SATISFY306_tree = 
                    (Object)adaptor.create(SATISFY306)
                    ;
                    root_0 = (Object)adaptor.becomeRoot(SATISFY306_tree, root_0);
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);


            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "resolution"


    public static class annotations_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "annotations"
    // parser/flatzinc/FlatzincFullExtParser.g:319:1: annotations : ( DC annotation )* -> ^( ANNOTATIONS ( annotation )* ) ;
    public final FlatzincFullExtParser.annotations_return annotations() throws RecognitionException {
        FlatzincFullExtParser.annotations_return retval = new FlatzincFullExtParser.annotations_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token DC307=null;
        FlatzincFullExtParser.annotation_return annotation308 =null;


        Object DC307_tree=null;
        RewriteRuleTokenStream stream_DC=new RewriteRuleTokenStream(adaptor,"token DC");
        RewriteRuleSubtreeStream stream_annotation=new RewriteRuleSubtreeStream(adaptor,"rule annotation");
        try {
            // parser/flatzinc/FlatzincFullExtParser.g:320:5: ( ( DC annotation )* -> ^( ANNOTATIONS ( annotation )* ) )
            // parser/flatzinc/FlatzincFullExtParser.g:320:9: ( DC annotation )*
            {
            // parser/flatzinc/FlatzincFullExtParser.g:320:9: ( DC annotation )*
            loop59:
            do {
                int alt59=2;
                switch ( input.LA(1) ) {
                case DC:
                    {
                    alt59=1;
                    }
                    break;

                }

                switch (alt59) {
            	case 1 :
            	    // parser/flatzinc/FlatzincFullExtParser.g:320:10: DC annotation
            	    {
            	    DC307=(Token)match(input,DC,FOLLOW_DC_in_annotations2435); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_DC.add(DC307);


            	    pushFollow(FOLLOW_annotation_in_annotations2437);
            	    annotation308=annotation();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_annotation.add(annotation308.getTree());

            	    }
            	    break;

            	default :
            	    break loop59;
                }
            } while (true);


            // AST REWRITE
            // elements: annotation
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {

            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 321:5: -> ^( ANNOTATIONS ( annotation )* )
            {
                // parser/flatzinc/FlatzincFullExtParser.g:321:9: ^( ANNOTATIONS ( annotation )* )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot(
                (Object)adaptor.create(ANNOTATIONS, "ANNOTATIONS")
                , root_1);

                // parser/flatzinc/FlatzincFullExtParser.g:321:23: ( annotation )*
                while ( stream_annotation.hasNext() ) {
                    adaptor.addChild(root_1, stream_annotation.nextTree());

                }
                stream_annotation.reset();

                adaptor.addChild(root_0, root_1);
                }

            }


            retval.tree = root_0;
            }

            }

            retval.stop = input.LT(-1);


            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "annotations"


    public static class annotation_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "annotation"
    // parser/flatzinc/FlatzincFullExtParser.g:324:1: annotation : IDENTIFIER ( LP expr ( CM expr )* RP )? -> IDENTIFIER ( LP ( expr )+ RP )? ;
    public final FlatzincFullExtParser.annotation_return annotation() throws RecognitionException {
        FlatzincFullExtParser.annotation_return retval = new FlatzincFullExtParser.annotation_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token IDENTIFIER309=null;
        Token LP310=null;
        Token CM312=null;
        Token RP314=null;
        FlatzincFullExtParser.expr_return expr311 =null;

        FlatzincFullExtParser.expr_return expr313 =null;


        Object IDENTIFIER309_tree=null;
        Object LP310_tree=null;
        Object CM312_tree=null;
        Object RP314_tree=null;
        RewriteRuleTokenStream stream_RP=new RewriteRuleTokenStream(adaptor,"token RP");
        RewriteRuleTokenStream stream_IDENTIFIER=new RewriteRuleTokenStream(adaptor,"token IDENTIFIER");
        RewriteRuleTokenStream stream_CM=new RewriteRuleTokenStream(adaptor,"token CM");
        RewriteRuleTokenStream stream_LP=new RewriteRuleTokenStream(adaptor,"token LP");
        RewriteRuleSubtreeStream stream_expr=new RewriteRuleSubtreeStream(adaptor,"rule expr");
        try {
            // parser/flatzinc/FlatzincFullExtParser.g:325:5: ( IDENTIFIER ( LP expr ( CM expr )* RP )? -> IDENTIFIER ( LP ( expr )+ RP )? )
            // parser/flatzinc/FlatzincFullExtParser.g:325:9: IDENTIFIER ( LP expr ( CM expr )* RP )?
            {
            IDENTIFIER309=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_annotation2472); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_IDENTIFIER.add(IDENTIFIER309);


            // parser/flatzinc/FlatzincFullExtParser.g:325:20: ( LP expr ( CM expr )* RP )?
            int alt61=2;
            switch ( input.LA(1) ) {
                case LP:
                    {
                    alt61=1;
                    }
                    break;
            }

            switch (alt61) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtParser.g:325:21: LP expr ( CM expr )* RP
                    {
                    LP310=(Token)match(input,LP,FOLLOW_LP_in_annotation2475); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LP.add(LP310);


                    pushFollow(FOLLOW_expr_in_annotation2477);
                    expr311=expr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_expr.add(expr311.getTree());

                    // parser/flatzinc/FlatzincFullExtParser.g:325:29: ( CM expr )*
                    loop60:
                    do {
                        int alt60=2;
                        switch ( input.LA(1) ) {
                        case CM:
                            {
                            alt60=1;
                            }
                            break;

                        }

                        switch (alt60) {
                    	case 1 :
                    	    // parser/flatzinc/FlatzincFullExtParser.g:325:30: CM expr
                    	    {
                    	    CM312=(Token)match(input,CM,FOLLOW_CM_in_annotation2480); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_CM.add(CM312);


                    	    pushFollow(FOLLOW_expr_in_annotation2482);
                    	    expr313=expr();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_expr.add(expr313.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop60;
                        }
                    } while (true);


                    RP314=(Token)match(input,RP,FOLLOW_RP_in_annotation2486); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RP.add(RP314);


                    }
                    break;

            }


            // AST REWRITE
            // elements: IDENTIFIER, expr, LP, RP
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {

            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 326:5: -> IDENTIFIER ( LP ( expr )+ RP )?
            {
                adaptor.addChild(root_0, 
                stream_IDENTIFIER.nextNode()
                );

                // parser/flatzinc/FlatzincFullExtParser.g:326:20: ( LP ( expr )+ RP )?
                if ( stream_expr.hasNext()||stream_LP.hasNext()||stream_RP.hasNext() ) {
                    adaptor.addChild(root_0, 
                    stream_LP.nextNode()
                    );

                    if ( !(stream_expr.hasNext()) ) {
                        throw new RewriteEarlyExitException();
                    }
                    while ( stream_expr.hasNext() ) {
                        adaptor.addChild(root_0, stream_expr.nextTree());

                    }
                    stream_expr.reset();

                    adaptor.addChild(root_0, 
                    stream_RP.nextNode()
                    );

                }
                stream_expr.reset();
                stream_LP.reset();
                stream_RP.reset();

            }


            retval.tree = root_0;
            }

            }

            retval.stop = input.LT(-1);


            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "annotation"


    public static class bool_const_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "bool_const"
    // parser/flatzinc/FlatzincFullExtParser.g:330:1: bool_const : ( TRUE ^| FALSE ^);
    public final FlatzincFullExtParser.bool_const_return bool_const() throws RecognitionException {
        FlatzincFullExtParser.bool_const_return retval = new FlatzincFullExtParser.bool_const_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token TRUE315=null;
        Token FALSE316=null;

        Object TRUE315_tree=null;
        Object FALSE316_tree=null;

        try {
            // parser/flatzinc/FlatzincFullExtParser.g:331:5: ( TRUE ^| FALSE ^)
            int alt62=2;
            switch ( input.LA(1) ) {
            case TRUE:
                {
                alt62=1;
                }
                break;
            case FALSE:
                {
                alt62=2;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 62, 0, input);

                throw nvae;

            }

            switch (alt62) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtParser.g:331:9: TRUE ^
                    {
                    root_0 = (Object)adaptor.nil();


                    TRUE315=(Token)match(input,TRUE,FOLLOW_TRUE_in_bool_const2527); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    TRUE315_tree = 
                    (Object)adaptor.create(TRUE315)
                    ;
                    root_0 = (Object)adaptor.becomeRoot(TRUE315_tree, root_0);
                    }

                    }
                    break;
                case 2 :
                    // parser/flatzinc/FlatzincFullExtParser.g:332:9: FALSE ^
                    {
                    root_0 = (Object)adaptor.nil();


                    FALSE316=(Token)match(input,FALSE,FOLLOW_FALSE_in_bool_const2538); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    FALSE316_tree = 
                    (Object)adaptor.create(FALSE316)
                    ;
                    root_0 = (Object)adaptor.becomeRoot(FALSE316_tree, root_0);
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);


            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "bool_const"

    // $ANTLR start synpred9_FlatzincFullExtParser
    public final void synpred9_FlatzincFullExtParser_fragment() throws RecognitionException {
        // parser/flatzinc/FlatzincFullExtParser.g:68:4: ( LP predicates ( AND predicates )+ RP )
        // parser/flatzinc/FlatzincFullExtParser.g:68:4: LP predicates ( AND predicates )+ RP
        {
        match(input,LP,FOLLOW_LP_in_synpred9_FlatzincFullExtParser165); if (state.failed) return ;

        pushFollow(FOLLOW_predicates_in_synpred9_FlatzincFullExtParser167);
        predicates();

        state._fsp--;
        if (state.failed) return ;

        // parser/flatzinc/FlatzincFullExtParser.g:68:18: ( AND predicates )+
        int cnt63=0;
        loop63:
        do {
            int alt63=2;
            switch ( input.LA(1) ) {
            case AND:
                {
                alt63=1;
                }
                break;

            }

            switch (alt63) {
        	case 1 :
        	    // parser/flatzinc/FlatzincFullExtParser.g:68:19: AND predicates
        	    {
        	    match(input,AND,FOLLOW_AND_in_synpred9_FlatzincFullExtParser170); if (state.failed) return ;

        	    pushFollow(FOLLOW_predicates_in_synpred9_FlatzincFullExtParser172);
        	    predicates();

        	    state._fsp--;
        	    if (state.failed) return ;

        	    }
        	    break;

        	default :
        	    if ( cnt63 >= 1 ) break loop63;
        	    if (state.backtracking>0) {state.failed=true; return ;}
                    EarlyExitException eee =
                        new EarlyExitException(63, input);
                    throw eee;
            }
            cnt63++;
        } while (true);


        match(input,RP,FOLLOW_RP_in_synpred9_FlatzincFullExtParser176); if (state.failed) return ;

        }

    }
    // $ANTLR end synpred9_FlatzincFullExtParser

    // $ANTLR start synpred45_FlatzincFullExtParser
    public final void synpred45_FlatzincFullExtParser_fragment() throws RecognitionException {
        // parser/flatzinc/FlatzincFullExtParser.g:161:4: ( attr_op ( DO attr_op )* ( DO attribute )? )
        // parser/flatzinc/FlatzincFullExtParser.g:161:4: attr_op ( DO attr_op )* ( DO attribute )?
        {
        pushFollow(FOLLOW_attr_op_in_synpred45_FlatzincFullExtParser833);
        attr_op();

        state._fsp--;
        if (state.failed) return ;

        // parser/flatzinc/FlatzincFullExtParser.g:161:12: ( DO attr_op )*
        loop66:
        do {
            int alt66=2;
            switch ( input.LA(1) ) {
            case DO:
                {
                switch ( input.LA(2) ) {
                case ANY:
                case MAX:
                case MIN:
                case SIZE:
                case SUM:
                    {
                    alt66=1;
                    }
                    break;

                }

                }
                break;

            }

            switch (alt66) {
        	case 1 :
        	    // parser/flatzinc/FlatzincFullExtParser.g:161:13: DO attr_op
        	    {
        	    match(input,DO,FOLLOW_DO_in_synpred45_FlatzincFullExtParser836); if (state.failed) return ;

        	    pushFollow(FOLLOW_attr_op_in_synpred45_FlatzincFullExtParser838);
        	    attr_op();

        	    state._fsp--;
        	    if (state.failed) return ;

        	    }
        	    break;

        	default :
        	    break loop66;
            }
        } while (true);


        // parser/flatzinc/FlatzincFullExtParser.g:161:27: ( DO attribute )?
        int alt67=2;
        switch ( input.LA(1) ) {
            case DO:
                {
                alt67=1;
                }
                break;
        }

        switch (alt67) {
            case 1 :
                // parser/flatzinc/FlatzincFullExtParser.g:161:28: DO attribute
                {
                match(input,DO,FOLLOW_DO_in_synpred45_FlatzincFullExtParser844); if (state.failed) return ;

                pushFollow(FOLLOW_attribute_in_synpred45_FlatzincFullExtParser846);
                attribute();

                state._fsp--;
                if (state.failed) return ;

                }
                break;

        }


        }

    }
    // $ANTLR end synpred45_FlatzincFullExtParser

    // Delegated rules

    public final boolean synpred9_FlatzincFullExtParser() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred9_FlatzincFullExtParser_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred45_FlatzincFullExtParser() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred45_FlatzincFullExtParser_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }


    protected DFA2 dfa2 = new DFA2(this);
    protected DFA26 dfa26 = new DFA26(this);
    protected DFA28 dfa28 = new DFA28(this);
    protected DFA45 dfa45 = new DFA45(this);
    protected DFA47 dfa47 = new DFA47(this);
    static final String DFA2_eotS =
        "\20\uffff";
    static final String DFA2_eofS =
        "\20\uffff";
    static final String DFA2_minS =
        "\1\10\1\53\2\uffff\1\45\1\26\1\21\1\46\1\45\1\64\1\21\1\26\1\21"+
        "\1\15\1\46\1\21";
    static final String DFA2_maxS =
        "\1\124\1\53\2\uffff\1\46\1\26\1\110\2\46\1\64\1\110\1\26\1\110\1"+
        "\124\1\46\1\110";
    static final String DFA2_acceptS =
        "\2\uffff\1\2\1\1\14\uffff";
    static final String DFA2_specialS =
        "\20\uffff}>";
    static final String[] DFA2_transitionS = {
            "\1\1\4\uffff\1\3\6\uffff\1\2\11\uffff\1\3\3\uffff\1\2\2\uffff"+
            "\1\3\3\uffff\1\2\2\uffff\1\2\1\uffff\1\2\25\uffff\1\2\1\uffff"+
            "\1\2\4\uffff\1\3\1\uffff\1\2\6\uffff\1\2",
            "\1\4",
            "",
            "",
            "\1\6\1\5",
            "\1\7",
            "\1\10\66\uffff\1\11",
            "\1\12",
            "\1\14\1\13",
            "\1\15",
            "\1\10\66\uffff\1\11",
            "\1\16",
            "\1\10\66\uffff\1\11",
            "\1\3\20\uffff\1\3\6\uffff\1\3\45\uffff\1\3\10\uffff\1\2",
            "\1\17",
            "\1\10\66\uffff\1\11"
    };

    static final short[] DFA2_eot = DFA.unpackEncodedString(DFA2_eotS);
    static final short[] DFA2_eof = DFA.unpackEncodedString(DFA2_eofS);
    static final char[] DFA2_min = DFA.unpackEncodedStringToUnsignedChars(DFA2_minS);
    static final char[] DFA2_max = DFA.unpackEncodedStringToUnsignedChars(DFA2_maxS);
    static final short[] DFA2_accept = DFA.unpackEncodedString(DFA2_acceptS);
    static final short[] DFA2_special = DFA.unpackEncodedString(DFA2_specialS);
    static final short[][] DFA2_transition;

    static {
        int numStates = DFA2_transitionS.length;
        DFA2_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA2_transition[i] = DFA.unpackEncodedString(DFA2_transitionS[i]);
        }
    }

    class DFA2 extends DFA {

        public DFA2(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 2;
            this.eot = DFA2_eot;
            this.eof = DFA2_eof;
            this.min = DFA2_min;
            this.max = DFA2_max;
            this.accept = DFA2_accept;
            this.special = DFA2_special;
            this.transition = DFA2_transition;
        }
        public String getDescription() {
            return "()* loopback of 43:19: ( param_decl )*";
        }
    }
    static final String DFA26_eotS =
        "\7\uffff";
    static final String DFA26_eofS =
        "\1\uffff\1\4\3\uffff\1\4\1\uffff";
    static final String DFA26_minS =
        "\1\6\1\21\1\uffff\1\6\1\uffff\1\21\1\0";
    static final String DFA26_maxS =
        "\1\126\1\112\1\uffff\1\126\1\uffff\1\112\1\0";
    static final String DFA26_acceptS =
        "\2\uffff\1\2\1\uffff\1\1\2\uffff";
    static final String DFA26_specialS =
        "\6\uffff\1\0}>";
    static final String[] DFA26_transitionS = {
            "\1\1\7\uffff\1\2\3\uffff\1\2\31\uffff\1\1\1\uffff\1\1\17\uffff"+
            "\2\2\1\uffff\2\2\11\uffff\1\1\4\uffff\1\1\3\uffff\2\2",
            "\1\4\5\uffff\1\3\55\uffff\1\4\4\uffff\1\4",
            "",
            "\1\5\7\uffff\1\6\3\uffff\1\6\31\uffff\1\5\1\uffff\1\5\17\uffff"+
            "\2\6\1\uffff\2\6\11\uffff\1\5\4\uffff\1\5\3\uffff\2\6",
            "",
            "\1\4\5\uffff\1\3\55\uffff\1\4\4\uffff\1\4",
            "\1\uffff"
    };

    static final short[] DFA26_eot = DFA.unpackEncodedString(DFA26_eotS);
    static final short[] DFA26_eof = DFA.unpackEncodedString(DFA26_eofS);
    static final char[] DFA26_min = DFA.unpackEncodedStringToUnsignedChars(DFA26_minS);
    static final char[] DFA26_max = DFA.unpackEncodedStringToUnsignedChars(DFA26_maxS);
    static final short[] DFA26_accept = DFA.unpackEncodedString(DFA26_acceptS);
    static final short[] DFA26_special = DFA.unpackEncodedString(DFA26_specialS);
    static final short[][] DFA26_transition;

    static {
        int numStates = DFA26_transitionS.length;
        DFA26_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA26_transition[i] = DFA.unpackEncodedString(DFA26_transitionS[i]);
        }
    }

    class DFA26 extends DFA {

        public DFA26(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 26;
            this.eot = DFA26_eot;
            this.eof = DFA26_eof;
            this.min = DFA26_min;
            this.max = DFA26_max;
            this.accept = DFA26_accept;
            this.special = DFA26_special;
            this.transition = DFA26_transition;
        }
        public String getDescription() {
            return "160:1: comb_attr : ( attr_op ( DO attr_op )* ( DO attribute )? -> ^( DO ( attr_op )* ( attribute )? ) | ( attr_op DO )* attribute -> ^( DO ( attr_op )* ( attribute )? ) );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA26_6 = input.LA(1);

                         
                        int index26_6 = input.index();
                        input.rewind();

                        s = -1;
                        if ( (synpred45_FlatzincFullExtParser()) ) {s = 4;}

                        else if ( (true) ) {s = 2;}

                         
                        input.seek(index26_6);

                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}

            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 26, _s, input);
            error(nvae);
            throw nvae;
        }

    }
    static final String DFA28_eotS =
        "\20\uffff";
    static final String DFA28_eofS =
        "\20\uffff";
    static final String DFA28_minS =
        "\1\10\1\53\2\uffff\1\45\1\26\1\21\1\46\1\45\1\64\1\21\1\26\1\21"+
        "\1\15\1\46\1\21";
    static final String DFA28_maxS =
        "\1\124\1\53\2\uffff\1\46\1\26\1\110\2\46\1\64\1\110\1\26\1\110\1"+
        "\124\1\46\1\110";
    static final String DFA28_acceptS =
        "\2\uffff\1\1\1\2\14\uffff";
    static final String DFA28_specialS =
        "\20\uffff}>";
    static final String[] DFA28_transitionS = {
            "\1\1\4\uffff\1\2\20\uffff\1\2\6\uffff\2\2\1\uffff\1\2\42\uffff"+
            "\1\2\10\uffff\1\3",
            "\1\4",
            "",
            "",
            "\1\6\1\5",
            "\1\7",
            "\1\10\66\uffff\1\11",
            "\1\12",
            "\1\14\1\13",
            "\1\15",
            "\1\10\66\uffff\1\11",
            "\1\16",
            "\1\10\66\uffff\1\11",
            "\1\2\20\uffff\1\2\6\uffff\2\2\1\uffff\1\2\42\uffff\1\2\10\uffff"+
            "\1\3",
            "\1\17",
            "\1\10\66\uffff\1\11"
    };

    static final short[] DFA28_eot = DFA.unpackEncodedString(DFA28_eotS);
    static final short[] DFA28_eof = DFA.unpackEncodedString(DFA28_eofS);
    static final char[] DFA28_min = DFA.unpackEncodedStringToUnsignedChars(DFA28_minS);
    static final char[] DFA28_max = DFA.unpackEncodedStringToUnsignedChars(DFA28_maxS);
    static final short[] DFA28_accept = DFA.unpackEncodedString(DFA28_acceptS);
    static final short[] DFA28_special = DFA.unpackEncodedString(DFA28_specialS);
    static final short[][] DFA28_transition;

    static {
        int numStates = DFA28_transitionS.length;
        DFA28_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA28_transition[i] = DFA.unpackEncodedString(DFA28_transitionS[i]);
        }
    }

    class DFA28 extends DFA {

        public DFA28(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 28;
            this.eot = DFA28_eot;
            this.eof = DFA28_eof;
            this.min = DFA28_min;
            this.max = DFA28_max;
            this.accept = DFA28_accept;
            this.special = DFA28_special;
            this.transition = DFA28_transition;
        }
        public String getDescription() {
            return "192:1: pred_param_type : ( par_pred_param_type | var_pred_param_type );";
        }
    }
    static final String DFA45_eotS =
        "\33\uffff";
    static final String DFA45_eofS =
        "\33\uffff";
    static final String DFA45_minS =
        "\1\10\1\53\1\uffff\1\64\2\uffff\2\45\1\26\1\21\2\uffff\1\46\1\45"+
        "\1\64\1\21\1\26\1\21\1\15\1\46\2\uffff\1\64\1\21\1\45\2\uffff";
    static final String DFA45_maxS =
        "\1\113\1\53\1\uffff\1\64\2\uffff\1\46\1\50\1\26\1\110\2\uffff\2"+
        "\46\1\64\1\110\1\26\1\110\1\113\1\46\2\uffff\1\64\1\110\1\50\2\uffff";
    static final String DFA45_acceptS =
        "\2\uffff\1\1\1\uffff\1\2\1\3\4\uffff\1\4\1\5\10\uffff\1\6\1\7\3"+
        "\uffff\1\10\1\11";
    static final String DFA45_specialS =
        "\33\uffff}>";
    static final String[] DFA45_transitionS = {
            "\1\1\4\uffff\1\2\20\uffff\1\2\6\uffff\1\2\1\4\1\uffff\1\5\42"+
            "\uffff\1\3",
            "\1\6",
            "",
            "\1\7",
            "",
            "",
            "\1\11\1\10",
            "\1\2\1\12\1\uffff\1\13",
            "\1\14",
            "\1\15\66\uffff\1\16",
            "",
            "",
            "\1\17",
            "\1\21\1\20",
            "\1\22",
            "\1\15\66\uffff\1\16",
            "\1\23",
            "\1\15\66\uffff\1\16",
            "\1\2\20\uffff\1\2\6\uffff\1\2\1\24\1\uffff\1\25\42\uffff\1"+
            "\26",
            "\1\27",
            "",
            "",
            "\1\30",
            "\1\15\66\uffff\1\16",
            "\1\2\1\31\1\uffff\1\32",
            "",
            ""
    };

    static final short[] DFA45_eot = DFA.unpackEncodedString(DFA45_eotS);
    static final short[] DFA45_eof = DFA.unpackEncodedString(DFA45_eofS);
    static final char[] DFA45_min = DFA.unpackEncodedStringToUnsignedChars(DFA45_minS);
    static final char[] DFA45_max = DFA.unpackEncodedStringToUnsignedChars(DFA45_maxS);
    static final short[] DFA45_accept = DFA.unpackEncodedString(DFA45_acceptS);
    static final short[] DFA45_special = DFA.unpackEncodedString(DFA45_specialS);
    static final short[][] DFA45_transition;

    static {
        int numStates = DFA45_transitionS.length;
        DFA45_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA45_transition[i] = DFA.unpackEncodedString(DFA45_transitionS[i]);
        }
    }

    class DFA45 extends DFA {

        public DFA45(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 45;
            this.eot = DFA45_eot;
            this.eof = DFA45_eof;
            this.min = DFA45_min;
            this.max = DFA45_max;
            this.accept = DFA45_accept;
            this.special = DFA45_special;
            this.transition = DFA45_transition;
        }
        public String getDescription() {
            return "234:1: par_pred_param_type : ( par_type | INT_CONST DD INT_CONST -> ^( DD INT_CONST INT_CONST ) | LB INT_CONST ( CM INT_CONST )* RB -> ^( CM ( INT_CONST )+ ) | SET OF INT_CONST DD INT_CONST -> ^( SET ^( DD INT_CONST INT_CONST ) ) | SET OF LB INT_CONST ( CM INT_CONST )* RB -> ^( SET ^( CM ( INT_CONST )+ ) ) | ARRAY LS index_set ( CM index_set )* RS OF INT_CONST DD INT_CONST -> ^( ARRAY ( index_set )+ ^( DD INT_CONST INT_CONST ) ) | ARRAY LS index_set ( CM index_set )* RS OF LB INT_CONST ( CM INT_CONST )* RB -> ^( ARRAY ( index_set )+ ^( CM ( INT_CONST )+ ) ) | ARRAY LS index_set ( CM index_set )* RS OF SET OF INT_CONST DD INT_CONST -> ^( ARRAY ( index_set )+ ^( SET ^( DD INT_CONST INT_CONST ) ) ) | ARRAY LS index_set ( CM index_set )* RS OF SET OF LB INT_CONST ( CM INT_CONST )* RB -> ^( ARRAY ( index_set )+ ^( SET ^( CM ( INT_CONST )+ ) ) ) );";
        }
    }
    static final String DFA47_eotS =
        "\27\uffff";
    static final String DFA47_eofS =
        "\27\uffff";
    static final String DFA47_minS =
        "\1\10\1\53\1\15\1\45\1\64\1\uffff\1\26\1\21\1\45\1\46\1\45\1\64"+
        "\1\uffff\1\21\1\26\1\21\1\124\1\46\1\15\1\21\1\64\1\45\1\uffff";
    static final String DFA47_maxS =
        "\1\124\1\53\1\113\1\46\1\64\1\uffff\1\26\1\110\1\50\2\46\1\64\1"+
        "\uffff\1\110\1\26\1\110\1\124\1\46\1\113\1\110\1\64\1\50\1\uffff";
    static final String DFA47_acceptS =
        "\5\uffff\1\1\6\uffff\1\2\11\uffff\1\3";
    static final String DFA47_specialS =
        "\27\uffff}>";
    static final String[] DFA47_transitionS = {
            "\1\1\113\uffff\1\2",
            "\1\3",
            "\1\5\20\uffff\1\5\6\uffff\2\5\1\uffff\1\5\42\uffff\1\4",
            "\1\7\1\6",
            "\1\10",
            "",
            "\1\11",
            "\1\12\66\uffff\1\13",
            "\1\14\1\5\1\uffff\1\5",
            "\1\15",
            "\1\17\1\16",
            "\1\20",
            "",
            "\1\12\66\uffff\1\13",
            "\1\21",
            "\1\12\66\uffff\1\13",
            "\1\22",
            "\1\23",
            "\1\5\20\uffff\1\5\6\uffff\2\5\1\uffff\1\5\42\uffff\1\24",
            "\1\12\66\uffff\1\13",
            "\1\25",
            "\1\26\1\5\1\uffff\1\5",
            ""
    };

    static final short[] DFA47_eot = DFA.unpackEncodedString(DFA47_eotS);
    static final short[] DFA47_eof = DFA.unpackEncodedString(DFA47_eofS);
    static final char[] DFA47_min = DFA.unpackEncodedStringToUnsignedChars(DFA47_minS);
    static final char[] DFA47_max = DFA.unpackEncodedStringToUnsignedChars(DFA47_maxS);
    static final short[] DFA47_accept = DFA.unpackEncodedString(DFA47_acceptS);
    static final short[] DFA47_special = DFA.unpackEncodedString(DFA47_specialS);
    static final short[][] DFA47_transition;

    static {
        int numStates = DFA47_transitionS.length;
        DFA47_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA47_transition[i] = DFA.unpackEncodedString(DFA47_transitionS[i]);
        }
    }

    class DFA47 extends DFA {

        public DFA47(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 47;
            this.eot = DFA47_eot;
            this.eof = DFA47_eof;
            this.min = DFA47_min;
            this.max = DFA47_max;
            this.accept = DFA47_accept;
            this.special = DFA47_special;
            this.transition = DFA47_transition;
        }
        public String getDescription() {
            return "259:1: var_pred_param_type : ( var_type -> ^( VAR var_type ) | VAR SET OF INT -> ^( VAR SET ) | ARRAY LS index_set ( CM index_set )* RS OF VAR SET OF INT -> ^( ARRAY ( index_set )+ ^( VAR SET ) ) );";
        }
    }
 

    public static final BitSet FOLLOW_pred_decl_in_flatzinc_ext_model67 = new BitSet(new long[]{0x0000522440102100L,0x0000000000102858L});
    public static final BitSet FOLLOW_param_decl_in_flatzinc_ext_model72 = new BitSet(new long[]{0x0000522440102100L,0x0000000000102850L});
    public static final BitSet FOLLOW_var_decl_in_flatzinc_ext_model77 = new BitSet(new long[]{0x0000520400100100L,0x0000000000102050L});
    public static final BitSet FOLLOW_constraint_in_flatzinc_ext_model82 = new BitSet(new long[]{0x0000520400100000L,0x0000000000002050L});
    public static final BitSet FOLLOW_group_decl_in_flatzinc_ext_model87 = new BitSet(new long[]{0x0000520400000000L,0x0000000000002050L});
    public static final BitSet FOLLOW_structure_in_flatzinc_ext_model92 = new BitSet(new long[]{0x0000000000000000L,0x0000000000002000L});
    public static final BitSet FOLLOW_solve_goal_in_flatzinc_ext_model96 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENTIFIER_in_group_decl125 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_CL_in_group_decl127 = new BitSet(new long[]{0xC002040800044000L,0x0000000000640006L});
    public static final BitSet FOLLOW_predicates_in_group_decl129 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000400L});
    public static final BitSet FOLLOW_SC_in_group_decl131 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_predicate_in_predicates160 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LP_in_predicates165 = new BitSet(new long[]{0xC002040800044000L,0x0000000000640006L});
    public static final BitSet FOLLOW_predicates_in_predicates167 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_AND_in_predicates170 = new BitSet(new long[]{0xC002040800044000L,0x0000000000640006L});
    public static final BitSet FOLLOW_predicates_in_predicates172 = new BitSet(new long[]{0x0000000000000010L,0x0000000000000080L});
    public static final BitSet FOLLOW_RP_in_predicates176 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LP_in_predicates192 = new BitSet(new long[]{0xC002040800044000L,0x0000000000640006L});
    public static final BitSet FOLLOW_predicates_in_predicates194 = new BitSet(new long[]{0x0800000000000000L});
    public static final BitSet FOLLOW_OR_in_predicates197 = new BitSet(new long[]{0xC002040800044000L,0x0000000000640006L});
    public static final BitSet FOLLOW_predicates_in_predicates199 = new BitSet(new long[]{0x0800000000000000L,0x0000000000000080L});
    public static final BitSet FOLLOW_RP_in_predicates203 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TRUE_in_predicate226 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_attribute_in_predicate231 = new BitSet(new long[]{0x05E8000000000000L});
    public static final BitSet FOLLOW_op_in_predicate233 = new BitSet(new long[]{0x0000004000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_predicate235 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IN_in_predicate240 = new BitSet(new long[]{0x0000040000000000L});
    public static final BitSet FOLLOW_LP_in_predicate242 = new BitSet(new long[]{0x0000000400000000L});
    public static final BitSet FOLLOW_IDENTIFIER_in_predicate244 = new BitSet(new long[]{0x0000000000020000L,0x0000000000000080L});
    public static final BitSet FOLLOW_CM_in_predicate247 = new BitSet(new long[]{0x0000000400000000L});
    public static final BitSet FOLLOW_IDENTIFIER_in_predicate249 = new BitSet(new long[]{0x0000000000020000L,0x0000000000000080L});
    public static final BitSet FOLLOW_RP_in_predicate253 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NOT_in_predicate269 = new BitSet(new long[]{0xC002000800044000L,0x0000000000640006L});
    public static final BitSet FOLLOW_predicate_in_predicate271 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_struct_in_structure440 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000400L});
    public static final BitSet FOLLOW_SC_in_structure442 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_struct_reg_in_structure450 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000400L});
    public static final BitSet FOLLOW_SC_in_structure452 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_coll_in_struct467 = new BitSet(new long[]{0x0010000000000000L});
    public static final BitSet FOLLOW_OF_in_struct469 = new BitSet(new long[]{0x0000010000000000L});
    public static final BitSet FOLLOW_LB_in_struct471 = new BitSet(new long[]{0x0000520400000000L,0x0000000000000050L});
    public static final BitSet FOLLOW_elt_in_struct473 = new BitSet(new long[]{0x0000000000020000L,0x0000000000000020L});
    public static final BitSet FOLLOW_CM_in_struct476 = new BitSet(new long[]{0x0000520400000000L,0x0000000000000050L});
    public static final BitSet FOLLOW_elt_in_struct478 = new BitSet(new long[]{0x0000000000020000L,0x0000000000000020L});
    public static final BitSet FOLLOW_RB_in_struct482 = new BitSet(new long[]{0x0000008000000002L});
    public static final BitSet FOLLOW_KEY_in_struct485 = new BitSet(new long[]{0xC000500000044040L,0x0000000000621006L});
    public static final BitSet FOLLOW_comb_attr_in_struct487 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENTIFIER_in_struct_reg519 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_AS_in_struct_reg521 = new BitSet(new long[]{0x0000520000000000L,0x0000000000000050L});
    public static final BitSet FOLLOW_coll_in_struct_reg523 = new BitSet(new long[]{0x0010000000000000L});
    public static final BitSet FOLLOW_OF_in_struct_reg525 = new BitSet(new long[]{0x0000010000000000L});
    public static final BitSet FOLLOW_LB_in_struct_reg527 = new BitSet(new long[]{0x0000000001000000L});
    public static final BitSet FOLLOW_many_in_struct_reg529 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000020L});
    public static final BitSet FOLLOW_RB_in_struct_reg531 = new BitSet(new long[]{0x0000008000000002L});
    public static final BitSet FOLLOW_KEY_in_struct_reg534 = new BitSet(new long[]{0xC000500000044040L,0x0000000000621006L});
    public static final BitSet FOLLOW_comb_attr_in_struct_reg536 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_struct_reg_in_elt570 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_struct_in_elt580 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENTIFIER_in_elt590 = new BitSet(new long[]{0x0000008000000002L});
    public static final BitSet FOLLOW_KEY_in_elt593 = new BitSet(new long[]{0xC000000000044000L,0x0000000000600006L});
    public static final BitSet FOLLOW_attribute_in_elt595 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_EACH_in_many611 = new BitSet(new long[]{0xC000000000044000L,0x0000000000600006L});
    public static final BitSet FOLLOW_attribute_in_many613 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_AS_in_many615 = new BitSet(new long[]{0x0000520000000000L,0x0000000000000050L});
    public static final BitSet FOLLOW_coll_in_many617 = new BitSet(new long[]{0x0010008000000002L});
    public static final BitSet FOLLOW_OF_in_many620 = new BitSet(new long[]{0x0000010000000000L});
    public static final BitSet FOLLOW_LB_in_many622 = new BitSet(new long[]{0x0000000001000000L});
    public static final BitSet FOLLOW_many_in_many626 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000020L});
    public static final BitSet FOLLOW_RB_in_many628 = new BitSet(new long[]{0x0000008000000002L});
    public static final BitSet FOLLOW_KEY_in_many633 = new BitSet(new long[]{0xC000500000044040L,0x0000000000621006L});
    public static final BitSet FOLLOW_comb_attr_in_many635 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_QUEUE_in_coll705 = new BitSet(new long[]{0x0000040000000000L});
    public static final BitSet FOLLOW_LP_in_coll707 = new BitSet(new long[]{0x0200000000000000L,0x0000000001000000L});
    public static final BitSet FOLLOW_qiter_in_coll710 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000080L});
    public static final BitSet FOLLOW_RP_in_coll712 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_REV_in_coll722 = new BitSet(new long[]{0x0000020000000000L});
    public static final BitSet FOLLOW_LIST_in_coll726 = new BitSet(new long[]{0x0000040000000000L});
    public static final BitSet FOLLOW_LP_in_coll728 = new BitSet(new long[]{0x0200000080000000L,0x0000000001800000L});
    public static final BitSet FOLLOW_liter_in_coll731 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000080L});
    public static final BitSet FOLLOW_RP_in_coll733 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_coll742 = new BitSet(new long[]{0x0000000100000000L});
    public static final BitSet FOLLOW_HEAP_in_coll748 = new BitSet(new long[]{0x0000040000000000L});
    public static final BitSet FOLLOW_LP_in_coll751 = new BitSet(new long[]{0x0200000000000000L,0x0000000001000000L});
    public static final BitSet FOLLOW_qiter_in_coll754 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000080L});
    public static final BitSet FOLLOW_RP_in_coll756 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_qiter_in_liter798 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FOR_in_liter808 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_WFOR_in_liter818 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_attr_op_in_comb_attr833 = new BitSet(new long[]{0x0000000000800002L});
    public static final BitSet FOLLOW_DO_in_comb_attr836 = new BitSet(new long[]{0x0000500000000040L,0x0000000000021000L});
    public static final BitSet FOLLOW_attr_op_in_comb_attr838 = new BitSet(new long[]{0x0000000000800002L});
    public static final BitSet FOLLOW_DO_in_comb_attr844 = new BitSet(new long[]{0xC000000000044000L,0x0000000000600006L});
    public static final BitSet FOLLOW_attribute_in_comb_attr846 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_attr_op_in_comb_attr870 = new BitSet(new long[]{0x0000000000800000L});
    public static final BitSet FOLLOW_DO_in_comb_attr872 = new BitSet(new long[]{0xC000500000044040L,0x0000000000621006L});
    public static final BitSet FOLLOW_attribute_in_comb_attr876 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PREDICATE_in_pred_decl964 = new BitSet(new long[]{0x0000000400000000L});
    public static final BitSet FOLLOW_IDENTIFIER_in_pred_decl966 = new BitSet(new long[]{0x0000040000000000L});
    public static final BitSet FOLLOW_LP_in_pred_decl968 = new BitSet(new long[]{0x0000016040002100L,0x0000000000100800L});
    public static final BitSet FOLLOW_pred_param_in_pred_decl970 = new BitSet(new long[]{0x0000000000020000L,0x0000000000000080L});
    public static final BitSet FOLLOW_CM_in_pred_decl973 = new BitSet(new long[]{0x0000016040002100L,0x0000000000100800L});
    public static final BitSet FOLLOW_pred_param_in_pred_decl975 = new BitSet(new long[]{0x0000000000020000L,0x0000000000000080L});
    public static final BitSet FOLLOW_RP_in_pred_decl979 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000400L});
    public static final BitSet FOLLOW_SC_in_pred_decl981 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_pred_param_type_in_pred_param1009 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_CL_in_pred_param1011 = new BitSet(new long[]{0x0000000400000000L});
    public static final BitSet FOLLOW_IDENTIFIER_in_pred_param1013 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_par_pred_param_type_in_pred_param_type1047 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_var_pred_param_type_in_pred_param_type1057 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ARRAY_in_par_type1076 = new BitSet(new long[]{0x0000080000000000L});
    public static final BitSet FOLLOW_LS_in_par_type1078 = new BitSet(new long[]{0x0000006000000000L});
    public static final BitSet FOLLOW_index_set_in_par_type1080 = new BitSet(new long[]{0x0000000000020000L,0x0000000000000100L});
    public static final BitSet FOLLOW_CM_in_par_type1083 = new BitSet(new long[]{0x0000006000000000L});
    public static final BitSet FOLLOW_index_set_in_par_type1085 = new BitSet(new long[]{0x0000000000020000L,0x0000000000000100L});
    public static final BitSet FOLLOW_RS_in_par_type1089 = new BitSet(new long[]{0x0010000000000000L});
    public static final BitSet FOLLOW_OF_in_par_type1091 = new BitSet(new long[]{0x0000002040002000L,0x0000000000000800L});
    public static final BitSet FOLLOW_par_type_u_in_par_type1093 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_par_type_u_in_par_type1119 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_BOOL_in_par_type_u1151 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FLOAT_in_par_type_u1161 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SET_in_par_type_u1171 = new BitSet(new long[]{0x0010000000000000L});
    public static final BitSet FOLLOW_OF_in_par_type_u1173 = new BitSet(new long[]{0x0000002000000000L});
    public static final BitSet FOLLOW_INT_in_par_type_u1175 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INT_in_par_type_u1185 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ARRAY_in_var_type1204 = new BitSet(new long[]{0x0000080000000000L});
    public static final BitSet FOLLOW_LS_in_var_type1206 = new BitSet(new long[]{0x0000006000000000L});
    public static final BitSet FOLLOW_index_set_in_var_type1208 = new BitSet(new long[]{0x0000000000020000L,0x0000000000000100L});
    public static final BitSet FOLLOW_CM_in_var_type1211 = new BitSet(new long[]{0x0000006000000000L});
    public static final BitSet FOLLOW_index_set_in_var_type1213 = new BitSet(new long[]{0x0000000000020000L,0x0000000000000100L});
    public static final BitSet FOLLOW_RS_in_var_type1217 = new BitSet(new long[]{0x0010000000000000L});
    public static final BitSet FOLLOW_OF_in_var_type1219 = new BitSet(new long[]{0x0000000000000000L,0x0000000000100000L});
    public static final BitSet FOLLOW_VAR_in_var_type1221 = new BitSet(new long[]{0x0000016040002000L,0x0000000000000800L});
    public static final BitSet FOLLOW_var_type_u_in_var_type1223 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_VAR_in_var_type1249 = new BitSet(new long[]{0x0000016040002000L,0x0000000000000800L});
    public static final BitSet FOLLOW_var_type_u_in_var_type1251 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_BOOL_in_var_type_u1283 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FLOAT_in_var_type_u1293 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INT_in_var_type_u1303 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INT_CONST_in_var_type_u1313 = new BitSet(new long[]{0x0000000000400000L});
    public static final BitSet FOLLOW_DD_in_var_type_u1315 = new BitSet(new long[]{0x0000004000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_var_type_u1317 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LB_in_var_type_u1344 = new BitSet(new long[]{0x0000004000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_var_type_u1346 = new BitSet(new long[]{0x0000000000020000L,0x0000000000000020L});
    public static final BitSet FOLLOW_CM_in_var_type_u1349 = new BitSet(new long[]{0x0000004000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_var_type_u1351 = new BitSet(new long[]{0x0000000000020000L,0x0000000000000020L});
    public static final BitSet FOLLOW_RB_in_var_type_u1355 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SET_in_var_type_u1379 = new BitSet(new long[]{0x0010000000000000L});
    public static final BitSet FOLLOW_OF_in_var_type_u1381 = new BitSet(new long[]{0x0000004000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_var_type_u1383 = new BitSet(new long[]{0x0000000000400000L});
    public static final BitSet FOLLOW_DD_in_var_type_u1385 = new BitSet(new long[]{0x0000004000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_var_type_u1387 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SET_in_var_type_u1416 = new BitSet(new long[]{0x0010000000000000L});
    public static final BitSet FOLLOW_OF_in_var_type_u1418 = new BitSet(new long[]{0x0000010000000000L});
    public static final BitSet FOLLOW_LB_in_var_type_u1420 = new BitSet(new long[]{0x0000004000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_var_type_u1422 = new BitSet(new long[]{0x0000000000020000L,0x0000000000000020L});
    public static final BitSet FOLLOW_CM_in_var_type_u1425 = new BitSet(new long[]{0x0000004000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_var_type_u1427 = new BitSet(new long[]{0x0000000000020000L,0x0000000000000020L});
    public static final BitSet FOLLOW_RB_in_var_type_u1431 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_par_type_in_par_pred_param_type1469 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type1481 = new BitSet(new long[]{0x0000000000400000L});
    public static final BitSet FOLLOW_DD_in_par_pred_param_type1483 = new BitSet(new long[]{0x0000004000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type1485 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LB_in_par_pred_param_type1510 = new BitSet(new long[]{0x0000004000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type1512 = new BitSet(new long[]{0x0000000000020000L,0x0000000000000020L});
    public static final BitSet FOLLOW_CM_in_par_pred_param_type1515 = new BitSet(new long[]{0x0000004000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type1517 = new BitSet(new long[]{0x0000000000020000L,0x0000000000000020L});
    public static final BitSet FOLLOW_RB_in_par_pred_param_type1521 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SET_in_par_pred_param_type1545 = new BitSet(new long[]{0x0010000000000000L});
    public static final BitSet FOLLOW_OF_in_par_pred_param_type1547 = new BitSet(new long[]{0x0000004000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type1549 = new BitSet(new long[]{0x0000000000400000L});
    public static final BitSet FOLLOW_DD_in_par_pred_param_type1551 = new BitSet(new long[]{0x0000004000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type1553 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SET_in_par_pred_param_type1582 = new BitSet(new long[]{0x0010000000000000L});
    public static final BitSet FOLLOW_OF_in_par_pred_param_type1584 = new BitSet(new long[]{0x0000010000000000L});
    public static final BitSet FOLLOW_LB_in_par_pred_param_type1586 = new BitSet(new long[]{0x0000004000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type1588 = new BitSet(new long[]{0x0000000000020000L,0x0000000000000020L});
    public static final BitSet FOLLOW_CM_in_par_pred_param_type1591 = new BitSet(new long[]{0x0000004000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type1593 = new BitSet(new long[]{0x0000000000020000L,0x0000000000000020L});
    public static final BitSet FOLLOW_RB_in_par_pred_param_type1597 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ARRAY_in_par_pred_param_type1627 = new BitSet(new long[]{0x0000080000000000L});
    public static final BitSet FOLLOW_LS_in_par_pred_param_type1629 = new BitSet(new long[]{0x0000006000000000L});
    public static final BitSet FOLLOW_index_set_in_par_pred_param_type1631 = new BitSet(new long[]{0x0000000000020000L,0x0000000000000100L});
    public static final BitSet FOLLOW_CM_in_par_pred_param_type1634 = new BitSet(new long[]{0x0000006000000000L});
    public static final BitSet FOLLOW_index_set_in_par_pred_param_type1636 = new BitSet(new long[]{0x0000000000020000L,0x0000000000000100L});
    public static final BitSet FOLLOW_RS_in_par_pred_param_type1640 = new BitSet(new long[]{0x0010000000000000L});
    public static final BitSet FOLLOW_OF_in_par_pred_param_type1642 = new BitSet(new long[]{0x0000004000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type1644 = new BitSet(new long[]{0x0000000000400000L});
    public static final BitSet FOLLOW_DD_in_par_pred_param_type1646 = new BitSet(new long[]{0x0000004000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type1648 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ARRAY_in_par_pred_param_type1680 = new BitSet(new long[]{0x0000080000000000L});
    public static final BitSet FOLLOW_LS_in_par_pred_param_type1682 = new BitSet(new long[]{0x0000006000000000L});
    public static final BitSet FOLLOW_index_set_in_par_pred_param_type1684 = new BitSet(new long[]{0x0000000000020000L,0x0000000000000100L});
    public static final BitSet FOLLOW_CM_in_par_pred_param_type1687 = new BitSet(new long[]{0x0000006000000000L});
    public static final BitSet FOLLOW_index_set_in_par_pred_param_type1689 = new BitSet(new long[]{0x0000000000020000L,0x0000000000000100L});
    public static final BitSet FOLLOW_RS_in_par_pred_param_type1693 = new BitSet(new long[]{0x0010000000000000L});
    public static final BitSet FOLLOW_OF_in_par_pred_param_type1695 = new BitSet(new long[]{0x0000010000000000L});
    public static final BitSet FOLLOW_LB_in_par_pred_param_type1697 = new BitSet(new long[]{0x0000004000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type1699 = new BitSet(new long[]{0x0000000000020000L,0x0000000000000020L});
    public static final BitSet FOLLOW_CM_in_par_pred_param_type1702 = new BitSet(new long[]{0x0000004000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type1704 = new BitSet(new long[]{0x0000000000020000L,0x0000000000000020L});
    public static final BitSet FOLLOW_RB_in_par_pred_param_type1708 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ARRAY_in_par_pred_param_type1739 = new BitSet(new long[]{0x0000080000000000L});
    public static final BitSet FOLLOW_LS_in_par_pred_param_type1741 = new BitSet(new long[]{0x0000006000000000L});
    public static final BitSet FOLLOW_index_set_in_par_pred_param_type1743 = new BitSet(new long[]{0x0000000000020000L,0x0000000000000100L});
    public static final BitSet FOLLOW_CM_in_par_pred_param_type1746 = new BitSet(new long[]{0x0000006000000000L});
    public static final BitSet FOLLOW_index_set_in_par_pred_param_type1748 = new BitSet(new long[]{0x0000000000020000L,0x0000000000000100L});
    public static final BitSet FOLLOW_RS_in_par_pred_param_type1752 = new BitSet(new long[]{0x0010000000000000L});
    public static final BitSet FOLLOW_OF_in_par_pred_param_type1754 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000800L});
    public static final BitSet FOLLOW_SET_in_par_pred_param_type1756 = new BitSet(new long[]{0x0010000000000000L});
    public static final BitSet FOLLOW_OF_in_par_pred_param_type1758 = new BitSet(new long[]{0x0000004000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type1760 = new BitSet(new long[]{0x0000000000400000L});
    public static final BitSet FOLLOW_DD_in_par_pred_param_type1762 = new BitSet(new long[]{0x0000004000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type1764 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ARRAY_in_par_pred_param_type1800 = new BitSet(new long[]{0x0000080000000000L});
    public static final BitSet FOLLOW_LS_in_par_pred_param_type1802 = new BitSet(new long[]{0x0000006000000000L});
    public static final BitSet FOLLOW_index_set_in_par_pred_param_type1804 = new BitSet(new long[]{0x0000000000020000L,0x0000000000000100L});
    public static final BitSet FOLLOW_CM_in_par_pred_param_type1807 = new BitSet(new long[]{0x0000006000000000L});
    public static final BitSet FOLLOW_index_set_in_par_pred_param_type1809 = new BitSet(new long[]{0x0000000000020000L,0x0000000000000100L});
    public static final BitSet FOLLOW_RS_in_par_pred_param_type1813 = new BitSet(new long[]{0x0010000000000000L});
    public static final BitSet FOLLOW_OF_in_par_pred_param_type1815 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000800L});
    public static final BitSet FOLLOW_SET_in_par_pred_param_type1817 = new BitSet(new long[]{0x0010000000000000L});
    public static final BitSet FOLLOW_OF_in_par_pred_param_type1819 = new BitSet(new long[]{0x0000010000000000L});
    public static final BitSet FOLLOW_LB_in_par_pred_param_type1821 = new BitSet(new long[]{0x0000004000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type1823 = new BitSet(new long[]{0x0000000000020000L,0x0000000000000020L});
    public static final BitSet FOLLOW_CM_in_par_pred_param_type1826 = new BitSet(new long[]{0x0000004000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type1828 = new BitSet(new long[]{0x0000000000020000L,0x0000000000000020L});
    public static final BitSet FOLLOW_RB_in_par_pred_param_type1832 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_var_type_in_var_pred_param_type1877 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_VAR_in_var_pred_param_type1900 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000800L});
    public static final BitSet FOLLOW_SET_in_var_pred_param_type1902 = new BitSet(new long[]{0x0010000000000000L});
    public static final BitSet FOLLOW_OF_in_var_pred_param_type1904 = new BitSet(new long[]{0x0000002000000000L});
    public static final BitSet FOLLOW_INT_in_var_pred_param_type1906 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ARRAY_in_var_pred_param_type1929 = new BitSet(new long[]{0x0000080000000000L});
    public static final BitSet FOLLOW_LS_in_var_pred_param_type1931 = new BitSet(new long[]{0x0000006000000000L});
    public static final BitSet FOLLOW_index_set_in_var_pred_param_type1933 = new BitSet(new long[]{0x0000000000020000L,0x0000000000000100L});
    public static final BitSet FOLLOW_CM_in_var_pred_param_type1936 = new BitSet(new long[]{0x0000006000000000L});
    public static final BitSet FOLLOW_index_set_in_var_pred_param_type1938 = new BitSet(new long[]{0x0000000000020000L,0x0000000000000100L});
    public static final BitSet FOLLOW_RS_in_var_pred_param_type1942 = new BitSet(new long[]{0x0010000000000000L});
    public static final BitSet FOLLOW_OF_in_var_pred_param_type1944 = new BitSet(new long[]{0x0000000000000000L,0x0000000000100000L});
    public static final BitSet FOLLOW_VAR_in_var_pred_param_type1946 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000800L});
    public static final BitSet FOLLOW_SET_in_var_pred_param_type1948 = new BitSet(new long[]{0x0010000000000000L});
    public static final BitSet FOLLOW_OF_in_var_pred_param_type1950 = new BitSet(new long[]{0x0000002000000000L});
    public static final BitSet FOLLOW_INT_in_var_pred_param_type1952 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INT_CONST_in_index_set1991 = new BitSet(new long[]{0x0000000000400000L});
    public static final BitSet FOLLOW_DD_in_index_set1993 = new BitSet(new long[]{0x0000004000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_index_set1995 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INT_in_index_set2024 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LB_in_expr2056 = new BitSet(new long[]{0x0000004000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_expr2058 = new BitSet(new long[]{0x0000000000020000L,0x0000000000000020L});
    public static final BitSet FOLLOW_CM_in_expr2061 = new BitSet(new long[]{0x0000004000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_expr2063 = new BitSet(new long[]{0x0000000000020000L,0x0000000000000020L});
    public static final BitSet FOLLOW_RB_in_expr2067 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_bool_const_in_expr2091 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INT_CONST_in_expr2101 = new BitSet(new long[]{0x0000000000400002L});
    public static final BitSet FOLLOW_DD_in_expr2104 = new BitSet(new long[]{0x0000004000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_expr2106 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LS_in_expr2118 = new BitSet(new long[]{0x0000094420000000L,0x0000000000048100L});
    public static final BitSet FOLLOW_expr_in_expr2121 = new BitSet(new long[]{0x0000000000020000L,0x0000000000000100L});
    public static final BitSet FOLLOW_CM_in_expr2124 = new BitSet(new long[]{0x0000094420000000L,0x0000000000048000L});
    public static final BitSet FOLLOW_expr_in_expr2126 = new BitSet(new long[]{0x0000000000020000L,0x0000000000000100L});
    public static final BitSet FOLLOW_RS_in_expr2132 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_id_expr_in_expr2160 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_in_expr2170 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENTIFIER_in_id_expr2191 = new BitSet(new long[]{0x00000C0000000002L});
    public static final BitSet FOLLOW_LP_in_id_expr2195 = new BitSet(new long[]{0x0000094420000000L,0x0000000000048000L});
    public static final BitSet FOLLOW_expr_in_id_expr2197 = new BitSet(new long[]{0x0000000000020000L,0x0000000000000080L});
    public static final BitSet FOLLOW_CM_in_id_expr2200 = new BitSet(new long[]{0x0000094420000000L,0x0000000000048000L});
    public static final BitSet FOLLOW_expr_in_id_expr2202 = new BitSet(new long[]{0x0000000000020000L,0x0000000000000080L});
    public static final BitSet FOLLOW_RP_in_id_expr2206 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LS_in_id_expr2210 = new BitSet(new long[]{0x0000004000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_id_expr2212 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000100L});
    public static final BitSet FOLLOW_RS_in_id_expr2214 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_par_type_in_param_decl2234 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_CL_in_param_decl2236 = new BitSet(new long[]{0x0000000400000000L});
    public static final BitSet FOLLOW_IDENTIFIER_in_param_decl2238 = new BitSet(new long[]{0x0000000002000000L});
    public static final BitSet FOLLOW_EQ_in_param_decl2240 = new BitSet(new long[]{0x0000094420000000L,0x0000000000048000L});
    public static final BitSet FOLLOW_expr_in_param_decl2242 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000400L});
    public static final BitSet FOLLOW_SC_in_param_decl2244 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_var_type_in_var_decl2272 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_CL_in_var_decl2274 = new BitSet(new long[]{0x0000000400000000L});
    public static final BitSet FOLLOW_IDENTIFIER_in_var_decl2276 = new BitSet(new long[]{0x0000000002200000L,0x0000000000000400L});
    public static final BitSet FOLLOW_annotations_in_var_decl2278 = new BitSet(new long[]{0x0000000002000000L,0x0000000000000400L});
    public static final BitSet FOLLOW_EQ_in_var_decl2281 = new BitSet(new long[]{0x0000094420000000L,0x0000000000048000L});
    public static final BitSet FOLLOW_expr_in_var_decl2283 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000400L});
    public static final BitSet FOLLOW_SC_in_var_decl2287 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CONSTRAINT_in_constraint2317 = new BitSet(new long[]{0x0000000400000000L});
    public static final BitSet FOLLOW_IDENTIFIER_in_constraint2319 = new BitSet(new long[]{0x0000040000000000L});
    public static final BitSet FOLLOW_LP_in_constraint2321 = new BitSet(new long[]{0x0000094420000000L,0x0000000000048000L});
    public static final BitSet FOLLOW_expr_in_constraint2323 = new BitSet(new long[]{0x0000000000020000L,0x0000000000000080L});
    public static final BitSet FOLLOW_CM_in_constraint2326 = new BitSet(new long[]{0x0000094420000000L,0x0000000000048000L});
    public static final BitSet FOLLOW_expr_in_constraint2328 = new BitSet(new long[]{0x0000000000020000L,0x0000000000000080L});
    public static final BitSet FOLLOW_RP_in_constraint2332 = new BitSet(new long[]{0x0000000000200000L,0x0000000000000400L});
    public static final BitSet FOLLOW_annotations_in_constraint2334 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000400L});
    public static final BitSet FOLLOW_SC_in_constraint2336 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SOLVE_in_solve_goal2364 = new BitSet(new long[]{0x0000A00000200000L,0x0000000000000200L});
    public static final BitSet FOLLOW_annotations_in_solve_goal2367 = new BitSet(new long[]{0x0000A00000000000L,0x0000000000000200L});
    public static final BitSet FOLLOW_resolution_in_solve_goal2369 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000400L});
    public static final BitSet FOLLOW_SC_in_solve_goal2371 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MINIMIZE_in_resolution2388 = new BitSet(new long[]{0x0000094420000000L,0x0000000000048000L});
    public static final BitSet FOLLOW_expr_in_resolution2391 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MAXIMIZE_in_resolution2401 = new BitSet(new long[]{0x0000094420000000L,0x0000000000048000L});
    public static final BitSet FOLLOW_expr_in_resolution2404 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SATISFY_in_resolution2414 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DC_in_annotations2435 = new BitSet(new long[]{0x0000000400000000L});
    public static final BitSet FOLLOW_annotation_in_annotations2437 = new BitSet(new long[]{0x0000000000200002L});
    public static final BitSet FOLLOW_IDENTIFIER_in_annotation2472 = new BitSet(new long[]{0x0000040000000002L});
    public static final BitSet FOLLOW_LP_in_annotation2475 = new BitSet(new long[]{0x0000094420000000L,0x0000000000048000L});
    public static final BitSet FOLLOW_expr_in_annotation2477 = new BitSet(new long[]{0x0000000000020000L,0x0000000000000080L});
    public static final BitSet FOLLOW_CM_in_annotation2480 = new BitSet(new long[]{0x0000094420000000L,0x0000000000048000L});
    public static final BitSet FOLLOW_expr_in_annotation2482 = new BitSet(new long[]{0x0000000000020000L,0x0000000000000080L});
    public static final BitSet FOLLOW_RP_in_annotation2486 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TRUE_in_bool_const2527 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FALSE_in_bool_const2538 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LP_in_synpred9_FlatzincFullExtParser165 = new BitSet(new long[]{0xC002040800044000L,0x0000000000640006L});
    public static final BitSet FOLLOW_predicates_in_synpred9_FlatzincFullExtParser167 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_AND_in_synpred9_FlatzincFullExtParser170 = new BitSet(new long[]{0xC002040800044000L,0x0000000000640006L});
    public static final BitSet FOLLOW_predicates_in_synpred9_FlatzincFullExtParser172 = new BitSet(new long[]{0x0000000000000010L,0x0000000000000080L});
    public static final BitSet FOLLOW_RP_in_synpred9_FlatzincFullExtParser176 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_attr_op_in_synpred45_FlatzincFullExtParser833 = new BitSet(new long[]{0x0000000000800002L});
    public static final BitSet FOLLOW_DO_in_synpred45_FlatzincFullExtParser836 = new BitSet(new long[]{0x0000500000000040L,0x0000000000021000L});
    public static final BitSet FOLLOW_attr_op_in_synpred45_FlatzincFullExtParser838 = new BitSet(new long[]{0x0000000000800002L});
    public static final BitSet FOLLOW_DO_in_synpred45_FlatzincFullExtParser844 = new BitSet(new long[]{0xC000000000044000L,0x0000000000600006L});
    public static final BitSet FOLLOW_attribute_in_synpred45_FlatzincFullExtParser846 = new BitSet(new long[]{0x0000000000000002L});

}