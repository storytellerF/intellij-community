fun foo(a: Int) {
    A@
    while (true) {
        B@
        while (true) {
            if (a > 0) break@A
            if (a < 0) <selection>break@B</selection>
            (break)
            break
        }

        B@
        while (true) {
            if (a > 0) break@A
            if (a < 0) break@B
        }
    }
}