package terminodiff.engine.concepts

import org.hl7.fhir.r4.model.CodeSystem
import terminodiff.engine.graph.FhirConceptDetails
import terminodiff.engine.graph.PropertyMap
import terminodiff.i18n.LocalizedStrings

typealias FhirConcept = CodeSystem.ConceptDefinitionComponent

typealias PropertyDiff = MutableList<KeyedListDiffResult<String>>

data class ConceptDiff(
    val conceptComparison: List<ConceptDiffResult>,
    val propertyComparison: PropertyDiff
) {
    fun toString(localizedStrings: LocalizedStrings): String {
        return "ConceptDiff(conceptComparison=[${conceptComparison.map { it.toString(localizedStrings) }}], " +
                "propertyComparison=[${propertyComparison.joinToString(",")}]"
    }

    companion object {

        val diffItems = listOf(
            ConceptDiffItem({ display }, { display }),
            ConceptDiffItem({ definition }, { definition })
        )

        fun compareConcept(
            leftConcept: FhirConceptDetails,
            rightConcept: FhirConceptDetails,
            leftProperties: PropertyMap,
            rightProperties: PropertyMap
        ): ConceptDiff {
            val conceptDiff = diffItems.map { di ->
                di.compare(leftConcept, rightConcept)
            }
            val leftProperty = leftConcept.property ?: listOf()
            val rightProperty = rightConcept.property ?: listOf()
            val propertyDiff: PropertyDiff = keyedListDiff(
                left = leftProperty,
                right = rightProperty,
                getKey = { propertyCode },
                getValue = { this.value } // TODO: 23/12/21 depending on the type of the property, we will need to retrieve the type from PropertyMap
                // and use the valueCoding, etc. instances for comparison. This may require merging the left and right property lists beforehand.
            )
            return ConceptDiff(conceptDiff, propertyDiff)
        }
    }
}

data class ConceptDiffResult(
    val diffItem: ConceptDiffItem,
    val result: ConceptDiffItem.ConceptDiffResultEnum
) {
    fun toString(localizedStrings: LocalizedStrings): String {
        return "ConceptDiffResult(diffItem=${diffItem.toString(localizedStrings)}, result=$result)"
    }
}

data class ConceptDiffItem(
    val label: LocalizedStrings.() -> String,
    private val instanceGetter: FhirConceptDetails.() -> String?
) {
    fun compare(c1: FhirConceptDetails, c2: FhirConceptDetails): ConceptDiffResult {
        val left = instanceGetter.invoke(c1)
        val right = instanceGetter.invoke(c2)
        @Suppress("KotlinConstantConditions") val result = when {
            left == null && right == null -> ConceptDiffResultEnum.IDENTICAL
            (left != null && right == null) || (left == null && right != null) -> ConceptDiffResultEnum.DIFFERENT
            else -> if (left == right) ConceptDiffResultEnum.IDENTICAL else ConceptDiffResultEnum.DIFFERENT
        }
        return ConceptDiffResult(this, result)
    }

    fun toString(localizedStrings: LocalizedStrings): String {
        return "ConceptDiffItem(label='${localizedStrings.label()}')"
    }

    enum class ConceptDiffResultEnum {
        IDENTICAL, DIFFERENT
    }
}

fun <T, K> keyedListDiff(
    left: List<T>,
    right: List<T>,
    getKey: T.() -> K,
    getValue: T.() -> Any
): MutableList<KeyedListDiffResult<K>> {
    val diffResult = mutableListOf<KeyedListDiffResult<K>>()
    val leftKeys = left.map { it.getKey() }.toSet()
    val rightKeys = right.map { it.getKey() }.toSet()
    val onlyInLeft = leftKeys.filter { it !in rightKeys }.toSet()
    onlyInLeft.forEach {
        diffResult.add(
            KeyedListDiffResult(
                kind = KeyedListDiffResult.KeyedListDiffResultKind.KEY_ONLY_IN_LEFT,
                key = it
            )
        )
    }
    val onlyInRight = rightKeys.filter { it !in leftKeys }.toSet()
    onlyInRight.forEach {
        diffResult.add(
            KeyedListDiffResult(
                kind = KeyedListDiffResult.KeyedListDiffResultKind.KEY_ONLY_IN_RIGHT,
                key = it
            )
        )
    }
    val inBoth = leftKeys.plus(rightKeys).minus(onlyInLeft).minus(onlyInRight)
    left.filter { it.getKey() in inBoth }.forEach { l ->
        val valueLeft = l.getValue()
        val matchingRight = right.find { r -> r.getKey() == l.getKey() }!!
        val valueRight = matchingRight.getValue()
        if (valueLeft != valueRight) {
            diffResult.add(
                KeyedListDiffResult(
                    kind = KeyedListDiffResult.KeyedListDiffResultKind.VALUE_DIFFERENT,
                    key = l.getKey(),
                    leftValue = valueLeft,
                    rightValue = valueRight
                )
            )
        }
    }
    return diffResult
}

data class KeyedListDiffResult<K>(
    val kind: KeyedListDiffResultKind,
    val key: K,
    val leftValue: Any? = null,
    val rightValue: Any? = null
) {
    enum class KeyedListDiffResultKind {
        KEY_ONLY_IN_LEFT,
        KEY_ONLY_IN_RIGHT,
        VALUE_DIFFERENT
    }
}