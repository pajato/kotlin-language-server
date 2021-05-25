package symbols

const val message = "Hello Kotlin!"

fun main() {
    val name = "Hello World"
    println(name)
    println(message)
    var counter = 0
    val foo = if (counter % 2 == 0) "even" else "odd"
    /* when (counter++) {
        1 -> println("Is one")
        2 -> println("Is two")
        else -> println("Is $counter and foo is $foo")
    } */
}
