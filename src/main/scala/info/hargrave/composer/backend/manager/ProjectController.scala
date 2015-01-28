package info.hargrave.composer.backend.manager

import java.io.{FileInputStream, FileOutputStream, File}

import info.hargrave.composer._
import info.hargrave.composer.backend.manager.projects.CUEProject
import info.hargrave.composer.backend.manager.ui.ProjectUserInterface
import info.hargrave.composer.ui.PromptInterface
import info.hargrave.composer.ui.PromptInterface.{PromptType, PromptResponse}
import info.hargrave.composer.util.Localization
import scala.collection.mutable.{Map => MutableMap}

/**
 * Manages a collection of projects, working with a TabPane
 */
class ProjectController(implicit val interface: ProjectUserInterface,
                        implicit val prompts:   PromptInterface) extends AnyRef with Localization /* explicit import to fix IntelliJ compiler bug */ {

    import ProjectController.ProjectDataAccess

    private implicit val projectStorage = MutableMap[Project, File]()


    /**
     * Get the active project from the interface
     *
     * @return active project
     */
    def activeProject: Option[Project] = interface.activeProject

    /**
     * Procedure for closing a project.
     * Checks if the project has been saved in its current state and prompts the user to save it if it has not been saved
     *
     * @param project project
     */
    def closeProject(project: Project): Unit = {
        logger.info(t"log.project.close", project)

        prompts.displayConfirmationPrompt(t"dialog.save_project", t"dialog.save_project.banner",
                                          t"dialog.save_project.body", cancelable = true) match {
            case PromptResponse.CONFIRM =>
                logger.info(t"log.info.project.close_confirm_save")
                try {
                    saveProject(project)
                } catch {
                    case anyThrowable: Throwable =>
                        prompts.displayNotificationPrompt(t"dialog.error", t"dialog.error_while_saving",
                                                          t"dialog.error_while_saving.body", promptType = PromptType.ERROR) match {
                            case PromptResponse.ACKNOWLEDGE => // Do nothing
                            case PromptResponse.DENY        => return
                        }
                        logger.error(t"error.project.saving", anyThrowable)
                }

                try {
                    interface.closeProject(project)
                } catch {
                    case noSuchProject: NoSuchElementException =>
                        logger.error(t"error.project.close", noSuchProject)
                }

            case PromptResponse.DENY => try {
                interface.closeProject(project)
            } catch {
                case noSuchProject: NoSuchElementException =>
                    logger.error(t"error.project.close", noSuchProject)
            }

            case PromptResponse.CLOSED | PromptResponse.CANCEL =>
                logger.info(t"log.info.project.close_confirm_dismissed")
        }


    }

    /**
     * Save a project.
     *
     * If promptForLocation is true, the user will be prompted for a location to save the file to. If a location different
     * from the project's current location, the project's stored location will be changed to that location.
     *
     * If promptForLocation is false, the project location will be looked up via [[ProjectDataAccess]]. If the project
     * has no location, it will be set to ''user.home/project.default_file_name'' (i.e. /home/user/untitled.cue)
     *
     * @param project               project to save
     * @param promptForLocation     whether to prompt for the location to save the project to or not
     */
    def saveProject(project: Project, promptForLocation: Boolean = false): Unit = {
        logger.info(t"log.project.saving", project)

        val storageLocation = project.storageLocation match {
            case some: Some[File] => some.get
            case None =>
                if(!promptForLocation)
                    logger.warn(t"warn.project.location_always_default")
                new File(p"user.home", t"project.default_file_name")
        }

        lazy val saveLocation = promptForLocation match {
            case false  => storageLocation
            case true   => askForLocation()
        }

        verifyAndSaveFile(saveLocation)

        // End of function
        // Nested functions follow
        //--------------------------------------------------------------------------------------------------------------

        def verifyAndSaveFile(saveFile: File): Unit = saveFile.exists() match {
            case true   =>
                prompts.displayNotificationPrompt(t"dialog.save_file.already_exists", t"dialog.save_file.already_exists.banner",
                                                  tf"dialog.save_file.already_exists.body"(saveFile.getAbsolutePath), PromptType.WARNING) match {
                    case PromptResponse.ACKNOWLEDGE =>
                        writeProjectToFile(saveFile)
                        project.storageLocation = Some(saveFile)
                    case PromptResponse.DENY        =>
                        val alternateSaveFile = askForLocation()
                        verifyAndSaveFile(alternateSaveFile)
                    case PromptResponse.CLOSED      =>
                        // Do nothing
                }
            case false  =>
                writeProjectToFile(saveFile)
                project.storageLocation = Some(saveFile)
        }

        def askForLocation(): File = {
            prompts.displayFileSelectionPrompt(initialFile = Some(storageLocation), title = Some(t"dialog.save_file"),
                                               filter = Some(project.extensionFilter), saveFile = true,
                                               validator = fOpt => fOpt.isDefined && !fOpt.get(0).isDirectory) match {
                case files: Some[Seq[File]] => files.get(0)
                case None =>
                    logger.warn("The user failed to select a save file when prompted (somehow?)")
                    throw new IllegalStateException("Unreachable branch: None returned by file prompt with Some-only validator")
            }
        }

        def writeProjectToFile(file: File): Unit = {
            logger.trace("Blindly attempting to create file at {}", file.getAbsolutePath)
            file.createNewFile()

            logger.debug("Opening an output stream to {}", file.getAbsolutePath)
            val output = new FileOutputStream(file)

            logger.trace("Calling project#writeProject on {}", output)
            try {
                project.writeProject(output)
                output.flush()
            }
            finally output.close()

            logger.debug("Save complete")
        }
    }

    /**
     * Attempt to find a project factory based on the file extension and then construct the project.
     * Once instantiated, call readProject on an inputStream constructed with the provided file.
     *
     * Assuming that the project does not throw an exception [[Project.readProject() for the reasons provided]], the project will be returned
     * otherwise the exception will be passed up.
     *
     * @param file file to read from
     * @return project instance
     */
    @throws(classOf[NoSuchElementException])
    def createProjectFromFile(file: File): Project = {
        val projectInstance = ProjectController.ProjectExtensionAssociations(file.getName.split("\\.").last.toLowerCase)()
        val inputStream     = new FileInputStream(file)
        logger.debug("instantiated project ({}) based on filetype", projectInstance)
        logger.trace("opened input stream {} on file {}", Seq(inputStream, file):_*)

        try projectInstance.readProject(inputStream)
        finally inputStream.close()

        projectInstance.storageLocation = Some(file)
        projectInstance
    }

    /**
     * Call [[Project.readProject()]] on a preexisting project and its storage location
     *
     * @param project project to reload
     */
    def reloadProject(project: Project): Unit = project.storageLocation match {
        case someFile: Some[File]   =>
            val fileInputStream = new FileInputStream(someFile.get)
            try project.readProject(fileInputStream) finally fileInputStream.close()
        case None                   => throw new IllegalArgumentException(t"project.no_storage_location")
    }

    /**
     * Open a file selection dialog that allows for selection of multiple files and then process the selected files
     * and open the created resulting projects
     *
     * @return opened projects
     */
    def openProjectsInteractively(): Seq[Project] = {
        prompts.displayFileSelectionPrompt(title = Some(t"dialog.open_files"), filter = Some(ProjectController.FatExtensionFilter),
                                           multipleFiles = true, validator = fOpt => fOpt.isDefined && fOpt.get.forall(_.isFile)) match {
            case someFiles: Some[Seq[File]] =>
                val files       = someFiles.get
                val projects    = files.map(createProjectFromFile)
                projects.foreach(addProject)
                projects
            case None => Seq() // Dialog was closed
        }
    }

    /**
     * Perform the necessary action to add a preexisting project to the controller
     *
     * @param project project
     */
    def addProject(project: Project): Unit = {
        logger.debug("adding project {}, storage: {}", Seq(project, project.storageLocation):_*)
        interface.addProject(project)
    }
}
object ProjectController {

