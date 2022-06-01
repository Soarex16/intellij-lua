package com.soarex16.lua.lang.psi

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet
import com.soarex16.lua.lang.LuaLanguage
import org.jetbrains.annotations.NonNls

class LuaElementType(@NonNls debugName: String) : IElementType(debugName, LuaLanguage) {
    companion object {
        val LUA_CHUNK_FILE: IFileElementType = IFileElementType(LuaLanguage)

        //region Statements
        val LUA_BLOCK = LuaElementType("LUA_BLOCK")
        val STATEMENT_LIST = LuaElementType("STATEMENT_LIST")

        // varlist `=` explist
        val ASSIGNMENT_STATEMENT = LuaElementType("ASSIGNMENT_STATEMENT")

        // laststat ::= return [explist] | break
        val RETURN_STATEMENT = LuaElementType("RETURN_STATEMENT")
        val BREAK_STATEMENT = LuaElementType("BREAK_STATEMENT")

        // do block end
        val DO_STATEMENT = LuaElementType("DO_STATEMENT")

        // functioncall
        val FUNCTION_CALL_STATEMENT = LuaElementType("FUNCTION_CALL_STATEMENT")

        // function_expression (closure)
        val FUNCTION_EXPRESSION = LuaElementType("FUNCTION_EXPRESSION")

        // if exp then block {elseif exp then block} [else block] end
        val CONDITIONAL_STATEMENT = LuaElementType("CONDITIONAL_STATEMENT")
        val CONDITIONAL_STATEMENT_THEN_BRANCH = LuaElementType("CONDITIONAL_STATEMENT_THEN_BRANCH")
        val CONDITIONAL_STATEMENT_ELSEIF_BRANCH = LuaElementType("CONDITIONAL_STATEMENT_ELSEIF_BRANCH")
        val CONDITIONAL_STATEMENT_ELSE_BRANCH = LuaElementType("CONDITIONAL_STATEMENT_ELSE_BRANCH")

        //region Loops
        // while exp do block end
        val WHILE_STATEMENT = LuaElementType("while")
        // repeat block until exp
        val REPEAT_STATEMENT = LuaElementType("REPEAT_STATEMENT")
        // for Name `=` exp `,` exp [`,` exp] do block end
        val SIMPLE_FOR_LOOP_STATEMENT = LuaElementType("SIMPLE_FOR_LOOP_STATEMENT")
        // for namelist in explist do block end
        val RANGE_FOR_LOOP_STATEMENT = LuaElementType("RANGE_FOR_LOOP_STATEMENT")
        //endregion

        // function funcname funcbody
        val FUNCTION_DEFINITION_STATEMENT = LuaElementType("FUNCTION_DEFINITION_STATEMENT")
        // local function Name funcbody
        val LOCAL_FUNCTION_DEFINITION_STATEMENT = LuaElementType("FUNCTION_DEFINITION_STATEMENT")
        // local namelist [`=` explist]
        val LOCAL_NAME_DEFINITION_STATEMENT = LuaElementType("LOCAL_ASSIGNMENT_STATEMENT")

        val LOCAL_STATEMENTS = TokenSet.create(LOCAL_FUNCTION_DEFINITION_STATEMENT, LOCAL_NAME_DEFINITION_STATEMENT)
        val LAST_STATEMENTS = TokenSet.create(RETURN_STATEMENT, BREAK_STATEMENT)
        val STATEMENTS = TokenSet.create(ASSIGNMENT_STATEMENT, FUNCTION_CALL_STATEMENT, DO_STATEMENT,
            WHILE_STATEMENT, REPEAT_STATEMENT, SIMPLE_FOR_LOOP_STATEMENT, RANGE_FOR_LOOP_STATEMENT,
            CONDITIONAL_STATEMENT,
            FUNCTION_DEFINITION_STATEMENT,
            *LOCAL_STATEMENTS.types,
            *LAST_STATEMENTS.types)
        //endregion

        //region Names
        // var ::=  Name | prefixexp `[` exp `]` | prefixexp `.` Name
        val NAME = LuaElementType("NAME")
        val NAME_LIST = LuaElementType("NAME_LIST")
        val NAME_EXPR = LuaElementType("NAME_EXPR")
        val VARIABLE = LuaElementType("VARIABLE")
        val VARIABLE_LIST = LuaElementType("VARIABLE_LIST")
        //endregion

        // Expressions
        val PREFIX_EXPRESSION = LuaElementType("PREFIX_EXPRESSION")
        val INDEX_EXPRESSION = LuaElementType("INDEX_EXPRESSION")
        val EXPRESSION_LIST = LuaElementType("EXPRESSION_LIST")

        //region Literals and built-in constants
        // false | true
        val BOOLEAN_CONSTANT = LuaElementType("BOOLEAN_CONSTANT")
        // nil
        val NIL = LuaElementType("NIL")
        // Number
        val NUMBER_LITERAL = LuaElementType("NUMBER_LITERAL")
        // String
        val STRING_LITERAL = LuaElementType("STRING_LITERAL")
        // "..."
        val ELLIPSIS = LuaElementType("ELLIPSIS")
        val LITERAL_EXPRESSIONS = TokenSet.create(
            NIL, BOOLEAN_CONSTANT, NUMBER_LITERAL, STRING_LITERAL, ELLIPSIS
        )
        //endregion

        // Binary expressions
        // exp binop exp
        val BINARY_EXPRESSION = LuaElementType("BINARY_EXPRESSION")
        // binop ::= `+` | `-` | `*` | `/` | `//` | `^` | `%` | `..` |
        //     `<<` | `>>` | `|` | `&` | `~`
        //     `<` | `<=` | `>` | `>=` | `==` | `~=` |
        //     and | or
        val BINARY_OPERATOR = LuaElementType("BINARY_OPERATOR")

        // Unary expressions
        // unop exp
        val UNARY_EXPRESSION = LuaElementType("UNARY_EXPRESSION")
        // unop ::= `-` | not | `#`
        val UNARY_OPERATOR = LuaElementType("UNARY_OPERATOR")

        // `...` currently not supported
        // val DOT_DOT_DOT_EXPRESSION = LuaElementType("DOT_DOT_DOT_EXPRESSION")

        // function
        val FUNCTION_CALL_EXPRESSION = LuaElementType("FUNCTION_CALL_EXPRESSION")

        val EXPRESSIONS = TokenSet.create(
            *LITERAL_EXPRESSIONS.types,
            BINARY_EXPRESSION, UNARY_EXPRESSION
        )

        //region Tables
        // table constructor
        val TABLE_EXPRESSION = LuaElementType("TABLE_EXPR")
        val TABLE_FIELD = LuaElementType("TABLE_FIELD")
        val TABLE_FIELD_SEPARATOR = LuaElementType("TABLE_FIELD_SEPARATOR")
        //endregion

        //region Functions
        val FUNCTION_PARAMETERS_LIST = LuaElementType("FUNCTION_PARAMETERS_LIST")
        val FUNCTION_ARGUMENTS_LIST = LuaElementType("FUNCTION_ARGUMENTS_LIST")
        val FUNCTION_BODY = LuaElementType("FUNCTION_BODY")
        //endregion

        val GARBAGE_AT_THE_END_OF_FILE = LuaElementType("GARBAGE_AT_THE_END_OF_FILE")

        fun createElement(node: ASTNode): PsiElement = when(node.elementType) {
            else -> ASTWrapperPsiElement(node)
        }
    }
}