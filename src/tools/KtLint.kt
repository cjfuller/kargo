package kargo.tools

import kargo.Config
import kargo.Subprocess
import java.io.File
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.div

object KtLint : Tool {
    override val version = "0.43.2"

    override fun executable(): Path = Config.global.kargoDir / "ktlint"

    override fun downloadURL(version: String): String =
        "https://github.com/pinterest/ktlint/releases/download/$version/ktlint"

    fun lint(format_in_place: Boolean = false) {
        Subprocess.jar(executable().absolutePathString()) {
            if (format_in_place) {
                arg("-F")
            }
            // TODO(colin): what about .kts files?
            val glob = listOf(Config.global.srcDir.absolutePathString(), "**", "*.kt").joinToString(File.separator)
            arg(glob)
        }.getOrThrow().run_check()
    }

    fun format() {
        lint(format_in_place = true)
    }
}
