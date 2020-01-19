package sandbox.explorer

object Factories {
    fun addPerson(): Person {
        return Person.findOrCreate(
            emailValue = "john@example.com",
            firstNameValue = "John",
            lastNameValue = "Smith",
            ratingValue = 1,
            gitHubUsernameValue = "jdsmith"
        )
    }
}
