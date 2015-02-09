package info.hargrave.composer.ui.cue

//import javafx.scene.control.TableColumn.CellEditEvent

import javafx.scene.Node

import info.hargrave.composer._
import info.hargrave.composer.ui.Editable
import info.hargrave.composer.util.CUEUtilities._

import scalafx.Includes._
import scalafx.beans.property._
import scalafx.collections.ObservableBuffer
import scalafx.scene.control.TableColumn.{CellDataFeatures, CellEditEvent}
import scalafx.scene.control._
import scalafx.scene.control.cell.TextFieldTableCell
import scalafx.scene.layout.{Priority, VBox}
import scalafx.util.converter.DefaultStringConverter

/**
 * Provides editing functionality for [[info.hargrave.composer.util.CUEUtilities.HasMetaData]]
 */
class MetaDataView(dataSource: HasMetaData) extends VBox with Editable {

    private val dataTableView   = new TableView[MetaDataAssociation]() {
        vgrow = Priority.Always
    }

    private val tableDataKeyCol = new TableColumn[MetaDataAssociation, String] {
        text        = t"ui.md_view.data_col"
        minWidth    = 150
    }
    private val tableDataValCol = new TableColumn[MetaDataAssociation, String] {
        text        = t"ui.md_view.value_col"
        minWidth    = 200
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

    // Toolbar Setup ---------------------------------------------------------------------------------------------------

    private val editingToolBar  = new ToolBar()
    private val addPropertyBtn  = new MenuButton {
        text = t"ui.common.verb_add"

        items.onChange { disable = items.isEmpty }
    }
    private val remPropertyBtn  = new Button {

        def updateDisabled(): Unit = {
            disable = dataTableView.selectionModel.value.isEmpty || dataTableView.items.value.isEmpty
        }

        dataTableView.items.value.onChange { updateDisabled() }
        dataTableView.selectionModel.value.selectedItems.onChange { updateDisabled() }

        text = t"ui.common.verb_remove"
    }
    editingToolBar.items ++= Seq[Node](addPropertyBtn, remPropertyBtn)

    editingToolBar.visible.bind(editableProperty)

    remPropertyBtn.onAction = () => dataTableView.selectionModel.value.selectedItems.foreach(removeItem)

    // Control Setup ---------------------------------------------------------------------------------------------------

    children += editingToolBar
    children += dataTableView

    // TableView setup -------------------------------------------------------------------------------------------------

    /*
     * Filter the initial list of display items to include only those with defined values
     */
    dataTableView.items = ObservableBuffer(dataSource.dataAccess.toSeq.filter(_.access.value.isDefined))
    dataTableView.items.value.onChange({ synchronizeAvailableFields() })

    // Utility Methods -------------------------------------------------------------------------------------------------

    private def removeItem(assoc: MetaDataAssociation): Unit = {
        assoc.access.value = None
        dataTableView.items.value.remove(assoc)
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
    remPropertyBtn.updateDisabled()
}
