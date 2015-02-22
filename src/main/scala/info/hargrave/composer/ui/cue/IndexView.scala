package info.hargrave.composer.ui.cue

import com.blogspot.myjavafx.NumberSpinner
import info.hargrave.composer._
import info.hargrave.composer.ui.Editable
import cuelib._

import scalafx.scene.Node
import scalafx.scene.control.Label
import scalafx.scene.layout.HBox

/**
 * Wraps a [[PositionView]] and allows for modification of an index
 */
class IndexView(index: ObservableIndex) extends HBox with Editable {

    if(!index.getPosition.?) throw new IllegalArgumentException("Index must have a position")

    private val positionView    = new PositionView(index.getPosition)
    positionView.bindEditable()

    private val numberLabel     = new Label(t"ui.common.noun_number")

    private val numberSpinner   = new NumberSpinner(0, 99) {
        valueProperty.bindBidirectional(index.numberProperty)
    }
    numberSpinner.bindEditable()

    children = Seq[Node](numberLabel, positionView)
}
