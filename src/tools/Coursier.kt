package kargo.tools

import kargo.Config
import kargo.Subprocess
import kargo.recListPath
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.copyTo
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteExisting
import kotlin.io.path.div
import kotlin.io.path.exists
import kotlin.io.path.extension
import kotlin.io.path.name
import kotlin.io.path.notExists
import kotlin.io.path.writeText

object Coursier : Tool {
    override val version = "2.0.13"

    override fun executable() = Config.global.kargoDir / "cs"

    override fun downloadURL(version: String): String =
        "https://github.com/coursier/coursier/releases/download/v$version/coursier"

    fun lock_deps() {
        val specs = Config.global.dependencyStrings()
        val deps = Subprocess.jar(executable().absolutePathString()) {
            arg("resolve")
            addArgs(*specs.toTypedArray())
        }.getOrThrow().check_output()
        Config.global.lockFile.writeText(deps)
    }

    fun clear_deps() {
        if (Config.global.depsDir.exists()) {
            for (jar in recListPath(Config.global.depsDir).filter { it.extension == "jar" }) {
                jar.deleteExisting()
            }
        }
    }

    fun fetch_deps() {
        if (Config.global.lockFile.notExists()) {
            lock_deps()
        }
        if (Config.global.depsDir.notExists()) {
            Config.global.depsDir.createDirectories()
        }
        val depFiles = Subprocess.jar(executable().absolutePathString()) {
            arg("fetch")
            addArgs(*Config.global.lockedDependencyStrings().toTypedArray())
        }.getOrThrow().check_output()
        depFiles.lines().forEach {
            if (it.isNotEmpty()) {
                val source = Path(it)
                source.copyTo(Config.global.depsDir / source.name, overwrite = true)
            }
        }
    }
}
