package arrow

import arrow.optics.Lens
import io.kotlintest.shouldBe
import io.kotlintest.specs.DescribeSpec

typealias GB = Int

data class Memory(val size: GB)
data class MotherBoard(val board: String, val memory: Memory)
data class Laptop(val price: Double, val motherBoard: MotherBoard)

fun createUpgradedLaptop(laptopx8: Laptop): Laptop =
    laptopx8.copy(
        price = 780.0,
        motherBoard = laptopx8.motherBoard.copy(
            memory = laptopx8.motherBoard.memory.copy(
                size = laptopx8.motherBoard.memory.size * 2
            )
        )
    )

val laptopPrice: Lens<Laptop, Double> = Lens(
    get = { laptop -> laptop.price },
    set = { laptop, value -> laptop.copy(price = value) }
)

val laptopMotherBoard: Lens<Laptop, MotherBoard> = Lens(
    get = { laptop -> laptop.motherBoard },
    set = { laptop, value -> laptop.copy(motherBoard = value) }
)

val motherBoardMemory: Lens<MotherBoard, Memory> = Lens(
    get = { mb -> mb.memory },
    set = { mb, value -> mb.copy(memory = value) }
)

val memorySize: Lens<Memory, GB> = Lens(
    get = { memory -> memory.size },
    set = { memory, value -> memory.copy(size = value) }
)

// The power of Lens is in how it can be combined
val laptopMemorySize: Lens<Laptop, GB> =
    laptopMotherBoard compose motherBoardMemory compose memorySize

class OpticsSpec : DescribeSpec({
    describe("Optics") {
        it("can get to nested objects the hard way") {
            val laptopx8 = Laptop(500.0, MotherBoard("X", Memory(8)))

            val result = createUpgradedLaptop(laptopx8)
            val expectedLaptop =
                Laptop(780.0, MotherBoard("X", Memory(16)))
            result shouldBe expectedLaptop
        }

        it("can access fields in deeply-nested structures") {
            val laptopX8 = Laptop(500.0, MotherBoard("X", Memory(8)))
            val laptopX16 = laptopMemorySize.modify(laptopPrice.set(laptopX8, 780.0)) {
                    size -> size * 2
            }
            val expectedLaptop =
                Laptop(780.0, MotherBoard("X", Memory(16)))

            laptopX16 shouldBe expectedLaptop
        }
    }
})
