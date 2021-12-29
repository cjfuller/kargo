package kargo.commands

import kargo.tools.KotlinC

object Build : Runnable {
    override fun run() = KotlinC.build()
}