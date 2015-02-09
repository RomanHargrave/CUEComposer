package info.hargrave.composer.ui.cue

import com.blogspot.myjavafx.NumberSpinner
import info.hargrave.composer._
import info.hargrave.composer.ui.Editable
import info.hargrave.composer.util.CUEUtilities._
import jwbroek.cuelib.Index

import scalafx.Includes._
import scalafx.scene.Node
import scalafx.scene.control.Label
import scalafx.scene.layout.HBox

/**
 * Wraps a [[PositionView]] and allows for modification of an index
 */
class IndexView(index: Index) extends HBox with Editable {

    private val positionView    = new PositionView(index.position.orNull)
    positionView.editableProperty.bind(editableProperty)

    private val numberLabel     = new Label(t"ui.common.noun_number")

    private val numberSpinner   = new NumberSpinner(0, 99) {
        valueProperty.onChange { index.number = Option(getValue.intValue) }
        valueProperty.value = index.number.getOrElse[Int](0)
    }
    numberSpinner.editableProperty.bind(editableProperty)

    children = Seq[Node](numberLabel, positionView)
}
