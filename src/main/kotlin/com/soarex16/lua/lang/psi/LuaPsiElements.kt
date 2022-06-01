package com.soarex16.lua.lang.psi

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.*
import com.intellij.psi.util.descendantsOfType
import com.soarex16.lua.lang.LuaFileType

abstract class LuaPsiElement(node: ASTNode) : ASTWrapperPsiElement(node) {
    override fun getContainingFile(): PsiFile? = super.getContainingFile() as? LuaFile
}

interface LuaNamedElement : PsiNameIdentifierOwner

class LuaNameDeclaration(node: ASTNode) : LuaPsiElement(node), LuaNamedElement {
    override fun setName(name: String): PsiElement {
        val newIdentifier = PsiFileFactory
            .getInstance(project)
            .createFileFromText("tmp.lua", LuaFileType, "$name = nil")
            .descendantsOfType<LuaNameDeclaration>()
            .first()
            .nameIdentifier!!
        nameIdentifier!!.replace(newIdentifier)
        return this
    }

    override fun getNameIdentifier(): PsiElement? = findChildByType(LuaTokenType.IDENTIFIER)

    override fun getName(): String = nameIdentifier?.text.orEmpty()
}

class LuaPrefixExpression(node: ASTNode) : LuaPsiElement(node)

class LuaAssignmentStatement(node: ASTNode) : LuaPsiElement(node)
class LuaFunctionDefinitionStatement(node: ASTNode) : LuaPsiElement(node)

class LuaSimpleForLoopStatement(node: ASTNode) : LuaPsiElement(node)

class LuaBlock(node: ASTNode) : LuaPsiElement(node)
class LuaChunk(node: ASTNode) : LuaPsiElement(node)

class LuaNameReference(node: ASTNode) : LuaPsiElement(node) {
    override fun getReference(): PsiReference? {
        // we don't support compound prefix expression names resolving because it's too complicated
        if (parent is LuaPrefixExpression && parent.descendantsOfType<LuaNameReference>().count() > 1)
            return null

        val nameToFind = findChildByType<PsiElement>(LuaTokenType.IDENTIFIER)?.text ?: return null
        var currentScope: PsiElement? = this
        while (currentScope != null) {
            // for, function, local assignment, local function, assignment
            when (currentScope) {
                is LuaChunk, is LuaBlock, is LuaFunctionDefinitionStatement, is LuaSimpleForLoopStatement -> {
                    val referenceTarget = currentScope
                        .descendantsOfType<LuaNameDeclaration>()
                        .find { it.name == nameToFind }

                    if (referenceTarget != null) {
                        return ref(referenceTarget)
                    }
                }
            }
            currentScope = currentScope.parent
        }

        return null
    }

    private fun ref(element: PsiElement): PsiReference =
        object : PsiReferenceBase<PsiElement>(this, this.textRangeInParent) {
            override fun resolve(): PsiElement = element
        }
}
