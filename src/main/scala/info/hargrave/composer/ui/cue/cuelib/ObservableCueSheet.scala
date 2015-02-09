
package info.hargrave.composer.ui.cue.cuelib

import java.util

import info.hargrave.commons.Memoization
import jwbroek.cuelib.{FileData, CueSheet}

import scalafx.beans.property.{IntegerProperty, StringProperty}
import scalafx.collections.ObservableBuffer
import scalafx.event.subscriptions.Subscription

import scala.collection.JavaConverters._

/**
 * Observable CUESheet implementation for JFX compatibility
 */
class ObservableCueSheet extends CueSheet with Observability {

    val catalogProperty     = StringProperty(super.getCatalog)
    val cdtFileProperty     = StringProperty(super.getCdTextFile)
    val performerProperty   = StringProperty(super.getPerformer)
    val songwriterProperty  = StringProperty(super.getSongwriter)
    val titleProperty       = StringProperty(super.getTitle)
    val discIdProperty      = StringProperty(super.getDiscid)
    val genreProperty       = StringProperty(super.getGenre)
    val yearProperty        = IntegerProperty(super.getYear)
    val commentProperty     = StringProperty(super.getComment)
    val fileDataProperty    = ObservableBuffer(super.getFileData.asScala:_*)

    Set(catalogProperty, cdtFileProperty, performerProperty,
        songwriterProperty, titleProperty, discIdProperty, genreProperty,
        yearProperty, commentProperty, fileDataProperty).foreach(_.invalidatesParent())

    /**
     * Create an observable clone from the fields of another cuesheet
     *
     * @param clone CUESheet to clone fields from
     */
    def this(clone: CueSheet) {
        this()

        setCatalog(clone.getCatalog)
        setCdTextFile(clone.getCdTextFile)
        setPerformer(clone.getPerformer)
        setSongwriter(clone.getSongwriter)
        setTitle(clone.getTitle)
        setDiscid(clone.getDiscid)
        setGenre(clone.getGenre)
        setYear(clone.getYear)
        setComment(clone.getComment)

        getFileData.clear()
        getFileData.addAll(clone.getFileData)
    }

    override def getCatalog: String = catalogProperty.value

    override def setCatalog(catalog: String): Unit = catalogProperty.value = catalog

    override def getCdTextFile: String = cdtFileProperty.value

    override def setCdTextFile(cdTextFile: String): Unit = cdtFileProperty.value = cdTextFile

    override def getPerformer: String = performerProperty.value

    override def setPerformer(performer: String): Unit = performerProperty.value = performer

    override def getSongwriter: String = songwriterProperty.value

    override def setSongwriter(songwriter: String): Unit = songwriterProperty.value = songwriter

    override def getTitle: String = titleProperty.value

    override def setTitle(title: String): Unit = titleProperty.value = title

    override def getDiscid: String = discIdProperty.value

    override def setDiscid(discid: String): Unit = discIdProperty.value = discid

    override def getGenre: String = genreProperty.value

    override def setGenre(genre: String): Unit = genreProperty.value = genre

    override def getYear: Int = yearProperty.value

    override def setYear(year: Int): Unit = yearProperty.value = year

    override def getComment: String = commentProperty.value

    override def setComment(comment: String): Unit = commentProperty.value = comment

    override def getFileData: util.List[FileData] = fileDataProperty

    /**
     * Bind a subordinate cuesheet to this implementation and return a subscription to cancel the binding
     *
     * @param subordinate subordinate cuesheet
     * @return binding subscription
     */
    def bind(subordinate: CueSheet): Subscription = {
        val subscriptions   = Set(catalogProperty.onInvalidate { subordinate.setCatalog(getCatalog) },
                                  cdtFileProperty.onInvalidate { subordinate.setCdTextFile(getCdTextFile) },
                                  performerProperty.onInvalidate { subordinate.setPerformer(getPerformer) },
                                  songwriterProperty.onInvalidate { subordinate.setSongwriter(getSongwriter) },
                                  titleProperty.onInvalidate { subordinate.setTitle(getTitle) },
                                  discIdProperty.onInvalidate { subordinate.setDiscid(getDiscid) },
                                  genreProperty.onInvalidate {subordinate.setGenre(getGenre) },
                                  yearProperty.onInvalidate { subordinate.setYear(getYear) },
                                  commentProperty.onInvalidate { subordinate.setComment(getComment)},
                                  fileDataProperty.onInvalidate { subordinate.getFileData.clear(); subordinate.getFileData.addAll(getFileData); () })

        new Subscription {
            override def cancel(): Unit = subscriptions.foreach(_.cancel())
        }
    }
}
object ObservableCueSheet extends AnyRef with Memoization {

    val fromCueSheet    = memoize { sheet: CueSheet => new ObservableCueSheet(sheet) }
    val bindSubordinate = memoize { pair: (ObservableCueSheet, CueSheet) => pair._1.bind(pair._2) }

    /**
     * Create an observable clone from a subordinate sheet and bind the subordinate to the observable clone
     *
     * @param clone subordinate cuesheet
     * @return observable cuesheet
     */
    def apply(clone: CueSheet): ObservableCueSheet = clone match {
        case impl: ObservableCueSheet => impl
        case normal: CueSheet =>
            val impl = fromCueSheet(normal)
            bindSubordinate((impl, clone))
            impl
    }
}