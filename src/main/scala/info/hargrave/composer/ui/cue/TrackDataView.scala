package info.hargrave.composer.ui.cue

import javafx.geometry.Insets
import javafx.scene.control.{CheckMenuItem => JFXCheckMenuItem}

import info.hargrave.composer._
import info.hargrave.composer.ui.Editable
import info.hargrave.composer.util.CUEUtilities._
import jwbroek.cuelib.{Position, TrackData}

import scala.collection.JavaConverters._
import scalafx.Includes._
import scalafx.collections.ObservableBuffer
import scalafx.scene.control._
import scalafx.scene.layout.{GridPane, Priority, VBox}

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

    // Right Half ------------------------------------------------------------------------------------------------------

    private val rightPane = new VBox {
        fillWidth = true
    }

    items.add(rightPane)

    // ----- Index Editor ----------------------------------------------------------------------------------------------

    private val indexView = new IndexTableView(trackData.getIndices.asScala) {
        editable    = true
        vgrow       = Priority.Always

        indices.onChange { trackData.indices = indices }
    }

    rightPane.children.add(indexView)

    // ----- Properties Editor -----------------------------------------------------------------------------------------

    private val dataTypeLabel   = new Label(t"ui.td_view.data_type")
    private val dataTypeCombo   = new ChoiceBox[String] {
        items = ObservableBuffer(TrackEntry.DataTypes)
        value = trackData.dataType.orNull
        value.onChange { trackData.setDataType(value.value) }

        editableProperty.onChange { disable = !editable }
    }

    private val flagsLabel      = new Label(t"ui.td_view.flags")
    private val flagsMenu       = new MenuButton {

        def synchronizeItems(): Unit = {
            val selectedItems   = items.filter { case(item: JFXCheckMenuItem) => item.selected.value }
            val selectedStrings = selectedItems.map(_.text.value).sorted
            setText(selectedStrings)
            trackData.flags = selectedStrings
        }

        def setText(flags: Iterable[String]): Unit = text = flags.mkString(", ")

        items = TrackEntry.Flags.map {flag => new CheckMenuItem(flag){
            onAction = () => synchronizeItems()
            selected = trackData.flags.exists(_ == flag)
        }}

        synchronizeItems()
    }

    private val pregapPosition  = new PositionView(trackData.pregap.getOrElse(new Position)) {
        minutesProperty.onChange { if(trackData.pregap.isDefined) trackData.pregap.get.minutes = Option(minutes) }
        secondsProperty.onChange { if(trackData.pregap.isDefined) trackData.pregap.get.seconds = Option(seconds) }
        framesProperty.onChange { if(trackData.pregap.isDefined) trackData.pregap.get.frames = Option(frames) }
    }
    private val pregapCheck     = new CheckBox(t"ui.td_view.pregap") {
        selected = trackData.pregap.isDefined
        selected.onChange { trackData.pregap = if(selected.value) Option(pregapPosition.value) else None }
        pregapPosition.editableProperty.bind(selected)
    }

    private val postgapPosition = new PositionView(trackData.postgap.getOrElse(new Position)) {
        minutesProperty.onChange { if(trackData.postgap.isDefined) trackData.postgap.get.minutes = Option(minutes) }
        secondsProperty.onChange { if(trackData.postgap.isDefined) trackData.postgap.get.seconds = Option(seconds) }
        framesProperty.onChange { if(trackData.postgap.isDefined) trackData.postgap.get.frames = Option(frames) }
    }
    private val postgapCheck    = new CheckBox(t"ui.td_view.postgap") {
        selected = trackData.postgap.isDefined
        selected.onChange { trackData.postgap = if(selected.value) Option(postgapPosition.value) else None }
        postgapPosition.editableProperty.bind(selected)
    }

    private val propertiesPane  = new GridPane {
        vgap = 5
        hgap = 10
        padding = new Insets(10, 10, 10, 10)

        prefHeight.bind(minHeight)

        addRow(0, dataTypeLabel, dataTypeCombo)
        addRow(1, flagsLabel, flagsMenu)
        addRow(2, pregapCheck, pregapPosition)
        addRow(3, postgapCheck, postgapPosition)
    }

    rightPane.children.add(propertiesPane)

}
