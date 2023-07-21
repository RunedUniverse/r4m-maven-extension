# Runes4Maven (r4m) Maven Extension
Runes4Maven (r4m) Maven Extension provides an alternative way for defining maven executions

## Concept
Maven hides a bunch of very useful options under the hood. Additionally the Executions feature is very powerful but can be quite a hussle to configure correctly. Furthermore maven does not provide an option to just launch one phase which might include only goals that don't require any prerequisites.

Runes4Maven (r4m) reworks two core features and adds ease of life features.
1. Which executions get activated?
2. Which Goals are included in which execution?
3. Selecting Goals through 'modes'
4. Activating Executions for entire lifecycle-tasks (phases)
5. Specific declaration of which phases are to run.
6. Help Lifecycle

### Project Execution Model (PEM) [1-3]
The Project Execution Model `pem.xml` can be used to override all inherited default executions and specify specific triggers, restrictions and modes for your specific usecase.
We strongly recommend not to write the PEM from scratch but to generate the currently active configuration. This can be done by running `r4m:gen-full-pem` to generate to full PEM usually around 700-1000 lines (yes maven does inject a ton of configuration), we recommend to generate only the relevant PEM for your usecase by running `r4m:gen-rel-pem`.
Please keep in mind that the relevant PEM of the root project of a multi-module-maven project only includes the config of the root project.

The `pem.xml` has the schema header shown below, allowing you to speed up writing it with an adequate IDE (Eclipse/Intellij).
```xml
<project-execution-model
    xmlns="https://api.runeduniverse.net/runes4tools/r4m-pem"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="https://api.runeduniverse.net/runes4tools/r4m-pem 
    https://api.runeduniverse.net/runes4tools/r4m-pem-v1_0_0.xsd">

    <modelVersion>1.0.0</modelVersion>
    <executions>
        ...
    </executions>
</project-execution-model>
```

Every Execution defined in the PEM can have multiple triggers. Currently following triggers are available - other core-extensions may define additionaly triggers:

`default`, `on-call` (this is always active), `active-profile`, `inactive-profile`, `provided-profile`, `missing-profile`


Every Execution defined in the PEM can have multiple restrictions, once a restriction type is defined at least one of those definitions has to be fulfilled. Currently following restriction is available - other core-extensions may define additionaly restrictions:

`packaging-procedure`

Every Goal in the PEM has to have at least one mode. By default the modes `default` and `dev` are preconfigured. This can be changed inside the `pem.xml`.

**What might I use modes for?** With modes you can have two seperate 'modes' of one execution, for example you can make it so that a formtting goal only runs in 'dev' mode and not during normal operation.

### Lifecycle Tasks Rework [3-5]
When running maven you append goal/lifecycle tasks to the maven command 'mvn'.

Maven already provides the goal tasks in following style - as such they are unchanged:

`<plugin-prefix>:<goal>[@<execution>]` or `<plugin-group-id>:<plugin-artifact-id>[:<plugin-version>]:<goal>[@<execution>]`


Runes4Maven (r4m) further reworked the Lifecycle Tasks which allow following style:

`[<modes>/]<lifecycle-tasks>[@<executions>]`

##### &lt;modes&gt;
Modes are a comma separated list of active modes. By default the modes 'default' and 'dev' are available but you may define others within the pem.xml config.

##### &lt;lifecycle-tasks&gt;
Lifecycle Tasks are a comma separated list of active lifecycle-phases. Every Lifecycle Phase may have one additional modifier before and/or after it:

`]` or `[` `<phase>` `]` or `[`

**Modifiers**

```
   ']' before the phase:
       select all phases before the phase excluding the phase itself
   '[' before the phase:
       select all phases after the phase including the phase itself
   ']' after the phase:
       select all phases before the phase including the phase itself
   '[' after the phase:
       select all phases after the phase excluding the phase itself
```

##### &lt;executions&gt;
Executions are a comma separated list of active executions.

#### Examples

Install everything without running tests in 'dev' mode:

`dev/]test[,install]`

Run all the tests without recompiling, with the 'pipeline' execution:

`[test]@pipeline`

Package the project with the custom defined modes 'my-mode' & 'my-test-mode' and executions 'pipeline' & 'test':

`my-mode,my-test-mode/package]@pipeline,test`

### Help Lifecycle [6]
Runes4Maven (r4m) provides the help lifecycle. With this the cmd `mvn help` is finally viable. Runes4Maven does **not** crawl build-plugins to attach their help goals to the `help` lifecycle.

