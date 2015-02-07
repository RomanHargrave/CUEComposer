package info.hargrave.composer.ui

import com.blogspot.myjavafx.NumberSpinner
import info.hargrave.composer._
import info.hargrave.composer.util.CUEUtilities._
import jwbroek.cuelib.Position

import scalafx.Includes._
import scalafx.beans.property.{ObjectProperty, BooleanProperty}
import scalafx.scene.Node
import scalafx.scene.control.Label
import scalafx.scene.layout.HBox
import scalafx.util.converter.NumberStringConverter

/**
 * Allows for the display (and editing) of a  [[Position]]
 */
class PositionView(position: Position) extends HBox with Editable {

    private val lengthLabel = new Label(t"ui.common.noun_minutes")
    private val separator = new Label(":")

    val converterProperty = ObjectProperty(new NumberStringConverter)

    private val minuteSpinner = new NumberSpinner(0, 99) {
        valueProperty.onChange {
                                   position.minutes = Option(converter.fromString(getText).intValue)
                               }
        valueProperty.value = position.minutes.getOrElse[Int](0)
    }
    private val secondSpinner = new NumberSpinner(0, 99) {
        valueProperty.onChange {
                                   position.seconds = Option(converter.fromString(getText).intValue)
                               }
        valueProperty.value = position.seconds.getOrElse[Int](0)
    }
    private val frameSpinner = new NumberSpinner(0, 75) {
        valueProperty.onChange {
                                   position.frames = Option(converter.fromString(getText).intValue)
                               }
        valueProperty.value = position.seconds.getOrElse[Int](0)
    }

    minuteSpinner.editableProperty.bind(editableProperty)
    secondSpinner.editableProperty.bind(editableProperty)
    frameSpinner.editableProperty.bind(editableProperty)
    minuteSpinner.numberStringConverterProperty.bind(converterProperty)
    secondSpinner.numberStringConverterProperty.bind(converterProperty)
    frameSpinner.numberStringConverterProperty.bind(converterProperty)

    final def converter = converterProperty.value
    final def converter_=(newValue: NumberStringConverter) = converterProperty.value = newValue

    children = Seq[Node](minuteSpinner, secondSpinner, frameSpinner)
}