package info.hargrave.composer.backend.manager

import java.io.{InputStream, OutputStream}

/**
 * Defines the idea of a project, something that is open and being worked with by the user.
 * At the most basic level, a project has a title and can be saved to the disk.
 */
abstract class Project {

    /**
     * Name of the project
     *
     * @return project name
     */
    def title: String

    /**
     * Write the project to an output stream
     *
     * @param output output stream
     */
    def writeProject(output: OutputStream)

    /**
     * Read the project from an input stream
     *
     * @throws IllegalArgumentException when the input stream provides invalid data
     * @param input input stream
     */
    @throws(classOf[IllegalArgumentException])
    def readProject(input: InputStream)

    /**
     * Returns true when the project has been modified
     *
     * @return true if the project has been modified since it was last saved successfully, or if it is new.
     */
    def isModified: Boolean

}
