// $ANTLR 3.4 parser/flatzinc/FlatzincWalker.g 2012-11-29 14:33:19

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

import choco.kernel.ResolutionPolicy;
import gnu.trove.map.hash.THashMap;
import org.antlr.runtime.*;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.TreeNodeStream;
import org.antlr.runtime.tree.TreeParser;
import org.antlr.runtime.tree.TreeRuleReturnScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import parser.flatzinc.ast.*;
import parser.flatzinc.ast.declaration.*;
import parser.flatzinc.ast.expression.*;
import solver.Solver;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"all", "warnings", "unchecked"})
public class FlatzincWalker extends TreeParser {
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
    public TreeParser[] getDelegates() {
        return new TreeParser[]{};
    }

    // delegators


    public FlatzincWalker(TreeNodeStream input) {
        this(input, new RecognizerSharedState());
    }

    public FlatzincWalker(TreeNodeStream input, RecognizerSharedState state) {
        super(input, state);
    }

    public String[] getTokenNames() {
        return FlatzincWalker.tokenNames;
    }

    public String getGrammarFileName() {
        return "parser/flatzinc/FlatzincWalker.g";
    }


    // The flatzinc logger -- 'System.out/err' is fobidden!
    protected static final Logger LOGGER = LoggerFactory.getLogger("fzn");

    // maintains map between name and objects
    public THashMap<String, Object> map;

    // the solver
    public Solver mSolver;
    // goal configuration
    public GoalConf gc;

    // the layout dedicated to pretty print message wrt to fzn recommendations
    public final FZNLayout mLayout = new FZNLayout();


