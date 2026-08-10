# report

Placeholder module — not used yet in the current MVP.

`ReportGenerator` and its supporting model (`Finding`, `Severity`, `ReportFormat`, formatters)
currently live inside `gradle-plugin` instead of here. They'll move to this module once a second
consumer (a runtime/`ObfuscationSDK` module for in-app reflection checks) needs to reuse the same
report generation code without pulling in the Gradle plugin's dependencies.

This module is kept scaffolded now so that future split doesn't require restructuring the
project layout.
