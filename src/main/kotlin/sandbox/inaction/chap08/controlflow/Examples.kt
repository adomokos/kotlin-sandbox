package sandbox.inaction.chap08.controlflow

data class Person(val name: String, val age: Int)

val people = listOf(Person("Alice", 28), Person("Bob", 31))

fun lookForAlice(people: List<Person>) {
    for (person in people) {
        if (person.name == "Alice") {
            println("Found!")
            return
        }
    }
    println("Alice is not found")
}

/*
  return from the outer function is possible
  only if the function takes the lambda as an
  argument is inlined.
*/
fun lookForAliceForEach(people: List<Person>) {
    people.forEach {
        if (it.name == "Alice") {
            println("Found!")
            return
        }
    }
    println("Alice is not found")
}

// To distinguish a local return from a non-local one,
// use labels
fun lookForAliceWithLabel(people: List<Person>) {
    people.forEach mylabel@{
        if (it.name == "Alice") return@mylabel
    }
    println("Alice might be somewhere") // This is always printed
}

// Return with function name
fun lookForAliceWithFnName(people: List<Person>) {
    people.forEach {
        if (it.name == "Alice") return@forEach
    }
    println("Alice might be somewhere")
}

fun labeledExpression() {
    println(StringBuilder().apply sb@{
        listOf(1, 2, 3).apply {
        this@sb.append(this.toString())
        }
    })
}

// Anonymus functions
fun lookForAliceAnonymousFn(people: List<Person>) {
    people.forEach(fun (person) {
        if (person.name == "Alice") return // return refers to the closest function
        println("${person.name} is not Alice")
    })
}

fun filterPplWithAnonymousFn(people: List<Person>) {
    // Block body for anonymous function needs a return type
    val youngPpl = people.filter(fun (person): Boolean {
        return person.age < 30
    })
    // Expression body does not need a return type
    // Something like this:
    /*
    val youngPpl =
        people.filter(fun (person) = person.age < 30)
    */

    println("Young ppl are: $youngPpl")
}

// return returns from the closest function

fun runExamples() {
    lookForAlice(people)
    lookForAliceForEach(people)
    lookForAliceWithLabel(people)
    lookForAliceWithFnName(people)
    labeledExpression()
    lookForAliceAnonymousFn(people)
    filterPplWithAnonymousFn(people)
}
