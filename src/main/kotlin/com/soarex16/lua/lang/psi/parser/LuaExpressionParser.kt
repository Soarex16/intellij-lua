package com.soarex16.lua.lang.psi.parser

import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiBuilder.Marker
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import com.soarex16.lua.lang.psi.LuaElementType
import com.soarex16.lua.lang.psi.LuaElementType.Companion.BINARY_EXPRESSION
import com.soarex16.lua.lang.psi.LuaElementType.Companion.BINARY_OPERATOR
import com.soarex16.lua.lang.psi.LuaElementType.Companion.BOOLEAN_CONSTANT
import com.soarex16.lua.lang.psi.LuaElementType.Companion.ELLIPSIS
import com.soarex16.lua.lang.psi.LuaElementType.Companion.EXPRESSION_LIST
import com.soarex16.lua.lang.psi.LuaElementType.Companion.FUNCTION_ARGUMENTS_LIST
import com.soarex16.lua.lang.psi.LuaElementType.Companion.FUNCTION_BODY
import com.soarex16.lua.lang.psi.LuaElementType.Companion.FUNCTION_CALL_EXPRESSION
import com.soarex16.lua.lang.psi.LuaElementType.Companion.FUNCTION_PARAMETERS_LIST
import com.soarex16.lua.lang.psi.LuaElementType.Companion.INDEX_EXPRESSION
import com.soarex16.lua.lang.psi.LuaElementType.Companion.LITERAL_EXPRESSIONS
import com.soarex16.lua.lang.psi.LuaElementType.Companion.NAME_EXPR
import com.soarex16.lua.lang.psi.LuaElementType.Companion.NIL
import com.soarex16.lua.lang.psi.LuaElementType.Companion.NUMBER_LITERAL
import com.soarex16.lua.lang.psi.LuaElementType.Companion.PREFIX_EXPRESSION
import com.soarex16.lua.lang.psi.LuaElementType.Companion.STRING_LITERAL
import com.soarex16.lua.lang.psi.LuaElementType.Companion.TABLE_EXPRESSION
import com.soarex16.lua.lang.psi.LuaElementType.Companion.TABLE_FIELD
import com.soarex16.lua.lang.psi.LuaElementType.Companion.TABLE_FIELD_SEPARATOR
import com.soarex16.lua.lang.psi.LuaElementType.Companion.UNARY_EXPRESSION
import com.soarex16.lua.lang.psi.LuaElementType.Companion.UNARY_OPERATOR
import com.soarex16.lua.lang.psi.LuaElementType.Companion.VARIABLE
import com.soarex16.lua.lang.psi.LuaElementType.Companion.VARIABLE_LIST
import com.soarex16.lua.lang.psi.LuaTokenType

private enum class ExpressionType(val ops: TokenSet) {
    // Operator precedence in Lua follows the table below, from the lower to the higher priority
    OR(LuaTokenType.OR),
    AND(LuaTokenType.AND),
    RELATIONAL(
        LuaTokenType.LESS,
        LuaTokenType.LESS_EQUAL,
        LuaTokenType.GREATER,
        LuaTokenType.GREATER_EQUAL,
        LuaTokenType.EQUAL,
        LuaTokenType.NOT_EQUAL,
    ),
    BIT_OR(LuaTokenType.BOR),
    BIT_AND(LuaTokenType.BAND),
    BIT_SHIFT(LuaTokenType.SHL, LuaTokenType.SHR),
    CONCAT(LuaTokenType.DOTDOT),
    ADDITIVE(LuaTokenType.ADD, LuaTokenType.SUB),
    MULTIPLICATIVE(LuaTokenType.MUL, LuaTokenType.DIV, LuaTokenType.IDIV, LuaTokenType.MOD),
    UNARY(LuaTokenType.NOT, LuaTokenType.HASH, LuaTokenType.SUB, LuaTokenType.BNOT),
    POWER(LuaTokenType.POW),
    ATOM();

    constructor(vararg tokens: IElementType) : this(TokenSet.create(*tokens))
}

