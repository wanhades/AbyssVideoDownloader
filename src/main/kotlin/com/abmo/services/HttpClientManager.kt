package com.abmo.services

import com.abmo.common.Logger
import com.mashape.unirest.http.Unirest
import java.io.BufferedReader
import java.io.InputStreamReader

class HttpClientManager {

    companion object {
        private const val USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/129.0.6668.102 Safari/537.36 (AirWatch Browser v22.05.0.4)"
        private const val CURL_IMPERSONATE_INSTALL_URL = "https://github.com/lwthiker/curl-impersonate/blob/main/INSTALL.md"
    }

    fun makeHttpRequest(url: String, headers: Map<String, String?>? = null, curlPath: String): Response? {
        Logger.debug("Starting HTTP GET request to $url")

        return if (isLinuxDistro()) {
            makeRequestWithCurl(url, headers, curlPath)
        } else {
            makeHttpRequest(url, headers)
        }
    }

    private fun isLinuxDistro(): Boolean {
        val osName = System.getProperty("os.name").lowercase()
        val linuxDistros = setOf(
            "linux", "ubuntu", "debian", "centos", "fedora",
            "redhat", "suse", "arch", "manjaro", "mint",
            "elementary", "kali", "gentoo", "alpine"
        )
        return linuxDistros.any { osName.contains(it) }
    }

    private fun makeRequestWithCurl(url: String, headers: Map<String, String?>?, curlPath: String): Response? {
        Logger.debug("Running on Linux distro, using curl-impersonate-chrome")

        if (!isCurlImpersonateAvailable()) {
            showInstallationInstructions()
            return null
        }

        return try {
            val command = buildCurlCommand(url, headers, curlPath)
            executeCurlCommand(command)
        } catch (e: Exception) {
            Logger.error("Error executing curl-impersonate-chrome: ${e.message}")
            null
        }
    }

    private fun makeHttpRequest(url: String, headers: Map<String, String?>?): Response? {
        Logger.debug("Running on Windows, using Unirest")

        return try {
            val response = Unirest.get(url)
                .headers(headers)
                .asString()

            Logger.debug("Received response with status ${response.status}", response.status !in 200..299)

            if (response.status !in 200..299) {
                Logger.error("HTTP request failed with status ${response.status}")
                return null
            }

            Response(
                body = response.body,
                statusCode = response.status
            )
        } catch (e: Exception) {
            Logger.error("Error making HTTP request with Unirest: ${e.message}")
            null
        }
    }

    private fun isCurlImpersonateAvailable(): Boolean {
        return try {
            val processBuilder = ProcessBuilder("curl-impersonate-chrome", "--version")
            val process = processBuilder.start()
            val exitCode = process.waitFor()
            exitCode == 0
        } catch (e: Exception) {
            false
        }
    }

    private fun showInstallationInstructions() {
        Logger.error("curl-impersonate-chrome is not installed or not found in PATH")
        println("ERROR: curl-impersonate-chrome is required for Linux-based environments.")
        println("Please install it by following the instructions at:")
        println(CURL_IMPERSONATE_INSTALL_URL)
    }

    private fun buildCurlCommand(url: String, headers: Map<String, String?>?, curlPath: String): List<String> {
        val command = mutableListOf(
            curlPath,
            "-s",
            "-A", USER_AGENT,
            "-w", "%{http_code}",
            url
        )

        headers?.forEach { (key, value) ->
            if (value != null) {
                command.addAll(listOf("-H", "$key: $value"))
            }
        }

        return command
    }

    private fun executeCurlCommand(command: List<String>): Response? {
        val processBuilder = ProcessBuilder(command)
        val process = processBuilder.start()

        val reader = BufferedReader(InputStreamReader(process.inputStream))
        val fullResponse = reader.readText()
        reader.close()

        val exitCode = process.waitFor()
        Logger.debug("curl-impersonate-chrome completed with exit code $exitCode", exitCode != 0)

        if (exitCode != 0) {
            val errorReader = BufferedReader(InputStreamReader(process.errorStream))
            val errorOutput = errorReader.readText()
            errorReader.close()
            Logger.error("curl-impersonate-chrome failed: $errorOutput")
            return null
        }

        val statusCode = try {
            val lastThreeChars = fullResponse.takeLast(3)
            lastThreeChars.toInt()
        } catch (e: Exception) {
            Logger.error("Failed to parse HTTP status code from curl response")
            return null
        }


        val responseBody = fullResponse.dropLast(3)

        Logger.debug("Received response with status $statusCode", statusCode !in 200..299)

        if (statusCode !in 200..299) {
            Logger.error("HTTP request failed with status $statusCode")
            return null
        }

        return Response(
            body = responseBody,
            statusCode = statusCode
        )
    }
}

data class Response(
    val body: String,
    val statusCode: Int
)