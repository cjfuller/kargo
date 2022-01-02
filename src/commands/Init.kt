package kargo.commands

import kargo.Config
import kargo.tools.Coursier
import kargo.tools.JUnitRunner
import kargo.tools.KotlinC
import kargo.tools.KtLint
import kotlinx.coroutines.runBlocking
import kotlin.io.path.createDirectories
import kotlin.io.path.exists

object Init : Runnable {
    override fun run() {
        if (!Config.global.kargoDir.exists()) {
            Config.global.kargoDir.createDirectories()
            Config.global.depsDir.createDirectories()
            Config.global.targetDir.createDirectories()
        }
        runBlocking {
            Coursier.download()
            KtLint.download()
            KotlinC.download()
            JUnitRunner.download()
        }
    }
}
