package info.hargrave.composer.util

import scala.collection.JavaConversions._

import jwbroek.cuelib._
import CueSheet.{MetaDataField => md}

import scala.collection.mutable.{Seq => MutableSeq, Set => MutableSet}

trait CUEUtilities {

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

    sealed trait HasMetaData {

        val dataAccess: MetaData
    }

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

        val Localisation = Names.map((_, t"cue.meta_data.${_}"))
    }

    implicit def symbol2metadata(symbol: Symbol): md = MetaDataAssociations.BySymbol.get(symbol) match {
        case Some(metaData) => metaData
        case None           => throw new IllegalArgumentException("No such metadata exists")
    }

    implicit def metadata2symbol(metaData: md): Symbol = MetaDataAssociations.ByOrdinal(metaData)

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
    }

    implicit class FileDataWrapper(data: FileData) {

        def indices: MutableSeq[Index] = data.getAllIndices

        def file = Option(data.getFile)
        def file_=(path: Option[String]) = data.setFile(path.orNull)

        def fileType = Option(data.getFileType)
        def fileType_=(name: Option[String]) = data.setFileType(name.orNull)

        def trackData = data.getTrackData

        def parent = data.getParent
        def parent_=(sheet: CueSheet) = data.setParent(sheet)
    }

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

        def flags: MutableSet[String] = data.getFlags

        def parent = data.getParent
        def parent_=(fileData: FileData) = data.setParent(fileData)
    }

    implicit class IndexWrapper(index: Index) {

        def number = if(index.getNumber >= 0) Some(index.getNumber) else None
        def number_=(num: Option[Int]) = index.setNumber(num.getOrElse(-1))

        def position = Option(index.getPosition)
        def position_=(pos: Option[Position]) = index.setPosition(pos.orNull)
    }

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
    }
}
object CUEUtilities extends CUEUtilities