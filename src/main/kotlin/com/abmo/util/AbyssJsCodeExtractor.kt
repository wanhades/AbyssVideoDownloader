package com.abmo.util

import com.abmo.common.Logger

class AbyssJsCodeExtractor {

    companion object {
        private val FUNCTION_WITH_SHIFT_PUSH_REGEX = Regex(
            """\(function\s*\([^)]*\)\s*\{(?:[^{}]++|\{(?:[^{}]++|\{[^{}]*+})*+})*+}(?:\([^)]*\))?\s*;?\)""",
            RegexOption.DOT_MATCHES_ALL
        )

        private val SELF_ASSIGNING_FUNCTION_REGEX = Regex(
            """function\s+(\w+)\s*\(\)\s*\{\s*var\s+(\w+)\s*=\s*\[.*?];\s*\1\s*=\s*function\s*\(\)\s*\{\s*return\s+\2;\s*};\s*return\s+\1\(\);\s*}""",
            RegexOption.DOT_MATCHES_ALL
        )

        private val VARIABLE_ASSIGNMENT_REGEX = Regex("""var\s+([a-zA-Z])\s*=\s*([a-zA-Z]);""")
        private val COMMA_VARIABLE_ASSIGNMENT_REGEX = Regex(",\\s*([a-zA-Z_]\\w*)\\s*=\\s*\\{\\s*}")

        private val OBJECT_ASSIGNMENT_PATTERN_REGEX = Regex(
            "var\\s+([a-zA-Z_]\\w*)\\s*=\\s*\\{\\s*};.*?var\\s+\\w+\\s*=\\s*\\{\\s*\\.\\.\\.\\1\\s*,",
            RegexOption.DOT_MATCHES_ALL
        )

        private val COMPLEX_CONCATENATION_REGEX = Regex(
            """([A-Za-z])\(0x[a-fA-F0-9]+\)(?:\s*\+\s*(?:\([A-Za-z]\(0x[a-fA-F0-9]+\)(?:\s*\+\s*[A-Za-z]\(0x[a-fA-F0-9]+\))*(?:\s*\+\s*'[^']*')?\)|[A-Za-z]\(0x[a-fA-F0-9]+\)|'[^']*'))*""",
            RegexOption.DOT_MATCHES_ALL
        )

        private val OBJECT_ASSIGN_REPLACEMENT_REGEX = Regex(
            """var\s+[a-zA-Z]\s*=\s*\{\.\.\.[a-zA-Z],\s*sourcesEncoded:\s*sourcesEncoded\s*}"""
        )
    }

    fun getCompleteJsCode(jsResponse: String?): String? {
        if (jsResponse.isNullOrBlank()) {
            Logger.error("Empty response received")
            return null
        }

        val varName = extractVariableName(jsResponse)
        if (varName == null) {
            Logger.error("Variable name not found")
            return null
        }

        val sourcesEncodedDeclaration = extractComplexConcatenation(jsResponse, varName)
        if (sourcesEncodedDeclaration == null) {
            Logger.error("Function chain not found")
            return null
        }

        val objectAssignmentPattern = extractObjectAssignmentPattern(jsResponse)
//        println(objectAssignmentPattern)
        if (objectAssignmentPattern == null) {
            Logger.error("Object assignment pattern not found")
            return null
        }



        val commaVariableName = extractCommaVariableName(jsResponse)
        if (commaVariableName == null) {
            Logger.error("Comma variable name not found")
            return null
        }

        return buildCompleteCode(
            jsResponse = jsResponse,
            varName = varName,
            sourcesEncodedDeclaration = sourcesEncodedDeclaration,
            objectAssignmentPattern = objectAssignmentPattern,
            commaVariableName = commaVariableName
        )
    }

    private fun buildCompleteCode(
        jsResponse: String,
        varName: String,
        sourcesEncodedDeclaration: String,
        objectAssignmentPattern: String,
        commaVariableName: String
    ): String = buildString {
        extractShiftPushFunctions(jsResponse).forEach { appendLine(it) }
        extractSelfAssigningFunction(jsResponse)?.let { appendLine(it) }
        extractFunction(jsResponse, varName)?.let { appendLine(it) }
        appendLine("var $commaVariableName = {};")
        extractVariableDeclarations(jsResponse).forEach { appendLine(it) }
        appendLine(sourcesEncodedDeclaration)
        appendLine(replaceWithObjectAssign(objectAssignmentPattern))
        appendLine("java.lang.System.out.println(JSON.stringify(b))")
    }

    private fun extractVariableName(jsCode: String): String? =
        VARIABLE_ASSIGNMENT_REGEX.find(jsCode)?.groupValues?.get(2)

    private fun extractCommaVariableName(jsCode: String): String? =
        COMMA_VARIABLE_ASSIGNMENT_REGEX.find(jsCode)?.groupValues?.get(1)

    private fun extractObjectAssignmentPattern(jsCode: String): String? {
        val startIndex = jsCode.indexOf("JSON")
        val endIndex = jsCode.lastIndexOf("...")

        val relevantSection = jsCode.substring(startIndex, endIndex)
        return OBJECT_ASSIGNMENT_PATTERN_REGEX.find(relevantSection)?.value
            ?.plus("sourcesEncoded: sourcesEncoded}")
    }

    private fun extractComplexConcatenation(jsCode: String, varName: String): String? {
        val longestChain = COMPLEX_CONCATENATION_REGEX.findAll(jsCode)
            .map { it.value }
            .maxByOrNull { it.length }
            ?: return null

        val oldName = longestChain.substringBefore("(")
        return "var $oldName = $varName;\nvar sourcesEncoded = $longestChain"
    }

    private fun extractShiftPushFunctions(jsCode: String): List<String> =
        FUNCTION_WITH_SHIFT_PUSH_REGEX.findAll(jsCode)
            .map { it.value }
            .filter { functionCode ->
                containsPattern(functionCode, "shift") && containsPattern(functionCode, "push")
            }
            .toList()

    private fun containsPattern(code: String, pattern: String): Boolean =
        code.contains(Regex("""\b$pattern\b|\['$pattern']|\.$pattern\("""))

    private fun extractSelfAssigningFunction(jsCode: String): String? =
        SELF_ASSIGNING_FUNCTION_REGEX.find(jsCode)?.value

    private fun extractFunction(jsCode: String, functionName: String): String? {
        val pattern = Regex(
            """function\s+($functionName)\s*\([^)]*\)\s*\{((?:[^{}]++|\{(?:[^{}]++|\{[^{}]*+})*+})*+)}"""
        )
        return pattern.find(jsCode)?.value
    }

    private fun replaceWithObjectAssign(code: String): String {
        val variableName = code.substringAfter("...")
            .substringBefore(",sourcesEncoded: sourcesEncoded")

        return OBJECT_ASSIGN_REPLACEMENT_REGEX.replace(code) {
            """
            var b = Object.assign({}, $variableName);
            b.sourcesEncoded = sourcesEncoded;
            """.trimIndent()
        }
    }

    private fun extractVariableDeclarations(jsCode: String): List<String> {
        val regex = Regex("""var\s+([a-zA-Z_$][a-zA-Z0-9_$]*)\s*=\s*\{\s*}\s*;""")
        return regex.findAll(jsCode)
            .map { it.value }
            .toList()
    }
}











