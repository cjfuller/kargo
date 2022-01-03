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
    val KARGO_DIR = Path(".kargo")
    val CONFIG = Path("Kargo.toml")
    val LOCK = Path("Kargo.lock")
    val TARGET = Path("target")
    val DEPS = KARGO_DIR / "deps"
}

inline fun<reified T> optionalKey(config: com.uchuhimo.konf.Config, key: String, default: () -> T): T = try {
    config.at(key).toValue()
} catch(e: Exception) {
    default()
}

data class Config(
    val root: Path,
    val dependencies: Map<String, String>,
    val srcDir: Path = root / Path("src"),
    val kotlinVersion: String,
    val name: String,
    val useSerializationPlugin: Boolean,
    val projectLayout: ProjectLayout = ProjectLayout.FLAT,
    val kargoDir: Path = root / DefaultPaths.KARGO_DIR,
    val lockFile: Path = root / DefaultPaths.LOCK,
    val targetDir: Path = root / DefaultPaths.TARGET,
    val depsDir: Path = root / DefaultPaths.DEPS,
) {

    fun lockedDependencyStrings(): List<String> =
        lockFile.readLines()

    fun depsJarFiles(): List<Path> =
        recListPath(depsDir)
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

    fun dependencyStrings(): List<String> =
        dependencies.map { "${it.key}:${it.value}" }

    companion object {
        fun load(configPath: Path = DefaultPaths.CONFIG): Config {
            val loadedConfig = com.uchuhimo.konf.Config()
                .from.toml.string(configPath.readText())
            return Config(
                root = configPath.toAbsolutePath().parent,
                dependencies = loadedConfig
                    .at("dependencies")
                    .toValue<Map<String, String>>()
                    .map { it.key.trim { it == '"' } to it.value }
                    .toMap(),
                kotlinVersion = loadedConfig.at("package.kotlin_version").toValue(),
                name = loadedConfig.at("package.name").toValue(),
                useSerializationPlugin = optionalKey(loadedConfig,"package.use_serialization_plugin" ) { false },
                projectLayout = optionalKey(loadedConfig, "package.project_layout") { "flat"}.let(ProjectLayout::of)
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