    val ProjectExtensionAssociations: Map[String, (()=>Project)] = Map("cue" -> (()=> new CUEProject))
    val ProjectExtensionFilters: Map[Class[_<:Project], Map[String, Seq[String]]] =
        Map(classOf[CUEProject ] -> Map(t"project.type.cue" -> Seq("*.cue")))

    lazy val FatExtensionFilter =
        ProjectExtensionFilters.values.foldRight(Map[String, Seq[String]]()) {case(filter, fatMap) =>
                                                                                var updatedFatMap = fatMap
                                                                                filter.foreach{case(desc, exts) =>
                                                                                               updatedFatMap = fatMap.updated(desc, updatedFatMap.get(desc) match {
                                                                                                   case someSeq: Some[Seq[String]]  => someSeq.get ++ exts
                                                                                                   case None                        => exts
                                                                                               })
                                                                                              }
                                                                                updatedFatMap
                                                                             }

    implicit class ProjectDataAccess (project: Project)(implicit val locationMap: MutableMap[Project, File]) {

        def storageLocation: Option[File] = locationMap.get(project)

        def storageLocation_=(file: Option[File]): Option[File] = file match {
            case someFile: Some[File] => locationMap.put(project, file.get)
            case None =>
                locationMap.remove(project)
                None
        }


        def extensionFilter: Map[String, Seq[String]] =
            ProjectController.ProjectExtensionFilters.getOrElse(project.getClass, Map(t"project.type.unknown" -> Seq("*")))

    }
}
