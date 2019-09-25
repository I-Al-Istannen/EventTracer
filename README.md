# About

This is a simple plugin which instruments the `SimplePluginManager#fireEvent` method using ByteBuddy. It then intercepts any fired event, replaces it with a dynamically generated proxy that logs method calls and hands it of.

Finally, after `fireEvent` is done, it reports the results (method calls) in its own `AuditEvent`.
This enables you to easily debug what plugins are messing with your event and what they are doing.

## Features
* Can intercept all fired events
* Logs all calls to *non final methods*. This is a sad limitation, but the dynamically generated subclass can not overwrite the final method sadly.
* Provides an event with the source event and a list of all method calls (containing the method, the parameters, the plugin and the stacktrace)

## Caveats and Problems
* As said before, final methods are not tracked
* It uses Objenesis for instantiating objects (so that should be fine)
* It uses Reflection on JDK internal packages to be able to set final fields.
  This is needed as the subclasses need to have the same state as their source event so final methods work as expected.

## Possible remedies
* Do not make a subclass, but instead *redefine the existing class*, adding custom interception logic. This has a few drawbacks:
    - We can not add fields, so the audit/call log needs to be stored in some central Map-like collection
    - We can not add fields, so the original source event needs to be stored in some central Map-like collection
    - We are fundamentally messing with classes and redefine bukkit/other plugin's stuff. This will probably not cause problems, but it is quite invasive.
    - This still does not solve the final field problem., but it could make it better: If all methods are redirected to the original, final fields can not escape and their values should not matter at all.