/**
 * expression
 * : 'nil' | 'false' | 'true'
 * | number
 * | string
 * | function_expression
 * | prefix_expression
 * | table_constructor
 * | <assoc=right> expression operatorPower expression
 * | unary_operator expression
 * | expression mul_div_mod_operator expression
 * | expression add_sub_operator expression
 * | <assoc=right> expression string_concat_operator expression
 * | expression comparison_operator expression
 * | expression add_operator expression
 * | expression or_operator expression
 * | expression bitwise_operator expression
 * ;
 */
class LuaExpressionParser(private val builder: PsiBuilder) : ParserBase(builder) {
    fun parseExpression(): Marker? {
        return parseExpression(ExpressionType.OR)
    }

    private fun parseExpression(type: ExpressionType): Marker? = when (type) {
        ExpressionType.OR -> parseBinaryExpression(type, ExpressionType.AND)
        ExpressionType.AND -> parseBinaryExpression(type, ExpressionType.RELATIONAL)
        ExpressionType.RELATIONAL -> parseBinaryExpression(type, ExpressionType.BIT_OR)
        ExpressionType.BIT_OR -> parseBinaryExpression(type, ExpressionType.BIT_AND)
        ExpressionType.BIT_AND -> parseBinaryExpression(type, ExpressionType.BIT_SHIFT)
        ExpressionType.BIT_SHIFT -> parseBinaryExpression(type, ExpressionType.CONCAT)
        ExpressionType.CONCAT -> parseBinaryExpression(type, ExpressionType.ADDITIVE)
        ExpressionType.ADDITIVE -> parseBinaryExpression(type, ExpressionType.MULTIPLICATIVE)
        ExpressionType.MULTIPLICATIVE -> parseBinaryExpression(type, ExpressionType.UNARY)
        ExpressionType.UNARY -> parseUnaryExpression(type, ExpressionType.POWER)
        ExpressionType.POWER -> parseUnaryExpression(type, ExpressionType.ATOM)
        ExpressionType.ATOM -> parseAtomicExpression()
    }

    // binary_expression ::= expression bin_op expression
    private fun parseBinaryExpression(type: ExpressionType, next: ExpressionType): Marker? {
        var mark = parseExpression(next) ?: return null

        /**
         * Unroll left recursive rule into loop as proposed in
         * https://matklad.github.io/2020/04/13/simple-but-powerful-pratt-parsing.html
         */
        while (true) {
            if (builder.tokenType in type.ops) {
                val operator = builder.mark()
                advance()
                operator.done(BINARY_OPERATOR)

                val right = parseExpression(next)
                if (right == null) {
                    error("Expected expression")
                }

                mark = mark.precede() // handle associativity
                mark.done(BINARY_EXPRESSION)

                // previously we putted mark, so we
                // handled this bad input ``correctly``
                if (right == null) {
                    break
                }
            } else break
        }

        return mark
    }

    // unary_expression ::= un_op expression
    private fun parseUnaryExpression(type: ExpressionType, next: ExpressionType): Marker? {
        if (builder.tokenType !in type.ops)
            return parseExpression(next)

        val mark = builder.mark()

        val operator = builder.mark()
        advance()
        operator.done(UNARY_OPERATOR)

        // There is no left recursion, so we can make recursive call here
        val inner = parseUnaryExpression(type, next)
        if (inner == null) {
            error("Expected expression")
        }

        mark.done(UNARY_EXPRESSION)
        return mark
    }

    // atomicExpression ::= literal_expression | table_expression | prefix_expression | function_expression
    fun parseAtomicExpression(): Marker? {
        val literalMark = parseLiteralExpression()
        if (literalMark != null) {
            return literalMark
        }

        val tableMark = parseTableExpression()
        if (tableMark != null) {
            return tableMark
        }

        val functionMark = parseFunctionExpression()
        if (functionMark != null) {
            return functionMark
        }

        return parsePrefixExpression()
    }

    // function_expression ::= 'function' function_body
    private fun parseFunctionExpression(): Marker? {
        val mark = builder.mark()

        if (builder.tokenType != LuaTokenType.FUNCTION) {
            mark.drop()
            return null
        }

        advance() // function
        parseFunctionBody()

        mark.done(LuaElementType.FUNCTION_EXPRESSION)
        return mark
    }

