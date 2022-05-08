package com.soarex16.lua.lang

import com.intellij.lexer.FlexAdapter

class LuaLexer : FlexAdapter(com.soarex16.lua.lang.lexer._LuaLexer(null))