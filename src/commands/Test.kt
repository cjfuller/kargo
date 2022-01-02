package kargo.commands

import kargo.tools.JUnitRunner

object Test : Runnable {
    override fun run() {
        Build.run()
        JUnitRunner.test()
    }
}
