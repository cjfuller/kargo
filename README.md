# kargo
A command-line build tool for kotlin inspired by rust's [cargo](https://doc.rust-lang.org/cargo/). 

# Installation

Download the release for your platform from the releases section. These are jar files with a prefix to make them self-executing; 
they require a JVM (currently tested with openjdk-11) and the `java` command on your path. If you plan to use the native-image
command that uses graalvm to build a native binary, you'll need a working install of graalvm including the native-image command.

# Usage

## Setup

Prior to starting, you'll need to create a `Kargo.toml` file at the root of your project, which gives basic information about the
project and a list of dependencies. A minimal `Kargo.toml` looks like:

```toml
[package]
name = "my_awesome_project"
kotlin_version = "1.6.10"

[dependencies]

```

After this file is created, you'll need to run `kargo init`, which will download and vendor a number of helper tools into the
`.kargo` directory within your project. (You should add `.kargo` to your `.gitignore` file.)

Dependencies can be added using syntax like `"io.ktor:ktor-client-core" = "1.6.7"`, one line per dependency in the
`[dependencies]` section of your `Kargo.toml`. After you initially set up your project and whenever you add a dependency, you
should lock the dependencies with `kargo lock`. This will resolve all direct and transitive dependencies to consistent versions
and write them to the `Kargo.lock` file. (You should commit Kargo.lock to version control.)

Once you've locked dependencies (and every time you re-lock them), run `kargo deps`, which will vendor the locked version of all
dependencies into `.kargo/deps`. (If needed, you can put the jars in this directory on the classpath for your editor / IDE for
completion and syntax highlighting.)

## Command reference

### init

`kargo init`

Vendors helper tools into the `.kargo` directory within your project. You should run this once when starting a new project after
creating the `Kargo.toml` file, or if you update kargo itself.

### lock

`kargo lock`

Reads depdencies from `Kargo.toml`, resolves their transitive dependencies, and writes the complete set of all direct and transitive
dependencies pinned to consistent versions to `Kargo.lock`. You should run this whenever you update the dependencies in
`Kargo.toml`.

### deps

`kargo deps`

Vendors the dependencies exactly as specified in `Kargo.lock` into `.kargo/deps` in your project folder. You should run this whenever
you relock the dependencies.

### build

`kargo build`

Builds your project using the kotlin CLI compiler. This will produce a jar file in `target/<project.name>.jar` containing your code
and the kotlin runtime. This can be run directly with `java -jar <jarfile>` but you also have to pass all the dependencies on the classpath.

### assemble

`kargo assemble`

Builds a fat jar from the result of `kargo build` and your dependencies. Writes the fat jar to `target/assembly/<project.name>.jar` and also creates
a direcly executable version for unix as `target/assembly/<project.name>` and for windows as `target/assembly/<project.name>.bat`.

### native-image

`kargo native-image`

Build a native executable using GraalVM's native-image tool, using the fat jar produced from `kargo assemble`. (Because GraalVM is somewhat more complex
to install and set up, it's not one of the automatically vendored tools, and you'll need a working installation including the native-image command for this
to work.)

### lint

`kargo lint`

Runs [`ktlint`](https://ktlint.github.io/) over your kotlin sources and reports any errors.

### fmt

`kargo fmt`

Uses [`ktlint`](https://ktlint.github.io/) to format your kotlin sources in-place. This command is
known not to work on java 17.

## Kargo.toml reference

### Known keys

`[package]`: this section is required and contains the following keys

- `name` (required): the name of the project. This is used for the filename of the built artifacts.
- `kotlin_version` (required): the version of the kotlin compiler and runtime to use. (The kotlin compiler
  is vendored per project, so it's not a problem to have multiple projects on your machine using different
  kotlin versions.)
- `use_serialization_plugin` (optional, default `false`): if you use `kotlinx.serialization` in your project, set this
  to `true` to enable the corresponding compiler plugin.

`[dependencies]`: this section is required but can be empty

Each key in this section is the maven coordinates of the dependency, minus the version portion, and the value
is the version.

### Example

See the [`Kargo.toml`](https://github.com/cjfuller/kargo/blob/main/Kargo.toml) file in this repository used to
build kargo itself.

# Development

kargo is self-hosting, so download a release and use kargo to build itself rather than using the legacy gradle
build files used for bootstrapping. The gradle files will be removed at some later date.

## Roadmap

### Short-term

- `kargo test` for running tests (and exclusion of test files from builds, etc.)
- `kargo run` as a convenience to run the jar produced by `build` with the correct classpath,
  without having to run the time-consuming assembly step
- a number of other configuration options (option to turn off including the kotlin runtime in the jar,
  support of other source / test layouts, etc.)

### Medium-term
- better support for building / publishing libraries, rather than runnable applications

### Long-term
- kotlin js/native support
