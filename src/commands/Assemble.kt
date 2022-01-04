package kargo.commands

import kargo.Config
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
import kotlin.io.path.writeBytes

object Assemble : Runnable {
    val assemblyDir: Path
        get() = Config.global.kargoDir / "assembly"
    val metaInf = Path("META-INF")
    val outputJar: Path
        get() = (
            Config.global.targetDir / "assembly" / (
                KotlinC.outputJar().relativeTo(
                    Config.global.targetDir
                )
                )
            )

    val windowsStub = """
        @echo off
        java -jar "%~dp0%~n0%~x0" %*
        exit /b
        
    """.trimIndent().replace("\n", "\r\n")
    val unixStub = """
        #!/bin/sh
        THIS_FILE=`which "${'$'}0" 2>/dev/null`
        [ ${'$'}? -gt 0 -a -f "${'$'}0" ] && THIS_FILE="./${'$'}0"
        exec java -jar ${'$'}THIS_FILE "${'$'}@"
        
    """.trimIndent()

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
                    val targetDigest = MessageDigest.getInstance("SHA-1")
                        .digest(targetPath.readBytes())
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
        for (dep in Config.global.depsJarFiles()) {
            copyJarContents(dep, includeMetaInf = false)
        }
        copyJarContents(KotlinC.outputJar(), includeMetaInf = true)
        if (outputJar.parent.notExists()) {
            outputJar.parent.createDirectories()
        }
        outputJar.deleteIfExists()
        val zip = ZipFile(outputJar.absolutePathString())
        for (entry in assemblyDir.listDirectoryEntries()) {
            if (entry.isRegularFile()) {
                zip.addFile(entry.toFile())
            } else {
                zip.addFolder(entry.toFile())
            }
        }
        val assemblyContents = outputJar.readBytes()
        val winAssembly = windowsStub.toByteArray() + assemblyContents
        (outputJar.parent / (Config.global.name + ".bat")).writeBytes(winAssembly)
        val unixAssembly = unixStub.toByteArray() + assemblyContents
        (outputJar.parent / Config.global.name).writeBytes(unixAssembly)
    }
}
