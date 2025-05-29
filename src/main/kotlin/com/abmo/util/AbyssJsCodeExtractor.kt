package com.abmo.util

import java.util.regex.Pattern

class AbyssJsCodeExtractor {

    private fun extractDynamicVariablePattern(jsCode: String): String? {
        val regex = Regex(
            pattern = """var\s+([a-zA-Z_$][a-zA-Z0-9_$]*)\s*=\s*\(function\s*\([^)]*\)\s*\{[\s\S]*?}\s*\([^)]*\)\s*\)\s*,\s*([a-zA-Z_$][a-zA-Z0-9_$]*)\s*=\s*\1\s*\(\s*this\s*,\s*function\s*\([^)]*\)\s*\{[\s\S]*?}\s*\)\s*;""",
            option = RegexOption.DOT_MATCHES_ALL
        )
        return regex.find(jsCode)?.value
    }

    private fun extractFunction(jsCode: String, functionName: String): String? {
        val robustPattern =
            """function\s+($functionName)\s*\([^)]*\)\s*\{((?:[^{}]++|\{(?:[^{}]++|\{[^{}]*+})*+})*+)}""".toRegex()
        val match = robustPattern.find(jsCode)
        return match?.value
    }

    private fun extractFunctionsWithShiftAndPush(jsCode: String): List<String> {
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

    private fun extractComplexConcatenation(jsCode: String): String? {
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

    private fun extractSelfAssigningFunction(jsCode: String): String? {
        val regex = Regex(
            """function\s+(\w+)\s*\(\)\s*\{\s*var\s+(\w+)\s*=\s*\[.*?];\s*\1\s*=\s*function\s*\(\)\s*\{\s*return\s+\2;\s*};\s*return\s+\1\(\);\s*}""",
            RegexOption.DOT_MATCHES_ALL
        )
        return regex.find(jsCode)?.value
    }

    private fun extractVariableName(jsCode: String): String? {
        val pattern = Regex("""var\s+([a-zA-Z])\s*=\s*([a-zA-Z]);""")
        return pattern.find(jsCode)?.groupValues?.get(2)
    }

    fun getCompleteJsCode(jsResponse: String?): String? {
        jsResponse ?: return null

        val varName = extractVariableName(jsResponse) ?: run {
            println("variable name wasn't found")
            return null
        }

        val result = extractComplexConcatenation(jsResponse) ?: run {
            println("function chain wasn't found")
            return null
        }

        val functionName = result.substringBefore("(")

        return buildString {
            appendLine(extractFunctionsWithShiftAndPush(jsResponse))
            appendLine(extractFunction(jsResponse, varName))
            appendLine(extractDynamicVariablePattern(jsResponse))
            appendLine(extractSelfAssigningFunction(jsResponse))
            append("java.lang.System.out.println(${result.replace("$functionName(", "$varName(")})")
        }
    }

}











