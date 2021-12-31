package kargo

import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.div
import kotlin.io.path.isRegularFile
import kotlin.io.path.listDirectoryEntries

val KARGO_DIR = Path(".kargo")
val CONFIG = Path("Kargo.toml")
val LOCK = Path("Kargo.lock")
val TARGET = Path("target")
val DEPS = KARGO_DIR / "deps"

fun isWindows(): Boolean = "win" in System.getProperty("os.name").lowercase()

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
