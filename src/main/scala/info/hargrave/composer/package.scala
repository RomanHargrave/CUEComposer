package info.hargrave

import info.hargrave.composer.ui.{FXPromptInterface, PromptInterface}
import info.hargrave.composer.util.{RegexStrings, PropertyStrings, Localization}
import grizzled.slf4j.Logger

import scalafx.application.JFXApp
import scalafx.stage.Stage

/**
 * The root package for CUEComposer
 */
package object composer extends AnyRef with Localization with PropertyStrings with RegexStrings {

    import scalafx.event.subscriptions.Subscription

    /**
     * Provides a general purpose prompts access
     */
    object Prompts extends FXPromptInterface

    /**
     * Provides the default logger to use
     */
    implicit val logger: Logger = Logger("info.hargrave.composer")

    /**
     * Provides the implementation of [[PromptInterface]] to user
     */
    implicit val promptInterface: PromptInterface = new FXPromptInterface

    /**
    * Allow easy access to the active stage without boilerplate.
    * Implicitly defined so as to allow for implicit parameter comprehension, etc...
    *
    * @return current stage
    */
    implicit def stage: Stage = JFXApp.ACTIVE_APP.stage

    /**
     * Turns any traversable containing Subscriptions in to a multiplexed subscription.
     * Because I have too much boilerplate subscription multiplexing.
     *
     * @param subs  subscriptions
     * @return      multiplexed subscription
     */
    implicit def multiplexSubscription(subs: Traversable[Subscription]): Subscription = new Subscription {
        override def cancel(): Unit = subs.foreach(_.cancel())
    }

    implicit class UnaryDecorations(something: AnyRef) {
        def ? = something != null
    }
}
