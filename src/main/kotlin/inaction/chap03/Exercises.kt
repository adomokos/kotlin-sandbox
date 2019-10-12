package inaction.chap03

import strings.joinToString as joinTS
import strings.lastChar as last

fun <T> joinToString(
    collection: Collection<T>,
    separator: String,
    prefix: String,
    postfix: String
): String {
    val result = StringBuilder(prefix)

    for ((index, element) in collection.withIndex()) {
        if (index > 0) result.append(separator)
        result.append(element)
    }

    result.append(postfix)
    return result.toString()
}

fun <T> joinToString2(
    collection: Collection<T>,
    separator: String = ", ",
    prefix: String = "",
    postfix: String = ""
): String {
    return joinToString(collection,
                        separator,
                        prefix,
                        postfix)
}

open class View {
    open fun click() = println("View clicked")
}

class Button : View() {
    override fun click() = println("Button clicked")
}

// Extension properties
val String.lastChar: Char
    get() = get(length - 1)

var StringBuilder.lastChar: Char
    get() = get(length - 1)
    set(value: Char) {
        this.setCharAt(length - 1, value)
    }

// Kotlin functions with extension functions
// on collections
fun collectionExample() {
    var strings: List<String> =
        listOf("first", "second", "fourteenth")
    println(strings.last())

    val numbers: Collection<Int> = setOf(1, 14, 2)
    println(numbers.max())
}

// Var args
fun spreadOperator(args: Array<String>) {
    val list = listOf("args: ", *args)
    println(list)
}

// infix operators
fun infixExample() {
    val map = mapOf(1 to "one", 7 to "seven", 53 to "fifty-three")
    // same as
    val map2 = mapOf(1.to("one"), 7.to("seven"))
    println(map)
}

// Regex
fun parsePath(path: String) {
    val directory = path.substringBeforeLast("/")
    val fullName = path.substringAfterLast("/")

    val fileName = fullName.substringBeforeLast(".")
    val extension = fullName.substringAfterLast(".")

    println("Dir: $directory, name: $fileName, ext: $extension")
}

// local functions
class User(val id: Int, val name: String, val address: String)

fun saveUser(user: User) {
    if (user.name.isEmpty()) {
        throw IllegalArgumentException(
        "Can't save user ${user.id}: empty Name"
        )
    }
    if (user.address.isEmpty()) {
        throw IllegalArgumentException(
        "Can't save user ${user.id}: empty Address"
        )
    }

    // Save user to the DB
}

fun saveUser2(user: User) {
    fun validate(
        user: User,
        value: String,
        fieldName: String
    ) {
                    if (value.isEmpty()) {
                    throw IllegalArgumentException(
                        "Can't save user ${user.id}: empty $fieldName"
                    )
                    }
                }

    validate(user, user.name, "Name")
    validate(user, user.address, "Address")
}

// Reuse enclosing function's field
fun saveUser3(user: User) {
    fun validate(value: String, fieldName: String) {
        if (value.isEmpty()) {
        throw IllegalArgumentException(
            "Can't save user ${user.id}: empty $fieldName"
        )
        }
    }

    validate(user.name, "Name")
    validate(user.address, "Address")
}

// Use validate as an extension function
fun User.validateBeforeSave() {
    fun validate(value: String, fieldName: String) {
        if (value.isEmpty()) {
        throw IllegalArgumentException(
            "Can't save user $id: empty $fieldName"
        )
        }
    }

    validate(name, "Name")
    validate(address, "Address")
}

fun runChap03() {
    val list = listOf(1, 2, 3)
    println(joinToString(list, "; ", "(", ")"))
    println(joinToString(list, "; ", "", ""))
    println(joinToString2(list))
    println(joinToString2(list, postfix = ";", prefix = "# "))
    println(list.joinTS(postfix = ";", prefix = "# "))

    println("Kotlin".last())

    val view: View = Button()
    view.click()

    println("Kotlin".lastChar)

    val ab = StringBuilder("Kotlin?")
    ab.lastChar = '!'
    println(ab)

    collectionExample()

    spreadOperator(arrayOf("one", "two"))

    infixExample()

    // Destructuring pairs
    val (number, name) = 1 to "one"
    println("The number is $number and name is $name")

    parsePath("/Users/adomokos/kotlin-book/chapter.adoc")

    // saveUser(User(1, "", ""))
    saveUser2(User(1, "John", "123 Main Street"))
}
