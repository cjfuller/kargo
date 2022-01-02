package kargo.commands

import kargo.Subprocess
import kotlin.io.path.absolutePathString

object GraalNativeImage : Runnable {
    override fun run() {
        Subprocess.new {
            command = "native-image"
            arg("-jar")
            arg(Assemble.outputJar.absolutePathString())
            workingDirectory = Assemble.outputJar.parent
        }.getOrThrow().run_check()
    }
}
