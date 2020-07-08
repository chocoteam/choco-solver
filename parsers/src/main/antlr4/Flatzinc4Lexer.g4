lexer grammar Flatzinc4Lexer;

@lexer::header {
}

/*********************************************
 * KEYWORDS
 **********************************************/

BOOL:'bool';
TRUE:'true';
FALSE:'false';
INT:'int';
FLOAT:'float';
SET :'set';
OF :'of';
ARRAY :'array';
VAR :'var';
PAR :'par';
PREDICATE :'predicate';
CONSTRAINT :  'constraint';
SOLVE :'solve';
SATISFY :'satisfy';
MINIMIZE :'minimize';
MAXIMIZE :'maximize';

DD:'..';
DO:'.';
LB:'{';
RB:'}';
CM:',';
LS:'[';
RS :']';
EQ:'=';
PL:'+';
MN:'-';
SC:';';
CL:':';
DC:'::';
LP:'(';
RP:')';


/*********************************************
 * GENERAL
 **********************************************/


IDENTIFIER
    :   ('a'..'z'|'A'..'Z'|'_') ('a'..'z'|'A'..'Z'|'0'..'9'|'_')*
    ;


COMMENT
    :   '%' ~('\n'|'\r')* '\r'? '\n' -> skip
    ;

WS  :   ( ' '
        | '\t'
        | '\r'
        | '\n'
        ) -> skip
    ;

/*********************************************
 * TYPES
 **********************************************/

INT_CONST
    :   ('+'|'-')? ('0'..'9')+
    ;

//FLOAT_
//    :   ('0'..'9')+ '.' ('0'..'9')* EXPONENT?
//    |   '.' ('0'..'9')+ EXPONENT?
//    |   ('0'..'9')+ EXPONENT
//    ;

STRING
    :  '"' ( ESC_SEQ | ~('\\'|'"') )* '"'
    ;

CHAR:  '\'' ( ESC_SEQ | ~('\''|'\\') ) '\''
    ;

/*********************************************
 * FRAGMENTS
 **********************************************/


fragment
EXPONENT : ('e'|'E') ('+'|'-')? ('0'..'9')+ ;

fragment
ESC_SEQ
    :   '\\' ('b'|'t'|'n'|'f'|'r'|'"'|'\''|'\\')
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