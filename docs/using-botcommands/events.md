# Using events
{{ wiki_stub }}

## Conditional event listeners based on available intents
Use [[RequiredIntents]].

!!! tip

    You can also use [[RequiredIntents]] for classes other than events listeners, for example,
    you might toggle an entire feature off if the required intents are not enabled.

## Listening to events
Use [[BEventListener]].

### Listener modes
See [[BEventListener#mode]]

## Lifecycle events
Here is a list of events in the order they fire:

### Initialization events
- [[BStatusChangeEvent]]
- [[PreLoadEvent]]
- [[LoadEvent]]
- [[PostLoadEvent]]
- [[BReadyEvent]]

## JDA events
- [[InjectedJDAEvent]]
- [[PreFirstGatewayConnectEvent]]
- [[FirstGuildReadyEvent]]