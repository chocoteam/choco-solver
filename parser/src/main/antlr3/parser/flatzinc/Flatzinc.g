grammar Flatzinc;

options {
  language = Java;
  output=AST;
}

// PARSER RULES

tokens{
    DD='..';
    DO='.';
    LB='{';
    RB='}';
    CM=',';
    LS='[';
    RS =']';
    EQ='=';
    PL='+';
    MN='-';
    SC=';';
    CL=':';
    DC='::';
    LP='(';
    RP=')';
    
    BOOL='bool';
    TRUE='true';
    FALSE='false';
    INT='int';
    FLOAT='float';
    SET = 'set';
    OF = 'of';
    ARRAY = 'array';
    VAR = 'var';
    PAR = 'par';
    PREDICATE = 'predicate';
    CONSTRAINT =  'constraint';
    SOLVE = 'solve';
    SATISFY = 'satisfy';
    MINIMIZE = 'minimize';
    MAXIMIZE = 'maximize';
}

@header {
package parser.flatzinc;
import parser.flatzinc.ast.expression.*;
}

@lexer::header {
package parser.flatzinc;
}


// PARSER RULES

flatzinc_model
	:   (pred_decl)* (param_decl)* (var_decl)* (constraint)* solve_goal
	;


pred_decl
	:   PREDICATE IDENTIFIER LP pred_param (CM pred_param)* RP SC
	-> ^(PREDICATE IDENTIFIER pred_param+)
	;

pred_param
    :   pred_param_type CL IDENTIFIER
    ->  ^(CL pred_param_type IDENTIFIER)
    ;

pred_param_type
    :   par_pred_param_type
    |   var_pred_param_type
    ;

APAR
    :    '###_P###'
    ;

ARRPAR
    :    '###AP###'
    ;

par_type
    :   ARRAY LS index_set (CM index_set)* RS OF par_type_u
    ->  ^(ARRPAR index_set+ par_type_u)
    |   par_type_u
    ->  ^(APAR par_type_u)
    ;

par_type_u
    :   BOOL
    |   FLOAT
    |   SET OF INT
    |   INT
    ;

AVAR
    :    '###_V###'
    ;

ARRVAR
    :    '###AV###'
    ;

var_type
    :   ARRAY LS index_set (CM index_set)* RS OF VAR var_type_u
    ->  ^(ARRVAR index_set+ var_type_u)
    |   VAR var_type_u
    ->  ^(AVAR var_type_u)
    ;

var_type_u
    :   BOOL
    |   FLOAT
    |   INT
    |   INT_CONST DD INT_CONST
    ->  ^(DD INT_CONST INT_CONST)
//    |   FLOAT_ DD FLOAT_
//    ->  ^(DD FLOAT_ FLOAT_)
    |   LB INT_CONST (CM INT_CONST)* RB
    ->  ^(CM INT_CONST+)
    |   SET OF INT_CONST DD INT_CONST
    ->  ^(SET ^(DD INT_CONST INT_CONST))
    |   SET OF LB INT_CONST (CM INT_CONST)* RB
    ->  ^(SET ^(CM INT_CONST INT_CONST))
    ;

par_pred_param_type
    :   par_type
//    |   FLOAT_ DD FLOAT_
//    ->  ^(DD FLOAT_ FLOAT_)
    |   INT_CONST DD INT_CONST
    ->  ^(DD INT_CONST INT_CONST)
    |   LB INT_CONST (CM INT_CONST)* RB
    ->  ^(CM INT_CONST+)
    |   SET OF INT_CONST DD INT_CONST
    ->  ^(SET ^(DD INT_CONST INT_CONST))
    |   SET OF LB INT_CONST (CM INT_CONST)* RB
    ->  ^(SET ^(CM INT_CONST+))
//    |   ARRAY LS index_set (CM index_set)* RS OF FLOAT_ DD FLOAT_
//    ->  ^(ARRAY index_set+ ^(DD FLOAT_ FLOAT_))
    |   ARRAY LS index_set (CM index_set)* RS OF INT_CONST DD INT_CONST
    ->  ^(ARRAY index_set+ ^(DD INT_CONST INT_CONST))
    |   ARRAY LS index_set (CM index_set)* RS OF LB INT_CONST (CM INT_CONST)* RB
    ->  ^(ARRAY index_set+ ^(CM INT_CONST+))
    |   ARRAY LS index_set (CM index_set)* RS OF SET OF INT_CONST DD INT_CONST
    ->  ^(ARRAY index_set+ ^(SET ^(DD INT_CONST INT_CONST)))
    |   ARRAY LS index_set (CM index_set)* RS OF SET OF LB INT_CONST (CM INT_CONST)* RB
    ->  ^(ARRAY index_set+ ^(SET ^(CM INT_CONST+)))
    ;


