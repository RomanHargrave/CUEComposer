package info.hargrave.composer.ui.cue.cuelib

import jwbroek.cuelib._

/**
 * Implements implicit converters from CUELib models to observable counterparts
 */
trait ObservableConverters {

    /**
     * Convert a CueSheet to an Observable CueSheet
     *
     * @param sheet implicit CueSheet
     * @return observable CueSheet
     */
    implicit def cueSheet2Observable(sheet: CueSheet): ObservableCueSheet = ObservableCueSheet(sheet)

    /**
     * Convert a FileData instance to an Observable FileData
     *
     * @param data implicit FileData
     * @return observable FileData
     */
    implicit def fileData2Observable(data: FileData): ObservableFileData = ObservableFileData(data)

    /**
     * Convert a TrackData instance to an Observable TrackData
     *
     * @param data implicit TrackData
     * @return observable TrackData
     */
    implicit def trackData2Observable(data: TrackData): ObservableTrackData = ObservableTrackData(data)

    /**
     * Convert an Index to an Observable Index
     *
     * @param index implicit Index
     * @return observable Index
     */
    implicit def index2Observable(index: Index): ObservableIndex = ObservableIndex(index)

    /**
     * Convert a Position to an Observable Position
     *
     * @param position implicit Position
     * @return observable Position
     */
    implicit def position2Observable(position: Position): ObservablePosition = ObservablePosition(position)

}
