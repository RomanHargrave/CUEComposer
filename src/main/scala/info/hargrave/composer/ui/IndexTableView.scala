package info.hargrave.composer.ui

import jwbroek.cuelib.{Position, Index}

import javafx.scene.control.{TableView => JFXTableView}

import scalafx.beans.property.ObjectProperty
import scalafx.collections.ObservableBuffer
import scalafx.scene.control.TableColumn.{CellEditEvent, CellDataFeatures}
import scalafx.scene.control.TableView.ResizeFeatures
import scalafx.scene.control.{Button, ToolBar, TableColumn, TableView}
import scalafx.scene.layout.{Priority, VBox}
import scalafx.Includes._

import info.hargrave.composer._
import info.hargrave.composer.util.CUEUtilities._

/**
 * Provides a TableView implementation for viewing editing lists of indices ([[jwbroek.cuelib.Index]])
 */
class IndexTableView(indices: Seq[Index]) extends VBox with Editable {

    // Toolbar Setup ---------------------------------------------------------------------------------------------------

    private val toolbar = new ToolBar {
        visible.bind(editableProperty)
    }

    toolbar.items = Seq(new Button("Dummy Text"))

    // TableView setup -------------------------------------------------------------------------------------------------

    private val numberCellFactory   = {(col: TableColumn[Index, Number]) =>
        new NumberSpinnerCell[Index] {
            editable.bind(editableProperty)
            lowerBound = 0
            upperBound = 99
            prefWidth.bind(col.prefWidth)
        }
    }
    private val numberValueFactory  = {(col: CellDataFeatures[Index, Number]) =>
        ObjectProperty[Number](col.value.number.getOrElse[Int](0))
    }
    private val indexNumberColumn   = new TableColumn[Index, Number] {
        text                = t"ui.common.noun_number"
        cellFactory         = numberCellFactory
        cellValueFactory    = numberValueFactory
        prefWidth           = 67
        editable.bind(editableProperty)
    }
    indexNumberColumn.onEditCommit  = {(event: CellEditEvent[Index, Number]) =>
        logger.trace(s"${event.rowValue.formatted} number updating to ${event.newValue.intValue}")
        event.rowValue.number = Option(event.newValue.intValue)
        logger.debug(s"${event.rowValue.formatted} number updated to ${event.rowValue.number}")

    }

    private val positionCellFactory = {(col: TableColumn[Index, Position]) =>
        new PositionTableCell[Index] {
            editable.bind(editableProperty)
            prefWidth.bind(col.prefWidth)
        }
    }
    private val positionValFactory  = {(col: CellDataFeatures[Index, Position]) =>
        ObjectProperty(col.value.position.getOrElse(new Position()))
    }
    private val indexPositionColumn = new TableColumn[Index, Position] {
        text                = t"ui.common.noun_position"
        cellFactory         = positionCellFactory
        cellValueFactory    = positionValFactory
        prefWidth           = 200
        editable.bind(editableProperty)
    }
    indexPositionColumn.onEditCommit    = {(event: CellEditEvent[Index, Position]) =>
        event.rowValue.position = Option(event.newValue)
    }

    private val indexView = new TableView[Index] {
        editable.bind(editableProperty)
        vgrow = Priority.Always
    }

    indexView.columns ++= Seq(indexNumberColumn, indexPositionColumn)
    indexView.items = ObservableBuffer(indices)

    // VBox Setup ------------------------------------------------------------------------------------------------------

    children = Seq(toolbar, indexView)

}
