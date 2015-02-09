package info.hargrave.composer.ui.cue.cuelib

import java.util

import info.hargrave.commons.Memoization
import jwbroek.cuelib.{TrackData, CueSheet, Index, FileData}

import scalafx.beans.Observable
import scalafx.beans.property.{ObjectProperty, StringProperty}
import scalafx.collections.ObservableBuffer

import scala.collection.JavaConverters._


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

    def bind(subordinate: FileData): Unit = {
        fileProperty.onChange { subordinate.setFile(this.getFile) }
        fileTypeProperty.onChange { subordinate.setFileType(this.getFileType) }
        trackData.onChange {
                               subordinate.getTrackData.clear()
                               subordinate.getTrackData.addAll(getTrackData)
                               ()
                           }
    }
}
object ObservableFileData extends AnyRef with Memoization {

    val fromFileData = memoize { dataArg: FileData => new ObservableFileData(dataArg) }

    def apply(sub: FileData): ObservableFileData = {
        val wrapper = fromFileData(sub)
        wrapper.bind(sub)
        wrapper
    }
}
