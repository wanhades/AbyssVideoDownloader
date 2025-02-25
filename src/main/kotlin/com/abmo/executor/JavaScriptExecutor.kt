package com.abmo.executor

import com.abmo.common.Logger
import org.mozilla.javascript.Context
import org.mozilla.javascript.EvaluatorException
import org.mozilla.javascript.Scriptable
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.PrintStream

class JavaScriptExecutor {
    /**
     * Executes a JavaScript function from a file with the specified arguments.
     *
     * @param javascriptFileName The name of the JavaScript file to load from resources.
     * @param identifier The name of the JavaScript function to call.
     * @param arguments The arguments to pass to the JavaScript function.
     * @return The result of the JavaScript function as a String
     * @throws IllegalArgumentException If the JavaScript file is not found in resources.
     */
    fun runJavaScriptCode(javascriptCode: String? = null, javascriptFileName: String? = null, identifier: String? = null, vararg arguments: Any?): String {
        val context = Context.enter()
        context.optimizationLevel = -1
        val scope: Scriptable = context.initStandardObjects()
        val outputStream = ByteArrayOutputStream()
        val printStream = PrintStream(outputStream)
        val originalOut = System.out

        val jsScript = if (javascriptFileName != null) {
            val jsFileStream: InputStream = javaClass.getResourceAsStream("/$javascriptFileName")
                ?: throw IllegalArgumentException("File $javascriptFileName not found in resources")
            jsFileStream.bufferedReader().use { it.readText() }
        } else {
            javascriptCode
        }

        try {
            System.setOut(printStream) // Redirect System.out to our stream

            context.evaluateString(scope, jsScript, javascriptFileName, 1, null)
            val jsFunction = scope.get(identifier, scope)

            if (jsFunction is org.mozilla.javascript.Function) {
                val jsArgs = arguments.map { arg -> Context.javaToJS(arg, scope) }.toTypedArray()
                val result = jsFunction.call(context, scope, scope, jsArgs)
                return result as? String ?: ""
            }

            printStream.flush()
            return outputStream.toString().trim() // Return the captured output

        }
        catch (e: EvaluatorException) {
            Logger.error("Exception occurred while executing the provided JavaScript code: ${e.message}")
            return ""
        }
        finally {
            System.setOut(originalOut) // Restore original System.out
            Context.exit()
        }
    }

}