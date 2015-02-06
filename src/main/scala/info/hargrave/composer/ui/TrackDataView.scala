package info.hargrave.composer.ui

import jwbroek.cuelib.TrackData

import scalafx.beans.property.BooleanProperty
import scalafx.scene.layout.{VBox, Priority}
import scalafx.Includes._

import info.hargrave.composer._
import info.hargrave.composer.util.CUEUtilities._

/**
 * Date: 2/3/15
 * Time: 11:25 AM
 */
class TrackDataView(trackData: TrackData) extends VBox {

    val editableProperty = new BooleanProperty

    // Metadata Editor -------------------------------------------------------------------------------------------------

    val metadataView = new MetaDataView(trackData)
    metadataView.fillWidth = true


    metadataView.editableProperty.bind(this.editableProperty) // Can't go in construction function because of shadowing

    VBox.setVgrow(metadataView, Priority.Always)

    children += metadataView

    // API -------------------------------------------------------------------------------------------------------------

    def editable = editableProperty.value
    def editable_=(bool: Boolean) = editableProperty.value = bool
}
