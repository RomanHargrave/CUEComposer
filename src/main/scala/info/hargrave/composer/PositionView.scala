package info.hargrave.composer

import com.blogspot.myjavafx.NumberSpinner
import jwbroek.cuelib.Position

import scalafx.beans.property.BooleanProperty
import scalafx.collections.ObservableBuffer
import scalafx.scene.Node
import scalafx.scene.control.Label
import scalafx.scene.layout.{HBox, GridPane}
import scalafx.Includes._

import info.hargrave.composer._
import info.hargrave.composer.util.CUEUtilities._

/**
 * Allows for the display (and editing) of a  [[Position]]
 */
class PositionView(position: Position) extends HBox {

    val editableProperty = new BooleanProperty()

    private val lengthLabel     = new Label(t"ui.common.noun_minutes")
    private val separator       = new Label(":")

    private val minuteSpinner   = new NumberSpinner(0, 99) {
        valueProperty.onChange { position.minutes = Option(getValue.intValue) }
        valueProperty.set(position.minutes.getOrElse(0))
    }
    private val secondSpinner   = new NumberSpinner(0, 99) {
        valueProperty.onChange { position.seconds = Option(getValue.intValue) }
        valueProperty.set(position.seconds.getOrElse(0))
    }
    private val frameSpinner    = new NumberSpinner(0, 75) {
        valueProperty.onChange { position.frames = Option(getValue.intValue) }
        valueProperty.set(position.seconds.getOrElse(0))
    }

    minuteSpinner.editableProperty.bind(editableProperty)
    secondSpinner.editableProperty.bind(editableProperty)
    frameSpinner.editableProperty.bind(editableProperty)

    children = Seq[Node](lengthLabel,
                         minuteSpinner, separator,
                         secondSpinner, separator,
                         frameSpinner)

    def editable = editableProperty.value
    def editable_=(bool: Boolean) = editableProperty.value = bool
}
