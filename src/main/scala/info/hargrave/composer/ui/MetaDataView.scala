package info.hargrave.composer.ui

//import javafx.scene.control.TableColumn.CellEditEvent

import info.hargrave.composer.util.CUEUtilities

import scalafx.beans.property._
import scalafx.collections.ObservableBuffer
import scalafx.scene.control.TableColumn.{CellEditEvent, CellDataFeatures}
import scalafx.scene.control.cell.{TextFieldTableCell, ComboBoxTableCell}
import scalafx.scene.control._
import scalafx.scene.layout.BorderPane
import scalafx.Includes._

import info.hargrave.composer._
import info.hargrave.composer.util.CUEUtilities._

import scalafx.util.StringConverter
import scalafx.util.converter.DefaultStringConverter

/**
 * Provides editing functionality for [[info.hargrave.composer.util.CUEUtilities.HasMetaData]]
 */
class MetaDataView(dataSource: HasMetaData) extends BorderPane {

    val editableProperty: BooleanProperty = new BooleanProperty

    private val editingToolBar  = new ToolBar()
    private val addPropertyBtn  = new MenuButton {
        text = t"ui.common.verb_add"
    }
    private val remPropertyBtn  = new Button {
        text = t"ui.common.verb_remove"
    }
    editingToolBar.items ++= Seq(addPropertyBtn, remPropertyBtn)

    private val dataTableView   = new TableView[MetaDataAssociation]()

    private val tableDataKeyCol = new TableColumn[MetaDataAssociation, String] {
        text = t"ui.md_view.data_col"
    }
    private val tableDataValCol = new TableColumn[MetaDataAssociation, String] {
        text = t"ui.md_view.value_col"
    }
    dataTableView.columns ++= Seq(tableDataKeyCol, tableDataValCol)
    dataTableView.editable.bind(editableProperty)

    // JFX Callback Implementations ------------------------------------------------------------------------------------

    private val mdNameConverter     = {(assoc: CellDataFeatures[MetaDataAssociation, String]) =>
        ReadOnlyStringWrapper(MetaDataAssociations.Localisation(assoc.value.name)) }

    private val mdAccessValFactory  = {(acc: CellDataFeatures[MetaDataAssociation, String]) =>
        StringProperty(acc.value.access.value.getOrElse(t"ui.common.adj_unset")) }

    private val mdAccValStringConv  = {(acc: MetaDataAccess) =>
        acc.value.orNull }

    private val mdAccessCellFactory = {(col: TableColumn[MetaDataAssociation, String]) =>
        new TextFieldTableCell[MetaDataAssociation, String] {
            this.delegate.setConverter(new DefaultStringConverter)
            this.editable = editableProperty.value
            this.editable.bind(editableProperty)
        }
    }

    tableDataKeyCol.cellValueFactory = mdNameConverter

    tableDataValCol.cellFactory      = mdAccessCellFactory
    tableDataValCol.cellValueFactory = mdAccessValFactory
    tableDataValCol.onEditCommit     = (event: CellEditEvent[MetaDataAssociation, String]) => {
        val access = event.rowValue.access
        logger.debug(s"updating $dataSource(${event.rowValue.name} = ${access.value}) from cell value ${event.oldValue} to ${event.newValue}")
        access.value = Some(event.newValue)
    }

    // Control API -----------------------------------------------------------------------------------------------------

    final def editable = editableProperty.value
    final def editable_=(bool: Boolean) = editableProperty.value = bool

    // Control Setup ---------------------------------------------------------------------------------------------------

    center = dataTableView

    // TableView setup -------------------------------------------------------------------------------------------------

    /*
     * Filter the initial list of display items to include only those with defined values
     */
    dataTableView.items = ObservableBuffer(dataSource.dataAccess.toSeq.filter(_.access.value.isDefined))
    dataTableView.items.value.onChange({synchronizeAvailableFields()})

    // Toolbar Setup ---------------------------------------------------------------------------------------------------

    editableProperty.onChange({ top = if(editable) editingToolBar else null })

    remPropertyBtn.onAction = () => {
        val removedItem = Option(dataTableView.selectionModel.value.selectedItemProperty.value)
        dataTableView.items.value.remove(dataTableView.selectionModel.value.getSelectedIndex)

        removedItem match {
            case Some(association)  =>
                association.access.value = None
            case None   =>
                logger.debug("Invalid attempt to remove a non-existant table entry")
        }
    }

    dataTableView.selectionModel.value.selectedIndex.onChange({updateRemoveButtonState()})

    // Utility Methods -------------------------------------------------------------------------------------------------

    private def updateRemoveButtonState(): Unit = {
        remPropertyBtn.disable = dataTableView.selectionModel.value.selectedItemProperty.value == null
    }

    private def synchronizeAvailableFields(): Unit = {
        val availableAccesses = dataSource.dataAccess.toSeq.diff(dataTableView.items.value)
        val menuItems = if(availableAccesses.isEmpty) {
            Seq(new MenuItem{
                text = t"ui.md_view.no_fields"
                disable = true
            })
        } else {
            availableAccesses.map {access =>
                new MenuItem {
                    text = MetaDataAssociations.Localisation(access.name)
                    onAction = () => dataTableView.items.value += access
                }
            }
        }

        addPropertyBtn.items = ObservableBuffer(menuItems)
    }

    synchronizeAvailableFields()
}
