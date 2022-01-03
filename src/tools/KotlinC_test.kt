package kargo.tools

import kargo.commands.Build
import kargo.commands.Deps
import kargo.commands.Lock
import kargo.testutil.withBasicProject
import org.junit.jupiter.api.Test
import kotlin.io.path.exists
import kotlin.test.assertTrue

class KotlinCTest {
    @Test
    fun testEmptyDependenciesBuildsOk() {
        withBasicProject {
            initialize()
            Lock.run()
            Deps.run()
            Build.run()
            assertTrue(KotlinC.outputJar().exists())
        }
    }
}
