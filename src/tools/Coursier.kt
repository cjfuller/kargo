package kargo.tools

import kargo.Config
import kargo.Subprocess
import kargo.recListPath
import java.nio.file.Path
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

    fun dependencyStrings(from: Map<String, String>): List<String> =
        from.map { "${it.key}:${it.value}" }

    fun lock(from: Map<String, String>, to: Path) {
        val specs = dependencyStrings(from)
        val deps = Subprocess.jar(executable().absolutePathString()) {
            arg("resolve")
            addArgs(*specs.toTypedArray())
        }.getOrThrow().check_output()
        to.writeText(deps)
    }

    fun lock_deps() {
        lock(Config.global.dependencies, Config.global.lockFile)
        lock(Config.global.testDependencies, Config.global.testLockFile)
    }

    fun clear_deps(inDir: Path) {
        if (inDir.exists()) {
            for (jar in recListPath(inDir).filter { it.extension == "jar" }) {
                jar.deleteExisting()
            }
        }
    }

    fun clear_deps() {
        clear_deps(Config.global.depsDir)
        clear_deps(Config.global.testDepsDir)
    }

    fun fetch_deps(lockFile: Path, depsDir: Path) {
        if (lockFile.notExists()) {
            lock_deps()
        }
        if (depsDir.notExists()) {
            depsDir.createDirectories()
        }
        val depFiles = Subprocess.jar(executable().absolutePathString()) {
            arg("fetch")
            addArgs(*Config.global.lockedDependencyStrings(lockFile).toTypedArray())
        }.getOrThrow().check_output()
        depFiles.lines().forEach {
            if (it.isNotEmpty()) {
                val source = Path(it)
                source.copyTo(depsDir / source.name, overwrite = true)
            }
        }
    }

    fun fetch_deps() {
        fetch_deps(Config.global.lockFile, Config.global.depsDir)
        fetch_deps(Config.global.testLockFile, Config.global.testDepsDir)
    }
}
