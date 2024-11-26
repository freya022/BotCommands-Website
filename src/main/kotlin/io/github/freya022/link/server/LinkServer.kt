package io.github.freya022.link.server

import ch.qos.logback.classic.ClassicConstants
import io.github.classgraph.ClassGraph
import io.github.classgraph.ClassInfo
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
import kotlin.metadata.jvm.KotlinClassMetadata
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
    }

@Suppress("t")
private fun getIdentifierLinkRepresentation(identifier: String): LinkRepresentation? {
    fun notFound(reason: String): LinkRepresentation? {
        logger.info { "Could not find '$identifier': $reason" }
        return null
    }

    // Extensions are handled implicitly because they are treated as top-level functions in KDocs
    if (identifier[0].isLowerCase()) { // Top-level
        classes.forEach { clazz ->
            if (!clazz.annotations.directOnly().containsName(Metadata::class.java.name))
                return@forEach

            val metadata = readMetadata(clazz)
            val kmPackage = (metadata as? KotlinClassMetadata.FileFacade)?.kmPackage
                ?: return@forEach

            val func = kmPackage.functions.firstOrNull { function -> function.name == identifier }
            if (func != null) {
                return LinkRepresentation(
                    func.toSimpleString(),
                    "${kmPackage.getBaseLink(clazz)}/${func.name.toKDocCase()}.html"
                )
            }

            val prop = kmPackage.properties.firstOrNull { property -> property.name == identifier }
            if (prop != null)
                return LinkRepresentation(identifier, "${kmPackage.getBaseLink(clazz)}/${prop.name.toKDocCase()}.html")
        }

        return notFound("'$identifier' is neither a top-level function or property")
    } else if ('#' in identifier) { // Member (property or function)
        val (className, memberName) = identifier.split("#")
        val classInfo = findClass(className)
            ?: return notFound("'$className' was not found")
        val metadata = readMetadata(classInfo)
            ?: return notFound("'$className' is not a Kotlin class")

        val kmClass = (metadata as? KotlinClassMetadata.Class)?.kmClass
            ?: return notFound("'$className' is not a class")
        val func = kmClass.functions.firstOrNull { function -> function.name == memberName }
        if (func != null)
            return LinkRepresentation(identifier, "${kmClass.getBaseLink(classInfo)}/${func.name.toKDocCase()}.html")

        val prop = kmClass.properties.firstOrNull { property -> property.name == memberName }
        if (prop != null)
            return LinkRepresentation(identifier, "${kmClass.getBaseLink(classInfo)}/${prop.name.toKDocCase()}.html")

        val enumEntry = kmClass.enumEntries.firstOrNull { enumEntry -> enumEntry == memberName }
        if (enumEntry != null)
            return LinkRepresentation(identifier, "${kmClass.getBaseLink(classInfo)}/${enumEntry.toKDocCase()}/index.html")

        return notFound("'$memberName' is neither a function, property or enum value in '$className'")
    } else {
        val className = identifier
        val classInfo = findClass(className)
            ?: return notFound("'$className' was not found")
        val metadata = readMetadata(classInfo)
            ?: return notFound("'$className' is not a Kotlin class")

        val kmClass = (metadata as? KotlinClassMetadata.Class)?.kmClass
            ?: return notFound("'$className' is not a class")

        return when (kmClass.kind) {
            ClassKind.ANNOTATION_CLASS -> LinkRepresentation("#!java @$identifier", "${kmClass.getBaseLink(classInfo)}/index.html")
            else -> LinkRepresentation(identifier, "${kmClass.getBaseLink(classInfo)}/index.html")
        }
    }
}

private fun findClass(className: String): ClassInfo? {
    return classes.firstOrNull { classInfo -> classInfo.simpleNestedName.replace('$', '.') == className }
}

private fun readMetadata(classInfo: ClassInfo): KotlinClassMetadata? {
    val metadataAnnotation = classInfo.annotationInfo.directOnly()[Metadata::class.java.name]?.loadClassAndInstantiate() as Metadata?
        ?: return null
    return KotlinClassMetadata.readStrict(metadataAnnotation)
}