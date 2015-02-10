package info.hargrave.composer.test

import info.hargrave.composer.ui.cue.cuelib.{ObservableFileData, ObservableCueSheet}

import jwbroek.cuelib.{FileData, CueSheet}

import org.scalatest._
import org.scalatest.concurrent.AsyncAssertions

import uk.co.jemos.podam.api.PodamFactoryImpl

import scala.collection.JavaConverters._

import info.hargrave.composer.ui.cue.cuelib._

class CueLibObservabilityTest extends FreeSpec with Matchers with AsyncAssertions {

    val garbageInjector = new PodamFactoryImpl

    def listsEqual[T](a: Iterable[T], b: Iterable[T]): Boolean = a.zip(b).forall {case(x, y) => x == y}

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
        }
        "as a model" - {
            val cueSheet    = garbageInjector.manufacturePojo(classOf[CueSheet])
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
                    "getFileData" in {
                        assert(listsEqual(observable.getFileData.asScala, cueSheet.getFileData.asScala))
                    }
                }
                "should modify the subordinate sheet" - {
                    val moreGarbage = garbageInjector.manufacturePojo(classOf[CueSheet])

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
                    "getFileData" in {
                        observable.getFileData.clear()
                        observable.getFileData.addAll(moreGarbage.getFileData)
                        assert(listsEqual(observable.getFileData.asScala, cueSheet.getFileData.asScala))
                    }
                }
                "should react to mutation" - {
                    val moreGarbage = garbageInjector.manufacturePojo(classOf[CueSheet])

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
                    "setFileData" in {
                        val waiter      = new Waiter
                        observable.fileDataProperty.onInvalidate { waiter.dismiss() }
                        observable.getFileData.addAll(moreGarbage.getFileData)
                        waiter.await()
                    }
                }
                "should invalidate the sheet" - {
                    val moreGarbage = garbageInjector.manufacturePojo(classOf[CueSheet])

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
                    "setFileData" in {
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
            val cueSheet    = new CueSheet
            val fileData    = new FileData(cueSheet)
            val observable  = ObservableFileData(fileData)

            "should be memoized" in {
                ObservableFileData(fileData) should be(observable)
            }

            "should not allow for nested conversion" in {
                ObservableFileData(observable) should be(observable)
            }
        }
        "as a model" - {
            val fileData    = garbageInjector.manufacturePojo(classOf[FileData])
            val observable  = ObservableFileData(fileData)

            "its properties" - {
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
                    "getTrackData" in {
                        val waiter  = new Waiter
                        observable.trackData.onInvalidate { waiter.dismiss() }
                        observable.getTrackData.addAll(moreGarbage.getTrackData)
                        waiter.await()
                    }
                }
                "should invalidate the parent" - {
                    val moreGarbage = garbageInjector.manufacturePojo(classOf[FileData])

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
        }
        "as a feature" - {
            val fileData    = garbageInjector.manufacturePojo(classOf[FileData])

            "should have implicit conversion" in {
                assert((fileData: ObservableFileData).isInstanceOf[ObservableFileData])
            }
        }
    }
}
