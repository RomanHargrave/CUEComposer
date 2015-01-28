package info.hargrave.composer.backend.manager.projects

import java.io.OutputStream

import info.hargrave.composer.backend.manager.Project

import scalafx.scene.Node

/**
 * Date: 1/25/15
 * Time: 12:43 PM
 */
class CUEProject extends Project {

    /**
     * Read the project from an input stream
     *
     * @param input input stream
     */
    override def readProject(input: _root_.java.io.InputStream): Unit = ???

    /**
     * Name of the project
     *
     * @return project name
     */
    override def title: String = ???

    /**
     * Returns true when the project has been modified
     *
     * @return true if the project has been modified since it was last saved successfully, or if it is new.
     */
    override def isModified: Boolean = ???

    /**
     * Write the project to an output stream
     *
     * @param output output stream
     */
    override def writeProject(output: OutputStream): Unit = ???

}
