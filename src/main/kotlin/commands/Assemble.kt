package kargo.commands

import kargo.Config
import kargo.KARGO_DIR
import kargo.TARGET
import kargo.recListPath
import kargo.tools.KotlinC
import net.lingala.zip4j.ZipFile
import java.nio.file.Path
import java.security.MessageDigest
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.appendLines
import kotlin.io.path.copyTo
import kotlin.io.path.createDirectories
import kotlin.io.path.createTempDirectory
import kotlin.io.path.deleteIfExists
import kotlin.io.path.div
import kotlin.io.path.exists
import kotlin.io.path.isRegularFile
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.io.path.notExists
import kotlin.io.path.readBytes
import kotlin.io.path.readLines
import kotlin.io.path.relativeTo

object Assemble : Runnable {
    val assemblyDir = KARGO_DIR / "assembly"
    val metaInf = Path("META-INF")

    fun copyJarContents(jar: Path, includeMetaInf: Boolean = false) {
        val targetDir = createTempDirectory()
        try {
            ZipFile(jar.absolutePathString()).extractAll(targetDir.absolutePathString())
            for (file in recListPath(targetDir)) {
                val relpath = file.relativeTo(targetDir)
                val targetPath = assemblyDir / relpath
                if (relpath.startsWith(metaInf)) {
                    if (relpath.startsWith(metaInf / "services")) {
                        if (targetPath.exists()) {
                            targetPath.appendLines(file.readLines())
                            continue
                        }
                    } else if (!includeMetaInf) {
                        continue
                    }
                }
                if (relpath.name == "module-info.class") {
                    continue
                }
                if (!targetPath.parent.exists()) {
                    targetPath.parent.createDirectories()
                }
                if (targetPath.exists()) {
                    val targetDigest = MessageDigest.getInstance("SHA-1").digest(targetPath.readBytes())
                    val sourceDigest = MessageDigest.getInstance("SHA-1").digest(file.readBytes())
                    if (MessageDigest.isEqual(targetDigest, sourceDigest)) {
                        continue
                        // Otherwise, we'll throw below when copying.
                    }
                }
                file.copyTo(targetPath)
            }
        } finally {
            targetDir.toFile().deleteRecursively()
        }
    }

    override fun run() {
        if (assemblyDir.exists()) {
            assemblyDir.toFile().deleteRecursively()
        }
        assemblyDir.createDirectories()
        for (dep in Config.depsJarFiles()) {
            copyJarContents(Path(dep), includeMetaInf = false)
        }
        copyJarContents(KotlinC.outputJar(), includeMetaInf = true)
        val outDir = TARGET / "assembly"
        if (outDir.notExists()) {
            outDir.createDirectories()
        }
        val outFile = (outDir / (KotlinC.outputJar().relativeTo(TARGET)))
        outFile.deleteIfExists()
        val zip = ZipFile(outFile.absolutePathString())
        for (entry in assemblyDir.listDirectoryEntries()) {
            if (entry.isRegularFile()) {
                zip.addFile(entry.toFile())
            } else {
                zip.addFolder(entry.toFile())
            }
        }
    }
}
