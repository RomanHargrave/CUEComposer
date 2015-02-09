package info.hargrave.composer.ui.cue.cuelib

import java.util

import info.hargrave.commons.Memoization
import jwbroek.cuelib.{TrackData, CueSheet, Index, FileData}

import scalafx.beans.Observable
import scalafx.beans.property.{ObjectProperty, StringProperty}
import scalafx.collections.ObservableBuffer

import scala.collection.JavaConverters._
import scalafx.event.subscriptions.Subscription


/**
 * Wraps a FileData object and provides observable values in place of the non-observable values in order to allow
 * for better JavaFX integration
 */
final class ObservableFileData(parent: CueSheet) extends FileData(parent) with Observability {



    val fileProperty        = StringProperty(super.getFile)
    val fileTypeProperty    = StringProperty(super.getFileType)
    val parentProperty      = ObjectProperty(super.getParent)
    val trackData           = ObservableBuffer(super.getTrackData.asScala:_*)

    fileProperty.onChange       { invalidate() }
    fileTypeProperty.onChange   { invalidate() }
    parentProperty.onChange     { invalidate() }
    trackData.onChange          { invalidate() }

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
        trackData.addAll(clone.getTrackData)
    }

    override def setFile(file: String): Unit = fileProperty.value = file

    override def setFileType(fileType: String): Unit = fileTypeProperty.value = fileType

    override def setParent(parent: CueSheet): Unit = parentProperty.value = parent

    override def getTrackData: util.List[TrackData] = trackData

    override def getFile: String = fileProperty.value

    override def getFileType: String = fileTypeProperty.value

    override def getParent: CueSheet = parentProperty.value

    override def getAllIndices: util.List[Index] = trackData.map(track => track.getIndices.asScala).flatten.asJava

    /**
     * Bind the fields of a subordinate FileData to the corresponding properties in this implementation
     * and return a subscription to cancel the binding
     *
     * @param subordinate subordinate filedata
     * @return binding subscription
     */
    def bind(subordinate: FileData): Subscription = {
        val subscriptions = Seq(fileProperty.onChange { subordinate.setFile(this.getFile) },
                                fileTypeProperty.onChange { subordinate.setFileType(this.getFileType) },
                                trackData.onChange {
                                                       subordinate.getTrackData.clear()
                                                       subordinate.getTrackData.addAll(getTrackData)
                                                       ()
                                                   })


        new Subscription {
            override def cancel(): Unit = subscriptions.foreach(_.cancel())
        }
    }
}
object ObservableFileData extends AnyRef with Memoization {

    val fromFileData    = memoize { dataArg: FileData => new ObservableFileData(dataArg) }
    val bindSubordinate = memoize { pair: (ObservableFileData, FileData) => pair._1.bind(pair._2) }

    def apply(sub: FileData): ObservableFileData = sub match {
        case impl: ObservableFileData => impl
        case normal: FileData =>
            val wrapper = fromFileData(sub)
            bindSubordinate(wrapper, sub)
            wrapper
    }
}
