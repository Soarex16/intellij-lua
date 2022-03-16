package com.soarex16.lua.lang

import com.soarex16.lua.LuaIcons
import com.intellij.lang.Language
import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.vfs.VirtualFile
import java.nio.charset.StandardCharsets
import javax.swing.Icon

object LuaLanguage : Language("Lua")

object LuaFileType : LanguageFileType(LuaLanguage) {
    override fun getName(): String = "Lua"

    override fun getDescription(): String = "Lua"

    override fun getDefaultExtension(): String = "lua"

    override fun getIcon(): Icon = LuaIcons.FILE

    override fun getCharset(file: VirtualFile, content: ByteArray?): String = StandardCharsets.UTF_8.name()
}