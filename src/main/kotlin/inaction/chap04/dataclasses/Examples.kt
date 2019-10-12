package inaction.chap04.dataclasses

class Client(val name: String, val postalCode: Int) {
    override fun equals(other: Any?): Boolean {
        if (other == null || other !is Client)
            return false
        return name == other.name && postalCode == other.postalCode
    }

    override fun toString() = "Client(name=$name, postalCode=$postalCode)"

    // hashcode is needed, otherwise deeper equality check wont' work!
    override fun hashCode(): Int = name.hashCode() * 31 + postalCode
}

// The equals, toString and hashCode method overrides are generated
// for data classes
data class Client2(val name: String, val postalcode: Int)

// Delegating to another object
// CountingSet counts the number of added items

class CountingSet<T> (
    val innerSet: MutableCollection<T> = HashSet<T>()
) : MutableCollection<T> by innerSet { // using `by` added the delegated methods
    var objectsAdded = 0

    override fun add(element: T): Boolean { // overriding the delegated methods
        objectsAdded++
        return innerSet.add(element)
    }

    override fun addAll(c: Collection<T>): Boolean {
        objectsAdded += c.size
        return innerSet.addAll(c)
    }
}
