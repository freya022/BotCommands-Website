# Using localization
{{ wiki_stub }}

## In application commands
See [[AppLocalizationContext]], or the localized replies methods on your interaction event,
usually inherited from [[LocalizableInteraction]], [[LocalizableReplyCallback]] and [[LocalizableEditCallback]].

In a nutshell, events have a few methods which let you reply directly
with a localized string and a configurable (and overridable) locale,
with the translated strings coming from [[BLocalizationConfigBuilder#responseBundles]].

Messages sent using `InteractionHook` can also be localized, as the `getHook` method will now return a [[LocalizableInteractionHook]].

You can also change a few things such as the locale used by the interaction, its localization bundle, or its prefix,
see the [[LocalizableInteraction]] docs.

You will typically use the [`replyUser`][[LocalizableReplyCallback#replyUser]] and [`replyGuild`][[LocalizableReplyCallback#replyGuild]] functions,
letting you reply using the user/guild locale in a simple and elegant way,
while allowing customization using [[UserLocaleProvider]] and [[GuildLocaleProvider]] respectively.

## Extending support
### How localization bundles are loaded


### Localization map providers
See `LocalizationMapProvider`.

### Localization map readers
See `LocalizationMapReader`.

You can create one of your own with [[JacksonLocalizationMapReader]].

### Localization templates
See [[LocalizationTemplate]].
