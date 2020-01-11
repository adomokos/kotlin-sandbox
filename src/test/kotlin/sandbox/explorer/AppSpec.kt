package sandbox.explorer

import io.kotlintest.shouldNotBe
import io.kotlintest.specs.StringSpec

class AppSpec : StringSpec({
    "the app has a greeting" {
        val classUnderTest = App()
        classUnderTest.greeting shouldNotBe null
    }
})
