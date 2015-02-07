package info.hargrave.composer.ui

import scalafx.beans.property.BooleanProperty

/**
 * Mixin that provides common functionality to controls that have an 'editable' property
 */
trait Editable {

    val editableProperty = new BooleanProperty()

    def editable = editableProperty.value
    def editable_=(bool: Boolean) = editableProperty.value = bool
}
