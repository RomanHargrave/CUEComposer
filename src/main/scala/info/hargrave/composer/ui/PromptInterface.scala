package info.hargrave.composer.ui

import info.hargrave.composer.ui.PromptInterface.{PromptResponse, PromptType}

/**
 * Interface that may be used to display prompts (such as a confirmation or a warning) to the user.
 */
trait PromptInterface {

    /**
     * Display a confirmation prompt, offering the user two-to-three options (Confirm, Deny, Cancel)
     *
     * @param title         prompt title
     * @param banner        banner text of the prompt (if the implementation supports it). should disambiguate the title.
     * @param body          body text of the prompt
     * @param cancelable    whether or not the user should be allowed to exit the dialog without selecting an explicit action
     * @return user response
     */
    def displayConfirmationPrompt(title: String, banner: String, body: String, cancelable: Boolean = false): PromptResponse

    /**
     * Display a prompt, offering only an action to acknowledge the prompt.
     *
     * @param title         prompt title
     * @param banner        banner text (if available). should disambiguate the title.
     * @param body          body text of the prompt
     * @param promptType    [[PromptType prompt type]] to be shown to the user
     * @return user response
     */
    def displayNotificationPrompt(title: String, banner: String, body: String, promptType: PromptType = PromptType.INFO): PromptResponse

}

object PromptInterface {

    /**
     * Denotes prompt type, where
     * - [[PromptType.INFO]] should correlate to a prompt displaying information
     * - [[PromptType.WARNING]] should correlate to a prompt displaying semi-critical information, or a potential for error
     * - [[PromptType.ERROR]] should correlate to a prompt display critical information, or imminent potential for error
     *
     * @param ordinal ordinal number for prompt type.
     */
    final case class PromptType private (ordinal: Int)
    object PromptType {

        /**
         * Prompt type applicable when noncritical information is to be displayed to the user
         */
        val INFO    = new PromptType(0)

        /**
         * Prompt type applicable when semi-critical information is to be displayed to the user
         */
        val WARNING = new PromptType(1)

        /**
         * Prompt type applicable when serious or critical information is to be displayed to the user
         */
        val ERROR   = new PromptType(3)

        /**
         * Pseudo-enumeration values collection
         */
        val values  = Seq(INFO, WARNING, ERROR).sortBy(_.ordinal)
    }

    /**
     * Denotes a user response, where
     * - [[PromptResponse.ACKNOWLEDGE]] indicates that the user has acknowledged the prompt
     * - [[PromptResponse.CONFIRM]] indicates that the user has acknowledged the dialog and wishes to confirm the prompt
     * - [[PromptResponse.DENY]] indicates that the user has acknowledged and wishes not to confirm the prompt (and still continue)
     * - [[PromptResponse.CANCEL]] indicates that the user has dismissed the prompt
     * - [[PromptResponse.CLOSED]] indicates that the dialog was closed without any explicit choice made by the user
     *
     * @param ordinal ordinal number for prompt type
     */
    final case class PromptResponse private (ordinal: Int)
    object PromptResponse {

        /**
         * Response type indicating acknowledgement of the prompt
         */
        val ACKNOWLEDGE     = new PromptResponse(0)

        /**
         * Response indicating that the user wishes to perform the action indicated by the prompt
         */
        val CONFIRM         = new PromptResponse(1)

        /**
         * Response type indicating acknowledgement of the prompt, and the the user wishes *not* to perform the indicated action
         */
        val DENY            = new PromptResponse(2)

        /**
         * Response type indicating dismissal of the prompt
         */
        val CANCEL          = new PromptResponse(3)

        /**
         * Response type indicated that the dialog was closed, with no definite selection
         */
        val CLOSED          = new PromptResponse(4)

        /**
         * Pseudo-enumeration values collection
         */
        val values  = Seq(ACKNOWLEDGE, CONFIRM, DENY, CANCEL, CLOSED).sortBy(_.ordinal)
    }
}
