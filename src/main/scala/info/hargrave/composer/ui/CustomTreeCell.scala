package info.hargrave.composer.ui

import javafx.scene.control.{TreeCell => JFXTreeCell}

import scalafx.scene.control.TreeCell

/**
 * Provides a CustomTreeCell that allows for implementation of the UpdateItem method
 */
abstract class CustomTreeCell[T] extends TreeCell[T] {

    self =>

    override val delegate = new JFXTreeCell[T] {
        override def updateItem(item: T, empty: Boolean): Unit = {
            self.updateItem(item, empty)
            super.updateItem(item, empty)
        }



        override def toString: String = s"[CustomTreeCell] ${super.toString}"
    }

    def updateItem(item: T, empty: Boolean): Unit
}