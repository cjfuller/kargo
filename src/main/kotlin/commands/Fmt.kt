package kargo.commands

import kargo.tools.KtLint

object Fmt : Runnable {
    override fun run() = KtLint.format()
}
