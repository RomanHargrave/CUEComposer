package info.hargrave.composer.util

import info.hargrave.composer._

import scala.collection.JavaConversions._

import jwbroek.cuelib._
import CueSheet.{MetaDataField => md}

import scala.collection.mutable.{Seq => MutableSeq}

sealed trait CUEUtilities {

    private implicit def optStr2OptInt(optStr: Option[String]): Option[Int] = optStr match {
        case Some(str)  => Some(str.toInt)
        case None       => None
    }

    private implicit def optInt2OptStr(optInt: Option[Int]): Option[String] = optInt match {
        case Some(num)  => Some(num.toString)
        case None       => None
    }

    /**
     * Defines the type of a key to retrieve the undlerying metadata access
     */
    type MetaDataName   = Symbol

    /**
     * Defines the type of the underlying metadata implementation
     */
    type MetaDataOrd    = md

    /**
     * Defines a tuple containing an accessor for a data value at position 1, and a mutator at position 2
     */
    type MetaDataAccess = (()=>Option[String], Option[String]=>Unit)

    /**
     * Defines an association between a metadata key and functions to mutate that data
     */
    type MetaData = Map[MetaDataName, MetaDataAccess]

    /**
     * Defines an individual unit wherein a MetaDataName and Access can be found
     */
    type MetaDataAssociation = (MetaDataName, MetaDataAccess)

    /**
     * An object that provides a MetaData via dataAccess
     */
    sealed trait HasMetaData {

        val dataAccess: MetaData
    }

    /**
     * Provides decorations that allow for .value/.value = _ to be called on metadata accessors
     *
     * @param access implicit access
     */
    implicit class MetaDataAccessDecorator(access: MetaDataAccess) {

        def value = access._1()
        def value_=(optStr: Option[String]) = access._2(optStr)
    }

    /**
     * Provides decoration that allow for easy access to MetaDataAssociation
     *
     * @param assoc implicit association
     */
    implicit class MetaDataAssociationDecorator(assoc: MetaDataAssociation) {

        def name    = assoc._1
        def access  = assoc._2
    }

    /**
     * Various assortments of collections for interacting with md/Symbol conversion and Localisation
     */
    object MetaDataAssociations extends AnyRef with Localization {
        val BySymbol = Map('album_performer -> md.ALBUMPERFORMER, 'album_songwriter -> md.ALBUMSONGWRITER,
                           'album_title -> md.ALBUMTITLE, 'catalog -> md.CATALOG, 'cd_text_file -> md.CDTEXTFILE,
                           'comment -> md.COMMENT, 'disc_id -> md.DISCID, 'genre -> md.GENRE, 'isrc_code -> md.ISRCCODE,
                           'performer -> md.PERFORMER, 'songwriter -> md.SONGWRITER, 'title -> md.TITLE, 
                           'track_number -> md.TRACKNUMBER, 'track_performer -> md.TRACKPERFORMER, 
                           'track_songwriter -> md.TRACKSONGWRITER, 'track_title -> md.TRACKTITLE, 'year -> md.YEAR)

        val ByOrdinal = BySymbol.map(_.swap)

        val Names = BySymbol.keys
        val Ordinals = BySymbol.values

        val Localisation = Names.map(sym => (sym, t"cue.meta_data.${sym.name}")).toMap
    }

    implicit def symbol2metadata(symbol: Symbol): md = MetaDataAssociations.BySymbol.get(symbol) match {
        case Some(metaData) => metaData
        case None           => throw new IllegalArgumentException("No such metadata exists")
    }

    implicit def metadata2symbol(metaData: md): Symbol = MetaDataAssociations.ByOrdinal(metaData)

