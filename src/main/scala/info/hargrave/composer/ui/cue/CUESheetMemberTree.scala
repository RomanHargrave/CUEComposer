package info.hargrave.composer.ui.cue

import info.hargrave.composer._
import info.hargrave.composer.ui.cue.cuelib.{ObservableFileData, ObservableTrackData, _}
import info.hargrave.composer.ui.{CustomTreeCell, Editable}
import info.hargrave.composer.util.CUEUtilities._

import jwbroek.cuelib.{FileData, TrackData}

import scala.collection.JavaConversions._
import scalafx.Includes._
import scalafx.collections.ObservableBuffer
import scalafx.collections.ObservableBuffer.{Add, Change, Remove}
import scalafx.event.subscriptions.Subscription
import scalafx.scene.Node
import scalafx.scene.control._
import scalafx.scene.layout.{Priority, VBox}

import CUESheetMemberTree.CueEntryCell

/**
 * Displays a CUE Sheet as a two-level TreeView[Either[FileData,TrackData]] from which the user can select file
 * sections,
 * or track declarations within the sections.
 *
 * It provides a selection callback that will provide the Either[...] selected by the user.
 *
 * When editable is true, it will display a toolbar that allows for addition and removal of sheet members.
 */
class CUESheetMemberTree(sheet: ObservableCueSheet) extends VBox with Editable {

    import CUESheetMemberTree.CueSheetMember

    def fileDataCollection = sheet.fileDataProperty

    // Element List ----------------------------------------------------------------------------------------------------
    private val elementsList = new TreeView[CueSheetMember] {

        root = new TreeItem
        showRoot = false
        cellFactory = view => new CueEntryCell
        vgrow = Priority.Always
    }

    fileDataCollection
        .map(data => new TreeItem[CueSheetMember](Left(data: ObservableFileData)) {
            children = data.trackData.map(tData => new TreeItem[CueSheetMember](Right(tData: ObservableTrackData)))
        })
        .foreach(elementsList.root.value.children.add(_))

    fileDataCollection.onChange((collection: ObservableBuffer[FileData], changes: Seq[Change]) => changes.foreach {
        case Add(_, elements: Traversable[FileData])    =>
            elements
                .map(data => new TreeItem[CueSheetMember](Left(ObservableFileData(data))) {
                    children = data.getTrackData.map( tData => new TreeItem[CueSheetMember](Right(ObservableTrackData(tData))) )
                })
                .foreach(elementsList.root.value.children.add(_))
        case Remove(_, elements)    =>
            elementsList.root.value.children
                .find { child => elements.exists(_.equals(child.value.value.left.get)) }
                .foreach(elementsList.root.value.children.remove(_))
        case _                      =>
    })

    // Toolbar ---------------------------------------------------------------------------------------------------------
    private val elementsToolbar = new ToolBar {
        visible.bind(editableProperty)
    }

    /*
     * 'Add' button allows for the adding of new tracks and files
     */
    private val addMemberBtn = new MenuButton {

        text = t"ui.common.verb_add"

        /*
         * Add a File to the current sheet.
         */
        val fileMemberOption = new MenuItem {

            text = t"cuesheet.file_entry"

            def updateDisabled(): Unit = {
                disable = fileDataCollection.length >= 99
            }

            fileDataCollection.onChange { updateDisabled() }
            onAction = () => fileDataCollection += new FileData(sheet)
        }

        /*
         * Add a Track to the currently selected file.
         * If a track is selected, the new track will be added to the parent, if it exists.
         */
        val trackMemberOption = new MenuItem {

            text = t"cuesheet.track_entry"

            def updateDisabled(): Unit = {
                disable = selectedItem match {
                    case Some(Left(fileData)) => fileData.trackData.length >= 99
                    case _                    => false
                }
            }

            onAction = () => {
                logger.trace("called")
                if (selectedItem.isDefined) {
                    val selectedFileData = selectedItem match {
                        case Some(Left(fileData))                 =>
                            fileData
                        case Some(Right(tData)) if tData.parent ? =>
                            tData.parent
                        case _                                    =>
                            throw new IllegalStateException(s"Operation cannot be performed on this item")
                    }

                    selectedFileData.trackData.add(new TrackData(selectedFileData))
                }
            }

            fileDataCollection.onChange { updateDisabled() }
            onSelectionChanged { (ign: Option[CueSheetMember]) => updateDisabled() }
        }

        items = Seq(fileMemberOption, trackMemberOption)
    }

