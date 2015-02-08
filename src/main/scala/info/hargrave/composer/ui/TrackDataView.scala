package info.hargrave.composer.ui

import com.blogspot.myjavafx.NumberSpinner
import jwbroek.cuelib.{Position, Index, TrackData}

import scalafx.beans.property.{ObjectProperty, StringProperty, BooleanProperty}
import scalafx.collections.ObservableBuffer
import scalafx.scene.control.TableColumn.CellDataFeatures
import scalafx.scene.control.{SplitPane, TableCell, TableColumn, TableView}
import scalafx.scene.layout.{HBox, GridPane, VBox, Priority}
import scalafx.Includes._

import javafx.geometry.Insets
import javafx.scene.control.{TableCell => JFXTableCell}

import info.hargrave.composer._
import info.hargrave.composer.util.CUEUtilities._

import scala.collection.JavaConverters._

/**
 * Date: 2/3/15
 * Time: 11:25 AM
 */
class TrackDataView(trackData: TrackData) extends SplitPane with Editable {

    // Metadata Editor -------------------------------------------------------------------------------------------------

    private val metadataView = new MetaDataView(trackData)
    metadataView.fillWidth = true


    metadataView.editableProperty.bind(this.editableProperty) // Can't go in construction function because of shadowing

    VBox.setVgrow(metadataView, Priority.Always)

    items.add(metadataView)

    // Index/Properties Editor -----------------------------------------------------------------------------------------

    private val indexView = new IndexTableView(trackData.getIndices.asScala) {
        editable = true
    }

    items.add(indexView)

}
