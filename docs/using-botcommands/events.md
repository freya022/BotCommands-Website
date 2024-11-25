# Using events
{{ wiki_stub }}

## Conditional event listeners based on available intents
Use [[RequiredIntents]].

!!! tip

    You can also use [[RequiredIntents]] for classes other than events listeners, for example,
    you might toggle an entire feature off if the required intents are not enabled.

## Listening to events
Use [[BEventListener]].

## Lifecycle events
Here is a list of events in the order they fire:

### Initialization events
#### Status change event
See [[BStatusChangeEvent]].

#### Pre-load event
See [[PreLoadEvent]].

#### Load event
See [[LoadEvent]].

#### Post-load event
See [[PostLoadEvent]].

#### Ready event
See [[BReadyEvent]].

### Injected JDA event
See [[InjectedJDAEvent]].

### First guild ready event
See [[FirstGuildReadyEvent]].

## JDA events
### InjectedJDAEvent
See [[InjectedJDAEvent]].

### FirstGuildReadyEvent
See [[FirstGuildReadyEvent]].