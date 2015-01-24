package info.hargrave

import javafx.scene.control.Dialogs
import javafx.scene.control.Dialogs.DialogResponse

import info.hargrave.composer.util.Localization
import org.slf4j.LoggerFactory

import scalafx.application.JFXApp
import scalafx.stage.Stage

/**
 * The root package for CUEComposer
 */
package object composer extends AnyRef with Localization {

    /**
     * Universal logger
     */
    implicit val logger = LoggerFactory.getLogger("info.hargrave.composer")

    /**
    * Allow easy access to the active stage without boilerplate.
    * Implicitly defined so as to allow for implicit parameter comprehension, etc...
    *
    * @return current stage
    */
    implicit def stage: Stage = JFXApp.ACTIVE_APP.stage

    /**
     * Prompt the user with a confirmation dialog.
     *
     * @param title     window title
     * @param header    masthead
     * @param body      message body
     * @param stage     parent stage
     * @return          dialog response
     */
    def confirmationDialog(title:String, header:String, body:String)(implicit stage: Stage): DialogResponse =
        Dialogs.showConfirmDialog(stage, body, header, title)
}
