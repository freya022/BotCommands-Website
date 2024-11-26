package io.github.freya022.link.server

import ch.qos.logback.classic.ClassicConstants
import io.github.freya022.link.server.resolution.ClassMemberResolver
import io.github.freya022.link.server.resolution.ClassResolver
import io.github.freya022.link.server.resolution.TopLevelResolver
import io.github.freya022.wiki.config.Environment
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.*
import io.ktor.resources.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.resources.*
import io.ktor.server.resources.Resources
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.slf4j.event.Level
import kotlin.io.path.absolutePathString

private val logger by lazy { KotlinLogging.logger { } }

@Resource("/link")
class Link(val identifier: String)

fun main() {
    System.setProperty(ClassicConstants.CONFIG_FILE_PROPERTY, Environment.logbackConfigPath.absolutePathString())
    logger.info { "Loading logback configuration at ${Environment.logbackConfigPath.absolutePathString()}" }

    embeddedLinkServer().start(wait = true)
}

fun embeddedLinkServer() =
    embeddedServer(Netty, port = 16069) {
        install(ContentNegotiation) {
            json()
        }
        install(Resources)
        install(CallLogging) {
            level = Level.DEBUG
        }

        routing {
            link()
        }
    }

private fun Routing.link() {
    get<Link> {
        val identifier = it.identifier
        runCatching {
            getIdentifierLinkRepresentation(identifier)
        }.onSuccess { link ->
            call.respond(link)
        }.onFailure { exception ->
            if (exception is LinkException) {
                logger.info(exception) { "Could not get link for identifier '$identifier'" }
                call.respondText("Link not found", status = HttpStatusCode.NotFound)
            } else {
                logger.error(exception) { "Could not get link for identifier '$identifier'" }
                call.respondText("Exception while getting link", status = HttpStatusCode.InternalServerError)
            }
        }
    }
}

private fun getIdentifierLinkRepresentation(identifier: String): LinkRepresentation {
    // Extensions are handled implicitly because they are treated as top-level functions in KDocs
    return if (identifier[0].isLowerCase()) { // Top-level
        TopLevelResolver.singleTopLevelFunction(identifier)
    } else if ('#' in identifier) { // Member (property or function)
        ClassMemberResolver.singleMember(identifier)
    } else { // Class
        ClassResolver.singleClass(identifier)
    }
}
