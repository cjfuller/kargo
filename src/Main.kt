package kargo

import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.ExperimentalCli
import kotlinx.cli.Subcommand
import kotlin.io.path.Path

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

    class Run(val moreArgs: List<String>) : Subcommand("run", "Run the jar produced by `build` or the supplied script") {
        val script: String? by parser.option(ArgType.String, shortName = "s", description = "Script file to run")
        override fun execute() {
            kargo.commands.Run(script = script?.let { Path(it) }, runArgs = moreArgs).run()
        }
    }

    class Test : Subcommand("test", "Run tests using JUnit") {
        override fun execute() {
            kargo.commands.Test.run()
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

    val argsForParser = args.takeWhile { it != "--" }
    val argsToPassOn = args.dropWhile { it != "--" }.dropWhile { it == "--" }

    parser.subcommands(Build(), Init(), Lock(), Deps(), Fmt(), Lint(), Assemble(), GraalNativeImage(), Run(argsToPassOn), Test())
    parser.parse(argsForParser.toTypedArray())
}
