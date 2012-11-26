// $ANTLR 3.4 parser/flatzinc/FlatzincFullExtWalker.g 2012-11-26 14:06:12

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
import gnu.trove.map.hash.TIntObjectHashMap;
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
import parser.flatzinc.ast.ext.*;
import solver.Solver;
import solver.propagation.ISchedulable;
import solver.propagation.PropagationEngine;
import solver.propagation.generator.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings({"all", "warnings", "unchecked"})
public class FlatzincFullExtWalker extends TreeParser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "AND", "ANNOTATIONS", "ANY", "APAR", "ARRAY", "ARRPAR", "ARRVAR", "AS", "AVAR", "BOOL", "CA1", "CA2", "CARITY", "CHAR", "CL", "CM", "CNAME", "COMMENT", "CONSTRAINT", "CSTR", "DC", "DD", "DO", "EACH", "ENGINE", "EQ", "ESC_SEQ", "EXPONENT", "EXPR", "FALSE", "FLOAT", "FOR", "HEAP", "HEX_DIGIT", "IDENTIFIER", "IN", "INDEX", "INT", "INT_CONST", "KEY", "LB", "LIST", "LP", "LS", "MANY1", "MANY2", "MANY3", "MANY4", "MAX", "MAXIMIZE", "MIN", "MINIMIZE", "MN", "NOT", "OCTAL_ESC", "OEQ", "OF", "OGQ", "OGT", "OLQ", "OLT", "ONE", "ONQ", "OR", "ORDERBY", "PAR", "PARITY", "PL", "PPRIO", "PPRIOD", "PREDICATE", "PROP", "QUEUE", "RB", "REV", "RP", "RS", "SATISFY", "SC", "SET", "SIZE", "SOLVE", "STREG", "STRING", "STRUC1", "STRUC2", "SUM", "TRUE", "UNICODE_ESC", "VAR", "VCARD", "VNAME", "WFOR", "WONE", "WS"
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
    public static final int ENGINE=28;
    public static final int EQ=29;
    public static final int ESC_SEQ=30;
    public static final int EXPONENT=31;
    public static final int EXPR=32;
    public static final int FALSE=33;
    public static final int FLOAT=34;
    public static final int FOR=35;
    public static final int HEAP=36;
    public static final int HEX_DIGIT=37;
    public static final int IDENTIFIER=38;
    public static final int IN=39;
    public static final int INDEX=40;
    public static final int INT=41;
    public static final int INT_CONST=42;
    public static final int KEY=43;
    public static final int LB=44;
    public static final int LIST=45;
    public static final int LP=46;
    public static final int LS=47;
    public static final int MANY1=48;
    public static final int MANY2=49;
    public static final int MANY3=50;
    public static final int MANY4=51;
    public static final int MAX=52;
    public static final int MAXIMIZE=53;
    public static final int MIN=54;
    public static final int MINIMIZE=55;
    public static final int MN=56;
    public static final int NOT=57;
    public static final int OCTAL_ESC=58;
    public static final int OEQ=59;
    public static final int OF=60;
    public static final int OGQ=61;
    public static final int OGT=62;
    public static final int OLQ=63;
    public static final int OLT=64;
    public static final int ONE=65;
    public static final int ONQ=66;
    public static final int OR=67;
    public static final int ORDERBY=68;
    public static final int PAR=69;
    public static final int PARITY=70;
    public static final int PL=71;
    public static final int PPRIO=72;
    public static final int PPRIOD=73;
    public static final int PREDICATE=74;
    public static final int PROP=75;
    public static final int QUEUE=76;
    public static final int RB=77;
    public static final int REV=78;
    public static final int RP=79;
    public static final int RS=80;
    public static final int SATISFY=81;
    public static final int SC=82;
    public static final int SET=83;
    public static final int SIZE=84;
    public static final int SOLVE=85;
    public static final int STREG=86;
    public static final int STRING=87;
    public static final int STRUC1=88;
    public static final int STRUC2=89;
    public static final int SUM=90;
    public static final int TRUE=91;
    public static final int UNICODE_ESC=92;
    public static final int VAR=93;
    public static final int VCARD=94;
    public static final int VNAME=95;
    public static final int WFOR=96;
    public static final int WONE=97;
    public static final int WS=98;

    // delegates
    public TreeParser[] getDelegates() {
        return new TreeParser[] {};
    }

    // delegators


    public FlatzincFullExtWalker(TreeNodeStream input) {
        this(input, new RecognizerSharedState());
    }
    public FlatzincFullExtWalker(TreeNodeStream input, RecognizerSharedState state) {
        super(input, state);
    }

    public String[] getTokenNames() { return FlatzincFullExtWalker.tokenNames; }
    public String getGrammarFileName() { return "parser/flatzinc/FlatzincFullExtWalker.g"; }


    // The flatzinc logger -- 'System.out/err' is fobidden!
    protected static final Logger LOGGER = LoggerFactory.getLogger("fzn");

    // maintains map between name and objects
    public THashMap<String, Object> map;

    public THashMap<String, ArrayList> groups;

    // the solver
    public Solver mSolver;
    // goal configuration
    public GoalConf gc;

    // the layout dedicated to pretty print message wrt to fzn recommendations
    public final FZNLayout mLayout = new FZNLayout();



    // $ANTLR start "flatzinc_model"
    // parser/flatzinc/FlatzincFullExtWalker.g:102:1: flatzinc_model[Solver aSolver, THashMap<String, Object> map, GoalConf gc] : ( pred_decl )* ( param_decl )* ( var_decl )* ( constraint )* ( engine )? solve_goal ;
    public final void flatzinc_model(Solver aSolver, THashMap<String, Object> map, GoalConf gc) throws RecognitionException {
        try {
            // parser/flatzinc/FlatzincFullExtWalker.g:103:2: ( ( pred_decl )* ( param_decl )* ( var_decl )* ( constraint )* ( engine )? solve_goal )
            // parser/flatzinc/FlatzincFullExtWalker.g:104:2: ( pred_decl )* ( param_decl )* ( var_decl )* ( constraint )* ( engine )? solve_goal
            {

            	this.mSolver = aSolver;
            	this.gc = gc;
            	this.map = map;
            	this.groups = new THashMap();
                

            // parser/flatzinc/FlatzincFullExtWalker.g:110:5: ( pred_decl )*
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
            	    // parser/flatzinc/FlatzincFullExtWalker.g:110:6: pred_decl
            	    {
            	    pushFollow(FOLLOW_pred_decl_in_flatzinc_model53);
            	    pred_decl();

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    break loop1;
                }
            } while (true);


            // parser/flatzinc/FlatzincFullExtWalker.g:110:18: ( param_decl )*
            loop2:
            do {
                int alt2=2;
                switch ( input.LA(1) ) {
                case PAR:
                    {
                    alt2=1;
                    }
                    break;

                }

                switch (alt2) {
            	case 1 :
            	    // parser/flatzinc/FlatzincFullExtWalker.g:110:19: param_decl
            	    {
            	    pushFollow(FOLLOW_param_decl_in_flatzinc_model58);
            	    param_decl();

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    break loop2;
                }
            } while (true);


            // parser/flatzinc/FlatzincFullExtWalker.g:110:32: ( var_decl )*
            loop3:
            do {
                int alt3=2;
                switch ( input.LA(1) ) {
                case VAR:
                    {
                    alt3=1;
                    }
                    break;

                }

                switch (alt3) {
            	case 1 :
            	    // parser/flatzinc/FlatzincFullExtWalker.g:110:33: var_decl
            	    {
            	    pushFollow(FOLLOW_var_decl_in_flatzinc_model63);
            	    var_decl();

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    break loop3;
                }
            } while (true);


            // parser/flatzinc/FlatzincFullExtWalker.g:110:44: ( constraint )*
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
            	    // parser/flatzinc/FlatzincFullExtWalker.g:110:45: constraint
            	    {
            	    pushFollow(FOLLOW_constraint_in_flatzinc_model68);
            	    constraint();

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    break loop4;
                }
            } while (true);


            // parser/flatzinc/FlatzincFullExtWalker.g:110:58: ( engine )?
            int alt5=2;
            switch ( input.LA(1) ) {
                case IDENTIFIER:
                    {
                    alt5=1;
                    }
                    break;
            }

            switch (alt5) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:110:58: engine
                    {
                    pushFollow(FOLLOW_engine_in_flatzinc_model72);
                    engine();

                    state._fsp--;


                    }
                    break;

            }


            pushFollow(FOLLOW_solve_goal_in_flatzinc_model75);
            solve_goal();

            state._fsp--;



            	if (LoggerFactory.getLogger("fzn").isInfoEnabled()) {
                    mLayout.setSearchLoop(mSolver.getSearchLoop());
                }
            	

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "flatzinc_model"



    // $ANTLR start "engine"
    // parser/flatzinc/FlatzincFullExtWalker.g:123:1: engine : ( group_decl[arcs] )+ ps= structure[propagationEngine] ;
    public final void engine() throws RecognitionException {
        PropagationStrategy ps =null;



        	ArrayList<Arc> arcs= Arc.populate(mSolver);
        	PropagationEngine propagationEngine = new PropagationEngine(mSolver);
        	
        try {
            // parser/flatzinc/FlatzincFullExtWalker.g:139:5: ( ( group_decl[arcs] )+ ps= structure[propagationEngine] )
            // parser/flatzinc/FlatzincFullExtWalker.g:139:9: ( group_decl[arcs] )+ ps= structure[propagationEngine]
            {
            // parser/flatzinc/FlatzincFullExtWalker.g:139:9: ( group_decl[arcs] )+
            int cnt6=0;
            loop6:
            do {
                int alt6=2;
                switch ( input.LA(1) ) {
                case IDENTIFIER:
                    {
                    alt6=1;
                    }
                    break;

                }

                switch (alt6) {
            	case 1 :
            	    // parser/flatzinc/FlatzincFullExtWalker.g:139:10: group_decl[arcs]
            	    {
            	    pushFollow(FOLLOW_group_decl_in_engine108);
            	    group_decl(arcs);

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    if ( cnt6 >= 1 ) break loop6;
                        EarlyExitException eee =
                            new EarlyExitException(6, input);
                        throw eee;
                }
                cnt6++;
            } while (true);


            pushFollow(FOLLOW_structure_in_engine117);
            ps=structure(propagationEngine);

            state._fsp--;


            }


                if(!arcs.isEmpty()){
                    LOGGER.warn("% Remaining arcs after group declarations");
                    throw new FZNException("Remaining arcs after group declarations");
                }
                if (arcs.isEmpty() && ps == null) {
                    LOGGER.warn("% no engine defined");
                    throw new FZNException("no engine defined");
                }
                mSolver.set(propagationEngine.set(ps));

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "engine"



    // $ANTLR start "group_decl"
    // parser/flatzinc/FlatzincFullExtWalker.g:142:1: group_decl[ArrayList<Arc> arcs] : ^( IDENTIFIER p= predicates ) ;
    public final void group_decl(ArrayList<Arc> arcs) throws RecognitionException {
        CommonTree IDENTIFIER1=null;
        Predicate p =null;


        try {
            // parser/flatzinc/FlatzincFullExtWalker.g:143:5: ( ^( IDENTIFIER p= predicates ) )
            // parser/flatzinc/FlatzincFullExtWalker.g:144:5: ^( IDENTIFIER p= predicates )
            {
            IDENTIFIER1=(CommonTree)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_group_decl139); 

            match(input, Token.DOWN, null); 
            pushFollow(FOLLOW_predicates_in_group_decl143);
            p=predicates();

            state._fsp--;


            match(input, Token.UP, null); 



                ArrayList<Arc> aGroup = Filter.execute(p,arcs);
                if(aGroup.isEmpty()){
                    LOGGER.error("% Empty predicate declaration :"+ (IDENTIFIER1!=null?IDENTIFIER1.getLine():0)+":"+(IDENTIFIER1!=null?IDENTIFIER1.getCharPositionInLine():0));
                    throw new FZNException("Empty predicate declaration");
                }
                Arc.remove(arcs, aGroup);
                groups.put((IDENTIFIER1!=null?IDENTIFIER1.getText():null),aGroup);
                

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "group_decl"



    // $ANTLR start "predicates"
    // parser/flatzinc/FlatzincFullExtWalker.g:158:1: predicates returns [Predicate pred] : (p= predicate | ^( AND (p= predicates )+ ) | ^( OR (p= predicates )+ ) );
    public final Predicate predicates() throws RecognitionException {
        Predicate pred = null;


        Predicate p =null;


        try {
            // parser/flatzinc/FlatzincFullExtWalker.g:159:5: (p= predicate | ^( AND (p= predicates )+ ) | ^( OR (p= predicates )+ ) )
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
            case AND:
                {
                alt9=2;
                }
                break;
            case OR:
                {
                alt9=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 9, 0, input);

                throw nvae;

            }

            switch (alt9) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:159:9: p= predicate
                    {
                    pushFollow(FOLLOW_predicate_in_predicates178);
                    p=predicate();

                    state._fsp--;



                        pred = p;
                        

                    }
                    break;
                case 2 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:164:5: ^( AND (p= predicates )+ )
                    {

                        ArrayList<Predicate> preds = new ArrayList();
                        

                    match(input,AND,FOLLOW_AND_in_predicates203); 

                    match(input, Token.DOWN, null); 
                    // parser/flatzinc/FlatzincFullExtWalker.g:167:11: (p= predicates )+
                    int cnt7=0;
                    loop7:
                    do {
                        int alt7=2;
                        switch ( input.LA(1) ) {
                        case AND:
                        case CARITY:
                        case CNAME:
                        case CSTR:
                        case IN:
                        case NOT:
                        case OR:
                        case PARITY:
                        case PPRIO:
                        case PPRIOD:
                        case PROP:
                        case TRUE:
                        case VAR:
                        case VCARD:
                        case VNAME:
                            {
                            alt7=1;
                            }
                            break;

                        }

                        switch (alt7) {
                    	case 1 :
                    	    // parser/flatzinc/FlatzincFullExtWalker.g:167:12: p= predicates
                    	    {
                    	    pushFollow(FOLLOW_predicates_in_predicates208);
                    	    p=predicates();

                    	    state._fsp--;


                    	    preds.add(p);

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt7 >= 1 ) break loop7;
                                EarlyExitException eee =
                                    new EarlyExitException(7, input);
                                throw eee;
                        }
                        cnt7++;
                    } while (true);


                    match(input, Token.UP, null); 



                        pred = new BoolPredicate(preds, BoolPredicate.TYPE.AND);
                        

                    }
                    break;
                case 3 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:172:5: ^( OR (p= predicates )+ )
                    {

                        ArrayList<Predicate> preds = new ArrayList();
                        

                    match(input,OR,FOLLOW_OR_in_predicates237); 

                    match(input, Token.DOWN, null); 
                    // parser/flatzinc/FlatzincFullExtWalker.g:175:10: (p= predicates )+
                    int cnt8=0;
                    loop8:
                    do {
                        int alt8=2;
                        switch ( input.LA(1) ) {
                        case AND:
                        case CARITY:
                        case CNAME:
                        case CSTR:
                        case IN:
                        case NOT:
                        case OR:
                        case PARITY:
                        case PPRIO:
                        case PPRIOD:
                        case PROP:
                        case TRUE:
                        case VAR:
                        case VCARD:
                        case VNAME:
                            {
                            alt8=1;
                            }
                            break;

                        }

                        switch (alt8) {
                    	case 1 :
                    	    // parser/flatzinc/FlatzincFullExtWalker.g:175:11: p= predicates
                    	    {
                    	    pushFollow(FOLLOW_predicates_in_predicates242);
                    	    p=predicates();

                    	    state._fsp--;


                    	    preds.add(p);

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt8 >= 1 ) break loop8;
                                EarlyExitException eee =
                                    new EarlyExitException(8, input);
                                throw eee;
                        }
                        cnt8++;
                    } while (true);


                    match(input, Token.UP, null); 



                        pred = new BoolPredicate(preds, BoolPredicate.TYPE.OR);
                        

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return pred;
    }
    // $ANTLR end "predicates"



    // $ANTLR start "predicate"
    // parser/flatzinc/FlatzincFullExtWalker.g:182:1: predicate returns [Predicate pred] : ( TRUE |a= attribute o= op i= INT_CONST | ^( IN (i= IDENTIFIER )+ ) | NOT p= predicate );
    public final Predicate predicate() throws RecognitionException {
        Predicate pred = null;


        CommonTree i=null;
        Attribute a =null;

        Operator o =null;

        Predicate p =null;


        try {
            // parser/flatzinc/FlatzincFullExtWalker.g:183:2: ( TRUE |a= attribute o= op i= INT_CONST | ^( IN (i= IDENTIFIER )+ ) | NOT p= predicate )
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
                NoViableAltException nvae =
                    new NoViableAltException("", 11, 0, input);

                throw nvae;

            }

            switch (alt11) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:183:4: TRUE
                    {
                    match(input,TRUE,FOLLOW_TRUE_in_predicate273); 


                    	pred = TruePredicate.singleton;
                    	

                    }
                    break;
                case 2 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:187:4: a= attribute o= op i= INT_CONST
                    {
                    pushFollow(FOLLOW_attribute_in_predicate283);
                    a=attribute();

                    state._fsp--;


                    pushFollow(FOLLOW_op_in_predicate287);
                    o=op();

                    state._fsp--;


                    i=(CommonTree)match(input,INT_CONST,FOLLOW_INT_CONST_in_predicate291); 


                        pred = new IntPredicate(a,o,Integer.valueOf((i!=null?i.getText():null)));
                        

                    }
                    break;
                case 3 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:192:2: ^( IN (i= IDENTIFIER )+ )
                    {

                    	ArrayList<String> ids = new ArrayList();
                    	

                    match(input,IN,FOLLOW_IN_in_predicate308); 

                    match(input, Token.DOWN, null); 
                    // parser/flatzinc/FlatzincFullExtWalker.g:195:11: (i= IDENTIFIER )+
                    int cnt10=0;
                    loop10:
                    do {
                        int alt10=2;
                        switch ( input.LA(1) ) {
                        case IDENTIFIER:
                            {
                            alt10=1;
                            }
                            break;

                        }

                        switch (alt10) {
                    	case 1 :
                    	    // parser/flatzinc/FlatzincFullExtWalker.g:195:12: i= IDENTIFIER
                    	    {
                    	    i=(CommonTree)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_predicate313); 

                    	    ids.add((i!=null?i.getText():null));

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt10 >= 1 ) break loop10;
                                EarlyExitException eee =
                                    new EarlyExitException(10, input);
                                throw eee;
                        }
                        cnt10++;
                    } while (true);


                    match(input, Token.UP, null); 



                    	pred = new ExtPredicate(ids, map);
                    	

                    }
                    break;
                case 4 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:199:4: NOT p= predicate
                    {
                    match(input,NOT,FOLLOW_NOT_in_predicate325); 

                    pushFollow(FOLLOW_predicate_in_predicate329);
                    p=predicate();

                    state._fsp--;



                        pred = new NotPredicate(p);
                        

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return pred;
    }
    // $ANTLR end "predicate"



    // $ANTLR start "attribute"
    // parser/flatzinc/FlatzincFullExtWalker.g:207:1: attribute returns [Attribute attr] : ( VAR | CSTR | PROP | VNAME | VCARD | CNAME | CARITY | PPRIO | PARITY | PPRIOD );
    public final Attribute attribute() throws RecognitionException {
        Attribute attr = null;


        try {
            // parser/flatzinc/FlatzincFullExtWalker.g:208:5: ( VAR | CSTR | PROP | VNAME | VCARD | CNAME | CARITY | PPRIO | PARITY | PPRIOD )
            int alt12=10;
            switch ( input.LA(1) ) {
            case VAR:
                {
                alt12=1;
                }
                break;
            case CSTR:
                {
                alt12=2;
                }
                break;
            case PROP:
                {
                alt12=3;
                }
                break;
            case VNAME:
                {
                alt12=4;
                }
                break;
            case VCARD:
                {
                alt12=5;
                }
                break;
            case CNAME:
                {
                alt12=6;
                }
                break;
            case CARITY:
                {
                alt12=7;
                }
                break;
            case PPRIO:
                {
                alt12=8;
                }
                break;
            case PARITY:
                {
                alt12=9;
                }
                break;
            case PPRIOD:
                {
                alt12=10;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 12, 0, input);

                throw nvae;

            }

            switch (alt12) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:208:9: VAR
                    {
                    match(input,VAR,FOLLOW_VAR_in_attribute356); 

                    attr = Attribute.VAR;

                    }
                    break;
                case 2 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:209:9: CSTR
                    {
                    match(input,CSTR,FOLLOW_CSTR_in_attribute372); 

                    attr = Attribute.CSTR;

                    }
                    break;
                case 3 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:210:9: PROP
                    {
                    match(input,PROP,FOLLOW_PROP_in_attribute387); 

                    attr = Attribute.PROP;

                    }
                    break;
                case 4 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:211:9: VNAME
                    {
                    match(input,VNAME,FOLLOW_VNAME_in_attribute402); 

                    attr = Attribute.VNAME;

                    }
                    break;
                case 5 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:212:9: VCARD
                    {
                    match(input,VCARD,FOLLOW_VCARD_in_attribute417); 

                    attr = Attribute.VCARD;

                    }
                    break;
                case 6 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:213:9: CNAME
                    {
                    match(input,CNAME,FOLLOW_CNAME_in_attribute431); 

                    attr = Attribute.CNAME;

                    }
                    break;
                case 7 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:214:9: CARITY
                    {
                    match(input,CARITY,FOLLOW_CARITY_in_attribute446); 

                    attr = Attribute.CARITY;

                    }
                    break;
                case 8 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:215:9: PPRIO
                    {
                    match(input,PPRIO,FOLLOW_PPRIO_in_attribute459); 

                    attr = Attribute.PPRIO;

                    }
                    break;
                case 9 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:216:9: PARITY
                    {
                    match(input,PARITY,FOLLOW_PARITY_in_attribute473); 

                    attr = Attribute.PARITY;

                    }
                    break;
                case 10 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:217:9: PPRIOD
                    {
                    match(input,PPRIOD,FOLLOW_PPRIOD_in_attribute486); 

                    attr = Attribute.PPRIOD;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return attr;
    }
    // $ANTLR end "attribute"



    // $ANTLR start "op"
    // parser/flatzinc/FlatzincFullExtWalker.g:221:1: op returns [Operator value] : ( OEQ | ONQ | OLT | OGT | OLQ | OGQ );
    public final Operator op() throws RecognitionException {
        Operator value = null;


        try {
            // parser/flatzinc/FlatzincFullExtWalker.g:222:5: ( OEQ | ONQ | OLT | OGT | OLQ | OGQ )
            int alt13=6;
            switch ( input.LA(1) ) {
            case OEQ:
                {
                alt13=1;
                }
                break;
            case ONQ:
                {
                alt13=2;
                }
                break;
            case OLT:
                {
                alt13=3;
                }
                break;
            case OGT:
                {
                alt13=4;
                }
                break;
            case OLQ:
                {
                alt13=5;
                }
                break;
            case OGQ:
                {
                alt13=6;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 13, 0, input);

                throw nvae;

            }

            switch (alt13) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:222:9: OEQ
                    {
                    match(input,OEQ,FOLLOW_OEQ_in_op514); 

                    value = Operator.EQ;

                    }
                    break;
                case 2 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:223:9: ONQ
                    {
                    match(input,ONQ,FOLLOW_ONQ_in_op526); 

                    value = Operator.NQ;

                    }
                    break;
                case 3 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:224:9: OLT
                    {
                    match(input,OLT,FOLLOW_OLT_in_op538); 

                    value = Operator.LT;

                    }
                    break;
                case 4 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:225:9: OGT
                    {
                    match(input,OGT,FOLLOW_OGT_in_op550); 

                    value = Operator.GT;

                    }
                    break;
                case 5 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:226:9: OLQ
                    {
                    match(input,OLQ,FOLLOW_OLQ_in_op562); 

                    value = Operator.LQ;

                    }
                    break;
                case 6 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:227:9: OGQ
                    {
                    match(input,OGQ,FOLLOW_OGQ_in_op574); 

                    value = Operator.GQ;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return value;
    }
    // $ANTLR end "op"



    // $ANTLR start "structure"
    // parser/flatzinc/FlatzincFullExtWalker.g:233:1: structure[PropagationEngine pe] returns [PropagationStrategy ps] : (s= struct[pe] |sr= struct_reg[pe] );
    public final PropagationStrategy structure(PropagationEngine pe) throws RecognitionException {
        PropagationStrategy ps = null;


        PropagationStrategy s =null;

        PropagationStrategy sr =null;


        try {
            // parser/flatzinc/FlatzincFullExtWalker.g:234:2: (s= struct[pe] |sr= struct_reg[pe] )
            int alt14=2;
            switch ( input.LA(1) ) {
            case STRUC1:
            case STRUC2:
                {
                alt14=1;
                }
                break;
            case STREG:
                {
                alt14=2;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 14, 0, input);

                throw nvae;

            }

            switch (alt14) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:234:4: s= struct[pe]
                    {
                    pushFollow(FOLLOW_struct_in_structure603);
                    s=struct(pe);

                    state._fsp--;



                    	ps = s;
                    	

                    }
                    break;
                case 2 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:238:6: sr= struct_reg[pe]
                    {
                    pushFollow(FOLLOW_struct_reg_in_structure616);
                    sr=struct_reg(pe);

                    state._fsp--;



                    	ps = sr;
                    	

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ps;
    }
    // $ANTLR end "structure"



    // $ANTLR start "struct"
    // parser/flatzinc/FlatzincFullExtWalker.g:244:1: struct[PropagationEngine pe] returns [PropagationStrategy item] : ( ^( STRUC1 (element= elt[pe] )+ c= coll[elements, ca] ) | ^( STRUC2 (element= elt[pe] )+ ca= comb_attr c= coll[elements, ca] ) );
    public final PropagationStrategy struct(PropagationEngine pe) throws RecognitionException {
        PropagationStrategy item = null;


        ISchedulable[] element =null;

        PropagationStrategy c =null;

        CombinedAttribute ca =null;



             ArrayList<ISchedulable> elements = new ArrayList<ISchedulable>();

        try {
            // parser/flatzinc/FlatzincFullExtWalker.g:251:5: ( ^( STRUC1 (element= elt[pe] )+ c= coll[elements, ca] ) | ^( STRUC2 (element= elt[pe] )+ ca= comb_attr c= coll[elements, ca] ) )
            int alt17=2;
            switch ( input.LA(1) ) {
            case STRUC1:
                {
                alt17=1;
                }
                break;
            case STRUC2:
                {
                alt17=2;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 17, 0, input);

                throw nvae;

            }

            switch (alt17) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:251:7: ^( STRUC1 (element= elt[pe] )+ c= coll[elements, ca] )
                    {
                    match(input,STRUC1,FOLLOW_STRUC1_in_struct649); 

                    match(input, Token.DOWN, null); 
                    // parser/flatzinc/FlatzincFullExtWalker.g:251:16: (element= elt[pe] )+
                    int cnt15=0;
                    loop15:
                    do {
                        int alt15=2;
                        switch ( input.LA(1) ) {
                        case IDENTIFIER:
                        case STREG:
                        case STRUC1:
                        case STRUC2:
                            {
                            alt15=1;
                            }
                            break;

                        }

                        switch (alt15) {
                    	case 1 :
                    	    // parser/flatzinc/FlatzincFullExtWalker.g:251:17: element= elt[pe]
                    	    {
                    	    pushFollow(FOLLOW_elt_in_struct656);
                    	    element=elt(pe);

                    	    state._fsp--;


                    	    elements.addAll(Arrays.asList(element));

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt15 >= 1 ) break loop15;
                                EarlyExitException eee =
                                    new EarlyExitException(15, input);
                                throw eee;
                        }
                        cnt15++;
                    } while (true);


                    pushFollow(FOLLOW_coll_in_struct664);
                    c=coll(elements, ca);

                    state._fsp--;


                    match(input, Token.UP, null); 


                    }
                    break;
                case 2 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:252:6: ^( STRUC2 (element= elt[pe] )+ ca= comb_attr c= coll[elements, ca] )
                    {
                    match(input,STRUC2,FOLLOW_STRUC2_in_struct674); 

                    match(input, Token.DOWN, null); 
                    // parser/flatzinc/FlatzincFullExtWalker.g:252:15: (element= elt[pe] )+
                    int cnt16=0;
                    loop16:
                    do {
                        int alt16=2;
                        switch ( input.LA(1) ) {
                        case IDENTIFIER:
                        case STREG:
                        case STRUC1:
                        case STRUC2:
                            {
                            alt16=1;
                            }
                            break;

                        }

                        switch (alt16) {
                    	case 1 :
                    	    // parser/flatzinc/FlatzincFullExtWalker.g:252:16: element= elt[pe]
                    	    {
                    	    pushFollow(FOLLOW_elt_in_struct681);
                    	    element=elt(pe);

                    	    state._fsp--;


                    	    elements.addAll(Arrays.asList(element));

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt16 >= 1 ) break loop16;
                                EarlyExitException eee =
                                    new EarlyExitException(16, input);
                                throw eee;
                        }
                        cnt16++;
                    } while (true);


                    pushFollow(FOLLOW_comb_attr_in_struct689);
                    ca=comb_attr();

                    state._fsp--;


                    pushFollow(FOLLOW_coll_in_struct693);
                    c=coll(elements, ca);

                    state._fsp--;


                    match(input, Token.UP, null); 


                    }
                    break;

            }

                 item = c;

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return item;
    }
    // $ANTLR end "struct"



    // $ANTLR start "struct_reg"
    // parser/flatzinc/FlatzincFullExtWalker.g:255:1: struct_reg[PropagationEngine pe] returns [PropagationStrategy item] : ( ^( STREG id= IDENTIFIER . . ) | ^( STREG id= IDENTIFIER ca= comb_attr . . ) );
    public final PropagationStrategy struct_reg(PropagationEngine pe) throws RecognitionException {
        PropagationStrategy item = null;


        CommonTree id=null;
        CombinedAttribute ca =null;



            int m_idx = -1,c_idx = -1;

        try {
            // parser/flatzinc/FlatzincFullExtWalker.g:278:2: ( ^( STREG id= IDENTIFIER . . ) | ^( STREG id= IDENTIFIER ca= comb_attr . . ) )
            int alt18=2;
            alt18 = dfa18.predict(input);
            switch (alt18) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:278:4: ^( STREG id= IDENTIFIER . . )
                    {
                    match(input,STREG,FOLLOW_STREG_in_struct_reg721); 

                    match(input, Token.DOWN, null); 
                    id=(CommonTree)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_struct_reg725); 

                    m_idx = input.mark();

                    matchAny(input); 

                    c_idx = input.mark();

                    matchAny(input); 

                    match(input, Token.UP, null); 


                    }
                    break;
                case 2 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:279:9: ^( STREG id= IDENTIFIER ca= comb_attr . . )
                    {
                    match(input,STREG,FOLLOW_STREG_in_struct_reg746); 

                    match(input, Token.DOWN, null); 
                    id=(CommonTree)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_struct_reg750); 

                    pushFollow(FOLLOW_comb_attr_in_struct_reg754);
                    ca=comb_attr();

                    state._fsp--;


                    m_idx = input.mark();

                    matchAny(input); 

                    c_idx = input.mark();

                    matchAny(input); 

                    match(input, Token.UP, null); 


                    }
                    break;

            }

            //    String id = $IDENTIFIER.text;
                ArrayList<Arc> arcs = groups.get((id!=null?id.getText():null));
                if(arcs == null){
                    LOGGER.error("% Unknown group_decl :"+id);
                    throw new FZNException("Unknown group_decl :"+id);
                }
                for(int k = 0; k < arcs.size(); k++){
                    pe.declareArc(arcs.get(k));
                }
                input.seek(m_idx);
                ArrayList<PropagationStrategy> pss = many(arcs).pss;
                input.release(m_idx);
                input.seek(c_idx);
                item = coll(pss, ca);
                input.release(c_idx);
                //BEWARE: kind of ugly patch...
                match(input, Token.UP, null);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return item;
    }
    // $ANTLR end "struct_reg"



    // $ANTLR start "elt"
    // parser/flatzinc/FlatzincFullExtWalker.g:282:1: elt[PropagationEngine pe] returns [ISchedulable[] items] : (s= struct[pe] |sr= struct_reg[pe] | IDENTIFIER ( KEY a= attribute )? );
    public final ISchedulable[] elt(PropagationEngine pe) throws RecognitionException {
        ISchedulable[] items = null;


        CommonTree IDENTIFIER2=null;
        PropagationStrategy s =null;

        PropagationStrategy sr =null;

        Attribute a =null;


        try {
            // parser/flatzinc/FlatzincFullExtWalker.g:283:5: (s= struct[pe] |sr= struct_reg[pe] | IDENTIFIER ( KEY a= attribute )? )
            int alt20=3;
            switch ( input.LA(1) ) {
            case STRUC1:
            case STRUC2:
                {
                alt20=1;
                }
                break;
            case STREG:
                {
                alt20=2;
                }
                break;
            case IDENTIFIER:
                {
                alt20=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 20, 0, input);

                throw nvae;

            }

            switch (alt20) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:283:7: s= struct[pe]
                    {
                    pushFollow(FOLLOW_struct_in_elt786);
                    s=struct(pe);

                    state._fsp--;



                        items = new ISchedulable[]{s};
                        

                    }
                    break;
                case 2 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:287:4: sr= struct_reg[pe]
                    {
                    pushFollow(FOLLOW_struct_reg_in_elt800);
                    sr=struct_reg(pe);

                    state._fsp--;



                    	items = new ISchedulable[]{sr};
                    	

                    }
                    break;
                case 3 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:291:4: IDENTIFIER ( KEY a= attribute )?
                    {
                    IDENTIFIER2=(CommonTree)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_elt809); 

                    // parser/flatzinc/FlatzincFullExtWalker.g:291:15: ( KEY a= attribute )?
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
                            // parser/flatzinc/FlatzincFullExtWalker.g:291:16: KEY a= attribute
                            {
                            match(input,KEY,FOLLOW_KEY_in_elt812); 

                            pushFollow(FOLLOW_attribute_in_elt816);
                            a=attribute();

                            state._fsp--;


                            }
                            break;

                    }



                    	String id = (IDENTIFIER2!=null?IDENTIFIER2.getText():null);
                    	ArrayList<Arc> scope = groups.get(id);
                    	// iterate over in to create arcs
                        Arc[] arcs = scope.toArray(new Arc[scope.size()]);
                        for(int i = 0 ; i < scope.size(); i++){
                            Arc arc = scope.get(i);
                            pe.declareArc(arc);
                            arc.attachEvaluator(a);
                        }
                        items = arcs;
                    	

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return items;
    }
    // $ANTLR end "elt"


    public static class many_return extends TreeRuleReturnScope {
        public ArrayList<PropagationStrategy> pss;
        public int depth;
    };


    // $ANTLR start "many"
    // parser/flatzinc/FlatzincFullExtWalker.g:306:1: many[ArrayList<Arc> in] returns [ArrayList<PropagationStrategy> pss, int depth] : ( ^( MANY1 a= attribute . ) | ^( MANY2 a= attribute ca= comb_attr . ) | ^( MANY3 a= attribute . . ) | ^( MANY4 a= attribute ca= comb_attr . . ) );
    public final FlatzincFullExtWalker.many_return many(ArrayList<Arc> in) throws RecognitionException {
        FlatzincFullExtWalker.many_return retval = new FlatzincFullExtWalker.many_return();
        retval.start = input.LT(1);


        Attribute a =null;

        CombinedAttribute ca =null;



            retval.pss = new ArrayList<PropagationStrategy>();
            int c_idx = -1, m_idx = -1;


        try {
            // parser/flatzinc/FlatzincFullExtWalker.g:417:5: ( ^( MANY1 a= attribute . ) | ^( MANY2 a= attribute ca= comb_attr . ) | ^( MANY3 a= attribute . . ) | ^( MANY4 a= attribute ca= comb_attr . . ) )
            int alt21=4;
            switch ( input.LA(1) ) {
            case MANY1:
                {
                alt21=1;
                }
                break;
            case MANY2:
                {
                alt21=2;
                }
                break;
            case MANY3:
                {
                alt21=3;
                }
                break;
            case MANY4:
                {
                alt21=4;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 21, 0, input);

                throw nvae;

            }

            switch (alt21) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:417:9: ^( MANY1 a= attribute . )
                    {
                    match(input,MANY1,FOLLOW_MANY1_in_many856); 

                    match(input, Token.DOWN, null); 
                    pushFollow(FOLLOW_attribute_in_many860);
                    a=attribute();

                    state._fsp--;


                    c_idx = input.mark();

                    matchAny(input); 

                    match(input, Token.UP, null); 


                    }
                    break;
                case 2 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:418:9: ^( MANY2 a= attribute ca= comb_attr . )
                    {
                    match(input,MANY2,FOLLOW_MANY2_in_many877); 

                    match(input, Token.DOWN, null); 
                    pushFollow(FOLLOW_attribute_in_many881);
                    a=attribute();

                    state._fsp--;


                    pushFollow(FOLLOW_comb_attr_in_many885);
                    ca=comb_attr();

                    state._fsp--;


                    c_idx = input.mark();

                    matchAny(input); 

                    match(input, Token.UP, null); 


                    }
                    break;
                case 3 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:419:9: ^( MANY3 a= attribute . . )
                    {
                    match(input,MANY3,FOLLOW_MANY3_in_many902); 

                    match(input, Token.DOWN, null); 
                    pushFollow(FOLLOW_attribute_in_many906);
                    a=attribute();

                    state._fsp--;


                    m_idx = input.mark();

                    matchAny(input); 

                    c_idx = input.mark();

                    matchAny(input); 

                    match(input, Token.UP, null); 


                    }
                    break;
                case 4 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:420:9: ^( MANY4 a= attribute ca= comb_attr . . )
                    {
                    match(input,MANY4,FOLLOW_MANY4_in_many927); 

                    match(input, Token.DOWN, null); 
                    pushFollow(FOLLOW_attribute_in_many931);
                    a=attribute();

                    state._fsp--;


                    pushFollow(FOLLOW_comb_attr_in_many935);
                    ca=comb_attr();

                    state._fsp--;


                    m_idx = input.mark();

                    matchAny(input); 

                    c_idx = input.mark();

                    matchAny(input); 

                    match(input, Token.UP, null); 


                    }
                    break;

            }

                if(m_idx == -1){ // we are in a "last many" case
                    // Build as many list as different values of "attribute" in "in"
                    // if "attribute" is dynamic, the overall range of values must be created
                    if(a.isDynamic()){
                         int max = 0;
                         for(int i = 0; i< in.size(); i++){
                             Arc arc = in.get(i);
                             int ev = a.eval(arc);
                             if(ev > max)max = ev;
                         }

                         input.seek(c_idx);
                         PropagationStrategy _ps = coll(in, ca);
                         input.release(c_idx);

                        Switcher sw = new Switcher(a, 0,max, _ps, in.toArray(new Arc[in.size()]));
                        retval.pss.addAll(Arrays.asList(sw.getPS()));
                    }else{
                        // otherwise, create as many "coll" as value of attribute
                        TIntObjectHashMap<ArrayList<Arc>> sublists = new TIntObjectHashMap<ArrayList<Arc>>();
                        for(int i = 0; i< in.size(); i++){
                            Arc arc = in.get(i);
                            int ev = a.eval(arc);
                            if(!sublists.contains(ev)){
                                ArrayList<Arc> evlist = new ArrayList<Arc>();
                                sublists.put(ev, evlist);
                            }
                            sublists.get(ev).add(arc);
                        }

                        int[] evs = sublists.keys();
                        for (int k = 0; k < evs.length; k++) {
                            int ev = evs[k];
                            input.seek(c_idx);
                            retval.pss.add(coll(sublists.get(ev), ca));
                        }
                        input.release(c_idx);
                        retval.depth = 0;
                    }
                }else{ // we are in a recursive many
                    if(a.isDynamic()){
                        // Build as many list as different values of "attribute" in "in"
                        int max = 0;
                        for(int i = 0; i< in.size(); i++){
                            Arc arc = in.get(i);
                            int ev = a.eval(arc);
                            if (max < ev) {
                                max = ev;
                            }
                        }
                        int _d = 0;
                        input.seek(m_idx);
                        FlatzincFullExtWalker.many_return manyret = many(in);
                        // 1. get depth
                        _d = manyret.depth;
                        // 2. build correct attribute (including depth
                        ArrayList<AttributeOperator> aos = new ArrayList<AttributeOperator>();
                        aos.add(AttributeOperator.ANY);
                        for(int i = 1 ; i < _d; i++){
                            aos.add(AttributeOperator.ANY);
                        }
                        CombinedAttribute _ca = new CombinedAttribute(aos, a);
                        // 3. build on coll
                        input.seek(c_idx);
                        PropagationStrategy _ps = coll(manyret.pss, ca);
                        input.release(c_idx);

                        Switcher sw = new Switcher(_ca, 0,max, _ps, manyret.pss.toArray(new PropagationStrategy[manyret.pss.size()]));
                        retval.pss.addAll(Arrays.asList(sw.getPS()));
                        retval.depth = _d +1;
                    }else{
                        // Build as many list as different values of "attribute" in "in"
                        TIntObjectHashMap<ArrayList<Arc>> sublists = new TIntObjectHashMap<ArrayList<Arc>>();
                        for(int i = 0; i< in.size(); i++){
                            Arc arc = in.get(i);
                            int ev = a.eval(arc);
                            if(!sublists.contains(ev)){
                                ArrayList<Arc> evlist = new ArrayList<Arc>();
                                sublists.put(ev, evlist);
                            }
                            sublists.get(ev).add(arc);
                        }
                        int[] evs = sublists.keys();
                        ArrayList<ArrayList<PropagationStrategy>> _pss = new ArrayList<ArrayList<PropagationStrategy>>(evs.length);
                        int _d = 0;
                        for (int k = 0; k < evs.length; k++) {
                            int ev = evs[k];
                            input.seek(m_idx);
                            FlatzincFullExtWalker.many_return manyret = many(sublists.get(ev));
                            _pss.add(manyret.pss);
                            assert (k == 0 || _d == manyret.depth);
                            _d = manyret.depth;
                        }
                        retval.depth = _d +1;
                        input.release(m_idx);
                        for (int p = 0; p < _pss.size(); p++) {
                            ArrayList<PropagationStrategy> _ps = _pss.get(p);
                            input.seek(c_idx);
                            retval.pss.add(coll(_ps, ca));
                        }
                        input.release(c_idx);
                    }
                }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "many"



    // $ANTLR start "coll"
    // parser/flatzinc/FlatzincFullExtWalker.g:427:1: coll[ArrayList<? extends ISchedulable> elements, CombinedAttribute ca] returns [PropagationStrategy ps] : ( ^( QUEUE it= qiter ) | ^( LIST (r= REV )? it= liter ) | ^( HEAP (m= MAX )? it= qiter ) );
    public final PropagationStrategy coll(ArrayList<? extends ISchedulable> elements, CombinedAttribute ca) throws RecognitionException {
        PropagationStrategy ps = null;


        CommonTree r=null;
        CommonTree m=null;
        Iterator it =null;



            if(elements.isEmpty()){
                LOGGER.error("% Create a empty collection");
                throw new FZNException("Create a empty collection");
            }else if(elements.size() == 1){
                LOGGER.warn("% Create a collection with a single element");
            }

        try {
            // parser/flatzinc/FlatzincFullExtWalker.g:437:5: ( ^( QUEUE it= qiter ) | ^( LIST (r= REV )? it= liter ) | ^( HEAP (m= MAX )? it= qiter ) )
            int alt24=3;
            switch ( input.LA(1) ) {
            case QUEUE:
                {
                alt24=1;
                }
                break;
            case LIST:
                {
                alt24=2;
                }
                break;
            case HEAP:
                {
                alt24=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 24, 0, input);

                throw nvae;

            }

            switch (alt24) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:437:7: ^( QUEUE it= qiter )
                    {
                    match(input,QUEUE,FOLLOW_QUEUE_in_coll978); 

                    match(input, Token.DOWN, null); 
                    pushFollow(FOLLOW_qiter_in_coll982);
                    it=qiter();

                    state._fsp--;


                    match(input, Token.UP, null); 



                        ps = new Queue(elements.toArray(new ISchedulable[elements.size()]));
                        ps = it.set(ps);
                        ps.attachEvaluator(ca);
                        

                    }
                    break;
                case 2 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:443:7: ^( LIST (r= REV )? it= liter )
                    {
                    match(input,LIST,FOLLOW_LIST_in_coll998); 

                    match(input, Token.DOWN, null); 
                    // parser/flatzinc/FlatzincFullExtWalker.g:443:15: (r= REV )?
                    int alt22=2;
                    switch ( input.LA(1) ) {
                        case REV:
                            {
                            alt22=1;
                            }
                            break;
                    }

                    switch (alt22) {
                        case 1 :
                            // parser/flatzinc/FlatzincFullExtWalker.g:443:15: r= REV
                            {
                            r=(CommonTree)match(input,REV,FOLLOW_REV_in_coll1002); 

                            }
                            break;

                    }


                    pushFollow(FOLLOW_liter_in_coll1007);
                    it=liter();

                    state._fsp--;


                    match(input, Token.UP, null); 



                        ISchedulable[] elts = elements.toArray(new ISchedulable[elements.size()]);
                        // check if an order is required
                        boolean order = false;
                        for (int i = 0; i < elts.length; i++) {
                            try {
                                elts[i].evaluate();
                                order = true;
                            } catch (NullPointerException npe) {
                                if (order) {
                                    LOGGER.error("% Cannot sort the collection, keys are missing");
                                    throw new FZNException("Cannot sort the collection, keys are missing");
                                }
                            }
                        }
                        ps = new Sort(order, r != null, elts);
                        ps = it.set(ps);
                        ps.attachEvaluator(ca);
                        

                    }
                    break;
                case 3 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:463:7: ^( HEAP (m= MAX )? it= qiter )
                    {
                    match(input,HEAP,FOLLOW_HEAP_in_coll1023); 

                    match(input, Token.DOWN, null); 
                    // parser/flatzinc/FlatzincFullExtWalker.g:463:15: (m= MAX )?
                    int alt23=2;
                    switch ( input.LA(1) ) {
                        case MAX:
                            {
                            alt23=1;
                            }
                            break;
                    }

                    switch (alt23) {
                        case 1 :
                            // parser/flatzinc/FlatzincFullExtWalker.g:463:15: m= MAX
                            {
                            m=(CommonTree)match(input,MAX,FOLLOW_MAX_in_coll1027); 

                            }
                            break;

                    }


                    pushFollow(FOLLOW_qiter_in_coll1032);
                    it=qiter();

                    state._fsp--;


                    match(input, Token.UP, null); 



                    	ISchedulable[] elts = elements.toArray(new ISchedulable[elements.size()]);
                        for (int i = 0; i < elts.length; i++) {
                            try {
                                elts[i].evaluate();
                            } catch (NullPointerException npe) {
                                    LOGGER.error("% Cannot sort the collection, keys are missing");
                                    throw new FZNException("Cannot sort the collection, keys are missing");
                            }
                        }
                        ps = new SortDyn(m != null, elts);
                        ps = it.set(ps);
                        ps.attachEvaluator(ca);
                        

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ps;
    }
    // $ANTLR end "coll"



    // $ANTLR start "qiter"
    // parser/flatzinc/FlatzincFullExtWalker.g:481:1: qiter returns [Iterator it] : ( ONE | WONE );
    public final Iterator qiter() throws RecognitionException {
        Iterator it = null;


        try {
            // parser/flatzinc/FlatzincFullExtWalker.g:482:5: ( ONE | WONE )
            int alt25=2;
            switch ( input.LA(1) ) {
            case ONE:
                {
                alt25=1;
                }
                break;
            case WONE:
                {
                alt25=2;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 25, 0, input);

                throw nvae;

            }

            switch (alt25) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:482:9: ONE
                    {
                    match(input,ONE,FOLLOW_ONE_in_qiter1059); 

                    it = Iterator.ONE;

                    }
                    break;
                case 2 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:483:9: WONE
                    {
                    match(input,WONE,FOLLOW_WONE_in_qiter1071); 

                    it = Iterator.WONE;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return it;
    }
    // $ANTLR end "qiter"



    // $ANTLR start "liter"
    // parser/flatzinc/FlatzincFullExtWalker.g:486:1: liter returns [Iterator it] : (q= qiter | FOR | WFOR );
    public final Iterator liter() throws RecognitionException {
        Iterator it = null;


        Iterator q =null;


        try {
            // parser/flatzinc/FlatzincFullExtWalker.g:487:5: (q= qiter | FOR | WFOR )
            int alt26=3;
            switch ( input.LA(1) ) {
            case ONE:
            case WONE:
                {
                alt26=1;
                }
                break;
            case FOR:
                {
                alt26=2;
                }
                break;
            case WFOR:
                {
                alt26=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 26, 0, input);

                throw nvae;

            }

            switch (alt26) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:487:9: q= qiter
                    {
                    pushFollow(FOLLOW_qiter_in_liter1103);
                    q=qiter();

                    state._fsp--;


                    it = q;

                    }
                    break;
                case 2 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:488:9: FOR
                    {
                    match(input,FOR,FOLLOW_FOR_in_liter1115); 

                    it = Iterator.FOR;

                    }
                    break;
                case 3 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:489:9: WFOR
                    {
                    match(input,WFOR,FOLLOW_WFOR_in_liter1127); 

                    it = Iterator.WFOR;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return it;
    }
    // $ANTLR end "liter"



    // $ANTLR start "comb_attr"
    // parser/flatzinc/FlatzincFullExtWalker.g:494:1: comb_attr returns [CombinedAttribute ca] : ( ^( CA1 (ao= attr_op )* (ea= attribute )? ) | ^( CA2 (ao= attr_op )+ ea= attribute ) );
    public final CombinedAttribute comb_attr() throws RecognitionException {
        CombinedAttribute ca = null;


        AttributeOperator ao =null;

        Attribute ea =null;



            ArrayList<AttributeOperator> aos = new ArrayList<AttributeOperator>();

        try {
            // parser/flatzinc/FlatzincFullExtWalker.g:501:5: ( ^( CA1 (ao= attr_op )* (ea= attribute )? ) | ^( CA2 (ao= attr_op )+ ea= attribute ) )
            int alt30=2;
            switch ( input.LA(1) ) {
            case CA1:
                {
                alt30=1;
                }
                break;
            case CA2:
                {
                alt30=2;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 30, 0, input);

                throw nvae;

            }

            switch (alt30) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:501:7: ^( CA1 (ao= attr_op )* (ea= attribute )? )
                    {
                    match(input,CA1,FOLLOW_CA1_in_comb_attr1161); 

                    if ( input.LA(1)==Token.DOWN ) {
                        match(input, Token.DOWN, null); 
                        // parser/flatzinc/FlatzincFullExtWalker.g:501:13: (ao= attr_op )*
                        loop27:
                        do {
                            int alt27=2;
                            switch ( input.LA(1) ) {
                            case ANY:
                            case MAX:
                            case MIN:
                            case SIZE:
                            case SUM:
                                {
                                alt27=1;
                                }
                                break;

                            }

                            switch (alt27) {
                        	case 1 :
                        	    // parser/flatzinc/FlatzincFullExtWalker.g:501:14: ao= attr_op
                        	    {
                        	    pushFollow(FOLLOW_attr_op_in_comb_attr1168);
                        	    ao=attr_op();

                        	    state._fsp--;


                        	    aos.add(ao);

                        	    }
                        	    break;

                        	default :
                        	    break loop27;
                            }
                        } while (true);


                        // parser/flatzinc/FlatzincFullExtWalker.g:501:45: (ea= attribute )?
                        int alt28=2;
                        switch ( input.LA(1) ) {
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
                                alt28=1;
                                }
                                break;
                        }

                        switch (alt28) {
                            case 1 :
                                // parser/flatzinc/FlatzincFullExtWalker.g:501:45: ea= attribute
                                {
                                pushFollow(FOLLOW_attribute_in_comb_attr1175);
                                ea=attribute();

                                state._fsp--;


                                }
                                break;

                        }


                        match(input, Token.UP, null); 
                    }


                    }
                    break;
                case 2 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:502:9: ^( CA2 (ao= attr_op )+ ea= attribute )
                    {
                    match(input,CA2,FOLLOW_CA2_in_comb_attr1188); 

                    match(input, Token.DOWN, null); 
                    // parser/flatzinc/FlatzincFullExtWalker.g:502:15: (ao= attr_op )+
                    int cnt29=0;
                    loop29:
                    do {
                        int alt29=2;
                        switch ( input.LA(1) ) {
                        case ANY:
                        case MAX:
                        case MIN:
                        case SIZE:
                        case SUM:
                            {
                            alt29=1;
                            }
                            break;

                        }

                        switch (alt29) {
                    	case 1 :
                    	    // parser/flatzinc/FlatzincFullExtWalker.g:502:16: ao= attr_op
                    	    {
                    	    pushFollow(FOLLOW_attr_op_in_comb_attr1195);
                    	    ao=attr_op();

                    	    state._fsp--;


                    	    aos.add(ao);

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt29 >= 1 ) break loop29;
                                EarlyExitException eee =
                                    new EarlyExitException(29, input);
                                throw eee;
                        }
                        cnt29++;
                    } while (true);


                    pushFollow(FOLLOW_attribute_in_comb_attr1202);
                    ea=attribute();

                    state._fsp--;


                    match(input, Token.UP, null); 


                    }
                    break;

            }

                ca = new CombinedAttribute(aos, ea);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ca;
    }
    // $ANTLR end "comb_attr"



    // $ANTLR start "attr_op"
    // parser/flatzinc/FlatzincFullExtWalker.g:505:1: attr_op returns [AttributeOperator ao] : ( ANY | MIN | MAX | SUM | SIZE );
    public final AttributeOperator attr_op() throws RecognitionException {
        AttributeOperator ao = null;


        try {
            // parser/flatzinc/FlatzincFullExtWalker.g:506:5: ( ANY | MIN | MAX | SUM | SIZE )
            int alt31=5;
            switch ( input.LA(1) ) {
            case ANY:
                {
                alt31=1;
                }
                break;
            case MIN:
                {
                alt31=2;
                }
                break;
            case MAX:
                {
                alt31=3;
                }
                break;
            case SUM:
                {
                alt31=4;
                }
                break;
            case SIZE:
                {
                alt31=5;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 31, 0, input);

                throw nvae;

            }

            switch (alt31) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:506:7: ANY
                    {
                    match(input,ANY,FOLLOW_ANY_in_attr_op1220); 

                    ao = AttributeOperator.ANY;

                    }
                    break;
                case 2 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:507:9: MIN
                    {
                    match(input,MIN,FOLLOW_MIN_in_attr_op1232); 

                    ao = AttributeOperator.MIN;

                    }
                    break;
                case 3 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:508:9: MAX
                    {
                    match(input,MAX,FOLLOW_MAX_in_attr_op1244); 

                    ao = AttributeOperator.MAX;

                    }
                    break;
                case 4 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:509:9: SUM
                    {
                    match(input,SUM,FOLLOW_SUM_in_attr_op1256); 

                    ao = AttributeOperator.SUM;

                    }
                    break;
                case 5 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:510:9: SIZE
                    {
                    match(input,SIZE,FOLLOW_SIZE_in_attr_op1268); 

                    ao = AttributeOperator.SIZE;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ao;
    }
    // $ANTLR end "attr_op"



    // $ANTLR start "par_type"
    // parser/flatzinc/FlatzincFullExtWalker.g:520:1: par_type returns [Declaration decl] : ( ^( ARRPAR (d= index_set )+ p= par_type_u ) | ^( APAR p= par_type_u ) );
    public final Declaration par_type() throws RecognitionException {
        Declaration decl = null;


        Declaration d =null;

        Declaration p =null;


        try {
            // parser/flatzinc/FlatzincFullExtWalker.g:521:5: ( ^( ARRPAR (d= index_set )+ p= par_type_u ) | ^( APAR p= par_type_u ) )
            int alt33=2;
            switch ( input.LA(1) ) {
            case ARRPAR:
                {
                alt33=1;
                }
                break;
            case APAR:
                {
                alt33=2;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 33, 0, input);

                throw nvae;

            }

            switch (alt33) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:522:5: ^( ARRPAR (d= index_set )+ p= par_type_u )
                    {

                            List<Declaration> decls = new ArrayList();
                        

                    match(input,ARRPAR,FOLLOW_ARRPAR_in_par_type1312); 

                    match(input, Token.DOWN, null); 
                    // parser/flatzinc/FlatzincFullExtWalker.g:525:18: (d= index_set )+
                    int cnt32=0;
                    loop32:
                    do {
                        int alt32=2;
                        switch ( input.LA(1) ) {
                        case INDEX:
                            {
                            alt32=1;
                            }
                            break;

                        }

                        switch (alt32) {
                    	case 1 :
                    	    // parser/flatzinc/FlatzincFullExtWalker.g:525:19: d= index_set
                    	    {
                    	    pushFollow(FOLLOW_index_set_in_par_type1317);
                    	    d=index_set();

                    	    state._fsp--;


                    	    decls.add(d);

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt32 >= 1 ) break loop32;
                                EarlyExitException eee =
                                    new EarlyExitException(32, input);
                                throw eee;
                        }
                        cnt32++;
                    } while (true);


                    pushFollow(FOLLOW_par_type_u_in_par_type1324);
                    p=par_type_u();

                    state._fsp--;


                    match(input, Token.UP, null); 



                        decl = new DArray(decls,p);
                        

                    }
                    break;
                case 2 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:529:9: ^( APAR p= par_type_u )
                    {
                    match(input,APAR,FOLLOW_APAR_in_par_type1342); 

                    match(input, Token.DOWN, null); 
                    pushFollow(FOLLOW_par_type_u_in_par_type1346);
                    p=par_type_u();

                    state._fsp--;


                    match(input, Token.UP, null); 



                        decl = p;
                        

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return decl;
    }
    // $ANTLR end "par_type"



    // $ANTLR start "par_type_u"
    // parser/flatzinc/FlatzincFullExtWalker.g:535:1: par_type_u returns [Declaration decl] : ( BOOL | FLOAT | SET OF INT | INT );
    public final Declaration par_type_u() throws RecognitionException {
        Declaration decl = null;


        try {
            // parser/flatzinc/FlatzincFullExtWalker.g:536:5: ( BOOL | FLOAT | SET OF INT | INT )
            int alt34=4;
            switch ( input.LA(1) ) {
            case BOOL:
                {
                alt34=1;
                }
                break;
            case FLOAT:
                {
                alt34=2;
                }
                break;
            case SET:
                {
                alt34=3;
                }
                break;
            case INT:
                {
                alt34=4;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 34, 0, input);

                throw nvae;

            }

            switch (alt34) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:536:9: BOOL
                    {
                    match(input,BOOL,FOLLOW_BOOL_in_par_type_u1376); 


                        decl =DBool.me;
                        

                    }
                    break;
                case 2 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:540:9: FLOAT
                    {
                    match(input,FLOAT,FOLLOW_FLOAT_in_par_type_u1392); 


                        decl =DFloat.me;
                        

                    }
                    break;
                case 3 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:544:9: SET OF INT
                    {
                    match(input,SET,FOLLOW_SET_in_par_type_u1408); 

                    match(input,OF,FOLLOW_OF_in_par_type_u1410); 

                    match(input,INT,FOLLOW_INT_in_par_type_u1412); 


                        decl =DSetOfInt.me;
                        

                    }
                    break;
                case 4 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:548:9: INT
                    {
                    match(input,INT,FOLLOW_INT_in_par_type_u1428); 


                        decl =DInt.me;
                        

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return decl;
    }
    // $ANTLR end "par_type_u"



    // $ANTLR start "var_type"
    // parser/flatzinc/FlatzincFullExtWalker.g:554:1: var_type returns [Declaration decl] : ( ^( ARRVAR (d= index_set )+ d= var_type_u ) | ^( AVAR d= var_type_u ) );
    public final Declaration var_type() throws RecognitionException {
        Declaration decl = null;


        Declaration d =null;


        try {
            // parser/flatzinc/FlatzincFullExtWalker.g:555:5: ( ^( ARRVAR (d= index_set )+ d= var_type_u ) | ^( AVAR d= var_type_u ) )
            int alt36=2;
            switch ( input.LA(1) ) {
            case ARRVAR:
                {
                alt36=1;
                }
                break;
            case AVAR:
                {
                alt36=2;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 36, 0, input);

                throw nvae;

            }

            switch (alt36) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:556:5: ^( ARRVAR (d= index_set )+ d= var_type_u )
                    {

                        List<Declaration> decls = new ArrayList();
                        

                    match(input,ARRVAR,FOLLOW_ARRVAR_in_var_type1469); 

                    match(input, Token.DOWN, null); 
                    // parser/flatzinc/FlatzincFullExtWalker.g:559:14: (d= index_set )+
                    int cnt35=0;
                    loop35:
                    do {
                        int alt35=2;
                        switch ( input.LA(1) ) {
                        case INDEX:
                            {
                            alt35=1;
                            }
                            break;

                        }

                        switch (alt35) {
                    	case 1 :
                    	    // parser/flatzinc/FlatzincFullExtWalker.g:559:15: d= index_set
                    	    {
                    	    pushFollow(FOLLOW_index_set_in_var_type1474);
                    	    d=index_set();

                    	    state._fsp--;


                    	    decls.add(d);

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt35 >= 1 ) break loop35;
                                EarlyExitException eee =
                                    new EarlyExitException(35, input);
                                throw eee;
                        }
                        cnt35++;
                    } while (true);


                    pushFollow(FOLLOW_var_type_u_in_var_type1481);
                    d=var_type_u();

                    state._fsp--;


                    match(input, Token.UP, null); 



                        decl = new DArray(decls, d);
                        

                    }
                    break;
                case 2 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:563:9: ^( AVAR d= var_type_u )
                    {
                    match(input,AVAR,FOLLOW_AVAR_in_var_type1499); 

                    match(input, Token.DOWN, null); 
                    pushFollow(FOLLOW_var_type_u_in_var_type1503);
                    d=var_type_u();

                    state._fsp--;


                    match(input, Token.UP, null); 



                        decl =d;
                        

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return decl;
    }
    // $ANTLR end "var_type"



    // $ANTLR start "var_type_u"
    // parser/flatzinc/FlatzincFullExtWalker.g:569:1: var_type_u returns [Declaration decl] : ( BOOL | FLOAT | INT | ^( DD i1= INT_CONST i2= INT_CONST ) | ^( CM (i= INT_CONST )+ ) | ^( SET ^( DD i1= INT_CONST i2= INT_CONST ) ) | ^( SET ^( CM (i= INT_CONST )+ ) ) );
    public final Declaration var_type_u() throws RecognitionException {
        Declaration decl = null;


        CommonTree i1=null;
        CommonTree i2=null;
        CommonTree i=null;

        try {
            // parser/flatzinc/FlatzincFullExtWalker.g:570:5: ( BOOL | FLOAT | INT | ^( DD i1= INT_CONST i2= INT_CONST ) | ^( CM (i= INT_CONST )+ ) | ^( SET ^( DD i1= INT_CONST i2= INT_CONST ) ) | ^( SET ^( CM (i= INT_CONST )+ ) ) )
            int alt39=7;
            switch ( input.LA(1) ) {
            case BOOL:
                {
                alt39=1;
                }
                break;
            case FLOAT:
                {
                alt39=2;
                }
                break;
            case INT:
                {
                alt39=3;
                }
                break;
            case DD:
                {
                alt39=4;
                }
                break;
            case CM:
                {
                alt39=5;
                }
                break;
            case SET:
                {
                switch ( input.LA(2) ) {
                case DOWN:
                    {
                    switch ( input.LA(3) ) {
                    case DD:
                        {
                        alt39=6;
                        }
                        break;
                    case CM:
                        {
                        alt39=7;
                        }
                        break;
                    default:
                        NoViableAltException nvae =
                            new NoViableAltException("", 39, 7, input);

                        throw nvae;

                    }

                    }
                    break;
                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 39, 6, input);

                    throw nvae;

                }

                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 39, 0, input);

                throw nvae;

            }

            switch (alt39) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:570:9: BOOL
                    {
                    match(input,BOOL,FOLLOW_BOOL_in_var_type_u1534); 


                        decl = DBool.me;
                        

                    }
                    break;
                case 2 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:574:9: FLOAT
                    {
                    match(input,FLOAT,FOLLOW_FLOAT_in_var_type_u1550); 


                        decl = DFloat.me;
                        

                    }
                    break;
                case 3 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:578:9: INT
                    {
                    match(input,INT,FOLLOW_INT_in_var_type_u1566); 


                        decl = DInt.me;
                        

                    }
                    break;
                case 4 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:582:9: ^( DD i1= INT_CONST i2= INT_CONST )
                    {
                    match(input,DD,FOLLOW_DD_in_var_type_u1583); 

                    match(input, Token.DOWN, null); 
                    i1=(CommonTree)match(input,INT_CONST,FOLLOW_INT_CONST_in_var_type_u1587); 

                    i2=(CommonTree)match(input,INT_CONST,FOLLOW_INT_CONST_in_var_type_u1591); 

                    match(input, Token.UP, null); 



                        decl = new DInt2(EInt.make((i1!=null?i1.getText():null)), EInt.make((i2!=null?i2.getText():null)));
                        

                    }
                    break;
                case 5 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:592:5: ^( CM (i= INT_CONST )+ )
                    {

                        ArrayList<EInt> values = new ArrayList();
                        

                    match(input,CM,FOLLOW_CM_in_var_type_u1626); 

                    match(input, Token.DOWN, null); 
                    // parser/flatzinc/FlatzincFullExtWalker.g:595:14: (i= INT_CONST )+
                    int cnt37=0;
                    loop37:
                    do {
                        int alt37=2;
                        switch ( input.LA(1) ) {
                        case INT_CONST:
                            {
                            alt37=1;
                            }
                            break;

                        }

                        switch (alt37) {
                    	case 1 :
                    	    // parser/flatzinc/FlatzincFullExtWalker.g:595:15: i= INT_CONST
                    	    {
                    	    i=(CommonTree)match(input,INT_CONST,FOLLOW_INT_CONST_in_var_type_u1631); 

                    	    values.add(EInt.make((i!=null?i.getText():null)));

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt37 >= 1 ) break loop37;
                                EarlyExitException eee =
                                    new EarlyExitException(37, input);
                                throw eee;
                        }
                        cnt37++;
                    } while (true);


                    match(input, Token.UP, null); 



                        decl = new DManyInt(values);
                        

                    }
                    break;
                case 6 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:599:9: ^( SET ^( DD i1= INT_CONST i2= INT_CONST ) )
                    {
                    match(input,SET,FOLLOW_SET_in_var_type_u1652); 

                    match(input, Token.DOWN, null); 
                    match(input,DD,FOLLOW_DD_in_var_type_u1655); 

                    match(input, Token.DOWN, null); 
                    i1=(CommonTree)match(input,INT_CONST,FOLLOW_INT_CONST_in_var_type_u1659); 

                    i2=(CommonTree)match(input,INT_CONST,FOLLOW_INT_CONST_in_var_type_u1663); 

                    match(input, Token.UP, null); 


                    match(input, Token.UP, null); 



                        decl = new DSet(new DInt2(EInt.make((i1!=null?i1.getText():null)), EInt.make((i2!=null?i2.getText():null))));
                        

                    }
                    break;
                case 7 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:604:5: ^( SET ^( CM (i= INT_CONST )+ ) )
                    {

                        ArrayList<EInt> values = new ArrayList();
                        

                    match(input,SET,FOLLOW_SET_in_var_type_u1693); 

                    match(input, Token.DOWN, null); 
                    match(input,CM,FOLLOW_CM_in_var_type_u1696); 

                    match(input, Token.DOWN, null); 
                    // parser/flatzinc/FlatzincFullExtWalker.g:607:19: (i= INT_CONST )+
                    int cnt38=0;
                    loop38:
                    do {
                        int alt38=2;
                        switch ( input.LA(1) ) {
                        case INT_CONST:
                            {
                            alt38=1;
                            }
                            break;

                        }

                        switch (alt38) {
                    	case 1 :
                    	    // parser/flatzinc/FlatzincFullExtWalker.g:607:20: i= INT_CONST
                    	    {
                    	    i=(CommonTree)match(input,INT_CONST,FOLLOW_INT_CONST_in_var_type_u1701); 

                    	    values.add(EInt.make((i!=null?i.getText():null)));

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt38 >= 1 ) break loop38;
                                EarlyExitException eee =
                                    new EarlyExitException(38, input);
                                throw eee;
                        }
                        cnt38++;
                    } while (true);


                    match(input, Token.UP, null); 


                    match(input, Token.UP, null); 



                        decl = new DSet(new DManyInt(values));
                        

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return decl;
    }
    // $ANTLR end "var_type_u"



    // $ANTLR start "index_set"
    // parser/flatzinc/FlatzincFullExtWalker.g:613:1: index_set returns [Declaration decl] : ( ^( INDEX ^( DD i1= INT_CONST i2= INT_CONST ) ) | ^( INDEX INT ) );
    public final Declaration index_set() throws RecognitionException {
        Declaration decl = null;


        CommonTree i1=null;
        CommonTree i2=null;

        try {
            // parser/flatzinc/FlatzincFullExtWalker.g:614:5: ( ^( INDEX ^( DD i1= INT_CONST i2= INT_CONST ) ) | ^( INDEX INT ) )
            int alt40=2;
            switch ( input.LA(1) ) {
            case INDEX:
                {
                switch ( input.LA(2) ) {
                case DOWN:
                    {
                    switch ( input.LA(3) ) {
                    case DD:
                        {
                        alt40=1;
                        }
                        break;
                    case INT:
                        {
                        alt40=2;
                        }
                        break;
                    default:
                        NoViableAltException nvae =
                            new NoViableAltException("", 40, 2, input);

                        throw nvae;

                    }

                    }
                    break;
                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 40, 1, input);

                    throw nvae;

                }

                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 40, 0, input);

                throw nvae;

            }

            switch (alt40) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:614:9: ^( INDEX ^( DD i1= INT_CONST i2= INT_CONST ) )
                    {
                    match(input,INDEX,FOLLOW_INDEX_in_index_set1736); 

                    match(input, Token.DOWN, null); 
                    match(input,DD,FOLLOW_DD_in_index_set1739); 

                    match(input, Token.DOWN, null); 
                    i1=(CommonTree)match(input,INT_CONST,FOLLOW_INT_CONST_in_index_set1743); 

                    i2=(CommonTree)match(input,INT_CONST,FOLLOW_INT_CONST_in_index_set1747); 

                    match(input, Token.UP, null); 


                    match(input, Token.UP, null); 



                        decl = new DInt2(EInt.make((i1!=null?i1.getText():null)), EInt.make((i2!=null?i2.getText():null)));
                        

                    }
                    break;
                case 2 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:618:9: ^( INDEX INT )
                    {
                    match(input,INDEX,FOLLOW_INDEX_in_index_set1766); 

                    match(input, Token.DOWN, null); 
                    match(input,INT,FOLLOW_INT_in_index_set1768); 

                    match(input, Token.UP, null); 



                        decl = DInt.me;
                        

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return decl;
    }
    // $ANTLR end "index_set"



    // $ANTLR start "expr"
    // parser/flatzinc/FlatzincFullExtWalker.g:624:1: expr returns [Expression exp] : ( LB (i= INT_CONST )+ RB |b= bool_const |i1= INT_CONST ( DD i2= INT_CONST )? | ^( EXPR LS (e= expr )* RS ) |e= id_expr | STRING );
    public final Expression expr() throws RecognitionException {
        Expression exp = null;


        CommonTree i=null;
        CommonTree i1=null;
        CommonTree i2=null;
        CommonTree STRING3=null;
        boolean b =false;

        Expression e =null;


        try {
            // parser/flatzinc/FlatzincFullExtWalker.g:625:5: ( LB (i= INT_CONST )+ RB |b= bool_const |i1= INT_CONST ( DD i2= INT_CONST )? | ^( EXPR LS (e= expr )* RS ) |e= id_expr | STRING )
            int alt44=6;
            switch ( input.LA(1) ) {
            case LB:
                {
                alt44=1;
                }
                break;
            case FALSE:
            case TRUE:
                {
                alt44=2;
                }
                break;
            case INT_CONST:
                {
                alt44=3;
                }
                break;
            case EXPR:
                {
                alt44=4;
                }
                break;
            case IDENTIFIER:
                {
                alt44=5;
                }
                break;
            case STRING:
                {
                alt44=6;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 44, 0, input);

                throw nvae;

            }

            switch (alt44) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:626:5: LB (i= INT_CONST )+ RB
                    {

                        ArrayList<EInt> values = new ArrayList();
                        

                    match(input,LB,FOLLOW_LB_in_expr1812); 

                    // parser/flatzinc/FlatzincFullExtWalker.g:629:12: (i= INT_CONST )+
                    int cnt41=0;
                    loop41:
                    do {
                        int alt41=2;
                        switch ( input.LA(1) ) {
                        case INT_CONST:
                            {
                            alt41=1;
                            }
                            break;

                        }

                        switch (alt41) {
                    	case 1 :
                    	    // parser/flatzinc/FlatzincFullExtWalker.g:629:13: i= INT_CONST
                    	    {
                    	    i=(CommonTree)match(input,INT_CONST,FOLLOW_INT_CONST_in_expr1817); 

                    	    values.add(EInt.make((i!=null?i.getText():null)));

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt41 >= 1 ) break loop41;
                                EarlyExitException eee =
                                    new EarlyExitException(41, input);
                                throw eee;
                        }
                        cnt41++;
                    } while (true);


                    match(input,RB,FOLLOW_RB_in_expr1822); 


                        exp = new ESetList(values);
                        

                    }
                    break;
                case 2 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:633:9: b= bool_const
                    {
                    pushFollow(FOLLOW_bool_const_in_expr1840);
                    b=bool_const();

                    state._fsp--;



                        exp =EBool.make(b);
                        

                    }
                    break;
                case 3 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:637:9: i1= INT_CONST ( DD i2= INT_CONST )?
                    {
                    i1=(CommonTree)match(input,INT_CONST,FOLLOW_INT_CONST_in_expr1858); 

                    // parser/flatzinc/FlatzincFullExtWalker.g:637:22: ( DD i2= INT_CONST )?
                    int alt42=2;
                    switch ( input.LA(1) ) {
                        case DD:
                            {
                            alt42=1;
                            }
                            break;
                    }

                    switch (alt42) {
                        case 1 :
                            // parser/flatzinc/FlatzincFullExtWalker.g:637:23: DD i2= INT_CONST
                            {
                            match(input,DD,FOLLOW_DD_in_expr1861); 

                            i2=(CommonTree)match(input,INT_CONST,FOLLOW_INT_CONST_in_expr1865); 

                            }
                            break;

                    }



                        if(i2==null){
                            exp =EInt.make((i1!=null?i1.getText():null));
                        }else{
                            exp = new ESetBounds(EInt.make((i1!=null?i1.getText():null)), EInt.make((i2!=null?i2.getText():null)));
                        }
                        

                    }
                    break;
                case 4 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:646:5: ^( EXPR LS (e= expr )* RS )
                    {

                        ArrayList<Expression> exps = new ArrayList();
                        

                    match(input,EXPR,FOLLOW_EXPR_in_expr1895); 

                    match(input, Token.DOWN, null); 
                    match(input,LS,FOLLOW_LS_in_expr1897); 

                    // parser/flatzinc/FlatzincFullExtWalker.g:649:18: (e= expr )*
                    loop43:
                    do {
                        int alt43=2;
                        switch ( input.LA(1) ) {
                        case EXPR:
                        case FALSE:
                        case IDENTIFIER:
                        case INT_CONST:
                        case LB:
                        case STRING:
                        case TRUE:
                            {
                            alt43=1;
                            }
                            break;

                        }

                        switch (alt43) {
                    	case 1 :
                    	    // parser/flatzinc/FlatzincFullExtWalker.g:649:19: e= expr
                    	    {
                    	    pushFollow(FOLLOW_expr_in_expr1902);
                    	    e=expr();

                    	    state._fsp--;


                    	    exps.add(e);

                    	    }
                    	    break;

                    	default :
                    	    break loop43;
                        }
                    } while (true);


                    match(input,RS,FOLLOW_RS_in_expr1907); 

                    match(input, Token.UP, null); 



                        if(exps.size()>0){
                            exp = new EArray(exps);
                        }else{
                            exp = new EArray();
                        }
                        

                    }
                    break;
                case 5 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:657:9: e= id_expr
                    {
                    pushFollow(FOLLOW_id_expr_in_expr1926);
                    e=id_expr();

                    state._fsp--;



                        exp = e;
                        

                    }
                    break;
                case 6 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:661:9: STRING
                    {
                    STRING3=(CommonTree)match(input,STRING,FOLLOW_STRING_in_expr1942); 


                        exp = new EString((STRING3!=null?STRING3.getText():null));
                        

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return exp;
    }
    // $ANTLR end "expr"



    // $ANTLR start "id_expr"
    // parser/flatzinc/FlatzincFullExtWalker.g:685:1: id_expr returns [Expression exp] : IDENTIFIER ( ( LP e= expr ( CM e= expr )* RP ) | ( LS i= INT_CONST RS ) )? ;
    public final Expression id_expr() throws RecognitionException {
        Expression exp = null;


        CommonTree i=null;
        CommonTree IDENTIFIER4=null;
        Expression e =null;


        try {
            // parser/flatzinc/FlatzincFullExtWalker.g:686:5: ( IDENTIFIER ( ( LP e= expr ( CM e= expr )* RP ) | ( LS i= INT_CONST RS ) )? )
            // parser/flatzinc/FlatzincFullExtWalker.g:687:5: IDENTIFIER ( ( LP e= expr ( CM e= expr )* RP ) | ( LS i= INT_CONST RS ) )?
            {

                ArrayList<Expression> exps = new ArrayList();
                

            IDENTIFIER4=(CommonTree)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_id_expr2000); 

            // parser/flatzinc/FlatzincFullExtWalker.g:690:19: ( ( LP e= expr ( CM e= expr )* RP ) | ( LS i= INT_CONST RS ) )?
            int alt46=3;
            switch ( input.LA(1) ) {
                case LP:
                    {
                    alt46=1;
                    }
                    break;
                case LS:
                    {
                    alt46=2;
                    }
                    break;
            }

            switch (alt46) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:690:20: ( LP e= expr ( CM e= expr )* RP )
                    {
                    // parser/flatzinc/FlatzincFullExtWalker.g:690:20: ( LP e= expr ( CM e= expr )* RP )
                    // parser/flatzinc/FlatzincFullExtWalker.g:690:21: LP e= expr ( CM e= expr )* RP
                    {
                    match(input,LP,FOLLOW_LP_in_id_expr2004); 

                    pushFollow(FOLLOW_expr_in_id_expr2008);
                    e=expr();

                    state._fsp--;


                    exps.add(e);

                    // parser/flatzinc/FlatzincFullExtWalker.g:690:45: ( CM e= expr )*
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
                    	    // parser/flatzinc/FlatzincFullExtWalker.g:690:46: CM e= expr
                    	    {
                    	    match(input,CM,FOLLOW_CM_in_id_expr2012); 

                    	    pushFollow(FOLLOW_expr_in_id_expr2016);
                    	    e=expr();

                    	    state._fsp--;


                    	    exps.add(e);

                    	    }
                    	    break;

                    	default :
                    	    break loop45;
                        }
                    } while (true);


                    match(input,RP,FOLLOW_RP_in_id_expr2021); 

                    }


                    }
                    break;
                case 2 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:690:76: ( LS i= INT_CONST RS )
                    {
                    // parser/flatzinc/FlatzincFullExtWalker.g:690:76: ( LS i= INT_CONST RS )
                    // parser/flatzinc/FlatzincFullExtWalker.g:690:77: LS i= INT_CONST RS
                    {
                    match(input,LS,FOLLOW_LS_in_id_expr2025); 

                    i=(CommonTree)match(input,INT_CONST,FOLLOW_INT_CONST_in_id_expr2029); 

                    match(input,RS,FOLLOW_RS_in_id_expr2031); 

                    }


                    }
                    break;

            }



                if(exps.size()>0){
                    exp = new EAnnotation(new EIdentifier(map, (IDENTIFIER4!=null?IDENTIFIER4.getText():null)), exps);
                }else if(i!=null) {
                    exp = new EIdArray(map, (IDENTIFIER4!=null?IDENTIFIER4.getText():null), Integer.parseInt((i!=null?i.getText():null)));
                }else{
                    exp = new EIdentifier(map, (IDENTIFIER4!=null?IDENTIFIER4.getText():null));
                }
                

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return exp;
    }
    // $ANTLR end "id_expr"



    // $ANTLR start "param_decl"
    // parser/flatzinc/FlatzincFullExtWalker.g:703:1: param_decl : ^( PAR IDENTIFIER pt= par_type e= expr ) ;
    public final void param_decl() throws RecognitionException {
        CommonTree IDENTIFIER5=null;
        Declaration pt =null;

        Expression e =null;


        try {
            // parser/flatzinc/FlatzincFullExtWalker.g:704:2: ( ^( PAR IDENTIFIER pt= par_type e= expr ) )
            // parser/flatzinc/FlatzincFullExtWalker.g:704:6: ^( PAR IDENTIFIER pt= par_type e= expr )
            {
            match(input,PAR,FOLLOW_PAR_in_param_decl2058); 

            match(input, Token.DOWN, null); 
            IDENTIFIER5=(CommonTree)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_param_decl2060); 

            pushFollow(FOLLOW_par_type_in_param_decl2064);
            pt=par_type();

            state._fsp--;


            pushFollow(FOLLOW_expr_in_param_decl2068);
            e=expr();

            state._fsp--;


            match(input, Token.UP, null); 



            	// Parameter(THashMap<String, Object> map, Declaration type, String identifier, Expression expression)
                FParameter.make_parameter(map, pt, (IDENTIFIER5!=null?IDENTIFIER5.getText():null), e);
                

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "param_decl"



    // $ANTLR start "var_decl"
    // parser/flatzinc/FlatzincFullExtWalker.g:712:1: var_decl : ^( VAR IDENTIFIER vt= var_type anns= annotations (e= expr )? ) ;
    public final void var_decl() throws RecognitionException {
        CommonTree IDENTIFIER6=null;
        Declaration vt =null;

        List<EAnnotation> anns =null;

        Expression e =null;


        try {
            // parser/flatzinc/FlatzincFullExtWalker.g:713:2: ( ^( VAR IDENTIFIER vt= var_type anns= annotations (e= expr )? ) )
            // parser/flatzinc/FlatzincFullExtWalker.g:713:6: ^( VAR IDENTIFIER vt= var_type anns= annotations (e= expr )? )
            {
            match(input,VAR,FOLLOW_VAR_in_var_decl2087); 

            match(input, Token.DOWN, null); 
            IDENTIFIER6=(CommonTree)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_var_decl2089); 

            pushFollow(FOLLOW_var_type_in_var_decl2093);
            vt=var_type();

            state._fsp--;


            pushFollow(FOLLOW_annotations_in_var_decl2097);
            anns=annotations();

            state._fsp--;


            // parser/flatzinc/FlatzincFullExtWalker.g:713:53: (e= expr )?
            int alt47=2;
            switch ( input.LA(1) ) {
                case EXPR:
                case FALSE:
                case IDENTIFIER:
                case INT_CONST:
                case LB:
                case STRING:
                case TRUE:
                    {
                    alt47=1;
                    }
                    break;
            }

            switch (alt47) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:713:53: e= expr
                    {
                    pushFollow(FOLLOW_expr_in_var_decl2101);
                    e=expr();

                    state._fsp--;


                    }
                    break;

            }


            match(input, Token.UP, null); 



            	FVariable.make_variable(map, vt, (IDENTIFIER6!=null?IDENTIFIER6.getText():null), anns, e, mSolver, mLayout);
            	

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "var_decl"



    // $ANTLR start "constraint"
    // parser/flatzinc/FlatzincFullExtWalker.g:719:1: constraint : ^( CONSTRAINT IDENTIFIER (e= expr )+ anns= annotations ) ;
    public final void constraint() throws RecognitionException {
        CommonTree IDENTIFIER7=null;
        Expression e =null;

        List<EAnnotation> anns =null;


        try {
            // parser/flatzinc/FlatzincFullExtWalker.g:720:2: ( ^( CONSTRAINT IDENTIFIER (e= expr )+ anns= annotations ) )
            // parser/flatzinc/FlatzincFullExtWalker.g:721:2: ^( CONSTRAINT IDENTIFIER (e= expr )+ anns= annotations )
            {

            	//  Solver aSolver, String id, List<Expression> exps, List<EAnnotation> annotations
            	ArrayList<Expression> exps = new ArrayList();
            	

            match(input,CONSTRAINT,FOLLOW_CONSTRAINT_in_constraint2126); 

            match(input, Token.DOWN, null); 
            IDENTIFIER7=(CommonTree)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_constraint2128); 

            // parser/flatzinc/FlatzincFullExtWalker.g:725:30: (e= expr )+
            int cnt48=0;
            loop48:
            do {
                int alt48=2;
                switch ( input.LA(1) ) {
                case EXPR:
                case FALSE:
                case IDENTIFIER:
                case INT_CONST:
                case LB:
                case STRING:
                case TRUE:
                    {
                    alt48=1;
                    }
                    break;

                }

                switch (alt48) {
            	case 1 :
            	    // parser/flatzinc/FlatzincFullExtWalker.g:725:31: e= expr
            	    {
            	    pushFollow(FOLLOW_expr_in_constraint2133);
            	    e=expr();

            	    state._fsp--;


            	    exps.add(e);

            	    }
            	    break;

            	default :
            	    if ( cnt48 >= 1 ) break loop48;
                        EarlyExitException eee =
                            new EarlyExitException(48, input);
                        throw eee;
                }
                cnt48++;
            } while (true);


            pushFollow(FOLLOW_annotations_in_constraint2140);
            anns=annotations();

            state._fsp--;


            match(input, Token.UP, null); 



            	String id = (IDENTIFIER7!=null?IDENTIFIER7.getText():null);
            	FConstraint.make_constraint(mSolver, map, id, exps, anns);
            	

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "constraint"



    // $ANTLR start "solve_goal"
    // parser/flatzinc/FlatzincFullExtWalker.g:732:1: solve_goal : ^( SOLVE anns= annotations res= resolution[type,expr] ) ;
    public final void solve_goal() throws RecognitionException {
        List<EAnnotation> anns =null;


        try {
            // parser/flatzinc/FlatzincFullExtWalker.g:733:2: ( ^( SOLVE anns= annotations res= resolution[type,expr] ) )
            // parser/flatzinc/FlatzincFullExtWalker.g:734:2: ^( SOLVE anns= annotations res= resolution[type,expr] )
            {

            	ResolutionPolicy type = ResolutionPolicy.SATISFACTION;
            	Expression expr = null;
            	

            match(input,SOLVE,FOLLOW_SOLVE_in_solve_goal2160); 

            match(input, Token.DOWN, null); 
            pushFollow(FOLLOW_annotations_in_solve_goal2164);
            anns=annotations();

            state._fsp--;


            pushFollow(FOLLOW_resolution_in_solve_goal2168);
            resolution(type, expr);

            state._fsp--;


            match(input, Token.UP, null); 



                FGoal.define_goal(gc, mSolver,anns,type,expr);
            	

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "solve_goal"



    // $ANTLR start "resolution"
    // parser/flatzinc/FlatzincFullExtWalker.g:744:1: resolution[ResolutionPolicy type, Expression expr] : ( SATISFY | ^( MINIMIZE e= expr ) | ^( MAXIMIZE e= expr ) );
    public final void resolution(ResolutionPolicy type, Expression expr) throws RecognitionException {
        Expression e =null;


        try {
            // parser/flatzinc/FlatzincFullExtWalker.g:745:5: ( SATISFY | ^( MINIMIZE e= expr ) | ^( MAXIMIZE e= expr ) )
            int alt49=3;
            switch ( input.LA(1) ) {
            case SATISFY:
                {
                alt49=1;
                }
                break;
            case MINIMIZE:
                {
                alt49=2;
                }
                break;
            case MAXIMIZE:
                {
                alt49=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 49, 0, input);

                throw nvae;

            }

            switch (alt49) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:745:9: SATISFY
                    {
                    match(input,SATISFY,FOLLOW_SATISFY_in_resolution2192); 

                    }
                    break;
                case 2 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:746:9: ^( MINIMIZE e= expr )
                    {
                    match(input,MINIMIZE,FOLLOW_MINIMIZE_in_resolution2203); 

                    match(input, Token.DOWN, null); 
                    pushFollow(FOLLOW_expr_in_resolution2207);
                    e=expr();

                    state._fsp--;


                    match(input, Token.UP, null); 



                        type =ResolutionPolicy.MINIMIZE;
                        expr =e;
                        

                    }
                    break;
                case 3 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:751:9: ^( MAXIMIZE e= expr )
                    {
                    match(input,MAXIMIZE,FOLLOW_MAXIMIZE_in_resolution2225); 

                    match(input, Token.DOWN, null); 
                    pushFollow(FOLLOW_expr_in_resolution2229);
                    e=expr();

                    state._fsp--;


                    match(input, Token.UP, null); 



                        type =ResolutionPolicy.MAXIMIZE;
                        expr =e;
                        

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "resolution"



    // $ANTLR start "annotations"
    // parser/flatzinc/FlatzincFullExtWalker.g:758:1: annotations returns [List<EAnnotation> anns] : ^( ANNOTATIONS (e= annotation )* ) ;
    public final List<EAnnotation> annotations() throws RecognitionException {
        List<EAnnotation> anns = null;


        EAnnotation e =null;


        try {
            // parser/flatzinc/FlatzincFullExtWalker.g:759:5: ( ^( ANNOTATIONS (e= annotation )* ) )
            // parser/flatzinc/FlatzincFullExtWalker.g:760:5: ^( ANNOTATIONS (e= annotation )* )
            {

                anns = new ArrayList();
                

            match(input,ANNOTATIONS,FOLLOW_ANNOTATIONS_in_annotations2272); 

            if ( input.LA(1)==Token.DOWN ) {
                match(input, Token.DOWN, null); 
                // parser/flatzinc/FlatzincFullExtWalker.g:763:23: (e= annotation )*
                loop50:
                do {
                    int alt50=2;
                    switch ( input.LA(1) ) {
                    case IDENTIFIER:
                        {
                        alt50=1;
                        }
                        break;

                    }

                    switch (alt50) {
                	case 1 :
                	    // parser/flatzinc/FlatzincFullExtWalker.g:763:24: e= annotation
                	    {
                	    pushFollow(FOLLOW_annotation_in_annotations2277);
                	    e=annotation();

                	    state._fsp--;


                	    anns.add(e);

                	    }
                	    break;

                	default :
                	    break loop50;
                    }
                } while (true);


                match(input, Token.UP, null); 
            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return anns;
    }
    // $ANTLR end "annotations"



    // $ANTLR start "annotation"
    // parser/flatzinc/FlatzincFullExtWalker.g:766:1: annotation returns [EAnnotation ann] : IDENTIFIER ( LP (e= expr )+ RP )? ;
    public final EAnnotation annotation() throws RecognitionException {
        EAnnotation ann = null;


        CommonTree IDENTIFIER8=null;
        Expression e =null;


        try {
            // parser/flatzinc/FlatzincFullExtWalker.g:767:5: ( IDENTIFIER ( LP (e= expr )+ RP )? )
            // parser/flatzinc/FlatzincFullExtWalker.g:768:5: IDENTIFIER ( LP (e= expr )+ RP )?
            {

                ArrayList<Expression> exps = new ArrayList();
                

            IDENTIFIER8=(CommonTree)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_annotation2314); 

            // parser/flatzinc/FlatzincFullExtWalker.g:771:16: ( LP (e= expr )+ RP )?
            int alt52=2;
            switch ( input.LA(1) ) {
                case LP:
                    {
                    alt52=1;
                    }
                    break;
            }

            switch (alt52) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:771:17: LP (e= expr )+ RP
                    {
                    match(input,LP,FOLLOW_LP_in_annotation2317); 

                    // parser/flatzinc/FlatzincFullExtWalker.g:771:20: (e= expr )+
                    int cnt51=0;
                    loop51:
                    do {
                        int alt51=2;
                        switch ( input.LA(1) ) {
                        case EXPR:
                        case FALSE:
                        case IDENTIFIER:
                        case INT_CONST:
                        case LB:
                        case STRING:
                        case TRUE:
                            {
                            alt51=1;
                            }
                            break;

                        }

                        switch (alt51) {
                    	case 1 :
                    	    // parser/flatzinc/FlatzincFullExtWalker.g:771:21: e= expr
                    	    {
                    	    pushFollow(FOLLOW_expr_in_annotation2322);
                    	    e=expr();

                    	    state._fsp--;


                    	    exps.add(e);

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt51 >= 1 ) break loop51;
                                EarlyExitException eee =
                                    new EarlyExitException(51, input);
                                throw eee;
                        }
                        cnt51++;
                    } while (true);


                    match(input,RP,FOLLOW_RP_in_annotation2327); 

                    }
                    break;

            }



                ann = new EAnnotation(new EIdentifier(map,(IDENTIFIER8!=null?IDENTIFIER8.getText():null)), exps);
                

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ann;
    }
    // $ANTLR end "annotation"



    // $ANTLR start "bool_const"
    // parser/flatzinc/FlatzincFullExtWalker.g:777:1: bool_const returns [boolean value] : ( TRUE | FALSE );
    public final boolean bool_const() throws RecognitionException {
        boolean value = false;


        try {
            // parser/flatzinc/FlatzincFullExtWalker.g:778:5: ( TRUE | FALSE )
            int alt53=2;
            switch ( input.LA(1) ) {
            case TRUE:
                {
                alt53=1;
                }
                break;
            case FALSE:
                {
                alt53=2;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 53, 0, input);

                throw nvae;

            }

            switch (alt53) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:778:9: TRUE
                    {
                    match(input,TRUE,FOLLOW_TRUE_in_bool_const2359); 

                    value = true;

                    }
                    break;
                case 2 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:779:9: FALSE
                    {
                    match(input,FALSE,FOLLOW_FALSE_in_bool_const2371); 

                    value = false;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return value;
    }
    // $ANTLR end "bool_const"



    // $ANTLR start "pred_decl"
    // parser/flatzinc/FlatzincFullExtWalker.g:783:1: pred_decl : ^( PREDICATE IDENTIFIER ( pred_param )+ ) ;
    public final void pred_decl() throws RecognitionException {
        CommonTree IDENTIFIER9=null;

        try {
            // parser/flatzinc/FlatzincFullExtWalker.g:784:2: ( ^( PREDICATE IDENTIFIER ( pred_param )+ ) )
            // parser/flatzinc/FlatzincFullExtWalker.g:784:6: ^( PREDICATE IDENTIFIER ( pred_param )+ )
            {
            match(input,PREDICATE,FOLLOW_PREDICATE_in_pred_decl2390); 

            match(input, Token.DOWN, null); 
            IDENTIFIER9=(CommonTree)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_pred_decl2392); 

            // parser/flatzinc/FlatzincFullExtWalker.g:784:29: ( pred_param )+
            int cnt54=0;
            loop54:
            do {
                int alt54=2;
                switch ( input.LA(1) ) {
                case CL:
                    {
                    alt54=1;
                    }
                    break;

                }

                switch (alt54) {
            	case 1 :
            	    // parser/flatzinc/FlatzincFullExtWalker.g:784:29: pred_param
            	    {
            	    pushFollow(FOLLOW_pred_param_in_pred_decl2394);
            	    pred_param();

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    if ( cnt54 >= 1 ) break loop54;
                        EarlyExitException eee =
                            new EarlyExitException(54, input);
                        throw eee;
                }
                cnt54++;
            } while (true);


            match(input, Token.UP, null); 



            //        LOGGER.info("% skip predicate : "+ (IDENTIFIER9!=null?IDENTIFIER9.getText():null));
            	

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "pred_decl"



    // $ANTLR start "pred_param"
    // parser/flatzinc/FlatzincFullExtWalker.g:791:1: pred_param : ^( CL pred_param_type IDENTIFIER ) ;
    public final void pred_param() throws RecognitionException {
        try {
            // parser/flatzinc/FlatzincFullExtWalker.g:792:5: ( ^( CL pred_param_type IDENTIFIER ) )
            // parser/flatzinc/FlatzincFullExtWalker.g:792:9: ^( CL pred_param_type IDENTIFIER )
            {
            match(input,CL,FOLLOW_CL_in_pred_param2417); 

            match(input, Token.DOWN, null); 
            pushFollow(FOLLOW_pred_param_type_in_pred_param2419);
            pred_param_type();

            state._fsp--;


            match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_pred_param2421); 

            match(input, Token.UP, null); 


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "pred_param"



    // $ANTLR start "pred_param_type"
    // parser/flatzinc/FlatzincFullExtWalker.g:796:1: pred_param_type : ( par_pred_param_type | var_pred_param_type );
    public final void pred_param_type() throws RecognitionException {
        try {
            // parser/flatzinc/FlatzincFullExtWalker.g:797:5: ( par_pred_param_type | var_pred_param_type )
            int alt55=2;
            alt55 = dfa55.predict(input);
            switch (alt55) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:797:9: par_pred_param_type
                    {
                    pushFollow(FOLLOW_par_pred_param_type_in_pred_param_type2442);
                    par_pred_param_type();

                    state._fsp--;


                    }
                    break;
                case 2 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:798:9: var_pred_param_type
                    {
                    pushFollow(FOLLOW_var_pred_param_type_in_pred_param_type2452);
                    var_pred_param_type();

                    state._fsp--;


                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "pred_param_type"



    // $ANTLR start "par_pred_param_type"
    // parser/flatzinc/FlatzincFullExtWalker.g:802:1: par_pred_param_type : ( par_type | ^( DD INT_CONST INT_CONST ) | ^( CM ( INT_CONST )+ ) | ^( SET ^( DD INT_CONST INT_CONST ) ) | ^( SET ^( CM ( INT_CONST )+ ) ) | ^( ARRAY ( index_set )+ ^( DD INT_CONST INT_CONST ) ) | ^( ARRAY ( index_set )+ ^( CM ( INT_CONST )+ ) ) | ^( ARRAY ( index_set )+ ^( SET ^( DD INT_CONST INT_CONST ) ) ) | ^( ARRAY ( index_set )+ ^( SET ^( CM ( INT_CONST )+ ) ) ) );
    public final void par_pred_param_type() throws RecognitionException {
        try {
            // parser/flatzinc/FlatzincFullExtWalker.g:803:5: ( par_type | ^( DD INT_CONST INT_CONST ) | ^( CM ( INT_CONST )+ ) | ^( SET ^( DD INT_CONST INT_CONST ) ) | ^( SET ^( CM ( INT_CONST )+ ) ) | ^( ARRAY ( index_set )+ ^( DD INT_CONST INT_CONST ) ) | ^( ARRAY ( index_set )+ ^( CM ( INT_CONST )+ ) ) | ^( ARRAY ( index_set )+ ^( SET ^( DD INT_CONST INT_CONST ) ) ) | ^( ARRAY ( index_set )+ ^( SET ^( CM ( INT_CONST )+ ) ) ) )
            int alt64=9;
            alt64 = dfa64.predict(input);
            switch (alt64) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:803:9: par_type
                    {
                    pushFollow(FOLLOW_par_type_in_par_pred_param_type2472);
                    par_type();

                    state._fsp--;


                    }
                    break;
                case 2 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:805:9: ^( DD INT_CONST INT_CONST )
                    {
                    match(input,DD,FOLLOW_DD_in_par_pred_param_type2484); 

                    match(input, Token.DOWN, null); 
                    match(input,INT_CONST,FOLLOW_INT_CONST_in_par_pred_param_type2486); 

                    match(input,INT_CONST,FOLLOW_INT_CONST_in_par_pred_param_type2488); 

                    match(input, Token.UP, null); 


                    }
                    break;
                case 3 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:806:9: ^( CM ( INT_CONST )+ )
                    {
                    match(input,CM,FOLLOW_CM_in_par_pred_param_type2500); 

                    match(input, Token.DOWN, null); 
                    // parser/flatzinc/FlatzincFullExtWalker.g:806:14: ( INT_CONST )+
                    int cnt56=0;
                    loop56:
                    do {
                        int alt56=2;
                        switch ( input.LA(1) ) {
                        case INT_CONST:
                            {
                            alt56=1;
                            }
                            break;

                        }

                        switch (alt56) {
                    	case 1 :
                    	    // parser/flatzinc/FlatzincFullExtWalker.g:806:14: INT_CONST
                    	    {
                    	    match(input,INT_CONST,FOLLOW_INT_CONST_in_par_pred_param_type2502); 

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt56 >= 1 ) break loop56;
                                EarlyExitException eee =
                                    new EarlyExitException(56, input);
                                throw eee;
                        }
                        cnt56++;
                    } while (true);


                    match(input, Token.UP, null); 


                    }
                    break;
                case 4 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:807:9: ^( SET ^( DD INT_CONST INT_CONST ) )
                    {
                    match(input,SET,FOLLOW_SET_in_par_pred_param_type2515); 

                    match(input, Token.DOWN, null); 
                    match(input,DD,FOLLOW_DD_in_par_pred_param_type2518); 

                    match(input, Token.DOWN, null); 
                    match(input,INT_CONST,FOLLOW_INT_CONST_in_par_pred_param_type2520); 

                    match(input,INT_CONST,FOLLOW_INT_CONST_in_par_pred_param_type2522); 

                    match(input, Token.UP, null); 


                    match(input, Token.UP, null); 


                    }
                    break;
                case 5 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:808:9: ^( SET ^( CM ( INT_CONST )+ ) )
                    {
                    match(input,SET,FOLLOW_SET_in_par_pred_param_type2535); 

                    match(input, Token.DOWN, null); 
                    match(input,CM,FOLLOW_CM_in_par_pred_param_type2538); 

                    match(input, Token.DOWN, null); 
                    // parser/flatzinc/FlatzincFullExtWalker.g:808:20: ( INT_CONST )+
                    int cnt57=0;
                    loop57:
                    do {
                        int alt57=2;
                        switch ( input.LA(1) ) {
                        case INT_CONST:
                            {
                            alt57=1;
                            }
                            break;

                        }

                        switch (alt57) {
                    	case 1 :
                    	    // parser/flatzinc/FlatzincFullExtWalker.g:808:20: INT_CONST
                    	    {
                    	    match(input,INT_CONST,FOLLOW_INT_CONST_in_par_pred_param_type2540); 

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt57 >= 1 ) break loop57;
                                EarlyExitException eee =
                                    new EarlyExitException(57, input);
                                throw eee;
                        }
                        cnt57++;
                    } while (true);


                    match(input, Token.UP, null); 


                    match(input, Token.UP, null); 


                    }
                    break;
                case 6 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:810:9: ^( ARRAY ( index_set )+ ^( DD INT_CONST INT_CONST ) )
                    {
                    match(input,ARRAY,FOLLOW_ARRAY_in_par_pred_param_type2555); 

                    match(input, Token.DOWN, null); 
                    // parser/flatzinc/FlatzincFullExtWalker.g:810:17: ( index_set )+
                    int cnt58=0;
                    loop58:
                    do {
                        int alt58=2;
                        switch ( input.LA(1) ) {
                        case INDEX:
                            {
                            alt58=1;
                            }
                            break;

                        }

                        switch (alt58) {
                    	case 1 :
                    	    // parser/flatzinc/FlatzincFullExtWalker.g:810:17: index_set
                    	    {
                    	    pushFollow(FOLLOW_index_set_in_par_pred_param_type2557);
                    	    index_set();

                    	    state._fsp--;


                    	    }
                    	    break;

                    	default :
                    	    if ( cnt58 >= 1 ) break loop58;
                                EarlyExitException eee =
                                    new EarlyExitException(58, input);
                                throw eee;
                        }
                        cnt58++;
                    } while (true);


                    match(input,DD,FOLLOW_DD_in_par_pred_param_type2561); 

                    match(input, Token.DOWN, null); 
                    match(input,INT_CONST,FOLLOW_INT_CONST_in_par_pred_param_type2563); 

                    match(input,INT_CONST,FOLLOW_INT_CONST_in_par_pred_param_type2565); 

                    match(input, Token.UP, null); 


                    match(input, Token.UP, null); 


                    }
                    break;
                case 7 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:811:9: ^( ARRAY ( index_set )+ ^( CM ( INT_CONST )+ ) )
                    {
                    match(input,ARRAY,FOLLOW_ARRAY_in_par_pred_param_type2578); 

                    match(input, Token.DOWN, null); 
                    // parser/flatzinc/FlatzincFullExtWalker.g:811:17: ( index_set )+
                    int cnt59=0;
                    loop59:
                    do {
                        int alt59=2;
                        switch ( input.LA(1) ) {
                        case INDEX:
                            {
                            alt59=1;
                            }
                            break;

                        }

                        switch (alt59) {
                    	case 1 :
                    	    // parser/flatzinc/FlatzincFullExtWalker.g:811:17: index_set
                    	    {
                    	    pushFollow(FOLLOW_index_set_in_par_pred_param_type2580);
                    	    index_set();

                    	    state._fsp--;


                    	    }
                    	    break;

                    	default :
                    	    if ( cnt59 >= 1 ) break loop59;
                                EarlyExitException eee =
                                    new EarlyExitException(59, input);
                                throw eee;
                        }
                        cnt59++;
                    } while (true);


                    match(input,CM,FOLLOW_CM_in_par_pred_param_type2584); 

                    match(input, Token.DOWN, null); 
                    // parser/flatzinc/FlatzincFullExtWalker.g:811:33: ( INT_CONST )+
                    int cnt60=0;
                    loop60:
                    do {
                        int alt60=2;
                        switch ( input.LA(1) ) {
                        case INT_CONST:
                            {
                            alt60=1;
                            }
                            break;

                        }

                        switch (alt60) {
                    	case 1 :
                    	    // parser/flatzinc/FlatzincFullExtWalker.g:811:33: INT_CONST
                    	    {
                    	    match(input,INT_CONST,FOLLOW_INT_CONST_in_par_pred_param_type2586); 

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt60 >= 1 ) break loop60;
                                EarlyExitException eee =
                                    new EarlyExitException(60, input);
                                throw eee;
                        }
                        cnt60++;
                    } while (true);


                    match(input, Token.UP, null); 


                    match(input, Token.UP, null); 


                    }
                    break;
                case 8 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:812:9: ^( ARRAY ( index_set )+ ^( SET ^( DD INT_CONST INT_CONST ) ) )
                    {
                    match(input,ARRAY,FOLLOW_ARRAY_in_par_pred_param_type2600); 

                    match(input, Token.DOWN, null); 
                    // parser/flatzinc/FlatzincFullExtWalker.g:812:17: ( index_set )+
                    int cnt61=0;
                    loop61:
                    do {
                        int alt61=2;
                        switch ( input.LA(1) ) {
                        case INDEX:
                            {
                            alt61=1;
                            }
                            break;

                        }

                        switch (alt61) {
                    	case 1 :
                    	    // parser/flatzinc/FlatzincFullExtWalker.g:812:17: index_set
                    	    {
                    	    pushFollow(FOLLOW_index_set_in_par_pred_param_type2602);
                    	    index_set();

                    	    state._fsp--;


                    	    }
                    	    break;

                    	default :
                    	    if ( cnt61 >= 1 ) break loop61;
                                EarlyExitException eee =
                                    new EarlyExitException(61, input);
                                throw eee;
                        }
                        cnt61++;
                    } while (true);


                    match(input,SET,FOLLOW_SET_in_par_pred_param_type2606); 

                    match(input, Token.DOWN, null); 
                    match(input,DD,FOLLOW_DD_in_par_pred_param_type2609); 

                    match(input, Token.DOWN, null); 
                    match(input,INT_CONST,FOLLOW_INT_CONST_in_par_pred_param_type2611); 

                    match(input,INT_CONST,FOLLOW_INT_CONST_in_par_pred_param_type2613); 

                    match(input, Token.UP, null); 


                    match(input, Token.UP, null); 


                    match(input, Token.UP, null); 


                    }
                    break;
                case 9 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:813:9: ^( ARRAY ( index_set )+ ^( SET ^( CM ( INT_CONST )+ ) ) )
                    {
                    match(input,ARRAY,FOLLOW_ARRAY_in_par_pred_param_type2627); 

                    match(input, Token.DOWN, null); 
                    // parser/flatzinc/FlatzincFullExtWalker.g:813:17: ( index_set )+
                    int cnt62=0;
                    loop62:
                    do {
                        int alt62=2;
                        switch ( input.LA(1) ) {
                        case INDEX:
                            {
                            alt62=1;
                            }
                            break;

                        }

                        switch (alt62) {
                    	case 1 :
                    	    // parser/flatzinc/FlatzincFullExtWalker.g:813:17: index_set
                    	    {
                    	    pushFollow(FOLLOW_index_set_in_par_pred_param_type2629);
                    	    index_set();

                    	    state._fsp--;


                    	    }
                    	    break;

                    	default :
                    	    if ( cnt62 >= 1 ) break loop62;
                                EarlyExitException eee =
                                    new EarlyExitException(62, input);
                                throw eee;
                        }
                        cnt62++;
                    } while (true);


                    match(input,SET,FOLLOW_SET_in_par_pred_param_type2633); 

                    match(input, Token.DOWN, null); 
                    match(input,CM,FOLLOW_CM_in_par_pred_param_type2636); 

                    match(input, Token.DOWN, null); 
                    // parser/flatzinc/FlatzincFullExtWalker.g:813:39: ( INT_CONST )+
                    int cnt63=0;
                    loop63:
                    do {
                        int alt63=2;
                        switch ( input.LA(1) ) {
                        case INT_CONST:
                            {
                            alt63=1;
                            }
                            break;

                        }

                        switch (alt63) {
                    	case 1 :
                    	    // parser/flatzinc/FlatzincFullExtWalker.g:813:39: INT_CONST
                    	    {
                    	    match(input,INT_CONST,FOLLOW_INT_CONST_in_par_pred_param_type2638); 

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt63 >= 1 ) break loop63;
                                EarlyExitException eee =
                                    new EarlyExitException(63, input);
                                throw eee;
                        }
                        cnt63++;
                    } while (true);


                    match(input, Token.UP, null); 


                    match(input, Token.UP, null); 


                    match(input, Token.UP, null); 


                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "par_pred_param_type"



    // $ANTLR start "var_pred_param_type"
    // parser/flatzinc/FlatzincFullExtWalker.g:817:1: var_pred_param_type : ( ^( VAR var_type ) | ^( VAR SET ) | ^( ARRAY ( index_set )+ ^( VAR SET ) ) );
    public final void var_pred_param_type() throws RecognitionException {
        try {
            // parser/flatzinc/FlatzincFullExtWalker.g:818:5: ( ^( VAR var_type ) | ^( VAR SET ) | ^( ARRAY ( index_set )+ ^( VAR SET ) ) )
            int alt66=3;
            switch ( input.LA(1) ) {
            case VAR:
                {
                switch ( input.LA(2) ) {
                case DOWN:
                    {
                    switch ( input.LA(3) ) {
                    case SET:
                        {
                        alt66=2;
                        }
                        break;
                    case ARRVAR:
                    case AVAR:
                        {
                        alt66=1;
                        }
                        break;
                    default:
                        NoViableAltException nvae =
                            new NoViableAltException("", 66, 3, input);

                        throw nvae;

                    }

                    }
                    break;
                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 66, 1, input);

                    throw nvae;

                }

                }
                break;
            case ARRAY:
                {
                alt66=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 66, 0, input);

                throw nvae;

            }

            switch (alt66) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:818:9: ^( VAR var_type )
                    {
                    match(input,VAR,FOLLOW_VAR_in_var_pred_param_type2663); 

                    match(input, Token.DOWN, null); 
                    pushFollow(FOLLOW_var_type_in_var_pred_param_type2665);
                    var_type();

                    state._fsp--;


                    match(input, Token.UP, null); 


                    }
                    break;
                case 2 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:819:9: ^( VAR SET )
                    {
                    match(input,VAR,FOLLOW_VAR_in_var_pred_param_type2677); 

                    match(input, Token.DOWN, null); 
                    match(input,SET,FOLLOW_SET_in_var_pred_param_type2679); 

                    match(input, Token.UP, null); 


                    }
                    break;
                case 3 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:820:9: ^( ARRAY ( index_set )+ ^( VAR SET ) )
                    {
                    match(input,ARRAY,FOLLOW_ARRAY_in_var_pred_param_type2691); 

                    match(input, Token.DOWN, null); 
                    // parser/flatzinc/FlatzincFullExtWalker.g:820:17: ( index_set )+
                    int cnt65=0;
                    loop65:
                    do {
                        int alt65=2;
                        switch ( input.LA(1) ) {
                        case INDEX:
                            {
                            alt65=1;
                            }
                            break;

                        }

                        switch (alt65) {
                    	case 1 :
                    	    // parser/flatzinc/FlatzincFullExtWalker.g:820:17: index_set
                    	    {
                    	    pushFollow(FOLLOW_index_set_in_var_pred_param_type2693);
                    	    index_set();

                    	    state._fsp--;


                    	    }
                    	    break;

                    	default :
                    	    if ( cnt65 >= 1 ) break loop65;
                                EarlyExitException eee =
                                    new EarlyExitException(65, input);
                                throw eee;
                        }
                        cnt65++;
                    } while (true);


                    match(input,VAR,FOLLOW_VAR_in_var_pred_param_type2697); 

                    match(input, Token.DOWN, null); 
                    match(input,SET,FOLLOW_SET_in_var_pred_param_type2699); 

                    match(input, Token.UP, null); 


                    match(input, Token.UP, null); 


                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "var_pred_param_type"

    // Delegated rules


    protected DFA18 dfa18 = new DFA18(this);
    protected DFA55 dfa55 = new DFA55(this);
    protected DFA64 dfa64 = new DFA64(this);
    static final String DFA18_eotS =
        "\56\uffff";
    static final String DFA18_eofS =
        "\56\uffff";
    static final String DFA18_minS =
        "\1\126\1\2\1\46\1\4\2\2\1\uffff\1\3\1\4\17\3\1\uffff\5\3\1\4\12"+
        "\3\1\2\2\4\2\3";
    static final String DFA18_maxS =
        "\1\126\1\2\1\46\3\142\1\uffff\21\142\1\uffff\25\142";
    static final String DFA18_acceptS =
        "\6\uffff\1\1\21\uffff\1\2\25\uffff";
    static final String DFA18_specialS =
        "\56\uffff}>";
    static final String[] DFA18_transitionS = {
            "\1\1",
            "\1\2",
            "\1\3",
            "\12\6\1\4\1\5\123\6",
            "\1\7\1\uffff\137\6",
            "\1\10\1\uffff\137\6",
            "",
            "\1\30\2\6\1\11\11\6\1\24\3\6\1\23\2\6\1\17\34\6\1\13\1\6\1"+
            "\12\17\6\1\26\1\6\1\25\1\27\1\6\1\20\10\6\1\15\5\6\1\14\2\6"+
            "\1\16\1\22\1\21\3\6",
            "\2\6\1\31\55\6\1\33\1\6\1\32\35\6\1\35\5\6\1\34\10\6",
            "\1\36\2\6\1\11\11\6\1\24\3\6\1\23\2\6\1\17\34\6\1\13\1\6\1"+
            "\12\17\6\1\26\1\6\1\25\1\27\1\6\1\20\10\6\1\15\5\6\1\14\2\6"+
            "\1\16\1\22\1\21\3\6",
            "\1\36\2\6\1\11\11\6\1\24\3\6\1\23\2\6\1\17\34\6\1\13\1\6\1"+
            "\12\17\6\1\26\1\6\1\25\1\27\1\6\1\20\10\6\1\15\5\6\1\14\2\6"+
            "\1\16\1\22\1\21\3\6",
            "\1\36\2\6\1\11\11\6\1\24\3\6\1\23\2\6\1\17\34\6\1\13\1\6\1"+
            "\12\17\6\1\26\1\6\1\25\1\27\1\6\1\20\10\6\1\15\5\6\1\14\2\6"+
            "\1\16\1\22\1\21\3\6",
            "\1\36\2\6\1\11\11\6\1\24\3\6\1\23\2\6\1\17\34\6\1\13\1\6\1"+
            "\12\17\6\1\26\1\6\1\25\1\27\1\6\1\20\10\6\1\15\5\6\1\14\2\6"+
            "\1\16\1\22\1\21\3\6",
            "\1\36\2\6\1\11\11\6\1\24\3\6\1\23\2\6\1\17\34\6\1\13\1\6\1"+
            "\12\17\6\1\26\1\6\1\25\1\27\1\6\1\20\10\6\1\15\5\6\1\14\2\6"+
            "\1\16\1\22\1\21\3\6",
            "\1\36\137\6",
            "\1\36\137\6",
            "\1\36\137\6",
            "\1\36\137\6",
            "\1\36\137\6",
            "\1\36\137\6",
            "\1\36\137\6",
            "\1\36\137\6",
            "\1\36\137\6",
            "\1\36\137\6",
            "",
            "\3\6\1\31\11\6\1\45\3\6\1\44\2\6\1\40\34\6\1\33\1\6\1\32\17"+
            "\6\1\47\1\6\1\46\1\50\1\6\1\41\10\6\1\35\5\6\1\34\2\6\1\37\1"+
            "\43\1\42\3\6",
            "\3\6\1\31\11\6\1\45\3\6\1\44\2\6\1\40\34\6\1\33\1\6\1\32\17"+
            "\6\1\47\1\6\1\46\1\50\1\6\1\41\10\6\1\35\5\6\1\34\2\6\1\37\1"+
            "\43\1\42\3\6",
            "\3\6\1\31\11\6\1\45\3\6\1\44\2\6\1\40\34\6\1\33\1\6\1\32\17"+
            "\6\1\47\1\6\1\46\1\50\1\6\1\41\10\6\1\35\5\6\1\34\2\6\1\37\1"+
            "\43\1\42\3\6",
            "\3\6\1\31\11\6\1\45\3\6\1\44\2\6\1\40\34\6\1\33\1\6\1\32\17"+
            "\6\1\47\1\6\1\46\1\50\1\6\1\41\10\6\1\35\5\6\1\34\2\6\1\37\1"+
            "\43\1\42\3\6",
            "\3\6\1\31\11\6\1\45\3\6\1\44\2\6\1\40\34\6\1\33\1\6\1\32\17"+
            "\6\1\47\1\6\1\46\1\50\1\6\1\41\10\6\1\35\5\6\1\34\2\6\1\37\1"+
            "\43\1\42\3\6",
            "\137\51",
            "\1\52\137\6",
            "\1\52\137\6",
            "\1\52\137\6",
            "\1\52\137\6",
            "\1\52\137\6",
            "\1\52\137\6",
            "\1\52\137\6",
            "\1\52\137\6",
            "\1\52\137\6",
            "\1\52\137\6",
            "\1\53\1\6\137\30",
            "\137\51",
            "\137\54",
            "\1\55\137\54",
            "\1\6\137\30"
    };

    static final short[] DFA18_eot = DFA.unpackEncodedString(DFA18_eotS);
    static final short[] DFA18_eof = DFA.unpackEncodedString(DFA18_eofS);
    static final char[] DFA18_min = DFA.unpackEncodedStringToUnsignedChars(DFA18_minS);
    static final char[] DFA18_max = DFA.unpackEncodedStringToUnsignedChars(DFA18_maxS);
    static final short[] DFA18_accept = DFA.unpackEncodedString(DFA18_acceptS);
    static final short[] DFA18_special = DFA.unpackEncodedString(DFA18_specialS);
    static final short[][] DFA18_transition;

    static {
        int numStates = DFA18_transitionS.length;
        DFA18_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA18_transition[i] = DFA.unpackEncodedString(DFA18_transitionS[i]);
        }
    }

    class DFA18 extends DFA {

        public DFA18(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 18;
            this.eot = DFA18_eot;
            this.eof = DFA18_eof;
            this.min = DFA18_min;
            this.max = DFA18_max;
            this.accept = DFA18_accept;
            this.special = DFA18_special;
            this.transition = DFA18_transition;
        }
        public String getDescription() {
            return "255:1: struct_reg[PropagationEngine pe] returns [PropagationStrategy item] : ( ^( STREG id= IDENTIFIER . . ) | ^( STREG id= IDENTIFIER ca= comb_attr . . ) );";
        }
    }
    static final String DFA55_eotS =
        "\17\uffff";
    static final String DFA55_eofS =
        "\17\uffff";
    static final String DFA55_minS =
        "\1\7\1\uffff\1\2\1\uffff\1\50\1\2\1\31\1\2\1\3\1\52\1\23\1\52\2"+
        "\3\1\23";
    static final String DFA55_maxS =
        "\1\135\1\uffff\1\2\1\uffff\1\50\1\2\1\51\1\2\1\3\1\52\1\135\1\52"+
        "\2\3\1\135";
    static final String DFA55_acceptS =
        "\1\uffff\1\1\1\uffff\1\2\13\uffff";
    static final String DFA55_specialS =
        "\17\uffff}>";
    static final String[] DFA55_transitionS = {
            "\1\1\1\2\1\1\11\uffff\1\1\5\uffff\1\1\71\uffff\1\1\11\uffff"+
            "\1\3",
            "",
            "\1\4",
            "",
            "\1\5",
            "\1\6",
            "\1\7\17\uffff\1\10",
            "\1\11",
            "\1\12",
            "\1\13",
            "\1\1\5\uffff\1\1\16\uffff\1\5\52\uffff\1\1\11\uffff\1\3",
            "\1\14",
            "\1\15",
            "\1\16",
            "\1\1\5\uffff\1\1\16\uffff\1\5\52\uffff\1\1\11\uffff\1\3"
    };

    static final short[] DFA55_eot = DFA.unpackEncodedString(DFA55_eotS);
    static final short[] DFA55_eof = DFA.unpackEncodedString(DFA55_eofS);
    static final char[] DFA55_min = DFA.unpackEncodedStringToUnsignedChars(DFA55_minS);
    static final char[] DFA55_max = DFA.unpackEncodedStringToUnsignedChars(DFA55_maxS);
    static final short[] DFA55_accept = DFA.unpackEncodedString(DFA55_acceptS);
    static final short[] DFA55_special = DFA.unpackEncodedString(DFA55_specialS);
    static final short[][] DFA55_transition;

    static {
        int numStates = DFA55_transitionS.length;
        DFA55_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA55_transition[i] = DFA.unpackEncodedString(DFA55_transitionS[i]);
        }
    }

    class DFA55 extends DFA {

        public DFA55(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 55;
            this.eot = DFA55_eot;
            this.eof = DFA55_eof;
            this.min = DFA55_min;
            this.max = DFA55_max;
            this.accept = DFA55_accept;
            this.special = DFA55_special;
            this.transition = DFA55_transition;
        }
        public String getDescription() {
            return "796:1: pred_param_type : ( par_pred_param_type | var_pred_param_type );";
        }
    }
    static final String DFA64_eotS =
        "\32\uffff";
    static final String DFA64_eofS =
        "\32\uffff";
    static final String DFA64_minS =
        "\1\7\3\uffff\2\2\1\23\1\50\2\uffff\1\2\1\31\1\2\1\3\1\52\1\23\1"+
        "\52\2\uffff\1\2\1\3\1\23\1\3\2\uffff\1\23";
    static final String DFA64_maxS =
        "\1\123\3\uffff\2\2\1\31\1\50\2\uffff\1\2\1\51\1\2\1\3\1\52\1\123"+
        "\1\52\2\uffff\1\2\1\3\1\31\1\3\2\uffff\1\123";
    static final String DFA64_acceptS =
        "\1\uffff\1\1\1\2\1\3\4\uffff\1\4\1\5\7\uffff\1\6\1\7\4\uffff\1\10"+
        "\1\11\1\uffff";
    static final String DFA64_specialS =
        "\32\uffff}>";
    static final String[] DFA64_transitionS = {
            "\1\1\1\5\1\1\11\uffff\1\3\5\uffff\1\2\71\uffff\1\4",
            "",
            "",
            "",
            "\1\6",
            "\1\7",
            "\1\11\5\uffff\1\10",
            "\1\12",
            "",
            "",
            "\1\13",
            "\1\14\17\uffff\1\15",
            "\1\16",
            "\1\17",
            "\1\20",
            "\1\22\5\uffff\1\21\16\uffff\1\12\52\uffff\1\23",
            "\1\24",
            "",
            "",
            "\1\25",
            "\1\26",
            "\1\30\5\uffff\1\27",
            "\1\31",
            "",
            "",
            "\1\22\5\uffff\1\21\16\uffff\1\12\52\uffff\1\23"
    };

    static final short[] DFA64_eot = DFA.unpackEncodedString(DFA64_eotS);
    static final short[] DFA64_eof = DFA.unpackEncodedString(DFA64_eofS);
    static final char[] DFA64_min = DFA.unpackEncodedStringToUnsignedChars(DFA64_minS);
    static final char[] DFA64_max = DFA.unpackEncodedStringToUnsignedChars(DFA64_maxS);
    static final short[] DFA64_accept = DFA.unpackEncodedString(DFA64_acceptS);
    static final short[] DFA64_special = DFA.unpackEncodedString(DFA64_specialS);
    static final short[][] DFA64_transition;

    static {
        int numStates = DFA64_transitionS.length;
        DFA64_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA64_transition[i] = DFA.unpackEncodedString(DFA64_transitionS[i]);
        }
    }

    class DFA64 extends DFA {

        public DFA64(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 64;
            this.eot = DFA64_eot;
            this.eof = DFA64_eof;
            this.min = DFA64_min;
            this.max = DFA64_max;
            this.accept = DFA64_accept;
            this.special = DFA64_special;
            this.transition = DFA64_transition;
        }
        public String getDescription() {
            return "802:1: par_pred_param_type : ( par_type | ^( DD INT_CONST INT_CONST ) | ^( CM ( INT_CONST )+ ) | ^( SET ^( DD INT_CONST INT_CONST ) ) | ^( SET ^( CM ( INT_CONST )+ ) ) | ^( ARRAY ( index_set )+ ^( DD INT_CONST INT_CONST ) ) | ^( ARRAY ( index_set )+ ^( CM ( INT_CONST )+ ) ) | ^( ARRAY ( index_set )+ ^( SET ^( DD INT_CONST INT_CONST ) ) ) | ^( ARRAY ( index_set )+ ^( SET ^( CM ( INT_CONST )+ ) ) ) );";
        }
    }
 

    public static final BitSet FOLLOW_pred_decl_in_flatzinc_model53 = new BitSet(new long[]{0x0000004000400000L,0x0000000020200420L});
    public static final BitSet FOLLOW_param_decl_in_flatzinc_model58 = new BitSet(new long[]{0x0000004000400000L,0x0000000020200020L});
    public static final BitSet FOLLOW_var_decl_in_flatzinc_model63 = new BitSet(new long[]{0x0000004000400000L,0x0000000020200000L});
    public static final BitSet FOLLOW_constraint_in_flatzinc_model68 = new BitSet(new long[]{0x0000004000400000L,0x0000000000200000L});
    public static final BitSet FOLLOW_engine_in_flatzinc_model72 = new BitSet(new long[]{0x0000000000000000L,0x0000000000200000L});
    public static final BitSet FOLLOW_solve_goal_in_flatzinc_model75 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_group_decl_in_engine108 = new BitSet(new long[]{0x0000004000000000L,0x0000000003400000L});
    public static final BitSet FOLLOW_structure_in_engine117 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENTIFIER_in_group_decl139 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_predicates_in_group_decl143 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_predicate_in_predicates178 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_AND_in_predicates203 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_predicates_in_predicates208 = new BitSet(new long[]{0x0200008000910018L,0x00000000E8000B48L});
    public static final BitSet FOLLOW_OR_in_predicates237 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_predicates_in_predicates242 = new BitSet(new long[]{0x0200008000910018L,0x00000000E8000B48L});
    public static final BitSet FOLLOW_TRUE_in_predicate273 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_attribute_in_predicate283 = new BitSet(new long[]{0xE800000000000000L,0x0000000000000005L});
    public static final BitSet FOLLOW_op_in_predicate287 = new BitSet(new long[]{0x0000040000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_predicate291 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IN_in_predicate308 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_IDENTIFIER_in_predicate313 = new BitSet(new long[]{0x0000004000000008L});
    public static final BitSet FOLLOW_NOT_in_predicate325 = new BitSet(new long[]{0x0200008000910000L,0x00000000E8000B40L});
    public static final BitSet FOLLOW_predicate_in_predicate329 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_VAR_in_attribute356 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CSTR_in_attribute372 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PROP_in_attribute387 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_VNAME_in_attribute402 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_VCARD_in_attribute417 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CNAME_in_attribute431 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CARITY_in_attribute446 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PPRIO_in_attribute459 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PARITY_in_attribute473 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PPRIOD_in_attribute486 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_OEQ_in_op514 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ONQ_in_op526 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_OLT_in_op538 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_OGT_in_op550 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_OLQ_in_op562 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_OGQ_in_op574 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_struct_in_structure603 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_struct_reg_in_structure616 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRUC1_in_struct649 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_elt_in_struct656 = new BitSet(new long[]{0x0000205000000000L,0x0000000003401000L});
    public static final BitSet FOLLOW_coll_in_struct664 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_STRUC2_in_struct674 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_elt_in_struct681 = new BitSet(new long[]{0x000000400000C000L,0x0000000003400000L});
    public static final BitSet FOLLOW_comb_attr_in_struct689 = new BitSet(new long[]{0x0000201000000000L,0x0000000000001000L});
    public static final BitSet FOLLOW_coll_in_struct693 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_STREG_in_struct_reg721 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_IDENTIFIER_in_struct_reg725 = new BitSet(new long[]{0xFFFFFFFFFFFFFFF0L,0x00000007FFFFFFFFL});
    public static final BitSet FOLLOW_STREG_in_struct_reg746 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_IDENTIFIER_in_struct_reg750 = new BitSet(new long[]{0x000000000000C000L});
    public static final BitSet FOLLOW_comb_attr_in_struct_reg754 = new BitSet(new long[]{0xFFFFFFFFFFFFFFF0L,0x00000007FFFFFFFFL});
    public static final BitSet FOLLOW_struct_in_elt786 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_struct_reg_in_elt800 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENTIFIER_in_elt809 = new BitSet(new long[]{0x0000080000000002L});
    public static final BitSet FOLLOW_KEY_in_elt812 = new BitSet(new long[]{0x0000000000910000L,0x00000000E0000B40L});
    public static final BitSet FOLLOW_attribute_in_elt816 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MANY1_in_many856 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_attribute_in_many860 = new BitSet(new long[]{0xFFFFFFFFFFFFFFF0L,0x00000007FFFFFFFFL});
    public static final BitSet FOLLOW_MANY2_in_many877 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_attribute_in_many881 = new BitSet(new long[]{0x000000000000C000L});
    public static final BitSet FOLLOW_comb_attr_in_many885 = new BitSet(new long[]{0xFFFFFFFFFFFFFFF0L,0x00000007FFFFFFFFL});
    public static final BitSet FOLLOW_MANY3_in_many902 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_attribute_in_many906 = new BitSet(new long[]{0xFFFFFFFFFFFFFFF0L,0x00000007FFFFFFFFL});
    public static final BitSet FOLLOW_MANY4_in_many927 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_attribute_in_many931 = new BitSet(new long[]{0x000000000000C000L});
    public static final BitSet FOLLOW_comb_attr_in_many935 = new BitSet(new long[]{0xFFFFFFFFFFFFFFF0L,0x00000007FFFFFFFFL});
    public static final BitSet FOLLOW_QUEUE_in_coll978 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_qiter_in_coll982 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_LIST_in_coll998 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_REV_in_coll1002 = new BitSet(new long[]{0x0000000800000000L,0x0000000300000002L});
    public static final BitSet FOLLOW_liter_in_coll1007 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_HEAP_in_coll1023 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_MAX_in_coll1027 = new BitSet(new long[]{0x0000000000000000L,0x0000000200000002L});
    public static final BitSet FOLLOW_qiter_in_coll1032 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_ONE_in_qiter1059 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_WONE_in_qiter1071 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_qiter_in_liter1103 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FOR_in_liter1115 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_WFOR_in_liter1127 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CA1_in_comb_attr1161 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_attr_op_in_comb_attr1168 = new BitSet(new long[]{0x0050000000910048L,0x00000000E4100B40L});
    public static final BitSet FOLLOW_attribute_in_comb_attr1175 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_CA2_in_comb_attr1188 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_attr_op_in_comb_attr1195 = new BitSet(new long[]{0x0050000000910040L,0x00000000E4100B40L});
    public static final BitSet FOLLOW_attribute_in_comb_attr1202 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_ANY_in_attr_op1220 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MIN_in_attr_op1232 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MAX_in_attr_op1244 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SUM_in_attr_op1256 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SIZE_in_attr_op1268 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ARRPAR_in_par_type1312 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_index_set_in_par_type1317 = new BitSet(new long[]{0x0000030400002000L,0x0000000000080000L});
    public static final BitSet FOLLOW_par_type_u_in_par_type1324 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_APAR_in_par_type1342 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_par_type_u_in_par_type1346 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_BOOL_in_par_type_u1376 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FLOAT_in_par_type_u1392 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SET_in_par_type_u1408 = new BitSet(new long[]{0x1000000000000000L});
    public static final BitSet FOLLOW_OF_in_par_type_u1410 = new BitSet(new long[]{0x0000020000000000L});
    public static final BitSet FOLLOW_INT_in_par_type_u1412 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INT_in_par_type_u1428 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ARRVAR_in_var_type1469 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_index_set_in_var_type1474 = new BitSet(new long[]{0x0000030402082000L,0x0000000000080000L});
    public static final BitSet FOLLOW_var_type_u_in_var_type1481 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_AVAR_in_var_type1499 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_var_type_u_in_var_type1503 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_BOOL_in_var_type_u1534 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FLOAT_in_var_type_u1550 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INT_in_var_type_u1566 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DD_in_var_type_u1583 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_INT_CONST_in_var_type_u1587 = new BitSet(new long[]{0x0000040000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_var_type_u1591 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_CM_in_var_type_u1626 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_INT_CONST_in_var_type_u1631 = new BitSet(new long[]{0x0000040000000008L});
    public static final BitSet FOLLOW_SET_in_var_type_u1652 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_DD_in_var_type_u1655 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_INT_CONST_in_var_type_u1659 = new BitSet(new long[]{0x0000040000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_var_type_u1663 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_SET_in_var_type_u1693 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_CM_in_var_type_u1696 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_INT_CONST_in_var_type_u1701 = new BitSet(new long[]{0x0000040000000008L});
    public static final BitSet FOLLOW_INDEX_in_index_set1736 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_DD_in_index_set1739 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_INT_CONST_in_index_set1743 = new BitSet(new long[]{0x0000040000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_index_set1747 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_INDEX_in_index_set1766 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_INT_in_index_set1768 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_LB_in_expr1812 = new BitSet(new long[]{0x0000040000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_expr1817 = new BitSet(new long[]{0x0000040000000000L,0x0000000000002000L});
    public static final BitSet FOLLOW_RB_in_expr1822 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_bool_const_in_expr1840 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INT_CONST_in_expr1858 = new BitSet(new long[]{0x0000000002000002L});
    public static final BitSet FOLLOW_DD_in_expr1861 = new BitSet(new long[]{0x0000040000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_expr1865 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_EXPR_in_expr1895 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_LS_in_expr1897 = new BitSet(new long[]{0x0000144300000000L,0x0000000008810000L});
    public static final BitSet FOLLOW_expr_in_expr1902 = new BitSet(new long[]{0x0000144300000000L,0x0000000008810000L});
    public static final BitSet FOLLOW_RS_in_expr1907 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_id_expr_in_expr1926 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_in_expr1942 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENTIFIER_in_id_expr2000 = new BitSet(new long[]{0x0000C00000000002L});
    public static final BitSet FOLLOW_LP_in_id_expr2004 = new BitSet(new long[]{0x0000144300000000L,0x0000000008800000L});
    public static final BitSet FOLLOW_expr_in_id_expr2008 = new BitSet(new long[]{0x0000000000080000L,0x0000000000008000L});
    public static final BitSet FOLLOW_CM_in_id_expr2012 = new BitSet(new long[]{0x0000144300000000L,0x0000000008800000L});
    public static final BitSet FOLLOW_expr_in_id_expr2016 = new BitSet(new long[]{0x0000000000080000L,0x0000000000008000L});
    public static final BitSet FOLLOW_RP_in_id_expr2021 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LS_in_id_expr2025 = new BitSet(new long[]{0x0000040000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_id_expr2029 = new BitSet(new long[]{0x0000000000000000L,0x0000000000010000L});
    public static final BitSet FOLLOW_RS_in_id_expr2031 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PAR_in_param_decl2058 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_IDENTIFIER_in_param_decl2060 = new BitSet(new long[]{0x0000000000000280L});
    public static final BitSet FOLLOW_par_type_in_param_decl2064 = new BitSet(new long[]{0x0000144300000000L,0x0000000008800000L});
    public static final BitSet FOLLOW_expr_in_param_decl2068 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_VAR_in_var_decl2087 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_IDENTIFIER_in_var_decl2089 = new BitSet(new long[]{0x0000000000001400L});
    public static final BitSet FOLLOW_var_type_in_var_decl2093 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_annotations_in_var_decl2097 = new BitSet(new long[]{0x0000144300000008L,0x0000000008800000L});
    public static final BitSet FOLLOW_expr_in_var_decl2101 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_CONSTRAINT_in_constraint2126 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_IDENTIFIER_in_constraint2128 = new BitSet(new long[]{0x0000144300000000L,0x0000000008800000L});
    public static final BitSet FOLLOW_expr_in_constraint2133 = new BitSet(new long[]{0x0000144300000020L,0x0000000008800000L});
    public static final BitSet FOLLOW_annotations_in_constraint2140 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_SOLVE_in_solve_goal2160 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_annotations_in_solve_goal2164 = new BitSet(new long[]{0x00A0000000000000L,0x0000000000020000L});
    public static final BitSet FOLLOW_resolution_in_solve_goal2168 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_SATISFY_in_resolution2192 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MINIMIZE_in_resolution2203 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_expr_in_resolution2207 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_MAXIMIZE_in_resolution2225 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_expr_in_resolution2229 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_ANNOTATIONS_in_annotations2272 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_annotation_in_annotations2277 = new BitSet(new long[]{0x0000004000000008L});
    public static final BitSet FOLLOW_IDENTIFIER_in_annotation2314 = new BitSet(new long[]{0x0000400000000002L});
    public static final BitSet FOLLOW_LP_in_annotation2317 = new BitSet(new long[]{0x0000144300000000L,0x0000000008800000L});
    public static final BitSet FOLLOW_expr_in_annotation2322 = new BitSet(new long[]{0x0000144300000000L,0x0000000008808000L});
    public static final BitSet FOLLOW_RP_in_annotation2327 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TRUE_in_bool_const2359 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FALSE_in_bool_const2371 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PREDICATE_in_pred_decl2390 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_IDENTIFIER_in_pred_decl2392 = new BitSet(new long[]{0x0000000000040000L});
    public static final BitSet FOLLOW_pred_param_in_pred_decl2394 = new BitSet(new long[]{0x0000000000040008L});
    public static final BitSet FOLLOW_CL_in_pred_param2417 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_pred_param_type_in_pred_param2419 = new BitSet(new long[]{0x0000004000000000L});
    public static final BitSet FOLLOW_IDENTIFIER_in_pred_param2421 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_par_pred_param_type_in_pred_param_type2442 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_var_pred_param_type_in_pred_param_type2452 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_par_type_in_par_pred_param_type2472 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DD_in_par_pred_param_type2484 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type2486 = new BitSet(new long[]{0x0000040000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type2488 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_CM_in_par_pred_param_type2500 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type2502 = new BitSet(new long[]{0x0000040000000008L});
    public static final BitSet FOLLOW_SET_in_par_pred_param_type2515 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_DD_in_par_pred_param_type2518 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type2520 = new BitSet(new long[]{0x0000040000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type2522 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_SET_in_par_pred_param_type2535 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_CM_in_par_pred_param_type2538 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type2540 = new BitSet(new long[]{0x0000040000000008L});
    public static final BitSet FOLLOW_ARRAY_in_par_pred_param_type2555 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_index_set_in_par_pred_param_type2557 = new BitSet(new long[]{0x0000010002000000L});
    public static final BitSet FOLLOW_DD_in_par_pred_param_type2561 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type2563 = new BitSet(new long[]{0x0000040000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type2565 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_ARRAY_in_par_pred_param_type2578 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_index_set_in_par_pred_param_type2580 = new BitSet(new long[]{0x0000010000080000L});
    public static final BitSet FOLLOW_CM_in_par_pred_param_type2584 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type2586 = new BitSet(new long[]{0x0000040000000008L});
    public static final BitSet FOLLOW_ARRAY_in_par_pred_param_type2600 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_index_set_in_par_pred_param_type2602 = new BitSet(new long[]{0x0000010000000000L,0x0000000000080000L});
    public static final BitSet FOLLOW_SET_in_par_pred_param_type2606 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_DD_in_par_pred_param_type2609 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type2611 = new BitSet(new long[]{0x0000040000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type2613 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_ARRAY_in_par_pred_param_type2627 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_index_set_in_par_pred_param_type2629 = new BitSet(new long[]{0x0000010000000000L,0x0000000000080000L});
    public static final BitSet FOLLOW_SET_in_par_pred_param_type2633 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_CM_in_par_pred_param_type2636 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type2638 = new BitSet(new long[]{0x0000040000000008L});
    public static final BitSet FOLLOW_VAR_in_var_pred_param_type2663 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_var_type_in_var_pred_param_type2665 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_VAR_in_var_pred_param_type2677 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_SET_in_var_pred_param_type2679 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_ARRAY_in_var_pred_param_type2691 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_index_set_in_var_pred_param_type2693 = new BitSet(new long[]{0x0000010000000000L,0x0000000020000000L});
    public static final BitSet FOLLOW_VAR_in_var_pred_param_type2697 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_SET_in_var_pred_param_type2699 = new BitSet(new long[]{0x0000000000000008L});

}