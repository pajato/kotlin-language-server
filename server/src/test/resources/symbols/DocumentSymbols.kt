private class DocumentSymbols() {
    val aProperty = 1

    fun aFunction(aFunctionArg: Int) {
        println("Hello Kotlin Language Server")
    }

    constructor(aConstructorArg: Int): this() {
    }
}
