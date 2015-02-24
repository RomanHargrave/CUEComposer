package info.hargrave.composer.ui.cue

import info.hargrave.composer._
import info.hargrave.composer.ui.PromptInterface.PromptType
import info.hargrave.composer.ui.{Editable, NumberSpinnerCell}
import info.hargrave.composer.util.CUEUtilities._
import info.hargrave.composer.ui.cue.cuelib.ObservableIndex
import info.hargrave.composer.ui.cue.cuelib.ObservablePosition

import jwbroek.cuelib.{Index, Position}

import scalafx.Includes._
import scalafx.beans.property.ObjectProperty
import scalafx.collections.ObservableBuffer
import scalafx.scene.control.TableColumn.{CellDataFeatures, CellEditEvent}
import scalafx.scene.control._
import scalafx.scene.layout.{Priority, VBox}

/**
 * Provides a TableView implementation for viewing editing lists of indices ([[jwbroek.cuelib.Index]])
 */
class IndexTableView private() extends VBox with Editable {


    val indices = ObservableBuffer.empty[Index]

    def this(initialIndices: Traversable[Index]) = {
        this()

        indices ++= initialIndices
    }

    // TableView setup -------------------------------------------------------------------------------------------------

    /*
     * Creates the left-most cell that represents the Index ID.
     */
    private val numberCellFactory   = {(col: TableColumn[Index, Number]) =>
        new NumberSpinnerCell[Index] {
            editable.bind(editableProperty)
            lowerBound = 0
            upperBound = 99
            minWidth.bind(col.minWidth)
        }
    }

    /*
     * Converter hack that works around both NumberSpinner and CueLib behaviour
     */
    private val numberValueFactory  = {(col: CellDataFeatures[Index, Number]) =>
        ObjectProperty[Number](col.value.number.getOrElse[Int](0))
    }

    /*
     * Left-most column that contains the Index ID data
     */
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

    /*
     * Cell factory which creates the right-most cell containing the position view
     */
    private val positionCellFactory = {(col: TableColumn[Index, Position]) =>

        new PositionTableCell[Index] {
            editable.bind(editableProperty)
            minWidth.bind(col.minWidth)
        }
    }
    private val positionValFactory  = {(col: CellDataFeatures[Index, Position]) =>

        /*
         * Unguarded `get`, as the index should have a `position` assigned to it.
         * If it does not, then it did not originate from the CUESheet, or was not added in a sane fashion.
         */
        ObjectProperty[Position](ObservablePosition(col.value.position.get))
    }

    /*
     * Right-most columns containing position data
     */
    private val indexPositionColumn = new TableColumn[Index, Position] {
        text                = t"ui.common.noun_position"
        cellFactory         = positionCellFactory
        cellValueFactory    = positionValFactory
        minWidth            = 200
        editable.bind(editableProperty)
    }

    indexPositionColumn.onEditCommit = {(event: CellEditEvent[Index, Position]) =>
        event.rowValue.getPosition.setMinutes(event.newValue.getMinutes)
        event.rowValue.getPosition.setSeconds(event.newValue.getSeconds)
        event.rowValue.getPosition.setFrames(event.newValue.getFrames)
    }

    /*
     * Table which holds both columns
     */
    private val indexView = new TableView[Index] {
        editable.bind(editableProperty)
        vgrow = Priority.Always
    }

    indexView.columns ++= Seq(indexNumberColumn, indexPositionColumn)

    /*
     * Assigned such that `indexView.items` holds a reference to `indices`, and not a copy thereof.
     * As such, the displayed indices are bound to `indices`
     */
    indexView.items = indices

    // Toolbar Setup ---------------------------------------------------------------------------------------------------

    private val toolbar     = new ToolBar {
        visible.bind(editableProperty)
    }

    private val btnAddIndex = new Button(t"ui.common.verb_add") {

        indices.onChange { disable = indices.size >= 99 }
        onAction = () => {
            indices += new ObservableIndex(largestIndex + 1, new ObservablePosition)
        }
    }
    private val btnRemIndex = new Button(t"ui.common.verb_remove") {

        def updateDisableState(): Unit = {
            disable = indices.size <= 1 || indexView.selectionModel.value.isEmpty
        }

        indices.onChange { updateDisableState() } // Minimum 1 index per track
        indexView.selectionModel.value.selectedItems.onChange { updateDisableState() }

        onAction = () => indexView.selectionModel.value.selectedItems.foreach(indices.-=)
    }

    toolbar.items = Seq(btnAddIndex, btnRemIndex)

    // VBox Setup ------------------------------------------------------------------------------------------------------

    children = Seq(toolbar, indexView)

    // API -------------------------------------------------------------------------------------------------------------

    final def largestIndex: Int = indices.sortWith(_.getNumber > _.getNumber).last.getNumber

}
