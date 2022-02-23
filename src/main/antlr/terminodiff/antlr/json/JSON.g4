
/** Taken from "The Definitive ANTLR 4 Reference" by Terence Parr */

// Derived from http://json.org

grammar JSON;

@header {
    package terminodiff.antlr.json;
}

json
   : value
   ;

obj
   : '{' pair (comma pair)* '}'
   | '{' '}'
   ;

pair
   : propertyName ':' value
   ;

propertyName //color me 1
   : STRING
   ;

arr
   : '[' value (comma value)* ']'
   | '[' ']'
   ;

value
   : obj
   | arr
   | literal
   | specialliteral
   ;

literal // colour me 2
   : STRING
   | NUMBER
   ;

specialliteral // colour me 3
    : 'true'
    | 'false'
    | 'null'
    ;

comma
   : ','
   ;


STRING
   : '"' (ESC | SAFECODEPOINT)* '"'
   ;


fragment ESC
   : '\\' (["\\/bfnrt] | UNICODE)
   ;


fragment UNICODE
   : 'u' HEX HEX HEX HEX
   ;


fragment HEX
   : [0-9a-fA-F]
   ;


fragment SAFECODEPOINT
   : ~ ["\\\u0000-\u001F]
   ;


NUMBER
   : '-'? INT ('.' [0-9] +)? EXP?
   ;


fragment INT
   : '0' | [1-9] [0-9]*
   ;

// no leading zeros

fragment EXP
   : [Ee] [+\-]? INT
   ;

// \- since - means "range" inside [...]

WS
   : [ \t\n\r] + -> skip
   ;