    // $ANTLR start "flatzinc_model"
    // parser/flatzinc/FlatzincWalker.g:79:1: flatzinc_model[Solver aSolver, THashMap<String, Object> map, GoalConf gc] : ( pred_decl )* ( param_decl )* ( var_decl )* ( constraint )* solve_goal ;
    public final void flatzinc_model(Solver aSolver, THashMap<String, Object> map, GoalConf gc) throws RecognitionException {
        try {
            // parser/flatzinc/FlatzincWalker.g:80:2: ( ( pred_decl )* ( param_decl )* ( var_decl )* ( constraint )* solve_goal )
            // parser/flatzinc/FlatzincWalker.g:81:2: ( pred_decl )* ( param_decl )* ( var_decl )* ( constraint )* solve_goal
            {

                this.mSolver = aSolver;
                this.map = map;
                this.gc = gc;


                // parser/flatzinc/FlatzincWalker.g:86:5: ( pred_decl )*
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
                            // parser/flatzinc/FlatzincWalker.g:86:6: pred_decl
                        {
                            pushFollow(FOLLOW_pred_decl_in_flatzinc_model52);
                            pred_decl();

                            state._fsp--;


                        }
                        break;

                        default:
                            break loop1;
                    }
                } while (true);


                // parser/flatzinc/FlatzincWalker.g:86:18: ( param_decl )*
                loop2:
                do {
                    int alt2 = 2;
                    switch (input.LA(1)) {
                        case PAR: {
                            alt2 = 1;
                        }
                        break;

                    }

                    switch (alt2) {
                        case 1:
                            // parser/flatzinc/FlatzincWalker.g:86:19: param_decl
                        {
                            pushFollow(FOLLOW_param_decl_in_flatzinc_model57);
                            param_decl();

                            state._fsp--;


                        }
                        break;

                        default:
                            break loop2;
                    }
                } while (true);


                // parser/flatzinc/FlatzincWalker.g:86:32: ( var_decl )*
                loop3:
                do {
                    int alt3 = 2;
                    switch (input.LA(1)) {
                        case VAR: {
                            alt3 = 1;
                        }
                        break;

                    }

                    switch (alt3) {
                        case 1:
                            // parser/flatzinc/FlatzincWalker.g:86:33: var_decl
                        {
                            pushFollow(FOLLOW_var_decl_in_flatzinc_model62);
                            var_decl();

                            state._fsp--;


                        }
                        break;

                        default:
                            break loop3;
                    }
                } while (true);


                // parser/flatzinc/FlatzincWalker.g:86:44: ( constraint )*
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
                            // parser/flatzinc/FlatzincWalker.g:86:45: constraint
                        {
                            pushFollow(FOLLOW_constraint_in_flatzinc_model67);
                            constraint();

                            state._fsp--;


                        }
                        break;

                        default:
                            break loop4;
                    }
                } while (true);


                pushFollow(FOLLOW_solve_goal_in_flatzinc_model71);
                solve_goal();

                state._fsp--;


                if (LoggerFactory.getLogger("fzn").isInfoEnabled()) {
                    mLayout.setSearchLoop(mSolver.getSearchLoop());
                }


            }

        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
        } finally {
            // do for sure before leaving
        }
        return;
    }
    // $ANTLR end "flatzinc_model"


    // $ANTLR start "par_type"
    // parser/flatzinc/FlatzincWalker.g:94:1: par_type returns [Declaration decl] : ( ^( ARRPAR (d= index_set )+ p= par_type_u ) | ^( APAR p= par_type_u ) );
    public final Declaration par_type() throws RecognitionException {
        Declaration decl = null;


        Declaration d = null;

        Declaration p = null;


        try {
            // parser/flatzinc/FlatzincWalker.g:95:5: ( ^( ARRPAR (d= index_set )+ p= par_type_u ) | ^( APAR p= par_type_u ) )
            int alt6 = 2;
            switch (input.LA(1)) {
                case ARRPAR: {
                    alt6 = 1;
                }
                break;
                case APAR: {
                    alt6 = 2;
                }
                break;
                default:
                    NoViableAltException nvae =
                            new NoViableAltException("", 6, 0, input);

                    throw nvae;

            }

            switch (alt6) {
                case 1:
                    // parser/flatzinc/FlatzincWalker.g:96:5: ^( ARRPAR (d= index_set )+ p= par_type_u )
                {

                    List<Declaration> decls = new ArrayList();


                    match(input, ARRPAR, FOLLOW_ARRPAR_in_par_type110);

                    match(input, Token.DOWN, null);
                    // parser/flatzinc/FlatzincWalker.g:99:18: (d= index_set )+
                    int cnt5 = 0;
                    loop5:
                    do {
                        int alt5 = 2;
                        switch (input.LA(1)) {
                            case INDEX: {
                                alt5 = 1;
                            }
                            break;

                        }

                        switch (alt5) {
                            case 1:
                                // parser/flatzinc/FlatzincWalker.g:99:19: d= index_set
                            {
                                pushFollow(FOLLOW_index_set_in_par_type115);
                                d = index_set();

                                state._fsp--;


                                decls.add(d);

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


                    pushFollow(FOLLOW_par_type_u_in_par_type122);
                    p = par_type_u();

                    state._fsp--;


                    match(input, Token.UP, null);


                    decl = new DArray(decls, p);


                }
                break;
                case 2:
                    // parser/flatzinc/FlatzincWalker.g:103:9: ^( APAR p= par_type_u )
                {
                    match(input, APAR, FOLLOW_APAR_in_par_type140);

                    match(input, Token.DOWN, null);
                    pushFollow(FOLLOW_par_type_u_in_par_type144);
                    p = par_type_u();

                    state._fsp--;


                    match(input, Token.UP, null);


                    decl = p;


                }
                break;

            }
        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
        } finally {
            // do for sure before leaving
        }
        return decl;
    }
    // $ANTLR end "par_type"


    // $ANTLR start "par_type_u"
    // parser/flatzinc/FlatzincWalker.g:109:1: par_type_u returns [Declaration decl] : ( BOOL | FLOAT | SET OF INT | INT );
    public final Declaration par_type_u() throws RecognitionException {
        Declaration decl = null;


        try {
            // parser/flatzinc/FlatzincWalker.g:110:5: ( BOOL | FLOAT | SET OF INT | INT )
            int alt7 = 4;
            switch (input.LA(1)) {
                case BOOL: {
                    alt7 = 1;
                }
                break;
                case FLOAT: {
                    alt7 = 2;
                }
                break;
                case SET: {
                    alt7 = 3;
                }
                break;
                case INT: {
                    alt7 = 4;
                }
                break;
                default:
                    NoViableAltException nvae =
                            new NoViableAltException("", 7, 0, input);

                    throw nvae;

            }

            switch (alt7) {
                case 1:
                    // parser/flatzinc/FlatzincWalker.g:110:9: BOOL
                {
                    match(input, BOOL, FOLLOW_BOOL_in_par_type_u174);


                    decl = DBool.me;


                }
                break;
                case 2:
                    // parser/flatzinc/FlatzincWalker.g:114:9: FLOAT
                {
                    match(input, FLOAT, FOLLOW_FLOAT_in_par_type_u190);


                    decl = DFloat.me;


                }
                break;
                case 3:
                    // parser/flatzinc/FlatzincWalker.g:118:9: SET OF INT
                {
                    match(input, SET, FOLLOW_SET_in_par_type_u206);

                    match(input, OF, FOLLOW_OF_in_par_type_u208);

                    match(input, INT, FOLLOW_INT_in_par_type_u210);


                    decl = DSetOfInt.me;


                }
                break;
                case 4:
                    // parser/flatzinc/FlatzincWalker.g:122:9: INT
                {
                    match(input, INT, FOLLOW_INT_in_par_type_u226);


                    decl = DInt.me;


                }
                break;

            }
        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
        } finally {
            // do for sure before leaving
        }
        return decl;
    }
    // $ANTLR end "par_type_u"


    // $ANTLR start "var_type"
    // parser/flatzinc/FlatzincWalker.g:128:1: var_type returns [Declaration decl] : ( ^( ARRVAR (d= index_set )+ d= var_type_u ) | ^( AVAR d= var_type_u ) );
    public final Declaration var_type() throws RecognitionException {
        Declaration decl = null;


        Declaration d = null;


        try {
            // parser/flatzinc/FlatzincWalker.g:129:5: ( ^( ARRVAR (d= index_set )+ d= var_type_u ) | ^( AVAR d= var_type_u ) )
            int alt9 = 2;
            switch (input.LA(1)) {
                case ARRVAR: {
                    alt9 = 1;
                }
                break;
                case AVAR: {
                    alt9 = 2;
                }
                break;
                default:
                    NoViableAltException nvae =
                            new NoViableAltException("", 9, 0, input);

                    throw nvae;

            }

            switch (alt9) {
                case 1:
                    // parser/flatzinc/FlatzincWalker.g:130:5: ^( ARRVAR (d= index_set )+ d= var_type_u )
                {

                    List<Declaration> decls = new ArrayList();


                    match(input, ARRVAR, FOLLOW_ARRVAR_in_var_type267);

                    match(input, Token.DOWN, null);
                    // parser/flatzinc/FlatzincWalker.g:133:14: (d= index_set )+
                    int cnt8 = 0;
                    loop8:
                    do {
                        int alt8 = 2;
                        switch (input.LA(1)) {
                            case INDEX: {
                                alt8 = 1;
                            }
                            break;

                        }

                        switch (alt8) {
                            case 1:
                                // parser/flatzinc/FlatzincWalker.g:133:15: d= index_set
                            {
                                pushFollow(FOLLOW_index_set_in_var_type272);
                                d = index_set();

                                state._fsp--;


                                decls.add(d);

                            }
                            break;

                            default:
                                if (cnt8 >= 1) break loop8;
                                EarlyExitException eee =
                                        new EarlyExitException(8, input);
                                throw eee;
                        }
                        cnt8++;
                    } while (true);


                    pushFollow(FOLLOW_var_type_u_in_var_type279);
                    d = var_type_u();

                    state._fsp--;


                    match(input, Token.UP, null);


                    decl = new DArray(decls, d);


                }
                break;
                case 2:
                    // parser/flatzinc/FlatzincWalker.g:137:9: ^( AVAR d= var_type_u )
                {
                    match(input, AVAR, FOLLOW_AVAR_in_var_type297);

                    match(input, Token.DOWN, null);
                    pushFollow(FOLLOW_var_type_u_in_var_type301);
                    d = var_type_u();

                    state._fsp--;


                    match(input, Token.UP, null);


                    decl = d;


                }
                break;

            }
        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
        } finally {
            // do for sure before leaving
        }
        return decl;
    }
    // $ANTLR end "var_type"


    // $ANTLR start "var_type_u"
    // parser/flatzinc/FlatzincWalker.g:143:1: var_type_u returns [Declaration decl] : ( BOOL | FLOAT | INT | ^( DD i1= INT_CONST i2= INT_CONST ) | ^( CM (i= INT_CONST )+ ) | ^( SET ^( DD i1= INT_CONST i2= INT_CONST ) ) | ^( SET ^( CM (i= INT_CONST )+ ) ) );
    public final Declaration var_type_u() throws RecognitionException {
        Declaration decl = null;


        CommonTree i1 = null;
        CommonTree i2 = null;
        CommonTree i = null;

        try {
            // parser/flatzinc/FlatzincWalker.g:144:5: ( BOOL | FLOAT | INT | ^( DD i1= INT_CONST i2= INT_CONST ) | ^( CM (i= INT_CONST )+ ) | ^( SET ^( DD i1= INT_CONST i2= INT_CONST ) ) | ^( SET ^( CM (i= INT_CONST )+ ) ) )
            int alt12 = 7;
            switch (input.LA(1)) {
                case BOOL: {
                    alt12 = 1;
                }
                break;
                case FLOAT: {
                    alt12 = 2;
                }
                break;
                case INT: {
                    alt12 = 3;
                }
                break;
                case DD: {
                    alt12 = 4;
                }
                break;
                case CM: {
                    alt12 = 5;
                }
                break;
                case SET: {
                    switch (input.LA(2)) {
                        case DOWN: {
                            switch (input.LA(3)) {
                                case DD: {
                                    alt12 = 6;
                                }
                                break;
                                case CM: {
                                    alt12 = 7;
                                }
                                break;
                                default:
                                    NoViableAltException nvae =
                                            new NoViableAltException("", 12, 7, input);

                                    throw nvae;

                            }

                        }
                        break;
                        default:
                            NoViableAltException nvae =
                                    new NoViableAltException("", 12, 6, input);

                            throw nvae;

                    }

                }
                break;
                default:
                    NoViableAltException nvae =
                            new NoViableAltException("", 12, 0, input);

                    throw nvae;

            }

            switch (alt12) {
                case 1:
                    // parser/flatzinc/FlatzincWalker.g:144:9: BOOL
                {
                    match(input, BOOL, FOLLOW_BOOL_in_var_type_u332);


                    decl = DBool.me;


                }
                break;
                case 2:
                    // parser/flatzinc/FlatzincWalker.g:148:9: FLOAT
                {
                    match(input, FLOAT, FOLLOW_FLOAT_in_var_type_u348);


                    decl = DFloat.me;


                }
                break;
                case 3:
                    // parser/flatzinc/FlatzincWalker.g:152:9: INT
                {
                    match(input, INT, FOLLOW_INT_in_var_type_u364);


                    decl = DInt.me;


                }
                break;
                case 4:
                    // parser/flatzinc/FlatzincWalker.g:156:9: ^( DD i1= INT_CONST i2= INT_CONST )
                {
                    match(input, DD, FOLLOW_DD_in_var_type_u381);

                    match(input, Token.DOWN, null);
                    i1 = (CommonTree) match(input, INT_CONST, FOLLOW_INT_CONST_in_var_type_u385);

                    i2 = (CommonTree) match(input, INT_CONST, FOLLOW_INT_CONST_in_var_type_u389);

                    match(input, Token.UP, null);


                    decl = new DInt2(EInt.make((i1 != null ? i1.getText() : null)), EInt.make((i2 != null ? i2.getText() : null)));


                }
                break;
                case 5:
                    // parser/flatzinc/FlatzincWalker.g:166:5: ^( CM (i= INT_CONST )+ )
                {

                    ArrayList<EInt> values = new ArrayList();


                    match(input, CM, FOLLOW_CM_in_var_type_u424);

                    match(input, Token.DOWN, null);
                    // parser/flatzinc/FlatzincWalker.g:169:14: (i= INT_CONST )+
                    int cnt10 = 0;
                    loop10:
                    do {
                        int alt10 = 2;
                        switch (input.LA(1)) {
                            case INT_CONST: {
                                alt10 = 1;
                            }
                            break;

                        }

                        switch (alt10) {
                            case 1:
                                // parser/flatzinc/FlatzincWalker.g:169:15: i= INT_CONST
                            {
                                i = (CommonTree) match(input, INT_CONST, FOLLOW_INT_CONST_in_var_type_u429);

                                values.add(EInt.make((i != null ? i.getText() : null)));

                            }
                            break;

                            default:
                                if (cnt10 >= 1) break loop10;
                                EarlyExitException eee =
                                        new EarlyExitException(10, input);
                                throw eee;
                        }
                        cnt10++;
                    } while (true);


                    match(input, Token.UP, null);


                    decl = new DManyInt(values);


                }
                break;
                case 6:
                    // parser/flatzinc/FlatzincWalker.g:173:9: ^( SET ^( DD i1= INT_CONST i2= INT_CONST ) )
                {
                    match(input, SET, FOLLOW_SET_in_var_type_u450);

                    match(input, Token.DOWN, null);
                    match(input, DD, FOLLOW_DD_in_var_type_u453);

                    match(input, Token.DOWN, null);
                    i1 = (CommonTree) match(input, INT_CONST, FOLLOW_INT_CONST_in_var_type_u457);

                    i2 = (CommonTree) match(input, INT_CONST, FOLLOW_INT_CONST_in_var_type_u461);

                    match(input, Token.UP, null);


                    match(input, Token.UP, null);


                    decl = new DSet(new DInt2(EInt.make((i1 != null ? i1.getText() : null)), EInt.make((i2 != null ? i2.getText() : null))));


                }
                break;
                case 7:
                    // parser/flatzinc/FlatzincWalker.g:178:5: ^( SET ^( CM (i= INT_CONST )+ ) )
                {

                    ArrayList<EInt> values = new ArrayList();


                    match(input, SET, FOLLOW_SET_in_var_type_u491);

                    match(input, Token.DOWN, null);
                    match(input, CM, FOLLOW_CM_in_var_type_u494);

                    match(input, Token.DOWN, null);
                    // parser/flatzinc/FlatzincWalker.g:181:19: (i= INT_CONST )+
                    int cnt11 = 0;
                    loop11:
                    do {
                        int alt11 = 2;
                        switch (input.LA(1)) {
                            case INT_CONST: {
                                alt11 = 1;
                            }
                            break;

                        }

                        switch (alt11) {
                            case 1:
                                // parser/flatzinc/FlatzincWalker.g:181:20: i= INT_CONST
                            {
                                i = (CommonTree) match(input, INT_CONST, FOLLOW_INT_CONST_in_var_type_u499);

                                values.add(EInt.make((i != null ? i.getText() : null)));

                            }
                            break;

                            default:
                                if (cnt11 >= 1) break loop11;
                                EarlyExitException eee =
                                        new EarlyExitException(11, input);
                                throw eee;
                        }
                        cnt11++;
                    } while (true);


                    match(input, Token.UP, null);


                    match(input, Token.UP, null);


                    decl = new DSet(new DManyInt(values));


                }
                break;

            }
        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
        } finally {
            // do for sure before leaving
        }
        return decl;
    }
    // $ANTLR end "var_type_u"


    // $ANTLR start "index_set"
    // parser/flatzinc/FlatzincWalker.g:187:1: index_set returns [Declaration decl] : ( ^( INDEX ^( DD i1= INT_CONST i2= INT_CONST ) ) | ^( INDEX INT ) );
    public final Declaration index_set() throws RecognitionException {
        Declaration decl = null;


        CommonTree i1 = null;
        CommonTree i2 = null;

        try {
            // parser/flatzinc/FlatzincWalker.g:188:5: ( ^( INDEX ^( DD i1= INT_CONST i2= INT_CONST ) ) | ^( INDEX INT ) )
            int alt13 = 2;
            switch (input.LA(1)) {
                case INDEX: {
                    switch (input.LA(2)) {
                        case DOWN: {
                            switch (input.LA(3)) {
                                case DD: {
                                    alt13 = 1;
                                }
                                break;
                                case INT: {
                                    alt13 = 2;
                                }
                                break;
                                default:
                                    NoViableAltException nvae =
                                            new NoViableAltException("", 13, 2, input);

                                    throw nvae;

                            }

                        }
                        break;
                        default:
                            NoViableAltException nvae =
                                    new NoViableAltException("", 13, 1, input);

                            throw nvae;

                    }

                }
                break;
                default:
                    NoViableAltException nvae =
                            new NoViableAltException("", 13, 0, input);

                    throw nvae;

            }

            switch (alt13) {
                case 1:
                    // parser/flatzinc/FlatzincWalker.g:188:9: ^( INDEX ^( DD i1= INT_CONST i2= INT_CONST ) )
                {
                    match(input, INDEX, FOLLOW_INDEX_in_index_set534);

                    match(input, Token.DOWN, null);
                    match(input, DD, FOLLOW_DD_in_index_set537);

                    match(input, Token.DOWN, null);
                    i1 = (CommonTree) match(input, INT_CONST, FOLLOW_INT_CONST_in_index_set541);

                    i2 = (CommonTree) match(input, INT_CONST, FOLLOW_INT_CONST_in_index_set545);

                    match(input, Token.UP, null);


                    match(input, Token.UP, null);


                    decl = new DInt2(EInt.make((i1 != null ? i1.getText() : null)), EInt.make((i2 != null ? i2.getText() : null)));


                }
                break;
                case 2:
                    // parser/flatzinc/FlatzincWalker.g:192:9: ^( INDEX INT )
                {
                    match(input, INDEX, FOLLOW_INDEX_in_index_set564);

                    match(input, Token.DOWN, null);
                    match(input, INT, FOLLOW_INT_in_index_set566);

                    match(input, Token.UP, null);


                    decl = DInt.me;


                }
                break;

            }
        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
        } finally {
            // do for sure before leaving
        }
        return decl;
    }
    // $ANTLR end "index_set"


    // $ANTLR start "expr"
    // parser/flatzinc/FlatzincWalker.g:198:1: expr returns [Expression exp] : ( LB RB | LB (i= INT_CONST )+ RB |b= bool_const |i1= INT_CONST ( DD i2= INT_CONST )? | ^( EXPR LS (e= expr )* RS ) |e= id_expr | STRING );
    public final Expression expr() throws RecognitionException {
        Expression exp = null;


        CommonTree i = null;
        CommonTree i1 = null;
        CommonTree i2 = null;
        CommonTree STRING1 = null;
        boolean b = false;

        Expression e = null;


        try {
            // parser/flatzinc/FlatzincWalker.g:199:5: ( LB RB | LB (i= INT_CONST )+ RB |b= bool_const |i1= INT_CONST ( DD i2= INT_CONST )? | ^( EXPR LS (e= expr )* RS ) |e= id_expr | STRING )
            int alt17 = 7;
            switch (input.LA(1)) {
                case LB: {
                    switch (input.LA(2)) {
                        case RB: {
                            alt17 = 1;
                        }
                        break;
                        case INT_CONST: {
                            alt17 = 2;
                        }
                        break;
                        default:
                            NoViableAltException nvae =
                                    new NoViableAltException("", 17, 1, input);

                            throw nvae;

                    }

                }
                break;
                case FALSE:
                case TRUE: {
                    alt17 = 3;
                }
                break;
                case INT_CONST: {
                    alt17 = 4;
                }
                break;
                case EXPR: {
                    alt17 = 5;
                }
                break;
                case IDENTIFIER: {
                    alt17 = 6;
                }
                break;
                case STRING: {
                    alt17 = 7;
                }
                break;
                default:
                    NoViableAltException nvae =
                            new NoViableAltException("", 17, 0, input);

                    throw nvae;

            }

            switch (alt17) {
                case 1:
                    // parser/flatzinc/FlatzincWalker.g:199:9: LB RB
                {
                    match(input, LB, FOLLOW_LB_in_expr598);

                    match(input, RB, FOLLOW_RB_in_expr600);


                    exp = new ESetList(new ArrayList());


                }
                break;
                case 2:
                    // parser/flatzinc/FlatzincWalker.g:204:5: LB (i= INT_CONST )+ RB
                {

                    ArrayList<EInt> values = new ArrayList();


                    match(input, LB, FOLLOW_LB_in_expr628);

                    // parser/flatzinc/FlatzincWalker.g:207:12: (i= INT_CONST )+
                    int cnt14 = 0;
                    loop14:
                    do {
                        int alt14 = 2;
                        switch (input.LA(1)) {
                            case INT_CONST: {
                                alt14 = 1;
                            }
                            break;

                        }

                        switch (alt14) {
                            case 1:
                                // parser/flatzinc/FlatzincWalker.g:207:13: i= INT_CONST
                            {
                                i = (CommonTree) match(input, INT_CONST, FOLLOW_INT_CONST_in_expr633);

                                values.add(EInt.make((i != null ? i.getText() : null)));

                            }
                            break;

                            default:
                                if (cnt14 >= 1) break loop14;
                                EarlyExitException eee =
                                        new EarlyExitException(14, input);
                                throw eee;
                        }
                        cnt14++;
                    } while (true);


                    match(input, RB, FOLLOW_RB_in_expr638);


                    exp = new ESetList(values);


                }
                break;
                case 3:
                    // parser/flatzinc/FlatzincWalker.g:211:9: b= bool_const
                {
                    pushFollow(FOLLOW_bool_const_in_expr656);
                    b = bool_const();

                    state._fsp--;


                    exp = EBool.make(b);


                }
                break;
                case 4:
                    // parser/flatzinc/FlatzincWalker.g:215:9: i1= INT_CONST ( DD i2= INT_CONST )?
                {
                    i1 = (CommonTree) match(input, INT_CONST, FOLLOW_INT_CONST_in_expr674);

                    // parser/flatzinc/FlatzincWalker.g:215:22: ( DD i2= INT_CONST )?
                    int alt15 = 2;
                    switch (input.LA(1)) {
                        case DD: {
                            alt15 = 1;
                        }
                        break;
                    }

                    switch (alt15) {
                        case 1:
                            // parser/flatzinc/FlatzincWalker.g:215:23: DD i2= INT_CONST
                        {
                            match(input, DD, FOLLOW_DD_in_expr677);

                            i2 = (CommonTree) match(input, INT_CONST, FOLLOW_INT_CONST_in_expr681);

                        }
                        break;

                    }


                    if (i2 == null) {
                        exp = EInt.make((i1 != null ? i1.getText() : null));
                    } else {
                        exp = new ESetBounds(EInt.make((i1 != null ? i1.getText() : null)), EInt.make((i2 != null ? i2.getText() : null)));
                    }


                }
                break;
                case 5:
                    // parser/flatzinc/FlatzincWalker.g:224:5: ^( EXPR LS (e= expr )* RS )
                {

                    ArrayList<Expression> exps = new ArrayList();


                    match(input, EXPR, FOLLOW_EXPR_in_expr711);

                    match(input, Token.DOWN, null);
                    match(input, LS, FOLLOW_LS_in_expr713);

                    // parser/flatzinc/FlatzincWalker.g:227:18: (e= expr )*
                    loop16:
                    do {
                        int alt16 = 2;
                        switch (input.LA(1)) {
                            case EXPR:
                            case FALSE:
                            case IDENTIFIER:
                            case INT_CONST:
                            case LB:
                            case STRING:
                            case TRUE: {
                                alt16 = 1;
                            }
                            break;

                        }

                        switch (alt16) {
                            case 1:
                                // parser/flatzinc/FlatzincWalker.g:227:19: e= expr
                            {
                                pushFollow(FOLLOW_expr_in_expr718);
                                e = expr();

                                state._fsp--;


                                exps.add(e);

                            }
                            break;

                            default:
                                break loop16;
                        }
                    } while (true);


                    match(input, RS, FOLLOW_RS_in_expr723);

                    match(input, Token.UP, null);


                    if (exps.size() > 0) {
                        exp = new EArray(exps);
                    } else {
                        exp = new EArray();
                    }


                }
                break;
                case 6:
                    // parser/flatzinc/FlatzincWalker.g:235:9: e= id_expr
                {
                    pushFollow(FOLLOW_id_expr_in_expr742);
                    e = id_expr();

                    state._fsp--;


                    exp = e;


                }
                break;
                case 7:
                    // parser/flatzinc/FlatzincWalker.g:239:9: STRING
                {
                    STRING1 = (CommonTree) match(input, STRING, FOLLOW_STRING_in_expr758);


                    exp = new EString((STRING1 != null ? STRING1.getText() : null));


                }
                break;

            }
        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
        } finally {
            // do for sure before leaving
        }
        return exp;
    }
    // $ANTLR end "expr"


    // $ANTLR start "id_expr"
    // parser/flatzinc/FlatzincWalker.g:263:1: id_expr returns [Expression exp] : IDENTIFIER ( ( LP e= expr ( CM e= expr )* RP ) | ( LS i= INT_CONST RS ) )? ;
    public final Expression id_expr() throws RecognitionException {
        Expression exp = null;


        CommonTree i = null;
        CommonTree IDENTIFIER2 = null;
        Expression e = null;


        try {
            // parser/flatzinc/FlatzincWalker.g:264:5: ( IDENTIFIER ( ( LP e= expr ( CM e= expr )* RP ) | ( LS i= INT_CONST RS ) )? )
            // parser/flatzinc/FlatzincWalker.g:265:5: IDENTIFIER ( ( LP e= expr ( CM e= expr )* RP ) | ( LS i= INT_CONST RS ) )?
            {

                ArrayList<Expression> exps = new ArrayList();


                IDENTIFIER2 = (CommonTree) match(input, IDENTIFIER, FOLLOW_IDENTIFIER_in_id_expr816);

                // parser/flatzinc/FlatzincWalker.g:268:19: ( ( LP e= expr ( CM e= expr )* RP ) | ( LS i= INT_CONST RS ) )?
                int alt19 = 3;
                switch (input.LA(1)) {
                    case LP: {
                        alt19 = 1;
                    }
                    break;
                    case LS: {
                        alt19 = 2;
                    }
                    break;
                }

                switch (alt19) {
                    case 1:
                        // parser/flatzinc/FlatzincWalker.g:268:20: ( LP e= expr ( CM e= expr )* RP )
                    {
                        // parser/flatzinc/FlatzincWalker.g:268:20: ( LP e= expr ( CM e= expr )* RP )
                        // parser/flatzinc/FlatzincWalker.g:268:21: LP e= expr ( CM e= expr )* RP
                        {
                            match(input, LP, FOLLOW_LP_in_id_expr820);

                            pushFollow(FOLLOW_expr_in_id_expr824);
                            e = expr();

                            state._fsp--;


                            exps.add(e);

                            // parser/flatzinc/FlatzincWalker.g:268:45: ( CM e= expr )*
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
                                        // parser/flatzinc/FlatzincWalker.g:268:46: CM e= expr
                                    {
                                        match(input, CM, FOLLOW_CM_in_id_expr828);

                                        pushFollow(FOLLOW_expr_in_id_expr832);
                                        e = expr();

                                        state._fsp--;


                                        exps.add(e);

                                    }
                                    break;

                                    default:
                                        break loop18;
                                }
                            } while (true);


                            match(input, RP, FOLLOW_RP_in_id_expr837);

                        }


                    }
                    break;
                    case 2:
                        // parser/flatzinc/FlatzincWalker.g:268:76: ( LS i= INT_CONST RS )
                    {
                        // parser/flatzinc/FlatzincWalker.g:268:76: ( LS i= INT_CONST RS )
                        // parser/flatzinc/FlatzincWalker.g:268:77: LS i= INT_CONST RS
                        {
                            match(input, LS, FOLLOW_LS_in_id_expr841);

                            i = (CommonTree) match(input, INT_CONST, FOLLOW_INT_CONST_in_id_expr845);

                            match(input, RS, FOLLOW_RS_in_id_expr847);

                        }


                    }
                    break;

                }


                if (exps.size() > 0) {
                    exp = new EAnnotation(new EIdentifier(map, (IDENTIFIER2 != null ? IDENTIFIER2.getText() : null)), exps);
                } else if (i != null) {
                    exp = new EIdArray(map, (IDENTIFIER2 != null ? IDENTIFIER2.getText() : null), Integer.parseInt((i != null ? i.getText() : null)));
                } else {
                    exp = new EIdentifier(map, (IDENTIFIER2 != null ? IDENTIFIER2.getText() : null));
                }


            }

        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
        } finally {
            // do for sure before leaving
        }
        return exp;
    }
    // $ANTLR end "id_expr"


    // $ANTLR start "param_decl"
    // parser/flatzinc/FlatzincWalker.g:281:1: param_decl : ^( PAR IDENTIFIER pt= par_type e= expr ) ;
    public final void param_decl() throws RecognitionException {
        CommonTree IDENTIFIER3 = null;
        Declaration pt = null;

        Expression e = null;


        try {
            // parser/flatzinc/FlatzincWalker.g:282:2: ( ^( PAR IDENTIFIER pt= par_type e= expr ) )
            // parser/flatzinc/FlatzincWalker.g:282:6: ^( PAR IDENTIFIER pt= par_type e= expr )
            {
                match(input, PAR, FOLLOW_PAR_in_param_decl874);

                match(input, Token.DOWN, null);
                IDENTIFIER3 = (CommonTree) match(input, IDENTIFIER, FOLLOW_IDENTIFIER_in_param_decl876);

                pushFollow(FOLLOW_par_type_in_param_decl880);
                pt = par_type();

                state._fsp--;


                pushFollow(FOLLOW_expr_in_param_decl884);
                e = expr();

                state._fsp--;


                match(input, Token.UP, null);


                // Parameter(THashMap<String, Object> map, Declaration type, String identifier, Expression expression)
                FParameter.make_parameter(map, pt, (IDENTIFIER3 != null ? IDENTIFIER3.getText() : null), e);


            }

        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
        } finally {
            // do for sure before leaving
        }
        return;
    }
    // $ANTLR end "param_decl"


    // $ANTLR start "var_decl"
    // parser/flatzinc/FlatzincWalker.g:290:1: var_decl : ^( VAR IDENTIFIER vt= var_type anns= annotations (e= expr )? ) ;
    public final void var_decl() throws RecognitionException {
        CommonTree IDENTIFIER4 = null;
        Declaration vt = null;

        List<EAnnotation> anns = null;

        Expression e = null;


        try {
            // parser/flatzinc/FlatzincWalker.g:291:2: ( ^( VAR IDENTIFIER vt= var_type anns= annotations (e= expr )? ) )
            // parser/flatzinc/FlatzincWalker.g:291:6: ^( VAR IDENTIFIER vt= var_type anns= annotations (e= expr )? )
            {
                match(input, VAR, FOLLOW_VAR_in_var_decl903);

                match(input, Token.DOWN, null);
                IDENTIFIER4 = (CommonTree) match(input, IDENTIFIER, FOLLOW_IDENTIFIER_in_var_decl905);

                pushFollow(FOLLOW_var_type_in_var_decl909);
                vt = var_type();

                state._fsp--;


                pushFollow(FOLLOW_annotations_in_var_decl913);
                anns = annotations();

                state._fsp--;


                // parser/flatzinc/FlatzincWalker.g:291:53: (e= expr )?
                int alt20 = 2;
                switch (input.LA(1)) {
                    case EXPR:
                    case FALSE:
                    case IDENTIFIER:
                    case INT_CONST:
                    case LB:
                    case STRING:
                    case TRUE: {
                        alt20 = 1;
                    }
                    break;
                }

                switch (alt20) {
                    case 1:
                        // parser/flatzinc/FlatzincWalker.g:291:53: e= expr
                    {
                        pushFollow(FOLLOW_expr_in_var_decl917);
                        e = expr();

                        state._fsp--;


                    }
                    break;

                }


                match(input, Token.UP, null);


                FVariable.make_variable(map, vt, (IDENTIFIER4 != null ? IDENTIFIER4.getText() : null), anns, e, mSolver, mLayout);


            }

        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
        } finally {
            // do for sure before leaving
        }
        return;
    }
    // $ANTLR end "var_decl"


    // $ANTLR start "constraint"
    // parser/flatzinc/FlatzincWalker.g:297:1: constraint : ^( CONSTRAINT IDENTIFIER (e= expr )+ anns= annotations ) ;
    public final void constraint() throws RecognitionException {
        CommonTree IDENTIFIER5 = null;
        Expression e = null;

        List<EAnnotation> anns = null;


        try {
            // parser/flatzinc/FlatzincWalker.g:298:2: ( ^( CONSTRAINT IDENTIFIER (e= expr )+ anns= annotations ) )
            // parser/flatzinc/FlatzincWalker.g:299:2: ^( CONSTRAINT IDENTIFIER (e= expr )+ anns= annotations )
            {

                //  Solver aSolver, String id, List<Expression> exps, List<EAnnotation> annotations
                ArrayList<Expression> exps = new ArrayList();


                match(input, CONSTRAINT, FOLLOW_CONSTRAINT_in_constraint942);

                match(input, Token.DOWN, null);
                IDENTIFIER5 = (CommonTree) match(input, IDENTIFIER, FOLLOW_IDENTIFIER_in_constraint944);

                // parser/flatzinc/FlatzincWalker.g:303:30: (e= expr )+
                int cnt21 = 0;
                loop21:
                do {
                    int alt21 = 2;
                    switch (input.LA(1)) {
                        case EXPR:
                        case FALSE:
                        case IDENTIFIER:
                        case INT_CONST:
                        case LB:
                        case STRING:
                        case TRUE: {
                            alt21 = 1;
                        }
                        break;

                    }

                    switch (alt21) {
                        case 1:
                            // parser/flatzinc/FlatzincWalker.g:303:31: e= expr
                        {
                            pushFollow(FOLLOW_expr_in_constraint949);
                            e = expr();

                            state._fsp--;


                            exps.add(e);

                        }
                        break;

                        default:
                            if (cnt21 >= 1) break loop21;
                            EarlyExitException eee =
                                    new EarlyExitException(21, input);
                            throw eee;
                    }
                    cnt21++;
                } while (true);


                pushFollow(FOLLOW_annotations_in_constraint956);
                anns = annotations();

                state._fsp--;


                match(input, Token.UP, null);


                String id = (IDENTIFIER5 != null ? IDENTIFIER5.getText() : null);
                FConstraint.make_constraint(mSolver, map, id, exps, anns);


            }

        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
        } finally {
            // do for sure before leaving
        }
        return;
    }
    // $ANTLR end "constraint"


    // $ANTLR start "solve_goal"
    // parser/flatzinc/FlatzincWalker.g:310:1: solve_goal : ^( SOLVE anns= annotations res= resolution ) ;
    public final void solve_goal() throws RecognitionException {
        List<EAnnotation> anns = null;

        FlatzincWalker.resolution_return res = null;


        try {
            // parser/flatzinc/FlatzincWalker.g:311:2: ( ^( SOLVE anns= annotations res= resolution ) )
            // parser/flatzinc/FlatzincWalker.g:312:2: ^( SOLVE anns= annotations res= resolution )
            {
                match(input, SOLVE, FOLLOW_SOLVE_in_solve_goal973);

                match(input, Token.DOWN, null);
                pushFollow(FOLLOW_annotations_in_solve_goal977);
                anns = annotations();

                state._fsp--;


                pushFollow(FOLLOW_resolution_in_solve_goal981);
                res = resolution();

                state._fsp--;


                match(input, Token.UP, null);


                FGoal.define_goal(gc, mSolver, anns, res.type, res.expr);


            }

        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
        } finally {
            // do for sure before leaving
        }
        return;
    }
    // $ANTLR end "solve_goal"


    public static class resolution_return extends TreeRuleReturnScope {
        public ResolutionPolicy type;
        public Expression expr;
    }

    ;


    // $ANTLR start "resolution"
    // parser/flatzinc/FlatzincWalker.g:318:1: resolution returns [ResolutionPolicy type, Expression expr] : ( SATISFY | ^( MINIMIZE e= expr ) | ^( MAXIMIZE e= expr ) );
    public final FlatzincWalker.resolution_return resolution() throws RecognitionException {
        FlatzincWalker.resolution_return retval = new FlatzincWalker.resolution_return();
        retval.start = input.LT(1);


        Expression e = null;


        try {
            // parser/flatzinc/FlatzincWalker.g:319:5: ( SATISFY | ^( MINIMIZE e= expr ) | ^( MAXIMIZE e= expr ) )
            int alt22 = 3;
            switch (input.LA(1)) {
                case SATISFY: {
                    alt22 = 1;
                }
                break;
                case MINIMIZE: {
                    alt22 = 2;
                }
                break;
                case MAXIMIZE: {
                    alt22 = 3;
                }
                break;
                default:
                    NoViableAltException nvae =
                            new NoViableAltException("", 22, 0, input);

                    throw nvae;

            }

            switch (alt22) {
                case 1:
                    // parser/flatzinc/FlatzincWalker.g:319:9: SATISFY
                {
                    match(input, SATISFY, FOLLOW_SATISFY_in_resolution1005);


                    retval.type = ResolutionPolicy.SATISFACTION;
                    retval.expr = null;


                }
                break;
                case 2:
                    // parser/flatzinc/FlatzincWalker.g:324:9: ^( MINIMIZE e= expr )
                {
                    match(input, MINIMIZE, FOLLOW_MINIMIZE_in_resolution1022);

                    match(input, Token.DOWN, null);
                    pushFollow(FOLLOW_expr_in_resolution1026);
                    e = expr();

                    state._fsp--;


                    match(input, Token.UP, null);


                    retval.type = ResolutionPolicy.MINIMIZE;
                    retval.expr = e;


                }
                break;
                case 3:
                    // parser/flatzinc/FlatzincWalker.g:329:9: ^( MAXIMIZE e= expr )
                {
                    match(input, MAXIMIZE, FOLLOW_MAXIMIZE_in_resolution1044);

                    match(input, Token.DOWN, null);
                    pushFollow(FOLLOW_expr_in_resolution1048);
                    e = expr();

                    state._fsp--;


                    match(input, Token.UP, null);


                    retval.type = ResolutionPolicy.MAXIMIZE;
                    retval.expr = e;


                }
                break;

            }
        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
        } finally {
            // do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "resolution"


    // $ANTLR start "annotations"
    // parser/flatzinc/FlatzincWalker.g:336:1: annotations returns [List<EAnnotation> anns] : ^( ANNOTATIONS (e= annotation )* ) ;
    public final List<EAnnotation> annotations() throws RecognitionException {
        List<EAnnotation> anns = null;


        EAnnotation e = null;


        try {
            // parser/flatzinc/FlatzincWalker.g:337:5: ( ^( ANNOTATIONS (e= annotation )* ) )
            // parser/flatzinc/FlatzincWalker.g:338:5: ^( ANNOTATIONS (e= annotation )* )
            {

                anns = new ArrayList();


                match(input, ANNOTATIONS, FOLLOW_ANNOTATIONS_in_annotations1091);

                if (input.LA(1) == Token.DOWN) {
                    match(input, Token.DOWN, null);
                    // parser/flatzinc/FlatzincWalker.g:341:23: (e= annotation )*
                    loop23:
                    do {
                        int alt23 = 2;
                        switch (input.LA(1)) {
                            case IDENTIFIER: {
                                alt23 = 1;
                            }
                            break;

                        }

                        switch (alt23) {
                            case 1:
                                // parser/flatzinc/FlatzincWalker.g:341:24: e= annotation
                            {
                                pushFollow(FOLLOW_annotation_in_annotations1096);
                                e = annotation();

                                state._fsp--;


                                anns.add(e);

                            }
                            break;

                            default:
                                break loop23;
                        }
                    } while (true);


                    match(input, Token.UP, null);
                }


            }

        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
        } finally {
            // do for sure before leaving
        }
        return anns;
    }
    // $ANTLR end "annotations"


    // $ANTLR start "annotation"
    // parser/flatzinc/FlatzincWalker.g:344:1: annotation returns [EAnnotation ann] : IDENTIFIER ( LP (e= expr )+ RP )? ;
    public final EAnnotation annotation() throws RecognitionException {
        EAnnotation ann = null;


        CommonTree IDENTIFIER6 = null;
        Expression e = null;


        try {
            // parser/flatzinc/FlatzincWalker.g:345:5: ( IDENTIFIER ( LP (e= expr )+ RP )? )
            // parser/flatzinc/FlatzincWalker.g:346:5: IDENTIFIER ( LP (e= expr )+ RP )?
            {

                ArrayList<Expression> exps = new ArrayList();


                IDENTIFIER6 = (CommonTree) match(input, IDENTIFIER, FOLLOW_IDENTIFIER_in_annotation1133);

                // parser/flatzinc/FlatzincWalker.g:349:16: ( LP (e= expr )+ RP )?
                int alt25 = 2;
                switch (input.LA(1)) {
                    case LP: {
                        alt25 = 1;
                    }
                    break;
                }

                switch (alt25) {
                    case 1:
                        // parser/flatzinc/FlatzincWalker.g:349:17: LP (e= expr )+ RP
                    {
                        match(input, LP, FOLLOW_LP_in_annotation1136);

                        // parser/flatzinc/FlatzincWalker.g:349:20: (e= expr )+
                        int cnt24 = 0;
                        loop24:
                        do {
                            int alt24 = 2;
                            switch (input.LA(1)) {
                                case EXPR:
                                case FALSE:
                                case IDENTIFIER:
                                case INT_CONST:
                                case LB:
                                case STRING:
                                case TRUE: {
                                    alt24 = 1;
                                }
                                break;

                            }

                            switch (alt24) {
                                case 1:
                                    // parser/flatzinc/FlatzincWalker.g:349:21: e= expr
                                {
                                    pushFollow(FOLLOW_expr_in_annotation1141);
                                    e = expr();

                                    state._fsp--;


                                    exps.add(e);

                                }
                                break;

                                default:
                                    if (cnt24 >= 1) break loop24;
                                    EarlyExitException eee =
                                            new EarlyExitException(24, input);
                                    throw eee;
                            }
                            cnt24++;
                        } while (true);


                        match(input, RP, FOLLOW_RP_in_annotation1146);

                    }
                    break;

                }


                ann = new EAnnotation(new EIdentifier(map, (IDENTIFIER6 != null ? IDENTIFIER6.getText() : null)), exps);


            }

        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
        } finally {
            // do for sure before leaving
        }
        return ann;
    }
    // $ANTLR end "annotation"


    // $ANTLR start "bool_const"
    // parser/flatzinc/FlatzincWalker.g:355:1: bool_const returns [boolean value] : ( TRUE | FALSE );
    public final boolean bool_const() throws RecognitionException {
        boolean value = false;


        try {
            // parser/flatzinc/FlatzincWalker.g:356:5: ( TRUE | FALSE )
            int alt26 = 2;
            switch (input.LA(1)) {
                case TRUE: {
                    alt26 = 1;
                }
                break;
                case FALSE: {
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
                    // parser/flatzinc/FlatzincWalker.g:356:9: TRUE
                {
                    match(input, TRUE, FOLLOW_TRUE_in_bool_const1178);

                    value = true;

                }
                break;
                case 2:
                    // parser/flatzinc/FlatzincWalker.g:357:9: FALSE
                {
                    match(input, FALSE, FOLLOW_FALSE_in_bool_const1190);

                    value = false;

                }
                break;

            }
        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
        } finally {
            // do for sure before leaving
        }
        return value;
    }
    // $ANTLR end "bool_const"


    // $ANTLR start "pred_decl"
    // parser/flatzinc/FlatzincWalker.g:361:1: pred_decl : ^( PREDICATE IDENTIFIER ( pred_param )+ ) ;
    public final void pred_decl() throws RecognitionException {
        CommonTree IDENTIFIER7 = null;

        try {
            // parser/flatzinc/FlatzincWalker.g:362:2: ( ^( PREDICATE IDENTIFIER ( pred_param )+ ) )
            // parser/flatzinc/FlatzincWalker.g:362:6: ^( PREDICATE IDENTIFIER ( pred_param )+ )
            {
                match(input, PREDICATE, FOLLOW_PREDICATE_in_pred_decl1209);

                match(input, Token.DOWN, null);
                IDENTIFIER7 = (CommonTree) match(input, IDENTIFIER, FOLLOW_IDENTIFIER_in_pred_decl1211);

                // parser/flatzinc/FlatzincWalker.g:362:29: ( pred_param )+
                int cnt27 = 0;
                loop27:
                do {
                    int alt27 = 2;
                    switch (input.LA(1)) {
                        case CL: {
                            alt27 = 1;
                        }
                        break;

                    }

                    switch (alt27) {
                        case 1:
                            // parser/flatzinc/FlatzincWalker.g:362:29: pred_param
                        {
                            pushFollow(FOLLOW_pred_param_in_pred_decl1213);
                            pred_param();

                            state._fsp--;


                        }
                        break;

                        default:
                            if (cnt27 >= 1) break loop27;
                            EarlyExitException eee =
                                    new EarlyExitException(27, input);
                            throw eee;
                    }
                    cnt27++;
                } while (true);


                match(input, Token.UP, null);


                //        LOGGER.info("% skip predicate : "+ (IDENTIFIER7!=null?IDENTIFIER7.getText():null));


            }

        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
        } finally {
            // do for sure before leaving
        }
        return;
    }
    // $ANTLR end "pred_decl"


    // $ANTLR start "pred_param"
    // parser/flatzinc/FlatzincWalker.g:369:1: pred_param : ^( CL pred_param_type IDENTIFIER ) ;
    public final void pred_param() throws RecognitionException {
        try {
            // parser/flatzinc/FlatzincWalker.g:370:5: ( ^( CL pred_param_type IDENTIFIER ) )
            // parser/flatzinc/FlatzincWalker.g:370:9: ^( CL pred_param_type IDENTIFIER )
            {
                match(input, CL, FOLLOW_CL_in_pred_param1236);

                match(input, Token.DOWN, null);
                pushFollow(FOLLOW_pred_param_type_in_pred_param1238);
                pred_param_type();

                state._fsp--;


                match(input, IDENTIFIER, FOLLOW_IDENTIFIER_in_pred_param1240);

                match(input, Token.UP, null);


            }

        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
        } finally {
            // do for sure before leaving
        }
        return;
    }
    // $ANTLR end "pred_param"


    // $ANTLR start "pred_param_type"
    // parser/flatzinc/FlatzincWalker.g:374:1: pred_param_type : ( par_pred_param_type | var_pred_param_type );
    public final void pred_param_type() throws RecognitionException {
        try {
            // parser/flatzinc/FlatzincWalker.g:375:5: ( par_pred_param_type | var_pred_param_type )
            int alt28 = 2;
            alt28 = dfa28.predict(input);
            switch (alt28) {
                case 1:
                    // parser/flatzinc/FlatzincWalker.g:375:9: par_pred_param_type
                {
                    pushFollow(FOLLOW_par_pred_param_type_in_pred_param_type1261);
                    par_pred_param_type();

                    state._fsp--;


                }
                break;
                case 2:
                    // parser/flatzinc/FlatzincWalker.g:376:9: var_pred_param_type
                {
                    pushFollow(FOLLOW_var_pred_param_type_in_pred_param_type1271);
                    var_pred_param_type();

                    state._fsp--;


                }
                break;

            }
        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
        } finally {
            // do for sure before leaving
        }
        return;
    }
    // $ANTLR end "pred_param_type"


    // $ANTLR start "par_pred_param_type"
    // parser/flatzinc/FlatzincWalker.g:380:1: par_pred_param_type : ( par_type | ^( DD INT_CONST INT_CONST ) | ^( CM ( INT_CONST )+ ) | ^( SET ^( DD INT_CONST INT_CONST ) ) | ^( SET ^( CM ( INT_CONST )+ ) ) | ^( ARRAY ( index_set )+ ^( DD INT_CONST INT_CONST ) ) | ^( ARRAY ( index_set )+ ^( CM ( INT_CONST )+ ) ) | ^( ARRAY ( index_set )+ ^( SET ^( DD INT_CONST INT_CONST ) ) ) | ^( ARRAY ( index_set )+ ^( SET ^( CM ( INT_CONST )+ ) ) ) );
    public final void par_pred_param_type() throws RecognitionException {
        try {
            // parser/flatzinc/FlatzincWalker.g:381:5: ( par_type | ^( DD INT_CONST INT_CONST ) | ^( CM ( INT_CONST )+ ) | ^( SET ^( DD INT_CONST INT_CONST ) ) | ^( SET ^( CM ( INT_CONST )+ ) ) | ^( ARRAY ( index_set )+ ^( DD INT_CONST INT_CONST ) ) | ^( ARRAY ( index_set )+ ^( CM ( INT_CONST )+ ) ) | ^( ARRAY ( index_set )+ ^( SET ^( DD INT_CONST INT_CONST ) ) ) | ^( ARRAY ( index_set )+ ^( SET ^( CM ( INT_CONST )+ ) ) ) )
            int alt37 = 9;
            alt37 = dfa37.predict(input);
            switch (alt37) {
                case 1:
                    // parser/flatzinc/FlatzincWalker.g:381:9: par_type
                {
                    pushFollow(FOLLOW_par_type_in_par_pred_param_type1291);
                    par_type();

                    state._fsp--;


                }
                break;
                case 2:
                    // parser/flatzinc/FlatzincWalker.g:383:9: ^( DD INT_CONST INT_CONST )
                {
                    match(input, DD, FOLLOW_DD_in_par_pred_param_type1303);

                    match(input, Token.DOWN, null);
                    match(input, INT_CONST, FOLLOW_INT_CONST_in_par_pred_param_type1305);

                    match(input, INT_CONST, FOLLOW_INT_CONST_in_par_pred_param_type1307);

                    match(input, Token.UP, null);


                }
                break;
                case 3:
                    // parser/flatzinc/FlatzincWalker.g:384:9: ^( CM ( INT_CONST )+ )
                {
                    match(input, CM, FOLLOW_CM_in_par_pred_param_type1319);

                    match(input, Token.DOWN, null);
                    // parser/flatzinc/FlatzincWalker.g:384:14: ( INT_CONST )+
                    int cnt29 = 0;
                    loop29:
                    do {
                        int alt29 = 2;
                        switch (input.LA(1)) {
                            case INT_CONST: {
                                alt29 = 1;
                            }
                            break;

                        }

                        switch (alt29) {
                            case 1:
                                // parser/flatzinc/FlatzincWalker.g:384:14: INT_CONST
                            {
                                match(input, INT_CONST, FOLLOW_INT_CONST_in_par_pred_param_type1321);

                            }
                            break;

                            default:
                                if (cnt29 >= 1) break loop29;
                                EarlyExitException eee =
                                        new EarlyExitException(29, input);
                                throw eee;
                        }
                        cnt29++;
                    } while (true);


                    match(input, Token.UP, null);


                }
                break;
                case 4:
                    // parser/flatzinc/FlatzincWalker.g:385:9: ^( SET ^( DD INT_CONST INT_CONST ) )
                {
                    match(input, SET, FOLLOW_SET_in_par_pred_param_type1334);

                    match(input, Token.DOWN, null);
                    match(input, DD, FOLLOW_DD_in_par_pred_param_type1337);

                    match(input, Token.DOWN, null);
                    match(input, INT_CONST, FOLLOW_INT_CONST_in_par_pred_param_type1339);

                    match(input, INT_CONST, FOLLOW_INT_CONST_in_par_pred_param_type1341);

                    match(input, Token.UP, null);


                    match(input, Token.UP, null);


                }
                break;
                case 5:
                    // parser/flatzinc/FlatzincWalker.g:386:9: ^( SET ^( CM ( INT_CONST )+ ) )
                {
                    match(input, SET, FOLLOW_SET_in_par_pred_param_type1354);

                    match(input, Token.DOWN, null);
                    match(input, CM, FOLLOW_CM_in_par_pred_param_type1357);

                    match(input, Token.DOWN, null);
                    // parser/flatzinc/FlatzincWalker.g:386:20: ( INT_CONST )+
                    int cnt30 = 0;
                    loop30:
                    do {
                        int alt30 = 2;
                        switch (input.LA(1)) {
                            case INT_CONST: {
                                alt30 = 1;
                            }
                            break;

                        }

                        switch (alt30) {
                            case 1:
                                // parser/flatzinc/FlatzincWalker.g:386:20: INT_CONST
                            {
                                match(input, INT_CONST, FOLLOW_INT_CONST_in_par_pred_param_type1359);

                            }
                            break;

                            default:
                                if (cnt30 >= 1) break loop30;
                                EarlyExitException eee =
                                        new EarlyExitException(30, input);
                                throw eee;
                        }
                        cnt30++;
                    } while (true);


                    match(input, Token.UP, null);


                    match(input, Token.UP, null);


                }
                break;
                case 6:
                    // parser/flatzinc/FlatzincWalker.g:388:9: ^( ARRAY ( index_set )+ ^( DD INT_CONST INT_CONST ) )
                {
                    match(input, ARRAY, FOLLOW_ARRAY_in_par_pred_param_type1374);

                    match(input, Token.DOWN, null);
                    // parser/flatzinc/FlatzincWalker.g:388:17: ( index_set )+
                    int cnt31 = 0;
                    loop31:
                    do {
                        int alt31 = 2;
                        switch (input.LA(1)) {
                            case INDEX: {
                                alt31 = 1;
                            }
                            break;

                        }

                        switch (alt31) {
                            case 1:
                                // parser/flatzinc/FlatzincWalker.g:388:17: index_set
                            {
                                pushFollow(FOLLOW_index_set_in_par_pred_param_type1376);
                                index_set();

                                state._fsp--;


                            }
                            break;

                            default:
                                if (cnt31 >= 1) break loop31;
                                EarlyExitException eee =
                                        new EarlyExitException(31, input);
                                throw eee;
                        }
                        cnt31++;
                    } while (true);


                    match(input, DD, FOLLOW_DD_in_par_pred_param_type1380);

                    match(input, Token.DOWN, null);
                    match(input, INT_CONST, FOLLOW_INT_CONST_in_par_pred_param_type1382);

                    match(input, INT_CONST, FOLLOW_INT_CONST_in_par_pred_param_type1384);

                    match(input, Token.UP, null);


                    match(input, Token.UP, null);


                }
                break;
                case 7:
                    // parser/flatzinc/FlatzincWalker.g:389:9: ^( ARRAY ( index_set )+ ^( CM ( INT_CONST )+ ) )
                {
                    match(input, ARRAY, FOLLOW_ARRAY_in_par_pred_param_type1397);

                    match(input, Token.DOWN, null);
                    // parser/flatzinc/FlatzincWalker.g:389:17: ( index_set )+
                    int cnt32 = 0;
                    loop32:
                    do {
                        int alt32 = 2;
                        switch (input.LA(1)) {
                            case INDEX: {
                                alt32 = 1;
                            }
                            break;

                        }

                        switch (alt32) {
                            case 1:
                                // parser/flatzinc/FlatzincWalker.g:389:17: index_set
                            {
                                pushFollow(FOLLOW_index_set_in_par_pred_param_type1399);
                                index_set();

                                state._fsp--;


                            }
                            break;

                            default:
                                if (cnt32 >= 1) break loop32;
                                EarlyExitException eee =
                                        new EarlyExitException(32, input);
                                throw eee;
                        }
                        cnt32++;
                    } while (true);


                    match(input, CM, FOLLOW_CM_in_par_pred_param_type1403);

                    match(input, Token.DOWN, null);
                    // parser/flatzinc/FlatzincWalker.g:389:33: ( INT_CONST )+
                    int cnt33 = 0;
                    loop33:
                    do {
                        int alt33 = 2;
                        switch (input.LA(1)) {
                            case INT_CONST: {
                                alt33 = 1;
                            }
                            break;

                        }

                        switch (alt33) {
                            case 1:
                                // parser/flatzinc/FlatzincWalker.g:389:33: INT_CONST
                            {
                                match(input, INT_CONST, FOLLOW_INT_CONST_in_par_pred_param_type1405);

                            }
                            break;

                            default:
                                if (cnt33 >= 1) break loop33;
                                EarlyExitException eee =
                                        new EarlyExitException(33, input);
                                throw eee;
                        }
                        cnt33++;
                    } while (true);


                    match(input, Token.UP, null);


                    match(input, Token.UP, null);


                }
                break;
                case 8:
                    // parser/flatzinc/FlatzincWalker.g:390:9: ^( ARRAY ( index_set )+ ^( SET ^( DD INT_CONST INT_CONST ) ) )
                {
                    match(input, ARRAY, FOLLOW_ARRAY_in_par_pred_param_type1419);

                    match(input, Token.DOWN, null);
                    // parser/flatzinc/FlatzincWalker.g:390:17: ( index_set )+
                    int cnt34 = 0;
                    loop34:
                    do {
                        int alt34 = 2;
                        switch (input.LA(1)) {
                            case INDEX: {
                                alt34 = 1;
                            }
                            break;

                        }

                        switch (alt34) {
                            case 1:
                                // parser/flatzinc/FlatzincWalker.g:390:17: index_set
                            {
                                pushFollow(FOLLOW_index_set_in_par_pred_param_type1421);
                                index_set();

                                state._fsp--;


                            }
                            break;

                            default:
                                if (cnt34 >= 1) break loop34;
                                EarlyExitException eee =
                                        new EarlyExitException(34, input);
                                throw eee;
                        }
                        cnt34++;
                    } while (true);


                    match(input, SET, FOLLOW_SET_in_par_pred_param_type1425);

                    match(input, Token.DOWN, null);
                    match(input, DD, FOLLOW_DD_in_par_pred_param_type1428);

                    match(input, Token.DOWN, null);
                    match(input, INT_CONST, FOLLOW_INT_CONST_in_par_pred_param_type1430);

                    match(input, INT_CONST, FOLLOW_INT_CONST_in_par_pred_param_type1432);

                    match(input, Token.UP, null);


                    match(input, Token.UP, null);


                    match(input, Token.UP, null);


                }
                break;
                case 9:
                    // parser/flatzinc/FlatzincWalker.g:391:9: ^( ARRAY ( index_set )+ ^( SET ^( CM ( INT_CONST )+ ) ) )
                {
                    match(input, ARRAY, FOLLOW_ARRAY_in_par_pred_param_type1446);

                    match(input, Token.DOWN, null);
                    // parser/flatzinc/FlatzincWalker.g:391:17: ( index_set )+
                    int cnt35 = 0;
                    loop35:
                    do {
                        int alt35 = 2;
                        switch (input.LA(1)) {
                            case INDEX: {
                                alt35 = 1;
                            }
                            break;

                        }

                        switch (alt35) {
                            case 1:
                                // parser/flatzinc/FlatzincWalker.g:391:17: index_set
                            {
                                pushFollow(FOLLOW_index_set_in_par_pred_param_type1448);
                                index_set();

                                state._fsp--;


                            }
                            break;

                            default:
                                if (cnt35 >= 1) break loop35;
                                EarlyExitException eee =
                                        new EarlyExitException(35, input);
                                throw eee;
                        }
                        cnt35++;
                    } while (true);


                    match(input, SET, FOLLOW_SET_in_par_pred_param_type1452);

                    match(input, Token.DOWN, null);
                    match(input, CM, FOLLOW_CM_in_par_pred_param_type1455);

                    match(input, Token.DOWN, null);
                    // parser/flatzinc/FlatzincWalker.g:391:39: ( INT_CONST )+
                    int cnt36 = 0;
                    loop36:
                    do {
                        int alt36 = 2;
                        switch (input.LA(1)) {
                            case INT_CONST: {
                                alt36 = 1;
                            }
                            break;

                        }

                        switch (alt36) {
                            case 1:
                                // parser/flatzinc/FlatzincWalker.g:391:39: INT_CONST
                            {
                                match(input, INT_CONST, FOLLOW_INT_CONST_in_par_pred_param_type1457);

                            }
                            break;

                            default:
                                if (cnt36 >= 1) break loop36;
                                EarlyExitException eee =
                                        new EarlyExitException(36, input);
                                throw eee;
                        }
                        cnt36++;
                    } while (true);


                    match(input, Token.UP, null);


                    match(input, Token.UP, null);


                    match(input, Token.UP, null);


                }
                break;

            }
        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
        } finally {
            // do for sure before leaving
        }
        return;
    }
    // $ANTLR end "par_pred_param_type"


    // $ANTLR start "var_pred_param_type"
    // parser/flatzinc/FlatzincWalker.g:395:1: var_pred_param_type : ( ^( VAR var_type ) | ^( VAR SET ) | ^( ARRAY ( index_set )+ ^( VAR SET ) ) );
    public final void var_pred_param_type() throws RecognitionException {
        try {
            // parser/flatzinc/FlatzincWalker.g:396:5: ( ^( VAR var_type ) | ^( VAR SET ) | ^( ARRAY ( index_set )+ ^( VAR SET ) ) )
            int alt39 = 3;
            switch (input.LA(1)) {
                case VAR: {
                    switch (input.LA(2)) {
                        case DOWN: {
                            switch (input.LA(3)) {
                                case SET: {
                                    alt39 = 2;
                                }
                                break;
                                case ARRVAR:
                                case AVAR: {
                                    alt39 = 1;
                                }
                                break;
                                default:
                                    NoViableAltException nvae =
                                            new NoViableAltException("", 39, 3, input);

                                    throw nvae;

                            }

                        }
                        break;
                        default:
                            NoViableAltException nvae =
                                    new NoViableAltException("", 39, 1, input);

                            throw nvae;

                    }

                }
                break;
                case ARRAY: {
                    alt39 = 3;
                }
                break;
                default:
                    NoViableAltException nvae =
                            new NoViableAltException("", 39, 0, input);

                    throw nvae;

            }

            switch (alt39) {
                case 1:
                    // parser/flatzinc/FlatzincWalker.g:396:9: ^( VAR var_type )
                {
                    match(input, VAR, FOLLOW_VAR_in_var_pred_param_type1482);

                    match(input, Token.DOWN, null);
                    pushFollow(FOLLOW_var_type_in_var_pred_param_type1484);
                    var_type();

                    state._fsp--;


                    match(input, Token.UP, null);


                }
                break;
                case 2:
                    // parser/flatzinc/FlatzincWalker.g:397:9: ^( VAR SET )
                {
                    match(input, VAR, FOLLOW_VAR_in_var_pred_param_type1496);

                    match(input, Token.DOWN, null);
                    match(input, SET, FOLLOW_SET_in_var_pred_param_type1498);

                    match(input, Token.UP, null);


                }
                break;
                case 3:
                    // parser/flatzinc/FlatzincWalker.g:398:9: ^( ARRAY ( index_set )+ ^( VAR SET ) )
                {
                    match(input, ARRAY, FOLLOW_ARRAY_in_var_pred_param_type1510);

                    match(input, Token.DOWN, null);
                    // parser/flatzinc/FlatzincWalker.g:398:17: ( index_set )+
                    int cnt38 = 0;
                    loop38:
                    do {
                        int alt38 = 2;
                        switch (input.LA(1)) {
                            case INDEX: {
                                alt38 = 1;
                            }
                            break;

                        }

                        switch (alt38) {
                            case 1:
                                // parser/flatzinc/FlatzincWalker.g:398:17: index_set
                            {
                                pushFollow(FOLLOW_index_set_in_var_pred_param_type1512);
                                index_set();

                                state._fsp--;


                            }
                            break;

                            default:
                                if (cnt38 >= 1) break loop38;
                                EarlyExitException eee =
                                        new EarlyExitException(38, input);
                                throw eee;
                        }
                        cnt38++;
                    } while (true);


                    match(input, VAR, FOLLOW_VAR_in_var_pred_param_type1516);

                    match(input, Token.DOWN, null);
                    match(input, SET, FOLLOW_SET_in_var_pred_param_type1518);

                    match(input, Token.UP, null);


                    match(input, Token.UP, null);


                }
                break;

            }
        } catch (RecognitionException re) {
            reportError(re);
            recover(input, re);
        } finally {
            // do for sure before leaving
        }
        return;
    }
    // $ANTLR end "var_pred_param_type"

    // Delegated rules


    protected DFA28 dfa28 = new DFA28(this);
    protected DFA37 dfa37 = new DFA37(this);
    static final String DFA28_eotS =
            "\17\uffff";
    static final String DFA28_eofS =
            "\17\uffff";
    static final String DFA28_minS =
            "\1\5\1\uffff\1\2\1\uffff\1\33\1\2\1\21\1\2\1\3\1\35\1\15\1\35\2" +
                    "\3\1\15";
    static final String DFA28_maxS =
            "\1\63\1\uffff\1\2\1\uffff\1\33\1\2\1\34\1\2\1\3\1\35\1\63\1\35\2" +
                    "\3\1\63";
    static final String DFA28_acceptS =
            "\1\uffff\1\1\1\uffff\1\2\13\uffff";
    static final String DFA28_specialS =
            "\17\uffff}>";
    static final String[] DFA28_transitionS = {
            "\1\1\1\2\1\1\5\uffff\1\1\3\uffff\1\1\34\uffff\1\1\4\uffff\1" +
                    "\3",
            "",
            "\1\4",
            "",
            "\1\5",
            "\1\6",
            "\1\7\12\uffff\1\10",
            "\1\11",
            "\1\12",
            "\1\13",
            "\1\1\3\uffff\1\1\11\uffff\1\5\22\uffff\1\1\4\uffff\1\3",
            "\1\14",
            "\1\15",
            "\1\16",
            "\1\1\3\uffff\1\1\11\uffff\1\5\22\uffff\1\1\4\uffff\1\3"
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
        for (int i = 0; i < numStates; i++) {
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
            return "374:1: pred_param_type : ( par_pred_param_type | var_pred_param_type );";
        }
    }

    static final String DFA37_eotS =
            "\32\uffff";
    static final String DFA37_eofS =
            "\32\uffff";
    static final String DFA37_minS =
            "\1\5\3\uffff\2\2\1\15\1\33\2\uffff\1\2\1\21\1\2\1\3\1\35\1\15\1" +
                    "\35\2\uffff\1\2\1\3\1\15\1\3\2\uffff\1\15";
    static final String DFA37_maxS =
            "\1\56\3\uffff\2\2\1\21\1\33\2\uffff\1\2\1\34\1\2\1\3\1\35\1\56\1" +
                    "\35\2\uffff\1\2\1\3\1\21\1\3\2\uffff\1\56";
    static final String DFA37_acceptS =
            "\1\uffff\1\1\1\2\1\3\4\uffff\1\4\1\5\7\uffff\1\6\1\7\4\uffff\1\10" +
                    "\1\11\1\uffff";
    static final String DFA37_specialS =
            "\32\uffff}>";
    static final String[] DFA37_transitionS = {
            "\1\1\1\5\1\1\5\uffff\1\3\3\uffff\1\2\34\uffff\1\4",
            "",
            "",
            "",
            "\1\6",
            "\1\7",
            "\1\11\3\uffff\1\10",
            "\1\12",
            "",
            "",
            "\1\13",
            "\1\14\12\uffff\1\15",
            "\1\16",
            "\1\17",
            "\1\20",
            "\1\22\3\uffff\1\21\11\uffff\1\12\22\uffff\1\23",
            "\1\24",
            "",
            "",
            "\1\25",
            "\1\26",
            "\1\30\3\uffff\1\27",
            "\1\31",
            "",
            "",
            "\1\22\3\uffff\1\21\11\uffff\1\12\22\uffff\1\23"
    };

    static final short[] DFA37_eot = DFA.unpackEncodedString(DFA37_eotS);
    static final short[] DFA37_eof = DFA.unpackEncodedString(DFA37_eofS);
    static final char[] DFA37_min = DFA.unpackEncodedStringToUnsignedChars(DFA37_minS);
    static final char[] DFA37_max = DFA.unpackEncodedStringToUnsignedChars(DFA37_maxS);
    static final short[] DFA37_accept = DFA.unpackEncodedString(DFA37_acceptS);
    static final short[] DFA37_special = DFA.unpackEncodedString(DFA37_specialS);
    static final short[][] DFA37_transition;

    static {
        int numStates = DFA37_transitionS.length;
        DFA37_transition = new short[numStates][];
        for (int i = 0; i < numStates; i++) {
            DFA37_transition[i] = DFA.unpackEncodedString(DFA37_transitionS[i]);
        }
    }

    class DFA37 extends DFA {

        public DFA37(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 37;
            this.eot = DFA37_eot;
            this.eof = DFA37_eof;
            this.min = DFA37_min;
            this.max = DFA37_max;
            this.accept = DFA37_accept;
            this.special = DFA37_special;
            this.transition = DFA37_transition;
        }

        public String getDescription() {
            return "380:1: par_pred_param_type : ( par_type | ^( DD INT_CONST INT_CONST ) | ^( CM ( INT_CONST )+ ) | ^( SET ^( DD INT_CONST INT_CONST ) ) | ^( SET ^( CM ( INT_CONST )+ ) ) | ^( ARRAY ( index_set )+ ^( DD INT_CONST INT_CONST ) ) | ^( ARRAY ( index_set )+ ^( CM ( INT_CONST )+ ) ) | ^( ARRAY ( index_set )+ ^( SET ^( DD INT_CONST INT_CONST ) ) ) | ^( ARRAY ( index_set )+ ^( SET ^( CM ( INT_CONST )+ ) ) ) );";
        }
    }


    public static final BitSet FOLLOW_pred_decl_in_flatzinc_model52 = new BitSet(new long[]{0x0008814000008000L});
    public static final BitSet FOLLOW_param_decl_in_flatzinc_model57 = new BitSet(new long[]{0x0008804000008000L});
    public static final BitSet FOLLOW_var_decl_in_flatzinc_model62 = new BitSet(new long[]{0x0008800000008000L});
    public static final BitSet FOLLOW_constraint_in_flatzinc_model67 = new BitSet(new long[]{0x0000800000008000L});
    public static final BitSet FOLLOW_solve_goal_in_flatzinc_model71 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ARRPAR_in_par_type110 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_index_set_in_par_type115 = new BitSet(new long[]{0x0000400019000400L});
    public static final BitSet FOLLOW_par_type_u_in_par_type122 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_APAR_in_par_type140 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_par_type_u_in_par_type144 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_BOOL_in_par_type_u174 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FLOAT_in_par_type_u190 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SET_in_par_type_u206 = new BitSet(new long[]{0x0000002000000000L});
    public static final BitSet FOLLOW_OF_in_par_type_u208 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_INT_in_par_type_u210 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INT_in_par_type_u226 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ARRVAR_in_var_type267 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_index_set_in_var_type272 = new BitSet(new long[]{0x0000400019022400L});
    public static final BitSet FOLLOW_var_type_u_in_var_type279 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_AVAR_in_var_type297 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_var_type_u_in_var_type301 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_BOOL_in_var_type_u332 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FLOAT_in_var_type_u348 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INT_in_var_type_u364 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DD_in_var_type_u381 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_INT_CONST_in_var_type_u385 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_INT_CONST_in_var_type_u389 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_CM_in_var_type_u424 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_INT_CONST_in_var_type_u429 = new BitSet(new long[]{0x0000000020000008L});
    public static final BitSet FOLLOW_SET_in_var_type_u450 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_DD_in_var_type_u453 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_INT_CONST_in_var_type_u457 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_INT_CONST_in_var_type_u461 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_SET_in_var_type_u491 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_CM_in_var_type_u494 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_INT_CONST_in_var_type_u499 = new BitSet(new long[]{0x0000000020000008L});
    public static final BitSet FOLLOW_INDEX_in_index_set534 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_DD_in_index_set537 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_INT_CONST_in_index_set541 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_INT_CONST_in_index_set545 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_INDEX_in_index_set564 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_INT_in_index_set566 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_LB_in_expr598 = new BitSet(new long[]{0x0000020000000000L});
    public static final BitSet FOLLOW_RB_in_expr600 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LB_in_expr628 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_INT_CONST_in_expr633 = new BitSet(new long[]{0x0000020020000000L});
    public static final BitSet FOLLOW_RB_in_expr638 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_bool_const_in_expr656 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INT_CONST_in_expr674 = new BitSet(new long[]{0x0000000000020002L});
    public static final BitSet FOLLOW_DD_in_expr677 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_INT_CONST_in_expr681 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_EXPR_in_expr711 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_LS_in_expr713 = new BitSet(new long[]{0x0003080064C00000L});
    public static final BitSet FOLLOW_expr_in_expr718 = new BitSet(new long[]{0x0003080064C00000L});
    public static final BitSet FOLLOW_RS_in_expr723 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_id_expr_in_expr742 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_in_expr758 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENTIFIER_in_id_expr816 = new BitSet(new long[]{0x0000000180000002L});
    public static final BitSet FOLLOW_LP_in_id_expr820 = new BitSet(new long[]{0x0003000064C00000L});
    public static final BitSet FOLLOW_expr_in_id_expr824 = new BitSet(new long[]{0x0000040000002000L});
    public static final BitSet FOLLOW_CM_in_id_expr828 = new BitSet(new long[]{0x0003000064C00000L});
    public static final BitSet FOLLOW_expr_in_id_expr832 = new BitSet(new long[]{0x0000040000002000L});
    public static final BitSet FOLLOW_RP_in_id_expr837 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LS_in_id_expr841 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_INT_CONST_in_id_expr845 = new BitSet(new long[]{0x0000080000000000L});
    public static final BitSet FOLLOW_RS_in_id_expr847 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PAR_in_param_decl874 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_IDENTIFIER_in_param_decl876 = new BitSet(new long[]{0x00000000000000A0L});
    public static final BitSet FOLLOW_par_type_in_param_decl880 = new BitSet(new long[]{0x0003000064C00000L});
    public static final BitSet FOLLOW_expr_in_param_decl884 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_VAR_in_var_decl903 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_IDENTIFIER_in_var_decl905 = new BitSet(new long[]{0x0000000000000300L});
    public static final BitSet FOLLOW_var_type_in_var_decl909 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_annotations_in_var_decl913 = new BitSet(new long[]{0x0003000064C00008L});
    public static final BitSet FOLLOW_expr_in_var_decl917 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_CONSTRAINT_in_constraint942 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_IDENTIFIER_in_constraint944 = new BitSet(new long[]{0x0003000064C00000L});
    public static final BitSet FOLLOW_expr_in_constraint949 = new BitSet(new long[]{0x0003000064C00010L});
    public static final BitSet FOLLOW_annotations_in_constraint956 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_SOLVE_in_solve_goal973 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_annotations_in_solve_goal977 = new BitSet(new long[]{0x0000100600000000L});
    public static final BitSet FOLLOW_resolution_in_solve_goal981 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_SATISFY_in_resolution1005 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MINIMIZE_in_resolution1022 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_expr_in_resolution1026 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_MAXIMIZE_in_resolution1044 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_expr_in_resolution1048 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_ANNOTATIONS_in_annotations1091 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_annotation_in_annotations1096 = new BitSet(new long[]{0x0000000004000008L});
    public static final BitSet FOLLOW_IDENTIFIER_in_annotation1133 = new BitSet(new long[]{0x0000000080000002L});
    public static final BitSet FOLLOW_LP_in_annotation1136 = new BitSet(new long[]{0x0003000064C00000L});
    public static final BitSet FOLLOW_expr_in_annotation1141 = new BitSet(new long[]{0x0003040064C00000L});
    public static final BitSet FOLLOW_RP_in_annotation1146 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TRUE_in_bool_const1178 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FALSE_in_bool_const1190 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PREDICATE_in_pred_decl1209 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_IDENTIFIER_in_pred_decl1211 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_pred_param_in_pred_decl1213 = new BitSet(new long[]{0x0000000000001008L});
    public static final BitSet FOLLOW_CL_in_pred_param1236 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_pred_param_type_in_pred_param1238 = new BitSet(new long[]{0x0000000004000000L});
    public static final BitSet FOLLOW_IDENTIFIER_in_pred_param1240 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_par_pred_param_type_in_pred_param_type1261 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_var_pred_param_type_in_pred_param_type1271 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_par_type_in_par_pred_param_type1291 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DD_in_par_pred_param_type1303 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type1305 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type1307 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_CM_in_par_pred_param_type1319 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type1321 = new BitSet(new long[]{0x0000000020000008L});
    public static final BitSet FOLLOW_SET_in_par_pred_param_type1334 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_DD_in_par_pred_param_type1337 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type1339 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type1341 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_SET_in_par_pred_param_type1354 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_CM_in_par_pred_param_type1357 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type1359 = new BitSet(new long[]{0x0000000020000008L});
    public static final BitSet FOLLOW_ARRAY_in_par_pred_param_type1374 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_index_set_in_par_pred_param_type1376 = new BitSet(new long[]{0x0000000008020000L});
    public static final BitSet FOLLOW_DD_in_par_pred_param_type1380 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type1382 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type1384 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_ARRAY_in_par_pred_param_type1397 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_index_set_in_par_pred_param_type1399 = new BitSet(new long[]{0x0000000008002000L});
    public static final BitSet FOLLOW_CM_in_par_pred_param_type1403 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type1405 = new BitSet(new long[]{0x0000000020000008L});
    public static final BitSet FOLLOW_ARRAY_in_par_pred_param_type1419 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_index_set_in_par_pred_param_type1421 = new BitSet(new long[]{0x0000400008000000L});
    public static final BitSet FOLLOW_SET_in_par_pred_param_type1425 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_DD_in_par_pred_param_type1428 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type1430 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type1432 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_ARRAY_in_par_pred_param_type1446 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_index_set_in_par_pred_param_type1448 = new BitSet(new long[]{0x0000400008000000L});
    public static final BitSet FOLLOW_SET_in_par_pred_param_type1452 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_CM_in_par_pred_param_type1455 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type1457 = new BitSet(new long[]{0x0000000020000008L});
    public static final BitSet FOLLOW_VAR_in_var_pred_param_type1482 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_var_type_in_var_pred_param_type1484 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_VAR_in_var_pred_param_type1496 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_SET_in_var_pred_param_type1498 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_ARRAY_in_var_pred_param_type1510 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_index_set_in_var_pred_param_type1512 = new BitSet(new long[]{0x0008000008000000L});
    public static final BitSet FOLLOW_VAR_in_var_pred_param_type1516 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_SET_in_var_pred_param_type1518 = new BitSet(new long[]{0x0000000000000008L});

}