    // function_body ::= '(' function_parameters_list? ')' block 'end'
    fun parseFunctionBody(): Marker {
        val mark = builder.mark()

        expectAdvance(LuaTokenType.L_PAREN) { "'('" }
        parseFunctionParametersList()
        expectAdvance(LuaTokenType.R_PAREN) { "')'" }

        LuaStatementParser(builder).parseBlock()
        expectAdvance(LuaTokenType.END) { "'end'" }

        mark.done(FUNCTION_BODY)
        return mark
    }

    // function_parameters_list ::= name_list
    // name_list ::= NAME (',' NAME)*
    private fun parseFunctionParametersList(): Marker {
        val mark = builder.mark()
        parseNameList(isDeclaration = true)
        mark.done(FUNCTION_PARAMETERS_LIST)

        return mark
    }

    // prefix_expression ::= variable | '(' expression ')' | function_call_expression
    fun parsePrefixExpression(includeFunctionCall: Boolean = true): Marker? {
        val mark = builder.mark()
        var prefix = choice(
            this::parseParensExpression,
            this::parseName,
        )
        var prefixType = NAME_EXPR
        var inputConsumed = false

        while (prefix != null) {
            inputConsumed = true
            val suffix = parseIndexExpression(prefix)
            if (suffix != null) {
                prefixType = INDEX_EXPRESSION
                prefix = suffix
            } else if (includeFunctionCall && prefixApplicableForCall(prefixType)) {
                prefixType = FUNCTION_CALL_EXPRESSION
                prefix = parseCallExpression(prefix)
            } else {
                break
            }
        }

        return if (inputConsumed) {
            mark.done(PREFIX_EXPRESSION)
            mark
        } else {
            mark.drop()
            null
        }
    }

    private fun parseIndexExpression(prefix: Marker): Marker? = when (builder.tokenType) {
        LuaTokenType.DOT, LuaTokenType.COLON -> {
            advance()
            expectAdvance(LuaTokenType.IDENTIFIER) { "identifier" }
            val mark = prefix.precede()
            mark.done(INDEX_EXPRESSION)
            mark
        }
        LuaTokenType.L_BRACKET -> {
            expectAdvance(LuaTokenType.L_BRACKET) { "'['" }
            expectExpression()
            expectAdvance(LuaTokenType.R_BRACKET) { "']'" }
            val mark = prefix.precede()
            mark.done(INDEX_EXPRESSION)
            mark
        }
        else -> null
    }

    private fun prefixApplicableForCall(type: IElementType): Boolean =
        type != TABLE_EXPRESSION || type !in LITERAL_EXPRESSIONS

    private fun parseParensExpression(): Marker? {
        if (!expectAdvance(LuaTokenType.L_PAREN) { "'('" }) return null
        val marker = parseExpression()
        if (marker == null) {
            error("Expected expression")
        }
        expectAdvance(LuaTokenType.L_PAREN) { "')'" }
        return marker
    }

    // call_expression ::= name_and_arguments+
    fun parseCallExpression(prefix: Marker): Marker? {
        return if (builder.tokenType == LuaTokenType.L_PAREN) {
            parseFunctionArguments()
            val mark = prefix.precede()
            mark.done(FUNCTION_CALL_EXPRESSION)
            mark
        } else {
            null
        }
    }

    // variable ::= NAME | prefix_expression '[' expression ']' | prefix_expression '.' NAME
    fun parseVariable(): Marker? {
        val mark = builder.mark()

        val parseResult = parsePrefixExpression(includeFunctionCall = false)
        return if (parseResult == null) {
            mark.rollbackTo()
            null
        } else {
            mark.done(VARIABLE)
            mark
        }
    }

    // For simplicity only expression list
    // function_arguments ::= '(' expression_list? ')' | table_constructor | string
    private fun parseFunctionArguments(): Marker {
        val mark = builder.mark()

        expectAdvance(LuaTokenType.L_PAREN) { "'('" } // (
        parseExpressionList(true)
        expectAdvance(LuaTokenType.R_PAREN) { "')'" } // )

        mark.done(FUNCTION_ARGUMENTS_LIST)
        return mark
    }

