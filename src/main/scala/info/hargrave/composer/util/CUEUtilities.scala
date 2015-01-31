package info.hargrave.composer.util

import scala.collection.JavaConversions._

import jwbroek.cuelib._
import CueSheet.{MetaDataField => md}

import scala.collection.mutable.{Seq => MutableSeq, Set => MutableSet}

trait CUEUtilities {

    implicit def symbol2metadata(symbol: Symbol): md = symbol match {
        case 'album_performer   => md.ALBUMPERFORMER
        case 'album_songwriter  => md.ALBUMSONGWRITER
        case 'album_title       => md.ALBUMTITLE
        case 'catalog           => md.CATALOG
        case 'cd_text_file      => md.CDTEXTFILE
        case 'comment           => md.COMMENT
        case 'disc_id           => md.DISCID
        case 'genre             => md.GENRE
        case 'isrc_code         => md.ISRCCODE
        case 'performer         => md.PERFORMER
        case 'songwriter        => md.SONGWRITER
        case 'title             => md.TITLE
        case 'track_number      => md.TRACKNUMBER
        case 'track_performer   => md.TRACKPERFORMER
        case 'track_songwriter  => md.TRACKSONGWRITER
        case 'track_title       => md.TRACKTITLE
        case 'year              => md.YEAR
        case _                  => throw new IllegalArgumentException("Invalid metadata field")
    }

    implicit def metadata2symbol(metaData: md): Symbol = metaData match {
        case md.ALBUMPERFORMER  => 'album_performer
        case md.ALBUMSONGWRITER => 'album_songwriter
        case md.ALBUMTITLE      => 'album_title
        case md.CATALOG         => 'catalog
        case md.CDTEXTFILE      => 'cd_text_file
        case md.COMMENT         => 'comment
        case md.DISCID          => 'disc_id
        case md.GENRE           => 'genre
        case md.ISRCCODE        => 'isrc_code
        case md.PERFORMER       => 'performer
        case md.SONGWRITER      => 'songwriter
        case md.TITLE           => 'title
        case md.TRACKNUMBER     => 'track_number
        case md.TRACKPERFORMER  => 'track_performer
        case md.TRACKSONGWRITER => 'track_songwriter
        case md.TRACKTITLE      => 'track_title
        case md.YEAR            => 'year
    }

    implicit class CUESheetWrapper(sheet: CueSheet) {

        def metaData(field: Symbol): Option[String] = field match {
            case 'album_performer | 'performer  => performer
            case 'album_songwriter| 'songwriter => songwriter
            case 'album_title | 'title          => title
            case 'catalog                       => catalog
            case 'cd_text_file                  => cdTextFile
            case 'comment                       => comment
            case 'disc_id                       => discID
            case 'genre                         => genre
            case 'year                          => year match {
                case Some(number) => Some(number.toString)
                case None => None
            }
            case _                              =>
                throw new IllegalArgumentException("Unsupported metadata field")
        }

        def performer = Option(sheet.getPerformer)
        def performer_=(name: Option[String]): Unit = sheet.setPerformer(name.orNull)

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

    implicit class TrackDataWrapper(data: TrackData) {

        def metaData(name: Symbol): Option[String] = name match {
            case 'isrc_code         => isrcCode
            case 'performer         => parent.parent.performer
            case 'track_performer   => performer
            case 'songwriter        => performer.orElse(parent.parent.songwriter)
            case 'track_songwriter  => songwriter
            case 'title             => title.orElse(parent.parent.title)
            case 'track_title       => title
            case 'track_number      => number match {
                case Some(num)  => Some(num.toString)
                case None       => None
            }
            case other              => parent.parent.metaData(other)
        }

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