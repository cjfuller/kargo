package kargo

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.nio.file.Path

class NoCommandProvidedException : Exception("No command provided. Ensure you build with `command = ...`.")
class CalledSubprocessException(val status: Int) : Exception("Subprocess exited with status $status.")

data class Subprocess(
    var command: String? = null,
    var args: MutableList<String> = mutableListOf(),
    private var stdout: ProcessBuilder.Redirect = ProcessBuilder.Redirect.INHERIT,
    private var stderr: ProcessBuilder.Redirect = ProcessBuilder.Redirect.INHERIT,
    private var captureOut: Boolean = false,
    var workingDirectory: Path = Config.global.root
) {
    fun arg(a: String) {
        this.args.add(a)
    }

    fun addArgs(vararg multiArgs: String) {
        for (a in multiArgs) {
            arg(a)
        }
    }

    fun run(): Result<Process> = this.runCatching {
        ProcessBuilder(command, *args.toTypedArray())
            .redirectInput(ProcessBuilder.Redirect.INHERIT)
            .redirectOutput(stdout)
            .directory(workingDirectory.toFile())
            .redirectError(stderr)
            .start()
    }

    fun check_output(): String {
        stdout = ProcessBuilder.Redirect.PIPE
        val proc = run().getOrThrow()
        val output = StringBuilder()
        runBlocking {
            async {
                withContext(Dispatchers.IO) {
                    val reader = proc.inputStream.bufferedReader()
                    var line = reader.readLine()
                    while (line != null) {
                        output.append(line)
                        output.append(System.lineSeparator())
                        line = reader.readLine()
                    }
                }
            }
            async {
                val retval = proc.waitFor()
                if (retval != 0) {
                    throw CalledSubprocessException(retval)
                }
            }
        }
        return output.toString()
    }

    fun run_check() {
        val retval = run().getOrThrow().waitFor()
        if (retval != 0) {
            throw CalledSubprocessException(retval)
        }
    }

    companion object {
        fun new(body: Subprocess.() -> Unit): Result<Subprocess> {
            val base = Subprocess()
            base.body()
            return if (base.command == null) {
                Result.failure(NoCommandProvidedException())
            } else {
                Result.success(base)
            }
        }

        fun jar(jarName: String, body: Subprocess.() -> Unit): Result<Subprocess> {
            val base = Subprocess()
            base.command = "java"
            base.arg("-jar")
            base.arg(jarName)
            base.body()
            return Result.success(base)
        }
    }
}
