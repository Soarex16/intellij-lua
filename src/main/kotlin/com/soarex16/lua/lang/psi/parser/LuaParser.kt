package com.soarex16.lua.lang.psi.parser

import com.intellij.lang.ASTNode
import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiParser
import com.intellij.psi.tree.IElementType

class LuaParser : PsiParser {
    override fun parse(root: IElementType, builder: PsiBuilder): ASTNode {
        builder.setDebugMode(true) // unbalanced tree
        LuaStatementParser(builder).parseChunk()
        return builder.treeBuilt
    }
}