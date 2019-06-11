package example.kotlin.compiler.plugins.wither

import de.hanno.kotlin.compiler.plugins.annotations.Wither

@Wither
data class TestClass(val a: String, val b: Int = 4)

fun main(args: Array<String>) {
    val testInstance = TestClass("asd")
    testInstance.javaClass.methods.forEach {
        val params = it.parameters.map { it.type.simpleName }.fold("") { acc, s -> acc + s }
        val accessible = if (it.isAccessible) "accessible" else ""
        println("$accessible ${it.name}($params): ${it.returnType.simpleName}")
        if(it.name == "withA") {
            it.isAccessible = true
            println("call to withA results in: " + it.invoke(testInstance, "foobar"))
        }
    }
//  This would be possible if the IDE would use the compiler plugin
//    println(TestClass("asdasd").withA())
}
