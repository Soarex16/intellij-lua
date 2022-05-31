package com.soarex16.lua.lang.psi.parser

import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiBuilder.Marker
import com.soarex16.lua.lang.psi.LuaElementType.Companion.ASSIGNMENT_STATEMENT
import com.soarex16.lua.lang.psi.LuaElementType.Companion.BREAK_STATEMENT
import com.soarex16.lua.lang.psi.LuaElementType.Companion.CONDITIONAL_STATEMENT
import com.soarex16.lua.lang.psi.LuaElementType.Companion.CONDITIONAL_STATEMENT_ELSEIF_BRANCH
import com.soarex16.lua.lang.psi.LuaElementType.Companion.CONDITIONAL_STATEMENT_ELSE_BRANCH
import com.soarex16.lua.lang.psi.LuaElementType.Companion.CONDITIONAL_STATEMENT_THEN_BRANCH
import com.soarex16.lua.lang.psi.LuaElementType.Companion.DO_STATEMENT
import com.soarex16.lua.lang.psi.LuaElementType.Companion.FUNCTION_CALL_STATEMENT
import com.soarex16.lua.lang.psi.LuaElementType.Companion.FUNCTION_DEFINITION_STATEMENT
import com.soarex16.lua.lang.psi.LuaElementType.Companion.GARBAGE_AT_THE_END_OF_FILE
import com.soarex16.lua.lang.psi.LuaElementType.Companion.LOCAL_FUNCTION_DEFINITION_STATEMENT
import com.soarex16.lua.lang.psi.LuaElementType.Companion.LOCAL_NAME_DEFINITION_STATEMENT
import com.soarex16.lua.lang.psi.LuaElementType.Companion.LUA_BLOCK
import com.soarex16.lua.lang.psi.LuaElementType.Companion.NAME_LIST
import com.soarex16.lua.lang.psi.LuaElementType.Companion.RANGE_FOR_LOOP_STATEMENT
import com.soarex16.lua.lang.psi.LuaElementType.Companion.REPEAT_STATEMENT
import com.soarex16.lua.lang.psi.LuaElementType.Companion.RETURN_STATEMENT
import com.soarex16.lua.lang.psi.LuaElementType.Companion.SIMPLE_FOR_LOOP_STATEMENT
import com.soarex16.lua.lang.psi.LuaElementType.Companion.STATEMENT_LIST
import com.soarex16.lua.lang.psi.LuaElementType.Companion.WHILE_STATEMENT
import com.soarex16.lua.lang.psi.LuaTokenType

/**
 * statement
 * : assignment_statement
 * | function_call_statement
 * | do_statement
 * | while_statement
 * | repeat_until_statement
 * | conditional_statement
 * | simple_for_statement
 * | range_for_statement
 * | function_definition_statement
 * | local_function_declaration_statement
 * | local_name_definition_statement
 * ;
 */
class LuaStatementParser(private val builder: PsiBuilder): ParserBase(builder) {
    fun parseStatement() : Marker? = when(builder.tokenType) { // падает на local list { }
        LuaTokenType.DO -> parseDoStatement()
        LuaTokenType.WHILE -> parseWhileStatement()
        LuaTokenType.REPEAT -> parseRepeatUntilStatement()
        LuaTokenType.IF -> parseConditionalStatement()
        LuaTokenType.FOR -> parseForStatement()
        LuaTokenType.FUNCTION -> parseFunctionStatement()
        LuaTokenType.LOCAL -> parseLocalDefinition()
        LuaTokenType.BREAK -> parseBreakStatement()
        LuaTokenType.RETURN -> parseReturnStatement()
        else -> parseOtherStatement()
    }

    // chunk ::= block EOF
    fun parseChunk() {
        parseBlock()
        parseEofGarbage()
    }

    fun parseBlock() {
        val mark = builder.mark()
        parseStatementList()
        mark.done(LUA_BLOCK)
    }

    // local_definition ::= local_function_definition_statement | local_name_definition_statement
    private fun parseLocalDefinition(): Marker? {
        return if (lookAhead(1) == LuaTokenType.FUNCTION)
            parseLocalFunctionDefinition()
        else
            parseLocalNameDefinition()
    }

