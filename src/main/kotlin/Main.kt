package kargo

import kotlinx.cli.ArgParser
import kotlinx.cli.ExperimentalCli
import kotlinx.cli.Subcommand

@ExperimentalCli
fun main(args: Array<String>) {
    val parser = ArgParser("kargo")

    class Init : Subcommand("init", "Required first-time setup") {
        override fun execute() {
            kargo.commands.Init.run()
        }
    }

    class Lock : Subcommand("lock", "Lock dependencies") {
        override fun execute() {
            kargo.commands.Lock.run()
        }
    }

    class Build : Subcommand("build", "Build the project") {
        override fun execute() {
            kargo.commands.Build.run()
        }
    }

    class Deps : Subcommand("deps", "Fetch and vendor dependencies from lockfile") {
        override fun execute() {
            kargo.commands.Deps.run()
        }
    }

    class Fmt : Subcommand("fmt", "Format kotlin sources using ktlint") {
        override fun execute() {
            kargo.commands.Fmt.run()
        }
    }

    class Lint : Subcommand("lint", "Lint kotlin sources using ktlint") {
        override fun execute() {
            kargo.commands.Lint.run()
        }
    }

    class Assemble : Subcommand("assemble", "Assemble a fat jar with all deps included") {
        override fun execute() {
            kargo.commands.Assemble.run()
        }
    }

    val nativeImageDesc = (
        "Build a native image using GraalVM for the current OS. You must already have " +
            "a working install of GraalVM, including the native-image tool, for this to work."
        )
    class GraalNativeImage : Subcommand("native-image", nativeImageDesc) {
        override fun execute() {
            kargo.commands.GraalNativeImage.run()
        }
    }

    parser.subcommands(Build(), Init(), Lock(), Deps(), Fmt(), Lint(), Assemble(), GraalNativeImage())
    parser.parse(args)
}
