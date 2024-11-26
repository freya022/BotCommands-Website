package io.github.freya022.link.server

import ch.qos.logback.classic.ClassicConstants
import io.github.classgraph.ClassGraph
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
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
import kotlinx.serialization.Serializable
import org.slf4j.event.Level
import kotlin.io.path.absolutePathString
import kotlin.metadata.ClassKind
import kotlin.metadata.KmClass
import kotlin.metadata.jvm.KotlinClassMetadata
import kotlin.metadata.jvm.moduleName
import kotlin.metadata.kind

private val logger by lazy { KotlinLogging.logger { } }

private val classes = ClassGraph()
    .enableClassInfo()
    .enableAnnotationInfo()
    .acceptPackages("io.github.freya022.botcommands.api")
    .scan()
    .allClasses

@Resource("/link")
class Link(val identifier: String)

@Serializable
data class LinkRepresentation(
    val label: String,
    val url: String,
//    val tooltip: String,
)

fun main() {
    System.setProperty(ClassicConstants.CONFIG_FILE_PROPERTY, Environment.logbackConfigPath.absolutePathString())
    logger.info { "Loading logback configuration at ${Environment.logbackConfigPath.absolutePathString()}" }

    embeddedServer(Netty, port = 16069) {
        install(ContentNegotiation) {
            json()
        }
        install(Resources)
        install(CallLogging) {
            level = Level.DEBUG
        }

        routing {
            get<Link> {
                val identifier = it.identifier
                val link = getIdentifierLinkRepresentation(identifier)
                if (link != null) {
                    call.respond(link)
                } else {
                    call.respondText("Link not found", status = HttpStatusCode.NotFound)
                }
            }
        }
    }.start(wait = true)
}

private fun getIdentifierLinkRepresentation(identifier: String): LinkRepresentation? {
    fun getClassMetadata(className: String): KotlinClassMetadata? {
        val classInfo = classes.firstOrNull { classInfo -> classInfo.simpleNestedName.replace('$', '.') == className }
            ?: return null
        val metadataAnnotation = classInfo.annotationInfo.directOnly()[Metadata::class.java.name].loadClassAndInstantiate() as Metadata
        return KotlinClassMetadata.readStrict(metadataAnnotation)
    }

    fun notFound(reason: String): LinkRepresentation? {
        logger.info { "Could not find '$identifier': $reason" }
        return null
    }

    fun String.toKDocCase(): String {
        return replace(Regex("[A-Z]")) { "-${it.value.lowercase()}" }
    }

    fun KmClass.getBaseLink(): String {
        val module = moduleName!!.toKDocCase()
        val firstUppercaseIndex = name.indexOfFirst { ch -> ch.isUpperCase() }
        val packageName = name.substring(0..<firstUppercaseIndex - 1).replace('/', '.')
        val classNames = name.substring(firstUppercaseIndex).split(".").joinToString("/") { it.toKDocCase() }
        return "https://docs.bc.freya02.dev/$module/$packageName/$classNames"
    }

    if ('#' in identifier) {
        val (className, memberName) = identifier.split("#")
        val metadata = getClassMetadata(className)
            ?: return notFound("'$className' was not found")

        val kmClass = (metadata as? KotlinClassMetadata.Class)?.kmClass
            ?: return notFound("'$className' is not a class")
        val func = kmClass.functions.firstOrNull { function -> function.name == memberName }
        if (func != null) {
            return LinkRepresentation(identifier, "${kmClass.getBaseLink()}/${func.name.toKDocCase()}.html")
        }

        val prop = kmClass.properties.firstOrNull { property -> property.name == memberName }
        if (prop != null)
            return LinkRepresentation(identifier, "${kmClass.getBaseLink()}/${prop.name.toKDocCase()}.html")

        val enumEntry = kmClass.enumEntries.firstOrNull { enumEntry -> enumEntry == memberName }
        if (enumEntry != null)
            return LinkRepresentation(identifier, "${kmClass.getBaseLink()}/${enumEntry.toKDocCase()}/index.html")

        return notFound("'$memberName' is neither a function, property or enum value in '$className'")
    } else {
        val className = identifier
        val metadata = getClassMetadata(className)
            ?: return notFound("'$className' was not found")

        val kmClass = (metadata as? KotlinClassMetadata.Class)?.kmClass
            ?: return notFound("'$className' is not a class")

        return when (kmClass.kind) {
            ClassKind.ANNOTATION_CLASS -> LinkRepresentation("#!java @$identifier", "${kmClass.getBaseLink()}/index.html")
            else -> LinkRepresentation(identifier, "${kmClass.getBaseLink()}/index.html")
        }
    }
}

private val KmClass.packageName: String get() = name.takeWhile { !it.isUpperCase() }
private val KmClass.simpleNestedName: String get() = name.dropWhile { !it.isUpperCase() }