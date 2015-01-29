package info.hargrave.composer.ui

import javafx.scene.control.{ListCell => JFXCell}

import info.hargrave.composer.ui.CustomListCell.JFXCellUpdateDelegate

import scalafx.scene.control.ListCell

abstract class CustomListCell[T](override val delegate: JFXCellUpdateDelegate[T] = new JFXCellUpdateDelegate[T]) extends ListCell[T]() {

    delegate.updateFunction = Some(updateItem(_, _))

    def updateItem(item: T, empty: Boolean): Unit
}
object CustomListCell {

    /**
     * Provides a configurable UpdateItem method override for use with scalaFX as a delegate cell.
     */
    class JFXCellUpdateDelegate[T] extends JFXCell[T] {

        private[ui] var updateFunction: Option[((T, Boolean) => _)] = None

        override def updateItem(item: T, empty: Boolean): Unit = {
            super.updateItem(item, empty)
            updateFunction match {
                case someFunction: Some[((T, Boolean) => _)] => someFunction.get.apply(item, empty)
                case None => // Do nothing
            }
        }


    }
}