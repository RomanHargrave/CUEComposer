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
 * Allows for the display of a  [[Position]]
 * In an editing situation, it will not directly modify the position.
 */
class PositionView private() extends HBox with Editable {

    val minutesProperty = new ObjectProperty[Number]
    val secondsProperty = new ObjectProperty[Number]
    val framesProperty  = new ObjectProperty[Number]

    def this(minutes: Int, seconds: Int, frames: Int) = {
        this()
        this.minutes = minutes
        this.seconds = seconds
        this.frames  = frames
    }

    def this(position: Position) = {
        this(position.minutes.getOrElse(0), position.seconds.getOrElse(0), position.frames.getOrElse(0))
    }

    private val minuteSeparator = new Label(":")
    private val secondSeparator = new Label(":")

    val converterProperty = ObjectProperty(new NumberStringConverter)

    private val minuteSpinner = new NumberSpinner(0, 99) {
        valueProperty.bind(minutesProperty)
    }
    private val secondSpinner = new NumberSpinner(0, 99) {
        valueProperty.bind(secondsProperty)
    }
    private val frameSpinner = new NumberSpinner(0, 75) {
        valueProperty.bind(framesProperty)
    }

    minuteSpinner.editableProperty.bind(editableProperty)
    secondSpinner.editableProperty.bind(editableProperty)
    frameSpinner.editableProperty.bind(editableProperty)

    minuteSpinner.numberStringConverterProperty.bind(converterProperty)
    secondSpinner.numberStringConverterProperty.bind(converterProperty)
    frameSpinner.numberStringConverterProperty.bind(converterProperty)

    minuteSpinner.prefWidthProperty.bind(minWidth / 3)
    secondSpinner.prefWidthProperty.bind(minWidth / 3)
    frameSpinner.prefWidthProperty.bind(minWidth / 3)

    minWidth = 200

    final def converter = converterProperty.value
    final def converter_=(newValue: NumberStringConverter) = converterProperty.value = newValue

    children = Seq[Node](minuteSpinner, minuteSeparator,
                         secondSpinner, secondSeparator,
                         frameSpinner)

    final def minutes = converterProperty.value.fromString(Option(minuteSpinner.getText).getOrElse("0")).intValue
    final def minutes_=(int: Int) = minutesProperty.value = int

    final def seconds = converterProperty.value.fromString(Option(secondSpinner.getText).getOrElse("0")).intValue
    final def seconds_=(int: Int) = secondsProperty.value = int

    final def frames = converterProperty.value.fromString(Option(frameSpinner.getText).getOrElse("0")).intValue
    final def frames_=(int: Int) = framesProperty.value = int

    final def value = new Position(minutes, seconds, frames)
    final def value_=(pos: Position): Unit = {
        minutes = pos.minutes.getOrElse(0)
        seconds = pos.seconds.getOrElse(0)
        frames  = pos.frames.getOrElse(0)
    }
}