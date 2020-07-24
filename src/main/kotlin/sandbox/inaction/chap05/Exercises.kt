package sandbox.inaction.chap05

data class Person(val name: String, val age: Int)
class Book(val title: String, val authors: List<String>)

fun findOldestJavaStyle(people: List<Person>) {
    var maxAge = 0
    var theOldest: Person? = null

    for (person in people) {
        if (person.age > maxAge) {
            maxAge = person.age
            theOldest = person
        }
    }

    println(theOldest)
}

fun runLambdaFunction(x: Int, y: Int) {
    val sum = { x1: Int, y1: Int ->
        println("Computing the sum of $x1 and $y1")
        x1 + y1
    }
    println("Sum of $x and $y is ${sum(x, y)}")
}

fun joinNames(people: List<Person>): String {
    // A more expressive function call
    // return people.joinToString(separator = " ", transform = { p: Person -> p.name })

    // Call lambda outside of parameters
    return people.joinToString(" ") { p: Person -> p.name }
}

fun printMessagesWithPrefix(messages: Collection<String>, prefix: String) {
    messages.forEach {
        println("$prefix $it")
    }
}

fun printProblemCounts(responses: Collection<String>) {
    var clientErrors = 0
    var serverErrors = 0
    responses.forEach {
        if (it.startsWith("4")) {
            clientErrors++
        } else if (it.startsWith("5")) {
            serverErrors++
        }
    }

    println("$clientErrors client errors, $serverErrors server errors")
}

// val getAge = Person::age // It's called as `member reference`
// val getAge = { person: Person -> person.age } // Same as above, but more verbose

fun salute() = println("salute!")

fun toCreatePerson(): Person {
    // An action of creating an instance of "Person" is saved as value
    val createPerson = ::Person
    return createPerson("Alice", 29)
}

fun filterEven(list: Collection<Int>): Collection<Int> {
    return list.filter { it % 2 == 0 }
}

fun filterOlderThan30(people: Collection<Person>): Collection<Person> {
    return people.filter { it.age > 30 }
}

fun nameOfOlderThan30(people: Collection<Person>): Collection<String> {
    return people.filter { it.age > 30 }.map(Person::name)
}

fun makeValuesUppercased(values: Map<Int, String>): Map<Int, String> {
    return values.filterKeys { it > 0 }.mapValues { it.value.toUpperCase() }
}

val canBeInClub27 = { p: Person -> p.age <= 27 }

fun findAuthorsInBooks() {
    var books = listOf(
        Book("Thursday Next", listOf("Jasper Fforde")),
        Book("Mort", listOf("Terry Pratchett")),
        Book("Good Omens", listOf("Terry Pratchett", "Neil Gaiman"))
    )
    println(books.flatMap { it.authors }.toSet())
}

// Lazy collection operations: sequences
fun findNamesStartingWithALazy(people: Collection<Person>): Collection<String> {
    return people.asSequence()
        .map(Person::name)
        .filter { it.startsWith("A") }
        .toList()
}

fun generate100Lazily(): Sequence<Int> {
    val naturalNumbers = generateSequence(0) { it + 1 }
    val numberTo100 = naturalNumbers.takeWhile { it <= 100 }
    return numberTo100
}

// Lambdas with receivers

fun alphabet(): String {
    val stringBuilder = StringBuilder()
    return with(stringBuilder) {
        for (letter in 'A'..'Z') {
            this.append(letter)
        }

        append("\nNow I know the alphabet!")
        this.toString()
    }
}

fun alphabet2() = with(StringBuilder()) {
    for (letter in 'A'..'Z') {
        append(letter)
    }
    append("\nNow I know the alphabet")
    toString()
}

fun alphabet3() = StringBuilder().apply {
    for (letter in 'A'..'Z') {
        append(letter)
    }
    append("\nNow I know the alphabet")
}.toString()

fun alphabet4() = buildString {
    for (letter in 'A'..'Z') {
        append(letter)
    }
    append("\nNow I know the alphabet!")
}

fun runChap05() {
    val people = listOf(
        Person("Alice", 26),
        Person("Bob", 31),
        Person("Carol", 31)
    )
    findOldestJavaStyle(people)

    // And the Kotlin style
    println(people.maxBy { it.age })
    println(people.maxBy(Person::age))
    // No syntax shortcuts
    println(people.maxBy({ p: Person -> p.age }))
    // Lambda can be the last argument and moved
    // out of the argument list
    println(people.maxBy() { p: Person -> p.age })
    // Parens can be removed
    println(people.maxBy { p: Person -> p.age })

    // If lambda is stored in variable, type has to be defined
    val getAge = { p: Person -> p.age }
    println(people.maxBy(getAge))

    runLambdaFunction(2, 3)

    // Call lambda expression directly
    run { println(42) }

    println(joinNames(people))

    val errors = listOf("403 Forbidden", "404 Not Found")
    printMessagesWithPrefix(errors, "Error: ")

    val responses = listOf(
        "200 OK", "418 I'm a teapot",
        "500 Internal Server Error"
    )

    printProblemCounts(responses)

    run(::salute) // reference to the top level function

    println(toCreatePerson())

    println(filterEven(listOf(1, 2, 3, 4)))

    println(filterOlderThan30(people))
    println(nameOfOlderThan30(people))
    println(makeValuesUppercased(mapOf(0 to "zero", 1 to "one")))

    println(people.all(canBeInClub27))
    println(people.any(canBeInClub27))
    println(people.count(canBeInClub27))
    println(people.find(canBeInClub27))
    println(people.groupBy { it.age })
    println(listOf("a", "ab", "b").groupBy(String::first))

    val strings = listOf("abc", "def")
    println(strings.flatMap { it.toList() })

    findAuthorsInBooks()
    println(generate100Lazily()) // Unprocessed
    println(generate100Lazily().sum())

    println(alphabet())
    println(alphabet2())
    println(alphabet3())
    println(alphabet4())
}
