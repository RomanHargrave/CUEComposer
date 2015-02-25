package info.hargrave.composer.test

import info.hargrave.composer.ui.cue.cuelib.{ObservableFileData, ObservableCueSheet}

import jwbroek.cuelib._

import org.scalatest._
import org.scalatest.concurrent.AsyncAssertions
import org.scalatest.time.{Milliseconds, Span}

import uk.co.jemos.podam.api.PodamFactoryImpl

import scala.collection.JavaConverters._

import info.hargrave.composer.ui.cue.cuelib._

import scala.util.Random

class CueLibObservabilityTest extends FreeSpec with Matchers with AsyncAssertions {

    val garbageInjector = new PodamFactoryImpl

    def listsEqual[T](a: Iterable[T], b: Iterable[T]): Boolean = a.zip(b).forall {case(x, y) => x == y}
    def randomIndices = (0 to 3).map(_ => garbageInjector.manufacturePojo(classOf[Index]))
    def randomTracks =
        (0 to 10)
            .map(_ => garbageInjector.manufacturePojo(classOf[TrackData]))
            .map { t => t.getIndices.addAll(randomIndices.asJava); t }
    def randomFiles =
        (0 to 10)
            .map(_ => garbageInjector.manufacturePojo(classOf[FileData]))
            .map { f => f.getTrackData.addAll(randomTracks.asJava); f}

