package info.hargrave.composer.ui.cue.cuelib

import java.util
import javafx.collections.SetChangeListener
import javafx.collections.SetChangeListener.Change

import info.hargrave.commons.Memoization
import jwbroek.cuelib.{Position, Index, TrackData, FileData}

import scalafx.beans.property.{IntegerProperty, StringProperty, ObjectProperty}
import scalafx.collections.{ObservableSet, ObservableBuffer}

import scala.collection.JavaConverters._
import scalafx.event.subscriptions.Subscription

/**
 * Provides an observable wrapper around TrackData
 */
final class ObservableTrackData(parent: FileData) extends TrackData(parent) with Observability {

    val dataTypeProperty    = StringProperty(super.getDataType)
    val isrcCodeProperty    = StringProperty(super.getIsrcCode)
    val numberProperty      = IntegerProperty(super.getNumber)
    val performerProperty   = StringProperty(super.getPerformer)
    val postgapProperty     = ObjectProperty(super.getPostgap)
    val pregapProperty      = ObjectProperty(super.getPregap)
    val songwriterProperty  = ObjectProperty(super.getSongwriter)
    val titleProperty       = ObjectProperty(super.getTitle)
    val indicesProperty     = ObservableBuffer(super.getIndices.asScala:_*)
    val flagsProperty       = ObservableSet(super.getFlags.asScala)
    val parentProperty      = ObjectProperty(super.getParent)

    Set(dataTypeProperty, isrcCodeProperty, numberProperty, performerProperty,
        postgapProperty, pregapProperty, songwriterProperty, titleProperty,
        indicesProperty, flagsProperty, parentProperty).foreach(_.invalidatesParent())

    /**
     * Clone the passed TrackData
     *
     * @param clone TrackData to clone
     */
    def this(clone: TrackData) = {
        this(clone.getParent)

        setDataType(clone.getDataType)
        setIsrcCode(clone.getIsrcCode)
        setNumber(clone.getNumber)
        setPerformer(clone.getPerformer)
        setPostgap(clone.getPostgap)
        setPregap(clone.getPregap)
        setSongwriter(clone.getSongwriter)
        setTitle(clone.getTitle)
        setParent(clone.getParent)

        indicesProperty.addAll(clone.getIndices)
        flagsProperty.addAll(clone.getFlags)
    }

    override def getDataType: String = dataTypeProperty.value

    override def setDataType(dataType: String): Unit = dataTypeProperty.value = dataType

    override def getIsrcCode: String = isrcCodeProperty.value

    override def setIsrcCode(isrcCode: String): Unit = isrcCodeProperty.value = isrcCode

    override def getNumber: Int = numberProperty.value

    override def setNumber(number: Int): Unit = numberProperty.value = number

    override def getPerformer: String = performerProperty.value

    override def setPerformer(performer: String): Unit = performerProperty.value = performer

    override def getPostgap: Position = postgapProperty.value

    override def setPostgap(postgap: Position): Unit = postgapProperty.value = postgap

    override def getPregap: Position = pregapProperty.value

    override def setPregap(pregap: Position): Unit = pregapProperty.value = pregap

    override def getSongwriter: String = songwriterProperty.value

    override def setSongwriter(songwriter: String): Unit = songwriterProperty.value = songwriter

    override def getTitle: String = titleProperty.value

    override def setTitle(title: String): Unit = titleProperty.value = title

    override def getIndices: util.List[Index] = indicesProperty

    override def getFlags: util.Set[String] = flagsProperty

    override def getParent: FileData = parentProperty.value

    override def setParent(parent: FileData): Unit = parentProperty.value = parent

    /**
     * Bind the fields of a subordinate TrackData to this implementation
     * Returns a subscription that will cancel all change listeners
     *
     * @param subordinate subordinate TrackData object
     * @return
     */
    def bind(subordinate: TrackData): Subscription = {
        val subscriptions = Set(dataTypeProperty.onChange { subordinate.setDataType(getDataType) },
                                isrcCodeProperty.onChange { subordinate.setIsrcCode(getIsrcCode) },
                                numberProperty.onChange { subordinate.setNumber(getNumber) },
                                performerProperty.onChange { subordinate.setPerformer(getPerformer) },
                                postgapProperty.onChange { subordinate.setPostgap(getPostgap) },
                                pregapProperty.onChange { subordinate.setPregap(getPregap) },
                                songwriterProperty.onChange { subordinate.setSongwriter(getSongwriter) },
                                titleProperty.onChange { subordinate.setTitle(getTitle) },
                                indicesProperty.onChange { subordinate.getIndices.clear(); subordinate.getIndices.addAll(getIndices); () },
                                parentProperty.onChange { subordinate.setParent(getParent) })

        /*
         * JFX ObservableSet subscriptions return the listener instead of a subscription, WTF?
         */
        val flagsListener = new SetChangeListener[String] {
            override def onChanged(change: Change[_ <: String]): Unit = {
                subordinate.getFlags.clear()
                subordinate.getFlags.addAll(getFlags)
            }
        }
        flagsProperty.delegate.addListener(flagsListener)

        new Subscription {
            override def cancel(): Unit = {
                subscriptions.foreach(_.cancel())
                flagsProperty.removeListener(flagsListener)
            }
        }
    }
}

object ObservableTrackData extends AnyRef with Memoization {

    /**
     * Memoized function that creates an ObservableTrackData for a TrackData
     */
    val fromTrackData   = memoize { sub: TrackData => new ObservableTrackData(sub) }

    /**
     * Memoized function that binds a TrackData to an ObservableTrackData and returns the subscription
     */
    val bindSubordinate = memoize { pair: (ObservableTrackData, TrackData) => pair._1.bind(pair._2) }

    def apply(subordinate: TrackData): ObservableTrackData = subordinate match {
        case impl: ObservableTrackData => impl
        case normal: TrackData =>
            val impl = fromTrackData(subordinate)
            bindSubordinate((impl, subordinate))
            impl
    }
}