    /**
     * Provides scala decorations for [[CueSheet]] and [[HasMetaData MetaData access]]
     *
     * @param sheet implicit CueSheet
     */
    implicit class CUESheetWrapper(sheet: CueSheet) extends HasMetaData {

        val dataAccess: MetaData =
            Map('performer -> (()=>performer, performer_=(_)), 'songwriter -> (()=>songwriter, songwriter_=(_)),
                'title -> (()=>title, title_=(_)), 'catalog -> (()=>catalog, catalog_=(_)),
                'cd_text_file -> (()=>cdTextFile, cdTextFile_=(_)), 'comment -> (()=>comment, comment_=(_)),
                'disc_id -> (()=>discID, discID_=(_)), 'genre -> (()=>genre, genre_=(_)),
                'year -> (()=>optInt2OptStr(year), (s: Option[String])=>year_=(s)))


        def metaData(field: Symbol): Option[String] = dataAccess.get(field) match {
            case Some(functions)    => functions._1()
            case _                  =>
                throw new IllegalArgumentException("Unsupported metadata field")
        }

        def performer = Option(sheet.getPerformer)
        def performer_=(name: Option[String]) = sheet.setPerformer(name.orNull)

        def songwriter = Option(sheet.getSongwriter)
        def songwriter_=(name: Option[String]) = sheet.setSongwriter(name.orNull)

        def title = Option(sheet.getTitle)
        def title_=(text: Option[String]) = sheet.setTitle(text.orNull)

        def catalog = Option(sheet.getCatalog)
        def catalog_=(cat: Option[String]) = sheet.setCatalog(cat.orNull)

        def cdTextFile = Option(sheet.getCdTextFile)
        def cdTextFile_=(file: Option[String]) = sheet.setCatalog(file.orNull)

        def comment = Option(sheet.getComment)
        def comment_=(text: Option[String]) = sheet.setComment(text.orNull)

        def discID = Option(sheet.getDiscid)
        def discID_=(id: Option[String]) = sheet.setDiscid(id.orNull)

        def genre = Option(sheet.getGenre)
        def genre_=(text: Option[String]) = sheet.setGenre(text.orNull)

        def year = if(sheet.getYear >= 0) Some(sheet.getYear) else None
        def year_=(num: Option[Int]) = sheet.setYear(num.getOrElse(-1))

        def fileData: Seq[FileData] = sheet.getFileData
        def fileData_=(data: Seq[FileData]) = {
            sheet.getFileData.clear()
            sheet.getFileData.addAll(data)
        }

        def trackData: Seq[TrackData] = sheet.getAllTrackData
    }

    /**
     * Provides scala decorators for [[FileData]]
     *
     * @param data implicit FileData
     */
    implicit class FileDataWrapper(data: FileData) {

        def indices: MutableSeq[Index] = data.getAllIndices

        def file = Option(data.getFile)
        def file_=(path: Option[String]) = data.setFile(path.orNull)

        def fileType = Option(data.getFileType)
        def fileType_=(name: Option[String]) = data.setFileType(name.orNull)

        def trackData: MutableSeq[TrackData] = data.getTrackData

        def lastTrack: Option[TrackData] = if(trackData.isEmpty) {
            None
        } else {
            Option(trackData
                    .sortWith(_.number.getOrElse(0) < _.number.getOrElse(0))
                    .last)
        }

        def lastTrackNumber: Option[Int] = lastTrack match {
            case Some(track)    => track.number
            case None           => None
        }

        def parent = data.getParent
        def parent_=(sheet: CueSheet) = data.setParent(sheet)
    }
    object FileEntry {

        /**
         * List of filetypes acknowledged by the CUE standard,
         * though the filetype field can be anything so long as the cue parser
         * supports that file type
         */
        val FileTypes = Seq("BINARY",   // Anything
                            "MOTOROLA", // Proprietary
                            "AIFF",     // AIFF Audio
                            "WAVE",     // Any audio data that is not AIFF or MP3, usually
                            "MP3")      // MP3 Audio
    }

    /**
     * Provides scala decorators for [[TrackData]] as well as [[HasMetaData]]
     *
     * @param data implicit TrackData
     */
    implicit class TrackDataWrapper(data: TrackData) extends HasMetaData {

        val dataAccess: MetaData =
            Map('isrc_code -> (()=>isrcCode, isrcCode_=(_)), 'performer -> (()=>performer, performer_=(_)),
                'songwriter -> (()=>songwriter, songwriter_=(_)), 'title -> (()=>title, title_=(_)),
                'track_number -> (()=>optInt2OptStr(number), (s:Option[String])=>number_=(s)))

        def dataType = Option(data.getDataType)
        def dataType_=(tpe: Option[String]) = data.setDataType(tpe.orNull)

        def isrcCode = Option(data.getIsrcCode)
        def isrcCode_=(code: Option[String]) = data.setIsrcCode(code.orNull)

        def number = if(data.getNumber >= 0) Some(data.getNumber) else None
        def number_=(num: Option[Int]) = data.setNumber(num.getOrElse(-1))

        def performer = Option(data.getPerformer)
        def performer_=(text: Option[String]) = data.setPerformer(text.orNull)

        def songwriter = Option(data.getSongwriter)
        def songwriter_=(text: Option[String]) = data.setSongwriter(text.orNull)

        def title = Option(data.getTitle)
        def title_=(text: Option[String]) = data.setTitle(text.orNull)

        def postgap = Option(data.getPostgap)
        def postgap_=(gap: Option[Position]) = data.setPostgap(gap.orNull)

        def pregap = Option(data.getPregap)
        def pregap_=(gap: Option[Position]) = data.setPregap(gap.orNull)

        def index(num: Int) = Option(data.getIndex(num))

        def indices: MutableSeq[Index] = data.getIndices
        def indices_=(seq: Seq[Index]): Unit = {
            data.getIndices.clear()
            data.getIndices.addAll(seq)
        }

        def flags: Iterable[String] = data.getFlags
        def flags_=(newFlags: Iterable[String]) = {
            data.getFlags.clear()
            data.getFlags.addAll(newFlags)
        }

        def parent = data.getParent
        def parent_=(fileData: FileData) = data.setParent(fileData)
    }
    object TrackEntry {

        /**
         * Describes all available track flags
         */
        val Flags = Seq("DCP",      // Digital copy permitted
                        "4CH",      // Four-channel audio
                        "PRE",      // Pre-emphasis enabled (audio only)
                        "SCMS")     // Serial Copy Management System

        /**
         * Describes all available data types
         */
        val DataTypes = Seq("AUDIO",        // Audio track
                            "CDG",          // Karaoke CD+G
                            "MODE1/2048",   // CD-ROM cooked data
                            "MODE1/2352",   // CD-ROM raw data
                            "MODE2/2336",   // CD-ROM-XA data
                            "MODE2/2352",   // CD-ROM-XA data
                            "CDI/2336",     // CD-I data
                            "CDI/2352")     // CD-I data
    }

    /**
     * Provides scala decorators for [[Index]]
     *
     * @param index implicit Index
     */
    implicit class IndexWrapper(index: Index) {

        def number = if(index.getNumber >= 0) Some(index.getNumber) else None
        def number_=(num: Option[Int]) = index.setNumber(num.getOrElse(-1))

        def position = Option(index.getPosition)
        def position_=(pos: Option[Position]) = index.setPosition(pos.orNull)
        
        def formatted =
            tf"cuesheet.index.format"(number.getOrElse(0),
                                      if (position.isDefined) position.get.formatted else t"ui.common.concept_none")
    }

    /**
     * Provides scala decorators for [[Position]]
     *
     * @param pos implicit Position
     */
    implicit class PositionWrapper(pos: Position) {

        def frames = if(pos.getFrames >= 0) Some(pos.getFrames) else None
        def frames_=(num: Option[Int]) = num match {
            case Some(number) if number >= 0    => pos.setFrames(number)
            case Some(number)                   =>
                throw new IllegalArgumentException("Frame count must be greater than or equal 0")
            case None                           => pos.setFrames(0)
        }

        def minutes = if(pos.getMinutes >= 0) Some(pos.getMinutes) else None
        def minutes_=(num: Option[Int]) = num match {
            case Some(number) if number >= 0    => pos.setMinutes(number)
            case Some(number)                   =>
                throw new IllegalArgumentException("Minute value must be greater than or equal to 0")
            case None                           => pos.setMinutes(0)
        }

        def seconds = if(pos.getSeconds >= 0) Some(pos.getSeconds) else None
        def seconds_=(num: Option[Int]) = num match {
            case Some(number) if number >= 0    => pos.setSeconds(number)
            case Some(number)                   =>
                throw new IllegalArgumentException("Second value must be greater than or equal to 0")
            case None                           => pos.setSeconds(0)
        }

        def totalFrames = pos.getTotalFrames

        /**
         * Returns a formatted string representing the position as a timestamp
         *
         * @return timestamp string
         */
        def formatted = tf"cuesheet.position.format"(minutes.getOrElse(0), seconds.getOrElse(0), frames.getOrElse(0))
    }
}
object CUEUtilities extends CUEUtilities