package kargo.commands

import kargo.tools.Coursier

object Deps : Runnable {
    override fun run() = Coursier.fetch_deps()
}
