package com.edisonli.patterns.base

/**
 * @author edison007.li
 */
abstract class BaseSingleton<T> {
    @Volatile
    private var instance: T? = null

    protected fun getInstance(creator: () -> T): T {
        return instance ?: synchronized(this) {
            instance ?: creator.invoke().also {
                instance = it
            }
        }
    }
}
