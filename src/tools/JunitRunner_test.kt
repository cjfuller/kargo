package kargo.tools

import kargo.Config
import kargo.testutil.withBasicProject
import kargo.testutil.withProjectConfig
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertTrue

class JUnitRunnerTest {
    @Test
    fun `includes common test dependencies`() {
        withBasicProject {
            initialize()
            assertTrue(Config.global.testLockFile.exists())
            assertContains(
                Config.global.testLockFile.readText(),
                "org.jetbrains.kotlin:kotlin-test-common"
            )
        }
    }

    @Test
    fun `merges additional test dependencies`() {
        val config = """
            [package]
            name = "test_dep_merge"
            kotlin_version = "1.6.10"
            package_layout = "flat"
            
            [dependencies]
            
            [test.dependencies]
            "io.kotest:kotest-property" = "5.0.3"
        """.trimIndent()
        withProjectConfig(config) {
            initialize()
            assertTrue(Config.global.testLockFile.exists())
            // Should still have the common deps.
            assertContains(
                Config.global.testLockFile.readText(),
                "org.jetbrains.kotlin:kotlin-test-common"
            )
            // ...and also contain the extra ones
            assertContains(
                Config.global.testLockFile.readText(),
                "io.kotest:kotest-property:5.0.3"
            )
        }
    }
}
