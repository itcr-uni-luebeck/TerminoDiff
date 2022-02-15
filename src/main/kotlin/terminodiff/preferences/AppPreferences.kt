package terminodiff.preferences

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import terminodiff.TerminoDiffApp
import terminodiff.i18n.SupportedLocale
import java.util.prefs.Preferences
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

private val logger: Logger = LoggerFactory.getLogger(AppPreferences::class.java)

/**
 * https://stackoverflow.com/q/66462586
 */
object AppPreferences {
    //val systemPref by lazy { Preferences.systemNodeForPackage(TerminoDiffApp::class.java) }
    private val userPref: Preferences by lazy {
        Preferences.userNodeForPackage(TerminoDiffApp::class.java).also { pref ->
            logger.debug("loaded preferences from ${pref.absolutePath()}")
        }
    }

    var language: String by preference(userPref, "language_", SupportedLocale.defaultLocale.name)
    var darkModeEnabled: Boolean by preference(userPref, "dark_mode_enabled", false)
    var fileBrowserDirectory: String by preference(userPref, "file_browser_directory", System.getProperty("user.home"))
    var terminologyServerUrl: String by preference(userPref, "terminology_server_url", "https://r4.ontoserver.csiro.au/fhir")
}

inline fun <reified T : Any> preference(preferences: Preferences, key: String, defaultValue: T) =
    PreferenceDelegate(preferences, key, defaultValue, T::class)

class PreferenceDelegate<T : Any>(
    private val preferences: Preferences,
    private val key: String,
    private val defaultValue: T,
    val type: KClass<T>
) : ReadWriteProperty<Any, T> {

    @Suppress("UNCHECKED_CAST")
    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        with(preferences) {
            when (type) {
                Int::class -> putInt(key, value as Int)
                Long::class -> putLong(key, value as Long)
                Float::class -> putFloat(key, value as Float)
                Boolean::class -> putBoolean(key, value as Boolean)
                String::class -> put(key, value as String)
                ByteArray::class -> putByteArray(key, value as ByteArray)
                else -> error("Unsupported preference type $type.")
            }
        }.also {
            logger.debug("stored [$key]='$value'")
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun getValue(thisRef: Any, property: KProperty<*>): T {
        return with(preferences) {
            when (type) {
                Int::class -> getInt(key, defaultValue as Int)
                Long::class -> getLong(key, defaultValue as Long)
                Float::class -> getFloat(key, defaultValue as Float)
                Boolean::class -> getBoolean(key, defaultValue as Boolean)
                String::class -> get(key, defaultValue as String)
                ByteArray::class -> getByteArray(key, defaultValue as ByteArray)
                else -> error("Unsupported preference type $type.")
            }
        } as T
    }

}