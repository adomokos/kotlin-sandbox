package inaction.chap04.objects

import java.io.File

/*
// Singleton objects are declared this way
object Payroll {
  val allEmployees = arrayListOf<Person>()

  fun calculatedSalary() {
    for (person in allEmployees) {

    }
  }
}
*/

// Comparators don't need multiple instance, singleton should work
object CaseInsensitiveFileComparator : Comparator<File> {
  override fun compare(file1: File, file2: File): Int {
    return file1.path.compareTo(file2.path, ignoreCase = true)
  }
}

// Implementing Comparator with a nested object
data class Person(val name: String) {
  object NameComparator: Comparator<Person> {
    override fun compare(p1: Person, p2: Person): Int =
      p1.name.compareTo(p2.name)
  }
}

// Companion object, feels like a static method
class A {
  companion object {
    fun bar() {
      println("Companion object called")
    }
  }
}
// They are ideal to implement Factory pattern

// A factory example
class FactoryUser private constructor(val nickname: String) {
  companion object {
    fun getFacebookName(accountId: Int): String {
      return "John-$accountId"
    }

    fun newSubscribingUser(email: String) =
      FactoryUser(email.substringBefore('@'))

    fun newFacebookUser(accountId: Int) =
      FactoryUser(getFacebookName(accountId))
  }
}

// Companion object used with the object
data class JsonPerson(val name: String) {
  companion object Loader {
    fun fromJSON(jsonText: String): JsonPerson = JsonPerson(name = jsonText)
  }
}

interface JSONFactory<T> {
  fun fromJSON(jsonText: String): T
}

class JsonFactoryPerson(val name: String) {
  companion object : JSONFactory<JsonFactoryPerson> {
    override fun fromJSON(jsonText: String): JsonFactoryPerson =
      JsonFactoryPerson(jsonText)
  }
}