var_pred_param_type
    :   var_type
    ->  ^(VAR var_type)
    |   VAR SET OF INT
    ->  ^(VAR SET)
    |   ARRAY LS index_set (CM index_set)* RS OF VAR SET OF INT
    ->  ^(ARRAY index_set+ ^(VAR SET))
    ;

INDEX
    :   '###ID###'
    ;

index_set
    :   INT_CONST DD INT_CONST
    ->  ^(INDEX ^(DD INT_CONST INT_CONST))
    |   INT
    ->  ^(INDEX INT)
    ;

EXPR
    :   '###EX###'
    ;

expr
    :   LB INT_CONST (CM INT_CONST)* RB
    ->  LB INT_CONST+ RB
    |   bool_const
    |   INT_CONST (DD INT_CONST)?
    |   LS (expr (CM expr)*)? RS
    ->  ^(EXPR LS expr* RS)
    |   id_expr
    |   STRING
//    |   FLOAT_
    ;

id_expr
//options {backtrack=true;}
    :   IDENTIFIER ((LP expr (CM expr)* RP)|(LS INT_CONST RS))?
    ;


param_decl
	:   par_type CL IDENTIFIER EQ expr SC
	->  ^(PAR IDENTIFIER par_type expr)
	;


var_decl
	:   var_type CL IDENTIFIER annotations (EQ expr)? SC
	->  ^(VAR IDENTIFIER var_type annotations expr?)
	;

constraint
	:   CONSTRAINT IDENTIFIER LP expr (CM expr)* RP annotations SC
	->  ^(CONSTRAINT IDENTIFIER expr+ annotations)
	;

solve_goal
	:   SOLVE^ annotations resolution SC!
	;

resolution
    :   MINIMIZE^ expr
    |   MAXIMIZE^ expr
    |   SATISFY^
    ;

ANNOTATIONS
    :   '###AS###'
    ;

annotations
    :   (DC annotation)*
    ->  ^(ANNOTATIONS annotation*)
    ;

annotation
    :   IDENTIFIER (LP expr (CM expr)* RP)?
    ->  IDENTIFIER (LP expr+ RP)?
    ;


INT_CONST
    :   ('+'|'-')? ('0'..'9')+
    ;

IDENTIFIER
    :   ('a'..'z'|'A'..'Z'|'_') ('a'..'z'|'A'..'Z'|'0'..'9'|'_')*
    ;


bool_const
    :   TRUE^
    |   FALSE^
    ;


//FLOAT_
//    :   ('0'..'9')+ '.' ('0'..'9')* EXPONENT?
//    |   '.' ('0'..'9')+ EXPONENT?
//    |   ('0'..'9')+ EXPONENT
//    ;


COMMENT
    :   '%' ~('\n'|'\r')* '\r'? '\n' {$channel=HIDDEN;}
    ;

WS  :   ( ' '
        | '\t'
        | '\r'
        | '\n'
        ) {$channel=HIDDEN;}
    ;

STRING
    :  '"' ( ESC_SEQ | ~('\\'|'"') )* '"'
    ;

CHAR:  '\'' ( ESC_SEQ | ~('\''|'\\') ) '\''
    ;

fragment
EXPONENT : ('e'|'E') ('+'|'-')? ('0'..'9')+ ;

fragment
ESC_SEQ
    :   '\\' ('b'|'t'|'n'|'f'|'r'|'\"'|'\''|'\\')
    |   UNICODE_ESC
    |   OCTAL_ESC
    ;

fragment
OCTAL_ESC
    :   '\\' ('0'..'3') ('0'..'7') ('0'..'7')
    |   '\\' ('0'..'7') ('0'..'7')
    |   '\\' ('0'..'7')
    ;

fragment
HEX_DIGIT : ('0'..'9'|'a'..'f'|'A'..'F') ;

fragment
UNICODE_ESC
    :   '\\' 'u' HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT
    ;

