package info.hargrave.composer.util

import info.hargrave.composer.util
import info.hargrave.composer.util.Localization.TString
import org.streum.configrity._
import org.streum.configrity.io.FlatFormat

import scala.collection.immutable.StringLike
import scala.io.Source

/**
 * Trait that can be mixed in where i18n resources are needed.
 * All that this trait provides is an implicit converter for StringContext that provides extended string
 * functionality for translation.
 *
 * If infrequent translation is needed, or it is not possible to extend the trait, the Localization companion object
 * provides a translation method (the same as used in `TranslatableStringContext(StringContext)`)
 * @author Roman Hargrave <roman@hargrave.info>
 */
trait Localization {

    /**
     * Provides additional functionality to StringContext that allows for shorthanded translation of strings.
     *
     * For instance, where Localization is mixed in and translation is needed, `t"name"` can be used in place of
     * [[util.Localization.translate(String)]]
     *
     * @param context StringContext object passed by the runtime during implicit conversion
     */
    implicit class TranslatableStringContext (val context: StringContext) {
        def t(args: Any*): String = Localization.translate(context.s(args:_*))
        def tf(args: Any*): TString = new TString(context.s(args:_*))
    }

    /**
     * Turns a TString in to a String, because the compiler/vm does not seem to comprehend what should be de-facto
     * stringification
     *
     * @param tstr t-string
     * @return string
     */
    implicit def tString2String(tstr: TString): String = tstr.toString
}

/**
 * Companion object to [[util.Localization]] that wraps the i18n configuration.
 *
 * The [[https://github.com/paradigmatic/Configrity Configrity]] library is used to read i18n configuration files
 * (stored under `src/main/resources/locale/` or `locale/` in the jar, with the extension `cfg`). The configurations are
 * stored according to [[org.streum.configrity.io.FlatFormat]], which is a basic `key = value` format.
 *
 * In order to select locale, the VM system property `locale` may be set to a locale named in accordance with common
 * convention, (e.g. en, en_US, pt, pt_BR, fr). The locale files are named according to the same convention. If the
 * `locale` variable is unset (or set such that getProperty will return a value interpretable as None), the locale
 * configuration will default to `en_US`.
 */
object Localization {

    private val localeName          =   Option(System.getProperty("locale")) match {
                                            case str:Some[String] => str.get
                                            case None => "en_US"
                                        }
    private val localeConfiguration =
        Configuration.load(Source.fromInputStream(getClass.getResourceAsStream(s"/locale/$localeName.cfg")), FlatFormat)

    /**
     * Convert the translation name in to the appropriate localized string.
     * If no translation exists for the name, the name will be returned.
     * @param name translation name
     * @return localized string, or if not available, the name
     */
    def translate(name: String): String = localeConfiguration.get[String](name) match {
        case text:Some[String] => text.get
        case None => name
    }

    final class TString(str: String) {

        def apply(values: Any*): String = {
            translate(str).format(values)
        }

        override def toString: String = translate(str)
    }
}