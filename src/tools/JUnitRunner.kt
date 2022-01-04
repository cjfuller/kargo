package kargo.tools

import kargo.Config
import kargo.Subprocess
import kargo.recListPath
import kargo.toClasspathString
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.div
import kotlin.io.path.extension
import kotlin.io.path.invariantSeparatorsPathString
import kotlin.io.path.relativeTo

object JUnitRunner : Tool {
    override val version = "1.8.2"
    override fun executable(): Path = Config.global.kargoDir / "junit-standalone.jar"
    override fun downloadURL(version: String): String =
        "https://repo1.maven.org/maven2/org/junit/platform/junit-platform-console-standalone" +
            "/$version/junit-platform-console-standalone-$version.jar"

    fun test() {
        KotlinC.buildTests()
        val testClasses = recListPath(KotlinC.testOutputDir()).filter { it.extension == "class" }
        val classPath = (
            Config.global.depsJarFiles() + listOf(
                KotlinC.outputJar(),
                KotlinC.testOutputDir(),
            )
            ).toClasspathString()
        Subprocess.jar(executable().absolutePathString()) {
            classPath?.let { addArgs("-cp", it) }
            arg("--fail-if-no-tests")
            arg("--disable-banner")
            addArgs("-n", ".+")
            for (testFile in testClasses) {
                addArgs(
                    "-c",
                    testFile.relativeTo(KotlinC.testOutputDir())
                        .invariantSeparatorsPathString.replace('/', '.').replace(".class", "")
                )
            }
        }.getOrThrow().run_check()
    }
}
