// $ANTLR 3.4 parser/flatzinc/FlatzincFullExtParser.g 2012-11-20 17:05:08

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
import org.antlr.runtime.tree.*;


@SuppressWarnings({"all", "warnings", "unchecked"})
public class FlatzincFullExtParser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "AND", "ANNOTATIONS", "ANY", "APAR", "ARRAY", "ARRPAR", "ARRVAR", "AS", "AVAR", "BOOL", "CA1", "CA2", "CARITY", "CHAR", "CL", "CM", "CNAME", "COMMENT", "CONSTRAINT", "CSTR", "DC", "DD", "DO", "EACH", "EQ", "ESC_SEQ", "EXPONENT", "EXPR", "FALSE", "FLOAT", "FOR", "HEAP", "HEX_DIGIT", "IDENTIFIER", "IN", "INDEX", "INT", "INT_CONST", "KEY", "LB", "LIST", "LP", "LS", "MANY1", "MANY2", "MANY3", "MANY4", "MAX", "MAXIMIZE", "MIN", "MINIMIZE", "MN", "NOT", "OCTAL_ESC", "OEQ", "OF", "OGQ", "OGT", "OLQ", "OLT", "ONE", "ONQ", "OR", "ORDERBY", "PAR", "PARITY", "PL", "PPRIO", "PPRIOD", "PREDICATE", "PROP", "QUEUE", "RB", "REV", "RP", "RS", "SATISFY", "SC", "SET", "SIZE", "SOLVE", "STREG", "STRING", "STRUC", "SUM", "TRUE", "UNICODE_ESC", "VAR", "VCARD", "VNAME", "WFOR", "WONE", "WS"
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
    public static final int CA1=14;
    public static final int CA2=15;
    public static final int CARITY=16;
    public static final int CHAR=17;
    public static final int CL=18;
    public static final int CM=19;
    public static final int CNAME=20;
    public static final int COMMENT=21;
    public static final int CONSTRAINT=22;
    public static final int CSTR=23;
    public static final int DC=24;
    public static final int DD=25;
    public static final int DO=26;
    public static final int EACH=27;
    public static final int EQ=28;
    public static final int ESC_SEQ=29;
    public static final int EXPONENT=30;
    public static final int EXPR=31;
    public static final int FALSE=32;
    public static final int FLOAT=33;
    public static final int FOR=34;
    public static final int HEAP=35;
    public static final int HEX_DIGIT=36;
    public static final int IDENTIFIER=37;
    public static final int IN=38;
    public static final int INDEX=39;
    public static final int INT=40;
    public static final int INT_CONST=41;
    public static final int KEY=42;
    public static final int LB=43;
    public static final int LIST=44;
    public static final int LP=45;
    public static final int LS=46;
    public static final int MANY1=47;
    public static final int MANY2=48;
    public static final int MANY3=49;
    public static final int MANY4=50;
    public static final int MAX=51;
    public static final int MAXIMIZE=52;
    public static final int MIN=53;
    public static final int MINIMIZE=54;
    public static final int MN=55;
    public static final int NOT=56;
    public static final int OCTAL_ESC=57;
    public static final int OEQ=58;
    public static final int OF=59;
    public static final int OGQ=60;
    public static final int OGT=61;
    public static final int OLQ=62;
    public static final int OLT=63;
    public static final int ONE=64;
    public static final int ONQ=65;
    public static final int OR=66;
    public static final int ORDERBY=67;
    public static final int PAR=68;
    public static final int PARITY=69;
    public static final int PL=70;
    public static final int PPRIO=71;
    public static final int PPRIOD=72;
    public static final int PREDICATE=73;
    public static final int PROP=74;
    public static final int QUEUE=75;
    public static final int RB=76;
    public static final int REV=77;
    public static final int RP=78;
    public static final int RS=79;
    public static final int SATISFY=80;
    public static final int SC=81;
    public static final int SET=82;
    public static final int SIZE=83;
    public static final int SOLVE=84;
    public static final int STREG=85;
    public static final int STRING=86;
    public static final int STRUC=87;
    public static final int SUM=88;
    public static final int TRUE=89;
    public static final int UNICODE_ESC=90;
    public static final int VAR=91;
    public static final int VCARD=92;
    public static final int VNAME=93;
    public static final int WFOR=94;
    public static final int WONE=95;
    public static final int WS=96;

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
                    // elements: predicates, OR
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
            // elements: comb_attr, elt, coll
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
    // parser/flatzinc/FlatzincFullExtParser.g:123:1: struct_reg : IDENTIFIER AS coll OF LB many RB ( KEY ca= comb_attr )? -> {ca==null}? ^( STREG IDENTIFIER many coll ) -> ^( STREG IDENTIFIER comb_attr many coll ) ;
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
        FlatzincFullExtParser.comb_attr_return ca =null;

        FlatzincFullExtParser.coll_return coll52 =null;

        FlatzincFullExtParser.many_return many55 =null;


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
            // parser/flatzinc/FlatzincFullExtParser.g:124:2: ( IDENTIFIER AS coll OF LB many RB ( KEY ca= comb_attr )? -> {ca==null}? ^( STREG IDENTIFIER many coll ) -> ^( STREG IDENTIFIER comb_attr many coll ) )
            // parser/flatzinc/FlatzincFullExtParser.g:124:4: IDENTIFIER AS coll OF LB many RB ( KEY ca= comb_attr )?
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


            // parser/flatzinc/FlatzincFullExtParser.g:124:37: ( KEY ca= comb_attr )?
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
                    // parser/flatzinc/FlatzincFullExtParser.g:124:38: KEY ca= comb_attr
                    {
                    KEY57=(Token)match(input,KEY,FOLLOW_KEY_in_struct_reg545); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_KEY.add(KEY57);


                    pushFollow(FOLLOW_comb_attr_in_struct_reg549);
                    ca=comb_attr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_comb_attr.add(ca.getTree());

                    }
                    break;

            }


            // AST REWRITE
            // elements: IDENTIFIER, many, many, comb_attr, coll, IDENTIFIER, coll
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {

            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 125:2: -> {ca==null}? ^( STREG IDENTIFIER many coll )
            if (ca==null) {
                // parser/flatzinc/FlatzincFullExtParser.g:125:22: ^( STREG IDENTIFIER many coll )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot(
                (Object)adaptor.create(STREG, "STREG")
                , root_1);

                adaptor.addChild(root_1, 
                stream_IDENTIFIER.nextNode()
                );

                adaptor.addChild(root_1, stream_many.nextTree());

                adaptor.addChild(root_1, stream_coll.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            else // 126:2: -> ^( STREG IDENTIFIER comb_attr many coll )
            {
                // parser/flatzinc/FlatzincFullExtParser.g:126:22: ^( STREG IDENTIFIER comb_attr many coll )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot(
                (Object)adaptor.create(STREG, "STREG")
                , root_1);

                adaptor.addChild(root_1, 
                stream_IDENTIFIER.nextNode()
                );

                adaptor.addChild(root_1, stream_comb_attr.nextTree());

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
    // $ANTLR end "struct_reg"


    public static class elt_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "elt"
    // parser/flatzinc/FlatzincFullExtParser.g:130:1: elt : ( struct_reg | struct | IDENTIFIER ( KEY attribute )? );
    public final FlatzincFullExtParser.elt_return elt() throws RecognitionException {
        FlatzincFullExtParser.elt_return retval = new FlatzincFullExtParser.elt_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token IDENTIFIER60=null;
        Token KEY61=null;
        FlatzincFullExtParser.struct_reg_return struct_reg58 =null;

        FlatzincFullExtParser.struct_return struct59 =null;

        FlatzincFullExtParser.attribute_return attribute62 =null;


        Object IDENTIFIER60_tree=null;
        Object KEY61_tree=null;

        try {
            // parser/flatzinc/FlatzincFullExtParser.g:131:5: ( struct_reg | struct | IDENTIFIER ( KEY attribute )? )
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
                    // parser/flatzinc/FlatzincFullExtParser.g:131:7: struct_reg
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_struct_reg_in_elt618);
                    struct_reg58=struct_reg();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, struct_reg58.getTree());

                    }
                    break;
                case 2 :
                    // parser/flatzinc/FlatzincFullExtParser.g:132:9: struct
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_struct_in_elt628);
                    struct59=struct();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, struct59.getTree());

                    }
                    break;
                case 3 :
                    // parser/flatzinc/FlatzincFullExtParser.g:133:9: IDENTIFIER ( KEY attribute )?
                    {
                    root_0 = (Object)adaptor.nil();


                    IDENTIFIER60=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_elt638); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    IDENTIFIER60_tree = 
                    (Object)adaptor.create(IDENTIFIER60)
                    ;
                    adaptor.addChild(root_0, IDENTIFIER60_tree);
                    }

                    // parser/flatzinc/FlatzincFullExtParser.g:133:20: ( KEY attribute )?
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
                            // parser/flatzinc/FlatzincFullExtParser.g:133:21: KEY attribute
                            {
                            KEY61=(Token)match(input,KEY,FOLLOW_KEY_in_elt641); if (state.failed) return retval;
                            if ( state.backtracking==0 ) {
                            KEY61_tree = 
                            (Object)adaptor.create(KEY61)
                            ;
                            adaptor.addChild(root_0, KEY61_tree);
                            }

                            pushFollow(FOLLOW_attribute_in_elt643);
                            attribute62=attribute();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) adaptor.addChild(root_0, attribute62.getTree());

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
    // parser/flatzinc/FlatzincFullExtParser.g:136:1: many : EACH attribute AS coll ( OF LB m= many RB )? ( KEY ca= comb_attr )? -> {m==null && ca == null}? ^( MANY1 attribute coll ) -> {m==null && ca != null}? ^( MANY2 attribute comb_attr coll ) -> {m!=null && ca == null}? ^( MANY3 attribute many coll ) -> ^( MANY4 attribute comb_attr many coll ) ;
    public final FlatzincFullExtParser.many_return many() throws RecognitionException {
        FlatzincFullExtParser.many_return retval = new FlatzincFullExtParser.many_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token EACH63=null;
        Token AS65=null;
        Token OF67=null;
        Token LB68=null;
        Token RB69=null;
        Token KEY70=null;
        FlatzincFullExtParser.many_return m =null;

        FlatzincFullExtParser.comb_attr_return ca =null;

        FlatzincFullExtParser.attribute_return attribute64 =null;

        FlatzincFullExtParser.coll_return coll66 =null;


        Object EACH63_tree=null;
        Object AS65_tree=null;
        Object OF67_tree=null;
        Object LB68_tree=null;
        Object RB69_tree=null;
        Object KEY70_tree=null;
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
            // parser/flatzinc/FlatzincFullExtParser.g:137:5: ( EACH attribute AS coll ( OF LB m= many RB )? ( KEY ca= comb_attr )? -> {m==null && ca == null}? ^( MANY1 attribute coll ) -> {m==null && ca != null}? ^( MANY2 attribute comb_attr coll ) -> {m!=null && ca == null}? ^( MANY3 attribute many coll ) -> ^( MANY4 attribute comb_attr many coll ) )
            // parser/flatzinc/FlatzincFullExtParser.g:137:7: EACH attribute AS coll ( OF LB m= many RB )? ( KEY ca= comb_attr )?
            {
            EACH63=(Token)match(input,EACH,FOLLOW_EACH_in_many659); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_EACH.add(EACH63);


            pushFollow(FOLLOW_attribute_in_many661);
            attribute64=attribute();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_attribute.add(attribute64.getTree());

            AS65=(Token)match(input,AS,FOLLOW_AS_in_many663); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_AS.add(AS65);


            pushFollow(FOLLOW_coll_in_many665);
            coll66=coll();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_coll.add(coll66.getTree());

            // parser/flatzinc/FlatzincFullExtParser.g:137:30: ( OF LB m= many RB )?
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
                    // parser/flatzinc/FlatzincFullExtParser.g:137:31: OF LB m= many RB
                    {
                    OF67=(Token)match(input,OF,FOLLOW_OF_in_many668); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_OF.add(OF67);


                    LB68=(Token)match(input,LB,FOLLOW_LB_in_many670); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LB.add(LB68);


                    pushFollow(FOLLOW_many_in_many674);
                    m=many();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_many.add(m.getTree());

                    RB69=(Token)match(input,RB,FOLLOW_RB_in_many676); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RB.add(RB69);


                    }
                    break;

            }


            // parser/flatzinc/FlatzincFullExtParser.g:137:49: ( KEY ca= comb_attr )?
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
                    // parser/flatzinc/FlatzincFullExtParser.g:137:50: KEY ca= comb_attr
                    {
                    KEY70=(Token)match(input,KEY,FOLLOW_KEY_in_many681); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_KEY.add(KEY70);


                    pushFollow(FOLLOW_comb_attr_in_many685);
                    ca=comb_attr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_comb_attr.add(ca.getTree());

                    }
                    break;

            }


            // AST REWRITE
            // elements: comb_attr, many, many, attribute, comb_attr, coll, coll, attribute, coll, attribute, attribute, coll
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {

            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 138:5: -> {m==null && ca == null}? ^( MANY1 attribute coll )
            if (m==null && ca == null) {
                // parser/flatzinc/FlatzincFullExtParser.g:138:36: ^( MANY1 attribute coll )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot(
                (Object)adaptor.create(MANY1, "MANY1")
                , root_1);

                adaptor.addChild(root_1, stream_attribute.nextTree());

                adaptor.addChild(root_1, stream_coll.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            else // 139:5: -> {m==null && ca != null}? ^( MANY2 attribute comb_attr coll )
            if (m==null && ca != null) {
                // parser/flatzinc/FlatzincFullExtParser.g:139:36: ^( MANY2 attribute comb_attr coll )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot(
                (Object)adaptor.create(MANY2, "MANY2")
                , root_1);

                adaptor.addChild(root_1, stream_attribute.nextTree());

                adaptor.addChild(root_1, stream_comb_attr.nextTree());

                adaptor.addChild(root_1, stream_coll.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            else // 140:5: -> {m!=null && ca == null}? ^( MANY3 attribute many coll )
            if (m!=null && ca == null) {
                // parser/flatzinc/FlatzincFullExtParser.g:140:36: ^( MANY3 attribute many coll )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot(
                (Object)adaptor.create(MANY3, "MANY3")
                , root_1);

                adaptor.addChild(root_1, stream_attribute.nextTree());

                adaptor.addChild(root_1, stream_many.nextTree());

                adaptor.addChild(root_1, stream_coll.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            else // 141:5: -> ^( MANY4 attribute comb_attr many coll )
            {
                // parser/flatzinc/FlatzincFullExtParser.g:141:36: ^( MANY4 attribute comb_attr many coll )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot(
                (Object)adaptor.create(MANY4, "MANY4")
                , root_1);

                adaptor.addChild(root_1, stream_attribute.nextTree());

                adaptor.addChild(root_1, stream_comb_attr.nextTree());

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
    // parser/flatzinc/FlatzincFullExtParser.g:147:1: coll : ( QUEUE LP qiter RP -> ^( QUEUE qiter ) | ( REV )? LIST LP liter RP -> ^( LIST ( REV )? liter ) | ( MAX )? HEAP LP qiter RP -> ^( HEAP ( MAX )? qiter ) );
    public final FlatzincFullExtParser.coll_return coll() throws RecognitionException {
        FlatzincFullExtParser.coll_return retval = new FlatzincFullExtParser.coll_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token QUEUE71=null;
        Token LP72=null;
        Token RP74=null;
        Token REV75=null;
        Token LIST76=null;
        Token LP77=null;
        Token RP79=null;
        Token MAX80=null;
        Token HEAP81=null;
        Token LP82=null;
        Token RP84=null;
        FlatzincFullExtParser.qiter_return qiter73 =null;

        FlatzincFullExtParser.liter_return liter78 =null;

        FlatzincFullExtParser.qiter_return qiter83 =null;


        Object QUEUE71_tree=null;
        Object LP72_tree=null;
        Object RP74_tree=null;
        Object REV75_tree=null;
        Object LIST76_tree=null;
        Object LP77_tree=null;
        Object RP79_tree=null;
        Object MAX80_tree=null;
        Object HEAP81_tree=null;
        Object LP82_tree=null;
        Object RP84_tree=null;
        RewriteRuleTokenStream stream_REV=new RewriteRuleTokenStream(adaptor,"token REV");
        RewriteRuleTokenStream stream_MAX=new RewriteRuleTokenStream(adaptor,"token MAX");
        RewriteRuleTokenStream stream_HEAP=new RewriteRuleTokenStream(adaptor,"token HEAP");
        RewriteRuleTokenStream stream_LIST=new RewriteRuleTokenStream(adaptor,"token LIST");
        RewriteRuleTokenStream stream_RP=new RewriteRuleTokenStream(adaptor,"token RP");
        RewriteRuleTokenStream stream_QUEUE=new RewriteRuleTokenStream(adaptor,"token QUEUE");
        RewriteRuleTokenStream stream_LP=new RewriteRuleTokenStream(adaptor,"token LP");
        RewriteRuleSubtreeStream stream_liter=new RewriteRuleSubtreeStream(adaptor,"rule liter");
        RewriteRuleSubtreeStream stream_qiter=new RewriteRuleSubtreeStream(adaptor,"rule qiter");
        try {
            // parser/flatzinc/FlatzincFullExtParser.g:148:5: ( QUEUE LP qiter RP -> ^( QUEUE qiter ) | ( REV )? LIST LP liter RP -> ^( LIST ( REV )? liter ) | ( MAX )? HEAP LP qiter RP -> ^( HEAP ( MAX )? qiter ) )
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
                    // parser/flatzinc/FlatzincFullExtParser.g:148:7: QUEUE LP qiter RP
                    {
                    QUEUE71=(Token)match(input,QUEUE,FOLLOW_QUEUE_in_coll811); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_QUEUE.add(QUEUE71);


                    LP72=(Token)match(input,LP,FOLLOW_LP_in_coll813); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LP.add(LP72);


                    pushFollow(FOLLOW_qiter_in_coll815);
                    qiter73=qiter();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_qiter.add(qiter73.getTree());

                    RP74=(Token)match(input,RP,FOLLOW_RP_in_coll817); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RP.add(RP74);


                    // AST REWRITE
                    // elements: qiter, QUEUE
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {

                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 149:5: -> ^( QUEUE qiter )
                    {
                        // parser/flatzinc/FlatzincFullExtParser.g:149:9: ^( QUEUE qiter )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot(
                        stream_QUEUE.nextNode()
                        , root_1);

                        adaptor.addChild(root_1, stream_qiter.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }


                    retval.tree = root_0;
                    }

                    }
                    break;
                case 2 :
                    // parser/flatzinc/FlatzincFullExtParser.g:150:7: ( REV )? LIST LP liter RP
                    {
                    // parser/flatzinc/FlatzincFullExtParser.g:150:7: ( REV )?
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
                            // parser/flatzinc/FlatzincFullExtParser.g:150:8: REV
                            {
                            REV75=(Token)match(input,REV,FOLLOW_REV_in_coll839); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_REV.add(REV75);


                            }
                            break;

                    }


                    LIST76=(Token)match(input,LIST,FOLLOW_LIST_in_coll843); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LIST.add(LIST76);


                    LP77=(Token)match(input,LP,FOLLOW_LP_in_coll845); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LP.add(LP77);


                    pushFollow(FOLLOW_liter_in_coll847);
                    liter78=liter();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_liter.add(liter78.getTree());

                    RP79=(Token)match(input,RP,FOLLOW_RP_in_coll849); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RP.add(RP79);


                    // AST REWRITE
                    // elements: liter, REV, LIST
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {

                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 151:5: -> ^( LIST ( REV )? liter )
                    {
                        // parser/flatzinc/FlatzincFullExtParser.g:151:9: ^( LIST ( REV )? liter )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot(
                        stream_LIST.nextNode()
                        , root_1);

                        // parser/flatzinc/FlatzincFullExtParser.g:151:16: ( REV )?
                        if ( stream_REV.hasNext() ) {
                            adaptor.addChild(root_1, 
                            stream_REV.nextNode()
                            );

                        }
                        stream_REV.reset();

                        adaptor.addChild(root_1, stream_liter.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }


                    retval.tree = root_0;
                    }

                    }
                    break;
                case 3 :
                    // parser/flatzinc/FlatzincFullExtParser.g:152:7: ( MAX )? HEAP LP qiter RP
                    {
                    // parser/flatzinc/FlatzincFullExtParser.g:152:7: ( MAX )?
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
                            // parser/flatzinc/FlatzincFullExtParser.g:152:8: MAX
                            {
                            MAX80=(Token)match(input,MAX,FOLLOW_MAX_in_coll874); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_MAX.add(MAX80);


                            }
                            break;

                    }


                    HEAP81=(Token)match(input,HEAP,FOLLOW_HEAP_in_coll878); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_HEAP.add(HEAP81);


                    LP82=(Token)match(input,LP,FOLLOW_LP_in_coll881); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LP.add(LP82);


                    pushFollow(FOLLOW_qiter_in_coll883);
                    qiter83=qiter();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_qiter.add(qiter83.getTree());

                    RP84=(Token)match(input,RP,FOLLOW_RP_in_coll885); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RP.add(RP84);


                    // AST REWRITE
                    // elements: MAX, qiter, HEAP
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {

                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 153:5: -> ^( HEAP ( MAX )? qiter )
                    {
                        // parser/flatzinc/FlatzincFullExtParser.g:153:9: ^( HEAP ( MAX )? qiter )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot(
                        stream_HEAP.nextNode()
                        , root_1);

                        // parser/flatzinc/FlatzincFullExtParser.g:153:16: ( MAX )?
                        if ( stream_MAX.hasNext() ) {
                            adaptor.addChild(root_1, 
                            stream_MAX.nextNode()
                            );

                        }
                        stream_MAX.reset();

                        adaptor.addChild(root_1, stream_qiter.nextTree());

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
    // $ANTLR end "coll"


    public static class qiter_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "qiter"
    // parser/flatzinc/FlatzincFullExtParser.g:156:1: qiter : ( ONE | WONE );
    public final FlatzincFullExtParser.qiter_return qiter() throws RecognitionException {
        FlatzincFullExtParser.qiter_return retval = new FlatzincFullExtParser.qiter_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token set85=null;

        Object set85_tree=null;

        try {
            // parser/flatzinc/FlatzincFullExtParser.g:157:5: ( ONE | WONE )
            // parser/flatzinc/FlatzincFullExtParser.g:
            {
            root_0 = (Object)adaptor.nil();


            set85=(Token)input.LT(1);

            if ( input.LA(1)==ONE||input.LA(1)==WONE ) {
                input.consume();
                if ( state.backtracking==0 ) adaptor.addChild(root_0, 
                (Object)adaptor.create(set85)
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
    // parser/flatzinc/FlatzincFullExtParser.g:161:1: liter : ( qiter | FOR | WFOR );
    public final FlatzincFullExtParser.liter_return liter() throws RecognitionException {
        FlatzincFullExtParser.liter_return retval = new FlatzincFullExtParser.liter_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token FOR87=null;
        Token WFOR88=null;
        FlatzincFullExtParser.qiter_return qiter86 =null;


        Object FOR87_tree=null;
        Object WFOR88_tree=null;

        try {
            // parser/flatzinc/FlatzincFullExtParser.g:162:5: ( qiter | FOR | WFOR )
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
                    // parser/flatzinc/FlatzincFullExtParser.g:162:7: qiter
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_qiter_in_liter942);
                    qiter86=qiter();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, qiter86.getTree());

                    }
                    break;
                case 2 :
                    // parser/flatzinc/FlatzincFullExtParser.g:163:9: FOR
                    {
                    root_0 = (Object)adaptor.nil();


                    FOR87=(Token)match(input,FOR,FOLLOW_FOR_in_liter952); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    FOR87_tree = 
                    (Object)adaptor.create(FOR87)
                    ;
                    adaptor.addChild(root_0, FOR87_tree);
                    }

                    }
                    break;
                case 3 :
                    // parser/flatzinc/FlatzincFullExtParser.g:164:9: WFOR
                    {
                    root_0 = (Object)adaptor.nil();


                    WFOR88=(Token)match(input,WFOR,FOLLOW_WFOR_in_liter962); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    WFOR88_tree = 
                    (Object)adaptor.create(WFOR88)
                    ;
                    adaptor.addChild(root_0, WFOR88_tree);
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
    // parser/flatzinc/FlatzincFullExtParser.g:168:1: comb_attr : ( attr_op ( DO attr_op )* ( DO attribute )? -> ^( CA1 ( attr_op )* ( attribute )? ) | ( attr_op DO )+ attribute -> ^( CA2 ( attr_op )+ attribute ) );
    public final FlatzincFullExtParser.comb_attr_return comb_attr() throws RecognitionException {
        FlatzincFullExtParser.comb_attr_return retval = new FlatzincFullExtParser.comb_attr_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token DO90=null;
        Token DO92=null;
        Token DO95=null;
        FlatzincFullExtParser.attr_op_return attr_op89 =null;

        FlatzincFullExtParser.attr_op_return attr_op91 =null;

        FlatzincFullExtParser.attribute_return attribute93 =null;

        FlatzincFullExtParser.attr_op_return attr_op94 =null;

        FlatzincFullExtParser.attribute_return attribute96 =null;


        Object DO90_tree=null;
        Object DO92_tree=null;
        Object DO95_tree=null;
        RewriteRuleTokenStream stream_DO=new RewriteRuleTokenStream(adaptor,"token DO");
        RewriteRuleSubtreeStream stream_attribute=new RewriteRuleSubtreeStream(adaptor,"rule attribute");
        RewriteRuleSubtreeStream stream_attr_op=new RewriteRuleSubtreeStream(adaptor,"rule attr_op");
        try {
            // parser/flatzinc/FlatzincFullExtParser.g:169:2: ( attr_op ( DO attr_op )* ( DO attribute )? -> ^( CA1 ( attr_op )* ( attribute )? ) | ( attr_op DO )+ attribute -> ^( CA2 ( attr_op )+ attribute ) )
            int alt27=2;
            alt27 = dfa27.predict(input);
            switch (alt27) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtParser.g:169:4: attr_op ( DO attr_op )* ( DO attribute )?
                    {
                    pushFollow(FOLLOW_attr_op_in_comb_attr977);
                    attr_op89=attr_op();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_attr_op.add(attr_op89.getTree());

                    // parser/flatzinc/FlatzincFullExtParser.g:169:12: ( DO attr_op )*
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
                    	    // parser/flatzinc/FlatzincFullExtParser.g:169:13: DO attr_op
                    	    {
                    	    DO90=(Token)match(input,DO,FOLLOW_DO_in_comb_attr980); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_DO.add(DO90);


                    	    pushFollow(FOLLOW_attr_op_in_comb_attr982);
                    	    attr_op91=attr_op();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_attr_op.add(attr_op91.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop24;
                        }
                    } while (true);


                    // parser/flatzinc/FlatzincFullExtParser.g:169:27: ( DO attribute )?
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
                            // parser/flatzinc/FlatzincFullExtParser.g:169:28: DO attribute
                            {
                            DO92=(Token)match(input,DO,FOLLOW_DO_in_comb_attr988); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_DO.add(DO92);


                            pushFollow(FOLLOW_attribute_in_comb_attr990);
                            attribute93=attribute();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_attribute.add(attribute93.getTree());

                            }
                            break;

                    }


                    // AST REWRITE
                    // elements: attr_op, attribute
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {

                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 170:2: -> ^( CA1 ( attr_op )* ( attribute )? )
                    {
                        // parser/flatzinc/FlatzincFullExtParser.g:170:6: ^( CA1 ( attr_op )* ( attribute )? )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot(
                        (Object)adaptor.create(CA1, "CA1")
                        , root_1);

                        // parser/flatzinc/FlatzincFullExtParser.g:170:12: ( attr_op )*
                        while ( stream_attr_op.hasNext() ) {
                            adaptor.addChild(root_1, stream_attr_op.nextTree());

                        }
                        stream_attr_op.reset();

                        // parser/flatzinc/FlatzincFullExtParser.g:170:21: ( attribute )?
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
                    // parser/flatzinc/FlatzincFullExtParser.g:171:6: ( attr_op DO )+ attribute
                    {
                    // parser/flatzinc/FlatzincFullExtParser.g:171:6: ( attr_op DO )+
                    int cnt26=0;
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
                    	    // parser/flatzinc/FlatzincFullExtParser.g:171:7: attr_op DO
                    	    {
                    	    pushFollow(FOLLOW_attr_op_in_comb_attr1014);
                    	    attr_op94=attr_op();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_attr_op.add(attr_op94.getTree());

                    	    DO95=(Token)match(input,DO,FOLLOW_DO_in_comb_attr1016); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_DO.add(DO95);


                    	    }
                    	    break;

                    	default :
                    	    if ( cnt26 >= 1 ) break loop26;
                    	    if (state.backtracking>0) {state.failed=true; return retval;}
                                EarlyExitException eee =
                                    new EarlyExitException(26, input);
                                throw eee;
                        }
                        cnt26++;
                    } while (true);


                    pushFollow(FOLLOW_attribute_in_comb_attr1020);
                    attribute96=attribute();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_attribute.add(attribute96.getTree());

                    // AST REWRITE
                    // elements: attr_op, attribute
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {

                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 172:2: -> ^( CA2 ( attr_op )+ attribute )
                    {
                        // parser/flatzinc/FlatzincFullExtParser.g:172:6: ^( CA2 ( attr_op )+ attribute )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot(
                        (Object)adaptor.create(CA2, "CA2")
                        , root_1);

                        if ( !(stream_attr_op.hasNext()) ) {
                            throw new RewriteEarlyExitException();
                        }
                        while ( stream_attr_op.hasNext() ) {
                            adaptor.addChild(root_1, stream_attr_op.nextTree());

                        }
                        stream_attr_op.reset();

                        adaptor.addChild(root_1, stream_attribute.nextTree());

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
    // parser/flatzinc/FlatzincFullExtParser.g:175:1: attr_op : ( ANY | MIN | MAX | SUM | SIZE );
    public final FlatzincFullExtParser.attr_op_return attr_op() throws RecognitionException {
        FlatzincFullExtParser.attr_op_return retval = new FlatzincFullExtParser.attr_op_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token set97=null;

        Object set97_tree=null;

        try {
            // parser/flatzinc/FlatzincFullExtParser.g:176:5: ( ANY | MIN | MAX | SUM | SIZE )
            // parser/flatzinc/FlatzincFullExtParser.g:
            {
            root_0 = (Object)adaptor.nil();


            set97=(Token)input.LT(1);

            if ( input.LA(1)==ANY||input.LA(1)==MAX||input.LA(1)==MIN||input.LA(1)==SIZE||input.LA(1)==SUM ) {
                input.consume();
                if ( state.backtracking==0 ) adaptor.addChild(root_0, 
                (Object)adaptor.create(set97)
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
    // parser/flatzinc/FlatzincFullExtParser.g:190:1: pred_decl : PREDICATE IDENTIFIER LP pred_param ( CM pred_param )* RP SC -> ^( PREDICATE IDENTIFIER ( pred_param )+ ) ;
    public final FlatzincFullExtParser.pred_decl_return pred_decl() throws RecognitionException {
        FlatzincFullExtParser.pred_decl_return retval = new FlatzincFullExtParser.pred_decl_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token PREDICATE98=null;
        Token IDENTIFIER99=null;
        Token LP100=null;
        Token CM102=null;
        Token RP104=null;
        Token SC105=null;
        FlatzincFullExtParser.pred_param_return pred_param101 =null;

        FlatzincFullExtParser.pred_param_return pred_param103 =null;


        Object PREDICATE98_tree=null;
        Object IDENTIFIER99_tree=null;
        Object LP100_tree=null;
        Object CM102_tree=null;
        Object RP104_tree=null;
        Object SC105_tree=null;
        RewriteRuleTokenStream stream_SC=new RewriteRuleTokenStream(adaptor,"token SC");
        RewriteRuleTokenStream stream_RP=new RewriteRuleTokenStream(adaptor,"token RP");
        RewriteRuleTokenStream stream_PREDICATE=new RewriteRuleTokenStream(adaptor,"token PREDICATE");
        RewriteRuleTokenStream stream_IDENTIFIER=new RewriteRuleTokenStream(adaptor,"token IDENTIFIER");
        RewriteRuleTokenStream stream_CM=new RewriteRuleTokenStream(adaptor,"token CM");
        RewriteRuleTokenStream stream_LP=new RewriteRuleTokenStream(adaptor,"token LP");
        RewriteRuleSubtreeStream stream_pred_param=new RewriteRuleSubtreeStream(adaptor,"rule pred_param");
        try {
            // parser/flatzinc/FlatzincFullExtParser.g:191:2: ( PREDICATE IDENTIFIER LP pred_param ( CM pred_param )* RP SC -> ^( PREDICATE IDENTIFIER ( pred_param )+ ) )
            // parser/flatzinc/FlatzincFullExtParser.g:191:6: PREDICATE IDENTIFIER LP pred_param ( CM pred_param )* RP SC
            {
            PREDICATE98=(Token)match(input,PREDICATE,FOLLOW_PREDICATE_in_pred_decl1107); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_PREDICATE.add(PREDICATE98);


            IDENTIFIER99=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_pred_decl1109); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_IDENTIFIER.add(IDENTIFIER99);


            LP100=(Token)match(input,LP,FOLLOW_LP_in_pred_decl1111); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LP.add(LP100);


            pushFollow(FOLLOW_pred_param_in_pred_decl1113);
            pred_param101=pred_param();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_pred_param.add(pred_param101.getTree());

            // parser/flatzinc/FlatzincFullExtParser.g:191:41: ( CM pred_param )*
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
            	    // parser/flatzinc/FlatzincFullExtParser.g:191:42: CM pred_param
            	    {
            	    CM102=(Token)match(input,CM,FOLLOW_CM_in_pred_decl1116); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_CM.add(CM102);


            	    pushFollow(FOLLOW_pred_param_in_pred_decl1118);
            	    pred_param103=pred_param();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_pred_param.add(pred_param103.getTree());

            	    }
            	    break;

            	default :
            	    break loop28;
                }
            } while (true);


            RP104=(Token)match(input,RP,FOLLOW_RP_in_pred_decl1122); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RP.add(RP104);


            SC105=(Token)match(input,SC,FOLLOW_SC_in_pred_decl1124); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_SC.add(SC105);


            // AST REWRITE
            // elements: IDENTIFIER, PREDICATE, pred_param
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {

            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 192:2: -> ^( PREDICATE IDENTIFIER ( pred_param )+ )
            {
                // parser/flatzinc/FlatzincFullExtParser.g:192:5: ^( PREDICATE IDENTIFIER ( pred_param )+ )
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
    // parser/flatzinc/FlatzincFullExtParser.g:195:1: pred_param : pred_param_type CL IDENTIFIER -> ^( CL pred_param_type IDENTIFIER ) ;
    public final FlatzincFullExtParser.pred_param_return pred_param() throws RecognitionException {
        FlatzincFullExtParser.pred_param_return retval = new FlatzincFullExtParser.pred_param_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token CL107=null;
        Token IDENTIFIER108=null;
        FlatzincFullExtParser.pred_param_type_return pred_param_type106 =null;


        Object CL107_tree=null;
        Object IDENTIFIER108_tree=null;
        RewriteRuleTokenStream stream_IDENTIFIER=new RewriteRuleTokenStream(adaptor,"token IDENTIFIER");
        RewriteRuleTokenStream stream_CL=new RewriteRuleTokenStream(adaptor,"token CL");
        RewriteRuleSubtreeStream stream_pred_param_type=new RewriteRuleSubtreeStream(adaptor,"rule pred_param_type");
        try {
            // parser/flatzinc/FlatzincFullExtParser.g:196:5: ( pred_param_type CL IDENTIFIER -> ^( CL pred_param_type IDENTIFIER ) )
            // parser/flatzinc/FlatzincFullExtParser.g:196:9: pred_param_type CL IDENTIFIER
            {
            pushFollow(FOLLOW_pred_param_type_in_pred_param1152);
            pred_param_type106=pred_param_type();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_pred_param_type.add(pred_param_type106.getTree());

            CL107=(Token)match(input,CL,FOLLOW_CL_in_pred_param1154); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_CL.add(CL107);


            IDENTIFIER108=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_pred_param1156); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_IDENTIFIER.add(IDENTIFIER108);


            // AST REWRITE
            // elements: CL, pred_param_type, IDENTIFIER
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {

            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 197:5: -> ^( CL pred_param_type IDENTIFIER )
            {
                // parser/flatzinc/FlatzincFullExtParser.g:197:9: ^( CL pred_param_type IDENTIFIER )
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
    // parser/flatzinc/FlatzincFullExtParser.g:200:1: pred_param_type : ( par_pred_param_type | var_pred_param_type );
    public final FlatzincFullExtParser.pred_param_type_return pred_param_type() throws RecognitionException {
        FlatzincFullExtParser.pred_param_type_return retval = new FlatzincFullExtParser.pred_param_type_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        FlatzincFullExtParser.par_pred_param_type_return par_pred_param_type109 =null;

        FlatzincFullExtParser.var_pred_param_type_return var_pred_param_type110 =null;



        try {
            // parser/flatzinc/FlatzincFullExtParser.g:201:5: ( par_pred_param_type | var_pred_param_type )
            int alt29=2;
            alt29 = dfa29.predict(input);
            switch (alt29) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtParser.g:201:9: par_pred_param_type
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_par_pred_param_type_in_pred_param_type1190);
                    par_pred_param_type109=par_pred_param_type();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, par_pred_param_type109.getTree());

                    }
                    break;
                case 2 :
                    // parser/flatzinc/FlatzincFullExtParser.g:202:9: var_pred_param_type
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_var_pred_param_type_in_pred_param_type1200);
                    var_pred_param_type110=var_pred_param_type();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, var_pred_param_type110.getTree());

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
    // parser/flatzinc/FlatzincFullExtParser.g:205:1: par_type : ( ARRAY LS index_set ( CM index_set )* RS OF par_type_u -> ^( ARRPAR ( index_set )+ par_type_u ) | par_type_u -> ^( APAR par_type_u ) );
    public final FlatzincFullExtParser.par_type_return par_type() throws RecognitionException {
        FlatzincFullExtParser.par_type_return retval = new FlatzincFullExtParser.par_type_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token ARRAY111=null;
        Token LS112=null;
        Token CM114=null;
        Token RS116=null;
        Token OF117=null;
        FlatzincFullExtParser.index_set_return index_set113 =null;

        FlatzincFullExtParser.index_set_return index_set115 =null;

        FlatzincFullExtParser.par_type_u_return par_type_u118 =null;

        FlatzincFullExtParser.par_type_u_return par_type_u119 =null;


        Object ARRAY111_tree=null;
        Object LS112_tree=null;
        Object CM114_tree=null;
        Object RS116_tree=null;
        Object OF117_tree=null;
        RewriteRuleTokenStream stream_RS=new RewriteRuleTokenStream(adaptor,"token RS");
        RewriteRuleTokenStream stream_OF=new RewriteRuleTokenStream(adaptor,"token OF");
        RewriteRuleTokenStream stream_ARRAY=new RewriteRuleTokenStream(adaptor,"token ARRAY");
        RewriteRuleTokenStream stream_LS=new RewriteRuleTokenStream(adaptor,"token LS");
        RewriteRuleTokenStream stream_CM=new RewriteRuleTokenStream(adaptor,"token CM");
        RewriteRuleSubtreeStream stream_index_set=new RewriteRuleSubtreeStream(adaptor,"rule index_set");
        RewriteRuleSubtreeStream stream_par_type_u=new RewriteRuleSubtreeStream(adaptor,"rule par_type_u");
        try {
            // parser/flatzinc/FlatzincFullExtParser.g:206:5: ( ARRAY LS index_set ( CM index_set )* RS OF par_type_u -> ^( ARRPAR ( index_set )+ par_type_u ) | par_type_u -> ^( APAR par_type_u ) )
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
                    // parser/flatzinc/FlatzincFullExtParser.g:206:9: ARRAY LS index_set ( CM index_set )* RS OF par_type_u
                    {
                    ARRAY111=(Token)match(input,ARRAY,FOLLOW_ARRAY_in_par_type1219); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ARRAY.add(ARRAY111);


                    LS112=(Token)match(input,LS,FOLLOW_LS_in_par_type1221); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LS.add(LS112);


                    pushFollow(FOLLOW_index_set_in_par_type1223);
                    index_set113=index_set();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_index_set.add(index_set113.getTree());

                    // parser/flatzinc/FlatzincFullExtParser.g:206:28: ( CM index_set )*
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
                    	    // parser/flatzinc/FlatzincFullExtParser.g:206:29: CM index_set
                    	    {
                    	    CM114=(Token)match(input,CM,FOLLOW_CM_in_par_type1226); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_CM.add(CM114);


                    	    pushFollow(FOLLOW_index_set_in_par_type1228);
                    	    index_set115=index_set();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_index_set.add(index_set115.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop30;
                        }
                    } while (true);


                    RS116=(Token)match(input,RS,FOLLOW_RS_in_par_type1232); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RS.add(RS116);


                    OF117=(Token)match(input,OF,FOLLOW_OF_in_par_type1234); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_OF.add(OF117);


                    pushFollow(FOLLOW_par_type_u_in_par_type1236);
                    par_type_u118=par_type_u();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_par_type_u.add(par_type_u118.getTree());

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
                    // 207:5: -> ^( ARRPAR ( index_set )+ par_type_u )
                    {
                        // parser/flatzinc/FlatzincFullExtParser.g:207:9: ^( ARRPAR ( index_set )+ par_type_u )
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
                    // parser/flatzinc/FlatzincFullExtParser.g:208:9: par_type_u
                    {
                    pushFollow(FOLLOW_par_type_u_in_par_type1262);
                    par_type_u119=par_type_u();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_par_type_u.add(par_type_u119.getTree());

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
                    // 209:5: -> ^( APAR par_type_u )
                    {
                        // parser/flatzinc/FlatzincFullExtParser.g:209:9: ^( APAR par_type_u )
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
    // parser/flatzinc/FlatzincFullExtParser.g:212:1: par_type_u : ( BOOL | FLOAT | SET OF INT | INT );
    public final FlatzincFullExtParser.par_type_u_return par_type_u() throws RecognitionException {
        FlatzincFullExtParser.par_type_u_return retval = new FlatzincFullExtParser.par_type_u_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token BOOL120=null;
        Token FLOAT121=null;
        Token SET122=null;
        Token OF123=null;
        Token INT124=null;
        Token INT125=null;

        Object BOOL120_tree=null;
        Object FLOAT121_tree=null;
        Object SET122_tree=null;
        Object OF123_tree=null;
        Object INT124_tree=null;
        Object INT125_tree=null;

        try {
            // parser/flatzinc/FlatzincFullExtParser.g:213:5: ( BOOL | FLOAT | SET OF INT | INT )
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
                    // parser/flatzinc/FlatzincFullExtParser.g:213:9: BOOL
                    {
                    root_0 = (Object)adaptor.nil();


                    BOOL120=(Token)match(input,BOOL,FOLLOW_BOOL_in_par_type_u1294); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    BOOL120_tree = 
                    (Object)adaptor.create(BOOL120)
                    ;
                    adaptor.addChild(root_0, BOOL120_tree);
                    }

                    }
                    break;
                case 2 :
                    // parser/flatzinc/FlatzincFullExtParser.g:214:9: FLOAT
                    {
                    root_0 = (Object)adaptor.nil();


                    FLOAT121=(Token)match(input,FLOAT,FOLLOW_FLOAT_in_par_type_u1304); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    FLOAT121_tree = 
                    (Object)adaptor.create(FLOAT121)
                    ;
                    adaptor.addChild(root_0, FLOAT121_tree);
                    }

                    }
                    break;
                case 3 :
                    // parser/flatzinc/FlatzincFullExtParser.g:215:9: SET OF INT
                    {
                    root_0 = (Object)adaptor.nil();


                    SET122=(Token)match(input,SET,FOLLOW_SET_in_par_type_u1314); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    SET122_tree = 
                    (Object)adaptor.create(SET122)
                    ;
                    adaptor.addChild(root_0, SET122_tree);
                    }

                    OF123=(Token)match(input,OF,FOLLOW_OF_in_par_type_u1316); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    OF123_tree = 
                    (Object)adaptor.create(OF123)
                    ;
                    adaptor.addChild(root_0, OF123_tree);
                    }

                    INT124=(Token)match(input,INT,FOLLOW_INT_in_par_type_u1318); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    INT124_tree = 
                    (Object)adaptor.create(INT124)
                    ;
                    adaptor.addChild(root_0, INT124_tree);
                    }

                    }
                    break;
                case 4 :
                    // parser/flatzinc/FlatzincFullExtParser.g:216:9: INT
                    {
                    root_0 = (Object)adaptor.nil();


                    INT125=(Token)match(input,INT,FOLLOW_INT_in_par_type_u1328); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    INT125_tree = 
                    (Object)adaptor.create(INT125)
                    ;
                    adaptor.addChild(root_0, INT125_tree);
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
    // parser/flatzinc/FlatzincFullExtParser.g:219:1: var_type : ( ARRAY LS index_set ( CM index_set )* RS OF VAR var_type_u -> ^( ARRVAR ( index_set )+ var_type_u ) | VAR var_type_u -> ^( AVAR var_type_u ) );
    public final FlatzincFullExtParser.var_type_return var_type() throws RecognitionException {
        FlatzincFullExtParser.var_type_return retval = new FlatzincFullExtParser.var_type_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token ARRAY126=null;
        Token LS127=null;
        Token CM129=null;
        Token RS131=null;
        Token OF132=null;
        Token VAR133=null;
        Token VAR135=null;
        FlatzincFullExtParser.index_set_return index_set128 =null;

        FlatzincFullExtParser.index_set_return index_set130 =null;

        FlatzincFullExtParser.var_type_u_return var_type_u134 =null;

        FlatzincFullExtParser.var_type_u_return var_type_u136 =null;


        Object ARRAY126_tree=null;
        Object LS127_tree=null;
        Object CM129_tree=null;
        Object RS131_tree=null;
        Object OF132_tree=null;
        Object VAR133_tree=null;
        Object VAR135_tree=null;
        RewriteRuleTokenStream stream_RS=new RewriteRuleTokenStream(adaptor,"token RS");
        RewriteRuleTokenStream stream_VAR=new RewriteRuleTokenStream(adaptor,"token VAR");
        RewriteRuleTokenStream stream_OF=new RewriteRuleTokenStream(adaptor,"token OF");
        RewriteRuleTokenStream stream_ARRAY=new RewriteRuleTokenStream(adaptor,"token ARRAY");
        RewriteRuleTokenStream stream_LS=new RewriteRuleTokenStream(adaptor,"token LS");
        RewriteRuleTokenStream stream_CM=new RewriteRuleTokenStream(adaptor,"token CM");
        RewriteRuleSubtreeStream stream_index_set=new RewriteRuleSubtreeStream(adaptor,"rule index_set");
        RewriteRuleSubtreeStream stream_var_type_u=new RewriteRuleSubtreeStream(adaptor,"rule var_type_u");
        try {
            // parser/flatzinc/FlatzincFullExtParser.g:220:5: ( ARRAY LS index_set ( CM index_set )* RS OF VAR var_type_u -> ^( ARRVAR ( index_set )+ var_type_u ) | VAR var_type_u -> ^( AVAR var_type_u ) )
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
                    // parser/flatzinc/FlatzincFullExtParser.g:220:9: ARRAY LS index_set ( CM index_set )* RS OF VAR var_type_u
                    {
                    ARRAY126=(Token)match(input,ARRAY,FOLLOW_ARRAY_in_var_type1347); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ARRAY.add(ARRAY126);


                    LS127=(Token)match(input,LS,FOLLOW_LS_in_var_type1349); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LS.add(LS127);


                    pushFollow(FOLLOW_index_set_in_var_type1351);
                    index_set128=index_set();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_index_set.add(index_set128.getTree());

                    // parser/flatzinc/FlatzincFullExtParser.g:220:28: ( CM index_set )*
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
                    	    // parser/flatzinc/FlatzincFullExtParser.g:220:29: CM index_set
                    	    {
                    	    CM129=(Token)match(input,CM,FOLLOW_CM_in_var_type1354); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_CM.add(CM129);


                    	    pushFollow(FOLLOW_index_set_in_var_type1356);
                    	    index_set130=index_set();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_index_set.add(index_set130.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop33;
                        }
                    } while (true);


                    RS131=(Token)match(input,RS,FOLLOW_RS_in_var_type1360); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RS.add(RS131);


                    OF132=(Token)match(input,OF,FOLLOW_OF_in_var_type1362); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_OF.add(OF132);


                    VAR133=(Token)match(input,VAR,FOLLOW_VAR_in_var_type1364); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_VAR.add(VAR133);


                    pushFollow(FOLLOW_var_type_u_in_var_type1366);
                    var_type_u134=var_type_u();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_var_type_u.add(var_type_u134.getTree());

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
                    // 221:5: -> ^( ARRVAR ( index_set )+ var_type_u )
                    {
                        // parser/flatzinc/FlatzincFullExtParser.g:221:9: ^( ARRVAR ( index_set )+ var_type_u )
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
                    // parser/flatzinc/FlatzincFullExtParser.g:222:9: VAR var_type_u
                    {
                    VAR135=(Token)match(input,VAR,FOLLOW_VAR_in_var_type1392); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_VAR.add(VAR135);


                    pushFollow(FOLLOW_var_type_u_in_var_type1394);
                    var_type_u136=var_type_u();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_var_type_u.add(var_type_u136.getTree());

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
                    // 223:5: -> ^( AVAR var_type_u )
                    {
                        // parser/flatzinc/FlatzincFullExtParser.g:223:9: ^( AVAR var_type_u )
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
    // parser/flatzinc/FlatzincFullExtParser.g:226:1: var_type_u : ( BOOL | FLOAT | INT | INT_CONST DD INT_CONST -> ^( DD INT_CONST INT_CONST ) | LB INT_CONST ( CM INT_CONST )* RB -> ^( CM ( INT_CONST )+ ) | SET OF INT_CONST DD INT_CONST -> ^( SET ^( DD INT_CONST INT_CONST ) ) | SET OF LB INT_CONST ( CM INT_CONST )* RB -> ^( SET ^( CM INT_CONST INT_CONST ) ) );
    public final FlatzincFullExtParser.var_type_u_return var_type_u() throws RecognitionException {
        FlatzincFullExtParser.var_type_u_return retval = new FlatzincFullExtParser.var_type_u_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token BOOL137=null;
        Token FLOAT138=null;
        Token INT139=null;
        Token INT_CONST140=null;
        Token DD141=null;
        Token INT_CONST142=null;
        Token LB143=null;
        Token INT_CONST144=null;
        Token CM145=null;
        Token INT_CONST146=null;
        Token RB147=null;
        Token SET148=null;
        Token OF149=null;
        Token INT_CONST150=null;
        Token DD151=null;
        Token INT_CONST152=null;
        Token SET153=null;
        Token OF154=null;
        Token LB155=null;
        Token INT_CONST156=null;
        Token CM157=null;
        Token INT_CONST158=null;
        Token RB159=null;

        Object BOOL137_tree=null;
        Object FLOAT138_tree=null;
        Object INT139_tree=null;
        Object INT_CONST140_tree=null;
        Object DD141_tree=null;
        Object INT_CONST142_tree=null;
        Object LB143_tree=null;
        Object INT_CONST144_tree=null;
        Object CM145_tree=null;
        Object INT_CONST146_tree=null;
        Object RB147_tree=null;
        Object SET148_tree=null;
        Object OF149_tree=null;
        Object INT_CONST150_tree=null;
        Object DD151_tree=null;
        Object INT_CONST152_tree=null;
        Object SET153_tree=null;
        Object OF154_tree=null;
        Object LB155_tree=null;
        Object INT_CONST156_tree=null;
        Object CM157_tree=null;
        Object INT_CONST158_tree=null;
        Object RB159_tree=null;
        RewriteRuleTokenStream stream_INT_CONST=new RewriteRuleTokenStream(adaptor,"token INT_CONST");
        RewriteRuleTokenStream stream_SET=new RewriteRuleTokenStream(adaptor,"token SET");
        RewriteRuleTokenStream stream_LB=new RewriteRuleTokenStream(adaptor,"token LB");
        RewriteRuleTokenStream stream_RB=new RewriteRuleTokenStream(adaptor,"token RB");
        RewriteRuleTokenStream stream_OF=new RewriteRuleTokenStream(adaptor,"token OF");
        RewriteRuleTokenStream stream_DD=new RewriteRuleTokenStream(adaptor,"token DD");
        RewriteRuleTokenStream stream_CM=new RewriteRuleTokenStream(adaptor,"token CM");

        try {
            // parser/flatzinc/FlatzincFullExtParser.g:227:5: ( BOOL | FLOAT | INT | INT_CONST DD INT_CONST -> ^( DD INT_CONST INT_CONST ) | LB INT_CONST ( CM INT_CONST )* RB -> ^( CM ( INT_CONST )+ ) | SET OF INT_CONST DD INT_CONST -> ^( SET ^( DD INT_CONST INT_CONST ) ) | SET OF LB INT_CONST ( CM INT_CONST )* RB -> ^( SET ^( CM INT_CONST INT_CONST ) ) )
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
                    // parser/flatzinc/FlatzincFullExtParser.g:227:9: BOOL
                    {
                    root_0 = (Object)adaptor.nil();


                    BOOL137=(Token)match(input,BOOL,FOLLOW_BOOL_in_var_type_u1426); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    BOOL137_tree = 
                    (Object)adaptor.create(BOOL137)
                    ;
                    adaptor.addChild(root_0, BOOL137_tree);
                    }

                    }
                    break;
                case 2 :
                    // parser/flatzinc/FlatzincFullExtParser.g:228:9: FLOAT
                    {
                    root_0 = (Object)adaptor.nil();


                    FLOAT138=(Token)match(input,FLOAT,FOLLOW_FLOAT_in_var_type_u1436); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    FLOAT138_tree = 
                    (Object)adaptor.create(FLOAT138)
                    ;
                    adaptor.addChild(root_0, FLOAT138_tree);
                    }

                    }
                    break;
                case 3 :
                    // parser/flatzinc/FlatzincFullExtParser.g:229:9: INT
                    {
                    root_0 = (Object)adaptor.nil();


                    INT139=(Token)match(input,INT,FOLLOW_INT_in_var_type_u1446); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    INT139_tree = 
                    (Object)adaptor.create(INT139)
                    ;
                    adaptor.addChild(root_0, INT139_tree);
                    }

                    }
                    break;
                case 4 :
                    // parser/flatzinc/FlatzincFullExtParser.g:230:9: INT_CONST DD INT_CONST
                    {
                    INT_CONST140=(Token)match(input,INT_CONST,FOLLOW_INT_CONST_in_var_type_u1456); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_INT_CONST.add(INT_CONST140);


                    DD141=(Token)match(input,DD,FOLLOW_DD_in_var_type_u1458); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_DD.add(DD141);


                    INT_CONST142=(Token)match(input,INT_CONST,FOLLOW_INT_CONST_in_var_type_u1460); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_INT_CONST.add(INT_CONST142);


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
                    // 231:5: -> ^( DD INT_CONST INT_CONST )
                    {
                        // parser/flatzinc/FlatzincFullExtParser.g:231:9: ^( DD INT_CONST INT_CONST )
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
                    // parser/flatzinc/FlatzincFullExtParser.g:234:9: LB INT_CONST ( CM INT_CONST )* RB
                    {
                    LB143=(Token)match(input,LB,FOLLOW_LB_in_var_type_u1487); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LB.add(LB143);


                    INT_CONST144=(Token)match(input,INT_CONST,FOLLOW_INT_CONST_in_var_type_u1489); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_INT_CONST.add(INT_CONST144);


                    // parser/flatzinc/FlatzincFullExtParser.g:234:22: ( CM INT_CONST )*
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
                    	    // parser/flatzinc/FlatzincFullExtParser.g:234:23: CM INT_CONST
                    	    {
                    	    CM145=(Token)match(input,CM,FOLLOW_CM_in_var_type_u1492); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_CM.add(CM145);


                    	    INT_CONST146=(Token)match(input,INT_CONST,FOLLOW_INT_CONST_in_var_type_u1494); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_INT_CONST.add(INT_CONST146);


                    	    }
                    	    break;

                    	default :
                    	    break loop35;
                        }
                    } while (true);


                    RB147=(Token)match(input,RB,FOLLOW_RB_in_var_type_u1498); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RB.add(RB147);


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
                    // 235:5: -> ^( CM ( INT_CONST )+ )
                    {
                        // parser/flatzinc/FlatzincFullExtParser.g:235:9: ^( CM ( INT_CONST )+ )
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
                    // parser/flatzinc/FlatzincFullExtParser.g:236:9: SET OF INT_CONST DD INT_CONST
                    {
                    SET148=(Token)match(input,SET,FOLLOW_SET_in_var_type_u1522); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_SET.add(SET148);


                    OF149=(Token)match(input,OF,FOLLOW_OF_in_var_type_u1524); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_OF.add(OF149);


                    INT_CONST150=(Token)match(input,INT_CONST,FOLLOW_INT_CONST_in_var_type_u1526); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_INT_CONST.add(INT_CONST150);


                    DD151=(Token)match(input,DD,FOLLOW_DD_in_var_type_u1528); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_DD.add(DD151);


                    INT_CONST152=(Token)match(input,INT_CONST,FOLLOW_INT_CONST_in_var_type_u1530); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_INT_CONST.add(INT_CONST152);


                    // AST REWRITE
                    // elements: DD, SET, INT_CONST, INT_CONST
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {

                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 237:5: -> ^( SET ^( DD INT_CONST INT_CONST ) )
                    {
                        // parser/flatzinc/FlatzincFullExtParser.g:237:9: ^( SET ^( DD INT_CONST INT_CONST ) )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot(
                        stream_SET.nextNode()
                        , root_1);

                        // parser/flatzinc/FlatzincFullExtParser.g:237:15: ^( DD INT_CONST INT_CONST )
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
                    // parser/flatzinc/FlatzincFullExtParser.g:238:9: SET OF LB INT_CONST ( CM INT_CONST )* RB
                    {
                    SET153=(Token)match(input,SET,FOLLOW_SET_in_var_type_u1559); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_SET.add(SET153);


                    OF154=(Token)match(input,OF,FOLLOW_OF_in_var_type_u1561); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_OF.add(OF154);


                    LB155=(Token)match(input,LB,FOLLOW_LB_in_var_type_u1563); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LB.add(LB155);


                    INT_CONST156=(Token)match(input,INT_CONST,FOLLOW_INT_CONST_in_var_type_u1565); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_INT_CONST.add(INT_CONST156);


                    // parser/flatzinc/FlatzincFullExtParser.g:238:29: ( CM INT_CONST )*
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
                    	    // parser/flatzinc/FlatzincFullExtParser.g:238:30: CM INT_CONST
                    	    {
                    	    CM157=(Token)match(input,CM,FOLLOW_CM_in_var_type_u1568); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_CM.add(CM157);


                    	    INT_CONST158=(Token)match(input,INT_CONST,FOLLOW_INT_CONST_in_var_type_u1570); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_INT_CONST.add(INT_CONST158);


                    	    }
                    	    break;

                    	default :
                    	    break loop36;
                        }
                    } while (true);


                    RB159=(Token)match(input,RB,FOLLOW_RB_in_var_type_u1574); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RB.add(RB159);


                    // AST REWRITE
                    // elements: SET, INT_CONST, CM, INT_CONST
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {

                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 239:5: -> ^( SET ^( CM INT_CONST INT_CONST ) )
                    {
                        // parser/flatzinc/FlatzincFullExtParser.g:239:9: ^( SET ^( CM INT_CONST INT_CONST ) )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot(
                        stream_SET.nextNode()
                        , root_1);

                        // parser/flatzinc/FlatzincFullExtParser.g:239:15: ^( CM INT_CONST INT_CONST )
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
    // parser/flatzinc/FlatzincFullExtParser.g:242:1: par_pred_param_type : ( par_type | INT_CONST DD INT_CONST -> ^( DD INT_CONST INT_CONST ) | LB INT_CONST ( CM INT_CONST )* RB -> ^( CM ( INT_CONST )+ ) | SET OF INT_CONST DD INT_CONST -> ^( SET ^( DD INT_CONST INT_CONST ) ) | SET OF LB INT_CONST ( CM INT_CONST )* RB -> ^( SET ^( CM ( INT_CONST )+ ) ) | ARRAY LS index_set ( CM index_set )* RS OF INT_CONST DD INT_CONST -> ^( ARRAY ( index_set )+ ^( DD INT_CONST INT_CONST ) ) | ARRAY LS index_set ( CM index_set )* RS OF LB INT_CONST ( CM INT_CONST )* RB -> ^( ARRAY ( index_set )+ ^( CM ( INT_CONST )+ ) ) | ARRAY LS index_set ( CM index_set )* RS OF SET OF INT_CONST DD INT_CONST -> ^( ARRAY ( index_set )+ ^( SET ^( DD INT_CONST INT_CONST ) ) ) | ARRAY LS index_set ( CM index_set )* RS OF SET OF LB INT_CONST ( CM INT_CONST )* RB -> ^( ARRAY ( index_set )+ ^( SET ^( CM ( INT_CONST )+ ) ) ) );
    public final FlatzincFullExtParser.par_pred_param_type_return par_pred_param_type() throws RecognitionException {
        FlatzincFullExtParser.par_pred_param_type_return retval = new FlatzincFullExtParser.par_pred_param_type_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token INT_CONST161=null;
        Token DD162=null;
        Token INT_CONST163=null;
        Token LB164=null;
        Token INT_CONST165=null;
        Token CM166=null;
        Token INT_CONST167=null;
        Token RB168=null;
        Token SET169=null;
        Token OF170=null;
        Token INT_CONST171=null;
        Token DD172=null;
        Token INT_CONST173=null;
        Token SET174=null;
        Token OF175=null;
        Token LB176=null;
        Token INT_CONST177=null;
        Token CM178=null;
        Token INT_CONST179=null;
        Token RB180=null;
        Token ARRAY181=null;
        Token LS182=null;
        Token CM184=null;
        Token RS186=null;
        Token OF187=null;
        Token INT_CONST188=null;
        Token DD189=null;
        Token INT_CONST190=null;
        Token ARRAY191=null;
        Token LS192=null;
        Token CM194=null;
        Token RS196=null;
        Token OF197=null;
        Token LB198=null;
        Token INT_CONST199=null;
        Token CM200=null;
        Token INT_CONST201=null;
        Token RB202=null;
        Token ARRAY203=null;
        Token LS204=null;
        Token CM206=null;
        Token RS208=null;
        Token OF209=null;
        Token SET210=null;
        Token OF211=null;
        Token INT_CONST212=null;
        Token DD213=null;
        Token INT_CONST214=null;
        Token ARRAY215=null;
        Token LS216=null;
        Token CM218=null;
        Token RS220=null;
        Token OF221=null;
        Token SET222=null;
        Token OF223=null;
        Token LB224=null;
        Token INT_CONST225=null;
        Token CM226=null;
        Token INT_CONST227=null;
        Token RB228=null;
        FlatzincFullExtParser.par_type_return par_type160 =null;

        FlatzincFullExtParser.index_set_return index_set183 =null;

        FlatzincFullExtParser.index_set_return index_set185 =null;

        FlatzincFullExtParser.index_set_return index_set193 =null;

        FlatzincFullExtParser.index_set_return index_set195 =null;

        FlatzincFullExtParser.index_set_return index_set205 =null;

        FlatzincFullExtParser.index_set_return index_set207 =null;

        FlatzincFullExtParser.index_set_return index_set217 =null;

        FlatzincFullExtParser.index_set_return index_set219 =null;


        Object INT_CONST161_tree=null;
        Object DD162_tree=null;
        Object INT_CONST163_tree=null;
        Object LB164_tree=null;
        Object INT_CONST165_tree=null;
        Object CM166_tree=null;
        Object INT_CONST167_tree=null;
        Object RB168_tree=null;
        Object SET169_tree=null;
        Object OF170_tree=null;
        Object INT_CONST171_tree=null;
        Object DD172_tree=null;
        Object INT_CONST173_tree=null;
        Object SET174_tree=null;
        Object OF175_tree=null;
        Object LB176_tree=null;
        Object INT_CONST177_tree=null;
        Object CM178_tree=null;
        Object INT_CONST179_tree=null;
        Object RB180_tree=null;
        Object ARRAY181_tree=null;
        Object LS182_tree=null;
        Object CM184_tree=null;
        Object RS186_tree=null;
        Object OF187_tree=null;
        Object INT_CONST188_tree=null;
        Object DD189_tree=null;
        Object INT_CONST190_tree=null;
        Object ARRAY191_tree=null;
        Object LS192_tree=null;
        Object CM194_tree=null;
        Object RS196_tree=null;
        Object OF197_tree=null;
        Object LB198_tree=null;
        Object INT_CONST199_tree=null;
        Object CM200_tree=null;
        Object INT_CONST201_tree=null;
        Object RB202_tree=null;
        Object ARRAY203_tree=null;
        Object LS204_tree=null;
        Object CM206_tree=null;
        Object RS208_tree=null;
        Object OF209_tree=null;
        Object SET210_tree=null;
        Object OF211_tree=null;
        Object INT_CONST212_tree=null;
        Object DD213_tree=null;
        Object INT_CONST214_tree=null;
        Object ARRAY215_tree=null;
        Object LS216_tree=null;
        Object CM218_tree=null;
        Object RS220_tree=null;
        Object OF221_tree=null;
        Object SET222_tree=null;
        Object OF223_tree=null;
        Object LB224_tree=null;
        Object INT_CONST225_tree=null;
        Object CM226_tree=null;
        Object INT_CONST227_tree=null;
        Object RB228_tree=null;
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
            // parser/flatzinc/FlatzincFullExtParser.g:243:5: ( par_type | INT_CONST DD INT_CONST -> ^( DD INT_CONST INT_CONST ) | LB INT_CONST ( CM INT_CONST )* RB -> ^( CM ( INT_CONST )+ ) | SET OF INT_CONST DD INT_CONST -> ^( SET ^( DD INT_CONST INT_CONST ) ) | SET OF LB INT_CONST ( CM INT_CONST )* RB -> ^( SET ^( CM ( INT_CONST )+ ) ) | ARRAY LS index_set ( CM index_set )* RS OF INT_CONST DD INT_CONST -> ^( ARRAY ( index_set )+ ^( DD INT_CONST INT_CONST ) ) | ARRAY LS index_set ( CM index_set )* RS OF LB INT_CONST ( CM INT_CONST )* RB -> ^( ARRAY ( index_set )+ ^( CM ( INT_CONST )+ ) ) | ARRAY LS index_set ( CM index_set )* RS OF SET OF INT_CONST DD INT_CONST -> ^( ARRAY ( index_set )+ ^( SET ^( DD INT_CONST INT_CONST ) ) ) | ARRAY LS index_set ( CM index_set )* RS OF SET OF LB INT_CONST ( CM INT_CONST )* RB -> ^( ARRAY ( index_set )+ ^( SET ^( CM ( INT_CONST )+ ) ) ) )
            int alt46=9;
            alt46 = dfa46.predict(input);
            switch (alt46) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtParser.g:243:9: par_type
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_par_type_in_par_pred_param_type1612);
                    par_type160=par_type();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, par_type160.getTree());

                    }
                    break;
                case 2 :
                    // parser/flatzinc/FlatzincFullExtParser.g:246:9: INT_CONST DD INT_CONST
                    {
                    INT_CONST161=(Token)match(input,INT_CONST,FOLLOW_INT_CONST_in_par_pred_param_type1624); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_INT_CONST.add(INT_CONST161);


                    DD162=(Token)match(input,DD,FOLLOW_DD_in_par_pred_param_type1626); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_DD.add(DD162);


                    INT_CONST163=(Token)match(input,INT_CONST,FOLLOW_INT_CONST_in_par_pred_param_type1628); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_INT_CONST.add(INT_CONST163);


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
                    // 247:5: -> ^( DD INT_CONST INT_CONST )
                    {
                        // parser/flatzinc/FlatzincFullExtParser.g:247:9: ^( DD INT_CONST INT_CONST )
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
                    // parser/flatzinc/FlatzincFullExtParser.g:248:9: LB INT_CONST ( CM INT_CONST )* RB
                    {
                    LB164=(Token)match(input,LB,FOLLOW_LB_in_par_pred_param_type1653); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LB.add(LB164);


                    INT_CONST165=(Token)match(input,INT_CONST,FOLLOW_INT_CONST_in_par_pred_param_type1655); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_INT_CONST.add(INT_CONST165);


                    // parser/flatzinc/FlatzincFullExtParser.g:248:22: ( CM INT_CONST )*
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
                    	    // parser/flatzinc/FlatzincFullExtParser.g:248:23: CM INT_CONST
                    	    {
                    	    CM166=(Token)match(input,CM,FOLLOW_CM_in_par_pred_param_type1658); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_CM.add(CM166);


                    	    INT_CONST167=(Token)match(input,INT_CONST,FOLLOW_INT_CONST_in_par_pred_param_type1660); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_INT_CONST.add(INT_CONST167);


                    	    }
                    	    break;

                    	default :
                    	    break loop38;
                        }
                    } while (true);


                    RB168=(Token)match(input,RB,FOLLOW_RB_in_par_pred_param_type1664); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RB.add(RB168);


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
                    // 249:5: -> ^( CM ( INT_CONST )+ )
                    {
                        // parser/flatzinc/FlatzincFullExtParser.g:249:9: ^( CM ( INT_CONST )+ )
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
                    // parser/flatzinc/FlatzincFullExtParser.g:250:9: SET OF INT_CONST DD INT_CONST
                    {
                    SET169=(Token)match(input,SET,FOLLOW_SET_in_par_pred_param_type1688); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_SET.add(SET169);


                    OF170=(Token)match(input,OF,FOLLOW_OF_in_par_pred_param_type1690); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_OF.add(OF170);


                    INT_CONST171=(Token)match(input,INT_CONST,FOLLOW_INT_CONST_in_par_pred_param_type1692); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_INT_CONST.add(INT_CONST171);


                    DD172=(Token)match(input,DD,FOLLOW_DD_in_par_pred_param_type1694); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_DD.add(DD172);


                    INT_CONST173=(Token)match(input,INT_CONST,FOLLOW_INT_CONST_in_par_pred_param_type1696); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_INT_CONST.add(INT_CONST173);


                    // AST REWRITE
                    // elements: DD, INT_CONST, INT_CONST, SET
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {

                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 251:5: -> ^( SET ^( DD INT_CONST INT_CONST ) )
                    {
                        // parser/flatzinc/FlatzincFullExtParser.g:251:9: ^( SET ^( DD INT_CONST INT_CONST ) )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot(
                        stream_SET.nextNode()
                        , root_1);

                        // parser/flatzinc/FlatzincFullExtParser.g:251:15: ^( DD INT_CONST INT_CONST )
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
                    // parser/flatzinc/FlatzincFullExtParser.g:252:9: SET OF LB INT_CONST ( CM INT_CONST )* RB
                    {
                    SET174=(Token)match(input,SET,FOLLOW_SET_in_par_pred_param_type1725); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_SET.add(SET174);


                    OF175=(Token)match(input,OF,FOLLOW_OF_in_par_pred_param_type1727); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_OF.add(OF175);


                    LB176=(Token)match(input,LB,FOLLOW_LB_in_par_pred_param_type1729); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LB.add(LB176);


                    INT_CONST177=(Token)match(input,INT_CONST,FOLLOW_INT_CONST_in_par_pred_param_type1731); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_INT_CONST.add(INT_CONST177);


                    // parser/flatzinc/FlatzincFullExtParser.g:252:29: ( CM INT_CONST )*
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
                    	    // parser/flatzinc/FlatzincFullExtParser.g:252:30: CM INT_CONST
                    	    {
                    	    CM178=(Token)match(input,CM,FOLLOW_CM_in_par_pred_param_type1734); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_CM.add(CM178);


                    	    INT_CONST179=(Token)match(input,INT_CONST,FOLLOW_INT_CONST_in_par_pred_param_type1736); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_INT_CONST.add(INT_CONST179);


                    	    }
                    	    break;

                    	default :
                    	    break loop39;
                        }
                    } while (true);


                    RB180=(Token)match(input,RB,FOLLOW_RB_in_par_pred_param_type1740); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RB.add(RB180);


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
                    // 253:5: -> ^( SET ^( CM ( INT_CONST )+ ) )
                    {
                        // parser/flatzinc/FlatzincFullExtParser.g:253:9: ^( SET ^( CM ( INT_CONST )+ ) )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot(
                        stream_SET.nextNode()
                        , root_1);

                        // parser/flatzinc/FlatzincFullExtParser.g:253:15: ^( CM ( INT_CONST )+ )
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
                    // parser/flatzinc/FlatzincFullExtParser.g:256:9: ARRAY LS index_set ( CM index_set )* RS OF INT_CONST DD INT_CONST
                    {
                    ARRAY181=(Token)match(input,ARRAY,FOLLOW_ARRAY_in_par_pred_param_type1770); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ARRAY.add(ARRAY181);


                    LS182=(Token)match(input,LS,FOLLOW_LS_in_par_pred_param_type1772); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LS.add(LS182);


                    pushFollow(FOLLOW_index_set_in_par_pred_param_type1774);
                    index_set183=index_set();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_index_set.add(index_set183.getTree());

                    // parser/flatzinc/FlatzincFullExtParser.g:256:28: ( CM index_set )*
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
                    	    // parser/flatzinc/FlatzincFullExtParser.g:256:29: CM index_set
                    	    {
                    	    CM184=(Token)match(input,CM,FOLLOW_CM_in_par_pred_param_type1777); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_CM.add(CM184);


                    	    pushFollow(FOLLOW_index_set_in_par_pred_param_type1779);
                    	    index_set185=index_set();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_index_set.add(index_set185.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop40;
                        }
                    } while (true);


                    RS186=(Token)match(input,RS,FOLLOW_RS_in_par_pred_param_type1783); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RS.add(RS186);


                    OF187=(Token)match(input,OF,FOLLOW_OF_in_par_pred_param_type1785); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_OF.add(OF187);


                    INT_CONST188=(Token)match(input,INT_CONST,FOLLOW_INT_CONST_in_par_pred_param_type1787); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_INT_CONST.add(INT_CONST188);


                    DD189=(Token)match(input,DD,FOLLOW_DD_in_par_pred_param_type1789); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_DD.add(DD189);


                    INT_CONST190=(Token)match(input,INT_CONST,FOLLOW_INT_CONST_in_par_pred_param_type1791); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_INT_CONST.add(INT_CONST190);


                    // AST REWRITE
                    // elements: INT_CONST, index_set, ARRAY, DD, INT_CONST
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {

                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 257:5: -> ^( ARRAY ( index_set )+ ^( DD INT_CONST INT_CONST ) )
                    {
                        // parser/flatzinc/FlatzincFullExtParser.g:257:9: ^( ARRAY ( index_set )+ ^( DD INT_CONST INT_CONST ) )
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

                        // parser/flatzinc/FlatzincFullExtParser.g:257:28: ^( DD INT_CONST INT_CONST )
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
                    // parser/flatzinc/FlatzincFullExtParser.g:258:9: ARRAY LS index_set ( CM index_set )* RS OF LB INT_CONST ( CM INT_CONST )* RB
                    {
                    ARRAY191=(Token)match(input,ARRAY,FOLLOW_ARRAY_in_par_pred_param_type1823); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ARRAY.add(ARRAY191);


                    LS192=(Token)match(input,LS,FOLLOW_LS_in_par_pred_param_type1825); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LS.add(LS192);


                    pushFollow(FOLLOW_index_set_in_par_pred_param_type1827);
                    index_set193=index_set();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_index_set.add(index_set193.getTree());

                    // parser/flatzinc/FlatzincFullExtParser.g:258:28: ( CM index_set )*
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
                    	    // parser/flatzinc/FlatzincFullExtParser.g:258:29: CM index_set
                    	    {
                    	    CM194=(Token)match(input,CM,FOLLOW_CM_in_par_pred_param_type1830); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_CM.add(CM194);


                    	    pushFollow(FOLLOW_index_set_in_par_pred_param_type1832);
                    	    index_set195=index_set();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_index_set.add(index_set195.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop41;
                        }
                    } while (true);


                    RS196=(Token)match(input,RS,FOLLOW_RS_in_par_pred_param_type1836); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RS.add(RS196);


                    OF197=(Token)match(input,OF,FOLLOW_OF_in_par_pred_param_type1838); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_OF.add(OF197);


                    LB198=(Token)match(input,LB,FOLLOW_LB_in_par_pred_param_type1840); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LB.add(LB198);


                    INT_CONST199=(Token)match(input,INT_CONST,FOLLOW_INT_CONST_in_par_pred_param_type1842); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_INT_CONST.add(INT_CONST199);


                    // parser/flatzinc/FlatzincFullExtParser.g:258:63: ( CM INT_CONST )*
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
                    	    // parser/flatzinc/FlatzincFullExtParser.g:258:64: CM INT_CONST
                    	    {
                    	    CM200=(Token)match(input,CM,FOLLOW_CM_in_par_pred_param_type1845); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_CM.add(CM200);


                    	    INT_CONST201=(Token)match(input,INT_CONST,FOLLOW_INT_CONST_in_par_pred_param_type1847); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_INT_CONST.add(INT_CONST201);


                    	    }
                    	    break;

                    	default :
                    	    break loop42;
                        }
                    } while (true);


                    RB202=(Token)match(input,RB,FOLLOW_RB_in_par_pred_param_type1851); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RB.add(RB202);


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
                    // 259:5: -> ^( ARRAY ( index_set )+ ^( CM ( INT_CONST )+ ) )
                    {
                        // parser/flatzinc/FlatzincFullExtParser.g:259:9: ^( ARRAY ( index_set )+ ^( CM ( INT_CONST )+ ) )
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

                        // parser/flatzinc/FlatzincFullExtParser.g:259:28: ^( CM ( INT_CONST )+ )
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
                    // parser/flatzinc/FlatzincFullExtParser.g:260:9: ARRAY LS index_set ( CM index_set )* RS OF SET OF INT_CONST DD INT_CONST
                    {
                    ARRAY203=(Token)match(input,ARRAY,FOLLOW_ARRAY_in_par_pred_param_type1882); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ARRAY.add(ARRAY203);


                    LS204=(Token)match(input,LS,FOLLOW_LS_in_par_pred_param_type1884); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LS.add(LS204);


                    pushFollow(FOLLOW_index_set_in_par_pred_param_type1886);
                    index_set205=index_set();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_index_set.add(index_set205.getTree());

                    // parser/flatzinc/FlatzincFullExtParser.g:260:28: ( CM index_set )*
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
                    	    // parser/flatzinc/FlatzincFullExtParser.g:260:29: CM index_set
                    	    {
                    	    CM206=(Token)match(input,CM,FOLLOW_CM_in_par_pred_param_type1889); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_CM.add(CM206);


                    	    pushFollow(FOLLOW_index_set_in_par_pred_param_type1891);
                    	    index_set207=index_set();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_index_set.add(index_set207.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop43;
                        }
                    } while (true);


                    RS208=(Token)match(input,RS,FOLLOW_RS_in_par_pred_param_type1895); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RS.add(RS208);


                    OF209=(Token)match(input,OF,FOLLOW_OF_in_par_pred_param_type1897); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_OF.add(OF209);


                    SET210=(Token)match(input,SET,FOLLOW_SET_in_par_pred_param_type1899); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_SET.add(SET210);


                    OF211=(Token)match(input,OF,FOLLOW_OF_in_par_pred_param_type1901); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_OF.add(OF211);


                    INT_CONST212=(Token)match(input,INT_CONST,FOLLOW_INT_CONST_in_par_pred_param_type1903); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_INT_CONST.add(INT_CONST212);


                    DD213=(Token)match(input,DD,FOLLOW_DD_in_par_pred_param_type1905); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_DD.add(DD213);


                    INT_CONST214=(Token)match(input,INT_CONST,FOLLOW_INT_CONST_in_par_pred_param_type1907); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_INT_CONST.add(INT_CONST214);


                    // AST REWRITE
                    // elements: ARRAY, INT_CONST, SET, DD, index_set, INT_CONST
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {

                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 261:5: -> ^( ARRAY ( index_set )+ ^( SET ^( DD INT_CONST INT_CONST ) ) )
                    {
                        // parser/flatzinc/FlatzincFullExtParser.g:261:9: ^( ARRAY ( index_set )+ ^( SET ^( DD INT_CONST INT_CONST ) ) )
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

                        // parser/flatzinc/FlatzincFullExtParser.g:261:28: ^( SET ^( DD INT_CONST INT_CONST ) )
                        {
                        Object root_2 = (Object)adaptor.nil();
                        root_2 = (Object)adaptor.becomeRoot(
                        stream_SET.nextNode()
                        , root_2);

                        // parser/flatzinc/FlatzincFullExtParser.g:261:34: ^( DD INT_CONST INT_CONST )
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
                    // parser/flatzinc/FlatzincFullExtParser.g:262:9: ARRAY LS index_set ( CM index_set )* RS OF SET OF LB INT_CONST ( CM INT_CONST )* RB
                    {
                    ARRAY215=(Token)match(input,ARRAY,FOLLOW_ARRAY_in_par_pred_param_type1943); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ARRAY.add(ARRAY215);


                    LS216=(Token)match(input,LS,FOLLOW_LS_in_par_pred_param_type1945); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LS.add(LS216);


                    pushFollow(FOLLOW_index_set_in_par_pred_param_type1947);
                    index_set217=index_set();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_index_set.add(index_set217.getTree());

                    // parser/flatzinc/FlatzincFullExtParser.g:262:28: ( CM index_set )*
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
                    	    // parser/flatzinc/FlatzincFullExtParser.g:262:29: CM index_set
                    	    {
                    	    CM218=(Token)match(input,CM,FOLLOW_CM_in_par_pred_param_type1950); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_CM.add(CM218);


                    	    pushFollow(FOLLOW_index_set_in_par_pred_param_type1952);
                    	    index_set219=index_set();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_index_set.add(index_set219.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop44;
                        }
                    } while (true);


                    RS220=(Token)match(input,RS,FOLLOW_RS_in_par_pred_param_type1956); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RS.add(RS220);


                    OF221=(Token)match(input,OF,FOLLOW_OF_in_par_pred_param_type1958); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_OF.add(OF221);


                    SET222=(Token)match(input,SET,FOLLOW_SET_in_par_pred_param_type1960); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_SET.add(SET222);


                    OF223=(Token)match(input,OF,FOLLOW_OF_in_par_pred_param_type1962); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_OF.add(OF223);


                    LB224=(Token)match(input,LB,FOLLOW_LB_in_par_pred_param_type1964); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LB.add(LB224);


                    INT_CONST225=(Token)match(input,INT_CONST,FOLLOW_INT_CONST_in_par_pred_param_type1966); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_INT_CONST.add(INT_CONST225);


                    // parser/flatzinc/FlatzincFullExtParser.g:262:70: ( CM INT_CONST )*
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
                    	    // parser/flatzinc/FlatzincFullExtParser.g:262:71: CM INT_CONST
                    	    {
                    	    CM226=(Token)match(input,CM,FOLLOW_CM_in_par_pred_param_type1969); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_CM.add(CM226);


                    	    INT_CONST227=(Token)match(input,INT_CONST,FOLLOW_INT_CONST_in_par_pred_param_type1971); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_INT_CONST.add(INT_CONST227);


                    	    }
                    	    break;

                    	default :
                    	    break loop45;
                        }
                    } while (true);


                    RB228=(Token)match(input,RB,FOLLOW_RB_in_par_pred_param_type1975); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RB.add(RB228);


                    // AST REWRITE
                    // elements: CM, index_set, SET, ARRAY, INT_CONST
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {

                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 263:5: -> ^( ARRAY ( index_set )+ ^( SET ^( CM ( INT_CONST )+ ) ) )
                    {
                        // parser/flatzinc/FlatzincFullExtParser.g:263:9: ^( ARRAY ( index_set )+ ^( SET ^( CM ( INT_CONST )+ ) ) )
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

                        // parser/flatzinc/FlatzincFullExtParser.g:263:28: ^( SET ^( CM ( INT_CONST )+ ) )
                        {
                        Object root_2 = (Object)adaptor.nil();
                        root_2 = (Object)adaptor.becomeRoot(
                        stream_SET.nextNode()
                        , root_2);

                        // parser/flatzinc/FlatzincFullExtParser.g:263:34: ^( CM ( INT_CONST )+ )
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
    // parser/flatzinc/FlatzincFullExtParser.g:267:1: var_pred_param_type : ( var_type -> ^( VAR var_type ) | VAR SET OF INT -> ^( VAR SET ) | ARRAY LS index_set ( CM index_set )* RS OF VAR SET OF INT -> ^( ARRAY ( index_set )+ ^( VAR SET ) ) );
    public final FlatzincFullExtParser.var_pred_param_type_return var_pred_param_type() throws RecognitionException {
        FlatzincFullExtParser.var_pred_param_type_return retval = new FlatzincFullExtParser.var_pred_param_type_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token VAR230=null;
        Token SET231=null;
        Token OF232=null;
        Token INT233=null;
        Token ARRAY234=null;
        Token LS235=null;
        Token CM237=null;
        Token RS239=null;
        Token OF240=null;
        Token VAR241=null;
        Token SET242=null;
        Token OF243=null;
        Token INT244=null;
        FlatzincFullExtParser.var_type_return var_type229 =null;

        FlatzincFullExtParser.index_set_return index_set236 =null;

        FlatzincFullExtParser.index_set_return index_set238 =null;


        Object VAR230_tree=null;
        Object SET231_tree=null;
        Object OF232_tree=null;
        Object INT233_tree=null;
        Object ARRAY234_tree=null;
        Object LS235_tree=null;
        Object CM237_tree=null;
        Object RS239_tree=null;
        Object OF240_tree=null;
        Object VAR241_tree=null;
        Object SET242_tree=null;
        Object OF243_tree=null;
        Object INT244_tree=null;
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
            // parser/flatzinc/FlatzincFullExtParser.g:268:5: ( var_type -> ^( VAR var_type ) | VAR SET OF INT -> ^( VAR SET ) | ARRAY LS index_set ( CM index_set )* RS OF VAR SET OF INT -> ^( ARRAY ( index_set )+ ^( VAR SET ) ) )
            int alt48=3;
            alt48 = dfa48.predict(input);
            switch (alt48) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtParser.g:268:9: var_type
                    {
                    pushFollow(FOLLOW_var_type_in_var_pred_param_type2020);
                    var_type229=var_type();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_var_type.add(var_type229.getTree());

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
                    // 269:5: -> ^( VAR var_type )
                    {
                        // parser/flatzinc/FlatzincFullExtParser.g:269:9: ^( VAR var_type )
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
                    // parser/flatzinc/FlatzincFullExtParser.g:270:9: VAR SET OF INT
                    {
                    VAR230=(Token)match(input,VAR,FOLLOW_VAR_in_var_pred_param_type2043); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_VAR.add(VAR230);


                    SET231=(Token)match(input,SET,FOLLOW_SET_in_var_pred_param_type2045); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_SET.add(SET231);


                    OF232=(Token)match(input,OF,FOLLOW_OF_in_var_pred_param_type2047); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_OF.add(OF232);


                    INT233=(Token)match(input,INT,FOLLOW_INT_in_var_pred_param_type2049); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_INT.add(INT233);


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
                    // 271:5: -> ^( VAR SET )
                    {
                        // parser/flatzinc/FlatzincFullExtParser.g:271:9: ^( VAR SET )
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
                    // parser/flatzinc/FlatzincFullExtParser.g:272:9: ARRAY LS index_set ( CM index_set )* RS OF VAR SET OF INT
                    {
                    ARRAY234=(Token)match(input,ARRAY,FOLLOW_ARRAY_in_var_pred_param_type2072); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ARRAY.add(ARRAY234);


                    LS235=(Token)match(input,LS,FOLLOW_LS_in_var_pred_param_type2074); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LS.add(LS235);


                    pushFollow(FOLLOW_index_set_in_var_pred_param_type2076);
                    index_set236=index_set();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_index_set.add(index_set236.getTree());

                    // parser/flatzinc/FlatzincFullExtParser.g:272:28: ( CM index_set )*
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
                    	    // parser/flatzinc/FlatzincFullExtParser.g:272:29: CM index_set
                    	    {
                    	    CM237=(Token)match(input,CM,FOLLOW_CM_in_var_pred_param_type2079); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_CM.add(CM237);


                    	    pushFollow(FOLLOW_index_set_in_var_pred_param_type2081);
                    	    index_set238=index_set();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_index_set.add(index_set238.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop47;
                        }
                    } while (true);


                    RS239=(Token)match(input,RS,FOLLOW_RS_in_var_pred_param_type2085); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RS.add(RS239);


                    OF240=(Token)match(input,OF,FOLLOW_OF_in_var_pred_param_type2087); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_OF.add(OF240);


                    VAR241=(Token)match(input,VAR,FOLLOW_VAR_in_var_pred_param_type2089); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_VAR.add(VAR241);


                    SET242=(Token)match(input,SET,FOLLOW_SET_in_var_pred_param_type2091); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_SET.add(SET242);


                    OF243=(Token)match(input,OF,FOLLOW_OF_in_var_pred_param_type2093); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_OF.add(OF243);


                    INT244=(Token)match(input,INT,FOLLOW_INT_in_var_pred_param_type2095); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_INT.add(INT244);


                    // AST REWRITE
                    // elements: index_set, SET, VAR, ARRAY
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {

                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 273:5: -> ^( ARRAY ( index_set )+ ^( VAR SET ) )
                    {
                        // parser/flatzinc/FlatzincFullExtParser.g:273:9: ^( ARRAY ( index_set )+ ^( VAR SET ) )
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

                        // parser/flatzinc/FlatzincFullExtParser.g:273:28: ^( VAR SET )
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
    // parser/flatzinc/FlatzincFullExtParser.g:276:1: index_set : ( INT_CONST DD INT_CONST -> ^( INDEX ^( DD INT_CONST INT_CONST ) ) | INT -> ^( INDEX INT ) );
    public final FlatzincFullExtParser.index_set_return index_set() throws RecognitionException {
        FlatzincFullExtParser.index_set_return retval = new FlatzincFullExtParser.index_set_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token INT_CONST245=null;
        Token DD246=null;
        Token INT_CONST247=null;
        Token INT248=null;

        Object INT_CONST245_tree=null;
        Object DD246_tree=null;
        Object INT_CONST247_tree=null;
        Object INT248_tree=null;
        RewriteRuleTokenStream stream_INT_CONST=new RewriteRuleTokenStream(adaptor,"token INT_CONST");
        RewriteRuleTokenStream stream_INT=new RewriteRuleTokenStream(adaptor,"token INT");
        RewriteRuleTokenStream stream_DD=new RewriteRuleTokenStream(adaptor,"token DD");

        try {
            // parser/flatzinc/FlatzincFullExtParser.g:277:5: ( INT_CONST DD INT_CONST -> ^( INDEX ^( DD INT_CONST INT_CONST ) ) | INT -> ^( INDEX INT ) )
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
                    // parser/flatzinc/FlatzincFullExtParser.g:277:9: INT_CONST DD INT_CONST
                    {
                    INT_CONST245=(Token)match(input,INT_CONST,FOLLOW_INT_CONST_in_index_set2134); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_INT_CONST.add(INT_CONST245);


                    DD246=(Token)match(input,DD,FOLLOW_DD_in_index_set2136); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_DD.add(DD246);


                    INT_CONST247=(Token)match(input,INT_CONST,FOLLOW_INT_CONST_in_index_set2138); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_INT_CONST.add(INT_CONST247);


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
                    // 278:5: -> ^( INDEX ^( DD INT_CONST INT_CONST ) )
                    {
                        // parser/flatzinc/FlatzincFullExtParser.g:278:9: ^( INDEX ^( DD INT_CONST INT_CONST ) )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot(
                        (Object)adaptor.create(INDEX, "INDEX")
                        , root_1);

                        // parser/flatzinc/FlatzincFullExtParser.g:278:17: ^( DD INT_CONST INT_CONST )
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
                    // parser/flatzinc/FlatzincFullExtParser.g:279:9: INT
                    {
                    INT248=(Token)match(input,INT,FOLLOW_INT_in_index_set2167); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_INT.add(INT248);


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
                    // 280:5: -> ^( INDEX INT )
                    {
                        // parser/flatzinc/FlatzincFullExtParser.g:280:9: ^( INDEX INT )
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
    // parser/flatzinc/FlatzincFullExtParser.g:283:1: expr : ( LB INT_CONST ( CM INT_CONST )* RB -> LB ( INT_CONST )+ RB | bool_const | INT_CONST ( DD INT_CONST )? | LS ( expr ( CM expr )* )? RS -> ^( EXPR LS ( expr )* RS ) | id_expr | STRING );
    public final FlatzincFullExtParser.expr_return expr() throws RecognitionException {
        FlatzincFullExtParser.expr_return retval = new FlatzincFullExtParser.expr_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token LB249=null;
        Token INT_CONST250=null;
        Token CM251=null;
        Token INT_CONST252=null;
        Token RB253=null;
        Token INT_CONST255=null;
        Token DD256=null;
        Token INT_CONST257=null;
        Token LS258=null;
        Token CM260=null;
        Token RS262=null;
        Token STRING264=null;
        FlatzincFullExtParser.bool_const_return bool_const254 =null;

        FlatzincFullExtParser.expr_return expr259 =null;

        FlatzincFullExtParser.expr_return expr261 =null;

        FlatzincFullExtParser.id_expr_return id_expr263 =null;


        Object LB249_tree=null;
        Object INT_CONST250_tree=null;
        Object CM251_tree=null;
        Object INT_CONST252_tree=null;
        Object RB253_tree=null;
        Object INT_CONST255_tree=null;
        Object DD256_tree=null;
        Object INT_CONST257_tree=null;
        Object LS258_tree=null;
        Object CM260_tree=null;
        Object RS262_tree=null;
        Object STRING264_tree=null;
        RewriteRuleTokenStream stream_RS=new RewriteRuleTokenStream(adaptor,"token RS");
        RewriteRuleTokenStream stream_INT_CONST=new RewriteRuleTokenStream(adaptor,"token INT_CONST");
        RewriteRuleTokenStream stream_LB=new RewriteRuleTokenStream(adaptor,"token LB");
        RewriteRuleTokenStream stream_RB=new RewriteRuleTokenStream(adaptor,"token RB");
        RewriteRuleTokenStream stream_LS=new RewriteRuleTokenStream(adaptor,"token LS");
        RewriteRuleTokenStream stream_CM=new RewriteRuleTokenStream(adaptor,"token CM");
        RewriteRuleSubtreeStream stream_expr=new RewriteRuleSubtreeStream(adaptor,"rule expr");
        try {
            // parser/flatzinc/FlatzincFullExtParser.g:284:5: ( LB INT_CONST ( CM INT_CONST )* RB -> LB ( INT_CONST )+ RB | bool_const | INT_CONST ( DD INT_CONST )? | LS ( expr ( CM expr )* )? RS -> ^( EXPR LS ( expr )* RS ) | id_expr | STRING )
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
                    // parser/flatzinc/FlatzincFullExtParser.g:284:9: LB INT_CONST ( CM INT_CONST )* RB
                    {
                    LB249=(Token)match(input,LB,FOLLOW_LB_in_expr2199); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LB.add(LB249);


                    INT_CONST250=(Token)match(input,INT_CONST,FOLLOW_INT_CONST_in_expr2201); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_INT_CONST.add(INT_CONST250);


                    // parser/flatzinc/FlatzincFullExtParser.g:284:22: ( CM INT_CONST )*
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
                    	    // parser/flatzinc/FlatzincFullExtParser.g:284:23: CM INT_CONST
                    	    {
                    	    CM251=(Token)match(input,CM,FOLLOW_CM_in_expr2204); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_CM.add(CM251);


                    	    INT_CONST252=(Token)match(input,INT_CONST,FOLLOW_INT_CONST_in_expr2206); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_INT_CONST.add(INT_CONST252);


                    	    }
                    	    break;

                    	default :
                    	    break loop50;
                        }
                    } while (true);


                    RB253=(Token)match(input,RB,FOLLOW_RB_in_expr2210); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RB.add(RB253);


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
                    // 285:5: -> LB ( INT_CONST )+ RB
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
                    // parser/flatzinc/FlatzincFullExtParser.g:286:9: bool_const
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_bool_const_in_expr2234);
                    bool_const254=bool_const();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, bool_const254.getTree());

                    }
                    break;
                case 3 :
                    // parser/flatzinc/FlatzincFullExtParser.g:287:9: INT_CONST ( DD INT_CONST )?
                    {
                    root_0 = (Object)adaptor.nil();


                    INT_CONST255=(Token)match(input,INT_CONST,FOLLOW_INT_CONST_in_expr2244); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    INT_CONST255_tree = 
                    (Object)adaptor.create(INT_CONST255)
                    ;
                    adaptor.addChild(root_0, INT_CONST255_tree);
                    }

                    // parser/flatzinc/FlatzincFullExtParser.g:287:19: ( DD INT_CONST )?
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
                            // parser/flatzinc/FlatzincFullExtParser.g:287:20: DD INT_CONST
                            {
                            DD256=(Token)match(input,DD,FOLLOW_DD_in_expr2247); if (state.failed) return retval;
                            if ( state.backtracking==0 ) {
                            DD256_tree = 
                            (Object)adaptor.create(DD256)
                            ;
                            adaptor.addChild(root_0, DD256_tree);
                            }

                            INT_CONST257=(Token)match(input,INT_CONST,FOLLOW_INT_CONST_in_expr2249); if (state.failed) return retval;
                            if ( state.backtracking==0 ) {
                            INT_CONST257_tree = 
                            (Object)adaptor.create(INT_CONST257)
                            ;
                            adaptor.addChild(root_0, INT_CONST257_tree);
                            }

                            }
                            break;

                    }


                    }
                    break;
                case 4 :
                    // parser/flatzinc/FlatzincFullExtParser.g:288:9: LS ( expr ( CM expr )* )? RS
                    {
                    LS258=(Token)match(input,LS,FOLLOW_LS_in_expr2261); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LS.add(LS258);


                    // parser/flatzinc/FlatzincFullExtParser.g:288:12: ( expr ( CM expr )* )?
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
                            // parser/flatzinc/FlatzincFullExtParser.g:288:13: expr ( CM expr )*
                            {
                            pushFollow(FOLLOW_expr_in_expr2264);
                            expr259=expr();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_expr.add(expr259.getTree());

                            // parser/flatzinc/FlatzincFullExtParser.g:288:18: ( CM expr )*
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
                            	    // parser/flatzinc/FlatzincFullExtParser.g:288:19: CM expr
                            	    {
                            	    CM260=(Token)match(input,CM,FOLLOW_CM_in_expr2267); if (state.failed) return retval; 
                            	    if ( state.backtracking==0 ) stream_CM.add(CM260);


                            	    pushFollow(FOLLOW_expr_in_expr2269);
                            	    expr261=expr();

                            	    state._fsp--;
                            	    if (state.failed) return retval;
                            	    if ( state.backtracking==0 ) stream_expr.add(expr261.getTree());

                            	    }
                            	    break;

                            	default :
                            	    break loop52;
                                }
                            } while (true);


                            }
                            break;

                    }


                    RS262=(Token)match(input,RS,FOLLOW_RS_in_expr2275); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RS.add(RS262);


                    // AST REWRITE
                    // elements: RS, expr, LS
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {

                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 289:5: -> ^( EXPR LS ( expr )* RS )
                    {
                        // parser/flatzinc/FlatzincFullExtParser.g:289:9: ^( EXPR LS ( expr )* RS )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot(
                        (Object)adaptor.create(EXPR, "EXPR")
                        , root_1);

                        adaptor.addChild(root_1, 
                        stream_LS.nextNode()
                        );

                        // parser/flatzinc/FlatzincFullExtParser.g:289:19: ( expr )*
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
                    // parser/flatzinc/FlatzincFullExtParser.g:290:9: id_expr
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_id_expr_in_expr2303);
                    id_expr263=id_expr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, id_expr263.getTree());

                    }
                    break;
                case 6 :
                    // parser/flatzinc/FlatzincFullExtParser.g:291:9: STRING
                    {
                    root_0 = (Object)adaptor.nil();


                    STRING264=(Token)match(input,STRING,FOLLOW_STRING_in_expr2313); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    STRING264_tree = 
                    (Object)adaptor.create(STRING264)
                    ;
                    adaptor.addChild(root_0, STRING264_tree);
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
    // parser/flatzinc/FlatzincFullExtParser.g:295:1: id_expr : IDENTIFIER ( ( LP expr ( CM expr )* RP ) | ( LS INT_CONST RS ) )? ;
    public final FlatzincFullExtParser.id_expr_return id_expr() throws RecognitionException {
        FlatzincFullExtParser.id_expr_return retval = new FlatzincFullExtParser.id_expr_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token IDENTIFIER265=null;
        Token LP266=null;
        Token CM268=null;
        Token RP270=null;
        Token LS271=null;
        Token INT_CONST272=null;
        Token RS273=null;
        FlatzincFullExtParser.expr_return expr267 =null;

        FlatzincFullExtParser.expr_return expr269 =null;


        Object IDENTIFIER265_tree=null;
        Object LP266_tree=null;
        Object CM268_tree=null;
        Object RP270_tree=null;
        Object LS271_tree=null;
        Object INT_CONST272_tree=null;
        Object RS273_tree=null;

        try {
            // parser/flatzinc/FlatzincFullExtParser.g:297:5: ( IDENTIFIER ( ( LP expr ( CM expr )* RP ) | ( LS INT_CONST RS ) )? )
            // parser/flatzinc/FlatzincFullExtParser.g:297:9: IDENTIFIER ( ( LP expr ( CM expr )* RP ) | ( LS INT_CONST RS ) )?
            {
            root_0 = (Object)adaptor.nil();


            IDENTIFIER265=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_id_expr2334); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            IDENTIFIER265_tree = 
            (Object)adaptor.create(IDENTIFIER265)
            ;
            adaptor.addChild(root_0, IDENTIFIER265_tree);
            }

            // parser/flatzinc/FlatzincFullExtParser.g:297:20: ( ( LP expr ( CM expr )* RP ) | ( LS INT_CONST RS ) )?
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
                    // parser/flatzinc/FlatzincFullExtParser.g:297:21: ( LP expr ( CM expr )* RP )
                    {
                    // parser/flatzinc/FlatzincFullExtParser.g:297:21: ( LP expr ( CM expr )* RP )
                    // parser/flatzinc/FlatzincFullExtParser.g:297:22: LP expr ( CM expr )* RP
                    {
                    LP266=(Token)match(input,LP,FOLLOW_LP_in_id_expr2338); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LP266_tree = 
                    (Object)adaptor.create(LP266)
                    ;
                    adaptor.addChild(root_0, LP266_tree);
                    }

                    pushFollow(FOLLOW_expr_in_id_expr2340);
                    expr267=expr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, expr267.getTree());

                    // parser/flatzinc/FlatzincFullExtParser.g:297:30: ( CM expr )*
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
                    	    // parser/flatzinc/FlatzincFullExtParser.g:297:31: CM expr
                    	    {
                    	    CM268=(Token)match(input,CM,FOLLOW_CM_in_id_expr2343); if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) {
                    	    CM268_tree = 
                    	    (Object)adaptor.create(CM268)
                    	    ;
                    	    adaptor.addChild(root_0, CM268_tree);
                    	    }

                    	    pushFollow(FOLLOW_expr_in_id_expr2345);
                    	    expr269=expr();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) adaptor.addChild(root_0, expr269.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop55;
                        }
                    } while (true);


                    RP270=(Token)match(input,RP,FOLLOW_RP_in_id_expr2349); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    RP270_tree = 
                    (Object)adaptor.create(RP270)
                    ;
                    adaptor.addChild(root_0, RP270_tree);
                    }

                    }


                    }
                    break;
                case 2 :
                    // parser/flatzinc/FlatzincFullExtParser.g:297:45: ( LS INT_CONST RS )
                    {
                    // parser/flatzinc/FlatzincFullExtParser.g:297:45: ( LS INT_CONST RS )
                    // parser/flatzinc/FlatzincFullExtParser.g:297:46: LS INT_CONST RS
                    {
                    LS271=(Token)match(input,LS,FOLLOW_LS_in_id_expr2353); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LS271_tree = 
                    (Object)adaptor.create(LS271)
                    ;
                    adaptor.addChild(root_0, LS271_tree);
                    }

                    INT_CONST272=(Token)match(input,INT_CONST,FOLLOW_INT_CONST_in_id_expr2355); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    INT_CONST272_tree = 
                    (Object)adaptor.create(INT_CONST272)
                    ;
                    adaptor.addChild(root_0, INT_CONST272_tree);
                    }

                    RS273=(Token)match(input,RS,FOLLOW_RS_in_id_expr2357); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    RS273_tree = 
                    (Object)adaptor.create(RS273)
                    ;
                    adaptor.addChild(root_0, RS273_tree);
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
    // parser/flatzinc/FlatzincFullExtParser.g:301:1: param_decl : par_type CL IDENTIFIER EQ expr SC -> ^( PAR IDENTIFIER par_type expr ) ;
    public final FlatzincFullExtParser.param_decl_return param_decl() throws RecognitionException {
        FlatzincFullExtParser.param_decl_return retval = new FlatzincFullExtParser.param_decl_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token CL275=null;
        Token IDENTIFIER276=null;
        Token EQ277=null;
        Token SC279=null;
        FlatzincFullExtParser.par_type_return par_type274 =null;

        FlatzincFullExtParser.expr_return expr278 =null;


        Object CL275_tree=null;
        Object IDENTIFIER276_tree=null;
        Object EQ277_tree=null;
        Object SC279_tree=null;
        RewriteRuleTokenStream stream_SC=new RewriteRuleTokenStream(adaptor,"token SC");
        RewriteRuleTokenStream stream_EQ=new RewriteRuleTokenStream(adaptor,"token EQ");
        RewriteRuleTokenStream stream_IDENTIFIER=new RewriteRuleTokenStream(adaptor,"token IDENTIFIER");
        RewriteRuleTokenStream stream_CL=new RewriteRuleTokenStream(adaptor,"token CL");
        RewriteRuleSubtreeStream stream_par_type=new RewriteRuleSubtreeStream(adaptor,"rule par_type");
        RewriteRuleSubtreeStream stream_expr=new RewriteRuleSubtreeStream(adaptor,"rule expr");
        try {
            // parser/flatzinc/FlatzincFullExtParser.g:302:2: ( par_type CL IDENTIFIER EQ expr SC -> ^( PAR IDENTIFIER par_type expr ) )
            // parser/flatzinc/FlatzincFullExtParser.g:302:6: par_type CL IDENTIFIER EQ expr SC
            {
            pushFollow(FOLLOW_par_type_in_param_decl2377);
            par_type274=par_type();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_par_type.add(par_type274.getTree());

            CL275=(Token)match(input,CL,FOLLOW_CL_in_param_decl2379); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_CL.add(CL275);


            IDENTIFIER276=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_param_decl2381); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_IDENTIFIER.add(IDENTIFIER276);


            EQ277=(Token)match(input,EQ,FOLLOW_EQ_in_param_decl2383); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_EQ.add(EQ277);


            pushFollow(FOLLOW_expr_in_param_decl2385);
            expr278=expr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_expr.add(expr278.getTree());

            SC279=(Token)match(input,SC,FOLLOW_SC_in_param_decl2387); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_SC.add(SC279);


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
            // 303:2: -> ^( PAR IDENTIFIER par_type expr )
            {
                // parser/flatzinc/FlatzincFullExtParser.g:303:6: ^( PAR IDENTIFIER par_type expr )
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
    // parser/flatzinc/FlatzincFullExtParser.g:307:1: var_decl : var_type CL IDENTIFIER annotations ( EQ expr )? SC -> ^( VAR IDENTIFIER var_type annotations ( expr )? ) ;
    public final FlatzincFullExtParser.var_decl_return var_decl() throws RecognitionException {
        FlatzincFullExtParser.var_decl_return retval = new FlatzincFullExtParser.var_decl_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token CL281=null;
        Token IDENTIFIER282=null;
        Token EQ284=null;
        Token SC286=null;
        FlatzincFullExtParser.var_type_return var_type280 =null;

        FlatzincFullExtParser.annotations_return annotations283 =null;

        FlatzincFullExtParser.expr_return expr285 =null;


        Object CL281_tree=null;
        Object IDENTIFIER282_tree=null;
        Object EQ284_tree=null;
        Object SC286_tree=null;
        RewriteRuleTokenStream stream_SC=new RewriteRuleTokenStream(adaptor,"token SC");
        RewriteRuleTokenStream stream_EQ=new RewriteRuleTokenStream(adaptor,"token EQ");
        RewriteRuleTokenStream stream_IDENTIFIER=new RewriteRuleTokenStream(adaptor,"token IDENTIFIER");
        RewriteRuleTokenStream stream_CL=new RewriteRuleTokenStream(adaptor,"token CL");
        RewriteRuleSubtreeStream stream_var_type=new RewriteRuleSubtreeStream(adaptor,"rule var_type");
        RewriteRuleSubtreeStream stream_expr=new RewriteRuleSubtreeStream(adaptor,"rule expr");
        RewriteRuleSubtreeStream stream_annotations=new RewriteRuleSubtreeStream(adaptor,"rule annotations");
        try {
            // parser/flatzinc/FlatzincFullExtParser.g:308:2: ( var_type CL IDENTIFIER annotations ( EQ expr )? SC -> ^( VAR IDENTIFIER var_type annotations ( expr )? ) )
            // parser/flatzinc/FlatzincFullExtParser.g:308:6: var_type CL IDENTIFIER annotations ( EQ expr )? SC
            {
            pushFollow(FOLLOW_var_type_in_var_decl2415);
            var_type280=var_type();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_var_type.add(var_type280.getTree());

            CL281=(Token)match(input,CL,FOLLOW_CL_in_var_decl2417); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_CL.add(CL281);


            IDENTIFIER282=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_var_decl2419); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_IDENTIFIER.add(IDENTIFIER282);


            pushFollow(FOLLOW_annotations_in_var_decl2421);
            annotations283=annotations();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_annotations.add(annotations283.getTree());

            // parser/flatzinc/FlatzincFullExtParser.g:308:41: ( EQ expr )?
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
                    // parser/flatzinc/FlatzincFullExtParser.g:308:42: EQ expr
                    {
                    EQ284=(Token)match(input,EQ,FOLLOW_EQ_in_var_decl2424); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_EQ.add(EQ284);


                    pushFollow(FOLLOW_expr_in_var_decl2426);
                    expr285=expr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_expr.add(expr285.getTree());

                    }
                    break;

            }


            SC286=(Token)match(input,SC,FOLLOW_SC_in_var_decl2430); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_SC.add(SC286);


            // AST REWRITE
            // elements: expr, annotations, var_type, IDENTIFIER
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {

            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 309:2: -> ^( VAR IDENTIFIER var_type annotations ( expr )? )
            {
                // parser/flatzinc/FlatzincFullExtParser.g:309:6: ^( VAR IDENTIFIER var_type annotations ( expr )? )
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

                // parser/flatzinc/FlatzincFullExtParser.g:309:44: ( expr )?
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
    // parser/flatzinc/FlatzincFullExtParser.g:312:1: constraint : CONSTRAINT IDENTIFIER LP expr ( CM expr )* RP annotations SC -> ^( CONSTRAINT IDENTIFIER ( expr )+ annotations ) ;
    public final FlatzincFullExtParser.constraint_return constraint() throws RecognitionException {
        FlatzincFullExtParser.constraint_return retval = new FlatzincFullExtParser.constraint_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token CONSTRAINT287=null;
        Token IDENTIFIER288=null;
        Token LP289=null;
        Token CM291=null;
        Token RP293=null;
        Token SC295=null;
        FlatzincFullExtParser.expr_return expr290 =null;

        FlatzincFullExtParser.expr_return expr292 =null;

        FlatzincFullExtParser.annotations_return annotations294 =null;


        Object CONSTRAINT287_tree=null;
        Object IDENTIFIER288_tree=null;
        Object LP289_tree=null;
        Object CM291_tree=null;
        Object RP293_tree=null;
        Object SC295_tree=null;
        RewriteRuleTokenStream stream_SC=new RewriteRuleTokenStream(adaptor,"token SC");
        RewriteRuleTokenStream stream_RP=new RewriteRuleTokenStream(adaptor,"token RP");
        RewriteRuleTokenStream stream_CONSTRAINT=new RewriteRuleTokenStream(adaptor,"token CONSTRAINT");
        RewriteRuleTokenStream stream_IDENTIFIER=new RewriteRuleTokenStream(adaptor,"token IDENTIFIER");
        RewriteRuleTokenStream stream_CM=new RewriteRuleTokenStream(adaptor,"token CM");
        RewriteRuleTokenStream stream_LP=new RewriteRuleTokenStream(adaptor,"token LP");
        RewriteRuleSubtreeStream stream_expr=new RewriteRuleSubtreeStream(adaptor,"rule expr");
        RewriteRuleSubtreeStream stream_annotations=new RewriteRuleSubtreeStream(adaptor,"rule annotations");
        try {
            // parser/flatzinc/FlatzincFullExtParser.g:313:2: ( CONSTRAINT IDENTIFIER LP expr ( CM expr )* RP annotations SC -> ^( CONSTRAINT IDENTIFIER ( expr )+ annotations ) )
            // parser/flatzinc/FlatzincFullExtParser.g:313:6: CONSTRAINT IDENTIFIER LP expr ( CM expr )* RP annotations SC
            {
            CONSTRAINT287=(Token)match(input,CONSTRAINT,FOLLOW_CONSTRAINT_in_constraint2460); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_CONSTRAINT.add(CONSTRAINT287);


            IDENTIFIER288=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_constraint2462); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_IDENTIFIER.add(IDENTIFIER288);


            LP289=(Token)match(input,LP,FOLLOW_LP_in_constraint2464); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LP.add(LP289);


            pushFollow(FOLLOW_expr_in_constraint2466);
            expr290=expr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_expr.add(expr290.getTree());

            // parser/flatzinc/FlatzincFullExtParser.g:313:36: ( CM expr )*
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
            	    // parser/flatzinc/FlatzincFullExtParser.g:313:37: CM expr
            	    {
            	    CM291=(Token)match(input,CM,FOLLOW_CM_in_constraint2469); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_CM.add(CM291);


            	    pushFollow(FOLLOW_expr_in_constraint2471);
            	    expr292=expr();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_expr.add(expr292.getTree());

            	    }
            	    break;

            	default :
            	    break loop58;
                }
            } while (true);


            RP293=(Token)match(input,RP,FOLLOW_RP_in_constraint2475); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RP.add(RP293);


            pushFollow(FOLLOW_annotations_in_constraint2477);
            annotations294=annotations();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_annotations.add(annotations294.getTree());

            SC295=(Token)match(input,SC,FOLLOW_SC_in_constraint2479); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_SC.add(SC295);


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
            // 314:2: -> ^( CONSTRAINT IDENTIFIER ( expr )+ annotations )
            {
                // parser/flatzinc/FlatzincFullExtParser.g:314:6: ^( CONSTRAINT IDENTIFIER ( expr )+ annotations )
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
    // parser/flatzinc/FlatzincFullExtParser.g:317:1: solve_goal : SOLVE ^ annotations resolution SC !;
    public final FlatzincFullExtParser.solve_goal_return solve_goal() throws RecognitionException {
        FlatzincFullExtParser.solve_goal_return retval = new FlatzincFullExtParser.solve_goal_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token SOLVE296=null;
        Token SC299=null;
        FlatzincFullExtParser.annotations_return annotations297 =null;

        FlatzincFullExtParser.resolution_return resolution298 =null;


        Object SOLVE296_tree=null;
        Object SC299_tree=null;

        try {
            // parser/flatzinc/FlatzincFullExtParser.g:318:2: ( SOLVE ^ annotations resolution SC !)
            // parser/flatzinc/FlatzincFullExtParser.g:318:6: SOLVE ^ annotations resolution SC !
            {
            root_0 = (Object)adaptor.nil();


            SOLVE296=(Token)match(input,SOLVE,FOLLOW_SOLVE_in_solve_goal2507); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            SOLVE296_tree = 
            (Object)adaptor.create(SOLVE296)
            ;
            root_0 = (Object)adaptor.becomeRoot(SOLVE296_tree, root_0);
            }

            pushFollow(FOLLOW_annotations_in_solve_goal2510);
            annotations297=annotations();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, annotations297.getTree());

            pushFollow(FOLLOW_resolution_in_solve_goal2512);
            resolution298=resolution();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, resolution298.getTree());

            SC299=(Token)match(input,SC,FOLLOW_SC_in_solve_goal2514); if (state.failed) return retval;

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
    // parser/flatzinc/FlatzincFullExtParser.g:321:1: resolution : ( MINIMIZE ^ expr | MAXIMIZE ^ expr | SATISFY ^);
    public final FlatzincFullExtParser.resolution_return resolution() throws RecognitionException {
        FlatzincFullExtParser.resolution_return retval = new FlatzincFullExtParser.resolution_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token MINIMIZE300=null;
        Token MAXIMIZE302=null;
        Token SATISFY304=null;
        FlatzincFullExtParser.expr_return expr301 =null;

        FlatzincFullExtParser.expr_return expr303 =null;


        Object MINIMIZE300_tree=null;
        Object MAXIMIZE302_tree=null;
        Object SATISFY304_tree=null;

        try {
            // parser/flatzinc/FlatzincFullExtParser.g:322:5: ( MINIMIZE ^ expr | MAXIMIZE ^ expr | SATISFY ^)
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
                    // parser/flatzinc/FlatzincFullExtParser.g:322:9: MINIMIZE ^ expr
                    {
                    root_0 = (Object)adaptor.nil();


                    MINIMIZE300=(Token)match(input,MINIMIZE,FOLLOW_MINIMIZE_in_resolution2531); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    MINIMIZE300_tree = 
                    (Object)adaptor.create(MINIMIZE300)
                    ;
                    root_0 = (Object)adaptor.becomeRoot(MINIMIZE300_tree, root_0);
                    }

                    pushFollow(FOLLOW_expr_in_resolution2534);
                    expr301=expr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, expr301.getTree());

                    }
                    break;
                case 2 :
                    // parser/flatzinc/FlatzincFullExtParser.g:323:9: MAXIMIZE ^ expr
                    {
                    root_0 = (Object)adaptor.nil();


                    MAXIMIZE302=(Token)match(input,MAXIMIZE,FOLLOW_MAXIMIZE_in_resolution2544); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    MAXIMIZE302_tree = 
                    (Object)adaptor.create(MAXIMIZE302)
                    ;
                    root_0 = (Object)adaptor.becomeRoot(MAXIMIZE302_tree, root_0);
                    }

                    pushFollow(FOLLOW_expr_in_resolution2547);
                    expr303=expr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, expr303.getTree());

                    }
                    break;
                case 3 :
                    // parser/flatzinc/FlatzincFullExtParser.g:324:9: SATISFY ^
                    {
                    root_0 = (Object)adaptor.nil();


                    SATISFY304=(Token)match(input,SATISFY,FOLLOW_SATISFY_in_resolution2557); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    SATISFY304_tree = 
                    (Object)adaptor.create(SATISFY304)
                    ;
                    root_0 = (Object)adaptor.becomeRoot(SATISFY304_tree, root_0);
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
    // parser/flatzinc/FlatzincFullExtParser.g:327:1: annotations : ( DC annotation )* -> ^( ANNOTATIONS ( annotation )* ) ;
    public final FlatzincFullExtParser.annotations_return annotations() throws RecognitionException {
        FlatzincFullExtParser.annotations_return retval = new FlatzincFullExtParser.annotations_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token DC305=null;
        FlatzincFullExtParser.annotation_return annotation306 =null;


        Object DC305_tree=null;
        RewriteRuleTokenStream stream_DC=new RewriteRuleTokenStream(adaptor,"token DC");
        RewriteRuleSubtreeStream stream_annotation=new RewriteRuleSubtreeStream(adaptor,"rule annotation");
        try {
            // parser/flatzinc/FlatzincFullExtParser.g:328:5: ( ( DC annotation )* -> ^( ANNOTATIONS ( annotation )* ) )
            // parser/flatzinc/FlatzincFullExtParser.g:328:9: ( DC annotation )*
            {
            // parser/flatzinc/FlatzincFullExtParser.g:328:9: ( DC annotation )*
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
            	    // parser/flatzinc/FlatzincFullExtParser.g:328:10: DC annotation
            	    {
            	    DC305=(Token)match(input,DC,FOLLOW_DC_in_annotations2578); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_DC.add(DC305);


            	    pushFollow(FOLLOW_annotation_in_annotations2580);
            	    annotation306=annotation();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_annotation.add(annotation306.getTree());

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
            // 329:5: -> ^( ANNOTATIONS ( annotation )* )
            {
                // parser/flatzinc/FlatzincFullExtParser.g:329:9: ^( ANNOTATIONS ( annotation )* )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot(
                (Object)adaptor.create(ANNOTATIONS, "ANNOTATIONS")
                , root_1);

                // parser/flatzinc/FlatzincFullExtParser.g:329:23: ( annotation )*
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
    // parser/flatzinc/FlatzincFullExtParser.g:332:1: annotation : IDENTIFIER ( LP expr ( CM expr )* RP )? -> IDENTIFIER ( LP ( expr )+ RP )? ;
    public final FlatzincFullExtParser.annotation_return annotation() throws RecognitionException {
        FlatzincFullExtParser.annotation_return retval = new FlatzincFullExtParser.annotation_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token IDENTIFIER307=null;
        Token LP308=null;
        Token CM310=null;
        Token RP312=null;
        FlatzincFullExtParser.expr_return expr309 =null;

        FlatzincFullExtParser.expr_return expr311 =null;


        Object IDENTIFIER307_tree=null;
        Object LP308_tree=null;
        Object CM310_tree=null;
        Object RP312_tree=null;
        RewriteRuleTokenStream stream_RP=new RewriteRuleTokenStream(adaptor,"token RP");
        RewriteRuleTokenStream stream_IDENTIFIER=new RewriteRuleTokenStream(adaptor,"token IDENTIFIER");
        RewriteRuleTokenStream stream_CM=new RewriteRuleTokenStream(adaptor,"token CM");
        RewriteRuleTokenStream stream_LP=new RewriteRuleTokenStream(adaptor,"token LP");
        RewriteRuleSubtreeStream stream_expr=new RewriteRuleSubtreeStream(adaptor,"rule expr");
        try {
            // parser/flatzinc/FlatzincFullExtParser.g:333:5: ( IDENTIFIER ( LP expr ( CM expr )* RP )? -> IDENTIFIER ( LP ( expr )+ RP )? )
            // parser/flatzinc/FlatzincFullExtParser.g:333:9: IDENTIFIER ( LP expr ( CM expr )* RP )?
            {
            IDENTIFIER307=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_annotation2615); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_IDENTIFIER.add(IDENTIFIER307);


            // parser/flatzinc/FlatzincFullExtParser.g:333:20: ( LP expr ( CM expr )* RP )?
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
                    // parser/flatzinc/FlatzincFullExtParser.g:333:21: LP expr ( CM expr )* RP
                    {
                    LP308=(Token)match(input,LP,FOLLOW_LP_in_annotation2618); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LP.add(LP308);


                    pushFollow(FOLLOW_expr_in_annotation2620);
                    expr309=expr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_expr.add(expr309.getTree());

                    // parser/flatzinc/FlatzincFullExtParser.g:333:29: ( CM expr )*
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
                    	    // parser/flatzinc/FlatzincFullExtParser.g:333:30: CM expr
                    	    {
                    	    CM310=(Token)match(input,CM,FOLLOW_CM_in_annotation2623); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_CM.add(CM310);


                    	    pushFollow(FOLLOW_expr_in_annotation2625);
                    	    expr311=expr();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_expr.add(expr311.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop61;
                        }
                    } while (true);


                    RP312=(Token)match(input,RP,FOLLOW_RP_in_annotation2629); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RP.add(RP312);


                    }
                    break;

            }


            // AST REWRITE
            // elements: expr, IDENTIFIER, LP, RP
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {

            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 334:5: -> IDENTIFIER ( LP ( expr )+ RP )?
            {
                adaptor.addChild(root_0, 
                stream_IDENTIFIER.nextNode()
                );

                // parser/flatzinc/FlatzincFullExtParser.g:334:20: ( LP ( expr )+ RP )?
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
    // parser/flatzinc/FlatzincFullExtParser.g:338:1: bool_const : ( TRUE ^| FALSE ^);
    public final FlatzincFullExtParser.bool_const_return bool_const() throws RecognitionException {
        FlatzincFullExtParser.bool_const_return retval = new FlatzincFullExtParser.bool_const_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token TRUE313=null;
        Token FALSE314=null;

        Object TRUE313_tree=null;
        Object FALSE314_tree=null;

        try {
            // parser/flatzinc/FlatzincFullExtParser.g:339:5: ( TRUE ^| FALSE ^)
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
                    // parser/flatzinc/FlatzincFullExtParser.g:339:9: TRUE ^
                    {
                    root_0 = (Object)adaptor.nil();


                    TRUE313=(Token)match(input,TRUE,FOLLOW_TRUE_in_bool_const2670); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    TRUE313_tree = 
                    (Object)adaptor.create(TRUE313)
                    ;
                    root_0 = (Object)adaptor.becomeRoot(TRUE313_tree, root_0);
                    }

                    }
                    break;
                case 2 :
                    // parser/flatzinc/FlatzincFullExtParser.g:340:9: FALSE ^
                    {
                    root_0 = (Object)adaptor.nil();


                    FALSE314=(Token)match(input,FALSE,FOLLOW_FALSE_in_bool_const2681); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    FALSE314_tree = 
                    (Object)adaptor.create(FALSE314)
                    ;
                    root_0 = (Object)adaptor.becomeRoot(FALSE314_tree, root_0);
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
        // parser/flatzinc/FlatzincFullExtParser.g:169:4: ( attr_op ( DO attr_op )* ( DO attribute )? )
        // parser/flatzinc/FlatzincFullExtParser.g:169:4: attr_op ( DO attr_op )* ( DO attribute )?
        {
        pushFollow(FOLLOW_attr_op_in_synpred47_FlatzincFullExtParser977);
        attr_op();

        state._fsp--;
        if (state.failed) return ;

        // parser/flatzinc/FlatzincFullExtParser.g:169:12: ( DO attr_op )*
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
        	    // parser/flatzinc/FlatzincFullExtParser.g:169:13: DO attr_op
        	    {
        	    match(input,DO,FOLLOW_DO_in_synpred47_FlatzincFullExtParser980); if (state.failed) return ;

        	    pushFollow(FOLLOW_attr_op_in_synpred47_FlatzincFullExtParser982);
        	    attr_op();

        	    state._fsp--;
        	    if (state.failed) return ;

        	    }
        	    break;

        	default :
        	    break loop67;
            }
        } while (true);


        // parser/flatzinc/FlatzincFullExtParser.g:169:27: ( DO attribute )?
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
                // parser/flatzinc/FlatzincFullExtParser.g:169:28: DO attribute
                {
                match(input,DO,FOLLOW_DO_in_synpred47_FlatzincFullExtParser988); if (state.failed) return ;

                pushFollow(FOLLOW_attribute_in_synpred47_FlatzincFullExtParser990);
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
        "\1\10\1\56\2\uffff\1\50\1\31\1\23\1\51\1\50\1\73\1\23\1\31\1\23"+
        "\1\15\1\51\1\23";
    static final String DFA2_maxS =
        "\1\133\1\56\2\uffff\1\51\1\31\1\117\2\51\1\73\1\117\1\31\1\117\1"+
        "\133\1\51\1\117";
    static final String DFA2_acceptS =
        "\2\uffff\1\2\1\1\14\uffff";
    static final String DFA2_specialS =
        "\20\uffff}>";
    static final String[] DFA2_transitionS = {
            "\1\1\4\uffff\1\3\10\uffff\1\2\12\uffff\1\3\1\uffff\1\2\1\uffff"+
            "\1\2\2\uffff\1\3\3\uffff\1\2\6\uffff\1\2\27\uffff\1\2\1\uffff"+
            "\1\2\4\uffff\1\3\1\uffff\1\2\6\uffff\1\2",
            "\1\4",
            "",
            "",
            "\1\6\1\5",
            "\1\7",
            "\1\10\73\uffff\1\11",
            "\1\12",
            "\1\14\1\13",
            "\1\15",
            "\1\10\73\uffff\1\11",
            "\1\16",
            "\1\10\73\uffff\1\11",
            "\1\3\23\uffff\1\3\6\uffff\1\3\51\uffff\1\3\10\uffff\1\2",
            "\1\17",
            "\1\10\73\uffff\1\11"
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
        "\1\uffff\1\3\2\uffff\1\3\2\uffff";
    static final String DFA27_minS =
        "\1\6\1\23\1\6\1\uffff\1\23\1\0\1\uffff";
    static final String DFA27_maxS =
        "\1\130\1\121\1\135\1\uffff\1\121\1\0\1\uffff";
    static final String DFA27_acceptS =
        "\3\uffff\1\1\2\uffff\1\2";
    static final String DFA27_specialS =
        "\5\uffff\1\0\1\uffff}>";
    static final String[] DFA27_transitionS = {
            "\1\1\54\uffff\1\1\1\uffff\1\1\35\uffff\1\1\4\uffff\1\1",
            "\1\3\6\uffff\1\2\61\uffff\1\3\4\uffff\1\3",
            "\1\4\11\uffff\1\5\3\uffff\1\5\2\uffff\1\5\33\uffff\1\4\1\uffff"+
            "\1\4\17\uffff\1\5\1\uffff\2\5\1\uffff\1\5\10\uffff\1\4\4\uffff"+
            "\1\4\2\uffff\3\5",
            "",
            "\1\3\6\uffff\1\2\61\uffff\1\3\4\uffff\1\3",
            "\1\uffff",
            ""
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
            return "168:1: comb_attr : ( attr_op ( DO attr_op )* ( DO attribute )? -> ^( CA1 ( attr_op )* ( attribute )? ) | ( attr_op DO )+ attribute -> ^( CA2 ( attr_op )+ attribute ) );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA27_5 = input.LA(1);

                         
                        int index27_5 = input.index();
                        input.rewind();

                        s = -1;
                        if ( (synpred47_FlatzincFullExtParser()) ) {s = 3;}

                        else if ( (true) ) {s = 6;}

                         
                        input.seek(index27_5);

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
        "\1\10\1\56\2\uffff\1\50\1\31\1\23\1\51\1\50\1\73\1\23\1\31\1\23"+
        "\1\15\1\51\1\23";
    static final String DFA29_maxS =
        "\1\133\1\56\2\uffff\1\51\1\31\1\117\2\51\1\73\1\117\1\31\1\117\1"+
        "\133\1\51\1\117";
    static final String DFA29_acceptS =
        "\2\uffff\1\1\1\2\14\uffff";
    static final String DFA29_specialS =
        "\20\uffff}>";
    static final String[] DFA29_transitionS = {
            "\1\1\4\uffff\1\2\23\uffff\1\2\6\uffff\2\2\1\uffff\1\2\46\uffff"+
            "\1\2\10\uffff\1\3",
            "\1\4",
            "",
            "",
            "\1\6\1\5",
            "\1\7",
            "\1\10\73\uffff\1\11",
            "\1\12",
            "\1\14\1\13",
            "\1\15",
            "\1\10\73\uffff\1\11",
            "\1\16",
            "\1\10\73\uffff\1\11",
            "\1\2\23\uffff\1\2\6\uffff\2\2\1\uffff\1\2\46\uffff\1\2\10\uffff"+
            "\1\3",
            "\1\17",
            "\1\10\73\uffff\1\11"
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
            return "200:1: pred_param_type : ( par_pred_param_type | var_pred_param_type );";
        }
    }
    static final String DFA46_eotS =
        "\33\uffff";
    static final String DFA46_eofS =
        "\33\uffff";
    static final String DFA46_minS =
        "\1\10\1\56\1\uffff\1\73\2\uffff\2\50\1\31\1\23\2\uffff\1\51\1\50"+
        "\1\73\1\23\1\31\1\23\1\15\1\51\2\uffff\1\73\1\23\1\50\2\uffff";
    static final String DFA46_maxS =
        "\1\122\1\56\1\uffff\1\73\2\uffff\1\51\1\53\1\31\1\117\2\uffff\2"+
        "\51\1\73\1\117\1\31\1\117\1\122\1\51\2\uffff\1\73\1\117\1\53\2\uffff";
    static final String DFA46_acceptS =
        "\2\uffff\1\1\1\uffff\1\2\1\3\4\uffff\1\4\1\5\10\uffff\1\6\1\7\3"+
        "\uffff\1\10\1\11";
    static final String DFA46_specialS =
        "\33\uffff}>";
    static final String[] DFA46_transitionS = {
            "\1\1\4\uffff\1\2\23\uffff\1\2\6\uffff\1\2\1\4\1\uffff\1\5\46"+
            "\uffff\1\3",
            "\1\6",
            "",
            "\1\7",
            "",
            "",
            "\1\11\1\10",
            "\1\2\1\12\1\uffff\1\13",
            "\1\14",
            "\1\15\73\uffff\1\16",
            "",
            "",
            "\1\17",
            "\1\21\1\20",
            "\1\22",
            "\1\15\73\uffff\1\16",
            "\1\23",
            "\1\15\73\uffff\1\16",
            "\1\2\23\uffff\1\2\6\uffff\1\2\1\24\1\uffff\1\25\46\uffff\1"+
            "\26",
            "\1\27",
            "",
            "",
            "\1\30",
            "\1\15\73\uffff\1\16",
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
            return "242:1: par_pred_param_type : ( par_type | INT_CONST DD INT_CONST -> ^( DD INT_CONST INT_CONST ) | LB INT_CONST ( CM INT_CONST )* RB -> ^( CM ( INT_CONST )+ ) | SET OF INT_CONST DD INT_CONST -> ^( SET ^( DD INT_CONST INT_CONST ) ) | SET OF LB INT_CONST ( CM INT_CONST )* RB -> ^( SET ^( CM ( INT_CONST )+ ) ) | ARRAY LS index_set ( CM index_set )* RS OF INT_CONST DD INT_CONST -> ^( ARRAY ( index_set )+ ^( DD INT_CONST INT_CONST ) ) | ARRAY LS index_set ( CM index_set )* RS OF LB INT_CONST ( CM INT_CONST )* RB -> ^( ARRAY ( index_set )+ ^( CM ( INT_CONST )+ ) ) | ARRAY LS index_set ( CM index_set )* RS OF SET OF INT_CONST DD INT_CONST -> ^( ARRAY ( index_set )+ ^( SET ^( DD INT_CONST INT_CONST ) ) ) | ARRAY LS index_set ( CM index_set )* RS OF SET OF LB INT_CONST ( CM INT_CONST )* RB -> ^( ARRAY ( index_set )+ ^( SET ^( CM ( INT_CONST )+ ) ) ) );";
        }
    }
    static final String DFA48_eotS =
        "\27\uffff";
    static final String DFA48_eofS =
        "\27\uffff";
    static final String DFA48_minS =
        "\1\10\1\56\1\15\1\50\1\73\1\uffff\1\31\1\23\1\50\1\51\1\50\1\73"+
        "\1\uffff\1\23\1\31\1\23\1\133\1\51\1\15\1\23\1\73\1\50\1\uffff";
    static final String DFA48_maxS =
        "\1\133\1\56\1\122\1\51\1\73\1\uffff\1\31\1\117\1\53\2\51\1\73\1"+
        "\uffff\1\117\1\31\1\117\1\133\1\51\1\122\1\117\1\73\1\53\1\uffff";
    static final String DFA48_acceptS =
        "\5\uffff\1\1\6\uffff\1\2\11\uffff\1\3";
    static final String DFA48_specialS =
        "\27\uffff}>";
    static final String[] DFA48_transitionS = {
            "\1\1\122\uffff\1\2",
            "\1\3",
            "\1\5\23\uffff\1\5\6\uffff\2\5\1\uffff\1\5\46\uffff\1\4",
            "\1\7\1\6",
            "\1\10",
            "",
            "\1\11",
            "\1\12\73\uffff\1\13",
            "\1\14\1\5\1\uffff\1\5",
            "\1\15",
            "\1\17\1\16",
            "\1\20",
            "",
            "\1\12\73\uffff\1\13",
            "\1\21",
            "\1\12\73\uffff\1\13",
            "\1\22",
            "\1\23",
            "\1\5\23\uffff\1\5\6\uffff\2\5\1\uffff\1\5\46\uffff\1\24",
            "\1\12\73\uffff\1\13",
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
            return "267:1: var_pred_param_type : ( var_type -> ^( VAR var_type ) | VAR SET OF INT -> ^( VAR SET ) | ARRAY LS index_set ( CM index_set )* RS OF VAR SET OF INT -> ^( ARRAY ( index_set )+ ^( VAR SET ) ) );";
        }
    }
 

    public static final BitSet FOLLOW_pred_decl_in_flatzinc_ext_model67 = new BitSet(new long[]{0x0008112A00402100L,0x0000000008142A00L});
    public static final BitSet FOLLOW_param_decl_in_flatzinc_ext_model72 = new BitSet(new long[]{0x0008112A00402100L,0x0000000008142800L});
    public static final BitSet FOLLOW_var_decl_in_flatzinc_ext_model77 = new BitSet(new long[]{0x0008102800400100L,0x0000000008102800L});
    public static final BitSet FOLLOW_constraint_in_flatzinc_ext_model82 = new BitSet(new long[]{0x0008102800400000L,0x0000000000102800L});
    public static final BitSet FOLLOW_group_decl_in_flatzinc_ext_model87 = new BitSet(new long[]{0x0008102800000000L,0x0000000000102800L});
    public static final BitSet FOLLOW_structure_in_flatzinc_ext_model92 = new BitSet(new long[]{0x0000000000000000L,0x0000000000100000L});
    public static final BitSet FOLLOW_solve_goal_in_flatzinc_ext_model96 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENTIFIER_in_group_decl125 = new BitSet(new long[]{0x0000000000040000L});
    public static final BitSet FOLLOW_CL_in_group_decl127 = new BitSet(new long[]{0x0100204000910000L,0x000000003A0005A0L});
    public static final BitSet FOLLOW_predicates_in_group_decl129 = new BitSet(new long[]{0x0000000000000000L,0x0000000000020000L});
    public static final BitSet FOLLOW_SC_in_group_decl131 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_predicate_in_predicates160 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LP_in_predicates165 = new BitSet(new long[]{0x0100204000910000L,0x000000003A0005A0L});
    public static final BitSet FOLLOW_predicates_in_predicates167 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_AND_in_predicates170 = new BitSet(new long[]{0x0100204000910000L,0x000000003A0005A0L});
    public static final BitSet FOLLOW_predicates_in_predicates172 = new BitSet(new long[]{0x0000000000000010L,0x0000000000004000L});
    public static final BitSet FOLLOW_RP_in_predicates176 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LP_in_predicates192 = new BitSet(new long[]{0x0100204000910000L,0x000000003A0005A0L});
    public static final BitSet FOLLOW_predicates_in_predicates194 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000004L});
    public static final BitSet FOLLOW_OR_in_predicates197 = new BitSet(new long[]{0x0100204000910000L,0x000000003A0005A0L});
    public static final BitSet FOLLOW_predicates_in_predicates199 = new BitSet(new long[]{0x0000000000000000L,0x0000000000004004L});
    public static final BitSet FOLLOW_RP_in_predicates203 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TRUE_in_predicate226 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_attribute_in_predicate231 = new BitSet(new long[]{0xF400000000000000L,0x0000000000000002L});
    public static final BitSet FOLLOW_op_in_predicate233 = new BitSet(new long[]{0x0000020000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_predicate235 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IN_in_predicate240 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_LP_in_predicate242 = new BitSet(new long[]{0x0000002000000000L});
    public static final BitSet FOLLOW_IDENTIFIER_in_predicate244 = new BitSet(new long[]{0x0000000000080000L,0x0000000000004000L});
    public static final BitSet FOLLOW_CM_in_predicate247 = new BitSet(new long[]{0x0000002000000000L});
    public static final BitSet FOLLOW_IDENTIFIER_in_predicate249 = new BitSet(new long[]{0x0000000000080000L,0x0000000000004000L});
    public static final BitSet FOLLOW_RP_in_predicate253 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NOT_in_predicate269 = new BitSet(new long[]{0x0100004000910000L,0x000000003A0005A0L});
    public static final BitSet FOLLOW_predicate_in_predicate271 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_struct_in_structure451 = new BitSet(new long[]{0x0000000000000000L,0x0000000000020000L});
    public static final BitSet FOLLOW_SC_in_structure453 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_struct_reg_in_structure461 = new BitSet(new long[]{0x0000000000000000L,0x0000000000020000L});
    public static final BitSet FOLLOW_SC_in_structure463 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_coll_in_struct478 = new BitSet(new long[]{0x0800000000000000L});
    public static final BitSet FOLLOW_OF_in_struct480 = new BitSet(new long[]{0x0000080000000000L});
    public static final BitSet FOLLOW_LB_in_struct482 = new BitSet(new long[]{0x0008102800000000L,0x0000000000002800L});
    public static final BitSet FOLLOW_elt_in_struct484 = new BitSet(new long[]{0x0000000000080000L,0x0000000000001000L});
    public static final BitSet FOLLOW_CM_in_struct487 = new BitSet(new long[]{0x0008102800000000L,0x0000000000002800L});
    public static final BitSet FOLLOW_elt_in_struct489 = new BitSet(new long[]{0x0000000000080000L,0x0000000000001000L});
    public static final BitSet FOLLOW_RB_in_struct493 = new BitSet(new long[]{0x0000040000000002L});
    public static final BitSet FOLLOW_KEY_in_struct496 = new BitSet(new long[]{0x0028000000000040L,0x0000000001080000L});
    public static final BitSet FOLLOW_comb_attr_in_struct498 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENTIFIER_in_struct_reg530 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_AS_in_struct_reg532 = new BitSet(new long[]{0x0008100800000000L,0x0000000000002800L});
    public static final BitSet FOLLOW_coll_in_struct_reg534 = new BitSet(new long[]{0x0800000000000000L});
    public static final BitSet FOLLOW_OF_in_struct_reg536 = new BitSet(new long[]{0x0000080000000000L});
    public static final BitSet FOLLOW_LB_in_struct_reg538 = new BitSet(new long[]{0x0000000008000000L});
    public static final BitSet FOLLOW_many_in_struct_reg540 = new BitSet(new long[]{0x0000000000000000L,0x0000000000001000L});
    public static final BitSet FOLLOW_RB_in_struct_reg542 = new BitSet(new long[]{0x0000040000000002L});
    public static final BitSet FOLLOW_KEY_in_struct_reg545 = new BitSet(new long[]{0x0028000000000040L,0x0000000001080000L});
    public static final BitSet FOLLOW_comb_attr_in_struct_reg549 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_struct_reg_in_elt618 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_struct_in_elt628 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENTIFIER_in_elt638 = new BitSet(new long[]{0x0000040000000002L});
    public static final BitSet FOLLOW_KEY_in_elt641 = new BitSet(new long[]{0x0000000000910000L,0x00000000380005A0L});
    public static final BitSet FOLLOW_attribute_in_elt643 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_EACH_in_many659 = new BitSet(new long[]{0x0000000000910000L,0x00000000380005A0L});
    public static final BitSet FOLLOW_attribute_in_many661 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_AS_in_many663 = new BitSet(new long[]{0x0008100800000000L,0x0000000000002800L});
    public static final BitSet FOLLOW_coll_in_many665 = new BitSet(new long[]{0x0800040000000002L});
    public static final BitSet FOLLOW_OF_in_many668 = new BitSet(new long[]{0x0000080000000000L});
    public static final BitSet FOLLOW_LB_in_many670 = new BitSet(new long[]{0x0000000008000000L});
    public static final BitSet FOLLOW_many_in_many674 = new BitSet(new long[]{0x0000000000000000L,0x0000000000001000L});
    public static final BitSet FOLLOW_RB_in_many676 = new BitSet(new long[]{0x0000040000000002L});
    public static final BitSet FOLLOW_KEY_in_many681 = new BitSet(new long[]{0x0028000000000040L,0x0000000001080000L});
    public static final BitSet FOLLOW_comb_attr_in_many685 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_QUEUE_in_coll811 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_LP_in_coll813 = new BitSet(new long[]{0x0000000000000000L,0x0000000080000001L});
    public static final BitSet FOLLOW_qiter_in_coll815 = new BitSet(new long[]{0x0000000000000000L,0x0000000000004000L});
    public static final BitSet FOLLOW_RP_in_coll817 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_REV_in_coll839 = new BitSet(new long[]{0x0000100000000000L});
    public static final BitSet FOLLOW_LIST_in_coll843 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_LP_in_coll845 = new BitSet(new long[]{0x0000000400000000L,0x00000000C0000001L});
    public static final BitSet FOLLOW_liter_in_coll847 = new BitSet(new long[]{0x0000000000000000L,0x0000000000004000L});
    public static final BitSet FOLLOW_RP_in_coll849 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MAX_in_coll874 = new BitSet(new long[]{0x0000000800000000L});
    public static final BitSet FOLLOW_HEAP_in_coll878 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_LP_in_coll881 = new BitSet(new long[]{0x0000000000000000L,0x0000000080000001L});
    public static final BitSet FOLLOW_qiter_in_coll883 = new BitSet(new long[]{0x0000000000000000L,0x0000000000004000L});
    public static final BitSet FOLLOW_RP_in_coll885 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_qiter_in_liter942 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FOR_in_liter952 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_WFOR_in_liter962 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_attr_op_in_comb_attr977 = new BitSet(new long[]{0x0000000004000002L});
    public static final BitSet FOLLOW_DO_in_comb_attr980 = new BitSet(new long[]{0x0028000000000040L,0x0000000001080000L});
    public static final BitSet FOLLOW_attr_op_in_comb_attr982 = new BitSet(new long[]{0x0000000004000002L});
    public static final BitSet FOLLOW_DO_in_comb_attr988 = new BitSet(new long[]{0x0000000000910000L,0x00000000380005A0L});
    public static final BitSet FOLLOW_attribute_in_comb_attr990 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_attr_op_in_comb_attr1014 = new BitSet(new long[]{0x0000000004000000L});
    public static final BitSet FOLLOW_DO_in_comb_attr1016 = new BitSet(new long[]{0x0028000000910040L,0x00000000390805A0L});
    public static final BitSet FOLLOW_attribute_in_comb_attr1020 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PREDICATE_in_pred_decl1107 = new BitSet(new long[]{0x0000002000000000L});
    public static final BitSet FOLLOW_IDENTIFIER_in_pred_decl1109 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_LP_in_pred_decl1111 = new BitSet(new long[]{0x00000B0200002100L,0x0000000008040000L});
    public static final BitSet FOLLOW_pred_param_in_pred_decl1113 = new BitSet(new long[]{0x0000000000080000L,0x0000000000004000L});
    public static final BitSet FOLLOW_CM_in_pred_decl1116 = new BitSet(new long[]{0x00000B0200002100L,0x0000000008040000L});
    public static final BitSet FOLLOW_pred_param_in_pred_decl1118 = new BitSet(new long[]{0x0000000000080000L,0x0000000000004000L});
    public static final BitSet FOLLOW_RP_in_pred_decl1122 = new BitSet(new long[]{0x0000000000000000L,0x0000000000020000L});
    public static final BitSet FOLLOW_SC_in_pred_decl1124 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_pred_param_type_in_pred_param1152 = new BitSet(new long[]{0x0000000000040000L});
    public static final BitSet FOLLOW_CL_in_pred_param1154 = new BitSet(new long[]{0x0000002000000000L});
    public static final BitSet FOLLOW_IDENTIFIER_in_pred_param1156 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_par_pred_param_type_in_pred_param_type1190 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_var_pred_param_type_in_pred_param_type1200 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ARRAY_in_par_type1219 = new BitSet(new long[]{0x0000400000000000L});
    public static final BitSet FOLLOW_LS_in_par_type1221 = new BitSet(new long[]{0x0000030000000000L});
    public static final BitSet FOLLOW_index_set_in_par_type1223 = new BitSet(new long[]{0x0000000000080000L,0x0000000000008000L});
    public static final BitSet FOLLOW_CM_in_par_type1226 = new BitSet(new long[]{0x0000030000000000L});
    public static final BitSet FOLLOW_index_set_in_par_type1228 = new BitSet(new long[]{0x0000000000080000L,0x0000000000008000L});
    public static final BitSet FOLLOW_RS_in_par_type1232 = new BitSet(new long[]{0x0800000000000000L});
    public static final BitSet FOLLOW_OF_in_par_type1234 = new BitSet(new long[]{0x0000010200002000L,0x0000000000040000L});
    public static final BitSet FOLLOW_par_type_u_in_par_type1236 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_par_type_u_in_par_type1262 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_BOOL_in_par_type_u1294 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FLOAT_in_par_type_u1304 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SET_in_par_type_u1314 = new BitSet(new long[]{0x0800000000000000L});
    public static final BitSet FOLLOW_OF_in_par_type_u1316 = new BitSet(new long[]{0x0000010000000000L});
    public static final BitSet FOLLOW_INT_in_par_type_u1318 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INT_in_par_type_u1328 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ARRAY_in_var_type1347 = new BitSet(new long[]{0x0000400000000000L});
    public static final BitSet FOLLOW_LS_in_var_type1349 = new BitSet(new long[]{0x0000030000000000L});
    public static final BitSet FOLLOW_index_set_in_var_type1351 = new BitSet(new long[]{0x0000000000080000L,0x0000000000008000L});
    public static final BitSet FOLLOW_CM_in_var_type1354 = new BitSet(new long[]{0x0000030000000000L});
    public static final BitSet FOLLOW_index_set_in_var_type1356 = new BitSet(new long[]{0x0000000000080000L,0x0000000000008000L});
    public static final BitSet FOLLOW_RS_in_var_type1360 = new BitSet(new long[]{0x0800000000000000L});
    public static final BitSet FOLLOW_OF_in_var_type1362 = new BitSet(new long[]{0x0000000000000000L,0x0000000008000000L});
    public static final BitSet FOLLOW_VAR_in_var_type1364 = new BitSet(new long[]{0x00000B0200002000L,0x0000000000040000L});
    public static final BitSet FOLLOW_var_type_u_in_var_type1366 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_VAR_in_var_type1392 = new BitSet(new long[]{0x00000B0200002000L,0x0000000000040000L});
    public static final BitSet FOLLOW_var_type_u_in_var_type1394 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_BOOL_in_var_type_u1426 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FLOAT_in_var_type_u1436 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INT_in_var_type_u1446 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INT_CONST_in_var_type_u1456 = new BitSet(new long[]{0x0000000002000000L});
    public static final BitSet FOLLOW_DD_in_var_type_u1458 = new BitSet(new long[]{0x0000020000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_var_type_u1460 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LB_in_var_type_u1487 = new BitSet(new long[]{0x0000020000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_var_type_u1489 = new BitSet(new long[]{0x0000000000080000L,0x0000000000001000L});
    public static final BitSet FOLLOW_CM_in_var_type_u1492 = new BitSet(new long[]{0x0000020000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_var_type_u1494 = new BitSet(new long[]{0x0000000000080000L,0x0000000000001000L});
    public static final BitSet FOLLOW_RB_in_var_type_u1498 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SET_in_var_type_u1522 = new BitSet(new long[]{0x0800000000000000L});
    public static final BitSet FOLLOW_OF_in_var_type_u1524 = new BitSet(new long[]{0x0000020000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_var_type_u1526 = new BitSet(new long[]{0x0000000002000000L});
    public static final BitSet FOLLOW_DD_in_var_type_u1528 = new BitSet(new long[]{0x0000020000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_var_type_u1530 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SET_in_var_type_u1559 = new BitSet(new long[]{0x0800000000000000L});
    public static final BitSet FOLLOW_OF_in_var_type_u1561 = new BitSet(new long[]{0x0000080000000000L});
    public static final BitSet FOLLOW_LB_in_var_type_u1563 = new BitSet(new long[]{0x0000020000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_var_type_u1565 = new BitSet(new long[]{0x0000000000080000L,0x0000000000001000L});
    public static final BitSet FOLLOW_CM_in_var_type_u1568 = new BitSet(new long[]{0x0000020000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_var_type_u1570 = new BitSet(new long[]{0x0000000000080000L,0x0000000000001000L});
    public static final BitSet FOLLOW_RB_in_var_type_u1574 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_par_type_in_par_pred_param_type1612 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type1624 = new BitSet(new long[]{0x0000000002000000L});
    public static final BitSet FOLLOW_DD_in_par_pred_param_type1626 = new BitSet(new long[]{0x0000020000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type1628 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LB_in_par_pred_param_type1653 = new BitSet(new long[]{0x0000020000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type1655 = new BitSet(new long[]{0x0000000000080000L,0x0000000000001000L});
    public static final BitSet FOLLOW_CM_in_par_pred_param_type1658 = new BitSet(new long[]{0x0000020000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type1660 = new BitSet(new long[]{0x0000000000080000L,0x0000000000001000L});
    public static final BitSet FOLLOW_RB_in_par_pred_param_type1664 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SET_in_par_pred_param_type1688 = new BitSet(new long[]{0x0800000000000000L});
    public static final BitSet FOLLOW_OF_in_par_pred_param_type1690 = new BitSet(new long[]{0x0000020000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type1692 = new BitSet(new long[]{0x0000000002000000L});
    public static final BitSet FOLLOW_DD_in_par_pred_param_type1694 = new BitSet(new long[]{0x0000020000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type1696 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SET_in_par_pred_param_type1725 = new BitSet(new long[]{0x0800000000000000L});
    public static final BitSet FOLLOW_OF_in_par_pred_param_type1727 = new BitSet(new long[]{0x0000080000000000L});
    public static final BitSet FOLLOW_LB_in_par_pred_param_type1729 = new BitSet(new long[]{0x0000020000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type1731 = new BitSet(new long[]{0x0000000000080000L,0x0000000000001000L});
    public static final BitSet FOLLOW_CM_in_par_pred_param_type1734 = new BitSet(new long[]{0x0000020000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type1736 = new BitSet(new long[]{0x0000000000080000L,0x0000000000001000L});
    public static final BitSet FOLLOW_RB_in_par_pred_param_type1740 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ARRAY_in_par_pred_param_type1770 = new BitSet(new long[]{0x0000400000000000L});
    public static final BitSet FOLLOW_LS_in_par_pred_param_type1772 = new BitSet(new long[]{0x0000030000000000L});
    public static final BitSet FOLLOW_index_set_in_par_pred_param_type1774 = new BitSet(new long[]{0x0000000000080000L,0x0000000000008000L});
    public static final BitSet FOLLOW_CM_in_par_pred_param_type1777 = new BitSet(new long[]{0x0000030000000000L});
    public static final BitSet FOLLOW_index_set_in_par_pred_param_type1779 = new BitSet(new long[]{0x0000000000080000L,0x0000000000008000L});
    public static final BitSet FOLLOW_RS_in_par_pred_param_type1783 = new BitSet(new long[]{0x0800000000000000L});
    public static final BitSet FOLLOW_OF_in_par_pred_param_type1785 = new BitSet(new long[]{0x0000020000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type1787 = new BitSet(new long[]{0x0000000002000000L});
    public static final BitSet FOLLOW_DD_in_par_pred_param_type1789 = new BitSet(new long[]{0x0000020000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type1791 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ARRAY_in_par_pred_param_type1823 = new BitSet(new long[]{0x0000400000000000L});
    public static final BitSet FOLLOW_LS_in_par_pred_param_type1825 = new BitSet(new long[]{0x0000030000000000L});
    public static final BitSet FOLLOW_index_set_in_par_pred_param_type1827 = new BitSet(new long[]{0x0000000000080000L,0x0000000000008000L});
    public static final BitSet FOLLOW_CM_in_par_pred_param_type1830 = new BitSet(new long[]{0x0000030000000000L});
    public static final BitSet FOLLOW_index_set_in_par_pred_param_type1832 = new BitSet(new long[]{0x0000000000080000L,0x0000000000008000L});
    public static final BitSet FOLLOW_RS_in_par_pred_param_type1836 = new BitSet(new long[]{0x0800000000000000L});
    public static final BitSet FOLLOW_OF_in_par_pred_param_type1838 = new BitSet(new long[]{0x0000080000000000L});
    public static final BitSet FOLLOW_LB_in_par_pred_param_type1840 = new BitSet(new long[]{0x0000020000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type1842 = new BitSet(new long[]{0x0000000000080000L,0x0000000000001000L});
    public static final BitSet FOLLOW_CM_in_par_pred_param_type1845 = new BitSet(new long[]{0x0000020000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type1847 = new BitSet(new long[]{0x0000000000080000L,0x0000000000001000L});
    public static final BitSet FOLLOW_RB_in_par_pred_param_type1851 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ARRAY_in_par_pred_param_type1882 = new BitSet(new long[]{0x0000400000000000L});
    public static final BitSet FOLLOW_LS_in_par_pred_param_type1884 = new BitSet(new long[]{0x0000030000000000L});
    public static final BitSet FOLLOW_index_set_in_par_pred_param_type1886 = new BitSet(new long[]{0x0000000000080000L,0x0000000000008000L});
    public static final BitSet FOLLOW_CM_in_par_pred_param_type1889 = new BitSet(new long[]{0x0000030000000000L});
    public static final BitSet FOLLOW_index_set_in_par_pred_param_type1891 = new BitSet(new long[]{0x0000000000080000L,0x0000000000008000L});
    public static final BitSet FOLLOW_RS_in_par_pred_param_type1895 = new BitSet(new long[]{0x0800000000000000L});
    public static final BitSet FOLLOW_OF_in_par_pred_param_type1897 = new BitSet(new long[]{0x0000000000000000L,0x0000000000040000L});
    public static final BitSet FOLLOW_SET_in_par_pred_param_type1899 = new BitSet(new long[]{0x0800000000000000L});
    public static final BitSet FOLLOW_OF_in_par_pred_param_type1901 = new BitSet(new long[]{0x0000020000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type1903 = new BitSet(new long[]{0x0000000002000000L});
    public static final BitSet FOLLOW_DD_in_par_pred_param_type1905 = new BitSet(new long[]{0x0000020000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type1907 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ARRAY_in_par_pred_param_type1943 = new BitSet(new long[]{0x0000400000000000L});
    public static final BitSet FOLLOW_LS_in_par_pred_param_type1945 = new BitSet(new long[]{0x0000030000000000L});
    public static final BitSet FOLLOW_index_set_in_par_pred_param_type1947 = new BitSet(new long[]{0x0000000000080000L,0x0000000000008000L});
    public static final BitSet FOLLOW_CM_in_par_pred_param_type1950 = new BitSet(new long[]{0x0000030000000000L});
    public static final BitSet FOLLOW_index_set_in_par_pred_param_type1952 = new BitSet(new long[]{0x0000000000080000L,0x0000000000008000L});
    public static final BitSet FOLLOW_RS_in_par_pred_param_type1956 = new BitSet(new long[]{0x0800000000000000L});
    public static final BitSet FOLLOW_OF_in_par_pred_param_type1958 = new BitSet(new long[]{0x0000000000000000L,0x0000000000040000L});
    public static final BitSet FOLLOW_SET_in_par_pred_param_type1960 = new BitSet(new long[]{0x0800000000000000L});
    public static final BitSet FOLLOW_OF_in_par_pred_param_type1962 = new BitSet(new long[]{0x0000080000000000L});
    public static final BitSet FOLLOW_LB_in_par_pred_param_type1964 = new BitSet(new long[]{0x0000020000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type1966 = new BitSet(new long[]{0x0000000000080000L,0x0000000000001000L});
    public static final BitSet FOLLOW_CM_in_par_pred_param_type1969 = new BitSet(new long[]{0x0000020000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type1971 = new BitSet(new long[]{0x0000000000080000L,0x0000000000001000L});
    public static final BitSet FOLLOW_RB_in_par_pred_param_type1975 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_var_type_in_var_pred_param_type2020 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_VAR_in_var_pred_param_type2043 = new BitSet(new long[]{0x0000000000000000L,0x0000000000040000L});
    public static final BitSet FOLLOW_SET_in_var_pred_param_type2045 = new BitSet(new long[]{0x0800000000000000L});
    public static final BitSet FOLLOW_OF_in_var_pred_param_type2047 = new BitSet(new long[]{0x0000010000000000L});
    public static final BitSet FOLLOW_INT_in_var_pred_param_type2049 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ARRAY_in_var_pred_param_type2072 = new BitSet(new long[]{0x0000400000000000L});
    public static final BitSet FOLLOW_LS_in_var_pred_param_type2074 = new BitSet(new long[]{0x0000030000000000L});
    public static final BitSet FOLLOW_index_set_in_var_pred_param_type2076 = new BitSet(new long[]{0x0000000000080000L,0x0000000000008000L});
    public static final BitSet FOLLOW_CM_in_var_pred_param_type2079 = new BitSet(new long[]{0x0000030000000000L});
    public static final BitSet FOLLOW_index_set_in_var_pred_param_type2081 = new BitSet(new long[]{0x0000000000080000L,0x0000000000008000L});
    public static final BitSet FOLLOW_RS_in_var_pred_param_type2085 = new BitSet(new long[]{0x0800000000000000L});
    public static final BitSet FOLLOW_OF_in_var_pred_param_type2087 = new BitSet(new long[]{0x0000000000000000L,0x0000000008000000L});
    public static final BitSet FOLLOW_VAR_in_var_pred_param_type2089 = new BitSet(new long[]{0x0000000000000000L,0x0000000000040000L});
    public static final BitSet FOLLOW_SET_in_var_pred_param_type2091 = new BitSet(new long[]{0x0800000000000000L});
    public static final BitSet FOLLOW_OF_in_var_pred_param_type2093 = new BitSet(new long[]{0x0000010000000000L});
    public static final BitSet FOLLOW_INT_in_var_pred_param_type2095 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INT_CONST_in_index_set2134 = new BitSet(new long[]{0x0000000002000000L});
    public static final BitSet FOLLOW_DD_in_index_set2136 = new BitSet(new long[]{0x0000020000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_index_set2138 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INT_in_index_set2167 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LB_in_expr2199 = new BitSet(new long[]{0x0000020000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_expr2201 = new BitSet(new long[]{0x0000000000080000L,0x0000000000001000L});
    public static final BitSet FOLLOW_CM_in_expr2204 = new BitSet(new long[]{0x0000020000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_expr2206 = new BitSet(new long[]{0x0000000000080000L,0x0000000000001000L});
    public static final BitSet FOLLOW_RB_in_expr2210 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_bool_const_in_expr2234 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INT_CONST_in_expr2244 = new BitSet(new long[]{0x0000000002000002L});
    public static final BitSet FOLLOW_DD_in_expr2247 = new BitSet(new long[]{0x0000020000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_expr2249 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LS_in_expr2261 = new BitSet(new long[]{0x00004A2100000000L,0x0000000002408000L});
    public static final BitSet FOLLOW_expr_in_expr2264 = new BitSet(new long[]{0x0000000000080000L,0x0000000000008000L});
    public static final BitSet FOLLOW_CM_in_expr2267 = new BitSet(new long[]{0x00004A2100000000L,0x0000000002400000L});
    public static final BitSet FOLLOW_expr_in_expr2269 = new BitSet(new long[]{0x0000000000080000L,0x0000000000008000L});
    public static final BitSet FOLLOW_RS_in_expr2275 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_id_expr_in_expr2303 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_in_expr2313 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENTIFIER_in_id_expr2334 = new BitSet(new long[]{0x0000600000000002L});
    public static final BitSet FOLLOW_LP_in_id_expr2338 = new BitSet(new long[]{0x00004A2100000000L,0x0000000002400000L});
    public static final BitSet FOLLOW_expr_in_id_expr2340 = new BitSet(new long[]{0x0000000000080000L,0x0000000000004000L});
    public static final BitSet FOLLOW_CM_in_id_expr2343 = new BitSet(new long[]{0x00004A2100000000L,0x0000000002400000L});
    public static final BitSet FOLLOW_expr_in_id_expr2345 = new BitSet(new long[]{0x0000000000080000L,0x0000000000004000L});
    public static final BitSet FOLLOW_RP_in_id_expr2349 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LS_in_id_expr2353 = new BitSet(new long[]{0x0000020000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_id_expr2355 = new BitSet(new long[]{0x0000000000000000L,0x0000000000008000L});
    public static final BitSet FOLLOW_RS_in_id_expr2357 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_par_type_in_param_decl2377 = new BitSet(new long[]{0x0000000000040000L});
    public static final BitSet FOLLOW_CL_in_param_decl2379 = new BitSet(new long[]{0x0000002000000000L});
    public static final BitSet FOLLOW_IDENTIFIER_in_param_decl2381 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_EQ_in_param_decl2383 = new BitSet(new long[]{0x00004A2100000000L,0x0000000002400000L});
    public static final BitSet FOLLOW_expr_in_param_decl2385 = new BitSet(new long[]{0x0000000000000000L,0x0000000000020000L});
    public static final BitSet FOLLOW_SC_in_param_decl2387 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_var_type_in_var_decl2415 = new BitSet(new long[]{0x0000000000040000L});
    public static final BitSet FOLLOW_CL_in_var_decl2417 = new BitSet(new long[]{0x0000002000000000L});
    public static final BitSet FOLLOW_IDENTIFIER_in_var_decl2419 = new BitSet(new long[]{0x0000000011000000L,0x0000000000020000L});
    public static final BitSet FOLLOW_annotations_in_var_decl2421 = new BitSet(new long[]{0x0000000010000000L,0x0000000000020000L});
    public static final BitSet FOLLOW_EQ_in_var_decl2424 = new BitSet(new long[]{0x00004A2100000000L,0x0000000002400000L});
    public static final BitSet FOLLOW_expr_in_var_decl2426 = new BitSet(new long[]{0x0000000000000000L,0x0000000000020000L});
    public static final BitSet FOLLOW_SC_in_var_decl2430 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CONSTRAINT_in_constraint2460 = new BitSet(new long[]{0x0000002000000000L});
    public static final BitSet FOLLOW_IDENTIFIER_in_constraint2462 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_LP_in_constraint2464 = new BitSet(new long[]{0x00004A2100000000L,0x0000000002400000L});
    public static final BitSet FOLLOW_expr_in_constraint2466 = new BitSet(new long[]{0x0000000000080000L,0x0000000000004000L});
    public static final BitSet FOLLOW_CM_in_constraint2469 = new BitSet(new long[]{0x00004A2100000000L,0x0000000002400000L});
    public static final BitSet FOLLOW_expr_in_constraint2471 = new BitSet(new long[]{0x0000000000080000L,0x0000000000004000L});
    public static final BitSet FOLLOW_RP_in_constraint2475 = new BitSet(new long[]{0x0000000001000000L,0x0000000000020000L});
    public static final BitSet FOLLOW_annotations_in_constraint2477 = new BitSet(new long[]{0x0000000000000000L,0x0000000000020000L});
    public static final BitSet FOLLOW_SC_in_constraint2479 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SOLVE_in_solve_goal2507 = new BitSet(new long[]{0x0050000001000000L,0x0000000000010000L});
    public static final BitSet FOLLOW_annotations_in_solve_goal2510 = new BitSet(new long[]{0x0050000000000000L,0x0000000000010000L});
    public static final BitSet FOLLOW_resolution_in_solve_goal2512 = new BitSet(new long[]{0x0000000000000000L,0x0000000000020000L});
    public static final BitSet FOLLOW_SC_in_solve_goal2514 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MINIMIZE_in_resolution2531 = new BitSet(new long[]{0x00004A2100000000L,0x0000000002400000L});
    public static final BitSet FOLLOW_expr_in_resolution2534 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MAXIMIZE_in_resolution2544 = new BitSet(new long[]{0x00004A2100000000L,0x0000000002400000L});
    public static final BitSet FOLLOW_expr_in_resolution2547 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SATISFY_in_resolution2557 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DC_in_annotations2578 = new BitSet(new long[]{0x0000002000000000L});
    public static final BitSet FOLLOW_annotation_in_annotations2580 = new BitSet(new long[]{0x0000000001000002L});
    public static final BitSet FOLLOW_IDENTIFIER_in_annotation2615 = new BitSet(new long[]{0x0000200000000002L});
    public static final BitSet FOLLOW_LP_in_annotation2618 = new BitSet(new long[]{0x00004A2100000000L,0x0000000002400000L});
    public static final BitSet FOLLOW_expr_in_annotation2620 = new BitSet(new long[]{0x0000000000080000L,0x0000000000004000L});
    public static final BitSet FOLLOW_CM_in_annotation2623 = new BitSet(new long[]{0x00004A2100000000L,0x0000000002400000L});
    public static final BitSet FOLLOW_expr_in_annotation2625 = new BitSet(new long[]{0x0000000000080000L,0x0000000000004000L});
    public static final BitSet FOLLOW_RP_in_annotation2629 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TRUE_in_bool_const2670 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FALSE_in_bool_const2681 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LP_in_synpred9_FlatzincFullExtParser165 = new BitSet(new long[]{0x0100204000910000L,0x000000003A0005A0L});
    public static final BitSet FOLLOW_predicates_in_synpred9_FlatzincFullExtParser167 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_AND_in_synpred9_FlatzincFullExtParser170 = new BitSet(new long[]{0x0100204000910000L,0x000000003A0005A0L});
    public static final BitSet FOLLOW_predicates_in_synpred9_FlatzincFullExtParser172 = new BitSet(new long[]{0x0000000000000010L,0x0000000000004000L});
    public static final BitSet FOLLOW_RP_in_synpred9_FlatzincFullExtParser176 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_attr_op_in_synpred47_FlatzincFullExtParser977 = new BitSet(new long[]{0x0000000004000002L});
    public static final BitSet FOLLOW_DO_in_synpred47_FlatzincFullExtParser980 = new BitSet(new long[]{0x0028000000000040L,0x0000000001080000L});
    public static final BitSet FOLLOW_attr_op_in_synpred47_FlatzincFullExtParser982 = new BitSet(new long[]{0x0000000004000002L});
    public static final BitSet FOLLOW_DO_in_synpred47_FlatzincFullExtParser988 = new BitSet(new long[]{0x0000000000910000L,0x00000000380005A0L});
    public static final BitSet FOLLOW_attribute_in_synpred47_FlatzincFullExtParser990 = new BitSet(new long[]{0x0000000000000002L});

}