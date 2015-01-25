package info.hargrave.composer.backend.manager.ui

import info.hargrave.composer.backend.manager.{Project, ProjectController}

/**
 * Couples the [[ProjectController]] with the user interface.
 * `ProjectUI` is a UI that allows for handling multiple containers, with only one residing in the foreground at anytime.
 * This could be for instance, a TDI element like [[scalafx.scene.control.TabPane]] or an MDI element (like [[javax.swing.JDesktopPane]]).
 */
trait ProjectUserInterface {

    def projects: Iterable[Project]

    def activeProject: Option[Project]

    def addProject(project: Project)

    def closeProject(project: Project)

    def switchTo(project: Project)

}
