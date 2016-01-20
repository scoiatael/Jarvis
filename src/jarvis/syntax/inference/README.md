# Type inference

Part of analysis done by Jarvis will be (partial) type inference for Clojure programs.

This feature will function as engine behind autocompletion and suggestions, gathering data about program being written and interacting with REPL behind the scenes.

Its full scope is yet to be defined, as it may yet turn out to be impossible to bring full power of type inference to Clojure. If that happens, we'd have to simply use function signatures returned by REPL to match suggestions and autocompletion.

It may later prove useful to extract this tool as a library to be used for Clojure code verification.
