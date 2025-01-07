# Using localization
{{ wiki_stub }}

## In application commands
See [[AppLocalizationContext]], or the localized replies methods on your interaction event,
usually inherited from [[LocalizableInteraction]], [[LocalizableReplyCallback]] and [[LocalizableEditCallback]].

You can also use a [[LocalizableInteractionHook]] by retrieving it from your event's [interaction hook][[LocalizableInteraction#getHook]].

You can also change a few things such as the locale used by the interaction, its localization bundle, or its prefix,
see the [[LocalizableInteraction]] docs.

## Extending support
### How localization bundles are loaded


### Localization map providers
See `LocalizationMapProvider`.

### Localization map readers
See `LocalizationMapReader`.

You can create one of your own with [[JacksonLocalizationMapReader]].

### Localization templates
See [[LocalizationTemplate]].
