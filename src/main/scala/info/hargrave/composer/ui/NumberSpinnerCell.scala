package info.hargrave.composer.ui

import com.blogspot.myjavafx.NumberSpinner
import info.hargrave.composer.ui.NumberSpinnerCell.JFXImpl

import scalafx.Includes._

import info.hargrave.composer._

import javafx.scene.control.{TableCell => JFXTableCell}

import scalafx.beans.property.ObjectProperty
import scalafx.delegate.SFXDelegate
import scalafx.scene.control.TableCell
import scalafx.scene.input.{KeyEvent, KeyCode}
import scalafx.util.converter.NumberStringConverter

/**
 * Provides a TableCell that is edited via a NumberSpinner
 */
class NumberSpinnerCell[S](override val delegate: JFXImpl[S] = new JFXImpl[S])
        extends TableCell[S, Number](delegate) with SFXDelegate[JFXImpl[S]] {

    final def valueProperty = delegate.valueProperty

    final def value = valueProperty.value
    final def value_=(num: Number) = valueProperty.value = num

    final def lowerBound = delegate.lowerBound.value
    final def lowerBound_=(number: Number) = delegate.lowerBound.value = number

    final def upperBound = delegate.upperBound.value
    final def upperBound_=(number: Number) = delegate.upperBound.value = number

    final def stepWidth = delegate.stepWidth.value
    final def stepWidth_=(number: Number) = delegate.stepWidth.value = number

    final def stringConverter = delegate.stringConverter.value
    final def stringConverter_=(converter: NumberStringConverter) = delegate.stringConverter.value = converter

    final def spinner = delegate.spinner
}
object NumberSpinnerCell {

    final class JFXImpl[S] extends JFXTableCell[S, Number] {

        private[NumberSpinnerCell] val valueProperty: ObjectProperty[Number] = new ObjectProperty[Number]
        private[NumberSpinnerCell] var spinner: Option[NumberSpinner] = None

        private[NumberSpinnerCell] val lowerBound   = new ObjectProperty[Number]
        private[NumberSpinnerCell] val upperBound   = new ObjectProperty[Number]
        private[NumberSpinnerCell] val stepWidth    = new ObjectProperty[Number]

        private[NumberSpinnerCell] val stringConverter = ObjectProperty(new NumberStringConverter)

        override def startEdit(): Unit = {
            super.startEdit()
            if(spinner.isEmpty) {
                spinner = Some(new NumberSpinner {
                    minValueProperty.bind(lowerBound)
                    maxValueProperty.bind(upperBound)

                    setNumberStringConverter(stringConverter.value)
                    numberStringConverterProperty.bind(stringConverter)

                    setStepWidth(stepWidth.value)
                    stepWidthProperty.bind(stepWidth)

                    setOnKeyReleased({(event: KeyEvent) =>
                        event.code match {
                            case KeyCode.ENTER  =>
                                commitEdit(stringConverter.value.fromString(getText))
                            case KeyCode.ESCAPE =>
                                cancelEdit()
                            case _ => // Ignored
                        }
                    })
                })
            }

            spinner.get.valueProperty().bindBidirectional(valueProperty)

            setText(null)
            setGraphic(spinner.get)
        }

        /*
         * Unlink the behaviour of commitEdit() would suggest, cancelEdit() does not result in a call to updateItem()
         */
        override def cancelEdit(): Unit = {
            super.cancelEdit()
            setGraphic(null)
            setText(stringConverter.value.toString(valueProperty.value))
        }

        override def updateItem(item: Number, empty: Boolean): Unit = empty match {
            case true =>
                setText(null)
                setGraphic(null)
                valueProperty.setValue(null)
                spinner = None
                super.updateItem(item, empty)
            case false =>
                valueProperty.setValue(item)
                setText(stringConverter.value.toString(item))
                setGraphic(null)
                super.updateItem(item, empty)
        }
    }
}
