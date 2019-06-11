Kotlin compiler plugin that generates withX functions for data classes.
This can be useful for Java interop.


```text
$ ./gradlew :example-app:run
```
```text

> Task :example-app:compileKotlin
w: Advanced option value is passed in an obsolete form. Please use the '=' character to specify the value: -Xplugin=...
w: : *** generateClassSyntheticParts ***
w: : *** class TestClass is annotated with wither: true ***
w: : *** Generating witherFunction withA for property public final val a: kotlin.String defined in example.kotlin.compiler.plugins.wither.TestClass[PropertyDescriptorImpl@4052da2a] ***
w: : *** Generating witherFunction withB for property public final val b: kotlin.Int defined in example.kotlin.compiler.plugins.wither.TestClass[PropertyDescriptorImpl@19c1d5be] ***


> Task :example-app:run
 withA(String): TestClass
call to withA results in: TestClass(a=foobar, b=4)
 getA(): String
 getB(): int
 component1(): String
 component2(): int
 copy$default(TestClassStringintintObject): TestClass
 withB(int): TestClass
 equals(Object): boolean
 toString(): String
 hashCode(): int
 copy(Stringint): TestClass
 wait(longint): void
 wait(long): void
 wait(): void
 getClass(): Class
 notify(): void
 notifyAll(): void
```
