package kifio

import java.lang.NullPointerException

fun<T> checkNotNull(obj: T?): T {
    return obj ?: throw NullPointerException("Exactly null!")
}