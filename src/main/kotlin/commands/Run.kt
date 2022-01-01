package kargo.commands

import kargo.Subprocess
import kargo.tools.KotlinC
import net.lingala.zip4j.ZipFile
import java.io.File
import java.nio.file.Path
import kotlin.io.path.absolutePathString

class Run(val script: Path?, val runArgs: List<String>) : Runnable {
    override fun run() {
        if (script == null) {
            val mainJar = ZipFile(KotlinC.outputJar().absolutePathString())
            val manifest = mainJar.getInputStream(mainJar.getFileHeader("META-INF/MANIFEST.mf"))
                .bufferedReader().readText()
            val mainClass = Regex("""Main-Class: (.*)$""", RegexOption.MULTILINE)
                .find(manifest)?.groupValues?.get(1)
                ?: throw Exception(
                    "Could not locate Main-Class attribute in built jar file. " +
                        "Is there a main() defined?"
                )

            val classPath = listOf(
                KotlinC.depsClasspath(), KotlinC.outputJar().absolutePathString()
            ).joinToString(File.pathSeparator)

            Subprocess.new {
                command = "java"
                addArgs("-cp", classPath)
                arg(mainClass)
                addArgs(*runArgs.toTypedArray())
            }.getOrThrow().run_check()
        } else {
            KotlinC.script(script, runArgs)
        }
    }
}
