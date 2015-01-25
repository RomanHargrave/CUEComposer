package info.hargrave.composer.util

import java.util.regex.Pattern

/**
 * Adds a implicit class comprehension to the context that allows for r"" strings that do regex patterns
 */
trait RegexStrings {

    implicit class PatternStringContext(cx: StringContext) {
        def r(parts: String*): Pattern = Pattern.compile(cx.raw(parts))
    }

}
