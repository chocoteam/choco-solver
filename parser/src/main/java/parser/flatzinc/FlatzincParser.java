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

// $ANTLR 3.4 parser/flatzinc/FlatzincParser.g 2012-11-13 10:00:41

package parser.flatzinc;


import org.antlr.runtime.*;
import org.antlr.runtime.tree.*;


@SuppressWarnings({"all", "warnings", "unchecked"})
public class FlatzincParser extends Parser {
    public static final String[] tokenNames = new String[]{
            "<invalid>", "<EOR>", "<DOWN>", "<UP>", "ANNOTATIONS", "APAR", "ARRAY", "ARRPAR", "ARRVAR", "AVAR", "BOOL", "CHAR", "CL", "CM", "COMMENT", "CONSTRAINT", "DC", "DD", "DO", "EQ", "ESC_SEQ", "EXPONENT", "EXPR", "FALSE", "FLOAT", "HEX_DIGIT", "IDENTIFIER", "INDEX", "INT", "INT_CONST", "LB", "LP", "LS", "MAXIMIZE", "MINIMIZE", "MN", "OCTAL_ESC", "OF", "PAR", "PL", "PREDICATE", "RB", "RP", "RS", "SATISFY", "SC", "SET", "SOLVE", "STRING", "TRUE", "UNICODE_ESC", "VAR", "WS"
    };

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
    public Parser[] getDelegates() {
        return new Parser[]{};
    }

    // delegators


    public FlatzincParser(TokenStream input) {
        this(input, new RecognizerSharedState());
    }

    public FlatzincParser(TokenStream input, RecognizerSharedState state) {
        super(input, state);
    }

    protected TreeAdaptor adaptor = new CommonTreeAdaptor();

    public void setTreeAdaptor(TreeAdaptor adaptor) {
        this.adaptor = adaptor;
    }

    public TreeAdaptor getTreeAdaptor() {
        return adaptor;
    }

    public String[] getTokenNames() {
        return FlatzincParser.tokenNames;
    }

    public String getGrammarFileName() {
        return "parser/flatzinc/FlatzincParser.g";
    }


    public static class flatzinc_model_return extends ParserRuleReturnScope {
        Object tree;

        public Object getTree() {
            return tree;
        }
    }

    ;


