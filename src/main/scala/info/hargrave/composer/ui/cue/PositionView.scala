package info.hargrave.composer.ui.cue

import com.blogspot.myjavafx.NumberSpinner
import info.hargrave.composer.ui.Editable
import info.hargrave.composer.ui.cue.cuelib.{Observability, ObservablePosition}
import jwbroek.cuelib.Position

import scalafx.Includes._
import scalafx.beans.property.ObjectProperty
import scalafx.scene.Node
import scalafx.scene.control.Label
import scalafx.scene.layout.HBox
import scalafx.util.converter.NumberStringConverter

/**
 * Allows for the display of a  [[Position]]
 * In an editing situation, it will not directly modify the position.
 */
class PositionView(val underlying: ObservablePosition) extends HBox with Editable {

    def this(minutes: Int, seconds: Int, frames: Int) = {
        this(new Position(minutes, seconds, frames))
    }

    def minutesProperty = underlying.minutesProperty
    def secondsProperty = underlying.secondsProperty
    def framesProperty  = underlying.framesProperty

    private val minuteSeparator = new Label(":")
    private val secondSeparator = new Label(":")

    val converterProperty = ObjectProperty(new NumberStringConverter)

    private val minuteSpinner = new NumberSpinner(0, 99) {
        valueProperty.bindBidirectional(minutesProperty)
    }
    minuteSpinner.bindEditable()
    private val secondSpinner = new NumberSpinner(0, 99) {
        valueProperty.bindBidirectional(secondsProperty)
    }
    secondSpinner.bindEditable()
    private val frameSpinner = new NumberSpinner(0, 75) {
        valueProperty.bindBidirectional(framesProperty)
    }
    frameSpinner.bindEditable()

    minuteSpinner.numberStringConverterProperty.bind(converterProperty)
    secondSpinner.numberStringConverterProperty.bind(converterProperty)
    frameSpinner.numberStringConverterProperty.bind(converterProperty)

    minuteSpinner.prefWidthProperty.bind(minWidth / 3)
    secondSpinner.prefWidthProperty.bind(minWidth / 3)
    frameSpinner.prefWidthProperty.bind(minWidth / 3)

    minWidth = 200

    final def converter = converterProperty.value

    final def converter_=(newValue: NumberStringConverter) = converterProperty.value = newValue

    children = Seq[Node](minuteSpinner, minuteSeparator, secondSpinner, secondSeparator, frameSpinner)

    final def minutes = minutesProperty.value

    final def minutes_=(int: Int) = minutesProperty.value = int

    final def seconds = secondsProperty.value

    final def seconds_=(int: Int) = secondsProperty.value = int

    final def frames = framesProperty.value

    final def frames_=(int: Int) = framesProperty.value = int

}
object PositionView {

    /**
     * Create a copy of `position` instead of using that position as the underlying position
     * This is used in scenarios such as table-cell editing, where the view must be edited without side-effects
     *
     * @param position position to copy from
     */
    def copyFrom(position: Position): PositionView = new PositionView(position.getMinutes, position.getSeconds, position.getFrames)
}