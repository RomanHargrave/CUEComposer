package info.hargrave.composer.backend.manager.projects

import java.io.{InputStream, OutputStream}

import info.hargrave.composer.backend.manager.Project
import jwbroek.cuelib.{CueSheetSerializer, CueSheet, CueParser}

/**
 * Date: 1/25/15
 * Time: 12:43 PM
 */
class CUEProject extends Project {

    private var underlyingCueSheet: Option[CueSheet] = None

    /**
     * Read the project from an input stream
     *
     * @param input input stream
     */
    final override def readProject(input: InputStream): Unit = {
        underlyingCueSheet = Some(CueParser.parse(input))
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
