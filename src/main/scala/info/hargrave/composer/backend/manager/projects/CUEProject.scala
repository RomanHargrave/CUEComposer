package info.hargrave.composer.backend.manager.projects

import java.io.{InputStream, OutputStream}

import scala.collection.JavaConversions._

import info.hargrave.composer.backend.manager.Project
import info.hargrave.composer._

import jwbroek.cuelib.{CueSheetSerializer, CueSheet, CueParser, Error => CuesheetError, Warning => CueSheetWarning, Message => CuesheetMessage}

/**
 * Date: 1/25/15
 * Time: 12:43 PM
 */
class CUEProject extends Project {

    private var underlyingCueSheet: Option[CueSheet] = Some(new CueSheet)

    /**
     * Access the underlying cue sheet object
     * @return underlying cue sheet
     */
    final def cueSheet: Option[CueSheet] = underlyingCueSheet

    /**
     * Read the project from an input stream
     *
     * @param input input stream
     */
    final override def readProject(input: InputStream): Unit = {
        underlyingCueSheet = Some(CueParser.parse(input))
        underlyingCueSheet match {
            case someSheet: Some[CueSheet] =>
                logger.debug(s"CUESheet parsed with ${someSheet.get.getMessages.length} messages")
                someSheet.get.getMessages.foreach {
                                                       case error: CuesheetError =>
                                                           logger.error(tf"cuesheet.parse_error"(error.getMessage, error.getInput, error.getLineNumber))
                                                       case warning: CueSheetWarning =>
                                                           logger.warn(tf"cuesheet.parse_warning"(warning.getMessage, warning.getInput, warning.getLineNumber))
                                                       case message: CuesheetMessage =>
                                                           logger.info(tf"cuesheet.parse_message"(message.getMessage, message.getInput, message.getLineNumber))
                                                   }
            case None =>
                logger.debug("CUESheet parser did not like the input stream!")
                throw new IllegalArgumentException("Invalid CUE sheet.")
        }
    }

    /**
     * Name of the project
     *
     * @return project name
     */
    override def title: String = underlyingCueSheet.get.toString

    /**
     * Returns true when the project has been modified
     *
     * @return true if the project has been modified since it was last saved successfully, or if it is new.
     */
    override def isModified: Boolean = false

    /**
     * Write the project to an output stream
     *
     * @param output output stream
     */
    final override def writeProject(output: OutputStream): Unit = underlyingCueSheet match {
        case someSheet: Some[CueSheet]  =>
            val serializer = new CueSheetSerializer
            output.write(serializer.serializeCueSheet(someSheet.get).getBytes)
        case None                       => throw new IllegalStateException("No underlying CueSheet exists")
    }



}
