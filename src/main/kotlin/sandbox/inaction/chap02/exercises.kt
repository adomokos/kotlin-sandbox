package sandbox.inaction.chap02

import sandbox.inaction.chap02.learning.fizzBuzz
import sandbox.inaction.chap02.learning.recognize

fun max(a: Int, b: Int): Int {
    return if (a > b) a else b
}

fun max2(a: Int, b: Int): Int =
    if (a > b) a else b

fun sayHello(name: String?): String {
    val nameVar = if (name != null) name else "Kotlin"
    return "Hello, $nameVar!"
}

// Calculated property
class Rectangle(val height: Int, val width: Int) {
    val isSquare: Boolean
        get() {
            return height == width
        }
}

// Enums
enum class Color(
    val r: Int,
    val g: Int,
    val b: Int
) {
    RED(255, 0, 0),
    ORANGE(255, 165, 0),
    YELLOW(255, 255, 0),
    GREEN(0, 255, 0),
    BLUE(0, 0, 255),
    INDIGO(75, 0, 130),
    VIOLET(238, 130, 238);

    fun rgb() = (r * 256 * g) * 256 + b
}

fun getMnemonic(color: Color) =
    when (color) {
        Color.RED -> "Richard"
        Color.ORANGE -> "Of"
        Color.YELLOW -> "York"
        Color.GREEN -> "Gave"
        Color.BLUE -> "Battle"
        Color.INDIGO -> "In"
        Color.VIOLET -> "Vain"
    }

// Combine multiple enum values
fun getWarmth(color: Color) =
    when (color) {
        Color.RED, Color.ORANGE, Color.YELLOW -> "warm"
        Color.GREEN -> "neutral"
        Color.BLUE, Color.INDIGO, Color.VIOLET -> "cold"
    }

fun mix(c1: Color, c2: Color) =
    when (setOf(c1, c2)) {
        setOf(Color.RED, Color.YELLOW) -> Color.ORANGE
        setOf(Color.YELLOW, Color.BLUE) -> Color.GREEN
        setOf(Color.BLUE, Color.VIOLET) -> Color.INDIGO
        else -> throw Exception("Dirty color")
    }

// Smart Casting
interface Expr
class Num(val value: Int) : Expr
class Sum(val left: Expr, val right: Expr) : Expr

fun eval(e: Expr): Int =
    /*
    if (e is Num) {
        val n = e as Num
        return n.value
    }
    if (e is Sum) {
        return eval(e.right) + eval(e.left)
    }

    throw IllegalArgumentException("Unknown expression")
    */
    // Using when
    when (e) {
        is Num -> e.value
        is Sum -> eval(e.right) + eval(e.left)
        else -> throw IllegalArgumentException("Unknown expression")
    }

// Iterators and ranges
// fun fizzBuzz(i: Int) = when {
// i % 15 == 0 -> "FizzBuzz "
// i % 3 == 0 -> "Fizz "
// i % 5 == 0 -> "Buzz "
// else -> "$i "
// }

// fun recognize(c: Char) = when (c) {
// in '0'..'9' -> "It's a digit!"
// in 'a'..'z', in 'A'..'Z' -> "It's a letter!"
// else -> "I don't know..."
// }

fun runChap02() {
    println("The max of 2 and 5 is ${max(2,5)}")
    println("The max2 of 2 and 5 is ${max2(2,5)}")

    println(sayHello("Attila"))
    println(sayHello(null))

    val rectangle = Rectangle(15, 15)
    println("The rectangle is square? ${rectangle.isSquare}")

    println("Blue color: ${Color.BLUE.rgb()}")

    println("Mnemonic: ${getMnemonic(Color.BLUE)}")

    println("Color warmth for organge: ${getWarmth(Color.ORANGE)}")
    println("Color warmth for blue: ${getWarmth(Color.BLUE)}")

    println("Mix colors: ${mix(Color.BLUE, Color.YELLOW)}")

    // Evaluate expressions
    println(eval(Sum(Sum(Num(1), Num(2)), Num(4))))

    for (i in (1..100)) {
        print(fizzBuzz(i))
    }
    println('\n')

    println(recognize('9'))
    println(recognize('B'))
}
