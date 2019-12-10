package sandbox.inaction.chap07

import java.time.LocalDate
import sandbox.inaction.chap07.delegated.runPropertyChange
import sandbox.inaction.chap07.expando.runExpandoExample

data class Point(val x: Int, val y: Int) {
    operator fun plus(other: Point): Point {
        return Point(x + other.x, y + other.y)
    }
}

class Point2(val x: Int, val y: Int) {
    override fun equals(obj: Any?): Boolean {
        if (obj === this) return true
        if (obj !is Point2) return false
        return obj.x == x && obj.y == y
    }
}

// Compare two objects
class Person(
    val firstName: String,
    val lastName: String
) : Comparable<Person> {
    override fun compareTo(other: Person): Int {
        return compareValuesBy(this, other,
        Person::lastName, Person::firstName)
    }
}

data class Rectangle(val upperLeft: Point, val lowerRight: Point)

operator fun Rectangle.contains(p: Point): Boolean {
    return p.x in upperLeft.x until lowerRight.x &&
            p.y in upperLeft.y until lowerRight.y
}

// Defining the operator as an extension function
/*
operator fun Point.plus(other: Point): Point {
  ...
}
*/

// Defining an operator with different operand types
operator fun Point.times(scale: Double): Point {
    return Point((x * scale).toInt(), (y * scale).toInt())
}

// Defining an operator with a different return type
operator fun Char.times(count: Int): String {
    return toString().repeat(count)
}

// Overloading the unary minus operator
operator fun Point.unaryMinus(): Point {
    return Point(-x, -y)
}

// Access x or y coordinate by index
operator fun Point.get(index: Int): Int {
    return when (index) {
        0 -> x
        1 -> y
    else ->
        throw IndexOutOfBoundsException("Invalid coordinate $index")
    }
}

/*
Functions provided by Kotlin for bitwise operator:
* shl - Signed shift left
* shr - Signed shift right
* ushr - unsigned shift right
* and - Bitwise and
* or - Bitwise or
* xor - Bitwise xor
* inv - Bitwise inversion
*/

fun addPoints() {
    val p1 = Point(10, 20)
    val p2 = Point(30, 40)
    println(p1 + p2)
}

fun timesPoint() {
    val p = Point(10, 20)
    println(p * 1.5)
}

fun tryBitwise() {
    println(0x0F and 0xF0)
    println(0x0F or 0xF0)
    println(0x1 shl 4)
}

// Compound assignment operators `+=` and `-=`
fun compoundAssignmentOperator() {
    var point = Point(1, 2)
    point += Point(3, 4)
    println(point)
}

// Add to immutable collections
fun addToList() {
    val list = arrayListOf(1, 2)
    list += 3
    println(list)
    val newList = list + listOf(4, 5)
    println(newList)
}

fun overloadingUnaryMinusOperator() {
    val p = Point(10, 20)
    println(-p)
}

fun comparePoints() {
    val p1 = Point2(10, 20)
    val p2 = Point2(10, 20)
    val p3 = Point2(5, 5)
    println(p1 == p1)
    println(p1 == p2)
    println(p1 == p3)
    println(null == p3)
}

fun comparePeople() {
    val p1 = Person("Alice", "Smith")
    val p2 = Person("Bob", "Johnson")
    println(p1 < p2)
}

fun pointIndexer() {
    val p1 = Point(10, 20)
    println(p1[0])
    println(p1[1])
}

fun runContains() {
    val rect = Rectangle(Point(10, 20), Point(50, 50))
    println(Point(20, 30) in rect)
    println(Point(5, 5) in rect)
}

fun tenDayRange() {
    val now = LocalDate.now()
    val vacation = now..now.plusDays(10)
    println(now.plusWeeks(1) in vacation)
}

// Desctructuring
data class NameComponents(
    val name: String,
    val extension: String
)

fun splitFilename(fullName: String): NameComponents {
    var result = fullName.split('.', limit = 2)
    return NameComponents(result[0], result[1])
}

fun runDestructuring() {
    val (name, ext) = splitFilename("example.kt")
    println("The values are: $name and $ext")
}

fun printEntries(map: Map<String, String>) {
    for ((key, value) in map) {
        println("$key -> $value")
    }
}

fun runPrintEntries() {
    val map = mapOf("Oracle" to "Java", "JetBrains" to "Kotlin")
    printEntries(map)
}

// 7.5 is next

fun runChap07() {
    addPoints()
    timesPoint()
    println('a' * 3)
    tryBitwise()
    compoundAssignmentOperator()
    addToList()
    overloadingUnaryMinusOperator()
    comparePoints()
    comparePeople()
    pointIndexer()
    runContains()
    tenDayRange()
    runDestructuring()
    runPrintEntries()

    // from imported module
    runPropertyChange()
    runExpandoExample()
}
