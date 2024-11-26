package io.github.freya022.link.server

import ch.qos.logback.classic.ClassicConstants
import io.github.classgraph.ClassGraph
import io.github.classgraph.ClassInfo
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.link.server.utils.getBaseLink
import io.github.freya022.link.server.utils.toKDocCase
import io.github.freya022.link.server.utils.toSimpleString
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

class LinkException(message: String) : IllegalArgumentException(message)

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
        singleTopLevelFunction(identifier)
    } else if ('#' in identifier) { // Member (property or function)
        singleMember(identifier)
    } else { // Class
        singleClass(identifier)
    }
}

private fun singleTopLevelFunction(identifier: String): LinkRepresentation {
    val candidates = classes.mapNotNull { clazz -> getTopLevelFunctionOrNull(clazz, identifier) }
    if (candidates.isEmpty()) {
        notFound("'$identifier' is neither a top-level function or property")
    } else if (candidates.size > 1) {
        notFound("Found multiple candidates for '$identifier':\n${candidates.joinToString("\n") { it.url } }")
    } else {
        return candidates.first()
    }
}

private fun getTopLevelFunctionOrNull(clazz: ClassInfo, identifier: String): LinkRepresentation? {
    if (!clazz.annotations.directOnly().containsName(Metadata::class.java.name))
        return null

    val metadata = readMetadata(clazz)
        ?: return null
    val kmPackage = (metadata as? KotlinClassMetadata.FileFacade)?.kmPackage
        ?: return null

    val func = kmPackage.functions.firstOrNull { function -> function.name == identifier }
    if (func != null) {
        return LinkRepresentation(
            func.toSimpleString(),
            "${kmPackage.getBaseLink(clazz)}/${func.name.toKDocCase()}.html"
        )
    }

    val prop = kmPackage.properties.firstOrNull { property -> property.name == identifier }
    if (prop != null) {
        return LinkRepresentation(
            identifier,
            "${kmPackage.getBaseLink(clazz)}/${prop.name.toKDocCase()}.html"
        )
    }

    return null
}

private fun singleMember(identifier: String): LinkRepresentation {
    val (className, memberName) = identifier.split("#")
    val classes = findClasses(className)
    if (classes.isEmpty()) {
        notFound("'$className' was not found")
    } else {
        val candidates = classes.mapNotNull { classInfo -> getMemberOrNull(classInfo, memberName) }
        if (candidates.isEmpty()) {
            notFound("'$memberName' is neither a function, property or enum value in '$className'")
        } else if (candidates.size > 1) {
            notFound("Found multiple candidates for '$identifier':\n${candidates.joinToString("\n") { it.url } }")
        } else {
            return candidates.first()
        }
    }
}

private fun getMemberOrNull(classInfo: ClassInfo, memberName: String): LinkRepresentation? {
    val className = classInfo.name
    val memberLabel = "${classInfo.simpleNestedName}.$memberName"

    val metadata = readMetadata(classInfo)
        ?: notFound("'$className' is not a Kotlin class")

    val kmClass = (metadata as? KotlinClassMetadata.Class)?.kmClass
        ?: notFound("'$className' is not a class")
    val func = kmClass.functions.firstOrNull { function -> function.name == memberName }
    if (func != null)
        return LinkRepresentation(memberLabel, "${kmClass.getBaseLink(classInfo)}/${func.name.toKDocCase()}.html")

    val prop = kmClass.properties.firstOrNull { property -> property.name == memberName }
    if (prop != null)
        return LinkRepresentation(memberLabel, "${kmClass.getBaseLink(classInfo)}/${prop.name.toKDocCase()}.html")

    val enumEntry = kmClass.enumEntries.firstOrNull { enumEntry -> enumEntry == memberName }
    if (enumEntry != null)
        return LinkRepresentation(
            memberLabel,
            "${kmClass.getBaseLink(classInfo)}/${enumEntry.toKDocCase()}/index.html"
        )

    return null
}

private fun singleClass(className: String): LinkRepresentation {
    val classes = findClasses(className)
    if (classes.isEmpty()) {
        notFound("'$className' was not found")
    } else if (classes.size > 1) {
        notFound("Found multiple candidates for '$className': ${classes.joinToString { it.name } }")
    } else {
        val classInfo = classes.first()
        val metadata = readMetadata(classInfo)
            ?: notFound("'$className' is not a Kotlin class")

        val kmClass = (metadata as? KotlinClassMetadata.Class)?.kmClass
            ?: notFound("'$className' is not a class")

        return when (kmClass.kind) {
            ClassKind.ANNOTATION_CLASS -> LinkRepresentation("#!java @$className", "${kmClass.getBaseLink(classInfo)}/index.html")
            else -> LinkRepresentation(className, "${kmClass.getBaseLink(classInfo)}/index.html")
        }
    }
}

private fun notFound(reason: String): Nothing = throw LinkException(reason)

private fun findClasses(className: String): List<ClassInfo> {
    return classes.filter { classInfo -> classInfo.simpleNestedName.replace('$', '.') == className }
}

private fun readMetadata(classInfo: ClassInfo): KotlinClassMetadata? {
    val metadataAnnotation = classInfo.annotationInfo.directOnly()[Metadata::class.java.name]?.loadClassAndInstantiate() as Metadata?
        ?: return null
    return KotlinClassMetadata.readStrict(metadataAnnotation)
}