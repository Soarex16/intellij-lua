package com.soarex16.lua

import com.intellij.application.options.CodeStyleAbstractPanel
import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.StringEscapesTokenTypes
import com.intellij.psi.tree.IElementType
import com.soarex16.lua.lang.LuaFileType
import com.soarex16.lua.lang.LuaLanguage
import com.soarex16.lua.lang.LuaLexer
import com.soarex16.lua.lang.psi.LuaTokenType

enum class LuaTextAttributeKeys(humanName: String, fallback: TextAttributesKey) {
    COMMENT("Comment", DefaultLanguageHighlighterColors.DOC_COMMENT),
    STRING("String//String text", DefaultLanguageHighlighterColors.STRING),
    CONSTANT("Built in constants", DefaultLanguageHighlighterColors.CONSTANT),
    NUMBER("Number//Number", DefaultLanguageHighlighterColors.NUMBER),
    KEYWORD("Keyword", DefaultLanguageHighlighterColors.KEYWORD),

    DOUBLE_COLON("Braces and Operators//Double colon", DefaultLanguageHighlighterColors.DOT),
    SEMICOLON("Braces and Operators//Semicolon", DefaultLanguageHighlighterColors.SEMICOLON),
    COLON("Braces and Operators//Colon", DefaultLanguageHighlighterColors.DOT),
    COMMA("Braces and Operators//Comma", DefaultLanguageHighlighterColors.COMMA),
    DOT("Braces and Operators//Dot", DefaultLanguageHighlighterColors.DOT),
    DOTDOTDOT("Braces and Operators//Dot dot dot", DefaultLanguageHighlighterColors.DOT),

    OPERATOR("Braces and Operators//Operators", DefaultLanguageHighlighterColors.OPERATION_SIGN),

    PARENTHESES("Braces and Operators//Parentheses", DefaultLanguageHighlighterColors.PARENTHESES),
    BRACKETS("Braces and Operators//Brackets", DefaultLanguageHighlighterColors.BRACKETS),
    BRACES("Braces and Operators//Braces", DefaultLanguageHighlighterColors.BRACES),

    VALID_STRING_ESCAPE("String//Escape Sequence//Valid", DefaultLanguageHighlighterColors.VALID_STRING_ESCAPE),
    INVALID_STRING_ESCAPE("String//Escape Sequence//Invalid", DefaultLanguageHighlighterColors.INVALID_STRING_ESCAPE),
    IDENTIFIER("Identifier", DefaultLanguageHighlighterColors.IDENTIFIER);

    val key = TextAttributesKey.createTextAttributesKey("Lua.$name", fallback)
    val descriptor = AttributesDescriptor(humanName, key)
}

class LuaColorSettingsPage : ColorSettingsPage {
    override fun getDisplayName() = LuaLanguage.displayName
    override fun getIcon() = LuaFileType.icon
    override fun getAttributeDescriptors() = LuaTextAttributeKeys.values().map { it.descriptor }.toTypedArray()
    override fun getColorDescriptors(): Array<ColorDescriptor> = ColorDescriptor.EMPTY_ARRAY
    override fun getHighlighter() = LuaSyntaxHighlighter()

    override fun getAdditionalHighlightingTagToDescriptorMap(): Map<String, TextAttributesKey> {
        return LuaTextAttributeKeys.values().associateBy({ it.name }, { it.key })
    }

    private val DEMO_TEXT = CodeStyleAbstractPanel.readFromFile(LuaLanguage::class.java, "Sample.lua")
    override fun getDemoText(): String = DEMO_TEXT
}

class LuaSyntaxHighlighter : SyntaxHighlighterBase() {
    companion object {
        val elementTextAttributes = HashMap<IElementType, TextAttributesKey>()
    }

    init {
        fillMap(elementTextAttributes, LuaTokenType.KEYWORDS, LuaTextAttributeKeys.KEYWORD.key)
        fillMap(elementTextAttributes, LuaTokenType.OPERATORS, LuaTextAttributeKeys.OPERATOR.key)
        fillMap(elementTextAttributes, LuaTokenType.NUMBERS, LuaTextAttributeKeys.NUMBER.key)
        fillMap(elementTextAttributes, LuaTokenType.COMMENTS, LuaTextAttributeKeys.COMMENT.key)
        fillMap(elementTextAttributes, LuaTokenType.STRINGS, LuaTextAttributeKeys.STRING.key)
        fillMap(elementTextAttributes, LuaTokenType.CONSTANTS, LuaTextAttributeKeys.CONSTANT.key)

        elementTextAttributes[LuaTokenType.L_PAREN] = LuaTextAttributeKeys.PARENTHESES.key
        elementTextAttributes[LuaTokenType.R_PAREN] = LuaTextAttributeKeys.PARENTHESES.key

        elementTextAttributes[LuaTokenType.L_BRACE] = LuaTextAttributeKeys.BRACES.key
        elementTextAttributes[LuaTokenType.R_BRACE] = LuaTextAttributeKeys.BRACES.key

        elementTextAttributes[LuaTokenType.L_BRACKET] = LuaTextAttributeKeys.BRACKETS.key
        elementTextAttributes[LuaTokenType.R_BRACKET] = LuaTextAttributeKeys.BRACKETS.key

        elementTextAttributes[LuaTokenType.IDENTIFIER] = LuaTextAttributeKeys.IDENTIFIER.key

        elementTextAttributes[LuaTokenType.DOUBLE_COLON] = LuaTextAttributeKeys.DOUBLE_COLON.key
        elementTextAttributes[LuaTokenType.SEMICOLON] = LuaTextAttributeKeys.SEMICOLON.key
        elementTextAttributes[LuaTokenType.COLON] = LuaTextAttributeKeys.COLON.key
        elementTextAttributes[LuaTokenType.COMMA] = LuaTextAttributeKeys.COMMA.key
        elementTextAttributes[LuaTokenType.DOT] = LuaTextAttributeKeys.DOT.key
        elementTextAttributes[LuaTokenType.DOTDOTDOT] = LuaTextAttributeKeys.DOTDOTDOT.key

        elementTextAttributes[StringEscapesTokenTypes.VALID_STRING_ESCAPE_TOKEN] = LuaTextAttributeKeys.VALID_STRING_ESCAPE.key
        elementTextAttributes[StringEscapesTokenTypes.INVALID_CHARACTER_ESCAPE_TOKEN] = LuaTextAttributeKeys.INVALID_STRING_ESCAPE.key
    }

    override fun getHighlightingLexer(): Lexer = LuaLexer()

    override fun getTokenHighlights(tokenType: IElementType?): Array<TextAttributesKey> =
        pack(elementTextAttributes[tokenType])
}

class LuaSyntaxHighlighterFactory : SyntaxHighlighterFactory() {
    override fun getSyntaxHighlighter(project: Project?, virtualFile: VirtualFile?): SyntaxHighlighter =
        LuaSyntaxHighlighter()
}