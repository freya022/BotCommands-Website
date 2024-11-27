In addition to what [JDA-KTX](https://github.com/MinnDevelopment/jda-ktx) offers, Kotlin users have access to top-level functions and extensions in various categories:

## JDA
### REST actions
??? example "[[awaitUnit]] — Awaits completion and returns `Unit`, particularly useful to reply + return" 

    ```kotlin
    fun onSlashCommand(event: GuildSlashEvent, inputUser: InputUser)
        val member = inputUser.member
            ?: return event.reply_("The user needs to be a member of this server!", ephemeral = true).awaitUnit()
    }
    ```

??? example "[[awaitOrNullOn]] — Awaits completion, returns `null` if the action failed with the expected error response"

    ```kotlin
    fun onSlashBanInfo(event: GuildSlashEvent, user: User)
        val ban = event.guild.retrieveBan(user).awaitOrNullOn(ErrorResponse.UNKNOWN_BAN)
            ?: return event.reply_("This user is not banned", ephemeral = true).awaitUnit()
    }
    ```

??? example "[[awaitCatching]] — Awaits completion and returns a `Result` with the wrapped value/failure"

    ```kotlin
    fun onSlashBanInfo(event: GuildSlashEvent, user: User)
        val ban = event.guild.retrieveBan(user).awaitCatching()
            .onErrorResponse(ErrorResponse.UNKNOWN_BAN) {
                return event.reply_("This user is not banned", ephemeral = true).awaitUnit()
            }
            .getOrThrow()
    }
    ```

### Error response handling
- [[onErrorResponse]] — Runs code if the specified error response was returned, see `awaitCatching` above
- [[ignore]] — Ignores and clears the specified error responses
- [[handle]] — Replaced the specified error response with the calculated value

??? example "[[runIgnoringResponse]] — Runs the block and ignores the following error responses, throws other exceptions"

    ```kotlin
    runIgnoringResponse(ErrorResponse.CANNOT_SEND_TO_USER) {
        channel.sendMessage(msg).await()
    }
    ```

??? example "[[runIgnoringResponseOrNull]] — Runs the block and returns `null` on the following error responses, throws other exceptions"

    ```kotlin
    suspend fun Guild.retrieveBanOrNull(user: UserSnowflake): Ban? = runIgnoringResponseOrNull(ErrorResponse.UNKNOWN_BAN) {
        retrieveBan(user).await() // Can also use awaitOrNullOn, removing runIgnoringResponseOrNull
    }
    ```

### Messages
- [[toEditData]] — Does what it says, the edit data will replace the content of the entire message
- [[toCreateData]] — Do I need to say anything?

- [[send]] — Sends the message, this is useful for chaining
- [[edit]] — Edits with that message, this is useful for chaining
- [[replaceWith]] — Replaces this interaction's message with the following content

- [[deleteDelayed]] — Deletes the message after the delay, the rest action itself is delayed, not the code execution (unless you use `await`)

### Entity retrieval
Those check the cache before doing a request.

- [[retrieveMemberOrNull]] — Returns null if the member does not exist
- [[retrieveUserOrNull]] — Returns null if the user does not exist
- [[retrieveThreadChannelById]] — Retrieves a thread by ID, from any thread container, archived or not.
- [[retrieveThreadChannelOrNull]] — Same but returns null if it does not exist, if the bot doesn't have access to it, or if the channel isn't a thread.

### Misc
??? example "[[suppressContentWarning]] — Runs the block and suppresses warnings emitted by JDA when reading message content, this is mostly useful in message context commands"

    ```kotlin
    val contentRaw = suppressContentWarning { message.contentRaw }
    ```

- [[getMissingPermissions]] — Gets the missing permissions from the required permissions, for the given entity, in the specified channel

Any method accepting a Java `Duration` should also have an extension using Kotlin's `Duration`

## Resolvers
??? example "[[enumResolver]] — Creates a parameter resolver which transforms arguments into an enum entry, compatible with most handlers"

    ```kotlin
    enum class MyEnum {
        FIRST,
        SECOND,
        THIRD
    }
    
    @BConfiguration
    class MyEnumResolverProvider {
        // Creates an enum resolver for all values
        // you can also customize what values can be used, per-guild,
        // and also change how they are displayed
        @Resolver
        fun myEnumResolver() = enumResolver<MyEnum>()
    }
    ```

??? example "[[resolverFactory]] — Creates a factory for parameter resolvers, useful to avoid the boilerplate of using [[TypedParameterResolverFactory]]"

    See example on [the docs][[ResolverFactory]]

## I/O
??? example "[[readResource]] — Gets an `InputStream` of a resource from the classpath"

    ```kotlin
    readResource("/file.txt").use { contentStream ->
        // ...
    }
    ```

??? example "[[readResourceAsString]] — Gets a resource from the classpath as a `String`"

    ```kotlin
    val content = readResourceAsString("/file.txt")
    ```

??? example "[[withResource]] — Uses an `InputStream` of a resource from the classpath"

    ```kotlin
    withResource("/file.txt") { contentStream ->
        // ...
    }
    ```

## Coroutines
??? example "[[namedDefaultScope]] — Creates a `CoroutineScope` with a thread name and a fixed thread pool"

    ```kotlin
    // 1 thread named "[feature] timeout"
    // You can also configure other CoroutineScope characteristics
    private val timeoutScope = namedDefaultScope("[feature] timeout", corePoolSize = 1)

    // ...

    timeoutScope.launch {
        // Async task
    }
    ```

## Logging
??? example "[[loggerOf]] — Creates a logger targeting the specified class"

    ```kotlin
    private val logger = KotlinLogging.loggerOf<MyService>()

    @BService
    class MyServiceImpl : MyService {
        // ...
    }
    ```

## Collections
- [[enumSetOf]] — Creates an enum set of the provided type
- [[enumSetOfAll]] — Creates an enum set of the provided type, with all the entries in it
- [[enumMapOf]] — Creates a map with an enum key
- [[toImmutableList]] — Creates an _immutable copy_ of the list
- [[toImmutableSet]] — Creates an _immutable copy_ of the set
- [[toImmutableMap]] — Creates an _immutable copy_ of the map
- [[containsAny]] — Checks if the collection contains any of the provided elements