package com.abmo.util

import com.abmo.common.Logger
import java.util.regex.Pattern

class AbyssJsCodeExtractor {

    // hacky tacky solution, got no time to this in an efficient way :)
    private fun extractSwitchCaseContent(jsCode: String, variableName: String): String? {
        val casePattern = "case\\s*'[^']*'\\s*:(.*?)(?=case\\s*'|}\\s*break;)"
        val cases = Regex(casePattern, RegexOption.DOT_MATCHES_ALL).findAll(jsCode)

        return cases.map { match ->
            match.groupValues[1]
                .trim()
                .replace(Regex("\\s*continue;\\s*$"), "")
                .trim()
        }.maxByOrNull { it.length }
            ?.replace(Regex("\\[([a-zA-Z])\\(")) { // replace current function names with the global one
            "[${variableName}("
        }?.replace(Regex("(\\.\\.\\.[^,}]+,\\s*)\\.\\.\\.\\w+"), "$1sourcesEncoded: sourcesEncoded")
    }

    fun extractWhileSwitchFunction(jsCode: String): String? {
        val pattern = "function\\s+\\w+\\s*\\(\\s*\\)\\s*\\{\\s*var\\s+\\w+\\s*=\\s*\\w+\\s*,\\s*\\w+\\s*=\\s*\\{.*?while.*?switch.*?break;.*?}.*?}"
        val match = Regex(pattern, RegexOption.DOT_MATCHES_ALL).find(jsCode)?.value
        val withoutWhile = match?.replace(Regex("while\\s*\\([^)]*\\)\\s*\\{.*?break;\\s*}", RegexOption.DOT_MATCHES_ALL), "")
        return withoutWhile?.replace(Regex("^function\\s+\\w+\\s*\\([^)]*\\)\\s*\\{|}\\s*$"), "")?.trim()
    }

    fun extractFunction(jsCode: String, functionName: String): String? {
        val robustPattern =
            """function\s+($functionName)\s*\([^)]*\)\s*\{((?:[^{}]++|\{(?:[^{}]++|\{[^{}]*+})*+})*+)}""".toRegex()
        val match = robustPattern.find(jsCode)
        return match?.value
    }

    fun extractFunctionsWithShiftAndPush(jsCode: String): List<String> {
        val improvedPattern = Regex(
            """(\(function\s*\([^)]*\)\s*\{(?:[^{}]++|\{(?:[^{}]++|\{[^{}]*+})*+})*+}(?:\([^)]*\))?\s*;?\))""",
            RegexOption.DOT_MATCHES_ALL
        )
        val results = mutableListOf<String>()
        val matches = improvedPattern.findAll(jsCode)

        for (match in matches) {
            val functionCode = match.value
            val containsShift = functionCode.contains(Regex("""\bshift\b|\['shift']|\.shift\("""))
            val containsPush = functionCode.contains(Regex("""\bpush\b|\['push']|\.push\("""))

            if (containsShift && containsPush) {
                results.add(functionCode)
            }
        }

        return results
    }

    fun extractComplexConcatenation(jsCode: String): String? {
        val pattern = Pattern.compile(
            """([A-Za-z])\(0x[a-fA-F0-9]+\)(?:\s*\+\s*(?:\([A-Za-z]\(0x[a-fA-F0-9]+\)(?:\s*\+\s*[A-Za-z]\(0x[a-fA-F0-9]+\))*(?:\s*\+\s*'[^']*')?\)|[A-Za-z]\(0x[a-fA-F0-9]+\)|'[^']*'))*""",
            Pattern.MULTILINE or Pattern.DOTALL
        )

        val matcher = pattern.matcher(jsCode)
        var longestChain = ""

        while (matcher.find()) {
            val currentChain = matcher.group()
            if (currentChain.length > longestChain.length) {
                longestChain = currentChain
            }
        }

        return longestChain.ifEmpty { null }
    }

    fun extractSelfAssigningFunction(jsCode: String): String? {
        val regex = Regex(
            """function\s+(\w+)\s*\(\)\s*\{\s*var\s+(\w+)\s*=\s*\[.*?];\s*\1\s*=\s*function\s*\(\)\s*\{\s*return\s+\2;\s*};\s*return\s+\1\(\);\s*}""",
            RegexOption.DOT_MATCHES_ALL
        )
        return regex.find(jsCode)?.value
    }

    fun extractVariableName(jsCode: String): String? {
        val pattern = Regex("""var\s+([a-zA-Z])\s*=\s*([a-zA-Z]);""")
        return pattern.find(jsCode)?.groupValues?.get(2)
    }

    private fun replaceWithObjectAssign(code: String): String {
        val regex = Regex("""var\s+[a-zA-Z]\s*=\s*\{\.\.\.[a-zA-Z],\s*sourcesEncoded:\s*sourcesEncoded\s*},\s*[a-zA-Z]\s*=\s*[a-zA-Z]\s*;""")
        val variableName = code.substringAfter("...")
            .substringBefore(",sourcesEncoded: sourcesEncoded")
        return regex.replace(code, """
            var b = Object.assign({}, $variableName);
            b.sourcesEncoded = sourcesEncoded;
        """.trimIndent())
    }

    fun getCompleteJsCode(jsResponse: String?): String? {
        jsResponse ?: run {
            Logger.error("empty response received")
            return null
        }

        val varName = extractVariableName(jsResponse) ?: run {
            Logger.error("variable name wasn't found")
            return null
        }

        val result = extractComplexConcatenation(jsResponse) ?: run {
            Logger.error("function chain wasn't found")
            return null
        }

        val switchCaseContent = extractSwitchCaseContent(jsResponse, varName)
        if (switchCaseContent == null) {
            Logger.error("failed to find switch case content")
            return null
        }

        return buildString {
            appendLine(extractFunctionsWithShiftAndPush(jsResponse))
            appendLine(extractSelfAssigningFunction(jsResponse))
            appendLine(extractFunction(jsResponse, varName))
            appendLine(extractWhileSwitchFunction(jsResponse))
            appendLine("var sourcesEncoded = $result")
            appendLine(replaceWithObjectAssign(switchCaseContent))
            append("java.lang.System.out.println(JSON.stringify(b))")
        }
    }

}











