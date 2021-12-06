package terminodiff.i18n

import androidx.compose.ui.text.intl.Locale

abstract class Strings(
    val language: String,
    val terminoDiff: String = "TerminoDiff"
)

enum class SupportedLocale(val locale: Locale) {
    EN(Locale("en")),
    DE(Locale("de"));
    companion object {
        fun getDefaultLocale() = EN
    }
}

class GermanStrings : Strings(
    language = "de",
    terminoDiff = "German TerminoDiff"
)

class EnglishStrings : Strings(
    language = "en",
    terminoDiff = "English TerminoDiff"
)

fun getStrings(locale: SupportedLocale): Strings =
    when (locale) {
        SupportedLocale.DE -> GermanStrings()
        SupportedLocale.EN -> EnglishStrings()
    }
