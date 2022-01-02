package kargo.commands

import kargo.tools.Coursier

object Deps : Runnable {
    override fun run() {
        Coursier.clear_deps()
        Coursier.fetch_deps()
    }
}
