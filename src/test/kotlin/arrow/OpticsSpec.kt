package arrow

// import io.kotlintest.shouldBe
// import arrow.optics.Optional
import arrow.optics.optics
import io.kotlintest.specs.StringSpec

@optics data class Street(val number: Int, val name: String)
@optics data class Address(val city: String, val street: Street)
@optics data class Company(val name: String, val address: Address)
@optics data class Employee(val name: String, val company: Company?)

// val optional: Optional<Employee, String> = Employee.company.address.street.name

class OpticsSpec : StringSpec({
    "can modify a field".config(enabled = false) {
        /*
        val john = Employee("John Doe",
            Company("Kategory",
            Address("Functional city", Street(42, "lambda street"))))
         */
        // optional.modify(john, String::toUpperCase)
    }
})
