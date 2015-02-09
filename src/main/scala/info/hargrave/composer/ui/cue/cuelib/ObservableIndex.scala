package info.hargrave.composer.ui.cue.cuelib

import info.hargrave.commons.Memoization
import jwbroek.cuelib.{Position, Index}

import scalafx.beans.property.{ObjectProperty, IntegerProperty}
import scalafx.event.subscriptions.Subscription

/**
 * Date: 2/9/15
 * Time: 3:05 PM
 */
final class ObservableIndex extends Index with Observability {

    val numberProperty      = IntegerProperty(super.getNumber)
    val positionProperty    = ObjectProperty(super.getPosition)

    numberProperty.onChange     { invalidate() }
    positionProperty.onChange   { invalidate() }

    /**
     * Create an observable clone with specified index's fields
     *
     * @param clone Index to clone
     */
    def this(clone: Index) {
        this()

        setNumber(clone.getNumber)
        setPosition(clone.getPosition)
    }

    /**
     * Create an observable index with the specified number and position
     *
     * @param number    index number
     * @param position  position
     */
    def this(number: Int, position: Position) {
        this()

        setNumber(number)
        setPosition(position)
    }

    override def getNumber: Int = numberProperty.value

    override def setNumber(number: Int): Unit = numberProperty.value = number

    override def getPosition: Position = positionProperty.value

    override def setPosition(position: Position): Unit = positionProperty.value = position

    /**
     * Bind the fields of a subordinate index to the fields of this implementation and return a subscription to
     * cancel the binding
     *
     * @param subordinate subordinate index
     * @return binding subscription
     */
    def bind(subordinate: Index): Subscription =  {
        val subscribers = Seq(numberProperty.onChange   { subordinate.setNumber(getNumber) },
                              positionProperty.onChange { subordinate.setPosition(getPosition) })

        new Subscription {
            override def cancel(): Unit = subscribers.foreach(_.cancel())
        }
    }
}
object ObservableIndex extends AnyRef with Memoization {

    val fromIndex       = memoize { index: Index => new ObservableIndex(index) }
    val bindSubordinate = memoize { pair: (ObservableIndex, Index) => pair._1.bind(pair._2) }

    /**
     * Create an observable clone of a subordinate index and bind the subordinate index's fields to the observable clone
     *
     * @param clone index to clone
     * @return observable clone
     */
    def apply(clone: Index): ObservableIndex = clone match {
        case impl: ObservableIndex => impl
        case normal: Index =>
            val impl = fromIndex(normal)
            bindSubordinate((impl, normal))
            impl
    }
}