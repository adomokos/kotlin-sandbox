package inaction.chap09

/*
`Reified type parameters` allow you to refer at runtime to the
specific types used as type arguments in an inline function call.

`Declaration-site variance` lets you specify whether a generic type
with a type argument is a subtype of a supertype of another generic
type with the same base type.

Java does, Kotlin does not support raw types.
*/

fun sliceWithChar() {
	val letters = ('a'..'z').toList()
	println(letters.slice<Char>(0..2))
	println(letters.slice(10..13))
}

fun callGenericHigherOrderFunction() {
	val members = listOf("John", "Paul", "George")
	val otherMembers = mutableListOf<String>("Paul", "Linda", "Mick")

	// fun <T> List<T>.filter(predicate: (T) -> Boolean): List<T>
	println(members.filter { it !in otherMembers })
}

fun runChap09() {
	// val exampleList: List<String> = listOf("John", 2)
	sliceWithChar()
	callGenericHigherOrderFunction()
}
