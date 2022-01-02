package kargo.tools

import kargo.Config
import kargo.Subprocess
import kargo.isWindows
import kargo.toClasspathString
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.createDirectories
import kotlin.io.path.div
import kotlin.io.path.exists

enum class KotlinCBundle(val relpath: Path) : BundledTool {
    KOTLINC(if (isWindows()) { Path("kotlinc/bin/kotlinc.bat") } else { Path("kotlinc/bin/kotlinc") }),
    SER_PLUGIN(Path("kotlinc/lib/kotlinx-serialization-compiler-plugin.jar"));

    override fun path(): Path = relpath
}

object KotlinC : ToolZipBundle<KotlinCBundle> {
    override val version: String by lazy { Config.global.kotlinVersion }
    override fun zipFileTarget(): Path = Config.global.kargoDir / "kotlinc.zip"
    // The kotlin zip has a top-level `kotlinc` directory, so we extract directly into KARGO_DIR.
    override fun folderUnzipTarget(): Path = Config.global.kargoDir
    override fun downloadURL(version: String): String =
        "https://github.com/JetBrains/kotlin/releases/download/v$version/kotlin-compiler-$version.zip"
    fun outputJar(): Path = Config.global.targetDir / "${Config.global.name}.jar"
    fun testOutputDir(): Path = Config.global.targetDir / "test" / "classes"

    override fun download() {
        val kotlincDir = (folderUnzipTarget() / "kotlinc")
        if (kotlincDir.exists()) {
            kotlincDir.toFile().deleteRecursively()
        }
        super.download()
    }


    fun script(script: Path, scriptArgs: List<String>) {
        Subprocess.new {
            command = path(KotlinCBundle.KOTLINC).absolutePathString()
            addArgs("-script", script.absolutePathString())
            addArgs("-cp", (Config.global.depsJarFiles() + outputJar()).toClasspathString())
            if (Config.global.useSerializationPlugin) {
                arg("-Xplugin=${path(KotlinCBundle.SER_PLUGIN).absolutePathString()}")
            }
            if (scriptArgs.isNotEmpty()) {
                addArgs("--", *scriptArgs.toTypedArray())
            }
        }.getOrThrow().run_check()
    }

    fun buildFiles(files: Sequence<Path>, target: Path, additionalClasspath: List<Path> = listOf()) {
        Subprocess.new {
            command = path(KotlinCBundle.KOTLINC).absolutePathString()
            addArgs("-d", target.absolutePathString())
            // TODO(colin): include or not based on package type
            arg("-include-runtime")
            addArgs("-cp", (Config.global.depsJarFiles() + additionalClasspath).toClasspathString())
            if (Config.global.useSerializationPlugin) {
                arg("-Xplugin=${path(KotlinCBundle.SER_PLUGIN).absolutePathString()}")
            }
            files.map { it.absolutePathString() }.forEach(::arg)
        }.getOrThrow().check_output()
    }

    fun buildTests() {
        if (testOutputDir().exists()) {
            testOutputDir().toFile().deleteRecursively()
        }
        testOutputDir().createDirectories()
        buildFiles(Config.global.testFiles(), testOutputDir(), listOf(outputJar()))
    }

    fun build() {
        buildFiles(Config.global.sourceFiles(), outputJar())
    }
}
