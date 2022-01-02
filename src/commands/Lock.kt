package kargo.commands

import kargo.tools.Coursier

object Lock : Runnable {
    override fun run() = Coursier.lock_deps()
}
