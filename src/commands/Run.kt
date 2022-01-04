package kargo.commands

import kargo.Config
import kargo.Subprocess
import kargo.toClasspathString
import kargo.tools.KotlinC
import net.lingala.zip4j.ZipFile
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

            Subprocess.new {
                command = "java"
                (Config.global.depsJarFiles() + listOf(KotlinC.outputJar()))
                    .toClasspathString()?.let {
                        addArgs("-cp", it)
                    }
                arg(mainClass)
                addArgs(*runArgs.toTypedArray())
            }.getOrThrow().run_check()
        } else {
            KotlinC.script(script, runArgs)
        }
    }
}
