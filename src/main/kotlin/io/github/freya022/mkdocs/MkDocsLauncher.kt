package io.github.freya022.mkdocs

import ch.qos.logback.classic.ClassicConstants
import io.github.freya022.link.server.embeddedLinkServer
import io.github.freya022.wiki.config.Environment
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.engine.*
import java.lang.ProcessBuilder.Redirect
import java.util.*
import kotlin.concurrent.thread
import kotlin.io.path.absolutePathString
import kotlin.system.exitProcess

private val logger by lazy { KotlinLogging.logger { } }

private lateinit var linkServer: EmbeddedServer<*, *>
private lateinit var process: Process
private lateinit var externalTerminationThread: Thread
private lateinit var inputThread: Thread

fun main(args: Array<String>) {
    require(args.size >= 2) {
        "The command to launch should have least two arguments"
    }
    require(args[0] == "mkdocs" || args[0] == "mike") {
        "The command to launch must be mkdocs or mike"
    }

    System.setProperty(ClassicConstants.CONFIG_FILE_PROPERTY, Environment.logbackConfigPath.absolutePathString())
    logger.info { "Loading logback configuration at ${Environment.logbackConfigPath.absolutePathString()}" }

    linkServer = embeddedLinkServer()
    linkServer.start(wait = false)

    process = ProcessBuilder(*args)
        .redirectOutput(Redirect.INHERIT)
        .redirectError(Redirect.INHERIT)
        .start()

    externalTerminationThread = thread(name = "MkDocs Termination Watcher", block = ::externalTerminationListener)

    inputThread = thread(name = "MkDocsLauncher input", block = ::inputListener)

    Runtime.getRuntime().addShutdownHook(thread(start = false, name = "MkDocsLauncher Shutdown Hook", block = ::onShutdown))
}

private fun externalTerminationListener() {
    runCatching {
        process.waitFor()
    }.onSuccess { exitCode ->
        logger.warn { "Mkdocs exited early with code $exitCode" }
        exitProcess(exitCode)
    }.onFailure { throwable ->
        logger.debug(throwable) { "Mkdocs terminated" }
    }
}

private fun inputListener() {
    val scanner = Scanner(System.`in`)
    logger.info { "Type 'exit' to close this" }
    while (true) {
        try {
            if (scanner.nextLine() == "exit") {
                return exitProcess(0)
            }
        } catch (_: NoSuchElementException) {
            return
        }
    }
}

private fun onShutdown() {
    externalTerminationThread.interrupt()
    inputThread.interrupt()

    if (process.isAlive) {
        if (process.supportsNormalTermination())
            logger.info { "Terminating MkDocs normally" }
        else
            logger.info { "Terminating MkDocs forcibly" }
        process.destroy()
    }

    logger.info { "Stopping link server" }
    linkServer.stop()
}