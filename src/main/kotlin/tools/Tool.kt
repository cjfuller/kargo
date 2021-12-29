package kargo.tools

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.readBytes
import kotlinx.coroutines.runBlocking
import java.nio.file.Path
import kotlin.io.path.writeBytes

interface Tool {
    val version: String
    fun executable(): Path
    fun downloadURL(version: String): String

    suspend fun download() {
        val client = HttpClient()
        runBlocking {
            val resp = client.get<HttpResponse>(downloadURL(version))
            val coursierExe = resp.readBytes()
            executable().writeBytes(coursierExe)
        }
    }
}
