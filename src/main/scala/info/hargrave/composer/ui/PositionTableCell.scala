package info.hargrave.composer.ui

import javafx.scene.control.{TableCell => JFXTableCell}

import info.hargrave.composer.util.CUEUtilities._
import info.hargrave.composer.ui.PositionTableCell.JFXImpl

import jwbroek.cuelib.Position

import scalafx.delegate.SFXDelegate
import scalafx.scene.control.TableCell
import scalafx.scene.input.{KeyCode, KeyEvent}
import scalafx.Includes._

/**
 * Provides a TableCell that allows for the display and editing of a position
 */
class PositionTableCell[S](override val delegate: JFXImpl[S] = new JFXImpl[S])
        extends TableCell[S, Position] with SFXDelegate[JFXImpl[S]]
object PositionTableCell {

    /**
     * Custom delegate class that provides the implementation of startEdit, cancelEdit, and updateItem
     * for displaying positions
     *
     * @tparam S item data type
     */
    final class JFXImpl[S] extends JFXTableCell[S, Position] {

        private var position: Option[Position] = None
        private var positionView: Option[PositionView] = None

        override def startEdit(): Unit = if(isEditable && getTableView.isEditable && getTableColumn.isEditable) {
            super.startEdit()

            if(positionView.isEmpty) {
                positionView = Some(new PositionView(position.get) {
                    onKeyReleased = { (event: KeyEvent) =>
                        println(event)
                        event.code match {
                            case KeyCode.ENTER =>
                                commitEdit(value)
                            case KeyCode.ESCAPE =>
                                cancelEdit()
                            case _ =>
                        }

                    }
                })

                positionView.get.editableProperty.bind(editableProperty())
            }

            positionView.get.value = position.get

            setText(null)
            setGraphic(positionView.get)
        }

        override def cancelEdit(): Unit = {
            setGraphic(null)
            setText(position.getOrElse(new Position).formatted)
            super.cancelEdit()
        }

        override def updateItem(item: Position, empty: Boolean): Unit = empty match {
            case true =>
                setText(null)
                setGraphic(null)
                position = None
                super.updateItem(item, empty)
            case false =>
                position = Some(item)
                setGraphic(null)
                setText(item.formatted)
                super.updateItem(item, empty)
        }
    }
}
