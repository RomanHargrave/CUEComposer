package info.hargrave.composer.ui

import jwbroek.cuelib.{TrackData, FileData, CueSheet}

import scalafx.Includes._
import scalafx.beans.property.BooleanProperty
import scalafx.event.subscriptions.Subscription
import scalafx.scene.control.{TreeItem, TreeView, ToolBar}
import scalafx.scene.layout.{Priority, VBox, BorderPane}

import info.hargrave.composer.ui.CUESheetMemberTree.CueEntryCell
import info.hargrave.composer.util.CUEUtilities._
import info.hargrave.composer._

import scala.collection.JavaConversions._

/**
 * Displays a CUE Sheet as a two-level TreeView[Either[FileData,TrackData]] from which the user can select file sections,
 * or track declarations within the sections.
 *
 * It provides a selection callback that will provide the Either[...] selected by the user.
 *
 * When editable is true, it will display a toolbar that allows for addition and removal of sheet members.
 */
class CUESheetMemberTree(sheet: CueSheet) extends VBox {

    val editableProperty = new BooleanProperty

    private val elementsToolbar = new ToolBar()
    children += elementsToolbar
    elementsToolbar.visible.bind(editableProperty)

    private val elementsList = new TreeView[Either[FileData, TrackData]] {
        root = new TreeItem
        showRoot = false
        cellFactory = {view => new CueEntryCell}
        vgrow   = Priority.Always
    }
    children += elementsList

    // Data Setup ------------------------------------------------------------------------------------------------------

    /*
     * For each file entry create an item, then construct a sequence of items for each track entry and assign them to
     * the file item as children.
     */
    private val fileEntries = sheet.getFileData.map({data =>
        val fileItem = new TreeItem[Either[FileData, TrackData]](Left(data))

        fileItem.children ++= data.getTrackData.map({track =>
            new TreeItem[Either[FileData, TrackData]](Right(track)).delegate
        })

        fileItem.delegate
    })

    elementsList.root.value.children ++= fileEntries

    // Component API ---------------------------------------------------------------------------------------------------

    final def editable = editableProperty.value
    final def editable_=(bool: Boolean) = editableProperty.value = bool

    final def selectedItem = elementsList.selectionModel.value.getSelectedItem.value match {
        case null       => None
        case treeItem   => Option(treeItem.value)
    }

    final def onSelectionChanged(op: Option[Either[FileData, TrackData]]=>_): Subscription =
        elementsList.selectionModel.value.selectedItemProperty.onChange ({
            op(selectedItem)
            ()
        })
}
object CUESheetMemberTree {
    final class CueEntryCell extends CustomTreeCell[Either[FileData, TrackData]] {

        override def updateItem(item: Either[FileData, TrackData], empty: Boolean): Unit = empty match {
            case true =>
                text = null
            case false =>
                text = item match {
                    case null => null // oh java
                    case Left(fileData)     =>
                        s"${fileData.getFileType} ${fileData.getFile}"
                    case Right(trackData)   =>
                        if(trackData.getNumber > 0) tf"ui.cue.track_entry"(trackData.getNumber) else t"ui.cue.undefined_track"
                }
        }
    }
}