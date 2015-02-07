package info.hargrave.composer.ui

import com.blogspot.myjavafx.NumberSpinner
import info.hargrave.composer._
import info.hargrave.composer.util.CUEUtilities._
import jwbroek.cuelib.Position

import scalafx.Includes._
import scalafx.beans.property.BooleanProperty
import scalafx.scene.Node
import scalafx.scene.control.Label
import scalafx.scene.layout.HBox

/**
 * Allows for the display (and editing) of a  [[Position]]
 */
class PositionView(position: Position) extends HBox with Editable {

    private val lengthLabel = new Label(t"ui.common.noun_minutes")
    private val separator = new Label(":")

    private val minuteSpinner = new NumberSpinner(0, 99) {
        valueProperty.onChange {
                                   position.minutes = Option(getValue.intValue)
                               }
        valueProperty.value = position.minutes.getOrElse[Int](0)
    }
    private val secondSpinner = new NumberSpinner(0, 99) {
        valueProperty.onChange {
                                   position.seconds = Option(getValue.intValue)
                               }
        valueProperty.value = position.seconds.getOrElse[Int](0)
    }
    private val frameSpinner = new NumberSpinner(0, 75) {
        valueProperty.onChange {
                                   position.frames = Option(getValue.intValue)
                               }
        valueProperty.value = position.seconds.getOrElse[Int](0)
    }

    minuteSpinner.editableProperty.bind(editableProperty)
    secondSpinner.editableProperty.bind(editableProperty)
    frameSpinner.editableProperty.bind(editableProperty)

    children = Seq[Node](minuteSpinner, secondSpinner, frameSpinner)

}