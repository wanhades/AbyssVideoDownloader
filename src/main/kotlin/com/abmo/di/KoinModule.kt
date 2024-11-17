package com.abmo.di

import com.abmo.util.CliArguments
import com.abmo.crypto.CryptoHelper
import com.abmo.executor.JavaScriptExecutor
import com.abmo.services.ProviderDispatcher
import com.abmo.services.VideoDownloader
import com.google.gson.Gson
import org.koin.dsl.module

val koinModule = module {
    single { JavaScriptExecutor() }
    single { CryptoHelper() }
    single { VideoDownloader() }
    single { ProviderDispatcher() }
    single { Gson() }
    factory { (args: Array<String>) -> CliArguments(args) }
}