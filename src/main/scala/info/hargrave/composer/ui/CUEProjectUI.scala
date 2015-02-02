package info.hargrave.composer.ui

import info.hargrave.composer._
import info.hargrave.composer.backend.manager.projects.CUEProject
import info.hargrave.composer.ui.CUEProjectUI.CueEntryCell
import info.hargrave.composer.util.CUEUtilities._
import jwbroek.cuelib.{CueSheet, FileData, TrackData}

import scala.collection.JavaConversions._
import scalafx.Includes._
import scalafx.geometry.Side
import scalafx.scene.control.TabPane.TabClosingPolicy
import scalafx.scene.control._
import scalafx.scene.layout.BorderPane

/**
 * Frontend implementation for [[CUEProject CUEProjects]]
 */
class CUEProjectUI(project: CUEProject) extends TabPane {

    private def cueSheet: CueSheet = project.cueSheet match {
        case someSheet: Some[CueSheet]  => someSheet.get
        case None => throw new IllegalArgumentException("project has an invalid cuesheet")
    }

    rotateGraphic = true
    tabClosingPolicy = TabClosingPolicy.UNAVAILABLE
    side = Side.RIGHT

    // Element Editor --------------------------------------------------------------------------------------------------

    private val elementsEditor = new BorderPane()

    private val elementsToolbar = new ToolBar()
    elementsEditor.top = elementsToolbar

    private val elementsList = new TreeView[Either[FileData, TrackData]] {
        root = new TreeItem
        showRoot = false
        cellFactory = {view => new CueEntryCell}
    }

    cueSheet.getFileData.foreach({data =>
                                    val fileItem = new TreeItem[Either[FileData, TrackData]](Left(data))
                                    data.getTrackData.foreach({track => fileItem.getChildren.add(new TreeItem[Either[FileData, TrackData]](Right(track)))})
                                    elementsList.root.value.getChildren.add(fileItem)
                                 })


    elementsEditor.left = elementsList

    tabs += new Tab {
        content = elementsEditor
        text    = t"ui.cue.member_data_editor"
    }.delegate

    // CUE Sheet Metadata Editor ---------------------------------------------------------------------------------------

    private val sheetMetadataEditor = new MetaDataView(cueSheet) {
        editable = true
    }

    tabs += new Tab {
        content = sheetMetadataEditor
        text    = t"ui.cue.sheet_data_editor"
    }.delegate

}
object CUEProjectUI {


    class CueEntryCell extends CustomTreeCell[Either[FileData, TrackData]] {

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
