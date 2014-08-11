# Java `sudo`-like user impersonation

This small example uses copious amounts of anonymous inner classes to create 'sandboxes' for java applications. Each `Runnable`/`Callable<T>` will get the user specified for them in the surrounding `sudo` invocation when calling `AppSec.currentUser`. Nesting calls to `AppSec.sudo` is supported as well as spawning new threads from within a `sudo` closure, which then inherits the parent's security scope. See the unit test for an example.
