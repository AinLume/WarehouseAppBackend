package utils

inline fun <reified T : Enum<T>> enumFromStringOrNull(value: String?): T? {
    if (value == null) return null
    return try {
        enumValueOf<T>(value.uppercase())
    } catch (e: IllegalArgumentException) {
        null
    }
}

inline fun <reified T : Enum<T>> enumFromStringOrThrow(value: String?): T? {
    if (value == null) return null
    return try {
        enumValueOf<T>(value.uppercase())
    } catch (e: IllegalArgumentException) {
        throw IllegalArgumentException(
            "Invalid value '$value'. Allowed values: ${enumValues<T>().map { it.name }}"
        )
    }
}