package info.hargrave.composer.ui.cue.cuelib

import scalafx.beans.property.ObjectProperty
import scalafx.event.subscriptions.Subscription

/**
 * Provides a simple observability interface with no specific contracts
 *
 * This works by using an ObjectProperty and invalidating it with a new object allocation when the invalidate function
 * is called.
 */
trait Observability {

    private val delegate = ObjectProperty(new Object)

    protected final def invalidate(): Unit = delegate.value = new Object

    final def onChange(op: => Any): Subscription = delegate.onChange { op; () }
}
