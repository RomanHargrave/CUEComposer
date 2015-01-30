package info.hargrave.composer.ui

import javafx.scene.control.{TreeCell => JFXTreeCell}

import info.hargrave.composer.ui.CustomTreeCell.JFXTreeCellUpdateDelegate

import scalafx.delegate.SFXDelegate
import scalafx.scene.control.TreeCell

/**
 * Provides a CustomTreeCell that allows for implementation of the UpdateItem method
 */
abstract class CustomTreeCell[T](override val delegate: JFXTreeCellUpdateDelegate[T] = new JFXTreeCellUpdateDelegate[T])
        extends TreeCell[T](delegate)
        with SFXDelegate[JFXTreeCellUpdateDelegate[T]] {

    delegate.updateAction = Some({(item: T, empty: Boolean) => updateItem(item, empty)})

    def updateItem(item: T, empty: Boolean): Unit
}
object CustomTreeCell {

    class JFXTreeCellUpdateDelegate[T] extends JFXTreeCell[T] {

        private[ui] var updateAction: Option[(T, Boolean) => _] = None

        final override def updateItem(item: T, empty: Boolean): Unit = {
            updateAction match {
                case Some(action) => action(item, empty)
                case None => // do nothing
            }
            super.updateItem(item, empty)
        }
    }
}