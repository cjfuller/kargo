package kargo.tools

import kargo.commands.Build
import kargo.testutil.withBasicProject
import org.junit.jupiter.api.Test
import kotlin.io.path.exists
import kotlin.test.assertTrue

class KotlinCTest {
    @Test
    fun testEmptyDependenciesBuildsOk() {
        withBasicProject {
            initialize()
            Build.run()
            assertTrue(KotlinC.outputJar().exists())
        }
    }
}
