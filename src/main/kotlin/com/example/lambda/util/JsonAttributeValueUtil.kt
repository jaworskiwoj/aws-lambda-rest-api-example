package com.example.lambda.util

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.*
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import java.text.NumberFormat
import java.util.function.Consumer


class JsonAttributeValueUtil {

    fun toAttributeValues(jsonNode: JsonNode): Map<String, AttributeValue> {
        val result = mutableMapOf<String, AttributeValue>()

        jsonNode.fieldNames().forEach {
            result[it] = toAttributeValue(jsonNode.get(it))
        }

        return result
    }

    private fun toAttributeValue(jsonNode: JsonNode): AttributeValue {
        return when {
            jsonNode.isObject -> toAttributeValue(jsonNode as ObjectNode)
            jsonNode.isArray -> toAttributeValue(jsonNode as ArrayNode)
            jsonNode.isValueNode -> toAttributeValue(jsonNode as ValueNode)
            else -> throw IllegalStateException("Unexpected node type: $jsonNode")
        }
    }

    private fun toAttributeValue(objectNode: ObjectNode): AttributeValue {
        val attributesMap: MutableMap<String, AttributeValue> = HashMap()
        val attributesIterable = Iterable { objectNode.fields() }
        for ((key, value) in attributesIterable) {
            attributesMap[key] = toAttributeValue(value)
        }
        return AttributeValue.builder().m(attributesMap).build()
    }

    private fun toAttributeValue(arrayNode: ArrayNode): AttributeValue {
        val builder = AttributeValue.builder()
        val childAttributes = mutableListOf<AttributeValue>()
        arrayNode.forEach(Consumer { jsonNode: JsonNode -> childAttributes.add(toAttributeValue(jsonNode)) })
        return builder.l(childAttributes).build()
    }

    private fun toAttributeValue(valueNode: ValueNode): AttributeValue {
        return when {
            valueNode.isNumber -> toAttributeValue(valueNode as NumericNode)
            valueNode.isBoolean -> AttributeValue.builder().bool(valueNode.asBoolean()).build()
            valueNode.isTextual -> AttributeValue.builder().s(valueNode.asText()).build()
            valueNode.isNull -> AttributeValue.builder().nul(true).build()
            else -> throw IllegalStateException("Unexpected value type: $valueNode")
        }
    }

    private fun toAttributeValue(numericNode: NumericNode): AttributeValue {
        return AttributeValue.builder().n(numericNode.asText()).build()
    }

    fun fromAttributeValue(map: Map<String?, AttributeValue>): JsonNode {
        val objectNode = JsonNodeFactory.instance.objectNode()
        map.entries.forEach(Consumer { entry: Map.Entry<String?, AttributeValue> -> objectNode.set<JsonNode>(entry.key, fromAttributeValue(entry.value)) })
        return objectNode
    }

    private fun fromAttributeValue(attributeValue: AttributeValue): JsonNode {
        return when {
            attributeValue.hasM() -> fromAttributeValue(attributeValue.m())
            attributeValue.hasL() -> fromAttributeValue(attributeValue.l())
            attributeValue.s() != null -> JsonNodeFactory.instance.textNode(attributeValue.s())
            attributeValue.bool() != null -> JsonNodeFactory.instance.booleanNode(attributeValue.bool())
            attributeValue.n() != null -> fromAttributeValue(NumberFormat.getInstance().parse(attributeValue.n()))
            attributeValue.nul() -> JsonNodeFactory.instance.nullNode()
            else -> throw IllegalStateException("Unexpected attribute value type: $attributeValue")
        }
    }

    private fun fromAttributeValue(list: List<AttributeValue>): JsonNode {
        val arrayNode = JsonNodeFactory.instance.arrayNode()
        list.forEach(Consumer { attributeValue: AttributeValue -> arrayNode.add(fromAttributeValue(attributeValue)) })
        return arrayNode
    }

    private fun fromAttributeValue(number: Number): JsonNode {
        return when (number) {
            is Double -> JsonNodeFactory.instance.numberNode(number)
            is Float -> JsonNodeFactory.instance.numberNode(number)
            is Long -> JsonNodeFactory.instance.numberNode(number)
            is Short -> JsonNodeFactory.instance.numberNode(number)
            is Int -> JsonNodeFactory.instance.numberNode(number)
            else -> throw IllegalStateException("Unknown Numeric Type: $number")
        }
    }
}