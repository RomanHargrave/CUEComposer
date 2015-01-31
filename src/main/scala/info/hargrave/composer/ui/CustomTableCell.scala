package info.hargrave.composer.ui

import javafx.scene.control.{TableCell => JFXTableCell}

import info.hargrave.composer.ui.CustomTableCell.JFXTableCellUpdateDelegate

import scalafx.delegate.SFXDelegate
import scalafx.scene.control.TableCell

/**
 * Provides a tablecell with updateItem support
 */
abstract class CustomTableCell[S, T](override val delegate: JFXTableCellUpdateDelegate[S, T] = new JFXTableCellUpdateDelegate[S, T]) extends TableCell[S, T](delegate) with SFXDelegate[JFXTableCellUpdateDelegate[S, T]] {

    def updateItem(item: T, empty: Boolean): Unit

    delegate.updateAction = Some({(i: T, e: Boolean) => updateItem(i, e)})
}
object CustomTableCell {

    class JFXTableCellUpdateDelegate[S, T] extends JFXTableCell[S, T] {

        private[ui] var updateAction: Option[(T, Boolean) => _] = None

        final override def updateItem(item: T, empty: Boolean): Unit = {
            super.updateItem(item, empty)
            updateAction match {
                case Some(action)   => action(item, empty)
                case None           => // do nothing
            }
        }
    }
}
