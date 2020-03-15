package sandbox.samples

import io.kotest.core.spec.style.StringSpec
import io.kotest.inspectors.forAll
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldHaveLength
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.arbitrary.Arb
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.default
import io.kotest.property.arbitrary.positiveInts
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

class PropertyBasedTestingSpec : StringSpec() {
    data class Person(val name: String, val age: Int)

    init {
        "can do property-based testing - with 100 examples" {
            checkAll<String, String> { a, b ->
                (a + b) shouldHaveLength(a.length + b.length)
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

        "generate the defaults for list" {
            val gen = Arb.default<List<Int>>()
            checkAll(10, gen) { list ->
                list.forAll { i ->
                    i.shouldBeInstanceOf<Int>()
                }
            }
        }

        "generate the defaults for set" {
            val gen = Arb.default<Set<String>>()
            checkAll(gen) { inst ->
                inst.forAll { i ->
                    i.shouldBeInstanceOf<String>()
                }
            }
        }

        "string size" {
            checkAll<String, String> { a, b ->
                (a + b) shouldHaveLength(a.length + b.length)
            }
        }

        "person generator" {
            val gen = Arb.bind(Arb.string(), Arb.positiveInts(), ::Person)
            checkAll(gen) {
                it.name shouldNotBe null
                it.age shouldBeGreaterThan(0)
            }
        }
    }
}