    // local_function_definition_statement ::= 'local' 'function' NAME function_body
    private fun parseLocalFunctionDefinition(): Marker {
        val mark = builder.mark()

        expectAdvance(LuaTokenType.LOCAL) { "'local'" }
        expectAdvance(LuaTokenType.FUNCTION) { "'function'" }

        LuaExpressionParser(builder).parseFunctionBody()
        mark.done(LOCAL_FUNCTION_DEFINITION_STATEMENT)

        return mark
    }

    // local_name_definition_statement ::= 'local' name_list ('=' expression_list)?;
    private fun parseLocalNameDefinition(): Marker {
        val mark = builder.mark()

        expectAdvance(LuaTokenType.LOCAL) { "'local'" }
        parseNameList()

        if (builder.tokenType == LuaTokenType.ASSIGN) {
            advance() // =
            parseExpressionList()
        }

        mark.done(LOCAL_NAME_DEFINITION_STATEMENT)

        return mark
    }

    // function_definition_statement ::= 'function' function_name function_body
    private fun parseFunctionStatement(): Marker {
        val mark = builder.mark()

        expectAdvance(LuaTokenType.FUNCTION) { "'function'" }
        expectAdvance(LuaTokenType.IDENTIFIER) { "function name" }

        LuaExpressionParser(builder).parseFunctionBody()

        mark.done(FUNCTION_DEFINITION_STATEMENT)

        return mark
    }

    // for_statement ::= simple_for_statement | range_for_statement
    // simple_for_statement ::= 'for' NAME '=' expression ',' expression (',' expression)? 'do' block 'end'
    // range_for_statement ::= 'for' name_list 'in' expression_list 'do' block 'end'
    private fun parseForStatement(): Marker {
        val mark = builder.mark()

        expectAdvance(LuaTokenType.FOR) { "'for'" }

        val nameListMark = builder.mark()
        val namesCount = parseNameList(createElement = false)
        val loopType = if (namesCount > 1 || builder.tokenType == LuaTokenType.IN) { // range_for_statement
            nameListMark.done(NAME_LIST)

            expectAdvance(LuaTokenType.IN) { "'in'" }
            parseExpressionList()

            RANGE_FOR_LOOP_STATEMENT
        } else {
            nameListMark.drop()

            expectAdvance(LuaTokenType.ASSIGN) { "'='" } // =
            expectExpression() // expr

            expectAdvance(LuaTokenType.COMMA) { "','" } // ,
            expectExpression() // expr

            if (builder.tokenType == LuaTokenType.COMMA) {
                expectAdvance(LuaTokenType.COMMA) { "','" } // ,
                expectExpression() // expr
            }

            SIMPLE_FOR_LOOP_STATEMENT
        }

        expectAdvance(LuaTokenType.DO) { "'do'" }
        parseBlock()
        expectAdvance(LuaTokenType.END) { "'end'" }

        mark.done(loopType)
        return mark
    }

    // conditional_statement ::= 'if' expression 'then' block ('elseif' expression 'then' block)* ('else' block)? 'end'
    private fun parseConditionalStatement(): Marker {
        val mark = builder.mark()

        expectAdvance(LuaTokenType.IF) { "'if'" }
        expectExpression()
        expectAdvance(LuaTokenType.THEN) { "'then'" }
        val thenBranch = builder.mark()
        parseBlock()
        thenBranch.done(CONDITIONAL_STATEMENT_THEN_BRANCH)

        if (builder.tokenType == LuaTokenType.ELSEIF) {
            parseSome("elseif statement") {
                val elseIfMark = builder.mark()
                val elseIf = expectAdvance(LuaTokenType.ELSEIF) { "'elseif'" }
                if (elseIf) {
                    return@parseSome false
                }

                expectExpression()
                expectAdvance(LuaTokenType.THEN) { "'then'" }
                parseBlock()

                elseIfMark.done(CONDITIONAL_STATEMENT_ELSEIF_BRANCH)

                return@parseSome true
            }
        }

        if (builder.tokenType == LuaTokenType.ELSE) {
            val elseMark = builder.mark()
            expectAdvance(LuaTokenType.ELSE) { "'else'" }
            parseBlock()
            elseMark.done(CONDITIONAL_STATEMENT_ELSE_BRANCH)
        }

        expectAdvance(LuaTokenType.END) { "'end'" }

        mark.done(CONDITIONAL_STATEMENT)
        return mark
    }

