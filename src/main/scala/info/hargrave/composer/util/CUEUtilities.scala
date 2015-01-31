package info.hargrave.composer.util

import jwbroek.cuelib.CueSheet
import CueSheet.{MetaDataField => md}

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
}
