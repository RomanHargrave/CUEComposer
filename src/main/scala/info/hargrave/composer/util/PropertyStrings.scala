package info.hargrave.composer.util

/**
 * Contains PropertyStringContext (which doesn't want scaladoc to link to it)
 */
trait PropertyStrings {

    /**
     * Allows shorthand lookup of system properties using p""
     *
     * ex: ''p"user.home"''
     *
     * @param cx strings parts
     */
    implicit class PropertyStringContext (val cx: StringContext) {
        def p(parts: String*): String = System.getProperty(cx.s(parts))
    }
}