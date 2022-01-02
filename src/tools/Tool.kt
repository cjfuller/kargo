package kargo.tools

import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.readBytes
import kargo.Config
import kotlinx.coroutines.runBlocking
import net.lingala.zip4j.ZipFile
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteIfExists
import kotlin.io.path.div
import kotlin.io.path.exists
import kotlin.io.path.notExists
import kotlin.io.path.writeBytes

interface Tool {
    val version: String
    fun executable(): Path
    fun downloadURL(version: String): String

    suspend fun download() {
        val client = HttpClient()
        executable().deleteIfExists()
        runBlocking {
            val resp = client.get<HttpResponse>(downloadURL(version))
            val exe = resp.readBytes()
            executable().writeBytes(exe)
        }
    }
}

interface BundledTool {
    fun path(): Path
}

interface ToolZipBundle<T : BundledTool> {
    val version: String
    fun zipFileTarget(): Path
    fun folderUnzipTarget(): Path
    fun downloadURL(version: String): String

    fun download() {
        val client = HttpClient(Apache)
        zipFileTarget().deleteIfExists()
        val zipContents = runBlocking {
            val resp = client.get<HttpResponse>(downloadURL(version))
            resp.readBytes()
        }
        zipFileTarget().writeBytes(zipContents)
        if (folderUnzipTarget().exists() && folderUnzipTarget() != Config.global.kargoDir) {
            folderUnzipTarget().toFile().deleteRecursively()
        }
        if (folderUnzipTarget().notExists()) {
            folderUnzipTarget().createDirectories()
        }
        ZipFile(zipFileTarget().absolutePathString()).extractAll(folderUnzipTarget().absolutePathString())
    }

    fun path(tool: T): Path =
        folderUnzipTarget() / tool.path()
}
