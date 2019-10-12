package inaction.chap08.returnfn

// Returning functions from functions
enum class Delivery { STANDARD, EXPEDITED }
class Order(val itemCount: Int)

fun getShippingCostCalculator(
    delivery: Delivery
): (Order) -> Double {
    if (delivery == Delivery.EXPEDITED) {
        return { order -> 6 + 2.1 * order.itemCount }
    }

    return { order -> 1.2 * order.itemCount }
}

fun runShippingCostCalculator() {
    val calculator = getShippingCostCalculator(Delivery.EXPEDITED)
    println("Shipping costs ${calculator(Order(3))}")
    val standardShippingCalculator =
        getShippingCostCalculator(Delivery.STANDARD)
    println("Standard shipping costs " +
        "${standardShippingCalculator(Order(3))}")
}

// Another GUI-related example

data class Person(
    val firstName: String,
    val lastName: String,
    val phoneNumber: String?
)

class ContactListFilters {
    var prefix: String = ""
    var onlyWithPhoneNumber: Boolean = false

    fun getPredicate(): (Person) -> Boolean {
        val startsWithPrefix = { p: Person ->
        p.firstName.startsWith(prefix) ||
            p.lastName.startsWith(prefix) }
        if (!onlyWithPhoneNumber) {
        return startsWithPrefix
        }
        return { startsWithPrefix(it) &&
                it.phoneNumber != null }
    }
}

fun runPersonFiltering() {
    val contacts = listOf(Person("John", "Lennon", "123-4567"),
                            Person("Paul", "McCartney", null))
    val contactListFilters = ContactListFilters()
    with(contactListFilters) {
        prefix = "Jo"
        onlyWithPhoneNumber = true
    }

    println(contacts.filter(contactListFilters.getPredicate()))
}

fun runExamples() {
    runShippingCostCalculator()
    runPersonFiltering()
}
