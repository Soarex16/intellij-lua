package com.soarex16.lua.lang;

import com.intellij.psi.tree.IElementType;
import com.soarex16.lua.lang.psi.LuaTokenType;

import static com.intellij.psi.TokenType.BAD_CHARACTER;
%%

%public
%class _LuaLexer
%implements FlexLexer
%type IElementType
%unicode

%{
    StringBuilder string = new StringBuilder();

    private IElementType parseLongBracketsString() {
        // TODO: implement
        return null;
    }
%}

/* Main Character Classes */
LineTerminator = \r|\n|\r\n
InputCharacter = [^\r\n]

Whitespace = {LineTerminator} | [ \t\f]
Alpha = [a-zA-Z]
Num = [0-9]
AlphaNum = {Alpha} | {Num}
Underscore = _

Identifier = ({Underscore} | {Alpha}) {AlphaNum}*

DoubleQuotedString = (\"([^\"\r\n\\]|\\.)*\")
SingleQuotedString = (\'([^\'\r\n\\]|\\.)*\')

LineComment = "--" {InputCharacter}* {LineTerminator}?
BlockComment = "--[[" ~"]]"

/* Numbers */

/* Integer Numbers */

DecDigit = [0-9]
OctDigit = [0-7]
HexDigit = [0-9a-fA-F]

DecIntNumber = {DecDigit}+
OctIntNumber = {OctDigit}+
HexIntNumber = "0" [xX] {HexDigit}+

/* Real Numbers */

Exponent = [eE] [+-]? [0-9]+
RealNumber = ([0-9]+\.[0-9]*|[0-9]*\.[0-9]+) {Exponent}?

%state LITERAL_STRING

%%

<YYINITIAL> {
    /* Boolean Literals */
    "true"   { return LuaTokenType.Companion.getTRUE(); }
    "false"   { return LuaTokenType.Companion.getFALSE(); }

    /* Nil Literal */
    "nil"   { return LuaTokenType.Companion.getNIL(); }

    /* Logical Operators */
    "not"   { return LuaTokenType.Companion.getNOT(); }
    "or"   { return LuaTokenType.Companion.getOR(); }
    "and"   { return LuaTokenType.Companion.getAND(); }

    /* Control Structures */
    "if"   { return LuaTokenType.Companion.getIF(); }
    "then"   { return LuaTokenType.Companion.getTHEN(); }
    "else"   { return LuaTokenType.Companion.getELSE(); }
    "elseif"   { return LuaTokenType.Companion.getELSEIF(); }
    "end"   { return LuaTokenType.Companion.getEND(); }
    "goto"   { return LuaTokenType.Companion.getGOTO(); }

    "while"   { return LuaTokenType.Companion.getWHILE(); }
    "repeat"   { return LuaTokenType.Companion.getREPEAT(); }
    "for"   { return LuaTokenType.Companion.getFOR(); }
    "do"   { return LuaTokenType.Companion.getDO(); }
    "until"   { return LuaTokenType.Companion.getUNTIL(); }
    "break"   { return LuaTokenType.Companion.getBREAK(); }
    "in"   { return LuaTokenType.Companion.getIN(); }

    "local"   { return LuaTokenType.Companion.getLOCAL(); }

    /* Functions */
    "function"  { return LuaTokenType.Companion.getFUNCTION(); }
    "return"  { return LuaTokenType.Companion.getRETURN(); }

    /* Separators */
    "("   { return LuaTokenType.Companion.getL_PAREN(); }
    "("   { return LuaTokenType.Companion.getR_PAREN(); }
    "{"   { return LuaTokenType.Companion.getL_BRACE(); }
    "}"   { return LuaTokenType.Companion.getR_BRACE(); }
    "["   { return LuaTokenType.Companion.getL_BRACKET(); }
    "]"   { return LuaTokenType.Companion.getR_BRACKET(); }

    ";"   { return LuaTokenType.Companion.getSEMICOLON(); }
    ":"   { return LuaTokenType.Companion.getCOLON(); }
    "::"   { return LuaTokenType.Companion.getDOUBLE_COLON(); }
    ","   { return LuaTokenType.Companion.getCOMMA(); }
    "."   { return LuaTokenType.Companion.getDOT(); }
    "..."   { return LuaTokenType.Companion.getDOTDOTDOT(); }

    /* String Operators */
    ".."   { return LuaTokenType.Companion.getDOTDOT(); }
    "#"   { return LuaTokenType.Companion.getHASH(); }

    /* Bitwise Operators */
    "<<"   { return LuaTokenType.Companion.getSHL(); }
    ">>"   { return LuaTokenType.Companion.getSHR(); }
    "|"   { return LuaTokenType.Companion.getBOR(); }
    "&"   { return LuaTokenType.Companion.getBAND(); }
    "~"   { return LuaTokenType.Companion.getBNOT(); }

    /* Arithmetic Operators */
    "+"   { return LuaTokenType.Companion.getADD(); }
    "-"   { return LuaTokenType.Companion.getSUB(); }
    "*"   { return LuaTokenType.Companion.getMUL(); }
    "/"   { return LuaTokenType.Companion.getDIV(); }
    "//"   { return LuaTokenType.Companion.getIDIV(); }
    "%"   { return LuaTokenType.Companion.getMOD(); }
    "^"   { return LuaTokenType.Companion.getPOW(); }

    /* Relational Operators */
    "<"   { return LuaTokenType.Companion.getLESS(); }
    "<="   { return LuaTokenType.Companion.getLESS_EQUAL(); }
    ">"   { return LuaTokenType.Companion.getGREATER(); }
    ">="   { return LuaTokenType.Companion.getGREATER_EQUAL(); }
    "=="   { return LuaTokenType.Companion.getEQUAL(); }
    "~="   { return LuaTokenType.Companion.getNOT_EQUAL(); }

    /* Assignment */
    "="   { return LuaTokenType.Companion.getASSIGN(); }
}

{Identifier} { return LuaTokenType.Companion.getIDENTIFIER(); }

/* Comments */
{LineComment} { return LuaTokenType.Companion.getLINE_COMMENT(); }
{BlockComment} { return LuaTokenType.Companion.getBLOCK_COMMENT(); }

/* String Literals */
{DoubleQuotedString} { return LuaTokenType.Companion.getDOUBLE_QUOTED_STRING(); }
{SingleQuotedString} { return LuaTokenType.Companion.getSINGLE_QUOTED_STRING(); }

"[[" { return parseLongBracketsString(); }

/* Number Literals */

/* Integer Numbers */

{DecIntNumber} { return LuaTokenType.Companion.getDEC_INT_NUMBER(); }
{OctIntNumber} { return LuaTokenType.Companion.getDEC_INT_NUMBER(); }
{HexIntNumber} { return LuaTokenType.Companion.getHEX_INT_NUMBER(); }

/* Real Numbers */

{RealNumber} { return LuaTokenType.Companion.getREAL_NUMBER(); }

/* error fallback */
[^] { return BAD_CHARACTER; }