    // $ANTLR start "flatzinc_model"
    // parser/flatzinc/FlatzincParser.g:43:1: flatzinc_model : ( pred_decl )* ( param_decl )* ( var_decl )* ( constraint )* solve_goal ;
    public final FlatzincParser.flatzinc_model_return flatzinc_model() throws RecognitionException {
        FlatzincParser.flatzinc_model_return retval = new FlatzincParser.flatzinc_model_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        FlatzincParser.pred_decl_return pred_decl1 = null;

        FlatzincParser.param_decl_return param_decl2 = null;

        FlatzincParser.var_decl_return var_decl3 = null;

        FlatzincParser.constraint_return constraint4 = null;

        FlatzincParser.solve_goal_return solve_goal5 = null;


        try {
            // parser/flatzinc/FlatzincParser.g:44:2: ( ( pred_decl )* ( param_decl )* ( var_decl )* ( constraint )* solve_goal )
            // parser/flatzinc/FlatzincParser.g:44:6: ( pred_decl )* ( param_decl )* ( var_decl )* ( constraint )* solve_goal
            {
                root_0 = (Object) adaptor.nil();


                // parser/flatzinc/FlatzincParser.g:44:6: ( pred_decl )*
                loop1:
                do {
                    int alt1 = 2;
                    switch (input.LA(1)) {
                        case PREDICATE: {
                            alt1 = 1;
                        }
                        break;

                    }

                    switch (alt1) {
                        case 1:
                            // parser/flatzinc/FlatzincParser.g:44:7: pred_decl
                        {
                            pushFollow(FOLLOW_pred_decl_in_flatzinc_model59);
                            pred_decl1 = pred_decl();

                            state._fsp--;

                            adaptor.addChild(root_0, pred_decl1.getTree());

                        }
                        break;

                        default:
                            break loop1;
                    }
                } while (true);


                // parser/flatzinc/FlatzincParser.g:44:19: ( param_decl )*
                loop2:
                do {
                    int alt2 = 2;
                    alt2 = dfa2.predict(input);
                    switch (alt2) {
                        case 1:
                            // parser/flatzinc/FlatzincParser.g:44:20: param_decl
                        {
                            pushFollow(FOLLOW_param_decl_in_flatzinc_model64);
                            param_decl2 = param_decl();

                            state._fsp--;

                            adaptor.addChild(root_0, param_decl2.getTree());

                        }
                        break;

                        default:
                            break loop2;
                    }
                } while (true);


                // parser/flatzinc/FlatzincParser.g:44:33: ( var_decl )*
                loop3:
                do {
                    int alt3 = 2;
                    switch (input.LA(1)) {
                        case ARRAY:
                        case VAR: {
                            alt3 = 1;
                        }
                        break;

                    }

                    switch (alt3) {
                        case 1:
                            // parser/flatzinc/FlatzincParser.g:44:34: var_decl
                        {
                            pushFollow(FOLLOW_var_decl_in_flatzinc_model69);
                            var_decl3 = var_decl();

                            state._fsp--;

                            adaptor.addChild(root_0, var_decl3.getTree());

                        }
                        break;

                        default:
                            break loop3;
                    }
                } while (true);


                // parser/flatzinc/FlatzincParser.g:44:45: ( constraint )*
                loop4:
                do {
                    int alt4 = 2;
                    switch (input.LA(1)) {
                        case CONSTRAINT: {
                            alt4 = 1;
                        }
                        break;

                    }

                    switch (alt4) {
                        case 1:
                            // parser/flatzinc/FlatzincParser.g:44:46: constraint
                        {
                            pushFollow(FOLLOW_constraint_in_flatzinc_model74);
                            constraint4 = constraint();

                            state._fsp--;

                            adaptor.addChild(root_0, constraint4.getTree());

                        }
                        break;

                        default:
                            break loop4;
                    }
                } while (true);


                pushFollow(FOLLOW_solve_goal_in_flatzinc_model78);
                solve_goal5 = solve_goal();

                state._fsp--;

                adaptor.addChild(root_0, solve_goal5.getTree());

            }

            retval.stop = input.LT(-1);


            retval.tree = (Object) adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
            retval.tree = (Object) adaptor.errorNode(input, retval.start, input.LT(-1), re);

        } finally {
            // do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "flatzinc_model"


    public static class pred_decl_return extends ParserRuleReturnScope {
        Object tree;

        public Object getTree() {
            return tree;
        }
    }

    ;


    // $ANTLR start "pred_decl"
    // parser/flatzinc/FlatzincParser.g:48:1: pred_decl : PREDICATE IDENTIFIER LP pred_param ( CM pred_param )* RP SC -> ^( PREDICATE IDENTIFIER ( pred_param )+ ) ;
    public final FlatzincParser.pred_decl_return pred_decl() throws RecognitionException {
        FlatzincParser.pred_decl_return retval = new FlatzincParser.pred_decl_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token PREDICATE6 = null;
        Token IDENTIFIER7 = null;
        Token LP8 = null;
        Token CM10 = null;
        Token RP12 = null;
        Token SC13 = null;
        FlatzincParser.pred_param_return pred_param9 = null;

        FlatzincParser.pred_param_return pred_param11 = null;


        Object PREDICATE6_tree = null;
        Object IDENTIFIER7_tree = null;
        Object LP8_tree = null;
        Object CM10_tree = null;
        Object RP12_tree = null;
        Object SC13_tree = null;
        RewriteRuleTokenStream stream_SC = new RewriteRuleTokenStream(adaptor, "token SC");
        RewriteRuleTokenStream stream_RP = new RewriteRuleTokenStream(adaptor, "token RP");
        RewriteRuleTokenStream stream_PREDICATE = new RewriteRuleTokenStream(adaptor, "token PREDICATE");
        RewriteRuleTokenStream stream_IDENTIFIER = new RewriteRuleTokenStream(adaptor, "token IDENTIFIER");
        RewriteRuleTokenStream stream_CM = new RewriteRuleTokenStream(adaptor, "token CM");
        RewriteRuleTokenStream stream_LP = new RewriteRuleTokenStream(adaptor, "token LP");
        RewriteRuleSubtreeStream stream_pred_param = new RewriteRuleSubtreeStream(adaptor, "rule pred_param");
        try {
            // parser/flatzinc/FlatzincParser.g:49:2: ( PREDICATE IDENTIFIER LP pred_param ( CM pred_param )* RP SC -> ^( PREDICATE IDENTIFIER ( pred_param )+ ) )
            // parser/flatzinc/FlatzincParser.g:49:6: PREDICATE IDENTIFIER LP pred_param ( CM pred_param )* RP SC
            {
                PREDICATE6 = (Token) match(input, PREDICATE, FOLLOW_PREDICATE_in_pred_decl92);
                stream_PREDICATE.add(PREDICATE6);


                IDENTIFIER7 = (Token) match(input, IDENTIFIER, FOLLOW_IDENTIFIER_in_pred_decl94);
                stream_IDENTIFIER.add(IDENTIFIER7);


                LP8 = (Token) match(input, LP, FOLLOW_LP_in_pred_decl96);
                stream_LP.add(LP8);


                pushFollow(FOLLOW_pred_param_in_pred_decl98);
                pred_param9 = pred_param();

                state._fsp--;

                stream_pred_param.add(pred_param9.getTree());

                // parser/flatzinc/FlatzincParser.g:49:41: ( CM pred_param )*
                loop5:
                do {
                    int alt5 = 2;
                    switch (input.LA(1)) {
                        case CM: {
                            alt5 = 1;
                        }
                        break;

                    }

                    switch (alt5) {
                        case 1:
                            // parser/flatzinc/FlatzincParser.g:49:42: CM pred_param
                        {
                            CM10 = (Token) match(input, CM, FOLLOW_CM_in_pred_decl101);
                            stream_CM.add(CM10);


                            pushFollow(FOLLOW_pred_param_in_pred_decl103);
                            pred_param11 = pred_param();

                            state._fsp--;

                            stream_pred_param.add(pred_param11.getTree());

                        }
                        break;

                        default:
                            break loop5;
                    }
                } while (true);


                RP12 = (Token) match(input, RP, FOLLOW_RP_in_pred_decl107);
                stream_RP.add(RP12);


                SC13 = (Token) match(input, SC, FOLLOW_SC_in_pred_decl109);
                stream_SC.add(SC13);


                // AST REWRITE
                // elements: IDENTIFIER, pred_param, PREDICATE
                // token labels: 
                // rule labels: retval
                // token list labels: 
                // rule list labels: 
                // wildcard labels: 
                retval.tree = root_0;
                RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.tree : null);

                root_0 = (Object) adaptor.nil();
                // 50:2: -> ^( PREDICATE IDENTIFIER ( pred_param )+ )
                {
                    // parser/flatzinc/FlatzincParser.g:50:5: ^( PREDICATE IDENTIFIER ( pred_param )+ )
                    {
                        Object root_1 = (Object) adaptor.nil();
                        root_1 = (Object) adaptor.becomeRoot(
                                stream_PREDICATE.nextNode()
                                , root_1);

                        adaptor.addChild(root_1,
                                stream_IDENTIFIER.nextNode()
                        );

                        if (!(stream_pred_param.hasNext())) {
                            throw new RewriteEarlyExitException();
                        }
                        while (stream_pred_param.hasNext()) {
                            adaptor.addChild(root_1, stream_pred_param.nextTree());

                        }
                        stream_pred_param.reset();

                        adaptor.addChild(root_0, root_1);
                    }

                }


                retval.tree = root_0;

            }

            retval.stop = input.LT(-1);


            retval.tree = (Object) adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
            retval.tree = (Object) adaptor.errorNode(input, retval.start, input.LT(-1), re);

        } finally {
            // do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "pred_decl"


    public static class pred_param_return extends ParserRuleReturnScope {
        Object tree;

        public Object getTree() {
            return tree;
        }
    }

    ;


    // $ANTLR start "pred_param"
    // parser/flatzinc/FlatzincParser.g:53:1: pred_param : pred_param_type CL IDENTIFIER -> ^( CL pred_param_type IDENTIFIER ) ;
    public final FlatzincParser.pred_param_return pred_param() throws RecognitionException {
        FlatzincParser.pred_param_return retval = new FlatzincParser.pred_param_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token CL15 = null;
        Token IDENTIFIER16 = null;
        FlatzincParser.pred_param_type_return pred_param_type14 = null;


        Object CL15_tree = null;
        Object IDENTIFIER16_tree = null;
        RewriteRuleTokenStream stream_IDENTIFIER = new RewriteRuleTokenStream(adaptor, "token IDENTIFIER");
        RewriteRuleTokenStream stream_CL = new RewriteRuleTokenStream(adaptor, "token CL");
        RewriteRuleSubtreeStream stream_pred_param_type = new RewriteRuleSubtreeStream(adaptor, "rule pred_param_type");
        try {
            // parser/flatzinc/FlatzincParser.g:54:5: ( pred_param_type CL IDENTIFIER -> ^( CL pred_param_type IDENTIFIER ) )
            // parser/flatzinc/FlatzincParser.g:54:9: pred_param_type CL IDENTIFIER
            {
                pushFollow(FOLLOW_pred_param_type_in_pred_param137);
                pred_param_type14 = pred_param_type();

                state._fsp--;

                stream_pred_param_type.add(pred_param_type14.getTree());

                CL15 = (Token) match(input, CL, FOLLOW_CL_in_pred_param139);
                stream_CL.add(CL15);


                IDENTIFIER16 = (Token) match(input, IDENTIFIER, FOLLOW_IDENTIFIER_in_pred_param141);
                stream_IDENTIFIER.add(IDENTIFIER16);


                // AST REWRITE
                // elements: IDENTIFIER, CL, pred_param_type
                // token labels: 
                // rule labels: retval
                // token list labels: 
                // rule list labels: 
                // wildcard labels: 
                retval.tree = root_0;
                RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.tree : null);

                root_0 = (Object) adaptor.nil();
                // 55:5: -> ^( CL pred_param_type IDENTIFIER )
                {
                    // parser/flatzinc/FlatzincParser.g:55:9: ^( CL pred_param_type IDENTIFIER )
                    {
                        Object root_1 = (Object) adaptor.nil();
                        root_1 = (Object) adaptor.becomeRoot(
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

            retval.stop = input.LT(-1);


            retval.tree = (Object) adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
            retval.tree = (Object) adaptor.errorNode(input, retval.start, input.LT(-1), re);

        } finally {
            // do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "pred_param"


    public static class pred_param_type_return extends ParserRuleReturnScope {
        Object tree;

        public Object getTree() {
            return tree;
        }
    }

    ;


    // $ANTLR start "pred_param_type"
    // parser/flatzinc/FlatzincParser.g:58:1: pred_param_type : ( par_pred_param_type | var_pred_param_type );
    public final FlatzincParser.pred_param_type_return pred_param_type() throws RecognitionException {
        FlatzincParser.pred_param_type_return retval = new FlatzincParser.pred_param_type_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        FlatzincParser.par_pred_param_type_return par_pred_param_type17 = null;

        FlatzincParser.var_pred_param_type_return var_pred_param_type18 = null;


        try {
            // parser/flatzinc/FlatzincParser.g:59:5: ( par_pred_param_type | var_pred_param_type )
            int alt6 = 2;
            alt6 = dfa6.predict(input);
            switch (alt6) {
                case 1:
                    // parser/flatzinc/FlatzincParser.g:59:9: par_pred_param_type
                {
                    root_0 = (Object) adaptor.nil();


                    pushFollow(FOLLOW_par_pred_param_type_in_pred_param_type175);
                    par_pred_param_type17 = par_pred_param_type();

                    state._fsp--;

                    adaptor.addChild(root_0, par_pred_param_type17.getTree());

                }
                break;
                case 2:
                    // parser/flatzinc/FlatzincParser.g:60:9: var_pred_param_type
                {
                    root_0 = (Object) adaptor.nil();


                    pushFollow(FOLLOW_var_pred_param_type_in_pred_param_type185);
                    var_pred_param_type18 = var_pred_param_type();

                    state._fsp--;

                    adaptor.addChild(root_0, var_pred_param_type18.getTree());

                }
                break;

            }
            retval.stop = input.LT(-1);


            retval.tree = (Object) adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
            retval.tree = (Object) adaptor.errorNode(input, retval.start, input.LT(-1), re);

        } finally {
            // do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "pred_param_type"


    public static class par_type_return extends ParserRuleReturnScope {
        Object tree;

        public Object getTree() {
            return tree;
        }
    }

    ;


    // $ANTLR start "par_type"
    // parser/flatzinc/FlatzincParser.g:63:1: par_type : ( ARRAY LS index_set ( CM index_set )* RS OF par_type_u -> ^( ARRPAR ( index_set )+ par_type_u ) | par_type_u -> ^( APAR par_type_u ) );
    public final FlatzincParser.par_type_return par_type() throws RecognitionException {
        FlatzincParser.par_type_return retval = new FlatzincParser.par_type_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token ARRAY19 = null;
        Token LS20 = null;
        Token CM22 = null;
        Token RS24 = null;
        Token OF25 = null;
        FlatzincParser.index_set_return index_set21 = null;

        FlatzincParser.index_set_return index_set23 = null;

        FlatzincParser.par_type_u_return par_type_u26 = null;

        FlatzincParser.par_type_u_return par_type_u27 = null;


        Object ARRAY19_tree = null;
        Object LS20_tree = null;
        Object CM22_tree = null;
        Object RS24_tree = null;
        Object OF25_tree = null;
        RewriteRuleTokenStream stream_RS = new RewriteRuleTokenStream(adaptor, "token RS");
        RewriteRuleTokenStream stream_OF = new RewriteRuleTokenStream(adaptor, "token OF");
        RewriteRuleTokenStream stream_ARRAY = new RewriteRuleTokenStream(adaptor, "token ARRAY");
        RewriteRuleTokenStream stream_LS = new RewriteRuleTokenStream(adaptor, "token LS");
        RewriteRuleTokenStream stream_CM = new RewriteRuleTokenStream(adaptor, "token CM");
        RewriteRuleSubtreeStream stream_index_set = new RewriteRuleSubtreeStream(adaptor, "rule index_set");
        RewriteRuleSubtreeStream stream_par_type_u = new RewriteRuleSubtreeStream(adaptor, "rule par_type_u");
        try {
            // parser/flatzinc/FlatzincParser.g:64:5: ( ARRAY LS index_set ( CM index_set )* RS OF par_type_u -> ^( ARRPAR ( index_set )+ par_type_u ) | par_type_u -> ^( APAR par_type_u ) )
            int alt8 = 2;
            switch (input.LA(1)) {
                case ARRAY: {
                    alt8 = 1;
                }
                break;
                case BOOL:
                case FLOAT:
                case INT:
                case SET: {
                    alt8 = 2;
                }
                break;
                default:
                    NoViableAltException nvae =
                            new NoViableAltException("", 8, 0, input);

                    throw nvae;

            }

            switch (alt8) {
                case 1:
                    // parser/flatzinc/FlatzincParser.g:64:9: ARRAY LS index_set ( CM index_set )* RS OF par_type_u
                {
                    ARRAY19 = (Token) match(input, ARRAY, FOLLOW_ARRAY_in_par_type204);
                    stream_ARRAY.add(ARRAY19);


                    LS20 = (Token) match(input, LS, FOLLOW_LS_in_par_type206);
                    stream_LS.add(LS20);


                    pushFollow(FOLLOW_index_set_in_par_type208);
                    index_set21 = index_set();

                    state._fsp--;

                    stream_index_set.add(index_set21.getTree());

                    // parser/flatzinc/FlatzincParser.g:64:28: ( CM index_set )*
                    loop7:
                    do {
                        int alt7 = 2;
                        switch (input.LA(1)) {
                            case CM: {
                                alt7 = 1;
                            }
                            break;

                        }

                        switch (alt7) {
                            case 1:
                                // parser/flatzinc/FlatzincParser.g:64:29: CM index_set
                            {
                                CM22 = (Token) match(input, CM, FOLLOW_CM_in_par_type211);
                                stream_CM.add(CM22);


                                pushFollow(FOLLOW_index_set_in_par_type213);
                                index_set23 = index_set();

                                state._fsp--;

                                stream_index_set.add(index_set23.getTree());

                            }
                            break;

                            default:
                                break loop7;
                        }
                    } while (true);


                    RS24 = (Token) match(input, RS, FOLLOW_RS_in_par_type217);
                    stream_RS.add(RS24);


                    OF25 = (Token) match(input, OF, FOLLOW_OF_in_par_type219);
                    stream_OF.add(OF25);


                    pushFollow(FOLLOW_par_type_u_in_par_type221);
                    par_type_u26 = par_type_u();

                    state._fsp--;

                    stream_par_type_u.add(par_type_u26.getTree());

                    // AST REWRITE
                    // elements: index_set, par_type_u
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.tree : null);

                    root_0 = (Object) adaptor.nil();
                    // 65:5: -> ^( ARRPAR ( index_set )+ par_type_u )
                    {
                        // parser/flatzinc/FlatzincParser.g:65:9: ^( ARRPAR ( index_set )+ par_type_u )
                        {
                            Object root_1 = (Object) adaptor.nil();
                            root_1 = (Object) adaptor.becomeRoot(
                                    (Object) adaptor.create(ARRPAR, "ARRPAR")
                                    , root_1);

                            if (!(stream_index_set.hasNext())) {
                                throw new RewriteEarlyExitException();
                            }
                            while (stream_index_set.hasNext()) {
                                adaptor.addChild(root_1, stream_index_set.nextTree());

                            }
                            stream_index_set.reset();

                            adaptor.addChild(root_1, stream_par_type_u.nextTree());

                            adaptor.addChild(root_0, root_1);
                        }

                    }


                    retval.tree = root_0;

                }
                break;
                case 2:
                    // parser/flatzinc/FlatzincParser.g:66:9: par_type_u
                {
                    pushFollow(FOLLOW_par_type_u_in_par_type247);
                    par_type_u27 = par_type_u();

                    state._fsp--;

                    stream_par_type_u.add(par_type_u27.getTree());

                    // AST REWRITE
                    // elements: par_type_u
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.tree : null);

                    root_0 = (Object) adaptor.nil();
                    // 67:5: -> ^( APAR par_type_u )
                    {
                        // parser/flatzinc/FlatzincParser.g:67:9: ^( APAR par_type_u )
                        {
                            Object root_1 = (Object) adaptor.nil();
                            root_1 = (Object) adaptor.becomeRoot(
                                    (Object) adaptor.create(APAR, "APAR")
                                    , root_1);

                            adaptor.addChild(root_1, stream_par_type_u.nextTree());

                            adaptor.addChild(root_0, root_1);
                        }

                    }


                    retval.tree = root_0;

                }
                break;

            }
            retval.stop = input.LT(-1);


            retval.tree = (Object) adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
            retval.tree = (Object) adaptor.errorNode(input, retval.start, input.LT(-1), re);

        } finally {
            // do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "par_type"


    public static class par_type_u_return extends ParserRuleReturnScope {
        Object tree;

        public Object getTree() {
            return tree;
        }
    }

    ;


    // $ANTLR start "par_type_u"
    // parser/flatzinc/FlatzincParser.g:70:1: par_type_u : ( BOOL | FLOAT | SET OF INT | INT );
    public final FlatzincParser.par_type_u_return par_type_u() throws RecognitionException {
        FlatzincParser.par_type_u_return retval = new FlatzincParser.par_type_u_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token BOOL28 = null;
        Token FLOAT29 = null;
        Token SET30 = null;
        Token OF31 = null;
        Token INT32 = null;
        Token INT33 = null;

        Object BOOL28_tree = null;
        Object FLOAT29_tree = null;
        Object SET30_tree = null;
        Object OF31_tree = null;
        Object INT32_tree = null;
        Object INT33_tree = null;

        try {
            // parser/flatzinc/FlatzincParser.g:71:5: ( BOOL | FLOAT | SET OF INT | INT )
            int alt9 = 4;
            switch (input.LA(1)) {
                case BOOL: {
                    alt9 = 1;
                }
                break;
                case FLOAT: {
                    alt9 = 2;
                }
                break;
                case SET: {
                    alt9 = 3;
                }
                break;
                case INT: {
                    alt9 = 4;
                }
                break;
                default:
                    NoViableAltException nvae =
                            new NoViableAltException("", 9, 0, input);

                    throw nvae;

            }

            switch (alt9) {
                case 1:
                    // parser/flatzinc/FlatzincParser.g:71:9: BOOL
                {
                    root_0 = (Object) adaptor.nil();


                    BOOL28 = (Token) match(input, BOOL, FOLLOW_BOOL_in_par_type_u279);
                    BOOL28_tree =
                            (Object) adaptor.create(BOOL28)
                    ;
                    adaptor.addChild(root_0, BOOL28_tree);


                }
                break;
                case 2:
                    // parser/flatzinc/FlatzincParser.g:72:9: FLOAT
                {
                    root_0 = (Object) adaptor.nil();


                    FLOAT29 = (Token) match(input, FLOAT, FOLLOW_FLOAT_in_par_type_u289);
                    FLOAT29_tree =
                            (Object) adaptor.create(FLOAT29)
                    ;
                    adaptor.addChild(root_0, FLOAT29_tree);


                }
                break;
                case 3:
                    // parser/flatzinc/FlatzincParser.g:73:9: SET OF INT
                {
                    root_0 = (Object) adaptor.nil();


                    SET30 = (Token) match(input, SET, FOLLOW_SET_in_par_type_u299);
                    SET30_tree =
                            (Object) adaptor.create(SET30)
                    ;
                    adaptor.addChild(root_0, SET30_tree);


                    OF31 = (Token) match(input, OF, FOLLOW_OF_in_par_type_u301);
                    OF31_tree =
                            (Object) adaptor.create(OF31)
                    ;
                    adaptor.addChild(root_0, OF31_tree);


                    INT32 = (Token) match(input, INT, FOLLOW_INT_in_par_type_u303);
                    INT32_tree =
                            (Object) adaptor.create(INT32)
                    ;
                    adaptor.addChild(root_0, INT32_tree);


                }
                break;
                case 4:
                    // parser/flatzinc/FlatzincParser.g:74:9: INT
                {
                    root_0 = (Object) adaptor.nil();


                    INT33 = (Token) match(input, INT, FOLLOW_INT_in_par_type_u313);
                    INT33_tree =
                            (Object) adaptor.create(INT33)
                    ;
                    adaptor.addChild(root_0, INT33_tree);


                }
                break;

            }
            retval.stop = input.LT(-1);


            retval.tree = (Object) adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
            retval.tree = (Object) adaptor.errorNode(input, retval.start, input.LT(-1), re);

        } finally {
            // do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "par_type_u"


    public static class var_type_return extends ParserRuleReturnScope {
        Object tree;

        public Object getTree() {
            return tree;
        }
    }

    ;


    // $ANTLR start "var_type"
    // parser/flatzinc/FlatzincParser.g:77:1: var_type : ( ARRAY LS index_set ( CM index_set )* RS OF VAR var_type_u -> ^( ARRVAR ( index_set )+ var_type_u ) | VAR var_type_u -> ^( AVAR var_type_u ) );
    public final FlatzincParser.var_type_return var_type() throws RecognitionException {
        FlatzincParser.var_type_return retval = new FlatzincParser.var_type_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token ARRAY34 = null;
        Token LS35 = null;
        Token CM37 = null;
        Token RS39 = null;
        Token OF40 = null;
        Token VAR41 = null;
        Token VAR43 = null;
        FlatzincParser.index_set_return index_set36 = null;

        FlatzincParser.index_set_return index_set38 = null;

        FlatzincParser.var_type_u_return var_type_u42 = null;

        FlatzincParser.var_type_u_return var_type_u44 = null;


        Object ARRAY34_tree = null;
        Object LS35_tree = null;
        Object CM37_tree = null;
        Object RS39_tree = null;
        Object OF40_tree = null;
        Object VAR41_tree = null;
        Object VAR43_tree = null;
        RewriteRuleTokenStream stream_RS = new RewriteRuleTokenStream(adaptor, "token RS");
        RewriteRuleTokenStream stream_VAR = new RewriteRuleTokenStream(adaptor, "token VAR");
        RewriteRuleTokenStream stream_OF = new RewriteRuleTokenStream(adaptor, "token OF");
        RewriteRuleTokenStream stream_ARRAY = new RewriteRuleTokenStream(adaptor, "token ARRAY");
        RewriteRuleTokenStream stream_LS = new RewriteRuleTokenStream(adaptor, "token LS");
        RewriteRuleTokenStream stream_CM = new RewriteRuleTokenStream(adaptor, "token CM");
        RewriteRuleSubtreeStream stream_index_set = new RewriteRuleSubtreeStream(adaptor, "rule index_set");
        RewriteRuleSubtreeStream stream_var_type_u = new RewriteRuleSubtreeStream(adaptor, "rule var_type_u");
        try {
            // parser/flatzinc/FlatzincParser.g:78:5: ( ARRAY LS index_set ( CM index_set )* RS OF VAR var_type_u -> ^( ARRVAR ( index_set )+ var_type_u ) | VAR var_type_u -> ^( AVAR var_type_u ) )
            int alt11 = 2;
            switch (input.LA(1)) {
                case ARRAY: {
                    alt11 = 1;
                }
                break;
                case VAR: {
                    alt11 = 2;
                }
                break;
                default:
                    NoViableAltException nvae =
                            new NoViableAltException("", 11, 0, input);

                    throw nvae;

            }

            switch (alt11) {
                case 1:
                    // parser/flatzinc/FlatzincParser.g:78:9: ARRAY LS index_set ( CM index_set )* RS OF VAR var_type_u
                {
                    ARRAY34 = (Token) match(input, ARRAY, FOLLOW_ARRAY_in_var_type332);
                    stream_ARRAY.add(ARRAY34);


                    LS35 = (Token) match(input, LS, FOLLOW_LS_in_var_type334);
                    stream_LS.add(LS35);


                    pushFollow(FOLLOW_index_set_in_var_type336);
                    index_set36 = index_set();

                    state._fsp--;

                    stream_index_set.add(index_set36.getTree());

                    // parser/flatzinc/FlatzincParser.g:78:28: ( CM index_set )*
                    loop10:
                    do {
                        int alt10 = 2;
                        switch (input.LA(1)) {
                            case CM: {
                                alt10 = 1;
                            }
                            break;

                        }

                        switch (alt10) {
                            case 1:
                                // parser/flatzinc/FlatzincParser.g:78:29: CM index_set
                            {
                                CM37 = (Token) match(input, CM, FOLLOW_CM_in_var_type339);
                                stream_CM.add(CM37);


                                pushFollow(FOLLOW_index_set_in_var_type341);
                                index_set38 = index_set();

                                state._fsp--;

                                stream_index_set.add(index_set38.getTree());

                            }
                            break;

                            default:
                                break loop10;
                        }
                    } while (true);


                    RS39 = (Token) match(input, RS, FOLLOW_RS_in_var_type345);
                    stream_RS.add(RS39);


                    OF40 = (Token) match(input, OF, FOLLOW_OF_in_var_type347);
                    stream_OF.add(OF40);


                    VAR41 = (Token) match(input, VAR, FOLLOW_VAR_in_var_type349);
                    stream_VAR.add(VAR41);


                    pushFollow(FOLLOW_var_type_u_in_var_type351);
                    var_type_u42 = var_type_u();

                    state._fsp--;

                    stream_var_type_u.add(var_type_u42.getTree());

                    // AST REWRITE
                    // elements: var_type_u, index_set
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.tree : null);

                    root_0 = (Object) adaptor.nil();
                    // 79:5: -> ^( ARRVAR ( index_set )+ var_type_u )
                    {
                        // parser/flatzinc/FlatzincParser.g:79:9: ^( ARRVAR ( index_set )+ var_type_u )
                        {
                            Object root_1 = (Object) adaptor.nil();
                            root_1 = (Object) adaptor.becomeRoot(
                                    (Object) adaptor.create(ARRVAR, "ARRVAR")
                                    , root_1);

                            if (!(stream_index_set.hasNext())) {
                                throw new RewriteEarlyExitException();
                            }
                            while (stream_index_set.hasNext()) {
                                adaptor.addChild(root_1, stream_index_set.nextTree());

                            }
                            stream_index_set.reset();

                            adaptor.addChild(root_1, stream_var_type_u.nextTree());

                            adaptor.addChild(root_0, root_1);
                        }

                    }


                    retval.tree = root_0;

                }
                break;
                case 2:
                    // parser/flatzinc/FlatzincParser.g:80:9: VAR var_type_u
                {
                    VAR43 = (Token) match(input, VAR, FOLLOW_VAR_in_var_type377);
                    stream_VAR.add(VAR43);


                    pushFollow(FOLLOW_var_type_u_in_var_type379);
                    var_type_u44 = var_type_u();

                    state._fsp--;

                    stream_var_type_u.add(var_type_u44.getTree());

                    // AST REWRITE
                    // elements: var_type_u
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.tree : null);

                    root_0 = (Object) adaptor.nil();
                    // 81:5: -> ^( AVAR var_type_u )
                    {
                        // parser/flatzinc/FlatzincParser.g:81:9: ^( AVAR var_type_u )
                        {
                            Object root_1 = (Object) adaptor.nil();
                            root_1 = (Object) adaptor.becomeRoot(
                                    (Object) adaptor.create(AVAR, "AVAR")
                                    , root_1);

                            adaptor.addChild(root_1, stream_var_type_u.nextTree());

                            adaptor.addChild(root_0, root_1);
                        }

                    }


                    retval.tree = root_0;

                }
                break;

            }
            retval.stop = input.LT(-1);


            retval.tree = (Object) adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
            retval.tree = (Object) adaptor.errorNode(input, retval.start, input.LT(-1), re);

        } finally {
            // do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "var_type"


    public static class var_type_u_return extends ParserRuleReturnScope {
        Object tree;

        public Object getTree() {
            return tree;
        }
    }

    ;


    // $ANTLR start "var_type_u"
    // parser/flatzinc/FlatzincParser.g:84:1: var_type_u : ( BOOL | FLOAT | INT | INT_CONST DD INT_CONST -> ^( DD INT_CONST INT_CONST ) | LB INT_CONST ( CM INT_CONST )* RB -> ^( CM ( INT_CONST )+ ) | SET OF INT_CONST DD INT_CONST -> ^( SET ^( DD INT_CONST INT_CONST ) ) | SET OF LB INT_CONST ( CM INT_CONST )* RB -> ^( SET ^( CM INT_CONST INT_CONST ) ) );
    public final FlatzincParser.var_type_u_return var_type_u() throws RecognitionException {
        FlatzincParser.var_type_u_return retval = new FlatzincParser.var_type_u_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token BOOL45 = null;
        Token FLOAT46 = null;
        Token INT47 = null;
        Token INT_CONST48 = null;
        Token DD49 = null;
        Token INT_CONST50 = null;
        Token LB51 = null;
        Token INT_CONST52 = null;
        Token CM53 = null;
        Token INT_CONST54 = null;
        Token RB55 = null;
        Token SET56 = null;
        Token OF57 = null;
        Token INT_CONST58 = null;
        Token DD59 = null;
        Token INT_CONST60 = null;
        Token SET61 = null;
        Token OF62 = null;
        Token LB63 = null;
        Token INT_CONST64 = null;
        Token CM65 = null;
        Token INT_CONST66 = null;
        Token RB67 = null;

        Object BOOL45_tree = null;
        Object FLOAT46_tree = null;
        Object INT47_tree = null;
        Object INT_CONST48_tree = null;
        Object DD49_tree = null;
        Object INT_CONST50_tree = null;
        Object LB51_tree = null;
        Object INT_CONST52_tree = null;
        Object CM53_tree = null;
        Object INT_CONST54_tree = null;
        Object RB55_tree = null;
        Object SET56_tree = null;
        Object OF57_tree = null;
        Object INT_CONST58_tree = null;
        Object DD59_tree = null;
        Object INT_CONST60_tree = null;
        Object SET61_tree = null;
        Object OF62_tree = null;
        Object LB63_tree = null;
        Object INT_CONST64_tree = null;
        Object CM65_tree = null;
        Object INT_CONST66_tree = null;
        Object RB67_tree = null;
        RewriteRuleTokenStream stream_INT_CONST = new RewriteRuleTokenStream(adaptor, "token INT_CONST");
        RewriteRuleTokenStream stream_SET = new RewriteRuleTokenStream(adaptor, "token SET");
        RewriteRuleTokenStream stream_LB = new RewriteRuleTokenStream(adaptor, "token LB");
        RewriteRuleTokenStream stream_RB = new RewriteRuleTokenStream(adaptor, "token RB");
        RewriteRuleTokenStream stream_OF = new RewriteRuleTokenStream(adaptor, "token OF");
        RewriteRuleTokenStream stream_DD = new RewriteRuleTokenStream(adaptor, "token DD");
        RewriteRuleTokenStream stream_CM = new RewriteRuleTokenStream(adaptor, "token CM");

        try {
            // parser/flatzinc/FlatzincParser.g:85:5: ( BOOL | FLOAT | INT | INT_CONST DD INT_CONST -> ^( DD INT_CONST INT_CONST ) | LB INT_CONST ( CM INT_CONST )* RB -> ^( CM ( INT_CONST )+ ) | SET OF INT_CONST DD INT_CONST -> ^( SET ^( DD INT_CONST INT_CONST ) ) | SET OF LB INT_CONST ( CM INT_CONST )* RB -> ^( SET ^( CM INT_CONST INT_CONST ) ) )
            int alt14 = 7;
            switch (input.LA(1)) {
                case BOOL: {
                    alt14 = 1;
                }
                break;
                case FLOAT: {
                    alt14 = 2;
                }
                break;
                case INT: {
                    alt14 = 3;
                }
                break;
                case INT_CONST: {
                    alt14 = 4;
                }
                break;
                case LB: {
                    alt14 = 5;
                }
                break;
                case SET: {
                    switch (input.LA(2)) {
                        case OF: {
                            switch (input.LA(3)) {
                                case INT_CONST: {
                                    alt14 = 6;
                                }
                                break;
                                case LB: {
                                    alt14 = 7;
                                }
                                break;
                                default:
                                    NoViableAltException nvae =
                                            new NoViableAltException("", 14, 7, input);

                                    throw nvae;

                            }

                        }
                        break;
                        default:
                            NoViableAltException nvae =
                                    new NoViableAltException("", 14, 6, input);

                            throw nvae;

                    }

                }
                break;
                default:
                    NoViableAltException nvae =
                            new NoViableAltException("", 14, 0, input);

                    throw nvae;

            }

            switch (alt14) {
                case 1:
                    // parser/flatzinc/FlatzincParser.g:85:9: BOOL
                {
                    root_0 = (Object) adaptor.nil();


                    BOOL45 = (Token) match(input, BOOL, FOLLOW_BOOL_in_var_type_u411);
                    BOOL45_tree =
                            (Object) adaptor.create(BOOL45)
                    ;
                    adaptor.addChild(root_0, BOOL45_tree);


                }
                break;
                case 2:
                    // parser/flatzinc/FlatzincParser.g:86:9: FLOAT
                {
                    root_0 = (Object) adaptor.nil();


                    FLOAT46 = (Token) match(input, FLOAT, FOLLOW_FLOAT_in_var_type_u421);
                    FLOAT46_tree =
                            (Object) adaptor.create(FLOAT46)
                    ;
                    adaptor.addChild(root_0, FLOAT46_tree);


                }
                break;
                case 3:
                    // parser/flatzinc/FlatzincParser.g:87:9: INT
                {
                    root_0 = (Object) adaptor.nil();


                    INT47 = (Token) match(input, INT, FOLLOW_INT_in_var_type_u431);
                    INT47_tree =
                            (Object) adaptor.create(INT47)
                    ;
                    adaptor.addChild(root_0, INT47_tree);


                }
                break;
                case 4:
                    // parser/flatzinc/FlatzincParser.g:88:9: INT_CONST DD INT_CONST
                {
                    INT_CONST48 = (Token) match(input, INT_CONST, FOLLOW_INT_CONST_in_var_type_u441);
                    stream_INT_CONST.add(INT_CONST48);


                    DD49 = (Token) match(input, DD, FOLLOW_DD_in_var_type_u443);
                    stream_DD.add(DD49);


                    INT_CONST50 = (Token) match(input, INT_CONST, FOLLOW_INT_CONST_in_var_type_u445);
                    stream_INT_CONST.add(INT_CONST50);


                    // AST REWRITE
                    // elements: DD, INT_CONST, INT_CONST
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.tree : null);

                    root_0 = (Object) adaptor.nil();
                    // 89:5: -> ^( DD INT_CONST INT_CONST )
                    {
                        // parser/flatzinc/FlatzincParser.g:89:9: ^( DD INT_CONST INT_CONST )
                        {
                            Object root_1 = (Object) adaptor.nil();
                            root_1 = (Object) adaptor.becomeRoot(
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
                break;
                case 5:
                    // parser/flatzinc/FlatzincParser.g:92:9: LB INT_CONST ( CM INT_CONST )* RB
                {
                    LB51 = (Token) match(input, LB, FOLLOW_LB_in_var_type_u472);
                    stream_LB.add(LB51);


                    INT_CONST52 = (Token) match(input, INT_CONST, FOLLOW_INT_CONST_in_var_type_u474);
                    stream_INT_CONST.add(INT_CONST52);


                    // parser/flatzinc/FlatzincParser.g:92:22: ( CM INT_CONST )*
                    loop12:
                    do {
                        int alt12 = 2;
                        switch (input.LA(1)) {
                            case CM: {
                                alt12 = 1;
                            }
                            break;

                        }

                        switch (alt12) {
                            case 1:
                                // parser/flatzinc/FlatzincParser.g:92:23: CM INT_CONST
                            {
                                CM53 = (Token) match(input, CM, FOLLOW_CM_in_var_type_u477);
                                stream_CM.add(CM53);


                                INT_CONST54 = (Token) match(input, INT_CONST, FOLLOW_INT_CONST_in_var_type_u479);
                                stream_INT_CONST.add(INT_CONST54);


                            }
                            break;

                            default:
                                break loop12;
                        }
                    } while (true);


                    RB55 = (Token) match(input, RB, FOLLOW_RB_in_var_type_u483);
                    stream_RB.add(RB55);


                    // AST REWRITE
                    // elements: CM, INT_CONST
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.tree : null);

                    root_0 = (Object) adaptor.nil();
                    // 93:5: -> ^( CM ( INT_CONST )+ )
                    {
                        // parser/flatzinc/FlatzincParser.g:93:9: ^( CM ( INT_CONST )+ )
                        {
                            Object root_1 = (Object) adaptor.nil();
                            root_1 = (Object) adaptor.becomeRoot(
                                    stream_CM.nextNode()
                                    , root_1);

                            if (!(stream_INT_CONST.hasNext())) {
                                throw new RewriteEarlyExitException();
                            }
                            while (stream_INT_CONST.hasNext()) {
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
                break;
                case 6:
                    // parser/flatzinc/FlatzincParser.g:94:9: SET OF INT_CONST DD INT_CONST
                {
                    SET56 = (Token) match(input, SET, FOLLOW_SET_in_var_type_u507);
                    stream_SET.add(SET56);


                    OF57 = (Token) match(input, OF, FOLLOW_OF_in_var_type_u509);
                    stream_OF.add(OF57);


                    INT_CONST58 = (Token) match(input, INT_CONST, FOLLOW_INT_CONST_in_var_type_u511);
                    stream_INT_CONST.add(INT_CONST58);


                    DD59 = (Token) match(input, DD, FOLLOW_DD_in_var_type_u513);
                    stream_DD.add(DD59);


                    INT_CONST60 = (Token) match(input, INT_CONST, FOLLOW_INT_CONST_in_var_type_u515);
                    stream_INT_CONST.add(INT_CONST60);


                    // AST REWRITE
                    // elements: SET, INT_CONST, INT_CONST, DD
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.tree : null);

                    root_0 = (Object) adaptor.nil();
                    // 95:5: -> ^( SET ^( DD INT_CONST INT_CONST ) )
                    {
                        // parser/flatzinc/FlatzincParser.g:95:9: ^( SET ^( DD INT_CONST INT_CONST ) )
                        {
                            Object root_1 = (Object) adaptor.nil();
                            root_1 = (Object) adaptor.becomeRoot(
                                    stream_SET.nextNode()
                                    , root_1);

                            // parser/flatzinc/FlatzincParser.g:95:15: ^( DD INT_CONST INT_CONST )
                            {
                                Object root_2 = (Object) adaptor.nil();
                                root_2 = (Object) adaptor.becomeRoot(
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
                break;
                case 7:
                    // parser/flatzinc/FlatzincParser.g:96:9: SET OF LB INT_CONST ( CM INT_CONST )* RB
                {
                    SET61 = (Token) match(input, SET, FOLLOW_SET_in_var_type_u544);
                    stream_SET.add(SET61);


                    OF62 = (Token) match(input, OF, FOLLOW_OF_in_var_type_u546);
                    stream_OF.add(OF62);


                    LB63 = (Token) match(input, LB, FOLLOW_LB_in_var_type_u548);
                    stream_LB.add(LB63);


                    INT_CONST64 = (Token) match(input, INT_CONST, FOLLOW_INT_CONST_in_var_type_u550);
                    stream_INT_CONST.add(INT_CONST64);


                    // parser/flatzinc/FlatzincParser.g:96:29: ( CM INT_CONST )*
                    loop13:
                    do {
                        int alt13 = 2;
                        switch (input.LA(1)) {
                            case CM: {
                                alt13 = 1;
                            }
                            break;

                        }

                        switch (alt13) {
                            case 1:
                                // parser/flatzinc/FlatzincParser.g:96:30: CM INT_CONST
                            {
                                CM65 = (Token) match(input, CM, FOLLOW_CM_in_var_type_u553);
                                stream_CM.add(CM65);


                                INT_CONST66 = (Token) match(input, INT_CONST, FOLLOW_INT_CONST_in_var_type_u555);
                                stream_INT_CONST.add(INT_CONST66);


                            }
                            break;

                            default:
                                break loop13;
                        }
                    } while (true);


                    RB67 = (Token) match(input, RB, FOLLOW_RB_in_var_type_u559);
                    stream_RB.add(RB67);


                    // AST REWRITE
                    // elements: CM, INT_CONST, SET, INT_CONST
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.tree : null);

                    root_0 = (Object) adaptor.nil();
                    // 97:5: -> ^( SET ^( CM INT_CONST INT_CONST ) )
                    {
                        // parser/flatzinc/FlatzincParser.g:97:9: ^( SET ^( CM INT_CONST INT_CONST ) )
                        {
                            Object root_1 = (Object) adaptor.nil();
                            root_1 = (Object) adaptor.becomeRoot(
                                    stream_SET.nextNode()
                                    , root_1);

                            // parser/flatzinc/FlatzincParser.g:97:15: ^( CM INT_CONST INT_CONST )
                            {
                                Object root_2 = (Object) adaptor.nil();
                                root_2 = (Object) adaptor.becomeRoot(
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
                break;

            }
            retval.stop = input.LT(-1);


            retval.tree = (Object) adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
            retval.tree = (Object) adaptor.errorNode(input, retval.start, input.LT(-1), re);

        } finally {
            // do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "var_type_u"


    public static class par_pred_param_type_return extends ParserRuleReturnScope {
        Object tree;

        public Object getTree() {
            return tree;
        }
    }

    ;


    // $ANTLR start "par_pred_param_type"
    // parser/flatzinc/FlatzincParser.g:100:1: par_pred_param_type : ( par_type | INT_CONST DD INT_CONST -> ^( DD INT_CONST INT_CONST ) | LB INT_CONST ( CM INT_CONST )* RB -> ^( CM ( INT_CONST )+ ) | SET OF INT_CONST DD INT_CONST -> ^( SET ^( DD INT_CONST INT_CONST ) ) | SET OF LB INT_CONST ( CM INT_CONST )* RB -> ^( SET ^( CM ( INT_CONST )+ ) ) | ARRAY LS index_set ( CM index_set )* RS OF INT_CONST DD INT_CONST -> ^( ARRAY ( index_set )+ ^( DD INT_CONST INT_CONST ) ) | ARRAY LS index_set ( CM index_set )* RS OF LB INT_CONST ( CM INT_CONST )* RB -> ^( ARRAY ( index_set )+ ^( CM ( INT_CONST )+ ) ) | ARRAY LS index_set ( CM index_set )* RS OF SET OF INT_CONST DD INT_CONST -> ^( ARRAY ( index_set )+ ^( SET ^( DD INT_CONST INT_CONST ) ) ) | ARRAY LS index_set ( CM index_set )* RS OF SET OF LB INT_CONST ( CM INT_CONST )* RB -> ^( ARRAY ( index_set )+ ^( SET ^( CM ( INT_CONST )+ ) ) ) );
    public final FlatzincParser.par_pred_param_type_return par_pred_param_type() throws RecognitionException {
        FlatzincParser.par_pred_param_type_return retval = new FlatzincParser.par_pred_param_type_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token INT_CONST69 = null;
        Token DD70 = null;
        Token INT_CONST71 = null;
        Token LB72 = null;
        Token INT_CONST73 = null;
        Token CM74 = null;
        Token INT_CONST75 = null;
        Token RB76 = null;
        Token SET77 = null;
        Token OF78 = null;
        Token INT_CONST79 = null;
        Token DD80 = null;
        Token INT_CONST81 = null;
        Token SET82 = null;
        Token OF83 = null;
        Token LB84 = null;
        Token INT_CONST85 = null;
        Token CM86 = null;
        Token INT_CONST87 = null;
        Token RB88 = null;
        Token ARRAY89 = null;
        Token LS90 = null;
        Token CM92 = null;
        Token RS94 = null;
        Token OF95 = null;
        Token INT_CONST96 = null;
        Token DD97 = null;
        Token INT_CONST98 = null;
        Token ARRAY99 = null;
        Token LS100 = null;
        Token CM102 = null;
        Token RS104 = null;
        Token OF105 = null;
        Token LB106 = null;
        Token INT_CONST107 = null;
        Token CM108 = null;
        Token INT_CONST109 = null;
        Token RB110 = null;
        Token ARRAY111 = null;
        Token LS112 = null;
        Token CM114 = null;
        Token RS116 = null;
        Token OF117 = null;
        Token SET118 = null;
        Token OF119 = null;
        Token INT_CONST120 = null;
        Token DD121 = null;
        Token INT_CONST122 = null;
        Token ARRAY123 = null;
        Token LS124 = null;
        Token CM126 = null;
        Token RS128 = null;
        Token OF129 = null;
        Token SET130 = null;
        Token OF131 = null;
        Token LB132 = null;
        Token INT_CONST133 = null;
        Token CM134 = null;
        Token INT_CONST135 = null;
        Token RB136 = null;
        FlatzincParser.par_type_return par_type68 = null;

        FlatzincParser.index_set_return index_set91 = null;

        FlatzincParser.index_set_return index_set93 = null;

        FlatzincParser.index_set_return index_set101 = null;

        FlatzincParser.index_set_return index_set103 = null;

        FlatzincParser.index_set_return index_set113 = null;

        FlatzincParser.index_set_return index_set115 = null;

        FlatzincParser.index_set_return index_set125 = null;

        FlatzincParser.index_set_return index_set127 = null;


        Object INT_CONST69_tree = null;
        Object DD70_tree = null;
        Object INT_CONST71_tree = null;
        Object LB72_tree = null;
        Object INT_CONST73_tree = null;
        Object CM74_tree = null;
        Object INT_CONST75_tree = null;
        Object RB76_tree = null;
        Object SET77_tree = null;
        Object OF78_tree = null;
        Object INT_CONST79_tree = null;
        Object DD80_tree = null;
        Object INT_CONST81_tree = null;
        Object SET82_tree = null;
        Object OF83_tree = null;
        Object LB84_tree = null;
        Object INT_CONST85_tree = null;
        Object CM86_tree = null;
        Object INT_CONST87_tree = null;
        Object RB88_tree = null;
        Object ARRAY89_tree = null;
        Object LS90_tree = null;
        Object CM92_tree = null;
        Object RS94_tree = null;
        Object OF95_tree = null;
        Object INT_CONST96_tree = null;
        Object DD97_tree = null;
        Object INT_CONST98_tree = null;
        Object ARRAY99_tree = null;
        Object LS100_tree = null;
        Object CM102_tree = null;
        Object RS104_tree = null;
        Object OF105_tree = null;
        Object LB106_tree = null;
        Object INT_CONST107_tree = null;
        Object CM108_tree = null;
        Object INT_CONST109_tree = null;
        Object RB110_tree = null;
        Object ARRAY111_tree = null;
        Object LS112_tree = null;
        Object CM114_tree = null;
        Object RS116_tree = null;
        Object OF117_tree = null;
        Object SET118_tree = null;
        Object OF119_tree = null;
        Object INT_CONST120_tree = null;
        Object DD121_tree = null;
        Object INT_CONST122_tree = null;
        Object ARRAY123_tree = null;
        Object LS124_tree = null;
        Object CM126_tree = null;
        Object RS128_tree = null;
        Object OF129_tree = null;
        Object SET130_tree = null;
        Object OF131_tree = null;
        Object LB132_tree = null;
        Object INT_CONST133_tree = null;
        Object CM134_tree = null;
        Object INT_CONST135_tree = null;
        Object RB136_tree = null;
        RewriteRuleTokenStream stream_RS = new RewriteRuleTokenStream(adaptor, "token RS");
        RewriteRuleTokenStream stream_INT_CONST = new RewriteRuleTokenStream(adaptor, "token INT_CONST");
        RewriteRuleTokenStream stream_SET = new RewriteRuleTokenStream(adaptor, "token SET");
        RewriteRuleTokenStream stream_LB = new RewriteRuleTokenStream(adaptor, "token LB");
        RewriteRuleTokenStream stream_RB = new RewriteRuleTokenStream(adaptor, "token RB");
        RewriteRuleTokenStream stream_OF = new RewriteRuleTokenStream(adaptor, "token OF");
        RewriteRuleTokenStream stream_DD = new RewriteRuleTokenStream(adaptor, "token DD");
        RewriteRuleTokenStream stream_ARRAY = new RewriteRuleTokenStream(adaptor, "token ARRAY");
        RewriteRuleTokenStream stream_LS = new RewriteRuleTokenStream(adaptor, "token LS");
        RewriteRuleTokenStream stream_CM = new RewriteRuleTokenStream(adaptor, "token CM");
        RewriteRuleSubtreeStream stream_index_set = new RewriteRuleSubtreeStream(adaptor, "rule index_set");
        try {
            // parser/flatzinc/FlatzincParser.g:101:5: ( par_type | INT_CONST DD INT_CONST -> ^( DD INT_CONST INT_CONST ) | LB INT_CONST ( CM INT_CONST )* RB -> ^( CM ( INT_CONST )+ ) | SET OF INT_CONST DD INT_CONST -> ^( SET ^( DD INT_CONST INT_CONST ) ) | SET OF LB INT_CONST ( CM INT_CONST )* RB -> ^( SET ^( CM ( INT_CONST )+ ) ) | ARRAY LS index_set ( CM index_set )* RS OF INT_CONST DD INT_CONST -> ^( ARRAY ( index_set )+ ^( DD INT_CONST INT_CONST ) ) | ARRAY LS index_set ( CM index_set )* RS OF LB INT_CONST ( CM INT_CONST )* RB -> ^( ARRAY ( index_set )+ ^( CM ( INT_CONST )+ ) ) | ARRAY LS index_set ( CM index_set )* RS OF SET OF INT_CONST DD INT_CONST -> ^( ARRAY ( index_set )+ ^( SET ^( DD INT_CONST INT_CONST ) ) ) | ARRAY LS index_set ( CM index_set )* RS OF SET OF LB INT_CONST ( CM INT_CONST )* RB -> ^( ARRAY ( index_set )+ ^( SET ^( CM ( INT_CONST )+ ) ) ) )
            int alt23 = 9;
            alt23 = dfa23.predict(input);
            switch (alt23) {
                case 1:
                    // parser/flatzinc/FlatzincParser.g:101:9: par_type
                {
                    root_0 = (Object) adaptor.nil();


                    pushFollow(FOLLOW_par_type_in_par_pred_param_type597);
                    par_type68 = par_type();

                    state._fsp--;

                    adaptor.addChild(root_0, par_type68.getTree());

                }
                break;
                case 2:
                    // parser/flatzinc/FlatzincParser.g:104:9: INT_CONST DD INT_CONST
                {
                    INT_CONST69 = (Token) match(input, INT_CONST, FOLLOW_INT_CONST_in_par_pred_param_type609);
                    stream_INT_CONST.add(INT_CONST69);


                    DD70 = (Token) match(input, DD, FOLLOW_DD_in_par_pred_param_type611);
                    stream_DD.add(DD70);


                    INT_CONST71 = (Token) match(input, INT_CONST, FOLLOW_INT_CONST_in_par_pred_param_type613);
                    stream_INT_CONST.add(INT_CONST71);


                    // AST REWRITE
                    // elements: DD, INT_CONST, INT_CONST
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.tree : null);

                    root_0 = (Object) adaptor.nil();
                    // 105:5: -> ^( DD INT_CONST INT_CONST )
                    {
                        // parser/flatzinc/FlatzincParser.g:105:9: ^( DD INT_CONST INT_CONST )
                        {
                            Object root_1 = (Object) adaptor.nil();
                            root_1 = (Object) adaptor.becomeRoot(
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
                break;
                case 3:
                    // parser/flatzinc/FlatzincParser.g:106:9: LB INT_CONST ( CM INT_CONST )* RB
                {
                    LB72 = (Token) match(input, LB, FOLLOW_LB_in_par_pred_param_type638);
                    stream_LB.add(LB72);


                    INT_CONST73 = (Token) match(input, INT_CONST, FOLLOW_INT_CONST_in_par_pred_param_type640);
                    stream_INT_CONST.add(INT_CONST73);


                    // parser/flatzinc/FlatzincParser.g:106:22: ( CM INT_CONST )*
                    loop15:
                    do {
                        int alt15 = 2;
                        switch (input.LA(1)) {
                            case CM: {
                                alt15 = 1;
                            }
                            break;

                        }

                        switch (alt15) {
                            case 1:
                                // parser/flatzinc/FlatzincParser.g:106:23: CM INT_CONST
                            {
                                CM74 = (Token) match(input, CM, FOLLOW_CM_in_par_pred_param_type643);
                                stream_CM.add(CM74);


                                INT_CONST75 = (Token) match(input, INT_CONST, FOLLOW_INT_CONST_in_par_pred_param_type645);
                                stream_INT_CONST.add(INT_CONST75);


                            }
                            break;

                            default:
                                break loop15;
                        }
                    } while (true);


                    RB76 = (Token) match(input, RB, FOLLOW_RB_in_par_pred_param_type649);
                    stream_RB.add(RB76);


                    // AST REWRITE
                    // elements: INT_CONST, CM
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.tree : null);

                    root_0 = (Object) adaptor.nil();
                    // 107:5: -> ^( CM ( INT_CONST )+ )
                    {
                        // parser/flatzinc/FlatzincParser.g:107:9: ^( CM ( INT_CONST )+ )
                        {
                            Object root_1 = (Object) adaptor.nil();
                            root_1 = (Object) adaptor.becomeRoot(
                                    stream_CM.nextNode()
                                    , root_1);

                            if (!(stream_INT_CONST.hasNext())) {
                                throw new RewriteEarlyExitException();
                            }
                            while (stream_INT_CONST.hasNext()) {
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
                break;
                case 4:
                    // parser/flatzinc/FlatzincParser.g:108:9: SET OF INT_CONST DD INT_CONST
                {
                    SET77 = (Token) match(input, SET, FOLLOW_SET_in_par_pred_param_type673);
                    stream_SET.add(SET77);


                    OF78 = (Token) match(input, OF, FOLLOW_OF_in_par_pred_param_type675);
                    stream_OF.add(OF78);


                    INT_CONST79 = (Token) match(input, INT_CONST, FOLLOW_INT_CONST_in_par_pred_param_type677);
                    stream_INT_CONST.add(INT_CONST79);


                    DD80 = (Token) match(input, DD, FOLLOW_DD_in_par_pred_param_type679);
                    stream_DD.add(DD80);


                    INT_CONST81 = (Token) match(input, INT_CONST, FOLLOW_INT_CONST_in_par_pred_param_type681);
                    stream_INT_CONST.add(INT_CONST81);


                    // AST REWRITE
                    // elements: DD, INT_CONST, INT_CONST, SET
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.tree : null);

                    root_0 = (Object) adaptor.nil();
                    // 109:5: -> ^( SET ^( DD INT_CONST INT_CONST ) )
                    {
                        // parser/flatzinc/FlatzincParser.g:109:9: ^( SET ^( DD INT_CONST INT_CONST ) )
                        {
                            Object root_1 = (Object) adaptor.nil();
                            root_1 = (Object) adaptor.becomeRoot(
                                    stream_SET.nextNode()
                                    , root_1);

                            // parser/flatzinc/FlatzincParser.g:109:15: ^( DD INT_CONST INT_CONST )
                            {
                                Object root_2 = (Object) adaptor.nil();
                                root_2 = (Object) adaptor.becomeRoot(
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
                break;
                case 5:
                    // parser/flatzinc/FlatzincParser.g:110:9: SET OF LB INT_CONST ( CM INT_CONST )* RB
                {
                    SET82 = (Token) match(input, SET, FOLLOW_SET_in_par_pred_param_type710);
                    stream_SET.add(SET82);


                    OF83 = (Token) match(input, OF, FOLLOW_OF_in_par_pred_param_type712);
                    stream_OF.add(OF83);


                    LB84 = (Token) match(input, LB, FOLLOW_LB_in_par_pred_param_type714);
                    stream_LB.add(LB84);


                    INT_CONST85 = (Token) match(input, INT_CONST, FOLLOW_INT_CONST_in_par_pred_param_type716);
                    stream_INT_CONST.add(INT_CONST85);


                    // parser/flatzinc/FlatzincParser.g:110:29: ( CM INT_CONST )*
                    loop16:
                    do {
                        int alt16 = 2;
                        switch (input.LA(1)) {
                            case CM: {
                                alt16 = 1;
                            }
                            break;

                        }

                        switch (alt16) {
                            case 1:
                                // parser/flatzinc/FlatzincParser.g:110:30: CM INT_CONST
                            {
                                CM86 = (Token) match(input, CM, FOLLOW_CM_in_par_pred_param_type719);
                                stream_CM.add(CM86);


                                INT_CONST87 = (Token) match(input, INT_CONST, FOLLOW_INT_CONST_in_par_pred_param_type721);
                                stream_INT_CONST.add(INT_CONST87);


                            }
                            break;

                            default:
                                break loop16;
                        }
                    } while (true);


                    RB88 = (Token) match(input, RB, FOLLOW_RB_in_par_pred_param_type725);
                    stream_RB.add(RB88);


                    // AST REWRITE
                    // elements: SET, INT_CONST, CM
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.tree : null);

                    root_0 = (Object) adaptor.nil();
                    // 111:5: -> ^( SET ^( CM ( INT_CONST )+ ) )
                    {
                        // parser/flatzinc/FlatzincParser.g:111:9: ^( SET ^( CM ( INT_CONST )+ ) )
                        {
                            Object root_1 = (Object) adaptor.nil();
                            root_1 = (Object) adaptor.becomeRoot(
                                    stream_SET.nextNode()
                                    , root_1);

                            // parser/flatzinc/FlatzincParser.g:111:15: ^( CM ( INT_CONST )+ )
                            {
                                Object root_2 = (Object) adaptor.nil();
                                root_2 = (Object) adaptor.becomeRoot(
                                        stream_CM.nextNode()
                                        , root_2);

                                if (!(stream_INT_CONST.hasNext())) {
                                    throw new RewriteEarlyExitException();
                                }
                                while (stream_INT_CONST.hasNext()) {
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
                break;
                case 6:
                    // parser/flatzinc/FlatzincParser.g:114:9: ARRAY LS index_set ( CM index_set )* RS OF INT_CONST DD INT_CONST
                {
                    ARRAY89 = (Token) match(input, ARRAY, FOLLOW_ARRAY_in_par_pred_param_type755);
                    stream_ARRAY.add(ARRAY89);


                    LS90 = (Token) match(input, LS, FOLLOW_LS_in_par_pred_param_type757);
                    stream_LS.add(LS90);


                    pushFollow(FOLLOW_index_set_in_par_pred_param_type759);
                    index_set91 = index_set();

                    state._fsp--;

                    stream_index_set.add(index_set91.getTree());

                    // parser/flatzinc/FlatzincParser.g:114:28: ( CM index_set )*
                    loop17:
                    do {
                        int alt17 = 2;
                        switch (input.LA(1)) {
                            case CM: {
                                alt17 = 1;
                            }
                            break;

                        }

                        switch (alt17) {
                            case 1:
                                // parser/flatzinc/FlatzincParser.g:114:29: CM index_set
                            {
                                CM92 = (Token) match(input, CM, FOLLOW_CM_in_par_pred_param_type762);
                                stream_CM.add(CM92);


                                pushFollow(FOLLOW_index_set_in_par_pred_param_type764);
                                index_set93 = index_set();

                                state._fsp--;

                                stream_index_set.add(index_set93.getTree());

                            }
                            break;

                            default:
                                break loop17;
                        }
                    } while (true);


                    RS94 = (Token) match(input, RS, FOLLOW_RS_in_par_pred_param_type768);
                    stream_RS.add(RS94);


                    OF95 = (Token) match(input, OF, FOLLOW_OF_in_par_pred_param_type770);
                    stream_OF.add(OF95);


                    INT_CONST96 = (Token) match(input, INT_CONST, FOLLOW_INT_CONST_in_par_pred_param_type772);
                    stream_INT_CONST.add(INT_CONST96);


                    DD97 = (Token) match(input, DD, FOLLOW_DD_in_par_pred_param_type774);
                    stream_DD.add(DD97);


                    INT_CONST98 = (Token) match(input, INT_CONST, FOLLOW_INT_CONST_in_par_pred_param_type776);
                    stream_INT_CONST.add(INT_CONST98);


                    // AST REWRITE
                    // elements: ARRAY, index_set, DD, INT_CONST, INT_CONST
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.tree : null);

                    root_0 = (Object) adaptor.nil();
                    // 115:5: -> ^( ARRAY ( index_set )+ ^( DD INT_CONST INT_CONST ) )
                    {
                        // parser/flatzinc/FlatzincParser.g:115:9: ^( ARRAY ( index_set )+ ^( DD INT_CONST INT_CONST ) )
                        {
                            Object root_1 = (Object) adaptor.nil();
                            root_1 = (Object) adaptor.becomeRoot(
                                    stream_ARRAY.nextNode()
                                    , root_1);

                            if (!(stream_index_set.hasNext())) {
                                throw new RewriteEarlyExitException();
                            }
                            while (stream_index_set.hasNext()) {
                                adaptor.addChild(root_1, stream_index_set.nextTree());

                            }
                            stream_index_set.reset();

                            // parser/flatzinc/FlatzincParser.g:115:28: ^( DD INT_CONST INT_CONST )
                            {
                                Object root_2 = (Object) adaptor.nil();
                                root_2 = (Object) adaptor.becomeRoot(
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
                break;
                case 7:
                    // parser/flatzinc/FlatzincParser.g:116:9: ARRAY LS index_set ( CM index_set )* RS OF LB INT_CONST ( CM INT_CONST )* RB
                {
                    ARRAY99 = (Token) match(input, ARRAY, FOLLOW_ARRAY_in_par_pred_param_type808);
                    stream_ARRAY.add(ARRAY99);


                    LS100 = (Token) match(input, LS, FOLLOW_LS_in_par_pred_param_type810);
                    stream_LS.add(LS100);


                    pushFollow(FOLLOW_index_set_in_par_pred_param_type812);
                    index_set101 = index_set();

                    state._fsp--;

                    stream_index_set.add(index_set101.getTree());

                    // parser/flatzinc/FlatzincParser.g:116:28: ( CM index_set )*
                    loop18:
                    do {
                        int alt18 = 2;
                        switch (input.LA(1)) {
                            case CM: {
                                alt18 = 1;
                            }
                            break;

                        }

                        switch (alt18) {
                            case 1:
                                // parser/flatzinc/FlatzincParser.g:116:29: CM index_set
                            {
                                CM102 = (Token) match(input, CM, FOLLOW_CM_in_par_pred_param_type815);
                                stream_CM.add(CM102);


                                pushFollow(FOLLOW_index_set_in_par_pred_param_type817);
                                index_set103 = index_set();

                                state._fsp--;

                                stream_index_set.add(index_set103.getTree());

                            }
                            break;

                            default:
                                break loop18;
                        }
                    } while (true);


                    RS104 = (Token) match(input, RS, FOLLOW_RS_in_par_pred_param_type821);
                    stream_RS.add(RS104);


                    OF105 = (Token) match(input, OF, FOLLOW_OF_in_par_pred_param_type823);
                    stream_OF.add(OF105);


                    LB106 = (Token) match(input, LB, FOLLOW_LB_in_par_pred_param_type825);
                    stream_LB.add(LB106);


                    INT_CONST107 = (Token) match(input, INT_CONST, FOLLOW_INT_CONST_in_par_pred_param_type827);
                    stream_INT_CONST.add(INT_CONST107);


                    // parser/flatzinc/FlatzincParser.g:116:63: ( CM INT_CONST )*
                    loop19:
                    do {
                        int alt19 = 2;
                        switch (input.LA(1)) {
                            case CM: {
                                alt19 = 1;
                            }
                            break;

                        }

                        switch (alt19) {
                            case 1:
                                // parser/flatzinc/FlatzincParser.g:116:64: CM INT_CONST
                            {
                                CM108 = (Token) match(input, CM, FOLLOW_CM_in_par_pred_param_type830);
                                stream_CM.add(CM108);


                                INT_CONST109 = (Token) match(input, INT_CONST, FOLLOW_INT_CONST_in_par_pred_param_type832);
                                stream_INT_CONST.add(INT_CONST109);


                            }
                            break;

                            default:
                                break loop19;
                        }
                    } while (true);


                    RB110 = (Token) match(input, RB, FOLLOW_RB_in_par_pred_param_type836);
                    stream_RB.add(RB110);


                    // AST REWRITE
                    // elements: ARRAY, index_set, INT_CONST, CM
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.tree : null);

                    root_0 = (Object) adaptor.nil();
                    // 117:5: -> ^( ARRAY ( index_set )+ ^( CM ( INT_CONST )+ ) )
                    {
                        // parser/flatzinc/FlatzincParser.g:117:9: ^( ARRAY ( index_set )+ ^( CM ( INT_CONST )+ ) )
                        {
                            Object root_1 = (Object) adaptor.nil();
                            root_1 = (Object) adaptor.becomeRoot(
                                    stream_ARRAY.nextNode()
                                    , root_1);

                            if (!(stream_index_set.hasNext())) {
                                throw new RewriteEarlyExitException();
                            }
                            while (stream_index_set.hasNext()) {
                                adaptor.addChild(root_1, stream_index_set.nextTree());

                            }
                            stream_index_set.reset();

                            // parser/flatzinc/FlatzincParser.g:117:28: ^( CM ( INT_CONST )+ )
                            {
                                Object root_2 = (Object) adaptor.nil();
                                root_2 = (Object) adaptor.becomeRoot(
                                        stream_CM.nextNode()
                                        , root_2);

                                if (!(stream_INT_CONST.hasNext())) {
                                    throw new RewriteEarlyExitException();
                                }
                                while (stream_INT_CONST.hasNext()) {
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
                break;
                case 8:
                    // parser/flatzinc/FlatzincParser.g:118:9: ARRAY LS index_set ( CM index_set )* RS OF SET OF INT_CONST DD INT_CONST
                {
                    ARRAY111 = (Token) match(input, ARRAY, FOLLOW_ARRAY_in_par_pred_param_type867);
                    stream_ARRAY.add(ARRAY111);


                    LS112 = (Token) match(input, LS, FOLLOW_LS_in_par_pred_param_type869);
                    stream_LS.add(LS112);


                    pushFollow(FOLLOW_index_set_in_par_pred_param_type871);
                    index_set113 = index_set();

                    state._fsp--;

                    stream_index_set.add(index_set113.getTree());

                    // parser/flatzinc/FlatzincParser.g:118:28: ( CM index_set )*
                    loop20:
                    do {
                        int alt20 = 2;
                        switch (input.LA(1)) {
                            case CM: {
                                alt20 = 1;
                            }
                            break;

                        }

                        switch (alt20) {
                            case 1:
                                // parser/flatzinc/FlatzincParser.g:118:29: CM index_set
                            {
                                CM114 = (Token) match(input, CM, FOLLOW_CM_in_par_pred_param_type874);
                                stream_CM.add(CM114);


                                pushFollow(FOLLOW_index_set_in_par_pred_param_type876);
                                index_set115 = index_set();

                                state._fsp--;

                                stream_index_set.add(index_set115.getTree());

                            }
                            break;

                            default:
                                break loop20;
                        }
                    } while (true);


                    RS116 = (Token) match(input, RS, FOLLOW_RS_in_par_pred_param_type880);
                    stream_RS.add(RS116);


                    OF117 = (Token) match(input, OF, FOLLOW_OF_in_par_pred_param_type882);
                    stream_OF.add(OF117);


                    SET118 = (Token) match(input, SET, FOLLOW_SET_in_par_pred_param_type884);
                    stream_SET.add(SET118);


                    OF119 = (Token) match(input, OF, FOLLOW_OF_in_par_pred_param_type886);
                    stream_OF.add(OF119);


                    INT_CONST120 = (Token) match(input, INT_CONST, FOLLOW_INT_CONST_in_par_pred_param_type888);
                    stream_INT_CONST.add(INT_CONST120);


                    DD121 = (Token) match(input, DD, FOLLOW_DD_in_par_pred_param_type890);
                    stream_DD.add(DD121);


                    INT_CONST122 = (Token) match(input, INT_CONST, FOLLOW_INT_CONST_in_par_pred_param_type892);
                    stream_INT_CONST.add(INT_CONST122);


                    // AST REWRITE
                    // elements: DD, ARRAY, INT_CONST, INT_CONST, index_set, SET
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.tree : null);

                    root_0 = (Object) adaptor.nil();
                    // 119:5: -> ^( ARRAY ( index_set )+ ^( SET ^( DD INT_CONST INT_CONST ) ) )
                    {
                        // parser/flatzinc/FlatzincParser.g:119:9: ^( ARRAY ( index_set )+ ^( SET ^( DD INT_CONST INT_CONST ) ) )
                        {
                            Object root_1 = (Object) adaptor.nil();
                            root_1 = (Object) adaptor.becomeRoot(
                                    stream_ARRAY.nextNode()
                                    , root_1);

                            if (!(stream_index_set.hasNext())) {
                                throw new RewriteEarlyExitException();
                            }
                            while (stream_index_set.hasNext()) {
                                adaptor.addChild(root_1, stream_index_set.nextTree());

                            }
                            stream_index_set.reset();

                            // parser/flatzinc/FlatzincParser.g:119:28: ^( SET ^( DD INT_CONST INT_CONST ) )
                            {
                                Object root_2 = (Object) adaptor.nil();
                                root_2 = (Object) adaptor.becomeRoot(
                                        stream_SET.nextNode()
                                        , root_2);

                                // parser/flatzinc/FlatzincParser.g:119:34: ^( DD INT_CONST INT_CONST )
                                {
                                    Object root_3 = (Object) adaptor.nil();
                                    root_3 = (Object) adaptor.becomeRoot(
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
                break;
                case 9:
                    // parser/flatzinc/FlatzincParser.g:120:9: ARRAY LS index_set ( CM index_set )* RS OF SET OF LB INT_CONST ( CM INT_CONST )* RB
                {
                    ARRAY123 = (Token) match(input, ARRAY, FOLLOW_ARRAY_in_par_pred_param_type928);
                    stream_ARRAY.add(ARRAY123);


                    LS124 = (Token) match(input, LS, FOLLOW_LS_in_par_pred_param_type930);
                    stream_LS.add(LS124);


                    pushFollow(FOLLOW_index_set_in_par_pred_param_type932);
                    index_set125 = index_set();

                    state._fsp--;

                    stream_index_set.add(index_set125.getTree());

                    // parser/flatzinc/FlatzincParser.g:120:28: ( CM index_set )*
                    loop21:
                    do {
                        int alt21 = 2;
                        switch (input.LA(1)) {
                            case CM: {
                                alt21 = 1;
                            }
                            break;

                        }

                        switch (alt21) {
                            case 1:
                                // parser/flatzinc/FlatzincParser.g:120:29: CM index_set
                            {
                                CM126 = (Token) match(input, CM, FOLLOW_CM_in_par_pred_param_type935);
                                stream_CM.add(CM126);


                                pushFollow(FOLLOW_index_set_in_par_pred_param_type937);
                                index_set127 = index_set();

                                state._fsp--;

                                stream_index_set.add(index_set127.getTree());

                            }
                            break;

                            default:
                                break loop21;
                        }
                    } while (true);


                    RS128 = (Token) match(input, RS, FOLLOW_RS_in_par_pred_param_type941);
                    stream_RS.add(RS128);


                    OF129 = (Token) match(input, OF, FOLLOW_OF_in_par_pred_param_type943);
                    stream_OF.add(OF129);


                    SET130 = (Token) match(input, SET, FOLLOW_SET_in_par_pred_param_type945);
                    stream_SET.add(SET130);


                    OF131 = (Token) match(input, OF, FOLLOW_OF_in_par_pred_param_type947);
                    stream_OF.add(OF131);


                    LB132 = (Token) match(input, LB, FOLLOW_LB_in_par_pred_param_type949);
                    stream_LB.add(LB132);


                    INT_CONST133 = (Token) match(input, INT_CONST, FOLLOW_INT_CONST_in_par_pred_param_type951);
                    stream_INT_CONST.add(INT_CONST133);


                    // parser/flatzinc/FlatzincParser.g:120:70: ( CM INT_CONST )*
                    loop22:
                    do {
                        int alt22 = 2;
                        switch (input.LA(1)) {
                            case CM: {
                                alt22 = 1;
                            }
                            break;

                        }

                        switch (alt22) {
                            case 1:
                                // parser/flatzinc/FlatzincParser.g:120:71: CM INT_CONST
                            {
                                CM134 = (Token) match(input, CM, FOLLOW_CM_in_par_pred_param_type954);
                                stream_CM.add(CM134);


                                INT_CONST135 = (Token) match(input, INT_CONST, FOLLOW_INT_CONST_in_par_pred_param_type956);
                                stream_INT_CONST.add(INT_CONST135);


                            }
                            break;

                            default:
                                break loop22;
                        }
                    } while (true);


                    RB136 = (Token) match(input, RB, FOLLOW_RB_in_par_pred_param_type960);
                    stream_RB.add(RB136);


                    // AST REWRITE
                    // elements: SET, INT_CONST, index_set, CM, ARRAY
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.tree : null);

                    root_0 = (Object) adaptor.nil();
                    // 121:5: -> ^( ARRAY ( index_set )+ ^( SET ^( CM ( INT_CONST )+ ) ) )
                    {
                        // parser/flatzinc/FlatzincParser.g:121:9: ^( ARRAY ( index_set )+ ^( SET ^( CM ( INT_CONST )+ ) ) )
                        {
                            Object root_1 = (Object) adaptor.nil();
                            root_1 = (Object) adaptor.becomeRoot(
                                    stream_ARRAY.nextNode()
                                    , root_1);

                            if (!(stream_index_set.hasNext())) {
                                throw new RewriteEarlyExitException();
                            }
                            while (stream_index_set.hasNext()) {
                                adaptor.addChild(root_1, stream_index_set.nextTree());

                            }
                            stream_index_set.reset();

                            // parser/flatzinc/FlatzincParser.g:121:28: ^( SET ^( CM ( INT_CONST )+ ) )
                            {
                                Object root_2 = (Object) adaptor.nil();
                                root_2 = (Object) adaptor.becomeRoot(
                                        stream_SET.nextNode()
                                        , root_2);

                                // parser/flatzinc/FlatzincParser.g:121:34: ^( CM ( INT_CONST )+ )
                                {
                                    Object root_3 = (Object) adaptor.nil();
                                    root_3 = (Object) adaptor.becomeRoot(
                                            stream_CM.nextNode()
                                            , root_3);

                                    if (!(stream_INT_CONST.hasNext())) {
                                        throw new RewriteEarlyExitException();
                                    }
                                    while (stream_INT_CONST.hasNext()) {
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
                break;

            }
            retval.stop = input.LT(-1);


            retval.tree = (Object) adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
            retval.tree = (Object) adaptor.errorNode(input, retval.start, input.LT(-1), re);

        } finally {
            // do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "par_pred_param_type"


    public static class var_pred_param_type_return extends ParserRuleReturnScope {
        Object tree;

        public Object getTree() {
            return tree;
        }
    }

    ;


    // $ANTLR start "var_pred_param_type"
    // parser/flatzinc/FlatzincParser.g:125:1: var_pred_param_type : ( var_type -> ^( VAR var_type ) | VAR SET OF INT -> ^( VAR SET ) | ARRAY LS index_set ( CM index_set )* RS OF VAR SET OF INT -> ^( ARRAY ( index_set )+ ^( VAR SET ) ) );
    public final FlatzincParser.var_pred_param_type_return var_pred_param_type() throws RecognitionException {
        FlatzincParser.var_pred_param_type_return retval = new FlatzincParser.var_pred_param_type_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token VAR138 = null;
        Token SET139 = null;
        Token OF140 = null;
        Token INT141 = null;
        Token ARRAY142 = null;
        Token LS143 = null;
        Token CM145 = null;
        Token RS147 = null;
        Token OF148 = null;
        Token VAR149 = null;
        Token SET150 = null;
        Token OF151 = null;
        Token INT152 = null;
        FlatzincParser.var_type_return var_type137 = null;

        FlatzincParser.index_set_return index_set144 = null;

        FlatzincParser.index_set_return index_set146 = null;


        Object VAR138_tree = null;
        Object SET139_tree = null;
        Object OF140_tree = null;
        Object INT141_tree = null;
        Object ARRAY142_tree = null;
        Object LS143_tree = null;
        Object CM145_tree = null;
        Object RS147_tree = null;
        Object OF148_tree = null;
        Object VAR149_tree = null;
        Object SET150_tree = null;
        Object OF151_tree = null;
        Object INT152_tree = null;
        RewriteRuleTokenStream stream_RS = new RewriteRuleTokenStream(adaptor, "token RS");
        RewriteRuleTokenStream stream_SET = new RewriteRuleTokenStream(adaptor, "token SET");
        RewriteRuleTokenStream stream_VAR = new RewriteRuleTokenStream(adaptor, "token VAR");
        RewriteRuleTokenStream stream_INT = new RewriteRuleTokenStream(adaptor, "token INT");
        RewriteRuleTokenStream stream_OF = new RewriteRuleTokenStream(adaptor, "token OF");
        RewriteRuleTokenStream stream_ARRAY = new RewriteRuleTokenStream(adaptor, "token ARRAY");
        RewriteRuleTokenStream stream_LS = new RewriteRuleTokenStream(adaptor, "token LS");
        RewriteRuleTokenStream stream_CM = new RewriteRuleTokenStream(adaptor, "token CM");
        RewriteRuleSubtreeStream stream_index_set = new RewriteRuleSubtreeStream(adaptor, "rule index_set");
        RewriteRuleSubtreeStream stream_var_type = new RewriteRuleSubtreeStream(adaptor, "rule var_type");
        try {
            // parser/flatzinc/FlatzincParser.g:126:5: ( var_type -> ^( VAR var_type ) | VAR SET OF INT -> ^( VAR SET ) | ARRAY LS index_set ( CM index_set )* RS OF VAR SET OF INT -> ^( ARRAY ( index_set )+ ^( VAR SET ) ) )
            int alt25 = 3;
            alt25 = dfa25.predict(input);
            switch (alt25) {
                case 1:
                    // parser/flatzinc/FlatzincParser.g:126:9: var_type
                {
                    pushFollow(FOLLOW_var_type_in_var_pred_param_type1005);
                    var_type137 = var_type();

                    state._fsp--;

                    stream_var_type.add(var_type137.getTree());

                    // AST REWRITE
                    // elements: var_type
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.tree : null);

                    root_0 = (Object) adaptor.nil();
                    // 127:5: -> ^( VAR var_type )
                    {
                        // parser/flatzinc/FlatzincParser.g:127:9: ^( VAR var_type )
                        {
                            Object root_1 = (Object) adaptor.nil();
                            root_1 = (Object) adaptor.becomeRoot(
                                    (Object) adaptor.create(VAR, "VAR")
                                    , root_1);

                            adaptor.addChild(root_1, stream_var_type.nextTree());

                            adaptor.addChild(root_0, root_1);
                        }

                    }


                    retval.tree = root_0;

                }
                break;
                case 2:
                    // parser/flatzinc/FlatzincParser.g:128:9: VAR SET OF INT
                {
                    VAR138 = (Token) match(input, VAR, FOLLOW_VAR_in_var_pred_param_type1028);
                    stream_VAR.add(VAR138);


                    SET139 = (Token) match(input, SET, FOLLOW_SET_in_var_pred_param_type1030);
                    stream_SET.add(SET139);


                    OF140 = (Token) match(input, OF, FOLLOW_OF_in_var_pred_param_type1032);
                    stream_OF.add(OF140);


                    INT141 = (Token) match(input, INT, FOLLOW_INT_in_var_pred_param_type1034);
                    stream_INT.add(INT141);


                    // AST REWRITE
                    // elements: SET, VAR
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.tree : null);

                    root_0 = (Object) adaptor.nil();
                    // 129:5: -> ^( VAR SET )
                    {
                        // parser/flatzinc/FlatzincParser.g:129:9: ^( VAR SET )
                        {
                            Object root_1 = (Object) adaptor.nil();
                            root_1 = (Object) adaptor.becomeRoot(
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
                break;
                case 3:
                    // parser/flatzinc/FlatzincParser.g:130:9: ARRAY LS index_set ( CM index_set )* RS OF VAR SET OF INT
                {
                    ARRAY142 = (Token) match(input, ARRAY, FOLLOW_ARRAY_in_var_pred_param_type1057);
                    stream_ARRAY.add(ARRAY142);


                    LS143 = (Token) match(input, LS, FOLLOW_LS_in_var_pred_param_type1059);
                    stream_LS.add(LS143);


                    pushFollow(FOLLOW_index_set_in_var_pred_param_type1061);
                    index_set144 = index_set();

                    state._fsp--;

                    stream_index_set.add(index_set144.getTree());

                    // parser/flatzinc/FlatzincParser.g:130:28: ( CM index_set )*
                    loop24:
                    do {
                        int alt24 = 2;
                        switch (input.LA(1)) {
                            case CM: {
                                alt24 = 1;
                            }
                            break;

                        }

                        switch (alt24) {
                            case 1:
                                // parser/flatzinc/FlatzincParser.g:130:29: CM index_set
                            {
                                CM145 = (Token) match(input, CM, FOLLOW_CM_in_var_pred_param_type1064);
                                stream_CM.add(CM145);


                                pushFollow(FOLLOW_index_set_in_var_pred_param_type1066);
                                index_set146 = index_set();

                                state._fsp--;

                                stream_index_set.add(index_set146.getTree());

                            }
                            break;

                            default:
                                break loop24;
                        }
                    } while (true);


                    RS147 = (Token) match(input, RS, FOLLOW_RS_in_var_pred_param_type1070);
                    stream_RS.add(RS147);


                    OF148 = (Token) match(input, OF, FOLLOW_OF_in_var_pred_param_type1072);
                    stream_OF.add(OF148);


                    VAR149 = (Token) match(input, VAR, FOLLOW_VAR_in_var_pred_param_type1074);
                    stream_VAR.add(VAR149);


                    SET150 = (Token) match(input, SET, FOLLOW_SET_in_var_pred_param_type1076);
                    stream_SET.add(SET150);


                    OF151 = (Token) match(input, OF, FOLLOW_OF_in_var_pred_param_type1078);
                    stream_OF.add(OF151);


                    INT152 = (Token) match(input, INT, FOLLOW_INT_in_var_pred_param_type1080);
                    stream_INT.add(INT152);


                    // AST REWRITE
                    // elements: SET, ARRAY, VAR, index_set
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.tree : null);

                    root_0 = (Object) adaptor.nil();
                    // 131:5: -> ^( ARRAY ( index_set )+ ^( VAR SET ) )
                    {
                        // parser/flatzinc/FlatzincParser.g:131:9: ^( ARRAY ( index_set )+ ^( VAR SET ) )
                        {
                            Object root_1 = (Object) adaptor.nil();
                            root_1 = (Object) adaptor.becomeRoot(
                                    stream_ARRAY.nextNode()
                                    , root_1);

                            if (!(stream_index_set.hasNext())) {
                                throw new RewriteEarlyExitException();
                            }
                            while (stream_index_set.hasNext()) {
                                adaptor.addChild(root_1, stream_index_set.nextTree());

                            }
                            stream_index_set.reset();

                            // parser/flatzinc/FlatzincParser.g:131:28: ^( VAR SET )
                            {
                                Object root_2 = (Object) adaptor.nil();
                                root_2 = (Object) adaptor.becomeRoot(
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
                break;

            }
            retval.stop = input.LT(-1);


            retval.tree = (Object) adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
            retval.tree = (Object) adaptor.errorNode(input, retval.start, input.LT(-1), re);

        } finally {
            // do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "var_pred_param_type"


    public static class index_set_return extends ParserRuleReturnScope {
        Object tree;

        public Object getTree() {
            return tree;
        }
    }

    ;


    // $ANTLR start "index_set"
    // parser/flatzinc/FlatzincParser.g:134:1: index_set : ( INT_CONST DD INT_CONST -> ^( INDEX ^( DD INT_CONST INT_CONST ) ) | INT -> ^( INDEX INT ) );
    public final FlatzincParser.index_set_return index_set() throws RecognitionException {
        FlatzincParser.index_set_return retval = new FlatzincParser.index_set_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token INT_CONST153 = null;
        Token DD154 = null;
        Token INT_CONST155 = null;
        Token INT156 = null;

        Object INT_CONST153_tree = null;
        Object DD154_tree = null;
        Object INT_CONST155_tree = null;
        Object INT156_tree = null;
        RewriteRuleTokenStream stream_INT_CONST = new RewriteRuleTokenStream(adaptor, "token INT_CONST");
        RewriteRuleTokenStream stream_INT = new RewriteRuleTokenStream(adaptor, "token INT");
        RewriteRuleTokenStream stream_DD = new RewriteRuleTokenStream(adaptor, "token DD");

        try {
            // parser/flatzinc/FlatzincParser.g:135:5: ( INT_CONST DD INT_CONST -> ^( INDEX ^( DD INT_CONST INT_CONST ) ) | INT -> ^( INDEX INT ) )
            int alt26 = 2;
            switch (input.LA(1)) {
                case INT_CONST: {
                    alt26 = 1;
                }
                break;
                case INT: {
                    alt26 = 2;
                }
                break;
                default:
                    NoViableAltException nvae =
                            new NoViableAltException("", 26, 0, input);

                    throw nvae;

            }

            switch (alt26) {
                case 1:
                    // parser/flatzinc/FlatzincParser.g:135:9: INT_CONST DD INT_CONST
                {
                    INT_CONST153 = (Token) match(input, INT_CONST, FOLLOW_INT_CONST_in_index_set1119);
                    stream_INT_CONST.add(INT_CONST153);


                    DD154 = (Token) match(input, DD, FOLLOW_DD_in_index_set1121);
                    stream_DD.add(DD154);


                    INT_CONST155 = (Token) match(input, INT_CONST, FOLLOW_INT_CONST_in_index_set1123);
                    stream_INT_CONST.add(INT_CONST155);


                    // AST REWRITE
                    // elements: DD, INT_CONST, INT_CONST
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.tree : null);

                    root_0 = (Object) adaptor.nil();
                    // 136:5: -> ^( INDEX ^( DD INT_CONST INT_CONST ) )
                    {
                        // parser/flatzinc/FlatzincParser.g:136:9: ^( INDEX ^( DD INT_CONST INT_CONST ) )
                        {
                            Object root_1 = (Object) adaptor.nil();
                            root_1 = (Object) adaptor.becomeRoot(
                                    (Object) adaptor.create(INDEX, "INDEX")
                                    , root_1);

                            // parser/flatzinc/FlatzincParser.g:136:17: ^( DD INT_CONST INT_CONST )
                            {
                                Object root_2 = (Object) adaptor.nil();
                                root_2 = (Object) adaptor.becomeRoot(
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
                break;
                case 2:
                    // parser/flatzinc/FlatzincParser.g:137:9: INT
                {
                    INT156 = (Token) match(input, INT, FOLLOW_INT_in_index_set1152);
                    stream_INT.add(INT156);


                    // AST REWRITE
                    // elements: INT
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.tree : null);

                    root_0 = (Object) adaptor.nil();
                    // 138:5: -> ^( INDEX INT )
                    {
                        // parser/flatzinc/FlatzincParser.g:138:9: ^( INDEX INT )
                        {
                            Object root_1 = (Object) adaptor.nil();
                            root_1 = (Object) adaptor.becomeRoot(
                                    (Object) adaptor.create(INDEX, "INDEX")
                                    , root_1);

                            adaptor.addChild(root_1,
                                    stream_INT.nextNode()
                            );

                            adaptor.addChild(root_0, root_1);
                        }

                    }


                    retval.tree = root_0;

                }
                break;

            }
            retval.stop = input.LT(-1);


            retval.tree = (Object) adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
            retval.tree = (Object) adaptor.errorNode(input, retval.start, input.LT(-1), re);

        } finally {
            // do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "index_set"


    public static class expr_return extends ParserRuleReturnScope {
        Object tree;

        public Object getTree() {
            return tree;
        }
    }

    ;


    // $ANTLR start "expr"
    // parser/flatzinc/FlatzincParser.g:141:1: expr : ( LB INT_CONST ( CM INT_CONST )* RB -> LB ( INT_CONST )+ RB | bool_const | INT_CONST ( DD INT_CONST )? | LS ( expr ( CM expr )* )? RS -> ^( EXPR LS ( expr )* RS ) | id_expr | STRING );
    public final FlatzincParser.expr_return expr() throws RecognitionException {
        FlatzincParser.expr_return retval = new FlatzincParser.expr_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token LB157 = null;
        Token INT_CONST158 = null;
        Token CM159 = null;
        Token INT_CONST160 = null;
        Token RB161 = null;
        Token INT_CONST163 = null;
        Token DD164 = null;
        Token INT_CONST165 = null;
        Token LS166 = null;
        Token CM168 = null;
        Token RS170 = null;
        Token STRING172 = null;
        FlatzincParser.bool_const_return bool_const162 = null;

        FlatzincParser.expr_return expr167 = null;

        FlatzincParser.expr_return expr169 = null;

        FlatzincParser.id_expr_return id_expr171 = null;


        Object LB157_tree = null;
        Object INT_CONST158_tree = null;
        Object CM159_tree = null;
        Object INT_CONST160_tree = null;
        Object RB161_tree = null;
        Object INT_CONST163_tree = null;
        Object DD164_tree = null;
        Object INT_CONST165_tree = null;
        Object LS166_tree = null;
        Object CM168_tree = null;
        Object RS170_tree = null;
        Object STRING172_tree = null;
        RewriteRuleTokenStream stream_RS = new RewriteRuleTokenStream(adaptor, "token RS");
        RewriteRuleTokenStream stream_INT_CONST = new RewriteRuleTokenStream(adaptor, "token INT_CONST");
        RewriteRuleTokenStream stream_LB = new RewriteRuleTokenStream(adaptor, "token LB");
        RewriteRuleTokenStream stream_RB = new RewriteRuleTokenStream(adaptor, "token RB");
        RewriteRuleTokenStream stream_LS = new RewriteRuleTokenStream(adaptor, "token LS");
        RewriteRuleTokenStream stream_CM = new RewriteRuleTokenStream(adaptor, "token CM");
        RewriteRuleSubtreeStream stream_expr = new RewriteRuleSubtreeStream(adaptor, "rule expr");
        try {
            // parser/flatzinc/FlatzincParser.g:142:5: ( LB INT_CONST ( CM INT_CONST )* RB -> LB ( INT_CONST )+ RB | bool_const | INT_CONST ( DD INT_CONST )? | LS ( expr ( CM expr )* )? RS -> ^( EXPR LS ( expr )* RS ) | id_expr | STRING )
            int alt31 = 6;
            switch (input.LA(1)) {
                case LB: {
                    alt31 = 1;
                }
                break;
                case FALSE:
                case TRUE: {
                    alt31 = 2;
                }
                break;
                case INT_CONST: {
                    alt31 = 3;
                }
                break;
                case LS: {
                    alt31 = 4;
                }
                break;
                case IDENTIFIER: {
                    alt31 = 5;
                }
                break;
                case STRING: {
                    alt31 = 6;
                }
                break;
                default:
                    NoViableAltException nvae =
                            new NoViableAltException("", 31, 0, input);

                    throw nvae;

            }

            switch (alt31) {
                case 1:
                    // parser/flatzinc/FlatzincParser.g:142:9: LB INT_CONST ( CM INT_CONST )* RB
                {
                    LB157 = (Token) match(input, LB, FOLLOW_LB_in_expr1184);
                    stream_LB.add(LB157);


                    INT_CONST158 = (Token) match(input, INT_CONST, FOLLOW_INT_CONST_in_expr1186);
                    stream_INT_CONST.add(INT_CONST158);


                    // parser/flatzinc/FlatzincParser.g:142:22: ( CM INT_CONST )*
                    loop27:
                    do {
                        int alt27 = 2;
                        switch (input.LA(1)) {
                            case CM: {
                                alt27 = 1;
                            }
                            break;

                        }

                        switch (alt27) {
                            case 1:
                                // parser/flatzinc/FlatzincParser.g:142:23: CM INT_CONST
                            {
                                CM159 = (Token) match(input, CM, FOLLOW_CM_in_expr1189);
                                stream_CM.add(CM159);


                                INT_CONST160 = (Token) match(input, INT_CONST, FOLLOW_INT_CONST_in_expr1191);
                                stream_INT_CONST.add(INT_CONST160);


                            }
                            break;

                            default:
                                break loop27;
                        }
                    } while (true);


                    RB161 = (Token) match(input, RB, FOLLOW_RB_in_expr1195);
                    stream_RB.add(RB161);


                    // AST REWRITE
                    // elements: RB, INT_CONST, LB
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.tree : null);

                    root_0 = (Object) adaptor.nil();
                    // 143:5: -> LB ( INT_CONST )+ RB
                    {
                        adaptor.addChild(root_0,
                                stream_LB.nextNode()
                        );

                        if (!(stream_INT_CONST.hasNext())) {
                            throw new RewriteEarlyExitException();
                        }
                        while (stream_INT_CONST.hasNext()) {
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
                break;
                case 2:
                    // parser/flatzinc/FlatzincParser.g:144:9: bool_const
                {
                    root_0 = (Object) adaptor.nil();


                    pushFollow(FOLLOW_bool_const_in_expr1219);
                    bool_const162 = bool_const();

                    state._fsp--;

                    adaptor.addChild(root_0, bool_const162.getTree());

                }
                break;
                case 3:
                    // parser/flatzinc/FlatzincParser.g:145:9: INT_CONST ( DD INT_CONST )?
                {
                    root_0 = (Object) adaptor.nil();


                    INT_CONST163 = (Token) match(input, INT_CONST, FOLLOW_INT_CONST_in_expr1229);
                    INT_CONST163_tree =
                            (Object) adaptor.create(INT_CONST163)
                    ;
                    adaptor.addChild(root_0, INT_CONST163_tree);


                    // parser/flatzinc/FlatzincParser.g:145:19: ( DD INT_CONST )?
                    int alt28 = 2;
                    switch (input.LA(1)) {
                        case DD: {
                            alt28 = 1;
                        }
                        break;
                    }

                    switch (alt28) {
                        case 1:
                            // parser/flatzinc/FlatzincParser.g:145:20: DD INT_CONST
                        {
                            DD164 = (Token) match(input, DD, FOLLOW_DD_in_expr1232);
                            DD164_tree =
                                    (Object) adaptor.create(DD164)
                            ;
                            adaptor.addChild(root_0, DD164_tree);


                            INT_CONST165 = (Token) match(input, INT_CONST, FOLLOW_INT_CONST_in_expr1234);
                            INT_CONST165_tree =
                                    (Object) adaptor.create(INT_CONST165)
                            ;
                            adaptor.addChild(root_0, INT_CONST165_tree);


                        }
                        break;

                    }


                }
                break;
                case 4:
                    // parser/flatzinc/FlatzincParser.g:146:9: LS ( expr ( CM expr )* )? RS
                {
                    LS166 = (Token) match(input, LS, FOLLOW_LS_in_expr1246);
                    stream_LS.add(LS166);


                    // parser/flatzinc/FlatzincParser.g:146:12: ( expr ( CM expr )* )?
                    int alt30 = 2;
                    switch (input.LA(1)) {
                        case FALSE:
                        case IDENTIFIER:
                        case INT_CONST:
                        case LB:
                        case LS:
                        case STRING:
                        case TRUE: {
                            alt30 = 1;
                        }
                        break;
                    }

                    switch (alt30) {
                        case 1:
                            // parser/flatzinc/FlatzincParser.g:146:13: expr ( CM expr )*
                        {
                            pushFollow(FOLLOW_expr_in_expr1249);
                            expr167 = expr();

                            state._fsp--;

                            stream_expr.add(expr167.getTree());

                            // parser/flatzinc/FlatzincParser.g:146:18: ( CM expr )*
                            loop29:
                            do {
                                int alt29 = 2;
                                switch (input.LA(1)) {
                                    case CM: {
                                        alt29 = 1;
                                    }
                                    break;

                                }

                                switch (alt29) {
                                    case 1:
                                        // parser/flatzinc/FlatzincParser.g:146:19: CM expr
                                    {
                                        CM168 = (Token) match(input, CM, FOLLOW_CM_in_expr1252);
                                        stream_CM.add(CM168);


                                        pushFollow(FOLLOW_expr_in_expr1254);
                                        expr169 = expr();

                                        state._fsp--;

                                        stream_expr.add(expr169.getTree());

                                    }
                                    break;

                                    default:
                                        break loop29;
                                }
                            } while (true);


                        }
                        break;

                    }


                    RS170 = (Token) match(input, RS, FOLLOW_RS_in_expr1260);
                    stream_RS.add(RS170);


                    // AST REWRITE
                    // elements: expr, LS, RS
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.tree : null);

                    root_0 = (Object) adaptor.nil();
                    // 147:5: -> ^( EXPR LS ( expr )* RS )
                    {
                        // parser/flatzinc/FlatzincParser.g:147:9: ^( EXPR LS ( expr )* RS )
                        {
                            Object root_1 = (Object) adaptor.nil();
                            root_1 = (Object) adaptor.becomeRoot(
                                    (Object) adaptor.create(EXPR, "EXPR")
                                    , root_1);

                            adaptor.addChild(root_1,
                                    stream_LS.nextNode()
                            );

                            // parser/flatzinc/FlatzincParser.g:147:19: ( expr )*
                            while (stream_expr.hasNext()) {
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
                break;
                case 5:
                    // parser/flatzinc/FlatzincParser.g:148:9: id_expr
                {
                    root_0 = (Object) adaptor.nil();


                    pushFollow(FOLLOW_id_expr_in_expr1288);
                    id_expr171 = id_expr();

                    state._fsp--;

                    adaptor.addChild(root_0, id_expr171.getTree());

                }
                break;
                case 6:
                    // parser/flatzinc/FlatzincParser.g:149:9: STRING
                {
                    root_0 = (Object) adaptor.nil();


                    STRING172 = (Token) match(input, STRING, FOLLOW_STRING_in_expr1298);
                    STRING172_tree =
                            (Object) adaptor.create(STRING172)
                    ;
                    adaptor.addChild(root_0, STRING172_tree);


                }
                break;

            }
            retval.stop = input.LT(-1);


            retval.tree = (Object) adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
            retval.tree = (Object) adaptor.errorNode(input, retval.start, input.LT(-1), re);

        } finally {
            // do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "expr"


    public static class id_expr_return extends ParserRuleReturnScope {
        Object tree;

        public Object getTree() {
            return tree;
        }
    }

    ;


    // $ANTLR start "id_expr"
    // parser/flatzinc/FlatzincParser.g:153:1: id_expr : IDENTIFIER ( ( LP expr ( CM expr )* RP ) | ( LS INT_CONST RS ) )? ;
    public final FlatzincParser.id_expr_return id_expr() throws RecognitionException {
        FlatzincParser.id_expr_return retval = new FlatzincParser.id_expr_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token IDENTIFIER173 = null;
        Token LP174 = null;
        Token CM176 = null;
        Token RP178 = null;
        Token LS179 = null;
        Token INT_CONST180 = null;
        Token RS181 = null;
        FlatzincParser.expr_return expr175 = null;

        FlatzincParser.expr_return expr177 = null;


        Object IDENTIFIER173_tree = null;
        Object LP174_tree = null;
        Object CM176_tree = null;
        Object RP178_tree = null;
        Object LS179_tree = null;
        Object INT_CONST180_tree = null;
        Object RS181_tree = null;

        try {
            // parser/flatzinc/FlatzincParser.g:155:5: ( IDENTIFIER ( ( LP expr ( CM expr )* RP ) | ( LS INT_CONST RS ) )? )
            // parser/flatzinc/FlatzincParser.g:155:9: IDENTIFIER ( ( LP expr ( CM expr )* RP ) | ( LS INT_CONST RS ) )?
            {
                root_0 = (Object) adaptor.nil();


                IDENTIFIER173 = (Token) match(input, IDENTIFIER, FOLLOW_IDENTIFIER_in_id_expr1319);
                IDENTIFIER173_tree =
                        (Object) adaptor.create(IDENTIFIER173)
                ;
                adaptor.addChild(root_0, IDENTIFIER173_tree);


                // parser/flatzinc/FlatzincParser.g:155:20: ( ( LP expr ( CM expr )* RP ) | ( LS INT_CONST RS ) )?
                int alt33 = 3;
                switch (input.LA(1)) {
                    case LP: {
                        alt33 = 1;
                    }
                    break;
                    case LS: {
                        alt33 = 2;
                    }
                    break;
                }

                switch (alt33) {
                    case 1:
                        // parser/flatzinc/FlatzincParser.g:155:21: ( LP expr ( CM expr )* RP )
                    {
                        // parser/flatzinc/FlatzincParser.g:155:21: ( LP expr ( CM expr )* RP )
                        // parser/flatzinc/FlatzincParser.g:155:22: LP expr ( CM expr )* RP
                        {
                            LP174 = (Token) match(input, LP, FOLLOW_LP_in_id_expr1323);
                            LP174_tree =
                                    (Object) adaptor.create(LP174)
                            ;
                            adaptor.addChild(root_0, LP174_tree);


                            pushFollow(FOLLOW_expr_in_id_expr1325);
                            expr175 = expr();

                            state._fsp--;

                            adaptor.addChild(root_0, expr175.getTree());

                            // parser/flatzinc/FlatzincParser.g:155:30: ( CM expr )*
                            loop32:
                            do {
                                int alt32 = 2;
                                switch (input.LA(1)) {
                                    case CM: {
                                        alt32 = 1;
                                    }
                                    break;

                                }

                                switch (alt32) {
                                    case 1:
                                        // parser/flatzinc/FlatzincParser.g:155:31: CM expr
                                    {
                                        CM176 = (Token) match(input, CM, FOLLOW_CM_in_id_expr1328);
                                        CM176_tree =
                                                (Object) adaptor.create(CM176)
                                        ;
                                        adaptor.addChild(root_0, CM176_tree);


                                        pushFollow(FOLLOW_expr_in_id_expr1330);
                                        expr177 = expr();

                                        state._fsp--;

                                        adaptor.addChild(root_0, expr177.getTree());

                                    }
                                    break;

                                    default:
                                        break loop32;
                                }
                            } while (true);


                            RP178 = (Token) match(input, RP, FOLLOW_RP_in_id_expr1334);
                            RP178_tree =
                                    (Object) adaptor.create(RP178)
                            ;
                            adaptor.addChild(root_0, RP178_tree);


                        }


                    }
                    break;
                    case 2:
                        // parser/flatzinc/FlatzincParser.g:155:45: ( LS INT_CONST RS )
                    {
                        // parser/flatzinc/FlatzincParser.g:155:45: ( LS INT_CONST RS )
                        // parser/flatzinc/FlatzincParser.g:155:46: LS INT_CONST RS
                        {
                            LS179 = (Token) match(input, LS, FOLLOW_LS_in_id_expr1338);
                            LS179_tree =
                                    (Object) adaptor.create(LS179)
                            ;
                            adaptor.addChild(root_0, LS179_tree);


                            INT_CONST180 = (Token) match(input, INT_CONST, FOLLOW_INT_CONST_in_id_expr1340);
                            INT_CONST180_tree =
                                    (Object) adaptor.create(INT_CONST180)
                            ;
                            adaptor.addChild(root_0, INT_CONST180_tree);


                            RS181 = (Token) match(input, RS, FOLLOW_RS_in_id_expr1342);
                            RS181_tree =
                                    (Object) adaptor.create(RS181)
                            ;
                            adaptor.addChild(root_0, RS181_tree);


                        }


                    }
                    break;

                }


            }

            retval.stop = input.LT(-1);


            retval.tree = (Object) adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
            retval.tree = (Object) adaptor.errorNode(input, retval.start, input.LT(-1), re);

        } finally {
            // do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "id_expr"


    public static class param_decl_return extends ParserRuleReturnScope {
        Object tree;

        public Object getTree() {
            return tree;
        }
    }

    ;


    // $ANTLR start "param_decl"
    // parser/flatzinc/FlatzincParser.g:159:1: param_decl : par_type CL IDENTIFIER EQ expr SC -> ^( PAR IDENTIFIER par_type expr ) ;
    public final FlatzincParser.param_decl_return param_decl() throws RecognitionException {
        FlatzincParser.param_decl_return retval = new FlatzincParser.param_decl_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token CL183 = null;
        Token IDENTIFIER184 = null;
        Token EQ185 = null;
        Token SC187 = null;
        FlatzincParser.par_type_return par_type182 = null;

        FlatzincParser.expr_return expr186 = null;


        Object CL183_tree = null;
        Object IDENTIFIER184_tree = null;
        Object EQ185_tree = null;
        Object SC187_tree = null;
        RewriteRuleTokenStream stream_SC = new RewriteRuleTokenStream(adaptor, "token SC");
        RewriteRuleTokenStream stream_EQ = new RewriteRuleTokenStream(adaptor, "token EQ");
        RewriteRuleTokenStream stream_IDENTIFIER = new RewriteRuleTokenStream(adaptor, "token IDENTIFIER");
        RewriteRuleTokenStream stream_CL = new RewriteRuleTokenStream(adaptor, "token CL");
        RewriteRuleSubtreeStream stream_par_type = new RewriteRuleSubtreeStream(adaptor, "rule par_type");
        RewriteRuleSubtreeStream stream_expr = new RewriteRuleSubtreeStream(adaptor, "rule expr");
        try {
            // parser/flatzinc/FlatzincParser.g:160:2: ( par_type CL IDENTIFIER EQ expr SC -> ^( PAR IDENTIFIER par_type expr ) )
            // parser/flatzinc/FlatzincParser.g:160:6: par_type CL IDENTIFIER EQ expr SC
            {
                pushFollow(FOLLOW_par_type_in_param_decl1362);
                par_type182 = par_type();

                state._fsp--;

                stream_par_type.add(par_type182.getTree());

                CL183 = (Token) match(input, CL, FOLLOW_CL_in_param_decl1364);
                stream_CL.add(CL183);


                IDENTIFIER184 = (Token) match(input, IDENTIFIER, FOLLOW_IDENTIFIER_in_param_decl1366);
                stream_IDENTIFIER.add(IDENTIFIER184);


                EQ185 = (Token) match(input, EQ, FOLLOW_EQ_in_param_decl1368);
                stream_EQ.add(EQ185);


                pushFollow(FOLLOW_expr_in_param_decl1370);
                expr186 = expr();

                state._fsp--;

                stream_expr.add(expr186.getTree());

                SC187 = (Token) match(input, SC, FOLLOW_SC_in_param_decl1372);
                stream_SC.add(SC187);


                // AST REWRITE
                // elements: par_type, expr, IDENTIFIER
                // token labels: 
                // rule labels: retval
                // token list labels: 
                // rule list labels: 
                // wildcard labels: 
                retval.tree = root_0;
                RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.tree : null);

                root_0 = (Object) adaptor.nil();
                // 161:2: -> ^( PAR IDENTIFIER par_type expr )
                {
                    // parser/flatzinc/FlatzincParser.g:161:6: ^( PAR IDENTIFIER par_type expr )
                    {
                        Object root_1 = (Object) adaptor.nil();
                        root_1 = (Object) adaptor.becomeRoot(
                                (Object) adaptor.create(PAR, "PAR")
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

            retval.stop = input.LT(-1);


            retval.tree = (Object) adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
            retval.tree = (Object) adaptor.errorNode(input, retval.start, input.LT(-1), re);

        } finally {
            // do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "param_decl"


    public static class var_decl_return extends ParserRuleReturnScope {
        Object tree;

        public Object getTree() {
            return tree;
        }
    }

    ;


    // $ANTLR start "var_decl"
    // parser/flatzinc/FlatzincParser.g:165:1: var_decl : var_type CL IDENTIFIER annotations ( EQ expr )? SC -> ^( VAR IDENTIFIER var_type annotations ( expr )? ) ;
    public final FlatzincParser.var_decl_return var_decl() throws RecognitionException {
        FlatzincParser.var_decl_return retval = new FlatzincParser.var_decl_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token CL189 = null;
        Token IDENTIFIER190 = null;
        Token EQ192 = null;
        Token SC194 = null;
        FlatzincParser.var_type_return var_type188 = null;

        FlatzincParser.annotations_return annotations191 = null;

        FlatzincParser.expr_return expr193 = null;


        Object CL189_tree = null;
        Object IDENTIFIER190_tree = null;
        Object EQ192_tree = null;
        Object SC194_tree = null;
        RewriteRuleTokenStream stream_SC = new RewriteRuleTokenStream(adaptor, "token SC");
        RewriteRuleTokenStream stream_EQ = new RewriteRuleTokenStream(adaptor, "token EQ");
        RewriteRuleTokenStream stream_IDENTIFIER = new RewriteRuleTokenStream(adaptor, "token IDENTIFIER");
        RewriteRuleTokenStream stream_CL = new RewriteRuleTokenStream(adaptor, "token CL");
        RewriteRuleSubtreeStream stream_var_type = new RewriteRuleSubtreeStream(adaptor, "rule var_type");
        RewriteRuleSubtreeStream stream_expr = new RewriteRuleSubtreeStream(adaptor, "rule expr");
        RewriteRuleSubtreeStream stream_annotations = new RewriteRuleSubtreeStream(adaptor, "rule annotations");
        try {
            // parser/flatzinc/FlatzincParser.g:166:2: ( var_type CL IDENTIFIER annotations ( EQ expr )? SC -> ^( VAR IDENTIFIER var_type annotations ( expr )? ) )
            // parser/flatzinc/FlatzincParser.g:166:6: var_type CL IDENTIFIER annotations ( EQ expr )? SC
            {
                pushFollow(FOLLOW_var_type_in_var_decl1400);
                var_type188 = var_type();

                state._fsp--;

                stream_var_type.add(var_type188.getTree());

                CL189 = (Token) match(input, CL, FOLLOW_CL_in_var_decl1402);
                stream_CL.add(CL189);


                IDENTIFIER190 = (Token) match(input, IDENTIFIER, FOLLOW_IDENTIFIER_in_var_decl1404);
                stream_IDENTIFIER.add(IDENTIFIER190);


                pushFollow(FOLLOW_annotations_in_var_decl1406);
                annotations191 = annotations();

                state._fsp--;

                stream_annotations.add(annotations191.getTree());

                // parser/flatzinc/FlatzincParser.g:166:41: ( EQ expr )?
                int alt34 = 2;
                switch (input.LA(1)) {
                    case EQ: {
                        alt34 = 1;
                    }
                    break;
                }

                switch (alt34) {
                    case 1:
                        // parser/flatzinc/FlatzincParser.g:166:42: EQ expr
                    {
                        EQ192 = (Token) match(input, EQ, FOLLOW_EQ_in_var_decl1409);
                        stream_EQ.add(EQ192);


                        pushFollow(FOLLOW_expr_in_var_decl1411);
                        expr193 = expr();

                        state._fsp--;

                        stream_expr.add(expr193.getTree());

                    }
                    break;

                }


                SC194 = (Token) match(input, SC, FOLLOW_SC_in_var_decl1415);
                stream_SC.add(SC194);


                // AST REWRITE
                // elements: IDENTIFIER, var_type, expr, annotations
                // token labels: 
                // rule labels: retval
                // token list labels: 
                // rule list labels: 
                // wildcard labels: 
                retval.tree = root_0;
                RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.tree : null);

                root_0 = (Object) adaptor.nil();
                // 167:2: -> ^( VAR IDENTIFIER var_type annotations ( expr )? )
                {
                    // parser/flatzinc/FlatzincParser.g:167:6: ^( VAR IDENTIFIER var_type annotations ( expr )? )
                    {
                        Object root_1 = (Object) adaptor.nil();
                        root_1 = (Object) adaptor.becomeRoot(
                                (Object) adaptor.create(VAR, "VAR")
                                , root_1);

                        adaptor.addChild(root_1,
                                stream_IDENTIFIER.nextNode()
                        );

                        adaptor.addChild(root_1, stream_var_type.nextTree());

                        adaptor.addChild(root_1, stream_annotations.nextTree());

                        // parser/flatzinc/FlatzincParser.g:167:44: ( expr )?
                        if (stream_expr.hasNext()) {
                            adaptor.addChild(root_1, stream_expr.nextTree());

                        }
                        stream_expr.reset();

                        adaptor.addChild(root_0, root_1);
                    }

                }


                retval.tree = root_0;

            }

            retval.stop = input.LT(-1);


            retval.tree = (Object) adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
            retval.tree = (Object) adaptor.errorNode(input, retval.start, input.LT(-1), re);

        } finally {
            // do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "var_decl"


    public static class constraint_return extends ParserRuleReturnScope {
        Object tree;

        public Object getTree() {
            return tree;
        }
    }

    ;


    // $ANTLR start "constraint"
    // parser/flatzinc/FlatzincParser.g:170:1: constraint : CONSTRAINT IDENTIFIER LP expr ( CM expr )* RP annotations SC -> ^( CONSTRAINT IDENTIFIER ( expr )+ annotations ) ;
    public final FlatzincParser.constraint_return constraint() throws RecognitionException {
        FlatzincParser.constraint_return retval = new FlatzincParser.constraint_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token CONSTRAINT195 = null;
        Token IDENTIFIER196 = null;
        Token LP197 = null;
        Token CM199 = null;
        Token RP201 = null;
        Token SC203 = null;
        FlatzincParser.expr_return expr198 = null;

        FlatzincParser.expr_return expr200 = null;

        FlatzincParser.annotations_return annotations202 = null;


        Object CONSTRAINT195_tree = null;
        Object IDENTIFIER196_tree = null;
        Object LP197_tree = null;
        Object CM199_tree = null;
        Object RP201_tree = null;
        Object SC203_tree = null;
        RewriteRuleTokenStream stream_SC = new RewriteRuleTokenStream(adaptor, "token SC");
        RewriteRuleTokenStream stream_RP = new RewriteRuleTokenStream(adaptor, "token RP");
        RewriteRuleTokenStream stream_CONSTRAINT = new RewriteRuleTokenStream(adaptor, "token CONSTRAINT");
        RewriteRuleTokenStream stream_IDENTIFIER = new RewriteRuleTokenStream(adaptor, "token IDENTIFIER");
        RewriteRuleTokenStream stream_CM = new RewriteRuleTokenStream(adaptor, "token CM");
        RewriteRuleTokenStream stream_LP = new RewriteRuleTokenStream(adaptor, "token LP");
        RewriteRuleSubtreeStream stream_expr = new RewriteRuleSubtreeStream(adaptor, "rule expr");
        RewriteRuleSubtreeStream stream_annotations = new RewriteRuleSubtreeStream(adaptor, "rule annotations");
        try {
            // parser/flatzinc/FlatzincParser.g:171:2: ( CONSTRAINT IDENTIFIER LP expr ( CM expr )* RP annotations SC -> ^( CONSTRAINT IDENTIFIER ( expr )+ annotations ) )
            // parser/flatzinc/FlatzincParser.g:171:6: CONSTRAINT IDENTIFIER LP expr ( CM expr )* RP annotations SC
            {
                CONSTRAINT195 = (Token) match(input, CONSTRAINT, FOLLOW_CONSTRAINT_in_constraint1445);
                stream_CONSTRAINT.add(CONSTRAINT195);


                IDENTIFIER196 = (Token) match(input, IDENTIFIER, FOLLOW_IDENTIFIER_in_constraint1447);
                stream_IDENTIFIER.add(IDENTIFIER196);


                LP197 = (Token) match(input, LP, FOLLOW_LP_in_constraint1449);
                stream_LP.add(LP197);


                pushFollow(FOLLOW_expr_in_constraint1451);
                expr198 = expr();

                state._fsp--;

                stream_expr.add(expr198.getTree());

                // parser/flatzinc/FlatzincParser.g:171:36: ( CM expr )*
                loop35:
                do {
                    int alt35 = 2;
                    switch (input.LA(1)) {
                        case CM: {
                            alt35 = 1;
                        }
                        break;

                    }

                    switch (alt35) {
                        case 1:
                            // parser/flatzinc/FlatzincParser.g:171:37: CM expr
                        {
                            CM199 = (Token) match(input, CM, FOLLOW_CM_in_constraint1454);
                            stream_CM.add(CM199);


                            pushFollow(FOLLOW_expr_in_constraint1456);
                            expr200 = expr();

                            state._fsp--;

                            stream_expr.add(expr200.getTree());

                        }
                        break;

                        default:
                            break loop35;
                    }
                } while (true);


                RP201 = (Token) match(input, RP, FOLLOW_RP_in_constraint1460);
                stream_RP.add(RP201);


                pushFollow(FOLLOW_annotations_in_constraint1462);
                annotations202 = annotations();

                state._fsp--;

                stream_annotations.add(annotations202.getTree());

                SC203 = (Token) match(input, SC, FOLLOW_SC_in_constraint1464);
                stream_SC.add(SC203);


                // AST REWRITE
                // elements: IDENTIFIER, annotations, CONSTRAINT, expr
                // token labels: 
                // rule labels: retval
                // token list labels: 
                // rule list labels: 
                // wildcard labels: 
                retval.tree = root_0;
                RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.tree : null);

                root_0 = (Object) adaptor.nil();
                // 172:2: -> ^( CONSTRAINT IDENTIFIER ( expr )+ annotations )
                {
                    // parser/flatzinc/FlatzincParser.g:172:6: ^( CONSTRAINT IDENTIFIER ( expr )+ annotations )
                    {
                        Object root_1 = (Object) adaptor.nil();
                        root_1 = (Object) adaptor.becomeRoot(
                                stream_CONSTRAINT.nextNode()
                                , root_1);

                        adaptor.addChild(root_1,
                                stream_IDENTIFIER.nextNode()
                        );

                        if (!(stream_expr.hasNext())) {
                            throw new RewriteEarlyExitException();
                        }
                        while (stream_expr.hasNext()) {
                            adaptor.addChild(root_1, stream_expr.nextTree());

                        }
                        stream_expr.reset();

                        adaptor.addChild(root_1, stream_annotations.nextTree());

                        adaptor.addChild(root_0, root_1);
                    }

                }


                retval.tree = root_0;

            }

            retval.stop = input.LT(-1);


            retval.tree = (Object) adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
            retval.tree = (Object) adaptor.errorNode(input, retval.start, input.LT(-1), re);

        } finally {
            // do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "constraint"


    public static class solve_goal_return extends ParserRuleReturnScope {
        Object tree;

        public Object getTree() {
            return tree;
        }
    }

    ;


    // $ANTLR start "solve_goal"
    // parser/flatzinc/FlatzincParser.g:175:1: solve_goal : SOLVE ^ annotations resolution SC !;
    public final FlatzincParser.solve_goal_return solve_goal() throws RecognitionException {
        FlatzincParser.solve_goal_return retval = new FlatzincParser.solve_goal_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token SOLVE204 = null;
        Token SC207 = null;
        FlatzincParser.annotations_return annotations205 = null;

        FlatzincParser.resolution_return resolution206 = null;


        Object SOLVE204_tree = null;
        Object SC207_tree = null;

        try {
            // parser/flatzinc/FlatzincParser.g:176:2: ( SOLVE ^ annotations resolution SC !)
            // parser/flatzinc/FlatzincParser.g:176:6: SOLVE ^ annotations resolution SC !
            {
                root_0 = (Object) adaptor.nil();


                SOLVE204 = (Token) match(input, SOLVE, FOLLOW_SOLVE_in_solve_goal1492);
                SOLVE204_tree =
                        (Object) adaptor.create(SOLVE204)
                ;
                root_0 = (Object) adaptor.becomeRoot(SOLVE204_tree, root_0);


                pushFollow(FOLLOW_annotations_in_solve_goal1495);
                annotations205 = annotations();

                state._fsp--;

                adaptor.addChild(root_0, annotations205.getTree());

                pushFollow(FOLLOW_resolution_in_solve_goal1497);
                resolution206 = resolution();

                state._fsp--;

                adaptor.addChild(root_0, resolution206.getTree());

                SC207 = (Token) match(input, SC, FOLLOW_SC_in_solve_goal1499);

            }

            retval.stop = input.LT(-1);


            retval.tree = (Object) adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
            retval.tree = (Object) adaptor.errorNode(input, retval.start, input.LT(-1), re);

        } finally {
            // do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "solve_goal"


    public static class resolution_return extends ParserRuleReturnScope {
        Object tree;

        public Object getTree() {
            return tree;
        }
    }

    ;


    // $ANTLR start "resolution"
    // parser/flatzinc/FlatzincParser.g:179:1: resolution : ( MINIMIZE ^ expr | MAXIMIZE ^ expr | SATISFY ^);
    public final FlatzincParser.resolution_return resolution() throws RecognitionException {
        FlatzincParser.resolution_return retval = new FlatzincParser.resolution_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token MINIMIZE208 = null;
        Token MAXIMIZE210 = null;
        Token SATISFY212 = null;
        FlatzincParser.expr_return expr209 = null;

        FlatzincParser.expr_return expr211 = null;


        Object MINIMIZE208_tree = null;
        Object MAXIMIZE210_tree = null;
        Object SATISFY212_tree = null;

        try {
            // parser/flatzinc/FlatzincParser.g:180:5: ( MINIMIZE ^ expr | MAXIMIZE ^ expr | SATISFY ^)
            int alt36 = 3;
            switch (input.LA(1)) {
                case MINIMIZE: {
                    alt36 = 1;
                }
                break;
                case MAXIMIZE: {
                    alt36 = 2;
                }
                break;
                case SATISFY: {
                    alt36 = 3;
                }
                break;
                default:
                    NoViableAltException nvae =
                            new NoViableAltException("", 36, 0, input);

                    throw nvae;

            }

            switch (alt36) {
                case 1:
                    // parser/flatzinc/FlatzincParser.g:180:9: MINIMIZE ^ expr
                {
                    root_0 = (Object) adaptor.nil();


                    MINIMIZE208 = (Token) match(input, MINIMIZE, FOLLOW_MINIMIZE_in_resolution1516);
                    MINIMIZE208_tree =
                            (Object) adaptor.create(MINIMIZE208)
                    ;
                    root_0 = (Object) adaptor.becomeRoot(MINIMIZE208_tree, root_0);


                    pushFollow(FOLLOW_expr_in_resolution1519);
                    expr209 = expr();

                    state._fsp--;

                    adaptor.addChild(root_0, expr209.getTree());

                }
                break;
                case 2:
                    // parser/flatzinc/FlatzincParser.g:181:9: MAXIMIZE ^ expr
                {
                    root_0 = (Object) adaptor.nil();


                    MAXIMIZE210 = (Token) match(input, MAXIMIZE, FOLLOW_MAXIMIZE_in_resolution1529);
                    MAXIMIZE210_tree =
                            (Object) adaptor.create(MAXIMIZE210)
                    ;
                    root_0 = (Object) adaptor.becomeRoot(MAXIMIZE210_tree, root_0);


                    pushFollow(FOLLOW_expr_in_resolution1532);
                    expr211 = expr();

                    state._fsp--;

                    adaptor.addChild(root_0, expr211.getTree());

                }
                break;
                case 3:
                    // parser/flatzinc/FlatzincParser.g:182:9: SATISFY ^
                {
                    root_0 = (Object) adaptor.nil();


                    SATISFY212 = (Token) match(input, SATISFY, FOLLOW_SATISFY_in_resolution1542);
                    SATISFY212_tree =
                            (Object) adaptor.create(SATISFY212)
                    ;
                    root_0 = (Object) adaptor.becomeRoot(SATISFY212_tree, root_0);


                }
                break;

            }
            retval.stop = input.LT(-1);


            retval.tree = (Object) adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
            retval.tree = (Object) adaptor.errorNode(input, retval.start, input.LT(-1), re);

        } finally {
            // do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "resolution"


    public static class annotations_return extends ParserRuleReturnScope {
        Object tree;

        public Object getTree() {
            return tree;
        }
    }

    ;


    // $ANTLR start "annotations"
    // parser/flatzinc/FlatzincParser.g:185:1: annotations : ( DC annotation )* -> ^( ANNOTATIONS ( annotation )* ) ;
    public final FlatzincParser.annotations_return annotations() throws RecognitionException {
        FlatzincParser.annotations_return retval = new FlatzincParser.annotations_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token DC213 = null;
        FlatzincParser.annotation_return annotation214 = null;


        Object DC213_tree = null;
        RewriteRuleTokenStream stream_DC = new RewriteRuleTokenStream(adaptor, "token DC");
        RewriteRuleSubtreeStream stream_annotation = new RewriteRuleSubtreeStream(adaptor, "rule annotation");
        try {
            // parser/flatzinc/FlatzincParser.g:186:5: ( ( DC annotation )* -> ^( ANNOTATIONS ( annotation )* ) )
            // parser/flatzinc/FlatzincParser.g:186:9: ( DC annotation )*
            {
                // parser/flatzinc/FlatzincParser.g:186:9: ( DC annotation )*
                loop37:
                do {
                    int alt37 = 2;
                    switch (input.LA(1)) {
                        case DC: {
                            alt37 = 1;
                        }
                        break;

                    }

                    switch (alt37) {
                        case 1:
                            // parser/flatzinc/FlatzincParser.g:186:10: DC annotation
                        {
                            DC213 = (Token) match(input, DC, FOLLOW_DC_in_annotations1563);
                            stream_DC.add(DC213);


                            pushFollow(FOLLOW_annotation_in_annotations1565);
                            annotation214 = annotation();

                            state._fsp--;

                            stream_annotation.add(annotation214.getTree());

                        }
                        break;

                        default:
                            break loop37;
                    }
                } while (true);


                // AST REWRITE
                // elements: annotation
                // token labels: 
                // rule labels: retval
                // token list labels: 
                // rule list labels: 
                // wildcard labels: 
                retval.tree = root_0;
                RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.tree : null);

                root_0 = (Object) adaptor.nil();
                // 187:5: -> ^( ANNOTATIONS ( annotation )* )
                {
                    // parser/flatzinc/FlatzincParser.g:187:9: ^( ANNOTATIONS ( annotation )* )
                    {
                        Object root_1 = (Object) adaptor.nil();
                        root_1 = (Object) adaptor.becomeRoot(
                                (Object) adaptor.create(ANNOTATIONS, "ANNOTATIONS")
                                , root_1);

                        // parser/flatzinc/FlatzincParser.g:187:23: ( annotation )*
                        while (stream_annotation.hasNext()) {
                            adaptor.addChild(root_1, stream_annotation.nextTree());

                        }
                        stream_annotation.reset();

                        adaptor.addChild(root_0, root_1);
                    }

                }


                retval.tree = root_0;

            }

            retval.stop = input.LT(-1);


            retval.tree = (Object) adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
            retval.tree = (Object) adaptor.errorNode(input, retval.start, input.LT(-1), re);

        } finally {
            // do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "annotations"


    public static class annotation_return extends ParserRuleReturnScope {
        Object tree;

        public Object getTree() {
            return tree;
        }
    }

    ;


    // $ANTLR start "annotation"
    // parser/flatzinc/FlatzincParser.g:190:1: annotation : IDENTIFIER ( LP expr ( CM expr )* RP )? -> IDENTIFIER ( LP ( expr )+ RP )? ;
    public final FlatzincParser.annotation_return annotation() throws RecognitionException {
        FlatzincParser.annotation_return retval = new FlatzincParser.annotation_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token IDENTIFIER215 = null;
        Token LP216 = null;
        Token CM218 = null;
        Token RP220 = null;
        FlatzincParser.expr_return expr217 = null;

        FlatzincParser.expr_return expr219 = null;


        Object IDENTIFIER215_tree = null;
        Object LP216_tree = null;
        Object CM218_tree = null;
        Object RP220_tree = null;
        RewriteRuleTokenStream stream_RP = new RewriteRuleTokenStream(adaptor, "token RP");
        RewriteRuleTokenStream stream_IDENTIFIER = new RewriteRuleTokenStream(adaptor, "token IDENTIFIER");
        RewriteRuleTokenStream stream_CM = new RewriteRuleTokenStream(adaptor, "token CM");
        RewriteRuleTokenStream stream_LP = new RewriteRuleTokenStream(adaptor, "token LP");
        RewriteRuleSubtreeStream stream_expr = new RewriteRuleSubtreeStream(adaptor, "rule expr");
        try {
            // parser/flatzinc/FlatzincParser.g:191:5: ( IDENTIFIER ( LP expr ( CM expr )* RP )? -> IDENTIFIER ( LP ( expr )+ RP )? )
            // parser/flatzinc/FlatzincParser.g:191:9: IDENTIFIER ( LP expr ( CM expr )* RP )?
            {
                IDENTIFIER215 = (Token) match(input, IDENTIFIER, FOLLOW_IDENTIFIER_in_annotation1600);
                stream_IDENTIFIER.add(IDENTIFIER215);


                // parser/flatzinc/FlatzincParser.g:191:20: ( LP expr ( CM expr )* RP )?
                int alt39 = 2;
                switch (input.LA(1)) {
                    case LP: {
                        alt39 = 1;
                    }
                    break;
                }

                switch (alt39) {
                    case 1:
                        // parser/flatzinc/FlatzincParser.g:191:21: LP expr ( CM expr )* RP
                    {
                        LP216 = (Token) match(input, LP, FOLLOW_LP_in_annotation1603);
                        stream_LP.add(LP216);


                        pushFollow(FOLLOW_expr_in_annotation1605);
                        expr217 = expr();

                        state._fsp--;

                        stream_expr.add(expr217.getTree());

                        // parser/flatzinc/FlatzincParser.g:191:29: ( CM expr )*
                        loop38:
                        do {
                            int alt38 = 2;
                            switch (input.LA(1)) {
                                case CM: {
                                    alt38 = 1;
                                }
                                break;

                            }

                            switch (alt38) {
                                case 1:
                                    // parser/flatzinc/FlatzincParser.g:191:30: CM expr
                                {
                                    CM218 = (Token) match(input, CM, FOLLOW_CM_in_annotation1608);
                                    stream_CM.add(CM218);


                                    pushFollow(FOLLOW_expr_in_annotation1610);
                                    expr219 = expr();

                                    state._fsp--;

                                    stream_expr.add(expr219.getTree());

                                }
                                break;

                                default:
                                    break loop38;
                            }
                        } while (true);


                        RP220 = (Token) match(input, RP, FOLLOW_RP_in_annotation1614);
                        stream_RP.add(RP220);


                    }
                    break;

                }


                // AST REWRITE
                // elements: RP, expr, LP, IDENTIFIER
                // token labels: 
                // rule labels: retval
                // token list labels: 
                // rule list labels: 
                // wildcard labels: 
                retval.tree = root_0;
                RewriteRuleSubtreeStream stream_retval = new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null ? retval.tree : null);

                root_0 = (Object) adaptor.nil();
                // 192:5: -> IDENTIFIER ( LP ( expr )+ RP )?
                {
                    adaptor.addChild(root_0,
                            stream_IDENTIFIER.nextNode()
                    );

                    // parser/flatzinc/FlatzincParser.g:192:20: ( LP ( expr )+ RP )?
                    if (stream_RP.hasNext() || stream_expr.hasNext() || stream_LP.hasNext()) {
                        adaptor.addChild(root_0,
                                stream_LP.nextNode()
                        );

                        if (!(stream_expr.hasNext())) {
                            throw new RewriteEarlyExitException();
                        }
                        while (stream_expr.hasNext()) {
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

            retval.stop = input.LT(-1);


            retval.tree = (Object) adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
            retval.tree = (Object) adaptor.errorNode(input, retval.start, input.LT(-1), re);

        } finally {
            // do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "annotation"


    public static class bool_const_return extends ParserRuleReturnScope {
        Object tree;

        public Object getTree() {
            return tree;
        }
    }

    ;


    // $ANTLR start "bool_const"
    // parser/flatzinc/FlatzincParser.g:196:1: bool_const : ( TRUE ^| FALSE ^);
    public final FlatzincParser.bool_const_return bool_const() throws RecognitionException {
        FlatzincParser.bool_const_return retval = new FlatzincParser.bool_const_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token TRUE221 = null;
        Token FALSE222 = null;

        Object TRUE221_tree = null;
        Object FALSE222_tree = null;

        try {
            // parser/flatzinc/FlatzincParser.g:197:5: ( TRUE ^| FALSE ^)
            int alt40 = 2;
            switch (input.LA(1)) {
                case TRUE: {
                    alt40 = 1;
                }
                break;
                case FALSE: {
                    alt40 = 2;
                }
                break;
                default:
                    NoViableAltException nvae =
                            new NoViableAltException("", 40, 0, input);

                    throw nvae;

            }

            switch (alt40) {
                case 1:
                    // parser/flatzinc/FlatzincParser.g:197:9: TRUE ^
                {
                    root_0 = (Object) adaptor.nil();


                    TRUE221 = (Token) match(input, TRUE, FOLLOW_TRUE_in_bool_const1655);
                    TRUE221_tree =
                            (Object) adaptor.create(TRUE221)
                    ;
                    root_0 = (Object) adaptor.becomeRoot(TRUE221_tree, root_0);


                }
                break;
                case 2:
                    // parser/flatzinc/FlatzincParser.g:198:9: FALSE ^
                {
                    root_0 = (Object) adaptor.nil();


                    FALSE222 = (Token) match(input, FALSE, FOLLOW_FALSE_in_bool_const1666);
                    FALSE222_tree =
                            (Object) adaptor.create(FALSE222)
                    ;
                    root_0 = (Object) adaptor.becomeRoot(FALSE222_tree, root_0);


                }
                break;

            }
            retval.stop = input.LT(-1);


            retval.tree = (Object) adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
            retval.tree = (Object) adaptor.errorNode(input, retval.start, input.LT(-1), re);

        } finally {
            // do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "bool_const"

    // Delegated rules


    protected DFA2 dfa2 = new DFA2(this);
    protected DFA6 dfa6 = new DFA6(this);
    protected DFA23 dfa23 = new DFA23(this);
    protected DFA25 dfa25 = new DFA25(this);
    static final String DFA2_eotS =
            "\20\uffff";
    static final String DFA2_eofS =
            "\20\uffff";
    static final String DFA2_minS =
            "\1\6\1\40\2\uffff\1\34\1\21\1\15\1\35\1\34\1\45\1\15\1\21\1\15\1" +
                    "\12\1\35\1\15";
    static final String DFA2_maxS =
            "\1\63\1\40\2\uffff\1\35\1\21\1\53\2\35\1\45\1\53\1\21\1\53\1\63" +
                    "\1\35\1\53";
    static final String DFA2_acceptS =
            "\2\uffff\1\2\1\1\14\uffff";
    static final String DFA2_specialS =
            "\20\uffff}>";
    static final String[] DFA2_transitionS = {
            "\1\1\3\uffff\1\3\4\uffff\1\2\10\uffff\1\3\3\uffff\1\3\21\uffff" +
                    "\1\3\1\2\3\uffff\1\2",
            "\1\4",
            "",
            "",
            "\1\6\1\5",
            "\1\7",
            "\1\10\35\uffff\1\11",
            "\1\12",
            "\1\14\1\13",
            "\1\15",
            "\1\10\35\uffff\1\11",
            "\1\16",
            "\1\10\35\uffff\1\11",
            "\1\3\15\uffff\1\3\3\uffff\1\3\21\uffff\1\3\4\uffff\1\2",
            "\1\17",
            "\1\10\35\uffff\1\11"
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
        for (int i = 0; i < numStates; i++) {
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
            return "()* loopback of 44:19: ( param_decl )*";
        }
    }

    static final String DFA6_eotS =
            "\20\uffff";
    static final String DFA6_eofS =
            "\20\uffff";
    static final String DFA6_minS =
            "\1\6\1\40\2\uffff\1\34\1\21\1\15\1\35\1\34\1\45\1\15\1\21\1\15\1" +
                    "\12\1\35\1\15";
    static final String DFA6_maxS =
            "\1\63\1\40\2\uffff\1\35\1\21\1\53\2\35\1\45\1\53\1\21\1\53\1\63" +
                    "\1\35\1\53";
    static final String DFA6_acceptS =
            "\2\uffff\1\1\1\2\14\uffff";
    static final String DFA6_specialS =
            "\20\uffff}>";
    static final String[] DFA6_transitionS = {
            "\1\1\3\uffff\1\2\15\uffff\1\2\3\uffff\3\2\17\uffff\1\2\4\uffff" +
                    "\1\3",
            "\1\4",
            "",
            "",
            "\1\6\1\5",
            "\1\7",
            "\1\10\35\uffff\1\11",
            "\1\12",
            "\1\14\1\13",
            "\1\15",
            "\1\10\35\uffff\1\11",
            "\1\16",
            "\1\10\35\uffff\1\11",
            "\1\2\15\uffff\1\2\3\uffff\3\2\17\uffff\1\2\4\uffff\1\3",
            "\1\17",
            "\1\10\35\uffff\1\11"
    };

    static final short[] DFA6_eot = DFA.unpackEncodedString(DFA6_eotS);
    static final short[] DFA6_eof = DFA.unpackEncodedString(DFA6_eofS);
    static final char[] DFA6_min = DFA.unpackEncodedStringToUnsignedChars(DFA6_minS);
    static final char[] DFA6_max = DFA.unpackEncodedStringToUnsignedChars(DFA6_maxS);
    static final short[] DFA6_accept = DFA.unpackEncodedString(DFA6_acceptS);
    static final short[] DFA6_special = DFA.unpackEncodedString(DFA6_specialS);
    static final short[][] DFA6_transition;

    static {
        int numStates = DFA6_transitionS.length;
        DFA6_transition = new short[numStates][];
        for (int i = 0; i < numStates; i++) {
            DFA6_transition[i] = DFA.unpackEncodedString(DFA6_transitionS[i]);
        }
    }

    class DFA6 extends DFA {

        public DFA6(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 6;
            this.eot = DFA6_eot;
            this.eof = DFA6_eof;
            this.min = DFA6_min;
            this.max = DFA6_max;
            this.accept = DFA6_accept;
            this.special = DFA6_special;
            this.transition = DFA6_transition;
        }

        public String getDescription() {
            return "58:1: pred_param_type : ( par_pred_param_type | var_pred_param_type );";
        }
    }

    static final String DFA23_eotS =
            "\33\uffff";
    static final String DFA23_eofS =
            "\33\uffff";
    static final String DFA23_minS =
            "\1\6\1\40\1\uffff\1\45\2\uffff\2\34\1\21\1\15\2\uffff\1\35\1\34" +
                    "\1\45\1\15\1\21\1\15\1\12\1\35\2\uffff\1\45\1\15\1\34\2\uffff";
    static final String DFA23_maxS =
            "\1\56\1\40\1\uffff\1\45\2\uffff\1\35\1\36\1\21\1\53\2\uffff\2\35" +
                    "\1\45\1\53\1\21\1\53\1\56\1\35\2\uffff\1\45\1\53\1\36\2\uffff";
    static final String DFA23_acceptS =
            "\2\uffff\1\1\1\uffff\1\2\1\3\4\uffff\1\4\1\5\10\uffff\1\6\1\7\3" +
                    "\uffff\1\10\1\11";
    static final String DFA23_specialS =
            "\33\uffff}>";
    static final String[] DFA23_transitionS = {
            "\1\1\3\uffff\1\2\15\uffff\1\2\3\uffff\1\2\1\4\1\5\17\uffff\1" +
                    "\3",
            "\1\6",
            "",
            "\1\7",
            "",
            "",
            "\1\11\1\10",
            "\1\2\1\12\1\13",
            "\1\14",
            "\1\15\35\uffff\1\16",
            "",
            "",
            "\1\17",
            "\1\21\1\20",
            "\1\22",
            "\1\15\35\uffff\1\16",
            "\1\23",
            "\1\15\35\uffff\1\16",
            "\1\2\15\uffff\1\2\3\uffff\1\2\1\24\1\25\17\uffff\1\26",
            "\1\27",
            "",
            "",
            "\1\30",
            "\1\15\35\uffff\1\16",
            "\1\2\1\31\1\32",
            "",
            ""
    };

    static final short[] DFA23_eot = DFA.unpackEncodedString(DFA23_eotS);
    static final short[] DFA23_eof = DFA.unpackEncodedString(DFA23_eofS);
    static final char[] DFA23_min = DFA.unpackEncodedStringToUnsignedChars(DFA23_minS);
    static final char[] DFA23_max = DFA.unpackEncodedStringToUnsignedChars(DFA23_maxS);
    static final short[] DFA23_accept = DFA.unpackEncodedString(DFA23_acceptS);
    static final short[] DFA23_special = DFA.unpackEncodedString(DFA23_specialS);
    static final short[][] DFA23_transition;

    static {
        int numStates = DFA23_transitionS.length;
        DFA23_transition = new short[numStates][];
        for (int i = 0; i < numStates; i++) {
            DFA23_transition[i] = DFA.unpackEncodedString(DFA23_transitionS[i]);
        }
    }

    class DFA23 extends DFA {

        public DFA23(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 23;
            this.eot = DFA23_eot;
            this.eof = DFA23_eof;
            this.min = DFA23_min;
            this.max = DFA23_max;
            this.accept = DFA23_accept;
            this.special = DFA23_special;
            this.transition = DFA23_transition;
        }

        public String getDescription() {
            return "100:1: par_pred_param_type : ( par_type | INT_CONST DD INT_CONST -> ^( DD INT_CONST INT_CONST ) | LB INT_CONST ( CM INT_CONST )* RB -> ^( CM ( INT_CONST )+ ) | SET OF INT_CONST DD INT_CONST -> ^( SET ^( DD INT_CONST INT_CONST ) ) | SET OF LB INT_CONST ( CM INT_CONST )* RB -> ^( SET ^( CM ( INT_CONST )+ ) ) | ARRAY LS index_set ( CM index_set )* RS OF INT_CONST DD INT_CONST -> ^( ARRAY ( index_set )+ ^( DD INT_CONST INT_CONST ) ) | ARRAY LS index_set ( CM index_set )* RS OF LB INT_CONST ( CM INT_CONST )* RB -> ^( ARRAY ( index_set )+ ^( CM ( INT_CONST )+ ) ) | ARRAY LS index_set ( CM index_set )* RS OF SET OF INT_CONST DD INT_CONST -> ^( ARRAY ( index_set )+ ^( SET ^( DD INT_CONST INT_CONST ) ) ) | ARRAY LS index_set ( CM index_set )* RS OF SET OF LB INT_CONST ( CM INT_CONST )* RB -> ^( ARRAY ( index_set )+ ^( SET ^( CM ( INT_CONST )+ ) ) ) );";
        }
    }

    static final String DFA25_eotS =
            "\27\uffff";
    static final String DFA25_eofS =
            "\27\uffff";
    static final String DFA25_minS =
            "\1\6\1\40\1\12\1\34\1\45\1\uffff\1\21\1\15\1\34\1\35\1\34\1\45\1" +
                    "\uffff\1\15\1\21\1\15\1\63\1\35\1\12\1\15\1\45\1\34\1\uffff";
    static final String DFA25_maxS =
            "\1\63\1\40\1\56\1\35\1\45\1\uffff\1\21\1\53\1\36\2\35\1\45\1\uffff" +
                    "\1\53\1\21\1\53\1\63\1\35\1\56\1\53\1\45\1\36\1\uffff";
    static final String DFA25_acceptS =
            "\5\uffff\1\1\6\uffff\1\2\11\uffff\1\3";
    static final String DFA25_specialS =
            "\27\uffff}>";
    static final String[] DFA25_transitionS = {
            "\1\1\54\uffff\1\2",
            "\1\3",
            "\1\5\15\uffff\1\5\3\uffff\3\5\17\uffff\1\4",
            "\1\7\1\6",
            "\1\10",
            "",
            "\1\11",
            "\1\12\35\uffff\1\13",
            "\1\14\2\5",
            "\1\15",
            "\1\17\1\16",
            "\1\20",
            "",
            "\1\12\35\uffff\1\13",
            "\1\21",
            "\1\12\35\uffff\1\13",
            "\1\22",
            "\1\23",
            "\1\5\15\uffff\1\5\3\uffff\3\5\17\uffff\1\24",
            "\1\12\35\uffff\1\13",
            "\1\25",
            "\1\26\2\5",
            ""
    };

    static final short[] DFA25_eot = DFA.unpackEncodedString(DFA25_eotS);
    static final short[] DFA25_eof = DFA.unpackEncodedString(DFA25_eofS);
    static final char[] DFA25_min = DFA.unpackEncodedStringToUnsignedChars(DFA25_minS);
    static final char[] DFA25_max = DFA.unpackEncodedStringToUnsignedChars(DFA25_maxS);
    static final short[] DFA25_accept = DFA.unpackEncodedString(DFA25_acceptS);
    static final short[] DFA25_special = DFA.unpackEncodedString(DFA25_specialS);
    static final short[][] DFA25_transition;

    static {
        int numStates = DFA25_transitionS.length;
        DFA25_transition = new short[numStates][];
        for (int i = 0; i < numStates; i++) {
            DFA25_transition[i] = DFA.unpackEncodedString(DFA25_transitionS[i]);
        }
    }

    class DFA25 extends DFA {

        public DFA25(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 25;
            this.eot = DFA25_eot;
            this.eof = DFA25_eof;
            this.min = DFA25_min;
            this.max = DFA25_max;
            this.accept = DFA25_accept;
            this.special = DFA25_special;
            this.transition = DFA25_transition;
        }

        public String getDescription() {
            return "125:1: var_pred_param_type : ( var_type -> ^( VAR var_type ) | VAR SET OF INT -> ^( VAR SET ) | ARRAY LS index_set ( CM index_set )* RS OF VAR SET OF INT -> ^( ARRAY ( index_set )+ ^( VAR SET ) ) );";
        }
    }


    public static final BitSet FOLLOW_pred_decl_in_flatzinc_model59 = new BitSet(new long[]{0x0008C10011008440L});
    public static final BitSet FOLLOW_param_decl_in_flatzinc_model64 = new BitSet(new long[]{0x0008C00011008440L});
    public static final BitSet FOLLOW_var_decl_in_flatzinc_model69 = new BitSet(new long[]{0x0008800000008040L});
    public static final BitSet FOLLOW_constraint_in_flatzinc_model74 = new BitSet(new long[]{0x0000800000008000L});
    public static final BitSet FOLLOW_solve_goal_in_flatzinc_model78 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PREDICATE_in_pred_decl92 = new BitSet(new long[]{0x0000000004000000L});
    public static final BitSet FOLLOW_IDENTIFIER_in_pred_decl94 = new BitSet(new long[]{0x0000000080000000L});
    public static final BitSet FOLLOW_LP_in_pred_decl96 = new BitSet(new long[]{0x0008400071000440L});
    public static final BitSet FOLLOW_pred_param_in_pred_decl98 = new BitSet(new long[]{0x0000040000002000L});
    public static final BitSet FOLLOW_CM_in_pred_decl101 = new BitSet(new long[]{0x0008400071000440L});
    public static final BitSet FOLLOW_pred_param_in_pred_decl103 = new BitSet(new long[]{0x0000040000002000L});
    public static final BitSet FOLLOW_RP_in_pred_decl107 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_SC_in_pred_decl109 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_pred_param_type_in_pred_param137 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_CL_in_pred_param139 = new BitSet(new long[]{0x0000000004000000L});
    public static final BitSet FOLLOW_IDENTIFIER_in_pred_param141 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_par_pred_param_type_in_pred_param_type175 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_var_pred_param_type_in_pred_param_type185 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ARRAY_in_par_type204 = new BitSet(new long[]{0x0000000100000000L});
    public static final BitSet FOLLOW_LS_in_par_type206 = new BitSet(new long[]{0x0000000030000000L});
    public static final BitSet FOLLOW_index_set_in_par_type208 = new BitSet(new long[]{0x0000080000002000L});
    public static final BitSet FOLLOW_CM_in_par_type211 = new BitSet(new long[]{0x0000000030000000L});
    public static final BitSet FOLLOW_index_set_in_par_type213 = new BitSet(new long[]{0x0000080000002000L});
    public static final BitSet FOLLOW_RS_in_par_type217 = new BitSet(new long[]{0x0000002000000000L});
    public static final BitSet FOLLOW_OF_in_par_type219 = new BitSet(new long[]{0x0000400011000400L});
    public static final BitSet FOLLOW_par_type_u_in_par_type221 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_par_type_u_in_par_type247 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_BOOL_in_par_type_u279 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FLOAT_in_par_type_u289 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SET_in_par_type_u299 = new BitSet(new long[]{0x0000002000000000L});
    public static final BitSet FOLLOW_OF_in_par_type_u301 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_INT_in_par_type_u303 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INT_in_par_type_u313 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ARRAY_in_var_type332 = new BitSet(new long[]{0x0000000100000000L});
    public static final BitSet FOLLOW_LS_in_var_type334 = new BitSet(new long[]{0x0000000030000000L});
    public static final BitSet FOLLOW_index_set_in_var_type336 = new BitSet(new long[]{0x0000080000002000L});
    public static final BitSet FOLLOW_CM_in_var_type339 = new BitSet(new long[]{0x0000000030000000L});
    public static final BitSet FOLLOW_index_set_in_var_type341 = new BitSet(new long[]{0x0000080000002000L});
    public static final BitSet FOLLOW_RS_in_var_type345 = new BitSet(new long[]{0x0000002000000000L});
    public static final BitSet FOLLOW_OF_in_var_type347 = new BitSet(new long[]{0x0008000000000000L});
    public static final BitSet FOLLOW_VAR_in_var_type349 = new BitSet(new long[]{0x0000400071000400L});
    public static final BitSet FOLLOW_var_type_u_in_var_type351 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_VAR_in_var_type377 = new BitSet(new long[]{0x0000400071000400L});
    public static final BitSet FOLLOW_var_type_u_in_var_type379 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_BOOL_in_var_type_u411 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FLOAT_in_var_type_u421 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INT_in_var_type_u431 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INT_CONST_in_var_type_u441 = new BitSet(new long[]{0x0000000000020000L});
    public static final BitSet FOLLOW_DD_in_var_type_u443 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_INT_CONST_in_var_type_u445 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LB_in_var_type_u472 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_INT_CONST_in_var_type_u474 = new BitSet(new long[]{0x0000020000002000L});
    public static final BitSet FOLLOW_CM_in_var_type_u477 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_INT_CONST_in_var_type_u479 = new BitSet(new long[]{0x0000020000002000L});
    public static final BitSet FOLLOW_RB_in_var_type_u483 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SET_in_var_type_u507 = new BitSet(new long[]{0x0000002000000000L});
    public static final BitSet FOLLOW_OF_in_var_type_u509 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_INT_CONST_in_var_type_u511 = new BitSet(new long[]{0x0000000000020000L});
    public static final BitSet FOLLOW_DD_in_var_type_u513 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_INT_CONST_in_var_type_u515 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SET_in_var_type_u544 = new BitSet(new long[]{0x0000002000000000L});
    public static final BitSet FOLLOW_OF_in_var_type_u546 = new BitSet(new long[]{0x0000000040000000L});
    public static final BitSet FOLLOW_LB_in_var_type_u548 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_INT_CONST_in_var_type_u550 = new BitSet(new long[]{0x0000020000002000L});
    public static final BitSet FOLLOW_CM_in_var_type_u553 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_INT_CONST_in_var_type_u555 = new BitSet(new long[]{0x0000020000002000L});
    public static final BitSet FOLLOW_RB_in_var_type_u559 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_par_type_in_par_pred_param_type597 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type609 = new BitSet(new long[]{0x0000000000020000L});
    public static final BitSet FOLLOW_DD_in_par_pred_param_type611 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type613 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LB_in_par_pred_param_type638 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type640 = new BitSet(new long[]{0x0000020000002000L});
    public static final BitSet FOLLOW_CM_in_par_pred_param_type643 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type645 = new BitSet(new long[]{0x0000020000002000L});
    public static final BitSet FOLLOW_RB_in_par_pred_param_type649 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SET_in_par_pred_param_type673 = new BitSet(new long[]{0x0000002000000000L});
    public static final BitSet FOLLOW_OF_in_par_pred_param_type675 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type677 = new BitSet(new long[]{0x0000000000020000L});
    public static final BitSet FOLLOW_DD_in_par_pred_param_type679 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type681 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SET_in_par_pred_param_type710 = new BitSet(new long[]{0x0000002000000000L});
    public static final BitSet FOLLOW_OF_in_par_pred_param_type712 = new BitSet(new long[]{0x0000000040000000L});
    public static final BitSet FOLLOW_LB_in_par_pred_param_type714 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type716 = new BitSet(new long[]{0x0000020000002000L});
    public static final BitSet FOLLOW_CM_in_par_pred_param_type719 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type721 = new BitSet(new long[]{0x0000020000002000L});
    public static final BitSet FOLLOW_RB_in_par_pred_param_type725 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ARRAY_in_par_pred_param_type755 = new BitSet(new long[]{0x0000000100000000L});
    public static final BitSet FOLLOW_LS_in_par_pred_param_type757 = new BitSet(new long[]{0x0000000030000000L});
    public static final BitSet FOLLOW_index_set_in_par_pred_param_type759 = new BitSet(new long[]{0x0000080000002000L});
    public static final BitSet FOLLOW_CM_in_par_pred_param_type762 = new BitSet(new long[]{0x0000000030000000L});
    public static final BitSet FOLLOW_index_set_in_par_pred_param_type764 = new BitSet(new long[]{0x0000080000002000L});
    public static final BitSet FOLLOW_RS_in_par_pred_param_type768 = new BitSet(new long[]{0x0000002000000000L});
    public static final BitSet FOLLOW_OF_in_par_pred_param_type770 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type772 = new BitSet(new long[]{0x0000000000020000L});
    public static final BitSet FOLLOW_DD_in_par_pred_param_type774 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type776 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ARRAY_in_par_pred_param_type808 = new BitSet(new long[]{0x0000000100000000L});
    public static final BitSet FOLLOW_LS_in_par_pred_param_type810 = new BitSet(new long[]{0x0000000030000000L});
    public static final BitSet FOLLOW_index_set_in_par_pred_param_type812 = new BitSet(new long[]{0x0000080000002000L});
    public static final BitSet FOLLOW_CM_in_par_pred_param_type815 = new BitSet(new long[]{0x0000000030000000L});
    public static final BitSet FOLLOW_index_set_in_par_pred_param_type817 = new BitSet(new long[]{0x0000080000002000L});
    public static final BitSet FOLLOW_RS_in_par_pred_param_type821 = new BitSet(new long[]{0x0000002000000000L});
    public static final BitSet FOLLOW_OF_in_par_pred_param_type823 = new BitSet(new long[]{0x0000000040000000L});
    public static final BitSet FOLLOW_LB_in_par_pred_param_type825 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type827 = new BitSet(new long[]{0x0000020000002000L});
    public static final BitSet FOLLOW_CM_in_par_pred_param_type830 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type832 = new BitSet(new long[]{0x0000020000002000L});
    public static final BitSet FOLLOW_RB_in_par_pred_param_type836 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ARRAY_in_par_pred_param_type867 = new BitSet(new long[]{0x0000000100000000L});
    public static final BitSet FOLLOW_LS_in_par_pred_param_type869 = new BitSet(new long[]{0x0000000030000000L});
    public static final BitSet FOLLOW_index_set_in_par_pred_param_type871 = new BitSet(new long[]{0x0000080000002000L});
    public static final BitSet FOLLOW_CM_in_par_pred_param_type874 = new BitSet(new long[]{0x0000000030000000L});
    public static final BitSet FOLLOW_index_set_in_par_pred_param_type876 = new BitSet(new long[]{0x0000080000002000L});
    public static final BitSet FOLLOW_RS_in_par_pred_param_type880 = new BitSet(new long[]{0x0000002000000000L});
    public static final BitSet FOLLOW_OF_in_par_pred_param_type882 = new BitSet(new long[]{0x0000400000000000L});
    public static final BitSet FOLLOW_SET_in_par_pred_param_type884 = new BitSet(new long[]{0x0000002000000000L});
    public static final BitSet FOLLOW_OF_in_par_pred_param_type886 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type888 = new BitSet(new long[]{0x0000000000020000L});
    public static final BitSet FOLLOW_DD_in_par_pred_param_type890 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type892 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ARRAY_in_par_pred_param_type928 = new BitSet(new long[]{0x0000000100000000L});
    public static final BitSet FOLLOW_LS_in_par_pred_param_type930 = new BitSet(new long[]{0x0000000030000000L});
    public static final BitSet FOLLOW_index_set_in_par_pred_param_type932 = new BitSet(new long[]{0x0000080000002000L});
    public static final BitSet FOLLOW_CM_in_par_pred_param_type935 = new BitSet(new long[]{0x0000000030000000L});
    public static final BitSet FOLLOW_index_set_in_par_pred_param_type937 = new BitSet(new long[]{0x0000080000002000L});
    public static final BitSet FOLLOW_RS_in_par_pred_param_type941 = new BitSet(new long[]{0x0000002000000000L});
    public static final BitSet FOLLOW_OF_in_par_pred_param_type943 = new BitSet(new long[]{0x0000400000000000L});
    public static final BitSet FOLLOW_SET_in_par_pred_param_type945 = new BitSet(new long[]{0x0000002000000000L});
    public static final BitSet FOLLOW_OF_in_par_pred_param_type947 = new BitSet(new long[]{0x0000000040000000L});
    public static final BitSet FOLLOW_LB_in_par_pred_param_type949 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type951 = new BitSet(new long[]{0x0000020000002000L});
    public static final BitSet FOLLOW_CM_in_par_pred_param_type954 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type956 = new BitSet(new long[]{0x0000020000002000L});
    public static final BitSet FOLLOW_RB_in_par_pred_param_type960 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_var_type_in_var_pred_param_type1005 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_VAR_in_var_pred_param_type1028 = new BitSet(new long[]{0x0000400000000000L});
    public static final BitSet FOLLOW_SET_in_var_pred_param_type1030 = new BitSet(new long[]{0x0000002000000000L});
    public static final BitSet FOLLOW_OF_in_var_pred_param_type1032 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_INT_in_var_pred_param_type1034 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ARRAY_in_var_pred_param_type1057 = new BitSet(new long[]{0x0000000100000000L});
    public static final BitSet FOLLOW_LS_in_var_pred_param_type1059 = new BitSet(new long[]{0x0000000030000000L});
    public static final BitSet FOLLOW_index_set_in_var_pred_param_type1061 = new BitSet(new long[]{0x0000080000002000L});
    public static final BitSet FOLLOW_CM_in_var_pred_param_type1064 = new BitSet(new long[]{0x0000000030000000L});
    public static final BitSet FOLLOW_index_set_in_var_pred_param_type1066 = new BitSet(new long[]{0x0000080000002000L});
    public static final BitSet FOLLOW_RS_in_var_pred_param_type1070 = new BitSet(new long[]{0x0000002000000000L});
    public static final BitSet FOLLOW_OF_in_var_pred_param_type1072 = new BitSet(new long[]{0x0008000000000000L});
    public static final BitSet FOLLOW_VAR_in_var_pred_param_type1074 = new BitSet(new long[]{0x0000400000000000L});
    public static final BitSet FOLLOW_SET_in_var_pred_param_type1076 = new BitSet(new long[]{0x0000002000000000L});
    public static final BitSet FOLLOW_OF_in_var_pred_param_type1078 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_INT_in_var_pred_param_type1080 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INT_CONST_in_index_set1119 = new BitSet(new long[]{0x0000000000020000L});
    public static final BitSet FOLLOW_DD_in_index_set1121 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_INT_CONST_in_index_set1123 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INT_in_index_set1152 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LB_in_expr1184 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_INT_CONST_in_expr1186 = new BitSet(new long[]{0x0000020000002000L});
    public static final BitSet FOLLOW_CM_in_expr1189 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_INT_CONST_in_expr1191 = new BitSet(new long[]{0x0000020000002000L});
    public static final BitSet FOLLOW_RB_in_expr1195 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_bool_const_in_expr1219 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INT_CONST_in_expr1229 = new BitSet(new long[]{0x0000000000020002L});
    public static final BitSet FOLLOW_DD_in_expr1232 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_INT_CONST_in_expr1234 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LS_in_expr1246 = new BitSet(new long[]{0x0003080164800000L});
    public static final BitSet FOLLOW_expr_in_expr1249 = new BitSet(new long[]{0x0000080000002000L});
    public static final BitSet FOLLOW_CM_in_expr1252 = new BitSet(new long[]{0x0003000164800000L});
    public static final BitSet FOLLOW_expr_in_expr1254 = new BitSet(new long[]{0x0000080000002000L});
    public static final BitSet FOLLOW_RS_in_expr1260 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_id_expr_in_expr1288 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_in_expr1298 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENTIFIER_in_id_expr1319 = new BitSet(new long[]{0x0000000180000002L});
    public static final BitSet FOLLOW_LP_in_id_expr1323 = new BitSet(new long[]{0x0003000164800000L});
    public static final BitSet FOLLOW_expr_in_id_expr1325 = new BitSet(new long[]{0x0000040000002000L});
    public static final BitSet FOLLOW_CM_in_id_expr1328 = new BitSet(new long[]{0x0003000164800000L});
    public static final BitSet FOLLOW_expr_in_id_expr1330 = new BitSet(new long[]{0x0000040000002000L});
    public static final BitSet FOLLOW_RP_in_id_expr1334 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LS_in_id_expr1338 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_INT_CONST_in_id_expr1340 = new BitSet(new long[]{0x0000080000000000L});
    public static final BitSet FOLLOW_RS_in_id_expr1342 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_par_type_in_param_decl1362 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_CL_in_param_decl1364 = new BitSet(new long[]{0x0000000004000000L});
    public static final BitSet FOLLOW_IDENTIFIER_in_param_decl1366 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_EQ_in_param_decl1368 = new BitSet(new long[]{0x0003000164800000L});
    public static final BitSet FOLLOW_expr_in_param_decl1370 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_SC_in_param_decl1372 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_var_type_in_var_decl1400 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_CL_in_var_decl1402 = new BitSet(new long[]{0x0000000004000000L});
    public static final BitSet FOLLOW_IDENTIFIER_in_var_decl1404 = new BitSet(new long[]{0x0000200000090000L});
    public static final BitSet FOLLOW_annotations_in_var_decl1406 = new BitSet(new long[]{0x0000200000080000L});
    public static final BitSet FOLLOW_EQ_in_var_decl1409 = new BitSet(new long[]{0x0003000164800000L});
    public static final BitSet FOLLOW_expr_in_var_decl1411 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_SC_in_var_decl1415 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CONSTRAINT_in_constraint1445 = new BitSet(new long[]{0x0000000004000000L});
    public static final BitSet FOLLOW_IDENTIFIER_in_constraint1447 = new BitSet(new long[]{0x0000000080000000L});
    public static final BitSet FOLLOW_LP_in_constraint1449 = new BitSet(new long[]{0x0003000164800000L});
    public static final BitSet FOLLOW_expr_in_constraint1451 = new BitSet(new long[]{0x0000040000002000L});
    public static final BitSet FOLLOW_CM_in_constraint1454 = new BitSet(new long[]{0x0003000164800000L});
    public static final BitSet FOLLOW_expr_in_constraint1456 = new BitSet(new long[]{0x0000040000002000L});
    public static final BitSet FOLLOW_RP_in_constraint1460 = new BitSet(new long[]{0x0000200000010000L});
    public static final BitSet FOLLOW_annotations_in_constraint1462 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_SC_in_constraint1464 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SOLVE_in_solve_goal1492 = new BitSet(new long[]{0x0000100600010000L});
    public static final BitSet FOLLOW_annotations_in_solve_goal1495 = new BitSet(new long[]{0x0000100600000000L});
    public static final BitSet FOLLOW_resolution_in_solve_goal1497 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_SC_in_solve_goal1499 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MINIMIZE_in_resolution1516 = new BitSet(new long[]{0x0003000164800000L});
    public static final BitSet FOLLOW_expr_in_resolution1519 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MAXIMIZE_in_resolution1529 = new BitSet(new long[]{0x0003000164800000L});
    public static final BitSet FOLLOW_expr_in_resolution1532 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SATISFY_in_resolution1542 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DC_in_annotations1563 = new BitSet(new long[]{0x0000000004000000L});
    public static final BitSet FOLLOW_annotation_in_annotations1565 = new BitSet(new long[]{0x0000000000010002L});
    public static final BitSet FOLLOW_IDENTIFIER_in_annotation1600 = new BitSet(new long[]{0x0000000080000002L});
    public static final BitSet FOLLOW_LP_in_annotation1603 = new BitSet(new long[]{0x0003000164800000L});
    public static final BitSet FOLLOW_expr_in_annotation1605 = new BitSet(new long[]{0x0000040000002000L});
    public static final BitSet FOLLOW_CM_in_annotation1608 = new BitSet(new long[]{0x0003000164800000L});
    public static final BitSet FOLLOW_expr_in_annotation1610 = new BitSet(new long[]{0x0000040000002000L});
    public static final BitSet FOLLOW_RP_in_annotation1614 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TRUE_in_bool_const1655 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FALSE_in_bool_const1666 = new BitSet(new long[]{0x0000000000000002L});

}