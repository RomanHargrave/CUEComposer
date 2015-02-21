package info.hargrave.composer.ui.cue.cuelib


import javafx.beans.{InvalidationListener, Observable => JFXObservable}

import info.hargrave.commons.Memoization

import scalafx.beans.Observable

import scala.collection.mutable.{Set => MSet}

/**
 * Provides a simple observability interface with no specific contracts
 *
 * This works by using an ObjectProperty and invalidating it with a new object allocation when the invalidate function
 * is called.
 */
trait Observability extends Observable with Memoization {

    private val subscribers = MSet[InvalidationListener]()

    final def invalidate(): Unit = subscribers.foreach(_.invalidated(this))

    /**
     * Decorates SFX observables such that calling invalidatesParent will cause them to call invalidate() on
     * @param observable implicit observable
     */
    implicit class ObservableDecorator(observable: Observable) {

        def invalidatesParent(): Unit = observable.onInvalidate { invalidate() }
    }

    override def delegate: JFXObservable = cache {
                                                     new JFXObservable {

                                                         override def removeListener(invalidationListener:
                                                                                     InvalidationListener): Unit =
                                                             subscribers -= invalidationListener

                                                         override def addListener(invalidationListener:
                                                                                  InvalidationListener): Unit =
                                                             subscribers += invalidationListener
                                                     }
                                                 }

}