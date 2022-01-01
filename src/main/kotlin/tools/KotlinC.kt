package kargo.tools

import kargo.Config
import kargo.KARGO_DIR
import kargo.Subprocess
import kargo.TARGET
import kargo.isWindows
import java.io.File
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.div
import kotlin.io.path.exists

enum class KotlinCBundle(val relpath: Path) : BundledTool {
    KOTLINC(if (isWindows()) { Path("kotlinc/bin/kotlinc.bat") } else { Path("kotlinc/bin/kotlinc") }),
    SER_PLUGIN(Path("kotlinc/lib/kotlinx-serialization-compiler-plugin.jar"));

    override fun path(): Path = relpath
}

object KotlinC : ToolZipBundle<KotlinCBundle> {
    override val version: String by lazy { Config.global.kotlinVersion }
    override fun zipFileTarget(): Path = KARGO_DIR / "kotlinc.zip"
    // The kotlin zip has a top-level `kotlinc` directory, so we extract directly into KARGO_DIR.
    override fun folderUnzipTarget(): Path = KARGO_DIR
    override fun downloadURL(version: String): String =
        "https://github.com/JetBrains/kotlin/releases/download/v$version/kotlin-compiler-$version.zip"
    fun outputJar(): Path = TARGET / "${Config.global.name}.jar"

    override fun download() {
        val kotlincDir = (folderUnzipTarget() / "kotlinc")
        if (kotlincDir.exists()) {
            kotlincDir.toFile().deleteRecursively()
        }
        super.download()
    }

    fun depsClasspath(): String = Config.depsJarFiles().joinToString(File.pathSeparator)

    fun script(script: Path, scriptArgs: List<String>) {
        Subprocess.new {
            command = path(KotlinCBundle.KOTLINC).absolutePathString()
            addArgs("-script", script.absolutePathString())
            addArgs("-cp", listOf(depsClasspath(), outputJar().absolutePathString()).joinToString(File.pathSeparator))
            if (Config.global.useSerializationPlugin) {
                arg("-Xplugin=${path(KotlinCBundle.SER_PLUGIN).absolutePathString()}")
            }
            if (scriptArgs.isNotEmpty()) {
                addArgs("--", *scriptArgs.toTypedArray())
            }
        }.getOrThrow().run_check()
    }

    fun build() {
        Subprocess.new {
            command = path(KotlinCBundle.KOTLINC).absolutePathString()
            addArgs("-d", outputJar().absolutePathString())
            // TODO(colin): include or not based on package type
            arg("-include-runtime")
            addArgs("-cp", depsClasspath())
            if (Config.global.useSerializationPlugin) {
                arg("-Xplugin=${path(KotlinCBundle.SER_PLUGIN).absolutePathString()}")
            }
            arg(Config.global.srcDir.absolutePathString())
        }.getOrThrow().check_output()
    }
}
