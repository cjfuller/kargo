package kargo

import com.uchuhimo.konf.source.toml
import com.uchuhimo.konf.toValue
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.div
import kotlin.io.path.extension
import kotlin.io.path.readLines
import kotlin.io.path.readText

enum class ProjectLayout {
    CLASSIC,
    FLAT;

    companion object {
        fun of(s: String): ProjectLayout =
            if (s.lowercase() == "classic") {
                CLASSIC
            } else if (s.lowercase() == "flat") {
                FLAT
            } else {
                throw Exception("Unknown project layout $s")
            }
    }
}

private object DefaultPaths {
    val CONFIG = Path("Kargo.toml")
}

inline fun<reified T> optionalKey(config: com.uchuhimo.konf.Config, key: String, default: () -> T): T = try {
    config.at(key).toValue()
} catch(e: Exception) {
    default()
}

data class Config(
    val root: Path,
    val dependencies: Map<String, String>,
    val testDependencies: Map<String, String>,
    val srcDir: Path = root / Path("src"),
    val kotlinVersion: String,
    val name: String,
    val useSerializationPlugin: Boolean,
    val projectLayout: ProjectLayout = ProjectLayout.FLAT,
) {
    val kargoDir: Path = root / ".kargo"
    val lockFile: Path = root / "Kargo.lock"
    val testLockFile: Path = root / "Kargo.test.lock"
    val targetDir: Path = root / "target"
    val depsDir: Path = kargoDir / "deps"
    val testDepsDir: Path = kargoDir / "test" / "deps"

    fun lockedDependencyStrings(lockFile: Path): List<String> =
        lockFile.readLines()

    fun depsJarFiles(): List<Path> =
        recListPath(depsDir)
            .filter { it.extension == "jar" }
            .toList()

    fun testDepsJarFiles(): List<Path> =
        recListPath(testDepsDir)
            .filter { it.extension == "jar" }
            .toList()

    fun sourceFiles(): Sequence<Path> =
        recListPath(srcDir)
            .filter { it.extension == "kt" }
            .filter { !it.isTestFile() }

    fun testFiles(): Sequence<Path> =
        recListPath(srcDir)
            .filter { it.extension == "kt" }
            .filter { it.isTestFile() }

    companion object {
        fun commonTestDependencies(kotlinVersion: String): Map<String, String> = mapOf(
            "org.jetbrains.kotlin:kotlin-test-common" to kotlinVersion,
            "org.jetbrains.kotlin:kotlin-test-annotations-common" to kotlinVersion,
            "org.jetbrains.kotlin:kotlin-test-junit5" to kotlinVersion,
        )

        fun load(configPath: Path = DefaultPaths.CONFIG): Config {
            val loadedConfig = com.uchuhimo.konf.Config()
                .from.toml.string(configPath.readText())
            val kotlinVersion: String = loadedConfig.at("package.kotlin_version").toValue()
            return Config(
                root = configPath.toAbsolutePath().parent,
                dependencies = loadedConfig
                    .at("dependencies")
                    .toValue<Map<String, String>>()
                    .map { it.key.trim { it == '"' } to it.value }
                    .toMap(),
                kotlinVersion = kotlinVersion,
                name = loadedConfig.at("package.name").toValue(),
                testDependencies = commonTestDependencies(kotlinVersion) +
                    optionalKey<Map<String, String>>(loadedConfig, "test.dependencies") { mapOf() }
                    .map { it.key.trim { it == '"' } to it.value }
                    .toMap(),
            useSerializationPlugin = optionalKey(
                    loadedConfig,"package.use_serialization_plugin" ) { false },
                projectLayout = optionalKey(
                    loadedConfig, "package.project_layout") { "flat"}.let(ProjectLayout::of)
            )
        }

        // TODO(colin): do something more principled here for mocking the config in tests.
        val _loaded: Config by lazy { load() }
        val _global: ThreadLocal<Config?> = ThreadLocal()
        val global: Config
            get() = _global.get() ?: _loaded

        fun<T> withGlobal(cfg: Config, block: () -> T): T {
            val old = _global.get()
            try {
                _global.set(cfg)
                return block()
            } finally {
                _global.set(old)
            }
        }
    }
}
