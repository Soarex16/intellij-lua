package com.soarex16.lua.lang.psi

import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.lang.PsiParser
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet
import com.soarex16.lua.lang.LuaLexer
import com.soarex16.lua.lang.psi.parser.LuaParser

class LuaParserDefinition : ParserDefinition {
    override fun createLexer(project: Project?): Lexer = LuaLexer()

    override fun createParser(project: Project?): PsiParser = LuaParser()

    override fun getFileNodeType(): IFileElementType = LuaElementType.LUA_CHUNK_FILE

    override fun getCommentTokens(): TokenSet = LuaTokenType.COMMENTS

    override fun getStringLiteralElements(): TokenSet = LuaTokenType.STRINGS

    override fun createElement(node: ASTNode): PsiElement = LuaElementType.createElement(node)

    override fun createFile(viewProvider: FileViewProvider): PsiFile = LuaFile(viewProvider)
}