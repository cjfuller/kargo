package kargo.commands

import kargo.tools.KtLint

object Lint : Runnable {
    override fun run() = KtLint.lint()
}
