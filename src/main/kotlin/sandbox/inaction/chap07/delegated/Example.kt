package sandbox.inaction.chap07.delegated

import java.beans.PropertyChangeListener
import java.beans.PropertyChangeSupport
import kotlin.properties.Delegates
import kotlin.reflect.KProperty

open class PropertyChangeAware {
    protected val changeSupport =
        PropertyChangeSupport(this)

        fun addPropertyChangeListener(listener: PropertyChangeListener) {
            changeSupport.addPropertyChangeListener(listener)
        }

        fun removePropertyChangeListener(listener: PropertyChangeListener) {
            changeSupport.removePropertyChangeListener(listener)
        }
}

class ChangingPerson(
    val name: String,
    age: Int,
    salary: Int
) : PropertyChangeAware() {
    var age: Int = age
        set(newValue) {
            val oldValue = field
            field = newValue
            changeSupport.firePropertyChange("age", oldValue, newValue)
        }

    var salary: Int = salary
        set(newValue) {
            val oldValue = field
            field = newValue
            changeSupport.firePropertyChange("salary", oldValue, newValue)
        }
}

// Refactored ChangingPerson using ObservableProperty

class ObservableProperty(
    val propName: String,
    var propValue: Int,
    val changeSupport: PropertyChangeSupport
) {
    fun getValue(): Int = propValue
    fun setValue(newValue: Int) {
        val oldValue = propValue
        propValue = newValue
        changeSupport.firePropertyChange(propName, oldValue, newValue)
    }
}

class ChangingPerson2(
    val name: String,
    age: Int,
    salary: Int
) : PropertyChangeAware() {
    val _age = ObservableProperty("age", age, changeSupport)
    var age: Int
        get() = _age.getValue()
        set(value) { _age.setValue(value) }

    val _salary = ObservableProperty("salary", salary, changeSupport)
    var salary: Int
        get() = _salary.getValue()
        set(value) { _salary.setValue(value) }
}

class ObservableProperty2(
    var propValue: Int,
    val changeSupport: PropertyChangeSupport
) {
    operator fun getValue(p: ChangingPerson3, prop: KProperty<*>): Int = propValue

    operator fun setValue(p: ChangingPerson3, prop: KProperty<*>, newValue: Int) {
        val oldValue = propValue
        propValue = newValue
        changeSupport.firePropertyChange(prop.name, oldValue, newValue)
    }
}

class ChangingPerson3(
    val name: String,
    age: Int,
    salary: Int
) : PropertyChangeAware() {
    var age: Int by ObservableProperty2(age, changeSupport)
    var salary: Int by ObservableProperty2(salary, changeSupport)
}

// Kotlin already has the ObservableProperty2
class ChangingPerson4(
    val name: String,
    age: Int,
    salary: Int
) : PropertyChangeAware() {
    private val observer = {
        prop: KProperty<*>, oldValue: Int, newValue: Int
        -> changeSupport.firePropertyChange(prop.name, oldValue, newValue)
    }

    var age: Int by Delegates.observable(age, observer)
    var salary: Int by Delegates.observable(salary, observer)
}

fun runPropertyChange() {
    val p = ChangingPerson4("John", 34, 2000)
    p.addPropertyChangeListener(
        PropertyChangeListener { event ->
        println("Property ${event.propertyName} changed " +
                "from ${event.oldValue} to ${event.newValue}")
        }
    )

    p.age = 35
    p.salary = 2100
}
