package info.hargrave.composer.ui.cue.cuelib

import info.hargrave.commons.Memoization
import jwbroek.cuelib.Position

import scalafx.beans.property.IntegerProperty
import scalafx.event.subscriptions.Subscription

/**
 * A position implementation that provides observable property fields to increase JFX application compatibility on top of CUELib
 */
final class ObservablePosition extends Position with Observability {

    val minutesProperty = IntegerProperty(super.getMinutes)
    val secondsProperty = IntegerProperty(super.getSeconds)
    val framesProperty  = IntegerProperty(super.getFrames)

    Set(minutesProperty, secondsProperty, framesProperty).foreach(_.invalidatesParent())

    /**
     * Create an observable clone using a position's fields
     *
     * @param clone position to clone
     */
    def this(clone: Position) = {
        this()

        setMinutes(clone.getMinutes)
        setSeconds(clone.getSeconds)
        setFrames(clone.getFrames)
    }

    /**
     * Create an observable position with the specified values
     * @param minutes   minutes, redbook dictates that this should not exceed 99
     * @param seconds   seconds, redbook dictates that this should not exceed 99
     * @param frames    frames, redbook dictates that this should not exceed 75
     */
    def this(minutes: Int, seconds: Int, frames: Int) = {
        this()

        setMinutes(minutes)
        setSeconds(seconds)
        setFrames(frames)
    }

    override def getFrames: Int = framesProperty.value

    override def setFrames(frames: Int): Unit = framesProperty.value= frames

    override def getMinutes: Int = minutesProperty.value

    override def setMinutes(minutes: Int): Unit = minutesProperty.value = minutes

    override def getSeconds: Int = secondsProperty.value

    override def setSeconds(seconds: Int): Unit = secondsProperty.value = seconds

    /**
     * Bind a subordinate position's fields to this implementation's fields and return a subscription to cancel
     * the binding
     *
     * @param subordinate subordinate position
     * @return binding subscription
     */
    def bind(subordinate: Position): Subscription = {
        val subscriptions = Set(minutesProperty.onChange { subordinate.setMinutes(getMinutes) },
                                secondsProperty.onChange { subordinate.setSeconds(getSeconds) },
                                framesProperty.onChange  { subordinate.setFrames(getFrames) })

        new Subscription {
            override def cancel(): Unit = subscriptions.foreach(_.cancel())
        }
    }
}
object ObservablePosition extends AnyRef with Memoization {

    val fromPosition    = memoize { position: Position => new ObservablePosition(position) }
    val bindSubordinate = memoize { pair: (ObservablePosition, Position) => pair._1.bind(pair._2) }

    /**
     * Create an observable clone from a subordinate position and bind the subordinate position to the
     * observable clone
     *
     * @param clone subordinate position
     * @return observable position
     */
    def apply(clone: Position): ObservablePosition = clone match {
        case impl: ObservablePosition => impl
        case normal: Position =>
            val impl = fromPosition(clone)
            bindSubordinate((impl, normal))
            impl
    }
}