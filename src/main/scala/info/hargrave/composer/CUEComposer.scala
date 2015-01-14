package info.hargrave.composer

import info.hargrave.composer.util.Localization

import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage

/**
 * Main window for composer
 *
 * @author Roman Hargrave
 */
class CUEComposer extends JFXApp with Localization {
    stage = new PrimaryStage {
        title = t"composer.title"
    }
}
