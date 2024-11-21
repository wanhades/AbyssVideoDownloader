package com.abmo.executor

import org.mozilla.javascript.Context
import org.mozilla.javascript.Scriptable
import java.io.InputStream

class JavaScriptExecutor {
    /**
     * Executes a JavaScript function from a file with the specified arguments.
     *
     * @param fileName The name of the JavaScript file to load from resources.
     * @param identifier The name of the JavaScript function to call.
     * @param arguments The arguments to pass to the JavaScript function.
     * @return The result of the JavaScript function as a String, or an empty string if the function is not found or result is not a String.
     * @throws IllegalArgumentException If the JavaScript file is not found in resources.
     */
    fun runJavaScriptCode(fileName: String, identifier: String, vararg arguments: Any?): String {
        val context = Context.enter()
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