package com.soarex16.lua.lang.psi

import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import com.soarex16.lua.lang.LuaLanguage
import org.jetbrains.annotations.NonNls

open class LuaTokenType(@NonNls debugName: String) : IElementType(debugName, LuaLanguage) {
    override fun toString(): String = "${LuaLanguage.id}.TOKEN.${super.toString()}"

    companion object {
        val TRUE = LuaTokenType("TRUE")
        val FALSE = LuaTokenType("FALSE")

        val NIL = LuaTokenType("NIL")

        val NOT = LuaTokenType("NOT")
        val OR = LuaTokenType("OR")
        val AND = LuaTokenType("AND")

        val IF = LuaTokenType("IF")
        val THEN = LuaTokenType("THEN")
        val ELSE = LuaTokenType("ELSE")
        val ELSEIF = LuaTokenType("ELSEIF")
        val END = LuaTokenType("END")
        val GOTO = LuaTokenType("GOTO")

        val WHILE = LuaTokenType("WHILE")
        val REPEAT = LuaTokenType("REPEAT")
        val FOR = LuaTokenType("FOR")
        val DO = LuaTokenType("DO")
        val UNTIL = LuaTokenType("UNTIL")
        val BREAK = LuaTokenType("BREAK")
        val IN = LuaTokenType("IN")

        val LOCAL = LuaTokenType("LOCAL")
        val FUNCTION = LuaTokenType("FUNCTION")
        val RETURN = LuaTokenType("RETURN")

        val L_PAREN = LuaTokenType("L_PAREN") // (
        val R_PAREN = LuaTokenType("R_PAREN") // )
        val L_BRACE = LuaTokenType("L_BRACE") // {
        val R_BRACE = LuaTokenType("R_BRACE") // }
        val L_BRACKET = LuaTokenType("L_BRACKET") // [
        val R_BRACKET = LuaTokenType("R_BRACKET") // ]

        val DOUBLE_COLON = LuaTokenType("DOUBLE_COLON") // ::
        val SEMICOLON = LuaTokenType("SEMICOLON") // ;
        val COLON = LuaTokenType("COLON") // :
        val COMMA = LuaTokenType("COMMA") // ,
        val DOT = LuaTokenType("DOT") // .
        val DOTDOT = LuaTokenType("DOTDOT") // .., string concatenation
        val DOTDOTDOT = LuaTokenType("DOTDOTDOT") // ...

        val SHL = LuaTokenType("SHL") // <<
        val SHR = LuaTokenType("SHR") // >>
        val BOR = LuaTokenType("BOR") // |
        val BAND = LuaTokenType("BAND") // &
        val BNOT = LuaTokenType("BNOT") // ~

        val ADD = LuaTokenType("ADD") // +
        val SUB = LuaTokenType("SUB") // -
        val MUL = LuaTokenType("MUL") // *
        val DIV = LuaTokenType("DIV") // /
        val IDIV = LuaTokenType("IDIV") // //
        val MOD = LuaTokenType("MOD") // %
        val POW = LuaTokenType("POW") // ^
        val HASH = LuaTokenType("HASH") // #, string length operator

        val LESS = LuaTokenType("LESS") // <
        val LESS_EQUAL = LuaTokenType("LESS_EQUAL") // <=
        val GREATER = LuaTokenType("GREATER") // >
        val GREATER_EQUAL = LuaTokenType("GREATER_EQUAL") // >=
        val EQUAL = LuaTokenType("EQUAL") // ==
        val NOT_EQUAL = LuaTokenType("NOT_EQUAL") // ~=
        val ASSIGN = LuaTokenType("ASSIGN") // =

        val LINE_COMMENT = LuaTokenType("LINE_COMMENT") // --
        val BLOCK_COMMENT = LuaTokenType("BLOCK_COMMENT") // --[[ ]]

        val IDENTIFIER = LuaTokenType("IDENTIFIER")

        val DEC_INT_NUMBER = LuaTokenType("DEC_INT_NUMBER")
        val HEX_INT_NUMBER = LuaTokenType("HEX_INT_NUMBER")
        val OCT_INT_NUMBER = LuaTokenType("OCT_INT_NUMBER")
        val REAL_NUMBER = LuaTokenType("REAL_NUMBER")

        val SINGLE_QUOTED_STRING = LuaTokenType("SINGLE_QUOTED_STRING") // 'some string'
        val DOUBLE_QUOTED_STRING = LuaTokenType("DOUBLE_QUOTED_STRING") // "some string"
        val LONG_BRACKETS_STRING = LuaTokenType("LONG_BRACKETS_STRING") // [[ some string ]]

        val COMMENTS = TokenSet.create(LINE_COMMENT, BLOCK_COMMENT)
        val CONSTANTS = TokenSet.create(TRUE, FALSE, NIL)
        val LITERALS = TokenSet.create(
            TRUE, FALSE, NIL,
            SINGLE_QUOTED_STRING, DOUBLE_QUOTED_STRING, LONG_BRACKETS_STRING,
            DEC_INT_NUMBER, HEX_INT_NUMBER, OCT_INT_NUMBER, REAL_NUMBER
        )
        val NUMBERS = TokenSet.create(DEC_INT_NUMBER, HEX_INT_NUMBER, OCT_INT_NUMBER, REAL_NUMBER)
        val STRINGS = TokenSet.create(SINGLE_QUOTED_STRING, DOUBLE_QUOTED_STRING, LONG_BRACKETS_STRING)
        val KEYWORDS = TokenSet.create(
            NOT, OR, AND, IF, THEN, ELSE, ELSEIF, END, GOTO, WHILE, REPEAT,
            FOR, DO, UNTIL, BREAK, IN, LOCAL, FUNCTION, RETURN
        )

        val OPERATORS = TokenSet.create(
            DOTDOT,
            SHL, SHR, BOR, BAND, BNOT,
            ADD, SUB, MUL, DIV, IDIV, MOD, POW, HASH,
            LESS, LESS_EQUAL, GREATER, GREATER_EQUAL, EQUAL, NOT_EQUAL, ASSIGN
        )
    }
}