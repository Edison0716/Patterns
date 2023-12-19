package com.edisonli.patterns.anno

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class SingletonAnno(val type: String = TYPE_DOUBLE_CHECK) {
    companion object {
        const val TYPE_DOUBLE_CHECK = "double_check"
        const val TYPE_EAGER = "eager"
    }
}
