package kargo.tools

import kargo.Config
import kargo.DEPS
import kargo.KARGO_DIR
import kargo.LOCK
import kargo.Subprocess
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.copyTo
import kotlin.io.path.createDirectories
import kotlin.io.path.div
import kotlin.io.path.name
import kotlin.io.path.notExists
import kotlin.io.path.writeText

object Coursier : Tool {
    override val version = "2.0.13"

    override fun executable() = KARGO_DIR / "cs"

    override fun downloadURL(version: String): String =
        "https://github.com/coursier/coursier/releases/download/v$version/coursier"

    fun lock_deps() {
        val specs = Config.global.dependencyStrings()
        val deps = Subprocess.jar(executable().absolutePathString()) {
            arg("resolve")
            addArgs(*specs.toTypedArray())
        }.getOrThrow().check_output()
        LOCK.writeText(deps)
    }

    fun fetch_deps() {
        if (LOCK.notExists()) {
            lock_deps()
        }
        if (DEPS.notExists()) {
            DEPS.createDirectories()
        }
        val depFiles = Subprocess.jar(executable().absolutePathString()) {
            arg("fetch")
            addArgs(*Config.lockedDependencyStrings().toTypedArray())
        }.getOrThrow().check_output()
        println("Copying...")
        depFiles.lines().forEach {
            if (it.isNotEmpty()) {
                val source = Path(it)
                println("$source -> ${DEPS / source.name}")
                source.copyTo(DEPS / source.name, overwrite = true)
            }
        }
    }
}
