package info.hargrave.composer

/**
 * Launches the composer application.
 *
 * Meant for use during development.
 *
 * @author Roman Hargrave
 */
object ComposerLauncher {

    def main(args: Array[String]): Unit = {
        (new CUEComposer).main(args)
    }

}
