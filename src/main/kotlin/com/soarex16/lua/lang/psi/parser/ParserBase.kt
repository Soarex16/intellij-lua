package com.soarex16.lua.lang.psi.parser

import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiBuilder.Marker
import com.intellij.psi.TokenType
import com.intellij.psi.TokenType.BAD_CHARACTER
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import com.soarex16.lua.lang.psi.LuaElementType
import com.soarex16.lua.lang.psi.LuaTokenType

typealias Parselet = () -> Boolean // parsing primitive
typealias ProductionParselet = () -> Marker? // parselet that creates AST elements (aka 'productions')

open class ParserBase(private val builder: PsiBuilder) {
    protected fun expectAdvance(expectedType: IElementType, errorProvider: () -> String): Boolean {
        if (builder.tokenType === expectedType) {
            advance()
            return true
        } else {
            builder.error("${errorProvider()} expected")
        }
        return false
    }

    protected fun expectAdvance(expectedTypes: TokenSet, errorProvider: () -> String): Boolean {
        if (builder.tokenType in expectedTypes) {
            advance()
            return true
        } else {
            builder.error("${errorProvider()} expected")
        }
        return false
    }

    protected fun lookAhead(steps: Int): IElementType? {
        require(steps >= 0)
        var step = steps
        var tok = builder.lookAhead(step)
        while (tok != null && tok == BAD_CHARACTER) {
            step += 1
            tok = builder.lookAhead(step)
        }

        return tok
    }

    protected fun error(message: String) {
        builder.mark().error(message)
    }

    protected fun advance(): IElementType? {
        val result = builder.tokenType
        builder.advanceLexer()
        while (builder.tokenType == TokenType.BAD_CHARACTER) {
            val badMark = builder.mark()
            builder.advanceLexer()
            badMark.error("Unexpected character")
        }
        return result
    }

    protected fun advance(token: IElementType) {
        assert(builder.tokenType == token)
        advance()
    }

    /**
     * Parser combinator which consumes many times and raises an error if can't parse at least one entry
     * @return true if some input was consumed
     */
    protected fun parseSome(productionName: String, parser: Parselet): Boolean {
        val errorMark = builder.mark()
        var consumed = safePoint(parser)
        if (!consumed) {
            errorMark.error("Expected $productionName")
            return false
        }

        while (consumed) {
            consumed = safePoint(parser)
        }
        return true
    }

    /**
     * Parser combinator which consumes zero or more
     */
    protected fun parseMany(parser: Parselet): Boolean {
        var consumed = safePoint(parser)
        while (consumed) {
            consumed = safePoint(parser)
        }

        return true
    }

    /**
     * Parser combinator which consumes zero or more entries separated by separator
     * @return true if some input was consumed
     */
    protected fun separatedBy(allowDanglingSeparator: Boolean = true, separatorName: String, separatorParser: Parselet, productionName: String, parser: Parselet): Boolean {
        var consumed = safePoint(parser)
        if (!consumed) {
            return false
        }

        while (consumed) {
            val separator = safePoint(separatorParser)
            if (!separator) { // неустойчиво к пропущенным сепараторам
                break
            }

            consumed = safePoint(parser)
            if (!consumed) {
                if (!allowDanglingSeparator) {
                    builder.error("Expected $productionName")
                }
            }
        }

        return true
    }

    protected fun safePoint(parser: Parselet): Boolean {
        val mark = builder.mark()
        if (parser()) {
            mark.drop()
            return true
        }

        mark.rollbackTo()
        return false
    }

    protected fun safePoint(parser: ProductionParselet): Marker? {
        val mark = builder.mark()
        val parseResult = parser()
        if (parseResult != null) {
            mark.drop()
        } else {
            mark.rollbackTo()
        }

        return parseResult
    }

    protected fun rollbackIfNull(parser: ProductionParselet): Marker? {
        val mark = builder.mark()
        val parseResult = parser()
        if (parseResult != null) {
            mark.drop()
        } else {
            mark.rollbackTo()
        }

        return parseResult
    }

    /**
     * Returns index of first parser that matches the input or null if none matched
     */
    protected fun choice(vararg parsers: Parselet): Int? {
        for ((index, parser) in parsers.withIndex()) {
            val matched = safePoint(parser)
            if (matched) {
                return index
            }
        }

        return null
    }

    protected fun choice(vararg parsers: ProductionParselet): Marker? {
        for (parser in parsers) {
            val marker = rollbackIfNull(parser)
            if (marker != null) {
                return marker
            }
        }

        return null
    }

    fun parseNameList(createElement: Boolean = true): Int {
        val mark = builder.mark()
        var namesCount = 0
        separatedBy(allowDanglingSeparator = false, "','", this::parseComma, "identifier") {
            val parsed = safePoint(this::parseName)
            if (parsed != null) {
                namesCount += 1
            }
            parsed != null
        }

        if (createElement) {
            mark.done(LuaElementType.NAME_LIST)
        } else {
            mark.drop()
        }

        return namesCount
    }

    fun parseName(): Marker? {
        val mark = builder.mark()
        val ident = expectAdvance(LuaTokenType.IDENTIFIER) { "identifier" }
        if (ident) {
            mark.done(LuaElementType.NAME)
            return mark
        }

        mark.drop()
        return null
    }

    fun parseComma(): Boolean {
        return expectAdvance(LuaTokenType.COMMA) { "','" }
    }
}