    // repeat_until_statement ::= 'repeat' block 'until' expression
    private fun parseRepeatUntilStatement(): Marker {
        val mark = builder.mark()

        expectAdvance(LuaTokenType.REPEAT) { "'repeat'" }
        parseBlock()
        expectAdvance(LuaTokenType.UNTIL) { "'until'" }

        expectExpression()

        mark.done(REPEAT_STATEMENT)

        return mark
    }

    // while_statement ::= 'while' expression 'do' block 'end'
    private fun parseWhileStatement(): Marker {
        val mark = builder.mark()

        expectAdvance(LuaTokenType.WHILE) { "'while'" }

        expectExpression()

        expectAdvance(LuaTokenType.DO) { "'do'" }
        parseBlock()
        expectAdvance(LuaTokenType.END) { "'end'" }

        mark.done(WHILE_STATEMENT)

        return mark
    }

    // do_statement ::= 'do' block 'end'
    private fun parseDoStatement(): Marker {
        val mark = builder.mark()

        expectAdvance(LuaTokenType.DO) { "'do'" }
        parseBlock()

        expectAdvance(LuaTokenType.END) { "'end'" }
        mark.done(DO_STATEMENT)

        return mark
    }

    // break_statement ::= 'break'
    private fun parseBreakStatement(): Marker {
        val mark = builder.mark()
        advance() // break
        mark.done(BREAK_STATEMENT)
        return mark
    }

    // return_statement ::= 'return' expression_list? ';'?
    private fun parseReturnStatement(): Marker {
        val mark = builder.mark()
        advance() // return

        parseExpressionList(canBeEmpty = true) // expression_list

        if (builder.tokenType == LuaTokenType.SEMICOLON) {
            advance() // optional ';'
        }

        mark.done(RETURN_STATEMENT)
        return mark
    }

    // other_statement ::= assignment_statement | function_call_statement
    // assignment_statement ::= variable_list '=' expression_list
    // function_call_statement ::= variable_or_expression name_and_arguments+
    private fun parseOtherStatement(): Marker? {
        if (builder.tokenType != LuaTokenType.IDENTIFIER) {
            return null
        }

        val callStatement = parseFunctionCallStatement()
        if (callStatement != null)
            return callStatement

        val mark = builder.mark()
        val expressionParser = LuaExpressionParser(builder)

        expressionParser.parseVariableList() // variable_list
        expectAdvance(LuaTokenType.ASSIGN) { "'='" } // =
        parseExpressionList() // expression_list

        mark.done(ASSIGNMENT_STATEMENT)
        return mark
    }

    private fun parseFunctionCallStatement(): Marker? {
        val mark = builder.mark()

        val expressionParser = LuaExpressionParser(builder)
        val parsedPrefix = expressionParser.parsePrefixExpression(includeFunctionCall = false)
        return if (parsedPrefix != null && builder.tokenType == LuaTokenType.L_PAREN) {
            expressionParser.parseCallExpression(parsedPrefix)
            mark.done(FUNCTION_CALL_STATEMENT)
            mark
        } else {
            mark.rollbackTo()
            null
        }
    }

    // statement_list ::= (statement ';')*
    private fun parseStatementList(): Marker {
        val statementListMark = builder.mark()

        var mark = parseStatement()
        while (mark != null) {
            if (builder.tokenType == LuaTokenType.SEMICOLON) { // optional semicolons
                advance()
            }

            mark = parseStatement()
        }

        statementListMark.done(STATEMENT_LIST)
        return statementListMark
    }

    private fun parseExpressionList(canBeEmpty: Boolean = false) {
        LuaExpressionParser(builder).parseExpressionList(canBeEmpty)
    }

    private fun expectExpression() {
        LuaExpressionParser(builder).expectExpression()
    }

    private fun parseEofGarbage() {
        if (builder.tokenType == null)
            return

        val mark = builder.mark()
        builder.error("Expected statement")

        while (builder.tokenType != null) {
            when (builder.tokenType) {
                // если встретили какой-то хороший токен - пробуем попарсить
                LuaTokenType.FUNCTION, LuaTokenType.IDENTIFIER, LuaTokenType.DO, LuaTokenType.WHILE,
                LuaTokenType.REPEAT, LuaTokenType.IF, LuaTokenType.FOR -> parseStatementList()
                else -> advance()
            }
        }

        mark.done(GARBAGE_AT_THE_END_OF_FILE)
    }
}