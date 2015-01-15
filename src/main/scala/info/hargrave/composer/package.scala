package info.hargrave

import javafx.scene.control.Dialogs
import javafx.scene.control.Dialogs.DialogResponse
import javafx.{stage => jfxs}

import scalafx.application.JFXApp
import scalafx.stage.Stage

/**
 * The root package for CUEComposer
 */
package object composer {

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
