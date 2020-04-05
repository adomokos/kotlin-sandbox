package sandbox.samples

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class TypeClassesSpec : StringSpec() {
    data class Apple(val weight: Int)

    interface AdderTC<AddableT> {
        fun add(a: AddableT, b: AddableT): AddableT

        // AddableT type should be a member of AdderTC type class by implementing all its methods.
        fun addAll(addables: List<AddableT>) =
            addables.reduce { acc, i -> add(acc, i) }
    }

    val intCombine = object : AdderTC<Int> {
        override fun add(a: Int, b: Int) = a + b
    }

    val stringCombine = object : AdderTC<String> {
        override fun add(a: String, b: String) = a + b
    }

    init {
        "can compare apples" {
            val appleComparator = Comparator<Apple> { apple1, apple2 ->
                apple1?.let { first ->
                    apple2?.let { second ->
                        first.weight.compareTo(second.weight)
                    } ?: 1
                } ?: 0
            }

            val result = listOf(Apple(3), Apple(1), Apple(2)).sortedWith(appleComparator)
            result shouldBe listOf(Apple(1), Apple(2), Apple(3))
        }

        "can use type class to combine entities" {
            val intResult = intCombine.addAll(listOf(1, 2, 3))
            intResult shouldBe 6

            val stringResult = stringCombine.addAll(listOf("ab", "cd", "ef"))
            stringResult shouldBe "abcdef"
        }
    }
}
