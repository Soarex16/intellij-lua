package com.soarex16.lua.lang.psi

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider
import com.soarex16.lua.lang.LuaFileType
import com.soarex16.lua.lang.LuaLanguage

class LuaFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, LuaLanguage) {
    override fun getFileType(): FileType = LuaFileType
}