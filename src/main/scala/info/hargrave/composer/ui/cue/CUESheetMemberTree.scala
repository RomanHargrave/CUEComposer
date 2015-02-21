package info.hargrave.composer.ui.cue

import info.hargrave.composer._
import info.hargrave.composer.ui.cue.CUESheetMemberTree.CueEntryCell
import info.hargrave.composer.ui.cue.cuelib._
import info.hargrave.composer.ui.{CustomTreeCell, Editable}
import info.hargrave.composer.util.CUEUtilities._
import jwbroek.cuelib.{CueSheet, FileData, TrackData}

import scala.collection.JavaConversions._
import scalafx.Includes._
import scalafx.collections.ObservableBuffer
import scalafx.event.subscriptions.Subscription
import scalafx.scene.Node
import scalafx.scene.control._
import scalafx.scene.layout.{Priority, VBox}

/**
 * Displays a CUE Sheet as a two-level TreeView[Either[FileData,TrackData]] from which the user can select file
 * sections,
 * or track declarations within the sections.
 *
 * It provides a selection callback that will provide the Either[...] selected by the user.
 *
 * When editable is true, it will display a toolbar that allows for addition and removal of sheet members.
 */
class CUESheetMemberTree(sheet: CueSheet) extends VBox with Editable {

    import info.hargrave.composer.ui.cue.CUESheetMemberTree.ObservableMember

    val filesProperty = ObservableBuffer(Seq[FileData]())

    // Element List ----------------------------------------------------------------------------------------------------
    private val elementsList = new TreeView[ObservableMember] {
        root = new TreeItem
        showRoot = false
        cellFactory = view => new CueEntryCell
        vgrow = Priority.Always
    }

    private def synchronizeData(): Unit = {

        /*
         * For each file entry create an item, then construct a sequence of items for each track entry and assign them
         * to the file item as children.
         */
        elementsList.root.value.children = filesProperty.map(data => {
            new TreeItem[ObservableMember](Left(data: ObservableFileData)) {
                children = data.trackData.map(tData => {
                    new TreeItem[ObservableMember](Right(tData: ObservableTrackData))
                })
            }
        })
    }

    filesProperty.onChange {
                               synchronizeData()
                           }

    // Toolbar ---------------------------------------------------------------------------------------------------------
    private val elementsToolbar = new ToolBar {
        visible.bind(editableProperty)
    }
    private val addMemberBtn = new MenuButton {

        text = t"ui.common.verb_add"

        val fileMemberOption = new MenuItem {

            text = t"cuesheet.file_entry"

            def updateDisabled(): Unit = {
                disable = filesProperty.length >= 99
            }

            filesProperty.onChange {
                                       updateDisabled()
                                   }
            onAction = () => filesProperty += new FileData(sheet)
        }

        val trackMemberOption = new MenuItem {

            text = t"cuesheet.track_entry"

            def updateDisabled(): Unit = {
                disable = selectedItem match {
                    case Some(Left(fileData)) => fileData.trackData.length >= 99
                    case _                    => false
                }
            }

            onAction = () => selectedItem.get.left.get.getTrackData.add(new TrackData(selectedItem.get.left.get))

            filesProperty.onChange {
                                       updateDisabled()
                                   }
            onSelectionChanged { (ign: Option[Either[FileData, TrackData]]) => updateDisabled()}
        }

        items = Seq(fileMemberOption, trackMemberOption)
    }
    private val delMemberBtn = new Button {

        def updateDisabled(): Unit = {
            disable = selectedItem.isEmpty || filesProperty.isEmpty
        }

        filesProperty.onChange {
                                   updateDisabled()
                               }
        onSelectionChanged { (ign: Option[Either[FileData, TrackData]]) => updateDisabled()}

        onAction = () => {
            selectedItem match {
                case Some(Left(fileData))   =>
                    filesProperty.remove(fileData)
                case Some(Right(trackData)) =>

                    trackData.parent.getTrackData.remove(trackData)

                    elementsList.selectionModel.value.select(null)

                    synchronizeData()
                case None                   => // Nothing
            }
        }

        text = t"ui.common.verb_remove"
    }

    elementsToolbar.items = Seq(addMemberBtn, delMemberBtn)

    // Setup Self ------------------------------------------------------------------------------------------------------

    children = Seq[Node](elementsToolbar, elementsList)

    filesProperty.addAll(sheet.fileData)

    // Component API ---------------------------------------------------------------------------------------------------
    final def selectedItem = {
        elementsList.selectionModel.value.getSelectedItem match {
            case null     => None
            case propItem => Option(propItem.value.value)
        }
    }

    final def onSelectionChanged(op: Option[Either[FileData, TrackData]] => _): Subscription = {
        elementsList.selectionModel.value.selectedItemProperty.onChange({
                                                                            op(selectedItem)
                                                                            ()
                                                                        })
    }
}

object CUESheetMemberTree {

    import info.hargrave.composer.ui.cue.cuelib.{Observability, ObservableFileData, ObservableTrackData}

    type ObservableMember = Either[ObservableFileData, ObservableTrackData]

    final class CueEntryCell extends CustomTreeCell[ObservableMember] {

        private var childSubscription: Option[Subscription] = None

        override def updateItem(item: ObservableMember, empty: Boolean): Unit = {
            empty match {
                case true  =>
                    if (childSubscription.isDefined) {
                        childSubscription.get.cancel()
                        childSubscription = None
                    }
                    text = null
                case false =>
                    item match {
                        case Left(fileData)   =>
                            childSubscription =
                                    Option(fileData.onInvalidate { text = s"${fileData.getFileType} ${fileData.getFile}" })

                            fileData.invalidate()
                        case Right(trackData) =>
                            childSubscription =
                                    Option(trackData.onInvalidate {   text =  if (trackData.getNumber > 0) {
                                                                                  tf"ui.cue.track_entry"(trackData.getNumber)
                                                                              } else {
                                                                                  t"ui.cue.undefined_track"
                                                                              }
                                                                  })


                            trackData.invalidate()
                    }
            }
        }
    }

}