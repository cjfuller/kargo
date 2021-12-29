package kargo.commands

import kargo.DEPS
import kargo.KARGO_DIR
import kargo.TARGET
import kargo.tools.Coursier
import kargo.tools.KotlinC
import kargo.tools.KtLint
import kotlinx.coroutines.runBlocking
import kotlin.io.path.createDirectories
import kotlin.io.path.exists

object Init : Runnable {
    override fun run() {
        if (!KARGO_DIR.exists()) {
            KARGO_DIR.createDirectories()
            DEPS.createDirectories()
            TARGET.createDirectories()
        }
        runBlocking {
            Coursier.download()
            KtLint.download()
            KotlinC.download()
        }
    }
}
