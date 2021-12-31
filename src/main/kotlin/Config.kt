package kargo

import com.uchuhimo.konf.ConfigSpec
import com.uchuhimo.konf.source.toml
import com.uchuhimo.konf.toValue
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.extension
import kotlin.io.path.readLines
import kotlin.io.path.readText

data class Config(
    val dependencies: Map<String, String>,
    val srcDir: Path = Path("src"),
    val kotlinVersion: String,
    val name: String,
    val useSerializationPlugin: Boolean
) {
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
                useSerializationPlugin = try {
                    loadedConfig.at("package.use_serialization_plugin").toValue()
                } catch (e: Exception) {
                    false
                }
            )
        }

        fun lockedDependencyStrings(): List<String> =
            LOCK.readLines()

        fun depsJarFiles(): List<String> =
            recListPath(DEPS)
                .filter { it.extension == "jar" }
                .map { it.absolutePathString() }
                .toList()

        val global: Config by lazy { load() }

        fun sourceFiles(): List<String> =
            // TODO(colin): filter out tests
            recListPath(global.srcDir)
                .filter { it.extension == "kt" }
                .map { it.absolutePathString() }
                .toList()
    }

    fun dependencyStrings(): List<String> =
        dependencies.map { "${it.key}:${it.value}" }
}
