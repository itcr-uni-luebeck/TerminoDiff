package terminodiff.engine.concepts

import org.hl7.fhir.r4.model.CodeSystem
import terminodiff.engine.graph.FhirConceptDetails
import terminodiff.i18n.LocalizedStrings

typealias FhirConcept = CodeSystem.ConceptDefinitionComponent

typealias PropertyDiff = MutableList<KeyedListDiffResult<String, String>>

data class ConceptDiff(
    val conceptComparison: List<ConceptDiffResult>,
    val propertyComparison: PropertyDiff,
) {
    fun toString(localizedStrings: LocalizedStrings): String {
        return "ConceptDiff(conceptComparison=[${conceptComparison.map { it.toString(localizedStrings) }}], " +
                "propertyComparison=[${propertyComparison.joinToString(",")}]"
    }

    companion object {

        private val diffItems = listOf(
            ConceptDiffItem({ display }, { display }),
            ConceptDiffItem({ definition }, { definition })
        )

        fun compareConcept(
            leftConcept: FhirConceptDetails,
            rightConcept: FhirConceptDetails,
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
                getStringValue = { this.value } // TODO: 23/12/21 depending on the type of the property, we will need to retrieve the type from PropertyMap
                // and use the valueCoding, etc. instances for comparison. This may require merging the left and right property lists beforehand.
            )
            return ConceptDiff(conceptDiff, propertyDiff)
        }
    }
}

data class ConceptDiffResult(
    val diffItem: ConceptDiffItem,
    val result: ConceptDiffItem.ConceptDiffResultEnum,
) {
    fun toString(localizedStrings: LocalizedStrings): String {
        return "ConceptDiffResult(diffItem=${diffItem.toString(localizedStrings)}, result=$result)"
    }
}

data class ConceptDiffItem(
    val label: LocalizedStrings.() -> String,
    private val instanceGetter: FhirConceptDetails.() -> String?,
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
    getStringValue: T.() -> String?,
): MutableList<KeyedListDiffResult<K, String>> {
    val diffResult = mutableListOf<KeyedListDiffResult<K, String>>()
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
    diffResult.addAll(left.filter { it.getKey() in inBoth }.groupBy(getKey).map { l ->
        val valueLeft = l.value.map(getStringValue)
        val matchingRight = right.filter { r -> r.getKey() == l.key }
        val valueRight = matchingRight.map(getStringValue)
        val result =
            when {
                valueLeft != valueRight -> KeyedListDiffResult.KeyedListDiffResultKind.VALUE_DIFFERENT
                else -> KeyedListDiffResult.KeyedListDiffResultKind.IDENTICAL
            }
        KeyedListDiffResult(
            kind = result,
            key = l.key,
            leftValue = valueLeft,
            rightValue = valueRight
        )
    })
    return diffResult
}

data class KeyedListDiffResult<K, V>(
    val kind: KeyedListDiffResultKind,
    val key: K,
    val leftValue: List<V?>? = null,
    val rightValue: List<V?>? = null,
) {
    enum class KeyedListDiffResultKind {
        KEY_ONLY_IN_LEFT,
        KEY_ONLY_IN_RIGHT,
        VALUE_DIFFERENT,
        IDENTICAL,
    }
}