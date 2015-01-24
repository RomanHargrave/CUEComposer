package info.hargrave.composer.ui

import java.io.File
import javafx.scene.control.Dialogs
import javafx.scene.control.Dialogs.DialogResponse
import javafx.stage.Stage

import info.hargrave.composer.ui.PromptInterface.{PromptResponse, PromptType}
import info.hargrave.composer.util.Localization

import scalafx.stage.FileChooser
import scalafx.stage.FileChooser.ExtensionFilter

/**
 * Implements [[PromptInterface]] for JavaFX using [[javafx.scene.control.Dialogs]]
 * [[javafx.scene.control.Dialogs]] is a provided by a third-party dialog toolkit for JavaFX 2.0 that aims to mimic
 * the Dialog functionality provided in JavaFX 8.
 */
object FXPromptInterface extends AnyRef with Localization {

    implicit def dialogResponse2PromptResponse(response: DialogResponse): PromptResponse = response match {
        case DialogResponse.OK      => PromptResponse.ACKNOWLEDGE
        case DialogResponse.YES     => PromptResponse.CONFIRM
        case DialogResponse.NO      => PromptResponse.DENY
        case DialogResponse.CANCEL  => PromptResponse.CANCEL
        case DialogResponse.CLOSED  => PromptResponse.CLOSED
    }

    implicit def promptResponse2string(response: PromptResponse): String = response match {
        case PromptResponse.ACKNOWLEDGE     => t"prompt.acknowledge"
        case PromptResponse.CONFIRM         => t"prompt.confirm"
        case PromptResponse.DENY            => t"prompt.deny"
        case PromptResponse.CANCEL          => t"prompt.cancel"
        case PromptResponse.CLOSED          => t"prompt.closed"
    }

    implicit def promptType2DialogFunction(promptType: PromptType): ((Stage, String, String, String) => DialogResponse) = promptType match {
        case PromptType.ERROR   => Dialogs.showErrorDialog
        case PromptType.INFO    => (owner, message, masthead, title) => {
            Dialogs.showInformationDialog(owner, message, masthead, title) // Why does this return nothing?
            DialogResponse.OK
        }
        case PromptType.WARNING => Dialogs.showWarningDialog
    }
}
final class FXPromptInterface extends PromptInterface {
    import info.hargrave.composer.stage
    import info.hargrave.composer.ui.FXPromptInterface._

    /**
     * Display a confirmation prompt, offering the user two-to-three options (Confirm, Deny, Cancel)
     *
     * @param title         prompt title
     * @param banner        banner text of the prompt (if the implementation supports it). should disambiguate the title.
     * @param body          body text of the prompt
     * @param cancelable    whether or not the user should be allowed to exit the dialog without selecting an explicit action
     * @return user response
     */
    override def displayConfirmationPrompt(title: String, banner: String, body: String, cancelable: Boolean): PromptResponse = {
        Dialogs.showConfirmDialog(stage, body, banner, title)
    }

    /**
     * Display a prompt, offering only an action to acknowledge the prompt.
     *
     * @param title         prompt title
     * @param banner        banner text (if available). should disambiguate the title.
     * @param body          body text of the prompt
     * @param promptType    [[PromptType prompt type]] to be shown to the user
     * @return user response
     */
    override def displayNotificationPrompt(title: String, banner: String, body: String, promptType: PromptType): PromptResponse = {
        promptType(stage, body, banner, title)
    }

    /**
     * Display a file selection prompt, with optional title and filters.
     *
     * @param initialFile   initial file, None by default
     * @param wTitle        window title, None by default
     * @param filter        filters, None by default. format of map is {"description" -> Traversable("*.mask, mask.*, etc...")}
     * @param multipleFiles whether to allow multiple files or not. this is false by default
     * @return If no file is selected, None, otherwise, Some[Traversable(File...)]
     */
    override def displayFileSelectionPrompt(initialFile: Option[File] = None, wTitle: Option[String] = None,
                                            filter: Option[Map[String, Seq[String]]] = None, multipleFiles: Boolean = false): Option[Seq[File]] = {
        import scala.collection.JavaConversions.asJavaCollection

        val chooser = new FileChooser {

            if(initialFile.isDefined) {
                initialDirectory = initialFile.get.getParentFile
                if(initialFile.get.isFile)
                    initialFileName = initialFile.get.getName
            }

            if(wTitle.isDefined) {
                title = wTitle.get
            }

            if(filter.isDefined) {
                val mappedFilters = filter.get.map { case(description, extensions) => new ExtensionFilter(description, extensions).delegate }
                extensionFilters.setAll(mappedFilters)
            }
        }

        val result = multipleFiles match {
            case true   =>
                chooser.showOpenMultipleDialog(null)
            case false  =>
                val selected = chooser.showOpenDialog(null)
                if(selected == null) Seq(selected) else null
        }

        Option(result)
    }

}
