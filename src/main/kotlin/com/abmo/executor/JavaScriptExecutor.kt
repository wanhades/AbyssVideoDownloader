package com.abmo.executor

import org.mozilla.javascript.Context
import org.mozilla.javascript.Scriptable
import java.io.InputStream

class JavaScriptExecutor {


    private val context = Context.enter()

    fun runJavaScriptCode(fileName: String, identifier: String, vararg arguments: Any?): String {
            val scope: Scriptable = context.initStandardObjects()
            val jsFileStream: InputStream = javaClass.getResourceAsStream("/$fileName")
                ?: throw IllegalArgumentException("File $fileName not found in resources")
            val jsScript = jsFileStream.bufferedReader().use { it.readText() }

            context.evaluateString(scope, jsScript, fileName, 1, null)
            val jsFunction = scope.get(identifier, scope)

            if (jsFunction is org.mozilla.javascript.Function) {
                val jsArgs = arguments.map { arg -> Context.javaToJS(arg, scope) }.toTypedArray()
                val result = jsFunction.call(context, scope, scope, jsArgs)
                return result as? String ?: ""
            }

            return ""
        }

}