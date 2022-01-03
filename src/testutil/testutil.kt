package kargo.testutil

import kargo.Config
import kargo.commands.Init
import java.nio.file.Path
import java.util.UUID
import kotlin.io.path.absolutePathString
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
    }
}

fun <T> withBasicProject(providedName: String? = null, block: ProjectSpec.() -> T): T {
    val temp = createTempDirectory()
    try {
        val name = providedName ?: UUID.randomUUID().toString()
        println("Creating test project $name in $temp")
        val config = """
            [package]
            name = "$name"
            kotlin_version = "1.6.10"
            package_layout = "flat"
            
            [dependencies]
            
        """.trimIndent()
        (temp / "Kargo.toml").writeText(config)
        (temp / "src").createDirectories()
        (temp / "src" / "Main.kt").writeText(
            """
            fun main() { }
            """.trimIndent()
        )
        val loaded = Config.load(temp / "Kargo.toml")
        return Config.withGlobal(loaded) {
            ProjectSpec(name = name, homeDir = temp).block()
        }
    } finally {
        if (temp.exists()) {
            temp.toFile().deleteRecursively()
        }
    }
}
