package com.abmo

import com.abmo.di.koinModule
import org.koin.core.context.startKoin


suspend fun main(args: Array<String>) {
    startKoin { modules(koinModule) }
    Application(args).run()
}
