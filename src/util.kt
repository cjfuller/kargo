package kargo

import java.io.File
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.div
import kotlin.io.path.isRegularFile
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.relativeTo

fun isWindows(): Boolean = "win" in System.getProperty("os.name").lowercase()

fun Path.isTestFile(): Boolean =
    if (Config.global.projectLayout == ProjectLayout.FLAT) {
        absolutePathString().endsWith("_test.kt")
    } else {
        relativeTo(Config.global.srcDir).startsWith(Path("test")/"kotlin")
    }

fun recListPath(p: Path): Sequence<Path> = sequence {
    for (entry in p.listDirectoryEntries()) {
        if (entry.isRegularFile()) {
            yield(entry)
        } else {
            for (subentry in recListPath(entry)) {
                yield(subentry)
            }
        }
    }
}

fun List<Path>.toClasspathString(): String? = if (isEmpty()) {
    null
} else {
    joinToString(File.pathSeparator) { it.absolutePathString() }
}