    // table_constructor ::= '{' table_field_list? '}'
    private fun parseTableExpression(): Marker? {
        if (builder.tokenType != LuaTokenType.L_BRACE)
            return null

        val mark = builder.mark()
        expectAdvance(LuaTokenType.L_BRACE) { "'{'" }
        parseTableFieldList()
        expectAdvance(LuaTokenType.R_BRACE) { "'}'" }
        mark.done(TABLE_EXPRESSION)

        return mark
    }

    // table_field_list ::= field (field_separator field)* field_separator?
    private fun parseTableFieldList(): Boolean = separatedBy(
        allowDanglingSeparator = true,
        "field separator (',' or ';')",
        this::parseTableFieldSeparator,
        "field"
    ) {
        parseTableField() != null
    }

    // variable_list ::= variable (',' variable)*
    fun parseVariableList(): Boolean {
        val mark = builder.mark()

        val consumed = separatedBy(
            allowDanglingSeparator = false,
            "','",
            this::parseComma,
            "variable"
        ) { parseVariable() != null }

        if (!consumed) {
            mark.rollbackTo()
        } else {
            mark.done(VARIABLE_LIST)
        }

        return consumed
    }

    // field_separator ::= ',' | ';'
    private fun parseTableFieldSeparator(): Boolean {
        val mark = builder.mark()
        if (builder.tokenType in LuaTokenType.FIELD_SEPARATORS) {
            advance()
            mark.done(TABLE_FIELD_SEPARATOR)
            return true
        } else {
            mark.drop()
        }

        return false
    }

    // field ::= expression | NAME '=' expression | '[' expression ']' '=' expression
    private fun parseTableField(): Marker? {
        when (builder.tokenType) {
            LuaTokenType.IDENTIFIER -> { // NAME '=' expression
                val mark = builder.mark()
                advance() // ID

                if (builder.tokenType != LuaTokenType.ASSIGN) { // rollback to 'expression' alternative
                    mark.rollbackTo()
                } else {
                    advance() // =
                    expectExpression() // expr
                    mark.done(TABLE_FIELD)
                    return mark
                }
            }
            LuaTokenType.L_BRACKET -> { // '[' expression ']' '=' expression
                val mark = builder.mark()
                advance() // [

                expectExpression() // expr
                expectAdvance(LuaTokenType.R_BRACKET) { "']'" } // ]
                expectAdvance(LuaTokenType.ASSIGN) { "'='" } // =
                expectExpression() // expr

                mark.done(TABLE_FIELD)
                return mark
            }
        }

        // 'expression' alternative
        val expr = parseExpression()
        if (expr != null) {
            val mark = expr.precede()
            mark.done(TABLE_FIELD)
        }

        return expr
    }

    // expression_list ::= expression (',' expression)*
    fun parseExpressionList(canBeEmpty: Boolean = false): Boolean {
        val mark = builder.mark()
        val consumed = separatedBy(
            allowDanglingSeparator = false,
            "','",
            this::parseComma,
            "expression",
        ) {
            parseExpression() != null
        }
        if (!canBeEmpty && !consumed) {
            builder.error("Expression expected")
        }

        if (!consumed) {
            mark.drop()
        } else {
            mark.done(EXPRESSION_LIST)
        }

        return consumed
    }

    // literalExpr ::= 'nil' | 'false' | 'true' | NUMBER | STRING | '...'
    fun parseLiteralExpression(): Marker? {
        val mark = builder.mark()
        when (builder.tokenType) {
            LuaTokenType.NIL -> mark.done(NIL)
            LuaTokenType.FALSE, LuaTokenType.TRUE -> mark.done(BOOLEAN_CONSTANT)
            LuaTokenType.DOTDOTDOT -> mark.done(ELLIPSIS)
            in LuaTokenType.NUMBERS -> mark.done(NUMBER_LITERAL)
            in LuaTokenType.STRINGS -> mark.done(STRING_LITERAL)
            else -> {
                mark.drop()
                return null
            }
        }

        advance()
        return mark
    }

    fun expectExpression(): Marker? {
        val expr = parseExpression()
        if (expr == null) {
            builder.error("Expression expected")
        }
        return expr
    }
}