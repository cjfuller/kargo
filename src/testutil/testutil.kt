package kargo.testutil

import kargo.Config
import kargo.commands.Deps
import kargo.commands.Init
import kargo.commands.Lock
import java.nio.file.Path
import java.util.UUID
import kotlin.io.path.createDirectories
import kotlin.io.path.createTempDirectory
import kotlin.io.path.div
import kotlin.io.path.exists
import kotlin.io.path.writeText

data class ProjectSpec(
    val name: String,
    val homeDir: Path
) {
    fun initialize() {
        Init.run()
        Lock.run()
        Deps.run()
    }
}

fun <T> withBasicProject(providedName: String? = null, block: ProjectSpec.() -> T): T {
    val name = providedName ?: UUID.randomUUID().toString()
    val config = """
        [package]
        name = "$name"
        kotlin_version = "1.6.10"
        package_layout = "flat"
        
        [dependencies]
        
    """.trimIndent()
    return withProjectConfig(config, block)
}

fun <T> withProjectConfig(config: String, block: ProjectSpec.() -> T): T {
    val temp = createTempDirectory()
    try {
        (temp / "Kargo.toml").writeText(config)
        (temp / "src").createDirectories()
        (temp / "src" / "Main.kt").writeText(
            """
            fun main() { }
            """.trimIndent()
        )
        val loaded = Config.load(temp / "Kargo.toml")
        return Config.withGlobal(loaded) {
            ProjectSpec(name = loaded.name, homeDir = temp).block()
        }
    } finally {
        if (temp.exists()) {
            temp.toFile().deleteRecursively()
        }
    }
}
