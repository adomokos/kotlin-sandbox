package inaction.chap06

import java.io.BufferedReader
import java.io.StringReader
// import java.io.ByteArrayInputStream

fun strLen(s: String?): Int =
  if (s != null) s.length else 0

fun printAllCaps(s: String?) {
  val allCaps: String? = s?.toUpperCase()
  println(allCaps)
}

// Nullable property, safe-call operator to access that

class Employee(val name: String, val manager: Employee?)

fun managerName(employee: Employee): String? =
  employee.manager?.name

fun invokeManagerName() {
  val ceo = Employee("Da Boss", null)
  val developer = Employee("Bob Smith", ceo)
  println(managerName(developer))
  println(managerName(ceo))
}

// Chaining safe-call operators
class Address(val streetAddress: String, val zipCode: Int,
              val city: String, val country: String)

class Company(val name: String, val address: Address?)
class Person(val name: String, val company: Company?)

fun Person.countryName(): String {
  val country = this.company?.address?.country
  return if (country != null) country else "Unknown"
}

// The null-coalescing operator - the Elvis operator...
fun foo(s: String?) {
  val t: String = s ?: "<empty string>" // if it's null - then empty string
  println("Value of `t` is $t")
}

// Using the Elvis-operator for Person.countryName
fun Person.countryName2() =
  company?.address?.country ?: "Unknown"

// Can be used to throw exception
fun printShippingLabel(person: Person) {
  val address = person.company?.address
    ?: throw IllegalArgumentException("No address")
  with(address) {
    println(streetAddress)
    println("$zipCode $city, $country")
  }
}

// The let operator - calling non-nullable types
// with nullable values
fun sendEmailTo(email: String) {
  println("Sending email to $email")
}

fun invokeSendEmailTo() {
  var email: String? = "yole@example.com"
  email?.let{ sendEmailTo(it) }
  email = null
  // This won't be invoked, as email is null
  email?.let { sendEmailTo(it) }
}

// Late-initialized properties
// class MyService {
  // fun performAction(): String = "foo"
// }

// class MyTest {
  // private lateinit var myService: MyService

  // @Before fun setUp() {
    // myService = MyService()
  // }

  // @Test fun testAction() {
    // Assert.assertEquals("foo",
      // myService.performAction())
  // }
// }

// Extension for Nullable types

fun verifyUserInput(input: String?) {
  if (input.myIsNullOrBlank()) {
    println("Please fill in the required fields")
  }
}

fun String?.myIsNullOrBlank(): Boolean =
  this == null || this.isBlank()

// Nullability of type parameters
// All type params of functions and classes in Kotlin are nullable.
fun <T> printHashCode(t: T) {
  println(t?.hashCode())
}

fun <T: Any> printHashCodeNotNull(t: T) {
  println(t.hashCode()) 
}

// Primitive and other basic types
fun showProgress(progress: Int) {
  val percent = progress.coerceIn(0, 100)
  println("We're ${percent}% done!")
}

/* Full list of types that correspond to Java primitive types:
* Integer types - Byte, Short, Int, Long
* Floating-point number types - Float, Double
* Character type - Char
* Boolean type - Boolean
*/

data class ThePerson(val name: String,
                     val age: Int? = null) {
  fun isOlderThan(other: ThePerson): Boolean? {
    if (age == null || other.age == null)
      return null

    return age > other.age
  }
}

fun compareAge() {
  val sam = ThePerson("Sam", 35)
  val amy = ThePerson("Amy", 42)
  val jane = ThePerson("Jane")

  println("Is Sam older than Amy? - ${sam.isOlderThan(amy)}")
  println("Is Sam older than Jane? - ${sam.isOlderThan(jane)}")
}

// Converting primitive types

fun convertNumbers() {
  val i = 1
  // val l: Long = i <- this won't work
  val l: Long = i.toLong()

  val x = 1
  val list = listOf(1L, 2L, 3L)

  println(x.toLong() in list)
}

/* Literal suffixes
* for Long `L` like 1L, 234L
* for Double 0.12, 2.0, 1.2e-10
* for Float `f` of `F` like 123.4f or .456F
* for Hexadecimal `0x` or `0X` prefix
* for Binary `0b` or `0B`
*/

// Unit is similar to Java's void
interface Processor<T> {
  fun process(): T
}

class NoResultProcessor: Processor<Unit> {
  override fun process() {
    // do stuff
  }
}

// Collections and Arrays

fun readNumbers(reader: BufferedReader): List<Int?> {
  val result = ArrayList<Int?>()
  for (line in reader.lineSequence()) {
    try {
      val number = line.toInt()
      result.add(number)
    }
    catch (e: NumberFormatException) {
      result.add(null)
    }
  }

  return result
}

fun runReadNumbers() {
  val input = "1\n2\n3\nA\n"

  val inputReader = BufferedReader(StringReader(input));
  val result = readNumbers(inputReader)
  println(result)
}

fun addValidNumbers(numbers: List<Int?>) {
  var sumOfValidNumbers = 0
  var invalidNumbers = 0
  for (number in numbers) {
    if (number != null) {
      sumOfValidNumbers += number
    } else {
      invalidNumbers++
    }
  }
  println("Sum of valid numbers: $sumOfValidNumbers")
  println("Invalid numbers: $invalidNumbers")
}

// filtering `null` is so common, Kotlin added a function
fun addValidNumbers2(numbers: List<Int?>) {
  val validNumbers = numbers.filterNotNull()
  println("Sum of valid numbers: ${validNumbers.sum()}")
  println("Invalid numbers: ${numbers.size - validNumbers.size}")
}

fun runAddValidNumbers() {
  val input = "1\nabc\n42"
  val reader = BufferedReader(StringReader(input))
  val numbers = readNumbers(reader)
  addValidNumbers(numbers)
  addValidNumbers2(numbers)
}

// Arrays - prefer to use collections

fun printLetters() {
  val letters = Array<String>(26) { i -> ('a' + i).toString() }
  println(letters.joinToString(""))
}

fun printIndexed(args: Array<String>) {
  args.forEachIndexed { index, element ->
    println("Argument $index is: $element")
  }
}

fun runChap06() {
  println("Length of 'hello' is ${strLen("hello")}")
  val name: String? = null
  println("Length of null is ${strLen(name)}")

  printAllCaps("abc")
  printAllCaps(null)

  invokeManagerName()

  var person = Person("John", null)
  println(person.countryName())

  foo("Hey")
  foo(null)

  println(person.countryName2())

  val address = Address("Elsestr. 58", 34992, "Frankfurt", "Germany")
  val niceCoffeeShop = Company("Nice coffee shop", address)
  val person2 = Person("John", niceCoffeeShop)

  // printShippingLabel(person)
  printShippingLabel(person2)

  invokeSendEmailTo()

  verifyUserInput(" ")
  verifyUserInput(null)

  // Nullability of type params
  printHashCodeNotNull(42)

  showProgress(146)
  compareAge()
  convertNumbers()

  runReadNumbers()
  runAddValidNumbers()
  printLetters()
  printIndexed(arrayOf("one", "two", "three"))
}
