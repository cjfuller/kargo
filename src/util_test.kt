package kargo

import kotlin.io.path.Path
import kotlin.io.path.div
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UtilTest {
    @Test
    fun testIsTestFileFlat() {
        val baseConfig = Config.load()
        Config.withGlobal(baseConfig.copy(projectLayout = ProjectLayout.FLAT)) {
            assertTrue((Config.global.srcDir / "some" / "path" / "to" / "my_test.kt").isTestFile())
            assertFalse((Config.global.srcDir / "test" / "kotlin" / "TestSomething.kt").isTestFile())
        }
    }

    @Test
    fun testIsTestFileClassic() {
        val baseConfig = Config.load()
        Config.withGlobal(baseConfig.copy(projectLayout = ProjectLayout.CLASSIC)) {
            assertFalse((Config.global.srcDir / "some" / "path" / "to" / "my_test.kt").isTestFile())
            assertTrue((Config.global.srcDir / "test" / "kotlin" / "TestSomething.kt").isTestFile())
        }
    }
}