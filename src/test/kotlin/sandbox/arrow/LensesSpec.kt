package sandbox.arrow

import arrow.optics.Lens
import arrow.optics.optics
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

@optics data class MemoryL(val size: GB) { companion object }

@optics data class MotherBoardL(val brand: String, val memoryL: MemoryL) { companion object }

@optics data class LaptopL(val price: Double, val motherBoardL: MotherBoardL) { companion object }

class LensesSpec : DescribeSpec({
    describe("Generates Lenses code") {
        it("can work with different types") {
            val laptopX8 = LaptopL(500.0, MotherBoardL("X", MemoryL(8)))
            val memoryLens: Lens<LaptopL, GB> = LaptopL.motherBoardL.memoryL.size
            var updatedLaptop = memoryLens.modify(laptopX8) { x -> 2 * x }

            memoryLens.get(updatedLaptop) shouldBe 16
        }
    }
})
