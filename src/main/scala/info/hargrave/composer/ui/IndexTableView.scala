package info.hargrave.composer.ui

import info.hargrave.composer.ui.PromptInterface.PromptType
import jwbroek.cuelib.{Position, Index}

import scalafx.beans.property.ObjectProperty
import scalafx.collections.ObservableBuffer
import scalafx.scene.control.TableColumn.{CellEditEvent, CellDataFeatures}
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
            minWidth.bind(col.minWidth)
        }
    }
    private val numberValueFactory  = {(col: CellDataFeatures[Index, Number]) =>
        ObjectProperty[Number](col.value.number.getOrElse[Int](0))
    }
    private val indexNumberColumn   = new TableColumn[Index, Number] {
        text                = t"ui.common.noun_number"
        cellFactory         = numberCellFactory
        cellValueFactory    = numberValueFactory
        minWidth            = 80
        editable.bind(editableProperty)
    }
    indexNumberColumn.onEditCommit  = {(event: CellEditEvent[Index, Number]) =>
        if(indices.exists(_.number == Option(event.newValue))){
            event.consume()
            Prompts.displayNotificationPrompt(t"notification.index_error", t"notification.index_error.banner",
                                              tf"notification.duplicate_index"(event.newValue), PromptType.ERROR)
        } else {
            event.rowValue.number = Option(event.newValue.intValue)
        }
    }

    private val positionCellFactory = {(col: TableColumn[Index, Position]) =>
        new PositionTableCell[Index] {
            editable.bind(editableProperty)
            minWidth.bind(col.minWidth)
        }
    }
    private val positionValFactory  = {(col: CellDataFeatures[Index, Position]) =>
        ObjectProperty(col.value.position.getOrElse(new Position()))
    }
    private val indexPositionColumn = new TableColumn[Index, Position] {
        text                = t"ui.common.noun_position"
        cellFactory         = positionCellFactory
        cellValueFactory    = positionValFactory
        minWidth            = 200
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
