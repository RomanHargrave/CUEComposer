package info.hargrave.composer.ui.cue.cuelib

import java.util

import info.hargrave.commons.Memoization
import info.hargrave.composer._
import jwbroek.cuelib.{CueSheet, FileData, TrackData}

import scala.collection.JavaConverters._
import scalafx.beans.property.{ObjectProperty, StringProperty}
import scalafx.collections.ObservableBuffer
import scalafx.event.subscriptions.Subscription


/**
 * Wraps a FileData object and provides observable values in place of the non-observable values in order to allow
 * for better JavaFX integration
 */
final class ObservableFileData(parent: CueSheet) extends FileData(parent) with Observability {


    val fileProperty      = StringProperty(super.getFile)
    val fileTypeProperty  = StringProperty(super.getFileType)
    val parentProperty    = ObjectProperty(super.getParent)
    val trackDataProperty = ObservableBuffer(super.getTrackData.asScala: _*)

    Set(fileProperty, fileTypeProperty, parentProperty, trackDataProperty).foreach(_.invalidatesParent())

    /**
     * Clone values from the specified FileData
     * If the FileData should become subordinate this (mutate on changes made to this instance), see [[bind()]]
     *
     * @param clone FileData to clone values from
     */
    def this(clone: FileData) = {
        this(clone.getParent)

        setFile(clone.getFile)
        setFileType(clone.getFileType)

        trackDataProperty.addAll(clone.getTrackData)
    }

    override def setFile(file: String): Unit = fileProperty.value = file

    override def setFileType(fileType: String): Unit = fileTypeProperty.value = fileType

    override def setParent(parent: CueSheet): Unit = parentProperty.value = parent

    override def getTrackData: util.List[TrackData] = trackDataProperty

    override def getFile: String = fileProperty.value

    override def getFileType: String = fileTypeProperty.value

    override def getParent: CueSheet = parentProperty.value

    /**
     * Bind the fields of a subordinate FileData to the corresponding properties in this implementation
     * and return a subscription to cancel the binding
     *
     * @param subordinate subordinate filedata
     * @return binding subscription
     */
    def bind(subordinate: FileData): Subscription = {
        import javafx.beans.binding.Bindings

        val subscriptions = Set(fileProperty.onChange { subordinate.setFile(this.getFile) },
                                fileTypeProperty.onChange { subordinate.setFileType(this.getFileType) },
                                parentProperty.onChange { subordinate.setParent(this.getParent) })

        Bindings.bindContent(subordinate.getTrackData, trackDataProperty)

        new Subscription {
            override def cancel(): Unit = {
                subscriptions.cancel()
                Bindings.unbindContent(subordinate.getTrackData, trackDataProperty)
            }
        }
    }
}

object ObservableFileData extends AnyRef with Memoization {

    val fromFileData    = memoize { dataArg: FileData => new ObservableFileData(dataArg) }
    val bindSubordinate = memoize { pair: (ObservableFileData, FileData) => pair._1.bind(pair._2) }

    def apply(sub: FileData): ObservableFileData = sub match {
        case impl: ObservableFileData => impl
        case normal: FileData         =>
            val wrapper = fromFileData(sub)
            bindSubordinate(wrapper, sub)
            wrapper
    }
}
