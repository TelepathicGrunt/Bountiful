package io.ejekta.kambrikx.testing

import io.ejekta.kambrikx.internal.serial.encoders.TagEncoder
import kotlinx.serialization.*
import kotlinx.serialization.builtins.ListSerializer


@InternalSerializationApi
@OptIn(ExperimentalSerializationApi::class)
fun main(args: Array<String>) {

    fun <T> encodeToTag(serializer: SerializationStrategy<T>, obj: T): Any {
        //lateinit var result: Tag
        //val encoder = TagEncoder { result = this }
        val encoder = TagEncoder()
        encoder.encodeSerializableValue(serializer, obj)
        //return result
        return encoder.root
    }

    @Serializable
    data class Wallet(val money: Double)

    @Serializable
    data class Person(val name: String, val age: Int, val items: Map<String, Map<Long, String>>)


    val b = encodeToTag(
        ListSerializer(Person.serializer()),
        listOf(
            Person("Bobby", 55, mapOf(
                "keys" to mapOf(
                    2L to "hai",
                    4L to "there"
                )
            ))
        )
    )

    println("Result: $b")


}


