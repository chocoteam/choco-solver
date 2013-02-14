grammar PropPol;

// PARSER RULES

tokens{
	CM=',';
	SC=';';
	CL=':';
	LP='(';
	RP=')';
	QUEUE='queue';
    LIST='list';
    HEAP='heap';
    ONE='one';
    WONE='wone';
    FOR='for';
    WFOR='wfor';
    INC='inc';
    DEC='dec';
    ASSERT='assert';
    MAX='max';
    MIN='min';
    PRIORITY='priority';
    DEGREE='degree';
    OPEQ='==';
    OPNQ='!=';
    OPGT='>';
    OPLT='<';
    RAW='->';
    AND='&';
    OR='|';
}

@header {
	package generator;
}

@lexer::header {
	package generator;
}

policy
    :	groups SC links CM affectations SC
    ;


eval
    :   PRIORITY 
    |   DEGREE
    ;

func
    :   MIN
	|   MAX
	;

funcExp
    :   func LP eval  RP
    ;

comp
    :   INC
    |   DEC
    ;

compExp
    :   comp LP funcExp RP
    ;

opers
    :   OPEQ
    |   OPNQ
    |   OPGT
    |   OPLT
    ;

predExp
    :   ASSERT LP eval opers (FLOAT|INT) RP
    ;


qiter
    :   ONE
    |   WONE
    ;


liter
    :   qiter
    |   FOR
    |   WFOR
    ;

groupExp
    :   QUEUE LP qiter RP
    |   LIST LP liter (CM compExp)? RP
    |   HEAP LP qiter CM funcExp RP
    ;

// definition of one group
group
    :	ID CL groupExp
    ;

// defintion of more than one group
groups
    :	group (CM group)*
    ;

// defintion of one link between group
link
    :	ID RAW ID
    ;

// definition of group network
links
    :	link (CM link)*
	;

condition
	:	ID
	|   predExp
	;

conditions
	: LP  condition (AND|OR) condition LP
	| condition
	;


// affectation of propagators to group
affectation
	: conditions RAW ID 	
	;

affectations
	:	affectation (CM affectation)*	
	;

// LEXER RULES

ID
    :	('a'..'z'|'A'..'Z'|'_') ('a'..'z'|'A'..'Z'|'0'..'9'|'_')*
    ;

INT
    :	'0'..'9'+
    ;

FLOAT
    :   ('0'..'9')+ '.' ('0'..'9')* EXPONENT?
    |   '.' ('0'..'9')+ EXPONENT?
    |   ('0'..'9')+ EXPONENT
    ;

COMMENT
    :   '//' ~('\n'|'\r')* '\r'? '\n' {$channel=HIDDEN;}
    |   '/*' ( options {greedy=false;} : . )* '*/' {$channel=HIDDEN;}
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

fragment
EXPONENT : ('e'|'E') ('+'|'-')? ('0'..'9')+ ;

fragment
HEX_DIGIT : ('0'..'9'|'a'..'f'|'A'..'F') ;

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
UNICODE_ESC
    :   '\\' 'u' HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT
    ;
