/*
 * Copyright 2017-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

@file:Suppress("FunctionName")

package kotlinx.serialization.json

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*

/**
 * Generic exception indicating a problem with JSON serialization and deserialization.
 */
internal open class JsonException(message: String) : SerializationException(message)

/**
 * Thrown when [Json] has failed to parse the given JSON string or deserialize it to a target class.
 */
internal class JsonDecodingException(offset: Int, message: String) :
    JsonException("Unexpected JSON token at offset $offset: $message")

/**
 * Thrown when [Json] has failed to create a JSON string from the given value.
 */
internal class JsonEncodingException(message: String) : JsonException(message)

internal fun JsonDecodingException(offset: Int, message: String, input: String) =
    JsonDecodingException(offset, "$message.\n JSON input: ${input.minify(offset)}")

internal fun InvalidFloatingPoint(value: Number, output: String) = JsonEncodingException(
    "Unexpected special floating-point value $value. By default, " +
            "non-finite floating point values are prohibited because they do not conform JSON specification. " +
            "It is possible to serialize them using 'JsonBuilder.allowSpecialFloatingPointValues = true'\n" +
            "Current output: ${output.minify()}"
)

internal fun InvalidFloatingPointDecoded(value: Number, key: String, output: String) =
    JsonDecodingException(-1, invalidFp(value, key, output))

internal fun InvalidFloatingPoint(value: Number, key: String, output: String) =
    JsonEncodingException(invalidFp(value, key, output))

private fun invalidFp(value: Number, key: String, output: String): String {
    return "Unexpected special floating-point value $value with key $key. By default, " +
            "non-finite floating point values are prohibited because they do not conform JSON specification. " +
            "It is possible to serialize them using 'JsonBuilder.allowSpecialFloatingPointValues = true'\n" +
            "Current output: ${output.minify()}"
}

internal fun UnknownKeyException(key: String, input: String) = JsonDecodingException(
    -1,
    "JSON encountered unknown key: '$key'. You can enable 'JsonBuilder.ignoreUnknownKeys' property to ignore unknown keys.\n" +
            " Current input: ${input.minify()}"
)

internal fun InvalidKeyKindException(keyDescriptor: SerialDescriptor) = JsonEncodingException(
    "Value of type '${keyDescriptor.serialName}' can't be used in JSON as a key in the map. " +
            "It should have either primitive or enum kind, but its kind is '${keyDescriptor.kind}.'\n" +
            "You can convert such maps to arrays [key1, value1, key2, value2,...] using 'JsonBuilder.allowStructuredMapKeys' property"
)

private fun String.minify(offset: Int = -1): String {
    if (length < 200) return this
    if (offset == -1) {
        val start = this.length - 60
        if (start <= 0) return this
        return "....." + substring(start)
    }

    val start = offset - 30
    val end = offset + 30
    val prefix = if (start <= 0) "" else "....."
    val suffix = if (end >= length) "" else "....."
    return prefix + substring(start.coerceAtLeast(0), end.coerceAtMost(length)) + suffix
}
