// $ANTLR 3.4 parser/flatzinc/FlatzincFullExtWalker.g 2012-11-19 15:03:03

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
import parser.flatzinc.ast.FConstraint;
import parser.flatzinc.ast.FGoal;
import parser.flatzinc.ast.FParameter;
import parser.flatzinc.ast.FVariable;
import parser.flatzinc.ast.declaration.*;
import parser.flatzinc.ast.expression.*;
import parser.flatzinc.ast.ext.*;
import solver.Solver;
import solver.propagation.ISchedulable;
import solver.propagation.PropagationEngine;
import solver.propagation.generator.*;
import solver.propagation.hardcoded.ConstraintEngine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings({"all", "warnings", "unchecked"})
public class FlatzincFullExtWalker extends TreeParser {
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

    // search for all solutions
    public boolean all = false;
    // free search strategy
    public boolean free = false;

    // the solver
    public Solver mSolver;

    // the layout dedicated to pretty print message wrt to fzn recommendations
    public final FZNLayout mLayout = new FZNLayout();



    // $ANTLR start "flatzinc_model"
    // parser/flatzinc/FlatzincFullExtWalker.g:104:1: flatzinc_model[Solver aSolver, THashMap<String, Object> map] : ( pred_decl )* ( param_decl )* ( var_decl )* ( constraint )* ( group_decl[arcs] )* (ps= structure[propagationEngine] )? solve_goal ;
    public final void flatzinc_model(Solver aSolver, THashMap<String, Object> map) throws RecognitionException {
        PropagationStrategy ps =null;


        try {
            // parser/flatzinc/FlatzincFullExtWalker.g:105:2: ( ( pred_decl )* ( param_decl )* ( var_decl )* ( constraint )* ( group_decl[arcs] )* (ps= structure[propagationEngine] )? solve_goal )
            // parser/flatzinc/FlatzincFullExtWalker.g:106:2: ( pred_decl )* ( param_decl )* ( var_decl )* ( constraint )* ( group_decl[arcs] )* (ps= structure[propagationEngine] )? solve_goal
            {

            	this.mSolver = aSolver;
            	this.map = map;
            	this.groups = new THashMap();
                

            // parser/flatzinc/FlatzincFullExtWalker.g:111:5: ( pred_decl )*
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
            	    // parser/flatzinc/FlatzincFullExtWalker.g:111:6: pred_decl
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


            // parser/flatzinc/FlatzincFullExtWalker.g:111:18: ( param_decl )*
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
            	    // parser/flatzinc/FlatzincFullExtWalker.g:111:19: param_decl
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


            // parser/flatzinc/FlatzincFullExtWalker.g:111:32: ( var_decl )*
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
            	    // parser/flatzinc/FlatzincFullExtWalker.g:111:33: var_decl
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


            // parser/flatzinc/FlatzincFullExtWalker.g:111:44: ( constraint )*
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
            	    // parser/flatzinc/FlatzincFullExtWalker.g:111:45: constraint
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



            	ArrayList<Arc> arcs= Arc.populate(mSolver);
            	

            // parser/flatzinc/FlatzincFullExtWalker.g:115:2: ( group_decl[arcs] )*
            loop5:
            do {
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
            	    // parser/flatzinc/FlatzincFullExtWalker.g:115:3: group_decl[arcs]
            	    {
            	    pushFollow(FOLLOW_group_decl_in_flatzinc_model77);
            	    group_decl(arcs);

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    break loop5;
                }
            } while (true);



            	if(!arcs.isEmpty()){
            	    LOGGER.warn("% Remaining arcs after group declarations");
            	}

            	PropagationEngine propagationEngine = new PropagationEngine(mSolver);
            	

            // parser/flatzinc/FlatzincFullExtWalker.g:123:2: (ps= structure[propagationEngine] )?
            int alt6=2;
            switch ( input.LA(1) ) {
                case STREG:
                case STRUC:
                    {
                    alt6=1;
                    }
                    break;
            }

            switch (alt6) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:123:3: ps= structure[propagationEngine]
                    {
                    pushFollow(FOLLOW_structure_in_flatzinc_model91);
                    ps=structure(propagationEngine);

                    state._fsp--;


                    }
                    break;

            }



                if (ps == null) {
                    if (mSolver.getEngine() == null) {
                        LOGGER.warn("% no engine defined -- use default one instead");
                        mSolver.set(new ConstraintEngine(mSolver));
                    }
                } else {
                    mSolver.set(propagationEngine.set(ps));
                }
                

            pushFollow(FOLLOW_solve_goal_in_flatzinc_model103);
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



    // $ANTLR start "group_decl"
    // parser/flatzinc/FlatzincFullExtWalker.g:171:1: group_decl[ArrayList<Arc> arcs] : ^( IDENTIFIER p= predicates ) ;
    public final void group_decl(ArrayList<Arc> arcs) throws RecognitionException {
        CommonTree IDENTIFIER1=null;
        Predicate p =null;


        try {
            // parser/flatzinc/FlatzincFullExtWalker.g:172:5: ( ^( IDENTIFIER p= predicates ) )
            // parser/flatzinc/FlatzincFullExtWalker.g:173:5: ^( IDENTIFIER p= predicates )
            {
            IDENTIFIER1=(CommonTree)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_group_decl157); 

            match(input, Token.DOWN, null); 
            pushFollow(FOLLOW_predicates_in_group_decl161);
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
    // parser/flatzinc/FlatzincFullExtWalker.g:187:1: predicates returns [Predicate pred] : (p= predicate | ^( AND (p= predicates )+ ) | ^( OR (p= predicates )+ ) );
    public final Predicate predicates() throws RecognitionException {
        Predicate pred = null;


        Predicate p =null;


        try {
            // parser/flatzinc/FlatzincFullExtWalker.g:188:5: (p= predicate | ^( AND (p= predicates )+ ) | ^( OR (p= predicates )+ ) )
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
                    // parser/flatzinc/FlatzincFullExtWalker.g:188:9: p= predicate
                    {
                    pushFollow(FOLLOW_predicate_in_predicates196);
                    p=predicate();

                    state._fsp--;



                        pred = p;
                        

                    }
                    break;
                case 2 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:193:5: ^( AND (p= predicates )+ )
                    {

                        ArrayList<Predicate> preds = new ArrayList();
                        

                    match(input,AND,FOLLOW_AND_in_predicates221); 

                    match(input, Token.DOWN, null); 
                    // parser/flatzinc/FlatzincFullExtWalker.g:196:11: (p= predicates )+
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
                    	    // parser/flatzinc/FlatzincFullExtWalker.g:196:12: p= predicates
                    	    {
                    	    pushFollow(FOLLOW_predicates_in_predicates226);
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
                    // parser/flatzinc/FlatzincFullExtWalker.g:201:5: ^( OR (p= predicates )+ )
                    {

                        ArrayList<Predicate> preds = new ArrayList();
                        

                    match(input,OR,FOLLOW_OR_in_predicates255); 

                    match(input, Token.DOWN, null); 
                    // parser/flatzinc/FlatzincFullExtWalker.g:204:10: (p= predicates )+
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
                    	    // parser/flatzinc/FlatzincFullExtWalker.g:204:11: p= predicates
                    	    {
                    	    pushFollow(FOLLOW_predicates_in_predicates260);
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
    // parser/flatzinc/FlatzincFullExtWalker.g:211:1: predicate returns [Predicate pred] : ( TRUE |a= attribute o= op i= INT_CONST | ^( IN (i= IDENTIFIER )+ ) | NOT p= predicate );
    public final Predicate predicate() throws RecognitionException {
        Predicate pred = null;


        CommonTree i=null;
        Attribute a =null;

        Operator o =null;

        Predicate p =null;


        try {
            // parser/flatzinc/FlatzincFullExtWalker.g:212:2: ( TRUE |a= attribute o= op i= INT_CONST | ^( IN (i= IDENTIFIER )+ ) | NOT p= predicate )
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
                    // parser/flatzinc/FlatzincFullExtWalker.g:212:4: TRUE
                    {
                    match(input,TRUE,FOLLOW_TRUE_in_predicate291); 


                    	pred = TruePredicate.singleton;
                    	

                    }
                    break;
                case 2 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:216:4: a= attribute o= op i= INT_CONST
                    {
                    pushFollow(FOLLOW_attribute_in_predicate301);
                    a=attribute();

                    state._fsp--;


                    pushFollow(FOLLOW_op_in_predicate305);
                    o=op();

                    state._fsp--;


                    i=(CommonTree)match(input,INT_CONST,FOLLOW_INT_CONST_in_predicate309); 


                        pred = new IntPredicate(a,o,Integer.valueOf((i!=null?i.getText():null)));
                        

                    }
                    break;
                case 3 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:221:2: ^( IN (i= IDENTIFIER )+ )
                    {

                    	ArrayList<String> ids = new ArrayList();
                    	

                    match(input,IN,FOLLOW_IN_in_predicate326); 

                    match(input, Token.DOWN, null); 
                    // parser/flatzinc/FlatzincFullExtWalker.g:224:11: (i= IDENTIFIER )+
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
                    	    // parser/flatzinc/FlatzincFullExtWalker.g:224:12: i= IDENTIFIER
                    	    {
                    	    i=(CommonTree)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_predicate331); 

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
                    // parser/flatzinc/FlatzincFullExtWalker.g:228:4: NOT p= predicate
                    {
                    match(input,NOT,FOLLOW_NOT_in_predicate343); 

                    pushFollow(FOLLOW_predicate_in_predicate347);
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
    // parser/flatzinc/FlatzincFullExtWalker.g:236:1: attribute returns [Attribute attr] : ( VAR | CSTR | PROP | VNAME | VCARD | CNAME | CARITY | PPRIO | PARITY | PPRIOD );
    public final Attribute attribute() throws RecognitionException {
        Attribute attr = null;


        try {
            // parser/flatzinc/FlatzincFullExtWalker.g:237:5: ( VAR | CSTR | PROP | VNAME | VCARD | CNAME | CARITY | PPRIO | PARITY | PPRIOD )
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
                    // parser/flatzinc/FlatzincFullExtWalker.g:237:9: VAR
                    {
                    match(input,VAR,FOLLOW_VAR_in_attribute374); 

                    attr = Attribute.VAR;

                    }
                    break;
                case 2 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:238:9: CSTR
                    {
                    match(input,CSTR,FOLLOW_CSTR_in_attribute390); 

                    attr = Attribute.CSTR;

                    }
                    break;
                case 3 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:239:9: PROP
                    {
                    match(input,PROP,FOLLOW_PROP_in_attribute405); 

                    attr = Attribute.PROP;

                    }
                    break;
                case 4 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:240:9: VNAME
                    {
                    match(input,VNAME,FOLLOW_VNAME_in_attribute420); 

                    attr = Attribute.VNAME;

                    }
                    break;
                case 5 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:241:9: VCARD
                    {
                    match(input,VCARD,FOLLOW_VCARD_in_attribute435); 

                    attr = Attribute.VCARD;

                    }
                    break;
                case 6 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:242:9: CNAME
                    {
                    match(input,CNAME,FOLLOW_CNAME_in_attribute449); 

                    attr = Attribute.CNAME;

                    }
                    break;
                case 7 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:243:9: CARITY
                    {
                    match(input,CARITY,FOLLOW_CARITY_in_attribute464); 

                    attr = Attribute.CARITY;

                    }
                    break;
                case 8 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:244:9: PPRIO
                    {
                    match(input,PPRIO,FOLLOW_PPRIO_in_attribute477); 

                    attr = Attribute.PPRIO;

                    }
                    break;
                case 9 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:245:9: PARITY
                    {
                    match(input,PARITY,FOLLOW_PARITY_in_attribute491); 

                    attr = Attribute.PARITY;

                    }
                    break;
                case 10 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:246:9: PPRIOD
                    {
                    match(input,PPRIOD,FOLLOW_PPRIOD_in_attribute504); 

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
    // parser/flatzinc/FlatzincFullExtWalker.g:250:1: op returns [Operator value] : ( OEQ | ONQ | OLT | OGT | OLQ | OGQ );
    public final Operator op() throws RecognitionException {
        Operator value = null;


        try {
            // parser/flatzinc/FlatzincFullExtWalker.g:251:5: ( OEQ | ONQ | OLT | OGT | OLQ | OGQ )
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
                    // parser/flatzinc/FlatzincFullExtWalker.g:251:9: OEQ
                    {
                    match(input,OEQ,FOLLOW_OEQ_in_op532); 

                    value = Operator.EQ;

                    }
                    break;
                case 2 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:252:9: ONQ
                    {
                    match(input,ONQ,FOLLOW_ONQ_in_op544); 

                    value = Operator.NQ;

                    }
                    break;
                case 3 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:253:9: OLT
                    {
                    match(input,OLT,FOLLOW_OLT_in_op556); 

                    value = Operator.LT;

                    }
                    break;
                case 4 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:254:9: OGT
                    {
                    match(input,OGT,FOLLOW_OGT_in_op568); 

                    value = Operator.GT;

                    }
                    break;
                case 5 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:255:9: OLQ
                    {
                    match(input,OLQ,FOLLOW_OLQ_in_op580); 

                    value = Operator.LQ;

                    }
                    break;
                case 6 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:256:9: OGQ
                    {
                    match(input,OGQ,FOLLOW_OGQ_in_op592); 

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
    // parser/flatzinc/FlatzincFullExtWalker.g:262:1: structure[PropagationEngine pe] returns [PropagationStrategy ps] : (s= struct[pe] |sr= struct_reg[pe] );
    public final PropagationStrategy structure(PropagationEngine pe) throws RecognitionException {
        PropagationStrategy ps = null;


        PropagationStrategy s =null;

        PropagationStrategy sr =null;


        try {
            // parser/flatzinc/FlatzincFullExtWalker.g:263:2: (s= struct[pe] |sr= struct_reg[pe] )
            int alt14=2;
            switch ( input.LA(1) ) {
            case STRUC:
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
                    // parser/flatzinc/FlatzincFullExtWalker.g:263:4: s= struct[pe]
                    {
                    pushFollow(FOLLOW_struct_in_structure621);
                    s=struct(pe);

                    state._fsp--;



                    	ps = s;
                    	

                    }
                    break;
                case 2 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:267:6: sr= struct_reg[pe]
                    {
                    pushFollow(FOLLOW_struct_reg_in_structure634);
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
    // parser/flatzinc/FlatzincFullExtWalker.g:273:1: struct[PropagationEngine pe] returns [PropagationStrategy item] : ^( STRUC (element= elt[pe] )+ (ca= comb_attr )? c= coll[elements, ca] ) ;
    public final PropagationStrategy struct(PropagationEngine pe) throws RecognitionException {
        PropagationStrategy item = null;


        ISchedulable[] element =null;

        CombinedAttribute ca =null;

        PropagationStrategy c =null;


        try {
            // parser/flatzinc/FlatzincFullExtWalker.g:274:5: ( ^( STRUC (element= elt[pe] )+ (ca= comb_attr )? c= coll[elements, ca] ) )
            // parser/flatzinc/FlatzincFullExtWalker.g:274:7: ^( STRUC (element= elt[pe] )+ (ca= comb_attr )? c= coll[elements, ca] )
            {
            match(input,STRUC,FOLLOW_STRUC_in_struct659); 


                // init list
                ArrayList<ISchedulable> elements = new ArrayList<ISchedulable>();
                

            match(input, Token.DOWN, null); 
            // parser/flatzinc/FlatzincFullExtWalker.g:279:5: (element= elt[pe] )+
            int cnt15=0;
            loop15:
            do {
                int alt15=2;
                switch ( input.LA(1) ) {
                case IDENTIFIER:
                case STREG:
                case STRUC:
                    {
                    alt15=1;
                    }
                    break;

                }

                switch (alt15) {
            	case 1 :
            	    // parser/flatzinc/FlatzincFullExtWalker.g:279:6: element= elt[pe]
            	    {
            	    pushFollow(FOLLOW_elt_in_struct676);
            	    element=elt(pe);

            	    state._fsp--;



            	        // feed list
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


            // parser/flatzinc/FlatzincFullExtWalker.g:285:7: (ca= comb_attr )?
            int alt16=2;
            switch ( input.LA(1) ) {
                case DO:
                    {
                    alt16=1;
                    }
                    break;
            }

            switch (alt16) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:285:7: ca= comb_attr
                    {
                    pushFollow(FOLLOW_comb_attr_in_struct698);
                    ca=comb_attr();

                    state._fsp--;


                    }
                    break;

            }


            pushFollow(FOLLOW_coll_in_struct707);
            c=coll(elements, ca);

            state._fsp--;


            match(input, Token.UP, null); 



                item = c;
                

            }

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
    // parser/flatzinc/FlatzincFullExtWalker.g:292:1: struct_reg[PropagationEngine pe] returns [PropagationStrategy item] : ^( STREG IDENTIFIER . (ca= comb_attr )? c= coll[pss, ca] ) ;
    public final PropagationStrategy struct_reg(PropagationEngine pe) throws RecognitionException {
        PropagationStrategy item = null;


        CommonTree IDENTIFIER2=null;
        CombinedAttribute ca =null;

        PropagationStrategy c =null;


        try {
            // parser/flatzinc/FlatzincFullExtWalker.g:293:2: ( ^( STREG IDENTIFIER . (ca= comb_attr )? c= coll[pss, ca] ) )
            // parser/flatzinc/FlatzincFullExtWalker.g:293:4: ^( STREG IDENTIFIER . (ca= comb_attr )? c= coll[pss, ca] )
            {
            match(input,STREG,FOLLOW_STREG_in_struct_reg733); 

            match(input, Token.DOWN, null); 
            IDENTIFIER2=(CommonTree)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_struct_reg735); 


            	String id = (IDENTIFIER2!=null?IDENTIFIER2.getText():null);
                ArrayList<Arc> arcs = groups.get(id);
                if(arcs == null){
                    LOGGER.error("% Unknown group_decl :"+id);
                    throw new FZNException("Unknown group_decl :"+id);
                }
                for(int k = 0; k < arcs.size(); k++){
                    pe.declareArc(arcs.get(k));
                }
                int m_idx = input.mark();
            	

            matchAny(input); 


                input.seek(m_idx);
                ArrayList<PropagationStrategy> pss = many(arcs).pss;
                input.release(m_idx);
                

            // parser/flatzinc/FlatzincFullExtWalker.g:312:8: (ca= comb_attr )?
            int alt17=2;
            switch ( input.LA(1) ) {
                case DO:
                    {
                    alt17=1;
                    }
                    break;
            }

            switch (alt17) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:312:8: ca= comb_attr
                    {
                    pushFollow(FOLLOW_comb_attr_in_struct_reg760);
                    ca=comb_attr();

                    state._fsp--;


                    }
                    break;

            }


            pushFollow(FOLLOW_coll_in_struct_reg769);
            c=coll(pss, ca);

            state._fsp--;



                item = c;
                

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
        return item;
    }
    // $ANTLR end "struct_reg"



    // $ANTLR start "elt"
    // parser/flatzinc/FlatzincFullExtWalker.g:320:1: elt[PropagationEngine pe] returns [ISchedulable[] items] : (s= struct[pe] |sr= struct_reg[pe] | IDENTIFIER ( KEY a= attribute )? );
    public final ISchedulable[] elt(PropagationEngine pe) throws RecognitionException {
        ISchedulable[] items = null;


        CommonTree IDENTIFIER3=null;
        PropagationStrategy s =null;

        PropagationStrategy sr =null;

        Attribute a =null;


        try {
            // parser/flatzinc/FlatzincFullExtWalker.g:321:5: (s= struct[pe] |sr= struct_reg[pe] | IDENTIFIER ( KEY a= attribute )? )
            int alt19=3;
            switch ( input.LA(1) ) {
            case STRUC:
                {
                alt19=1;
                }
                break;
            case STREG:
                {
                alt19=2;
                }
                break;
            case IDENTIFIER:
                {
                alt19=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 19, 0, input);

                throw nvae;

            }

            switch (alt19) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:321:7: s= struct[pe]
                    {
                    pushFollow(FOLLOW_struct_in_elt804);
                    s=struct(pe);

                    state._fsp--;



                        items = new ISchedulable[]{s};
                        

                    }
                    break;
                case 2 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:325:4: sr= struct_reg[pe]
                    {
                    pushFollow(FOLLOW_struct_reg_in_elt818);
                    sr=struct_reg(pe);

                    state._fsp--;



                    	items = new ISchedulable[]{sr};
                    	

                    }
                    break;
                case 3 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:329:4: IDENTIFIER ( KEY a= attribute )?
                    {
                    IDENTIFIER3=(CommonTree)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_elt827); 

                    // parser/flatzinc/FlatzincFullExtWalker.g:329:15: ( KEY a= attribute )?
                    int alt18=2;
                    switch ( input.LA(1) ) {
                        case KEY:
                            {
                            alt18=1;
                            }
                            break;
                    }

                    switch (alt18) {
                        case 1 :
                            // parser/flatzinc/FlatzincFullExtWalker.g:329:16: KEY a= attribute
                            {
                            match(input,KEY,FOLLOW_KEY_in_elt830); 

                            pushFollow(FOLLOW_attribute_in_elt834);
                            a=attribute();

                            state._fsp--;


                            }
                            break;

                    }



                    	String id = (IDENTIFIER3!=null?IDENTIFIER3.getText():null);
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
    // parser/flatzinc/FlatzincFullExtWalker.g:344:1: many[ArrayList<Arc> in] returns [ArrayList<PropagationStrategy> pss, int depth] : ( ^(a= attribute (ca= comb_attr )? . ) | ^( EACH a= attribute (ca= comb_attr )? . . ) );
    public final FlatzincFullExtWalker.many_return many(ArrayList<Arc> in) throws RecognitionException {
        FlatzincFullExtWalker.many_return retval = new FlatzincFullExtWalker.many_return();
        retval.start = input.LT(1);


        Attribute a =null;

        CombinedAttribute ca =null;



            retval.pss = new ArrayList<PropagationStrategy>();

        try {
            // parser/flatzinc/FlatzincFullExtWalker.g:348:5: ( ^(a= attribute (ca= comb_attr )? . ) | ^( EACH a= attribute (ca= comb_attr )? . . ) )
            int alt22=2;
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
                alt22=1;
                }
                break;
            case EACH:
                {
                alt22=2;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 22, 0, input);

                throw nvae;

            }

            switch (alt22) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:349:5: ^(a= attribute (ca= comb_attr )? . )
                    {
                    pushFollow(FOLLOW_attribute_in_many874);
                    a=attribute();

                    state._fsp--;


                    match(input, Token.DOWN, null); 
                    // parser/flatzinc/FlatzincFullExtWalker.g:350:7: (ca= comb_attr )?
                    int alt20=2;
                    alt20 = dfa20.predict(input);
                    switch (alt20) {
                        case 1 :
                            // parser/flatzinc/FlatzincFullExtWalker.g:350:7: ca= comb_attr
                            {
                            pushFollow(FOLLOW_comb_attr_in_many882);
                            ca=comb_attr();

                            state._fsp--;


                            }
                            break;

                    }



                        int c_idx = input.mark();
                        

                    matchAny(input); 


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
                        

                    match(input, Token.UP, null); 


                    }
                    break;
                case 2 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:397:5: ^( EACH a= attribute (ca= comb_attr )? . . )
                    {
                    match(input,EACH,FOLLOW_EACH_in_many920); 

                    match(input, Token.DOWN, null); 
                    pushFollow(FOLLOW_attribute_in_many928);
                    a=attribute();

                    state._fsp--;


                    // parser/flatzinc/FlatzincFullExtWalker.g:399:7: (ca= comb_attr )?
                    int alt21=2;
                    alt21 = dfa21.predict(input);
                    switch (alt21) {
                        case 1 :
                            // parser/flatzinc/FlatzincFullExtWalker.g:399:7: ca= comb_attr
                            {
                            pushFollow(FOLLOW_comb_attr_in_many936);
                            ca=comb_attr();

                            state._fsp--;


                            }
                            break;

                    }



                        int m_idx = input.mark();
                        

                    matchAny(input); 


                        int c_idx = input.mark();
                        

                    matchAny(input); 


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
        return retval;
    }
    // $ANTLR end "many"



    // $ANTLR start "coll"
    // parser/flatzinc/FlatzincFullExtWalker.g:479:1: coll[ArrayList<? extends ISchedulable> elements, CombinedAttribute ca] returns [PropagationStrategy ps] : ( QUEUE it= qiter | (r= REV )? LIST it= liter | (m= ( MAX ) )? HEAP it= qiter );
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
            // parser/flatzinc/FlatzincFullExtWalker.g:489:5: ( QUEUE it= qiter | (r= REV )? LIST it= liter | (m= ( MAX ) )? HEAP it= qiter )
            int alt25=3;
            switch ( input.LA(1) ) {
            case QUEUE:
                {
                alt25=1;
                }
                break;
            case LIST:
            case REV:
                {
                alt25=2;
                }
                break;
            case HEAP:
            case MAX:
                {
                alt25=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 25, 0, input);

                throw nvae;

            }

            switch (alt25) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:489:7: QUEUE it= qiter
                    {
                    match(input,QUEUE,FOLLOW_QUEUE_in_coll1011); 

                    pushFollow(FOLLOW_qiter_in_coll1015);
                    it=qiter();

                    state._fsp--;



                        ps = new Queue(elements.toArray(new ISchedulable[elements.size()]));
                        ps = it.set(ps);
                        ps.attachEvaluator(ca);
                        

                    }
                    break;
                case 2 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:495:7: (r= REV )? LIST it= liter
                    {
                    // parser/flatzinc/FlatzincFullExtWalker.g:495:7: (r= REV )?
                    int alt23=2;
                    switch ( input.LA(1) ) {
                        case REV:
                            {
                            alt23=1;
                            }
                            break;
                    }

                    switch (alt23) {
                        case 1 :
                            // parser/flatzinc/FlatzincFullExtWalker.g:495:8: r= REV
                            {
                            r=(CommonTree)match(input,REV,FOLLOW_REV_in_coll1032); 

                            }
                            break;

                    }


                    match(input,LIST,FOLLOW_LIST_in_coll1036); 

                    pushFollow(FOLLOW_liter_in_coll1040);
                    it=liter();

                    state._fsp--;



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
                    // parser/flatzinc/FlatzincFullExtWalker.g:515:7: (m= ( MAX ) )? HEAP it= qiter
                    {
                    // parser/flatzinc/FlatzincFullExtWalker.g:515:8: (m= ( MAX ) )?
                    int alt24=2;
                    switch ( input.LA(1) ) {
                        case MAX:
                            {
                            alt24=1;
                            }
                            break;
                    }

                    switch (alt24) {
                        case 1 :
                            // parser/flatzinc/FlatzincFullExtWalker.g:515:8: m= ( MAX )
                            {
                            // parser/flatzinc/FlatzincFullExtWalker.g:515:9: ( MAX )
                            // parser/flatzinc/FlatzincFullExtWalker.g:515:10: MAX
                            {
                            match(input,MAX,FOLLOW_MAX_in_coll1057); 

                            }


                            }
                            break;

                    }


                    match(input,HEAP,FOLLOW_HEAP_in_coll1061); 

                    pushFollow(FOLLOW_qiter_in_coll1065);
                    it=qiter();

                    state._fsp--;



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
    // parser/flatzinc/FlatzincFullExtWalker.g:533:1: qiter returns [Iterator it] : ( ONE | WONE );
    public final Iterator qiter() throws RecognitionException {
        Iterator it = null;


        try {
            // parser/flatzinc/FlatzincFullExtWalker.g:534:5: ( ONE | WONE )
            int alt26=2;
            switch ( input.LA(1) ) {
            case ONE:
                {
                alt26=1;
                }
                break;
            case WONE:
                {
                alt26=2;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 26, 0, input);

                throw nvae;

            }

            switch (alt26) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:534:9: ONE
                    {
                    match(input,ONE,FOLLOW_ONE_in_qiter1091); 

                    it = Iterator.ONE;

                    }
                    break;
                case 2 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:535:9: WONE
                    {
                    match(input,WONE,FOLLOW_WONE_in_qiter1103); 

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
    // parser/flatzinc/FlatzincFullExtWalker.g:538:1: liter returns [Iterator it] : (q= qiter | FOR | WFOR );
    public final Iterator liter() throws RecognitionException {
        Iterator it = null;


        Iterator q =null;


        try {
            // parser/flatzinc/FlatzincFullExtWalker.g:539:5: (q= qiter | FOR | WFOR )
            int alt27=3;
            switch ( input.LA(1) ) {
            case ONE:
            case WONE:
                {
                alt27=1;
                }
                break;
            case FOR:
                {
                alt27=2;
                }
                break;
            case WFOR:
                {
                alt27=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 27, 0, input);

                throw nvae;

            }

            switch (alt27) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:539:9: q= qiter
                    {
                    pushFollow(FOLLOW_qiter_in_liter1135);
                    q=qiter();

                    state._fsp--;


                    it = q;

                    }
                    break;
                case 2 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:540:9: FOR
                    {
                    match(input,FOR,FOLLOW_FOR_in_liter1147); 

                    it = Iterator.FOR;

                    }
                    break;
                case 3 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:541:9: WFOR
                    {
                    match(input,WFOR,FOLLOW_WFOR_in_liter1159); 

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
    // parser/flatzinc/FlatzincFullExtWalker.g:546:1: comb_attr returns [CombinedAttribute ca] : ^( DO (ao= attr_op )* (ea= attribute )? ) ;
    public final CombinedAttribute comb_attr() throws RecognitionException {
        CombinedAttribute ca = null;


        AttributeOperator ao =null;

        Attribute ea =null;


        try {
            // parser/flatzinc/FlatzincFullExtWalker.g:547:2: ( ^( DO (ao= attr_op )* (ea= attribute )? ) )
            // parser/flatzinc/FlatzincFullExtWalker.g:548:2: ^( DO (ao= attr_op )* (ea= attribute )? )
            {

            	ArrayList<AttributeOperator> aos = new ArrayList<AttributeOperator>();
            	

            match(input,DO,FOLLOW_DO_in_comb_attr1186); 

            if ( input.LA(1)==Token.DOWN ) {
                match(input, Token.DOWN, null); 
                // parser/flatzinc/FlatzincFullExtWalker.g:551:7: (ao= attr_op )*
                loop28:
                do {
                    int alt28=2;
                    switch ( input.LA(1) ) {
                    case ANY:
                    case MAX:
                    case MIN:
                    case SIZE:
                    case SUM:
                        {
                        alt28=1;
                        }
                        break;

                    }

                    switch (alt28) {
                	case 1 :
                	    // parser/flatzinc/FlatzincFullExtWalker.g:551:8: ao= attr_op
                	    {
                	    pushFollow(FOLLOW_attr_op_in_comb_attr1191);
                	    ao=attr_op();

                	    state._fsp--;



                	    	aos.add(ao);
                	    	

                	    }
                	    break;

                	default :
                	    break loop28;
                    }
                } while (true);


                // parser/flatzinc/FlatzincFullExtWalker.g:555:7: (ea= attribute )?
                int alt29=2;
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
                        alt29=1;
                        }
                        break;
                }

                switch (alt29) {
                    case 1 :
                        // parser/flatzinc/FlatzincFullExtWalker.g:555:7: ea= attribute
                        {
                        pushFollow(FOLLOW_attribute_in_comb_attr1202);
                        ea=attribute();

                        state._fsp--;


                        }
                        break;

                }



                	if(ea == null && aos.isEmpty()){
                	    LOGGER.error("% Wrong key declaration");
                        throw new FZNException("Wrong key declaration");
                	}
                	ca = new CombinedAttribute(aos, ea);
                	

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
        return ca;
    }
    // $ANTLR end "comb_attr"



    // $ANTLR start "attr_op"
    // parser/flatzinc/FlatzincFullExtWalker.g:566:1: attr_op returns [AttributeOperator ao] : ( ANY | MIN | MAX | SUM | SIZE );
    public final AttributeOperator attr_op() throws RecognitionException {
        AttributeOperator ao = null;


        try {
            // parser/flatzinc/FlatzincFullExtWalker.g:567:5: ( ANY | MIN | MAX | SUM | SIZE )
            int alt30=5;
            switch ( input.LA(1) ) {
            case ANY:
                {
                alt30=1;
                }
                break;
            case MIN:
                {
                alt30=2;
                }
                break;
            case MAX:
                {
                alt30=3;
                }
                break;
            case SUM:
                {
                alt30=4;
                }
                break;
            case SIZE:
                {
                alt30=5;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 30, 0, input);

                throw nvae;

            }

            switch (alt30) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:567:7: ANY
                    {
                    match(input,ANY,FOLLOW_ANY_in_attr_op1226); 

                    ao = AttributeOperator.ANY;

                    }
                    break;
                case 2 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:568:9: MIN
                    {
                    match(input,MIN,FOLLOW_MIN_in_attr_op1238); 

                    ao = AttributeOperator.MIN;

                    }
                    break;
                case 3 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:569:9: MAX
                    {
                    match(input,MAX,FOLLOW_MAX_in_attr_op1250); 

                    ao = AttributeOperator.MAX;

                    }
                    break;
                case 4 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:570:9: SUM
                    {
                    match(input,SUM,FOLLOW_SUM_in_attr_op1262); 

                    ao = AttributeOperator.SUM;

                    }
                    break;
                case 5 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:571:9: SIZE
                    {
                    match(input,SIZE,FOLLOW_SIZE_in_attr_op1274); 

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
    // parser/flatzinc/FlatzincFullExtWalker.g:581:1: par_type returns [Declaration decl] : ( ^( ARRPAR (d= index_set )+ p= par_type_u ) | ^( APAR p= par_type_u ) );
    public final Declaration par_type() throws RecognitionException {
        Declaration decl = null;


        Declaration d =null;

        Declaration p =null;


        try {
            // parser/flatzinc/FlatzincFullExtWalker.g:582:5: ( ^( ARRPAR (d= index_set )+ p= par_type_u ) | ^( APAR p= par_type_u ) )
            int alt32=2;
            switch ( input.LA(1) ) {
            case ARRPAR:
                {
                alt32=1;
                }
                break;
            case APAR:
                {
                alt32=2;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 32, 0, input);

                throw nvae;

            }

            switch (alt32) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:583:5: ^( ARRPAR (d= index_set )+ p= par_type_u )
                    {

                            List<Declaration> decls = new ArrayList();
                        

                    match(input,ARRPAR,FOLLOW_ARRPAR_in_par_type1318); 

                    match(input, Token.DOWN, null); 
                    // parser/flatzinc/FlatzincFullExtWalker.g:586:18: (d= index_set )+
                    int cnt31=0;
                    loop31:
                    do {
                        int alt31=2;
                        switch ( input.LA(1) ) {
                        case INDEX:
                            {
                            alt31=1;
                            }
                            break;

                        }

                        switch (alt31) {
                    	case 1 :
                    	    // parser/flatzinc/FlatzincFullExtWalker.g:586:19: d= index_set
                    	    {
                    	    pushFollow(FOLLOW_index_set_in_par_type1323);
                    	    d=index_set();

                    	    state._fsp--;


                    	    decls.add(d);

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt31 >= 1 ) break loop31;
                                EarlyExitException eee =
                                    new EarlyExitException(31, input);
                                throw eee;
                        }
                        cnt31++;
                    } while (true);


                    pushFollow(FOLLOW_par_type_u_in_par_type1330);
                    p=par_type_u();

                    state._fsp--;


                    match(input, Token.UP, null); 



                        decl = new DArray(decls,p);
                        

                    }
                    break;
                case 2 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:590:9: ^( APAR p= par_type_u )
                    {
                    match(input,APAR,FOLLOW_APAR_in_par_type1348); 

                    match(input, Token.DOWN, null); 
                    pushFollow(FOLLOW_par_type_u_in_par_type1352);
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
    // parser/flatzinc/FlatzincFullExtWalker.g:596:1: par_type_u returns [Declaration decl] : ( BOOL | FLOAT | SET OF INT | INT );
    public final Declaration par_type_u() throws RecognitionException {
        Declaration decl = null;


        try {
            // parser/flatzinc/FlatzincFullExtWalker.g:597:5: ( BOOL | FLOAT | SET OF INT | INT )
            int alt33=4;
            switch ( input.LA(1) ) {
            case BOOL:
                {
                alt33=1;
                }
                break;
            case FLOAT:
                {
                alt33=2;
                }
                break;
            case SET:
                {
                alt33=3;
                }
                break;
            case INT:
                {
                alt33=4;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 33, 0, input);

                throw nvae;

            }

            switch (alt33) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:597:9: BOOL
                    {
                    match(input,BOOL,FOLLOW_BOOL_in_par_type_u1382); 


                        decl =DBool.me;
                        

                    }
                    break;
                case 2 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:601:9: FLOAT
                    {
                    match(input,FLOAT,FOLLOW_FLOAT_in_par_type_u1398); 


                        decl =DFloat.me;
                        

                    }
                    break;
                case 3 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:605:9: SET OF INT
                    {
                    match(input,SET,FOLLOW_SET_in_par_type_u1414); 

                    match(input,OF,FOLLOW_OF_in_par_type_u1416); 

                    match(input,INT,FOLLOW_INT_in_par_type_u1418); 


                        decl =DSetOfInt.me;
                        

                    }
                    break;
                case 4 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:609:9: INT
                    {
                    match(input,INT,FOLLOW_INT_in_par_type_u1434); 


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
    // parser/flatzinc/FlatzincFullExtWalker.g:615:1: var_type returns [Declaration decl] : ( ^( ARRVAR (d= index_set )+ d= var_type_u ) | ^( AVAR d= var_type_u ) );
    public final Declaration var_type() throws RecognitionException {
        Declaration decl = null;


        Declaration d =null;


        try {
            // parser/flatzinc/FlatzincFullExtWalker.g:616:5: ( ^( ARRVAR (d= index_set )+ d= var_type_u ) | ^( AVAR d= var_type_u ) )
            int alt35=2;
            switch ( input.LA(1) ) {
            case ARRVAR:
                {
                alt35=1;
                }
                break;
            case AVAR:
                {
                alt35=2;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 35, 0, input);

                throw nvae;

            }

            switch (alt35) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:617:5: ^( ARRVAR (d= index_set )+ d= var_type_u )
                    {

                        List<Declaration> decls = new ArrayList();
                        

                    match(input,ARRVAR,FOLLOW_ARRVAR_in_var_type1475); 

                    match(input, Token.DOWN, null); 
                    // parser/flatzinc/FlatzincFullExtWalker.g:620:14: (d= index_set )+
                    int cnt34=0;
                    loop34:
                    do {
                        int alt34=2;
                        switch ( input.LA(1) ) {
                        case INDEX:
                            {
                            alt34=1;
                            }
                            break;

                        }

                        switch (alt34) {
                    	case 1 :
                    	    // parser/flatzinc/FlatzincFullExtWalker.g:620:15: d= index_set
                    	    {
                    	    pushFollow(FOLLOW_index_set_in_var_type1480);
                    	    d=index_set();

                    	    state._fsp--;


                    	    decls.add(d);

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt34 >= 1 ) break loop34;
                                EarlyExitException eee =
                                    new EarlyExitException(34, input);
                                throw eee;
                        }
                        cnt34++;
                    } while (true);


                    pushFollow(FOLLOW_var_type_u_in_var_type1487);
                    d=var_type_u();

                    state._fsp--;


                    match(input, Token.UP, null); 



                        decl = new DArray(decls, d);
                        

                    }
                    break;
                case 2 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:624:9: ^( AVAR d= var_type_u )
                    {
                    match(input,AVAR,FOLLOW_AVAR_in_var_type1505); 

                    match(input, Token.DOWN, null); 
                    pushFollow(FOLLOW_var_type_u_in_var_type1509);
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
    // parser/flatzinc/FlatzincFullExtWalker.g:630:1: var_type_u returns [Declaration decl] : ( BOOL | FLOAT | INT | ^( DD i1= INT_CONST i2= INT_CONST ) | ^( CM (i= INT_CONST )+ ) | ^( SET ^( DD i1= INT_CONST i2= INT_CONST ) ) | ^( SET ^( CM (i= INT_CONST )+ ) ) );
    public final Declaration var_type_u() throws RecognitionException {
        Declaration decl = null;


        CommonTree i1=null;
        CommonTree i2=null;
        CommonTree i=null;

        try {
            // parser/flatzinc/FlatzincFullExtWalker.g:631:5: ( BOOL | FLOAT | INT | ^( DD i1= INT_CONST i2= INT_CONST ) | ^( CM (i= INT_CONST )+ ) | ^( SET ^( DD i1= INT_CONST i2= INT_CONST ) ) | ^( SET ^( CM (i= INT_CONST )+ ) ) )
            int alt38=7;
            switch ( input.LA(1) ) {
            case BOOL:
                {
                alt38=1;
                }
                break;
            case FLOAT:
                {
                alt38=2;
                }
                break;
            case INT:
                {
                alt38=3;
                }
                break;
            case DD:
                {
                alt38=4;
                }
                break;
            case CM:
                {
                alt38=5;
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
                        alt38=6;
                        }
                        break;
                    case CM:
                        {
                        alt38=7;
                        }
                        break;
                    default:
                        NoViableAltException nvae =
                            new NoViableAltException("", 38, 7, input);

                        throw nvae;

                    }

                    }
                    break;
                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 38, 6, input);

                    throw nvae;

                }

                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 38, 0, input);

                throw nvae;

            }

            switch (alt38) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:631:9: BOOL
                    {
                    match(input,BOOL,FOLLOW_BOOL_in_var_type_u1540); 


                        decl = DBool.me;
                        

                    }
                    break;
                case 2 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:635:9: FLOAT
                    {
                    match(input,FLOAT,FOLLOW_FLOAT_in_var_type_u1556); 


                        decl = DFloat.me;
                        

                    }
                    break;
                case 3 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:639:9: INT
                    {
                    match(input,INT,FOLLOW_INT_in_var_type_u1572); 


                        decl = DInt.me;
                        

                    }
                    break;
                case 4 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:643:9: ^( DD i1= INT_CONST i2= INT_CONST )
                    {
                    match(input,DD,FOLLOW_DD_in_var_type_u1589); 

                    match(input, Token.DOWN, null); 
                    i1=(CommonTree)match(input,INT_CONST,FOLLOW_INT_CONST_in_var_type_u1593); 

                    i2=(CommonTree)match(input,INT_CONST,FOLLOW_INT_CONST_in_var_type_u1597); 

                    match(input, Token.UP, null); 



                        decl = new DInt2(EInt.make((i1!=null?i1.getText():null)), EInt.make((i2!=null?i2.getText():null)));
                        

                    }
                    break;
                case 5 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:653:5: ^( CM (i= INT_CONST )+ )
                    {

                        ArrayList<EInt> values = new ArrayList();
                        

                    match(input,CM,FOLLOW_CM_in_var_type_u1632); 

                    match(input, Token.DOWN, null); 
                    // parser/flatzinc/FlatzincFullExtWalker.g:656:14: (i= INT_CONST )+
                    int cnt36=0;
                    loop36:
                    do {
                        int alt36=2;
                        switch ( input.LA(1) ) {
                        case INT_CONST:
                            {
                            alt36=1;
                            }
                            break;

                        }

                        switch (alt36) {
                    	case 1 :
                    	    // parser/flatzinc/FlatzincFullExtWalker.g:656:15: i= INT_CONST
                    	    {
                    	    i=(CommonTree)match(input,INT_CONST,FOLLOW_INT_CONST_in_var_type_u1637); 

                    	    values.add(EInt.make((i!=null?i.getText():null)));

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt36 >= 1 ) break loop36;
                                EarlyExitException eee =
                                    new EarlyExitException(36, input);
                                throw eee;
                        }
                        cnt36++;
                    } while (true);


                    match(input, Token.UP, null); 



                        decl = new DManyInt(values);
                        

                    }
                    break;
                case 6 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:660:9: ^( SET ^( DD i1= INT_CONST i2= INT_CONST ) )
                    {
                    match(input,SET,FOLLOW_SET_in_var_type_u1658); 

                    match(input, Token.DOWN, null); 
                    match(input,DD,FOLLOW_DD_in_var_type_u1661); 

                    match(input, Token.DOWN, null); 
                    i1=(CommonTree)match(input,INT_CONST,FOLLOW_INT_CONST_in_var_type_u1665); 

                    i2=(CommonTree)match(input,INT_CONST,FOLLOW_INT_CONST_in_var_type_u1669); 

                    match(input, Token.UP, null); 


                    match(input, Token.UP, null); 



                        decl = new DSet(new DInt2(EInt.make((i1!=null?i1.getText():null)), EInt.make((i2!=null?i2.getText():null))));
                        

                    }
                    break;
                case 7 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:665:5: ^( SET ^( CM (i= INT_CONST )+ ) )
                    {

                        ArrayList<EInt> values = new ArrayList();
                        

                    match(input,SET,FOLLOW_SET_in_var_type_u1699); 

                    match(input, Token.DOWN, null); 
                    match(input,CM,FOLLOW_CM_in_var_type_u1702); 

                    match(input, Token.DOWN, null); 
                    // parser/flatzinc/FlatzincFullExtWalker.g:668:19: (i= INT_CONST )+
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
                    	    // parser/flatzinc/FlatzincFullExtWalker.g:668:20: i= INT_CONST
                    	    {
                    	    i=(CommonTree)match(input,INT_CONST,FOLLOW_INT_CONST_in_var_type_u1707); 

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
    // parser/flatzinc/FlatzincFullExtWalker.g:674:1: index_set returns [Declaration decl] : ( ^( INDEX ^( DD i1= INT_CONST i2= INT_CONST ) ) | ^( INDEX INT ) );
    public final Declaration index_set() throws RecognitionException {
        Declaration decl = null;


        CommonTree i1=null;
        CommonTree i2=null;

        try {
            // parser/flatzinc/FlatzincFullExtWalker.g:675:5: ( ^( INDEX ^( DD i1= INT_CONST i2= INT_CONST ) ) | ^( INDEX INT ) )
            int alt39=2;
            switch ( input.LA(1) ) {
            case INDEX:
                {
                switch ( input.LA(2) ) {
                case DOWN:
                    {
                    switch ( input.LA(3) ) {
                    case DD:
                        {
                        alt39=1;
                        }
                        break;
                    case INT:
                        {
                        alt39=2;
                        }
                        break;
                    default:
                        NoViableAltException nvae =
                            new NoViableAltException("", 39, 2, input);

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
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 39, 0, input);

                throw nvae;

            }

            switch (alt39) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:675:9: ^( INDEX ^( DD i1= INT_CONST i2= INT_CONST ) )
                    {
                    match(input,INDEX,FOLLOW_INDEX_in_index_set1742); 

                    match(input, Token.DOWN, null); 
                    match(input,DD,FOLLOW_DD_in_index_set1745); 

                    match(input, Token.DOWN, null); 
                    i1=(CommonTree)match(input,INT_CONST,FOLLOW_INT_CONST_in_index_set1749); 

                    i2=(CommonTree)match(input,INT_CONST,FOLLOW_INT_CONST_in_index_set1753); 

                    match(input, Token.UP, null); 


                    match(input, Token.UP, null); 



                        decl = new DInt2(EInt.make((i1!=null?i1.getText():null)), EInt.make((i2!=null?i2.getText():null)));
                        

                    }
                    break;
                case 2 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:679:9: ^( INDEX INT )
                    {
                    match(input,INDEX,FOLLOW_INDEX_in_index_set1772); 

                    match(input, Token.DOWN, null); 
                    match(input,INT,FOLLOW_INT_in_index_set1774); 

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
    // parser/flatzinc/FlatzincFullExtWalker.g:685:1: expr returns [Expression exp] : ( LB (i= INT_CONST )+ RB |b= bool_const |i1= INT_CONST ( DD i2= INT_CONST )? | ^( EXPR LS (e= expr )* RS ) |e= id_expr | STRING );
    public final Expression expr() throws RecognitionException {
        Expression exp = null;


        CommonTree i=null;
        CommonTree i1=null;
        CommonTree i2=null;
        CommonTree STRING4=null;
        boolean b =false;

        Expression e =null;


        try {
            // parser/flatzinc/FlatzincFullExtWalker.g:686:5: ( LB (i= INT_CONST )+ RB |b= bool_const |i1= INT_CONST ( DD i2= INT_CONST )? | ^( EXPR LS (e= expr )* RS ) |e= id_expr | STRING )
            int alt43=6;
            switch ( input.LA(1) ) {
            case LB:
                {
                alt43=1;
                }
                break;
            case FALSE:
            case TRUE:
                {
                alt43=2;
                }
                break;
            case INT_CONST:
                {
                alt43=3;
                }
                break;
            case EXPR:
                {
                alt43=4;
                }
                break;
            case IDENTIFIER:
                {
                alt43=5;
                }
                break;
            case STRING:
                {
                alt43=6;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 43, 0, input);

                throw nvae;

            }

            switch (alt43) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:687:5: LB (i= INT_CONST )+ RB
                    {

                        ArrayList<EInt> values = new ArrayList();
                        

                    match(input,LB,FOLLOW_LB_in_expr1818); 

                    // parser/flatzinc/FlatzincFullExtWalker.g:690:12: (i= INT_CONST )+
                    int cnt40=0;
                    loop40:
                    do {
                        int alt40=2;
                        switch ( input.LA(1) ) {
                        case INT_CONST:
                            {
                            alt40=1;
                            }
                            break;

                        }

                        switch (alt40) {
                    	case 1 :
                    	    // parser/flatzinc/FlatzincFullExtWalker.g:690:13: i= INT_CONST
                    	    {
                    	    i=(CommonTree)match(input,INT_CONST,FOLLOW_INT_CONST_in_expr1823); 

                    	    values.add(EInt.make((i!=null?i.getText():null)));

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt40 >= 1 ) break loop40;
                                EarlyExitException eee =
                                    new EarlyExitException(40, input);
                                throw eee;
                        }
                        cnt40++;
                    } while (true);


                    match(input,RB,FOLLOW_RB_in_expr1828); 


                        exp = new ESetList(values);
                        

                    }
                    break;
                case 2 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:694:9: b= bool_const
                    {
                    pushFollow(FOLLOW_bool_const_in_expr1846);
                    b=bool_const();

                    state._fsp--;



                        exp =EBool.make(b);
                        

                    }
                    break;
                case 3 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:698:9: i1= INT_CONST ( DD i2= INT_CONST )?
                    {
                    i1=(CommonTree)match(input,INT_CONST,FOLLOW_INT_CONST_in_expr1864); 

                    // parser/flatzinc/FlatzincFullExtWalker.g:698:22: ( DD i2= INT_CONST )?
                    int alt41=2;
                    switch ( input.LA(1) ) {
                        case DD:
                            {
                            alt41=1;
                            }
                            break;
                    }

                    switch (alt41) {
                        case 1 :
                            // parser/flatzinc/FlatzincFullExtWalker.g:698:23: DD i2= INT_CONST
                            {
                            match(input,DD,FOLLOW_DD_in_expr1867); 

                            i2=(CommonTree)match(input,INT_CONST,FOLLOW_INT_CONST_in_expr1871); 

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
                    // parser/flatzinc/FlatzincFullExtWalker.g:707:5: ^( EXPR LS (e= expr )* RS )
                    {

                        ArrayList<Expression> exps = new ArrayList();
                        

                    match(input,EXPR,FOLLOW_EXPR_in_expr1901); 

                    match(input, Token.DOWN, null); 
                    match(input,LS,FOLLOW_LS_in_expr1903); 

                    // parser/flatzinc/FlatzincFullExtWalker.g:710:18: (e= expr )*
                    loop42:
                    do {
                        int alt42=2;
                        switch ( input.LA(1) ) {
                        case EXPR:
                        case FALSE:
                        case IDENTIFIER:
                        case INT_CONST:
                        case LB:
                        case STRING:
                        case TRUE:
                            {
                            alt42=1;
                            }
                            break;

                        }

                        switch (alt42) {
                    	case 1 :
                    	    // parser/flatzinc/FlatzincFullExtWalker.g:710:19: e= expr
                    	    {
                    	    pushFollow(FOLLOW_expr_in_expr1908);
                    	    e=expr();

                    	    state._fsp--;


                    	    exps.add(e);

                    	    }
                    	    break;

                    	default :
                    	    break loop42;
                        }
                    } while (true);


                    match(input,RS,FOLLOW_RS_in_expr1913); 

                    match(input, Token.UP, null); 



                        if(exps.size()>0){
                            exp = new EArray(exps);
                        }else{
                            exp = new EArray();
                        }
                        

                    }
                    break;
                case 5 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:718:9: e= id_expr
                    {
                    pushFollow(FOLLOW_id_expr_in_expr1932);
                    e=id_expr();

                    state._fsp--;



                        exp = e;
                        

                    }
                    break;
                case 6 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:722:9: STRING
                    {
                    STRING4=(CommonTree)match(input,STRING,FOLLOW_STRING_in_expr1948); 


                        exp = new EString((STRING4!=null?STRING4.getText():null));
                        

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
    // parser/flatzinc/FlatzincFullExtWalker.g:746:1: id_expr returns [Expression exp] : IDENTIFIER ( ( LP e= expr ( CM e= expr )* RP ) | ( LS i= INT_CONST RS ) )? ;
    public final Expression id_expr() throws RecognitionException {
        Expression exp = null;


        CommonTree i=null;
        CommonTree IDENTIFIER5=null;
        Expression e =null;


        try {
            // parser/flatzinc/FlatzincFullExtWalker.g:747:5: ( IDENTIFIER ( ( LP e= expr ( CM e= expr )* RP ) | ( LS i= INT_CONST RS ) )? )
            // parser/flatzinc/FlatzincFullExtWalker.g:748:5: IDENTIFIER ( ( LP e= expr ( CM e= expr )* RP ) | ( LS i= INT_CONST RS ) )?
            {

                ArrayList<Expression> exps = new ArrayList();
                

            IDENTIFIER5=(CommonTree)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_id_expr2006); 

            // parser/flatzinc/FlatzincFullExtWalker.g:751:19: ( ( LP e= expr ( CM e= expr )* RP ) | ( LS i= INT_CONST RS ) )?
            int alt45=3;
            switch ( input.LA(1) ) {
                case LP:
                    {
                    alt45=1;
                    }
                    break;
                case LS:
                    {
                    alt45=2;
                    }
                    break;
            }

            switch (alt45) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:751:20: ( LP e= expr ( CM e= expr )* RP )
                    {
                    // parser/flatzinc/FlatzincFullExtWalker.g:751:20: ( LP e= expr ( CM e= expr )* RP )
                    // parser/flatzinc/FlatzincFullExtWalker.g:751:21: LP e= expr ( CM e= expr )* RP
                    {
                    match(input,LP,FOLLOW_LP_in_id_expr2010); 

                    pushFollow(FOLLOW_expr_in_id_expr2014);
                    e=expr();

                    state._fsp--;


                    exps.add(e);

                    // parser/flatzinc/FlatzincFullExtWalker.g:751:45: ( CM e= expr )*
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
                    	    // parser/flatzinc/FlatzincFullExtWalker.g:751:46: CM e= expr
                    	    {
                    	    match(input,CM,FOLLOW_CM_in_id_expr2018); 

                    	    pushFollow(FOLLOW_expr_in_id_expr2022);
                    	    e=expr();

                    	    state._fsp--;


                    	    exps.add(e);

                    	    }
                    	    break;

                    	default :
                    	    break loop44;
                        }
                    } while (true);


                    match(input,RP,FOLLOW_RP_in_id_expr2027); 

                    }


                    }
                    break;
                case 2 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:751:76: ( LS i= INT_CONST RS )
                    {
                    // parser/flatzinc/FlatzincFullExtWalker.g:751:76: ( LS i= INT_CONST RS )
                    // parser/flatzinc/FlatzincFullExtWalker.g:751:77: LS i= INT_CONST RS
                    {
                    match(input,LS,FOLLOW_LS_in_id_expr2031); 

                    i=(CommonTree)match(input,INT_CONST,FOLLOW_INT_CONST_in_id_expr2035); 

                    match(input,RS,FOLLOW_RS_in_id_expr2037); 

                    }


                    }
                    break;

            }



                if(exps.size()>0){
                    exp = new EAnnotation(new EIdentifier(map, (IDENTIFIER5!=null?IDENTIFIER5.getText():null)), exps);
                }else if(i!=null) {
                    exp = new EIdArray(map, (IDENTIFIER5!=null?IDENTIFIER5.getText():null), Integer.parseInt((i!=null?i.getText():null)));
                }else{
                    exp = new EIdentifier(map, (IDENTIFIER5!=null?IDENTIFIER5.getText():null));
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
    // parser/flatzinc/FlatzincFullExtWalker.g:764:1: param_decl : ^( PAR IDENTIFIER pt= par_type e= expr ) ;
    public final void param_decl() throws RecognitionException {
        CommonTree IDENTIFIER6=null;
        Declaration pt =null;

        Expression e =null;


        try {
            // parser/flatzinc/FlatzincFullExtWalker.g:765:2: ( ^( PAR IDENTIFIER pt= par_type e= expr ) )
            // parser/flatzinc/FlatzincFullExtWalker.g:765:6: ^( PAR IDENTIFIER pt= par_type e= expr )
            {
            match(input,PAR,FOLLOW_PAR_in_param_decl2064); 

            match(input, Token.DOWN, null); 
            IDENTIFIER6=(CommonTree)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_param_decl2066); 

            pushFollow(FOLLOW_par_type_in_param_decl2070);
            pt=par_type();

            state._fsp--;


            pushFollow(FOLLOW_expr_in_param_decl2074);
            e=expr();

            state._fsp--;


            match(input, Token.UP, null); 



            	// Parameter(THashMap<String, Object> map, Declaration type, String identifier, Expression expression)
                FParameter.make_parameter(map, pt, (IDENTIFIER6!=null?IDENTIFIER6.getText():null), e);
                

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
    // parser/flatzinc/FlatzincFullExtWalker.g:773:1: var_decl : ^( VAR IDENTIFIER vt= var_type anns= annotations (e= expr )? ) ;
    public final void var_decl() throws RecognitionException {
        CommonTree IDENTIFIER7=null;
        Declaration vt =null;

        List<EAnnotation> anns =null;

        Expression e =null;


        try {
            // parser/flatzinc/FlatzincFullExtWalker.g:774:2: ( ^( VAR IDENTIFIER vt= var_type anns= annotations (e= expr )? ) )
            // parser/flatzinc/FlatzincFullExtWalker.g:774:6: ^( VAR IDENTIFIER vt= var_type anns= annotations (e= expr )? )
            {
            match(input,VAR,FOLLOW_VAR_in_var_decl2093); 

            match(input, Token.DOWN, null); 
            IDENTIFIER7=(CommonTree)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_var_decl2095); 

            pushFollow(FOLLOW_var_type_in_var_decl2099);
            vt=var_type();

            state._fsp--;


            pushFollow(FOLLOW_annotations_in_var_decl2103);
            anns=annotations();

            state._fsp--;


            // parser/flatzinc/FlatzincFullExtWalker.g:774:53: (e= expr )?
            int alt46=2;
            switch ( input.LA(1) ) {
                case EXPR:
                case FALSE:
                case IDENTIFIER:
                case INT_CONST:
                case LB:
                case STRING:
                case TRUE:
                    {
                    alt46=1;
                    }
                    break;
            }

            switch (alt46) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:774:53: e= expr
                    {
                    pushFollow(FOLLOW_expr_in_var_decl2107);
                    e=expr();

                    state._fsp--;


                    }
                    break;

            }


            match(input, Token.UP, null); 



            	FVariable.make_variable(map, vt, (IDENTIFIER7!=null?IDENTIFIER7.getText():null), anns, e, mSolver, mLayout);
            	

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
    // parser/flatzinc/FlatzincFullExtWalker.g:780:1: constraint : ^( CONSTRAINT IDENTIFIER (e= expr )+ anns= annotations ) ;
    public final void constraint() throws RecognitionException {
        CommonTree IDENTIFIER8=null;
        Expression e =null;

        List<EAnnotation> anns =null;


        try {
            // parser/flatzinc/FlatzincFullExtWalker.g:781:2: ( ^( CONSTRAINT IDENTIFIER (e= expr )+ anns= annotations ) )
            // parser/flatzinc/FlatzincFullExtWalker.g:782:2: ^( CONSTRAINT IDENTIFIER (e= expr )+ anns= annotations )
            {

            	//  Solver aSolver, String id, List<Expression> exps, List<EAnnotation> annotations
            	ArrayList<Expression> exps = new ArrayList();
            	

            match(input,CONSTRAINT,FOLLOW_CONSTRAINT_in_constraint2132); 

            match(input, Token.DOWN, null); 
            IDENTIFIER8=(CommonTree)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_constraint2134); 

            // parser/flatzinc/FlatzincFullExtWalker.g:786:30: (e= expr )+
            int cnt47=0;
            loop47:
            do {
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
            	    // parser/flatzinc/FlatzincFullExtWalker.g:786:31: e= expr
            	    {
            	    pushFollow(FOLLOW_expr_in_constraint2139);
            	    e=expr();

            	    state._fsp--;


            	    exps.add(e);

            	    }
            	    break;

            	default :
            	    if ( cnt47 >= 1 ) break loop47;
                        EarlyExitException eee =
                            new EarlyExitException(47, input);
                        throw eee;
                }
                cnt47++;
            } while (true);


            pushFollow(FOLLOW_annotations_in_constraint2146);
            anns=annotations();

            state._fsp--;


            match(input, Token.UP, null); 



            	String id = (IDENTIFIER8!=null?IDENTIFIER8.getText():null);
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
    // parser/flatzinc/FlatzincFullExtWalker.g:793:1: solve_goal : ^( SOLVE anns= annotations res= resolution[type,expr] ) ;
    public final void solve_goal() throws RecognitionException {
        List<EAnnotation> anns =null;


        try {
            // parser/flatzinc/FlatzincFullExtWalker.g:794:2: ( ^( SOLVE anns= annotations res= resolution[type,expr] ) )
            // parser/flatzinc/FlatzincFullExtWalker.g:795:2: ^( SOLVE anns= annotations res= resolution[type,expr] )
            {

            	ResolutionPolicy type = ResolutionPolicy.SATISFACTION;
            	Expression expr = null;
            	

            match(input,SOLVE,FOLLOW_SOLVE_in_solve_goal2166); 

            match(input, Token.DOWN, null); 
            pushFollow(FOLLOW_annotations_in_solve_goal2170);
            anns=annotations();

            state._fsp--;


            pushFollow(FOLLOW_resolution_in_solve_goal2174);
            resolution(type, expr);

            state._fsp--;


            match(input, Token.UP, null); 



                FGoal.define_goal(free, all, mSolver,anns,type,expr);
            	

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
    // parser/flatzinc/FlatzincFullExtWalker.g:805:1: resolution[ResolutionPolicy type, Expression expr] : ( SATISFY | ^( MINIMIZE e= expr ) | ^( MAXIMIZE e= expr ) );
    public final void resolution(ResolutionPolicy type, Expression expr) throws RecognitionException {
        Expression e =null;


        try {
            // parser/flatzinc/FlatzincFullExtWalker.g:806:5: ( SATISFY | ^( MINIMIZE e= expr ) | ^( MAXIMIZE e= expr ) )
            int alt48=3;
            switch ( input.LA(1) ) {
            case SATISFY:
                {
                alt48=1;
                }
                break;
            case MINIMIZE:
                {
                alt48=2;
                }
                break;
            case MAXIMIZE:
                {
                alt48=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 48, 0, input);

                throw nvae;

            }

            switch (alt48) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:806:9: SATISFY
                    {
                    match(input,SATISFY,FOLLOW_SATISFY_in_resolution2198); 

                    }
                    break;
                case 2 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:807:9: ^( MINIMIZE e= expr )
                    {
                    match(input,MINIMIZE,FOLLOW_MINIMIZE_in_resolution2209); 

                    match(input, Token.DOWN, null); 
                    pushFollow(FOLLOW_expr_in_resolution2213);
                    e=expr();

                    state._fsp--;


                    match(input, Token.UP, null); 



                        type =ResolutionPolicy.MINIMIZE;
                        expr =e;
                        

                    }
                    break;
                case 3 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:812:9: ^( MAXIMIZE e= expr )
                    {
                    match(input,MAXIMIZE,FOLLOW_MAXIMIZE_in_resolution2231); 

                    match(input, Token.DOWN, null); 
                    pushFollow(FOLLOW_expr_in_resolution2235);
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
    // parser/flatzinc/FlatzincFullExtWalker.g:819:1: annotations returns [List<EAnnotation> anns] : ^( ANNOTATIONS (e= annotation )* ) ;
    public final List<EAnnotation> annotations() throws RecognitionException {
        List<EAnnotation> anns = null;


        EAnnotation e =null;


        try {
            // parser/flatzinc/FlatzincFullExtWalker.g:820:5: ( ^( ANNOTATIONS (e= annotation )* ) )
            // parser/flatzinc/FlatzincFullExtWalker.g:821:5: ^( ANNOTATIONS (e= annotation )* )
            {

                anns = new ArrayList();
                

            match(input,ANNOTATIONS,FOLLOW_ANNOTATIONS_in_annotations2278); 

            if ( input.LA(1)==Token.DOWN ) {
                match(input, Token.DOWN, null); 
                // parser/flatzinc/FlatzincFullExtWalker.g:824:23: (e= annotation )*
                loop49:
                do {
                    int alt49=2;
                    switch ( input.LA(1) ) {
                    case IDENTIFIER:
                        {
                        alt49=1;
                        }
                        break;

                    }

                    switch (alt49) {
                	case 1 :
                	    // parser/flatzinc/FlatzincFullExtWalker.g:824:24: e= annotation
                	    {
                	    pushFollow(FOLLOW_annotation_in_annotations2283);
                	    e=annotation();

                	    state._fsp--;


                	    anns.add(e);

                	    }
                	    break;

                	default :
                	    break loop49;
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
    // parser/flatzinc/FlatzincFullExtWalker.g:827:1: annotation returns [EAnnotation ann] : IDENTIFIER ( LP (e= expr )+ RP )? ;
    public final EAnnotation annotation() throws RecognitionException {
        EAnnotation ann = null;


        CommonTree IDENTIFIER9=null;
        Expression e =null;


        try {
            // parser/flatzinc/FlatzincFullExtWalker.g:828:5: ( IDENTIFIER ( LP (e= expr )+ RP )? )
            // parser/flatzinc/FlatzincFullExtWalker.g:829:5: IDENTIFIER ( LP (e= expr )+ RP )?
            {

                ArrayList<Expression> exps = new ArrayList();
                

            IDENTIFIER9=(CommonTree)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_annotation2320); 

            // parser/flatzinc/FlatzincFullExtWalker.g:832:16: ( LP (e= expr )+ RP )?
            int alt51=2;
            switch ( input.LA(1) ) {
                case LP:
                    {
                    alt51=1;
                    }
                    break;
            }

            switch (alt51) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:832:17: LP (e= expr )+ RP
                    {
                    match(input,LP,FOLLOW_LP_in_annotation2323); 

                    // parser/flatzinc/FlatzincFullExtWalker.g:832:20: (e= expr )+
                    int cnt50=0;
                    loop50:
                    do {
                        int alt50=2;
                        switch ( input.LA(1) ) {
                        case EXPR:
                        case FALSE:
                        case IDENTIFIER:
                        case INT_CONST:
                        case LB:
                        case STRING:
                        case TRUE:
                            {
                            alt50=1;
                            }
                            break;

                        }

                        switch (alt50) {
                    	case 1 :
                    	    // parser/flatzinc/FlatzincFullExtWalker.g:832:21: e= expr
                    	    {
                    	    pushFollow(FOLLOW_expr_in_annotation2328);
                    	    e=expr();

                    	    state._fsp--;


                    	    exps.add(e);

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt50 >= 1 ) break loop50;
                                EarlyExitException eee =
                                    new EarlyExitException(50, input);
                                throw eee;
                        }
                        cnt50++;
                    } while (true);


                    match(input,RP,FOLLOW_RP_in_annotation2333); 

                    }
                    break;

            }



                ann = new EAnnotation(new EIdentifier(map,(IDENTIFIER9!=null?IDENTIFIER9.getText():null)), exps);
                

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
    // parser/flatzinc/FlatzincFullExtWalker.g:838:1: bool_const returns [boolean value] : ( TRUE | FALSE );
    public final boolean bool_const() throws RecognitionException {
        boolean value = false;


        try {
            // parser/flatzinc/FlatzincFullExtWalker.g:839:5: ( TRUE | FALSE )
            int alt52=2;
            switch ( input.LA(1) ) {
            case TRUE:
                {
                alt52=1;
                }
                break;
            case FALSE:
                {
                alt52=2;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 52, 0, input);

                throw nvae;

            }

            switch (alt52) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:839:9: TRUE
                    {
                    match(input,TRUE,FOLLOW_TRUE_in_bool_const2365); 

                    value = true;

                    }
                    break;
                case 2 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:840:9: FALSE
                    {
                    match(input,FALSE,FOLLOW_FALSE_in_bool_const2377); 

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
    // parser/flatzinc/FlatzincFullExtWalker.g:844:1: pred_decl : ^( PREDICATE IDENTIFIER ( pred_param )+ ) ;
    public final void pred_decl() throws RecognitionException {
        CommonTree IDENTIFIER10=null;

        try {
            // parser/flatzinc/FlatzincFullExtWalker.g:845:2: ( ^( PREDICATE IDENTIFIER ( pred_param )+ ) )
            // parser/flatzinc/FlatzincFullExtWalker.g:845:6: ^( PREDICATE IDENTIFIER ( pred_param )+ )
            {
            match(input,PREDICATE,FOLLOW_PREDICATE_in_pred_decl2396); 

            match(input, Token.DOWN, null); 
            IDENTIFIER10=(CommonTree)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_pred_decl2398); 

            // parser/flatzinc/FlatzincFullExtWalker.g:845:29: ( pred_param )+
            int cnt53=0;
            loop53:
            do {
                int alt53=2;
                switch ( input.LA(1) ) {
                case CL:
                    {
                    alt53=1;
                    }
                    break;

                }

                switch (alt53) {
            	case 1 :
            	    // parser/flatzinc/FlatzincFullExtWalker.g:845:29: pred_param
            	    {
            	    pushFollow(FOLLOW_pred_param_in_pred_decl2400);
            	    pred_param();

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    if ( cnt53 >= 1 ) break loop53;
                        EarlyExitException eee =
                            new EarlyExitException(53, input);
                        throw eee;
                }
                cnt53++;
            } while (true);


            match(input, Token.UP, null); 



            //        LOGGER.info("% skip predicate : "+ (IDENTIFIER10!=null?IDENTIFIER10.getText():null));
            	

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
    // parser/flatzinc/FlatzincFullExtWalker.g:852:1: pred_param : ^( CL pred_param_type IDENTIFIER ) ;
    public final void pred_param() throws RecognitionException {
        try {
            // parser/flatzinc/FlatzincFullExtWalker.g:853:5: ( ^( CL pred_param_type IDENTIFIER ) )
            // parser/flatzinc/FlatzincFullExtWalker.g:853:9: ^( CL pred_param_type IDENTIFIER )
            {
            match(input,CL,FOLLOW_CL_in_pred_param2423); 

            match(input, Token.DOWN, null); 
            pushFollow(FOLLOW_pred_param_type_in_pred_param2425);
            pred_param_type();

            state._fsp--;


            match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_pred_param2427); 

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
    // parser/flatzinc/FlatzincFullExtWalker.g:857:1: pred_param_type : ( par_pred_param_type | var_pred_param_type );
    public final void pred_param_type() throws RecognitionException {
        try {
            // parser/flatzinc/FlatzincFullExtWalker.g:858:5: ( par_pred_param_type | var_pred_param_type )
            int alt54=2;
            alt54 = dfa54.predict(input);
            switch (alt54) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:858:9: par_pred_param_type
                    {
                    pushFollow(FOLLOW_par_pred_param_type_in_pred_param_type2448);
                    par_pred_param_type();

                    state._fsp--;


                    }
                    break;
                case 2 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:859:9: var_pred_param_type
                    {
                    pushFollow(FOLLOW_var_pred_param_type_in_pred_param_type2458);
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
    // parser/flatzinc/FlatzincFullExtWalker.g:863:1: par_pred_param_type : ( par_type | ^( DD INT_CONST INT_CONST ) | ^( CM ( INT_CONST )+ ) | ^( SET ^( DD INT_CONST INT_CONST ) ) | ^( SET ^( CM ( INT_CONST )+ ) ) | ^( ARRAY ( index_set )+ ^( DD INT_CONST INT_CONST ) ) | ^( ARRAY ( index_set )+ ^( CM ( INT_CONST )+ ) ) | ^( ARRAY ( index_set )+ ^( SET ^( DD INT_CONST INT_CONST ) ) ) | ^( ARRAY ( index_set )+ ^( SET ^( CM ( INT_CONST )+ ) ) ) );
    public final void par_pred_param_type() throws RecognitionException {
        try {
            // parser/flatzinc/FlatzincFullExtWalker.g:864:5: ( par_type | ^( DD INT_CONST INT_CONST ) | ^( CM ( INT_CONST )+ ) | ^( SET ^( DD INT_CONST INT_CONST ) ) | ^( SET ^( CM ( INT_CONST )+ ) ) | ^( ARRAY ( index_set )+ ^( DD INT_CONST INT_CONST ) ) | ^( ARRAY ( index_set )+ ^( CM ( INT_CONST )+ ) ) | ^( ARRAY ( index_set )+ ^( SET ^( DD INT_CONST INT_CONST ) ) ) | ^( ARRAY ( index_set )+ ^( SET ^( CM ( INT_CONST )+ ) ) ) )
            int alt63=9;
            alt63 = dfa63.predict(input);
            switch (alt63) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:864:9: par_type
                    {
                    pushFollow(FOLLOW_par_type_in_par_pred_param_type2478);
                    par_type();

                    state._fsp--;


                    }
                    break;
                case 2 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:866:9: ^( DD INT_CONST INT_CONST )
                    {
                    match(input,DD,FOLLOW_DD_in_par_pred_param_type2490); 

                    match(input, Token.DOWN, null); 
                    match(input,INT_CONST,FOLLOW_INT_CONST_in_par_pred_param_type2492); 

                    match(input,INT_CONST,FOLLOW_INT_CONST_in_par_pred_param_type2494); 

                    match(input, Token.UP, null); 


                    }
                    break;
                case 3 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:867:9: ^( CM ( INT_CONST )+ )
                    {
                    match(input,CM,FOLLOW_CM_in_par_pred_param_type2506); 

                    match(input, Token.DOWN, null); 
                    // parser/flatzinc/FlatzincFullExtWalker.g:867:14: ( INT_CONST )+
                    int cnt55=0;
                    loop55:
                    do {
                        int alt55=2;
                        switch ( input.LA(1) ) {
                        case INT_CONST:
                            {
                            alt55=1;
                            }
                            break;

                        }

                        switch (alt55) {
                    	case 1 :
                    	    // parser/flatzinc/FlatzincFullExtWalker.g:867:14: INT_CONST
                    	    {
                    	    match(input,INT_CONST,FOLLOW_INT_CONST_in_par_pred_param_type2508); 

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt55 >= 1 ) break loop55;
                                EarlyExitException eee =
                                    new EarlyExitException(55, input);
                                throw eee;
                        }
                        cnt55++;
                    } while (true);


                    match(input, Token.UP, null); 


                    }
                    break;
                case 4 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:868:9: ^( SET ^( DD INT_CONST INT_CONST ) )
                    {
                    match(input,SET,FOLLOW_SET_in_par_pred_param_type2521); 

                    match(input, Token.DOWN, null); 
                    match(input,DD,FOLLOW_DD_in_par_pred_param_type2524); 

                    match(input, Token.DOWN, null); 
                    match(input,INT_CONST,FOLLOW_INT_CONST_in_par_pred_param_type2526); 

                    match(input,INT_CONST,FOLLOW_INT_CONST_in_par_pred_param_type2528); 

                    match(input, Token.UP, null); 


                    match(input, Token.UP, null); 


                    }
                    break;
                case 5 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:869:9: ^( SET ^( CM ( INT_CONST )+ ) )
                    {
                    match(input,SET,FOLLOW_SET_in_par_pred_param_type2541); 

                    match(input, Token.DOWN, null); 
                    match(input,CM,FOLLOW_CM_in_par_pred_param_type2544); 

                    match(input, Token.DOWN, null); 
                    // parser/flatzinc/FlatzincFullExtWalker.g:869:20: ( INT_CONST )+
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
                    	    // parser/flatzinc/FlatzincFullExtWalker.g:869:20: INT_CONST
                    	    {
                    	    match(input,INT_CONST,FOLLOW_INT_CONST_in_par_pred_param_type2546); 

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


                    match(input, Token.UP, null); 


                    }
                    break;
                case 6 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:871:9: ^( ARRAY ( index_set )+ ^( DD INT_CONST INT_CONST ) )
                    {
                    match(input,ARRAY,FOLLOW_ARRAY_in_par_pred_param_type2561); 

                    match(input, Token.DOWN, null); 
                    // parser/flatzinc/FlatzincFullExtWalker.g:871:17: ( index_set )+
                    int cnt57=0;
                    loop57:
                    do {
                        int alt57=2;
                        switch ( input.LA(1) ) {
                        case INDEX:
                            {
                            alt57=1;
                            }
                            break;

                        }

                        switch (alt57) {
                    	case 1 :
                    	    // parser/flatzinc/FlatzincFullExtWalker.g:871:17: index_set
                    	    {
                    	    pushFollow(FOLLOW_index_set_in_par_pred_param_type2563);
                    	    index_set();

                    	    state._fsp--;


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


                    match(input,DD,FOLLOW_DD_in_par_pred_param_type2567); 

                    match(input, Token.DOWN, null); 
                    match(input,INT_CONST,FOLLOW_INT_CONST_in_par_pred_param_type2569); 

                    match(input,INT_CONST,FOLLOW_INT_CONST_in_par_pred_param_type2571); 

                    match(input, Token.UP, null); 


                    match(input, Token.UP, null); 


                    }
                    break;
                case 7 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:872:9: ^( ARRAY ( index_set )+ ^( CM ( INT_CONST )+ ) )
                    {
                    match(input,ARRAY,FOLLOW_ARRAY_in_par_pred_param_type2584); 

                    match(input, Token.DOWN, null); 
                    // parser/flatzinc/FlatzincFullExtWalker.g:872:17: ( index_set )+
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
                    	    // parser/flatzinc/FlatzincFullExtWalker.g:872:17: index_set
                    	    {
                    	    pushFollow(FOLLOW_index_set_in_par_pred_param_type2586);
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


                    match(input,CM,FOLLOW_CM_in_par_pred_param_type2590); 

                    match(input, Token.DOWN, null); 
                    // parser/flatzinc/FlatzincFullExtWalker.g:872:33: ( INT_CONST )+
                    int cnt59=0;
                    loop59:
                    do {
                        int alt59=2;
                        switch ( input.LA(1) ) {
                        case INT_CONST:
                            {
                            alt59=1;
                            }
                            break;

                        }

                        switch (alt59) {
                    	case 1 :
                    	    // parser/flatzinc/FlatzincFullExtWalker.g:872:33: INT_CONST
                    	    {
                    	    match(input,INT_CONST,FOLLOW_INT_CONST_in_par_pred_param_type2592); 

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


                    match(input, Token.UP, null); 


                    match(input, Token.UP, null); 


                    }
                    break;
                case 8 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:873:9: ^( ARRAY ( index_set )+ ^( SET ^( DD INT_CONST INT_CONST ) ) )
                    {
                    match(input,ARRAY,FOLLOW_ARRAY_in_par_pred_param_type2606); 

                    match(input, Token.DOWN, null); 
                    // parser/flatzinc/FlatzincFullExtWalker.g:873:17: ( index_set )+
                    int cnt60=0;
                    loop60:
                    do {
                        int alt60=2;
                        switch ( input.LA(1) ) {
                        case INDEX:
                            {
                            alt60=1;
                            }
                            break;

                        }

                        switch (alt60) {
                    	case 1 :
                    	    // parser/flatzinc/FlatzincFullExtWalker.g:873:17: index_set
                    	    {
                    	    pushFollow(FOLLOW_index_set_in_par_pred_param_type2608);
                    	    index_set();

                    	    state._fsp--;


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


                    match(input,SET,FOLLOW_SET_in_par_pred_param_type2612); 

                    match(input, Token.DOWN, null); 
                    match(input,DD,FOLLOW_DD_in_par_pred_param_type2615); 

                    match(input, Token.DOWN, null); 
                    match(input,INT_CONST,FOLLOW_INT_CONST_in_par_pred_param_type2617); 

                    match(input,INT_CONST,FOLLOW_INT_CONST_in_par_pred_param_type2619); 

                    match(input, Token.UP, null); 


                    match(input, Token.UP, null); 


                    match(input, Token.UP, null); 


                    }
                    break;
                case 9 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:874:9: ^( ARRAY ( index_set )+ ^( SET ^( CM ( INT_CONST )+ ) ) )
                    {
                    match(input,ARRAY,FOLLOW_ARRAY_in_par_pred_param_type2633); 

                    match(input, Token.DOWN, null); 
                    // parser/flatzinc/FlatzincFullExtWalker.g:874:17: ( index_set )+
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
                    	    // parser/flatzinc/FlatzincFullExtWalker.g:874:17: index_set
                    	    {
                    	    pushFollow(FOLLOW_index_set_in_par_pred_param_type2635);
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


                    match(input,SET,FOLLOW_SET_in_par_pred_param_type2639); 

                    match(input, Token.DOWN, null); 
                    match(input,CM,FOLLOW_CM_in_par_pred_param_type2642); 

                    match(input, Token.DOWN, null); 
                    // parser/flatzinc/FlatzincFullExtWalker.g:874:39: ( INT_CONST )+
                    int cnt62=0;
                    loop62:
                    do {
                        int alt62=2;
                        switch ( input.LA(1) ) {
                        case INT_CONST:
                            {
                            alt62=1;
                            }
                            break;

                        }

                        switch (alt62) {
                    	case 1 :
                    	    // parser/flatzinc/FlatzincFullExtWalker.g:874:39: INT_CONST
                    	    {
                    	    match(input,INT_CONST,FOLLOW_INT_CONST_in_par_pred_param_type2644); 

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
    // parser/flatzinc/FlatzincFullExtWalker.g:878:1: var_pred_param_type : ( ^( VAR var_type ) | ^( VAR SET ) | ^( ARRAY ( index_set )+ ^( VAR SET ) ) );
    public final void var_pred_param_type() throws RecognitionException {
        try {
            // parser/flatzinc/FlatzincFullExtWalker.g:879:5: ( ^( VAR var_type ) | ^( VAR SET ) | ^( ARRAY ( index_set )+ ^( VAR SET ) ) )
            int alt65=3;
            switch ( input.LA(1) ) {
            case VAR:
                {
                switch ( input.LA(2) ) {
                case DOWN:
                    {
                    switch ( input.LA(3) ) {
                    case SET:
                        {
                        alt65=2;
                        }
                        break;
                    case ARRVAR:
                    case AVAR:
                        {
                        alt65=1;
                        }
                        break;
                    default:
                        NoViableAltException nvae =
                            new NoViableAltException("", 65, 3, input);

                        throw nvae;

                    }

                    }
                    break;
                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 65, 1, input);

                    throw nvae;

                }

                }
                break;
            case ARRAY:
                {
                alt65=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 65, 0, input);

                throw nvae;

            }

            switch (alt65) {
                case 1 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:879:9: ^( VAR var_type )
                    {
                    match(input,VAR,FOLLOW_VAR_in_var_pred_param_type2669); 

                    match(input, Token.DOWN, null); 
                    pushFollow(FOLLOW_var_type_in_var_pred_param_type2671);
                    var_type();

                    state._fsp--;


                    match(input, Token.UP, null); 


                    }
                    break;
                case 2 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:880:9: ^( VAR SET )
                    {
                    match(input,VAR,FOLLOW_VAR_in_var_pred_param_type2683); 

                    match(input, Token.DOWN, null); 
                    match(input,SET,FOLLOW_SET_in_var_pred_param_type2685); 

                    match(input, Token.UP, null); 


                    }
                    break;
                case 3 :
                    // parser/flatzinc/FlatzincFullExtWalker.g:881:9: ^( ARRAY ( index_set )+ ^( VAR SET ) )
                    {
                    match(input,ARRAY,FOLLOW_ARRAY_in_var_pred_param_type2697); 

                    match(input, Token.DOWN, null); 
                    // parser/flatzinc/FlatzincFullExtWalker.g:881:17: ( index_set )+
                    int cnt64=0;
                    loop64:
                    do {
                        int alt64=2;
                        switch ( input.LA(1) ) {
                        case INDEX:
                            {
                            alt64=1;
                            }
                            break;

                        }

                        switch (alt64) {
                    	case 1 :
                    	    // parser/flatzinc/FlatzincFullExtWalker.g:881:17: index_set
                    	    {
                    	    pushFollow(FOLLOW_index_set_in_var_pred_param_type2699);
                    	    index_set();

                    	    state._fsp--;


                    	    }
                    	    break;

                    	default :
                    	    if ( cnt64 >= 1 ) break loop64;
                                EarlyExitException eee =
                                    new EarlyExitException(64, input);
                                throw eee;
                        }
                        cnt64++;
                    } while (true);


                    match(input,VAR,FOLLOW_VAR_in_var_pred_param_type2703); 

                    match(input, Token.DOWN, null); 
                    match(input,SET,FOLLOW_SET_in_var_pred_param_type2705); 

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


    protected DFA20 dfa20 = new DFA20(this);
    protected DFA21 dfa21 = new DFA21(this);
    protected DFA54 dfa54 = new DFA54(this);
    protected DFA63 dfa63 = new DFA63(this);
    static final String DFA20_eotS =
        "\25\uffff";
    static final String DFA20_eofS =
        "\25\uffff";
    static final String DFA20_minS =
        "\1\4\1\2\1\uffff\20\3\1\uffff\1\3";
    static final String DFA20_maxS =
        "\1\132\1\3\1\uffff\20\132\1\uffff\1\132";
    static final String DFA20_acceptS =
        "\2\uffff\1\2\20\uffff\1\1\1\uffff";
    static final String DFA20_specialS =
        "\25\uffff}>";
    static final String[] DFA20_transitionS = {
            "\24\2\1\1\102\2",
            "\1\3\1\2",
            "",
            "\1\23\2\2\1\4\7\2\1\17\3\2\1\16\2\2\1\12\27\2\1\6\1\2\1\5\17"+
            "\2\1\21\1\2\1\20\1\22\1\2\1\13\10\2\1\10\4\2\1\7\2\2\1\11\1"+
            "\15\1\14\3\2",
            "\1\24\2\2\1\4\7\2\1\17\3\2\1\16\2\2\1\12\27\2\1\6\1\2\1\5\17"+
            "\2\1\21\1\2\1\20\1\22\1\2\1\13\10\2\1\10\4\2\1\7\2\2\1\11\1"+
            "\15\1\14\3\2",
            "\1\24\2\2\1\4\7\2\1\17\3\2\1\16\2\2\1\12\27\2\1\6\1\2\1\5\17"+
            "\2\1\21\1\2\1\20\1\22\1\2\1\13\10\2\1\10\4\2\1\7\2\2\1\11\1"+
            "\15\1\14\3\2",
            "\1\24\2\2\1\4\7\2\1\17\3\2\1\16\2\2\1\12\27\2\1\6\1\2\1\5\17"+
            "\2\1\21\1\2\1\20\1\22\1\2\1\13\10\2\1\10\4\2\1\7\2\2\1\11\1"+
            "\15\1\14\3\2",
            "\1\24\2\2\1\4\7\2\1\17\3\2\1\16\2\2\1\12\27\2\1\6\1\2\1\5\17"+
            "\2\1\21\1\2\1\20\1\22\1\2\1\13\10\2\1\10\4\2\1\7\2\2\1\11\1"+
            "\15\1\14\3\2",
            "\1\24\2\2\1\4\7\2\1\17\3\2\1\16\2\2\1\12\27\2\1\6\1\2\1\5\17"+
            "\2\1\21\1\2\1\20\1\22\1\2\1\13\10\2\1\10\4\2\1\7\2\2\1\11\1"+
            "\15\1\14\3\2",
            "\1\24\127\2",
            "\1\24\127\2",
            "\1\24\127\2",
            "\1\24\127\2",
            "\1\24\127\2",
            "\1\24\127\2",
            "\1\24\127\2",
            "\1\24\127\2",
            "\1\24\127\2",
            "\1\24\127\2",
            "",
            "\1\2\127\23"
    };

    static final short[] DFA20_eot = DFA.unpackEncodedString(DFA20_eotS);
    static final short[] DFA20_eof = DFA.unpackEncodedString(DFA20_eofS);
    static final char[] DFA20_min = DFA.unpackEncodedStringToUnsignedChars(DFA20_minS);
    static final char[] DFA20_max = DFA.unpackEncodedStringToUnsignedChars(DFA20_maxS);
    static final short[] DFA20_accept = DFA.unpackEncodedString(DFA20_acceptS);
    static final short[] DFA20_special = DFA.unpackEncodedString(DFA20_specialS);
    static final short[][] DFA20_transition;

    static {
        int numStates = DFA20_transitionS.length;
        DFA20_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA20_transition[i] = DFA.unpackEncodedString(DFA20_transitionS[i]);
        }
    }

    class DFA20 extends DFA {

        public DFA20(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 20;
            this.eot = DFA20_eot;
            this.eof = DFA20_eof;
            this.min = DFA20_min;
            this.max = DFA20_max;
            this.accept = DFA20_accept;
            this.special = DFA20_special;
            this.transition = DFA20_transition;
        }
        public String getDescription() {
            return "350:7: (ca= comb_attr )?";
        }
    }
    static final String DFA21_eotS =
        "\31\uffff";
    static final String DFA21_eofS =
        "\31\uffff";
    static final String DFA21_minS =
        "\1\4\1\2\1\uffff\20\3\1\uffff\1\4\1\2\1\4\2\3";
    static final String DFA21_maxS =
        "\2\132\1\uffff\20\132\1\uffff\5\132";
    static final String DFA21_acceptS =
        "\2\uffff\1\2\20\uffff\1\1\5\uffff";
    static final String DFA21_specialS =
        "\31\uffff}>";
    static final String[] DFA21_transitionS = {
            "\24\2\1\1\102\2",
            "\1\3\1\uffff\127\2",
            "",
            "\1\23\2\2\1\4\7\2\1\17\3\2\1\16\2\2\1\12\27\2\1\6\1\2\1\5\17"+
            "\2\1\21\1\2\1\20\1\22\1\2\1\13\10\2\1\10\4\2\1\7\2\2\1\11\1"+
            "\15\1\14\3\2",
            "\1\24\2\2\1\4\7\2\1\17\3\2\1\16\2\2\1\12\27\2\1\6\1\2\1\5\17"+
            "\2\1\21\1\2\1\20\1\22\1\2\1\13\10\2\1\10\4\2\1\7\2\2\1\11\1"+
            "\15\1\14\3\2",
            "\1\24\2\2\1\4\7\2\1\17\3\2\1\16\2\2\1\12\27\2\1\6\1\2\1\5\17"+
            "\2\1\21\1\2\1\20\1\22\1\2\1\13\10\2\1\10\4\2\1\7\2\2\1\11\1"+
            "\15\1\14\3\2",
            "\1\24\2\2\1\4\7\2\1\17\3\2\1\16\2\2\1\12\27\2\1\6\1\2\1\5\17"+
            "\2\1\21\1\2\1\20\1\22\1\2\1\13\10\2\1\10\4\2\1\7\2\2\1\11\1"+
            "\15\1\14\3\2",
            "\1\24\2\2\1\4\7\2\1\17\3\2\1\16\2\2\1\12\27\2\1\6\1\2\1\5\17"+
            "\2\1\21\1\2\1\20\1\22\1\2\1\13\10\2\1\10\4\2\1\7\2\2\1\11\1"+
            "\15\1\14\3\2",
            "\1\24\2\2\1\4\7\2\1\17\3\2\1\16\2\2\1\12\27\2\1\6\1\2\1\5\17"+
            "\2\1\21\1\2\1\20\1\22\1\2\1\13\10\2\1\10\4\2\1\7\2\2\1\11\1"+
            "\15\1\14\3\2",
            "\1\24\127\2",
            "\1\24\127\2",
            "\1\24\127\2",
            "\1\24\127\2",
            "\1\24\127\2",
            "\1\24\127\2",
            "\1\24\127\2",
            "\1\24\127\2",
            "\1\24\127\2",
            "\1\24\127\2",
            "",
            "\127\25",
            "\1\26\1\2\127\23",
            "\127\27",
            "\1\30\127\27",
            "\1\2\127\23"
    };

    static final short[] DFA21_eot = DFA.unpackEncodedString(DFA21_eotS);
    static final short[] DFA21_eof = DFA.unpackEncodedString(DFA21_eofS);
    static final char[] DFA21_min = DFA.unpackEncodedStringToUnsignedChars(DFA21_minS);
    static final char[] DFA21_max = DFA.unpackEncodedStringToUnsignedChars(DFA21_maxS);
    static final short[] DFA21_accept = DFA.unpackEncodedString(DFA21_acceptS);
    static final short[] DFA21_special = DFA.unpackEncodedString(DFA21_specialS);
    static final short[][] DFA21_transition;

    static {
        int numStates = DFA21_transitionS.length;
        DFA21_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA21_transition[i] = DFA.unpackEncodedString(DFA21_transitionS[i]);
        }
    }

    class DFA21 extends DFA {

        public DFA21(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 21;
            this.eot = DFA21_eot;
            this.eof = DFA21_eof;
            this.min = DFA21_min;
            this.max = DFA21_max;
            this.accept = DFA21_accept;
            this.special = DFA21_special;
            this.transition = DFA21_transition;
        }
        public String getDescription() {
            return "399:7: (ca= comb_attr )?";
        }
    }
    static final String DFA54_eotS =
        "\17\uffff";
    static final String DFA54_eofS =
        "\17\uffff";
    static final String DFA54_minS =
        "\1\7\1\uffff\1\2\1\uffff\1\45\1\2\1\27\1\2\1\3\1\47\1\21\1\47\2"+
        "\3\1\21";
    static final String DFA54_maxS =
        "\1\125\1\uffff\1\2\1\uffff\1\45\1\2\1\46\1\2\1\3\1\47\1\125\1\47"+
        "\2\3\1\125";
    static final String DFA54_acceptS =
        "\1\uffff\1\1\1\uffff\1\2\13\uffff";
    static final String DFA54_specialS =
        "\17\uffff}>";
    static final String[] DFA54_transitionS = {
            "\1\1\1\2\1\1\7\uffff\1\1\5\uffff\1\1\64\uffff\1\1\10\uffff\1"+
            "\3",
            "",
            "\1\4",
            "",
            "\1\5",
            "\1\6",
            "\1\7\16\uffff\1\10",
            "\1\11",
            "\1\12",
            "\1\13",
            "\1\1\5\uffff\1\1\15\uffff\1\5\46\uffff\1\1\10\uffff\1\3",
            "\1\14",
            "\1\15",
            "\1\16",
            "\1\1\5\uffff\1\1\15\uffff\1\5\46\uffff\1\1\10\uffff\1\3"
    };

    static final short[] DFA54_eot = DFA.unpackEncodedString(DFA54_eotS);
    static final short[] DFA54_eof = DFA.unpackEncodedString(DFA54_eofS);
    static final char[] DFA54_min = DFA.unpackEncodedStringToUnsignedChars(DFA54_minS);
    static final char[] DFA54_max = DFA.unpackEncodedStringToUnsignedChars(DFA54_maxS);
    static final short[] DFA54_accept = DFA.unpackEncodedString(DFA54_acceptS);
    static final short[] DFA54_special = DFA.unpackEncodedString(DFA54_specialS);
    static final short[][] DFA54_transition;

    static {
        int numStates = DFA54_transitionS.length;
        DFA54_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA54_transition[i] = DFA.unpackEncodedString(DFA54_transitionS[i]);
        }
    }

    class DFA54 extends DFA {

        public DFA54(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 54;
            this.eot = DFA54_eot;
            this.eof = DFA54_eof;
            this.min = DFA54_min;
            this.max = DFA54_max;
            this.accept = DFA54_accept;
            this.special = DFA54_special;
            this.transition = DFA54_transition;
        }
        public String getDescription() {
            return "857:1: pred_param_type : ( par_pred_param_type | var_pred_param_type );";
        }
    }
    static final String DFA63_eotS =
        "\32\uffff";
    static final String DFA63_eofS =
        "\32\uffff";
    static final String DFA63_minS =
        "\1\7\3\uffff\2\2\1\21\1\45\2\uffff\1\2\1\27\1\2\1\3\1\47\1\21\1"+
        "\47\2\uffff\1\2\1\3\1\21\1\3\2\uffff\1\21";
    static final String DFA63_maxS =
        "\1\114\3\uffff\2\2\1\27\1\45\2\uffff\1\2\1\46\1\2\1\3\1\47\1\114"+
        "\1\47\2\uffff\1\2\1\3\1\27\1\3\2\uffff\1\114";
    static final String DFA63_acceptS =
        "\1\uffff\1\1\1\2\1\3\4\uffff\1\4\1\5\7\uffff\1\6\1\7\4\uffff\1\10"+
        "\1\11\1\uffff";
    static final String DFA63_specialS =
        "\32\uffff}>";
    static final String[] DFA63_transitionS = {
            "\1\1\1\5\1\1\7\uffff\1\3\5\uffff\1\2\64\uffff\1\4",
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
            "\1\14\16\uffff\1\15",
            "\1\16",
            "\1\17",
            "\1\20",
            "\1\22\5\uffff\1\21\15\uffff\1\12\46\uffff\1\23",
            "\1\24",
            "",
            "",
            "\1\25",
            "\1\26",
            "\1\30\5\uffff\1\27",
            "\1\31",
            "",
            "",
            "\1\22\5\uffff\1\21\15\uffff\1\12\46\uffff\1\23"
    };

    static final short[] DFA63_eot = DFA.unpackEncodedString(DFA63_eotS);
    static final short[] DFA63_eof = DFA.unpackEncodedString(DFA63_eofS);
    static final char[] DFA63_min = DFA.unpackEncodedStringToUnsignedChars(DFA63_minS);
    static final char[] DFA63_max = DFA.unpackEncodedStringToUnsignedChars(DFA63_maxS);
    static final short[] DFA63_accept = DFA.unpackEncodedString(DFA63_acceptS);
    static final short[] DFA63_special = DFA.unpackEncodedString(DFA63_specialS);
    static final short[][] DFA63_transition;

    static {
        int numStates = DFA63_transitionS.length;
        DFA63_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA63_transition[i] = DFA.unpackEncodedString(DFA63_transitionS[i]);
        }
    }

    class DFA63 extends DFA {

        public DFA63(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 63;
            this.eot = DFA63_eot;
            this.eof = DFA63_eof;
            this.min = DFA63_min;
            this.max = DFA63_max;
            this.accept = DFA63_accept;
            this.special = DFA63_special;
            this.transition = DFA63_transition;
        }
        public String getDescription() {
            return "863:1: par_pred_param_type : ( par_type | ^( DD INT_CONST INT_CONST ) | ^( CM ( INT_CONST )+ ) | ^( SET ^( DD INT_CONST INT_CONST ) ) | ^( SET ^( CM ( INT_CONST )+ ) ) | ^( ARRAY ( index_set )+ ^( DD INT_CONST INT_CONST ) ) | ^( ARRAY ( index_set )+ ^( CM ( INT_CONST )+ ) ) | ^( ARRAY ( index_set )+ ^( SET ^( DD INT_CONST INT_CONST ) ) ) | ^( ARRAY ( index_set )+ ^( SET ^( CM ( INT_CONST )+ ) ) ) );";
        }
    }
 

    public static final BitSet FOLLOW_pred_decl_in_flatzinc_model53 = new BitSet(new long[]{0x4000000800100000L,0x000000000022C008L});
    public static final BitSet FOLLOW_param_decl_in_flatzinc_model58 = new BitSet(new long[]{0x4000000800100000L,0x000000000022C000L});
    public static final BitSet FOLLOW_var_decl_in_flatzinc_model63 = new BitSet(new long[]{0x0000000800100000L,0x000000000022C000L});
    public static final BitSet FOLLOW_constraint_in_flatzinc_model68 = new BitSet(new long[]{0x0000000800100000L,0x000000000002C000L});
    public static final BitSet FOLLOW_group_decl_in_flatzinc_model77 = new BitSet(new long[]{0x0000000800000000L,0x000000000002C000L});
    public static final BitSet FOLLOW_structure_in_flatzinc_model91 = new BitSet(new long[]{0x0000000000000000L,0x0000000000004000L});
    public static final BitSet FOLLOW_solve_goal_in_flatzinc_model103 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENTIFIER_in_group_decl157 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_predicates_in_group_decl161 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_predicate_in_predicates196 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_AND_in_predicates221 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_predicates_in_predicates226 = new BitSet(new long[]{0x9004001000244018L,0x0000000000E80016L});
    public static final BitSet FOLLOW_OR_in_predicates255 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_predicates_in_predicates260 = new BitSet(new long[]{0x9004001000244018L,0x0000000000E80016L});
    public static final BitSet FOLLOW_TRUE_in_predicate291 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_attribute_in_predicate301 = new BitSet(new long[]{0x0BD0000000000000L});
    public static final BitSet FOLLOW_op_in_predicate305 = new BitSet(new long[]{0x0000008000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_predicate309 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IN_in_predicate326 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_IDENTIFIER_in_predicate331 = new BitSet(new long[]{0x0000000800000008L});
    public static final BitSet FOLLOW_NOT_in_predicate343 = new BitSet(new long[]{0x8004001000244000L,0x0000000000E80016L});
    public static final BitSet FOLLOW_predicate_in_predicate347 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_VAR_in_attribute374 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CSTR_in_attribute390 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PROP_in_attribute405 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_VNAME_in_attribute420 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_VCARD_in_attribute435 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CNAME_in_attribute449 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CARITY_in_attribute464 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PPRIO_in_attribute477 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PARITY_in_attribute491 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PPRIOD_in_attribute504 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_OEQ_in_op532 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ONQ_in_op544 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_OLT_in_op556 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_OGT_in_op568 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_OLQ_in_op580 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_OGQ_in_op592 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_struct_in_structure621 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_struct_reg_in_structure634 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRUC_in_struct659 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_elt_in_struct676 = new BitSet(new long[]{0x0000240A01000000L,0x00000000000280A0L});
    public static final BitSet FOLLOW_comb_attr_in_struct698 = new BitSet(new long[]{0x0000240200000000L,0x00000000000000A0L});
    public static final BitSet FOLLOW_coll_in_struct707 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_STREG_in_struct_reg733 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_IDENTIFIER_in_struct_reg735 = new BitSet(new long[]{0xFFFFFFFFFFFFFFF0L,0x0000000007FFFFFFL});
    public static final BitSet FOLLOW_comb_attr_in_struct_reg760 = new BitSet(new long[]{0x0000240200000000L,0x00000000000000A0L});
    public static final BitSet FOLLOW_coll_in_struct_reg769 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_struct_in_elt804 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_struct_reg_in_elt818 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENTIFIER_in_elt827 = new BitSet(new long[]{0x0000010000000002L});
    public static final BitSet FOLLOW_KEY_in_elt830 = new BitSet(new long[]{0x8000000000244000L,0x0000000000E00016L});
    public static final BitSet FOLLOW_attribute_in_elt834 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_attribute_in_many874 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_comb_attr_in_many882 = new BitSet(new long[]{0xFFFFFFFFFFFFFFF0L,0x0000000007FFFFFFL});
    public static final BitSet FOLLOW_EACH_in_many920 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_attribute_in_many928 = new BitSet(new long[]{0xFFFFFFFFFFFFFFF0L,0x0000000007FFFFFFL});
    public static final BitSet FOLLOW_comb_attr_in_many936 = new BitSet(new long[]{0xFFFFFFFFFFFFFFF0L,0x0000000007FFFFFFL});
    public static final BitSet FOLLOW_QUEUE_in_coll1011 = new BitSet(new long[]{0x0400000000000000L,0x0000000002000000L});
    public static final BitSet FOLLOW_qiter_in_coll1015 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_REV_in_coll1032 = new BitSet(new long[]{0x0000040000000000L});
    public static final BitSet FOLLOW_LIST_in_coll1036 = new BitSet(new long[]{0x0400000100000000L,0x0000000003000000L});
    public static final BitSet FOLLOW_liter_in_coll1040 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MAX_in_coll1057 = new BitSet(new long[]{0x0000000200000000L});
    public static final BitSet FOLLOW_HEAP_in_coll1061 = new BitSet(new long[]{0x0400000000000000L,0x0000000002000000L});
    public static final BitSet FOLLOW_qiter_in_coll1065 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ONE_in_qiter1091 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_WONE_in_qiter1103 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_qiter_in_liter1135 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FOR_in_liter1147 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_WFOR_in_liter1159 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DO_in_comb_attr1186 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_attr_op_in_comb_attr1191 = new BitSet(new long[]{0x8000A00000244048L,0x0000000000E42016L});
    public static final BitSet FOLLOW_attribute_in_comb_attr1202 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_ANY_in_attr_op1226 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MIN_in_attr_op1238 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MAX_in_attr_op1250 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SUM_in_attr_op1262 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SIZE_in_attr_op1274 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ARRPAR_in_par_type1318 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_index_set_in_par_type1323 = new BitSet(new long[]{0x0000006080002000L,0x0000000000001000L});
    public static final BitSet FOLLOW_par_type_u_in_par_type1330 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_APAR_in_par_type1348 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_par_type_u_in_par_type1352 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_BOOL_in_par_type_u1382 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FLOAT_in_par_type_u1398 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SET_in_par_type_u1414 = new BitSet(new long[]{0x0020000000000000L});
    public static final BitSet FOLLOW_OF_in_par_type_u1416 = new BitSet(new long[]{0x0000004000000000L});
    public static final BitSet FOLLOW_INT_in_par_type_u1418 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INT_in_par_type_u1434 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ARRVAR_in_var_type1475 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_index_set_in_var_type1480 = new BitSet(new long[]{0x0000006080822000L,0x0000000000001000L});
    public static final BitSet FOLLOW_var_type_u_in_var_type1487 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_AVAR_in_var_type1505 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_var_type_u_in_var_type1509 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_BOOL_in_var_type_u1540 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FLOAT_in_var_type_u1556 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INT_in_var_type_u1572 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DD_in_var_type_u1589 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_INT_CONST_in_var_type_u1593 = new BitSet(new long[]{0x0000008000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_var_type_u1597 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_CM_in_var_type_u1632 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_INT_CONST_in_var_type_u1637 = new BitSet(new long[]{0x0000008000000008L});
    public static final BitSet FOLLOW_SET_in_var_type_u1658 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_DD_in_var_type_u1661 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_INT_CONST_in_var_type_u1665 = new BitSet(new long[]{0x0000008000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_var_type_u1669 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_SET_in_var_type_u1699 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_CM_in_var_type_u1702 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_INT_CONST_in_var_type_u1707 = new BitSet(new long[]{0x0000008000000008L});
    public static final BitSet FOLLOW_INDEX_in_index_set1742 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_DD_in_index_set1745 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_INT_CONST_in_index_set1749 = new BitSet(new long[]{0x0000008000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_index_set1753 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_INDEX_in_index_set1772 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_INT_in_index_set1774 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_LB_in_expr1818 = new BitSet(new long[]{0x0000008000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_expr1823 = new BitSet(new long[]{0x0000008000000000L,0x0000000000000040L});
    public static final BitSet FOLLOW_RB_in_expr1828 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_bool_const_in_expr1846 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INT_CONST_in_expr1864 = new BitSet(new long[]{0x0000000000800002L});
    public static final BitSet FOLLOW_DD_in_expr1867 = new BitSet(new long[]{0x0000008000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_expr1871 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_EXPR_in_expr1901 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_LS_in_expr1903 = new BitSet(new long[]{0x0000028860000000L,0x0000000000090200L});
    public static final BitSet FOLLOW_expr_in_expr1908 = new BitSet(new long[]{0x0000028860000000L,0x0000000000090200L});
    public static final BitSet FOLLOW_RS_in_expr1913 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_id_expr_in_expr1932 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_in_expr1948 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENTIFIER_in_id_expr2006 = new BitSet(new long[]{0x0000180000000002L});
    public static final BitSet FOLLOW_LP_in_id_expr2010 = new BitSet(new long[]{0x0000028860000000L,0x0000000000090000L});
    public static final BitSet FOLLOW_expr_in_id_expr2014 = new BitSet(new long[]{0x0000000000020000L,0x0000000000000100L});
    public static final BitSet FOLLOW_CM_in_id_expr2018 = new BitSet(new long[]{0x0000028860000000L,0x0000000000090000L});
    public static final BitSet FOLLOW_expr_in_id_expr2022 = new BitSet(new long[]{0x0000000000020000L,0x0000000000000100L});
    public static final BitSet FOLLOW_RP_in_id_expr2027 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LS_in_id_expr2031 = new BitSet(new long[]{0x0000008000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_id_expr2035 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000200L});
    public static final BitSet FOLLOW_RS_in_id_expr2037 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PAR_in_param_decl2064 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_IDENTIFIER_in_param_decl2066 = new BitSet(new long[]{0x0000000000000280L});
    public static final BitSet FOLLOW_par_type_in_param_decl2070 = new BitSet(new long[]{0x0000028860000000L,0x0000000000090000L});
    public static final BitSet FOLLOW_expr_in_param_decl2074 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_VAR_in_var_decl2093 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_IDENTIFIER_in_var_decl2095 = new BitSet(new long[]{0x0000000000001400L});
    public static final BitSet FOLLOW_var_type_in_var_decl2099 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_annotations_in_var_decl2103 = new BitSet(new long[]{0x0000028860000008L,0x0000000000090000L});
    public static final BitSet FOLLOW_expr_in_var_decl2107 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_CONSTRAINT_in_constraint2132 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_IDENTIFIER_in_constraint2134 = new BitSet(new long[]{0x0000028860000000L,0x0000000000090000L});
    public static final BitSet FOLLOW_expr_in_constraint2139 = new BitSet(new long[]{0x0000028860000020L,0x0000000000090000L});
    public static final BitSet FOLLOW_annotations_in_constraint2146 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_SOLVE_in_solve_goal2166 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_annotations_in_solve_goal2170 = new BitSet(new long[]{0x0001400000000000L,0x0000000000000400L});
    public static final BitSet FOLLOW_resolution_in_solve_goal2174 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_SATISFY_in_resolution2198 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MINIMIZE_in_resolution2209 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_expr_in_resolution2213 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_MAXIMIZE_in_resolution2231 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_expr_in_resolution2235 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_ANNOTATIONS_in_annotations2278 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_annotation_in_annotations2283 = new BitSet(new long[]{0x0000000800000008L});
    public static final BitSet FOLLOW_IDENTIFIER_in_annotation2320 = new BitSet(new long[]{0x0000080000000002L});
    public static final BitSet FOLLOW_LP_in_annotation2323 = new BitSet(new long[]{0x0000028860000000L,0x0000000000090000L});
    public static final BitSet FOLLOW_expr_in_annotation2328 = new BitSet(new long[]{0x0000028860000000L,0x0000000000090100L});
    public static final BitSet FOLLOW_RP_in_annotation2333 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TRUE_in_bool_const2365 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FALSE_in_bool_const2377 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PREDICATE_in_pred_decl2396 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_IDENTIFIER_in_pred_decl2398 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_pred_param_in_pred_decl2400 = new BitSet(new long[]{0x0000000000010008L});
    public static final BitSet FOLLOW_CL_in_pred_param2423 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_pred_param_type_in_pred_param2425 = new BitSet(new long[]{0x0000000800000000L});
    public static final BitSet FOLLOW_IDENTIFIER_in_pred_param2427 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_par_pred_param_type_in_pred_param_type2448 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_var_pred_param_type_in_pred_param_type2458 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_par_type_in_par_pred_param_type2478 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DD_in_par_pred_param_type2490 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type2492 = new BitSet(new long[]{0x0000008000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type2494 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_CM_in_par_pred_param_type2506 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type2508 = new BitSet(new long[]{0x0000008000000008L});
    public static final BitSet FOLLOW_SET_in_par_pred_param_type2521 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_DD_in_par_pred_param_type2524 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type2526 = new BitSet(new long[]{0x0000008000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type2528 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_SET_in_par_pred_param_type2541 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_CM_in_par_pred_param_type2544 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type2546 = new BitSet(new long[]{0x0000008000000008L});
    public static final BitSet FOLLOW_ARRAY_in_par_pred_param_type2561 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_index_set_in_par_pred_param_type2563 = new BitSet(new long[]{0x0000002000800000L});
    public static final BitSet FOLLOW_DD_in_par_pred_param_type2567 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type2569 = new BitSet(new long[]{0x0000008000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type2571 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_ARRAY_in_par_pred_param_type2584 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_index_set_in_par_pred_param_type2586 = new BitSet(new long[]{0x0000002000020000L});
    public static final BitSet FOLLOW_CM_in_par_pred_param_type2590 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type2592 = new BitSet(new long[]{0x0000008000000008L});
    public static final BitSet FOLLOW_ARRAY_in_par_pred_param_type2606 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_index_set_in_par_pred_param_type2608 = new BitSet(new long[]{0x0000002000000000L,0x0000000000001000L});
    public static final BitSet FOLLOW_SET_in_par_pred_param_type2612 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_DD_in_par_pred_param_type2615 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type2617 = new BitSet(new long[]{0x0000008000000000L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type2619 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_ARRAY_in_par_pred_param_type2633 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_index_set_in_par_pred_param_type2635 = new BitSet(new long[]{0x0000002000000000L,0x0000000000001000L});
    public static final BitSet FOLLOW_SET_in_par_pred_param_type2639 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_CM_in_par_pred_param_type2642 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_INT_CONST_in_par_pred_param_type2644 = new BitSet(new long[]{0x0000008000000008L});
    public static final BitSet FOLLOW_VAR_in_var_pred_param_type2669 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_var_type_in_var_pred_param_type2671 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_VAR_in_var_pred_param_type2683 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_SET_in_var_pred_param_type2685 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_ARRAY_in_var_pred_param_type2697 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_index_set_in_var_pred_param_type2699 = new BitSet(new long[]{0x0000002000000000L,0x0000000000200000L});
    public static final BitSet FOLLOW_VAR_in_var_pred_param_type2703 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_SET_in_var_pred_param_type2705 = new BitSet(new long[]{0x0000000000000008L});

}