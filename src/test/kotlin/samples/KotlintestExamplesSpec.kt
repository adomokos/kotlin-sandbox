package samples

import io.kotlintest.properties.Gen
import io.kotlintest.properties.assertAll
import io.kotlintest.properties.forAll
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

// Custom generator
/* Must implement:
interface Gen<T> {
    fun constants(): Iterable<T>
    fun random(): Sequence<T>
}
*/

data class PBPerson(val name: String, val age: Int)
class PBPersonGenerator : Gen<PBPerson> {
    override fun constants() = emptyList<PBPerson>()
    override fun random() = generateSequence {
        val intList = Gen.choose(1, 1000)
        PBPerson(Gen.string().random().first(), intList.random().first())
    }
}

class KotlintestExamplesSpec : StringSpec() {
    init {
        "can do property-based testing - with 100 examples" {
            assertAll { a: String, b: String ->
                (a + b).length shouldBe a.length + b.length
            }
        }

        // This does not work :-(
        /*
        "can run a defined number of tests" {
           forAll(100) { a: String, b: String ->
                (a + b).length shouldBe a.length + b.length
            }
        }
        */

        "string size" {
            forAll(Gen.string(), Gen.string()) { a: String, b: String ->
                (a + b).length == a.length + b.length
            }
        }

        "person generator" {
            forAll(PBPersonGenerator()) { person1: PBPerson ->
                person1.age > 0
            }
        }
    }
}
