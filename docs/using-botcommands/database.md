# Using a database

The framework provides several JDBC abstractions, statement logging, reporting long transactions
and officially supports PostgreSQL and H2 (in PostgreSQL compatibility mode).

!!! info

    Some features requires a database, such as [components](../using-components.md) and paginators.

## Creating a `ConnectionSupplier`

A `ConnectionSupplier` is responsible for providing connections to your database, as well as some metadata.

Requesting the connection supplier service is not recommended,
you should instead use `Database` (Kotlin) and `BlockingDatabase` (Java).

If you wish not to use these abstractions, you can use their `fetchConnection` methods,
to at least take advantage of statement logging and long transaction reports.

We'll also use [HikariCP](https://github.com/brettwooldridge/HikariCP), a connection pool
that will reduce latency a lot when often reacquiring connections.

Creating a datasource requires implementing `HikariSourceSupplier`,
where you can directly give the connection details, that's it.

??? example

    === "PostgreSQL"
        === "Kotlin"
            ```kotlin
            @BService
            class DatabaseSource(config: Config) : HikariSourceSupplier {
                override val source = HikariDataSource(HikariConfig().apply {
                    // Suppose those are your config values
                    jdbcUrl = config.databaseConfig.url
                    username = config.databaseConfig.user
                    password = config.databaseConfig.password
            
                    // At most 2 JDBC connections, suspends the coroutine if all connections are used
                    maximumPoolSize = 2
                    // Emits a warning and does a thread/coroutine dump after the duration
                    leakDetectionThreshold = 10.seconds.inWholeMilliseconds
                })
            }
            ```
        === "Java"
            ```java
            @BService
            public class DatabaseSource implements HikariSourceSupplier {
                private final HikariDataSource source;
            
                public DatabaseSource(Config config) {
                    final var hikariConfig = new HikariConfig();
                    // Suppose those are your config values
                    hikariConfig.setJdbcUrl(config.getDatabaseConfig().getUrl());
                    hikariConfig.setUsername(config.getDatabaseConfig().getUser());
                    hikariConfig.setPassword(config.getDatabaseConfig().getPassword());
            
                    // At most 2 JDBC connections, the database will block if all connections are used
                    hikariConfig.setMaximumPoolSize(2);
            
                    // Emits a warning and does a thread/coroutine dump after the duration (in milliseconds)
                    hikariConfig.setLeakDetectionThreshold(10000);
            
                    source = new HikariDataSource(hikariConfig);
                }
            
                @NotNull
                @Override
                public HikariDataSource getSource() {
                    return source;
                }
            }
            ```

        !!! tip "PostgreSQL connection URL"
            The URL should be similar to `jdbc:postgresql://[HOST]:[PORT]/[DB_NAME]`, by default, the port is 5432.
        
    === "H2"
        === "Kotlin"
            ```kotlin
            @BService
            class DatabaseSource : HikariSourceSupplier {
                override val source = HikariDataSource(HikariConfig().apply {
                    // Create an in-file database with the PostgreSQL compatibility mode
                    jdbcUrl = "jdbc:h2:./MyBotDatabase;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH"
            
                    // At most 2 JDBC connections, suspends the coroutine if all connections are used
                    maximumPoolSize = 2
                    // Emits a warning and does a thread/coroutine dump after the duration
                    leakDetectionThreshold = 10.seconds.inWholeMilliseconds
                })
            }
            ```
        === "Java"
            ```java
            @BService
            public class DatabaseSource implements HikariSourceSupplier {
                private final HikariDataSource source;
            
                public DatabaseSource() {
                    final var hikariConfig = new HikariConfig();
                    // Create an in-file database with the PostgreSQL compatibility mode
                    hikariConfig.setJdbcUrl("jdbc:h2:./MyBotDatabase;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH");
            
                    // At most 2 JDBC connections, the database will block if all connections are used
                    hikariConfig.setMaximumPoolSize(2);
            
                    // Emits a warning and does a thread/coroutine dump after the duration (in milliseconds)
                    hikariConfig.setLeakDetectionThreshold(10000);
            
                    source = new HikariDataSource(hikariConfig);
                }
            
                @NotNull
                @Override
                public HikariDataSource getSource() {
                    return source;
                }
            }
            ```

        This will create a database stored in a file called `MyBotDatabase`, in the current working directory,
        see more details on the [H2 website](https://www.h2database.com/html/features.html#database_url).

## Using migration
The framework's tables may be automatically created and migrated on updates,
while the migration scripts uses a naming scheme compatible with Flyway, it may work with other migration libraries.

??? info "How Flyway works, simplified"

    The library maintains it's own table, which contains the migration scripts it has executed,
    including some data about them, like their hash.

    When you run it, Flyway will check the migrations it found, in the location you told it to look into,
    and then checks which one already ran, in the chronological order (that's why you name them following a pattern).

    Migrations that were not applied will run, after which they can't be modified anymore.

!!! note "Using [`ServiceLoader`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/ServiceLoader.html)-based libraries"

    Libraries like Flyway, PostgreSQL and SLF4J,
    uses a `ServiceLoader` which requires having files in your JAR to load these services.

    While creating your JAR, your build tools needs to be configured to merge those files properly,
    as it would overwrite those files when multiple libraries target a common interface, causing missing services.

    You can see how it's done in [this Setup section](../setup/getting-started.md#creating-a-runnable-jar).

### Migrating the framework schema
After creating your `ConnectionSupplier`, you can run:

```java
Flyway.configure()
    .dataSource(source) // Your already existing data source
    .schemas("bc") // The name of the internal schema
    .locations("bc_database_scripts") // Where the migration scripts are located
    .validateMigrationNaming(true)
    .loggers("slf4j") // Both JDA and BC logs using SLF4J
    .load()
    .migrate() // Create or update existing schema
```

### Migrating your own schema
Migrating your own schema is very similar to the code above, here are a few guidelines:

1. Replace the schema (`bc`) with your own
2. Replace the location of your migration scripts (`bc_database_scripts`) to your own (like `[bot name]_database_scripts)`
3. Each migration script must be named similar to `V[version].[date]__[Description_text].sql`, for example:
   `V1.2.3.2024.11.23__Add_tags.sql`
4. After running a migration script, it cannot be modified. You can always delete your schema to start over.
5. Do not use `#!sql IF NOT EXISTS` in your SQL,
   it is not necessary since your schema should be empty (or even non-existant) when the migrations run,
   and will cause unexpected issues if data definitions already exist.

## Configuration
### Logging statements
SQL Statements can be logged if:

- `BDatabaseConfig#logQueries` is enabled,
  and the logger of the class that created the prepared statement has its `TRACE` logs enabled
- **Or,** `BDatabaseConfig#queryLogThreshold` is configured,
  and the logger of the class that created the prepared statement has its `WARN` logs enabled

!!! tip "Ignoring utility classes creating prepared statements"

    If you are creating a prepared statement in a class unrelated to what actually uses the statement,
    the queries will be logged using the unrelated class.
    
    The logger used is the first class in the call stack that's not annotated with `#!java @IgnoreStackFrame`,
    you may use this annotation on your class, making the logger use the caller class.
    
    You can alternatively use `withLogger` to manually set the logger of a prepared statement.

### Report long transactions
A thread/coroutine dump can be created when the connection has a longer lifetime than expected,
refer to `BDatabaseConfig#dumpLongTransactions` for more details.

## Sample usages

[//]: # (TODO waiting on https://github.com/facelessuser/pymdown-extensions/issues/2217 ?)
### Running a statement, returning a value
=== "Kotlin"
    ```kotlin
    --8<-- "wiki/commands/slash/SlashDb.kt:db_return_value-kotlin"
    ```

=== "Java"
    ```java
    --8<-- "wiki/java/commands/slash/SlashDb.java:db_return_value-java"
    ```

### Running a statement, returning rows
=== "Kotlin"
    ```kotlin
    --8<-- "wiki/commands/slash/SlashDb.kt:db_return_rows-kotlin"
    ```

=== "Java"
    ```java
    --8<-- "wiki/java/commands/slash/SlashDb.java:db_return_rows-java"
    ```

### Running multiple statements in a transaction statement
=== "Kotlin"
    ```kotlin
    --8<-- "wiki/commands/slash/SlashDb.kt:db_transaction-kotlin"
    ```

=== "Java"
    ```java
    --8<-- "wiki/java/commands/slash/SlashDb.java:db_transaction-java"
    ```

### Running a single statement, returning generated keys
=== "Kotlin"
    ```kotlin
    --8<-- "wiki/commands/slash/SlashDb.kt:db_generated_keys-kotlin"
    ```

=== "Java"
    ```java
    --8<-- "wiki/java/commands/slash/SlashDb.java:db_generated_keys-java"
    ```