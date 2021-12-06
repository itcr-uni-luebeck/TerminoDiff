package terminodiff.i18n

import terminodiff.engine.metadata.MetadataDiff.MetadataDiffItemResult

abstract class LocalizedStrings(
    val canonicalUrl: String,
    val contact: String,
    val date: String,
    val description: String,
    val experimental: String,
    val id: String = "ID",
    val identifiers: String,
    val jurisdiction: String,
    val language: String,
    val metadataDiffResults: (MetadataDiffItemResult) -> String,
    val name: String = "Name",
    val publisher: String,
    val status: String = "Status",
    val terminoDiff: String = "TerminoDiff",
    val title: String,
    val version: String = "Version",
)

enum class SupportedLocale {
    EN,
    DE;

    companion object {
        fun getDefaultLocale() = EN
    }
}

class GermanStrings : LocalizedStrings(
    canonicalUrl = "Kanonische URL",
    contact = "Kontakt",
    date = "Datum",
    description = "Beschreibung",
    experimental = "Experimentell?",
    identifiers = "IDs",
    jurisdiction = "Jurisdiktion",
    language = "de",
    metadataDiffResults = {
        when (it) {
            MetadataDiffItemResult.BOTH_EMPTY -> "beide leer"
            MetadataDiffItemResult.DIFFERENT -> "unterschiedlich"
            MetadataDiffItemResult.IDENTICAL -> "identisch"
            MetadataDiffItemResult.DIFFERENT_COUNT -> "unterschiedliche Anzahl"
            MetadataDiffItemResult.BOTH_NULL -> "beide Null"
            MetadataDiffItemResult.DIFFERENT_TEXT -> "unterschiedlicher Text"
        }
    },
    publisher = "Herausgeber",
    title = "Titel",
)

class EnglishStrings : LocalizedStrings(
    canonicalUrl = "Canonical URL",
    contact = "Contact",
    date = "Date",
    description = "Description",
    experimental = "Experimental?",
    identifiers = "Identifiers",
    jurisdiction = "Jurisdiction",
    language = "en",
    metadataDiffResults = {
        when (it) {
            MetadataDiffItemResult.BOTH_EMPTY -> "both empty"
            MetadataDiffItemResult.DIFFERENT -> "different"
            MetadataDiffItemResult.IDENTICAL -> "identical"
            MetadataDiffItemResult.DIFFERENT_COUNT -> "different count"
            MetadataDiffItemResult.BOTH_NULL -> "both null"
            MetadataDiffItemResult.DIFFERENT_TEXT -> "different text"
        }
    },
    publisher = "Publisher",
    title = "Title",
)

val defaultStrings = getStrings()

fun getStrings(locale: SupportedLocale = SupportedLocale.getDefaultLocale()): LocalizedStrings =
    when (locale) {
        SupportedLocale.DE -> GermanStrings()
        SupportedLocale.EN -> EnglishStrings()
    }
