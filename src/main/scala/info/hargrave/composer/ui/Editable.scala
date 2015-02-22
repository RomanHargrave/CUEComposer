package info.hargrave.composer.ui

import scalafx.beans.property.BooleanProperty

/**
 * Mixin that provides common functionality to controls that have an 'editable' property
 */
trait Editable {

    import javafx.scene.Node

    val editableProperty = new BooleanProperty()

    def editable = editableProperty.value
    def editable_=(bool: Boolean) = editableProperty.value = bool

    implicit class OtherEditableDecorations(other: Editable) {

        def bindEditable() = other.editableProperty.bind(editableProperty)
    }

    /**
     * Allows for calling dependsOnEditability() on a node to make it depend on the editability of the enclosing
     * Editable
     *
     * @param node implicit node
     */
    implicit class NodeDecorations(node: Node) {

        def bindEditable() = node.disableProperty.bind(!editableProperty)
    }
}
