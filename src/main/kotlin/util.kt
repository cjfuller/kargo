package kargo

import com.uchuhimo.konf.source.toml
import com.uchuhimo.konf.toValue
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.div
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.readLines
import kotlin.io.path.readText

val KARGO_DIR = Path(".kargo")
val CONFIG = Path("Kargo.toml")
val LOCK = Path("Kargo.lock")
val TARGET = Path("target")
val DEPS = KARGO_DIR / "deps"

fun isWindows(): Boolean = "win" in System.getProperty("os.name")

data class Config(val dependencies: Map<String, String>, val srcDir: Path = Path("src"), val kotlinVersion: String, val name: String) {
    companion object {
        fun load(): Config {
            val loadedConfig = com.uchuhimo.konf.Config()
                    .from.toml.string(CONFIG.readText())
            return Config(
                dependencies = loadedConfig
                    .at("dependencies")
                    .toValue(),
            kotlinVersion = loadedConfig.at("package.kotlin_version").toValue(),
            name = loadedConfig.at("package.name").toValue(),
            )
        }

        fun lockedDependencyStrings(): List<String> =
            LOCK.readLines()

        fun depsJarFiles(): List<String> =
            DEPS.listDirectoryEntries("*.jar").map { it.absolutePathString() }

        val global: Config by lazy { load() }

        fun sourceFiles(): List<String> =
            // TODO(colin): filter out tests
            global.srcDir.listDirectoryEntries("**/*.kt").map { it.absolutePathString()}
    }

    fun dependencyStrings(): List<String> =
        dependencies.map { "${it.key}:${it.value}" }
}
