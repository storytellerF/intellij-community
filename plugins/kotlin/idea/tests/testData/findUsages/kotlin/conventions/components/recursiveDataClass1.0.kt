// PSI_ELEMENT: org.jetbrains.kotlin.psi.KtParameter
// OPTIONS: usages
// PSI_ELEMENT_AS_TITLE: "a: A?"
data class A(val <caret>a: A?, val n: Int)

fun f(a: A) {
    val (a1, n1) = a
    val (a2, n2) =
        a?.a ?: return
    val (a3, n3) = a1 ?: return
}



// IGNORE_K2_LOG
