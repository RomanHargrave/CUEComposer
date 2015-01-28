package info.hargrave.composer

import info.hargrave.composer.backend.manager.ProjectController
import info.hargrave.composer.backend.manager.ui.ProjectUserInterface
import info.hargrave.composer.ui.{FXPromptInterface, PromptInterface, TabbedProjectUI}
import info.hargrave.composer.util.Localization

import scalafx.application.{Platform, JFXApp}
import scalafx.application.JFXApp.PrimaryStage
import scalafx.event.EventIncludes
import scalafx.scene.Scene
import scalafx.scene.control.{SeparatorMenuItem, MenuItem, Menu, MenuBar}
import scalafx.scene.layout.BorderPane

/**
 * Main window for composer
 *
 * @author Roman Hargrave
 */
final class CUEComposer extends JFXApp with Localization with EventIncludes {

    implicit val projectUI = new TabbedProjectUI

    val controller  = new ProjectController

    val fileMenu    = new Menu(t"ui.menu.file") {
        items = List(   new MenuItem(t"ui.menu.file.new")   {
                            id = "menu.file.new"
                        },
                        new MenuItem(t"ui.menu.file.open")  {
                            id = "menu.file.open"
                            onAction = () => controller.openProjectsInteractively()
                        },
                        new MenuItem(t"ui.menu.file.save")  { id = "menu.file.save" },
                        new MenuItem(t"ui.menu.file.saveas"){ id = "menu.file.save_as" },
                        new SeparatorMenuItem,
                        new MenuItem(t"ui.menu.file.exit")  {
                            id = "menu.file.exit"
                            onAction = () => Platform.exit()
                        } )
    }

    val editMenu    = new Menu(t"ui.menu.edit") {
        items = List(   new MenuItem(t"ui.menu.edit.cut")   { id = "menu.edit.cut" },
                        new MenuItem(t"ui.menu.edit.copy")  { id = "menu.edit.copy" },
                        new MenuItem(t"ui.menu.edit.paste") { id = "menu.edit.paste" } )
    }

    val helpMenu    = new Menu(t"ui.menu.help") {
        items = List(   new MenuItem(t"ui.menu.help.about") { id = "menu.help.about" },
                        new MenuItem(t"ui.menu.help.web")   { id = "menu.help.website" } )
    }

    val menuBar     = new MenuBar {
        menus = List(fileMenu, editMenu, helpMenu)
    }

    /*
     * End component initialization
     */

    val rootPane = new BorderPane {
        top     = menuBar
        center  = projectUI
    }

    stage = new PrimaryStage {
        title       = t"composer.title"
        resizable   = true
        width       = 640
        height      = 480

        width   onChange { rootPane.prefWidth   = width.value  }
        height  onChange { rootPane.prefHeight  = height.value }

        scene = new Scene {
            content = rootPane
        }

    }

    override def stopApp(): Unit = {
        logger.info(t"log.app.stopping")
        super.stopApp()
    }

    logger.trace("Composer Initialized")
}
