package info.hargrave.composer.ui

import info.hargrave.composer._
import info.hargrave.composer.backend.manager.projects.CUEProject
import info.hargrave.composer.util.CUEUtilities._
import jwbroek.cuelib.{CueSheet, FileData, TrackData}

import scala.collection.JavaConversions._
import scalafx.Includes._
import scalafx.collections.ObservableBuffer
import scalafx.geometry.Side
import scalafx.scene.control.TabPane.TabClosingPolicy
import scalafx.scene.control._
import scalafx.scene.layout.{Priority, Pane, HBox, BorderPane}

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

    /*
     * Contains the active cuesheet element
     */
    private val elementsEditor  = new HBox {
        vgrow       = Priority.Always
        hgrow       = Priority.Always
        fillHeight  = true
    }
    private val elementTree     = new CUESheetMemberTree(cueSheet){
        editable    = true
        vgrow       = Priority.Always
    }
    private val activeElement   = new Pane

    elementTree.onSelectionChanged {(selection: Option[Either[FileData, TrackData]]) =>
        val newChild = selection match {
            case Some(Left(fileData))   => Some(new FileDataView(fileData) {
                editable = true
            })
            case Some(Right(trackData)) => None
            case None                   => None
        }

        activeElement.children = if(newChild.isDefined) ObservableBuffer(Seq(newChild.get)) else null
    }

    elementsEditor.children ++= Seq(elementTree, activeElement)

    tabs += new Tab {
        content = elementsEditor
        text    = t"ui.cue.member_data_editor"
    }.delegate

    // CUE Sheet Metadata Editor ---------------------------------------------------------------------------------------

    private val sheetMetadataEditor = new MetaDataView(cueSheet) {
        editable    = true
        vgrow       = Priority.Always
        hgrow       = Priority.Always
    }

    tabs += new Tab {
        content = sheetMetadataEditor
        text    = t"ui.cue.sheet_data_editor"
    }.delegate

    hgrow   = Priority.Always
    vgrow   = Priority.Always
}