But we recommend every build-plugin developer to attach their help goals to this lifecycle. In case the default maven way does not work for you, we recommend you to include a `plugin-pem.xml` file with the same syntax as the PEM in your jar under the path `META-INF/r4m/plugin-pem.xml` and set the execution 'source' field to `plugin`. You may of course also attach all your other goals to lifecycles in the way.

## Goals

Goal | Description
`r4m:help` | Prints the help-page
`r4m:help-tasks` | Prints goal/lifecycle tasks help-page. It describes how the new build argument 'lifecycle-tasks' works.
`r4m:gen-full-pem` | Discovers all loaded Executions which influence the current project build lifecycles. Discovered Executions will be condensed as much as possible and written to the 'full-pem.xml' file in the defined build directory.
`r4m:gen-rel-pem` | Discovers all loaded Executions which are relevant to and influence the current project build lifecycles. Discovered Executions will be condensed as much as possible and written to the 'rel-pem.xml' file in the defined build directory.
`r4m:status` | Shows the status of all r4m features.

## Properties

Property | Default | Options | Description
---|---|---|---
`r4m.active-profiles-inheritance` | `upstream` | `upstream, top-level, false` | This property defines whether active profiles are inherited when running single modules (defined via `-pl <module>`) inside a multi-module-maven project. Since by default profiles activated via cli (`-P<profile>`) only apply to the top-level project.
`r4m.fancy-output` | `true` | `true, false` | Some help goals output "fancy" styled text which gets removed when logging. These goals will log in an alternate way when set to `false`.
`r4m.generate-plugin-executions` | `true` | `true, false` | This property defines whether missing active executions can be auto-generated. For further information see [Issue #5](https://github.com/RunedUniverse/r4m-maven-extension/issues/5#issuecomment-1618743363).
`r4m.generate-plugin-executions-on-fork` | `true` | `true, false` | This property defines whether missing active executions can be auto-generated when forking. For further information see [Issue #5](https://github.com/RunedUniverse/r4m-maven-extension/issues/5#issuecomment-1618743363).
`r4m.lifecycle-task-request-calculator` | `declared` | `declared, sequential` | This property defines how lifecycle-tasks without modifiers get interpreted. Option `declared`   -> `<phase>` => `[<phase>]` vs option `sequential` -> `<phase>` =>  `<phase>]`.
`r4m.lifecycle-task-request-calculator-on-fork` | `sequential` | `declared, sequential` | This property defines how maven default based goal forks, defining an `execute-phase`, get interpreted. Option `declared`   -> `<phase>` => `[<phase>]` vs option `sequential` -> `<phase>` =>  `<phase>]`.
`r4m.missing-build-plugin-handler` | `warn` | `skip, warn, scan, download` | This property defines how active goals without an active plugin definition should get handled. In case you have a plugin which requires a secondary "missing/undefined" plugin you may set this property to `scan` or `download` but be warned this slows down operation significantly. Furthermore in case you have a maven-plugin which has a secondary plugin bundled you can forceload the secondary plugin by setting this property to `scan` but again this slows down operation significantly and is **not** recommended. Please just define all required plugins!
`r4m.patch-mojo-on-fork` | `true` | `true, false` | This defines whether the mojos during fork can be rewritten. This 'fixes' the gui but may break other plugins which might rely on that variable downstream.

## Installation
Runes4Maven (r4m) reworks can be installed in two major ways:

### Maven Core Extension
Runes4Maven (r4m) installed as maven core extension exposes r4m full potential.
A Core Extension can be installed system wide via the `${maven.home}/lib/ext` folder of you maven installation.

Or per execution by adding the CLI argument `-Dmaven.ext.class.path=extension.jar`

Or per project by adding it in the `extensions.xml`. This has to be located in the `.mvn` folder which has to be in the root folder of your maven project per [maven definition](https://maven.apache.org/configure.html#mvn-extensions-xml-file).


We recommend the per project installation:
> just place following file in the `.mvn` folder your projects root directory!

`.mvn/extensions.xml`

```xml
<extensions xmlns="http://maven.apache.org/EXTENSIONS/1.0.0"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/EXTENSIONS/1.0.0 http://maven.apache.org/xsd/core-extensions-1.0.0.xsd">
	<extension>
		<groupId>net.runeduniverse.tools.maven.r4m</groupId>
		<artifactId>r4m-maven-extension</artifactId>
		<version>1.0.0</version>
	</extension>
</extensions>
```

### Maven Build Extension
Runes4Maven (r4m) installed as maven build extension limits its potential.
Some features may not be available in this mode it will print a sizable warning!

[Maven Guide Example for Build Extensions](https://maven.apache.org/guides/mini/guide-using-extensions.html#build-extension)
