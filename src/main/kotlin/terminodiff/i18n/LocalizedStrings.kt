package terminodiff.i18n

abstract class LocalizedStrings(
    val language: String,
    val terminoDiff: String = "TerminoDiff",
    val canonicalUrl: String,
    val version: String = "Version",
    val title: String,
    val name: String = "Name",
    val id: String = "ID",
    val identifiers: String,
    val status: String = "Status",
    val experimental: String,
    val date: String,
    val publisher: String,
    val contact: String,
    val description: String,
    val jurisdiction: String
)

enum class SupportedLocale {
    EN,
    DE;

    companion object {
        fun getDefaultLocale() = EN
    }
}

class GermanStrings : LocalizedStrings(
    language = "de",
    canonicalUrl = "Kanonische URL",
    title = "Titel",
    identifiers = "IDs",
    experimental = "Experimentell?",
    date = "Datum",
    publisher = "Herausgeber",
    contact = "Kontakt",
    description = "Beschreibung",
    jurisdiction = "Jurisdiktion"
)

class EnglishStrings : LocalizedStrings(
    language = "en",
    canonicalUrl = "Canonical URL",
    title = "Title",
    identifiers = "Identifiers",
    experimental = "Experimental?",
    date = "Date",
    publisher = "Publisher",
    contact = "Contact",
    description = "Description",
    jurisdiction = "Jurisdiction"
)

val defaultStrings = getStrings()

fun getStrings(locale: SupportedLocale = SupportedLocale.getDefaultLocale()): LocalizedStrings =
    when (locale) {
        SupportedLocale.DE -> GermanStrings()
        SupportedLocale.EN -> EnglishStrings()
    }
