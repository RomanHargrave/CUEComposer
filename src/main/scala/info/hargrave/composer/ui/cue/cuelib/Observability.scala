package info.hargrave.composer.ui.cue.cuelib


import javafx.beans.{Observable => JFXObservable, InvalidationListener}

import info.hargrave.commons.Memoization

import scalafx.beans.Observable
import scalafx.beans.property.ObjectProperty

/**
 * Provides a simple observability interface with no specific contracts
 *
 * This works by using an ObjectProperty and invalidating it with a new object allocation when the invalidate function
 * is called.
 */
trait Observability extends Observable with Memoization {

    private val property = ObjectProperty(new Object)

    protected final def invalidate(): Unit = property.value = new Object

    /**
     * Decorates SFX observables such that calling invalidatesParent will cause them to call invalidate() on
     * @param observable implicit observable
     */
    implicit class ObservableDecorator(observable: Observable) {

        def invalidatesParent(): Unit = observable.onInvalidate { invalidate() }
    }

    def delegate: JFXObservable = cache {
                                            new JFXObservable {

                                                override def removeListener(invalidationListener: InvalidationListener): Unit =
                                                    property.delegate.removeListener(invalidationListener)

                                                override def addListener(invalidationListener: InvalidationListener): Unit =
                                                    property.delegate.addListener(invalidationListener)
                                            }
                                        }

}