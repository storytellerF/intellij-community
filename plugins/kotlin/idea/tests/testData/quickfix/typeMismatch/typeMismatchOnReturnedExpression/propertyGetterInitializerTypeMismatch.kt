// "Change type of 'A.x' to '() -> Int'" "true"
class A {
    var x: Int
        get(): Int = if (true) { {42}<caret> } else { {24} }
        set(i: Int) {}
}

// FUS_QUICKFIX_NAME: org.jetbrains.kotlin.idea.quickfix.ChangeVariableTypeFix
// FUS_K2_QUICKFIX_NAME: org.jetbrains.kotlin.idea.k2.codeinsight.fixes.ChangeTypeQuickFixFactories$UpdateTypeQuickFix