    "An Observable CueSheet" - {
        "as an object" - {
            val cueSheet    = new CueSheet
            val observable  = ObservableCueSheet(cueSheet)

            "should be memoized" in {
                ObservableCueSheet(cueSheet) should be(observable)
            }

            "should not allow for nested conversion" in {
                ObservableCueSheet(observable) should be(observable)
            }

            "should be have field equality to the subordinate" in {
                observable should equal(cueSheet)
            }
        }
        "as a model" - {
            val cueSheet    = garbageInjector.manufacturePojo(classOf[CueSheet])
            cueSheet.getFileData.addAll(randomFiles.asJava)
            val observable  = ObservableCueSheet(cueSheet)

            "its properties" - {
                "should be identical to those of the cloned sheet" - {
                    "getCatalog" in {
                        observable.getCatalog       shouldEqual cueSheet.getCatalog
                    }
                    "getCdTextFile" in {
                        observable.getCdTextFile    shouldEqual cueSheet.getCdTextFile
                    }
                    "getPerformer" in {
                        observable.getPerformer     shouldEqual cueSheet.getPerformer
                    }
                    "getTitle" in {
                        observable.getTitle         shouldEqual cueSheet.getTitle
                    }
                    "getDiscId" in {
                        observable.getDiscid        shouldEqual cueSheet.getDiscid
                    }
                    "getGenre" in {
                        observable.getGenre         shouldEqual cueSheet.getGenre
                    }
                    "getYear" in {
                        observable.getYear          shouldEqual cueSheet.getYear
                    }
                    "getComment" in {
                        observable.getComment       shouldEqual cueSheet.getComment
                    }
                    "fileDataProperty" in {
                        assert(listsEqual(observable.getFileData.asScala, cueSheet.getFileData.asScala))
                    }
                }
                "should modify the subordinate sheet" - {
                    val moreGarbage = garbageInjector.manufacturePojo(classOf[CueSheet])
                    moreGarbage.getFileData.addAll(randomFiles.asJava)

                    "setCatalog" in {
                        observable.setCatalog(moreGarbage.getCatalog)
                        observable.getCatalog       shouldEqual cueSheet.getCatalog
                    }
                    "setCdTextFile" in {
                        observable.setCatalog(moreGarbage.getCdTextFile)
                        observable.getCdTextFile    shouldEqual cueSheet.getCdTextFile
                    }
                    "setPerformer" in {
                        observable.setPerformer(moreGarbage.getPerformer)
                        observable.getPerformer     shouldEqual cueSheet.getPerformer
                    }
                    "setTitle" in {
                        observable.setTitle(moreGarbage.getTitle)
                        observable.getTitle         shouldEqual cueSheet.getTitle
                    }
                    "setDiscId" in {
                        observable.setDiscid(moreGarbage.getDiscid)
                        observable.getDiscid        shouldEqual cueSheet.getDiscid
                    }
                    "setGenre" in {
                        observable.setGenre(moreGarbage.getGenre)
                        observable.getGenre         shouldEqual cueSheet.getGenre
                    }
                    "setYear" in {
                        observable.setYear(moreGarbage.getYear)
                        observable.getYear          shouldEqual cueSheet.getYear
                    }
                    "setComment" in {
                        observable.setComment(moreGarbage.getComment)
                        observable.getComment       shouldEqual cueSheet.getComment
                    }
                    "fileDataProperty" in {
                        observable.getFileData.clear()
                        observable.getFileData.addAll(moreGarbage.getFileData)
                        assert(listsEqual(observable.getFileData.asScala, cueSheet.getFileData.asScala))
                    }
                }
                "should react to mutation" - {
                    val moreGarbage = garbageInjector.manufacturePojo(classOf[CueSheet])
                    moreGarbage.getFileData.addAll(randomFiles.asJava)

                    "setCatalog" in {
                        val waiter      = new Waiter
                        observable.catalogProperty.onInvalidate { waiter.dismiss() }
                        observable.setCatalog(moreGarbage.getCatalog)
                        waiter.await()
                    }
                    "setCdTextFile" in {
                        val waiter      = new Waiter
                        observable.cdtFileProperty.onInvalidate { waiter.dismiss() }
                        observable.setCdTextFile(moreGarbage.getCdTextFile)
                        waiter.await()
                    }
                    "setPerformer" in {
                        val waiter      = new Waiter
                        observable.performerProperty.onInvalidate { waiter.dismiss() }
                        observable.setPerformer(moreGarbage.getPerformer)
                        waiter.await()
                    }
                    "setTitle" in {
                        val waiter      = new Waiter
                        observable.titleProperty.onInvalidate { waiter.dismiss() }
                        observable.setTitle(moreGarbage.getTitle)
                        waiter.await()
                    }
                    "setDiscId" in {
                        val waiter      = new Waiter
                        observable.discIdProperty.onInvalidate { waiter.dismiss() }
                        observable.setDiscid(moreGarbage.getDiscid)
                        waiter.await()
                    }
                    "setGenre" in {
                        val waiter      = new Waiter
                        observable.genreProperty.onInvalidate { waiter.dismiss() }
                        observable.setGenre(moreGarbage.getGenre)
                        waiter.await()
                    }
                    "setYear" in {
                        val waiter      = new Waiter
                        observable.yearProperty.onInvalidate { waiter.dismiss() }
                        observable.setYear(moreGarbage.getYear)
                        waiter.await()
                    }
                    "setComment" in {
                        val waiter      = new Waiter
                        observable.commentProperty.onInvalidate { waiter.dismiss() }
                        observable.setComment(moreGarbage.getComment)
                        waiter.await()
                    }
                    "fileDataProperty" in {
                        val waiter      = new Waiter
                        observable.fileDataProperty.onInvalidate { waiter.dismiss() }
                        observable.getFileData.addAll(moreGarbage.getFileData)
                        waiter.await()
                    }
                }
                "should invalidate the sheet" - {
                    val moreGarbage = garbageInjector.manufacturePojo(classOf[CueSheet])
                    moreGarbage.getFileData.addAll(randomFiles.asJava)

                    "setCatalog" in {
                        val waiter      = new Waiter
                        observable.onInvalidate { waiter.dismiss() }
                        observable.setCatalog(moreGarbage.getCatalog)
                        waiter.await()
                    }
                    "setCdTextFile" in {
                        val waiter      = new Waiter
                        observable.onInvalidate { waiter.dismiss() }
                        observable.setCdTextFile(moreGarbage.getCdTextFile)
                        waiter.await()
                    }
                    "setPerformer" in {
                        val waiter      = new Waiter
                        observable.onInvalidate { waiter.dismiss() }
                        observable.setPerformer(moreGarbage.getPerformer)
                        waiter.await()
                    }
                    "setTitle" in {
                        val waiter      = new Waiter
                        observable.onInvalidate { waiter.dismiss() }
                        observable.setTitle(moreGarbage.getTitle)
                        waiter.await()
                    }
                    "setDiscId" in {
                        val waiter      = new Waiter
                        observable.onInvalidate { waiter.dismiss() }
                        observable.setDiscid(moreGarbage.getDiscid)
                        waiter.await()
                    }
                    "setGenre" in {
                        val waiter      = new Waiter
                        observable.onInvalidate { waiter.dismiss() }
                        observable.setGenre(moreGarbage.getGenre)
                        waiter.await()
                    }
                    "setYear" in {
                        val waiter      = new Waiter
                        observable.onInvalidate { waiter.dismiss() }
                        observable.setYear(moreGarbage.getYear)
                        waiter.await()
                    }
                    "setComment" in {
                        val waiter      = new Waiter
                        observable.onInvalidate { waiter.dismiss() }
                        observable.setComment(moreGarbage.getComment)
                        waiter.await()
                    }
                    "fileDataProperty" in {
                        val waiter      = new Waiter
                        observable.onInvalidate { waiter.dismiss() }
                        observable.getFileData.addAll(moreGarbage.getFileData)
                        waiter.await()
                    }
                }
            }
        }
        "as a feature" - {
            val cueSheet    = garbageInjector.manufacturePojo(classOf[CueSheet])

            "should have implicit conversion" in {
                assert((cueSheet: ObservableCueSheet).isInstanceOf[ObservableCueSheet])
            }
        }
    }
    "An Observable FileData" - {
        "as an object" - {
            val fileData    = garbageInjector.manufacturePojo(classOf[FileData])
            fileData.getTrackData.addAll(randomTracks.asJava)

            val observable  = ObservableFileData(fileData)

            "should be memoized" in {
                ObservableFileData(fileData) should be(observable)
            }

            "should not allow for nested conversion" in {
                ObservableFileData(observable) should be(observable)
            }

            "should be have field equality to the subordinate" in {
                observable should equal(fileData)
            }
        }
        "as a model" - {
            "its properties" - {
                val fileData    = garbageInjector.manufacturePojo(classOf[FileData])

                fileData.getTrackData.addAll(randomTracks.asJava)

                val observable  = ObservableFileData(fileData)

                "should be identical to those of the cloned data" - {
                    "getFile" in {
                        observable.getFile shouldEqual fileData.getFile
                    }
                    "getFileType" in {
                        observable.getFileType shouldEqual fileData.getFileType
                    }
                    "getParent" in {
                        observable.getParent shouldEqual fileData.getParent
                    }
                    "getTrackData" in {
                        assert(listsEqual(observable.getTrackData.asScala, fileData.getTrackData.asScala))
                    }
                }
                "should modify the subordinate sheet" - {
                    val moreGarbage = garbageInjector.manufacturePojo(classOf[FileData])
                    moreGarbage.getTrackData.addAll(randomTracks.asJava)

                    "setFile" in {
                        observable.setFile(moreGarbage.getFile)
                        observable.getFile shouldEqual fileData.getFile
                    }
                    "setFileType" in {
                        observable.setFileType(moreGarbage.getFileType)
                        observable.getFileType shouldEqual fileData.getFileType
                    }
                    "setParent" in {
                        observable.setParent(moreGarbage.getParent)
                        observable.getParent shouldEqual fileData.getParent
                    }
                    "getTrackData" in {
                        observable.getTrackData.clear()
                        observable.getTrackData.addAll(moreGarbage.getTrackData)
                        assert(listsEqual(observable.getTrackData.asScala, fileData.getTrackData.asScala))
                    }
                }
                "should react to mutation" - {
                    val moreGarbage = garbageInjector.manufacturePojo(classOf[FileData])
                    moreGarbage.getTrackData.addAll(randomTracks.asJava)

                    "setFile" in {
                        val waiter  = new Waiter
                        observable.fileProperty.onInvalidate { waiter.dismiss() }
                        observable.setFile(moreGarbage.getFile)
                        waiter.await()
                    }
                    "setFileType" in {
                        val waiter  = new Waiter
                        observable.fileTypeProperty.onInvalidate { waiter.dismiss() }
                        observable.setFileType(moreGarbage.getFileType)
                        waiter.await()
                    }
                    "setParent" in {
                        val waiter  = new Waiter
                        observable.parentProperty.onInvalidate { waiter.dismiss() }
                        observable.setParent(moreGarbage.getParent)
                        waiter.await()
                    }
                    "trackDataProperty" in {
                        val waiter  = new Waiter
                        observable.trackDataProperty.onInvalidate { waiter.dismiss() }
                        observable.getTrackData.addAll(moreGarbage.getTrackData)
                        waiter.await()
                    }
                }
                "should invalidate the parent" - {
                    val moreGarbage = garbageInjector.manufacturePojo(classOf[FileData])
                    moreGarbage.getTrackData.addAll(randomTracks.asJava)

                    "setFile" in {
                        val waiter  = new Waiter
                        observable.onInvalidate { waiter.dismiss() }
                        observable.setFile(moreGarbage.getFile)
                        waiter.await()
                    }
                    "setFileType" in {
                        val waiter  = new Waiter
                        observable.onInvalidate { waiter.dismiss() }
                        observable.setFileType(moreGarbage.getFileType)
                        waiter.await()
                    }
                    "setParent" in {
                        val waiter  = new Waiter
                        observable.onInvalidate { waiter.dismiss() }
                        observable.setParent(moreGarbage.getParent)
                        waiter.await()
                    }
                    "getTrackData" in {
                        val waiter  = new Waiter
                        observable.onInvalidate { waiter.dismiss() }
                        observable.getTrackData.addAll(moreGarbage.getTrackData)
                        waiter.await()
                    }
                }
            }
            "its data should affect outcome of operations implemented in the superclass" - {
                val fileData    = garbageInjector.manufacturePojo(classOf[FileData])
                val tracks      = randomTracks
                fileData.getTrackData.addAll(tracks.asJava)

                val observable  = ObservableFileData(fileData)

                "getAllIndices" in {
                    assert(observable.getAllIndices.containsAll(tracks.flatMap(t => t.getIndices.asScala).asJava))
                }
            }
        }
        "as a feature" - {
            val fileData    = garbageInjector.manufacturePojo(classOf[FileData])

            "should have implicit conversion" in {
                assert((fileData: ObservableFileData).isInstanceOf[ObservableFileData])
            }
        }
    }
    "An Observable TrackData" - {
        "as an object" - {
            val trackData   = garbageInjector.manufacturePojo(classOf[TrackData])
            val observable  = ObservableTrackData(trackData)

            "should be memoized" in {
                ObservableTrackData(trackData) should be(observable)
            }

            "should not allow for nested conversion" in {
                ObservableTrackData(observable) should be(observable)
            }

            "should be have field equality to the subordinate" in {
                observable should equal(trackData)
            }
        }
        "as a model" - {
            val trackData   = garbageInjector.manufacturePojo(classOf[TrackData])
            val indices = randomIndices.asJava
            trackData.getIndices.addAll(indices)
            val flags   = (0 to 10).map(_ => (new Random).alphanumeric.take(6).toString()).asJavaCollection
            trackData.getFlags.addAll(flags)
            val observable  = ObservableTrackData(trackData)

            "its properties" - {
                "should be identical to those of the cloned data" - {
                    "getDataType" in {
                        observable.getDataType shouldEqual trackData.getDataType
                    }
                    "getIsrcCode" in {
                        observable.getIsrcCode shouldEqual trackData.getIsrcCode
                    }
                    "getNumber" in {
                        observable.getNumber shouldEqual trackData.getNumber
                    }
                    "getPerformer" in {
                        observable.getPerformer shouldEqual trackData.getPerformer
                    }
                    "getPostgap" in {
                        observable.getPostgap shouldEqual trackData.getPostgap
                    }
                    "getPregap" in {
                        observable.getPregap shouldEqual trackData.getPregap
                    }
                    "getSongwriter" in {
                        observable.getSongwriter shouldEqual trackData.getSongwriter
                    }
                    "getTitle" in {
                        observable.getTitle shouldEqual trackData.getTitle
                    }
                    "indicesProperty" in {
                        assert(listsEqual(observable.getIndices.asScala, trackData.getIndices.asScala))
                    }
                    "flagsProperty" in {
                        assert(listsEqual(observable.getFlags.asScala, trackData.getFlags.asScala))
                    }
                    "getParent" in {
                        observable.getParent shouldEqual trackData.getParent
                    }
                }
                "should modify the subordinate data" - {
                    val moreGarbage = garbageInjector.manufacturePojo(classOf[TrackData])
                    val indices = randomIndices
                    moreGarbage.getIndices.addAll(indices.asJava)
                    val flags   = (0 to 10).map(_ => (new Random).alphanumeric.take(6).toString()).asJavaCollection
                    moreGarbage.getFlags.addAll(flags)

                    "getDataType" in {
                        observable.setDataType(moreGarbage.getDataType)
                        observable.getDataType shouldEqual trackData.getDataType
                    }
                    "getIsrcCode" in {
                        observable.setIsrcCode(moreGarbage.getIsrcCode)
                        observable.getIsrcCode shouldEqual trackData.getIsrcCode
                    }
                    "getNumber" in {
                        observable.setNumber(moreGarbage.getNumber)
                        observable.getNumber shouldEqual trackData.getNumber
                    }
                    "getPerformer" in {
                        observable.setPerformer(moreGarbage.getPerformer)
                        observable.getPerformer shouldEqual trackData.getPerformer
                    }
                    "getPostgap" in {
                        observable.setPostgap(moreGarbage.getPostgap)
                        observable.getPostgap shouldEqual trackData.getPostgap
                    }
                    "getPregap" in {
                        observable.setPregap(moreGarbage.getPregap)
                        observable.getPregap shouldEqual trackData.getPregap
                    }
                    "getSongwriter" in {
                        observable.setSongwriter(moreGarbage.getSongwriter)
                        observable.getSongwriter shouldEqual trackData.getSongwriter
                    }
                    "getTitle" in {
                        observable.setTitle(moreGarbage.getTitle)
                        observable.getTitle shouldEqual trackData.getTitle
                    }
                    "indicesProperty" in {
                        observable.getIndices.clear()
                        observable.getIndices.addAll(moreGarbage.getIndices)
                        assert(listsEqual(observable.getIndices.asScala, trackData.getIndices.asScala))
                    }
                    "flagsProperty" in {
                        observable.getFlags.clear()
                        observable.getFlags.addAll(moreGarbage.getFlags)
                        assert(listsEqual(observable.getFlags.asScala, trackData.getFlags.asScala))
                    }
                    "getParent" in {
                        observable.setParent(moreGarbage.getParent)
                        observable.getParent shouldEqual trackData.getParent
                    }
                }
                "should react to mutation" - {
                    val moreGarbage = garbageInjector.manufacturePojo(classOf[TrackData])
                    val indices = randomIndices
                    moreGarbage.getIndices.addAll(indices.asJava)
                    val flags   = (0 to 10).map(_ => (new Random).alphanumeric.take(6).toString()).asJavaCollection
                    moreGarbage.getFlags.addAll(flags)

                    "getDataType" in {
                        val waiter  = new Waiter
                        observable.dataTypeProperty.onInvalidate { waiter.dismiss() }
                        observable.setDataType(moreGarbage.getDataType)
                        waiter.await()
                    }
                    "getIsrcCode" in {
                        val waiter  = new Waiter
                        observable.isrcCodeProperty.onInvalidate { waiter.dismiss() }
                        observable.setIsrcCode(moreGarbage.getIsrcCode)
                        waiter.await()
                    }
                    "getNumber" in {
                        val waiter  = new Waiter
                        observable.numberProperty.onInvalidate { waiter.dismiss() }
                        observable.setNumber(moreGarbage.getNumber)
                        waiter.await()
                    }
                    "getPerformer" in {
                        val waiter  = new Waiter
                        observable.performerProperty.onInvalidate { waiter.dismiss() }
                        observable.setPerformer(moreGarbage.getPerformer)
                        waiter.await()
                    }
                    "getPostgap" in {
                        val waiter  = new Waiter
                        observable.postgapProperty.onInvalidate { waiter.dismiss() }
                        observable.setPostgap(moreGarbage.getPostgap)
                        waiter.await()
                    }
                    "getPregap" in {
                        val waiter  = new Waiter
                        observable.pregapProperty.onInvalidate { waiter.dismiss() }
                        observable.setPregap(moreGarbage.getPregap)
                        waiter.await()
                    }
                    "getSongwriter" in {
                        val waiter  = new Waiter
                        observable.songwriterProperty.onInvalidate { waiter.dismiss() }
                        observable.setSongwriter(moreGarbage.getSongwriter)
                        waiter.await()
                    }
                    "getTitle" in {
                        val waiter  = new Waiter
                        observable.titleProperty.onInvalidate { waiter.dismiss() }
                        observable.setTitle(moreGarbage.getTitle)
                        waiter.await()
                    }
                    "indicesProperty" in {
                        val waiter  = new Waiter
                        observable.indicesProperty.onInvalidate { waiter.dismiss() }
                        observable.getIndices.addAll(moreGarbage.getIndices)
                        waiter.await()
                    }
                    "flagsProperty" in {
                        val waiter  = new Waiter
                        observable.flagsProperty.onInvalidate { waiter.dismiss() }
                        observable.getFlags.addAll(Set("bork").asJava)
                        waiter.await(timeout(Span(500, Milliseconds)))
                    }
                    "getParent" in {
                        val waiter  = new Waiter
                        observable.parentProperty.onInvalidate { waiter.dismiss() }
                        observable.setParent(moreGarbage.getParent)
                        waiter.await()
                    }
                }
                "should invalidate the parent" - {
                    val moreGarbage = garbageInjector.manufacturePojo(classOf[TrackData])
                    val indices = randomIndices
                    moreGarbage.getIndices.addAll(indices.asJava)
                    val flags   = (0 to 10).map(_ => (new Random).alphanumeric.take(6).toString()).asJavaCollection
                    moreGarbage.getFlags.addAll(flags)

                    "getDataType" in {
                        val waiter  = new Waiter
                        observable.onInvalidate { waiter.dismiss() }
                        observable.setDataType(moreGarbage.getDataType)
                        waiter.await()
                    }
                    "getIsrcCode" in {
                        val waiter  = new Waiter
                        observable.onInvalidate { waiter.dismiss() }
                        observable.setIsrcCode(moreGarbage.getIsrcCode)
                        waiter.await()
                    }
                    "getNumber" in {
                        val waiter  = new Waiter
                        observable.onInvalidate { waiter.dismiss() }
                        observable.setNumber(moreGarbage.getNumber)
                        waiter.await()
                    }
                    "getPerformer" in {
                        val waiter  = new Waiter
                        observable.onInvalidate { waiter.dismiss() }
                        observable.setPerformer(moreGarbage.getPerformer)
                        waiter.await()
                    }
                    "getPostgap" in {
                        val waiter  = new Waiter
                        observable.onInvalidate { waiter.dismiss() }
                        observable.setPostgap(moreGarbage.getPostgap)
                        waiter.await()
                    }
                    "getPregap" in {
                        val waiter  = new Waiter
                        observable.onInvalidate { waiter.dismiss() }
                        observable.setPregap(moreGarbage.getPregap)
                        waiter.await()
                    }
                    "getSongwriter" in {
                        val waiter  = new Waiter
                        observable.onInvalidate { waiter.dismiss() }
                        observable.setSongwriter(moreGarbage.getSongwriter)
                        waiter.await()
                    }
                    "getTitle" in {
                        val waiter  = new Waiter
                        observable.onInvalidate { waiter.dismiss() }
                        observable.setTitle(moreGarbage.getTitle)
                        waiter.await()
                    }
                    "indicesProperty" in {
                        val waiter  = new Waiter
                        observable.onInvalidate { waiter.dismiss() }
                        observable.getIndices.addAll(moreGarbage.getIndices)
                        waiter.await()
                    }
                    "flagsProperty" in {
                        val waiter  = new Waiter
                        observable.onInvalidate { waiter.dismiss() }
                        observable.getFlags.addAll(Set("foo").asJava)
                        waiter.await()
                    }
                    "getParent" in {
                        val waiter  = new Waiter
                        observable.onInvalidate { waiter.dismiss() }
                        observable.setParent(moreGarbage.getParent)
                        waiter.await()
                    }
                }
            }
            "its data should affect outcome of operations implemented in the superclass" - {
                val moreGarbage = garbageInjector.manufacturePojo(classOf[TrackData])
                val indices = (0 to 10).map(_ => garbageInjector.manufacturePojo(classOf[Index]))
                moreGarbage.getIndices.addAll(indices.asJava)

                "getIndex(Int)" in {
                    observable.getIndices.clear()
                    observable.getIndices.addAll(moreGarbage.getIndices)

                    // List index 0, not Index(0)
                    val pickIdx = moreGarbage.getIndices.get(0)

                    observable.getIndex(pickIdx.getNumber) shouldBe pickIdx
                }
            }
        }
        "as a feature" - {
            val trackData   = garbageInjector.manufacturePojo(classOf[TrackData])

            "it should provide implicit conversion" in {
                assert((trackData: ObservableTrackData).isInstanceOf[ObservableTrackData])
            }
        }
    }
    "An Observable Index" - {
        "as an object" - {
            val index       = garbageInjector.manufacturePojo(classOf[Index])
            val observable  = ObservableIndex(index)

            "should be memoized" in {
                ObservableIndex(index) should be(observable)
            }
            "should not allow nested conversion" in {
                ObservableIndex(observable) should be(observable)
            }

            "should be have field equality to the subordinate" in {
                observable should equal(index)
            }
        }
        "as a model" - {
            val index       = garbageInjector.manufacturePojo(classOf[Index])
            val observable  = ObservableIndex(index)

            "should be identical to those of the cloned data" - {
                "getNumber" in {
                    observable.getNumber shouldEqual index.getNumber
                }
                "getPosition" in {
                    observable.getPosition shouldEqual index.getPosition
                }
            }
            "should modify the subordinate data" - {
                val moreJunk    = garbageInjector.manufacturePojo(classOf[Index])

                "setNumber" in {
                    observable.setNumber(moreJunk.getNumber)
                    observable.getNumber shouldEqual index.getNumber
                }
                "setPosition" in {
                    observable.setPosition(moreJunk.getPosition)
                    observable.getPosition shouldEqual index.getPosition
                }
            }
            "should react to mutation" - {
                val moreJunk    = garbageInjector.manufacturePojo(classOf[Index])

                "setNumber" in {
                    val waiter  = new Waiter
                    observable.numberProperty.onInvalidate { waiter.dismiss() }
                    observable.setNumber(moreJunk.getNumber)
                    waiter.await()
                }
                "setPosition" in {
                    val waiter  = new Waiter
                    observable.positionProperty.onInvalidate { waiter.dismiss() }
                    observable.setPosition(moreJunk.getPosition)
                    waiter.await()
                }
            }
            "should invalidate the index when modified" - {
                val moreJunk    = garbageInjector.manufacturePojo(classOf[Index])

                "setNumber" in {
                    val waiter  = new Waiter
                    observable.onInvalidate { waiter.dismiss() }
                    observable.setNumber(moreJunk.getNumber)
                    waiter.await()
                }
                "setPosition" in {
                    val waiter  = new Waiter
                    observable.onInvalidate { waiter.dismiss() }
                    observable.setPosition(moreJunk.getPosition)
                    waiter.await()
                }
            }
        }
        "as a feature" - {
            val index   = garbageInjector.manufacturePojo(classOf[Index])

            "should provide implicit conversion" in {
                assert((index: ObservableIndex).isInstanceOf[ObservableIndex])
            }
        }
    }
    "An Observable Position" - {
        "as an object" - {
            val position    = garbageInjector.manufacturePojo(classOf[Position])
            val observable  = ObservablePosition(position)

            "should be memoized" in {
                ObservablePosition(position) shouldBe observable
            }

            "should not allow nested conversion" in {
                ObservablePosition(observable) shouldBe observable
            }

            "should be have field equality to the subordinate" in {
                observable should equal(position)
            }
        }
        "as a model" - {
            val position    = garbageInjector.manufacturePojo(classOf[Position])
            val observable  = ObservablePosition(position)

            "should be identical to the cloned position" - {
                "getFrames" in {
                    observable.getFrames shouldEqual position.getFrames
                }
                "getMinutes" in {
                    observable.getMinutes shouldEqual position.getMinutes
                }
                "getSeconds" in {
                    observable.getSeconds shouldEqual position.getSeconds
                }
            }
            "should mutate the subordinate data" - {
                val moreJunk = garbageInjector.manufacturePojo(classOf[Position])

                "setFrames" in {
                    observable.setFrames(moreJunk.getFrames)
                    position.getFrames shouldEqual moreJunk.getFrames
                }
                "setMinutes" in {
                    observable.setMinutes(moreJunk.getMinutes)
                    position.getMinutes shouldEqual moreJunk.getMinutes
                }
                "setSeconds" in {
                    observable.setSeconds(moreJunk.getSeconds)
                    position.getSeconds shouldEqual moreJunk.getSeconds
                }
            }
            "should react to mutation" - {
                val moreJunk = garbageInjector.manufacturePojo(classOf[Position])

                "setFrames" in {
                    val waiter  = new Waiter
                    observable.onInvalidate({ waiter.dismiss() })
                    observable.setFrames(moreJunk.getFrames)
                    waiter.await()

                }
                "setMinutes" in {
                    val waiter  = new Waiter
                    observable.onInvalidate({ waiter.dismiss() })
                    observable.setMinutes(moreJunk.getMinutes)
                    waiter.await()
                }
                "setSeconds" in {
                    val waiter  = new Waiter
                    observable.onInvalidate({ waiter.dismiss() })
                    observable.setSeconds(moreJunk.getSeconds)
                    waiter.await()
                }
            }
        }
        "as a feature" - {
            val position    = garbageInjector.manufacturePojo(classOf[Position])

            "should provide implicit conversion" in {
                assert((position: ObservablePosition).isInstanceOf[ObservablePosition])
            }
        }
    }
}
