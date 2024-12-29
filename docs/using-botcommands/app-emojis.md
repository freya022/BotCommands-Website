# Using application emojis

Long gone are the days when you had to add emojis to your own guilds,
you can now add up to 2000 emojis on your application.

While you can add them manually in your dashboard, they are:

* Not very practical
* Requires you to hardcode them (unless you fetch them yourself, but that's even more effort)
* Can only be used for one application, making it annoying when you switch from a test bot to your production bot.

Which is why you can let the framework upload them for you!
It will upload them after JDA logs in, but before it comes online.

## Making an application emoji container

The first step is to create a class that will hold your application emojis,
you will need to annotate it with [[AppEmojiContainer]], 
which tells the library to load all `ApplicationEmoji` fields inside it.

This will search emojis in the `/emojis` folder by default, but you can change it in the annotation. 

## Registering/retrieving application emojis
### Eager retrieval

=== "Kotlin"
    You can use [delegated properties](https://kotlinlang.org/docs/delegated-properties.html) on [[AppEmojisRegistry]] to get an application emoji from your property's name,
    for example:

    ```kotlin
    --8<-- "wiki/emojis/AppEmojis.kt:eager_app_emojis-kotlin"
    ```

=== "Java"
    You can use [[AppEmojisRegistry#get]], pass it the name of your field, for example:

    ```java
    --8<-- "wiki/java/emojis/AppEmojis.java:eager_app_emojis-java"
    ```

!!! warning

    You must not initialize the class before the emojis are loaded,
    in other words, you can only access your fields/properties once they have been retrieved.
    You typically don't need to do anything but to use the emojis when they are actually needed.

### Lazy retrieval (Kotlin only)

Similar to eager retrieval, you can use [delegated properties](https://kotlinlang.org/docs/delegated-properties.html) on [[AppEmojisRegistry#lazy]], for example:

```kotlin
--8<-- "wiki/emojis/LazyAppEmojis.kt:lazy_app_emojis-kotlin"
```

As always, you cannot use the emoji before it is loaded, however,
loading the `object` itself is fine.