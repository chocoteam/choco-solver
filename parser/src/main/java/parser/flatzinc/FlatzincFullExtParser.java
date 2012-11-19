// $ANTLR 3.4 parser/flatzinc/FlatzincFullExtParser.g 2012-11-19 15:03:00

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
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "AND", "ANNOTATIONS", "ANY", "APAR", "ARRAY", "ARRPAR", "ARRVAR", "AS", "AVAR", "BOOL", "CARITY", "CHAR", "CL", "CM", "CNAME", "COMMENT", "CONSTRAINT", "CSTR", "DC", "DD", "DO", "EACH", "EQ", "ESC_SEQ", "EXPONENT", "EXPR", "FALSE", "FLOAT", "FOR", "HEAP", "HEX_DIGIT", "IDENTIFIER", "IN", "INDEX", "INT", "INT_CONST", "KEY", "LB", "LIST", "LP", "LS", "MAX", "MAXIMIZE", "MIN", "MINIMIZE", "MN", "NOT", "OCTAL_ESC", "OEQ", "OF", "OGQ", "OGT", "OLQ", "OLT", "ONE", "ONQ", "OR", "ORDERBY", "PAR", "PARITY", "PL", "PPRIO", "PPRIOD", "PREDICATE", "PROP", "QUEUE", "RB", "REV", "RP", "RS", "SATISFY", "SC", "SET", "SIZE", "SOLVE", "STREG", "STRING", "STRUC", "SUM", "TRUE", "UNICODE_ESC", "VAR", "VCARD", "VNAME", "WFOR", "WONE", "WS"
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
                case HEAP:
                case IDENTIFIER:
                case LIST:
                case MAX:
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
            case CSTR:
            case IN:
            case NOT:
            case PARITY:
            case PPRIO:
            case PPRIOD:
            case PROP:
            case TRUE:
            case VAR:
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
            case CSTR:
            case PARITY:
            case PPRIO:
            case PPRIOD:
            case PROP:
            case VAR:
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
                    // elements: IDENTIFIER, IN
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
    // parser/flatzinc/FlatzincFullExtParser.g:84:1: attribute : ( VAR | CSTR | PROP | VNAME | VCARD | CNAME | CARITY | PPRIO | PARITY | PPRIOD );
    public final FlatzincFullExtParser.attribute_return attribute() throws RecognitionException {
        FlatzincFullExtParser.attribute_return retval = new FlatzincFullExtParser.attribute_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token set35=null;

        Object set35_tree=null;

        try {
            // parser/flatzinc/FlatzincFullExtParser.g:85:2: ( VAR | CSTR | PROP | VNAME | VCARD | CNAME | CARITY | PPRIO | PARITY | PPRIOD )
            // parser/flatzinc/FlatzincFullExtParser.g:
            {
            root_0 = (Object)adaptor.nil();


            set35=(Token)input.LT(1);

            if ( input.LA(1)==CARITY||input.LA(1)==CNAME||input.LA(1)==CSTR||input.LA(1)==PARITY||(input.LA(1) >= PPRIO && input.LA(1) <= PPRIOD)||input.LA(1)==PROP||(input.LA(1) >= VAR && input.LA(1) <= VNAME) ) {
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
    // parser/flatzinc/FlatzincFullExtParser.g:99:1: op : ( OEQ | ONQ | OLT | OGT | OLQ | OGQ );
    public final FlatzincFullExtParser.op_return op() throws RecognitionException {
        FlatzincFullExtParser.op_return retval = new FlatzincFullExtParser.op_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token set36=null;

        Object set36_tree=null;

        try {
            // parser/flatzinc/FlatzincFullExtParser.g:100:5: ( OEQ | ONQ | OLT | OGT | OLQ | OGQ )
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
    // parser/flatzinc/FlatzincFullExtParser.g:113:1: structure : ( struct SC !| struct_reg SC !);
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
            // parser/flatzinc/FlatzincFullExtParser.g:114:2: ( struct SC !| struct_reg SC !)
            int alt12=2;
            switch ( input.LA(1) ) {
            case HEAP:
            case LIST:
            case MAX:
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
                    // parser/flatzinc/FlatzincFullExtParser.g:114:6: struct SC !
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_struct_in_structure451);
                    struct37=struct();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, struct37.getTree());

                    SC38=(Token)match(input,SC,FOLLOW_SC_in_structure453); if (state.failed) return retval;

                    }
                    break;
                case 2 :
                    // parser/flatzinc/FlatzincFullExtParser.g:115:6: struct_reg SC !
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_struct_reg_in_structure461);
                    struct_reg39=struct_reg();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, struct_reg39.getTree());

                    SC40=(Token)match(input,SC,FOLLOW_SC_in_structure463); if (state.failed) return retval;

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
    // parser/flatzinc/FlatzincFullExtParser.g:118:1: struct : coll OF LB elt ( CM elt )* RB ( KEY comb_attr )? -> ^( STRUC ( elt )+ ( comb_attr )? coll ) ;
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
            // parser/flatzinc/FlatzincFullExtParser.g:119:5: ( coll OF LB elt ( CM elt )* RB ( KEY comb_attr )? -> ^( STRUC ( elt )+ ( comb_attr )? coll ) )
            // parser/flatzinc/FlatzincFullExtParser.g:119:7: coll OF LB elt ( CM elt )* RB ( KEY comb_attr )?
            {
            pushFollow(FOLLOW_coll_in_struct478);
            coll41=coll();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_coll.add(coll41.getTree());

            OF42=(Token)match(input,OF,FOLLOW_OF_in_struct480); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_OF.add(OF42);


            LB43=(Token)match(input,LB,FOLLOW_LB_in_struct482); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LB.add(LB43);


            pushFollow(FOLLOW_elt_in_struct484);
            elt44=elt();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_elt.add(elt44.getTree());

            // parser/flatzinc/FlatzincFullExtParser.g:119:22: ( CM elt )*
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
            	    // parser/flatzinc/FlatzincFullExtParser.g:119:23: CM elt
            	    {
            	    CM45=(Token)match(input,CM,FOLLOW_CM_in_struct487); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_CM.add(CM45);


            	    pushFollow(FOLLOW_elt_in_struct489);
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


            RB47=(Token)match(input,RB,FOLLOW_RB_in_struct493); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RB.add(RB47);


            // parser/flatzinc/FlatzincFullExtParser.g:119:35: ( KEY comb_attr )?
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
                    // parser/flatzinc/FlatzincFullExtParser.g:119:36: KEY comb_attr
                    {
                    KEY48=(Token)match(input,KEY,FOLLOW_KEY_in_struct496); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_KEY.add(KEY48);


                    pushFollow(FOLLOW_comb_attr_in_struct498);
                    comb_attr49=comb_attr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_comb_attr.add(comb_attr49.getTree());

                    }
                    break;

            }


            // AST REWRITE
            // elements: comb_attr, coll, elt
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {

            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 120:5: -> ^( STRUC ( elt )+ ( comb_attr )? coll )
            {
                // parser/flatzinc/FlatzincFullExtParser.g:120:9: ^( STRUC ( elt )+ ( comb_attr )? coll )
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

                // parser/flatzinc/FlatzincFullExtParser.g:120:22: ( comb_attr )?
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
    // parser/flatzinc/FlatzincFullExtParser.g:123:1: struct_reg : IDENTIFIER AS coll OF LB many RB ( KEY comb_attr )? -> ^( STREG IDENTIFIER many ( comb_attr )? coll ) ;
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
            // parser/flatzinc/FlatzincFullExtParser.g:124:2: ( IDENTIFIER AS coll OF LB many RB ( KEY comb_attr )? -> ^( STREG IDENTIFIER many ( comb_attr )? coll ) )
            // parser/flatzinc/FlatzincFullExtParser.g:124:4: IDENTIFIER AS coll OF LB many RB ( KEY comb_attr )?
            {
            IDENTIFIER50=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_struct_reg530); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_IDENTIFIER.add(IDENTIFIER50);


            AS51=(Token)match(input,AS,FOLLOW_AS_in_struct_reg532); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_AS.add(AS51);


            pushFollow(FOLLOW_coll_in_struct_reg534);
            coll52=coll();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_coll.add(coll52.getTree());

            OF53=(Token)match(input,OF,FOLLOW_OF_in_struct_reg536); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_OF.add(OF53);


            LB54=(Token)match(input,LB,FOLLOW_LB_in_struct_reg538); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LB.add(LB54);


            pushFollow(FOLLOW_many_in_struct_reg540);
            many55=many();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_many.add(many55.getTree());

            RB56=(Token)match(input,RB,FOLLOW_RB_in_struct_reg542); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RB.add(RB56);


            // parser/flatzinc/FlatzincFullExtParser.g:124:37: ( KEY comb_attr )?
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
                    // parser/flatzinc/FlatzincFullExtParser.g:124:38: KEY comb_attr
                    {
                    KEY57=(Token)match(input,KEY,FOLLOW_KEY_in_struct_reg545); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_KEY.add(KEY57);


                    pushFollow(FOLLOW_comb_attr_in_struct_reg547);
                    comb_attr58=comb_attr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_comb_attr.add(comb_attr58.getTree());

                    }
                    break;

            }


            // AST REWRITE
            // elements: comb_attr, IDENTIFIER, coll, many
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {

            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 125:2: -> ^( STREG IDENTIFIER many ( comb_attr )? coll )
            {
                // parser/flatzinc/FlatzincFullExtParser.g:125:6: ^( STREG IDENTIFIER many ( comb_attr )? coll )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot(
                (Object)adaptor.create(STREG, "STREG")
                , root_1);

                adaptor.addChild(root_1, 
                stream_IDENTIFIER.nextNode()
                );

                adaptor.addChild(root_1, stream_many.nextTree());

                // parser/flatzinc/FlatzincFullExtParser.g:125:30: ( comb_attr )?
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
    // parser/flatzinc/FlatzincFullExtParser.g:129:1: elt : ( struct_reg | struct | IDENTIFIER ( KEY attribute )? );
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
            // parser/flatzinc/FlatzincFullExtParser.g:130:5: ( struct_reg | struct | IDENTIFIER ( KEY attribute )? )
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
            case HEAP:
            case LIST:
            case MAX:
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
                    // parser/flatzinc/FlatzincFullExtParser.g:130:7: struct_reg
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_struct_reg_in_elt581);
                    struct_reg59=struct_reg();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, struct_reg59.getTree());

                    }
                    break;
                case 2 :
                    // parser/flatzinc/FlatzincFullExtParser.g:131:9: struct
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_struct_in_elt591);
                    struct60=struct();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, struct60.getTree());

                    }
                    break;
                case 3 :
                    // parser/flatzinc/FlatzincFullExtParser.g:132:9: IDENTIFIER ( KEY attribute )?
                    {
                    root_0 = (Object)adaptor.nil();


                    IDENTIFIER61=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_elt601); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    IDENTIFIER61_tree = 
                    (Object)adaptor.create(IDENTIFIER61)
                    ;
                    adaptor.addChild(root_0, IDENTIFIER61_tree);
                    }

                    // parser/flatzinc/FlatzincFullExtParser.g:132:20: ( KEY attribute )?
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
                            // parser/flatzinc/FlatzincFullExtParser.g:132:21: KEY attribute
                            {
                            KEY62=(Token)match(input,KEY,FOLLOW_KEY_in_elt604); if (state.failed) return retval;
                            if ( state.backtracking==0 ) {
                            KEY62_tree = 
                            (Object)adaptor.create(KEY62)
                            ;
                            adaptor.addChild(root_0, KEY62_tree);
                            }

                            pushFollow(FOLLOW_attribute_in_elt606);
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
    // parser/flatzinc/FlatzincFullExtParser.g:135:1: many : EACH attribute AS coll ( OF LB m= many RB )? ( KEY comb_attr )? -> {m==null}? ^( attribute ( comb_attr )? coll ) -> ^( EACH attribute ( comb_attr )? many coll ) ;
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
            // parser/flatzinc/FlatzincFullExtParser.g:136:5: ( EACH attribute AS coll ( OF LB m= many RB )? ( KEY comb_attr )? -> {m==null}? ^( attribute ( comb_attr )? coll ) -> ^( EACH attribute ( comb_attr )? many coll ) )
            // parser/flatzinc/FlatzincFullExtParser.g:136:7: EACH attribute AS coll ( OF LB m= many RB )? ( KEY comb_attr )?
            {
            EACH64=(Token)match(input,EACH,FOLLOW_EACH_in_many622); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_EACH.add(EACH64);


            pushFollow(FOLLOW_attribute_in_many624);
            attribute65=attribute();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_attribute.add(attribute65.getTree());

            AS66=(Token)match(input,AS,FOLLOW_AS_in_many626); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_AS.add(AS66);


            pushFollow(FOLLOW_coll_in_many628);
            coll67=coll();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_coll.add(coll67.getTree());

            // parser/flatzinc/FlatzincFullExtParser.g:136:30: ( OF LB m= many RB )?
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
                    // parser/flatzinc/FlatzincFullExtParser.g:136:31: OF LB m= many RB
                    {
                    OF68=(Token)match(input,OF,FOLLOW_OF_in_many631); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_OF.add(OF68);


                    LB69=(Token)match(input,LB,FOLLOW_LB_in_many633); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LB.add(LB69);


                    pushFollow(FOLLOW_many_in_many637);
                    m=many();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_many.add(m.getTree());

                    RB70=(Token)match(input,RB,FOLLOW_RB_in_many639); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RB.add(RB70);


                    }
                    break;

            }


            // parser/flatzinc/FlatzincFullExtParser.g:136:49: ( KEY comb_attr )?
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
                    // parser/flatzinc/FlatzincFullExtParser.g:136:50: KEY comb_attr
                    {
                    KEY71=(Token)match(input,KEY,FOLLOW_KEY_in_many644); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_KEY.add(KEY71);


                    pushFollow(FOLLOW_comb_attr_in_many646);
                    comb_attr72=comb_attr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_comb_attr.add(comb_attr72.getTree());

                    }
                    break;

            }


            // AST REWRITE
            // elements: comb_attr, attribute, comb_attr, coll, EACH, coll, many, attribute
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {

            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 137:5: -> {m==null}? ^( attribute ( comb_attr )? coll )
            if (m==null) {
                // parser/flatzinc/FlatzincFullExtParser.g:137:21: ^( attribute ( comb_attr )? coll )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot(stream_attribute.nextNode(), root_1);

                // parser/flatzinc/FlatzincFullExtParser.g:137:33: ( comb_attr )?
                if ( stream_comb_attr.hasNext() ) {
                    adaptor.addChild(root_1, stream_comb_attr.nextTree());

                }
                stream_comb_attr.reset();

                adaptor.addChild(root_1, stream_coll.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            else // 138:5: -> ^( EACH attribute ( comb_attr )? many coll )
            {
                // parser/flatzinc/FlatzincFullExtParser.g:138:21: ^( EACH attribute ( comb_attr )? many coll )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot(
                stream_EACH.nextNode()
                , root_1);

                adaptor.addChild(root_1, stream_attribute.nextTree());

                // parser/flatzinc/FlatzincFullExtParser.g:138:38: ( comb_attr )?
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
    // parser/flatzinc/FlatzincFullExtParser.g:144:1: coll : ( QUEUE LP ! qiter RP !| ( REV )? LIST LP ! liter RP !| ( MAX )? HEAP LP ! qiter RP !);
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
        Token MAX82=null;
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
        Object MAX82_tree=null;
        Object HEAP83_tree=null;
        Object LP84_tree=null;
        Object RP86_tree=null;

        try {
            // parser/flatzinc/FlatzincFullExtParser.g:145:5: ( QUEUE LP ! qiter RP !| ( REV )? LIST LP ! liter RP !| ( MAX )? HEAP LP ! qiter RP !)
            int alt22=3;
            switch ( input.LA(1) ) {
            case QUEUE:
                {
                alt22=1;
                }
                break;
            case LIST:
            case REV:
                {
                alt22=2;
                }
                break;
            case HEAP:
            case MAX:
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
                    // parser/flatzinc/FlatzincFullExtParser.g:145:7: QUEUE LP ! qiter RP !
                    {
                    root_0 = (Object)adaptor.nil();


                    QUEUE73=(Token)match(input,QUEUE,FOLLOW_QUEUE_in_coll716); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    QUEUE73_tree = 
                    (Object)adaptor.create(QUEUE73)
                    ;
                    adaptor.addChild(root_0, QUEUE73_tree);
                    }

                    LP74=(Token)match(input,LP,FOLLOW_LP_in_coll718); if (state.failed) return retval;

                    pushFollow(FOLLOW_qiter_in_coll721);
                    qiter75=qiter();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, qiter75.getTree());

                    RP76=(Token)match(input,RP,FOLLOW_RP_in_coll723); if (state.failed) return retval;

                    }
                    break;
                case 2 :
                    // parser/flatzinc/FlatzincFullExtParser.g:146:7: ( REV )? LIST LP ! liter RP !
                    {
                    root_0 = (Object)adaptor.nil();


                    // parser/flatzinc/FlatzincFullExtParser.g:146:7: ( REV )?
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
                            // parser/flatzinc/FlatzincFullExtParser.g:146:8: REV
                            {
                            REV77=(Token)match(input,REV,FOLLOW_REV_in_coll733); if (state.failed) return retval;
                            if ( state.backtracking==0 ) {
                            REV77_tree = 
                            (Object)adaptor.create(REV77)
                            ;
                            adaptor.addChild(root_0, REV77_tree);
                            }

                            }
                            break;

                    }


                    LIST78=(Token)match(input,LIST,FOLLOW_LIST_in_coll737); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LIST78_tree = 
                    (Object)adaptor.create(LIST78)
                    ;
                    adaptor.addChild(root_0, LIST78_tree);
                    }

                    LP79=(Token)match(input,LP,FOLLOW_LP_in_coll739); if (state.failed) return retval;

                    pushFollow(FOLLOW_liter_in_coll742);
                    liter80=liter();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, liter80.getTree());

                    RP81=(Token)match(input,RP,FOLLOW_RP_in_coll744); if (state.failed) return retval;

                    }
                    break;
                case 3 :
                    // parser/flatzinc/FlatzincFullExtParser.g:147:7: ( MAX )? HEAP LP ! qiter RP !
                    {
                    root_0 = (Object)adaptor.nil();


                    // parser/flatzinc/FlatzincFullExtParser.g:147:7: ( MAX )?
                    int alt21=2;
                    switch ( input.LA(1) ) {
                        case MAX:
                            {
                            alt21=1;
                            }
                            break;
                    }

                    switch (alt21) {
                        case 1 :
                            // parser/flatzinc/FlatzincFullExtParser.g:147:8: MAX
                            {
                            MAX82=(Token)match(input,MAX,FOLLOW_MAX_in_coll754); if (state.failed) return retval;
                            if ( state.backtracking==0 ) {
                            MAX82_tree = 
                            (Object)adaptor.create(MAX82)
                            ;
                            adaptor.addChild(root_0, MAX82_tree);
                            }

                            }
                            break;

                    }


                    HEAP83=(Token)match(input,HEAP,FOLLOW_HEAP_in_coll758); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    HEAP83_tree = 
                    (Object)adaptor.create(HEAP83)
                    ;
                    adaptor.addChild(root_0, HEAP83_tree);
                    }

                    LP84=(Token)match(input,LP,FOLLOW_LP_in_coll761); if (state.failed) return retval;

                    pushFollow(FOLLOW_qiter_in_coll764);
                    qiter85=qiter();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, qiter85.getTree());

                    RP86=(Token)match(input,RP,FOLLOW_RP_in_coll766); if (state.failed) return retval;

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
    // parser/flatzinc/FlatzincFullExtParser.g:150:1: qiter : ( ONE | WONE );
    public final FlatzincFullExtParser.qiter_return qiter() throws RecognitionException {
        FlatzincFullExtParser.qiter_return retval = new FlatzincFullExtParser.qiter_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token set87=null;

        Object set87_tree=null;

        try {
            // parser/flatzinc/FlatzincFullExtParser.g:151:5: ( ONE | WONE )
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
    // parser/flatzinc/FlatzincFullExtParser.g:155:1: liter : ( qiter | FOR | WFOR );
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
            // parser/flatzinc/FlatzincFullExtParser.g:156:5: ( qiter | FOR | WFOR )
            int alt23=3;
            switch ( input.LA(1) ) {
            case ONE:
            case WONE:
                {
                alt23=1;
                }
                break;
            case FOR:
                {
                alt23=2;
                }
                break;
            case WFOR:
                {
                alt23=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 23, 0, input);

                throw nvae;

            }

            switch (alt23) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtParser.g:156:7: qiter
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_qiter_in_liter808);
                    qiter88=qiter();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, qiter88.getTree());

                    }
                    break;
                case 2 :
                    // parser/flatzinc/FlatzincFullExtParser.g:157:9: FOR
                    {
                    root_0 = (Object)adaptor.nil();


                    FOR89=(Token)match(input,FOR,FOLLOW_FOR_in_liter818); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    FOR89_tree = 
                    (Object)adaptor.create(FOR89)
                    ;
                    adaptor.addChild(root_0, FOR89_tree);
                    }

                    }
                    break;
                case 3 :
                    // parser/flatzinc/FlatzincFullExtParser.g:158:9: WFOR
                    {
                    root_0 = (Object)adaptor.nil();


                    WFOR90=(Token)match(input,WFOR,FOLLOW_WFOR_in_liter828); if (state.failed) return retval;
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
    // parser/flatzinc/FlatzincFullExtParser.g:162:1: comb_attr : ( attr_op ( DO attr_op )* ( DO attribute )? -> ^( DO ( attr_op )* ( attribute )? ) | ( attr_op DO )* attribute -> ^( DO ( attr_op )* ( attribute )? ) );
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
            // parser/flatzinc/FlatzincFullExtParser.g:163:2: ( attr_op ( DO attr_op )* ( DO attribute )? -> ^( DO ( attr_op )* ( attribute )? ) | ( attr_op DO )* attribute -> ^( DO ( attr_op )* ( attribute )? ) )
            int alt27=2;
            alt27 = dfa27.predict(input);
            switch (alt27) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtParser.g:163:4: attr_op ( DO attr_op )* ( DO attribute )?
                    {
                    pushFollow(FOLLOW_attr_op_in_comb_attr843);
                    attr_op91=attr_op();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_attr_op.add(attr_op91.getTree());

                    // parser/flatzinc/FlatzincFullExtParser.g:163:12: ( DO attr_op )*
                    loop24:
                    do {
                        int alt24=2;
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
                                alt24=1;
                                }
                                break;

                            }

                            }
                            break;

                        }

                        switch (alt24) {
                    	case 1 :
                    	    // parser/flatzinc/FlatzincFullExtParser.g:163:13: DO attr_op
                    	    {
                    	    DO92=(Token)match(input,DO,FOLLOW_DO_in_comb_attr846); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_DO.add(DO92);


                    	    pushFollow(FOLLOW_attr_op_in_comb_attr848);
                    	    attr_op93=attr_op();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_attr_op.add(attr_op93.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop24;
                        }
                    } while (true);


                    // parser/flatzinc/FlatzincFullExtParser.g:163:27: ( DO attribute )?
                    int alt25=2;
                    switch ( input.LA(1) ) {
                        case DO:
                            {
                            alt25=1;
                            }
                            break;
                    }

                    switch (alt25) {
                        case 1 :
                            // parser/flatzinc/FlatzincFullExtParser.g:163:28: DO attribute
                            {
                            DO94=(Token)match(input,DO,FOLLOW_DO_in_comb_attr854); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_DO.add(DO94);


                            pushFollow(FOLLOW_attribute_in_comb_attr856);
                            attribute95=attribute();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_attribute.add(attribute95.getTree());

                            }
                            break;

                    }


                    // AST REWRITE
                    // elements: attr_op, DO, attribute
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
                case 2 :
                    // parser/flatzinc/FlatzincFullExtParser.g:165:6: ( attr_op DO )* attribute
                    {
                    // parser/flatzinc/FlatzincFullExtParser.g:165:6: ( attr_op DO )*
                    loop26:
                    do {
                        int alt26=2;
                        switch ( input.LA(1) ) {
                        case ANY:
                        case MAX:
                        case MIN:
                        case SIZE:
                        case SUM:
                            {
                            alt26=1;
                            }
                            break;

                        }

                        switch (alt26) {
                    	case 1 :
                    	    // parser/flatzinc/FlatzincFullExtParser.g:165:7: attr_op DO
                    	    {
                    	    pushFollow(FOLLOW_attr_op_in_comb_attr880);
                    	    attr_op96=attr_op();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_attr_op.add(attr_op96.getTree());

                    	    DO97=(Token)match(input,DO,FOLLOW_DO_in_comb_attr882); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_DO.add(DO97);


                    	    }
                    	    break;

                    	default :
                    	    break loop26;
                        }
                    } while (true);


                    pushFollow(FOLLOW_attribute_in_comb_attr886);
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
                    // 166:2: -> ^( DO ( attr_op )* ( attribute )? )
                    {
                        // parser/flatzinc/FlatzincFullExtParser.g:166:6: ^( DO ( attr_op )* ( attribute )? )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot(
                        stream_DO.nextNode()
                        , root_1);

                        // parser/flatzinc/FlatzincFullExtParser.g:166:11: ( attr_op )*
                        while ( stream_attr_op.hasNext() ) {
                            adaptor.addChild(root_1, stream_attr_op.nextTree());

                        }
                        stream_attr_op.reset();

                        // parser/flatzinc/FlatzincFullExtParser.g:166:20: ( attribute )?
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
    // parser/flatzinc/FlatzincFullExtParser.g:169:1: attr_op : ( ANY | MIN | MAX | SUM | SIZE );
    public final FlatzincFullExtParser.attr_op_return attr_op() throws RecognitionException {
        FlatzincFullExtParser.attr_op_return retval = new FlatzincFullExtParser.attr_op_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token set99=null;

        Object set99_tree=null;

        try {
            // parser/flatzinc/FlatzincFullExtParser.g:170:5: ( ANY | MIN | MAX | SUM | SIZE )
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
    // parser/flatzinc/FlatzincFullExtParser.g:184:1: pred_decl : PREDICATE IDENTIFIER LP pred_param ( CM pred_param )* RP SC -> ^( PREDICATE IDENTIFIER ( pred_param )+ ) ;
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
            // parser/flatzinc/FlatzincFullExtParser.g:185:2: ( PREDICATE IDENTIFIER LP pred_param ( CM pred_param )* RP SC -> ^( PREDICATE IDENTIFIER ( pred_param )+ ) )
            // parser/flatzinc/FlatzincFullExtParser.g:185:6: PREDICATE IDENTIFIER LP pred_param ( CM pred_param )* RP SC
            {
            PREDICATE100=(Token)match(input,PREDICATE,FOLLOW_PREDICATE_in_pred_decl974); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_PREDICATE.add(PREDICATE100);


            IDENTIFIER101=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_pred_decl976); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_IDENTIFIER.add(IDENTIFIER101);


            LP102=(Token)match(input,LP,FOLLOW_LP_in_pred_decl978); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LP.add(LP102);


            pushFollow(FOLLOW_pred_param_in_pred_decl980);
            pred_param103=pred_param();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_pred_param.add(pred_param103.getTree());

            // parser/flatzinc/FlatzincFullExtParser.g:185:41: ( CM pred_param )*
            loop28:
            do {
                int alt28=2;
                switch ( input.LA(1) ) {
                case CM:
                    {
                    alt28=1;
                    }
                    break;

                }

                switch (alt28) {
            	case 1 :
            	    // parser/flatzinc/FlatzincFullExtParser.g:185:42: CM pred_param
            	    {
            	    CM104=(Token)match(input,CM,FOLLOW_CM_in_pred_decl983); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_CM.add(CM104);


            	    pushFollow(FOLLOW_pred_param_in_pred_decl985);
            	    pred_param105=pred_param();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_pred_param.add(pred_param105.getTree());

            	    }
            	    break;

            	default :
            	    break loop28;
                }
            } while (true);


            RP106=(Token)match(input,RP,FOLLOW_RP_in_pred_decl989); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RP.add(RP106);


            SC107=(Token)match(input,SC,FOLLOW_SC_in_pred_decl991); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_SC.add(SC107);


            // AST REWRITE
            // elements: PREDICATE, IDENTIFIER, pred_param
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {

            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 186:2: -> ^( PREDICATE IDENTIFIER ( pred_param )+ )
            {
                // parser/flatzinc/FlatzincFullExtParser.g:186:5: ^( PREDICATE IDENTIFIER ( pred_param )+ )
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
    // parser/flatzinc/FlatzincFullExtParser.g:189:1: pred_param : pred_param_type CL IDENTIFIER -> ^( CL pred_param_type IDENTIFIER ) ;
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
            // parser/flatzinc/FlatzincFullExtParser.g:190:5: ( pred_param_type CL IDENTIFIER -> ^( CL pred_param_type IDENTIFIER ) )
            // parser/flatzinc/FlatzincFullExtParser.g:190:9: pred_param_type CL IDENTIFIER
            {
            pushFollow(FOLLOW_pred_param_type_in_pred_param1019);
            pred_param_type108=pred_param_type();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_pred_param_type.add(pred_param_type108.getTree());

            CL109=(Token)match(input,CL,FOLLOW_CL_in_pred_param1021); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_CL.add(CL109);


            IDENTIFIER110=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_pred_param1023); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_IDENTIFIER.add(IDENTIFIER110);


            // AST REWRITE
            // elements: pred_param_type, IDENTIFIER, CL
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {

            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 191:5: -> ^( CL pred_param_type IDENTIFIER )
            {
                // parser/flatzinc/FlatzincFullExtParser.g:191:9: ^( CL pred_param_type IDENTIFIER )
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
    // parser/flatzinc/FlatzincFullExtParser.g:194:1: pred_param_type : ( par_pred_param_type | var_pred_param_type );
    public final FlatzincFullExtParser.pred_param_type_return pred_param_type() throws RecognitionException {
        FlatzincFullExtParser.pred_param_type_return retval = new FlatzincFullExtParser.pred_param_type_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        FlatzincFullExtParser.par_pred_param_type_return par_pred_param_type111 =null;

        FlatzincFullExtParser.var_pred_param_type_return var_pred_param_type112 =null;



        try {
            // parser/flatzinc/FlatzincFullExtParser.g:195:5: ( par_pred_param_type | var_pred_param_type )
            int alt29=2;
            alt29 = dfa29.predict(input);
            switch (alt29) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtParser.g:195:9: par_pred_param_type
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_par_pred_param_type_in_pred_param_type1057);
                    par_pred_param_type111=par_pred_param_type();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, par_pred_param_type111.getTree());

                    }
                    break;
                case 2 :
                    // parser/flatzinc/FlatzincFullExtParser.g:196:9: var_pred_param_type
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_var_pred_param_type_in_pred_param_type1067);
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
    // parser/flatzinc/FlatzincFullExtParser.g:199:1: par_type : ( ARRAY LS index_set ( CM index_set )* RS OF par_type_u -> ^( ARRPAR ( index_set )+ par_type_u ) | par_type_u -> ^( APAR par_type_u ) );
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
            // parser/flatzinc/FlatzincFullExtParser.g:200:5: ( ARRAY LS index_set ( CM index_set )* RS OF par_type_u -> ^( ARRPAR ( index_set )+ par_type_u ) | par_type_u -> ^( APAR par_type_u ) )
            int alt31=2;
            switch ( input.LA(1) ) {
            case ARRAY:
                {
                alt31=1;
                }
                break;
            case BOOL:
            case FLOAT:
            case INT:
            case SET:
                {
                alt31=2;
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
                    // parser/flatzinc/FlatzincFullExtParser.g:200:9: ARRAY LS index_set ( CM index_set )* RS OF par_type_u
                    {
                    ARRAY113=(Token)match(input,ARRAY,FOLLOW_ARRAY_in_par_type1086); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ARRAY.add(ARRAY113);


                    LS114=(Token)match(input,LS,FOLLOW_LS_in_par_type1088); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LS.add(LS114);


                    pushFollow(FOLLOW_index_set_in_par_type1090);
                    index_set115=index_set();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_index_set.add(index_set115.getTree());

                    // parser/flatzinc/FlatzincFullExtParser.g:200:28: ( CM index_set )*
                    loop30:
                    do {
                        int alt30=2;
                        switch ( input.LA(1) ) {
                        case CM:
                            {
                            alt30=1;
                            }
                            break;

                        }

                        switch (alt30) {
                    	case 1 :
                    	    // parser/flatzinc/FlatzincFullExtParser.g:200:29: CM index_set
                    	    {
                    	    CM116=(Token)match(input,CM,FOLLOW_CM_in_par_type1093); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_CM.add(CM116);


                    	    pushFollow(FOLLOW_index_set_in_par_type1095);
                    	    index_set117=index_set();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_index_set.add(index_set117.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop30;
                        }
                    } while (true);


                    RS118=(Token)match(input,RS,FOLLOW_RS_in_par_type1099); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RS.add(RS118);


                    OF119=(Token)match(input,OF,FOLLOW_OF_in_par_type1101); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_OF.add(OF119);


                    pushFollow(FOLLOW_par_type_u_in_par_type1103);
                    par_type_u120=par_type_u();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_par_type_u.add(par_type_u120.getTree());

                    // AST REWRITE
                    // elements: index_set, par_type_u
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {

                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 201:5: -> ^( ARRPAR ( index_set )+ par_type_u )
                    {
                        // parser/flatzinc/FlatzincFullExtParser.g:201:9: ^( ARRPAR ( index_set )+ par_type_u )
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
                    // parser/flatzinc/FlatzincFullExtParser.g:202:9: par_type_u
                    {
                    pushFollow(FOLLOW_par_type_u_in_par_type1129);
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
                    // 203:5: -> ^( APAR par_type_u )
                    {
                        // parser/flatzinc/FlatzincFullExtParser.g:203:9: ^( APAR par_type_u )
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
    // parser/flatzinc/FlatzincFullExtParser.g:206:1: par_type_u : ( BOOL | FLOAT | SET OF INT | INT );
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
            // parser/flatzinc/FlatzincFullExtParser.g:207:5: ( BOOL | FLOAT | SET OF INT | INT )
            int alt32=4;
            switch ( input.LA(1) ) {
            case BOOL:
                {
                alt32=1;
                }
                break;
            case FLOAT:
                {
                alt32=2;
                }
                break;
            case SET:
                {
                alt32=3;
                }
                break;
            case INT:
                {
                alt32=4;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 32, 0, input);

                throw nvae;

            }

            switch (alt32) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtParser.g:207:9: BOOL
                    {
                    root_0 = (Object)adaptor.nil();


                    BOOL122=(Token)match(input,BOOL,FOLLOW_BOOL_in_par_type_u1161); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    BOOL122_tree = 
                    (Object)adaptor.create(BOOL122)
                    ;
                    adaptor.addChild(root_0, BOOL122_tree);
                    }

                    }
                    break;
                case 2 :
                    // parser/flatzinc/FlatzincFullExtParser.g:208:9: FLOAT
                    {
                    root_0 = (Object)adaptor.nil();


                    FLOAT123=(Token)match(input,FLOAT,FOLLOW_FLOAT_in_par_type_u1171); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    FLOAT123_tree = 
                    (Object)adaptor.create(FLOAT123)
                    ;
                    adaptor.addChild(root_0, FLOAT123_tree);
                    }

                    }
                    break;
                case 3 :
                    // parser/flatzinc/FlatzincFullExtParser.g:209:9: SET OF INT
                    {
                    root_0 = (Object)adaptor.nil();


                    SET124=(Token)match(input,SET,FOLLOW_SET_in_par_type_u1181); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    SET124_tree = 
                    (Object)adaptor.create(SET124)
                    ;
                    adaptor.addChild(root_0, SET124_tree);
                    }

                    OF125=(Token)match(input,OF,FOLLOW_OF_in_par_type_u1183); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    OF125_tree = 
                    (Object)adaptor.create(OF125)
                    ;
                    adaptor.addChild(root_0, OF125_tree);
                    }

                    INT126=(Token)match(input,INT,FOLLOW_INT_in_par_type_u1185); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    INT126_tree = 
                    (Object)adaptor.create(INT126)
                    ;
                    adaptor.addChild(root_0, INT126_tree);
                    }

                    }
                    break;
                case 4 :
                    // parser/flatzinc/FlatzincFullExtParser.g:210:9: INT
                    {
                    root_0 = (Object)adaptor.nil();


                    INT127=(Token)match(input,INT,FOLLOW_INT_in_par_type_u1195); if (state.failed) return retval;
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
    // parser/flatzinc/FlatzincFullExtParser.g:213:1: var_type : ( ARRAY LS index_set ( CM index_set )* RS OF VAR var_type_u -> ^( ARRVAR ( index_set )+ var_type_u ) | VAR var_type_u -> ^( AVAR var_type_u ) );
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
            // parser/flatzinc/FlatzincFullExtParser.g:214:5: ( ARRAY LS index_set ( CM index_set )* RS OF VAR var_type_u -> ^( ARRVAR ( index_set )+ var_type_u ) | VAR var_type_u -> ^( AVAR var_type_u ) )
            int alt34=2;
            switch ( input.LA(1) ) {
            case ARRAY:
                {
                alt34=1;
                }
                break;
            case VAR:
                {
                alt34=2;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 34, 0, input);

                throw nvae;

            }

            switch (alt34) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtParser.g:214:9: ARRAY LS index_set ( CM index_set )* RS OF VAR var_type_u
                    {
                    ARRAY128=(Token)match(input,ARRAY,FOLLOW_ARRAY_in_var_type1214); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ARRAY.add(ARRAY128);


                    LS129=(Token)match(input,LS,FOLLOW_LS_in_var_type1216); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LS.add(LS129);


                    pushFollow(FOLLOW_index_set_in_var_type1218);
                    index_set130=index_set();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_index_set.add(index_set130.getTree());

                    // parser/flatzinc/FlatzincFullExtParser.g:214:28: ( CM index_set )*
                    loop33:
                    do {
                        int alt33=2;
                        switch ( input.LA(1) ) {
                        case CM:
                            {
                            alt33=1;
                            }
                            break;

                        }

                        switch (alt33) {
                    	case 1 :
                    	    // parser/flatzinc/FlatzincFullExtParser.g:214:29: CM index_set
                    	    {
                    	    CM131=(Token)match(input,CM,FOLLOW_CM_in_var_type1221); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_CM.add(CM131);


                    	    pushFollow(FOLLOW_index_set_in_var_type1223);
                    	    index_set132=index_set();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_index_set.add(index_set132.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop33;
                        }
                    } while (true);


                    RS133=(Token)match(input,RS,FOLLOW_RS_in_var_type1227); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RS.add(RS133);


                    OF134=(Token)match(input,OF,FOLLOW_OF_in_var_type1229); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_OF.add(OF134);


                    VAR135=(Token)match(input,VAR,FOLLOW_VAR_in_var_type1231); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_VAR.add(VAR135);


                    pushFollow(FOLLOW_var_type_u_in_var_type1233);
                    var_type_u136=var_type_u();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_var_type_u.add(var_type_u136.getTree());

                    // AST REWRITE
                    // elements: index_set, var_type_u
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {

                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 215:5: -> ^( ARRVAR ( index_set )+ var_type_u )
                    {
                        // parser/flatzinc/FlatzincFullExtParser.g:215:9: ^( ARRVAR ( index_set )+ var_type_u )
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
                    // parser/flatzinc/FlatzincFullExtParser.g:216:9: VAR var_type_u
                    {
                    VAR137=(Token)match(input,VAR,FOLLOW_VAR_in_var_type1259); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_VAR.add(VAR137);


                    pushFollow(FOLLOW_var_type_u_in_var_type1261);
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
                    // 217:5: -> ^( AVAR var_type_u )
                    {
                        // parser/flatzinc/FlatzincFullExtParser.g:217:9: ^( AVAR var_type_u )
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
    // parser/flatzinc/FlatzincFullExtParser.g:220:1: var_type_u : ( BOOL | FLOAT | INT | INT_CONST DD INT_CONST -> ^( DD INT_CONST INT_CONST ) | LB INT_CONST ( CM INT_CONST )* RB -> ^( CM ( INT_CONST )+ ) | SET OF INT_CONST DD INT_CONST -> ^( SET ^( DD INT_CONST INT_CONST ) ) | SET OF LB INT_CONST ( CM INT_CONST )* RB -> ^( SET ^( CM INT_CONST INT_CONST ) ) );
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
            // parser/flatzinc/FlatzincFullExtParser.g:221:5: ( BOOL | FLOAT | INT | INT_CONST DD INT_CONST -> ^( DD INT_CONST INT_CONST ) | LB INT_CONST ( CM INT_CONST )* RB -> ^( CM ( INT_CONST )+ ) | SET OF INT_CONST DD INT_CONST -> ^( SET ^( DD INT_CONST INT_CONST ) ) | SET OF LB INT_CONST ( CM INT_CONST )* RB -> ^( SET ^( CM INT_CONST INT_CONST ) ) )
            int alt37=7;
            switch ( input.LA(1) ) {
            case BOOL:
                {
                alt37=1;
                }
                break;
            case FLOAT:
                {
                alt37=2;
                }
                break;
            case INT:
                {
                alt37=3;
                }
                break;
            case INT_CONST:
                {
                alt37=4;
                }
                break;
            case LB:
                {
                alt37=5;
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
                        alt37=6;
                        }
                        break;
                    case LB:
                        {
                        alt37=7;
                        }
                        break;
                    default:
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 37, 7, input);

                        throw nvae;

                    }

                    }
                    break;
                default:
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 37, 6, input);

                    throw nvae;

                }

                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 37, 0, input);

                throw nvae;

            }

            switch (alt37) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtParser.g:221:9: BOOL
                    {
                    root_0 = (Object)adaptor.nil();


                    BOOL139=(Token)match(input,BOOL,FOLLOW_BOOL_in_var_type_u1293); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    BOOL139_tree = 
                    (Object)adaptor.create(BOOL139)
                    ;
                    adaptor.addChild(root_0, BOOL139_tree);
                    }

                    }
                    break;
                case 2 :
                    // parser/flatzinc/FlatzincFullExtParser.g:222:9: FLOAT
                    {
                    root_0 = (Object)adaptor.nil();


                    FLOAT140=(Token)match(input,FLOAT,FOLLOW_FLOAT_in_var_type_u1303); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    FLOAT140_tree = 
                    (Object)adaptor.create(FLOAT140)
                    ;
                    adaptor.addChild(root_0, FLOAT140_tree);
                    }

                    }
                    break;
                case 3 :
                    // parser/flatzinc/FlatzincFullExtParser.g:223:9: INT
                    {
                    root_0 = (Object)adaptor.nil();


                    INT141=(Token)match(input,INT,FOLLOW_INT_in_var_type_u1313); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    INT141_tree = 
                    (Object)adaptor.create(INT141)
                    ;
                    adaptor.addChild(root_0, INT141_tree);
                    }

                    }
                    break;
                case 4 :
                    // parser/flatzinc/FlatzincFullExtParser.g:224:9: INT_CONST DD INT_CONST
                    {
                    INT_CONST142=(Token)match(input,INT_CONST,FOLLOW_INT_CONST_in_var_type_u1323); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_INT_CONST.add(INT_CONST142);


                    DD143=(Token)match(input,DD,FOLLOW_DD_in_var_type_u1325); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_DD.add(DD143);


                    INT_CONST144=(Token)match(input,INT_CONST,FOLLOW_INT_CONST_in_var_type_u1327); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_INT_CONST.add(INT_CONST144);


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
                    // 225:5: -> ^( DD INT_CONST INT_CONST )
                    {
                        // parser/flatzinc/FlatzincFullExtParser.g:225:9: ^( DD INT_CONST INT_CONST )
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
                    // parser/flatzinc/FlatzincFullExtParser.g:228:9: LB INT_CONST ( CM INT_CONST )* RB
                    {
                    LB145=(Token)match(input,LB,FOLLOW_LB_in_var_type_u1354); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LB.add(LB145);


                    INT_CONST146=(Token)match(input,INT_CONST,FOLLOW_INT_CONST_in_var_type_u1356); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_INT_CONST.add(INT_CONST146);


                    // parser/flatzinc/FlatzincFullExtParser.g:228:22: ( CM INT_CONST )*
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
                    	    // parser/flatzinc/FlatzincFullExtParser.g:228:23: CM INT_CONST
                    	    {
                    	    CM147=(Token)match(input,CM,FOLLOW_CM_in_var_type_u1359); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_CM.add(CM147);


                    	    INT_CONST148=(Token)match(input,INT_CONST,FOLLOW_INT_CONST_in_var_type_u1361); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_INT_CONST.add(INT_CONST148);


                    	    }
                    	    break;

                    	default :
                    	    break loop35;
                        }
                    } while (true);


                    RB149=(Token)match(input,RB,FOLLOW_RB_in_var_type_u1365); if (state.failed) return retval; 
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
                    // 229:5: -> ^( CM ( INT_CONST )+ )
                    {
                        // parser/flatzinc/FlatzincFullExtParser.g:229:9: ^( CM ( INT_CONST )+ )
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
                    // parser/flatzinc/FlatzincFullExtParser.g:230:9: SET OF INT_CONST DD INT_CONST
                    {
                    SET150=(Token)match(input,SET,FOLLOW_SET_in_var_type_u1389); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_SET.add(SET150);


                    OF151=(Token)match(input,OF,FOLLOW_OF_in_var_type_u1391); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_OF.add(OF151);


                    INT_CONST152=(Token)match(input,INT_CONST,FOLLOW_INT_CONST_in_var_type_u1393); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_INT_CONST.add(INT_CONST152);


                    DD153=(Token)match(input,DD,FOLLOW_DD_in_var_type_u1395); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_DD.add(DD153);


                    INT_CONST154=(Token)match(input,INT_CONST,FOLLOW_INT_CONST_in_var_type_u1397); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_INT_CONST.add(INT_CONST154);


                    // AST REWRITE
                    // elements: DD, INT_CONST, SET, INT_CONST
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {

                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 231:5: -> ^( SET ^( DD INT_CONST INT_CONST ) )
                    {
                        // parser/flatzinc/FlatzincFullExtParser.g:231:9: ^( SET ^( DD INT_CONST INT_CONST ) )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot(
                        stream_SET.nextNode()
                        , root_1);

                        // parser/flatzinc/FlatzincFullExtParser.g:231:15: ^( DD INT_CONST INT_CONST )
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
                    // parser/flatzinc/FlatzincFullExtParser.g:232:9: SET OF LB INT_CONST ( CM INT_CONST )* RB
                    {
                    SET155=(Token)match(input,SET,FOLLOW_SET_in_var_type_u1426); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_SET.add(SET155);


                    OF156=(Token)match(input,OF,FOLLOW_OF_in_var_type_u1428); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_OF.add(OF156);


                    LB157=(Token)match(input,LB,FOLLOW_LB_in_var_type_u1430); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LB.add(LB157);


                    INT_CONST158=(Token)match(input,INT_CONST,FOLLOW_INT_CONST_in_var_type_u1432); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_INT_CONST.add(INT_CONST158);


                    // parser/flatzinc/FlatzincFullExtParser.g:232:29: ( CM INT_CONST )*
                    loop36:
                    do {
                        int alt36=2;
                        switch ( input.LA(1) ) {
                        case CM:
                            {
                            alt36=1;
                            }
                            break;

                        }

                        switch (alt36) {
                    	case 1 :
                    	    // parser/flatzinc/FlatzincFullExtParser.g:232:30: CM INT_CONST
                    	    {
                    	    CM159=(Token)match(input,CM,FOLLOW_CM_in_var_type_u1435); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_CM.add(CM159);


                    	    INT_CONST160=(Token)match(input,INT_CONST,FOLLOW_INT_CONST_in_var_type_u1437); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_INT_CONST.add(INT_CONST160);


                    	    }
                    	    break;

                    	default :
                    	    break loop36;
                        }
                    } while (true);


                    RB161=(Token)match(input,RB,FOLLOW_RB_in_var_type_u1441); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RB.add(RB161);


                    // AST REWRITE
                    // elements: CM, SET, INT_CONST, INT_CONST
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {

                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 233:5: -> ^( SET ^( CM INT_CONST INT_CONST ) )
                    {
                        // parser/flatzinc/FlatzincFullExtParser.g:233:9: ^( SET ^( CM INT_CONST INT_CONST ) )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot(
                        stream_SET.nextNode()
                        , root_1);

                        // parser/flatzinc/FlatzincFullExtParser.g:233:15: ^( CM INT_CONST INT_CONST )
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
    // parser/flatzinc/FlatzincFullExtParser.g:236:1: par_pred_param_type : ( par_type | INT_CONST DD INT_CONST -> ^( DD INT_CONST INT_CONST ) | LB INT_CONST ( CM INT_CONST )* RB -> ^( CM ( INT_CONST )+ ) | SET OF INT_CONST DD INT_CONST -> ^( SET ^( DD INT_CONST INT_CONST ) ) | SET OF LB INT_CONST ( CM INT_CONST )* RB -> ^( SET ^( CM ( INT_CONST )+ ) ) | ARRAY LS index_set ( CM index_set )* RS OF INT_CONST DD INT_CONST -> ^( ARRAY ( index_set )+ ^( DD INT_CONST INT_CONST ) ) | ARRAY LS index_set ( CM index_set )* RS OF LB INT_CONST ( CM INT_CONST )* RB -> ^( ARRAY ( index_set )+ ^( CM ( INT_CONST )+ ) ) | ARRAY LS index_set ( CM index_set )* RS OF SET OF INT_CONST DD INT_CONST -> ^( ARRAY ( index_set )+ ^( SET ^( DD INT_CONST INT_CONST ) ) ) | ARRAY LS index_set ( CM index_set )* RS OF SET OF LB INT_CONST ( CM INT_CONST )* RB -> ^( ARRAY ( index_set )+ ^( SET ^( CM ( INT_CONST )+ ) ) ) );
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
            // parser/flatzinc/FlatzincFullExtParser.g:237:5: ( par_type | INT_CONST DD INT_CONST -> ^( DD INT_CONST INT_CONST ) | LB INT_CONST ( CM INT_CONST )* RB -> ^( CM ( INT_CONST )+ ) | SET OF INT_CONST DD INT_CONST -> ^( SET ^( DD INT_CONST INT_CONST ) ) | SET OF LB INT_CONST ( CM INT_CONST )* RB -> ^( SET ^( CM ( INT_CONST )+ ) ) | ARRAY LS index_set ( CM index_set )* RS OF INT_CONST DD INT_CONST -> ^( ARRAY ( index_set )+ ^( DD INT_CONST INT_CONST ) ) | ARRAY LS index_set ( CM index_set )* RS OF LB INT_CONST ( CM INT_CONST )* RB -> ^( ARRAY ( index_set )+ ^( CM ( INT_CONST )+ ) ) | ARRAY LS index_set ( CM index_set )* RS OF SET OF INT_CONST DD INT_CONST -> ^( ARRAY ( index_set )+ ^( SET ^( DD INT_CONST INT_CONST ) ) ) | ARRAY LS index_set ( CM index_set )* RS OF SET OF LB INT_CONST ( CM INT_CONST )* RB -> ^( ARRAY ( index_set )+ ^( SET ^( CM ( INT_CONST )+ ) ) ) )
            int alt46=9;
            alt46 = dfa46.predict(input);
            switch (alt46) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtParser.g:237:9: par_type
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_par_type_in_par_pred_param_type1479);
                    par_type162=par_type();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, par_type162.getTree());

                    }
                    break;
                case 2 :
                    // parser/flatzinc/FlatzincFullExtParser.g:240:9: INT_CONST DD INT_CONST
                    {
                    INT_CONST163=(Token)match(input,INT_CONST,FOLLOW_INT_CONST_in_par_pred_param_type1491); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_INT_CONST.add(INT_CONST163);


                    DD164=(Token)match(input,DD,FOLLOW_DD_in_par_pred_param_type1493); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_DD.add(DD164);


                    INT_CONST165=(Token)match(input,INT_CONST,FOLLOW_INT_CONST_in_par_pred_param_type1495); if (state.failed) return retval; 
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
                    // 241:5: -> ^( DD INT_CONST INT_CONST )
                    {
                        // parser/flatzinc/FlatzincFullExtParser.g:241:9: ^( DD INT_CONST INT_CONST )
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
                    // parser/flatzinc/FlatzincFullExtParser.g:242:9: LB INT_CONST ( CM INT_CONST )* RB
                    {
                    LB166=(Token)match(input,LB,FOLLOW_LB_in_par_pred_param_type1520); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LB.add(LB166);


                    INT_CONST167=(Token)match(input,INT_CONST,FOLLOW_INT_CONST_in_par_pred_param_type1522); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_INT_CONST.add(INT_CONST167);


                    // parser/flatzinc/FlatzincFullExtParser.g:242:22: ( CM INT_CONST )*
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
                    	    // parser/flatzinc/FlatzincFullExtParser.g:242:23: CM INT_CONST
                    	    {
                    	    CM168=(Token)match(input,CM,FOLLOW_CM_in_par_pred_param_type1525); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_CM.add(CM168);


                    	    INT_CONST169=(Token)match(input,INT_CONST,FOLLOW_INT_CONST_in_par_pred_param_type1527); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_INT_CONST.add(INT_CONST169);


                    	    }
                    	    break;

                    	default :
                    	    break loop38;
                        }
                    } while (true);


                    RB170=(Token)match(input,RB,FOLLOW_RB_in_par_pred_param_type1531); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RB.add(RB170);


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
                    // 243:5: -> ^( CM ( INT_CONST )+ )
                    {
                        // parser/flatzinc/FlatzincFullExtParser.g:243:9: ^( CM ( INT_CONST )+ )
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
                    // parser/flatzinc/FlatzincFullExtParser.g:244:9: SET OF INT_CONST DD INT_CONST
                    {
                    SET171=(Token)match(input,SET,FOLLOW_SET_in_par_pred_param_type1555); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_SET.add(SET171);


                    OF172=(Token)match(input,OF,FOLLOW_OF_in_par_pred_param_type1557); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_OF.add(OF172);


                    INT_CONST173=(Token)match(input,INT_CONST,FOLLOW_INT_CONST_in_par_pred_param_type1559); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_INT_CONST.add(INT_CONST173);


                    DD174=(Token)match(input,DD,FOLLOW_DD_in_par_pred_param_type1561); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_DD.add(DD174);


                    INT_CONST175=(Token)match(input,INT_CONST,FOLLOW_INT_CONST_in_par_pred_param_type1563); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_INT_CONST.add(INT_CONST175);


                    // AST REWRITE
                    // elements: SET, INT_CONST, DD, INT_CONST
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {

                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 245:5: -> ^( SET ^( DD INT_CONST INT_CONST ) )
                    {
                        // parser/flatzinc/FlatzincFullExtParser.g:245:9: ^( SET ^( DD INT_CONST INT_CONST ) )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot(
                        stream_SET.nextNode()
                        , root_1);

                        // parser/flatzinc/FlatzincFullExtParser.g:245:15: ^( DD INT_CONST INT_CONST )
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
                    // parser/flatzinc/FlatzincFullExtParser.g:246:9: SET OF LB INT_CONST ( CM INT_CONST )* RB
                    {
                    SET176=(Token)match(input,SET,FOLLOW_SET_in_par_pred_param_type1592); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_SET.add(SET176);


                    OF177=(Token)match(input,OF,FOLLOW_OF_in_par_pred_param_type1594); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_OF.add(OF177);


                    LB178=(Token)match(input,LB,FOLLOW_LB_in_par_pred_param_type1596); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LB.add(LB178);


                    INT_CONST179=(Token)match(input,INT_CONST,FOLLOW_INT_CONST_in_par_pred_param_type1598); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_INT_CONST.add(INT_CONST179);


                    // parser/flatzinc/FlatzincFullExtParser.g:246:29: ( CM INT_CONST )*
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
                    	    // parser/flatzinc/FlatzincFullExtParser.g:246:30: CM INT_CONST
                    	    {
                    	    CM180=(Token)match(input,CM,FOLLOW_CM_in_par_pred_param_type1601); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_CM.add(CM180);


                    	    INT_CONST181=(Token)match(input,INT_CONST,FOLLOW_INT_CONST_in_par_pred_param_type1603); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_INT_CONST.add(INT_CONST181);


                    	    }
                    	    break;

                    	default :
                    	    break loop39;
                        }
                    } while (true);


                    RB182=(Token)match(input,RB,FOLLOW_RB_in_par_pred_param_type1607); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RB.add(RB182);


                    // AST REWRITE
                    // elements: CM, SET, INT_CONST
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {

                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 247:5: -> ^( SET ^( CM ( INT_CONST )+ ) )
                    {
                        // parser/flatzinc/FlatzincFullExtParser.g:247:9: ^( SET ^( CM ( INT_CONST )+ ) )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot(
                        stream_SET.nextNode()
                        , root_1);

                        // parser/flatzinc/FlatzincFullExtParser.g:247:15: ^( CM ( INT_CONST )+ )
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
                    // parser/flatzinc/FlatzincFullExtParser.g:250:9: ARRAY LS index_set ( CM index_set )* RS OF INT_CONST DD INT_CONST
                    {
                    ARRAY183=(Token)match(input,ARRAY,FOLLOW_ARRAY_in_par_pred_param_type1637); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ARRAY.add(ARRAY183);


                    LS184=(Token)match(input,LS,FOLLOW_LS_in_par_pred_param_type1639); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LS.add(LS184);


                    pushFollow(FOLLOW_index_set_in_par_pred_param_type1641);
                    index_set185=index_set();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_index_set.add(index_set185.getTree());

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
                    	    CM186=(Token)match(input,CM,FOLLOW_CM_in_par_pred_param_type1644); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_CM.add(CM186);


                    	    pushFollow(FOLLOW_index_set_in_par_pred_param_type1646);
                    	    index_set187=index_set();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_index_set.add(index_set187.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop40;
                        }
                    } while (true);


                    RS188=(Token)match(input,RS,FOLLOW_RS_in_par_pred_param_type1650); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RS.add(RS188);


                    OF189=(Token)match(input,OF,FOLLOW_OF_in_par_pred_param_type1652); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_OF.add(OF189);


                    INT_CONST190=(Token)match(input,INT_CONST,FOLLOW_INT_CONST_in_par_pred_param_type1654); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_INT_CONST.add(INT_CONST190);


                    DD191=(Token)match(input,DD,FOLLOW_DD_in_par_pred_param_type1656); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_DD.add(DD191);


                    INT_CONST192=(Token)match(input,INT_CONST,FOLLOW_INT_CONST_in_par_pred_param_type1658); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_INT_CONST.add(INT_CONST192);


                    // AST REWRITE
                    // elements: INT_CONST, DD, INT_CONST, ARRAY, index_set
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {

                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 251:5: -> ^( ARRAY ( index_set )+ ^( DD INT_CONST INT_CONST ) )
                    {
                        // parser/flatzinc/FlatzincFullExtParser.g:251:9: ^( ARRAY ( index_set )+ ^( DD INT_CONST INT_CONST ) )
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

                        // parser/flatzinc/FlatzincFullExtParser.g:251:28: ^( DD INT_CONST INT_CONST )
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
                    // parser/flatzinc/FlatzincFullExtParser.g:252:9: ARRAY LS index_set ( CM index_set )* RS OF LB INT_CONST ( CM INT_CONST )* RB
                    {
                    ARRAY193=(Token)match(input,ARRAY,FOLLOW_ARRAY_in_par_pred_param_type1690); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ARRAY.add(ARRAY193);


                    LS194=(Token)match(input,LS,FOLLOW_LS_in_par_pred_param_type1692); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LS.add(LS194);


                    pushFollow(FOLLOW_index_set_in_par_pred_param_type1694);
                    index_set195=index_set();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_index_set.add(index_set195.getTree());

                    // parser/flatzinc/FlatzincFullExtParser.g:252:28: ( CM index_set )*
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
                    	    // parser/flatzinc/FlatzincFullExtParser.g:252:29: CM index_set
                    	    {
                    	    CM196=(Token)match(input,CM,FOLLOW_CM_in_par_pred_param_type1697); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_CM.add(CM196);


                    	    pushFollow(FOLLOW_index_set_in_par_pred_param_type1699);
                    	    index_set197=index_set();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_index_set.add(index_set197.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop41;
                        }
                    } while (true);


                    RS198=(Token)match(input,RS,FOLLOW_RS_in_par_pred_param_type1703); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RS.add(RS198);


                    OF199=(Token)match(input,OF,FOLLOW_OF_in_par_pred_param_type1705); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_OF.add(OF199);


                    LB200=(Token)match(input,LB,FOLLOW_LB_in_par_pred_param_type1707); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LB.add(LB200);


                    INT_CONST201=(Token)match(input,INT_CONST,FOLLOW_INT_CONST_in_par_pred_param_type1709); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_INT_CONST.add(INT_CONST201);


                    // parser/flatzinc/FlatzincFullExtParser.g:252:63: ( CM INT_CONST )*
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
                    	    // parser/flatzinc/FlatzincFullExtParser.g:252:64: CM INT_CONST
                    	    {
                    	    CM202=(Token)match(input,CM,FOLLOW_CM_in_par_pred_param_type1712); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_CM.add(CM202);


                    	    INT_CONST203=(Token)match(input,INT_CONST,FOLLOW_INT_CONST_in_par_pred_param_type1714); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_INT_CONST.add(INT_CONST203);


                    	    }
                    	    break;

                    	default :
                    	    break loop42;
                        }
                    } while (true);


                    RB204=(Token)match(input,RB,FOLLOW_RB_in_par_pred_param_type1718); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RB.add(RB204);


                    // AST REWRITE
                    // elements: index_set, ARRAY, CM, INT_CONST
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {

                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 253:5: -> ^( ARRAY ( index_set )+ ^( CM ( INT_CONST )+ ) )
                    {
                        // parser/flatzinc/FlatzincFullExtParser.g:253:9: ^( ARRAY ( index_set )+ ^( CM ( INT_CONST )+ ) )
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

                        // parser/flatzinc/FlatzincFullExtParser.g:253:28: ^( CM ( INT_CONST )+ )
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
                    // parser/flatzinc/FlatzincFullExtParser.g:254:9: ARRAY LS index_set ( CM index_set )* RS OF SET OF INT_CONST DD INT_CONST
                    {
                    ARRAY205=(Token)match(input,ARRAY,FOLLOW_ARRAY_in_par_pred_param_type1749); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ARRAY.add(ARRAY205);


                    LS206=(Token)match(input,LS,FOLLOW_LS_in_par_pred_param_type1751); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LS.add(LS206);


                    pushFollow(FOLLOW_index_set_in_par_pred_param_type1753);
                    index_set207=index_set();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_index_set.add(index_set207.getTree());

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
                    	    CM208=(Token)match(input,CM,FOLLOW_CM_in_par_pred_param_type1756); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_CM.add(CM208);


                    	    pushFollow(FOLLOW_index_set_in_par_pred_param_type1758);
                    	    index_set209=index_set();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_index_set.add(index_set209.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop43;
                        }
                    } while (true);


                    RS210=(Token)match(input,RS,FOLLOW_RS_in_par_pred_param_type1762); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RS.add(RS210);


                    OF211=(Token)match(input,OF,FOLLOW_OF_in_par_pred_param_type1764); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_OF.add(OF211);


                    SET212=(Token)match(input,SET,FOLLOW_SET_in_par_pred_param_type1766); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_SET.add(SET212);


                    OF213=(Token)match(input,OF,FOLLOW_OF_in_par_pred_param_type1768); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_OF.add(OF213);


                    INT_CONST214=(Token)match(input,INT_CONST,FOLLOW_INT_CONST_in_par_pred_param_type1770); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_INT_CONST.add(INT_CONST214);


                    DD215=(Token)match(input,DD,FOLLOW_DD_in_par_pred_param_type1772); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_DD.add(DD215);


                    INT_CONST216=(Token)match(input,INT_CONST,FOLLOW_INT_CONST_in_par_pred_param_type1774); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_INT_CONST.add(INT_CONST216);


                    // AST REWRITE
                    // elements: SET, INT_CONST, INT_CONST, index_set, ARRAY, DD
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {

                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 255:5: -> ^( ARRAY ( index_set )+ ^( SET ^( DD INT_CONST INT_CONST ) ) )
                    {
                        // parser/flatzinc/FlatzincFullExtParser.g:255:9: ^( ARRAY ( index_set )+ ^( SET ^( DD INT_CONST INT_CONST ) ) )
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

                        // parser/flatzinc/FlatzincFullExtParser.g:255:28: ^( SET ^( DD INT_CONST INT_CONST ) )
                        {
                        Object root_2 = (Object)adaptor.nil();
                        root_2 = (Object)adaptor.becomeRoot(
                        stream_SET.nextNode()
                        , root_2);

                        // parser/flatzinc/FlatzincFullExtParser.g:255:34: ^( DD INT_CONST INT_CONST )
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
                    // parser/flatzinc/FlatzincFullExtParser.g:256:9: ARRAY LS index_set ( CM index_set )* RS OF SET OF LB INT_CONST ( CM INT_CONST )* RB
                    {
                    ARRAY217=(Token)match(input,ARRAY,FOLLOW_ARRAY_in_par_pred_param_type1810); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ARRAY.add(ARRAY217);


                    LS218=(Token)match(input,LS,FOLLOW_LS_in_par_pred_param_type1812); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LS.add(LS218);


                    pushFollow(FOLLOW_index_set_in_par_pred_param_type1814);
                    index_set219=index_set();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_index_set.add(index_set219.getTree());

                    // parser/flatzinc/FlatzincFullExtParser.g:256:28: ( CM index_set )*
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
                    	    // parser/flatzinc/FlatzincFullExtParser.g:256:29: CM index_set
                    	    {
                    	    CM220=(Token)match(input,CM,FOLLOW_CM_in_par_pred_param_type1817); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_CM.add(CM220);


                    	    pushFollow(FOLLOW_index_set_in_par_pred_param_type1819);
                    	    index_set221=index_set();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_index_set.add(index_set221.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop44;
                        }
                    } while (true);


                    RS222=(Token)match(input,RS,FOLLOW_RS_in_par_pred_param_type1823); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RS.add(RS222);


                    OF223=(Token)match(input,OF,FOLLOW_OF_in_par_pred_param_type1825); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_OF.add(OF223);


                    SET224=(Token)match(input,SET,FOLLOW_SET_in_par_pred_param_type1827); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_SET.add(SET224);


                    OF225=(Token)match(input,OF,FOLLOW_OF_in_par_pred_param_type1829); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_OF.add(OF225);


                    LB226=(Token)match(input,LB,FOLLOW_LB_in_par_pred_param_type1831); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LB.add(LB226);


                    INT_CONST227=(Token)match(input,INT_CONST,FOLLOW_INT_CONST_in_par_pred_param_type1833); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_INT_CONST.add(INT_CONST227);


                    // parser/flatzinc/FlatzincFullExtParser.g:256:70: ( CM INT_CONST )*
                    loop45:
                    do {
                        int alt45=2;
                        switch ( input.LA(1) ) {
                        case CM:
                            {
                            alt45=1;
                            }
                            break;

                        }

                        switch (alt45) {
                    	case 1 :
                    	    // parser/flatzinc/FlatzincFullExtParser.g:256:71: CM INT_CONST
                    	    {
                    	    CM228=(Token)match(input,CM,FOLLOW_CM_in_par_pred_param_type1836); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_CM.add(CM228);


                    	    INT_CONST229=(Token)match(input,INT_CONST,FOLLOW_INT_CONST_in_par_pred_param_type1838); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_INT_CONST.add(INT_CONST229);


                    	    }
                    	    break;

                    	default :
                    	    break loop45;
                        }
                    } while (true);


                    RB230=(Token)match(input,RB,FOLLOW_RB_in_par_pred_param_type1842); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RB.add(RB230);


                    // AST REWRITE
                    // elements: ARRAY, SET, INT_CONST, CM, index_set
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {

                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 257:5: -> ^( ARRAY ( index_set )+ ^( SET ^( CM ( INT_CONST )+ ) ) )
                    {
                        // parser/flatzinc/FlatzincFullExtParser.g:257:9: ^( ARRAY ( index_set )+ ^( SET ^( CM ( INT_CONST )+ ) ) )
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

                        // parser/flatzinc/FlatzincFullExtParser.g:257:28: ^( SET ^( CM ( INT_CONST )+ ) )
                        {
                        Object root_2 = (Object)adaptor.nil();
                        root_2 = (Object)adaptor.becomeRoot(
                        stream_SET.nextNode()
                        , root_2);

                        // parser/flatzinc/FlatzincFullExtParser.g:257:34: ^( CM ( INT_CONST )+ )
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
    // parser/flatzinc/FlatzincFullExtParser.g:261:1: var_pred_param_type : ( var_type -> ^( VAR var_type ) | VAR SET OF INT -> ^( VAR SET ) | ARRAY LS index_set ( CM index_set )* RS OF VAR SET OF INT -> ^( ARRAY ( index_set )+ ^( VAR SET ) ) );
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
            // parser/flatzinc/FlatzincFullExtParser.g:262:5: ( var_type -> ^( VAR var_type ) | VAR SET OF INT -> ^( VAR SET ) | ARRAY LS index_set ( CM index_set )* RS OF VAR SET OF INT -> ^( ARRAY ( index_set )+ ^( VAR SET ) ) )
            int alt48=3;
            alt48 = dfa48.predict(input);
            switch (alt48) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtParser.g:262:9: var_type
                    {
                    pushFollow(FOLLOW_var_type_in_var_pred_param_type1887);
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
                    // 263:5: -> ^( VAR var_type )
                    {
                        // parser/flatzinc/FlatzincFullExtParser.g:263:9: ^( VAR var_type )
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
                    // parser/flatzinc/FlatzincFullExtParser.g:264:9: VAR SET OF INT
                    {
                    VAR232=(Token)match(input,VAR,FOLLOW_VAR_in_var_pred_param_type1910); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_VAR.add(VAR232);


                    SET233=(Token)match(input,SET,FOLLOW_SET_in_var_pred_param_type1912); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_SET.add(SET233);


                    OF234=(Token)match(input,OF,FOLLOW_OF_in_var_pred_param_type1914); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_OF.add(OF234);


                    INT235=(Token)match(input,INT,FOLLOW_INT_in_var_pred_param_type1916); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_INT.add(INT235);


                    // AST REWRITE
                    // elements: SET, VAR
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {

                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 265:5: -> ^( VAR SET )
                    {
                        // parser/flatzinc/FlatzincFullExtParser.g:265:9: ^( VAR SET )
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
                    // parser/flatzinc/FlatzincFullExtParser.g:266:9: ARRAY LS index_set ( CM index_set )* RS OF VAR SET OF INT
                    {
                    ARRAY236=(Token)match(input,ARRAY,FOLLOW_ARRAY_in_var_pred_param_type1939); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ARRAY.add(ARRAY236);


                    LS237=(Token)match(input,LS,FOLLOW_LS_in_var_pred_param_type1941); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LS.add(LS237);


                    pushFollow(FOLLOW_index_set_in_var_pred_param_type1943);
                    index_set238=index_set();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_index_set.add(index_set238.getTree());

                    // parser/flatzinc/FlatzincFullExtParser.g:266:28: ( CM index_set )*
                    loop47:
                    do {
                        int alt47=2;
                        switch ( input.LA(1) ) {
                        case CM:
                            {
                            alt47=1;
                            }
                            break;

                        }

                        switch (alt47) {
                    	case 1 :
                    	    // parser/flatzinc/FlatzincFullExtParser.g:266:29: CM index_set
                    	    {
                    	    CM239=(Token)match(input,CM,FOLLOW_CM_in_var_pred_param_type1946); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_CM.add(CM239);


                    	    pushFollow(FOLLOW_index_set_in_var_pred_param_type1948);
                    	    index_set240=index_set();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_index_set.add(index_set240.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop47;
                        }
                    } while (true);


                    RS241=(Token)match(input,RS,FOLLOW_RS_in_var_pred_param_type1952); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RS.add(RS241);


                    OF242=(Token)match(input,OF,FOLLOW_OF_in_var_pred_param_type1954); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_OF.add(OF242);


                    VAR243=(Token)match(input,VAR,FOLLOW_VAR_in_var_pred_param_type1956); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_VAR.add(VAR243);


                    SET244=(Token)match(input,SET,FOLLOW_SET_in_var_pred_param_type1958); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_SET.add(SET244);


                    OF245=(Token)match(input,OF,FOLLOW_OF_in_var_pred_param_type1960); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_OF.add(OF245);


                    INT246=(Token)match(input,INT,FOLLOW_INT_in_var_pred_param_type1962); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_INT.add(INT246);


                    // AST REWRITE
                    // elements: SET, ARRAY, index_set, VAR
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {

                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 267:5: -> ^( ARRAY ( index_set )+ ^( VAR SET ) )
                    {
                        // parser/flatzinc/FlatzincFullExtParser.g:267:9: ^( ARRAY ( index_set )+ ^( VAR SET ) )
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

                        // parser/flatzinc/FlatzincFullExtParser.g:267:28: ^( VAR SET )
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
    // parser/flatzinc/FlatzincFullExtParser.g:270:1: index_set : ( INT_CONST DD INT_CONST -> ^( INDEX ^( DD INT_CONST INT_CONST ) ) | INT -> ^( INDEX INT ) );
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
            // parser/flatzinc/FlatzincFullExtParser.g:271:5: ( INT_CONST DD INT_CONST -> ^( INDEX ^( DD INT_CONST INT_CONST ) ) | INT -> ^( INDEX INT ) )
            int alt49=2;
            switch ( input.LA(1) ) {
            case INT_CONST:
                {
                alt49=1;
                }
                break;
            case INT:
                {
                alt49=2;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 49, 0, input);

                throw nvae;

            }

            switch (alt49) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtParser.g:271:9: INT_CONST DD INT_CONST
                    {
                    INT_CONST247=(Token)match(input,INT_CONST,FOLLOW_INT_CONST_in_index_set2001); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_INT_CONST.add(INT_CONST247);


                    DD248=(Token)match(input,DD,FOLLOW_DD_in_index_set2003); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_DD.add(DD248);


                    INT_CONST249=(Token)match(input,INT_CONST,FOLLOW_INT_CONST_in_index_set2005); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_INT_CONST.add(INT_CONST249);


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
                    // 272:5: -> ^( INDEX ^( DD INT_CONST INT_CONST ) )
                    {
                        // parser/flatzinc/FlatzincFullExtParser.g:272:9: ^( INDEX ^( DD INT_CONST INT_CONST ) )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot(
                        (Object)adaptor.create(INDEX, "INDEX")
                        , root_1);

                        // parser/flatzinc/FlatzincFullExtParser.g:272:17: ^( DD INT_CONST INT_CONST )
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
                    // parser/flatzinc/FlatzincFullExtParser.g:273:9: INT
                    {
                    INT250=(Token)match(input,INT,FOLLOW_INT_in_index_set2034); if (state.failed) return retval; 
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
                    // 274:5: -> ^( INDEX INT )
                    {
                        // parser/flatzinc/FlatzincFullExtParser.g:274:9: ^( INDEX INT )
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
    // parser/flatzinc/FlatzincFullExtParser.g:277:1: expr : ( LB INT_CONST ( CM INT_CONST )* RB -> LB ( INT_CONST )+ RB | bool_const | INT_CONST ( DD INT_CONST )? | LS ( expr ( CM expr )* )? RS -> ^( EXPR LS ( expr )* RS ) | id_expr | STRING );
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
            // parser/flatzinc/FlatzincFullExtParser.g:278:5: ( LB INT_CONST ( CM INT_CONST )* RB -> LB ( INT_CONST )+ RB | bool_const | INT_CONST ( DD INT_CONST )? | LS ( expr ( CM expr )* )? RS -> ^( EXPR LS ( expr )* RS ) | id_expr | STRING )
            int alt54=6;
            switch ( input.LA(1) ) {
            case LB:
                {
                alt54=1;
                }
                break;
            case FALSE:
            case TRUE:
                {
                alt54=2;
                }
                break;
            case INT_CONST:
                {
                alt54=3;
                }
                break;
            case LS:
                {
                alt54=4;
                }
                break;
            case IDENTIFIER:
                {
                alt54=5;
                }
                break;
            case STRING:
                {
                alt54=6;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 54, 0, input);

                throw nvae;

            }

            switch (alt54) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtParser.g:278:9: LB INT_CONST ( CM INT_CONST )* RB
                    {
                    LB251=(Token)match(input,LB,FOLLOW_LB_in_expr2066); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LB.add(LB251);


                    INT_CONST252=(Token)match(input,INT_CONST,FOLLOW_INT_CONST_in_expr2068); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_INT_CONST.add(INT_CONST252);


                    // parser/flatzinc/FlatzincFullExtParser.g:278:22: ( CM INT_CONST )*
                    loop50:
                    do {
                        int alt50=2;
                        switch ( input.LA(1) ) {
                        case CM:
                            {
                            alt50=1;
                            }
                            break;

                        }

                        switch (alt50) {
                    	case 1 :
                    	    // parser/flatzinc/FlatzincFullExtParser.g:278:23: CM INT_CONST
                    	    {
                    	    CM253=(Token)match(input,CM,FOLLOW_CM_in_expr2071); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_CM.add(CM253);


                    	    INT_CONST254=(Token)match(input,INT_CONST,FOLLOW_INT_CONST_in_expr2073); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_INT_CONST.add(INT_CONST254);


                    	    }
                    	    break;

                    	default :
                    	    break loop50;
                        }
                    } while (true);


                    RB255=(Token)match(input,RB,FOLLOW_RB_in_expr2077); if (state.failed) return retval; 
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
                    // 279:5: -> LB ( INT_CONST )+ RB
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
                    // parser/flatzinc/FlatzincFullExtParser.g:280:9: bool_const
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_bool_const_in_expr2101);
                    bool_const256=bool_const();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, bool_const256.getTree());

                    }
                    break;
                case 3 :
                    // parser/flatzinc/FlatzincFullExtParser.g:281:9: INT_CONST ( DD INT_CONST )?
                    {
                    root_0 = (Object)adaptor.nil();


                    INT_CONST257=(Token)match(input,INT_CONST,FOLLOW_INT_CONST_in_expr2111); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    INT_CONST257_tree = 
                    (Object)adaptor.create(INT_CONST257)
                    ;
                    adaptor.addChild(root_0, INT_CONST257_tree);
                    }

                    // parser/flatzinc/FlatzincFullExtParser.g:281:19: ( DD INT_CONST )?
                    int alt51=2;
                    switch ( input.LA(1) ) {
                        case DD:
                            {
                            alt51=1;
                            }
                            break;
                    }

                    switch (alt51) {
                        case 1 :
                            // parser/flatzinc/FlatzincFullExtParser.g:281:20: DD INT_CONST
                            {
                            DD258=(Token)match(input,DD,FOLLOW_DD_in_expr2114); if (state.failed) return retval;
                            if ( state.backtracking==0 ) {
                            DD258_tree = 
                            (Object)adaptor.create(DD258)
                            ;
                            adaptor.addChild(root_0, DD258_tree);
                            }

                            INT_CONST259=(Token)match(input,INT_CONST,FOLLOW_INT_CONST_in_expr2116); if (state.failed) return retval;
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
                    // parser/flatzinc/FlatzincFullExtParser.g:282:9: LS ( expr ( CM expr )* )? RS
                    {
                    LS260=(Token)match(input,LS,FOLLOW_LS_in_expr2128); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LS.add(LS260);


                    // parser/flatzinc/FlatzincFullExtParser.g:282:12: ( expr ( CM expr )* )?
                    int alt53=2;
                    switch ( input.LA(1) ) {
                        case FALSE:
                        case IDENTIFIER:
                        case INT_CONST:
                        case LB:
                        case LS:
                        case STRING:
                        case TRUE:
                            {
                            alt53=1;
                            }
                            break;
                    }

                    switch (alt53) {
                        case 1 :
                            // parser/flatzinc/FlatzincFullExtParser.g:282:13: expr ( CM expr )*
                            {
                            pushFollow(FOLLOW_expr_in_expr2131);
                            expr261=expr();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_expr.add(expr261.getTree());

                            // parser/flatzinc/FlatzincFullExtParser.g:282:18: ( CM expr )*
                            loop52:
                            do {
                                int alt52=2;
                                switch ( input.LA(1) ) {
                                case CM:
                                    {
                                    alt52=1;
                                    }
                                    break;

                                }

                                switch (alt52) {
                            	case 1 :
                            	    // parser/flatzinc/FlatzincFullExtParser.g:282:19: CM expr
                            	    {
                            	    CM262=(Token)match(input,CM,FOLLOW_CM_in_expr2134); if (state.failed) return retval; 
                            	    if ( state.backtracking==0 ) stream_CM.add(CM262);


                            	    pushFollow(FOLLOW_expr_in_expr2136);
                            	    expr263=expr();

                            	    state._fsp--;
                            	    if (state.failed) return retval;
                            	    if ( state.backtracking==0 ) stream_expr.add(expr263.getTree());

                            	    }
                            	    break;

                            	default :
                            	    break loop52;
                                }
                            } while (true);


                            }
                            break;

                    }


                    RS264=(Token)match(input,RS,FOLLOW_RS_in_expr2142); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RS.add(RS264);


                    // AST REWRITE
                    // elements: LS, RS, expr
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {

                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 283:5: -> ^( EXPR LS ( expr )* RS )
                    {
                        // parser/flatzinc/FlatzincFullExtParser.g:283:9: ^( EXPR LS ( expr )* RS )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot(
                        (Object)adaptor.create(EXPR, "EXPR")
                        , root_1);

                        adaptor.addChild(root_1, 
                        stream_LS.nextNode()
                        );

                        // parser/flatzinc/FlatzincFullExtParser.g:283:19: ( expr )*
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
                    // parser/flatzinc/FlatzincFullExtParser.g:284:9: id_expr
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_id_expr_in_expr2170);
                    id_expr265=id_expr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, id_expr265.getTree());

                    }
                    break;
                case 6 :
                    // parser/flatzinc/FlatzincFullExtParser.g:285:9: STRING
                    {
                    root_0 = (Object)adaptor.nil();


                    STRING266=(Token)match(input,STRING,FOLLOW_STRING_in_expr2180); if (state.failed) return retval;
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
    // parser/flatzinc/FlatzincFullExtParser.g:289:1: id_expr : IDENTIFIER ( ( LP expr ( CM expr )* RP ) | ( LS INT_CONST RS ) )? ;
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
            // parser/flatzinc/FlatzincFullExtParser.g:291:5: ( IDENTIFIER ( ( LP expr ( CM expr )* RP ) | ( LS INT_CONST RS ) )? )
            // parser/flatzinc/FlatzincFullExtParser.g:291:9: IDENTIFIER ( ( LP expr ( CM expr )* RP ) | ( LS INT_CONST RS ) )?
            {
            root_0 = (Object)adaptor.nil();


            IDENTIFIER267=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_id_expr2201); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            IDENTIFIER267_tree = 
            (Object)adaptor.create(IDENTIFIER267)
            ;
            adaptor.addChild(root_0, IDENTIFIER267_tree);
            }

            // parser/flatzinc/FlatzincFullExtParser.g:291:20: ( ( LP expr ( CM expr )* RP ) | ( LS INT_CONST RS ) )?
            int alt56=3;
            switch ( input.LA(1) ) {
                case LP:
                    {
                    alt56=1;
                    }
                    break;
                case LS:
                    {
                    alt56=2;
                    }
                    break;
            }

            switch (alt56) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtParser.g:291:21: ( LP expr ( CM expr )* RP )
                    {
                    // parser/flatzinc/FlatzincFullExtParser.g:291:21: ( LP expr ( CM expr )* RP )
                    // parser/flatzinc/FlatzincFullExtParser.g:291:22: LP expr ( CM expr )* RP
                    {
                    LP268=(Token)match(input,LP,FOLLOW_LP_in_id_expr2205); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LP268_tree = 
                    (Object)adaptor.create(LP268)
                    ;
                    adaptor.addChild(root_0, LP268_tree);
                    }

                    pushFollow(FOLLOW_expr_in_id_expr2207);
                    expr269=expr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, expr269.getTree());

                    // parser/flatzinc/FlatzincFullExtParser.g:291:30: ( CM expr )*
                    loop55:
                    do {
                        int alt55=2;
                        switch ( input.LA(1) ) {
                        case CM:
                            {
                            alt55=1;
                            }
                            break;

                        }

                        switch (alt55) {
                    	case 1 :
                    	    // parser/flatzinc/FlatzincFullExtParser.g:291:31: CM expr
                    	    {
                    	    CM270=(Token)match(input,CM,FOLLOW_CM_in_id_expr2210); if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) {
                    	    CM270_tree = 
                    	    (Object)adaptor.create(CM270)
                    	    ;
                    	    adaptor.addChild(root_0, CM270_tree);
                    	    }

                    	    pushFollow(FOLLOW_expr_in_id_expr2212);
                    	    expr271=expr();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) adaptor.addChild(root_0, expr271.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop55;
                        }
                    } while (true);


                    RP272=(Token)match(input,RP,FOLLOW_RP_in_id_expr2216); if (state.failed) return retval;
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
                    // parser/flatzinc/FlatzincFullExtParser.g:291:45: ( LS INT_CONST RS )
                    {
                    // parser/flatzinc/FlatzincFullExtParser.g:291:45: ( LS INT_CONST RS )
                    // parser/flatzinc/FlatzincFullExtParser.g:291:46: LS INT_CONST RS
                    {
                    LS273=(Token)match(input,LS,FOLLOW_LS_in_id_expr2220); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LS273_tree = 
                    (Object)adaptor.create(LS273)
                    ;
                    adaptor.addChild(root_0, LS273_tree);
                    }

                    INT_CONST274=(Token)match(input,INT_CONST,FOLLOW_INT_CONST_in_id_expr2222); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    INT_CONST274_tree = 
                    (Object)adaptor.create(INT_CONST274)
                    ;
                    adaptor.addChild(root_0, INT_CONST274_tree);
                    }

                    RS275=(Token)match(input,RS,FOLLOW_RS_in_id_expr2224); if (state.failed) return retval;
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
    // parser/flatzinc/FlatzincFullExtParser.g:295:1: param_decl : par_type CL IDENTIFIER EQ expr SC -> ^( PAR IDENTIFIER par_type expr ) ;
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
            // parser/flatzinc/FlatzincFullExtParser.g:296:2: ( par_type CL IDENTIFIER EQ expr SC -> ^( PAR IDENTIFIER par_type expr ) )
            // parser/flatzinc/FlatzincFullExtParser.g:296:6: par_type CL IDENTIFIER EQ expr SC
            {
            pushFollow(FOLLOW_par_type_in_param_decl2244);
            par_type276=par_type();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_par_type.add(par_type276.getTree());

            CL277=(Token)match(input,CL,FOLLOW_CL_in_param_decl2246); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_CL.add(CL277);


            IDENTIFIER278=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_param_decl2248); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_IDENTIFIER.add(IDENTIFIER278);


            EQ279=(Token)match(input,EQ,FOLLOW_EQ_in_param_decl2250); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_EQ.add(EQ279);


            pushFollow(FOLLOW_expr_in_param_decl2252);
            expr280=expr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_expr.add(expr280.getTree());

            SC281=(Token)match(input,SC,FOLLOW_SC_in_param_decl2254); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_SC.add(SC281);


            // AST REWRITE
            // elements: expr, par_type, IDENTIFIER
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {

            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 297:2: -> ^( PAR IDENTIFIER par_type expr )
            {
                // parser/flatzinc/FlatzincFullExtParser.g:297:6: ^( PAR IDENTIFIER par_type expr )
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
    // parser/flatzinc/FlatzincFullExtParser.g:301:1: var_decl : var_type CL IDENTIFIER annotations ( EQ expr )? SC -> ^( VAR IDENTIFIER var_type annotations ( expr )? ) ;
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
            // parser/flatzinc/FlatzincFullExtParser.g:302:2: ( var_type CL IDENTIFIER annotations ( EQ expr )? SC -> ^( VAR IDENTIFIER var_type annotations ( expr )? ) )
            // parser/flatzinc/FlatzincFullExtParser.g:302:6: var_type CL IDENTIFIER annotations ( EQ expr )? SC
            {
            pushFollow(FOLLOW_var_type_in_var_decl2282);
            var_type282=var_type();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_var_type.add(var_type282.getTree());

            CL283=(Token)match(input,CL,FOLLOW_CL_in_var_decl2284); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_CL.add(CL283);


            IDENTIFIER284=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_var_decl2286); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_IDENTIFIER.add(IDENTIFIER284);


            pushFollow(FOLLOW_annotations_in_var_decl2288);
            annotations285=annotations();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_annotations.add(annotations285.getTree());

            // parser/flatzinc/FlatzincFullExtParser.g:302:41: ( EQ expr )?
            int alt57=2;
            switch ( input.LA(1) ) {
                case EQ:
                    {
                    alt57=1;
                    }
                    break;
            }

            switch (alt57) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtParser.g:302:42: EQ expr
                    {
                    EQ286=(Token)match(input,EQ,FOLLOW_EQ_in_var_decl2291); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_EQ.add(EQ286);


                    pushFollow(FOLLOW_expr_in_var_decl2293);
                    expr287=expr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_expr.add(expr287.getTree());

                    }
                    break;

            }


            SC288=(Token)match(input,SC,FOLLOW_SC_in_var_decl2297); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_SC.add(SC288);


            // AST REWRITE
            // elements: annotations, var_type, IDENTIFIER, expr
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {

            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 303:2: -> ^( VAR IDENTIFIER var_type annotations ( expr )? )
            {
                // parser/flatzinc/FlatzincFullExtParser.g:303:6: ^( VAR IDENTIFIER var_type annotations ( expr )? )
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

                // parser/flatzinc/FlatzincFullExtParser.g:303:44: ( expr )?
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
    // parser/flatzinc/FlatzincFullExtParser.g:306:1: constraint : CONSTRAINT IDENTIFIER LP expr ( CM expr )* RP annotations SC -> ^( CONSTRAINT IDENTIFIER ( expr )+ annotations ) ;
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
            // parser/flatzinc/FlatzincFullExtParser.g:307:2: ( CONSTRAINT IDENTIFIER LP expr ( CM expr )* RP annotations SC -> ^( CONSTRAINT IDENTIFIER ( expr )+ annotations ) )
            // parser/flatzinc/FlatzincFullExtParser.g:307:6: CONSTRAINT IDENTIFIER LP expr ( CM expr )* RP annotations SC
            {
            CONSTRAINT289=(Token)match(input,CONSTRAINT,FOLLOW_CONSTRAINT_in_constraint2327); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_CONSTRAINT.add(CONSTRAINT289);


            IDENTIFIER290=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_constraint2329); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_IDENTIFIER.add(IDENTIFIER290);


            LP291=(Token)match(input,LP,FOLLOW_LP_in_constraint2331); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LP.add(LP291);


            pushFollow(FOLLOW_expr_in_constraint2333);
            expr292=expr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_expr.add(expr292.getTree());

            // parser/flatzinc/FlatzincFullExtParser.g:307:36: ( CM expr )*
            loop58:
            do {
                int alt58=2;
                switch ( input.LA(1) ) {
                case CM:
                    {
                    alt58=1;
                    }
                    break;

                }

                switch (alt58) {
            	case 1 :
            	    // parser/flatzinc/FlatzincFullExtParser.g:307:37: CM expr
            	    {
            	    CM293=(Token)match(input,CM,FOLLOW_CM_in_constraint2336); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_CM.add(CM293);


            	    pushFollow(FOLLOW_expr_in_constraint2338);
            	    expr294=expr();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_expr.add(expr294.getTree());

            	    }
            	    break;

            	default :
            	    break loop58;
                }
            } while (true);


            RP295=(Token)match(input,RP,FOLLOW_RP_in_constraint2342); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RP.add(RP295);


            pushFollow(FOLLOW_annotations_in_constraint2344);
            annotations296=annotations();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_annotations.add(annotations296.getTree());

            SC297=(Token)match(input,SC,FOLLOW_SC_in_constraint2346); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_SC.add(SC297);


            // AST REWRITE
            // elements: IDENTIFIER, CONSTRAINT, expr, annotations
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {

            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 308:2: -> ^( CONSTRAINT IDENTIFIER ( expr )+ annotations )
            {
                // parser/flatzinc/FlatzincFullExtParser.g:308:6: ^( CONSTRAINT IDENTIFIER ( expr )+ annotations )
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
    // parser/flatzinc/FlatzincFullExtParser.g:311:1: solve_goal : SOLVE ^ annotations resolution SC !;
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
            // parser/flatzinc/FlatzincFullExtParser.g:312:2: ( SOLVE ^ annotations resolution SC !)
            // parser/flatzinc/FlatzincFullExtParser.g:312:6: SOLVE ^ annotations resolution SC !
            {
            root_0 = (Object)adaptor.nil();


            SOLVE298=(Token)match(input,SOLVE,FOLLOW_SOLVE_in_solve_goal2374); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            SOLVE298_tree = 
            (Object)adaptor.create(SOLVE298)
            ;
            root_0 = (Object)adaptor.becomeRoot(SOLVE298_tree, root_0);
            }

            pushFollow(FOLLOW_annotations_in_solve_goal2377);
            annotations299=annotations();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, annotations299.getTree());

            pushFollow(FOLLOW_resolution_in_solve_goal2379);
            resolution300=resolution();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, resolution300.getTree());

            SC301=(Token)match(input,SC,FOLLOW_SC_in_solve_goal2381); if (state.failed) return retval;

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
    // parser/flatzinc/FlatzincFullExtParser.g:315:1: resolution : ( MINIMIZE ^ expr | MAXIMIZE ^ expr | SATISFY ^);
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
            // parser/flatzinc/FlatzincFullExtParser.g:316:5: ( MINIMIZE ^ expr | MAXIMIZE ^ expr | SATISFY ^)
            int alt59=3;
            switch ( input.LA(1) ) {
            case MINIMIZE:
                {
                alt59=1;
                }
                break;
            case MAXIMIZE:
                {
                alt59=2;
                }
                break;
            case SATISFY:
                {
                alt59=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 59, 0, input);

                throw nvae;

            }

            switch (alt59) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtParser.g:316:9: MINIMIZE ^ expr
                    {
                    root_0 = (Object)adaptor.nil();


                    MINIMIZE302=(Token)match(input,MINIMIZE,FOLLOW_MINIMIZE_in_resolution2398); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    MINIMIZE302_tree = 
                    (Object)adaptor.create(MINIMIZE302)
                    ;
                    root_0 = (Object)adaptor.becomeRoot(MINIMIZE302_tree, root_0);
                    }

                    pushFollow(FOLLOW_expr_in_resolution2401);
                    expr303=expr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, expr303.getTree());

                    }
                    break;
                case 2 :
                    // parser/flatzinc/FlatzincFullExtParser.g:317:9: MAXIMIZE ^ expr
                    {
                    root_0 = (Object)adaptor.nil();


                    MAXIMIZE304=(Token)match(input,MAXIMIZE,FOLLOW_MAXIMIZE_in_resolution2411); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    MAXIMIZE304_tree = 
                    (Object)adaptor.create(MAXIMIZE304)
                    ;
                    root_0 = (Object)adaptor.becomeRoot(MAXIMIZE304_tree, root_0);
                    }

                    pushFollow(FOLLOW_expr_in_resolution2414);
                    expr305=expr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, expr305.getTree());

                    }
                    break;
                case 3 :
                    // parser/flatzinc/FlatzincFullExtParser.g:318:9: SATISFY ^
                    {
                    root_0 = (Object)adaptor.nil();


                    SATISFY306=(Token)match(input,SATISFY,FOLLOW_SATISFY_in_resolution2424); if (state.failed) return retval;
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
    // parser/flatzinc/FlatzincFullExtParser.g:321:1: annotations : ( DC annotation )* -> ^( ANNOTATIONS ( annotation )* ) ;
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
            // parser/flatzinc/FlatzincFullExtParser.g:322:5: ( ( DC annotation )* -> ^( ANNOTATIONS ( annotation )* ) )
            // parser/flatzinc/FlatzincFullExtParser.g:322:9: ( DC annotation )*
            {
            // parser/flatzinc/FlatzincFullExtParser.g:322:9: ( DC annotation )*
            loop60:
            do {
                int alt60=2;
                switch ( input.LA(1) ) {
                case DC:
                    {
                    alt60=1;
                    }
                    break;

                }

                switch (alt60) {
            	case 1 :
            	    // parser/flatzinc/FlatzincFullExtParser.g:322:10: DC annotation
            	    {
            	    DC307=(Token)match(input,DC,FOLLOW_DC_in_annotations2445); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_DC.add(DC307);


            	    pushFollow(FOLLOW_annotation_in_annotations2447);
            	    annotation308=annotation();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_annotation.add(annotation308.getTree());

            	    }
            	    break;

            	default :
            	    break loop60;
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
            // 323:5: -> ^( ANNOTATIONS ( annotation )* )
            {
                // parser/flatzinc/FlatzincFullExtParser.g:323:9: ^( ANNOTATIONS ( annotation )* )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot(
                (Object)adaptor.create(ANNOTATIONS, "ANNOTATIONS")
                , root_1);

                // parser/flatzinc/FlatzincFullExtParser.g:323:23: ( annotation )*
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
    // parser/flatzinc/FlatzincFullExtParser.g:326:1: annotation : IDENTIFIER ( LP expr ( CM expr )* RP )? -> IDENTIFIER ( LP ( expr )+ RP )? ;
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
            // parser/flatzinc/FlatzincFullExtParser.g:327:5: ( IDENTIFIER ( LP expr ( CM expr )* RP )? -> IDENTIFIER ( LP ( expr )+ RP )? )
            // parser/flatzinc/FlatzincFullExtParser.g:327:9: IDENTIFIER ( LP expr ( CM expr )* RP )?
            {
            IDENTIFIER309=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_annotation2482); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_IDENTIFIER.add(IDENTIFIER309);


            // parser/flatzinc/FlatzincFullExtParser.g:327:20: ( LP expr ( CM expr )* RP )?
            int alt62=2;
            switch ( input.LA(1) ) {
                case LP:
                    {
                    alt62=1;
                    }
                    break;
            }

            switch (alt62) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtParser.g:327:21: LP expr ( CM expr )* RP
                    {
                    LP310=(Token)match(input,LP,FOLLOW_LP_in_annotation2485); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LP.add(LP310);


                    pushFollow(FOLLOW_expr_in_annotation2487);
                    expr311=expr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_expr.add(expr311.getTree());

                    // parser/flatzinc/FlatzincFullExtParser.g:327:29: ( CM expr )*
                    loop61:
                    do {
                        int alt61=2;
                        switch ( input.LA(1) ) {
                        case CM:
                            {
                            alt61=1;
                            }
                            break;

                        }

                        switch (alt61) {
                    	case 1 :
                    	    // parser/flatzinc/FlatzincFullExtParser.g:327:30: CM expr
                    	    {
                    	    CM312=(Token)match(input,CM,FOLLOW_CM_in_annotation2490); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_CM.add(CM312);


                    	    pushFollow(FOLLOW_expr_in_annotation2492);
                    	    expr313=expr();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_expr.add(expr313.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop61;
                        }
                    } while (true);


                    RP314=(Token)match(input,RP,FOLLOW_RP_in_annotation2496); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RP.add(RP314);


                    }
                    break;

            }


            // AST REWRITE
            // elements: IDENTIFIER, RP, expr, LP
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {

            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 328:5: -> IDENTIFIER ( LP ( expr )+ RP )?
            {
                adaptor.addChild(root_0, 
                stream_IDENTIFIER.nextNode()
                );

                // parser/flatzinc/FlatzincFullExtParser.g:328:20: ( LP ( expr )+ RP )?
                if ( stream_RP.hasNext()||stream_expr.hasNext()||stream_LP.hasNext() ) {
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
                stream_RP.reset();
                stream_expr.reset();
                stream_LP.reset();

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
    // parser/flatzinc/FlatzincFullExtParser.g:332:1: bool_const : ( TRUE ^| FALSE ^);
    public final FlatzincFullExtParser.bool_const_return bool_const() throws RecognitionException {
        FlatzincFullExtParser.bool_const_return retval = new FlatzincFullExtParser.bool_const_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token TRUE315=null;
        Token FALSE316=null;

        Object TRUE315_tree=null;
        Object FALSE316_tree=null;

        try {
            // parser/flatzinc/FlatzincFullExtParser.g:333:5: ( TRUE ^| FALSE ^)
            int alt63=2;
            switch ( input.LA(1) ) {
            case TRUE:
                {
                alt63=1;
                }
                break;
            case FALSE:
                {
                alt63=2;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 63, 0, input);

                throw nvae;

            }

            switch (alt63) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtParser.g:333:9: TRUE ^
                    {
                    root_0 = (Object)adaptor.nil();


                    TRUE315=(Token)match(input,TRUE,FOLLOW_TRUE_in_bool_const2537); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    TRUE315_tree = 
                    (Object)adaptor.create(TRUE315)
                    ;
                    root_0 = (Object)adaptor.becomeRoot(TRUE315_tree, root_0);
                    }

                    }
                    break;
                case 2 :
                    // parser/flatzinc/FlatzincFullExtParser.g:334:9: FALSE ^
                    {
                    root_0 = (Object)adaptor.nil();


                    FALSE316=(Token)match(input,FALSE,FOLLOW_FALSE_in_bool_const2548); if (state.failed) return retval;
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
        int cnt64=0;
        loop64:
        do {
            int alt64=2;
            switch ( input.LA(1) ) {
            case AND:
                {
                alt64=1;
                }
                break;

            }

            switch (alt64) {
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
        	    if ( cnt64 >= 1 ) break loop64;
        	    if (state.backtracking>0) {state.failed=true; return ;}
                    EarlyExitException eee =
                        new EarlyExitException(64, input);
                    throw eee;
            }
            cnt64++;
        } while (true);


        match(input,RP,FOLLOW_RP_in_synpred9_FlatzincFullExtParser176); if (state.failed) return ;

        }

    }
    // $ANTLR end synpred9_FlatzincFullExtParser

    // $ANTLR start synpred47_FlatzincFullExtParser
    public final void synpred47_FlatzincFullExtParser_fragment() throws RecognitionException {
        // parser/flatzinc/FlatzincFullExtParser.g:163:4: ( attr_op ( DO attr_op )* ( DO attribute )? )
        // parser/flatzinc/FlatzincFullExtParser.g:163:4: attr_op ( DO attr_op )* ( DO attribute )?
        {
        pushFollow(FOLLOW_attr_op_in_synpred47_FlatzincFullExtParser843);
        attr_op();

        state._fsp--;
        if (state.failed) return ;

        // parser/flatzinc/FlatzincFullExtParser.g:163:12: ( DO attr_op )*
        loop67:
        do {
            int alt67=2;
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
                    alt67=1;
                    }
                    break;

                }

                }
                break;

            }

            switch (alt67) {
        	case 1 :
        	    // parser/flatzinc/FlatzincFullExtParser.g:163:13: DO attr_op
        	    {
        	    match(input,DO,FOLLOW_DO_in_synpred47_FlatzincFullExtParser846); if (state.failed) return ;

        	    pushFollow(FOLLOW_attr_op_in_synpred47_FlatzincFullExtParser848);
        	    attr_op();

        	    state._fsp--;
        	    if (state.failed) return ;

        	    }
        	    break;

        	default :
        	    break loop67;
            }
        } while (true);


        // parser/flatzinc/FlatzincFullExtParser.g:163:27: ( DO attribute )?
        int alt68=2;
        switch ( input.LA(1) ) {
            case DO:
                {
                alt68=1;
                }
                break;
        }

        switch (alt68) {
            case 1 :
                // parser/flatzinc/FlatzincFullExtParser.g:163:28: DO attribute
                {
                match(input,DO,FOLLOW_DO_in_synpred47_FlatzincFullExtParser854); if (state.failed) return ;

                pushFollow(FOLLOW_attribute_in_synpred47_FlatzincFullExtParser856);
                attribute();

                state._fsp--;
                if (state.failed) return ;

                }
                break;

        }


        }

    }
    // $ANTLR end synpred47_FlatzincFullExtParser

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
    public final boolean synpred47_FlatzincFullExtParser() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred47_FlatzincFullExtParser_fragment(); // can never throw exception
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
    protected DFA27 dfa27 = new DFA27(this);
    protected DFA29 dfa29 = new DFA29(this);
    protected DFA46 dfa46 = new DFA46(this);
    protected DFA48 dfa48 = new DFA48(this);
    static final String DFA2_eotS =
        "\20\uffff";
    static final String DFA2_eofS =
        "\20\uffff";
    static final String DFA2_minS =
        "\1\10\1\54\2\uffff\1\46\1\27\1\21\1\47\1\46\1\65\1\21\1\27\1\21"+
        "\1\15\1\47\1\21";
    static final String DFA2_maxS =
        "\1\125\1\54\2\uffff\1\47\1\27\1\111\2\47\1\65\1\111\1\27\1\111\1"+
        "\125\1\47\1\111";
    static final String DFA2_acceptS =
        "\2\uffff\1\2\1\1\14\uffff";
    static final String DFA2_specialS =
        "\20\uffff}>";
    static final String[] DFA2_transitionS = {
            "\1\1\4\uffff\1\3\6\uffff\1\2\12\uffff\1\3\1\uffff\1\2\1\uffff"+
            "\1\2\2\uffff\1\3\3\uffff\1\2\2\uffff\1\2\27\uffff\1\2\1\uffff"+
            "\1\2\4\uffff\1\3\1\uffff\1\2\6\uffff\1\2",
            "\1\4",
            "",
            "",
            "\1\6\1\5",
            "\1\7",
            "\1\10\67\uffff\1\11",
            "\1\12",
            "\1\14\1\13",
            "\1\15",
            "\1\10\67\uffff\1\11",
            "\1\16",
            "\1\10\67\uffff\1\11",
            "\1\3\21\uffff\1\3\6\uffff\1\3\45\uffff\1\3\10\uffff\1\2",
            "\1\17",
            "\1\10\67\uffff\1\11"
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
    static final String DFA27_eotS =
        "\7\uffff";
    static final String DFA27_eofS =
        "\1\uffff\1\4\3\uffff\1\4\1\uffff";
    static final String DFA27_minS =
        "\1\6\1\21\1\uffff\1\6\1\uffff\1\21\1\0";
    static final String DFA27_maxS =
        "\1\127\1\113\1\uffff\1\127\1\uffff\1\113\1\0";
    static final String DFA27_acceptS =
        "\2\uffff\1\2\1\uffff\1\1\2\uffff";
    static final String DFA27_specialS =
        "\6\uffff\1\0}>";
    static final String[] DFA27_transitionS = {
            "\1\1\7\uffff\1\2\3\uffff\1\2\2\uffff\1\2\27\uffff\1\1\1\uffff"+
            "\1\1\17\uffff\1\2\1\uffff\2\2\1\uffff\1\2\10\uffff\1\1\4\uffff"+
            "\1\1\2\uffff\3\2",
            "\1\4\6\uffff\1\3\55\uffff\1\4\4\uffff\1\4",
            "",
            "\1\5\7\uffff\1\6\3\uffff\1\6\2\uffff\1\6\27\uffff\1\5\1\uffff"+
            "\1\5\17\uffff\1\6\1\uffff\2\6\1\uffff\1\6\10\uffff\1\5\4\uffff"+
            "\1\5\2\uffff\3\6",
            "",
            "\1\4\6\uffff\1\3\55\uffff\1\4\4\uffff\1\4",
            "\1\uffff"
    };

    static final short[] DFA27_eot = DFA.unpackEncodedString(DFA27_eotS);
    static final short[] DFA27_eof = DFA.unpackEncodedString(DFA27_eofS);
    static final char[] DFA27_min = DFA.unpackEncodedStringToUnsignedChars(DFA27_minS);
    static final char[] DFA27_max = DFA.unpackEncodedStringToUnsignedChars(DFA27_maxS);
    static final short[] DFA27_accept = DFA.unpackEncodedString(DFA27_acceptS);
    static final short[] DFA27_special = DFA.unpackEncodedString(DFA27_specialS);
    static final short[][] DFA27_transition;

    static {
        int numStates = DFA27_transitionS.length;
        DFA27_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA27_transition[i] = DFA.unpackEncodedString(DFA27_transitionS[i]);
        }
    }

    class DFA27 extends DFA {

        public DFA27(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 27;
            this.eot = DFA27_eot;
            this.eof = DFA27_eof;
            this.min = DFA27_min;
            this.max = DFA27_max;
            this.accept = DFA27_accept;
            this.special = DFA27_special;
            this.transition = DFA27_transition;
        }
        public String getDescription() {
            return "162:1: comb_attr : ( attr_op ( DO attr_op )* ( DO attribute )? -> ^( DO ( attr_op )* ( attribute )? ) | ( attr_op DO )* attribute -> ^( DO ( attr_op )* ( attribute )? ) );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA27_6 = input.LA(1);

                         
                        int index27_6 = input.index();
                        input.rewind();

                        s = -1;
                        if ( (synpred47_FlatzincFullExtParser()) ) {s = 4;}

                        else if ( (true) ) {s = 2;}

                         
                        input.seek(index27_6);

                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}

            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 27, _s, input);
            error(nvae);
            throw nvae;
        }

    }
    static final String DFA29_eotS =
        "\20\uffff";
    static final String DFA29_eofS =
        "\20\uffff";
    static final String DFA29_minS =
        "\1\10\1\54\2\uffff\1\46\1\27\1\21\1\47\1\46\1\65\1\21\1\27\1\21"+
        "\1\15\1\47\1\21";
    static final String DFA29_maxS =
        "\1\125\1\54\2\uffff\1\47\1\27\1\111\2\47\1\65\1\111\1\27\1\111\1"+
        "\125\1\47\1\111";
    static final String DFA29_acceptS =
        "\2\uffff\1\1\1\2\14\uffff";
    static final String DFA29_specialS =
        "\20\uffff}>";
    static final String[] DFA29_transitionS = {
            "\1\1\4\uffff\1\2\21\uffff\1\2\6\uffff\2\2\1\uffff\1\2\42\uffff"+
            "\1\2\10\uffff\1\3",
            "\1\4",
            "",
            "",
            "\1\6\1\5",
            "\1\7",
            "\1\10\67\uffff\1\11",
            "\1\12",
            "\1\14\1\13",
            "\1\15",
            "\1\10\67\uffff\1\11",
            "\1\16",
            "\1\10\67\uffff\1\11",
            "\1\2\21\uffff\1\2\6\uffff\2\2\1\uffff\1\2\42\uffff\1\2\10\uffff"+
            "\1\3",
            "\1\17",
            "\1\10\67\uffff\1\11"
    };

    static final short[] DFA29_eot = DFA.unpackEncodedString(DFA29_eotS);
    static final short[] DFA29_eof = DFA.unpackEncodedString(DFA29_eofS);
    static final char[] DFA29_min = DFA.unpackEncodedStringToUnsignedChars(DFA29_minS);
    static final char[] DFA29_max = DFA.unpackEncodedStringToUnsignedChars(DFA29_maxS);
    static final short[] DFA29_accept = DFA.unpackEncodedString(DFA29_acceptS);
    static final short[] DFA29_special = DFA.unpackEncodedString(DFA29_specialS);
    static final short[][] DFA29_transition;

    static {
        int numStates = DFA29_transitionS.length;
        DFA29_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA29_transition[i] = DFA.unpackEncodedString(DFA29_transitionS[i]);
        }
    }

    class DFA29 extends DFA {

        public DFA29(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 29;
            this.eot = DFA29_eot;
            this.eof = DFA29_eof;
            this.min = DFA29_min;
            this.max = DFA29_max;
            this.accept = DFA29_accept;
            this.special = DFA29_special;
            this.transition = DFA29_transition;
        }
        public String getDescription() {
            return "194:1: pred_param_type : ( par_pred_param_type | var_pred_param_type );";
        }
    }
    static final String DFA46_eotS =
        "\33\uffff";
    static final String DFA46_eofS =
        "\33\uffff";
    static final String DFA46_minS =
        "\1\10\1\54\1\uffff\1\65\2\uffff\2\46\1\27\1\21\2\uffff\1\47\1\46"+
        "\1\65\1\21\1\27\1\21\1\15\1\47\2\uffff\1\65\1\21\1\46\2\uffff";
    static final String DFA46_maxS =
        "\1\114\1\54\1\uffff\1\65\2\uffff\1\47\1\51\1\27\1\111\2\uffff\2"+
        "\47\1\65\1\111\1\27\1\111\1\114\1\47\2\uffff\1\65\1\111\1\51\2\uffff";
    static final String DFA46_acceptS =
        "\2\uffff\1\1\1\uffff\1\2\1\3\4\uffff\1\4\1\5\10\uffff\1\6\1\7\3"+
        "\uffff\1\10\1\11";
    static final String DFA46_specialS =
        "\33\uffff}>";
    static final String[] DFA46_transitionS = {
            "\1\1\4\uffff\1\2\21\uffff\1\2\6\uffff\1\2\1\4\1\uffff\1\5\42"+
            "\uffff\1\3",
            "\1\6",
            "",
            "\1\7",
            "",
            "",
            "\1\11\1\10",
            "\1\2\1\12\1\uffff\1\13",
            "\1\14",
            "\1\15\67\uffff\1\16",
            "",
            "",
            "\1\17",
            "\1\21\1\20",
            "\1\22",
            "\1\15\67\uffff\1\16",
            "\1\23",
            "\1\15\67\uffff\1\16",
            "\1\2\21\uffff\1\2\6\uffff\1\2\1\24\1\uffff\1\25\42\uffff\1"+
            "\26",
            "\1\27",
            "",
            "",
            "\1\30",
            "\1\15\67\uffff\1\16",
            "\1\2\1\31\1\uffff\1\32",
            "",
            ""
    };

    static final short[] DFA46_eot = DFA.unpackEncodedString(DFA46_eotS);
    static final short[] DFA46_eof = DFA.unpackEncodedString(DFA46_eofS);
    static final char[] DFA46_min = DFA.unpackEncodedStringToUnsignedChars(DFA46_minS);
    static final char[] DFA46_max = DFA.unpackEncodedStringToUnsignedChars(DFA46_maxS);
    static final short[] DFA46_accept = DFA.unpackEncodedString(DFA46_acceptS);
    static final short[] DFA46_special = DFA.unpackEncodedString(DFA46_specialS);
    static final short[][] DFA46_transition;

    static {
        int numStates = DFA46_transitionS.length;
        DFA46_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA46_transition[i] = DFA.unpackEncodedString(DFA46_transitionS[i]);
        }
    }

    class DFA46 extends DFA {

        public DFA46(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 46;
            this.eot = DFA46_eot;
            this.eof = DFA46_eof;
            this.min = DFA46_min;
            this.max = DFA46_max;
            this.accept = DFA46_accept;
            this.special = DFA46_special;
            this.transition = DFA46_transition;
        }
        public String getDescription() {
            return "236:1: par_pred_param_type : ( par_type | INT_CONST DD INT_CONST -> ^( DD INT_CONST INT_CONST ) | LB INT_CONST ( CM INT_CONST )* RB -> ^( CM ( INT_CONST )+ ) | SET OF INT_CONST DD INT_CONST -> ^( SET ^( DD INT_CONST INT_CONST ) ) | SET OF LB INT_CONST ( CM INT_CONST )* RB -> ^( SET ^( CM ( INT_CONST )+ ) ) | ARRAY LS index_set ( CM index_set )* RS OF INT_CONST DD INT_CONST -> ^( ARRAY ( index_set )+ ^( DD INT_CONST INT_CONST ) ) | ARRAY LS index_set ( CM index_set )* RS OF LB INT_CONST ( CM INT_CONST )* RB -> ^( ARRAY ( index_set )+ ^( CM ( INT_CONST )+ ) ) | ARRAY LS index_set ( CM index_set )* RS OF SET OF INT_CONST DD INT_CONST -> ^( ARRAY ( index_set )+ ^( SET ^( DD INT_CONST INT_CONST ) ) ) | ARRAY LS index_set ( CM index_set )* RS OF SET OF LB INT_CONST ( CM INT_CONST )* RB -> ^( ARRAY ( index_set )+ ^( SET ^( CM ( INT_CONST )+ ) ) ) );";
        }
    }
    static final String DFA48_eotS =
        "\27\uffff";
    static final String DFA48_eofS =
        "\27\uffff";
    static final String DFA48_minS =
        "\1\10\1\54\1\15\1\46\1\65\1\uffff\1\27\1\21\1\46\1\47\1\46\1\65"+
        "\1\uffff\1\21\1\27\1\21\1\125\1\47\1\15\1\21\1\65\1\46\1\uffff";
    static final String DFA48_maxS =
        "\1\125\1\54\1\114\1\47\1\65\1\uffff\1\27\1\111\1\51\2\47\1\65\1"+
        "\uffff\1\111\1\27\1\111\1\125\1\47\1\114\1\111\1\65\1\51\1\uffff";
    static final String DFA48_acceptS =
        "\5\uffff\1\1\6\uffff\1\2\11\uffff\1\3";
    static final String DFA48_specialS =
        "\27\uffff}>";
    static final String[] DFA48_transitionS = {
            "\1\1\114\uffff\1\2",
            "\1\3",
            "\1\5\21\uffff\1\5\6\uffff\2\5\1\uffff\1\5\42\uffff\1\4",
            "\1\7\1\6",
            "\1\10",
            "",
            "\1\11",
            "\1\12\67\uffff\1\13",
            "\1\14\1\5\1\uffff\1\5",
            "\1\15",
            "\1\17\1\16",
            "\1\20",
            "",
            "\1\12\67\uffff\1\13",
            "\1\21",
            "\1\12\67\uffff\1\13",
            "\1\22",
            "\1\23",
            "\1\5\21\uffff\1\5\6\uffff\2\5\1\uffff\1\5\42\uffff\1\24",
            "\1\12\67\uffff\1\13",
            "\1\25",
            "\1\26\1\5\1\uffff\1\5",
            ""
    };

    static final short[] DFA48_eot = DFA.unpackEncodedString(DFA48_eotS);
    static final short[] DFA48_eof = DFA.unpackEncodedString(DFA48_eofS);
    static final char[] DFA48_min = DFA.unpackEncodedStringToUnsignedChars(DFA48_minS);
    static final char[] DFA48_max = DFA.unpackEncodedStringToUnsignedChars(DFA48_maxS);
    static final short[] DFA48_accept = DFA.unpackEncodedString(DFA48_acceptS);
    static final short[] DFA48_special = DFA.unpackEncodedString(DFA48_specialS);
    static final short[][] DFA48_transition;

    static {
        int numStates = DFA48_transitionS.length;
        DFA48_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA48_transition[i] = DFA.unpackEncodedString(DFA48_transitionS[i]);
        }
    }

    class DFA48 extends DFA {

        public DFA48(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 48;
            this.eot = DFA48_eot;
            this.eof = DFA48_eof;
            this.min = DFA48_min;
            this.max = DFA48_max;
            this.accept = DFA48_accept;
            this.special = DFA48_special;
            this.transition = DFA48_transition;
        }
        public String getDescription() {
            return "261:1: var_pred_param_type : ( var_type -> ^( VAR var_type ) | VAR SET OF INT -> ^( VAR SET ) | ARRAY LS index_set ( CM index_set )* RS OF VAR SET OF INT -> ^( ARRAY ( index_set )+ ^( VAR SET ) ) );";
        }
    }
 

    public static final BitSet FOLLOW_pred_decl_in_flatzinc_ext_model67 = new BitSet(new long[]{0x0000244A80102100L,0x00000000002050A8L});
    public static final BitSet FOLLOW_param_decl_in_flatzinc_ext_model72 = new BitSet(new long[]{0x0000244A80102100L,0x00000000002050A0L});
    public static final BitSet FOLLOW_var_decl_in_flatzinc_ext_model77 = new BitSet(new long[]{0x0000240A00100100L,0x00000000002040A0L});
    public static final BitSet FOLLOW_constraint_in_flatzinc_ext_model82 = new BitSet(new long[]{0x0000240A00100000L,0x00000000000040A0L});
    public static final BitSet FOLLOW_group_decl_in_flatzinc_ext_model87 = new BitSet(new long[]{0x0000240A00000000L,0x00000000000040A0L});
    public static final BitSet FOLLOW_structure_in_flatzinc_ext_model92 = new BitSet(new long[]{0x0000000000000000L,0x0000000000004000L});
    public static final BitSet FOLLOW_solve_goal_in_flatzinc_ext_model96 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENTIFIER_in_group_decl125 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_CL_in_group_decl127 = new BitSet(new long[]{0x8004081000244000L,0x0000000000E80016L});
    public static final BitSet FOLLOW_predicates_in_group_decl129 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000800L});
    public static final BitSet FOLLOW_SC_in_group_decl131 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_predicate_in_predicates160 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LP_in_predicates165 = new BitSet(new long[]{0x8004081000244000L,0x0000000000E80016L});
    public static final BitSet FOLLOW_predicates_in_predicates167 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_AND_in_predicates170 = new BitSet(new long[]{0x8004081000244000L,0x0000000000E80016L});
    public static final BitSet FOLLOW_predicates_in_predicates172 = new BitSet(new long[]{0x0000000000000010L,0x0000000000000100L});
    public static final BitSet FOLLOW_RP_in_predicates176 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LP_in_predicates192 = new BitSet(new long[]{0x8004081000244000L,0x0000000000E80016L});
    public static final BitSet FOLLOW_predicates_in_predicates194 = new BitSet(new long[]{0x1000000000000000L});
    public static final BitSet FOLLOW_OR_in_predicates197 = new BitSet(new long[]{0x8004081000244000L,0x0000000000E80016L});
    public static final BitSet FOLLOW_predicates_in_predicates199 = new BitSet(new long[]{0x1000000000000000L,0x0000000000000100L});
    public static final BitSet FOLLOW_RP_in_predicates203 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TRUE_in_predicate226 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_attribute_in_predicate231 = new BitSet(new long[]{0x0BD0000000000000L});
    public static final BitSet FOLLOW_op_in_predicate233 = new BitSet(new long[]{0x0000008000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_predicate235 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IN_in_predicate240 = new BitSet(new long[]{0x0000080000000000L});
    public static final BitSet FOLLOW_LP_in_predicate242 = new BitSet(new long[]{0x0000000800000000L});
    public static final BitSet FOLLOW_IDENTIFIER_in_predicate244 = new BitSet(new long[]{0x0000000000020000L,0x0000000000000100L});
    public static final BitSet FOLLOW_CM_in_predicate247 = new BitSet(new long[]{0x0000000800000000L});
    public static final BitSet FOLLOW_IDENTIFIER_in_predicate249 = new BitSet(new long[]{0x0000000000020000L,0x0000000000000100L});
    public static final BitSet FOLLOW_RP_in_predicate253 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NOT_in_predicate269 = new BitSet(new long[]{0x8004001000244000L,0x0000000000E80016L});
    public static final BitSet FOLLOW_predicate_in_predicate271 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_struct_in_structure451 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000800L});
    public static final BitSet FOLLOW_SC_in_structure453 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_struct_reg_in_structure461 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000800L});
    public static final BitSet FOLLOW_SC_in_structure463 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_coll_in_struct478 = new BitSet(new long[]{0x0020000000000000L});
    public static final BitSet FOLLOW_OF_in_struct480 = new BitSet(new long[]{0x0000020000000000L});
    public static final BitSet FOLLOW_LB_in_struct482 = new BitSet(new long[]{0x0000240A00000000L,0x00000000000000A0L});
    public static final BitSet FOLLOW_elt_in_struct484 = new BitSet(new long[]{0x0000000000020000L,0x0000000000000040L});
    public static final BitSet FOLLOW_CM_in_struct487 = new BitSet(new long[]{0x0000240A00000000L,0x00000000000000A0L});
    public static final BitSet FOLLOW_elt_in_struct489 = new BitSet(new long[]{0x0000000000020000L,0x0000000000000040L});
    public static final BitSet FOLLOW_RB_in_struct493 = new BitSet(new long[]{0x0000010000000002L});
    public static final BitSet FOLLOW_KEY_in_struct496 = new BitSet(new long[]{0x8000A00000244040L,0x0000000000E42016L});
    public static final BitSet FOLLOW_comb_attr_in_struct498 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENTIFIER_in_struct_reg530 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_AS_in_struct_reg532 = new BitSet(new long[]{0x0000240200000000L,0x00000000000000A0L});
    public static final BitSet FOLLOW_coll_in_struct_reg534 = new BitSet(new long[]{0x0020000000000000L});
    public static final BitSet FOLLOW_OF_in_struct_reg536 = new BitSet(new long[]{0x0000020000000000L});
    public static final BitSet FOLLOW_LB_in_struct_reg538 = new BitSet(new long[]{0x0000000002000000L});
    public static final BitSet FOLLOW_many_in_struct_reg540 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
    public static final BitSet FOLLOW_RB_in_struct_reg542 = new BitSet(new long[]{0x0000010000000002L});
    public static final BitSet FOLLOW_KEY_in_struct_reg545 = new BitSet(new long[]{0x8000A00000244040L,0x0000000000E42016L});
    public static final BitSet FOLLOW_comb_attr_in_struct_reg547 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_struct_reg_in_elt581 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_struct_in_elt591 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENTIFIER_in_elt601 = new BitSet(new long[]{0x0000010000000002L});
    public static final BitSet FOLLOW_KEY_in_elt604 = new BitSet(new long[]{0x8000000000244000L,0x0000000000E00016L});
    public static final BitSet FOLLOW_attribute_in_elt606 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_EACH_in_many622 = new BitSet(new long[]{0x8000000000244000L,0x0000000000E00016L});
    public static final BitSet FOLLOW_attribute_in_many624 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_AS_in_many626 = new BitSet(new long[]{0x0000240200000000L,0x00000000000000A0L});
    public static final BitSet FOLLOW_coll_in_many628 = new BitSet(new long[]{0x0020010000000002L});
    public static final BitSet FOLLOW_OF_in_many631 = new BitSet(new long[]{0x0000020000000000L});
    public static final BitSet FOLLOW_LB_in_many633 = new BitSet(new long[]{0x0000000002000000L});
    public static final BitSet FOLLOW_many_in_many637 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
    public static final BitSet FOLLOW_RB_in_many639 = new BitSet(new long[]{0x0000010000000002L});
    public static final BitSet FOLLOW_KEY_in_many644 = new BitSet(new long[]{0x8000A00000244040L,0x0000000000E42016L});
    public static final BitSet FOLLOW_comb_attr_in_many646 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_QUEUE_in_coll716 = new BitSet(new long[]{0x0000080000000000L});
    public static final BitSet FOLLOW_LP_in_coll718 = new BitSet(new long[]{0x0400000000000000L,0x0000000002000000L});
    public static final BitSet FOLLOW_qiter_in_coll721 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000100L});
    public static final BitSet FOLLOW_RP_in_coll723 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_REV_in_coll733 = new BitSet(new long[]{0x0000040000000000L});
    public static final BitSet FOLLOW_LIST_in_coll737 = new BitSet(new long[]{0x0000080000000000L});
    public static final BitSet FOLLOW_LP_in_coll739 = new BitSet(new long[]{0x0400000100000000L,0x0000000003000000L});
    public static final BitSet FOLLOW_liter_in_coll742 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000100L});
    public static final BitSet FOLLOW_RP_in_coll744 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MAX_in_coll754 = new BitSet(new long[]{0x0000000200000000L});
    public static final BitSet FOLLOW_HEAP_in_coll758 = new BitSet(new long[]{0x0000080000000000L});
    public static final BitSet FOLLOW_LP_in_coll761 = new BitSet(new long[]{0x0400000000000000L,0x0000000002000000L});
    public static final BitSet FOLLOW_qiter_in_coll764 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000100L});
    public static final BitSet FOLLOW_RP_in_coll766 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_qiter_in_liter808 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FOR_in_liter818 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_WFOR_in_liter828 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_attr_op_in_comb_attr843 = new BitSet(new long[]{0x0000000001000002L});
    public static final BitSet FOLLOW_DO_in_comb_attr846 = new BitSet(new long[]{0x0000A00000000040L,0x0000000000042000L});
    public static final BitSet FOLLOW_attr_op_in_comb_attr848 = new BitSet(new long[]{0x0000000001000002L});
    public static final BitSet FOLLOW_DO_in_comb_attr854 = new BitSet(new long[]{0x8000000000244000L,0x0000000000E00016L});
    public static final BitSet FOLLOW_attribute_in_comb_attr856 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_attr_op_in_comb_attr880 = new BitSet(new long[]{0x0000000001000000L});
    public static final BitSet FOLLOW_DO_in_comb_attr882 = new BitSet(new long[]{0x8000A00000244040L,0x0000000000E42016L});
    public static final BitSet FOLLOW_attribute_in_comb_attr886 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PREDICATE_in_pred_decl974 = new BitSet(new long[]{0x0000000800000000L});
    public static final BitSet FOLLOW_IDENTIFIER_in_pred_decl976 = new BitSet(new long[]{0x0000080000000000L});
    public static final BitSet FOLLOW_LP_in_pred_decl978 = new BitSet(new long[]{0x000002C080002100L,0x0000000000201000L});
    public static final BitSet FOLLOW_pred_param_in_pred_decl980 = new BitSet(new long[]{0x0000000000020000L,0x0000000000000100L});
    public static final BitSet FOLLOW_CM_in_pred_decl983 = new BitSet(new long[]{0x000002C080002100L,0x0000000000201000L});
    public static final BitSet FOLLOW_pred_param_in_pred_decl985 = new BitSet(new long[]{0x0000000000020000L,0x0000000000000100L});
    public static final BitSet FOLLOW_RP_in_pred_decl989 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000800L});
    public static final BitSet FOLLOW_SC_in_pred_decl991 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_pred_param_type_in_pred_param1019 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_CL_in_pred_param1021 = new BitSet(new long[]{0x0000000800000000L});
    public static final BitSet FOLLOW_IDENTIFIER_in_pred_param1023 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_par_pred_param_type_in_pred_param_type1057 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_var_pred_param_type_in_pred_param_type1067 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ARRAY_in_par_type1086 = new BitSet(new long[]{0x0000100000000000L});
    public static final BitSet FOLLOW_LS_in_par_type1088 = new BitSet(new long[]{0x000000C000000000L});
    public static final BitSet FOLLOW_index_set_in_par_type1090 = new BitSet(new long[]{0x0000000000020000L,0x0000000000000200L});
    public static final BitSet FOLLOW_CM_in_par_type1093 = new BitSet(new long[]{0x000000C000000000L});
    public static final BitSet FOLLOW_index_set_in_par_type1095 = new BitSet(new long[]{0x0000000000020000L,0x0000000000000200L});
    public static final BitSet FOLLOW_RS_in_par_type1099 = new BitSet(new long[]{0x0020000000000000L});
    public static final BitSet FOLLOW_OF_in_par_type1101 = new BitSet(new long[]{0x0000004080002000L,0x0000000000001000L});
    public static final BitSet FOLLOW_par_type_u_in_par_type1103 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_par_type_u_in_par_type1129 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_BOOL_in_par_type_u1161 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FLOAT_in_par_type_u1171 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SET_in_par_type_u1181 = new BitSet(new long[]{0x0020000000000000L});
    public static final BitSet FOLLOW_OF_in_par_type_u1183 = new BitSet(new long[]{0x0000004000000000L});
    public static final BitSet FOLLOW_INT_in_par_type_u1185 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INT_in_par_type_u1195 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ARRAY_in_var_type1214 = new BitSet(new long[]{0x0000100000000000L});
    public static final BitSet FOLLOW_LS_in_var_type1216 = new BitSet(new long[]{0x000000C000000000L});
    public static final BitSet FOLLOW_index_set_in_var_type1218 = new BitSet(new long[]{0x0000000000020000L,0x0000000000000200L});
    public static final BitSet FOLLOW_CM_in_var_type1221 = new BitSet(new long[]{0x000000C000000000L});
    public static final BitSet FOLLOW_index_set_in_var_type1223 = new BitSet(new long[]{0x0000000000020000L,0x0000000000000200L});
    public static final BitSet FOLLOW_RS_in_var_type1227 = new BitSet(new long[]{0x0020000000000000L});
    public static final BitSet FOLLOW_OF_in_var_type1229 = new BitSet(new long[]{0x0000000000000000L,0x0000000000200000L});
    public static final BitSet FOLLOW_VAR_in_var_type1231 = new BitSet(new long[]{0x000002C080002000L,0x0000000000001000L});
    public static final BitSet FOLLOW_var_type_u_in_var_type1233 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_VAR_in_var_type1259 = new BitSet(new long[]{0x000002C080002000L,0x0000000000001000L});
    public static final BitSet FOLLOW_var_type_u_in_var_type1261 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_BOOL_in_var_type_u1293 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FLOAT_in_var_type_u1303 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INT_in_var_type_u1313 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INT_CONST_in_var_type_u1323 = new BitSet(new long[]{0x0000000000800000L});
    public static final BitSet FOLLOW_DD_in_var_type_u1325 = new BitSet(new long[]{0x0000008000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_var_type_u1327 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LB_in_var_type_u1354 = new BitSet(new long[]{0x0000008000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_var_type_u1356 = new BitSet(new long[]{0x0000000000020000L,0x0000000000000040L});
    public static final BitSet FOLLOW_CM_in_var_type_u1359 = new BitSet(new long[]{0x0000008000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_var_type_u1361 = new BitSet(new long[]{0x0000000000020000L,0x0000000000000040L});
    public static final BitSet FOLLOW_RB_in_var_type_u1365 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SET_in_var_type_u1389 = new BitSet(new long[]{0x0020000000000000L});
    public static final BitSet FOLLOW_OF_in_var_type_u1391 = new BitSet(new long[]{0x0000008000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_var_type_u1393 = new BitSet(new long[]{0x0000000000800000L});
    public static final BitSet FOLLOW_DD_in_var_type_u1395 = new BitSet(new long[]{0x0000008000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_var_type_u1397 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SET_in_var_type_u1426 = new BitSet(new long[]{0x0020000000000000L});
    public static final BitSet FOLLOW_OF_in_var_type_u1428 = new BitSet(new long[]{0x0000020000000000L});
    public static final BitSet FOLLOW_LB_in_var_type_u1430 = new BitSet(new long[]{0x0000008000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_var_type_u1432 = new BitSet(new long[]{0x0000000000020000L,0x0000000000000040L});
    public static final BitSet FOLLOW_CM_in_var_type_u1435 = new BitSet(new long[]{0x0000008000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_var_type_u1437 = new BitSet(new long[]{0x0000000000020000L,0x0000000000000040L});
    public static final BitSet FOLLOW_RB_in_var_type_u1441 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_par_type_in_par_pred_param_type1479 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type1491 = new BitSet(new long[]{0x0000000000800000L});
    public static final BitSet FOLLOW_DD_in_par_pred_param_type1493 = new BitSet(new long[]{0x0000008000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type1495 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LB_in_par_pred_param_type1520 = new BitSet(new long[]{0x0000008000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type1522 = new BitSet(new long[]{0x0000000000020000L,0x0000000000000040L});
    public static final BitSet FOLLOW_CM_in_par_pred_param_type1525 = new BitSet(new long[]{0x0000008000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type1527 = new BitSet(new long[]{0x0000000000020000L,0x0000000000000040L});
    public static final BitSet FOLLOW_RB_in_par_pred_param_type1531 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SET_in_par_pred_param_type1555 = new BitSet(new long[]{0x0020000000000000L});
    public static final BitSet FOLLOW_OF_in_par_pred_param_type1557 = new BitSet(new long[]{0x0000008000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type1559 = new BitSet(new long[]{0x0000000000800000L});
    public static final BitSet FOLLOW_DD_in_par_pred_param_type1561 = new BitSet(new long[]{0x0000008000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type1563 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SET_in_par_pred_param_type1592 = new BitSet(new long[]{0x0020000000000000L});
    public static final BitSet FOLLOW_OF_in_par_pred_param_type1594 = new BitSet(new long[]{0x0000020000000000L});
    public static final BitSet FOLLOW_LB_in_par_pred_param_type1596 = new BitSet(new long[]{0x0000008000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type1598 = new BitSet(new long[]{0x0000000000020000L,0x0000000000000040L});
    public static final BitSet FOLLOW_CM_in_par_pred_param_type1601 = new BitSet(new long[]{0x0000008000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type1603 = new BitSet(new long[]{0x0000000000020000L,0x0000000000000040L});
    public static final BitSet FOLLOW_RB_in_par_pred_param_type1607 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ARRAY_in_par_pred_param_type1637 = new BitSet(new long[]{0x0000100000000000L});
    public static final BitSet FOLLOW_LS_in_par_pred_param_type1639 = new BitSet(new long[]{0x000000C000000000L});
    public static final BitSet FOLLOW_index_set_in_par_pred_param_type1641 = new BitSet(new long[]{0x0000000000020000L,0x0000000000000200L});
    public static final BitSet FOLLOW_CM_in_par_pred_param_type1644 = new BitSet(new long[]{0x000000C000000000L});
    public static final BitSet FOLLOW_index_set_in_par_pred_param_type1646 = new BitSet(new long[]{0x0000000000020000L,0x0000000000000200L});
    public static final BitSet FOLLOW_RS_in_par_pred_param_type1650 = new BitSet(new long[]{0x0020000000000000L});
    public static final BitSet FOLLOW_OF_in_par_pred_param_type1652 = new BitSet(new long[]{0x0000008000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type1654 = new BitSet(new long[]{0x0000000000800000L});
    public static final BitSet FOLLOW_DD_in_par_pred_param_type1656 = new BitSet(new long[]{0x0000008000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type1658 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ARRAY_in_par_pred_param_type1690 = new BitSet(new long[]{0x0000100000000000L});
    public static final BitSet FOLLOW_LS_in_par_pred_param_type1692 = new BitSet(new long[]{0x000000C000000000L});
    public static final BitSet FOLLOW_index_set_in_par_pred_param_type1694 = new BitSet(new long[]{0x0000000000020000L,0x0000000000000200L});
    public static final BitSet FOLLOW_CM_in_par_pred_param_type1697 = new BitSet(new long[]{0x000000C000000000L});
    public static final BitSet FOLLOW_index_set_in_par_pred_param_type1699 = new BitSet(new long[]{0x0000000000020000L,0x0000000000000200L});
    public static final BitSet FOLLOW_RS_in_par_pred_param_type1703 = new BitSet(new long[]{0x0020000000000000L});
    public static final BitSet FOLLOW_OF_in_par_pred_param_type1705 = new BitSet(new long[]{0x0000020000000000L});
    public static final BitSet FOLLOW_LB_in_par_pred_param_type1707 = new BitSet(new long[]{0x0000008000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type1709 = new BitSet(new long[]{0x0000000000020000L,0x0000000000000040L});
    public static final BitSet FOLLOW_CM_in_par_pred_param_type1712 = new BitSet(new long[]{0x0000008000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type1714 = new BitSet(new long[]{0x0000000000020000L,0x0000000000000040L});
    public static final BitSet FOLLOW_RB_in_par_pred_param_type1718 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ARRAY_in_par_pred_param_type1749 = new BitSet(new long[]{0x0000100000000000L});
    public static final BitSet FOLLOW_LS_in_par_pred_param_type1751 = new BitSet(new long[]{0x000000C000000000L});
    public static final BitSet FOLLOW_index_set_in_par_pred_param_type1753 = new BitSet(new long[]{0x0000000000020000L,0x0000000000000200L});
    public static final BitSet FOLLOW_CM_in_par_pred_param_type1756 = new BitSet(new long[]{0x000000C000000000L});
    public static final BitSet FOLLOW_index_set_in_par_pred_param_type1758 = new BitSet(new long[]{0x0000000000020000L,0x0000000000000200L});
    public static final BitSet FOLLOW_RS_in_par_pred_param_type1762 = new BitSet(new long[]{0x0020000000000000L});
    public static final BitSet FOLLOW_OF_in_par_pred_param_type1764 = new BitSet(new long[]{0x0000000000000000L,0x0000000000001000L});
    public static final BitSet FOLLOW_SET_in_par_pred_param_type1766 = new BitSet(new long[]{0x0020000000000000L});
    public static final BitSet FOLLOW_OF_in_par_pred_param_type1768 = new BitSet(new long[]{0x0000008000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type1770 = new BitSet(new long[]{0x0000000000800000L});
    public static final BitSet FOLLOW_DD_in_par_pred_param_type1772 = new BitSet(new long[]{0x0000008000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type1774 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ARRAY_in_par_pred_param_type1810 = new BitSet(new long[]{0x0000100000000000L});
    public static final BitSet FOLLOW_LS_in_par_pred_param_type1812 = new BitSet(new long[]{0x000000C000000000L});
    public static final BitSet FOLLOW_index_set_in_par_pred_param_type1814 = new BitSet(new long[]{0x0000000000020000L,0x0000000000000200L});
    public static final BitSet FOLLOW_CM_in_par_pred_param_type1817 = new BitSet(new long[]{0x000000C000000000L});
    public static final BitSet FOLLOW_index_set_in_par_pred_param_type1819 = new BitSet(new long[]{0x0000000000020000L,0x0000000000000200L});
    public static final BitSet FOLLOW_RS_in_par_pred_param_type1823 = new BitSet(new long[]{0x0020000000000000L});
    public static final BitSet FOLLOW_OF_in_par_pred_param_type1825 = new BitSet(new long[]{0x0000000000000000L,0x0000000000001000L});
    public static final BitSet FOLLOW_SET_in_par_pred_param_type1827 = new BitSet(new long[]{0x0020000000000000L});
    public static final BitSet FOLLOW_OF_in_par_pred_param_type1829 = new BitSet(new long[]{0x0000020000000000L});
    public static final BitSet FOLLOW_LB_in_par_pred_param_type1831 = new BitSet(new long[]{0x0000008000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type1833 = new BitSet(new long[]{0x0000000000020000L,0x0000000000000040L});
    public static final BitSet FOLLOW_CM_in_par_pred_param_type1836 = new BitSet(new long[]{0x0000008000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type1838 = new BitSet(new long[]{0x0000000000020000L,0x0000000000000040L});
    public static final BitSet FOLLOW_RB_in_par_pred_param_type1842 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_var_type_in_var_pred_param_type1887 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_VAR_in_var_pred_param_type1910 = new BitSet(new long[]{0x0000000000000000L,0x0000000000001000L});
    public static final BitSet FOLLOW_SET_in_var_pred_param_type1912 = new BitSet(new long[]{0x0020000000000000L});
    public static final BitSet FOLLOW_OF_in_var_pred_param_type1914 = new BitSet(new long[]{0x0000004000000000L});
    public static final BitSet FOLLOW_INT_in_var_pred_param_type1916 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ARRAY_in_var_pred_param_type1939 = new BitSet(new long[]{0x0000100000000000L});
    public static final BitSet FOLLOW_LS_in_var_pred_param_type1941 = new BitSet(new long[]{0x000000C000000000L});
    public static final BitSet FOLLOW_index_set_in_var_pred_param_type1943 = new BitSet(new long[]{0x0000000000020000L,0x0000000000000200L});
    public static final BitSet FOLLOW_CM_in_var_pred_param_type1946 = new BitSet(new long[]{0x000000C000000000L});
    public static final BitSet FOLLOW_index_set_in_var_pred_param_type1948 = new BitSet(new long[]{0x0000000000020000L,0x0000000000000200L});
    public static final BitSet FOLLOW_RS_in_var_pred_param_type1952 = new BitSet(new long[]{0x0020000000000000L});
    public static final BitSet FOLLOW_OF_in_var_pred_param_type1954 = new BitSet(new long[]{0x0000000000000000L,0x0000000000200000L});
    public static final BitSet FOLLOW_VAR_in_var_pred_param_type1956 = new BitSet(new long[]{0x0000000000000000L,0x0000000000001000L});
    public static final BitSet FOLLOW_SET_in_var_pred_param_type1958 = new BitSet(new long[]{0x0020000000000000L});
    public static final BitSet FOLLOW_OF_in_var_pred_param_type1960 = new BitSet(new long[]{0x0000004000000000L});
    public static final BitSet FOLLOW_INT_in_var_pred_param_type1962 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INT_CONST_in_index_set2001 = new BitSet(new long[]{0x0000000000800000L});
    public static final BitSet FOLLOW_DD_in_index_set2003 = new BitSet(new long[]{0x0000008000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_index_set2005 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INT_in_index_set2034 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LB_in_expr2066 = new BitSet(new long[]{0x0000008000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_expr2068 = new BitSet(new long[]{0x0000000000020000L,0x0000000000000040L});
    public static final BitSet FOLLOW_CM_in_expr2071 = new BitSet(new long[]{0x0000008000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_expr2073 = new BitSet(new long[]{0x0000000000020000L,0x0000000000000040L});
    public static final BitSet FOLLOW_RB_in_expr2077 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_bool_const_in_expr2101 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INT_CONST_in_expr2111 = new BitSet(new long[]{0x0000000000800002L});
    public static final BitSet FOLLOW_DD_in_expr2114 = new BitSet(new long[]{0x0000008000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_expr2116 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LS_in_expr2128 = new BitSet(new long[]{0x0000128840000000L,0x0000000000090200L});
    public static final BitSet FOLLOW_expr_in_expr2131 = new BitSet(new long[]{0x0000000000020000L,0x0000000000000200L});
    public static final BitSet FOLLOW_CM_in_expr2134 = new BitSet(new long[]{0x0000128840000000L,0x0000000000090000L});
    public static final BitSet FOLLOW_expr_in_expr2136 = new BitSet(new long[]{0x0000000000020000L,0x0000000000000200L});
    public static final BitSet FOLLOW_RS_in_expr2142 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_id_expr_in_expr2170 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_in_expr2180 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENTIFIER_in_id_expr2201 = new BitSet(new long[]{0x0000180000000002L});
    public static final BitSet FOLLOW_LP_in_id_expr2205 = new BitSet(new long[]{0x0000128840000000L,0x0000000000090000L});
    public static final BitSet FOLLOW_expr_in_id_expr2207 = new BitSet(new long[]{0x0000000000020000L,0x0000000000000100L});
    public static final BitSet FOLLOW_CM_in_id_expr2210 = new BitSet(new long[]{0x0000128840000000L,0x0000000000090000L});
    public static final BitSet FOLLOW_expr_in_id_expr2212 = new BitSet(new long[]{0x0000000000020000L,0x0000000000000100L});
    public static final BitSet FOLLOW_RP_in_id_expr2216 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LS_in_id_expr2220 = new BitSet(new long[]{0x0000008000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_id_expr2222 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000200L});
    public static final BitSet FOLLOW_RS_in_id_expr2224 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_par_type_in_param_decl2244 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_CL_in_param_decl2246 = new BitSet(new long[]{0x0000000800000000L});
    public static final BitSet FOLLOW_IDENTIFIER_in_param_decl2248 = new BitSet(new long[]{0x0000000004000000L});
    public static final BitSet FOLLOW_EQ_in_param_decl2250 = new BitSet(new long[]{0x0000128840000000L,0x0000000000090000L});
    public static final BitSet FOLLOW_expr_in_param_decl2252 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000800L});
    public static final BitSet FOLLOW_SC_in_param_decl2254 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_var_type_in_var_decl2282 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_CL_in_var_decl2284 = new BitSet(new long[]{0x0000000800000000L});
    public static final BitSet FOLLOW_IDENTIFIER_in_var_decl2286 = new BitSet(new long[]{0x0000000004400000L,0x0000000000000800L});
    public static final BitSet FOLLOW_annotations_in_var_decl2288 = new BitSet(new long[]{0x0000000004000000L,0x0000000000000800L});
    public static final BitSet FOLLOW_EQ_in_var_decl2291 = new BitSet(new long[]{0x0000128840000000L,0x0000000000090000L});
    public static final BitSet FOLLOW_expr_in_var_decl2293 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000800L});
    public static final BitSet FOLLOW_SC_in_var_decl2297 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CONSTRAINT_in_constraint2327 = new BitSet(new long[]{0x0000000800000000L});
    public static final BitSet FOLLOW_IDENTIFIER_in_constraint2329 = new BitSet(new long[]{0x0000080000000000L});
    public static final BitSet FOLLOW_LP_in_constraint2331 = new BitSet(new long[]{0x0000128840000000L,0x0000000000090000L});
    public static final BitSet FOLLOW_expr_in_constraint2333 = new BitSet(new long[]{0x0000000000020000L,0x0000000000000100L});
    public static final BitSet FOLLOW_CM_in_constraint2336 = new BitSet(new long[]{0x0000128840000000L,0x0000000000090000L});
    public static final BitSet FOLLOW_expr_in_constraint2338 = new BitSet(new long[]{0x0000000000020000L,0x0000000000000100L});
    public static final BitSet FOLLOW_RP_in_constraint2342 = new BitSet(new long[]{0x0000000000400000L,0x0000000000000800L});
    public static final BitSet FOLLOW_annotations_in_constraint2344 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000800L});
    public static final BitSet FOLLOW_SC_in_constraint2346 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SOLVE_in_solve_goal2374 = new BitSet(new long[]{0x0001400000400000L,0x0000000000000400L});
    public static final BitSet FOLLOW_annotations_in_solve_goal2377 = new BitSet(new long[]{0x0001400000000000L,0x0000000000000400L});
    public static final BitSet FOLLOW_resolution_in_solve_goal2379 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000800L});
    public static final BitSet FOLLOW_SC_in_solve_goal2381 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MINIMIZE_in_resolution2398 = new BitSet(new long[]{0x0000128840000000L,0x0000000000090000L});
    public static final BitSet FOLLOW_expr_in_resolution2401 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MAXIMIZE_in_resolution2411 = new BitSet(new long[]{0x0000128840000000L,0x0000000000090000L});
    public static final BitSet FOLLOW_expr_in_resolution2414 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SATISFY_in_resolution2424 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DC_in_annotations2445 = new BitSet(new long[]{0x0000000800000000L});
    public static final BitSet FOLLOW_annotation_in_annotations2447 = new BitSet(new long[]{0x0000000000400002L});
    public static final BitSet FOLLOW_IDENTIFIER_in_annotation2482 = new BitSet(new long[]{0x0000080000000002L});
    public static final BitSet FOLLOW_LP_in_annotation2485 = new BitSet(new long[]{0x0000128840000000L,0x0000000000090000L});
    public static final BitSet FOLLOW_expr_in_annotation2487 = new BitSet(new long[]{0x0000000000020000L,0x0000000000000100L});
    public static final BitSet FOLLOW_CM_in_annotation2490 = new BitSet(new long[]{0x0000128840000000L,0x0000000000090000L});
    public static final BitSet FOLLOW_expr_in_annotation2492 = new BitSet(new long[]{0x0000000000020000L,0x0000000000000100L});
    public static final BitSet FOLLOW_RP_in_annotation2496 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TRUE_in_bool_const2537 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FALSE_in_bool_const2548 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LP_in_synpred9_FlatzincFullExtParser165 = new BitSet(new long[]{0x8004081000244000L,0x0000000000E80016L});
    public static final BitSet FOLLOW_predicates_in_synpred9_FlatzincFullExtParser167 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_AND_in_synpred9_FlatzincFullExtParser170 = new BitSet(new long[]{0x8004081000244000L,0x0000000000E80016L});
    public static final BitSet FOLLOW_predicates_in_synpred9_FlatzincFullExtParser172 = new BitSet(new long[]{0x0000000000000010L,0x0000000000000100L});
    public static final BitSet FOLLOW_RP_in_synpred9_FlatzincFullExtParser176 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_attr_op_in_synpred47_FlatzincFullExtParser843 = new BitSet(new long[]{0x0000000001000002L});
    public static final BitSet FOLLOW_DO_in_synpred47_FlatzincFullExtParser846 = new BitSet(new long[]{0x0000A00000000040L,0x0000000000042000L});
    public static final BitSet FOLLOW_attr_op_in_synpred47_FlatzincFullExtParser848 = new BitSet(new long[]{0x0000000001000002L});
    public static final BitSet FOLLOW_DO_in_synpred47_FlatzincFullExtParser854 = new BitSet(new long[]{0x8000000000244000L,0x0000000000E00016L});
    public static final BitSet FOLLOW_attribute_in_synpred47_FlatzincFullExtParser856 = new BitSet(new long[]{0x0000000000000002L});

}