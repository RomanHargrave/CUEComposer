package info.hargrave.composer.backend.manager

import java.io.{InputStream, OutputStream}

import scalafx.scene.Node

/**
 * Defines the idea of a project, something that is open and being worked with by the user.
 * At the most basic level, a project has a title and can be saved to the disk.
 */
abstract class Project {

    private var controllerRef: Option[ProjectController] = None

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

    def interfaceComponent: Node

    protected def onClose(): Unit = {}

    private[manager] def controller_=(projectController: Option[ProjectController]): Unit = controllerRef = projectController
    def controller = controllerRef
}
