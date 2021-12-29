package kargo.tools

import kargo.Config
import kargo.KARGO_DIR
import kargo.Subprocess
import kargo.TARGET
import java.io.File
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.div

object KotlinC : Tool {
    override val version: String by lazy { Config.global.kotlinVersion }
    override fun executable(): Path = KARGO_DIR / "kotlinc"
    override fun downloadURL(version: String): String =
        "https://github.com/JetBrains/kotlin/releases/download/v${version}/kotlin-compiler-${version}.zip"

    fun build() {
        Subprocess.jar(executable().absolutePathString()) {
            addArgs("-cp", Config.depsJarFiles().joinToString(File.pathSeparator))
            addArgs("-d", (TARGET / "${Config.global.name}.jar").absolutePathString())
            // TODO(colin): include or not based on package type
            arg("-include-runtime")
            addArgs(*Config.sourceFiles().toTypedArray())
        }.getOrThrow().check_output()
    }
}