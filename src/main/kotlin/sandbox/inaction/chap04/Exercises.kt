package sandbox.inaction.chap04

import java.io.File
import sandbox.inaction.chap04.dataclasses.Client
import sandbox.inaction.chap04.dataclasses.Client2
import sandbox.inaction.chap04.dataclasses.CountingSet
import sandbox.inaction.chap04.objects.A
import sandbox.inaction.chap04.objects.CaseInsensitiveFileComparator
import sandbox.inaction.chap04.objects.FactoryUser
import sandbox.inaction.chap04.objects.JsonPerson
import sandbox.inaction.chap04.objects.Person

// Interfaces

interface Clickable {
    fun click()
    fun showOff() = println("I'm clickable") // default implementation
}

interface Focusable {
    fun setFocus(b: Boolean) =
        println("I ${if (b) "got" else "lost"} focus.")

    fun showOff() = println("I'm focusable")
}

class Button : Clickable, Focusable {
    override fun click() = println("I was clicked")
    override fun showOff() {
        super<Clickable>.showOff()
        super<Focusable>.showOff()
    }
}

// This class is open
open class RichButton : Clickable {
    // This function is final, can't be overridden
    fun disable() = Unit
    open fun animate() = Unit
    // This function overrides an open function and is open as well
    final override fun click() = Unit
}

// Classes are closed by default

// 4.1.3 Visibility modifiers: public by default
// If you omit a visibility modifier, methods are public by default

// A new one is added - internal - visible inside the module

// 4.1.4 Inner and nested classes - nested by default
// Sealed classes

sealed class Expr {
    class Num(val value: Int) : Expr()
    class Sum(val left: Expr, val right: Expr) : Expr()
}

fun eval(e: Expr): Int =
    when (e) {
        is Expr.Num -> e.value
        is Expr.Sum -> eval(e.right) + eval(e.left)
        // Since the class Expr is `sealed`,
        // there's no need to provide a default branch
    }

// 4.2 Declaring a class with nontrivial constructor and properties

// The code in the parens is the `primary constructor`
// By adding `val` keyword to the constructor, initializes the field
class User(val nickname: String)

// Unpacking it ☝️
class User2 constructor(_nickname: String) {
    val nickname: String

    // Initializer block, can have many of them
    init {
        nickname = _nickname
    }
}

// Another way to declare it
class User3(_nickname: String) {
    val nickname = _nickname
}

// Can declare default values for constructor params
class User4(
    val nickname: String,
    val isSubscribed: Boolean = true
)

// If you inherit a class and don't provide any constructor,
// you have to explicitly invoke the constructor, even if it
// does not have params
// For example: class RadioButton: Button()

// Make a private constructor
class Secretive private constructor()

// Secondary constructors
// Rare in Kotlin, needed to interact with Java classes

/*
class AttributeSet
class Context

open class View {
    constructor(ctx: Context) {
        // some code
    }
    constructor(ctx: Context, attr: AttributeSet) {
        // some more code
    }
}
*/

// 4.2.3 - Implementing properties declared in interfaces
interface IUser {
    val nickname: String
}

// Primary constructor property
class PrivateUser(override val nickname: String) : IUser

// Custom getter
class SubscribingUser(val email: String) : IUser {
    override val nickname: String
        get() = email.substringBefore('@')
}

// Property initializer
class FacebookUser(val accountId: Int) : IUser {
    fun getFacebookName(accountId: Int): String =
        // look it up in the DB
        "some-user for $accountId"

    override val nickname = getFacebookName(accountId)
}

// Calculated property in an interface
interface IUser2 {
    val email: String

    // Property doesn't have a backing field:
    // the result value jk
    val nickname: String
        get() = email.substringBefore('@')
}

class ChangingUser(val name: String) {
    var address: String = "unspecified"
        set(value: String) {
            println(
                """
        Address was changed for $name:
        "$field" -> "$value".
                """.trimIndent()
            )
            field = value
        }
}

// 4.2.5 - Changing accessor visibility
class LengthCounter {
    var counter: Int = 0
        private set

    fun addWord(word: String) {
        counter += word.length
    }
}

fun runChap04() {
    val button = Button()
    button.click()
    button.setFocus(true)
    button.showOff()
    button.setFocus(false)

    val alice = User4("Alice")
    println("Is Alice subscribed? ${alice.isSubscribed}")

    val bob = User4("Bob", false)
    println("Is Bob subscribed? ${bob.isSubscribed}")

    // Explicitly specify names for some constructor arguments
    val carol = User4("Carol", isSubscribed = false)
    println("Is Carol subscribed? ${carol.isSubscribed}")

    println(PrivateUser("test@kotlinlang.org").nickname)
    println(SubscribingUser("test@kotlinlang.org").nickname)
    println(FacebookUser(1234).nickname)

    val changingUser = ChangingUser("Alice")
    changingUser.address = "Elsenheimerstrasse 47, 80687 Muenchen"

    val lengthCounter = LengthCounter()
    lengthCounter.addWord("Hi!")
    println("The lengthCounter's counter property is: ${lengthCounter.counter}")

    // Trying data classes
    val client = Client("John Smith", 60062)
    println(client)

    val client2 = Client("John Smith", 60062)
    println("Are the clients equal? ${client == client2}")

    // Create a set of clients
    val processed = hashSetOf(Client("Alice", 342562))
    // The HashSet checks the hashcode first, if it returns true,
    // then it will call equals to make sure the values are the same
    println(processed.contains(Client("Alice", 342562)))

    val anotherClient = Client2("John Smith", 60062)
    println(anotherClient)

    val anotherClient2 = Client2("John Smith", 60062)
    println("Are the anotherClients equal? ${anotherClient == anotherClient2}")

    // Delegated class
    val cset = CountingSet<Int>()
    cset.addAll(listOf(1, 1, 2))
    println("${cset.objectsAdded} objects were added, ${cset.size} remain")

    println(
        CaseInsensitiveFileComparator.compare(
            File("/tmp"), File("/tmp")
        )
    )

    val files = listOf(File("/z"), File("/a"))
    println(files.sortedWith(CaseInsensitiveFileComparator))

    val people = listOf(Person("Bob"), Person("Alice"))
    println(people.sortedWith(Person.NameComparator))

    A.bar()

    val subscribingUser = FactoryUser.newSubscribingUser("bob@gmail.com")
    println(subscribingUser.nickname)

    val person = JsonPerson.Loader.fromJSON("{name: 'John'}")
    println(person)
}