    /*
     * Delete the currently selected member
     * TODO Needs to be debugged: not working consistently
     */
    private val delMemberBtn = new Button {

        def updateDisabled(): Unit = {
            disable = selectedItem.isEmpty || fileDataCollection.isEmpty
        }

        fileDataCollection.onChange { updateDisabled() }
        onSelectionChanged { (ign: Option[CueSheetMember]) => updateDisabled() }

        onAction = () => {
            selectedItem match {
                case Some(Left(fileData))   =>
                    fileDataCollection.remove(fileData)
                case Some(Right(trackData)) =>
                    trackData.parent.trackData.remove(trackData)
                    elementsList.selectionModel.value.select(null)
                case None                   => // Nothing
            }
        }

        text = t"ui.common.verb_remove"
    }

    elementsToolbar.items = Seq(addMemberBtn, delMemberBtn)

    // Setup Self ------------------------------------------------------------------------------------------------------

    children = Seq[Node](elementsToolbar, elementsList)

    // Component API ---------------------------------------------------------------------------------------------------
    final def selectedItem = {
        elementsList.selectionModel.value.getSelectedItem match {
            case null     => None
            case propItem => Option(propItem.value.value)
        }
    }

    final def onSelectionChanged(op: Option[CueSheetMember] => _): Subscription = {
        elementsList.selectionModel.value.selectedItemProperty.onChange({
                                                                            op(selectedItem)
                                                                            ()
                                                                        })
    }
}

object CUESheetMemberTree {

    type CueSheetMember = Either[ObservableFileData, ObservableTrackData]

    object CueEntryCell {

        import javafx.scene.control.{TreeItem => JTreeItem}

        // God this is a dirty kluge
        // TODO bugfix needed: of course this borks the member tree. what should I have expected?
        // This code causes intellij IDEA 14.0's Scala formatter to have a massive stroke.
        // don't hurt intellij IDEA by avoiding the formatter wherever you see anonymous matchers inside brackets,
        // because it will treat the first `case` statement as a parameter statement, and crap all over the formatting
        // in strange ways
        private def bindFileDataChildren(fileData: ObservableFileData,
                                         children: ObservableBuffer[JTreeItem[CueSheetMember]]): Subscription =
            fileData.trackDataProperty.onChange { (buffer: ObservableBuffer[TrackData], changes: Seq[Change]) =>
                changes.foreach {
                                    case Add(_, added: Traversable[TrackData]) =>
                                        added
                                            .map(n => new JTreeItem[CueSheetMember](Right(n: ObservableTrackData)))
                                            .foreach(children.add)
                                    case Remove(_, removed: Traversable[TrackData]) =>
                                        val deadChildren = for (removing <- removed) yield {
                                            children.filter { child =>
                                                child.value.value != null && child.value.value == Right(removing: ObservableTrackData)
                                            }
                                        }

                                        deadChildren.foreach(children.remove(_))

                                    case _ => // Pointless
                                }
            }
    }

    final class CueEntryCell extends CustomTreeCell[CueSheetMember] {

        import scalafx.Includes._

        private var childSubscription: Option[Subscription] = None

        override def updateItem(item: CueSheetMember, empty: Boolean): Unit = {
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
                            logger.trace(s"cell $this binding to $fileData")
                            childSubscription = Some(Seq(fileData.onInvalidate
                                                         { text = s"${ fileData.getFileType } ${ fileData.getFile }" },
                                                         CueEntryCell.bindFileDataChildren(fileData,
                                                                                           treeItem.value.children)))

                            fileData.invalidate()
                        case Right(trackData) =>
                            childSubscription =
                                    Option(trackData.onInvalidate {
                                        text = if (trackData.getNumber > 0) {
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