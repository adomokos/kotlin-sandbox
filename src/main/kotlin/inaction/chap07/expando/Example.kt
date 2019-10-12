package inaction.chap07.expando

// Expando Objects = dynamically defined set of attributes

class Person {
    private val _attributes = hashMapOf<String, String>()

    fun setAttribute(attrName: String, value: String) {
        _attributes[attrName] = value
    }

    val name: String
        get() = _attributes["name"]!!
}

// You can use `by` to pull it from the delegated property
class Person2 {
    private val _attributes = hashMapOf<String, String>()

    fun setAttribute(attrName: String, value: String) {
        _attributes[attrName] = value
    }

    val name: String by _attributes
}

fun runExpandoExample() {
    val p = Person2()
    val data = mapOf("name" to "John", "company" to "Apple")
    for ((attrName, value) in data)
        p.setAttribute(attrName, value)
    println(p.name)
}
